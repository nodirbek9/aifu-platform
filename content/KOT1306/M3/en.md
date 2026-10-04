---
title: The ISO/OSI model. Open systems interconnection. Network layers. The notions of protocol, interface and standard
summary: Open systems and their interconnection; the seven layers of the ISO/OSI model, the job, data unit (PDU), protocols and devices of each; the difference between a protocol and an interface; encapsulation and decapsulation; standards organisations; comparing the OSI and TCP/IP models.
---

## Why it matters

When you open a page in a browser, data leaves the program and travels through the operating system, the network adapter, a cable, a switch and routers to a server on another continent — and there it goes the opposite way. Hundreds of devices and programs made by different companies do this. How do they "understand" each other? The answer is a **layered model**: the task is split into seven parts and separate standards are written for each part. The ISO/OSI model is the common language of network specialists: "the problem is at layer two" means something to every engineer.

:::note
**After this lesson you will be able to:**

- explain the notion of an open system and the advantage of a layered approach;
- list the seven OSI layers in order and state the job of each;
- tell a protocol from an interface;
- explain encapsulation step by step and calculate the overhead spent on headers;
- name the main standards organisations and the difference between the OSI and TCP/IP models.
:::

## Open systems

An **open system** is a system built according to open (publicly published) standards. Its interfaces and protocols are not secret, so any manufacturer can build a compatible device or program. For example, the Ethernet standard is open — which is why adapters from different companies plug into one switch and work together.

In 1984 the International Organization for Standardization (**ISO**) adopted a reference model for **Open Systems Interconnection** (**OSI**) (ISO/IEC 7498). The model divides communication over a network into **seven layers**. The model is not a protocol — it is a "map" describing which job each layer should do.

Advantages of the layered approach:

- **Dividing complexity** — each layer solves only its own task.
- **Independent replacement** — replacing copper cable with fibre requires no change to programs at the upper layers.
- **Compatibility** — since each layer is standardised, products of different manufacturers work together.
- **Teaching and diagnostics** — a problem can be searched for layer by layer.

## The seven layers of the OSI model

| № | Layer | Job | Data unit (PDU) | Protocols and technologies | Devices |
|---|---|---|---|---|---|
| 7 | **Application** | provides network services to user programs | message | HTTP, FTP, SMTP, DNS | gateway |
| 6 | **Presentation** | data format conversion, encryption, compression | message | TLS/SSL, JPEG, ASCII, UTF-8 | — |
| 5 | **Session** | establishing, managing and ending a communication session | message | NetBIOS, RPC | — |
| 4 | **Transport** | reliable (or fast) delivery between processes, segmentation, ports | segment (TCP) / datagram (UDP) | TCP, UDP | — |
| 3 | **Network** | logical addressing and routing | packet | IP, ICMP, OSPF | router |
| 2 | **Data Link** | delivering frames within one segment, physical (MAC) address, error detection | frame | Ethernet, Wi-Fi (802.11), PPP | switch, bridge |
| 1 | **Physical** | transmitting bits as signals over a medium | bit | RS-232, 100BASE-TX, DSL | repeater, hub, cable |

A mnemonic for the layers: **"Please Do Not Throw Sausage Pizza Away"** (1 to 7: Physical, Data Link, Network, Transport, Session, Presentation, Application).

The layers fall into two groups: **1–3** are network-dependent (they carry data from node to node and also run on routers), **4–7** run on the end systems (hosts) and provide communication between programs.

## Protocol and interface

| Notion | Between whom | Example |
|---|---|---|
| **Protocol** | between the **same** layers on different computers (horizontal) | the rules by which the TCP modules of two computers exchange segments |
| **Interface** | between **adjacent** layers on one computer (vertical) | a program calls TCP through a socket |
| **Service** | what a lower layer offers to the layer above | the transport layer offers the application layer a "reliable delivery" service |

