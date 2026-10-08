---
title: Saralash algoritmlari
summary: Algoritm murakkabligi (O-belgisi), tanlab va qoʻyib saralash, birlashtirib va tez saralash gʻoyasi, barqaror saralash, standart saralash funksiyasi va kalit boʻyicha saralash.
---

## Nima uchun bu mavzu muhim?

Reyting jadvali balga qarab, telefon kitobchasi familiyaga qarab, internet-doʻkon narxga qarab tartiblanadi. Ikkilik qidiruv faqat saralangan maʼlumotda ishlaydi. Shuning uchun saralash — kompyuterlar eng koʻp bajaradigan ishlardan biri.

Birinchi semestrda pufakchali saralashni yozgandik. U 100 ta element uchun bemalol ishlaydi, lekin universitetning 10 000 talabasini saralashda sekin, 1 million yozuvda esa amalda yaroqsiz. Bu maʼruzada saralash usullarini **tezligi** boʻyicha solishtirishni, sodda usullarni yozishni va katta maʼlumotda standart funksiyadan foydalanishni oʻrganamiz.

:::note
**Bu darsdan soʻng siz:**

- algoritm murakkabligini O-belgisi bilan baholay olasiz (*O*(*n*), *O*(*n*²), *O*(*n* log *n*));
- tanlab saralash va qoʻyib saralashni uch tilda yoza olasiz;
- birlashtirib saralash va tez saralash gʻoyasini tushuntira olasiz;
- barqaror saralash nima ekanini va qachon muhimligini ayta olasiz;
- standart saralash funksiyasi bilan yozuvlarni bir nechta kalit boʻyicha saralay olasiz.
:::

## Algoritm murakkabligi

Algoritmning tezligi soniyalarda emas, **kirish hajmi *n* oshganda amallar soni qanday oʻsishi** bilan oʻlchanadi. Bu **O-belgisi** (katta O) bilan yoziladi:

| Murakkablik | Nomi | *n* = 1000 da amallar | Misol |
|---|---|---|---|
| *O*(1) | oʻzgarmas | 1 | massiv elementiga murojaat |
| *O*(log *n*) | logarifmik | ~10 | ikkilik qidiruv |
| *O*(*n*) | chiziqli | 1 000 | chiziqli qidiruv, yigʻindi |
| *O*(*n* log *n*) | | ~10 000 | birlashtirib saralash, standart `sort` |
| *O*(*n*²) | kvadratik | 1 000 000 | pufakchali, tanlab, qoʻyib saralash |

Farq katta *n* da yaqqol koʻrinadi: *n* = 1 000 000 boʻlsa, *O*(*n*²) — 10¹² amal (kompyuterda bir necha soat), *O*(*n* log *n*) — 2 · 10⁷ amal (bir soniyadan kam).

## Tanlab saralash

**Tanlab saralash (selection sort)**: massivning saralanmagan qismidan eng kichik elementni topib, uni shu qismning boshiga qoʻyamiz.

1. *i* = 0 dan *n* − 2 gacha:
2. *i* … *n* − 1 oraligʻidan eng kichik elementning indeksini topamiz;
3. uni `a[i]` bilan almashtiramiz.

Har bir qadamdan keyin chap qism saralangan va oʻz joyida. Solishtirishlar soni har doim ~*n*²/2, lekin almashtirishlar *n* − 1 tadan oshmaydi — yozish qimmat boʻlgan hollarda foydali.

## Qoʻyib saralash

**Qoʻyib saralash (insertion sort)**: qoʻldagi kartalarni tartiblash kabi — har bir yangi elementni chapdagi saralangan qismga oʻz joyiga suqib qoʻyamiz. Kattaroq elementlar bittadan oʻngga suriladi.

Deyarli saralangan massivda qoʻyib saralash juda tez — taxminan *n* qadam. Shu sababli standart saralash funksiyalari kichik boʻlaklar uchun aynan shu usuldan foydalanadi.

## Birlashtirib va tez saralash

Ikkala usul ham **„boʻl va hukmronlik qil“** gʻoyasiga asoslangan va rekursiv:

| | Birlashtirib saralash (merge sort) | Tez saralash (quick sort) |
|---|---|---|
| Gʻoya | massivni ikkiga boʻlib, har birini saralab, ikki saralangan qismni birlashtirish | tayanch element tanlab, kichiklarni chapga, kattalarni oʻngga ajratib, har tomonni saralash |
| Murakkablik | har doim *O*(*n* log *n*) | oʻrtacha *O*(*n* log *n*), eng yomon *O*(*n*²) |
| Qoʻshimcha xotira | *O*(*n*) | *O*(log *n*) |
| Barqarorligi | barqaror | barqaror emas |

Bu algoritmlar keyingi kurslarda batafsil yoziladi; amalda ularning tayyor, sinalgan variantlari ishlatiladi.

## Barqaror saralash va kalit boʻyicha saralash

