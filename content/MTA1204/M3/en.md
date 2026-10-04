---
title: Fibonacci search. Searching by hashing. Internal and external sorting
summary: The Fibonacci search algorithm and its complexity; the hash function, the hash table, collisions and the chaining method, the load factor; a classification of sorting algorithms, internal and external sorting, k-way merging of sorted runs.
---

## Why it matters

In the previous lecture we saw that binary search in a sorted array takes *O*(log *n*) steps. That is very fast, but two questions remain. First: can we search even faster? Yes — **hashing** finds a key in *O*(1) on average, which is why dictionaries, database indexes and compilers' symbol tables are built on it. Second: what if the data does not fit in main memory? Sorting not a million but billions of records needs **external sorting**.

In this lecture we look at Fibonacci search, hash tables and a general classification of sorting algorithms.

:::note
**After this lesson you will be able to:**

- explain the idea of Fibonacci search and write it;
- explain the notions of a hash function, a hash table and a collision;
- build a simple hash table with the chaining method;
- classify sorting algorithms by stability, memory and complexity;
- explain the difference between internal and external sorting, and k-way merging.
:::

## Fibonacci search

**Fibonacci search** is an "add-and-subtract" variant of binary search: it splits the range not in the middle but in the ratio of consecutive Fibonacci numbers. The smallest Fibonacci number *F*(*k*) ≥ *n* is found, and at every step the position checked is `offset + F(k − 2)`; depending on the answer, the range shrinks to *F*(*k* − 1) or *F*(*k* − 2).

| Feature | Binary search | Fibonacci search |
|---|---|---|
| Split point | the middle (`/ 2`) | the Fibonacci ratio (≈ 0.618) |
| Operations | division | only addition and subtraction |
| Complexity | *O*(log *n*) | *O*(log *n*) |
| When useful | ordinary cases | on devices where division is expensive; in memory read sequentially (accesses to nearby positions) |

## Searching by hashing

A **hash function** turns a key (a number, a string) into a table index: *h*(*key*) ∈ {0, …, *M* − 1}. A **hash table** is an array of *M* "buckets"; a key is written into its bucket and is later searched for in exactly that bucket.

