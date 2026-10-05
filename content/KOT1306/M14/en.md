---
title: Security problems in networks. Methods of ensuring security (Firewall, VPN, IDS)
summary: The information security triad (confidentiality, integrity, availability); network threats — scanning, sniffing, spoofing, MITM, DoS/DDoS, password guessing, phishing, malware; protection methods — firewalls (packet filter, stateful, NGFW, WAF), DMZ, VPN (IPsec, SSL/TLS, WireGuard), IDS/IPS (signatures and anomalies), encryption, authentication and MFA; defence in depth; detecting scans and password guessing.
---

## Why it matters

Any server connected to the Internet is scanned automatically within minutes: someone is looking for open ports, old software and simple passwords. A university network stores student data, grades and financial documents. One port left open or a "123456" password puts the whole network at risk. A network engineer must know how attacks work and what layers of protection are built against them.

:::note
**After this lesson you will be able to:**

- explain the three basic requirements of information security;
- name the main network threats and kinds of attack;
- explain the kinds of firewall and the purpose of a DMZ and a VPN;
- know the difference between IDS and IPS, and the signature and anomaly methods;
- detect scanning and password-guessing attacks from log records.
:::

## The security triad (CIA)

| Requirement | Meaning | Violation | Protection |
|---|---|---|---|
| **Confidentiality** | only authorised people see the data | sniffing, leaks | encryption, access control |
| **Integrity** | data does not change without authorisation | forgery, MITM | hashes, digital signatures, MACs |
| **Availability** | the service works when needed | DoS/DDoS, failures | redundancy, filtering, CDN |

## Network threats

| Attack | Content | Layer |
|---|---|---|
| **Scanning** (port scan) | finding open ports and service versions (nmap) — preparing an attack | 3–4 |
| **Sniffing** | getting passwords and data from unencrypted traffic | 2–7 |
| **ARP/DNS spoofing** | redirecting traffic to oneself with fake replies | 2, 7 |
| **MITM** (man in the middle) | getting between two parties to read and change traffic | 2–7 |
| **DoS / DDoS** | knocking a service out by flooding it with requests (SYN flood, UDP flood, HTTP flood); DDoS — from thousands of infected devices (a botnet) | 3–7 |
| **Password guessing** (brute force, dictionary attack) | trying passwords one after another on SSH, RDP, web logins | 7 |
| **Phishing** | tricking a user out of a password with a fake mail or site | social engineering |
| **Malware** | viruses, worms, trojans, ransomware | 7 |
| **Exploiting vulnerabilities** | using a bug in unpatched software (for example, SMB 1 — WannaCry) | 7 |

## Firewalls

A **firewall** is a system that passes or blocks traffic between networks according to rules.

| Kind | What it looks at | Feature |
|---|---|---|
| **Packet filter** (stateless) | each packet's IP, port, protocol separately | fast, simple; reply packets need separate rules |
| **Stateful** | keeps a table of connections | automatically passes replies to connections started from inside |
| **Application-level** (proxy, **WAF**) | the content of HTTP requests | blocks attacks such as SQL injection and XSS |
| **Next generation** (NGFW) | applications, users, IPS, URL filtering | all in one device |

Rules are checked from top to bottom and the **first matching** rule applies; at the end there is the **default policy** "deny everything" (default deny). A program that checks such a rule list is the task of practical session A14.

**DMZ** (demilitarised zone): servers that must be reachable from the Internet (web, mail) are put in a segment separate from the internal network. Even if the web server is broken into, the attacker cannot pass directly to the internal network.

```text
Internet ── [Firewall] ──┬── DMZ: web server, mail server
                         └── [Internal firewall] ── internal network: AD, file server, computers
```

## VPN

A **VPN** (Virtual Private Network) is an **encrypted tunnel** over an open network (the Internet). It is used in two ways: **site-to-site** (joining branches) and **remote access** (connecting an employee at home to the office network).

| Technology | Layer | Feature |
|---|---|---|
| **IPsec** | 3 | AH (integrity), ESP (encryption), IKE (key exchange); the standard for site-to-site |
| **SSL/TLS VPN** (OpenVPN, AnyConnect) | 4–7 | over port 443, passes firewalls easily |
| **WireGuard** | 3 | modern, simple and fast, a small code base |

