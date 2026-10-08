---
title: Komponentalar bilan ishlash
summary: Dasturiy komponent tushunchasi; interfeys — komponent shartnomasi, almashtiriladigan komponentlar; xususiyatlar, metodlar va hodisalar; hodisaga javob beruvchi funksiyalar (callback, listener); grafik interfeys komponentlari va ularni joylashtirish; kompozitsiya.
---

## Nima uchun bu mavzu muhim?

Zamonaviy dastur bir butun kod boʻlagi emas, balki bir-biriga ulanadigan **komponentlar**dan yigʻiladi: tugma, matn maydoni, toʻlov moduli, maʼlumotlar bazasiga ulanish, xabar yuboruvchi xizmat. Har bir komponent oʻz ishini biladi va boshqalar bilan faqat aniq belgilangan **interfeys** orqali gaplashadi. Shu tufayli komponentni almashtirish (masalan, bir toʻlov tizimini boshqasiga) dasturning qolgan qismini oʻzgartirmasdan amalga oshiriladi.

Visual Studio, IntelliJ yoki Qt Creator kabi muhitlarda grafik ilova shu tamoyil asosida quriladi: oynaga tayyor komponentlar (tugma, roʻyxat, jadval) tashlanadi, ularning **xususiyatlari** sozlanadi va **hodisalari**ga javob beruvchi funksiyalar yoziladi. Bu maʼruzada komponentli dasturlashning asosiy gʻoyalarini uch tilda koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- dasturiy komponent va uning interfeysi nima ekanini tushuntira olasiz;
- interfeys orqali almashtiriladigan komponentlar yoza olasiz;
- komponentning xususiyatlari, metodlari va hodisalarini farqlay olasiz;
- hodisaga javob beruvchi funksiyalarni (callback, listener) roʻyxatga olib, chaqira olasiz;
- murakkab komponentni oddiylaridan kompozitsiya orqali yigʻa olasiz.
:::

## Komponent va interfeys

**Komponent** — aniq vazifali, mustaqil almashtiriladigan dastur boʻlagi. Uning tashqi dunyo bilan aloqasi **interfeys** — qanday metodlar borligi va ular nima qilishi haqidagi shartnoma — orqali boʻladi. Ichki tuzilishi esa yashirin (inkapsulyatsiya).

| Til | Interfeys | Amalga oshirish |
|---|---|---|
| Java | `interface Chegirma { long hisobla(long summa); }` | `class Foizli implements Chegirma` |
| C++ | sof virtual metodli sinf: `virtual long long hisobla(long long) const = 0;` | `class Foizli : public Chegirma` |
| Python | `abc.ABC` va `@abstractmethod` yoki shunchaki bir xil nomli metodlar | `class Foizli(Chegirma)` |

Interfeysga qarab yozilgan kod („buyurtma summasini chegirma komponenti yordamida hisobla“) qaysi komponent ulanganini bilmaydi — shuning uchun yangi chegirma turi qoʻshilsa, u oʻzgarmaydi.

## Xususiyatlar, metodlar va hodisalar

Grafik interfeys komponentlari (tugma, matn maydoni, belgi) uch xil aʼzoga ega:

| Aʼzo | Maʼnosi | Tugma uchun misol |
|---|---|---|
| Xususiyat (property) | komponent holati | `Text = "Saqlash"`, `Enabled = false`, `Width = 120` |
| Metod | komponentga buyruq | `Focus()`, `Show()`, `Hide()` |
| Hodisa (event) | komponentda biror narsa yuz berdi | `Click`, `MouseEnter`, `TextChanged` |

**Hodisaga asoslangan dasturlash**da dastur oʻzi „buyruq bermaydi“, balki hodisalarni kutadi: foydalanuvchi tugmani bosganda komponent unga **roʻyxatdan oʻtgan** barcha funksiyalarni chaqiradi. Bunday funksiya **callback** yoki **listener** (tinglovchi) deyiladi.

| Til / kutubxona | Hodisaga javob beruvchini ulash |
|---|---|
| C# Windows Forms | `button1.Click += Button1_Click;` |
| Java Swing | `tugma.addActionListener(e -> saqlash());` |
| Python Tkinter | `Button(oyna, text="Saqlash", command=saqlash)` |
| C++ Qt | `connect(tugma, &QPushButton::clicked, this, &Oyna::saqlash);` |

