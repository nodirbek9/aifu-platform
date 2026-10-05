---
title: Kiberxavfsizlik tushunchalari va asosiy tamoyillari. Axborot xavfsizligi, konfidensiallik, yaxlitlik, foydalanish mumkinligi. Tahdid, zaiflik, risk va boshqaruv vositalari
summary: Kiberxavfsizlik va axborot xavfsizligi tushunchalari; CIA triadasi — konfidensiallik, yaxlitlik, foydalanish mumkinligi; qoʻshimcha xossalar — autentiklik, rad etib boʻlmaslik, hisobdorlik; aktiv, tahdid, zaiflik, ekspluatatsiya, risk; boshqaruv vositalari turlari (maʼmuriy, texnik, fizik) va vazifalari (oldini olish, aniqlash, tiklash); chuqur himoya.
---

## Nima uchun bu mavzu muhim?

Bugun universitetning baholar bazasi, bankdagi pul, kasalxonadagi tibbiy karta va hatto suv taʼminoti tizimi — barchasi kompyuter tizimlarida. Bitta muvaffaqiyatli hujum talabalar shaxsiy maʼlumotlarining sizib chiqishiga, xizmatning kunlab toʻxtashiga yoki millionlab soʻm zararga olib keladi. Kiberxavfsizlik — faqat "xakerlarga qarshi kurash" emas, balki qanday maʼlumot qimmatli ekanini, unga nima xavf solishini va himoyaga qancha resurs sarflash oqilona ekanini tushunish. Ushbu birinchi maʼruzada fanning asosiy tushunchalari — butun kurs davomida ishlatiladigan "lugʻat" bilan tanishamiz.

:::note
**Bu darsdan soʻng siz:**

- kiberxavfsizlik va axborot xavfsizligi tushunchalarini farqlay olasiz;
- CIA triadasini va har bir xossaning buzilishiga misollar keltira olasiz;
- aktiv, tahdid, zaiflik, ekspluatatsiya va risk tushunchalarini bir-biridan ajrata olasiz;
- himoya vositalarini turi va vazifasi boʻyicha tasniflay olasiz;
- chuqur himoya tamoyilini tushuntira olasiz.
:::

## Axborot xavfsizligi va kiberxavfsizlik

**Axborot xavfsizligi** — axborotni har qanday shaklda (qogʻozda, kompyuterda, odamlar xotirasida) ruxsatsiz kirish, oʻzgartirish, yoʻq qilish va oshkor qilishdan himoyalash. **Kiberxavfsizlik** — uning raqamli qismi: kompyuterlar, tarmoqlar, dasturlar va maʼlumotlarni kiberhujumlardan himoyalash.

| | Axborot xavfsizligi | Kiberxavfsizlik |
|---|---|---|
| Obyekt | har qanday shakldagi axborot | raqamli tizimlar va maʼlumotlar |
| Misol | maxfiy hujjatni seyfda saqlash | serverni brandmauer bilan himoyalash |
| Tahdidlar | oʻgʻirlik, yongʻin, xodimning xatosi | zararli dastur, fishing, DDoS |

## CIA triadasi

Axborot xavfsizligining uchta asosiy xossasi — **CIA** (Confidentiality, Integrity, Availability):

| Xossa | Maʼnosi | Buzilishiga misol | Himoya usullari |
|---|---|---|---|
| **Konfidensiallik** | maʼlumot faqat vakolatli shaxslarga ochiq | talabalar baholari bazasi sizib chiqdi | shifrlash, kirish huquqlari, autentifikatsiya |
| **Yaxlitlik** | maʼlumot ruxsatsiz va sezilmasdan oʻzgartirilmaydi | bank hisobidagi summa oʻzgartirildi | xesh, raqamli imzo, oʻzgarishlar jurnali |
| **Foydalanish mumkinligi** | vakolatli foydalanuvchi kerak paytda maʼlumot va xizmatdan foydalana oladi | DDoS hujumi saytni ishdan chiqardi | zaxira nusxalar, zaxira serverlar, DDoS himoyasi |

Xossalar baʼzan bir-biriga zid: haddan tashqari qatʼiy himoya (masalan, har 5 daqiqada parol) konfidensiallikni oshiradi, lekin foydalanish mumkinligini kamaytiradi. Yaxshi xavfsizlik — muvozanat.

Qoʻshimcha xossalar:

- **Autentiklik** — maʼlumot yoki foydalanuvchi haqiqatan oʻzi aytgan manbadan.
- **Rad etib boʻlmaslik** (non-repudiation) — yuboruvchi xabarni yubormaganini inkor eta olmaydi (elektron raqamli imzo).
- **Hisobdorlik** (accountability) — har bir harakatni aniq shaxsga bogʻlash mumkin (jurnallar).

## Asosiy tushunchalar

| Tushuncha | Maʼnosi | Misol |
|---|---|---|
| **Aktiv** | tashkilot uchun qimmatli narsa | talabalar bazasi, server, obroʻ |
| **Tahdid** | aktivga zarar yetkazishi mumkin boʻlgan hodisa yoki shaxs | xaker, virus, yongʻin, xodimning xatosi |
| **Zaiflik** | tizimdagi kamchilik, undan tahdid foydalanishi mumkin | yangilanmagan dastur, oddiy parol, ochiq port |
| **Ekspluatatsiya** (exploit) | zaiflikdan foydalanish usuli yoki dasturi | maʼlum zaiflik uchun tayyor hujum kodi |
| **Risk** | tahdid zaiflikdan foydalanib zarar yetkazish ehtimoli va oqibati | "bazadagi SQL-inyeksiya orqali maʼlumot sizishi" |
| **Nazorat vositasi** | riskni kamaytiruvchi chora | brandmauer, xodimlarni oʻqitish, qulf |
| **Hodisa** (incident) | xavfsizlik buzilishining haqiqiy holati | serverga ruxsatsiz kirish aniqlandi |

