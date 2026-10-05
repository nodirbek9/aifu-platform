---
title: Bulutli texnologiyalarni turli sohalarda amalga oshirish tamoyillari
summary: Seminar — bulutga oʻtish tamoyillari va bosqichlari (baholash, rejalashtirish, koʻchirish, optimallashtirish); koʻchirish strategiyalari ("6R": rehost, replatform, refactor, repurchase, retain, retire); sohalar boʻyicha qoʻllanish — taʼlim, sogʻliqni saqlash, bank va moliya, savdo, davlat boshqaruvi, kichik biznes; huquqiy talablar (shaxsiy maʼlumotlarni lokallashtirish); xarajatlarni boshqarish (FinOps); keys va guruh topshiriqlari.
---

## Seminarning maqsadi

M11 maʼruzasida bulut modellari va iqtisodiy jihatlarini koʻrdik. Seminarda tashkilot bulutga **qanday oʻtishi** — qaysi bosqichlar, strategiyalar va xavflar borligini — tahlil qilamiz va turli sohalar uchun bulut yechimlarini taklif qilamiz.

:::note
**Bu seminardan soʻng siz:**

- bulutga oʻtish bosqichlari va tamoyillarini ayta olasiz;
- "6R" koʻchirish strategiyalarini farqlay olasiz;
- turli sohalarda bulutdan foydalanish misollarini keltira olasiz;
- huquqiy talablar va xarajatlarni boshqarishni hisobga olib reja tuza olasiz.
:::

## Bulutga oʻtish bosqichlari

| Bosqich | Mazmuni |
|---|---|
| **1. Baholash** | mavjud tizimlar va maʼlumotlar roʻyxati, ularning muhimligi, maxfiylik darajasi, TCO hisobi (M2) |
| **2. Rejalashtirish** | qaysi tizim qachon va qaysi strategiya bilan koʻchiriladi; model (SaaS, PaaS, IaaS) va provayder tanlash; xavfsizlik va zaxira talablari |
| **3. Koʻchirish** | pilot loyiha, maʼlumotlarni koʻchirish, sinov, foydalanuvchilarni oʻqitish |
| **4. Optimallashtirish** | xarajatlarni kuzatish, resurslarni moslashtirish, avtomatlashtirish |

**"6R" strategiyalari**:

| Strategiya | Mazmuni | Misol |
|---|---|---|
| **Rehost** ("koʻtarib koʻchirish") | tizim oʻzgarishsiz virtual serverga (IaaS) | eski buxgalteriya serveri bulutdagi VM ga |
| **Replatform** | kichik moslashtirish bilan | oʻz MBBT oʻrniga boshqariladigan bulut MBBT |
| **Refactor** | bulut uchun qayta yozish | ilovani mikroservislar va konteynerlarga |
| **Repurchase** | SaaS ga almashtirish | oʻz pochta serveri oʻrniga Microsoft 365 yoki Google Workspace |
| **Retain** | hozircha oʻz joyida qoldirish | qonun talab qiladigan maxfiy tizimlar |
| **Retire** | keraksizini oʻchirish | hech kim ishlatmaydigan eski ilovalar |

## Sohalar boʻyicha qoʻllanish

| Soha | Bulut yechimlari | Natija |
|---|---|---|
| **Taʼlim** | LMS platformalar, videodarslar, onlayn ofis, imtihon tizimlari | masofaviy taʼlim, sessiya davridagi yuklamaga moslashish |
| **Sogʻliqni saqlash** | elektron tibbiy kartalar, telemeditsina, tasvirlar arxivi | maʼlumotlarning istalgan klinikada mavjudligi; maxfiylik — qatʼiy talab |
| **Bank va moliya** | mobil banking backend, skoring va tahlil, firibgarlikni aniqlash | masshtablash; regulyator talablari tufayli koʻpincha xususiy yoki gibrid bulut |
| **Savdo** | internet-doʻkon, marketpleys integratsiyasi, onlayn kassa, CRM | mavsumiy choʻqqilarda ("qora juma") resursni tez oshirish |
| **Davlat boshqaruvi** | davlat xizmatlari portallari, idoralararo platformalar, hukumat bulutlari | xarajatni markazlashtirish, tez joriy etish |
| **Kichik biznes** | SaaS buxgalteriya, CRM, pochta, sayt konstruktorlari | boshlangʻich sarmoyasiz professional vositalar |

## Huquqiy talablar va xavfsizlik

