---
title: Working with recursive functions
summary: The idea of recursion, the base case and the recursive step, the call stack, stack overflow, recursion versus iteration, the Towers of Hanoi, converting a number to binary.
---

## Why it matters

A folder on a computer contains folders, and those contain folders again. How do you compute the "size of a folder"? The answer is simple: the size of a folder = the size of its files + the size of each subfolder. The definition uses "the size of a folder" itself — that is **recursion**.

Recursion means solving a problem through a smaller copy of itself. It is the natural tool for tree-like structures (folders, family trees, the elements of a web page), "divide and conquer" algorithms (quicksort, binary search) and many mathematical definitions. Some problems are much harder to write without it.

:::note
**After this lesson you will be able to:**

- identify the two parts of a recursive function — the base case and the recursive step;
- explain how the call stack works and why it overflows;
- solve problems such as the factorial, raising to a power and the binary form recursively;
- solve the Towers of Hanoi with recursion;
- make a reasoned choice between recursion and a loop.
:::

## The idea of recursion

A **recursive function** is a function that calls itself. A correct recursive function always has two parts:

| Part | Purpose | In the factorial |
|---|---|---|
| **Base case** | the simplest case, whose answer is known directly; recursion stops here | for *n* = 0, 0! = 1 |
| **Recursive step** | the problem is reduced to a smaller copy of itself | *n*! = *n* · (*n* − 1)! |

Every recursive call must bring the problem **closer** to the base case: *n* → *n* − 1 → … → 0. Otherwise the recursion is endless.

Recursive thinking: "If I trust that I can solve the smaller problem, how do I solve the big one?" For the factorial: assume (*n* − 1)! is known, then *n*! = *n* · (*n* − 1)!.

## The call stack

When a function is called, the computer sets aside a **frame** in memory for its parameters and local variables and puts it on the **call stack**. When the function ends, the frame is removed from the stack. A stack is a "last in, first out" structure, like a pile of plates.

When `faktorial(3)` is called:

| Step | Stack (top on the right) | What happens |
|---|---|---|
| 1 | f(3) | needs 3 · f(2) — waits |
| 2 | f(3) f(2) | 2 · f(1) — waits |
| 3 | f(3) f(2) f(1) | 1 · f(0) — waits |
| 4 | f(3) f(2) f(1) f(0) | the base case: returns 1 |
| 5 | f(3) f(2) f(1) | returns 1 · 1 = 1 |
| 6 | f(3) f(2) | returns 2 · 1 = 2 |
| 7 | f(3) | returns 3 · 2 = 6 |

Recursion works in two phases: **going down** (calls until the base case) and **coming back up** (the results are gathered in reverse order).

The stack has a limited size. Recursion that is too deep, or has no base case, leads to a **stack overflow**:

| Language | Error |
|---|---|
| Python | `RecursionError: maximum recursion depth exceeded` (the limit is usually 1000) |
| Java | `StackOverflowError` |
| C++ | the program crashes (segmentation fault) |

## Recursion or a loop?

Any recursion can be written as a loop and vice versa. The choice depends on the problem:

| Recursion fits | A loop fits |
|---|---|
| the structure is a tree (folders, family trees) | a simple sequence (a sum, the factorial) |
| "divide and conquer" (Hanoi, quicksort) | the depth may be very large |
| the definition itself is recursive | speed and memory matter |

Be careful: the "direct" recursion for Fibonacci numbers, `fib(n) = fib(n-1) + fib(n-2)`, computes the same values again and again and is noticeably slow already at *n* = 40 (the number of calls grows exponentially). The loop from A7 does it instantly.

## Worked example 1: the recursive factorial and digit sum

An integer *n* (0 ≤ *n* ≤ 20) is given. Compute *n*! and the sum of the digits of *n* with recursive functions.

