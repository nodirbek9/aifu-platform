---
title: Shablonlar bilan ishlash
summary: Umumlashgan dasturlash gʻoyasi; C++ funksiya va sinf shablonlari, Java generiklari va cheklovlar (extends Comparable), Python'da „oʻrdak tiplash“ va TypeVar; shablon funksiya, shablon sinf (stek) va umumiy saralash.
---

## Nima uchun bu mavzu muhim?

Butun sonlar massivining eng kattasini topuvchi funksiya yozdik. Ertaga xuddi shu ish haqiqiy sonlar uchun, indinga satrlar uchun kerak boʻladi. Har safar funksiyani nusxalab, faqat turini almashtirish — xatoga yoʻl ochadi: bitta nusxada xato tuzatilsa, qolganlari eskicha qoladi.

**Shablonlar** (C++'da template, Java'da generic) algoritmni **tur parametri** bilan bir marta yozish imkonini beradi: `T` turidagi massivning eng kattasi — `T` oʻrniga `int` ham, `double` ham, `String` ham qoʻyilishi mumkin. Standart kutubxonalarning oʻzi shunday qurilgan: `vector<int>`, `ArrayList<String>`, `sort` — barchasi shablonlar.

:::note
**Bu darsdan soʻng siz:**

- umumlashgan (generic) dasturlash nima uchun kerakligini tushuntira olasiz;
- C++'da funksiya va sinf shablonini, Java'da generik metod va sinfni yoza olasiz;
- tur parametriga cheklov qoʻya olasiz (`T extends Comparable<T>`);
- Python'da bir funksiya turli turlar bilan nima uchun ishlashini („oʻrdak tiplash“) tushuntira olasiz;
- shablon funksiya va sinflardan oʻz dasturingizda foydalana olasiz.
:::

## Umumlashgan dasturlash

Eng katta elementni topish algoritmi turga bogʻliq emas: unga faqat ikkita element **solishtirilishi** (`<`) kerak. Shablon aynan shuni ifodalaydi: „qandaydir `T` turi uchun, agar `T` larni solishtirish mumkin boʻlsa“.

| Til | Funksiya shabloni | Chaqiruv |
|---|---|---|
| C++ | `template <typename T> T engKatta(const vector<T>& a)` | `engKatta(sonlar)`, `engKatta(sozlar)` |
| Java | `static <T extends Comparable<T>> T engKatta(List<T> a)` | `engKatta(sonlar)` |
| Python | `def eng_katta(a): ...` (tur talab qilinmaydi) | `eng_katta(sonlar)` |

Tur parametri odatda bitta katta harf bilan belgilanadi: `T` (type), `K` va `V` (key, value), `E` (element).

## C++ shablonlari

C++ shabloni — kod **qolipi**. Kompilyator `engKatta(sonlar)` chaqiruvini koʻrganda `T = int` uchun funksiyaning alohida nusxasini yaratadi, `engKatta(sozlar)` uchun esa `T = string` nusxasini. Bu jarayon **instansiyalash** deyiladi.

- Tur odatda argumentlardan **avtomatik aniqlanadi**; kerak boʻlsa aniq yoziladi: `engKatta<double>(a)`.
- Shablon ichida `T` ga qanday amallar qoʻllangan boʻlsa (masalan, `<`), haqiqiy tur shularni qoʻllashi kerak — aks holda kompilyatsiya xatosi.
- Sinf shabloni: `template <typename T> class Stek { vector<T> elementlar; ... };` va `Stek<int> s;`.
- Shablon toʻliq kodi sarlavha faylida (`.h`) turishi kerak: kompilyator uni har bir tur uchun koʻrishi lozim.

## Java generiklari

Java'da tur parametri burchakli qavsda yoziladi: sinf uchun `class Stek<T>`, metod uchun qaytaradigan turdan oldin `<T>`.