**Barqaror (stable) saralash** — teng kalitli elementlarning oʻzaro tartibini saqlaydi. Masalan, talabalar alifbo tartibida turgan boʻlsa va biz ularni ball boʻyicha barqaror saralasak, bir xil balli talabalar alifbo tartibida qoladi.

Standart funksiyalar:

| Til | Funksiya | Barqaror? | Kalit / solishtirish |
|---|---|---|---|
| Python | `a.sort()`, `sorted(a)` | ha | `key=lambda x: ...`, `reverse=True` |
| Java | `Arrays.sort`, `list.sort` | obyektlar uchun ha | `Comparator.comparing(...)` |
| C++ | `sort` / `stable_sort` (`<algorithm>`) | `sort` — yoʻq, `stable_sort` — ha | solishtirish funksiyasi (lambda) |

Bir nechta kalit boʻyicha saralash: avval birinchi kalit, tenglik boʻlsa — ikkinchisi. Masalan, ball **kamayish** tartibida, teng boʻlsa — ism **oʻsish** tartibida.

## Ishlangan misol 1: tanlab saralash va qadamlar soni

Birinchi qatorda *n*, ikkinchisida *n* ta son. Tanlab saralash bilan saralang; saralangan massivni va bajarilgan almashtirishlar sonini chiqaring (element oʻzi bilan almashtirilmaydi).

```python
n = int(input())
a = list(map(int, input().split()))

almashtirish = 0
for i in range(n - 1):
    eng_kichik = i
    for j in range(i + 1, n):
        if a[j] < a[eng_kichik]:
            eng_kichik = j
    if eng_kichik != i:
        a[i], a[eng_kichik] = a[eng_kichik], a[i]
        almashtirish += 1

print(*a)
print(almashtirish)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int almashtirish = 0;
        for (int i = 0; i < n - 1; i++) {
            int engKichik = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[engKichik]) engKichik = j;
            }
            if (engKichik != i) {
                int t = a[i];
                a[i] = a[engKichik];
                a[engKichik] = t;
                almashtirish++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(almashtirish);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int almashtirish = 0;
    for (int i = 0; i < n - 1; i++) {
        int engKichik = i;
        for (int j = i + 1; j < n; j++) {
            if (a[j] < a[engKichik]) engKichik = j;
        }
        if (engKichik != i) {
            swap(a[i], a[engKichik]);
            almashtirish++;
        }
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl << almashtirish << endl;
    return 0;
}
```

Kirish `5` va `29 10 14 37 13` boʻlsa:

| *i* | Eng kichigi | Massiv qadamdan keyin |
|---|---|---|
| 0 | 10 | 10 29 14 37 13 |
| 1 | 13 | 10 13 14 37 29 |
| 2 | 14 (oʻz joyida) | 10 13 14 37 29 |
| 3 | 29 | 10 13 14 29 37 |

Chiqadi: `10 13 14 29 37` va `3` — 4 qadamdan bittasida element allaqachon oʻz joyida edi. C++'da `swap` — `<utility>` dagi tayyor almashtirish funksiyasi.

## Ishlangan misol 2: qoʻyib saralash

Birinchi qatorda *n*, ikkinchisida *n* ta son. Qoʻyib saralash bilan saralang.

```python
n = int(input())
a = list(map(int, input().split()))

for i in range(1, n):
    joriy = a[i]
    j = i - 1
    while j >= 0 and a[j] > joriy:
        a[j + 1] = a[j]          # kattaroq elementni oʻngga suramiz
        j -= 1
    a[j + 1] = joriy             # boʻshagan joyga qoʻyamiz

print(*a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        for (int i = 1; i < n; i++) {
            int joriy = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > joriy) {
                a[j + 1] = a[j];         // kattaroq elementni oʻngga suramiz
                j--;
            }
            a[j + 1] = joriy;            // boʻshagan joyga qoʻyamiz
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    for (int i = 1; i < n; i++) {
        int joriy = a[i];
        int j = i - 1;
        while (j >= 0 && a[j] > joriy) {
            a[j + 1] = a[j];         // kattaroq elementni oʻngga suramiz
            j--;
        }
        a[j + 1] = joriy;            // boʻshagan joyga qoʻyamiz
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

Kirish `6` va `5 2 4 6 1 3` boʻlsa, saralangan chap qism har qadamda bittaga oʻsadi:

| *i* | *joriy* | Massiv qadamdan keyin |
|---|---|---|
| 1 | 2 | **2 5** 4 6 1 3 |
| 2 | 4 | **2 4 5** 6 1 3 |
| 3 | 6 | **2 4 5 6** 1 3 |
| 4 | 1 | **1 2 4 5 6** 3 |
| 5 | 3 | **1 2 3 4 5 6** |

Chiqadi: `1 2 3 4 5 6`. `while` shartida `j >= 0` birinchi turadi: qisqa tutashuv tufayli `a[-1]` ga murojaat qilinmaydi.

## Ishlangan misol 3: talabalar reytingi

Birinchi qatorda *n*, keyingi *n* qatorda talaba ismi va bali (probel bilan). Talabalarni ball boʻyicha kamayish tartibida, teng ballda — ism boʻyicha alifbo tartibida chiqaring.

```python
n = int(input())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append((ism, int(ball)))

