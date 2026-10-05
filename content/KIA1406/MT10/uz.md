---
title: Tarmoqda uzatiluvchi axborotni himoyalashda kriptografiyaning oʻrni. Zararkunanda dasturlar va ulardan himoyalanish (masalan, Malwarebytes misolida)
summary: Tarmoqda kriptografiya qaysi xossalarni taʼminlaydi — konfidensiallik, yaxlitlik, autentiklik; qatlamlar boʻyicha protokollar — Wi-Fi (WPA3), IPSec, TLS, SSH, DNSSEC va shifrlangan DNS (DoH, DoT), pochta (S/MIME, PGP, MTA-STS), uchdan-uchga shifrlash (Signal protokoli); kalitlarni boshqarish va sertifikatlar; kriptografiya chegaralari — metamaʼlumotlar, oxirgi nuqtalar; Malwarebytes — imkoniyatlari, skanerlash va karantin, real vaqt himoyasi, boshqa antivirus bilan birga ishlatish; zararlangan kompyuterni tozalash tartibi.
---

## Nima uchun bu mavzu muhim?

Tarmoq orqali uzatilgan har qanday maʼlumot — parol, xabar, toʻlov — koʻplab qurilmalar (routerlar, provayderlar, Wi-Fi nuqtalar) orqali oʻtadi va ularning har biri uni koʻrishi yoki oʻzgartirishi mumkin. Kriptografiya bu yoʻlni himoyalaydi; zararli dasturlar esa maʼlumotni shifrlashdan **oldin** yoki **keyin** — qurilmaning oʻzida oʻgʻirlaydi. Shuning uchun ikkala mavzu birga oʻrganiladi: kanalni himoyalash va oxirgi nuqtani himoyalash bir-birini toʻldiradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- tarmoqda kriptografiya qaysi xossalarni taʼminlashini tushuntira olasiz;
- turli qatlamlardagi himoya protokollarini ayta olasiz;
- kriptografiyaning cheklovlarini (metamaʼlumotlar, oxirgi nuqtalar) bilasiz;
- Malwarebytes kabi vositalarning imkoniyatlari va oʻrnini tushuntira olasiz;
- zararlangan kompyuterni tozalash tartibini bajara olasiz.
:::

## Tarmoqda kriptografiya nimani taʼminlaydi

| Xossa | Mexanizm | Misol |
|---|---|---|
| **Konfidensiallik** | simmetrik shifrlash (AES, ChaCha20, M6) | trafik mazmunini tinglab boʻlmaydi |
| **Yaxlitlik** | MAC, AEAD (AES-GCM) (M8) | yoʻlda oʻzgartirilgan paket rad etiladi |
| **Autentiklik** | sertifikatlar va ERI (M8), kalit almashinuvi (M7) | siz haqiqatan bankning serveriga ulandingiz |
| **Oldinga maxfiylik** | vaqtinchalik DH kalitlari (M9) | kalit keyin oʻgʻirlansa ham eski trafik ochilmaydi |

## Qatlamlar boʻyicha himoya protokollari

| Qatlam / soha | Protokollar | Nimani himoya qiladi |
|---|---|---|
| Simsiz kanal | WPA2, WPA3 (MT7) | qurilma va kirish nuqtasi oraligʻi |
| Tarmoq | IPSec, WireGuard (M9, M14) | IP paketlar, VPN tunnellari |
| Transport / ilova | TLS (HTTPS, IMAPS, SMTPS) (M9) | veb, pochta va API ulanishlari |
| Masofaviy boshqaruv | SSH (M9) | serverga kirish va fayl uzatish |
| DNS | DNSSEC (javob haqiqiyligi), DoH / DoT (soʻrov maxfiyligi) | nomni manzilga aylantirish |
| Elektron pochta | TLS serverlar orasida, MTA-STS; S/MIME va PGP (xatning oʻzini shifrlash va imzolash) | xat mazmuni va muallifi |
| Xabar almashish | **uchdan-uchga shifrlash** (Signal protokoli — Signal, WhatsApp) | server ham xabarni oʻqiy olmaydi |

**Kalitlarni boshqarish**: kriptografiyaning kuchi kalitlar va sertifikatlar qanchalik toʻgʻri yaratilishi, saqlanishi, almashtirilishi va bekor qilinishiga bogʻliq (PKI, M8). Eng koʻp muammolar algoritmda emas, aynan shu yerda: muddati oʻtgan sertifikatlar, kodda yozib qoʻyilgan kalitlar, almashtirilmagan parollar.

**Kriptografiyaning chegaralari**:

- **Metamaʼlumotlar** — kim, kim bilan, qachon va qancha hajmda aloqa qilgani — koʻpincha shifrlanmaydi.
- **Oxirgi nuqtalar**: maʼlumot qurilmada ochiq holda boʻladi; zararli dastur uni shifrlashdan oldin (klaviatura, ekran) yoki keyin oʻqiydi.
- **Notoʻgʻri foydalanish**: sertifikat ogohlantirishini eʼtiborsiz qoldirish, eskirgan protokollar (A9).

## Zararkunanda dasturlardan himoya va Malwarebytes

**Malwarebytes** — zararli dasturlarni aniqlash va olib tashlashga ixtisoslashgan dastur (Windows, macOS, Android, iOS). Bepul versiyasi talab boʻyicha skanerlash va tozalash uchun, pullik versiyasi real vaqt himoyasi bilan.

