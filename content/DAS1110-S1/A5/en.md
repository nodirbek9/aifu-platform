---
title: Branching processes. The selection statement. The ternary operator
summary: The switch (Java, C++) and match (Python) selection statements, case, break, default, fall-through; the ternary operator; when to use if and when switch.
---

## Why it matters

An ATM menu: 1 — balance, 2 — cash, 3 — payment, 4 — exit. Each key of a phone keypad gives its own letters. A timetable is shown for the number of the weekday. In such problems one variable is compared with one of **several exact values**. This can be written with an `if-else if` chain, but there is a special, more readable statement for it — the **selection statement**.

We also meet a short tool that writes an "one of two" choice as a single expression — the **ternary operator**.

:::note
**After this session you will be able to:**

- write `switch` (Java, C++) and `match` (Python) statements;
- explain what `break` and `default` do and avoid the fall-through bug;
- choose a value by a condition with the ternary operator;
- pick the right tool for a problem: `if`, `switch` or the ternary operator.
:::

## The switch statement (Java, C++)

The **`switch` selection statement** compares the value of an expression with the constants of the `case` labels in turn and starts executing at the one that matches.

| Part | Purpose |
|---|---|
| `switch (expression)` | which value is checked (an integer, a character; in Java also a string) |
| `case 1:` | "if the value is 1, start here" |
| `break;` | leave the `switch` |
| `default:` | when no `case` matches (optional, but recommended) |

An important feature is **fall-through**: without `break`, execution runs on into the next `case`. It is often a source of bugs, but sometimes it is used on purpose: one action for several values.

```text
case 6:
case 7:
    "weekend"
    break
```

In C++ `switch` works only with integer and character types; in Java also with strings (`String`). Since Java 14 there is a short form: `case 1 -> ...;` — it has no fall-through and needs no `break`.

## The match statement (Python)

Since Python 3.10 there is a `match` statement:

| Part | Meaning |
|---|---|
| `match expression:` | which value is checked |
| `case 1:` | if the value is 1 |
| `case 6 \| 7:` | if it is 6 or 7 |
| `case _:` | every other case (in place of `default`) |

Python has no fall-through: only the matching `case` runs, and no `break` is needed. Before version 3.10 the same problem is written with `if-elif`.

## The ternary operator

The **ternary (three-part) operator** returns one of two values depending on a condition. It is not a *statement* but an *expression*: its result can be assigned to a variable or printed directly.

| Language | How it is written | Example |
|---|---|---|
| Java, C++ | `condition ? value1 : value2` | `mx = a > b ? a : b;` |
| Python | `value1 if condition else value2` | `mx = a if a > b else b` |

The ternary operator is handy for short choices. Nested ternaries (`a ? b : c ? d : e`) are hard to read — use `if` in such cases.

## Which one when?

| Situation | Choice |
|---|---|
| One variable compared with several exact values (a menu, a day, a command) | `switch` / `match` |
| Ranges (`ball >= 86`), complex conditions | `if-else if` |
| One of two values, in one expression | the ternary operator |

## Worked example 1: the day of the week

The number of a weekday (1–7) is given. Print the day's name; for 6 and 7 add "(weekend)". For a wrong number print `wrong day`.

```python
kun = int(input())

match kun:
    case 1:
        print("Monday")
    case 2:
        print("Tuesday")
    case 3:
        print("Wednesday")
    case 4:
        print("Thursday")
    case 5:
        print("Friday")
    case 6:
        print("Saturday (weekend)")
    case 7:
        print("Sunday (weekend)")
    case _:
        print("wrong day")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int kun = in.nextInt();

        switch (kun) {
            case 1: System.out.println("Monday"); break;
            case 2: System.out.println("Tuesday"); break;
            case 3: System.out.println("Wednesday"); break;
            case 4: System.out.println("Thursday"); break;
            case 5: System.out.println("Friday"); break;
            case 6: System.out.println("Saturday (weekend)"); break;
            case 7: System.out.println("Sunday (weekend)"); break;
            default: System.out.println("wrong day");
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int kun;
    cin >> kun;

    switch (kun) {
        case 1: cout << "Monday" << endl; break;
        case 2: cout << "Tuesday" << endl; break;
        case 3: cout << "Wednesday" << endl; break;
        case 4: cout << "Thursday" << endl; break;
        case 5: cout << "Friday" << endl; break;
        case 6: cout << "Saturday (weekend)" << endl; break;
        case 7: cout << "Sunday (weekend)" << endl; break;
        default: cout << "wrong day" << endl;
    }
    return 0;
}
```

For the input `3`, `case 3` matches, `Wednesday` is printed, and `break` leaves the `switch`. For the input `9` no `case` matches — `default` runs. If the `break` of `case 3` were forgotten in Java or C++, `Thursday` would be printed after `Wednesday` as well.

## Worked example 2: a calculator

