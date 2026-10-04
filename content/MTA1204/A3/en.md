---
title: Search algorithms. Binary, interpolation and Fibonacci search. Searching by hashing (practical session)
summary: Practical uses of binary search — searching on the answer (the integer square root), the bisection method for real numbers; finding a pair with a hash set and speeding it up from O(n²) to O(n); choosing a search method.
---

## Why it matters

In the lectures we saw search algorithms from the point of view of finding an element in an array. In practice, however, the most powerful use of binary search is searching not in an array but **in a range of answers**: "what is the largest number for which the condition holds?" Hashing, in turn, turns a nested loop into a single loop in many problems.

In this session we solve three practical problems: the integer square root of a large number (binary search on the answer), the root of an equation (bisection) and a pair whose sum is a given number (a hash set).

:::note
**After this session you will be able to:**

- apply binary search "on the answer" — over a range with a monotone condition;
- solve an equation for real numbers with the bisection method;
- find a pair in *O*(*n*) with a hash set;
- choose a suitable search method for a problem.
:::

## Binary search on the answer

Binary search works not only in a sorted array. If the question is "what is the largest *x* for which *P*(*x*) holds?" and *P* is **monotone** (true up to some bound, false after it), the answer can be found by halving the range:

```text
chap = the smallest possible answer, ong = the largest possible answer
while chap < ong:
    orta = (chap + ong + 1) / 2        ← round up
    if P(orta): chap = orta            (the answer is orta or larger)
    else:       ong = orta − 1
answer: chap
```

The `+ 1` matters: without it the step `chap = orta` may not shrink the range and lead to an endless loop. For a range of answers from 0 to 2 · 10⁹, ~31 steps are enough.

## The bisection method

For real numbers the same idea is called **bisection** (halving the interval): if *f*(*x*) is continuous and has different signs at the ends of [*a*, *b*], we halve the interval and keep the half containing the root. The error halves at every step: after 100 steps it is 2⁻¹⁰⁰ of the initial length — smaller even than the precision of a `double`.

## Searching with a hash

"Is there a pair in the array whose sum is *t*?" — the direct solution checks all pairs: *O*(*n*²). With a hash set: for every *x* the question "has *t* − *x* occurred before?" is answered in *O*(1), *O*(*n*) in total. This "look the complement up in a hash" method works in very many problems.

| Problem | A suitable method |
|---|---|
| a one-off search in an unsorted array | linear |
| many searches in a sorted array | binary |
| sorted, uniformly distributed data | interpolation |
| very many searches by key, order not needed | a hash table |
| the largest/smallest value satisfying a monotone condition | binary search on the answer |

## Worked example 1: the integer square root

*n* is given (0 ≤ *n* ≤ 10¹⁸). Print the largest integer *x* with *x*² ≤ *n* and the number of binary search steps. Do not use the real-number `sqrt` — for large *n* it gives rounding errors.

```python
n = int(input())
chap, ong = 0, 2 * 10**9                # the answer lies in this range
qadam = 0
while chap < ong:
    qadam += 1
    orta = (chap + ong + 1) // 2        # round up
    if orta * orta <= n:                # the condition holds — the answer is orta or larger
        chap = orta
    else:
        ong = orta - 1
print(chap, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long chap = 0, ong = 2_000_000_000L;        // the answer lies in this range
        int qadam = 0;
        while (chap < ong) {
            qadam++;
            long orta = (chap + ong + 1) / 2;       // round up
            if (orta * orta <= n) chap = orta;      // the condition holds — the answer is orta or larger
            else ong = orta - 1;
        }
        System.out.println(chap + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long chap = 0, ong = 2000000000LL;         // the answer lies in this range
    int qadam = 0;
    while (chap < ong) {
        qadam++;
        long long orta = (chap + ong + 1) / 2;      // round up
        if (orta * orta <= n) chap = orta;          // the condition holds — the answer is orta or larger
        else ong = orta - 1;
    }
    cout << chap << " " << qadam << endl;
    return 0;
}
```

| *n* | Result | Steps |
|---|---|---|
| 10 | 3 | 31 |
| 10¹⁸ | 1,000,000,000 | 30 |
| 999,999,999,999,999,999 | 999,999,999 | 31 |

In the last row `sqrt` might have returned 10⁹ (because of real-number rounding), while binary search gives the exact answer. `orta * orta` ≤ 4 · 10¹⁸ fits in a `long` (`long long`); if you take an upper bound larger than 2 · 10⁹, the product may overflow (*chap*/*ong*/*orta* are left/right/middle, *qadam* a step).

## Worked example 2: bisection — the root of an equation

*c* > 0 is given. Find the positive root of the equation *x*³ + *x* = *c* with the bisection method to 6 decimal places. *f*(*x*) = *x*³ + *x* is increasing, so the root is unique and lies in [0, max(1, *c*)].

