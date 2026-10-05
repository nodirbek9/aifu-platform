---
title: General information about programming languages
summary: Independent study — the concept of a programming language; generations of languages (machine code, assembler, high-level, query languages, low-code); compilation, interpretation and JIT; paradigms (procedural, object-oriented, functional, declarative); typing (static and dynamic); languages for economists — Python, R, SQL, VBA, Java, C++, JavaScript, the 1C language; the danger of calculating money in floating-point numbers and a program showing it; development environments and artificial intelligence assistants; choosing a language.
---

## Purpose of this independent study

The examples on the platform are given in Python, Java and C++. Why these languages? How do they differ, and which language is it useful for an economist to learn? This independent study gives a general map of the world of programming languages.

:::note
**After this independent study you will be able to:**

- name the generations and types of programming languages;
- distinguish compilation and interpretation;
- explain the main paradigms with examples;
- choose a suitable language for economic tasks;
- know the danger of floating-point numbers in money calculations.
:::

## The concept and generations

A **programming language** is an artificial language with strict rules (syntax and semantics) for writing instructions for a computer to execute.

| Generation | Description | Example |
|---|---|---|
| **1GL** | machine code — zeros and ones | `10110000 01100001` |
| **2GL** | assembler — short names for machine instructions | `MOV AL, 61h` |
| **3GL** | high-level, close to human language | C, Java, Python |
| **4GL** | describing the result ("what is needed", not "how") | SQL, part of R, report generators |
| **Low-code / no-code** | visual builders, minimal code | Power Apps, Zapier, Bubble |

## How a program is executed

| Method | How it works | Advantage | Example |
|---|---|---|---|
| **Compilation** | the whole program is translated into machine code in advance | runs fast, errors are found in advance | C, C++, Go |
| **Interpretation** | the program is executed line by line | quick testing, flexibility | Python, R, JavaScript (traditionally) |
| **Bytecode + JIT** | compiled to intermediate code, sped up at run time | platform independence, good speed | Java, C# |

## Paradigms

| Paradigm | Idea | Example |
|---|---|---|
| **Procedural** | a program is a sequence of commands and functions | `calculate_tax(amount)` |
| **Object-oriented (OOP)** | data and operations are combined into objects | `account.deposit(500)` — an "Account" object |
| **Functional** | the result is a composition of functions, state does not change | `sum(map(tax, amounts))` |
| **Declarative** | writing "what" is needed, not "how" | `SELECT SUM(amount) FROM sales` |

Modern languages often support several paradigms (Python — procedural, OOP and functional).

**Typing**: **static** (Java, C++) — a variable's type is declared in advance and errors are found at compile time; **dynamic** (Python, R) — the type is determined at run time, writing is faster, but some errors appear only when the program runs.

## Languages for economists

| Language | Where | Why it is useful |
|---|---|---|
| **Python** | analysis, automation, machine learning | simple syntax, pandas, a large community (MT2, MT9) |
| **R** | statistics, econometrics, scientific research | statistical packages, high-quality charts |
| **SQL** | databases (M7, MT8) | in almost every company; the analyst's main tool |
| **VBA** | automating Excel and Office | macros, recurring reports |
| **Java, C#** | banking and corporate systems (M15) | reliability, large projects |
| **C++** | high-frequency trading, computational cores | maximum speed |
| **JavaScript** | websites, dashboards, Google Sheets scripts | runs in the browser |
| **The 1C language** | configurations of 1C:Accounting and 1C:ERP | accounting systems in the CIS and Uzbekistan |

:::tip
An effective path for an economist: **Excel → SQL → Python (or R)**. SQL gives you data retrieval, and Python analysis and automation.
:::

## Money and floating-point numbers

A computer stores `double` (`float`) numbers in binary. A simple decimal fraction such as 0.1 becomes an infinite fraction in binary and is stored approximately. As a result, small errors accumulate in money calculations — unacceptable for accounting. The correct solution: store money as **an integer in the smallest unit** (tiyins, cents) or with a special decimal type (`Decimal`, `BigDecimal`).

**Worked example**: the first line holds amounts (soums, with two digits of tiyins), the second the expected total. Calculate the sum in `double` and as an integer in tiyins, and check whether it equals the expected value.

