---
title: Axborotni ruxsatsiz foydalanishlardan himoyalash. Ijtimoiy injeneriyada hujumlar tahlili
summary: Ruxsatsiz foydalanishdan himoyaning qatlamli tizimi — identifikatsiya va autentifikatsiya, foydalanishni boshqarish, shifrlash, tarmoq va fizik himoya, kuzatuv va javob; ijtimoiy muhandislik tushunchasi va psixologik taʼsir usullari (Chaldini tamoyillari); hujum turlari — fishing, nishonli fishing, vishing, smishing, bahona (pretexting), oʻlja, biznes pochtasini buzish, deepfake; real hodisalar tahlili — Twitter (2020), MGM Resorts (2023), Arup deepfake videoqoʻngʻirogʻi (2024); tashkiliy himoya choralari.
---

## Nima uchun bu mavzu muhim?

Texnik himoya qanchalik kuchli boʻlsa, hujumchilar shunchalik **odamlarga** murojaat qiladi: parolni buzishdan koʻra uni xodimdan soʻrab olish osonroq. Verizon DBIR hisobotlariga koʻra, maʼlumot sizishining katta qismida inson omili — xato, aldov yoki oʻgʻirlangan hisob maʼlumotlari — ishtirok etadi. Shu sababli ruxsatsiz foydalanishdan himoya texnik choralar bilan birga odamlar va jarayonlarni ham qamrab olishi kerak.

:::note
**Bu mustaqil ishdan soʻng siz:**

- ruxsatsiz foydalanishdan himoyaning qatlamlarini ayta olasiz;
- ijtimoiy muhandislikda qoʻllanadigan psixologik taʼsir usullarini tushuntira olasiz;
- ijtimoiy muhandislik hujumlarining asosiy turlarini taniy olasiz;
- real hodisalarni tahlil qilib, ularni toʻxtatishi mumkin boʻlgan choralarni aniqlay olasiz.
:::

## Ruxsatsiz foydalanishdan himoya qatlamlari

| Qatlam | Choralar | Batafsil |
|---|---|---|
| Identifikatsiya va autentifikatsiya | noyob hisoblar, kuchli parollar, MFA, FIDO2 | M10, A10 |
| Foydalanishni boshqarish | eng kam imtiyoz, rollar, vazifalarni ajratish, huquqlarni muntazam qayta koʻrib chiqish | M11, A11, MT17 |
| Maʼlumotni himoyalash | shifrlash (saqlashda va uzatishda), DLP | M6, M14, MT10 |
| Tarmoq | segmentlash, xavfsizlik devori, VPN, nol ishonch | M14 |
| Fizik | kirish nazorati, toza stol, qulflangan ekran | M12 |
| Kuzatuv va javob | jurnallar, SIEM, hodisaga javob rejasi | MT7 |
| **Odamlar va jarayonlar** | xabardorlik, tasdiqlash tartiblari, xabar berish madaniyati | ushbu mavzu, MT16 |

## Ijtimoiy muhandislik

**Ijtimoiy muhandislik** — odamni aldab, u oʻz xohishi bilan maxfiy maʼlumot berishi yoki xavfsizlikni buzuvchi harakat qilishiga undash. U texnik zaiflikdan emas, inson psixologiyasidan foydalanadi. Psixolog R. Chaldini taʼsir tamoyillari bu yerda ham ishlaydi:

| Tamoyil | Hujumda qanday koʻrinadi |
|---|---|
| **Obroʻ** (authority) | "rahbar", "bank xavfsizlik xizmati", "IT boʻlimi" nomidan murojaat |
| **Shoshilinchlik** (urgency / scarcity) | "hisobingiz 1 soatda bloklanadi", "bugun toʻlanmasa jarima" |
| **Ijtimoiy isbot** | "boshqa xodimlar allaqachon tasdiqladi" |
| **Yoqtirish** | samimiy suhbat, umumiy tanishlar, maqtov |
| **Oʻzaro yordam** (reciprocity) | avval kichik yordam yoki sovgʻa, keyin soʻrov |
| **Majburiyat va izchillik** | kichik roziliklardan katta soʻrovga oʻtish |

**Asosiy hujum turlari**:

