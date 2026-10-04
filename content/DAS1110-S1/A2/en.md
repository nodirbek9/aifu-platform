---
title: Data types. Input and output functions. Operators
summary: The size and range of types, overflow, type conversion, reading several values on one line, formatted output, compound assignment operators.
---

## Why it matters

If a banking app drops the fractional part when it calculates interest, the customer loses money. A program that averages students' marks in integers prints 4 instead of 4.67, and the student gets "satisfactory" instead of "good". These are not syntax errors — the program runs, it just gives a wrong result. The cause is often a badly chosen data type.

In this practical session we practise the ranges of types, type conversion, input and output.

:::note
**After this session you will be able to:**

- choose a data type that fits the problem (`int`, `long long`, `double`);
- avoid integer overflow and the integer-division mistake;
- read several values given on one line;
- print a result with the precision and look required (`01:02:05`, `4.67`);
- use compound operators such as `+=`, `-=`, `++`.
:::

## The size and range of types

Computer memory is limited, so each type sets aside a fixed amount of space for a value. A number that does not fit cannot be stored.

| Type (C++ / Java) | Size | Range of values (approximately) |
|---|---|---|
| `int` | 4 bytes | −2.1 billion … 2.1 billion (−2³¹ … 2³¹−1) |
| `long long` / `long` | 8 bytes | ±9.2 · 10¹⁸ |
| `double` | 8 bytes | ±1.7 · 10³⁰⁸, ~15–16 exact digits |
| `char` | C++: 1 byte, Java: 2 bytes | one character |
| `bool` / `boolean` | usually 1 byte | `true` or `false` |

In Python `int` has no limit: the bigger the number, the more space it takes. Python's `float` is the same as `double` in C++ and Java.

**Overflow** is a result going beyond the range of its type. In C++ and Java, in an `int` variable 2 147 483 647 + 1 silently becomes −2 147 483 648. That is why large numbers (populations, milliseconds, factorials) get `long long` (C++) or `long` (Java).

## Type conversion

When an expression mixes values of different types, the language brings them to one type.

- **Implicit conversion** — automatically from a smaller type to a larger one: adding an `int` and a `double` gives a `double`. For example, `7 / 2.0` = 3.5.
- **Explicit conversion** — on the programmer's order: `(double) a` in C++ and Java, also `static_cast<double>(a)` in C++; `float(a)`, `int(x)` in Python.

| Expression | Python | Java / C++ |
|---|---|---|
| `14 / 3` | `4.666…` | `4` |
| `14 / 3.0` | `4.666…` | `4.666…` |
| `(double) 14 / 3` | — | `4.666…` |
| `int(4.99)` / `(int) 4.99` | `4` | `4` (the fraction is dropped, not rounded) |

## Input: several values on one line

Values are often given on one line separated by spaces: `4 5 5`.

- **C++**: `cin >> a >> b >> c;` skips spaces and new lines by itself.
- **Java**: the `nextInt()` method of `Scanner` works the same way.
- **Python**: `input()` returns the whole line as text; it must be split into pieces with `split()` and each piece turned into a number: `a, b, c = map(int, input().split())`.

## Formatted output

To print a result in the required form, a format is given:

| Task | Python | Java | C++ |
|---|---|---|---|
| 2 decimal places | `f"{x:.2f}"` | `String.format("%.2f", x)` | `fixed << setprecision(2) << x` |
| Width 2, leading zero | `f"{n:02d}"` | `String.format("%02d", n)` | `setw(2) << setfill('0') << n` |

In C++ `setprecision` and `setw` need `#include <iomanip>`.

## Compound operators

| Written | Meaning |
|---|---|
| `x += 5` | `x = x + 5` |
| `x -= 2` | `x = x - 2` |
| `x *= 3` | `x = x * 3` |
| `x /= 2` | `x = x / 2` |
| `x %= 60` | `x = x % 60` |
| `x++` / `++x` (Java, C++) | `x = x + 1` |
| `x--` / `--x` (Java, C++) | `x = x - 1` |

Python has no `++` or `--`: you write `x += 1`.

## Worked example 1: seconds to clock format

An integer — a number of seconds — is given (0 ≤ *n* < 86 400). Print it as `HH:MM:ss`.

