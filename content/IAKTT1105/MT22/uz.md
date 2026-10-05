---
title: Blokcheyn texnologiyasi va uning qoʻllanilishi
summary: Mustaqil ish — blokcheyn va taqsimlangan reyestr texnologiyalari (DLT); asosiy platformalar — Bitcoin, Ethereum, Hyperledger Fabric, Corda va ularning farqlari; masshtablash yechimlari (ikkinchi daraja, L2); tranzaksiya xarajati ("gaz"); blokcheyn loyihasini joriy etish bosqichlari (PoC, pilot, sanoat ekspluatatsiyasi); yopilgan loyihalar saboqlari (TradeLens, we.trade); markaziy bank raqamli valyutalari; texnologiyaning yetuklik darajasi; loyihani baholash topshiriqi.
---

## Mustaqil ishning maqsadi

M12 da blokcheyn tushunchasi, turlari va xesh zanjirini, S12 da sohalarda qoʻllanishini koʻrdik. Bu mustaqil ishda **amaliy texnologiya tanlovi**ga eʼtibor qaratamiz: qaysi platformalar bor, ular nimasi bilan farq qiladi, loyiha qanday bosqichlarda joriy etiladi va nima uchun koʻp mashhur loyihalar yopilgan.

:::note
**Bu mustaqil ishdan soʻng siz:**

- blokcheyn va taqsimlangan reyestr texnologiyalari farqini tushuntira olasiz;
- asosiy platformalarni xususiyatlari boʻyicha solishtira olasiz;
- blokcheyn loyihasini joriy etish bosqichlarini ayta olasiz;
- muvaffaqiyatsiz loyihalardan saboq chiqara olasiz.
:::

## Blokcheyn va DLT

**Taqsimlangan reyestr texnologiyasi** (DLT) — bir nechta ishtirokchida nusxasi saqlanadigan va kelishuv (konsensus) orqali yangilanadigan reyestr. **Blokcheyn** — DLT ning bir turi: yozuvlar bloklarga yigʻilib, xesh orqali zanjirga bogʻlanadi (M12). Barcha DLT lar blok zanjiri shaklida emas — masalan, Corda tranzaksiyalarni faqat ularga aloqador tomonlar orasida saqlaydi.

## Asosiy platformalar

| Platforma | Turi | Konsensus | Aqlli shartnoma | Qoʻllanish |
|---|---|---|---|---|
| **Bitcoin** (2009) | ommaviy, ruxsatsiz | Proof of Work | juda cheklangan | raqamli valyuta, qiymat saqlash |
| **Ethereum** (2015) | ommaviy, ruxsatsiz | Proof of Stake (2022 dan) | toʻliq (Solidity tili) | tokenlar, DeFi, NFT, ilovalar |
| **Hyperledger Fabric** | xususiy, ruxsatli | ishtirokchilar kelishuvi (buyurtma xizmati) | bor (Go, Java, JavaScript) | korxonalararo jarayonlar, taʼminot zanjiri |
| **Corda** | xususiy, ruxsatli | notarius tugunlari | bor (Kotlin, Java) | bank va moliya, savdoni moliyalashtirish |

**Ruxsatsiz** (permissionless) — istalgan kishi tugun ochib qatnasha oladi; **ruxsatli** (permissioned) — faqat tasdiqlangan ishtirokchilar (masalan, banklar konsorsiumi). Korxonalar odatda ruxsatli tarmoqlarni tanlaydi: ishtirokchilar maʼlum, tezlik yuqori, maʼlumotni maxfiy saqlash osonroq.

## Masshtablash va xarajatlar

- Ommaviy blokcheynlar sekundiga cheklangan tranzaksiyani qayta ishlaydi (Bitcoin — taxminan 7, toʻlov kartalari tizimlari — minglab).
- **Ikkinchi daraja** (Layer 2) yechimlari — tranzaksiyalar asosiy zanjirdan tashqarida tez bajariladi, natija vaqti-vaqti bilan asosiy zanjirga yoziladi (masalan, Bitcoin uchun Lightning Network, Ethereum uchun rollup lar).
- **Tranzaksiya haqi** ("gaz" Ethereum da) — tarmoq band boʻlganda keskin oshadi; arzon mikrotoʻlovlar uchun bu muammo.

## Joriy etish bosqichlari

| Bosqich | Maqsad | Natija |
|---|---|---|
| **1. Muammoni aniqlash** | ishonch muammosi bormi? (S12 dagi qaror savollari) | blokcheyn kerakmi yoki oddiy MB yetarlimi |
| **2. PoC** (gʻoya isboti) | texnik jihatdan ishlashini koʻrsatish | prototip, bir necha hafta |
| **3. Pilot** | real ishtirokchilar bilan cheklangan hajmda | foyda va xarajatlar oʻlchanadi |
| **4. Sanoat ekspluatatsiyasi** | butun jarayonni koʻchirish | boshqaruv (governance), qoʻllab-quvvatlash, huquqiy asos |

Koʻp loyihalar 2- yoki 3-bosqichda toʻxtaydi: texnologiya ishlaydi, lekin **ishtirokchilarni kelishtirish va biznes modeli** muammo boʻladi.

## Yopilgan loyihalar saboqlari

