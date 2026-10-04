---
title: Rekursiv funksiyalar bilan ishlash
summary: Rekursiya tushunchasi, bazaviy holat va rekursiv qadam, chaqiruvlar steki, stekning toʻlib ketishi, rekursiya va iteratsiya, Xanoy minoralari, sonni ikkilik koʻrinishga oʻtkazish.
---

## Nima uchun bu mavzu muhim?

Kompyuterdagi papka ichida papkalar, ularning ichida yana papkalar boʻladi. „Papka hajmi“ni qanday hisoblash mumkin? Javob oddiy: papka hajmi = undagi fayllar hajmi + har bir ichki papkaning hajmi. Taʼrifda „papka hajmi“ning oʻzi qatnashyapti — bu **rekursiya**.

Rekursiya — masalani oʻzining kichikroq nusxasi orqali yechish. U daraxtsimon tuzilmalar (papkalar, oila shajarasi, veb-sahifa elementlari), „boʻl va hukmronlik qil“ algoritmlari (tez saralash, ikkilik qidiruv) va koʻp matematik taʼriflar uchun tabiiy vosita. Baʼzi masalalar rekursiyasiz ancha murakkab yoziladi.

:::note
**Bu darsdan soʻng siz:**

- rekursiv funksiyaning ikki qismini — bazaviy holat va rekursiv qadamni — ajrata olasiz;
- chaqiruvlar steki qanday ishlashini va stek nima uchun toʻlib ketishini tushuntira olasiz;
- faktorial, darajaga koʻtarish, ikkilik koʻrinish kabi masalalarni rekursiv yecha olasiz;
- Xanoy minoralari masalasini rekursiya bilan yecha olasiz;
- rekursiya va sikl oʻrtasida asosli tanlov qila olasiz.
:::

## Rekursiya tushunchasi

**Rekursiv funksiya** — oʻzini oʻzi chaqiradigan funksiya. Toʻgʻri rekursiv funksiyada doim ikki qism bor:

| Qism | Vazifasi | Faktorial misolida |
|---|---|---|
| **Bazaviy holat (base case)** | eng oddiy holat, javob toʻgʻridan-toʻgʻri maʼlum; rekursiya shu yerda toʻxtaydi | *n* = 0 boʻlsa, 0! = 1 |
| **Rekursiv qadam** | masala oʻzining kichikroq nusxasiga keltiriladi | *n*! = *n* · (*n* − 1)! |

Har bir rekursiv chaqiruv masalani bazaviy holatga **yaqinlashtirishi** kerak: *n* → *n* − 1 → … → 0. Aks holda rekursiya cheksiz boʻladi.

Rekursiv fikrlash: „Agar men kichikroq masalani yecha olaman deb ishonsam, katta masalani qanday yechaman?“ Faktorial uchun: (*n* − 1)! maʼlum deb faraz qilsak, *n*! = *n* · (*n* − 1)!.

## Chaqiruvlar steki

Funksiya chaqirilganda kompyuter uning parametrlari va lokal oʻzgaruvchilari uchun xotirada **freym (frame)** ajratadi va uni **chaqiruvlar steki (call stack)** ga qoʻyadi. Funksiya tugaganda freym stekdan olib tashlanadi. Stek — „oxirgi kelgan birinchi ketadi“ tuzilmasi, xuddi bir-birining ustiga qoʻyilgan likopchalar kabi.

`faktorial(3)` chaqirilganda:

| Qadam | Stek (yuqorisi oʻngda) | Nima boʻladi |
|---|---|---|
| 1 | f(3) | 3 · f(2) ni hisoblash kerak — kutadi |
| 2 | f(3) f(2) | 2 · f(1) — kutadi |
| 3 | f(3) f(2) f(1) | 1 · f(0) — kutadi |
| 4 | f(3) f(2) f(1) f(0) | bazaviy holat: 1 qaytaradi |
| 5 | f(3) f(2) f(1) | 1 · 1 = 1 qaytaradi |
| 6 | f(3) f(2) | 2 · 1 = 2 qaytaradi |
| 7 | f(3) | 3 · 2 = 6 qaytaradi |

