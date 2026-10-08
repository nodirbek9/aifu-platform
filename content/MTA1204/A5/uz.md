---
title: Pufaksimon va Xoar saralash. Parallel saralash algoritmlari (amaliy mashgʻulot)
summary: Pufaksimon saralashning oʻtishlar soni va „toshbaqa“ muammosi, kokteyl (shaker) saralash; Xoar ajratishi asosida k-chi eng kichik elementni topish (quickselect); saralash tarmogʻini 0-1 prinsipi bilan tekshirish.
---

## Nima uchun bu mavzu muhim?

Maʼruzada pufaksimon va tez saralashni, shuningdek saralash tarmoqlarini koʻrdik. Amaliyotda bu algoritmlarning gʻoyalari kutilmagan joylarda ishlatiladi: pufaksimon saralashni ikki tomonga yurgizish uni sezilarli tezlashtiradi, Xoar ajratishi esa butun massivni saralamasdan medianani yoki eng yaxshi *k* talabani topadi. Saralash tarmogʻi toʻgʻriligini tekshirish uchun esa barcha mumkin boʻlgan kirishlarni emas, faqat 0 va 1 lardan iborat kirishlarni sinash kifoya.

:::note
**Bu mashgʻulotdan soʻng siz:**

- pufaksimon saralashning oʻtishlar sonini oʻlchab, „toshbaqa“ muammosini tushuntira olasiz;
- kokteyl saralashni yozib, uning afzalligini koʻrsata olasiz;
- Xoar ajratishi bilan *k*-chi eng kichik elementni oʻrtacha *O*(*n*) da topa olasiz;
- 0-1 prinsipi yordamida saralash tarmogʻini tekshira olasiz.
:::

## Quyonlar va toshbaqalar

Pufaksimon saralashda katta element bitta oʻtishda oxirigacha „uchib“ boradi — bu **quyon**. Kichik element esa har oʻtishda faqat bitta oʻringa chapga suriladi — bu **toshbaqa**. Massiv oxiridagi bitta kichik element (`2 3 4 5 6 7 1`) pufaksimon saralashni deyarli *n* ta oʻtishga majbur qiladi.

**Kokteyl saralash** (shaker sort) oʻtishlarni navbatma-navbat chapdan oʻngga va oʻngdan chapga bajaradi: teskari oʻtishda toshbaqalar ham bitta oʻtishda boshiga yetadi. Eng yomon holatda murakkablik baribir *O*(*n*²), lekin „deyarli saralangan“ massivlarda u pufaksimondan ancha tez.

## Tanlash masalasi va quickselect

**Tanlash masalasi**: massivdagi *k*-chi eng kichik elementni topish (*k* = *n*/2 — mediana). Toʻliq saralash *O*(*n* log *n*). Xoarning **quickselect** algoritmi esa ajratishdan keyin faqat **bitta** qismga — *k*-chi oʻrin tushgan qismga — davom etadi:

| | Tez saralash | Quickselect |
|---|---|---|
| Ajratishdan keyin | ikkala qism rekursiv saralanadi | faqat bitta qism koʻriladi |
| Oʻrtacha vaqt | *O*(*n* log *n*) | *O*(*n*) (*n* + *n*/2 + *n*/4 + … ≈ 2*n*) |
| Eng yomon | *O*(*n*²) | *O*(*n*²) |

C++ `nth_element`, Pythonʼda `heapq.nsmallest` va statistikada medianani tez hisoblash shu gʻoyaga asoslangan.

## 0-1 prinsipi

Saralash tarmogʻi toʻgʻriligini isbotlash uchun **0-1 prinsipi** (Knut): agar tarmoq 0 va 1 lardan iborat **barcha** 2ⁿ ta kirishni toʻgʻri saralasa, u istalgan sonlar ketma-ketligini ham toʻgʻri saralaydi. *n* = 16 uchun 65 536 ta tekshiruv — barcha 16! ≈ 2 · 10¹³ ta tartibni sinashdan beqiyos oson.

## Ishlangan misol 1: pufaksimon va kokteyl saralash oʻtishlari

Birinchi satrda *n*, ikkinchisida *n* ta son. Yaxshilangan pufaksimon saralash (almashtirishsiz oʻtishda toʻxtaydi) va kokteyl saralash bajargan oʻtishlar sonini chiqaring.

