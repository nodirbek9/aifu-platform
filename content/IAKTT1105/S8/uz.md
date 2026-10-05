---
title: Hisoblash jarayonlarining grafik tasviri
summary: Seminar — blok-sxema tushunchasi va standart belgilari (ISO 5807): boshlanish/tugash, kiritish/chiqarish, jarayon, shart, sikl, oldindan belgilangan jarayon, ulagich; blok-sxema chizish qoidalari; chiziqli, tarmoqlanuvchi va takrorlanuvchi jarayonlarning blok-sxemalari; blok-sxemadan dasturga oʻtish; ishlangan misol — pogʻonali elektr energiyasi tarifi (blok-sxema va dastur); blok-sxema chizish vositalari (draw.io, Visio, Word shakllari); platformadagi tarmoqlanuvchi algoritm topshirigʻi.
---

## Seminarning maqsadi

M8 maʼruzasida algoritm tushunchasi va tuzilmalarini koʻrdik. Seminarda algoritmni **grafik tarzda** — blok-sxema koʻrinishida tasvirlashni mashq qilamiz. Blok-sxema iqtisodchi, dasturchi va rahbar uchun umumiy "til": jarayonning qaysi joyida qaror qabul qilinishi, nima takrorlanishi bir qarashda koʻrinadi. Bundan keyin blok-sxemani dasturga aylantiramiz.

:::note
**Bu seminardan soʻng siz:**

- blok-sxemaning standart belgilarini bilasiz;
- chiziqli, tarmoqlanuvchi va takrorlanuvchi jarayonlarning blok-sxemasini chiza olasiz;
- blok-sxemani dasturga va aksincha, dasturni blok-sxemaga aylantira olasiz;
- blok-sxema chizish vositalaridan foydalana olasiz.
:::

## Blok-sxema belgilari

| Belgi | Shakl | Maʼnosi |
|---|---|---|
| **Boshlanish / tugash** | yumaloq burchakli toʻrtburchak (oval) | algoritmning boshi va oxiri |
| **Kiritish / chiqarish** | parallelogramm | maʼlumotni kiritish yoki natijani chiqarish |
| **Jarayon** | toʻrtburchak | hisoblash, qiymat berish |
| **Shart** (qaror) | romb | "ha/yoʻq" savoli, ikki chiqish yoʻli |
| **Sikl boshi** | oltiburchak (modifikatsiya) | takrorlash parametrlari |
| **Oldindan belgilangan jarayon** | ikki yon chiziqli toʻrtburchak | alohida tasvirlangan yordamchi algoritm |
| **Ulagich** | doira | katta sxemani qismlarga boʻlishda bogʻlash |
| **Oqim chiziqlari** | strelkalar | bajarilish tartibi (yuqoridan pastga, chapdan oʻngga) |

## Chizish qoidalari

- Bitta boshlanish va kamida bitta tugash bloki.
- Har bir blokdan (shartdan tashqari) bitta chiqish; shartdan aniq "ha" va "yoʻq" deb belgilangan ikkita chiqish.
- Chiziqlar kesishmasligi kerak; kerak boʻlsa ulagichdan foydalaning.
- Blok ichidagi yozuv qisqa va aniq: `S ← n × p`, `n > 200?`.
- Sikl, albatta, tugashi kerak — sikl sharti oxir-oqibat bajarilmay qolishini tekshiring.

## Uch asosiy tuzilma blok-sxemada

**Chiziqli** (QQS bilan narx):

```text
( Boshlanish ) → / narx kiritish / → [ jami ← narx × 1,12 ] → / jami chiqarish / → ( Tugash )
```

**Tarmoqlanuvchi** (shartga qarab ikki yoʻl):

```text
            / qiymat kiritish /
                    |
              < shart bajarildimi? >
               ha |          | yoʻq
          [ 1-hisob ]    [ 2-hisob ]
                    \      /
              / natija chiqarish /
```

**Takrorlanuvchi** (har bir oy uchun):

```text
[ i ← 1, jami ← 0 ] → < i ≤ n ? > —yoʻq→ / jami chiqarish /
                         | ha
              [ jami ← jami + savdo(i) ]
              [ i ← i + 1 ] → (sikl boshiga qaytish)
```

## Ishlangan misol: pogʻonali elektr tarifi

Oʻquv misoli (narxlar shartli): oyiga 200 kVt soatgacha har biri 600 soʻm, 200 dan ortiq qismi har biri 1 000 soʻm. Pogʻonali tarif kam isteʼmol qiluvchilarni qoʻllab-quvvatlaydi va tejamkorlikni ragʻbatlantiradi.

Blok-sxema:

```text
( Boshlanish )
      |
/ kvt kiritish /
      |
< kvt ≤ 200 ? >
 ha |                         | yoʻq
[ summa ← kvt × 600 ]   [ summa ← 200 × 600 + (kvt − 200) × 1000 ]
      \                      /
       / summa chiqarish /
              |
          ( Tugash )
```

Dastur (bir nechta isteʼmolchi uchun takrorlanadi):