Callbacklarni saqlash uchun tillarda funksiya qiymat sifatida ishlatiladi: Javaʼda lambda va funksional interfeys (`Runnable`, `Consumer<T>`), C++'da `std::function`, Pythonʼda oddiy funksiya yoki `lambda`.

## Grafik interfeys namunasi

Masalan, Python Tkinterʼda bitta oyna, matn maydoni va tugmadan iborat dastur shunday yoziladi (grafik muhit kerak boʻlgani uchun avtomatik tekshiruvda ishga tushirilmaydi):

```text
import tkinter as tk

def salomlash():
    belgi.config(text="Salom, " + maydon.get() + "!")   # xususiyatni oʻzgartirish

oyna = tk.Tk()
maydon = tk.Entry(oyna)
tugma = tk.Button(oyna, text="Salomlash", command=salomlash)   # hodisaga ulash
belgi = tk.Label(oyna, text="")
for komponent in (maydon, tugma, belgi):
    komponent.pack()                                   # joylashtirish
oyna.mainloop()                                        # hodisalar sikli
```

`mainloop()` — **hodisalar sikli**: dastur shu yerda foydalanuvchi harakatlarini kutib turadi va har bir hodisa uchun tegishli callbackni chaqiradi. Barcha grafik kutubxonalar shu sxema boʻyicha ishlaydi.

## Kompozitsiya

Murakkab komponent oddiylaridan **yigʻiladi**: oyna panellardan, panel tugma va maydonlardan iborat. Bu **kompozitsiya** — „…da … bor“ munosabati (oynada tugma bor). Merosxoʻrlikdan farqli ravishda, kompozitsiyada komponent boshqasining ichki tuzilishiga bogʻlanmaydi va ularni dastur ishlayotganda ham almashtirish mumkin. Dasturchilar orasida mashhur qoida: „merosxoʻrlikdan koʻra kompozitsiyani afzal koʻring“.

## Ishlangan misol 1: almashtiriladigan chegirma komponenti

Birinchi satrda chegirma turi: `yoq`, `foiz P` yoki `qatiy S` (summadan P foiz yoki S soʻm, lekin summa manfiy boʻlmaydi). Ikkinchi satrda *n*, uchinchisida *n* ta buyurtma summasi. Har bir buyurtmaning yakuniy summasini chiqaring. Hisoblovchi kod faqat `Chegirma` interfeysini biladi.

