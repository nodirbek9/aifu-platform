---
title: Ethernet technologies. Addressing network nodes. The notion of a MAC address
summary: The history of Ethernet and the IEEE 802.3 standards (from 10BASE-T to 400G); the structure of an Ethernet frame — preamble, addresses, EtherType, data, FCS; the minimum and maximum frame; the CSMA/CD method and collisions; the MAC address — structure, OUI, unicast, multicast and broadcast, global and local addresses; levels of node addressing; channel efficiency.
---

## Why it matters

Almost 100% of today's wired local networks are **Ethernet**. The cable in a university computer lab, the yellow ports of a home router, the 100-gigabit servers in a data centre — all of these are standards from one family. Knowing how an Ethernet frame is built, what a MAC address means and why the smallest frame is 64 bytes is the basis for analysing a network and finding faults.

:::note
**After this lesson you will be able to:**

- read Ethernet standards from their names (for example, 1000BASE-T);
- name the fields of an Ethernet frame and their lengths;
- explain the CSMA/CD method and why a minimum frame length is needed;
- analyse the structure of a MAC address and determine its type (unicast, multicast, broadcast) and scope (global, local);
- calculate the useful throughput of a channel for different frame sizes.
:::

## The history and standards of Ethernet

Ethernet was created in 1973 at Xerox PARC by Robert Metcalfe and adopted in 1983 as the **IEEE 802.3** standard. A standard's name has three parts: **speed** – **BASE** (baseband — a single channel) – **medium**.

| Standard | Speed | Medium | Distance | Name |
|---|---|---|---|---|
| 10BASE5 / 10BASE2 | 10 Mbit/s | thick / thin coaxial | 500 / 185 m | early Ethernet (bus) |
| 10BASE-T | 10 Mbit/s | twisted pair Cat3 | 100 m | Ethernet |
| 100BASE-TX | 100 Mbit/s | Cat5 | 100 m | Fast Ethernet |
| 100BASE-FX | 100 Mbit/s | multi-mode fibre | 2 km | Fast Ethernet |
| 1000BASE-T | 1 Gbit/s | Cat5e (4 pairs) | 100 m | Gigabit Ethernet |
| 1000BASE-SX / LX | 1 Gbit/s | multi- / single-mode fibre | 550 m / 5 km | Gigabit Ethernet |
| 10GBASE-T | 10 Gbit/s | Cat6a | 100 m | 10 Gigabit Ethernet |
| 40G / 100G / 400G | 40–400 Gbit/s | mostly fibre | metres to km | data centres |

Speed has grown 40,000 times, but **the frame format has barely changed** — this compatibility is the secret of Ethernet's success.

## The Ethernet frame

| Field | Length (bytes) | Purpose |
|---|---|---|
| Preamble | 7 | the pattern `10101010` — synchronises the receiver's clock |
| SFD (start frame delimiter) | 1 | `10101011` — the frame starts here |
| Destination MAC | 6 | to whom |
| Source MAC | 6 | from whom |
| EtherType / Length | 2 | the protocol inside: `0x0800` — IPv4, `0x0806` — ARP, `0x86DD` — IPv6 |
| Data (payload) | 46–1500 | the upper-layer packet; padded if shorter than 46 |
| FCS | 4 | the CRC-32 checksum |

The preamble and SFD are usually not counted in the "frame length". So the **minimum frame** is 6 + 6 + 2 + 46 + 4 = **64 bytes**, and the **maximum** is 1518 bytes (1522 with a VLAN tag). Between frames there is a pause of at least 12 bytes' time — the **IFG** (inter-frame gap). Data centres also use **jumbo** frames (up to 9000 bytes).

## CSMA/CD

Classic Ethernet worked over a shared medium (a bus or a hub) and controlled access with **CSMA/CD** (Carrier Sense Multiple Access with Collision Detection):

1. **Listen** (carrier sense): if the channel is busy — wait.
2. The channel is free — start transmitting and watch the channel while transmitting.
3. **If a collision is detected** — stop transmitting and send a 32-bit **jam** signal.
4. **Binary exponential backoff**: after the *n*-th collision, wait a random number of slot times from 0 to 2^min(*n*, 10) − 1; after 16 attempts the frame is dropped.

**Why is the minimum frame 64 bytes?** The sender must notice a collision **before** it finishes sending the frame. The time for a signal to reach the farthest point of the segment and for the collision news to come back — the **slot time** — was set at 512 bits = 64 bytes at 10 Mbit/s. If a frame were shorter, a collision could arrive after the sender had decided "the job is done".

In modern switched **full-duplex** Ethernet there are no collisions at all — every port is a separate channel and CSMA/CD is switched off. But for compatibility the minimum frame has stayed at 64 bytes.

## The MAC address

A **MAC address** is a 48-bit (6-byte) physical address, usually written in hexadecimal. The written forms differ, but the meaning is the same:

```text
3C:52:82:1A:9F:07      Linux, macOS
3C-52-82-1A-9F-07      Windows
3c52.821a.9f07         Cisco
```

