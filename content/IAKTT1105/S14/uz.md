---
title: Axborotni kriptografik himoyalash usullari
summary: Seminar — kriptografiya vazifalari (maxfiylik, yaxlitlik, autentifikatsiya, rad etib boʻlmaslik); klassik shifrlar (Sezar shifri) va ularning zaifligi; simmetrik shifrlash (AES) va kalit almashish muammosi; asimmetrik shifrlash (RSA) va ochiq/yopiq kalitlar — qoʻlda hisoblanadigan kichik misol; xesh-funksiyalar; elektron raqamli imzo va sertifikatlar; HTTPS; iqtisodiyotda qoʻllanish (bank, soliq, ESF); amaliy topshiriqlar.
---

## Seminarning maqsadi

M14 maʼruzasida shifrlash himoya choralaridan biri sifatida tilga olindi. Seminarda kriptografiya qanday ishlashini oddiy misollarda qoʻlda hisoblab koʻramiz va uning bank, soliq va elektron hujjat aylanishidagi oʻrnini tahlil qilamiz.

:::note
**Bu seminardan soʻng siz:**

- kriptografiyaning toʻrt vazifasini ayta olasiz;
- simmetrik va asimmetrik shifrlashni farqlay olasiz;
- ochiq va yopiq kalitlarning rolini kichik misolda koʻrsata olasiz;
- xesh, elektron raqamli imzo va sertifikat nima ekanini tushuntira olasiz.
:::

## Kriptografiya vazifalari

| Vazifa | Maʼnosi | Vosita |
|---|---|---|
| **Maxfiylik** | xabarni begona oʻqiy olmaydi | shifrlash |
| **Yaxlitlik** | xabar yoʻlda oʻzgartirilmagan | xesh, imzo |
| **Autentifikatsiya** | xabar haqiqatan kimdan kelgani tasdiqlanadi | imzo, sertifikat |
| **Rad etib boʻlmaslik** | muallif imzolaganini inkor eta olmaydi | elektron raqamli imzo |

Asosiy atamalar: **ochiq matn** — asl xabar; **shifrmatn** — shifrlangan xabar; **kalit** — shifrlash va ochish uchun maxfiy parametr. Zamonaviy kriptografiyaning asosiy tamoyili (**Kerkgoffs tamoyili**): algoritm hammaga maʼlum boʻlishi mumkin, xavfsizlik faqat kalitning maxfiyligiga tayanadi.

## Klassik shifr: Sezar shifri

Har bir harf alifboda bir xil qadamga (kalit) suriladi. Kalit 3 boʻlsa: A → D, B → E, ..., X → A.

```text
Ochiq matn:  BANK
Kalit:       3
Shifrmatn:   EDQN
```

**Zaifligi**: lotin alifbosida faqat 25 ta mumkin kalit — barchasini sinab chiqish bir necha soniya. Bundan tashqari, harflar chastotasi saqlanadi. Bu misol zamonaviy shifrlar nima uchun juda katta kalit maydonidan foydalanishini koʻrsatadi.

## Simmetrik shifrlash

Shifrlash va ochish uchun **bitta kalit** ishlatiladi. Zamonaviy standart — **AES** (128 yoki 256 bitli kalit): 2^128 ta kalitni sinab chiqish amalda imkonsiz. Afzalligi — juda tez, katta hajmdagi maʼlumotlarni (disklar, zaxira nusxalar, trafik) shifrlash uchun qulay.

**Muammo — kalit almashish**: joʻnatuvchi va qabul qiluvchi kalitni qanday qilib xavfsiz kelishib oladi? Agar kalit ochiq kanal orqali yuborilsa, uni ushlab olgan kishi barcha xabarlarni ocha oladi. Bu muammoni asimmetrik kriptografiya hal qiladi.

## Asimmetrik shifrlash

Har bir foydalanuvchida **ikki kalit** bor: **ochiq kalit** (hammaga beriladi) va **yopiq kalit** (faqat egasida). Ochiq kalit bilan shifrlangan xabarni faqat mos yopiq kalit ocha oladi. Shuning uchun Ali Valiga maxfiy xabar yubormoqchi boʻlsa, uni **Valining ochiq kaliti** bilan shifrlaydi.

**Kichik RSA misoli** (haqiqiy kalitlar yuzlab xonali sonlar; bu yerda tushunish uchun kichik sonlar):

```text
1. Ikki tub son: p = 5, q = 11
2. n = p × q = 55;  φ = (p − 1)(q − 1) = 40
3. Ochiq daraja e = 3 (40 bilan umumiy boʻluvchisi yoʻq)
4. Yopiq daraja d: e × d ≡ 1 (mod 40) → d = 27, chunki 3 × 27 = 81 = 2 × 40 + 1
   Ochiq kalit: (e = 3, n = 55);  yopiq kalit: (d = 27, n = 55)

Shifrlash (m = 8):  c = 8^3 mod 55 = 512 mod 55 = 17
Ochish:             m = 17^27 mod 55 = 8
```

Hujumchi n = 55 ni biladi, lekin d ni topish uchun n ni tub koʻpaytuvchilarga ajratishi kerak. 55 uchun bu oson, ammo 600 dan ortiq xonali son uchun mavjud kompyuterlarga amalda imkonsiz — RSA xavfsizligi shunga asoslanadi.

**Amalda gibrid sxema**: asimmetrik kriptografiya sekin, shuning uchun u faqat tasodifiy simmetrik kalitni xavfsiz almashish uchun ishlatiladi, maʼlumotning oʻzi esa AES bilan shifrlanadi. HTTPS aynan shunday ishlaydi.

