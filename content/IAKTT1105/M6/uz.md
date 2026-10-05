---
title: Tarmoq texnologiyalari va internet xizmatlari
summary: Kompyuter tarmogʻi tushunchasi va iqtisodiy ahamiyati; tarmoq turlari — PAN, LAN, MAN, WAN, Internet; topologiyalar; tarmoq qurilmalari; aloqa kanallari — optik tola, mis kabel, Wi-Fi, mobil aloqa (4G, 5G), sunʼiy yoʻldosh; IP manzil, DNS va domenlar, protokollar (TCP/IP, HTTP/HTTPS) haqida asosiy tushunchalar; Internet xizmatlari — veb, elektron pochta, bulutli saqlash, messenjerlar, videokonferensiyalar, onlayn toʻlovlar va savdo; korporativ tarmoq va intranet; kanalni tanlashda iqtisodiy mezonlar (tezlik, ishonchlilik, narx, SLA).
---

## Nima uchun bu mavzu muhim?

Bugun deyarli har bir iqtisodiy jarayon tarmoq orqali oʻtadi: onlayn kassa cheklari soliq xizmatiga, toʻlovlar bankka, buyurtmalar omborga, hisobotlar rahbarga. Internet uzilib qolsa, savdo va hisob-kitob toʻxtaydi. Iqtisodchi tarmoq qanday ishlashini muhandis darajasida bilishi shart emas, lekin asosiy tushunchalarni, Internet xizmatlarini va aloqa kanalini tanlash mezonlarini bilishi kerak.

:::note
**Bu darsdan soʻng siz:**

- tarmoq turlari va topologiyalarini ayta olasiz;
- asosiy tarmoq qurilmalari va aloqa kanallarini bilasiz;
- IP manzil, DNS, domen va protokol tushunchalarini oddiy tilda tushuntira olasiz;
- Internet xizmatlarining iqtisodiyotdagi qoʻllanishini misollar bilan koʻrsata olasiz;
- aloqa kanalini iqtisodiy mezonlar boʻyicha tanlay olasiz.
:::

## Tarmoq tushunchasi va turlari

**Kompyuter tarmogʻi** — maʼlumot almashish va resurslarni (printer, fayllar, Internet, dasturlar) birgalikda ishlatish uchun bogʻlangan qurilmalar majmui.

| Tur | Qamrovi | Misol |
|---|---|---|
| **PAN** (shaxsiy) | bir necha metr | telefon va simsiz quloqchin (Bluetooth) |
| **LAN** (lokal) | bitta xona, bino, kampus | ofis tarmogʻi, universitet kompyuter sinfi |
| **MAN** (shahar) | shahar | shahar operatorining tarmogʻi |
| **WAN** (global) | mamlakatlar, qitʼalar | bank filiallarini bogʻlovchi tarmoq |
| **Internet** | butun dunyo | tarmoqlar tarmogʻi |

**Topologiyalar**: "yulduz" (barcha qurilmalar markaziy kommutatorga ulanadi — eng keng tarqalgan, bitta kabel uzilsa faqat bitta kompyuter uziladi), "shina" va "halqa" (tarixiy), "toʻr" (bir nechta yoʻl — ishonchli, magistral tarmoqlarda).

## Tarmoq qurilmalari va aloqa kanallari

| Qurilma | Vazifasi |
|---|---|
| **Tarmoq kartasi** | kompyuterni tarmoqqa ulaydi (simli yoki Wi-Fi) |
| **Kommutator** (switch) | lokal tarmoqda qurilmalarni bogʻlaydi |
| **Router** (marshrutizator) | turli tarmoqlarni, masalan ofis tarmogʻini Internet bilan bogʻlaydi |
| **Wi-Fi kirish nuqtasi** | simsiz ulanish |
| **Modem / ONT** | provayder liniyasiga ulanish (optik tolada ONT) |

| Kanal | Tezlik | Xususiyatlari |
|---|---|---|
| **Optik tola** | yuzlab Mbit/s dan Gbit/s gacha | eng tez va barqaror, korxonalar va uylar uchun |
| **Mis kabel** (Ethernet, ADSL) | ofisda 1 Gbit/s; ADSL past | ADSL eskirmoqda |
| **Wi-Fi** | yuzlab Mbit/s | qulay, lekin devorlar va shovqinga bogʻliq |
| **Mobil aloqa** (4G LTE, 5G) | onlarcha–yuzlab Mbit/s | harakatda, zaxira kanal sifatida |
| **Sunʼiy yoʻldosh** | turlicha | olis hududlar, kechikish (yangi past orbitali tizimlarda kamroq) |

## Internet asoslari: oddiy tilda

- **IP manzil** — tarmoqdagi qurilmaning "pochta manzili" (masalan, `192.168.1.10` yoki IPv6 manzil).
- **Domen nomi** — odam uchun qulay nom (`soliq.uz`); **DNS** uni IP manzilga aylantiradi — "telefon kitobi" kabi.
- **Protokol** — maʼlumot almashish qoidalari: TCP/IP (maʼlumotni yetkazish), HTTP/HTTPS (veb-sahifalar; HTTPS — shifrlangan), SMTP/IMAP (pochta).
- **Provayder** (ISP) — Internetga ulanish xizmatini koʻrsatuvchi kompaniya.
- **Tezlik** Mbit/s da; **kechikish** (ping) millisekundlarda — videoqoʻngʻiroq va onlayn savdo uchun muhim.

