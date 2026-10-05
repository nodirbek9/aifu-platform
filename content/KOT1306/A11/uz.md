---
title: Windows Server operatsion tizimida tarmoq xizmatlarini oʻrnatish va sozlash (DHCP, DNS)
summary: DHCP server rolini oʻrnatish va avtorizatsiya qilish; doira (scope), manzillar hovuzi, istisnolar, rezervatsiyalar, ijara muddati, opsiyalar (shlyuz, DNS); DNS server rolini oʻrnatish; toʻgʻri va teskari qidiruv zonalari, A, CNAME, PTR, MX yozuvlari, yoʻnaltiruvchilar (forwarders); PowerShell buyruqlari; mijozda tekshirish; DHCP hovuzini modellashtirish.
---

## Nima uchun bu mavzu muhim?

M9 va M12 maʼruzalarida DHCP va DNS qanday ishlashini oʻrgandik. Endi ularni oʻzimiz **sozlaymiz**: A10 da oʻrnatilgan `SRV-DC01` serveri tarmoqdagi kompyuterlarga avtomatik manzil beradi va ichki nomlarni (`srv-dc01.aifu.local`, `printer.aifu.local`) hal qiladi. Bu ikki xizmat — har qanday tashkilot tarmogʻining poydevori: ular ishlamasa, foydalanuvchilar uchun "hech narsa ishlamaydi".

:::note
**Bu mashgʻulotdan soʻng siz:**

- Windows Server da DHCP rolini oʻrnatib, doira, istisnolar, rezervatsiyalar va opsiyalarni sozlay olasiz;
- DNS rolini oʻrnatib, toʻgʻri va teskari zonalar hamda yozuvlar yarata olasiz;
- xuddi shu ishlarni PowerShell buyruqlari bilan bajara olasiz;
- mijoz kompyuterda `ipconfig` va `nslookup` bilan natijani tekshira olasiz;
- DHCP server manzillarni qanday tanlashi va ijaralarni qanday yuritishini tushuntira olasiz.
:::

## Laboratoriya sxemasi

| Qurilma | Manzil | Vazifa |
|---|---|---|
| `SRV-DC01` | 192.168.50.10/24 (statik) | DHCP va DNS server |
| Router (shlyuz) | 192.168.50.1 | Internetga chiqish |
| Mijozlar | 192.168.50.100–192.168.50.199 (DHCP) | foydalanuvchi kompyuterlari |
| Printer | 192.168.50.150 (rezervatsiya) | doimiy manzil, lekin DHCP orqali |

Barcha VM lar **ichki tarmoqda** — sinov DHCP serveri haqiqiy tarmoqqa chiqmasligi shart.

## DHCP serverni sozlash

1. Server Manager → **Add Roles and Features** → **DHCP Server**. Oʻrnatishdan soʻng sariq bayroqcha → **Complete DHCP configuration** (xavfsizlik guruhlarini yaratish; domenda — serverni AD da **avtorizatsiya qilish**: avtorizatsiyasiz DHCP server domen tarmogʻida ishlamaydi).
2. **Tools → DHCP** → IPv4 → **New Scope**:
   - nomi: `Talabalar-LAN`;
   - hovuz: 192.168.50.100 – 192.168.50.199, maska /24;
   - **istisnolar**: 192.168.50.100–192.168.50.109 (kelajakda statik qurilmalar uchun);
   - **ijara muddati**: 8 soat (kompyuter sinfi uchun qisqa, ofis uchun 8 kun);
   - **opsiyalar**: 003 Router = 192.168.50.1, 006 DNS Servers = 192.168.50.10, 015 DNS Domain Name = `aifu.local`;
   - doirani **faollashtirish** (Activate).
3. **Rezervatsiya**: Scope → Reservations → New → printer MAC manzili va 192.168.50.150.

PowerShell ekvivalenti:

```text
Install-WindowsFeature DHCP -IncludeManagementTools
Add-DhcpServerv4Scope -Name "Talabalar-LAN" -StartRange 192.168.50.100 -EndRange 192.168.50.199 -SubnetMask 255.255.255.0 -LeaseDuration 08:00:00
Add-DhcpServerv4ExclusionRange -ScopeId 192.168.50.0 -StartRange 192.168.50.100 -EndRange 192.168.50.109
Set-DhcpServerv4OptionValue -ScopeId 192.168.50.0 -Router 192.168.50.1 -DnsServer 192.168.50.10 -DnsDomain aifu.local
Add-DhcpServerv4Reservation -ScopeId 192.168.50.0 -IPAddress 192.168.50.150 -ClientId "00-1A-2B-3C-4D-5E" -Name "Printer"
Get-DhcpServerv4Lease -ScopeId 192.168.50.0
```

## DNS serverni sozlash

1. **Add Roles and Features** → **DNS Server** (domen kontrollerida AD bilan birga avtomatik oʻrnatiladi).
2. **Tools → DNS** → **Forward Lookup Zones** → New Zone → Primary → `aifu.local`.
3. Yozuvlar qoʻshish:
   - **A**: `srv-dc01` → 192.168.50.10; `printer` → 192.168.50.150;
   - **CNAME**: `files` → `srv-dc01.aifu.local`;
   - **MX**: `aifu.local` → `mail.aifu.local` (ustuvorlik 10).