talabalar.sort(key=lambda t: (-t[1], t[0]))

for ism, ball in talabalar:
    print(ism, ball)
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    record Talaba(String ism, int ball) {}

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            talabalar.add(new Talaba(in.next(), in.nextInt()));
        }

        talabalar.sort(Comparator.comparingInt(Talaba::ball).reversed()
                .thenComparing(Talaba::ism));

        for (Talaba t : talabalar) {
            System.out.println(t.ism() + " " + t.ball());
        }
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
    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;

    sort(talabalar.begin(), talabalar.end(), [](const Talaba& x, const Talaba& y) {
        if (x.ball != y.ball) return x.ball > y.ball;   // ball: kamayish
        return x.ism < y.ism;                           // ism: alifbo
    });

    for (const auto& t : talabalar) {
        cout << t.ism << " " << t.ball << endl;
    }
    return 0;
}
```

Kirish:

```text
5
Malika 88
Aziz 92
Bekzod 88
Dilnoza 75
Anvar 92
```

Chiqadi:

```text
Anvar 92
Aziz 92
Bekzod 88
Malika 88
Dilnoza 75
```

Pythonʼda kalit — juftlik `(-ball, ism)`: minus belgisi ball tartibini teskari qiladi, ism esa oddiy tartibda qoladi. Javaʼda `Comparator` zanjiri, C++'da solishtirish funksiyasi xuddi shu qoidani bildiradi: „*x* *y* dan oldin turadimi?“. Bu standart funksiyalar *O*(*n* log *n*) da ishlaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Katta maʼlumotda *O*(*n*²) saralash.** 100 000 element uchun pufakchali saralash vaqt chegarasidan oshadi; standart `sort` ishlating.
- **C++ solishtirish funksiyasida `<=`.** Solishtirish „qatʼiy kichik“ boʻlishi kerak; `<=` aniqlanmagan xatti-harakatga olib keladi.
- **Pythonʼda `sorted(a)` natijasini saqlamaslik.** `sorted` yangi roʻyxat qaytaradi; `a.sort()` esa joyida saralaydi va `None` qaytaradi.
- **Barqarorlikka tayanish.** C++'dagi `sort` barqaror emas; tartib muhim boʻlsa — `stable_sort` yoki toʻliq kalit.
:::

:::tip
Saralash algoritmini tekshirishda uchta maxsus kirishni albatta sinang: allaqachon saralangan, teskari saralangan va bir xil elementlardan iborat massiv.
:::

## Xulosa

- Algoritm tezligi *n* oshganda amallar soni qanday oʻsishi bilan baholanadi: *O*(*n*²) va *O*(*n* log *n*) farqi katta *n* da juda katta.
- Tanlab saralash — eng kichigini topib boshiga qoʻyish; qoʻyib saralash — har bir elementni saralangan qismga suqib qoʻyish; ikkalasi *O*(*n*²).
- Birlashtirib va tez saralash — „boʻl va hukmronlik qil“ asosidagi *O*(*n* log *n*) algoritmlar.
- Barqaror saralash teng elementlarning tartibini saqlaydi.
- Amalda standart `sort` ishlatiladi; kalit yoki solishtirish funksiyasi bilan istalgan tartib beriladi.

## Oʻzingizni tekshiring

:::check
Q: *n* 10 marta oshsa, *O*(*n*²) algoritmning ishlash vaqti necha marta oshadi?
A: Taxminan 100 marta.
Q: `4 3 2 1` massivini tanlab saralashda nechta almashtirish bajariladi?
A: 2 ta: 4 ↔ 1, keyin 3 ↔ 2; qolgan qadamlarda elementlar oʻz joyida.
Q: Qaysi holda qoʻyib saralash juda tez ishlaydi?
A: Massiv deyarli saralangan boʻlsa: har bir element oʻz joyiga yaqin, siljitishlar kam — taxminan *n* qadam.
Q: Barqaror saralash nima?
A: Kalitlari teng elementlarning dastlabki oʻzaro tartibini saqlaydigan saralash.
Q: Pythonʼda roʻyxatni ball boʻyicha kamayish, ism boʻyicha oʻsish tartibida qanday saralaysiz?
A: `a.sort(key=lambda t: (-t.ball, t.ism))` (yoki juftliklar uchun `(-t[1], t[0])`).
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: saralash algoritmlari
Tanlab, qoʻyib, birlashtirib va tez saralashning animatsiyasi.
:::

:::link https://docs.python.org/3/howto/sorting.html
Python: saralash boʻyicha qoʻllanma
`sorted`, `key`, `reverse` va barqarorlik haqida.
:::
