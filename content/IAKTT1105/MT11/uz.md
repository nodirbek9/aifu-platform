---
title: Hisoblash jarayonlarining grafik tasviri
summary: Mustaqil ish — blok-sxemadan tashqari grafik tasvirlash vositalari; biznes-jarayonlarni modellashtirish notatsiyasi BPMN (hodisalar, vazifalar, shlyuzlar, "suzish yoʻlaklari"); UML faoliyat diagrammasi; maʼlumotlar oqimi diagrammasi (DFD); qaror jadvallari; algoritmni qoʻlda tekshirish — kuzatuv jadvali; vositalar (draw.io, Visio, Lucidchart, Camunda Modeler); kredit arizasini koʻrib chiqish jarayoni misolida modellashtirish topshiriqi.
---

## Mustaqil ishning maqsadi

S8 seminarida blok-sxemalar va ularning standart belgilarini (ISO 5807) oʻrgandik. Iqtisodiyotda esa koʻpincha bitta dastur emas, **bir nechta boʻlim va odamlar ishtirok etadigan jarayon** tasvirlanadi: kredit berish, xarid, hisob-fakturani tasdiqlash. Buning uchun boshqa grafik tillar qoʻllanadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- BPMN ning asosiy elementlarini oʻqiy va chiza olasiz;
- "suzish yoʻlaklari" bilan masʼuliyatni koʻrsata olasiz;
- UML faoliyat diagrammasi va DFD qachon kerakligini bilasiz;
- murakkab shartlarni qaror jadvali bilan tasvirlay olasiz;
- algoritmni kuzatuv jadvali bilan qoʻlda tekshira olasiz.
:::

## Qaysi tasvir qachon?

| Tasvir | Nimani koʻrsatadi | Kim uchun |
|---|---|---|
| **Blok-sxema** (S8) | bitta algoritm qadamlari | dasturchi, talaba |
| **BPMN** | tashkilotdagi biznes-jarayon, ishtirokchilar va hujjatlar | tahlilchi, rahbar, IT |
| **UML faoliyat diagrammasi** | tizimdagi harakatlar ketma-ketligi va parallellik | tizim tahlilchisi, dasturchi |
| **DFD** (maʼlumotlar oqimi) | maʼlumot qayerdan kelib, qayerda saqlanib, qayerga ketadi | axborot tizimini loyihalovchi (M9) |
| **Qaror jadvali** | koʻp shartlar kombinatsiyasi va natijalar | tahlilchi, auditor |

## BPMN asoslari

**BPMN** (Business Process Model and Notation) — biznes-jarayonlarni tasvirlashning xalqaro standarti (ISO/IEC 19510).

| Element | Belgi | Maʼnosi |
|---|---|---|
| **Boshlanish hodisasi** | ingichka doira ○ | jarayonni ishga tushiruvchi: "ariza kelib tushdi" |
| **Yakuniy hodisa** | qalin doira ◉ | jarayon tugashi: "kredit berildi" |
| **Oraliq hodisa** | ikki chiziqli doira | kutish, taymer, xabar: "3 kun ichida javob" |
| **Vazifa** (task) | burchaklari yumaloq toʻrtburchak | bajariladigan ish: "Skoringni tekshirish" |
| **Eksklyuziv shlyuz** | ichida × bor romb | faqat bitta yoʻl tanlanadi (ha / yoʻq) |
| **Parallel shlyuz** | ichida + bor romb | bir nechta yoʻl bir vaqtda bajariladi |
| **Ketma-ketlik oqimi** | toʻgʻri strelka | vazifalar tartibi |
| **Xabar oqimi** | uzuq-uzuq strelka | tashkilotlar orasidagi xabar |
| **Hovuz va yoʻlak** (pool, lane) | gorizontal polosalar | kim bajaradi: boʻlim yoki rol |
| **Maʼlumot obyekti** | burchagi bukilgan varaq | hujjat: ariza, shartnoma |

**Misol: isteʼmol krediti arizasi** (yoʻlaklar — Mijoz, Kredit menejeri, Risk boʻlimi):