```python
from abc import ABC, abstractmethod


class Chegirma(ABC):
    @abstractmethod
    def hisobla(self, summa):
        pass


class Yoq(Chegirma):
    def hisobla(self, summa):
        return summa


class Foizli(Chegirma):
    def __init__(self, foiz):
        self.foiz = foiz

    def hisobla(self, summa):
        return summa - summa * self.foiz // 100


class Qatiy(Chegirma):
    def __init__(self, miqdor):
        self.miqdor = miqdor

    def hisobla(self, summa):
        return max(summa - self.miqdor, 0)


def chegirma_yarat(satr):                  # qaysi komponent ulanishini shu yer hal qiladi
    q = satr.split()
    if q[0] == "foiz":
        return Foizli(int(q[1]))
    if q[0] == "qatiy":
        return Qatiy(int(q[1]))
    return Yoq()


chegirma = chegirma_yarat(input())
n = int(input())
for summa in map(int, input().split()):
    print(chegirma.hisobla(summa))         # bu qator turini bilmaydi
```
```java
import java.util.Scanner;

public class Main {
    interface Chegirma {
        long hisobla(long summa);
    }

    static class Yoq implements Chegirma {
        public long hisobla(long summa) { return summa; }
    }

    static class Foizli implements Chegirma {
        private final long foiz;
        Foizli(long foiz) { this.foiz = foiz; }
        public long hisobla(long summa) { return summa - summa * foiz / 100; }
    }

    static class Qatiy implements Chegirma {
        private final long miqdor;
        Qatiy(long miqdor) { this.miqdor = miqdor; }
        public long hisobla(long summa) { return Math.max(summa - miqdor, 0); }
    }

    static Chegirma chegirmaYarat(String satr) {   // qaysi komponent ulanishini shu yer hal qiladi
        String[] q = satr.trim().split("\\s+");
        if (q[0].equals("foiz")) return new Foizli(Long.parseLong(q[1]));
        if (q[0].equals("qatiy")) return new Qatiy(Long.parseLong(q[1]));
        return new Yoq();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Chegirma chegirma = chegirmaYarat(in.nextLine());
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            System.out.println(chegirma.hisobla(in.nextLong()));   // bu qator turini bilmaydi
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <memory>
#include <sstream>
#include <string>
using namespace std;

class Chegirma {
public:
    virtual ~Chegirma() = default;
    virtual long long hisobla(long long summa) const = 0;
};

class Yoq : public Chegirma {
public:
    long long hisobla(long long summa) const override { return summa; }
};

class Foizli : public Chegirma {
    long long foiz;
public:
    Foizli(long long foiz) : foiz(foiz) {}
    long long hisobla(long long summa) const override { return summa - summa * foiz / 100; }
};

class Qatiy : public Chegirma {
    long long miqdor;
public:
    Qatiy(long long miqdor) : miqdor(miqdor) {}
    long long hisobla(long long summa) const override { return max(summa - miqdor, 0LL); }
};

unique_ptr<Chegirma> chegirmaYarat(const string& satr) {   // qaysi komponent ulanishini shu yer hal qiladi
    istringstream oqim(satr);
    string tur;
    long long qiymat = 0;
    oqim >> tur >> qiymat;
    if (tur == "foiz") return make_unique<Foizli>(qiymat);
    if (tur == "qatiy") return make_unique<Qatiy>(qiymat);
    return make_unique<Yoq>();
}

int main() {
    string satr;
    getline(cin, satr);
    unique_ptr<Chegirma> chegirma = chegirmaYarat(satr);
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        long long summa;
        cin >> summa;
        cout << chegirma->hisobla(summa) << endl;   // bu qator turini bilmaydi
    }
    return 0;
}
```

Kirish:

```text
foiz 15
3
100000 250000 40000
```

Chiqish:

```text
85000
212500
34000
```

Asosiy sikl faqat `hisobla` metodini chaqiradi. Ertaga „ikkinchi buyurtmaga 50 %“ kabi yangi chegirma kerak boʻlsa, yangi sinf yoziladi va `chegirma_yarat` ga bitta qator qoʻshiladi — sikl oʻzgarmaydi. Komponentni yaratish bir joyda (bu yerda `chegirma_yarat`), ishlatish esa boshqa joyda — bu komponentli dizaynning asosiy qoidasi. `qatiy 50000` bilan 40000 summa 0 boʻladi, manfiy emas.

## Ishlangan misol 2: hodisalar va tinglovchilar

`Tugma` komponentiga bir nechta tinglovchi ulanadi: hisoblagich (necha marta bosilgan), jurnal (har bir bosishni yozib boradi) va ogohlantiruvchi (3-bosishdan keyin `Ehtiyot boʻling!` deydi). Kirishda *n* ta hodisa: `bos ISM` (ISM nomli foydalanuvchi bosdi) yoki `holat`. `bos` uchun tinglovchilar ulanish tartibida chaqiriladi; `holat` uchun jami bosishlar soni chiqariladi.