A **protocol** defines three things: **syntax** (the message format — fields and their lengths), **semantics** (the meaning of each field and the action in response) and **timing** (the order and timing of actions). The set of protocols of all layers on one computer is called a **protocol stack**.

An important point: layer *N* talks **logically** to layer *N* on the other computer, but **in reality** data goes down, passes over the physical medium and goes up on the other side.

## Encapsulation and decapsulation

When data is sent, each layer "puts into an envelope" the unit coming from above: it adds its own **header**, and the data link layer also adds a **trailer** at the end (for example, the FCS — a checksum). This is **encapsulation**. At the receiver the process is reversed: each layer reads its header, removes it and passes the rest up — **decapsulation**.

```text
Application [              data              ]
Transport   [TCP 20][          data          ]               = segment
Network     [IP 20][TCP 20][       data       ]              = packet
Data Link   [Eth 14][IP 20][TCP 20][   data   ][FCS 4]       = frame
Physical    0101100101...                                    = bits
```

Headers are not payload — they are **overhead**. For Ethernet + IPv4 + TCP the overhead in one frame is 14 + 20 + 20 + 4 = 58 bytes (without optional fields). The data part of an Ethernet frame (the MTU) is 1500 bytes; taking away the IP and TCP headers leaves at most 1460 bytes of useful data in one segment (the MSS — Maximum Segment Size).

## Standards organisations

| Organisation | What it standardises |
|---|---|
| **ISO** (International Organization for Standardization) | the OSI model, many international standards |
| **IEEE** (Institute of Electrical and Electronics Engineers) | the 802 family: 802.3 Ethernet, 802.11 Wi-Fi, 802.1Q VLAN |
| **IETF** (Internet Engineering Task Force) | Internet protocols — the **RFC** documents (RFC 791 — IP, RFC 9293 — TCP) |
| **ITU-T** (International Telecommunication Union) | telecommunications: X.25, V.92 modems, G.992 ADSL |
| **ANSI / TIA / EIA** | cabling systems: TIA-568 (twisted-pair wiring schemes) |
| **W3C** | web standards: HTML, CSS |

Standards come in two kinds: **de jure** (adopted by an official body, for example IEEE 802.3) and **de facto** (standard in practice because they are widespread, for example early TCP/IP).

## OSI and TCP/IP

In practice the Internet runs on the **TCP/IP** model. It appeared before OSI and is simpler — four layers:

| TCP/IP layer | OSI equivalent | Protocols |
|---|---|---|
| Application | 7, 6, 5 | HTTP, DNS, SMTP, TLS |
| Transport | 4 | TCP, UDP |
| Internet | 3 | IP, ICMP, ARP* |
| Network access (Link) | 2, 1 | Ethernet, Wi-Fi |

\*ARP is usually considered to sit between layers 2 and 3.

The OSI model is used as a language for explanation and diagnostics, while TCP/IP is the actual set of protocols.

## Worked example 1: encapsulation overhead

The first line holds *k*, the next *k* lines hold the size *S* of data being sent (bytes, *S* ≥ 1). The data is split into segments of MSS = 1460 bytes, and 58 bytes of overhead are added to each frame. For each *S* print the number of frames, the total bytes that crossed the cable and the efficiency (the share of useful data, %, to two decimal places).

```python
MSS = 1460
SARF = 14 + 20 + 20 + 4          # Ethernet + IP + TCP + FCS

k = int(input())
for _ in range(k):
    s = int(input())
    kadrlar = (s + MSS - 1) // MSS   # division rounded up
    jami = s + kadrlar * SARF
    print(s, kadrlar, jami, f"{s * 100 / jami:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static final long MSS = 1460;
    static final long SARF = 14 + 20 + 20 + 4;   // Ethernet + IP + TCP + FCS

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        for (int i = 0; i < k; i++) {
            long s = in.nextLong();
            long kadrlar = (s + MSS - 1) / MSS;  // division rounded up
            long jami = s + kadrlar * SARF;
            System.out.println(s + " " + kadrlar + " " + jami + " " + String.format("%.2f", s * 100.0 / jami));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

const long long MSS = 1460;
const long long SARF = 14 + 20 + 20 + 4;   // Ethernet + IP + TCP + FCS

int main() {
    int k;
    cin >> k;
    for (int i = 0; i < k; i++) {
        long long s;
        cin >> s;
        long long kadrlar = (s + MSS - 1) / MSS;   // division rounded up
        long long jami = s + kadrlar * SARF;
        printf("%lld %lld %lld %.2f\n", s, kadrlar, jami, s * 100.0 / jami);
    }
    return 0;
}
```

