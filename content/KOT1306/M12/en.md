---
title: The Internet and the DNS service. The DNS service and how it works
summary: The structure of the Internet — providers, autonomous systems, exchange points; the hierarchy of domain names — the root, top-level domains (TLDs), zones; kinds of DNS servers; recursive and iterative queries; caching and TTL; resource records — A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; the DNS message and how names are encoded; nslookup and dig; DNS security.
---

## Why it matters

A person remembers `aifu.uz`, while a computer works with `185.x.x.x`. Every time a site is opened, a mail is sent or an app connects to a server, **DNS** (the Domain Name System) first turns the name into an address. If DNS fails, it feels as if "the Internet is down", even though every cable is in place. DNS is the Internet's "phone book" and its most important distributed database.

:::note
**After this lesson you will be able to:**

- explain the structure of the Internet as providers and autonomous systems;
- describe the hierarchy of domain names and the notion of a zone;
- explain step by step how a name is resolved through recursive and iterative queries;
- know the main resource record types and their purpose;
- check DNS records with `nslookup` or `dig`.
:::

## The structure of the Internet

The Internet is not a single network but a union of **autonomous systems** (ASes). Every AS — a provider's, university's or large company's network — has its own number (ASN). Providers fall into tiers: **Tier 1** — own backbones all over the world and exchange traffic with each other for free; **Tier 2** — regional; **Tier 3** — serve end users. Networks connect directly at **IXPs** (Internet Exchange Points) and exchange routes through **BGP**. Addresses and ASNs are distributed by **IANA** and the regional registries (for Uzbekistan, **RIPE NCC**).

## The hierarchy of domain names

```text
                              . (root)
          ┌──────────┬────────┼─────────┬──────────┐
         com        org      uz        net        edu        ← top-level domains (TLDs)
          │                  │
       google              aifu                                ← second level
          │                  │
         www         www   lms   mail                          ← hosts / subdomains
```

A name is read from right to left: `lms.aifu.uz.` — the root (`.`), the TLD `uz`, the domain `aifu`, the host `lms`. The trailing dot of a fully qualified name (FQDN) stands for the root. Kinds of TLD: **gTLD** (generic: .com, .org, .net, .info), **ccTLD** (country: .uz, .ru, .de), new gTLDs (.app, .dev). In Uzbekistan the `.uz` zone is run by **UZINFOCOM**.

A **zone** is the part of the name space one organisation manages. The owner of `aifu.uz` sets the records in their own zone, while the `uz` zone holds only "ask such-and-such servers about aifu.uz" (a **delegation**, NS records).

## DNS servers and name resolution

| Server | Role |
|---|---|
| **Recursive resolver** (provider, 8.8.8.8, 1.1.1.1) | finds the full answer for the client and caches it |
| **Root servers** | 13 names (a…m.root-servers.net), hundreds of copies worldwide; point to the TLD servers |
| **TLD servers** | point to a domain's authoritative servers |
| **Authoritative** server | the original source of a zone's records |

Resolving `lms.aifu.uz`:

1. The browser and the OS check their **cache** (and the `hosts` file).
2. The computer sends a **recursive** query to the recursive resolver: "give me the full answer".
3. If the resolver has nothing cached, it asks **iteratively**: the root → "ask such-and-such a server about `uz`"; the `uz` TLD server → "ask ns1.aifu.uz about `aifu.uz`"; the authoritative server → "`lms.aifu.uz` = 185.74.5.10".
4. The resolver returns the answer to the client and caches it for the record's **TTL**.

DNS queries usually go over **UDP 53**; large answers and zone transfers (AXFR) go over **TCP 53**.

## Resource records

| Type | Purpose | Example |
|---|---|---|
| **A** | name → IPv4 | `aifu.uz. A 185.74.5.10` |
| **AAAA** | name → IPv6 | `aifu.uz. AAAA 2a00:1d40::10` |
| **CNAME** | alias → canonical name | `www.aifu.uz. CNAME aifu.uz.` |
| **MX** | the domain's mail server (with a priority) | `aifu.uz. MX 10 mail.aifu.uz.` |
| **NS** | the zone's authoritative servers | `aifu.uz. NS ns1.aifu.uz.` |
| **PTR** | IP → name (reverse zone) | `10.5.74.185.in-addr.arpa. PTR aifu.uz.` |
| **TXT** | text: SPF, proof of domain ownership | `aifu.uz. TXT "v=spf1 mx -all"` |
| **SOA** | zone parameters: primary server, serial number, timers | — |

Every record's **TTL** (in seconds) says how long it may be cached. A small TTL — changes spread fast but the servers carry more load; a large TTL — the reverse.

## The DNS message and name encoding

A DNS message: a 12-byte header (ID, flags, numbers of questions and answers), then the **question** section and the **answer**, **authority** and **additional** sections. A name is encoded not as text but as a sequence of **labels**: each label is preceded by its length (1 byte), and at the end comes `00` for the root. A label is at most 63 bytes, the whole name at most 255.

