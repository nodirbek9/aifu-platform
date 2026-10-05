---
title: Finding network faults, using operating system utilities
summary: Hands-on work with Windows and Linux network utilities — ipconfig, ping, tracert, pathping, nslookup, arp, netstat, route, getmac, Test-NetConnection; interpreting the results; fault scenarios (cable, DHCP, gateway, DNS, port, route); analysing netstat and tracert output with a program; writing a report.
---

## Why it matters

In lecture M15 we learned the troubleshooting methodology. In this session we get our hands dirty: we find faults deliberately created in the lab network using the utilities. In real work the output of these commands may run to hundreds of lines — so we also learn to analyse it with a program: administrators write such scripts to automate their daily checks.

:::note
**After this session you will be able to:**

- use the main network utilities and interpret their results;
- identify typical fault scenarios from their symptoms;
- get the listening ports and connection statistics from `netstat` output;
- find from a `tracert` result where delay appears;
- write a short fault report.
:::

## Quick reference to the utilities

| Task | Windows | Linux |
|---|---|---|
| Address and settings | `ipconfig /all` | `ip addr`, `ip route`, `resolvectl status` |
| Renew the address | `ipconfig /release` + `/renew` | `sudo dhclient -r && sudo dhclient` |
| Connectivity | `ping -n 10 host` | `ping -c 10 host` |
| Path | `tracert host` | `traceroute host` |
| Path + loss | `pathping host` | `mtr host` |
| DNS | `nslookup host`, `ipconfig /flushdns` | `dig host`, `nslookup host` |
| ARP cache | `arp -a`, `arp -d *` | `ip neigh` |
| Connections and ports | `netstat -ano`, `Get-NetTCPConnection` | `ss -tunap` |
| Routing table | `route print` | `ip route` |
| MAC addresses | `getmac /v` | `ip link` |
| TCP port check | `Test-NetConnection host -Port 443` | `nc -zv host 443` |
| Computer name | `hostname` | `hostname` |

## Fault scenarios

The teacher creates the following faults in the lab; for each, fill in a **symptom → check → cause → solution** table.

| № | Symptom | Check | Usual cause |
|---|---|---|---|
| 1 | "Network cable unplugged" | `ipconfig`, the port light | the cable is unplugged or the port is off |
| 2 | address 169.254.x.x | `ipconfig /all`, `/renew` | the DHCP server is down or unreachable |
| 3 | local network works, no Internet | `ping` the gateway, `route print` | the gateway is wrong or missing |
| 4 | `ping 8.8.8.8` works, sites do not | `nslookup` | the DNS server address is wrong |
| 5 | the site does not open, ping works | `Test-NetConnection -Port 443`, `netstat -ano` | the port is closed, the service is down, firewall |
| 6 | two computers cannot see each other | compare the masks in `ipconfig` | different subnets or a wrong mask |
| 7 | "Windows has detected an IP address conflict" | `arp -a`, `ping` | the same static IP on two devices |
| 8 | the Internet is very slow and drops | `pathping`, `ping -n 100` | loss, weak Wi-Fi, a full link |

## Report format

```text
Date and time:      2026-10-05 10:40
Symptom:            sites do not open on PC07, Telegram works
Checks:             ipconfig /all — 192.168.50.107/24, gateway .1, DNS 8.8.4.4
                    ping 8.8.8.8 — 0% loss; nslookup aifu.uz — "timed out"
Cause:              the DNS server address was typed wrongly by hand (8.8.4.44)
Solution:           set DNS to automatic (DHCP) in the IPv4 settings
Result:             nslookup and the browser work
```

## Worked example 1: analysing netstat output

The input holds lines of `netstat -an` output (protocol, local address, foreign address and, for TCP, the state). Print the listening TCP ports (without repeats, in increasing order), the number of TCP connections in each state (in alphabetical order of the state name, excluding `LISTENING`) and the foreign IP with the most connections (the smaller string on a tie).

