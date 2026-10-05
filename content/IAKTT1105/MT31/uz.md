---
title: Soliq hisobotlarini yuborish
summary: Mustaqil ish — elektron soliq hisobotini yuborishning bosqichma-bosqich tartibi (ERI va kirish, shaklni tanlash, toʻldirish yoki buxgalteriya dasturidan import, tekshirish, imzolash, yuborish, qabul qilinganlik bildirishnomasi); tuzatuvchi hisobot; hisobot taqvimi va muddatlar; elektron hisob-faktura (ESF) hayot sikli — yaratish, imzolash, yuborish, qabul qilish yoki rad etish; QQS mexanizmi va ESF bilan solishtirish; soliq organining avtomatik tahlili; toʻlanadigan QQS ni hisoblash dasturi; hujjatlarni saqlash; topshiriq.
---

## Mustaqil ishning maqsadi

S15 seminarida soliq hisobotlari elektron tarzda topshirilishi va asosiy soliqlar bilan tanishdik, M15 da hisobot tizimlarini koʻrdik. Bu mustaqil ish — **amaliy qoʻllanma**: hisobot qanday tayyorlanadi va yuboriladi, elektron hisob-fakturalar qanday aylanadi va hisobot raqamlari ular bilan qanday solishtiriladi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- elektron hisobotni yuborish bosqichlarini ketma-ket ayta olasiz;
- tuzatuvchi hisobot qachon va qanday topshirilishini bilasiz;
- ESF hayot siklini tushuntira olasiz;
- QQS ni ESF lar asosida hisoblab, hisobot bilan solishtira olasiz.
:::

## Hisobotni yuborish bosqichlari

| Bosqich | Harakat | Eʼtibor bering |
|---|---|---|
| **1. Tayyorgarlik** | amal qiluvchi ERI kaliti (MT30), E-IMZO yoki mobil imzo | kalit muddati va vakolat (rahbar yoki ishonchnoma) |
| **2. Kirish** | soliq toʻlovchining shaxsiy kabineti (my.soliq.uz) ga ERI bilan kirish | faqat rasmiy saytdan (S13 dagi xavfsizlik) |
| **3. Shaklni tanlash** | soliq turi, hisobot davri (oy, chorak, yil) | amaldagi shakl versiyasi |
| **4. Toʻldirish** | qoʻlda yoki buxgalteriya dasturidan (M15) fayl import qilish | raqamlar buxgalteriya registrlariga mos |
| **5. Tekshirish** | tizimning formatli va mantiqiy tekshiruvlari, xatolar roʻyxati | ogohlantirishlarni eʼtiborsiz qoldirmang |
| **6. Imzolash** | ERI bilan imzolash | imzo — hisobot toʻgʻriligi uchun javobgarlik |
| **7. Yuborish** | hisobot soliq organiga joʻnatiladi | muddatning oxirgi kuniga qoldirmang (S15) |
| **8. Bildirishnoma** | qabul qilingani yoki rad etilgani haqida xabar | rad etilsa — sababini tuzatib, qayta yuborish |
| **9. Saqlash** | hisobot, bildirishnoma va asos hujjatlar arxivi | qonunda belgilangan muddat davomida saqlash |

## Tuzatuvchi hisobot

Topshirilgan hisobotda xato topilsa (masalan, kechikib kelgan ESF hisobga olinmagan), **tuzatuvchi hisobot** topshiriladi — u oldingisining oʻrniga butun davr uchun toʻgʻri raqamlarni koʻrsatadi. Agar xato soliqni kamaytirgan boʻlsa, qoʻshimcha soliq va penyani tezroq toʻlash zararni kamaytiradi; xatoni soliq organi oʻzi aniqlasa, jarimalar ham qoʻshilishi mumkin. Tuzatishni soliq tekshiruvi boshlanishidan oldin oʻz vaqtida topshirish muhim.

## Hisobot taqvimi

Har bir soliq turining oʻz davri va muddati bor (oylik, choraklik, yillik). Amaliy usul — **soliq taqvimi**: jadval yoki elektron kalendar, unda har bir hisobot, toʻlov muddati va masʼul xodim belgilanadi; muddatdan 3–5 kun oldin eslatma. Amaldagi muddatlarni Soliq kodeksi va soliq qoʻmitasi saytidan tekshiring — ular oʻzgarib turadi.

## Elektron hisob-faktura (ESF) hayot sikli

```text
Sotuvchi: yaratish → ERI bilan imzolash → yuborish
                                               │
Xaridor:                     qabul qilish (imzolash) yoki rad etish (sababi bilan)
                                               │
                    Qabul qilingan ESF ikkala tomon hisobi va soliq organi tizimida aks etadi
```

- ESF da: tomonlar STIR lari, tovar yoki xizmatlar va ularning identifikatsiya kodlari (IKPU, MT15), miqdor, narx, QQS stavkasi va summasi.
- Xato boʻlsa — tuzatilgan ESF yoki bekor qilish (xaridor roziligi bilan).
- **Faqat qabul qilingan** ESF lar asosida xaridor QQS ni hisobga oladi (kamaytiradi).