```python
class Tugma:
    def __init__(self, matn):
        self.matn = matn                    # xususiyat
        self._tinglovchilar = []

    def bosilganda(self, funksiya):         # hodisaga javob beruvchini ulash
        self._tinglovchilar.append(funksiya)

    def bos(self, kim):                     # hodisa yuz berdi
        for funksiya in self._tinglovchilar:
            funksiya(kim)


soni = 0


def hisoblagich(kim):
    global soni
    soni += 1


def jurnal(kim):
    print(f"{kim}: {tugma.matn}")


def ogohlantirish(kim):
    if soni > 3:
        print("Ehtiyot boʻling!")


tugma = Tugma("Saqlash")
tugma.bosilganda(hisoblagich)
tugma.bosilganda(jurnal)
tugma.bosilganda(ogohlantirish)

n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "bos":
        tugma.bos(q[1])
    else:
        print(soni)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class Main {
    static class Tugma {
        final String matn;                                  // xususiyat
        private final List<Consumer<String>> tinglovchilar = new ArrayList<>();

        Tugma(String matn) { this.matn = matn; }

        void bosilganda(Consumer<String> funksiya) {        // hodisaga javob beruvchini ulash
            tinglovchilar.add(funksiya);
        }

        void bos(String kim) {                              // hodisa yuz berdi
            for (Consumer<String> funksiya : tinglovchilar) funksiya.accept(kim);
        }
    }

    static int soni = 0;

    public static void main(String[] args) {
        Tugma tugma = new Tugma("Saqlash");
        tugma.bosilganda(kim -> soni++);
        tugma.bosilganda(kim -> System.out.println(kim + ": " + tugma.matn));
        tugma.bosilganda(kim -> {
            if (soni > 3) System.out.println("Ehtiyot boʻling!");
        });

        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].equals("bos")) tugma.bos(q[1]);
            else System.out.println(soni);
        }
    }
}
```
```cpp
#include <functional>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class Tugma {
    vector<function<void(const string&)>> tinglovchilar;

public:
    string matn;                                            // xususiyat

    Tugma(string matn) : matn(matn) {}

    void bosilganda(function<void(const string&)> funksiya) {   // hodisaga javob beruvchini ulash
        tinglovchilar.push_back(funksiya);
    }

    void bos(const string& kim) {                           // hodisa yuz berdi
        for (auto& funksiya : tinglovchilar) funksiya(kim);
    }
};

int main() {
    int soni = 0;
    Tugma tugma("Saqlash");
    tugma.bosilganda([&](const string&) { soni++; });
    tugma.bosilganda([&](const string& kim) { cout << kim << ": " << tugma.matn << endl; });
    tugma.bosilganda([&](const string&) {
        if (soni > 3) cout << "Ehtiyot boʻling!" << endl;
    });

    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "bos") {
            string kim;
            cin >> kim;
            tugma.bos(kim);
        } else {
            cout << soni << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
6
bos Aziz
bos Malika
holat
bos Aziz
bos Bekzod
holat
```

Chiqish:

```text
Aziz: Saqlash
Malika: Saqlash
2
Aziz: Saqlash
Bekzod: Saqlash
Ehtiyot boʻling!
4
```

`Tugma` oʻzi hech narsa hisoblamaydi va chiqarmaydi — u faqat „meni bosishdi“ deb ulangan funksiyalarni chaqiradi. Yangi xatti-harakat kerak boʻlsa, tugma sinfi oʻzgarmaydi, faqat yangi tinglovchi ulanadi. C++'da `[&]` — lambda tashqi oʻzgaruvchilarni (`soni`, `tugma`) havola orqali ushlaydi; Javaʼda lambda faqat sinf maydonini (`static int soni`) oʻzgartira oladi, lokal oʻzgaruvchini emas.

## Ishlangan misol 3: kompozitsiya — oddiy forma

Kirishda *n* ta komponent tavsifi: `belgi MATN`, `maydon NOM KENGLIK` yoki `tugma MATN`. Ulardan `Forma` komponentini yigʻing va uni matn koʻrinishida chizing: forma ramka bilan oʻraladi, ichida har bir komponent alohida satrda. Belgi — matnning oʻzi, maydon — `NOM: [____]` (KENGLIK ta pastki chiziq), tugma — `< MATN >`. Ramka kengligi eng uzun satrga qarab tanlanadi.