| Tur | Tavsif |
|---|---|
| **Fishing** | ommaviy soxta xatlar: soxta sayt, ilova yoki havola |
| **Nishonli fishing** (spear phishing) | aniq bir shaxsga moslashtirilgan xat, ochiq maʼlumotlardan foydalanib |
| **Vishing** | telefon orqali aldash (soʻnggi yillarda ovozni soxtalashtirish bilan) |
| **Smishing** | SMS va messenjer xabarlari orqali |
| **Bahona** (pretexting) | ishonchli vaziyat toʻqib chiqarish: "auditor", "kuryer", "yangi xodim" |
| **Oʻlja** (baiting) | qiziqarli fleshka yoki "bepul" yuklab olish (A12) |
| **Biznes pochtasini buzish** (BEC) | rahbar yoki yetkazib beruvchi nomidan toʻlov rekvizitlarini almashtirish |
| **Deepfake** | sunʼiy intellekt yordamida soxta ovoz yoki video |

## Real hodisalar tahlili

:::case
**Keys 1. Twitter (2020-yil iyul).** Hujumchilar xodimlarga telefon qilib, ichki IT yordam xizmati nomidan soxta saytga kirish maʼlumotlarini kiritishga undagan. Ichki boshqaruv paneli orqali mashhur shaxslar hisoblaridan kriptovalyuta firibgarligi xabarlari eʼlon qilingan (A3).

**Tahlil.** Usul — vishing va bahona; tamoyil — obroʻ va shoshilinchlik. Zaifliklar: ichki vositaga keng kirish, telefon orqali soʻrovni tasdiqlash tartibi yoʻqligi. Choralar: ichki vositalarga eng kam imtiyoz, fishingga chidamli MFA (apparat kalitlar), muhim amallar uchun ikki kishilik tasdiq.
:::

:::case
**Keys 2. MGM Resorts (AQSH, 2023-yil sentabr).** Ommaviy axborot vositalari xabariga koʻra, hujumchilar ijtimoiy tarmoqlardan xodim haqida maʼlumot topib, IT yordam xizmatiga uning nomidan qoʻngʻiroq qilgan va hisobni tiklashga erishgan. Keyingi hujum mehmonxona va kazino tizimlarini bir necha kun ishdan chiqargan; kompaniya zararni taxminan 100 million dollar deb baholagan.

**Tahlil.** Usul — vishing, oldindan ochiq manbalardan razvedka; tamoyil — obroʻ, yordam soʻrash. Zaiflik — yordam xizmatida shaxsni ishonchli tekshirish tartibining yoʻqligi. Choralar: parol yoki MFA ni tiklashda qatʼiy tekshiruv (qayta qoʻngʻiroq rasmiy raqamga, rahbar tasdigʻi, shaxsan kelish), imtiyozli hisoblar uchun alohida tartib.
:::

:::case
**Keys 3. Deepfake videoqoʻngʻiroq (Gonkong, 2024-yil boshi).** Muhandislik kompaniyasi Arup ning moliya xodimi videokonferensiyada "moliya direktori" va boshqa "hamkasblar" bilan suhbatlashgan — ularning barchasi sunʼiy intellekt yordamida yaratilgan soxta tasvirlar boʻlgan. Xodim bir necha tranzaksiyada taxminan 25 million AQSH dollarini oʻtkazgan.

**Tahlil.** Usul — BEC va deepfake; tamoyil — obroʻ va ijtimoiy isbot (konferensiyada "koʻp odam" bor). Zaiflik — katta toʻlovlar uchun mustaqil kanal orqali tasdiqlash tartibi yoʻqligi. Choralar: katta yoki gʻayrioddiy toʻlovni boshqa, oldindan maʼlum kanal orqali tasdiqlash; ikki kishilik imzo; "maxfiy va shoshilinch toʻlov" — avtomatik shubha belgisi.
:::

**Umumiy saboq**: uchala holatda ham texnik himoya mavjud edi, lekin **jarayon** — mustaqil kanal orqali tasdiqlash — yoʻq yoki ishlamagan. Ijtimoiy muhandislikka qarshi eng samarali chora — muhim harakatlarni bitta suhbat yoki xatga tayanmasdan tekshirish tartibi.

## Tashkiliy himoya choralari