Input:

```text
4
1460
100
1000000
1
```

Output:

```text
1460 1 1518 96.18
100 1 158 63.29
1000000 685 1039730 96.18
1 1 59 1.69
```

Conclusion: for large files the overhead is only ~4%, but for small messages (say, 1 byte of data about a single key press) more than 98% of the bytes on the cable are headers.

## Worked example 2: the path through the layers

A one-line message is given. On the sending side each layer from 7 down to 2 prepends the header `H<number>|`, and layer 2 also appends the trailer `|T2`; layer 1 transmits the result, reported as a number of bits. On the receiving side the headers are removed in reverse order. Print every step (`->` — sending, `<-` — receiving).

```python
pogonalar = [
    (7, "Application"), (6, "Presentation"), (5, "Session"),
    (4, "Transport"), (3, "Network"), (2, "Data Link"),
]

xabar = input()
birlik = xabar
for raqam, nom in pogonalar:                 # top down
    birlik = "H" + str(raqam) + "|" + birlik
    if raqam == 2:
        birlik = birlik + "|T2"              # the data link layer also adds a trailer
    print("->", raqam, nom + ":", birlik)
print("->", 1, "Physical:", len(birlik) * 8, "bit")

birlik = birlik[:len(birlik) - 3]            # the trailer is removed first
for raqam, nom in reversed(pogonalar):       # bottom up
    birlik = birlik[len("H" + str(raqam) + "|"):]
    print("<-", raqam, nom + ":", birlik)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int[] raqamlar = {7, 6, 5, 4, 3, 2};
        String[] nomlar = {"Application", "Presentation", "Session", "Transport", "Network", "Data Link"};
        Scanner in = new Scanner(System.in);
        String xabar = in.nextLine();
        String birlik = xabar;
        for (int i = 0; i < raqamlar.length; i++) {        // top down
            birlik = "H" + raqamlar[i] + "|" + birlik;
            if (raqamlar[i] == 2) {
                birlik = birlik + "|T2";                    // the data link layer also adds a trailer
            }
            System.out.println("-> " + raqamlar[i] + " " + nomlar[i] + ": " + birlik);
        }
        System.out.println("-> " + 1 + " Physical: " + birlik.length() * 8 + " bit");

        birlik = birlik.substring(0, birlik.length() - 3);  // the trailer is removed first
        for (int i = raqamlar.length - 1; i >= 0; i--) {   // bottom up
            birlik = birlik.substring(("H" + raqamlar[i] + "|").length());
            System.out.println("<- " + raqamlar[i] + " " + nomlar[i] + ": " + birlik);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int raqamlar[] = {7, 6, 5, 4, 3, 2};
    string nomlar[] = {"Application", "Presentation", "Session", "Transport", "Network", "Data Link"};
    string xabar;
    getline(cin, xabar);
    string birlik = xabar;
    for (int i = 0; i < 6; i++) {                          // top down
        birlik = "H" + to_string(raqamlar[i]) + "|" + birlik;
        if (raqamlar[i] == 2) {
            birlik = birlik + "|T2";                        // the data link layer also adds a trailer
        }
        cout << "-> " << raqamlar[i] << " " << nomlar[i] << ": " << birlik << endl;
    }
    cout << "-> " << 1 << " Physical: " << birlik.size() * 8 << " bit" << endl;

    birlik = birlik.substr(0, birlik.size() - 3);          // the trailer is removed first
    for (int i = 5; i >= 0; i--) {                         // bottom up
        birlik = birlik.substr(("H" + to_string(raqamlar[i]) + "|").size());
        cout << "<- " << raqamlar[i] << " " << nomlar[i] << ": " << birlik << endl;
    }
    return 0;
}
```

