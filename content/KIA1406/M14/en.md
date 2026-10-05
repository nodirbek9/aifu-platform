---
title: Network security tools. Firewall, VPN, IDS, DLP, honeypot
summary: Network protection tools — firewalls (packet filter, stateful, application proxy, NGFW, WAF), rule order and default deny, DMZ and segmentation; VPN — site-to-site and remote access, IPSec, SSL/TLS VPN, WireGuard; IDS and IPS — signature-based and anomaly-based detection, network and host systems, Snort, Suricata; SIEM; DLP — data loss prevention; honeypots and deception technology; defense in depth and zero trust.
---

## Why does this topic matter?

No single tool can protect a network completely: a firewall does not see an attack arriving through an allowed port, an IDS sees it but does not stop it, and encryption protects data from eavesdropping but not from leaking out. That is why organizations deploy tools **in layers** — each one closes the gaps of another. In this lesson we look at the purpose, operating principle and limitations of the main tools.

:::note
**After this lesson you will be able to:**

- explain the types of firewalls and how rules are evaluated;
- know the idea of a DMZ and network segmentation;
- name the types of VPN and their use cases;
- distinguish IDS from IPS and signature-based from anomaly-based detection;
- explain the purpose of DLP and honeypots and their place in defense in depth.
:::

## Firewalls

A firewall passes or stops traffic between networks according to a **policy**.

| Type | What it checks | Advantage | Limitation |
|---|---|---|---|
| **Packet filter** (stateless) | the address, port and protocol of each packet | fast, simple | does not know the connection context |
| **Stateful** | a connection table: does a reply packet belong to a requested connection | return traffic is automatic, fake "replies" are stopped | does not see application content |
| **Application proxy** | the content of protocols such as HTTP, SMTP | fine-grained control, content sanitization | slower, separate for each protocol |
| **NGFW** (next generation) | application identification, user, IPS, TLS inspection | many functions in one device | complex, expensive |
| **WAF** | requests to a web application (SQL injection, XSS patterns) | specialized in web attacks | does not replace secure code |
| **Host firewall** | one computer (Windows Defender Firewall, iptables/nftables) | protection inside the internal network too | must be managed on every host |

**Rule order**: rules are evaluated from top to bottom and the **first matching** rule is applied; if none matches — the final **default deny** rule. That is why specific rules must sit above general ones. Not only incoming but also **outgoing** traffic must be filtered — an infected computer tries to connect outward.

**DMZ and segmentation**: servers that must be reachable from outside (web, mail) sit in a separate **DMZ** segment — even if one is compromised, the path to the internal network is closed. The internal network is also divided into **segments** with VLANs and firewalls for departments, servers, users and IoT devices — an incident in one segment does not spread to the whole network.

## VPN

A **VPN** builds an encrypted "tunnel" over an open network (the Internet).

| Type | Use |
|---|---|
| **Site-to-site** | a permanent link between office and branch networks (usually IPSec, M9) |
| **Remote access** | an employee connects to the corporate network from home or while travelling |
| **SSL/TLS VPN** | via a browser or client program, port 443; passes firewalls easily |
| **WireGuard** | modern, small code base, fast; Curve25519 and ChaCha20 |

A VPN protects the channel, but if the connected device itself is infected, the risk enters the network. That is why remote access requires **MFA** (M10), a device health check and least privilege (access only to the needed systems). Because VPN devices are exposed to the Internet, patching them quickly is important. The modern approach is **ZTNA** (zero trust network access): access not to the whole network but to each application separately, verified every time.

## IDS and IPS

| | IDS (detection) | IPS (prevention) |
|---|---|---|
| Placement | listens to a copy of the traffic (SPAN port, TAP) | in the traffic path (inline) |
| Action | raises an alert | drops the packet, closes the connection |
| Cost of an error | a false alert — extra work | a false block — a service outage |

**Detection methods**:

- **Signature-based**: a database of known attack patterns (Snort, Suricata rules). Precise and understandable, but does not see a new (zero-day) attack; the database must be kept up to date.
- **Anomaly-based**: a model of normal behaviour (traffic volume, time, addresses) and detection of deviations from it. Finds new attacks too, but produces many false alerts.
- **Host-based** systems (HIDS, EDR) watch files, processes and logs; **network-based** ones (NIDS) watch traffic. In encrypted traffic a NIDS does not see the content — here host-level monitoring matters.

**SIEM** (Security Information and Event Management) collects the logs of all sources — firewalls, IDS, servers, Active Directory — in one place, links them (correlation) and generates alerts for the security operations centre (SOC).

## DLP and honeypots

