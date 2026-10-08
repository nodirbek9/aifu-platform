---
title: Takrorlanuvchi jarayonlar. Soʻng shartli takrorlash operatori. Ichma-ich takrorlanish
summary: do-while sikli va uning Pythonʼdagi oʻrnini bosuvchi while True + break; kiritishni tekshirish; ichma-ich sikllar, tashqi va ichki hisoblagich, ichki sikldagi break.
---

## Nima uchun bu mavzu muhim?

Bankomat PIN-kodni soʻraydi — notoʻgʻri boʻlsa, yana soʻraydi. Soʻrov kamida bir marta boʻlishi aniq, necha marta takrorlanishi esa nomaʼlum. Bu — **soʻng shartli sikl**ning klassik holati.

Dars jadvali esa haftaning har bir kuni uchun, har bir kunning har bir parasi uchun tuziladi: sikl ichida sikl. Jadvallar, matritsalar, juftliklarni saralash — bularning hammasi **ichma-ich sikllar** bilan yoziladi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- `do-while` siklini Java va C++'da, uning oʻrnini bosuvchi `while True` + `break` ni Pythonʼda yoza olasiz;
- foydalanuvchi toʻgʻri qiymat kiritguncha soʻrovni takrorlay olasiz;
- ichma-ich sikllar bilan jadval va shakllar chiqara olasiz;
- ichma-ich sikl necha marta bajarilishini hisoblay olasiz va ichki sikldagi `break` ning taʼsirini tushuntira olasiz.
:::

## do-while sikli

**Soʻng shartli sikl** avval tanani bajaradi, keyin shartni tekshiradi:

| Til | Yozilishi |
|---|---|
| Java, C++ | `do { tana } while (shart);` |
| Python | `while True:` → tana → `if not shart: break` |

Pythonʼda `do-while` yoʻq. Uning oʻrniga **cheksiz sikl va undan chiqish** andozasi ishlatiladi: `while True:` ichida tana, oxirida shart yolgʻon boʻlsa `break`.

`while` va `do-while` farqi faqat birinchi tekshiruvda: `while` tanani bir marta ham bajarmasligi mumkin, `do-while` esa kamida bir marta bajaradi. Shuning uchun `do-while` „avval qil, keyin tekshir“ masalalarida tabiiy: kiritishni tekshirish, menyu, „yana oʻynaysizmi?“.

## Ichma-ich sikllar

Bir siklning tanasida boshqa sikl boʻlsa — bu **ichma-ich (nested) sikl**. Tashqi siklning har bir iteratsiyasida ichki sikl **toʻliq** bajariladi.

Agar tashqi sikl *n* marta, ichki sikl *m* marta ishlasa, ichki tana *n* · *m* marta bajariladi. Soat misoli: soat milining bir aylanishida daqiqa mili 60 marta qadam tashlaydi; 24 soatda — 24 · 60 = 1440 marta.

Ichki siklning chegarasi tashqi hisoblagichga bogʻliq boʻlishi mumkin: `for j in range(1, i + 1)` — bunday sikl „uchburchak“ shakl beradi va ichki tana 1 + 2 + … + *n* = *n*(*n* + 1)/2 marta bajariladi.

Ikki muhim qoida:

1. Tashqi va ichki hisoblagichlarga **turli nom** bering (`i`, `j`). Bir xil nom — eng koʻp uchraydigan xato.
2. `break` faqat **oʻzi turgan** (ichki) siklni tugatadi; tashqi sikl davom etadi.

## Ishlangan misol 1: kiritishni tekshirish

Foydalanuvchi 1 dan 100 gacha son kiritishi kerak. Notoʻgʻri son kiritilsa, `Oraliqdan tashqarida` deb, qayta soʻrang. Toʻgʻri son kiritilganda `Qabul qilindi: <son>` chiqaring.

```python
while True:
    x = int(input())
    if 1 <= x <= 100:
        break
    print("Oraliqdan tashqarida")

print("Qabul qilindi:", x)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int x;
        boolean notogri;

        do {
            x = in.nextInt();
            notogri = x < 1 || x > 100;
            if (notogri) {
                System.out.println("Oraliqdan tashqarida");
            }
        } while (notogri);

        System.out.println("Qabul qilindi: " + x);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int x;
    bool notogri;

    do {
        cin >> x;
        notogri = x < 1 || x > 100;
        if (notogri) {
            cout << "Oraliqdan tashqarida" << endl;
        }
    } while (notogri);

    cout << "Qabul qilindi: " << x << endl;
    return 0;
}
```

Kirish `150`, `-3`, `42` boʻlsa:

1. 1-iteratsiya: *x* = 150 — notoʻgʻri, xabar chiqadi, sikl davom etadi.
2. 2-iteratsiya: *x* = −3 — notoʻgʻri, yana xabar.
3. 3-iteratsiya: *x* = 42 — toʻgʻri, sikl tugaydi.
4. Chiqadi: ikki marta `Oraliqdan tashqarida`, keyin `Qabul qilindi: 42`.

Son kamida bir marta oʻqilishi kerak — shuning uchun soʻng shartli sikl. `while` bilan yozilsa, oʻqishni sikldan oldin ham, ichida ham takrorlash kerak boʻlardi.

## Ishlangan misol 2: yulduzchalar uchburchagi

Natural son *n* kiritiladi. *n* qatorli uchburchak chiqaring: *i*-qatorda *i* ta yulduzcha.