4. **Reverse Lookup Zones** → New Zone → `50.168.192.in-addr.arpa` → **PTR** yozuvlari (A yozuvini yaratishda "Create associated PTR record" belgisi).
5. **Forwarders** (server xususiyatlari): ichki zonada boʻlmagan nomlar uchun provayder DNS yoki 1.1.1.1.

```text
Install-WindowsFeature DNS -IncludeManagementTools
Add-DnsServerPrimaryZone -Name "aifu.local" -ZoneFile "aifu.local.dns"
Add-DnsServerPrimaryZone -NetworkId "192.168.50.0/24" -ZoneFile "50.168.192.in-addr.arpa.dns"
Add-DnsServerResourceRecordA -ZoneName "aifu.local" -Name "srv-dc01" -IPv4Address 192.168.50.10 -CreatePtr
Add-DnsServerResourceRecordCName -ZoneName "aifu.local" -Name "files" -HostNameAlias "srv-dc01.aifu.local"
Set-DnsServerForwarder -IPAddress 1.1.1.1
```

DHCP ni DNS bilan bogʻlash mumkin: DHCP server mijozga manzil berganda uning nomini DNS ga avtomatik yozadi (**dinamik yangilash**) — shunda `talaba-pc07.aifu.local` ham hal qilinadi.

## Mijozda tekshirish

```text
ipconfig /release
ipconfig /renew
ipconfig /all              manzil, maska, shlyuz, DNS, DHCP server, ijara muddati
nslookup srv-dc01.aifu.local
nslookup files.aifu.local
nslookup 192.168.50.10     teskari zona (PTR) ishlayaptimi
```

Serverda **Address Leases** boʻlimida mijozning ijarasi paydo boʻlishi kerak. Mijoz 169.254.x.x manzil olsa — DHCP server topilmagan (doira faol emas, avtorizatsiya yoʻq yoki VM lar turli tarmoqda).

## Ishlangan misol: DHCP hovuzini modellashtirish

Birinchi satrda hovuz boshi, oxiri (bitta /24 ichida) va ijara muddati (soniya). Keyin istisnolar soni va manzillari, rezervatsiyalar soni va "MAC manzil" juftlari, soʻng hodisalar soni va har bir hodisa: vaqt, MAC, `DISCOVER` yoki `RELEASE`. Server qoidalari:

- rezervatsiyasi bor mijoz doim oʻz manzilini oladi;
- ijarasi hali tugamagan mijoz oʻsha manzilni qayta oladi (ijara uzaytiriladi);
- aks holda — istisno, rezervatsiya va band boʻlmagan **eng kichik** manzil; vaqti tugagan ijaralar boʻshatiladi;
- boʻsh manzil boʻlmasa — `manzil yoʻq`.

```python
def son(ip):
    return int(ip.split(".")[3])


boshi, oxiri, muddat = input().split()
tarmoq = boshi.rsplit(".", 1)[0] + "."
muddat = int(muddat)
istisno = {son(input().strip()) for _ in range(int(input()))}
rezerv = {}                                  # MAC -> oxirgi oktet
for _ in range(int(input())):
    mac, ip = input().split()
    rezerv[mac] = son(ip)
ijara = {}                                   # oktet -> (MAC, tugash vaqti)
for _ in range(int(input())):
    vaqt, mac, tur = input().split()
    vaqt = int(vaqt)
    for oktet in [o for o, (m, t) in ijara.items() if t <= vaqt]:
        del ijara[oktet]                     # muddati tugagan ijaralar
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
        print(vaqt, mac, "manzil yoʻq")
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
        Map<String, Integer> rezerv = new HashMap<>();             // MAC -> oxirgi oktet
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String mac = in.next();
            rezerv.put(mac, son(in.next()));
        }
        Map<Integer, String> egasi = new TreeMap<>();              // oktet -> MAC
        Map<Integer, Integer> tugash = new HashMap<>();            // oktet -> tugash vaqti
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            int vaqt = in.nextInt();
            String mac = in.next();
            String tur = in.next();
            egasi.keySet().removeIf(o -> tugash.get(o) <= vaqt);    // muddati tugagan ijaralar
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
                System.out.println(vaqt + " " + mac + " manzil yoʻq");
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
    map<string, int> rezerv;                                    // MAC -> oxirgi oktet
    set<int> rezervlangan;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string mac, ip;
        cin >> mac >> ip;
        rezerv[mac] = son(ip);
        rezervlangan.insert(son(ip));
    }
    map<int, pair<string, int>> ijara;                          // oktet -> (MAC, tugash vaqti)
    cin >> n;
    for (int i = 0; i < n; i++) {
        int vaqt;
        string mac, tur;
        cin >> vaqt >> mac >> tur;
        for (auto it = ijara.begin(); it != ijara.end();) {
            if (it->second.second <= vaqt) it = ijara.erase(it);    // muddati tugagan ijaralar
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
            cout << vaqt << " " << mac << " manzil yoʻq" << endl;
        } else {
            ijara[tanlangan] = {mac, vaqt + muddat};
            cout << vaqt << " " << mac << " OFFER " << tarmoq << tanlangan << endl;
        }
    }
    return 0;
}
```

