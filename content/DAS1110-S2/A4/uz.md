---
title: Rekursiv funksiya. Foydalanuvchi kutubxonasini tashkil etish
summary: Massiv ustida rekursiya, memoizatsiya (Fibonachchi), qaytish usuli bilan barcha variantlarni hosil qilish, oʻz funksiyalar kutubxonasini yaratish — Python moduli, Java sinfi va C++ sarlavha fayli va nomlar fazosi.
---

## Nima uchun bu mavzu muhim?

Birinchi semestrda rekursiyaning asosini koʻrdik: bazaviy holat, rekursiv qadam va chaqiruvlar steki. Amaliyotda rekursiya uchta vaziyatda ayniqsa foydali: maʼlumotni **boʻlaklarga boʻlib** ishlash (massiv, satr), bir xil hisoblar koʻp takrorlanganda **natijani eslab qolish**, va barcha **variantlarni sanab chiqish** (parollar, jadval tuzish, oʻyin yurishlari).

Yozilgan foydali funksiyalarni har bir yangi dasturga koʻchirib yurish noqulay. Ularni **kutubxona**ga yigʻib, kerakli joyda ulash kerak. Bu mashgʻulotda ikkalasini mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- massiv ustida rekursiv funksiya (yigʻindi, maksimum, teskari aylantirish) yoza olasiz;
- memoizatsiya bilan rekursiyani eksponensial vaqtdan chiziqli vaqtga tushira olasiz;
- qaytish usuli (backtracking) bilan barcha variantlarni hosil qila olasiz;
- oʻz funksiyalaringizni Python moduli, Java sinfi yoki C++ sarlavha fayli va nomlar fazosiga yigʻa olasiz.
:::

## Massiv ustida rekursiya

Massivli masalani rekursiv yechish uchun uni **kichikroq massivli xuddi shunday masala**ga keltiramiz:

```text
yigindi(a, n) = 0,                          agar n = 0
yigindi(a, n) = yigindi(a, n - 1) + a[n-1], aks holda
```

Massivni har safar nusxalamaslik uchun funksiyaga massivning oʻzi va **chegaralar** (`n` yoki `chap`, `ong` indekslari) uzatiladi. Teskari aylantirish uchun ham shunday: chetki ikki elementni almashtirib, ichki qismni rekursiv aylantiramiz; `chap >= ong` boʻlsa — bazaviy holat.

## Memoizatsiya

Fibonachchi sonlari: *F*(0) = 0, *F*(1) = 1, *F*(*n*) = *F*(*n* − 1) + *F*(*n* − 2). Toʻgʻridan-toʻgʻri rekursiya juda sekin:

| *n* | Oddiy rekursiyadagi chaqiruvlar | Memoizatsiya bilan |
|---|---|---|
| 10 | 177 | 19 |
| 30 | 2 692 537 | 59 |
| 50 | ~4 · 10¹⁰ (bir necha daqiqa) | 99 |

Sabab — bir xil qiymatlar qayta-qayta hisoblanadi: *F*(50) ichida *F*(48) ikki marta, *F*(47) uch marta va hokazo. **Memoizatsiya** — har bir hisoblangan natijani jadvalga yozib qoʻyish va keyingi safar jadvaldan olish. Shunda har bir *F*(*k*) faqat bir marta hisoblanadi.

| Til | Memo jadvali |
|---|---|
| Python | lugʻat yoki `@functools.lru_cache` (`@cache`) bezagichi |
| Java | `long[] memo = new long[n + 1]` yoki `HashMap` |
| C++ | `vector<long long> memo(n + 1, -1)` |

## Qaytish usuli (backtracking)

Barcha variantlarni hosil qilish uchun javob **qadamma-qadam** quriladi: har bir qadamda mumkin boʻlgan tanlovlardan biri qilinadi, rekursiv davom ettiriladi, soʻng tanlov **bekor qilinadi** va keyingisi sinab koʻriladi.

```text
qur(joriy):
    agar joriy toʻliq boʻlsa: uni chiqar; qayt
    har bir mumkin tanlov t uchun:
        joriyga t ni qoʻsh
        qur(joriy)
        t ni olib tashla          ← „qaytish“
```

