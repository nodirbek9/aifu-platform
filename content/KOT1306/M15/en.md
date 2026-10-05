---
title: Troubleshooting network problems. Finding and fixing problems that arise while a network is running
summary: A troubleshooting methodology; OSI-based approaches — bottom-up, top-down, divide and conquer; the typical problems and symptoms of each layer; the main utilities — ping, tracert, ipconfig, nslookup, arp, netstat, pathping; analysing ping results — loss, RTT, jitter; documentation.
---

## Why it matters

"The Internet isn't working" is the phrase a network administrator hears most. Behind it may be a broken cable, a wrong IP address, a DNS error, a firewall rule or a provider problem. Random actions (rebooting everything) sometimes help, but without understanding the problem they let it come back. With a systematic approach the problem can be found in minutes.

:::note
**After this lesson you will be able to:**

- explain the stages of troubleshooting and the OSI-based approaches;
- identify the typical problems of each layer from their symptoms;
- interpret the results of `ping`, `tracert`, `ipconfig`, `nslookup`, `arp`, `netstat`;
- calculate loss, delay and jitter from ping statistics;
- tell from the results of checks which layer a problem is at.
:::

## Methodology

1. **Identify the problem**: what does not work, for whom, since when, what has changed? ("Only for you, or for the whole room?")
2. **Form a hypothesis**: choose the most likely cause.
3. **Test it**: try the hypothesis; if it is not confirmed — the next hypothesis.
4. **Plan and act**: the fix (assessing its impact in advance).
5. **Verify**: does everything work, has a new problem appeared?
6. **Document**: write down the cause, the symptoms and the solution — faster next time.

## OSI-based approaches

| Approach | How | When it suits |
|---|---|---|
| **Bottom-up** | cable → link → IP → transport → application | a physical problem is likely, a new connection |
| **Top-down** | application → … → cable | only one program does not work |
| **Divide and conquer** | start from the middle layer (`ping` — layer 3): if it works — go up, if not — go down | the fastest for an experienced engineer |

## Typical problems by layer

| Layer | Symptoms | Causes | Checks |
|---|---|---|---|
| 1 — Physical | port light off, "Network cable unplugged" | broken or wrong cable, port off, weak Wi-Fi | lights, cable tester, another port |
| 2 — Data link | connected, but nobody can be reached | wrong VLAN, duplex mismatch, STP block, MAC filter | `show interfaces`, `arp -a` |
| 3 — Network | 169.254.x.x, "Destination host unreachable" | DHCP not working, wrong IP/mask/gateway, no route | `ipconfig /all`, `ping` the gateway, `tracert` |
| 4 — Transport | ping works, but the service cannot be reached | port closed, firewall, service not running | `Test-NetConnection server -Port 443`, `netstat -an` |
| 7 — Application | works by IP, not by name; site error | DNS, proxy, certificate, application settings | `nslookup`, the browser console |

## The main utilities

| Command (Windows) | Linux | What it gives |
|---|---|---|
| `ipconfig /all` | `ip addr`, `ip route` | address, mask, gateway, DNS, DHCP |
| `ipconfig /release`, `/renew`, `/flushdns` | `dhclient -r`, `resolvectl flush-caches` | renew the address, clear the DNS cache |
| `ping` | `ping` | connectivity and RTT (ICMP Echo) |
| `tracert` | `traceroute` | the routers on the way |
| `pathping` | `mtr` | loss and delay at every hop |
| `nslookup` | `dig`, `nslookup` | DNS queries |
| `arp -a` | `ip neigh` | the ARP cache |
| `netstat -ano` | `ss -tunap` | open ports and connections |
| `Test-NetConnection` | `nc -zv` | whether a TCP port is open |

**The classic sequence** ("the Internet isn't working"):

```text
ipconfig /all                 is there an address? 169.254 means a DHCP problem
ping 127.0.0.1                does the TCP/IP stack work
ping 192.168.1.1              is the gateway reachable (local network)
ping 8.8.8.8                  is the Internet reachable by IP
ping google.com               does DNS work (if 8.8.8.8 works and this does not — DNS)
tracert 8.8.8.8               where does it break
```

## Understanding ping results

- **Packet loss**: 0% — normal; 1–2% — noticeable (voice calls break up); more — a serious problem (a noisy cable, a full link, weak Wi-Fi).
- **RTT** (round-trip time): < 1 ms on a LAN, 5–20 ms within a city, 100–250 ms to another continent.
- **Jitter** — the variation of consecutive RTTs; important for voice and video (should not exceed 30 ms).
- `Request timed out` — no reply came (the packet was lost or ICMP is blocked); `Destination host unreachable` — a router found no route; `TTL expired in transit` — a routing loop.

