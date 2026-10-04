---
title: Managing routers
summary: The parts of a router (CPU, RAM, NVRAM, Flash, ROM) and the boot process; connecting via console, SSH and Telnet; Cisco IOS modes and basic commands; configuring interfaces; protecting with passwords and SSH; static and default routes; reading show ip route output; saving the configuration; tracing a packet's path through routing tables.
---

## Why it matters

A router is the "crossroads" between networks. If it is set up wrongly, a whole department is left without the Internet; if it is left without a password, a stranger can take over the whole network. A network engineer's daily work is connecting to a router, configuring interfaces, adding routes and finding the cause of a fault with `show` commands. In this session we gain these skills using Cisco IOS as the example.

:::note
**After this session you will be able to:**

- name the router's memory types and its boot sequence;
- connect through the console or SSH and move between the IOS modes;
- give interfaces IP addresses and add static and default routes;
- protect a router with passwords and SSH;
- read `show ip route` output and tell which way a packet will go.
:::

## The parts of a router

| Part | What it stores |
|---|---|
| **ROM** | the bootstrap, the POST test, the emergency ROMMON mode |
| **Flash** | the IOS operating system image (a `.bin` file) |
| **NVRAM** | the **startup-config** — the configuration loaded at power-on |
| **RAM** | the running IOS, the **running-config**, the routing and ARP tables, packet buffers |

**Boot**: POST (hardware check) → the bootstrap in ROM → loading IOS from Flash into RAM → copying the startup-config from NVRAM to the running-config. If there is no startup-config, the initial setup dialogue appears.

Important: the commands you type go straight into the **running-config** (RAM). Without `copy running-config startup-config` (or `write memory`) they are **lost** on reload.

## Ways of connecting

| Method | How | When |
|---|---|---|
| **Console** | a special console cable, a terminal program (PuTTY, 9600 baud) | the first setup, when the network is down |
| **SSH** | over the network, encrypted | everyday remote management |
| **Telnet** | over the network, **unencrypted** | should not be used — the password is visible in the clear |
| **AUX** | through a modem | an old method |

## IOS modes and basic commands

```text
Router>                       user mode (viewing only)
Router> enable
Router#                       privileged mode (show, copy, reload, debug)
Router# configure terminal
Router(config)#               global configuration
Router(config)# interface g0/0
Router(config-if)#            interface configuration
Router(config-if)# exit       one level up;  end or Ctrl+Z — back to privileged mode
```

| Command | What it shows |
|---|---|
| `show ip interface brief` | interfaces, IP addresses, state (up/down) |
| `show ip route` | the routing table |
| `show running-config` | the current configuration |
| `show version` | the IOS version, uptime, memory |
| `show interfaces g0/0` | interface statistics, errors |
| `show cdp neighbors` | neighbouring Cisco devices |

Interface states: **up/up** — working; **administratively down/down** — `no shutdown` was not given; **up/down** — there is a cable but a layer 2 problem; **down/down** — no cable or no device at the other end.

## Protection

```text
Router(config)# hostname R1
R1(config)# enable secret Kuchli#Parol2026          the privileged mode password (hashed)
R1(config)# service password-encryption               hide the other passwords
R1(config)# line console 0
R1(config-line)# password KonsolParol
R1(config-line)# login
R1(config-line)# exit
R1(config)# ip domain-name aifu.local
R1(config)# crypto key generate rsa modulus 2048      the SSH keys
R1(config)# username admin secret AdminParol
R1(config)# line vty 0 4
R1(config-line)# login local
R1(config-line)# transport input ssh                  SSH only, Telnet closed
R1(config)# banner motd #Authorised users only#
```

Use **`enable secret`**, not `enable password` — the latter is stored in the configuration as plain text.

## Routes

```text
R1(config)# ip route 10.20.0.0 255.255.0.0 192.168.1.2     a static route: network mask next_hop
R1(config)# ip route 0.0.0.0 0.0.0.0 203.0.113.1           the default route
```

