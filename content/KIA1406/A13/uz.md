---
title: Tarmoq trafikini monitoring qilish (Wireshark yoki shunga oʻxshash vosita)
summary: Amaliy mashgʻulot — tarmoq trafikini himoya maqsadida kuzatish; huquqiy va axloqiy qoidalar; Wireshark oynalari; ushlash filtri va koʻrsatish filtri, foydali filtrlar (http, dns, tls, ip.addr, tcp.port); oqimni kuzatish; statistika — protokollar ierarxiyasi, suhbatlar, nuqtalar, I/O grafigi; shifrlanmagan protokollarni aniqlash va ularni xavfsiz muqobillar bilan almashtirish; anomaliya belgilari; tcpdump va oqim jurnallari (NetFlow); maxfiylik; oqimlar jurnalidan eng koʻp trafik yuborgan hostlar va shifrlanmagan ulanishlarni aniqlovchi dastur.
---

## Nima uchun bu mavzu muhim?

Tarmoq administratori va xavfsizlik mutaxassisi tarmoqda **nima sodir boʻlayotganini koʻra olishi** kerak: qaysi qurilma kim bilan aloqa qilyapti, qaysi protokollar ishlatilmoqda, parollar shifrlanmagan holda uzatilmayaptimi, odatdan tashqari faollik bormi. Trafik tahlili nosozliklarni topish, siyosatga rioya qilinishini tekshirish va hodisalarni aniqlash uchun asosiy vositadir (M14 dagi IDS va SIEM ham shunga tayanadi).

:::note
**Bu amaliy mashgʻulotdan soʻng siz:**

- trafikni faqat qonuniy va ruxsat etilgan sharoitda kuzatish qoidalarini bilasiz;
- Wireshark da trafikni ushlab, filtrlar bilan tahlil qila olasiz;
- statistika vositalari bilan tarmoq "rasmini" koʻra olasiz;
- shifrlanmagan protokollarni topib, ularni xavfsiz muqobillar bilan almashtirishni taklif qila olasiz;
- oqim jurnallarini dastur yordamida tahlil qila olasiz.
:::

:::warning
**Huquqiy va axloqiy qoidalar.** Trafikni faqat **oʻzingizning**, **oʻquv** yoki **yozma ruxsat berilgan** tarmoqda kuzating. Boshqalarning trafikini ruxsatsiz ushlash — shaxsiy hayot daxlsizligini buzish va qonunda javobgarlik nazarda tutilgan harakat (A3). Tashkilotda monitoring xavfsizlik siyosati bilan belgilanadi va xodimlar bundan xabardor qilinadi. Ushlangan fayllarda parollar va shaxsiy maʼlumotlar boʻlishi mumkin — ularni maxfiy maʼlumot sifatida saqlang va ishdan soʻng oʻchiring.
:::

## Wireshark bilan tanishish

**Wireshark** — bepul, ochiq kodli trafik tahlilchisi (Windows, Linux, macOS). Oynasi uch qismdan iborat:

| Qism | Nimani koʻrsatadi |
|---|---|
| **Paketlar roʻyxati** | har bir paket: vaqt, manba, manzil, protokol, uzunlik, qisqa izoh |
| **Paket tafsilotlari** | tanlangan paketning qatlamlar boʻyicha tuzilishi (Ethernet → IP → TCP/UDP → ilova) |
| **Baytlar** | paketning xom baytlari (oʻn oltilik va matn) |

**Tayyorgarlik**: ushlash uchun interfeysni tanlash (Wi-Fi yoki Ethernet). **Promiscuous rejim** — tarmoq kartasi faqat oʻziga emas, segmentdagi barcha kadrlarni qabul qiladi; zamonaviy kommutatorli tarmoqda baribir asosan oʻz trafigingiz koʻrinadi (butun segmentni kuzatish uchun kommutatorda port oynasi — SPAN sozlanadi, bu administrator vazifasi).