- Tur parametri faqat **obyekt turi** boʻla oladi: `Stek<Integer>`, `Stek<int>` emas. `int` va `Integer` orasidagi oʻtish avtomatik bajariladi (autoboxing).
- **Cheklov**: `<T extends Comparable<T>>` — „`T` oʻzaro solishtiriladigan tur boʻlishi shart“. Shundan keyin `a.compareTo(b)` chaqirish mumkin. Cheklovsiz `T` uchun faqat `Object` metodlari mavjud.
- Kompilyator turlarni tekshiradi: `Stek<String>` ga son qoʻshish kompilyatsiya xatosi beradi. Ish vaqtida esa tur parametri oʻchiriladi (type erasure) — shuning uchun `new T[n]` yozib boʻlmaydi, `ArrayList<T>` ishlatiladi.

## Python: oʻrdak tiplash

Python dinamik tipli til: funksiya parametri istalgan turdagi qiymatni qabul qiladi. Funksiya ichidagi amallar (masalan, `>`) qiymat uchun mavjud boʻlsa, hammasi ishlaydi. Bu **oʻrdak tiplash** (duck typing) deyiladi: „oʻrdakdek yursa va oʻrdakdek gʻaqillasa — demak, oʻrdak“.

Shuning uchun Python'da alohida shablon sintaksisi shart emas. Lekin kod oʻquvchisi va tekshiruv vositalari uchun **tur izohlari** yoziladi:

```python
from typing import TypeVar

T = TypeVar("T")

def birinchi(a: list[T]) -> T:      # „qanday T boʻlsa, oʻsha T qaytadi“
    return a[0]
```

Python 3.12'dan qisqaroq yozuv ham bor: `def birinchi[T](a: list[T]) -> T:`. Izohlar dastur ishlashiga taʼsir qilmaydi.

## Ishlangan misol 1: shablon funksiya

Birinchi satrda butun sonlar, ikkinchisida soʻzlar. Bitta umumiy `eng_katta` funksiyasi va bitta umumiy `sanash` funksiyasi yordamida har bir satrning eng katta elementini va u necha marta uchrashini chiqaring (soʻzlar alifbo tartibida solishtiriladi).

```python
def eng_katta(a):
    natija = a[0]
    for x in a:
        if x > natija:
            natija = x
    return natija


def sanash(a, qiymat):
    return sum(1 for x in a if x == qiymat)


sonlar = list(map(int, input().split()))
sozlar = input().split()

k = eng_katta(sonlar)
print(k, sanash(sonlar, k))
k = eng_katta(sozlar)
print(k, sanash(sozlar, k))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    static <T extends Comparable<T>> T engKatta(List<T> a) {
        T natija = a.get(0);
        for (T x : a) {
            if (x.compareTo(natija) > 0) natija = x;
        }
        return natija;
    }

    static <T> int sanash(List<T> a, T qiymat) {
        int soni = 0;
        for (T x : a) {
            if (x.equals(qiymat)) soni++;
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> sonlar = new ArrayList<>();
        for (String s : in.nextLine().trim().split("\\s+")) sonlar.add(Integer.parseInt(s));
        List<String> sozlar = Arrays.asList(in.nextLine().trim().split("\\s+"));

        Integer k1 = engKatta(sonlar);
        System.out.println(k1 + " " + sanash(sonlar, k1));
        String k2 = engKatta(sozlar);
        System.out.println(k2 + " " + sanash(sozlar, k2));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

template <typename T>
T engKatta(const vector<T>& a) {
    T natija = a[0];
    for (const T& x : a) {
        if (natija < x) natija = x;        // T uchun faqat < kerak
    }
    return natija;
}

template <typename T>
int sanash(const vector<T>& a, const T& qiymat) {
    int soni = 0;
    for (const T& x : a) {
        if (x == qiymat) soni++;
    }
    return soni;
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim1(satr);
    vector<int> sonlar;
    int x;
    while (oqim1 >> x) sonlar.push_back(x);

    getline(cin, satr);
    istringstream oqim2(satr);
    vector<string> sozlar;
    string s;
    while (oqim2 >> s) sozlar.push_back(s);

    int k1 = engKatta(sonlar);                 // T = int
    cout << k1 << " " << sanash(sonlar, k1) << endl;
    string k2 = engKatta(sozlar);              // T = string
    cout << k2 << " " << sanash(sozlar, k2) << endl;
    return 0;
}
```

