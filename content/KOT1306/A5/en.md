---
title: Local network technologies Ethernet, Fast Ethernet. Analysing packets with network sniffers
summary: Practical parameters of Ethernet and Fast Ethernet (100BASE-TX, 4B/5B + MLT-3, autonegotiation); network sniffers — Wireshark, tcpdump; promiscuous mode and port mirroring (SPAN) on a switch; capture and display filters; analysing a frame by hand from its hex dump, the VLAN tag; traffic statistics; the legal limits of using a sniffer.
---

## Why it matters

"The Internet is slow", "the printer cannot be found", "the site will not open" — the cause of such problems must be **seen**, not guessed. For this a network engineer uses a **sniffer** (a packet analyser): it captures every frame crossing the cable and unfolds it layer by layer. In this session we learn to work with Wireshark and, to understand what happens inside a sniffer, "unfold" a frame by hand with a program.

:::note
**After this session you will be able to:**

- state the main technical parameters of Fast Ethernet (100BASE-TX);
- capture traffic in Wireshark and pick out the packets you need with display filters;
- analyse an Ethernet frame by hand from its hex dump and find the addresses, the VLAN tag and the EtherType;
- build protocol statistics from captured traffic;
- know the legal and ethical limits of using a sniffer.
:::

## Ethernet and Fast Ethernet in practice

| Parameter | 10BASE-T | 100BASE-TX (Fast Ethernet) | 1000BASE-T |
|---|---|---|---|
| Cable | Cat3 | Cat5 | Cat5e |
| Pairs used | 2 (1-2, 3-6) | 2 (1-2, 3-6) | 4 (all, in both directions) |
| Encoding | Manchester | 4B/5B + MLT-3 | PAM-5 |
| Line rate | 10 MHz | 125 Mbaud | 125 Mbaud × 4 pairs |
| Segment | 100 m | 100 m | 100 m |

**Autonegotiation**: when a link comes up, the adapter and the switch port advertise their capabilities (10/100/1000, half/full) with special pulses, and the best mode both support is chosen. The `show interfaces` output of a switch port shows the speed and duplex.

## How sniffers work

Normally a network adapter accepts only frames sent **to itself** (and to broadcast and subscribed multicast addresses). A sniffer puts the adapter into **promiscuous mode** — it then accepts **all** frames in the segment.

But in a switched network the switch does not send other computers' unicast frames to your port. Therefore:

- **Your own traffic** — capture it on your own computer;
- **Another device's traffic** — set up **port mirroring** (SPAN) on the switch: a copy of the chosen port's traffic is sent to the port the sniffer is connected to;
- Or put a **TAP** (a special splitter) between the two devices.

| Tool | Feature |
|---|---|
| **Wireshark** | graphical interface, over 3000 protocol dissectors, statistics |
| **tcpdump** | the Linux/macOS command line, on servers; writes to a `.pcap` file |
| **tshark** | the command-line version of Wireshark |

## Capture and display filters

A **capture filter** (BPF syntax) records only matching packets; a **display filter** chooses which of the recorded packets are shown.

```text
Capture filters (BPF):              Display filters (Wireshark):
  host 192.168.1.10                   ip.addr == 192.168.1.10
  port 53                             dns
  arp                                 arp
  tcp and port 443                    tcp.port == 443
  not port 22                         eth.dst == ff:ff:ff:ff:ff:ff
                                      tcp.flags.syn == 1 && tcp.flags.ack == 0
```

## Practical work: a first capture with Wireshark

1. Start Wireshark, choose the active interface (Wi-Fi or Ethernet) and start capturing.
2. At the command line run `ipconfig /flushdns` and `ping aifu.uz`, and open any site in the browser. Stop the capture.
3. Filter `dns` — find the domain name query and reply. Write down the IP address in the reply.
4. Filter `icmp` — the `ping` packets. Select one and expand the **Ethernet II** part in the lower pane: destination and source MAC, Type = IPv4 (0x0800).
5. Filter `arp` — to which MAC address was the "Who has 192.168.1.1? Tell 192.168.1.10" request sent? (broadcast).
6. In **Statistics → Protocol Hierarchy** and **Statistics → Conversations** find which protocol and which address produced the most traffic.
7. In the bottom pane (the bytes) highlight the first 14 bytes of the frame — this is the Ethernet header. Compare it with the result of the first example below.