## QQS mexanizmi

**Qoʻshilgan qiymat soligʻi** har bir bosqichda faqat **qoʻshilgan qiymatdan** olinadi:

- **Sotuv QQS** (chiqish) — sotilgan tovar va xizmatlar summasiga stavka boʻyicha.
- **Xarid QQS** (kirish) — yetkazib beruvchilardan olingan, qabul qilingan ESF lardagi QQS; u hisobga olinadi.
- **Toʻlanadigan QQS** = sotuv QQS − hisobga olinadigan xarid QQS. Natija manfiy boʻlsa — ortiqcha summa keyingi davrlarga oʻtkaziladi yoki qonunda belgilangan tartibda qaytariladi.

Soliq organi hisobotdagi raqamlarni **ESF va onlayn kassalar maʼlumotlari** bilan avtomatik solishtiradi (S15): tafovut — savol yoki tekshiruv uchun asos. Shuning uchun hisobotdan oldin oʻz ESF laringizni tekshirib chiqish kerak.

## Ishlangan misol: ESF lar boʻyicha QQS

Har bir satrda: tur (`sale` — sotuv, `purchase` — xarid), ESF raqami, QQS siz summa (soʻm) va holat (`accepted` — qabul qilingan, `rejected` — rad etilgan, `pending` — kutilmoqda). Faqat qabul qilingan ESF lar hisobga olinadi; QQS stavkasi oʻquv maqsadida 12%.

```python
import sys

QQS_STAVKA = 12     # foizda; amaldagi stavkani Soliq kodeksidan tekshiring

sotuv_qqs = xarid_qqs = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    tur, raqam, summa, holat = satr.split()
    if holat != "accepted":                           # faqat qabul qilingan ESF hisobga olinadi
        print(f"{raqam}: hisobga olinmadi ({holat})")
        continue
    qqs = int(summa) * QQS_STAVKA // 100
    if tur == "sale":
        sotuv_qqs += qqs
    else:
        xarid_qqs += qqs

print(f"Sotuv QQS: {sotuv_qqs}")
print(f"Xarid QQS (hisobga olinadigan): {xarid_qqs}")
farq = sotuv_qqs - xarid_qqs
if farq >= 0:
    print(f"Toʻlanadigan QQS: {farq}")
else:
    print(f"Ortiqcha QQS (keyingi davrga): {-farq}")
```
```java
import java.util.Scanner;

public class Main {
    static final long QQS_STAVKA = 12;     // foizda; amaldagi stavkani Soliq kodeksidan tekshiring

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long sotuvQqs = 0, xaridQqs = 0;
        while (in.hasNext()) {
            String tur = in.next(), raqam = in.next();
            long summa = in.nextLong();
            String holat = in.next();
            if (!holat.equals("accepted")) {                  // faqat qabul qilingan ESF hisobga olinadi
                System.out.println(raqam + ": hisobga olinmadi (" + holat + ")");
                continue;
            }
            long qqs = summa * QQS_STAVKA / 100;
            if (tur.equals("sale")) sotuvQqs += qqs;
            else xaridQqs += qqs;
        }
        System.out.println("Sotuv QQS: " + sotuvQqs);
        System.out.println("Xarid QQS (hisobga olinadigan): " + xaridQqs);
        long farq = sotuvQqs - xaridQqs;
        if (farq >= 0) System.out.println("Toʻlanadigan QQS: " + farq);
        else System.out.println("Ortiqcha QQS (keyingi davrga): " + (-farq));
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const long long QQS_STAVKA = 12;     // foizda; amaldagi stavkani Soliq kodeksidan tekshiring

int main() {
    string tur, raqam, holat;
    long long summa, sotuvQqs = 0, xaridQqs = 0;
    while (cin >> tur >> raqam >> summa >> holat) {
        if (holat != "accepted") {                            // faqat qabul qilingan ESF hisobga olinadi
            cout << raqam << ": hisobga olinmadi (" << holat << ")\n";
            continue;
        }
        long long qqs = summa * QQS_STAVKA / 100;
        if (tur == "sale") sotuvQqs += qqs;
        else xaridQqs += qqs;
    }
    cout << "Sotuv QQS: " << sotuvQqs << "\n";
    cout << "Xarid QQS (hisobga olinadigan): " << xaridQqs << "\n";
    long long farq = sotuvQqs - xaridQqs;
    if (farq >= 0) cout << "Toʻlanadigan QQS: " << farq << "\n";
    else cout << "Ortiqcha QQS (keyingi davrga): " << -farq << "\n";
    return 0;
}
```

Kirish:

```text
sale ESF-101 25000000 accepted
sale ESF-102 18000000 accepted
sale ESF-103 7000000 rejected
purchase ESF-201 12000000 accepted
purchase ESF-202 9500000 pending
purchase ESF-203 6000000 accepted
```

