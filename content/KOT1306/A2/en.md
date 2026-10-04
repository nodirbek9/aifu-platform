---
title: Studying network topologies
summary: Analysing physical and logical topologies in practice; representing a network as a graph; cable types and wiring schemes (T568A/T568B, straight-through and crossover cables); checking network connectivity when links fail; finding single points of failure (critical nodes); building topologies in Packet Tracer.
---

## Why it matters

In lecture M2 we saw the kinds of topologies and their advantages and drawbacks. In practice an engineer answers a different question: "If this cable is cut or this switch burns out, who is left without a connection?" In this session we describe a network as a **graph**, answer that question with a program, and build topologies in Packet Tracer.

:::note
**After this session you will be able to:**

- write a network diagram as a graph (nodes and edges);
- know the T568A and T568B schemes and when straight-through and crossover cables are used;
- count how many pieces a network splits into when links fail;
- find the single points of failure (critical nodes) in a network;
- build star, ring and hybrid topologies in Packet Tracer and check connectivity.
:::

## A network is a graph

Any topology can be represented as a **graph**: the **nodes** are devices (computers, switches, routers), the **edges** are the links between them. The properties of a topology then become graph notions:

| Network question | Graph notion |
|---|---|
| Can two computers reach each other? | is there a **path** between them |
| How many separate pieces does the network consist of? | the number of **connected components** |
| Which device's failure splits the network? | an **articulation point** (critical node) |
| Which cable's failure splits the network? | a **bridge edge** |
| How many cables go into the central device? | the node's **degree** |

For example, in a star topology the central switch is a critical node and every cable is a bridge edge (if cut, one computer is cut off). A ring has no critical node: if one node fails, the rest stay connected through the remaining part of the ring. A full mesh has neither critical nodes nor bridge edges.

## Cables and wiring schemes

Twisted-pair cable is wired into an RJ-45 connector according to one of two standard schemes:

| Pin | T568A | T568B |
|---|---|---|
| 1 | white-green | white-orange |
| 2 | green | orange |
| 3 | white-orange | white-green |
| 4 | blue | blue |
| 5 | white-blue | white-blue |
| 6 | orange | green |
| 7 | white-brown | white-brown |
| 8 | brown | brown |

- **Straight-through cable** — both ends use the same scheme (B–B). It connects devices of **different** kinds: computer–switch, router–switch.
- **Crossover cable** — one end A, the other B. It connects devices of the **same** kind: computer–computer, switch–switch, router–computer.
- Modern devices have **Auto-MDI/X** — they detect the cable type themselves, but Packet Tracer and exams still ask for the rule.

## Practical work: topologies in Packet Tracer

1. **Star**: one 2960 switch and 4 PCs. Give the computers the addresses 192.168.1.1–192.168.1.4 (mask 255.255.255.0) and run `ping 192.168.1.4` from PC1.
2. **Extended star (tree)**: connect two switches with a crossover cable and add 3 PCs to each. Delete the cable between the two switches and see which computers lose connectivity.
3. **Ring**: connect 4 switches in a ring. In Packet Tracer one port stays orange (blocked) — this is **STP** (Spanning Tree Protocol) putting one link on standby so that frames do not circle the ring forever. Cut one of the working links — after a few seconds the standby link comes up.
4. For each diagram make a table: number of devices, number of cables, critical nodes.

## Worked example 1: the number of pieces when cables are cut

The first line holds the number of nodes *n* and of links *m*, then *m* links. Then come *q* and *q* links that are cut one after another. After each cut print how many connected components the network has split into.

