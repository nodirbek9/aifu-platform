---
title: IoT qurilmalari xavfsizlik zaifliklarini oʻrganish (keys yoki taqdimot)
summary: Amaliy mashgʻulot — IoT xavfsizligini keys asosida tahlil qilish; tahlil metodikasi (qurilmalar roʻyxati, aktivlar va maʼlumot oqimlari, STRIDE boʻyicha tahdid modeli, bazaviy talablar bilan solishtirish, xavfni baholash, choralar); tarixiy keyslar — Mirai botneti, kazinodagi akvarium termometri, uy kameralari va bolalar monitorlari, ulangan avtomobil va dasturiy chaqiruv; har bir keysning asosiy sababi va saboqlari; muvofiqlashtirilgan zaiflik oshkor qilish; ishlab chiqaruvchi va foydalanuvchi uchun tavsiyalar; guruh taqdimoti tuzilishi va baholash mezonlari.
---

## Nima uchun bu mavzu muhim?

M15 maʼruzasida IoT arxitekturasi, keng tarqalgan zaifliklar va bazaviy talablarni (ETSI EN 303 645) koʻrdik. Bu amaliy mashgʻulotda real hodisalarni **himoya nuqtai nazaridan** tahlil qilasiz: nima sodir boʻlgan, asosiy sabab qaysi toifaga kiradi va qaysi oddiy chora hodisaning oldini olishi mumkin edi. Natija — guruh taqdimoti yoki yozma keys tahlili.

:::note
**Bu amaliy mashgʻulotdan soʻng siz:**

- IoT tizimi uchun tahdid modelini tuza olasiz;
- real hodisalarni sabab toifalari boʻyicha tahlil qila olasiz;
- har bir sabab uchun himoya choralarini taklif qila olasiz;
- muvofiqlashtirilgan zaiflik oshkor qilish jarayonini tushuntira olasiz;
- tahlil natijalarini tushunarli taqdimotda koʻrsata olasiz.
:::

:::warning
Mashgʻulot — tahliliy: hodisalar ochiq manbalardagi maʼlumotlar asosida, himoya xulosalari uchun oʻrganiladi. Hech qanday real qurilma yoki tarmoqni ruxsatsiz tekshirmang — bu qonun buzilishi (A3 dagi kiberjinoyatlar). Oʻz qurilmangizni tekshirish uchun M15 dagi audit roʻyxatidan foydalaning.
:::

## Tahlil metodikasi

| Qadam | Savol | Natija |
|---|---|---|
| **1. Tizimni tasvirlash** | qanday qurilmalar, tarmoq, bulut va ilova bor? | arxitektura sxemasi (M15 dagi toʻrt qatlam) |
| **2. Aktivlar va maʼlumot oqimlari** | qanday maʼlumot yigʻiladi, qayerga ketadi, kim foydalanadi? | maʼlumot oqimlari diagrammasi |
| **3. Tahdid modeli** | qanday tahdidlar boʻlishi mumkin? | STRIDE jadvali |
| **4. Bazaviy talablar bilan solishtirish** | ETSI EN 303 645 ning qaysi bandlari bajarilmagan? | chetga chiqishlar roʻyxati |
| **5. Xavfni baholash** | ehtimol va taʼsir qanday? | xavf matritsasi (A2) |
| **6. Choralar** | ishlab chiqaruvchi, tashkilot va foydalanuvchi nima qilishi kerak? | ustuvorlikka koʻra tavsiyalar |

**STRIDE** — tahdidlarni toifalash usuli:

| Toifa | Maʼnosi | IoT misoli | Himoya xususiyati |
|---|---|---|---|
| **S**poofing | oʻzini boshqa sifatida koʻrsatish | soxta qurilma bulutga ulanadi | autentifikatsiya, qurilma sertifikati |
| **T**ampering | maʼlumot yoki dasturni oʻzgartirish | imzolanmagan proshivka | yaxlitlik, imzolangan yangilanish |
| **R**epudiation | harakatni inkor etish | kim eshikni ochgani yozilmagan | jurnallar |
| **I**nformation disclosure | maʼlumot oshkor boʻlishi | shifrlanmagan video oqimi | shifrlash, kirish nazorati |
| **D**enial of service | xizmatning toʻxtashi | qurilma ishlamay qoladi | barqarorlik, cheklovlar |
| **E**levation of privilege | huquqlarni oshirish | oddiy foydalanuvchi administrator boʻladi | minimal imtiyoz |

## Tarixiy keyslar

Har bir keys ochiq manbalardagi maʼlumotlar asosida, faqat **sabab va saboq** darajasida keltirilgan.

### 1. Mirai botneti (2016)

