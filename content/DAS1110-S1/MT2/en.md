---
title: Organising repetitive processes (algorithm and program)
summary: Independent work: an algorithm, a table of values, a program and tests for a computation with a loop; the sample problem is how many years a deposit takes to reach a goal.
---

## Why it matters

A loan schedule, deposit interest, a population forecast, the sum of a series — all of these rest on repeating one calculation many times. To build a loop correctly you need to know three things: where it starts, what changes at each step and when it stops. In this independent work you answer those three questions yourself and write a program that computes with a loop from start to finish.

:::note
**After this independent work you will be able to:**

- choose a fitting loop (`for` or `while`) for a problem and justify the choice;
- write down the start value, the step and the stop condition of a loop precisely;
- fill in a table of values by hand for a small input and check the program against it;
- compute with floating-point numbers and print the result to the required precision.
:::

## A quick review of the topic

For every loop, answer three questions:

| Question | In a `for` | In a `while` |
|---|---|---|
| Where does it start? | the start value of the counter | the values before the loop |
| What changes at each step? | the counter + the body | a variable inside the body |
| When does it stop? | when the counter reaches the bound | when the condition becomes false |

If the number of repetitions is known — `for`; if it is "until something happens" — `while`. An accumulator starts at 0 for a sum and at 1 for a product. When working with floating-point numbers, avoid integer division: `1.0 / k`.

## Sample problem: a deposit

*S* sum is put on deposit at an annual interest of *p* % (added at the end of each year). After how many years is the amount first at least *M* sum? Print the number of years and the amount at that moment (two decimal places). Input: *S*, *p*, *M* on one line (*S* < *M*, *p* > 0).

The number of repetitions is not known in advance — `while`. The stop condition: `summa >= M`. At each step: `summa = summa * (1 + p / 100)` and `yil += 1`. The loop is sure to end, because with *p* > 0 the amount grows every year.

```text
1. read S, p, M
2. summa = S, yil = 0
3. while summa < M:
       summa = summa * (1 + p / 100)
       yil = yil + 1
4. print yil and summa
```

```python
s, p, m = map(float, input().split())

summa = s
yil = 0
while summa < m:
    summa = summa * (1 + p / 100)
    yil += 1

print(yil, f"{summa:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double s = in.nextDouble();
        double p = in.nextDouble();
        double m = in.nextDouble();

        double summa = s;
        int yil = 0;
        while (summa < m) {
            summa = summa * (1 + p / 100);
            yil++;
        }

        System.out.println(yil + " " + String.format("%.2f", summa));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    double s, p, m;
    cin >> s >> p >> m;

    double summa = s;
    int yil = 0;
    while (summa < m) {
        summa = summa * (1 + p / 100);
        yil++;
    }

    cout << yil << " " << fixed << setprecision(2) << summa << endl;
    return 0;
}
```

The table of values for the input `1000000 20 2000000` (*yil* is the year, *summa* the amount):

| *yil* | *summa* (at the end of the year) | `summa < M`? |
|---|---|---|
| 0 | 1 000 000 | yes |
| 1 | 1 200 000 | yes |
| 2 | 1 440 000 | yes |
| 3 | 1 728 000 | yes |
| 4 | 2 073 600 | no — stop |

The output is `4 2073600.00`. Filling in such a table by hand before writing the program is a required part of the independent work.

## The independent work task

Solve the **sum of the harmonic series** problem given below. What you hand in:

1. **An analysis** — answers to the three questions: where the loop starts, what is added at each step, when it stops; why this kind of loop was chosen.
2. **A table of values** — the term and the sum at each step for *n* = 4 (by hand).
3. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
4. **An experiment** — the results for *n* = 10, 100, 1000 and a short conclusion: how does the sum change as *n* grows?

:::warning
**Assessment criteria and common mistakes**

- `1 / k` gives integer division (0) in Java and C++; for floating-point division write `1.0 / k`.
- The loop must start at 1: `1 / 0` is a division by zero.
- The result must be printed to the precision given in the statement (4 decimal places).
- The table of values is filled in by hand, not copied from the program.
:::

:::tip
Check your program first with *n* = 1 (answer 1.0000) and *n* = 2 (answer 1.5000) — they are the simplest tests and they find the most bugs.
:::

## Summary

- For every loop, write down the start, the step and the stop condition precisely.
- If the number of repetitions is unknown — `while`; if it is known — `for`.
- The table of values for a small input is built before the program is written.
- In floating-point calculations avoid integer division and print the result to the required precision.

## Check yourself

:::check
Q: Why does the sample use `while` rather than `for`?
A: How many years are needed is not known in advance — the loop goes on until the amount reaches the goal.
Q: What does the sample program do if *p* = 0?
A: The amount never changes and the loop never ends — which is why the statement requires *p* > 0.
Q: Is the sum of the harmonic series bounded as *n* grows?
A: No: it grows very slowly but without limit (about ln *n* + 0.577).
:::

:::link https://en.wikipedia.org/wiki/Harmonic_series_(mathematics)
Wikipedia: the harmonic series
The properties of the harmonic series and how fast it grows.
:::
