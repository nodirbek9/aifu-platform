---
title: Mobil tijorat
summary: Mustaqil ish — mobil tijoratning xususiyatlari va "avval mobil" yondashuvi; mobil kanallar — ilova, mobil veb, PWA, superilova ichidagi mini-dasturlar; mobil toʻlov texnologiyalari — NFC, statik va dinamik QR, tokenizatsiya, raqamli hamyonlar; joylashuvga asoslangan xizmatlar va push-xabarlar; ilova doʻkonlari iqtisodiyoti; ilova koʻrsatkichlari — oʻrnatishlar, DAU/MAU, saqlab qolish (retention), kogorta tahlili; mobil xavfsizlik; kogortalar boʻyicha saqlab qolishni hisoblash dasturi; topshiriq.
---

## Mustaqil ishning maqsadi

M13 da mobil tijorat tushunchasi bilan qisqacha tanishdik. Bugun Oʻzbekistonda Internet foydalanuvchilarining katta qismi tarmoqqa asosan smartfon orqali chiqadi, shuning uchun koʻp kompaniyalar uchun mobil kanal — asosiy kanal. Bu mustaqil ishda mobil tijoratning texnologiyalari, iqtisodiyoti va samaradorlik koʻrsatkichlarini chuqurroq oʻrganamiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- mobil kanal turlarini va ularning afzalliklarini solishtira olasiz;
- mobil toʻlov texnologiyalarini tushuntira olasiz;
- ilova doʻkonlari iqtisodiyotini bilasiz;
- DAU/MAU va saqlab qolish (retention) koʻrsatkichlarini hisoblay va izohlay olasiz.
:::

## Mobil tijoratning xususiyatlari

| Xususiyat | Mazmuni | Biznes uchun imkoniyat |
|---|---|---|
| **Doim yonida** | telefon kun boʻyi foydalanuvchi bilan | istalgan payt xarid, tezkor xabarlar |
| **Joylashuv** | GPS va Wi-Fi orqali joy aniqlanadi | yaqin doʻkon, yetkazish manzili, mahalliy takliflar |
| **Shaxsiylik** | bitta qurilma — bitta foydalanuvchi | shaxsiy tavsiyalar |
| **Kamera va sensorlar** | QR skanerlash, hujjatni suratga olish | toʻlov, masofaviy identifikatsiya (S13) |
| **Biometriya** | barmoq izi, yuz | tez va xavfsiz tasdiqlash |
| **Kichik ekran** | eʼtibor qisqa, kiritish noqulay | oddiy interfeys, bir bosishda toʻlov |

**"Avval mobil"** (mobile first) — xizmat avval telefon uchun loyihalanadi, keyin katta ekranlarga moslashtiriladi.

## Mobil kanallar

| Kanal | Afzallik | Kamchilik |
|---|---|---|
| **Native ilova** (iOS, Android) | eng tez va qulay, push-xabarlar, qurilma imkoniyatlari | ishlab chiqish qimmat, oʻrnatish kerak, doʻkon komissiyasi |
| **Mobil veb-sayt** | oʻrnatishsiz, qidiruvdan topiladi | imkoniyatlar cheklangan, sekinroq |
| **PWA** (progressiv veb-ilova) | veb, lekin ilovaga oʻxshaydi: ekranga qoʻshish, oflayn rejim | iOS da ayrim cheklovlar |
| **Superilova ichidagi mini-dastur** | millionlab foydalanuvchiga tayyor kirish, toʻlov va identifikatsiya tayyor | platforma qoidalari va komissiyasi, mijoz platformaga "tegishli" |
| **Messenjer boti** | Telegram kabi messenjerlarda oddiy buyurtma va xizmat | murakkab katalog uchun noqulay |

## Mobil toʻlov texnologiyalari

- **NFC** — telefon yoki soatni terminalga yaqinlashtirib toʻlash (kontaktsiz).
- **QR toʻlov**: **statik QR** — doʻkonning doimiy kodi, xaridor summani oʻzi kiritadi; **dinamik QR** — har bir xarid uchun summa bilan yangi kod, kassa avtomatik tasdiqlaydi.
- **Tokenizatsiya** — hamyonda karta raqami emas, uning oʻrnini bosuvchi **token** saqlanadi; token oʻgʻirlansa ham haqiqiy karta maʼlumoti oshkor boʻlmaydi (S14).
- **Raqamli hamyonlar** — Apple Pay, Google Pay, Samsung Pay; Oʻzbekistonda toʻlov ilovalari va bank ilovalari ichidagi hamyonlar.
- **Ilovadan ilovaga oʻtkazmalar** — telefon raqami boʻyicha pul yuborish.

## Joylashuv va push-xabarlar