## Xesh-funksiyalar

**Xesh-funksiya** — istalgan uzunlikdagi maʼlumotdan qisqa, qatʼiy uzunlikdagi "barmoq izi" hosil qiladi (masalan, **SHA-256** — 256 bit). Xususiyatlari: bir xil kirish → bir xil xesh; kirishdagi bitta belgi oʻzgarsa, xesh butunlay oʻzgaradi; xeshdan asl maʼlumotni tiklab boʻlmaydi. Qoʻllanishi: fayl yaxlitligini tekshirish, parollarni saqlash (parolning oʻzi emas, uning xeshi), blokcheyn (M12), imzo.

## Elektron raqamli imzo va sertifikatlar

**Elektron raqamli imzo (ERI)**:

1. Joʻnatuvchi hujjat xeshini hisoblaydi va uni **oʻzining yopiq kaliti** bilan imzolaydi.
2. Qabul qiluvchi imzoni **joʻnatuvchining ochiq kaliti** bilan tekshiradi va hujjat xeshini oʻzi hisoblab solishtiradi.
3. Mos kelsa — hujjat aynan shu muallifdan va oʻzgartirilmagan.

Ochiq kalit haqiqatan shu shaxsga tegishli ekanini **sertifikat** tasdiqlaydi — uni ishonchli **sertifikatlash markazi** beradi. Oʻzbekistonda ERI kalitlari va sertifikatlarini vakolatli markazlar beradi; ERI qonun boʻyicha qoʻlda qoʻyilgan imzo bilan teng (Elektron raqamli imzo toʻgʻrisidagi qonun).

## Iqtisodiyotda qoʻllanish

| Soha | Kriptografiya qayerda |
|---|---|
| **Bank** | HTTPS, karta chiplari, toʻlovlarni tasdiqlash, mobil banking, banklararo kanallar |
| **Soliq** | my.soliq.uz da hisobotlarni ERI bilan imzolash, elektron hisob-fakturalar (ESF) |
| **Elektron hujjat aylanishi** | shartnomalar, buyruqlar, davlat xizmatlari arizalari (my.gov.uz) |
| **Korxona** | noutbuk disklari, zaxira nusxalar, VPN orqali masofaviy ish |

:::warning
**Koʻp uchraydigan xatolar**

- **ERI kalitini (fleshka yoki fayl) boshqalarga berish.** Uning nomidan imzolangan hujjat uchun egasi javob beradi.
- **"Oʻz shifr algoritmimizni" yaratish.** Sinalgan standartlar (AES, RSA, SHA-256) ishlatiladi; xavfsizlik kalit maxfiyligida.
- **Parollarni ochiq holda saqlash.** Tizimlar parolning maxsus xeshini saqlashi kerak.
- **Brauzerdagi sertifikat ogohlantirishlarini eʼtiborsiz qoldirish.** Bu soxta sayt belgisi boʻlishi mumkin.
:::

## Amaliy topshiriqlar

1. Sezar shifri bilan (kalit 5) oʻz ismingizni shifrlang, keyin qoʻshningiz bilan almashib, uning shifrmatnini kalitsiz ochishga urining (hamma kalitlarni sinab).
2. Yuqoridagi RSA misolida m = 2 xabarni shifrlang: c = 2^3 mod 55 = ?
3. Ali Valiga maxfiy hujjat yubormoqda va uni imzolamoqchi. Qaysi kalit bilan shifrlaydi, qaysi kalit bilan imzolaydi?
4. Brauzerda istalgan bank saytini oching, qulf belgisini bosing va sertifikat kimga va kim tomonidan berilganini, amal qilish muddatini yozib oling.
5. Korxonangizdagi hujjat aylanishida ERI qayerlarda ishlatilishi mumkinligini sanang.

## Xulosa

- Kriptografiya vazifalari: maxfiylik, yaxlitlik, autentifikatsiya, rad etib boʻlmaslik.
- Simmetrik shifrlash (AES) — bitta kalit, tez; muammosi — kalit almashish.
- Asimmetrik shifrlash (RSA) — ochiq va yopiq kalit; amalda gibrid sxema (HTTPS).
- Xesh — maʼlumotning barmoq izi; ERI — xeshni yopiq kalit bilan imzolash; sertifikat ochiq kalit egasini tasdiqlaydi.
- Iqtisodiyotda: bank, soliq hisobotlari, ESF, elektron hujjat aylanishi.

## Oʻzingizni tekshiring

:::check
Q: Simmetrik shifrlashning asosiy muammosi nima?
A: Kalitni joʻnatuvchi va qabul qiluvchi orasida xavfsiz almashish.
Q: Xabarni faqat qabul qiluvchi ochishi uchun qaysi kalit bilan shifrlash kerak?
A: Qabul qiluvchining ochiq kaliti bilan.
Q: Elektron raqamli imzo nimani tasdiqlaydi?
A: Hujjat muallifini va hujjat imzolangandan keyin oʻzgartirilmaganini.
Q: Kerkgoffs tamoyili nima deydi?
A: Algoritm hammaga maʼlum boʻlishi mumkin; xavfsizlik faqat kalitning maxfiyligiga tayanadi.
:::

:::link https://www.khanacademy.org/computing/computer-science/cryptography
Khan Academy: Cryptography
Sezar shifridan ochiq kalitli kriptografiyagacha — interaktiv darslar (ingliz tilida).
:::