- **Nima boʻldi**: zavod parollari oʻzgartirilmagan yuz minglab kameralar va routerlar zararli dastur bilan egallanib, botnetga birlashtirildi va yirik onlayn xizmatlarning ishlamay qolishiga sabab boʻlgan DDoS hujumlarida ishlatildi.
- **Asosiy sabab**: universal standart parollar, keraksiz ochiq boshqaruv xizmatlari.
- **STRIDE**: Spoofing (zavod hisobidan foydalanish), Denial of service (oqibat).
- **Saboq**: noyob parollar va keraksiz xizmatlarni oʻchirish — ETSI EN 303 645 ning 1- va 6-bandlari; keyinchalik bir qator mamlakatlar standart parollarni qonun bilan taqiqladi.

### 2. Kazinodagi akvarium termometri (2017-yilda eʼlon qilingan)

- **Nima boʻldi**: xavfsizlik kompaniyasi xabariga koʻra, kazino akvariumidagi Internetga ulangan termometr tashkilot tarmogʻiga kirish nuqtasi boʻlib xizmat qilgan va maʼlumotlar tashqariga chiqarilgan.
- **Asosiy sabab**: IoT qurilma ichki tarmoq bilan bitta segmentda, chiquvchi trafik nazorat qilinmagan.
- **STRIDE**: Information disclosure.
- **Saboq**: **segmentlash** (IoT — alohida segment), chiquvchi trafikni cheklash va monitoring (M14, A13), qurilmalar roʻyxati.

### 3. Uy kameralari va bolalar monitorlari

- **Nima boʻldi**: turli yillarda uy kameralari va bolalar monitorlariga begonalar kirib olgani, ayrim hollarda xonadagi odamlar bilan "gaplashgani" haqida koʻplab xabarlar boʻlgan.
- **Asosiy sabab**: boshqa saytlardan oʻgʻirlangan parollarning qayta ishlatilishi, MFA yoʻqligi, zaif bulut hisoblari, yangilanmagan dastur.
- **STRIDE**: Spoofing, Information disclosure.
- **Saboq**: noyob va kuchli parol, **MFA**, bulut hisobini himoyalash, shifrlangan video oqimi; ishlab chiqaruvchi uchun — MFA ni sukut boʻyicha yoqish, gʻayrioddiy kirishlar haqida xabar berish.

### 4. Ulangan avtomobil va dasturiy chaqiruv (2015)

- **Nima boʻldi**: tadqiqotchilar ulangan avtomobilning multimedia tizimi orqali masofadan boshqarish funksiyalariga taʼsir qilish mumkinligini koʻrsatdilar va ishlab chiqaruvchi bilan hamkorlikda natijalarni eʼlon qildilar; ishlab chiqaruvchi taxminan 1,4 million avtomobilni xavfsizlik yangilanishi uchun chaqirib oldi.
- **Asosiy sabab**: multimedia va avtomobil boshqaruv tizimlari orasida yetarli ajratish yoʻqligi, masofadan yangilash imkoni cheklanganligi.
- **STRIDE**: Elevation of privilege, Tampering.
- **Saboq**: muhim tizimlarni ajratish, imzolangan masofaviy yangilanishlar (OTA), **muvofiqlashtirilgan oshkor qilish** — tadqiqotchi va ishlab chiqaruvchining hamkorligi.

## Muvofiqlashtirilgan zaiflik oshkor qilish

**Muvofiqlashtirilgan oshkor qilish** (coordinated vulnerability disclosure, CVD) — zaiflik topilganda u avval ishlab chiqaruvchiga maxfiy xabar qilinadi, tuzatish tayyorlanadi va shundan keyingina ommaga eʼlon qilinadi:

1. Tadqiqotchi zaiflikni ishlab chiqaruvchining rasmiy kanali orqali xabar qiladi (ETSI EN 303 645 ning 2-bandi — ishlab chiqaruvchida bunday kanal boʻlishi shart).
2. Ishlab chiqaruvchi tasdiqlaydi va tuzatishni tayyorlaydi.
3. Kelishilgan muddatda yangilanish chiqariladi va zaiflik eʼlon qilinadi (koʻpincha **CVE** identifikatori bilan).
4. Foydalanuvchilar yangilanishni oʻrnatadi.

Bu jarayon foydalanuvchilarni zaiflik maʼlum, lekin hali tuzatilmagan davrda himoya qiladi.

## Kim nima qilishi kerak

