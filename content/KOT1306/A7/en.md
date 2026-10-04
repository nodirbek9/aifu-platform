---
title: IP addresses. Classful addresses. Addressing with masks. Economical use of IP addresses
summary: Practice in binary and decimal conversion; classful addresses and default masks; the prefix–mask table; subnetting by hand; distributing an address space economically with variable-length masks (VLSM); route summarisation (supernetting); /30 and /31 for point-to-point links; joining two subnets with a router in Packet Tracer.
---

## Why it matters

A university has been given the 192.168.10.0/24 block: 50 computers in the library, 100 in the lab, 20 in the dean's office, plus servers and links between routers. If we split the block into equal parts, someone runs short while someone else has hundreds of addresses idle. **VLSM** is the way to give each department a subnet of exactly the size it needs. There is also the reverse problem: **summarising** many small networks as one entry in a routing table. In this session we do both by hand and with a program.

:::note
**After this session you will be able to:**

- convert octets between binary and decimal quickly;
- convert between a prefix, a mask and a block size;
- distribute a given block among departments with VLSM;
- combine several networks into one summarised route;
- connect two subnets through a router in Packet Tracer.
:::

## Binary conversion practice

The "weights" of an octet's bits: **128 64 32 16 8 4 2 1**. From decimal to binary: starting from the left, if you can subtract the weight — 1, otherwise 0.

| Decimal | Binary | Decimal | Binary |
|---|---|---|---|
| 192 | 11000000 | 248 | 11111000 |
| 224 | 11100000 | 252 | 11111100 |
| 240 | 11110000 | 254 | 11111110 |

Only these values occur in mask octets: **0, 128, 192, 224, 240, 248, 252, 254, 255**.

## Prefix, mask and block

| Prefix | Mask | Block (addresses) | Hosts |
|---|---|---|---|
| /24 | 255.255.255.0 | 256 | 254 |
| /25 | 255.255.255.128 | 128 | 126 |
| /26 | 255.255.255.192 | 64 | 62 |
| /27 | 255.255.255.224 | 32 | 30 |
| /28 | 255.255.255.240 | 16 | 14 |
| /29 | 255.255.255.248 | 8 | 6 |
| /30 | 255.255.255.252 | 4 | 2 |
| /31 | 255.255.255.254 | 2 | 2 (point-to-point links only, RFC 3021) |

The rule: the block for *H* hosts is the smallest **power of two** greater than or equal to *H* + 2. 50 hosts → 52 → 64 → /26.

## VLSM — variable-length subnet masks

**The algorithm**:

1. Sort the departments by number of hosts in **decreasing** order.
2. Find the block size for each (a power of 2 ≥ *H* + 2).
3. Allocate one after another from the start of the block: each new subnet starts where the previous one ended.

The largest-first order matters: when the large blocks go first, every block starts at an address that is a multiple of its size (it is aligned) and no gaps are left.

**Links between routers** use /30 (2 hosts) or /31 — giving them a /24 wastes 252 addresses.

## Route summarisation

If the networks 172.16.0.0/24, 172.16.1.0/24, 172.16.2.0/24 and 172.16.3.0/24 are reached through one router, other routers can keep one entry **172.16.0.0/22** instead of four. The summarised prefix is the **common leading bits** of all the addresses:

```text
172.16.0.0  = 10101100.00010000.000000|00.00000000
172.16.1.0  = 10101100.00010000.000000|01.00000000
172.16.2.0  = 10101100.00010000.000000|10.00000000
172.16.3.0  = 10101100.00010000.000000|11.00000000
              common part: 22 bits  ->  172.16.0.0/22
```

Summarisation shrinks the tables and speeds up routing. Be careful: the common prefix of 192.168.1.0/24 and 192.168.2.0/24 is a /22 — it also "covers" the networks 192.168.0.0 and 192.168.3.0, and if they are elsewhere, traffic may go the wrong way.

## Practical work: two subnets in Packet Tracer

1. Place a **2911** router, two **2960** switches and 4 PCs.
2. Take the subnets 192.168.10.128/26 for the library (50 hosts) and 192.168.10.192/27 for the dean's office (20 hosts) (from the VLSM example below).
3. Configure the router interfaces:

```text
Router> enable
Router# configure terminal
Router(config)# interface g0/0
Router(config-if)# ip address 192.168.10.129 255.255.255.192
Router(config-if)# no shutdown
Router(config-if)# interface g0/1
Router(config-if)# ip address 192.168.10.193 255.255.255.224
Router(config-if)# no shutdown
```

