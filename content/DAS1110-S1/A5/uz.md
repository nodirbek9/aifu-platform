---
title: Tarmoqlanuvchi jarayonlar. Tanlash operatori. Ternar operator
summary: switch (Java, C++) va match (Python) tanlash operatori, case, break, default, „tushib ketish“; ternar operator; qachon if, qachon switch.
---

## Nima uchun bu mavzu muhim?

Bankomat menyusi: 1 — balans, 2 — naqd pul, 3 — toʻlov, 4 — chiqish. Telefon klaviaturasidagi har bir tugma oʻz harflarini beradi. Hafta kuni raqamiga qarab dars jadvali chiqadi. Bunday masalalarda bitta oʻzgaruvchi **bir nechta aniq qiymat**dan biri bilan solishtiriladi. Buni `if-else if` zanjiri bilan ham yozish mumkin, lekin buning uchun maxsus, oʻqilishi osonroq operator bor — **tanlash operatori**.

Bundan tashqari, „ikkitadan biri“ tanlovini bitta ifodada yozadigan qisqa vosita — **ternar operator** — bilan tanishamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- `switch` (Java, C++) va `match` (Python) operatorlarini yoza olasiz;
- `break` va `default` ning vazifasini tushuntirib, „tushib ketish“ xatosini oldini olasiz;
- ternar operator bilan qiymatni shartga qarab tanlay olasiz;
- masalaga qarab `if`, `switch` yoki ternar operatordan mosini tanlay olasiz.
:::

## switch operatori (Java, C++)

**Tanlash operatori `switch`** ifoda qiymatini ketma-ket `case` belgilaridagi oʻzgarmaslar bilan solishtiradi va mos kelganidan boshlab bajaradi.

