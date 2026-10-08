---
title: Kompilyator turlari. Identifikatorlar va ularning turlari
summary: GCC, Clang, MSVC, javac va CPython; dasturni buyruq qatoridan kompilyatsiya qilish va ishga tushirish; kompilyator xabarlarini oʻqish; oʻzgaruvchi, oʻzgarmas, funksiya va klass nomlari.
---

## Nima uchun bu mavzu muhim?

Dasturchi kunining katta qismi kod yozishga emas, kompilyator „nima demoqchi“ ekanini tushunishga ketadi. `error: expected ';' before 'return'` degan xabarni oʻqiy olgan talaba xatoni bir daqiqada tuzatadi, oʻqiy olmagan talaba esa yarim soat kodni qaytadan yozadi.

Bu amaliy mashgʻulotda dasturni oʻz kompyuteringizda kompilyatsiya qilish va ishga tushirishni, kompilyator xabarlarini oʻqishni va nomlarni toʻgʻri tanlashni mashq qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- asosiy kompilyatorlar va interpretatorlarni (GCC, Clang, MSVC, javac, CPython) farqlay olasiz;
- Python, Java va C++ dasturini buyruq qatoridan ishga tushira olasiz;
- kompilyatorning xato (error) va ogohlantirish (warning) xabarlarini oʻqib, xatoni topa olasiz;
- oʻzgaruvchi, oʻzgarmas, funksiya va klass nomlarini har bir tilning qoidasiga koʻra yoza olasiz.
:::

## Kompilyator turlari

Bitta tilning bir nechta kompilyatori boʻlishi mumkin: ular bir xil standartni amalga oshiradi, lekin turli kompaniyalar tomonidan yaratilgan.

| Vosita | Til | Turi | Qayerda uchraydi |
|---|---|---|---|
| **GCC** (`g++`) | C, C++ | mashina kodiga kompilyator | Linux, MinGW orqali Windows |
| **Clang** (`clang++`) | C, C++ | mashina kodiga kompilyator | macOS, Linux, Windows |
| **MSVC** (`cl`) | C, C++ | mashina kodiga kompilyator | Visual Studio, Windows |
| **javac** | Java | bayt-kodga kompilyator | JDK tarkibida |
| **JVM** (`java`) | Java bayt-kodi | interpretator + JIT-kompilyator | JDK yoki JRE tarkibida |
| **CPython** (`python`) | Python | interpretator (ichida bayt-kod) | python.org dan oʻrnatiladi |

**JIT (just-in-time) kompilyatsiya** — dastur ishlayotganda tez-tez bajariladigan qismlarni mashina kodiga oʻgirish. Shu tufayli Java dasturi ishlay-ishlay tezlashadi.

Bu kursning amaliy topshiriqlari platformada GCC 13 (C++17), Java 21 va Python 3.11 da tekshiriladi.

## Dasturni kompilyatsiya qilish va ishga tushirish

Buyruq qatorida (terminalda) har bir til uchun ketma-ketlik quyidagicha:

| Til | Kompilyatsiya | Ishga tushirish |
|---|---|---|
| C++ | `g++ -std=c++17 -o main main.cpp` | `./main` (Windows: `main.exe`) |
| Java | `javac Main.java` | `java Main` |
| Python | — | `python main.py` |

C++ dasturi kompilyatsiya qilinayotganda uch bosqichdan oʻtadi:

1. **Preprotsessor** — `#include` va `#define` direktivalarini bajaradi: masalan, `<iostream>` faylining matnini dasturga qoʻshadi.
2. **Kompilyatsiya** — matnni obyekt fayliga (mashina kodiga) oʻgiradi; sintaksis xatolari shu yerda topiladi.
3. **Bogʻlash (linking)** — obyekt fayllarini va kutubxonalarni bitta bajariladigan faylga birlashtiradi.

Javaʼda fayl nomi `public` klass nomi bilan bir xil boʻlishi shart: `public class Main` faqat `Main.java` faylida turadi.

## Kompilyator xabarlarini oʻqish

Kompilyator ikki xil xabar beradi:

- **Xato (error)** — dastur kompilyatsiya qilinmaydi, uni tuzatish shart.
- **Ogohlantirish (warning)** — dastur kompilyatsiya qilinadi, lekin kompilyator shubhali joyni koʻrsatadi. Ogohlantirishlarni eʼtiborsiz qoldirmang: ular koʻpincha mantiqiy xatoni koʻrsatadi.

