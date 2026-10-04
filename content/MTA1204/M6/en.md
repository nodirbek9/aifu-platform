---
title: Lists. Graphs. Dijkstra's and Floyd's algorithms
summary: Singly linked, doubly linked and circular lists and how they differ from an array; the elements and kinds of graphs, representing a graph in a computer — the adjacency matrix and the adjacency list; the shortest path problem, Dijkstra's algorithm and the Floyd–Warshall algorithm.
---

## Why it matters

An array's elements lie side by side in memory — which is why inserting or deleting in the middle is expensive. A **linked list** connects elements with pointers and does these operations in *O*(1). This idea underlies all the more complex structures that follow — trees and graphs.

A **graph** is the most general model of interconnected objects: cities and roads, users and friendships, computers and network cables, web pages and links. A navigator finds the shortest route, and internet routers choose a packet's direction, with exactly the algorithms of this lecture.

:::note
**After this lesson you will be able to:**

- tell singly linked, doubly linked and circular lists apart and write a singly linked list;
- compare the complexity of operations on a list and an array;
- explain the elements and kinds of graphs and store a graph as a matrix and as lists;
- find the shortest distances and path from one vertex with Dijkstra's algorithm;
- find the distances between all pairs with the Floyd–Warshall algorithm.
:::

## Linked lists

A list consists of **nodes**; every node keeps a value and a pointer (reference) to the next node. The first node is the **head**; the last one's pointer is empty (`None`, `null`, `nullptr`).

| Kind | Structure | Advantage |
|---|---|---|
| Singly linked | `next` in every node | simple, little memory |
| Doubly linked | `prev` and `next` | walking both ways, removing a node in *O*(1) |
| Circular | the last node links to the head | a round-robin queue, players' turns |

| Operation | Array | Linked list |
|---|---|---|
| Access to element *i* | *O*(1) | *O*(*n*) — you must walk from the head |
| Insert at the front | *O*(*n*) | *O*(1) |
| Insert/delete after a known node | *O*(*n*) | *O*(1) |
| Search by value | *O*(*n*) | *O*(*n*) |

Java's `LinkedList` and C++'s `std::list` are doubly linked lists. In practice array-based lists (`ArrayList`, `vector`) are used more: the processor cache works faster with adjacent elements.

## Graphs

A **graph** *G* = (*V*, *E*) is a set of **vertices** (*V*) and the **edges** (*E*) joining them.

| Notion | Meaning |
|---|---|
| Undirected graph | an edge works both ways: friendship |
| Directed graph (digraph) | an edge goes one way — an arc: a one-way street, a web link |
| Weighted graph | every edge carries a number — its weight: distance, price, time |
| Degree of a vertex | the number of edges at it |
| Path | vertices joined one after another by edges |
| Connected graph | there is a path between any two vertices |

Representing a graph in a computer:

| Method | Memory | "Are *u* and *v* adjacent?" | When |
|---|---|---|---|
| **Adjacency matrix** `d[u][v]` | *O*(*V*²) | *O*(1) | dense, small graphs (*V* ≤ ~2000) |
| **Adjacency list** — a list of neighbours for every vertex | *O*(*V* + *E*) | *O*(degree) | sparse, large graphs (road networks, social networks) |

## Shortest paths: Dijkstra's algorithm

**Dijkstra's algorithm** (E. Dijkstra, 1959) — the shortest distances from one vertex to all others in a graph with non-negative weights:

```text
dist[s] = 0, the rest ∞; all vertices "unmarked"
n times:
    among the unmarked choose the v with the smallest dist and mark it
    for every neighbour u of v:
        if dist[v] + w(v, u) < dist[u]: dist[u] = dist[v] + w(v, u)   ← "relaxation"
```

The idea: a marked vertex's distance no longer changes — since weights are non-negative, any other path is longer. Choosing with an array gives *O*(*V*²); with a **priority queue** (a heap), *O*((*V* + *E*) log *V*) — for large sparse graphs. With negative weights Dijkstra works incorrectly (the Bellman–Ford algorithm is needed).

## The Floyd–Warshall algorithm

To find the distances between **all pairs**:

```text
for k = 1..n: for i = 1..n: for j = 1..n:
    d[i][j] = min(d[i][j], d[i][k] + d[k][j])
```

After step *k*, `d[i][j]` is the length of the shortest path whose intermediate vertices come only from {1, …, *k*}. Three nested loops — *O*(*V*³), memory *O*(*V*²). It works with negative edge weights too (if there is no negative cycle). The outer loop **must** run over *k*.

## Worked example 1: a singly linked list

The first line holds *q*, then *q* commands: `boshiga X` (to the front), `oxiriga X` (to the end), `ochir X` (delete the first *X*; print `absent` if there is none), `chiqar` (print the list; `empty` if it is empty). Write the list with nodes and pointers; keep the last node as well so that appending is *O*(1).

