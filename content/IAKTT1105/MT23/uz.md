---
title: Blokcheyn ishlash tamoyili
summary: Mustaqil ish — tranzaksiyaning hayot sikli (yaratish, raqamli imzo, tarmoqqa tarqatish, kutish navbati, blokka kiritish, tasdiqlar); blok tuzilishi — sarlavha (oldingi xesh, Merkle ildizi, vaqt, qiyinlik, nonce) va tranzaksiyalar; Merkle daraxti va qisqa isbot; konsensus mexanizmlari — Proof of Work, Proof of Stake, ruxsatli tarmoqlardagi kelishuv; qiyinlikni moslashtirish; zanjir ayrilishi va yakuniylik; Merkle ildizi va ish isbotini koʻrsatuvchi dastur; topshiriq.
---

## Mustaqil ishning maqsadi

M12 dagi misolda bloklar xesh orqali zanjirga bogʻlanishini koʻrdik. Lekin real blokcheynda yana savollar bor: tranzaksiya kim tomonidan va qanday tasdiqlanadi, blok ichida minglab tranzaksiyalar qanday "bitta xesh"ga aylanadi, notanish ishtirokchilar qanday kelishib oladi? Bu mustaqil ish blokcheynning **ichki ishlash tamoyili**ni bosqichma-bosqich koʻrsatadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- tranzaksiyaning yaratilishdan tasdiqlanishgacha boʻlgan yoʻlini tushuntira olasiz;
- blok sarlavhasi elementlarini ayta olasiz;
- Merkle daraxti nima uchun kerakligini koʻrsata olasiz;
- Proof of Work va Proof of Stake ni solishtira olasiz.
:::

## Tranzaksiyaning hayot sikli

1. **Yaratish**: Ali Valiga 500 birlik oʻtkazmoqchi; hamyon dasturi tranzaksiya yaratadi.
2. **Imzolash**: tranzaksiya Alining **yopiq kaliti** bilan imzolanadi (S14). Har kim Alining ochiq kaliti bilan imzoni tekshira oladi, lekin hech kim Ali nomidan soxta tranzaksiya yarata olmaydi.
3. **Tarqatish**: tranzaksiya tarmoq tugunlariga yuboriladi; har bir tugun imzo va qoldiqni tekshiradi.
4. **Kutish navbati** (mempool): tekshiruvdan oʻtgan tranzaksiyalar blokka kiritilishini kutadi; yuqori haq toʻlaganlar odatda tezroq kiritiladi.
5. **Blokka kiritish**: blok yaratuvchi (mayner yoki validator) tranzaksiyalarni blokka yigʻadi va konsensus qoidasiga koʻra blokni tarmoqqa taklif qiladi.
6. **Tasdiqlar**: blok ustiga yangi bloklar qoʻshilgan sari tranzaksiyani oʻzgartirish amalda imkonsiz boʻlib boradi. Bitcoin da odatda 6 ta tasdiq yetarli hisoblanadi.

## Blok tuzilishi

| Qism | Element | Vazifasi |
|---|---|---|
| **Sarlavha** | oldingi blok xeshi | zanjirga bogʻlash (M12) |
| | Merkle ildizi | blokdagi barcha tranzaksiyalarning bitta "barmoq izi" |
| | vaqt belgisi | blok yaratilgan vaqt |
| | qiyinlik | ish isboti uchun maqsad (PoW) |
| | nonce | qiyinlik shartini qanoatlantirish uchun tanlanadigan son |
| **Tana** | tranzaksiyalar roʻyxati | imzolangan oʻtkazmalar |

## Merkle daraxti

Tranzaksiyalar juftlab xeshlanadi, natijalar yana juftlab xeshlanadi — yuqorida bitta **Merkle ildizi** qoladi:

```text
                 ildiz = H(H12 + H34)
                /                    \
        H12 = H(H1 + H2)        H34 = H(H3 + H4)
         /         \              /         \
   H1 = H(tx1)  H2 = H(tx2)  H3 = H(tx3)  H4 = H(tx4)
```

**Nima uchun kerak?**