- **TradeLens** (Maersk va IBM, 2018–2022) — dengiz yuk tashish hujjatlari uchun blokcheyn platformasi. Texnik jihatdan ishlagan, lekin raqobatchi tashuvchilar platforma Maersk nazoratida ekanidan xavotirlanib, yetarli darajada qoʻshilmagan — tijorat jihatdan oʻzini oqlamay yopilgan.
- **we.trade** (Yevropa banklari konsorsiumi, 2022 da yopilgan) — kichik biznes savdosini moliyalashtirish; mablagʻ va mijozlar yetishmagan.

**Saboqlar**:

1. Blokcheyn **tarmoq effekti**ga (MT6) bogʻliq — ishtirokchilar koʻp boʻlmasa, qiymat yoʻq.
2. **Neytral boshqaruv** kerak — platforma bitta raqobatchiga tegishli boʻlsa, boshqalar ishonmaydi.
3. Aniq **biznes modeli** — kim toʻlaydi va nima uchun?
4. Mavjud tizimlar va qonunchilik bilan **integratsiya** — qogʻoz hujjatlar huquqiy kuchda qolsa, raqamli yozuv yetarli emas.

## Markaziy bank raqamli valyutalari

**CBDC** (S12) — milliy valyutaning raqamli shakli. Koʻplab markaziy banklar tadqiqot va pilot loyihalar olib bormoqda (Xitoy — elektron yuan, Yevropa — raqamli yevro loyihasi, Bagama, Nigeriya va boshqalar). Hammasi ham blokcheynda emas — koʻp loyihalar markazlashgan tizimni afzal koʻradi, chunki markaziy bank baribir yagona operator. Bu S12 dagi qoidani tasdiqlaydi: hamma ishonadigan yagona operator boʻlsa, blokcheyn shart emas.

## Texnologiyaning yetukligi

Blokcheyn 2017–2018-yillardagi "haddan tashqari umidlar" choʻqqisidan keyin real natija berayotgan sohalarga torayib bormoqda: raqamli aktivlar va toʻlovlar, stablecoinlar, aktivlarni tokenizatsiyalash, taʼminot zanjirining ayrim qismlari. Iqtisodchi uchun xulosa: har bir taklifni **xarajat, foyda va ishtirokchilar** nuqtai nazaridan tanqidiy baholash.

:::warning
**Koʻp uchraydigan xatolar**

- **Texnologiyadan boshlash.** "Bizga blokcheyn kerak" emas, "bizda qanday ishonch muammosi bor?" savolidan boshlang.
- **Faqat PoC natijasiga ishonish.** Texnik prototip ishlashi biznes muvaffaqiyatini kafolatlamaydi.
- **Ommaviy blokcheynga maxfiy maʼlumot yozish.** Ruxsatli tarmoq yoki faqat xesh yozish kerak.
- **Tranzaksiya xarajati va tezligini hisobga olmaslik.**
:::

## Topshiriq

1. Hyperledger Fabric va Ethereum ni solishtiruvchi jadval tuzing (kirish, konsensus, tezlik, xarajat, maxfiylik, tipik foydalanuvchilar).
2. TradeLens yoki we.trade haqida maqolalar topib, yopilish sabablarini oʻz soʻzlaringiz bilan yozing.
3. S12 dagi paxta-toʻqimachilik klasteri keysi uchun platformani tanlang va 4 bosqichli joriy etish rejasini tuzing: har bir bosqich maqsadi, muddati, muvaffaqiyat mezoni.

## Xulosa

- DLT — taqsimlangan reyestrlar umumiy nomi; blokcheyn — uning bloklar zanjiri shaklidagi turi.
- Platformalar: Bitcoin va Ethereum (ommaviy), Hyperledger Fabric va Corda (korporativ, ruxsatli).
- Masshtablash — L2 yechimlari; tranzaksiya haqi tarmoq yuklamasiga bogʻliq.
- Joriy etish: muammo → PoC → pilot → sanoat ekspluatatsiyasi; muvaffaqiyat ishtirokchilar, neytral boshqaruv va biznes modeliga bogʻliq.

## Oʻzingizni tekshiring

:::check
Q: Ruxsatli va ruxsatsiz blokcheynning farqi nima?
A: Ruxsatsizda istalgan kishi qatnasha oladi; ruxsatlida faqat tasdiqlangan ishtirokchilar.
Q: Ikkinchi daraja (Layer 2) yechimlari nima uchun kerak?
A: Tranzaksiyalarni asosiy zanjirdan tashqarida tez va arzon bajarib, masshtablash muammosini hal qilish uchun.
Q: TradeLens yopilishining asosiy saboqi nima?
A: Texnologiya yetarli emas — raqobatchilarni jalb qilish uchun neytral boshqaruv va aniq biznes modeli kerak.
Q: Nima uchun koʻp CBDC loyihalari blokcheyndan foydalanmaydi?
A: Markaziy bank baribir yagona ishonchli operator, shuning uchun markazlashgan tizim samaraliroq.
:::

:::link https://www.hyperledger.org/
Hyperledger Foundation
Korporativ blokcheyn platformalari boʻyicha ochiq loyihalar (ingliz tilida).
:::
