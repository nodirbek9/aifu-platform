---
title: Dinamik xotira bilan ishlash. Koʻrsatkichlar. Dinamik bir va koʻp oʻlchamli massivlar
summary: Koʻrsatkich arifmetikasi, dinamik bir oʻlchamli massiv, new/delete bilan dinamik matritsa, har xil uzunlikdagi satrli („tishli“) massiv, matritsani bir oʻlchamli xotirada saqlash; Java va Pythonʼdagi mosliklari.
---

## Nima uchun bu mavzu muhim?

Birinchi semestrda stek va uyum, koʻrsatkich, `new` va `delete` tushunchalari bilan tanishdik. Statik massivning kamchiligi — oʻlchami kompilyatsiya vaqtida belgilanadi: `int a[100][100]` doim 40 KB joy oladi, 1000 × 1000 matritsa uchun esa yetmaydi. Dinamik xotira bu muammoni hal qiladi — massiv **dastur ishlayotganda** kerakli oʻlchamda yaratiladi.

Bu amaliy mashgʻulotda dinamik massivlarning uch xil koʻrinishini mashq qilamiz: toʻrtburchak matritsa, satrlari har xil uzunlikdagi massiv va matritsani bitta uzluksiz xotira boʻlagida saqlash.

:::note
**Bu mashgʻulotdan soʻng siz:**

- koʻrsatkich arifmetikasini (`p + i`, `*(p + i)`, `p++`) tushuntira olasiz;
- C++'da dinamik matritsani `new` bilan yaratib, `delete[]` bilan toʻgʻri boʻshata olasiz;
- satrlari har xil uzunlikdagi („tishli“) massiv yarata olasiz;
- matritsani bir oʻlchamli massivda `i * m + j` indeksi bilan saqlay olasiz;
- Java va Pythonʼda xuddi shu tuzilmalarni qura olasiz.
:::

## Koʻrsatkich arifmetikasi

C++'da massiv nomi uning birinchi elementiga koʻrsatkichdek ishlaydi. Koʻrsatkichga butun son qoʻshilsa, u **shuncha element** oldinga suriladi (bayt emas):

| Ifoda | Maʼnosi |
|---|---|
| `p` | birinchi element manzili |
| `p + i` | *i*-element manzili |
| `*(p + i)` | *i*-element qiymati — `p[i]` bilan bir xil |
| `p++` | keyingi elementga oʻtish |
| `q - p` | ikki koʻrsatkich orasidagi elementlar soni |

`p + i` da kompilyator `i * sizeof(int)` baytni avtomatik hisoblaydi. Java va Pythonʼda koʻrsatkich arifmetikasi yoʻq — u yerda faqat indeks ishlatiladi va chegaradan chiqish darhol xatoga olib keladi.

## Dinamik matritsa

C++'da *n* × *m* dinamik matritsa ikki bosqichda yaratiladi: avval *n* ta **satr koʻrsatkichi** massivi, keyin har bir satr uchun *m* ta element:

```text
int** a = new int*[n];             // satr koʻrsatkichlari
har bir i uchun: a[i] = new int[m];  // har bir satr
...
har bir i uchun: delete[] a[i];      // boʻshatish — teskari tartibda
delete[] a;
```

Har bir `new[]` ga bitta `delete[]` mos kelishi shart, aks holda **xotira sizib chiqadi**. Shuning uchun amalda koʻpincha `vector<vector<int>>` ishlatiladi — u xotirani oʻzi boʻshatadi. Lekin `new`/`delete` ni tushunish muhim: `vector` ning ichida aynan shu jarayon yashiringan.

| Til | Dinamik *n* × *m* matritsa | Boʻshatish |
|---|---|---|
| C++ | `new int*[n]` + har bir satrga `new int[m]` | `delete[]` — har bir satr va koʻrsatkichlar massivi |
| C++ (tavsiya) | `vector<vector<int>> a(n, vector<int>(m))` | avtomatik |
| Java | `int[][] a = new int[n][m]` | axlat yigʻuvchi |
| Python | `[[0] * m for _ in range(n)]` | axlat yigʻuvchi |

## Tishli massiv

Satrlar alohida yaratilgani uchun ularning uzunligi **har xil** boʻlishi mumkin — bunday massiv „tishli“ (jagged) deyiladi. Masalan, Paskal uchburchagida *i*-satrda *i* + 1 ta element bor; toʻrtburchak matritsa ishlatilsa, deyarli yarmi bekor turadi.

