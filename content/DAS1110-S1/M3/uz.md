---
title: Tarmoqlanuvchi jarayonlarni tashkil etish
summary: Chiziqli va tarmoqlanuvchi algoritmlar, if, if-else, else-if zanjiri, ichma-ich shartlar, bloklar, shartni blok-sxemada tasvirlash.
---

## Nima uchun bu mavzu muhim?

Shu paytgacha yozgan dasturlarimiz har safar bir xil qadamlarni bajarardi: oʻqi, hisobla, chiqar. Hayotda esa qaror qabul qilishga toʻgʻri keladi. Talabaning bali 56 dan past boʻlsa — qayta topshiradi, aks holda — keyingi kursga oʻtadi. Kartada pul yetarli boʻlsa — toʻlov oʻtadi, yetmasa — rad etiladi. Navigator tirbandlik boʻlsa — boshqa yoʻlni tanlaydi.

Dastur sharoitga qarab turli yoʻllardan borishi uchun **tarmoqlanish** kerak. Bu mavzu dasturlashning eng asosiy gʻoyalaridan biri: undan keyin dasturlaringiz „oʻylay“ boshlaydi.

:::note
**Bu darsdan soʻng siz:**

- chiziqli va tarmoqlanuvchi algoritmni farqlay olasiz va tarmoqlanishni blok-sxemada tasvirlay olasiz;
- `if`, `if-else` va `else-if` zanjirini uch tilda yoza olasiz;
- bir nechta oraliqqa boʻlingan masalani (baholash, tariflar) toʻgʻri tartibdagi shartlar bilan yecha olasiz;
- ichma-ich shartlarni oʻqiy va mantiqiy amallar bilan soddalashtira olasiz.
:::

## Chiziqli va tarmoqlanuvchi algoritmlar

**Chiziqli algoritm** — qadamlari har doim bir xil tartibda, hech bir qadam tashlab ketilmasdan bajariladigan algoritm. Oldingi darslardagi barcha dasturlar chiziqli edi.

**Tarmoqlanuvchi algoritm** — biror **shart**ga qarab ikki yoki undan ortiq yoʻldan biri tanlanadigan algoritm. Shart — qiymati rost yoki yolgʻon boʻlgan mantiqiy ifoda: `ball >= 56`, `balans >= narx`, `x % 2 == 0`.

Blok-sxemada shart **romb** bilan tasvirlanadi. Rombdan ikkita chiziq chiqadi: „ha“ (rost) va „yoʻq“ (yolgʻon). Ikki tarmoq keyin yana bitta nuqtada birlashadi va algoritm davom etadi.

## if operatori

Eng sodda tarmoqlanish — **`if` (agar)**: shart rost boʻlsa, blok bajariladi; yolgʻon boʻlsa, tashlab ketiladi.

| Til | Yozilishi |
|---|---|
| Python | `if shart:` va keyingi qatorlar **chekinish** (4 probel) bilan |
| Java, C++ | `if (shart) { ... }` — shart qavsda, blok jingalak qavsda |

Pythonʼda blok chekinish bilan belgilanadi: bir xil chekinishli qatorlar bitta blok. Java va C++'da blok `{` va `}` orasidagi kod; chekinish faqat oʻqish uchun, lekin uni ham toʻgʻri qoʻyish kerak.

## if-else: ikki yoʻl

**`if-else` (agar … aks holda)** — shart rost boʻlsa birinchi blok, yolgʻon boʻlsa ikkinchi blok bajariladi. Ikkalasidan biri, albatta, bajariladi, ikkalasi birga hech qachon.

Masalan, „son juftmi yoki toqmi“: `if x % 2 == 0` — juft, `else` — toq.

## else-if zanjiri: koʻp yoʻl

Yoʻllar ikkitadan koʻp boʻlsa, shartlar zanjirga ulanadi: Pythonʼda `elif`, Java va C++'da `else if`. Shartlar **yuqoridan pastga** tekshiriladi; birinchi rost shartning bloki bajariladi va qolganlari tekshirilmaydi. Hech biri rost boʻlmasa, oxirgi `else` bajariladi (agar u boʻlsa).

Shu sababli shartlarning **tartibi** muhim. AIFUʼda 100 ballik tizimdan 5 ballik bahoga oʻtkazish:

| Ball | Baho |
|---|---|
| 86–100 | 5 (aʼlo) |
| 71–85 | 4 (yaxshi) |
| 56–70 | 3 (qoniqarli) |
| 0–55 | 2 (qoniqarsiz) |

