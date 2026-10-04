---
title: The TCP/IP protocol stack. Principles of data exchange between networks. The notion of the IP protocols
summary: The layers and protocols of the TCP/IP stack; principles of internetworking — the datagram, connectionless and best-effort delivery, the end-to-end principle; the fields of the IPv4 header; TTL; calculating the checksum; fragmentation and MTU; the role of ICMP; the main differences of IPv6.
---

## Why it matters

The Internet is a union of thousands of independent networks (providers, universities, companies). They are built on different technologies: one on Ethernet, another on Wi-Fi, a third on a fibre backbone. What joins them all into one common "language" is the **IP protocol**. Every packet travels with an IP header, and every router makes its decision by looking only at that header. That is why knowing the IP header field by field is the alphabet of a network engineer.

:::note
**After this lesson you will be able to:**

- name the four layers of the TCP/IP stack and the main protocols of each;
- explain IP's datagram, connectionless and "best effort" principles;
- read the fields of an IPv4 header and state their meaning;
- calculate and check the checksum of an IP header;
- calculate how a packet larger than the MTU is split into fragments.
:::

## The TCP/IP stack

| Layer | Protocols | Data unit | Address |
|---|---|---|---|
| **Application** | HTTP, HTTPS, DNS, SMTP, FTP, SSH, DHCP | message | domain name, URL |
| **Transport** | TCP, UDP | segment / datagram | port |
| **Internet** | IP (v4, v6), ICMP, IGMP | packet | IP address |
| **Network access** | Ethernet, Wi-Fi, PPP; ARP | frame | MAC address |

The stack has an **"hourglass"** shape: many link technologies at the bottom, many applications at the top, and in the middle a single **IP**. This "everything over IP, IP over everything" principle gives the Internet its flexibility: when a new kind of cable appears, only the bottom layer changes; when a new application appears, only the top.

## Principles of internetworking

| Principle | Meaning |
|---|---|
| **Datagram** | every packet is independent — it carries its full address with it; different packets may take different routes |
| **Connectionless** | no connection is set up before sending, the network keeps no "conversation" state |
| **Best effort** | IP tries to deliver but **guarantees nothing**: a packet may be lost, duplicated or arrive out of order |
| **End-to-end** | reliability, ordering and retransmission are the job of the end systems (TCP); the network core stays simple and fast |
| **Hop-by-hop routing** | every router chooses only the **next hop**, it does not know the whole route |

## The IPv4 header

The minimal header is **20 bytes** (five 32-bit words):

| Byte | Field | Bits | Meaning |
|---|---|---|---|
| 0 | Version, IHL | 4 + 4 | version = 4; IHL — the header length in 4-byte words (usually 5 → 20 bytes) |
| 1 | DSCP/ECN (type of service) | 8 | priority and congestion marking |
| 2–3 | Total length | 16 | header + data, in bytes (at most 65,535) |
| 4–5 | Identification | 16 | for joining the fragments of one packet |
| 6–7 | Flags, fragment offset | 3 + 13 | DF — "don't fragment", MF — "more fragments"; the offset in 8-byte units |
| 8 | **TTL** | 8 | every router decreases it by 1; at 0 the packet is dropped |
| 9 | Protocol | 8 | the data inside: 1 — ICMP, 6 — TCP, 17 — UDP |
| 10–11 | Header checksum | 16 | for the **header** only |
| 12–15 | Source IP | 32 | the sender |
| 16–19 | Destination IP | 32 | the receiver |

**TTL** (Time To Live) stops packets from circling forever in routing loops. The `tracert` (Windows) / `traceroute` (Linux) command sends packets with TTL = 1, 2, 3, … and finds the route from the "TTL exceeded" ICMP messages coming back from each router.

## The checksum

The IP header checksum is computed with **ones' complement** addition:

1. The checksum field is taken as 0.
2. The header is split into 16-bit words and all of them are added.
3. The part above 16 bits (the carry) is added back to the low 16 bits — until the result fits in 16 bits.
4. The result is inverted (subtracted from 0xFFFF).

