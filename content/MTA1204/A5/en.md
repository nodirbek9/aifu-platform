---
title: Bubble sort and Hoare's sort. Parallel sorting algorithms (practical session)
summary: The number of passes of bubble sort and the "turtle" problem, cocktail (shaker) sort; finding the k-th smallest element with Hoare partitioning (quickselect); checking a sorting network with the 0-1 principle.
---

## Why it matters

In the lecture we saw bubble sort and quick sort, and sorting networks too. In practice the ideas of these algorithms turn up in unexpected places: running bubble sort in both directions speeds it up noticeably, and Hoare partitioning finds the median or the best *k* students without sorting the whole array. And to check that a sorting network is correct, it is enough to try not all possible inputs but only those made of 0s and 1s.

:::note
**After this session you will be able to:**

- measure the number of passes of bubble sort and explain the "turtle" problem;
- write cocktail sort and show its advantage;
- find the *k*-th smallest element in *O*(*n*) on average with Hoare partitioning;
- check a sorting network with the 0-1 principle.
:::

## Rabbits and turtles

In bubble sort a large element "flies" to the end in one pass — a **rabbit**. A small element, however, moves only one position left per pass — a **turtle**. A single small element at the end of the array (`2 3 4 5 6 7 1`) forces bubble sort to make almost *n* passes.

**Cocktail sort** (shaker sort) alternates passes from left to right and from right to left: in the backward pass turtles also reach the front in a single pass. In the worst case the complexity is still *O*(*n*²), but on "nearly sorted" arrays it is much faster than bubble sort.

## The selection problem and quickselect

**The selection problem**: find the *k*-th smallest element of an array (*k* = *n*/2 is the median). A full sort is *O*(*n* log *n*). Hoare's **quickselect**, however, continues after partitioning into only **one** part — the one holding position *k*:

| | Quick sort | Quickselect |
|---|---|---|
| After partitioning | both parts are sorted recursively | only one part is examined |
| Average time | *O*(*n* log *n*) | *O*(*n*) (*n* + *n*/2 + *n*/4 + … ≈ 2*n*) |
| Worst case | *O*(*n*²) | *O*(*n*²) |

C++'s `nth_element`, Python's `heapq.nsmallest` and fast median computation in statistics rest on this idea.

## The 0-1 principle

To prove a sorting network correct there is the **0-1 principle** (Knuth): if a network correctly sorts **all** 2ⁿ inputs made of 0s and 1s, it correctly sorts any sequence of numbers. For *n* = 16 that is 65,536 checks — incomparably easier than trying all 16! ≈ 2 · 10¹³ orders.

## Worked example 1: passes of bubble sort and cocktail sort

The first line holds *n*, the second *n* numbers. Print the number of passes made by improved bubble sort (it stops after a pass without swaps) and by cocktail sort.

```python
def pufaksimon(asl):
    a = asl[:]
    otish = 0
    while True:
        otish += 1
        almashdi = False
        for i in range(len(a) - otish):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        if not almashdi:                # a pass without swaps — sorted
            return otish


def kokteyl(asl):
    a = asl[:]
    otish = 0
    chap, ong = 0, len(a) - 1
    while chap < ong:
        otish += 1                      # left to right: the rabbits
        almashdi = False
        for i in range(chap, ong):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        ong -= 1
        if not almashdi:
            break
        otish += 1                      # right to left: the turtles
        almashdi = False
        for i in range(ong, chap, -1):
            if a[i - 1] > a[i]:
                a[i - 1], a[i] = a[i], a[i - 1]
                almashdi = True
        chap += 1
        if not almashdi:
            break
    return otish


n = int(input())
a = list(map(int, input().split()))
print(pufaksimon(a), kokteyl(a))
```
```java
import java.util.Scanner;

public class Main {
    static int pufaksimon(int[] asl) {
        int[] a = asl.clone();
        int otish = 0;
        while (true) {
            otish++;
            boolean almashdi = false;
            for (int i = 0; i < a.length - otish; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            if (!almashdi) return otish;            // a pass without swaps — sorted
        }
    }

    static int kokteyl(int[] asl) {
        int[] a = asl.clone();
        int otish = 0, chap = 0, ong = a.length - 1;
        while (chap < ong) {
            otish++;                                // left to right: the rabbits
            boolean almashdi = false;
            for (int i = chap; i < ong; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            ong--;
            if (!almashdi) break;
            otish++;                                // right to left: the turtles
            almashdi = false;
            for (int i = ong; i > chap; i--) {
                if (a[i - 1] > a[i]) {
                    int t = a[i - 1]; a[i - 1] = a[i]; a[i] = t;
                    almashdi = true;
                }
            }
            chap++;
            if (!almashdi) break;
        }
        return otish;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        System.out.println(pufaksimon(a) + " " + kokteyl(a));
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int pufaksimon(vector<int> a) {                     // a is a copy
    int otish = 0;
    while (true) {
        otish++;
        bool almashdi = false;
        for (int i = 0; i < (int) a.size() - otish; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        if (!almashdi) return otish;                // a pass without swaps — sorted
    }
}

int kokteyl(vector<int> a) {
    int otish = 0, chap = 0, ong = (int) a.size() - 1;
    while (chap < ong) {
        otish++;                                    // left to right: the rabbits
        bool almashdi = false;
        for (int i = chap; i < ong; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        ong--;
        if (!almashdi) break;
        otish++;                                    // right to left: the turtles
        almashdi = false;
        for (int i = ong; i > chap; i--) {
            if (a[i - 1] > a[i]) {
                swap(a[i - 1], a[i]);
                almashdi = true;
            }
        }
        chap++;
        if (!almashdi) break;
    }
    return otish;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    cout << pufaksimon(a) << " " << kokteyl(a) << endl;
    return 0;
}
```

