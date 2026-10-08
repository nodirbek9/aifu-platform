---
title: Maʼlumot turlari. Kiritish va chiqarish funksiyalari. Operatorlar
summary: Turlarning oʻlchami va chegarasi, toʻlib ketish, turlarni oʻzgartirish, bir qatorda bir nechta qiymat kiritish, formatlangan chiqarish, qisqartirilgan taʼminlash operatorlari.
---

## Nima uchun bu mavzu muhim?

Bank ilovasi foizni hisoblashda kasr qismini tashlab yuborsa, mijoz pulini yoʻqotadi. Talabalar ballini butun sonlarda oʻrtacha hisoblagan dastur 4,67 oʻrniga 4 chiqaradi va talaba „yaxshi“ oʻrniga „qoniqarli“ oladi. Bunday xatolar sintaksis xatosi emas — dastur ishlaydi, faqat notoʻgʻri natija beradi. Ularning sababi koʻpincha maʼlumot turini notoʻgʻri tanlash.

Bu amaliy mashgʻulotda turlarning chegaralari, turlarni oʻzgartirish, kiritish va chiqarishni mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- masalaga mos maʼlumot turini (`int`, `long long`, `double`) tanlay olasiz;
- butun sonlarning toʻlib ketishi (overflow) va butun boʻlish xatosini oldini olasiz;
- bir qatorda kiritilgan bir nechta qiymatni oʻqiy olasiz;
- natijani kerakli aniqlik va koʻrinishda (`01:02:05`, `4.67`) chiqara olasiz;
- `+=`, `-=`, `++` kabi qisqartirilgan operatorlardan foydalana olasiz.
:::

## Turlarning oʻlchami va chegarasi

Kompyuter xotirasi cheklangan, shuning uchun har bir tur qiymat uchun belgilangan miqdorda joy ajratadi. Shu joyga sigʻmaydigan son saqlanmaydi.

| Tur (C++ / Java) | Oʻlchami | Qiymatlar oraligʻi (taxminan) |
|---|---|---|
| `int` | 4 bayt | −2,1 mlrd … 2,1 mlrd (−2³¹ … 2³¹−1) |
| `long long` / `long` | 8 bayt | ±9,2 · 10¹⁸ |
| `double` | 8 bayt | ±1,7 · 10³⁰⁸, ~15–16 ta aniq raqam |
| `char` | C++: 1 bayt, Java: 2 bayt | bitta belgi |
| `bool` / `boolean` | odatda 1 bayt | `true` yoki `false` |

Pythonʼda `int` chegarasiz: son qanchalik katta boʻlsa, shuncha joy oladi. `float` esa C++ va Javaʼdagi `double` bilan bir xil.

**Toʻlib ketish (overflow)** — natija tur chegarasidan chiqib ketishi. C++ va Javaʼda `int` oʻzgaruvchida 2 147 483 647 + 1 hech qanday xatosiz −2 147 483 648 ga aylanadi. Shuning uchun katta sonlar (aholi soni, millisekundlar, faktoriallar) uchun `long long` (C++) yoki `long` (Java) tanlanadi.

## Turlarni oʻzgartirish

Ifodada turli turdagi qiymatlar uchrasa, til ularni bir turga keltiradi.

- **Oshkormas (implicit) oʻzgartirish** — kichik turdan kattasiga avtomatik: `int` va `double` qoʻshilsa, natija `double`. Masalan, `7 / 2.0` = 3.5.
- **Oshkor (explicit) oʻzgartirish** — dasturchi buyrugʻi bilan: C++ va Javaʼda `(double) a`, C++'da yana `static_cast<double>(a)`; Pythonʼda `float(a)`, `int(x)`.

| Ifoda | Python | Java / C++ |
|---|---|---|
| `14 / 3` | `4.666…` | `4` |
| `14 / 3.0` | `4.666…` | `4.666…` |
| `(double) 14 / 3` | — | `4.666…` |
| `int(4.99)` / `(int) 4.99` | `4` | `4` (kasr tashlanadi, yaxlitlanmaydi) |