**DLP** (Data Loss Prevention) monitors and stops confidential data leaving the organization: email, web uploads, USB, cloud, printing. Data is identified by **patterns** (card numbers, passports, personal ID numbers), keywords, document labels and "fingerprints". Policies: warn, enforce encryption, block. DLP works well against accidental leaks; it does not fully stop a deliberate insider, but it leaves a trail.

A **honeypot** is a fake system (a server, service, document or account) deliberately placed to attract an attacker. Since it has no real users, any access to it is suspicious; false alerts are almost non-existent. Honeypots are used to study attackers' methods and to detect an intrusion into the internal network early; a **honeytoken** is a fake password or file that raises an alarm as soon as it is used. A honeypot must be isolated from the production network.

## Defense in depth and zero trust

| Layer | Tools |
|---|---|
| Perimeter | firewall, NGFW, DDoS protection, mail filter |
| Inside the network | segmentation, NIDS/IPS, honeypots |
| Host | host firewall, EDR, updates, disk encryption |
| Application | WAF, secure code, authentication |
| Data | encryption, DLP, backups |
| Monitoring | logs, SIEM, SOC |

The principle of **zero trust** (NIST SP 800-207): "never trust, always verify" — being inside the network is no reason for trust; every request is checked by user, device and context (the ABAC idea from M11).

## Worked example 1: firewall rules

The first line holds the number of rules *r*, then *r* rules: action (`allow`/`deny`), protocol, source network (CIDR or `any`), destination port (or `any`). The following lines hold packets: protocol, source IP, port. For each packet print the **first matching** rule and its action; if none matches — `deny (default)`.

```python
import sys


def ip_son(ip):                                 # IPv4 address -> 32-bit number
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def tarmoqda(ip, cidr):
    if cidr == "any":
        return True
    manzil, uzunlik = cidr.split("/")
    n = int(uzunlik)
    niqob = (0xFFFFFFFF << (32 - n)) & 0xFFFFFFFF if n else 0   # /24 -> 255.255.255.0
    return ip_son(ip) & niqob == ip_son(manzil) & niqob


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
r = int(satrlar[0][0])
qoidalar = satrlar[1:1 + r]
for proto, ip, port in satrlar[1 + r:]:
    natija = ("deny", "default")                # default deny
    for i, (amal, q_proto, q_tarmoq, q_port) in enumerate(qoidalar, start=1):
        if q_proto in ("any", proto) and tarmoqda(ip, q_tarmoq) and q_port in ("any", port):
            natija = (amal, f"#{i}")
            break                               # first matching rule
    print(f"{proto} {ip} -> {port}: {natija[0]} ({natija[1]})")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long ipSon(String ip) {                         // IPv4 address -> 32-bit number
        String[] q = ip.split("\\.");
        long s = 0;
        for (String x : q) s = (s << 8) | Long.parseLong(x);
        return s;
    }

    static boolean tarmoqda(String ip, String cidr) {
        if (cidr.equals("any")) return true;
        String[] q = cidr.split("/");
        int n = Integer.parseInt(q[1]);
        long niqob = n == 0 ? 0 : (0xFFFFFFFFL << (32 - n)) & 0xFFFFFFFFL;   // /24 -> 255.255.255.0
        return (ipSon(ip) & niqob) == (ipSon(q[0]) & niqob);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = Integer.parseInt(in.next());
        List<String[]> qoidalar = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            qoidalar.add(new String[] {in.next(), in.next(), in.next(), in.next()});
        }
        while (in.hasNext()) {
            String proto = in.next(), ip = in.next(), port = in.next();
            String amal = "deny", qaysi = "default";              // default deny
            for (int i = 0; i < r; i++) {
                String[] q = qoidalar.get(i);
                if ((q[1].equals("any") || q[1].equals(proto)) && tarmoqda(ip, q[2])
                        && (q[3].equals("any") || q[3].equals(port))) {
                    amal = q[0];
                    qaysi = "#" + (i + 1);
                    break;                                       // first matching rule
                }
            }
            System.out.println(proto + " " + ip + " -> " + port + ": " + amal + " (" + qaysi + ")");
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint32_t ipSon(const string& ip) {                         // IPv4 address -> 32-bit number
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return (a << 24) | (b << 16) | (c << 8) | d;
}

bool tarmoqda(const string& ip, const string& cidr) {
    if (cidr == "any") return true;
    size_t p = cidr.find('/');
    int n = stoi(cidr.substr(p + 1));
    uint32_t niqob = n == 0 ? 0 : 0xFFFFFFFFu << (32 - n);   // /24 -> 255.255.255.0
    return (ipSon(ip) & niqob) == (ipSon(cidr.substr(0, p)) & niqob);
}

int main() {
    int r;
    cin >> r;
    vector<vector<string>> qoidalar(r, vector<string>(4));
    for (auto& q : qoidalar) cin >> q[0] >> q[1] >> q[2] >> q[3];
    string proto, ip, port;
    while (cin >> proto >> ip >> port) {
        string amal = "deny", qaysi = "default";                // default deny
        for (int i = 0; i < r; i++) {
            const auto& q = qoidalar[i];
            if ((q[1] == "any" || q[1] == proto) && tarmoqda(ip, q[2]) && (q[3] == "any" || q[3] == port)) {
                amal = q[0];
                qaysi = "#" + to_string(i + 1);
                break;                                           // first matching rule
            }
        }
        cout << proto << " " << ip << " -> " << port << ": " << amal << " (" << qaysi << ")" << endl;
    }
    return 0;
}
```

