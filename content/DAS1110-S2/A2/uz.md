---
title: Elementlarni qidirish usullari. Elementlar ustida amallar
summary: Chiziqli qidiruvning turlari (birinchi, oxirgi, barcha uchrashlar, shart boʻyicha), saralangan massivda quyi va yuqori chegara, standart qidiruv funksiyalari, massivdan element oʻchirish, qoʻshish va siklik siljitish.
---

## Nima uchun bu mavzu muhim?

Kutubxona katalogida kitobni, kontaktlar roʻyxatida ismni, jadvalda talabaning bahosini topish — bularning barchasi **qidiruv**. Topilgandan keyin esa odatda element ustida amal bajariladi: oʻchiriladi, yangisi qoʻshiladi yoki elementlar surib qoʻyiladi.

Birinchi semestrda chiziqli va ikkilik qidiruvning oddiy shaklini koʻrgan edik: „element bormi va qayerda?“. Amalda savollar murakkabroq: *oxirgi* uchrash qayerda, *nechta* uchraydi, *x* dan katta birinchi element qaysi? Bu mashgʻulotda shu savollarga javob beradigan usullarni va massivni oʻzgartiruvchi asosiy amallarni oʻrganamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- chiziqli qidiruv bilan birinchi, oxirgi va barcha uchrashlarni topa olasiz;
- saralangan massivda quyi chegara (lower bound) va yuqori chegarani ikkilik qidiruv bilan topa olasiz;
- Python, Java va C++'ning standart qidiruv funksiyalaridan foydalana olasiz;
- massivdan element oʻchirish va massivga element qoʻshishni elementlarni surish orqali bajara olasiz;
- massivni siklik siljita olasiz.
:::

## Chiziqli qidiruvning turlari

Chiziqli qidiruv massivni boshidan oxirigacha koʻrib chiqadi — *O*(*n*). U **saralanmagan** massivda ham ishlaydi. Savolga qarab sikl biroz oʻzgaradi:

| Savol | Usul |
|---|---|
| Birinchi uchrash | topilishi bilan siklni toʻxtatish (`break`) |
| Oxirgi uchrash | oxiridan boshlab qidirish yoki toʻxtamasdan oxirgi topilganini eslab qolish |
| Barcha uchrashlar / soni | toʻxtamasdan, har bir topilganini yozib borish yoki sanash |
| Shart boʻyicha (masalan, birinchi manfiy) | `a[i] == x` oʻrniga ixtiyoriy shart |

„Topilmadi“ holatini oldindan belgilang: odatda indeks uchun `-1` qiymati olinadi.

## Saralangan massivda chegaralar

Saralangan massivda ikkilik qidiruv *O*(log *n*) qadamda ishlaydi. Undan „element bormi?“ dan tashqari ikki muhim savolga javob olish mumkin:

- **quyi chegara** (lower bound) — `a[i] >= x` boʻlgan **birinchi** indeks;
- **yuqori chegara** (upper bound) — `a[i] > x` boʻlgan **birinchi** indeks.

Agar bunday element boʻlmasa, javob *n* (massiv oxiri). Ular yordamida *x* ning massivda **nechta** uchrashi oson topiladi: `yuqori − quyi`. Masalan, `1 2 2 2 5 7` va *x* = 2 uchun quyi chegara 1, yuqori chegara 4, uchrashlar soni 3.

Quyi chegarani topishning umumiy sxemasi — javob har doim `[chap, ong]` oraligʻida turadi:

```text
chap = 0, ong = n
toki chap < ong:
    orta = (chap + ong) / 2
    agar a[orta] < x:  chap = orta + 1
    aks holda:         ong = orta
javob: chap
```

Yuqori chegara uchun faqat shart `a[orta] <= x` ga oʻzgaradi.

## Standart qidiruv funksiyalari

| Til | Chiziqli | Ikkilik (saralangan massivda) |
|---|---|---|
| Python | `x in a`, `a.index(x)`, `a.count(x)` | `bisect_left(a, x)`, `bisect_right(a, x)` (`bisect` moduli) |
| Java | sikl yoki `list.indexOf(x)` | `Arrays.binarySearch(a, x)` — indeks yoki manfiy son |
| C++ | `find(a.begin(), a.end(), x)` | `lower_bound`, `upper_bound`, `binary_search` (`<algorithm>`) |

Pythonda `bisect_left` — quyi chegara, `bisect_right` — yuqori chegara. Java'ning `Arrays.binarySearch` takroriy elementlardan **qaysi biri** topilishini kafolatlamaydi, shuning uchun chegaralar kerak boʻlsa, ularni oʻzingiz yozasiz.

