---
title: Maʼlumotlar bazalari va ularni ishlab chiqish modellari
summary: Mustaqil ish — maʼlumotlar modellari (ierarxik, tarmoqli, relatsion, hujjatli, kalit-qiymat, graf); maʼlumotlar bazasini loyihalash bosqichlari (talablar, konseptual, mantiqiy, fizik model, joriy etish); ER-modellashtirish — mohiyatlar, atributlar, bogʻlanishlar va kardinallik; kalitlar va yaxlitlik cheklovlari; normallashtirish (1NF, 2NF, 3NF) savdo jadvali misolida; anomaliyalar; SQL va NoSQL ni tanlash; loyihalash topshiriqi.
---

## Mustaqil ishning maqsadi

M7 da maʼlumotlar bazasi tushunchasi va SQL soʻrovlari (JOIN, GROUP BY) bilan tanishdik. Bu mustaqil ishda bir qadam orqaga qaytamiz: jadvallar **qanday loyihalanadi**, shunda maʼlumotlar takrorlanmaydi, xatolar kamayadi va soʻrovlar toʻgʻri natija beradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- asosiy maʼlumotlar modellarini farqlay olasiz;
- MB loyihalash bosqichlarini ayta olasiz;
- oddiy ER-diagramma chiza olasiz;
- jadvalni uchinchi normal shaklgacha normallashtira olasiz;
- vazifa uchun relatsion yoki NoSQL bazani tanlay olasiz.
:::

## Maʼlumotlar modellari

| Model | Tuzilishi | Misol / qoʻllanish |
|---|---|---|
| **Ierarxik** | daraxt: har bir yozuvning bitta "ota"si | fayl tizimi, eski bank tizimlari (IBM IMS) |
| **Tarmoqli** | yozuvlar koʻp-koʻp bogʻlangan graf | 1970-yillar tizimlari |
| **Relatsion** | jadvallar (munosabatlar) va kalitlar orqali bogʻlanish | PostgreSQL, MySQL, Oracle, SQL Server — buxgalteriya, bank, ERP |
| **Hujjatli** (NoSQL) | JSON kabi hujjatlar, moslashuvchan tuzilma | MongoDB — mahsulot katalogi, kontent |
| **Kalit-qiymat** (NoSQL) | kalit boʻyicha tez qidiruv | Redis — sessiyalar, kesh, savatcha |
| **Graf** (NoSQL) | tugunlar va qirralar | Neo4j — ijtimoiy aloqalar, firibgarlik tarmoqlari |

Relatsion model (E. Kodd, 1970) iqtisodiy tizimlarda hanuz asosiy: qatʼiy tuzilma, tranzaksiyalar va yaxlitlik kafolati (ACID) moliya uchun muhim.

## Loyihalash bosqichlari

| Bosqich | Natija |
|---|---|
| **1. Talablarni yigʻish** | qanday maʼlumotlar, kim foydalanadi, qanday hisobotlar kerak |
| **2. Konseptual model** | ER-diagramma: mohiyatlar va bogʻlanishlar (MBBT ga bogʻliq emas) |
| **3. Mantiqiy model** | jadvallar, ustunlar, kalitlar, normallashtirish |
| **4. Fizik model** | maʼlumot turlari, indekslar, aniq MBBT uchun sozlash |
| **5. Joriy etish va qoʻllab-quvvatlash** | yaratish, maʼlumotni koʻchirish, zaxira, optimallashtirish |

## ER-modellashtirish

- **Mohiyat** (entity) — maʼlumot saqlanadigan obyekt: Mijoz, Buyurtma, Mahsulot.
- **Atribut** — mohiyat xususiyati: Mijoz → ism, telefon, shahar.
- **Bogʻlanish** va **kardinallik**:

| Kardinallik | Misol |
|---|---|
| **1 : 1** | Xodim — Pasport |
| **1 : N** (birga-koʻp) | Mijoz — Buyurtmalar (bitta mijozning koʻp buyurtmasi) |
| **N : M** (koʻpga-koʻp) | Buyurtma — Mahsulot (buyurtmada koʻp mahsulot, mahsulot koʻp buyurtmada) |

N : M bogʻlanish relatsion bazada **oraliq jadval** bilan amalga oshiriladi (masalan, BuyurtmaQatori: buyurtma, mahsulot, miqdor).

```text
 Mijoz 1 ──── N Buyurtma 1 ──── N BuyurtmaQatori N ──── 1 Mahsulot
```

## Kalitlar va yaxlitlik

- **Birlamchi kalit** (primary key) — har bir satrni yagona aniqlaydi (mijoz_id). Bir jadvalda bitta.
- **Tashqi kalit** (foreign key) — boshqa jadvalning birlamchi kalitiga havola (Buyurtma.mijoz_id → Mijoz.mijoz_id).
- **Havolaviy yaxlitlik** — mavjud boʻlmagan mijozga buyurtma yozib boʻlmaydi; buyurtmasi bor mijozni oʻchirish cheklanadi.
- **Boshqa cheklovlar**: `NOT NULL` (majburiy), `UNIQUE` (takrorlanmas, masalan, STIR), `CHECK` (narx > 0).

## Normallashtirish: ishlangan misol

Doʻkon savdolarini bitta Excel jadvalida yuritadi:

| buyurtma | sana | mijoz | mijoz_tel | mahsulot | narx | miqdor |
|---|---|---|---|---|---|---|
| 101 | 03.10 | Aliyev | 901112233 | Choynak, Piyola | 85000, 15000 | 1, 6 |
| 102 | 03.10 | Karimova | 935556677 | Choynak | 85000 | 2 |
| 103 | 04.10 | Aliyev | 901112233 | Piyola | 15000 | 12 |

**Anomaliyalar**:

- **Yangilash**: Aliyev telefonini oʻzgartirsa, uni barcha satrlarda oʻzgartirish kerak — bittasi esdan chiqsa, maʼlumot ziddiyatli.
- **Qoʻshish**: hali sotilmagan yangi mahsulotni (narxi bilan) kiritib boʻlmaydi — buyurtma kerak.
- **Oʻchirish**: 102-buyurtma oʻchirilsa, Karimova haqidagi maʼlumot ham yoʻqoladi.

**1NF** (birinchi normal shakl) — har bir katakda bitta qiymat. 101-buyurtma ikki satrga ajratiladi: (101, Choynak, 85000, 1) va (101, Piyola, 15000, 6).

**2NF** — har bir ustun butun kalitga bogʻliq. 1NF dan keyin kalit (buyurtma, mahsulot), lekin narx faqat mahsulotga, sana va mijoz faqat buyurtmaga bogʻliq. Jadvallar ajratiladi:

- **Buyurtma** (buyurtma, sana, mijoz, mijoz_tel)
- **Mahsulot** (mahsulot, narx)
- **BuyurtmaQatori** (buyurtma, mahsulot, miqdor)

**3NF** — kalit boʻlmagan ustun boshqa kalit boʻlmagan ustunga bogʻliq emas. Buyurtmada mijoz_tel buyurtmaga emas, mijozga bogʻliq. Natija:

| Jadval | Ustunlar | Kalit |
|---|---|---|
| **Mijoz** | mijoz_id, ism, telefon | mijoz_id |
| **Mahsulot** | mahsulot_id, nom, narx | mahsulot_id |
| **Buyurtma** | buyurtma_id, sana, mijoz_id | buyurtma_id; mijoz_id — tashqi kalit |
| **BuyurtmaQatori** | buyurtma_id, mahsulot_id, miqdor | (buyurtma_id, mahsulot_id) |

Endi telefon bitta joyda saqlanadi, mahsulotni buyurtmasiz kiritish mumkin, buyurtma oʻchirilsa mijoz saqlanib qoladi. Hisobotlar esa JOIN bilan yigʻiladi (M7).

