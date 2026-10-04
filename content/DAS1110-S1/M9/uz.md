---
title: Dinamik xotira bilan ishlash
summary: Stek va uyum (heap), manzil va koʻrsatkich, & va * amallari, havola, new va delete, dinamik massiv, xotira sizib chiqishi; Java va Python'da avtomatik xotira boshqaruvi (axlat yigʻuvchi).
---

## Nima uchun bu mavzu muhim?

Dastur yozilayotganda guruhda nechta talaba boʻlishi, foydalanuvchi nechta rasm yuklashi, fayl qancha qatordan iborat boʻlishi nomaʼlum. Statik massiv `int a[1000];` bilan biz „eng yomon holat“ni taxmin qilamiz: kichik maʼlumotda xotira isrof boʻladi, katta maʼlumotda esa joy yetmaydi.

**Dinamik xotira** muammoni hal qiladi: dastur ishlayotgan paytda aynan kerakli hajmda xotira soʻraladi va kerak boʻlmay qolganda qaytariladi. Python roʻyxati va C++ `vector` i ichida aynan shu mexanizm ishlaydi. Uni tushunish xotira bilan bogʻliq eng ogʻir xatolarning — sizib chiqish, osilib qolgan koʻrsatkich, chegaradan chiqish — sababini koʻrishga yordam beradi.

:::note
**Bu darsdan soʻng siz:**

- stek va uyum (heap) xotirasining farqini tushuntira olasiz;
- C++'da koʻrsatkich eʼlon qilib, `&` va `*` amallaridan foydalana olasiz;
- `new` / `delete` bilan dinamik massiv yaratib, uni toʻgʻri boʻshata olasiz;
- xotira sizib chiqishi va osilib qolgan koʻrsatkich xatolarini taniy olasiz;
- Java va Python'da xotira qanday avtomatik boshqarilishini tushuntira olasiz.
:::

## Stek va uyum

Dastur xotirasining ikki asosiy qismi:

| | Stek (stack) | Uyum (heap) |
|---|---|---|
| Nima saqlanadi | lokal oʻzgaruvchilar, parametrlar, chaqiruv freymlari | dinamik ajratilgan maʼlumot |
| Kim boshqaradi | avtomatik: funksiya tugaganda boʻshaydi | C++'da dasturchi (`new` / `delete`); Java, Python'da axlat yigʻuvchi |
| Hajmi | kichik (odatda 1–8 MB) | katta (operativ xotira hajmicha) |
| Tezligi | juda tez | sekinroq |
| Oʻlcham qachon maʼlum | kompilyatsiyada | dastur ishlayotganda |

Shu sababli katta massivni stekda eʼlon qilish (`int a[10000000];` funksiya ichida) stekni toʻldirib yuborishi mumkin; uyumda esa bunday massiv bemalol joylashadi.

## Manzil va koʻrsatkich (C++)

Xotiradagi har bir bayt oʻz **manziliga** ega — tartib raqamiga. Oʻzgaruvchi xotirada qayerdadir joylashadi va uning manzili bor.

**Koʻrsatkich (pointer)** — boshqa oʻzgaruvchining manzilini saqlaydigan oʻzgaruvchi.

| Yozuv | Maʼnosi |
|---|---|
| `int* p;` | *p* — `int` ga koʻrsatkich |
| `p = &x;` | `&x` — *x* ning manzili; *p* endi *x* ni koʻrsatadi |
| `*p` | *p* koʻrsatgan joydagi qiymat („nomlashdan chiqarish“, dereference) |
| `*p = 10;` | *x* ga 10 yoziladi — *p* orqali |
| `nullptr` | hech narsani koʻrsatmaydigan koʻrsatkich |

`&` va `*` amallari bir-biriga teskari: `*(&x)` — bu *x* ning oʻzi.

**Havola (reference)** `int& r = x;` — *x* ning ikkinchi nomi. Koʻrsatkichdan farqi: havola albatta biror oʻzgaruvchiga bogʻlanadi, keyin boshqasiga qayta bogʻlanmaydi va `*` siz ishlatiladi. Funksiya parametrlari uchun havola qulayroq (M7 dagi `vector<int>& a`).

## new va delete

C++'da uyumdan xotira `new` bilan olinadi va `delete` bilan qaytariladi:

| Vazifa | Yozuv |
|---|---|
| Bitta `int` | `int* p = new int(5);` … `delete p;` |
| *n* ta `int` dan massiv | `int* a = new int[n];` … `delete[] a;` |

Qoida: har bir `new` ga bitta `delete`, har bir `new[]` ga bitta `delete[]`.

