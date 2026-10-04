---
title: Organising repetitive processes
summary: The idea of a loop, the counted loop (for), the pre-condition (while) and post-condition (do-while) loops, counters and accumulators, break and continue, the infinite loop.
---

## Why it matters

The dean's office has to compute the final scores of 600 students. Writing a separate line for each student means 600 lines of identical code. And if there are 700 students tomorrow, do we rewrite the program? No: it is enough to tell the computer "repeat this job for every student".

The computer's main strength is exactly repetition: it does the same thing a million times without mistakes and without getting tired. The construct that organises repetition is the **loop**. With loops and branching any algorithm can be written.

:::note
**After this lesson you will be able to:**

- explain the body, iteration, counter and continuation condition of a loop;
- write `for`, `while` and `do-while` loops in three languages and choose the one that fits the problem;
- write programs with an accumulator (sum, product) and a counter;
- use `break` and `continue` and find an infinite-loop bug.
:::

## The idea of a loop

A **loop** is a construct that repeats a group of actions several times. Its parts:

- The **body** — the actions that are repeated.
- An **iteration** — one execution of the body.
- The **continuation condition** — checked before (or after) each iteration; if it is true, the loop continues.
- The **loop variable (counter)** — a variable that changes on every iteration and affects the condition.

In a flowchart a loop is drawn as a line going back from the condition diamond to the start of the body.

There are three kinds of loops:

| Kind | When it is used | When the condition is checked |
|---|---|---|
| **Counted** (`for`) | the number of repetitions is known in advance | before each iteration |
| **Pre-condition** (`while`) | the number of repetitions is unknown and set by a condition | before each iteration; the body may never run |
| **Post-condition** (`do-while`) | the body must run at least once | after each iteration |

## The counted loop: for

The **`for`** loop works with a counter: a start value, a boundary condition and a step.

| Language | From 1 to *n* |
|---|---|
| Python | `for i in range(1, n + 1):` |
| Java, C++ | `for (int i = 1; i <= n; i++) { ... }` |

In Python, in `range(start, stop, step)` the `stop` is **not included**: `range(1, 5)` → 1, 2, 3, 4. `range(5)` → 0, 1, 2, 3, 4. In reverse: `range(10, 0, -1)` → 10, 9, …, 1.

In Java and C++ the brackets of `for` have three parts: `int i = 1` (once, at the start), `i <= n` (before each iteration), `i++` (after each iteration).

## The pre-condition loop: while

**`while`** repeats the body as long as the condition is true. It is used when the number of repetitions is not known in advance: "until the number becomes 0", "until the user enters the right password", "until the debt is paid".

| Language | How it is written |
|---|---|
| Python | `while condition:` |
| Java, C++ | `while (condition) { ... }` |

Inside the body the variable that affects the condition must change, otherwise the loop never ends — an **infinite loop**.

## The post-condition loop: do-while

**`do-while`** runs the body first and checks the condition afterwards. So the body runs at least once. Java and C++ have it: `do { ... } while (condition);` (with a semicolon at the end!). Python has no `do-while`; `while True:` with a `break` inside is used instead. This loop is covered in detail in session A8.

## Counters and accumulators

Most loop problems rest on two patterns:

- An **accumulator** gets a start value before the loop (0 for a sum, 1 for a product), and on each iteration a new value is added to it or multiplied into it: `s += x`, `p *= x`.
- A **counter** counts how many times a condition held: `k = 0` before the loop, `k += 1` when the condition is true.

The start value is set **before the loop**. Set it inside and it is reset on every iteration.

## break and continue

- **`break`** ends the loop at once; control passes to the statement after the loop. For example, stopping a search when the item is found.
- **`continue`** skips the rest of the current iteration and moves on to the next one. For example, skipping negative numbers.

Both are written the same way in all three languages.

## Worked example 1: the factorial

An integer *n* (0 ≤ *n* ≤ 20) is given. Compute *n*! = 1 · 2 · … · *n* (0! = 1).

```python
n = int(input())

f = 1                          # the start value of a product is 1
for i in range(1, n + 1):
    f *= i

print(f"{n}! = {f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        long f = 1;                // 20! does not fit in an int, hence long
        for (int i = 1; i <= n; i++) {
            f *= i;
        }

        System.out.println(n + "! = " + f);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long f = 1;           // 20! does not fit in an int, hence long long
    for (int i = 1; i <= n; i++) {
        f *= i;
    }

    cout << n << "! = " << f << endl;
    return 0;
}
```