## Worked example 1: encoding a name in DNS format

Each line holds a domain name. Print how it looks in a DNS message (the bytes in hexadecimal) and the number of bytes. If a label is empty or longer than 63 characters, or contains anything other than letters, digits and `-`, print `invalid`. A trailing dot (the root) is allowed.

```python
import sys

for nom in sys.stdin.read().split():
    toza = nom[:-1] if nom.endswith(".") else nom
    yorliqlar = toza.split(".")
    togri = all(0 < len(y) <= 63 and all(c.isascii() and (c.isalnum() or c == "-") for c in y)
                for y in yorliqlar)
    if not togri:
        print(nom, "invalid")
        continue
    baytlar = []
    for y in yorliqlar:
        baytlar.append(len(y))                 # label length
        baytlar.extend(ord(c) for c in y)
    baytlar.append(0)                          # the root
    print(nom, " ".join(format(b, "02x") for b in baytlar), "(" + str(len(baytlar)) + ")")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String toza = nom.endsWith(".") ? nom.substring(0, nom.length() - 1) : nom;
            String[] yorliqlar = toza.split("\\.", -1);
            boolean togri = true;
            for (String y : yorliqlar) {
                if (y.isEmpty() || y.length() > 63 || !y.matches("[A-Za-z0-9-]+")) togri = false;
            }
            if (!togri) {
                System.out.println(nom + " invalid");
                continue;
            }
            List<Integer> baytlar = new ArrayList<>();
            for (String y : yorliqlar) {
                baytlar.add(y.length());                       // label length
                for (char c : y.toCharArray()) baytlar.add((int) c);
            }
            baytlar.add(0);                                    // the root
            StringBuilder hex = new StringBuilder();
            for (int b : baytlar) hex.append(hex.length() > 0 ? " " : "").append(String.format("%02x", b));
            System.out.println(nom + " " + hex + " (" + baytlar.size() + ")");
        }
    }
}
```
```cpp
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string nom;
    while (cin >> nom) {
        string toza = !nom.empty() && nom.back() == '.' ? nom.substr(0, nom.size() - 1) : nom;
        vector<string> yorliqlar;
        size_t boshi = 0;
        while (true) {
            size_t nuqta = toza.find('.', boshi);
            yorliqlar.push_back(toza.substr(boshi, nuqta == string::npos ? string::npos : nuqta - boshi));
            if (nuqta == string::npos) break;
            boshi = nuqta + 1;
        }
        bool togri = true;
        for (auto& y : yorliqlar) {
            if (y.empty() || y.size() > 63) togri = false;
            for (unsigned char c : y) {
                if (!(isalnum(c) || c == '-') || c > 127) togri = false;
            }
        }
        if (!togri) {
            cout << nom << " invalid" << endl;
            continue;
        }
        vector<int> baytlar;
        for (auto& y : yorliqlar) {
            baytlar.push_back(y.size());                       // label length
            for (unsigned char c : y) baytlar.push_back(c);
        }
        baytlar.push_back(0);                                  // the root
        string hex;
        char buf[4];
        for (size_t i = 0; i < baytlar.size(); i++) {
            snprintf(buf, sizeof buf, "%02x", baytlar[i]);
            hex += (i ? " " : "") + string(buf);
        }
        cout << nom << " " << hex << " (" << baytlar.size() << ")" << endl;
    }
    return 0;
}
```

Input:

```text
www.aifu.uz
aifu.uz.
mail..aifu.uz
my_site.uz
```

Output:

```text
www.aifu.uz 03 77 77 77 04 61 69 66 75 02 75 7a 00 (13)
aifu.uz. 04 61 69 66 75 02 75 7a 00 (9)
mail..aifu.uz invalid
my_site.uz invalid
```

The dots are not written into the message — length bytes stand in their place. The `_` in `my_site` is not allowed in a host name (some service records, such as `_dmarc`, are the exception).

## Worked example 2: resolving a CNAME chain

The first line holds the number of records *k*, then *k* records: a name, a type (`A` or `CNAME`) and a value. Then come *q* queries — names. For each query follow the CNAME chain to an A record and print the path. If no record is found — `NXDOMAIN`; if the chain exceeds 8 steps — `loop`.

