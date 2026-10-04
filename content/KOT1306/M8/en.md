---
title: Addressing network nodes with IPv4. The IPv4 protocol and methods of addressing network nodes based on it
summary: The structure of an IPv4 address — network and host parts, dotted decimal and binary notation; classful addressing (A–E); the mask and prefix (CIDR); special addresses — network, broadcast, loopback, link-local, multicast; private addresses (RFC 1918) and NAT; the idea of subnetting and calculating by hand; static and dynamic address assignment.
---

## Why it matters

Every computer, phone and server is found on a network by its **IP address**. But an address is not just four random numbers: one part of it says "which network", the rest "which device on that network". A router directs a packet precisely by this split. Without knowing how to read an address, how to choose the matching mask and how to divide a network into parts, you cannot design any network.

:::note
**After this lesson you will be able to:**

- write an IPv4 address in dotted decimal and binary form;
- determine an address's class and type (private, public, loopback, multicast and others);
- convert between a mask and a prefix and explain what they mean;
- calculate the network and broadcast addresses for a given address and prefix by hand;
- choose a prefix from the required number of subnets and hosts.
:::

## The structure of an IPv4 address

An IPv4 address is **32 bits**. People split it into four 8-bit **octets** and write it in dotted decimal:

```text
192.168.10.77  =  11000000.10101000.00001010.01001101
```

An address has two parts: the **network part** (the high bits) and the **host part** (the low bits). Which bits belong to the network is set by the **mask**: in the mask the network bits are 1 and the host bits are 0. In short form a mask is written as a **prefix** — the number of ones:

| Prefix | Mask | Host bits | Number of hosts (2^h − 2) |
|---|---|---|---|
| /8 | 255.0.0.0 | 24 | 16,777,214 |
| /16 | 255.255.0.0 | 16 | 65,534 |
| /20 | 255.255.240.0 | 12 | 4,094 |
| /24 | 255.255.255.0 | 8 | 254 |
| /26 | 255.255.255.192 | 6 | 62 |
| /30 | 255.255.255.252 | 2 | 2 |

2 is subtracted from the number of hosts because the address whose host part is **all 0s** is the **network address**, and the one with **all 1s** is the **broadcast address**; they cannot be given to a device.

## Classful addressing

Originally (1981) the mask was not written separately — it followed from the first octet:

| Class | First bits | First octet | Default mask | Purpose |
|---|---|---|---|---|
| **A** | 0 | 0–127 | /8 | very large networks |
| **B** | 10 | 128–191 | /16 | medium networks |
| **C** | 110 | 192–223 | /24 | small networks |
| **D** | 1110 | 224–239 | — | multicast |
| **E** | 1111 | 240–255 | — | reserved for experiments |

The classful system wasted addresses badly: a C (254) was not enough for an organisation with 300 computers, while a B (65,534) stayed 99% empty. From 1993 **CIDR** (Classless Inter-Domain Routing) was introduced: a prefix can be any length (for example, /22 — 1022 hosts). Today classes survive only as a historical term and an exam question.

## Special addresses

| Range | Purpose |
|---|---|
| 0.0.0.0/8 | "this network"; 0.0.0.0 — no address yet (in a DHCP request) or "any address" |
| **10.0.0.0/8**, **172.16.0.0/12**, **192.168.0.0/16** | **private** (RFC 1918) — not routed on the Internet |
| 127.0.0.0/8 | **loopback** — the computer itself (127.0.0.1 = localhost) |
| 169.254.0.0/16 | **link-local** — Windows assigns it to itself when DHCP does not answer (APIPA) |
| 100.64.0.0/10 | providers' shared NAT (CGNAT) addresses |
| 224.0.0.0/4 | multicast (class D) |
| 240.0.0.0/4 | reserved (class E) |
| 255.255.255.255 | limited broadcast — everyone in the current segment |

**Private** addresses are reused over and over inside any organisation. On the way to the Internet the router uses **NAT** (Network Address Translation) to replace a private address with its own public one — this is how dozens of devices at home use the Internet through one public IP.

