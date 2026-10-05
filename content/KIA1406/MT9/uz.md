---
title: Zamonaviy antivirus dasturiy vositalari va ularning imkoniyatlari. Kiberjinoyat va kiberhuquq
summary: Antivirusdan EPP va EDR gacha — zamonaviy oxirgi nuqta himoyasi; aniqlash usullari — imzolar, evristika, xatti-harakat tahlili, mashinali oʻrganish, bulut reputatsiyasi, "qum qutisi"; qoʻshimcha imkoniyatlar — veb va pochta filtri, shifrlovchi viruslardan himoya, qurilma nazorati, markaziy boshqaruv; mahsulotlarni baholash — AV-TEST, AV-Comparatives; Windows Defender; kiberjinoyat tushunchasi va turlari; Oʻzbekiston qonunchiligi — Jinoyat kodeksidagi kompyuter axboroti sohasidagi jinoyatlar, "Kiberxavfsizlik toʻgʻrisida"gi, "Shaxsga doir maʼlumotlar toʻgʻrisida"gi qonunlar; xalqaro hujjatlar; jabrlanganda harakatlar.
---

## Nima uchun bu mavzu muhim?

Kompyuterdagi himoya vositasi — har bir foydalanuvchi va tashkilotning birinchi himoya chizigʻi. Lekin bugungi "antivirus" 1990-yillardagi imzo skanerlaridan ancha farq qiladi: u xatti-harakatni kuzatadi, bulutdan maʼlumot oladi va hodisaga javob beradi. Ikkinchi mavzu — kiberhuquq — himoyaning boshqa tomoni: qonun nima jinoyat ekanini belgilaydi, jabrlanuvchini himoya qiladi va mutaxassisning oʻzi uchun ham chegaralar qoʻyadi (M3).

:::note
**Bu mustaqil ishdan soʻng siz:**

- zamonaviy oxirgi nuqta himoyasi (EPP, EDR) imkoniyatlarini tushuntira olasiz;
- aniqlash usullarining kuchli va zaif tomonlarini ayta olasiz;
- antivirus mahsulotlarini mustaqil sinov natijalari boʻyicha baholay olasiz;
- kiberjinoyat turlarini va ular uchun Oʻzbekiston qonunchiligidagi javobgarlikni bilasiz;
- kiberjinoyatdan jabrlanganda toʻgʻri harakat qila olasiz.
:::

## Antivirusdan EPP va EDR gacha

| Avlod | Imkoniyatlar |
|---|---|
| **Klassik antivirus** | maʼlum zararli fayllar imzolari bazasi, faylni ochishda skanerlash |
| **EPP** (Endpoint Protection Platform) | antivirus + xost devori, veb-filtr, qurilma (USB) nazorati, ilovalar nazorati, markaziy boshqaruv |
| **EDR** (Endpoint Detection and Response) | jarayonlar, tarmoq ulanishlari va fayl amallarini doimiy yozib boradi; shubhali zanjirlarni aniqlaydi; qurilmani tarmoqdan ajratish, jarayonni toʻxtatish, tergov uchun maʼlumot |
| **XDR / MDR** | oxirgi nuqta, tarmoq, pochta va bulut maʼlumotlarini birlashtirish; tashqi mutaxassislar xizmati (MDR) |

## Aniqlash usullari

| Usul | Mohiyati | Kuchli tomoni | Zaif tomoni |
|---|---|---|---|
| **Imzolar** | maʼlum namunalarning xeshlari va baytlar ketma-ketligi | aniq, kam soxta signal | yangi yoki oʻzgartirilgan namunalarni koʻrmaydi |
| **Evristika** | shubhali tuzilma va buyruqlar | oʻzgartirilgan namunalarni ham topadi | soxta signallar |
| **Xatti-harakat tahlili** | dastur nima qilayotgani: koʻp fayllarni shifrlash, tizim sozlamalarini oʻzgartirish | yangi tahdidlarga qarshi samarali | zarar boshlangandan keyin ishga tushishi mumkin |
| **Mashinali oʻrganish** | millionlab namunalardan oʻrganilgan model | tez, yangi namunalarni umumlashtiradi | tushuntirish qiyin, aldanishi mumkin |
| **Bulut reputatsiyasi** | fayl yoki sayt haqida global maʼlumot | tez yangilanadi | Internet aloqasi kerak, maxfiylik masalasi |
| **Qum qutisi** (sandbox) | shubhali faylni izolyatsiyalangan muhitda ishga tushirib kuzatish | xatti-harakatni xavfsiz koʻrish | sekin, baʼzi namunalar sinov muhitini aniqlaydi |

