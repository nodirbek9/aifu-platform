---
title: Windows Server operatsion tizimini oʻrnatish va boshlangʻich sozlash
summary: Uskuna talablari va virtual mashina tayyorlash (Hyper-V, VirtualBox); Windows Server ni oʻrnatish bosqichlari — nashr va oʻrnatish turini tanlash, disk boʻlimlari; birinchi kirish va Server Manager; boshlangʻich sozlash — kompyuter nomi, statik IP, DNS, vaqt mintaqasi, yangilanishlar, masofaviy boshqaruv; PowerShell ekvivalentlari; rollarni qoʻshish.
---

## Nima uchun bu mavzu muhim?

Server — tashkilotning "yuragi": unda foydalanuvchilar bazasi, fayllar, DHCP va DNS ishlaydi. Notoʻgʻri boshlangan server (tasodifiy nom, DHCP dan olingan manzil, yangilanmagan tizim) keyinchalik koʻp muammo keltiradi. Bu mashgʻulotda Windows Server ni virtual mashinaga oʻrnatib, uni keyingi mashgʻulotlardagi rollar (A11 — DHCP va DNS, A12 — foydalanuvchilar, A13 — umumiy resurslar) uchun tayyorlaymiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- Windows Server uchun virtual mashina tayyorlay olasiz;
- toʻgʻri nashr va oʻrnatish turini tanlab, tizimni oʻrnata olasiz;
- serverga mazmunli nom, statik IP manzil va DNS bera olasiz;
- Server Manager va PowerShell bilan asosiy sozlashlarni bajara olasiz;
- serverga rol qoʻshish tartibini bilasiz.
:::

## Tayyorgarlik

| Parametr | Minimal talab (Windows Server 2022) | Laboratoriya uchun tavsiya |
|---|---|---|
| Protsessor | 1,4 GHz, 64 bit | 2 ta virtual yadro |
| Xotira | 512 MB (Server Core), 2 GB (Desktop Experience) | 4 GB |
| Disk | 32 GB | 60 GB |
| Tarmoq | 1 ta adapter | ichki yoki "Host-only" tarmoq |

**Virtual mashina** (Hyper-V, VirtualBox yoki VMware) ishlatiladi: real kompyuterga xavf yoʻq, holatni **snapshot** qilib, xato boʻlsa qaytish mumkin. Oʻrnatish obrazi (ISO) — Microsoft Evaluation Center dan 180 kunlik sinov versiyasi.

Tarmoq rejimi: **NAT** — VM Internetga chiqadi, lekin tashqaridan koʻrinmaydi; **Ichki/Host-only** — faqat VM lar va xost oʻzaro koʻrinadi (DHCP server sinovi uchun xavfsiz — universitet tarmogʻiga ikkinchi DHCP chiqib ketmaydi); **Bridge** — VM haqiqiy tarmoqda alohida kompyuter kabi.

## Oʻrnatish bosqichlari

1. VM ni ISO dan yuklang, til va klaviaturani tanlang, **Install now**.
2. Nashrni tanlang: **Windows Server 2022 Standard (Desktop Experience)** — grafik interfeysli. Server Core — faqat buyruqlar satri.
3. Litsenziya shartlarini qabul qiling, **Custom: Install Windows only** ni tanlang.
4. Disk: boʻsh joyni tanlang (oʻrnatuvchi tizim boʻlimlarini oʻzi yaratadi).
5. Oʻrnatish va qayta yuklanishlardan soʻng **Administrator** paroli soʻraladi — murakkab parol bering (katta va kichik harf, raqam, belgi, kamida 8 belgi).
6. Kirish: VM da **Ctrl+Alt+Del** oʻrniga gipervizor menyusidagi tegishli tugma (VirtualBox: Host+Del).

## Boshlangʻich sozlash

Kirishdan keyin **Server Manager** ochiladi → **Local Server**. Har bir bandni ketma-ket sozlang:

| Band | Nima qilish kerak | Nima uchun |
|---|---|---|
| **Computer name** | `SRV-DC01` kabi mazmunli nom, keyin qayta yuklash | tasodifiy `WIN-8F3K…` nomi bilan rollarni oʻrnatgach, nomni almashtirish muammoli |
| **Ethernet** (IP) | statik: `192.168.50.10/24`, shlyuz `192.168.50.1`, DNS `127.0.0.1` (keyinchalik DNS server boʻlsa) yoki ichki DNS | mijozlar serverni doimiy manzil boʻyicha topadi |
| **Time zone** | (UTC+05:00) Tashkent | Kerberos va jurnallar vaqtga bogʻliq |
| **Windows Update** | barcha yangilanishlarni oʻrnatish | maʼlum zaifliklarni yopish |
| **Remote Desktop** | kerak boʻlsa yoqish, faqat administratorlar uchun | masofaviy boshqaruv |
| **IE Enhanced Security** | serverda brauzerdan foydalanmang | server — ish stoli emas |

Xuddi shu sozlamalar **PowerShell** da (administrator sifatida):