## Ushlash va koʻrsatish filtrlari

- **Ushlash filtri** (capture filter, BPF sintaksisi) — nimani yozib olishni ushlashdan **oldin** cheklaydi: `host 192.168.1.10`, `port 53`.
- **Koʻrsatish filtri** (display filter) — yozib olinganlardan nimani **koʻrsatishni** tanlaydi:

| Filtr | Nimani koʻrsatadi |
|---|---|
| `http` | HTTP trafigi |
| `dns` | DNS soʻrovlari va javoblari |
| `tls` | TLS (HTTPS) ulanishlari |
| `ip.addr == 192.168.1.10` | shu manzil ishtirokidagi paketlar |
| `tcp.port == 443` | 443-portdagi TCP |
| `tcp.flags.syn == 1 && tcp.flags.ack == 0` | yangi TCP ulanishlarining boshlanishi |
| `http.request.method == "POST"` | HTTP POST soʻrovlari |
| `!(arp or dns)` | ARP va DNS dan tashqari hammasi |

**Oqimni kuzatish** (Follow → TCP Stream) — bitta ulanishdagi butun suhbatni matn koʻrinishida koʻrsatadi.

## Statistika vositalari

| Menyu (Statistics) | Foydasi |
|---|---|
| **Protocol Hierarchy** | trafikning qaysi protokollar boʻyicha taqsimlanishi |
| **Conversations** | qaysi juftliklar orasida qancha maʼlumot uzatilgan |
| **Endpoints** | eng faol manzillar ("eng koʻp gapiruvchilar") |
| **I/O Graph** | vaqt boʻyicha trafik hajmi — keskin oʻsishlarni koʻrish |

## Amaliy ish

**Laboratoriya**: oʻz kompyuteringiz yoki oʻquv virtual mashinasi; oʻquv maqsadida HTTP va HTTPS da ishlaydigan **oʻzingizning test sahifangiz** (masalan, oʻquv veb-serverdagi kirish shakli, haqiqiy parol emas, `test123`).

1. Ushlashni boshlang, bir nechta saytni oching, keyin toʻxtating. **Protocol Hierarchy** da qaysi protokollar koʻp ekanini yozing.
2. `dns` filtri: qaysi domenlar soʻralgan? Notanish domenlar bormi?
3. Test sahifangizdagi kirish shaklini **HTTP** orqali yuboring, `http.request.method == "POST"` filtri va **Follow → TCP Stream** bilan soʻrovni koʻring: `test123` ochiq matnda koʻrinadi. Xuddi shuni **HTTPS** orqali takrorlang — `tls` filtri bilan faqat shifrlangan maʼlumot koʻrinadi. Xulosa: nima uchun barcha kirish sahifalari faqat HTTPS boʻlishi kerak.
4. **Conversations** va **Endpoints** bilan eng faol manzillarni toping.
5. Natijalarni hisobotga yozing (shaxsiy maʼlumotlarsiz) va ushlangan faylni oʻchiring.

## Shifrlanmagan protokollar va ularning muqobillari

| Shifrlanmagan | Port | Xavfsiz muqobil |
|---|---|---|
| HTTP | 80 | HTTPS (443) |
| FTP | 21 | SFTP yoki FTPS |
| Telnet | 23 | SSH (22) |
| POP3 / IMAP | 110 / 143 | POP3S / IMAPS (995 / 993) |
| SNMP v1/v2c | 161 | SNMPv3 |

Monitoringning muhim natijalaridan biri — shunday protokollarni tarmoqdan topib, ularni muqobillari bilan almashtirish rejasini tuzish.

## Anomaliya belgilari

- Notanish yoki shubhali domenlarga koʻp DNS soʻrovlari.
- Ish vaqtidan tashqari katta hajmdagi chiquvchi trafik.
- Ichki qurilmaning tashqi notanish manzil bilan muntazam qisqa aloqalari.
- Kutilmagan protokollar (masalan, IoT qurilmasida Telnet — M15).
- Bitta manzildan juda koʻp ulanish urinishlari.