Rekursiya ikki bosqichda ishlaydi: **pastga tushish** (chaqiruvlar to bazaviy holatgacha) va **yuqoriga qaytish** (natijalar teskari tartibda yigʻiladi).

Stek hajmi cheklangan. Juda chuqur rekursiya yoki bazaviy holatsiz rekursiya **stekning toʻlib ketishi**ga olib keladi:

| Til | Xato |
|---|---|
| Python | `RecursionError: maximum recursion depth exceeded` (chegara odatda 1000) |
| Java | `StackOverflowError` |
| C++ | dastur qulaydi (segmentation fault) |

## Rekursiya yoki sikl?

Har qanday rekursiyani sikl bilan yozish mumkin va aksincha. Tanlov masalaga bogʻliq:

| Rekursiya qulay | Sikl qulay |
|---|---|
| tuzilma daraxtsimon (papkalar, shajara) | oddiy ketma-ketlik (yigʻindi, faktorial) |
| „boʻl va hukmronlik qil“ (Xanoy, tez saralash) | chuqurlik juda katta boʻlishi mumkin |
| taʼrifning oʻzi rekursiv | tezlik va xotira muhim |

Ehtiyot boʻling: Fibonachchi sonlarining „toʻgʻridan-toʻgʻri“ rekursiyasi `fib(n) = fib(n-1) + fib(n-2)` bir xil qiymatlarni qayta-qayta hisoblaydi va *n* = 40 da ham sezilarli sekin ishlaydi (chaqiruvlar soni eksponensial oʻsadi). A7 dagi sikl bilan esa bir zumda.

## Ishlangan misol 1: rekursiv faktorial va raqamlar yigʻindisi

Butun son *n* (0 ≤ *n* ≤ 20) kiritiladi. *n*! va *n* raqamlari yigʻindisini rekursiv funksiyalar bilan hisoblang.

```python
def faktorial(n):
    if n == 0:                       # bazaviy holat
        return 1
    return n * faktorial(n - 1)      # rekursiv qadam


def raqamlar_yigindisi(n):
    if n < 10:                       # bazaviy holat: bitta raqam
        return n
    return n % 10 + raqamlar_yigindisi(n // 10)


n = int(input())
print(faktorial(n), raqamlar_yigindisi(n))
```
```java
import java.util.Scanner;

public class Main {
    static long faktorial(int n) {
        if (n == 0) {                    // bazaviy holat
            return 1;
        }
        return n * faktorial(n - 1);     // rekursiv qadam
    }

    static int raqamlarYigindisi(int n) {
        if (n < 10) {                    // bazaviy holat: bitta raqam
            return n;
        }
        return n % 10 + raqamlarYigindisi(n / 10);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        System.out.println(faktorial(n) + " " + raqamlarYigindisi(n));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long faktorial(int n) {
    if (n == 0) {                    // bazaviy holat
        return 1;
    }
    return n * faktorial(n - 1);     // rekursiv qadam
}

int raqamlarYigindisi(int n) {
    if (n < 10) {                    // bazaviy holat: bitta raqam
        return n;
    }
    return n % 10 + raqamlarYigindisi(n / 10);
}

int main() {
    int n;
    cin >> n;
    cout << faktorial(n) << " " << raqamlarYigindisi(n) << endl;
    return 0;
}
```

Kirish `5` boʻlsa: `faktorial(5)` = 5 · 4 · 3 · 2 · 1 · 1 = 120; `raqamlar_yigindisi(5)` = 5 (bazaviy holat darhol). Chiqadi: `120 5`.

Kirish `12` boʻlsa: 12! = 479001600; raqamlar: `12 % 10 + yigindi(1)` = 2 + 1 = 3. Chiqadi: `479001600 3`.

Ikkala funksiyada ham avval bazaviy holat tekshiriladi — bu rekursiv funksiyaning birinchi qatori boʻlishi odat.

## Ishlangan misol 2: Xanoy minoralari

Uchta ustun bor: A, B, C. A ustunida *n* ta disk — kattasi pastda, kichigi tepada. Barcha disklarni A dan C ga oʻtkazish kerak. Qoidalar: bir vaqtda faqat bitta disk koʻchiriladi; katta disk kichigining ustiga qoʻyilmaydi. Barcha yurishlarni chiqaring.

