---
title: Trees. The Fenwick tree. The segment tree. Balanced trees (practical session)
summary: The range query problem; prefix sums — O(1) queries on an unchanging array; the segment tree — range minimum and element updates in O(log n); the idea of the Fenwick tree; the three cases of deleting a node from a binary search tree.
---

## Why it matters

In many problems two kinds of operations on an array come mixed: a **range query** ("what were the sales from day 3 to day 10?", "what was the lowest temperature in this range?") and an **update** ("day 5's figure was corrected"). Walking the range for every query is *O*(*n*) — for 10⁵ queries that is 10¹⁰ operations. Tree-based structures do both operations in *O*(log *n*).

In this session we go from simple to complex: prefix sums (without updates), the segment tree (with updates), and deletion from a binary search tree.

:::note
**After this session you will be able to:**

- find a range sum in *O*(1) with prefix sums;
- build a segment tree and do range minimum and element updates in *O*(log *n*);
- explain when a Fenwick tree is convenient;
- correctly delete a leaf, a node with one child and a node with two children from a BST.
:::

## Prefix sums

`p[i] = a[1] + a[2] + … + a[i]`, `p[0] = 0`. Then the sum of the range [*l*, *r*] is:

```text
sum(l, r) = p[r] − p[l − 1]
```

Preparation is *O*(*n*), every query *O*(1). The drawback: if one element changes, every `p[i]` after it must be recomputed: *O*(*n*). That is why prefix sums are for **unchanging** data.

## The segment tree

A **segment tree** is a binary tree in which every node keeps the result (sum, minimum, maximum) for a segment of the array. The root is the whole array, the leaves single elements. Stored in an array: round *n* up to a power of two; the leaves are in `t[n … 2n − 1]` and the children of node `i` are at `2i` and `2i + 1`.

| Operation | How | Complexity |
|---|---|---|
| Update `a[i] = x` | change the leaf and recompute its ancestors up to the root | *O*(log *n*) |
| Range query [*l*, *r*] | climb up from both bounds, combining the nodes lying fully inside | *O*(log *n*) |

A segment tree works with any **associative** operation (sum, min, max, GCD).

## The Fenwick tree

The **Fenwick tree** (binary indexed tree, 1994) is a more compact structure for prefix sums: one array of *n* elements, walked with the bit operation `i & (−i)`. Updating and a prefix sum are *O*(log *n*), and the code is much shorter than a segment tree's. Its limitation: mostly for "invertible" operations such as the sum (awkward for the minimum). You write it yourself in the practical assignment.

| Structure | Update | Query | When |
|---|---|---|---|
| Prefix sums | *O*(*n*) | *O*(1) | the data does not change |
| Fenwick tree | *O*(log *n*) | *O*(log *n*) | sums + updates |
| Segment tree | *O*(log *n*) | *O*(log *n*) | min/max or a complex operation + updates |

## Deleting from a BST

| Case | How |
|---|---|
| The node is a leaf | simply remove it |
| It has one child | put the child in its place |
| It has two children | copy the **smallest** key of the right subtree (the successor) into the node, then delete that element from the right subtree |

In the third case the BST property is kept: the successor is greater than everything on the left and no greater than everything on the right.

## Worked example 1: prefix sums

The first line holds *n*, the second *n* numbers, the third *q*, then *q* queries `l r` (from 1). For every query print the sum of [*l*, *r*].

```python
import sys

kirish = sys.stdin.read().split()           # fast reading for large input
n = int(kirish[0])
a = list(map(int, kirish[1:n + 1]))
p = [0] * (n + 1)
for i in range(n):
    p[i + 1] = p[i] + a[i]                  # p[i] is the sum of the first i elements

q = int(kirish[n + 1])
natija = []
for k in range(q):
    l = int(kirish[n + 2 + 2 * k])
    r = int(kirish[n + 3 + 2 * k])
    natija.append(p[r] - p[l - 1])          # O(1)
print("\n".join(map(str, natija)))
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader oquvchi = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(oquvchi.readLine());
        int n = Integer.parseInt(st.nextToken());
        long[] p = new long[n + 1];
        st = new StringTokenizer(oquvchi.readLine());
        for (int i = 0; i < n; i++) p[i + 1] = p[i] + Long.parseLong(st.nextToken());   // p[i] is the sum of the first i elements

        int q = Integer.parseInt(oquvchi.readLine().trim());
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            st = new StringTokenizer(oquvchi.readLine());
            int l = Integer.parseInt(st.nextToken()), r = Integer.parseInt(st.nextToken());
            sb.append(p[r] - p[l - 1]).append('\n');    // O(1)
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);                    // fast reading for large input
    cin.tie(nullptr);
    int n;
    cin >> n;
    vector<long long> p(n + 1, 0);
    for (int i = 0; i < n; i++) {
        long long x;
        cin >> x;
        p[i + 1] = p[i] + x;                        // p[i] is the sum of the first i elements
    }
    int q;
    cin >> q;
    while (q--) {
        int l, r;
        cin >> l >> r;
        cout << p[r] - p[l - 1] << '\n';            // O(1)
    }
    return 0;
}
```

