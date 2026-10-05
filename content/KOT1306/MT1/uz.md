---
title: Maʼruza va amaliy mashgʻulotlar mavzularini oʻzlashtirish
summary: "Kompyuter tarmoqlari" fanini mustaqil oʻzlashtirish rejasi; modullar boʻyicha haftalik jadval; maʼruza materialini oʻqish usuli; amaliy ishlarni uyda takrorlash uchun bepul vositalar (Packet Tracer, Wireshark, virtual mashinalar); oʻz-oʻzini tekshirish savollari; tayanch tushunchalar lugʻati.
---

## Nima uchun bu mavzu muhim?

Tarmoqlar fani — "qatlamli" fan: IP manzillashni bilmasdan marshrutlashni, TCP ni bilmasdan HTTP va soketlarni tushunib boʻlmaydi. Bitta mavzu oʻtkazib yuborilsa, keyingilari qiyinlashadi. Mustaqil taʼlim soatlari ayni shu boʻshliqlarni toʻldirish, maʼruzada koʻrilganni oʻz qoʻlingiz bilan takrorlash va bilimni amaliy koʻnikmaga aylantirish uchun ajratilgan.

:::note
**Bu mustaqil ishdan soʻng siz:**

- fan mavzularini oʻzlashtirish uchun shaxsiy haftalik reja tuza olasiz;
- har bir maʼruzani faol oʻqish usuli bilan oʻzlashtira olasiz;
- amaliy ishlarni uyda bepul vositalar bilan takrorlay olasiz;
- har bir modul boʻyicha oʻz bilimingizni savollar bilan tekshira olasiz.
:::

## Fan tuzilishi va oʻzlashtirish rejasi

| Hafta | Maʼruza | Amaliy | Mustaqil ish uchun asosiy natija |
|---|---|---|---|
| 1–2 | M1 Kirish va klassifikatsiya; M2 Topologiyalar va qurilmalar | A1 Qurilmalar; A2 Topologiyalar | qurilmalar va topologiyalar jadvali, uy tarmogʻi sxemasi |
| 3–4 | M3 OSI modeli; M4 Uzatish usullari va muhitlar | A3 Emulyatorlar; A4 Kodlash | OSI pogʻonalarini PDU va qurilmalari bilan yoddan chizish |
| 5–6 | M5 Ethernet va MAC; M6 Token Ring, FDDI, Wi-Fi | A5 Snifferlar; A6 Wi-Fi | Wireshark da kadrni maydonlarga ajratish |
| 7–8 | M7 TCP/IP va IP; M8 IPv4 manzillash | A7 Maskalar va VLSM | 20 ta quyi tarmoq masalasini qoʻlda yechish |
| 9–10 | M9 ARP, ICMP, DHCP, marshrutlash; M10 TCP, UDP, soketlar | A8 Marshrutizatorlar; A9 Soketlar | uch routerli Packet Tracer sxemasi, echo server |
| 11–12 | M11 HTTP, FTP, pochta; M12 DNS | A10 Windows Server; A11 DHCP va DNS | brauzer F12 da HTTP tahlili, `nslookup` mashqlari |
| 13–14 | M13 Server OT va domenlar; M14 Xavfsizlik | A12 Foydalanuvchilar; A13 Umumiy resurslar; A14 Brandmauer va VPN | VM da server, foydalanuvchilar va ulashishlar |
| 15 | M15 Nosozliklarni bartaraf qilish | A15 Utilitalar | nosozliklar boʻyicha hisobot |

Har haftaga 2–3 soat mustaqil ish rejalashtiring: **1 soat** — maʼruzani qayta oʻqish va konspekt, **1 soat** — amaliy ishni takrorlash, **0,5 soat** — "Oʻzingizni tekshiring" savollari va testlar.

## Maʼruzani faol oʻqish

1. **Koʻzdan kechirish** (5 daqiqa): sarlavhalar, jadvallar, "Bu darsdan soʻng siz" roʻyxati.
2. **Savollar**: har bir boʻlim sarlavhasini savolga aylantiring ("TCP qanday qilib ishonchlilikni taʼminlaydi?").
3. **Oʻqish**: javobni topib, oʻz soʻzlaringiz bilan qisqa yozing. Jadvallarni qayta chizing — yod olish uchun eng yaxshi usul.
4. **Misollarni ishlatish**: ishlangan misol kodini kompyuterda ishga tushiring va kirishni oʻzgartirib koʻring.
5. **Takrorlash**: 1 kun, 1 hafta va 1 oydan soʻng "Oʻzingizni tekshiring" savollariga yopiq kitob bilan javob bering.

## Uyda amaliyot uchun vositalar

| Vosita | Nima uchun | Narxi |
|---|---|---|
| **Cisco Packet Tracer** | topologiyalar, marshrutlash, DHCP, VLAN | bepul (Netacad hisobi) |
| **Wireshark** | paketlarni tahlil qilish | bepul |
| **VirtualBox** / Hyper-V | Windows Server va Linux VM lari | bepul |
| **Windows Server Evaluation** | A10–A14 mashgʻulotlari | 180 kun bepul |
| **Ubuntu Server** | Linux tarmoq xizmatlari | bepul |
| Buyruqlar satri | `ipconfig`, `ping`, `tracert`, `nslookup`, `netstat` | OT ichida |

