---
title: Fibonachchi izlash. Xeshlash orqali izlash. Ichki va tashqi saralash
summary: Fibonachchi izlash algoritmi va uning murakkabligi; xesh-funksiya, xesh-jadval, toʻqnashuvlar va zanjirlash usuli, yuklanish koeffitsienti; saralash algoritmlarining tasnifi, ichki va tashqi saralash, tartiblangan boʻlaklarni k-yoʻlli birlashtirish.
---

## Nima uchun bu mavzu muhim?

Oldingi maʼruzada saralangan massivda binar izlash *O*(log *n*) qadam olishini koʻrdik. Bu juda tez, lekin ikki savol qoladi. Birinchisi: bundan ham tezroq izlash mumkinmi? Ha — **xeshlash** oʻrtacha *O*(1) da topadi, shuning uchun lugʻatlar, maʼlumotlar bazasi indekslari va kompilyatorlarning nomlar jadvali aynan shunga qurilgan. Ikkinchisi: maʼlumot operativ xotiraga sigʻmasa-chi? Million emas, milliardlab yozuvni saralash uchun **tashqi saralash** kerak.

Bu maʼruzada Fibonachchi izlash, xesh-jadvallar va saralash algoritmlarining umumiy tasnifini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- Fibonachchi izlash gʻoyasini tushuntirib, uni yoza olasiz;
- xesh-funksiya, xesh-jadval va toʻqnashuv tushunchalarini tushuntira olasiz;
- zanjirlash usuli bilan oddiy xesh-jadval qura olasiz;
- saralash algoritmlarini barqarorlik, xotira va murakkablik boʻyicha tasniflay olasiz;
- ichki va tashqi saralash farqini va k-yoʻlli birlashtirishni tushuntira olasiz.
:::

## Fibonachchi izlash

**Fibonachchi izlash** — binar izlashning „qoʻshib-ayiradigan“ varianti: oraliqni oʻrtadan emas, ketma-ket Fibonachchi sonlari nisbatida boʻladi. *F*(*k*) ≥ *n* boʻlgan eng kichik Fibonachchi soni topiladi va har qadamda tekshiriladigan oʻrin `siljish + F(k − 2)` boʻladi; javobga qarab oraliq *F*(*k* − 1) yoki *F*(*k* − 2) ga qisqaradi.

| Xususiyat | Binar izlash | Fibonachchi izlash |
|---|---|---|
| Boʻlish nuqtasi | oʻrta (`/ 2`) | Fibonachchi nisbati (≈ 0,618) |
| Amallar | boʻlish | faqat qoʻshish va ayirish |
| Murakkablik | *O*(log *n*) | *O*(log *n*) |
| Qachon foydali | oddiy hollarda | boʻlish qimmat boʻlgan qurilmalarda; maʼlumot ketma-ket oʻqiladigan xotirada (yaqin oʻrinlarga murojaat) |

## Xeshlash orqali izlash

**Xesh-funksiya** kalitni (son, satr) jadval indeksiga aylantiradi: *h*(*kalit*) ∈ {0, …, *M* − 1}. **Xesh-jadval** — *M* ta „savat“dan iborat massiv; kalit oʻz savatiga yoziladi va keyin aynan shu savatdan qidiriladi.

Satr uchun keng tarqalgan xesh — **polinomial xesh** (Horner sxemasi):

```text
h = 0
har bir belgi c uchun: h = (h · 31 + kod(c)) mod M
```

Ikki xil kalit bitta savatga tushsa, bu **toʻqnashuv** (kolliziya) deyiladi. Ularni hal qilish usullari:

| Usul | Gʻoyasi |
|---|---|
| **Zanjirlash** | har bir savat — roʻyxat; toʻqnashgan kalitlar shu roʻyxatga qoʻshiladi |
| **Ochiq adreslash** | savat band boʻlsa, keyingi boʻsh savat qidiriladi (chiziqli yoki kvadratik sinash) |

**Yuklanish koeffitsienti** α = *n* / *M* (kalitlar soni / savatlar soni). Zanjirlashda oʻrtacha izlash vaqti *O*(1 + α). α oshib ketsa (masalan, 0,75 dan), jadval kattalashtiriladi va kalitlar qayta joylashtiriladi. Eng yomon holatda (hamma kalit bitta savatga tushsa) izlash *O*(*n*) ga aylanadi — shuning uchun yaxshi xesh-funksiya kalitlarni tekis taqsimlashi kerak.

Python `dict`/`set`, Java `HashMap`/`HashSet` va C++ `unordered_map`/`unordered_set` — aynan xesh-jadvallar.