The receiver adds the whole header (including the checksum) the same way: if the result is **0xFFFF**, the header is intact. Since every router decreases the TTL, it also recomputes the checksum.

## Fragmentation

Every link has an **MTU** (Maximum Transmission Unit): Ethernet — 1500 bytes. If a packet is larger than the link MTU, the router splits it into **fragments** (unless the DF flag is set):

- every fragment has its own IP header, with the same identification;
- the data part is a multiple of 8 (except the last), because the offset is written in 8-byte units;
- MF = 1 in every fragment except the last;
- fragments are reassembled only **at the receiver**.

A packet larger than the MTU with DF set is dropped, and an ICMP "Fragmentation needed" message goes back to the sender — **Path MTU Discovery** is based on this. In IPv6, routers do not fragment at all.

## Briefly about ICMP and IPv6

**ICMP** (Internet Control Message Protocol) is IP's "service messages": `Echo Request/Reply` (`ping`), `Destination Unreachable`, `Time Exceeded` (TTL ran out), `Fragmentation Needed`. ICMP itself travels inside an IP packet (protocol = 1).

**IPv6**: 128-bit addresses (`2001:db8::1`), a fixed 40-byte simplified header (no checksum, fragmentation only at the sender), Hop Limit instead of TTL, multicast instead of broadcast. Since IPv4 addresses ran out, IPv6 has been spreading fast.

## Worked example 1: analysing an IPv4 header

The input holds the 20 bytes of an IPv4 header in hexadecimal. Print the main fields and check the checksum; if it is wrong, print the correct value as well.