| Part | Meaning |
|---|---|
| The first 3 bytes — the **OUI** | the manufacturer identifier (assigned by the IEEE): for example, `00:1A:2B` |
| The last 3 bytes | assigned by the manufacturer, unique for each adapter |
| The least significant bit of byte 1 (I/G) | 0 — **unicast** (one device), 1 — **multicast** (a group) |
| The second bit of byte 1 (U/L) | 0 — **global** (set at the factory), 1 — **local** (set by software or an administrator) |

The **broadcast** address is `FF:FF:FF:FF:FF:FF` (all bits 1): the frame goes to every device in the segment. For example, `01:00:5E:...` is IPv4 multicast and `33:33:...` is IPv6 multicast. The "random MAC" feature of virtual machines and phones uses **local** addresses (for example, Docker containers use `02:42:...`).

## Levels of node addressing

| Level | Address | Example | Who assigns it |
|---|---|---|---|
| Data link (2) | MAC | `3C:52:82:1A:9F:07` | the manufacturer |
| Network (3) | IP | `192.168.1.10` | an administrator or DHCP |
| Transport (4) | port | `443` | the operating system or the program |
| Application (7) | domain name | `aifu.uz` | a DNS registrar |

A MAC address has meaning only within **one segment**: when a packet crosses a router it is put into a new frame and the MAC addresses change, while the IP addresses (without NAT) stay the same. Finding the MAC address for an IP address is the job of the **ARP** protocol (in detail in M9).

## Worked example 1: analysing a MAC address

Each line holds a MAC address in any written form. Bring it to the form `XX:XX:XX:XX:XX:XX` and print the OUI, the type (unicast/multicast) and the scope (global/local); for the broadcast address print only `broadcast`. For an invalid address print `invalid`. Input is read until the end of the file.

```python
import sys

for satr in sys.stdin:
    satr = satr.strip()
    if not satr:
        continue
    raqamlar = satr.replace(":", "").replace("-", "").replace(".", "").upper()
    if len(raqamlar) != 12 or any(c not in "0123456789ABCDEF" for c in raqamlar):
        print(satr, "invalid")
        continue
    baytlar = [raqamlar[i:i + 2] for i in range(0, 12, 2)]
    mac = ":".join(baytlar)
    if raqamlar == "F" * 12:
        print(mac, "broadcast")
        continue
    birinchi = int(baytlar[0], 16)
    tur = "multicast" if birinchi & 1 else "unicast"         # the I/G bit
    doira = "local" if birinchi & 2 else "global"             # the U/L bit
    print(mac, "OUI", ":".join(baytlar[:3]), tur, doira)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String raqamlar = satr.replace(":", "").replace("-", "").replace(".", "").toUpperCase();
            if (raqamlar.length() != 12 || !raqamlar.matches("[0-9A-F]+")) {
                System.out.println(satr + " invalid");
                continue;
            }
            String[] baytlar = new String[6];
            for (int i = 0; i < 6; i++) baytlar[i] = raqamlar.substring(2 * i, 2 * i + 2);
            String mac = String.join(":", baytlar);
            if (raqamlar.equals("FFFFFFFFFFFF")) {
                System.out.println(mac + " broadcast");
                continue;
            }
            int birinchi = Integer.parseInt(baytlar[0], 16);
            String tur = (birinchi & 1) != 0 ? "multicast" : "unicast";    // the I/G bit
            String doira = (birinchi & 2) != 0 ? "local" : "global";        // the U/L bit
            System.out.println(mac + " OUI " + baytlar[0] + ":" + baytlar[1] + ":" + baytlar[2] + " " + tur + " " + doira);
        }
    }
}
```
```cpp
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    while (cin >> satr) {
        string raqamlar;
        bool togri = true;
        for (char c : satr) {
            if (c == ':' || c == '-' || c == '.') continue;
            c = toupper(c);
            if (!isxdigit(c)) togri = false;
            raqamlar += c;
        }
        if (raqamlar.size() != 12 || !togri) {
            cout << satr << " invalid" << endl;
            continue;
        }
        string mac;
        for (int i = 0; i < 12; i += 2) mac += (i ? ":" : "") + raqamlar.substr(i, 2);
        if (raqamlar == "FFFFFFFFFFFF") {
            cout << mac << " broadcast" << endl;
            continue;
        }
        int birinchi = stoi(raqamlar.substr(0, 2), nullptr, 16);
        string tur = (birinchi & 1) ? "multicast" : "unicast";      // the I/G bit
        string doira = (birinchi & 2) ? "local" : "global";          // the U/L bit
        cout << mac << " OUI " << mac.substr(0, 8) << " " << tur << " " << doira << endl;
    }
    return 0;
}
```

Input:

```text
3c:52:82:1a:9f:07
01-00-5E-00-00-FB
ffff.ffff.ffff
02:42:ac:11:00:02
12:34:56
```

Output:

```text
3C:52:82:1A:9F:07 OUI 3C:52:82 unicast global
01:00:5E:00:00:FB OUI 01:00:5E multicast global
FF:FF:FF:FF:FF:FF broadcast
02:42:AC:11:00:02 OUI 02:42:AC unicast local
12:34:56 invalid
```

The second address is the multicast MAC address of mDNS (`224.0.0.251`), the fourth is a Docker container address (local).

## Worked example 2: frame size and useful throughput

Each line holds the channel speed *V* (Mbit/s) and the amount of data in a frame *L* (bytes). Print the frame length (with padding up to 46 bytes), the full size on the cable (adding the 8-byte preamble and the 12-byte IFG), the maximum number of frames per second (the integer part) and the useful throughput (Mbit/s, two decimal places).

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    v, l = map(int, satr.split())
    kadr = 6 + 6 + 2 + max(l, 46) + 4         # data shorter than 46 is padded
    kabelda = kadr + 8 + 12                   # preamble+SFD and the gap between frames
    sekundiga = v * 1_000_000 / (kabelda * 8)
    foydali = sekundiga * l * 8 / 1_000_000
    print(kadr, kabelda, int(sekundiga), f"{foydali:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long v = in.nextLong();
            long l = in.nextLong();
            long kadr = 6 + 6 + 2 + Math.max(l, 46) + 4;     // data shorter than 46 is padded
            long kabelda = kadr + 8 + 12;                    // preamble+SFD and the gap between frames
            double sekundiga = v * 1_000_000.0 / (kabelda * 8);
            double foydali = sekundiga * l * 8 / 1_000_000;
            System.out.println(kadr + " " + kabelda + " " + (long) sekundiga + " " + String.format("%.2f", foydali));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    long long v, l;
    while (cin >> v >> l) {
        long long kadr = 6 + 6 + 2 + max(l, 46LL) + 4;      // data shorter than 46 is padded
        long long kabelda = kadr + 8 + 12;                  // preamble+SFD and the gap between frames
        double sekundiga = v * 1000000.0 / (kabelda * 8);
        double foydali = sekundiga * l * 8 / 1000000;
        printf("%lld %lld %lld %.2f\n", kadr, kabelda, (long long) sekundiga, foydali);
    }
    return 0;
}
```

Input:

```text
100 46
100 1500
1000 10
10000 9000
```

Output:

```text
64 84 148809 54.76
1518 1538 8127 97.53
64 84 1488095 119.05
9018 9038 138304 9957.96
```

148,809 frames/s is the famous "maximum packet rate" of a 100 Mbit/s port (1,488,095 at Gigabit). With small frames the port is full of frames, yet the useful throughput falls to about half; with large frames, and jumbo frames especially, efficiency reaches 97–99%.

:::warning
**Common mistakes**

- **Adding the preamble to the frame length.** The 64- and 1518-byte limits are counted without the preamble and SFD; they are added only when computing time on the cable.
- **Thinking MAC addresses are routed across the world.** A MAC works only within one segment; after a router the frame is rebuilt with new MAC addresses.
- **Looking for the multicast bit in the wrong byte.** The I/G and U/L bits are the two least significant bits of the **first** byte.
- **Believing CSMA/CD still runs in a modern switched network.** In full-duplex mode there are no collisions and CSMA/CD is switched off.
:::

:::tip
Find your adapter's MAC address with `ipconfig /all` (or `ip link`) and look up its OUI in any online OUI lookup service — the manufacturer will show up. If "random MAC" is enabled in your phone's Wi-Fi settings, check that the second bit of its first byte is 1 (a local address).
:::

## Summary

- Ethernet is IEEE 802.3; naming: speed–BASE–medium (for example, 1000BASE-T); the frame format has barely changed from 10 Mbit/s to 400 Gbit/s.
- Frame: preamble 7 + SFD 1, destination and source MAC 6 + 6, EtherType 2, data 46–1500, FCS 4; the minimum frame is 64 bytes, the maximum 1518.
- CSMA/CD: listen, transmit, detect a collision, jam and binary exponential backoff; the minimum frame is needed to notice a collision in time.
- A MAC is 48 bits: OUI + device number; the I/G and U/L bits in the first byte set the type and scope; `FF:FF:FF:FF:FF:FF` is broadcast.

## Check yourself

:::check
Q: What does the name 1000BASE-T mean?
A: A speed of 1000 Mbit/s, baseband transmission, a twisted-pair medium.
Q: What happens if the data in an Ethernet frame is 20 bytes?
A: The data part is padded to 46 bytes, so the frame is not shorter than 64 bytes.
Q: Which protocol does the EtherType value `0x0806` denote?
A: ARP.
Q: Is the address `01:00:5E:7F:00:01` unicast or multicast?
A: Multicast — the least significant bit of the first byte `01` is 1.
Q: Why are there no collisions in modern switched networks?
A: Every port is a separate collision domain and works in full duplex — sending and receiving go over separate pairs.
:::

:::link https://en.wikipedia.org/wiki/IEEE_802.3
IEEE 802.3
The list of Ethernet standards, the frame format and history.
:::