Shartga mos kelmaydigan tanlovlar darhol tashlab yuboriladi — bu qidiruvni ancha qisqartiradi.

## Foydalanuvchi kutubxonasi

Bir nechta dasturda kerak boʻladigan funksiyalar alohida faylga yigʻiladi:

| Til | Kutubxona fayli | Ulash |
|---|---|---|
| Python | `massivlar.py` — oddiy funksiyalar | `import massivlar` → `massivlar.yigindi(a)` yoki `from massivlar import yigindi` |
| Java | `Massivlar.java` — `public final class` va `static` metodlar (`Math` sinfi kabi) | bir paketda — toʻgʻridan-toʻgʻri `Massivlar.yigindi(a)`; boshqa paketda — `import` |
| C++ | `massivlar.h` — eʼlonlar (prototiplar), `massivlar.cpp` — tanalar | `#include "massivlar.h"`, kompilyatsiya: `g++ main.cpp massivlar.cpp` |

C++'da funksiyalar **nomlar fazosi** (`namespace massivlar { ... }`) ichiga olinadi — boshqa kutubxonadagi bir xil nomli funksiyalar bilan toʻqnashmasligi uchun. Sarlavha fayli boshida `#pragma once` yoziladi: fayl bir necha marta ulansa ham, bir marta oʻqiladi.

Avtomatik tekshiruv tizimi bitta fayl qabul qiladi, shuning uchun quyidagi misollarda kutubxona shu faylning ichida turadi — lekin u xuddi alohida faylga koʻchirishga tayyor koʻrinishda yozilgan.

## Ishlangan misol 1: memoizatsiyali Fibonachchi

*n* kiritiladi (0 ≤ *n* ≤ 90). *F*(*n*) ni memoizatsiyali rekursiya bilan hisoblang.

```python
from functools import cache


@cache                          # natijalarni avtomatik eslab qoladi
def fib(n):
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)


n = int(input())
print(fib(n))
```
```java
import java.util.Scanner;

public class Main {
    static long[] memo;

    static long fib(int n) {
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];      // avval hisoblangan
        memo[n] = fib(n - 1) + fib(n - 2);
        return memo[n];
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        memo = new long[n + 1];
        System.out.println(fib(n));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

vector<long long> memo;

long long fib(int n) {
    if (n < 2) return n;
    if (memo[n] != -1) return memo[n];         // avval hisoblangan
    return memo[n] = fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    memo.assign(n + 1, -1);
    cout << fib(n) << endl;
    return 0;
}
```

`10` kiritilsa — `55`, `90` kiritilsa — `2880067194370816120` (bu `long` ga sigʻadi, *F*(93) esa sigʻmaydi). Java'da `memo[n] != 0` sharti ishlaydi, chunki *n* ≥ 2 uchun *F*(*n*) hech qachon 0 emas; C++'da „hisoblanmagan“ belgisi sifatida `-1` olingan. Memoizatsiyasiz *n* = 90 hisoblanib boʻlmasdi.

## Ishlangan misol 2: qaytish usuli — ketma-ket birlarsiz satrlar

*n* kiritiladi (1 ≤ *n* ≤ 15). Uzunligi *n* boʻlgan, `0` va `1` dan iborat, ikkita `1` yonma-yon kelmaydigan barcha satrlarni leksikografik tartibda, oxirida ularning sonini chiqaring.