For the input `5` the loop runs step by step:

| *i* | `f *= i` | *f* |
|---|---|---|
| at the start | — | 1 |
| 1 | 1 · 1 | 1 |
| 2 | 1 · 2 | 2 |
| 3 | 2 · 3 | 6 |
| 4 | 6 · 4 | 24 |
| 5 | 24 · 5 | 120 |

The output is `5! = 120`. For the input `0` the loop never runs and `0! = 1` is printed — because the start value was chosen correctly. `20! = 2432902008176640000` does not fit in an `int`; it needs `long` / `long long`.

## Worked example 2: the sum of a number's digits

A positive integer is given. Find the sum of its digits and the number of digits.

```python
n = int(input())

yigindi = 0
soni = 0
while n > 0:
    yigindi += n % 10          # the last digit
    n //= 10                   # drop the last digit
    soni += 1

print("Sum:", yigindi)
print("Digits:", soni)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int yigindi = 0;
        int soni = 0;
        while (n > 0) {
            yigindi += n % 10;     // the last digit
            n /= 10;               // drop the last digit
            soni++;
        }

        System.out.println("Sum: " + yigindi);
        System.out.println("Digits: " + soni);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    int yigindi = 0;
    int soni = 0;
    while (n > 0) {
        yigindi += n % 10;     // the last digit
        n /= 10;               // drop the last digit
        soni++;
    }

    cout << "Sum: " << yigindi << endl;
    cout << "Digits: " << soni << endl;
    return 0;
}
```

For the input `2026` (*yigindi* is the sum, *soni* the count):

| *n* (at the start of the iteration) | `n % 10` | *yigindi* | *soni* |
|---|---|---|---|
| 2026 | 6 | 6 | 1 |
| 202 | 2 | 8 | 2 |
| 20 | 0 | 8 | 3 |
| 2 | 2 | 10 | 4 |
| 0 | — | the loop ends | |

The output is `Sum: 10` and `Digits: 4`. The number of repetitions is not known in advance (it depends on the number of digits), so `while` was chosen. *n* gets smaller on every iteration, so the loop is sure to end.

:::warning
**Common mistakes**

- **An infinite loop.** If *n* does not change inside `while (n > 0)`, the program never ends. On the platform this gives the "time limit exceeded" verdict.
- **Off-by-one errors.** `range(1, n)` does not include *n*; do not mix up `i < n` and `i <= n`.
- **Resetting the accumulator inside the loop.** The result is only the last value.
- **Starting a product at 0.** With `f = 0` the result is always 0.
- **Forgetting the semicolon at the end of `do-while`** — a compile error.
:::

:::tip
Before writing a loop, answer three questions: what is repeated? when does it stop? what changes on each iteration? Then fill in a table of values by hand for a small input and check the program against it.
:::

## Summary

- A loop repeats its body while the condition is true; one execution of the body is an iteration.
- `for` — when the number of repetitions is known; `while` — by a condition, the body may never run; `do-while` — the body runs at least once.
- In Python `range(a, b)` does not include *b*; in Java and C++ `for (start; condition; step)`.
- An accumulator starts before the loop at 0 (sum) or 1 (product).
- `break` ends the loop, `continue` moves on to the next iteration.
- The variable that affects the condition must change in the body, otherwise the loop is infinite.

## Check yourself

:::check
Q: Which values does `range(2, 10, 3)` give in Python?
A: 2, 5, 8 (10 is not included).
Q: What is the main difference between `while` and `do-while`?
A: `while` checks the condition before the body, so the body may never run; `do-while` runs the body first, so it runs at least once.
Q: What is *s* after `s = 0; for i in range(1, 6): s += i`?
A: 15 (1 + 2 + 3 + 4 + 5).
Q: What is wrong with this code: `i = 1` `while i <= 10:` `print(i)`?
A: *i* never changes inside the loop — an infinite loop. The body needs `i += 1`.
Q: How do `break` and `continue` differ?
A: `break` ends the loop completely; `continue` only skips the rest of the current iteration and starts the next one.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#for-statements
Python: for and range
The `for`, `range`, `break` and `continue` sections of the official tutorial.
:::

:::link https://en.cppreference.com/w/cpp/language/for
cppreference: the for loop
The syntax of the C++ `for` loop, with examples.
:::
