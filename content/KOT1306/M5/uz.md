---
title: Ethernet texnologiyalari. Tarmoq tugunlarini manzillash. MAC manzil tushunchasi
summary: Ethernet tarixi va IEEE 802.3 standartlari (10BASE-T dan 400G gacha); Ethernet kadri tuzilishi — preambula, manzillar, EtherType, maʼlumot, FCS; minimal va maksimal kadr; CSMA/CD usuli va kolliziyalar; MAC manzil — tuzilishi, OUI, unicast, multicast va broadkast, global va lokal manzillar; tugunlarni manzillash darajalari; kanal samaradorligi.
---

## Nima uchun bu mavzu muhim?

Bugungi simli lokal tarmoqlarning deyarli 100% i — **Ethernet**. Universitet kompyuter sinfidagi kabel, uydagi routerning sariq portlari, maʼlumotlar markazidagi 100 gigabitli serverlar — barchasi bitta oiladagi standartlar. Ethernet kadri qanday tuzilgani, MAC manzil nimani anglatishi va nima uchun eng kichik kadr 64 bayt ekanini bilish tarmoqni tahlil qilish va nosozliklarni aniqlash uchun asos.

:::note
**Bu darsdan soʻng siz:**

- Ethernet standartlarini nomlanishi boʻyicha oʻqiy olasiz (masalan, 1000BASE-T);
- Ethernet kadrining maydonlari va ularning uzunligini ayta olasiz;
- CSMA/CD usulini va minimal kadr uzunligi nima uchun kerakligini tushuntira olasiz;
- MAC manzilning tuzilishini tahlil qilib, uning turi (unicast, multicast, broadkast) va doirasini (global, lokal) aniqlay olasiz;
- turli kadr oʻlchamlarida kanalning foydali tezligini hisoblay olasiz.
:::

## Ethernet tarixi va standartlari

Ethernet 1973-yilda Xerox PARC da Robert Metkalf tomonidan yaratilgan, 1983-yilda **IEEE 802.3** standarti sifatida qabul qilingan. Standart nomi uch qismdan iborat: **tezlik** – **BASE** (baseband — bitta kanal) – **muhit**.

| Standart | Tezlik | Muhit | Masofa | Nomi |
|---|---|---|---|---|
| 10BASE5 / 10BASE2 | 10 Mbit/s | qalin / ingichka koaksial | 500 / 185 m | dastlabki Ethernet (shina) |
| 10BASE-T | 10 Mbit/s | oʻralgan juftlik Cat3 | 100 m | Ethernet |
| 100BASE-TX | 100 Mbit/s | Cat5 | 100 m | Fast Ethernet |
| 100BASE-FX | 100 Mbit/s | koʻp modali optika | 2 km | Fast Ethernet |
| 1000BASE-T | 1 Gbit/s | Cat5e (4 juftlik) | 100 m | Gigabit Ethernet |
| 1000BASE-SX / LX | 1 Gbit/s | koʻp / bir modali optika | 550 m / 5 km | Gigabit Ethernet |
| 10GBASE-T | 10 Gbit/s | Cat6a | 100 m | 10 Gigabit Ethernet |
| 40G / 100G / 400G | 40–400 Gbit/s | asosan optika | metrdan km gacha | maʼlumotlar markazlari |

Tezlik 40 ming baravar oshgan, lekin **kadr formati deyarli oʻzgarmagan** — Ethernet muvaffaqiyatining siri ana shu moslikda.

## Ethernet kadri

| Maydon | Uzunligi (bayt) | Vazifasi |
|---|---|---|
| Preambula | 7 | `10101010` naqshi — qabul qiluvchi soatini sinxronlash |
| SFD (kadr boshi belgisi) | 1 | `10101011` — kadr shu yerdan boshlanadi |
| Qabul qiluvchi MAC | 6 | kimga |
| Yuboruvchi MAC | 6 | kimdan |
| EtherType / Uzunlik | 2 | ichidagi protokol: `0x0800` — IPv4, `0x0806` — ARP, `0x86DD` — IPv6 |
| Maʼlumot (payload) | 46–1500 | yuqori pogʻona paketi; 46 dan kam boʻlsa, toʻldiriladi (padding) |
| FCS | 4 | CRC-32 nazorat yigʻindisi |

Preambula va SFD odatda "kadr uzunligiga" kiritilmaydi. Shuning uchun **minimal kadr** 6 + 6 + 2 + 46 + 4 = **64 bayt**, **maksimal** — 1518 bayt (VLAN tegi bilan 1522). Kadrlar orasida kamida 12 baytlik vaqt pauzasi — **IFG** (inter-frame gap) boʻladi. Maʼlumotlar markazlarida **jumbo** kadrlar (9000 baytgacha) ham ishlatiladi.