Agar shartlarni kattadan kichikka qarab yozsak, har bir shartda faqat quyi chegarani tekshirish kifoya: ikkinchi shartga kelinganda ball 86 dan kichik ekani allaqachon maʼlum.

## Ichma-ich shartlar

Bir shartning ichida boshqa shart boʻlishi mumkin — bu **ichma-ich (nested) shart**. Masalan, avval „talaba toʻlov qilganmi?“, keyin „qarzi bormi?“ tekshiriladi.

Ichma-ich shartlarni koʻpincha mantiqiy amallar bilan soddalashtirsa boʻladi:

| Ichma-ich | Soddalashtirilgan |
|---|---|
| `if a > 0:` ichida `if b > 0:` | `if a > 0 and b > 0:` |
| `if x >= 10:` ichida `if x <= 20:` | `if 10 <= x <= 20:` (Python) yoki `x >= 10 && x <= 20` |

Qoida: chuqurligi uchtadan oshgan shartlar oʻqishni qiyinlashtiradi — ularni soddalashtiring yoki zanjirga aylantiring.

## Ishlangan misol 1: ballni bahoga aylantirish

Butun son — talabaning bali (0–100) kiritiladi. Bahoni chiqaring.

```python
ball = int(input())

if ball >= 86:
    baho = 5
elif ball >= 71:
    baho = 4
elif ball >= 56:
    baho = 3
else:
    baho = 2

print("Baho:", baho)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int ball = in.nextInt();

        int baho;
        if (ball >= 86) {
            baho = 5;
        } else if (ball >= 71) {
            baho = 4;
        } else if (ball >= 56) {
            baho = 3;
        } else {
            baho = 2;
        }

        System.out.println("Baho: " + baho);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int ball;
    cin >> ball;

    int baho;
    if (ball >= 86) {
        baho = 5;
    } else if (ball >= 71) {
        baho = 4;
    } else if (ball >= 56) {
        baho = 3;
    } else {
        baho = 2;
    }

    cout << "Baho: " << baho << endl;
    return 0;
}
```

Kirish `78` boʻlsa:

1. `78 >= 86` — yolgʻon, keyingi shartga oʻtiladi.
2. `78 >= 71` — rost: `baho = 4`, zanjirning qolgan qismi tekshirilmaydi.
3. Chiqadi: `Baho: 4`.

Kirish `56` boʻlsa: birinchi ikki shart yolgʻon, uchinchisi rost — `Baho: 3`. Chegaraviy qiymatlarni (55, 56, 70, 71, 85, 86) albatta sinab koʻring: xatolar koʻpincha aynan shu yerda.

## Ishlangan misol 2: kvadrat tenglama

*a*, *b*, *c* butun sonlari kiritiladi (*a* ≠ 0). *ax*² + *bx* + *c* = 0 tenglamaning ildizlarini toping. Diskriminant *D* = *b*² − 4*ac*: *D* < 0 — ildiz yoʻq, *D* = 0 — bitta ildiz *x* = −*b* / 2*a*, *D* > 0 — ikkita ildiz *x* = (−*b* ± √*D*) / 2*a*.

```python
import math

a, b, c = map(int, input().split())
d = b * b - 4 * a * c

if d < 0:
    print("Ildiz yoʻq")
elif d == 0:
    x = -b / (2 * a)
    print(f"x = {x:.2f}")
else:
    x1 = (-b + math.sqrt(d)) / (2 * a)
    x2 = (-b - math.sqrt(d)) / (2 * a)
    print(f"x1 = {x1:.2f}, x2 = {x2:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();
        int d = b * b - 4 * a * c;

        if (d < 0) {
            System.out.println("Ildiz yoʻq");
        } else if (d == 0) {
            double x = -b / (2.0 * a);
            System.out.println(String.format("x = %.2f", x));
        } else {
            double x1 = (-b + Math.sqrt(d)) / (2.0 * a);
            double x2 = (-b - Math.sqrt(d)) / (2.0 * a);
            System.out.println(String.format("x1 = %.2f, x2 = %.2f", x1, x2));
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;
    int d = b * b - 4 * a * c;

    cout << fixed << setprecision(2);
    if (d < 0) {
        cout << "Ildiz yoʻq" << endl;
    } else if (d == 0) {
        double x = -b / (2.0 * a);
        cout << "x = " << x << endl;
    } else {
        double x1 = (-b + sqrt(d)) / (2.0 * a);
        double x2 = (-b - sqrt(d)) / (2.0 * a);
        cout << "x1 = " << x1 << ", x2 = " << x2 << endl;
    }
    return 0;
}
```

