---
title: Doing independent work and homework
summary: Independent work: the order of doing an algorithmic assignment — analysing the statement, edge cases, a slow and a fast solution; stress testing — comparing a fast solution with a reliable one on random tests; a reproducible random number generator; writing a report.
---

## Why it matters

In an algorithmic assignment most of the time goes not into writing code but into **finding the bug**. The program passes the sample test but gets "wrong answer" in the automatic check — on which input? Professional programmers and olympiad contestants use **stress testing** for this: a fast (but possibly wrong) solution is compared with a slow but certainly correct one on thousands of random inputs. The first difference is a small example that exposes the bug.

:::note
**After this independent work you will be able to:**

- do an algorithmic assignment step by step: analysis, a slow solution, a fast solution, checking;
- write a stress-testing program and find an input that exposes a bug;
- use a random number generator that gives the same result every time;
- write a short report on the work done.
:::

## The order of doing an assignment

1. **Analyse the statement** — input, output, constraints. Derive the required complexity from the constraints: if *n* ≤ 10⁵, *O*(*n*²) will not do.
2. **A list of edge cases** — one element, all elements equal, the largest values, an empty result.
3. **A slow solution** — the simplest, certainly correct method (brute force, nested loops). It need not be fast.
4. **A fast solution** — an algorithm matching the constraints.
5. **Checking** — the sample tests, the edge cases and a stress test.
6. **A report** — the algorithm, its complexity, the tests and a conclusion.

## Stress testing

A stress test consists of three parts:

| Part | Its job |
|---|---|
| Generator | creates a small random input (small — so that a found bug is easy to analyse by hand) |
| Reliable solution | slow, but simple and certainly correct |
| Comparator | runs both solutions on the same input and compares the results |

For reproducibility the random numbers are computed from a **seed**: the same seed gives the same sequence of tests. Since the languages' standard generators differ, a simple **linear congruential generator** is used below:

```text
state = (state · 1103515245 + 12345) mod 2³¹
```

This formula gives exactly the same numbers in all three languages — a failing test can be recreated in another language too.

## The sample problem: catching a bug in a binary search

The task: count the elements **less than** *x* in a sorted array (the lower bound). A student wrote a binary search but got one character wrong. A stress test finds the bug: the seed and the number of tests are given; every test creates a random sorted array of length 1–8 with values 0–9 and an *x*; the reliable solution is plain counting.

```python
holat = 0


def tasodifiy():
    global holat
    holat = (holat * 1103515245 + 12345) % 2147483648   # a linear congruential generator
    return holat


def ishonchli(a, x):                        # slow but certainly correct
    return sum(1 for v in a if v < x)


def tez(a, x):                              # binary search — with a bug!
    chap, ong = 0, len(a)
    while chap < ong:
        orta = (chap + ong) // 2
        if a[orta] <= x:                    # bug: it should be <
            chap = orta + 1
        else:
            ong = orta
    return chap


holat, testlar = map(int, input().split())
for t in range(1, testlar + 1):
    n = tasodifiy() % 8 + 1
    a = sorted(tasodifiy() % 10 for _ in range(n))
    x = tasodifiy() % 10
    kutilgan, olingan = ishonchli(a, x), tez(a, x)
    if kutilgan != olingan:
        print(f"Bug found: test {t}")
        print("a =", *a)
        print("x =", x)
        print(f"expected {kutilgan}, got {olingan}")
        break
else:
    print(f"All correct: {testlar} tests")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static long holat;

    static long tasodifiy() {
        holat = (holat * 1103515245L + 12345) % 2147483648L;    // a linear congruential generator
        return holat;
    }

    static int ishonchli(int[] a, int x) {          // slow but certainly correct
        int soni = 0;
        for (int v : a) if (v < x) soni++;
        return soni;
    }

    static int tez(int[] a, int x) {                // binary search — with a bug!
        int chap = 0, ong = a.length;
        while (chap < ong) {
            int orta = (chap + ong) / 2;
            if (a[orta] <= x) chap = orta + 1;      // bug: it should be <
            else ong = orta;
        }
        return chap;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        holat = in.nextLong();
        int testlar = in.nextInt();
        for (int t = 1; t <= testlar; t++) {
            int n = (int) (tasodifiy() % 8) + 1;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = (int) (tasodifiy() % 10);
            Arrays.sort(a);
            int x = (int) (tasodifiy() % 10);
            int kutilgan = ishonchli(a, x), olingan = tez(a, x);
            if (kutilgan != olingan) {
                System.out.println("Bug found: test " + t);
                StringBuilder sb = new StringBuilder("a =");
                for (int v : a) sb.append(' ').append(v);
                System.out.println(sb);
                System.out.println("x = " + x);
                System.out.println("expected " + kutilgan + ", got " + olingan);
                return;
            }
        }
        System.out.println("All correct: " + testlar + " tests");
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

long long holat;

long long tasodifiy() {
    holat = (holat * 1103515245LL + 12345) % 2147483648LL;     // a linear congruential generator
    return holat;
}

int ishonchli(const vector<int>& a, int x) {        // slow but certainly correct
    int soni = 0;
    for (int v : a) if (v < x) soni++;
    return soni;
}

int tez(const vector<int>& a, int x) {              // binary search — with a bug!
    int chap = 0, ong = a.size();
    while (chap < ong) {
        int orta = (chap + ong) / 2;
        if (a[orta] <= x) chap = orta + 1;          // bug: it should be <
        else ong = orta;
    }
    return chap;
}

int main() {
    int testlar;
    cin >> holat >> testlar;
    for (int t = 1; t <= testlar; t++) {
        int n = tasodifiy() % 8 + 1;
        vector<int> a(n);
        for (int& v : a) v = tasodifiy() % 10;
        sort(a.begin(), a.end());
        int x = tasodifiy() % 10;
        int kutilgan = ishonchli(a, x), olingan = tez(a, x);
        if (kutilgan != olingan) {
            cout << "Bug found: test " << t << endl;
            cout << "a =";
            for (int v : a) cout << ' ' << v;
            cout << endl << "x = " << x << endl;
            cout << "expected " << kutilgan << ", got " << olingan << endl;
            return 0;
        }
    }
    cout << "All correct: " << testlar << " tests" << endl;
    return 0;
}
```