## Saralash algoritmlari tasnifi

| Algoritm | Oʻrtacha | Eng yomon | Qoʻshimcha xotira | Barqaror |
|---|---|---|---|---|
| Pufaksimon | *O*(*n*²) | *O*(*n*²) | *O*(1) | ha |
| Tanlab | *O*(*n*²) | *O*(*n*²) | *O*(1) | yoʻq |
| Qoʻyib | *O*(*n*²) | *O*(*n*²) | *O*(1) | ha |
| Shell | ≈ *O*(*n*^1,3) | *O*(*n*²) | *O*(1) | yoʻq |
| Birlashtirib | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(*n*) | ha |
| Piramidal | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(1) | yoʻq |
| Tez (Xoar) | *O*(*n* log *n*) | *O*(*n*²) | *O*(log *n*) | yoʻq |

Taqqoslashga asoslangan har qanday saralash eng yomon holatda kamida Ω(*n* log *n*) taqqoslash talab qiladi — bu isbotlangan pastki chegara.

## Ichki va tashqi saralash

**Ichki saralash** — barcha maʼlumot operativ xotirada (RAM) turadi; yuqoridagi algoritmlar shunday. **Tashqi saralash** — maʼlumot diskda va xotiraga sigʻmaydi (masalan, 100 GB jurnal fayli, 8 GB RAM). Standart usul — **tashqi birlashtirib saralash**:

1. Faylni xotiraga sigʻadigan boʻlaklarga boʻlib oʻqish, har bir boʻlakni ichki saralash bilan saralash va alohida faylga („tartiblangan seriya“) yozish.
2. Barcha seriyalarni bir vaqtda **k-yoʻlli birlashtirish**: har bir seriyadan birinchi elementni olib, eng kichigini natijaga yozish va oʻsha seriyadan keyingisini olish. Eng kichikni tez topish uchun **uyum** (heap, ustuvor navbat) ishlatiladi — har qadam *O*(log *k*).

Tashqi saralashda asosiy xarajat — disk bilan almashinuv, shuning uchun algoritm maʼlumotni **ketma-ket** va iloji boricha kam marta oʻqishga moʻljallanadi.

## Ishlangan misol 1: Fibonachchi izlash

Birinchi satrda *n*, ikkinchisida oʻsish tartibidagi *n* ta son, uchinchisida *x*. *x* ning oʻrnini (1 dan; topilmasa −1) va tekshiruvlar sonini chiqaring.

```python
def fibonachchi_izlash(a, x):
    n = len(a)
    f2, f1 = 0, 1                       # F(k-2), F(k-1)
    f = f1 + f2                         # F(k)
    while f < n:
        f2, f1 = f1, f
        f = f1 + f2
    siljish = -1
    qadam = 0
    while f > 1:
        i = min(siljish + f2, n - 1)
        qadam += 1
        if a[i] < x:                    # oʻng qism: F(k-1) uzunlikda
            f, f1 = f1, f2
            f2 = f - f1
            siljish = i
        elif a[i] > x:                  # chap qism: F(k-2) uzunlikda
            f, f1 = f2, f1 - f2
            f2 = f - f1
        else:
            return i + 1, qadam
    if f1 == 1 and siljish + 1 < n:     # bitta element qolgan boʻlishi mumkin
        qadam += 1
        if a[siljish + 1] == x:
            return siljish + 2, qadam
    return -1, qadam


n = int(input())
a = list(map(int, input().split()))
x = int(input())
print(*fibonachchi_izlash(a, x))
```
```java
import java.util.Scanner;

public class Main {
    // natija: {oʻrin (1 dan yoki -1), qadamlar}
    static int[] fibonachchiIzlash(int[] a, int x) {
        int n = a.length;
        int f2 = 0, f1 = 1;                 // F(k-2), F(k-1)
        int f = f1 + f2;                    // F(k)
        while (f < n) {
            f2 = f1;
            f1 = f;
            f = f1 + f2;
        }
        int siljish = -1, qadam = 0;
        while (f > 1) {
            int i = Math.min(siljish + f2, n - 1);
            qadam++;
            if (a[i] < x) {                 // oʻng qism: F(k-1) uzunlikda
                f = f1;
                f1 = f2;
                f2 = f - f1;
                siljish = i;
            } else if (a[i] > x) {          // chap qism: F(k-2) uzunlikda
                f = f2;
                f1 = f1 - f2;
                f2 = f - f1;
            } else {
                return new int[] {i + 1, qadam};
            }
        }
        if (f1 == 1 && siljish + 1 < n) {   // bitta element qolgan boʻlishi mumkin
            qadam++;
            if (a[siljish + 1] == x) return new int[] {siljish + 2, qadam};
        }
        return new int[] {-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int[] r = fibonachchiIzlash(a, in.nextInt());
        System.out.println(r[0] + " " + r[1]);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

// natija: {oʻrin (1 dan yoki -1), qadamlar}
pair<int, int> fibonachchiIzlash(const vector<int>& a, int x) {
    int n = a.size();
    int f2 = 0, f1 = 1;                     // F(k-2), F(k-1)
    int f = f1 + f2;                        // F(k)
    while (f < n) {
        f2 = f1;
        f1 = f;
        f = f1 + f2;
    }
    int siljish = -1, qadam = 0;
    while (f > 1) {
        int i = min(siljish + f2, n - 1);
        qadam++;
        if (a[i] < x) {                     // oʻng qism: F(k-1) uzunlikda
            f = f1;
            f1 = f2;
            f2 = f - f1;
            siljish = i;
        } else if (a[i] > x) {              // chap qism: F(k-2) uzunlikda
            f = f2;
            f1 = f1 - f2;
            f2 = f - f1;
        } else {
            return {i + 1, qadam};
        }
    }
    if (f1 == 1 && siljish + 1 < n) {       // bitta element qolgan boʻlishi mumkin
        qadam++;
        if (a[siljish + 1] == x) return {siljish + 2, qadam};
    }
    return {-1, qadam};
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    int x;
    cin >> x;
    auto [orin, qadam] = fibonachchiIzlash(a, x);
    cout << orin << " " << qadam << endl;
    return 0;
}
```

