---
title: Bulutli texnologiyalar va ular bilan ishlash
summary: Bulutli hisoblash tushunchasi va NIST boʻyicha beshta asosiy xususiyati; xizmat modellari — IaaS, PaaS, SaaS (va FaaS); joylashtirish modellari — ommaviy, xususiy, gibrid, jamoaviy bulut; yirik provayderlar (AWS, Microsoft Azure, Google Cloud) va mahalliy maʼlumotlar markazlari; iqtisodiy jihatlari — CAPEX dan OPEX ga oʻtish, "foydalanganingizcha toʻlash", masshtablash; umumiy javobgarlik modeli; afzalliklar va xavflar (maʼlumotlar joylashuvi, provayderga bogʻliqlik, Internetga bogʻliqlik); bulutli ofis xizmatlari bilan amaliy ishlash.
---

## Nima uchun bu mavzu muhim?

Bugun kichik doʻkon ham, yirik bank ham bulut xizmatlaridan foydalanadi: pochta va hujjatlar (Microsoft 365, Google Workspace), buxgalteriya va CRM (SaaS), saytlar va ilovalar uchun serverlar. Bulut korxonaga oʻz serverlarini sotib olmasdan, kerak paytda resurs olish va faqat ishlatilgani uchun toʻlash imkonini beradi. Iqtisodchi uchun asosiy savol — bulut qachon foydali va qanday xavflari bor.

:::note
**Bu darsdan soʻng siz:**

- bulutli hisoblash tushunchasi va asosiy xususiyatlarini tushuntira olasiz;
- IaaS, PaaS va SaaS modellarini farqlay olasiz;
- ommaviy, xususiy va gibrid bulutni solishtira olasiz;
- bulutning iqtisodiy afzalliklari va xavflarini baholay olasiz;
- umumiy javobgarlik modelini tushuntira olasiz.
:::

## Bulutli hisoblash tushunchasi

**Bulutli hisoblash** — hisoblash resurslaridan (serverlar, xotira, dasturlar) tarmoq orqali talab boʻyicha, tez va oson foydalanish modeli. NIST boʻyicha beshta asosiy xususiyat:

| Xususiyat | Maʼnosi |
|---|---|
| **Talab boʻyicha oʻz-oʻziga xizmat** | resursni operator yordamisiz, oʻzi buyurtma qiladi |
| **Keng tarmoq kirishi** | istalgan qurilmadan Internet orqali |
| **Resurslar puli** | provayder resurslari koʻp mijozlar orasida taqsimlanadi |
| **Tez moslashuvchanlik** (elasticity) | yuklama oshsa — kengayadi, kamaysa — qisqaradi |
| **Oʻlchanadigan xizmat** | foydalanish hisoblanadi va shunga qarab toʻlanadi |

## Xizmat modellari

| Model | Provayder nimani beradi | Mijoz nimani boshqaradi | Misol |
|---|---|---|---|
| **IaaS** (infratuzilma) | virtual serverlar, xotira, tarmoq | OT, dasturlar, maʼlumotlar | AWS EC2, Azure Virtual Machines |
| **PaaS** (platforma) | OT, maʼlumotlar bazasi, ishga tushirish muhiti | oʻz ilovasi va maʼlumotlari | Google App Engine, Azure App Service, boshqariladigan MBBT |
| **SaaS** (dastur) | tayyor dastur brauzer yoki ilova orqali | faqat maʼlumot va sozlamalar | Microsoft 365, Google Workspace, Salesforce, onlayn buxgalteriya |
| **FaaS** (funksiya) | kod faqat hodisa kelganda ishga tushadi | kod funksiyalari | AWS Lambda |

Oddiy oʻxshatish — "pitsa xizmat sifatida": oʻzing pishirasan (oʻz serveri), tayyor xamir va pech ijarasi (IaaS), buyurtma qilib uyda yeysan (PaaS), restoranda yeysan (SaaS).

## Joylashtirish modellari

| Model | Tavsif | Kim uchun |
|---|---|---|
| **Ommaviy bulut** | provayder infratuzilmasi koʻp mijozlarga | startaplar, kichik va oʻrta biznes |
| **Xususiy bulut** | faqat bitta tashkilot uchun (oʻzida yoki provayderda) | banklar, davlat organlari, maxfiy maʼlumotlar |
| **Gibrid bulut** | ommaviy va xususiy birgalikda | maxfiy maʼlumot — xususiyda, yuklama choʻqqisi — ommaviyda |
| **Jamoaviy bulut** | umumiy talablarga ega tashkilotlar guruhi uchun | davlat idoralari, tibbiyot muassasalari |

**Provayderlar**: global — Amazon Web Services, Microsoft Azure, Google Cloud; mahalliy — Oʻzbekistondagi maʼlumotlar markazlari va telekom operatorlarining bulut xizmatlari. "Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonunga koʻra Oʻzbekiston fuqarolarining shaxsiy maʼlumotlari mamlakat hududidagi serverlarda saqlanishi talab qilinadi — bu provayder tanlashga taʼsir qiladi.

## Iqtisodiy jihatlar

