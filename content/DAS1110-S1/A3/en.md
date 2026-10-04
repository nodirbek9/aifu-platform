---
title: Working with logical operations and mathematical functions
summary: Logical operations (AND, OR, NOT), the truth table, short-circuit evaluation, operator precedence; square root, power, absolute value, rounding and trigonometric functions.
---

## Why it matters

A university grants a scholarship only to a student who has passed every subject **and** has an average above 4. A library lends a book to a student **or** a teacher. Rules like these are written in a program with logical operations. And calculations of distances, areas and percentages cannot do without mathematical functions: roots, powers, rounding.

In this practical session we practise writing complex conditions as logical expressions and using the standard mathematical functions.

:::note
**After this session you will be able to:**

- write a complex condition with AND, OR and NOT;
- work out the value of a logical expression with a truth table;
- place brackets correctly, taking operator precedence into account;
- use square root, power, absolute value, rounding and trigonometric functions in three languages.
:::

## Logical operations

A **boolean expression** has only two possible values: true or false. Comparisons (`>`, `==`, `!=` …) give a boolean value; they can be combined with logical operations.

| Operation | Python | Java, C++ | True when |
|---|---|---|---|
| AND (conjunction) | `and` | `&&` | both parts are true |
| OR (disjunction) | `or` | `\|\|` | at least one part is true |
| NOT (negation) | `not` | `!` | the part is false |

A **truth table** shows every case:

| *A* | *B* | *A* AND *B* | *A* OR *B* | NOT *A* |
|---|---|---|---|---|
| false | false | false | false | true |
| false | true | false | true | true |
| true | false | false | true | false |
| true | true | true | true | false |

Examples: "a number from 10 to 20" — `x >= 10 and x <= 20` (shorter in Python: `10 <= x <= 20`); "an even number" — `x % 2 == 0`; "a weekend day" — `kun == 6 or kun == 7`.

### Short-circuit evaluation

**Short-circuit evaluation**: if the result is known from the first part, the second part is not evaluated. With AND, a false first part makes the whole expression false; with OR, a true first part makes it true. This helps avoid errors: `b != 0 and a / b > 2` never reaches the division by zero when *b* = 0.

### Operator precedence

Without brackets, operations are carried out in this order (top to bottom):

| Precedence | Operations |
|---|---|
| 1 | brackets `( )` |
| 2 | NOT: `not`, `!` |
| 3 | `*`, `/`, `//`, `%` |
| 4 | `+`, `-` |
| 5 | comparisons: `<`, `<=`, `>`, `>=`, `==`, `!=` |
| 6 | AND: `and`, `&&` |
| 7 | OR: `or`, `\|\|` |

AND is done before OR: `a or b and c` means `a or (b and c)`. When in doubt, add brackets: brackets are never superfluous.

## Mathematical functions

Each language has a set of standard mathematical functions: in Python the `math` module (`import math`), in Java the `Math` class (no import needed), in C++ the `<cmath>` header.

| Function | Python | Java | C++ | Example |
|---|---|---|---|---|
| Square root | `math.sqrt(x)` | `Math.sqrt(x)` | `sqrt(x)` | √16 = 4.0 |
| Power | `x ** y`, `math.pow(x, y)` | `Math.pow(x, y)` | `pow(x, y)` | 2¹⁰ = 1024 |
| Absolute value | `abs(x)` | `Math.abs(x)` | `abs(x)`, `fabs(x)` | \|−7\| = 7 |
| Round down | `math.floor(x)` | `Math.floor(x)` | `floor(x)` | ⌊3.7⌋ = 3 |
| Round up | `math.ceil(x)` | `Math.ceil(x)` | `ceil(x)` | ⌈3.2⌉ = 4 |
| Round to nearest | `round(x)` | `Math.round(x)` | `round(x)` | 3.5 → 4 |
| Sine, cosine | `math.sin(x)`, `math.cos(x)` | `Math.sin(x)` | `sin(x)` | the angle in radians |
| The number π | `math.pi` | `Math.PI` | `M_PI` or `acos(-1)` | 3.14159… |

Trigonometric functions take the angle in **radians**: 180° = π radians, so `radians = degrees * pi / 180`.

Python's `round()` rounds a number exactly halfway to the **even** neighbour: `round(2.5)` = 2, `round(3.5)` = 4 ("banker's rounding"). In Java and C++ 2.5 → 3.

## Worked example 1: a leap year

A year is given. Decide whether it is a leap year. The rule: a year is a leap year if it is divisible by 4 and not by 100, or if it is divisible by 400.

