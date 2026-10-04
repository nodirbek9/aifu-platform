---
title: Organising branching processes
summary: Linear and branching algorithms, if, if-else, the else-if chain, nested conditions, blocks, drawing a condition in a flowchart.
---

## Why it matters

Every program we have written so far did the same steps every time: read, compute, print. Real life needs decisions. If a student's score is below 56 — a resit; otherwise — on to the next year. If the card has enough money — the payment goes through; if not — it is declined. A navigator chooses another road when there is a traffic jam.

For a program to take different paths depending on the situation, it needs **branching**. This is one of the most fundamental ideas in programming: after it, your programs start to "think".

:::note
**After this lesson you will be able to:**

- tell a linear algorithm from a branching one and draw branching in a flowchart;
- write `if`, `if-else` and an `else-if` chain in three languages;
- solve problems split into several ranges (grades, tariffs) with conditions in the right order;
- read nested conditions and simplify them with logical operations.
:::

## Linear and branching algorithms

A **linear algorithm** is one whose steps are always carried out in the same order, with none skipped. Every program in the previous lessons was linear.

A **branching algorithm** is one in which, depending on a **condition**, one of two or more paths is chosen. A condition is a boolean expression whose value is true or false: `ball >= 56`, `balans >= narx`, `x % 2 == 0`.

In a flowchart a condition is drawn as a **diamond**. Two lines leave the diamond: "yes" (true) and "no" (false). The two branches then meet again at one point, and the algorithm continues.

## The if statement

The simplest branching is **`if`**: if the condition is true, the block is executed; if it is false, it is skipped.

| Language | How it is written |
|---|---|
| Python | `if condition:` and the following lines **indented** (4 spaces) |
| Java, C++ | `if (condition) { ... }` — the condition in brackets, the block in braces |

In Python a block is marked by indentation: lines with the same indentation form one block. In Java and C++ a block is the code between `{` and `}`; indentation is only for reading, but it must still be right.

## if-else: two paths

**`if-else`**: if the condition is true, the first block is executed; if false, the second. One of them is always executed, never both.

For example, "is the number even or odd": `if x % 2 == 0` — even, `else` — odd.

## The else-if chain: many paths

When there are more than two paths, conditions are chained: `elif` in Python, `else if` in Java and C++. The conditions are checked **from top to bottom**; the block of the first true condition is executed and the rest are not checked. If none is true, the final `else` runs (if there is one).

That is why the **order** of the conditions matters. Converting AIFU's 100-point scale to a 5-point grade:

| Score | Grade |
|---|---|
| 86–100 | 5 (excellent) |
| 71–85 | 4 (good) |
| 56–70 | 3 (satisfactory) |
| 0–55 | 2 (fail) |

If the conditions are written from the highest down, each only needs to check the lower bound: by the time the second condition is reached, the score is already known to be below 86.

## Nested conditions

A condition can contain another condition — a **nested condition**. For example, first "has the student paid?", then "is there a debt?".

Nested conditions can often be simplified with logical operations:

| Nested | Simplified |
|---|---|
| `if b > 0:` inside `if a > 0:` | `if a > 0 and b > 0:` |
| `if x <= 20:` inside `if x >= 10:` | `if 10 <= x <= 20:` (Python) or `x >= 10 && x <= 20` |

A rule of thumb: conditions nested deeper than three levels are hard to read — simplify them or turn them into a chain.

## Worked example 1: converting a score to a grade

An integer — a student's score (0–100) — is given. Print the grade.

```python
ball = int(input())

if ball >= 86:
    baho = 5
elif ball >= 71:
    baho = 4
elif ball >= 56:
    baho = 3
else:
    baho = 2

print("Grade:", baho)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int ball = in.nextInt();

        int baho;
        if (ball >= 86) {
            baho = 5;
        } else if (ball >= 71) {
            baho = 4;
        } else if (ball >= 56) {
            baho = 3;
        } else {
            baho = 2;
        }

        System.out.println("Grade: " + baho);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int ball;
    cin >> ball;

    int baho;
    if (ball >= 86) {
        baho = 5;
    } else if (ball >= 71) {
        baho = 4;
    } else if (ball >= 56) {
        baho = 3;
    } else {
        baho = 2;
    }

    cout << "Grade: " << baho << endl;
    return 0;
}
```

For the input `78` (*ball* is the score, *baho* the grade):

1. `78 >= 86` is false; move on to the next condition.
2. `78 >= 71` is true: `baho = 4`, and the rest of the chain is not checked.
3. The output is `Grade: 4`.

For the input `56` the first two conditions are false and the third is true — `Grade: 3`. Always try the boundary values (55, 56, 70, 71, 85, 86): that is where the bugs usually are.

