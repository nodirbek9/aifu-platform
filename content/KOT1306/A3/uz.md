---
title: Tarmoq emulyatorlari va ulardan foydalanish
summary: Simulyator va emulyator farqi; Cisco Packet Tracer, GNS3, EVE-NG, Mininet va boshqa vositalar; Packet Tracer interfeysi — qurilmalar, kabellar, real vaqt va simulyatsiya rejimlari; birinchi tarmoqni yigʻish, IP manzil berish va ping bilan tekshirish; qurilmani buyruqlar satri orqali sozlash asoslari.
---

## Nima uchun bu mavzu muhim?

Haqiqiy marshrutizator va kommutatorlar qimmat, laboratoriyada esa ularning soni cheklangan. Xato sozlash real tarmoqni toʻxtatib qoʻyishi mumkin. Shuning uchun tarmoq muhandislari yangi sxemani avval **kompyuter ichida** — simulyator yoki emulyatorda sinab koʻradi. Bu kursning keyingi amaliy mashgʻulotlarida (marshrutlash, DHCP, VLAN) ham emulyatordan foydalanamiz, shuning uchun bugun uning asosiy imkoniyatlarini oʻzlashtiramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- simulyator va emulyatorning farqini tushuntira olasiz;
- asosiy vositalarni (Packet Tracer, GNS3, EVE-NG, Mininet) va ular qaysi vazifaga mosligini ayta olasiz;
- Packet Tracer da qurilmalarni joylashtirib, mos kabellar bilan ulay olasiz;
- kompyuterlarga IP manzil berib, `ping` bilan aloqani tekshira olasiz;
- simulyatsiya rejimida paketning yoʻlini kuzata olasiz.
:::

## Simulyator va emulyator

| | **Simulyator** | **Emulyator** |
|---|---|---|
| Nima qiladi | qurilma xatti-harakatini **modellashtiradi** (soddalashtirilgan dastur) | qurilmaning **haqiqiy operatsion tizimini** (masalan, Cisco IOS obrazini) virtual mashinada ishga tushiradi |
| Buyruqlar | faqat qoʻllab-quvvatlangan buyruqlar toʻplami | haqiqiy qurilmadagi barcha buyruqlar |
| Resurs | kam (oddiy noutbukda ishlaydi) | koʻp (har bir qurilma — alohida virtual mashina) |
| Misol | Cisco Packet Tracer | GNS3, EVE-NG |
| Qachon | oʻqish, tushunchalarni oʻrganish | sertifikatlashga tayyorlanish, real loyihani sinash |

## Asosiy vositalar

| Vosita | Turi | Xususiyati |
|---|---|---|
| **Cisco Packet Tracer** | simulyator | bepul (Cisco Networking Academy hisobi bilan), oʻquv uchun eng qulay; simulyatsiya rejimida har bir paketni koʻrsatadi |
| **GNS3** | emulyator | ochiq kodli; haqiqiy IOS, Juniper, MikroTik obrazlari va virtual mashinalarni bogʻlaydi |
| **EVE-NG** | emulyator | brauzer orqali ishlaydigan laboratoriya serveri; koʻp ishlab chiqaruvchilar obrazlari |
| **Mininet** | emulyator | Linux da yuzlab virtual hostlar va kommutatorlar; SDN tadqiqotlari uchun |
| **Wireshark** | trafik analizatori | tarmoqni **modellashtirmaydi** — haqiqiy yoki emulyatordagi paketlarni ushlab, tahlil qiladi |

## Packet Tracer interfeysi

- **Ish maydoni** (Logical Workspace) — sxema chiziladigan joy.
- **Qurilmalar paneli** (pastki chap burchak) — Network Devices (marshrutizatorlar, kommutatorlar, konsentratorlar, simsiz qurilmalar), End Devices (PC, Laptop, Server, Printer), Connections (kabellar).
- **Kabellar**: toʻgʻri (Copper Straight-Through), kross (Copper Cross-Over), konsol (Console), optik (Fiber), seriya (Serial DCE/DTE). Chaqmoq belgisi — "avtomatik kabel tanlash".
- **Rejimlar** (pastki oʻng burchak): **Realtime** — tarmoq real vaqtda ishlaydi; **Simulation** — vaqt toʻxtatiladi, har bir paket konvert sifatida koʻrsatiladi va qadamma-qadam yuritiladi.
- **Qurilma oynasi** (qurilmani bosganda): Physical (modullar qoʻshish), Config (grafik sozlash), Desktop (kompyuter uchun: IP Configuration, Command Prompt, Web Browser), CLI (marshrutizator va kommutator buyruqlar satri).

Ulanish chiroqlari: **yashil** — aloqa bor, **toʻq sariq** — port ishga tushmoqda yoki STP bloklagan, **qizil** — aloqa yoʻq (port oʻchirilgan yoki kabel notoʻgʻri).

## Amaliy ish 1: birinchi tarmoq

1. Ish maydoniga bitta **2960** kommutator va uchta **PC** joylashtiring.
2. Har bir PC ni kommutatorga **toʻgʻri kabel** bilan ulang (PC: FastEthernet0 → Switch: Fa0/1, Fa0/2, Fa0/3). Chiroqlar yashil boʻlishini kuting.
3. Har bir PC da *Desktop → IP Configuration* oynasida manzil bering:

```text
PC0   IP 192.168.10.1   Mask 255.255.255.0
PC1   IP 192.168.10.2   Mask 255.255.255.0
PC2   IP 192.168.10.3   Mask 255.255.255.0
```

4. PC0 da *Desktop → Command Prompt* ni ochib, aloqani tekshiring:

```text
C:\> ping 192.168.10.3

Pinging 192.168.10.3 with 32 bytes of data:
Reply from 192.168.10.3: bytes=32 time<1ms TTL=128
Reply from 192.168.10.3: bytes=32 time<1ms TTL=128
...
```

Birinchi soʻrov baʼzan "Request timed out" bilan tugaydi — bu vaqtda kompyuter ARP orqali qabul qiluvchining MAC manzilini aniqlayotgan boʻladi.

## Amaliy ish 2: simulyatsiya rejimi

1. **Simulation** rejimiga oʻting. *Edit Filters* da faqat **ICMP** va **ARP** ni qoldiring.
2. PC0 dan PC2 ga yana `ping` bering yoki yuqori paneldagi "Add Simple PDU" (yopiq konvert) belgisi bilan PC0 va PC2 ni ketma-ket bosing.
3. **Capture/Forward** tugmasini bosib, paketlarni qadamma-qadam kuzating: avval ARP broadkasti kommutator orqali barcha PC larga boradi, keyin javob faqat PC0 ga qaytadi, soʻng ICMP paketlari faqat kerakli portlar orqali yuradi.
4. Istalgan konvertni bosing — **OSI Model** varagʻida paket har bir pogʻonada qanday qayta ishlangani, **Inbound/Outbound PDU Details** da esa sarlavhalar maydonlari koʻrinadi.

## Amaliy ish 3: buyruqlar satri bilan tanishish

Kommutatorni bosib, **CLI** varagʻiga oʻting:

```text
Switch> enable                         imtiyozli rejimga oʻtish
Switch# show mac-address-table         MAC jadvali (ping dan keyin toʻladi)
Switch# configure terminal             sozlash rejimi
Switch(config)# hostname SW1           qurilma nomini oʻzgartirish
SW1(config)# exit
SW1# show running-config               joriy sozlamalar
SW1# copy running-config startup-config   sozlamalarni saqlash
```

Cisco qurilmalarida rejimlar ierarxik: foydalanuvchi rejimi (`>`), imtiyozli rejim (`#`), global sozlash (`(config)#`) va interfeys sozlash (`(config-if)#`). Buyruqlarni toʻliq yozish shart emas — `conf t`, `sh run` kabi qisqartmalar ishlaydi, **Tab** esa buyruqni toʻldiradi, `?` — yordam.

:::warning
**Koʻp uchraydigan xatolar**

- **Wireshark ni emulyator deb hisoblash.** Wireshark trafikni faqat ushlaydi va tahlil qiladi — tarmoqni yaratmaydi.
- **Sxemani faylga saqlamasdan yopish.** Packet Tracer ishini `.pkt` fayl sifatida saqlang; qurilma ichidagi sozlamalar esa `copy running-config startup-config` bilan saqlanadi, aks holda qurilma qayta yuklanganda yoʻqoladi.
- **Turli quyi tarmoq manzillarini berish.** 192.168.10.1 va 192.168.20.2 kompyuterlari marshrutizatorsiz bir-biriga `ping` qila olmaydi.
- **Simulyatsiya rejimida qolib ketish.** Simulation rejimida vaqt toʻxtaydi — Capture/Forward bosilmasa, `ping` "javob bermayapti" boʻlib koʻrinadi.
:::

:::tip
Packet Tracer dagi **Activity Wizard** bilan yaratilgan tayyor topshiriqlar (`.pka` fayllar) avtomatik baholanadi. Netacad saytidagi "Getting Started with Cisco Packet Tracer" kursidagi mashqlarni bajarib, interfeysni tezroq oʻzlashtiring.
:::

## Xulosa

- Simulyator qurilma xatti-harakatini modellashtiradi (Packet Tracer), emulyator haqiqiy qurilma dasturini ishga tushiradi (GNS3, EVE-NG).
- Packet Tracer: qurilmalar paneli, kabel turlari, Realtime va Simulation rejimlari, qurilma oynasidagi Desktop va CLI.
- Birinchi tarmoq: kommutator, PC lar, toʻgʻri kabellar, bir quyi tarmoqdagi IP manzillar va `ping`.
- Simulyatsiya rejimida ARP va ICMP paketlarining yoʻli va har bir pogʻonadagi sarlavhalar koʻrinadi.

## Oʻzingizni tekshiring

:::check
Q: Simulyator va emulyatorning asosiy farqi nimada?
A: Simulyator qurilma xatti-harakatini soddalashtirilgan model bilan takrorlaydi, emulyator esa haqiqiy qurilmaning operatsion tizimini virtual muhitda ishga tushiradi.
Q: Packet Tracer da paketlarni qadamma-qadam kuzatish uchun qaysi rejim kerak?
A: Simulation rejimi (Capture/Forward tugmasi bilan).
Q: Birinchi `ping` soʻrovi nima uchun baʼzan "Request timed out" boʻladi?
A: Yuboruvchi avval ARP orqali qabul qiluvchining MAC manzilini aniqlaydi, shu vaqt ichida birinchi soʻrov kechikadi.
Q: Cisco kommutatorida sozlash rejimiga qaysi buyruqlar bilan oʻtiladi?
A: `enable`, keyin `configure terminal`.
Q: Port chiroqlari qizil boʻlsa, bu nimani bildiradi?
A: Aloqa yoʻq — port oʻchirilgan yoki kabel notoʻgʻri tanlangan.
:::

:::link https://www.netacad.com/courses/getting-started-cisco-packet-tracer
Getting Started with Cisco Packet Tracer
Cisco Networking Academy ning Packet Tracer boʻyicha bepul kirish kursi (ingliz tilida).
:::