## Calculating by hand: the network and broadcast address

Example: `172.16.37.130/20`.

1. Mask /20 = `11111111.11111111.11110000.00000000` = **255.255.240.0**. Full octets (255) stay as they are, zero octets become zero; only the 3rd octet is the "boundary".
2. 3rd octet: 37 = `00100101`; AND `11110000` = `00100000` = 32. **Network**: 172.16.**32**.0.
3. There are 12 host bits: the low 4 bits of the 3rd octet and the whole 4th octet. We fill them with 1s: 32 + 15 = 47, 4th octet 255. **Broadcast**: 172.16.**47**.255.
4. The first host is 172.16.32.1, the last 172.16.47.254, the number of hosts 2^12 − 2 = 4094.

A quick method is the **block size**: for the boundary octet 256 − 240 = 16. Networks start at 0, 16, 32, 48, …; 37 falls in 32–47.

Doing this calculation in a program is the independent task of practical session A7 (KOT-A7).

## Subnetting

To divide one large network into several smaller ones, **bits are borrowed** from the host part. If *b* bits are borrowed:

- the number of subnets = 2^*b*;
- the new prefix = the old prefix + *b*;
- hosts in each subnet = 2^(32 − new prefix) − 2.

For example, splitting 192.168.1.0/24 into 4 departments: *b* = 2, prefix /26, 62 hosts each: .0, .64, .128, .192.

## Methods of assigning addresses

- **Static** — an administrator types it in: servers, printers, router interfaces.
- **Dynamic** — a **DHCP** server assigns it automatically (address, mask, gateway, DNS) — for user computers and phones (in detail in M9 and A11).
- Checking on Windows: `ipconfig` (address, mask, gateway), on Linux: `ip addr`.

## Worked example 1: an address's class and type

Each line holds an IPv4 address. Print it in binary with its class and type; for an invalid address (not four numbers 0–255) print `invalid`. Input is read until the end of the file.

