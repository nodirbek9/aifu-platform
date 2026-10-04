---
title: Inkapsulyatsiya va merosxoʻrlik. Polimorfizm
summary: OYD ning uch tamoyili; yopiq maydonlar, getter va setter; voris sinf, super va konstruktorlar zanjiri; metodni qayta aniqlash, virtual metodlar va bitta roʻyxatda turli obyektlar; abstrakt sinf.
---

## Nima uchun bu mavzu muhim?

Oldingi ikki maʼruzada sinf yozdik va obyektlarni massivda saqladik. Lekin sinflarimizning maydonlari hammaga ochiq edi: istalgan kod talabaga `ball = -500` yozib qoʻyishi mumkin edi. Bundan tashqari, oʻxshash sinflar (masalan, oddiy xodim va menejer) uchun bir xil kodni ikki marta yozishga toʻgʻri kelardi.

OYD bu muammolarni uchta tamoyil bilan hal qiladi: **inkapsulyatsiya** obyektni notoʻgʻri holatdan himoya qiladi, **merosxoʻrlik** umumiy kodni bir joyda saqlaydi, **polimorfizm** esa turli obyektlar bilan bir xil usulda ishlash imkonini beradi. Bu uch tamoyil har qanday katta dasturning — mobil ilovadan tortib bank tizimigacha — asosida turadi.

:::note
**Bu darsdan soʻng siz:**

- OYD ning uchta tamoyilini oʻz soʻzingiz bilan tushuntira olasiz;
- maydonlarni yopib, ularni getter va tekshiruvchi setter orqali boshqara olasiz;
- voris sinf yozib, ota sinf konstruktori va metodlarini `super` orqali chaqira olasiz;
- metodni qayta aniqlab (override), polimorfizmni bitta roʻyxatdagi turli obyektlarda qoʻllay olasiz;
- C++'da `virtual` nima uchun kerakligini va abstrakt sinf nima ekanini tushuntira olasiz.
:::

## Inkapsulyatsiya

**Inkapsulyatsiya** — obyektning ichki maʼlumotini tashqaridan yashirish va unga faqat sinf metodlari orqali kirishga ruxsat berish. Shunda obyekt har doim **toʻgʻri holatda** qoladi: tekshiruvni metod bajaradi.

| Til | Yopiq maydon | Ochiq metod |
|---|---|---|
| Java | `private int ball;` | `public int getBall()`, `public void setBall(int b)` |
| C++ | `private:` boʻlimida (`class` da sukut boʻyicha) | `public:` boʻlimida |
| Python | kelishuv: `self._ball` (bitta pastki chiziq) — „ichki, tegmang“ | oddiy metod yoki `@property` |

**Getter** maydon qiymatini qaytaradi, **setter** esa yangi qiymatni **tekshirib** oʻrnatadi. Pythonda `private` kalit soʻzi yoʻq: pastki chiziq — dasturchilar orasidagi kelishuv, til uni majburlamaydi.

Kirish huquqlari (Java va C++):