- **Tasdiqlash tartiblari**: toʻlov rekvizitlarini oʻzgartirish, parolni tiklash, maxfiy maʼlumot berish — faqat mustaqil kanal orqali qayta tekshirilgandan keyin.
- **Fishingga chidamli autentifikatsiya**: FIDO2/passkey va apparat kalitlar (M10).
- **Xabardorlik dasturi**: muntazam qisqa treninglar, ruxsat etilgan fishing simulyatsiyalari, real misollar (MT16).
- **Xabar berish madaniyati**: shubhali xatni bir tugma bilan xabar qilish; xato qilgan xodimni jazolash emas, tez xabar bergani uchun minnatdorchilik.
- **Ochiq maʼlumotlarni cheklash**: xodimlar ijtimoiy tarmoqlarda ish tuzilmasi va jarayonlar haqida ortiqcha maʼlumot bermasligi.
- **Texnik yordam**: pochta filtri (SPF, DKIM, DMARC), tashqi xatlarni belgilash, soxta domenlarni kuzatish.

:::warning
**Koʻp uchraydigan xatolar**

- **"Men aldanmayman".** Ijtimoiy muhandislik charchoq, shoshilish va ishonchdan foydalanadi — tajribali mutaxassislar ham aldanadi.
- **Xatdagi raqamga qayta qoʻngʻiroq qilib tekshirish.** Faqat oldindan maʼlum, rasmiy raqam yoki kanal orqali tekshiring.
- **Videoqoʻngʻiroqni mutlaq dalil deb hisoblash.** Deepfake texnologiyalari ovoz va tasvirni soxtalashtira oladi.
- **Aldangan xodimni jazolash.** Bu keyingi hodisalarni yashirishga olib keladi.
- **Faqat texnik choralarga tayanish.** Jarayon va xabardorliksiz texnik himoya chetlab oʻtiladi.
:::

:::tip
Oʻzingiz uchun "toʻxta — tekshir — xabar ber" qoidasini oʻrnating: shoshilinch va gʻayrioddiy soʻrov kelganda harakatni toʻxtating, soʻrovni mustaqil kanal orqali tekshiring va shubhali boʻlsa, xavfsizlik xizmatiga xabar bering.
:::

## Xulosa

- Ruxsatsiz foydalanishdan himoya qatlamli: autentifikatsiya, foydalanishni boshqarish, shifrlash, tarmoq, fizik himoya, kuzatuv va odamlar.
- Ijtimoiy muhandislik psixologik tamoyillardan foydalanadi: obroʻ, shoshilinchlik, ijtimoiy isbot, yoqtirish, oʻzaro yordam.
- Turlari: fishing, nishonli fishing, vishing, smishing, bahona, oʻlja, BEC, deepfake.
- Twitter, MGM va Arup keyslari: asosiy zaiflik — mustaqil kanal orqali tasdiqlash tartibining yoʻqligi.
- Choralar: tasdiqlash tartiblari, fishingga chidamli MFA, xabardorlik, xabar berish madaniyati, pochta filtrlari.

## Oʻzingizni tekshiring

:::check
Q: Ijtimoiy muhandislikda "shoshilinchlik" tamoyili qanday ishlatiladi?
A: Qurbonni oʻylash va tekshirish imkoniyatidan mahrum qilish uchun vaqt bosimi yaratiladi.
Q: Biznes pochtasini buzish (BEC) hujumining asosiy maqsadi nima?
A: Rahbar yoki yetkazib beruvchi nomidan soxta toʻlov amalga oshirish yoki rekvizitlarni almashtirish.
Q: Arup hodisasidagi zararni qaysi chora toʻxtatishi mumkin edi?
A: Katta toʻlovni oldindan maʼlum mustaqil kanal orqali tasdiqlash va ikki kishilik imzo.
Q: Nima uchun aldangan xodimni jazolash xavfli?
A: Xodimlar keyingi xatolarini yashiradi, hodisalar kech aniqlanadi.
:::

:::link https://www.cisa.gov/secure-our-world/recognize-and-report-phishing
CISA: Recognize and Report Phishing
Fishingni tanish va xabar berish boʻyicha amaliy tavsiyalar (ingliz tilida).
:::