Bunday belgilar topilsa, ular hodisaga javob jarayoni orqali (M14) tekshiriladi.

## tcpdump va oqim jurnallari

- **tcpdump** — buyruq satrida trafikni ushlash (serverlarda grafik interfeys boʻlmaganda): `sudo tcpdump -i eth0 port 53 -w dns.pcap` — natijani keyin Wireshark da ochish mumkin.
- **Oqim jurnallari** (NetFlow, IPFIX, bulutdagi flow logs) — har bir paket emas, ulanishlar haqidagi qisqa yozuvlar: kimdan, kimga, qaysi port, qancha bayt. Ular hajmi kichik, uzoq saqlanadi va katta tarmoqlarni kuzatish uchun qulay.

## Ishlangan misol: oqimlar jurnali tahlili

Har bir satrda ulanish: manba IP, manzil IP, manzil porti, baytlar. Har bir manba host uchun jami baytlarni kamayish tartibida chiqaring, keyin shifrlanmagan protokollardagi ulanishlarni (21 — FTP, 23 — Telnet, 80 — HTTP, 110 — POP3, 143 — IMAP) kirish tartibida koʻrsating.

```python
import sys

OCHIQ = {"21": "FTP", "23": "Telnet", "80": "HTTP", "110": "POP3", "143": "IMAP"}

jami, ogohlantirish = {}, []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    manba, manzil, port, hajm = satr.split()
    jami[manba] = jami.get(manba, 0) + int(hajm)
    if port in OCHIQ:                                 # shifrlanmagan protokol
        ogohlantirish.append(f"{manba} -> {manzil}:{port} ({OCHIQ[port]})")

for host, h in sorted(jami.items(), key=lambda x: (-x[1], x[0])):
    print(f"{host}: {h} bayt")
for o in ogohlantirish:
    print(f"Shifrlanmagan: {o}")
```
```java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Map<String, String> ochiq = Map.of("21", "FTP", "23", "Telnet", "80", "HTTP", "110", "POP3", "143", "IMAP");
        Scanner in = new Scanner(System.in);
        Map<String, Long> jami = new LinkedHashMap<>();
        List<String> ogohlantirish = new ArrayList<>();
        while (in.hasNext()) {
            String manba = in.next(), manzil = in.next(), port = in.next();
            long hajm = in.nextLong();
            jami.merge(manba, hajm, Long::sum);
            if (ochiq.containsKey(port))                          // shifrlanmagan protokol
                ogohlantirish.add(manba + " -> " + manzil + ":" + port + " (" + ochiq.get(port) + ")");
        }
        List<Map.Entry<String, Long>> royxat = new ArrayList<>(jami.entrySet());
        royxat.sort((a, b) -> !a.getValue().equals(b.getValue()) ? Long.compare(b.getValue(), a.getValue()) : a.getKey().compareTo(b.getKey()));
        for (Map.Entry<String, Long> e : royxat) System.out.println(e.getKey() + ": " + e.getValue() + " bayt");
        for (String o : ogohlantirish) System.out.println("Shifrlanmagan: " + o);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    map<string, string> ochiq = {{"21", "FTP"}, {"23", "Telnet"}, {"80", "HTTP"}, {"110", "POP3"}, {"143", "IMAP"}};
    map<string, long long> jami;
    vector<string> ogohlantirish;
    string manba, manzil, port;
    long long hajm;
    while (cin >> manba >> manzil >> port >> hajm) {
        jami[manba] += hajm;
        if (ochiq.count(port))                                    // shifrlanmagan protokol
            ogohlantirish.push_back(manba + " -> " + manzil + ":" + port + " (" + ochiq[port] + ")");
    }
    vector<pair<string, long long>> royxat(jami.begin(), jami.end());
    sort(royxat.begin(), royxat.end(), [](auto& a, auto& b) {
        return a.second != b.second ? a.second > b.second : a.first < b.first;
    });
    for (auto& [host, h] : royxat) cout << host << ": " << h << " bayt\n";
    for (auto& o : ogohlantirish) cout << "Shifrlanmagan: " << o << "\n";
    return 0;
}
```