## Worked example 1: analysing a frame by hand

The input holds the bytes of a captured frame in hexadecimal (separated by spaces and line breaks; the FCS is not captured). Print the destination and source MAC addresses, the VLAN number if there is a VLAN tag (`0x8100`), the EtherType (with the protocol name) and the length of the data part.

```python
import sys

baytlar = [int(t, 16) for t in sys.stdin.read().split()]


def mac(b):
    return ":".join(format(x, "02X") for x in b)


print("destination:", mac(baytlar[0:6]))
print("source:", mac(baytlar[6:12]))
joy = 12
tur = baytlar[joy] * 256 + baytlar[joy + 1]
if tur == 0x8100:                              # 802.1Q VLAN tag: 4 bytes
    vlan = (baytlar[joy + 2] * 256 + baytlar[joy + 3]) & 0x0FFF
    print("VLAN:", vlan)
    joy += 4
    tur = baytlar[joy] * 256 + baytlar[joy + 1]
nomlar = {0x0800: "IPv4", 0x0806: "ARP", 0x86DD: "IPv6"}
print("EtherType: 0x" + format(tur, "04X") + " (" + nomlar.get(tur, "unknown") + ")")
print("payload:", len(baytlar) - joy - 2, "bytes")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String mac(List<Integer> b, int boshi) {
        StringBuilder s = new StringBuilder();
        for (int i = boshi; i < boshi + 6; i++) {
            if (i > boshi) s.append(":");
            s.append(String.format("%02X", b.get(i)));
        }
        return s.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> baytlar = new ArrayList<>();
        while (in.hasNext()) baytlar.add(Integer.parseInt(in.next(), 16));

        System.out.println("destination: " + mac(baytlar, 0));
        System.out.println("source: " + mac(baytlar, 6));
        int joy = 12;
        int tur = baytlar.get(joy) * 256 + baytlar.get(joy + 1);
        if (tur == 0x8100) {                                   // 802.1Q VLAN tag: 4 bytes
            int vlan = (baytlar.get(joy + 2) * 256 + baytlar.get(joy + 3)) & 0x0FFF;
            System.out.println("VLAN: " + vlan);
            joy += 4;
            tur = baytlar.get(joy) * 256 + baytlar.get(joy + 1);
        }
        String nom = tur == 0x0800 ? "IPv4" : tur == 0x0806 ? "ARP" : tur == 0x86DD ? "IPv6" : "unknown";
        System.out.println("EtherType: 0x" + String.format("%04X", tur) + " (" + nom + ")");
        System.out.println("payload: " + (baytlar.size() - joy - 2) + " bytes");
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

string mac(const vector<int>& b, int boshi) {
    string s;
    char buf[4];
    for (int i = boshi; i < boshi + 6; i++) {
        snprintf(buf, sizeof buf, "%02X", b[i]);
        s += (i > boshi ? ":" : "") + string(buf);
    }
    return s;
}

int main() {
    vector<int> baytlar;
    string t;
    while (cin >> t) baytlar.push_back(stoi(t, nullptr, 16));

    cout << "destination: " << mac(baytlar, 0) << endl;
    cout << "source: " << mac(baytlar, 6) << endl;
    int joy = 12;
    int tur = baytlar[joy] * 256 + baytlar[joy + 1];
    if (tur == 0x8100) {                                       // 802.1Q VLAN tag: 4 bytes
        int vlan = (baytlar[joy + 2] * 256 + baytlar[joy + 3]) & 0x0FFF;
        cout << "VLAN: " << vlan << endl;
        joy += 4;
        tur = baytlar[joy] * 256 + baytlar[joy + 1];
    }
    string nom = tur == 0x0800 ? "IPv4" : tur == 0x0806 ? "ARP" : tur == 0x86DD ? "IPv6" : "unknown";
    char hex[8];
    snprintf(hex, sizeof hex, "%04X", tur);
    cout << "EtherType: 0x" << hex << " (" << nom << ")" << endl;
    cout << "payload: " << baytlar.size() - joy - 2 << " bytes" << endl;
    return 0;
}
```

