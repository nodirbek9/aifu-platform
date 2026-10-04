---
title: The complexity of algorithms. Search algorithms
summary: Time and space complexity, counting operations, the O, Ω and Θ notations, complexity classes (an idea of P and NP), the complexity of recursive algorithms and recurrence relations; linear, binary and interpolation search and their complexity.
---

## Why it matters

Two programmers solved the same problem: one program runs in 0.01 seconds on 1,000 records, the other in 0.02 seconds. Which is better? The answer depends on what happens as the number of records grows: on 1,000,000 records the first may take 3 hours and the second 0.5 seconds. That is exactly why algorithms are compared **not in seconds but by their rate of growth**.

In this lecture we see how to estimate an algorithm's complexity, how to find the complexity of recursive algorithms, and how fast the most widely used search algorithms — linear, binary and interpolation search — are.

:::note
**After this lesson you will be able to:**

- estimate time and space complexity by counting operations;
- explain the meaning of the O, Ω and Θ notations;
- tell the main complexity classes apart, and the notions of P and NP;
- find the complexity of a recursive algorithm from a simple recurrence relation;
- write linear, binary and interpolation search and compare their complexity.
:::

## Time and space complexity

**Time complexity** is the number of basic operations *T*(*n*) an algorithm performs, as a function of the input size *n*. **Space complexity** is the extra memory *S*(*n*) the algorithm needs. Seconds depend on the computer, while the number of operations does not — so operations are counted.

Usually the **worst case** is estimated: the input on which the algorithm works the longest. Sometimes the **average case** matters too (for example, in quick sort and interpolation search).

| Notation | Meaning | Read as |
|---|---|---|
| *T*(*n*) = *O*(*f*(*n*)) | an upper bound: for large enough *n*, *T*(*n*) ≤ *c* · *f*(*n*) | "at most of order *f*" |
| *T*(*n*) = Ω(*f*(*n*)) | a lower bound: *T*(*n*) ≥ *c* · *f*(*n*) | "at least of order *f*" |
| *T*(*n*) = Θ(*f*(*n*)) | a bound from both sides | "exactly of order *f*" |

Rules of computation: constant factors are dropped (3*n* → *O*(*n*)), the fastest-growing term remains (*n*² + 100*n* → *O*(*n*²)), consecutive parts are added, nested loops are multiplied.

## Complexity classes

| Class | Operations for *n* = 10⁶ | Example |
|---|---|---|
| *O*(1) | 1 | accessing an array element |
| *O*(log *n*) | ~20 | binary search |
| *O*(*n*) | 10⁶ | linear search |
| *O*(*n* log *n*) | ~2 · 10⁷ | merge sort |
| *O*(*n*²) | 10¹² | bubble sort |
| *O*(2ⁿ) | impossible to compute | enumerating all subsets |

A modern computer performs ~10⁸–10⁹ simple operations per second. So for *n* = 10⁶, *O*(*n* log *n*) is instant while *O*(*n*²) takes several hours.

**The class P** is the problems solvable in polynomial time (*O*(*n*ᵏ)). **The class NP** is the problems whose given solution can be **checked** in polynomial time (for example, sudoku: hard to solve, easy to check). Is P = NP? — the most famous open question of computer science; the Clay Institute has offered a prize of one million dollars for its proof.

## The complexity of recursive algorithms

The running time of a recursive algorithm is written as a **recurrence relation**: the time for a big problem in terms of the time for smaller ones.

| Recurrence relation | Algorithm | Solution |
|---|---|---|
| *T*(*n*) = *T*(*n* − 1) + *O*(1) | recursive factorial, sum | *O*(*n*) |
| *T*(*n*) = *T*(*n*/2) + *O*(1) | binary search | *O*(log *n*) |
| *T*(*n*) = 2*T*(*n*/2) + *O*(*n*) | merge sort | *O*(*n* log *n*) |
| *T*(*n*) = *T*(*n* − 1) + *T*(*n* − 2) + *O*(1) | plain recursive Fibonacci | *O*(φⁿ) ≈ *O*(1.618ⁿ) |

For example, in binary search every step halves the search range: *n* → *n*/2 → *n*/4 → … → 1. That is log₂ *n* steps.

## Search algorithms

| Algorithm | Requirement | Worst case | Average |
|---|---|---|---|
| Linear | none | *O*(*n*) | *O*(*n*) |
| Binary | the array is sorted | *O*(log *n*) | *O*(log *n*) |
| Interpolation | sorted, values **uniformly distributed** | *O*(*n*) | *O*(log log *n*) |

**Interpolation search** resembles looking a name up in a phone book: looking for "Yoldoshev", we open the book not in the middle but near the end. The next position to check is computed by linear interpolation:

```text
position = left + (x − a[left]) · (right − left) / (a[right] − a[left])
```