Zamonaviy mahsulotlar bu usullarni **birlashtiradi** — birortasi ham yolgʻiz yetarli emas.

**Qoʻshimcha imkoniyatlar**: veb va pochta filtri (fishing sahifalarni bloklash), shifrlovchi viruslardan himoya (himoyalangan papkalar, masalan Windows dagi Controlled Folder Access), qurilma nazorati, zaifliklarni skanerlash, markaziy konsol va hisobotlar.

**Windows Defender** (Microsoft Defender Antivirus) Windows ga oʻrnatilgan va mustaqil sinovlarda yuqori natijalar koʻrsatadi; uy foydalanuvchisi uchun koʻpincha yetarli. Tashkilotlar markaziy boshqaruv va EDR imkoniyatlari uchun korporativ versiyalar yoki boshqa ishlab chiqaruvchilar (ESET, Kaspersky, Bitdefender, CrowdStrike, Sophos va boshq.) mahsulotlarini tanlaydi.

## Mahsulotlarni baholash

Reklama emas, **mustaqil laboratoriyalar** natijalariga tayaning:

| Laboratoriya | Nimani oʻlchaydi |
|---|---|
| **AV-TEST** (Germaniya) | himoya, tizim tezligiga taʼsir, foydalanish qulayligi (soxta signallar) |
| **AV-Comparatives** (Avstriya) | real sharoitdagi himoya, soxta signallar, unumdorlik, EDR sinovlari |
| **SE Labs**, **MITRE ATT&CK Evaluations** | hujum zanjirlarini aniqlash, EDR samaradorligi |

Taqqoslash mezonlari: aniqlash darajasi, soxta signallar soni, tizimni sekinlashtirishi, boshqaruv qulayligi, narx, qoʻllab-quvvatlash va ishlab chiqaruvchining maʼlumotlar bilan ishlash siyosati.

**Muhim tamoyillar**: bitta kompyuterda faqat **bitta** real vaqt antivirusi (ikkitasi bir-biriga xalaqit beradi); bazalar va dastur avtomatik yangilanadi; antivirus — yangilanishlar, zaxira nusxa, eng kam imtiyoz va xabardorlik oʻrnini bosmaydi.

## Kiberjinoyat va kiberhuquq

**Kiberjinoyat** — kompyuter tizimlari yoki axborotga qarshi yoki ular yordamida sodir etilgan jinoyat. Asosiy turlari:

- kompyuter tizimiga **ruxsatsiz kirish** va axborotni ruxsatsiz oʻzgartirish yoki yoʻq qilish;
- zararli dasturlarni yaratish va tarqatish;
- kompyuter yordamidagi **firibgarlik** va oʻgʻirlik (bank kartalari, onlayn aldov);
- shaxsiy maʼlumotlarni noqonuniy yigʻish va tarqatish;
- tovlamachilik (shu jumladan maʼlumotni shifrlab, toʻlov talab qilish);
- mualliflik huquqini buzish, taqiqlangan kontent tarqatish.

**Oʻzbekiston qonunchiligi** (asosiy hujjatlar, toʻliq matnlari — lex.uz da):

| Hujjat | Mazmuni |
|---|---|
| **Jinoyat kodeksi** — "Axborot texnologiyalari sohasidagi jinoyatlar" boʻlimi | kompyuter axborotiga ruxsatsiz kirish, zararli dasturlar, axborotni ruxsatsiz oʻzgartirish va boshqa harakatlar uchun javobgarlik |
| **"Kiberxavfsizlik toʻgʻrisida"gi Qonun** (2022) | kiberxavfsizlik tizimi, vakolatli organ, muhim axborot infratuzilmasi obyektlari talablari, hodisalar haqida xabar berish |
| **"Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonun** (2019) | shaxsiy maʼlumotlarni yigʻish, saqlash va himoya qilish; Oʻzbekiston fuqarolari maʼlumotlarini mamlakat hududida saqlash talabi |
| **"Elektron raqamli imzo toʻgʻrisida"gi Qonun** | ERI ning huquqiy maqomi (M8) |
| **"Axborotlashtirish toʻgʻrisida"gi Qonun** | axborot resurslari va tizimlarini himoya qilish asoslari |

