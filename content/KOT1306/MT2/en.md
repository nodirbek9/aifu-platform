---
title: Doing independent work and homework
summary: Kinds of independent work — an essay, a Packet Tracer project, a lab report, a programming task; a list of topics; the stages and documents of a network project (diagram, address plan, device list, test results); grading criteria; a sample task — finding IP and MAC conflicts in a network inventory.
---

## Why it matters

A network engineer's job is not only plugging in cables: it is drawing up a design, planning addresses, testing the result and **documenting** it. Another engineer quickly understands a well-documented network; in an undocumented one every fault turns into a "detective investigation". Independent work develops exactly these skills and makes up an important part of the final grade.

:::note
**After this independent work you will be able to:**

- choose a kind of independent work and do it according to the requirements;
- prepare a small network project with a diagram, an address plan and tests;
- know the grading criteria and check your own work before handing it in;
- find mistakes in a network inventory with a program.
:::

## Kinds of independent work

| Kind | Size | Result |
|---|---|---|
| **Essay** | 8–12 pages | an analytical text on the topic, at least 5 sources, a conclusion |
| **Packet Tracer project** | 1 `.pkt` file + a 3–5 page report | a working network and its documentation |
| **Lab report** | 2–4 pages | the steps of a practical session, screenshots, a conclusion |
| **Programming task** | a program in Python, Java or C++ | a solution that passes the platform's automatic tests |

### Essay topics (examples)

1. The move to IPv6: reasons, mechanisms (dual stack, tunnels) and the situation in Uzbekistan.
2. Comparing 5G and Wi-Fi 7.
3. SDN (software-defined networking) and OpenFlow.
4. DDoS attacks and ways of defending against them.
5. Data centre network architecture (spine-leaf).
6. VPN technologies: comparing IPsec, WireGuard and OpenVPN.
7. Internet of Things (IoT) network protocols: MQTT, CoAP, Zigbee.
8. DNS security: DNSSEC, DoH, DoT.

## The Packet Tracer project

**A sample task**: a three-storey faculty building — on every floor a computer lab (24 PCs), a teachers' room (8 PCs), shared Wi-Fi; a server room (DHCP, DNS, web server); Internet access.

Stages:

1. **Analyse the requirements** — numbers of devices, services, security requirements.
2. **Logical diagram** — the topology (star or tree), devices and links.
3. **Address plan** — subnets with VLSM, gateways, the servers' static addresses.
4. **Configuration** — routers, switches, DHCP, DNS, static routes.
5. **Testing** — `ping` and `tracert` from every subnet to the servers and the Internet, opening the web page.
6. **Documentation** — a report following the template below.

**The report template**:

```text
1. Task and requirements
2. Network diagram (picture)
3. Address plan: subnet | prefix | gateway | DHCP range | purpose
4. Device list: name | model | interfaces and addresses
5. Key configuration (router and switch commands)
6. Test results: check | expected result | actual result
7. Conclusion and suggestions for improvement
```

## Grading criteria

| Criterion | Share |
|---|---|
| Meeting the task requirements (the network works, services are configured) | 40% |
| Correctness and economy of the address plan | 20% |
| Tests and their results | 15% |
| Quality of documentation (diagram, tables, clear language) | 15% |
| Independence and defence (answering questions) | 10% |

Work handed in after the deadline is accepted but marked "late". Copying another student's work, or handing in AI-written text unchanged, breaks the rules of academic honesty.

## Sample task: finding conflicts in an inventory

Network documentation records a name, an IP and a MAC address for every device. If two devices are given the same IP by mistake — an **IP conflict**; the same MAC — a cloned virtual machine or an error in the documents. The program takes the number of devices on the first line and "name IP MAC" on the following lines, and for every repeated IP and MAC prints the devices using it (addresses and names in alphabetical order).

