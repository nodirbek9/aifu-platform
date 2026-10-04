---
title: Lokal tarmoq texnologiyalari Ethernet, Fast Ethernet. Tarmoq snifferlari yordamida paketlarni tahlil qilish
summary: Ethernet va Fast Ethernet ning amaliy parametrlari (100BASE-TX, 4B/5B + MLT-3, avtomatik kelishuv); tarmoq snifferlari — Wireshark, tcpdump; tinglash rejimi (promiscuous) va kommutatorda port nusxalash (SPAN); ushlash va koʻrsatish filtrlari; kadrni oʻn oltilik koʻrinishdan qoʻlda tahlil qilish, VLAN tegi; trafik statistikasi; snifferdan foydalanishning huquqiy chegaralari.
---

## Nima uchun bu mavzu muhim?

"Internet sekin", "printer topilmayapti", "sayt ochilmayapti" — bunday muammolarning sababini taxmin qilish emas, **koʻrish** kerak. Buning uchun tarmoq muhandisi **sniffer** (paket analizatori) ishlatadi: u kabeldan oʻtayotgan har bir kadrni ushlab, pogʻonalar boʻyicha ochib koʻrsatadi. Bu mashgʻulotda Wireshark bilan ishlashni oʻrganamiz va sniffer ichida nima boʻlayotganini tushunish uchun kadrni dastur yordamida qoʻlda "ochamiz".

:::note
**Bu mashgʻulotdan soʻng siz:**

- Fast Ethernet (100BASE-TX) ning asosiy texnik parametrlarini ayta olasiz;
- Wireshark da trafikni ushlab, koʻrsatish filtrlari bilan kerakli paketlarni ajrata olasiz;
- Ethernet kadrini oʻn oltilik koʻrinishdan qoʻlda tahlil qilib, manzillar, VLAN tegi va EtherType ni aniqlay olasiz;
- ushlangan trafik boʻyicha protokollar statistikasini tuza olasiz;
- snifferdan foydalanishning huquqiy va axloqiy chegaralarini bilasiz.
:::

## Ethernet va Fast Ethernet amalda

| Parametr | 10BASE-T | 100BASE-TX (Fast Ethernet) | 1000BASE-T |
|---|---|---|---|
| Kabel | Cat3 | Cat5 | Cat5e |
| Ishlatiladigan juftliklar | 2 (1-2, 3-6) | 2 (1-2, 3-6) | 4 (hammasi, ikki yoʻnalishda) |
| Kodlash | Manchester | 4B/5B + MLT-3 | PAM-5 |
| Liniya chastotasi | 10 MHz | 125 Mbod | 125 Mbod × 4 juftlik |
| Segment | 100 m | 100 m | 100 m |

**Avtomatik kelishuv** (autonegotiation): ulanish oʻrnatilganda adapter va kommutator porti oʻz imkoniyatlarini (10/100/1000, half/full) maxsus impulslar bilan eʼlon qiladi va ikkalasi ham qoʻllaydigan eng yaxshi rejim tanlanadi. Kommutator portining `show interfaces` chiqishida tezlik va dupleks koʻrinadi.

## Snifferlar qanday ishlaydi

Odatda tarmoq adapteri faqat **oʻziga** (va broadkast hamda obuna boʻlgan multicast manzillarga) yuborilgan kadrlarni qabul qiladi. Sniffer adapterni **tinglash rejimiga** (promiscuous mode) oʻtkazadi — u segmentdagi **barcha** kadrlarni qabul qiladi.

Lekin kommutatorli tarmoqda kommutator boshqa kompyuterlarning unicast kadrlarini sizning portingizga yubormaydi. Shuning uchun:

- **Oʻz trafigingizni** — oʻz kompyuteringizda ushlang;
- **Boshqa qurilma trafigini** — kommutatorda **port nusxalash** (SPAN, port mirroring) sozlanadi: tanlangan port trafigining nusxasi sniffer ulangan portga yuboriladi;
- Yoki ikki qurilma orasiga **TAP** (maxsus ajratgich) qoʻyiladi.