Xabar odatda **fayl nomi, qator raqami, ustun raqami va tavsif**dan iborat:

| Til | Xabar namunasi | Maʼnosi |
|---|---|---|
| C++ | `main.cpp:6:18: error: expected ';' before 'return'` | 6-qatorda nuqtali vergul tushib qolgan |
| Java | `Main.java:5: error: cannot find symbol` | 5-qatordagi nom eʼlon qilinmagan yoki xato yozilgan |
| Python | `NameError: name 'pirnt' is not defined` | `print` nomi notoʻgʻri yozilgan |

Qoida: **birinchi xatodan boshlang**. Bitta tushib qolgan qavs oʻnlab „xato“ keltirib chiqarishi mumkin; birinchisini tuzatsangiz, qolganlari oʻzi yoʻqoladi. Xato koʻrsatilgan qatorning *oldingi* qatoriga ham qarang — nuqtali vergul odatda shu yerda tushib qolgan boʻladi.

## Identifikatorlar va ularning turlari

Dasturdagi har bir nom biror narsani bildiradi. Nima bildirishiga qarab nomlar turlicha yoziladi — bu **nomlash kelishuvlari (naming conventions)**:

| Nima | Python | Java | C++ (keng tarqalgan uslub) |
|---|---|---|---|
| Oʻzgaruvchi | `talaba_soni` | `talabaSoni` | `talabaSoni` yoki `talaba_soni` |
| Oʻzgarmas | `MAX_BALL` | `MAX_BALL` | `MAX_BALL` yoki `kMaxBall` |
| Funksiya | `ortacha_ball()` | `ortachaBall()` | `ortachaBall()` |
| Klass | `Talaba` | `Talaba` | `Talaba` |

Uslublar nomlari: `snake_case` (pastki chiziq bilan), `camelCase` (keyingi soʻzlar katta harf bilan), `PascalCase` (birinchi harf ham katta), `UPPER_CASE` (hammasi katta). Kompilyator uslubni tekshirmaydi, lekin jamoada hamma bir xil yozadi — bu kodni oʻqishni osonlashtiradi.

**Oʻzgaruvchi (variable)** qiymati dastur davomida oʻzgarishi mumkin. **Oʻzgarmas (constant)** qiymati bir marta beriladi: C++'da `const`, Javaʼda `final` bilan; Pythonʼda esa faqat kelishuv boʻyicha (katta harfli nom).

## Ishlangan misol 1: toʻgʻri toʻrtburchak

Toʻgʻri toʻrtburchakning ikki tomoni (butun sonlar) kiritiladi. Perimetr va yuzni chiqaring.

```python
# Toʻgʻri toʻrtburchakning perimetri va yuzi
a = int(input())
b = int(input())

perimetr = 2 * (a + b)
yuza = a * b

print("Perimetr:", perimetr)
print("Yuza:", yuza)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Toʻgʻri toʻrtburchakning perimetri va yuzi
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        int perimetr = 2 * (a + b);
        int yuza = a * b;

        System.out.println("Perimetr: " + perimetr);
        System.out.println("Yuza: " + yuza);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    // Toʻgʻri toʻrtburchakning perimetri va yuzi
    int a, b;
    cin >> a >> b;

    int perimetr = 2 * (a + b);
    int yuza = a * b;

    cout << "Perimetr: " << perimetr << endl;
    cout << "Yuza: " << yuza << endl;
    return 0;
}
```

Kirish `7` va `4` boʻlsa:

1. *a* = 7, *b* = 4 oʻqiladi.
2. `2 * (a + b)` — avval qavs ichi: 7 + 4 = 11, keyin 2 · 11 = 22. Qavslarsiz `2 * a + b` = 18 boʻlardi — amallar tartibi muhim.
3. `a * b` = 28.
4. Chiqadi: `Perimetr: 22` va `Yuza: 28`.

Nomlar maʼnoli: `perimetr`, `yuza`. Bir oydan keyin ham dastur nima qilishi tushunarli.

## Ishlangan misol 2: ikki oʻzgaruvchi qiymatini almashtirish

Ikki butun son kiritiladi: *a* va *b*. Ularning qiymatlarini almashtiring va natijani chiqaring.