```python
def komponentlar(n, qirralar):
    qoshni = [[] for _ in range(n + 1)]
    for u, v in qirralar:
        qoshni[u].append(v)
        qoshni[v].append(u)
    korilgan = [False] * (n + 1)
    soni = 0
    for boshi in range(1, n + 1):
        if korilgan[boshi]:
            continue
        soni += 1                               # a new piece is found
        korilgan[boshi] = True
        stek = [boshi]
        while stek:
            x = stek.pop()
            for y in qoshni[x]:
                if not korilgan[y]:
                    korilgan[y] = True
                    stek.append(y)
    return soni


n, m = map(int, input().split())
qirralar = [tuple(map(int, input().split())) for _ in range(m)]
q = int(input())
for _ in range(q):
    u, v = map(int, input().split())
    qirralar = [e for e in qirralar if e != (u, v) and e != (v, u)]   # the link is cut
    print(komponentlar(n, qirralar))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int komponentlar(int n, List<int[]> qirralar) {
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int[] e : qirralar) {
            qoshni.get(e[0]).add(e[1]);
            qoshni.get(e[1]).add(e[0]);
        }
        boolean[] korilgan = new boolean[n + 1];
        int soni = 0;
        for (int boshi = 1; boshi <= n; boshi++) {
            if (korilgan[boshi]) continue;
            soni++;                                         // a new piece is found
            korilgan[boshi] = true;
            ArrayDeque<Integer> stek = new ArrayDeque<>();
            stek.push(boshi);
            while (!stek.isEmpty()) {
                int x = stek.pop();
                for (int y : qoshni.get(x)) {
                    if (!korilgan[y]) {
                        korilgan[y] = true;
                        stek.push(y);
                    }
                }
            }
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<int[]> qirralar = new ArrayList<>();
        for (int i = 0; i < m; i++) qirralar.add(new int[] {in.nextInt(), in.nextInt()});
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qirralar.removeIf(e -> (e[0] == u && e[1] == v) || (e[0] == v && e[1] == u));   // the link is cut
            System.out.println(komponentlar(n, qirralar));
        }
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int komponentlar(int n, const vector<pair<int, int>>& qirralar) {
    vector<vector<int>> qoshni(n + 1);
    for (auto& e : qirralar) {
        qoshni[e.first].push_back(e.second);
        qoshni[e.second].push_back(e.first);
    }
    vector<bool> korilgan(n + 1, false);
    int soni = 0;
    for (int boshi = 1; boshi <= n; boshi++) {
        if (korilgan[boshi]) continue;
        soni++;                                             // a new piece is found
        korilgan[boshi] = true;
        vector<int> stek = {boshi};
        while (!stek.empty()) {
            int x = stek.back();
            stek.pop_back();
            for (int y : qoshni[x]) {
                if (!korilgan[y]) {
                    korilgan[y] = true;
                    stek.push_back(y);
                }
            }
        }
    }
    return soni;
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> qirralar(m);
    for (auto& e : qirralar) cin >> e.first >> e.second;
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        int u, v;
        cin >> u >> v;
        vector<pair<int, int>> qolgan;
        for (auto& e : qirralar) {
            if (!((e.first == u && e.second == v) || (e.first == v && e.second == u))) qolgan.push_back(e);
        }
        qirralar = qolgan;                                  // the link is cut
        cout << komponentlar(n, qirralar) << endl;
    }
    return 0;
}
```

Input (1–2–3 a ring, 3–4 a connecting link, 5 and 6 around 4 — a star):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
3
1 2
3 4
4 5
```

Output:

```text
1
2
3
```

When the ring link 1–2 is cut the network stays whole (the path goes 1–3–2). The link 3–4, however, is a bridge edge: when it is cut the network splits in two. If the star link 4–5 is cut, computer 5 is left alone.

## Worked example 2: finding critical nodes

A graph is given. Which nodes are **critical**: if we remove them, the remaining nodes no longer form one connected network? We "switch off" each node in turn and check the rest with a search (*n* searches — enough for small networks). Print the critical nodes in increasing order, or `none` if there are none.

```python
def bogliqmi(n, qoshni, ochiq):
    tirik = [x for x in range(1, n + 1) if x != ochiq]
    korilgan = {tirik[0]}
    stek = [tirik[0]]
    while stek:
        x = stek.pop()
        for y in qoshni[x]:
            if y != ochiq and y not in korilgan:
                korilgan.add(y)
                stek.append(y)
    return len(korilgan) == len(tirik)        # did we reach all of them


n, m = map(int, input().split())
qoshni = [[] for _ in range(n + 1)]
for _ in range(m):
    u, v = map(int, input().split())
    qoshni[u].append(v)
    qoshni[v].append(u)