```python
import sys

b = [int(t, 16) for t in sys.stdin.read().split()]


def yigindi(baytlar):
    s = 0
    for i in range(0, len(baytlar), 2):
        s += baytlar[i] * 256 + baytlar[i + 1]
    while s > 0xFFFF:
        s = (s & 0xFFFF) + (s >> 16)          # add the carry back
    return s


nomlar = {1: "ICMP", 6: "TCP", 17: "UDP"}
print("version:", b[0] >> 4, "header:", (b[0] & 15) * 4, "bytes")
print("total length:", b[2] * 256 + b[3])
print("TTL:", b[8])
print("protocol: " + str(b[9]) + " (" + nomlar.get(b[9], "?") + ")")
print("source:", ".".join(str(x) for x in b[12:16]))
print("destination:", ".".join(str(x) for x in b[16:20]))
yozilgan = b[10] * 256 + b[11]
if yigindi(b[:20]) == 0xFFFF:
    print("checksum: 0x" + format(yozilgan, "04X") + " valid")
else:
    toza = b[:10] + [0, 0] + b[12:20]
    togrisi = 0xFFFF - yigindi(toza)
    print("checksum: 0x" + format(yozilgan, "04X") + " wrong, correct 0x" + format(togrisi, "04X"))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int yigindi(int[] baytlar) {
        int s = 0;
        for (int i = 0; i < baytlar.length; i += 2) {
            s += baytlar[i] * 256 + baytlar[i + 1];
        }
        while (s > 0xFFFF) {
            s = (s & 0xFFFF) + (s >> 16);                  // add the carry back
        }
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> oqilgan = new ArrayList<>();
        while (in.hasNext()) oqilgan.add(Integer.parseInt(in.next(), 16));
        int[] b = new int[20];
        for (int i = 0; i < 20; i++) b[i] = oqilgan.get(i);

        String nom = b[9] == 1 ? "ICMP" : b[9] == 6 ? "TCP" : b[9] == 17 ? "UDP" : "?";
        System.out.println("version: " + (b[0] >> 4) + " header: " + (b[0] & 15) * 4 + " bytes");
        System.out.println("total length: " + (b[2] * 256 + b[3]));
        System.out.println("TTL: " + b[8]);
        System.out.println("protocol: " + b[9] + " (" + nom + ")");
        System.out.println("source: " + b[12] + "." + b[13] + "." + b[14] + "." + b[15]);
        System.out.println("destination: " + b[16] + "." + b[17] + "." + b[18] + "." + b[19]);
        int yozilgan = b[10] * 256 + b[11];
        if (yigindi(b) == 0xFFFF) {
            System.out.println("checksum: 0x" + String.format("%04X", yozilgan) + " valid");
        } else {
            int[] toza = b.clone();
            toza[10] = 0;
            toza[11] = 0;
            int togrisi = 0xFFFF - yigindi(toza);
            System.out.println("checksum: 0x" + String.format("%04X", yozilgan) + " wrong, correct 0x" + String.format("%04X", togrisi));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int yigindi(const vector<int>& baytlar) {
    int s = 0;
    for (size_t i = 0; i < baytlar.size(); i += 2) {
        s += baytlar[i] * 256 + baytlar[i + 1];
    }
    while (s > 0xFFFF) {
        s = (s & 0xFFFF) + (s >> 16);                      // add the carry back
    }
    return s;
}

string hex4(int x) {
    char buf[8];
    snprintf(buf, sizeof buf, "%04X", x);
    return buf;
}

int main() {
    vector<int> b;
    string t;
    while (cin >> t) b.push_back(stoi(t, nullptr, 16));
    b.resize(20);

    string nom = b[9] == 1 ? "ICMP" : b[9] == 6 ? "TCP" : b[9] == 17 ? "UDP" : "?";
    cout << "version: " << (b[0] >> 4) << " header: " << (b[0] & 15) * 4 << " bytes" << endl;
    cout << "total length: " << b[2] * 256 + b[3] << endl;
    cout << "TTL: " << b[8] << endl;
    cout << "protocol: " << b[9] << " (" << nom << ")" << endl;
    cout << "source: " << b[12] << "." << b[13] << "." << b[14] << "." << b[15] << endl;
    cout << "destination: " << b[16] << "." << b[17] << "." << b[18] << "." << b[19] << endl;
    int yozilgan = b[10] * 256 + b[11];
    if (yigindi(b) == 0xFFFF) {
        cout << "checksum: 0x" << hex4(yozilgan) << " valid" << endl;
    } else {
        vector<int> toza = b;
        toza[10] = 0;
        toza[11] = 0;
        int togrisi = 0xFFFF - yigindi(toza);
        cout << "checksum: 0x" << hex4(yozilgan) << " wrong, correct 0x" << hex4(togrisi) << endl;
    }
    return 0;
}
```

Input:

```text
45 00 00 3c 1c 46 40 00 40 06 b1 e6 ac 10 0a 63 ac 10 0a 0c
```

Output:

```text
version: 4 header: 20 bytes
total length: 60
TTL: 64
protocol: 6 (TCP)
source: 172.16.10.99
destination: 172.16.10.12
checksum: 0xB1E6 valid
```

Now we change the TTL from 64 to 63 (byte 9 is `3f`) without updating the checksum — as if a router had made a mistake:

```text
45 00 00 3c 1c 46 40 00 3f 06 b1 e6 ac 10 0a 63 ac 10 0a 0c
```

The last line of the output:

```text
checksum: 0xB1E6 wrong, correct 0xB2E6
```

The TTL dropped by 1 — the checksum grew by 0x0100 (the TTL sits in the high byte of a 16-bit word). Routers use this property to update the checksum without recomputing it from scratch.

## Worked example 2: fragmentation

One line holds the length *L* (bytes) of an IP packet's data part and the link MTU. The header is 20 bytes. For each fragment print its number, total length (with the header), offset (in 8-byte units) and the MF flag.