Input:

```text
Salom
```

Output:

```text
-> 7 Application: H7|Salom
-> 6 Presentation: H6|H7|Salom
-> 5 Session: H5|H6|H7|Salom
-> 4 Transport: H4|H5|H6|H7|Salom
-> 3 Network: H3|H4|H5|H6|H7|Salom
-> 2 Data Link: H2|H3|H4|H5|H6|H7|Salom|T2
-> 1 Physical: 208 bit
<- 2 Data Link: H3|H4|H5|H6|H7|Salom
<- 3 Network: H4|H5|H6|H7|Salom
<- 4 Transport: H5|H6|H7|Salom
<- 5 Session: H6|H7|Salom
<- 6 Presentation: H7|Salom
<- 7 Application: Salom
```

Note: the unit with the most headers is the frame (layer 2), and at the receiver each layer removes only **its own** header — this shows the independence of the layers.

:::warning
**Common mistakes**

- **Counting the layers backwards.** Layer 1 is physical (the lowest), layer 7 is application (the highest). The numbers grow from the bottom up.
- **Taking the "application layer" for the user's program itself.** A browser is a program; the application layer is the protocol it uses (HTTP).
- **Confusing protocol and interface.** A protocol is the rules between the same layers on different computers, an interface is the boundary between adjacent layers on one computer.
- **Thinking a switch is a layer-3 device.** An ordinary switch works with MAC addresses — layer 2; the one working with IP is a router (or a special "L3 switch").
- **Ignoring header overhead.** A 100 Mbit/s channel never carries 100 Mbit/s of useful data — part of it is taken by headers.
:::

:::tip
Install Wireshark, open any web page and select one packet: the program unfolds it layer by layer — Frame, Ethernet II, Internet Protocol, Transmission Control Protocol, Hypertext Transfer Protocol. Write down how many bytes each header takes.
:::

## Summary

- An open system is built on open standards so that products of different manufacturers can work together; OSI is ISO's 7-layer reference model.
- Layers: 1 physical, 2 data link, 3 network, 4 transport, 5 session, 6 presentation, 7 application; PDUs: bit, frame, packet, segment, message.
- A protocol is the (horizontal) rules between the same layers, an interface is the (vertical) boundary between adjacent layers.
- In encapsulation each layer adds a header, in decapsulation it removes it; header overhead is very large for small messages.
- Standards: ISO, IEEE (802), IETF (RFC), ITU-T, TIA; in practice TCP/IP (4 layers) is used.

## Check yourself

:::check
Q: Which OSI layer deals with routing, and what is its PDU?
A: Layer 3 — the network layer; its data unit is the packet.
Q: What is the main difference between a protocol and an interface?
A: A protocol is the rules between the same layers on different computers, while an interface is the boundary between adjacent layers on one computer.
Q: Which layer adds a trailer at the end of the data, and what is it for?
A: The data link layer; the FCS (checksum) in the trailer detects whether the frame was damaged in transmission.
Q: If 2920 bytes of data are sent with MSS = 1460 and 58 bytes of overhead, how many bytes cross the cable?
A: 2 frames: 2920 + 2 × 58 = 3036 bytes.
Q: Which OSI layers does the TCP/IP "Application" layer correspond to?
A: OSI layers 5, 6 and 7 (session, presentation, application).
:::

:::link https://www.iso.org/standard/20269.html
ISO/IEC 7498-1: the OSI reference model
The page of the official OSI model standard.
:::