## Worked example 2: a quadratic equation

Integers *a*, *b*, *c* are given (*a* ≠ 0). Find the roots of *ax*² + *bx* + *c* = 0. The discriminant is *D* = *b*² − 4*ac*: *D* < 0 — no roots, *D* = 0 — one root *x* = −*b* / 2*a*, *D* > 0 — two roots *x* = (−*b* ± √*D*) / 2*a*.

```python
import math

a, b, c = map(int, input().split())
d = b * b - 4 * a * c

if d < 0:
    print("No roots")
elif d == 0:
    x = -b / (2 * a)
    print(f"x = {x:.2f}")
else:
    x1 = (-b + math.sqrt(d)) / (2 * a)
    x2 = (-b - math.sqrt(d)) / (2 * a)
    print(f"x1 = {x1:.2f}, x2 = {x2:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();
        int d = b * b - 4 * a * c;

        if (d < 0) {
            System.out.println("No roots");
        } else if (d == 0) {
            double x = -b / (2.0 * a);
            System.out.println(String.format("x = %.2f", x));
        } else {
            double x1 = (-b + Math.sqrt(d)) / (2.0 * a);
            double x2 = (-b - Math.sqrt(d)) / (2.0 * a);
            System.out.println(String.format("x1 = %.2f, x2 = %.2f", x1, x2));
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;
    int d = b * b - 4 * a * c;

    cout << fixed << setprecision(2);
    if (d < 0) {
        cout << "No roots" << endl;
    } else if (d == 0) {
        double x = -b / (2.0 * a);
        cout << "x = " << x << endl;
    } else {
        double x1 = (-b + sqrt(d)) / (2.0 * a);
        double x2 = (-b - sqrt(d)) / (2.0 * a);
        cout << "x1 = " << x1 << ", x2 = " << x2 << endl;
    }
    return 0;
}
```

For the input `1 -3 2`:

1. *D* = 9 − 8 = 1 > 0 — the third branch.
2. *x*₁ = (3 + 1) / 2 = 2, *x*₂ = (3 − 1) / 2 = 1.
3. The output is `x1 = 2.00, x2 = 1.00`.

For the input `1 2 1`: *D* = 0, `x = -1.00`. For the input `1 0 1`: *D* = −4, `No roots`. Each of the three branches has been checked by at least one test — a good habit.

Java and C++ use `2.0 * a`: with `2 * a`, the expression `-b / (2 * a)` would be integer division.

:::warning
**Common mistakes**

- **A semicolon after the condition (Java, C++).** `if (x > 0);` — the condition controls an empty statement and the next block always runs.
- **Several lines without braces.** `if (x > 0) a = 1; b = 2;` — `b = 2` does not depend on the condition. Always use `{ }`.
- **Mixed indentation in Python.** Mixing tabs and spaces gives `IndentationError`.
- **Conditions in the wrong order.** If `if ball >= 56` comes first, even 95 points get a "3".
- **A condition on `else`.** `else (x < 0)` is an error; if you need a condition, use `else if` / `elif`.
:::

:::tip
Think of at least one test for every branch, and check the boundary values separately. The number of branches is the minimum number of tests.
:::

## Summary

- In a branching algorithm the path is chosen by a condition; in a flowchart the condition is a diamond.
- `if` — one path, `if-else` — one of two, an `else-if` / `elif` chain — one of many.
- In a chain the conditions are checked from top to bottom and the first true one runs; order matters.
- In Python a block is marked by indentation, in Java and C++ by `{ }`.
- Nested conditions can often be simplified with `and` / `&&`.
- Every branch and every boundary value should be tested.

## Check yourself

:::check
Q: What is the difference between a linear and a branching algorithm?
A: In a linear algorithm all the steps are always carried out in the same order; in a branching algorithm different steps are chosen depending on a condition.
Q: If the first condition in the grading program is `ball >= 56`, what is the result for 95 points, and why?
A: 3: in a chain the first true condition runs; 95 >= 56 is true, so the rest are not checked.
Q: What does `if (x > 5); { System.out.println("katta"); }` do in Java?
A: It prints "katta" whatever *x* is: the semicolon is an empty statement and the block does not depend on the condition.
Q: How do you write `if a > 0:` with a nested `if b > 0:` as one condition?
A: `if a > 0 and b > 0:` (Java/C++: `if (a > 0 && b > 0)`).
Q: At least how many tests are needed to fully check the quadratic-equation program?
A: Three: one each for *D* < 0, *D* = 0 and *D* > 0.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#if-statements
Python: if statements
The `if`, `elif`, `else` section of the official tutorial.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/if.html
Oracle Java Tutorial: if-then and if-then-else
The official tutorial on Java's conditional statements.
:::