`11` elementli `10 22 35 40 45 50 80 82 85 90 100` massivi uchun: *x* = 85 → `9 4`, *x* = 45 → `5 1` (birinchi tekshiruvda topildi: *F*(7) = 13 ≥ 11, birinchi oʻrin 0 + *F*(5) = 5), *x* = 23 → `-1 4`. Kodda boʻlish amali umuman yoʻq — faqat qoʻshish va ayirish. Oxirgi `if` — oraliqda bitta element qolganda uni alohida tekshirish uchun.

## Ishlangan misol 2: zanjirlash usulidagi xesh-jadval

Birinchi satrda soʻzlar (kalitlar), ikkinchisida soʻrov soʻzlari. *M* = 7 savatli xesh-jadval quring (polinomial xesh, asos 31; takroriy soʻz qayta qoʻshilmaydi). Har bir savat uzunligini, soʻng har bir soʻrov uchun `bor` yoki `yoʻq` va savatda bajarilgan taqqoslashlar sonini chiqaring.

```python
M = 7


def xesh(soz):
    h = 0
    for c in soz:
        h = (h * 31 + ord(c)) % M       # Horner sxemasi
    return h


jadval = [[] for _ in range(M)]         # har bir savat — roʻyxat (zanjir)
for soz in input().split():
    savat = jadval[xesh(soz)]
    if soz not in savat:
        savat.append(soz)

print(*[len(savat) for savat in jadval])
for soz in input().split():
    taqqoslash = 0
    topildi = False
    for s in jadval[xesh(soz)]:         # faqat bitta savat koʻriladi
        taqqoslash += 1
        if s == soz:
            topildi = True
            break
    print(soz, "bor" if topildi else "yoʻq", taqqoslash)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final int M = 7;

    static int xesh(String soz) {
        int h = 0;
        for (char c : soz.toCharArray()) h = (h * 31 + c) % M;   // Horner sxemasi
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<List<String>> jadval = new ArrayList<>();          // har bir savat — roʻyxat (zanjir)
        for (int i = 0; i < M; i++) jadval.add(new ArrayList<>());
        for (String soz : in.nextLine().trim().split("\\s+")) {
            List<String> savat = jadval.get(xesh(soz));
            if (!savat.contains(soz)) savat.add(soz);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < M; i++) {
            if (i > 0) sb.append(' ');
            sb.append(jadval.get(i).size());
        }
        System.out.println(sb);

        for (String soz : in.nextLine().trim().split("\\s+")) {
            int taqqoslash = 0;
            boolean topildi = false;
            for (String s : jadval.get(xesh(soz))) {           // faqat bitta savat koʻriladi
                taqqoslash++;
                if (s.equals(soz)) {
                    topildi = true;
                    break;
                }
            }
            System.out.println(soz + " " + (topildi ? "bor" : "yoʻq") + " " + taqqoslash);
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <list>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const int M = 7;

int xesh(const string& soz) {
    int h = 0;
    for (unsigned char c : soz) h = (h * 31 + c) % M;      // Horner sxemasi
    return h;
}

int main() {
    vector<list<string>> jadval(M);                         // har bir savat — roʻyxat (zanjir)
    string satr, soz;
    getline(cin, satr);
    istringstream kalitlar(satr);
    while (kalitlar >> soz) {
        auto& savat = jadval[xesh(soz)];
        if (find(savat.begin(), savat.end(), soz) == savat.end()) savat.push_back(soz);
    }

    for (int i = 0; i < M; i++) cout << (i ? " " : "") << jadval[i].size();
    cout << endl;

    getline(cin, satr);
    istringstream sorovlar(satr);
    while (sorovlar >> soz) {
        int taqqoslash = 0;
        bool topildi = false;
        for (const string& s : jadval[xesh(soz)]) {         // faqat bitta savat koʻriladi
            taqqoslash++;
            if (s == soz) {
                topildi = true;
                break;
            }
        }
        cout << soz << " " << (topildi ? "bor" : "yoʻq") << " " << taqqoslash << endl;
    }
    return 0;
}
```

