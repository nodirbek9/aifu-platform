---
title: Server operatsion tizimlar va ishchi guruhlar. Server operatsion tizimlar va ularning vazifalari. Ishchi guruhlar va domenli tizimlar
summary: Server va ish stansiyasi operatsion tizimlarining farqi; Windows Server va Linux server distributivlari; server rollari va xizmatlari; ishchi guruh (peer-to-peer) va domen (mijoz–server) modellari; Active Directory — domen, domen kontrolleri, OU, guruh siyosatlari, oʻrmon va daraxt; markazlashgan autentifikatsiya; Linux dagi muqobillar.
---

## Nima uchun bu mavzu muhim?

Uyda uch-toʻrtta kompyuter uchun har bir kompyuterda alohida foydalanuvchi yaratish muammo emas. Lekin universitetdagi 1000 ta kompyuter va 5000 ta talaba uchun har bir kompyuterda alohida hisob ochib boʻlmaydi: talaba istalgan sinfda oʻz login va paroli bilan kirishi, administrator esa parol siyosatini, printer va dasturlarni bitta joydan boshqarishi kerak. Buni **server operatsion tizimlari** va **domen** modeli taʼminlaydi.

:::note
**Bu darsdan soʻng siz:**

- server va ish stansiyasi operatsion tizimlarining farqini tushuntira olasiz;
- asosiy server rollari va xizmatlarini ayta olasiz;
- ishchi guruh va domen modellarini solishtirib, qachon qaysi biri mosligini asoslay olasiz;
- Active Directory tuzilishini (domen, domen kontrolleri, OU, guruh siyosatlari) tushuntira olasiz.
:::

## Server operatsion tizimlari

| | Ish stansiyasi OT (Windows 11, Ubuntu Desktop) | Server OT (Windows Server, Ubuntu Server, RHEL) |
|---|---|---|
| Vazifasi | bitta foydalanuvchi ishi | koʻp mijozga xizmat koʻrsatish |
| Bir vaqtdagi ulanishlar | cheklangan (masalan, 20 ta umumiy papka ulanishi) | minglab |
| Grafik interfeys | asosiy | ixtiyoriy (Server Core, faqat buyruqlar satri) |
| Uskunalar | 1 protsessor, oddiy xotira | koʻp protsessor, katta RAM, RAID, ECC xotira |
| Ishonchlilik | qayta yuklash odatiy | uzluksiz ishlash, klasterlash, zaxira nusxa |
| Litsenziya | arzon | yadrolar yoki mijozlar soni boʻyicha (CAL) |

**Windows Server** (2016, 2019, 2022, 2025) nashrlari: **Standard** (2 ta virtual mashinagacha), **Datacenter** (cheksiz virtualizatsiya). Oʻrnatish turlari: **Desktop Experience** (grafik) va **Server Core** (grafiksiz, kam resurs va kamroq hujum yuzasi). Boshqaruv — **Server Manager**, **PowerShell**, **Windows Admin Center**.

**Linux serverlar**: Ubuntu Server, Debian, Red Hat Enterprise Linux (RHEL), Rocky/AlmaLinux. Internetdagi veb-serverlarning katta qismi Linux da ishlaydi. Boshqaruv — SSH va buyruqlar satri.

## Server rollari

| Rol | Vazifasi | Windows | Linux |
|---|---|---|---|
| Katalog xizmati | foydalanuvchilar va kompyuterlar bazasi, kirish | Active Directory Domain Services | Samba AD, FreeIPA, OpenLDAP |
| DHCP | manzillarni avtomatik berish | DHCP Server | isc-dhcp, Kea |
| DNS | nomlarni hal qilish | DNS Server | BIND, Unbound |
| Fayl serveri | umumiy papkalar | File and Storage Services (SMB) | Samba, NFS |
| Chop etish serveri | umumiy printerlar | Print Services | CUPS |
| Veb server | saytlar va ilovalar | IIS | Nginx, Apache |
| Virtualizatsiya | virtual mashinalar | Hyper-V | KVM |
| Masofaviy kirish | VPN, uzoqdan ish stoli | RRAS, RDS | OpenVPN, WireGuard |

Bitta jismoniy serverda koʻp rolni ishga tushirish mumkin, lekin yaxshi amaliyot — har bir muhim rolni alohida (koʻpincha virtual) serverda saqlash.

## Ishchi guruh va domen

| | **Ishchi guruh** (workgroup) | **Domen** (domain) |
|---|---|---|
| Model | teng huquqli (peer-to-peer) | mijoz–server |
| Hisoblar | har bir kompyuterda alohida (lokal) | markaziy bazada (domen kontrollerida) |
| Kirish | faqat shu kompyuterga | domendagi istalgan kompyuterga bitta login bilan |
| Boshqaruv | har bir kompyuterda qoʻlda | markazdan, guruh siyosatlari bilan |
| Hajm | ~10–20 kompyutergacha | yuzlab va minglab |
| Server | shart emas | kamida bitta domen kontrolleri (yaxshisi ikkita) |
| Narxi | bepul | server va litsenziyalar |

Ishchi guruh — uy va kichik ofis uchun: oddiy, lekin 10 ta kompyuterda parolni almashtirish 10 marta takrorlanadi. Domen — tashkilotlar uchun: markazlashgan xavfsizlik va boshqaruv.

## Active Directory

**Active Directory** (AD) — Microsoft ning katalog xizmati: tarmoqdagi barcha obyektlar (foydalanuvchilar, guruhlar, kompyuterlar, printerlar) haqidagi ierarxik baza.