```python
class Tugun:
    def __init__(self, qiymat):
        self.qiymat = qiymat
        self.keyingi = None


bosh = None
oxir = None                                 # the last node — appending is O(1)

q = int(input())
for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "boshiga":
        t = Tugun(int(buyruq[1]))
        t.keyingi = bosh
        bosh = t
        if oxir is None:
            oxir = t
    elif buyruq[0] == "oxiriga":
        t = Tugun(int(buyruq[1]))
        if oxir is None:
            bosh = oxir = t
        else:
            oxir.keyingi = t
            oxir = t
    elif buyruq[0] == "ochir":
        x = int(buyruq[1])
        oldingi, joriy = None, bosh
        while joriy is not None and joriy.qiymat != x:
            oldingi, joriy = joriy, joriy.keyingi
        if joriy is None:
            print("absent")
            continue
        if oldingi is None:                 # the head is removed
            bosh = joriy.keyingi
        else:
            oldingi.keyingi = joriy.keyingi # "bypass" the node
        if joriy is oxir:
            oxir = oldingi
    else:
        qiymatlar = []
        joriy = bosh
        while joriy is not None:
            qiymatlar.append(joriy.qiymat)
            joriy = joriy.keyingi
        print(*qiymatlar if qiymatlar else ["empty"])
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        int qiymat;
        Tugun keyingi;
        Tugun(int qiymat) { this.qiymat = qiymat; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun bosh = null, oxir = null;             // the last node — appending is O(1)
        int q = in.nextInt();
        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("boshiga")) {
                Tugun t = new Tugun(in.nextInt());
                t.keyingi = bosh;
                bosh = t;
                if (oxir == null) oxir = t;
            } else if (buyruq.equals("oxiriga")) {
                Tugun t = new Tugun(in.nextInt());
                if (oxir == null) {
                    bosh = oxir = t;
                } else {
                    oxir.keyingi = t;
                    oxir = t;
                }
            } else if (buyruq.equals("ochir")) {
                int x = in.nextInt();
                Tugun oldingi = null, joriy = bosh;
                while (joriy != null && joriy.qiymat != x) {
                    oldingi = joriy;
                    joriy = joriy.keyingi;
                }
                if (joriy == null) {
                    System.out.println("absent");
                    continue;
                }
                if (oldingi == null) bosh = joriy.keyingi;      // the head is removed
                else oldingi.keyingi = joriy.keyingi;           // "bypass" the node
                if (joriy == oxir) oxir = oldingi;
            } else {
                StringBuilder sb = new StringBuilder();
                for (Tugun t = bosh; t != null; t = t.keyingi) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(t.qiymat);
                }
                System.out.println(sb.length() > 0 ? sb.toString() : "empty");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

struct Tugun {
    int qiymat;
    Tugun* keyingi = nullptr;
};

int main() {
    Tugun* bosh = nullptr;
    Tugun* oxir = nullptr;                          // the last node — appending is O(1)
    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "boshiga") {
            Tugun* t = new Tugun{0};
            cin >> t->qiymat;
            t->keyingi = bosh;
            bosh = t;
            if (oxir == nullptr) oxir = t;
        } else if (buyruq == "oxiriga") {
            Tugun* t = new Tugun{0};
            cin >> t->qiymat;
            if (oxir == nullptr) {
                bosh = oxir = t;
            } else {
                oxir->keyingi = t;
                oxir = t;
            }
        } else if (buyruq == "ochir") {
            int x;
            cin >> x;
            Tugun* oldingi = nullptr;
            Tugun* joriy = bosh;
            while (joriy != nullptr && joriy->qiymat != x) {
                oldingi = joriy;
                joriy = joriy->keyingi;
            }
            if (joriy == nullptr) {
                cout << "absent" << endl;
                continue;
            }
            if (oldingi == nullptr) bosh = joriy->keyingi;      // the head is removed
            else oldingi->keyingi = joriy->keyingi;             // "bypass" the node
            if (joriy == oxir) oxir = oldingi;
            delete joriy;
        } else {
            if (bosh == nullptr) cout << "empty";
            for (Tugun* t = bosh; t != nullptr; t = t->keyingi) {
                cout << t->qiymat << (t->keyingi ? " " : "");
            }
            cout << endl;
        }
    }
    while (bosh != nullptr) {                       // free the remaining nodes
        Tugun* keyingi = bosh->keyingi;
        delete bosh;
        bosh = keyingi;
    }
    return 0;
}
```

Input:

```text
10
oxiriga 5
oxiriga 7
boshiga 3
chiqar
ochir 5
chiqar
ochir 9
ochir 3
ochir 7
chiqar
```

Output:

```text
3 5 7
3 7
absent
empty
```

