---
title: Working with functions
summary: What a function is and why it is needed, declaring and calling, parameters and arguments, the return value, void functions, pass by value, local and global variables, the C++ prototype.
---

## Why it matters

In the dean's office program a student's average score is computed in five places: the ranking, the scholarship, the transcript, the report and the message to parents. If the calculation is copied five times, then when the formula has to change you must find five places and fix them all the same way. Forget one and different pages show different scores.

A **function** solves this: the code is written once, given a name, and called by that name wherever it is needed. Functions break a large program into small, understandable pieces: each does one job and is checked on its own. You have already used functions: `print`, `sqrt`, `max` are all functions.

:::note
**After this lesson you will be able to:**

- declare and call a function and tell a parameter from an argument;
- write functions that return a value and functions that do not (`void`);
- explain what pass by value means and what happens when an array is passed;
- tell local and global variables apart;
- solve a problem by splitting it into several small functions.
:::

## Declaring a function

A **function** is a named, reusable piece of code. It takes input data (**parameters**), does some work and gives back a result (the **return value**).

| Part | Python | Java | C++ |
|---|---|---|---|
| Declaration | `def kvadrat(x):` | `static int kvadrat(int x) {` | `int kvadrat(int x) {` |
| Returning the result | `return x * x` | `return x * x;` | `return x * x;` |
| Call | `y = kvadrat(5)` | `int y = kvadrat(5);` | `int y = kvadrat(5);` |

In Java and C++ the function header states the **return type** (`int`) and the type of every parameter. In Python types are not written (optionally as annotations: `def kvadrat(x: int) -> int:`).

In Java a function (method) always lives inside a class; ordinary functions called from `main` are written as `static`.

## Parameters and arguments

- A **parameter** is the name in the function's declaration: *x* in `def kvadrat(x)`.
- An **argument** is the concrete value given in a call: 5 in `kvadrat(5)` (*kvadrat* means square).

In a call the arguments are matched to the parameters **by order**: in `daraja(2, 10)` the first argument goes to the first parameter, the second to the second. The number and types of the arguments must match the parameters.

## return and void

`return` does two things: it gives back the result and **ends the function immediately**. Code after `return` is not executed.

A function can also return nothing — it only performs an action (such as printing):

| Language | A function that returns no value |
|---|---|
| Python | no `return` (it actually returns `None`) |
| Java, C++ | the return type is `void` |

A good habit: a function that computes something should **return** the result, not print it. Then it can be used anywhere: printed, compared, added into another calculation.

## Pass by value

In Java, C++ and Python simple types (numbers, booleans) are passed to a function **by value**: the parameter receives a **copy** of the argument. Changing the parameter inside the function does not affect the caller's variable.

Arrays are different:

| Language | When an array is passed | If the function changes an element |
|---|---|---|
| Python | a reference to the list is passed | the original list changes |
| Java | a reference to the array is passed | the original array changes |
| C++ (`vector<int> a`) | the whole vector is **copied** | the original vector does not change |
| C++ (`vector<int>& a`) | a reference (`&`) is passed | the original vector changes |

The `&` sign in C++ means **pass by reference**: the parameter becomes the very same variable as the argument. This topic is covered in more depth in lesson M9 (pointers).

## Local and global variables

- A **local variable** is declared inside a function; it exists only in that function and disappears when the function ends. Parameters are local too.
- A **global variable** is declared outside all functions; it is visible everywhere.

Local variables with the same name in different functions are **different** variables. Avoid global variables as far as possible: any function can change them, and bugs become hard to find. Let a function receive everything it needs through parameters and give its result back through `return`.

## C++: the prototype

The C++ compiler reads the file from top to bottom: a function must be declared **before** it is called. There are two ways: write the function before `main`, or write only its **prototype** (the header followed by a semicolon) before `main` and give the body later:

```text
bool tubmi(int n);        // the prototype
int main() { ... tubmi(7) ... }
bool tubmi(int n) { ... } // the definition
```

Python and Java do not have this problem: Python looks the function up at the moment of the call, and Java sees the whole class in advance.

## Worked example 1: a list of primes

A natural number *n* is given. Print on one line all the primes from 2 to *n*. Put the primality test into a separate function.

```python
def tubmi(n):
    if n < 2:
        return False
    i = 2
    while i * i <= n:
        if n % i == 0:
            return False
        i += 1
    return True


n = int(input())
tublar = []
for k in range(2, n + 1):
    if tubmi(k):
        tublar.append(k)
print(*tublar)
```
```java
import java.util.Scanner;

public class Main {
    static boolean tubmi(int n) {
        if (n < 2) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        StringBuilder sb = new StringBuilder();
        for (int k = 2; k <= n; k++) {
            if (tubmi(k)) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(k);
            }
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

bool tubmi(int n);                 // the prototype

int main() {
    int n;
    cin >> n;

    bool birinchi = true;
    for (int k = 2; k <= n; k++) {
        if (tubmi(k)) {
            if (!birinchi) cout << ' ';
            cout << k;
            birinchi = false;
        }
    }
    cout << endl;
    return 0;
}

bool tubmi(int n) {
    if (n < 2) {
        return false;
    }
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) {
            return false;
        }
    }
    return true;
}
```