| Til | Tishli massiv |
|---|---|
| C++ | `a[i] = new int[i + 1];` yoki `vector<vector<int>>` |
| Java | `int[][] a = new int[n][];` va `a[i] = new int[i + 1];` |
| Python | roʻyxatlar roʻyxati — har bir ichki roʻyxat istalgan uzunlikda |

## Matritsani bir oʻlchamli massivda saqlash

Matritsani bitta uzluksiz massivda ham saqlash mumkin: *i*-satr, *j*-ustundagi element `i * m + j` oʻrinda turadi (satrlar ketma-ket joylashadi). Afzalliklari:

- bitta `new` va bitta `delete[]` — xato qilish ehtimoli kam;
- elementlar xotirada yonma-yon — protsessor keshi tufayli tezroq;
- butun matritsani bitta funksiyaga oddiy massivdek uzatish mumkin.

Grafika, tasvirlarni qayta ishlash va ilmiy hisoblash kutubxonalari aynan shu usuldan foydalanadi.

## Ishlangan misol 1: spiral matritsa

*n* va *m* kiritiladi. *n* × *m* dinamik matritsani 1 dan *n* · *m* gacha sonlar bilan soat strelkasi boʻyicha spiral tartibda toʻldiring va chiqaring (har bir son 3 belgi kenglikda).

```python
n, m = map(int, input().split())
a = [[0] * m for _ in range(n)]

yuqori, past, chap, ong = 0, n - 1, 0, m - 1
k = 1
while yuqori <= past and chap <= ong:
    for j in range(chap, ong + 1):              # yuqori satr: chapdan oʻngga
        a[yuqori][j] = k; k += 1
    yuqori += 1
    for i in range(yuqori, past + 1):           # oʻng ustun: yuqoridan pastga
        a[i][ong] = k; k += 1
    ong -= 1
    if yuqori <= past:
        for j in range(ong, chap - 1, -1):      # pastki satr: oʻngdan chapga
            a[past][j] = k; k += 1
        past -= 1
    if chap <= ong:
        for i in range(past, yuqori - 1, -1):   # chap ustun: pastdan yuqoriga
            a[i][chap] = k; k += 1
        chap += 1

for satr in a:
    print("".join(f"{x:3d}" for x in satr))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];

        int yuqori = 0, past = n - 1, chap = 0, ong = m - 1, k = 1;
        while (yuqori <= past && chap <= ong) {
            for (int j = chap; j <= ong; j++) a[yuqori][j] = k++;      // yuqori satr: chapdan oʻngga
            yuqori++;
            for (int i = yuqori; i <= past; i++) a[i][ong] = k++;      // oʻng ustun: yuqoridan pastga
            ong--;
            if (yuqori <= past) {
                for (int j = ong; j >= chap; j--) a[past][j] = k++;    // pastki satr: oʻngdan chapga
                past--;
            }
            if (chap <= ong) {
                for (int i = past; i >= yuqori; i--) a[i][chap] = k++; // chap ustun: pastdan yuqoriga
                chap++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int[] satr : a) {
            for (int x : satr) sb.append(String.format("%3d", x));
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    int** a = new int*[n];                      // satr koʻrsatkichlari
    for (int i = 0; i < n; i++) a[i] = new int[m];

    int yuqori = 0, past = n - 1, chap = 0, ong = m - 1, k = 1;
    while (yuqori <= past && chap <= ong) {
        for (int j = chap; j <= ong; j++) a[yuqori][j] = k++;      // yuqori satr: chapdan oʻngga
        yuqori++;
        for (int i = yuqori; i <= past; i++) a[i][ong] = k++;      // oʻng ustun: yuqoridan pastga
        ong--;
        if (yuqori <= past) {
            for (int j = ong; j >= chap; j--) a[past][j] = k++;    // pastki satr: oʻngdan chapga
            past--;
        }
        if (chap <= ong) {
            for (int i = past; i >= yuqori; i--) a[i][chap] = k++; // chap ustun: pastdan yuqoriga
            chap++;
        }
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) printf("%3d", a[i][j]);
        printf("\n");
    }

    for (int i = 0; i < n; i++) delete[] a[i];  // har bir satrni boʻshatamiz
    delete[] a;                                 // keyin koʻrsatkichlar massivini
    return 0;
}
```

`3 4` kiritilsa, chiqish:

```text
  1  2  3  4
 10 11 12  5
  9  8  7  6
```

Toʻrtta chegara (`yuqori`, `past`, `chap`, `ong`) har bir aylanishdan keyin ichkariga suriladi. Ichki ikki `if` toʻrtburchak (kvadrat emas) matritsada bitta satr yoki ustun qolganda uni ikki marta toʻldirmaslik uchun kerak. C++ versiyasining oxirida xotira **teskari tartibda** boʻshatiladi: avval satrlar, keyin koʻrsatkichlar massivi — aks holda satrlar manzili yoʻqoladi.

## Ishlangan misol 2: Paskal uchburchagi (tishli massiv)

*n* kiritiladi (1 ≤ *n* ≤ 30). Paskal uchburchagining dastlabki *n* satrini chiqaring: har bir satr chetlarida 1, ichki element esa yuqoridagi ikki qoʻshni element yigʻindisi.

```python
n = int(input())
uchburchak = []
for i in range(n):
    satr = [1] * (i + 1)                       # i-satrda i+1 ta element
    for j in range(1, i):
        satr[j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j]
    uchburchak.append(satr)

for satr in uchburchak:
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        long[][] uchburchak = new long[n][];            // satrlar hali yaratilmagan
        for (int i = 0; i < n; i++) {
            uchburchak[i] = new long[i + 1];            // i-satrda i+1 ta element
            uchburchak[i][0] = uchburchak[i][i] = 1;
            for (int j = 1; j < i; j++) {
                uchburchak[i][j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j];
            }
        }

        StringBuilder sb = new StringBuilder();
        for (long[] satr : uchburchak) {
            for (int j = 0; j < satr.length; j++) {
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
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    long long** uchburchak = new long long*[n];
    for (int i = 0; i < n; i++) {
        uchburchak[i] = new long long[i + 1];       // i-satrda i+1 ta element
        uchburchak[i][0] = uchburchak[i][i] = 1;
        for (int j = 1; j < i; j++) {
            uchburchak[i][j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j];
        }
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j <= i; j++) {
            if (j > 0) cout << ' ';
            cout << uchburchak[i][j];
        }
        cout << endl;
    }

    for (int i = 0; i < n; i++) delete[] uchburchak[i];
    delete[] uchburchak;
    return 0;
}
```

`5` kiritilsa, chiqish:

```text
1
1 1
1 2 1
1 3 3 1
1 4 6 4 1
```

Har bir satr aynan kerakli uzunlikda yaratildi: *n* = 30 uchun 465 ta element, toʻrtburchak 30 × 30 matritsa esa 900 ta joy olardi. Javaʼda `new long[n][]` — ikkinchi oʻlcham koʻrsatilmagan, satrlar keyin alohida yaratiladi. Satr chetidagi `[i][0] = [i][i] = 1` birinchi satrda (*i* = 0) bitta elementga ikki marta yozadi — bu xato emas. Qiymatlar tez oʻsadi (30-satrda 77 558 760 gacha, 35-satrda esa `int` chegarasidan oshadi), shuning uchun `long` tanlangan.

## Ishlangan misol 3: matritsani bir oʻlchamli massivda burish

Birinchi satrda *n* va *m*, soʻng *n* × *m* matritsa. Matritsani bitta bir oʻlchamli massivda saqlang va uni soat strelkasi boʻyicha 90° burib, natijani (*m* × *n*) ham bir oʻlchamli massivda hosil qilib chiqaring.

```python
n, m = map(int, input().split())
a = []
for _ in range(n):
    a.extend(map(int, input().split()))         # satrlar ketma-ket: a[i*m + j]

b = [0] * (n * m)                               # natija: m satr, n ustun
for i in range(n):
    for j in range(m):
        b[j * n + (n - 1 - i)] = a[i * m + j]

for i in range(m):
    print(*b[i * n:(i + 1) * n])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[] a = new int[n * m];                       // satrlar ketma-ket: a[i*m + j]
        for (int k = 0; k < n * m; k++) a[k] = in.nextInt();

        int[] b = new int[n * m];                       // natija: m satr, n ustun
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                b[j * n + (n - 1 - i)] = a[i * m + j];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(b[i * n + j]);
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

int main() {
    int n, m;
    cin >> n >> m;
    int* a = new int[n * m];                    // bitta uzluksiz boʻlak
    for (int* p = a; p < a + n * m; p++) cin >> *p;   // koʻrsatkich bilan oʻqish

    int* b = new int[n * m];                    // natija: m satr, n ustun
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            *(b + j * n + (n - 1 - i)) = *(a + i * m + j);

    for (int i = 0; i < m; i++) {
        const int* satr = b + i * n;            // i-satr boshi
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << satr[j];
        }
        cout << endl;
    }

    delete[] a;                                 // bitta new — bitta delete
    delete[] b;
    return 0;
}
```

