---
title: Koʻp oʻlchovli massivlar
summary: Ikki oʻlchovli massiv (matritsa), satr va ustun, a[i][j], uni yaratish, toʻldirish va chiqarish, satr va ustun yigʻindilari, diagonallar, transponirlash; Pythonʼdagi nusxa tuzogʻi.
---

## Nima uchun bu mavzu muhim?

Elektron jurnal — jadval: satrlarda talabalar, ustunlarda fanlar. Kinoteatr zali — qatorlar va oʻrinlar. Shaxmat taxtasi — 8 × 8 katak. Raqamli rasm — piksellar jadvali. Excel varagʻi — satrlar va ustunlar. Bunday maʼlumotni bitta qator bilan emas, **jadval** bilan ifodalash tabiiy.

Jadval koʻrinishidagi maʼlumot dasturda **ikki oʻlchovli massiv** — **matritsa** bilan saqlanadi. Bu dars matritsani yaratish, toʻldirish va uning ustida eng koʻp uchraydigan amallarni bajarishni oʻrgatadi.

:::note
**Bu darsdan soʻng siz:**

- ikki oʻlchovli massivning tuzilishini (satr, ustun, `a[i][j]`) tushuntira olasiz;
- Python, Java va C++'da *n* × *m* matritsa yaratib, uni toʻldirib, jadval koʻrinishida chiqara olasiz;
- satr va ustunlar yigʻindisini, bosh va yordamchi diagonal yigʻindisini hisoblay olasiz;
- matritsani transponirlay olasiz va Pythonʼdagi „bir xil satr“ tuzogʻidan qochasiz.
:::

## Ikki oʻlchovli massiv

**Ikki oʻlchovli massiv (matritsa)** — massivlar massivi: har bir elementi oʻzi bir massiv (satr). *n* × *m* matritsada *n* ta satr va *m* ta ustun bor.

| Tushuncha | Yozilishi | Misol (3 × 4 matritsa) |
|---|---|---|
| Element | `a[i][j]` — *i*-satr, *j*-ustun | `a[1][2]` — 2-satr, 3-ustun |
| Satrlar soni | *n* | 3 |
| Ustunlar soni | *m* | 4 |
| Satr indekslari | 0 … *n* − 1 | 0, 1, 2 |
| Ustun indekslari | 0 … *m* − 1 | 0, 1, 2, 3 |

Birinchi indeks — **satr**, ikkinchisi — **ustun**. Bu kelishuv jadvalni oʻqish tartibiga mos: avval qator, keyin undagi oʻrin.

Xotirada matritsa satrma-satr joylashadi: avval 0-satrning barcha elementlari, keyin 1-satrniki va hokazo. Shuning uchun matritsani satrlar boʻyicha aylanib chiqish (tashqi sikl — satr, ichki — ustun) tezroq ishlaydi.

**Kvadrat matritsa** — satrlar va ustunlar soni teng (*n* × *n*). Unda ikki diagonal bor:

| Diagonal | Elementlar | Shart |
|---|---|---|
| Bosh diagonal | `a[0][0]`, `a[1][1]`, …, `a[n-1][n-1]` | *i* = *j* |
| Yordamchi diagonal | `a[0][n-1]`, `a[1][n-2]`, …, `a[n-1][0]` | *i* + *j* = *n* − 1 |

## Matritsani yaratish

| Vazifa | Python | Java | C++ |
|---|---|---|---|
| *n* × *m* nollar | `a = [[0] * m for _ in range(n)]` | `int[][] a = new int[n][m];` | `vector<vector<int>> a(n, vector<int>(m));` |
| Qiymatlar bilan | `a = [[1, 2], [3, 4]]` | `int[][] a = {{1, 2}, {3, 4}};` | `vector<vector<int>> a = {{1, 2}, {3, 4}};` |
| Satrlar soni | `len(a)` | `a.length` | `a.size()` |
| Ustunlar soni | `len(a[0])` | `a[0].length` | `a[0].size()` |

C++'da klassik statik matritsa ham bor: `int a[10][10];` — oʻlchami oldindan belgilanadi.

### Pythonʼdagi tuzoq

`a = [[0] * m] * n` — **notoʻgʻri**! Bu *n* ta alohida satr emas, **bitta** satrga *n* ta havola yaratadi. `a[0][0] = 5` deb yozsangiz, barcha satrlarning birinchi elementi 5 boʻladi. Toʻgʻri usul — har bir satrni alohida yaratish: `[[0] * m for _ in range(n)]`.

