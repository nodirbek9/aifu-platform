---
title: Network devices and their functions. Network adapters, repeater, bridge, hub
summary: The network adapter and its parameters; the repeater and hub (layer 1), the bridge and switch (layer 2), the router (layer 3); how a switch learns MAC addresses and forwards, filters and floods frames; collision and broadcast domains; inspecting the adapter with operating system commands.
---

## Why it matters

An office network is slow: all computers are plugged into an old hub and their frames keep colliding. Replacing it with a switch changes things at once — why? In this practical session we go step by step through what network devices do with a frame, model a switch's "learning" with a program, and inspect the adapter of your own computer.

:::note
**After this session you will be able to:**

- find the main parameters of a network adapter (MAC address, speed, duplex) with operating system commands;
- explain what a repeater, hub, bridge, switch and router do with a frame or packet;
- tell how a switch fills its MAC table and when it forwards, filters or floods a frame;
- count the collision and broadcast domains in a network.
:::

## The network adapter (NIC)

A **network adapter** (Network Interface Card) is the device that connects a computer to the network medium. It works at layers 1 and 2: it turns bits into signals and assembles frames.

| Parameter | Meaning | Example |
|---|---|---|
| **MAC address** | a unique 48-bit address written by the manufacturer | `3C-52-82-1A-9F-07` |
| **Speed** | the supported speeds | 10/100/1000 Mbit/s |
| **Duplex** | half or full duplex | Full duplex |
| **Connection type** | inside or outside the computer | PCIe, USB, built into the motherboard |
| **Medium** | which cable or radio | RJ-45 (twisted pair), SFP (fibre), Wi-Fi |

The two sides agree on speed and duplex automatically (**autonegotiation**). If one side is set by hand to "100 Mbit/s full" and the other to "auto", a **duplex mismatch** can occur and the network slows down badly.

## What devices do with a frame

| Device | Layer | What it does with an incoming signal or frame |
|---|---|---|
| **Repeater** | 1 | regenerates and amplifies the signal and sends it out of the other port; knows nothing of the frame's content |
| **Hub** | 1 | a multiport repeater: sends the signal out of **all other** ports |
| **Bridge** | 2 | sits between two segments; passes or blocks a frame by MAC address |
| **Switch** | 2 | a multiport bridge: sends a frame only to the port **where the receiver is connected** |
| **Router** | 3 | directs a packet to another network by IP address; does not pass broadcasts |

Old Ethernet networks with repeaters and hubs followed the **5-4-3 rule**: between two computers at most 5 segments and 4 repeaters, and computers on only 3 of the segments — otherwise a collision would not be detected in time.

## How a switch works

A switch keeps in its **MAC table** (CAM table) which MAC addresses are behind each port. When a frame arrives it does two things:

1. **Learning**: it writes (or updates) the frame's **source** address and the port it came in on.
2. **Decision** — by the **destination** address:
   - the address is in the table on another port → **forwarding** to that port only;
   - the address is in the table on the very port the frame came in on → **filtering**: the frame is dropped;
   - the address is not in the table, or it is a broadcast (`FF-FF-FF-FF-FF-FF`) → **flooding**: to all ports except the incoming one.

An entry is usually removed if it is not used for 300 seconds (**aging**). Frame forwarding methods: **store-and-forward** (the frame is received completely and the FCS is checked), **cut-through** (forwarding starts as soon as the destination address is read) and **fragment-free** (after the first 64 bytes are received).

## Collision and broadcast domains

- A **collision domain** is a segment where the signals of two devices transmitting at once can collide. A hub joins all its ports into **one** collision domain; **every port** of a switch or router is a separate collision domain.
- A **broadcast domain** is the set of devices a single broadcast frame reaches. Both hubs and switches pass broadcasts; only a **router** (or a VLAN) stops them. That is why every router interface is a separate broadcast domain.

## Practical work: inspect your own adapter

On Windows open the command prompt (`cmd`):

```text
ipconfig /all          adapters, MAC (Physical Address), IP, gateway, DNS
getmac /v              the MAC addresses of all adapters
arp -a                 the MAC addresses of neighbouring devices the computer "knows"
```

