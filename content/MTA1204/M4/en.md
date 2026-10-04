---
title: Heap sort and Shell sort. Bubble sort and Hoare's sort
summary: The heap structure and the idea of heap sort, Shell sort and the effect of the gap sequence, bubble sort and its improved variant, Hoare's quick sort — partitioning, choosing the pivot, the average and the worst case.
---

## Why it matters

The simple sorting methods — bubble, selection, insertion — take *O*(*n*²) time and slow down already at 10⁵ elements. The three algorithms of this lecture solve that problem in different ways: **Shell** speeds up insertion sort by starting "from afar", **heap sort** uses a special structure — the heap — to give a guaranteed *O*(*n* log *n*), and **Hoare's quick sort** is in practice the fastest general-purpose sort and underlies many standard libraries.

Each algorithm has its own idea answering "how to be faster?" — understanding them teaches you to design new algorithms.

:::note
**After this lesson you will be able to:**

- explain bubble sort and its improved variant;
- show the idea of Shell sort and the effect of the gap sequence;
- explain the heap property and how a heap is kept in an array;
- explain the stages of heap sort and why it is *O*(*n* log *n*);
- write Hoare partitioning and show how the choice of pivot affects complexity.
:::

## Bubble sort

If neighbouring elements are in the wrong order, they are swapped; on every pass the largest element "bubbles up" to the end. *n* − 1 passes, *O*(*n*²) comparisons.

**The improved variant**: if a pass makes no swap at all, the array is sorted — the loop stops. Then a sorted array needs only one pass (*O*(*n*)). Bubble sort is stable but hardly used in practice: it is a teaching algorithm.

## Shell sort

**Shell sort** (Donald Shell, 1959) generalises insertion sort. Insertion sort moves an element only one position at a time, so a small element at the end takes long to reach the front. Shell's idea: first sort the elements standing *h* apart (*h*-sorting), then decrease *h*, finishing with *h* = 1 — an ordinary insertion sort. Large gaps quickly bring elements "roughly" into place.

| Gap sequence | Example (*n* = 100) | Worst case |
|---|---|---|
| Shell: *n*/2, *n*/4, …, 1 | 50, 25, 12, 6, 3, 1 | *O*(*n*²) |
| Knuth: (3ᵏ − 1)/2 | 40, 13, 4, 1 | *O*(*n*^1.5) |
| Sedgewick, Ciura and others | | better still |

Shell sort needs no extra memory and works well on medium-sized arrays; its exact complexity depends on the gaps and has not been fully analysed.

## The heap and heap sort

A **max-heap** is a complete binary tree in which every node's value is greater than or equal to its children's. It is kept in an array without pointers:

| Node | Index (from 0) |
|---|---|
| the root | 0 |
| the left child of *i* | 2*i* + 1 |
| the right child of *i* | 2*i* + 2 |
| the parent of *i* | (*i* − 1) / 2 |

The main operation is **sifting down**: if a node is smaller than its children, it is swapped with the larger child, and this continues downwards; *O*(log *n*). Building a heap from an array: sift down every node from the last inner node (*n*/2 − 1) up to the root — *O*(*n*) in total.

**Heap sort** (J. Williams, 1964):

```text
1. build a max-heap from the array
2. for last = n − 1 down to 1:
       swap a[0] (the largest) with a[last]
       sift a[0] down within 0..last−1
```

Every step puts the largest element in its place; *n* sift-downs × *O*(log *n*) = **a guaranteed *O*(*n* log *n*)**, with *O*(1) extra memory. Its drawbacks: it is not stable and is a little slower than quick sort in practice.

## Hoare's quick sort

**Quick sort** (Tony Hoare, 1960) is divide and conquer: a **pivot** element is chosen, the array is **partitioned** — elements less than or equal to the pivot go left, greater than or equal go right — and the two parts are sorted recursively.

**Hoare partitioning**: two pointers — *i* from the left looks for an element greater than or equal to the pivot, *j* from the right for one less than or equal; when found, they are swapped; partitioning ends when *i* ≥ *j*.

| Case | Complexity | When |
|---|---|---|
| Average | *O*(*n* log *n*) | the pivot splits the parts roughly equally |
| Worst | *O*(*n*²) | the pivot is always the smallest or the largest element |