Dinamik xotira bilan ishlashdagi uch xavfli xato:

| Xato | Nima boʻladi | Natija |
|---|---|---|
| **Xotira sizib chiqishi (memory leak)** | `delete` unutildi | dastur ishlagan sari koʻproq xotira egallaydi |
| **Osilib qolgan koʻrsatkich (dangling pointer)** | `delete` dan keyin koʻrsatkich ishlatildi | aniqlanmagan xatti-harakat, tasodifiy qulashlar |
| **Ikki marta boʻshatish (double free)** | bitta xotira ikki marta `delete` qilindi | dastur qulaydi |

Shuning uchun zamonaviy C++'da qoʻlda `new` / `delete` oʻrniga koʻpincha `vector` va „aqlli koʻrsatkichlar“ (`unique_ptr`) ishlatiladi — ular xotirani avtomatik qaytaradi. Lekin ularning ichida aynan shu mexanizm ishlaydi.

## Java va Python: axlat yigʻuvchi

Java va Python'da dasturchi xotirani qoʻlda boʻshatmaydi:

- **Java**: `new int[n]` obyektni uyumda yaratadi; oʻzgaruvchi obyektga **havola** saqlaydi. Obyektga birorta ham havola qolmasa, **axlat yigʻuvchi (garbage collector)** uni avtomatik oʻchiradi.
- **Python**: barcha qiymatlar — uyumdagi obyektlar, oʻzgaruvchilar — ularga havolalar. Havolalar soni nolga tushganda obyekt oʻchiriladi.

Bu sizib chiqish va osilib qolgan koʻrsatkich xatolarining aksariyatini yoʻqotadi. Evaziga dasturchi xotira qachon boʻshashini aniq boshqarmaydi va axlat yigʻuvchi biroz vaqt va xotira oladi. Java va Python'da koʻrsatkich arifmetikasi yoʻq: manzil bilan toʻgʻridan-toʻgʻri ishlab boʻlmaydi.

## Ishlangan misol 1: qiymatlarni funksiya orqali almashtirish

Ikki butun son kiritiladi. Ularni **funksiya** yordamida almashtiring va chiqaring. Har bir til buni oʻz usulida qiladi.

```python
def almashtir(a, b):
    # Python'da int oʻzgarmas obyekt: funksiya yangi juftlikni qaytaradi
    return b, a


x, y = map(int, input().split())
x, y = almashtir(x, y)
print(x, y)
```
```java
import java.util.Scanner;

public class Main {
    // Java'da int qiymat boʻyicha uzatiladi; massiv esa havola orqali
    static void almashtir(int[] juft) {
        int t = juft[0];
        juft[0] = juft[1];
        juft[1] = t;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] juft = {in.nextInt(), in.nextInt()};
        almashtir(juft);
        System.out.println(juft[0] + " " + juft[1]);
    }
}
```
```cpp
#include <iostream>
using namespace std;

// Koʻrsatkichlar orqali: funksiya asl oʻzgaruvchilarni oʻzgartiradi
void almashtir(int* a, int* b) {
    int t = *a;
    *a = *b;
    *b = t;
}

int main() {
    int x, y;
    cin >> x >> y;
    almashtir(&x, &y);           // manzillar uzatiladi
    cout << x << " " << y << endl;
    return 0;
}
```

Kirish `3 8` boʻlsa, uchala dastur ham `8 3` chiqaradi.

- **C++**: `almashtir(&x, &y)` *x* va *y* ning manzillarini uzatadi. Funksiya ichida `*a` — bu *x* ning oʻzi, shuning uchun almashtirish asl oʻzgaruvchilarda boʻladi. Havola bilan ham yozish mumkin: `void almashtir(int& a, int& b)` va chaqiruv `almashtir(x, y)`.
- **Java**: `int` qiymat boʻyicha uzatiladi, shuning uchun ikki sonni massivga solib, massivga havolani uzatamiz.
- **Python**: funksiya yangi qiymatlarni qaytaradi; Python'da sonlarni joyida oʻzgartirib boʻlmaydi.

## Ishlangan misol 2: dinamik massiv

Birinchi qatorda *n*, ikkinchisida *n* ta son. Faqat juft sonlardan iborat yangi massiv yarating — oʻlchami aynan juftlar soniga teng — va uni chiqaring.