| Qism | Vazifasi |
|---|---|
| `switch (ifoda)` | qaysi qiymat tekshiriladi (butun son, belgi; Java'da satr ham) |
| `case 1:` | „agar qiymat 1 boʻlsa, shu yerdan boshla“ |
| `break;` | `switch` dan chiqish |
| `default:` | hech bir `case` mos kelmasa (ixtiyoriy, lekin tavsiya etiladi) |

Muhim xususiyat — **tushib ketish (fall-through)**: `break` boʻlmasa, bajarilish keyingi `case` ga oʻtib ketadi. Bu koʻpincha xato manbai, lekin baʼzan ataylab ishlatiladi: bir nechta qiymat uchun bitta amal.

```text
case 6:
case 7:
    "dam olish kuni"
    break
```

C++'da `switch` faqat butun son va belgi turlari bilan ishlaydi; Java'da satr (`String`) bilan ham. Java 14 dan boshlab qisqa yozuv bor: `case 1 -> ...;` — unda tushib ketish yoʻq va `break` kerak emas.

## match operatori (Python)

Python 3.10 dan boshlab `match` operatori bor:

| Qism | Maʼnosi |
|---|---|
| `match ifoda:` | qaysi qiymat tekshiriladi |
| `case 1:` | qiymat 1 boʻlsa |
| `case 6 \| 7:` | 6 yoki 7 boʻlsa |
| `case _:` | boshqa barcha holatlar (`default` ning oʻrni) |

Python'da tushib ketish yoʻq: faqat mos kelgan `case` bajariladi, `break` kerak emas. Python 3.10 dan eski versiyalarda xuddi shu masala `if-elif` bilan yoziladi.

## Ternar operator

**Ternar (uch qismli) operator** shartga qarab ikki qiymatdan birini qaytaradi. U *operator* emas, *ifoda*: natijani oʻzgaruvchiga berish yoki toʻgʻridan-toʻgʻri chiqarish mumkin.

| Til | Yozilishi | Misol |
|---|---|---|
| Java, C++ | `shart ? qiymat1 : qiymat2` | `mx = a > b ? a : b;` |
| Python | `qiymat1 if shart else qiymat2` | `mx = a if a > b else b` |

Ternar operator qisqa tanlovlar uchun qulay. Ichma-ich ternar (`a ? b : c ? d : e`) esa oʻqishni qiyinlashtiradi — bunday hollarda `if` yaxshiroq.

## Qachon nima ishlatiladi?

| Holat | Tanlov |
|---|---|
| Bitta oʻzgaruvchi bir nechta aniq qiymat bilan solishtiriladi (menyu, kun, buyruq) | `switch` / `match` |
| Oraliqlar (`ball >= 86`), murakkab shartlar | `if-else if` |
| Ikki qiymatdan biri, bitta ifodada | ternar operator |

## Ishlangan misol 1: hafta kuni

Hafta kunining raqami (1–7) kiritiladi. Kun nomini chiqaring; 6 va 7 uchun qoʻshimcha „(dam olish)“ yozing. Notoʻgʻri raqam uchun `notoʻgʻri kun`.

```python
kun = int(input())

match kun:
    case 1:
        print("Dushanba")
    case 2:
        print("Seshanba")
    case 3:
        print("Chorshanba")
    case 4:
        print("Payshanba")
    case 5:
        print("Juma")
    case 6:
        print("Shanba (dam olish)")
    case 7:
        print("Yakshanba (dam olish)")
    case _:
        print("notoʻgʻri kun")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int kun = in.nextInt();

        switch (kun) {
            case 1: System.out.println("Dushanba"); break;
            case 2: System.out.println("Seshanba"); break;
            case 3: System.out.println("Chorshanba"); break;
            case 4: System.out.println("Payshanba"); break;
            case 5: System.out.println("Juma"); break;
            case 6: System.out.println("Shanba (dam olish)"); break;
            case 7: System.out.println("Yakshanba (dam olish)"); break;
            default: System.out.println("notoʻgʻri kun");
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int kun;
    cin >> kun;

    switch (kun) {
        case 1: cout << "Dushanba" << endl; break;
        case 2: cout << "Seshanba" << endl; break;
        case 3: cout << "Chorshanba" << endl; break;
        case 4: cout << "Payshanba" << endl; break;
        case 5: cout << "Juma" << endl; break;
        case 6: cout << "Shanba (dam olish)" << endl; break;
        case 7: cout << "Yakshanba (dam olish)" << endl; break;
        default: cout << "notoʻgʻri kun" << endl;
    }
    return 0;
}
```

Kirish `3` boʻlsa, `case 3` mos keladi, `Chorshanba` chiqadi, `break` `switch` dan chiqaradi. Kirish `9` boʻlsa, hech bir `case` mos kelmaydi — `default` bajariladi. Agar Java yoki C++'da `case 3` dagi `break` unutilsa, `Chorshanba` dan keyin `Payshanba` ham chiqib ketadi.

## Ishlangan misol 2: kalkulyator

Bir qatorda ifoda kiritiladi: butun son, amal belgisi (`+`, `-`, `*`, `/`) va butun son, probel bilan ajratilgan. Natijani chiqaring; boʻlishda ikki kasr xona aniqlikda. Nolga boʻlish va nomaʼlum amal uchun xabar chiqaring.

```python
qism = input().split()
a = int(qism[0])
amal = qism[1]
b = int(qism[2])

match amal:
    case "+":
        print(a + b)
    case "-":
        print(a - b)
    case "*":
        print(a * b)
    case "/":
        if b == 0:
            print("Xato: nolga boʻlish")
        else:
            print(f"{a / b:.2f}")
    case _:
        print("Xato: nomaʼlum amal")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        char amal = in.next().charAt(0);
        int b = in.nextInt();

        switch (amal) {
            case '+': System.out.println(a + b); break;
            case '-': System.out.println(a - b); break;
            case '*': System.out.println(a * b); break;
            case '/':
                if (b == 0) {
                    System.out.println("Xato: nolga boʻlish");
                } else {
                    System.out.println(String.format("%.2f", (double) a / b));
                }
                break;
            default: System.out.println("Xato: nomaʼlum amal");
        }
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    int a, b;
    char amal;
    cin >> a >> amal >> b;

    switch (amal) {
        case '+': cout << a + b << endl; break;
        case '-': cout << a - b << endl; break;
        case '*': cout << a * b << endl; break;
        case '/':
            if (b == 0) {
                cout << "Xato: nolga boʻlish" << endl;
            } else {
                cout << fixed << setprecision(2) << (double) a / b << endl;
            }
            break;
        default: cout << "Xato: nomaʼlum amal" << endl;
    }
    return 0;
}
```

Tahlil:

| Kirish | Tanlangan tarmoq | Natija |
|---|---|---|
| `7 * 6` | `*` | `42` |
| `7 / 2` | `/`, *b* ≠ 0 | `3.50` |
| `5 / 0` | `/`, *b* = 0 | `Xato: nolga boʻlish` |
| `5 % 2` | `default` | `Xato: nomaʼlum amal` |

`switch` ichida `if` boʻlishi mumkin — tuzilmalar bir-birining ichiga joylashadi. Java'da belgi `in.next().charAt(0)` bilan oʻqiladi: `next()` keyingi soʻzni beradi, `charAt(0)` uning birinchi belgisini.

## Ishlangan misol 3: ternar operator

Butun son kiritiladi. Uning juft yoki toqligini va modulini chiqaring.

```python
n = int(input())

turi = "juft" if n % 2 == 0 else "toq"
modul = n if n >= 0 else -n

print(turi, modul)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        String turi = n % 2 == 0 ? "juft" : "toq";
        int modul = n >= 0 ? n : -n;

        System.out.println(turi + " " + modul);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;

    string turi = n % 2 == 0 ? "juft" : "toq";
    int modul = n >= 0 ? n : -n;

    cout << turi << " " << modul << endl;
    return 0;
}
```

Kirish `-7` boʻlsa: `-7 % 2` Python'da 1, Java va C++'da −1 — ikkalasi ham 0 emas, shuning uchun `toq`; `-7 >= 0` yolgʻon, modul `7`. Chiqadi: `toq 7`. Toq sonni `n % 2 == 1` bilan tekshirish manfiy sonlarda Java va C++'da xato beradi — `n % 2 != 0` yoki `== 0` bilan tekshiring.

:::warning
**Koʻp uchraydigan xatolar**

- **`break` ni unutish (Java, C++).** Keyingi `case` lar ham bajariladi: „Chorshanba Payshanba Juma …“.
- **`case` da oʻzgaruvchi yoki oraliq.** `case x > 5:` mumkin emas; `case` da faqat oʻzgarmas qiymat. Oraliqlar uchun `if`.
- **C++'da satr boʻyicha `switch`.** C++'da `switch` faqat butun son va belgilar bilan ishlaydi.
- **`default` siz `switch`.** Kutilmagan qiymat jimgina eʼtiborsiz qoladi, xato topilmaydi.
- **Ichma-ich ternar operatorlar.** Bir qatorda uch-toʻrtta `?` — oʻqib boʻlmaydigan kod.
:::

:::tip
Har bir `case` ni `break` bilan tugatishni odat qiling; agar tushib ketish ataylab boʻlsa, izoh yozing: `// ataylab davom etadi`.
:::

## Xulosa

- `switch` (Java, C++) va `match` (Python) bitta qiymatni bir nechta aniq oʻzgarmas bilan solishtiradi.
- Java va C++'da `break` boʻlmasa, bajarilish keyingi `case` ga tushib ketadi; Python'da tushib ketish yoʻq.
- `default` / `case _` — boshqa barcha holatlar uchun.
- Ternar operator `shart ? a : b` (Python: `a if shart else b`) — qiymat qaytaruvchi qisqa tanlov.
- Oraliqlar va murakkab shartlar uchun `if`, aniq qiymatlar uchun `switch`, ikki qiymatdan biri uchun ternar operator.

## Oʻzingizni tekshiring

:::check
Q: Java'da `switch` da `case 2:` dagi `break` tushib qolgan boʻlsa va qiymat 2 boʻlsa, nima boʻladi?
A: `case 2` amali bajariladi, keyin `case 3` ning (va keyingilarning) amali ham — birinchi `break` gacha yoki `switch` oxirigacha.
Q: Python'da `default` ning vazifasini qaysi yozuv bajaradi?
A: `case _:`.
Q: `ball >= 86` kabi oraliqni `switch` bilan tekshirish mumkinmi?
A: Klassik `switch` da yoʻq: `case` da faqat aniq oʻzgarmas qiymat boʻladi. Oraliqlar uchun `if-else if` ishlatiladi.
Q: `mx = a > b ? a : b;` ni Python'da qanday yozasiz?
A: `mx = a if a > b else b`.
Q: Nima uchun Java'da toq sonni `n % 2 == 1` bilan tekshirish xavfli?
A: Manfiy toq sonlar uchun `n % 2` −1 ga teng, shart yolgʻon boʻlib chiqadi. `n % 2 != 0` bilan tekshirish kerak.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#match-statements
Python: match operatori
Rasmiy qoʻllanmadagi `match` va `case` boʻlimi.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/switch.html
Oracle Java Tutorial: switch operatori
`switch`, `case`, `break` va `default` haqida.
:::
