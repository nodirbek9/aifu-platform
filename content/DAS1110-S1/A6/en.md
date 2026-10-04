---
title: Repetitive processes. The counted and pre-condition loop statements
summary: Practical problems with for and while: a multiplication table, Euclid's algorithm, reading until 0 is entered (a sentinel value); which loop to choose.
---

## Why it matters

A till takes the goods one by one until the customer is done — how many there will be is not known in advance. A monthly report, on the other hand, is made exactly once for each of 30 days. The first case is `while`, the second `for`. Choosing the right loop keeps a program simple and free of bugs.

In the lecture we saw the kinds of loops. In this practical session we apply them to three typical problems and practise answering, for every loop, "when does it stop?".

:::note
**After this session you will be able to:**

- solve a problem with a known number of repetitions using `for`;
- solve a problem whose number of repetitions is set by a condition using `while`;
- read data until a "sentinel value" (for example 0) is entered;
- argue that a loop terminates and check it with a table of values.
:::

## for or while?

| Question | If the answer is "yes" |
|---|---|
| Is the number of repetitions known before the loop starts? (*n* times, from 1 to 100, for each day) | `for` |
| Does the loop go on until something happens? (until 0 is entered, until the number becomes 1, until the remainder is 0) | `while` |

Any `for` can be written with `while` and vice versa, but the fitting loop is shorter and clearer:

| `for` | The same with `while` |
|---|---|
| `for i in range(1, n + 1):` | `i = 1` → `while i <= n:` → … → `i += 1` |
| `for (int i = 1; i <= n; i++)` | `int i = 1; while (i <= n) { … i++; }` |

In a `while` it is easy to forget to increase the counter — in a `for` it is right in the header.

## Checking that a loop terminates

For every `while` loop, identify two things:

1. The **stop condition** — when does the condition become false?
2. The **progress** — what changes on each iteration, and does it bring the stop condition closer?

For example, in Euclid's algorithm the second number strictly decreases on every iteration and never becomes negative — so it reaches 0 at some point and the loop stops.

## Worked example 1: a multiplication table

An integer *n* is given. Print its multiplication table from 1 to 10.

```python
n = int(input())

for i in range(1, 11):
    print(f"{n} x {i} = {n * i}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int i = 1; i <= 10; i++) {
            System.out.println(n + " x " + i + " = " + n * i);
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

    for (int i = 1; i <= 10; i++) {
        cout << n << " x " << i << " = " << n * i << endl;
    }
    return 0;
}
```

For the input `7` the loop runs 10 times and prints:

```text
7 x 1 = 7
7 x 2 = 14
…
7 x 10 = 70
```

The number of repetitions (10) is known in advance — `for` is the natural choice. In Python `range(1, 11)` does not include 11, so there are 10 values.

## Worked example 2: Euclid's algorithm (GCD)

Two natural numbers *a* and *b* are given. Find their greatest common divisor (GCD).

**Euclid's algorithm**: GCD(*a*, *b*) = GCD(*b*, *a* mod *b*), and GCD(*a*, 0) = *a*. That is, while *b* ≠ 0, replace the pair with (*b*, *a* % *b*).

```python
a, b = map(int, input().split())

while b != 0:
    qoldiq = a % b
    a = b
    b = qoldiq

print("GCD:", a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        while (b != 0) {
            int qoldiq = a % b;
            a = b;
            b = qoldiq;
        }

        System.out.println("GCD: " + a);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;

    while (b != 0) {
        int qoldiq = a % b;
        a = b;
        b = qoldiq;
    }

    cout << "GCD: " << a << endl;
    return 0;
}
```

For the input `48 18` (*qoldiq* is the remainder):

| *a* | *b* | *a* % *b* |
|---|---|---|
| 48 | 18 | 12 |
| 18 | 12 | 6 |
| 12 | 6 | 0 |
| 6 | 0 | the loop ends |

The output is `GCD: 6`. The number of iterations is not known in advance — hence `while`. The guarantee that it stops: *b* strictly decreases at each step (a remainder is smaller than the divisor). This algorithm is more than 2300 years old and is still used in cryptography.