```python
import sys


def tur(o):
    if o[0] == 127:
        return "loopback"
    if o[0] == 10 or (o[0] == 172 and 16 <= o[1] <= 31) or (o[0] == 192 and o[1] == 168):
        return "private"
    if o[0] == 169 and o[1] == 254:
        return "link-local"
    if o == [255, 255, 255, 255]:
        return "broadcast"
    if 224 <= o[0] <= 239:
        return "multicast"
    if o[0] >= 240 or o[0] == 0:
        return "reserved"
    return "public"


for satr in sys.stdin:
    satr = satr.strip()
    if not satr:
        continue
    qismlar = satr.split(".")
    if len(qismlar) != 4 or not all(q.isdigit() and len(q) <= 3 and int(q) <= 255 for q in qismlar):
        print(satr, "invalid")
        continue
    o = [int(q) for q in qismlar]
    ikkilik = ".".join(format(x, "08b") for x in o)
    sinf = "A" if o[0] < 128 else "B" if o[0] < 192 else "C" if o[0] < 224 else "D" if o[0] < 240 else "E"
    print(satr, ikkilik, sinf, tur(o))
```
```java
import java.util.Scanner;

public class Main {
    static String tur(int[] o) {
        if (o[0] == 127) return "loopback";
        if (o[0] == 10 || (o[0] == 172 && o[1] >= 16 && o[1] <= 31) || (o[0] == 192 && o[1] == 168)) return "private";
        if (o[0] == 169 && o[1] == 254) return "link-local";
        if (o[0] == 255 && o[1] == 255 && o[2] == 255 && o[3] == 255) return "broadcast";
        if (o[0] >= 224 && o[0] <= 239) return "multicast";
        if (o[0] >= 240 || o[0] == 0) return "reserved";
        return "public";
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] qismlar = satr.split("\\.", -1);
            boolean togri = qismlar.length == 4;
            for (int i = 0; togri && i < 4; i++) {
                togri = qismlar[i].matches("[0-9]{1,3}") && Integer.parseInt(qismlar[i]) <= 255;
            }
            if (!togri) {
                System.out.println(satr + " invalid");
                continue;
            }
            int[] o = new int[4];
            StringBuilder ikkilik = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                o[i] = Integer.parseInt(qismlar[i]);
                if (i > 0) ikkilik.append(".");
                ikkilik.append(String.format("%8s", Integer.toBinaryString(o[i])).replace(' ', '0'));
            }
            String sinf = o[0] < 128 ? "A" : o[0] < 192 ? "B" : o[0] < 224 ? "C" : o[0] < 240 ? "D" : "E";
            System.out.println(satr + " " + ikkilik + " " + sinf + " " + tur(o));
        }
    }
}
```
```cpp
#include <bitset>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

string tur(const vector<int>& o) {
    if (o[0] == 127) return "loopback";
    if (o[0] == 10 || (o[0] == 172 && o[1] >= 16 && o[1] <= 31) || (o[0] == 192 && o[1] == 168)) return "private";
    if (o[0] == 169 && o[1] == 254) return "link-local";
    if (o[0] == 255 && o[1] == 255 && o[2] == 255 && o[3] == 255) return "broadcast";
    if (o[0] >= 224 && o[0] <= 239) return "multicast";
    if (o[0] >= 240 || o[0] == 0) return "reserved";
    return "public";
}

int main() {
    string satr;
    while (cin >> satr) {
        vector<string> qismlar;
        stringstream ss(satr);
        string q;
        while (getline(ss, q, '.')) qismlar.push_back(q);
        bool togri = qismlar.size() == 4 && satr.back() != '.';
        for (size_t i = 0; togri && i < 4; i++) {
            togri = !qismlar[i].empty() && qismlar[i].size() <= 3 &&
                    qismlar[i].find_first_not_of("0123456789") == string::npos && stoi(qismlar[i]) <= 255;
        }
        if (!togri) {
            cout << satr << " invalid" << endl;
            continue;
        }
        vector<int> o;
        string ikkilik;
        for (int i = 0; i < 4; i++) {
            o.push_back(stoi(qismlar[i]));
            ikkilik += (i ? "." : "") + bitset<8>(o[i]).to_string();
        }
        string sinf = o[0] < 128 ? "A" : o[0] < 192 ? "B" : o[0] < 224 ? "C" : o[0] < 240 ? "D" : "E";
        cout << satr << " " << ikkilik << " " << sinf << " " << tur(o) << endl;
    }
    return 0;
}
```

Input:

```text
10.1.2.3
172.20.5.1
8.8.8.8
127.0.0.1
224.0.0.251
169.254.10.20
192.168.1.300
```

Output:

```text
10.1.2.3 00001010.00000001.00000010.00000011 A private
172.20.5.1 10101100.00010100.00000101.00000001 B private
8.8.8.8 00001000.00001000.00001000.00001000 A public
127.0.0.1 01111111.00000000.00000000.00000001 A loopback
224.0.0.251 11100000.00000000.00000000.11111011 D multicast
169.254.10.20 10101001.11111110.00001010.00010100 B link-local
192.168.1.300 invalid
```

Note: the class is determined by the first octet only — 10.1.2.3 is in class A, but in practice it may be used with /24 or another prefix rather than /8.

## Worked example 2: planning subnets