```python
a = int(input())
b = int(input())

# Uchinchi, vaqtinchalik oʻzgaruvchi yordamida almashtiramiz
temp = a
a = b
b = temp

print("a =", a, "b =", b)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        // Uchinchi, vaqtinchalik oʻzgaruvchi yordamida almashtiramiz
        int temp = a;
        a = b;
        b = temp;

        System.out.println("a = " + a + " b = " + b);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int a, b;
    cin >> a >> b;

    // Uchinchi, vaqtinchalik oʻzgaruvchi yordamida almashtiramiz
    int temp = a;
    a = b;
    b = temp;

    cout << "a = " << a << " b = " << b << endl;
    return 0;
}
```

Kirish `5` va `9` boʻlsa, qiymatlar qadamma-qadam shunday oʻzgaradi:

| Qadam | *a* | *b* | *temp* |
|---|---|---|---|
| Oʻqilgandan keyin | 5 | 9 | — |
| `temp = a` | 5 | 9 | 5 |
| `a = b` | 9 | 9 | 5 |
| `b = temp` | 9 | 5 | 5 |

Natija: `a = 9 b = 5`. Agar `a = b; b = a;` deb yozsak, ikkalasi ham 9 boʻlib qoladi: *a* ning eski qiymati yoʻqoladi. Pythonʼda qisqa yozuv ham bor: `a, b = b, a`.

:::warning
**Koʻp uchraydigan xatolar**

- **Java faylining nomi klass nomiga mos emas.** `public class Main` `main.java` faylida — xato: `class Main is public, should be declared in a file named Main.java`.
- **Oxirgi xatodan boshlash.** Birinchi xato tuzatilgach, qolganlari koʻpincha yoʻqoladi.
- **Ogohlantirishlarni eʼtiborsiz qoldirish.** `warning: unused variable` yoki `warning: suggest parentheses` — haqiqiy xatoning belgisi boʻlishi mumkin.
- **Almashtirishda vaqtinchalik oʻzgaruvchini unutish.** Bir qiymat yoʻqoladi.
:::

:::tip
C++ dasturini `g++ -Wall -Wextra` bilan kompilyatsiya qiling: kompilyator koʻproq ogohlantirish beradi va koʻp xatolar dastur ishga tushmasdan oldin topiladi.
:::

## Xulosa

- C++ uchun GCC, Clang va MSVC kompilyatorlari bor; Java `javac` bilan bayt-kodga kompilyatsiya qilinib, JVMʼda ishlaydi; Python dasturini CPython interpretatori bajaradi.
- C++ kompilyatsiyasi: preprotsessor → kompilyatsiya → bogʻlash.
- Kompilyator xabarida fayl, qator va tavsif bor; birinchi xatodan boshlang, ogohlantirishlarni ham oʻqing.
- Nomlar turiga qarab turlicha yoziladi: `snake_case`, `camelCase`, `PascalCase`, `UPPER_CASE`.
- Ikki oʻzgaruvchini almashtirish uchun uchinchi, vaqtinchalik oʻzgaruvchi kerak.

## Oʻzingizni tekshiring

:::check
Q: GCC, javac va CPythonʼning asosiy farqi nimada?
A: GCC C++ dasturini mashina kodiga kompilyatsiya qiladi, javac Java dasturini JVM bajaradigan bayt-kodga oʻgiradi, CPython esa Python dasturini interpretatsiya qiladi.
Q: C++ kompilyatsiyasining qaysi bosqichida `#include <iostream>` bajariladi?
A: Preprotsessor bosqichida: fayl matni dasturga qoʻshiladi.
Q: `main.cpp:12:5: error: 'cout' was not declared in this scope` xabari nimani bildiradi?
A: `main.cpp` faylining 12-qatori, 5-ustunida `cout` nomi tanilmadi: `#include <iostream>` yoki `using namespace std;` (yoki `std::cout`) tushib qolgan.
Q: Javaʼda oʻzgarmas va klass nomlari qanday yoziladi?
A: Oʻzgarmas — `UPPER_CASE` (`MAX_BALL`), klass — `PascalCase` (`Talaba`).
Q: `a = b; b = a;` nima uchun qiymatlarni almashtirmaydi?
A: Birinchi qatordan keyin *a* ning eski qiymati yoʻqoladi, ikkala oʻzgaruvchida *b* ning qiymati qoladi.
:::

:::link https://gcc.gnu.org/onlinedocs/gcc/Warning-Options.html
GCC: ogohlantirish parametrlari
`-Wall`, `-Wextra` va boshqa parametrlar haqida rasmiy hujjat.
:::

:::link https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html
Oracle: Java nomlash kelishuvlari
Klass, metod, oʻzgaruvchi va oʻzgarmas nomlari qanday yoziladi.
:::
