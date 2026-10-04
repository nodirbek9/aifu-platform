---
title: IPv4 protokoli asosida tarmoq tugunlarini manzillash. IPv4 protokoli va uning asosida tarmoq tugunlarini manzillash usullari
summary: IPv4 manzilining tuzilishi — tarmoq va xost qismlari, nuqtali oʻnlik va ikkilik yozuv; sinfli manzillash (A–E); maska va prefiks (CIDR); maxsus manzillar — tarmoq, broadkast, loopback, link-local, multicast; xususiy manzillar (RFC 1918) va NAT; quyi tarmoqlarga boʻlish tushunchasi va qoʻlda hisoblash; manzillarni statik va dinamik berish.
---

## Nima uchun bu mavzu muhim?

Har bir kompyuter, telefon va server tarmoqda **IP manzil** orqali topiladi. Lekin manzil — shunchaki tasodifiy toʻrtta son emas: uning bir qismi "qaysi tarmoq", qolgani "shu tarmoqdagi qaysi qurilma" ekanini bildiradi. Marshrutizator aynan shu boʻlinishga qarab paketni yoʻnaltiradi. Manzilni qanday oʻqish, unga mos maskani qanday tanlash va tarmoqni qanday qismlarga boʻlishni bilmasdan biror tarmoqni loyihalab boʻlmaydi.

:::note
**Bu darsdan soʻng siz:**

- IPv4 manzilini nuqtali oʻnlik va ikkilik koʻrinishda yoza olasiz;
- manzil sinfini va turini (xususiy, ommaviy, loopback, multicast va boshqalar) aniqlay olasiz;
- maska va prefiksni bir-biriga oʻgira olasiz va ularning maʼnosini tushuntira olasiz;
- berilgan manzil va prefiks boʻyicha tarmoq va broadkast manzillarini qoʻlda hisoblay olasiz;
- kerakli quyi tarmoqlar va xostlar soni boʻyicha prefiksni tanlay olasiz.
:::

## IPv4 manzilining tuzilishi

IPv4 manzili — **32 bit**. Odamlar uni toʻrtta 8 bitli **oktetga** boʻlib, nuqtali oʻnlik koʻrinishda yozadi:

```text
192.168.10.77  =  11000000.10101000.00001010.01001101
```

Manzil ikki qismdan iborat: **tarmoq qismi** (yuqori bitlar) va **xost qismi** (pastki bitlar). Qaysi bitlar tarmoqqa tegishli ekanini **maska** belgilaydi: maskada tarmoq bitlari 1, xost bitlari 0. Maska qisqa yozuvda **prefiks** bilan beriladi — birlar soni:

| Prefiks | Maska | Xost bitlari | Xostlar soni (2^h − 2) |
|---|---|---|---|
| /8 | 255.0.0.0 | 24 | 16 777 214 |
| /16 | 255.255.0.0 | 16 | 65 534 |
| /20 | 255.255.240.0 | 12 | 4 094 |
| /24 | 255.255.255.0 | 8 | 254 |
| /26 | 255.255.255.192 | 6 | 62 |
| /30 | 255.255.255.252 | 2 | 2 |

Xostlar sonidan 2 ayiriladi, chunki xost qismi **hammasi 0** boʻlgan manzil — **tarmoq manzili**, **hammasi 1** boʻlgani — **broadkast manzili**; ularni qurilmaga berib boʻlmaydi.

## Sinfli manzillash

Dastlab (1981) maska alohida yozilmagan — u birinchi oktetdan kelib chiqqan:

| Sinf | Birinchi bitlar | Birinchi oktet | Standart maska | Vazifasi |
|---|---|---|---|---|
| **A** | 0 | 0–127 | /8 | juda katta tarmoqlar |
| **B** | 10 | 128–191 | /16 | oʻrta tarmoqlar |
| **C** | 110 | 192–223 | /24 | kichik tarmoqlar |
| **D** | 1110 | 224–239 | — | multicast |
| **E** | 1111 | 240–255 | — | tajriba uchun zaxira |

