---
title: Piramidal va Shell saralash. Pufaksimon va Xoar saralash algoritmlari
summary: Piramida (uyum) tuzilmasi va piramidal saralash gʻoyasi, Shell saralashi va qadamlar ketma-ketligining taʼsiri, pufaksimon saralash va uning yaxshilangan varianti, Xoarning tez saralashi — ajratish, tayanch element tanlash, oʻrtacha va eng yomon holat.
---

## Nima uchun bu mavzu muhim?

Oddiy saralash usullari — pufaksimon, tanlab, qoʻyib — *O*(*n*²) vaqt oladi va 10⁵ elementdayoq sekinlashadi. Bu maʼruzadagi uchta algoritm shu muammoni turli yoʻllar bilan hal qiladi: **Shell** qoʻyib saralashni „uzoqdan“ boshlab tezlashtiradi, **piramidal saralash** maxsus tuzilma — uyumdan foydalanib kafolatli *O*(*n* log *n*) beradi, **Xoarning tez saralashi** esa amalda eng tez umumiy saralash hisoblanadi va koʻp standart kutubxonalarning asosida turadi.

Har bir algoritmda „qanday qilib tezroq?“ degan savolga oʻziga xos gʻoya bor — ularni tushunish yangi algoritmlarni loyihalashni oʻrgatadi.

:::note
**Bu darsdan soʻng siz:**

- pufaksimon saralash va uning yaxshilangan variantini tushuntira olasiz;
- Shell saralashi gʻoyasini va qadamlar ketma-ketligining taʼsirini koʻrsata olasiz;
- piramida (uyum) xossasini va uni massivda saqlashni tushuntira olasiz;
- piramidal saralash bosqichlarini va uning *O*(*n* log *n*) ekanini asoslay olasiz;
- Xoar ajratishini yozib, tayanch element tanlash murakkablikka qanday taʼsir qilishini koʻrsata olasiz.
:::

## Pufaksimon saralash

Qoʻshni elementlar notoʻgʻri tartibda boʻlsa, ular almashtiriladi; har bir oʻtishda eng katta element „pufakchadek“ oxiriga chiqadi. *n* − 1 oʻtish, *O*(*n*²) taqqoslash.

**Yaxshilangan variant**: agar oʻtish davomida bitta ham almashtirish boʻlmasa, massiv saralangan — sikl toʻxtatiladi. Shunda saralangan massiv uchun faqat bitta oʻtish (*O*(*n*)) kerak boʻladi. Pufaksimon saralash barqaror, lekin amalda deyarli ishlatilmaydi: u oʻquv algoritmi.

## Shell saralashi

**Shell saralashi** (Donald Shell, 1959) — qoʻyib saralashning umumlashmasi. Qoʻyib saralash elementni har safar faqat bitta oʻringa suradi, shuning uchun oxirdagi kichik element boshiga yetguncha koʻp vaqt ketadi. Shell gʻoyasi: avval bir-biridan *h* masofada turgan elementlarni saralash (*h*-saralash), keyin *h* ni kamaytirib, oxirida *h* = 1 — oddiy qoʻyib saralash bilan tugatish. Katta qadamlar elementlarni tezda „taxminan toʻgʻri“ joyiga olib keladi.

| Qadamlar ketma-ketligi | Misol (*n* = 100) | Eng yomon holat |
|---|---|---|
| Shell: *n*/2, *n*/4, …, 1 | 50, 25, 12, 6, 3, 1 | *O*(*n*²) |
| Knut: (3ᵏ − 1)/2 | 40, 13, 4, 1 | *O*(*n*^1,5) |
| Sedjvik, Siura va boshqalar | | yanada yaxshi |

Shell saralashi qoʻshimcha xotira talab qilmaydi va oʻrta hajmdagi massivlarda yaxshi ishlaydi; uning aniq murakkabligi qadamlarga bogʻliq va toʻliq tahlil qilinmagan.

## Piramida (uyum) va piramidal saralash

**Maks-piramida** (max-heap) — har bir tugun qiymati bolalari qiymatidan katta yoki teng boʻlgan toʻliq binar daraxt. U massivda koʻrsatkichlarsiz saqlanadi:

| Tugun | Indeks (0 dan) |
|---|---|
| ildiz | 0 |
| *i* ning chap bolasi | 2*i* + 1 |
| *i* ning oʻng bolasi | 2*i* + 2 |
| *i* ning otasi | (*i* − 1) / 2 |