A common hash for strings is the **polynomial hash** (Horner's scheme):

```text
h = 0
for every character c: h = (h · 31 + code(c)) mod M
```

When two different keys land in the same bucket, it is called a **collision**. Ways of resolving them:

| Method | Idea |
|---|---|
| **Chaining** | every bucket is a list; colliding keys are added to that list |
| **Open addressing** | if a bucket is taken, the next free bucket is searched for (linear or quadratic probing) |

The **load factor** α = *n* / *M* (the number of keys / the number of buckets). With chaining the average search time is *O*(1 + α). If α grows too large (say, above 0.75), the table is enlarged and the keys are rehashed. In the worst case (all keys in one bucket) a search becomes *O*(*n*) — which is why a good hash function must spread the keys evenly.

Python's `dict`/`set`, Java's `HashMap`/`HashSet` and C++'s `unordered_map`/`unordered_set` are exactly hash tables.

## A classification of sorting algorithms

| Algorithm | Average | Worst | Extra memory | Stable |
|---|---|---|---|---|
| Bubble | *O*(*n*²) | *O*(*n*²) | *O*(1) | yes |
| Selection | *O*(*n*²) | *O*(*n*²) | *O*(1) | no |
| Insertion | *O*(*n*²) | *O*(*n*²) | *O*(1) | yes |
| Shell | ≈ *O*(*n*^1.3) | *O*(*n*²) | *O*(1) | no |
| Merge | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(*n*) | yes |
| Heap | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(1) | no |
| Quick (Hoare) | *O*(*n* log *n*) | *O*(*n*²) | *O*(log *n*) | no |

Any comparison-based sort needs at least Ω(*n* log *n*) comparisons in the worst case — this is a proven lower bound.

## Internal and external sorting

**Internal sorting** — all the data sits in main memory (RAM); the algorithms above are like this. **External sorting** — the data is on disk and does not fit in memory (say, a 100 GB log file with 8 GB of RAM). The standard method is **external merge sort**:

1. Read the file in chunks that fit in memory, sort each chunk with an internal sort and write it to a separate file (a "sorted run").
2. **k-way merge** all the runs at once: take the first element of every run, write the smallest to the result, and take the next one from that run. A **heap** (a priority queue) is used to find the smallest quickly — each step is *O*(log *k*).

In external sorting the main cost is disk traffic, so the algorithm is designed to read the data **sequentially** and as few times as possible.

## Worked example 1: Fibonacci search

The first line holds *n*, the second *n* numbers in ascending order, the third *x*. Print the position of *x* (from 1; −1 if absent) and the number of checks.

```python
def fibonachchi_izlash(a, x):
    n = len(a)
    f2, f1 = 0, 1                       # F(k-2), F(k-1)
    f = f1 + f2                         # F(k)
    while f < n:
        f2, f1 = f1, f
        f = f1 + f2
    siljish = -1
    qadam = 0
    while f > 1:
        i = min(siljish + f2, n - 1)
        qadam += 1
        if a[i] < x:                    # the right part: of length F(k-1)
            f, f1 = f1, f2
            f2 = f - f1
            siljish = i
        elif a[i] > x:                  # the left part: of length F(k-2)
            f, f1 = f2, f1 - f2
            f2 = f - f1
        else:
            return i + 1, qadam
    if f1 == 1 and siljish + 1 < n:     # one element may be left
        qadam += 1
        if a[siljish + 1] == x:
            return siljish + 2, qadam
    return -1, qadam


n = int(input())
a = list(map(int, input().split()))
x = int(input())
print(*fibonachchi_izlash(a, x))
```
```java
import java.util.Scanner;

public class Main {
    // the result: {position (from 1, or -1), steps}
    static int[] fibonachchiIzlash(int[] a, int x) {
        int n = a.length;
        int f2 = 0, f1 = 1;                 // F(k-2), F(k-1)
        int f = f1 + f2;                    // F(k)
        while (f < n) {
            f2 = f1;
            f1 = f;
            f = f1 + f2;
        }
        int siljish = -1, qadam = 0;
        while (f > 1) {
            int i = Math.min(siljish + f2, n - 1);
            qadam++;
            if (a[i] < x) {                 // the right part: of length F(k-1)
                f = f1;
                f1 = f2;
                f2 = f - f1;
                siljish = i;
            } else if (a[i] > x) {          // the left part: of length F(k-2)
                f = f2;
                f1 = f1 - f2;
                f2 = f - f1;
            } else {
                return new int[] {i + 1, qadam};
            }
        }
        if (f1 == 1 && siljish + 1 < n) {   // one element may be left
            qadam++;
            if (a[siljish + 1] == x) return new int[] {siljish + 2, qadam};
        }
        return new int[] {-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int[] r = fibonachchiIzlash(a, in.nextInt());
        System.out.println(r[0] + " " + r[1]);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

// the result: {position (from 1, or -1), steps}
pair<int, int> fibonachchiIzlash(const vector<int>& a, int x) {
    int n = a.size();
    int f2 = 0, f1 = 1;                     // F(k-2), F(k-1)
    int f = f1 + f2;                        // F(k)
    while (f < n) {
        f2 = f1;
        f1 = f;
        f = f1 + f2;
    }
    int siljish = -1, qadam = 0;
    while (f > 1) {
        int i = min(siljish + f2, n - 1);
        qadam++;
        if (a[i] < x) {                     // the right part: of length F(k-1)
            f = f1;
            f1 = f2;
            f2 = f - f1;
            siljish = i;
        } else if (a[i] > x) {              // the left part: of length F(k-2)
            f = f2;
            f1 = f1 - f2;
            f2 = f - f1;
        } else {
            return {i + 1, qadam};
        }
    }
    if (f1 == 1 && siljish + 1 < n) {       // one element may be left
        qadam++;
        if (a[siljish + 1] == x) return {siljish + 2, qadam};
    }
    return {-1, qadam};
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    int x;
    cin >> x;
    auto [orin, qadam] = fibonachchiIzlash(a, x);
    cout << orin << " " << qadam << endl;
    return 0;
}
```

For the 11-element array `10 22 35 40 45 50 80 82 85 90 100`: *x* = 85 → `9 4`, *x* = 45 → `5 1` (found on the first check: *F*(7) = 13 ≥ 11, the first position is 0 + *F*(5) = 5), *x* = 23 → `-1 4`. The code has no division at all — only addition and subtraction (*siljish* is the offset, *qadam* a step). The final `if` checks the single element that may remain in the range.

## Worked example 2: a hash table with chaining

The first line holds words (the keys), the second query words. Build a hash table of *M* = 7 buckets (a polynomial hash with base 31; a repeated word is not added again). Print the length of every bucket, then for every query `found` or `absent` and the number of comparisons made in the bucket.

```python
M = 7


def xesh(soz):
    h = 0
    for c in soz:
        h = (h * 31 + ord(c)) % M       # Horner's scheme
    return h


jadval = [[] for _ in range(M)]         # every bucket is a list (a chain)
for soz in input().split():
    savat = jadval[xesh(soz)]
    if soz not in savat:
        savat.append(soz)

print(*[len(savat) for savat in jadval])
for soz in input().split():
    taqqoslash = 0
    topildi = False
    for s in jadval[xesh(soz)]:         # only one bucket is examined
        taqqoslash += 1
        if s == soz:
            topildi = True
            break
    print(soz, "found" if topildi else "absent", taqqoslash)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final int M = 7;

    static int xesh(String soz) {
        int h = 0;
        for (char c : soz.toCharArray()) h = (h * 31 + c) % M;   // Horner's scheme
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<List<String>> jadval = new ArrayList<>();          // every bucket is a list (a chain)
        for (int i = 0; i < M; i++) jadval.add(new ArrayList<>());
        for (String soz : in.nextLine().trim().split("\\s+")) {
            List<String> savat = jadval.get(xesh(soz));
            if (!savat.contains(soz)) savat.add(soz);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < M; i++) {
            if (i > 0) sb.append(' ');
            sb.append(jadval.get(i).size());
        }
        System.out.println(sb);

        for (String soz : in.nextLine().trim().split("\\s+")) {
            int taqqoslash = 0;
            boolean topildi = false;
            for (String s : jadval.get(xesh(soz))) {           // only one bucket is examined
                taqqoslash++;
                if (s.equals(soz)) {
                    topildi = true;
                    break;
                }
            }
            System.out.println(soz + " " + (topildi ? "found" : "absent") + " " + taqqoslash);
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <list>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const int M = 7;

int xesh(const string& soz) {
    int h = 0;
    for (unsigned char c : soz) h = (h * 31 + c) % M;      // Horner's scheme
    return h;
}

int main() {
    vector<list<string>> jadval(M);                         // every bucket is a list (a chain)
    string satr, soz;
    getline(cin, satr);
    istringstream kalitlar(satr);
    while (kalitlar >> soz) {
        auto& savat = jadval[xesh(soz)];
        if (find(savat.begin(), savat.end(), soz) == savat.end()) savat.push_back(soz);
    }

    for (int i = 0; i < M; i++) cout << (i ? " " : "") << jadval[i].size();
    cout << endl;

    getline(cin, satr);
    istringstream sorovlar(satr);
    while (sorovlar >> soz) {
        int taqqoslash = 0;
        bool topildi = false;
        for (const string& s : jadval[xesh(soz)]) {         // only one bucket is examined
            taqqoslash++;
            if (s == soz) {
                topildi = true;
                break;
            }
        }
        cout << soz << " " << (topildi ? "found" : "absent") << " " << taqqoslash << endl;
    }
    return 0;
}
```

Input:

```text
olma nok uzum anor shaftoli gilos olcha behi
anor banan behi
```

Output:

```text
0 1 1 2 1 2 1
anor found 1
banan absent 1
behi found 2
```

8 words spread over 7 buckets: two buckets hold 2 words each (collisions) and one is empty. Every query looks at **only one bucket** — not at all 8 words (*jadval* is the table, *savat* a bucket, *taqqoslash* a comparison). The load factor is α = 8/7 ≈ 1.14, so one or two comparisons are enough on average. `banan` is not in the table, but its bucket holds another word — hence 1 comparison. Real systems make *M* a prime and enlarge the table as it fills.

## Worked example 3: k-way merging of sorted runs

The first line holds *k*, the next *k* lines sorted runs: first the length, then the elements. Merge all the runs into one sorted sequence — this is exactly the second stage of external sorting.

```python
import heapq

k = int(input())
seriyalar = []
for _ in range(k):
    q = list(map(int, input().split()))
    seriyalar.append(q[1:])

uyum = []                                   # (value, run, position)
for s in range(k):
    if seriyalar[s]:
        heapq.heappush(uyum, (seriyalar[s][0], s, 0))

natija = []
while uyum:
    qiymat, s, i = heapq.heappop(uyum)      # the smallest current element
    natija.append(qiymat)
    if i + 1 < len(seriyalar[s]):
        heapq.heappush(uyum, (seriyalar[s][i + 1], s, i + 1))
print(*natija)
```
```java
import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        int[][] seriyalar = new int[k][];
        for (int s = 0; s < k; s++) {
            int m = in.nextInt();
            seriyalar[s] = new int[m];
            for (int i = 0; i < m; i++) seriyalar[s][i] = in.nextInt();
        }

        // {value, run, position}; ordered by value
        PriorityQueue<int[]> uyum = new PriorityQueue<>((p, q) -> Integer.compare(p[0], q[0]));
        for (int s = 0; s < k; s++) {
            if (seriyalar[s].length > 0) uyum.add(new int[] {seriyalar[s][0], s, 0});
        }

        StringBuilder sb = new StringBuilder();
        while (!uyum.isEmpty()) {
            int[] eng = uyum.poll();                        // the smallest current element
            if (sb.length() > 0) sb.append(' ');
            sb.append(eng[0]);
            int s = eng[1], i = eng[2];
            if (i + 1 < seriyalar[s].length) uyum.add(new int[] {seriyalar[s][i + 1], s, i + 1});
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <functional>
#include <iostream>
#include <queue>
#include <tuple>
#include <vector>
using namespace std;

int main() {
    int k;
    cin >> k;
    vector<vector<int>> seriyalar(k);
    for (auto& seriya : seriyalar) {
        int m;
        cin >> m;
        seriya.resize(m);
        for (int& v : seriya) cin >> v;
    }

    // (value, run, position); greater — the smallest on top
    priority_queue<tuple<int, int, int>, vector<tuple<int, int, int>>, greater<>> uyum;
    for (int s = 0; s < k; s++) {
        if (!seriyalar[s].empty()) uyum.push({seriyalar[s][0], s, 0});
    }

    bool birinchi = true;
    while (!uyum.empty()) {
        auto [qiymat, s, i] = uyum.top();                   // the smallest current element
        uyum.pop();
        cout << (birinchi ? "" : " ") << qiymat;
        birinchi = false;
        if (i + 1 < (int) seriyalar[s].size()) uyum.push({seriyalar[s][i + 1], s, i + 1});
    }
    cout << endl;
    return 0;
}
```

Input:

```text
3
4 1 5 9 14
3 2 3 10
5 0 6 7 8 20
```

Output:

```text
0 1 2 3 5 6 7 8 9 10 14 20
```

The heap (*uyum*) always holds at most *k* elements — one from every run (*seriyalar*). So the memory is *O*(*k*), and merging *N* elements in total takes *O*(*N* log *k*) time. In real external sorting the runs are files on disk and only a small buffer of each is read at a time — but the algorithm is exactly this.

:::warning
**Common mistakes**

- **A bad hash function.** For example, a hash on the first letter only — all words starting with "a" land in one bucket and search becomes *O*(*n*).
- **Letting the hash run past the modulus.** If `% M` is not taken at every step of `h * 31 + c`, the number overflows (Java, C++).
- **Expecting order from a hash table.** A hash places keys in no order; if sorted output is needed, sort separately.
- **Not checking the bound in Fibonacci search.** `offset + f2` can run past the end of the array — `min(…, n − 1)` is required.
:::

:::tip
To check how good your hash function is, insert many keys and print the bucket lengths (as in example 2). If the lengths are close to one another, the function spreads the keys evenly.
:::

## Summary

- Fibonacci search splits the range in the ratio of Fibonacci numbers, uses only addition and subtraction and runs in *O*(log *n*).
- A hash table places a key into a bucket through a hash function; the average search is *O*(1 + α).
- Collisions are resolved by chaining or open addressing; when the load grows, the table is enlarged.
- Sorting algorithms differ in complexity, extra memory and stability; a comparison sort cannot beat Ω(*n* log *n*).
- External sorting: sort chunks internally, then merge them k-way with a heap — *O*(*N* log *k*).

## Check yourself

:::check
Q: Which operation does Fibonacci search avoid, unlike binary search?
A: Division: the positions are computed with addition and subtraction only.
Q: What is a collision?
A: When the hash function gives the same value (the same bucket) for two different keys.
Q: If a hash table has 1000 keys and 500 buckets, what is the load factor?
A: α = 1000 / 500 = 2.
Q: Why is a heap used in the k-way merge of external sorting?
A: To find the smallest of the *k* runs' current elements in *O*(log *k*).
Q: Which sorts are stable?
A: Bubble, insertion and merge sort.
:::

:::link https://en.wikipedia.org/wiki/Hash_table
Hash table
Hash functions, collision resolution and the load factor.
:::

:::link https://en.wikipedia.org/wiki/External_sorting
External sorting
Ways of sorting large amounts of data using a disk.
:::
