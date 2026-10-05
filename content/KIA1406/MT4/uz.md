---
title: Axborotni foydalanuvchanligini taʼminlashda antivirus, IDS, IPS vositalarining oʻrni. Zararli dasturiy vositalarning klassifikatsiyasi va himoya usullari
summary: Foydalanish mumkinligi (availability) xossasi va unga tahdidlar; antivirus, IDS va IPS ning foydalanish mumkinligini saqlashdagi roli va cheklovlari; himoya vositalari ishonchliligi — soxta signallar, IPS ning ishdan chiqishdagi xatti-harakati (fail-open, fail-closed); zararli dasturlar tasnifi — virus, qurt, troyan, shifrlovchi, josus dastur, reklama dasturi, rootkit, bot va botnet, kalit yozuvchi, faylsiz zararli dastur; tarqalish yoʻllari; tasnif boʻyicha himoya usullari; MITRE ATT&CK haqida tushuncha.
---

## Nima uchun bu mavzu muhim?

CIA triadasining uchinchi xossasi — **foydalanish mumkinligi** — koʻpincha eng qimmat: shifrlovchi virus tufayli kasalxona, zavod yoki bank toʻxtab qolsa, har soat katta zarar keltiradi. Antivirus, IDS va IPS foydalanish mumkinligini himoya qiladi, lekin notoʻgʻri sozlansa, oʻzlari ham uzilishlarga sabab boʻlishi mumkin. Shu bilan birga, himoyani toʻgʻri tanlash uchun zararli dasturlar qanday turlarga boʻlinishini bilish kerak.

:::note
**Bu mustaqil ishdan soʻng siz:**

- foydalanish mumkinligiga tahdidlarni va ularga qarshi vositalarni ayta olasiz;
- antivirus, IDS va IPS ning bu xossadagi rolini va xavflarini tushuntira olasiz;
- zararli dasturlarni asosiy turlar boʻyicha tasniflay olasiz;
- har bir tur uchun asosiy himoya choralarini taklif qila olasiz.
:::

## Foydalanish mumkinligi va unga tahdidlar

**Foydalanish mumkinligi** — vakolatli foydalanuvchi kerakli paytda axborot va xizmatdan foydalana olishi. Unga tahdidlar:

- zararli dasturlar (ayniqsa shifrlovchilar va maʼlumotni yoʻq qiluvchilar);
- xizmatdan voz kechish hujumlari (DoS/DDoS);
- apparat nosozliklari, elektr uzilishi, tabiiy ofatlar (M12);
- inson xatolari va notoʻgʻri sozlamalar (shu jumladan himoya vositalarining oʻzidagi).

## Himoya vositalarining roli

| Vosita | Foydalanish mumkinligini qanday himoya qiladi | Xavf yoki cheklov |
|---|---|---|
| **Antivirus / EDR** (MT9) | zararli dasturni ishga tushishidan oldin yoki erta bosqichda toʻxtatadi; shifrlovchi xatti-harakatni bloklaydi | soxta signal muhim tizim faylini karantinga olishi mumkin; notoʻgʻri yangilanish koʻp kompyuterlarni birdaniga ishdan chiqarishi mumkin |
| **IDS** (M14) | hujumni erta aniqlab, javob uchun vaqt beradi | oʻzi toʻxtatmaydi; ogohlantirishlarni kimdir koʻrishi kerak |
| **IPS** (M14) | hujum trafigini yoʻlda toʻxtatadi | soxta toʻxtatish qonuniy trafikni uzadi; qurilmaning oʻzi nosoz boʻlsa, trafik toʻxtaydi |

**Ishdan chiqishdagi xatti-harakat**: IPS yoki xavfsizlik devori nosoz boʻlganda ikki variant bor: **fail-open** (trafikni tekshirmasdan oʻtkazadi — xizmat ishlaydi, lekin himoyasiz) va **fail-closed** (trafikni toʻxtatadi — himoya saqlanadi, lekin xizmat uziladi). Tanlov risk baholashga bogʻliq: kasalxona tizimi uchun koʻpincha uzluksizlik, bank toʻlov tizimi uchun himoya ustun.

