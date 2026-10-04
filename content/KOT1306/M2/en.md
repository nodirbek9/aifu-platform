---
title: The basic principles of building networks. Network topologies. Network architecture. Network devices
summary: The principles of building networks — standardisation, layering, redundancy; physical and logical topologies (bus, star, ring, full mesh, tree, hybrid), their advantages and drawbacks; network architecture; network devices — adapter, repeater, hub, bridge, switch, router.
---

## Why it matters

Joining two computers with one cable is easy. But how should a university network of 500 computers, 20 printers and 10 servers be built so that it is fast, does not stop when one cable breaks, and is easy to extend? The answer comes from the **topology** (how the devices are connected), the **architecture** (how the network is split into layers) and the **devices** (what sits at every node).

:::note
**After this lesson you will be able to:**

- explain the basic principles of building networks;
- draw the main topologies and compare their advantages and drawbacks;
- tell physical and logical topology apart;
- state the purpose of network devices and at which layer of the OSI model they work;
- compute the number of links a topology needs.
:::

## Principles of building networks

| Principle | Meaning |
|---|---|
| **Standardisation** | devices from different manufacturers work together under common standards (IEEE 802, TCP/IP) |
| **Layering** (modularity) | a complex task is split into layers; each layer talks only to its neighbours |
| **Redundancy** | backup links and devices in critical places — if one fails, another takes over |
| **Scalability** | the ability to extend the network without rebuilding it entirely |
| **Hierarchy** | the core, distribution and access layers |

## Topologies

**Physical topology** is how cables and devices are actually connected. **Logical topology** is the path a signal or data actually takes. For example, a hub-based network is physically a star but logically a bus: the hub spreads the signal to all ports.

| Topology | Structure | Number of links (*n* nodes) | Advantage | Drawback |
|---|---|---|---|---|
| **Bus** | all nodes are attached to one shared cable | 1 shared cable | cheap, simple | if the cable breaks, the whole network stops; collisions |
| **Star** | every node connects to a central device | *n* | a broken cable affects only one node; easy to extend | if the central device fails, everything stops |
| **Ring** | every node connects to its two neighbours, forming a closed ring | *n* | no collisions (with the token method) | one break (in a simple ring) breaks the network |
| **Full mesh** | every node connects to all others | *n*(*n* − 1)/2 | the most reliable | very expensive; the number of links grows quadratically with *n* |
| **Tree** | a hierarchical combination of stars | *n* − 1 | convenient for large networks | if an upper-level device fails, the whole branch is cut off |
| **Hybrid** | a mixture of topologies | — | flexible | complex to design |

Modern LANs are almost always a **switched star** or a **tree**; WANs and provider networks are **partially meshed** for reliability.

## Network architecture

A **network architecture** is a general description of a network's structure, protocols and devices: how it is split into layers (OSI, TCP/IP), which protocols are used and what roles the nodes play. The main architectural models:

- **Client–server** — services sit on dedicated servers;
- **Peer-to-peer** — the nodes are equal;
- **The three-tier hierarchical model** (Cisco) — the access layer (switches where users' computers connect), the distribution layer (routing between buildings and floors), the core (a fast backbone).

## Network devices

| Device | Purpose | OSI layer | Main feature |
|---|---|---|---|
| **Network adapter** (NIC) | connects a computer to the network | 1–2 | has its own MAC address |
| **Repeater** | amplifies a weakened signal | 1 | does not understand the data's content |
| **Hub** | a multi-port repeater | 1 | sends a frame to **all** ports |
| **Bridge** | joins two segments | 2 | passes or blocks a frame by MAC address |
| **Switch** | a multi-port bridge | 2 | sends a frame only to the **right** port |
| **Router** | joins different networks | 3 | chooses the best path by IP address |
| **Gateway** | joins different protocols | 4–7 | translates protocols into one another |
| **Wireless access point** | connects Wi-Fi devices to the wired network | 1–2 | a bridge between radio and Ethernet |

## Worked example 1: the number of links for a topology

The first line holds *k*, the next *k* lines a topology name (`shina` — bus, `yulduz` — star, `halqa` — ring, `toliq` — full mesh, `daraxt` — tree) and the number of nodes *n*. For each, print the number of links (cables) needed: a bus has one shared cable, a star *n* (to the central device), a ring *n*, a full mesh *n*(*n* − 1)/2, a tree *n* − 1.

```python
def liniyalar(tur, n):
    if tur == "shina":
        return 1                        # one shared cable
    if tur in ("yulduz", "halqa"):
        return n
    if tur == "toliq":
        return n * (n - 1) // 2         # every pair is a separate link
    return n - 1                        # a tree


k = int(input())
for _ in range(k):
    tur, n = input().split()
    print(tur, liniyalar(tur, int(n)))
```
```java
import java.util.Scanner;

public class Main {
    static long liniyalar(String tur, long n) {
        switch (tur) {
            case "shina": return 1;                 // one shared cable
            case "yulduz": case "halqa": return n;
            case "toliq": return n * (n - 1) / 2;   // every pair is a separate link
            default: return n - 1;                  // a tree
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        for (int i = 0; i < k; i++) {
            String tur = in.next();
            long n = in.nextLong();
            System.out.println(tur + " " + liniyalar(tur, n));
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

long long liniyalar(const string& tur, long long n) {
    if (tur == "shina") return 1;                   // one shared cable
    if (tur == "yulduz" || tur == "halqa") return n;
    if (tur == "toliq") return n * (n - 1) / 2;     // every pair is a separate link
    return n - 1;                                   // a tree
}

int main() {
    int k;
    cin >> k;
    for (int i = 0; i < k; i++) {
        string tur;
        long long n;
        cin >> tur >> n;
        cout << tur << " " << liniyalar(tur, n) << endl;
    }
    return 0;
}
```

Input:

```text
4
yulduz 30
halqa 30
toliq 30
toliq 300
```

Output:

```text
yulduz 30
halqa 30
toliq 435
toliq 44850
```

For 30 computers a star needs 30 cables, a full mesh 435; for 300 computers, 44,850 (*liniyalar* means links, *tur* the type). That is why a full mesh is used only between a few critical nodes (say, the core routers).

## Worked example 2: identifying the topology from a graph

The first line holds the number of nodes *n* and of connections *m*, the next *m* lines connections `u v` (each pair once). Identify the topology: `yulduz` (one centre connected to all, the others only to the centre), `halqa` (all nodes have 2 neighbours and the graph is connected), `toliq` (every node connected to all others), `chiziq` — a line (two end nodes with 1 neighbour, the rest with 2, connected), otherwise `aralash` — mixed. Assume *n* ≥ 4.

```python
from collections import deque

n, m = map(int, input().split())
qoshni = [[] for _ in range(n + 1)]
for _ in range(m):
    u, v = map(int, input().split())
    qoshni[u].append(v)
    qoshni[v].append(u)

daraja = [len(qoshni[i]) for i in range(1, n + 1)]

korilgan = {1}                              # check connectivity with BFS
navbat = deque([1])
while navbat:
    v = navbat.popleft()
    for u in qoshni[v]:
        if u not in korilgan:
            korilgan.add(u)
            navbat.append(u)
boglamli = len(korilgan) == n

if all(d == n - 1 for d in daraja):
    print("toliq")
elif daraja.count(n - 1) == 1 and daraja.count(1) == n - 1:
    print("yulduz")
elif boglamli and all(d == 2 for d in daraja):
    print("halqa")
elif boglamli and daraja.count(1) == 2 and daraja.count(2) == n - 2:
    print("chiziq")
else:
    print("aralash")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int k = 0; k < m; k++) {
            int u = in.nextInt(), v = in.nextInt();
            qoshni.get(u).add(v);
            qoshni.get(v).add(u);
        }

        boolean[] korilgan = new boolean[n + 1];        // check connectivity with BFS
        ArrayDeque<Integer> navbat = new ArrayDeque<>();
        korilgan[1] = true;
        navbat.add(1);
        int soni = 1;
        while (!navbat.isEmpty()) {
            int v = navbat.poll();
            for (int u : qoshni.get(v)) {
                if (!korilgan[u]) {
                    korilgan[u] = true;
                    soni++;
                    navbat.add(u);
                }
            }
        }
        boolean boglamli = soni == n;

        int markazlar = 0, birlar = 0, ikkilar = 0;
        for (int i = 1; i <= n; i++) {
            int d = qoshni.get(i).size();
            if (d == n - 1) markazlar++;
            if (d == 1) birlar++;
            if (d == 2) ikkilar++;
        }

        if (markazlar == n) System.out.println("toliq");
        else if (markazlar == 1 && birlar == n - 1) System.out.println("yulduz");
        else if (boglamli && ikkilar == n) System.out.println("halqa");
        else if (boglamli && birlar == 2 && ikkilar == n - 2) System.out.println("chiziq");
        else System.out.println("aralash");
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> qoshni(n + 1);
    for (int k = 0; k < m; k++) {
        int u, v;
        cin >> u >> v;
        qoshni[u].push_back(v);
        qoshni[v].push_back(u);
    }

    vector<bool> korilgan(n + 1, false);                // check connectivity with BFS
    queue<int> navbat;
    korilgan[1] = true;
    navbat.push(1);
    int soni = 1;
    while (!navbat.empty()) {
        int v = navbat.front();
        navbat.pop();
        for (int u : qoshni[v]) {
            if (!korilgan[u]) {
                korilgan[u] = true;
                soni++;
                navbat.push(u);
            }
        }
    }
    bool boglamli = soni == n;

    int markazlar = 0, birlar = 0, ikkilar = 0;
    for (int i = 1; i <= n; i++) {
        int d = qoshni[i].size();
        if (d == n - 1) markazlar++;
        if (d == 1) birlar++;
        if (d == 2) ikkilar++;
    }

    if (markazlar == n) cout << "toliq" << endl;
    else if (markazlar == 1 && birlar == n - 1) cout << "yulduz" << endl;
    else if (boglamli && ikkilar == n) cout << "halqa" << endl;
    else if (boglamli && birlar == 2 && ikkilar == n - 2) cout << "chiziq" << endl;
    else cout << "aralash" << endl;
    return 0;
}
```

| Input (*n*, connections) | Result |
|---|---|
| 5: 1-2, 1-3, 1-4, 1-5 | `yulduz` |
| 4: 1-2, 2-3, 3-4, 4-1 | `halqa` |
| 4: 1-2, 2-3, 3-4 | `chiziq` |
| 4: all 6 pairs | `toliq` |
| 5: 1-2, 2-3, 3-1, 3-4, 4-5 | `aralash` |

Two signs are enough to identify a topology: every node's **degree** (how many neighbours it has) and whether the graph is **connected**. Two separate rings also have all nodes of degree 2, so for the ring and the line connectivity is checked with BFS (*qoshni* are the neighbours, *daraja* the degree, *boglamli* connected). This analysis is used in network monitoring systems: they collect the devices' connections (say, via LLDP) and draw the network map automatically.

:::warning
**Common mistakes**

- **Mixing up physical and logical topology.** A hub-based star works logically as a bus: all computers compete for one "shared channel".
- **Taking a hub and a switch for the same device.** A hub sends a frame to every port, a switch only to the right one.
- **Forgetting the star's "single point of failure".** If the central switch fails, the whole segment stops — critical networks have a backup switch.
- **Using a router in place of a switch.** A router joins different networks (by IP); connecting devices within one LAN is a switch's job.
:::

:::tip
Draw on paper the network of your university or your home: which device is where and how it is connected. Then mark which users would lose connection if each device failed — this shows the network's weak points.
:::

## Summary

- The principles of building networks: standardisation, layering, redundancy, scalability, hierarchy.
- The main topologies: bus, star, ring, full mesh, tree, hybrid; physical and logical topology may differ.
- A full mesh needs *n*(*n* − 1)/2 links — the most reliable but the most expensive.
- Devices by layer: repeater and hub (1), bridge and switch (2), router (3), gateway (4–7).

## Check yourself

:::check
Q: How many links does a full mesh of 10 nodes have?
A: 10 · 9 / 2 = 45.
Q: What is the main drawback of the star topology?
A: The central device is a single point of failure: if it breaks, the whole network stops.
Q: What does a switch base its decision on where to send a frame?
A: The receiver's MAC address (with the help of its MAC table).
Q: Which device joins different IP networks?
A: A router.
Q: What topology does a hub-based network have physically and logically?
A: Physically a star, logically a bus.
:::

:::link https://www.cisco.com/c/en/us/solutions/small-business/resource-center/networking/networking-basics.html
Cisco: networking basics
An introduction to network devices and topologies.
:::
