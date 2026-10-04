---
title: Bir oʻlchamli massivlar. Statik massivlar. Elementlarni saralash va qidirish usullari
summary: C++'da statik massiv, chiziqli qidiruv, pufakchali saralash, ikkilik (binar) qidiruv; algoritmlar tezligini solishtirish.
---

## Nima uchun bu mavzu muhim?

Telefon kitobchasida „Rahimov“ni qanday qidirasiz? Birinchi sahifadan boshlab har bir ismni oʻqib chiqmaysiz: kitobni oʻrtasidan ochasiz, „R“ harfi oldinda yoki orqada ekanini koʻrasiz va qidiruvni ikki baravar qisqartirasiz. Bu faqat ismlar **alifbo tartibida** boʻlgani uchun mumkin.

Saralash va qidirish — dasturlashning eng koʻp ishlatiladigan amallari: internet-doʻkonda narx boʻyicha tartiblash, reytingda oʻrinni aniqlash, bazadan talabani topish. Bu amaliy mashgʻulotda ularning eng sodda va eng muhim usullarini qoʻlda yozamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- C++'da oʻlchami oldindan belgilangan statik massivni eʼlon qila olasiz;
- chiziqli qidiruv bilan elementning oʻrnini topa olasiz;
- pufakchali saralash algoritmini yozib, uning ishlashini qadamma-qadam tushuntira olasiz;
- saralangan massivda ikkilik qidiruvni qoʻllay olasiz va uning nega tezligini asoslay olasiz.
:::

## Statik massiv (C++)

**Statik massiv** — oʻlchami kompilyatsiya paytida maʼlum boʻlgan, dastur davomida oʻzgarmaydigan massiv:

| Yozuv | Maʼnosi |
|---|---|
| `int a[100];` | 100 ta `int` uchun joy; qiymatlari aniqlanmagan („axlat“) |
| `int a[5] = {3, 1, 4, 1, 5};` | qiymatlar bilan |
| `int a[100] = {};` | 100 ta nol |
| `const int MAX = 1000;` `int a[MAX];` | oʻlcham oʻzgarmas orqali |

Masalada „*n* ≤ 1000“ deyilsa, massiv eng katta holat uchun (`MAX = 1000`) eʼlon qilinadi va faqat birinchi *n* ta element ishlatiladi. Statik massivning uzunligini u „bilmaydi“ — *n* ni alohida oʻzgaruvchida saqlash kerak.

Java va Python'da statik massiv yoʻq: Java massivi `new int[n]` bilan ish paytida yaratiladi (uzunligi keyin oʻzgarmaydi), Python roʻyxati esa hajmini oʻzgartira oladi.

## Chiziqli qidiruv

**Chiziqli qidiruv** — elementlarni boshidan boshlab birma-bir tekshirish, kerakligi topilguncha. Topilmasa, odatda −1 qaytariladi.

- Massiv saralangan boʻlishi shart emas.
- Eng yomon holatda barcha *n* element tekshiriladi.

## Pufakchali saralash

**Pufakchali saralash (bubble sort)** — qoʻshni elementlarni solishtirib, notoʻgʻri tartibdagilarini almashtirish. Har bir oʻtishda eng katta element „pufakcha“ kabi oxirga suzib chiqadi.

1. *i*-oʻtishda qoʻshni juftliklar `a[j]` va `a[j + 1]` (*j* = 0 … *n* − 2 − *i*) solishtiriladi.
2. `a[j] > a[j + 1]` boʻlsa — almashtiriladi.
3. Bir oʻtishda birorta ham almashtirish boʻlmasa, massiv saralangan — toʻxtash mumkin.

Har oʻtishdan keyin oxirgi element oʻz joyida, shuning uchun keyingi oʻtish bittaga qisqaroq. Eng yomon holatda taxminan *n*²/2 ta solishtirish bajariladi.

Boshqa oddiy usullar — **tanlab saralash (selection sort)**: eng kichikni topib, boshiga qoʻyish; **qoʻyib saralash (insertion sort)**: har bir elementni saralangan qismga oʻz joyiga qoʻyish. Amalda esa standart funksiyalar ishlatiladi: Python `a.sort()`, Java `Arrays.sort(a)`, C++ `sort(a, a + n)` — ular ~*n* log *n* vaqtda ishlaydi.

## Ikkilik qidiruv

**Ikkilik (binar) qidiruv** faqat **saralangan** massivda ishlaydi:

1. Qidiruv oraligʻi [*chap*, *ong*] — boshida butun massiv.
2. Oʻrtadagi element olinadi: *orta* = (*chap* + *ong*) / 2.
3. Agar `a[orta]` qidirilayotgan son boʻlsa — topildi. Kichik boʻlsa — qidiruv oʻng yarmida (*chap* = *orta* + 1), katta boʻlsa — chap yarmida (*ong* = *orta* − 1).
4. Oraliq boʻsh boʻlguncha (*chap* > *ong*) takrorlanadi.