The codes in `show ip route` output: **C** — directly connected, **L** — the interface's own address (/32), **S** — static, **R** — RIP, **O** — OSPF, **\*** — the candidate default route. The `[1/0]` in an entry is [administrative distance/metric].

For every packet a router picks the **longest matching prefix** from the table; a program for this choice is this session's independent task (KOT-A8).

## Practical work: three routers

1. In Packet Tracer connect R1, R2, R3 in a chain (R1–R2: 10.0.12.0/30, R2–R3: 10.0.23.0/30). Behind R1 is LAN-A (192.168.1.0/24), behind R3 is LAN-B (192.168.3.0/24).
2. Configure the interfaces on every router (`ip address`, `no shutdown`) and check with `show ip interface brief`.
3. Add a static route to the opposite LAN on R1 and R3, and routes to both LANs on R2.
4. Run `ping` and `tracert` from a computer in LAN-A to LAN-B.
5. Remove one route on R2 (`no ip route ...`) and see where `tracert` stops.
6. Save the configuration on all routers.

## Worked example 1: tracing a packet's path

The first line holds the number of entries *k*; each of the next *k* lines holds: a router, the name of a destination network and the next hop (`direct` — the network is directly connected to this router; the destination `default` is the default route). Then come *q* queries: a starting router and a destination network. The packet is sent with TTL = 6; each forwarding decreases the TTL by 1. Print the path and the outcome: `delivered`, `no route` or `TTL expired`.

```python
k = int(input())
jadval = {}                                   # (router, network) -> next hop
for _ in range(k):
    router, tarmoq, keyingi = input().split()
    jadval[(router, tarmoq)] = keyingi
q = int(input())
for _ in range(q):
    joriy, tarmoq = input().split()
    yol = [joriy]
    ttl = 6
    while True:
        keyingi = jadval.get((joriy, tarmoq), jadval.get((joriy, "default")))
        if keyingi == "direct":
            natija = "delivered"
            break
        if keyingi is None:
            natija = "no route"
            break
        if ttl == 0:
            natija = "TTL expired"             # a looping packet is dropped
            break
        ttl -= 1
        joriy = keyingi
        yol.append(joriy)
    print(" -> ".join(yol) + " (" + natija + ")")
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
        int k = in.nextInt();
        Map<String, String> jadval = new HashMap<>();          // "router network" -> next hop
        for (int i = 0; i < k; i++) {
            String router = in.next();
            String tarmoq = in.next();
            jadval.put(router + " " + tarmoq, in.next());
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String joriy = in.next();
            String tarmoq = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(joriy);
            int ttl = 6;
            String natija;
            while (true) {
                String keyingi = jadval.getOrDefault(joriy + " " + tarmoq, jadval.get(joriy + " default"));
                if ("direct".equals(keyingi)) {
                    natija = "delivered";
                    break;
                }
                if (keyingi == null) {
                    natija = "no route";
                    break;
                }
                if (ttl == 0) {
                    natija = "TTL expired";                      // a looping packet is dropped
                    break;
                }
                ttl--;
                joriy = keyingi;
                yol.add(joriy);
            }
            System.out.println(String.join(" -> ", yol) + " (" + natija + ")");
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
    int k;
    cin >> k;
    map<pair<string, string>, string> jadval;                // (router, network) -> next hop
    for (int i = 0; i < k; i++) {
        string router, tarmoq, keyingi;
        cin >> router >> tarmoq >> keyingi;
        jadval[{router, tarmoq}] = keyingi;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string joriy, tarmoq;
        cin >> joriy >> tarmoq;
        vector<string> yol = {joriy};
        int ttl = 6;
        string natija;
        while (true) {
            string keyingi;
            if (jadval.count({joriy, tarmoq})) keyingi = jadval[{joriy, tarmoq}];
            else if (jadval.count({joriy, "default"})) keyingi = jadval[{joriy, "default"}];
            if (keyingi == "direct") {
                natija = "delivered";
                break;
            }
            if (keyingi.empty()) {
                natija = "no route";
                break;
            }
            if (ttl == 0) {
                natija = "TTL expired";                          // a looping packet is dropped
                break;
            }
            ttl--;
            joriy = keyingi;
            yol.push_back(joriy);
        }
        string chiqish;
        for (size_t j = 0; j < yol.size(); j++) chiqish += (j ? " -> " : "") + yol[j];
        cout << chiqish << " (" << natija << ")" << endl;
    }
    return 0;
}
```

