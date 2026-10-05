---
title: Fizik xavfsizlik boʻyicha real misollarni tahlil qilish (keys)
summary: Fizik xavfsizlik hodisalarini tahlil qilish modeli — tahdid turi, buzilgan himoya qatlami, ishlamagan nazorat vazifasi (toʻsiq, toʻxtatish, aniqlash, javob); keyslar — Stuxnet va USB orqali havodan ajratilgan tarmoqqa kirish, AQSH Veteranlar ishlari departamenti noutbukining oʻgʻirlanishi, OVHcloud maʼlumotlar markazidagi yongʻin, Ayova sudi binosidagi fizik pentest, ikkinchi qoʻl disklardagi maʼlumotlar; umumiy xulosalar; oʻz binosini fizik audit qilish topshirigʻi; mavzu boʻyicha test.
---

## Nima uchun bu mavzu muhim?

M12 maʼruzasida fizik tahdidlar va nazorat choralarini nazariy koʻrdik. Real hodisalar shuni koʻrsatadiki, eng katta zararlar murakkab texnologiyalar yetishmasligidan emas, balki **oddiy** narsalardan kelib chiqadi: shifrlanmagan noutbuk, boshqa binoda saqlanmagan zaxira nusxa, ochiq qoldirilgan eshik, ulangan begona fleshka. Bu mashgʻulotda beshta keysni bitta model asosida tahlil qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- fizik xavfsizlik hodisasini tahdid turi, himoya qatlami va nazorat vazifasi boʻyicha tahlil qila olasiz;
- har bir keysda qaysi arzon chora zararni oldini olishi mumkin edi — aniqlay olasiz;
- fizik va axborot xavfsizligi bir-biriga qanday bogʻliqligini tushuntira olasiz;
- oʻz binongiz uchun oddiy fizik audit oʻtkaza olasiz.
:::

## Tahlil modeli

Har bir keysni quyidagi savollar boʻyicha koʻrib chiqamiz:

| Savol | Variantlar |
|---|---|
| Tahdid turi | tabiiy, texnogen, inson tomonidan (tashqi yoki ichki) |
| Qaysi qatlam buzildi | perimetr, bino, xona, qurilma, axborot tashuvchisi |
| Qaysi vazifa ishlamadi | toʻsiq, toʻxtatish, aniqlash, javob |
| Qaysi xossa buzildi | konfidensiallik, yaxlitlik, foydalanish mumkinligi |
| Qanday chora yordam berardi | texnik (shifrlash, qulf, datchik), tashkiliy (siyosat, trening), jismoniy (boshqa joy) |

## Keyslar

:::case
**Keys 1. Stuxnet (2010).** Eronning Natanz shahridagi uran boyitish zavodi tarmogʻi Internetdan **jismonan ajratilgan** (air gap) edi. Shunga qaramay, zararli dastur sentrifugalarni boshqaruvchi Siemens kontrollerlariga yetib bordi: u USB fleshkalar orqali tarqalgan va boshqaruv tizimiga oʻzgartirilgan buyruqlar yuborib, operatorlarga esa "normal" koʻrsatkichlarni koʻrsatgan. Taxminan mingga yaqin sentrifuga ishdan chiqdi.

**Tahlil.** Tahdid — inson tomonidan (maqsadli, davlat darajasidagi); qatlam — axborot tashuvchisi va qurilma; ishlamagan vazifa — toʻxtatish (olib kiriladigan tashuvchilar nazorati) va aniqlash. Buzilgan xossalar — yaxlitlik va foydalanish mumkinligi. Saboq: havo boʻshligʻi — mutlaq himoya emas; olinadigan tashuvchilarni boshqarish (faqat tekshirilgan, roʻyxatdan oʻtgan qurilmalar, avtoishga tushirishni oʻchirish, USB portlarni bloklash) va texnologik jarayon koʻrsatkichlarini mustaqil kuzatish zarur.
:::

