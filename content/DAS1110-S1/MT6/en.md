---
title: Organising nested loop processes
summary: Independent work: going through all pairs with a nested loop, the i < j condition, estimating the number of iterations; the sample problem is the number of inversions in an array.
---

## Why it matters

In a tournament every team plays every other team once. How many games are there, and how can they be listed? This is the problem of **going through all pairs**, and it is solved with a nested loop. The same pattern is used to find similar documents, the closest two points, or to measure how "sorted" data is. In this independent work you practise working with pairs and estimating how much work a program does.

:::note
**After this independent work you will be able to:**

- go through all the pairs (*i*, *j*) of an array without repeats, using the condition *i* < *j*;
- tie the bound of the inner loop to the outer counter;
- count how many times the inner body runs (*n*(*n* − 1)/2);
- list all the pairs by hand for a small input and check the program.
:::

## A quick review of the topic

Two ways of going through all pairs:

| Way | Loops | Number of pairs | Problem |
|---|---|---|---|
| Everyone with everyone | *i*: 0 … *n* − 1, *j*: 0 … *n* − 1 | *n*² | (*i*, *i*) and (*j*, *i*) are included |
| Only *i* < *j* | *i*: 0 … *n* − 1, *j*: *i* + 1 … *n* − 1 | *n*(*n* − 1)/2 | — |

In the second way every pair appears **exactly once**. The inner loop starts at *i* + 1 — its bound depends on the outer counter.

*n*(*n* − 1)/2 grows fast: 45 for *n* = 10, ~500 thousand for *n* = 1000, ~5 billion pairs for *n* = 100 000. So for large *n* a nested loop is slow and a faster algorithm is needed (sorting, hash tables — in later semesters).

## Sample problem: the number of inversions

The first line has *n*, the second *n* numbers. Find the number of **inversions** — pairs with *i* < *j* and *aᵢ* > *aⱼ*. The number of inversions shows how "far" an array is from sorted: 0 in a sorted array, *n*(*n* − 1)/2 in one sorted in descending order.

```text
1. read n and a
2. soni = 0
3. for each i = 0 … n-2:
       for each j = i+1 … n-1:
           if a[i] > a[j]: soni = soni + 1
4. print soni
```

```python
n = int(input())
a = list(map(int, input().split()))

soni = 0
for i in range(n):
    for j in range(i + 1, n):
        if a[i] > a[j]:
            soni += 1

print(soni)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }

        long soni = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (a[i] > a[j]) {
                    soni++;
                }
            }
        }

        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    long long soni = 0;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (a[i] > a[j]) {
                soni++;
            }
        }
    }

    cout << soni << endl;
    return 0;
}
```

For the input `5` and `3 1 4 2 5`, the inversions among all 10 pairs are (*soni* is the counter):

| Pair (values) | An inversion? |
|---|---|
| (3, 1) | yes |
| (3, 2) | yes |
| (4, 2) | yes |
| the other 7 | no |

The output is `3`. For the input `4` and `4 3 2 1` all 6 pairs are inversions, and `6` = 4 · 3 / 2 is printed. The counter is a `long`: for a large *n* the number of inversions can exceed an `int`.

## The independent work task

Solve the **pairs with sum *k*** problem given below. What you hand in:

1. **A hand count** — for *a* = (1, 5, 3, 3, 2), *k* = 6, write all 10 pairs into a table and mark the matching ones.
2. **The number of iterations** — how many times does the inner body run for *n* = 5, 100, 10 000? Estimate how long your program would run for *n* = 10 000.
3. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
4. **Tests** — at least 3: no matching pair, equal numbers present (for example 3 + 3), every pair matching.

:::warning
**Assessment criteria and common mistakes**

- If the inner loop starts at `j = 0`, each pair is counted twice and (*i*, *i*) is included — start at `j = i + 1`.
- Elements with the same value but in different positions (3 and 3) form a separate pair.
- The inner and outer loops must use different counter names (`i`, `j`).
- The hand-count table is not copied from the program.
:::

:::tip
First write all the pairs for *n* = 3: (0, 1), (0, 2), (1, 2). Check with a `print` that the inner body sees exactly these three pairs, then remove that output.
:::

## Summary

- All pairs are gone through with the condition *i* < *j*: the inner loop starts at *i* + 1.
- The number of pairs is *n*(*n* − 1)/2 — it grows fast as *n* grows.
- The counter can get large — choose a wide type (`long`).
- For a small input, list the pairs by hand and check the program.

## Check yourself

:::check
Q: How many games are played in a tournament of 8 teams (each plays each once)?
A: 28: 8 · 7 / 2.
Q: How many inversions does a sorted array have?
A: 0: in no pair is the earlier element larger than the later one.
Q: Does the number of inversions change if the inner loop starts at `j = i`?
A: No, because `a[i] > a[i]` is never true; but *n* useless checks are added. In the pair-sum problem, though, (*i*, *i*) could give a wrong result.
:::

:::link https://en.wikipedia.org/wiki/Inversion_(discrete_mathematics)
Wikipedia: inversion
Inversions in a sequence and how they relate to sorting.
:::