## Worked example 1: ping statistics

Each line holds one ping result: the RTT (ms, an integer) or `*` (no reply). Print the number of packets sent and received and the loss percentage, the minimum, average and maximum RTT, and the jitter (the mean absolute difference of the RTTs of consecutive replies received).

```python
import sys

natijalar = sys.stdin.read().split()
rtt = [int(x) for x in natijalar if x != "*"]
yuborildi = len(natijalar)
qabul = len(rtt)
yoqotish = (yuborildi - qabul) * 100 / yuborildi
print(f"packets: {yuborildi} sent, {qabul} received, {yoqotish:.1f}% lost")
if qabul == 0:
    print("RTT: no reply")
else:
    print(f"RTT: min {min(rtt)}, avg {sum(rtt) / qabul:.1f}, max {max(rtt)} ms")
    farqlar = [abs(rtt[i] - rtt[i - 1]) for i in range(1, qabul)]
    jitter = sum(farqlar) / len(farqlar) if farqlar else 0.0   # mean of consecutive differences
    print(f"jitter: {jitter:.1f} ms")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> rtt = new ArrayList<>();
        int yuborildi = 0;
        while (in.hasNext()) {
            String x = in.next();
            yuborildi++;
            if (!x.equals("*")) rtt.add(Integer.parseInt(x));
        }
        int qabul = rtt.size();
        double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
        System.out.println(String.format("packets: %d sent, %d received, %.1f%% lost", yuborildi, qabul, yoqotish));
        if (qabul == 0) {
            System.out.println("RTT: no reply");
        } else {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            long summa = 0;
            for (int r : rtt) {
                min = Math.min(min, r);
                max = Math.max(max, r);
                summa += r;
            }
            System.out.println(String.format("RTT: min %d, avg %.1f, max %d ms", min, (double) summa / qabul, max));
            double farqlar = 0;
            for (int i = 1; i < qabul; i++) farqlar += Math.abs(rtt.get(i) - rtt.get(i - 1));
            double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;     // mean of consecutive differences
            System.out.println(String.format("jitter: %.1f ms", jitter));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<int> rtt;
    int yuborildi = 0;
    string x;
    while (cin >> x) {
        yuborildi++;
        if (x != "*") rtt.push_back(stoi(x));
    }
    int qabul = rtt.size();
    double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
    printf("packets: %d sent, %d received, %.1f%% lost\n", yuborildi, qabul, yoqotish);
    if (qabul == 0) {
        printf("RTT: no reply\n");
    } else {
        long long summa = 0;
        for (int r : rtt) summa += r;
        printf("RTT: min %d, avg %.1f, max %d ms\n", *min_element(rtt.begin(), rtt.end()),
               (double) summa / qabul, *max_element(rtt.begin(), rtt.end()));
        double farqlar = 0;
        for (int i = 1; i < qabul; i++) farqlar += abs(rtt[i] - rtt[i - 1]);
        double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;          // mean of consecutive differences
        printf("jitter: %.1f ms\n", jitter);
    }
    return 0;
}
```

Input:

```text
12
15
*
11
30
```

Output:

```text
packets: 5 sent, 4 received, 20.0% lost
RTT: min 11, avg 17.0, max 30 ms
jitter: 8.7 ms
```

20% loss and 8.7 ms of jitter — bad for a cable, typical of weak Wi-Fi. A single 30 ms "spike" noticeably raised both the average and the jitter. When diagnosing, look not only at the average but also at the worst value and the spread.

## Worked example 2: layer-by-layer diagnosis

Each line holds the results of five checks for one computer (`ok` or `fail`) in this order: connection (cable or Wi-Fi), IP address (not 169.254), ping to the gateway, a query by name (DNS), ping to an IP on the Internet. Print the problem layer and the advice from the first failed check; if everything is `ok` — the network is fine.