:::case
**Keys 2. Veteranlar ishlari departamenti noutbuki (AQSH, 2006).** Xodim uyga olib ketgan noutbuk va tashqi disk uydan oʻgʻirlandi. Unda 26,5 million harbiy xizmatchi va veteranning ismi, ijtimoiy sugʻurta raqami va tugʻilgan sanasi bor edi — **shifrlanmagan** holda. Qurilmalar keyinroq topildi va maʼlumot ishlatilmagani aytildi, lekin xabardor qilish, kredit monitoringi va sud ishlariga katta mablagʻ sarflandi; vazirlik xavfsizlik siyosatini qayta koʻrib chiqdi.

**Tahlil.** Tahdid — inson tomonidan (oddiy oʻgʻirlik, maqsadli emas); qatlam — qurilma ofisdan tashqarida; ishlamagan vazifa — toʻxtatish (shifrlash yoʻq) va siyosat (maxfiy maʼlumotni uyga olib ketishga ruxsat). Buzilgan xossa — konfidensiallik. Saboq: barcha noutbuk va olinadigan disklarda toʻliq disk shifrlash (BitLocker, FileVault); maxfiy maʼlumotni qurilmaga koʻchirishni cheklash; yoʻqotish haqida darhol xabar berish tartibi. Shifrlangan qurilma oʻgʻirlansa — bu faqat qurilma narxi, maʼlumot sizishi emas.
:::

:::case
**Keys 3. OVHcloud maʼlumotlar markazidagi yongʻin (Strasburg, 2021-yil mart).** Yevropaning eng yirik xosting provayderlaridan birining SBG2 binosi yongʻinda butunlay yonib ketdi, qoʻshni SBG1 qisman zarar koʻrdi. Taxminan 3,6 million veb-sayt ishlamay qoldi. Zaxira nusxasi **xuddi shu kampusda** saqlangan yoki umuman boʻlmagan mijozlar maʼlumotlarini butunlay yoʻqotdi. Tergov xulosalariga koʻra yongʻin elektr taʼminoti uskunasidan boshlangan; binoning tabiiy havo bilan sovutish konstruksiyasi olovning tez tarqalishiga yordam bergan.

**Tahlil.** Tahdid — texnogen (yongʻin); qatlam — butun bino; ishlamagan vazifalar — toʻxtatish (yongʻinni cheklash) va javob (avtomatik oʻchirish yetarli boʻlmagan). Buzilgan xossa — foydalanish mumkinligi (va maʼlumotning yoʻqolishi). Saboq: **3-2-1 qoidasi** — 3 nusxa, 2 xil tashuvchi, 1 tasi boshqa joyda (boshqa shahar yoki hududda); bulut provayderida ham zaxira nusxa boshqa hududda boʻlishi kerak; tiklash rejasi muntazam sinab koʻriladi.
:::

:::case
**Keys 4. Ayova sudi binosidagi pentest (AQSH, 2019-yil sentabr).** Shtat sud boshqarmasi bilan shartnoma asosida ishlagan ikki pentester tunda Dallas okrugi sudi binosiga eshik qulfini ochib kirdi va signalizatsiyani ishga tushirdi. Ular politsiyani kutib turib, shartnomani koʻrsatdi, lekin binoga egalik qiluvchi okrug maʼmuriyati bunday sinovga rozilik bermagan edi — pentesterlar hibsga olindi. Keyinchalik ayblovlar olib tashlandi, lekin voqea sohada keng muhokama qilindi.

**Tahlil.** Bu hujum emas, ruxsat etilgan sinov edi — lekin u ikki narsani koʻrsatdi. Birinchidan, bino eshigi ochilgan, ammo **aniqlash** (signalizatsiya) va **javob** (politsiya tez yetib keldi) ishladi — himoya chuqurligi aynan shunday ishlashi kerak. Ikkinchidan, fizik pentest uchun **kim haqiqatan ruxsat bera olishi** (bino egasi), sinov doirasi, vaqti va favqulodda aloqa tartibi yozma ravishda aniq kelishilishi shart. Saboq: ruxsatsiz "sinov" — qonunbuzarlik; ruxsat hujjatlari pentester yonida boʻlishi va mulk egasi tomonidan imzolangan boʻlishi kerak.
:::