**2024-yil iyul hodisasi**: CrowdStrike EDR mahsulotining notoʻgʻri konfiguratsiya yangilanishi millionlab Windows kompyuterlarida tizimni ishdan chiqardi — aviakompaniyalar, banklar va kasalxonalar faoliyati buzildi. Saboq: himoya vositalarining yangilanishlari ham bosqichma-bosqich joriy etilishi va tiklash rejasi tayyor boʻlishi kerak.

**Ishonchlilikni oshirish**: soxta signallarni sozlash (istisnolar faqat asoslangan holda), yangilanishlarni avval sinov guruhida tekshirish, IPS ni avval faqat aniqlash rejimida ishga tushirish, ortiqchalik (ikki qurilma, klaster), monitoring va javob rejasi.

## Zararli dasturlar tasnifi

| Tur | Asosiy belgisi | Asosiy zarar |
|---|---|---|
| **Virus** | xost obyektni zararlab koʻpayadi (MT6) | fayl va tizimlarni buzish |
| **Qurt** (worm) | tarmoq orqali mustaqil tarqaladi | tez ommaviy zararlanish, tarmoqni yuklash |
| **Troyan** | foydali dastur qiyofasida, oʻzi koʻpaymaydi | yashirin kirish, maʼlumot oʻgʻirlash |
| **Shifrlovchi** (ransomware) | fayllarni shifrlab, toʻlov talab qiladi; koʻpincha maʼlumotni oldin oʻgʻirlaydi | foydalanish mumkinligi va konfidensiallik |
| **Josus dastur** (spyware), **kalit yozuvchi** (keylogger) | foydalanuvchi faoliyati va kiritgan maʼlumotlarini yigʻadi | parollar, karta maʼlumotlari |
| **Reklama dasturi** (adware) | keraksiz reklama, brauzer sozlamalarini oʻzgartirish | noqulaylik, kuzatuv |
| **Rootkit** | oʻzini va boshqa zararli dasturlarni OT dan yashiradi | uzoq muddat sezilmay qolish |
| **Bot va botnet** | masofadan boshqariladigan zararlangan qurilmalar tarmogʻi | DDoS, spam, keyingi hujumlar uchun resurs |
| **Faylsiz zararli dastur** | diskka fayl yozmasdan, tizim vositalari va xotirada ishlaydi | antivirus imzolaridan qochish |
| **Kriptomayner** | qurilma resursidan kriptovalyuta qazish uchun foydalanadi | sekinlashish, elektr sarfi |
| **Wiper** | maʼlumotni qaytarib boʻlmaydigan qilib yoʻq qiladi | foydalanish mumkinligi |

Amalda bitta namuna bir nechta tur xususiyatlarini birlashtiradi (masalan, qurt kabi tarqaladigan shifrlovchi). Tadqiqotchilar va SOC jamoalari zararli faoliyatni tasvirlash uchun **MITRE ATT&CK** bazasidan foydalanadi — u hujumchilarning taktika va usullarini bosqichlar boʻyicha tavsiflaydi va himoya choralarini ularga bogʻlash imkonini beradi.

## Tarqalish yoʻllari va himoya

| Tarqalish yoʻli | Himoya |
|---|---|
| fishing xatlari va zararli ilovalar | pochta filtri, xabardorlik treninglari, makroslarni cheklash |
| zararlangan yoki soxta saytlar | brauzer va OT yangilanishlari, veb-filtr, DNS filtri |
| yangilanmagan xizmatlar va zaifliklar | yangilanishlarni boshqarish, Internetga ochiq xizmatlarni kamaytirish, segmentlash |
| olinadigan tashuvchilar | USB nazorati, avtoishga tushirishni oʻchirish (A12, Stuxnet keysi) |
| norasmiy va "buzilgan" dasturlar | faqat rasmiy manbalar, ilovalarni oq roʻyxat bilan cheklash |
| oʻgʻirlangan parollar | MFA, noyob parollar (M10) |

