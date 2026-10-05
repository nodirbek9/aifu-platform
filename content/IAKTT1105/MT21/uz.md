---
title: Bulutli texnologiyalarda axborot xavfsizligini taʼminlash
summary: Mustaqil ish — bulutdagi asosiy xavflar (notoʻgʻri sozlamalar, hisoblarning buzilishi, maʼlumot sizishi, provayderga qaramlik, maʼlumot joylashuvi); umumiy javobgarlik modeli IaaS, PaaS va SaaS da; kirish huquqlarini boshqarish (IAM, MFA, minimal imtiyoz, rollar); shifrlash — saqlashda va uzatishda, kalitlarni boshqarish (KMS, BYOK); jurnallar va monitoring; zaxira va tiklash; muvofiqlik (ISO/IEC 27001, 27017, 27018, SOC 2, lokallashtirish); provayderni tanlashda xavfsizlik tekshiruv roʻyxati; hodisaga javob rejasi.
---

## Mustaqil ishning maqsadi

M14 da axborot xavfsizligi usullarini, M11 va S11 da bulutga oʻtishni koʻrdik. Bulutda xavfsizlik oʻziga xos: serverlar provayderda, lekin maʼlumot va sozlamalar uchun javobgarlik koʻpincha mijozda qoladi. Bu mustaqil ish tashkilot bulutdan **xavfsiz foydalanishi** uchun nimalarni nazorat qilishi kerakligini koʻrsatadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- bulutdagi asosiy xavflarni ayta olasiz;
- turli xizmat modellarida kim nimaga javobgarligini aniqlay olasiz;
- kirish huquqlari, shifrlash va monitoring choralarini tushuntira olasiz;
- provayderni xavfsizlik nuqtai nazaridan baholay olasiz.
:::

## Bulutdagi asosiy xavflar

| Xavf | Mazmuni | Misol |
|---|---|---|
| **Notoʻgʻri sozlamalar** | eng keng tarqalgan sabab: xizmat ochiq qoldirilgan | mijozlar fayllari saqlangan papka "hammaga ochiq" rejimida |
| **Hisoblarning buzilishi** | parol oʻgʻirlangan yoki taxmin qilingan, MFA yoʻq | administrator hisobi orqali maʼlumot olinadi |
| **Ortiqcha huquqlar** | xodim yoki ilovada keragidan koʻp ruxsat | stajyorda butun bazani oʻchirish huquqi |
| **Maʼlumot sizishi** | maxfiy maʼlumot ruxsatsiz koʻrinadi | shifrlanmagan zaxira nusxa |
| **Provayderga qaramlik** | maʼlumotni boshqa joyga koʻchirish qiyin | xususiy formatlar, chiqish narxi yuqori |
| **Maʼlumot joylashuvi** | maʼlumot qaysi mamlakatda saqlanishi | shaxsiy maʼlumotlarni lokallashtirish talabi (S11) |
| **Xizmat uzilishi** | provayder yoki Internet ishlamay qolishi | bir mintaqadagi nosozlik |

:::tip
Koʻpgina bulut hodisalari provayderning "buzilishi" tufayli emas, balki **mijozning oʻz sozlamalari** tufayli yuz beradi. Shuning uchun "bulut xavfsiz" degani "bizning maʼlumotimiz avtomatik xavfsiz" degani emas.
:::

## Umumiy javobgarlik modeli

| Kim javob beradi? | IaaS | PaaS | SaaS |
|---|---|---|---|
| Jismoniy maʼlumotlar markazi, uskunalar | provayder | provayder | provayder |
| Virtualizatsiya, tarmoq infratuzilmasi | provayder | provayder | provayder |
| Operatsion tizim va yangilanishlari | **mijoz** | provayder | provayder |
| Ilova | **mijoz** | **mijoz** | provayder |
| Kirish huquqlari va hisoblar | **mijoz** | **mijoz** | **mijoz** |
| Maʼlumotlar va ularni tasniflash | **mijoz** | **mijoz** | **mijoz** |
| Sozlamalar (ulashish, ochiqlik) | **mijoz** | **mijoz** | **mijoz** |

Har qanday modelda **maʼlumot, kirish huquqlari va sozlamalar** mijoz zimmasida qoladi.

