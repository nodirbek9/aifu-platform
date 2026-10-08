---
title: Takrorlanuvchi jarayonlarni tashkil etish
summary: Sikl tushunchasi, parametrli sikl (for), old shartli (while) va soʻng shartli (do-while) sikllar, hisoblagich va yigʻuvchi, break va continue, cheksiz sikl.
---

## Nima uchun bu mavzu muhim?

Dekanat 600 talabaning yakuniy balini hisoblashi kerak. Har bir talaba uchun alohida qator yozish — 600 qator bir xil kod. Ertaga talabalar 700 ta boʻlsa, dasturni qayta yozish kerakmi? Yoʻq: kompyuterga „har bir talaba uchun shu ishni takrorla“ deyish kifoya.

Kompyuterning asosiy kuchi aynan takrorlashda: bir xil amalni million marta xatosiz va charchamasdan bajaradi. Takrorlashni tashkil etuvchi tuzilma — **sikl (loop)**. Sikl va tarmoqlanish bilan istalgan algoritmni yozish mumkin.

:::note
**Bu darsdan soʻng siz:**

- siklning tanasi, iteratsiyasi, hisoblagichi va davom etish shartini tushuntira olasiz;
- `for`, `while` va `do-while` sikllarini uch tilda yoza olasiz va masalaga mosini tanlay olasiz;
- yigʻuvchi (yigʻindi, koʻpaytma) va hisoblagich bilan ishlaydigan dastur yoza olasiz;
- `break` va `continue` ni qoʻllay olasiz va cheksiz sikl xatosini topa olasiz.
:::

## Sikl tushunchasi

**Sikl** — bir guruh amallarni bir necha marta takrorlovchi tuzilma. Uning qismlari:

- **Sikl tanasi (body)** — takrorlanadigan amallar.
- **Iteratsiya** — tananing bir marta bajarilishi.
- **Davom etish sharti** — har iteratsiyadan oldin (yoki keyin) tekshiriladi; rost boʻlsa, sikl davom etadi.
- **Sikl oʻzgaruvchisi (hisoblagich)** — har iteratsiyada oʻzgaradigan va shartga taʼsir qiladigan oʻzgaruvchi.

Blok-sxemada sikl shart rombidan keyin orqaga — tananing boshiga qaytuvchi chiziq bilan tasvirlanadi.

Sikllarning uch turi bor:

| Tur | Qachon ishlatiladi | Shart qachon tekshiriladi |
|---|---|---|
| **Parametrli** (`for`) | takrorlar soni oldindan maʼlum | har iteratsiyadan oldin |
| **Old shartli** (`while`) | takrorlar soni noaniq, shart bilan belgilanadi | har iteratsiyadan oldin; tana bir marta ham bajarilmasligi mumkin |
| **Soʻng shartli** (`do-while`) | tana kamida bir marta bajarilishi kerak | har iteratsiyadan keyin |

## Parametrli sikl: for

**`for`** sikli hisoblagich bilan ishlaydi: boshlangʻich qiymat, chegaraviy shart va qadam.

| Til | 1 dan *n* gacha |
|---|---|
| Python | `for i in range(1, n + 1):` |
| Java, C++ | `for (int i = 1; i <= n; i++) { ... }` |

Pythonʼda `range(boshlanish, tugash, qadam)` — `tugash` **kirmaydi**: `range(1, 5)` → 1, 2, 3, 4. `range(5)` → 0, 1, 2, 3, 4. Teskari tartib: `range(10, 0, -1)` → 10, 9, …, 1.

Java va C++'da `for` qavsida uch qism bor: `int i = 1` (bir marta, boshida), `i <= n` (har iteratsiyadan oldin), `i++` (har iteratsiyadan keyin).

## Old shartli sikl: while

**`while`** — shart rost ekan, tanani takrorlaydi. Takrorlar soni oldindan noaniq boʻlganda qoʻllanadi: „son 0 boʻlguncha“, „foydalanuvchi toʻgʻri parol kiritguncha“, „qarz toʻlanguncha“.

| Til | Yozilishi |
|---|---|
| Python | `while shart:` |
| Java, C++ | `while (shart) { ... }` |

