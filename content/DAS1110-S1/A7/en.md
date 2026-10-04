---
title: Repetitive processes. The counted loop statement (continued)
summary: Harder problems with for: testing for a prime and break, Fibonacci numbers, the sum of a series; the step, reverse order, printing the result on one line.
---

## Why it matters

A bank card number is checked by a loop over its digits (the Luhn algorithm). Encryption on the internet rests on large prime numbers. A loan repayment schedule is a calculation repeated for every month. All of these use different "tricks" of the `for` loop: a step, stopping early, remembering earlier values.

In this practical session we practise these features of the `for` loop on three classic problems.

:::note
**After this session you will be able to:**

- use a step and reverse order in a `for` loop;
- stop a loop early with `break` and work with a "found / not found" flag;
- compute a sequence that depends on the two previous values (Fibonacci);
- compute the sum of a series of floating-point numbers and print the result on one line.
:::

## What a for loop can do

| Task | Python | Java, C++ |
|---|---|---|
| Even numbers from 0 to 20 | `range(0, 21, 2)` | `for (int i = 0; i <= 20; i += 2)` |
| From 10 down to 1 | `range(10, 0, -1)` | `for (int i = 10; i >= 1; i--)` |
| While *i*² ≤ *n* | `while i * i <= n:` | `for (int i = 2; i * i <= n; i++)` |

In Java and C++ the condition of a `for` can be any boolean expression (for example `i * i <= n`). In Python `range` gives only an arithmetic progression; a complex condition needs `while`.

## A flag and break

Questions of the form "is there such an item?" use the **flag** pattern:

1. Before the loop: `topildi = False` (found = false).
2. When the item is found in the loop: `topildi = True` and `break` — there is no need to check the rest.
3. After the loop the answer is printed according to the flag.

## Printing the result on one line

Normally `print` / `println` / `endl` moves to a new line each time. To print values on one line separated by spaces:

| Language | Printing without a new line |
|---|---|
| Python | `print(x, end=" ")` |
| Java | `System.out.print(x + " ")` |
| C++ | `cout << x << " "` (without `endl`) |

One new line is printed at the end. If the extra space is a problem, print the separator before every item except the first rather than after each.

## Worked example 1: is it prime?

A natural number *n* (*n* ≥ 1) is given. Print `prime` if it is prime, otherwise `not prime`.

A **prime number** is a number greater than 1 that is divisible only by 1 and itself. It is enough to test divisors up to √*n*: if *n* = *a* · *b* and *a* ≤ *b*, then *a* ≤ √*n*.

```python
n = int(input())

tub = n >= 2
i = 2
while i * i <= n:
    if n % i == 0:
        tub = False
        break
    i += 1

print("prime" if tub else "not prime")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        boolean tub = n >= 2;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                tub = false;
                break;
            }
        }

        System.out.println(tub ? "prime" : "not prime");
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    bool tub = n >= 2;
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) {
            tub = false;
            break;
        }
    }

    cout << (tub ? "prime" : "not prime") << endl;
    return 0;
}
```

For the input `91` (*tub* means prime): *i* = 2, 3, 4, 5, 6 — no division; *i* = 7: 91 = 7 · 13 — `tub = False`, `break`. The output is `not prime`. For `97`: *i* goes from 2 to 9 (10² = 100 > 97) and none divides — `prime`. For `1`: the flag is false from the start — `not prime`.

Testing up to √*n* makes a huge difference: for *n* ≈ 10⁹ a loop up to *n* is a billion steps, up to √*n* about 31 600.

## Worked example 2: Fibonacci numbers

A natural number *n* is given. Print on one line the first *n* terms of the Fibonacci sequence: 1, 1, 2, 3, 5, 8, … (each term is the sum of the two before it).

```python
n = int(input())

a, b = 1, 1
for i in range(n):
    print(a, end=" ")
    a, b = b, a + b
print()
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        long a = 1, b = 1;
        for (int i = 0; i < n; i++) {
            System.out.print(a + " ");
            long keyingi = a + b;
            a = b;
            b = keyingi;
        }
        System.out.println();
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long a = 1, b = 1;
    for (int i = 0; i < n; i++) {
        cout << a << " ";
        long long keyingi = a + b;
        a = b;
        b = keyingi;
    }
    cout << endl;
    return 0;
}
```