Sinfli tizim manzillarni juda isrof qilgan: 300 ta kompyuterli tashkilotga C (254) yetmagan, B (65 534) esa 99% boʻsh qolgan. 1993-yildan **CIDR** (Classless Inter-Domain Routing) joriy etildi: prefiks istalgan uzunlikda boʻlishi mumkin (masalan, /22 — 1022 xost). Bugun sinflar faqat tarixiy atama va imtihon savoli sifatida qolgan.

## Maxsus manzillar

| Diapazon | Vazifasi |
|---|---|
| 0.0.0.0/8 | "bu tarmoq"; 0.0.0.0 — manzil hali yoʻq (DHCP soʻrovida) yoki "har qanday manzil" |
| **10.0.0.0/8**, **172.16.0.0/12**, **192.168.0.0/16** | **xususiy** (RFC 1918) — Internetda marshrutlanmaydi |
| 127.0.0.0/8 | **loopback** — kompyuterning oʻzi (127.0.0.1 = localhost) |
| 169.254.0.0/16 | **link-local** — DHCP javob bermaganda Windows oʻziga beradi (APIPA) |
| 100.64.0.0/10 | provayderlarning umumiy NAT (CGNAT) manzillari |
| 224.0.0.0/4 | multicast (D sinf) |
| 240.0.0.0/4 | zaxira (E sinf) |
| 255.255.255.255 | cheklangan broadkast — joriy segmentdagi hammaga |

**Xususiy** manzillar istalgan tashkilot ichida qayta-qayta ishlatiladi. Internetga chiqishda router **NAT** (Network Address Translation) yordamida xususiy manzilni oʻzining ommaviy manziliga almashtiradi — shu tufayli bitta ommaviy IP orqali uydagi oʻnlab qurilma Internetdan foydalanadi.

## Qoʻlda hisoblash: tarmoq va broadkast manzili

Misol: `172.16.37.130/20`.

1. Maska /20 = `11111111.11111111.11110000.00000000` = **255.255.240.0**. Toʻliq oktetlar (255) oʻzgarmaydi, 0 boʻlganlari nolga aylanadi; faqat 3-oktet "chegaraviy".
2. 3-oktet: 37 = `00100101`; AND `11110000` = `00100000` = 32. **Tarmoq**: 172.16.**32**.0.
3. Xost bitlari 12 ta: 3-oktetning pastki 4 biti va butun 4-oktet. Ularni 1 bilan toʻldiramiz: 32 + 15 = 47, 4-oktet 255. **Broadkast**: 172.16.**47**.255.
4. Birinchi xost — 172.16.32.1, oxirgisi — 172.16.47.254, xostlar soni 2^12 − 2 = 4094.

Tez usul — **blok oʻlchami**: chegaraviy oktet uchun 256 − 240 = 16. Tarmoqlar 0, 16, 32, 48, … dan boshlanadi; 37 oraligʻi 32–47.

Bu hisoblashni dasturda bajarish — A7 amaliy mashgʻulotining mustaqil topshirigʻi (KOT-A7).

## Quyi tarmoqlarga boʻlish (subnetting)

Bitta katta tarmoqni bir nechta kichigiga boʻlish uchun xost qismidan **bitlar qarzga olinadi**. *b* bit olinsa:

- quyi tarmoqlar soni = 2^*b*;
- yangi prefiks = eski prefiks + *b*;
- har bir quyi tarmoqdagi xostlar = 2^(32 − yangi prefiks) − 2.

Masalan, 192.168.1.0/24 ni 4 ta boʻlimga boʻlish: *b* = 2, prefiks /26, har birida 62 xost: .0, .64, .128, .192.

## Manzillarni berish usullari

- **Statik** — administrator qoʻlda yozadi: serverlar, printerlar, marshrutizator interfeyslari.
- **Dinamik** — **DHCP** server avtomatik beradi (manzil, maska, shlyuz, DNS) — foydalanuvchi kompyuterlari va telefonlar uchun (M9 va A11 da batafsil).
- Windows da tekshirish: `ipconfig` (manzil, maska, shlyuz), Linux da: `ip addr`.