```python
def pufaksimon(asl):
    a = asl[:]
    otish = 0
    while True:
        otish += 1
        almashdi = False
        for i in range(len(a) - otish):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        if not almashdi:                # almashtirishsiz oʻtish — saralangan
            return otish


def kokteyl(asl):
    a = asl[:]
    otish = 0
    chap, ong = 0, len(a) - 1
    while chap < ong:
        otish += 1                      # chapdan oʻngga: quyonlar
        almashdi = False
        for i in range(chap, ong):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        ong -= 1
        if not almashdi:
            break
        otish += 1                      # oʻngdan chapga: toshbaqalar
        almashdi = False
        for i in range(ong, chap, -1):
            if a[i - 1] > a[i]:
                a[i - 1], a[i] = a[i], a[i - 1]
                almashdi = True
        chap += 1
        if not almashdi:
            break
    return otish


n = int(input())
a = list(map(int, input().split()))
print(pufaksimon(a), kokteyl(a))
```
```java
import java.util.Scanner;

public class Main {
    static int pufaksimon(int[] asl) {
        int[] a = asl.clone();
        int otish = 0;
        while (true) {
            otish++;
            boolean almashdi = false;
            for (int i = 0; i < a.length - otish; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            if (!almashdi) return otish;            // almashtirishsiz oʻtish — saralangan
        }
    }

    static int kokteyl(int[] asl) {
        int[] a = asl.clone();
        int otish = 0, chap = 0, ong = a.length - 1;
        while (chap < ong) {
            otish++;                                // chapdan oʻngga: quyonlar
            boolean almashdi = false;
            for (int i = chap; i < ong; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            ong--;
            if (!almashdi) break;
            otish++;                                // oʻngdan chapga: toshbaqalar
            almashdi = false;
            for (int i = ong; i > chap; i--) {
                if (a[i - 1] > a[i]) {
                    int t = a[i - 1]; a[i - 1] = a[i]; a[i] = t;
                    almashdi = true;
                }
            }
            chap++;
            if (!almashdi) break;
        }
        return otish;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        System.out.println(pufaksimon(a) + " " + kokteyl(a));
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int pufaksimon(vector<int> a) {                     // a — nusxa
    int otish = 0;
    while (true) {
        otish++;
        bool almashdi = false;
        for (int i = 0; i < (int) a.size() - otish; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        if (!almashdi) return otish;                // almashtirishsiz oʻtish — saralangan
    }
}

int kokteyl(vector<int> a) {
    int otish = 0, chap = 0, ong = (int) a.size() - 1;
    while (chap < ong) {
        otish++;                                    // chapdan oʻngga: quyonlar
        bool almashdi = false;
        for (int i = chap; i < ong; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        ong--;
        if (!almashdi) break;
        otish++;                                    // oʻngdan chapga: toshbaqalar
        almashdi = false;
        for (int i = ong; i > chap; i--) {
            if (a[i - 1] > a[i]) {
                swap(a[i - 1], a[i]);
                almashdi = true;
            }
        }
        chap++;
        if (!almashdi) break;
    }
    return otish;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    cout << pufaksimon(a) << " " << kokteyl(a) << endl;
    return 0;
}
```

| Massiv | Pufaksimon | Kokteyl |
|---|---|---|
| `2 3 4 5 6 7 1` | 7 | 3 |
| `5 4 3 2 1` | 5 | 4 |
| `1 2 3 4` | 1 | 1 |

Birinchi qatorda `1` — toshbaqa: pufaksimon saralash uni har oʻtishda bitta oʻringa siljitadi va 7 oʻtish talab qiladi, kokteyl saralashning birinchi teskari oʻtishi esa uni darhol boshiga olib keladi. Saralangan massiv uchun ikkala algoritm ham bitta tekshiruv oʻtishida toʻxtaydi.

## Ishlangan misol 2: quickselect — k-chi eng kichik element

Birinchi satrda *n* va *k*, ikkinchisida *n* ta son. Massivni toʻliq saralamasdan, Xoar ajratishi bilan *k*-chi eng kichik elementni toping (*k* = 1 — eng kichigi).

