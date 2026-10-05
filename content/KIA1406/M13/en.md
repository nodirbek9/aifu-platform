---
title: Network security. The OSI and TCP/IP models, vulnerabilities and threats, DoS attacks
summary: The tasks of network security; the OSI and TCP/IP models from a security point of view — threat categories and protective measures at each layer; the concepts of vulnerability, threat and risk; the main principles of network security (segmentation, minimal exposure, encryption, monitoring); denial of service (DoS) and distributed DoS (DDoS) — the concept, categories, consequences, botnets; protection against DoS — preparation, detection, response; rate limiting and a program showing it; legal aspects.
---

## Why does this topic matter?

Almost all systems — banking, public services, trade — work over networks. Network security protects three properties: the **confidentiality** of data in transit, its **integrity** and the **availability** of services (the CIA triad). In this lecture we look at which threat categories exist at the layers of the network models, which protection is applied against them, and how to protect against DoS incidents aimed at taking services down. The next lecture (M14) looks at the protection tools — firewalls, VPNs, IDS — in detail.

:::note
**After this lesson you will be able to:**

- explain the OSI and TCP/IP layers from a security point of view;
- name the threat categories and protective measures at each layer;
- distinguish the concepts of vulnerability, threat and risk;
- know the essence, consequences and protection methods of DoS and DDoS;
- show a rate-limiting rule in a program.
:::

## The OSI and TCP/IP models: a security point of view

Network tasks are divided into layers (OSI — 7 layers, TCP/IP — 4). This matters for security: each layer has its own threats and its own protection tools, and protection at one layer does not replace another.

| OSI layer | TCP/IP | Threat category (meaning) | Protective measures |
|---|---|---|---|
| **1. Physical** | Link | physical access to cables or equipment, cutting, theft | physical security (M12), locked server rooms, protected cabling |
| **2. Data link** | Link | redirecting traffic to another device on the local network by forging addresses; connecting an unauthorized device | port security on switches, 802.1X authentication, ARP inspection, segmentation with VLANs |
| **3. Network** | Internet | forging IP addresses, announcing routes incorrectly | ingress and egress filtering (BCP 38), route validation (RPKI), firewalls |
| **4. Transport** | Transport | exhausting connection resources, interfering with sessions | stateful firewalls, connection limits, operating system protection settings, TLS |
| **5–7. Session, presentation, application** | Application | vulnerable web applications, phishing, malicious files, DNS spoofing, plain-text protocols | HTTPS/TLS, a WAF, input validation, DNSSEC, secure protocols (A13), staff training |

**The key conclusion**: an attacker chooses the weakest layer. So protection must be **in depth** (at several layers) — the defence-in-depth principle from M14.

## Vulnerability, threat and risk

| Concept | Definition | Example |
|---|---|---|
| **Vulnerability** | a weakness in a system | unpatched router software, a default password, Telnet left open |
| **Threat** | an event or source that could exploit a vulnerability | malware, an intruder, an employee error, a natural disaster |
| **Risk** | the likelihood of a threat exploiting a vulnerability × the damage | the risk matrix in A2 |

Vulnerabilities are registered in public databases (**CVE**) and rated by severity (**CVSS**). An organization regularly checks its own systems (with an authorized vulnerability scanner), installs updates and takes extra measures for vulnerabilities that cannot be fixed.

## Principles of network security

1. **Segmentation** — dividing the network into zones (users, servers, IoT, DMZ); only the necessary traffic between zones (M14, M15).
2. **Minimal exposure** — only the necessary services and ports are open; everything else is "denied by default".
3. **Encryption** — confidential data crosses the network only encrypted (TLS, VPN — A14).
4. **Authentication** — devices and users are verified before they join the network (802.1X, MFA).
5. **Monitoring** — observing traffic and logs, detecting anomalies (A13, IDS and SIEM — M14).
6. **Updates and configuration** — network equipment software is up to date and default passwords are changed.

## Denial of service (DoS and DDoS)

**DoS** (Denial of Service) is an action aimed at making a system or service **unavailable** to legitimate users. It targets the **availability** property of the CIA triad. **DDoS** (Distributed DoS) is carried out from many sources at once, often from a **botnet** — a network of compromised computers and IoT devices (Mirai in M15); that is why blocking a single address is not enough.

