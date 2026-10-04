---
title: Trees as a data structure. Binary and balanced trees. Sorting with a tree
summary: The notion of a tree — root, node, leaf, height; directed, ordered and binary trees; the binary search tree — insertion, search, traversals (preorder, inorder, postorder, level order); balance and AVL and red-black trees; sorting with a tree.
---

## Why it matters

The folders on a computer, the structure of an HTML page, an organisation chart, a family tree — all of these are **trees**. A tree keeps data hierarchically and, if built well, searches, inserts and deletes in *O*(log *n*) — it searches as fast as a sorted array yet changes as fast as a list.

Database indexes (B-trees), Java's `TreeMap` and C++'s `map` (red-black trees), the syntax trees in compilers — these are the real-world faces of this lecture's topic.

:::note
**After this lesson you will be able to:**

- explain the elements of a tree (root, node, leaf, height) and its kinds;
- write insertion and search in a binary search tree;
- traverse a tree in preorder, inorder, postorder and level order;
- explain why balance matters and the idea of balanced trees;
- sort with a tree.
:::

## The notion of a tree

A **tree** is a connected graph without cycles; if one vertex is chosen as the **root**, every other node has exactly one **parent**.

| Notion | Meaning |
|---|---|
| Root | the only node without a parent |
| Leaf | a node without children |
| Depth of a node | the number of edges from the root to it |
| Height of a tree | the number of levels (the depth of the deepest leaf + 1) |
| Subtree | a node and all its descendants |

A **directed tree** has its edges pointing from the root to the children. An **ordered tree** — the order of the children matters (first, second, …). A **binary tree** — every node has at most two children: left and right.

## The binary search tree

A **binary search tree** (BST) — for every node: all keys in the left subtree are **smaller** than it, and those in the right subtree **greater or equal**.

- **Search**: starting from the root, go left if the key is smaller and right if it is greater — like binary search.
- **Insertion**: walk exactly as when searching and put a new leaf in the empty place.
- **Complexity**: *O*(*h*), where *h* is the height. In a balanced tree *h* ≈ log₂ *n*, but if elements are inserted in **sorted** order, the tree turns into a single "branch" and *h* = *n*.

## Traversing a tree

| Method | Order | Use |
|---|---|---|
| Preorder | node → left → right | copying a tree, the prefix form of an expression |
| Inorder | left → node → right | in a BST the keys come out in **sorted** order |
| Postorder | left → right → node | deleting a tree, computing a folder's size |
| Level order | from the root down, each level left to right | with a queue; finding the nearest node |

## Balanced trees

A tree is **balanced** if at every node the heights of the left and right subtrees are almost equal. Then *h* = *O*(log *n*) and all operations are fast. Trees that keep their balance automatically perform **rotations** after every insertion and deletion:

| Tree | Balance condition | Where |
|---|---|---|
| **AVL** (Adelson-Velsky, Landis, 1962) | at every node the subtree heights differ by ≤ 1 | systems with many searches |
| **Red-black** | rules on node colours; the longest path is ≤ 2 times the shortest | Java `TreeMap`, C++ `map`/`set` |
| **B-tree** | many children, all leaves on one level | database and file-system indexes |

## Sorting with a tree

**Tree sort**: insert all elements into a BST, then output them with an inorder traversal. With a balanced tree it is *O*(*n* log *n*); with a plain BST, *O*(*n*²) in the worst case (sorted input). Its advantage: the order is kept as elements are added, and a sorted list can be taken at any moment.

## Worked example 1: a BST and tree sort

The first line holds *n*, the second *n* numbers. Insert them into a BST in the given order (equal keys go right), and print the sorted sequence from an inorder traversal and the tree's height.

