---
title: Funksiyalar bilan ishlash. Funksiya tavsifi. Qiymat qaytaruvchi funksiya. Funksiya-protsedura. Funksiyalarni qayta yuklash
summary: Funksiya tavsifi (imzo va prototip), qiymat qaytaruvchi funksiya va protsedura, bir nechta natija qaytarish (kortej, havola, massiv), funksiyalarni qayta yuklash va standart parametrlar, masalani funksiyalarga boʻlish.
---

## Nima uchun bu mavzu muhim?

Birinchi semestrda funksiyani eʼlon qilish, chaqirish va undan qiymat qaytarishni oʻrgandik. Amaliyotda esa savollar shunday boʻladi: funksiya **ikkita** natija qaytarishi kerak boʻlsa-chi? Bir xil ishni turli parametrlar bilan bajaradigan funksiyalarga har xil nom oʻylab topish shartmi? Katta masalani qanday qilib kichik, tekshirsa boʻladigan funksiyalarga boʻlish kerak?

Bu amaliy mashgʻulotda funksiyalarni **loyihalash** koʻnikmasini mashq qilamiz: funksiya tavsifini aniq yozish, protsedura va qiymat qaytaruvchi funksiyani farqlash, bir nechta natija qaytarish va qayta yuklash.

:::note
**Bu mashgʻulotdan soʻng siz:**

- funksiya tavsifini (nomi, parametrlari, qaytaradigan turi) aniq yoza olasiz;
- qiymat qaytaruvchi funksiya va protseduradan oʻrinli foydalana olasiz;
- funksiyadan bir nechta natijani uch tilda qaytara olasiz;
- Java va C++'da funksiyalarni qayta yuklay olasiz, Python'da esa standart parametrlar bilan shunga erishasiz;
- masalani bir-birini chaqiradigan kichik funksiyalarga boʻla olasiz.
:::

## Funksiya tavsifi

Funksiya **tavsifi** (imzosi) — undan foydalanish uchun bilish kerak boʻlgan hamma narsa: nomi, parametrlari va ularning turlari, qaytaradigan qiymat turi. Tana esa „qanday ishlashi“ — chaqiruvchi uchun ahamiyatsiz.

| Til | Tavsif misoli |
|---|---|
| C++ (prototip) | `double ortacha(const vector<int>& a);` |
| Java | `static double ortacha(int[] a)` |
| Python | `def ortacha(a: list[int]) -> float:` (tur izohlari ixtiyoriy) |

Yaxshi funksiya **bitta ishni** bajaradi va nomi shu ishni aytadi: `ortacha`, `tubmi`, `chiqar_jadval`. Funksiya nomi „va“ soʻzi bilan tavsiflansa (masalan, „oʻqiydi **va** saralaydi **va** chiqaradi“), uni boʻlish kerak.

## Qiymat qaytaruvchi funksiya va protsedura

| | Qiymat qaytaruvchi funksiya | Protsedura |
|---|---|---|
| Vazifasi | hisoblaydi va natijani **qaytaradi** | amal **bajaradi** (chiqaradi, massivni oʻzgartiradi) |
| Qaytaradigan turi | `int`, `double`, `bool`, ... | `void` (Java, C++); Python'da `None` |
| Chaqiruv | ifoda ichida: `y = kvadrat(x) + 1` | alohida buyruq: `chiqar(a)` |

Qoida: hisoblovchi funksiya **chiqarmasin**, chiqaruvchi protsedura **hisoblamasin**. Shunda hisoblovchi funksiyani boshqa joyda ham ishlatish va testlash mumkin.

## Bir nechta natija qaytarish

`return` faqat bitta qiymat qaytaradi. Bir nechta natija kerak boʻlsa:

| Til | Usul | Misol |
|---|---|---|
| Python | kortej qaytarish | `return kichik, katta` va `a, b = f(x)` |
| Java | kichik sinf yoki `record` | `record Natija(int kichik, int katta)` |
| C++ | havola parametrlar | `void f(..., int& kichik, int& katta)` |
| C++ | `pair` / `struct` qaytarish | `return {kichik, katta};` |

C++'da `int&` — havola parametr: funksiya chaqiruvchining oʻzgaruvchisini toʻgʻridan-toʻgʻri oʻzgartiradi. Katta obyektlarni (vektor, satr) **nusxasiz** uzatish uchun `const vector<int>&` yoziladi — oʻqiladi, lekin oʻzgartirilmaydi.