Kirish `1 -3 2` boʻlsa:

1. *D* = 9 − 8 = 1 > 0 — uchinchi tarmoq.
2. *x*₁ = (3 + 1) / 2 = 2, *x*₂ = (3 − 1) / 2 = 1.
3. Chiqadi: `x1 = 2.00, x2 = 1.00`.

Kirish `1 2 1` boʻlsa: *D* = 0, `x = -1.00`. Kirish `1 0 1` boʻlsa: *D* = −4, `Ildiz yoʻq`. Uch tarmoqning har biri kamida bitta test bilan tekshirildi — bu yaxshi odat.

Java va C++'da `2.0 * a` deb yozilgan: `2 * a` boʻlsa, `-b / (2 * a)` butun boʻlish boʻlib qolardi.

:::warning
**Koʻp uchraydigan xatolar**

- **Shartdan keyin nuqtali vergul (Java, C++).** `if (x > 0);` — shart boʻsh operatorni boshqaradi, keyingi blok har doim bajariladi.
- **Jingalak qavslarsiz bir nechta qator.** `if (x > 0) a = 1; b = 2;` — `b = 2` shartga bogʻliq emas. Har doim `{ }` qoʻying.
- **Pythonʼda chekinishni aralashtirish.** Tab va probellar aralashsa, `IndentationError` chiqadi.
- **Shartlar tartibi notoʻgʻri.** `if ball >= 56` birinchi tursa, 95 ball ham „3“ oladi.
- **`else` ga shart yozish.** `else (x < 0)` — xato; shart kerak boʻlsa, `else if` / `elif`.
:::

:::tip
Har bir tarmoq uchun kamida bitta test oʻylab toping va chegaraviy qiymatlarni alohida tekshiring. Tarmoqlar soni testlarning eng kam sonini beradi.
:::

## Xulosa

- Tarmoqlanuvchi algoritmda shartga qarab turli yoʻl tanlanadi; blok-sxemada shart — romb.
- `if` — bitta yoʻl, `if-else` — ikkitadan biri, `else-if` / `elif` zanjiri — koʻp yoʻldan biri.
- Zanjirda shartlar yuqoridan pastga tekshiriladi va birinchi rost shart bajariladi; tartib muhim.
- Pythonʼda blok chekinish bilan, Java va C++'da `{ }` bilan belgilanadi.
- Ichma-ich shartlarni koʻpincha `and` / `&&` bilan soddalashtirish mumkin.
- Har bir tarmoq va har bir chegaraviy qiymat testlanishi kerak.

## Oʻzingizni tekshiring

:::check
Q: Chiziqli va tarmoqlanuvchi algoritmning farqi nimada?
A: Chiziqli algoritmda barcha qadamlar har doim bir xil tartibda bajariladi; tarmoqlanuvchi algoritmda shartga qarab turli qadamlar tanlanadi.
Q: Baholash dasturida birinchi shart `ball >= 56` boʻlsa, 95 ball uchun natija qanday boʻladi va nima uchun?
A: 3: zanjirda birinchi rost shart bajariladi, 95 >= 56 rost, qolgan shartlar tekshirilmaydi.
Q: Javaʼda `if (x > 5); { System.out.println("katta"); }` nima qiladi?
A: *x* qanday boʻlishidan qatʼi nazar „katta“ chiqaradi: nuqtali vergul boʻsh operator, blok shartga bogʻliq emas.
Q: `if a > 0:` ichida `if b > 0:` ni bitta shart bilan qanday yozasiz?
A: `if a > 0 and b > 0:` (Java/C++: `if (a > 0 && b > 0)`).
Q: Kvadrat tenglama dasturini toʻliq tekshirish uchun kamida nechta test kerak?
A: Uchta: *D* < 0, *D* = 0 va *D* > 0 holatlari uchun bittadan.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#if-statements
Python: if operatori
Rasmiy qoʻllanmadagi `if`, `elif`, `else` boʻlimi.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/if.html
Oracle Java Tutorial: if-then va if-then-else
Javaʼda shart operatorlari haqida rasmiy qoʻllanma.
:::
