---
title: The functions of the transport layer. The TCP and UDP protocols. The notion of sockets
summary: Transport layer functions — process-to-process delivery, multiplexing, ports; UDP — its header and uses; TCP — header fields, the three-way handshake, sequence numbers and acknowledgements, retransmission, the sliding window, flow and congestion control, closing a connection and the states; comparing TCP and UDP; the notion of a socket and the order of socket API calls.
---

## Why it matters

IP delivers a packet to a **computer**. But a computer runs a browser, a messenger, a music app and an update service at the same time — **which program** does the incoming data belong to? Besides, an IP packet may be lost or arrive out of order, while a file must arrive correct down to the byte. The **transport layer** solves these two problems: **ports** identify the program, and **TCP** provides reliable delivery. Every network program reaches the transport layer through a **socket**.

:::note
**After this lesson you will be able to:**

- explain the functions of the transport layer and the meaning of ports;
- compare TCP and UDP and justify which suits which task;
- calculate the three-way handshake and the sequence/acknowledgement numbers step by step;
- calculate how the sliding window limits the speed;
- explain the notion of a socket and the order of server and client socket calls.
:::

## The functions of the transport layer

| Function | Meaning |
|---|---|
| **Process-to-process delivery** | IP — computer to computer, transport — program to program |
| **Multiplexing** | many programs use one IP address at once; they are told apart by the **port** number |
| **Segmentation** | splitting a large data stream into segments and reassembling it |
| **Reliability** (TCP) | resending what was lost, removing duplicates, restoring order |
| **Flow control** (TCP) | a fast sender must not "flood" a slow receiver |
| **Congestion control** (TCP) | slowing down when the network is loaded |

A **port** is a 16-bit number (0–65,535): 0–1023 are **well-known** ports, 1024–49,151 are registered, 49,152–65,535 are **ephemeral** ports — the OS hands them out automatically for client connections.

| Port | Protocol | Port | Protocol |
|---|---|---|---|
| 20, 21 | FTP | 80 | HTTP |
| 22 | SSH | 110 / 143 | POP3 / IMAP |
| 23 | Telnet | 443 | HTTPS |
| 25 | SMTP | 3389 | RDP |
| 53 | DNS (UDP and TCP) | 67, 68 | DHCP (UDP) |

Every TCP connection is identified by a **5-tuple**: protocol, source IP, source port, destination IP, destination port. That is why thousands of clients can connect to port 443 of one web server at once — their IP or port differs.

## UDP

**UDP** (User Datagram Protocol) is a minimal transport: its header is only **8 bytes** (source port, destination port, length, checksum). No connection is set up, there are no acknowledgements or retransmissions, and order is not guaranteed. Its advantage is **speed and low delay**. Uses: DNS queries, online games, voice and video calls (a lost packet is better than a late one), DHCP, IPTV multicast. Modern **QUIC** (HTTP/3) builds its own reliability on top of UDP.

## TCP

**TCP** (Transmission Control Protocol) is a reliable, ordered, connection-oriented **byte stream**. Its header is at least **20 bytes**:

| Field | Meaning |
|---|---|
| Source and destination port | 16 + 16 bits |
| **Sequence number** | the number in the stream of the segment's first byte |
| **Acknowledgement number** | the **next** byte the receiver **expects** |
| Header length | in 4-byte words |
| Flags | **SYN** (connect), **ACK** (the acknowledgement field is valid), **FIN** (close), **RST** (reset), PSH, URG |
| **Window** | how many more bytes the receiver can accept (flow control) |
| Checksum | for the header and the data |

### The three-way handshake

1. Client → server: **SYN**, seq = *x* (a random initial number, the ISN).
2. Server → client: **SYN+ACK**, seq = *y*, ack = *x* + 1.
3. Client → server: **ACK**, seq = *x* + 1, ack = *y* + 1.

The SYN and FIN flags each take up one "virtual byte" — hence the +1 in the acknowledgement. After that data flows: if an *L*-byte segment arrives with seq = *s*, the receiver answers with ack = *s* + *L*.

**Closing** takes four steps (each side sends a FIN and the other returns an ACK). The side that closes actively waits 2×MSL in the **TIME_WAIT** state so that late segments do not get mixed into a new connection. The main states: LISTEN, SYN_SENT, SYN_RECEIVED, ESTABLISHED, FIN_WAIT, CLOSE_WAIT, TIME_WAIT, CLOSED (visible in `netstat -an`).

### Reliability, the window and congestion

