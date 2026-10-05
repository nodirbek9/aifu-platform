---
title: Jadval redaktorlardan iqtisodiy masalalarni yechish jarayonida foydalanish
summary: Elektron jadval tuzilishi — ishchi kitob, varaq, katak, manzil; maʼlumot turlari va formatlar; formulalar va nisbiy, absolyut, aralash murojaatlar; asosiy funksiyalar — matematik (SUM, ROUND), statistik (AVERAGE, MAX, MIN, COUNT), mantiqiy (IF, AND, OR), qidiruv (VLOOKUP, XLOOKUP), shartli yigʻindi (SUMIF, COUNTIF), moliyaviy (PMT, FV, NPV); diagrammalar; saralash va filtrlash; yigʻma jadvallar; ishlangan iqtisodiy misollar — savdo hisoboti va kredit toʻlovi.
---

## Nima uchun bu mavzu muhim?

Elektron jadvallar (Microsoft Excel, Google Sheets, LibreOffice Calc) — iqtisodchining eng koʻp ishlatadigan vositasi: byudjet, smeta, savdo hisoboti, ish haqi, kredit jadvali, investitsiya loyihasini baholash. Jadval formulalari bilan bir marta tuzilgan model maʼlumot oʻzgarganda avtomatik qayta hisoblanadi — bu vaqtni tejaydi va "agar … boʻlsa" tahlilini osonlashtiradi.

:::note
**Bu darsdan soʻng siz:**

- elektron jadval tuzilishi va maʼlumot turlarini bilasiz;
- nisbiy, absolyut va aralash murojaatlardan toʻgʻri foydalana olasiz;
- asosiy matematik, statistik, mantiqiy, qidiruv va moliyaviy funksiyalarni qoʻllay olasiz;
- maʼlumotlarni saralash, filtrlash va diagrammada tasvirlay olasiz;
- oddiy iqtisodiy modellarni jadvalda tuza olasiz.
:::

## Elektron jadval tuzilishi

- **Ishchi kitob** (fayl, `.xlsx`) bir nechta **varaq**dan iborat.
- Varaq **ustunlar** (A, B, C, …) va **satrlar** (1, 2, 3, …) dan tashkil topgan; ularning kesishmasi — **katak**, manzili masalan `B3`.
- **Diapazon** — kataklar guruhi: `A1:A10`, `B2:D5`.
- **Maʼlumot turlari**: son, matn, sana va vaqt, mantiqiy (TRUE/FALSE), formula. Sonlar formati: pul (soʻm), foiz, minglik ajratgich, oʻnli kasrlar soni.

## Formulalar va murojaatlar

Formula `=` belgisi bilan boshlanadi: `=B2*C2`, `=SUM(D2:D10)`. Amallar: `+ − * / ^` va qavslar.

| Murojaat turi | Yozilishi | Nusxalanganda | Qachon ishlatiladi |
|---|---|---|---|
| **Nisbiy** | `B2` | satr va ustun siljiydi | har satr uchun bir xil hisob (narx × miqdor) |
| **Absolyut** | `$B$2` | oʻzgarmaydi | bitta umumiy qiymat: QQS stavkasi, valyuta kursi |
| **Aralash** | `$B2` yoki `B$2` | faqat bittasi siljiydi | koʻpaytirish jadvali, ikki oʻlchamli hisoblar |

`F4` tugmasi murojaat turini almashtiradi.

## Asosiy funksiyalar

| Guruh | Funksiya | Vazifasi | Misol |
|---|---|---|---|
| Matematik | `SUM`, `ROUND`, `ROUNDDOWN` | yigʻindi, yaxlitlash | `=SUM(D2:D10)`, `=ROUND(E2;0)` |
| Statistik | `AVERAGE`, `MAX`, `MIN`, `COUNT`, `MEDIAN` | oʻrtacha, eng katta/kichik, soni | `=AVERAGE(D2:D10)` |
| Mantiqiy | `IF`, `AND`, `OR` | shartga qarab qiymat | `=IF(D2>=1000000;"katta";"oddiy")` |
| Shartli | `SUMIF`, `COUNTIF`, `AVERAGEIF` | shartga mos kataklar boʻyicha | `=SUMIF(B2:B10;"Toshkent";D2:D10)` |
| Qidiruv | `VLOOKUP`, `XLOOKUP`, `INDEX`/`MATCH` | jadvaldan qiymat topish | `=VLOOKUP(A2;Narxlar!A:B;2;FALSE)` |
| Matn va sana | `CONCAT`, `LEFT`, `TODAY`, `YEAR` | matn va sanalar bilan ishlash | `=YEAR(TODAY())` |
| Moliyaviy | `PMT`, `FV`, `PV`, `NPV`, `IRR` | kredit toʻlovi, kelajakdagi qiymat, investitsiya samaradorligi | `=PMT(24%/12;12;-10000000)` |

Eslatma: funksiya argumentlari ajratgichi til sozlamalariga bogʻliq — `;` yoki `,`. Funksiya nomlari interfeys tiliga qarab tarjima qilinishi mumkin (masalan, rus tilidagi Excel da `СУММ`).

## Ishlangan misol 1: savdo hisoboti

| | A | B | C | D | E |
|---|---|---|---|---|---|
| 1 | Tovar | Narx | Miqdor | Summa | QQS bilan |
| 2 | Choy | 25 000 | 40 | `=B2*C2` → 1 000 000 | `=D2*(1+$H$1)` → 1 120 000 |
| 3 | Qahva | 60 000 | 15 | 900 000 | 1 008 000 |
| 4 | Shakar | 14 000 | 50 | 700 000 | 784 000 |
| 5 | Jami | | `=SUM(C2:C4)` → 105 | `=SUM(D2:D4)` → 2 600 000 | 2 912 000 |