```python
import sys

tinglash = set()
holatlar = {}
masofaviy = {}
for satr in sys.stdin:
    q = satr.split()
    if len(q) < 4 or q[0] != "TCP":
        continue                                  # header and UDP lines
    port = int(q[1].rsplit(":", 1)[1])
    holat = q[3]
    if holat == "LISTENING":
        tinglash.add(port)
        continue
    holatlar[holat] = holatlar.get(holat, 0) + 1
    ip = q[2].rsplit(":", 1)[0]
    masofaviy[ip] = masofaviy.get(ip, 0) + 1
print("LISTENING: " + " ".join(str(p) for p in sorted(tinglash)))
for holat in sorted(holatlar):
    print(holat + ": " + str(holatlar[holat]))
if masofaviy:
    eng = min(masofaviy, key=lambda ip: (-masofaviy[ip], ip))
    print("busiest server: " + eng + " (" + str(masofaviy[eng]) + ")")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        TreeSet<Integer> tinglash = new TreeSet<>();
        Map<String, Integer> holatlar = new TreeMap<>();
        Map<String, Integer> masofaviy = new TreeMap<>();
        while (in.hasNextLine()) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q.length < 4 || !q[0].equals("TCP")) continue;          // header and UDP lines
            int port = Integer.parseInt(q[1].substring(q[1].lastIndexOf(':') + 1));
            String holat = q[3];
            if (holat.equals("LISTENING")) {
                tinglash.add(port);
                continue;
            }
            holatlar.merge(holat, 1, Integer::sum);
            masofaviy.merge(q[2].substring(0, q[2].lastIndexOf(':')), 1, Integer::sum);
        }
        StringJoiner portlar = new StringJoiner(" ");
        for (int p : tinglash) portlar.add(String.valueOf(p));
        System.out.println("LISTENING: " + portlar);
        for (Map.Entry<String, Integer> e : holatlar.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        String eng = null;
        for (Map.Entry<String, Integer> e : masofaviy.entrySet()) {
            if (eng == null || e.getValue() > masofaviy.get(eng)) eng = e.getKey();
        }
        if (eng != null) System.out.println("busiest server: " + eng + " (" + masofaviy.get(eng) + ")");
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    set<int> tinglash;
    map<string, int> holatlar, masofaviy;
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        vector<string> q;
        string s;
        while (ss >> s) q.push_back(s);
        if (q.size() < 4 || q[0] != "TCP") continue;                    // header and UDP lines
        int port = stoi(q[1].substr(q[1].rfind(':') + 1));
        string holat = q[3];
        if (holat == "LISTENING") {
            tinglash.insert(port);
            continue;
        }
        holatlar[holat]++;
        masofaviy[q[2].substr(0, q[2].rfind(':'))]++;
    }
    string portlar;
    for (int p : tinglash) portlar += (portlar.empty() ? "" : " ") + to_string(p);
    cout << "LISTENING: " << portlar << endl;
    for (auto& e : holatlar) cout << e.first << ": " << e.second << endl;
    string eng;
    for (auto& e : masofaviy) {
        if (eng.empty() || e.second > masofaviy[eng]) eng = e.first;
    }
    if (!eng.empty()) cout << "busiest server: " << eng << " (" << masofaviy[eng] << ")" << endl;
    return 0;
}
```

Input:

```text
  Proto  Local Address          Foreign Address        State
  TCP    0.0.0.0:135            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:445            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:3389           0.0.0.0:0              LISTENING
  TCP    192.168.1.10:51000     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51002     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51005     95.46.98.10:443        ESTABLISHED
  TCP    192.168.1.10:50990     95.46.98.10:443        TIME_WAIT
  TCP    192.168.1.10:51010     185.74.5.10:80         SYN_SENT
  UDP    0.0.0.0:5353           *:*
```

Output:

```text
LISTENING: 135 445 3389
ESTABLISHED: 3
SYN_SENT: 1
TIME_WAIT: 1
busiest server: 142.250.74.46 (2)
```

Interpretation: 3389 (RDP) is listening — if it is not needed, turn the service off or close it in the firewall. `SYN_SENT` — a connection request was sent but no reply came: port 80 of 185.74.5.10 is closed or a firewall is blocking it. Many such entries mean a network or server problem; `SYN_SENT` to many different addresses is a sign of malware scanning.

## Worked example 2: analysing a tracert result

Each line is one hop: its number, three RTTs (ms or `*`) and the router address (`-` — unknown). For each hop print the average RTT (over the replies received only) or `no reply`, then name the hop where delay grew most compared with the previous replying hop.