Kirish:

```text
4 9 2 9 7
olma nok uzum anor uzum
```

Chiqish:

```text
9 2
uzum 2
```

Har bir tilda algoritm **bir marta** yozilgan, lekin ikki xil tur bilan ishladi. C++'da kompilyator `engKatta<int>` va `engKatta<string>` degan ikki nusxa yaratdi. Java'da `int` lar `List<Integer>` da saqlanadi, solishtirish esa `compareTo` orqali — `<` faqat sonlar uchun. Java'da `equals` ishlatilgan: `Integer` obyektlarini `==` bilan solishtirish xavfli.

## Ishlangan misol 2: shablon sinf — stek

**Stek** — „oxirgi kirgan birinchi chiqadi“ (LIFO) tamoyilidagi tuzilma: element faqat yuqoriga qoʻyiladi (`push`) va faqat yuqoridan olinadi (`pop`). Bitta umumiy `Stek` sinfini yozing va undan soʻzlar va sonlar uchun foydalaning: birinchi satrdagi soʻzlarni va ikkinchi satrdagi sonlarni teskari tartibda chiqaring.

```python
class Stek:
    def __init__(self):
        self._elementlar = []

    def push(self, x):
        self._elementlar.append(x)

    def pop(self):
        return self._elementlar.pop()

    def bosh(self):
        return len(self._elementlar) == 0


def teskari(qiymatlar):
    s = Stek()
    for x in qiymatlar:
        s.push(x)
    natija = []
    while not s.bosh():
        natija.append(s.pop())
    return natija


print(*teskari(input().split()))
print(*teskari(list(map(int, input().split()))))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Stek<T> {
        private final List<T> elementlar = new ArrayList<>();

        void push(T x) {
            elementlar.add(x);
        }

        T pop() {
            return elementlar.remove(elementlar.size() - 1);
        }

        boolean bosh() {
            return elementlar.isEmpty();
        }
    }

    static <T> String teskari(List<T> qiymatlar) {
        Stek<T> s = new Stek<>();
        for (T x : qiymatlar) s.push(x);
        StringBuilder sb = new StringBuilder();
        while (!s.bosh()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(s.pop());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> sozlar = List.of(in.nextLine().trim().split("\\s+"));
        List<Integer> sonlar = new ArrayList<>();
        for (String t : in.nextLine().trim().split("\\s+")) sonlar.add(Integer.parseInt(t));

        System.out.println(teskari(sozlar));
        System.out.println(teskari(sonlar));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

template <typename T>
class Stek {
    vector<T> elementlar;

public:
    void push(const T& x) { elementlar.push_back(x); }

    T pop() {
        T x = elementlar.back();
        elementlar.pop_back();
        return x;
    }

    bool bosh() const { return elementlar.empty(); }
};

template <typename T>
void teskariChiqar(istringstream& oqim) {
    Stek<T> s;
    T x;
    while (oqim >> x) s.push(x);
    bool birinchi = true;
    while (!s.bosh()) {
        if (!birinchi) cout << ' ';
        cout << s.pop();
        birinchi = false;
    }
    cout << endl;
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim1(satr);
    teskariChiqar<string>(oqim1);              // T aniq koʻrsatildi
    getline(cin, satr);
    istringstream oqim2(satr);
    teskariChiqar<int>(oqim2);
    return 0;
}
```

Kirish:

```text
bir ikki uch
10 20 30 40
```

Chiqish:

```text
uch ikki bir
40 30 20 10
```

`Stek<string>` va `Stek<int>` — bitta sinf shablonidan hosil boʻlgan ikki xil tur. C++'dagi `teskariChiqar<string>(oqim1)` da tur aniq yozilgan: funksiya argumentlarida `T` yoʻq, shuning uchun kompilyator uni oʻzi aniqlay olmaydi. Java'da `Stek<String>` ga `s.push(5)` yozilsa, dastur kompilyatsiya boʻlmaydi — generiklar xatoni ishga tushirishdan **oldin** ushlaydi. Python sinfi esa hech qanday tur eʼlon qilmaydi va har qanday qiymat bilan ishlaydi.