```python
n = int(input())

for i in range(1, n + 1):
    for j in range(i):
        print("*", end="")
    print()
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            cout << "*";
        }
        cout << endl;
    }
    return 0;
}
```

Kirish `4` boʻlsa, chiqadi:

```text
*
**
***
****
```

Tashqi sikl qatorlarni, ichki sikl qatordagi belgilarni sanaydi. Yangi qator ichki sikldan **keyin**, lekin tashqi siklning ichida chiqariladi. Ichki tana 1 + 2 + 3 + 4 = 10 marta bajariladi. (Pythonʼda qisqasi bor: `print("*" * i)` — lekin bu yerda ichma-ich siklni mashq qilyapmiz.)

## Ishlangan misol 3: Pifagor uchliklari

Natural son *n* kiritiladi. *a* < *b* < *c* ≤ *n* va *a*² + *b*² = *c*² boʻlgan barcha uchliklarni chiqaring.

```python
n = int(input())

for a in range(1, n + 1):
    for b in range(a + 1, n + 1):
        for c in range(b + 1, n + 1):
            if a * a + b * b == c * c:
                print(a, b, c)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int a = 1; a <= n; a++) {
            for (int b = a + 1; b <= n; b++) {
                for (int c = b + 1; c <= n; c++) {
                    if (a * a + b * b == c * c) {
                        System.out.println(a + " " + b + " " + c);
                    }
                }
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    for (int a = 1; a <= n; a++) {
        for (int b = a + 1; b <= n; b++) {
            for (int c = b + 1; c <= n; c++) {
                if (a * a + b * b == c * c) {
                    cout << a << " " << b << " " << c << endl;
                }
            }
        }
    }
    return 0;
}
```

Kirish `20` boʻlsa, chiqadi:

```text
3 4 5
5 12 13
6 8 10
8 15 17
9 12 15
12 16 20
```

Ichki sikllar *a* + 1 va *b* + 1 dan boshlanadi — shunda har bir uchlik bir marta chiqadi (`4 3 5` takrorlanmaydi) va *a* < *b* < *c* sharti avtomatik bajariladi. Bu **toʻliq tanlash (brute force)**: barcha variantlar tekshiriladi. Uch qavatli sikl taxminan *n*³/6 marta ishlaydi — *n* = 20 uchun ~1140, *n* = 1000 uchun ~166 million. Katta *n* da tezroq algoritm kerak (masalan, *c* ni √(*a*² + *b*²) dan hisoblash).

:::warning
**Koʻp uchraydigan xatolar**

- **Ichki va tashqi siklda bir xil hisoblagich.** `for i …: for i …:` — ichki sikl tashqisini buzadi.
- **Yangi qatorni notoʻgʻri joyda chiqarish.** Ichki sikl ichida — har belgi alohida qatorda; tashqi sikldan keyin — hammasi bitta qatorda.
- **`break` tashqi siklni ham toʻxtatadi deb oʻylash.** U faqat ichki siklni tugatadi; tashqisidan chiqish uchun bayroq kerak.
- **`do-while` oxirida `;` ni unutish** (Java, C++) — kompilyatsiya xatosi.
- **Pythonʼda `while True:` dan chiqishni unutish** — cheksiz sikl.
:::

:::tip
Ichma-ich siklni yozishdan oldin kichik *n* uchun natijani qogʻozga chizing va „tashqi sikl nimani sanaydi? ichki-chi?“ savoliga javob bering.
:::

## Xulosa

- `do-while` tanani kamida bir marta bajaradi; Pythonʼda uning oʻrniga `while True:` va `break`.
- Kiritishni tekshirish — soʻng shartli siklning tipik qoʻllanishi.
- Tashqi siklning har iteratsiyasida ichki sikl toʻliq bajariladi; jami *n* · *m* marta.
- Ichki sikl chegarasi tashqi hisoblagichga bogʻliq boʻlishi mumkin (uchburchak shakllar).
- `break` faqat oʻzi turgan siklni tugatadi.

## Oʻzingizni tekshiring

:::check
Q: `do { … } while (false);` sikli necha marta bajariladi?
A: Bir marta: tana shart tekshirilishidan oldin bajariladi.
Q: Pythonʼda `do-while` ni qanday yozasiz?
A: `while True:` ichida tana, oxirida `if not shart: break`.
Q: `for i in range(5): for j in range(3): print(i, j)` necha qator chiqaradi?
A: 15: tashqi sikl 5 marta, har birida ichki sikl 3 marta.
Q: 2-misolda `print()` ichki sikl ichiga koʻchirilsa, nima boʻladi?
A: Har bir yulduzcha alohida qatorda chiqadi: jami 10 qator, har birida bitta `*`.
Q: 3-misolda ichki sikllar 1 dan boshlansa, natija qanday oʻzgaradi?
A: Har bir uchlik ikki marta chiqadi (`3 4 5` va `4 3 5`), ishlash vaqti esa oshadi. *c* ham 1 dan boshlansa, *c* < *b* holatlar ham behuda tekshiriladi.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/while.html
Oracle Java Tutorial: while va do-while
Ikkala siklning farqi haqida rasmiy qoʻllanma.
:::

:::link https://en.cppreference.com/w/cpp/language/do
cppreference: do-while sikli
C++'da `do-while` sintaksisi.
:::