Kirish (hovuz .100–.104, ijara 60 s, .101 istisno, .104 printer uchun rezerv):

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

Chiqish:

```text
0 BB:01 OFFER 192.168.50.100
5 BB:02 OFFER 192.168.50.102
10 AA:AA OFFER 192.168.50.104
20 BB:03 OFFER 192.168.50.103
30 BB:01 RELEASE 192.168.50.100
35 BB:04 OFFER 192.168.50.100
50 BB:03 OFFER 192.168.50.103
66 BB:05 OFFER 192.168.50.102
70 BB:02 manzil yoʻq
```

Kuzating: 50-soniyada BB:03 ijarani uzaytirdi (tugashi 110 ga koʻchdi). BB:02 ning ijarasi 65-soniyada tugadi va u yangilamadi — uning manzili .102 66-soniyada BB:05 ga berildi. 70-soniyada BB:02 qaytganda hovuzda boʻsh manzil qolmagan: .100 — BB:04, .102 — BB:05, .103 — BB:03, .101 istisno, .104 printerga rezerv. Haqiqiy tarmoqda bunday holat hovuz juda kichik yoki ijara muddati juda uzun ekanini bildiradi.

:::warning
**Koʻp uchraydigan xatolar**

- **DHCP serverni avtorizatsiya qilmaslik yoki doirani faollashtirmaslik.** Mijozlar 169.254.x.x oladi.
- **Statik manzilli qurilmalar diapazonini istisno qilmaslik.** DHCP shu manzilni boshqa kompyuterga berib, IP ziddiyati yuzaga keladi.
- **006 opsiyada tashqi DNS koʻrsatish.** Ichki nomlar (`aifu.local`) hal qilinmaydi; mijozlarga ichki DNS beriladi, tashqi nomlar uchun serverda forwarder sozlanadi.
- **Teskari zonani unutish.** `nslookup` IP boʻyicha nomni topa olmaydi va baʼzi xizmatlar sekinlashadi.
- **Ikki DHCP serverni bir xil hovuz bilan ishga tushirish.** Ular bir manzilni ikki mijozga berishi mumkin; zaxira uchun **DHCP Failover** ishlatiladi.
:::

:::tip
Mijoz VM da Wireshark ni ishga tushirib, `ipconfig /release` va `ipconfig /renew` ni bajaring. `dhcp` filtrida DORA ning toʻrt xabarini toping va Offer xabaridagi 003, 006, 015 va 051 (ijara muddati) opsiyalarini oʻzingiz sozlagan qiymatlar bilan solishtiring.
:::

## Xulosa

- DHCP: rolni oʻrnatish → avtorizatsiya → doira (hovuz, istisnolar, ijara) → opsiyalar (003, 006, 015) → faollashtirish → rezervatsiyalar.
- DNS: rolni oʻrnatish → toʻgʻri zona (`aifu.local`) → teskari zona → A, CNAME, MX, PTR yozuvlari → forwarderlar.
- Barcha amallarni `Add-DhcpServerv4Scope`, `Add-DnsServerPrimaryZone` kabi PowerShell buyruqlari bilan ham bajarish mumkin.
- Tekshirish: `ipconfig /all`, `ipconfig /renew`, `nslookup`, serverda Address Leases.
- DHCP server rezervatsiya → joriy ijara → eng kichik boʻsh manzil tartibida tanlaydi.

## Oʻzingizni tekshiring

:::check
Q: Doira opsiyalaridan 003 va 006 nimani beradi?
A: 003 — standart shlyuz (router), 006 — DNS server manzili.
Q: Istisno va rezervatsiyaning farqi nimada?
A: Istisno qilingan manzilni DHCP umuman bermaydi; rezervatsiya qilingan manzil faqat koʻrsatilgan MAC li qurilmaga beriladi.
Q: Mijoz manzil olgandan keyin nima uchun `nslookup srv-dc01` ishlamasligi mumkin?
A: Mijozga ichki DNS emas, tashqi DNS berilgan yoki `aifu.local` zonasida A yozuvi yoʻq.
Q: Teskari qidiruv zonasi nima uchun kerak?
A: IP manzil boʻyicha nomni topish (PTR yozuvlari) uchun.
Q: Ijara muddatini qisqa qilishning afzalligi qanday?
A: Mijozlar tez almashadigan joyda (kompyuter sinfi, Wi-Fi) manzillar tezroq boʻshaydi.
:::

:::link https://learn.microsoft.com/en-us/windows-server/networking/technologies/dhcp/dhcp-top
DHCP in Windows Server
Microsoft ning DHCP server boʻyicha rasmiy hujjatlari (ingliz tilida).
:::
