---
title: Axborot tizimlari va resurslaridan ruxsatsiz foydalanishlarni aniqlash uslubiyati. Simsiz aloqa tizimlarida axborot resurslarini himoyalash
summary: Ruxsatsiz foydalanishni aniqlash jarayoni — maʼlumot manbalari (jurnallar, tarmoq, xost), jurnallarni markazlashgan yigʻish, vaqtni sinxronlash, SIEM va korrelatsiya qoidalari, asosiy chiziq va anomaliyalar, ogohlantirishlarni saralash, hodisaga javob bosqichlari (NIST SP 800-61); tipik belgilar (indikatorlar); simsiz tarmoqlar xavfsizligi — Wi-Fi standartlari, WEP, WPA2, WPA3, Personal va Enterprise (802.1X, RADIUS) rejimlari, mehmon tarmogʻi, soxta kirish nuqtalari va ularni aniqlash, Bluetooth xavfsizligi; uy va ofis Wi-Fi ni xavfsiz sozlash.
---

## Nima uchun bu mavzu muhim?

Himoya qanchalik kuchli boʻlmasin, ertami-kechmi kimdir uni chetlab oʻtishga harakat qiladi. Hisobotlarga koʻra, buzilish va uni aniqlash oraligʻi koʻpincha haftalar va oylar bilan oʻlchanadi. Ruxsatsiz foydalanishni **tez aniqlash** zararni keskin kamaytiradi. Ikkinchi mavzu — simsiz tarmoqlar — tashkilot perimetrini binodan tashqariga, avtoturargoh va qoʻshni ofislarga ham chiqaradi: radio toʻlqinlar devorlarni tan olmaydi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- ruxsatsiz foydalanishni aniqlash uchun maʼlumot manbalari va jarayonni tushuntira olasiz;
- SIEM, korrelatsiya va anomaliyalarni aniqlash gʻoyasini bilasiz;
- hodisaga javob bosqichlarini ayta olasiz;
- Wi-Fi xavfsizlik standartlarini va rejimlarini farqlay olasiz;
- uy yoki ofis Wi-Fi tarmogʻini xavfsiz sozlay olasiz.
:::

## Ruxsatsiz foydalanishni aniqlash uslubiyati

**1. Maʼlumot manbalari**:

| Manba | Nimani koʻrsatadi |
|---|---|
| OT jurnallari (Windows Security, Linux auth.log) | kirishlar, muvaffaqiyatsiz urinishlar, huquq oʻzgarishlari (A10) |
| Ilova va maʼlumotlar bazasi jurnallari | kim qaysi yozuvga murojaat qilgan |
| Xavfsizlik devori, VPN, proksi jurnallari | ulanishlar, bloklangan urinishlar |
| IDS/IPS va EDR ogohlantirishlari | maʼlum hujum belgilari, shubhali jarayonlar (MT4, MT9) |
| Bulut va identifikatsiya xizmatlari jurnallari | kirishlar joyi va vaqti, MFA natijalari |
| Fizik kirish tizimlari | kim qachon binoga kirgan (M12) |

**2. Yigʻish va tayyorlash**: jurnallar markaziy serverga yuboriladi (hujumchi qurilmadagi jurnalni oʻchirsa ham nusxa qoladi); barcha qurilmalarda **vaqt sinxronlanadi** (NTP) — aks holda hodisalarni ketma-ketlikda bogʻlab boʻlmaydi; saqlash muddati qonun va siyosat boʻyicha belgilanadi.

**3. Tahlil**:

- **Qoidalar va korrelatsiya** (SIEM): bir nechta hodisani bogʻlash, masalan "bitta hisobga 10 daqiqada 20 ta muvaffaqiyatsiz kirish, keyin muvaffaqiyatli kirish".
- **Asosiy chiziq va anomaliyalar**: odatiy xatti-harakat (ish vaqti, odatiy joy, odatiy maʼlumot hajmi) va undan chetga chiqish — tunda kirish, imkonsiz sayohat (bir soatda ikki davlatdan kirish), odatdagidan koʻp yuklab olish.
- **Tahdid razvedkasi**: maʼlum zararli manzillar va fayl xeshlari roʻyxatlari bilan solishtirish.

**Tipik belgilar (indikatorlar)**: ishdan tashqari vaqtda imtiyozli hisobga kirish; yangi administrator hisobi yoki guruhga qoʻshilish; jurnallarning tozalanishi; notanish dastur yoki xizmat; tashqi notanish manzilga katta hajmli maʼlumot ketishi; bir xil manzildan koʻp hisoblarga kirish urinishlari.

**4. Ogohlantirishlarni saralash**: soxta signallar koʻp boʻladi — ustuvorlik (aktiv qiymati, ishonch darajasi), kontekst va tasdiqlash kerak. SOC tahlilchisining asosiy ishi ana shu.

**5. Hodisaga javob** (NIST SP 800-61 boʻyicha): **tayyorgarlik** → **aniqlash va tahlil** → **cheklash, yoʻq qilish va tiklash** → **hodisadan keyingi faoliyat** (saboqlar, qoidalarni yaxshilash). Dalillarni saqlash (MT3 dagi dalillar zanjiri, xesh) — huquqiy jarayon uchun muhim.

## Simsiz tarmoqlar xavfsizligi

