---
title: Organising recursive processes
summary: Independent work: the order of designing a recursive function — the base case, shrinking the problem, combining the result; a table of calls; the sample problem — writing a number's digits in reverse and counting them; the assignment — the Towers of Hanoi.
---

## Why it matters

Recursion solves a problem through **a smaller copy of itself**. Some problems (the Towers of Hanoi, walking a tree, enumerating all variants) are very hard to write with loops, yet take a few lines with recursion. But to be sure a recursive function works correctly, you must design it **systematically** and trace its calls. In this independent work you practise that skill.

:::note
**After this independent work you will be able to:**

- design a recursive function in three steps: the base case, shrinking, combining the result;
- build a table (or tree) of recursive calls;
- estimate the recursion depth and the number of calls;
- write a recursive solution in three languages and pass it through tests.
:::

## A short review of the topic

The order of designing a recursive function:

1. **The base case** — the smallest case answered without recursion (`n == 0`, an empty string, one disk).
2. **Shrinking** — reducing the problem to one or more **smaller** problems of the same kind (`n // 10`, `n - 1`).
3. **Combining** — getting the answer to the big problem from the answers to the small ones.

| Concept | Meaning |
|---|---|
| Recursion depth | the largest number of calls on the stack at the same time |
| Number of calls | how many times the function was called in total |
| Order of output | printing **before** the recursive call gives one order, **after** it the reverse order |

In Python the recursion depth is limited to ~1000 by default; in Java and C++ the limit depends on the stack size (usually several thousand to tens of thousands of calls).

## The sample problem: writing digits in reverse

*n* is given (0 ≤ *n* ≤ 10¹⁸). Write two recursive functions: `teskari(n)` (reverse) prints the digits of *n* starting from the last, on one line (`1200` → `0021`), and `raqamlar_soni(n)` (number of digits) returns how many digits it has. Print both results on separate lines.

```python
def teskari(n):
    print(n % 10, end="")           # the last digit first
    if n >= 10:
        teskari(n // 10)            # then the rest


def raqamlar_soni(n):
    if n < 10:                      # the base case: one digit
        return 1
    return 1 + raqamlar_soni(n // 10)


n = int(input())
teskari(n)
print()
print(raqamlar_soni(n))
```
```java
import java.util.Scanner;

public class Main {
    static void teskari(long n) {
        System.out.print(n % 10);   // the last digit first
        if (n >= 10) teskari(n / 10);   // then the rest
    }

    static int raqamlarSoni(long n) {
        if (n < 10) return 1;       // the base case: one digit
        return 1 + raqamlarSoni(n / 10);
    }

    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        teskari(n);
        System.out.println();
        System.out.println(raqamlarSoni(n));
    }
}
```
```cpp
#include <iostream>
using namespace std;

void teskari(long long n) {
    cout << n % 10;                 // the last digit first
    if (n >= 10) teskari(n / 10);   // then the rest
}

int raqamlarSoni(long long n) {
    if (n < 10) return 1;           // the base case: one digit
    return 1 + raqamlarSoni(n / 10);
}

int main() {
    long long n;
    cin >> n;
    teskari(n);
    cout << endl;
    cout << raqamlarSoni(n) << endl;
    return 0;
}
```

The table of calls — the independent work requires exactly such a table (for `raqamlar_soni(1200)`):

| Call | Base case? | Returns |
|---|---|---|
| `raqamlar_soni(1200)` | no | 1 + `raqamlar_soni(120)` = 4 |
| `raqamlar_soni(120)` | no | 1 + `raqamlar_soni(12)` = 3 |
| `raqamlar_soni(12)` | no | 1 + `raqamlar_soni(1)` = 2 |
| `raqamlar_soni(1)` | yes | 1 |

Tests: `1200` → `0021` and `4`; `7` → `7` and `1`; `0` → `0` and `1`. In `teskari` the printing comes **before** the recursive call, which is why the last digit comes out first. If you move the `print` after the call, the digits come out in their original order — a useful experiment too. The case *n* = 0 must be checked separately: "the number of digits of 0" is 1, not 0.

## The independent work assignment

Solve the assignment below: **the Towers of Hanoi** — move *n* disks from peg 1 to peg 3: one disk at a time, and a larger disk never goes on top of a smaller one. The recursive idea: *n* − 1 disks to the spare peg, the largest to the target, then *n* − 1 disks on top of it. What to hand in:

1. **A recursive description** — the function's parameters (number of disks, from, to, spare), the base case and the recursive step, in words.
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A tree of calls** — for *n* = 3: every call and the move it makes.
4. **A conclusion** — 3–5 sentences: why the number of moves equals 2ⁿ − 1, and how long it would take for *n* = 64 at one move per second.

:::warning
**Grading criteria and common mistakes**

- The solution must be recursive; a program with the list of moves written by hand or computed only by a formula is not accepted.
- The number of moves is printed on the first line — it must be known **before** the moves are printed (by the formula, or by collecting the list first).
- Do not mix up the pegs in the recursive calls: the *n* − 1 disks first go to the **spare** peg.
- The base case is *n* = 0 (nothing is done) or *n* = 1 (one move); make one of them explicit.
:::

:::tip
First carry out the moves for *n* = 1, 2, 3 by hand with three coins. If the sequence you found by hand matches what the program prints, you can trust it for large *n* too.
:::

## Summary

- A recursive function: base case → shrinking → combining the result.
- A table or tree of calls shows how the recursion works and helps find mistakes.
- Printing before the recursive call gives one order, printing after it the reverse.
- Edge cases (*n* = 0, a single element) need special attention in recursion.

## Check yourself

:::check
Q: What are the three steps of designing a recursive function?
A: Finding the base case, reducing the problem to a smaller one of the same kind, and combining the small answers into the big one.
Q: How many times is `raqamlar_soni(5000)` called (counting the first call)?
A: 4 times: 5000, 500, 50, 5.
Q: If in `teskari` the `print` comes after the recursive call, what is printed for `123`?
A: `123` — the digits in their original order.
Q: How many moves are needed for 4 disks in the Towers of Hanoi?
A: 2⁴ − 1 = 15.
:::

:::link https://en.wikipedia.org/wiki/Tower_of_Hanoi
The Tower of Hanoi
The puzzle's history, the recursive solution and the number of moves.
:::