```python
import sys

def tiyin(matn):
    som, tiyinlar = matn.split(".")
    return int(som) * 100 + int(tiyinlar)

satrlar = sys.stdin.read().strip().split("\n")
summalar = satrlar[0].split()
kutilgan = satrlar[1].strip()

haqiqiy = 0.0
for s in summalar:
    haqiqiy += float(s)                    # adding in double
butun = sum(tiyin(s) for s in summalar)    # integer in tiyins

print(f"double: sum == {kutilgan} -> {'yes' if haqiqiy == float(kutilgan) else 'no'}")
print(f"tiyin (integer): sum == {tiyin(kutilgan)} -> {'yes' if butun == tiyin(kutilgan) else 'no'}")
```
```java
import java.util.Scanner;

public class Main {
    static long tiyin(String matn) {
        String[] q = matn.split("\\.");
        return Long.parseLong(q[0]) * 100 + Long.parseLong(q[1]);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] summalar = in.nextLine().trim().split("\\s+");
        String kutilgan = in.nextLine().trim();

        double haqiqiy = 0.0;
        long butun = 0;
        for (String s : summalar) {
            haqiqiy += Double.parseDouble(s);      // adding in double
            butun += tiyin(s);                     // integer in tiyins
        }
        System.out.println("double: sum == " + kutilgan + " -> " + (haqiqiy == Double.parseDouble(kutilgan) ? "yes" : "no"));
        System.out.println("tiyin (integer): sum == " + tiyin(kutilgan) + " -> " + (butun == tiyin(kutilgan) ? "yes" : "no"));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

long long tiyin(const string& matn) {
    size_t nuqta = matn.find('.');
    return stoll(matn.substr(0, nuqta)) * 100 + stoll(matn.substr(nuqta + 1));
}

int main() {
    string satr, kutilgan, s;
    getline(cin, satr);
    getline(cin, kutilgan);
    kutilgan = kutilgan.substr(0, kutilgan.find_last_not_of(" \r") + 1);

    double haqiqiy = 0.0;
    long long butun = 0;
    istringstream ss(satr);
    while (ss >> s) {
        haqiqiy += stod(s);                        // adding in double
        butun += tiyin(s);                         // integer in tiyins
    }
    cout << "double: sum == " << kutilgan << " -> " << (haqiqiy == stod(kutilgan) ? "yes" : "no") << "\n";
    cout << "tiyin (integer): sum == " << tiyin(kutilgan) << " -> " << (butun == tiyin(kutilgan) ? "yes" : "no") << "\n";
    return 0;
}
```

Input:

```text
0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10
1.00
```

Output:

```text
double: sum == 1.00 -> no
tiyin (integer): sum == 100 -> yes
```

The sum of ten 10-tiyin amounts in `double` is 0.9999999999999999 and is not equal to 1.00! The result is the same in all three languages — this is not a language bug but the nature of binary numbers. Over a million transactions such differences lead to discrepancies in reports. That is why banking and accounting systems store money in tiyins or as `Decimal`, and in Excel the `ROUND` function is used.

## Development environments and assistants

- **IDE** (integrated development environment): Visual Studio Code, PyCharm, IntelliJ IDEA, RStudio — code editing, finding errors (a debugger), autocompletion.
- **Notebooks**: Jupyter, Google Colab — code, results and comments in one document; convenient for analysis.
- **Version control**: Git and GitHub — the history of changes, teamwork.
- **Artificial intelligence assistants** (GitHub Copilot and others) speed up writing code, but understanding and checking the result is the programmer's job (recall academic integrity in MT1).

:::warning
**Common mistakes**

- **Looking for "the best language".** A language is chosen to suit the task.
- **Storing money in `float`/`double`.** Use integers in tiyins or `Decimal`.
- **Copying code without understanding it.** It comes with bugs and security problems.
- **Trying to learn many languages at once.** First learn one in depth — the next ones become easier.
:::

## Assignment

1. Compare any example on the platform in Python, Java and C++: which parts are similar and which differ (declaring variables, reading input, output)?
2. Try the example program with 0.25 instead of 0.10. Why did the result change? (Hint: 0.25 = 1/4 is represented exactly in binary.)
3. Analyse three job advertisements (analyst, accountant, financier): which programming languages and tools are required?
4. Write a 5-line Python program that takes the prices of three products and outputs the total and the average.

## Summary

- Generations of languages: machine code → assembler → high-level → query languages → low-code.
- Execution: compilation, interpretation, bytecode and JIT; typing is static or dynamic.
- Paradigms: procedural, OOP, functional, declarative.
- For economists: SQL, Python or R, VBA; in corporate systems Java, C#, 1C.
- Storing money in floating-point numbers leads to errors — use integers in tiyins or Decimal.

## Check yourself

:::check
Q: What is the difference between compilation and interpretation?
A: In compilation the program is translated entirely into machine code in advance; in interpretation it is executed line by line.
Q: Which paradigm does SQL belong to?
A: Declarative — it describes "what" the result is, not "how" to compute it.
Q: Why is it dangerous to store money in double?
A: Decimal fractions such as 0.1 cannot be represented exactly in binary, and small errors accumulate.
Q: Which language is used to automate Excel?
A: VBA (as well as Office Scripts and Python).
:::

:::link https://docs.python.org/3/tutorial/floatingpoint.html
Python: Floating-Point Arithmetic
The official explanation of floating-point errors and their causes.
:::
