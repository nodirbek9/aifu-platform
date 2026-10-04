---
title: Obyektlar massivi
summary: Obyektlarni massiv va roʻyxatda saqlash, Java'da null havolalar, sikl orqali obyektlarni oʻzgartirish (C++'da nusxa va havola), maydon boʻyicha qidirish va yigʻish, obyektlarning tabiiy tartibi (__lt__, Comparable, operator<).
---

## Nima uchun bu mavzu muhim?

Bitta talaba obyekti foydali, lekin guruhda 25 talaba, universitetda esa minglab. Real dasturlar odatda **koʻp obyekt** bilan ishlaydi: doʻkon mahsulotlari, kutubxona kitoblari, jadvaldagi darslar. Ularni saqlashning eng oddiy usuli — **obyektlar massivi** (roʻyxati).

Oldingi maʼruzada bitta sinf va bir nechta obyekt yaratdik. Endi obyektlarni massivga joylab, ular ustida birinchi semestrdagi barcha usullarni — qidirish, yigʻindi, eng kattasini topish, saralashni — qoʻllaymiz. Shu bilan birga tillar orasidagi muhim farq — obyekt **nusxa**mi yoki **havola**mi — amalda qanday namoyon boʻlishini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- obyektlar massivini (roʻyxatini) uch tilda yaratib, toʻldira olasiz;
- Java'da `new Talaba[n]` nima yaratishini va nega `null` xatosi chiqishini tushuntira olasiz;
- sikl ichida obyektlarni toʻgʻri oʻzgartira olasiz (C++'da `auto&`);
- maydon boʻyicha qidirish, yigʻindi va maksimumni hisoblay olasiz;
- sinfga tabiiy tartib berib, obyektlarni standart funksiya bilan saralay olasiz.
:::

## Obyektlar massivini yaratish

| Til | Oʻzgarmas oʻlchamli | Oʻsuvchi roʻyxat |
|---|---|---|
| Python | — | `talabalar = []`, `talabalar.append(Talaba(...))` |
| Java | `Talaba[] t = new Talaba[n];` | `List<Talaba> t = new ArrayList<>();`, `t.add(new Talaba(...))` |
| C++ | `vector<Talaba> t(n);` (standart konstruktor kerak) | `vector<Talaba> t;`, `t.push_back(Talaba(...))` yoki `t.emplace_back(...)` |

**Java'dagi tuzoq.** `new Talaba[n]` *n* ta obyekt **yaratmaydi** — u *n* ta boʻsh havola (`null`) yaratadi. Har bir elementga obyekt alohida beriladi: `t[i] = new Talaba(...)`. Busiz `t[0].ism` ga murojaat `NullPointerException` beradi.

**C++'dagi xususiyat.** `vector<Talaba> t(n);` *n* ta obyektni darhol yaratadi, buning uchun sinfda parametrsiz (standart) konstruktor boʻlishi kerak. Agar faqat parametrli konstruktor boʻlsa, boʻsh `vector` yaratib, `push_back` yoki `emplace_back` bilan qoʻshing.

## Obyektlar ustida aylanish

Massivdagi har bir obyekt bilan ishlash uchun oddiy sikl yoki „har bir element uchun“ sikli ishlatiladi:

```text
Python:  for t in talabalar: ...
Java:    for (Talaba t : talabalar) { ... }
C++:     for (auto& t : talabalar) { ... }
```

Python va Java'da `t` — massivdagi obyektga **havola**, shuning uchun `t.ball += 5` massivdagi obyektni oʻzgartiradi. C++'da esa `for (auto t : talabalar)` har bir obyektning **nusxasini** oladi: nusxa oʻzgaradi, massiv esa oʻzgarishsiz qoladi. Obyektni oʻzgartirish uchun `auto&`, faqat oʻqish uchun `const auto&` yoziladi (nusxa ham yaratilmaydi).

## Maydon boʻyicha qidirish va yigʻish

Birinchi semestrdagi massiv algoritmlari obyektlar massivida ham ishlaydi — faqat elementning oʻzi emas, uning **maydoni** solishtiriladi:

| Masala | Son massivida | Obyektlar massivida |
|---|---|---|
| Yigʻindi | `s += a[i]` | `s += t[i].narx * t[i].miqdor` |
| Maksimum | `a[i] > eng` | `t[i].ball > eng.ball` (eng yaxshi **obyekt** saqlanadi) |
| Qidirish | `a[i] == x` | `t[i].ism.equals(x)` (Java), `t[i].ism == x` (Python, C++) |
| Sanash | `a[i] > 0` | `t[i].kurs == 2` |

Maksimumni topishda faqat eng katta qiymatni emas, **obyektning oʻzini** (yoki indeksini) eslab qolish qulay: keyin uning boshqa maydonlarini ham chiqarish mumkin.

## Obyektlarning tabiiy tartibi

Oʻtgan maʼruzada saralash uchun kalit (`key`) yoki taqqoslash funksiyasini berdik. Agar sinfning „odatiy“ tartibi boʻlsa, uni **sinfning oʻzida** belgilash mumkin — shunda saralash qoʻshimcha parametrsiz ishlaydi:

| Til | Sinfda nima yoziladi | Saralash |
|---|---|---|
| Python | `def __lt__(self, boshqa): return ...` | `sorted(a)`, `a.sort()` |
| Java | `implements Comparable<Kitob>` va `public int compareTo(Kitob b)` | `Collections.sort(a)`, `Arrays.sort(a)` |
| C++ | `bool operator<(const Kitob& b) const` | `sort(a.begin(), a.end())` |

Java'da `compareTo` manfiy son („men oldinroqman“), nol („tengmiz“) yoki musbat son qaytaradi; `Integer.compare(x, y)` va `String.compareTo` tayyor qiymat beradi.

## Ishlangan misol 1: ombordagi mahsulotlar

Birinchi satrda *n*, keyingi *n* satrda mahsulot nomi, narxi va miqdori. Ombordagi jami qiymatni (narx × miqdor yigʻindisi) va eng qimmat **zaxiraga** ega mahsulot nomini chiqaring.

```python
class Mahsulot:
    def __init__(self, nom, narx, miqdor):
        self.nom = nom
        self.narx = narx
        self.miqdor = miqdor

    def qiymat(self):
        return self.narx * self.miqdor


n = int(input())
mahsulotlar = []
for _ in range(n):
    nom, narx, miqdor = input().split()
    mahsulotlar.append(Mahsulot(nom, int(narx), int(miqdor)))

jami = 0
eng = mahsulotlar[0]
for m in mahsulotlar:
    jami += m.qiymat()
    if m.qiymat() > eng.qiymat():
        eng = m

print(jami)
print(eng.nom)
```
```java
import java.util.Scanner;

public class Main {
    static class Mahsulot {
        String nom;
        long narx, miqdor;

        Mahsulot(String nom, long narx, long miqdor) {
            this.nom = nom;
            this.narx = narx;
            this.miqdor = miqdor;
        }

        long qiymat() {
            return narx * miqdor;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Mahsulot[] mahsulotlar = new Mahsulot[n];        // hozircha n ta null
        for (int i = 0; i < n; i++) {
            mahsulotlar[i] = new Mahsulot(in.next(), in.nextLong(), in.nextLong());
        }

        long jami = 0;
        Mahsulot eng = mahsulotlar[0];
        for (Mahsulot m : mahsulotlar) {
            jami += m.qiymat();
            if (m.qiymat() > eng.qiymat()) eng = m;
        }

        System.out.println(jami);
        System.out.println(eng.nom);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class Mahsulot {
public:
    string nom;
    long long narx, miqdor;

    Mahsulot(string nom, long long narx, long long miqdor) : nom(nom), narx(narx), miqdor(miqdor) {}

    long long qiymat() const { return narx * miqdor; }
};

int main() {
    int n;
    cin >> n;
    vector<Mahsulot> mahsulotlar;
    for (int i = 0; i < n; i++) {
        string nom;
        long long narx, miqdor;
        cin >> nom >> narx >> miqdor;
        mahsulotlar.emplace_back(nom, narx, miqdor);
    }

    long long jami = 0;
    int eng = 0;                                    // eng qimmat zaxira indeksi
    for (int i = 0; i < n; i++) {
        jami += mahsulotlar[i].qiymat();
        if (mahsulotlar[i].qiymat() > mahsulotlar[eng].qiymat()) eng = i;
    }

    cout << jami << endl << mahsulotlar[eng].nom << endl;
    return 0;
}
```

Kirish:

```text
4
daftar 5000 120
ruchka 2000 300
kitob 45000 10
sumka 150000 5
```

Chiqish:

```text
2400000
sumka
```

Zaxira qiymatlari: 600 000, 600 000, 450 000, 750 000. `daftar` va `ruchka` ning qiymati teng: agar ular eng katta boʻlganda, qatʼiy `>` sharti tufayli **birinchi** uchragani (`daftar`) qolar edi, `>=` bilan esa oxirgisi (`ruchka`). Teng qiymatlarda qaysi obyekt tanlanishini shartning oʻzi belgilaydi. C++ versiyasida obyektning oʻzi emas, uning **indeksi** saqlanadi — nusxa yaratilmaydi.

## Ishlangan misol 2: sikl ichida obyektlarni oʻzgartirish

Birinchi satrda *n* va *k*, keyingi *n* satrda talaba ismi va bali. Bali 60 dan kam boʻlgan har bir talabaga *k* ball qoʻshing (lekin 100 dan oshirmang). Barcha talabalarni yangi ballari bilan, soʻng oʻtganlar (≥ 60) sonini chiqaring.

```python
class Talaba:
    def __init__(self, ism, ball):
        self.ism = ism
        self.ball = ball


n, k = map(int, input().split())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append(Talaba(ism, int(ball)))

for t in talabalar:                 # t — roʻyxatdagi obyektning oʻzi
    if t.ball < 60:
        t.ball = min(t.ball + k, 100)

otganlar = 0
for t in talabalar:
    print(t.ism, t.ball)
    if t.ball >= 60:
        otganlar += 1
print(otganlar)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Talaba {
        String ism;
        int ball;

        Talaba(String ism, int ball) {
            this.ism = ism;
            this.ball = ball;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt()));

        for (Talaba t : talabalar) {        // t — roʻyxatdagi obyektga havola
            if (t.ball < 60) t.ball = Math.min(t.ball + k, 100);
        }

        int otganlar = 0;
        for (Talaba t : talabalar) {
            System.out.println(t.ism + " " + t.ball);
            if (t.ball >= 60) otganlar++;
        }
        System.out.println(otganlar);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Talaba {
    string ism;
    int ball;
};

int main() {
    int n, k;
    cin >> n >> k;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;

    for (auto& t : talabalar) {         // & boʻlmasa, nusxa oʻzgarar edi
        if (t.ball < 60) t.ball = min(t.ball + k, 100);
    }

    int otganlar = 0;
    for (const auto& t : talabalar) {
        cout << t.ism << " " << t.ball << endl;
        if (t.ball >= 60) otganlar++;
    }
    cout << otganlar << endl;
    return 0;
}
```

Kirish:

```text
4 15
Aziz 52
Malika 91
Bekzod 40
Dilnoza 95
```

Chiqish:

```text
Aziz 67
Malika 91
Bekzod 55
Dilnoza 95
3
```

C++'da `struct` — barcha aʼzolari ochiq (`public`) boʻlgan sinf; maʼlumot tashuvchi oddiy turlar uchun qulay. Agar oʻzgartiruvchi siklda `auto&` oʻrniga `auto` yozilsa, dastur xato bermaydi, lekin ballar oʻzgarmaydi va chiqishda `Aziz 52`, `Bekzod 40`, oʻtganlar esa 2 boʻladi — bunday xatoni topish qiyin.

## Ishlangan misol 3: kitoblarning tabiiy tartibi

Birinchi satrda *n*, keyingi *n* satrda kitobning nashr yili va nomi (bitta soʻz). `Kitob` sinfiga tabiiy tartib bering: avval yil boʻyicha, yil teng boʻlsa — nom boʻyicha alifbo tartibida. Kitoblarni standart saralash bilan tartiblab chiqaring.

```python
class Kitob:
    def __init__(self, yil, nom):
        self.yil = yil
        self.nom = nom

    def __lt__(self, boshqa):           # „self boshqadan oldin turadimi?“
        return (self.yil, self.nom) < (boshqa.yil, boshqa.nom)


n = int(input())
kitoblar = []
for _ in range(n):
    yil, nom = input().split()
    kitoblar.append(Kitob(int(yil), nom))

kitoblar.sort()
for k in kitoblar:
    print(k.yil, k.nom)
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Kitob implements Comparable<Kitob> {
        int yil;
        String nom;

        Kitob(int yil, String nom) {
            this.yil = yil;
            this.nom = nom;
        }

        @Override
        public int compareTo(Kitob boshqa) {
            if (yil != boshqa.yil) return Integer.compare(yil, boshqa.yil);
            return nom.compareTo(boshqa.nom);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Kitob> kitoblar = new ArrayList<>();
        for (int i = 0; i < n; i++) kitoblar.add(new Kitob(in.nextInt(), in.next()));

        Collections.sort(kitoblar);
        for (Kitob k : kitoblar) System.out.println(k.yil + " " + k.nom);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Kitob {
    int yil;
    string nom;

    bool operator<(const Kitob& boshqa) const {     // „men boshqadan oldin turamanmi?“
        if (yil != boshqa.yil) return yil < boshqa.yil;
        return nom < boshqa.nom;
    }
};

int main() {
    int n;
    cin >> n;
    vector<Kitob> kitoblar(n);
    for (auto& k : kitoblar) cin >> k.yil >> k.nom;

    sort(kitoblar.begin(), kitoblar.end());
    for (const auto& k : kitoblar) cout << k.yil << " " << k.nom << endl;
    return 0;
}
```

Kirish:

```text
5
1999 Sarob
1926 Kecha
1999 Alpomish
2005 Dunyo
1926 Mehrob
```

Chiqish:

```text
1926 Kecha
1926 Mehrob
1999 Alpomish
1999 Sarob
2005 Dunyo
```

Uchala tilda ham saralash funksiyasi chaqiruvida hech qanday kalit berilmagan: tartib sinfning oʻzida — `__lt__`, `compareTo` yoki `operator<` da. Python'da kortejlarni solishtirish `(yil, nom)` avval birinchi elementni, teng boʻlsa ikkinchisini solishtiradi — bu ikki kalitli tartibning qisqa yozuvi.

:::warning
**Koʻp uchraydigan xatolar**

- **Java'da `new Talaba[n]` dan keyin obyekt yaratmaslik.** Massiv elementlari `null`; `t[i] = new Talaba(...)` siz `NullPointerException` chiqadi.
- **C++'da `auto&` oʻrniga `auto`.** Sikl nusxani oʻzgartiradi, massiv esa oʻzgarmaydi — dastur jim ishlaydi, natija notoʻgʻri.
- **Java'da satrlarni `==` bilan solishtirish.** `==` havolani solishtiradi; nom boʻyicha qidirishda `equals` ishlating.
- **Teng qiymatlarni hisobga olmaslik.** Maksimum topishda `>` va `>=` turli obyektni tanlaydi; shartda qaysi biri kerakligini aniqlang.
:::

:::tip
Obyektlar massivi bilan ishlaganda avval uni toʻgʻri oʻqiganingizni tekshiring: oʻqishdan soʻng darhol barcha obyektlarni chiqaradigan vaqtinchalik sikl yozing. Xatolarning katta qismi aynan oʻqish bosqichida boʻladi.
:::

## Xulosa

- Obyektlar massivda yoki oʻsuvchi roʻyxatda saqlanadi: Python `list`, Java `ArrayList`, C++ `vector`.
- Java'da `new T[n]` faqat `null` havolalar yaratadi; har bir obyekt `new` bilan alohida yaratiladi.
- C++'da obyektlarni sikl ichida oʻzgartirish uchun `auto&`, oʻqish uchun `const auto&` yoziladi.
- Massiv algoritmlari obyektlarga ham qoʻllanadi — maydonlar solishtiriladi, eng yaxshi obyekt yoki uning indeksi saqlanadi.
- Tabiiy tartib sinfda `__lt__`, `compareTo` yoki `operator<` bilan beriladi.

## Oʻzingizni tekshiring

:::check
Q: Java'da `Talaba[] t = new Talaba[3];` dan keyin nechta `Talaba` obyekti mavjud?
A: Birorta ham: massivda uchta `null` havola bor.
Q: C++'da `for (auto t : v) t.ball = 0;` dan keyin `v` dagi ballar nolga teng boʻladimi?
A: Yoʻq, `t` — nusxa; `for (auto& t : v)` yozish kerak.
Q: Python'da `for t in talabalar: t.ball += 1` roʻyxatdagi obyektlarni oʻzgartiradimi?
A: Ha, `t` roʻyxatdagi obyektning oʻziga havola.
Q: Java'da `compareTo` qanday qiymat qaytarishi kerak?
A: Joriy obyekt oldinroq turishi kerak boʻlsa manfiy, teng boʻlsa nol, keyinroq boʻlsa musbat son.
Q: Maksimum topishda nima uchun obyektni yoki indeksni saqlash qulay?
A: Keyin eng yaxshi obyektning boshqa maydonlarini (masalan, nomini) ham chiqarish mumkin.
:::

:::link https://docs.python.org/3/howto/sorting.html#odds-and-ends
Python: saralash va __lt__
Obyektlarni `__lt__` orqali saralash haqida (ingliz tilida).
:::

:::link https://dev.java/learn/api/collections-framework/
Java: kolleksiyalar
`List`, `ArrayList` va `Comparable` boʻyicha rasmiy oʻquv resursi (ingliz tilida).
:::