```text
┌ Mijoz ──────────┬───────────────────────────────────────────────────────────────┐
│  ○ → [Ariza yuborish] ···········································> [Javob olish] → ◉
├ Kredit menejeri ┼───────────────────────────────────────────────────────────────┤
│        ↓                                                      ↑         ↑
│  [Hujjatlarni tekshirish] → <×> toʻliqmi? ── yoʻq → [Hujjat soʻrash] ─┘   │
│                              │ ha                                         │
├ Risk boʻlimi ───┼────────────┼────────────────────────────────────────────┤
│                              ↓                                            │
│        <+> → [Kredit tarixi] ─┐                                           │
│         └──> [Daromadni tekshirish] → <+> → [Skoring] → <×> ball ≥ 600? ──┘
└─────────────────┴───────────────────────────────────────────────────────────────┘
```

Parallel shlyuz (<+>) kredit tarixi va daromad tekshiruvi bir vaqtda bajarilishini bildiradi — bu jarayonni tezlashtiradi. Yoʻlaklar har bir qadam uchun **kim javobgar** ekanini koʻrsatadi.

## UML faoliyat diagrammasi

UML (Unified Modeling Language) — dasturiy tizimlarni modellashtirish tili. Faoliyat diagrammasi blok-sxemaga oʻxshaydi, lekin **parallel harakatlar** (qalin chiziq — "ayrilish" va "qoʻshilish") va **boʻlimlar** (partition) bilan. Masalan, internet-doʻkonda toʻlov tasdiqlangach, "chek yuborish" va "omborga buyurtma" parallel bajariladi.

## Maʼlumotlar oqimi diagrammasi (DFD)

DFD jarayon ketma-ketligini emas, **maʼlumot harakatini** koʻrsatadi. Toʻrt element:

| Element | Misol |
|---|---|
| **Tashqi obyekt** (toʻrtburchak) | Mijoz, Soliq qoʻmitasi |
| **Jarayon** (doira yoki yumaloq toʻrtburchak) | "Buyurtmani qayta ishlash" |
| **Maʼlumot ombori** (ochiq toʻrtburchak) | "Buyurtmalar" bazasi |
| **Maʼlumot oqimi** (strelka) | "buyurtma maʼlumotlari", "elektron hisob-faktura" |

DFD axborot tizimini loyihalashda (M9, MT8) qaysi jadvallar va integratsiyalar kerakligini aniqlashga yordam beradi.

## Qaror jadvallari

Shartlar koʻp boʻlsa, blok-sxema rombsimon "oʻrmon"ga aylanadi. **Qaror jadvali** barcha kombinatsiyalarni ixcham koʻrsatadi. Misol — kredit arizasi boʻyicha qaror:

| Shart / Qoida | 1 | 2 | 3 | 4 |
|---|---|---|---|---|
| Skoring balli ≥ 600 | ha | ha | yoʻq | yoʻq |
| Oylik toʻlov daromadning ≤ 50% | ha | yoʻq | ha | yoʻq |
| **Harakat** | tasdiqlash | kamroq summa taklif qilish | kafil bilan koʻrib chiqish | rad etish |

Jadval auditorga ham tushunarli: har bir holat uchun aniq qoida bor va hech bir kombinatsiya "esdan chiqmagan".

## Kuzatuv jadvali

Algoritm yoki blok-sxemani kompyutersiz tekshirish uchun **kuzatuv jadvali** (trace table) tuziladi: har bir qadamda oʻzgaruvchilar qiymati yoziladi. Misol — oylik tushumlar yigʻindisi va eng katta oy:

```text
tushumlar: 40, 55, 35, 60
jami = 0, eng_katta = 0
HAR BIR t UCHUN:
    jami = jami + t
    AGAR t > eng_katta U HOLDA eng_katta = t
```