```python
k = int(input())
cname = {}
a = {}
for _ in range(k):
    nom, tur, qiymat = input().split()
    if tur == "CNAME":
        cname[nom] = qiymat
    else:
        a[nom] = qiymat
q = int(input())
for _ in range(q):
    nom = input().strip()
    yol = [nom]
    while nom in cname and len(yol) <= 8:
        nom = cname[nom]                       # from the alias to the canonical name
        yol.append(nom)
    if len(yol) > 8:
        print(" -> ".join(yol[:3]) + " ... loop")
    elif nom in a:
        print(" -> ".join(yol) + " = " + a[nom])
    else:
        print(" -> ".join(yol) + " NXDOMAIN")
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
        Map<String, String> cname = new HashMap<>();
        Map<String, String> a = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String nom = in.next();
            String tur = in.next();
            String qiymat = in.next();
            if (tur.equals("CNAME")) cname.put(nom, qiymat);
            else a.put(nom, qiymat);
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String nom = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(nom);
            while (cname.containsKey(nom) && yol.size() <= 8) {
                nom = cname.get(nom);                              // from the alias to the canonical name
                yol.add(nom);
            }
            if (yol.size() > 8) {
                System.out.println(String.join(" -> ", yol.subList(0, 3)) + " ... loop");
            } else if (a.containsKey(nom)) {
                System.out.println(String.join(" -> ", yol) + " = " + a.get(nom));
            } else {
                System.out.println(String.join(" -> ", yol) + " NXDOMAIN");
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

string birlashtir(const vector<string>& v, size_t n) {
    string s;
    for (size_t i = 0; i < n; i++) s += (i ? " -> " : "") + v[i];
    return s;
}

int main() {
    int k;
    cin >> k;
    map<string, string> cname, a;
    for (int i = 0; i < k; i++) {
        string nom, tur, qiymat;
        cin >> nom >> tur >> qiymat;
        if (tur == "CNAME") cname[nom] = qiymat;
        else a[nom] = qiymat;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string nom;
        cin >> nom;
        vector<string> yol = {nom};
        while (cname.count(nom) && yol.size() <= 8) {
            nom = cname[nom];                                      // from the alias to the canonical name
            yol.push_back(nom);
        }
        if (yol.size() > 8) {
            cout << birlashtir(yol, 3) << " ... loop" << endl;
        } else if (a.count(nom)) {
            cout << birlashtir(yol, yol.size()) << " = " << a[nom] << endl;
        } else {
            cout << birlashtir(yol, yol.size()) << " NXDOMAIN" << endl;
        }
    }
    return 0;
}
```

Input:

```text
6
aifu.uz A 185.74.5.10
www.aifu.uz CNAME aifu.uz
lms.aifu.uz CNAME cdn.aifu.uz
cdn.aifu.uz A 185.74.5.20
x.aifu.uz CNAME y.aifu.uz
y.aifu.uz CNAME x.aifu.uz
4
www.aifu.uz
lms.aifu.uz
eski.aifu.uz
x.aifu.uz
```

Output:

```text
www.aifu.uz -> aifu.uz = 185.74.5.10
lms.aifu.uz -> cdn.aifu.uz = 185.74.5.20
eski.aifu.uz NXDOMAIN
x.aifu.uz -> y.aifu.uz -> x.aifu.uz ... loop
```

Real resolvers also limit the length of a CNAME chain — otherwise a misconfigured zone would trap the resolver in an endless cycle. NXDOMAIN is the official DNS answer "no such name exists".

:::warning
**Common mistakes**

- **Thinking DNS works only over UDP.** Large answers, DNSSEC and zone transfers use TCP 53.
- **Expecting everyone to see the new address as soon as a record changes.** The old answer lives in caches until its TTL runs out.
- **Putting a CNAME on the domain itself (`aifu.uz`).** At the zone apex there are SOA and NS records, so a CNAME is not allowed there.
- **Mixing up recursive and iterative queries.** The client asks the resolver recursively (for a full answer); the resolver asks the root, TLD and authoritative servers iteratively.
- **Treating DNS as secure.** Plain DNS is unencrypted and can be forged; the protection is DNSSEC and DoH/DoT.
:::

:::tip
At the command line run `nslookup aifu.uz`, `nslookup -type=MX gmail.com`, `nslookup -type=NS uz` (on Linux/macOS `dig aifu.uz +trace` shows the whole iterative path). View the Windows DNS cache with `ipconfig /displaydns` and clear it with `ipconfig /flushdns`.
:::

## Summary

- The Internet is a hierarchy of autonomous systems and providers joined through BGP; direct exchange at IXPs.
- Domain names form the hierarchy root → TLD → domain → host; zones are delegated with NS records.
- Resolution: cache → recursive resolver → root → TLD → authoritative server; the answer is cached for its TTL.
- Records: A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; transport — UDP 53 (TCP 53 for large answers).
- A name is encoded in a message as length-prefixed labels: a label ≤ 63 bytes, `00` at the end.

## Check yourself

:::check
Q: What is the difference between recursive and iterative DNS queries?
A: With a recursive query the server finds the full answer for the client; with an iterative one each server says which server to ask next.
Q: Which record gives a domain's mail server?
A: The MX record.
Q: An A record with TTL = 3600 was changed. How long may the old address still be seen?
A: Up to an hour — an answer stored in a cache is used until its TTL runs out.
Q: Which bytes encode the name `mail.uz` in a DNS message?
A: `04 6d 61 69 6c 02 75 7a 00`.
Q: Which record is used to find a name from an IP address?
A: The PTR record (in the in-addr.arpa reverse zone).
:::

:::link https://www.cloudflare.com/learning/dns/what-is-dns/
Cloudflare: What is DNS?
An illustrated explanation of how DNS works.
:::