**Categories** (by which resource is exhausted):

| Category | What it targets | Main protection |
|---|---|---|
| **Volume-based** | filling the bandwidth of the network link | the provider and specialized DDoS protection services, a CDN, spare link capacity |
| **Protocol resources** | filling the connection tables of servers, firewalls or load balancers | operating system and firewall protection settings, connection limits |
| **Application level** | keeping an application busy by calling expensive operations (search, the sign-in page) many times | rate limiting, caching, a WAF, bot checks |

**Consequences**: the service stops and revenue is lost, customers are dissatisfied, there are recovery costs and reputational damage. DoS is sometimes also used to distract attention from another action — so other logs are watched during an incident too.

**A historical example**: in 2016 the Mirai botnet (M15) carried out a DDoS attack against a major DNS provider, and as a result many popular sites could not be opened by some users for several hours. The lesson: dependence on a single external service (DNS) is also an availability risk; backup providers and redundant services are important.

## Protection against DoS

| Stage | Measures |
|---|---|
| **Preparation** | spare link and server capacity; distributing services across several locations (CDN, anycast); a contract with a DDoS protection service; an agreed contact procedure with the Internet provider; backup DNS for critical services |
| **Detection** | knowing the normal level of traffic (a baseline), monitoring and alerts (A13), application latency and error counts |
| **Response** | a plan written in advance (a playbook): who does what, informing the provider, redirecting traffic to a scrubbing service, switching on the application's protection mode, informing customers |
| **Recovery and lessons** | restoring the service, analysing the incident, updating the plan |

**Application-level measures**: rate limiting, caching results, WAF rules, automatic scaling (M11, in the cloud — but with cost control), queues and limits for expensive operations.

**The "good neighbour" principle**: an organization makes sure that no packets with forged source addresses leave its network (BCP 38 filtering) and protects its own devices from becoming part of a botnet (the IoT audit in M15). This reduces the likelihood of DDoS against others.

## Worked example: rate limiting

**Rate limiting** means limiting the number of requests allowed from one address within a time window. The first line holds the limit and the window: `N W` (there must be no more than N requests within W seconds). The following lines hold requests sorted by time: the second and the IP address. For each address, output the number of requests and when the limit was first exceeded (a sliding window: `(t − W, t]`).

```python
import sys
from collections import deque

satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
limit, oyna = map(int, satrlar[0].split())

vaqtlar, soni, birinchi, tartib = {}, {}, {}, []
for satr in satrlar[1:]:
    t, ip = satr.split()
    t = int(t)
    if ip not in vaqtlar:
        vaqtlar[ip] = deque()
        soni[ip] = 0
        tartib.append(ip)
    q = vaqtlar[ip]
    q.append(t)
    while q[0] <= t - oyna:                        # drop requests that have left the window
        q.popleft()
    soni[ip] += 1
    if len(q) > limit and ip not in birinchi:      # the limit is exceeded for the first time
        birinchi[ip] = t

for ip in tartib:
    holat = f"limit exceeded (t={birinchi[ip]})" if ip in birinchi else "within limit"
    print(f"{ip}: {soni[ip]} requests, {holat}")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int limit = in.nextInt(), oyna = in.nextInt();
        Map<String, ArrayDeque<Integer>> vaqtlar = new HashMap<>();
        Map<String, Integer> soni = new HashMap<>(), birinchi = new HashMap<>();
        List<String> tartib = new ArrayList<>();
        while (in.hasNext()) {
            int t = in.nextInt();
            String ip = in.next();
            if (!vaqtlar.containsKey(ip)) {
                vaqtlar.put(ip, new ArrayDeque<>());
                soni.put(ip, 0);
                tartib.add(ip);
            }
            ArrayDeque<Integer> q = vaqtlar.get(ip);
            q.addLast(t);
            while (q.peekFirst() <= t - oyna) q.pollFirst();        // drop requests that have left the window
            soni.merge(ip, 1, Integer::sum);
            if (q.size() > limit && !birinchi.containsKey(ip)) birinchi.put(ip, t);   // the limit is exceeded for the first time
        }
        for (String ip : tartib) {
            String holat = birinchi.containsKey(ip) ? "limit exceeded (t=" + birinchi.get(ip) + ")" : "within limit";
            System.out.println(ip + ": " + soni.get(ip) + " requests, " + holat);
        }
    }
}
```
```cpp
#include <deque>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int main() {
    int limit, oyna, t;
    cin >> limit >> oyna;
    map<string, deque<int>> vaqtlar;
    map<string, int> soni, birinchi;
    vector<string> tartib;
    string ip;
    while (cin >> t >> ip) {
        if (!vaqtlar.count(ip)) tartib.push_back(ip);
        deque<int>& q = vaqtlar[ip];
        q.push_back(t);
        while (q.front() <= t - oyna) q.pop_front();               // drop requests that have left the window
        soni[ip]++;
        if ((int) q.size() > limit && !birinchi.count(ip)) birinchi[ip] = t;   // the limit is exceeded for the first time
    }
    for (auto& x : tartib) {
        string holat = birinchi.count(x) ? "limit exceeded (t=" + to_string(birinchi[x]) + ")" : "within limit";
        cout << x << ": " << soni[x] << " requests, " << holat << "\n";
    }
    return 0;
}
```

