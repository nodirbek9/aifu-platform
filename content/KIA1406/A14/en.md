---
title: Connecting a virtual VPN and protecting traffic
summary: Practical session — why a VPN is needed and what not to expect from it; types of VPN (remote access, site-to-site); protocols — WireGuard, OpenVPN, IPsec and how they compare; setting up WireGuard in a lab — key pairs, the [Interface] and [Peer] sections, AllowedIPs, Endpoint, connecting and checking the status; full and split tunnels; DNS leaks and the "kill switch"; criteria for choosing a VPN provider; a VPN policy in an organization; a program determining whether an address goes through the tunnel or directly.
---

## Why does this topic matter?

In lecture M14 we saw that a VPN is an encrypted tunnel over an open network. In this practical session you set up a VPN tunnel in **your own training lab** (two virtual machines, or a computer and a training server), check that it works and learn to control which path traffic takes. Employees working remotely, branch offices and working on public Wi-Fi are where VPNs are used every day.

:::note
**After this practical session you will be able to:**

- explain which risks a VPN protects against and which it does not;
- compare WireGuard, OpenVPN and IPsec;
- set up a WireGuard tunnel in a lab and check that it works;
- understand the difference between full and split tunnels and determine the route by CIDR;
- check for DNS leaks and the "kill switch".
:::

:::warning
Carry out the session only on a training network that belongs to you or that the teacher has authorized. Using a VPN on an organization's network is subject to its security policy; comply with the current legal requirements on VPN use.
:::

## What does a VPN protect against?

| It protects against | It does not protect against |
|---|---|
| outsiders reading traffic on public Wi-Fi | malicious sites and phishing |
| the provider or network owner seeing the content of traffic | malware on the device |
| insecure remote access to an internal network (employees, branches) | a weak password or a compromised account |
| data integrity problems on an open network | the VPN provider itself — it sees the traffic |

A VPN is a tool for confidentiality and secure access, but not "complete protection": antivirus, updates, MFA and caution are still needed.

## Types of VPN and protocols

- **Remote access** — an employee connects from a laptop to the office network.
- **Site-to-site** — two office networks are joined by a permanent tunnel.

| Protocol | Features | Advantage | Drawback |
|---|---|---|---|
| **WireGuard** | modern, a small code base, in the Linux kernel | fast, simple to configure, modern cryptography | static configuration, user management through external tools |
| **OpenVPN** | based on TLS, many years of experience | flexible, TCP or UDP, on many platforms | slower, more complex to configure |
| **IPsec** (IKEv2) | works at the network layer (OSI 3) | built into many operating systems and devices, the site-to-site standard | complex to configure |

## Practical work: setting up a WireGuard tunnel

**The lab**: two Linux virtual machines — a **server** (for example, 192.168.56.10) and a **client**. Addresses inside the tunnel: server `10.8.0.1`, client `10.8.0.2`.

**1. Installation and keys** (on both machines):

```text
sudo apt install wireguard
wg genkey | tee privatekey | wg pubkey > publickey
chmod 600 privatekey
```

Each side keeps its **private key** to itself and gives it to no one; the **public keys** are exchanged (the asymmetric cryptography idea from S14 and MT30).

**2. Server configuration** `/etc/wireguard/wg0.conf`:

```text
[Interface]
Address = 10.8.0.1/24
ListenPort = 51820
PrivateKey = <the server's private key>

[Peer]
PublicKey = <the client's public key>
AllowedIPs = 10.8.0.2/32
```

**3. Client configuration** `/etc/wireguard/wg0.conf`:

```text
[Interface]
Address = 10.8.0.2/24
PrivateKey = <the client's private key>
DNS = 10.8.0.1

[Peer]
PublicKey = <the server's public key>
Endpoint = 192.168.56.10:51820
AllowedIPs = 10.8.0.0/24
PersistentKeepalive = 25
```

**4. Starting and checking**:

```text
sudo wg-quick up wg0
sudo wg show
ping 10.8.0.1
```

The `wg show` output should show the **latest handshake** and the **transfer** (bytes sent and received). On the server's firewall only port 51820/UDP is opened (M14).