| Vosita | Xususiyati |
|---|---|
| **Wireshark** | grafik interfeys, 3000 dan ortiq protokol dekoderi, statistika |
| **tcpdump** | Linux/macOS buyruqlar satri, serverlarda; `.pcap` faylga yozadi |
| **tshark** | Wireshark ning buyruqlar satri versiyasi |

## Ushlash va koʻrsatish filtrlari

**Ushlash filtri** (capture filter, BPF sintaksisi) — faqat mos paketlar yoziladi; **koʻrsatish filtri** (display filter) — yozilganlar orasidan koʻrsatiladiganlari tanlanadi.

```text
Ushlash filtrlari (BPF):            Koʻrsatish filtrlari (Wireshark):
  host 192.168.1.10                   ip.addr == 192.168.1.10
  port 53                             dns
  arp                                 arp
  tcp and port 443                    tcp.port == 443
  not port 22                         eth.dst == ff:ff:ff:ff:ff:ff
                                      tcp.flags.syn == 1 && tcp.flags.ack == 0
```

## Amaliy ish: Wireshark bilan birinchi ushlash

1. Wireshark ni ishga tushiring, faol interfeysni (Wi-Fi yoki Ethernet) tanlab, ushlashni boshlang.
2. Buyruqlar satrida `ipconfig /flushdns` va `ping aifu.uz` ni bajaring, brauzerda istalgan saytni oching. Ushlashni toʻxtating.
3. Filtr `dns` — domen nomi soʻrovi va javobini toping. Javobdagi IP manzilni yozing.
4. Filtr `icmp` — `ping` paketlari. Bitta paketni tanlab, pastki panelda **Ethernet II** qismini oching: qabul qiluvchi va yuboruvchi MAC, Type = IPv4 (0x0800).
5. Filtr `arp` — "Who has 192.168.1.1? Tell 192.168.1.10" soʻrovi qaysi MAC manzilga yuborilgan? (broadkast).
6. **Statistics → Protocol Hierarchy** va **Statistics → Conversations** oynalarida qaysi protokol va qaysi manzil eng koʻp trafik hosil qilganini aniqlang.
7. Eng pastki panelda (baytlar) kadrning birinchi 14 baytini belgilang — bu Ethernet sarlavhasi. Uni quyidagi birinchi misol natijasi bilan solishtiring.

## Ishlangan misol 1: kadrni qoʻlda tahlil qilish

Kirishda ushlangan kadr baytlari oʻn oltilik koʻrinishda (boʻshliq va qator oxiri bilan ajratilgan; FCS ushlanmagan). Qabul qiluvchi va yuboruvchi MAC manzillarini, VLAN tegi boʻlsa (`0x8100`) VLAN raqamini, EtherType ni (protokol nomi bilan) va maʼlumot qismi uzunligini chiqaring.