## Funksiyalarni qayta yuklash

**Qayta yuklash** (overloading) — bir xil nomli, lekin parametrlari (soni yoki turi) har xil boʻlgan bir nechta funksiya. Kompilyator chaqiruvdagi argumentlarga qarab keraklisini tanlaydi:

```text
yuza(r)          → doira
yuza(a, b)       → toʻgʻri toʻrtburchak
yuza(a, b, c)    → uchburchak (Geron formulasi)
```

- **Java va C++** qayta yuklashni toʻgʻridan-toʻgʻri qoʻllaydi. Faqat qaytaradigan turi bilan farq qiluvchi funksiyalar qayta yuklanmaydi.
- **Python** qayta yuklashni qoʻllamaydi: bir xil nomli ikkinchi `def` birinchisini oʻchiradi. Uning oʻrniga **standart parametrlar** (`def f(a, b=None)`) yoki oʻzgaruvchan sonli argumentlar (`*args`) ishlatiladi.
- C++'da ham standart parametrlar bor: `void chiqar(int x, char ajratgich = ' ');`

## Ishlangan misol 1: qayta yuklangan yuza funksiyasi

Birinchi satrda *n*, keyingi *n* satrda bitta, ikkita yoki uchta musbat son: doira radiusi, toʻgʻri toʻrtburchak tomonlari yoki uchburchak tomonlari. Har bir satr uchun yuzani ikki kasr xona bilan chiqaring. Java va C++'da uchta bir xil nomli `yuza` funksiyasi, Python'da standart parametrli bitta funksiya.

```python
from math import pi, sqrt


def yuza(a, b=None, c=None):
    if b is None:                       # bitta son — doira
        return pi * a * a
    if c is None:                       # ikkita — toʻgʻri toʻrtburchak
        return a * b
    p = (a + b + c) / 2                 # uchta — Geron formulasi
    return sqrt(p * (p - a) * (p - b) * (p - c))


n = int(input())
for _ in range(n):
    sonlar = list(map(float, input().split()))
    print(f"{yuza(*sonlar):.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static double yuza(double r) {                       // doira
        return Math.PI * r * r;
    }

    static double yuza(double a, double b) {             // toʻgʻri toʻrtburchak
        return a * b;
    }

    static double yuza(double a, double b, double c) {   // uchburchak (Geron)
        double p = (a + b + c) / 2;
        return Math.sqrt(p * (p - a) * (p - b) * (p - c));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            double s;
            if (q.length == 1) s = yuza(Double.parseDouble(q[0]));
            else if (q.length == 2) s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]));
            else s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]), Double.parseDouble(q[2]));
            System.out.printf("%.2f%n", s);
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

double yuza(double r) { return M_PI * r * r; }              // doira

double yuza(double a, double b) { return a * b; }           // toʻgʻri toʻrtburchak

double yuza(double a, double b, double c) {                 // uchburchak (Geron)
    double p = (a + b + c) / 2;
    return sqrt(p * (p - a) * (p - b) * (p - c));
}

int main() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream oqim(satr);
        vector<double> q;
        double x;
        while (oqim >> x) q.push_back(x);
        double s;
        if (q.size() == 1) s = yuza(q[0]);
        else if (q.size() == 2) s = yuza(q[0], q[1]);
        else s = yuza(q[0], q[1], q[2]);
        printf("%.2f\n", s);
    }
    return 0;
}
```

Kirish:

```text
3
1
3 4
3 4 5
```

Chiqish:

```text
3.14
12.00
6.00
```

Har bir satrdagi sonlar soni oldindan nomaʼlum, shuning uchun satr butunligicha oʻqilib, keyin boʻlinadi (Java'da `split`, C++'da `istringstream`). Python'da `yuza(*sonlar)` roʻyxatni alohida argumentlarga yoyadi: `yuza(*[3.0, 4.0])` — bu `yuza(3.0, 4.0)`.

## Ishlangan misol 2: bir nechta natija qaytarish

Birinchi satrda *n*, ikkinchisida *n* ta butun son. `statistika` funksiyasi massivning eng kichik, eng katta elementi va oʻrta arifmetigini qaytarsin; asosiy dastur ularni bitta satrda chiqarsin (oʻrtacha — ikki kasr xona bilan).