```python
n = int(input())
ip_lar = {}
mac_lar = {}
for _ in range(n):
    nom, ip, mac = input().split()
    ip_lar.setdefault(ip, []).append(nom)
    mac_lar.setdefault(mac.upper(), []).append(nom)   # MAC is case-insensitive
topildi = False
for sarlavha, jadval in (("IP conflict", ip_lar), ("duplicate MAC", mac_lar)):
    for manzil in sorted(jadval):
        if len(jadval[manzil]) > 1:
            topildi = True
            print(sarlavha + ": " + manzil + " -> " + ", ".join(sorted(jadval[manzil])))
if not topildi:
    print("no conflicts")
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static boolean chiqar(String sarlavha, Map<String, List<String>> jadval) {
        boolean topildi = false;
        for (Map.Entry<String, List<String>> e : jadval.entrySet()) {
            if (e.getValue().size() > 1) {
                topildi = true;
                List<String> nomlar = new ArrayList<>(e.getValue());
                Collections.sort(nomlar);
                System.out.println(sarlavha + ": " + e.getKey() + " -> " + String.join(", ", nomlar));
            }
        }
        return topildi;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Map<String, List<String>> ipLar = new TreeMap<>();
        Map<String, List<String>> macLar = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            String nom = in.next(), ip = in.next(), mac = in.next();
            ipLar.computeIfAbsent(ip, k -> new ArrayList<>()).add(nom);
            macLar.computeIfAbsent(mac.toUpperCase(), k -> new ArrayList<>()).add(nom);   // MAC is case-insensitive
        }
        boolean birinchi = chiqar("IP conflict", ipLar);
        boolean ikkinchi = chiqar("duplicate MAC", macLar);
        if (!birinchi && !ikkinchi) System.out.println("no conflicts");
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

bool chiqar(const string& sarlavha, map<string, vector<string>>& jadval) {
    bool topildi = false;
    for (auto& e : jadval) {
        if (e.second.size() > 1) {
            topildi = true;
            sort(e.second.begin(), e.second.end());
            string nomlar;
            for (size_t i = 0; i < e.second.size(); i++) nomlar += (i ? ", " : "") + e.second[i];
            cout << sarlavha << ": " << e.first << " -> " << nomlar << endl;
        }
    }
    return topildi;
}

int main() {
    int n;
    cin >> n;
    map<string, vector<string>> ipLar, macLar;
    for (int i = 0; i < n; i++) {
        string nom, ip, mac;
        cin >> nom >> ip >> mac;
        ipLar[ip].push_back(nom);
        for (auto& c : mac) c = toupper(c);                          // MAC is case-insensitive
        macLar[mac].push_back(nom);
    }
    bool birinchi = chiqar("IP conflict", ipLar);
    bool ikkinchi = chiqar("duplicate MAC", macLar);
    if (!birinchi && !ikkinchi) cout << "no conflicts" << endl;
    return 0;
}
```

Input:

```text
6
PC01 192.168.50.101 00:1A:2B:00:00:01
PC02 192.168.50.102 00:1a:2b:00:00:02
PRN1 192.168.50.150 00:1A:2B:00:00:50
PC03 192.168.50.102 00:1A:2B:00:00:03
VM-A 192.168.50.120 08:00:27:AA:BB:CC
VM-B 192.168.50.121 08:00:27:aa:bb:cc
```

Output:

```text
IP conflict: 192.168.50.102 -> PC02, PC03
duplicate MAC: 08:00:27:AA:BB:CC -> VM-A, VM-B
```

PC02 and PC03 have the same address — one of them gets the "Windows has detected an IP address conflict" message on the network. VM-A and VM-B were cloned from one virtual machine without regenerating the MAC; if they are in one segment, the switch's MAC table gets confused. Run such a check before handing in project documents.

## Assignment

Do the Packet Tracer project from the sample above or the variant the teacher gives, and prepare the report from the template. Run the device list through the program above and confirm there are no conflicts. The programming tasks (KOT-A4, A7, A8, A9, A14) are checked automatically on the platform — hand in each one by its deadline.

:::warning
**Common mistakes**

- **Starting configuration without a diagram and an address plan.** Later the addresses overlap and everything has to be redone.
- **Not putting the tests into the report.** "It works" is not proof — `ping` and `tracert` results are needed.
- **Copying an essay from one source.** At least 5 sources, citations and your own conclusion are required.
- **Leaving the hand-in to the last day.** The Packet Tracer file version may not match or the file may fail to save — check in advance.
:::

:::tip
Save the project step by step (`loyiha-v1.pkt`, `loyiha-v2.pkt`): if a setting breaks everything, you can go back to the previous working version. This is the simplest form of version control.
:::

## Summary

- Kinds of independent work: an essay, a Packet Tracer project, a lab report, a programming task.
- Project stages: requirements → diagram → address plan → configuration → testing → documentation.
- Grading criteria: meeting the requirements, the address plan, tests, quality of documents, defence.
- Checking inventory documents with a program finds IP and MAC conflicts in advance.

## Check yourself

:::check
Q: What sections should a network project report have?
A: The task, the diagram, the address plan, the device list, the key configuration, the test results, the conclusion.
Q: How does an IP conflict arise and how does it show?
A: When two devices are given the same IP; one of them gets a warning and connectivity works intermittently.
Q: Why is letter case ignored when comparing MAC addresses?
A: A MAC is a hexadecimal number; `aa` and `AA` are the same value, only the written form differs.
Q: How are test results documented in a project?
A: In a table: the check, the expected result and the actual result (for example, `ping` and `tracert` output).
Q: What carries the largest share of the grade for independent work?
A: Meeting the task requirements — the network working and the services being configured.
:::

:::link https://www.netacad.com/cisco-packet-tracer
Cisco Packet Tracer
The network simulator for doing the projects.
:::
