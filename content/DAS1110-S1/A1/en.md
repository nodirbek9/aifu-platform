---
title: Kinds of compilers. Identifiers and their kinds
summary: GCC, Clang, MSVC, javac and CPython; compiling and running a program from the command line; reading compiler messages; names of variables, constants, functions and classes.
---

## Why it matters

Most of a programmer's day goes not into writing code but into understanding what the compiler "is trying to say". A student who can read the message `error: expected ';' before 'return'` fixes the error in a minute; one who cannot spends half an hour rewriting the code.

In this practical session we practise compiling and running a program on your own computer, reading compiler messages and choosing names well.

:::note
**After this session you will be able to:**

- tell apart the main compilers and interpreters (GCC, Clang, MSVC, javac, CPython);
- run a Python, Java and C++ program from the command line;
- read the compiler's error and warning messages and find the mistake;
- write the names of variables, constants, functions and classes following each language's rules.
:::

## Kinds of compilers

One language can have several compilers: they implement the same standard but are made by different companies.

| Tool | Language | Kind | Where you meet it |
|---|---|---|---|
| **GCC** (`g++`) | C, C++ | compiler to machine code | Linux, Windows through MinGW |
| **Clang** (`clang++`) | C, C++ | compiler to machine code | macOS, Linux, Windows |
| **MSVC** (`cl`) | C, C++ | compiler to machine code | Visual Studio, Windows |
| **javac** | Java | compiler to bytecode | part of the JDK |
| **JVM** (`java`) | Java bytecode | interpreter + JIT compiler | part of the JDK or JRE |
| **CPython** (`python`) | Python | interpreter (bytecode inside) | installed from python.org |

**JIT (just-in-time) compilation** translates the frequently executed parts of a program into machine code while it runs. That is why a Java program speeds up as it works.

The practice tasks of this course are checked on the platform with GCC 13 (C++17), Java 21 and Python 3.11.

## Compiling and running a program

On the command line (terminal) the sequence for each language is:

| Language | Compile | Run |
|---|---|---|
| C++ | `g++ -std=c++17 -o main main.cpp` | `./main` (Windows: `main.exe`) |
| Java | `javac Main.java` | `java Main` |
| Python | — | `python main.py` |

A C++ program goes through three stages when it is compiled:

1. The **preprocessor** carries out the `#include` and `#define` directives: for example, it inserts the text of `<iostream>` into the program.
2. **Compilation** turns the text into an object file (machine code); syntax errors are found here.
3. **Linking** joins the object files and libraries into one executable file.

In Java the file name must match the name of the `public` class: `public class Main` can only live in a file called `Main.java`.

## Reading compiler messages

A compiler gives two kinds of messages:

- An **error** — the program does not compile; it must be fixed.
- A **warning** — the program compiles, but the compiler points at a suspicious place. Do not ignore warnings: they often point at a logic error.

A message usually consists of **the file name, the line number, the column number and a description**:

| Language | Sample message | Meaning |
|---|---|---|
| C++ | `main.cpp:6:18: error: expected ';' before 'return'` | a semicolon is missing on line 6 |
| Java | `Main.java:5: error: cannot find symbol` | a name on line 5 is not declared or is misspelt |
| Python | `NameError: name 'pirnt' is not defined` | the name `print` is misspelt |

The rule: **start with the first error**. One missing bracket can cause dozens of "errors"; fix the first and the rest disappear by themselves. Also look at the line *before* the one reported — that is usually where the semicolon is missing.

## Identifiers and their kinds

Every name in a program stands for something. Depending on what it stands for, names are written differently — these are **naming conventions**:

| What | Python | Java | C++ (a common style) |
|---|---|---|---|
| Variable | `talaba_soni` | `talabaSoni` | `talabaSoni` or `talaba_soni` |
| Constant | `MAX_BALL` | `MAX_BALL` | `MAX_BALL` or `kMaxBall` |
| Function | `ortacha_ball()` | `ortachaBall()` | `ortachaBall()` |
| Class | `Talaba` | `Talaba` | `Talaba` |

The styles are called `snake_case` (with underscores), `camelCase` (later words capitalised), `PascalCase` (the first letter capitalised too) and `UPPER_CASE` (all capitals). The compiler does not check the style, but in a team everyone writes the same way — it makes the code easier to read.

The value of a **variable** can change while the program runs. The value of a **constant** is given once: with `const` in C++, with `final` in Java; in Python only by convention (a name in capitals).

## Worked example 1: a rectangle

