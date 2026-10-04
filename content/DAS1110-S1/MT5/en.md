---
title: Organising vectors
summary: Independent work: keeping a vector in an array, operations on vectors (sum, multiplication by a number, length, distance); the C++ vector and dynamic size; the sample problem is the sum of two vectors and the distance between them.
---

## Why it matters

A **vector** in mathematics is an ordered collection of numbers: a point on the plane (*x*, *y*), a velocity in space (*vx*, *vy*, *vz*), a student's marks in five subjects. Recommendation systems (YouTube, online shops) represent a user and a product as vectors of hundreds of dimensions and find similarity through the "distance" or the dot product between them. In a program a vector is kept as an ordinary one-dimensional array.

:::note
**After this independent work you will be able to:**

- keep an *n*-dimensional vector in an array;
- compute the sum of vectors, multiplication by a number, the length and the distance;
- create a vector of dynamic size with a C++ `vector`, a Java array or a Python list;
- work out the result by hand from the formula and check the program.
:::

## A quick review of the topic

For the vectors *a* = (*a*₁, …, *aₙ*) and *b* = (*b*₁, …, *bₙ*):

| Operation | Formula | In a program |
|---|---|---|
| Sum | (*a*₁ + *b*₁, …, *aₙ* + *bₙ*) | `c[i] = a[i] + b[i]` |
| Multiplying by a number | (*k* · *a*₁, …, *k* · *aₙ*) | `c[i] = k * a[i]` |
| Length | √(*a*₁² + … + *aₙ*²) | a sum in a loop, then `sqrt` |
| Distance | √((*a*₁ − *b*₁)² + … ) | the root of the sum of the squared differences |
| Dot product | *a*₁*b*₁ + … + *aₙbₙ* | `s += a[i] * b[i]` |

All of these take one loop, and the two vectors must have the same dimension.

In C++ the word "vector" has two meanings: the mathematical vector and `std::vector` — a resizable array. The second is handy for keeping the first: `vector<double> a(n);`.

## Sample problem: the sum and the distance

The first line has *n*, the second and third lines *n* integers each: the vectors *a* and *b*. On the first line print *a* + *b*; on the second, the distance between *a* and *b* (two decimal places).

```python
import math

n = int(input())
a = list(map(int, input().split()))
b = list(map(int, input().split()))

yigindi = [a[i] + b[i] for i in range(n)]

kvadratlar = 0
for i in range(n):
    kvadratlar += (a[i] - b[i]) ** 2
masofa = math.sqrt(kvadratlar)

print(*yigindi)
print(f"{masofa:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        int[] b = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        for (int i = 0; i < n; i++) b[i] = in.nextInt();

        StringBuilder sb = new StringBuilder();
        long kvadratlar = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i] + b[i]);
            long d = a[i] - b[i];
            kvadratlar += d * d;
        }

        System.out.println(sb);
        System.out.println(String.format("%.2f", Math.sqrt(kvadratlar)));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <vector>
#include <cmath>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n), b(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    for (int i = 0; i < n; i++) cin >> b[i];

    long long kvadratlar = 0;
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i] + b[i];
        long long d = a[i] - b[i];
        kvadratlar += d * d;
    }
    cout << endl;
    cout << fixed << setprecision(2) << sqrt((double) kvadratlar) << endl;
    return 0;
}
```

For the input `3`, `1 2 3`, `4 6 3`:

| *i* | *aᵢ* | *bᵢ* | *aᵢ* + *bᵢ* | (*aᵢ* − *bᵢ*)² |
|---|---|---|---|---|
| 1 | 1 | 4 | 5 | 9 |
| 2 | 2 | 6 | 8 | 16 |
| 3 | 3 | 3 | 6 | 0 |

The output is `5 8 6` and `5.00` (√25). The squared difference is computed as a `long`: with large coordinates the square could overflow an `int`.

## The independent work task

Solve the **dot product of vectors** problem given below. What you hand in:

1. **A hand calculation** — compute the dot product of *a* = (1, −2, 3) and *b* = (4, 0, −1) as a table.
2. **The geometric meaning** — what can be said about two vectors whose dot product is 0? Give two examples.
3. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
4. **An overflow analysis** — if the coordinates go up to 10⁶ and *n* = 10⁵, does the result fit in an `int`? Which type did you choose, and why?

:::warning
**Assessment criteria and common mistakes**

- The accumulator is set to 0 **before** the loop, not inside it.
- With large values the product exceeds an `int` — use `long` / `long long`.
- Both vectors must be read with the same length; do not read the second vector into the first.
- The hand calculation and the analysis are not copied from the program.
:::

:::tip
Write the two vectors one under the other and multiply them pair by pair: this is the quickest way to compute a dot product by hand, and the program works exactly the same way.
:::

## Summary

- An *n*-dimensional vector is kept in a program as an array of length *n*.
- The sum, multiplication by a number, length, distance and dot product each take one loop.
- Squares and products can overflow with large values — choose a wide type.
- The C++ `vector` is a handy resizable array for keeping a vector.

## Check yourself

:::check
Q: What is the length of the vector (3, 4)?
A: 5: √(9 + 16).
Q: What is the dot product of (1, 2) and (−2, 1), and what does it mean?
A: 1 · (−2) + 2 · 1 = 0: the vectors are perpendicular.
Q: What do you get when *a* = (1, 2, 3) is multiplied by 2?
A: (2, 4, 6).
:::

:::link https://en.wikipedia.org/wiki/Dot_product
Wikipedia: the dot product
Its definition, geometric meaning and properties.
:::