## Ishlangan misol 1: manzil sinfi va turi

Har bir satrda IPv4 manzili. Uni ikkilik koʻrinishda, sinfi va turi bilan chiqaring; notoʻgʻri manzil (toʻrtta 0–255 son emas) uchun `notoʻgʻri`. Kirish fayl oxirigacha oʻqiladi.

```python
import sys


def tur(o):
    if o[0] == 127:
        return "loopback"
    if o[0] == 10 or (o[0] == 172 and 16 <= o[1] <= 31) or (o[0] == 192 and o[1] == 168):
        return "xususiy"
    if o[0] == 169 and o[1] == 254:
        return "link-local"
    if o == [255, 255, 255, 255]:
        return "broadcast"
    if 224 <= o[0] <= 239:
        return "multicast"
    if o[0] >= 240 or o[0] == 0:
        return "zaxira"
    return "ommaviy"


for satr in sys.stdin:
    satr = satr.strip()
    if not satr:
        continue
    qismlar = satr.split(".")
    if len(qismlar) != 4 or not all(q.isdigit() and len(q) <= 3 and int(q) <= 255 for q in qismlar):
        print(satr, "notoʻgʻri")
        continue
    o = [int(q) for q in qismlar]
    ikkilik = ".".join(format(x, "08b") for x in o)
    sinf = "A" if o[0] < 128 else "B" if o[0] < 192 else "C" if o[0] < 224 else "D" if o[0] < 240 else "E"
    print(satr, ikkilik, sinf, tur(o))
```
```java
import java.util.Scanner;

public class Main {
    static String tur(int[] o) {
        if (o[0] == 127) return "loopback";
        if (o[0] == 10 || (o[0] == 172 && o[1] >= 16 && o[1] <= 31) || (o[0] == 192 && o[1] == 168)) return "xususiy";
        if (o[0] == 169 && o[1] == 254) return "link-local";
        if (o[0] == 255 && o[1] == 255 && o[2] == 255 && o[3] == 255) return "broadcast";
        if (o[0] >= 224 && o[0] <= 239) return "multicast";
        if (o[0] >= 240 || o[0] == 0) return "zaxira";
        return "ommaviy";
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] qismlar = satr.split("\\.", -1);
            boolean togri = qismlar.length == 4;
            for (int i = 0; togri && i < 4; i++) {
                togri = qismlar[i].matches("[0-9]{1,3}") && Integer.parseInt(qismlar[i]) <= 255;
            }
            if (!togri) {
                System.out.println(satr + " notoʻgʻri");
                continue;
            }
            int[] o = new int[4];
            StringBuilder ikkilik = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                o[i] = Integer.parseInt(qismlar[i]);
                if (i > 0) ikkilik.append(".");
                ikkilik.append(String.format("%8s", Integer.toBinaryString(o[i])).replace(' ', '0'));
            }
            String sinf = o[0] < 128 ? "A" : o[0] < 192 ? "B" : o[0] < 224 ? "C" : o[0] < 240 ? "D" : "E";
            System.out.println(satr + " " + ikkilik + " " + sinf + " " + tur(o));
        }
    }
}
```
```cpp
#include <bitset>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

string tur(const vector<int>& o) {
    if (o[0] == 127) return "loopback";
    if (o[0] == 10 || (o[0] == 172 && o[1] >= 16 && o[1] <= 31) || (o[0] == 192 && o[1] == 168)) return "xususiy";
    if (o[0] == 169 && o[1] == 254) return "link-local";
    if (o[0] == 255 && o[1] == 255 && o[2] == 255 && o[3] == 255) return "broadcast";
    if (o[0] >= 224 && o[0] <= 239) return "multicast";
    if (o[0] >= 240 || o[0] == 0) return "zaxira";
    return "ommaviy";
}

int main() {
    string satr;
    while (cin >> satr) {
        vector<string> qismlar;
        stringstream ss(satr);
        string q;
        while (getline(ss, q, '.')) qismlar.push_back(q);
        bool togri = qismlar.size() == 4 && satr.back() != '.';
        for (size_t i = 0; togri && i < 4; i++) {
            togri = !qismlar[i].empty() && qismlar[i].size() <= 3 &&
                    qismlar[i].find_first_not_of("0123456789") == string::npos && stoi(qismlar[i]) <= 255;
        }
        if (!togri) {
            cout << satr << " notoʻgʻri" << endl;
            continue;
        }
        vector<int> o;
        string ikkilik;
        for (int i = 0; i < 4; i++) {
            o.push_back(stoi(qismlar[i]));
            ikkilik += (i ? "." : "") + bitset<8>(o[i]).to_string();
        }
        string sinf = o[0] < 128 ? "A" : o[0] < 192 ? "B" : o[0] < 224 ? "C" : o[0] < 240 ? "D" : "E";
        cout << satr << " " << ikkilik << " " << sinf << " " << tur(o) << endl;
    }
    return 0;
}
```