```python
def statistika(a):
    return min(a), max(a), sum(a) / len(a)      # kortej


n = int(input())
a = list(map(int, input().split()))
kichik, katta, ortacha = statistika(a)          # kortejni yoyish
print(f"{kichik} {katta} {ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    record Statistika(int kichik, int katta, double ortacha) {}

    static Statistika statistika(int[] a) {
        int kichik = a[0], katta = a[0];
        long yigindi = 0;
        for (int x : a) {
            kichik = Math.min(kichik, x);
            katta = Math.max(katta, x);
            yigindi += x;
        }
        return new Statistika(kichik, katta, (double) yigindi / a.length);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Statistika s = statistika(a);
        System.out.printf("%d %d %.2f%n", s.kichik(), s.katta(), s.ortacha());
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

// natijalar havola parametrlar orqali qaytadi
void statistika(const vector<int>& a, int& kichik, int& katta, double& ortacha) {
    kichik = katta = a[0];
    long long yigindi = 0;
    for (int x : a) {
        if (x < kichik) kichik = x;
        if (x > katta) katta = x;
        yigindi += x;
    }
    ortacha = (double) yigindi / a.size();
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int kichik, katta;
    double ortacha;
    statistika(a, kichik, katta, ortacha);
    printf("%d %d %.2f\n", kichik, katta, ortacha);
    return 0;
}
```

`5` va `4 -2 9 7 1` kiritilsa, chiqish: `-2 9 3.80`. Uchala tilda ham funksiya **hisoblaydi**, chiqarishni esa asosiy dastur bajaradi. C++'da `const vector<int>& a` — massiv nusxalanmaydi va funksiya uni oʻzgartira olmaydi; `int& kichik` esa aksincha, chaqiruvchining oʻzgaruvchisiga yozadi. Java'dagi `(double) yigindi` — butun boʻlishdan qochish uchun turni oʻzgartirish.

## Ishlangan misol 3: masalani funksiyalarga boʻlish

**Mukammal son** — oʻzidan boshqa boʻluvchilari yigʻindisiga teng son (6 = 1 + 2 + 3). Birinchi satrda *a* va *b*. [*a*, *b*] oraliqdagi barcha mukammal sonlarni bitta satrda chiqaring; bunday son boʻlmasa `-` chiqaring.

```python
def boluvchilar_yigindisi(n):
    """n ning oʻzidan boshqa boʻluvchilari yigʻindisi."""
    if n == 1:
        return 0
    s = 1
    d = 2
    while d * d <= n:
        if n % d == 0:
            s += d
            if d != n // d:
                s += n // d
        d += 1
    return s


def mukammalmi(n):
    return boluvchilar_yigindisi(n) == n


a, b = map(int, input().split())
topilgan = [n for n in range(a, b + 1) if mukammalmi(n)]
print(*topilgan if topilgan else "-")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** n ning oʻzidan boshqa boʻluvchilari yigʻindisi. */
    static long boluvchilarYigindisi(long n) {
        if (n == 1) return 0;
        long s = 1;
        for (long d = 2; d * d <= n; d++) {
            if (n % d == 0) {
                s += d;
                if (d != n / d) s += n / d;
            }
        }
        return s;
    }

    static boolean mukammalmi(long n) {
        return boluvchilarYigindisi(n) == n;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long a = in.nextLong(), b = in.nextLong();
        List<String> topilgan = new ArrayList<>();
        for (long n = a; n <= b; n++) {
            if (mukammalmi(n)) topilgan.add(String.valueOf(n));
        }
        System.out.println(topilgan.isEmpty() ? "-" : String.join(" ", topilgan));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

// n ning oʻzidan boshqa boʻluvchilari yigʻindisi
long long boluvchilarYigindisi(long long n) {
    if (n == 1) return 0;
    long long s = 1;
    for (long long d = 2; d * d <= n; d++) {
        if (n % d == 0) {
            s += d;
            if (d != n / d) s += n / d;
        }
    }
    return s;
}

bool mukammalmi(long long n) {
    return boluvchilarYigindisi(n) == n;
}

int main() {
    long long a, b;
    cin >> a >> b;
    vector<long long> topilgan;
    for (long long n = a; n <= b; n++) {
        if (mukammalmi(n)) topilgan.push_back(n);
    }
    if (topilgan.empty()) cout << "-";
    for (size_t i = 0; i < topilgan.size(); i++) {
        if (i > 0) cout << ' ';
        cout << topilgan[i];
    }
    cout << endl;
    return 0;
}
```

