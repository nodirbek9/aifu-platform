---
title: Axborot tizimlarining asosiy tarkibiy qismlari
summary: Mustaqil ish — besh komponentli model (apparat, dasturiy taʼminot, maʼlumotlar, tartib-qoidalar, odamlar) va tarmoq; maʼlumotlarning turlari — asosiy (master), tranzaksion, maʼlumotnoma (klassifikatorlar: STIR, IFUT, bank MFO kodi), metamaʼlumotlar; uch qatlamli arxitektura (taqdimot, biznes-mantiq, maʼlumotlar); integratsiya — API, fayl almashinuvi, integratsiya shinasi; onlayn nazorat-kassa tizimi misolida komponentlarni tahlil qilish; "eng zaif boʻgʻin" tamoyili; topshiriq.
---

## Mustaqil ishning maqsadi

S9 seminarida axborot tizimini taʼminot turlari (texnik, dasturiy, axborot, tashkiliy, huquqiy va boshqalar) boʻyicha koʻrib chiqdik. Bu mustaqil ishda xalqaro adabiyotda keng qoʻllanadigan **besh komponentli model** va zamonaviy tizimlarning ichki tuzilishini — qatlamlar va integratsiyani — oʻrganamiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- axborot tizimining besh komponentini va ularning oʻzaro bogʻliqligini tushuntira olasiz;
- maʼlumotlarning asosiy turlarini va klassifikatorlar rolini ayta olasiz;
- uch qatlamli arxitekturani tasvirlay olasiz;
- tizimlar integratsiyasi usullarini farqlay olasiz.
:::

## Besh komponentli model

| Komponent | Mazmuni | Misol (bank filiali) |
|---|---|---|
| **Apparat** (hardware) | kompyuterlar, serverlar, tarmoq uskunalari, kassa va terminallar | operator kompyuteri, bankomat, server |
| **Dasturiy taʼminot** (software) | operatsion tizim, MBBT, amaliy dasturlar | bank ABS (M15), mobil ilova |
| **Maʼlumotlar** (data) | faktlar va yozuvlar | mijozlar, hisoblar, tranzaksiyalar |
| **Tartib-qoidalar** (procedures) | tizimdan qanday foydalanish yoʻriqnomalari | hisob ochish reglamenti, kunlik yopilish tartibi |
| **Odamlar** (people) | foydalanuvchilar, administratorlar, ishlab chiquvchilar | operator, kassir, IT xodimi, mijoz |

Bularni **tarmoq** (M6, MT5) bogʻlab turadi. Besh komponent ikki "qanot"ga boʻlinadi: chap tomonda apparat va dastur (**kompyuter tomoni**), oʻng tomonda tartib-qoidalar va odamlar (**inson tomoni**), oʻrtada — ikkalasini bogʻlovchi **maʼlumotlar**.

:::tip
Odatda tizim joriy etilganda eng koʻp eʼtibor apparat va dasturga beriladi, lekin eng koʻp muammo **tartib-qoidalar va odamlar** tomonida chiqadi: yoʻriqnoma yoʻq, xodimlar oʻqitilmagan, masʼuliyat belgilanmagan.
:::

## Maʼlumotlar turlari

| Tur | Mazmuni | Misol | Xususiyati |
|---|---|---|---|
| **Asosiy maʼlumotlar** (master data) | biznesning asosiy obyektlari | mijozlar, mahsulotlar, xodimlar, kontragentlar | kam oʻzgaradi, koʻp tizimlarda ishlatiladi |
| **Tranzaksion maʼlumotlar** | hodisalar va operatsiyalar | savdolar, toʻlovlar, kirim-chiqim | juda koʻp, doim oʻsadi |
| **Maʼlumotnoma maʼlumotlari** (klassifikatorlar) | standart kodlar roʻyxati | valyuta kodlari, mamlakatlar, faoliyat turlari | tashqi standartlardan olinadi |
| **Metamaʼlumotlar** | maʼlumot haqidagi maʼlumot | ustun nomi va turi, kim va qachon oʻzgartirgan | maʼlumotni topish va tushunish uchun (MT9) |

**Oʻzbekistondagi muhim klassifikatorlar va identifikatorlar**:

- **STIR** — soliq toʻlovchining identifikatsiya raqami (korxonalar va jismoniy shaxslar).
- **JSHSHIR (PINFL)** — jismoniy shaxsning shaxsiy identifikatsiya raqami.
- **IFUT** — iqtisodiy faoliyat turlarining umumdavlat tasniflagichi.
- **Bank MFO kodi** — bank filiallarining kodi; toʻlov topshiriqnomalarida ishlatiladi.
- **Tovarlar va xizmatlarning identifikatsiya kodlari (IKPU)** — elektron hisob-faktura va onlayn kassa cheklarida.

Klassifikatorlar turli tizimlarning bir-birini "tushunishi" uchun zarur: bank, soliq va statistika tizimi korxonani bir xil STIR bilan taniydi.

**Asosiy maʼlumotlarni boshqarish** (MDM): bitta mijoz CRM, buxgalteriya va sayt bazasida turlicha yozilsa (MT9 dagi "yagonalik" muammosi), hisobotlar ziddiyatli boʻladi. MDM — asosiy obyektlar uchun yagona "haqiqat manbai"ni yaratish.

## Uch qatlamli arxitektura

| Qatlam | Vazifasi | Misol |
|---|---|---|
| **Taqdimot** (interfeys) | foydalanuvchi bilan muloqot | veb-sahifa, mobil ilova, kassa ekrani |
| **Biznes-mantiq** (ilova serveri) | qoidalar va hisob-kitoblar | soliqni hisoblash, kredit qarori (MT13), limitlarni tekshirish |
| **Maʼlumotlar** | saqlash va qidirish | MBBT (MT8) |