```python
import sys

TASHXIS = [
    "layer 1: check the cable or Wi-Fi connection",
    "layer 3: no address from DHCP (ipconfig /renew)",
    "layer 3: gateway unreachable (mask, gateway, VLAN)",
    "layer 7: DNS is not working (nslookup, DNS server address)",
    "layer 3: provider or routing problem (tracert)",
]

for raqam, satr in enumerate(sys.stdin.read().strip().split("\n"), 1):
    natija = satr.split()
    xato = next((i for i, n in enumerate(natija) if n == "fail"), -1)   # the first failure
    if xato < 0:
        print(str(raqam) + ": network OK")
    else:
        print(str(raqam) + ": " + TASHXIS[xato])
```
```java
import java.util.Scanner;

public class Main {
    static final String[] TASHXIS = {
        "layer 1: check the cable or Wi-Fi connection",
        "layer 3: no address from DHCP (ipconfig /renew)",
        "layer 3: gateway unreachable (mask, gateway, VLAN)",
        "layer 7: DNS is not working (nslookup, DNS server address)",
        "layer 3: provider or routing problem (tracert)",
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int raqam = 0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            raqam++;
            String[] natija = satr.split(" ");
            int xato = -1;
            for (int i = 0; i < natija.length && xato < 0; i++) {
                if (natija[i].equals("fail")) xato = i;                     // the first failure
            }
            if (xato < 0) {
                System.out.println(raqam + ": network OK");
            } else {
                System.out.println(raqam + ": " + TASHXIS[xato]);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const string TASHXIS[] = {
    "layer 1: check the cable or Wi-Fi connection",
    "layer 3: no address from DHCP (ipconfig /renew)",
    "layer 3: gateway unreachable (mask, gateway, VLAN)",
    "layer 7: DNS is not working (nslookup, DNS server address)",
    "layer 3: provider or routing problem (tracert)",
};

int main() {
    string satr;
    int raqam = 0;
    while (getline(cin, satr)) {
        if (satr.find_first_not_of(" \r") == string::npos) continue;
        raqam++;
        istringstream ss(satr);
        string n;
        int xato = -1;
        for (int i = 0; ss >> n; i++) {
            if (n == "fail" && xato < 0) xato = i;                          // the first failure
        }
        if (xato < 0) {
            cout << raqam << ": network OK" << endl;
        } else {
            cout << raqam << ": " << TASHXIS[xato] << endl;
        }
    }
    return 0;
}
```

Input:

```text
ok ok ok fail ok
ok fail fail fail fail
ok ok ok ok ok
fail fail fail fail fail
ok ok ok ok fail
```

Output:

```text
1: layer 7: DNS is not working (nslookup, DNS server address)
2: layer 3: no address from DHCP (ipconfig /renew)
3: network OK
4: layer 1: check the cable or Wi-Fi connection
5: layer 3: provider or routing problem (tracert)
```

On the fourth computer all five checks failed, but there is a single cause: the cable. That is why we look at the first failure: the upper layers depend on the lower ones, and a lower problem "breaks" everything. The first computer is the classic case: the Internet is reachable by IP, but sites do not open.

:::warning
**Common mistakes**

- **Looking for a complex cause straight away.** The simple things first: the cable, the lights, "what was changed last".
- **Concluding that a host is down because `ping` gets no reply.** A firewall may block ICMP; check the port with `Test-NetConnection`.
- **Changing several things at once.** You cannot tell which change helped (or broke things).
- **Rebooting everything and not writing the problem down.** The problem returns and its cause stays unknown.
- **Looking only at the average RTT.** Loss and jitter spoil the user experience more.
:::

:::tip
Run the "classic sequence" on your own network and put the results in a table. Then run `pathping 8.8.8.8` (or `mtr 8.8.8.8`) and find at which hop delay and loss appear.
:::

## Summary

- Stages: identify → hypothesis → test → fix → verify → document.
- OSI approaches: bottom-up, top-down, divide and conquer.
- Each layer has its own symptoms and tools: lights, `arp`, `ipconfig`, `ping`, `tracert`, `Test-NetConnection`, `nslookup`.
- A ping result is judged by loss, RTT (min/avg/max) and jitter.
- The first failed lower layer is the root cause: the upper ones depend on it.

## Check yourself

:::check
Q: `ping 8.8.8.8` by IP works, but `ping google.com` does not. Which service is at fault?
A: DNS.
Q: A computer got a 169.254.x.x address. What is the likely cause?
A: The DHCP server could not be reached (an APIPA address).
Q: Which Windows utility shows the routers a packet passes through?
A: `tracert`.
Q: Where does the "divide and conquer" approach start?
A: From the middle layer, usually with `ping` (layer 3); depending on the result you move up or down.
Q: The RTTs are 10, 12, 30, 11 ms. What is the jitter?
A: (2 + 18 + 19) / 3 = 13 ms.
:::

:::link https://learn.microsoft.com/en-us/troubleshoot/windows-client/networking/tcpip-and-networking-troubleshooting-overview
TCP/IP troubleshooting
Microsoft's guide to diagnosing network problems.
:::
