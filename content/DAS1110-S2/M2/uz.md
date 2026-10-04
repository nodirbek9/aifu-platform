---
title: Obyektga yoʻnaltirilgan dasturlash asoslari
summary: Sinf va obyekt, maydonlar va metodlar, konstruktor, self va this, obyektni matnga aylantirish, statik maydon, obyekt havolasi va nusxasi; uch tilda sinf yozish.
---

## Nima uchun bu mavzu muhim?

Hozirgacha dasturlarimiz alohida oʻzgaruvchilar va funksiyalardan iborat edi. Talabaning ismi, bahosi va guruhi uchta alohida oʻzgaruvchida turdi, ularni qayta ishlovchi funksiyalar esa dasturning boshqa joyida. Dastur kattalashgani sari qaysi maʼlumot qaysi funksiyaga tegishli ekanini kuzatish qiyinlashadi.

**Obyektga yoʻnaltirilgan dasturlash** (OYD) maʼlumot va u bilan ishlaydigan amallarni bitta birlikka — **obyekt**ga birlashtiradi. Java toʻliq shu gʻoyaga qurilgan, Python va C++'da ham katta dasturlar asosan sinflar bilan yoziladi. Ushbu va keyingi ikki maʼruza OYD ning asosiy tushunchalariga bagʻishlangan.

:::note
**Bu darsdan soʻng siz:**

- sinf va obyekt tushunchalarini farqlay olasiz;
- maydon, metod va konstruktorli sinfni Python, Java va C++'da yoza olasiz;
- `self` va `this` nimani bildirishini tushuntira olasiz;
- obyektni matn koʻrinishiga oʻtkazuvchi metod yoza olasiz;
- statik maydon bilan obyekt maydoni farqini ayta olasiz;
- obyektni oʻzlashtirish (`b = a`) tillarda qanday ishlashini tushuntira olasiz.
:::

## Sinf va obyekt

**Sinf** (class) — yangi tur tavsifi: bu turdagi har bir qiymat qanday maʼlumotga ega va nima qila oladi. **Obyekt** — shu sinfning aniq nusxasi (instance).

| Tushuncha | Maʼnosi | Misol (`Talaba` sinfi) |
|---|---|---|
| Sinf | shablon, chizma | `Talaba` |
| Obyekt | sinfning aniq nusxasi | „Aziz, 2-kurs“ talabasi |
| Maydon (field, atribut) | obyekt saqlaydigan maʼlumot | `ism`, `kurs` |
| Metod | obyekt bajaradigan amal | `kursni_oshir()` |
| Konstruktor | obyekt yaratilganda ishlaydigan maxsus metod | maydonlarga boshlangʻich qiymat beradi |

Bitta sinfdan istalgancha obyekt yaratish mumkin; har birining maydonlari oʻziniki, metodlari esa umumiy.

## Sinfni uch tilda yozish

```python
class Talaba:
    def __init__(self, ism, kurs):     # konstruktor
        self.ism = ism                 # maydonlar
        self.kurs = kurs

    def kursni_oshir(self):            # metod
        self.kurs += 1

t = Talaba("Aziz", 2)
t.kursni_oshir()
print(t.ism, t.kurs)                   # Aziz 3
```

| | Python | Java | C++ |
|---|---|---|---|
| Konstruktor | `def __init__(self, ...)` | sinf nomi bilan: `Talaba(String ism, int kurs)` | sinf nomi bilan: `Talaba(string ism, int kurs)` |
| Maydonni eʼlon qilish | konstruktorda `self.ism = ...` | sinf tanasida: `String ism;` | sinf tanasida: `string ism;` |
| Joriy obyekt | `self` (birinchi parametr, aniq yoziladi) | `this` (yashirin) | `this` (koʻrsatkich, yashirin) |
| Obyekt yaratish | `Talaba("Aziz", 2)` | `new Talaba("Aziz", 2)` | `Talaba t("Aziz", 2);` |
| Matnga aylantirish | `def __str__(self)` | `public String toString()` | `<<` operatorini yoki oʻz metodini yozish |

