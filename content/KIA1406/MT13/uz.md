---
title: Kiberxavfsizlik sohasida karyera yoʻllarini oʻrganish. Axborot xavfsizligi sohasida ish boshlash
summary: Kiberxavfsizlik mehnat bozori va mutaxassislar tanqisligi; asosiy yoʻnalishlar — himoya va monitoring (SOC), hodisalarga javob va raqamli kriminalistika, xavfsizlik muhandisligi va arxitekturasi, ilovalar xavfsizligi, ruxsat etilgan sinov (pentest), boshqaruv, risk va muvofiqlik (GRC), kriptografiya va tadqiqot; NICE ramkasi; boshlangʻich lavozimlar; zarur bilim va koʻnikmalar; portfolio va qonuniy amaliyot maydonchalari; Oʻzbekistonda imkoniyatlar; shaxsiy rivojlanish rejasi.
---

## Nima uchun bu mavzu muhim?

Deyarli har bir tashkilot raqamlashmoqda — banklar, davlat xizmatlari, telekom, sanoat — va ularning barchasiga axborotni himoya qiluvchi mutaxassislar kerak. Xalqaro tadqiqotlar (masalan, ISC2 ning yillik hisobotlari) dunyoda millionlab kiberxavfsizlik mutaxassisi yetishmasligini qayd etadi. Lekin "kiberxavfsizlik" — bitta kasb emas, oʻnlab turli yoʻnalishlar: biri jurnallarni tahlil qiladi, biri kod tekshiradi, biri siyosat va auditlar bilan ishlaydi. Oʻz yoʻlingizni erta tanlash oʻqishni maqsadli qiladi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- kiberxavfsizlikning asosiy yoʻnalishlari va lavozimlarini ayta olasiz;
- har bir yoʻnalish uchun zarur bilim va koʻnikmalarni bilasiz;
- boshlangʻich lavozimga tayyorlanish yoʻlini rejalashtira olasiz;
- portfolio va amaliy tajribani qonuniy yoʻllar bilan toʻplay olasiz;
- shaxsiy rivojlanish rejasini tuza olasiz.
:::

## Asosiy yoʻnalishlar

| Yoʻnalish | Nima bilan shugʻullanadi | Tipik lavozimlar |
|---|---|---|
| **Monitoring va himoya** (Blue team, SOC) | jurnallar va ogohlantirishlarni kuzatish, hodisalarni aniqlash | SOC tahlilchisi (L1–L3), tahdidlar ovchisi |
| **Hodisalarga javob va kriminalistika** | hodisani cheklash, sabablarini aniqlash, dalillarni yigʻish | incident responder, raqamli kriminalist |
| **Xavfsizlik muhandisligi va arxitekturasi** | himoya vositalarini loyihalash va sozlash: devorlar, IAM, bulut | security engineer, cloud security engineer, arxitektor |
| **Ilovalar xavfsizligi** (AppSec, DevSecOps) | xavfsiz dasturlash, kodni tekshirish, zaifliklarni tuzatish | AppSec muhandisi, xavfsiz dasturchi |
| **Ruxsat etilgan sinov** (pentest, Red team) | shartnoma va ruxsat asosida himoyani sinash, hisobot | pentester, xavfsizlik auditori |
| **Boshqaruv, risk va muvofiqlik** (GRC) | siyosatlar, risklarni baholash, standartlar (ISO 27001), audit | GRC tahlilchisi, CISO |
| **Kriptografiya va tadqiqot** | algoritm va protokollarni loyihalash va tahlil qilish | kriptograf, tadqiqotchi |
| **Taʼlim va xabardorlik** | xodimlarni oʻqitish, treninglar | xavfsizlik boʻyicha trener |

**NICE ramkasi** (AQSH NIST ning Workforce Framework for Cybersecurity) bu sohani vazifalar, bilimlar va koʻnikmalar boʻyicha tavsiflaydi — undan oʻzingizga mos lavozimlar va ularga kerakli bilimlarni topishda foydalanish mumkin.