:::tip
Muhim nozik jihat: mahsulot narxi vaqt oʻtishi bilan oʻzgaradi. Eski buyurtmalar summasi oʻzgarmasligi uchun amalda **sotilgan paytdagi narx** BuyurtmaQatori ga ham yoziladi — bu takror emas, tarixiy fakt.
:::

## SQL yoki NoSQL?

| Vaziyat | Tanlov |
|---|---|
| Pul, hisob, qoldiqlar — aniqlik va tranzaksiyalar muhim | relatsion (SQL) |
| Tuzilmasi tez-tez oʻzgaradigan katalog, turli xususiyatli mahsulotlar | hujjatli |
| Juda tez oʻqish, vaqtinchalik maʼlumot | kalit-qiymat |
| Aloqalar tahlili (kim kimga pul oʻtkazgan) | graf |
| Katta hajm, tahlil (Big Data, M7, S7) | taqsimlangan tizimlar, maʼlumotlar omborlari |

Koʻp kompaniyalar bir nechta turdan birga foydalanadi ("poliglot saqlash").

:::warning
**Koʻp uchraydigan xatolar**

- **Bitta katta jadval ("Excel uslubi").** Anomaliyalar va ziddiyatli maʼlumotlar.
- **Bir katakda bir nechta qiymat** ("Choynak, Piyola") — filtrlash va hisoblash imkonsiz.
- **Ism yoki telefonni kalit qilish.** Ular takrorlanadi va oʻzgaradi; maxsus identifikator ishlating.
- **Haddan tashqari normallashtirish.** Hisobot uchun 10 ta JOIN kerak boʻlsa, tahliliy bazada ataylab qisman takrorlashga ruxsat beriladi.
:::

## Topshiriq

Oʻquv markazi uchun maʼlumotlar bazasini loyihalang: talabalar, oʻqituvchilar, kurslar, guruhlar, toʻlovlar.

1. Mohiyatlar va ularning atributlarini yozing.
2. Bogʻlanishlar va kardinallikni aniqlang (masalan, talaba — guruh: N : M).
3. ER-diagramma chizing (qogʻozda yoki draw.io da).
4. Jadvallarni 3NF da yozing: ustunlar, birlamchi va tashqi kalitlar.
5. "Har bir kurs boʻyicha oylik tushum" hisoboti qaysi jadvallarni birlashtirishni talab qiladi?

## Xulosa

- Modellar: ierarxik, tarmoqli, relatsion (asosiy), hujjatli, kalit-qiymat, graf.
- Loyihalash: talablar → konseptual (ER) → mantiqiy → fizik → joriy etish.
- Kalitlar va cheklovlar maʼlumot yaxlitligini taʼminlaydi.
- Normallashtirish (1NF–3NF) takror va anomaliyalarni yoʻqotadi; vazifaga qarab SQL yoki NoSQL tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: Yangilash anomaliyasi nima?
A: Bir xil maʼlumot bir necha joyda saqlanganda, uni hamma joyda oʻzgartirish zarurati va bittasi esdan chiqsa yuzaga keladigan ziddiyat.
Q: 1NF talabi nima?
A: Har bir katakda faqat bitta (boʻlinmas) qiymat boʻlishi.
Q: Koʻpga-koʻp bogʻlanish relatsion bazada qanday amalga oshiriladi?
A: Ikkala jadval kalitlarini oʻz ichiga olgan oraliq jadval bilan.
Q: Tashqi kalit nima?
A: Boshqa jadvalning birlamchi kalitiga havola qiluvchi ustun.
:::

:::link https://www.postgresql.org/docs/current/ddl-constraints.html
PostgreSQL: Constraints
Birlamchi va tashqi kalitlar hamda boshqa cheklovlar boʻyicha rasmiy hujjat (ingliz tilida).
:::