## Kirish huquqlarini boshqarish (IAM)

- **Yagona identifikatsiya**: har bir xodimning shaxsiy hisobi; umumiy "admin" hisobi yoʻq.
- **Koʻp faktorli autentifikatsiya** (MFA) — ayniqsa administratorlar uchun majburiy (M14).
- **Minimal imtiyoz va rollar**: "buxgalter", "tahlilchi", "administrator" rollari — huquqlar shaxsga emas, rolga beriladi.
- **Vaqtinchalik huquqlar**: kuchli huquq faqat kerakli ish uchun va cheklangan muddatga.
- **Hayot sikli**: xodim ishdan ketganda hisob darhol yopiladi; huquqlar muntazam (masalan, har chorakda) qayta koʻrib chiqiladi.
- **Ilova kalitlari** (API kalitlari) kodga yoki ochiq hujjatga yozilmaydi — maxsus maxfiy maʼlumotlar omborida saqlanadi.

## Shifrlash va kalitlarni boshqarish

| Holat | Qanday himoyalanadi |
|---|---|
| **Uzatishda** (in transit) | HTTPS/TLS, VPN (S14) |
| **Saqlashda** (at rest) | disklar, maʼlumotlar bazalari va zaxira nusxalarni shifrlash |
| **Ishlov berishda** (in use) | maxsus himoyalangan muhitlar (ilgʻor yoʻnalish) |

**Kalitlarni boshqarish**: provayderning kalit boshqaruv xizmati (KMS) kalitlarni yaratadi, saqlaydi va almashtiradi. **BYOK** (oʻz kalitini olib kelish) — mijoz kalitni oʻzi yaratadi va nazorat qiladi: zarur boʻlsa, kalitni bekor qilib, maʼlumotni provayder uchun ham oʻqib boʻlmaydigan qilish mumkin. Kalitga kirish ham alohida jurnallanadi.

## Jurnallar va monitoring

- Barcha muhim amallar jurnallanadi: kim, qachon, qayerdan, nima qildi (kirish, sozlamani oʻzgartirish, maʼlumotni yuklab olish).
- **Ogohlantirishlar**: odatiy boʻlmagan mamlakatdan kirish, bir vaqtda koʻp yuklab olish, sozlama "ommaviy"ga oʻzgartirilishi.
- **Sozlamalarni avtomatik tekshirish**: provayderlarning xavfsizlik holati xizmatlari ochiq qolgan resurslar va zaif sozlamalarni koʻrsatadi.
- Jurnallar oʻzgartirib boʻlmaydigan joyda va talab qilingan muddat saqlanadi.

## Zaxira va tiklash

- 3-2-1 qoidasi (M14) bulutda ham amal qiladi: kamida bitta nusxa **boshqa hisob, mintaqa yoki provayderda**.
- Zaxira nusxa ham shifrlanadi va unga kirish cheklanadi.
- Tiklash muntazam sinab koʻriladi; **RTO** (qancha vaqtda tiklanish kerak) va **RPO** (qancha maʼlumot yoʻqotishga yoʻl qoʻyiladi) oldindan belgilanadi.

## Muvofiqlik va sertifikatlar

| Standart / talab | Mazmuni |
|---|---|
| **ISO/IEC 27001** | axborot xavfsizligini boshqarish tizimi |
| **ISO/IEC 27017** | bulut xizmatlari uchun xavfsizlik choralari |
| **ISO/IEC 27018** | ommaviy bulutda shaxsiy maʼlumotlarni himoya qilish |
| **SOC 2** | xizmat koʻrsatuvchi tashkilot nazoratlari boʻyicha mustaqil audit hisoboti |
| **Milliy talablar** | shaxsiy maʼlumotlarni lokallashtirish, sohaviy normativlar (bank, tibbiyot) |

Sertifikat provayderning qismini tasdiqlaydi; mijoz oʻz qismini (huquqlar, sozlamalar) oʻzi taʼminlaydi.

## Provayderni tanlashda tekshiruv roʻyxati