The two sides of a rectangle (integers) are given. Print its perimeter and area.

```python
# Perimeter and area of a rectangle
a = int(input())
b = int(input())

perimetr = 2 * (a + b)
yuza = a * b

print("Perimeter:", perimetr)
print("Area:", yuza)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Perimeter and area of a rectangle
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        int perimetr = 2 * (a + b);
        int yuza = a * b;

        System.out.println("Perimeter: " + perimetr);
        System.out.println("Area: " + yuza);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    // Perimeter and area of a rectangle
    int a, b;
    cin >> a >> b;

    int perimetr = 2 * (a + b);
    int yuza = a * b;

    cout << "Perimeter: " << perimetr << endl;
    cout << "Area: " << yuza << endl;
    return 0;
}
```

For the input `7` and `4`:

1. *a* = 7 and *b* = 4 are read.
2. `2 * (a + b)` — the brackets first: 7 + 4 = 11, then 2 · 11 = 22. Without brackets `2 * a + b` = 18 — the order of operations matters.
3. `a * b` = 28.
4. The output is `Perimeter: 22` and `Area: 28`.

The names mean something: `perimetr` (perimeter), `yuza` (area). Even a month later it is clear what the program does.

## Worked example 2: swapping the values of two variables

Two integers *a* and *b* are given. Swap their values and print the result.

```python
a = int(input())
b = int(input())

# Swap through a third, temporary variable
temp = a
a = b
b = temp

print("a =", a, "b =", b)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        // Swap through a third, temporary variable
        int temp = a;
        a = b;
        b = temp;

        System.out.println("a = " + a + " b = " + b);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;

    // Swap through a third, temporary variable
    int temp = a;
    a = b;
    b = temp;

    cout << "a = " << a << " b = " << b << endl;
    return 0;
}
```

For the input `5` and `9` the values change step by step like this:

| Step | *a* | *b* | *temp* |
|---|---|---|---|
| After reading | 5 | 9 | — |
| `temp = a` | 5 | 9 | 5 |
| `a = b` | 9 | 9 | 5 |
| `b = temp` | 9 | 5 | 5 |

The result is `a = 9 b = 5`. Writing `a = b; b = a;` would leave both equal to 9: the old value of *a* is lost. Python also has a short form: `a, b = b, a`.

:::warning
**Common mistakes**

- **The Java file name does not match the class name.** `public class Main` in `main.java` is an error: `class Main is public, should be declared in a file named Main.java`.
- **Starting with the last error.** Once the first error is fixed, the others often disappear.
- **Ignoring warnings.** `warning: unused variable` or `warning: suggest parentheses` can be the sign of a real bug.
- **Forgetting the temporary variable when swapping.** One value is lost.
:::

:::tip
Compile C++ programs with `g++ -Wall -Wextra`: the compiler gives more warnings, and many bugs are found before the program is even run.
:::

## Summary

- C++ has the GCC, Clang and MSVC compilers; Java is compiled by `javac` into bytecode and runs on the JVM; a Python program is executed by the CPython interpreter.
- C++ compilation: preprocessor → compilation → linking.
- A compiler message has the file, the line and a description; start with the first error and read the warnings too.
- Names are written differently depending on their kind: `snake_case`, `camelCase`, `PascalCase`, `UPPER_CASE`.
- Swapping two variables needs a third, temporary one.

## Check yourself

:::check
Q: What is the main difference between GCC, javac and CPython?
A: GCC compiles a C++ program into machine code, javac turns a Java program into bytecode for the JVM, and CPython interprets a Python program.
Q: At which stage of C++ compilation is `#include <iostream>` carried out?
A: At the preprocessor stage: the text of the file is inserted into the program.
Q: What does `main.cpp:12:5: error: 'cout' was not declared in this scope` mean?
A: In `main.cpp`, line 12, column 5, the name `cout` is not recognised: `#include <iostream>` or `using namespace std;` (or `std::cout`) is missing.
Q: How are the names of constants and classes written in Java?
A: Constants in `UPPER_CASE` (`MAX_BALL`), classes in `PascalCase` (`Talaba`).
Q: Why does `a = b; b = a;` not swap the values?
A: After the first line the old value of *a* is lost, and both variables hold the value of *b*.
:::

:::link https://gcc.gnu.org/onlinedocs/gcc/Warning-Options.html
GCC: warning options
The official documentation of `-Wall`, `-Wextra` and other options.
:::

:::link https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html
Oracle: Java naming conventions
How the names of classes, methods, variables and constants are written.
:::