## Ishlangan misol 3: cheklovli umumiy saralash

Birinchi satrda haqiqiy sonlar. Ikkinchisida *n*, keyingi *n* satrda talaba ismi va bali. Bitta umumiy `saralash` funksiyasi (qoʻyib saralash) bilan sonlarni oʻsish tartibida, talabalarni esa tabiiy tartibda — bal kamayishi, teng balda ism boʻyicha — saralab chiqaring.

```python
def saralash(a):
    """Qoʻyib saralash: < amali bor har qanday tur uchun."""
    for i in range(1, len(a)):
        joriy = a[i]
        j = i - 1
        while j >= 0 and joriy < a[j]:
            a[j + 1] = a[j]
            j -= 1
        a[j + 1] = joriy


class Talaba:
    def __init__(self, ism, ball):
        self.ism = ism
        self.ball = ball

    def __lt__(self, boshqa):          # tabiiy tartib: bal kamayishi, keyin ism
        return (-self.ball, self.ism) < (-boshqa.ball, boshqa.ism)


sonlar = list(map(float, input().split()))
saralash(sonlar)
print(*sonlar)

n = int(input())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append(Talaba(ism, int(ball)))
saralash(talabalar)
for t in talabalar:
    print(t.ism, t.ball)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** Qoʻyib saralash: oʻzaro solishtiriladigan har qanday tur uchun. */
    static <T extends Comparable<T>> void saralash(List<T> a) {
        for (int i = 1; i < a.size(); i++) {
            T joriy = a.get(i);
            int j = i - 1;
            while (j >= 0 && joriy.compareTo(a.get(j)) < 0) {
                a.set(j + 1, a.get(j));
                j--;
            }
            a.set(j + 1, joriy);
        }
    }

    static class Talaba implements Comparable<Talaba> {
        final String ism;
        final int ball;

        Talaba(String ism, int ball) {
            this.ism = ism;
            this.ball = ball;
        }

        @Override
        public int compareTo(Talaba boshqa) {      // tabiiy tartib: bal kamayishi, keyin ism
            if (ball != boshqa.ball) return Integer.compare(boshqa.ball, ball);
            return ism.compareTo(boshqa.ism);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Double> sonlar = new ArrayList<>();
        for (String s : in.nextLine().trim().split("\\s+")) sonlar.add(Double.parseDouble(s));
        saralash(sonlar);
        StringBuilder sb = new StringBuilder();
        for (double x : sonlar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(sb);

        int n = Integer.parseInt(in.nextLine().trim());
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            talabalar.add(new Talaba(q[0], Integer.parseInt(q[1])));
        }
        saralash(talabalar);
        for (Talaba t : talabalar) System.out.println(t.ism + " " + t.ball);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// Qoʻyib saralash: < amali bor har qanday tur uchun
template <typename T>
void saralash(vector<T>& a) {
    for (size_t i = 1; i < a.size(); i++) {
        T joriy = a[i];
        int j = (int) i - 1;
        while (j >= 0 && joriy < a[j]) {
            a[j + 1] = a[j];
            j--;
        }
        a[j + 1] = joriy;
    }
}

struct Talaba {
    string ism;
    int ball;

    bool operator<(const Talaba& boshqa) const {   // tabiiy tartib: bal kamayishi, keyin ism
        if (ball != boshqa.ball) return ball > boshqa.ball;
        return ism < boshqa.ism;
    }
};

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim(satr);
    vector<double> sonlar;
    double x;
    while (oqim >> x) sonlar.push_back(x);
    saralash(sonlar);
    for (size_t i = 0; i < sonlar.size(); i++) {
        if (i > 0) cout << ' ';
        cout << sonlar[i];
    }
    cout << endl;

    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;
    saralash(talabalar);
    for (const auto& t : talabalar) cout << t.ism << " " << t.ball << endl;
    return 0;
}
```

Kirish:

```text
3.5 -1.25 2.0 10.75
4
Malika 88
Aziz 92
Bekzod 88
Dilnoza 95
```

Chiqish (Python va Java):

