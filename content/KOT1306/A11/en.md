---
title: Installing and configuring network services in Windows Server (DHCP, DNS)
summary: Installing and authorising the DHCP server role; the scope, the address pool, exclusions, reservations, the lease duration, options (gateway, DNS); installing the DNS server role; forward and reverse lookup zones, A, CNAME, PTR, MX records, forwarders; PowerShell commands; checking on a client; modelling a DHCP pool.
---

## Why it matters

In lectures M9 and M12 we learned how DHCP and DNS work. Now we **configure** them ourselves: the `SRV-DC01` server installed in A10 will hand out addresses automatically to the computers on the network and resolve internal names (`srv-dc01.aifu.local`, `printer.aifu.local`). These two services are the foundation of any organisation's network: if they fail, for users "nothing works".

:::note
**After this session you will be able to:**

- install the DHCP role on Windows Server and configure a scope, exclusions, reservations and options;
- install the DNS role and create forward and reverse zones and records;
- do the same with PowerShell commands;
- check the result on a client computer with `ipconfig` and `nslookup`;
- explain how a DHCP server chooses addresses and keeps track of leases.
:::

## The lab layout

| Device | Address | Role |
|---|---|---|
| `SRV-DC01` | 192.168.50.10/24 (static) | DHCP and DNS server |
| Router (gateway) | 192.168.50.1 | Internet access |
| Clients | 192.168.50.100–192.168.50.199 (DHCP) | user computers |
| Printer | 192.168.50.150 (reservation) | a permanent address, but through DHCP |

All VMs are on an **internal network** — the test DHCP server must not reach the real network.

## Configuring the DHCP server

1. Server Manager → **Add Roles and Features** → **DHCP Server**. After installation, the yellow flag → **Complete DHCP configuration** (creating the security groups; in a domain — **authorising** the server in AD: an unauthorised DHCP server does not work on a domain network).
2. **Tools → DHCP** → IPv4 → **New Scope**:
   - name: `Talabalar-LAN`;
   - pool: 192.168.50.100 – 192.168.50.199, mask /24;
   - **exclusions**: 192.168.50.100–192.168.50.109 (for future static devices);
   - **lease duration**: 8 hours (short for a computer lab, 8 days for an office);
   - **options**: 003 Router = 192.168.50.1, 006 DNS Servers = 192.168.50.10, 015 DNS Domain Name = `aifu.local`;
   - **activate** the scope.
3. **Reservation**: Scope → Reservations → New → the printer's MAC address and 192.168.50.150.

The PowerShell equivalent:

```text
Install-WindowsFeature DHCP -IncludeManagementTools
Add-DhcpServerv4Scope -Name "Talabalar-LAN" -StartRange 192.168.50.100 -EndRange 192.168.50.199 -SubnetMask 255.255.255.0 -LeaseDuration 08:00:00
Add-DhcpServerv4ExclusionRange -ScopeId 192.168.50.0 -StartRange 192.168.50.100 -EndRange 192.168.50.109
Set-DhcpServerv4OptionValue -ScopeId 192.168.50.0 -Router 192.168.50.1 -DnsServer 192.168.50.10 -DnsDomain aifu.local
Add-DhcpServerv4Reservation -ScopeId 192.168.50.0 -IPAddress 192.168.50.150 -ClientId "00-1A-2B-3C-4D-5E" -Name "Printer"
Get-DhcpServerv4Lease -ScopeId 192.168.50.0
```

## Configuring the DNS server

1. **Add Roles and Features** → **DNS Server** (on a domain controller it is installed automatically with AD).
2. **Tools → DNS** → **Forward Lookup Zones** → New Zone → Primary → `aifu.local`.
3. Add records:
   - **A**: `srv-dc01` → 192.168.50.10; `printer` → 192.168.50.150;
   - **CNAME**: `files` → `srv-dc01.aifu.local`;
   - **MX**: `aifu.local` → `mail.aifu.local` (priority 10).