If the values are uniformly distributed (say, 10, 20, 30, …), the position is found almost at once. With an uneven distribution (1, 2, 4, 8, …) it may be slower than binary search.

## Worked example 1: comparisons in linear and binary search

*n* and *x* are given. The array is `a[i] = 2i + 1` (the first *n* odd numbers, in ascending order). Search for *x* with linear and binary search and print on one line how many comparisons each made.

```python
n, x = map(int, input().split())


def a(i):                           # the array is not kept in memory
    return 2 * i + 1


chiziqli = 0
for i in range(n):
    chiziqli += 1
    if a(i) == x:
        break

binar = 0
chap, ong = 0, n - 1
while chap <= ong:
    binar += 1
    orta = (chap + ong) // 2
    if a(orta) == x:
        break
    if a(orta) < x:
        chap = orta + 1
    else:
        ong = orta - 1

print(chiziqli, binar)
```
```java
import java.util.Scanner;

public class Main {
    static long a(long i) {                 // the array is not kept in memory
        return 2 * i + 1;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = in.nextLong(), x = in.nextLong();

        long chiziqli = 0;
        for (long i = 0; i < n; i++) {
            chiziqli++;
            if (a(i) == x) break;
        }

        long binar = 0, chap = 0, ong = n - 1;
        while (chap <= ong) {
            binar++;
            long orta = (chap + ong) / 2;
            if (a(orta) == x) break;
            if (a(orta) < x) chap = orta + 1;
            else ong = orta - 1;
        }

        System.out.println(chiziqli + " " + binar);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long a(long long i) { return 2 * i + 1; }   // the array is not kept in memory

int main() {
    long long n, x;
    cin >> n >> x;

    long long chiziqli = 0;
    for (long long i = 0; i < n; i++) {
        chiziqli++;
        if (a(i) == x) break;
    }

    long long binar = 0, chap = 0, ong = n - 1;
    while (chap <= ong) {
        binar++;
        long long orta = (chap + ong) / 2;
        if (a(orta) == x) break;
        if (a(orta) < x) chap = orta + 1;
        else ong = orta - 1;
    }

    cout << chiziqli << " " << binar << endl;
    return 0;
}
```

| Input | Linear | Binary | Note |
|---|---|---|---|
| `1000000 1` | 1 | 19 | the first element — the best case for linear |
| `1000000 1234567` | 617,284 | 20 | a typical case |
| `1000000 4` | 1,000,000 | 20 | an even number is absent — the worst case for both |

On a million elements binary search makes at most 20 comparisons (2²⁰ ≈ 10⁶), linear search up to a million (*chiziqli* is linear, *binar* binary, *chap*/*ong*/*orta* left/right/middle). Yet in the first row linear search won: complexity describes the **worst** or **average** case, not every single input.

## Worked example 2: interpolation search

The first line holds *n*, the second *n* integers in ascending order, the third *x*. Find the position of *x* (from 1; −1 if absent) with interpolation search; print the position and the number of steps taken by interpolation and by binary search.

```python
def interpolyatsion(a, x):
    chap, ong = 0, len(a) - 1
    qadam = 0
    while chap <= ong and a[chap] <= x <= a[ong]:
        qadam += 1
        if a[ong] == a[chap]:
            orin = chap
        else:
            orin = chap + (x - a[chap]) * (ong - chap) // (a[ong] - a[chap])
        if a[orin] == x:
            return orin + 1, qadam
        if a[orin] < x:
            chap = orin + 1
        else:
            ong = orin - 1
    return -1, qadam


def binar(a, x):
    chap, ong = 0, len(a) - 1
    qadam = 0
    while chap <= ong:
        qadam += 1
        orta = (chap + ong) // 2
        if a[orta] == x:
            return orta + 1, qadam
        if a[orta] < x:
            chap = orta + 1
        else:
            ong = orta - 1
    return -1, qadam


n = int(input())
a = list(map(int, input().split()))
x = int(input())
orin, q1 = interpolyatsion(a, x)
_, q2 = binar(a, x)
print(orin, q1, q2)
```
```java
import java.util.Scanner;

public class Main {
    // the result: {position (from 1, or -1), steps}
    static long[] interpolyatsion(long[] a, long x) {
        int chap = 0, ong = a.length - 1;
        long qadam = 0;
        while (chap <= ong && a[chap] <= x && x <= a[ong]) {
            qadam++;
            int orin = a[ong] == a[chap] ? chap
                    : (int) (chap + (x - a[chap]) * (ong - chap) / (a[ong] - a[chap]));
            if (a[orin] == x) return new long[] {orin + 1, qadam};
            if (a[orin] < x) chap = orin + 1;
            else ong = orin - 1;
        }
        return new long[] {-1, qadam};
    }

    static long[] binar(long[] a, long x) {
        int chap = 0, ong = a.length - 1;
        long qadam = 0;
        while (chap <= ong) {
            qadam++;
            int orta = (chap + ong) / 2;
            if (a[orta] == x) return new long[] {orta + 1, qadam};
            if (a[orta] < x) chap = orta + 1;
            else ong = orta - 1;
        }
        return new long[] {-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = in.nextLong();
        long x = in.nextLong();
        long[] i1 = interpolyatsion(a, x), b1 = binar(a, x);
        System.out.println(i1[0] + " " + i1[1] + " " + b1[1]);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

// the result: {position (from 1, or -1), steps}
pair<long long, long long> interpolyatsion(const vector<long long>& a, long long x) {
    long long chap = 0, ong = (long long) a.size() - 1, qadam = 0;
    while (chap <= ong && a[chap] <= x && x <= a[ong]) {
        qadam++;
        long long orin = a[ong] == a[chap] ? chap
                       : chap + (x - a[chap]) * (ong - chap) / (a[ong] - a[chap]);
        if (a[orin] == x) return {orin + 1, qadam};
        if (a[orin] < x) chap = orin + 1;
        else ong = orin - 1;
    }
    return {-1, qadam};
}

pair<long long, long long> binar(const vector<long long>& a, long long x) {
    long long chap = 0, ong = (long long) a.size() - 1, qadam = 0;
    while (chap <= ong) {
        qadam++;
        long long orta = (chap + ong) / 2;
        if (a[orta] == x) return {orta + 1, qadam};
        if (a[orta] < x) chap = orta + 1;
        else ong = orta - 1;
    }
    return {-1, qadam};
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto& v : a) cin >> v;
    long long x;
    cin >> x;
    auto [orin, q1] = interpolyatsion(a, x);
    auto [_, q2] = binar(a, x);
    cout << orin << " " << q1 << " " << q2 << endl;
    return 0;
}
```