**Joylashuvga asoslangan xizmatlar**: eng yaqin filial, kuryerning real vaqtdagi joyi, "geozonaga" kirganda taklif. **Push-xabarlar**: buyurtma holati, aksiyalar, tashlab ketilgan savat haqida eslatma. Ikkalasi ham foydalanuvchi **roziligi** bilan va oʻlchov bilan ishlatiladi — haddan tashqari koʻp xabar ilovani oʻchirishga olib keladi.

## Ilova doʻkonlari iqtisodiyoti

- App Store va Google Play raqamli tovarlar va obunalar uchun toʻlovlardan **komissiya** oladi (odatda 15–30%, kichik ishlab chiquvchilar va obunaning ikkinchi yili uchun pastroq stavkalar mavjud).
- Jismoniy tovarlar (kiyim, oziq-ovqat) va xizmatlar (taksi) uchun toʻlovlarga bu komissiya odatda qoʻllanmaydi.
- Ilovani topish: doʻkondagi reyting, sharhlar va kalit soʻzlar (**ASO** — ilova doʻkoni uchun optimallashtirish).
- Ilova orqali kelgan mijozning **CAC** i (reklama + oʻrnatish) birlik iqtisodiyotiga kiradi (MT26).

## Ilova koʻrsatkichlari

| Koʻrsatkich | Formula | Maʼnosi |
|---|---|---|
| **Oʻrnatishlar** | yuklab olishlar soni | qamrov, lekin faollik emas |
| **DAU / MAU** | kunlik / oylik faol foydalanuvchilar | "yopishqoqlik": 0,25 — oʻrtacha foydalanuvchi oyda ~7,5 kun kiradi |
| **N-kun saqlab qolish** (retention) | N-kuni qaytganlar / oʻrnatganlar | ilova odatga aylanyaptimi |
| **Konversiya** | xarid qilganlar / faol foydalanuvchilar | M13 |
| **Oʻchirish darajasi** | ilovani oʻchirganlar ulushi | qoniqmaslik belgisi |

**Kogorta tahlili** — foydalanuvchilarni bir vaqtda (masalan, bir oyda) oʻrnatganlar guruhiga (kogortaga) ajratib, har bir guruhning vaqt boʻyicha xatti-harakatini kuzatish. Agar yangi kogortalarda saqlab qolish oshayotgan boʻlsa, ilova yaxshilanmoqda.

## Ishlangan misol: kogortalar boʻyicha saqlab qolish

Har bir satrda kogorta (oʻrnatish oyi), oʻrnatishlar soni va 1-, 7-, 30-kuni qaytgan foydalanuvchilar soni. Har bir kogorta va jami boʻyicha saqlab qolish foizlarini chiqaring.

```python
import sys

kogortalar = []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, ornatish, k1, k7, k30 = satr.split()
    kogortalar.append((nom, int(ornatish), int(k1), int(k7), int(k30)))

def foiz(qism, butun):
    return 100 * qism / butun

for nom, n, a, b, c in kogortalar:
    print(f"{nom}: 1-kun {foiz(a, n):.1f}%, 7-kun {foiz(b, n):.1f}%, 30-kun {foiz(c, n):.1f}%")

# jami — oʻrnatishlar soni boʻyicha vaznli
n = sum(k[1] for k in kogortalar)
a = sum(k[2] for k in kogortalar)
b = sum(k[3] for k in kogortalar)
c = sum(k[4] for k in kogortalar)
print(f"Jami: 1-kun {foiz(a, n):.1f}%, 7-kun {foiz(b, n):.1f}%, 30-kun {foiz(c, n):.1f}%")
```
```java
import java.util.Scanner;

public class Main {
    static double foiz(long qism, long butun) {
        return 100.0 * qism / butun;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = 0, a = 0, b = 0, c = 0;
        while (in.hasNext()) {
            String nom = in.next();
            long ornatish = in.nextLong(), k1 = in.nextLong(), k7 = in.nextLong(), k30 = in.nextLong();
            System.out.println(String.format("%s: 1-kun %.1f%%, 7-kun %.1f%%, 30-kun %.1f%%", nom, foiz(k1, ornatish), foiz(k7, ornatish), foiz(k30, ornatish)));
            n += ornatish; a += k1; b += k7; c += k30;          // jami — oʻrnatishlar soni boʻyicha vaznli
        }
        System.out.println(String.format("Jami: 1-kun %.1f%%, 7-kun %.1f%%, 30-kun %.1f%%", foiz(a, n), foiz(b, n), foiz(c, n)));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

double foiz(long long qism, long long butun) {
    return 100.0 * qism / butun;
}

int main() {
    string nom;
    long long ornatish, k1, k7, k30, n = 0, a = 0, b = 0, c = 0;
    while (cin >> nom >> ornatish >> k1 >> k7 >> k30) {
        printf("%s: 1-kun %.1f%%, 7-kun %.1f%%, 30-kun %.1f%%\n", nom.c_str(), foiz(k1, ornatish), foiz(k7, ornatish), foiz(k30, ornatish));
        n += ornatish; a += k1; b += k7; c += k30;               // jami — oʻrnatishlar soni boʻyicha vaznli
    }
    printf("Jami: 1-kun %.1f%%, 7-kun %.1f%%, 30-kun %.1f%%\n", foiz(a, n), foiz(b, n), foiz(c, n));
    return 0;
}
```