Input:

```text
5
allow tcp 10.0.0.0/8 22
deny tcp any 22
allow tcp any 443
allow udp 192.168.1.0/24 53
deny any 203.0.113.0/24 any
tcp 10.1.2.3 22
tcp 8.8.8.8 22
tcp 8.8.8.8 443
tcp 203.0.113.9 443
udp 192.168.1.77 53
udp 192.168.2.77 53
tcp 10.0.0.5 3389
```

Output:

```text
tcp 10.1.2.3 -> 22: allow (#1)
tcp 8.8.8.8 -> 22: deny (#2)
tcp 8.8.8.8 -> 443: allow (#3)
tcp 203.0.113.9 -> 443: allow (#3)
udp 192.168.1.77 -> 53: allow (#4)
udp 192.168.2.77 -> 53: deny (default)
tcp 10.0.0.5 -> 3389: deny (default)
```

SSH (port 22) is open only from the internal 10.0.0.0/8 network. But note: the administrator wanted to block the 203.0.113.0/24 network completely, yet this rule is in **5th place** — the earlier rule 3 (443 for everyone) matched first and let 203.0.113.9 through. The right fix is to put the blocking rule at the top of the list. Rule-order mistakes like this are the problem most often found in firewall audits.

## Worked example 2: DLP — detecting card numbers

A DLP system searches outgoing messages for bank card numbers. A card number has 13–19 digits (possibly written with dashes) and passes the **Luhn** checksum: every second digit from the right is doubled (subtract 9 if it exceeds 9), and the sum of all digits must be divisible by 10. If a word is such a number, hide all but its last 4 digits with `*`, and print the number found. (The input lines are short Uzbek messages: "customer card … for payment", "order number …", "card: … expires 12/27", "phone … and card …".)

```python
import sys


def luhn(raqamlar):
    jami = 0
    for i, ch in enumerate(reversed(raqamlar)):
        d = int(ch)
        if i % 2 == 1:                            # every second digit from the right
            d *= 2
            if d > 9:
                d -= 9
        jami += d
    return jami % 10 == 0


topildi = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    sozlar = satr.split(" ")
    for j, soz in enumerate(sozlar):
        raqam = soz.replace("-", "")
        if raqam.isdigit() and 13 <= len(raqam) <= 19 and luhn(raqam):
            sozlar[j] = "*" * (len(raqam) - 4) + raqam[-4:]   # only the last 4 digits remain
            topildi += 1
    print(" ".join(sozlar))
print(f"found: {topildi}")
```
```java
import java.util.Scanner;

public class Main {
    static boolean luhn(String raqamlar) {
        int jami = 0;
        for (int i = 0; i < raqamlar.length(); i++) {
            int d = raqamlar.charAt(raqamlar.length() - 1 - i) - '0';
            if (i % 2 == 1) {                                  // every second digit from the right
                d *= 2;
                if (d > 9) d -= 9;
            }
            jami += d;
        }
        return jami % 10 == 0;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int topildi = 0;
        while (in.hasNextLine()) {
            String satr = in.nextLine();
            if (satr.isBlank()) continue;
            String[] sozlar = satr.split(" ", -1);
            for (int j = 0; j < sozlar.length; j++) {
                String raqam = sozlar[j].replace("-", "");
                if (raqam.matches("[0-9]{13,19}") && luhn(raqam)) {
                    sozlar[j] = "*".repeat(raqam.length() - 4) + raqam.substring(raqam.length() - 4);   // only the last 4 digits remain
                    topildi++;
                }
            }
            System.out.println(String.join(" ", sozlar));
        }
        System.out.println("found: " + topildi);
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

bool luhn(const string& raqamlar) {
    int jami = 0;
    for (size_t i = 0; i < raqamlar.size(); i++) {
        int d = raqamlar[raqamlar.size() - 1 - i] - '0';
        if (i % 2 == 1) {                                        // every second digit from the right
            d *= 2;
            if (d > 9) d -= 9;
        }
        jami += d;
    }
    return jami % 10 == 0;
}

int main() {
    int topildi = 0;
    string satr;
    while (getline(cin, satr)) {
        if (satr.find_first_not_of(" \t\r") == string::npos) continue;
        vector<string> sozlar;
        size_t bosh = 0, p;
        while ((p = satr.find(' ', bosh)) != string::npos) {
            sozlar.push_back(satr.substr(bosh, p - bosh));
            bosh = p + 1;
        }
        sozlar.push_back(satr.substr(bosh));
        for (string& soz : sozlar) {
            string raqam;
            for (char c : soz) if (c != '-') raqam += c;
            bool hammasi = !raqam.empty() && all_of(raqam.begin(), raqam.end(), [](unsigned char c) { return isdigit(c); });
            if (hammasi && raqam.size() >= 13 && raqam.size() <= 19 && luhn(raqam)) {
                soz = string(raqam.size() - 4, '*') + raqam.substr(raqam.size() - 4);   // only the last 4 digits remain
                topildi++;
            }
        }
        for (size_t j = 0; j < sozlar.size(); j++) cout << (j ? " " : "") << sozlar[j];
        cout << endl;
    }
    cout << "found: " << topildi << endl;
    return 0;
}
```