```python
class Belgi:
    def __init__(self, matn):
        self.matn = matn

    def chiz(self):
        return self.matn


class Maydon:
    def __init__(self, nom, kenglik):
        self.nom, self.kenglik = nom, kenglik

    def chiz(self):
        return f"{self.nom}: [{'_' * self.kenglik}]"


class Tugma:
    def __init__(self, matn):
        self.matn = matn

    def chiz(self):
        return f"< {self.matn} >"


class Forma:
    def __init__(self):
        self.komponentlar = []              # kompozitsiya: formada komponentlar bor

    def qosh(self, komponent):
        self.komponentlar.append(komponent)

    def chiz(self):
        satrlar = [k.chiz() for k in self.komponentlar]
        en = max(len(s) for s in satrlar)
        chegara = "+" + "-" * (en + 2) + "+"
        ichki = [f"| {s.ljust(en)} |" for s in satrlar]
        return "\n".join([chegara] + ichki + [chegara])


forma = Forma()
n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "belgi":
        forma.qosh(Belgi(q[1]))
    elif q[0] == "maydon":
        forma.qosh(Maydon(q[1], int(q[2])))
    else:
        forma.qosh(Tugma(q[1]))
print(forma.chiz())
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    interface Komponent {
        String chiz();
    }

    record Belgi(String matn) implements Komponent {
        public String chiz() { return matn; }
    }

    record Maydon(String nom, int kenglik) implements Komponent {
        public String chiz() { return nom + ": [" + "_".repeat(kenglik) + "]"; }
    }

    record Tugma(String matn) implements Komponent {
        public String chiz() { return "< " + matn + " >"; }
    }

    static class Forma implements Komponent {
        private final List<Komponent> komponentlar = new ArrayList<>();   // kompozitsiya

        void qosh(Komponent k) { komponentlar.add(k); }

        public String chiz() {
            List<String> satrlar = new ArrayList<>();
            int en = 0;
            for (Komponent k : komponentlar) {
                String s = k.chiz();
                satrlar.add(s);
                en = Math.max(en, s.length());
            }
            String chegara = "+" + "-".repeat(en + 2) + "+";
            StringBuilder sb = new StringBuilder(chegara).append('\n');
            for (String s : satrlar) {
                sb.append("| ").append(s).append(" ".repeat(en - s.length())).append(" |\n");
            }
            return sb.append(chegara).toString();
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Forma forma = new Forma();
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].equals("belgi")) forma.qosh(new Belgi(q[1]));
            else if (q[0].equals("maydon")) forma.qosh(new Maydon(q[1], Integer.parseInt(q[2])));
            else forma.qosh(new Tugma(q[1]));
        }
        System.out.println(forma.chiz());
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Komponent {
public:
    virtual ~Komponent() = default;
    virtual string chiz() const = 0;
};

class Belgi : public Komponent {
    string matn;
public:
    Belgi(string matn) : matn(matn) {}
    string chiz() const override { return matn; }
};

class Maydon : public Komponent {
    string nom;
    int kenglik;
public:
    Maydon(string nom, int kenglik) : nom(nom), kenglik(kenglik) {}
    string chiz() const override { return nom + ": [" + string(kenglik, '_') + "]"; }
};

class Tugma : public Komponent {
    string matn;
public:
    Tugma(string matn) : matn(matn) {}
    string chiz() const override { return "< " + matn + " >"; }
};

class Forma : public Komponent {
    vector<unique_ptr<Komponent>> komponentlar;             // kompozitsiya
public:
    void qosh(unique_ptr<Komponent> k) { komponentlar.push_back(move(k)); }

    string chiz() const override {
        vector<string> satrlar;
        size_t en = 0;
        for (const auto& k : komponentlar) {
            satrlar.push_back(k->chiz());
            en = max(en, satrlar.back().size());
        }
        string chegara = "+" + string(en + 2, '-') + "+";
        string natija = chegara + "\n";
        for (const string& s : satrlar) {
            natija += "| " + s + string(en - s.size(), ' ') + " |\n";
        }
        return natija + chegara;
    }
};

int main() {
    Forma forma;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string tur, matn;
        cin >> tur >> matn;
        if (tur == "belgi") {
            forma.qosh(make_unique<Belgi>(matn));
        } else if (tur == "maydon") {
            int kenglik;
            cin >> kenglik;
            forma.qosh(make_unique<Maydon>(matn, kenglik));
        } else {
            forma.qosh(make_unique<Tugma>(matn));
        }
    }
    cout << forma.chiz() << endl;
    return 0;
}
```

Kirish (nomlarda faqat ASCII harflar: C++ `size()` baytlarni sanaydi, `ʻ` kabi belgilar esa ramkani siljitadi):