For the input `8`:

| *i* | printed (*a*) | new *a* | new *b* |
|---|---|---|---|
| 0 | 1 | 1 | 2 |
| 1 | 1 | 2 | 3 |
| 2 | 2 | 3 | 5 |
| 3 | 3 | 5 | 8 |
| … | … | … | … |
| 7 | 21 | 34 | 55 |

The output is `1 1 2 3 5 8 13 21 `. There is no need to store the whole sequence: the last two terms (*a*, *b*) are enough. In Python `a, b = b, a + b` updates both values at once; Java and C++ need the temporary variable `keyingi` (next). Fibonacci numbers grow fast: the 93rd term does not fit even in a `long`.

## Worked example 3: the sum of a series

A natural number *n* is given. Print the sum *S* = 1 + 1/2² + 1/3² + … + 1/*n*² to 6 decimal places. (As *n* grows the sum approaches π²/6 ≈ 1.644934.)

```python
n = int(input())

s = 0.0
for k in range(1, n + 1):
    s += 1 / (k * k)

print(f"{s:.6f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        double s = 0.0;
        for (int k = 1; k <= n; k++) {
            s += 1.0 / ((double) k * k);
        }

        System.out.println(String.format("%.6f", s));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int n;
    cin >> n;

    double s = 0.0;
    for (int k = 1; k <= n; k++) {
        s += 1.0 / ((double) k * k);
    }

    cout << fixed << setprecision(6) << s << endl;
    return 0;
}
```

For the input `3`: 1 + 0.25 + 0.111111 = `1.361111`. For `1000`: `1.643935` — very close to π²/6.

Two important details: (1) `1.0 / ...` avoids integer division; `1 / (k * k)` gives 0 in Java and C++ for *k* > 1. (2) `(double) k * k` keeps `k * k` from overflowing an `int` when *k* is large.

:::warning
**Common mistakes**

- **Treating 1 as prime.** A prime must be greater than 1; start the flag with `n >= 2`.
- **Resetting the flag inside the loop.** `else: tub = True` — the next iteration "erases" what was found earlier.
- **The update order in Fibonacci.** `a = b; b = a + b;` — *a* has already changed and the sequence breaks.
- **Integer division in a series.** `1 / (k * k)` is 0 in Java and C++; write `1.0 /`.
- **Taking 0 from `range(n + 1)`.** `1 / 0` is a division-by-zero error; the series starts at 1.
:::

:::tip
Test the code with a large *n* too: a number near 10⁹ for the prime test, 90 terms for Fibonacci. Speed and overflow problems do not show up on small tests.
:::

## Summary

- A `for` has a step (`range(a, b, step)`, `i += step`) and reverse order (`range(b, a, -1)`, `i--`).
- "Is there…?" is answered with a flag and `break`.
- Primality only needs testing up to √*n*; 1 is not prime.
- For Fibonacci only the last two terms are kept; in Python `a, b = b, a + b`.
- In a floating-point series, avoid integer division and overflow with `1.0 /` and `(double)`.

## Check yourself

:::check
Q: Which values does `range(20, 0, -5)` give in Python?
A: 20, 15, 10, 5.
Q: Why is it enough to test primality up to √*n*?
A: If *n* = *a* · *b* and *a* ≤ *b*, then *a* · *a* ≤ *n*, so *a* ≤ √*n*. So if there are divisors, one of them is certainly no greater than √*n*.
Q: What does example 2 print when `n = 1`?
A: Only `1` (and a space): the loop runs once.
Q: In Java, what is `1 / (k * k)` when *k* = 2, and why?
A: 0: both operands are integers and integer division drops the fraction.
Q: Does the result of the prime program change if `break` is removed?
A: The result stays the same, but the program is slower: the remaining numbers are still checked after a divisor is found.
:::

:::link https://en.wikipedia.org/wiki/Fibonacci_sequence
Wikipedia: the Fibonacci sequence
The history and properties of the sequence.
:::

:::link https://docs.python.org/3/library/stdtypes.html#range
Python: the range type
The official documentation of `range(start, stop, step)`.
:::