Input:

```text
Mijoz kartasi 4111111111111111 tolov uchun
Buyurtma raqami 1234567812345678
Karta: 5500-0000-0000-0004 muddati 12/27
Telefon 998901234567 va karta 4000056655665556
```

Output:

```text
Mijoz kartasi ************1111 tolov uchun
Buyurtma raqami 1234567812345678
Karta: ************0004 muddati 12/27
Telefon 998901234567 va karta ************5556
found: 3
```

(The examples use official **test** numbers of the payment systems.) The order number has 16 digits but failed the Luhn check — so it is not a card, and DLP left it alone; the phone number is too short. The checksum sharply reduces false alerts. Real DLP systems additionally consider context (nearby words such as "card", "CVV"), text inside files and document labels.

:::warning
**Common mistakes**

- **"We have a firewall — we are protected".** A firewall does not stop attacks through allowed ports, phishing or insider threats.
- **Not checking rule order.** If a broad allow rule sits above, the denials below do not work.
- **Filtering only incoming traffic.** Controlling outgoing traffic stops an infected device from talking to an external server.
- **Nobody looks at IDS alerts.** An unmonitored IDS is useless; alerts must reach the SIEM and a responsible person.
- **Leaving a VPN outdated.** An Internet-facing VPN gateway that is not patched becomes the easiest way into the network.
:::

:::tip
In Windows, the `wf.msc` window shows the inbound and outbound rules of Windows Defender Firewall and their profiles (Domain, Private, Public). In Linux, `sudo nft list ruleset` or `sudo iptables -L -n -v` prints the current rules and the number of packets that matched each one.
:::

## Summary

- Firewalls: packet filter, stateful, proxy, NGFW, WAF, host firewall; first matching rule and default deny; filter outgoing traffic too.
- A DMZ and segmentation limit the spread of an incident.
- VPN: site-to-site and remote access; MFA and patching are required; the modern alternative is ZTNA.
- An IDS detects, an IPS blocks; signature and anomaly methods complement each other; a SIEM correlates logs.
- DLP controls the outflow of confidential data; honeypots and honeytokens detect attackers early.
- Defense in depth and zero trust — layering tools and checking every request.

## Check yourself

:::check
Q: How does a stateful firewall differ from a simple packet filter?
A: It keeps a connection table and passes reply packets only if they belong to an existing connection.
Q: Why is a web server placed in a DMZ rather than in the internal network?
A: Even if the server is compromised, the attacker cannot reach the internal network directly.
Q: What is the main difference between an IDS and an IPS?
A: An IDS only detects and alerts; an IPS sits in the traffic path and stops the attack.
Q: What is the main drawback of signature-based detection?
A: It finds only known attacks; it does not see new (zero-day) attacks.
Q: Why do honeypot alerts have few false signals?
A: It has no real users, so any access to it is suspicious.
:::

:::link https://csrc.nist.gov/pubs/sp/800/41/r1/final
NIST SP 800-41 Rev. 1: Guidelines on Firewalls and Firewall Policy
A guide to firewall types and designing firewall policy.
:::
