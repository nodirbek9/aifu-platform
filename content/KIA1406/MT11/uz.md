---
title: Kiberjinoyatchilikni keng tarqalishining sabablari. Yuz tasviriga asoslangan autentifikatsiya usuli va uning xususiyatlari
summary: Kiberjinoyatchilik tarqalishining iqtisodiy, texnologik, ijtimoiy va huquqiy sabablari — past xarajat va yuqori daromad, anonimlik va kriptovalyutalar, "xizmat sifatida jinoyat" bozori, raqamlashuv tezligi va zaif himoya, xabardorlik yetishmasligi, transchegaraviy tergov qiyinligi; jinoyat uchburchagi va oldini olish yondashuvlari; yuz orqali autentifikatsiya — 2D va 3D usullar, ishlash bosqichlari, aniqlik, tirik ekanini aniqlash, adolatlilik va maxfiylik masalalari, qonuniy talablar.
---

## Nima uchun bu mavzu muhim?

Kiberjinoyatchilik dunyodagi eng tez oʻsayotgan jinoyat turlaridan biri: FBI ning IC3 markazi har yili yuz minglab shikoyat va milliardlab dollar zarar qayd etadi, Oʻzbekistonda ham karta firibgarligi va onlayn aldovlar bilan bogʻliq ishlar soni sezilarli oshgan. Nima uchun aynan shunday ekanini tushunish — himoyani toʻgʻri yoʻnaltirishning asosi. Ikkinchi mavzu — yuz orqali autentifikatsiya — bugun telefonlar, banklar va davlat xizmatlarida keng qoʻllanmoqda; uning kuchli va zaif tomonlarini bilish ham ana shu himoyaning bir qismi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- kiberjinoyatchilik tarqalishining asosiy sabablarini tahlil qila olasiz;
- jinoyat uchburchagi modeli orqali oldini olish choralarini taklif qila olasiz;
- yuz orqali autentifikatsiya qanday ishlashini tushuntira olasiz;
- 2D va 3D usullar, tirik ekanini aniqlash farqini bilasiz;
- yuzni tanishning maxfiylik, adolatlilik va huquqiy masalalarini ayta olasiz.
:::

## Kiberjinoyatchilik tarqalishining sabablari

| Sabab | Mazmuni |
|---|---|
| **Past xarajat, yuqori daromad** | hujum uchun qimmat uskunalar kerak emas; bitta muvaffaqiyatli firibgarlik yoki toʻlov katta daromad keltiradi |
| **Anonimlik va masofa** | jinoyatchi boshqa davlatda boʻlishi, soxta hisoblar va kriptovalyutalardan foydalanishi mumkin |
| **"Xizmat sifatida jinoyat"** | tayyor vositalar, oʻgʻirlangan maʼlumotlar va hujum infratuzilmasi pullik "xizmat" sifatida sotiladi — texnik bilimi past odamlar ham qatnashadi |
| **Tez raqamlashuv** | onlayn toʻlovlar, davlat xizmatlari, masofaviy ish — himoya ularning orqasidan ulgurmaydi |
| **Zaif himoya** | yangilanmagan tizimlar, takrorlangan parollar, MFA yoʻqligi (M10, A3 keyslari) |
| **Inson omili** | xabardorlik yetishmasligi, ishonuvchanlik, shoshilish — ijtimoiy muhandislik shunga tayanadi |
| **Huquqiy va tergov qiyinliklari** | transchegaraviy ishlar, dalillarni yigʻish va ekstraditsiya murakkabligi, jabrlanuvchilar koʻpincha murojaat qilmaydi |
| **Ijtimoiy-iqtisodiy omillar** | ishsizlik, yengil pul vasvasasi, "virtual" jinoyatni jiddiy qabul qilmaslik |

**Jinoyat uchburchagi** (motiv, imkoniyat, oqlash) kiberjinoyatga ham mos keladi: motivni kamaytirish qiyin, lekin **imkoniyatni** (texnik himoya, MFA, yangilanishlar, toʻlovlarni tasdiqlash) va **oqlashni** (xabardorlik, qonun va jazoning muqarrarligi) kamaytirish mumkin.

**Oldini olish yondashuvlari**: texnik himoya (M8–M14); aholining va xodimlarning raqamli savodxonligi; banklar va platformalarning firibgarlikni aniqlash tizimlari; tez murojaat qilish va tergov (Oʻzbekistonda — ichki ishlar organlari va bank, Kiberxavfsizlik markazi); xalqaro hamkorlik (Budapesht konvensiyasi, BMT kiberjinoyat konvensiyasi). Huquqiy asoslar M3 da koʻrilgan.

## Yuz orqali autentifikatsiya

**Ishlash bosqichlari**:

1. **Aniqlash** (detection): tasvirda yuzni topish.
2. **Tekislash** (alignment): yuzni koʻz va burun kabi nuqtalar boʻyicha standart holatga keltirish.
3. **Belgilarni ajratish**: chuqur neyron tarmoq yuzni sonli vektor — **embedding** (masalan, 128–512 son) ga aylantiradi.
4. **Solishtirish**: saqlangan vektor bilan masofa yoki oʻxshashlik hisoblanadi va chegara bilan taqqoslanadi (MT12 dagi kabi FAR/FRR muvozanati).

