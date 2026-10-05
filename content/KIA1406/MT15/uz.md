---
title: Virtual himoyalangan tarmoq va undan amaliyotda foydalanish (masalan, CyberGhost yoki ExpressVPN misolida)
summary: VPN turlari — korporativ (masofaviy kirish, saytdan saytga) va tijoriy shaxsiy VPN xizmatlari (CyberGhost, ExpressVPN, Proton VPN va boshq.); shaxsiy VPN nimadan himoya qiladi va nimadan himoya qilmaydi; VPN provayderini tanlash mezonlari — jurnal yuritmaslik siyosati va uning mustaqil auditi, yurisdiksiya, protokollar (WireGuard, OpenVPN, IKEv2), kill switch, DNS sizishidan himoya; bepul VPN xavflari; amaliy ish — VPN ni oʻrnatish, sozlash va sizishlarni tekshirish; qonun va tashkilot siyosatiga rioya qilish.
---

## Nima uchun bu mavzu muhim?

M14 da VPN ni tashkilot tarmogʻini himoyalash vositasi sifatida koʻrdik. Bugun "VPN" soʻzi koʻpincha boshqa narsani — reklamalarda "toʻliq anonimlik" vaʼda qiladigan **tijoriy shaxsiy VPN xizmatlarini** anglatadi. Ular haqiqatan foydali boʻlishi mumkin (masalan, jamoat Wi-Fi tarmogʻida), lekin ularning imkoniyatlari koʻpincha oshirib koʻrsatiladi. Bu ishda VPN dan ongli foydalanishni — nimani himoya qilishini, provayderni qanday tanlashni va sozlamani qanday tekshirishni oʻrganamiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- korporativ va shaxsiy VPN farqini tushuntira olasiz;
- shaxsiy VPN qaysi tahdidlardan himoya qilishini va qaysilaridan qilmasligini ayta olasiz;
- VPN provayderini asosli mezonlar boʻyicha tanlay olasiz;
- VPN ni sozlab, IP va DNS sizishini tekshira olasiz;
- VPN dan foydalanishning huquqiy va tashkiliy chegaralarini bilasiz.
:::

## Korporativ va shaxsiy VPN

| | Korporativ VPN | Shaxsiy (tijoriy) VPN |
|---|---|---|
| Maqsadi | xodimni tashkilot tarmogʻiga xavfsiz ulash | foydalanuvchi trafikini provayder serveri orqali Internetga chiqarish |
| Kim boshqaradi | tashkilot IT xizmati | VPN xizmat koʻrsatuvchi kompaniya |
| Ishonch | tashkilotga | VPN provayderiga — u endi trafikingizning metamaʼlumotlarini koʻradi |
| Misollar | IPSec, SSL VPN, WireGuard, ZTNA (M14) | CyberGhost, ExpressVPN, Proton VPN, Mullvad va boshq. |

## Shaxsiy VPN nimadan himoya qiladi

| Himoya qiladi | Himoya qilmaydi |
|---|---|
| jamoat Wi-Fi tarmogʻida (kafe, aeroport) mahalliy tarmoqdagilar trafikni tinglashidan | fishing saytlar va soxta ilovalardan |
| Internet provayderi (ISP) qaysi saytlarga kirganingizni koʻrishidan | zararli dasturlardan (VPN antivirus emas) |
| sayt sizning haqiqiy IP manzilingizni bilishidan | brauzer cookie lari, hisobga kirish va "barmoq izi" orqali kuzatishdan |
| | sayt yoki hisobingiz buzilishidan |
| | VPN provayderining oʻzidan — u endi ISP oʻrnini egallaydi |

Bugun saytlarning aksariyati HTTPS (TLS, M9) ishlatgani uchun jamoat Wi-Fi da ham mazmun allaqachon shifrlangan; VPN qoʻshimcha ravishda **qaysi** saytlarga ulanayotganingizni (DNS soʻrovlari, manzillar) yashiradi. "100% anonimlik" vaʼdasi — reklama.

## VPN provayderini tanlash mezonlari

| Mezon | Nimaga qarash kerak |
|---|---|
| **Jurnal yuritmaslik siyosati** (no-logs) | ulanishlar, IP va faoliyat jurnali saqlanmasligi; eng muhimi — **mustaqil audit** natijalari va real hodisalarda (sud soʻrovlari) tasdiqlangani |
| **Yurisdiksiya** | kompaniya qaysi davlat qonunlariga boʻysunadi, maʼlumotlarni saqlash majburiyatlari bormi |
| **Protokollar** | WireGuard, OpenVPN, IKEv2 — zamonaviy va tekshirilgan; PPTP — eskirgan va zaif |
| **Kill switch** | VPN uzilsa, trafik ochiq kanalga chiqib ketmasligi |
| **DNS va IPv6 sizishidan himoya** | DNS soʻrovlari provayderning serverlariga yoʻnaltiriladi |
| **Mustaqil xavfsizlik auditlari va ochiq kodli ilovalar** | ilova va infratuzilma tashqi tomonidan tekshirilganmi |
| **Shaffoflik** | egasi kim, shaffoflik hisobotlari, hodisalar haqida ochiq xabar beradimi |
| **Toʻlov va shaxsiy maʼlumot** | roʻyxatdan oʻtishda qancha shaxsiy maʼlumot talab qilinadi |