| Imkoniyat | Tavsif |
|---|---|
| **Skanerlash** | tez skanerlash (xotira, avtoishga tushish, tizim maydonlari), toʻliq skanerlash, tanlangan papka |
| **Aniqlash** | imzolar, evristika, xatti-harakat; ayniqsa keraksiz dasturlar (PUP) va reklama dasturlarini yaxshi aniqlaydi |
| **Karantin** | topilgan obyektlar xavfsiz joyga ajratiladi; xato boʻlsa tiklash mumkin |
| **Real vaqt himoyasi** (pullik) | veb-himoya, shifrlovchilardan himoya, ekspluatatsiyadan himoya |
| **Boshqa antivirus bilan birga** | Windows Defender bilan birga "ikkinchi fikr" skaneri sifatida ishlatish mumkin; ikki real vaqt himoyasini bir vaqtda yoqmang (MT9) |

**Amaliy ish (sinov kompyuteri yoki virtual mashinada)**:

1. Malwarebytes ni faqat **rasmiy saytdan** yuklab oling; oʻrnatuvchining raqamli imzosini tekshiring (fayl → Properties → Digital Signatures).
2. Maʼlumotlar bazasini yangilang va **toʻliq skanerlash** oʻtkazing.
3. Natijalarni tahlil qiling: topilgan obyektlar turi (zararli, PUP, shubhali), joylashuvi; karantin boʻlimini koʻrib chiqing.
4. Xavfsiz sinov uchun **EICAR test fayli** dan foydalaning — u barcha antiviruslar "zararli" deb taniydigan, lekin hech qanday zarar keltirmaydigan standart sinov qatori; himoya ishlayotganini tekshirish uchun. Haqiqiy zararli namunalarni hech qachon yuklab olmang.
5. Hisobotda: skanerlash vaqti, tekshirilgan obyektlar soni, topilganlar va ularga nisbatan qaror.

**Zararlangan kompyuterni tozalash tartibi**:

1. Qurilmani tarmoqdan uzing (zarar tarqalmasligi va maʼlumot chiqib ketmasligi uchun).
2. Boshqa, toza qurilmadan muhim hisoblar (email, bank) parollarini almashtiring.
3. Yangilangan antivirus va ikkinchi skaner (masalan, Malwarebytes) bilan toʻliq skanerlang; kerak boʻlsa xavfsiz rejimda.
4. Topilganlarni karantinga oling, keyin qayta skanerlang.
5. Chuqur zararlanish, rootkit yoki shifrlovchi boʻlsa — OT ni toʻliq qayta oʻrnatish va toza zaxira nusxadan tiklash (MT17) ishonchliroq.
6. Sababni aniqlang (qaysi fayl, havola yoki dastur) va takrorlanmasligi uchun choralar koʻring; tashkilotda — IT xizmatiga xabar bering.

:::warning
**Koʻp uchraydigan xatolar**

- **"HTTPS bor — demak hammasi xavfsiz".** Kriptografiya kanalni himoyalaydi, qurilmadagi zararli dastur va fishingdan emas.
- **Sertifikat ogohlantirishlarini eʼtiborsiz qoldirish.** Bu shifrlashning autentiklik qismini bekor qiladi.
- **"Tozalovchi" dasturlarni reklama yoki qalqib chiquvchi oynalardan oʻrnatish.** Soxta antiviruslar — keng tarqalgan zararli dastur turi.
- **Ikki real vaqt himoyasini bir vaqtda yoqish.** Ziddiyat va sekinlashuv.
- **Tozalashdan keyin parollarni almashtirmaslik.** Oʻgʻirlangan parollar zararli dastur olib tashlangandan keyin ham ishlaydi.
:::

:::tip
Brauzeringizda bir nechta saytning ulanish maʼlumotlarini koʻring (qulf → sertifikat; F12 → Security): TLS versiyasi va shifr toʻplamini yozib oling (A9, M9). DNS sozlamalarida "Secure DNS" (DoH) ni yoqing va uning nimani yashirishini tushuntiring.
:::

## Xulosa

- Tarmoqda kriptografiya konfidensiallik, yaxlitlik, autentiklik va oldinga maxfiylikni taʼminlaydi.
- Protokollar qatlamlar boʻyicha: WPA3, IPSec/WireGuard, TLS, SSH, DNSSEC/DoH, S/MIME/PGP, uchdan-uchga shifrlash.
- Kalitlar va sertifikatlarni boshqarish — kriptografiyaning eng zaif boʻgʻini; metamaʼlumotlar va oxirgi nuqtalar himoyalanmaydi.
- Malwarebytes — zararli dasturlarni aniqlash va tozalash vositasi; asosiy antivirus bilan "ikkinchi fikr" sifatida.
- Tozalash: tarmoqdan uzish, parollarni almashtirish, toʻliq skanerlash, kerak boʻlsa qayta oʻrnatish, sababni aniqlash.

## Oʻzingizni tekshiring

:::check
Q: Uchdan-uchga shifrlash oddiy TLS dan nimasi bilan farq qiladi?
A: Xabar faqat yuboruvchi va qabul qiluvchi qurilmalarida ochiladi; xizmat serveri ham uni oʻqiy olmaydi.
Q: Kriptografiya qaysi narsalarni himoya qilmaydi?
A: Metamaʼlumotlarni (kim, kim bilan, qachon) va oxirgi nuqtadagi maʼlumotni — zararli dastur uni shifrlashdan oldin yoki keyin oʻqiydi.
Q: EICAR test fayli nima uchun ishlatiladi?
A: Antivirus ishlayotganini xavfsiz tekshirish uchun — u zararsiz, lekin barcha antiviruslar uni aniqlaydi.
Q: Zararlangan kompyuterni tozalashning birinchi qadami nima?
A: Qurilmani tarmoqdan uzish.
:::

:::link https://www.malwarebytes.com/
Malwarebytes
Zararli dasturlardan himoya vositasining rasmiy sayti (ingliz tilida).
:::