```python
import sys

sys.setrecursionlimit(10000)


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(tugun, kalit):
    if tugun is None:
        return Tugun(kalit)                 # an empty place — a new leaf
    if kalit < tugun.kalit:
        tugun.chap = qosh(tugun.chap, kalit)
    else:
        tugun.ong = qosh(tugun.ong, kalit)
    return tugun


def simmetrik(tugun, natija):               # left → node → right
    if tugun is not None:
        simmetrik(tugun.chap, natija)
        natija.append(tugun.kalit)
        simmetrik(tugun.ong, natija)


def balandlik(tugun):
    if tugun is None:
        return 0
    return 1 + max(balandlik(tugun.chap), balandlik(tugun.ong))


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
natija = []
simmetrik(ildiz, natija)
print(*natija)
print(balandlik(ildiz))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun tugun, int kalit) {
        if (tugun == null) return new Tugun(kalit);         // an empty place — a new leaf
        if (kalit < tugun.kalit) tugun.chap = qosh(tugun.chap, kalit);
        else tugun.ong = qosh(tugun.ong, kalit);
        return tugun;
    }

    static void simmetrik(Tugun tugun, StringBuilder natija) {   // left → node → right
        if (tugun == null) return;
        simmetrik(tugun.chap, natija);
        if (natija.length() > 0) natija.append(' ');
        natija.append(tugun.kalit);
        simmetrik(tugun.ong, natija);
    }

    static int balandlik(Tugun tugun) {
        if (tugun == null) return 0;
        return 1 + Math.max(balandlik(tugun.chap), balandlik(tugun.ong));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        StringBuilder natija = new StringBuilder();
        simmetrik(ildiz, natija);
        System.out.println(natija);
        System.out.println(balandlik(ildiz));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* tugun, int kalit) {
    if (tugun == nullptr) return new Tugun{kalit};          // an empty place — a new leaf
    if (kalit < tugun->kalit) tugun->chap = qosh(tugun->chap, kalit);
    else tugun->ong = qosh(tugun->ong, kalit);
    return tugun;
}

void simmetrik(Tugun* tugun, vector<int>& natija) {         // left → node → right
    if (tugun == nullptr) return;
    simmetrik(tugun->chap, natija);
    natija.push_back(tugun->kalit);
    simmetrik(tugun->ong, natija);
}

int balandlik(Tugun* tugun) {
    if (tugun == nullptr) return 0;
    return 1 + max(balandlik(tugun->chap), balandlik(tugun->ong));
}

void ochir(Tugun* tugun) {                                  // freeing with a postorder traversal
    if (tugun == nullptr) return;
    ochir(tugun->chap);
    ochir(tugun->ong);
    delete tugun;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> natija;
    simmetrik(ildiz, natija);
    for (size_t i = 0; i < natija.size(); i++) cout << (i ? " " : "") << natija[i];
    cout << endl << balandlik(ildiz) << endl;
    ochir(ildiz);
    return 0;
}
```

For `7` and `50 30 70 20 40 60 80` the output is `20 30 40 50 60 70 80` and `3`: a perfectly balanced tree (root 50, then 30 and 70, then four leaves). For `5` and `1 2 3 4 5`, though, the height `5` comes with the sorted result — every new element went right and the tree became a list (*Tugun* is a node, *kalit* the key, *qosh* insert, *simmetrik* inorder, *balandlik* the height). At the end the C++ version frees the nodes with a **postorder** traversal: children must be deleted before their parent.

## Worked example 2: preorder, postorder and level-order traversal

The first line holds *n*, the second *n* distinct numbers. Insert them into a BST and print the tree in three ways: preorder, postorder and level order.

