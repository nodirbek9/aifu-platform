---
title: Network protocols and routing protocols. Network protocols and services (ARP, ICMP, DHCP). Routing protocols and how they work
summary: ARP — finding the MAC address for an IP address, the ARP cache, the danger of ARP spoofing; ICMP messages and ping, tracert; DHCP — the DORA process, leases, the relay agent; the routing table, static and dynamic routing, metric and administrative distance; distance-vector (RIP), link-state (OSPF) and path-vector (BGP) protocols; interior and exterior routing.
---

## Why it matters

When you plug a computer into a network, a lot happens within a second: it gets an address from **DHCP**, finds the gateway's MAC address with **ARP**, and packets travel along tables built by **routing protocols**. If there is a problem, **ICMP** reports it: "destination unreachable" or "time exceeded". Without knowing these helper protocols you cannot diagnose a network fault.

:::note
**After this lesson you will be able to:**

- explain how an ARP request and reply proceed and what the ARP cache is for;
- name the main ICMP messages and how the `ping` and `tracert` commands work with them;
- explain DHCP's DORA process step by step;
- tell static from dynamic routing, and distance-vector from link-state protocols;
- calculate how the distance-vector algorithm builds routing tables.
:::

## ARP — matching addresses

A computer wants to send a packet to 192.168.1.1, but an Ethernet frame needs a **MAC address**. **ARP** (Address Resolution Protocol) solves this:

1. The computer checks its **ARP cache** (`arp -a`). If there is an entry, it uses it at once.
2. If not, it sends a **broadcast** ARP request: "Who has 192.168.1.1? Tell 192.168.1.10".
3. The owner of 192.168.1.1 sends back a **unicast** reply: "192.168.1.1 is me, MAC: 00:1A:2B:3C:4D:5E".
4. The reply is stored in the cache and kept for a few minutes.

Important: if the receiver is **on another network**, the computer asks not for its MAC but for the MAC of the **default gateway** (the router). **Gratuitous ARP** — a device announces its own address (to detect IP conflicts). **ARP spoofing** — an attacker poses as the gateway with fake replies and routes the traffic through itself (man-in-the-middle); the defence is Dynamic ARP Inspection on the switch.

## ICMP — service messages

| Type | Message | When |
|---|---|---|
| 0 / 8 | Echo Reply / Echo Request | `ping` |
| 3 | Destination Unreachable | the network, host or port does not exist; fragmentation needed but DF is set |
| 5 | Redirect | a router points a host to a better gateway |
| 11 | Time Exceeded | the TTL dropped to 0 — `tracert` uses this |

`ping` shows whether there is connectivity and the round-trip time (RTT); `tracert` (`traceroute` on Linux) sends packets with TTL = 1, 2, 3, … and finds every router on the way. In some networks ICMP is blocked by a firewall — a `ping` that gets no reply does not mean the host is down.

## DHCP — assigning addresses automatically

The **DHCP** (Dynamic Host Configuration Protocol, UDP ports: server 67, client 68) process is **DORA**:

| Step | From → to | Content |
|---|---|---|
| **D**iscover | client → broadcast | "Is there a DHCP server on the network?" (the client has no IP yet: source 0.0.0.0) |
| **O**ffer | server → client | "I offer 192.168.1.57, the mask, gateway, DNS, a 24-hour lease" |
| **R**equest | client → broadcast | "I'll take 192.168.1.57" (other servers learn of it too) |
| **A**ck | server → client | "Confirmed" |

The address is **leased**; halfway through the lease the client renews it. If the server is on another subnet, a **DHCP relay agent** on the router (`ip helper-address`) forwards the broadcast to the server. `ipconfig /release` and `ipconfig /renew` repeat the process by hand.

## Routing

For every packet a router picks an entry from its **routing table**: network/prefix → next hop or outgoing interface. If several entries match, the **longest prefix** wins (a program for this choice is the task of practical session A8).

How the table gets filled:

