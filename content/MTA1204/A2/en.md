---
title: Computing the complexity of algorithms. Recursive computation algorithms (practical session)
summary: Checking complexity experimentally by counting operations, the method of doubling n; a divide-and-conquer recursive algorithm, the number of calls and the recursion depth; the exponential growth of plain recursive Fibonacci.
---

## Why it matters

In the lecture we saw the *O* notation and recurrence relations in theory. In practice the question is simple: is my program really *O*(*n* log *n*), or did I compute it wrong? The most reliable way to answer is to **count** the operations and watch how they grow as *n* increases.

In this session we run three experiments: we count the operations of linear, quadratic and logarithmic loops; we measure the calls and depth of a divide-and-conquer recursive algorithm; and we see how fast the calls of plain recursive Fibonacci explode.

:::note
**After this session you will be able to:**

- add a counter to a program and measure the number of operations;
- determine a complexity class with the method of doubling *n*;
- write a recurrence relation for a recursive algorithm and compute its number of calls in advance;
- explain the difference between recursion depth and the number of calls.
:::

## Checking complexity experimentally

The method: double *n* and see how many times the number of operations grew.

| Complexity | Operations when *n* → 2*n* | Sign |
|---|---|---|
| *O*(log *n*) | +1 | barely changes |
| *O*(*n*) | × 2 | doubles |
| *O*(*n* log *n*) | a little more than × 2 | |
| *O*(*n*²) | × 4 | quadruples |
| *O*(2ⁿ) | squares | doubles even for *n* + 1 |

Measuring time in seconds is possible too, but it depends on the computer's load and is imprecise for small *n*. A counter always gives the same result.

## Analysing a recursive algorithm

The **divide and conquer** method: split a problem into two halves, solve each recursively and combine the results. Two measures matter:

| Measure | Meaning | What it determines |
|---|---|---|
| Number of calls | how many times the function was called in total | time |
| Recursion depth | the most calls on the stack at the same time | memory (the stack) |

When finding the maximum by splitting an array in two: *T*(*n*) = 2*T*(*n*/2) + 1, *T*(1) = 1. The solution: *T*(*n*) = 2*n* − 1 calls, while the depth is ⌈log₂ *n*⌉ + 1. So the time is *O*(*n*) and the stack memory *O*(log *n*).

## Worked example 1: counting operations

*n* is given (1 ≤ *n* ≤ 3000). Count and print the inner operations of three loops: (1) looking at every element once; (2) looking at all pairs (*i* < *j*); (3) halving *n* until it reaches 1.

```python
n = int(input())

chiziqli = 0
for i in range(n):
    chiziqli += 1

kvadratik = 0
for i in range(n):
    for j in range(i + 1, n):       # all pairs i < j
        kvadratik += 1

logarifmik = 0
m = n
while m > 1:
    m //= 2
    logarifmik += 1

print(chiziqli, kvadratik, logarifmik)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();

        long chiziqli = 0;
        for (int i = 0; i < n; i++) chiziqli++;

        long kvadratik = 0;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)     // all pairs i < j
                kvadratik++;

        long logarifmik = 0;
        for (int m = n; m > 1; m /= 2) logarifmik++;

        System.out.println(chiziqli + " " + kvadratik + " " + logarifmik);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long chiziqli = 0;
    for (int i = 0; i < n; i++) chiziqli++;

    long long kvadratik = 0;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)         // all pairs i < j
            kvadratik++;

    long long logarifmik = 0;
    for (int m = n; m > 1; m /= 2) logarifmik++;

    cout << chiziqli << " " << kvadratik << " " << logarifmik << endl;
    return 0;
}
```

| *n* | Linear | Quadratic | Logarithmic |
|---|---|---|---|
| 1000 | 1,000 | 499,500 | 9 |
| 2000 | 2,000 | 1,999,000 | 10 |
| 3000 | 3,000 | 4,498,500 | 11 |

When *n* doubled, the linear counter doubled, the quadratic one almost quadrupled (*n*(*n* − 1)/2), and the logarithmic one grew by just one. This table alone shows the algorithms' complexity. Note: the quadratic loop needs 4.5 million operations for *n* = 3000 — for *n* = 10⁶ it would be 5 · 10¹¹, impossible to count.

## Worked example 2: divide and conquer — maximum, calls, depth

The first line holds *n*, the second *n* numbers. Find the maximum recursively by splitting the array in two; print the maximum, the number of calls and the greatest recursion depth.