Asosiy amal — **choʻktirish** (sift down): tugun bolalaridan kichik boʻlsa, katta bola bilan almashtiriladi va bu pastga qarab davom etadi; *O*(log *n*). Massivdan piramida qurish: oxirgi ichki tugundan (*n*/2 − 1) ildizgacha har birini choʻktirish — jami *O*(*n*).

**Piramidal saralash** (heapsort, J. Uilyams, 1964):

```text
1. massivdan maks-piramida quriladi
2. oxirgi = n − 1 dan 1 gacha:
       a[0] (eng katta) va a[oxirgi] almashtiriladi
       a[0] ni 0..oxirgi−1 oraligʻida choʻktiriladi
```

Har bir qadam eng katta elementni oʻz joyiga qoʻyadi; *n* ta choʻktirish × *O*(log *n*) = **kafolatli *O*(*n* log *n*)**, qoʻshimcha xotira *O*(1). Kamchiligi — barqaror emas va amalda tez saralashdan biroz sekinroq.

## Xoarning tez saralashi

**Tez saralash** (quicksort, Toni Xoar, 1960) — „boʻl va hukmron boʻl“: **tayanch** element (pivot) tanlanadi, massiv **ajratiladi** — chapga tayanchdan kichik yoki teng, oʻngga katta yoki teng elementlar — va ikki qism rekursiv saralanadi.

**Xoar ajratishi**: ikkita koʻrsatkich — chapdan *i* tayanchdan katta yoki teng elementni, oʻngdan *j* kichik yoki teng elementni qidiradi; topilsa almashtiriladi; *i* ≥ *j* boʻlganda ajratish tugaydi.

| Holat | Murakkablik | Qachon |
|---|---|---|
| Oʻrtacha | *O*(*n* log *n*) | tayanch qismlarni taxminan teng boʻladi |
| Eng yomon | *O*(*n*²) | tayanch har doim eng kichik yoki eng katta element |

Eng yomon holatdan qochish: tayanchni **oʻrtadan**, **tasodifiy** yoki „uchtadan oʻrtachasi“ (birinchi, oʻrta, oxirgi elementlarning medianasi) qilib olish. C++ `std::sort` tez saralashni piramidal saralash bilan himoyalaydi (introsort), Python va Java obyektlar uchun esa birlashtirib saralashga asoslangan Timsort ishlatadi.

## Ishlangan misol 1: Shell saralashi va qadamlar ketma-ketligi

*n* kiritiladi (1 ≤ *n* ≤ 1000). Kamayish tartibidagi `n, n−1, …, 1` massivini uch usulda saralang va taqqoslashlar sonini chiqaring: qoʻyib saralash (*h* = 1), Shell (*n*/2, *n*/4, …) va Knut qadamlari (… 40, 13, 4, 1).

