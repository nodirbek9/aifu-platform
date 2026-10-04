---
title: Kirish. Hisoblash tarmoqlarining rivojlanish tarixi. Kompyuter tarmoqlari klassifikatsiyasi
summary: Kompyuter tarmogʻi tushunchasi va vazifalari; terminal tizimlardan ARPANET va Internetgacha rivojlanish tarixi; tarmoqlarni hudud (PAN, LAN, MAN, WAN), boshqaruv (mijoz–server, peer-to-peer), uzatish muhiti va kommutatsiya usuli boʻyicha tasniflash; tarmoq xarakteristikalari.
---

## Nima uchun bu mavzu muhim?

Bugun deyarli hech bir kompyuter yolgʻiz ishlamaydi: telefon xabar yuboradi, bank ilovasi serverga murojaat qiladi, universitet platformasi talabaga topshiriq yuboradi. Bularning hammasi **kompyuter tarmogʻi** orqali sodir boʻladi. Tarmoq qanday tuzilganini bilish dasturchiga ham, tizim administratoriga ham, axborot xavfsizligi mutaxassisiga ham kerak: nima uchun sayt sekin ochilyapti, nima uchun printer topilmayapti, maʼlumot qanday yoʻl bilan va qanchalik xavfsiz yetib boradi — bularga faqat tarmoq asoslarini bilgan odam javob bera oladi.

Bu kirish maʼruzasida tarmoq tushunchasini, uning qanday rivojlanganini va tarmoqlarni qanday turlarga ajratish mumkinligini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- kompyuter tarmogʻi va uning asosiy vazifalarini taʼriflay olasiz;
- hisoblash tarmoqlari rivojlanishining asosiy bosqichlarini sanab bera olasiz;
- tarmoqlarni hudud, boshqaruv usuli, uzatish muhiti va kommutatsiya turi boʻyicha tasniflay olasiz;
- tarmoqning asosiy xarakteristikalarini (oʻtkazuvchanlik, kechikish, ishonchlilik) tushuntira olasiz.
:::

## Kompyuter tarmogʻi nima?

**Kompyuter tarmogʻi** — maʼlumot almashish va resurslardan birgalikda foydalanish uchun aloqa kanallari orqali ulangan kompyuterlar va boshqa qurilmalar majmui.

| Tarmoqning vazifasi | Misol |
|---|---|
| Maʼlumot almashish | elektron pochta, messenjerlar, fayl yuborish |
| Resurslarni birgalikda ishlatish | bitta printer, umumiy disk, internet kanali |
| Markazlashgan boshqaruv | foydalanuvchilar hisoblari, zaxira nusxalash |
| Masofaviy xizmatlar | veb-saytlar, bulutli hisoblash, onlayn taʼlim |

Har qanday tarmoqda uchta tarkibiy qism bor: **tugunlar** (kompyuter, server, telefon, printer), **aloqa kanallari** (kabel, radio, optik tola) va **protokollar** — maʼlumot almashish qoidalari.

## Rivojlanish tarixi

| Davr | Bosqich | Mazmuni |
|---|---|---|
| 1950–1960-yillar | Terminal tizimlar | bitta katta EHM ga oddiy terminallar (ekran va klaviatura) ulanadi; barcha hisob markazda |
| 1969 | ARPANET | AQSh mudofaa vazirligi loyihasi: 4 ta universitet kompyuteri birinchi **paketli kommutatsiya** tarmogʻiga ulandi |
| 1970-yillar | Lokal tarmoqlar | Ethernet (1973, Xerox) — bitta bino ichidagi kompyuterlarni ulash |
| 1974–1983 | TCP/IP | V. Serf va R. Kan protokollar stekini taklif qildi; 1983-yil 1-yanvarda ARPANET TCP/IP ga oʻtdi — Internetning „tugʻilgan kuni“ |
| 1989–1991 | World Wide Web | T. Berners-Li (CERN) veb-sahifalar, HTTP va HTML ni yaratdi |
| 1990-yillar | Tijoriy Internet | provayderlar, elektron pochta va veb hammaga ochildi |
| 2000-yillar | Keng polosali va simsiz | ADSL, optika, Wi-Fi, 3G |
| 2010-yillar – hozir | Mobil va bulutli | 4G/5G, bulutli xizmatlar, „narsalar interneti“ (IoT) |

Asosiy burilish nuqtasi — **paketli kommutatsiya**: maʼlumot kichik boʻlaklarga (paketlarga) boʻlinadi va har biri tarmoq boʻylab mustaqil yuboriladi. Bu telefon tarmoqlaridagi doimiy ulanishdan ancha tejamkor va bitta liniya uzilsa, paketlar boshqa yoʻldan boradi.

## Tarmoqlar tasnifi

### Hudud boʻyicha

| Tur | Toʻliq nomi | Qamrovi | Misol |
|---|---|---|---|
| PAN | Personal Area Network | bir necha metr | telefon va simsiz quloqchin (Bluetooth) |
| LAN | Local Area Network | xona, bino, kampus | universitet kompyuter sinfi |
| MAN | Metropolitan Area Network | shahar | shahar provayderining optik halqasi |
| WAN | Wide Area Network | mamlakat, qitʼa, butun dunyo | Internet, bank filiallari tarmogʻi |

### Boshqaruv usuli boʻyicha

