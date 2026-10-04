---
title: Funksiyalar bilan ishlash
summary: Funksiya nima va nima uchun kerak, eʼlon qilish va chaqirish, parametr va argument, qaytariladigan qiymat, void funksiyalar, qiymat boʻyicha uzatish, lokal va global oʻzgaruvchilar, C++ prototipi.
---

## Nima uchun bu mavzu muhim?

Dekanat dasturida talabaning oʻrtacha bali besh joyda hisoblanadi: reytingda, stipendiyada, transkriptda, hisobotda va ota-onaga xabarda. Agar hisoblash kodi besh marta nusxalangan boʻlsa, formulani oʻzgartirish kerak boʻlganda beshta joyni topish va bir xil tuzatish kerak. Bittasi esdan chiqsa — turli sahifalarda turli ball.

**Funksiya** bu muammoni hal qiladi: kod bir marta yoziladi, unga nom beriladi va kerakli joyda nomi bilan chaqiriladi. Funksiyalar katta dasturni kichik, tushunarli boʻlaklarga ajratadi: har bir boʻlak bitta ishni bajaradi va alohida tekshiriladi. Siz allaqachon funksiyalardan foydalangansiz: `print`, `sqrt`, `max` — hammasi funksiya.

:::note
**Bu darsdan soʻng siz:**

- funksiyani eʼlon qilib, uni chaqira olasiz va parametr bilan argumentni farqlay olasiz;
- qiymat qaytaruvchi va qaytarmaydigan (`void`) funksiyalarni yoza olasiz;
- qiymat boʻyicha uzatish nimani anglatishini va massiv uzatilganda nima boʻlishini tushuntira olasiz;
- lokal va global oʻzgaruvchilarni farqlay olasiz;
- masalani bir nechta kichik funksiyaga ajratib yecha olasiz.
:::

## Funksiyani eʼlon qilish

**Funksiya** — nomlangan, qayta ishlatiladigan kod boʻlagi. U kirish maʼlumotlarini (**parametrlar**) oladi, ish bajaradi va natija (**qaytariladigan qiymat**) beradi.

| Qism | Python | Java | C++ |
|---|---|---|---|
| Eʼlon qilish | `def kvadrat(x):` | `static int kvadrat(int x) {` | `int kvadrat(int x) {` |
| Natija qaytarish | `return x * x` | `return x * x;` | `return x * x;` |
| Chaqirish | `y = kvadrat(5)` | `int y = kvadrat(5);` | `int y = kvadrat(5);` |

Java va C++'da funksiya sarlavhasida **qaytariladigan tur** (`int`) va har bir parametrning turi yoziladi. Python'da turlar yozilmaydi (xohlasa, izoh sifatida: `def kvadrat(x: int) -> int:`).

Java'da funksiya (metod) albatta klass ichida turadi; `main` dan chaqiriladigan oddiy funksiyalar `static` deb yoziladi.

## Parametr va argument

- **Parametr** — funksiya eʼlonidagi nom: `def kvadrat(x)` da *x*.
- **Argument** — chaqiruvda beriladigan aniq qiymat: `kvadrat(5)` da 5.

Chaqiruvda argumentlar parametrlarga **tartib boʻyicha** beriladi: `daraja(2, 10)` da birinchi argument birinchi parametrga, ikkinchisi ikkinchisiga. Argumentlar soni va turi parametrlarga mos kelishi kerak.

## return va void

`return` ikki ishni bajaradi: natijani qaytaradi va funksiyani **darhol tugatadi**. `return` dan keyingi kod bajarilmaydi.

Hech narsa qaytarmaydigan funksiya ham boʻladi — u faqat amal bajaradi (masalan, chiqaradi):

| Til | Qiymat qaytarmaydigan funksiya |
|---|---|
| Python | `return` yoʻq (aslida `None` qaytaradi) |
| Java, C++ | qaytariladigan tur — `void` |

Yaxshi odat: hisoblovchi funksiya natijani **qaytarsin**, chiqarmasin. Shunda uni istalgan joyda ishlatish mumkin: chiqarish, solishtirish, boshqa hisobga qoʻshish.

## Qiymat boʻyicha uzatish

Java, C++ va Python'da oddiy turlar (son, mantiqiy qiymat) funksiyaga **qiymat boʻyicha (by value)** uzatiladi: parametr argumentning **nusxasini** oladi. Funksiya ichida parametrni oʻzgartirish chaqiruvchidagi oʻzgaruvchiga taʼsir qilmaydi.

Massivlar boshqacha:

| Til | Massiv uzatilganda | Funksiya massiv elementini oʻzgartirsa |
|---|---|---|
| Python | roʻyxatga havola uzatiladi | asl roʻyxat oʻzgaradi |
| Java | massivga havola uzatiladi | asl massiv oʻzgaradi |
| C++ (`vector<int> a`) | butun vektor **nusxalanadi** | asl vektor oʻzgarmaydi |
| C++ (`vector<int>& a`) | havola (`&`) uzatiladi | asl vektor oʻzgaradi |