kritik = [str(v) for v in range(1, n + 1) if not bogliqmi(n, qoshni, v)]
print(" ".join(kritik) if kritik else "none")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean bogliqmi(int n, List<List<Integer>> qoshni, int ochiq) {
        int boshi = ochiq == 1 ? 2 : 1;
        boolean[] korilgan = new boolean[n + 1];
        korilgan[boshi] = true;
        int soni = 1;
        ArrayDeque<Integer> stek = new ArrayDeque<>();
        stek.push(boshi);
        while (!stek.isEmpty()) {
            int x = stek.pop();
            for (int y : qoshni.get(x)) {
                if (y != ochiq && !korilgan[y]) {
                    korilgan[y] = true;
                    soni++;
                    stek.push(y);
                }
            }
        }
        return soni == n - 1;                     // did we reach all of them
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int i = 0; i < m; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qoshni.get(u).add(v);
            qoshni.get(v).add(u);
        }
        List<String> kritik = new ArrayList<>();
        for (int v = 1; v <= n; v++) {
            if (!bogliqmi(n, qoshni, v)) kritik.add(String.valueOf(v));
        }
        System.out.println(kritik.isEmpty() ? "none" : String.join(" ", kritik));
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

bool bogliqmi(int n, const vector<vector<int>>& qoshni, int ochiq) {
    int boshi = ochiq == 1 ? 2 : 1;
    vector<bool> korilgan(n + 1, false);
    korilgan[boshi] = true;
    int soni = 1;
    vector<int> stek = {boshi};
    while (!stek.empty()) {
        int x = stek.back();
        stek.pop_back();
        for (int y : qoshni[x]) {
            if (y != ochiq && !korilgan[y]) {
                korilgan[y] = true;
                soni++;
                stek.push_back(y);
            }
        }
    }
    return soni == n - 1;                         // did we reach all of them
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> qoshni(n + 1);
    for (int i = 0; i < m; i++) {
        int u, v;
        cin >> u >> v;
        qoshni[u].push_back(v);
        qoshni[v].push_back(u);
    }
    string kritik;
    for (int v = 1; v <= n; v++) {
        if (!bogliqmi(n, qoshni, v)) kritik += (kritik.empty() ? "" : " ") + to_string(v);
    }
    cout << (kritik.empty() ? "none" : kritik) << endl;
    return 0;
}
```

Input (the hybrid network of the first example, nothing cut):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
```

Output:

```text
3 4
```

Node 3 separates the ring from the rest, and node 4 is the centre of the star. They should be protected with standby links — for example, adding the link 2–4 makes node 3 no longer critical. For a full mesh of 4 nodes the answer is `none`.

Note: the programs are written for networks of at least 2 nodes (after switching one node off, at least one must remain).

:::warning
**Common mistakes**

- **Mixing up the physical and logical topology.** A ring in Packet Tracer is physically a ring, but since STP blocks one link it works logically as a tree.
- **Using a straight-through cable instead of a crossover one.** On devices without Auto-MDI/X, two switches or two computers will not connect with a straight-through cable.
- **Storing an edge in one direction only.** A cable is two-way — both *u* → *v* and *v* → *u* go into the adjacency lists, and a cut removes both directions.
- **Thinking the critical node is the one with the highest degree.** The degree need not be high: node 3 has degree 3, node 1 has degree 2 — what matters is that it splits the network.
:::

:::tip
Draw your home or faculty network on paper (router, Wi-Fi access points, switches, computers). Turn it into a graph and feed it to the program of the second example: which device is the most dangerous single point of failure?
:::

## Summary

- Any topology is a graph: nodes are devices, edges are links.
- Connectivity after links fail is checked with the number of components, single points of failure with critical nodes.
- T568A/T568B are the RJ-45 wiring schemes; a straight-through cable connects different devices, a crossover cable the same kind.
- In physical rings STP puts one link on standby and forms a logical tree.

## Check yourself

:::check
Q: Which node is critical in a star topology?
A: The central device (a switch or hub) — if it fails, all computers lose connectivity.
Q: If one link of a simple ring is cut, does the network split in two?
A: No, the nodes stay connected through the rest of the ring; it splits after a second cut.
Q: What cable should connect a computer to a switch?
A: A straight-through cable — they are devices of different kinds.
Q: In which pins do the T568A and T568B schemes differ?
A: In pins 1, 2 and 3, 6 — the green and orange pairs are swapped.
Q: Why is one port blocked when 3 switches are connected in a ring in Packet Tracer?
A: STP puts one link on standby so that frames do not circle the ring forever.
:::

:::link https://www.netacad.com/cisco-packet-tracer
Cisco Packet Tracer
Download the network simulator and free introductory courses.
:::