- Bitta tranzaksiya oʻzgarsa, ildiz ham oʻzgaradi — blok sarlavhasi butun tanani himoya qiladi.
- **Qisqa isbot**: tranzaksiya blokda borligini tekshirish uchun barcha tranzaksiyalar emas, faqat yoʻldagi "qoʻshni" xeshlar kerak — 1 000 000 tranzaksiyada atigi ~20 ta xesh (MT10 dagi log n gʻoyasi). Shu tufayli telefon hamyonlari butun blokcheynni yuklamasdan ishlaydi.

## Konsensus mexanizmlari

| Mexanizm | Kim blok yaratadi | Xavfsizlik nimaga asoslanadi | Kamchilik |
|---|---|---|---|
| **Proof of Work** (ish isboti) | qiyin hisob-kitob masalasini birinchi yechgan mayner | hisoblash quvvatining koʻp qismi halol ishtirokchilarda boʻlishi | juda koʻp elektr energiyasi |
| **Proof of Stake** (ulush isboti) | tasodifan tanlangan validator, garovga qoʻygan mablagʻiga mutanosib | validatorlar garovini yoʻqotishdan qoʻrqadi (qoida buzilsa garov kesiladi) | yirik egalar taʼsiri kuchliroq |
| **Ruxsatli kelishuv** (BFT turidagi) | tasdiqlangan ishtirokchilar ovoz beradi | ishtirokchilarning maʼlum qismidan koʻpi halol boʻlishi | ishtirokchilar soni cheklangan |

**Ish isboti gʻoyasi**: blok sarlavhasining xeshi maʼlum shartni (masalan, boshida bir nechta nol) qanoatlantirishi kerak. Xeshni oldindan bilib boʻlmagani uchun nonce ni birma-bir sinashdan boshqa yoʻl yoʻq. Talab qilinadigan nollar soni bittaga oshsa, oʻrtacha urinishlar soni 16 barobar oshadi (oʻn oltilik yozuvda). Tekshirish esa bitta xesh hisoblash bilan bajariladi — **yaratish qiyin, tekshirish oson**.

**Qiyinlikni moslashtirish**: Bitcoin da qiyinlik har 2 016 blokda shunday oʻzgartiriladi-ki, blok oʻrtacha har 10 daqiqada yaratiladi — tarmoqdagi quvvat oshsa, qiyinlik ham oshadi.

## Ishlangan misol: Merkle ildizi va ish isboti

Har bir satrda bitta tranzaksiya. Merkle ildizini hisoblang, keyin sarlavha `oldingi|ildiz|nonce` uchun xesh boshida 1, 2 va 3 ta nol boʻladigan eng kichik nonce ni toping. Oʻquv maqsadida M12 dagi FNV-1a xesh-funksiyasi ishlatiladi (haqiqiy tizimlarda SHA-256).