## Boshlangʻich lavozimlar va yoʻl xaritasi

Koʻpchilik mutaxassislar sohaga IT ning boshqa tarmogʻi orqali kiradi: tizim administratori, tarmoq muhandisi, dasturchi yoki texnik yordam xodimi sifatida. Bu yaxshi yoʻl: himoya qilish uchun avval tizim qanday ishlashini bilish kerak.

| Bosqich | Nima oʻrganiladi | Natija |
|---|---|---|
| 1. IT asoslari | operatsion tizimlar (Windows, Linux), tarmoqlar (TCP/IP, DNS), dasturlash (Python) | tizimlarni mustaqil sozlay olish |
| 2. Xavfsizlik asoslari | ushbu fan: risklar, kriptografiya, autentifikatsiya, foydalanishni boshqarish, tarmoq himoyasi | asosiy tushunchalar va vositalar |
| 3. Yoʻnalish tanlash | SOC, bulut, AppSec yoki GRC boʻyicha chuqurlashtirilgan kurslar | birinchi ixtisoslik |
| 4. Amaliyot | laboratoriyalar, musobaqalar, amaliyot (stajirovka), oʻz loyihalaringiz | portfolio |
| 5. Birinchi ish | junior lavozim, mentor bilan ishlash | real tajriba |

## Zarur koʻnikmalar

- **Texnik**: Linux va Windows boshqaruvi, tarmoqlar, skript yozish (Python, PowerShell, Bash), bulut asoslari, jurnallarni tahlil qilish, asosiy kriptografiya.
- **Tahliliy**: muammoni qismlarga ajratish, faraz tuzish va uni dalillar bilan tekshirish.
- **Kommunikatsiya**: texnik boʻlmagan rahbarga riskni tushunarli tushuntirish, aniq hisobot yozish (MT2).
- **Ingliz tili**: standartlar, hujjatlar va hamjamiyatning asosiy qismi ingliz tilida.
- **Axloq**: maxfiylikni saqlash, faqat ruxsat etilgan harakatlar, qonunga rioya qilish.
- **Doimiy oʻrganish**: texnologiyalar va tahdidlar har yili oʻzgaradi.

## Portfolio va qonuniy amaliyot

Ish beruvchi uchun "nimani bilaman" emas, "nimani qildim" muhimroq. Portfolio uchun:

- **Uy laboratoriyasi**: virtual mashinalarda kichik tarmoq, domen, jurnallarni markazlashgan yigʻish; nima qilganingizni yozib boring.
- **Taʼlim platformalari**: TryHackMe, Hack The Box Academy, Cisco Networking Academy, Microsoft Learn — ataylab yaratilgan, foydalanish ruxsat etilgan laboratoriyalar.
- **CTF musobaqalari** (Capture The Flag): jamoaviy masalalar — kriptografiya, kriminalistika, tarmoq tahlili.
- **Ochiq kodli loyihalar** va GitHub: oʻzingiz yozgan xavfsizlik skriptlari, hujjatlar.
- **Bloglar va hisobotlar**: oʻrganganlaringizni tushuntirib yozish — bu kommunikatsiya koʻnikmasini ham koʻrsatadi.

Esda tuting: begona tizimda "mashq" qilish — jinoyat (M3). Faqat ruxsat etilgan maydonchalardan foydalaning.

## Oʻzbekistonda imkoniyatlar

Banklar va toʻlov tizimlari, telekommunikatsiya kompaniyalari, davlat organlari va ularning axborot xavfsizligi xizmatlari, IT-kompaniyalar va IT Park rezidentlari, shuningdek xalqaro kompaniyalarga masofaviy ish — asosiy ish beruvchilar. "Kiberxavfsizlik toʻgʻrisida"gi Qonun (2022) muhim axborot infratuzilmasi obyektlariga xavfsizlik talablarini qoʻygani tufayli bunday mutaxassislarga talab oshib bormoqda. Universitet amaliyotlari, IT Park dasturlari va CTF musobaqalari — tajriba toʻplashning yaxshi yoʻllari.