For `6`, `5 2 8 1 9 3` and the queries `1 3`, `2 5`, `4 4`, `1 6` the output is `15`, `20`, `1`, `28`. Thanks to `p[0] = 0`, the case `l = 1` needs no special check. With many queries input and output matter too: Python reads the whole input at once, Java uses a `BufferedReader`, C++ `sync_with_stdio(false)` (*kirish* is the input, *natija* the result).

## Worked example 2: a segment tree — the range minimum

The first line holds *n*, the second *n* numbers, the third *q*, then *q* commands: `min l r` — print the smallest value in [*l*, *r*], `set i x` — replace element *i* with *x* (indices from 1).

```python
import sys

kirish = sys.stdin.read().split()
n = int(kirish[0])
olcham = 1
while olcham < n:
    olcham *= 2                             # a power of two
INF = float("inf")
t = [INF] * (2 * olcham)
for i in range(n):
    t[olcham + i] = int(kirish[1 + i])      # the leaves
for i in range(olcham - 1, 0, -1):
    t[i] = min(t[2 * i], t[2 * i + 1])      # parent = the minimum of the children

q = int(kirish[n + 1])
oqish = n + 2
natija = []
for _ in range(q):
    buyruq, x, y = kirish[oqish], int(kirish[oqish + 1]), int(kirish[oqish + 2])
    oqish += 3
    if buyruq == "set":
        i = olcham + x - 1
        t[i] = y
        i //= 2
        while i >= 1:                       # recompute up to the root
            t[i] = min(t[2 * i], t[2 * i + 1])
            i //= 2
    else:
        l, r = olcham + x - 1, olcham + y   # [l, r) a half-open range
        eng = INF
        while l < r:                        # climbing up from the bounds
            if l % 2 == 1:
                eng = min(eng, t[l])
                l += 1
            if r % 2 == 1:
                r -= 1
                eng = min(eng, t[r])
            l //= 2
            r //= 2
        natija.append(eng)
print("\n".join(map(str, natija)))
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader oquvchi = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(oquvchi.readLine().trim());
        int olcham = 1;
        while (olcham < n) olcham *= 2;                         // a power of two
        long[] t = new long[2 * olcham];
        Arrays.fill(t, Long.MAX_VALUE);
        StringTokenizer st = new StringTokenizer(oquvchi.readLine());
        for (int i = 0; i < n; i++) t[olcham + i] = Long.parseLong(st.nextToken());   // the leaves
        for (int i = olcham - 1; i >= 1; i--) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // parent = the minimum of the children

        int q = Integer.parseInt(oquvchi.readLine().trim());
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            st = new StringTokenizer(oquvchi.readLine());
            String buyruq = st.nextToken();
            int x = Integer.parseInt(st.nextToken());
            long y = Long.parseLong(st.nextToken());
            if (buyruq.equals("set")) {
                int i = olcham + x - 1;
                t[i] = y;
                for (i /= 2; i >= 1; i /= 2) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // recompute up to the root
            } else {
                int l = olcham + x - 1, r = olcham + (int) y;  // [l, r) a half-open range
                long eng = Long.MAX_VALUE;
                while (l < r) {                                 // climbing up from the bounds
                    if (l % 2 == 1) eng = Math.min(eng, t[l++]);
                    if (r % 2 == 1) eng = Math.min(eng, t[--r]);
                    l /= 2;
                    r /= 2;
                }
                sb.append(eng).append('\n');
            }
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n;
    cin >> n;
    int olcham = 1;
    while (olcham < n) olcham *= 2;                             // a power of two
    vector<long long> t(2 * olcham, LLONG_MAX);
    for (int i = 0; i < n; i++) cin >> t[olcham + i];           // the leaves
    for (int i = olcham - 1; i >= 1; i--) t[i] = min(t[2 * i], t[2 * i + 1]);   // parent = the minimum of the children

    int q;
    cin >> q;
    while (q--) {
        string buyruq;
        long long x, y;
        cin >> buyruq >> x >> y;
        if (buyruq == "set") {
            int i = olcham + x - 1;
            t[i] = y;
            for (i /= 2; i >= 1; i /= 2) t[i] = min(t[2 * i], t[2 * i + 1]);   // recompute up to the root
        } else {
            int l = olcham + x - 1, r = olcham + y;             // [l, r) a half-open range
            long long eng = LLONG_MAX;
            while (l < r) {                                     // climbing up from the bounds
                if (l % 2 == 1) eng = min(eng, t[l++]);
                if (r % 2 == 1) eng = min(eng, t[--r]);
                l /= 2;
                r /= 2;
            }
            cout << eng << '\n';
        }
    }
    return 0;
}
```