Input:

```text
5 10
1 10.0.0.5
2 10.0.0.7
2 10.0.0.5
3 10.0.0.5
4 10.0.0.5
5 10.0.0.5
6 10.0.0.5
8 10.0.0.7
12 10.0.0.9
13 10.0.0.9
14 10.0.0.9
15 10.0.0.5
20 10.0.0.7
```

Output:

```text
10.0.0.5: 7 requests, limit exceeded (t=6)
10.0.0.7: 3 requests, within limit
10.0.0.9: 3 requests, within limit
```

At second 6, `10.0.0.5` sent its sixth request within a 10-second window — the limit (5) was exceeded. In a real system, from that moment this address's requests are temporarily rejected or slowed down (an HTTP 429 response), while other users continue to work normally. In a DDoS there are many sources, so this measure alone is not enough — it is used together with the preparation and provider-level protection above.

## Legal aspects

Organizing DoS and DDoS attacks, creating or using botnets, and ordering "DDoS services" are acts that carry criminal liability in many countries, including Uzbekistan (A3). An organization checks its own protection only with authorized tests, agreed with its provider and protection service.

:::warning
**Common mistakes**

- **Protecting only one layer.** For example, a firewall alone does not stop application-level threats.
- **Being unprepared for DoS.** Looking for the provider's contact only once an incident has started wastes time.
- **Not knowing the normal level of traffic.** Without a baseline, an anomaly cannot be detected.
- **Dependence on a single external service (DNS, hosting).** Backup options are needed.
- **Ignoring the possibility of your own devices becoming part of a botnet.** Audit IoT devices and old servers.
:::

:::tip
Draw up a one-page DoS response plan for the availability of your organization's (or university's) website: responsible people, provider contacts, alert criteria, actions in the first 30 minutes.
:::

## Summary

- Network security protects confidentiality, integrity and availability; each OSI layer has its own threats and protection.
- A vulnerability is a weakness, a threat is a source that could exploit it, risk is likelihood × damage.
- Principles: segmentation, minimal exposure, encryption, authentication, monitoring, updates.
- DoS/DDoS targets availability; the categories are volume-based, protocol resources and application level; DDoS often comes from botnets.
- Protection: preparation (capacity, CDN, a DDoS service, a plan), detection (a baseline, monitoring), response and recovery; in the application — rate limiting, caching, a WAF.

## Check yourself

:::check
Q: Which property of the CIA triad does a DoS attack target?
A: Availability.
Q: What is the main difference between DDoS and DoS?
A: It is carried out from many sources at once (often a botnet), so blocking a single address is not enough.
Q: What is rate limiting?
A: Limiting the number of requests allowed from one address within a time window.
Q: Why must network protection be at several layers?
A: Each layer has its own threats, and protection at one layer does not replace another.
:::

:::link https://www.cisa.gov/resources-tools/resources/understanding-and-responding-distributed-denial-service-attacks
CISA: Understanding and Responding to DDoS Attacks
An official guide to preparing for and responding to DDoS.
:::