A VPN encrypts traffic, but if the connected device is infected, the attacker gets "inside" too — so VPN + MFA + device checks are needed.

## IDS and IPS

| | **IDS** (Intrusion Detection System) | **IPS** (Intrusion Prevention System) |
|---|---|---|
| Placement | analyses a copy of the traffic (SPAN) | sits in the traffic path (inline) |
| Action | alerts, writes to a log | blocks a suspicious packet |
| Risk | the attack gets through | a false alarm blocks legitimate traffic |

Detection methods: **signature-based** (a database of known attack patterns — Snort, Suricata rules; does not see a new attack) and **anomaly-based** (deviation from normal behaviour — for example, one address connects to many ports in a short time; finds new attacks too, but has more false alarms). Network IDS (NIDS) and host IDS (HIDS) are distinguished. A system that analyses logs centrally is a **SIEM**.

## Other protective measures

- **Encryption**: HTTPS, SSH, SMB 3, WPA3 — confidentiality and integrity.
- **Strong authentication**: long passwords, **MFA** (two-step verification), key-based SSH.
- **Updates** and turning off unneeded services — shrinking the attack surface.
- **Segmentation** (VLANs, DMZ) — if one part is broken, the whole network is not.
- **Backups** (the 3-2-1 rule) — against ransomware.
- **Defence in depth**: several independent layers of protection — if one is passed, the next catches it.

## Worked example 1: detecting a port scan

The first line holds the threshold *N* (number of distinct ports) and the window *W* (seconds). The following lines are a log of connection attempts: time, source IP and port (in increasing time order). If one source has connected to at least *N* **distinct** ports within the last *W* seconds (attempts with time greater than *t* − *W*), print an alert the first time this is detected. Input is read until the end of the file.

```python
import sys

satrlar = sys.stdin.read().split("\n")
n, w = map(int, satrlar[0].split())
tarix = {}                                      # IP -> [(time, port), ...]
aniqlangan = set()
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    t, ip, port = satr.split()
    t = int(t)
    yozuvlar = [(v, p) for v, p in tarix.get(ip, []) if v > t - w]   # only those in the window
    yozuvlar.append((t, port))
    tarix[ip] = yozuvlar
    portlar = {p for v, p in yozuvlar}
    if len(portlar) >= n and ip not in aniqlangan:
        aniqlangan.add(ip)
        print(str(t) + " " + ip + ": port scan (" + str(len(portlar)) + " ports)")
print("suspicious addresses: " + str(len(aniqlangan)))
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int w = in.nextInt();
        Map<String, List<long[]>> tarix = new HashMap<>();           // IP -> [(time, port), ...]
        Set<String> aniqlangan = new HashSet<>();
        while (in.hasNextLong()) {
            long t = in.nextLong();
            String ip = in.next();
            long port = in.nextLong();
            List<long[]> yozuvlar = tarix.computeIfAbsent(ip, k -> new ArrayList<>());
            yozuvlar.removeIf(e -> e[0] <= t - w);                      // only those in the window
            yozuvlar.add(new long[] {t, port});
            Set<Long> portlar = new HashSet<>();
            for (long[] e : yozuvlar) portlar.add(e[1]);
            if (portlar.size() >= n && !aniqlangan.contains(ip)) {
                aniqlangan.add(ip);
                System.out.println(t + " " + ip + ": port scan (" + portlar.size() + " ports)");
            }
        }
        System.out.println("suspicious addresses: " + aniqlangan.size());
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, w;
    cin >> n >> w;
    map<string, vector<pair<long long, long long>>> tarix;          // IP -> [(time, port), ...]
    set<string> aniqlangan;
    long long t, port;
    string ip;
    while (cin >> t >> ip >> port) {
        vector<pair<long long, long long>> yozuvlar;
        for (auto& e : tarix[ip]) {
            if (e.first > t - w) yozuvlar.push_back(e);                 // only those in the window
        }
        yozuvlar.push_back({t, port});
        tarix[ip] = yozuvlar;
        set<long long> portlar;
        for (auto& e : yozuvlar) portlar.insert(e.second);
        if ((int) portlar.size() >= n && !aniqlangan.count(ip)) {
            aniqlangan.insert(ip);
            cout << t << " " << ip << ": port scan (" << portlar.size() << " ports)" << endl;
        }
    }
    cout << "suspicious addresses: " << aniqlangan.size() << endl;
    return 0;
}
```