## Matritsani aylanib chiqish

Matritsa **ichma-ich sikl** bilan aylanib chiqiladi: tashqi sikl satrlar boʻyicha, ichki sikl ustunlar boʻyicha.

| Vazifa | Tashqi sikl | Ichki sikl | Amal |
|---|---|---|---|
| Barcha elementlar | *i*: 0 … *n* − 1 | *j*: 0 … *m* − 1 | `a[i][j]` |
| *i*-satr yigʻindisi | — | *j* boʻyicha | `s += a[i][j]` |
| *j*-ustun yigʻindisi | — | *i* boʻyicha | `s += a[i][j]` |
| Bosh diagonal | *i*: 0 … *n* − 1 | kerak emas | `a[i][i]` |

Diagonal uchun ichma-ich sikl kerak emas: diagonalda har satrda faqat bitta element bor. Ichma-ich sikl bilan *n*² elementni tekshirib, `if i == j` qoʻyish ham ishlaydi, lekin *n* marta sekinroq.

## Ishlangan misol 1: satr va ustun yigʻindilari

Birinchi qatorda *n* va *m*, keyingi *n* qatorda *m* tadan butun son. Har bir satr yigʻindisini (har biri alohida qatorda), keyin barcha ustun yigʻindilarini bitta qatorda chiqaring.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

for i in range(n):
    satr = 0
    for j in range(m):
        satr += a[i][j]
    print(satr)

ustunlar = []
for j in range(m):
    ustun = 0
    for i in range(n):
        ustun += a[i][j]
    ustunlar.append(ustun)
print(*ustunlar)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                a[i][j] = in.nextInt();
            }
        }

        for (int i = 0; i < n; i++) {
            int satr = 0;
            for (int j = 0; j < m; j++) {
                satr += a[i][j];
            }
            System.out.println(satr);
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < m; j++) {
            int ustun = 0;
            for (int i = 0; i < n; i++) {
                ustun += a[i][j];
            }
            if (j > 0) sb.append(' ');
            sb.append(ustun);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> a(n, vector<int>(m));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            cin >> a[i][j];
        }
    }

    for (int i = 0; i < n; i++) {
        int satr = 0;
        for (int j = 0; j < m; j++) {
            satr += a[i][j];
        }
        cout << satr << endl;
    }

    for (int j = 0; j < m; j++) {
        int ustun = 0;
        for (int i = 0; i < n; i++) {
            ustun += a[i][j];
        }
        if (j > 0) cout << ' ';
        cout << ustun;
    }
    cout << endl;
    return 0;
}
```

Kirish:

```text
3 4
5 4 3 5
4 4 5 3
3 5 4 4
```

Buni 3 talabaning 4 fan boʻyicha baholari deb tasavvur qiling. Satr yigʻindilari — har bir talabaning jami bali: `17`, `16`, `16`. Ustun yigʻindilari — har bir fan boʻyicha jami: `12 13 12 12`.

Ustun yigʻindisida sikllar tartibi almashadi: **tashqi sikl ustun** (*j*), ichki — satr (*i*). Indekslar esa baribir `a[i][j]` — avval satr, keyin ustun.

## Ishlangan misol 2: diagonallar va transponirlash

Birinchi qatorda *n*, keyingi *n* qatorda *n* tadan son — kvadrat matritsa. Bosh va yordamchi diagonallar yigʻindisini, keyin transponirlangan matritsani chiqaring.

**Transponirlash** — satrlarni ustunlarga aylantirish: yangi matritsada `t[i][j] = a[j][i]`.

```python
n = int(input())
a = [list(map(int, input().split())) for _ in range(n)]

bosh = 0
yordamchi = 0
for i in range(n):
    bosh += a[i][i]
    yordamchi += a[i][n - 1 - i]
print("Bosh diagonal:", bosh)
print("Yordamchi diagonal:", yordamchi)

