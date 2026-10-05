---
title: The concept of an algorithm and its properties
summary: Independent study — the concept and history of the algorithm (al-Khwarizmi); the properties of an algorithm — discreteness, definiteness, effectiveness, generality, comprehensibility; ways of representing it (verbal, flowchart, pseudocode, program); the basic structures (sequence, branching, loop); algorithm efficiency and complexity (O(1), O(log n), O(n), O(n²)); a program comparing linear and binary search; the link to database indexes; assignments.
---

## Purpose of this independent study

In M8 we saw the basics of algorithms and programming. In this independent study we look more deeply at the concept of an algorithm itself: which instructions count as an algorithm, how it can be represented, and which of two algorithms solving the same problem is better.

:::note
**After this independent study you will be able to:**

- state the definition of an algorithm and its five properties;
- represent an algorithm in different ways;
- distinguish the three basic structures;
- assess an algorithm's efficiency through the number of comparisons.
:::

## The concept and its history

An **algorithm** is a precise and understandable sequence of a finite number of steps given to an executor to solve a given problem. The executor may be a person, a computer, a robot or an ATM.

The word comes from the name of our great compatriot of the 9th century, **Muhammad ibn Musa al-Khwarizmi**: his treatise on calculation with Indian numerals was translated into Latin as "Algoritmi de numero Indorum". His treatise on "al-jabr" gave rise to the word "algebra".

## Properties of an algorithm

| Property | Meaning | Example of a violation |
|---|---|---|
| **Discreteness** | the algorithm is divided into separate steps executed one after another | "Prepare the report" — one big, undivided command |
| **Definiteness** (determinism) | every step is unambiguous; the same input gives the same result | "Raise the price a little" — how much is "a little"? |
| **Effectiveness** (finiteness) | it gives a result after a finite number of steps | a loop with no stopping condition |
| **Generality** | it works for a whole class of problems of one type, for different input data | a calculation written for one specific customer only |
| **Comprehensibility** | it consists of commands the executor can perform | telling a computer "Sense the customer's mood" |

## Ways of representing an algorithm

| Way | Advantage | Drawback |
|---|---|---|
| **Verbal** | everyone understands it | long, prone to ambiguity |
| **Flowchart** (S8, MT11) | the structure is visible | inconvenient for large algorithms |
| **Pseudocode** | short, close to a program, language-independent | no strict standard |
| **Program** | a computer executes it | requires knowing a programming language (MT12) |

**One algorithm in three forms** — checking a product's stock in a warehouse:

```text
Verbal:     If the stock is below the minimum reserve, place an order with the supplier,
            otherwise do nothing.

Pseudocode: IF stock < min_reserve THEN
                place_order(min_reserve * 2 - stock)
            END IF

Program:    if stock < min_reserve:
                place_order(min_reserve * 2 - stock)
```

## The basic structures

Any algorithm can be built from three structures (the Böhm–Jacopini theorem):

- **Sequence** — steps one after another: read the data → calculate → output.
- **Branching** (condition) — a different path depending on a condition (MT13, S8).
- **Loop** — steps are repeated until a condition is met or a set number of times (S15).

## Algorithm efficiency

The same problem can be solved by different algorithms, but the time they take differs sharply. Efficiency is assessed by the number of steps relative to the input size **n**:

| Complexity | Name | n = 1,000 | n = 1,000,000 | Example |
|---|---|---|---|---|
| **O(1)** | constant | 1 | 1 | taking an element of a table by its number |
| **O(log n)** | logarithmic | ~10 | ~20 | binary search in a sorted list |
| **O(n)** | linear | 1,000 | 1,000,000 | going through a list from start to end |
| **O(n²)** | quadratic | 1,000,000 | 10¹² | comparing every customer with every other |

The idea of **binary search** is searching a phone book: you open the middle; if the name you need comes earlier, you move to the left half, if later, to the right half. At each step the search area halves. The condition: the list must be **sorted**.

## Worked example: linear and binary search

The first line holds a sorted list of prices, the second the prices to look for. For each, output its position (or that it was not found) and the number of comparisons in linear and binary search.