## CSMA/CD

Klassik Ethernet umumiy muhitda (shina yoki konsentrator) ishlagan va kirishni **CSMA/CD** (Carrier Sense Multiple Access with Collision Detection) usuli bilan boshqargan:

1. **Tinglash** (carrier sense): kanal band boʻlsa — kutish.
2. Kanal boʻsh — uzatishni boshlash, uzatish davomida kanalni kuzatish.
3. **Kolliziya aniqlansa** (collision detection) — uzatishni toʻxtatib, 32 bitli **jam** signalini yuborish.
4. **Ikkilik eksponensial kutish** (backoff): *n*-kolliziyadan keyin 0 dan 2^min(*n*, 10) − 1 gacha tasodifiy sondagi slot vaqti kutiladi; 16 urinishdan soʻng kadr tashlab yuboriladi.

**Minimal kadr nima uchun 64 bayt?** Yuboruvchi kolliziyani kadrni uzatib boʻlishidan **oldin** sezishi kerak. Signal segmentning eng uzoq nuqtasiga borib, kolliziya xabari qaytib kelishi uchun ketadigan vaqt — **slot vaqti** — 10 Mbit/s da 512 bit = 64 bayt deb belgilangan. Kadr undan qisqa boʻlsa, kolliziya yuboruvchi "ish tugadi" deb hisoblagandan keyin kelishi mumkin edi.

Zamonaviy kommutatorli **toʻliq dupleks** Ethernet da kolliziya umuman yoʻq — har bir port alohida kanal, CSMA/CD oʻchirilgan. Lekin moslik uchun minimal kadr 64 bayt boʻlib qolgan.

## MAC manzil

**MAC manzil** — 48 bitli (6 bayt) fizik manzil, odatda oʻn oltilik koʻrinishda yoziladi. Yozuv shakllari turlicha, lekin maʼnosi bitta:

```text
3C:52:82:1A:9F:07      Linux, macOS
3C-52-82-1A-9F-07      Windows
3c52.821a.9f07         Cisco
```

| Qism | Maʼnosi |
|---|---|
| Birinchi 3 bayt — **OUI** | ishlab chiqaruvchi identifikatori (IEEE beradi): masalan, `00:1A:2B` |
| Oxirgi 3 bayt | ishlab chiqaruvchi tomonidan har bir adapterga noyob beriladi |
| 1-baytning eng kichik biti (I/G) | 0 — **unicast** (bitta qurilma), 1 — **multicast** (guruh) |
| 1-baytning ikkinchi biti (U/L) | 0 — **global** (zavodda berilgan), 1 — **lokal** (dastur yoki administrator bergan) |

**Broadkast** manzil — `FF:FF:FF:FF:FF:FF` (barcha bitlar 1): kadr segmentdagi barcha qurilmalarga boradi. Masalan, `01:00:5E:...` — IPv4 multicast, `33:33:...` — IPv6 multicast. Virtual mashinalar va telefonlarning "tasodifiy MAC" funksiyasi **lokal** manzillardan foydalanadi (masalan, Docker konteynerlari `02:42:...`).

## Tugunlarni manzillash darajalari

| Daraja | Manzil | Misol | Kim beradi |
|---|---|---|---|
| Kanal (2) | MAC | `3C:52:82:1A:9F:07` | ishlab chiqaruvchi |
| Tarmoq (3) | IP | `192.168.1.10` | administrator yoki DHCP |
| Transport (4) | port | `443` | operatsion tizim yoki dastur |
| Amaliy (7) | domen nomi | `aifu.uz` | DNS roʻyxatdan oʻtkazuvchi |

MAC manzil faqat **bitta segment** ichida maʼnoga ega: paket marshrutizatordan oʻtganda yangi kadrga solinadi va MAC manzillar almashadi, IP manzillar esa (NAT boʻlmasa) oʻzgarmaydi. IP manzil boʻyicha MAC manzilni topish — **ARP** protokolining vazifasi (M9 da batafsil).

## Ishlangan misol 1: MAC manzilni tahlil qilish

Har bir satrda istalgan yozuv shaklidagi MAC manzil. Uni `XX:XX:XX:XX:XX:XX` shakliga keltiring va OUI, turi (unicast/multicast) va doirasini (global/lokal) chiqaring; broadkast manzil uchun faqat `broadcast`. Notoʻgʻri manzil uchun `notoʻgʻri`. Kirish fayl oxirigacha oʻqiladi.