**5. Checking that traffic is encrypted**: as in session A13, watch the external interface with a traffic analyser — instead of the `ping` or HTTP request inside the tunnel, you see only UDP packets to port 51820, with encrypted content.

## Full and split tunnels

On the client side, **AllowedIPs** acts as a **routing rule**: "which addresses should be sent through the tunnel":

| Mode | AllowedIPs | Result |
|---|---|---|
| **Full tunnel** | `0.0.0.0/0` | all traffic goes through the VPN — maximum protection on public Wi-Fi, but slower |
| **Split tunnel** | only internal networks, for example `10.8.0.0/24, 192.168.50.0/24` | office resources go through the tunnel, the rest of the Internet directly |

A split tunnel saves bandwidth, but Internet traffic stays outside the VPN's protection — the organization's policy decides which mode is used when.

## Worked example: which path does an address take?

The first line holds the client's AllowedIPs list (CIDRs, comma-separated); the following lines hold destination addresses. For each address, determine whether it goes through the tunnel (and by which rule) or directly. If several rules match, the rule with the longest prefix (the most specific) is chosen — as in real routing.

```python
import sys

def son(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d

def mos(ip, cidr):
    tarmoq, uzunlik = cidr.split("/")
    uzunlik = int(uzunlik)
    niqob = 0 if uzunlik == 0 else (0xFFFFFFFF << (32 - uzunlik)) & 0xFFFFFFFF
    return (son(ip) & niqob) == (son(tarmoq) & niqob)

satrlar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
qoidalar = satrlar[0].split(",")
for ip in satrlar[1:]:
    eng_yaxshi = None                                  # the matching rule with the longest prefix
    for q in qoidalar:
        if mos(ip, q) and (eng_yaxshi is None or int(q.split("/")[1]) > int(eng_yaxshi.split("/")[1])):
            eng_yaxshi = q
    if eng_yaxshi:
        print(f"{ip} -> via tunnel ({eng_yaxshi})")
    else:
        print(f"{ip} -> direct")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long son(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static boolean mos(String ip, String cidr) {
        String[] q = cidr.split("/");
        int uzunlik = Integer.parseInt(q[1]);
        long niqob = uzunlik == 0 ? 0 : (0xFFFFFFFFL << (32 - uzunlik)) & 0xFFFFFFFFL;
        return (son(ip) & niqob) == (son(q[0]) & niqob);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s);
        }
        String[] qoidalar = satrlar.get(0).split(",");
        for (String ip : satrlar.subList(1, satrlar.size())) {
            String engYaxshi = null;                       // the matching rule with the longest prefix
            for (String q : qoidalar)
                if (mos(ip, q) && (engYaxshi == null || Integer.parseInt(q.split("/")[1]) > Integer.parseInt(engYaxshi.split("/")[1])))
                    engYaxshi = q;
            if (engYaxshi != null) System.out.println(ip + " -> via tunnel (" + engYaxshi + ")");
            else System.out.println(ip + " -> direct");
        }
    }
}
```
```cpp
#include <cstdint>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t son(const string& ip) {
    uint32_t a, b, c, d;
    char t;
    istringstream ss(ip);
    ss >> a >> t >> b >> t >> c >> t >> d;
    return (a << 24) | (b << 16) | (c << 8) | d;
}

int uzunligi(const string& cidr) { return stoi(cidr.substr(cidr.find('/') + 1)); }

bool mos(const string& ip, const string& cidr) {
    int uzunlik = uzunligi(cidr);
    uint32_t niqob = uzunlik == 0 ? 0 : (0xFFFFFFFFu << (32 - uzunlik));
    return (son(ip) & niqob) == (son(cidr.substr(0, cidr.find('/'))) & niqob);
}

int main() {
    string satr;
    getline(cin, satr);
    vector<string> qoidalar;
    stringstream ss(satr);
    string q;
    while (getline(ss, q, ',')) {
        q.erase(q.find_last_not_of(" \r") + 1);
        qoidalar.push_back(q);
    }
    string ip;
    while (cin >> ip) {
        string engYaxshi;                                  // the matching rule with the longest prefix
        for (auto& r : qoidalar)
            if (mos(ip, r) && (engYaxshi.empty() || uzunligi(r) > uzunligi(engYaxshi))) engYaxshi = r;
        if (!engYaxshi.empty()) cout << ip << " -> via tunnel (" << engYaxshi << ")\n";
        else cout << ip << " -> direct\n";
    }
    return 0;
}
```

