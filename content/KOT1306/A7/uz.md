---
title: IP-manzillar. Sinfli manzillar. Maskalar yordamida manzillash. IP manzillardan tejamkor foydalanish
summary: Ikkilik va oʻnlik oʻgirishlar mashqi; sinfli manzillar va standart maskalar; prefiks–maska jadvali; qoʻlda quyi tarmoqlarga boʻlish; oʻzgaruvchan uzunlikdagi maskalar (VLSM) bilan manzil maydonini tejamkor taqsimlash; marshrutlarni umumlashtirish (supernetting); nuqta-nuqta kanallar uchun /30 va /31; Packet Tracer da ikki quyi tarmoqni marshrutizator bilan bogʻlash.
---

## Nima uchun bu mavzu muhim?

Universitetga 192.168.10.0/24 bloki berilgan: kutubxonada 50 ta, laboratoriyada 100 ta, dekanatda 20 ta kompyuter, serverlar va marshrutizatorlar orasidagi kanallar bor. Blokni teng qismlarga boʻlsak, kimgadir yetmaydi, kimdadir yuzlab manzil boʻsh qoladi. **VLSM** — har bir boʻlimga aynan kerakli oʻlchamdagi quyi tarmoq berish usuli. Teskari masala ham bor: koʻp kichik tarmoqlarni marshrutlash jadvalida bitta yozuv bilan **umumlashtirish**. Bu mashgʻulotda ikkalasini qoʻlda va dastur bilan bajaramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- oktetlarni ikkilik va oʻnlik koʻrinishga tez oʻgira olasiz;
- prefiks, maska va blok oʻlchamini bir-biriga oʻgira olasiz;
- berilgan blokni VLSM bilan boʻlimlar ehtiyojiga koʻra taqsimlay olasiz;
- bir nechta tarmoqni bitta umumlashtirilgan marshrutga birlashtira olasiz;
- Packet Tracer da ikki quyi tarmoqni marshrutizator orqali bogʻlay olasiz.
:::

## Ikkilik oʻgirishlar mashqi

Oktet bitlarining "ogʻirliklari": **128 64 32 16 8 4 2 1**. Oʻnlikdan ikkilikka: chapdan boshlab ogʻirlikni ayira olsangiz — 1, aks holda 0.

| Oʻnlik | Ikkilik | Oʻnlik | Ikkilik |
|---|---|---|---|
| 192 | 11000000 | 248 | 11111000 |
| 224 | 11100000 | 252 | 11111100 |
| 240 | 11110000 | 254 | 11111110 |

Maska oktetlarida faqat shu qiymatlar uchraydi: **0, 128, 192, 224, 240, 248, 252, 254, 255**.

## Prefiks, maska va blok

| Prefiks | Maska | Blok (manzillar) | Xostlar |
|---|---|---|---|
| /24 | 255.255.255.0 | 256 | 254 |
| /25 | 255.255.255.128 | 128 | 126 |
| /26 | 255.255.255.192 | 64 | 62 |
| /27 | 255.255.255.224 | 32 | 30 |
| /28 | 255.255.255.240 | 16 | 14 |
| /29 | 255.255.255.248 | 8 | 6 |
| /30 | 255.255.255.252 | 4 | 2 |
| /31 | 255.255.255.254 | 2 | 2 (faqat nuqta-nuqta kanal, RFC 3021) |

Qoida: *H* ta xost uchun blok — *H* + 2 dan katta yoki teng boʻlgan eng kichik **ikkining darajasi**. 50 xost → 52 → 64 → /26.

## VLSM — oʻzgaruvchan uzunlikdagi maskalar

**Algoritm**:

1. Boʻlimlarni xostlar soni boʻyicha **kamayish** tartibida saralang.
2. Har biri uchun blok oʻlchamini toping (2 ning darajasi ≥ *H* + 2).
3. Blokning boshidan boshlab ketma-ket ajrating: har bir yangi quyi tarmoq oldingisi tugagan joydan boshlanadi.

Kattadan kichikka tartib muhim: katta bloklar avval joylashsa, har bir blok oʻz oʻlchamiga karrali manzildan boshlanadi (toʻgʻri tekislanadi) va boʻshliqlar qolmaydi.

**Marshrutizatorlar orasidagi kanallar** uchun /30 (2 xost) yoki /31 ishlatiladi — /24 berish 252 ta manzilni isrof qiladi.

## Marshrutlarni umumlashtirish