For `1 100` the output is:

```text
Bug found: test 5
a = 2 4 5 6 9 9
x = 4
expected 1, got 2
```

Now the bug is easy to analyse by hand: the array contains an element equal to 4, and the buggy solution counted it as "less" too — `<=` actually computes the **upper** bound (*holat* is the generator state, *ishonchli* the reliable solution, *tez* the fast one). Once it is fixed to `<`, the stress test says `All correct`. For `42 1` (one test) no bug shows up — a program that passes a sample test is not yet correct: with `42 3` the bug is found already in test 2.

## The independent work assignment

Choose **two** of the course's practical assignments (say, heap sort and the Fenwick tree) and for them:

1. **A slow solution** — write the simplest correct method (say, the ready `sort`, or a sum with a full loop).
2. **A stress test** — compare your fast solution with the slow one on at least 1000 small random tests; write the generator's seed in the report.
3. **A bug history** — describe one bug found by the stress test (or deliberately introduced): the input, the expected and actual results, the cause and the fix.
4. **A report** — for each assignment: the idea of the algorithm, its complexity, a table of tests, the stress-test result and a 3–5 sentence conclusion.

:::warning
**Grading criteria and common mistakes**

- The slow solution must be **independent** of the fast one: writing the same idea twice repeats the same bug.
- Let the generator create small inputs: a failing test with 10⁵ elements cannot be analysed by hand.
- Random tests without a seed do not let you recreate a bug — always keep the seed.
- Writing only "it passed all tests" in the report is not enough: show which tests and how many.
:::

:::tip
Prepare the stress test right at the start of the assignment: run it as soon as you have written the fast solution. The earlier a bug is found, the easier it is to fix.
:::

## Summary

- The order of an assignment: analysis → edge cases → slow solution → fast solution → checking → report.
- A stress test compares the fast solution with a reliable one on thousands of small random inputs and finds an example that exposes a bug.
- A seeded linear congruential generator recreates the tests identically every time and in any language.
- Passing the sample test does not guarantee that a program is correct.

## Check yourself

:::check
Q: What should the "reliable solution" in a stress test be like?
A: Simple, certainly correct and independent of the fast solution; its speed does not matter.
Q: Why does the generator create small inputs?
A: So that a found bug is easy to analyse by hand.
Q: Why is a seed needed for random tests?
A: To recreate the failing test exactly.
Q: What does the buggy binary search in the sample compute?
A: The number of elements less than or equal to *x* — the upper bound.
:::

:::link https://en.wikipedia.org/wiki/Linear_congruential_generator
Linear congruential generator
A simple pseudo-random number generator and its properties.
:::
