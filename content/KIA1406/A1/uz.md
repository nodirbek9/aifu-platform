---
title: Axborot xavfsizligi tushunchalari boʻyicha test va keys-stadi
summary: M1 tushunchalarini amalda qoʻllash — CIA xossalari, aktiv, tahdid, zaiflik, risk va nazorat vositalarini real vaziyatlarda aniqlash; keys-stadi tahlili usuli; oltita qisqa keys (universitet, bank, kasalxona, onlayn doʻkon, davlat xizmati, ijtimoiy tarmoq) va ularning yechim namunalari; mustaqil tahlil uchun topshiriq; yakuniy test.
---

## Nima uchun bu mavzu muhim?

Tushunchalarni yodlash oson, lekin real hodisada "bu yerda qaysi xossa buzildi, zaiflik qayerda va qanday chora kerak edi?" degan savolga tez javob berish — koʻnikma. Xavfsizlik mutaxassisining kundalik ishi aynan shu: hodisa yoki loyiha tavsifini oʻqib, uni tushunchalar tiliga oʻgirish va qaror taklif qilish. Bu mashgʻulotda keyslar orqali shu koʻnikmani mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- real vaziyat tavsifidan aktiv, tahdid, zaiflik va oqibatni ajrata olasiz;
- har bir hodisada CIA ning qaysi xossasi buzilganini aniqlay olasiz;
- har bir keys uchun oldini oluvchi, aniqlovchi va tiklovchi choralarni taklif qila olasiz;
- keys tahlilini tuzilgan shaklda yozib bera olasiz.
:::

## Keys tahlili usuli

Har bir keysni bir xil tartibda tahlil qiling:

| Qadam | Savol |
|---|---|
| 1. Aktiv | Nima himoyalanishi kerak edi? Uning qiymati qanday? |
| 2. Tahdid | Kim yoki nima zarar keltirdi? Ichki/tashqi, qasddan/tasodifiy? |
| 3. Zaiflik | Qaysi kamchilik hodisaga yoʻl ochdi? |
| 4. Buzilgan xossa | Konfidensiallik, yaxlitlik yoki foydalanish mumkinligi (bir nechtasi boʻlishi mumkin)? |
| 5. Oqibat | Moliyaviy, huquqiy, obroʻga oid, operatsion zarar |
| 6. Choralar | Oldini oluvchi, aniqlovchi, tiklovchi; maʼmuriy, texnik, fizik |

## Keyslar va yechim namunalari

:::case
**Keys 1. Universitet.** Oʻqituvchi baholar jadvalini shaxsiy pochtasiga yubormoqchi edi, lekin manzilni adashtirib, butun guruh talabalari roʻyxatiga yubordi. Jadvalda barcha talabalarning baholari va telefon raqamlari bor edi.

**Yechim namunasi.** Aktiv — talabalar shaxsiy maʼlumotlari va baholari. Tahdid — ichki, tasodifiy (xodim xatosi). Zaiflik — maxfiy maʼlumotni pochta orqali shifrlanmagan holda yuborish amaliyoti, maʼlumotlarni tasniflash siyosati yoʻqligi. Buzilgan xossa — **konfidensiallik**. Choralar: maʼlumotlarni tasniflash va yuborish qoidalari (maʼmuriy), DLP tizimi va tashqi manzilga yuborishda ogohlantirish (texnik), xodimlarni oʻqitish.
:::

:::case
**Keys 2. Bank.** Hujumchi mijozga bank nomidan SMS yuborib, "kartangiz bloklandi" deb soxta saytga kirishga undadi. Mijoz karta maʼlumotlari va SMS kodni kiritdi, hisobidan pul yechib olindi.

**Yechim namunasi.** Aktiv — mijoz mablagʻi va karta maʼlumotlari. Tahdid — tashqi, qasddan (fishing/smishing). Zaiflik — mijozning xabardor emasligi, faqat SMS kodga tayanuvchi tasdiqlash. Buzilgan xossalar — **konfidensiallik** (karta maʼlumotlari) va **yaxlitlik** (hisob holati ruxsatsiz oʻzgardi). Choralar: mijozlarni ogohlantirish, shubhali tranzaksiyalarni aniqlash tizimi, ilovada push-tasdiqlash, soxta domenlarni bloklash.
:::

:::case
**Keys 3. Kasalxona.** Shifrlovchi virus server fayllarini shifrladi; bemorlar kartalari bilan ishlash 3 kun toʻxtadi. Zaxira nusxalar oʻsha serverning ikkinchi diskida saqlangan va ular ham shifrlangan.

**Yechim namunasi.** Aktiv — tibbiy kartalar va ularga kirish imkoniyati. Tahdid — tashqi, qasddan (ransomware). Zaifliklar — zaxira nusxaning ajratilmaganligi, ehtimol yangilanmagan tizim yoki fishing xati. Buzilgan xossa — **foydalanish mumkinligi** (shuningdek, maʼlumot sizdirilgan boʻlsa — konfidensiallik). Choralar: 3-2-1 qoidasi boʻyicha zaxira (bitta nusxa oflayn), yangilanishlar, pochta filtri, tiklash rejasi va uni sinash.
:::

:::case
**Keys 4. Onlayn doʻkon.** Narxlar bazasidagi SQL-inyeksiya zaifligi orqali hujumchi bir nechta tovar narxini 1 soʻmga oʻzgartirib, ularni sotib oldi.