Input:

```text
7
R1 LAN-A direct
R1 LAN-B R2
R2 LAN-A R1
R2 LAN-B R3
R2 default R3
R3 LAN-B direct
R3 default R2
4
R1 LAN-B
R3 LAN-A
R1 LAN-C
R2 LAN-C
```

Output:

```text
R1 -> R2 -> R3 (delivered)
R3 -> R2 -> R1 (delivered)
R1 (no route)
R2 -> R3 -> R2 -> R3 -> R2 -> R3 -> R2 (TTL expired)
```

The last line is a classic **routing loop**: R2's default route points to R3 and R3's to R2. A packet for the non-existent LAN-C circles between them until the TTL runs out. In a real network `tracert` shows this as the same two addresses repeating in turn.

## Worked example 2: generating configuration commands

The first line holds the router's name, the second the number of interfaces *n*, then *n* lines "interface address/prefix", then the number of routes *m* and *m* lines "network/prefix next_hop". Print the sequence of commands to type into IOS (the prefix is converted to a mask).

```python
def maska(p):
    m = (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF if p > 0 else 0
    return ".".join(str((m >> s) & 255) for s in (24, 16, 8, 0))


nom = input().strip()
print("enable")
print("configure terminal")
print("hostname " + nom)
n = int(input())
for _ in range(n):
    interfeys, manzil = input().split()
    ip, p = manzil.split("/")
    print("interface " + interfeys)
    print(" ip address " + ip + " " + maska(int(p)))
    print(" no shutdown")
    print("exit")
m = int(input())
for _ in range(m):
    tarmoq, keyingi = input().split()
    ip, p = tarmoq.split("/")
    print("ip route " + ip + " " + maska(int(p)) + " " + keyingi)
print("end")
print("copy running-config startup-config")    # otherwise it is lost on reload
```
```java
import java.util.Scanner;

public class Main {
    static String maska(int p) {
        long m = p > 0 ? (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL : 0;
        return ((m >> 24) & 255) + "." + ((m >> 16) & 255) + "." + ((m >> 8) & 255) + "." + (m & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String nom = in.next();
        System.out.println("enable");
        System.out.println("configure terminal");
        System.out.println("hostname " + nom);
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String interfeys = in.next();
            String[] manzil = in.next().split("/");
            System.out.println("interface " + interfeys);
            System.out.println(" ip address " + manzil[0] + " " + maska(Integer.parseInt(manzil[1])));
            System.out.println(" no shutdown");
            System.out.println("exit");
        }
        int m = in.nextInt();
        for (int i = 0; i < m; i++) {
            String[] tarmoq = in.next().split("/");
            String keyingi = in.next();
            System.out.println("ip route " + tarmoq[0] + " " + maska(Integer.parseInt(tarmoq[1])) + " " + keyingi);
        }
        System.out.println("end");
        System.out.println("copy running-config startup-config");   // otherwise it is lost on reload
    }
}
```
```cpp
#include <cstdint>
#include <iostream>
#include <string>
using namespace std;

string maska(int p) {
    uint64_t m = p > 0 ? (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL : 0;
    return to_string((m >> 24) & 255) + "." + to_string((m >> 16) & 255) + "." + to_string((m >> 8) & 255) + "." + to_string(m & 255);
}

int main() {
    string nom;
    cin >> nom;
    cout << "enable" << endl;
    cout << "configure terminal" << endl;
    cout << "hostname " << nom << endl;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string interfeys, manzil;
        cin >> interfeys >> manzil;
        size_t chiziq = manzil.find('/');
        cout << "interface " << interfeys << endl;
        cout << " ip address " << manzil.substr(0, chiziq) << " " << maska(stoi(manzil.substr(chiziq + 1))) << endl;
        cout << " no shutdown" << endl;
        cout << "exit" << endl;
    }
    int m;
    cin >> m;
    for (int i = 0; i < m; i++) {
        string tarmoq, keyingi;
        cin >> tarmoq >> keyingi;
        size_t chiziq = tarmoq.find('/');
        cout << "ip route " << tarmoq.substr(0, chiziq) << " " << maska(stoi(tarmoq.substr(chiziq + 1))) << " " << keyingi << endl;
    }
    cout << "end" << endl;
    cout << "copy running-config startup-config" << endl;   // otherwise it is lost on reload
    return 0;
}
```

