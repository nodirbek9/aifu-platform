---
title: The concept and types of computer networks
summary: Independent study — packet switching; layered models (OSI and TCP/IP) and the role of each layer; addressing — MAC, IPv4, IPv6, private addresses, NAT, DHCP, ports; classifying networks (area, topology, management — client-server and peer-to-peer, transmission medium); network quality indicators — bandwidth, latency, loss; bits and bytes; simple diagnostics for office staff (ipconfig, ping, tracert, speedtest); a program calculating file transfer time.
---

## Purpose of this independent study

In M6 we got to know network types, devices and Internet services. In this independent study we look more deeply at **how a network works inside**: how data is split into pieces, how layers cooperate, how addresses are assigned and how network quality is measured.

:::note
**After this independent study you will be able to:**

- explain the idea of packet switching and the layered model;
- say what MAC and IP addresses, private addresses, NAT, DHCP and ports are;
- classify networks by different characteristics;
- distinguish bandwidth from latency and calculate transfer time;
- use simple network diagnostic commands.
:::

## Packet switching

In the telephone network a separate line is reserved for a call (circuit switching). In computer networks data is split into small **packets** (usually up to 1,500 bytes); each packet is sent separately with an address and reassembled at the recipient. The advantage: many users share one channel at the same time, and if a route breaks, packets go another way.

## Layered models

Network tasks are divided into layers: each layer does only its own job and "talks" to the neighbouring layers. **OSI** is a 7-layer theoretical model; **TCP/IP** is the 4-layer model actually used on the Internet.

| TCP/IP layer | OSI layers | Role | Examples |
|---|---|---|---|
| **Application** | 7 application, 6 presentation, 5 session | user services | HTTP/HTTPS, SMTP, DNS, TLS |
| **Transport** | 4 transport | delivery between applications, reliability | TCP (guaranteed), UDP (fast) |
| **Network (Internet)** | 3 network | addressing and routing | IP, router |
| **Link and physical** | 2 data link, 1 physical | transmission between neighbouring devices, signals | Ethernet, Wi-Fi, switch, cable |

**An analogy — the post**: the text of a letter (application) → putting it in an envelope and sending it by registered mail (transport) → the address and postcode (network) → a van, train or plane (link and physical). No stage needs to know how another works inside.

**TCP or UDP?** TCP checks that every packet has arrived and resends lost ones — for web pages, email, bank payments. UDP does not check but is fast — for video calls and online games (one lost frame does not matter).

## Addressing

| Concept | Meaning | Example |
|---|---|---|
| **MAC address** | the network card's factory address, used within the local network | `3C-52-82-1A-7F-04` |
| **IPv4 address** | 32 bits, four numbers 0–255; ~4.3 billion addresses — not enough | `213.230.96.15` |
| **IPv6 address** | 128 bits, practically unlimited addresses | `2001:db8::1` |
| **Private addresses** | only inside a network, not routed on the Internet | `192.168.x.x`, `10.x.x.x`, `172.16–31.x.x` |
| **NAT** | a router lets many internal addresses reach the Internet through one external address | 10 devices at home — 1 external IP |
| **DHCP** | automatically gives a device its IP address, gateway and DNS | when connecting to Wi-Fi |
| **Port** | identifies which application on a computer is meant | 443 — HTTPS, 25 — SMTP |
| **Mask** | which part of the address is the network and which the device | `255.255.255.0` (/24 — 254 devices) |

## Classifying networks

| Characteristic | Types |
|---|---|
| **Area** | PAN, LAN, MAN, WAN (M6) |
| **Topology** | star, bus, ring, mesh |
| **Management model** | **client-server** (a central server — a corporate network, a website) and **peer-to-peer** (equal nodes — file sharing, blockchain, M12) |
| **Transmission medium** | wired (copper, optical fibre), wireless (Wi-Fi, mobile, satellite) |
| **Access** | public (the Internet), private (an intranet), an extranet (for partners), a virtual private network via VPN |

## Network quality indicators

| Indicator | Meaning | Unit |
|---|---|---|
| **Bandwidth** (speed) | how much data is transferred per second | Mbit/s, Gbit/s |
| **Latency** (ping) | the round-trip time of a packet | ms |
| **Jitter** | the variability of latency | ms |
| **Packet loss** | the share of packets that do not arrive | % |

**Important**: providers state speed in **bits** (Mbit/s), while file sizes are in **bytes** (MB); 1 byte = 8 bits. 100 Mbit/s ≈ 12.5 MB/s. The real speed is usually 10–30% below the theoretical one (protocol "overhead", Wi-Fi interference, server speed).

For video calls latency and jitter matter (latency above 150 ms makes conversation difficult); for large files bandwidth matters.

## Worked example: file transfer time

Each line holds a file name, its size (MB) and the channel speed (Mbit/s). Output the theoretical transfer time and the practical time (assuming 80% efficiency) as hours:minutes:seconds, rounding seconds up.