| Modifikator | Kim koʻradi |
|---|---|
| `private` | faqat sinfning oʻzi |
| `protected` | sinf va uning vorislari (Java'da yana shu paket) |
| `public` | hamma |

## Merosxoʻrlik

**Merosxoʻrlik** (inheritance) — mavjud sinf (**ota**, base) asosida yangi sinf (**voris**, derived) yaratish. Voris otaning barcha maydon va metodlarini oladi va oʻzinikini qoʻshadi. Bu „**…dir**“ munosabati: menejer — bu xodim, doira — bu shakl.

| | Python | Java | C++ |
|---|---|---|---|
| Voris eʼlon qilish | `class Menejer(Xodim):` | `class Menejer extends Xodim` | `class Menejer : public Xodim` |
| Ota konstruktori | `super().__init__(ism, maosh)` | `super(ism, maosh);` (birinchi qator) | `: Xodim(ism, maosh)` (initsializatsiya roʻyxatida) |
| Ota metodi | `super().oylik()` | `super.oylik()` | `Xodim::oylik()` |

Voris obyekt yaratilganda **avval ota qismi**, keyin voris qismi quriladi — shuning uchun ota konstruktori birinchi chaqiriladi.

## Polimorfizm

**Polimorfizm** („koʻp shakllilik“) — bir xil chaqiruv obyektning haqiqiy turiga qarab turlicha ishlashi. Voris otaning metodini **qayta aniqlaydi** (override): nomi va parametrlari bir xil, mazmuni boshqa.

Polimorfizmning kuchi shundaki, turli voris obyektlarni **ota turidagi** bitta roʻyxatda saqlab, hammasiga bir xil metodni chaqirish mumkin — har biri oʻz versiyasini bajaradi:

```text
shakllar = [Doira(2), Tortburchak(3, 4), Uchburchak(6, 2)]
har bir s uchun: s.yuza()     → har biri oʻz formulasi bilan
```

Tillardagi farq:

- **Python va Java**da barcha metodlar sukut boʻyicha polimorf. Java'da `@Override` izohi xatoni ushlaydi: metod nomi notoʻgʻri yozilsa, kompilyator ogohlantiradi.
- **C++**da metod ota sinfda `virtual` deb eʼlon qilinishi, obyektlar esa **koʻrsatkich** yoki **havola** orqali ishlatilishi kerak. Aks holda har doim ota versiyasi chaqiriladi. Vorisda `override` yozish tavsiya etiladi.

**Abstrakt sinf** — oʻzidan obyekt yaratib boʻlmaydigan, faqat vorislar uchun shablon boʻlgan sinf. Uning abstrakt metodlari tanasiz boʻladi va har bir voris ularni albatta aniqlashi kerak: Java'da `abstract`, C++'da `virtual double yuza() const = 0;` (sof virtual metod), Python'da `abc` modulidagi `@abstractmethod`.

## Ishlangan misol 1: harorat sinfi (inkapsulyatsiya)

Birinchi satrda *n*, soʻng *n* ta butun son — Selsiy boʻyicha harorat. `Harorat` sinfida qiymat yopiq maydonda saqlanadi; setter absolyut noldan (−273 °C) past qiymatni rad etadi va eski qiymat saqlanib qoladi. Har bir son uchun setterni chaqiring: qabul qilinsa, haroratni Farengeytda (bitta kasr xona bilan), rad etilsa `Xato` chiqaring.

```python
class Harorat:
    def __init__(self):
        self._selsiy = 0                  # ichki maydon: tashqaridan tegilmaydi

    def selsiy(self):
        return self._selsiy

    def ornat(self, qiymat):
        if qiymat < -273:
            return False                  # notoʻgʻri qiymat obyektga kirmaydi
        self._selsiy = qiymat
        return True

    def farengeyt(self):
        return self._selsiy * 9 / 5 + 32


n = int(input())
h = Harorat()
for _ in range(n):
    c = int(input())
    if h.ornat(c):
        print(f"{h.farengeyt():.1f}")
    else:
        print("Xato")
```
```java
import java.util.Scanner;

public class Main {
    static class Harorat {
        private int selsiy = 0;           // ichki maydon: tashqaridan tegilmaydi

        public int getSelsiy() {
            return selsiy;
        }

        public boolean setSelsiy(int qiymat) {
            if (qiymat < -273) return false;   // notoʻgʻri qiymat obyektga kirmaydi
            selsiy = qiymat;
            return true;
        }

        public double farengeyt() {
            return selsiy * 9 / 5.0 + 32;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Harorat h = new Harorat();
        for (int i = 0; i < n; i++) {
            if (h.setSelsiy(in.nextInt())) {
                System.out.printf("%.1f%n", h.farengeyt());
            } else {
                System.out.println("Xato");
            }
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

class Harorat {
private:
    int selsiy = 0;                       // ichki maydon: tashqaridan tegilmaydi

public:
    int getSelsiy() const { return selsiy; }

    bool setSelsiy(int qiymat) {
        if (qiymat < -273) return false;  // notoʻgʻri qiymat obyektga kirmaydi
        selsiy = qiymat;
        return true;
    }

    double farengeyt() const { return selsiy * 9 / 5.0 + 32; }
};

int main() {
    int n;
    cin >> n;
    Harorat h;
    for (int i = 0; i < n; i++) {
        int c;
        cin >> c;
        if (h.setSelsiy(c)) {
            printf("%.1f\n", h.farengeyt());
        } else {
            printf("Xato\n");
        }
    }
    return 0;
}
```

Kirish:

```text
4
25
-300
-40
37
```

Chiqish:

```text
77.0
Xato
-40.0
98.6
```

`-300` rad etildi va obyekt oldingi qiymatini (25) saqlab qoldi. Java va C++'da `h.selsiy = -300;` deb yozib boʻlmaydi — kompilyator `private` maydonga kirishni taqiqlaydi. Formulada `9 / 5.0` yozilgan: `9 / 5` butun boʻlish boʻlib, 1 ni berardi.

## Ishlangan misol 2: xodim va menejer (merosxoʻrlik)

Birinchi satrda *n*, soʻng *n* satr: `xodim ISM MAOSH` yoki `menejer ISM MAOSH FOIZ`. Menejer — xodim, lekin uning oyligiga maoshning FOIZ foizi miqdorida mukofot qoʻshiladi. Har bir xodimning ismi va oyligini, oxirida esa jami oylik fondini chiqaring.

```python
class Xodim:
    def __init__(self, ism, maosh):
        self.ism = ism
        self.maosh = maosh

    def oylik(self):
        return self.maosh


class Menejer(Xodim):
    def __init__(self, ism, maosh, foiz):
        super().__init__(ism, maosh)      # ota qismini quramiz
        self.foiz = foiz

    def oylik(self):                      # qayta aniqlash
        return super().oylik() + self.maosh * self.foiz // 100


n = int(input())
xodimlar = []
for _ in range(n):
    q = input().split()
    if q[0] == "menejer":
        xodimlar.append(Menejer(q[1], int(q[2]), int(q[3])))
    else:
        xodimlar.append(Xodim(q[1], int(q[2])))

fond = 0
for x in xodimlar:
    print(x.ism, x.oylik())
    fond += x.oylik()
print(fond)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Xodim {
        protected String ism;
        protected long maosh;

        Xodim(String ism, long maosh) {
            this.ism = ism;
            this.maosh = maosh;
        }

        long oylik() {
            return maosh;
        }
    }

    static class Menejer extends Xodim {
        private final long foiz;

        Menejer(String ism, long maosh, long foiz) {
            super(ism, maosh);            // ota qismini quramiz
            this.foiz = foiz;
        }

        @Override
        long oylik() {                    // qayta aniqlash
            return super.oylik() + maosh * foiz / 100;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Xodim> xodimlar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String tur = in.next();
            if (tur.equals("menejer")) {
                xodimlar.add(new Menejer(in.next(), in.nextLong(), in.nextLong()));
            } else {
                xodimlar.add(new Xodim(in.next(), in.nextLong()));
            }
        }

        long fond = 0;
        for (Xodim x : xodimlar) {
            System.out.println(x.ism + " " + x.oylik());
            fond += x.oylik();
        }
        System.out.println(fond);
    }
}
```
```cpp
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Xodim {
protected:
    string ism;
    long long maosh;

public:
    Xodim(string ism, long long maosh) : ism(ism), maosh(maosh) {}
    virtual ~Xodim() = default;

    string getIsm() const { return ism; }
    virtual long long oylik() const { return maosh; }
};

class Menejer : public Xodim {
    long long foiz;

public:
    Menejer(string ism, long long maosh, long long foiz)
        : Xodim(ism, maosh), foiz(foiz) {}   // ota qismini quramiz

    long long oylik() const override {       // qayta aniqlash
        return Xodim::oylik() + maosh * foiz / 100;
    }
};

int main() {
    int n;
    cin >> n;
    vector<unique_ptr<Xodim>> xodimlar;
    for (int i = 0; i < n; i++) {
        string tur, ism;
        long long maosh;
        cin >> tur >> ism >> maosh;
        if (tur == "menejer") {
            long long foiz;
            cin >> foiz;
            xodimlar.push_back(make_unique<Menejer>(ism, maosh, foiz));
        } else {
            xodimlar.push_back(make_unique<Xodim>(ism, maosh));
        }
    }

    long long fond = 0;
    for (const auto& x : xodimlar) {
        cout << x->getIsm() << " " << x->oylik() << endl;
        fond += x->oylik();
    }
    cout << fond << endl;
    return 0;
}
```

Kirish:

```text
3
xodim Aziz 5000000
menejer Malika 8000000 25
xodim Bekzod 4500000
```

Chiqish:

```text
Aziz 5000000
Malika 10000000
Bekzod 4500000
19500000
```

`Menejer` ism va maosh maydonlarini qayta yozmadi — ularni `Xodim` dan meros oldi. `oylik()` ichida ota versiyasi chaqirildi (`super().oylik()`, `super.oylik()`, `Xodim::oylik()`) va unga mukofot qoʻshildi. Maydonlar `protected`: vorisga koʻrinadi, tashqi kodga emas. C++'da turli turdagi obyektlarni bitta `vector` da saqlash uchun **koʻrsatkich** kerak; `unique_ptr` — obyektni avtomatik oʻchiradigan „aqlli“ koʻrsatkich (dinamik xotira keyingi mavzularda batafsil). Ota sinfdagi `virtual ~Xodim()` voris obyekt toʻgʻri oʻchirilishini taʼminlaydi.

## Ishlangan misol 3: shakllar (polimorfizm va abstrakt sinf)

Birinchi satrda *n*, soʻng *n* satr: `doira R`, `tortburchak A B` yoki `uchburchak ASOS BALANDLIK`. Har bir shaklning turini va yuzini (ikki kasr xona bilan), oxirida eng katta yuzali shakl turini chiqaring. `Shakl` — abstrakt sinf.

```python
from abc import ABC, abstractmethod
from math import pi


class Shakl(ABC):
    @abstractmethod
    def yuza(self):
        pass

    @abstractmethod
    def nom(self):
        pass


class Doira(Shakl):
    def __init__(self, r):
        self.r = r

    def yuza(self):
        return pi * self.r * self.r

    def nom(self):
        return "doira"


class Tortburchak(Shakl):
    def __init__(self, a, b):
        self.a, self.b = a, b

    def yuza(self):
        return self.a * self.b

    def nom(self):
        return "tortburchak"


class Uchburchak(Shakl):
    def __init__(self, asos, h):
        self.asos, self.h = asos, h

    def yuza(self):
        return self.asos * self.h / 2

    def nom(self):
        return "uchburchak"


n = int(input())
shakllar = []
for _ in range(n):
    q = input().split()
    if q[0] == "doira":
        shakllar.append(Doira(float(q[1])))
    elif q[0] == "tortburchak":
        shakllar.append(Tortburchak(float(q[1]), float(q[2])))
    else:
        shakllar.append(Uchburchak(float(q[1]), float(q[2])))

eng = shakllar[0]
for s in shakllar:
    print(f"{s.nom()} {s.yuza():.2f}")    # qaysi yuza() — obyektning turi hal qiladi
    if s.yuza() > eng.yuza():
        eng = s
print(eng.nom())
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    abstract static class Shakl {
        abstract double yuza();
        abstract String nom();
    }

    static class Doira extends Shakl {
        final double r;
        Doira(double r) { this.r = r; }
        @Override double yuza() { return Math.PI * r * r; }
        @Override String nom() { return "doira"; }
    }

    static class Tortburchak extends Shakl {
        final double a, b;
        Tortburchak(double a, double b) { this.a = a; this.b = b; }
        @Override double yuza() { return a * b; }
        @Override String nom() { return "tortburchak"; }
    }

    static class Uchburchak extends Shakl {
        final double asos, h;
        Uchburchak(double asos, double h) { this.asos = asos; this.h = h; }
        @Override double yuza() { return asos * h / 2; }
        @Override String nom() { return "uchburchak"; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Shakl> shakllar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String tur = in.next();
            if (tur.equals("doira")) shakllar.add(new Doira(in.nextDouble()));
            else if (tur.equals("tortburchak")) shakllar.add(new Tortburchak(in.nextDouble(), in.nextDouble()));
            else shakllar.add(new Uchburchak(in.nextDouble(), in.nextDouble()));
        }

        Shakl eng = shakllar.get(0);
        for (Shakl s : shakllar) {
            System.out.printf("%s %.2f%n", s.nom(), s.yuza());   // qaysi yuza() — obyektning turi hal qiladi
            if (s.yuza() > eng.yuza()) eng = s;
        }
        System.out.println(eng.nom());
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Shakl {
public:
    virtual ~Shakl() = default;
    virtual double yuza() const = 0;      // sof virtual: Shakl — abstrakt
    virtual string nom() const = 0;
};

class Doira : public Shakl {
    double r;
public:
    Doira(double r) : r(r) {}
    double yuza() const override { return M_PI * r * r; }
    string nom() const override { return "doira"; }
};

class Tortburchak : public Shakl {
    double a, b;
public:
    Tortburchak(double a, double b) : a(a), b(b) {}
    double yuza() const override { return a * b; }
    string nom() const override { return "tortburchak"; }
};

class Uchburchak : public Shakl {
    double asos, h;
public:
    Uchburchak(double asos, double h) : asos(asos), h(h) {}
    double yuza() const override { return asos * h / 2; }
    string nom() const override { return "uchburchak"; }
};

int main() {
    int n;
    cin >> n;
    vector<unique_ptr<Shakl>> shakllar;
    for (int i = 0; i < n; i++) {
        string tur;
        double x, y;
        cin >> tur >> x;
        if (tur == "doira") {
            shakllar.push_back(make_unique<Doira>(x));
        } else {
            cin >> y;
            if (tur == "tortburchak") shakllar.push_back(make_unique<Tortburchak>(x, y));
            else shakllar.push_back(make_unique<Uchburchak>(x, y));
        }
    }

    const Shakl* eng = shakllar[0].get();
    for (const auto& s : shakllar) {
        printf("%s %.2f\n", s->nom().c_str(), s->yuza());   // qaysi yuza() — obyektning turi hal qiladi
        if (s->yuza() > eng->yuza()) eng = s.get();
    }
    printf("%s\n", eng->nom().c_str());
    return 0;
}
```

Kirish:

```text
3
doira 2
tortburchak 3 4
uchburchak 6 5
```

Chiqish:

```text
doira 12.57
tortburchak 12.00
uchburchak 15.00
uchburchak
```

Asosiy sikl faqat `Shakl` haqida biladi: unda `if (tur == "doira")` kabi tekshiruvlar yoʻq. Ertaga `Kvadrat` sinfi qoʻshilsa, siklni oʻzgartirish shart emas — bu polimorfizmning asosiy foydasi. `Shakl` abstrakt boʻlgani uchun `Shakl()` (Python), `new Shakl()` (Java) yoki `Shakl s;` (C++) xato beradi. Vorisdagi `override` kalit soʻzi ota sinfda aynan shunday virtual metod borligini kompilyatorga tekshirtiradi: nomda yoki parametrlarda xato boʻlsa, dastur kompilyatsiya boʻlmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Setterda tekshiruv yoʻq.** `setBall(int b) { ball = b; }` — maydonni ochiq qoldirish bilan bir xil; inkapsulyatsiyaning maʼnosi tekshiruvda.
- **Java'da `super(...)` ni birinchi qatorga yozmaslik.** Ota konstruktori voris konstruktorining birinchi buyrugʻi boʻlishi shart.
- **C++'da `virtual` siz qayta aniqlash.** Koʻrsatkich orqali chaqirilganda ota versiyasi ishlaydi; `virtual` va `override` yozing.
- **C++'da voris obyektni ota turidagi qiymatga nusxalash.** `Xodim x = menejer;` mukofot qismini „kesib tashlaydi“ (slicing); koʻrsatkich yoki havoladan foydalaning.
- **Merosxoʻrlikni „…dir“ boʻlmagan joyda qoʻllash.** Mashina dvigatelga ega, lekin u dvigatel *emas* — bunda dvigatel maydon boʻladi, ota sinf emas.
:::

:::tip
Yangi sinf ota sinfdan meros olishi kerakmi, degan savolga „X — bu Y mi?“ deb oʻzingizdan soʻrang. „Ha“ boʻlsa — merosxoʻrlik, „X da Y bor“ boʻlsa — maydon (kompozitsiya).
:::

## Xulosa

- Inkapsulyatsiya: maydonlar yopiq, oʻzgartirish tekshiruvchi metodlar orqali; obyekt doim toʻgʻri holatda.
- Merosxoʻrlik: voris otaning maydon va metodlarini oladi; ota konstruktori `super` orqali birinchi chaqiriladi.
- Polimorfizm: voris metodni qayta aniqlaydi; ota turidagi roʻyxatda har bir obyekt oʻz versiyasini bajaradi.
- C++'da polimorfizm uchun `virtual` metod va koʻrsatkich/havola kerak; ota sinfda virtual destruktor yoziladi.
- Abstrakt sinfdan obyekt yaratilmaydi; u vorislar bajarishi shart boʻlgan metodlarni belgilaydi.

## Oʻzingizni tekshiring

:::check
Q: Inkapsulyatsiya obyektni nimadan himoya qiladi?
A: Notoʻgʻri holatdan: maydonlar faqat tekshiruv bajaradigan metodlar orqali oʻzgaradi.
Q: `protected` maydonni kim koʻradi?
A: Sinfning oʻzi va uning vorislari (Java'da shu paketdagi sinflar ham).
Q: Voris obyekt yaratilganda qaysi konstruktor birinchi ishlaydi?
A: Ota sinf konstruktori, keyin voris konstruktori.
Q: C++'da ota sinfdagi metod `virtual` boʻlmasa, `Xodim* x = new Menejer(...); x->oylik()` qaysi versiyani chaqiradi?
A: Ota sinf (`Xodim`) versiyasini.
Q: Abstrakt sinfdan obyekt yaratish mumkinmi?
A: Yoʻq; faqat uning barcha abstrakt metodlarini aniqlagan vorislardan obyekt yaratiladi.
:::

:::link https://dev.java/learn/inheritance/
Java: merosxoʻrlik
Voris sinflar, `super`, qayta aniqlash va abstrakt sinflar haqida rasmiy oʻquv resursi (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/language/virtual
C++: virtual funksiyalar
`virtual`, `override` va sof virtual funksiyalar boʻyicha maʼlumotnoma (ingliz tilida).
:::