Sikl tanasida shartga taʼsir qiluvchi oʻzgaruvchi albatta oʻzgarishi kerak, aks holda sikl hech qachon tugamaydi — bu **cheksiz sikl**.

## Soʻng shartli sikl: do-while

**`do-while`** — avval tana bajariladi, keyin shart tekshiriladi. Shuning uchun tana kamida bir marta bajariladi. Java va C++'da bor: `do { ... } while (shart);` (oxirida nuqtali vergul!). Pythonʼda `do-while` yoʻq; uning oʻrniga `while True:` va ichida `break` ishlatiladi. Bu sikl A8 mashgʻulotida batafsil koʻriladi.

## Hisoblagich va yigʻuvchi

Sikl bilan ishlaydigan masalalarning koʻpi ikki andozaga tayanadi:

- **Yigʻuvchi (accumulator)** — sikldan oldin boshlangʻich qiymat beriladi (yigʻindi uchun 0, koʻpaytma uchun 1), har iteratsiyada yangi qiymat qoʻshiladi yoki koʻpaytiriladi: `s += x`, `p *= x`.
- **Hisoblagich (counter)** — shart bajarilgan holatlar sonini sanaydi: sikldan oldin `k = 0`, shart rost boʻlsa `k += 1`.

Boshlangʻich qiymat **sikldan oldin** beriladi. Sikl ichida bersangiz, u har iteratsiyada nolga tushib qoladi.

## break va continue

- **`break`** — siklni darhol tugatadi; boshqaruv sikldan keyingi operatorga oʻtadi. Masalan, kerakli element topilganda qidiruvni toʻxtatish.
- **`continue`** — joriy iteratsiyaning qolgan qismini tashlab, keyingi iteratsiyaga oʻtadi. Masalan, manfiy sonlarni oʻtkazib yuborish.

Ikkalasi ham uch tilda bir xil yoziladi.

## Ishlangan misol 1: faktorial

Butun son *n* (0 ≤ *n* ≤ 20) kiritiladi. *n*! = 1 · 2 · … · *n* ni hisoblang (0! = 1).

```python
n = int(input())

f = 1                          # koʻpaytma uchun boshlangʻich qiymat 1
for i in range(1, n + 1):
    f *= i

print(f"{n}! = {f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        long f = 1;                // 20! int ga sigʻmaydi, shuning uchun long
        for (int i = 1; i <= n; i++) {
            f *= i;
        }

        System.out.println(n + "! = " + f);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    long long f = 1;           // 20! int ga sigʻmaydi, shuning uchun long long
    for (int i = 1; i <= n; i++) {
        f *= i;
    }

    cout << n << "! = " << f << endl;
    return 0;
}
```

Kirish `5` boʻlsa, sikl qadamma-qadam:

| *i* | `f *= i` | *f* |
|---|---|---|
| boshida | — | 1 |
| 1 | 1 · 1 | 1 |
| 2 | 1 · 2 | 2 |
| 3 | 2 · 3 | 6 |
| 4 | 6 · 4 | 24 |
| 5 | 24 · 5 | 120 |

Chiqadi: `5! = 120`. Kirish `0` boʻlsa, sikl bir marta ham bajarilmaydi va `0! = 1` chiqadi — boshlangʻich qiymat toʻgʻri tanlangani uchun. `20! = 2432902008176640000` — bu son `int` ga sigʻmaydi, `long` / `long long` kerak.

## Ishlangan misol 2: son raqamlarining yigʻindisi

Musbat butun son kiritiladi. Uning raqamlari yigʻindisini va raqamlar sonini toping.

```python
n = int(input())

yigindi = 0
soni = 0
while n > 0:
    yigindi += n % 10          # oxirgi raqam
    n //= 10                   # oxirgi raqamni olib tashlaymiz
    soni += 1

print("Yigʻindi:", yigindi)
print("Raqamlar:", soni)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int yigindi = 0;
        int soni = 0;
        while (n > 0) {
            yigindi += n % 10;     // oxirgi raqam
            n /= 10;               // oxirgi raqamni olib tashlaymiz
            soni++;
        }

        System.out.println("Yigʻindi: " + yigindi);
        System.out.println("Raqamlar: " + soni);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;

    int yigindi = 0;
    int soni = 0;
    while (n > 0) {
        yigindi += n % 10;     // oxirgi raqam
        n /= 10;               // oxirgi raqamni olib tashlaymiz
        soni++;
    }

    cout << "Yigʻindi: " << yigindi << endl;
    cout << "Raqamlar: " << soni << endl;
    return 0;
}
```

