---
title: Configuring the firewall in the Windows operating system. Using a VPN service
summary: Windows Defender Firewall — profiles (domain, private, public), inbound and outbound rules, wf.msc, netsh advfirewall and PowerShell; testing rules and the log; NAT and PAT — why NAT also blocks inbound connections; the VPN client in Windows (IKEv2, SSTP, L2TP/IPsec), WireGuard, Windows Server RRAS; checking a VPN connection.
---

## Why it matters

In lecture M14 we covered the theory of firewalls and VPNs. Now we configure them in practice: a server must open only the ports it needs, a laptop on café Wi-Fi must stay closed under the "public" profile, and a teacher at home must connect to the university network through an encrypted tunnel. We also see with a program how **NAT** on a home router plays a protective role too.

:::note
**After this session you will be able to:**

- explain the Windows Defender Firewall profiles and the order of rules;
- create inbound and outbound rules with the graphical tool, `netsh` and PowerShell;
- test a rule and read the firewall log;
- explain how a NAT/PAT table works and why it blocks unexpected inbound packets;
- configure a VPN connection in Windows and check it.
:::

## Windows Defender Firewall

A **stateful** firewall built into Windows. It uses three **profiles** depending on the type of network:

| Profile | When | Typical setting |
|---|---|---|
| **Domain** | the computer is on the domain network (a DC was found) | managed by GPO |
| **Private** | a home or trusted network | file sharing and discovery may be allowed |
| **Public** | café, airport, hotel Wi-Fi | the strictest: inbound connections closed |

By default: **inbound** — everything not allowed is blocked; **outbound** — everything not blocked is allowed. Rule types: by program, by port, predefined (e.g. "File and Printer Sharing"), custom. A rule has: direction, action (Allow/Block), protocol and port, source and destination addresses, profile. **A Block rule beats an Allow.**

The tool: **wf.msc** (Windows Defender Firewall with Advanced Security).

## Creating rules

```text
netsh advfirewall show allprofiles                         state of the profiles
netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80
netsh advfirewall firewall add rule name="Block Telnet" dir=in action=block protocol=TCP localport=23
netsh advfirewall firewall show rule name="Web 80"
netsh advfirewall firewall delete rule name="Web 80"
```

PowerShell:

```text
Get-NetFirewallProfile | Select-Object Name, Enabled, DefaultInboundAction
New-NetFirewallRule -DisplayName "RDP from the lab only" -Direction Inbound -Protocol TCP -LocalPort 3389 -RemoteAddress 192.168.50.0/24 -Action Allow
New-NetFirewallRule -DisplayName "ICMP Echo" -Direction Inbound -Protocol ICMPv4 -IcmpType 8 -Action Allow
Get-NetFirewallRule -Enabled True -Direction Inbound | Measure-Object
Set-NetFirewallProfile -Profile Domain,Public,Private -LogBlocked True -LogFileName "%systemroot%\system32\LogFiles\Firewall\pfirewall.log"
```

**Testing**: from another computer, `Test-NetConnection 192.168.50.10 -Port 80` (`TcpTestSucceeded : True/False`) and `ping`. Blocked packets are written to the **log** (`pfirewall.log`) with their time, source and port.

## Practical work 1: protecting the server

1. Open `wf.msc` on `SRV-DC01` and check that all three profiles are on.
2. Add an inbound allow for **TCP 80** for IIS or a simple web server; check from the client with a browser.
3. Allow RDP (3389) only from the lab subnet (`192.168.50.0/24`).
4. Turn ICMP Echo on and off — compare the result of `ping` on the client.
5. Turn on logging of blocked packets, try to connect from the client to a closed port (`Test-NetConnection -Port 23`) and find the entry in the log.
6. Export the rule list: `netsh advfirewall export C:\fw-backup.wfw`.

A program that checks a rule list on the "first match" principle is this session's independent task (KOT-A14).

## NAT and PAT

A home or office router replaces the internal private addresses (192.168.x.x) with one public address — **PAT** (Port Address Translation, "NAT overload"):

1. An outgoing packet from inside: source `192.168.1.10:51000` → the router replaces it with `203.0.113.5:50000` and writes it into the **NAT table**.
2. The reply arrives at `203.0.113.5:50000` → the router finds it in the table and delivers it to `192.168.1.10:51000`.
3. An outside packet with no entry in the table is **dropped**: no computer inside was expecting it.

That is why a computer behind NAT cannot be reached directly from outside (unless **port forwarding** — a static entry — is configured). This is a side effect that protects, but it does not replace a firewall.