Avoiding the worst case: take the pivot **from the middle**, **at random**, or as the "median of three" (the median of the first, middle and last elements). C++'s `std::sort` protects quick sort with heap sort (introsort), while Python and Java use Timsort, based on merge sort, for objects.

## Worked example 1: Shell sort and the gap sequence

*n* is given (1 ≤ *n* ≤ 1000). Sort the descending array `n, n−1, …, 1` in three ways and print the number of comparisons: insertion sort (*h* = 1), Shell (*n*/2, *n*/4, …) and Knuth's gaps (… 40, 13, 4, 1).

```python
def shell(a, qadamlar):
    a = a[:]
    taqqoslash = 0
    for h in qadamlar:
        for i in range(h, len(a)):          # h-sorting: insertion sort with step h
            joriy = a[i]
            j = i
            while j >= h:
                taqqoslash += 1
                if a[j - h] > joriy:
                    a[j] = a[j - h]
                    j -= h
                else:
                    break
            a[j] = joriy
    return taqqoslash


n = int(input())
a = list(range(n, 0, -1))

yarim = []
h = n // 2
while h > 0:
    yarim.append(h)
    h //= 2

knut = []
h = 1
while h < n // 3:
    h = 3 * h + 1
while h >= 1:
    knut.append(h)
    h //= 3

print(shell(a, [1]), shell(a, yarim), shell(a, knut))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long shell(int[] asl, List<Integer> qadamlar) {
        int[] a = asl.clone();
        long taqqoslash = 0;
        for (int h : qadamlar) {
            for (int i = h; i < a.length; i++) {    // h-sorting: insertion sort with step h
                int joriy = a[i];
                int j = i;
                while (j >= h) {
                    taqqoslash++;
                    if (a[j - h] > joriy) {
                        a[j] = a[j - h];
                        j -= h;
                    } else {
                        break;
                    }
                }
                a[j] = joriy;
            }
        }
        return taqqoslash;
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = n - i;

        List<Integer> yarim = new ArrayList<>();
        for (int h = n / 2; h > 0; h /= 2) yarim.add(h);

        List<Integer> knut = new ArrayList<>();
        int h = 1;
        while (h < n / 3) h = 3 * h + 1;
        for (; h >= 1; h /= 3) knut.add(h);

        System.out.println(shell(a, List.of(1)) + " " + shell(a, yarim) + " " + shell(a, knut));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

long long shell(vector<int> a, const vector<int>& qadamlar) {   // a is a copy
    long long taqqoslash = 0;
    for (int h : qadamlar) {
        for (int i = h; i < (int) a.size(); i++) {  // h-sorting: insertion sort with step h
            int joriy = a[i];
            int j = i;
            while (j >= h) {
                taqqoslash++;
                if (a[j - h] > joriy) {
                    a[j] = a[j - h];
                    j -= h;
                } else {
                    break;
                }
            }
            a[j] = joriy;
        }
    }
    return taqqoslash;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) a[i] = n - i;

    vector<int> yarim;
    for (int h = n / 2; h > 0; h /= 2) yarim.push_back(h);

    vector<int> knut;
    int h = 1;
    while (h < n / 3) h = 3 * h + 1;
    for (; h >= 1; h /= 3) knut.push_back(h);

    cout << shell(a, {1}) << " " << shell(a, yarim) << " " << shell(a, knut) << endl;
    return 0;
}
```

| *n* | Insertion sort | Shell (*n*/2) | Knuth |
|---|---|---|---|
| 10 | 45 | 27 | 21 |
| 100 | 4,950 | 668 | 500 |
| 1000 | 499,500 | 11,716 | 8,550 |