## Internet xizmatlari va iqtisodiyot

| Xizmat | Iqtisodiy qoʻllanishi |
|---|---|
| **Veb-saytlar va portallar** | kompaniya sayti, internet-doʻkon, davlat xizmatlari portali |
| **Elektron pochta** | rasmiy yozishmalar, hisob-fakturalar, mijozlarga xabarlar |
| **Bulutli saqlash va ofis** | fayllarni birgalikda tahrirlash, zaxira (M11) |
| **Messenjerlar** (Telegram va boshq.) | mijozlar bilan tezkor aloqa, buyurtmalar, botlar |
| **Videokonferensiyalar** (Zoom, Teams, Meet) | masofaviy yigʻilish, muzokara, onlayn taʼlim |
| **Onlayn toʻlovlar va bank** | internet-banking, Click, Payme, QR toʻlovlar (M13) |
| **Elektron tijorat va marketpleyslar** | onlayn savdo, yetkazib berish (M13) |
| **Qidiruv va onlayn reklama** | mijozlarni jalb qilish, marketing |

**Korporativ tarmoq**: **intranet** — faqat xodimlar uchun ichki portal (hujjatlar, yangiliklar, ilovalar); **ekstranet** — hamkorlar uchun cheklangan kirish; filiallar xavfsiz kanal (VPN) orqali bogʻlanadi.

## Kanalni tanlashda iqtisodiy mezonlar

| Mezon | Savollar |
|---|---|
| **Tezlik** | xodimlar soni, bulut xizmatlari, videoqoʻngʻiroqlar uchun yetarlimi? |
| **Ishonchlilik** | provayder kafolati — **SLA** (masalan, 99,9% ishlash vaqti), nosozlikni bartaraf etish muddati |
| **Narx** | oylik toʻlov, ulanish va uskunalar narxi |
| **Zaxira kanal** | ikkinchi provayder yoki mobil Internet — savdo toʻxtamasligi uchun |
| **Statik IP, qoʻllab-quvvatlash** | server yoki kamera tizimi uchun kerakmi, 24/7 yordam bormi? |

Misol hisob: oyiga 300 ming soʻmlik zaxira mobil kanal qimmatmi? Agar asosiy kanal yiliga 2 kun ishlamasa va doʻkon kuniga 5 mln soʻm savdo qilsa, yoʻqotish 10 mln soʻm, zaxira kanal esa yiliga 3,6 mln soʻm — zaxira foydaliroq.

:::warning
**Koʻp uchraydigan xatolar**

- **Megabit va megabaytni adashtirish.** 100 Mbit/s ≈ 12,5 MB/s (M2).
- **Faqat narxga qarab provayder tanlash.** Uzilishlar va sekin yordam yoʻqotishga olib keladi — SLA ni tekshiring.
- **Zaxira kanalsiz ishlash.** Onlayn kassa va toʻlovlarga bogʻliq biznes uchun xavfli.
- **Jamoat Wi-Fi da maxfiy ish qilish.** HTTPS va VPN siz maʼlumot tinglanishi mumkin.
:::

:::tip
speedtest.net yoki shunga oʻxshash xizmatda uy yoki universitet Internet tezligi va kechikishini oʻlchang; natijani provayder vaʼda qilgan tezlik bilan solishtiring.
:::

## Xulosa

- Tarmoq — maʼlumot va resurslarni birgalikda ishlatish uchun bogʻlangan qurilmalar; turlari PAN, LAN, MAN, WAN, Internet.
- Qurilmalar: tarmoq kartasi, kommutator, router, Wi-Fi nuqta; kanallar: optik tola, kabel, Wi-Fi, mobil, sunʼiy yoʻldosh.
- IP manzil — qurilma manzili, DNS — domenni IP ga aylantiradi, HTTPS — shifrlangan veb.
- Internet xizmatlari: veb, pochta, bulut, messenjerlar, videokonferensiya, onlayn toʻlov va savdo.
- Kanal tanlash: tezlik, ishonchlilik (SLA), narx, zaxira kanal, qoʻllab-quvvatlash.

## Oʻzingizni tekshiring

:::check
Q: LAN va WAN ning farqi nima?
A: LAN — bitta bino yoki kampus doirasidagi lokal tarmoq; WAN — shaharlar va mamlakatlarni qamrab oluvchi global tarmoq.
Q: DNS nima vazifani bajaradi?
A: Domen nomini (masalan, soliq.uz) IP manzilga aylantiradi.
Q: Router kommutatordan nimasi bilan farq qiladi?
A: Kommutator lokal tarmoq ichidagi qurilmalarni bogʻlaydi; router turli tarmoqlarni, masalan ofisni Internet bilan bogʻlaydi.
Q: SLA nima?
A: Provayderning xizmat sifati boʻyicha kafolati — ishlash vaqti foizi va nosozlikni bartaraf etish muddati.
:::

:::link https://www.itu.int/en/ITU-D/Statistics/Pages/default.aspx
ITU: ICT Statistics
Mamlakatlar boʻyicha Internet va aloqa koʻrsatkichlari (ingliz tilida).
:::
