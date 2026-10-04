---
title: Takrorlanuvchi jarayonlarni tashkil etish (algoritm va dastur)
summary: Mustaqil ish: sikl bilan hisoblash masalasi uchun algoritm, qiymatlar jadvali, dastur va testlar; namunaviy masala — bank omonati necha yilda maqsadga yetadi.
---

## Nima uchun bu mavzu muhim?

Kredit jadvali, omonat foizi, aholi oʻsishi prognozi, qatorning yigʻindisi — bularning hammasi bir xil hisobni koʻp marta takrorlashga asoslanadi. Siklni toʻgʻri tuzish uchun uch narsani bilish kerak: nimadan boshlanadi, har qadamda nima oʻzgaradi, qachon toʻxtaydi. Bu mustaqil ishda shu uch savolga oʻzingiz javob berib, sikl bilan hisoblaydigan dasturni boshidan oxirigacha yozasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- masala uchun mos siklni (`for` yoki `while`) tanlab, tanlovingizni asoslay olasiz;
- siklning boshlangʻich qiymati, qadami va toʻxtash shartini aniq yoza olasiz;
- kichik kirish uchun qiymatlar jadvalini qoʻlda toʻldirib, dasturni tekshira olasiz;
- haqiqiy sonlar bilan hisoblab, natijani kerakli aniqlikda chiqara olasiz.
:::

## Mavzu boʻyicha qisqa takror

Har bir sikl uchun uch savolga javob bering:

| Savol | `for` da | `while` da |
|---|---|---|
| Nimadan boshlanadi? | hisoblagichning boshlangʻich qiymati | sikldan oldingi qiymatlar |
| Har qadamda nima oʻzgaradi? | hisoblagich + tana | tana ichida oʻzgaruvchi |
| Qachon toʻxtaydi? | hisoblagich chegaraga yetganda | shart yolgʻon boʻlganda |

Takrorlar soni maʼlum boʻlsa — `for`, hodisa roʻy berguncha boʻlsa — `while`. Yigʻindi uchun yigʻuvchi 0 dan, koʻpaytma uchun 1 dan boshlanadi. Haqiqiy son bilan ishlaganda butun boʻlishdan saqlaning: `1.0 / k`.

## Namunaviy masala: omonat

Omonatga *S* soʻm qoʻyildi, yillik foiz *p* % (har yil oxirida qoʻshiladi). Necha yildan keyin summa birinchi marta *M* soʻmdan kam boʻlmaydi? Yillar soni va oʻsha paytdagi summani (ikki kasr xona) chiqaring. Kirish: *S*, *p*, *M* bir qatorda (*S* < *M*, *p* > 0).

Takrorlar soni oldindan nomaʼlum — `while`. Toʻxtash sharti: `summa >= M`. Har qadamda: `summa = summa * (1 + p / 100)` va `yil += 1`. Sikl albatta tugaydi, chunki *p* > 0 da summa har yili oʻsadi.

```text
1. S, p, M ni oʻqi
2. summa = S, yil = 0
3. toki summa < M:
       summa = summa * (1 + p / 100)
       yil = yil + 1
4. yil va summa ni chiqar
```

```python
s, p, m = map(float, input().split())

summa = s
yil = 0
while summa < m:
    summa = summa * (1 + p / 100)
    yil += 1

print(yil, f"{summa:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double s = in.nextDouble();
        double p = in.nextDouble();
        double m = in.nextDouble();

        double summa = s;
        int yil = 0;
        while (summa < m) {
            summa = summa * (1 + p / 100);
            yil++;
        }

        System.out.println(yil + " " + String.format("%.2f", summa));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    double s, p, m;
    cin >> s >> p >> m;

    double summa = s;
    int yil = 0;
    while (summa < m) {
        summa = summa * (1 + p / 100);
        yil++;
    }

    cout << yil << " " << fixed << setprecision(2) << summa << endl;
    return 0;
}
```

Kirish `1000000 20 2000000` uchun qiymatlar jadvali:

| *yil* | *summa* (yil oxirida) | `summa < M`? |
|---|---|---|
| 0 | 1 000 000 | ha |
| 1 | 1 200 000 | ha |
| 2 | 1 440 000 | ha |
| 3 | 1 728 000 | ha |
| 4 | 2 073 600 | yoʻq — toʻxtash |

Chiqadi: `4 2073600.00`. Jadvalni dastur yozilishidan oldin qoʻlda toʻldirish — mustaqil ishning majburiy qismi.

## Mustaqil ish topshirigʻi

Quyida berilgan **garmonik qator yigʻindisi** masalasini yeching. Topshiriladigan ish:

1. **Tahlil** — uch savolga javob: sikl nimadan boshlanadi, har qadamda nima qoʻshiladi, qachon toʻxtaydi; nima uchun aynan shu sikl turi tanlandi.
2. **Qiymatlar jadvali** — *n* = 4 uchun har bir qadamdagi qoʻshiluvchi va yigʻindi (qoʻlda).
3. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
4. **Tajriba** — *n* = 10, 100, 1000 uchun natijalar va qisqa xulosa: *n* oshganda yigʻindi qanday oʻzgaradi?

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- `1 / k` Java va C++'da butun boʻlish beradi (0); haqiqiy boʻlish uchun `1.0 / k`.
- Sikl 1 dan boshlanishi kerak: `1 / 0` — nolga boʻlish.
- Natija shartda koʻrsatilgan aniqlikda (4 kasr xona) chiqarilishi shart.
- Qiymatlar jadvali dasturdan koʻchirilmasdan, qoʻlda toʻldirilgan boʻlishi kerak.
:::

:::tip
*n* = 1 (javob 1.0000) va *n* = 2 (javob 1.5000) uchun dasturingizni birinchi tekshiring — bular eng oddiy va eng koʻp xato topadigan testlar.
:::

## Xulosa

- Har bir sikl uchun boshlanish, qadam va toʻxtash shartini aniq yozing.
- Takrorlar soni nomaʼlum boʻlsa — `while`, maʼlum boʻlsa — `for`.
- Kichik kirish uchun qiymatlar jadvali dasturni yozishdan oldin tuziladi.
- Haqiqiy hisobda butun boʻlishdan saqlaning va natijani kerakli aniqlikda chiqaring.

## Oʻzingizni tekshiring

:::check
Q: Namunaviy masalada nima uchun `for` emas, `while` tanlangan?
A: Necha yil kerakligi oldindan nomaʼlum — sikl summa maqsadga yetguncha davom etadi.
Q: Agar *p* = 0 boʻlsa, namunaviy dastur nima qiladi?
A: Summa oʻzgarmaydi va sikl hech qachon tugamaydi — shuning uchun shartda *p* > 0 talab qilingan.
Q: Garmonik qatorda yigʻindi *n* oshgan sari chegaralanganmi?
A: Yoʻq: u juda sekin, lekin cheksiz oʻsadi (taxminan ln *n* + 0.577).
:::

:::link https://en.wikipedia.org/wiki/Harmonic_series_(mathematics)
Wikipedia: garmonik qator
Garmonik qatorning xossalari va oʻsish tezligi.
:::