**Xalqaro hujjatlar**: Kiberjinoyatlar toʻgʻrisidagi Budapesht konvensiyasi (Yevropa Kengashi, 2001) — milliy qonunlar va hamkorlik uchun namuna; BMT ning kiberjinoyatchilikka qarshi konvensiyasi (2024-yilda qabul qilingan).

**Kiberjinoyatdan jabrlanganda**:

1. Darhol bankka murojaat qiling (kartani bloklash, tranzaksiyani bahslash).
2. Parollarni almashtiring, MFA ni yoqing, boshqa qurilmadan hisoblarni tekshiring.
3. Dalillarni saqlang: xabarlar, skrinshotlar, sanalar, raqamlar, havolalar — oʻchirmang.
4. Huquqni muhofaza qilish organlariga (ichki ishlar organlari) ariza bering; tashkilotlarda — xavfsizlik xizmati va vakolatli organga (Oʻzbekistonda — Kiberxavfsizlik markazi) xabar bering.
5. Tanishlaringizni ogohlantiring — hisobingizdan ularga ham xabar yuborilgan boʻlishi mumkin.

:::warning
**Koʻp uchraydigan xatolar**

- **Ikki antivirusni birga oʻrnatish.** Ular bir-birining ishiga xalaqit beradi va tizimni sekinlashtiradi.
- **"Antivirus bor — xavfsizman".** Fishing, zaif parollar va yangilanmagan dasturlardan antivirus toʻliq himoya qilmaydi.
- **Antivirusni "tezlik uchun" oʻchirib qoʻyish.** Himoyasiz tizim birinchi xatoda zararlanadi.
- **Buzilgan (crack) dasturlar va kalit generatorlari.** Ular zararli kod tarqatishning eng koʻp yoʻllaridan biri va qonunbuzarlik.
- **Kiberjinoyat haqida xabar bermaslik.** Kech murojaat pulni qaytarish va jinoyatchini topish imkonini kamaytiradi.
:::

:::tip
Referatda antivirus mahsulotlarini solishtirish uchun AV-TEST va AV-Comparatives saytlaridagi oxirgi sinov natijalarini jadval qilib keltiring; qonunlar matnini faqat lex.uz dagi amaldagi tahriridan oling va moddalarni aniq koʻrsating.
:::

## Xulosa

- Zamonaviy himoya: antivirus → EPP → EDR → XDR/MDR; aniqlash usullari birlashtiriladi.
- Imzolar maʼlum tahdidlarga, evristika, xatti-harakat va mashinali oʻrganish yangilariga qarshi; qum qutisi — xavfsiz tahlil uchun.
- Mahsulotlar mustaqil laboratoriyalar (AV-TEST, AV-Comparatives) natijalari boʻyicha baholanadi.
- Kiberjinoyat: ruxsatsiz kirish, zararli dasturlar, firibgarlik, shaxsiy maʼlumotlarni buzish, tovlamachilik.
- Oʻzbekistonda: Jinoyat kodeksi, "Kiberxavfsizlik toʻgʻrisida"gi, "Shaxsga doir maʼlumotlar toʻgʻrisida"gi qonunlar; jabrlanganda — bank, dalillar, ariza.

## Oʻzingizni tekshiring

:::check
Q: EDR klassik antivirusdan nimasi bilan farq qiladi?
A: U jarayonlar va ulanishlarni doimiy kuzatadi, shubhali zanjirlarni aniqlaydi va javob choralarini (izolyatsiya, toʻxtatish) beradi.
Q: Imzo asosidagi aniqlashning asosiy kamchiligi nima?
A: U faqat maʼlum namunalarni topadi; yangi yoki oʻzgartirilgan namunalarni koʻrmaydi.
Q: Antivirus mahsulotlarini qanday xolis baholash mumkin?
A: AV-TEST, AV-Comparatives kabi mustaqil laboratoriyalarning sinov natijalari boʻyicha.
Q: Bank kartasi bilan firibgarlikdan jabrlanganda birinchi qadam nima?
A: Darhol bankka murojaat qilib, kartani bloklash va tranzaksiyani bahslash.
:::

:::link https://lex.uz/
Lex.uz — Oʻzbekiston Respublikasi qonunchilik maʼlumotlari milliy bazasi
Jinoyat kodeksi, "Kiberxavfsizlik toʻgʻrisida"gi va boshqa qonunlarning amaldagi matnlari.
:::
