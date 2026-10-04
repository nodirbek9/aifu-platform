---
title: Sorting algorithms. Internal and external sorting. Heap sort and Shell sort (practical session)
summary: Merge sort and its number of comparisons, comparison-free counting sort and its linear time, ordering by several keys with a stable sort; choosing a sorting algorithm for the problem.
---

## Why it matters

In the lectures we saw a classification of sorting algorithms. In practice the question is concrete: which algorithm is best for my data? The answer depends on the data: the number of elements, the range of values, whether stability is required, memory limits. For example, sorting 10⁶ students by age does not even need *O*(*n* log *n*) — since age runs from 0 to 120, *O*(*n*) is enough.

In this session we look at three practical methods: merge sort (the basis of external sorting), comparison-free counting sort, and ordering by several keys with a stable sort.

:::note
**After this session you will be able to:**

- write merge sort recursively and measure its number of comparisons;
- apply counting sort when the range of values is small;
- order by several keys by applying stable sorts in succession;
- choose a sorting algorithm for the conditions of a problem.
:::

## Merge sort

**Merge sort** (J. von Neumann, 1945): the array is split in two, each half is sorted recursively, then the two sorted halves are **merged** — at every step the smaller of the two halves' current elements is written to the result.

| Feature | Value |
|---|---|
| Time (in every case) | *O*(*n* log *n*) |
| Extra memory | *O*(*n*) |
| Stability | yes (with equal elements the left one is taken first) |
| Reading the data | sequential — which suits external sorting and linked lists |

## Counting sort

A comparison-based sort cannot be faster than Ω(*n* log *n*). But if the values lie **in a small range** (0 … *k*), no comparison is needed at all: count how many times each value occurs and write the result from the counts. **Counting sort** takes *O*(*n* + *k*) time and *O*(*k*) memory.

| When useful | When it does not fit |
|---|---|
| age, grade, month number, an ASCII character | a very large range of values (say, 10⁹) |
| *k* roughly *n* or smaller | real numbers, strings |

The idea of counting sort underlies **radix sort**: large numbers are sorted digit by digit, each time with a stable counting sort.

## Sorting by several keys

"First by group, and within a group by score in descending order" — such an order can be obtained in two ways:

1. **A combined key**: one sort whose comparison function compares the group first and, if equal, the score.
2. **Applying stable sorts in succession**: first a stable sort by the **secondary** key (the score), then by the **primary** key (the group). The second sort keeps the order of the first within equal groups.

The second way is how "click a column header to sort" works in tables: the column clicked last becomes the primary key. It works correctly only with a **stable** sort.

## Worked example 1: merge sort

The first line holds *n*, the second *n* numbers. Sort them with merge sort; print the sorted array and the number of comparisons made while merging.

```python
import sys

sys.setrecursionlimit(10000)
taqqoslash = 0


def birlashtirib_saralash(a):
    global taqqoslash
    if len(a) <= 1:
        return a
    orta = len(a) // 2
    chap = birlashtirib_saralash(a[:orta])
    ong = birlashtirib_saralash(a[orta:])
    natija = []
    i = j = 0
    while i < len(chap) and j < len(ong):
        taqqoslash += 1
        if chap[i] <= ong[j]:           # <= keeps the sort stable
            natija.append(chap[i])
            i += 1
        else:
            natija.append(ong[j])
            j += 1
    natija.extend(chap[i:])             # the remaining part
    natija.extend(ong[j:])
    return natija


n = int(input())
a = list(map(int, input().split()))
print(*birlashtirib_saralash(a))
print(taqqoslash)
```
```java
import java.util.Scanner;

public class Main {
    static long taqqoslash = 0;

    static void saralash(int[] a, int[] vaqtincha, int chap, int ong) {    // [chap, ong)
        if (ong - chap <= 1) return;
        int orta = (chap + ong) / 2;
        saralash(a, vaqtincha, chap, orta);
        saralash(a, vaqtincha, orta, ong);
        int i = chap, j = orta, k = chap;
        while (i < orta && j < ong) {
            taqqoslash++;
            if (a[i] <= a[j]) vaqtincha[k++] = a[i++];      // <= keeps the sort stable
            else vaqtincha[k++] = a[j++];
        }
        while (i < orta) vaqtincha[k++] = a[i++];           // the remaining part
        while (j < ong) vaqtincha[k++] = a[j++];
        for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        saralash(a, new int[n], 0, n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(taqqoslash);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

long long taqqoslash = 0;

void saralash(vector<int>& a, vector<int>& vaqtincha, int chap, int ong) {   // [chap, ong)
    if (ong - chap <= 1) return;
    int orta = (chap + ong) / 2;
    saralash(a, vaqtincha, chap, orta);
    saralash(a, vaqtincha, orta, ong);
    int i = chap, j = orta, k = chap;
    while (i < orta && j < ong) {
        taqqoslash++;
        if (a[i] <= a[j]) vaqtincha[k++] = a[i++];          // <= keeps the sort stable
        else vaqtincha[k++] = a[j++];
    }
    while (i < orta) vaqtincha[k++] = a[i++];               // the remaining part
    while (j < ong) vaqtincha[k++] = a[j++];
    for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n), vaqtincha(n);
    for (int& v : a) cin >> v;
    saralash(a, vaqtincha, 0, n);
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl << taqqoslash << endl;
    return 0;
}
```