:::case
**Keys 5. Ikkinchi qoʻl disklar va telefonlar.** Bir necha yillar davomida maʼlumotlarni tiklash kompaniyalari va universitetlar internet-auksionlarda sotib olingan ishlatilgan disklar, xotira kartalari va telefonlarni tekshirib, ularning sezilarli qismida (tadqiqotga qarab — har 2–3 tadan birida) shaxsiy va korporativ maʼlumotlar — hujjatlar, rasmlar, parollar, mijozlar bazalari — qolganini aniqlagan. Koʻp hollarda egalar fayllarni "oʻchirgan" yoki diskni tez formatlagan.

**Tahlil.** Tahdid — inson tomonidan (imkoniyatdan foydalanuvchi har kim); qatlam — axborot tashuvchisi hayotining oxiri; ishlamagan vazifa — toʻxtatish (yoʻq qilish jarayoni yoʻq). Buzilgan xossa — konfidensiallik. Saboq: NIST SP 800-88 boʻyicha Clear, Purge yoki Destroy; tashuvchilarni hisobga olish va yoʻq qilish dalolatnomasi; utilizatsiya boʻyicha pudratchilardan sertifikat talab qilish; boshidanoq disk shifrlash — keyin kriptografik tozalash bir zumda.
:::

## Umumiy xulosalar

| Kuzatuv | Qaysi keysda | Arzon chora |
|---|---|---|
| Olinadigan tashuvchi himoyani chetlab oʻtdi | Stuxnet | USB nazorati, avtoishga tushirishni oʻchirish |
| Qurilma oʻgʻirlandi, maʼlumot ochiq edi | Veteranlar departamenti | toʻliq disk shifrlash |
| Zaxira nusxa asl tizim bilan birga yoʻqoldi | OVHcloud | 3-2-1 qoidasi, boshqa hududdagi nusxa |
| Eshik ochildi, lekin aniqlash va javob ishladi | Ayova sudi | qatlamli himoya, signalizatsiya va javob rejasi |
| "Oʻchirilgan" maʼlumot tiklandi | Ikkinchi qoʻl disklar | NIST 800-88, yoʻq qilish dalolatnomasi |

Umumiy saboq: fizik va axborot xavfsizligi bir butun. Shifrlash qurilma oʻgʻirlanishi oqibatini kamaytiradi, fizik nazorat esa kiberhujumning boshlangʻich nuqtasini (USB, konsol, server xonasi) yopadi.

## Mustaqil tahlil topshirigʻi

Oʻzingiz oʻqiydigan yoki ishlaydigan binoni (yoki uning bir qavatini) kuzatib, qisqa **fizik audit** hisobotini tuzing. Hech qanday himoyani buzmang va ruxsatsiz joylarga kirmang — faqat ochiq koʻrinib turgan narsalarni yozing:

1. Perimetr va kirish: kirish nazorati bormi (qoʻriqchi, turniket, karta)? Tashrifchilar roʻyxatga olinadimi?
2. Ergashib kirish imkoniyati: eshiklar orqasidan kirish oson emasmi?
3. Ish joylari: ekranlar qulflanganmi, stollarda hujjatlar va parol yozilgan qogʻozlar bormi?
4. Texnik xonalar: server yoki kommutatsiya shkaflari qulflanganmi, belgilanganmi?
5. Muhit: yongʻin datchiklari, oʻt oʻchirgichlar, favqulodda chiqish belgilari bormi?
6. Hujjat va tashuvchilarni yoʻq qilish: qogʻoz maydalagich yoki maxsus konteyner bormi?