- **Directly connected** networks — as soon as an interface is given an IP.
- **Static** routes — typed by an administrator (`ip route 10.0.0.0 255.0.0.0 192.168.1.2`); for small networks and the default route `0.0.0.0/0`.
- **Dynamic** — routers tell each other through routing protocols, build the table themselves and find a new path on a failure.

If there are routes to the same network from different sources, the choice is by **administrative distance** (a trust level; Cisco: connected 0, static 1, OSPF 110, RIP 120), and within one protocol by the **metric** (hop count, link cost).

## Routing protocols

| Kind | Idea | Protocol | Metric | Feature |
|---|---|---|---|---|
| **Distance-vector** | each router sends its neighbours its own table ("which network — how far") | **RIP** | hop count (16 = unreachable) | simple; converges slowly, the "count to infinity" problem |
| **Link-state** | each router gets the map of the whole network and computes the shortest paths with Dijkstra's algorithm | **OSPF**, IS-IS | link cost (depends on speed) | converges fast, for large networks; areas |
| **Path-vector** | a route is sent together with the list of autonomous systems it passes | **BGP** | policies, AS path length | the Internet's inter-provider protocol |

An **autonomous system** (AS) is a group of networks run by one organisation. Inside an AS — **interior** protocols (IGP: RIP, OSPF, EIGRP), between ASes — the **exterior** protocol (EGP: BGP).

Ways of easing RIP's problems: **split horizon** (not advertising a route back to the neighbour it came from), **poison reverse**, the 15-hop maximum and **hold-down** timers.

## Worked example 1: the distance-vector algorithm

*n* routers and *m* links (with costs) are given. In every round all routers at once take their neighbours' distance vectors from the previous round and update their own table: *d*(*i*, *j*) = min(*c*(*i*, *k*) + *d*(*k*, *j*)) over all neighbours *k*. At the start every router knows only itself. Print how many rounds are needed until the tables stop changing, then the table of router *s* (destination, cost, next hop; the smaller-numbered neighbour on a tie).

