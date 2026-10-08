---
title: Algoritmlar murakkabligini hisoblash. Rekursiv hisoblash algoritmlari (amaliy mashgʻulot)
summary: Amallar sonini sanash orqali murakkablikni tajribada tekshirish, n ni ikki baravar oshirish usuli; „boʻl va hukmron boʻl“ usulidagi rekursiv algoritm, chaqiruvlar soni va rekursiya chuqurligi; oddiy rekursiv Fibonachchining eksponensial oʻsishi.
---

## Nima uchun bu mavzu muhim?

Maʼruzada *O*-belgisi va rekurrent munosabatlarni nazariy koʻrdik. Amaliyotda esa savol oddiy: dasturim haqiqatan *O*(*n* log *n*) mi yoki men xato hisobladimmi? Bunga javob berishning eng ishonchli yoʻli — amallarni **sanash** va *n* oshganda ular qanday oʻsishini kuzatish.

Bu mashgʻulotda uchta tajriba oʻtkazamiz: chiziqli, kvadratik va logarifmik sikllarning amallarini sanaymiz; „boʻl va hukmron boʻl“ usulidagi rekursiv algoritmning chaqiruvlari va chuqurligini oʻlchaymiz; oddiy rekursiv Fibonachchi chaqiruvlari qanchalik tez portlashini koʻramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- dasturga hisoblagich qoʻshib, amallar sonini oʻlchay olasiz;
- *n* ni ikki baravar oshirish usuli bilan murakkablik sinfini aniqlay olasiz;
- rekursiv algoritm uchun rekurrent munosabat tuzib, chaqiruvlar sonini oldindan hisoblay olasiz;
- rekursiya chuqurligi va chaqiruvlar soni farqini tushuntira olasiz.
:::

## Murakkablikni tajribada tekshirish

Usul: *n* ni ikki baravar oshiramiz va amallar soni necha marta oshganiga qaraymiz.

| Murakkablik | *n* → 2*n* boʻlganda amallar | Belgisi |
|---|---|---|
| *O*(log *n*) | +1 | deyarli oʻzgarmaydi |
| *O*(*n*) | × 2 | ikki baravar |
| *O*(*n* log *n*) | × 2 dan biroz koʻp | |
| *O*(*n*²) | × 4 | toʻrt baravar |
| *O*(2ⁿ) | kvadratga oshadi | *n* + 1 da ham ikki baravar |

Vaqtni soniyalarda oʻlchash ham mumkin, lekin u kompyuter yuklamasiga bogʻliq va kichik *n* da aniq emas. Hisoblagich esa har doim bir xil natija beradi.

## Rekursiv algoritmni tahlil qilish

**„Boʻl va hukmron boʻl“** (divide and conquer) usuli: masalani ikki yarmiga boʻlish, har birini rekursiv yechish va natijalarni birlashtirish. Ikkita koʻrsatkich muhim:

| Koʻrsatkich | Maʼnosi | Nimani belgilaydi |
|---|---|---|
| Chaqiruvlar soni | jami necha marta funksiya chaqirilgan | vaqt |
| Rekursiya chuqurligi | bir vaqtda stekda turgan eng koʻp chaqiruvlar | xotira (stek) |

Massivni ikkiga boʻlib maksimum topishda: *T*(*n*) = 2*T*(*n*/2) + 1, *T*(1) = 1. Yechimi: *T*(*n*) = 2*n* − 1 chaqiruv, chuqurlik esa ⌈log₂ *n*⌉ + 1. Demak, vaqt *O*(*n*), stek xotirasi *O*(log *n*).

## Ishlangan misol 1: amallarni sanash

*n* kiritiladi (1 ≤ *n* ≤ 3000). Uchta siklning ichki amallari sonini sanab chiqaring: (1) har bir elementni bir marta koʻrish; (2) barcha juftliklarni (*i* < *j*) koʻrish; (3) *n* ni 1 ga yetguncha ikkiga boʻlish.