Har bir topilma uchun xavf darajasi (past, oʻrta, yuqori) va arzon chora taklif qiling. Hisobotda aniq odamlar, parollar yoki kodlarni koʻrsatmang.

:::warning
**Koʻp uchraydigan xatolar**

- **"Bizda kiberxavfsizlik bor — fizik xavfsizlik shart emas".** Konsol yoki diskka jismoniy kirish koʻp himoyani chetlab oʻtadi.
- **Zaxira nusxani bir xonada yoki bir binoda saqlash.** Yongʻin va suv toshqini ikkalasini birga yoʻq qiladi.
- **Ruxsatsiz "sinov".** Hatto yaxshi niyat bilan qilingan fizik pentest ham bino egasining yozma ruxsatisiz — qonunbuzarlik.
- **Noutbuklarni shifrlamaslik.** Oʻgʻirlik — eng koʻp uchraydigan fizik hodisa; shifrlash uni maʼlumot sizishiga aylanishdan saqlaydi.
- **Topilgan fleshkani tekshirish uchun ulash.** Uni IT yoki xavfsizlik xizmatiga topshiring.
:::

:::tip
Keyslar boʻyicha qoʻshimcha maʼlumotni rasmiy hisobotlar va ishonchli OAV dan oling; bir manbaga tayanmang. Audit uchun tayyor nazorat roʻyxatlari (masalan, CISA ning fizik xavfsizlik qoʻllanmalari) yaxshi boshlangʻich nuqta.
:::

## Xulosa

- Fizik hodisalarni tahdid turi, buzilgan qatlam, ishlamagan vazifa va buzilgan xossa boʻyicha tahlil qilish qulay.
- Havo boʻshligʻi ham USB orqali buzilishi mumkin (Stuxnet) — olinadigan tashuvchilar nazorat qilinadi.
- Oʻgʻirlangan shifrlanmagan noutbuk — maʼlumot sizishi; shifrlangani — faqat qurilma yoʻqotilishi.
- Zaxira nusxa boshqa joyda boʻlishi kerak (3-2-1); bulut ham yongʻindan xoli emas (OVHcloud).
- Qatlamli himoyada bitta qatlam buzilsa, aniqlash va javob ishlashi kerak; fizik pentest — faqat egasining yozma ruxsati bilan.
- "Oʻchirilgan" maʼlumot tiklanadi — tashuvchilar NIST 800-88 boʻyicha yoʻq qilinadi.

## Oʻzingizni tekshiring

:::check
Q: Stuxnet havodan ajratilgan tarmoqqa qanday yetib borgan?
A: USB fleshkalar orqali — olinadigan tashuvchilar fizik izolyatsiyani chetlab oʻtdi.
Q: Veteranlar departamenti noutbuki holatida qaysi chora zararni deyarli yoʻqqa chiqarardi?
A: Toʻliq disk shifrlash — oʻgʻri maʼlumotni oʻqiy olmasdi.
Q: OVHcloud yongʻinidan keyin nima uchun ayrim mijozlar maʼlumotlarini butunlay yoʻqotdi?
A: Zaxira nusxalari xuddi shu kampusda saqlangan yoki umuman boʻlmagan; 3-2-1 qoidasi bajarilmagan.
Q: Ayova keysidan fizik pentest uchun qanday saboq olinadi?
A: Ruxsat bino egasidan yozma ravishda olinishi, sinov doirasi va aloqa tartibi aniq kelishilishi kerak.
Q: Ishlatilgan diskni sotishdan oldin nima qilish kerak?
A: NIST SP 800-88 boʻyicha tozalash (Clear yoki Purge) yoki diskni yoʻq qilish; oddiy oʻchirish va tez formatlash yetarli emas.
:::

:::link https://www.cisa.gov/topics/physical-security
CISA: Physical Security
AQSH Kiberxavfsizlik va infratuzilma xavfsizligi agentligining fizik xavfsizlik boʻyicha materiallari (ingliz tilida).
:::