```python
def shell(a, qadamlar):
    a = a[:]
    taqqoslash = 0
    for h in qadamlar:
        for i in range(h, len(a)):          # h-saralash: qoʻyib saralash h qadam bilan
            joriy = a[i]
            j = i
            while j >= h:
                taqqoslash += 1
                if a[j - h] > joriy:
                    a[j] = a[j - h]
                    j -= h
                else:
                    break
            a[j] = joriy
    return taqqoslash


n = int(input())
a = list(range(n, 0, -1))

yarim = []
h = n // 2
while h > 0:
    yarim.append(h)
    h //= 2

knut = []
h = 1
while h < n // 3:
    h = 3 * h + 1
while h >= 1:
    knut.append(h)
    h //= 3

print(shell(a, [1]), shell(a, yarim), shell(a, knut))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long shell(int[] asl, List<Integer> qadamlar) {
        int[] a = asl.clone();
        long taqqoslash = 0;
        for (int h : qadamlar) {
            for (int i = h; i < a.length; i++) {    // h-saralash: qoʻyib saralash h qadam bilan
                int joriy = a[i];
                int j = i;
                while (j >= h) {
                    taqqoslash++;
                    if (a[j - h] > joriy) {
                        a[j] = a[j - h];
                        j -= h;
                    } else {
                        break;
                    }
                }
                a[j] = joriy;
            }
        }
        return taqqoslash;
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = n - i;

        List<Integer> yarim = new ArrayList<>();
        for (int h = n / 2; h > 0; h /= 2) yarim.add(h);

        List<Integer> knut = new ArrayList<>();
        int h = 1;
        while (h < n / 3) h = 3 * h + 1;
        for (; h >= 1; h /= 3) knut.add(h);

        System.out.println(shell(a, List.of(1)) + " " + shell(a, yarim) + " " + shell(a, knut));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

long long shell(vector<int> a, const vector<int>& qadamlar) {   // a — nusxa
    long long taqqoslash = 0;
    for (int h : qadamlar) {
        for (int i = h; i < (int) a.size(); i++) {  // h-saralash: qoʻyib saralash h qadam bilan
            int joriy = a[i];
            int j = i;
            while (j >= h) {
                taqqoslash++;
                if (a[j - h] > joriy) {
                    a[j] = a[j - h];
                    j -= h;
                } else {
                    break;
                }
            }
            a[j] = joriy;
        }
    }
    return taqqoslash;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) a[i] = n - i;

    vector<int> yarim;
    for (int h = n / 2; h > 0; h /= 2) yarim.push_back(h);

    vector<int> knut;
    int h = 1;
    while (h < n / 3) h = 3 * h + 1;
    for (; h >= 1; h /= 3) knut.push_back(h);

    cout << shell(a, {1}) << " " << shell(a, yarim) << " " << shell(a, knut) << endl;
    return 0;
}
```

| *n* | Qoʻyib saralash | Shell (*n*/2) | Knut |
|---|---|---|---|
| 10 | 45 | 27 | 21 |
| 100 | 4 950 | 668 | 500 |
| 1000 | 499 500 | 11 716 | 8 550 |

Teskari tartibdagi massiv — qoʻyib saralash uchun eng yomon holat: *n*(*n* − 1)/2 taqqoslash. Shell saralashi xuddi shu ichki siklni ishlatadi, lekin katta qadamlar tufayli 1000 elementda 40–60 marta kam ish qiladi. Knut qadamlari *n*/2 dan ham yaxshiroq — qadamlar ketma-ketligini tanlash algoritmning eng muhim qismi.

## Ishlangan misol 2: Xoar ajratishi va tayanch tanlash

Birinchi satrda *n*, ikkinchisida *n* ta son. Massivni Xoar ajratishli tez saralash bilan saralang; saralangan massivni, soʻng tayanch birinchi element boʻlganda va oʻrta element boʻlganda bajarilgan taqqoslashlar sonini chiqaring.