```python
n = int(input())

chiziqli = 0
for i in range(n):
    chiziqli += 1

kvadratik = 0
for i in range(n):
    for j in range(i + 1, n):       # barcha i < j juftliklar
        kvadratik += 1

logarifmik = 0
m = n
while m > 1:
    m //= 2
    logarifmik += 1

print(chiziqli, kvadratik, logarifmik)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();

        long chiziqli = 0;
        for (int i = 0; i < n; i++) chiziqli++;

        long kvadratik = 0;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)     // barcha i < j juftliklar
                kvadratik++;

        long logarifmik = 0;
        for (int m = n; m > 1; m /= 2) logarifmik++;

        System.out.println(chiziqli + " " + kvadratik + " " + logarifmik);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long chiziqli = 0;
    for (int i = 0; i < n; i++) chiziqli++;

    long long kvadratik = 0;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)         // barcha i < j juftliklar
            kvadratik++;

    long long logarifmik = 0;
    for (int m = n; m > 1; m /= 2) logarifmik++;

    cout << chiziqli << " " << kvadratik << " " << logarifmik << endl;
    return 0;
}
```

| *n* | Chiziqli | Kvadratik | Logarifmik |
|---|---|---|---|
| 1000 | 1 000 | 499 500 | 9 |
| 2000 | 2 000 | 1 999 000 | 10 |
| 3000 | 3 000 | 4 498 500 | 11 |

*n* ikki baravar oshganda chiziqli hisoblagich ikki baravar, kvadratik — deyarli toʻrt baravar oshdi (*n*(*n* − 1)/2), logarifmik esa atigi bittaga. Shu jadvalning oʻzi algoritm murakkabligini koʻrsatadi. Eʼtibor bering: kvadratik sikl uchun *n* = 3000 da 4,5 million amal kerak — *n* = 10⁶ da esa 5 · 10¹¹ boʻlardi, buni sanab boʻlmaydi.

## Ishlangan misol 2: „boʻl va hukmron boʻl“ — maksimum, chaqiruvlar, chuqurlik

Birinchi satrda *n*, ikkinchisida *n* ta son. Maksimumni massivni ikkiga boʻlib rekursiv toping; maksimumni, chaqiruvlar sonini va rekursiyaning eng katta chuqurligini chiqaring.

```python
chaqiruvlar = 0
chuqurlik = 0


def maksimum(a, chap, ong, daraja):
    global chaqiruvlar, chuqurlik
    chaqiruvlar += 1
    chuqurlik = max(chuqurlik, daraja)
    if chap == ong:                             # bitta element
        return a[chap]
    orta = (chap + ong) // 2
    return max(maksimum(a, chap, orta, daraja + 1),
               maksimum(a, orta + 1, ong, daraja + 1))


n = int(input())
a = list(map(int, input().split()))
print(maksimum(a, 0, n - 1, 1), chaqiruvlar, chuqurlik)
```
```java
import java.util.Scanner;

public class Main {
    static long chaqiruvlar = 0;
    static int chuqurlik = 0;

    static int maksimum(int[] a, int chap, int ong, int daraja) {
        chaqiruvlar++;
        chuqurlik = Math.max(chuqurlik, daraja);
        if (chap == ong) return a[chap];            // bitta element
        int orta = (chap + ong) / 2;
        return Math.max(maksimum(a, chap, orta, daraja + 1),
                        maksimum(a, orta + 1, ong, daraja + 1));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int m = maksimum(a, 0, n - 1, 1);
        System.out.println(m + " " + chaqiruvlar + " " + chuqurlik);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

long long chaqiruvlar = 0;
int chuqurlik = 0;

int maksimum(const vector<int>& a, int chap, int ong, int daraja) {
    chaqiruvlar++;
    chuqurlik = max(chuqurlik, daraja);
    if (chap == ong) return a[chap];                // bitta element
    int orta = (chap + ong) / 2;
    return max(maksimum(a, chap, orta, daraja + 1),
               maksimum(a, orta + 1, ong, daraja + 1));
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& x : a) cin >> x;
    int m = maksimum(a, 0, n - 1, 1);
    cout << m << " " << chaqiruvlar << " " << chuqurlik << endl;
    return 0;
}
```

`8` va `3 9 2 7 5 1 8 6` uchun chiqish `9 15 4`: 2 · 8 − 1 = 15 chaqiruv va log₂ 8 + 1 = 4 qavat — rekurrent munosabat bashorat qilganidek. `3` va `5 1 4` uchun — `5 5 3`. Bu algoritm sikldagi oddiy maksimumdan tez emas (ikkalasi *O*(*n*)), lekin aynan shu sxema birlashtirib saralash va tez saralashning asosi. Chaqiruvlar soni vaqtni, chuqurlik esa stek xotirasini belgilaydi: million elementda 2 million chaqiruv boʻladi, lekin stekda bir vaqtda atigi ~21 tasi turadi.

## Ishlangan misol 3: oddiy rekursiv Fibonachchi

