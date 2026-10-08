---
title: Tarmoqlanuvchi jarayonlar. Shartli oʻtish operatori
summary: if operatori bilan amaliy masalalar: eng kattasini topish, uchburchak turi, nuqtaning choragi; kiritilgan maʼlumotni tekshirish va tarmoqlarni testlash.
---

## Nima uchun bu mavzu muhim?

Internet-doʻkon savatchadagi summa 300 000 soʻmdan oshsa, yetkazib berishni bepul qiladi; 1 000 000 dan oshsa, yana 5 % chegirma beradi. Bank kredit berishdan oldin oylik daromadni, yoshni va kredit tarixini tekshiradi. Bunday qoidalarni `if` operatori bilan xatosiz yozish — dasturchining kundalik ishi.

Maʼruzada `if`, `else` va `elif` / `else if` ni oʻrgandik. Bu amaliy mashgʻulotda ularni tipik masalalarda qoʻllaymiz va har bir tarmoqni test bilan tekshirishni odat qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- uch va undan ortiq son ichidan eng kattasini (eng kichigini) topa olasiz;
- bir nechta shartga bogʻliq masalani (uchburchak turi, nuqtaning choragi) toʻgʻri tartiblangan shartlar bilan yecha olasiz;
- kiritilgan maʼlumotning toʻgʻriligini tekshirib, notoʻgʻri holatda xabar chiqara olasiz;
- har bir tarmoq va chegaraviy holat uchun test tuza olasiz.
:::

## Shart operatori: qisqa takror

| Tuzilma | Python | Java, C++ |
|---|---|---|
| Faqat agar | `if s:` | `if (s) { }` |
| Agar … aks holda | `if s:` … `else:` | `if (s) { } else { }` |
| Zanjir | `if` … `elif` … `else` | `if` … `else if` … `else` |

Uch qoida:

1. Zanjirda birinchi rost shart bajariladi — shartlar tartibi natijani belgilaydi.
2. Java va C++'da har doim `{ }` qoʻying, hatto blokda bitta qator boʻlsa ham.
3. Har bir tarmoq kamida bitta test bilan tekshirilishi kerak.

## Andoza: eng kattasini topish

Bir nechta qiymatning eng kattasini topishning ishonchli usuli — **„hozirgacha eng katta“** oʻzgaruvchisi:

1. Birinchi qiymatni eng katta deb olamiz: `mx = a`.
2. Har bir keyingi qiymatni u bilan solishtiramiz: `if b > mx: mx = b`.
3. Oxirida `mx` — javob.

Bu usul har qancha qiymat uchun ishlaydi va keyinchalik sikl bilan birga qoʻllanadi. Taqqoslash uchun, „har bir son qolganlaridan katta ekanini tekshirish“ (`a >= b and a >= c`) usuli uch son uchun ishlaydi, lekin sonlar koʻpaysa, shartlar keskin uzayadi.

Standart funksiyalar ham bor: Pythonʼda `max(a, b, c)`, Javaʼda `Math.max(a, b)`, C++'da `max({a, b, c})`. Amaliy mashgʻulotda algoritmni tushunish uchun avval qoʻlda yozamiz.

## Andoza: kiritishni tekshirish

Dastur notoʻgʻri maʼlumotga ham toʻgʻri javob berishi kerak. Masalan, uchburchakning tomonlari musbat boʻlishi va **uchburchak tengsizligi**ni qanoatlantirishi kerak: har bir tomon qolgan ikkitasining yigʻindisidan kichik. Shuning uchun avval tekshiruv, keyin hisob:

```text
agar maʼlumot notoʻgʻri:
    xabar chiqar
aks holda:
    asosiy hisob
```

## Ishlangan misol 1: uch sonning eng kattasi

Bir qatorda uchta butun son kiritiladi. Eng kattasini chiqaring.

```python
a, b, c = map(int, input().split())

mx = a                 # hozirgacha eng katta
if b > mx:
    mx = b
if c > mx:
    mx = c

print(mx)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        int mx = a;            // hozirgacha eng katta
        if (b > mx) {
            mx = b;
        }
        if (c > mx) {
            mx = c;
        }

        System.out.println(mx);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    int mx = a;            // hozirgacha eng katta
    if (b > mx) {
        mx = b;
    }
    if (c > mx) {
        mx = c;
    }

    cout << mx << endl;
    return 0;
}
```

Kirish `7 12 9` boʻlsa:

1. `mx = 7`.
2. `12 > 7` — rost, `mx = 12`.
3. `9 > 12` — yolgʻon, `mx` oʻzgarmaydi.
4. Chiqadi: `12`.

Eʼtibor bering: bu yerda ikkita **alohida** `if` ishlatilgan, zanjir emas — chunki har bir son tekshirilishi kerak. Testlar: eng katta birinchi oʻrinda (`9 1 5`), oʻrtada, oxirida, teng sonlar (`4 4 4`), manfiy sonlar (`-5 -2 -9` → `-2`).

## Ishlangan misol 2: uchburchak turi

Uchburchakning uch tomoni (musbat butun sonlar) kiritiladi. Bunday uchburchak mavjud boʻlmasa `mavjud emas`, aks holda turini chiqaring: `teng tomonli`, `teng yonli` yoki `turli tomonli`.

```python
a, b, c = map(int, input().split())

if a + b <= c or a + c <= b or b + c <= a:
    print("mavjud emas")
elif a == b and b == c:
    print("teng tomonli")
elif a == b or b == c or a == c:
    print("teng yonli")
else:
    print("turli tomonli")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        if (a + b <= c || a + c <= b || b + c <= a) {
            System.out.println("mavjud emas");
        } else if (a == b && b == c) {
            System.out.println("teng tomonli");
        } else if (a == b || b == c || a == c) {
            System.out.println("teng yonli");
        } else {
            System.out.println("turli tomonli");
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b, c;
    cin >> a >> b >> c;

    if (a + b <= c || a + c <= b || b + c <= a) {
        cout << "mavjud emas" << endl;
    } else if (a == b && b == c) {
        cout << "teng tomonli" << endl;
    } else if (a == b || b == c || a == c) {
        cout << "teng yonli" << endl;
    } else {
        cout << "turli tomonli" << endl;
    }
    return 0;
}
```