```python
import sys

sys.setrecursionlimit(10000)


def tez_saralash(asl, tayanch_turi):
    a = asl[:]
    taqqoslash = 0

    def ajrat(chap, ong):
        nonlocal taqqoslash
        p = a[chap] if tayanch_turi == "birinchi" else a[(chap + ong) // 2]
        i, j = chap - 1, ong + 1
        while True:
            while True:                 # chapdan: >= p element
                i += 1
                taqqoslash += 1
                if a[i] >= p:
                    break
            while True:                 # oʻngdan: <= p element
                j -= 1
                taqqoslash += 1
                if a[j] <= p:
                    break
            if i >= j:
                return j
            a[i], a[j] = a[j], a[i]

    def saralash(chap, ong):
        if chap < ong:
            m = ajrat(chap, ong)
            saralash(chap, m)
            saralash(m + 1, ong)

    saralash(0, len(a) - 1)
    return a, taqqoslash


n = int(input())
a = list(map(int, input().split()))
natija, t1 = tez_saralash(a, "birinchi")
_, t2 = tez_saralash(a, "orta")
print(*natija)
print(t1, t2)
```
```java
import java.util.Scanner;

public class Main {
    static int[] a;
    static long taqqoslash;
    static boolean ortadan;

    static int ajrat(int chap, int ong) {
        int p = ortadan ? a[(chap + ong) / 2] : a[chap];
        int i = chap - 1, j = ong + 1;
        while (true) {
            do { i++; taqqoslash++; } while (a[i] < p);     // chapdan: >= p element
            do { j--; taqqoslash++; } while (a[j] > p);     // oʻngdan: <= p element
            if (i >= j) return j;
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    static void saralash(int chap, int ong) {
        if (chap < ong) {
            int m = ajrat(chap, ong);
            saralash(chap, m);
            saralash(m + 1, ong);
        }
    }

    static long tezSaralash(int[] asl, boolean ortadanTayanch) {
        a = asl.clone();
        taqqoslash = 0;
        ortadan = ortadanTayanch;
        saralash(0, a.length - 1);
        return taqqoslash;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] asl = new int[n];
        for (int i = 0; i < n; i++) asl[i] = in.nextInt();

        long t1 = tezSaralash(asl, false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        long t2 = tezSaralash(asl, true);
        System.out.println(sb);
        System.out.println(t1 + " " + t2);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

long long taqqoslash;
bool ortadan;

int ajrat(vector<int>& a, int chap, int ong) {
    int p = ortadan ? a[(chap + ong) / 2] : a[chap];
    int i = chap - 1, j = ong + 1;
    while (true) {
        do { i++; taqqoslash++; } while (a[i] < p);     // chapdan: >= p element
        do { j--; taqqoslash++; } while (a[j] > p);     // oʻngdan: <= p element
        if (i >= j) return j;
        swap(a[i], a[j]);
    }
}

void saralash(vector<int>& a, int chap, int ong) {
    if (chap < ong) {
        int m = ajrat(a, chap, ong);
        saralash(a, chap, m);
        saralash(a, m + 1, ong);
    }
}

long long tezSaralash(vector<int>& a, bool ortadanTayanch) {
    taqqoslash = 0;
    ortadan = ortadanTayanch;
    saralash(a, 0, (int) a.size() - 1);
    return taqqoslash;
}

int main() {
    int n;
    cin >> n;
    vector<int> asl(n);
    for (int& v : asl) cin >> v;

    vector<int> a1 = asl, a2 = asl;
    long long t1 = tezSaralash(a1, false);
    long long t2 = tezSaralash(a2, true);
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a1[i];
    cout << endl << t1 << " " << t2 << endl;
    return 0;
}
```

`10` va `35 33 42 10 14 19 27 44 26 31` uchun chiqish: `10 14 19 26 27 31 33 35 42 44` va `55 58` — tasodifiy tartibda ikkala tayanch deyarli bir xil. Lekin **saralangan** `1 2 … 20` massivida birinchi element tayanch boʻlsa 228, oʻrta element boʻlsa 107 taqqoslash; 200 elementda esa 20 298 va 1 743. Birinchi element saralangan massivda har doim eng kichik — ajratish bitta elementni ajratadi va algoritm *O*(*n*²) ga tushadi. Oʻrtadagi tayanch bu tuzoqdan qutqaradi.

## Ishlangan misol 3: massivdan maks-piramida qurish

Birinchi satrda *n*, ikkinchisida *n* ta son. Massivni joyida maks-piramidaga aylantiring (oxirgi ichki tugundan ildizgacha choʻktirish) va natija massivni chiqaring.

```python
def choktir(a, i, n):
    while True:
        chap = 2 * i + 1
        if chap >= n:                       # bola yoʻq — barg
            return
        katta = chap
        if chap + 1 < n and a[chap + 1] > a[chap]:
            katta = chap + 1                # katta bola
        if a[i] >= a[katta]:                # piramida xossasi bajarildi
            return
        a[i], a[katta] = a[katta], a[i]
        i = katta


n = int(input())
a = list(map(int, input().split()))
for i in range(n // 2 - 1, -1, -1):         # oxirgi ichki tugundan ildizgacha
    choktir(a, i, n)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    static void choktir(int[] a, int i, int n) {
        while (true) {
            int chap = 2 * i + 1;
            if (chap >= n) return;                      // bola yoʻq — barg
            int katta = chap;
            if (chap + 1 < n && a[chap + 1] > a[chap]) katta = chap + 1;   // katta bola
            if (a[i] >= a[katta]) return;               // piramida xossasi bajarildi
            int t = a[i]; a[i] = a[katta]; a[katta] = t;
            i = katta;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        for (int i = n / 2 - 1; i >= 0; i--) choktir(a, i, n);   // oxirgi ichki tugundan ildizgacha
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
#include <utility>
#include <vector>
using namespace std;

void choktir(vector<int>& a, int i, int n) {
    while (true) {
        int chap = 2 * i + 1;
        if (chap >= n) return;                          // bola yoʻq — barg
        int katta = chap;
        if (chap + 1 < n && a[chap + 1] > a[chap]) katta = chap + 1;   // katta bola
        if (a[i] >= a[katta]) return;                   // piramida xossasi bajarildi
        swap(a[i], a[katta]);
        i = katta;
    }
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    for (int i = n / 2 - 1; i >= 0; i--) choktir(a, i, n);   // oxirgi ichki tugundan ildizgacha
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl;
    return 0;
}
```