`H1` katagida QQS stavkasi 12% joylashgan (Oʻzbekistondagi QQS stavkasi; amaldagi stavkani soliq qonunchiligidan tekshiring). `E2` formulasida `$H$1` absolyut — formulani pastga nusxalaganda stavka katagi oʻzgarmaydi, `D2` esa `D3`, `D4` ga oʻzgaradi. Qoʻshimcha koʻrsatkichlar: oʻrtacha summa `=AVERAGE(D2:D4)` → 866 667; eng koʻp sotilgan `=MAX(D2:D4)` → 1 000 000; har bir tovar ulushi `=D2/$D$5` (foiz formatida) → 38,5%.

## Ishlangan misol 2: kredit toʻlovi

Bank 10 000 000 soʻm kreditni yillik 24% bilan 12 oyga annuitet (teng toʻlovlar) usulida beradi. Oylik toʻlov:

`=PMT(24%/12; 12; -10000000)` → **945 596** soʻm (taxminan).

Bu yerda `24%/12` — oylik stavka (2%), `12` — toʻlovlar soni, kredit summasi manfiy (bankdan olingan pul — sizga kirim, toʻlovlar — chiqim). Jami toʻlov ≈ 945 596 × 12 = 11 347 152 soʻm, ortiqcha toʻlov (foizlar) ≈ 1 347 152 soʻm. Stavka yoki muddatni oʻzgartirib, natija qanday oʻzgarishini darhol koʻrish mumkin — bu "agar … boʻlsa" tahlilining afzalligi. Haqiqiy kreditlarda komissiyalar va sugʻurta ham boʻlishi mumkin, shuning uchun bank bergan toʻlov jadvalini tekshiring.

## Saralash, filtrlash va diagrammalar

- **Saralash**: Data → Sort — bir yoki bir nechta ustun boʻyicha (masalan, summa kamayishi boʻyicha).
- **Filtr**: Data → Filter — faqat kerakli satrlarni koʻrsatish (masalan, faqat bitta hudud).
- **Shartli formatlash**: qiymatlarni rang bilan ajratish (reja bajarilmagan satrlar qizil).
- **Diagrammalar**: ustunli (taqqoslash), chiziqli (vaqt boʻyicha dinamika), doiraviy (ulushlar, 5–6 dan ortiq boʻlmagan qism), nuqtali (ikki koʻrsatkich bogʻliqligi).
- **Yigʻma jadval** (PivotTable): katta jadvalni guruhlab, yigʻindi va oʻrtachalarni tez hisoblash — S4 seminarida batafsil.

:::warning
**Koʻp uchraydigan xatolar**

- **Formulaga son yozib qoʻyish.** `=B2*1.12` oʻrniga stavkani alohida katakka qoʻying (`$H$1`) — oʻzgartirish oson.
- **Absolyut murojaatni unutish.** Nusxalanganda formula boʻsh katakka murojaat qilib, notoʻgʻri natija beradi.
- **Sonni matn sifatida saqlash.** Chap tomonga tekislangan "sonlar" yigʻindiga kirmaydi.
- **Formatni qiymat bilan adashtirish.** Ekranda 866 667 koʻrinsa ham, katakda 866 666,67 saqlangan boʻlishi mumkin.
- **Doiraviy diagrammaga juda koʻp qism qoʻyish.** Oʻqib boʻlmaydigan diagramma hosil boʻladi.
:::

:::tip
Ishlangan misol 1 dagi jadvalni oʻzingiz tuzing va QQS stavkasini `H1` da oʻzgartirib, barcha natijalar avtomatik yangilanishini kuzating. Keyin `PMT` funksiyasi bilan oʻz kredit misolingizni hisoblang.
:::

## Xulosa

- Elektron jadval: kitob, varaq, katak, diapazon; maʼlumot turlari va formatlar.
- Formulalar `=` bilan; nisbiy, absolyut (`$B$2`) va aralash murojaatlar.
- Funksiyalar: SUM, AVERAGE, MAX/MIN, COUNT, IF, SUMIF/COUNTIF, VLOOKUP/XLOOKUP, PMT, FV, NPV.
- Saralash, filtr, shartli formatlash, diagrammalar va yigʻma jadvallar tahlilni tezlashtiradi.
- Jadval modeli maʼlumot oʻzgarganda avtomatik qayta hisoblanadi — "agar … boʻlsa" tahlili uchun qulay.

## Oʻzingizni tekshiring

:::check
Q: A1:A10 kataklaridagi sonlar yigʻindisini hisoblovchi formula qanday?
A: =SUM(A1:A10).
Q: Formulani nusxalaganda oʻzgarmaydigan murojaat qanday yoziladi?
A: Absolyut murojaat, masalan $B$2.
Q: A1 = 12, A2 = 8, A3 = 10 boʻlsa, =AVERAGE(A1:A3) natijasi qancha?
A: 10.
Q: Annuitet kreditning oylik toʻlovi qaysi funksiya bilan hisoblanadi?
A: PMT.
:::

:::link https://support.microsoft.com/excel
Microsoft Excel yordami
Excel funksiyalari, formulalar va yigʻma jadvallar boʻyicha rasmiy qoʻllanmalar.
:::