Tahlil:

| Kirish | Qaysi shart rost | Natija |
|---|---|---|
| `1 2 5` | birinchi: 1 + 2 ≤ 5 | `mavjud emas` |
| `5 5 5` | ikkinchi | `teng tomonli` |
| `5 5 8` | uchinchi | `teng yonli` |
| `3 4 5` | hech biri | `turli tomonli` |

Tartib muhim: „teng tomonli“ sharti „teng yonli“dan **oldin** turishi kerak, chunki teng tomonli uchburchakda ham ikki tomon teng. Aks holda `5 5 5` „teng yonli“ deb topiladi. Tekshiruv esa eng birinchi: mavjud boʻlmagan uchburchakning turini aniqlashning maʼnosi yoʻq.

## Ishlangan misol 3: nuqtaning choragi

Nuqtaning koordinatalari *x* va *y* (butun sonlar) kiritiladi. Nuqta qaysi chorakda yotishini chiqaring; agar u oʻqda yotsa — `oʻqda`.

```python
x, y = map(int, input().split())

if x == 0 or y == 0:
    print("oʻqda")
elif x > 0:
    if y > 0:
        print(1)
    else:
        print(4)
else:
    if y > 0:
        print(2)
    else:
        print(3)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int x = in.nextInt();
        int y = in.nextInt();

        if (x == 0 || y == 0) {
            System.out.println("oʻqda");
        } else if (x > 0) {
            if (y > 0) {
                System.out.println(1);
            } else {
                System.out.println(4);
            }
        } else {
            if (y > 0) {
                System.out.println(2);
            } else {
                System.out.println(3);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int x, y;
    cin >> x >> y;

    if (x == 0 || y == 0) {
        cout << "oʻqda" << endl;
    } else if (x > 0) {
        if (y > 0) {
            cout << 1 << endl;
        } else {
            cout << 4 << endl;
        }
    } else {
        if (y > 0) {
            cout << 2 << endl;
        } else {
            cout << 3 << endl;
        }
    }
    return 0;
}
```

Bu yerda ichma-ich shart oʻrinli: avval *x* ning ishorasi, keyin *y* niki. Kirish `-3 4` → `2`, `5 -1` → `4`, `0 7` → `oʻqda`. Toʻrt chorak va oʻq — beshta tarmoq, demak kamida beshta test kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **Eng kattasini `if-elif` zanjiri bilan qidirish.** `if b > mx … elif c > mx` — *b* katta boʻlsa, *c* umuman tekshirilmaydi.
- **Uchburchak tengsizligida `<` va `<=` ni chalkashtirish.** `1 2 3` — „yassi“ uchburchak, u mavjud emas: `a + b <= c` boʻlishi kerak.
- **Umumiy holatni xususiydan oldin tekshirish.** „Teng yonli“ „teng tomonli“dan oldin tursa, teng tomonli uchburchak hech qachon topilmaydi.
- **Chegaraviy holatni unutish.** Nuqta oʻqda yotganda (*x* = 0) dastur notoʻgʻri chorak chiqaradi.
:::

:::tip
Testlarni kod yozishdan **oldin** jadval qilib yozing: kirish, kutilgan natija, qaysi tarmoq. Kod tayyor boʻlgach, jadvaldagi har bir qatorni tekshiring.
:::

## Xulosa

- Eng katta (eng kichik) qiymat „hozirgacha eng katta“ oʻzgaruvchisi va alohida `if` lar bilan topiladi.
- Kiritilgan maʼlumot avval tekshiriladi, keyin hisob bajariladi.
- Zanjirda xususiy holat (teng tomonli) umumiydan (teng yonli) oldin turadi.
- Ichma-ich shart mantiqiy ierarxiya boʻlganda oʻrinli (avval *x*, keyin *y*).
- Har bir tarmoq va chegaraviy holat uchun alohida test kerak.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun eng kattasini topishda `elif` emas, alohida `if` lar ishlatiladi?
A: Zanjirda birinchi rost shartdan keyin qolganlari tekshirilmaydi, lekin har bir son „hozirgacha eng katta“ bilan solishtirilishi kerak.
Q: `2 3 5` tomonli uchburchak mavjudmi?
A: Yoʻq: 2 + 3 = 5, tengsizlik qatʼiy emas — uchburchak „yassi“ boʻladi.
Q: Uchburchak turini aniqlash dasturida „teng yonli“ sharti birinchi tursa, `5 5 5` uchun nima chiqadi?
A: „teng yonli“: `a == b` rost, zanjir shu yerda toʻxtaydi.
Q: Nuqtaning choragi masalasi uchun kamida nechta test kerak?
A: Beshta: toʻrtta chorak va oʻqda yotgan nuqta.
Q: Uch sonning eng kichigini topish uchun dasturda nimani oʻzgartirasiz?
A: Solishtirish belgisini `>` dan `<` ga almashtirasiz (va oʻzgaruvchini `mn` deb atash maʼqul).
:::

:::link https://docs.python.org/3/library/functions.html#max
Python: max() funksiyasi
Bir nechta qiymatning eng kattasini qaytaruvchi standart funksiya.
:::

:::link https://en.cppreference.com/w/cpp/language/if
cppreference: if operatori
C++'da `if` va `else` sintaksisi.
:::