```python
c = float(input())
chap, ong = 0.0, max(1.0, c)            # f(chap) < c <= f(ong)
for _ in range(100):                    # every step halves the error
    orta = (chap + ong) / 2
    if orta**3 + orta < c:
        chap = orta
    else:
        ong = orta
print(f"{chap:.6f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        double c = new Scanner(System.in).nextDouble();
        double chap = 0.0, ong = Math.max(1.0, c);  // f(chap) < c <= f(ong)
        for (int k = 0; k < 100; k++) {             // every step halves the error
            double orta = (chap + ong) / 2;
            if (orta * orta * orta + orta < c) chap = orta;
            else ong = orta;
        }
        System.out.printf("%.6f%n", chap);
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    double c;
    cin >> c;
    double chap = 0.0, ong = max(1.0, c);           // f(chap) < c <= f(ong)
    for (int k = 0; k < 100; k++) {                 // every step halves the error
        double orta = (chap + ong) / 2;
        if (orta * orta * orta + orta < c) chap = orta;
        else ong = orta;
    }
    printf("%.6f\n", chap);
    return 0;
}
```

For `10` the output is `2.000000` (2³ + 2 = 10), for `1000` it is `9.966667`, for `0.5` it is `0.423854`. Here exactly 100 steps were taken instead of "until the interval is small": a `double`'s precision is limited, so the condition "interval < 10⁻¹⁵" might sometimes never come true. Bisection is slow (one binary digit per step) but very reliable: if the root is in the interval, it is certainly found.

## Worked example 3: a pair with a given sum (a hash set)

The first line holds *n* and *t*, the second *n* integers. Print the positions (from 1) of the pair whose sum equals *t* and whose second element stands furthest to the left; if there is no such pair, print `-`.

```python
n, t = map(int, input().split())
a = list(map(int, input().split()))

korilgan = {}                           # value -> its first position
javob = None
for j, x in enumerate(a):
    if t - x in korilgan:               # has the complement occurred before? — O(1)
        javob = (korilgan[t - x] + 1, j + 1)
        break
    if x not in korilgan:
        korilgan[x] = j

print(*javob if javob else "-")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long t = in.nextLong();
        Map<Long, Integer> korilgan = new HashMap<>();      // value -> its first position
        String javob = "-";
        for (int j = 0; j < n; j++) {
            long x = in.nextLong();
            Integer i = korilgan.get(t - x);                // has the complement occurred before? — O(1)
            if (i != null) {
                javob = (i + 1) + " " + (j + 1);
                break;
            }
            korilgan.putIfAbsent(x, j);
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <unordered_map>
#include <vector>
using namespace std;

int main() {
    int n;
    long long t;
    cin >> n >> t;
    vector<long long> a(n);
    for (auto& v : a) cin >> v;

    unordered_map<long long, int> korilgan;                 // value -> its first position
    for (int j = 0; j < n; j++) {
        auto it = korilgan.find(t - a[j]);                  // has the complement occurred before? — O(1)
        if (it != korilgan.end()) {
            cout << it->second + 1 << " " << j + 1 << endl;
            return 0;
        }
        korilgan.emplace(a[j], j);                          // not added if already present
    }
    cout << "-" << endl;
    return 0;
}
```

For `6 12` and `8 3 11 5 7 2` the output is `4 5` (5 + 7 = 12); for `3 10` and `1 2 3` it is `-`; for `2 8` and `4 4` it is `1 2`. Every element is looked at once with one access to the hash set — *O*(*n*) (*korilgan* means "seen", *javob* the answer). A nested loop would check 5 · 10⁹ pairs for 10⁵ elements; with a hash, 10⁵ operations. The case of equal elements (`4 4`) needs special care: so that an element is not paired with itself, it is checked first and added afterwards.

:::warning
**Common mistakes**

- **Forgetting the `+ 1` in binary search on the answer.** `orta = (chap + ong) / 2` together with `chap = orta` leads to an endless loop.
- **Trusting `sqrt` with large numbers.** A `double` does not hold integers above 2⁵³ exactly; use integer binary search.
- **Stopping bisection with a precision condition.** The condition `ong - chap > 1e-15` may never become false because of rounding; fix the number of steps.
- **Adding an element to the hash first and checking afterwards.** When `t = 2x` the element gets paired with itself.
:::

:::tip
Before writing a binary search on the answer, write the monotone condition on one line: "*x*² ≤ *n*", "*x*³ + *x* < *c*". If the condition switches from true to false (or the other way round) only once as *x* grows, binary search works.
:::

## Summary

- Binary search works on any range with a monotone condition: halving the range of answers finds the answer in *O*(log range) steps.
- With integers the pair `orta = (chap + ong + 1) / 2` and `chap = orta` prevents an endless loop.
- Bisection solves a real equation reliably with a fixed number of steps.
- A hash set answers "is there a complement?" in *O*(1), bringing pair search down from *O*(*n*²) to *O*(*n*).
- A search method is chosen by the structure of the data and the question.

## Check yourself

:::check
Q: What must the condition be like for binary search on the answer?
A: Monotone: true up to some bound and false after it (or the other way round).
Q: How many steps does binary search take over a range of answers from 0 to 10⁹?
A: About 30 (2³⁰ ≈ 10⁹).
Q: Why is the number of steps fixed in advance in bisection?
A: A `double`'s precision is limited; the condition "the error is very small" might never come true because of rounding.
Q: Why does a hash set speed up the search for a pair?
A: For every element the question "is *t* − *x* there?" is answered in *O*(1) without walking the whole array.
:::

:::link https://cp-algorithms.com/num_methods/binary_search.html
CP-Algorithms: binary search
Binary search on the answer and its variants.
:::