```python
import sys

for satr in sys.stdin:
    satr = satr.strip()
    if not satr:
        continue
    raqamlar = satr.replace(":", "").replace("-", "").replace(".", "").upper()
    if len(raqamlar) != 12 or any(c not in "0123456789ABCDEF" for c in raqamlar):
        print(satr, "notoʻgʻri")
        continue
    baytlar = [raqamlar[i:i + 2] for i in range(0, 12, 2)]
    mac = ":".join(baytlar)
    if raqamlar == "F" * 12:
        print(mac, "broadcast")
        continue
    birinchi = int(baytlar[0], 16)
    tur = "multicast" if birinchi & 1 else "unicast"         # I/G biti
    doira = "lokal" if birinchi & 2 else "global"             # U/L biti
    print(mac, "OUI", ":".join(baytlar[:3]), tur, doira)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String raqamlar = satr.replace(":", "").replace("-", "").replace(".", "").toUpperCase();
            if (raqamlar.length() != 12 || !raqamlar.matches("[0-9A-F]+")) {
                System.out.println(satr + " notoʻgʻri");
                continue;
            }
            String[] baytlar = new String[6];
            for (int i = 0; i < 6; i++) baytlar[i] = raqamlar.substring(2 * i, 2 * i + 2);
            String mac = String.join(":", baytlar);
            if (raqamlar.equals("FFFFFFFFFFFF")) {
                System.out.println(mac + " broadcast");
                continue;
            }
            int birinchi = Integer.parseInt(baytlar[0], 16);
            String tur = (birinchi & 1) != 0 ? "multicast" : "unicast";    // I/G biti
            String doira = (birinchi & 2) != 0 ? "lokal" : "global";        // U/L biti
            System.out.println(mac + " OUI " + baytlar[0] + ":" + baytlar[1] + ":" + baytlar[2] + " " + tur + " " + doira);
        }
    }
}
```
```cpp
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    while (cin >> satr) {
        string raqamlar;
        bool togri = true;
        for (char c : satr) {
            if (c == ':' || c == '-' || c == '.') continue;
            c = toupper(c);
            if (!isxdigit(c)) togri = false;
            raqamlar += c;
        }
        if (raqamlar.size() != 12 || !togri) {
            cout << satr << " notoʻgʻri" << endl;
            continue;
        }
        string mac;
        for (int i = 0; i < 12; i += 2) mac += (i ? ":" : "") + raqamlar.substr(i, 2);
        if (raqamlar == "FFFFFFFFFFFF") {
            cout << mac << " broadcast" << endl;
            continue;
        }
        int birinchi = stoi(raqamlar.substr(0, 2), nullptr, 16);
        string tur = (birinchi & 1) ? "multicast" : "unicast";      // I/G biti
        string doira = (birinchi & 2) ? "lokal" : "global";          // U/L biti
        cout << mac << " OUI " << mac.substr(0, 8) << " " << tur << " " << doira << endl;
    }
    return 0;
}
```

Kirish:

```text
3c:52:82:1a:9f:07
01-00-5E-00-00-FB
ffff.ffff.ffff
02:42:ac:11:00:02
12:34:56
```

Chiqish:

```text
3C:52:82:1A:9F:07 OUI 3C:52:82 unicast global
01:00:5E:00:00:FB OUI 01:00:5E multicast global
FF:FF:FF:FF:FF:FF broadcast
02:42:AC:11:00:02 OUI 02:42:AC unicast lokal
12:34:56 notoʻgʻri
```

Ikkinchi manzil — mDNS (`224.0.0.251`) ning multicast MAC manzili, toʻrtinchisi — Docker konteyneri manzili (lokal).

## Ishlangan misol 2: kadr oʻlchami va foydali tezlik

