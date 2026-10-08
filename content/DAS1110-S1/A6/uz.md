---
title: Takrorlanuvchi jarayonlar. Parametrli va old shartli takrorlash operatorlari
summary: for va while bilan amaliy masalalar: koʻpaytirish jadvali, Evklid algoritmi, 0 kiritilguncha oʻqish (toʻxtatuvchi qiymat); qaysi siklni tanlash.
---

## Nima uchun bu mavzu muhim?

Kassa apparati xaridor toʻlovni tugatguncha mahsulotlarni birma-bir qabul qiladi — nechta boʻlishi oldindan nomaʼlum. Oylik hisobot esa 30 kunning har biri uchun aniq bir marta tuziladi. Birinchi holat — `while`, ikkinchisi — `for`. Toʻgʻri siklni tanlash dasturni sodda va xatosiz qiladi.

Maʼruzada sikllarning turlarini koʻrdik. Bu amaliy mashgʻulotda ularni uchta tipik masalada qoʻllaymiz va har bir sikl uchun „qachon toʻxtaydi?“ savoliga javob berishni mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- takrorlar soni maʼlum masalani `for` bilan yecha olasiz;
- takrorlar soni shart bilan belgilanadigan masalani `while` bilan yecha olasiz;
- „toʻxtatuvchi qiymat“ (masalan, 0) kiritilguncha maʼlumot oʻqiy olasiz;
- siklning toʻxtashini isbotlay olasiz va qiymatlar jadvali bilan tekshira olasiz.
:::

## for yoki while?

| Savol | Javob „ha“ boʻlsa |
|---|---|
| Takrorlar soni sikl boshlanishidan oldin maʼlummi? (*n* marta, 1 dan 100 gacha, har bir kun uchun) | `for` |
| Sikl biror hodisa roʻy berguncha davom etadimi? (0 kiritilguncha, son 1 boʻlguncha, qoldiq 0 boʻlguncha) | `while` |

Har qanday `for` ni `while` bilan yozish mumkin va aksincha, lekin mos tanlangan sikl qisqa va tushunarli boʻladi:

| `for` | Xuddi shu `while` bilan |
|---|---|
| `for i in range(1, n + 1):` | `i = 1` → `while i <= n:` → … → `i += 1` |
| `for (int i = 1; i <= n; i++)` | `int i = 1; while (i <= n) { … i++; }` |

`while` da hisoblagichni oshirishni unutish oson — `for` da u sarlavhaning oʻzida.

## Siklning toʻxtashini tekshirish

Har bir `while` sikli uchun ikki narsani aniqlang:

1. **Toʻxtash sharti** — shart qachon yolgʻon boʻladi?
2. **Progress** — har iteratsiyada nima oʻzgaradi va u toʻxtash shartiga yaqinlashtiradimi?

Masalan, Evklid algoritmida har iteratsiyada ikkinchi son qatʼiy kamayadi va manfiy boʻlmaydi — demak, u qachondir 0 ga yetadi va sikl toʻxtaydi.

## Ishlangan misol 1: koʻpaytirish jadvali

Butun son *n* kiritiladi. Uning 1 dan 10 gacha koʻpaytirish jadvalini chiqaring.

```python
n = int(input())

for i in range(1, 11):
    print(f"{n} x {i} = {n * i}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        for (int i = 1; i <= 10; i++) {
            System.out.println(n + " x " + i + " = " + n * i);
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

    for (int i = 1; i <= 10; i++) {
        cout << n << " x " << i << " = " << n * i << endl;
    }
    return 0;
}
```

Kirish `7` boʻlsa, sikl 10 marta ishlaydi va chiqaradi:

```text
7 x 1 = 7
7 x 2 = 14
…
7 x 10 = 70
```

Takrorlar soni (10) oldindan maʼlum — `for` tabiiy tanlov. Pythonʼda `range(1, 11)` — 11 kirmaydi, shuning uchun 10 ta qiymat.

## Ishlangan misol 2: Evklid algoritmi (EKUB)

Ikki natural son *a* va *b* kiritiladi. Ularning eng katta umumiy boʻluvchisini (EKUB) toping.

**Evklid algoritmi**: EKUB(*a*, *b*) = EKUB(*b*, *a* mod *b*), va EKUB(*a*, 0) = *a*. Yaʼni *b* ≠ 0 ekan, juftlikni (*b*, *a* % *b*) ga almashtiramiz.

```python
a, b = map(int, input().split())

while b != 0:
    qoldiq = a % b
    a = b
    b = qoldiq

print("EKUB:", a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        while (b != 0) {
            int qoldiq = a % b;
            a = b;
            b = qoldiq;
        }

        System.out.println("EKUB: " + a);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;

    while (b != 0) {
        int qoldiq = a % b;
        a = b;
        b = qoldiq;
    }

    cout << "EKUB: " << a << endl;
    return 0;
}
```

Kirish `48 18` boʻlsa:

| *a* | *b* | *a* % *b* |
|---|---|---|
| 48 | 18 | 12 |
| 18 | 12 | 6 |
| 12 | 6 | 0 |
| 6 | 0 | sikl tugaydi |

Chiqadi: `EKUB: 6`. Nechta iteratsiya boʻlishi oldindan nomaʼlum — shuning uchun `while`. Toʻxtash kafolati: *b* har qadamda qatʼiy kamayadi (qoldiq boʻluvchidan kichik). Bu algoritm 2300 yildan ortiq tarixga ega va hozir ham kriptografiyada ishlatiladi.

## Ishlangan misol 3: 0 kiritilguncha oʻqish