C++'da `&` belgisi **havola boʻyicha uzatish** (by reference): parametr argumentning oʻzi bilan bitta oʻzgaruvchi boʻladi. Bu mavzu M9 darsida (koʻrsatkichlar) chuqurroq koʻriladi.

## Lokal va global oʻzgaruvchilar

- **Lokal oʻzgaruvchi** — funksiya ichida eʼlon qilingan; faqat shu funksiya ichida mavjud va funksiya tugaganda yoʻqoladi. Parametrlar ham lokal.
- **Global oʻzgaruvchi** — barcha funksiyalardan tashqarida eʼlon qilingan; hamma joyda koʻrinadi.

Turli funksiyalardagi bir xil nomli lokal oʻzgaruvchilar — **turli** oʻzgaruvchilar. Global oʻzgaruvchilardan iloji boricha qoching: ularni istalgan funksiya oʻzgartirishi mumkin va xatoni topish qiyinlashadi. Funksiya kerakli hamma narsani parametr orqali olsin va natijani `return` orqali bersin.

## C++: prototip

C++ kompilyatori faylni yuqoridan pastga oʻqiydi: funksiya chaqirilishidan **oldin** eʼlon qilingan boʻlishi kerak. Ikki yoʻl bor: funksiyani `main` dan oldin yozish yoki `main` dan oldin faqat **prototipni** (sarlavha va nuqtali vergul) yozib, tanani keyinroq berish:

```text
bool tubmi(int n);        // prototip
int main() { ... tubmi(7) ... }
bool tubmi(int n) { ... } // taʼrif
```

Python va Java'da bu muammo yoʻq: Python funksiyani chaqiruv paytida qidiradi, Java esa butun klassni oldindan koʻradi.

## Ishlangan misol 1: tub sonlar roʻyxati

Natural son *n* kiritiladi. 2 dan *n* gacha barcha tub sonlarni bir qatorda chiqaring. Tublikni tekshirishni alohida funksiyaga ajrating.

```python
def tubmi(n):
    if n < 2:
        return False
    i = 2
    while i * i <= n:
        if n % i == 0:
            return False
        i += 1
    return True


n = int(input())
tublar = []
for k in range(2, n + 1):
    if tubmi(k):
        tublar.append(k)
print(*tublar)
```
```java
import java.util.Scanner;

public class Main {
    static boolean tubmi(int n) {
        if (n < 2) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        StringBuilder sb = new StringBuilder();
        for (int k = 2; k <= n; k++) {
            if (tubmi(k)) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(k);
            }
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

bool tubmi(int n);                 // prototip

int main() {
    int n;
    cin >> n;

    bool birinchi = true;
    for (int k = 2; k <= n; k++) {
        if (tubmi(k)) {
            if (!birinchi) cout << ' ';
            cout << k;
            birinchi = false;
        }
    }
    cout << endl;
    return 0;
}

bool tubmi(int n) {
    if (n < 2) {
        return false;
    }
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) {
            return false;
        }
    }
    return true;
}
```

Kirish `30` boʻlsa, chiqadi: `2 3 5 7 11 13 17 19 23 29`.

1. `main` 2 dan 30 gacha har bir *k* uchun `tubmi(k)` ni chaqiradi.
2. `tubmi` ichida *n* — parametr; u har chaqiruvda yangi qiymat (*k* ning nusxasini) oladi.
3. Boʻluvchi topilishi bilan `return False` funksiyani darhol tugatadi — bayroq va `break` kerak emas.

A7 mashgʻulotidagi kod bilan solishtiring: oʻsha tekshiruv endi nomga ega va qayta ishlatiladi. `main` „nima qilinadi“ni, `tubmi` esa „qanday qilinadi“ni aytadi.

## Ishlangan misol 2: EKUB va EKUK

Ikki natural son kiritiladi. Ularning EKUB va EKUK ini chiqaring. EKUK(*a*, *b*) = *a* · *b* / EKUB(*a*, *b*).

```python
def ekub(a, b):
    while b != 0:
        a, b = b, a % b
    return a


def ekuk(a, b):
    return a // ekub(a, b) * b


x, y = map(int, input().split())
print("EKUB:", ekub(x, y))
print("EKUK:", ekuk(x, y))
```
```java
import java.util.Scanner;

public class Main {
    static long ekub(long a, long b) {
        while (b != 0) {
            long q = a % b;
            a = b;
            b = q;
        }
        return a;
    }

    static long ekuk(long a, long b) {
        return a / ekub(a, b) * b;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long x = in.nextLong();
        long y = in.nextLong();
        System.out.println("EKUB: " + ekub(x, y));
        System.out.println("EKUK: " + ekuk(x, y));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long ekub(long long a, long long b) {
    while (b != 0) {
        long long q = a % b;
        a = b;
        b = q;
    }
    return a;
}

long long ekuk(long long a, long long b) {
    return a / ekub(a, b) * b;
}

int main() {
    long long x, y;
    cin >> x >> y;
    cout << "EKUB: " << ekub(x, y) << endl;
    cout << "EKUK: " << ekuk(x, y) << endl;
    return 0;
}
```