Kirish:

```text
2 3
1 2 3
4 5 6
```

Chiqish:

```text
4 1
5 2
6 3
```

Burishda *i*-satr natijaning (*n* − 1 − *i*)-ustuniga aylanadi: birinchi satr `1 2 3` oxirgi ustunga tushdi. C++ versiyasida koʻrsatkich arifmetikasi toʻliq koʻrinadi: `for (int* p = a; p < a + n * m; p++)` massivni indekssiz aylanadi, `b + i * n` esa *i*-satrning boshini beradi — u oddiy massivdek `satr[j]` bilan ishlatiladi. Har bir matritsa uchun bittadan `new` va `delete[]` yetarli.

:::warning
**Koʻp uchraydigan xatolar**

- **`delete` va `delete[]` ni aralashtirish.** `new[]` bilan yaratilgan massiv faqat `delete[]` bilan boʻshatiladi.
- **Avval koʻrsatkichlar massivini oʻchirish.** `delete[] a;` dan keyin `a[i]` ga murojaat — aniqlanmagan xatti-harakat; satrlarni birinchi boʻshating.
- **Bir oʻlchamli saqlashda `i * n + j` yozish.** Satr uzunligi — ustunlar soni *m*; `i * m + j` boʻlishi kerak (*n* = *m* boʻlmasa, xato darhol koʻrinmaydi).
- **Koʻrsatkich bilan massiv chegarasidan chiqish.** `p <= a + n` — bir element ortiq; shart `p < a + n`.
:::

:::tip
C++'da `new`/`delete` bilan ishlaganda har bir `new` yozilishi bilan darhol unga mos `delete` ni ham yozib qoʻying, keyin oraliqdagi kodni toʻldiring. Amaliy loyihalarda esa `vector` va `unique_ptr` ishlating — ular xotirani oʻzi boʻshatadi.
:::

## Xulosa

- Koʻrsatkichga *i* qoʻshish uni *i* ta element oldinga suradi; `*(p + i)` — `p[i]` ning oʻzi.
- C++'da dinamik matritsa: koʻrsatkichlar massivi + har bir satr; boʻshatish teskari tartibda.
- Satrlar alohida yaratilsa, ular har xil uzunlikda boʻlishi mumkin — tishli massiv xotirani tejaydi.
- Matritsani bir oʻlchamli massivda `i * m + j` oʻrinda saqlash — bitta ajratish, tezroq murojaat.
- Java va Pythonʼda xotira avtomatik boʻshatiladi; C++'da amalda `vector` afzal.

## Oʻzingizni tekshiring

:::check
Q: `int* p = a;` boʻlsa, `*(p + 3)` nimaga teng?
A: `a[3]` ga — massivning toʻrtinchi elementiga.
Q: C++'da `new int*[n]` va har bir satr uchun `new int[m]` bilan yaratilgan matritsa qanday boʻshatiladi?
A: Avval har bir `a[i]` uchun `delete[] a[i]`, soʻng `delete[] a`.
Q: Javaʼda `new int[5][]` nima yaratadi?
A: 5 ta satr havolasi (`null`) bor massivni; har bir satr keyin `new int[...]` bilan alohida yaratiladi.
Q: 4 × 6 matritsa bir oʻlchamli massivda saqlansa, `[2][3]` element qaysi indeksda turadi?
A: 2 · 6 + 3 = 15-indeksda.
Q: Tishli massivning afzalligi nimada?
A: Har bir satr aynan kerakli uzunlikda boʻladi va xotira bekor sarflanmaydi.
:::

:::link https://en.cppreference.com/w/cpp/language/new
C++: new ifodasi
Dinamik xotira ajratish va massivlar uchun `new[]` haqida (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/language/operator_arithmetic#Additive_operators
C++: koʻrsatkich arifmetikasi
Koʻrsatkichga son qoʻshish va koʻrsatkichlar ayirmasi qoidalari (ingliz tilida).
:::
