---
title: The structure of programming languages
summary: The alphabet of a language, identifiers and keywords, literals, comments, data types, expressions and statements, the overall structure of a program.
---

## Why it matters

To build a sentence in English you need letters, words, parts of speech and punctuation. A programming language is the same: it has its own alphabet, "words", "sentences" and a strict grammar. With one difference — the computer never guesses what you meant. A person reads "recieve" and understands it; a compiler sees `prnt` and stops.

In this lesson we look at the "building materials" of a programming language. They are almost the same in Python, Java and C++: learn them in one language and the next one is easy to read.

:::note
**After this lesson you will be able to:**

- tell apart the alphabet of a language, an identifier, a keyword and a literal;
- distinguish valid identifiers from invalid ones;
- write the basic data types (integer, floating-point, boolean, character, string) in three languages;
- explain what an expression, a statement and a comment are, and analyse the structure of a simple program.
:::

## Alphabet and tokens

Every programming language has an **alphabet**: Latin letters (`A–Z`, `a–z`), digits (`0–9`), special characters (`+ - * / = < > ( ) [ ] { } ; , . : " '`) and whitespace (space, tab, new line).

A compiler or interpreter first splits the program text into **tokens** — the smallest meaningful pieces. For example, the line `narx = 2500 * soni;` consists of these tokens:

| Token | Kind |
|---|---|
| `narx` | identifier |
| `=` | operator (assignment) |
| `2500` | literal (integer) |
| `*` | operator (multiplication) |
| `soni` | identifier |
| `;` | separator |

## Identifiers

An **identifier** is the name of a program element: a variable, a function, a class. The programmer chooses it, but must follow the rules:

1. It consists only of letters, digits and the underscore (`_`).
2. It does not start with a digit: `2son` is invalid, `son2` is valid.
3. It cannot be a keyword: `int`, `class`, `for` are not names.
4. Capital and small letters differ: `Narx`, `narx` and `NARX` are three different names.

| Name | Valid? | Why |
|---|---|---|
| `talaba_soni` | yes | letters and an underscore |
| `jami2026` | yes | digits at the end |
| `_temp` | yes | may start with an underscore |
| `2-guruh` | no | starts with a digit and contains `-` |
| `oʻrtacha` | not recommended | the `ʻ` character is not allowed in many languages |
| `for` | no | a keyword |

**A good name** says what it means: not `x` but `talabalar_soni`; not `a1` but `oylik_maosh`. In Python words are separated by underscores (`oylik_maosh`); in Java and C++ "camel case" is more common (`oylikMaosh`).

## Keywords

**Keywords (reserved words)** are words reserved in advance that have a special meaning for the language. They cannot be used as names.

| Purpose | Python | Java | C++ |
|---|---|---|---|
| Condition | `if`, `elif`, `else` | `if`, `else`, `switch` | `if`, `else`, `switch` |
| Repetition | `for`, `while` | `for`, `while`, `do` | `for`, `while`, `do` |
| Function | `def`, `return` | `return` | `return` |
| Data type | — | `int`, `double`, `boolean`, `char` | `int`, `double`, `bool`, `char` |
| Boolean value | `True`, `False` | `true`, `false` | `true`, `false` |

## Literals and data types

A **literal** is a value written directly in the program text: `42`, `3.14`, `'A'`, `"Salom"`, `true`.

Every value has a **type**. The type decides how the value is stored in memory and which operations can be done with it.

| Type | Meaning | Python | Java | C++ | Example |
|---|---|---|---|---|---|
| Integer | negative, zero, positive whole number | `int` | `int`, `long` | `int`, `long long` | `-7`, `0`, `2026` |
| Floating-point | has a fractional part | `float` | `double` | `double` | `3.14`, `-0.5` |
| Boolean | true or false | `bool` | `boolean` | `bool` | `True` / `true` |
| Character | a single character | — (a 1-character `str`) | `char` | `char` | `'A'` |
| String | a sequence of characters | `str` | `String` | `string` | `"Salom"` |

Java and C++ are **statically typed**: the type of a variable is written when it is declared and does not change afterwards (`int yosh = 19;`). Python is **dynamically typed**: the type follows from the value (`yosh = 19`).

## Expressions and statements

An **expression** is a construct that has a value: `2 + 3`, `narx * soni`, `yosh >= 18`. A **statement** is a command that is executed: an assignment, output, a condition, a loop.

The main operations:

| Operation | Sign | Example | Result |
|---|---|---|---|
| Addition, subtraction | `+`, `-` | `7 - 2` | `5` |
| Multiplication | `*` | `7 * 2` | `14` |
| Division | `/` | `7 / 2` | Python: `3.5`, Java/C++ (integers): `3` |
| Integer division | `//` (Python), `/` (Java/C++, for integers) | `7 // 2` | `3` |
| Remainder | `%` | `7 % 2` | `1` |
| Comparison | `==`, `!=`, `<`, `>`, `<=`, `>=` | `7 > 2` | true |
| Assignment | `=` | `x = 5` | 5 is written into *x* |

Tell `=` and `==` apart: `=` assigns a value, `==` checks equality.

## Comments

A **comment** is text for people that the compiler ignores. It explains *why* the code is written the way it is.

- Python: `# a one-line comment`
- Java and C++: `// a one-line comment` and `/* a multi-line comment */`

## The overall structure of a program

The following program reads the radius of a circle and computes its area. It contains every element of this lesson: a comment, identifiers, keywords, literals, types, an expression and statements.

```python
# Area of a circle: S = pi * r^2
PI = 3.14159            # the literal 3.14159, named PI

r = float(input())      # a floating-point number is read
s = PI * r * r          # an expression
print(round(s, 2))      # the result to 2 decimal places
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Area of a circle: S = pi * r^2
        final double PI = 3.14159;   // a constant (final)

        Scanner in = new Scanner(System.in);
        double r = in.nextDouble();  // a floating-point number is read
        double s = PI * r * r;       // an expression
        System.out.printf("%.2f%n", s);
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    // Area of a circle: S = pi * r^2
    const double PI = 3.14159;   // a constant (const)

    double r;
    cin >> r;                    // a floating-point number is read
    double s = PI * r * r;       // an expression
    cout << fixed << setprecision(2) << s << endl;
    return 0;
}
```

For the input `5` all three programs print `78.54`:

1. `PI` is the name of a constant; in Java `final` and in C++ `const` guarantee that the value does not change. In Python constancy is a convention: the name is written in capitals.
2. The radius is read as a floating-point number: `float(...)`, `nextDouble()`, `cin >> r`.
3. The expression `PI * r * r` is evaluated and stored in *s*: 3.14159 · 25 = 78.53975.
4. The result is rounded to two decimal places and printed.

:::warning
**Common mistakes**

- **Mixing up `=` and `==`.** `if (x = 5)` is not a compile error in C++, but the condition is always true — a logic error.
- **Dividing integers.** In Java and C++ `7 / 2` is `3`. If you need a fraction, one of the numbers must be floating-point: `7.0 / 2`.
- **The ʻ character or a space in a name.** `oʻrtacha ball` is not a name. Write `ortacha_ball`.
- **Complex code without comments.** A week later you will not understand it yourself.
:::

:::tip
When choosing a name, ask yourself: "would someone else understand it without a comment?" A well-chosen name is the best comment.
:::

## Summary

- Program text is made of tokens: identifiers, keywords, literals, operators and separators.
- An identifier starts with a letter or `_`, consists of letters, digits and `_`, and cannot be a keyword; case matters.
- The basic types are integer, floating-point, boolean, character and string. Java and C++ are statically typed, Python dynamically.
- An expression has a value, a statement is executed; `=` is assignment, `==` is comparison.
- Comments explain why the code is written the way it is.

## Check yourself

:::check
Q: Which of `2guruh`, `guruh_2`, `class`, `_soni` are valid identifiers?
A: `guruh_2` and `_soni`. `2guruh` starts with a digit, and `class` is a keyword in Java and C++.
Q: What does the Java code `int a = 7, b = 2; System.out.println(a / b);` print?
A: `3`. Both numbers are integers, so the division returns the whole part.
Q: What is the difference between static and dynamic typing?
A: With static typing (Java, C++) the type of a variable is written when it is declared and does not change. With dynamic typing (Python) the type follows from the value, and the variable can later be given a value of another type.
Q: How do `x = 5` and `x == 5` differ?
A: The first writes 5 into *x* (assignment); the second checks whether *x* equals 5 and returns true or false.
Q: What are `17 % 5` and `17 // 5` in Python?
A: `17 % 5` = 2 (the remainder), `17 // 5` = 3 (the whole quotient).
:::

:::link https://docs.python.org/3/reference/lexical_analysis.html
Python: lexical analysis (identifiers and keywords)
The official Python language reference.
:::

:::link https://en.cppreference.com/w/cpp/language/types
cppreference: C++ fundamental types
Sizes and ranges of the integer, floating-point, boolean and character types.
:::