## Kiritish: bir qatorda bir nechta qiymat

Koʻpincha qiymatlar bitta qatorda probel bilan beriladi: `4 5 5`.

- **C++**: `cin >> a >> b >> c;` — probellar va yangi qatorlarni oʻzi oʻtkazib yuboradi.
- **Java**: `Scanner` ning `nextInt()` metodi ham xuddi shunday ishlaydi.
- **Python**: `input()` butun qatorni satr sifatida qaytaradi; uni `split()` bilan boʻlaklarga ajratib, har birini songa aylantirish kerak: `a, b, c = map(int, input().split())`.

## Formatlangan chiqarish

Natijani kerakli koʻrinishda chiqarish uchun format beriladi:

| Vazifa | Python | Java | C++ |
|---|---|---|---|
| 2 ta kasr xona | `f"{x:.2f}"` | `String.format("%.2f", x)` | `fixed << setprecision(2) << x` |
| Kengligi 2, oldidan nol | `f"{n:02d}"` | `String.format("%02d", n)` | `setw(2) << setfill('0') << n` |

C++'da `setprecision` va `setw` uchun `#include <iomanip>` kerak.

## Qisqartirilgan operatorlar

| Yozuv | Maʼnosi |
|---|---|
| `x += 5` | `x = x + 5` |
| `x -= 2` | `x = x - 2` |
| `x *= 3` | `x = x * 3` |
| `x /= 2` | `x = x / 2` |
| `x %= 60` | `x = x % 60` |
| `x++` / `++x` (Java, C++) | `x = x + 1` |
| `x--` / `--x` (Java, C++) | `x = x - 1` |

Pythonʼda `++` va `--` yoʻq: `x += 1` deb yoziladi.

## Ishlangan misol 1: soniyalarni soat formatiga oʻtkazish

Butun son — soniyalar soni kiritiladi (0 ≤ *n* < 86 400). Uni `SS:MM:ss` koʻrinishida chiqaring.

```python
n = int(input())

soat = n // 3600          # toʻliq soatlar
n %= 3600                 # qolgan soniyalar
daqiqa = n // 60
soniya = n % 60

print(f"{soat:02d}:{daqiqa:02d}:{soniya:02d}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int soat = n / 3600;      // toʻliq soatlar
        n %= 3600;                // qolgan soniyalar
        int daqiqa = n / 60;
        int soniya = n % 60;

        System.out.println(String.format("%02d:%02d:%02d", soat, daqiqa, soniya));
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

    int soat = n / 3600;      // toʻliq soatlar
    n %= 3600;                // qolgan soniyalar
    int daqiqa = n / 60;
    int soniya = n % 60;

    cout << setfill('0') << setw(2) << soat << ":"
         << setw(2) << daqiqa << ":" << setw(2) << soniya << endl;
    return 0;
}
```

Kirish `3725` boʻlsa:

1. `soat = 3725 // 3600` = 1 (butun boʻlish).
2. `n %= 3600` — qoldiq: *n* = 125.
3. `daqiqa = 125 // 60` = 2, `soniya = 125 % 60` = 5.
4. Har bir son ikki xonali qilib, oldidan nol bilan chiqariladi: `01:02:05`.

Butun boʻlish va qoldiq bu yerda „xato“ emas, aksincha, aynan kerakli vosita.

## Ishlangan misol 2: uchta bahoning oʻrtachasi

Bir qatorda talabaning uchta bahosi (butun sonlar) kiritiladi. Oʻrtacha bahoni ikki kasr xona aniqligida chiqaring.

```python
a, b, c = map(int, input().split())

ortacha = (a + b + c) / 3     # Python'da / har doim haqiqiy son beradi
print(f"{ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        // (a + b + c) / 3 butun boʻlish boʻlardi, shuning uchun double ga oʻtkazamiz
        double ortacha = (double) (a + b + c) / 3;
        System.out.println(String.format("%.2f", ortacha));
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    // (a + b + c) / 3 butun boʻlish boʻlardi, shuning uchun double ga oʻtkazamiz
    double ortacha = static_cast<double>(a + b + c) / 3;
    cout << fixed << setprecision(2) << ortacha << endl;
    return 0;
}
```

