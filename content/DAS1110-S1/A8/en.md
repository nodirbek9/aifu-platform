---
title: Repetitive processes. The post-condition loop statement. Nested loops
summary: The do-while loop and its Python replacement while True + break; validating input; nested loops, the outer and inner counters, break in an inner loop.
---

## Why it matters

An ATM asks for the PIN — if it is wrong, it asks again. The question is certain to be asked at least once, but how many times it repeats is unknown. This is the classic case of a **post-condition loop**.

A timetable, on the other hand, is built for every day of the week and for every class of every day: a loop inside a loop. Tables, matrices, going through pairs — all of these are written with **nested loops**.

:::note
**After this session you will be able to:**

- write a `do-while` loop in Java and C++ and its replacement `while True` + `break` in Python;
- repeat a request until the user enters a valid value;
- print tables and shapes with nested loops;
- count how many times a nested loop runs and explain the effect of `break` in an inner loop.
:::

## The do-while loop

A **post-condition loop** runs the body first and checks the condition afterwards:

| Language | How it is written |
|---|---|
| Java, C++ | `do { body } while (condition);` |
| Python | `while True:` → body → `if not condition: break` |

Python has no `do-while`. Instead it uses the **infinite loop with an exit** pattern: the body inside `while True:`, and at the end a `break` when the condition is false.

`while` and `do-while` differ only in the first check: `while` may never run the body, `do-while` runs it at least once. That makes `do-while` the natural choice for "do first, check afterwards" problems: input validation, menus, "play again?".

## Nested loops

When the body of one loop contains another loop, it is a **nested loop**. On every iteration of the outer loop the inner loop runs **in full**.

If the outer loop runs *n* times and the inner one *m* times, the inner body runs *n* · *m* times. A clock example: during one step of the hour hand the minute hand takes 60 steps; in 24 hours — 24 · 60 = 1440.

The bound of the inner loop can depend on the outer counter: `for j in range(1, i + 1)` — such a loop gives a "triangular" shape, and the inner body runs 1 + 2 + … + *n* = *n*(*n* + 1)/2 times.

Two important rules:

1. Give the outer and inner counters **different names** (`i`, `j`). The same name is the most common bug.
2. `break` ends only the loop **it is in** (the inner one); the outer loop goes on.

## Worked example 1: validating input

The user must enter a number from 1 to 100. If the number is invalid, print `Out of range` and ask again. When a valid number is entered, print `Accepted: <number>`.

```python
while True:
    x = int(input())
    if 1 <= x <= 100:
        break
    print("Out of range")

print("Accepted:", x)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int x;
        boolean notogri;

        do {
            x = in.nextInt();
            notogri = x < 1 || x > 100;
            if (notogri) {
                System.out.println("Out of range");
            }
        } while (notogri);

        System.out.println("Accepted: " + x);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int x;
    bool notogri;

    do {
        cin >> x;
        notogri = x < 1 || x > 100;
        if (notogri) {
            cout << "Out of range" << endl;
        }
    } while (notogri);

    cout << "Accepted: " << x << endl;
    return 0;
}
```

For the input `150`, `-3`, `42` (*notogri* means invalid):

1. Iteration 1: *x* = 150 — invalid, the message is printed and the loop continues.
2. Iteration 2: *x* = −3 — invalid, the message again.
3. Iteration 3: *x* = 42 — valid, the loop ends.
4. The output is `Out of range` twice, then `Accepted: 42`.

The number must be read at least once — hence a post-condition loop. With `while` the reading would have to be written both before the loop and inside it.

## Worked example 2: a triangle of stars

A natural number *n* is given. Print a triangle of *n* rows: row *i* has *i* stars.

```python
n = int(input())

for i in range(1, n + 1):
    for j in range(i):
        print("*", end="")
    print()
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            cout << "*";
        }
        cout << endl;
    }
    return 0;
}
```

For the input `4` the output is:

```text
*
**
***
****
```

The outer loop counts the rows, the inner loop the characters in a row. The new line is printed **after** the inner loop but inside the outer one. The inner body runs 1 + 2 + 3 + 4 = 10 times. (Python has a short form, `print("*" * i)` — but here we are practising nested loops.)

## Worked example 3: Pythagorean triples

A natural number *n* is given. Print all triples *a* < *b* < *c* ≤ *n* with *a*² + *b*² = *c*².

```python
n = int(input())

for a in range(1, n + 1):
    for b in range(a + 1, n + 1):
        for c in range(b + 1, n + 1):
            if a * a + b * b == c * c:
                print(a, b, c)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int a = 1; a <= n; a++) {
            for (int b = a + 1; b <= n; b++) {
                for (int c = b + 1; c <= n; c++) {
                    if (a * a + b * b == c * c) {
                        System.out.println(a + " " + b + " " + c);
                    }
                }
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    for (int a = 1; a <= n; a++) {
        for (int b = a + 1; b <= n; b++) {
            for (int c = b + 1; c <= n; c++) {
                if (a * a + b * b == c * c) {
                    cout << a << " " << b << " " << c << endl;
                }
            }
        }
    }
    return 0;
}
```

For the input `20` the output is:

```text
3 4 5
5 12 13
6 8 10
8 15 17
9 12 15
12 16 20
```

The inner loops start at *a* + 1 and *b* + 1 — so each triple is printed once (`4 3 5` is not repeated) and *a* < *b* < *c* holds automatically. This is **brute force**: every option is checked. A three-level loop runs about *n*³/6 times — about 1140 for *n* = 20 and about 166 million for *n* = 1000. Large *n* needs a faster algorithm (for example, computing *c* as √(*a*² + *b*²)).

:::warning
**Common mistakes**

- **The same counter in the inner and outer loop.** `for i …: for i …:` — the inner loop spoils the outer one.
- **The new line in the wrong place.** Inside the inner loop — every character on its own line; after the outer loop — everything on one line.
- **Thinking `break` also stops the outer loop.** It ends only the inner loop; leaving the outer one needs a flag.
- **Forgetting the `;` at the end of `do-while`** (Java, C++) — a compile error.
- **Forgetting the exit from `while True:` in Python** — an infinite loop.
:::

:::tip
Before writing a nested loop, draw the result for a small *n* on paper and answer: "what does the outer loop count? and the inner one?"
:::

## Summary

- `do-while` runs the body at least once; Python uses `while True:` and `break` instead.
- Input validation is the typical use of a post-condition loop.
- On every iteration of the outer loop the inner loop runs in full; *n* · *m* times in total.
- The bound of the inner loop can depend on the outer counter (triangular shapes).
- `break` ends only the loop it is in.

## Check yourself

:::check
Q: How many times does `do { … } while (false);` run?
A: Once: the body runs before the condition is checked.
Q: How do you write `do-while` in Python?
A: The body inside `while True:`, and at the end `if not condition: break`.
Q: How many lines does `for i in range(5): for j in range(3): print(i, j)` print?
A: 15: the outer loop runs 5 times and the inner one 3 times on each.
Q: What happens in example 2 if `print()` is moved inside the inner loop?
A: Every star ends up on its own line: 10 lines in total, each with one `*`.
Q: How does the result of example 3 change if the inner loops start at 1?
A: Each triple is printed twice (`3 4 5` and `4 3 5`) and the running time grows. If *c* also starts at 1, cases with *c* < *b* are checked for nothing as well.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/while.html
Oracle Java Tutorial: while and do-while
The official tutorial on the difference between the two loops.
:::

:::link https://en.cppreference.com/w/cpp/language/do
cppreference: the do-while loop
The syntax of `do-while` in C++.
:::