- **Retransmission**: if the acknowledgement does not arrive before the timer (RTO) runs out, the segment is resent; after three duplicate ACKs it is resent at once (**fast retransmit**).
- **Sliding window**: the sender may send up to a window's worth of bytes without waiting for acknowledgements. So the speed of one connection is ≤ window / RTT. To fill the network fully, the window must be at least the **BDP** (bandwidth-delay product = rate × RTT); windows larger than 64 KB need the **window scaling** option.
- **Congestion control**: the sender keeps an extra **cwnd** window — **slow start** (doubling every RTT), then **AIMD** (+1 segment every RTT, halving on loss). The effective window = min(cwnd, the receiver's window).

## TCP and UDP

| | TCP | UDP |
|---|---|---|
| Connection | yes (handshake) | no |
| Reliability | acknowledgements and retransmission | none |
| Order | restored | not guaranteed |
| Header | 20–60 bytes | 8 bytes |
| Flow/congestion control | yes | no |
| Data | a byte stream | separate messages (datagrams) |
| Uses | web, mail, files, SSH | DNS, voice/video, games, DHCP |

## Sockets

A **socket** is the **programming interface** (API) between a program and the transport layer; the program's "end" of a network connection. It is identified by an IP address, a port and a protocol. The socket API appeared in BSD Unix in 1983 and is now almost the same in every language:

```text
TCP server                         TCP client
socket()   create a socket         socket()
bind()     bind to IP:port
listen()   wait for connections
accept()   accept a connection  <—  connect()   (the three-way handshake)
recv()/send()                      send()/recv()
close()                            close()

UDP: socket() → bind() (server) → sendto()/recvfrom() → close()   (connectionless)
```

`accept()` returns a **new** socket for every client, while the listening socket keeps waiting for the next clients. A practical program with sockets is in session A9.

## Worked example 1: sequence and acknowledgement numbers

The first line holds the initial sequence numbers (ISNs) of the client and the server. The following lines say who is sending (`C` — client, `S` — server) and the data length. Print the three-way handshake, then every data segment and the other side's ACK in reply. Input is read until the end of the file.

```python
import sys

satrlar = sys.stdin.read().split("\n")
c_seq, s_seq = map(int, satrlar[0].split())
print("C->S SYN seq=" + str(c_seq))
print("S->C SYN+ACK seq=" + str(s_seq) + " ack=" + str(c_seq + 1))
c_seq += 1                                   # SYN takes up one number
s_seq += 1
print("C->S ACK seq=" + str(c_seq) + " ack=" + str(s_seq))
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    kim, uzunlik = satr.split()
    uzunlik = int(uzunlik)
    if kim == "C":
        print("C->S seq=" + str(c_seq) + " ack=" + str(s_seq) + " len=" + str(uzunlik))
        c_seq += uzunlik
        print("S->C ACK seq=" + str(s_seq) + " ack=" + str(c_seq))   # the next expected byte
    else:
        print("S->C seq=" + str(s_seq) + " ack=" + str(c_seq) + " len=" + str(uzunlik))
        s_seq += uzunlik
        print("C->S ACK seq=" + str(c_seq) + " ack=" + str(s_seq))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long cSeq = in.nextLong();
        long sSeq = in.nextLong();
        System.out.println("C->S SYN seq=" + cSeq);
        System.out.println("S->C SYN+ACK seq=" + sSeq + " ack=" + (cSeq + 1));
        cSeq += 1;                                            // SYN takes up one number
        sSeq += 1;
        System.out.println("C->S ACK seq=" + cSeq + " ack=" + sSeq);
        while (in.hasNext()) {
            String kim = in.next();
            long uzunlik = in.nextLong();
            if (kim.equals("C")) {
                System.out.println("C->S seq=" + cSeq + " ack=" + sSeq + " len=" + uzunlik);
                cSeq += uzunlik;
                System.out.println("S->C ACK seq=" + sSeq + " ack=" + cSeq);   // the next expected byte
            } else {
                System.out.println("S->C seq=" + sSeq + " ack=" + cSeq + " len=" + uzunlik);
                sSeq += uzunlik;
                System.out.println("C->S ACK seq=" + cSeq + " ack=" + sSeq);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    long long cSeq, sSeq;
    cin >> cSeq >> sSeq;
    cout << "C->S SYN seq=" << cSeq << endl;
    cout << "S->C SYN+ACK seq=" << sSeq << " ack=" << cSeq + 1 << endl;
    cSeq += 1;                                                // SYN takes up one number
    sSeq += 1;
    cout << "C->S ACK seq=" << cSeq << " ack=" << sSeq << endl;
    string kim;
    long long uzunlik;
    while (cin >> kim >> uzunlik) {
        if (kim == "C") {
            cout << "C->S seq=" << cSeq << " ack=" << sSeq << " len=" << uzunlik << endl;
            cSeq += uzunlik;
            cout << "S->C ACK seq=" << sSeq << " ack=" << cSeq << endl;   // the next expected byte
        } else {
            cout << "S->C seq=" << sSeq << " ack=" << cSeq << " len=" << uzunlik << endl;
            sSeq += uzunlik;
            cout << "C->S ACK seq=" << cSeq << " ack=" << sSeq << endl;
        }
    }
    return 0;
}
```

Input (the client sends a 100-byte request, the server replies in two segments of 1460 and 540 bytes):

```text
1000 5000
C 100
S 1460
S 540
```

Output:

```text
C->S SYN seq=1000
S->C SYN+ACK seq=5000 ack=1001
C->S ACK seq=1001 ack=5001
C->S seq=1001 ack=5001 len=100
S->C ACK seq=5001 ack=1101
S->C seq=5001 ack=1101 len=1460
C->S ACK seq=1101 ack=6461
S->C seq=6461 ack=1101 len=540
C->S ACK seq=1101 ack=7001
```

Wireshark shows the same numbers, but usually in **relative** form (taking the ISN as 0). The real ISN is chosen at random so that an attacker cannot slip in a fake segment with the "right" number.

## Worked example 2: the window and the speed limit

Each line holds a TCP window (bytes), the RTT (ms) and the link rate (Mbit/s). Print the highest speed of one connection (Mbit/s, two decimal places; the smaller of window/RTT and the link rate) and the window needed to fill the link — the BDP (bytes).

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    oyna, rtt, kanal = map(int, satr.split())
    oyna_tezligi = oyna * 8 / (rtt / 1000) / 1_000_000    # Mbit/s
    tezlik = min(oyna_tezligi, kanal)
    bdp = kanal * rtt * 125                               # Mbit/s * ms -> bytes
    print(f"{tezlik:.2f} Mbit/s BDP={bdp}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long oyna = in.nextLong();
            long rtt = in.nextLong();
            long kanal = in.nextLong();
            double oynaTezligi = oyna * 8 / (rtt / 1000.0) / 1_000_000;   // Mbit/s
            double tezlik = Math.min(oynaTezligi, kanal);
            long bdp = kanal * rtt * 125;                                // Mbit/s * ms -> bytes
            System.out.println(String.format("%.2f", tezlik) + " Mbit/s BDP=" + bdp);
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
    long long oyna, rtt, kanal;
    while (cin >> oyna >> rtt >> kanal) {
        double oynaTezligi = oyna * 8 / (rtt / 1000.0) / 1000000;      // Mbit/s
        double tezlik = min(oynaTezligi, (double) kanal);
        long long bdp = kanal * rtt * 125;                             // Mbit/s * ms -> bytes
        printf("%.2f Mbit/s BDP=%lld\n", tezlik, bdp);
    }
    return 0;
}
```

Input:

```text
65535 50 100
1048576 50 100
65535 1 1000
```

Output:

```text
10.49 Mbit/s BDP=625000
100.00 Mbit/s BDP=625000
524.28 Mbit/s BDP=125000
```

The first line is the classic problem: a 100 Mbit/s link, but with a 64 KB window and a 50 ms RTT a connection gets only ~10 Mbit/s; a 625,000-byte (≈ 610 KB) window is needed. That is why downloading from a distant server can be slow even on a fast link — if window scaling is not enabled.

:::warning
**Common mistakes**

- **Thinking the acknowledgement number is the last byte received.** The ACK is the number of the **next expected** byte.
- **Treating SYN and FIN as "empty".** They each take up one sequence number — hence seq + 1 after the handshake.
- **Seeing UDP as "bad TCP".** For real-time applications retransmission does harm: a late voice packet is useless.
- **Taking a port and a socket for the same thing.** A port is a number; a socket is a program's connection point identified by IP, port and protocol.
- **Thinking speed depends only on the link.** One TCP connection cannot go faster than window/RTT.
:::

:::tip
Open several sites in a browser and run `netstat -an | findstr ESTABLISHED` (Linux: `ss -tn`) at the command line: each line shows a local IP:port (an ephemeral port) and a remote IP:443. These are your open TCP connections.
:::

## Summary

- The transport layer delivers from program to program; programs are told apart by ports; a connection is identified by a 5-tuple.
- UDP: an 8-byte header, connectionless, no guarantees, fast — DNS, voice, games.
- TCP: the three-way handshake, seq/ack, retransmission, the sliding window, flow and congestion control, a four-step close.
- One TCP connection's speed ≤ window / RTT; to fill a link the window must be ≥ the BDP.
- A socket is a program's entry point to the network: the server does socket–bind–listen–accept, the client socket–connect.

## Check yourself

:::check
Q: The client sent seq = 300 in its SYN. What ack does the server return in its SYN+ACK?
A: ack = 301 — the SYN takes up one sequence number.
Q: A segment with seq = 2000 and 500 bytes of data was received. What is the acknowledgement number in the reply ACK?
A: 2500 — the next expected byte.
Q: Why are DNS queries usually sent over UDP?
A: The query and reply are small, and setting up a connection would add needless delay; if one is lost, the client simply repeats the query.
Q: The window is 32 KB and the RTT is 100 ms. What is the highest speed of one connection?
A: 32,768 × 8 / 0.1 ≈ 2.6 Mbit/s.
Q: What does `accept()` return on a server?
A: A new socket for working with the accepted client; the listening socket keeps waiting for further connections.
:::

:::link https://www.rfc-editor.org/rfc/rfc9293
RFC 9293: Transmission Control Protocol
The current specification of the TCP protocol.
:::