```python
from collections import deque


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(ildiz, kalit):                     # insertion with a loop — no recursion
    yangi = Tugun(kalit)
    if ildiz is None:
        return yangi
    joriy = ildiz
    while True:
        if kalit < joriy.kalit:
            if joriy.chap is None:
                joriy.chap = yangi
                return ildiz
            joriy = joriy.chap
        else:
            if joriy.ong is None:
                joriy.ong = yangi
                return ildiz
            joriy = joriy.ong


def togri(t, r):                            # node → left → right
    if t:
        r.append(t.kalit)
        togri(t.chap, r)
        togri(t.ong, r)


def teskari(t, r):                          # left → right → node
    if t:
        teskari(t.chap, r)
        teskari(t.ong, r)
        r.append(t.kalit)


def sathma_sath(ildiz):                     # with a queue
    r = []
    navbat = deque([ildiz] if ildiz else [])
    while navbat:
        t = navbat.popleft()
        r.append(t.kalit)
        if t.chap:
            navbat.append(t.chap)
        if t.ong:
            navbat.append(t.ong)
    return r


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
r = []
togri(ildiz, r)
print(*r)
r = []
teskari(ildiz, r)
print(*r)
print(*sathma_sath(ildiz))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun ildiz, int kalit) {             // insertion with a loop — no recursion
        Tugun yangi = new Tugun(kalit);
        if (ildiz == null) return yangi;
        Tugun joriy = ildiz;
        while (true) {
            if (kalit < joriy.kalit) {
                if (joriy.chap == null) { joriy.chap = yangi; return ildiz; }
                joriy = joriy.chap;
            } else {
                if (joriy.ong == null) { joriy.ong = yangi; return ildiz; }
                joriy = joriy.ong;
            }
        }
    }

    static void togri(Tugun t, List<Integer> r) {           // node → left → right
        if (t == null) return;
        r.add(t.kalit);
        togri(t.chap, r);
        togri(t.ong, r);
    }

    static void teskari(Tugun t, List<Integer> r) {         // left → right → node
        if (t == null) return;
        teskari(t.chap, r);
        teskari(t.ong, r);
        r.add(t.kalit);
    }

    static List<Integer> sathmaSath(Tugun ildiz) {         // with a queue
        List<Integer> r = new ArrayList<>();
        ArrayDeque<Tugun> navbat = new ArrayDeque<>();
        if (ildiz != null) navbat.add(ildiz);
        while (!navbat.isEmpty()) {
            Tugun t = navbat.poll();
            r.add(t.kalit);
            if (t.chap != null) navbat.add(t.chap);
            if (t.ong != null) navbat.add(t.ong);
        }
        return r;
    }

    static String satr(List<Integer> r) {
        StringBuilder sb = new StringBuilder();
        for (int x : r) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        List<Integer> r = new ArrayList<>();
        togri(ildiz, r);
        System.out.println(satr(r));
        r = new ArrayList<>();
        teskari(ildiz, r);
        System.out.println(satr(r));
        System.out.println(satr(sathmaSath(ildiz)));
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* ildiz, int kalit) {                      // insertion with a loop — no recursion
    Tugun* yangi = new Tugun{kalit};
    if (ildiz == nullptr) return yangi;
    Tugun* joriy = ildiz;
    while (true) {
        Tugun*& keyingi = kalit < joriy->kalit ? joriy->chap : joriy->ong;
        if (keyingi == nullptr) {
            keyingi = yangi;
            return ildiz;
        }
        joriy = keyingi;
    }
}

void togri(Tugun* t, vector<int>& r) {                      // node → left → right
    if (!t) return;
    r.push_back(t->kalit);
    togri(t->chap, r);
    togri(t->ong, r);
}

void teskari(Tugun* t, vector<int>& r) {                    // left → right → node
    if (!t) return;
    teskari(t->chap, r);
    teskari(t->ong, r);
    r.push_back(t->kalit);
}

vector<int> sathmaSath(Tugun* ildiz) {                      // with a queue
    vector<int> r;
    queue<Tugun*> navbat;
    if (ildiz) navbat.push(ildiz);
    while (!navbat.empty()) {
        Tugun* t = navbat.front();
        navbat.pop();
        r.push_back(t->kalit);
        if (t->chap) navbat.push(t->chap);
        if (t->ong) navbat.push(t->ong);
    }
    return r;
}

void chiqar(const vector<int>& r) {
    for (size_t i = 0; i < r.size(); i++) cout << (i ? " " : "") << r[i];
    cout << endl;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> r;
    togri(ildiz, r);
    chiqar(r);
    r.clear();
    teskari(ildiz, r);
    chiqar(r);
    chiqar(sathmaSath(ildiz));
    return 0;
}
```

For `7` and `50 30 70 20 40 60 80` the output is:

```text
50 30 20 40 70 60 80
20 40 30 60 80 70 50
50 30 70 20 40 60 80
```

In preorder the root comes first, in postorder last (*togri*, *teskari* and *sathma_sath* are preorder, postorder and level order). Level order works not with recursion but with a **queue**: when a node is output its children are put at the end of the queue, so an upper level comes out before a lower one. This time insertion is written with a loop — so that on sorted input deep recursion does not overflow the stack. In C++ `Tugun*& keyingi` is a reference to a pointer: through it one can write into the `chap` or `ong` field itself.

## Worked example 3: the height of a balanced and an unbalanced tree

*n* is given (1 ≤ *n* ≤ 10⁵). Build two trees from the numbers 1, 2, …, *n*: (1) by inserting the numbers into a BST one after another; (2) from the sorted array, with **the middle element as the root** and the left and right halves built recursively. Print the heights of both trees. Compute the first tree's height without building it — analysis is enough to show it is a chain.

