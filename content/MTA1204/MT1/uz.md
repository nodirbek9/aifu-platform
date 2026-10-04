---
title: Maʼruza va amaliy mashgʻulotlar mavzularini oʻzlashtirish
summary: Mustaqil ish: kurs mavzulari boʻyicha oʻzlashtirish rejasi, har bir modul uchun tayanch tushunchalar va tekshiruv savollari, algoritmni „qoʻlda bajarish“ usuli, mashq uchun masalalar toʻplamlari va oʻz bilimini baholash mezonlari.
---

## Nima uchun bu mavzu muhim?

Algoritmlar fanini faqat maʼruza tinglab oʻzlashtirib boʻlmaydi: algoritmni tushundim deyish uchun uni **qoʻlda bajarish**, **kodda yozish** va **murakkabligini tushuntirish** kerak. Bu mustaqil ish sizga kurs mavzularini tizimli takrorlash rejasini beradi: har bir modul uchun nimani bilish kerakligi, oʻzingizni qanday tekshirish va qayerda mashq qilish mumkinligi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- kurs mavzularini modullar boʻyicha tizimli takrorlay olasiz;
- har bir algoritmni kichik misolda qoʻlda bajarib, natijani tekshira olasiz;
- oʻz bilimingizni „taniyman — tushuntiraman — yozaman — tahlil qilaman“ shkalasida baholay olasiz;
- mashq uchun mos masalalarni topa olasiz.
:::

## Oʻzlashtirish shkalasi

Har bir mavzuni toʻrt darajada baholang:

| Daraja | Belgisi | Tekshiruv |
|---|---|---|
| 1. Taniyman | nomini va vazifasini bilaman | „Bu algoritm nima qiladi?“ ga bir gapda javob |
| 2. Tushuntiraman | ishlash tartibini soʻz bilan aytib bera olaman | doʻstingizga 2 daqiqada tushuntiring |
| 3. Yozaman | kitobga qaramasdan kodda yoza olaman | 20 daqiqada ishlaydigan dastur |
| 4. Tahlil qilaman | murakkabligini isbotlayman va chekka holatlarini bilaman | eng yomon kirishni oʻzingiz tuzing |

Imtihonga tayyorgarlikda har bir asosiy algoritm kamida 3-darajaga yetishi kerak.

## Modullar boʻyicha reja

| Modul | Tayanch tushunchalar | Oʻzingizni tekshiring |
|---|---|---|
| Algoritmlar va murakkablik | algoritm xossalari, Tyuring mashinasi, *O*/Ω/Θ, rekurrent munosabatlar | *T*(*n*) = 2*T*(*n*/2) + *n* ning yechimini asoslang |
| Izlash | chiziqli, binar, interpolyatsion, Fibonachchi izlash, xesh-jadval | binar izlash qadamlarini 16 elementli massivda qoʻlda bajaring |
| Saralash | pufaksimon, qoʻyib, Shell, piramidal, tez, birlashtirib saralash; barqarorlik; tashqi saralash | har bir saralashni `5 2 9 1 7` ustida qadamma-qadam yozing |
| Parallel saralash | saralash tarmoqlari, juft-toq saralash, Betcher tarmogʻi, 0-1 prinsipi | 4 elementli tarmoqni chizib, `4 3 2 1` ni oʻtkazing |
| Dinamik tuzilmalar | stek, navbat, dek, bogʻlangan roʻyxatlar | halqasimon buferni 5 ta buyruqda qoʻlda kuzating |
| Graflar | matritsa va roʻyxat, BFS, Deykstra, Floyd–Uorshell | 5 uchli grafda Deykstra jadvalini toʻldiring |
| Daraxtlar | BQD, aylanishlar, muvozanat, kesmalar va Fenvik daraxti | `8 3 10 1 6 14` ni BQD ga qoʻshib, uchala aylanishni yozing |

## Algoritmni qoʻlda bajarish

Eng samarali takrorlash usuli — algoritmni kichik kirishda **qogʻozda** bajarish:

1. 5–8 elementli kirish tanlang, unda chekka holat ham boʻlsin (teng elementlar, manfiy son, bitta element).
2. Har bir qadamda oʻzgaruvchilar holatini jadvalga yozing (indekslar, massiv, stek, masofalar).
3. Natijani tayyor dastur natijasi bilan solishtiring.
4. Taqqoslashlar (yoki qadamlar) sonini sanab, murakkablik bahosi bilan solishtiring.

Agar qaysidir qadamda „nima boʻlishini bilmayman“ desangiz — aynan shu joyni maʼruza matnidan qayta oʻqing.

## Mustaqil ish topshirigʻi

1. **Oʻzlashtirish jadvali** — yuqoridagi modullar jadvalidagi har bir algoritm uchun hozirgi darajangizni (1–4) belgilang.
2. **Qoʻlda bajarish** — darajasi 3 dan past boʻlgan har bir algoritm uchun bitta qoʻlda bajarish jadvali tuzing.
3. **Kod** — kamida beshta algoritmni (har bir moduldan bittadan) kitobga qaramasdan yozing va kursdagi namunaviy misollar bilan tekshiring.
4. **Mashq** — quyidagi toʻplamlardan har bir modul boʻyicha kamida ikkita masala yeching va yechim havolalarini yozib qoʻying.
5. **Xulosa** — qaysi mavzu eng qiyin boʻldi va uni qanday oʻzlashtirdingiz (5–7 gap).

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Qoʻlda bajarish jadvallari kod natijasidan koʻchirilmasdan, mustaqil tuzilgan boʻlishi kerak.
- Faqat „ishladi“ deb yozish yetarli emas: har bir algoritm uchun murakkablikni va eng yomon kirishni koʻrsating.
- Boshqa talabaning kodini koʻchirish — ish qabul qilinmaydi.
- Takrorlashni imtihon arafasiga qoldirmang: har hafta bitta modul — eng samarali reja.
:::

:::tip
Oʻrganganingizni boshqaga tushuntiring: guruhdoshingizga bitta algoritmni 5 daqiqada tushuntira olsangiz, siz uni haqiqatan bilasiz. Tushuntira olmagan joy — keyingi takrorlash mavzusi.
:::

## Xulosa

- Algoritmni oʻzlashtirish toʻrt darajali: taniyman, tushuntiraman, yozaman, tahlil qilaman.
- Har bir modul uchun tayanch tushunchalar va qoʻlda bajarish mashqi bor.
- Qoʻlda bajarish — xatolarni topish va murakkablikni his qilishning eng ishonchli usuli.
- Muntazam mashq va boshqalarga tushuntirish bilimni mustahkamlaydi.

## Oʻzingizni tekshiring

:::check
Q: Algoritmni „yozaman“ darajasida bilishning belgisi nima?
A: Uni kitobga qaramasdan qisqa vaqtda ishlaydigan dastur sifatida yozish.
Q: Qoʻlda bajarish uchun kirish qanday tanlanadi?
A: Kichik (5–8 element) va chekka holatni oʻz ichiga olgan: teng elementlar, manfiy son yoki bitta element.
Q: Nima uchun qoʻlda bajarishda qadamlar soni sanaladi?
A: Uni murakkablik bahosi bilan solishtirib, nazariy tahlilni amalda tekshirish uchun.
:::

:::link https://cses.fi/problemset/
CSES masalalar toʻplami
Saralash, izlash, graflar va daraxtlar boʻyicha saralangan masalalar (ingliz tilida).
:::

:::link https://visualgo.net/en
VisuAlgo
Kursdagi deyarli barcha algoritm va tuzilmalarning animatsiyasi (ingliz tilida).
:::