Rekursiv gʻoya: *n* ta diskni A dan C ga oʻtkazish uchun —

1. yuqoridagi *n* − 1 ta diskni A dan B ga oʻtkazamiz (C yordamchi);
2. eng katta diskni A dan C ga koʻchiramiz;
3. *n* − 1 ta diskni B dan C ga oʻtkazamiz (A yordamchi).

Bazaviy holat: *n* = 0 — hech narsa qilinmaydi.

```python
def xanoy(n, qayerdan, qayerga, yordamchi):
    if n == 0:
        return
    xanoy(n - 1, qayerdan, yordamchi, qayerga)
    print(qayerdan, "->", qayerga)
    xanoy(n - 1, yordamchi, qayerga, qayerdan)


n = int(input())
xanoy(n, "A", "C", "B")
```
```java
import java.util.Scanner;

public class Main {
    static void xanoy(int n, char qayerdan, char qayerga, char yordamchi) {
        if (n == 0) {
            return;
        }
        xanoy(n - 1, qayerdan, yordamchi, qayerga);
        System.out.println(qayerdan + " -> " + qayerga);
        xanoy(n - 1, yordamchi, qayerga, qayerdan);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        xanoy(n, 'A', 'C', 'B');
    }
}
```
```cpp
#include <iostream>
using namespace std;

void xanoy(int n, char qayerdan, char qayerga, char yordamchi) {
    if (n == 0) {
        return;
    }
    xanoy(n - 1, qayerdan, yordamchi, qayerga);
    cout << qayerdan << " -> " << qayerga << endl;
    xanoy(n - 1, yordamchi, qayerga, qayerdan);
}

int main() {
    int n;
    cin >> n;
    xanoy(n, 'A', 'C', 'B');
    return 0;
}
```

Kirish `3` boʻlsa, 7 ta yurish chiqadi:

```text
A -> C
A -> B
C -> B
A -> C
B -> A
B -> C
A -> C
```

Yurishlar soni 2ⁿ − 1: 3 disk — 7, 10 disk — 1023, 64 disk — 18 kvintilliondan ortiq. Rekursiv yechim uch qatordan iborat; siklli yechim ancha murakkab. Bu — rekursiya „oʻz oʻrnida“ boʻlgan klassik misol: biz har bir yurishni oʻylamaymiz, faqat masalani kichikrogʻiga keltirish usulini aytamiz.

## Ishlangan misol 3: ikkilik koʻrinish

Natural son *n* kiritiladi. Uning ikkilik sanoq sistemasidagi koʻrinishini chiqaring.

Ikkilik raqamlar 2 ga boʻlish qoldiqlari, lekin **teskari tartibda**: oxirgi qoldiq — birinchi raqam. Rekursiya tartibni oʻzi aylantiradi: avval *n* // 2 ning ikkilik koʻrinishi chiqariladi, keyin *n* % 2.

```python
def ikkilik(n):
    if n >= 2:
        ikkilik(n // 2)          # avval yuqori raqamlar
    print(n % 2, end="")         # keyin oxirgi raqam


n = int(input())
ikkilik(n)
print()
```
```java
import java.util.Scanner;

public class Main {
    static void ikkilik(int n) {
        if (n >= 2) {
            ikkilik(n / 2);          // avval yuqori raqamlar
        }
        System.out.print(n % 2);     // keyin oxirgi raqam
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        ikkilik(n);
        System.out.println();
    }
}
```
```cpp
#include <iostream>
using namespace std;

void ikkilik(int n) {
    if (n >= 2) {
        ikkilik(n / 2);          // avval yuqori raqamlar
    }
    cout << n % 2;               // keyin oxirgi raqam
}

int main() {
    int n;
    cin >> n;
    ikkilik(n);
    cout << endl;
    return 0;
}
```