```text
Rename-Computer -NewName "SRV-DC01" -Restart
Get-NetAdapter
New-NetIPAddress -InterfaceAlias "Ethernet" -IPAddress 192.168.50.10 -PrefixLength 24 -DefaultGateway 192.168.50.1
Set-DnsClientServerAddress -InterfaceAlias "Ethernet" -ServerAddresses 192.168.50.10
Set-TimeZone -Id "West Asia Standard Time"
Get-ComputerInfo | Select-Object CsName, WindowsProductName, OsVersion
```

Server Core da xuddi shu ishlar `sconfig` menyusi orqali bajariladi: 2 — nom, 8 — tarmoq, 6 — yangilanishlar, 7 — Remote Desktop.

## Rollarni qoʻshish

Server Manager → **Manage → Add Roles and Features**:

1. **Role-based or feature-based installation**.
2. Serverni tanlang (hovuzdan).
3. Rollarni belgilang — masalan, **DHCP Server**, **DNS Server** (A11 da), **File and Storage Services** (A13 da). Kerakli qoʻshimcha komponentlar avtomatik taklif qilinadi.
4. **Install**. Baʼzi rollar oʻrnatilgach, sariq bayroqcha bilan "post-deployment configuration" — keyingi sozlashni talab qiladi.

PowerShell da: `Install-WindowsFeature -Name DHCP, DNS -IncludeManagementTools`. Oʻrnatilganlarni koʻrish: `Get-WindowsFeature | Where-Object Installed`.

## Tekshirish roʻyxati

- [ ] Kompyuter nomi mazmunli va qayta yuklangan.
- [ ] Statik IP, maska, shlyuz va DNS toʻgʻri (`ipconfig /all`).
- [ ] Vaqt mintaqasi va vaqt toʻgʻri.
- [ ] Yangilanishlar oʻrnatilgan.
- [ ] Administrator paroli murakkab va hech kimga oshkor qilinmagan.
- [ ] VM holati snapshot sifatida saqlangan ("Toza oʻrnatish").

:::warning
**Koʻp uchraydigan xatolar**

- **Serverga DHCP dan manzil qoldirish.** Manzil oʻzgarsa, mijozlar serverni yoʻqotadi; server manzili doim statik.
- **Rollar oʻrnatilgandan keyin nomni almashtirish.** Ayniqsa domen kontrollerida bu jiddiy muammo; nom birinchi navbatda beriladi.
- **Sinov DHCP serverini universitet tarmogʻiga ulash (Bridge).** Begona DHCP butun tarmoq kompyuterlariga notoʻgʻri manzil tarqatadi; laboratoriyada ichki tarmoq ishlating.
- **Kundalik ishni Administrator hisobida bajarish va serverda internetdan dastur yuklash.** Server — faqat xizmat uchun.
- **Snapshot olmasdan katta oʻzgarish qilish.** Xatodan keyin qaytish imkoni qolmaydi.
:::

:::tip
Ikkinchi VM sifatida Windows 10/11 mijozini oʻrnating va uni xuddi shu ichki tarmoqqa ulang. Mijozga `192.168.50.20/24` manzilini bering va serverga `ping` qiling. Javob kelmasa — sababi koʻpincha Windows brandmaueri ICMP ni yopgani; `Get-NetFirewallRule -DisplayName "*Echo*"` bilan tekshiring.
:::

## Xulosa

- Windows Server laboratoriyada virtual mashinaga, ichki tarmoqda va snapshot bilan oʻrnatiladi.
- Nashr (Standard/Datacenter) va oʻrnatish turi (Desktop Experience/Server Core) oldindan tanlanadi.
- Boshlangʻich sozlash: nom → statik IP va DNS → vaqt mintaqasi → yangilanishlar → masofaviy boshqaruv.
- Rollar Server Manager dagi **Add Roles and Features** yoki `Install-WindowsFeature` bilan qoʻshiladi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun serverga statik IP manzil beriladi?
A: Mijozlar uni doimiy manzil boʻyicha topishi kerak; DHCP manzili oʻzgarishi mumkin.
Q: Server Core ning Desktop Experience dan asosiy farqi nimada?
A: Grafik interfeys yoʻq — boshqaruv buyruqlar satri, `sconfig` va PowerShell orqali; kamroq resurs va kamroq zaiflik.
Q: Sinov DHCP serveri uchun VM tarmogʻining qaysi rejimi xavfsiz?
A: Ichki (Internal) yoki Host-only — DHCP xabarlari haqiqiy tarmoqqa chiqmaydi.
Q: Server Manager da rol qoʻshish menyusi qayerda?
A: Manage → Add Roles and Features.
Q: Kompyuter nomini PowerShell da qanday almashtirish mumkin?
A: `Rename-Computer -NewName "SRV-DC01" -Restart`.
:::

:::link https://learn.microsoft.com/en-us/windows-server/get-started/install-windows-server
Install Windows Server
Microsoft ning Windows Server ni oʻrnatish boʻyicha rasmiy qoʻllanmasi (ingliz tilida).
:::
