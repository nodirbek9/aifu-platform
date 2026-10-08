---
title: Vektorlarni tashkil etish
summary: Mustaqil ish: vektorni massiv sifatida saqlash, vektorlar ustida amallar (yigʻindi, songa koʻpaytirish, uzunlik, masofa); C++ vector va dinamik oʻlcham; namunaviy masala — ikki vektor yigʻindisi va ular orasidagi masofa.
---

## Nima uchun bu mavzu muhim?

Matematikadagi **vektor** — tartiblangan sonlar toʻplami: tekislikdagi nuqta (*x*, *y*), fazodagi tezlik (*vx*, *vy*, *vz*), talabaning besh fan boʻyicha baholari. Tavsiya tizimlari (YouTube, internet-doʻkonlar) foydalanuvchi va mahsulotni yuzlab oʻlchamli vektorlar bilan ifodalaydi va ular orasidagi „masofa“ yoki skalyar koʻpaytma orqali oʻxshashlikni topadi. Dasturda vektor oddiy bir oʻlchovli massiv sifatida saqlanadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- *n* oʻlchamli vektorni massivda saqlay olasiz;
- vektorlar yigʻindisi, songa koʻpaytirish, uzunlik va masofani hisoblay olasiz;
- C++'da `vector`, Javaʼda massiv, Pythonʼda roʻyxat bilan dinamik oʻlchamli vektor yarata olasiz;
- natijani formula boʻyicha qoʻlda hisoblab, dasturni tekshira olasiz.
:::

## Mavzu boʻyicha qisqa takror

*a* = (*a*₁, …, *aₙ*) va *b* = (*b*₁, …, *bₙ*) vektorlar uchun:

| Amal | Formula | Dasturda |
|---|---|---|
| Yigʻindi | (*a*₁ + *b*₁, …, *aₙ* + *bₙ*) | `c[i] = a[i] + b[i]` |
| Songa koʻpaytirish | (*k* · *a*₁, …, *k* · *aₙ*) | `c[i] = k * a[i]` |
| Uzunlik | √(*a*₁² + … + *aₙ*²) | yigʻindi sikl bilan, keyin `sqrt` |
| Masofa | √((*a*₁ − *b*₁)² + … ) | ayirmalar kvadratlari yigʻindisining ildizi |
| Skalyar koʻpaytma | *a*₁*b*₁ + … + *aₙbₙ* | `s += a[i] * b[i]` |

Amallarning hammasi bitta sikl bilan bajariladi va ikki vektor oʻlchami bir xil boʻlishi shart.

C++'da „vektor“ soʻzi ikki maʼnoda: matematik vektor va `std::vector` — hajmi oʻzgaruvchan massiv. Ikkinchisi birinchisini saqlash uchun qulay: `vector<double> a(n);`.

## Namunaviy masala: yigʻindi va masofa

Birinchi qatorda *n*, ikkinchi va uchinchi qatorlarda *n* tadan butun son — *a* va *b* vektorlar. Birinchi qatorda *a* + *b* ni, ikkinchisida *a* va *b* orasidagi masofani (ikki kasr xona) chiqaring.

```python
import math

n = int(input())
a = list(map(int, input().split()))
b = list(map(int, input().split()))

yigindi = [a[i] + b[i] for i in range(n)]

kvadratlar = 0
for i in range(n):
    kvadratlar += (a[i] - b[i]) ** 2
masofa = math.sqrt(kvadratlar)

print(*yigindi)
print(f"{masofa:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        int[] b = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        for (int i = 0; i < n; i++) b[i] = in.nextInt();

        StringBuilder sb = new StringBuilder();
        long kvadratlar = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i] + b[i]);
            long d = a[i] - b[i];
            kvadratlar += d * d;
        }

        System.out.println(sb);
        System.out.println(String.format("%.2f", Math.sqrt(kvadratlar)));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <vector>
#include <cmath>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<long long> a(n), b(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    for (int i = 0; i < n; i++) cin >> b[i];

    long long kvadratlar = 0;
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i] + b[i];
        long long d = a[i] - b[i];
        kvadratlar += d * d;
    }
    cout << endl;
    cout << fixed << setprecision(2) << sqrt((double) kvadratlar) << endl;
    return 0;
}
```

Kirish `3`, `1 2 3`, `4 6 3` boʻlsa:

| *i* | *aᵢ* | *bᵢ* | *aᵢ* + *bᵢ* | (*aᵢ* − *bᵢ*)² |
|---|---|---|---|---|
| 1 | 1 | 4 | 5 | 9 |
| 2 | 2 | 6 | 8 | 16 |
| 3 | 3 | 3 | 6 | 0 |

Chiqadi: `5 8 6` va `5.00` (√25). Ayirmaning kvadrati `long` turida hisoblanadi: koordinatalar katta boʻlsa, `int` da kvadrat toʻlib ketishi mumkin.

## Mustaqil ish topshirigʻi

Quyida berilgan **vektorlarning skalyar koʻpaytmasi** masalasini yeching. Topshiriladigan ish:

1. **Qoʻlda hisob** — *a* = (1, −2, 3), *b* = (4, 0, −1) uchun skalyar koʻpaytmani jadval bilan hisoblang.
2. **Geometrik maʼno** — skalyar koʻpaytma 0 ga teng boʻlsa, vektorlar haqida nima deyish mumkin? Ikki misol keltiring.
3. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
4. **Toʻlib ketish tahlili** — koordinatalar 10⁶ gacha va *n* = 10⁵ boʻlsa, natija `int` ga sigʻadimi? Qaysi turni tanladingiz va nima uchun?

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Yigʻuvchi sikldan **oldin** 0 qilinadi; sikl ichida emas.
- Katta qiymatlarda koʻpaytma `int` dan oshadi — `long` / `long long` ishlating.
- Ikki vektor bir xil uzunlikda oʻqilishi kerak; ikkinchi vektorni birinchisi ichiga oʻqib yubormang.
- Qoʻlda hisob va tahlil dasturdan koʻchirilmasligi kerak.
:::

:::tip
Ikki vektorni ustma-ust yozib, juft-juft koʻpaytirib chiqing: bu skalyar koʻpaytmaning eng tez qoʻlda hisoblanadigan koʻrinishi va dastur ham aynan shunday ishlaydi.
:::

## Xulosa

- *n* oʻlchamli vektor dasturda uzunligi *n* boʻlgan massiv sifatida saqlanadi.
- Yigʻindi, songa koʻpaytirish, uzunlik, masofa va skalyar koʻpaytma bitta sikl bilan hisoblanadi.
- Kvadratlar va koʻpaytmalar katta qiymatlarda toʻlib ketishi mumkin — keng tur tanlang.
- C++'dagi `vector` — vektorni saqlash uchun qulay, hajmi oʻzgaruvchan massiv.

## Oʻzingizni tekshiring

:::check
Q: (3, 4) vektorning uzunligi qancha?
A: 5: √(9 + 16).
Q: (1, 2) va (−2, 1) vektorlarning skalyar koʻpaytmasi nimaga teng va bu nimani bildiradi?
A: 1 · (−2) + 2 · 1 = 0: vektorlar perpendikulyar.
Q: *a* = (1, 2, 3) ni 2 ga koʻpaytirsak, nima hosil boʻladi?
A: (2, 4, 6).
:::

:::link https://en.wikipedia.org/wiki/Dot_product
Wikipedia: skalyar koʻpaytma
Taʼrifi, geometrik maʼnosi va xossalari.
:::