Agar 172.16.0.0/24, 172.16.1.0/24, 172.16.2.0/24 va 172.16.3.0/24 tarmoqlari bitta router orqali erishiladigan boʻlsa, boshqa routerlar toʻrtta yozuv oʻrniga bitta **172.16.0.0/22** yozuvini saqlashi mumkin. Umumlashtirilgan prefiks — barcha manzillarning **umumiy boshlangʻich bitlari**:

```text
172.16.0.0  = 10101100.00010000.000000|00.00000000
172.16.1.0  = 10101100.00010000.000000|01.00000000
172.16.2.0  = 10101100.00010000.000000|10.00000000
172.16.3.0  = 10101100.00010000.000000|11.00000000
              umumiy qism: 22 bit  ->  172.16.0.0/22
```

Umumlashtirish jadvallarni kichraytiradi va marshrutlashni tezlashtiradi. Ehtiyot boʻling: 192.168.1.0/24 va 192.168.2.0/24 ning umumiy prefiksi /22 — u 192.168.0.0 va 192.168.3.0 tarmoqlarini ham "qamrab oladi", ular boshqa joyda boʻlsa, trafik notoʻgʻri yoʻnalishi mumkin.

## Amaliy ish: Packet Tracer da ikki quyi tarmoq

1. **2911** marshrutizator, ikkita **2960** kommutator va 4 ta PC joylashtiring.
2. Kutubxona (50 xost) uchun 192.168.10.128/26 va dekanat (20 xost) uchun 192.168.10.192/27 quyi tarmoqlarini oling (quyidagi VLSM misolidan).
3. Router interfeyslarini sozlang:

```text
Router> enable
Router# configure terminal
Router(config)# interface g0/0
Router(config-if)# ip address 192.168.10.129 255.255.255.192
Router(config-if)# no shutdown
Router(config-if)# interface g0/1
Router(config-if)# ip address 192.168.10.193 255.255.255.224
Router(config-if)# no shutdown
```

4. Kompyuterlarga mos quyi tarmoqdan manzil va **shlyuz** sifatida router interfeysi manzilini bering (masalan, 192.168.10.130/26, shlyuz 192.168.10.129).
5. Bir quyi tarmoqdagi kompyuterdan ikkinchisiga `ping` qiling. Keyin bitta kompyuterning shlyuzini oʻchirib, yana tekshiring — nima oʻzgardi?

Bitta CIDR manzil boʻyicha tarmoq, maska, broadkast va xostlarni hisoblovchi dastur — ushbu mashgʻulotning mustaqil topshirigʻi (KOT-A7).

## Ishlangan misol 1: marshrutlarni umumlashtirish

Birinchi satrda *n*, keyingi *n* satrda tarmoqlar `a.b.c.d/p` koʻrinishida. Ularning barchasini qamrab oladigan eng kichik umumlashtirilgan tarmoqni chiqaring: prefiks — barcha manzillarning umumiy boshlangʻich bitlari soni (va berilgan prefikslardan katta emas).

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


n = int(input())
manzillar = []
p = 32
for _ in range(n):
    ip, prefiks = input().split("/")
    manzillar.append(songa(ip))
    p = min(p, int(prefiks))
while p > 0 and any((x >> (32 - p)) != (manzillar[0] >> (32 - p)) for x in manzillar):
    p -= 1                                   # umumiy qism topilguncha qisqartiramiz