When deleting, the node itself does not move — the previous node's pointer "bypasses" it and links to the next one (*Tugun* is a node, *keyingi* next, *bosh* the head, *oxir* the tail, *oldingi* the previous, *joriy* the current). There are three separate cases: deleting the head (no `oldingi`), deleting the last node (`oxir` is updated) and the list becoming empty. In C++ a deleted node is freed with `delete`, and at the end of the program all remaining nodes are.

## Worked example 2: Dijkstra's algorithm

The first line holds *n* and *m* (vertices and edges), the next *m* lines undirected edges `u v w`. Print the shortest distances from vertex 1 to all vertices (`-` if unreachable) and the shortest path from vertex 1 to vertex *n*. Use an adjacency matrix and *O*(*n*²) selection.

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[INF] * (n + 1) for _ in range(n + 1)]     # the adjacency matrix
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u][v] = d[v][u] = min(d[u][v], w)

dist = [INF] * (n + 1)
dist[1] = 0
belgilangan = [False] * (n + 1)
ota = [0] * (n + 1)                             # to restore the path
for _ in range(n):
    v = -1
    for i in range(1, n + 1):                   # the nearest unmarked vertex
        if not belgilangan[i] and (v == -1 or dist[i] < dist[v]):
            v = i
    if dist[v] == INF:
        break
    belgilangan[v] = True
    for u in range(1, n + 1):                   # relaxation
        if dist[v] + d[v][u] < dist[u]:
            dist[u] = dist[v] + d[v][u]
            ota[u] = v

print(*[x if x < INF else "-" for x in dist[1:]])
if dist[n] < INF:
    yol = []
    x = n
    while x != 0:
        yol.append(x)
        x = ota[x]
    print(*reversed(yol))
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n + 1][n + 1];                // the adjacency matrix
        for (long[] satr : d) java.util.Arrays.fill(satr, INF);
        for (int k = 0; k < m; k++) {
            int u = in.nextInt(), v = in.nextInt();
            long w = in.nextLong();
            d[u][v] = d[v][u] = Math.min(d[u][v], w);
        }

        long[] dist = new long[n + 1];
        java.util.Arrays.fill(dist, INF);
        dist[1] = 0;
        boolean[] belgilangan = new boolean[n + 1];
        int[] ota = new int[n + 1];                         // to restore the path
        for (int k = 0; k < n; k++) {
            int v = -1;
            for (int i = 1; i <= n; i++) {                  // the nearest unmarked vertex
                if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
            }
            if (dist[v] == INF) break;
            belgilangan[v] = true;
            for (int u = 1; u <= n; u++) {                  // relaxation
                if (d[v][u] < INF && dist[v] + d[v][u] < dist[u]) {
                    dist[u] = dist[v] + d[v][u];
                    ota[u] = v;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(' ');
            sb.append(dist[i] < INF ? String.valueOf(dist[i]) : "-");
        }
        System.out.println(sb);
        if (dist[n] < INF) {
            List<Integer> yol = new ArrayList<>();
            for (int x = n; x != 0; x = ota[x]) yol.add(x);
            Collections.reverse(yol);
            StringBuilder y = new StringBuilder();
            for (int x : yol) {
                if (y.length() > 0) y.append(' ');
                y.append(x);
            }
            System.out.println(y);
        }
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n + 1, vector<long long>(n + 1, INF));   // the adjacency matrix
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u][v] = d[v][u] = min(d[u][v], w);
    }

    vector<long long> dist(n + 1, INF);
    dist[1] = 0;
    vector<bool> belgilangan(n + 1, false);
    vector<int> ota(n + 1, 0);                              // to restore the path
    for (int k = 0; k < n; k++) {
        int v = -1;
        for (int i = 1; i <= n; i++) {                      // the nearest unmarked vertex
            if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
        }
        if (dist[v] == INF) break;
        belgilangan[v] = true;
        for (int u = 1; u <= n; u++) {                      // relaxation
            if (d[v][u] < INF && dist[v] + d[v][u] < dist[u]) {
                dist[u] = dist[v] + d[v][u];
                ota[u] = v;
            }
        }
    }

    for (int i = 1; i <= n; i++) {
        cout << (i > 1 ? " " : "");
        if (dist[i] < INF) cout << dist[i];
        else cout << "-";
    }
    cout << endl;
    if (dist[n] < INF) {
        vector<int> yol;
        for (int x = n; x != 0; x = ota[x]) yol.push_back(x);
        reverse(yol.begin(), yol.end());
        for (size_t i = 0; i < yol.size(); i++) cout << (i ? " " : "") << yol[i];
        cout << endl;
    }
    return 0;
}
```

Input:

```text
5 6
1 2 4
1 3 1
3 2 2
2 4 1
3 4 5
4 5 3
```

Output:

```text
0 3 1 4 7
1 3 2 4 5
```

The direct edge 1 → 2 weighs 4, but the path 1 → 3 → 2 is 1 + 2 = 3 — shorter: when vertex 3 was marked, the distance to vertex 2 was "relaxed". The `ota` (parent) array remembers which vertex every vertex was reached from; the path is restored from the end to the start and printed in reverse (*belgilangan* means marked, *yol* the path). In Python adding to `float("inf")` is safe, while in Java and C++ `MAX / 4` is taken as ∞ and the existence of an edge is checked separately — otherwise `INF + w` would overflow.

## Worked example 3: the Floyd–Warshall algorithm

The first line holds *n* and *m*, the next *m* lines **directed** edges `u v w`. Print the matrix of shortest distances between all pairs (`-` if unreachable).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                              # the intermediate vertex — the outer loop!
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

for satr in d:
    print(*[x if x < INF else "-" for x in satr])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) d[i][j] = i == j ? 0 : INF;
        for (int k = 0; k < m; k++) {
            int u = in.nextInt() - 1, v = in.nextInt() - 1;
            d[u][v] = Math.min(d[u][v], in.nextLong());
        }

        for (int k = 0; k < n; k++)                     // the intermediate vertex — the outer loop!
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

        StringBuilder sb = new StringBuilder();
        for (long[] satr : d) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(satr[j] < INF ? String.valueOf(satr[j]) : "-");
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n, vector<long long>(n, INF));
    for (int i = 0; i < n; i++) d[i][i] = 0;
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u - 1][v - 1] = min(d[u - 1][v - 1], w);
    }

    for (int k = 0; k < n; k++)                         // the intermediate vertex — the outer loop!
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            cout << (j ? " " : "");
            if (d[i][j] < INF) cout << d[i][j];
            else cout << "-";
        }
        cout << endl;
    }
    return 0;
}
```