## Shaxsiy rivojlanish rejasi

Oʻzingiz uchun bir sahifalik reja tuzing:

1. Qaysi 1–2 yoʻnalish sizga qiziq va nima uchun?
2. Bu yoʻnalishdagi 3 ta real vakansiyani toping (mahalliy va xalqaro) — qanday talablar takrorlanadi?
3. Sizda allaqachon bor va yetishmayotgan koʻnikmalar roʻyxati.
4. Keyingi 6 oy uchun aniq qadamlar: kurslar, laboratoriyalar, bitta portfolio loyihasi.
5. Natijani qanday oʻlchaysiz (masalan, tugallangan laboratoriyalar soni, yozilgan hisobotlar)?

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat "xaker" boʻlishni orzu qilish.** Pentest — koʻp yoʻnalishlardan biri; ish oʻrinlarining katta qismi himoya, muhandislik va GRC da.
- **IT asoslarini oʻtkazib yuborish.** Tarmoq va OT ni bilmasdan xavfsizlikni chuqur tushunib boʻlmaydi.
- **Faqat sertifikatlarga tayanish.** Amaliy tajribasiz sertifikat suhbatda tezda koʻrinib qoladi.
- **Ruxsatsiz "mashq".** Jinoiy javobgarlik va soha uchun obroʻni yoʻqotish.
- **Kommunikatsiya koʻnikmalarini eʼtiborsiz qoldirish.** Topilgan muammoni tushuntira olmasangiz, u tuzatilmaydi.
:::

:::tip
LinkedIn yoki shunga oʻxshash tarmoqlarda siz qiziqqan lavozimdagi 5 nafar mutaxassis profilini koʻrib chiqing: ular qaysi lavozimdan boshlagan, qanday koʻnikmalarni koʻrsatgan? Bu eng realistik yoʻl xaritasi.
:::

## Xulosa

- Kiberxavfsizlik — koʻp yoʻnalishli soha: SOC, hodisalarga javob, muhandislik, AppSec, pentest, GRC, tadqiqot.
- Yoʻl: IT asoslari → xavfsizlik asoslari → yoʻnalish → amaliyot → birinchi ish.
- Texnik koʻnikmalar bilan birga tahliliy fikrlash, kommunikatsiya, ingliz tili va axloq muhim.
- Portfolio — uy laboratoriyasi, taʼlim platformalari, CTF, ochiq loyihalar; faqat qonuniy maydonchalar.
- Oʻzbekistonda talab oshmoqda: banklar, telekom, davlat organlari, IT-kompaniyalar.

## Oʻzingizni tekshiring

:::check
Q: SOC tahlilchisi asosan nima bilan shugʻullanadi?
A: Jurnallar va ogohlantirishlarni kuzatib, xavfsizlik hodisalarini aniqlaydi va ularni tahlil qiladi.
Q: GRC yoʻnalishi nimani anglatadi?
A: Boshqaruv, risk va muvofiqlik: siyosatlar, risklarni baholash, standartlar va audit bilan ishlash.
Q: Amaliy tajribani qonuniy yoʻl bilan qanday toʻplash mumkin?
A: Uy laboratoriyasi, ruxsat etilgan taʼlim platformalari, CTF musobaqalari va ochiq loyihalar orqali.
Q: Nima uchun koʻp mutaxassislar sohaga IT ning boshqa tarmogʻi orqali kiradi?
A: Himoya qilish uchun avval tizimlar, tarmoqlar va dasturlar qanday ishlashini bilish kerak.
:::

:::link https://niccs.cisa.gov/workforce-development/nice-framework
NICE Framework
Kiberxavfsizlik lavozimlari, vazifalari va koʻnikmalarining tavsifi (ingliz tilida).
:::