```python
l, mtu = map(int, input().split())
qism = (mtu - 20) // 8 * 8               # fragment data is a multiple of 8
joy = 0
raqam = 0
while joy < l:
    raqam += 1
    hajm = min(qism, l - joy)
    mf = 1 if joy + hajm < l else 0       # MF = 1 on all but the last
    print(raqam, "len=" + str(hajm + 20), "offset=" + str(joy // 8), "MF=" + str(mf))
    joy += hajm
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int l = in.nextInt();
        int mtu = in.nextInt();
        int qism = (mtu - 20) / 8 * 8;                       // fragment data is a multiple of 8
        int joy = 0;
        int raqam = 0;
        while (joy < l) {
            raqam++;
            int hajm = Math.min(qism, l - joy);
            int mf = joy + hajm < l ? 1 : 0;                  // MF = 1 on all but the last
            System.out.println(raqam + " len=" + (hajm + 20) + " offset=" + joy / 8 + " MF=" + mf);
            joy += hajm;
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

int main() {
    int l, mtu;
    cin >> l >> mtu;
    int qism = (mtu - 20) / 8 * 8;                           // fragment data is a multiple of 8
    int joy = 0;
    int raqam = 0;
    while (joy < l) {
        raqam++;
        int hajm = min(qism, l - joy);
        int mf = joy + hajm < l ? 1 : 0;                      // MF = 1 on all but the last
        cout << raqam << " len=" << hajm + 20 << " offset=" << joy / 8 << " MF=" << mf << endl;
        joy += hajm;
    }
    return 0;
}
```

Input:

```text
4000 1500
```

Output:

```text
1 len=1500 offset=0 MF=1
2 len=1500 offset=185 MF=1
3 len=1060 offset=370 MF=0
```

The 4000 bytes of data were split into 1480 + 1480 + 1040. The offset of the second fragment is 185 — that is 185 × 8 = 1480 bytes. With MTU = 576 the data in a fragment is 552 bytes, not 556 (a multiple of 8).

:::warning
**Common mistakes**

- **Reading IHL in bytes.** IHL = 5 means 5 four-byte words, that is 20 bytes.
- **Thinking the IP checksum also protects the data.** It checks only the header; the data has the TCP/UDP checksum and the link layer's CRC.
- **Writing the fragment offset in bytes.** The offset is in 8-byte units: 1480 bytes → 185.
- **Believing routers reassemble fragments.** Fragments are joined only at the final receiver.
- **Thinking IP guarantees delivery.** IP is "best effort"; reliability is TCP's job.
:::

:::tip
Run `tracert 8.8.8.8` (or `traceroute`) and look at the "Time-to-live exceeded" replies in Wireshark with the `icmp` filter. Check what the TTL is in the inner IP header of each reply.
:::

## Summary

- TCP/IP has 4 layers: application, transport, internet, network access; a single IP sits in the middle (the "hourglass").
- IP is datagram-based, connectionless, best effort; reliability lives in the end systems.
- The IPv4 header is 20 bytes: version, IHL, length, identification, flags and offset, TTL, protocol, checksum, source and destination.
- The checksum is the inverted ones' complement sum of the 16-bit words; a correct header sums to 0xFFFF.
- A packet larger than the MTU is split into pieces that are multiples of 8; it is reassembled at the receiver using MF and the offset.

## Check yourself

:::check
Q: Why is IP called a "best effort" protocol?
A: It tries to deliver a packet but does not guarantee delivery, ordering or the absence of duplicates.
Q: What is the length of an IPv4 header whose first byte is `46`?
A: IHL = 6 → 6 × 4 = 24 bytes (with 4 bytes of options).
Q: What is the TTL field for?
A: To stop a packet circling forever in a routing loop; every router decreases it by 1, and at 0 the packet is dropped.
Q: Into how many fragments are 3000 bytes of data split on a link with MTU = 1500?
A: 3: 1480 + 1480 + 40 bytes of data.
Q: If the protocol field is 17, what is inside the packet?
A: A UDP datagram.
:::

:::link https://www.rfc-editor.org/rfc/rfc791
RFC 791: Internet Protocol
The original specification of the IPv4 protocol.
:::