| Tomon | Tavsiyalar |
|---|---|
| **Ishlab chiqaruvchi** | noyob parollar, xavfsizlikni loyihalashdan boshlash, imzolangan yangilanishlar va eʼlon qilingan qoʻllab-quvvatlash muddati, zaifliklar uchun aloqa kanali, sukut boʻyicha MFA |
| **Tashkilot** | IoT qurilmalari roʻyxati, alohida segment, xaridda xavfsizlik talablari, muntazam audit (M15), chiquvchi trafik monitoringi |
| **Foydalanuvchi** | zavod parolini almashtirish, yangilanishlarni oʻrnatish, MFA ni yoqish, keraksiz funksiyalarni (masofaviy kirish, UPnP) oʻchirish, qoʻllab-quvvatlanmaydigan qurilmalarni almashtirish |

## Amaliy ish: keys tahlili yoki taqdimot

3–4 kishilik guruh bitta IoT tizimini tanlaydi (aqlli uy, videokuzatuv, aqlli hisoblagichlar, kiyiladigan qurilma, ulangan avtomobil, aqlli shahar svetoforlari) yoki yuqoridagi keyslardan birini chuqurroq oʻrganadi.

**Taqdimot tuzilishi** (10–12 slayd, 10 daqiqa):

1. Tizim va uning arxitekturasi (toʻrt qatlam).
2. Aktivlar va maʼlumot oqimlari, shaxsiy maʼlumotlar.
3. STRIDE boʻyicha tahdidlar jadvali.
4. ETSI EN 303 645 bilan solishtirish.
5. Real hodisa (agar boʻlsa) — sabab va oqibat, ochiq manbalar bilan.
6. Xavf matritsasi va ustuvor choralar.
7. Ishlab chiqaruvchi, tashkilot va foydalanuvchi uchun tavsiyalar.
8. Xulosa va manbalar.

**Baholash mezonlari**:

| Mezon | Ball |
|---|---|
| Arxitektura va maʼlumot oqimlari toʻgʻri tasvirlangan | 20 |
| Tahdid modeli (STRIDE) toʻliq va asoslangan | 25 |
| Bazaviy talablar bilan solishtirish | 15 |
| Choralar aniq, amalga oshirsa boʻladigan va ustuvorlikka ega | 25 |
| Taqdimot sifati, manbalar, savollarga javob | 15 |
| **Jami** | **100** |

:::warning
**Koʻp uchraydigan xatolar**

- **Hujum texnikasini batafsil tasvirlash.** Maqsad — sabab va himoya; texnik "retseptlar" kerak emas.
- **Faqat texnik choralar.** Ishlab chiqaruvchi siyosati, xarid talablari va foydalanuvchi odatlari ham muhim.
- **Manbasiz daʼvolar.** Har bir faktni ishonchli manba bilan tasdiqlang.
- **Maxfiylikni unutish.** Kamera va sensorlar shaxsiy hayot haqida maʼlumot yigʻadi.
:::

:::tip
Taqdimotning asosiy xabarini bitta jumlada bering va har bir slayd sarlavhasini toʻliq tasdiq qilib yozing, masalan: "Uydagi kameralar xavfsizligining 80% i uchta oddiy choraga bogʻliq: noyob parol, MFA va yangilanish".
:::

## Xulosa

- IoT keysini tahlil qilish: tizim → aktivlar va oqimlar → STRIDE → bazaviy talablar → xavf → choralar.
- Keyslar saboqlari: standart parollar (Mirai), segmentlash yoʻqligi (akvarium termometri), qayta ishlatilgan parollar va MFA yoʻqligi (kameralar), tizimlarni ajratish va yangilanish (ulangan avtomobil).
- Muvofiqlashtirilgan oshkor qilish foydalanuvchilarni tuzatish tayyor boʻlgunga qadar himoya qiladi.
- Himoya — ishlab chiqaruvchi, tashkilot va foydalanuvchining birgalikdagi masʼuliyati.

## Oʻzingizni tekshiring

:::check
Q: STRIDE dagi "I" harfi qanday tahdidni bildiradi?
A: Information disclosure — maʼlumotning ruxsatsiz oshkor boʻlishi.
Q: Mirai hodisasidan qaysi asosiy saboq olindi?
A: Universal standart parollardan voz kechish va keraksiz xizmatlarni oʻchirish zarurati.
Q: Akvarium termometri keysida qaysi chora hodisaning oldini olishi mumkin edi?
A: IoT qurilmalarini alohida tarmoq segmentiga ajratish va chiquvchi trafikni nazorat qilish.
Q: Muvofiqlashtirilgan zaiflik oshkor qilish nima?
A: Zaiflik avval ishlab chiqaruvchiga maxfiy xabar qilinadi va tuzatish chiqqandan keyingina ommaga eʼlon qilinadi.
:::

:::link https://owasp.org/www-project-internet-of-things/
OWASP Internet of Things Project
IoT xavfsizligi boʻyicha asosiy xavflar va tavsiyalar (ingliz tilida).
:::