Qatlamlarga ajratish afzalliklari: interfeysni (masalan, yangi mobil ilova) biznes-mantiqni oʻzgartirmasdan qoʻshish mumkin; har bir qatlam alohida masshtablanadi; xavfsizlik — foydalanuvchi maʼlumotlar bazasiga toʻgʻridan-toʻgʻri kira olmaydi.

## Integratsiya

Kompaniyada odatda oʻnlab tizimlar ishlaydi (buxgalteriya, CRM, sayt, ombor, bank-mijoz). Ular orasida maʼlumot almashish usullari:

| Usul | Mazmuni | Afzallik / kamchilik |
|---|---|---|
| **Qoʻlda** | Excel ga eksport va boshqa tizimga import | oddiy, lekin sekin va xatoga moyil |
| **Fayl almashinuvi** | belgilangan vaqtda XML, CSV fayllar | sodda; real vaqtda emas |
| **API** | tizimlar bir-biriga dastur orqali soʻrov yuboradi | real vaqtda, avtomatik; ishlab chiqish talab qiladi |
| **Integratsiya shinasi (ESB)**, xabar navbatlari | markaziy "pochta" orqali almashish | koʻp tizim uchun tartib; murakkab |

Davlat sektorida idoralararo integratsiya platformasi (M10) xuddi shu gʻoyada ishlaydi: fuqaro maʼlumotnomani bir idoradan olib boshqasiga eltmaydi — tizimlar API orqali almashadi.

## Misol: onlayn nazorat-kassa tizimi

| Komponent | Tarkibi |
|---|---|
| **Apparat** | onlayn kassa yoki kassa ilovasi oʻrnatilgan smartfon, fiskal modul, chek printeri, Internet |
| **Dasturiy taʼminot** | kassa dasturi, fiskal maʼlumotlar operatori serveri, soliq organi tizimi |
| **Maʼlumotlar** | chek (tranzaksion), IKPU kodlari va STIR (klassifikatorlar), sotuvchi va tovarlar (asosiy) |
| **Tartib-qoidalar** | har bir sotuvda chek berish, smena ochish va yopish, nosozlikda harakat tartibi |
| **Odamlar** | kassir, doʻkon egasi, soliq inspektori, xaridor (chekni QR orqali tekshiradi) |
| **Integratsiya** | kassa → operator → soliq organi (real vaqtda), xaridor → chekni tekshirish xizmati |

Bu tizimning bitta komponenti ishlamasa (Internet uzilsa, kassir chek bermasa), butun tizim maqsadi — savdoni shaffof qilish — buziladi. Bu **"eng zaif boʻgʻin" tamoyili**: tizim samaradorligi eng zaif komponenti bilan belgilanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Axborot tizimini faqat dastur deb bilish.** Odamlar, tartib-qoidalar va maʼlumotlarsiz u ishlamaydi.
- **Klassifikatorlarni eʼtiborsiz qoldirish.** Notoʻgʻri IKPU yoki STIR — hisob-faktura rad etiladi.
- **Bitta obyektni bir necha tizimda turlicha yuritish.** Asosiy maʼlumotlarni boshqarish kerak.
- **Tizimlarni qoʻlda bogʻlash.** Excel orqali koʻchirish — xato va kechikishlar manbai.
:::

## Topshiriq

Bitta axborot tizimini tanlang (universitet LMS, bank mobil ilovasi, my.gov.uz portali, internet-doʻkon):

1. Besh komponentni jadval koʻrinishida tavsiflang.
2. Tizimdagi asosiy, tranzaksion va maʼlumotnoma maʼlumotlariga 2 tadan misol keltiring.
3. Uch qatlamni koʻrsating: foydalanuvchi nimani koʻradi, qaysi qoidalar ishlaydi, maʼlumot qayerda saqlanadi.
4. Tizim qaysi tashqi tizimlar bilan integratsiyalashgan va qanday usulda?
5. Tizimning "eng zaif boʻgʻini"ni toping va uni kuchaytirish taklifini bering.

## Xulosa

- Besh komponent: apparat, dasturiy taʼminot, maʼlumotlar, tartib-qoidalar, odamlar; ularni tarmoq bogʻlaydi.
- Maʼlumotlar: asosiy, tranzaksion, maʼlumotnoma (klassifikatorlar), metamaʼlumotlar; MDM yagona haqiqat manbaini yaratadi.
- Uch qatlam: taqdimot, biznes-mantiq, maʼlumotlar.
- Integratsiya: qoʻlda, fayl, API, integratsiya shinasi; tizim eng zaif boʻgʻini darajasida ishlaydi.

## Oʻzingizni tekshiring

:::check
Q: Besh komponentli modelning komponentlari qaysilar?
A: Apparat, dasturiy taʼminot, maʼlumotlar, tartib-qoidalar va odamlar.
Q: Asosiy va tranzaksion maʼlumotlarning farqi nima?
A: Asosiy maʼlumotlar — biznesning kam oʻzgaradigan obyektlari (mijozlar, mahsulotlar); tranzaksion — hodisalar va operatsiyalar (savdolar, toʻlovlar).
Q: Klassifikatorlar nima uchun kerak?
A: Turli tizimlar bir xil obyektlarni yagona standart kodlar orqali tushunishi uchun.
Q: API orqali integratsiyaning afzalligi nima?
A: Maʼlumot real vaqtda va avtomatik, qoʻl mehnatisiz almashiladi.
:::

:::link https://lex.uz/
Lex.uz
Davlat klassifikatorlari va axborot tizimlariga oid normativ hujjatlar.
:::