```python
CHEKSIZ = 10 ** 9
n, m = map(int, input().split())
qoshni = [dict() for _ in range(n + 1)]
for _ in range(m):
    u, v, c = map(int, input().split())
    qoshni[u][v] = c
    qoshni[v][u] = c
s = int(input())

d = [[CHEKSIZ] * (n + 1) for _ in range(n + 1)]
for i in range(1, n + 1):
    d[i][i] = 0
raund = 0
while True:
    yangi = [qator[:] for qator in d]
    for i in range(1, n + 1):
        for j in range(1, n + 1):
            for k, c in qoshni[i].items():             # the neighbour's old vector
                if d[k][j] < CHEKSIZ and c + d[k][j] < yangi[i][j]:
                    yangi[i][j] = c + d[k][j]
    if yangi == d:
        break
    d = yangi
    raund += 1

print("rounds:", raund)
for j in range(1, n + 1):
    if j == s:
        continue
    keyingi = min(k for k, c in qoshni[s].items() if d[k][j] < CHEKSIZ and c + d[k][j] == d[s][j])
    print(j, d[s][j], keyingi)
```
```java
import java.util.Scanner;

public class Main {
    static final long CHEKSIZ = 1_000_000_000L;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        long[][] narx = new long[n + 1][n + 1];                // 0 — no link
        for (int i = 0; i < m; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            long c = in.nextLong();
            narx[u][v] = c;
            narx[v][u] = c;
        }
        int s = in.nextInt();

        long[][] d = new long[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) d[i][j] = i == j ? 0 : CHEKSIZ;
        }
        int raund = 0;
        while (true) {
            long[][] yangi = new long[n + 1][];
            for (int i = 0; i <= n; i++) yangi[i] = d[i].clone();
            boolean ozgardi = false;
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    for (int k = 1; k <= n; k++) {             // the neighbour's old vector
                        if (narx[i][k] > 0 && d[k][j] < CHEKSIZ && narx[i][k] + d[k][j] < yangi[i][j]) {
                            yangi[i][j] = narx[i][k] + d[k][j];
                            ozgardi = true;
                        }
                    }
                }
            }
            if (!ozgardi) break;
            d = yangi;
            raund++;
        }

        System.out.println("rounds: " + raund);
        for (int j = 1; j <= n; j++) {
            if (j == s) continue;
            int keyingi = 0;
            for (int k = 1; k <= n && keyingi == 0; k++) {
                if (narx[s][k] > 0 && d[k][j] < CHEKSIZ && narx[s][k] + d[k][j] == d[s][j]) keyingi = k;
            }
            System.out.println(j + " " + d[s][j] + " " + keyingi);
        }
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

const long long CHEKSIZ = 1000000000LL;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> narx(n + 1, vector<long long>(n + 1, 0));   // 0 — no link
    for (int i = 0; i < m; i++) {
        int u, v;
        long long c;
        cin >> u >> v >> c;
        narx[u][v] = c;
        narx[v][u] = c;
    }
    int s;
    cin >> s;

    vector<vector<long long>> d(n + 1, vector<long long>(n + 1, CHEKSIZ));
    for (int i = 1; i <= n; i++) d[i][i] = 0;
    int raund = 0;
    while (true) {
        vector<vector<long long>> yangi = d;
        bool ozgardi = false;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                for (int k = 1; k <= n; k++) {                 // the neighbour's old vector
                    if (narx[i][k] > 0 && d[k][j] < CHEKSIZ && narx[i][k] + d[k][j] < yangi[i][j]) {
                        yangi[i][j] = narx[i][k] + d[k][j];
                        ozgardi = true;
                    }
                }
            }
        }
        if (!ozgardi) break;
        d = yangi;
        raund++;
    }

    cout << "rounds: " << raund << endl;
    for (int j = 1; j <= n; j++) {
        if (j == s) continue;
        int keyingi = 0;
        for (int k = 1; k <= n && keyingi == 0; k++) {
            if (narx[s][k] > 0 && d[k][j] < CHEKSIZ && narx[s][k] + d[k][j] == d[s][j]) keyingi = k;
        }
        cout << j << " " << d[s][j] << " " << keyingi << endl;
    }
    return 0;
}
```

Input:

```text
5 6
1 2 1
2 3 2
3 4 1
1 4 5
4 5 3
3 5 7
1
```

Output:

```text
rounds: 4
2 1 2
3 3 2
4 4 2
5 7 2
```

Router 1 has a direct link to router 4 (cost 5), but the path 1–2–3–4 is cheaper (4) — the algorithm "discovers" this in rounds two and three. The longest path (from 1 to 5) has 4 links, so the tables settle in 4 rounds: in distance-vector protocols information spreads by only one hop per round.

## Worked example 2: the ARP cache

The first line holds the lifetime *T* of a cache entry (seconds) and the number of events *k*. Each of the next *k* lines holds a time (seconds, in increasing order) and the IP address the computer wants to send a packet to. If the entry is in the cache and younger than *T* — `cache`, otherwise an ARP request is sent (`ARP`) and the entry is refreshed. At the end print the number of ARP requests sent.