Muhim qoida: **faqat oʻz** kompyuteringiz, uy tarmogʻingiz va virtual laboratoriyangizda tajriba qiling. Boshqa tarmoqlarni skanerlash yoki trafigini ushlash — qonunbuzarlik.

## Tayanch tushunchalar lugʻati

| Tushuncha | Qisqa izoh |
|---|---|
| Protokol | bir xil pogʻonalar orasidagi muloqot qoidalari |
| PDU | pogʻona maʼlumot birligi: bit, kadr, paket, segment, xabar |
| MAC manzil | 48 bitli fizik manzil, 2-pogʻona |
| IP manzil | 32 (IPv4) yoki 128 (IPv6) bitli mantiqiy manzil, 3-pogʻona |
| Maska / prefiks | manzilning tarmoq va xost qismini ajratadi |
| Port | dasturni aniqlovchi 16 bitli raqam, 4-pogʻona |
| Kolliziya domeni | signallar toʻqnashishi mumkin boʻlgan segment |
| Broadkast domeni | bitta broadkast yetib boradigan qurilmalar toʻplami |
| Shlyuz | boshqa tarmoqqa chiqish uchun router manzili |
| DHCP / DNS | manzilni avtomatik berish / nomni manzilga oʻgirish |
| NAT | xususiy manzillarni ommaviy manzilga almashtirish |
| Brandmauer / VPN / IDS | trafikni filtrlash / shifrlangan tunnel / hujumni aniqlash |

## Modullar boʻyicha oʻz-oʻzini tekshirish

1. OSI ning yetti pogʻonasini, har birining PDU si va bitta protokolini ayting.
2. Konsentrator, kommutator va marshrutizator kadr yoki paket bilan nima qiladi?
3. 1000BASE-T nomini izohlang. Minimal Ethernet kadri nima uchun 64 bayt?
4. 172.16.37.130/20 ning tarmoq va broadkast manzilini qoʻlda toping.
5. DHCP ning DORA jarayonini va ARP soʻrovining borishini tushuntiring.
6. TCP uch bosqichli ulanishida seq va ack raqamlari qanday oʻzgaradi?
7. Brauzerda `https://aifu.uz` kiritilgandan sahifa ochilguncha qanday protokollar ishlaydi? (DNS → TCP → TLS → HTTP.)
8. Share va NTFS ruxsatlari birgalikda qanday amal qiladi?
9. Brandmauer, VPN va IDS ning vazifalarini solishtiring.
10. "Internet ishlamayapti" holatida qaysi buyruqlarni qaysi tartibda bajarasiz?

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat oʻqish, amaliyotsiz.** Tarmoqlar — amaliy fan: `ping` va Wireshark ni oʻzingiz ishlatmaguningizcha bilim mustahkamlanmaydi.
- **Oʻtkazib yuborilgan mavzuni "keyin" qoldirish.** IP manzillashsiz keyingi 8 ta mavzu tushunarsiz boʻladi.
- **Imtihon oldidan bir kecha oʻqish.** Takrorlash oraliqlari (1 kun, 1 hafta, 1 oy) bilan bilim uzoq saqlanadi.
- **Begona tarmoqlarda tajriba qilish.** Faqat oʻz laboratoriyangizda.
:::

:::tip
Har bir modul oxirida bitta sahifali "shpargalka" tuzing: asosiy jadval, formulalar va buyruqlar. Semestr oxirida 15 ta sahifa — imtihonga eng yaxshi tayyorgarlik materiali.
:::

## Xulosa

- Fan 15 maʼruza va 15 amaliy mashgʻulotdan iborat; mavzular bir-biriga tayanadi — tartib bilan oʻzlashtiring.
- Har hafta: maʼruzani faol oʻqish, amaliyotni takrorlash, oʻz-oʻzini tekshirish.
- Bepul vositalar: Packet Tracer, Wireshark, VirtualBox, Windows Server Evaluation, Ubuntu Server.
- Tajribalar faqat oʻz kompyuteringiz va laboratoriyangizda.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun IP manzillash mavzusini oʻtkazib yuborish xavfli?
A: Marshrutlash, DHCP, DNS, NAT, brandmauer qoidalari va nosozliklarni aniqlash — hammasi IP manzil va maskani tushunishga tayanadi.
Q: Maʼruzani faol oʻqishning birinchi qadami nima?
A: Koʻzdan kechirish — sarlavhalar, jadvallar va oʻquv natijalari roʻyxati bilan tanishish.
Q: Windows Server ni uyda qanday qilib bepul sinab koʻrish mumkin?
A: Evaluation versiyasini (180 kun) VirtualBox yoki Hyper-V dagi virtual mashinaga oʻrnatib.
Q: Brauzerda sahifa ochilganda protokollar qaysi tartibda ishlaydi?
A: DNS (nomni manzilga) → TCP (ulanish) → TLS (shifrlash) → HTTP (soʻrov va javob).
Q: Qaysi tarmoqlarda skanerlash va trafikni ushlash mumkin?
A: Faqat oʻzingizniki yoki egasining ruxsati bilan (laboratoriya).
:::

:::link https://www.netacad.com/courses/networking-basics
Cisco Networking Academy: Networking Basics
Tarmoq asoslari boʻyicha bepul onlayn kurs — mustaqil oʻqish uchun qoʻshimcha manba (ingliz tilida).
:::