```python
import sys

baytlar = [int(t, 16) for t in sys.stdin.read().split()]


def mac(b):
    return ":".join(format(x, "02X") for x in b)


print("qabul qiluvchi:", mac(baytlar[0:6]))
print("yuboruvchi:", mac(baytlar[6:12]))
joy = 12
tur = baytlar[joy] * 256 + baytlar[joy + 1]
if tur == 0x8100:                              # 802.1Q VLAN tegi: 4 bayt
    vlan = (baytlar[joy + 2] * 256 + baytlar[joy + 3]) & 0x0FFF
    print("VLAN:", vlan)
    joy += 4
    tur = baytlar[joy] * 256 + baytlar[joy + 1]
nomlar = {0x0800: "IPv4", 0x0806: "ARP", 0x86DD: "IPv6"}
print("EtherType: 0x" + format(tur, "04X") + " (" + nomlar.get(tur, "nomaʼlum") + ")")
print("maʼlumot:", len(baytlar) - joy - 2, "bayt")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String mac(List<Integer> b, int boshi) {
        StringBuilder s = new StringBuilder();
        for (int i = boshi; i < boshi + 6; i++) {
            if (i > boshi) s.append(":");
            s.append(String.format("%02X", b.get(i)));
        }
        return s.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> baytlar = new ArrayList<>();
        while (in.hasNext()) baytlar.add(Integer.parseInt(in.next(), 16));

        System.out.println("qabul qiluvchi: " + mac(baytlar, 0));
        System.out.println("yuboruvchi: " + mac(baytlar, 6));
        int joy = 12;
        int tur = baytlar.get(joy) * 256 + baytlar.get(joy + 1);
        if (tur == 0x8100) {                                   // 802.1Q VLAN tegi: 4 bayt
            int vlan = (baytlar.get(joy + 2) * 256 + baytlar.get(joy + 3)) & 0x0FFF;
            System.out.println("VLAN: " + vlan);
            joy += 4;
            tur = baytlar.get(joy) * 256 + baytlar.get(joy + 1);
        }
        String nom = tur == 0x0800 ? "IPv4" : tur == 0x0806 ? "ARP" : tur == 0x86DD ? "IPv6" : "nomaʼlum";
        System.out.println("EtherType: 0x" + String.format("%04X", tur) + " (" + nom + ")");
        System.out.println("maʼlumot: " + (baytlar.size() - joy - 2) + " bayt");
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

string mac(const vector<int>& b, int boshi) {
    string s;
    char buf[4];
    for (int i = boshi; i < boshi + 6; i++) {
        snprintf(buf, sizeof buf, "%02X", b[i]);
        s += (i > boshi ? ":" : "") + string(buf);
    }
    return s;
}

int main() {
    vector<int> baytlar;
    string t;
    while (cin >> t) baytlar.push_back(stoi(t, nullptr, 16));

    cout << "qabul qiluvchi: " << mac(baytlar, 0) << endl;
    cout << "yuboruvchi: " << mac(baytlar, 6) << endl;
    int joy = 12;
    int tur = baytlar[joy] * 256 + baytlar[joy + 1];
    if (tur == 0x8100) {                                       // 802.1Q VLAN tegi: 4 bayt
        int vlan = (baytlar[joy + 2] * 256 + baytlar[joy + 3]) & 0x0FFF;
        cout << "VLAN: " << vlan << endl;
        joy += 4;
        tur = baytlar[joy] * 256 + baytlar[joy + 1];
    }
    string nom = tur == 0x0800 ? "IPv4" : tur == 0x0806 ? "ARP" : tur == 0x86DD ? "IPv6" : "nomaʼlum";
    char hex[8];
    snprintf(hex, sizeof hex, "%04X", tur);
    cout << "EtherType: 0x" << hex << " (" << nom << ")" << endl;
    cout << "maʼlumot: " << baytlar.size() - joy - 2 << " bayt" << endl;
    return 0;
}
```

Kirish (ARP soʻrovi):

```text
ff ff ff ff ff ff 3c 52 82 1a 9f 07 08 06
00 01 08 00 06 04 00 01 3c 52 82 1a 9f 07
c0 a8 01 0a 00 00 00 00 00 00 c0 a8 01 01
```

Chiqish:

```text
qabul qiluvchi: FF:FF:FF:FF:FF:FF
yuboruvchi: 3C:52:82:1A:9F:07
EtherType: 0x0806 (ARP)
maʼlumot: 28 bayt
```

Kirish (10-VLAN ga tegishli IPv4 kadri):

```text
00 1a 2b 3c 4d 5e 3c 52 82 1a 9f 07 81 00 00 0a 08 00
45 00 00 14 00 01 00 00 40 06 00 00 c0 a8 01 0a 08 08 08 08
```

Chiqish:

```text
qabul qiluvchi: 00:1A:2B:3C:4D:5E
yuboruvchi: 3C:52:82:1A:9F:07
VLAN: 10
EtherType: 0x0800 (IPv4)
maʼlumot: 20 bayt
```

ARP soʻrovidagi `c0 a8 01 01` — bu soʻralayotgan IP manzil 192.168.1.1 (shlyuz). IPv4 sarlavhasini maydonlarga ajratishni M7 maʼruzasida davom ettiramiz.

## Ishlangan misol 2: protokollar statistikasi