| Tur | Xususiyati | Afzalligi | Kamchiligi |
|---|---|---|---|
| **Mijoz–server** | xizmatlarni maxsus serverlar beradi, mijozlar soʻraydi | markazlashgan boshqaruv va xavfsizlik | server ishdan chiqsa — xizmat toʻxtaydi |
| **Peer-to-peer** (teng huquqli) | har bir kompyuter ham mijoz, ham server | oddiy, server kerak emas | kattalashganda boshqarish qiyin |

### Uzatish muhiti boʻyicha

- **Simli**: oʻralgan juftlik (twisted pair), koaksial kabel, optik tola.
- **Simsiz**: radio (Wi-Fi, Bluetooth, mobil aloqa), infraqizil, sunʼiy yoʻldosh.

### Kommutatsiya usuli boʻyicha

| Usul | Gʻoyasi | Misol |
|---|---|---|
| Kanallar kommutatsiyasi | aloqa davomida ikki abonent orasida doimiy kanal ajratiladi | anʼanaviy telefon tarmogʻi |
| Paketlar kommutatsiyasi | maʼlumot paketlarga boʻlinadi, kanal boshqalar bilan boʻlishiladi | Internet |
| Xabarlar kommutatsiyasi | xabar butunlay qabul qilinib, keyin uzatiladi | eski telegraf, elektron pochta tamoyili |

## Tarmoq xarakteristikalari

| Xarakteristika | Maʼnosi | Oʻlchov birligi |
|---|---|---|
| **Oʻtkazuvchanlik qobiliyati** (bandwidth) | vaqt birligida uzatiladigan maʼlumot hajmi | bit/s (Kbit/s, Mbit/s, Gbit/s) |
| **Kechikish** (latency) | maʼlumot manbadan qabul qiluvchigacha yetish vaqti | millisekund (ms) |
| **Ishonchlilik** | xatosiz va uzilishsiz ishlash ehtimoli | foiz, yoʻqolgan paketlar ulushi |
| **Masshtablanuvchanlik** | tugunlar sonini oshirish imkoniyati | — |
| **Xavfsizlik** | maʼlumotni ruxsatsiz kirishdan himoyalash | — |

Eʼtibor bering: tarmoq tezligi **bitlarda** oʻlchanadi, fayl hajmi esa koʻpincha **baytlarda**. 100 Mbit/s kanal soniyasiga taxminan 12,5 MB uzatadi (1 bayt = 8 bit).

:::warning
**Koʻp uchraydigan xatolar**

- **Internet va World Wide Web ni bir narsa deb hisoblash.** Internet — tarmoqlar tarmogʻi; veb — uning ustida ishlaydigan xizmatlardan biri (pochta, messenjerlar, fayl almashish ham Internetdan foydalanadi).
- **Mbit/s va MB/s ni aralashtirish.** 100 Mbit/s ≠ 100 MB/s; farq 8 baravar.
- **LAN ni faqat „kichik“ tarmoq deb tushunish.** Kampus LAN minglab qurilmani qamrashi mumkin; tasnif hudud va boshqaruv bilan bogʻliq, qurilmalar soni bilan emas.
- **Kechikish va oʻtkazuvchanlikni chalkashtirish.** Keng kanal ham uzoq masofada (masalan, sunʼiy yoʻldosh orqali) katta kechikishga ega boʻlishi mumkin.
:::

:::tip
Atrofingizdagi tarmoqlarni tasniflab koʻring: uy Wi-Fi, universitet kompyuter sinfi, mobil internet, telefoningiz va soatingiz orasidagi Bluetooth. Har biri qaysi hududiy turga, qaysi boshqaruv usuliga va qaysi uzatish muhitiga tegishli?
:::

## Xulosa

- Kompyuter tarmogʻi — maʼlumot almashish va resurslarni birgalikda ishlatish uchun ulangan qurilmalar; tugunlar, kanallar va protokollardan iborat.
- Rivojlanish: terminal tizimlar → ARPANET va paketli kommutatsiya → Ethernet → TCP/IP va Internet → veb → mobil va bulutli tarmoqlar.
- Tasnif: hudud (PAN, LAN, MAN, WAN), boshqaruv (mijoz–server, peer-to-peer), uzatish muhiti (simli, simsiz), kommutatsiya (kanal, paket, xabar).
- Asosiy xarakteristikalar: oʻtkazuvchanlik, kechikish, ishonchlilik, masshtablanuvchanlik, xavfsizlik.

## Oʻzingizni tekshiring

:::check
Q: Internetning „tugʻilgan kuni“ deb qaysi voqea hisoblanadi?
A: 1983-yil 1-yanvarda ARPANET ning TCP/IP protokollariga oʻtishi.
Q: Paketli kommutatsiya kanallar kommutatsiyasidan nimasi bilan farq qiladi?
A: Doimiy kanal ajratilmaydi: maʼlumot paketlarga boʻlinib, kanalni boshqalar bilan boʻlishgan holda mustaqil yuboriladi.
Q: Shahar provayderining optik halqasi qaysi turdagi tarmoq?
A: MAN — shahar miqyosidagi tarmoq.
Q: 50 Mbit/s kanal soniyasiga taxminan necha megabayt uzatadi?
A: 50 / 8 ≈ 6,25 MB.
Q: Peer-to-peer tarmoqning asosiy kamchiligi nima?
A: Tugunlar koʻpayganda boshqarish va xavfsizlikni taʼminlash qiyinlashadi.
:::

:::link https://www.internetsociety.org/internet/history-internet/brief-history-internet/
Internet Society: Internetning qisqacha tarixi
Internet asoschilari tomonidan yozilgan tarixiy sharh (ingliz tilida).
:::