```python
n = int(input())
a = list(map(int, input().split()))

juftlar = [x for x in a if x % 2 == 0]   # roʻyxat kerakli hajmda yaratiladi

print(len(juftlar))
print(*juftlar)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];             // uyumda, oʻlchami ish paytida maʼlum
        int soni = 0;
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
            if (a[i] % 2 == 0) soni++;
        }

        int[] juftlar = new int[soni];    // aynan kerakli oʻlcham
        int k = 0;
        for (int x : a) {
            if (x % 2 == 0) juftlar[k++] = x;
        }

        System.out.println(soni);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < soni; i++) {
            if (i > 0) sb.append(' ');
            sb.append(juftlar[i]);
        }
        System.out.println(sb);
        // delete kerak emas: axlat yigʻuvchi oʻzi tozalaydi
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    int* a = new int[n];              // uyumda n ta int
    int soni = 0;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
        if (a[i] % 2 == 0) soni++;
    }

    int* juftlar = new int[soni];     // aynan kerakli oʻlcham
    int k = 0;
    for (int i = 0; i < n; i++) {
        if (a[i] % 2 == 0) juftlar[k++] = a[i];
    }

    cout << soni << endl;
    for (int i = 0; i < soni; i++) {
        if (i > 0) cout << ' ';
        cout << juftlar[i];
    }
    cout << endl;

    delete[] a;                       // har bir new[] ga — delete[]
    delete[] juftlar;
    return 0;
}
```

Kirish `7` va `3 8 5 12 7 6 1` boʻlsa: juftlar — 8, 12, 6. Chiqadi: `3` va `8 12 6`.

C++ versiyasida ikki marta `new[]` va ikki marta `delete[]`. Agar `delete[]` unutilsa, bu kichik dasturda hech narsa sezilmaydi (dastur tugaganda operatsion tizim xotirani qaytaradi), lekin kuniga million marta chaqiriladigan funksiyada — sizib chiqish serverni toʻxtatib qoʻyadi. `juftlar[k++] = x` — avval `juftlar[k]` ga yoziladi, keyin *k* bittaga oshadi.

## Ishlangan misol 3: oʻsuvchi massiv

Sonlar ketma-ket kiritiladi, 0 bilan tugaydi. Ularni massivda saqlang: massiv sigʻimi 2 dan boshlanadi va toʻlganda **ikki baravar** kattalashtiriladi. Oxirida sonlar soni, sigʻim va sonlarni teskari tartibda chiqaring.

`vector` va Python roʻyxati ichida aynan shunday ishlaydi.

```python
sigim = 2
massiv = [0] * sigim
soni = 0

x = int(input())
while x != 0:
    if soni == sigim:                  # joy tugadi: ikki baravar kattaroq massiv
        sigim *= 2
        yangi = [0] * sigim
        for i in range(soni):
            yangi[i] = massiv[i]
        massiv = yangi                 # eski roʻyxatni axlat yigʻuvchi oʻchiradi
    massiv[soni] = x
    soni += 1
    x = int(input())

print(soni, sigim)
print(*[massiv[i] for i in range(soni - 1, -1, -1)])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int sigim = 2;
        int[] massiv = new int[sigim];
        int soni = 0;

        int x = in.nextInt();
        while (x != 0) {
            if (soni == sigim) {           // joy tugadi: ikki baravar kattaroq massiv
                sigim *= 2;
                int[] yangi = new int[sigim];
                for (int i = 0; i < soni; i++) {
                    yangi[i] = massiv[i];
                }
                massiv = yangi;            // eski massivni axlat yigʻuvchi oʻchiradi
            }
            massiv[soni] = x;
            soni++;
            x = in.nextInt();
        }

        System.out.println(soni + " " + sigim);
        StringBuilder sb = new StringBuilder();
        for (int i = soni - 1; i >= 0; i--) {
            if (i < soni - 1) sb.append(' ');
            sb.append(massiv[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int sigim = 2;
    int* massiv = new int[sigim];
    int soni = 0;

    int x;
    cin >> x;
    while (x != 0) {
        if (soni == sigim) {           // joy tugadi: ikki baravar kattaroq massiv
            sigim *= 2;
            int* yangi = new int[sigim];
            for (int i = 0; i < soni; i++) {
                yangi[i] = massiv[i];
            }
            delete[] massiv;           // eskisini qoʻlda boʻshatamiz
            massiv = yangi;
        }
        massiv[soni] = x;
        soni++;
        cin >> x;
    }

    cout << soni << " " << sigim << endl;
    for (int i = soni - 1; i >= 0; i--) {
        if (i < soni - 1) cout << ' ';
        cout << massiv[i];
    }
    cout << endl;

    delete[] massiv;
    return 0;
}
```