```python
def qur(joriy, n, natija):
    if len(joriy) == n:
        natija.append(joriy)
        return
    qur(joriy + "0", n, natija)
    if not joriy or joriy[-1] != "1":      # oldingi belgi 1 boʻlsa, 1 qoʻyilmaydi
        qur(joriy + "1", n, natija)


n = int(input())
natija = []
qur("", n, natija)
print("\n".join(natija))
print(len(natija))
```
```java
import java.util.Scanner;

public class Main {
    static StringBuilder chiqish = new StringBuilder();
    static int soni = 0;

    static void qur(char[] joriy, int i) {
        if (i == joriy.length) {
            chiqish.append(joriy).append('\n');
            soni++;
            return;
        }
        joriy[i] = '0';
        qur(joriy, i + 1);
        if (i == 0 || joriy[i - 1] != '1') {   // oldingi belgi 1 boʻlsa, 1 qoʻyilmaydi
            joriy[i] = '1';
            qur(joriy, i + 1);
        }
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        qur(new char[n], 0);
        System.out.print(chiqish);
        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int soni = 0;

void qur(string& joriy, int n) {
    if ((int) joriy.size() == n) {
        cout << joriy << '\n';
        soni++;
        return;
    }
    joriy.push_back('0');
    qur(joriy, n);
    joriy.pop_back();                          // tanlovni bekor qilamiz
    if (joriy.empty() || joriy.back() != '1') {  // oldingi belgi 1 boʻlsa, 1 qoʻyilmaydi
        joriy.push_back('1');
        qur(joriy, n);
        joriy.pop_back();
    }
}

int main() {
    int n;
    cin >> n;
    string joriy;
    qur(joriy, n);
    cout << soni << endl;
    return 0;
}
```

`3` kiritilsa, chiqish:

```text
000
001
010
100
101
5
```

Avval `0`, keyin `1` sinab koʻrilgani uchun satrlar oʻz-oʻzidan leksikografik tartibda chiqadi. C++ versiyasida „qaytish“ aniq koʻrinadi: `push_back` — tanlov, `pop_back` — uni bekor qilish. Python versiyasida `joriy + "0"` yangi satr yaratadi, shuning uchun bekor qilish shart emas. Qiziq fakt: bunday satrlar soni Fibonachchi sonlari boʻyicha oʻsadi — 2, 3, 5, 8, 13, …

## Ishlangan misol 3: massivlar kutubxonasi

Birinchi satrda *n*, ikkinchisida *n* ta butun son. `massivlar` kutubxonasidagi rekursiv funksiyalar yordamida yigʻindini, maksimumni va teskari tartibdagi massivni chiqaring.