```python
import sys

oldingi = None
eng_sakrash, eng_oshish = None, -1.0
for satr in sys.stdin.read().strip().split("\n"):
    q = satr.split()
    vaqtlar = [int(x) for x in q[1:4] if x != "*"]
    if not vaqtlar:
        print(q[0] + " " + q[4] + ": no reply")      # the router does not answer ICMP
        continue
    ortacha = sum(vaqtlar) / len(vaqtlar)
    print(f"{q[0]} {q[4]}: {ortacha:.1f} ms")
    if oldingi is not None and ortacha - oldingi > eng_oshish:
        eng_sakrash, eng_oshish = q[0], ortacha - oldingi
    oldingi = ortacha
if eng_sakrash is not None:
    print(f"largest increase at hop {eng_sakrash} (+{eng_oshish:.1f} ms)")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Double oldingi = null;
        String engSakrash = null;
        double engOshish = -1.0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            double summa = 0;
            int soni = 0;
            for (int i = 1; i <= 3; i++) {
                if (!q[i].equals("*")) {
                    summa += Integer.parseInt(q[i]);
                    soni++;
                }
            }
            if (soni == 0) {
                System.out.println(q[0] + " " + q[4] + ": no reply");   // the router does not answer ICMP
                continue;
            }
            double ortacha = summa / soni;
            System.out.println(String.format("%s %s: %.1f ms", q[0], q[4], ortacha));
            if (oldingi != null && ortacha - oldingi > engOshish) {
                engSakrash = q[0];
                engOshish = ortacha - oldingi;
            }
            oldingi = ortacha;
        }
        if (engSakrash != null) {
            System.out.println(String.format("largest increase at hop %s (+%.1f ms)", engSakrash, engOshish));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string satr;
    bool bor = false;
    double oldingi = 0, engOshish = -1.0;
    string engSakrash;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string raqam, t[3], manzil;
        if (!(ss >> raqam >> t[0] >> t[1] >> t[2] >> manzil)) continue;
        double summa = 0;
        int soni = 0;
        for (auto& x : t) {
            if (x != "*") {
                summa += stoi(x);
                soni++;
            }
        }
        if (soni == 0) {
            printf("%s %s: no reply\n", raqam.c_str(), manzil.c_str());   // the router does not answer ICMP
            continue;
        }
        double ortacha = summa / soni;
        printf("%s %s: %.1f ms\n", raqam.c_str(), manzil.c_str(), ortacha);
        if (bor && ortacha - oldingi > engOshish) {
            engSakrash = raqam;
            engOshish = ortacha - oldingi;
        }
        oldingi = ortacha;
        bor = true;
    }
    if (!engSakrash.empty()) printf("largest increase at hop %s (+%.1f ms)\n", engSakrash.c_str(), engOshish);
    return 0;
}
```

Input:

```text
1 1 1 2 192.168.1.1
2 8 9 8 10.10.0.1
3 * * * -
4 12 11 13 195.158.1.1
5 60 * 62 72.14.215.1
6 61 60 59 8.8.8.8
```

Output:

```text
1 192.168.1.1: 1.3 ms
2 10.10.0.1: 8.3 ms
3 -: no reply
4 195.158.1.1: 12.0 ms
5 72.14.215.1: 61.0 ms
6 8.8.8.8: 60.0 ms
largest increase at hop 5 (+49.0 ms)
```

Interpretation: hop 3 did not reply, but the later ones work — this is not a fault, the router is configured not to answer ICMP. From hop 4 to 5 delay grew by ~49 ms — the provider's international link (distance to another country). If a large increase persists in all the following hops, the cause is at that point; if it is in one hop only, the router itself answers ICMP slowly.

:::warning
**Common mistakes**

- **Taking a `*` in `tracert` for a break.** If the following hops reply, the path works — the router just does not answer ICMP.
- **Running `netstat` with `-b` without administrator rights.** Seeing program names needs an administrator; get the PID with `-o` and compare it in Task Manager.
- **Running `arp -d *` or `ipconfig /release` in a remote (RDP) session.** The connection drops and you cannot get back in.
- **Checking without writing the results down.** Without a report, recurring problems are investigated from scratch every time.
:::

:::tip
Run `netstat -ano > netstat.txt` on your computer and feed the file to the program of the first example. For each listening port in the result, find which program it is with `tasklist /fi "PID eq <PID>"` and list the unneeded ones.
:::

## Summary

- The main utilities: `ipconfig`, `ping`, `tracert`/`pathping`, `nslookup`, `arp`, `netstat`, `route`, `Test-NetConnection` (Linux: `ip`, `ss`, `dig`, `mtr`, `nc`).
- For each scenario: symptom → check → cause → solution → result; all of it goes into the report.
- `netstat`: LISTENING — open services, ESTABLISHED — active connections, SYN_SENT — unanswered attempts, TIME_WAIT — closed connections.
- `tracert`: a `*` is not always a break; a sharp and lasting increase in delay points to the problem spot.
- Analysing utility output with a script automates daily checks.

## Check yourself

:::check
Q: Which Windows utility shows the routers a packet passes through to its destination?
A: `tracert`.
Q: Which Windows command shows a computer's IP address, mask and gateway?
A: `ipconfig` (in full — `ipconfig /all`).
Q: What may many `SYN_SENT` states in `netstat` mean?
A: Connection requests get no reply (a closed port, a firewall), or malware is scanning.
Q: All three results of one middle hop in `tracert` are `*`, while the later ones are normal. What does this mean?
A: That router is configured not to answer ICMP; the path works.
Q: Which command checks whether a TCP port is open?
A: `Test-NetConnection host -Port N` (`nc -zv host N` on Linux).
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/windows-commands
Windows commands
The full reference of Windows command-line utilities.
:::