maska = 0 if p == 0 else (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF
print(manzilga(manzillar[0] & maska) + "/" + str(p))
```
```java
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] manzillar = new long[n];
        int p = 32;
        for (int i = 0; i < n; i++) {
            String[] qism = in.next().split("/");
            manzillar[i] = songa(qism[0]);
            p = Math.min(p, Integer.parseInt(qism[1]));
        }
        while (p > 0) {
            boolean bir = true;
            for (long x : manzillar) {
                if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
            }
            if (bir) break;
            p--;                                             // umumiy qism topilguncha qisqartiramiz
        }
        long maska = p == 0 ? 0 : (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL;
        System.out.println(manzilga(manzillar[0] & maska) + "/" + p);
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    int n;
    cin >> n;
    vector<uint64_t> manzillar(n);
    int p = 32;
    for (int i = 0; i < n; i++) {
        string s;
        cin >> s;
        size_t chiziq = s.find('/');
        manzillar[i] = songa(s.substr(0, chiziq));
        p = min(p, stoi(s.substr(chiziq + 1)));
    }
    while (p > 0) {
        bool bir = true;
        for (uint64_t x : manzillar) {
            if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
        }
        if (bir) break;
        p--;                                                 // umumiy qism topilguncha qisqartiramiz
    }
    uint64_t maska = p == 0 ? 0 : (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL;
    cout << manzilga(manzillar[0] & maska) << "/" << p << endl;
    return 0;
}
```

Kirish:

```text
4
172.16.0.0/24
172.16.1.0/24
172.16.2.0/24
172.16.3.0/24
```

Chiqish:

```text
172.16.0.0/22
```

`10.1.4.0/24` va `10.1.7.0/24` uchun javob `10.1.4.0/22`, `192.168.1.0/24` va `192.168.2.0/24` uchun esa `192.168.0.0/22` — ikkinchisida ortiqcha (begona) tarmoqlar ham qamrab olinganiga eʼtibor bering.

## Ishlangan misol 2: VLSM taqsimlash

Birinchi satrda boshlangʻich blok (`a.b.c.d/p`) va boʻlimlar soni *k*. Keyingi *k* satrda boʻlim nomi va kerakli xostlar soni. Boʻlimlarni xostlar kamayishi tartibida (teng boʻlsa — kirish tartibida) blokdan ketma-ket taqsimlang. Har bir boʻlim uchun quyi tarmoq va undagi foydalanish mumkin boʻlgan xostlar sonini, joy yetmasa `joy yetmadi` ni chiqaring; oxirida qancha manzil ishlatilganini chiqaring.

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


blok, k = input().split()
ip, prefiks = blok.split("/")
boshi = songa(ip)
oxiri = boshi + 2 ** (32 - int(prefiks))
bolimlar = []
for _ in range(int(k)):
    nom, xost = input().split()
    bolimlar.append((nom, int(xost)))
bolimlar.sort(key=lambda b: -b[1])            # kattadan kichikka (barqaror)

joriy = boshi
for nom, xost in bolimlar:
    hajm = 4
    while hajm < xost + 2:                    # 2 ning darajasi >= xost + 2
        hajm *= 2
    if joriy + hajm > oxiri:
        print(nom, "joy yetmadi")
        continue
    p = 32 - (hajm.bit_length() - 1)
    print(nom, manzilga(joriy) + "/" + str(p), hajm - 2)
    joriy += hajm
print("ishlatildi:", joriy - boshi, "/", oxiri - boshi)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] blok = in.next().split("/");
        int k = in.nextInt();
        long boshi = songa(blok[0]);
        long oxiri = boshi + (1L << (32 - Integer.parseInt(blok[1])));
        List<String> nomlar = new ArrayList<>();
        List<Long> xostlar = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            nomlar.add(in.next());
            xostlar.add(in.nextLong());
        }
        List<Integer> tartib = new ArrayList<>();
        for (int i = 0; i < k; i++) tartib.add(i);
        tartib.sort((a, b) -> Long.compare(xostlar.get(b), xostlar.get(a)));   // kattadan kichikka (barqaror)

        long joriy = boshi;
        for (int i : tartib) {
            long hajm = 4;
            while (hajm < xostlar.get(i) + 2) {                // 2 ning darajasi >= xost + 2
                hajm *= 2;
            }
            if (joriy + hajm > oxiri) {
                System.out.println(nomlar.get(i) + " joy yetmadi");
                continue;
            }
            int p = 32 - Long.numberOfTrailingZeros(hajm);
            System.out.println(nomlar.get(i) + " " + manzilga(joriy) + "/" + p + " " + (hajm - 2));
            joriy += hajm;
        }
        System.out.println("ishlatildi: " + (joriy - boshi) + " / " + (oxiri - boshi));
    }
}
```
```cpp
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    string blok;
    int k;
    cin >> blok >> k;
    size_t chiziq = blok.find('/');
    uint64_t boshi = songa(blok.substr(0, chiziq));
    uint64_t oxiri = boshi + (1ULL << (32 - stoi(blok.substr(chiziq + 1))));
    vector<pair<string, long long>> bolimlar(k);
    for (auto& b : bolimlar) cin >> b.first >> b.second;
    stable_sort(bolimlar.begin(), bolimlar.end(), [](const pair<string, long long>& a, const pair<string, long long>& b) {
        return a.second > b.second;                          // kattadan kichikka (barqaror)
    });

    uint64_t joriy = boshi;
    for (auto& b : bolimlar) {
        long long hajm = 4;
        while (hajm < b.second + 2) {                        // 2 ning darajasi >= xost + 2
            hajm *= 2;
        }
        if (joriy + hajm > oxiri) {
            cout << b.first << " joy yetmadi" << endl;
            continue;
        }
        int p = 32 - __builtin_ctzll(hajm);
        cout << b.first << " " << manzilga(joriy) << "/" << p << " " << hajm - 2 << endl;
        joriy += hajm;
    }
    cout << "ishlatildi: " << joriy - boshi << " / " << oxiri - boshi << endl;
    return 0;
}
```

Kirish:

```text
192.168.10.0/24 5
Kutubxona 50
Dekanat 20
Laboratoriya 100
Server 10
Kanal 2
```

Chiqish:

```text
Laboratoriya 192.168.10.0/25 126
Kutubxona 192.168.10.128/26 62
Dekanat 192.168.10.192/27 30
Server 192.168.10.224/28 14
Kanal 192.168.10.240/30 2
ishlatildi: 244 / 256
```

Teng boʻlish (/27, 30 xost) bilan laboratoriyaga ham, kutubxonaga ham joy yetmas edi; VLSM bilan hammasi sigʻdi va 12 ta manzil (192.168.10.244–255) keyingi ehtiyojlar uchun qoldi.

:::warning
**Koʻp uchraydigan xatolar**

- **Blokni *H* boʻyicha emas, *H* + 2 boʻyicha tanlashni unutish.** 62 xost /26 ga sigʻadi, 63 xost esa — yoʻq (63 + 2 > 64).
- **VLSM da kichik boʻlimlardan boshlash.** Unda katta bloklar tekislanmay qoladi va manzil maydonida boʻshliqlar paydo boʻladi.
- **Kanallar uchun /24 berish.** Ikki router orasidagi kanalga /30 yoki /31 yetarli.
- **Umumlashtirish begona tarmoqlarni qamrab olishini hisobga olmaslik.** Umumlashtirilgan marshrut faqat qamrab olingan barcha tarmoqlar aynan shu yoʻnalishda boʻlsa toʻgʻri.
- **Shlyuzni boshqa quyi tarmoqdan berish.** Kompyuter shlyuzi doim oʻz quyi tarmogʻidagi router interfeysi manzili boʻladi.
:::

:::tip
Fakultetingiz uchun manzil rejasini tuzing: 10.20.0.0/22 blokini 4 ta kompyuter sinfi (har birida 30 kompyuter), 1 ta Wi-Fi tarmogʻi (200 qurilma), serverlar (12) va 3 ta routerlararo kanal uchun VLSM bilan taqsimlang. Natijani ikkinchi misol dasturi bilan tekshiring.
:::

## Xulosa

- Maska oktetlari: 0, 128, 192, 224, 240, 248, 252, 254, 255; blok = 256 − maska okteti.
- *H* ta xost uchun blok — *H* + 2 dan kichik boʻlmagan 2 ning darajasi.
- VLSM: kattadan kichikka saralash, ketma-ket ajratish — manzil maydoni tejaladi.
- Umumlashtirish: umumiy boshlangʻich bitlar → bitta qisqa prefiksli marshrut; ortiqcha qamrovdan ehtiyot boʻling.
- Nuqta-nuqta kanallar uchun /30 yoki /31.

## Oʻzingizni tekshiring

:::check
Q: 120 xost uchun eng kichik quyi tarmoq prefiksi qanday?
A: /25 — 128 ta manzil, 126 xost (122 ≤ 128).
Q: 255.255.255.248 maskasining blok oʻlchami va xostlar soni qancha?
A: Blok 8, xostlar 6.
Q: 10.0.0.0/24, 10.0.1.0/24, 10.0.2.0/24 va 10.0.3.0/24 ni qanday umumlashtirish mumkin?
A: 10.0.0.0/22.
Q: VLSM da nima uchun boʻlimlar kattadan kichikka saralanadi?
A: Har bir blok oʻz oʻlchamiga karrali manzildan boshlanishi (tekislanishi) va boʻshliqlar qolmasligi uchun.
Q: Ikki router orasidagi kanal uchun qaysi prefiks tejamkor?
A: /30 (2 xost) yoki RFC 3021 boʻyicha /31.
:::

:::link https://www.rfc-editor.org/rfc/rfc4632
RFC 4632: Classless Inter-domain Routing (CIDR)
CIDR, prefikslar va marshrutlarni umumlashtirish qoidalari (ingliz tilida).
:::