Input:

```text
R1
2
g0/0 192.168.1.1/24
g0/1 10.0.12.1/30
2
192.168.3.0/24 10.0.12.2
0.0.0.0/0 10.0.12.2
```

Output:

```text
enable
configure terminal
hostname R1
interface g0/0
 ip address 192.168.1.1 255.255.255.0
 no shutdown
exit
interface g0/1
 ip address 10.0.12.1 255.255.255.252
 no shutdown
exit
ip route 192.168.3.0 255.255.255.0 10.0.12.2
ip route 0.0.0.0 0.0.0.0 10.0.12.2
end
copy running-config startup-config
```

Such "configuration generators" are the simplest form of network automation (Ansible, Python + Netmiko): in a network with many routers typing commands by hand leads to mistakes, while a template always gives the same correct result.

:::warning
**Common mistakes**

- **Forgetting to save the configuration.** Without `copy running-config startup-config` everything is lost when the power goes off.
- **Not giving an interface `no shutdown`.** Cisco router interfaces are off by default (administratively down).
- **Using Telnet and setting `enable password`.** The passwords are visible in the clear; use SSH and `enable secret`.
- **Giving a distant router as the next hop of a static route.** The next hop is the address of a directly connected neighbour's interface.
- **Pointing two routers' default routes at each other.** This creates a routing loop (LAN-C in the first example).
:::

:::tip
After doing the practical work in Packet Tracer, copy the `show ip route` output on R2 and explain the code (C, L, S) and the `[1/0]` values on every line. Then turn on `debug ip packet` (in the lab only!) and watch the router make its decisions during a `ping`.
:::

## Summary

- A router: ROM (bootstrap), Flash (IOS), NVRAM (startup-config), RAM (running-config, tables).
- Connecting: the console for the first setup, then SSH only; Telnet is dangerous.
- IOS modes: `>` → `enable` → `#` → `configure terminal` → `(config)#` → `interface` → `(config-if)#`.
- Basic commands: `show ip interface brief`, `show ip route`, `show running-config`; routes are `ip route network mask next_hop`.
- Protection: `enable secret`, console and VTY passwords, `transport input ssh`, `service password-encryption`; at the end — save the configuration.

## Check yourself

:::check
Q: Where are the running-config and the startup-config stored?
A: The running-config is in RAM (current), the startup-config in NVRAM (loaded at power-on).
Q: What should be done if an interface is "administratively down"?
A: Give the `no shutdown` command in interface configuration mode.
Q: How is a default route written?
A: `ip route 0.0.0.0 0.0.0.0 <next_hop>`.
Q: Why is `enable secret` used rather than `enable password`?
A: `enable secret` stores the password as a hash, `enable password` as plain text.
Q: What do the codes `S` and `C` mean in `show ip route`?
A: S — a static route, C — a directly connected network.
:::

:::link https://www.netacad.com/courses/ccna-introduction-networks
CCNA: Introduction to Networks
Cisco Networking Academy's free course on configuring routers and switches.
:::
