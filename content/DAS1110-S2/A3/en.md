---
title: Working with functions. Function description. Value-returning functions. Procedures. Function overloading
summary: The description of a function (signature and prototype), value-returning functions and procedures, returning several results (a tuple, references, a record), function overloading and default parameters, splitting a problem into functions.
---

## Why it matters

In the first semester we learned to declare a function, call it and return a value from it. In practice the questions sound like this: what if a function must return **two** results? Must functions that do the same job with different parameters get different names? How do you split a big problem into small functions you can test?

In this practical session we practise **designing** functions: writing a function's description precisely, telling a procedure from a value-returning function, returning several results, and overloading.

:::note
**After this session you will be able to:**

- write a function's description (name, parameters, return type) precisely;
- use value-returning functions and procedures where each fits;
- return several results from a function in three languages;
- overload functions in Java and C++, and achieve the same in Python with default parameters;
- split a problem into small functions that call one another.
:::

## The description of a function

A function's **description** (its signature) is everything you need to know to use it: its name, its parameters and their types, and the type of value it returns. The body — "how it works" — does not matter to the caller.

| Language | Example description |
|---|---|
| C++ (a prototype) | `double ortacha(const vector<int>& a);` |
| Java | `static double ortacha(int[] a)` |
| Python | `def ortacha(a: list[int]) -> float:` (type hints are optional) |

A good function does **one job**, and its name says what that job is: `ortacha` (average), `tubmi` (is prime), `chiqar_jadval` (print table). If a function's description needs the word "and" ("reads **and** sorts **and** prints"), it should be split.

## Value-returning functions and procedures

| | Value-returning function | Procedure |
|---|---|---|
| Its job | computes and **returns** a result | **performs** an action (prints, changes an array) |
| Return type | `int`, `double`, `bool`, ... | `void` (Java, C++); `None` in Python |
| The call | inside an expression: `y = kvadrat(x) + 1` | a separate statement: `chiqar(a)` |

The rule: a computing function should **not print**, and a printing procedure should **not compute**. Then the computing function can be reused elsewhere and tested.

## Returning several results

`return` returns only one value. When several results are needed:

| Language | Method | Example |
|---|---|---|
| Python | return a tuple | `return kichik, katta` and `a, b = f(x)` |
| Java | a small class or a `record` | `record Natija(int kichik, int katta)` |
| C++ | reference parameters | `void f(..., int& kichik, int& katta)` |
| C++ | return a `pair` / `struct` | `return {kichik, katta};` |

In C++ `int&` is a reference parameter: the function changes the caller's variable directly. To pass large objects (a vector, a string) **without copying**, write `const vector<int>&` — it is read but not changed.

## Function overloading

**Overloading** means several functions with the same name but different parameters (in number or type). The compiler picks the right one from the arguments in the call:

```text
yuza(r)          → circle
yuza(a, b)       → rectangle
yuza(a, b, c)    → triangle (Heron's formula)
```

- **Java and C++** support overloading directly. Functions that differ only in their return type cannot be overloaded.
- **Python** does not support overloading: a second `def` with the same name replaces the first. Instead, **default parameters** (`def f(a, b=None)`) or a variable number of arguments (`*args`) are used.
- C++ has default parameters too: `void chiqar(int x, char ajratgich = ' ');`

## Worked example 1: an overloaded area function

The first line holds *n*, the next *n* lines one, two or three positive numbers: a circle's radius, a rectangle's sides or a triangle's sides. For every line print the area with two decimal places. In Java and C++ there are three functions all named `yuza` (area); in Python, one function with default parameters.