On Linux: `ip link` (MAC and state), `ip addr` (IP addresses), `ethtool eth0` (speed and duplex), `ip neigh` (the ARP table).

Write down: the names of the wired and wireless adapters, their MAC addresses, the link speed, and the IP and MAC address of the default gateway (from `arp -a`).

## Worked example 1: a switch's MAC table

The first line holds the number of switch ports *P* and the number of frames *k*. Each of the next *k* lines holds the port the frame came in on, the source address and the destination address (`FF` is broadcast). For each frame print the switch's decision: `forward <port>`, `filter` or `flood <ports>`; at the end print the MAC table in address order.

```python
p, k = map(int, input().split())
jadval = {}                                     # MAC -> port
for i in range(1, k + 1):
    port, manba, manzil = input().split()
    port = int(port)
    jadval[manba] = port                        # learning
    if manzil != "FF" and manzil in jadval:
        if jadval[manzil] == port:
            print(str(i) + ": filter")
        else:
            print(str(i) + ": forward " + str(jadval[manzil]))
    else:
        boshqalar = [str(x) for x in range(1, p + 1) if x != port]
        print(str(i) + ": flood " + " ".join(boshqalar))
print("table:")
for mac in sorted(jadval):
    print(mac, jadval[mac])
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int p = in.nextInt();
        int k = in.nextInt();
        Map<String, Integer> jadval = new TreeMap<>();    // MAC -> port
        for (int i = 1; i <= k; i++) {
            int port = in.nextInt();
            String manba = in.next();
            String manzil = in.next();
            jadval.put(manba, port);                      // learning
            if (!manzil.equals("FF") && jadval.containsKey(manzil)) {
                if (jadval.get(manzil) == port) {
                    System.out.println(i + ": filter");
                } else {
                    System.out.println(i + ": forward " + jadval.get(manzil));
                }
            } else {
                StringBuilder boshqalar = new StringBuilder();
                for (int x = 1; x <= p; x++) {
                    if (x != port) boshqalar.append(boshqalar.length() > 0 ? " " : "").append(x);
                }
                System.out.println(i + ": flood " + boshqalar);
            }
        }
        System.out.println("table:");
        for (Map.Entry<String, Integer> e : jadval.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    int p, k;
    cin >> p >> k;
    map<string, int> jadval;                              // MAC -> port
    for (int i = 1; i <= k; i++) {
        int port;
        string manba, manzil;
        cin >> port >> manba >> manzil;
        jadval[manba] = port;                             // learning
        if (manzil != "FF" && jadval.count(manzil)) {
            if (jadval[manzil] == port) {
                cout << i << ": filter" << endl;
            } else {
                cout << i << ": forward " << jadval[manzil] << endl;
            }
        } else {
            string boshqalar;
            for (int x = 1; x <= p; x++) {
                if (x != port) boshqalar += (boshqalar.empty() ? "" : " ") + to_string(x);
            }
            cout << i << ": flood " << boshqalar << endl;
        }
    }
    cout << "table:" << endl;
    for (auto& e : jadval) {
        cout << e.first << " " << e.second << endl;
    }
    return 0;
}
```

Input:

```text
4 7
1 AA BB
2 BB AA
3 CC FF
1 AA CC
4 DD BB
1 EE AA
3 BB CC
```

Output:

```text
1: flood 2 3 4
2: forward 1
3: flood 1 2 4
4: forward 3
5: forward 2
6: filter
7: filter
table:
AA 1
BB 3
CC 3
DD 4
EE 1
```

Analysis: in frame 1 `BB` is still unknown — the frame is flooded. Frame 3 is a broadcast — always flooded. In frame 6 `EE` and `AA` are both on port 1 (a hub sits there), so the frame does not cross the switch — it is filtered. In frame 7 the laptop `BB` has been moved to port 3: the table is updated.

## Worked example 2: counting domains

A router has *r* interfaces. Each interface has either `hub h` (a hub with *h* computers), `switch s` (a switch with *s* computers) or `host` (one computer directly) connected to it. Print the number of collision and broadcast domains.

The count: a hub segment (together with its link to the router) is 1 collision domain; a switch segment is *s* + 1 (each computer port plus the port linked to the router); a directly connected computer is 1. The broadcast domains are the number of interfaces *r*.