Har bir qatorda bittadan butun son kiritiladi; ketma-ketlik 0 bilan tugaydi (0 ning oʻzi hisobga kirmaydi). Sonlar yigʻindisini, sonini va oʻrtachasini chiqaring.

Bunday oxirni bildiruvchi qiymat **toʻxtatuvchi (sentinel) qiymat** deyiladi.

```python
yigindi = 0
soni = 0

x = int(input())
while x != 0:
    yigindi += x
    soni += 1
    x = int(input())

if soni > 0:
    print(f"Yigʻindi: {yigindi}, soni: {soni}, oʻrtacha: {yigindi / soni:.2f}")
else:
    print("Sonlar kiritilmadi")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int yigindi = 0;
        int soni = 0;

        int x = in.nextInt();
        while (x != 0) {
            yigindi += x;
            soni++;
            x = in.nextInt();
        }

        if (soni > 0) {
            System.out.println(String.format("Yigʻindi: %d, soni: %d, oʻrtacha: %.2f",
                    yigindi, soni, (double) yigindi / soni));
        } else {
            System.out.println("Sonlar kiritilmadi");
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int yigindi = 0;
    int soni = 0;

    int x;
    cin >> x;
    while (x != 0) {
        yigindi += x;
        soni++;
        cin >> x;
    }

    if (soni > 0) {
        cout << "Yigʻindi: " << yigindi << ", soni: " << soni << ", oʻrtacha: "
             << fixed << setprecision(2) << (double) yigindi / soni << endl;
    } else {
        cout << "Sonlar kiritilmadi" << endl;
    }
    return 0;
}
```

Kirish `5`, `3`, `-2`, `0` boʻlsa:

1. Birinchi son sikldan **oldin** oʻqiladi: *x* = 5.
2. 5 ≠ 0 — qoʻshiladi, keyingi son oʻqiladi: 3. Keyin −2.
3. *x* = 0 — sikl tugaydi; 0 yigʻindiga qoʻshilmaydi.
4. Chiqadi: `Yigʻindi: 6, soni: 3, oʻrtacha: 2.00`.

Bu „oldindan oʻqish“ andozasi: birinchi qiymat sikldan oldin, keyingilari tananing **oxirida** oʻqiladi. Agar birinchi son 0 boʻlsa, sikl bir marta ham ishlamaydi va nolga boʻlishning oldini olish uchun `soni > 0` tekshiriladi.

:::warning
**Koʻp uchraydigan xatolar**

- **`while` da hisoblagichni oshirishni unutish** — cheksiz sikl.
- **Toʻxtatuvchi qiymatni ham hisobga qoʻshish.** Agar oʻqish tananing boshida boʻlsa, 0 ham qoʻshiladi va `soni` bittaga koʻpayadi.
- **Boʻsh ketma-ketlikda nolga boʻlish.** Birinchi son 0 boʻlsa, `soni` = 0 — oʻrtacha hisoblab boʻlmaydi.
- **Evklid algoritmida tartib.** `a = b; b = a % b;` — *a* allaqachon oʻzgargan, natija notoʻgʻri. Qoldiqni avval vaqtinchalik oʻzgaruvchiga saqlang.
:::

:::tip
`while` siklini yozib boʻlgach, uchta holatda tekshiring: sikl bir marta ham ishlamaydi (darhol 0), bir marta ishlaydi, koʻp marta ishlaydi. Xatolarning aksariyati birinchi ikkitasida chiqadi.
:::

## Xulosa

- Takrorlar soni oldindan maʼlum boʻlsa — `for`, shart bilan belgilansa — `while`.
- Har bir `while` uchun toʻxtash sharti va har iteratsiyadagi progressni aniqlang.
- Evklid algoritmi: *b* ≠ 0 ekan, (*a*, *b*) ← (*b*, *a* % *b*); javob — *a*.
- Toʻxtatuvchi qiymat bilan oʻqishda birinchi qiymat sikldan oldin, keyingilari tana oxirida oʻqiladi.
- Sikl 0, 1 va koʻp marta ishlagan holatlarda tekshiriladi.

## Oʻzingizni tekshiring

:::check
Q: 1 dan 100 gacha sonlarning yigʻindisi uchun qaysi sikl qulayroq va nima uchun?
A: `for`: takrorlar soni (100) oldindan maʼlum.
Q: EKUB(84, 36) ni Evklid algoritmi bilan hisoblang.
A: (84, 36) → (36, 12) → (12, 0); EKUB = 12.
Q: Toʻxtatuvchi qiymatli dasturda nima uchun birinchi son sikldan oldin oʻqiladi?
A: Shart (`x != 0`) birinchi tekshirilganda *x* allaqachon qiymatga ega boʻlishi kerak; keyingi sonlar tana oxirida oʻqiladi, shunda 0 hisobga kirmaydi.
Q: Birinchi kiritilgan son 0 boʻlsa, 3-misoldagi dastur nima chiqaradi?
A: `Sonlar kiritilmadi`: sikl bir marta ham ishlamaydi va `soni` = 0.
Q: `for (int i = 10; i > 0; i -= 3)` sikli nechta iteratsiya bajaradi?
A: 4 ta: *i* = 10, 7, 4, 1.
:::

:::link https://visualgo.net/en
VisuAlgo: algoritmlar vizualizatsiyasi
Algoritmlarning qadamma-qadam animatsiyasi.
:::

:::link https://docs.python.org/3/reference/compound_stmts.html#the-while-statement
Python: while operatori
Python tilining rasmiy maʼlumotnomasidan.
:::
