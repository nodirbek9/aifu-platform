---
title: Blokcheyn texnologiyasi va undan foydalanish yoʻllari
summary: Blokcheyn tushunchasi — taqsimlangan reyestr, bloklar zanjiri, xesh orqali bogʻlanish; asosiy xossalar — oʻzgarmaslik, shaffoflik, markazsizlik; konsensus mexanizmlari (Proof of Work, Proof of Stake); ommaviy va ruxsatli (xususiy, konsorsium) blokcheynlar; smart-kontraktlar; kriptovalyutalar va raqamli aktivlar; iqtisodiyotda qoʻllanish — taʼminot zanjiri, moliya va toʻlovlar, hujjatlar va diplomlarni tasdiqlash, yer reyestri, davlat xaridlari; Oʻzbekistonda kripto-aktivlar tartibga solinishi; afzalliklar, cheklovlar va xavflar; xesh zanjiri dasturda.
---

## Nima uchun bu mavzu muhim?

Iqtisodiyotda ishonch qimmat: shartnoma, notarius, bank, auditor — barchasi "maʼlumot toʻgʻri va oʻzgartirilmagan" ekanini kafolatlash uchun kerak. **Blokcheyn** bu ishonchni texnologiya orqali beradi: yozuvlar koʻplab ishtirokchilarda nusxada saqlanadi va ularni sezdirmasdan oʻzgartirib boʻlmaydi. Bitkoin bilan mashhur boʻlgan bu texnologiya bugun taʼminot zanjirlari, moliya, davlat reyestrlari va hujjatlarni tasdiqlashda sinovdan oʻtmoqda.

:::note
**Bu darsdan soʻng siz:**

- blokcheyn tushunchasi va bloklar qanday bogʻlanishini tushuntira olasiz;
- konsensus mexanizmlari va blokcheyn turlarini farqlay olasiz;
- smart-kontrakt va raqamli aktiv tushunchalarini bilasiz;
- blokcheynning iqtisodiyotdagi qoʻllanish sohalarini ayta olasiz;
- blokcheyn qachon foydali va qachon ortiqcha ekanini baholay olasiz.
:::

## Blokcheyn tushunchasi

**Blokcheyn** — yozuvlar (tranzaksiyalar) bloklarga guruhlanadigan va har bir blok **oldingi blokning xesh-qiymatini** oʻz ichiga oladigan **taqsimlangan reyestr**. Xesh — maʼlumotning qisqa "barmoq izi": maʼlumot zarracha oʻzgarsa, xesh butunlay boshqacha boʻladi.

| Xossa | Mazmuni |
|---|---|
| **Taqsimlanganlik** | reyestr nusxasi koʻplab ishtirokchilarda (tugunlarda) saqlanadi |
| **Oʻzgarmaslik** | oʻtgan yozuvni oʻzgartirish keyingi barcha bloklarni buzadi va boshqa tugunlar buni rad etadi |
| **Shaffoflik** | ishtirokchilar tranzaksiyalar tarixini koʻradi |
| **Markazsizlik** | yagona markaziy boshqaruvchi yoʻq (ommaviy blokcheynlarda) |
| **Kriptografik himoya** | tranzaksiyalar ERI bilan imzolanadi |

**Konsensus** — tarmoq qaysi blok qoʻshilishini qanday kelishadi:

| Mexanizm | Mazmuni | Misol |
|---|---|---|
| **Proof of Work** (ish isboti) | blok qoʻshish uchun murakkab hisoblash; koʻp elektr energiyasi | Bitkoin |
| **Proof of Stake** (ulush isboti) | garovga qoʻyilgan mablagʻga qarab tanlash; energiya tejamkor | Ethereum (2022-yildan) |
| **Ruxsatli konsensus** | maʼlum ishtirokchilar ovozi | Hyperledger Fabric kabi korporativ tizimlar |

## Blokcheyn turlari

| Tur | Kim qatnashadi | Qoʻllanishi |
|---|---|---|
| **Ommaviy** (public) | istalgan kishi | kriptovalyutalar, ochiq platformalar |
| **Xususiy** (private) | bitta tashkilot | ichki hisob va audit |
| **Konsorsium** | bir nechta tashkilot (banklar, kompaniyalar guruhi) | banklararo hisob-kitoblar, taʼminot zanjiri |

## Smart-kontraktlar va raqamli aktivlar

**Smart-kontrakt** — blokcheynda saqlanadigan va shartlar bajarilganda **avtomatik** ishga tushadigan dastur. Misol: tovar omborga yetib kelgani tasdiqlansa, toʻlov avtomatik yetkazib beruvchiga oʻtadi — vositachi va kechikishsiz.

