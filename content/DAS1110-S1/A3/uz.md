---
title: Mantiqiy amallar va matematik funksiyalar bilan ishlash
summary: Mantiqiy amallar (VA, YOKI, EMAS), haqiqiylik jadvali, qisqa tutashuv, amallar ustuvorligi; ildiz, daraja, modul, yaxlitlash va trigonometrik funksiyalar.
---

## Nima uchun bu mavzu muhim?

Universitet stipendiyani faqat barcha fanlardan oʻtgan **va** oʻrtacha bali 4 dan yuqori talabaga beradi. Kutubxona kitobni talaba **yoki** oʻqituvchiga beradi. Bunday qoidalar dasturda mantiqiy amallar bilan yoziladi. Masofa, maydon, foiz kabi hisob-kitoblar esa matematik funksiyalarsiz boʻlmaydi: ildiz, daraja, yaxlitlash.

Bu amaliy mashgʻulotda murakkab shartlarni mantiqiy ifoda sifatida yozishni va standart matematik funksiyalardan foydalanishni mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- VA, YOKI, EMAS amallari bilan murakkab shart yoza olasiz;
- mantiqiy ifodaning qiymatini haqiqiylik jadvali yordamida hisoblay olasiz;
- amallar ustuvorligini hisobga olib, qavslarni toʻgʻri qoʻya olasiz;
- ildiz, daraja, modul, yaxlitlash va trigonometrik funksiyalarni uch tilda ishlata olasiz.
:::

## Mantiqiy amallar

**Mantiqiy (boolean) ifoda** qiymati faqat ikki xil boʻladi: rost (`true`) yoki yolgʻon (`false`). Solishtirishlar (`>`, `==`, `!=` …) mantiqiy qiymat beradi; ularni mantiqiy amallar bilan birlashtirish mumkin.

| Amal | Python | Java, C++ | Qachon rost |
|---|---|---|---|
| VA (AND, konyunksiya) | `and` | `&&` | ikkala qism ham rost boʻlsa |
| YOKI (OR, dizyunksiya) | `or` | `\|\|` | kamida bitta qism rost boʻlsa |
| EMAS (NOT, inkor) | `not` | `!` | qism yolgʻon boʻlsa |

**Haqiqiylik jadvali (truth table)** barcha holatlarni koʻrsatadi:

| *A* | *B* | *A* VA *B* | *A* YOKI *B* | EMAS *A* |
|---|---|---|---|---|
| yolgʻon | yolgʻon | yolgʻon | yolgʻon | rost |
| yolgʻon | rost | yolgʻon | rost | rost |
| rost | yolgʻon | yolgʻon | rost | yolgʻon |
| rost | rost | rost | rost | yolgʻon |