1. Maʼlumotlar qaysi mamlakat va mintaqada saqlanadi? Lokallashtirish talabiga mosmi?
2. Qanday sertifikatlar va mustaqil audit hisobotlari bor?
3. Shifrlash qanday: saqlashda, uzatishda; BYOK imkoniyati bormi?
4. MFA, rollar va jurnallar qoʻllab-quvvatlanadimi?
5. SLA: ishlash vaqti, hodisa haqida xabar berish muddati, kompensatsiya.
6. **Chiqish strategiyasi**: maʼlumotni qanday formatda va qancha muddatda qaytarib olish mumkin; shartnoma tugagach maʼlumot qanday oʻchiriladi?
7. Provayder xodimlari maʼlumotga qanday sharoitda kira oladi?

## Hodisaga javob rejasi

Hodisa yuz berganda tartib oldindan yozilgan boʻlishi kerak: **aniqlash** (ogohlantirish) → **cheklash** (hisobni bloklash, kalitlarni almashtirish, resursni yopish) → **tiklash** (zaxiradan) → **xabar berish** (rahbariyat, kerak boʻlsa — vakolatli organ va jabrlangan mijozlar) → **xulosa** (sabab va takrorlanmaslik choralari). Masʼul shaxslar va aloqa maʼlumotlari rejada koʻrsatiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **"Provayder hammasiga javob beradi" deb oʻylash.** Maʼlumot, huquqlar va sozlamalar — mijoz javobgarligi.
- **Administrator hisobida MFA yoʻq.** Eng xavfli zaiflik.
- **Ulashish sozlamasini "hammaga ochiq" qoldirish.** Maxfiy fayllar qidiruv tizimlariga tushishi mumkin.
- **Zaxirani faqat oʻsha bulut hisobida saqlash.** Hisob buzilsa, zaxira ham yoʻqoladi.
- **Chiqish strategiyasini oʻylamaslik.** Provayderni almashtirish kerak boʻlganda maʼlumot "garovda" qoladi.
:::

## Topshiriq

1. Oʻzingiz foydalanadigan bulut xizmatida (Google Drive, OneDrive va hokazo) quyidagilarni tekshiring: MFA yoqilganmi, qaysi fayllar "havolasi bor har kim" rejimida ulashilgan, qaysi qurilmalardan kirilgan. Natijani (shaxsiy maʼlumotlarsiz) yozing.
2. Kichik kompaniya (20 xodim, bulutli buxgalteriya va pochta) uchun 10 bandli bulut xavfsizligi siyosatini tuzing.
3. Ikki bulut provayderini yuqoridagi tekshiruv roʻyxati boʻyicha ularning rasmiy saytidagi maʼlumotlar asosida solishtiring.

## Xulosa

- Bulutdagi asosiy xavf — mijozning notoʻgʻri sozlamalari va buzilgan hisoblar.
- Umumiy javobgarlik: har qanday modelda maʼlumot, huquqlar va sozlamalar mijozda.
- Choralar: IAM (MFA, rollar, minimal imtiyoz), shifrlash va kalitlarni boshqarish, jurnallar va monitoring, zaxira (RTO, RPO).
- Provayder sertifikatlar, maʼlumot joylashuvi, SLA va chiqish strategiyasi boʻyicha tekshiriladi; hodisaga javob rejasi oldindan tayyorlanadi.

## Oʻzingizni tekshiring

:::check
Q: SaaS modelida ham mijoz nimalarga javobgar?
A: Kirish huquqlari va hisoblar, maʼlumotlar va ularni tasniflash, ulashish sozlamalari uchun.
Q: BYOK nima?
A: Mijoz shifrlash kalitini oʻzi yaratadi va nazorat qiladi, zarur boʻlsa uni bekor qila oladi.
Q: RTO va RPO nimani bildiradi?
A: RTO — xizmat qancha vaqtda tiklanishi kerakligi; RPO — qancha muddatlik maʼlumot yoʻqotilishiga yoʻl qoʻyilishi.
Q: Chiqish strategiyasi nima uchun kerak?
A: Provayderni almashtirish yoki shartnomani tugatishda maʼlumotni toʻliq va oʻz vaqtida qaytarib olish uchun.
:::

:::link https://cloudsecurityalliance.org/research/guidance
Cloud Security Alliance: Security Guidance
Bulut xavfsizligi boʻyicha xalqaro tavsiyalar (ingliz tilida).
:::