```python
from math import pi, sqrt


def yuza(a, b=None, c=None):
    if b is None:                       # one number — a circle
        return pi * a * a
    if c is None:                       # two — a rectangle
        return a * b
    p = (a + b + c) / 2                 # three — Heron's formula
    return sqrt(p * (p - a) * (p - b) * (p - c))


n = int(input())
for _ in range(n):
    sonlar = list(map(float, input().split()))
    print(f"{yuza(*sonlar):.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static double yuza(double r) {                       // a circle
        return Math.PI * r * r;
    }

    static double yuza(double a, double b) {             // a rectangle
        return a * b;
    }

    static double yuza(double a, double b, double c) {   // a triangle (Heron)
        double p = (a + b + c) / 2;
        return Math.sqrt(p * (p - a) * (p - b) * (p - c));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            double s;
            if (q.length == 1) s = yuza(Double.parseDouble(q[0]));
            else if (q.length == 2) s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]));
            else s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]), Double.parseDouble(q[2]));
            System.out.printf("%.2f%n", s);
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

double yuza(double r) { return M_PI * r * r; }              // a circle

double yuza(double a, double b) { return a * b; }           // a rectangle

double yuza(double a, double b, double c) {                 // a triangle (Heron)
    double p = (a + b + c) / 2;
    return sqrt(p * (p - a) * (p - b) * (p - c));
}

int main() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream oqim(satr);
        vector<double> q;
        double x;
        while (oqim >> x) q.push_back(x);
        double s;
        if (q.size() == 1) s = yuza(q[0]);
        else if (q.size() == 2) s = yuza(q[0], q[1]);
        else s = yuza(q[0], q[1], q[2]);
        printf("%.2f\n", s);
    }
    return 0;
}
```

Input:

```text
3
1
3 4
3 4 5
```

Output:

```text
3.14
12.00
6.00
```

The count of numbers on each line is not known in advance, so the whole line is read and then split (`split` in Java, `istringstream` — a string stream — in C++). In Python `yuza(*sonlar)` spreads the list into separate arguments: `yuza(*[3.0, 4.0])` is `yuza(3.0, 4.0)`.

## Worked example 2: returning several results

The first line holds *n*, the second *n* integers. A `statistika` function should return the array's smallest element, largest element and arithmetic mean; the main program prints them on one line (the mean with two decimal places).

```python
def statistika(a):
    return min(a), max(a), sum(a) / len(a)      # a tuple


n = int(input())
a = list(map(int, input().split()))
kichik, katta, ortacha = statistika(a)          # unpacking the tuple
print(f"{kichik} {katta} {ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    record Statistika(int kichik, int katta, double ortacha) {}

    static Statistika statistika(int[] a) {
        int kichik = a[0], katta = a[0];
        long yigindi = 0;
        for (int x : a) {
            kichik = Math.min(kichik, x);
            katta = Math.max(katta, x);
            yigindi += x;
        }
        return new Statistika(kichik, katta, (double) yigindi / a.length);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Statistika s = statistika(a);
        System.out.printf("%d %d %.2f%n", s.kichik(), s.katta(), s.ortacha());
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

// the results come back through reference parameters
void statistika(const vector<int>& a, int& kichik, int& katta, double& ortacha) {
    kichik = katta = a[0];
    long long yigindi = 0;
    for (int x : a) {
        if (x < kichik) kichik = x;
        if (x > katta) katta = x;
        yigindi += x;
    }
    ortacha = (double) yigindi / a.size();
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int kichik, katta;
    double ortacha;
    statistika(a, kichik, katta, ortacha);
    printf("%d %d %.2f\n", kichik, katta, ortacha);
    return 0;
}
```

For the input `5` and `4 -2 9 7 1` the output is `-2 9 3.80`. In all three languages the function **computes** and the main program does the printing (*kichik* is the smallest, *katta* the largest, *ortacha* the mean). In C++ `const vector<int>& a` means the array is not copied and the function cannot change it; `int& kichik`, the other way round, writes into the caller's variable. Java's `(double) yigindi` is a type cast to avoid integer division.

## Worked example 3: splitting a problem into functions

A **perfect number** equals the sum of its divisors other than itself (6 = 1 + 2 + 3). The first line holds *a* and *b*. Print all perfect numbers in the range [*a*, *b*] on one line; if there are none, print `-`.

