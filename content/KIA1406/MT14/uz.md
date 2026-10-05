---
title: Kiberxavfsizlik sohasida mashhur sertifikatlar. Parollarni boshqarish tizimlari (masalan, LastPass) va ulardan foydalanish
summary: Professional sertifikatlar va ularning vazifasi; boshlangʻich (CompTIA Security+, ISC2 CC, Cisco CyberOps), oʻrta va ixtisoslashgan (CySA+, CEH, OSCP, GIAC, bulut sertifikatlari) va yuqori darajadagi (CISSP, CISM, CISA) sertifikatlar; tayyorlanish yoʻli; parol menejerlari — ishlash tamoyili, nol bilim arxitekturasi, asosiy parol, avtotoʻldirish va fishingdan himoya; LastPass 2022 hodisasi va saboqlari; Bitwarden, KeePassXC, brauzer menejerlari; parol menejerini amalda sozlash.
---

## Nima uchun bu mavzu muhim?

Ikki mavzu bir-biriga bogʻliq: sertifikatlar — mutaxassisning bilimini ish beruvchi tushunadigan "tilda" tasdiqlash usuli (MT13 dagi karyera yoʻlining bir qismi), parol menejerlari esa M10 dagi eng muhim amaliy tavsiyalardan biri — har bir sayt uchun alohida, uzun, tasodifiy parol. Har ikkisida ham asosiy savol bir xil: **ishonch nimaga asoslanadi?** Sertifikat — bilim tekshirilganiga, parol menejeri — uning kriptografiyasi va ishlab chiqaruvchining xavfsizlik amaliyotiga.

:::note
**Bu mustaqil ishdan soʻng siz:**

- asosiy kiberxavfsizlik sertifikatlarini va ularning darajasini bilasiz;
- oʻz yoʻnalishingiz uchun mos sertifikatni tanlay olasiz;
- parol menejeri qanday ishlashini va nima uchun xavfsiz ekanini tushuntira olasiz;
- LastPass hodisasidan saboqlarni ayta olasiz;
- parol menejerini xavfsiz sozlay olasiz.
:::

## Professional sertifikatlar

Sertifikat — mustaqil tashkilot imtihoni orqali bilimni tasdiqlovchi hujjat. U tajribani almashtirmaydi, lekin rezyumeni saralashda va xalqaro bozorda yordam beradi; baʼzi lavozimlar va davlat shartnomalarida majburiy.

| Daraja | Sertifikat | Kimdan | Kim uchun |
|---|---|---|---|
| Boshlangʻich | **CompTIA Security+** | CompTIA | xavfsizlik asoslari, eng keng tan olingan boshlangʻich sertifikat |
| Boshlangʻich | **ISC2 CC** (Certified in Cybersecurity) | ISC2 | sohaga kirish, bepul oʻquv dasturlari bilan |
| Boshlangʻich | **Cisco CyberOps Associate** | Cisco | SOC va monitoring asoslari |
| Oʻrta | **CompTIA CySA+** | CompTIA | xavfsizlik tahlilchisi, SOC |
| Oʻrta | **CEH** | EC-Council | ruxsat etilgan sinov asoslari (nazariy) |
| Ixtisoslashgan | **OSCP** | OffSec | amaliy pentest (24 soatlik amaliy imtihon) |
| Ixtisoslashgan | **GIAC** (GSEC, GCIH va boshq.) | SANS/GIAC | turli texnik yoʻnalishlar |
| Bulut | **AWS Security Specialty**, **Microsoft SC-200/SC-300**, **CCSP** | AWS, Microsoft, ISC2 | bulut xavfsizligi |
| Yuqori | **CISSP** | ISC2 | tajribali mutaxassislar va rahbarlar (5 yil tajriba talab qilinadi) |
| Yuqori | **CISM**, **CISA** | ISACA | xavfsizlikni boshqarish; axborot tizimlari auditi |
| Standart | **ISO/IEC 27001 Lead Implementer / Lead Auditor** | turli akkreditatsiyalangan organlar | AXBT (ISMS) joriy etish va audit |