`7` va `4 10 3 5 1 8 9` uchun chiqish `10 5 9 4 1 8 3`. Daraxt koʻrinishida: ildiz 10, uning bolalari 5 va 9, 5 ning bolalari 4 va 1, 9 ning bolalari 8 va 3 — har bir ota bolalaridan katta. Eng katta element endi `a[0]` da: piramidal saralash shu yerdan boshlanadi — uni oxirgi element bilan almashtirib, ildizni qayta choʻktirish qoladi (buni amaliy topshiriqda bajarasiz). Piramida qurish *O*(*n*): pastki tugunlarning koʻpi barg, ular uchun choʻktirish deyarli bepul.

:::warning
**Koʻp uchraydigan xatolar**

- **Tez saralashda tayanchni doim birinchi element qilish.** Saralangan yoki deyarli saralangan maʼlumotda *O*(*n*²) va chuqur rekursiya; oʻrta yoki tasodifiy tayanch oling.
- **Piramida indekslarini 1 dan boshlab hisoblash.** 0 dan indekslashda bolalar 2*i* + 1 va 2*i* + 2; 1 dan boshlansa 2*i* va 2*i* + 1 — aralashtirmang.
- **Choʻktirishda kichik bola bilan almashtirish.** Faqat **katta** bola bilan almashtirish piramida xossasini saqlaydi.
- **Shell saralashini *h* = 1 siz tugatish.** Oxirgi qadam albatta 1 boʻlishi kerak, aks holda massiv toʻliq saralanmaydi.
:::

:::tip
Piramidani qogʻozda daraxt qilib chizing: massiv indekslarini tugunlarga yozing va har bir choʻktirishni strelka bilan koʻrsating. Daraxt koʻrinishida piramida xossasini tekshirish massivdagidan ancha oson.
:::

## Xulosa

- Pufaksimon saralash *O*(*n*²); almashtirishsiz oʻtishda toʻxtash saralangan massivni *O*(*n*) da tugatadi.
- Shell saralashi qoʻyib saralashni katta qadamlardan boshlaydi; samaradorlik qadamlar ketma-ketligiga bogʻliq.
- Maks-piramida massivda saqlanadi (bolalar 2*i* + 1, 2*i* + 2); qurish *O*(*n*), choʻktirish *O*(log *n*).
- Piramidal saralash kafolatli *O*(*n* log *n*) va *O*(1) qoʻshimcha xotira beradi, lekin barqaror emas.
- Xoarning tez saralashi oʻrtacha *O*(*n* log *n*), eng yomon *O*(*n*²); tayanchni toʻgʻri tanlash eng yomon holatdan saqlaydi.

## Oʻzingizni tekshiring

:::check
Q: Shell saralashi qoʻyib saralashdan nimasi bilan farq qiladi?
A: Avval bir-biridan *h* masofadagi elementlarni saralaydi va *h* ni 1 gacha kamaytiradi; elementlar katta sakrashlar bilan tez joyiga yaqinlashadi.
Q: 0 dan indekslangan piramidada 4-indeksli tugunning bolalari qaysi indekslarda?
A: 9 va 10.
Q: Nima uchun saralangan massivda birinchi element tayanch boʻlsa, tez saralash sekinlashadi?
A: Tayanch har doim eng kichik element boʻladi va ajratish massivni 1 va *n* − 1 qismlarga boʻladi — *O*(*n*²).
Q: Piramidal saralashning tez saralashdan afzalligi nima?
A: Eng yomon holatda ham *O*(*n* log *n*) kafolatlangan va qoʻshimcha xotira *O*(1).
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: saralash algoritmlari
Shell, piramidal va tez saralashning animatsiyasi (ingliz tilida).
:::

:::link https://en.wikipedia.org/wiki/Heapsort
Piramidal saralash
Algoritm, piramida qurish va murakkablik tahlili (ingliz tilida).
:::
