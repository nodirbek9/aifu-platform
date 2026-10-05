---
title: Kiberjinoyatlar boʻyicha real voqealarni tahlil qilish (case study)
summary: Real kiberhodisalarni tahlil qilish usuli — hujum zanjiri (Cyber Kill Chain), boshlangʻich kirish, rivojlanish, oqibat, javob choralari, huquqiy jihatlar; toʻrtta mashhur voqea — WannaCry (2017), Equifax (2017), Colonial Pipeline (2021), Twitter ijtimoiy muhandislik hujumi (2020); xulosalar va darslar; mustaqil tahlil topshirigʻi.
---

## Nima uchun bu mavzu muhim?

Kiberxavfsizlikning eng yaxshi darsliklari — boshqalarning xatolari. Har bir yirik hodisadan keyin tergov hisobotlari eʼlon qilinadi: hujumchi qanday kirdi, nega uni payqashmadi, qancha zarar boʻldi va keyin nima oʻzgardi. Bu hisobotlarni tahlil qilish mutaxassisga real hujumlar qanday "koʻrinishini" va qaysi oddiy choralar katta falokatning oldini olishi mumkinligini oʻrgatadi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- hujum bosqichlarini Cyber Kill Chain modeli boʻyicha ajrata olasiz;
- real hodisada boshlangʻich kirish usuli, zaiflik va oqibatlarni aniqlay olasiz;
- hodisaga javob choralarini va huquqiy oqibatlarni tahlil qila olasiz;
- har bir hodisadan tashkilot uchun amaliy xulosalar chiqara olasiz.
:::

## Tahlil modeli: Cyber Kill Chain

Lockheed Martin taklif qilgan model hujumni yetti bosqichga ajratadi; himoyachi har bir bosqichda zanjirni "uzishi" mumkin:

| Bosqich | Hujumchi nima qiladi | Himoya imkoniyati |
|---|---|---|
| 1. Razvedka | nishon haqida maʼlumot yigʻadi (xodimlar, texnologiyalar) | ochiq maʼlumotlarni cheklash |
| 2. Qurollanish | zararli fayl yoki ekspluatatsiya tayyorlaydi | — |
| 3. Yetkazish | xat, sayt, USB orqali yuboradi | pochta filtri, veb-filtr |
| 4. Ekspluatatsiya | zaiflikdan foydalanadi | yangilanishlar, EDR |
| 5. Oʻrnatish | tizimda qoladi (backdoor) | ilovalarni oq roʻyxat bilan cheklash |
| 6. Boshqaruv (C2) | tashqi server bilan bogʻlanadi | chiquvchi trafikni filtrlash, IDS |
| 7. Maqsadga erishish | maʼlumot oʻgʻirlash, shifrlash, sabotaj | segmentlash, DLP, zaxira nusxa |

## Keyslar

:::case
**Keys 1. WannaCry (2017-yil may).** Shifrlovchi qurt bir necha kunda 150 dan ortiq davlatdagi 200 mingdan ortiq kompyuterni zararladi. U Windows dagi SMBv1 protokolining zaifligidan (EternalBlue ekspluatatsiyasi) foydalanib, tarmoq orqali oʻz-oʻzidan tarqaldi. Microsoft tuzatishni hodisadan ikki oy oldin chiqargan edi. Buyuk Britaniya sogʻliqni saqlash tizimida operatsiyalar bekor qilindi, zavodlar toʻxtadi. Tarqalish tasodifan "oʻchirish kaliti" (kill switch) domeni roʻyxatdan oʻtkazilgach sekinlashdi.

**Tahlil.** Boshlangʻich kirish — Internetga ochiq 445-port; zaiflik — yangilanmagan tizimlar va eskirgan SMBv1; buzilgan xossa — foydalanish mumkinligi. Darslar: yangilanishlarni tez oʻrnatish (patch management), SMBv1 ni oʻchirish, 445-portni Internetga yopish, tarmoqni segmentlash, oflayn zaxira nusxalar.
:::

:::case
**Keys 2. Equifax (2017).** AQSHdagi yirik kredit byurosida 147 million kishining shaxsiy maʼlumotlari (ismi, ijtimoiy sugʻurta raqami, tugʻilgan sanasi) oʻgʻirlandi. Hujumchilar veb-ilovada ishlatilgan Apache Struts freymvorkining maʼlum zaifligidan foydalandi — tuzatish ikki oy oldin chiqqan, lekin oʻrnatilmagan edi. Hujumchilar ~76 kun sezilmay qoldi: trafikni tekshiruvchi qurilmaning sertifikati muddati oʻtgan va u ishlamagan edi.

**Tahlil.** Boshlangʻich kirish — zaif veb-ilova; zaifliklar — yangilanmagan komponent, ishlamaydigan monitoring, ichki tarmoqda segmentlash va shifrlash yetishmasligi; buzilgan xossa — konfidensiallik. Oqibatlar: yuzlab million dollarlik kompensatsiya va jarimalar, rahbariyat isteʼfosi. Darslar: dastur komponentlari inventari, tuzatishlarni nazorat qilish, monitoring vositalarining ishlashini tekshirish.
:::

:::case
**Keys 3. Colonial Pipeline (2021-yil may).** AQSH sharqiy qirgʻogʻiga yoqilgʻi yetkazuvchi quvur kompaniyasi shifrlovchi virus hujumidan keyin quvurni bir necha kunga toʻxtatdi; yoqilgʻi tanqisligi va vahima yuzaga keldi. Kompaniya ~4,4 mln dollar toʻlov toʻladi (keyinchalik bir qismi qaytarildi). Hujumchilar VPN ga sizib chiqqan parol bilan kirgan; hisobda koʻp omilli autentifikatsiya yoqilmagan edi.