Input:

```text
10.8.0.0/24,192.168.50.0/24,172.16.0.0/12,192.168.50.128/25
10.8.0.15
192.168.50.200
192.168.50.20
172.20.5.9
8.8.8.8
192.168.1.10
```

Output:

```text
10.8.0.15 -> via tunnel (10.8.0.0/24)
192.168.50.200 -> via tunnel (192.168.50.128/25)
192.168.50.20 -> via tunnel (192.168.50.0/24)
172.20.5.9 -> via tunnel (172.16.0.0/12)
8.8.8.8 -> direct
192.168.1.10 -> direct
```

`192.168.50.200` matches two rules; the more specific `/25` was chosen. `172.16.0.0/12` covers the private range from 172.16.0.0 to 172.31.255.255. `192.168.1.10` on the home network and the public `8.8.8.8` go directly — this is a split tunnel. If `0.0.0.0/0` is added to the list, all addresses go into the tunnel (a full tunnel).

## DNS leaks and the kill switch

- **A DNS leak**: even when traffic goes through the tunnel, DNS queries may go to the local provider — which then sees which sites are being visited. To check: with the VPN on, use a DNS leak test site or `nslookup` to see which DNS server answers; the fix is the `DNS =` line in the client configuration and, in a full tunnel, routing DNS inside the tunnel.
- **The kill switch**: so that traffic does not automatically go out unencrypted if the VPN drops — blocking the Internet until the tunnel is restored (a firewall rule or a setting in the client program).

## Choosing a VPN provider

When choosing a commercial VPN service, look at: the logging policy (what data is kept) and whether an independent audit has confirmed it, the company's jurisdiction, which protocols and encryption are used, whether there is a kill switch and DNS leak protection. **Free VPNs** often earn money by selling user data or through advertising — for corporate purposes only a VPN approved by the organization is used.

:::warning
**Common mistakes**

- **Sending the private key to the other side or storing it in an open place.** Only public keys are exchanged; `privatekey` has permissions 600.
- **Writing AllowedIPs wrongly.** The needed network does not go into the tunnel, or, conversely, all traffic does.
- **Seeing a VPN as "complete protection".** It does not protect against phishing and malware.
- **Not checking for DNS leaks.**
- **Opening unnecessary ports on the server's firewall.** Only the VPN port.
:::

:::tip
Include in your session report: both sides' configuration files (without the private keys!), the `wg show` output, the result of a `ping` through the tunnel and a screenshot showing only encrypted UDP packets on the external interface.
:::

## Summary

- A VPN creates an encrypted tunnel over an open network; it does not protect against phishing, malware or weak passwords.
- Protocols: WireGuard (fast, simple), OpenVPN (flexible), IPsec (network layer, site-to-site).
- WireGuard: key pairs, [Interface] and [Peer], AllowedIPs as a routing rule; checking with `wg show`.
- Full tunnel (`0.0.0.0/0`) and split tunnel; the rule with the longest prefix is chosen.
- DNS leaks and the kill switch are checked; a provider is chosen by logging policy and jurisdiction.

## Check yourself

:::check
Q: What does AllowedIPs determine on the client side in WireGuard?
A: Which addresses traffic is sent to through the tunnel — it acts as a routing rule.
Q: How is AllowedIPs written for a full tunnel?
A: 0.0.0.0/0.
Q: What is a DNS leak?
A: When traffic goes through the tunnel but DNS queries go outside it to the local provider.
Q: Which risks does a VPN not protect against?
A: Phishing and malicious sites, malware on the device, weak passwords.
:::

:::link https://www.wireguard.com/quickstart/
WireGuard: Quick Start
The official guide to installing and configuring WireGuard.
:::