An expression is given on one line: an integer, an operator sign (`+`, `-`, `*`, `/`) and an integer, separated by spaces. Print the result; for division, to two decimal places. For division by zero and an unknown operator, print a message.

```python
qism = input().split()
a = int(qism[0])
amal = qism[1]
b = int(qism[2])

match amal:
    case "+":
        print(a + b)
    case "-":
        print(a - b)
    case "*":
        print(a * b)
    case "/":
        if b == 0:
            print("Error: division by zero")
        else:
            print(f"{a / b:.2f}")
    case _:
        print("Error: unknown operation")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        char amal = in.next().charAt(0);
        int b = in.nextInt();

        switch (amal) {
            case '+': System.out.println(a + b); break;
            case '-': System.out.println(a - b); break;
            case '*': System.out.println(a * b); break;
            case '/':
                if (b == 0) {
                    System.out.println("Error: division by zero");
                } else {
                    System.out.println(String.format("%.2f", (double) a / b));
                }
                break;
            default: System.out.println("Error: unknown operation");
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int a, b;
    char amal;
    cin >> a >> amal >> b;

    switch (amal) {
        case '+': cout << a + b << endl; break;
        case '-': cout << a - b << endl; break;
        case '*': cout << a * b << endl; break;
        case '/':
            if (b == 0) {
                cout << "Error: division by zero" << endl;
            } else {
                cout << fixed << setprecision(2) << (double) a / b << endl;
            }
            break;
        default: cout << "Error: unknown operation" << endl;
    }
    return 0;
}
```

Analysis:

| Input | Branch chosen | Result |
|---|---|---|
| `7 * 6` | `*` | `42` |
| `7 / 2` | `/`, *b* ≠ 0 | `3.50` |
| `5 / 0` | `/`, *b* = 0 | `Error: division by zero` |
| `5 % 2` | `default` | `Error: unknown operation` |

A `switch` can contain an `if` — constructs nest inside each other. In Java a character is read with `in.next().charAt(0)`: `next()` gives the next word, `charAt(0)` its first character.

## Worked example 3: the ternary operator

An integer is given. Print whether it is even or odd, and its absolute value.

```python
n = int(input())

turi = "even" if n % 2 == 0 else "odd"
modul = n if n >= 0 else -n

print(turi, modul)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        String turi = n % 2 == 0 ? "even" : "odd";
        int modul = n >= 0 ? n : -n;

        System.out.println(turi + " " + modul);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;

    string turi = n % 2 == 0 ? "even" : "odd";
    int modul = n >= 0 ? n : -n;

    cout << turi << " " << modul << endl;
    return 0;
}
```

For the input `-7`: `-7 % 2` is 1 in Python and −1 in Java and C++; neither is 0, so `odd`; `-7 >= 0` is false, so the absolute value is `7`. The output is `odd 7`. Testing for odd with `n % 2 == 1` goes wrong on negative numbers in Java and C++ — test `n % 2 != 0` or `== 0`.

:::warning
**Common mistakes**

- **Forgetting `break` (Java, C++).** The following `case`s run too: "Wednesday Thursday Friday …".
- **A variable or a range in a `case`.** `case x > 5:` is not allowed; a `case` holds only a constant. Use `if` for ranges.
- **`switch` on a string in C++.** In C++ `switch` works only with integers and characters.
- **A `switch` without `default`.** An unexpected value is silently ignored and the bug goes unnoticed.
- **Nested ternary operators.** Three or four `?` on one line make unreadable code.
:::

:::tip
Make it a habit to end every `case` with `break`; if a fall-through is intended, say so in a comment: `// falls through on purpose`.
:::

## Summary

- `switch` (Java, C++) and `match` (Python) compare one value with several exact constants.
- In Java and C++ execution falls through into the next `case` without `break`; Python has no fall-through.
- `default` / `case _` handles every other case.
- The ternary operator `condition ? a : b` (Python: `a if condition else b`) is a short choice that returns a value.
- `if` for ranges and complex conditions, `switch` for exact values, the ternary operator for one of two values.

## Check yourself

:::check
Q: In Java, what happens if `case 2:` in a `switch` has no `break` and the value is 2?
A: The action of `case 2` runs, then the action of `case 3` (and the following ones) too — up to the first `break` or the end of the `switch`.
Q: Which Python form plays the role of `default`?
A: `case _:`.
Q: Can a range such as `ball >= 86` be tested with a `switch`?
A: Not with a classic `switch`: a `case` holds only an exact constant. Ranges use `if-else if`.
Q: How do you write `mx = a > b ? a : b;` in Python?
A: `mx = a if a > b else b`.
Q: Why is it risky to test for odd with `n % 2 == 1` in Java?
A: For negative odd numbers `n % 2` is −1, so the condition comes out false. Test `n % 2 != 0` instead.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#match-statements
Python: the match statement
The `match` and `case` section of the official tutorial.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/switch.html
Oracle Java Tutorial: the switch statement
About `switch`, `case`, `break` and `default`.
:::