```python
import sys

def chiziqli(roy, x):
    qadam = 0
    for i, v in enumerate(roy):
        qadam += 1
        if v == x:
            return i, qadam
    return -1, qadam

def ikkilik(roy, x):
    past, yuqori, qadam = 0, len(roy) - 1, 0
    while past <= yuqori:
        orta = (past + yuqori) // 2
        qadam += 1
        if roy[orta] == x:
            return orta, qadam
        if roy[orta] < x:
            past = orta + 1                 # search the right half
        else:
            yuqori = orta - 1               # search the left half
    return -1, qadam

satrlar = sys.stdin.read().strip().split("\n")
narxlar = [int(v) for v in satrlar[0].split()]
for x in [int(v) for v in satrlar[1].split()]:
    joy, a = chiziqli(narxlar, x)
    _, b = ikkilik(narxlar, x)
    natija = f"position {joy + 1}" if joy >= 0 else "not found"
    print(f"{x}: {natija}, linear {a}, binary {b} comparisons")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static int[] chiziqli(int[] roy, int x) {
        int qadam = 0;
        for (int i = 0; i < roy.length; i++) {
            qadam++;
            if (roy[i] == x) return new int[]{i, qadam};
        }
        return new int[]{-1, qadam};
    }

    static int[] ikkilik(int[] roy, int x) {
        int past = 0, yuqori = roy.length - 1, qadam = 0;
        while (past <= yuqori) {
            int orta = (past + yuqori) / 2;
            qadam++;
            if (roy[orta] == x) return new int[]{orta, qadam};
            if (roy[orta] < x) past = orta + 1;          // search the right half
            else yuqori = orta - 1;                      // search the left half
        }
        return new int[]{-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] narxlar = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int[] soralgan = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        for (int x : soralgan) {
            int[] a = chiziqli(narxlar, x);
            int[] b = ikkilik(narxlar, x);
            String natija = a[0] >= 0 ? "position " + (a[0] + 1) : "not found";
            System.out.println(x + ": " + natija + ", linear " + a[1] + ", binary " + b[1] + " comparisons");
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <utility>
#include <vector>
using namespace std;

pair<int, int> chiziqli(const vector<int>& roy, int x) {
    int qadam = 0;
    for (int i = 0; i < (int) roy.size(); i++) {
        qadam++;
        if (roy[i] == x) return {i, qadam};
    }
    return {-1, qadam};
}

pair<int, int> ikkilik(const vector<int>& roy, int x) {
    int past = 0, yuqori = roy.size() - 1, qadam = 0;
    while (past <= yuqori) {
        int orta = (past + yuqori) / 2;
        qadam++;
        if (roy[orta] == x) return {orta, qadam};
        if (roy[orta] < x) past = orta + 1;              // search the right half
        else yuqori = orta - 1;                          // search the left half
    }
    return {-1, qadam};
}

vector<int> oqi(const string& satr) {
    istringstream ss(satr);
    vector<int> v;
    int x;
    while (ss >> x) v.push_back(x);
    return v;
}

int main() {
    string a, b;
    getline(cin, a);
    getline(cin, b);
    vector<int> narxlar = oqi(a);
    for (int x : oqi(b)) {
        auto c = chiziqli(narxlar, x);
        auto d = ikkilik(narxlar, x);
        string natija = c.first >= 0 ? "position " + to_string(c.first + 1) : "not found";
        cout << x << ": " << natija << ", linear " << c.second << ", binary " << d.second << " comparisons\n";
    }
    return 0;
}
```

Input:

```text
1200 1850 2300 3100 4500 5200 6800 7400 8900 9600 10500 12000 13700 15000 18200
1200 9600 18200 5000
```

Output:

```text
1200: position 1, linear 1, binary 4 comparisons
9600: position 10, linear 10, binary 3 comparisons
18200: position 15, linear 15, binary 4 comparisons
5000: not found, linear 15, binary 4 comparisons
```

With 15 elements the difference is small, but in a database of 1,000,000 customers linear search makes 1,000,000 comparisons in the worst case, while binary search makes only ~20. Database **indexes** (MT8) are based on the same idea: that is exactly why a customer can be found by tax ID in a fraction of a millisecond.

:::warning
**Common mistakes**

- **Vague steps** ("approximately", "if necessary") — that is not an algorithm.
- **Forgetting the stopping condition.** The loop runs forever — effectiveness is violated.
- **Binary search in an unsorted list.** It gives a wrong result.
- **Testing efficiency only on small data.** An algorithm that is fast on 100 records may run for hours on a million.
:::

## Assignments

1. Write the algorithm for withdrawing cash from an ATM in verbal form and as pseudocode; show how each property is satisfied.
2. Find and fix the violations of the properties in a "making tea" instruction.
3. Search for 7400 and 100 in the example program. Check the result by hand.
4. At most how many comparisons does binary search make in a list of 1,000,000 elements? (Hint: 2²⁰ ≈ 1,000,000.)

## Summary

- An algorithm is a precise, finite sequence of steps; its name comes from al-Khwarizmi.
- Properties: discreteness, definiteness, effectiveness, generality, comprehensibility.
- Representation: verbal, flowchart, pseudocode, program; structures: sequence, branching, loop.
- Efficiency: O(1), O(log n), O(n), O(n²); binary search is very fast in a sorted list.

## Check yourself

:::check
Q: What does the effectiveness property mean?
A: The algorithm is guaranteed to give a result after a finite number of steps.
Q: Give an example of the generality property.
A: A payroll algorithm works for any employee and any amount.
Q: What is the condition for binary search?
A: The list must be sorted.
Q: When is O(n²) complexity dangerous?
A: When the data volume is large: if n grows 1,000 times, the time grows 1,000,000 times.
:::

:::link https://www.khanacademy.org/computing/computer-science/algorithms
Khan Academy: Algorithms
Interactive lessons on searching, sorting and complexity.
:::
