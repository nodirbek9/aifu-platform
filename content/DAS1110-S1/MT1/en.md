---
title: Organising branching processes (algorithm and program)
summary: Independent work: an algorithm for a problem split into ranges, a flowchart, a program and a table of tests; the sample problem is an electricity tariff.
---

## Why it matters

The tax rate depends on income, the electricity bill on consumption, a discount on the purchase total. Rules "by ranges" like these are everywhere, and writing them in a program without mistakes is a skill expected of every programmer. In this independent work you solve a range problem from start to finish on your own: the algorithm, the program and the tests.

:::note
**After this independent work you will be able to:**

- write the conditions of a range problem in the right order;
- describe an algorithm in words and as a flowchart;
- build a table of tests for every branch and boundary value;
- write the solution in Python, Java or C++ and pass the automatic tests.
:::

## A quick review of the topic

In a range problem the number line is split into several parts, and each part has its own rule. The order of solving:

1. **Write the ranges down** and decide which range each boundary belongs to (`<` or `<=`).
2. **Build a chain**: `if` … `elif` / `else if` … `else`. If you write the conditions from one side (from smallest to largest or the other way), each one only needs to check one boundary.
3. **Tests**: one value from inside each range, and values on both sides of each boundary.

| Mistake | Consequence |
|---|---|
| A boundary falls into two ranges | the result contradicts the statement |
| A boundary falls into no range | some values get no result |
| The order of the conditions is broken | a wide condition "swallows" a narrow one |

## Sample problem: an electricity tariff

A monthly consumption (kWh, an integer) is given. Compute the bill: the first 100 kWh at 450 sum each, from 101 to 300 at 900 sum, above 300 at 1350 sum.

The algorithm in words:

```text
1. read q
2. if q <= 100: tolov = q * 450
3. otherwise if q <= 300: tolov = 100 * 450 + (q - 100) * 900
4. otherwise: tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350
5. print tolov
```

In a flowchart these are two diamonds in a row: `q <= 100?` and `q <= 300?`, each with one calculation on its "yes" branch.

```python
q = int(input())

if q <= 100:
    tolov = q * 450
elif q <= 300:
    tolov = 100 * 450 + (q - 100) * 900
else:
    tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350

print(tolov)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int q = in.nextInt();

        long tolov;
        if (q <= 100) {
            tolov = q * 450L;
        } else if (q <= 300) {
            tolov = 100 * 450L + (q - 100) * 900L;
        } else {
            tolov = 100 * 450L + 200 * 900L + (q - 300) * 1350L;
        }

        System.out.println(tolov);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long q;
    cin >> q;

    long long tolov;
    if (q <= 100) {
        tolov = q * 450;
    } else if (q <= 300) {
        tolov = 100 * 450 + (q - 100) * 900;
    } else {
        tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350;
    }

    cout << tolov << endl;
    return 0;
}
```

The table of tests — the independent work requires exactly such a table (*tolov* is the bill):

| Input | Range | Expected result | Note |
|---|---|---|---|
| 0 | first | 0 | the lower bound |
| 100 | first | 45000 | a boundary: belongs to the first range |
| 101 | second | 45900 | the value just after the boundary |
| 150 | second | 90000 | inside the range |
| 300 | second | 225000 | a boundary |
| 350 | third | 292500 | inside the range |

In Java `450L` is a `long` literal: so that an `int` does not overflow for large consumption.

## The independent work task

Solve the **piecewise function** problem given below. What you hand in:

1. **The algorithm** — in words (like the sample) or a picture of a flowchart.
2. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
3. **The table of tests** — at least 6 tests: one from each range and around each boundary (*x* = 0, *x* = 10); the expected result of each worked out by hand.
4. **A conclusion** — 3–5 sentences: at which boundary is it easy to go wrong, and how did you check it.

:::warning
**Assessment criteria and common mistakes**

- The algorithm and the program must match; a program without an algorithm is not given full marks.
- The boundary values (*x* = 0 and *x* = 10) must fall into the range given in the statement: `0 ≤ x ≤ 10`.
- The expected results are worked out by hand, not copied from the program's output.
- A program copied from someone else's work is not accepted.
:::

:::tip
Fill in the table of tests first, then write the program. When the program gives every result in the table, submit it in the task window.
:::

## Summary

- In a range problem, first decide which range each boundary belongs to.
- Order the conditions from one side — then each checks one boundary.
- Tests: from inside each range and around each boundary.
- Working in the order algorithm → tests → program reduces mistakes.

## Check yourself

:::check
Q: Which tariff applies to 300 kWh in the sample?
A: The second range (`q <= 300`): 45 000 + 200 · 900 = 225 000 sum.
Q: At least how many tests does a problem with three ranges need?
A: At least 3 (one from each range), but a reliable check also adds tests around each boundary.
Q: What happens if the `elif q <= 300` condition comes before `if q <= 100`?
A: Consumption below 100 is charged at the second tariff too: the wide condition "swallows" the narrow one.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#if-statements
Python: if statements
The official tutorial on `if`, `elif`, `else`.
:::