Kirish:

```text
2026-01 2000 800 500 300
2026-02 2500 1100 700 400
2026-03 3000 1500 1050 690
```

Chiqish:

```text
2026-01: 1-kun 40.0%, 7-kun 25.0%, 30-kun 15.0%
2026-02: 1-kun 44.0%, 7-kun 28.0%, 30-kun 16.0%
2026-03: 1-kun 50.0%, 7-kun 35.0%, 30-kun 23.0%
Jami: 1-kun 45.3%, 7-kun 30.0%, 30-kun 18.5%
```

Har bir yangi kogortada saqlab qolish oshmoqda: mart oyida oʻrnatganlarning 23% i bir oydan keyin ham ilovadan foydalanmoqda (yanvarda — 15%). Bu mart oyida kiritilgan oʻzgarishlar (masalan, oddiyroq roʻyxatdan oʻtish yoki birinchi xarid uchun bonus) ishlaganini koʻrsatishi mumkin. Faqat oʻrnatishlar soniga qaralsa, bu muhim yaxshilanish koʻrinmay qolardi.

## Mobil xavfsizlik

- Ilovalarni faqat rasmiy doʻkondan oʻrnatish; ilova soʻraydigan ruxsatlarni (kontaktlar, SMS, joylashuv) tekshirish.
- SMS kodlarni hech kimga bermaslik (S13); ekran ulashish ilovalari orqali firibgarlikdan ehtiyot boʻlish.
- Ishlab chiquvchi uchun: tokenizatsiya, sertifikatni tekshirish, maʼlumotni telefonda shifrlab saqlash, biometrik tasdiqlash.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat oʻrnatishlar sonini oʻlchash.** Muhimi — saqlab qolish va faol foydalanuvchilar.
- **Haddan tashqari koʻp push-xabar.** Foydalanuvchi ilovani oʻchiradi.
- **Ilova doʻkoni komissiyasini birlik iqtisodiyotida hisobga olmaslik.**
- **Mobil versiyani "kichraytirilgan sayt" sifatida qilish.** Mobil foydalanuvchi ehtiyojlari boshqacha.
:::

## Topshiriq

1. Oʻzingiz foydalanadigan ikkita xarid yoki toʻlov ilovasini solishtiring: roʻyxatdan oʻtish qadamlari, toʻlov usullari, push-xabarlar, ruxsatlar.
2. Misol dasturiga aprel kogortasini qoʻshing (3 200 oʻrnatish, 1 700 / 1 200 / 800) va trendni izohlang.
3. Kichik doʻkon uchun mobil kanalni tanlang (native ilova, PWA, mini-dastur yoki bot) va xarajat hamda foyda boʻyicha asoslang.
4. Statik va dinamik QR toʻlovning sotuvchi va xaridor uchun afzalliklarini jadvalda solishtiring.

## Xulosa

- Mobil tijorat: doim yonida, joylashuv, shaxsiylik, kamera, biometriya; "avval mobil" yondashuvi.
- Kanallar: native ilova, mobil veb, PWA, superilova mini-dasturlari, botlar.
- Toʻlov: NFC, statik va dinamik QR, tokenizatsiya, raqamli hamyonlar.
- Koʻrsatkichlar: DAU/MAU, N-kun saqlab qolish, kogorta tahlili; ilova doʻkoni komissiyasi va CAC birlik iqtisodiyotiga kiradi.

## Oʻzingizni tekshiring

:::check
Q: Statik va dinamik QR toʻlovning farqi nima?
A: Statik QR doimiy, summani xaridor kiritadi; dinamik QR har bir xarid uchun summa bilan yaratiladi va avtomatik tasdiqlanadi.
Q: Tokenizatsiya nima uchun kerak?
A: Hamyonda karta raqami oʻrniga token saqlanadi — u oʻgʻirlansa ham haqiqiy karta maʼlumoti oshkor boʻlmaydi.
Q: DAU/MAU = 0,25 nimani bildiradi?
A: Oʻrtacha faol foydalanuvchi oyning taxminan chorak kunida (≈7–8 kun) ilovaga kiradi.
Q: Kogorta tahlili nima uchun kerak?
A: Turli vaqtda kelgan foydalanuvchilar guruhlarining xatti-harakatini solishtirib, mahsulot yaxshilanayotganini aniqlash uchun.
:::

:::link https://www.gsma.com/r/mobileeconomy/
GSMA: The Mobile Economy
Mamlakatlar va mintaqalar boʻyicha mobil iqtisodiyot hisobotlari (ingliz tilida).
:::