4. **Reverse Lookup Zones** → New Zone → `50.168.192.in-addr.arpa` → **PTR** records (the "Create associated PTR record" box when creating an A record).
5. **Forwarders** (server properties): the provider's DNS or 1.1.1.1 for names not in the internal zone.

```text
Install-WindowsFeature DNS -IncludeManagementTools
Add-DnsServerPrimaryZone -Name "aifu.local" -ZoneFile "aifu.local.dns"
Add-DnsServerPrimaryZone -NetworkId "192.168.50.0/24" -ZoneFile "50.168.192.in-addr.arpa.dns"
Add-DnsServerResourceRecordA -ZoneName "aifu.local" -Name "srv-dc01" -IPv4Address 192.168.50.10 -CreatePtr
Add-DnsServerResourceRecordCName -ZoneName "aifu.local" -Name "files" -HostNameAlias "srv-dc01.aifu.local"
Set-DnsServerForwarder -IPAddress 1.1.1.1
```

DHCP can be linked with DNS: when the DHCP server gives a client an address, it writes the client's name into DNS automatically (**dynamic update**) — then `talaba-pc07.aifu.local` resolves too.

## Checking on the client

```text
ipconfig /release
ipconfig /renew
ipconfig /all              address, mask, gateway, DNS, DHCP server, lease time
nslookup srv-dc01.aifu.local
nslookup files.aifu.local
nslookup 192.168.50.10     does the reverse zone (PTR) work
```

The client's lease should appear under **Address Leases** on the server. If the client gets a 169.254.x.x address, no DHCP server was found (the scope is not active, there is no authorisation, or the VMs are on different networks).

## Worked example: modelling a DHCP pool

The first line holds the pool start, the pool end (within one /24) and the lease duration (seconds). Then the number of exclusions and their addresses, the number of reservations and "MAC address" pairs, then the number of events and each event: time, MAC, `DISCOVER` or `RELEASE`. The server's rules:

- a client with a reservation always gets its own address;
- a client whose lease has not yet expired gets the same address again (the lease is extended);
- otherwise — the **smallest** address that is not excluded, reserved or taken; expired leases are freed;
- if there is no free address — `no address`.

