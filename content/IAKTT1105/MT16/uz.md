---
title: Axborot tizimlarining tasnifi
summary: Mustaqil ish — axborot tizimlarini turli belgilar boʻyicha tasniflash: avtomatlashtirish darajasi (qoʻlda, avtomatlashtirilgan, avtomatik), qayta ishlash xususiyati (axborot-qidiruv, axborot-hal qiluvchi — boshqaruv va maslahat beruvchi), boshqaruv darajasi, funksional soha, qoʻllanish sohasi (bank, soliq, statistika, buxgalteriya, geoaxborot, tibbiyot, taʼlim), masshtab (shaxsiy, guruh, korporativ, davlat), arxitektura (fayl-server, mijoz-server, koʻp qatlamli, bulutli), maʼlumot turi (faktografik, hujjatli); tasniflash jadvalini tuzish topshiriqi.
---

## Mustaqil ishning maqsadi

M9 da axborot tizimlari boshqaruv darajalari (TPS, MIS, DSS, EIS) va funksional sohalar boʻyicha tasniflandi. Lekin bitta tizimni koʻplab boshqa belgilar boʻyicha ham tavsiflash mumkin. Bu mustaqil ishda tasniflashning toʻliq tizimini tuzamiz — bu tizimni tanlash, solishtirish va hujjatlashtirishda kerak boʻladi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- axborot tizimlarini kamida sakkizta belgi boʻyicha tasniflay olasiz;
- har bir sinfga Oʻzbekistondan va dunyodan misol keltira olasiz;
- real tizim uchun toʻliq tasniflash "pasporti"ni tuza olasiz.
:::

## Avtomatlashtirish darajasi boʻyicha

| Tur | Mazmuni | Misol |
|---|---|---|
| **Qoʻlda** | barcha amallarni odam bajaradi, kompyutersiz | qogʻoz jurnal, kartoteka |
| **Avtomatlashtirilgan** | odam va kompyuter birgalikda; qaror odamda | buxgalteriya dasturi, CRM — iqtisodiyotdagi tizimlarning aksariyati |
| **Avtomatik** | odam aralashuvisiz | avtomatik toʻlov oʻtkazmalari, ishlab chiqarish liniyasini boshqarish, birjadagi algoritmik savdo |

## Qayta ishlash xususiyati boʻyicha

| Tur | Mazmuni | Misol |
|---|---|---|
| **Axborot-qidiruv** | maʼlumotni topish va berish, hisob-kitobsiz | Lex.uz, kutubxona katalogi, maʼlumotnoma tizimlari |
| **Axborot-hal qiluvchi: boshqaruv** | hisob-kitob qilib, boshqaruv qarori uchun natija beradi | ish haqi, ombor, rejalashtirish |
| **Axborot-hal qiluvchi: maslahat beruvchi** | variantlar va tavsiyalar beradi, qarorni odam qabul qiladi | qarorlarni qoʻllab-quvvatlash (DSS), ekspert tizimlar, skoring |

## Boshqaruv darajasi va funksional soha boʻyicha

Bu M9 da batafsil koʻrilgan: **operatsion** (TPS), **boshqaruv** (MIS), **qarorlarni qoʻllab-quvvatlash** (DSS), **rahbariyat** (EIS); funksional — **buxgalteriya**, **kadrlar**, **marketing**, **ishlab chiqarish**, **moliya**; korporativ — **ERP**, **CRM**, **SCM**.

## Qoʻllanish sohasi boʻyicha

| Soha | Tizimlar |
|---|---|
| **Bank** | bank ABS, karta protsessingi, internet-banking (S13, M15) |
| **Soliq** | soliq toʻlovchining shaxsiy kabineti, ESF, onlayn kassalar (S15) |
| **Statistika** | statistik hisobotlarni yigʻish va qayta ishlash tizimlari |
| **Buxgalteriya** | 1C, xalqaro analoglar (M15) |
| **Davlat boshqaruvi** | yagona interaktiv davlat xizmatlari portali, idoralararo tizimlar (M10) |
| **Geoaxborot (GAT, GIS)** | xarita asosidagi maʼlumotlar: yer kadastri, logistika marshrutlari, savdo nuqtalari tahlili |
| **Tibbiyot** | elektron tibbiy kartalar, shifoxona tizimlari |
| **Taʼlim** | LMS, talabalar kontingenti hisobi |
| **Huquqiy** | qonunchilik bazalari, sud hujjatlari tizimlari |

## Masshtab boʻyicha

| Tur | Foydalanuvchilar | Misol |
|---|---|---|
| **Shaxsiy (yakka)** | bitta foydalanuvchi | uy byudjeti ilovasi, shaxsiy Excel modeli |
| **Guruh** | boʻlim yoki kichik jamoa (oʻnlab kishi) | boʻlim hujjatlari, loyiha boshqaruvi |
| **Korporativ** | butun tashkilot, filiallar bilan | ERP, bank ABS |
| **Tarmoqlararo / davlat** | koʻp tashkilotlar va fuqarolar | soliq tizimi, davlat xizmatlari portali |
| **Global** | butun dunyo boʻyicha | SWIFT, xalqaro bron qilish tizimlari |

## Arxitektura boʻyicha