**Tanlash tamoyillari**: avval yoʻnalishingizni aniqlang (MT13); boshlangʻich sertifikat bilan boshlang; mahalliy va xalqaro vakansiyalarda qaysilari talab qilinishini tekshiring; narx va qayta tasdiqlash shartlarini (uzluksiz taʼlim ballari, yillik toʻlov) hisobga oling. Rasmiy imtihon markazlari va tashkilotlarning saytlaridan foydalaning — "imtihon savollari"ni sotuvchi saytlar qoidalarni buzadi va sertifikatni bekor qilishga olib kelishi mumkin.

**Tayyorlanish**: rasmiy imtihon mavzulari roʻyxati (exam objectives) asosida reja; amaliy laboratoriyalar; sinov testlari; 2–3 oy muntazam mashgʻulot.

## Parol menejerlari

**Parol menejeri** — barcha parollaringizni shifrlangan "seyf" (vault) da saqlovchi dastur. Siz faqat bitta kuchli **asosiy parol** (master password) ni eslab qolasiz.

**Ishlash tamoyili** (nol bilim arxitekturasi, zero-knowledge):

1. Asosiy paroldan qurilmaning oʻzida sekin kalit hosil qilish funksiyasi (PBKDF2 yoki Argon2, M8) bilan shifrlash kaliti olinadi.
2. Seyf shu kalit bilan (odatda AES-256) qurilmada shifrlanadi.
3. Serverga faqat **shifrlangan** seyf yuboriladi — xizmat koʻrsatuvchi asosiy parolni ham, parollaringizni ham bilmaydi.

**Afzalliklari**: har bir sayt uchun 20+ belgili tasodifiy parol; parollarni qayta ishlatmaslik (credential stuffing ga qarshi, M10); **avtotoʻldirish fishingdan himoya qiladi** — menejer soxta domenda parolni taklif qilmaydi; sizib chiqqan parollar haqida ogohlantirish.

**Xavflar**: barcha parollar bitta joyda — asosiy parol zaif boʻlsa yoki qurilma zararlangan boʻlsa, hammasi xavf ostida. Shuning uchun: uzun asosiy parol yoki ibora, hisobga MFA, qurilmani himoyalash.

| Menejer | Xususiyati |
|---|---|
| **Bitwarden** | ochiq kodli, mustaqil auditlardan oʻtgan, bepul tarifi bor, oʻz serveringizda ham ishlatish mumkin |
| **KeePassXC** | ochiq kodli, oflayn: seyf fayli faqat sizning qurilmangizda |
| **1Password** | tijoriy; asosiy paroldan tashqari "maxfiy kalit" (Secret Key) ham ishlatadi |
| **Brauzer menejerlari** (Chrome, Edge, Firefox, iCloud Keychain) | qulay, sinxronlanadi; qurilma va hisob himoyasiga bogʻliq |
| **LastPass** | mashhur tijoriy menejer; 2022-yilgi hodisa quyida |

## LastPass hodisasi (2022)

2022-yil avgustida hujumchilar LastPass dasturchisining ish hisobiga kirib, manba kodini oʻgʻirlagan. Bu maʼlumotlardan foydalanib, keyinroq ular kompaniyaning muhim xodimlaridan birining **uy kompyuteriga** (eskirgan dastur orqali) kirib, bulutdagi zaxira saqlash kalitlarini olgan va foydalanuvchilar seyflarining zaxira nusxalarini oʻgʻirlagan. Seyflar shifrlangan edi, lekin:

- seyflardagi baʼzi maydonlar (masalan, sayt manzillari) shifrlanmagan — hujumchilar kim qaysi saytlardan foydalanishini koʻrgan;
- eski hisoblarda PBKDF2 takrorlash soni past boʻlgan (baʼzilarida bir necha ming) — zaif asosiy parollarni oflayn tanlash osonlashgan;
- keyingi yillarda tadqiqotchilar va jurnalistlar bu hodisani oʻgʻirlangan seyflardan kriptovalyuta oʻgʻirlanishi bilan bogʻlashgan.

**Saboqlar**: (1) nol bilim arxitekturasi ishladi — kuchli asosiy parolli seyflar ochilmagan; (2) asosiy parolning kuchi hal qiluvchi; (3) sekin xeshlash parametrlari yangilanib turishi kerak; (4) metamaʼlumotlar ham shifrlanishi kerak; (5) xodimlarning uy qurilmalari ham tashkilot perimetrining bir qismi; (6) sizish haqida xabar olganda — muhim parollarni (ayniqsa email, bank) almashtirish.