Input (an ARP request):

```text
ff ff ff ff ff ff 3c 52 82 1a 9f 07 08 06
00 01 08 00 06 04 00 01 3c 52 82 1a 9f 07
c0 a8 01 0a 00 00 00 00 00 00 c0 a8 01 01
```

Output:

```text
destination: FF:FF:FF:FF:FF:FF
source: 3C:52:82:1A:9F:07
EtherType: 0x0806 (ARP)
payload: 28 bytes
```

Input (an IPv4 frame belonging to VLAN 10):

```text
00 1a 2b 3c 4d 5e 3c 52 82 1a 9f 07 81 00 00 0a 08 00
45 00 00 14 00 01 00 00 40 06 00 00 c0 a8 01 0a 08 08 08 08
```

Output:

```text
destination: 00:1A:2B:3C:4D:5E
source: 3C:52:82:1A:9F:07
VLAN: 10
EtherType: 0x0800 (IPv4)
payload: 20 bytes
```

The `c0 a8 01 01` in the ARP request is the IP address being asked about, 192.168.1.1 (the gateway). We continue splitting the IPv4 header into fields in lecture M7.

## Worked example 2: protocol statistics

The first line holds *n*; each of the next *n* lines is a short record of a captured packet: source IP, destination IP, protocol and size (bytes). For each protocol print the number of packets, the bytes and its share of the total (%, one decimal place) in decreasing order of bytes (by name on a tie); at the end print the address that sent the most bytes (the smaller string on a tie).

```python
n = int(input())
protokollar = {}                      # name -> [packets, bytes]
yuboruvchilar = {}
jami = 0
for _ in range(n):
    manba, manzil, protokol, hajm = input().split()
    hajm = int(hajm)
    jami += hajm
    p = protokollar.setdefault(protokol, [0, 0])
    p[0] += 1
    p[1] += hajm
    yuboruvchilar[manba] = yuboruvchilar.get(manba, 0) + hajm
for nom in sorted(protokollar, key=lambda x: (-protokollar[x][1], x)):
    paket, bayt = protokollar[nom]
    print(nom, paket, bayt, f"{bayt * 100 / jami:.1f}%")
eng = min(yuboruvchilar, key=lambda x: (-yuboruvchilar[x], x))
print("top sender:", eng, yuboruvchilar[eng])
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Map<String, long[]> protokollar = new TreeMap<>();     // name -> {packets, bytes}
        Map<String, Long> yuboruvchilar = new TreeMap<>();
        long jami = 0;
        for (int i = 0; i < n; i++) {
            String manba = in.next();
            String manzil = in.next();
            String protokol = in.next();
            long hajm = in.nextLong();
            jami += hajm;
            long[] p = protokollar.computeIfAbsent(protokol, k -> new long[2]);
            p[0]++;
            p[1] += hajm;
            yuboruvchilar.merge(manba, hajm, Long::sum);
        }
        List<String> nomlar = new ArrayList<>(protokollar.keySet());
        nomlar.sort((a, b) -> Long.compare(protokollar.get(b)[1], protokollar.get(a)[1]));   // ties keep the TreeMap order
        for (String nom : nomlar) {
            long[] p = protokollar.get(nom);
            System.out.println(nom + " " + p[0] + " " + p[1] + " " + String.format("%.1f", p[1] * 100.0 / jami) + "%");
        }
        String eng = null;
        for (Map.Entry<String, Long> e : yuboruvchilar.entrySet()) {
            if (eng == null || e.getValue() > yuboruvchilar.get(eng)) eng = e.getKey();
        }
        System.out.println("top sender: " + eng + " " + yuboruvchilar.get(eng));
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    map<string, pair<long long, long long>> protokollar;   // name -> {packets, bytes}
    map<string, long long> yuboruvchilar;
    long long jami = 0;
    for (int i = 0; i < n; i++) {
        string manba, manzil, protokol;
        long long hajm;
        cin >> manba >> manzil >> protokol >> hajm;
        jami += hajm;
        protokollar[protokol].first++;
        protokollar[protokol].second += hajm;
        yuboruvchilar[manba] += hajm;
    }
    vector<string> nomlar;
    for (auto& e : protokollar) nomlar.push_back(e.first);
    stable_sort(nomlar.begin(), nomlar.end(), [&](const string& a, const string& b) {
        return protokollar[a].second > protokollar[b].second;
    });
    for (auto& nom : nomlar) {
        auto p = protokollar[nom];
        printf("%s %lld %lld %.1f%%\n", nom.c_str(), p.first, p.second, p.second * 100.0 / jami);
    }
    string eng;
    for (auto& e : yuboruvchilar) {
        if (eng.empty() || e.second > yuboruvchilar[eng]) eng = e.first;
    }
    cout << "top sender: " << eng << " " << yuboruvchilar[eng] << endl;
    return 0;
}
```

