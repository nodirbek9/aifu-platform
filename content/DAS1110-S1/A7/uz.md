---
title: Takrorlanuvchi jarayonlar. Parametrli takrorlash operatori (davomi)
summary: for bilan murakkabroq masalalar: tub sonni tekshirish va break, Fibonachchi sonlari, qator yigʻindisi; qadam, teskari tartib, natijani bir qatorda chiqarish.
---

## Nima uchun bu mavzu muhim?

Bank kartasi raqamining toʻgʻriligi raqamlar ustida sikl bilan tekshiriladi (Luhn algoritmi). Internetdagi shifrlash katta tub sonlarga tayanadi. Kredit boʻyicha toʻlov jadvali har oy uchun takrorlanadigan hisob. Bu masalalarning barchasi `for` siklining turli „hiyla“laridan foydalanadi: qadam, erta toʻxtash, oldingi qiymatlarni eslab qolish.

Bu amaliy mashgʻulotda `for` siklining shu imkoniyatlarini uchta klassik masalada mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- `for` siklida qadam va teskari tartibdan foydalana olasiz;
- `break` bilan siklni erta toʻxtatib, „topildi / topilmadi“ bayrogʻi bilan ishlay olasiz;
- oldingi ikki qiymatga tayanadigan ketma-ketlikni (Fibonachchi) hisoblay olasiz;
- haqiqiy sonli qator yigʻindisini hisoblab, natijani bir qatorda chiqara olasiz.
:::

## for siklining imkoniyatlari

| Vazifa | Python | Java, C++ |
|---|---|---|
| 0 dan 20 gacha juftlar | `range(0, 21, 2)` | `for (int i = 0; i <= 20; i += 2)` |
| 10 dan 1 gacha | `range(10, 0, -1)` | `for (int i = 10; i >= 1; i--)` |
| *i*² ≤ *n* boʻlguncha | `while i * i <= n:` | `for (int i = 2; i * i <= n; i++)` |

Java va C++'da `for` sharti istalgan mantiqiy ifoda boʻlishi mumkin (masalan, `i * i <= n`). Pythonʼda `range` faqat arifmetik progressiya beradi; murakkab shart uchun `while` ishlatiladi.

## Bayroq va break

„Shunday element bormi?“ turidagi savollar uchun **bayroq (flag)** andozasi qoʻllanadi:

1. Sikldan oldin: `topildi = False`.
2. Siklda element topilsa: `topildi = True` va `break` — qolganlarini tekshirishga hojat yoʻq.
3. Sikldan keyin bayroqqa qarab javob chiqariladi.

## Natijani bir qatorda chiqarish

Odatda `print` / `println` / `endl` har safar yangi qatorga oʻtadi. Qiymatlarni probel bilan bir qatorda chiqarish uchun:

| Til | Yangi qatorsiz chiqarish |
|---|---|
| Python | `print(x, end=" ")` |
| Java | `System.out.print(x + " ")` |
| C++ | `cout << x << " "` (`endl` siz) |

Oxirida bitta yangi qator chiqariladi. Ortiqcha probel muammo boʻlsa, ajratuvchini birinchi elementdan oldin emas, keyingilaridan oldin chiqaring.

## Ishlangan misol 1: tub sonmi?

Natural son *n* (*n* ≥ 1) kiritiladi. U tub boʻlsa `tub`, aks holda `tub emas` chiqaring.

**Tub son** — faqat 1 ga va oʻziga boʻlinadigan, 1 dan katta son. Boʻluvchilarni √*n* gacha tekshirish yetarli: agar *n* = *a* · *b* va *a* ≤ *b* boʻlsa, *a* ≤ √*n*.

```python
n = int(input())

tub = n >= 2
i = 2
while i * i <= n:
    if n % i == 0:
        tub = False
        break
    i += 1

print("tub" if tub else "tub emas")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        boolean tub = n >= 2;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                tub = false;
                break;
            }
        }

        System.out.println(tub ? "tub" : "tub emas");
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    bool tub = n >= 2;
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) {
            tub = false;
            break;
        }
    }

    cout << (tub ? "tub" : "tub emas") << endl;
    return 0;
}
```

Kirish `91` boʻlsa: *i* = 2, 3, 4, 5, 6 — boʻlinmaydi; *i* = 7: 91 = 7 · 13 — `tub = False`, `break`. Chiqadi: `tub emas`. Kirish `97`: *i* 2 dan 9 gacha (10² = 100 > 97), hech biri boʻlmaydi — `tub`. Kirish `1`: bayroq boshidanoq yolgʻon — `tub emas`.

√*n* gacha tekshirish katta farq beradi: *n* ≈ 10⁹ boʻlsa, *n* gacha sikl milliard, √*n* gacha esa ~31 600 qadam.

## Ishlangan misol 2: Fibonachchi sonlari

Natural son *n* kiritiladi. Fibonachchi ketma-ketligining dastlabki *n* ta hadini bir qatorda chiqaring: 1, 1, 2, 3, 5, 8, … (har bir had oldingi ikkitasining yigʻindisi).

```python
n = int(input())

a, b = 1, 1
for i in range(n):
    print(a, end=" ")
    a, b = b, a + b
print()
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        long a = 1, b = 1;
        for (int i = 0; i < n; i++) {
            System.out.print(a + " ");
            long keyingi = a + b;
            a = b;
            b = keyingi;
        }
        System.out.println();
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long a = 1, b = 1;
    for (int i = 0; i < n; i++) {
        cout << a << " ";
        long long keyingi = a + b;
        a = b;
        b = keyingi;
    }
    cout << endl;
    return 0;
}
```