Har qadamda oraliq ikki baravar qisqaradi:

| Elementlar soni | Chiziqli qidiruv (eng koʻp) | Ikkilik qidiruv (eng koʻp) |
|---|---|---|
| 1 000 | 1 000 | 10 |
| 1 000 000 | 1 000 000 | 20 |
| 1 000 000 000 | 1 000 000 000 | 30 |

## Ishlangan misol 1: chiziqli qidiruv

Birinchi qatorda *n* (*n* ≤ 1000), ikkinchisida *n* ta son, uchinchisida qidirilayotgan son *x*. *x* birinchi marta uchragan oʻrinni (1 dan boshlab) chiqaring; topilmasa `-1`.

```python
n = int(input())
a = list(map(int, input().split()))
x = int(input())

orin = -1
for i in range(n):
    if a[i] == x:
        orin = i + 1
        break

print(orin)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        int x = in.nextInt();

        int orin = -1;
        for (int i = 0; i < n; i++) {
            if (a[i] == x) {
                orin = i + 1;
                break;
            }
        }

        System.out.println(orin);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];                    // statik massiv: eng katta holat uchun joy

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    int x;
    cin >> x;

    int orin = -1;
    for (int i = 0; i < n; i++) {
        if (a[i] == x) {
            orin = i + 1;
            break;
        }
    }

    cout << orin << endl;
    return 0;
}
```

Kirish `6`, `4 8 15 16 23 42`, `16` boʻlsa: 4, 8, 15 tekshiriladi, 4-element 16 — `orin = 4`, `break`. Chiqadi: `4`. *x* = 7 boʻlsa, barcha 6 element tekshiriladi va `-1` chiqadi.

C++ versiyasida massiv `main` dan tashqarida — **global** eʼlon qilingan: global massivlar avtomatik nollar bilan toʻldiriladi va katta oʻlchamda ham stekni toʻldirib yubormaydi.

## Ishlangan misol 2: pufakchali saralash

Birinchi qatorda *n*, ikkinchisida *n* ta son. Massivni oʻsish tartibida saralang va chiqaring.

```python
n = int(input())
a = list(map(int, input().split()))

for i in range(n - 1):
    almashdi = False
    for j in range(n - 1 - i):
        if a[j] > a[j + 1]:
            a[j], a[j + 1] = a[j + 1], a[j]
            almashdi = True
    if not almashdi:
        break

print(*a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }

        for (int i = 0; i < n - 1; i++) {
            boolean almashdi = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int t = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = t;
                    almashdi = true;
                }
            }
            if (!almashdi) {
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    for (int i = 0; i < n - 1; i++) {
        bool almashdi = false;
        for (int j = 0; j < n - 1 - i; j++) {
            if (a[j] > a[j + 1]) {
                int t = a[j];
                a[j] = a[j + 1];
                a[j + 1] = t;
                almashdi = true;
            }
        }
        if (!almashdi) {
            break;
        }
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

Kirish `5` va `5 1 4 2 8` boʻlsa:

| Oʻtish | Massiv oʻtishdan keyin | Oʻz joyiga tushgan |
|---|---|---|
| 1 | 1 4 2 5 8 | 8 |
| 2 | 1 2 4 5 8 | 5 |
| 3 | 1 2 4 5 8 (almashtirish yoʻq) | — toʻxtash |

Chiqadi: `1 2 4 5 8`. 3-oʻtishda birorta almashtirish boʻlmagani uchun `almashdi` bayrogʻi soxta qoldi va saralash muddatidan oldin tugadi. Allaqachon saralangan massiv bitta oʻtishda tekshiriladi.

## Ishlangan misol 3: ikkilik qidiruv

Birinchi qatorda *n*, ikkinchisida oʻsish tartibida saralangan *n* ta son, uchinchisida *x*. *x* ning oʻrnini (1 dan) va nechta qadamda topilganini chiqaring; topilmasa `-1` va qadamlar soni.

```python
n = int(input())
a = list(map(int, input().split()))
x = int(input())

chap, ong = 0, n - 1
orin = -1
qadam = 0
while chap <= ong:
    qadam += 1
    orta = (chap + ong) // 2
    if a[orta] == x:
        orin = orta + 1
        break
    elif a[orta] < x:
        chap = orta + 1
    else:
        ong = orta - 1