Kirish:

```text
192.168.1.10 142.250.1.1 443 52000
192.168.1.10 93.184.216.34 80 4800
192.168.1.25 10.0.0.5 23 1200
192.168.1.25 142.250.1.1 443 81000
192.168.1.40 8.8.8.8 53 900
192.168.1.40 203.0.113.9 21 15000
```

Chiqish:

```text
192.168.1.25: 82200 bayt
192.168.1.10: 56800 bayt
192.168.1.40: 15900 bayt
Shifrlanmagan: 192.168.1.10 -> 93.184.216.34:80 (HTTP)
Shifrlanmagan: 192.168.1.25 -> 10.0.0.5:23 (Telnet)
Shifrlanmagan: 192.168.1.40 -> 203.0.113.9:21 (FTP)
```

Tahlil natijasi — harakat rejasi: `10.0.0.5` qurilmasini Telnet oʻrniga SSH bilan boshqarish, FTP serverini SFTP ga oʻtkazish, HTTP saytga HTTPS ni majburiy qilish. Eng faol host `192.168.1.25` — uning trafigi kutilgan ish bilan mos kelishini tekshirish kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **Ruxsatsiz tarmoqda trafik ushlash.** Qonun buzilishi.
- **Ushlangan faylni himoyasiz saqlash yoki ulashish.** Unda parollar va shaxsiy maʼlumotlar boʻlishi mumkin.
- **Ushlash va koʻrsatish filtrlarini adashtirish.** Sintaksislari turlicha.
- **Shifrlangan trafikni "koʻrinmayapti — demak muammo" deb hisoblash.** TLS mazmunini koʻrmaslik — normal holat; metamaʼlumotlar (manzil, hajm, vaqt) baribir tahlil qilinadi.
:::

:::tip
Hisobotda faqat xulosalar, statistika va maskalangan manzillarni keltiring; ushlangan `.pcap` faylini topshiriqqa biriktirmang.
:::

## Xulosa

- Trafik faqat oʻz, oʻquv yoki ruxsat berilgan tarmoqda kuzatiladi; ushlangan maʼlumot maxfiy.
- Wireshark: paketlar roʻyxati, tafsilotlar, baytlar; ushlash filtri (BPF) va koʻrsatish filtri.
- Statistika: protokollar ierarxiyasi, suhbatlar, nuqtalar, I/O grafigi.
- Monitoringning amaliy natijasi — shifrlanmagan protokollarni topib, HTTPS, SSH, SFTP kabi muqobillarga oʻtkazish va anomaliyalarni tekshirish.
- Katta tarmoqlarda oqim jurnallari (NetFlow) va avtomatik tahlil ishlatiladi.

## Oʻzingizni tekshiring

:::check
Q: Ushlash filtri koʻrsatish filtridan nimasi bilan farq qiladi?
A: Ushlash filtri nima yozib olinishini oldindan cheklaydi; koʻrsatish filtri yozib olinganlardan nimani koʻrsatishni tanlaydi.
Q: HTTP orqali yuborilgan parol nima uchun trafik tahlilchisida koʻrinadi?
A: HTTP trafigi shifrlanmagan.
Q: Telnet ning xavfsiz muqobili qaysi?
A: SSH.
Q: Wireshark da eng faol manzillarni qaysi vosita koʻrsatadi?
A: Statistics → Endpoints (va Conversations).
:::

:::link https://www.wireshark.org/docs/wsug_html_chunked/
Wireshark User’s Guide
Wireshark boʻyicha rasmiy foydalanuvchi qoʻllanmasi (ingliz tilida).
:::
