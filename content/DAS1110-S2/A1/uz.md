---
title: Koʻp oʻlchamli massivlar. Statik massivlar. Elementlarni saralash usullari
summary: Ikki oʻlchamli massiv (matritsa) eʼlon qilish va oʻqish, satr va ustun boʻyicha aylanish, transponirlash, statik massiv chegaralari, matritsa satrlari va ustunlari ustida saralash.
---

## Nima uchun bu mavzu muhim?

Dars jadvali — kunlar va paralar, imtihon qaydnomasi — talabalar va fanlar, rasm — piksellar qatori va ustuni. Bunday maʼlumot tabiiy ravishda **jadval** koʻrinishida boʻladi va dasturda **ikki oʻlchamli massiv** (matritsa) bilan saqlanadi.

Birinchi semestrda bir oʻlchamli massivni oʻrgangan edik, oʻtgan maʼruzada esa saralash usullarini koʻrdik. Bu amaliy mashgʻulotda ikkalasini birlashtiramiz: matritsani oʻqiymiz, satr va ustunlar boʻyicha hisoblaymiz va satrlarni saralaymiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- ikki oʻlchamli massivni uch tilda eʼlon qilib, klaviaturadan oʻqiy olasiz;
- satr boʻyicha va ustun boʻyicha aylanib chiqib, yigʻindi, maksimum kabi qiymatlarni hisoblay olasiz;
- matritsani transponirlay olasiz;
- statik massiv bilan dinamik oʻlchamli massivning farqini tushuntira olasiz;
- matritsa satrlarini biror mezon boʻyicha saralay olasiz.
:::

## Ikki oʻlchamli massiv

Ikki oʻlchamli massiv — bu **massivlar massivi**: har bir element oʻzi ham massiv (satr). `a[i][j]` — *i*-satr, *j*-ustundagi element. Ikkala indeks ham 0 dan boshlanadi.

| Til | Eʼlon qilish (*n* × *m*, nollar bilan) | Element |
|---|---|---|
| Python | `a = [[0] * m for _ in range(n)]` | `a[i][j]` |
| Java | `int[][] a = new int[n][m];` | `a[i][j]` |
| C++ (statik) | `int a[100][100];` | `a[i][j]` |
| C++ (dinamik oʻlcham) | `vector<vector<int>> a(n, vector<int>(m));` | `a[i][j]` |

Pythonda `[[0] * m] * n` deb yozib boʻlmaydi: bu bitta satrning *n* ta havolasini yaratadi va bitta elementni oʻzgartirsangiz, butun ustun oʻzgaradi.

## Statik massiv

**Statik massiv** — oʻlchami kompilyatsiya vaqtida maʼlum boʻlgan massiv. C++'da `int a[100][100];` xotirada 100 · 100 ta `int` uchun joy ajratadi, garchi masalada faqat 3 × 4 matritsa kerak boʻlsa ham. Shuning uchun:

- oʻlcham masaladagi eng katta chegaradan kichik boʻlmasligi kerak (`n, m ≤ 100` boʻlsa — `[100][100]`);
- haqiqiy oʻlchamlar `n` va `m` alohida oʻzgaruvchilarda saqlanadi;
- katta statik massivni funksiya ichida emas, **global** eʼlon qilish maʼqul — funksiya steki kichik (odatda 1–8 MB).

Java va Pythonda massiv ishga tushganda yaratiladi (`new int[n][m]`), shuning uchun oʻlchamni kiritilgan `n` va `m` dan olish mumkin.

## Satr va ustun boʻyicha aylanish

Ichma-ich ikki sikl: tashqi sikl satrlar, ichki sikl ustunlar boʻyicha — **satr boʻyicha aylanish**. Agar tashqi sikl ustunlar, ichkisi satrlar boʻyicha boʻlsa — **ustun boʻyicha aylanish**.

```text
satr boʻyicha:   i = 0..n-1 uchun: j = 0..m-1 uchun: a[i][j]
ustun boʻyicha:  j = 0..m-1 uchun: i = 0..n-1 uchun: a[i][j]
```

Satr yigʻindisi uchun yigʻindi har bir satr boshida nolga tushiriladi; ustun yigʻindisi uchun esa har bir ustun boshida. **Transponirlash** — satrlarni ustunlarga aylantirish: `t[j][i] = a[i][j]`, natija *m* × *n* oʻlchamli.

## Matritsa ustida saralash

