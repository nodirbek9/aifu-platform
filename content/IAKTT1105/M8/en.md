---
title: Fundamentals of algorithms and programming
summary: The concept and properties of an algorithm — discreteness, definiteness, understandability, effectiveness, generality; ways to represent an algorithm — in words, flowcharts, pseudocode, a program; the basic structures — linear, branching, looping; variables, data types, operations; programming languages and their uses in the economy (Python, VBA, SQL, R, Java, C++); the stages of creating a program; worked examples — the break-even point (linear and branching) and monthly sales analysis (looping).
---

## Why does this topic matter?

Every calculation — payroll, tax, a loan, a discount, a report — is a precise sequence of steps, that is, an **algorithm**. Algorithmic thinking lets an economist break a complex problem into stages, find errors and automate repetitive work (Excel macros, Python scripts). Today many finance and analytics job openings require knowledge of Python or SQL.

:::note
**After this lesson you will be able to:**

- state the concept and properties of an algorithm;
- represent an algorithm as a flowchart and in pseudocode;
- distinguish linear, branching and looping structures;
- know the concepts of a variable, a data type and an operation;
- carry out the stages of turning a simple economic problem into a program.
:::

## The algorithm and its properties

An **algorithm** is a precise, finite sequence of actions performed to solve a given problem. The word comes from the name of Muhammad ibn Musa al-Khwarizmi.

| Property | Meaning |
|---|---|
| **Discreteness** | consists of separate steps |
| **Definiteness** | every step is unambiguous |
| **Understandability** | the executor (a person or a computer) can perform it |
| **Effectiveness** | gives a result after a finite number of steps |
| **Generality** | works for a whole class of similar problems (with different input data) |

## Ways of representing an algorithm

- **In words**: "Divide the amount by 12, round the result…"
- **A flowchart** (in detail in S8 and MT11): an oval — start/end, a parallelogram — input/output, a rectangle — an action, a diamond — a condition, arrows — the order.
- **Pseudocode**: close to a program but written freely.
- **A program**: a precise notation in a programming language.

## Basic structures

| Structure | Meaning | Economic example |
|---|---|---|
| **Linear** | actions one after another, once | calculating a price with VAT |
| **Branching** | different paths depending on a condition | is there a profit or a loss; is there a tax relief |
| **Looping** | actions repeated several times | a calculation for every month, the pay of every employee on a list |

A **variable** is a named memory location (`narx` — price, `miqdor` — quantity); **data types**: integer, real number, text, logical; **operations**: arithmetic (`+ − * /`), comparison (`> < =`), logical (AND, OR, NOT).

## Programming languages in the economy

| Language | Use |
|---|---|
| **Python** | data analysis (pandas), automation, machine learning, financial models |
| **VBA** | macros in Excel and Office |
| **SQL** | reports from databases (M7) |
| **R** | statistics and econometrics |
| **Java, C#** | banking and enterprise systems |
| **C++** | high-speed trading systems, computing |
| **JavaScript** | web applications, online shops |

**The stages of creating a program**: stating the problem → a mathematical model → an algorithm (flowchart) → writing the program → testing (on test data) → fixing errors → use and documentation.

## Worked example 1: the break-even point