```python
import sys

def vaqt_matni(soniya):
    return f"{soniya // 3600}:{soniya % 3600 // 60:02d}:{soniya % 60:02d}"

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, hajm, tezlik = satr.split()
    hajm, tezlik = int(hajm), int(tezlik)
    bit = hajm * 8                                      # MB -> Mbit
    nazariy = (bit + tezlik - 1) // tezlik              # round up
    amalda = (bit * 100 + tezlik * 80 - 1) // (tezlik * 80)   # 80% efficiency
    print(f"{nom}, {hajm} MB, {tezlik} Mbit/s: theoretical {vaqt_matni(nazariy)}, in practice {vaqt_matni(amalda)}")
```
```java
import java.util.Scanner;

public class Main {
    static String vaqtMatni(long soniya) {
        return String.format("%d:%02d:%02d", soniya / 3600, soniya % 3600 / 60, soniya % 60);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            long hajm = in.nextLong(), tezlik = in.nextLong();
            long bit = hajm * 8;                                         // MB -> Mbit
            long nazariy = (bit + tezlik - 1) / tezlik;                  // round up
            long amalda = (bit * 100 + tezlik * 80 - 1) / (tezlik * 80);  // 80% efficiency
            System.out.println(nom + ", " + hajm + " MB, " + tezlik + " Mbit/s: theoretical " + vaqtMatni(nazariy) + ", in practice " + vaqtMatni(amalda));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

string vaqtMatni(long long soniya) {
    char buf[32];
    snprintf(buf, sizeof buf, "%lld:%02lld:%02lld", soniya / 3600, soniya % 3600 / 60, soniya % 60);
    return buf;
}

int main() {
    string nom;
    long long hajm, tezlik;
    while (cin >> nom >> hajm >> tezlik) {
        long long bit = hajm * 8;                                        // MB -> Mbit
        long long nazariy = (bit + tezlik - 1) / tezlik;                 // round up
        long long amalda = (bit * 100 + tezlik * 80 - 1) / (tezlik * 80); // 80% efficiency
        cout << nom << ", " << hajm << " MB, " << tezlik << " Mbit/s: theoretical " << vaqtMatni(nazariy)
             << ", in practice " << vaqtMatni(amalda) << "\n";
    }
    return 0;
}
```

Input:

```text
report.pdf 25 100
video.mp4 1500 100
video.mp4 1500 20
backup.zip 50000 100
backup.zip 50000 1000
```

Output:

```text
report.pdf, 25 MB, 100 Mbit/s: theoretical 0:00:02, in practice 0:00:03
video.mp4, 1500 MB, 100 Mbit/s: theoretical 0:02:00, in practice 0:02:30
video.mp4, 1500 MB, 20 Mbit/s: theoretical 0:10:00, in practice 0:12:30
backup.zip, 50000 MB, 100 Mbit/s: theoretical 1:06:40, in practice 1:23:20
backup.zip, 50000 MB, 1000 Mbit/s: theoretical 0:06:40, in practice 0:08:20
```

Uploading a 50 GB backup to the cloud over a 100 Mbit/s channel takes 1 hour 23 minutes in practice — during working hours this slows down other employees' Internet. Solutions: upload the backup at night, send only changed files (incremental backup), or get a faster channel. Such calculations are needed when choosing a channel (M6) and in a backup plan (M14).

## Simple diagnostics

| Command (Windows) | What it does |
|---|---|
| `ipconfig` | the computer's IP address, mask, gateway |
| `ping 8.8.8.8` | reaching an external server and the latency |
| `ping google.com` | also checks that DNS works |
| `tracert google.com` | which routers the packet passes through |
| speedtest websites | the real download and upload speed, ping |

**The order**: if "there is no Internet", check the cable or Wi-Fi first, then `ipconfig` (is there an address?), then `ping` the gateway, then an external IP, then a domain. Whichever step fails is where the problem is. This helps give the IT department precise information.

:::warning
**Common mistakes**

- **Confusing Mbit/s and MB/s.** An 8-fold difference.
- **Confusing speed with latency.** Even on a fast channel, a satellite link gives high latency.
- **Thinking a private address (192.168...) can be reached from the Internet.** It is visible only inside the internal network.
- **Treating the speed the provider promises as a guarantee.** Check the SLA in the contract.
:::

## Assignment

1. Use the `ipconfig` command to find out whether your computer's IP address is private or public.
2. `ping` three different websites and compare the latencies; explain the difference.
3. Measure the real speed of your home or university Internet with a speedtest and compare it with the speed in the provider's tariff.
4. With the example program, calculate the time to download a 2 GB video lesson over 4G (20 Mbit/s) and over optical fibre (200 Mbit/s).

## Summary

- Data is transferred split into packets; network tasks are divided into layers (OSI — 7, TCP/IP — 4).
- Addressing: MAC, IPv4/IPv6, private addresses and NAT, DHCP, ports.
- Classification: area, topology, management model, medium, access.
- Quality: bandwidth, latency, jitter, loss; 1 byte = 8 bits.

## Check yourself

:::check
Q: What is the main difference between TCP and UDP?
A: TCP guarantees delivery and resends lost packets; UDP does not guarantee it but is faster.
Q: Why is NAT needed?
A: To let many internal devices reach the Internet through one external IP address (there are not enough IPv4 addresses).
Q: In theory, how many seconds does it take to download a 100 MB file over a 100 Mbit/s channel?
A: 100 × 8 / 100 = 8 seconds.
Q: What does DHCP do?
A: It automatically gives a device its IP address, gateway and DNS settings.
:::

:::link https://www.cloudflare.com/learning/network-layer/what-is-the-network-layer/
Cloudflare Learning Center: Network layer
Simple explanations of network layers, IP and routing.
:::