Matritsa bilan bogʻliq saralash masalalari ikki xil boʻladi:

| Masala | Nima saralanadi | Qanday |
|---|---|---|
| Har bir satrni saralash | satr ichidagi elementlar | har bir `a[i]` ni alohida massiv sifatida saralash |
| Satrlarni saralash | satrlar butunligicha | kalit (masalan, satr yigʻindisi) boʻyicha satrlarning oʻrnini almashtirish |
| Ustunni saralash | ustun elementlari | ustunni alohida massivga koʻchirish, saralash, qaytarib yozish |

Satrlar butunligicha saralanganda satr ichidagi tartib oʻzgarmaydi — faqat satrlarning oʻrni almashadi.

## Ishlangan misol 1: satr yigʻindisi va ustun maksimumi

Birinchi satrda *n* va *m*, soʻng *n* ta satrda *m* tadan butun son. Har bir satrning yigʻindisini, keyin har bir ustunning eng katta elementini chiqaring.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

for i in range(n):
    print(sum(a[i]))

ustun_max = []
for j in range(m):
    eng_katta = a[0][j]
    for i in range(1, n):
        if a[i][j] > eng_katta:
            eng_katta = a[i][j]
    ustun_max.append(eng_katta)
print(*ustun_max)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        for (int i = 0; i < n; i++) {
            long yigindi = 0;
            for (int j = 0; j < m; j++) yigindi += a[i][j];
            System.out.println(yigindi);
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < m; j++) {
            int engKatta = a[0][j];
            for (int i = 1; i < n; i++) {
                if (a[i][j] > engKatta) engKatta = a[i][j];
            }
            if (j > 0) sb.append(' ');
            sb.append(engKatta);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int a[100][100];   // statik massiv: eng katta oʻlcham boʻyicha

int main() {
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            cin >> a[i][j];

    for (int i = 0; i < n; i++) {
        long long yigindi = 0;
        for (int j = 0; j < m; j++) yigindi += a[i][j];
        cout << yigindi << endl;
    }

    for (int j = 0; j < m; j++) {
        int engKatta = a[0][j];
        for (int i = 1; i < n; i++) {
            if (a[i][j] > engKatta) engKatta = a[i][j];
        }
        if (j > 0) cout << ' ';
        cout << engKatta;
    }
    cout << endl;
    return 0;
}
```

Kirish:

```text
3 4
5 1 8 2
7 3 0 4
1 9 6 2
```

Chiqish:

```text
16
14
18
7 9 8 4
```

Ustun maksimumida tashqi sikl `j` boʻyicha — bu ustun boʻyicha aylanish. Boshlangʻich qiymat `a[0][j]` olinadi, `0` emas: aks holda manfiy sonlardan iborat ustunda xato javob chiqadi.

## Ishlangan misol 2: transponirlash

Birinchi satrda *n* va *m*, soʻng *n* × *m* matritsa. Transponirlangan matritsani (*m* × *n*) chiqaring.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

t = [[0] * n for _ in range(m)]
for i in range(n):
    for j in range(m):
        t[j][i] = a[i][j]

for satr in t:
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        int[][] t = new int[m][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                t[j][i] = a[i][j];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(t[i][j]);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int a[100][100], t[100][100];

int main() {
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            cin >> a[i][j];

    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            t[j][i] = a[i][j];

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << t[i][j];
        }
        cout << endl;
    }
    return 0;
}
```

`2 3` va `1 2 3` / `4 5 6` kiritilsa, chiqish: `1 4`, `2 5`, `3 6`. Natija massivi `t` ning oʻlchami *m* × *n* — satr va ustunlar soni almashgan.

## Ishlangan misol 3: satrlarni yigʻindisi boʻyicha saralash

Birinchi satrda *n* va *m*, soʻng *n* × *m* matritsa. Satrlarni yigʻindisi boʻyicha oʻsish tartibida joylashtiring; yigʻindisi teng satrlar kiritilgan tartibda qoladi. Satr ichidagi elementlar oʻzgarmaydi.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

a.sort(key=sum)          # barqaror: teng yigʻindilar oʻz tartibida

for satr in a:
    print(*satr)
```
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    static long yigindi(int[] satr) {
        long s = 0;
        for (int x : satr) s += x;
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        Arrays.sort(a, Comparator.comparingLong(Main::yigindi));   // obyektlar uchun barqaror

        StringBuilder sb = new StringBuilder();
        for (int[] satr : a) {
            for (int j = 0; j < m; j++) {
                if (j > 0) sb.append(' ');
                sb.append(satr[j]);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <numeric>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> a(n, vector<int>(m));
    for (auto& satr : a)
        for (auto& x : satr) cin >> x;

    stable_sort(a.begin(), a.end(), [](const vector<int>& x, const vector<int>& y) {
        return accumulate(x.begin(), x.end(), 0LL) < accumulate(y.begin(), y.end(), 0LL);
    });

    for (const auto& satr : a) {
        for (int j = 0; j < m; j++) {
            if (j > 0) cout << ' ';
            cout << satr[j];
        }
        cout << endl;
    }
    return 0;
}
```

Kirish:

```text
4 3
5 5 5
1 2 3
9 0 0
3 2 1
```

Chiqish:

```text
1 2 3
3 2 1
9 0 0
5 5 5
```

Yigʻindilar: 15, 6, 9, 6. `1 2 3` va `3 2 1` ning yigʻindisi teng (6), shuning uchun ular kiritilgan tartibda qoladi — bu **barqaror** saralash. C++'da shu sababli `sort` emas, `stable_sort` ishlatildi; satrlar `vector` boʻlgani uchun ularni butunligicha almashtirish mumkin (statik `int a[100][100]` satrlarini `sort` bilan almashtirib boʻlmaydi).

:::warning
**Koʻp uchraydigan xatolar**

- **Pythonda `[[0] * m] * n`.** Barcha satrlar bitta roʻyxat boʻlib qoladi; `[[0] * m for _ in range(n)]` yozing.
- **Indekslarni almashtirib yuborish.** `a[j][i]` *n* ≠ *m* boʻlganda massiv chegarasidan chiqadi; har doim `a[satr][ustun]`.
- **Katta statik massivni `main` ichida eʼlon qilish.** `int a[1000][1000]` stekni toʻldirib, dastur ishga tushishi bilan yiqiladi; uni global eʼlon qiling.
- **Maksimumni `0` dan boshlash.** Manfiy sonlar uchun notoʻgʻri; birinchi elementdan boshlang.
:::

:::tip
Matritsa bilan ishlaydigan dasturni kichik, lekin toʻrtburchak (masalan, 2 × 3) misolda tekshiring: kvadrat matritsada `i` va `j` almashib ketgan xato koʻrinmay qolishi mumkin.
:::

## Xulosa

- Ikki oʻlchamli massiv — massivlar massivi; `a[i][j]` — *i*-satr, *j*-ustun.
- Statik massiv eng katta oʻlcham boʻyicha eʼlon qilinadi, haqiqiy oʻlcham `n`, `m` da saqlanadi.
- Tashqi sikl satrlar boʻyicha — satr boʻyicha aylanish; ustunlar boʻyicha — ustun boʻyicha aylanish.
- Transponirlash: `t[j][i] = a[i][j]`, oʻlcham *m* × *n*.
- Har bir satr alohida massiv sifatida saralanishi yoki satrlar butunligicha kalit boʻyicha saralanishi mumkin.

## Oʻzingizni tekshiring

:::check
Q: 3 × 5 matritsada `a[2][4]` qaysi element?
A: Uchinchi satr, beshinchi ustundagi element — oxirgi satrning oxirgi elementi.
Q: Nima uchun Pythonda `[[0] * 3] * 2` xavfli?
A: Ikkala satr bitta roʻyxatga havola boʻladi: `a[0][0] = 1` qilinsa, `a[1][0]` ham 1 boʻladi.
Q: 2 × 5 matritsani transponirlasak, natija oʻlchami qanday?
A: 5 × 2.
Q: Har bir satrni oʻsish tartibida saralash uchun Pythonda nima yoziladi?
A: `for satr in a: satr.sort()` — har bir satr alohida roʻyxat sifatida saralanadi.
Q: Satrlarni yigʻindi boʻyicha saralaganda satr ichidagi tartib oʻzgaradimi?
A: Yoʻq, satrlar butunligicha oʻrin almashadi.
:::

:::link https://docs.python.org/3/tutorial/datastructures.html#nested-list-comprehensions
Python: ichma-ich roʻyxatlar
Matritsani roʻyxatlar roʻyxati sifatida yaratish va transponirlash (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/language/array
C++: massivlar
Statik va koʻp oʻlchamli massivlar haqida maʼlumotnoma (ingliz tilida).
:::