Kirish `2026` boʻlsa:

| *n* (iteratsiya boshida) | `n % 10` | *yigindi* | *soni* |
|---|---|---|---|
| 2026 | 6 | 6 | 1 |
| 202 | 2 | 8 | 2 |
| 20 | 0 | 8 | 3 |
| 2 | 2 | 10 | 4 |
| 0 | — | sikl tugaydi | |

Chiqadi: `Yigʻindi: 10` va `Raqamlar: 4`. Takrorlar soni oldindan nomaʼlum (raqamlar soniga bogʻliq), shuning uchun `while` tanlandi. Har iteratsiyada *n* kamayadi — sikl albatta tugaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Cheksiz sikl.** `while (n > 0)` ichida *n* oʻzgarmasa, dastur hech qachon tugamaydi. Platformada bu „vaqt chegarasi oshdi“ natijasini beradi.
- **Bittaga xato (off-by-one).** `range(1, n)` *n* ni oʻz ichiga olmaydi; `i < n` va `i <= n` ni chalkashtirmang.
- **Yigʻuvchini sikl ichida nolga tenglash.** Natija faqat oxirgi qiymatga teng boʻlib qoladi.
- **Koʻpaytmani 0 dan boshlash.** `f = 0` boʻlsa, natija har doim 0.
- **`do-while` oxiridagi nuqtali vergulni unutish** — kompilyatsiya xatosi.
:::

:::tip
Sikl yozishdan oldin uch savolga javob bering: nima takrorlanadi? qachon toʻxtaydi? har iteratsiyada nima oʻzgaradi? Keyin kichik kirish uchun qiymatlar jadvalini qoʻlda toʻldirib, dasturni tekshiring.
:::

## Xulosa

- Sikl tanani shart rost ekan takrorlaydi; tananing bir marta bajarilishi — iteratsiya.
- `for` — takrorlar soni maʼlum boʻlganda; `while` — shart bilan, tana bir marta ham bajarilmasligi mumkin; `do-while` — tana kamida bir marta bajariladi.
- Pythonʼda `range(a, b)` da *b* kirmaydi; Java va C++'da `for (boshlash; shart; qadam)`.
- Yigʻuvchi sikldan oldin 0 (yigʻindi) yoki 1 (koʻpaytma) bilan boshlanadi.
- `break` siklni tugatadi, `continue` keyingi iteratsiyaga oʻtadi.
- Sikl tanasida shartga taʼsir qiluvchi oʻzgaruvchi oʻzgarishi kerak, aks holda sikl cheksiz.

## Oʻzingizni tekshiring

:::check
Q: Pythonʼda `range(2, 10, 3)` qanday qiymatlarni beradi?
A: 2, 5, 8 (10 kirmaydi).
Q: `while` va `do-while` ning asosiy farqi nimada?
A: `while` shartni tanadan oldin tekshiradi va tana bir marta ham bajarilmasligi mumkin; `do-while` avval tanani bajaradi, shuning uchun u kamida bir marta bajariladi.
Q: `s = 0; for i in range(1, 6): s += i` dan keyin *s* nimaga teng?
A: 15 (1 + 2 + 3 + 4 + 5).
Q: Quyidagi kodda qanday xato bor: `i = 1` `while i <= 10:` `print(i)`?
A: *i* sikl ichida oʻzgarmaydi — cheksiz sikl. Tanaga `i += 1` qoʻshish kerak.
Q: `break` va `continue` farqi nimada?
A: `break` siklni butunlay tugatadi; `continue` faqat joriy iteratsiyaning qolgan qismini tashlab, keyingi iteratsiyani boshlaydi.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#for-statements
Python: for va range
Rasmiy qoʻllanmadagi `for`, `range`, `break` va `continue` boʻlimlari.
:::

:::link https://en.cppreference.com/w/cpp/language/for
cppreference: for sikli
C++'da `for` siklining sintaksisi va misollar.
:::