| Array | *x* | Result | Interpolation | Binary |
|---|---|---|---|---|
| `10 20 30 … 100` | 70 | 7 | 1 step | 4 steps |
| `10 20 30 … 100` | 75 | −1 | 1 step | 4 steps |
| `1 2 4 8 … 512` | 256 | 9 | 4 steps | 3 steps |

On a uniformly distributed array interpolation search found the position **on the first try**. On numbers that double, however, the formula "leans" towards the small values and needed more steps than binary search (*orin* is the position, *qadam* a step). The check `a[ong] == a[chap]` prevents division by zero. The product `(x − a[chap]) · (ong − chap)` can be large, which is why Java and C++ use `long`.

:::warning
**Common mistakes**

- **Paying too much attention to constant factors.** 2*n* and 100*n* are both *O*(*n*); for large *n* the difference is negligible compared with *n*².
- **Binary or interpolation search on an unsorted array.** The result is wrong; sorting (*O*(*n* log *n*)) is needed first.
- **Division by zero in interpolation search.** The case `a[ong] == a[chap]` is checked separately.
- **Taking the best case as the algorithm's estimate.** Linear search finds the first element in one step, yet its complexity is still *O*(*n*).
:::

:::tip
A practical way to check an algorithm's complexity: double *n* and watch how the time (or the number of operations) changes. For *O*(*n*) it doubles, for *O*(*n*²) it quadruples, and for *O*(log *n*) it grows by just one step.
:::

## Summary

- Complexity is the rate at which the number of operations grows with the input size; usually the worst case is estimated.
- *O* is an upper bound, Ω a lower bound, Θ a bound from both sides.
- The main classes: *O*(1), *O*(log *n*), *O*(*n*), *O*(*n* log *n*), *O*(*n*²), *O*(2ⁿ); P and NP are the problems solvable and checkable in polynomial time.
- The complexity of a recursive algorithm is found from its recurrence relation.
- Linear search is *O*(*n*), binary *O*(log *n*), interpolation *O*(log log *n*) on average (for a uniform distribution).

## Check yourself

:::check
Q: What is the complexity of an algorithm with *T*(*n*) = 5*n*² + 3*n* + 100?
A: *O*(*n*²) — the fastest-growing term remains and the factor is dropped.
Q: At most how many comparisons does binary search make in a sorted array of 1,000,000 elements?
A: About 20, because 2²⁰ ≈ 10⁶.
Q: What complexity does *T*(*n*) = *T*(*n*/2) + *O*(1) give?
A: *O*(log *n*).
Q: When is interpolation search faster than binary search?
A: When the array is sorted and the values are uniformly distributed.
Q: What is the main sign of a problem in the class NP?
A: A proposed solution can be checked in polynomial time.
:::

:::link https://www.bigocheatsheet.com/
Big-O cheat sheet
A table of the complexity of the main algorithms and data structures.
:::

:::link https://en.wikipedia.org/wiki/Interpolation_search
Interpolation search
The algorithm, its complexity and a comparison with binary search.
:::