**Yechim namunasi.** Aktiv — narxlar bazasi va tushum. Tahdid — tashqi, qasddan. Zaiflik — kiritilgan maʼlumotni tekshirmaydigan dastur kodi. Buzilgan xossa — **yaxlitlik**. Choralar: parametrlashtirilgan soʻrovlar, WAF, kodni xavfsizlik nuqtai nazaridan tekshirish, narx oʻzgarishlarini jurnalga yozish va keskin oʻzgarishda ogohlantirish.
:::

:::case
**Keys 5. Davlat xizmati portali.** Hisobotlarni topshirish muddatining oxirgi kunida portal DDoS hujumi tufayli 6 soat ochilmadi.

**Yechim namunasi.** Aktiv — portal xizmati. Tahdid — tashqi, qasddan. Zaiflik — DDoS himoyasining yoʻqligi, yagona maʼlumotlar markazi. Buzilgan xossa — **foydalanish mumkinligi**. Choralar: provayder darajasida DDoS filtrlash, CDN, zaxira maʼlumotlar markazi, muddatni uzaytirish tartibi (maʼmuriy kompensatsiya).
:::

:::case
**Keys 6. Ijtimoiy tarmoq.** Talaba barcha saytlarda bir xil paroldan foydalanardi. Kichik forum maʼlumotlari sizgach, hujumchi shu parol bilan uning ijtimoiy tarmoq hisobiga kirib, doʻstlariga pul soʻrab xabarlar yubordi.

**Yechim namunasi.** Aktiv — hisob va shaxsning obroʻsi. Tahdid — tashqi, qasddan (credential stuffing). Zaiflik — parolni takror ishlatish, MFA yoʻqligi. Buzilgan xossalar — **konfidensiallik** (yozishmalar), **yaxlitlik** (uning nomidan xabarlar), **autentiklik**. Choralar: har bir sayt uchun alohida parol (parol menejeri), MFA, kirish haqida bildirishnomalar.
:::

## Mustaqil tahlil uchun topshiriq

Quyidagi uchta keysni jadval shaklida (6 qadam) tahlil qiling:

1. Xodim ishdan boʻshagach, uning hisobi 2 oy oʻchirilmadi; u uydan kirib, mijozlar bazasini nusxalab oldi.
2. Server xonasidagi konditsioner buzilib, serverlar qizib ketdi va oʻchdi; sayt 10 soat ishlamadi.
3. Oʻqituvchi noutbukini avtobusda unutdi; diskda shifrlanmagan imtihon savollari bor edi.

:::warning
**Koʻp uchraydigan xatolar**

- **Har bir hodisada faqat bitta xossani koʻrish.** Koʻpincha bir vaqtda bir nechta xossa buziladi (fishing — konfidensiallik va yaxlitlik).
- **Zaiflik oʻrniga tahdidni yozish.** "Xaker" — tahdid; "yangilanmagan server" — zaiflik.
- **Faqat texnik choralar taklif qilish.** Koʻp keyslarda asosiy chora — maʼmuriy (qoida, oʻqitish, jarayon).
- **Faqat oldini oluvchi choralarni oʻylash.** Aniqlash va tiklash rejasi ham kerak: 100% oldini olish imkonsiz.
:::

:::tip
Yangiliklardan soʻnggi oyda sodir boʻlgan bitta haqiqiy kiberhodisani toping va uni 6 qadamli jadval bilan tahlil qiling. Manbani koʻrsating va qaysi chora hodisani eng arzon yoʻl bilan oldini olishi mumkin edi — shuni asoslang.
:::

## Xulosa

- Keys tahlili: aktiv → tahdid → zaiflik → buzilgan xossa → oqibat → choralar.
- Har bir hodisada bir nechta CIA xossasi buzilishi mumkin; qoʻshimcha xossalarni (autentiklik, rad etib boʻlmaslik) ham hisobga oling.
- Choralar uch turdan (maʼmuriy, texnik, fizik) va uch vazifadan (oldini olish, aniqlash, tiklash) muvozanatli tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: Keys 3 da qaysi xossa birinchi navbatda buzilgan va eng muhim tiklovchi chora qaysi?
A: Foydalanish mumkinligi; ajratilgan (oflayn) zaxira nusxa va sinalgan tiklash rejasi.
Q: Keys 4 da zaiflik nima?
A: Foydalanuvchi kiritgan maʼlumotni tekshirmaydigan dastur kodi (SQL-inyeksiya).
Q: Parolni takror ishlatishdan foydalanadigan hujum qanday ataladi?
A: Credential stuffing — sizib chiqqan login va parollarni boshqa saytlarda sinash.
Q: Ishdan boʻshagan xodim hisobining oʻchirilmasligi qaysi siyosat buzilishi?
A: Kirishni boshqarish siyosati (huquqlarni oʻz vaqtida olib qoʻyish).
Q: Noutbuk yoʻqolganda maʼlumotni qaysi chora himoyalaydi?
A: Diskni toʻliq shifrlash (masalan, BitLocker).
:::

:::link https://www.cisa.gov/topics/cybersecurity-best-practices
CISA: Cybersecurity Best Practices
AQSH kiberxavfsizlik agentligining amaliy tavsiyalari (ingliz tilida).
:::