```python
def boluvchilar_yigindisi(n):
    """The sum of the divisors of n other than n itself."""
    if n == 1:
        return 0
    s = 1
    d = 2
    while d * d <= n:
        if n % d == 0:
            s += d
            if d != n // d:
                s += n // d
        d += 1
    return s


def mukammalmi(n):
    return boluvchilar_yigindisi(n) == n


a, b = map(int, input().split())
topilgan = [n for n in range(a, b + 1) if mukammalmi(n)]
print(*topilgan if topilgan else "-")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** The sum of the divisors of n other than n itself. */
    static long boluvchilarYigindisi(long n) {
        if (n == 1) return 0;
        long s = 1;
        for (long d = 2; d * d <= n; d++) {
            if (n % d == 0) {
                s += d;
                if (d != n / d) s += n / d;
            }
        }
        return s;
    }

    static boolean mukammalmi(long n) {
        return boluvchilarYigindisi(n) == n;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long a = in.nextLong(), b = in.nextLong();
        List<String> topilgan = new ArrayList<>();
        for (long n = a; n <= b; n++) {
            if (mukammalmi(n)) topilgan.add(String.valueOf(n));
        }
        System.out.println(topilgan.isEmpty() ? "-" : String.join(" ", topilgan));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

// the sum of the divisors of n other than n itself
long long boluvchilarYigindisi(long long n) {
    if (n == 1) return 0;
    long long s = 1;
    for (long long d = 2; d * d <= n; d++) {
        if (n % d == 0) {
            s += d;
            if (d != n / d) s += n / d;
        }
    }
    return s;
}

bool mukammalmi(long long n) {
    return boluvchilarYigindisi(n) == n;
}

int main() {
    long long a, b;
    cin >> a >> b;
    vector<long long> topilgan;
    for (long long n = a; n <= b; n++) {
        if (mukammalmi(n)) topilgan.push_back(n);
    }
    if (topilgan.empty()) cout << "-";
    for (size_t i = 0; i < topilgan.size(); i++) {
        if (i > 0) cout << ' ';
        cout << topilgan[i];
    }
    cout << endl;
    return 0;
}
```

For the input `1 10000` the output is `6 28 496 8128`; for `30 400` it is `-`. The program has three layers: `boluvchilar_yigindisi` (sum of divisors) only computes, `mukammalmi` (is perfect) answers a yes/no question, and the main program reads and prints. Divisors are searched up to √*n*: if *d* is a divisor, so is *n* / *d*; the condition `d != n / d` keeps the root from being added twice for perfect squares (say 16 = 4 · 4).

:::warning
**Common mistakes**

- **Printing inside a computing function.** With `def ortacha(a): print(...)` the result cannot be used in another computation; `return` it.
- **Trying to overload in Python.** A second `def yuza(a, b)` replaces the first `def yuza(r)`; use default parameters.
- **A "result" parameter without `&` in C++.** `void f(int kichik)` changes a copy, and the caller's variable does not change.
- **A mutable default value in Python.** With `def f(a, royxat=[])` the list is shared by all calls; use `None` and create a new list inside.
:::

:::tip
Before writing a function, write down its description and 2–3 example calls (input → expected result). For example: `mukammalmi(6) → True`, `mukammalmi(8) → False`. Once the function is ready, check it on these examples first.
:::

## Summary

- A function's description is its name, parameters and return type; a good function does one job.
- A value-returning function computes, a procedure (`void`) acts; keep computing and printing apart.
- Several results: Python — a tuple, Java — a `record`, C++ — reference parameters or a `pair`/`struct`.
- Java and C++ overload functions by the number and types of parameters; Python uses default parameters.
- A big problem is split into small functions that call one another.

## Check yourself

:::check
Q: What does a function's description include?
A: Its name, its parameters (and their types) and the type of value it returns.
Q: How does a call to a `void` function differ from a call to a value-returning one?
A: A value-returning function is used inside an expression (`y = f(x) + 1`), while a `void` function is called as a separate statement.
Q: Can `int f(int x)` and `double f(int x)` be declared together in Java?
A: No: functions with the same parameters that differ only in return type cannot be overloaded.
Q: In C++, after calling `void f(int& x) { x = 5; }`, what is the caller's variable equal to?
A: 5 — `x` is a reference, the caller's variable itself.
Q: How can a Python function return two values?
A: As a tuple: `return a, b`, and at the call `x, y = f()`.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#default-argument-values
Python: default argument values
The official tutorial on default parameters and `*args`.
:::

:::link https://en.cppreference.com/w/cpp/language/overload_resolution
C++: overload resolution
How the compiler chooses among overloaded functions.
:::