4. Give the computers addresses from their subnet and the router interface address as the **gateway** (for example, 192.168.10.130/26 with gateway 192.168.10.129).
5. `ping` from a computer in one subnet to the other. Then remove one computer's gateway and test again — what changed?

A program that computes the network, mask, broadcast and hosts for one CIDR address is this session's independent task (KOT-A7).

## Worked example 1: route summarisation

The first line holds *n*, the next *n* lines networks in the form `a.b.c.d/p`. Print the smallest summarised network covering them all: the prefix is the number of common leading bits of all the addresses (and no larger than the given prefixes).

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


n = int(input())
manzillar = []
p = 32
for _ in range(n):
    ip, prefiks = input().split("/")
    manzillar.append(songa(ip))
    p = min(p, int(prefiks))
while p > 0 and any((x >> (32 - p)) != (manzillar[0] >> (32 - p)) for x in manzillar):
    p -= 1                                   # shorten until a common part is found
maska = 0 if p == 0 else (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF
print(manzilga(manzillar[0] & maska) + "/" + str(p))
```
```java
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] manzillar = new long[n];
        int p = 32;
        for (int i = 0; i < n; i++) {
            String[] qism = in.next().split("/");
            manzillar[i] = songa(qism[0]);
            p = Math.min(p, Integer.parseInt(qism[1]));
        }
        while (p > 0) {
            boolean bir = true;
            for (long x : manzillar) {
                if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
            }
            if (bir) break;
            p--;                                             // shorten until a common part is found
        }
        long maska = p == 0 ? 0 : (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL;
        System.out.println(manzilga(manzillar[0] & maska) + "/" + p);
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    int n;
    cin >> n;
    vector<uint64_t> manzillar(n);
    int p = 32;
    for (int i = 0; i < n; i++) {
        string s;
        cin >> s;
        size_t chiziq = s.find('/');
        manzillar[i] = songa(s.substr(0, chiziq));
        p = min(p, stoi(s.substr(chiziq + 1)));
    }
    while (p > 0) {
        bool bir = true;
        for (uint64_t x : manzillar) {
            if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
        }
        if (bir) break;
        p--;                                                 // shorten until a common part is found
    }
    uint64_t maska = p == 0 ? 0 : (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL;
    cout << manzilga(manzillar[0] & maska) << "/" << p << endl;
    return 0;
}
```

Input:

```text
4
172.16.0.0/24
172.16.1.0/24
172.16.2.0/24
172.16.3.0/24
```

Output:

```text
172.16.0.0/22
```

For `10.1.4.0/24` and `10.1.7.0/24` the answer is `10.1.4.0/22`, and for `192.168.1.0/24` and `192.168.2.0/24` it is `192.168.0.0/22` — note that the latter also covers extra (foreign) networks.

## Worked example 2: VLSM allocation

The first line holds the starting block (`a.b.c.d/p`) and the number of departments *k*. Each of the next *k* lines holds a department name and the number of hosts it needs. Allocate the departments one after another from the block in decreasing order of hosts (input order on a tie). For each department print its subnet and the number of usable hosts in it, or `no room` if it does not fit; at the end print how many addresses were used.

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


blok, k = input().split()
ip, prefiks = blok.split("/")
boshi = songa(ip)
oxiri = boshi + 2 ** (32 - int(prefiks))
bolimlar = []
for _ in range(int(k)):
    nom, xost = input().split()
    bolimlar.append((nom, int(xost)))
bolimlar.sort(key=lambda b: -b[1])            # largest first (stable)

joriy = boshi
for nom, xost in bolimlar:
    hajm = 4
    while hajm < xost + 2:                    # a power of 2 >= hosts + 2
        hajm *= 2
    if joriy + hajm > oxiri:
        print(nom, "no room")
        continue
    p = 32 - (hajm.bit_length() - 1)
    print(nom, manzilga(joriy) + "/" + str(p), hajm - 2)
    joriy += hajm
print("used:", joriy - boshi, "/", oxiri - boshi)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] blok = in.next().split("/");
        int k = in.nextInt();
        long boshi = songa(blok[0]);
        long oxiri = boshi + (1L << (32 - Integer.parseInt(blok[1])));
        List<String> nomlar = new ArrayList<>();
        List<Long> xostlar = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            nomlar.add(in.next());
            xostlar.add(in.nextLong());
        }
        List<Integer> tartib = new ArrayList<>();
        for (int i = 0; i < k; i++) tartib.add(i);
        tartib.sort((a, b) -> Long.compare(xostlar.get(b), xostlar.get(a)));   // largest first (stable)

        long joriy = boshi;
        for (int i : tartib) {
            long hajm = 4;
            while (hajm < xostlar.get(i) + 2) {                // a power of 2 >= hosts + 2
                hajm *= 2;
            }
            if (joriy + hajm > oxiri) {
                System.out.println(nomlar.get(i) + " no room");
                continue;
            }
            int p = 32 - Long.numberOfTrailingZeros(hajm);
            System.out.println(nomlar.get(i) + " " + manzilga(joriy) + "/" + p + " " + (hajm - 2));
            joriy += hajm;
        }
        System.out.println("used: " + (joriy - boshi) + " / " + (oxiri - boshi));
    }
}
```
```cpp
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    string blok;
    int k;
    cin >> blok >> k;
    size_t chiziq = blok.find('/');
    uint64_t boshi = songa(blok.substr(0, chiziq));
    uint64_t oxiri = boshi + (1ULL << (32 - stoi(blok.substr(chiziq + 1))));
    vector<pair<string, long long>> bolimlar(k);
    for (auto& b : bolimlar) cin >> b.first >> b.second;
    stable_sort(bolimlar.begin(), bolimlar.end(), [](const pair<string, long long>& a, const pair<string, long long>& b) {
        return a.second > b.second;                          // largest first (stable)
    });

    uint64_t joriy = boshi;
    for (auto& b : bolimlar) {
        long long hajm = 4;
        while (hajm < b.second + 2) {                        // a power of 2 >= hosts + 2
            hajm *= 2;
        }
        if (joriy + hajm > oxiri) {
            cout << b.first << " no room" << endl;
            continue;
        }
        int p = 32 - __builtin_ctzll(hajm);
        cout << b.first << " " << manzilga(joriy) << "/" << p << " " << hajm - 2 << endl;
        joriy += hajm;
    }
    cout << "used: " << joriy - boshi << " / " << oxiri - boshi << endl;
    return 0;
}
```

Input:

```text
192.168.10.0/24 5
Kutubxona 50
Dekanat 20
Laboratoriya 100
Server 10
Kanal 2
```

Output:

```text
Laboratoriya 192.168.10.0/25 126
Kutubxona 192.168.10.128/26 62
Dekanat 192.168.10.192/27 30
Server 192.168.10.224/28 14
Kanal 192.168.10.240/30 2
used: 244 / 256
```

(The department names are in Uzbek: library, dean's office, laboratory, server, link.) With an equal split (/27, 30 hosts) neither the laboratory nor the library would have fitted; with VLSM everything fits, and 12 addresses (192.168.10.244–255) are left for future needs.

:::warning
**Common mistakes**

- **Forgetting to size the block by *H* + 2 rather than *H*.** 62 hosts fit in a /26, but 63 do not (63 + 2 > 64).
- **Starting VLSM from the small departments.** Then the large blocks end up unaligned and gaps appear in the address space.
- **Giving links a /24.** A /30 or /31 is enough for a link between two routers.
- **Ignoring that a summary may cover foreign networks.** A summarised route is correct only if all the networks it covers really lie in that direction.
- **Giving a gateway from another subnet.** A computer's gateway is always the router interface address in its own subnet.
:::

:::tip
Draw up an address plan for your faculty: allocate the block 10.20.0.0/22 with VLSM to 4 computer labs (30 computers each), 1 Wi-Fi network (200 devices), servers (12) and 3 links between routers. Check the result with the program of the second example.
:::

## Summary

- Mask octets: 0, 128, 192, 224, 240, 248, 252, 254, 255; block = 256 − the mask octet.
- The block for *H* hosts is the power of 2 not smaller than *H* + 2.
- VLSM: sort largest first, allocate one after another — the address space is saved.
- Summarisation: common leading bits → one route with a shorter prefix; beware of over-coverage.
- /30 or /31 for point-to-point links.

## Check yourself

:::check
Q: What is the smallest subnet prefix for 120 hosts?
A: /25 — 128 addresses, 126 hosts (122 ≤ 128).
Q: What are the block size and the number of hosts of the mask 255.255.255.248?
A: Block 8, 6 hosts.
Q: How can 10.0.0.0/24, 10.0.1.0/24, 10.0.2.0/24 and 10.0.3.0/24 be summarised?
A: 10.0.0.0/22.
Q: Why are departments sorted largest first in VLSM?
A: So that every block starts at an address that is a multiple of its size (is aligned) and no gaps are left.
Q: Which prefix is economical for a link between two routers?
A: /30 (2 hosts) or /31 per RFC 3021.
:::

:::link https://www.rfc-editor.org/rfc/rfc4632
RFC 4632: Classless Inter-domain Routing (CIDR)
CIDR, prefixes and the rules of route summarisation.
:::
