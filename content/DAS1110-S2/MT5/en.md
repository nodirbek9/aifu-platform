---
title: Organising nested recursive processes
summary: Independent work: nested recursion — a call to the same function inside a call's argument; counting calls, recursion depth and fast growth; the sample problem — McCarthy's 91 function; the assignment — the Ackermann function.
---

## Why it matters

In ordinary recursion a function calls itself with a smaller argument: *f*(*n* − 1). In **nested recursion** the argument itself is also a call to the same function: *f*(*f*(*n* + 11)). Such functions are very short to write, but their number of calls and their result are hard to predict — they are studied in the theory of computation to answer the question "how powerful is recursion?". In this independent work you practise tracing nested recursion and analysing its behaviour.

:::note
**After this independent work you will be able to:**

- tell nested recursion apart from ordinary recursion;
- trace a sequence of nested calls step by step;
- measure the number of calls with a global counter;
- set up a program correctly, taking the recursion depth limit into account.
:::

## A short review of the topic

| Kind of recursion | Shape | Example |
|---|---|---|
| Linear | one call | *n*! = *n* · (*n* − 1)! |
| Tree-like | several independent calls | *F*(*n*) = *F*(*n* − 1) + *F*(*n* − 2) |
| Nested | a call's result is again an argument | *M*(*n*) = *M*(*M*(*n* + 11)) |

A nested call is evaluated **from the inside out**: first the inner *M*(*n* + 11) is computed completely, and its result becomes the argument of the outer *M*. Proving that such a recursion stops is harder than for ordinary recursion: the argument does not always decrease (*n* + 11 is larger!).

To count the calls, a **counter** is declared outside the function and increased on the function's first line: `global soni` in Python, `static long soni` in Java, a global variable in C++.

## The sample problem: McCarthy's 91 function

A function proposed by John McCarthy (who coined the term "artificial intelligence"):

```text
M(n) = n − 10,          if n > 100
M(n) = M(M(n + 11)),    otherwise
```

*n* is given (0 ≤ *n* ≤ 200). Print *M*(*n*) and how many times the function was called in total.

```python
import sys

sys.setrecursionlimit(10000)
soni = 0


def m(n):
    global soni
    soni += 1
    if n > 100:
        return n - 10
    return m(m(n + 11))             # a nested call


n = int(input())
natija = m(n)
print(natija)
print(soni)
```
```java
import java.util.Scanner;

public class Main {
    static long soni = 0;

    static int m(int n) {
        soni++;
        if (n > 100) return n - 10;
        return m(m(n + 11));        // a nested call
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int natija = m(n);
        System.out.println(natija);
        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long soni = 0;

int m(int n) {
    soni++;
    if (n > 100) return n - 10;
    return m(m(n + 11));            // a nested call
}

int main() {
    int n;
    cin >> n;
    int natija = m(n);
    cout << natija << endl;
    cout << soni << endl;
    return 0;
}
```

The sequence of calls — the independent work requires exactly such a table (for *M*(99)):

| Step | Call | What happens |
|---|---|---|
| 1 | *M*(99) | 99 ≤ 100 → *M*(*M*(110)) |
| 2 | *M*(110) | 110 > 100 → 100 |
| 3 | *M*(100) | 100 ≤ 100 → *M*(*M*(111)) |
| 4 | *M*(111) | 111 > 100 → 101 |
| 5 | *M*(101) | 101 > 100 → **91** |

Tests:

| Input | *M*(*n*) | Number of calls |
|---|---|---|
| 99 | 91 | 5 |
| 87 | 91 | 29 |
| 150 | 140 | 1 |
| 0 | 91 | 203 |

A remarkable property: for **any** *n* below 101 the result is always 91 — hence the function's name. This is not visible in advance and can be understood only by tracing the calls or with a mathematical proof (*soni* is the count, *natija* the result). In Python `setrecursionlimit` raises the depth limit: for *n* = 0 the recursion goes more than 100 levels deep.

## The independent work assignment

Solve the assignment below: **the Ackermann function**:

```text
A(0, n) = n + 1
A(m, 0) = A(m − 1, 1),               m > 0
A(m, n) = A(m − 1, A(m, n − 1)),     m > 0, n > 0
```

*m* and *n* are given (0 ≤ *m* ≤ 3, 0 ≤ *n* ≤ 5); print *A*(*m*, *n*). What to hand in:

1. **A table of calls** — the whole sequence of calls for *A*(1, 2) (as in the sample above).
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of experiments** — the results for *m* = 0, 1, 2, 3 and *n* = 0…3, and the number of calls (measured with a counter).
4. **A conclusion** — 3–5 sentences: how the result and the number of calls grow as *m* increases; why *A*(4, 2) cannot be computed.

:::warning
**Grading criteria and common mistakes**

- The function must check the three cases of the definition in exactly this order: first *m* = 0, then *n* = 0.
- In Python raise `sys.setrecursionlimit`: for *A*(3, 5) the depth reaches hundreds of levels.
- The result can be large, but within the given bounds it fits in an `int` — check this in your conclusion.
- The numbers of calls in the table of experiments are measured by the program, not guessed.
:::

:::tip
First compute a few values of *A*(0, *n*), then *A*(1, *n*) and *A*(2, *n*), and look for a pattern (for example, *A*(1, *n*) = *n* + 2). The formulas you find help check the program's results.
:::

## Summary

- In nested recursion a call's result becomes an argument of the same function again; evaluation goes from the inside out.
- The result and the number of calls of such functions are not visible in advance — they are studied with a tracing table and a counter.
- To measure the number of calls, a counter outside the function is increased on every call.
- With deep recursion, mind the language limits: `setrecursionlimit` in Python, the stack size in Java and C++.

## Check yourself

:::check
Q: How does nested recursion differ from tree-like recursion?
A: In tree-like recursion the calls are independent and their results are added; in nested recursion one call's result is another call's argument.
Q: In *M*(*M*(110)), which call runs first?
A: The inner *M*(110); its result (100) is given to the outer *M*.
Q: What is *M*(50)?
A: 91 — for any *n* below 101, *M*(*n*) = 91.
Q: Why is the call counter declared outside the function rather than inside it?
A: A variable inside the function is created anew on every call; a total accumulates only in an outer (global or static) variable.
:::

:::link https://en.wikipedia.org/wiki/McCarthy_91_function
The McCarthy 91 function
The function's history and the proof that the result is always 91.
:::