```python
import sys

sys.setrecursionlimit(10000)


def muvozanatli_balandlik(chap, ong):
    """the height of the tree built from [chap, ong] with the middle as the root."""
    if chap > ong:
        return 0
    orta = (chap + ong) // 2                # the middle element is the root
    return 1 + max(muvozanatli_balandlik(chap, orta - 1),
                   muvozanatli_balandlik(orta + 1, ong))


n = int(input())
ketma_ket = n                               # every number goes right — a chain
print(ketma_ket, muvozanatli_balandlik(1, n))
```
```java
import java.util.Scanner;

public class Main {
    /** the height of the tree built from [chap, ong] with the middle as the root. */
    static int muvozanatliBalandlik(int chap, int ong) {
        if (chap > ong) return 0;
        int orta = (chap + ong) / 2;                // the middle element is the root
        return 1 + Math.max(muvozanatliBalandlik(chap, orta - 1),
                            muvozanatliBalandlik(orta + 1, ong));
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int ketmaKet = n;                           // every number goes right — a chain
        System.out.println(ketmaKet + " " + muvozanatliBalandlik(1, n));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

// the height of the tree built from [chap, ong] with the middle as the root
int muvozanatliBalandlik(int chap, int ong) {
    if (chap > ong) return 0;
    int orta = (chap + ong) / 2;                    // the middle element is the root
    return 1 + max(muvozanatliBalandlik(chap, orta - 1),
                   muvozanatliBalandlik(orta + 1, ong));
}

int main() {
    int n;
    cin >> n;
    int ketmaKet = n;                               // every number goes right — a chain
    cout << ketmaKet << " " << muvozanatliBalandlik(1, n) << endl;
    return 0;
}
```

| *n* | Inserted in order | Balanced |
|---|---|---|
| 7 | 7 | 3 |
| 1000 | 1000 | 10 |
| 100,000 | 100,000 | 17 |

When 1, 2, …, *n* are inserted in order, every new number is larger than all before it — it is linked to the last node on the right and the tree becomes a chain: height *n*, search *O*(*n*). A tree built from the same numbers with the middle as the root has height ⌈log₂(*n* + 1)⌉: just 17 for 100,000 elements (*ketma_ket* means in sequence, *muvozanatli* balanced). AVL and red-black trees keep such a height automatically, whatever order the numbers arrive in. In the balanced build the recursion depth is also log *n* — which is why it is safe even for *n* = 10⁵.

:::warning
**Common mistakes**

- **Inserting sorted data into a plain BST.** The tree turns into a chain; use a balanced tree (`TreeMap`, `map`) or a random order.
- **Stack overflow in recursive insertion and traversal.** In a chain tree the depth equals *n*; insertion with a loop or a balanced tree is needed.
- **No rule for equal keys.** An equal key must always go to one side (say, the right), otherwise search cannot find them.
- **Not freeing the tree in C++.** Every `new Tugun` needs a `delete`; do it with a postorder traversal.
:::

:::tip
When working with a BST, draw the tree on paper and run every inserted element from the root, asking "left or right?". If the inorder traversal is not sorted, there is a mistake in insertion.
:::

## Summary

- A tree is a connected graph without cycles: a root, nodes, leaves; the height is the number of levels.
- A BST: smaller keys on the left, greater on the right; search and insertion are *O*(*h*).
- Traversals: preorder, inorder (sorted order in a BST), postorder and level order (with a queue).
- An unbalanced BST turns into a chain on sorted input (*h* = *n*); AVL, red-black and B-trees guarantee *h* = *O*(log *n*).
- Tree sort: insert the elements, then traverse inorder — *O*(*n* log *n*) with a balanced tree.

## Check yourself

:::check
Q: Which traversal of a BST gives the keys in sorted order?
A: Inorder: left → node → right.
Q: If 1, 2, 3, 4 are inserted into a BST in order, what is the tree's height?
A: 4 — every element goes right and a chain forms.
Q: Which structure does level-order traversal need?
A: A queue.
Q: Why is search in Java's `TreeMap` always *O*(log *n*)?
A: It is a red-black tree — after every insertion and deletion it restores balance with rotations.
:::

:::link https://visualgo.net/en/bst
VisuAlgo: binary search tree
An animation of a BST and an AVL tree.
:::