| Standart | Holati |
|---|---|
| **Ochiq tarmoq** | shifrlash yoʻq — VPN yoki HTTPS siz maxfiy ish qilmang (MT15) |
| **WEP** | butunlay buzilgan, ishlatmang |
| **WPA (TKIP)** | eskirgan |
| **WPA2-Personal (AES/CCMP)** | keng tarqalgan; kuchi umumiy parolga bogʻliq — zaif parol oflayn tanlanishi mumkin |
| **WPA3-Personal (SAE)** | tavsiya etiladi: oflayn parol tanlashga chidamli, oldinga maxfiylik |
| **WPA2/WPA3-Enterprise (802.1X)** | har bir foydalanuvchi oʻz hisobi yoki sertifikati bilan RADIUS server orqali; tashkilotlar uchun |

**Ofis uchun choralar**: Enterprise rejim (umumiy parol yoʻq — xodim ketganda faqat uning hisobi bloklanadi); xodimlar, mehmonlar va IoT qurilmalar uchun alohida tarmoqlar (VLAN); mehmon tarmogʻidan ichki tarmoqqa kirish yoʻq; kirish nuqtalari va kontroller proshivkasini yangilash; **soxta kirish nuqtalarini** (rogue AP — ruxsatsiz oʻrnatilgan yoki tashkilot nomini taqlid qiluvchi) simsiz IDS (WIDS) bilan aniqlash; signal kuchini binodan tashqariga keraksiz chiqmaydigan qilib sozlash.

**Uy uchun choralar**: routerning standart administrator parolini almashtirish; WPA3 (yoki kamida WPA2-AES) va uzun parol; **WPS ni oʻchirish**; router proshivkasini yangilash; masofaviy boshqaruvni oʻchirish; mehmonlar va "aqlli" qurilmalar uchun alohida tarmoq.

**Foydalanuvchi uchun**: jamoat Wi-Fi da faqat HTTPS saytlar va VPN; tarmoq nomi tanish boʻlsa ham — u soxta boʻlishi mumkin; avtomatik ulanishni oʻchirish; ishlatilmayotganda Wi-Fi va **Bluetooth** ni oʻchirish, Bluetooth qurilmalarni faqat ishonchli joyda juftlash.

:::warning
**Koʻp uchraydigan xatolar**

- **Jurnallarni faqat qurilmaning oʻzida saqlash.** Hujumchi ularni birinchi navbatda oʻchiradi.
- **Vaqt sinxronlanmagan qurilmalar.** Hodisalar zanjirini tiklab boʻlmaydi.
- **Ogohlantirishlarni hech kim koʻrmasligi.** SIEM ga yigʻish — boshlanish, tahlil va javob jarayoni kerak.
- **Ofisda bitta umumiy Wi-Fi paroli.** Uni bilganlar soni vaqt oʻtishi bilan nazoratsiz oshadi.
- **WPS va standart router parolini qoldirish.** Bu uy tarmoqlaridagi eng oddiy zaifliklar.
:::

:::tip
Oʻz Windows kompyuteringizda `eventvwr.msc` → Security jurnalidan oxirgi bir haftadagi kirishlarni (4624, 4625) koʻrib chiqing: qaysi vaqtlarda va qanday turdagi kirishlar boʻlgan? Bu — "asosiy chiziq" ni tushunishning oddiy mashqi. Routeringiz sozlamalarida esa xavfsizlik rejimi, WPS va proshivka versiyasini tekshiring.
:::

## Xulosa

- Aniqlash uslubiyati: manbalarni aniqlash → markazlashgan yigʻish va vaqt sinxronlash → qoidalar, korrelatsiya, anomaliyalar → saralash → javob.
- Tipik belgilar: ishdan tashqari imtiyozli kirish, yangi administratorlar, jurnallarni tozalash, katta hajmdagi maʼlumot chiqishi.
- Hodisaga javob: tayyorgarlik, aniqlash, cheklash va tiklash, saboqlar; dalillarni saqlash.
- Wi-Fi: WEP va WPA eskirgan; WPA3 yoki WPA2-AES; tashkilotlarda Enterprise (802.1X), segmentlash, soxta kirish nuqtalarini aniqlash.
- Uyda: router paroli, WPS ni oʻchirish, yangilanishlar, mehmon tarmogʻi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun jurnallar markaziy serverga yuboriladi?
A: Hujumchi qurilmadagi jurnalni oʻchirsa ham nusxa saqlanadi va turli manbalarni birga tahlil qilish mumkin boʻladi.
Q: "Imkonsiz sayohat" anomaliyasi nima?
A: Bitta hisobga qisqa vaqtda bir-biridan uzoq joylardan kirish — hisob maʼlumotlari oʻgʻirlanganining belgisi.
Q: WPA2-Enterprise ning Personal rejimidan asosiy afzalligi nima?
A: Umumiy parol yoʻq: har bir foydalanuvchi oʻz hisobi yoki sertifikati bilan kiradi va alohida bloklanishi mumkin.
Q: Soxta kirish nuqtasi (rogue AP) nima?
A: Ruxsatsiz oʻrnatilgan yoki ishonchli tarmoq nomini taqlid qiluvchi kirish nuqtasi.
:::

:::link https://csrc.nist.gov/pubs/sp/800/61/r3/final
NIST SP 800-61: Incident Response Recommendations
Xavfsizlik hodisalarini aniqlash va ularga javob berish boʻyicha tavsiyalar (ingliz tilida).
:::