```python
import sys

def fnv1a(matn):                                     # oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return format(h, "08x")

tranzaksiyalar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
daraja = [fnv1a(t) for t in tranzaksiyalar]
while len(daraja) > 1:
    if len(daraja) % 2 == 1:
        daraja.append(daraja[-1])                    # toq boʻlsa, oxirgisi takrorlanadi
    daraja = [fnv1a(daraja[i] + daraja[i + 1]) for i in range(0, len(daraja), 2)]
ildiz = daraja[0]
print(f"Merkle ildizi: {ildiz}")

for q in range(1, 4):                                # ish isboti: boshida q ta nol
    nonce = 0
    while True:
        xesh = fnv1a(f"00000000|{ildiz}|{nonce}")
        if xesh.startswith("0" * q):
            break
        nonce += 1
    print(f"qiyinlik {q}: nonce {nonce}, xesh {xesh}, urinishlar {nonce + 1}")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String fnv1a(String matn) {                       // oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
        long h = 2166136261L;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = ((h ^ (b & 0xff)) * 16777619L) & 0xFFFFFFFFL;
        return String.format("%08x", h);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> daraja = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) daraja.add(fnv1a(s));
        }
        while (daraja.size() > 1) {
            if (daraja.size() % 2 == 1) daraja.add(daraja.get(daraja.size() - 1));   // toq boʻlsa, oxirgisi takrorlanadi
            List<String> keyingi = new ArrayList<>();
            for (int i = 0; i < daraja.size(); i += 2) keyingi.add(fnv1a(daraja.get(i) + daraja.get(i + 1)));
            daraja = keyingi;
        }
        String ildiz = daraja.get(0);
        System.out.println("Merkle ildizi: " + ildiz);

        for (int q = 1; q <= 3; q++) {                       // ish isboti: boshida q ta nol
            long nonce = 0;
            String xesh;
            while (true) {
                xesh = fnv1a("00000000|" + ildiz + "|" + nonce);
                if (xesh.startsWith("0".repeat(q))) break;
                nonce++;
            }
            System.out.println("qiyinlik " + q + ": nonce " + nonce + ", xesh " + xesh + ", urinishlar " + (nonce + 1));
        }
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

string fnv1a(const string& matn) {                           // oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
    uint64_t h = 2166136261ULL;
    for (unsigned char b : matn) h = ((h ^ b) * 16777619ULL) & 0xFFFFFFFFULL;
    char buf[16];
    snprintf(buf, sizeof buf, "%08llx", (unsigned long long) h);
    return buf;
}

int main() {
    vector<string> daraja;
    string s;
    while (getline(cin, s)) {
        size_t a = s.find_first_not_of(" \t\r"), b = s.find_last_not_of(" \t\r");
        if (a != string::npos) daraja.push_back(fnv1a(s.substr(a, b - a + 1)));
    }
    while (daraja.size() > 1) {
        if (daraja.size() % 2 == 1) daraja.push_back(daraja.back());   // toq boʻlsa, oxirgisi takrorlanadi
        vector<string> keyingi;
        for (size_t i = 0; i < daraja.size(); i += 2) keyingi.push_back(fnv1a(daraja[i] + daraja[i + 1]));
        daraja = keyingi;
    }
    string ildiz = daraja[0];
    cout << "Merkle ildizi: " << ildiz << "\n";

    for (int q = 1; q <= 3; q++) {                           // ish isboti: boshida q ta nol
        long long nonce = 0;
        string xesh;
        while (true) {
            xesh = fnv1a("00000000|" + ildiz + "|" + to_string(nonce));
            if (xesh.compare(0, q, string(q, '0')) == 0) break;
            nonce++;
        }
        cout << "qiyinlik " << q << ": nonce " << nonce << ", xesh " << xesh << ", urinishlar " << nonce + 1 << "\n";
    }
    return 0;
}
```

Kirish:

```text
Ali->Vali 500
Vali->Sardor 200
Sardor->Kamola 150
Kamola->Ali 50
```

Chiqish:

```text
Merkle ildizi: b9802d65
qiyinlik 1: nonce 210, xesh 0b3c9c4a, urinishlar 211
qiyinlik 2: nonce 231, xesh 00380dcb, urinishlar 232
qiyinlik 3: nonce 4394, xesh 0005b4b7, urinishlar 4395
```

Kuzatishlar:

- Oʻrtacha 1 ta nol uchun ~16, 2 ta uchun ~256, 3 ta uchun ~4 096 urinish kutiladi. Bu safar 1 ta nolga 211 urinish ketdi — omadsizlik: ish isboti tasodifiy jarayon, faqat **oʻrtacha** qiyinlik oldindan maʼlum.
- Birinchi tranzaksiyani "Ali->Vali 900" ga oʻzgartirib koʻring: Merkle ildizi `2b924064` ga, barcha nonce lar esa butunlay boshqa qiymatlarga oʻzgaradi. Bitta tranzaksiyani soxtalashtirish blokni qaytadan "qazib olish"ni talab qiladi — va undan keyingi barcha bloklarni ham.
- Tekshiruvchi uchun esa nonce maʼlum boʻlsa, bitta xesh hisoblash yetarli.

## Zanjir ayrilishi va yakuniylik