| Array | Bubble | Cocktail |
|---|---|---|
| `2 3 4 5 6 7 1` | 7 | 3 |
| `5 4 3 2 1` | 5 | 4 |
| `1 2 3 4` | 1 | 1 |

In the first row `1` is a turtle: bubble sort moves it one place per pass and needs 7 passes, while cocktail sort's first backward pass brings it to the front at once (*otish* is a pass, *almashdi* "swapped"). For a sorted array both algorithms stop after one checking pass.

## Worked example 2: quickselect — the k-th smallest element

The first line holds *n* and *k*, the second *n* numbers. Without fully sorting the array, find the *k*-th smallest element with Hoare partitioning (*k* = 1 is the smallest).

```python
n, k = map(int, input().split())
a = list(map(int, input().split()))
k -= 1                                  # a position counted from 0
chap, ong = 0, n - 1
while chap < ong:
    p = a[(chap + ong) // 2]            # Hoare partitioning
    i, j = chap - 1, ong + 1
    while True:
        i += 1
        while a[i] < p:
            i += 1
        j -= 1
        while a[j] > p:
            j -= 1
        if i >= j:
            break
        a[i], a[j] = a[j], a[i]
    if k <= j:                          # continue only into the part holding k
        ong = j
    else:
        chap = j + 1
print(a[chap])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt() - 1;     // a position counted from 0
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int chap = 0, ong = n - 1;
        while (chap < ong) {
            int p = a[(chap + ong) / 2];                // Hoare partitioning
            int i = chap - 1, j = ong + 1;
            while (true) {
                do i++; while (a[i] < p);
                do j--; while (a[j] > p);
                if (i >= j) break;
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
            if (k <= j) ong = j;                        // continue only into the part holding k
            else chap = j + 1;
        }
        System.out.println(a[chap]);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    k--;                                                // a position counted from 0
    vector<int> a(n);
    for (int& v : a) cin >> v;

    int chap = 0, ong = n - 1;
    while (chap < ong) {
        int p = a[(chap + ong) / 2];                    // Hoare partitioning
        int i = chap - 1, j = ong + 1;
        while (true) {
            do i++; while (a[i] < p);
            do j--; while (a[j] > p);
            if (i >= j) break;
            swap(a[i], a[j]);
        }
        if (k <= j) ong = j;                            // continue only into the part holding k
        else chap = j + 1;
    }
    cout << a[chap] << endl;
    return 0;
}
```

For `6 3` and `7 10 4 3 20 15` the output is `7` (sorted: 3 4 **7** 10 15 20); for `6 1` it is `3`. There is no recursion — a loop is used, since only one part remains each time. After Hoare partitioning no element of the left part is greater than any element of the right part, so the *k*-th element lies exactly in the part that contains *k*.

## Worked example 3: checking a sorting network with the 0-1 principle

The first line holds *n* (1 ≤ *n* ≤ 16) and the number of comparators *m*, the next *m* lines the comparators `i j` (from 0, *i* < *j*). Try all 2ⁿ 0-1 inputs (in lexicographic order): if the network sorts them all, print `yes`, otherwise print the first input it fails to sort.