```python
# ---- massivlar.py faylining mazmuni ----
def yigindi(a, n):
    """a ning dastlabki n ta elementi yigʻindisi."""
    if n == 0:
        return 0
    return yigindi(a, n - 1) + a[n - 1]


def maksimum(a, n):
    if n == 1:
        return a[0]
    return max(maksimum(a, n - 1), a[n - 1])


def teskari(a, chap, ong):
    if chap >= ong:
        return
    a[chap], a[ong] = a[ong], a[chap]
    teskari(a, chap + 1, ong - 1)
# ---- asosiy dastur (alohida faylda: import massivlar) ----


n = int(input())
a = list(map(int, input().split()))
print(yigindi(a, n))
print(maksimum(a, n))
teskari(a, 0, n - 1)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    // Massivlar.java faylida public final class boʻladi
    static final class Massivlar {
        private Massivlar() {}                 // obyekt yaratilmaydi: faqat static metodlar

        static long yigindi(int[] a, int n) {
            if (n == 0) return 0;
            return yigindi(a, n - 1) + a[n - 1];
        }

        static int maksimum(int[] a, int n) {
            if (n == 1) return a[0];
            return Math.max(maksimum(a, n - 1), a[n - 1]);
        }

        static void teskari(int[] a, int chap, int ong) {
            if (chap >= ong) return;
            int t = a[chap];
            a[chap] = a[ong];
            a[ong] = t;
            teskari(a, chap + 1, ong - 1);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        System.out.println(Massivlar.yigindi(a, n));
        System.out.println(Massivlar.maksimum(a, n));
        Massivlar.teskari(a, 0, n - 1);
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
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

// ---- massivlar.h va massivlar.cpp mazmuni ----
namespace massivlar {
    long long yigindi(const vector<int>& a, int n) {
        if (n == 0) return 0;
        return yigindi(a, n - 1) + a[n - 1];
    }

    int maksimum(const vector<int>& a, int n) {
        if (n == 1) return a[0];
        return max(maksimum(a, n - 1), a[n - 1]);
    }

    void teskari(vector<int>& a, int chap, int ong) {
        if (chap >= ong) return;
        swap(a[chap], a[ong]);
        teskari(a, chap + 1, ong - 1);
    }
}
// ---- asosiy dastur ----

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    cout << massivlar::yigindi(a, n) << endl;
    cout << massivlar::maksimum(a, n) << endl;
    massivlar::teskari(a, 0, n - 1);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

`5` va `3 8 -1 6 2` kiritilsa, chiqish: `18`, `8` va `2 6 -1 8 3`. Kutubxonadagi funksiyalar hech narsa oʻqimaydi va chiqarmaydi — shuning uchun ularni istalgan dasturda qayta ishlatish mumkin. C++'da `massivlar::yigindi` — nomlar fazosi orqali murojaat; Java'da `Massivlar.yigindi` — sinf nomi orqali, xuddi `Math.max` kabi. Har bir rekursiv chaqiruv stekda joy egallaydi, shuning uchun bunday funksiyalar bir necha ming elementgacha xavfsiz; juda katta massivlar uchun sikl afzal.

:::warning
**Koʻp uchraydigan xatolar**

- **Bazaviy holatni unutish yoki notoʻgʻri yozish.** Rekursiya toʻxtamaydi va stek toʻlib ketadi (`RecursionError`, `StackOverflowError`).
- **Memo jadvalini funksiya ichida yaratish.** Har bir chaqiruvda yangi jadval paydo boʻladi va hech narsa eslab qolinmaydi; jadval funksiyadan tashqarida boʻlishi kerak.
- **Qaytish usulida tanlovni bekor qilmaslik.** Umumiy holat (masalan, C++'dagi `joriy`) keyingi tarmoqqa „ifloslangan“ holda oʻtadi.
- **Python'da `import` qilinadigan faylga sinov kodi yozish.** Modul ulanganda bu kod ham bajariladi; uni `if __name__ == "__main__":` ichiga oling.
:::

:::tip
Rekursiv funksiyani tekshirishda eng kichik holatlardan boshlang: *n* = 0, 1, 2. Ular toʻgʻri ishlasa va rekursiv qadam toʻgʻri boʻlsa, funksiya barcha *n* uchun toʻgʻri ishlaydi — bu matematik induksiyaning oʻzi.
:::

## Xulosa

- Massivli masala chegaralarni kichraytirib, xuddi shunday kichikroq masalaga keltiriladi.
- Memoizatsiya bir xil hisoblarni takrorlamaydi: Fibonachchi eksponensial vaqtdan chiziqli vaqtga tushadi.
- Qaytish usuli: tanlov → rekursiya → tanlovni bekor qilish; mos kelmaydigan tarmoqlar darhol kesiladi.
- Kutubxona: Python — modul, Java — `static` metodli sinf, C++ — sarlavha fayli, `.cpp` fayl va nomlar fazosi.
- Kutubxona funksiyalari oʻqimaydi va chiqarmaydi — faqat hisoblaydi yoki maʼlumotni oʻzgartiradi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun oddiy rekursiv Fibonachchi sekin ishlaydi?
A: Bir xil qiymatlar (masalan, *F*(*n* − 2)) koʻp marta qayta hisoblanadi; chaqiruvlar soni eksponensial oʻsadi.
Q: Memoizatsiya nima?
A: Hisoblangan natijalarni jadvalda saqlab, keyingi safar qayta hisoblamasdan jadvaldan olish.
Q: Qaytish usulida „qaytish“ nimani anglatadi?
A: Rekursiv chaqiruvdan soʻng qilingan tanlovni bekor qilib, keyingi variantni sinash.
Q: C++'da nomlar fazosi nima uchun kerak?
A: Turli kutubxonalardagi bir xil nomli funksiyalar toʻqnashmasligi uchun: `massivlar::yigindi` va `matritsa::yigindi` farqlanadi.
Q: Python modulidagi sinov kodi import qilinganda bajarilmasligi uchun nima qilinadi?
A: U `if __name__ == "__main__":` bloki ichiga yoziladi.
:::

:::link https://docs.python.org/3/tutorial/modules.html
Python: modullar
Oʻz modulingizni yaratish va `import` qilish haqida rasmiy qoʻllanma (ingliz tilida).
:::

:::link https://docs.python.org/3/library/functools.html#functools.cache
Python: functools.cache
Funksiya natijalarini avtomatik eslab qoluvchi bezagich (ingliz tilida).
:::