```python
def son(ip):
    return int(ip.split(".")[3])


boshi, oxiri, muddat = input().split()
tarmoq = boshi.rsplit(".", 1)[0] + "."
muddat = int(muddat)
istisno = {son(input().strip()) for _ in range(int(input()))}
rezerv = {}                                  # MAC -> last octet
for _ in range(int(input())):
    mac, ip = input().split()
    rezerv[mac] = son(ip)
ijara = {}                                   # octet -> (MAC, expiry time)
for _ in range(int(input())):
    vaqt, mac, tur = input().split()
    vaqt = int(vaqt)
    for oktet in [o for o, (m, t) in ijara.items() if t <= vaqt]:
        del ijara[oktet]                     # expired leases
    if tur == "RELEASE":
        for oktet, (m, t) in list(ijara.items()):
            if m == mac:
                del ijara[oktet]
                print(vaqt, mac, "RELEASE " + tarmoq + str(oktet))
        continue
    if mac in rezerv:
        print(vaqt, mac, "OFFER " + tarmoq + str(rezerv[mac]))
        continue
    tanlangan = next((o for o, (m, t) in ijara.items() if m == mac), None)
    if tanlangan is None:
        for o in range(son(boshi), son(oxiri) + 1):
            if o not in istisno and o not in rezerv.values() and o not in ijara:
                tanlangan = o
                break
    if tanlangan is None:
        print(vaqt, mac, "no address")
    else:
        ijara[tanlangan] = (mac, vaqt + muddat)
        print(vaqt, mac, "OFFER " + tarmoq + str(tanlangan))
```
```java
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;

public class Main {
    static int son(String ip) {
        return Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String boshi = in.next();
        String oxiri = in.next();
        int muddat = in.nextInt();
        String tarmoq = boshi.substring(0, boshi.lastIndexOf('.') + 1);
        Set<Integer> istisno = new HashSet<>();
        int n = in.nextInt();
        for (int i = 0; i < n; i++) istisno.add(son(in.next()));
        Map<String, Integer> rezerv = new HashMap<>();             // MAC -> last octet
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String mac = in.next();
            rezerv.put(mac, son(in.next()));
        }
        Map<Integer, String> egasi = new TreeMap<>();              // octet -> MAC
        Map<Integer, Integer> tugash = new HashMap<>();            // octet -> expiry time
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            int vaqt = in.nextInt();
            String mac = in.next();
            String tur = in.next();
            egasi.keySet().removeIf(o -> tugash.get(o) <= vaqt);    // expired leases
            if (tur.equals("RELEASE")) {
                for (Integer o : egasi.keySet().toArray(new Integer[0])) {
                    if (egasi.get(o).equals(mac)) {
                        egasi.remove(o);
                        System.out.println(vaqt + " " + mac + " RELEASE " + tarmoq + o);
                    }
                }
                continue;
            }
            if (rezerv.containsKey(mac)) {
                System.out.println(vaqt + " " + mac + " OFFER " + tarmoq + rezerv.get(mac));
                continue;
            }
            Integer tanlangan = null;
            for (Map.Entry<Integer, String> e : egasi.entrySet()) {
                if (e.getValue().equals(mac)) tanlangan = e.getKey();
            }
            if (tanlangan == null) {
                for (int o = son(boshi); o <= son(oxiri); o++) {
                    if (!istisno.contains(o) && !rezerv.containsValue(o) && !egasi.containsKey(o)) {
                        tanlangan = o;
                        break;
                    }
                }
            }
            if (tanlangan == null) {
                System.out.println(vaqt + " " + mac + " no address");
            } else {
                egasi.put(tanlangan, mac);
                tugash.put(tanlangan, vaqt + muddat);
                System.out.println(vaqt + " " + mac + " OFFER " + tarmoq + tanlangan);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <utility>
using namespace std;

int son(const string& ip) {
    return stoi(ip.substr(ip.rfind('.') + 1));
}

int main() {
    string boshi, oxiri;
    int muddat, n;
    cin >> boshi >> oxiri >> muddat;
    string tarmoq = boshi.substr(0, boshi.rfind('.') + 1);
    set<int> istisno;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string ip;
        cin >> ip;
        istisno.insert(son(ip));
    }
    map<string, int> rezerv;                                    // MAC -> last octet
    set<int> rezervlangan;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string mac, ip;
        cin >> mac >> ip;
        rezerv[mac] = son(ip);
        rezervlangan.insert(son(ip));
    }
    map<int, pair<string, int>> ijara;                          // octet -> (MAC, expiry time)
    cin >> n;
    for (int i = 0; i < n; i++) {
        int vaqt;
        string mac, tur;
        cin >> vaqt >> mac >> tur;
        for (auto it = ijara.begin(); it != ijara.end();) {
            if (it->second.second <= vaqt) it = ijara.erase(it);    // expired leases
            else ++it;
        }
        if (tur == "RELEASE") {
            for (auto it = ijara.begin(); it != ijara.end();) {
                if (it->second.first == mac) {
                    cout << vaqt << " " << mac << " RELEASE " << tarmoq << it->first << endl;
                    it = ijara.erase(it);
                } else {
                    ++it;
                }
            }
            continue;
        }
        if (rezerv.count(mac)) {
            cout << vaqt << " " << mac << " OFFER " << tarmoq << rezerv[mac] << endl;
            continue;
        }
        int tanlangan = -1;
        for (auto& e : ijara) {
            if (e.second.first == mac) tanlangan = e.first;
        }
        if (tanlangan < 0) {
            for (int o = son(boshi); o <= son(oxiri); o++) {
                if (!istisno.count(o) && !rezervlangan.count(o) && !ijara.count(o)) {
                    tanlangan = o;
                    break;
                }
            }
        }
        if (tanlangan < 0) {
            cout << vaqt << " " << mac << " no address" << endl;
        } else {
            ijara[tanlangan] = {mac, vaqt + muddat};
            cout << vaqt << " " << mac << " OFFER " << tarmoq << tanlangan << endl;
        }
    }
    return 0;
}
```

