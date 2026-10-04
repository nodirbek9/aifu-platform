---
title: Branching processes. The conditional statement
summary: Practical problems with the if statement: finding the largest, the type of a triangle, the quadrant of a point; validating input and testing every branch.
---

## Why it matters

An online shop makes delivery free when the basket is over 300 000 sum, and gives a further 5 % discount over 1 000 000. A bank checks monthly income, age and credit history before granting a loan. Writing such rules with `if` without mistakes is a programmer's daily work.

In the lecture we learnt `if`, `else` and `elif` / `else if`. In this practical session we apply them to typical problems and make a habit of checking every branch with a test.

:::note
**After this session you will be able to:**

- find the largest (smallest) of three or more numbers;
- solve problems that depend on several conditions (the type of a triangle, the quadrant of a point) with conditions in the right order;
- check that the input is valid and print a message when it is not;
- write a test for every branch and boundary case.
:::

## The conditional statement: a quick review

| Construct | Python | Java, C++ |
|---|---|---|
| Only if | `if s:` | `if (s) { }` |
| If … else | `if s:` … `else:` | `if (s) { } else { }` |
| Chain | `if` … `elif` … `else` | `if` … `else if` … `else` |

Three rules:

1. In a chain the first true condition runs — the order of the conditions decides the result.
2. In Java and C++ always use `{ }`, even when the block has one line.
3. Every branch must be checked by at least one test.

## Pattern: finding the largest

A reliable way to find the largest of several values is a **"largest so far"** variable:

1. Take the first value as the largest: `mx = a`.
2. Compare each next value with it: `if b > mx: mx = b`.
3. At the end `mx` is the answer.

This works for any number of values and is later used together with a loop. By comparison, "check that each number is larger than the others" (`a >= b and a >= c`) works for three numbers, but the conditions grow quickly as the numbers multiply.

There are standard functions too: `max(a, b, c)` in Python, `Math.max(a, b)` in Java, `max({a, b, c})` in C++. In the practical session we first write it by hand to understand the algorithm.

## Pattern: validating the input

A program must respond correctly to bad data too. For example, the sides of a triangle must be positive and satisfy the **triangle inequality**: each side is shorter than the sum of the other two. So first the check, then the computation:

```text
if the data is invalid:
    print a message
else:
    the main computation
```

## Worked example 1: the largest of three numbers

Three integers are given on one line. Print the largest.

```python
a, b, c = map(int, input().split())

mx = a                 # the largest so far
if b > mx:
    mx = b
if c > mx:
    mx = c

print(mx)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        int mx = a;            // the largest so far
        if (b > mx) {
            mx = b;
        }
        if (c > mx) {
            mx = c;
        }

        System.out.println(mx);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    int mx = a;            // the largest so far
    if (b > mx) {
        mx = b;
    }
    if (c > mx) {
        mx = c;
    }

    cout << mx << endl;
    return 0;
}
```

For the input `7 12 9`:

1. `mx = 7`.
2. `12 > 7` is true, `mx = 12`.
3. `9 > 12` is false, `mx` does not change.
4. The output is `12`.

Note: there are two **separate** `if`s here, not a chain — because every number must be checked. Tests: the largest first (`9 1 5`), in the middle, last, equal numbers (`4 4 4`), negative numbers (`-5 -2 -9` → `-2`).

## Worked example 2: the type of a triangle

The three sides of a triangle (positive integers) are given. If no such triangle exists, print `does not exist`; otherwise print its type: `equilateral`, `isosceles` or `scalene`.

```python
a, b, c = map(int, input().split())

if a + b <= c or a + c <= b or b + c <= a:
    print("does not exist")
elif a == b and b == c:
    print("equilateral")
elif a == b or b == c or a == c:
    print("isosceles")
else:
    print("scalene")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        if (a + b <= c || a + c <= b || b + c <= a) {
            System.out.println("does not exist");
        } else if (a == b && b == c) {
            System.out.println("equilateral");
        } else if (a == b || b == c || a == c) {
            System.out.println("isosceles");
        } else {
            System.out.println("scalene");
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    if (a + b <= c || a + c <= b || b + c <= a) {
        cout << "does not exist" << endl;
    } else if (a == b && b == c) {
        cout << "equilateral" << endl;
    } else if (a == b || b == c || a == c) {
        cout << "isosceles" << endl;
    } else {
        cout << "scalene" << endl;
    }
    return 0;
}
```