```python
n, m = map(int, input().split())
tarmoq = [tuple(map(int, input().split())) for _ in range(m)]

javob = "yes"
for maska in range(2 ** n):
    kirish = [(maska >> (n - 1 - i)) & 1 for i in range(n)]    # a 0-1 sequence
    a = kirish[:]
    for i, j in tarmoq:
        if a[i] > a[j]:
            a[i], a[j] = a[j], a[i]
    if any(a[t] > a[t + 1] for t in range(n - 1)):            # not sorted
        javob = "".join(map(str, kirish))
        break
print(javob)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] tarmoq = new int[m][2];
        for (int[] t : tarmoq) {
            t[0] = in.nextInt();
            t[1] = in.nextInt();
        }

        String javob = "yes";
        for (int maska = 0; maska < (1 << n) && javob.equals("yes"); maska++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // a 0-1 sequence
            int[] kirish = a.clone();
            for (int[] t : tarmoq) {
                if (a[t[0]] > a[t[1]]) {
                    int x = a[t[0]]; a[t[0]] = a[t[1]]; a[t[1]] = x;
                }
            }
            for (int i = 0; i + 1 < n; i++) {
                if (a[i] > a[i + 1]) {                                     // not sorted
                    StringBuilder sb = new StringBuilder();
                    for (int b : kirish) sb.append(b);
                    javob = sb.toString();
                    break;
                }
            }
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> tarmoq(m);
    for (auto& [i, j] : tarmoq) cin >> i >> j;

    for (int maska = 0; maska < (1 << n); maska++) {
        vector<int> a(n);
        for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // a 0-1 sequence
        vector<int> kirish = a;
        for (auto [i, j] : tarmoq) {
            if (a[i] > a[j]) swap(a[i], a[j]);
        }
        for (int i = 0; i + 1 < n; i++) {
            if (a[i] > a[i + 1]) {                                     // not sorted
                for (int b : kirish) cout << b;
                cout << endl;
                return 0;
            }
        }
    }
    cout << "yes" << endl;
    return 0;
}
```

| Network (*n* = 4) | Result |
|---|---|
| (0,1) (2,3) (0,2) (1,3) (1,2) | `yes` — the optimal network for 4 elements |
| (0,1) (2,3) (0,2) (1,3) | `0101` — the last comparator (1,2) is missing |

In the second network the input `0101` should have become `0011`, but the two middle elements were never compared. The 0-1 principle guarantees that since the first network sorts all 16 0-1 inputs, it also sorts any 4 numbers correctly (*tarmoq* is the network, *maska* the bit mask, *kirish* the input). Taking the input from the bits of a binary number is the shortest way to enumerate all 0-1 sequences.

:::warning
**Common mistakes**

- **Re-checking the sorted tail in bubble sort.** After pass *k* the last *k* elements are in place; the inner loop runs up to `n − otish`.
- **Continuing into both parts in quickselect.** It turns into quick sort and becomes *O*(*n* log *n*); only the part holding *k*.
- **Mixing counting from 1 and from 0 in quickselect.** Turn the input *k* into `k − 1` at once.
- **Testing a network on a few random inputs only.** A mistake may show up on rare inputs; the 0-1 principle gives a complete check.
:::

:::tip
When testing quickselect, compare the result with a plain sort: `sorted(a)[k − 1]`. If both give the same answer on several random arrays, the algorithm works.
:::

## Summary

- In bubble sort small elements ("turtles") move slowly; cocktail sort fixes this with passes in both directions.
- Quickselect continues into only one part after Hoare partitioning and finds the *k*-th element in *O*(*n*) on average.
- The 0-1 principle: if a network sorts all 2ⁿ 0-1 inputs, it sorts any input.
- The ideas of these algorithms (partitioning, two-way passes, a data-independent network) are reused in other problems.

## Check yourself

:::check
Q: What is a "turtle" in bubble sort?
A: A small element near the end of the array: it moves only one place left per pass.
Q: Why is quickselect *O*(*n*) on average?
A: After each partitioning only one part remains: *n* + *n*/2 + *n*/4 + … ≈ 2*n*.
Q: How many inputs are tried to check an 8-input sorting network with the 0-1 principle?
A: 2⁸ = 256.
Q: Is cocktail sort faster than bubble sort for `6 5 4 3 2 1`?
A: Not noticeably: on a reversed array both are *O*(*n*²); cocktail sort wins on "nearly sorted" arrays with many turtles.
:::

:::link https://en.wikipedia.org/wiki/Quickselect
Quickselect
Hoare's selection algorithm and its complexity.
:::