Kirish `4 5 5` boʻlsa:

1. Uch son oʻqiladi; Pythonʼda `input().split()` qatorni `["4", "5", "5"]` roʻyxatiga ajratadi, `map(int, ...)` har birini songa aylantiradi.
2. Yigʻindi 14. Java va C++'da `14 / 3` = 4 boʻlardi — shuning uchun yigʻindi avval `double` ga oʻtkaziladi: 14.0 / 3 = 4.666…
3. Ikki xonagacha yaxlitlab chiqariladi: `4.67`.

:::warning
**Koʻp uchraydigan xatolar**

- **`(double) (a + b + c / 3)`** — qavs notoʻgʻri joyda: avval `c / 3` butun boʻlinadi. Oʻzgartirish boʻlishdan *oldin* bajarilishi kerak.
- **`int` da katta son.** 50 000 · 50 000 = 2,5 mlrd `int` ga sigʻmaydi, natija manfiy chiqadi. `long long` yoki `long` ishlating.
- **Pythonʼda `input().split()` ni songa aylantirmaslik.** `"4" + "5"` = `"45"`.
- **`setfill('0')` dan keyin nolni qaytarmaslik.** C++'da `setfill` keyingi barcha chiqarishlarga taʼsir qiladi; `setw` esa faqat bitta keyingi qiymatga.
:::

:::tip
Natijani chiqarishdan oldin „bu qiymat qaysi turda?“ deb soʻrang. Agar ifodada faqat butun sonlar boʻlsa-yu, javob kasr boʻlishi kerak boʻlsa — oʻzgartirish kerak.
:::

## Xulosa

- Har bir turning oʻlchami va chegarasi bor; `int` ≈ ±2,1 mlrd, kattaroq sonlar uchun `long long` / `long`. Pythonʼning `int` i chegarasiz.
- Butun sonlarni boʻlish kasrni tashlaydi; kasr kerak boʻlsa, `double` ga oʻtkazing.
- Pythonʼda bir qatordagi qiymatlar `map(int, input().split())` bilan oʻqiladi; C++ va Javaʼda `cin >>` va `nextInt()` probellarni oʻzi oʻtkazib yuboradi.
- Formatlash: `.2f` — ikki kasr xona, `02d` — ikki xonali, oldidan nol.
- `x += 5` — `x = x + 5` ning qisqa yozuvi; Java va C++'da `x++` ham bor.

## Oʻzingizni tekshiring

:::check
Q: Javaʼda `int x = 2147483647; x++;` dan keyin *x* nimaga teng?
A: −2147483648: `int` toʻlib ketadi va eng kichik qiymatga oʻtadi.
Q: C++'da `(double) 7 / 2` va `(double) (7 / 2)` qanday farq qiladi?
A: Birinchisi 3.5 (avval 7 `double` ga oʻtadi), ikkinchisi 3.0 (avval butun boʻlish 3 beradi, keyin u `double` ga oʻtadi).
Q: Pythonʼda `1 2 3` qatoridan uchta sonni qanday oʻqiysiz?
A: `a, b, c = map(int, input().split())`.
Q: `n = 200` boʻlsa, `n // 60` va `n % 60` nimaga teng?
A: 3 va 20: 200 soniya — 3 daqiqa 20 soniya.
Q: 5 sonini `05` koʻrinishida qanday chiqarasiz (Python va Java)?
A: Python: `f"{5:02d}"`, Java: `String.format("%02d", 5)`.
:::

:::link https://docs.python.org/3/library/string.html#format-specification-mini-language
Python: format spetsifikatsiyasi
`.2f`, `02d` va boshqa formatlar haqida rasmiy hujjat.
:::

:::link https://en.cppreference.com/w/cpp/io/manip
cppreference: kiritish-chiqarish manipulyatorlari
`setw`, `setfill`, `setprecision`, `fixed` va boshqalar.
:::