## Worked example: a PAT table

The first line holds the router's public IP address and the first outside port. The following lines are events: `OUT inside_ip inside_port outside_ip outside_port` (an outgoing packet from inside) or `IN outside_ip outside_port router_port` (a packet from outside). For an outgoing packet, the same inside address-and-port pair gets the same router port, and a new one gets the next port. Deliver an incoming packet to the inside address by the table, or drop it (no entry, or the packet came from a different server).

```python
import sys

satrlar = sys.stdin.read().strip().split("\n")
ommaviy, keyingi = satrlar[0].split()
keyingi = int(keyingi)
jadval = {}                               # (inside_ip, inside_port) -> router port
teskari = {}                              # router port -> (inside_ip, inside_port, server_ip, server_port)
for satr in satrlar[1:]:
    q = satr.split()
    if q[0] == "OUT":
        kalit = (q[1], q[2])
        if kalit not in jadval:
            jadval[kalit] = keyingi       # a new entry
            teskari[keyingi] = (q[1], q[2], q[3], q[4])
            keyingi += 1
        port = jadval[kalit]
        print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " -> " + q[3] + ":" + q[4])
    else:
        port = int(q[3])
        yozuv = teskari.get(port)
        if yozuv is not None and yozuv[2] == q[1] and yozuv[3] == q[2]:
            print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " -> " + yozuv[0] + ":" + yozuv[1])
        else:
            print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " dropped")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String ommaviy = in.next();
        int keyingi = in.nextInt();
        Map<String, Integer> jadval = new HashMap<>();             // "inside_ip inside_port" -> router port
        Map<Integer, String[]> teskari = new HashMap<>();          // router port -> {inside_ip, inside_port, server_ip, server_port}
        while (in.hasNext()) {
            String tur = in.next();
            if (tur.equals("OUT")) {
                String ip = in.next(), ichkiPort = in.next(), server = in.next(), serverPort = in.next();
                String kalit = ip + " " + ichkiPort;
                if (!jadval.containsKey(kalit)) {
                    jadval.put(kalit, keyingi);                     // a new entry
                    teskari.put(keyingi, new String[] {ip, ichkiPort, server, serverPort});
                    keyingi++;
                }
                int port = jadval.get(kalit);
                System.out.println(ip + ":" + ichkiPort + " -> " + ommaviy + ":" + port + " -> " + server + ":" + serverPort);
            } else {
                String server = in.next(), serverPort = in.next();
                int port = in.nextInt();
                String[] yozuv = teskari.get(port);
                if (yozuv != null && yozuv[2].equals(server) && yozuv[3].equals(serverPort)) {
                    System.out.println(server + ":" + serverPort + " -> " + ommaviy + ":" + port + " -> " + yozuv[0] + ":" + yozuv[1]);
                } else {
                    System.out.println(server + ":" + serverPort + " -> " + ommaviy + ":" + port + " dropped");
                }
            }
        }
    }
}
```
```cpp
#include <array>
#include <iostream>
#include <map>
#include <string>
#include <utility>
using namespace std;

int main() {
    string ommaviy;
    int keyingi;
    cin >> ommaviy >> keyingi;
    map<pair<string, string>, int> jadval;                         // (inside_ip, inside_port) -> router port
    map<int, array<string, 4>> teskari;                            // router port -> {inside_ip, inside_port, server_ip, server_port}
    string tur;
    while (cin >> tur) {
        if (tur == "OUT") {
            string ip, ichkiPort, server, serverPort;
            cin >> ip >> ichkiPort >> server >> serverPort;
            pair<string, string> kalit = {ip, ichkiPort};
            if (!jadval.count(kalit)) {
                jadval[kalit] = keyingi;                           // a new entry
                teskari[keyingi] = {ip, ichkiPort, server, serverPort};
                keyingi++;
            }
            int port = jadval[kalit];
            cout << ip << ":" << ichkiPort << " -> " << ommaviy << ":" << port << " -> " << server << ":" << serverPort << endl;
        } else {
            string server, serverPort;
            int port;
            cin >> server >> serverPort >> port;
            if (teskari.count(port) && teskari[port][2] == server && teskari[port][3] == serverPort) {
                cout << server << ":" << serverPort << " -> " << ommaviy << ":" << port << " -> " << teskari[port][0] << ":" << teskari[port][1] << endl;
            } else {
                cout << server << ":" << serverPort << " -> " << ommaviy << ":" << port << " dropped" << endl;
            }
        }
    }
    return 0;
}
```