`1 10000` kiritilsa, chiqish: `6 28 496 8128`; `30 400` uchun — `-`. Dastur uch qavatdan iborat: `boluvchilar_yigindisi` faqat hisoblaydi, `mukammalmi` savolga „ha/yoʻq“ javob beradi, asosiy dastur esa oʻqiydi va chiqaradi. Boʻluvchilar √*n* gacha qidiriladi: *d* boʻluvchi boʻlsa, *n* / *d* ham boʻluvchi; `d != n / d` sharti toʻla kvadratlarda (masalan, 16 = 4 · 4) ildizni ikki marta qoʻshmaslik uchun.

:::warning
**Koʻp uchraydigan xatolar**

- **Hisoblovchi funksiya ichida chiqarish.** `def ortacha(a): print(...)` — natijani boshqa hisobda ishlatib boʻlmaydi; `return` qiling.
- **Python'da qayta yuklashga urinish.** Ikkinchi `def yuza(a, b)` birinchi `def yuza(r)` ni oʻchiradi; standart parametrlardan foydalaning.
- **C++'da `&` siz „natija“ parametr.** `void f(int kichik)` nusxani oʻzgartiradi, chaqiruvchining oʻzgaruvchisi oʻzgarmaydi.
- **Python'da oʻzgaruvchan standart qiymat.** `def f(a, royxat=[])` — roʻyxat barcha chaqiruvlar uchun bitta boʻlib qoladi; `None` ishlatib, ichida yangi roʻyxat yarating.
:::

:::tip
Funksiyani yozishdan oldin uning tavsifini va 2–3 ta chaqiruv misolini (kirish → kutilgan natija) yozib qoʻying. Masalan: `mukammalmi(6) → True`, `mukammalmi(8) → False`. Funksiya tayyor boʻlgach, avval shu misollarda tekshiring.
:::

## Xulosa

- Funksiya tavsifi — nomi, parametrlari va qaytaradigan turi; yaxshi funksiya bitta ishni bajaradi.
- Qiymat qaytaruvchi funksiya hisoblaydi, protsedura (`void`) amal bajaradi; hisob va chiqarishni ajrating.
- Bir nechta natija: Python — kortej, Java — `record`, C++ — havola parametrlar yoki `pair`/`struct`.
- Java va C++ funksiyalarni parametrlar soni va turi boʻyicha qayta yuklaydi; Python'da standart parametrlar ishlatiladi.
- Katta masala bir-birini chaqiradigan kichik funksiyalarga boʻlinadi.

## Oʻzingizni tekshiring

:::check
Q: Funksiya tavsifiga nimalar kiradi?
A: Nomi, parametrlari (va ularning turlari) hamda qaytaradigan qiymat turi.
Q: `void` funksiya bilan qiymat qaytaruvchi funksiya chaqiruvining farqi nimada?
A: Qiymat qaytaruvchi funksiya ifoda ichida ishlatiladi (`y = f(x) + 1`), `void` funksiya esa alohida buyruq sifatida chaqiriladi.
Q: Java'da `int f(int x)` va `double f(int x)` ni birga eʼlon qilish mumkinmi?
A: Yoʻq: parametrlar bir xil, faqat qaytaradigan tur bilan farq qiluvchi funksiyalar qayta yuklanmaydi.
Q: C++'da `void f(int& x) { x = 5; }` chaqirilgandan keyin chaqiruvchining oʻzgaruvchisi nimaga teng?
A: 5 ga — `x` havola, u chaqiruvchining oʻzgaruvchisining oʻzi.
Q: Python'da funksiyadan ikkita qiymatni qanday qaytarish mumkin?
A: Kortej sifatida: `return a, b`, chaqiruvda `x, y = f()`.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#default-argument-values
Python: standart argument qiymatlari
Standart parametrlar va `*args` haqida rasmiy qoʻllanma (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/language/overload_resolution
C++: qayta yuklashni tanlash
Kompilyator qayta yuklangan funksiyalardan qaysi birini tanlashi haqida (ingliz tilida).
:::