A company has a monthly fixed cost *F* (rent, salaries), the product price is *p*, the variable cost per unit is *v*. Each unit gives a margin of *p − v*; to break even, at least ⌈*F* / (*p − v*)⌉ units must be sold. If *p ≤ v*, sales do not cover the loss. Each line holds *F p v*.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    doimiy, narx, ozgaruvchan = map(int, satr.split())
    marja = narx - ozgaruvchan                     # the amount left from one unit
    if marja <= 0:                                  # branching: a condition
        print("loss not covered: price too low")
    else:
        miqdor = (doimiy + marja - 1) // marja      # division rounded up
        print(f"break-even point: {miqdor} units, revenue {miqdor * narx}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long doimiy = in.nextLong(), narx = in.nextLong(), ozgaruvchan = in.nextLong();
            long marja = narx - ozgaruvchan;                   // the amount left from one unit
            if (marja <= 0) {                                   // branching: a condition
                System.out.println("loss not covered: price too low");
            } else {
                long miqdor = (doimiy + marja - 1) / marja;     // division rounded up
                System.out.println("break-even point: " + miqdor + " units, revenue " + miqdor * narx);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long doimiy, narx, ozgaruvchan;
    while (cin >> doimiy >> narx >> ozgaruvchan) {
        long long marja = narx - ozgaruvchan;                  // the amount left from one unit
        if (marja <= 0) {                                       // branching: a condition
            cout << "loss not covered: price too low" << endl;
        } else {
            long long miqdor = (doimiy + marja - 1) / marja;    // division rounded up
            cout << "break-even point: " << miqdor << " units, revenue " << miqdor * narx << endl;
        }
    }
    return 0;
}
```

Input:

```text
12000000 50000 30000
5000000 15000 15000
9100000 40000 25000
```

Output:

```text
break-even point: 600 units, revenue 30000000
loss not covered: price too low
break-even point: 607 units, revenue 24280000
```

In the third case 9,100,000 / 15,000 = 606.67 — 606 units are not enough, so the result is rounded **up**: the formula (*F* + *m* − 1) div *m* gives exactly this with integers. In the second case the price equals the variable cost — however much is sold, the fixed cost is never covered.

## Worked example 2: monthly sales analysis

The first number is the number of months *n*, followed by *n* monthly sales figures (million soums). Print the total, the average, the best month and the number of months above average.

```python
import sys

t = list(map(int, sys.stdin.read().split()))
n, savdo = t[0], t[1:]
jami, eng = 0, 0
for i in range(n):                               # loop: every month
    jami += savdo[i]
    if savdo[i] > savdo[eng]:
        eng = i
ortacha = jami / n
yuqori = 0
for x in savdo:                                  # second loop: comparing with the average
    if x > ortacha:
        yuqori += 1
print(f"total: {jami}")
print(f"average: {ortacha:.1f}")
print(f"best month: {eng + 1} ({savdo[eng]})")
print(f"months above average: {yuqori}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] savdo = new int[n];
        for (int i = 0; i < n; i++) savdo[i] = in.nextInt();
        long jami = 0;
        int eng = 0;
        for (int i = 0; i < n; i++) {                    // loop: every month
            jami += savdo[i];
            if (savdo[i] > savdo[eng]) eng = i;
        }
        double ortacha = (double) jami / n;
        int yuqori = 0;
        for (int x : savdo) {                             // second loop: comparing with the average
            if (x > ortacha) yuqori++;
        }
        System.out.println("total: " + jami);
        System.out.println(String.format("average: %.1f", ortacha));
        System.out.println("best month: " + (eng + 1) + " (" + savdo[eng] + ")");
        System.out.println("months above average: " + yuqori);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> savdo(n);
    for (int i = 0; i < n; i++) cin >> savdo[i];
    long long jami = 0;
    int eng = 0;
    for (int i = 0; i < n; i++) {                        // loop: every month
        jami += savdo[i];
        if (savdo[i] > savdo[eng]) eng = i;
    }
    double ortacha = (double) jami / n;
    int yuqori = 0;
    for (int x : savdo) {                                // second loop: comparing with the average
        if (x > ortacha) yuqori++;
    }
    printf("total: %lld\n", jami);
    printf("average: %.1f\n", ortacha);
    printf("best month: %d (%d)\n", eng + 1, savdo[eng]);
    printf("months above average: %d\n", yuqori);
    return 0;
}
```

Input:

```text
6 120 135 98 160 142 110
```

Output:

```text
total: 765
average: 127.5
best month: 4 (160)
months above average: 3
```

Two loops are needed: to know the average, everything must first be summed, and then each month compared with it. In Excel the same result comes from `SUM`, `AVERAGE`, `MAX`, `MATCH` and `COUNTIF` (M4) — while a program automatically repeats the same analysis for thousands of files and reports.

:::warning
**Common mistakes**

- **Choosing the wrong rounding direction.** For break-even the number of units is rounded up, a discount usually down — read the problem statement.
- **Not testing edge cases.** Test zero, negative, very large values and the "exactly equal" condition.
- **Mixing integers and real numbers.** In some languages `7 / 2 = 3` (integer division).
- **Real numbers for money.** To avoid rounding errors, store money as an integer in tiyins or soums.
- **Writing a complex program without a flowchart.** Draw the algorithm first, then code it.
:::

:::tip
Run the worked examples in the platform's code editor and change the input: make the price lower than the variable cost, add a new month to the sales. Draw a flowchart for each example.
:::

## Summary

- An algorithm is a precise, finite sequence of actions; its properties: discreteness, definiteness, understandability, effectiveness, generality.
- Representation: words, flowcharts, pseudocode, programs.
- Structures: linear, branching (a condition), looping (a cycle).
- Languages in the economy: Python, VBA, SQL, R, Java, C++, JavaScript.
- Stages: problem → model → algorithm → program → testing → fixing → use.

## Check yourself

:::check
Q: What does the effectiveness property of an algorithm mean?
A: The algorithm gives a result after a finite number of steps.
Q: Which shape marks a condition in a flowchart?
A: A diamond.
Q: Calculating the pay of all employees on a list is an example of which structure?
A: Looping (a cycle).
Q: Why is the number of units at the break-even point rounded up?
A: A fraction of a unit cannot be sold, and rounding down would not fully cover the fixed cost.
:::

:::link https://docs.python.org/3/tutorial/
The Python Tutorial
The official beginner's guide to the Python programming language.
:::