A reversed array is the worst case for insertion sort: *n*(*n* − 1)/2 comparisons. Shell sort uses the same inner loop, yet thanks to the large gaps does 40–60 times less work on 1000 elements (*qadamlar* are the gaps, *yarim* the halving sequence, *knut* Knuth's). Knuth's gaps beat *n*/2 too — choosing the gap sequence is the most important part of the algorithm.

## Worked example 2: Hoare partitioning and choosing the pivot

The first line holds *n*, the second *n* numbers. Sort the array with quick sort using Hoare partitioning; print the sorted array, then the number of comparisons made when the pivot is the first element and when it is the middle element.

```python
import sys

sys.setrecursionlimit(10000)


def tez_saralash(asl, tayanch_turi):
    a = asl[:]
    taqqoslash = 0

    def ajrat(chap, ong):
        nonlocal taqqoslash
        p = a[chap] if tayanch_turi == "birinchi" else a[(chap + ong) // 2]
        i, j = chap - 1, ong + 1
        while True:
            while True:                 # from the left: an element >= p
                i += 1
                taqqoslash += 1
                if a[i] >= p:
                    break
            while True:                 # from the right: an element <= p
                j -= 1
                taqqoslash += 1
                if a[j] <= p:
                    break
            if i >= j:
                return j
            a[i], a[j] = a[j], a[i]

    def saralash(chap, ong):
        if chap < ong:
            m = ajrat(chap, ong)
            saralash(chap, m)
            saralash(m + 1, ong)

    saralash(0, len(a) - 1)
    return a, taqqoslash


n = int(input())
a = list(map(int, input().split()))
natija, t1 = tez_saralash(a, "birinchi")
_, t2 = tez_saralash(a, "orta")
print(*natija)
print(t1, t2)
```
```java
import java.util.Scanner;

public class Main {
    static int[] a;
    static long taqqoslash;
    static boolean ortadan;

    static int ajrat(int chap, int ong) {
        int p = ortadan ? a[(chap + ong) / 2] : a[chap];
        int i = chap - 1, j = ong + 1;
        while (true) {
            do { i++; taqqoslash++; } while (a[i] < p);     // from the left: an element >= p
            do { j--; taqqoslash++; } while (a[j] > p);     // from the right: an element <= p
            if (i >= j) return j;
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    static void saralash(int chap, int ong) {
        if (chap < ong) {
            int m = ajrat(chap, ong);
            saralash(chap, m);
            saralash(m + 1, ong);
        }
    }

    static long tezSaralash(int[] asl, boolean ortadanTayanch) {
        a = asl.clone();
        taqqoslash = 0;
        ortadan = ortadanTayanch;
        saralash(0, a.length - 1);
        return taqqoslash;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] asl = new int[n];
        for (int i = 0; i < n; i++) asl[i] = in.nextInt();

        long t1 = tezSaralash(asl, false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        long t2 = tezSaralash(asl, true);
        System.out.println(sb);
        System.out.println(t1 + " " + t2);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

long long taqqoslash;
bool ortadan;

int ajrat(vector<int>& a, int chap, int ong) {
    int p = ortadan ? a[(chap + ong) / 2] : a[chap];
    int i = chap - 1, j = ong + 1;
    while (true) {
        do { i++; taqqoslash++; } while (a[i] < p);     // from the left: an element >= p
        do { j--; taqqoslash++; } while (a[j] > p);     // from the right: an element <= p
        if (i >= j) return j;
        swap(a[i], a[j]);
    }
}

void saralash(vector<int>& a, int chap, int ong) {
    if (chap < ong) {
        int m = ajrat(a, chap, ong);
        saralash(a, chap, m);
        saralash(a, m + 1, ong);
    }
}

long long tezSaralash(vector<int>& a, bool ortadanTayanch) {
    taqqoslash = 0;
    ortadan = ortadanTayanch;
    saralash(a, 0, (int) a.size() - 1);
    return taqqoslash;
}

int main() {
    int n;
    cin >> n;
    vector<int> asl(n);
    for (int& v : asl) cin >> v;

    vector<int> a1 = asl, a2 = asl;
    long long t1 = tezSaralash(a1, false);
    long long t2 = tezSaralash(a2, true);
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a1[i];
    cout << endl << t1 << " " << t2 << endl;
    return 0;
}
```

For `10` and `35 33 42 10 14 19 27 44 26 31` the output is `10 14 19 26 27 31 33 35 42 44` and `55 58` — on a random order both pivots are almost the same. But on the **sorted** array `1 2 … 20` the first-element pivot takes 228 comparisons and the middle one 107; on 200 elements, 20,298 and 1,743. In a sorted array the first element is always the smallest — partitioning splits off a single element and the algorithm degrades to *O*(*n*²). A middle pivot saves it from this trap (*tayanch* is the pivot, *ajrat* partition).

## Worked example 3: building a max-heap from an array

The first line holds *n*, the second *n* numbers. Turn the array into a max-heap in place (sifting down from the last inner node to the root) and print the resulting array.

```python
def choktir(a, i, n):
    while True:
        chap = 2 * i + 1
        if chap >= n:                       # no child — a leaf
            return
        katta = chap
        if chap + 1 < n and a[chap + 1] > a[chap]:
            katta = chap + 1                # the larger child
        if a[i] >= a[katta]:                # the heap property holds
            return
        a[i], a[katta] = a[katta], a[i]
        i = katta


n = int(input())
a = list(map(int, input().split()))
for i in range(n // 2 - 1, -1, -1):         # from the last inner node up to the root
    choktir(a, i, n)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    static void choktir(int[] a, int i, int n) {
        while (true) {
            int chap = 2 * i + 1;
            if (chap >= n) return;                      // no child — a leaf
            int katta = chap;
            if (chap + 1 < n && a[chap + 1] > a[chap]) katta = chap + 1;   // the larger child
            if (a[i] >= a[katta]) return;               // the heap property holds
            int t = a[i]; a[i] = a[katta]; a[katta] = t;
            i = katta;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        for (int i = n / 2 - 1; i >= 0; i--) choktir(a, i, n);   // from the last inner node up to the root
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

void choktir(vector<int>& a, int i, int n) {
    while (true) {
        int chap = 2 * i + 1;
        if (chap >= n) return;                          // no child — a leaf
        int katta = chap;
        if (chap + 1 < n && a[chap + 1] > a[chap]) katta = chap + 1;   // the larger child
        if (a[i] >= a[katta]) return;                   // the heap property holds
        swap(a[i], a[katta]);
        i = katta;
    }
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    for (int i = n / 2 - 1; i >= 0; i--) choktir(a, i, n);   // from the last inner node up to the root
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl;
    return 0;
}
```

For `7` and `4 10 3 5 1 8 9` the output is `10 5 9 4 1 8 3`. As a tree: the root is 10, its children 5 and 9, the children of 5 are 4 and 1, those of 9 are 8 and 3 — every parent is larger than its children. The largest element is now in `a[0]`: heap sort starts from here — what remains is to swap it with the last element and sift the root down again (you do this in the practical assignment). Building a heap is *O*(*n*): most of the lower nodes are leaves, and sifting them down is almost free (*choktir* means sift down, *katta* the larger).

:::warning
**Common mistakes**

- **Always taking the first element as quick sort's pivot.** On sorted or nearly sorted data it gives *O*(*n*²) and deep recursion; take a middle or random pivot.
- **Counting heap indices from 1.** With indices from 0 the children are 2*i* + 1 and 2*i* + 2; from 1 they are 2*i* and 2*i* + 1 — do not mix them up.
- **Swapping with the smaller child when sifting down.** Only swapping with the **larger** child keeps the heap property.
- **Ending Shell sort without *h* = 1.** The last gap must be 1, otherwise the array is not fully sorted.
:::

:::tip
Draw the heap on paper as a tree: write the array indices on the nodes and show every sift-down with an arrow. Checking the heap property in tree form is much easier than in the array.
:::

## Summary

- Bubble sort is *O*(*n*²); stopping after a pass without swaps finishes a sorted array in *O*(*n*).
- Shell sort starts insertion sort with large gaps; its efficiency depends on the gap sequence.
- A max-heap is kept in an array (children at 2*i* + 1, 2*i* + 2); building it is *O*(*n*), sifting down *O*(log *n*).
- Heap sort gives a guaranteed *O*(*n* log *n*) with *O*(1) extra memory, but it is not stable.
- Hoare's quick sort is *O*(*n* log *n*) on average and *O*(*n*²) in the worst case; choosing the pivot well avoids the worst case.

## Check yourself

:::check
Q: How does Shell sort differ from insertion sort?
A: It first sorts elements standing *h* apart and decreases *h* down to 1; elements approach their places quickly in large jumps.
Q: In a heap indexed from 0, at which indices are the children of the node at index 4?
A: 9 and 10.
Q: Why does quick sort slow down on a sorted array when the first element is the pivot?
A: The pivot is always the smallest element, and partitioning splits the array into parts of 1 and *n* − 1 — *O*(*n*²).
Q: What is the advantage of heap sort over quick sort?
A: *O*(*n* log *n*) is guaranteed even in the worst case, and the extra memory is *O*(1).
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: sorting algorithms
An animation of Shell sort, heap sort and quick sort.
:::

:::link https://en.wikipedia.org/wiki/Heapsort
Heapsort
The algorithm, building the heap and the complexity analysis.
:::
