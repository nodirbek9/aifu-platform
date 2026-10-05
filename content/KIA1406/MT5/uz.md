---
title: Axborot-kommunikatsiya texnologiyalari xavfsizligiga boʻladigan tahdidlar. Zararkunanda dasturlarning turlari
summary: Tahdidlar tasnifi — manba (tabiiy, texnogen, inson), niyat (tasodifiy, qasddan), joylashuv (ichki, tashqi); tahdid manbalari (aktorlar) — davlat guruhlari, uyushgan jinoyatchilar, xaktivistlar, insayderlar, tajribasiz hujumchilar; ENISA tahdidlar landshafti — shifrlovchilar, zararli dasturlar, ijtimoiy muhandislik, maʼlumotlarga tahdidlar, foydalanish mumkinligiga tahdidlar, axborot manipulyatsiyasi, taʼminot zanjiri hujumlari; zararkunanda dasturlarning maqsadi boʻyicha turlari va mobil zararli dasturlar; tahdidlarni modellashtirish (STRIDE) asoslari.
---

## Nima uchun bu mavzu muhim?

Himoyani rejalashtirish "kimdan va nimadan himoyalanamiz?" degan savoldan boshlanadi (M2 dagi risk = tahdid × zaiflik × taʼsir). Kichik doʻkon va davlat vazirligiga bir xil hujumchilar hujum qilmaydi, ularning maqsadi va imkoniyatlari ham turlicha. Tahdidlarni tizimli tasniflash va dolzarb tahdidlar landshaftini bilish cheklangan resurslarni eng muhim yoʻnalishlarga sarflash imkonini beradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- tahdidlarni manba, niyat va joylashuv boʻyicha tasniflay olasiz;
- asosiy tahdid aktorlarini va ularning maqsadlarini ayta olasiz;
- ENISA landshaftidagi asosiy tahdidlarni bilasiz;
- zararkunanda dasturlarni maqsadi boʻyicha guruhlay olasiz, mobil tahdidlarni ayta olasiz;
- STRIDE modeli bilan oddiy tizim tahdidlarini aniqlay olasiz.
:::

## Tahdidlar tasnifi

| Mezon | Turlari | Misollar |
|---|---|---|
| **Manba** | tabiiy, texnogen, inson | zilzila; elektr uzilishi; hujumchi yoki xato qilgan xodim |
| **Niyat** | tasodifiy, qasddan | notoʻgʻri yuborilgan email; maqsadli hujum |
| **Joylashuv** | ichki, tashqi | insayder; Internetdagi hujumchi |
| **Buziladigan xossa** | konfidensiallik, yaxlitlik, foydalanish mumkinligi | maʼlumot sizishi; soxtalashtirish; xizmat toʻxtashi |

## Tahdid aktorlari

| Aktor | Maqsadi | Imkoniyatlari |
|---|---|---|
| **Davlat bilan bogʻliq guruhlar** (APT) | josuslik, sabotaj, siyosiy taʼsir | juda yuqori: uzoq muddatli, maqsadli, oʻz vositalari |
| **Uyushgan jinoyatchilar** | pul: tovlamachilik, firibgarlik, maʼlumot sotish | yuqori; "xizmat sifatida jinoyat" bozori (MT11) |
| **Xaktivistlar** | gʻoyaviy yoki siyosiy namoyish | oʻrta: DDoS, saytlarni buzish, maʼlumot sizdirish |
| **Insayderlar** | qasos, pul, beparvolik | tizimlarga qonuniy kirish huquqi bor |
| **Tajribasiz hujumchilar** ("script kiddies") | qiziqish, obroʻ | past: tayyor vositalar, oddiy zaifliklar |
| **Raqobatchilar** | sanoat josusligi | turlicha |

Tahdid aktorini bilish himoya darajasini tanlashga yordam beradi: oddiy tashkilot uchun eng ehtimoliy tahdid — maqsadsiz ommaviy hujumlar va firibgarlik, muhim infratuzilma uchun — maqsadli APT.

## ENISA tahdidlar landshafti

Yevropa Ittifoqi kiberxavfsizlik agentligi (ENISA) har yili asosiy tahdidlarni eʼlon qiladi. Soʻnggi hisobotlarda doimiy ravishda quyidagilar qayd etiladi:

| Tahdid | Mazmuni |
|---|---|
| **Shifrlovchilar** (ransomware) | maʼlumotni shifrlab va oʻgʻirlab tovlamachilik; eng katta moliyaviy zarar |
| **Zararli dasturlar** | troyanlar, josus dasturlar, botnetlar va boshqalar (MT4) |
| **Ijtimoiy muhandislik** | fishing, telefon orqali aldash, biznes pochtasini buzish (BEC) (MT8, MT16) |
| **Maʼlumotlarga tahdidlar** | maʼlumot sizishi va buzilishi, notoʻgʻri sozlangan bulut omborlari |
| **Foydalanish mumkinligiga tahdidlar** | DDoS va Internet xizmatlarining uzilishi |
| **Axborot manipulyatsiyasi** | dezinformatsiya, deepfake, soxta kontent |
| **Taʼminot zanjiri hujumlari** | yetkazib beruvchi yoki dastur yangilanishi orqali koʻplab mijozlarga kirish (masalan, SolarWinds, 2020) |

## Zararkunanda dasturlarning turlari

MT4 da tarqalish usuli boʻyicha batafsil tasnif berilgan. **Maqsadi** boʻyicha guruhlash:

| Maqsad | Turlar |
|---|---|
| **Pul topish** | shifrlovchilar, bank troyanlari, kriptomaynerlar, reklama dasturlari |
| **Josuslik va maʼlumot oʻgʻirlash** | josus dasturlar, kalit yozuvchilar, maʼlumot oʻgʻirlovchilar (infostealer) |
| **Nazoratni egallash** | masofaviy kirish troyanlari, botlar, rootkitlar |
| **Buzish va sabotaj** | wiper lar, sanoat tizimlariga qarshi zararli dasturlar |

**Mobil zararli dasturlar**: norasmiy manbalardan oʻrnatilgan ilovalar (APK fayllar), haddan tashqari ruxsatlar soʻrovchi ilovalar (SMS, maxsus imkoniyatlar), SMS kodlarni ushlovchi bank troyanlari, soxta "yangilanish" yoki "bank ilovasi". Himoya: faqat rasmiy doʻkonlar, ilova ruxsatlarini tekshirish, OT yangilanishlari, notanish havolalardan ilova oʻrnatmaslik.

## Tahdidlarni modellashtirish: STRIDE

Yangi tizim loyihalanganda tahdidlar **STRIDE** modeli bilan tizimli aniqlanadi:

| Harf | Tahdid | Buziladigan xossa | Himoya misoli |
|---|---|---|---|
| **S** — Spoofing | boshqa shaxs nomidan ishlash | autentiklik | MFA, sertifikatlar |
| **T** — Tampering | maʼlumotni oʻzgartirish | yaxlitlik | xesh, ERI, MAC |
| **R** — Repudiation | harakatni inkor etish | rad etib boʻlmaslik | jurnallar, ERI |
| **I** — Information disclosure | maʼlumot sizishi | konfidensiallik | shifrlash, foydalanishni boshqarish |
| **D** — Denial of service | xizmatni toʻxtatish | foydalanish mumkinligi | zaxira, tezlikni cheklash |
| **E** — Elevation of privilege | huquqni oshirish | avtorizatsiya | eng kam imtiyoz, yangilanishlar |

Amaliy mashq: universitet platformasidagi "baholarni koʻrish" funksiyasi uchun har bir STRIDE harfi boʻyicha bittadan tahdid va himoya chorasini yozing.

:::warning
**Koʻp uchraydigan xatolar**

- **"Bizni hech kim nishonga olmaydi".** Koʻp hujumlar maqsadsiz va avtomatlashtirilgan: zaif tizimlar oʻzi topiladi.
- **Faqat tashqi tahdidlarni hisobga olish.** Insayderlar va xodimlar xatolari katta ulushni tashkil qiladi.
- **Yetkazib beruvchilarga koʻr-koʻrona ishonish.** Taʼminot zanjiri — zamonaviy hujumlarning muhim yoʻli; shartnomada xavfsizlik talablarini belgilang.
- **Mobil qurilmalarni himoyasiz qoldirish.** Telefon — bank, pochta va MFA kalitlari joylashgan qurilma.
- **Eskirgan statistikadan foydalanish.** Tahdid landshafti har yili oʻzgaradi — eng soʻnggi hisobotlardan foydalaning.
:::

:::tip
Referat uchun ENISA Threat Landscape ning eng soʻnggi hisobotini oching va undagi asosiy tahdidlar roʻyxatini oʻz tashkilotingiz (masalan, universitet) misolida tahlil qiling: qaysi tahdidlar eng ehtimoliy va qanday choralar allaqachon mavjud?
:::

## Xulosa

- Tahdidlar manba, niyat, joylashuv va buziladigan xossa boʻyicha tasniflanadi.
- Aktorlar: davlat guruhlari, uyushgan jinoyatchilar, xaktivistlar, insayderlar, tajribasiz hujumchilar — maqsad va imkoniyatlari turlicha.
- ENISA landshafti: shifrlovchilar, zararli dasturlar, ijtimoiy muhandislik, maʼlumot va foydalanish mumkinligiga tahdidlar, axborot manipulyatsiyasi, taʼminot zanjiri.
- Zararkunanda dasturlar maqsadi boʻyicha: pul, josuslik, nazorat, sabotaj; mobil tahdidlar alohida eʼtibor talab qiladi.
- STRIDE — loyihalash bosqichida tahdidlarni tizimli aniqlash usuli.

## Oʻzingizni tekshiring

:::check
Q: APT guruhlari oddiy jinoyatchilardan nimasi bilan farq qiladi?
A: Ular odatda davlat bilan bogʻliq, uzoq muddatli maqsadli hujumlar oʻtkazadi va yuqori resurslarga ega.
Q: Taʼminot zanjiri hujumi nima?
A: Yetkazib beruvchi yoki dastur yangilanishini buzish orqali uning koʻplab mijozlariga kirish.
Q: STRIDE dagi "R" qaysi tahdidni bildiradi va unga qarshi qanday chora bor?
A: Repudiation — harakatni inkor etish; jurnallar va ERI.
Q: Mobil zararli dasturlarning keng tarqalgan yoʻli qaysi?
A: Norasmiy manbalardan oʻrnatilgan va ortiqcha ruxsatlar soʻraydigan ilovalar.
:::

:::link https://www.enisa.europa.eu/topics/cyber-threats/threat-landscape
ENISA Threat Landscape
Yevropa Ittifoqi kiberxavfsizlik agentligining yillik tahdidlar hisobotlari (ingliz tilida).
:::