For `7` and `38 27 43 3 9 82 10` the output is `3 9 10 27 38 43 82` and `13`. For sorted and reversed 8-element arrays the number of comparisons is 12 too — merge sort is almost indifferent to the input order (*birlashtirib saralash* is merge sort, *vaqtincha* a temporary array). In the Java and C++ versions one `vaqtincha` array is allocated in advance and reused by all merges — creating a new array in every call would be slower.

## Worked example 2: counting sort

The first line holds *n*, the second the ages of *n* students (0 to 120). Sort the ages with comparison-free counting sort; print the sorted list and the most frequent age (the smallest if there are several) with its count.

```python
n = int(input())
yoshlar = list(map(int, input().split()))

sanoq = [0] * 121                       # how many times each age occurs
for y in yoshlar:
    sanoq[y] += 1

natija = []
for y in range(121):                    # the ages in ascending order
    natija.extend([y] * sanoq[y])

eng_kop = max(range(121), key=lambda y: (sanoq[y], -y))
print(*natija)
print(eng_kop, sanoq[eng_kop])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] sanoq = new int[121];                         // how many times each age occurs
        for (int i = 0; i < n; i++) sanoq[in.nextInt()]++;

        StringBuilder sb = new StringBuilder();
        int engKop = 0;
        for (int y = 0; y <= 120; y++) {                    // the ages in ascending order
            for (int k = 0; k < sanoq[y]; k++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(y);
            }
            if (sanoq[y] > sanoq[engKop]) engKop = y;
        }
        System.out.println(sb);
        System.out.println(engKop + " " + sanoq[engKop]);
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
    vector<int> sanoq(121, 0);                              // how many times each age occurs
    for (int i = 0; i < n; i++) {
        int y;
        cin >> y;
        sanoq[y]++;
    }

    bool birinchi = true;
    int engKop = 0;
    for (int y = 0; y <= 120; y++) {                        // the ages in ascending order
        for (int k = 0; k < sanoq[y]; k++) {
            cout << (birinchi ? "" : " ") << y;
            birinchi = false;
        }
        if (sanoq[y] > sanoq[engKop]) engKop = y;
    }
    cout << endl << engKop << " " << sanoq[engKop] << endl;
    return 0;
}
```

For `8` and `19 21 19 20 22 19 21 18` the output is `18 19 19 19 20 21 21 22` and `19 3`. The algorithm has not a single comparison (`<`) of elements: every age is directly an index of the `sanoq` (count) array. For 10⁶ students that is 10⁶ + 121 operations — faster than any comparison sort. The most frequent value comes for free from the same counts — an extra benefit of counting sort (*yoshlar* are the ages, *eng_kop* the most frequent).

## Worked example 3: ordering by several keys with a stable sort