Kirish:

```text
10.1.2.3
172.20.5.1
8.8.8.8
127.0.0.1
224.0.0.251
169.254.10.20
192.168.1.300
```

Chiqish:

```text
10.1.2.3 00001010.00000001.00000010.00000011 A xususiy
172.20.5.1 10101100.00010100.00000101.00000001 B xususiy
8.8.8.8 00001000.00001000.00001000.00001000 A ommaviy
127.0.0.1 01111111.00000000.00000000.00000001 A loopback
224.0.0.251 11100000.00000000.00000000.11111011 D multicast
169.254.10.20 10101001.11111110.00001010.00010100 B link-local
192.168.1.300 notoʻgʻri
```

Diqqat: sinf faqat birinchi oktetga qarab aniqlanadi — 10.1.2.3 A sinfiga kiradi, lekin amalda u /8 emas, /24 yoki boshqa prefiks bilan ishlatilishi mumkin.

## Ishlangan misol 2: quyi tarmoqlarni rejalash

Har bir satrda boshlangʻich prefiks, kerakli quyi tarmoqlar soni *S* va har bir quyi tarmoqdagi kerakli xostlar soni *H*. Qarzga olinadigan bitlar sonini eng kam qilib tanlang (2^*b* ≥ *S*), yangi prefiksni, hosil boʻladigan quyi tarmoqlar va har biridagi xostlar sonini chiqaring va *H* ta xost sigʻishi yoki sigʻmasligini ayting. Yangi prefiks 30 dan oshsa — `imkonsiz`.

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    prefiks, s, h = map(int, satr.split())
    b = 0
    while 2 ** b < s:                    # eng kam qarz bitlari
        b += 1
    yangi = prefiks + b
    if yangi > 30:
        print("imkonsiz")
        continue
    xostlar = 2 ** (32 - yangi) - 2
    print("/" + str(yangi), 2 ** b, xostlar, "yetarli" if xostlar >= h else "yetmaydi")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int prefiks = in.nextInt();
            long s = in.nextLong();
            long h = in.nextLong();
            int b = 0;
            while ((1L << b) < s) {                         // eng kam qarz bitlari
                b++;
            }
            int yangi = prefiks + b;
            if (yangi > 30) {
                System.out.println("imkonsiz");
                continue;
            }
            long xostlar = (1L << (32 - yangi)) - 2;
            System.out.println("/" + yangi + " " + (1L << b) + " " + xostlar + " " + (xostlar >= h ? "yetarli" : "yetmaydi"));
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int prefiks;
    long long s, h;
    while (cin >> prefiks >> s >> h) {
        int b = 0;
        while ((1LL << b) < s) {                            // eng kam qarz bitlari
            b++;
        }
        int yangi = prefiks + b;
        if (yangi > 30) {
            cout << "imkonsiz" << endl;
            continue;
        }
        long long xostlar = (1LL << (32 - yangi)) - 2;
        cout << "/" << yangi << " " << (1LL << b) << " " << xostlar << " " << (xostlar >= h ? "yetarli" : "yetmaydi") << endl;
    }
    return 0;
}
```

Kirish:

```text
24 4 50
24 6 30
24 10 20
16 100 300
24 100 1
```

Chiqish:

```text
/26 4 62 yetarli
/27 8 30 yetarli
/28 16 14 yetmaydi
/23 128 510 yetarli
imkonsiz
```

Uchinchi satr — klassik ziddiyat: koʻproq quyi tarmoq uchun koʻproq bit olinadi va har biridagi xostlar kamayadi. 10 ta boʻlimga 20 tadan kompyuter /24 ichiga teng boʻlaklarda sigʻmaydi — bunday holatda kattaroq manzil bloki yoki turli oʻlchamdagi quyi tarmoqlar (VLSM, A7 da) kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **Xostlar sonidan 2 ni ayirmaslik.** /24 da 256 emas, 254 ta xost: tarmoq va broadkast manzillari band.
- **172.16.0.0/12 diapazonini notoʻgʻri eslash.** U 172.16.0.0 dan 172.31.255.255 gacha; 172.32.x.x — ommaviy manzil.
- **Sinfni maska bilan aralashtirish.** Sinf — birinchi oktet boʻyicha tarixiy belgi; haqiqiy maska prefiks bilan beriladi.
- **169.254.x.x manzilni "Internet ishlayapti" deb tushunish.** Bu manzil DHCP server topilmaganini bildiradi.
- **Maskani notoʻgʻri yozish.** Maskada birlar faqat chapda ketma-ket turadi: 255.255.255.192 toʻgʻri, 255.255.255.191 — maska emas.
:::

:::tip
`ipconfig` bilan kompyuteringizning IP manzili va maskasini koʻring. Prefiksni aniqlang, tarmoq va broadkast manzilini qoʻlda hisoblang va manzil turi (xususiy yoki ommaviy) qanday ekanini ayting. Keyin "what is my ip" xizmatida tashqi manzilingizni koʻring — ular nega farq qiladi? (NAT.)
:::

## Xulosa

- IPv4 — 32 bit, toʻrt oktet; maska yoki prefiks tarmoq va xost qismlarini ajratadi; xostlar soni 2^h − 2.
- Sinflar: A (0–127), B (128–191), C (192–223), D — multicast, E — zaxira; bugun CIDR ishlatiladi.
- Xususiy diapazonlar: 10/8, 172.16/12, 192.168/16; loopback 127/8; link-local 169.254/16.
- Tarmoq manzili — IP AND maska, broadkast — xost bitlari hammasi 1; blok oʻlchami = 256 − chegaraviy oktet maskasi.
- Quyi tarmoqlarga boʻlish: *b* bit qarz → 2^*b* quyi tarmoq, prefiks + *b*.

## Oʻzingizni tekshiring

:::check
Q: 255.255.255.224 maskasi qaysi prefiksga mos keladi va unda nechta xost bor?
A: /27; 2^5 − 2 = 30 ta xost.
Q: 172.31.200.5 manzili xususiymi?
A: Ha — u 172.16.0.0/12 (172.16.0.0–172.31.255.255) diapazoniga kiradi.
Q: 10.20.30.40/12 manzilining tarmoq manzilini toping.
A: Blok 16, 2-oktet: 20 → 16; tarmoq 10.16.0.0.
Q: /24 tarmoqni 8 ta quyi tarmoqqa boʻlsak, yangi prefiks va xostlar soni qancha?
A: 3 bit qarz: /27, har birida 30 xost.
Q: Kompyuter 169.254.33.7 manzilini oldi. Bu nimani bildiradi?
A: DHCP server javob bermagan va Windows oʻziga link-local (APIPA) manzil bergan.
:::

:::link https://www.rfc-editor.org/rfc/rfc1918
RFC 1918: xususiy Internet manzillari
Xususiy diapazonlar va ularni ishlatish qoidalari (ingliz tilida).
:::
