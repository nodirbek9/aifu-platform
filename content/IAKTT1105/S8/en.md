---
title: Graphical representation of computational processes
summary: Seminar — the concept of a flowchart and its standard symbols (ISO 5807): terminator, input/output, process, decision, loop, predefined process, connector; rules for drawing flowcharts; flowcharts of linear, branching and looping processes; moving from a flowchart to a program; a worked example — a tiered electricity tariff (flowchart and program); flowchart tools (draw.io, Visio, Word shapes); the platform's branching algorithm task.
---

## Purpose of the seminar

In lecture M8 we covered the concept and structures of algorithms. In the seminar we practise representing an algorithm **graphically** — as a flowchart. A flowchart is a common "language" for an economist, a programmer and a manager: where decisions are made in a process and what is repeated can be seen at a glance. Then we turn a flowchart into a program.

:::note
**After this seminar you will be able to:**

- know the standard flowchart symbols;
- draw flowcharts of linear, branching and looping processes;
- turn a flowchart into a program and, conversely, a program into a flowchart;
- use flowchart drawing tools.
:::

## Flowchart symbols

| Symbol | Shape | Meaning |
|---|---|---|
| **Terminator** (start / end) | a rounded rectangle (oval) | the beginning and end of the algorithm |
| **Input / output** | a parallelogram | entering data or outputting a result |
| **Process** | a rectangle | a calculation, assigning a value |
| **Decision** | a diamond | a "yes/no" question, two exits |
| **Loop start** | a hexagon (preparation) | the loop parameters |
| **Predefined process** | a rectangle with double side lines | a helper algorithm described separately |
| **Connector** | a circle | linking when a large chart is split into parts |
| **Flow lines** | arrows | the order of execution (top to bottom, left to right) |

## Drawing rules

- One start block and at least one end block.
- Each block (except a decision) has one exit; a decision has two exits clearly labelled "yes" and "no".
- Lines should not cross; use connectors if needed.
- The text inside a block is short and precise: `S ← n × p`, `n > 200?`.
- A loop must end — check that the loop condition eventually becomes false.

## The three basic structures in flowcharts

**Linear** (a price with VAT):

```text
( Start ) → / input price / → [ total ← price × 1.12 ] → / output total / → ( End )
```

**Branching** (two paths depending on a condition):

```text
            / input value /
                    |
              < condition met? >
              yes |          | no
          [ calculation 1 ] [ calculation 2 ]
                    \      /
              / output result /
```

**Looping** (for every month):

```text
[ i ← 1, total ← 0 ] → < i ≤ n ? > —no→ / output total /
                         | yes
              [ total ← total + sales(i) ]
              [ i ← i + 1 ] → (back to the loop start)
```

## Worked example: a tiered electricity tariff

A teaching example (notional prices): up to 200 kWh a month cost 600 soums each, the part above 200 costs 1,000 soums each. A tiered tariff supports low consumers and encourages saving.

The flowchart:

```text
( Start )
      |
/ input kwh /
      |
< kwh ≤ 200 ? >
 yes |                        | no
[ amount ← kwh × 600 ]   [ amount ← 200 × 600 + (kwh − 200) × 1000 ]
      \                      /
       / output amount /
              |
          ( End )
```

The program (repeated for several consumers):

```python
import sys

CHEGARA, NARX1, NARX2 = 200, 600, 1000             # a notional tariff for a teaching example

for t in sys.stdin.read().split():
    kvt = int(t)                                    # input
    if kvt <= CHEGARA:                              # condition (diamond)
        summa = kvt * NARX1
    else:
        summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2
    print(f"{kvt} kWh: {summa} soums")          # output
```
```java
import java.util.Scanner;

public class Main {
    static final long CHEGARA = 200, NARX1 = 600, NARX2 = 1000;   // a notional tariff for a teaching example

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long kvt = in.nextLong();                     // input
            long summa;
            if (kvt <= CHEGARA) {                         // condition (diamond)
                summa = kvt * NARX1;
            } else {
                summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2;
            }
            System.out.println(kvt + " kWh: " + summa + " soums");   // output
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

const long long CHEGARA = 200, NARX1 = 600, NARX2 = 1000;          // a notional tariff for a teaching example

int main() {
    long long kvt;
    while (cin >> kvt) {                                  // input
        long long summa;
        if (kvt <= CHEGARA) {                             // condition (diamond)
            summa = kvt * NARX1;
        } else {
            summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2;
        }
        cout << kvt << " kWh: " << summa << " soums" << endl;   // output
    }
    return 0;
}
```

Input:

```text
150 200 201 350 0
```

Output:

```text
150 kWh: 90000 soums
200 kWh: 120000 soums
201 kWh: 121000 soums
350 kWh: 270000 soums
0 kWh: 0 soums
```

The test values were chosen on purpose: the threshold itself (200), one more than it (201), values below and above the threshold, and zero. Boundary values are where errors hide most often in branching algorithms (writing "<" instead of "≤" makes 200 kWh calculate incorrectly).

## Tools

- **draw.io** (diagrams.net) — free, in the browser, with a library of flowchart shapes.
- **Microsoft Visio**, **Lucidchart** — professional diagrams.
- **Word/PowerPoint** shapes and SmartArt — for simple charts.
- **Mermaid** — diagrams from text (in documentation and wikis).

## Practical tasks

1. Draw the flowchart of the worked example in draw.io and extend it to a three-tier tariff (for example up to 200, 200–500, above 500).
2. Draw a flowchart for the "monthly sales analysis" program from M8 (two loops).
3. For the platform task **"Branching algorithm: purchase discount"**, first draw a flowchart (a sequence of several decision diamonds), then turn it into a program. Make up test data for the boundary values yourself.
4. "Execute" a flowchart drawn by your partner: calculate the result step by step for 3 input values and compare it with the program's result.

:::warning
**Common mistakes**

- **Drawing one exit from a decision.** A diamond always has two paths: "yes" and "no".
- **A path that never reaches the end block.** Every path must eventually come to the end.
- **Losing a boundary value.** The difference between "≥" and ">" is the most common mistake.
- **An infinite loop.** If the loop variable (i ← i + 1) is not changed, the loop never ends.
- **Long text inside a block.** Write a short formula and move the explanation to a comment.
:::

:::tip
The best way to check a flowchart is to "execute it on paper": make a table (columns — variables, rows — steps) and write down the values after each block.
:::

## Summary

- A flowchart is a graphical representation of an algorithm: oval (start/end), parallelogram (input/output), rectangle (process), diamond (decision), hexagon (loop), arrows.
- Rules: one start, two exits from a decision, no crossing lines, a loop must end.
- Linear, branching and looping structures have the same logic in a flowchart and in a program.
- Testing boundary values is the main way to find errors in branching algorithms.

## Check yourself

:::check
Q: Which shape marks a decision in a flowchart, and how many exits does it have?
A: A diamond; two exits — "yes" and "no".
Q: Which shape represents input and output?
A: A parallelogram.
Q: In the tiered tariff example, what is the amount for 201 kWh?
A: 121,000 soums (200 × 600 + 1 × 1000).
Q: Which values must be tested when testing a branching algorithm?
A: Boundary values — the threshold itself and the values just below and above it.
:::

:::link https://www.drawio.com/
draw.io (diagrams.net)
A free online tool for drawing flowcharts and diagrams.
:::