for i in range(n):
    satr = []
    for j in range(n):
        satr.append(a[j][i])
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] a = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = in.nextInt();
            }
        }

        int bosh = 0, yordamchi = 0;
        for (int i = 0; i < n; i++) {
            bosh += a[i][i];
            yordamchi += a[i][n - 1 - i];
        }
        System.out.println("Bosh diagonal: " + bosh);
        System.out.println("Yordamchi diagonal: " + yordamchi);

        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(a[j][i]);
            }
            System.out.println(sb);
        }
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<vector<int>> a(n, vector<int>(n));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            cin >> a[i][j];
        }
    }

    int bosh = 0, yordamchi = 0;
    for (int i = 0; i < n; i++) {
        bosh += a[i][i];
        yordamchi += a[i][n - 1 - i];
    }
    cout << "Bosh diagonal: " << bosh << endl;
    cout << "Yordamchi diagonal: " << yordamchi << endl;

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << a[j][i];
        }
        cout << endl;
    }
    return 0;
}
```

Kirish `3` va matritsa:

```text
1 2 3
4 5 6
7 8 9
```

1. Bosh diagonal: 1 + 5 + 9 = 15.
2. Yordamchi diagonal: `a[0][2]` + `a[1][1]` + `a[2][0]` = 3 + 5 + 7 = 15.
3. Transponirlangan matritsa (`a[j][i]` chiqariladi):

```text
1 4 7
2 5 8
3 6 9
```

Transponirlangan matritsa alohida saqlanmadi — uni chiqarish uchun indekslarni almashtirish kifoya. Agar matritsani joyida transponirlash kerak boʻlsa, faqat bosh diagonal ustidagi elementlar (*j* > *i*) `a[j][i]` bilan almashtiriladi; hammasi almashtirilsa, matritsa ikki marta aylanib, avvalgi holiga qaytadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Pythonʼda `[[0] * m] * n`.** Barcha satrlar bitta obyekt: bitta elementni oʻzgartirish butun ustunni oʻzgartiradi.
- **Indekslarni almashtirib yuborish.** `a[j][i]` va `a[i][j]` — turli elementlar; *n* ≠ *m* boʻlsa, chegaradan chiqish.
- **Satr va ustun sonini chalkashtirish.** Ustunlar soni — `len(a[0])` / `a[0].length`, `len(a)` emas.
- **Yordamchi diagonalda `a[i][n - i]`.** Toʻgʻrisi `a[i][n - 1 - i]`; aks holda *i* = 0 da chegaradan chiqiladi.
- **Satr yigʻindisini sikldan tashqarida nolga tenglash.** Har bir satr uchun yigʻuvchi tashqi sikl **ichida** nolga tenglanadi.
:::

:::tip
Matritsa masalasini yechishdan oldin 3 × 3 misolni qogʻozga indekslari bilan yozing (`a[0][0]` … `a[2][2]`). Kerakli elementlarni belgilab, ularning indekslari orasidagi bogʻlanishni toping (masalan, *i* + *j* = 2).
:::

## Xulosa

- Matritsa — massivlar massivi; `a[i][j]` da avval satr, keyin ustun.
- Python: `[[0] * m for _ in range(n)]`, Java: `new int[n][m]`, C++: `vector<vector<int>> a(n, vector<int>(m))`.
- Matritsa ichma-ich sikl bilan aylanib chiqiladi; satr yigʻindisida tashqi sikl satr, ustun yigʻindisida — ustun.
- Bosh diagonal: *i* = *j*; yordamchi: *i* + *j* = *n* − 1; diagonal uchun bitta sikl yetarli.
- Transponirlash: `t[i][j] = a[j][i]`.
- Pythonʼda `[[0] * m] * n` ishlatmang — satrlar bitta obyekt boʻlib qoladi.

## Oʻzingizni tekshiring

:::check
Q: 4 × 5 matritsada nechta element bor va oxirgi elementning indekslari qanday?
A: 20 ta; oxirgi element — `a[3][4]`.
Q: 5 × 5 matritsada `a[1][3]` qaysi diagonalda yotadi?
A: Yordamchi diagonalda: 1 + 3 = 4 = *n* − 1.
Q: Pythonʼda `a = [[0] * 3] * 2; a[0][1] = 7; print(a)` nima chiqaradi?
A: `[[0, 7, 0], [0, 7, 0]]`: ikkala satr ham bitta roʻyxatga havola.
Q: 2 × 3 matritsa transponirlansa, qanday oʻlchamli boʻladi?
A: 3 × 2.
Q: Bosh diagonal yigʻindisini topish uchun ichma-ich sikl kerakmi?
A: Yoʻq: har bir satrda diagonal elementi bitta (`a[i][i]`), bitta sikl yetarli.
:::

:::link https://docs.python.org/3/faq/programming.html#how-do-i-create-a-multidimensional-list
Python FAQ: koʻp oʻlchovli roʻyxat
`[[0] * m] * n` tuzogʻi va toʻgʻri usul haqida.
:::

:::link https://en.wikipedia.org/wiki/Transpose
Wikipedia: matritsani transponirlash
Transponirlash taʼrifi va xossalari.
:::