Input:

```text
6
5 2 8 1 9 3
7
min 1 3
min 3 6
set 4 10
min 3 6
min 4 4
set 2 7
min 1 2
```

Output:

```text
2
1
3
10
5
```

After `set 4 10` the `1` at element 4 is gone, so the minimum of [3, 6] changed to `3` — the update recomputed only the leaf and its ancestors (log₂ 8 = 3 nodes) (*olcham* is the size, *eng* the smallest). In a query `l` and `r` climb up from the leaves: an "odd" node at a bound that lies fully inside the range is added to the result. Since the tree size was rounded up to 8, the extra leaves were filled with ∞ — they do not affect the minimum.

## Worked example 3: deleting from a BST

The first line holds *n* keys, the second the keys to delete. Insert the keys into a BST, then delete the given keys (a missing key is ignored) and print the resulting tree in preorder; `-` if the tree is empty.

```python
import sys

sys.setrecursionlimit(10000)


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(t, kalit):
    if t is None:
        return Tugun(kalit)
    if kalit < t.kalit:
        t.chap = qosh(t.chap, kalit)
    else:
        t.ong = qosh(t.ong, kalit)
    return t


def ochir(t, kalit):
    if t is None:                           # no such key
        return None
    if kalit < t.kalit:
        t.chap = ochir(t.chap, kalit)
    elif kalit > t.kalit:
        t.ong = ochir(t.ong, kalit)
    elif t.chap is None:                    # a leaf or only a right child
        return t.ong
    elif t.ong is None:                     # only a left child
        return t.chap
    else:                                   # two children: replace with the successor
        keyingi = t.ong
        while keyingi.chap is not None:
            keyingi = keyingi.chap
        t.kalit = keyingi.kalit
        t.ong = ochir(t.ong, keyingi.kalit)
    return t


def togri(t, r):
    if t:
        r.append(t.kalit)
        togri(t.chap, r)
        togri(t.ong, r)


ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
for x in map(int, input().split()):
    ildiz = ochir(ildiz, x)
r = []
togri(ildiz, r)
print(*r if r else "-")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun t, int kalit) {
        if (t == null) return new Tugun(kalit);
        if (kalit < t.kalit) t.chap = qosh(t.chap, kalit);
        else t.ong = qosh(t.ong, kalit);
        return t;
    }

    static Tugun ochir(Tugun t, int kalit) {
        if (t == null) return null;                         // no such key
        if (kalit < t.kalit) t.chap = ochir(t.chap, kalit);
        else if (kalit > t.kalit) t.ong = ochir(t.ong, kalit);
        else if (t.chap == null) return t.ong;              // a leaf or only a right child
        else if (t.ong == null) return t.chap;              // only a left child
        else {                                              // two children: replace with the successor
            Tugun keyingi = t.ong;
            while (keyingi.chap != null) keyingi = keyingi.chap;
            t.kalit = keyingi.kalit;
            t.ong = ochir(t.ong, keyingi.kalit);
        }
        return t;
    }

    static void togri(Tugun t, List<Integer> r) {
        if (t == null) return;
        r.add(t.kalit);
        togri(t.chap, r);
        togri(t.ong, r);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun ildiz = null;
        for (String s : in.nextLine().trim().split("\\s+")) ildiz = qosh(ildiz, Integer.parseInt(s));
        for (String s : in.nextLine().trim().split("\\s+")) ildiz = ochir(ildiz, Integer.parseInt(s));
        List<Integer> r = new ArrayList<>();
        togri(ildiz, r);
        StringBuilder sb = new StringBuilder();
        for (int x : r) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(r.isEmpty() ? "-" : sb.toString());
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* t, int kalit) {
    if (!t) return new Tugun{kalit};
    if (kalit < t->kalit) t->chap = qosh(t->chap, kalit);
    else t->ong = qosh(t->ong, kalit);
    return t;
}

Tugun* ochir(Tugun* t, int kalit) {
    if (!t) return nullptr;                                 // no such key
    if (kalit < t->kalit) {
        t->chap = ochir(t->chap, kalit);
    } else if (kalit > t->kalit) {
        t->ong = ochir(t->ong, kalit);
    } else if (!t->chap) {                                  // a leaf or only a right child
        Tugun* bola = t->ong;
        delete t;
        return bola;
    } else if (!t->ong) {                                   // only a left child
        Tugun* bola = t->chap;
        delete t;
        return bola;
    } else {                                                // two children: replace with the successor
        Tugun* keyingi = t->ong;
        while (keyingi->chap) keyingi = keyingi->chap;
        t->kalit = keyingi->kalit;
        t->ong = ochir(t->ong, keyingi->kalit);
    }
    return t;
}

void togri(Tugun* t, vector<int>& r) {
    if (!t) return;
    r.push_back(t->kalit);
    togri(t->chap, r);
    togri(t->ong, r);
}

int main() {
    string satr;
    int x;
    Tugun* ildiz = nullptr;
    getline(cin, satr);
    istringstream kalitlar(satr);
    while (kalitlar >> x) ildiz = qosh(ildiz, x);
    getline(cin, satr);
    istringstream ochiriladigan(satr);
    while (ochiriladigan >> x) ildiz = ochir(ildiz, x);

    vector<int> r;
    togri(ildiz, r);
    if (r.empty()) cout << "-";
    for (size_t i = 0; i < r.size(); i++) cout << (i ? " " : "") << r[i];
    cout << endl;
    return 0;
}
```

