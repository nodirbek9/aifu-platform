---
title: Monitoring network traffic (Wireshark or a similar tool)
summary: Practical session — observing network traffic for defensive purposes; legal and ethical rules; the Wireshark window; capture filters and display filters, useful filters (http, dns, tls, ip.addr, tcp.port); following a stream; statistics — protocol hierarchy, conversations, endpoints, the I/O graph; identifying unencrypted protocols and replacing them with secure alternatives; signs of anomalies; tcpdump and flow logs (NetFlow); privacy; a program finding the top talkers and unencrypted connections in a flow log.
---

## Why does this topic matter?

A network administrator and a security specialist must be able to **see what is happening on the network**: which device talks to whom, which protocols are used, whether passwords are sent unencrypted, whether there is unusual activity. Traffic analysis is a key tool for troubleshooting, checking policy compliance and detecting incidents (the IDS and SIEM from M14 rely on it too).

:::note
**After this practical session you will be able to:**

- know the rules for observing traffic only under legal and authorized conditions;
- capture traffic in Wireshark and analyse it with filters;
- see a "picture" of the network with the statistics tools;
- find unencrypted protocols and propose replacing them with secure alternatives;
- analyse flow logs with a program.
:::

:::warning
**Legal and ethical rules.** Observe traffic only on **your own**, a **training** or a network **authorized in writing**. Capturing other people's traffic without authorization violates privacy and is an act for which the law provides liability (A3). In an organization, monitoring is defined by the security policy and employees are informed about it. Capture files may contain passwords and personal data — keep them as confidential data and delete them after the work.
:::

## Getting to know Wireshark

**Wireshark** is a free, open-source traffic analyser (Windows, Linux, macOS). Its window has three parts:

| Part | What it shows |
|---|---|
| **Packet list** | each packet: time, source, destination, protocol, length, a short note |
| **Packet details** | the structure of the selected packet by layer (Ethernet → IP → TCP/UDP → application) |
| **Bytes** | the packet's raw bytes (hexadecimal and text) |