## Elementlar ustida amallar

Massiv xotirada uzluksiz joylashadi, shuning uchun oʻrtadan element oʻchirish yoki qoʻshish **elementlarni surish** orqali bajariladi:

- ***k*-oʻrindagi elementni oʻchirish**: `a[k+1]`, `a[k+2]`, … elementlarni bir qadam chapga surish, soʻng uzunlikni bittaga kamaytirish;
- ***k*-oʻringa element qoʻshish**: oxiridan boshlab `a[k]` gacha elementlarni bir qadam oʻngga surish, soʻng `a[k] = x`;
- **siklik siljitish** (*k* qadam oʻngga): har bir `a[i]` yangi massivning `(i + k) mod n` oʻrniga tushadi.

Bu amallar *O*(*n*) vaqt oladi. Pythonda `a.pop(k)`, `a.insert(k, x)`, C++'da `vector::erase` va `vector::insert` xuddi shu surishni ichida bajaradi.

## Ishlangan misol 1: barcha uchrashlarni oʻchirish

Birinchi satrda *n* va *x*, ikkinchisida *n* ta son. *x* ga teng barcha elementlarni oʻchiring: qolgan elementlarni kiritilgan tartibda, keyingi satrda oʻchirilganlar sonini chiqaring. Massiv boʻsh qolsa, birinchi satrga `-` chiqaring.

```python
n, x = map(int, input().split())
a = list(map(int, input().split()))

k = 0                       # yangi uzunlik
for i in range(n):
    if a[i] != x:
        a[k] = a[i]         # qoladigan elementni oldinga suramiz
        k += 1

print(*a[:k] if k > 0 else "-")
print(n - k)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), x = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int k = 0;                  // yangi uzunlik
        for (int i = 0; i < n; i++) {
            if (a[i] != x) {
                a[k] = a[i];        // qoladigan elementni oldinga suramiz
                k++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < k; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(k > 0 ? sb : "-");
        System.out.println(n - k);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, x;
    cin >> n >> x;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int k = 0;                  // yangi uzunlik
    for (int i = 0; i < n; i++) {
        if (a[i] != x) {
            a[k] = a[i];        // qoladigan elementni oldinga suramiz
            k++;
        }
    }

    if (k == 0) cout << "-";
    for (int i = 0; i < k; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl << n - k << endl;
    return 0;
}
```

`7 3` va `3 1 3 4 3 5 3` kiritilsa, chiqish: `1 4 5` va `4`. Har bir elementni alohida oʻchirib, qolganini surish *O*(*n*²) boʻlardi; bu yerda esa „ikki koʻrsatkich“ — `i` oʻqiydi, `k` yozadi — massivni bir marta koʻrib chiqadi, *O*(*n*).

## Ishlangan misol 2: saralangan massivda uchrashlar soni

Birinchi satrda *n*, ikkinchisida oʻsish tartibida saralangan *n* ta son, uchinchisida *q*, soʻng *q* ta soʻrov *x*. Har bir soʻrov uchun *x* massivda necha marta uchrashini chiqaring.

```python
from bisect import bisect_left, bisect_right

n = int(input())
a = list(map(int, input().split()))
q = int(input())
for _ in range(q):
    x = int(input())
    print(bisect_right(a, x) - bisect_left(a, x))
```
```java
import java.util.Scanner;

public class Main {
    // a[i] >= x (qatʼiy = false) yoki a[i] > x (qatʼiy = true) boʻlgan birinchi indeks
    static int chegara(int[] a, int x, boolean qatiy) {
        int chap = 0, ong = a.length;
        while (chap < ong) {
            int orta = (chap + ong) / 2;
            if (a[orta] < x || (qatiy && a[orta] == x)) chap = orta + 1;
            else ong = orta;
        }
        return chap;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int q = in.nextInt();
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            int x = in.nextInt();
            sb.append(chegara(a, x, true) - chegara(a, x, false)).append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    int q;
    cin >> q;
    while (q--) {
        int x;
        cin >> x;
        auto quyi = lower_bound(a.begin(), a.end(), x);
        auto yuqori = upper_bound(a.begin(), a.end(), x);
        cout << (yuqori - quyi) << '\n';
    }
    return 0;
}
```

Kirish:

```text
6
1 2 2 2 5 7
4
2
5
3
8
```

Chiqish:

```text
3
1
0
0
```