Input (threshold: 4 distinct ports within 10 seconds):

```text
4 10
1 10.0.0.5 22
2 10.0.0.5 23
2 192.168.1.20 443
3 10.0.0.5 80
4 192.168.1.20 443
5 10.0.0.5 443
6 10.0.0.9 22
20 10.0.0.9 80
31 10.0.0.9 443
42 10.0.0.9 3389
```

Output:

```text
5 10.0.0.5: port scan (4 ports)
suspicious addresses: 1
```

10.0.0.5 tried 4 distinct ports in five seconds — a typical scan. 192.168.1.20 connects to the same port again and again — an ordinary user. 10.0.0.9 also tried 4 ports, but one every 10–11 seconds — they do not fit in the window. This is a "slow scan", a way of fooling IDSes; choosing the window and threshold is therefore a setting that needs care.

## Worked example 2: blocking against password guessing

The first line holds the threshold *L* (failed attempts), the window *W* and the block duration *B* (seconds). The following lines are a login log: time, IP and result (`OK` or `FAIL`). Attempts from a blocked address are rejected. When the failed attempts within the last *W* seconds reach *L*, the address is blocked for *B* seconds (this is the idea of the `fail2ban` program).

```python
import sys

satrlar = sys.stdin.read().split("\n")
chegara, oyna, muddat = map(int, satrlar[0].split())
xatolar = {}                                    # IP -> times of failed attempts
blok = {}                                       # IP -> time the block ends
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    t, ip, natija = satr.split()
    t = int(t)
    if t < blok.get(ip, -1):
        print(str(t) + " " + ip + ": rejected (blocked)")
        continue
    if natija == "OK":
        print(str(t) + " " + ip + ": logged in")
        continue
    vaqtlar = [v for v in xatolar.get(ip, []) if v > t - oyna] + [t]
    if len(vaqtlar) >= chegara:
        blok[ip] = t + muddat
        xatolar[ip] = []
        print(str(t) + " " + ip + ": blocked for " + str(muddat) + " s")
    else:
        xatolar[ip] = vaqtlar
        print(str(t) + " " + ip + ": wrong password (" + str(len(vaqtlar)) + "/" + str(chegara) + ")")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int chegara = in.nextInt();
        int oyna = in.nextInt();
        int muddat = in.nextInt();
        Map<String, List<Long>> xatolar = new HashMap<>();            // IP -> times of failed attempts
        Map<String, Long> blok = new HashMap<>();                     // IP -> time the block ends
        while (in.hasNextLong()) {
            long t = in.nextLong();
            String ip = in.next();
            String natija = in.next();
            if (t < blok.getOrDefault(ip, -1L)) {
                System.out.println(t + " " + ip + ": rejected (blocked)");
                continue;
            }
            if (natija.equals("OK")) {
                System.out.println(t + " " + ip + ": logged in");
                continue;
            }
            List<Long> vaqtlar = new ArrayList<>();
            for (long v : xatolar.getOrDefault(ip, new ArrayList<>())) {
                if (v > t - oyna) vaqtlar.add(v);
            }
            vaqtlar.add(t);
            if (vaqtlar.size() >= chegara) {
                blok.put(ip, t + muddat);
                xatolar.put(ip, new ArrayList<>());
                System.out.println(t + " " + ip + ": blocked for " + muddat + " s");
            } else {
                xatolar.put(ip, vaqtlar);
                System.out.println(t + " " + ip + ": wrong password (" + vaqtlar.size() + "/" + chegara + ")");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int main() {
    int chegara, oyna, muddat;
    cin >> chegara >> oyna >> muddat;
    map<string, vector<long long>> xatolar;                          // IP -> times of failed attempts
    map<string, long long> blok;                                     // IP -> time the block ends
    long long t;
    string ip, natija;
    while (cin >> t >> ip >> natija) {
        if (blok.count(ip) && t < blok[ip]) {
            cout << t << " " << ip << ": rejected (blocked)" << endl;
            continue;
        }
        if (natija == "OK") {
            cout << t << " " << ip << ": logged in" << endl;
            continue;
        }
        vector<long long> vaqtlar;
        for (long long v : xatolar[ip]) {
            if (v > t - oyna) vaqtlar.push_back(v);
        }
        vaqtlar.push_back(t);
        if ((int) vaqtlar.size() >= chegara) {
            blok[ip] = t + muddat;
            xatolar[ip].clear();
            cout << t << " " << ip << ": blocked for " << muddat << " s" << endl;
        } else {
            xatolar[ip] = vaqtlar;
            cout << t << " " << ip << ": wrong password (" << vaqtlar.size() << "/" << chegara << ")" << endl;
        }
    }
    return 0;
}
```