**Tahlil.** Boshlangʻich kirish — oʻgʻirlangan parol bilan VPN; zaiflik — MFA yoʻqligi, ishlatilmayotgan hisobning faol qolgani; buzilgan xossa — foydalanish mumkinligi (maʼlumot ham oʻgʻirlangan). Darslar: masofaviy kirishda majburiy MFA, keraksiz hisoblarni oʻchirish, IT va texnologik (OT) tarmoqlarni ajratish, hodisaga javob rejasini oldindan tayyorlash.
:::

:::case
**Keys 4. Twitter (2020-yil iyul).** Hujumchilar Twitter xodimlariga texnik yordam xizmati nomidan telefon qilib (vishing), ularni soxta saytda ichki tizim loginlarini kiritishga undadi. Ichki boshqaruv paneli orqali mashhur shaxslarning (siyosatchilar, kompaniyalar rahbarlari) hisoblaridan "bitkoin yuboring — ikki baravar qaytaramiz" degan xabarlar eʼlon qilindi. Bir necha soatda 100 ming dollardan ortiq yigʻildi. Asosiy ishtirokchilar qoʻlga olindi.

**Tahlil.** Boshlangʻich kirish — ijtimoiy muhandislik; zaifliklar — ichki vositalarga keng kirish huquqi, xodimlarning bunday hujumlarga tayyor emasligi; buzilgan xossalar — yaxlitlik va autentiklik (soxta xabarlar), obroʻ. Darslar: ichki vositalarga eng kam imtiyoz, muhim amallar uchun ikki kishilik tasdiq, apparat kalitlar bilan MFA, ijtimoiy muhandislikka qarshi treninglar.
:::

## Umumiy xulosalar

| Kuzatuv | Qaysi keysda | Arzon chora |
|---|---|---|
| Maʼlum zaiflik oʻz vaqtida yopilmagan | WannaCry, Equifax | tuzatishlarni boshqarish jarayoni |
| Bitta parol hamma narsani ochgan | Colonial Pipeline | MFA |
| Odamlar aldangan | Twitter | trening, apparat kalitlar |
| Hujum uzoq sezilmagan | Equifax | monitoringni sinash, jurnallarni tahlil qilish |
| Tiklash qiyin boʻlgan | WannaCry, Colonial | oflayn zaxira va tiklash rejasi |

## Mustaqil tahlil topshirigʻi

Quyidagilardan birini tanlab, yuqoridagi shablon (Kill Chain bosqichlari, zaiflik, buzilgan xossa, oqibatlar, huquqiy jihatlar, darslar) boʻyicha 2–3 betlik tahlil yozing: NotPetya (2017), SolarWinds (2020), Log4Shell zaifligi (2021), MOVEit hujumlari (2023). Kamida uchta ishonchli manbaga (tergov hisobotlari, CISA, yirik OAV) havola bering.

:::warning
**Koʻp uchraydigan xatolar**

- **Hodisani faqat "xakerlar kuchli edi" deb tushuntirish.** Deyarli har bir keysda oddiy, maʼlum va arzon choralar yetishmagan.
- **Manbasiz yoki bitta maqolaga tayanib yozish.** Raqamlar va faktlarni rasmiy hisobotlar bilan tekshiring.
- **Faqat texnik tomonni koʻrish.** Huquqiy, moliyaviy va obroʻga oid oqibatlarni ham tahlil qiling.
- **Toʻlov toʻlashni yechim deb hisoblash.** Toʻlov maʼlumot qaytishini kafolatlamaydi va jinoyatchilarni ragʻbatlantiradi.
:::

:::tip
Har bir keys uchun bitta savolga javob bering: "Agar men oʻsha tashkilotda xavfsizlik boʻyicha masʼul boʻlganimda, hodisadan bir yil oldin qaysi bitta chorani joriy etgan boʻlardim?" Javobingizni narxi va samarasi bilan asoslang.
:::

## Xulosa

- Cyber Kill Chain hujumni yetti bosqichga ajratadi; himoya istalgan bosqichda zanjirni uzishi mumkin.
- Yirik hodisalarning koʻpchiligi maʼlum zaifliklar, MFA yoʻqligi, ijtimoiy muhandislik va sust monitoringdan boshlangan.
- Har bir keysdan aniq, amaliy va arzon darslar chiqarish — tahlilning asosiy maqsadi.

## Oʻzingizni tekshiring

:::check
Q: WannaCry qanday zaiflik orqali tarqaldi va qaysi chora uni toʻxtatishi mumkin edi?
A: Windows SMBv1 zaifligi (EternalBlue); oʻz vaqtida oʻrnatilgan tuzatish, SMBv1 ni oʻchirish va 445-portni yopish.
Q: Colonial Pipeline hujumida hujumchilar qanday kirgan?
A: Sizib chiqqan parol bilan MFA yoqilmagan VPN hisobi orqali.
Q: Twitter hodisasida qanday hujum turi ishlatilgan?
A: Ijtimoiy muhandislik — telefon orqali aldash (vishing).
Q: Equifax da hujum nima uchun uzoq sezilmadi?
A: Trafikni tekshiruvchi qurilmaning sertifikati muddati oʻtgan va u ishlamagan edi.
Q: Cyber Kill Chain ning oxirgi bosqichi qanday ataladi?
A: Maqsadga erishish (Actions on Objectives) — maʼlumot oʻgʻirlash, shifrlash yoki sabotaj.
:::

:::link https://www.cisa.gov/news-events/cybersecurity-advisories
CISA Cybersecurity Advisories
AQSH kiberxavfsizlik agentligining hodisa va zaifliklar boʻyicha ogohlantirishlari (ingliz tilida).
:::