*x* = 3 uchun ikkala chegara ham 4 (birinchi `>= 3` element — 5), farqi 0. *x* = 8 uchun ikkalasi ham 6 — massiv oxiri. Har bir soʻrov *O*(log *n*) vaqt oladi, shuning uchun 10⁵ ta soʻrov ham tez bajariladi. Java versiyasida bitta funksiya ikkala chegarani topadi: `qatiy` parametri `<=` yoki `<` ni tanlaydi.

## Ishlangan misol 3: siklik siljitish

Birinchi satrda *n* va *k*, ikkinchisida *n* ta son. Massivni *k* qadam oʻngga siklik siljiting: oxirgi elementlar boshiga oʻtadi.

```python
n, k = map(int, input().split())
a = list(map(int, input().split()))

k %= n
b = [0] * n
for i in range(n):
    b[(i + k) % n] = a[i]

print(*b)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long k = in.nextLong();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int s = (int) (k % n);
        int[] b = new int[n];
        for (int i = 0; i < n; i++) b[(i + s) % n] = a[i];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(b[i]);
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
    long long k;
    cin >> n >> k;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int s = k % n;
    vector<int> b(n);
    for (int i = 0; i < n; i++) b[(i + s) % n] = a[i];

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;
    return 0;
}
```

`5 2` va `1 2 3 4 5` kiritilsa, chiqish: `4 5 1 2 3`. `5 7` kiritilsa ham natija xuddi shunday: 7 qadam siljitish 2 qadamga teng, chunki *n* = 5 qadamdan keyin massiv oʻz holiga qaytadi — shuning uchun avval `k % n` olinadi. *k* juda katta boʻlishi mumkinligi uchun Java va C++'da u `long` turida oʻqiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Saralanmagan massivda ikkilik qidiruv.** Natija tasodifiy boʻladi; avval saralang yoki chiziqli qidiruvdan foydalaning.
- **Chegaralarda cheksiz sikl.** `ong = orta` bilan birga `while (chap <= ong)` yozilsa, sikl toʻxtamaydi; sxemadagi `chap < ong` ni saqlang.
- **Pythonda roʻyxat ustida aylanayotib oʻchirish.** `for v in a: a.remove(v)` elementlarni oʻtkazib yuboradi; yangi roʻyxat yarating yoki ikki koʻrsatkich usulini qoʻllang.
- **Siljitishda `k % n` ni unutish.** Katta *k* bilan sikl behuda aylanadi yoki indeks chegaradan chiqadi.
:::

:::tip
Ikkilik qidiruvni yozgach, uni uchta chekka holatda tekshiring: *x* massivdagi barcha elementdan kichik, barchasidan katta va bir nechta marta takrorlanadi.
:::

## Xulosa

- Chiziqli qidiruv *O*(*n*), istalgan massivda ishlaydi; savolga qarab birinchi, oxirgi yoki barcha uchrashlar topiladi.
- Saralangan massivda quyi chegara (`>= x`) va yuqori chegara (`> x`) ikkilik qidiruv bilan *O*(log *n*) da topiladi; ularning farqi — uchrashlar soni.
- Python `bisect`, C++ `lower_bound`/`upper_bound` tayyor chegaralarni beradi.
- Massivning oʻrtasidan oʻchirish va qoʻshish elementlarni surish bilan bajariladi, *O*(*n*).
- Siklik siljitishda element `(i + k) mod n` oʻringa tushadi.

## Oʻzingizni tekshiring

:::check
Q: `4 1 4 2 4` massivida 4 ning oxirgi uchrashi qaysi indeksda (0 dan)?
A: 4-indeksda (oxirgi element).
Q: `1 3 3 6 9` massivida *x* = 4 uchun quyi chegara nechaga teng?
A: 3: `>= 4` boʻlgan birinchi element — 6, uning indeksi 3.
Q: Saralangan massivda *x* ning uchrashlar sonini qanday topish mumkin?
A: Yuqori chegaradan quyi chegarani ayirib: `upper_bound − lower_bound`.
Q: 1000 elementli massivning boshidan bitta element oʻchirilsa, nechta element suriladi?
A: 999 ta — qolgan barcha elementlar bir qadam chapga suriladi.
Q: `1 2 3 4` massivini 1 qadam oʻngga siklik siljitsak, natija qanday?
A: `4 1 2 3`.
:::

:::link https://docs.python.org/3/library/bisect.html
Python: bisect moduli
`bisect_left`, `bisect_right` va saralangan roʻyxat bilan ishlash (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/algorithm/lower_bound
C++: lower_bound
Quyi chegara funksiyasi va misollar (ingliz tilida).
:::