Input:

```text
6
192.168.1.10 142.250.74.46 TCP 1500
142.250.74.46 192.168.1.10 TCP 1500
192.168.1.10 8.8.8.8 DNS 80
8.8.8.8 192.168.1.10 DNS 120
192.168.1.10 192.168.1.255 UDP 300
192.168.1.20 192.168.1.10 TCP 500
```

Output:

```text
TCP 3 3500 87.5%
UDP 1 300 7.5%
DNS 2 200 5.0%
top sender: 192.168.1.10 1880
```

This is a simplified version of Wireshark's **Protocol Hierarchy** and **Conversations** windows. The UDP packet sent to `192.168.1.255` is a subnet broadcast.

:::warning
**Common mistakes**

- **Expecting to see other computers' traffic in a switched network.** Without port mirroring (SPAN) you see only your own traffic, broadcasts and multicasts.
- **Mixing up capture and display filters.** `ip.addr == 10.0.0.1` is a display filter; in a capture filter you write `host 10.0.0.1`.
- **Ignoring the VLAN tag.** In a tagged frame the EtherType starts at byte 16, not 12; reading `0x8100` as the protocol is wrong.
- **Capturing other people's traffic without permission.** This may break the law. Use a sniffer only on your own computer, in a lab network or with the network owner's written permission.
:::

:::tip
In Wireshark right-click any TCP packet and choose **Follow → TCP Stream**: the whole conversation appears in one window. Try it with an HTTP (unencrypted) site and you will see for yourself why HTTPS is needed.
:::

## Summary

- Fast Ethernet (100BASE-TX): Cat5, two pairs, 4B/5B + MLT-3, 100 m; speed and duplex are negotiated automatically.
- A sniffer puts the adapter into promiscuous mode; in a switched network SPAN or a TAP is needed for other ports' traffic.
- Capture filters (BPF) choose the packets recorded, display filters the packets shown.
- The Ethernet header: 6 + 6 bytes of MAC, an optional 4-byte VLAN tag, 2 bytes of EtherType.
- Traffic statistics show which protocol and which device keep the network busy.

## Check yourself

:::check
Q: What is promiscuous mode?
A: A mode in which the adapter accepts all frames in the segment, not only those meant for it.
Q: What must be done to capture another computer's traffic in a switched network?
A: Set up port mirroring (SPAN) on the switch or install a TAP device.
Q: What does it mean if bytes 12–13 of a frame are `81 00`?
A: There is an 802.1Q VLAN tag; the next 2 bytes hold the VLAN number, and the real EtherType comes after them.
Q: What filter shows only DNS packets in Wireshark?
A: `dns` (or `udp.port == 53`).
Q: Which encoding methods are used in 100BASE-TX?
A: The block code 4B/5B and the line code MLT-3.
:::

:::link https://www.wireshark.org/docs/wsug_html_chunked/
Wireshark User's Guide
The official guide to capturing, filters and statistics.
:::