```text
4
belgi Royxat
maydon Ism 10
maydon Parol 8
tugma Yuborish
```

Chiqish:

```text
+-------------------+
| Royxat            |
| Ism: [__________] |
| Parol: [________] |
| < Yuborish >      |
+-------------------+
```

`Forma` oʻzi ham `Komponent` (Java va C++'da): uni boshqa formaning ichiga qoʻyish mumkin — bu „Kompozit“ deb ataluvchi mashhur dizayn namunasi. Forma har bir komponent qanday chizilishini bilmaydi, faqat `chiz()` ni chaqiradi va natijalarni ramkaga joylaydi. Yangi komponent turi (masalan, `Belgilash katagi`) qoʻshilsa, forma kodi oʻzgarmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Komponentning ichki tuzilishiga bogʻlanish.** Boshqa komponent maydonlariga toʻgʻridan-toʻgʻri murojaat qilish uni almashtirishni imkonsiz qiladi; faqat interfeys metodlaridan foydalaning.
- **Hodisa ichida uzoq ish.** Grafik ilovada callback ichidagi uzoq hisob butun oynani „muzlatadi“: hodisalar sikli kutib qoladi.
- **Callbackni chaqirish oʻrniga uzatish.** Tkinterʼda `command=saqlash()` funksiyani darhol chaqiradi va natijasini (`None`) ulaydi; `command=saqlash` yozing.
- **Hamma narsani merosxoʻrlik bilan qilish.** „Tugma — bu oyna“ emas; oynada tugma *bor* — kompozitsiya ishlating.
:::

:::tip
Yangi komponent loyihalashda avval uning **interfeysini** yozing: qanday metodlari, xususiyatlari va hodisalari boʻladi. Interfeys tayyor boʻlsa, komponentni ishlatadigan kodni ichki qism hali yozilmasdan ham yozish va sinash mumkin.
:::

## Xulosa

- Komponent — aniq vazifali, interfeys orqali ulanadigan va almashtiriladigan dastur boʻlagi.
- Interfeysga qarab yozilgan kod qaysi komponent ulanganini bilmaydi; komponentni yaratish va ishlatish alohida joyda.
- Grafik komponentlarda xususiyatlar (holat), metodlar (buyruqlar) va hodisalar (yuz bergan narsa) bor.
- Hodisaga javob beruvchi funksiyalar (callback, listener) komponentga ulanadi va hodisa yuz berganda chaqiriladi; grafik dastur hodisalar siklida ishlaydi.
- Murakkab komponent oddiylaridan kompozitsiya orqali yigʻiladi.

## Oʻzingizni tekshiring

:::check
Q: Komponent interfeysi nima uchun kerak?
A: Komponentdan foydalanuvchi kod faqat shartnomaga tayanishi va komponentni boshqasiga almashtirganda oʻzgarmasligi uchun.
Q: Tugmaning `Text`, `Click` va `Focus()` aʼzolari mos ravishda nima?
A: `Text` — xususiyat, `Click` — hodisa, `Focus()` — metod.
Q: Callback (tinglovchi) nima?
A: Komponentga ulanadigan va maʼlum hodisa yuz berganda komponent tomonidan chaqiriladigan funksiya.
Q: Hodisalar sikli nima qiladi?
A: Foydalanuvchi harakatlarini kutadi va har bir hodisa uchun unga ulangan funksiyalarni chaqiradi.
Q: Kompozitsiya merosxoʻrlikdan nimasi bilan farq qiladi?
A: Kompozitsiya — „…da … bor“ munosabati: obyekt boshqa obyektlarni oʻz ichiga oladi va ular bilan interfeys orqali ishlaydi; merosxoʻrlik — „…dir“ munosabati.
:::

:::link https://docs.python.org/3/library/tkinter.html
Python: tkinter
Pythonʼning standart grafik interfeys kutubxonasi — komponentlar, hodisalar va joylashtirish (ingliz tilida).
:::

:::link https://learn.microsoft.com/en-us/dotnet/desktop/winforms/controls/
Windows Forms: boshqaruv elementlari
Visual Studioʻdagi komponentlar, ularning xususiyatlari va hodisalari (ingliz tilida).
:::