```python
def faktorial(n):
    if n == 0:                       # the base case
        return 1
    return n * faktorial(n - 1)      # the recursive step


def raqamlar_yigindisi(n):
    if n < 10:                       # the base case: one digit
        return n
    return n % 10 + raqamlar_yigindisi(n // 10)


n = int(input())
print(faktorial(n), raqamlar_yigindisi(n))
```
```java
import java.util.Scanner;

public class Main {
    static long faktorial(int n) {
        if (n == 0) {                    // the base case
            return 1;
        }
        return n * faktorial(n - 1);     // the recursive step
    }

    static int raqamlarYigindisi(int n) {
        if (n < 10) {                    // the base case: one digit
            return n;
        }
        return n % 10 + raqamlarYigindisi(n / 10);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        System.out.println(faktorial(n) + " " + raqamlarYigindisi(n));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long faktorial(int n) {
    if (n == 0) {                    // the base case
        return 1;
    }
    return n * faktorial(n - 1);     // the recursive step
}

int raqamlarYigindisi(int n) {
    if (n < 10) {                    // the base case: one digit
        return n;
    }
    return n % 10 + raqamlarYigindisi(n / 10);
}

int main() {
    int n;
    cin >> n;
    cout << faktorial(n) << " " << raqamlarYigindisi(n) << endl;
    return 0;
}
```

For the input `5`: `faktorial(5)` = 5 · 4 · 3 · 2 · 1 · 1 = 120; `raqamlar_yigindisi(5)` ("digit sum") = 5 (the base case straight away). The output is `120 5`.

For the input `12`: 12! = 479001600; the digits: `12 % 10 + sum(1)` = 2 + 1 = 3. The output is `479001600 3`.

Both functions check the base case first — by habit the first line of a recursive function.

## Worked example 2: the Towers of Hanoi

There are three rods: A, B and C. Rod A holds *n* discs — the largest at the bottom, the smallest on top. All the discs must be moved from A to C. The rules: only one disc is moved at a time; a larger disc is never placed on a smaller one. Print every move.

The recursive idea: to move *n* discs from A to C —

1. move the top *n* − 1 discs from A to B (C is the helper);
2. move the largest disc from A to C;
3. move the *n* − 1 discs from B to C (A is the helper).

The base case: *n* = 0 — do nothing.

```python
def xanoy(n, qayerdan, qayerga, yordamchi):
    if n == 0:
        return
    xanoy(n - 1, qayerdan, yordamchi, qayerga)
    print(qayerdan, "->", qayerga)
    xanoy(n - 1, yordamchi, qayerga, qayerdan)


n = int(input())
xanoy(n, "A", "C", "B")
```
```java
import java.util.Scanner;

public class Main {
    static void xanoy(int n, char qayerdan, char qayerga, char yordamchi) {
        if (n == 0) {
            return;
        }
        xanoy(n - 1, qayerdan, yordamchi, qayerga);
        System.out.println(qayerdan + " -> " + qayerga);
        xanoy(n - 1, yordamchi, qayerga, qayerdan);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        xanoy(n, 'A', 'C', 'B');
    }
}
```
```cpp
#include <iostream>
using namespace std;

void xanoy(int n, char qayerdan, char qayerga, char yordamchi) {
    if (n == 0) {
        return;
    }
    xanoy(n - 1, qayerdan, yordamchi, qayerga);
    cout << qayerdan << " -> " << qayerga << endl;
    xanoy(n - 1, yordamchi, qayerga, qayerdan);
}

int main() {
    int n;
    cin >> n;
    xanoy(n, 'A', 'C', 'B');
    return 0;
}
```

For the input `3`, 7 moves are printed (*qayerdan* — from, *qayerga* — to, *yordamchi* — helper):

```text
A -> C
A -> B
C -> B
A -> C
B -> A
B -> C
A -> C
```

The number of moves is 2ⁿ − 1: 3 discs — 7, 10 discs — 1023, 64 discs — over 18 quintillion. The recursive solution is three lines; a loop-based one is much more complicated. This is the classic case of recursion "in its right place": we do not think through every move, we only say how to reduce the problem to a smaller one.