```python
yil = int(input())

kabisa = (yil % 4 == 0 and yil % 100 != 0) or yil % 400 == 0
print(kabisa)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int yil = in.nextInt();

        boolean kabisa = (yil % 4 == 0 && yil % 100 != 0) || yil % 400 == 0;
        System.out.println(kabisa);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int yil;
    cin >> yil;

    bool kabisa = (yil % 4 == 0 && yil % 100 != 0) || yil % 400 == 0;
    cout << boolalpha << kabisa << endl;   // boolalpha: true/false instead of 1/0
    return 0;
}
```

For different inputs (*yil* is the year, *kabisa* means leap):

| Year | `% 4 == 0` | `% 100 != 0` | `% 400 == 0` | Result |
|---|---|---|---|---|
| 2024 | true | true | false | true |
| 1900 | true | false | false | false |
| 2000 | true | false | true | true |
| 2026 | false | true | false | false |

Python prints `True`/`False`, Java `true`/`false`. C++ prints `1`/`0` by default; the `boolalpha` manipulator switches to words.

## Worked example 2: Heron's formula

The three sides of a triangle (floating-point numbers) are given on one line. Compute its area with Heron's formula: *p* = (*a* + *b* + *c*) / 2, *S* = √(*p*(*p* − *a*)(*p* − *b*)(*p* − *c*)).

```python
import math

a, b, c = map(float, input().split())

p = (a + b + c) / 2                        # the semi-perimeter
s = math.sqrt(p * (p - a) * (p - b) * (p - c))
print(f"{s:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double a = in.nextDouble();
        double b = in.nextDouble();
        double c = in.nextDouble();

        double p = (a + b + c) / 2;                 // the semi-perimeter
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        System.out.println(String.format("%.2f", s));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

int main() {
    double a, b, c;
    cin >> a >> b >> c;

    double p = (a + b + c) / 2;                 // the semi-perimeter
    double s = sqrt(p * (p - a) * (p - b) * (p - c));
    cout << fixed << setprecision(2) << s << endl;
    return 0;
}
```

For the input `3 4 5`:

1. *p* = (3 + 4 + 5) / 2 = 6.
2. Under the root: 6 · 3 · 2 · 1 = 36.
3. √36 = 6, and `6.00` is printed.

For the input `2 3 4`: *p* = 4.5, under the root 4.5 · 2.5 · 1.5 · 0.5 = 8.4375, *S* ≈ 2.9047, and `2.90` is printed.

:::warning
**Common mistakes**

- **Writing `10 < x < 20` in Java or C++.** It is correct in Python, a compile error in Java, and wrong in meaning in C++: `10 < x` (0 or 1) is evaluated first and then compared with 20 — the result is always true. Correct: `x > 10 && x < 20`.
- **Mixing up `&` and `&&`.** `&` is a bitwise operation and does not short-circuit.
- **Not converting degrees to radians.** `sin(30)` is the sine of 30 radians, not 0.5.
- **Comparing floating-point numbers with `==`.** `0.1 + 0.2 == 0.3` is false! Floating-point numbers are stored approximately; compare like this: `abs(x - y) < 1e-9`.
:::

:::tip
Before writing a complex condition, say it as a full sentence: "the year is divisible by 4 AND not by 100, OR it is divisible by 400". Then translate each part into code and group the parts with brackets.
:::

## Summary

- Logical operations: AND (`and`, `&&`), OR (`or`, `||`), NOT (`not`, `!`); the result follows the truth table.
- Short-circuit evaluation: if the result is known from the first part, the second is not evaluated.
- Precedence: NOT → arithmetic → comparisons → AND → OR; when in doubt, add brackets.
- Mathematical functions: `math` in Python, `Math` in Java, `<cmath>` in C++; angles are in radians.
- Compare floating-point numbers not with `==` but through a small difference.

## Check yourself

:::check
Q: What is `x > 10 and x % 2 == 0` when *x* = 15?
A: False: the first part is true, but 15 is not even.
Q: How do you write `not (a > 5)` without `not`?
A: `a <= 5`.
Q: Does `true || 1 / 0 > 0` cause an error in Java?
A: No: because of short-circuit evaluation the first part of OR is true and the second is not evaluated.
Q: How many 45-seat buses are needed for 47 students, and which function computes it?
A: 2: `ceil(47 / 45)` — rounding up (write `47 / 45.0` to avoid integer division).
Q: What are `round(2.5)` and `round(3.5)` in Python?
A: 2 and 4: Python rounds a halfway number to the even neighbour.
:::

:::link https://docs.python.org/3/library/math.html
Python: the math module
The official list of every mathematical function.
:::

:::link https://en.cppreference.com/w/cpp/numeric/math
cppreference: common mathematical functions
`sqrt`, `pow`, `floor`, `ceil`, `round` and others from `<cmath>`.
:::