Birinchi satrda *n*, keyingi *n* satrda ushlangan paketlarning qisqa yozuvi: yuboruvchi IP, qabul qiluvchi IP, protokol va hajm (bayt). Har bir protokol uchun paketlar soni, baytlar va umumiy hajmdagi ulushini (%, bir xona) baytlar kamayishi tartibida chiqaring (teng boʻlsa — nom boʻyicha); oxirida eng koʻp bayt yuborgan manzilni chiqaring (teng boʻlsa — kichik satr).

```python
n = int(input())
protokollar = {}                      # nom -> [paketlar, baytlar]
yuboruvchilar = {}
jami = 0
for _ in range(n):
    manba, manzil, protokol, hajm = input().split()
    hajm = int(hajm)
    jami += hajm
    p = protokollar.setdefault(protokol, [0, 0])
    p[0] += 1
    p[1] += hajm
    yuboruvchilar[manba] = yuboruvchilar.get(manba, 0) + hajm
for nom in sorted(protokollar, key=lambda x: (-protokollar[x][1], x)):
    paket, bayt = protokollar[nom]
    print(nom, paket, bayt, f"{bayt * 100 / jami:.1f}%")
eng = min(yuboruvchilar, key=lambda x: (-yuboruvchilar[x], x))
print("eng faol yuboruvchi:", eng, yuboruvchilar[eng])
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Map<String, long[]> protokollar = new TreeMap<>();     // nom -> {paketlar, baytlar}
        Map<String, Long> yuboruvchilar = new TreeMap<>();
        long jami = 0;
        for (int i = 0; i < n; i++) {
            String manba = in.next();
            String manzil = in.next();
            String protokol = in.next();
            long hajm = in.nextLong();
            jami += hajm;
            long[] p = protokollar.computeIfAbsent(protokol, k -> new long[2]);
            p[0]++;
            p[1] += hajm;
            yuboruvchilar.merge(manba, hajm, Long::sum);
        }
        List<String> nomlar = new ArrayList<>(protokollar.keySet());
        nomlar.sort((a, b) -> Long.compare(protokollar.get(b)[1], protokollar.get(a)[1]));   // TreeMap tartibi teng holatda saqlanadi
        for (String nom : nomlar) {
            long[] p = protokollar.get(nom);
            System.out.println(nom + " " + p[0] + " " + p[1] + " " + String.format("%.1f", p[1] * 100.0 / jami) + "%");
        }
        String eng = null;
        for (Map.Entry<String, Long> e : yuboruvchilar.entrySet()) {
            if (eng == null || e.getValue() > yuboruvchilar.get(eng)) eng = e.getKey();
        }
        System.out.println("eng faol yuboruvchi: " + eng + " " + yuboruvchilar.get(eng));
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    map<string, pair<long long, long long>> protokollar;   // nom -> {paketlar, baytlar}
    map<string, long long> yuboruvchilar;
    long long jami = 0;
    for (int i = 0; i < n; i++) {
        string manba, manzil, protokol;
        long long hajm;
        cin >> manba >> manzil >> protokol >> hajm;
        jami += hajm;
        protokollar[protokol].first++;
        protokollar[protokol].second += hajm;
        yuboruvchilar[manba] += hajm;
    }
    vector<string> nomlar;
    for (auto& e : protokollar) nomlar.push_back(e.first);
    stable_sort(nomlar.begin(), nomlar.end(), [&](const string& a, const string& b) {
        return protokollar[a].second > protokollar[b].second;
    });
    for (auto& nom : nomlar) {
        auto p = protokollar[nom];
        printf("%s %lld %lld %.1f%%\n", nom.c_str(), p.first, p.second, p.second * 100.0 / jami);
    }
    string eng;
    for (auto& e : yuboruvchilar) {
        if (eng.empty() || e.second > yuboruvchilar[eng]) eng = e.first;
    }
    cout << "eng faol yuboruvchi: " << eng << " " << yuboruvchilar[eng] << endl;
    return 0;
}
```

Kirish:

```text
6
192.168.1.10 142.250.74.46 TCP 1500
142.250.74.46 192.168.1.10 TCP 1500
192.168.1.10 8.8.8.8 DNS 80
8.8.8.8 192.168.1.10 DNS 120
192.168.1.10 192.168.1.255 UDP 300
192.168.1.20 192.168.1.10 TCP 500
```

Chiqish:

```text
TCP 3 3500 87.5%
UDP 1 300 7.5%
DNS 2 200 5.0%
eng faol yuboruvchi: 192.168.1.10 1880
```

Bu Wireshark dagi **Protocol Hierarchy** va **Conversations** oynalarining soddalashtirilgan varianti. `192.168.1.255` ga yuborilgan UDP paket — quyi tarmoq broadkasti.

:::warning
**Koʻp uchraydigan xatolar**

- **Kommutatorli tarmoqda boshqa kompyuterlar trafigini koʻraman deb kutish.** Port nusxalash (SPAN) sozlanmasa, faqat oʻz trafigingiz, broadkast va multicast koʻrinadi.
- **Ushlash va koʻrsatish filtrlarini aralashtirish.** `ip.addr == 10.0.0.1` — koʻrsatish filtri; ushlash filtrida `host 10.0.0.1` yoziladi.
- **VLAN tegini hisobga olmaslik.** Tegli kadrda EtherType 12-baytda emas, 16-baytda boshlanadi; `0x8100` ni protokol deb oʻqish xato.
- **Boshqalarning trafigini ruxsatsiz ushlash.** Bu qonun buzilishi boʻlishi mumkin. Snifferni faqat oʻz kompyuteringizda, laboratoriya tarmogʻida yoki tarmoq egasining yozma ruxsati bilan ishlating.
:::

:::tip
Wireshark da istalgan TCP paketni oʻng tugma bilan bosib, **Follow → TCP Stream** ni tanlang: butun suhbat bir oynada koʻrinadi. HTTP (shifrlanmagan) sayt bilan buni sinab koʻring va nima uchun HTTPS kerakligini oʻzingiz koʻrasiz.
:::

## Xulosa

- Fast Ethernet (100BASE-TX): Cat5, ikki juftlik, 4B/5B + MLT-3, 100 m; tezlik va dupleks avtomatik kelishiladi.
- Sniffer adapterni tinglash rejimiga oʻtkazadi; kommutatorli tarmoqda boshqa portlar trafigi uchun SPAN yoki TAP kerak.
- Ushlash filtrlari (BPF) yoziladigan paketlarni, koʻrsatish filtrlari koʻrinadiganlarini tanlaydi.
- Ethernet sarlavhasi: 6 + 6 bayt MAC, ixtiyoriy 4 baytli VLAN tegi, 2 bayt EtherType.
- Trafik statistikasi qaysi protokol va qurilma tarmoqni band qilayotganini koʻrsatadi.

## Oʻzingizni tekshiring

:::check
Q: Tinglash rejimi (promiscuous mode) nima?
A: Adapter faqat oʻziga moʻljallangan emas, segmentdagi barcha kadrlarni qabul qiladigan rejim.
Q: Kommutatorli tarmoqda boshqa kompyuter trafigini ushlash uchun nima qilish kerak?
A: Kommutatorda port nusxalashni (SPAN) sozlash yoki TAP qurilmasini qoʻyish.
Q: Kadrda 12–13-baytlar `81 00` boʻlsa, bu nimani bildiradi?
A: 802.1Q VLAN tegi bor; keyingi 2 baytda VLAN raqami, haqiqiy EtherType esa undan keyin.
Q: Faqat DNS paketlarini koʻrsatish uchun Wireshark da qanday filtr yoziladi?
A: `dns` (yoki `udp.port == 53`).
Q: 100BASE-TX da qaysi kodlash usullari ishlatiladi?
A: Mantiqiy 4B/5B va chiziqli MLT-3.
:::

:::link https://www.wireshark.org/docs/wsug_html_chunked/
Wireshark foydalanuvchi qoʻllanmasi
Ushlash, filtrlar va statistika boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