Ular bir-biri bilan bogʻlangan: **tahdid** **zaiflik** orqali **aktiv**ga taʼsir qiladi va bu **risk**ni yuzaga keltiradi; **nazorat vositalari** zaiflikni yopadi yoki oqibatni kamaytiradi. Soddalashtirilgan formula: *Risk = Tahdid × Zaiflik × Taʼsir*. Zaiflik boʻlmasa yoki tahdid boʻlmasa — risk yoʻq.

Tahdid manbalari: **tashqi** (xakerlar, jinoiy guruhlar, davlat tuzilmalari, raqobatchilar) va **ichki** (norozi yoki ehtiyotsiz xodimlar); **qasddan** va **tasodifiy**; **tabiiy** (yongʻin, suv toshqini, zilzila).

## Boshqaruv vositalari

**Turi boʻyicha**:

| Turi | Mazmuni | Misollar |
|---|---|---|
| **Maʼmuriy** (administrativ) | siyosatlar, qoidalar, oʻqitish | xavfsizlik siyosati, parol qoidalari, xodimlar treningi |
| **Texnik** (mantiqiy) | dasturiy va apparat vositalar | shifrlash, brandmauer, antivirus, MFA |
| **Fizik** | jismoniy toʻsiqlar | qulflar, kirish kartalari, kameralar, qoʻriqchi |

**Vazifasi boʻyicha**:

| Vazifa | Maqsad | Misol |
|---|---|---|
| **Oldini oluvchi** (preventive) | hodisaning sodir boʻlishiga yoʻl qoʻymaslik | brandmauer, kirish huquqlari |
| **Aniqlovchi** (detective) | hodisani payqash | IDS, jurnallar, kamera |
| **Tuzatuvchi / tiklovchi** (corrective) | oqibatni bartaraf etish | zaxira nusxadan tiklash, yamoq oʻrnatish |
| **Toʻxtatuvchi** (deterrent) | hujumchini qaytarish | ogohlantirish yozuvlari, jarimalar |
| **Kompensatsiyalovchi** | asosiy chora imkonsiz boʻlganda muqobil | eski tizimni alohida tarmoqqa ajratish |

**Chuqur himoya** (defense in depth) — bitta vositaga tayanmaslik: tarmoq chegarasida brandmauer, ichkarida segmentlash, kompyuterda antivirus, ilovada autentifikatsiya, maʼlumotda shifrlash, odamlarda — xabardorlik. Bir qatlam yengilsa, keyingisi ushlab qoladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Xavfsizlikni faqat texnik masala deb hisoblash.** Hodisalarning katta qismi odam omili (fishing, oddiy parol, ehtiyotsizlik) bilan boshlanadi.
- **Tahdid va zaiflikni chalkashtirish.** Xaker — tahdid; yangilanmagan dastur — zaiflik.
- **Faqat konfidensiallikni oʻylash.** Shifrlovchi virus maʼlumotni oʻgʻirlamasdan ham foydalanish mumkinligini butunlay yoʻq qiladi.
- **"Bizga hech kim hujum qilmaydi" fikri.** Avtomatlashtirilgan hujumlar har bir Internetga ulangan tizimni nishonga oladi.
:::

:::tip
Oʻzingizning raqamli hayotingiz uchun kichik jadval tuzing: 5 ta aktiv (telefon, pochta, bank ilovasi, ijtimoiy tarmoq, universitet hisobi), ularning har biri uchun bitta tahdid, bitta zaiflik va bitta himoya chorasi. Koʻp hollarda eng kuchli chora — ikki bosqichli autentifikatsiya va yangilanishlar.
:::

## Xulosa

- Axborot xavfsizligi — axborotni har qanday shaklda himoyalash; kiberxavfsizlik — uning raqamli qismi.
- CIA: konfidensiallik, yaxlitlik, foydalanish mumkinligi; qoʻshimcha — autentiklik, rad etib boʻlmaslik, hisobdorlik.
- Aktiv, tahdid, zaiflik, ekspluatatsiya, risk, nazorat vositasi, hodisa — asosiy atamalar.
- Vositalar: maʼmuriy, texnik, fizik; oldini oluvchi, aniqlovchi, tuzatuvchi, toʻxtatuvchi, kompensatsiyalovchi.
- Chuqur himoya — bir nechta mustaqil himoya qatlami.

## Oʻzingizni tekshiring

:::check
Q: Maʼlumot faqat vakolatli shaxslarga ochiq boʻlishi CIA ning qaysi xossasi?
A: Konfidensiallik.
Q: DoS hujumi birinchi navbatda qaysi xossaga qarshi qaratilgan?
A: Foydalanish mumkinligiga.
Q: Yangilanmagan dastur tahdidmi yoki zaiflikmi?
A: Zaiflik — tahdid undan foydalanishi mumkin.
Q: Video kuzatuv kamerasi vazifasi boʻyicha qanday vosita?
A: Aniqlovchi (detective) — shuningdek, toʻxtatuvchi taʼsirga ham ega; turi boʻyicha — fizik.
Q: Rad etib boʻlmaslik qanday vosita bilan taʼminlanadi?
A: Elektron raqamli imzo bilan.
:::

:::link https://www.nist.gov/cyberframework
NIST Cybersecurity Framework
Kiberxavfsizlikni boshqarishning xalqaro tan olingan asosi (ingliz tilida).
:::