```python
n = int(input())

soat = n // 3600          # whole hours
n %= 3600                 # the seconds left
daqiqa = n // 60
soniya = n % 60

print(f"{soat:02d}:{daqiqa:02d}:{soniya:02d}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int soat = n / 3600;      // whole hours
        n %= 3600;                // the seconds left
        int daqiqa = n / 60;
        int soniya = n % 60;

        System.out.println(String.format("%02d:%02d:%02d", soat, daqiqa, soniya));
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

    int soat = n / 3600;      // whole hours
    n %= 3600;                // the seconds left
    int daqiqa = n / 60;
    int soniya = n % 60;

    cout << setfill('0') << setw(2) << soat << ":"
         << setw(2) << daqiqa << ":" << setw(2) << soniya << endl;
    return 0;
}
```

For the input `3725`:

1. `soat = 3725 // 3600` = 1 (integer division) — *soat* means hours.
2. `n %= 3600` — the remainder: *n* = 125.
3. `daqiqa = 125 // 60` = 2 (minutes), `soniya = 125 % 60` = 5 (seconds).
4. Each number is printed with two digits and a leading zero: `01:02:05`.

Here integer division and the remainder are not a "mistake" but exactly the tool we need.

## Worked example 2: the average of three marks

A student's three marks (integers) are given on one line. Print the average mark to two decimal places.

```python
a, b, c = map(int, input().split())

ortacha = (a + b + c) / 3     # in Python / always gives a floating-point number
print(f"{ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        // (a + b + c) / 3 would be integer division, so we convert to double
        double ortacha = (double) (a + b + c) / 3;
        System.out.println(String.format("%.2f", ortacha));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    // (a + b + c) / 3 would be integer division, so we convert to double
    double ortacha = static_cast<double>(a + b + c) / 3;
    cout << fixed << setprecision(2) << ortacha << endl;
    return 0;
}
```

For the input `4 5 5`:

1. Three numbers are read; in Python `input().split()` splits the line into the list `["4", "5", "5"]`, and `map(int, ...)` turns each item into a number.
2. The sum is 14. In Java and C++ `14 / 3` would give 4 — so the sum is converted to `double` first: 14.0 / 3 = 4.666…
3. It is printed rounded to two places: `4.67`.

:::warning
**Common mistakes**

- **`(double) (a + b + c / 3)`** — the bracket is in the wrong place: `c / 3` is divided as integers first. The conversion must happen *before* the division.
- **A large number in an `int`.** 50 000 · 50 000 = 2.5 billion does not fit in an `int`, and the result comes out negative. Use `long long` or `long`.
- **Not turning `input().split()` into numbers in Python.** `"4" + "5"` = `"45"`.
- **Forgetting that `setfill('0')` stays.** In C++ `setfill` affects all later output, while `setw` affects only the next value.
:::

:::tip
Before printing a result, ask: "what type is this value?" If an expression has only integers but the answer must have a fraction, a conversion is needed.
:::

## Summary

- Every type has a size and a range; `int` ≈ ±2.1 billion, larger numbers need `long long` / `long`. Python's `int` has no limit.
- Dividing integers drops the fraction; if you need it, convert to `double`.
- In Python values on one line are read with `map(int, input().split())`; in C++ and Java `cin >>` and `nextInt()` skip the spaces by themselves.
- Formatting: `.2f` — two decimal places, `02d` — two digits with a leading zero.
- `x += 5` is short for `x = x + 5`; Java and C++ also have `x++`.

## Check yourself

:::check
Q: In Java, what is *x* after `int x = 2147483647; x++;`?
A: −2147483648: the `int` overflows and wraps to the smallest value.
Q: How do `(double) 7 / 2` and `(double) (7 / 2)` differ in C++?
A: The first is 3.5 (7 is converted to `double` first); the second is 3.0 (integer division gives 3 first, then that is converted to `double`).
Q: How do you read three numbers from the line `1 2 3` in Python?
A: `a, b, c = map(int, input().split())`.
Q: If `n = 200`, what are `n // 60` and `n % 60`?
A: 3 and 20: 200 seconds is 3 minutes 20 seconds.
Q: How do you print the number 5 as `05` (Python and Java)?
A: Python: `f"{5:02d}"`, Java: `String.format("%02d", 5)`.
:::

:::link https://docs.python.org/3/library/string.html#format-specification-mini-language
Python: the format specification
The official documentation of `.2f`, `02d` and other formats.
:::

:::link https://en.cppreference.com/w/cpp/io/manip
cppreference: input/output manipulators
`setw`, `setfill`, `setprecision`, `fixed` and others.
:::