Misollar: „son 10 dan 20 gacha“ — `x >= 10 and x <= 20` (Python'da qisqasi: `10 <= x <= 20`); „juft son“ — `x % 2 == 0`; „dam olish kuni“ — `kun == 6 or kun == 7`.

### Qisqa tutashuv

**Qisqa tutashuv (short-circuit)** — natija birinchi qismdan maʼlum boʻlsa, ikkinchi qism hisoblanmaydi. VA'da birinchi qism yolgʻon boʻlsa, butun ifoda yolgʻon; YOKI'da birinchi qism rost boʻlsa, butun ifoda rost. Bu xatoning oldini olishda foydali: `b != 0 and a / b > 2` ifodasi *b* = 0 boʻlganda nolga boʻlishga yetib bormaydi.

### Amallar ustuvorligi

Ifodada qavs boʻlmasa, amallar quyidagi tartibda bajariladi (yuqoridan pastga):

| Ustuvorlik | Amallar |
|---|---|
| 1 | qavslar `( )` |
| 2 | EMAS: `not`, `!` |
| 3 | `*`, `/`, `//`, `%` |
| 4 | `+`, `-` |
| 5 | solishtirish: `<`, `<=`, `>`, `>=`, `==`, `!=` |
| 6 | VA: `and`, `&&` |
| 7 | YOKI: `or`, `\|\|` |

VA amali YOKI'dan oldin bajariladi: `a or b and c` — bu `a or (b and c)`. Shubha tugʻilsa, qavs qoʻying: qavs hech qachon ortiqcha emas.

## Matematik funksiyalar

Har bir tilda standart matematik funksiyalar toʻplami bor: Python'da `math` moduli (`import math`), Java'da `Math` klassi (import kerak emas), C++'da `<cmath>` sarlavha fayli.

| Funksiya | Python | Java | C++ | Misol |
|---|---|---|---|---|
| Kvadrat ildiz | `math.sqrt(x)` | `Math.sqrt(x)` | `sqrt(x)` | √16 = 4.0 |
| Daraja | `x ** y`, `math.pow(x, y)` | `Math.pow(x, y)` | `pow(x, y)` | 2¹⁰ = 1024 |
| Modul | `abs(x)` | `Math.abs(x)` | `abs(x)`, `fabs(x)` | \|−7\| = 7 |
| Pastga yaxlitlash | `math.floor(x)` | `Math.floor(x)` | `floor(x)` | ⌊3.7⌋ = 3 |
| Yuqoriga yaxlitlash | `math.ceil(x)` | `Math.ceil(x)` | `ceil(x)` | ⌈3.2⌉ = 4 |
| Yaqiniga yaxlitlash | `round(x)` | `Math.round(x)` | `round(x)` | 3.5 → 4 |
| Sinus, kosinus | `math.sin(x)`, `math.cos(x)` | `Math.sin(x)` | `sin(x)` | burchak radianda |
| π soni | `math.pi` | `Math.PI` | `M_PI` yoki `acos(-1)` | 3.14159… |

Trigonometrik funksiyalar burchakni **radianda** qabul qiladi: 180° = π radian, yaʼni `radian = gradus * pi / 180`.

Python'ning `round()` funksiyasi teng masofadagi sonni **juft** songa yaxlitlaydi: `round(2.5)` = 2, `round(3.5)` = 4 („bankir yaxlitlashi“). Java va C++'da 2.5 → 3.

## Ishlangan misol 1: kabisa yili

Yil kiritiladi. U kabisa yili ekanini aniqlang. Qoida: yil 4 ga boʻlinsa va 100 ga boʻlinmasa, yoki 400 ga boʻlinsa — kabisa yili.

```python
yil = int(input())

kabisa = (yil % 4 == 0 and yil % 100 != 0) or yil % 400 == 0
print(kabisa)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int yil = in.nextInt();

        boolean kabisa = (yil % 4 == 0 && yil % 100 != 0) || yil % 400 == 0;
        System.out.println(kabisa);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int yil;
    cin >> yil;

    bool kabisa = (yil % 4 == 0 && yil % 100 != 0) || yil % 400 == 0;
    cout << boolalpha << kabisa << endl;   // boolalpha: 1/0 oʻrniga true/false
    return 0;
}
```

Turli kirishlar uchun:

| Yil | `% 4 == 0` | `% 100 != 0` | `% 400 == 0` | Natija |
|---|---|---|---|---|
| 2024 | rost | rost | yolgʻon | rost |
| 1900 | rost | yolgʻon | yolgʻon | yolgʻon |
| 2000 | rost | yolgʻon | rost | rost |
| 2026 | yolgʻon | rost | yolgʻon | yolgʻon |

Python `True`/`False`, Java `true`/`false` chiqaradi. C++ esa odatda `1`/`0` chiqaradi; `boolalpha` manipulyatori soʻz bilan chiqarishni yoqadi.

## Ishlangan misol 2: Geron formulasi

Uchburchakning uch tomoni (haqiqiy sonlar) bir qatorda kiritiladi. Uning yuzini Geron formulasi bilan hisoblang: *p* = (*a* + *b* + *c*) / 2, *S* = √(*p*(*p* − *a*)(*p* − *b*)(*p* − *c*)).

```python
import math

a, b, c = map(float, input().split())

p = (a + b + c) / 2                        # yarim perimetr
s = math.sqrt(p * (p - a) * (p - b) * (p - c))
print(f"{s:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double a = in.nextDouble();
        double b = in.nextDouble();
        double c = in.nextDouble();

        double p = (a + b + c) / 2;                 // yarim perimetr
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        System.out.println(String.format("%.2f", s));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

int main() {
    double a, b, c;
    cin >> a >> b >> c;

    double p = (a + b + c) / 2;                 // yarim perimetr
    double s = sqrt(p * (p - a) * (p - b) * (p - c));
    cout << fixed << setprecision(2) << s << endl;
    return 0;
}
```

Kirish `3 4 5` boʻlsa:

1. *p* = (3 + 4 + 5) / 2 = 6.
2. Ildiz ostida: 6 · 3 · 2 · 1 = 36.
3. √36 = 6, chiqadi: `6.00`.

Kirish `2 3 4` boʻlsa: *p* = 4.5, ildiz ostida 4.5 · 2.5 · 1.5 · 0.5 = 8.4375, *S* ≈ 2.9047, chiqadi `2.90`.

:::warning
**Koʻp uchraydigan xatolar**

- **`10 < x < 20` ni Java yoki C++'da yozish.** Python'da bu toʻgʻri, Java'da kompilyatsiya xatosi, C++'da esa notoʻgʻri maʼno: avval `10 < x` (0 yoki 1) hisoblanadi, keyin u 20 bilan solishtiriladi — natija har doim rost. Toʻgʻrisi: `x > 10 && x < 20`.
- **`&` va `&&` ni chalkashtirish.** `&` — bitli amal, u qisqa tutashuvsiz ishlaydi.
- **Gradusni radianga oʻtkazmaslik.** `sin(30)` — 30 radianning sinusi, 0.5 emas.
- **Haqiqiy sonlarni `==` bilan solishtirish.** `0.1 + 0.2 == 0.3` yolgʻon! Haqiqiy sonlar taxminiy saqlanadi; `abs(x - y) < 1e-9` kabi solishtiring.
:::

:::tip
Murakkab shartni yozishdan oldin uni oʻzbek tilida toʻliq gap bilan ayting: „yil 4 ga boʻlinadi VA 100 ga boʻlinmaydi, YOKI 400 ga boʻlinadi“. Keyin har bir boʻlakni kodga oʻgiring va qavs bilan guruhlang.
:::

## Xulosa

- Mantiqiy amallar: VA (`and`, `&&`), YOKI (`or`, `||`), EMAS (`not`, `!`); natija haqiqiylik jadvali boʻyicha aniqlanadi.
- Qisqa tutashuv: natija birinchi qismdan maʼlum boʻlsa, ikkinchisi hisoblanmaydi.
- Ustuvorlik: EMAS → arifmetika → solishtirish → VA → YOKI; shubhada qavs qoʻying.
- Matematik funksiyalar: Python'da `math`, Java'da `Math`, C++'da `<cmath>`; burchaklar radianda.
- Haqiqiy sonlarni `==` bilan emas, kichik farq orqali solishtiring.

## Oʻzingizni tekshiring

:::check
Q: *x* = 15 boʻlsa, `x > 10 and x % 2 == 0` qiymati qanday?
A: Yolgʻon: birinchi qism rost, lekin 15 juft emas.
Q: `not (a > 5)` ifodasini `not` siz qanday yozasiz?
A: `a <= 5`.
Q: `true || 1 / 0 > 0` ifodasi Java'da xato beradimi?
A: Yoʻq: qisqa tutashuv tufayli YOKI'ning birinchi qismi rost boʻlgani uchun ikkinchisi hisoblanmaydi.
Q: 47 talabani 45 oʻrinli avtobuslarga joylash uchun nechta avtobus kerak va buni qaysi funksiya bilan hisoblaysiz?
A: 2 ta: `ceil(47 / 45)` — yuqoriga yaxlitlash (butun boʻlishdan saqlanish uchun `47 / 45.0`).
Q: Python'da `round(2.5)` va `round(3.5)` nimaga teng?
A: 2 va 4: Python teng masofadagi sonni juft songa yaxlitlaydi.
:::

:::link https://docs.python.org/3/library/math.html
Python: math moduli
Barcha matematik funksiyalarning rasmiy roʻyxati.
:::

:::link https://en.cppreference.com/w/cpp/numeric/math
cppreference: umumiy matematik funksiyalar
`<cmath>` dagi `sqrt`, `pow`, `floor`, `ceil`, `round` va boshqalar.
:::