```python
t_yashash, k = map(int, input().split())
kesh = {}                                     # IP -> time stored
soni = 0
for _ in range(k):
    vaqt, ip = input().split()
    vaqt = int(vaqt)
    if ip in kesh and vaqt - kesh[ip] < t_yashash:
        print(vaqt, ip, "cache")
    else:
        print(vaqt, ip, "ARP")               # broadcast request and reply
        kesh[ip] = vaqt
        soni += 1
print("ARP requests:", soni)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int tYashash = in.nextInt();
        int k = in.nextInt();
        Map<String, Integer> kesh = new HashMap<>();          // IP -> time stored
        int soni = 0;
        for (int i = 0; i < k; i++) {
            int vaqt = in.nextInt();
            String ip = in.next();
            if (kesh.containsKey(ip) && vaqt - kesh.get(ip) < tYashash) {
                System.out.println(vaqt + " " + ip + " cache");
            } else {
                System.out.println(vaqt + " " + ip + " ARP");  // broadcast request and reply
                kesh.put(ip, vaqt);
                soni++;
            }
        }
        System.out.println("ARP requests: " + soni);
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    int tYashash, k;
    cin >> tYashash >> k;
    map<string, int> kesh;                                    // IP -> time stored
    int soni = 0;
    for (int i = 0; i < k; i++) {
        int vaqt;
        string ip;
        cin >> vaqt >> ip;
        if (kesh.count(ip) && vaqt - kesh[ip] < tYashash) {
            cout << vaqt << " " << ip << " cache" << endl;
        } else {
            cout << vaqt << " " << ip << " ARP" << endl;      // broadcast request and reply
            kesh[ip] = vaqt;
            soni++;
        }
    }
    cout << "ARP requests: " << soni << endl;
    return 0;
}
```

Input:

```text
120 6
0 192.168.1.1
5 192.168.1.1
30 192.168.1.20
100 192.168.1.1
125 192.168.1.1
130 192.168.1.20
```

Output:

```text
0 192.168.1.1 ARP
5 192.168.1.1 cache
30 192.168.1.20 ARP
100 192.168.1.1 cache
125 192.168.1.1 ARP
130 192.168.1.20 cache
ARP requests: 3
```

At second 125 the gateway entry is 125 ≥ 120 seconds old — it has expired and is asked for again. Without a cache every packet would need a broadcast request — 6 instead of 3.

:::warning
**Common mistakes**

- **Thinking ARP asks for the MAC address of a computer on another network.** What is asked for is the MAC address of the default gateway.
- **Concluding that a host is down because `ping` fails.** ICMP may be blocked by a firewall.
- **Believing DHCP's Request step is unicast.** The client broadcasts the Request too — so that other servers withdraw their offers.
- **Thinking RIP's metric is link speed.** RIP only counts hops: a slow 2-hop path counts as "better" than a fast 3-hop one.
- **Confusing administrative distance with the metric.** Administrative distance compares different sources, the metric compares paths within one protocol.
:::

:::tip
Look at your ARP cache with `arp -a`, clear it with `arp -d *` (with administrator rights) and look again after a `ping`. In Wireshark, with the filter `arp or dhcp`, find the four DORA steps during `ipconfig /release` and `ipconfig /renew`.
:::

## Summary

- ARP finds the MAC address for an IP address with a broadcast request and a unicast reply; the result is kept in a cache.
- ICMP carries service messages: Echo (ping), Destination Unreachable, Time Exceeded (tracert).
- DHCP is DORA: Discover, Offer, Request, Ack; the address is leased, and another network needs a relay agent.
- The routing table holds connected, static and dynamic entries; the choice is by longest prefix, administrative distance, metric.
- Protocols: distance-vector (RIP), link-state (OSPF), path-vector (BGP); IGP inside an AS, BGP between ASes.

## Check yourself

:::check
Q: To what address are an ARP request and an ARP reply sent?
A: The request is broadcast (FF:FF:FF:FF:FF:FF), the reply is unicast to the requester.
Q: Which ICMP message does `tracert` use?
A: Time Exceeded (type 11) — the message routers send when the TTL runs out.
Q: What does the abbreviation DORA stand for?
A: Discover, Offer, Request, Acknowledge — the steps of getting an address through DHCP.
Q: Why does OSPF converge faster than RIP?
A: Every router gets the map of the whole network and computes the shortest paths itself; in RIP information spreads by only one hop per update.
Q: What does a RIP metric of 16 mean?
A: The network is unreachable (infinity).
:::

:::link https://www.rfc-editor.org/rfc/rfc2131
RFC 2131: Dynamic Host Configuration Protocol
The specification of the DHCP protocol.
:::