Kirish `13` boʻlsa: `ikkilik(13)` → `ikkilik(6)` → `ikkilik(3)` → `ikkilik(1)`. Bazaviy holatda `1` chiqadi, keyin qaytishda: 3 % 2 = `1`, 6 % 2 = `0`, 13 % 2 = `1`. Chiqadi: `1101` (8 + 4 + 0 + 1 = 13).

Chiqarish rekursiv chaqiruvdan **keyin** turgani uchun raqamlar „qaytish“ bosqichida, yaʼni teskari tartibda chiqadi. Agar `print` chaqiruvdan oldin tursa, natija `1011` — teskari boʻlardi.

:::warning
**Koʻp uchraydigan xatolar**

- **Bazaviy holat yoʻq yoki unga yetib boʻlmaydi.** `faktorial(n - 1)` manfiy *n* bilan chaqirilsa yoki `n == 0` tekshirilmasa — stek toʻlib ketadi.
- **Rekursiv chaqiruv natijasini qaytarmaslik.** `faktorial(n - 1) * n` hisoblanadi, lekin `return` yoʻq — natija yoʻqoladi.
- **Juda chuqur rekursiya.** Python'da 1000 dan ortiq chuqurlik — `RecursionError`; bunday hollarda sikl ishlating.
- **Bir xil hisoblarni takrorlash.** Fibonachchining oddiy rekursiyasi *n* = 40 da yuz millionlab chaqiruv qiladi.
- **Global oʻzgaruvchida natija toʻplash.** Rekursiv funksiya natijani `return` orqali bersin.
:::

:::tip
Rekursiv funksiya yozishda avval bazaviy holatni yozing, keyin „kichikroq masala yechilgan deb faraz qilaman“ deb rekursiv qadamni yozing. Funksiyaning ichki ishini butun chuqurlik boʻyicha kuzatishga urinmang — bitta qadam toʻgʻri boʻlsa, butun rekursiya toʻgʻri.
:::

## Xulosa

- Rekursiv funksiya oʻzini chaqiradi; unda bazaviy holat va masalani kichraytiradigan rekursiv qadam boʻlishi shart.
- Har bir chaqiruv chaqiruvlar stekida freym oladi; rekursiya pastga tushadi, keyin natijalarni yigʻib qaytadi.
- Stek cheklangan: bazaviy holatsiz yoki juda chuqur rekursiya stekni toʻldirib yuboradi.
- Rekursiya daraxtsimon va „boʻl va hukmronlik qil“ masalalarida qulay (Xanoy minoralari: 2ⁿ − 1 yurish).
- Chaqiruvdan keyingi amallar teskari tartibda bajariladi (ikkilik koʻrinish).
- Oddiy ketma-ketliklar va katta chuqurlik uchun sikl maʼqulroq.

## Oʻzingizni tekshiring

:::check
Q: Rekursiv funksiyaning qaysi ikki qismi boʻlishi shart?
A: Bazaviy holat (rekursiya toʻxtaydigan eng oddiy holat) va masalani bazaviy holatga yaqinlashtiradigan rekursiv qadam.
Q: `def f(n): return n + f(n - 1)` funksiyasida qanday xato bor?
A: Bazaviy holat yoʻq: rekursiya hech qachon toʻxtamaydi va stek toʻlib ketadi (Python'da `RecursionError`).
Q: Xanoy minoralari masalasida 5 ta disk uchun nechta yurish kerak?
A: 2⁵ − 1 = 31.
Q: 3-misolda `ikkilik(6)` nima chiqaradi?
A: `110`.
Q: Nima uchun Fibonachchi sonlarini oddiy rekursiya bilan hisoblash sekin?
A: Bir xil qiymatlar qayta-qayta hisoblanadi: `fib(n-1)` va `fib(n-2)` ham ichida `fib(n-3)` ni hisoblaydi va hokazo; chaqiruvlar soni eksponensial oʻsadi.
:::

:::link https://en.wikipedia.org/wiki/Tower_of_Hanoi
Wikipedia: Xanoy minoralari
Masalaning tarixi, yechimi va yurishlar soni.
:::

:::link https://visualgo.net/en/recursion
VisuAlgo: rekursiya daraxti
Rekursiv chaqiruvlarning vizualizatsiyasi.
:::