## Worked example 3: reading until 0

One integer is given on each line; the sequence ends with 0 (the 0 itself does not count). Print the sum, the count and the average of the numbers.

A value that marks the end like this is called a **sentinel value**.

```python
yigindi = 0
soni = 0

x = int(input())
while x != 0:
    yigindi += x
    soni += 1
    x = int(input())

if soni > 0:
    print(f"Sum: {yigindi}, count: {soni}, average: {yigindi / soni:.2f}")
else:
    print("No numbers entered")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int yigindi = 0;
        int soni = 0;

        int x = in.nextInt();
        while (x != 0) {
            yigindi += x;
            soni++;
            x = in.nextInt();
        }

        if (soni > 0) {
            System.out.println(String.format("Sum: %d, count: %d, average: %.2f",
                    yigindi, soni, (double) yigindi / soni));
        } else {
            System.out.println("No numbers entered");
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int yigindi = 0;
    int soni = 0;

    int x;
    cin >> x;
    while (x != 0) {
        yigindi += x;
        soni++;
        cin >> x;
    }

    if (soni > 0) {
        cout << "Sum: " << yigindi << ", count: " << soni << ", average: "
             << fixed << setprecision(2) << (double) yigindi / soni << endl;
    } else {
        cout << "No numbers entered" << endl;
    }
    return 0;
}
```

For the input `5`, `3`, `-2`, `0`:

1. The first number is read **before** the loop: *x* = 5.
2. 5 ≠ 0 — it is added and the next number is read: 3. Then −2.
3. *x* = 0 — the loop ends; the 0 is not added to the sum.
4. The output is `Sum: 6, count: 3, average: 2.00`.

This is the "read ahead" pattern: the first value is read before the loop, the next ones at the **end** of the body. If the first number is 0, the loop never runs, and the `soni > 0` check prevents a division by zero.

:::warning
**Common mistakes**

- **Forgetting to increase the counter in a `while`** — an infinite loop.
- **Counting the sentinel.** If the reading is at the start of the body, the 0 is added too and `soni` is one too many.
- **Division by zero on an empty sequence.** If the first number is 0, `soni` = 0 — no average can be computed.
- **The order in Euclid's algorithm.** `a = b; b = a % b;` — *a* has already changed and the result is wrong. Save the remainder in a temporary variable first.
:::

:::tip
Once a `while` loop is written, check it in three cases: the loop never runs (0 straight away), it runs once, it runs many times. Most bugs show up in the first two.
:::

## Summary

- If the number of repetitions is known in advance — `for`; if it is set by a condition — `while`.
- For every `while`, identify the stop condition and the progress on each iteration.
- Euclid's algorithm: while *b* ≠ 0, (*a*, *b*) ← (*b*, *a* % *b*); the answer is *a*.
- When reading up to a sentinel, the first value is read before the loop and the next ones at the end of the body.
- A loop is checked with 0, 1 and many iterations.

## Check yourself

:::check
Q: Which loop is handier for the sum of the numbers from 1 to 100, and why?
A: `for`: the number of repetitions (100) is known in advance.
Q: Compute GCD(84, 36) with Euclid's algorithm.
A: (84, 36) → (36, 12) → (12, 0); GCD = 12.
Q: Why is the first number read before the loop in the sentinel program?
A: When the condition (`x != 0`) is checked the first time, *x* must already have a value; the next numbers are read at the end of the body, so the 0 is not counted.
Q: What does the program of example 3 print if the first number entered is 0?
A: `No numbers entered`: the loop never runs and `soni` = 0.
Q: How many iterations does `for (int i = 10; i > 0; i -= 3)` make?
A: 4: *i* = 10, 7, 4, 1.
:::

:::link https://visualgo.net/en
VisuAlgo: visualising algorithms
Step-by-step animations of algorithms.
:::

:::link https://docs.python.org/3/reference/compound_stmts.html#the-while-statement
Python: the while statement
From the official Python language reference.
:::