```python
n, k = map(int, input().split())
a = list(map(int, input().split()))
k -= 1                                  # 0 dan sanaladigan oʻrin
chap, ong = 0, n - 1
while chap < ong:
    p = a[(chap + ong) // 2]            # Xoar ajratishi
    i, j = chap - 1, ong + 1
    while True:
        i += 1
        while a[i] < p:
            i += 1
        j -= 1
        while a[j] > p:
            j -= 1
        if i >= j:
            break
        a[i], a[j] = a[j], a[i]
    if k <= j:                          # faqat k tushgan qismga davom etamiz
        ong = j
    else:
        chap = j + 1
print(a[chap])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt() - 1;     // 0 dan sanaladigan oʻrin
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int chap = 0, ong = n - 1;
        while (chap < ong) {
            int p = a[(chap + ong) / 2];                // Xoar ajratishi
            int i = chap - 1, j = ong + 1;
            while (true) {
                do i++; while (a[i] < p);
                do j--; while (a[j] > p);
                if (i >= j) break;
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
            if (k <= j) ong = j;                        // faqat k tushgan qismga davom etamiz
            else chap = j + 1;
        }
        System.out.println(a[chap]);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    k--;                                                // 0 dan sanaladigan oʻrin
    vector<int> a(n);
    for (int& v : a) cin >> v;

    int chap = 0, ong = n - 1;
    while (chap < ong) {
        int p = a[(chap + ong) / 2];                    // Xoar ajratishi
        int i = chap - 1, j = ong + 1;
        while (true) {
            do i++; while (a[i] < p);
            do j--; while (a[j] > p);
            if (i >= j) break;
            swap(a[i], a[j]);
        }
        if (k <= j) ong = j;                            // faqat k tushgan qismga davom etamiz
        else chap = j + 1;
    }
    cout << a[chap] << endl;
    return 0;
}
```

`6 3` va `7 10 4 3 20 15` uchun chiqish `7` (saralanganda: 3 4 **7** 10 15 20); `6 1` uchun — `3`. Rekursiya yoʻq — sikl ishlatildi, chunki har safar faqat bitta qism qoladi. Xoar ajratishidan keyin chap qismdagi barcha elementlar oʻng qismdagilardan katta emas, shuning uchun *k*-chi element aynan *k* tushgan qismda boʻladi.

## Ishlangan misol 3: saralash tarmogʻini 0-1 prinsipi bilan tekshirish

Birinchi satrda *n* (1 ≤ *n* ≤ 16) va taqqoslagichlar soni *m*, keyingi *m* satrda taqqoslagichlar `i j` (0 dan, *i* < *j*). Barcha 2ⁿ ta 0-1 kirishni (leksikografik tartibda) sinab koʻring: tarmoq hammasini saralasa `ha`, aks holda birinchi saralanmagan kirishni chiqaring.

```python
n, m = map(int, input().split())
tarmoq = [tuple(map(int, input().split())) for _ in range(m)]

javob = "ha"
for maska in range(2 ** n):
    kirish = [(maska >> (n - 1 - i)) & 1 for i in range(n)]    # 0-1 ketma-ketlik
    a = kirish[:]
    for i, j in tarmoq:
        if a[i] > a[j]:
            a[i], a[j] = a[j], a[i]
    if any(a[t] > a[t + 1] for t in range(n - 1)):            # saralanmadi
        javob = "".join(map(str, kirish))
        break
print(javob)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] tarmoq = new int[m][2];
        for (int[] t : tarmoq) {
            t[0] = in.nextInt();
            t[1] = in.nextInt();
        }

        String javob = "ha";
        for (int maska = 0; maska < (1 << n) && javob.equals("ha"); maska++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // 0-1 ketma-ketlik
            int[] kirish = a.clone();
            for (int[] t : tarmoq) {
                if (a[t[0]] > a[t[1]]) {
                    int x = a[t[0]]; a[t[0]] = a[t[1]]; a[t[1]] = x;
                }
            }
            for (int i = 0; i + 1 < n; i++) {
                if (a[i] > a[i + 1]) {                                     // saralanmadi
                    StringBuilder sb = new StringBuilder();
                    for (int b : kirish) sb.append(b);
                    javob = sb.toString();
                    break;
                }
            }
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> tarmoq(m);
    for (auto& [i, j] : tarmoq) cin >> i >> j;

    for (int maska = 0; maska < (1 << n); maska++) {
        vector<int> a(n);
        for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // 0-1 ketma-ketlik
        vector<int> kirish = a;
        for (auto [i, j] : tarmoq) {
            if (a[i] > a[j]) swap(a[i], a[j]);
        }
        for (int i = 0; i + 1 < n; i++) {
            if (a[i] > a[i + 1]) {                                     // saralanmadi
                for (int b : kirish) cout << b;
                cout << endl;
                return 0;
            }
        }
    }
    cout << "ha" << endl;
    return 0;
}
```