Kirish:

```text
olma nok uzum anor shaftoli gilos olcha behi
anor banan behi
```

Chiqish:

```text
0 1 1 2 1 2 1
anor bor 1
banan yoʻq 1
behi bor 2
```

8 ta soʻz 7 ta savatga tarqaldi: ikkita savatda 2 tadan soʻz (toʻqnashuv), bittasi boʻsh. Har bir soʻrov **faqat bitta savatni** koʻradi — 8 ta soʻzning hammasini emas. Yuklanish koeffitsienti α = 8/7 ≈ 1,14, shuning uchun oʻrtacha bir-ikki taqqoslash yetadi. `banan` jadvalda yoʻq, lekin uning savatida boshqa soʻz bor — shuning uchun 1 ta taqqoslash bajarildi. Haqiqiy tizimlarda *M* tub son va jadval toʻlgan sari kattalashtiriladi.

## Ishlangan misol 3: tartiblangan seriyalarni k-yoʻlli birlashtirish

Birinchi satrda *k*, keyingi *k* satrda tartiblangan seriyalar: avval uzunligi, keyin elementlari. Barcha seriyalarni bitta tartiblangan ketma-ketlikka birlashtiring — tashqi saralashning ikkinchi bosqichi aynan shunday.

```python
import heapq

k = int(input())
seriyalar = []
for _ in range(k):
    q = list(map(int, input().split()))
    seriyalar.append(q[1:])

uyum = []                                   # (qiymat, seriya, oʻrin)
for s in range(k):
    if seriyalar[s]:
        heapq.heappush(uyum, (seriyalar[s][0], s, 0))

natija = []
while uyum:
    qiymat, s, i = heapq.heappop(uyum)      # eng kichik joriy element
    natija.append(qiymat)
    if i + 1 < len(seriyalar[s]):
        heapq.heappush(uyum, (seriyalar[s][i + 1], s, i + 1))
print(*natija)
```
```java
import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        int[][] seriyalar = new int[k][];
        for (int s = 0; s < k; s++) {
            int m = in.nextInt();
            seriyalar[s] = new int[m];
            for (int i = 0; i < m; i++) seriyalar[s][i] = in.nextInt();
        }

        // {qiymat, seriya, oʻrin}; qiymat boʻyicha tartib
        PriorityQueue<int[]> uyum = new PriorityQueue<>((p, q) -> Integer.compare(p[0], q[0]));
        for (int s = 0; s < k; s++) {
            if (seriyalar[s].length > 0) uyum.add(new int[] {seriyalar[s][0], s, 0});
        }

        StringBuilder sb = new StringBuilder();
        while (!uyum.isEmpty()) {
            int[] eng = uyum.poll();                        // eng kichik joriy element
            if (sb.length() > 0) sb.append(' ');
            sb.append(eng[0]);
            int s = eng[1], i = eng[2];
            if (i + 1 < seriyalar[s].length) uyum.add(new int[] {seriyalar[s][i + 1], s, i + 1});
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <functional>
#include <iostream>
#include <queue>
#include <tuple>
#include <vector>
using namespace std;

int main() {
    int k;
    cin >> k;
    vector<vector<int>> seriyalar(k);
    for (auto& seriya : seriyalar) {
        int m;
        cin >> m;
        seriya.resize(m);
        for (int& v : seriya) cin >> v;
    }

    // (qiymat, seriya, oʻrin); greater — eng kichigi tepada
    priority_queue<tuple<int, int, int>, vector<tuple<int, int, int>>, greater<>> uyum;
    for (int s = 0; s < k; s++) {
        if (!seriyalar[s].empty()) uyum.push({seriyalar[s][0], s, 0});
    }

    bool birinchi = true;
    while (!uyum.empty()) {
        auto [qiymat, s, i] = uyum.top();                   // eng kichik joriy element
        uyum.pop();
        cout << (birinchi ? "" : " ") << qiymat;
        birinchi = false;
        if (i + 1 < (int) seriyalar[s].size()) uyum.push({seriyalar[s][i + 1], s, i + 1});
    }
    cout << endl;
    return 0;
}
```