print(orin, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        int x = in.nextInt();

        int chap = 0, ong = n - 1;
        int orin = -1;
        int qadam = 0;
        while (chap <= ong) {
            qadam++;
            int orta = (chap + ong) / 2;
            if (a[orta] == x) {
                orin = orta + 1;
                break;
            } else if (a[orta] < x) {
                chap = orta + 1;
            } else {
                ong = orta - 1;
            }
        }

        System.out.println(orin + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    int x;
    cin >> x;

    int chap = 0, ong = n - 1;
    int orin = -1;
    int qadam = 0;
    while (chap <= ong) {
        qadam++;
        int orta = (chap + ong) / 2;
        if (a[orta] == x) {
            orin = orta + 1;
            break;
        } else if (a[orta] < x) {
            chap = orta + 1;
        } else {
            ong = orta - 1;
        }
    }

    cout << orin << " " << qadam << endl;
    return 0;
}
```

Kirish `10`, `3 7 11 15 19 23 27 31 35 39`, `27` boʻlsa:

| Qadam | *chap* | *ong* | *orta* | `a[orta]` | Qaror |
|---|---|---|---|---|---|
| 1 | 0 | 9 | 4 | 19 | 19 < 27 → oʻngga |
| 2 | 5 | 9 | 7 | 31 | 31 > 27 → chapga |
| 3 | 5 | 6 | 5 | 23 | 23 < 27 → oʻngga |
| 4 | 6 | 6 | 6 | 27 | topildi |

Chiqadi: `7 4` — 7-oʻrinda, 4 qadamda. Chiziqli qidiruv 7 qadam qilardi; 1 000 000 element uchun farq 20 ga qarshi milliongacha. *x* = 20 boʻlsa, oraliq boʻshab qoladi va `-1 3` chiqadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Saralanmagan massivda ikkilik qidiruv.** Natija notoʻgʻri boʻladi — xato xabarisiz.
- **`while chap < ong`.** Oraliqda bitta element qolganda u tekshirilmaydi; shart `<=` boʻlishi kerak.
- **`chap = orta` yoki `ong = orta`.** Oraliq qisqarmay qolishi mumkin — cheksiz sikl. `orta + 1` va `orta - 1` yozing.
- **Pufakchali saralashda ichki sikl `j < n - i`.** Oxirgi iteratsiyada `a[j + 1]` chegaradan chiqadi; toʻgʻrisi `j < n - 1 - i`.
- **C++'da lokal statik massiv qiymatlarini nol deb hisoblash.** `main` ichidagi `int a[100];` „axlat“ bilan toʻla; `= {}` bilan nollang yoki global eʼlon qiling.
:::

:::tip
Saralash va qidiruvni avval 5–6 ta sonli qogʻozdagi misolda qoʻlda bajaring, har qadamdagi *chap*, *ong*, *orta* qiymatlarini jadvalga yozing. Dastur natijasi jadval bilan mos kelsa, algoritm toʻgʻri tushunilgan.
:::

## Xulosa

- C++ statik massivi `int a[MAX];` — oʻlchami oldindan, eng katta holat uchun; *n* alohida saqlanadi.
- Chiziqli qidiruv istalgan massivda ishlaydi, eng koʻp *n* qadam.
- Pufakchali saralash qoʻshnilarni almashtiradi; almashtirish boʻlmasa, erta toʻxtaydi; ~*n*²/2 solishtirish.
- Ikkilik qidiruv faqat saralangan massivda, har qadamda oraliqni ikkiga boʻladi: ~log₂ *n* qadam.
- Amalda standart `sort` funksiyalari ishlatiladi; qoʻlda yozish algoritmni tushunish uchun.

## Oʻzingizni tekshiring

:::check
Q: Chiziqli qidiruv uchun massiv saralangan boʻlishi kerakmi? Ikkilik qidiruv uchun-chi?
A: Chiziqli qidiruv uchun shart emas; ikkilik qidiruv uchun shart.
Q: `3 1 2` massivi pufakchali saralashning birinchi oʻtishidan keyin qanday koʻrinishda boʻladi?
A: `1 2 3`: 3 ↔ 1, keyin 3 ↔ 2.
Q: 1024 ta elementli saralangan massivda ikkilik qidiruv eng koʻpi bilan necha qadam qiladi?
A: 11 ta (2¹⁰ = 1024; har qadam oraliqni ikkiga boʻladi, oxirida bitta element ham tekshiriladi).
Q: Pufakchali saralashda `almashdi` bayrogʻi nima beradi?
A: Bir oʻtishda almashtirish boʻlmasa, massiv saralangan — qolgan oʻtishlar bajarilmaydi; saralangan massiv bitta oʻtishda tekshiriladi.
Q: C++'da `int a[5];` `main` ichida eʼlon qilinsa, `a[0]` nimaga teng?
A: Aniqlanmagan: xotiradagi tasodifiy qiymat. Global massiv yoki `int a[5] = {};` nollar bilan boshlanadi.
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: saralash algoritmlari
Pufakchali, tanlab, qoʻyib saralash va boshqalarning animatsiyasi.
:::

:::link https://en.cppreference.com/w/cpp/algorithm/sort
cppreference: std::sort
C++ standart saralash funksiyasi.
:::