- **Domen** — umumiy baza va xavfsizlik siyosatiga ega obyektlar guruhi, DNS nomi bilan: `aifu.local`.
- **Domen kontrolleri** (DC) — AD bazasini saqlaydigan va foydalanuvchilarni autentifikatsiya qiladigan server. Bir nechta DC bir-biri bilan **replikatsiya** qiladi — bittasi buzilsa, boshqasi ishlaydi.
- **OU** (Organizational Unit) — domen ichidagi "papkalar": `Talabalar`, `Oʻqituvchilar`, `Kompyuter sinflari`. Ularga siyosat bogʻlanadi va boshqaruv huquqi topshiriladi.
- **Guruh siyosatlari** (GPO) — markazdan qoʻllanadigan sozlamalar: parol uzunligi, ekran qulfi, dasturlarni oʻrnatish, printer ulash, USB ni taqiqlash.
- **Daraxt** (tree) va **oʻrmon** (forest) — bir-biriga ishonch munosabati bilan bogʻlangan domenlar: `aifu.local`, `talaba.aifu.local`.
- Autentifikatsiya protokoli — **Kerberos** (chiptalar), katalogga murojaat — **LDAP** (389-port). AD DNS ga kuchli bogʻliq: mijozlar DC ni DNS dagi SRV yozuvlari orqali topadi.

Kompyuterni domenga qoʻshish: tizim xususiyatlari → "Domen" → `aifu.local` → domen administratori login va paroli → qayta yuklash. Shundan soʻng kompyuterga `AIFU\talaba01` hisobi bilan kirish mumkin.

## Amalda: universitet tarmogʻi

| Element | Yechim |
|---|---|
| Talaba va oʻqituvchi hisoblari | AD dagi `Talabalar`, `Oʻqituvchilar` OU lari |
| Parol siyosati | domen GPO: kamida 10 belgi, 90 kunda almashtirish |
| Kompyuter sinflari | `Laboratoriyalar` OU, GPO bilan dasturlarni oʻrnatish va USB ni cheklash |
| Fayllar | fayl serveri, har bir guruhga umumiy papka, huquqlar guruhlar orqali |
| Manzillar va nomlar | DHCP va DNS serverlari (A11 da sozlanadi) |

:::warning
**Koʻp uchraydigan xatolar**

- **Domen va ishchi guruhni bir xil deb oʻylash.** Ishchi guruhda markaziy baza yoʻq — har bir kompyuter oʻz hisoblarini oʻzi saqlaydi.
- **Bitta domen kontrolleri bilan ishlash.** U buzilsa, hech kim tizimga kira olmaydi; kamida ikkita DC tavsiya etiladi.
- **Mijozlarning DNS sozlamasida tashqi DNS (8.8.8.8) ni koʻrsatish.** Domen kompyuterlari DC ni topa olmaydi; mijozlarga ichki DNS server berilishi kerak.
- **Huquqlarni har bir foydalanuvchiga alohida berish.** Huquqlar guruhlarga beriladi, foydalanuvchilar esa guruhlarga qoʻshiladi.
- **Serverda kundalik ishni administrator hisobi bilan bajarish.** Administrator hisobi faqat boshqaruv uchun.
:::

:::tip
Windows da `systeminfo | findstr /B "Domain"` (yoki tizim xususiyatlari) bilan kompyuteringiz ishchi guruhda yoki domenda ekanini aniqlang. Universitet kompyuterida `whoami` va `echo %USERDOMAIN%` buyruqlarini bajarib, domen nomini koʻring.
:::

## Xulosa

- Server OT koʻp mijozga uzluksiz xizmat koʻrsatish uchun moʻljallangan; Windows Server (Desktop Experience va Server Core) va Linux serverlar keng tarqalgan.
- Asosiy rollar: katalog xizmati, DHCP, DNS, fayl va chop etish, veb, virtualizatsiya, masofaviy kirish.
- Ishchi guruh — teng huquqli kichik tarmoq, lokal hisoblar; domen — markaziy hisoblar va boshqaruv.
- Active Directory: domen, domen kontrollerlari va replikatsiya, OU, GPO, daraxt va oʻrmon; Kerberos, LDAP, DNS ga tayanadi.

## Oʻzingizni tekshiring

:::check
Q: Server Core oʻrnatish turining afzalligi nimada?
A: Grafik interfeys yoʻq — kamroq resurs sarflaydi, kamroq yangilanish talab qiladi va hujum yuzasi kichik.
Q: Ishchi guruhda foydalanuvchi hisoblari qayerda saqlanadi?
A: Har bir kompyuterning oʻzida (lokal hisoblar).
Q: Domen kontrolleri nima vazifani bajaradi?
A: Active Directory bazasini saqlaydi va domen foydalanuvchilarini autentifikatsiya qiladi.
Q: OU va guruhning farqi nimada?
A: OU — obyektlarni tartiblash va siyosat qoʻllash uchun konteyner; guruh — huquqlar berish uchun foydalanuvchilar toʻplami.
Q: Nima uchun domen kompyuterlari ichki DNS serverdan foydalanishi kerak?
A: Ular domen kontrollerini DNS dagi SRV yozuvlari orqali topadi; tashqi DNS da bu yozuvlar yoʻq.
:::

:::link https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/get-started/virtual-dc/active-directory-domain-services-overview
Active Directory Domain Services overview
Microsoft ning AD DS haqidagi rasmiy sharhi (ingliz tilida).
:::