Kirish:

```text
3
4 1 5 9 14
3 2 3 10
5 0 6 7 8 20
```

Chiqish:

```text
0 1 2 3 5 6 7 8 9 10 14 20
```

Uyumda har doim koʻpi bilan *k* ta element turadi — har bir seriyadan bittadan. Shuning uchun xotira *O*(*k*), jami *N* ta elementni birlashtirish vaqti *O*(*N* log *k*). Haqiqiy tashqi saralashda seriyalar diskdagi fayllar boʻladi va har biridan bir vaqtda faqat kichik bufer oʻqiladi — algoritm esa aynan shu.

:::warning
**Koʻp uchraydigan xatolar**

- **Yomon xesh-funksiya.** Masalan, faqat birinchi harf boʻyicha xesh — „a“ bilan boshlanuvchi barcha soʻzlar bitta savatga tushadi va izlash *O*(*n*) ga aylanadi.
- **Xeshni moduldan chiqib ketishiga yoʻl qoʻyish.** `h * 31 + c` har qadamda `% M` olinmasa, son toʻlib ketadi (Java, C++).
- **Xesh-jadvaldan tartib kutish.** Xesh kalitlarni tartibsiz joylaydi; saralangan chiqish kerak boʻlsa, alohida saralang.
- **Fibonachchi izlashda chegarani tekshirmaslik.** `siljish + f2` massiv oxiridan oshib ketishi mumkin — `min(…, n − 1)` shart.
:::

:::tip
Xesh-funksiyangiz qanchalik yaxshi ekanini tekshirish uchun koʻp kalitni joylab, savatlar uzunligini chiqaring (2-misoldagidek). Uzunliklar bir-biriga yaqin boʻlsa, funksiya kalitlarni tekis taqsimlaydi.
:::

## Xulosa

- Fibonachchi izlash oraliqni Fibonachchi sonlari nisbatida boʻladi, faqat qoʻshish-ayirish ishlatadi va *O*(log *n*) ishlaydi.
- Xesh-jadval kalitni xesh-funksiya orqali savatga joylaydi; oʻrtacha izlash *O*(1 + α).
- Toʻqnashuvlar zanjirlash yoki ochiq adreslash bilan hal qilinadi; yuklanish oshganda jadval kattalashtiriladi.
- Saralash algoritmlari murakkablik, qoʻshimcha xotira va barqarorlik boʻyicha farqlanadi; taqqoslashli saralash Ω(*n* log *n*) dan tez boʻlolmaydi.
- Tashqi saralash: boʻlaklarni ichki saralash, soʻng uyum yordamida k-yoʻlli birlashtirish — *O*(*N* log *k*).

## Oʻzingizni tekshiring

:::check
Q: Fibonachchi izlash binar izlashdan qaysi amalni ishlatmasligi bilan farq qiladi?
A: Boʻlish amalini: oʻrinlar faqat qoʻshish va ayirish bilan hisoblanadi.
Q: Toʻqnashuv nima?
A: Ikki xil kalitning xesh-funksiyasi bir xil qiymat (bitta savat) berishi.
Q: Xesh-jadvalda 1000 ta kalit va 500 ta savat boʻlsa, yuklanish koeffitsienti qancha?
A: α = 1000 / 500 = 2.
Q: Nima uchun tashqi saralashda k-yoʻlli birlashtirishda uyum ishlatiladi?
A: *k* ta seriyaning joriy elementlaridan eng kichigini *O*(log *k*) da topish uchun.
Q: Qaysi saralashlar barqaror?
A: Pufaksimon, qoʻyib va birlashtirib saralash.
:::

:::link https://en.wikipedia.org/wiki/Hash_table
Xesh-jadval
Xesh-funksiyalar, toʻqnashuvlarni hal qilish va yuklanish koeffitsienti (ingliz tilida).
:::

:::link https://en.wikipedia.org/wiki/External_sorting
Tashqi saralash
Katta hajmdagi maʼlumotni disk yordamida saralash usullari (ingliz tilida).
:::