## Amaliy ish: parol menejerini sozlash

1. Ochiq kodli menejerni tanlang (Bitwarden yoki KeePassXC) va rasmiy saytdan oʻrnating.
2. Kamida 4–5 tasodifiy soʻzdan iborat **asosiy ibora** yarating; uni hech qayerda raqamli saqlamang, faqat qogʻozda xavfsiz joyda (zaxira sifatida).
3. Hisobga **MFA** yoqing (autentifikator ilovasi yoki apparat kalit).
4. Generator sozlamalari: uzunlik 20+, barcha belgi turlari.
5. Eng muhim 3 hisobning (email, bank, davlat xizmatlari) parolini menejer yaratgan noyob parolga almashtiring.
6. Favqulodda kirish yoki tiklash kodlarini xavfsiz joyda saqlang.
7. Hisobotingizda faqat sozlamalar skrinshotlarini koʻrsating — parollar va seyf mazmunini **emas**.

:::warning
**Koʻp uchraydigan xatolar**

- **Asosiy parolni boshqa joyda ham ishlatish.** U faqat parol menejeri uchun boʻlishi kerak.
- **Parol menejeriga MFA yoqmaslik.** Seyfga kirish — barcha hisoblarga kirish.
- **Norasmiy manbadan oʻrnatish.** Soxta "parol menejeri" — parollarni oʻgʻirlashning eng toʻgʻri yoʻli.
- **Avtotoʻldirish ishlamaganda parolni qoʻlda koʻchirib kiritish.** Bu soxta saytning belgisi boʻlishi mumkin — avval domenni tekshiring.
- **Sertifikatni bilim bilan tenglashtirish.** Sertifikat — tasdiq, lekin amaliy tajriba alohida talab qilinadi.
:::

:::tip
Menejerdagi "xavfsizlik hisoboti" (password health) funksiyasidan foydalaning: u qayta ishlatilgan, zaif va sizib chiqqan parollarni koʻrsatadi — ularni birma-bir almashtirish rejasini tuzing.
:::

## Xulosa

- Sertifikatlar bilimni tasdiqlaydi: boshlangʻich (Security+, ISC2 CC), oʻrta va ixtisoslashgan (CySA+, OSCP, GIAC, bulut), yuqori (CISSP, CISM, CISA).
- Tanlov yoʻnalish va vakansiyalar talablariga qarab; tayyorlanish rasmiy mavzular va amaliyot bilan.
- Parol menejeri: asosiy paroldan qurilmada kalit, seyf shifrlangan holda saqlanadi (nol bilim).
- Afzalliklar: noyob uzun parollar, fishingdan himoya; xavf — bitta nuqta, shuning uchun kuchli asosiy parol va MFA.
- LastPass 2022: shifrlash ishladi, lekin zaif asosiy parollar, past takrorlash soni va shifrlanmagan metamaʼlumotlar xavf tugʻdirdi.

## Oʻzingizni tekshiring

:::check
Q: Nol bilim arxitekturasi nimani anglatadi?
A: Seyf qurilmada asosiy paroldan olingan kalit bilan shifrlanadi; xizmat koʻrsatuvchi asosiy parolni ham, parollarni ham bilmaydi.
Q: Parol menejerining avtotoʻldirishi fishingdan qanday himoya qiladi?
A: U parolni faqat saqlangan domenga taklif qiladi; soxta domenda parol avtomatik toʻldirilmaydi.
Q: LastPass hodisasida nima uchun zaif asosiy parolli foydalanuvchilar koʻproq xavf ostida qoldi?
A: Shifrlangan seyflar oʻgʻirlangan; zaif parollarni (ayniqsa past PBKDF2 takrorlash sonida) oflayn tanlab topish mumkin.
Q: Kiberxavfsizlikka endi kirayotgan mutaxassis uchun qaysi sertifikatlar mos?
A: Boshlangʻich sertifikatlar: CompTIA Security+, ISC2 CC yoki Cisco CyberOps Associate.
:::

:::link https://bitwarden.com/help/bitwarden-security-white-paper/
Bitwarden Security Whitepaper
Ochiq kodli parol menejerining kriptografik arxitekturasi tavsifi (ingliz tilida).
:::
