---
title: Kompyuter viruslari va virusdan himoyalanish usullari. Xorijiy davlatlarning elektron raqamli imzo algoritmlari tahlili. Identifikatsiya va autentifikatsiya tushunchasi va vazifalari
summary: Kompyuter virusi tushunchasi va boshqa zararli dasturlardan farqi; viruslarning yashash muhiti boʻyicha tasnifi (fayl, yuklash sektori, makro, skript) va hayot sikli; zararlanish belgilari; himoya usullari — profilaktika, aniqlash, tiklash; xorijiy ERI algoritmlari — RSA-PSS, DSA, ECDSA, EdDSA (AQSH, xalqaro), GOST R 34.10-2012 (Rossiya), SM2 (Xitoy), Oʻz DSt 1092:2009; postkvant imzolar — ML-DSA, SLH-DSA; algoritmlarni solishtirish mezonlari; identifikatsiya va autentifikatsiyaning vazifalari.
---

## Nima uchun bu mavzu muhim?

Mavzu uch qismdan iborat, lekin ularni bitta savol birlashtiradi: **tizimga kimning (yoki nimaning) kodi va hujjati ishonchli?** Virus — ruxsatsiz kod; ERI — hujjat va dastur kim tomonidan yaratilganini isbotlash vositasi (masalan, imzolangan yangilanishlar viruslarni almashtirishga yoʻl qoʻymaydi); identifikatsiya va autentifikatsiya — tizimdan kim foydalanayotganini aniqlash. Turli davlatlar oʻz ERI standartlarini ishlab chiqqan, xalqaro hamkorlikda ularning farqini bilish muhim.

:::note
**Bu mustaqil ishdan soʻng siz:**

- kompyuter virusini boshqa zararli dasturlardan farqlay olasiz va viruslarni tasniflay olasiz;
- zararlanish belgilarini va himoya choralarini bilasiz;
- asosiy xorijiy ERI algoritmlarini va ularning matematik asosini ayta olasiz;
- ERI algoritmlarini xavfsizlik, kalit uzunligi va tezlik boʻyicha solishtira olasiz;
- identifikatsiya va autentifikatsiyaning vazifalarini tushuntira olasiz.
:::

## Kompyuter virusi

**Kompyuter virusi** — boshqa dastur, hujjat yoki tizim sohasiga oʻz kodini qoʻshib (zararlab) koʻpayadigan va zararlangan obyekt ishga tushganda faollashadigan dastur. Uning asosiy farqi — **xost obyektga** muhtojligi: qurt (worm) mustaqil tarqaladi, troyan esa foydali dastur qiyofasiga kiradi, lekin oʻzi koʻpaymaydi (MT4, MT5 da batafsil tasnif).

**Yashash muhiti boʻyicha tasnif**:

| Tur | Nimani zararlaydi | Tarixiy misol |
|---|---|---|
| **Fayl viruslari** | bajariladigan fayllar | 1990-yillardagi DOS viruslari |
| **Yuklash viruslari** | disk yoki flesh-xotiraning yuklash sektori | Brain (1986), Michelangelo |
| **Makroviruslar** | ofis hujjatlaridagi makroslar | Melissa (1999) |
| **Skript viruslari** | skript fayllari (VBS, JS) | ILOVEYOU (2000, aslida qurt xususiyatlari ham bor) |

Zamonaviy hujumlarda "klassik" viruslar kam uchraydi; bugun asosiy tahdid — qurtlar, troyanlar, shifrlovchi viruslar va fayl yaratmasdan ishlaydigan zararli dasturlar. Lekin "virus" soʻzi kundalik tilda barcha zararli dasturlar uchun ishlatiladi.

**Zararlanish belgilari**: kompyuter keskin sekinlashadi; notanish jarayonlar, dasturlar yoki brauzer kengaytmalari paydo boʻladi; antivirus oʻchib qoladi yoki yangilanmaydi; fayllar oʻzgaradi yoki ochilmaydi; notanish tarmoq faolligi; doʻstlarga sizning nomingizdan xabarlar ketadi.

**Himoya usullari**:

| Bosqich | Choralar |
|---|---|
| **Profilaktika** | OT va dasturlarni yangilash, faqat rasmiy manbalardan oʻrnatish, makroslarni standart oʻchirilgan holda saqlash, kundalik ish standart hisobda (MT17), olinadigan tashuvchilar nazorati, xabardorlik |
| **Aniqlash** | antivirus yoki EDR (MT9), jurnallarni kuzatish, fayl yaxlitligini tekshirish (A8) |
| **Javob va tiklash** | zararlangan qurilmani tarmoqdan ajratish, toʻliq tekshiruv, kerak boʻlsa OT ni qayta oʻrnatish, zaxira nusxadan tiklash (MT17), parollarni almashtirish |

## Xorijiy ERI algoritmlari

| Algoritm | Davlat / standart | Matematik asos | Xususiyatlari |
|---|---|---|---|
| **RSA-PSS** (PKCS #1) | AQSH, xalqaro (RFC 8017) | katta sonlarni koʻpaytuvchilarga ajratish qiyinligi | eng keng tarqalgan; kalit 2048–4096 bit; imzo kalit uzunligiga teng |
| **DSA** | AQSH, FIPS 186 | chekli maydonda diskret logarifm | FIPS 186-5 da yangi imzolar uchun endi tavsiya etilmaydi |
| **ECDSA** | AQSH (FIPS 186-5), xalqaro | elliptik egri chiziqda diskret logarifm (P-256, P-384) | qisqa kalit (256 bit ≈ RSA 3072); TLS, bitkoin; har bir imzo uchun tasodifiy son toʻgʻri tanlanishi shart |
| **EdDSA (Ed25519)** | xalqaro (RFC 8032), FIPS 186-5 | Edwards egri chizigʻi | tez, deterministik (tasodifiy son xatosi xavfi yoʻq), SSH va zamonaviy protokollarda |
| **GOST R 34.10-2012** | Rossiya | elliptik egri chiziq | 256 va 512 bitli variantlar, xesh — GOST R 34.11-2012 (Streebog) |
| **SM2** | Xitoy | elliptik egri chiziq | xesh — SM3; Xitoy tijorat kriptografiyasi standarti |
| **Oʻz DSt 1092:2009** | Oʻzbekiston | diskret logarifm | milliy ERI standarti, xesh — Oʻz DSt 1106:2009 (M8) |
| **ML-DSA** (FIPS 204, 2024) | AQSH, postkvant | modul panjaralar (lattice) | kvant kompyuterlarga chidamli deb hisoblanadi; imzo va kalit kattaroq |
| **SLH-DSA** (FIPS 205, 2024) | AQSH, postkvant | faqat xesh-funksiyalar | juda konservativ xavfsizlik, lekin imzo katta va sekin |

**Solishtirish mezonlari**: xavfsizlik darajasi (bitlarda); kalit va imzo uzunligi; imzolash va tekshirish tezligi; amalga oshirish xatolariga chidamlilik (masalan, ECDSA da tasodifiy sonni takrorlash yopiq kalitni oshkor qiladi — 2010-yilda Sony PlayStation 3 kalitining sizishi shunday sodir boʻlgan); standartlashtirish va qonuniy tan olinishi; **kvant kompyuterlar tahdidi** — Shor algoritmi RSA va elliptik egri chiziqlarni buzishi mumkin, shuning uchun postkvant algoritmlarga oʻtish rejalashtirilmoqda.

**Nima uchun davlatlar oʻz standartlarini yaratadi**: kriptografik mustaqillik, xorijiy algoritmlarga ishonch masalasi, milliy sertifikatlash tizimi. Natijada xalqaro hujjat almashinuvida bir nechta algoritmni qoʻllab-quvvatlash yoki oʻzaro tan olish kelishuvlari kerak boʻladi.

## Identifikatsiya va autentifikatsiya vazifalari

M10 da batafsil koʻrilgan; qisqacha:

- **Identifikatsiya** — subyektga noyob identifikator berish va uni tanish (login, ID raqam, sertifikatdagi Subject).
- **Autentifikatsiya** — daʼvo qilingan shaxsni tasdiqlash (bilim, egalik, xususiyat omillari; MFA).
- **Vazifalari**: ruxsatsiz kirishning oldini olish; avtorizatsiya uchun asos (M11); hisobdorlik — kim nima qilganini jurnallarda qayd etish; rad etib boʻlmaslik — ERI bilan birga harakat aynan shu shaxs tomonidan bajarilganini isbotlash.

ERI ham autentifikatsiya vositasi: sertifikat egasining identifikatorini (Subject) uning ochiq kaliti bilan bogʻlaydi, yopiq kalit bilan imzolash esa uning egaligini isbotlaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Har qanday zararli dasturni "virus" deb atash.** Ilmiy ishda tasnifni aniq qoʻllang: virus, qurt, troyan va hokazo.
- **Hujjatdagi makroslarni "ochish uchun" yoqish.** Notanish manbadan kelgan hujjatda makroslarni yoqmang.
- **ERI algoritmini faqat kalit uzunligi boʻyicha solishtirish.** 256 bitli ECDSA kaliti 3072 bitli RSA kalitiga teng xavfsizlik beradi.
- **ECDSA da tasodifiy sonni notoʻgʻri yaratish.** Bitta xato yopiq kalitni oshkor qiladi; tekshirilgan kutubxonalardan foydalaning.
- **Kvant tahdidini eʼtiborsiz qoldirish.** Uzoq muddat saqlanadigan imzolar va shifrlangan maʼlumotlar uchun postkvant rejalashtirish kerak.
:::

:::tip
Referatda algoritmlarni jadvalda solishtirib, har biri uchun rasmiy standart hujjatiga havola bering (FIPS 186-5, FIPS 204, RFC 8032, GOST R 34.10-2012, Oʻz DSt 1092:2009). Oʻz kompyuteringizda `openssl list -signature-algorithms` buyrugʻi qaysi imzo algoritmlari qoʻllab-quvvatlanishini koʻrsatadi.
:::

## Xulosa

- Virus xost obyektni zararlab koʻpayadi; yashash muhiti boʻyicha — fayl, yuklash, makro va skript viruslari.
- Himoya: profilaktika (yangilanish, rasmiy manbalar, makroslarni cheklash, standart hisob), aniqlash (antivirus, EDR), tiklash (izolyatsiya, zaxira nusxa).
- ERI algoritmlari: RSA-PSS, DSA, ECDSA, EdDSA, GOST R 34.10-2012, SM2, Oʻz DSt 1092:2009; postkvant — ML-DSA, SLH-DSA.
- Solishtirish: xavfsizlik, kalit va imzo hajmi, tezlik, amalga oshirish xatolariga chidamlilik, standartlashtirish, kvant tahdidi.
- Identifikatsiya va autentifikatsiya — ruxsatsiz kirishni oldini olish, avtorizatsiya, hisobdorlik va rad etib boʻlmaslik asosi.

## Oʻzingizni tekshiring

:::check
Q: Virus qurtdan nimasi bilan farq qiladi?
A: Virus koʻpayishi uchun xost obyekt (fayl, hujjat, yuklash sektori) kerak; qurt mustaqil tarqaladi.
Q: Nima uchun Ed25519 ECDSA ga nisbatan amalga oshirish xatolariga chidamliroq?
A: U deterministik: har bir imzo uchun tasodifiy son yaratish talab qilinmaydi, shuning uchun bu xato bilan kalit oshkor boʻlmaydi.
Q: Rossiya va Xitoyning ERI standartlari qanday nomlanadi?
A: GOST R 34.10-2012 (Rossiya) va SM2 (Xitoy).
Q: Postkvant imzo algoritmlariga nima uchun oʻtish rejalashtirilmoqda?
A: Kvant kompyuterlarda Shor algoritmi RSA va elliptik egri chiziqqa asoslangan imzolarni buzishi mumkin.
:::

:::link https://csrc.nist.gov/pubs/fips/186-5/final
FIPS 186-5: Digital Signature Standard (DSS)
RSA, ECDSA va EdDSA imzo algoritmlarining amaldagi AQSH standarti (ingliz tilida).
:::