Input (pool .100–.104, 60 s lease, .101 excluded, .104 reserved for the printer):

```text
192.168.50.100 192.168.50.104 60
1
192.168.50.101
1
AA:AA 192.168.50.104
9
0 BB:01 DISCOVER
5 BB:02 DISCOVER
10 AA:AA DISCOVER
20 BB:03 DISCOVER
30 BB:01 RELEASE
35 BB:04 DISCOVER
50 BB:03 DISCOVER
66 BB:05 DISCOVER
70 BB:02 DISCOVER
```

Output:

```text
0 BB:01 OFFER 192.168.50.100
5 BB:02 OFFER 192.168.50.102
10 AA:AA OFFER 192.168.50.104
20 BB:03 OFFER 192.168.50.103
30 BB:01 RELEASE 192.168.50.100
35 BB:04 OFFER 192.168.50.100
50 BB:03 OFFER 192.168.50.103
66 BB:05 OFFER 192.168.50.102
70 BB:02 no address
```

Note: at second 50 BB:03 extended its lease (its expiry moved to 110). BB:02's lease ran out at second 65 and it did not renew — its address .102 went to BB:05 at second 66. When BB:02 came back at second 70, no free address was left in the pool: .100 — BB:04, .102 — BB:05, .103 — BB:03, .101 excluded, .104 reserved for the printer. On a real network this means the pool is too small or the lease too long.

:::warning
**Common mistakes**

- **Not authorising the DHCP server or not activating the scope.** Clients get 169.254.x.x.
- **Not excluding the range of statically addressed devices.** DHCP gives that address to another computer and an IP conflict arises.
- **Giving an external DNS in option 006.** Internal names (`aifu.local`) are not resolved; clients get the internal DNS, and a forwarder is configured on the server for external names.
- **Forgetting the reverse zone.** `nslookup` cannot find a name by IP and some services slow down.
- **Running two DHCP servers with the same pool.** They may give one address to two clients; for redundancy **DHCP Failover** is used.
:::

:::tip
Start Wireshark on the client VM and run `ipconfig /release` and `ipconfig /renew`. With the `dhcp` filter find the four DORA messages and compare the options 003, 006, 015 and 051 (lease time) in the Offer with the values you configured.
:::

## Summary

- DHCP: install the role → authorise → scope (pool, exclusions, lease) → options (003, 006, 015) → activate → reservations.
- DNS: install the role → forward zone (`aifu.local`) → reverse zone → A, CNAME, MX, PTR records → forwarders.
- Everything can also be done with PowerShell commands such as `Add-DhcpServerv4Scope` and `Add-DnsServerPrimaryZone`.
- Checking: `ipconfig /all`, `ipconfig /renew`, `nslookup`, Address Leases on the server.
- A DHCP server chooses in the order reservation → current lease → the smallest free address.

## Check yourself

:::check
Q: What do scope options 003 and 006 provide?
A: 003 — the default gateway (router), 006 — the DNS server address.
Q: What is the difference between an exclusion and a reservation?
A: DHCP never gives out an excluded address; a reserved address is given only to the device with the specified MAC.
Q: Why might `nslookup srv-dc01` fail after the client has got an address?
A: The client was given an external DNS rather than the internal one, or there is no A record in the `aifu.local` zone.
Q: What is a reverse lookup zone for?
A: For finding a name by IP address (PTR records).
Q: What is the advantage of a short lease duration?
A: Where clients change often (a computer lab, Wi-Fi), addresses are freed faster.
:::

:::link https://learn.microsoft.com/en-us/windows-server/networking/technologies/dhcp/dhcp-top
DHCP in Windows Server
Microsoft's official documentation on the DHCP server.
:::