Input:

```text
203.0.113.5 50000
OUT 192.168.1.10 51000 8.8.8.8 53
OUT 192.168.1.11 51000 142.250.74.46 443
IN 8.8.8.8 53 50000
IN 142.250.74.46 443 50001
OUT 192.168.1.10 51000 8.8.8.8 53
IN 198.51.100.66 443 50001
IN 198.51.100.66 3389 50002
```

Output:

```text
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
192.168.1.11:51000 -> 203.0.113.5:50001 -> 142.250.74.46:443
8.8.8.8:53 -> 203.0.113.5:50000 -> 192.168.1.10:51000
142.250.74.46:443 -> 203.0.113.5:50001 -> 192.168.1.11:51000
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
198.51.100.66:443 -> 203.0.113.5:50001 dropped
198.51.100.66:3389 -> 203.0.113.5:50002 dropped
```

Even though both computers use the same inside port (51000), the router told them apart with different outside ports. The last two packets: one to an existing port but from a **different** server (an attempt at a fake reply), the other to a port with no entry at all (an RDP scan) — both were dropped.

## Connecting to a VPN

**The Windows client**: Settings → Network & Internet → **VPN** → "Add a VPN connection": provider — Windows (built-in), server name (`vpn.aifu.uz`), VPN type (**IKEv2** — recommended, **SSTP** — over port 443, **L2TP/IPsec**), sign-in type (user name and password, certificate). Other options are the **WireGuard** or OpenVPN clients (a configuration file is imported).

**The server side**: on Windows Server the **Remote Access → RRAS** (Routing and Remote Access) role, on Linux WireGuard or OpenVPN. The relevant ports are opened in the firewall: IKEv2 — UDP 500 and 4500, SSTP — TCP 443, WireGuard — UDP 51820.

**Checking**:

```text
rasdial "AIFU VPN" user password    connect from the command line
ipconfig /all                       the VPN adapter and the internal address it got
route print                         which traffic goes through the tunnel
ping 192.168.50.10                  reaching an internal server
tracert 192.168.50.10               the first hop is the VPN server
rasdial "AIFU VPN" /disconnect
```

**Split tunnelling**: only internal network traffic goes through the tunnel, the rest goes straight to the Internet. It saves bandwidth, but a security policy sometimes requires sending all traffic through the tunnel (full tunnel).

:::warning
**Common mistakes**

- **Turning the firewall off completely because "it doesn't work".** The right way is to add a rule for exactly the port needed.
- **Choosing the "Private" profile on public Wi-Fi.** File sharing and network discovery are opened to strangers.
- **Not restricting the source address in a rule.** Instead of opening RDP from "Any", give only the subnet that needs it.
- **Taking NAT for a firewall.** Attacks come straight through ports opened by port forwarding or UPnP.
- **Using a PPTP VPN.** An obsolete and broken protocol; use IKEv2, SSTP or WireGuard.
:::

:::tip
On your home router's management page (your own only!) find the NAT table or the "Attached devices" section. Look at the open connections on your computer with `netstat -n` and compare their inside ports with the outside ports in the router's table (if the router shows them).
:::

## Summary

- Windows Defender Firewall is stateful; the profiles are Domain, Private, Public; inbound is blocked by default, Block beats Allow.
- Rules: `wf.msc`, `netsh advfirewall`, `New-NetFirewallRule`; testing — `Test-NetConnection`, the log — `pfirewall.log`.
- PAT replaces inside address:port with public address:port; incoming packets not in the table are dropped.
- VPN: the Windows client (IKEv2, SSTP, L2TP), WireGuard, RRAS; checking — `ipconfig`, `route print`, `tracert`.

## Check yourself

:::check
Q: Which Windows Defender Firewall profile is the strictest, and when is it used?
A: Public — on public networks (café or airport Wi-Fi).
Q: How can an inbound allow for port 80 be added with `netsh`?
A: `netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80`.
Q: Why can a computer behind NAT not be reached from outside?
A: The router's NAT table has no entry for such a connection — it does not know which inside computer to give the packet to, so it drops it.
Q: How can the effect of firewall rules be tested?
A: With `Test-NetConnection server -Port N` from another computer and by looking at the firewall log.
Q: What is split tunnelling?
A: In a VPN, sending only internal network traffic through the tunnel and the rest straight to the Internet.
:::

:::link https://learn.microsoft.com/en-us/windows/security/operating-system-security/network-security/windows-firewall/
Windows Firewall
Microsoft's official documentation on Windows Firewall.
:::