The first line holds *n*, the next *n* lines a student's name, group number and score. Print the students by group in ascending order and within a group by score in descending order; students with equal scores keep their input order. Do it with two stable sorts: first by score, then by group.

```python
n = int(input())
talabalar = []
for _ in range(n):
    ism, guruh, ball = input().split()
    talabalar.append((ism, int(guruh), int(ball)))

talabalar.sort(key=lambda t: -t[2])     # 1) the secondary key: the score (descending)
talabalar.sort(key=lambda t: t[1])      # 2) the primary key: the group — stable!

for ism, guruh, ball in talabalar:
    print(guruh, ism, ball)
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    record Talaba(String ism, int guruh, int ball) {}

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt(), in.nextInt()));

        talabalar.sort(Comparator.comparingInt(Talaba::ball).reversed());   // 1) the secondary key: the score (descending)
        talabalar.sort(Comparator.comparingInt(Talaba::guruh));             // 2) the primary key: the group — stable!

        for (Talaba t : talabalar) System.out.println(t.guruh() + " " + t.ism() + " " + t.ball());
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Talaba {
    string ism;
    int guruh, ball;
};

int main() {
    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.guruh >> t.ball;

    stable_sort(talabalar.begin(), talabalar.end(),          // 1) the secondary key: the score (descending)
                [](const Talaba& x, const Talaba& y) { return x.ball > y.ball; });
    stable_sort(talabalar.begin(), talabalar.end(),          // 2) the primary key: the group — stable!
                [](const Talaba& x, const Talaba& y) { return x.guruh < y.guruh; });

    for (const auto& t : talabalar) cout << t.guruh << " " << t.ism << " " << t.ball << endl;
    return 0;
}
```

Input:

```text
6
Aziz 102 85
Malika 101 92
Bekzod 102 92
Dilnoza 101 85
Anvar 102 85
Sardor 101 70
```

Output:

```text
101 Malika 92
101 Dilnoza 85
101 Sardor 70
102 Bekzod 92
102 Aziz 85
102 Anvar 85
```

After the first sort the students are ordered by score. The second sort separates the groups, but being **stable**, it does not break the score order within each group. Aziz and Anvar in group 102 have equal scores (85) — they kept their input order. In C++ it matters to use `stable_sort` rather than `sort`: plain `sort` is not stable, and the result could be wrong.

:::warning
**Common mistakes**

- **Writing `<` when merging.** With equal elements the right one is taken first and stability is broken; write `<=`.
- **Applying counting sort to a large range.** With values up to 10⁹ the count array does not fit in memory; a comparison sort or radix sort is needed.
- **Reversing the order in a multi-key sort.** If you sort by the **primary** key first, the second sort destroys it; secondary first, then primary.
- **Expecting stability from `sort` in C++.** If a stable order is needed — `stable_sort`.
:::

:::tip
When choosing a sorting algorithm, answer three questions: does the data fit in memory (if not — external merge sort)? Is the range of values small (if so — counting sort)? Does the order of equal elements matter (if so — a stable sort)?
:::

## Summary

- Merge sort is always *O*(*n* log *n*), stable, needs *O*(*n*) extra memory and reads data sequentially.
- Counting sort runs in *O*(*n* + *k*) without comparisons — when the range of values is small.
- A multi-key order: a stable sort by the secondary key first, then by the primary key.
- An algorithm is chosen by data size, range of values and the stability requirement.

## Check yourself

:::check
Q: Why does merge sort suit external sorting?
A: It reads and writes data sequentially; no random disk access is needed.
Q: Why can counting sort be faster than the Ω(*n* log *n*) bound?
A: It does not compare elements with one another — the bound applies only to comparison-based sorts.
Q: If a table is stably sorted first by the "name" column and then by "group", which key is primary?
A: "Group" — the last sort; within a group the order by name is kept.
Q: Which algorithm is fastest for sorting 5 · 10⁶ grades (0 to 100)?
A: Counting sort: *O*(*n* + 101).
:::

:::link https://en.wikipedia.org/wiki/Counting_sort
Counting sort
The algorithm, its complexity and its link to radix sort.
:::