**`self` va `this`** — metod chaqirilgan obyektning oʻzi. `t.kursni_oshir()` chaqirilganda metod ichida `self` (Java/C++'da `this`) aynan `t` ga teng. Java va C++'da maydon nomi parametr nomi bilan bir xil boʻlsa, `this.ism = ism` deb farqlanadi.

## Statik maydonlar

Oddiy maydon har bir obyektda alohida. **Statik maydon** (Python'da — sinf atributi) esa sinfga tegishli va barcha obyektlar uchun **bitta**. Masalan, nechta talaba yaratilganini sanash uchun:

| Til | Statik maydon | Murojaat |
|---|---|---|
| Python | sinf tanasida: `soni = 0` | `Talaba.soni` |
| Java | `static int soni = 0;` | `Talaba.soni` |
| C++ | `static int soni;` va sinfdan tashqarida `int Talaba::soni = 0;` (C++17'dan `inline static int soni = 0;`) | `Talaba::soni` |

## Havola va nusxa

Obyektni boshqa oʻzgaruvchiga oʻzlashtirish tillarda **turlicha** ishlaydi:

- **Python va Java**: oʻzgaruvchi obyektning oʻzini emas, unga **havolani** saqlaydi. `b = a` dan keyin `a` va `b` — bitta obyekt; `b.kurs` ni oʻzgartirsangiz, `a.kurs` ham oʻzgaradi.
- **C++**: `Talaba b = a;` obyektning **nusxasini** yaratadi; `b` ni oʻzgartirish `a` ga taʼsir qilmaydi. Havola kerak boʻlsa — `Talaba& b = a;`.

Shuning uchun Java yoki Pythonda obyektni funksiyaga uzatib, funksiya ichida uning maydonini oʻzgartirsangiz, oʻzgarish tashqarida ham koʻrinadi.

## Ishlangan misol 1: toʻgʻri toʻrtburchak sinfi

Toʻgʻri toʻrtburchakning eni va boʻyi kiritiladi. `Tortburchak` sinfini yozing: konstruktor, `yuza()` va `perimetr()` metodlari. Yuza va perimetrni chiqaring.

```python
class Tortburchak:
    def __init__(self, en, boy):
        self.en = en
        self.boy = boy

    def yuza(self):
        return self.en * self.boy

    def perimetr(self):
        return 2 * (self.en + self.boy)


en, boy = map(int, input().split())
t = Tortburchak(en, boy)
print(t.yuza(), t.perimetr())
```
```java
import java.util.Scanner;

public class Main {
    static class Tortburchak {
        long en, boy;

        Tortburchak(long en, long boy) {
            this.en = en;
            this.boy = boy;
        }

        long yuza() {
            return en * boy;
        }

        long perimetr() {
            return 2 * (en + boy);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tortburchak t = new Tortburchak(in.nextLong(), in.nextLong());
        System.out.println(t.yuza() + " " + t.perimetr());
    }
}
```
```cpp
#include <iostream>
using namespace std;

class Tortburchak {
public:
    long long en, boy;

    Tortburchak(long long en, long long boy) : en(en), boy(boy) {}

    long long yuza() const { return en * boy; }
    long long perimetr() const { return 2 * (en + boy); }
};

int main() {
    long long en, boy;
    cin >> en >> boy;
    Tortburchak t(en, boy);
    cout << t.yuza() << " " << t.perimetr() << endl;
    return 0;
}
```

`3 5` kiritilsa, chiqish: `15 16`. C++'dagi `: en(en), boy(boy)` — **initsializatsiya roʻyxati**: maydonlar konstruktor tanasidan oldin qiymat oladi. Metod oxiridagi `const` — „bu metod obyektni oʻzgartirmaydi“ degan vaʼda. Java'da `static class` — `Main` ichidagi mustaqil sinf; bitta faylda yozish uchun shunday qilamiz.

## Ishlangan misol 2: vaqt sinfi

Soat va daqiqa (`HH MM`) hamda *k* daqiqa kiritiladi. `Vaqt` sinfini yozing: `qosh(k)` metodi vaqtni *k* daqiqaga oldinga suradi (sutka aylanadi), matnga aylantirish esa `HH:MM` koʻrinishini beradi.

```python
class Vaqt:
    def __init__(self, soat, daqiqa):
        self.daqiqalar = (soat * 60 + daqiqa) % 1440

    def qosh(self, k):
        self.daqiqalar = (self.daqiqalar + k) % 1440

    def __str__(self):
        return f"{self.daqiqalar // 60:02d}:{self.daqiqalar % 60:02d}"


soat, daqiqa = map(int, input().split())
k = int(input())
v = Vaqt(soat, daqiqa)
v.qosh(k)
print(v)
```
```java
import java.util.Scanner;

public class Main {
    static class Vaqt {
        private int daqiqalar;

        Vaqt(int soat, int daqiqa) {
            daqiqalar = (soat * 60 + daqiqa) % 1440;
        }

        void qosh(int k) {
            daqiqalar = (daqiqalar + k) % 1440;
        }

        @Override
        public String toString() {
            return String.format("%02d:%02d", daqiqalar / 60, daqiqalar % 60);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Vaqt v = new Vaqt(in.nextInt(), in.nextInt());
        v.qosh(in.nextInt());
        System.out.println(v);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

class Vaqt {
    int daqiqalar;

public:
    Vaqt(int soat, int daqiqa) : daqiqalar((soat * 60 + daqiqa) % 1440) {}

    void qosh(int k) { daqiqalar = (daqiqalar + k) % 1440; }

    string matn() const {
        char buf[6];
        snprintf(buf, sizeof buf, "%02d:%02d", daqiqalar / 60, daqiqalar % 60);
        return buf;
    }
};

int main() {
    int soat, daqiqa, k;
    cin >> soat >> daqiqa >> k;
    Vaqt v(soat, daqiqa);
    v.qosh(k);
    cout << v.matn() << endl;
    return 0;
}
```

`23 45` va `30` kiritilsa, chiqish `00:15`: 23:45 dan 30 daqiqa oʻtib yarim tundan oʻtdik. Obyekt ichida vaqt soat va daqiqa sifatida emas, **yarim tundan beri oʻtgan daqiqalar** sifatida saqlanadi — hisoblash shunda soddaroq. Bu ichki tanlov sinfdan foydalanuvchiga koʻrinmaydi: u faqat `qosh` va matn koʻrinishini biladi. Python'da `print(v)` avtomatik `__str__` ni, Java'da `println(v)` — `toString()` ni chaqiradi.

## Ishlangan misol 3: kasr sinfi va statik hisoblagich

Ikkita kasr `a b` va `c d` (`a/b` va `c/d`) kiritiladi. `Kasr` sinfini yozing: konstruktor kasrni qisqartiradi, `qosh` metodi yangi `Kasr` obyektini qaytaradi. Yigʻindini va dastur davomida jami nechta `Kasr` obyekti yaratilganini chiqaring.

```python
from math import gcd


class Kasr:
    soni = 0                        # statik: barcha obyektlar uchun bitta

    def __init__(self, surat, maxraj):
        g = gcd(surat, maxraj)
        self.surat = surat // g
        self.maxraj = maxraj // g
        Kasr.soni += 1

    def qosh(self, boshqa):
        return Kasr(self.surat * boshqa.maxraj + boshqa.surat * self.maxraj,
                    self.maxraj * boshqa.maxraj)

    def __str__(self):
        return f"{self.surat}/{self.maxraj}"


a, b = map(int, input().split())
c, d = map(int, input().split())
x = Kasr(a, b)
y = Kasr(c, d)
print(x.qosh(y))
print(Kasr.soni)
```
```java
import java.util.Scanner;

public class Main {
    static class Kasr {
        static int soni = 0;        // statik: barcha obyektlar uchun bitta
        final long surat, maxraj;

        Kasr(long surat, long maxraj) {
            long g = ekub(surat, maxraj);
            this.surat = surat / g;
            this.maxraj = maxraj / g;
            soni++;
        }

        static long ekub(long a, long b) {
            return b == 0 ? a : ekub(b, a % b);
        }

        Kasr qosh(Kasr boshqa) {
            return new Kasr(surat * boshqa.maxraj + boshqa.surat * maxraj, maxraj * boshqa.maxraj);
        }

        @Override
        public String toString() {
            return surat + "/" + maxraj;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Kasr x = new Kasr(in.nextLong(), in.nextLong());
        Kasr y = new Kasr(in.nextLong(), in.nextLong());
        System.out.println(x.qosh(y));
        System.out.println(Kasr.soni);
    }
}
```
```cpp
#include <iostream>
#include <numeric>
using namespace std;

class Kasr {
public:
    inline static int soni = 0;     // statik: barcha obyektlar uchun bitta
    long long surat, maxraj;

    Kasr(long long s, long long m) {
        long long g = gcd(s, m);
        surat = s / g;
        maxraj = m / g;
        soni++;
    }

    Kasr qosh(const Kasr& boshqa) const {
        return Kasr(surat * boshqa.maxraj + boshqa.surat * maxraj, maxraj * boshqa.maxraj);
    }
};

int main() {
    long long a, b, c, d;
    cin >> a >> b >> c >> d;
    Kasr x(a, b), y(c, d);
    Kasr z = x.qosh(y);
    cout << z.surat << "/" << z.maxraj << endl;
    cout << Kasr::soni << endl;
    return 0;
}
```

`1 6` va `1 3` kiritilsa: 1/6 + 1/3 = 3/18 + 6/18 = 9/18 = 1/2. Chiqish `1/2` va `3`: ikkita kasr kiritildi, uchinchisi `qosh` ichida yaratildi. `soni` statik boʻlgani uchun uchala obyekt ham bitta hisoblagichni oshirdi. `qosh` mavjud obyektlarni oʻzgartirmaydi, **yangi** obyekt qaytaradi — shu sababli `x` va `y` oʻz qiymatida qoladi. C++'da `gcd` — `<numeric>` dagi tayyor funksiya (C++17), `inline static` esa statik maydonni sinf ichida boshlash imkonini beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Python metodida `self` ni unutish.** `def yuza():` deb yozilsa, `t.yuza()` chaqiruvi xato beradi; maydonga ham faqat `self.en` orqali murojaat qilinadi.
- **Java/C++'da `this` siz bir xil nom.** Konstruktorda `en = en;` parametrni oʻziga oʻzlashtiradi, maydon esa 0 boʻlib qoladi; `this.en = en` yozing.
- **Java'da `new` siz obyekt.** `Tortburchak t;` faqat havola eʼlon qiladi (`null`); obyekt `new` bilan yaratiladi.
- **Havola nusxa deb oʻylash.** Python/Java'da `b = a` yangi obyekt yaratmaydi; `b` ni oʻzgartirish `a` ni ham oʻzgartiradi.
:::

:::tip
Sinf loyihalashda avval „bu obyekt nimani **biladi**?“ (maydonlar) va „nima **qila oladi**?“ (metodlar) savollariga qogʻozda javob yozing, keyin kodga oʻting.
:::

## Xulosa

- Sinf — yangi tur tavsifi; obyekt — uning aniq nusxasi.
- Maydonlar obyekt maʼlumotini, metodlar uning amallarini ifodalaydi; konstruktor obyektni boshlangʻich holatga keltiradi.
- `self` / `this` — metod chaqirilgan obyektning oʻzi.
- Statik maydon sinfga tegishli va barcha obyektlar uchun bitta.
- Python va Java'da oʻzgaruvchi obyektga havola saqlaydi, C++'da oʻzlashtirish nusxa yaratadi.

## Oʻzingizni tekshiring

:::check
Q: Sinf va obyekt oʻrtasidagi farq nima?
A: Sinf — tur tavsifi (chizma), obyekt — shu sinfdan yaratilgan aniq qiymat; bitta sinfdan koʻp obyekt yaratish mumkin.
Q: Konstruktor qachon ishlaydi?
A: Obyekt yaratilayotganda, avtomatik ravishda bir marta.
Q: Python'da `a = Talaba("Ali", 1)`, `b = a`, `b.kurs = 2` dan keyin `a.kurs` nechaga teng?
A: 2 — `a` va `b` bitta obyektga havola.
Q: C++'da `Talaba b = a; b.kurs = 2;` dan keyin `a.kurs` oʻzgaradimi?
A: Yoʻq, `b` — `a` ning nusxasi.
Q: Statik maydon oddiy maydondan nimasi bilan farq qiladi?
A: U sinfga tegishli va barcha obyektlar uchun bitta; oddiy maydon har bir obyektda alohida.
:::

:::link https://docs.python.org/3/tutorial/classes.html
Python: sinflar
Sinf, obyekt, `__init__` va metodlar haqida rasmiy qoʻllanma (ingliz tilida).
:::

:::link https://dev.java/learn/classes-objects/
Java: sinflar va obyektlar
Java'da sinf, konstruktor va `this` boʻyicha rasmiy oʻquv resursi (ingliz tilida).
:::