Kirish `8` boʻlsa:

| *i* | chiqariladi (*a*) | yangi *a* | yangi *b* |
|---|---|---|---|
| 0 | 1 | 1 | 2 |
| 1 | 1 | 2 | 3 |
| 2 | 2 | 3 | 5 |
| 3 | 3 | 5 | 8 |
| … | … | … | … |
| 7 | 21 | 34 | 55 |

Chiqadi: `1 1 2 3 5 8 13 21 `. Butun ketma-ketlikni saqlash shart emas: faqat oxirgi ikki had (*a*, *b*) yetarli. Pythonʼda `a, b = b, a + b` ikkala qiymatni bir vaqtda yangilaydi; Java va C++'da vaqtinchalik `keyingi` oʻzgaruvchisi kerak. Fibonachchi sonlari tez oʻsadi: 93-had `long` ga ham sigʻmaydi.

## Ishlangan misol 3: qator yigʻindisi

Natural son *n* kiritiladi. *S* = 1 + 1/2² + 1/3² + … + 1/*n*² yigʻindisini 6 kasr xona aniqlikda chiqaring. (*n* oshgan sari bu yigʻindi π²/6 ≈ 1.644934 ga yaqinlashadi.)

```python
n = int(input())

s = 0.0
for k in range(1, n + 1):
    s += 1 / (k * k)

print(f"{s:.6f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        double s = 0.0;
        for (int k = 1; k <= n; k++) {
            s += 1.0 / ((double) k * k);
        }

        System.out.println(String.format("%.6f", s));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int n;
    cin >> n;

    double s = 0.0;
    for (int k = 1; k <= n; k++) {
        s += 1.0 / ((double) k * k);
    }

    cout << fixed << setprecision(6) << s << endl;
    return 0;
}
```

Kirish `3` boʻlsa: 1 + 0.25 + 0.111111 = `1.361111`. Kirish `1000` boʻlsa: `1.643935` — π²/6 ga juda yaqin.

Ikki muhim tafsilot: (1) `1.0 / ...` — butun boʻlish boʻlmasligi uchun; `1 / (k * k)` Java va C++'da *k* > 1 da 0 beradi. (2) `(double) k * k` — *k* katta boʻlganda `k * k` `int` da toʻlib ketmasligi uchun.

:::warning
**Koʻp uchraydigan xatolar**

- **1 ni tub deb hisoblash.** Tub son 1 dan katta boʻlishi kerak; bayroqni `n >= 2` bilan boshlang.
- **Bayroqni sikl ichida qayta oʻrnatish.** `else: tub = True` — keyingi iteratsiya oldingi topilmani „oʻchiradi“.
- **Fibonachchida yangilash tartibi.** `a = b; b = a + b;` — *a* allaqachon oʻzgargan, ketma-ketlik buziladi.
- **Qatorda butun boʻlish.** `1 / (k * k)` Java va C++'da 0; `1.0 /` yozing.
- **`range(n + 1)` dan 0 ni ham olish.** `1 / 0` — nolga boʻlish xatosi; qator 1 dan boshlanadi.
:::

:::tip
Kodni katta *n* bilan ham sinab koʻring: tub son uchun 10⁹ ga yaqin son, Fibonachchi uchun 90 ta had. Tezlik va toʻlib ketish muammolari kichik testlarda koʻrinmaydi.
:::

## Xulosa

- `for` da qadam (`range(a, b, step)`, `i += step`) va teskari tartib (`range(b, a, -1)`, `i--`) bor.
- „Bormi?“ savoliga bayroq va `break` bilan javob beriladi.
- Tub sonlikni √*n* gacha tekshirish yetarli; 1 tub emas.
- Fibonachchi uchun faqat oxirgi ikki had saqlanadi; Pythonʼda `a, b = b, a + b`.
- Haqiqiy qatorda `1.0 /` va `(double)` bilan butun boʻlish va toʻlib ketishdan saqlaning.

## Oʻzingizni tekshiring

:::check
Q: Pythonʼda `range(20, 0, -5)` qanday qiymatlarni beradi?
A: 20, 15, 10, 5.
Q: Nima uchun tub sonlikni √*n* gacha tekshirish yetarli?
A: Agar *n* = *a* · *b* va *a* ≤ *b* boʻlsa, *a* · *a* ≤ *n*, yaʼni *a* ≤ √*n*. Demak, boʻluvchi boʻlsa, ulardan biri albatta √*n* dan oshmaydi.
Q: 2-misolda `n = 1` boʻlsa, nima chiqadi?
A: Faqat `1` (va probel): sikl bir marta ishlaydi.
Q: Javaʼda `1 / (k * k)` *k* = 2 boʻlganda nimaga teng va nima uchun?
A: 0: ikkala operand butun, butun boʻlish kasrni tashlaydi.
Q: Tub son dasturida `break` olib tashlansa, natija oʻzgaradimi?
A: Natija oʻzgarmaydi, lekin dastur sekinlashadi: boʻluvchi topilgandan keyin ham qolgan sonlar tekshiriladi.
:::

:::link https://en.wikipedia.org/wiki/Fibonacci_sequence
Wikipedia: Fibonachchi sonlari
Ketma-ketlikning tarixi va xossalari.
:::

:::link https://docs.python.org/3/library/stdtypes.html#range
Python: range turi
`range(start, stop, step)` haqida rasmiy hujjat.
:::