Input (3 failures within 60 seconds → a 300-second block):

```text
3 60 300
10 203.0.113.7 FAIL
12 203.0.113.7 FAIL
13 192.168.1.20 FAIL
14 203.0.113.7 FAIL
20 203.0.113.7 OK
50 192.168.1.20 OK
320 203.0.113.7 FAIL
```

Output:

```text
10 203.0.113.7: wrong password (1/3)
12 203.0.113.7: wrong password (2/3)
13 192.168.1.20: wrong password (1/3)
14 203.0.113.7: blocked for 300 s
20 203.0.113.7: rejected (blocked)
50 192.168.1.20: logged in
320 203.0.113.7: wrong password (1/3)
```

At second 20 even a correct password was rejected — the block had not ended. This cuts an attacker's guessing rate from dozens of attempts a second to 3 in 5 minutes, while a user who mistyped the password once (192.168.1.20) logged in without trouble.

:::warning
**Common mistakes**

- **Thinking "there is a firewall, so we are safe".** An ordinary firewall does not see an attack that comes through an allowed port (for example, a web application vulnerability).
- **An "allow everything" default policy.** The right approach is to deny everything and open only what is needed.
- **Nobody looking at IDS alerts.** Without a response process an IDS is useless; logs are analysed in a SIEM.
- **Thinking a VPN is complete protection.** A VPN encrypts the tunnel but does not stop an infected device or a stolen password — MFA is needed.
- **Leaving RDP (3389) or SSH (22) open to the Internet with a password.** They come under password-guessing attack within hours; hide them behind a VPN, use keys or MFA.
:::

:::tip
On **your own** computer or a lab VM only, find the open ports with `nmap -sV 127.0.0.1` (scanning other networks without permission is against the law). On Windows compare with `netstat -ano | findstr LISTENING` which program listens on which port, and turn off unneeded services.
:::

## Summary

- Security means confidentiality, integrity and availability (CIA).
- Threats: scanning, sniffing, spoofing, MITM, DoS/DDoS, password guessing, phishing, malware, vulnerabilities.
- Firewalls: packet filter, stateful, WAF, NGFW; the first matching rule and default deny; the DMZ.
- VPN: IPsec, SSL/TLS, WireGuard — an encrypted tunnel over an open network.
- IDS detects, IPS blocks; signature and anomaly methods; defence in depth — several layers.

## Check yourself

:::check
Q: Which attack violates the "availability" requirement of the CIA triad?
A: DoS/DDoS — the service stops working for its users.
Q: What is the advantage of a stateful firewall over a plain packet filter?
A: It keeps a table of connections and automatically passes the replies to connections started from inside.
Q: What is a DMZ for?
A: To separate servers reachable from the Internet from the internal network — even if they are broken into, the internal network stays protected.
Q: What is the main difference between IDS and IPS?
A: An IDS detects an attack and alerts; an IPS sits in the traffic path and blocks it.
Q: What is the drawback of signature-based detection?
A: It does not see new (zero-day) attacks whose pattern is not in the database.
:::

:::link https://owasp.org/www-project-top-ten/
OWASP Top 10
The list of the most common security vulnerabilities in web applications.
:::