Ikki mayner bir vaqtda blok topsa, zanjir vaqtincha **ayriladi** (fork). Tugunlar eng koʻp ish sarflangan (odatda eng uzun) zanjirni tanlaydi, qisqa tarmoqdagi blok bekor boʻladi va uning tranzaksiyalari navbatga qaytadi. Shuning uchun PoW da yakuniylik **ehtimoliy**: tasdiqlar koʻpaygan sari tranzaksiyani bekor qilish ehtimoli keskin kamayadi. Ruxsatli tarmoqlardagi BFT kelishuvida esa yakuniylik **darhol**: blok qabul qilingach, u qaytarilmaydi.

Tizimning xavfsizligi bitta asosiy shartga tayanadi: hisoblash quvvati (PoW) yoki garov (PoS) ning **koʻp qismi halol ishtirokchilarda** boʻlishi. Shuning uchun kichik tarmoqlar katta tarmoqlarga qaraganda zaifroq.

:::warning
**Koʻp uchraydigan xatolar**

- **"Mayner tranzaksiyani soxtalashtira oladi" deb oʻylash.** Imzosiz tranzaksiyani u yarata olmaydi; u faqat tartib va kiritishni tanlaydi.
- **Bitta tasdiqni yakuniy deb hisoblash.** Katta summalar uchun bir nechta tasdiq kutiladi.
- **Merkle ildizini oddiy roʻyxat xeshi bilan adashtirish.** Daraxt qisqa isbotlarga imkon beradi.
- **PoS ni "bepul xavfsizlik" deb bilish.** U ham iqtisodiy ragʻbatlar va garovga tayanadi.
:::

## Topshiriq

1. Misolni 5 ta tranzaksiya bilan sinang. Toq sonli darajada nima sodir boʻladi? Merkle daraxtini qoʻlda chizing.
2. 4 ta nol uchun oʻrtacha necha urinish kutiladi? Dasturda tekshiring va vaqtni oʻlchang.
3. Bitta tranzaksiya yaratilishidan 6 ta tasdiqgacha boʻlgan yoʻlni sxema koʻrinishida chizing (MT11 dagi BPMN yoki blok-sxema).
4. PoW va PoS ni energiya, xavfsizlik, markazlashuv xavfi va kirish narxi boʻyicha jadvalda solishtiring.

## Xulosa

- Tranzaksiya: yaratish → imzo → tarqatish → navbat → blok → tasdiqlar.
- Blok sarlavhasi: oldingi xesh, Merkle ildizi, vaqt, qiyinlik, nonce.
- Merkle daraxti bitta ildiz bilan barcha tranzaksiyalarni himoya qiladi va qisqa isbot beradi.
- Konsensus: PoW (hisoblash), PoS (garov), ruxsatli BFT; ish isboti — yaratish qiyin, tekshirish oson.
- PoW da yakuniylik ehtimoliy, BFT da darhol; xavfsizlik halol koʻpchilikka tayanadi.

## Oʻzingizni tekshiring

:::check
Q: Tranzaksiyani kim va qanday kalit bilan imzolaydi?
A: Pul joʻnatuvchi oʻzining yopiq kaliti bilan; boshqalar uning ochiq kaliti bilan tekshiradi.
Q: Merkle ildizi nima?
A: Blokdagi barcha tranzaksiyalarni juftlab qayta-qayta xeshlash natijasida olingan bitta xesh.
Q: Nima uchun ish isbotida "yaratish qiyin, tekshirish oson"?
A: Shartni qanoatlantiruvchi nonce ni topish uchun koʻp urinish kerak, uni tekshirish uchun esa bitta xesh hisoblash yetarli.
Q: Proof of Stake da validator nima uchun halol ishlaydi?
A: Qoidani buzsa, garovga qoʻygan mablagʻini yoʻqotadi.
:::

:::link https://bitcoin.org/bitcoin.pdf
Bitcoin: A Peer-to-Peer Electronic Cash System
Satoshi Nakamotoning blokcheyn ishlash tamoyillarini bayon qilgan asl maqolasi (ingliz tilida).
:::