**Tur boʻyicha asosiy choralar**: shifrlovchilarga qarshi — oflayn zaxira nusxa va tiklash rejasi (MT17); josus dasturlar va kalit yozuvchilarga qarshi — MFA va apparat kalitlar; rootkitlarga qarshi — xavfsiz yuklash (Secure Boot), shubha boʻlsa OT ni toʻliq qayta oʻrnatish; botnetlarga qarshi — chiquvchi trafikni filtrlash va IoT qurilmalarini segmentlash.

:::warning
**Koʻp uchraydigan xatolar**

- **Himoya vositasini "oʻrnatib unutish".** Soxta signallar, yangilanishlar va ogohlantirishlar muntazam koʻrib chiqilishi kerak.
- **IPS ni darhol toʻxtatish rejimida yoqish.** Avval aniqlash rejimida trafikni oʻrganing, keyin qoidalarni sozlang.
- **Shifrlovchidan keyin toʻlov qilish.** Toʻlov maʼlumot qaytishini kafolatlamaydi va jinoyatchilarni moliyalashtiradi; avval zaxira nusxa va vakolatli organlar bilan ishlang.
- **Zararlangan kompyuterni "tozalab" ishlatishda davom etish.** Rootkit yoki chuqur zararlanishda OT ni qayta oʻrnatish ishonchliroq.
- **Tasnifni aralashtirish.** Ilmiy ishda virus, qurt va troyanni aniq farqlang.
:::

:::tip
MITRE ATT&CK saytida (attack.mitre.org) "Ransomware" yoki "Data Encrypted for Impact" usulini toping va unga bogʻlangan "Mitigations" boʻlimini oʻqing — referatingiz uchun himoya choralarining tizimli roʻyxati shu yerda.
:::

## Xulosa

- Foydalanish mumkinligiga tahdidlar: zararli dasturlar, DoS, nosozliklar, inson xatolari.
- Antivirus/EDR, IDS va IPS foydalanish mumkinligini himoya qiladi, lekin soxta signallar va nosoz yangilanishlar ham uzilishga olib kelishi mumkin; fail-open va fail-closed — risk asosidagi tanlov.
- Zararli dasturlar: virus, qurt, troyan, shifrlovchi, josus dastur, reklama dasturi, rootkit, botnet, faylsiz, kriptomayner, wiper.
- Himoya tarqalish yoʻllariga qarab: pochta filtri, yangilanishlar, USB nazorati, rasmiy manbalar, MFA, zaxira nusxa.
- MITRE ATT&CK — hujum usullari va himoya choralarini tizimlashtiruvchi baza.

## Oʻzingizni tekshiring

:::check
Q: IPS ning fail-open va fail-closed rejimlari qanday farq qiladi?
A: Fail-open nosozlikda trafikni tekshirmasdan oʻtkazadi (xizmat ishlaydi), fail-closed esa toʻxtatadi (himoya saqlanadi, xizmat uziladi).
Q: Qurt virusdan qanday farq qiladi?
A: Qurt tarmoq orqali mustaqil tarqaladi; virusga xost obyekt kerak.
Q: Shifrlovchi viruslarga qarshi eng muhim tiklash chorasi nima?
A: Oflayn yoki oʻzgartirib boʻlmaydigan zaxira nusxa va sinalgan tiklash rejasi.
Q: Rootkitning asosiy xususiyati nima?
A: U oʻzini va boshqa zararli dasturlarni operatsion tizim va himoya vositalaridan yashiradi.
:::

:::link https://attack.mitre.org/
MITRE ATT&CK
Hujumchilar taktikalari, usullari va ularga qarshi choralar bilimlar bazasi (ingliz tilida).
:::