| Tur | Mazmuni | Xususiyati |
|---|---|---|
| **Lokal (yakka kompyuter)** | dastur va maʼlumot bitta kompyuterda | oddiy; hamkorlik yoʻq |
| **Fayl-server** | maʼlumot fayllari umumiy serverda, qayta ishlash har bir kompyuterda | tarmoq yuklamasi katta, koʻp foydalanuvchida sekin |
| **Mijoz-server** | MBBT serveri soʻrovlarni qayta ishlaydi, mijoz faqat natijani oladi | ishonchli, koʻp foydalanuvchiga mos |
| **Koʻp qatlamli** | taqdimot, biznes-mantiq va maʼlumotlar alohida (MT15) | masshtablanadi, veb va mobil ilovalar |
| **Bulutli (SaaS)** | tizim provayder serverida, brauzer orqali (M11) | tez boshlash, obuna toʻlovi |

## Maʼlumot turi boʻyicha

- **Faktografik** — aniq faktlar tuzilmali koʻrinishda: raqamlar, sanalar, nomlar (buxgalteriya, ombor).
- **Hujjatli** — matnli hujjatlar va ularni qidirish: elektron hujjat aylanishi, arxiv, Lex.uz.
- **Multimedia va fazoviy** — rasm, video, xaritalar (GAT, videokuzatuv).

## Tasniflash "pasporti" — misol

**Tizim**: yagona interaktiv davlat xizmatlari portali (my.gov.uz).

| Belgi | Sinf |
|---|---|
| Avtomatlashtirish darajasi | avtomatlashtirilgan (koʻp xizmatlar avtomatik) |
| Qayta ishlash xususiyati | axborot-hal qiluvchi (ariza qabul qilish va qayta ishlash) + axborot-qidiruv (xizmatlar katalogi) |
| Boshqaruv darajasi | operatsion (G2C xizmatlar) |
| Qoʻllanish sohasi | davlat boshqaruvi |
| Masshtab | davlat miqyosida |
| Arxitektura | koʻp qatlamli, veb va mobil, idoralararo integratsiya bilan |
| Maʼlumot turi | faktografik va hujjatli |

:::tip
Tasniflash nazariy mashq emas: tizimni sotib olish yoki ishlab chiqishda "pasport" talablarni aniqlashtiradi — masalan, "korporativ, mijoz-server, avtomatlashtirilgan, buxgalteriya" degan tavsif yetkazib beruvchilarni solishtirishni osonlashtiradi.
:::

:::warning
**Koʻp uchraydigan xatolar**

- **Bitta tizimni faqat bitta sinfga kiritish.** Tasniflash koʻp oʻlchovli — har bir belgi boʻyicha alohida.
- **"Avtomatlashtirilgan" va "avtomatik"ni adashtirish.** Birinchisida qaror odamda qoladi.
- **Fayl-server va mijoz-serverni farqlamaslik.** Koʻp foydalanuvchida fayl-server keskin sekinlashadi.
- **Bulutli tizimni "Internetdagi sayt" deb soddalashtirish.** Unda maʼlumot joylashuvi, SLA va xavfsizlik masalalari bor (S11).
:::

## Topshiriq

1. Uchta tizim uchun tasniflash "pasporti"ni tuzing: (a) bank mobil ilovasi, (b) universitet LMS, (c) kichik doʻkondagi onlayn kassa.
2. Har bir belgi boʻyicha (8 ta) Oʻzbekistondan kamida bitta misol topib, jadval tuzing.
3. Bitta tizim vaqt oʻtishi bilan sinfini qanday oʻzgartirganini tasvirlang (masalan, fayl-serverli buxgalteriyadan bulutli buxgalteriyaga).

## Xulosa

- Axborot tizimlari koʻp belgilar boʻyicha tasniflanadi: avtomatlashtirish, qayta ishlash xususiyati, boshqaruv darajasi, funksiya, soha, masshtab, arxitektura, maʼlumot turi.
- Iqtisodiyotdagi tizimlarning aksariyati — avtomatlashtirilgan va axborot-hal qiluvchi.
- Arxitektura lokal va fayl-serverdan mijoz-server, koʻp qatlamli va bulutli tizimlarga oʻtib bormoqda.
- Tasniflash "pasporti" tizimni tanlash va solishtirishda amaliy vosita.

## Oʻzingizni tekshiring

:::check
Q: Avtomatlashtirilgan va avtomatik tizimning farqi nima?
A: Avtomatlashtirilganda odam va kompyuter birgalikda ishlaydi va qarorni odam qabul qiladi; avtomatikda jarayon odam aralashuvisiz bajariladi.
Q: Axborot-qidiruv tizimiga misol keltiring.
A: Lex.uz qonunchilik bazasi yoki kutubxona katalogi.
Q: Fayl-server va mijoz-server arxitekturasining asosiy farqi nima?
A: Fayl-serverda maʼlumot har bir kompyuterda qayta ishlanadi; mijoz-serverda soʻrovni server bajarib, mijozga faqat natijani yuboradi.
Q: Geoaxborot tizimi iqtisodiyotda qayerda ishlatiladi?
A: Yer kadastri, logistika marshrutlari, savdo nuqtalari joylashuvini tahlil qilishda.
:::

:::link https://my.gov.uz/
Yagona interaktiv davlat xizmatlari portali
Davlat miqyosidagi axborot tizimiga misol.
:::