**Preparation**: choosing the interface to capture on (Wi-Fi or Ethernet). **Promiscuous mode** — the network card accepts all frames on the segment, not only those addressed to it; on a modern switched network you still mostly see your own traffic (to observe a whole segment, a port mirror — SPAN — is configured on the switch, which is the administrator's job).

## Capture and display filters

- A **capture filter** (BPF syntax) limits what is recorded **before** capturing: `host 192.168.1.10`, `port 53`.
- A **display filter** chooses what to **show** from what has been recorded:

| Filter | What it shows |
|---|---|
| `http` | HTTP traffic |
| `dns` | DNS queries and responses |
| `tls` | TLS (HTTPS) connections |
| `ip.addr == 192.168.1.10` | packets involving this address |
| `tcp.port == 443` | TCP on port 443 |
| `tcp.flags.syn == 1 && tcp.flags.ack == 0` | the start of new TCP connections |
| `http.request.method == "POST"` | HTTP POST requests |
| `!(arp or dns)` | everything except ARP and DNS |

**Following a stream** (Follow → TCP Stream) shows the whole conversation of one connection as text.

## Statistics tools

| Menu (Statistics) | Use |
|---|---|
| **Protocol Hierarchy** | how traffic is distributed across protocols |
| **Conversations** | how much data was transferred between which pairs |
| **Endpoints** | the most active addresses ("top talkers") |
| **I/O Graph** | traffic volume over time — seeing sudden spikes |

## Practical work

**The lab**: your own computer or a training virtual machine; for teaching purposes, **your own test page** working over HTTP and HTTPS (for example, a sign-in form on a training web server, not a real password: `test123`).

1. Start capturing, open a few websites, then stop. In **Protocol Hierarchy**, note which protocols are most common.
2. The `dns` filter: which domains were queried? Are there unfamiliar domains?
3. Submit the sign-in form on your test page over **HTTP**, and view the request with the `http.request.method == "POST"` filter and **Follow → TCP Stream**: `test123` is visible in plain text. Repeat the same over **HTTPS** — with the `tls` filter only encrypted data is visible. Conclusion: why all sign-in pages must use HTTPS only.
4. Find the most active addresses with **Conversations** and **Endpoints**.
5. Write up the results in a report (without personal data) and delete the capture file.

## Unencrypted protocols and their alternatives

| Unencrypted | Port | Secure alternative |
|---|---|---|
| HTTP | 80 | HTTPS (443) |
| FTP | 21 | SFTP or FTPS |
| Telnet | 23 | SSH (22) |
| POP3 / IMAP | 110 / 143 | POP3S / IMAPS (995 / 993) |
| SNMP v1/v2c | 161 | SNMPv3 |

One of the important results of monitoring is finding such protocols on the network and drawing up a plan to replace them with their alternatives.

## Signs of anomalies

- Many DNS queries to unfamiliar or suspicious domains.
- Large outbound traffic outside working hours.
- Regular short connections from an internal device to an unfamiliar external address.
- Unexpected protocols (for example, Telnet on an IoT device — M15).
- A very large number of connection attempts from one address.

If such signs are found, they are investigated through the incident response process (M14).

## tcpdump and flow logs

- **tcpdump** — capturing traffic on the command line (on servers without a graphical interface): `sudo tcpdump -i eth0 port 53 -w dns.pcap` — the result can later be opened in Wireshark.
- **Flow logs** (NetFlow, IPFIX, cloud flow logs) — not every packet but short records about connections: from whom, to whom, which port, how many bytes. They are small, kept for a long time and convenient for observing large networks.

## Worked example: analysing a flow log

Each line holds a connection: source IP, destination IP, destination port, bytes. Output the total bytes for each source host in decreasing order, then show the connections over unencrypted protocols (21 — FTP, 23 — Telnet, 80 — HTTP, 110 — POP3, 143 — IMAP) in input order.

```python
import sys

OCHIQ = {"21": "FTP", "23": "Telnet", "80": "HTTP", "110": "POP3", "143": "IMAP"}

jami, ogohlantirish = {}, []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    manba, manzil, port, hajm = satr.split()
    jami[manba] = jami.get(manba, 0) + int(hajm)
    if port in OCHIQ:                                 # unencrypted protocol
        ogohlantirish.append(f"{manba} -> {manzil}:{port} ({OCHIQ[port]})")

for host, h in sorted(jami.items(), key=lambda x: (-x[1], x[0])):
    print(f"{host}: {h} bytes")
for o in ogohlantirish:
    print(f"Unencrypted: {o}")
```
```java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Map<String, String> ochiq = Map.of("21", "FTP", "23", "Telnet", "80", "HTTP", "110", "POP3", "143", "IMAP");
        Scanner in = new Scanner(System.in);
        Map<String, Long> jami = new LinkedHashMap<>();
        List<String> ogohlantirish = new ArrayList<>();
        while (in.hasNext()) {
            String manba = in.next(), manzil = in.next(), port = in.next();
            long hajm = in.nextLong();
            jami.merge(manba, hajm, Long::sum);
            if (ochiq.containsKey(port))                          // unencrypted protocol
                ogohlantirish.add(manba + " -> " + manzil + ":" + port + " (" + ochiq.get(port) + ")");
        }
        List<Map.Entry<String, Long>> royxat = new ArrayList<>(jami.entrySet());
        royxat.sort((a, b) -> !a.getValue().equals(b.getValue()) ? Long.compare(b.getValue(), a.getValue()) : a.getKey().compareTo(b.getKey()));
        for (Map.Entry<String, Long> e : royxat) System.out.println(e.getKey() + ": " + e.getValue() + " bytes");
        for (String o : ogohlantirish) System.out.println("Unencrypted: " + o);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    map<string, string> ochiq = {{"21", "FTP"}, {"23", "Telnet"}, {"80", "HTTP"}, {"110", "POP3"}, {"143", "IMAP"}};
    map<string, long long> jami;
    vector<string> ogohlantirish;
    string manba, manzil, port;
    long long hajm;
    while (cin >> manba >> manzil >> port >> hajm) {
        jami[manba] += hajm;
        if (ochiq.count(port))                                    // unencrypted protocol
            ogohlantirish.push_back(manba + " -> " + manzil + ":" + port + " (" + ochiq[port] + ")");
    }
    vector<pair<string, long long>> royxat(jami.begin(), jami.end());
    sort(royxat.begin(), royxat.end(), [](auto& a, auto& b) {
        return a.second != b.second ? a.second > b.second : a.first < b.first;
    });
    for (auto& [host, h] : royxat) cout << host << ": " << h << " bytes\n";
    for (auto& o : ogohlantirish) cout << "Unencrypted: " << o << "\n";
    return 0;
}
```

Input:

```text
192.168.1.10 142.250.1.1 443 52000
192.168.1.10 93.184.216.34 80 4800
192.168.1.25 10.0.0.5 23 1200
192.168.1.25 142.250.1.1 443 81000
192.168.1.40 8.8.8.8 53 900
192.168.1.40 203.0.113.9 21 15000
```

Output:

```text
192.168.1.25: 82200 bytes
192.168.1.10: 56800 bytes
192.168.1.40: 15900 bytes
Unencrypted: 192.168.1.10 -> 93.184.216.34:80 (HTTP)
Unencrypted: 192.168.1.25 -> 10.0.0.5:23 (Telnet)
Unencrypted: 192.168.1.40 -> 203.0.113.9:21 (FTP)
```

The result of the analysis is an action plan: manage the device `10.0.0.5` with SSH instead of Telnet, move the FTP server to SFTP, make HTTPS mandatory for the HTTP site. The most active host, `192.168.1.25`, should be checked to see whether its traffic matches its expected work.

:::warning
**Common mistakes**

- **Capturing traffic on a network without authorization.** A breach of the law.
- **Storing or sharing a capture file without protection.** It may contain passwords and personal data.
- **Confusing capture and display filters.** Their syntax differs.
- **Treating encrypted traffic as "invisible, so a problem".** Not seeing TLS content is normal; the metadata (addresses, volume, time) is still analysed.
:::

:::tip
In your report give only conclusions, statistics and masked addresses; do not attach the captured `.pcap` file to the assignment.
:::

## Summary

- Traffic is observed only on your own, a training or an authorized network; captured data is confidential.
- Wireshark: the packet list, details and bytes; capture filters (BPF) and display filters.
- Statistics: protocol hierarchy, conversations, endpoints, the I/O graph.
- The practical result of monitoring is finding unencrypted protocols and moving to alternatives such as HTTPS, SSH and SFTP, and investigating anomalies.
- Large networks use flow logs (NetFlow) and automated analysis.

## Check yourself

:::check
Q: How does a capture filter differ from a display filter?
A: A capture filter limits in advance what is recorded; a display filter chooses what to show from what has been recorded.
Q: Why can a password sent over HTTP be seen in a traffic analyser?
A: HTTP traffic is not encrypted.
Q: What is the secure alternative to Telnet?
A: SSH.
Q: Which Wireshark tool shows the most active addresses?
A: Statistics → Endpoints (and Conversations).
:::

:::link https://www.wireshark.org/docs/wsug_html_chunked/
Wireshark User’s Guide
The official Wireshark user guide.
:::