Input:

```text
50 30 70 20 40 60 80
20 30 50 99
```

Output:

```text
60 40 70 80
```

`20` is a leaf: it was simply removed. `30` had one child left (`40`) — it took `30`'s place. `50` has two children: the smallest key of the right subtree, `60`, was copied to the root and that `60` leaf was deleted. `99` is not in the tree — it was ignored (*ochir* means delete, *keyingi* the successor). The result: the root 60, 40 on the left, 70 on the right with its right child 80 — the BST property holds.

:::warning
**Common mistakes**

- **Subtracting `p[l]` in a prefix sum.** The correct form is `p[r] − p[l − 1]`; otherwise element *l* is subtracted too.
- **Not recomputing the ancestors after an update in a segment tree.** Queries return the old value.
- **Filling the extra leaves with 0.** For the minimum, 0 spoils the result; the operation's neutral element is needed — ∞ (and 0 for the sum).
- **Copying only the key when deleting a node with two children.** The successor's old node must be deleted too, otherwise the key stays twice.
:::

:::tip
Draw a segment tree on paper as an 8-leaf tree and write each node's segment (`[1..4]`, `[5..6]`, …) above it. Seeing in the picture which nodes a query combines makes the code much easier to understand.
:::

## Summary

- Prefix sums give a range sum on an unchanging array in *O*(1): `p[r] − p[l − 1]`.
- A segment tree does updates and range queries in *O*(log *n*) for any associative operation.
- A Fenwick tree is more compact for sums and its code is short; for operations like the minimum a segment tree is convenient.
- Deleting from a BST: a leaf — remove it, one child — lift it up, two children — replace with the successor.

## Check yourself

:::check
Q: What is the prefix sum array for `a = 4 1 3 2`?
A: `p = 0 4 5 8 10`.
Q: Why do prefix sums not suit frequently updated data?
A: If one element changes, every `p[i]` after it must be recomputed — *O*(*n*).
Q: How many nodes does an update change in a segment tree?
A: The leaf and all its ancestors — log₂ *n* + 1.
Q: When a node with two children is deleted from a BST, which key is put in its place?
A: The smallest key of the right subtree (or the largest of the left subtree).
:::

:::link https://cp-algorithms.com/data_structures/segment_tree.html
CP-Algorithms: segment tree
Building, queries, updates and variants.
:::