```python
chaqiruvlar = 0
chuqurlik = 0


def maksimum(a, chap, ong, daraja):
    global chaqiruvlar, chuqurlik
    chaqiruvlar += 1
    chuqurlik = max(chuqurlik, daraja)
    if chap == ong:                             # one element
        return a[chap]
    orta = (chap + ong) // 2
    return max(maksimum(a, chap, orta, daraja + 1),
               maksimum(a, orta + 1, ong, daraja + 1))


n = int(input())
a = list(map(int, input().split()))
print(maksimum(a, 0, n - 1, 1), chaqiruvlar, chuqurlik)
```
```java
import java.util.Scanner;

public class Main {
    static long chaqiruvlar = 0;
    static int chuqurlik = 0;

    static int maksimum(int[] a, int chap, int ong, int daraja) {
        chaqiruvlar++;
        chuqurlik = Math.max(chuqurlik, daraja);
        if (chap == ong) return a[chap];            // one element
        int orta = (chap + ong) / 2;
        return Math.max(maksimum(a, chap, orta, daraja + 1),
                        maksimum(a, orta + 1, ong, daraja + 1));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int m = maksimum(a, 0, n - 1, 1);
        System.out.println(m + " " + chaqiruvlar + " " + chuqurlik);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

long long chaqiruvlar = 0;
int chuqurlik = 0;

int maksimum(const vector<int>& a, int chap, int ong, int daraja) {
    chaqiruvlar++;
    chuqurlik = max(chuqurlik, daraja);
    if (chap == ong) return a[chap];                // one element
    int orta = (chap + ong) / 2;
    return max(maksimum(a, chap, orta, daraja + 1),
               maksimum(a, orta + 1, ong, daraja + 1));
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& x : a) cin >> x;
    int m = maksimum(a, 0, n - 1, 1);
    cout << m << " " << chaqiruvlar << " " << chuqurlik << endl;
    return 0;
}
```

For `8` and `3 9 2 7 5 1 8 6` the output is `9 15 4`: 2 · 8 − 1 = 15 calls and log₂ 8 + 1 = 4 levels — just as the recurrence relation predicted. For `3` and `5 1 4` it is `5 5 3` (*chaqiruvlar* are the calls, *chuqurlik* the depth, *daraja* the level). This algorithm is no faster than a plain maximum in a loop (both are *O*(*n*)), but exactly this scheme underlies merge sort and quick sort. The number of calls determines time, the depth the stack memory: on a million elements there are 2 million calls, yet only ~21 of them are on the stack at a time.

## Worked example 3: plain recursive Fibonacci

*n* is given (0 ≤ *n* ≤ 30). Compute *F*(*n*) with direct recursion and print how many times the function was called.

```python
chaqiruvlar = 0


def fib(n):
    global chaqiruvlar
    chaqiruvlar += 1
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)


n = int(input())
print(fib(n), chaqiruvlar)
```
```java
import java.util.Scanner;

public class Main {
    static long chaqiruvlar = 0;

    static long fib(int n) {
        chaqiruvlar++;
        if (n < 2) return n;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        long f = fib(n);
        System.out.println(f + " " + chaqiruvlar);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long chaqiruvlar = 0;

long long fib(int n) {
    chaqiruvlar++;
    if (n < 2) return n;
    return fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    long long f = fib(n);
    cout << f << " " << chaqiruvlar << endl;
    return 0;
}
```

| *n* | *F*(*n*) | Calls |
|---|---|---|
| 10 | 55 | 177 |
| 20 | 6,765 | 21,891 |
| 25 | 75,025 | 242,785 |

Every time *n* grows by 5, the calls grow ~11 times (φ⁵ ≈ 11): this is exponential growth. For *n* = 50 the number of calls reaches 4 · 10¹⁰. The reason is that the same values are computed again and again; the way to fix this is done in the independent assignment. In Java and C++ the result of `fib(n)` is taken into a separate variable so that it is guaranteed to be computed before `chaqiruvlar` in the print expression.

:::warning
**Common mistakes**

- **Putting the counter in the wrong place.** The counter must sit right next to the operation being counted (the inner loop's body, the function's entry).
- **Drawing a conclusion from one measurement.** Complexity is a rate of growth; compare at least two different values of *n*.
- **Confusing depth with the number of calls.** 15 calls may be 4 levels deep — they measure different resources.
- **Relying on evaluation order in C++/Java.** In `cout << fib(n) << chaqiruvlar`, which part is evaluated first was not guaranteed before C++17; take the result into a variable first.
:::

:::tip
Write the experiment's results in a table and compute the ratio of neighbouring rows (for example, 1,999,000 / 499,500 ≈ 4). If the ratio stays constant, you found the complexity class correctly.
:::

## Summary

- By measuring operations with a counter and doubling *n*, a complexity class can be determined experimentally.
- A divide-and-conquer algorithm is analysed with a recurrence relation: *T*(*n*) = 2*T*(*n*/2) + 1 → 2*n* − 1 calls.
- The number of calls determines time, the recursion depth the stack memory.
- Plain recursive Fibonacci is exponential: the same values are recomputed many times.

## Check yourself

:::check
Q: If the number of operations quadruples when *n* doubles, what is the complexity?
A: *O*(*n*²).
Q: How many calls does finding the maximum by halving make for a 16-element array?
A: 2 · 16 − 1 = 31.
Q: What is the recursion depth in that algorithm?
A: log₂ 16 + 1 = 5.
Q: Why is plain recursive Fibonacci slow?
A: The same small values (*F*(*n* − 2), *F*(*n* − 3), …) are recomputed many times; the number of calls grows exponentially.
:::

:::link https://en.wikipedia.org/wiki/Master_theorem_(analysis_of_algorithms)
The master theorem
A general way to solve divide-and-conquer recurrence relations.
:::
