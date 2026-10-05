---
title: Axborot tizimlarining asosiy tarkibiy qismlari
summary: Seminar — axborot tizimining taʼminot turlari: texnik, dasturiy, axborot (maʼlumotlar va klassifikatorlar), matematik, tashkiliy, huquqiy, ergonomik va kadrlar taʼminoti; ularning oʻzaro bogʻliqligi; real tizim misolida tarkibiy qismlarni tahlil qilish; axborot tizimi uchun talablar hujjatini tuzish topshirigʻi.
---

## Seminarning maqsadi

M9 maʼruzasida axborot tizimining funksiyalari va turlarini koʻrdik. Seminarda tizim **nimalardan tashkil topishini** — "taʼminot" turlarini — batafsil koʻrib chiqamiz va real tizim misolida har birini aniqlaymiz. Bu bilim tizim joriy etilayotganda nimani unutmaslik kerakligini koʻrsatadi: koʻpincha loyihalar texnika yoki dastur emas, kadrlar, tashkiliy yoki huquqiy taʼminot yetishmasligi sababli muvaffaqiyatsiz boʻladi.

:::note
**Bu seminardan soʻng siz:**

- axborot tizimining taʼminot turlarini ayta olasiz;
- har bir taʼminot turining vazifasini misollar bilan tushuntira olasiz;
- real tizimning tarkibiy qismlarini tahlil qila olasiz;
- oddiy axborot tizimi uchun talablar roʻyxatini tuza olasiz.
:::

## Taʼminot turlari

| Taʼminot | Mazmuni | Misol (universitet talabalar hisobi tizimi) |
|---|---|---|
| **Texnik** | kompyuterlar, serverlar, tarmoq, printerlar, skanerlar | serverlar, dekanat kompyuterlari, Wi-Fi |
| **Dasturiy** | OT, MBBT, amaliy dasturlar, xavfsizlik vositalari | Linux server, PostgreSQL, veb-ilova, antivirus |
| **Axborot** | maʼlumotlar bazalari, klassifikatorlar va kodlar, hujjat shakllari | talabalar, guruhlar, fanlar, baholar jadvallari; fanlar kodlari |
| **Matematik** | hisoblash usullari, algoritmlar, modellar | GPA hisoblash formulasi, stipendiya reytingi algoritmi |
| **Tashkiliy** | tartiblar, yoʻriqnomalar, rollar va masʼuliyat | kim baho qoʻyadi, qachon yopiladi, xatoni kim tuzatadi |
| **Huquqiy** | qonunlar, nizomlar, shartnomalar, litsenziyalar | shaxsga doir maʼlumotlar qonuni, universitet nizomi, dastur litsenziyasi |
| **Ergonomik** | foydalanuvchi uchun qulaylik, interfeys, ish joyi | tushunarli menyular, mobil versiya, uch tilda interfeys |
| **Kadrlar** | foydalanuvchilar va IT mutaxassislar, ularning malakasi | oʻqituvchilar, dekanat xodimlari, administratorlar, oʻqitish |
| **Lingvistik** (qoʻshimcha) | atamalar, tillar, lugʻatlar | oʻzbek, rus va ingliz tillaridagi atamalar |

## Taʼminot turlarining bogʻliqligi

- **Axborot** taʼminoti sifatsiz boʻlsa (masalan, fan kodlari takrorlanadi), eng yaxshi dastur ham notoʻgʻri hisobot beradi.
- **Tashkiliy** taʼminot boʻlmasa (baholarni kim, qachon kiritadi — belgilanmagan), tizimda maʼlumot oʻz vaqtida paydo boʻlmaydi.
- **Kadrlar** tayyor boʻlmasa, xodimlar qogʻozda ishlashda davom etadi.
- **Huquqiy** taʼminot shaxsiy maʼlumotlar bilan qonuniy ishlash va dastur litsenziyalariga rioya qilishni kafolatlaydi.