| Afzallik | Izoh |
|---|---|
| **CAPEX → OPEX** | katta boshlangʻich sarmoya oʻrniga oylik toʻlov (M2) |
| **Foydalanganingizcha toʻlash** | mavsumiy yuklamada (masalan, "qora juma") faqat qoʻshimcha resurs uchun toʻlanadi |
| **Tez boshlash** | server bir necha daqiqada tayyor, oylar emas |
| **Masshtablash** | biznes oʻsganda resurs oshiriladi |
| **Mutaxassislar xarajati kamayadi** | apparat va maʼlumotlar markazini provayder boshqaradi |

| Xavf | Izoh |
|---|---|
| **Uzoq muddatda qimmatlashish** | doimiy katta yuklamada oʻz serveri arzonroq boʻlishi mumkin (M2 dagi TCO misoli) |
| **Provayderga bogʻliqlik** (vendor lock-in) | boshqa provayderga koʻchish qiyin va qimmat |
| **Internetga bogʻliqlik** | aloqa uzilsa — xizmat ham yoʻq |
| **Maʼlumotlar joylashuvi va maxfiylik** | qonun talablari, uchinchi tomon kirishi |
| **Xarajat nazorati** | unutilgan resurslar hisobni oshiradi — byudjet ogohlantirishlari kerak |

## Umumiy javobgarlik modeli

Bulutda xavfsizlik **provayder va mijoz oʻrtasida taqsimlanadi**: provayder infratuzilmani (binolar, apparat, tarmoq, virtualizatsiya) himoya qiladi, mijoz esa oʻz maʼlumotlari, foydalanuvchilar va kirish huquqlari, sozlamalarini himoya qiladi. SaaS da mijozning ulushi eng kichik, IaaS da eng katta. Koʻp maʼlumot sizishlari provayder emas, **mijozning notoʻgʻri sozlamasi** (masalan, hammaga ochiq qoldirilgan saqlash joyi) tufayli sodir boʻladi (MT21 da batafsil).

## Amaliy ish: bulutli ofis

1. Google Workspace yoki Microsoft 365 (OneDrive) da papka yarating, unda jadval va hujjat oching.
2. Hamkoringizga **faqat koʻrish** va boshqasiga **tahrirlash** huquqini bering; havola orqali ulashish va aniq odamga ulashish farqini koʻring.
3. Jadvalni birgalikda tahrirlang va **versiyalar tarixi** dan oldingi holatni tiklang.
4. Hisobingizda ikki bosqichli autentifikatsiyani yoqing.

:::warning
**Koʻp uchraydigan xatolar**

- **"Bulutda — demak xavfsiz va zaxiralangan".** Maʼlumotlar va kirish huquqlari uchun mijoz javobgar; zaxira siyosatini tekshiring.
- **"Havolasi bor har kim" rejimida maxfiy fayllarni ulashish.** Havola tarqalsa, hamma oʻqiy oladi.
- **Resurslar va xarajatlarni kuzatmaslik.** Ishlatilmayotgan serverlar ham pul oladi.
- **Chiqish strategiyasini oʻylamaslik.** Maʼlumotlarni qanday va qancha turib boshqa joyga koʻchirish mumkinligini oldindan biling.
:::

:::tip
Bulut provayderlarining narx kalkulyatorlarida (masalan, AWS Pricing Calculator) kichik veb-sayt uchun oylik xarajatni hisoblab koʻring va M2 dagi TCO usuli bilan oʻz serveri xarajatiga solishtiring.
:::

## Xulosa

- Bulutli hisoblash — tarmoq orqali talab boʻyicha resurslar: oʻz-oʻziga xizmat, keng kirish, resurslar puli, moslashuvchanlik, oʻlchanadigan xizmat.
- Xizmat modellari: IaaS, PaaS, SaaS (va FaaS); joylashtirish: ommaviy, xususiy, gibrid, jamoaviy.
- Iqtisodiy jihatdan: CAPEX → OPEX, foydalanganingizcha toʻlash, tez boshlash; xavflar — qimmatlashish, bogʻliqlik, maxfiylik.
- Umumiy javobgarlik: provayder — infratuzilma, mijoz — maʼlumotlar, kirish va sozlamalar.

## Oʻzingizni tekshiring

:::check
Q: Tayyor dasturni brauzer orqali obuna asosida ishlatish qaysi xizmat modeli?
A: SaaS.
Q: Virtual serverlar va xotirani ijaraga olish qaysi model?
A: IaaS.
Q: Xususiy bulut nima?
A: Faqat bitta tashkilot uchun ajratilgan bulut infratuzilmasi.
Q: Umumiy javobgarlik modelida mijoz nima uchun javob beradi?
A: Oʻz maʼlumotlari, foydalanuvchilar va kirish huquqlari hamda sozlamalari uchun.
:::

:::link https://csrc.nist.gov/pubs/sp/800/145/final
NIST SP 800-145: The NIST Definition of Cloud Computing
Bulutli hisoblashning rasmiy taʼrifi, xizmat va joylashtirish modellari (ingliz tilida).
:::