Kirish `12 18` boʻlsa: EKUB = 6, EKUK = 12 / 6 · 18 = 36. Chiqadi: `EKUB: 6` va `EKUK: 36`.

Eʼtibor bering:

- `ekuk` funksiyasi `ekub` ni **chaqiradi** — funksiyalar bir-biridan foydalanadi.
- `ekub` ichida *a* va *b* oʻzgartiriladi, lekin `main` dagi *x* va *y* oʻzgarmaydi: parametrlar — nusxalar.
- `a / ekub(a, b) * b` — avval boʻlib, keyin koʻpaytiramiz: `a * b` katta sonlarda toʻlib ketishi mumkin, oraliq natija esa kichikroq.

:::warning
**Koʻp uchraydigan xatolar**

- **`return` oʻrniga `print`.** Funksiya natijani chiqaradi, lekin qaytarmaydi: `y = f(5)` da *y* — `None` (Python) yoki kompilyatsiya xatosi (Java, C++).
- **Barcha yoʻllarda `return` yoʻq.** Java'da `missing return statement` xatosi; C++'da xato yoʻq, lekin natija aniqlanmagan.
- **Parametrni oʻzgartirib, chaqiruvchida natija kutish.** Oddiy turlar nusxa boʻyicha uzatiladi; natijani `return` bilan qaytaring.
- **C++'da prototipsiz chaqiruv.** Funksiya `main` dan keyin yozilgan boʻlsa: `'tubmi' was not declared in this scope`.
- **Java'da `static` ni unutish.** `main` dan `non-static method cannot be referenced from a static context` xatosi.
:::

:::tip
Funksiyaga nom berishda feʼl yoki savol ishlating: `hisobla_ortacha`, `tubmi`, `chiqar_jadval`. Agar funksiyani bitta gap bilan tushuntirib boʻlmasa — u juda koʻp ish qilyapti, uni ikkiga boʻling.
:::

## Xulosa

- Funksiya — nomlangan, qayta ishlatiladigan kod boʻlagi: parametrlarni oladi, `return` bilan natija qaytaradi.
- Parametr — eʼlondagi nom, argument — chaqiruvdagi qiymat; ular tartib boʻyicha mos keladi.
- `return` natijani qaytaradi va funksiyani tugatadi; natija qaytarmaydigan funksiya — `void` (Python'da `return` siz).
- Oddiy turlar qiymat boʻyicha uzatiladi; Python va Java'da massiv havola orqali, C++'da `vector` nusxalanadi (havola uchun `&`).
- Lokal oʻzgaruvchilar faqat funksiya ichida yashaydi; global oʻzgaruvchilardan qoching.
- C++'da funksiya chaqiruvdan oldin eʼlon qilinishi (yoki prototipi yozilishi) kerak.

## Oʻzingizni tekshiring

:::check
Q: `def f(a, b): return a - b` va `f(10, 3)` chaqiruvida parametrlar va argumentlar qaysilar?
A: Parametrlar — *a* va *b*; argumentlar — 10 va 3. Natija 7.
Q: Python'da `def f(x): x = x + 1` va `n = 5; f(n); print(n)` nima chiqaradi?
A: 5: *x* — *n* ning nusxasi (aniqrogʻi, yangi qiymat faqat lokal *x* ga beriladi).
Q: C++'da `void tozala(vector<int> a) { a[0] = 0; }` chaqirilgandan keyin asl vektorning birinchi elementi oʻzgaradimi?
A: Yoʻq: vektor nusxalanadi. Oʻzgarishi uchun parametr `vector<int>& a` boʻlishi kerak.
Q: `return` dan keyin yozilgan qator bajariladimi?
A: Yoʻq: `return` funksiyani darhol tugatadi.
Q: Nima uchun hisoblovchi funksiya natijani chiqarmasdan qaytargani maʼqul?
A: Qaytarilgan natijani istalgan joyda ishlatish mumkin: chiqarish, solishtirish, boshqa hisoblarga qoʻshish; chiqaruvchi funksiyani esa faqat chiqarish uchun ishlatsa boʻladi.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#defining-functions
Python: funksiyalarni eʼlon qilish
Rasmiy qoʻllanmadagi `def` va `return` boʻlimi.
:::

:::link https://en.cppreference.com/w/cpp/language/function
cppreference: funksiya eʼloni
C++'da funksiya eʼloni va taʼrifi.
:::