*n* kiritiladi (0 ≤ *n* ≤ 30). *F*(*n*) ni toʻgʻridan-toʻgʻri rekursiya bilan hisoblang va funksiya necha marta chaqirilganini chiqaring.

```python
chaqiruvlar = 0


def fib(n):
    global chaqiruvlar
    chaqiruvlar += 1
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)


n = int(input())
print(fib(n), chaqiruvlar)
```
```java
import java.util.Scanner;

public class Main {
    static long chaqiruvlar = 0;

    static long fib(int n) {
        chaqiruvlar++;
        if (n < 2) return n;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        long f = fib(n);
        System.out.println(f + " " + chaqiruvlar);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long chaqiruvlar = 0;

long long fib(int n) {
    chaqiruvlar++;
    if (n < 2) return n;
    return fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    long long f = fib(n);
    cout << f << " " << chaqiruvlar << endl;
    return 0;
}
```

| *n* | *F*(*n*) | Chaqiruvlar |
|---|---|---|
| 10 | 55 | 177 |
| 20 | 6 765 | 21 891 |
| 25 | 75 025 | 242 785 |

*n* har 5 ga oshganda chaqiruvlar ~11 baravar oshadi (φ⁵ ≈ 11): bu eksponensial oʻsish. *n* = 50 da chaqiruvlar soni 4 · 10¹⁰ ga yetadi. Sabab — bir xil qiymatlar qayta-qayta hisoblanadi; buni tuzatish usuli mustaqil topshiriqda bajariladi. Java va C++'da `fib(n)` natijasi alohida oʻzgaruvchiga olingan: chiqarish ifodasida `chaqiruvlar` dan oldin hisoblanishi kafolatlanishi uchun.

:::warning
**Koʻp uchraydigan xatolar**

- **Hisoblagichni notoʻgʻri joyga qoʻyish.** Hisoblagich aynan sanalayotgan amal (ichki sikl tanasi, funksiya kirishi) yonida turishi kerak.
- **Bitta oʻlchov bilan xulosa chiqarish.** Murakkablik — oʻsish tezligi; kamida ikki xil *n* bilan solishtiring.
- **Chuqurlik va chaqiruvlar sonini aralashtirish.** 15 ta chaqiruv 4 qavat chuqurlikda boʻlishi mumkin — ular turli resurslarni oʻlchaydi.
- **C++/Javaʼda baholash tartibiga tayanish.** `cout << fib(n) << chaqiruvlar` da qaysi qism avval hisoblanishi C++17 dan oldin kafolatlanmagan; natijani oldin oʻzgaruvchiga oling.
:::

:::tip
Tajriba natijalarini jadvalga yozib, qoʻshni qatorlar nisbatini hisoblang (masalan, 1 999 000 / 499 500 ≈ 4). Nisbat oʻzgarmas boʻlib qolsa, murakkablik sinfini toʻgʻri topgansiz.
:::

## Xulosa

- Amallar sonini hisoblagich bilan oʻlchab, *n* ni ikki baravar oshirib, murakkablik sinfini tajribada aniqlash mumkin.
- „Boʻl va hukmron boʻl“ algoritmi rekurrent munosabat bilan tahlil qilinadi: *T*(*n*) = 2*T*(*n*/2) + 1 → 2*n* − 1 chaqiruv.
- Chaqiruvlar soni vaqtni, rekursiya chuqurligi stek xotirasini belgilaydi.
- Oddiy rekursiv Fibonachchi eksponensial: bir xil qiymatlar koʻp marta qayta hisoblanadi.

## Oʻzingizni tekshiring

:::check
Q: *n* ikki baravar oshganda amallar soni toʻrt baravar oshsa, murakkablik qanday?
A: *O*(*n*²).
Q: Ikkiga boʻlib maksimum topish 16 elementli massiv uchun nechta chaqiruv bajaradi?
A: 2 · 16 − 1 = 31 ta.
Q: Shu algoritmda rekursiya chuqurligi qancha?
A: log₂ 16 + 1 = 5.
Q: Nima uchun oddiy rekursiv Fibonachchi sekin?
A: Bir xil kichik qiymatlar (*F*(*n* − 2), *F*(*n* − 3), …) koʻp marta qayta hisoblanadi; chaqiruvlar soni eksponensial oʻsadi.
:::

:::link https://en.wikipedia.org/wiki/Master_theorem_(analysis_of_algorithms)
Master teoremasi
„Boʻl va hukmron boʻl“ rekurrent munosabatlarini yechishning umumiy usuli (ingliz tilida).
:::