**Raqamli aktivlar**: kriptovalyutalar (Bitkoin, Ether), stablkoinlar (fiat valyutaga bogʻlangan), tokenlar (aktivga ulush, masalan koʻchmas mulk yoki obligatsiya). Ayrim markaziy banklar **markaziy bank raqamli valyutasi** (CBDC) loyihalarini oʻrganmoqda. Kriptovalyutalar narxi juda oʻzgaruvchan — investitsiya sifatida yuqori xavfli.

**Oʻzbekistonda** kripto-aktivlar aylanmasi qonunchilik bilan tartibga solingan: kripto-birjalar va boshqa xizmat koʻrsatuvchilar vakolatli davlat organidan litsenziya olishi kerak; amaldagi talablarni rasmiy manbalardan tekshiring.

## Iqtisodiyotda qoʻllanish

| Soha | Qoʻllanish | Qiymat |
|---|---|---|
| **Taʼminot zanjiri** | tovarning kelib chiqishi va har bir bosqichi (paxta → ip → mato) | soxtalashtirishga qarshi, shaffof eksport |
| **Moliya** | xalqaro oʻtkazmalar, savdo moliyalashtirish, tokenlashtirilgan aktivlar | tezlik, vositachilarning kamayishi |
| **Hujjatlarni tasdiqlash** | diplomlar, sertifikatlar, shartnomalar xeshini saqlash | soxta hujjatni tez aniqlash |
| **Davlat reyestrlari** | yer va koʻchmas mulk, litsenziyalar | yozuvlarni yashirincha oʻzgartirib boʻlmaydi |
| **Davlat xaridlari** | tender bosqichlari tarixi | shaffoflik |

## Ishlangan misol: xesh zanjiri

Har bir yozuv uchun blok xeshi = *H*(oldingi xesh | yozuv), birinchi blokning oldingi xeshi — 00000000. Oʻquv maqsadida *H* — 32 bitli FNV-1a (haqiqiy blokcheynlarda SHA-256). Taʼminot zanjiri yozuvlari kiritiladi; har bir blokni chiqaring.

```python
import sys


def fnv1a(matn):                                 # oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return h


oldingi = "00000000"
yozuvlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
for i, yozuv in enumerate(yozuvlar, start=1):
    xesh = format(fnv1a(oldingi + "|" + yozuv), "08x")   # blok oldingi xeshga bogʻlanadi
    print(f"blok {i}: {yozuv} | oldingi {oldingi} | xesh {xesh}")
    oldingi = xesh
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {                             // oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
        int h = 0x811C9DC5;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = (h ^ (b & 0xFF)) * 16777619;
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        String oldingi = "00000000";
        int i = 0;
        while (in.hasNextLine()) {
            String yozuv = in.nextLine();
            if (yozuv.isBlank()) continue;
            i++;
            String xesh = String.format("%08x", fnv1a(oldingi + "|" + yozuv));   // blok oldingi xeshga bogʻlanadi
            System.out.println("blok " + i + ": " + yozuv + " | oldingi " + oldingi + " | xesh " + xesh);
            oldingi = xesh;
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {                           // oʻquv xesh-funksiyasi (haqiqiy tizimda SHA-256)
    uint32_t h = 2166136261u;
    for (unsigned char b : matn) h = (h ^ b) * 16777619u;
    return h;
}

int main() {
    string oldingi = "00000000", yozuv;
    int i = 0;
    while (getline(cin, yozuv)) {
        if (!yozuv.empty() && yozuv.back() == '\r') yozuv.pop_back();
        if (yozuv.find_first_not_of(' ') == string::npos) continue;
        i++;
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", fnv1a(oldingi + "|" + yozuv));   // blok oldingi xeshga bogʻlanadi
        string xesh = buf;
        cout << "blok " << i << ": " << yozuv << " | oldingi " << oldingi << " | xesh " << xesh << endl;
        oldingi = xesh;
    }
    return 0;
}
```

Kirish:

```text
Paxta 20t Fermer->Zavod
Ip 15t Zavod->Fabrika
Mato 9000m Fabrika->Eksport
```

Chiqish:

```text
blok 1: Paxta 20t Fermer->Zavod | oldingi 00000000 | xesh 174beafb
blok 2: Ip 15t Zavod->Fabrika | oldingi 174beafb | xesh a6917dc3
blok 3: Mato 9000m Fabrika->Eksport | oldingi a6917dc3 | xesh d839ca87
```

Endi birinchi yozuvda paxta miqdori "20t" oʻrniga "25t" deb oʻzgartirilsa, dastur quyidagini chiqaradi:

```text
blok 1: Paxta 25t Fermer->Zavod | oldingi 00000000 | xesh 1483cdfe
blok 2: Ip 15t Zavod->Fabrika | oldingi 1483cdfe | xesh 2e0c2669
blok 3: Mato 9000m Fabrika->Eksport | oldingi 2e0c2669 | xesh f6e44f26
```

Ikkinchi va uchinchi yozuvlar oʻzgarmagan boʻlsa ham, ularning xeshlari oʻzgardi — oxirgi xesh (d839ca87) boshqa ishtirokchilardagi nusxa bilan mos kelmaydi va soxtalashtirish darhol aniqlanadi. Shuning uchun taʼminot zanjirida har bir ishtirokchi oxirgi xeshni saqlab qoʻysa yetarli. Haqiqiy blokcheynda bunga konsensus va ERI ham qoʻshiladi (KIA kursidagi M9 da ish isboti bilan batafsil).

## Afzalliklar, cheklovlar va xavflar

| Afzallik | Cheklov va xavf |
|---|---|
| vositachisiz ishonch | tezlik past, tranzaksiya narxi baʼzan yuqori |
| oʻzgartirib boʻlmaydigan tarix | xato yozuvni ham "oʻchirib" boʻlmaydi |
| shaffoflik va audit | shaxsiy maʼlumotlar bilan ziddiyat ("unutilish huquqi") |
| avtomatlashtirish (smart-kontraktlar) | smart-kontrakt xatolari katta yoʻqotishlarga olib kelgan |
| | kriptovalyutalar narxining oʻzgaruvchanligi, firibgarlik loyihalari |
| | **"axlat kirsa — axlat chiqadi"**: blokcheyn kiritilgan maʼlumot toʻgʻriligini kafolatlamaydi |

**Qachon blokcheyn kerak?** Bir-biriga toʻliq ishonmaydigan bir nechta tomon bitta umumiy reyestrni yuritishi va uni yagona markazga topshirmasligi kerak boʻlganda. Agar bitta ishonchli tashkilot maʼlumotni boshqarsa — oddiy maʼlumotlar bazasi arzonroq va tezroq.

:::warning
**Koʻp uchraydigan xatolar**

- **Blokcheynni har qanday muammoga yechim deb bilish.** Koʻp holatda oddiy maʼlumotlar bazasi yetarli.
- **Blokcheyn = kriptovalyuta deb tushunish.** Kriptovalyuta — blokcheynning bitta qoʻllanishi.
- **"Kafolatlangan daromad" vaʼda qiluvchi kripto-loyihalarga ishonish.** Bu koʻpincha firibgarlik belgisi.
- **Yopiq kalitni yoʻqotish yoki boshqaga berish.** Raqamli aktiv qaytarib boʻlmaydigan holda yoʻqoladi.
:::

:::tip
Misoldagi dasturda ikkinchi yozuvni oʻzgartirib koʻring: qaysi xeshlar oʻzgaradi va qaysilari oʻzgarmaydi? Natijani bloklar zanjiri sxemasi bilan tushuntiring.
:::

## Xulosa

- Blokcheyn — bloklari oldingi blok xeshi orqali bogʻlangan taqsimlangan reyestr; oʻzgarmas, shaffof, markazsiz.
- Konsensus: Proof of Work, Proof of Stake, ruxsatli; turlari: ommaviy, xususiy, konsorsium.
- Smart-kontrakt shartlar bajarilganda avtomatik ishlaydi; raqamli aktivlar yuqori xavfli.
- Qoʻllanish: taʼminot zanjiri, moliya, hujjatlarni tasdiqlash, reyestrlar, xaridlar.
- Blokcheyn ishonchsiz tomonlar umumiy reyestr yuritganda foydali; aks holda oddiy baza yetarli.

## Oʻzingizni tekshiring

:::check
Q: Blokcheynda har bir blok oldingi blok bilan qanday bogʻlanadi?
A: Oldingi blokning xesh-qiymati orqali.
Q: Taqsimlangan reyestr nima?
A: Koʻplab ishtirokchilarda nusxada saqlanadigan reyestr.
Q: Shartlar bajarilganda avtomatik ishga tushadigan blokcheyndagi dastur nima deb ataladi?
A: Aqlli shartnoma (smart-kontrakt).
Q: Blokcheyn qachon oddiy maʼlumotlar bazasidan afzal?
A: Bir-biriga toʻliq ishonmaydigan bir nechta tomon yagona markazsiz umumiy reyestr yuritishi kerak boʻlganda.
:::

:::link https://www.investopedia.com/terms/b/blockchain.asp
Investopedia: Blockchain
Blokcheyn texnologiyasi va uning iqtisodiyotdagi qoʻllanishi haqida tushuntirish (ingliz tilida).
:::