Masalan, CyberGhost va ExpressVPN — mashhur tijoriy xizmatlar; ular jurnal yuritmaslik siyosatini eʼlon qilgan va mustaqil auditlar oʻtkazganini bildiradi. Bunday daʼvolarni har doim **audit hisobotlarining oʻzi** va ularning sanasi bilan tekshiring — reklama sahifasi dalil emas. Shuningdek, bir nechta mashhur VPN brendlari bitta yirik kompaniyaga tegishli boʻlishi mumkinligini hisobga oling.

**Bepul VPN xavflari**: xizmat qandaydir yoʻl bilan pul topishi kerak — reklama, trafik maʼlumotlarini sotish yoki kuzatuv. Tadqiqotlar bepul VPN ilovalarining bir qismi trafikni shifrlamasligi, ortiqcha ruxsatlar soʻrashi va hatto zararli kod saqlashini koʻrsatgan. Rasmiy doʻkon va ishlab chiqaruvchi saytidan boshqa joydan oʻrnatmang.

## Amaliy ish

Bajarishdan oldin tashkilotingiz (universitet, ish joyi) siyosatini va mamlakat qonunchiligini tekshiring: baʼzi tarmoqlarda shaxsiy VPN ishlatish taqiqlangan boʻlishi mumkin.

1. **Asosiy holatni yozib oling**: VPN siz ipleak.net yoki browserleaks.com ga kiring — IP manzil, mamlakat va DNS serverlarini yozing.
2. **Ishonchli VPN ni oʻrnating** (bepul tarifi boʻlgan va auditdan oʻtgan xizmat yoki universitet bergan VPN) — faqat rasmiy saytdan.
3. **Sozlang**: protokol — WireGuard yoki OpenVPN; **kill switch** yoqilgan; DNS sizishidan himoya yoqilgan; avtomatik ulanish jamoat Wi-Fi tarmoqlarida.
4. **Tekshiring**: VPN yoqilgan holda ipleak.net ni qayta oching — IP va DNS VPN serveriga tegishli boʻlishi, haqiqiy IP va ISP DNS koʻrinmasligi kerak. WebRTC sizishini ham tekshiring.
5. **Kill switch sinovi**: VPN ulanishini uzib koʻring — Internet vaqtincha toʻxtashi kerak.
6. **Tezlikni solishtiring**: speedtest bilan VPN siz va VPN bilan; masofaviy serverlar sekinroq ekanini kuzating.
7. **Hisobot**: natijalar jadvali (skrinshotlarda haqiqiy IP manzilni yashiring), xulosa — sizning holatingizda VPN qanday tahdidlardan himoya qildi va qaysilaridan himoya qilmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **VPN ni "toʻliq anonimlik" deb hisoblash.** Hisobga kirsangiz yoki cookie lar saqlansa, sayt sizni baribir taniydi; VPN provayderi esa trafikingizni koʻradi.
- **Bepul nomaʼlum VPN ilovasi.** Maʼlumotlaringiz mahsulotga aylanadi.
- **Kill switch siz ishlatish.** Ulanish uzilganda trafik ochiq ketadi va siz buni sezmaysiz.
- **VPN ni antivirus yoki fishingdan himoya deb tushunish.** Bu vazifalarni boshqa vositalar bajaradi.
- **Qonun va tashkilot siyosatini eʼtiborsiz qoldirish.** Ish tarmogʻida ruxsatsiz VPN — siyosat buzilishi va xavfsizlik nazoratini chetlab oʻtish.
:::

:::tip
Eng yaxshi "shaxsiy VPN" baʼzan — oʻzingiz boshqaradigan VPN: uy routeringiz yoki kichik serverda WireGuard ni sozlab, jamoat Wi-Fi dan uyingiz orqali Internetga chiqishingiz mumkin. Bu holda trafikingizni uchinchi tomon koʻrmaydi.
:::

## Xulosa

- Korporativ VPN tashkilot tarmogʻiga ulaydi; shaxsiy VPN trafikni provayder serveri orqali Internetga chiqaradi va ishonchni ISP dan VPN provayderiga oʻtkazadi.
- Shaxsiy VPN jamoat Wi-Fi da tinglash, ISP kuzatuvi va IP oshkor boʻlishidan himoya qiladi; fishing, zararli dastur va cookie kuzatuvidan emas.
- Provayderni tanlash: auditdan oʻtgan no-logs, yurisdiksiya, zamonaviy protokollar, kill switch, DNS sizishidan himoya, shaffoflik.
- Bepul nomaʼlum VPN — xavfli; faqat rasmiy manbalardan.
- Sozlamani ipleak kabi testlar bilan tekshiring va qonun hamda tashkilot siyosatiga rioya qiling.

## Oʻzingizni tekshiring

:::check
Q: Shaxsiy VPN ishlatganda trafikingizning metamaʼlumotlarini kim koʻradi?
A: VPN provayderi — u Internet provayderi oʻrnini egallaydi.
Q: Kill switch nima uchun kerak?
A: VPN ulanishi uzilganda trafik himoyasiz kanal orqali chiqib ketmasligi uchun.
Q: VPN fishing saytidan himoya qiladimi?
A: Yoʻq, VPN kanalni himoyalaydi; soxta saytga parol kiritishning oldini olmaydi.
Q: VPN provayderining "jurnal yuritmaymiz" degan daʼvosini qanday tekshirish mumkin?
A: Mustaqil audit hisobotlari, ularning sanasi va real hodisalardagi (sud soʻrovlari) tasdiqlar orqali.
:::

:::link https://ipleak.net/
ipleak.net
IP, DNS va WebRTC sizishini tekshirish uchun bepul test sahifasi (ingliz tilida).
:::