## Amaliy topshiriq: tizimni tahlil qilish

Guruhlarda bitta real tizimni tanlang: universitet platformasi, bank mobil ilovasi, my.gov.uz, doʻkon kassa tizimi yoki kutubxona katalogi.

1. Har bir taʼminot turi uchun kamida 2 ta aniq element yozing (jadval koʻrinishida).
2. Tizimning foydalanuvchilarini va ularning rollarini aniqlang.
3. Asosiy maʼlumotlar obyektlarini (jadvallar) va ularning bogʻlanishlarini sanab chiqing (M7).
4. Qaysi taʼminot turi eng zaif deb oʻylaysiz? Nima uchun va qanday yaxshilash mumkin?

## Amaliy topshiriq: talablar hujjati

Kichik tashkilot (masalan, oʻquv markazi) uchun "Talabalar va toʻlovlar hisobi" tizimiga talablar hujjatini tuzing:

| Boʻlim | Mazmuni |
|---|---|
| Maqsad | tizim qanday muammoni hal qiladi |
| Foydalanuvchilar | administrator, oʻqituvchi, buxgalter, talaba |
| Funksional talablar | "tizim har oy toʻlov qarzdorlari roʻyxatini chiqaradi" kabi aniq jumlalar |
| Maʼlumotlar | talabalar, guruhlar, toʻlovlar — asosiy maydonlar |
| Nofunksional talablar | tezlik, mavjudlik, xavfsizlik, til, mobil qurilmalar |
| Taʼminot | texnik, dasturiy, kadrlar va boshqalar boʻyicha zarur resurslar |
| Cheklovlar | byudjet, muddat, qonun talablari |

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat texnik va dasturiy taʼminotga eʼtibor berish.** Tashkiliy va kadrlar taʼminoti yetishmasa, tizim ishlamaydi.
- **Noaniq talablar.** "Tizim qulay boʻlsin" emas, "yangi foydalanuvchi 10 daqiqada asosiy amallarni oʻrganadi".
- **Klassifikator va kodlarni tartibga solmaslik.** Bir xil obyekt turli nomlar bilan kiritiladi.
- **Huquqiy talablarni unutish.** Shaxsiy maʼlumotlar va litsenziyalar — majburiy.
:::

:::tip
Talablar hujjatini yozganda har bir funksional talabni "foydalanuvchi … qila oladi" yoki "tizim … qiladi" shaklida yozing va uni qanday tekshirish mumkinligini (qabul mezoni) qoʻshing.
:::

## Xulosa

- Axborot tizimi taʼminot turlari: texnik, dasturiy, axborot, matematik, tashkiliy, huquqiy, ergonomik, kadrlar (va lingvistik).
- Ular bir-biriga bogʻliq: birining zaifligi butun tizimni zaiflashtiradi.
- Real tizimni tahlil qilish va talablar hujjatini tuzish — iqtisodchining tizim buyurtmachisi sifatidagi asosiy koʻnikmasi.

## Oʻzingizni tekshiring

:::check
Q: Axborot taʼminotiga nimalar kiradi?
A: Maʼlumotlar bazalari, klassifikatorlar va kodlar, hujjat shakllari.
Q: Tashkiliy taʼminot nima uchun kerak?
A: Kim, qachon va qanday maʼlumot kiritishini, rollar va masʼuliyatni belgilash uchun.
Q: Stipendiya reytingini hisoblash algoritmi qaysi taʼminot turiga kiradi?
A: Matematik taʼminotga.
Q: Yaxshi funksional talab qanday yoziladi?
A: Aniq va tekshiriladigan jumla bilan: "tizim … qiladi" va qabul mezoni bilan.
:::

:::link https://www.iso.org/standard/72089.html
ISO/IEC/IEEE 29148: Requirements engineering
Tizim va dasturiy taʼminot talablarini tuzish standarti (ingliz tilida).
:::