Analysis:

| Input | Which condition is true | Result |
|---|---|---|
| `1 2 5` | the first: 1 + 2 ≤ 5 | `does not exist` |
| `5 5 5` | the second | `equilateral` |
| `5 5 8` | the third | `isosceles` |
| `3 4 5` | none | `scalene` |

The order matters: the "equilateral" condition must come **before** "isosceles", because an equilateral triangle also has two equal sides. Otherwise `5 5 5` would be called "isosceles". The validity check comes first of all: there is no point in finding the type of a triangle that does not exist.

## Worked example 3: the quadrant of a point

The coordinates *x* and *y* of a point (integers) are given. Print which quadrant the point lies in; if it lies on an axis, print `on an axis`.

```python
x, y = map(int, input().split())

if x == 0 or y == 0:
    print("on an axis")
elif x > 0:
    if y > 0:
        print(1)
    else:
        print(4)
else:
    if y > 0:
        print(2)
    else:
        print(3)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int x = in.nextInt();
        int y = in.nextInt();

        if (x == 0 || y == 0) {
            System.out.println("on an axis");
        } else if (x > 0) {
            if (y > 0) {
                System.out.println(1);
            } else {
                System.out.println(4);
            }
        } else {
            if (y > 0) {
                System.out.println(2);
            } else {
                System.out.println(3);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int x, y;
    cin >> x >> y;

    if (x == 0 || y == 0) {
        cout << "on an axis" << endl;
    } else if (x > 0) {
        if (y > 0) {
            cout << 1 << endl;
        } else {
            cout << 4 << endl;
        }
    } else {
        if (y > 0) {
            cout << 2 << endl;
        } else {
            cout << 3 << endl;
        }
    }
    return 0;
}
```

A nested condition fits here: first the sign of *x*, then the sign of *y*. Input `-3 4` → `2`, `5 -1` → `4`, `0 7` → `on an axis`. Four quadrants and the axis make five branches, so at least five tests are needed.

:::warning
**Common mistakes**

- **Finding the largest with an `if-elif` chain.** `if b > mx … elif c > mx` — when *b* is larger, *c* is never checked.
- **Mixing up `<` and `<=` in the triangle inequality.** `1 2 3` is a "flat" triangle and does not exist: the test must be `a + b <= c`.
- **Checking the general case before the special one.** If "isosceles" comes before "equilateral", an equilateral triangle is never found.
- **Forgetting the boundary case.** When the point is on an axis (*x* = 0), the program prints a wrong quadrant.
:::

:::tip
Write the tests **before** the code as a table: input, expected result, which branch. When the code is ready, check every row of the table.
:::

## Summary

- The largest (smallest) value is found with a "largest so far" variable and separate `if`s.
- The input is validated first, then the computation is done.
- In a chain the special case (equilateral) comes before the general one (isosceles).
- A nested condition fits when there is a logical hierarchy (first *x*, then *y*).
- Every branch and boundary case needs its own test.

## Check yourself

:::check
Q: Why are separate `if`s used, not `elif`, to find the largest?
A: In a chain the rest are not checked after the first true condition, but every number has to be compared with the "largest so far".
Q: Does a triangle with sides `2 3 5` exist?
A: No: 2 + 3 = 5, the inequality is not strict — the triangle is flat.
Q: What does the triangle-type program print for `5 5 5` if the "isosceles" condition comes first?
A: "isosceles": `a == b` is true and the chain stops there.
Q: At least how many tests does the quadrant problem need?
A: Five: four quadrants and a point on an axis.
Q: What do you change in the program to find the smallest of three numbers?
A: Replace the comparison `>` with `<` (and better call the variable `mn`).
:::

:::link https://docs.python.org/3/library/functions.html#max
Python: the max() function
The standard function that returns the largest of several values.
:::

:::link https://en.cppreference.com/w/cpp/language/if
cppreference: the if statement
The syntax of `if` and `else` in C++.
:::