Input:

```text
5 5
1 2 3
2 3 2
1 3 7
3 4 1
4 1 6
```

Output:

```text
0 3 5 6 -
9 0 2 3 -
7 10 0 1 -
6 9 11 0 -
- - - - 0
```

The direct edge 1 → 3 weighs 7, but through vertex 2 it is 3 + 2 = 5 — Floyd found this at step *k* = 2. Vertex 5 has no edges at all — it is cut off from the others, and its row and column are filled with `-`. The graph is directed, so the matrix is not symmetric: the distance 1 → 2 is 3, while 2 → 1 is 9 (2 → 3 → 4 → 1). For *n* = 400 Floyd does 6.4 · 10⁷ operations — under a second; for *n* = 10⁴ it would be 10¹² — for such graphs Dijkstra from every vertex is better.

:::warning
**Common mistakes**

- **Forgetting the head or the tail when deleting from a list.** The `bosh` and `oxir` pointers are left pointing at a stale node.
- **Using Dijkstra on a graph with negative weights.** A marked vertex's distance could decrease later — the result is wrong.
- **Swapping the order of the loops in Floyd.** If *k* is not the outer loop, the algorithm works incorrectly.
- **Adding a weight to ∞ and overflowing.** In Java and C++ `Long.MAX_VALUE + w` gives a negative number; take a smaller ∞ or check that the edge exists.
:::

:::tip
When testing graph algorithms, draw the graph on paper: vertices as circles, edges as weighted lines. Write in a table which vertex Dijkstra marks at every step and how the distances change.
:::

## Summary

- A linked list is joined by pointers: inserting and deleting are *O*(1), but access by index is *O*(*n*).
- A graph is vertices and edges; it can be directed/undirected and weighted; it is stored as a matrix (dense) or adjacency lists (sparse).
- Dijkstra: from one vertex, non-negative weights; *O*(*V*²) or *O*((*V* + *E*) log *V*) with a heap.
- Floyd–Warshall: all pairs, *O*(*V*³); the outer loop is the intermediate vertex *k*.
- A path is restored through the parent array from the end to the start.

## Check yourself

:::check
Q: Why is access to element *i* of a singly linked list *O*(*n*)?
A: The elements are not adjacent in memory; to reach it you must walk the pointers from the head.
Q: How should a road graph with 10⁶ vertices and 3 · 10⁶ edges be stored?
A: With adjacency lists: a matrix would need 10¹² cells.
Q: Why does Dijkstra's algorithm fail with negative weights?
A: A marked vertex's distance is taken as final, while a negative edge could decrease it later.
Q: What is the complexity of the Floyd–Warshall algorithm?
A: *O*(*V*³) time and *O*(*V*²) memory.
:::

:::link https://visualgo.net/en/sssp
VisuAlgo: shortest paths
An animation of Dijkstra's and other algorithms.
:::

:::link https://cp-algorithms.com/graph/all-pair-shortest-path-floyd-warshall.html
CP-Algorithms: Floyd–Warshall
The algorithm, its proof and detecting negative cycles.
:::