Each line holds the starting prefix, the required number of subnets *S* and the required number of hosts in each subnet *H*. Choose the fewest borrowed bits (2^*b* ≥ *S*), print the new prefix, the resulting number of subnets and of hosts in each, and say whether *H* hosts fit. If the new prefix exceeds 30, print `impossible`.

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    prefiks, s, h = map(int, satr.split())
    b = 0
    while 2 ** b < s:                    # the fewest borrowed bits
        b += 1
    yangi = prefiks + b
    if yangi > 30:
        print("impossible")
        continue
    xostlar = 2 ** (32 - yangi) - 2
    print("/" + str(yangi), 2 ** b, xostlar, "enough" if xostlar >= h else "not enough")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int prefiks = in.nextInt();
            long s = in.nextLong();
            long h = in.nextLong();
            int b = 0;
            while ((1L << b) < s) {                         // the fewest borrowed bits
                b++;
            }
            int yangi = prefiks + b;
            if (yangi > 30) {
                System.out.println("impossible");
                continue;
            }
            long xostlar = (1L << (32 - yangi)) - 2;
            System.out.println("/" + yangi + " " + (1L << b) + " " + xostlar + " " + (xostlar >= h ? "enough" : "not enough"));
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int prefiks;
    long long s, h;
    while (cin >> prefiks >> s >> h) {
        int b = 0;
        while ((1LL << b) < s) {                            // the fewest borrowed bits
            b++;
        }
        int yangi = prefiks + b;
        if (yangi > 30) {
            cout << "impossible" << endl;
            continue;
        }
        long long xostlar = (1LL << (32 - yangi)) - 2;
        cout << "/" << yangi << " " << (1LL << b) << " " << xostlar << " " << (xostlar >= h ? "enough" : "not enough") << endl;
    }
    return 0;
}
```

Input:

```text
24 4 50
24 6 30
24 10 20
16 100 300
24 100 1
```

Output:

```text
/26 4 62 enough
/27 8 30 enough
/28 16 14 not enough
/23 128 510 enough
impossible
```

The third line is the classic trade-off: more subnets need more borrowed bits, and each subnet gets fewer hosts. 10 departments of 20 computers do not fit into a /24 in equal pieces — this needs a larger address block or subnets of different sizes (VLSM, in A7).

:::warning
**Common mistakes**

- **Not subtracting 2 from the number of hosts.** A /24 has 254 hosts, not 256: the network and broadcast addresses are taken.
- **Misremembering the 172.16.0.0/12 range.** It runs from 172.16.0.0 to 172.31.255.255; 172.32.x.x is a public address.
- **Mixing up class and mask.** The class is a historical label from the first octet; the real mask is given by the prefix.
- **Reading a 169.254.x.x address as "the Internet works".** This address means no DHCP server was found.
- **Writing a mask wrongly.** In a mask the ones stand only on the left, in a row: 255.255.255.192 is correct, 255.255.255.191 is not a mask.
:::

:::tip
Look at your computer's IP address and mask with `ipconfig`. Determine the prefix, calculate the network and broadcast addresses by hand and say what type the address is (private or public). Then check your external address with a "what is my ip" service — why do they differ? (NAT.)
:::

## Summary

- IPv4 is 32 bits, four octets; the mask or prefix separates the network and host parts; the number of hosts is 2^h − 2.
- Classes: A (0–127), B (128–191), C (192–223), D — multicast, E — reserved; today CIDR is used.
- Private ranges: 10/8, 172.16/12, 192.168/16; loopback 127/8; link-local 169.254/16.
- Network address = IP AND mask, broadcast = all host bits 1; block size = 256 − the mask of the boundary octet.
- Subnetting: borrowing *b* bits → 2^*b* subnets, prefix + *b*.

## Check yourself

:::check
Q: Which prefix does the mask 255.255.255.224 correspond to, and how many hosts does it have?
A: /27; 2^5 − 2 = 30 hosts.
Q: Is 172.31.200.5 a private address?
A: Yes — it falls in the 172.16.0.0/12 range (172.16.0.0–172.31.255.255).
Q: Find the network address of 10.20.30.40/12.
A: Block 16, 2nd octet: 20 → 16; network 10.16.0.0.
Q: If a /24 is split into 8 subnets, what are the new prefix and the number of hosts?
A: 3 borrowed bits: /27, 30 hosts each.
Q: A computer got the address 169.254.33.7. What does this mean?
A: The DHCP server did not answer, and Windows gave itself a link-local (APIPA) address.
:::

:::link https://www.rfc-editor.org/rfc/rfc1918
RFC 1918: Address Allocation for Private Internets
The private ranges and the rules for using them.
:::