## Worked example 3: the binary form

A natural number *n* is given. Print its form in the binary number system.

The binary digits are the remainders of division by 2, but **in reverse order**: the last remainder is the first digit. Recursion reverses the order by itself: first the binary form of *n* // 2 is printed, then *n* % 2.

```python
def ikkilik(n):
    if n >= 2:
        ikkilik(n // 2)          # the higher digits first
    print(n % 2, end="")         # then the last digit


n = int(input())
ikkilik(n)
print()
```
```java
import java.util.Scanner;

public class Main {
    static void ikkilik(int n) {
        if (n >= 2) {
            ikkilik(n / 2);          // the higher digits first
        }
        System.out.print(n % 2);     // then the last digit
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        ikkilik(n);
        System.out.println();
    }
}
```
```cpp
#include <iostream>
using namespace std;

void ikkilik(int n) {
    if (n >= 2) {
        ikkilik(n / 2);          // the higher digits first
    }
    cout << n % 2;               // then the last digit
}

int main() {
    int n;
    cin >> n;
    ikkilik(n);
    cout << endl;
    return 0;
}
```

For the input `13`: `ikkilik(13)` → `ikkilik(6)` → `ikkilik(3)` → `ikkilik(1)`. The base case prints `1`, then on the way back: 3 % 2 = `1`, 6 % 2 = `0`, 13 % 2 = `1`. The output is `1101` (8 + 4 + 0 + 1 = 13).

Because the printing comes **after** the recursive call, the digits are printed during the "coming back" phase, that is, in reverse order. If `print` came before the call, the result would be reversed: `1011`.

:::warning
**Common mistakes**

- **No base case, or one that cannot be reached.** If `faktorial(n - 1)` is called with a negative *n*, or `n == 0` is not checked, the stack overflows.
- **Not returning the result of the recursive call.** `faktorial(n - 1) * n` is computed, but without `return` the result is lost.
- **Recursion that is too deep.** In Python a depth over 1000 gives `RecursionError`; use a loop in such cases.
- **Repeating the same calculations.** The plain recursion for Fibonacci makes hundreds of millions of calls at *n* = 40.
- **Collecting the result in a global variable.** Let a recursive function give its result through `return`.
:::

:::tip
Write a recursive function like this: the base case first, then the recursive step with the thought "I assume the smaller problem is already solved". Do not try to trace the function through its whole depth — if one step is right, the whole recursion is right.
:::

## Summary

- A recursive function calls itself; it must have a base case and a recursive step that makes the problem smaller.
- Each call gets a frame on the call stack; recursion goes down, then comes back gathering the results.
- The stack is limited: recursion without a base case, or too deep, overflows it.
- Recursion fits tree-like and "divide and conquer" problems (the Towers of Hanoi: 2ⁿ − 1 moves).
- Actions after the call are carried out in reverse order (the binary form).
- For simple sequences and large depths a loop is preferable.

## Check yourself

:::check
Q: Which two parts must a recursive function have?
A: A base case (the simplest case, where recursion stops) and a recursive step that brings the problem closer to the base case.
Q: What is wrong with `def f(n): return n + f(n - 1)`?
A: There is no base case: the recursion never stops and the stack overflows (`RecursionError` in Python).
Q: How many moves does the Towers of Hanoi need for 5 discs?
A: 2⁵ − 1 = 31.
Q: What does `ikkilik(6)` from example 3 print?
A: `110`.
Q: Why is computing Fibonacci numbers with plain recursion slow?
A: The same values are computed many times: both `fib(n-1)` and `fib(n-2)` compute `fib(n-3)` inside, and so on; the number of calls grows exponentially.
:::

:::link https://en.wikipedia.org/wiki/Tower_of_Hanoi
Wikipedia: the Tower of Hanoi
The history of the puzzle, its solution and the number of moves.
:::

:::link https://visualgo.net/en/recursion
VisuAlgo: the recursion tree
Visualising recursive calls.
:::