Har bir satrda kanal tezligi *V* (Mbit/s) va kadrdagi maʼlumot hajmi *L* (bayt). Kadr uzunligini (46 baytgacha toʻldirish bilan), kabeldagi toʻliq hajmni (preambula 8 + IFG 12 bayt qoʻshib), sekundiga eng koʻp kadrlar sonini (butun qismi) va foydali tezlikni (Mbit/s, ikki xona) chiqaring.

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    v, l = map(int, satr.split())
    kadr = 6 + 6 + 2 + max(l, 46) + 4         # 46 dan kam maʼlumot toʻldiriladi
    kabelda = kadr + 8 + 12                   # preambula+SFD va kadrlar orasidagi pauza
    sekundiga = v * 1_000_000 / (kabelda * 8)
    foydali = sekundiga * l * 8 / 1_000_000
    print(kadr, kabelda, int(sekundiga), f"{foydali:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long v = in.nextLong();
            long l = in.nextLong();
            long kadr = 6 + 6 + 2 + Math.max(l, 46) + 4;     // 46 dan kam maʼlumot toʻldiriladi
            long kabelda = kadr + 8 + 12;                    // preambula+SFD va kadrlar orasidagi pauza
            double sekundiga = v * 1_000_000.0 / (kabelda * 8);
            double foydali = sekundiga * l * 8 / 1_000_000;
            System.out.println(kadr + " " + kabelda + " " + (long) sekundiga + " " + String.format("%.2f", foydali));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    long long v, l;
    while (cin >> v >> l) {
        long long kadr = 6 + 6 + 2 + max(l, 46LL) + 4;      // 46 dan kam maʼlumot toʻldiriladi
        long long kabelda = kadr + 8 + 12;                  // preambula+SFD va kadrlar orasidagi pauza
        double sekundiga = v * 1000000.0 / (kabelda * 8);
        double foydali = sekundiga * l * 8 / 1000000;
        printf("%lld %lld %lld %.2f\n", kadr, kabelda, (long long) sekundiga, foydali);
    }
    return 0;
}
```

Kirish:

```text
100 46
100 1500
1000 10
10000 9000
```

Chiqish:

```text
64 84 148809 54.76
1518 1538 8127 97.53
64 84 1488095 119.05
9018 9038 138304 9957.96
```

148 809 kadr/s — 100 Mbit/s portning mashhur "maksimal paket tezligi" (Gigabit da 1 488 095). Kichik kadrlar bilan port kadrlarga toʻla, lekin foydali tezlik yarmigacha tushadi; katta kadrlar va ayniqsa jumbo kadrlar bilan samaradorlik 97–99% ga yetadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Preambulani kadr uzunligiga qoʻshish.** 64 va 1518 baytlik chegaralar preambula va SFD siz hisoblanadi; ular faqat kabeldagi vaqtni hisoblashda qoʻshiladi.
- **MAC manzil butun dunyo boʻylab marshrutlanadi deb oʻylash.** MAC faqat bitta segmentda ishlaydi; marshrutizatordan keyin kadr yangi MAC manzillar bilan qayta tuziladi.
- **Multicast bitini notoʻgʻri baytdan qidirish.** I/G va U/L bitlari **birinchi** baytning eng kichik ikki biti.
- **Zamonaviy kommutatorli tarmoqda ham CSMA/CD ishlaydi deb hisoblash.** Toʻliq dupleks rejimda kolliziya boʻlmaydi va CSMA/CD oʻchirilgan.
:::

:::tip
`ipconfig /all` (yoki `ip link`) bilan adapteringizning MAC manzilini toping va uning OUI qismini istalgan onlayn OUI qidiruv xizmatida tekshiring — ishlab chiqaruvchi chiqadi. Telefoningizdagi Wi-Fi sozlamalarida "tasodifiy MAC" yoqilgan boʻlsa, uning birinchi baytining ikkinchi biti 1 ekanini (lokal manzil) tekshiring.
:::

## Xulosa

- Ethernet — IEEE 802.3; nomlanishi: tezlik–BASE–muhit (masalan, 1000BASE-T); kadr formati 10 Mbit/s dan 400 Gbit/s gacha deyarli oʻzgarmagan.
- Kadr: preambula 7 + SFD 1, qabul qiluvchi va yuboruvchi MAC 6 + 6, EtherType 2, maʼlumot 46–1500, FCS 4; minimal kadr 64, maksimal 1518 bayt.
- CSMA/CD: tinglash, uzatish, kolliziyani aniqlash, jam va ikkilik eksponensial kutish; minimal kadr kolliziyani oʻz vaqtida sezish uchun kerak.
- MAC — 48 bit: OUI + qurilma raqami; birinchi baytdagi I/G va U/L bitlari turi va doirasini belgilaydi; `FF:FF:FF:FF:FF:FF` — broadkast.

## Oʻzingizni tekshiring

:::check
Q: 1000BASE-T nomi nimani bildiradi?
A: 1000 Mbit/s tezlik, baseband uzatish, oʻralgan juftlik (twisted pair) muhiti.
Q: Ethernet kadridagi maʼlumot 20 bayt boʻlsa, nima qilinadi?
A: Maʼlumot qismi 46 baytgacha toʻldiriladi (padding), shunda kadr 64 baytdan kam boʻlmaydi.
Q: `0x0806` EtherType qiymati qaysi protokolni bildiradi?
A: ARP.
Q: `01:00:5E:7F:00:01` manzili unicast mi yoki multicast mi?
A: Multicast — birinchi bayt `01` ning eng kichik biti 1.
Q: Nima uchun zamonaviy kommutatorli tarmoqlarda kolliziyalar boʻlmaydi?
A: Har bir port alohida kolliziya domeni va toʻliq dupleks rejimda ishlaydi — yuborish va qabul qilish alohida juftliklar orqali.
:::

:::link https://en.wikipedia.org/wiki/IEEE_802.3
IEEE 802.3
Ethernet standartlari roʻyxati, kadr formati va tarixi (ingliz tilida).
:::