Kirish `4`, `9`, `2`, `7`, `5`, `0` boʻlsa (har biri alohida qatorda):

| Kiritildi | *soni* (oldin) | *sigim* | Nima boʻldi |
|---|---|---|---|
| 4 | 0 | 2 | yozildi |
| 9 | 1 | 2 | yozildi |
| 2 | 2 | 2 → 4 | massiv toʻla: yangi massiv, 2 ta element koʻchirildi |
| 7 | 3 | 4 | yozildi |
| 5 | 4 | 4 → 8 | massiv toʻla: yangi massiv, 4 ta element koʻchirildi |

Chiqadi: `5 8` va `5 7 2 9 4`.

Nima uchun ikki baravar? Har safar bittaga kattalashtirilsa, har bir qoʻshishda butun massiv koʻchiriladi — *n* ta son uchun ~*n*²/2 koʻchirish. Ikki baravar kattalashtirishda jami koʻchirishlar soni 2*n* dan oshmaydi. C++ versiyasida `delete[] massiv` eski massivni boʻshatadi — busiz har bir kattalashtirishda xotira sizib chiqardi.

:::warning
**Koʻp uchraydigan xatolar**

- **`new[]` ni `delete` (qavssiz) bilan boʻshatish.** Massiv uchun albatta `delete[]`.
- **`delete` dan keyin koʻrsatkichni ishlatish.** Boʻshatilgan xotira endi sizniki emas; koʻrsatkichga `nullptr` bering.
- **Funksiyadan lokal oʻzgaruvchining manzilini qaytarish.** Funksiya tugaganda lokal oʻzgaruvchi stekdan yoʻqoladi — koʻrsatkich osilib qoladi.
- **Initsializatsiyalanmagan koʻrsatkich.** `int* p; *p = 5;` — tasodifiy manzilga yozish.
- **Java'da massivni nusxalash deb `b = a` yozish.** Bu nusxa emas, ikkinchi havola: `b[0] = 1` *a* ni ham oʻzgartiradi.
:::

:::tip
C++'da `new` yozgan zahotiyoq tegishli `delete` ni qayerda yozishingizni hal qiling. Agar buni aniq aytib boʻlmasa — `vector` ishlating: u xotirani oʻzi boshqaradi va chegaradan chiqishni `at()` bilan tekshira oladi.
:::

## Xulosa

- Stek — lokal oʻzgaruvchilar uchun kichik, avtomatik xotira; uyum — dinamik maʼlumot uchun katta xotira.
- Koʻrsatkich manzil saqlaydi: `&x` — manzil, `*p` — manzildagi qiymat; havola — oʻzgaruvchining ikkinchi nomi.
- C++'da `new` / `new[]` uyumdan xotira oladi, `delete` / `delete[]` qaytaradi; har biriga juft boʻlishi shart.
- Sizib chiqish, osilib qolgan koʻrsatkich va ikki marta boʻshatish — dinamik xotiraning asosiy xatolari.
- Java va Python'da xotirani axlat yigʻuvchi qaytaradi; oʻzgaruvchilar obyektlarga havola saqlaydi.
- Oʻsuvchi massiv toʻlganda ikki baravar kattalashtiriladi — `vector` va Python roʻyxati shunday ishlaydi.

## Oʻzingizni tekshiring

:::check
Q: `int x = 7; int* p = &x; *p = 20;` dan keyin *x* nimaga teng?
A: 20: *p* *x* ni koʻrsatadi, `*p = 20` *x* ga yozadi.
Q: `int* a = new int[100];` bilan ajratilgan xotira qanday boʻshatiladi?
A: `delete[] a;`.
Q: Xotira sizib chiqishi nima?
A: Ajratilgan dinamik xotira boʻshatilmay qolishi: dastur ishlagan sari egallagan xotirasi oshib boradi.
Q: Java'da nima uchun `delete` yoʻq?
A: Hech qanday havola qolmagan obyektlarni axlat yigʻuvchi avtomatik oʻchiradi.
Q: 3-misolda 9 ta son kiritilsa (0 dan oldin), oxirgi sigʻim qancha boʻladi?
A: 16: 2 → 4 → 8 → 16 (9-son kelganda 8 ta joy toʻla).
:::

:::link https://en.cppreference.com/w/cpp/language/new
cppreference: new ifodasi
C++'da dinamik xotira ajratish.
:::

:::link https://en.cppreference.com/w/cpp/memory/unique_ptr
cppreference: std::unique_ptr
Xotirani avtomatik qaytaruvchi „aqlli koʻrsatkich“.
:::