| Qadam | t | jami | t > eng_katta? | eng_katta |
|---|---|---|---|---|
| boshlanish | — | 0 | — | 0 |
| 1 | 40 | 40 | ha | 40 |
| 2 | 55 | 95 | ha | 55 |
| 3 | 35 | 130 | yoʻq | 55 |
| 4 | 60 | 190 | ha | 60 |

Natija: jami 190, eng katta oy 60. Kuzatuv jadvali xatolarni dastur yozishdan oldin topishga yordam beradi — masalan, `eng_katta` boshlangʻich qiymati 0 boʻlsa, barcha tushumlar manfiy boʻlganda (zarar) algoritm notoʻgʻri ishlaydi.

## Vositalar

| Vosita | Xususiyati |
|---|---|
| **draw.io** (diagrams.net) | bepul, brauzerda, BPMN, UML, blok-sxema shakllari |
| **Microsoft Visio** | korporativ, Microsoft 365 bilan |
| **Lucidchart** | bulutli, birgalikda ishlash |
| **Camunda Modeler, Bizagi** | BPMN modellash va jarayonlarni avtomatlashtirish |
| **PlantUML, Mermaid** | matndan diagramma (kod sifatida saqlanadi) |

:::warning
**Koʻp uchraydigan xatolar**

- **Yoʻlaklarsiz jarayon.** Kim javobgarligi noaniq qoladi.
- **Shlyuzdan chiquvchi yoʻllarga yozuv yoʻq** ("ha", "yoʻq", shart). Oʻquvchi qaysi yoʻl qachon tanlanishini bilmaydi.
- **Bitta diagrammada hammasi.** Katta jarayonni kichik osti jarayonlarga boʻling.
- **Qaror jadvalida kombinatsiya tushib qolishi.** n ta ha/yoʻq shartda 2ⁿ ta ustun boʻlishi kerak (yoki birlashtirilganlari koʻrsatilishi).
:::

## Topshiriq

1. Universitetga hujjat topshirish (yoki internet-doʻkondan buyurtma) jarayonini BPMN da kamida 3 ta yoʻlak bilan chizing (draw.io da).
2. Jarayonda kamida bitta eksklyuziv va bitta parallel shlyuz boʻlsin.
3. Shu jarayon uchun DFD chizing: tashqi obyektlar, jarayonlar, maʼlumot omborlari.
4. "Ish haqiga mukofot" qoidasi uchun 3 shartli qaror jadvali tuzing (masalan, reja bajarilishi, ish staji, intizom).
5. Kuzatuv jadvalidagi misolni tushumlar −10, −5, −20 boʻlganda tekshiring va xatoni tuzating.

## Xulosa

- Blok-sxema — bitta algoritm uchun; tashkiliy jarayonlar uchun BPMN, tizimlar uchun UML va DFD.
- BPMN: hodisalar, vazifalar, shlyuzlar (× va +), oqimlar, hovuz va yoʻlaklar.
- DFD maʼlumot harakatini koʻrsatadi; qaror jadvali koʻp shartli qoidalarni ixcham ifodalaydi.
- Kuzatuv jadvali algoritmni qoʻlda tekshirish va xatolarni erta topish vositasi.

## Oʻzingizni tekshiring

:::check
Q: BPMN dagi yoʻlak (lane) nimani bildiradi?
A: Jarayon qadamlarini bajaruvchi boʻlim yoki rolni.
Q: Eksklyuziv va parallel shlyuzning farqi nima?
A: Eksklyuzivda faqat bitta yoʻl tanlanadi, parallelda bir nechta yoʻl bir vaqtda bajariladi.
Q: DFD blok-sxemadan nima bilan farq qiladi?
A: U harakatlar ketma-ketligini emas, maʼlumotlarning qayerdan kelib, qayerda saqlanib, qayerga ketishini koʻrsatadi.
Q: 3 ta ha/yoʻq shartli qaror jadvalida nechta qoida (ustun) boʻladi?
A: 2³ = 8.
:::

:::link https://www.bpmn.org/
BPMN.org
BPMN notatsiyasi boʻyicha rasmiy maʼlumot va namunalar (ingliz tilida).
:::