```python
r = int(input())
kolliziya = 0
for _ in range(r):
    qism = input().split()
    if qism[0] == "hub":
        kolliziya += 1                   # the whole hub is one domain
    elif qism[0] == "switch":
        kolliziya += int(qism[1]) + 1    # every port + the link to the router
    else:
        kolliziya += 1                   # a directly connected computer
print("collision domains:", kolliziya)
print("broadcast domains:", r)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = Integer.parseInt(in.nextLine().trim());
        int kolliziya = 0;
        for (int i = 0; i < r; i++) {
            String[] qism = in.nextLine().trim().split(" ");
            if (qism[0].equals("hub")) {
                kolliziya += 1;                           // the whole hub is one domain
            } else if (qism[0].equals("switch")) {
                kolliziya += Integer.parseInt(qism[1]) + 1;   // every port + the link to the router
            } else {
                kolliziya += 1;                           // a directly connected computer
            }
        }
        System.out.println("collision domains: " + kolliziya);
        System.out.println("broadcast domains: " + r);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int r = stoi(satr);
    int kolliziya = 0;
    for (int i = 0; i < r; i++) {
        getline(cin, satr);
        istringstream qism(satr);
        string tur;
        qism >> tur;
        if (tur == "hub") {
            kolliziya += 1;                               // the whole hub is one domain
        } else if (tur == "switch") {
            int s;
            qism >> s;
            kolliziya += s + 1;                           // every port + the link to the router
        } else {
            kolliziya += 1;                               // a directly connected computer
        }
    }
    cout << "collision domains: " << kolliziya << endl;
    cout << "broadcast domains: " << r << endl;
    return 0;
}
```

Input:

```text
3
hub 5
switch 8
host
```

Output:

```text
collision domains: 11
broadcast domains: 3
```

If we replace the hub with a 5-port switch, the first segment has 6 collision domains instead of 1 — each computer gets its own "lane" and the collisions disappear. The number of broadcast domains does not change.

:::warning
**Common mistakes**

- **Thinking a hub sends a frame only to the receiver.** A hub does not read addresses — the signal goes out of all ports.
- **Believing a switch blocks broadcasts.** A switch floods broadcasts to all ports; only a router or a VLAN splits a broadcast domain.
- **Thinking a switch learns the destination address.** It records only the **source** address and the incoming port.
- **Confusing a MAC address with an IP address.** A MAC is a physical address tied to the adapter (layer 2), an IP is a logical address in the network (layer 3).
:::

:::tip
In Cisco Packet Tracer connect 4 computers first to a hub and then to a switch, and watch a single `ping` in simulation mode: the hub sends the frame to every computer, while the switch, after the first frame, sends it only to the right port. View the switch's MAC table with `show mac-address-table`.
:::

## Summary

- A network adapter connects a computer to the medium; it has a MAC address, a speed and a duplex mode, seen with `ipconfig /all`, `getmac`, `ip link`.
- Repeaters and hubs are layer 1 (signal everywhere), bridges and switches are layer 2 (by MAC), routers are layer 3 (by IP).
- A switch learns the source address and forwards, filters or floods by the destination.
- Every switch port is a separate collision domain; routers separate broadcast domains.

## Check yourself

:::check
Q: Which address does a switch write into its MAC table when a frame arrives?
A: The frame's source MAC address and the port the frame came in on.
Q: What does a switch do if the destination address is not in the table?
A: It floods the frame to all ports except the incoming one.
Q: 24 computers are connected to a 24-port hub. How many collision domains are there?
A: One — a hub joins all its ports into one collision domain.
Q: 4 switches are connected to the 4 interfaces of a router. How many broadcast domains are there?
A: 4 — every router interface is a separate broadcast domain.
Q: What is the difference between store-and-forward and cut-through?
A: Store-and-forward receives the whole frame and checks the FCS; cut-through starts forwarding as soon as it reads the destination address — faster, but it also passes damaged frames.
:::

:::link https://en.wikipedia.org/wiki/Network_switch
Network switch
An article on switch types, the MAC table, forwarding methods and domains.
:::