- **Shaxsiy maʼlumotlarni lokallashtirish**: Oʻzbekiston fuqarolarining shaxsiy maʼlumotlari mamlakat hududidagi serverlarda saqlanishi kerak ("Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonun) — xorijiy bulutdan foydalanishda tekshiring.
- **Sohaviy talablar**: banklar uchun Markaziy bank talablari, tibbiyot maʼlumotlari uchun maxsus himoya.
- **Umumiy javobgarlik**: mijoz kirish huquqlari, shifrlash, zaxira nusxa va sozlamalar uchun javob beradi (M11, MT21).
- **Shartnoma (SLA)**: ishlash vaqti, maʼlumotlarni qaytarish, provayderdan chiqish tartibi.

## Xarajatlarni boshqarish (FinOps)

Bulutda xarajat foydalanishga bogʻliq, shuning uchun uni doimiy boshqarish kerak: byudjet va ogohlantirishlar, ishlatilmayotgan resurslarni oʻchirish, toʻgʻri oʻlchamdagi serverlar, uzoq muddatli band qilish chegirmalari, xarajatlarni boʻlimlar boʻyicha teglash. Bu amaliyot **FinOps** deb ataladi — moliya va IT birgalikda bulut xarajatini boshqaradi.

## Keys: "Ilm" xususiy universiteti

Universitetda 3 000 talaba. Hozir: oʻz serverida eski talabalar hisobi tizimi, alohida pochta serveri, darslar faqat auditoriyada, sessiya davrida sayt "osilib qoladi". Rahbariyat bulutga oʻtishni oʻylamoqda.

**Guruh topshiriqlari**:

1. Har bir tizim uchun "6R" strategiyasini tanlang va asoslang (talabalar hisobi, pochta, sayt, yangi LMS).
2. SaaS, PaaS va IaaS dan qaysi biri qaysi tizimga mos?
3. Shaxsiy maʼlumotlar (talabalar, baholar, pasport maʼlumotlari) uchun qanday talablar va choralar kerak?
4. Bosqichma-bosqich reja tuzing: pilot, muddatlar, oʻqitish, xavflar.
5. Xarajatlarni qanday kuzatish va kamaytirish mumkin?

:::warning
**Koʻp uchraydigan xatolar**

- **Hammasini birdaniga koʻchirish.** Pilot loyihasiz katta xavf.
- **Eski tizimni oʻzgarishsiz koʻchirib, bulut afzalliklarini kutish.** "Rehost" tez, lekin har doim arzon emas.
- **Lokallashtirish talabini eʼtiborsiz qoldirish.** Qonun buzilishi va jarimalar.
- **Chiqish rejasini tuzmaslik.** Provayderni almashtirish kerak boʻlsa, maʼlumotlarni qanday olish mumkin?
:::

:::tip
Oʻzingiz bilgan bitta tashkilot (doʻkon, oʻquv markazi, klinika) uchun qaysi tizimlarni bulutga koʻchirish mumkinligini va qaysi strategiya mos kelishini jadvalda koʻrsating.
:::

## Xulosa

- Bulutga oʻtish: baholash → rejalashtirish → koʻchirish (pilot bilan) → optimallashtirish.
- "6R": rehost, replatform, refactor, repurchase, retain, retire.
- Sohalar: taʼlim, sogʻliqni saqlash, bank, savdo, davlat, kichik biznes — har birida oʻz talablari bor.
- Huquqiy talablar (lokallashtirish, sohaviy normativlar), umumiy javobgarlik va SLA hisobga olinadi.
- Xarajatlar FinOps amaliyoti bilan doimiy boshqariladi.

## Oʻzingizni tekshiring

:::check
Q: "Repurchase" strategiyasi nimani bildiradi?
A: Oʻz tizimini tayyor SaaS mahsulotiga almashtirishni, masalan oʻz pochta serveri oʻrniga Microsoft 365.
Q: Nima uchun banklar koʻpincha xususiy yoki gibrid bulutni tanlaydi?
A: Regulyator talablari va maxfiy maʼlumotlarni himoya qilish zarurati tufayli.
Q: Shaxsiy maʼlumotlarni lokallashtirish talabi nimani anglatadi?
A: Oʻzbekiston fuqarolarining shaxsiy maʼlumotlari mamlakat hududidagi serverlarda saqlanishi kerak.
Q: FinOps nima?
A: Moliya va IT ning birgalikda bulut xarajatlarini kuzatish va optimallashtirish amaliyoti.
:::

:::link https://aws.amazon.com/cloud-migration/
AWS: Cloud Migration
Bulutga koʻchirish bosqichlari va strategiyalari boʻyicha materiallar (ingliz tilida).
:::