Chiqish:

```text
ESF-103: hisobga olinmadi (rejected)
ESF-202: hisobga olinmadi (pending)
Sotuv QQS: 5160000
Xarid QQS (hisobga olinadigan): 2160000
Toʻlanadigan QQS: 3000000
```

Eʼtibor bering:

- **ESF-202** (xarid, kutilmoqda) hisobga olinmadi — yetkazib beruvchi yoki oʻzingizning xodimingiz uni hali qabul qilmagan. Agar u hisobot topshirilgandan keyin qabul qilinsa, 1 140 000 soʻm QQS keyingi davrda yoki tuzatuvchi hisobot orqali hisobga olinadi. Hisobotdan oldin "kutilmoqda" holatidagi ESF larni tekshirish — pulni tejash.
- **ESF-103** (sotuv, rad etilgan) — xaridor rozi boʻlmagan: sababini aniqlab, tuzatilgan ESF yuborish kerak; aks holda haqiqiy sotuv va hisobot raqamlari farq qiladi.
- Bu hisob-kitob soliq organi tizimidagi avtomatik solishtiruvning soddalashtirilgan koʻrinishi.

## Hujjatlarni saqlash va javobgarlik

- Hisobotlar, bildirishnomalar, ESF lar va asos hujjatlar qonunda belgilangan muddat davomida saqlanadi (elektron arxiv, zaxira nusxa — M14).
- Hisobotni imzolagan shaxs uning toʻgʻriligi uchun javob beradi; ERI ni boshqaga topshirish — katta xavf (MT30).
- Muddat buzilishi, kam hisoblash va hisobot topshirmaslik uchun jarima va penyalar belgilangan — amaldagi miqdorlarni Soliq kodeksidan tekshiring.

:::warning
**Koʻp uchraydigan xatolar**

- **Oxirgi kunni kutish.** ERI muddati tugagan yoki tizim band boʻlishi mumkin.
- **"Kutilmoqda" va rad etilgan ESF larni tekshirmaslik.** Hisobga olinadigan QQS yoʻqoladi yoki tafovut chiqadi.
- **Tizim ogohlantirishlarini eʼtiborsiz qoldirish.** Formatli xatolar hisobotning rad etilishiga olib keladi.
- **Xatoni tuzatuvchi hisobot bilan oʻz vaqtida tuzatmaslik.** Penya kun sayin oshadi.
- **Bildirishnomani saqlamaslik.** Hisobot oʻz vaqtida topshirilganini isbotlash qiyinlashadi.
:::

## Topshiriq

1. Kichik korxona (QQS toʻlovchi, 5 xodim) uchun bir choraklik soliq taqvimini tuzing: hisobotlar, toʻlovlar, masʼullar, eslatmalar.
2. Misolga yana ikkita ESF qoʻshing (biri xarid, qabul qilingan; biri sotuv, kutilmoqda) va natijani qoʻlda tekshiring.
3. Xarid ESF lari sotuvdan koʻp boʻlgan holat uchun maʼlumot tuzing va dastur "ortiqcha QQS" ni toʻgʻri chiqarishini tekshiring.
4. Hisobot yuborishning 9 bosqichini blok-sxema yoki BPMN koʻrinishida chizing (MT11).

## Xulosa

- Hisobot yuborish: ERI → kirish → shakl → toʻldirish yoki import → tekshirish → imzo → yuborish → bildirishnoma → saqlash.
- Xato topilsa — tuzatuvchi hisobot; muddatlar soliq taqvimi bilan nazorat qilinadi.
- ESF: yaratish, imzolash, yuborish, qabul qilish yoki rad etish; xaridor faqat qabul qilingan ESF dagi QQS ni hisobga oladi.
- Toʻlanadigan QQS = sotuv QQS − hisobga olinadigan xarid QQS; soliq organi hisobotni ESF va onlayn kassalar bilan avtomatik solishtiradi.

## Oʻzingizni tekshiring

:::check
Q: Topshirilgan hisobotda xato topilsa, nima qilinadi?
A: Butun davr uchun toʻgʻri raqamlar bilan tuzatuvchi hisobot topshiriladi.
Q: Xaridor qaysi ESF lardagi QQS ni hisobga oladi?
A: Faqat qabul qilingan (imzolangan) ESF lardagi.
Q: Toʻlanadigan QQS qanday hisoblanadi?
A: Sotuv QQS dan hisobga olinadigan xarid QQS ayiriladi.
Q: Nima uchun hisobotdan oldin "kutilmoqda" holatidagi ESF larni tekshirish kerak?
A: Ular qabul qilinmaguncha xarid QQS hisobga olinmaydi va toʻlanadigan soliq ortiqcha boʻladi.
:::

:::link https://my.soliq.uz/
Soliq toʻlovchining shaxsiy kabineti
Elektron hisobotlar, ESF va soliq xizmatlari.
:::