For the input `30` the output is `2 3 5 7 11 13 17 19 23 29`.

1. `main` calls `tubmi(k)` ("is it prime?") for every *k* from 2 to 30.
2. Inside `tubmi`, *n* is a parameter; on every call it receives a new value (a copy of *k*).
3. As soon as a divisor is found, `return False` ends the function immediately — no flag or `break` is needed.

Compare it with the code of session A7: the same test now has a name and is reused. `main` says "what is done", `tubmi` says "how".

## Worked example 2: GCD and LCM

Two natural numbers are given. Print their GCD and LCM. LCM(*a*, *b*) = *a* · *b* / GCD(*a*, *b*).

```python
def ekub(a, b):
    while b != 0:
        a, b = b, a % b
    return a


def ekuk(a, b):
    return a // ekub(a, b) * b


x, y = map(int, input().split())
print("GCD:", ekub(x, y))
print("LCM:", ekuk(x, y))
```
```java
import java.util.Scanner;

public class Main {
    static long ekub(long a, long b) {
        while (b != 0) {
            long q = a % b;
            a = b;
            b = q;
        }
        return a;
    }

    static long ekuk(long a, long b) {
        return a / ekub(a, b) * b;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long x = in.nextLong();
        long y = in.nextLong();
        System.out.println("GCD: " + ekub(x, y));
        System.out.println("LCM: " + ekuk(x, y));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long ekub(long long a, long long b) {
    while (b != 0) {
        long long q = a % b;
        a = b;
        b = q;
    }
    return a;
}

long long ekuk(long long a, long long b) {
    return a / ekub(a, b) * b;
}

int main() {
    long long x, y;
    cin >> x >> y;
    cout << "GCD: " << ekub(x, y) << endl;
    cout << "LCM: " << ekuk(x, y) << endl;
    return 0;
}
```

For the input `12 18`: GCD = 6, LCM = 12 / 6 · 18 = 36. The output is `GCD: 6` and `LCM: 36`. (`ekub` and `ekuk` are the Uzbek abbreviations for GCD and LCM.)

Note:

- The `ekuk` function **calls** `ekub` — functions use each other.
- Inside `ekub`, *a* and *b* change, but *x* and *y* in `main` stay the same: parameters are copies.
- `a / ekub(a, b) * b` divides first and multiplies afterwards: `a * b` could overflow for large numbers, while the intermediate result is smaller.

:::warning
**Common mistakes**

- **`print` instead of `return`.** The function prints the result but does not return it: in `y = f(5)`, *y* is `None` (Python) or a compile error (Java, C++).
- **Not every path has a `return`.** Java gives `missing return statement`; C++ gives no error, but the result is undefined.
- **Changing a parameter and expecting the caller to see it.** Simple types are passed as copies; return the result with `return`.
- **A call without a prototype in C++.** If the function is written after `main`: `'tubmi' was not declared in this scope`.
- **Forgetting `static` in Java.** From `main`: `non-static method cannot be referenced from a static context`.
:::

:::tip
Name functions with a verb or a question: `hisobla_ortacha` ("compute the average"), `tubmi` ("is it prime?"), `chiqar_jadval` ("print the table"). If a function cannot be explained in one sentence, it does too much — split it in two.
:::

## Summary

- A function is a named, reusable piece of code: it takes parameters and returns a result with `return`.
- A parameter is the name in the declaration, an argument the value in the call; they are matched by order.
- `return` gives back the result and ends the function; a function with no result is `void` (in Python, no `return`).
- Simple types are passed by value; in Python and Java an array is passed by reference, in C++ a `vector` is copied (`&` for a reference).
- Local variables live only inside their function; avoid global ones.
- In C++ a function must be declared (or prototyped) before it is called.

## Check yourself

:::check
Q: In `def f(a, b): return a - b` and the call `f(10, 3)`, which are the parameters and which the arguments?
A: The parameters are *a* and *b*; the arguments are 10 and 3. The result is 7.
Q: What does Python print for `def f(x): x = x + 1` and `n = 5; f(n); print(n)`?
A: 5: the new value is given only to the local *x*; *n* does not change.
Q: In C++, does the first element of the original vector change after calling `void tozala(vector<int> a) { a[0] = 0; }`?
A: No: the vector is copied. For it to change, the parameter must be `vector<int>& a`.
Q: Is a line written after `return` executed?
A: No: `return` ends the function immediately.
Q: Why is it better for a computing function to return its result rather than print it?
A: A returned result can be used anywhere: printed, compared, added to other calculations; a printing function can only be used for printing.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#defining-functions
Python: defining functions
The `def` and `return` section of the official tutorial.
:::

:::link https://en.cppreference.com/w/cpp/language/function
cppreference: function declaration
Declaring and defining functions in C++.
:::