```python
import sys

CHEGARA, NARX1, NARX2 = 200, 600, 1000             # oʻquv misoli uchun shartli tarif

for t in sys.stdin.read().split():
    kvt = int(t)                                    # kiritish
    if kvt <= CHEGARA:                              # shart (romb)
        summa = kvt * NARX1
    else:
        summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2
    print(f"{kvt} kVt soat: {summa} soʻm")          # chiqarish
```
```java
import java.util.Scanner;

public class Main {
    static final long CHEGARA = 200, NARX1 = 600, NARX2 = 1000;   // oʻquv misoli uchun shartli tarif

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long kvt = in.nextLong();                     // kiritish
            long summa;
            if (kvt <= CHEGARA) {                         // shart (romb)
                summa = kvt * NARX1;
            } else {
                summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2;
            }
            System.out.println(kvt + " kVt soat: " + summa + " soʻm");   // chiqarish
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

const long long CHEGARA = 200, NARX1 = 600, NARX2 = 1000;          // oʻquv misoli uchun shartli tarif

int main() {
    long long kvt;
    while (cin >> kvt) {                                  // kiritish
        long long summa;
        if (kvt <= CHEGARA) {                             // shart (romb)
            summa = kvt * NARX1;
        } else {
            summa = CHEGARA * NARX1 + (kvt - CHEGARA) * NARX2;
        }
        cout << kvt << " kVt soat: " << summa << " soʻm" << endl;   // chiqarish
    }
    return 0;
}
```

Kirish:

```text
150 200 201 350 0
```

Chiqish:

```text
150 kVt soat: 90000 soʻm
200 kVt soat: 120000 soʻm
201 kVt soat: 121000 soʻm
350 kVt soat: 270000 soʻm
0 kVt soat: 0 soʻm
```

Sinov qiymatlari ataylab tanlangan: chegaraning oʻzi (200), undan bitta ortiq (201), chegaradan past va yuqori, nol. Chegaradagi qiymatlar — tarmoqlanuvchi algoritmlarda xatolar eng koʻp yashiringan joy ("≤" oʻrniga "<" yozilsa, 200 kVt notoʻgʻri hisoblanadi).

## Vositalar

- **draw.io** (diagrams.net) — bepul, brauzerda, blok-sxema shakllari kutubxonasi bilan.
- **Microsoft Visio**, **Lucidchart** — professional diagrammalar.
- **Word/PowerPoint** shakllari va SmartArt — oddiy sxemalar uchun.
- **Mermaid** — matndan diagramma (hujjatlar va wiki larda).

## Amaliy topshiriqlar

1. Ishlangan misoldagi blok-sxemani draw.io da chizing va 3 pogʻonali tarifga kengaytiring (masalan, 200 gacha, 200–500, 500 dan ortiq).
2. M8 dagi "oylik savdo tahlili" dasturi uchun blok-sxema chizing (ikkita sikl).
3. Platformadagi **"Tarmoqlanuvchi algoritm: xarid chegirmasi"** topshirigʻi uchun avval blok-sxema chizing (bir nechta shartli romb ketma-ketligi), keyin uni dasturga aylantiring. Chegaraviy qiymatlar uchun sinov maʼlumotlarini oʻzingiz tuzing.
4. Hamkoringiz chizgan blok-sxemani "ijro eting": 3 ta kirish qiymati bilan qadamma-qadam natijani hisoblang va dastur natijasi bilan solishtiring.

:::warning
**Koʻp uchraydigan xatolar**

- **Shartdan bitta chiqish chizish.** Rombdan doim "ha" va "yoʻq" ikki yoʻl chiqadi.
- **Tugash blokiga yetib bormaydigan yoʻl.** Har bir yoʻl oxir-oqibat tugashga kelishi kerak.
- **Chegaraviy qiymatni yoʻqotish.** "≥" va ">" farqi — eng keng tarqalgan xato.
- **Cheksiz sikl.** Sikl oʻzgaruvchisi (i ← i + 1) oʻzgartirilmasa, sikl tugamaydi.
- **Blok ichida uzun matn.** Qisqa formula yozing, tushuntirishni izohga chiqaring.
:::

:::tip
Blok-sxemani tekshirishning eng yaxshi usuli — "qogʻozda ijro etish": jadval tuzing (ustunlar — oʻzgaruvchilar, satrlar — qadamlar) va har bir blokdan keyin qiymatlarni yozib boring.
:::

## Xulosa

- Blok-sxema — algoritmning grafik tasviri: oval (boshlanish/tugash), parallelogramm (kiritish/chiqarish), toʻrtburchak (jarayon), romb (shart), oltiburchak (sikl), strelkalar.
- Qoidalar: bitta boshlanish, shartdan ikki chiqish, chiziqlar kesishmasin, sikl tugashi kerak.
- Chiziqli, tarmoqlanuvchi va takrorlanuvchi tuzilmalar blok-sxemada va dasturda bir xil mantiqqa ega.
- Chegaraviy qiymatlarni sinash — tarmoqlanuvchi algoritmlarda xatolarni topishning asosiy usuli.

## Oʻzingizni tekshiring

:::check
Q: Blok-sxemada shart qaysi shakl bilan belgilanadi va undan nechta chiqish boʻladi?
A: Romb bilan; ikkita chiqish — "ha" va "yoʻq".
Q: Kiritish va chiqarish qaysi shakl bilan tasvirlanadi?
A: Parallelogramm bilan.
Q: Pogʻonali tarif misolida 201 kVt soat uchun summa qancha?
A: 121 000 soʻm (200 × 600 + 1 × 1000).
Q: Tarmoqlanuvchi algoritmni sinashda qaysi qiymatlar albatta tekshiriladi?
A: Chegaraviy qiymatlar — chegaraning oʻzi va undan bitta kichik va katta qiymatlar.
:::

:::link https://www.drawio.com/
draw.io (diagrams.net)
Blok-sxema va diagrammalar chizish uchun bepul onlayn vosita (ingliz tilida).
:::