| Tarmoq (*n* = 4) | Natija |
|---|---|
| (0,1) (2,3) (0,2) (1,3) (1,2) | `ha` — bu 4 element uchun optimal tarmoq |
| (0,1) (2,3) (0,2) (1,3) | `0101` — oxirgi (1,2) taqqoslagich yetishmaydi |

Ikkinchi tarmoqda `0101` kirishi `0011` boʻlishi kerak edi, lekin oʻrtadagi ikki element tekshirilmay qoldi. 0-1 prinsipi kafolat beradi: birinchi tarmoq 16 ta 0-1 kirishning hammasini saralagani uchun, istalgan 4 ta sonni ham toʻgʻri saralaydi. Kirishni ikkilik sonning bitlaridan olish — barcha 0-1 ketma-ketliklarni sanab chiqishning eng qisqa usuli.

:::warning
**Koʻp uchraydigan xatolar**

- **Pufaksimon saralashda oxirgi saralangan qismni qayta tekshirish.** *k*-oʻtishdan keyin oxirgi *k* element joyida; ichki sikl `n − otish` gacha boradi.
- **Quickselectʼda ikkala qismga ham davom etish.** Bu tez saralashga aylanadi va *O*(*n* log *n*) boʻladi; faqat *k* tushgan qism.
- **Quickselectʼda 1 dan va 0 dan sanashni aralashtirish.** Kirishdagi *k* ni darhol `k − 1` ga aylantiring.
- **Tarmoqni faqat bir nechta tasodifiy kirishda tekshirish.** Xato kamdan-kam kirishda chiqishi mumkin; 0-1 prinsipi toʻliq tekshiruv beradi.
:::

:::tip
Quickselectʼni tekshirishda natijani oddiy saralash bilan solishtiring: `sorted(a)[k − 1]`. Bir nechta tasodifiy massivda ikkalasi bir xil javob bersa, algoritm toʻgʻri ishlaydi.
:::

## Xulosa

- Pufaksimon saralashda kichik elementlar („toshbaqalar“) sekin siljiydi; kokteyl saralash ikki yoʻnalishdagi oʻtishlar bilan buni tuzatadi.
- Quickselect Xoar ajratishidan keyin faqat bitta qismga davom etib, *k*-chi elementni oʻrtacha *O*(*n*) da topadi.
- 0-1 prinsipi: tarmoq barcha 2ⁿ ta 0-1 kirishni saralasa, istalgan kirishni saralaydi.
- Algoritm gʻoyalari (ajratish, ikki tomonlama oʻtish, maʼlumotga bogʻliq boʻlmagan tarmoq) boshqa masalalarda ham qayta ishlatiladi.

## Oʻzingizni tekshiring

:::check
Q: Pufaksimon saralashda „toshbaqa“ nima?
A: Massiv oxiriga yaqin turgan kichik element: har oʻtishda faqat bitta oʻringa chapga siljiydi.
Q: Quickselect nima uchun oʻrtacha *O*(*n*)?
A: Har ajratishdan keyin faqat bitta qism qoladi: *n* + *n*/2 + *n*/4 + … ≈ 2*n*.
Q: 8 kirishli saralash tarmogʻini 0-1 prinsipi bilan tekshirish uchun nechta kirish sinanadi?
A: 2⁸ = 256 ta.
Q: `6 5 4 3 2 1` massivi uchun kokteyl saralash pufaksimondan tezmi?
A: Sezilarli emas: teskari tartibda ikkalasi ham *O*(*n*²); kokteyl saralash toshbaqalar koʻp boʻlgan „deyarli saralangan“ massivlarda yutadi.
:::

:::link https://en.wikipedia.org/wiki/Quickselect
Quickselect
Xoarning tanlash algoritmi va uning murakkabligi (ingliz tilida).
:::