```text
-1.25 2.0 3.5 10.75
Dilnoza 95
Aziz 92
Bekzod 88
Malika 88
```

C++ `cout` haqiqiy sonni ortiqcha nollarsiz chiqaradi, shuning uchun uning birinchi satri `-1.25 2 3.5 10.75` boʻladi. `saralash` haqiqiy sonlar haqida ham, talabalar haqida ham hech narsa bilmaydi — u faqat `<` (Java'da `compareTo`) dan foydalanadi. Java'dagi `<T extends Comparable<T>>` cheklovi shuni kafolatlaydi: `Comparable` boʻlmagan tur bilan chaqirilsa, kompilyator xato beradi. C++'da cheklov yozilmagan, lekin `Talaba` da `operator<` boʻlmasa, xato shablon ichidagi `joriy < a[j]` qatorida chiqadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Java'da primitiv tur parametri.** `List<int>` yozib boʻlmaydi; `List<Integer>`, `List<Double>` ishlating.
- **Java'da cheklovsiz `compareTo`.** `<T>` uchun `a.compareTo(b)` kompilyatsiya boʻlmaydi; `<T extends Comparable<T>>` yozing.
- **C++'da shablonni `.cpp` faylga yashirish.** Boshqa fayldan chaqirilganda „aniqlanmagan havola“ xatosi chiqadi; shablon kodi sarlavha faylida turadi.
- **C++'da shablon xatosini notoʻgʻri joydan izlash.** Kompilyator xatoni shablon ichida koʻrsatadi, lekin sabab koʻpincha chaqiruvdagi tur — unga kerakli amal (`<`, `==`, `<<`) yetishmaydi.
:::

:::tip
Shablon yozishdan oldin algoritmni bitta aniq tur uchun (masalan, `int`) yozib, tekshirib oling. Ishlagach, `int` ni `T` ga almashtiring va `T` ga qaysi amallar kerakligini roʻyxatga yozing — bu shablonning „talablari“.
:::

## Xulosa

- Shablon (generic) algoritmni tur parametri bilan bir marta yozish imkonini beradi.
- C++ har bir ishlatilgan tur uchun shablonning alohida nusxasini yaratadi; shablon kodi sarlavha faylida turadi.
- Java generiklari faqat obyekt turlari bilan ishlaydi; `extends` bilan cheklov qoʻyiladi, turlar kompilyatsiyada tekshiriladi.
- Python'da oʻrdak tiplash tufayli funksiya har qanday mos tur bilan ishlaydi; `TypeVar` faqat izoh uchun.
- Shablon tur parametriga qoʻyadigan talablar — undan foydalaniladigan amallar (`<`, `==`, …).

## Oʻzingizni tekshiring

:::check
Q: Shablonlar qanday muammoni hal qiladi?
A: Bir xil algoritmni turli turlar uchun nusxalab yozishni: algoritm tur parametri bilan bir marta yoziladi.
Q: Java'da nima uchun `Stek<int>` yozib boʻlmaydi?
A: Tur parametri faqat obyekt turi boʻla oladi; `Stek<Integer>` ishlatiladi.
Q: `<T extends Comparable<T>>` nimani bildiradi?
A: `T` oʻzaro solishtiriladigan tur boʻlishi shart; shuning uchun `compareTo` chaqirish mumkin.
Q: C++'da `engKatta(sonlar)` va `engKatta(sozlar)` chaqirilsa, kompilyator nechta funksiya yaratadi?
A: Ikkita: `T = int` va `T = string` uchun alohida nusxa.
Q: Python'da nima uchun maxsus shablon sintaksisi shart emas?
A: Til dinamik tipli: funksiya ichidagi amallar qiymat uchun mavjud boʻlsa, u har qanday turda ishlaydi (oʻrdak tiplash).
:::

:::link https://dev.java/learn/generics/
Java: generiklar
Generik sinf va metodlar, cheklovlar boʻyicha rasmiy oʻquv resursi (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/language/templates
C++: shablonlar
Funksiya va sinf shablonlari haqida maʼlumotnoma (ingliz tilida).
:::