| Usul | Qanday | Xususiyatlari |
|---|---|---|
| **2D** (oddiy kamera) | rang tasviri | arzon, hamma qurilmada; rasm yoki videoga aldanishi mumkin — tirik ekanini aniqlash muhim |
| **3D** (strukturali yorugʻlik, ToF) | yuz chuqurligi xaritasi | rasm va ekranga chidamli; masalan, Apple Face ID ~1/1 000 000 FAR ni eʼlon qiladi |
| **Infraqizil** | IQ tasvir | qorongʻida ishlaydi, ekrandagi rasmni farqlaydi |

**Tirik ekanini aniqlash** (liveness, PAD — presentation attack detection): passiv (tekstura, aks, chuqurlik tahlili) yoki faol (koʻzni qisish, boshni burish). Zamonaviy xavf — **deepfake** videolar va maskalar; shuning uchun masofaviy identifikatsiyada (bank hisobini onlayn ochish) sertifikatlangan PAD talab qilinadi.

**Afzalliklari**: kontaktsiz, tez, qulay. **Cheklovlari**: yorugʻlik, burchak, yosh oʻzgarishi, egizaklar; niqob va koʻzoynak; xato ehtimoli guruhlar orasida farq qilishi mumkin (NIST FRVT/FRTE sinovlari demografik farqlarni qayd etgan).

## Maxfiylik, adolatlilik va qonun

- **Rozilik va maqsad**: telefonni ochish (foydalanuvchi oʻzi tanlaydi, shablon qurilmada) va jamoat joylarida ommaviy kuzatuv (roziliksiz) — butunlay boshqa masalalar.
- **Saqlash**: yuz shablonlari — biometrik shaxsiy maʼlumot; "Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonunga muvofiq asos, himoya va saqlash muddati talab qilinadi. Yevropa Ittifoqida GDPR va AI Act jamoat joylarida real vaqtli identifikatsiyani qatʼiy cheklaydi.
- **Adolatlilik**: tizim turli yosh, jins va teri rangidagi odamlarda bir xil aniqlikda ishlashi sinab koʻrilishi kerak.
- **Inson nazorati**: muhim qarorlar (masalan, hibsga olish) faqat avtomatik moslikka asoslanmasligi kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **Kiberjinoyatni "faqat texnik muammo" deb hisoblash.** Uning sabablari iqtisodiy, ijtimoiy va huquqiy ham.
- **Jabrlanganda murojaat qilmaslik.** Tez xabar berish (bank, huquqni muhofaza qilish organlari) pulni qaytarish va boshqalarni himoya qilish imkonini oshiradi.
- **2D yuz tanishni yagona himoya sifatida ishlatish.** Tirik ekanini aniqlashsiz u rasm bilan aldanadi.
- **Yuz tasvirlarini roziliksiz yigʻish.** Bu qonun va axloq talablarini buzadi.
- **Avtomatik moslikni mutlaq haqiqat deb qabul qilish.** Har qanday biometrik tizimda xato ehtimoli bor.
:::

:::tip
Referat uchun ishonchli statistikani FBI IC3 yillik hisobotlari, ENISA Threat Landscape va Oʻzbekiston rasmiy manbalaridan oling; yuz tanish aniqligi boʻyicha — NIST FRTE (Face Recognition Technology Evaluation) natijalaridan.
:::

## Xulosa

- Kiberjinoyat tarqalishining sabablari: past xarajat va yuqori daromad, anonimlik, "xizmat sifatida jinoyat", tez raqamlashuv, zaif himoya, inson omili, tergov qiyinligi.
- Jinoyat uchburchagi: imkoniyat va oqlashni kamaytirish — amaliy oldini olish yoʻli.
- Yuz autentifikatsiyasi: aniqlash, tekislash, embedding, chegara bilan solishtirish.
- 3D va IQ usullar va tirik ekanini aniqlash rasm, video va deepfake ga qarshi zarur.
- Maxfiylik, rozilik, adolatlilik va inson nazorati — yuz tanishni qoʻllashning majburiy shartlari.

## Oʻzingizni tekshiring

:::check
Q: "Xizmat sifatida jinoyat" kiberjinoyatchilikning tarqalishiga qanday taʼsir qiladi?
A: Tayyor vositalar va xizmatlar sotilgani uchun texnik bilimi past odamlar ham jinoyatga qoʻshila oladi.
Q: Jinoyat uchburchagining qaysi tomonlariga tashkilot taʼsir koʻrsata oladi?
A: Asosan imkoniyat (texnik va tashkiliy himoya) va oqlash (xabardorlik, jazoning muqarrarligi) ga.
Q: 2D yuz tanishning asosiy zaifligi nima?
A: Rasm yoki videoga aldanishi mumkin; tirik ekanini aniqlash talab qilinadi.
Q: Yuz tanish tizimida "embedding" nima?
A: Neyron tarmoq yuz tasviridan hosil qilgan sonli vektor; solishtirish shu vektorlar orasidagi masofa boʻyicha bajariladi.
:::

:::link https://www.ic3.gov/
FBI Internet Crime Complaint Center (IC3)
Internet jinoyatlari boʻyicha yillik hisobotlar va statistika (ingliz tilida).
:::
