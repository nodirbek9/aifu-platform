---
title: Saralash algoritmlari. Ichki va tashqi saralash. Piramidal va Shell saralash (amaliy mashgʻulot)
summary: Birlashtirib saralash va uning taqqoslashlar soni, taqqoslashsiz sanab saralash (counting sort) va uning chiziqli vaqti, barqaror saralash yordamida koʻp kalitli tartiblash; saralash algoritmini masalaga qarab tanlash.
---

## Nima uchun bu mavzu muhim?

Maʼruzalarda saralash algoritmlarining tasnifini koʻrdik. Amaliyotda esa savol aniq: mening maʼlumotim uchun qaysi algoritm eng yaxshi? Javob maʼlumotga bogʻliq: elementlar soni, qiymatlar oraligʻi, barqarorlik talabi, xotira cheklovi. Masalan, 10⁶ ta talabani yoshi boʻyicha saralash uchun *O*(*n* log *n*) ham shart emas — yosh 0 dan 120 gacha boʻlgani uchun *O*(*n*) yetadi.

Bu mashgʻulotda uchta amaliy usulni koʻramiz: birlashtirib saralash (tashqi saralashning asosi), taqqoslashsiz sanab saralash va barqaror saralash yordamida bir nechta kalit boʻyicha tartiblash.

:::note
**Bu mashgʻulotdan soʻng siz:**

- birlashtirib saralashni rekursiv yozib, uning taqqoslashlar sonini oʻlchay olasiz;
- qiymatlar oraligʻi kichik boʻlganda sanab saralashni qoʻllay olasiz;
- barqaror saralashni ketma-ket qoʻllab, bir nechta kalit boʻyicha tartiblay olasiz;
- masala shartiga qarab saralash algoritmini tanlay olasiz.
:::

## Birlashtirib saralash

**Birlashtirib saralash** (merge sort, J. fon Neyman, 1945): massiv ikkiga boʻlinadi, har bir yarim rekursiv saralanadi, soʻng ikki saralangan yarim **birlashtiriladi** — har qadamda ikkala yarimning joriy elementlaridan kichigi natijaga yoziladi.

| Xususiyat | Qiymat |
|---|---|
| Vaqt (har qanday holatda) | *O*(*n* log *n*) |
| Qoʻshimcha xotira | *O*(*n*) |
| Barqarorlik | ha (teng elementlarda chapdagisi oldin olinadi) |
| Maʼlumotni oʻqish | ketma-ket — shuning uchun tashqi saralash va bogʻlangan roʻyxatlar uchun qulay |

## Sanab saralash

Taqqoslashga asoslangan saralash Ω(*n* log *n*) dan tez boʻlolmaydi. Lekin qiymatlar **kichik oraliqda** (0 … *k*) boʻlsa, taqqoslash umuman kerak emas: har bir qiymat necha marta uchraganini sanab, natijani sanoqlar boʻyicha yozish mumkin. **Sanab saralash** (counting sort) vaqti *O*(*n* + *k*), xotirasi *O*(*k*).

| Qachon foydali | Qachon yaramaydi |
|---|---|
| yosh, baho, oy raqami, ASCII belgi | qiymatlar oraligʻi juda katta (masalan, 10⁹) |
| *k* taxminan *n* yoki undan kichik | haqiqiy sonlar, satrlar |

Sanab saralash gʻoyasi **razryadli saralash** (radix sort) ning asosi: katta sonlar raqamma-raqam, har gal barqaror sanab saralash bilan saralanadi.

## Koʻp kalitli saralash

„Avval guruh boʻyicha, guruh ichida bal boʻyicha kamayish tartibida“ — bunday tartibni ikki usulda olish mumkin:

1. **Birlashgan kalit**: bitta saralash, taqqoslash funksiyasi avval guruhni, teng boʻlsa balni solishtiradi.
2. **Barqaror saralashni ketma-ket qoʻllash**: avval **ikkinchi darajali** kalit (bal) boʻyicha, keyin **asosiy** kalit (guruh) boʻyicha barqaror saralash. Ikkinchi saralash teng guruhlarda birinchi saralash tartibini saqlaydi.

Ikkinchi usul jadvallarda „ustun sarlavhasini bosib saralash“ ning ishlash tartibi: oxirgi bosilgan ustun asosiy kalit boʻladi. Bu faqat **barqaror** saralash bilan toʻgʻri ishlaydi.

## Ishlangan misol 1: birlashtirib saralash

Birinchi satrda *n*, ikkinchisida *n* ta son. Birlashtirib saralash bilan saralang; saralangan massivni va birlashtirish paytidagi taqqoslashlar sonini chiqaring.

```python
import sys

sys.setrecursionlimit(10000)
taqqoslash = 0


def birlashtirib_saralash(a):
    global taqqoslash
    if len(a) <= 1:
        return a
    orta = len(a) // 2
    chap = birlashtirib_saralash(a[:orta])
    ong = birlashtirib_saralash(a[orta:])
    natija = []
    i = j = 0
    while i < len(chap) and j < len(ong):
        taqqoslash += 1
        if chap[i] <= ong[j]:           # <= — barqarorlik uchun
            natija.append(chap[i])
            i += 1
        else:
            natija.append(ong[j])
            j += 1
    natija.extend(chap[i:])             # qolgan qism
    natija.extend(ong[j:])
    return natija


n = int(input())
a = list(map(int, input().split()))
print(*birlashtirib_saralash(a))
print(taqqoslash)
```
```java
import java.util.Scanner;

public class Main {
    static long taqqoslash = 0;

    static void saralash(int[] a, int[] vaqtincha, int chap, int ong) {    // [chap, ong)
        if (ong - chap <= 1) return;
        int orta = (chap + ong) / 2;
        saralash(a, vaqtincha, chap, orta);
        saralash(a, vaqtincha, orta, ong);
        int i = chap, j = orta, k = chap;
        while (i < orta && j < ong) {
            taqqoslash++;
            if (a[i] <= a[j]) vaqtincha[k++] = a[i++];      // <= — barqarorlik uchun
            else vaqtincha[k++] = a[j++];
        }
        while (i < orta) vaqtincha[k++] = a[i++];           // qolgan qism
        while (j < ong) vaqtincha[k++] = a[j++];
        for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        saralash(a, new int[n], 0, n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(taqqoslash);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

long long taqqoslash = 0;

void saralash(vector<int>& a, vector<int>& vaqtincha, int chap, int ong) {   // [chap, ong)
    if (ong - chap <= 1) return;
    int orta = (chap + ong) / 2;
    saralash(a, vaqtincha, chap, orta);
    saralash(a, vaqtincha, orta, ong);
    int i = chap, j = orta, k = chap;
    while (i < orta && j < ong) {
        taqqoslash++;
        if (a[i] <= a[j]) vaqtincha[k++] = a[i++];          // <= — barqarorlik uchun
        else vaqtincha[k++] = a[j++];
    }
    while (i < orta) vaqtincha[k++] = a[i++];               // qolgan qism
    while (j < ong) vaqtincha[k++] = a[j++];
    for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n), vaqtincha(n);
    for (int& v : a) cin >> v;
    saralash(a, vaqtincha, 0, n);
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl << taqqoslash << endl;
    return 0;
}
```

`7` va `38 27 43 3 9 82 10` uchun chiqish: `3 9 10 27 38 43 82` va `13`. Saralangan va teskari tartibdagi 8 elementli massivlar uchun ham taqqoslashlar soni 12 — birlashtirib saralash kirish tartibiga deyarli befarq. Java va C++ versiyalarida bitta `vaqtincha` massiv oldindan ajratiladi va barcha birlashtirishlarda qayta ishlatiladi — har bir chaqiruvda yangi massiv yaratish sekinroq boʻlardi.

## Ishlangan misol 2: sanab saralash

Birinchi satrda *n*, ikkinchisida *n* ta talabaning yoshi (0 dan 120 gacha). Yoshlarni taqqoslashsiz sanab saralash bilan saralang; saralangan roʻyxatni va eng koʻp uchragan yoshni (bir nechta boʻlsa — eng kichigini) va uning sonini chiqaring.

```python
n = int(input())
yoshlar = list(map(int, input().split()))

sanoq = [0] * 121                       # har bir yosh necha marta uchraydi
for y in yoshlar:
    sanoq[y] += 1

natija = []
for y in range(121):                    # yoshlar oʻsish tartibida
    natija.extend([y] * sanoq[y])

eng_kop = max(range(121), key=lambda y: (sanoq[y], -y))
print(*natija)
print(eng_kop, sanoq[eng_kop])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] sanoq = new int[121];                         // har bir yosh necha marta uchraydi
        for (int i = 0; i < n; i++) sanoq[in.nextInt()]++;

        StringBuilder sb = new StringBuilder();
        int engKop = 0;
        for (int y = 0; y <= 120; y++) {                    // yoshlar oʻsish tartibida
            for (int k = 0; k < sanoq[y]; k++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(y);
            }
            if (sanoq[y] > sanoq[engKop]) engKop = y;
        }
        System.out.println(sb);
        System.out.println(engKop + " " + sanoq[engKop]);
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
    vector<int> sanoq(121, 0);                              // har bir yosh necha marta uchraydi
    for (int i = 0; i < n; i++) {
        int y;
        cin >> y;
        sanoq[y]++;
    }

    bool birinchi = true;
    int engKop = 0;
    for (int y = 0; y <= 120; y++) {                        // yoshlar oʻsish tartibida
        for (int k = 0; k < sanoq[y]; k++) {
            cout << (birinchi ? "" : " ") << y;
            birinchi = false;
        }
        if (sanoq[y] > sanoq[engKop]) engKop = y;
    }
    cout << endl << engKop << " " << sanoq[engKop] << endl;
    return 0;
}
```

`8` va `19 21 19 20 22 19 21 18` uchun chiqish: `18 19 19 19 20 21 21 22` va `19 3`. Algoritmda bitta ham taqqoslash (`<`) yoʻq: har bir yosh toʻgʻridan-toʻgʻri `sanoq` massivining indeksi. 10⁶ talaba uchun 10⁶ + 121 amal — har qanday taqqoslashli saralashdan tez. Eng koʻp uchragan qiymat ham shu sanoqlardan bepul olinadi — bu sanab saralashning qoʻshimcha foydasi.

## Ishlangan misol 3: barqaror saralash bilan koʻp kalitli tartiblash

Birinchi satrda *n*, keyingi *n* satrda talaba ismi, guruh raqami va bali. Talabalarni guruh boʻyicha oʻsish, guruh ichida bal boʻyicha kamayish tartibida chiqaring; teng ballilar kiritilgan tartibda qoladi. Ikki barqaror saralash bilan bajaring: avval bal, keyin guruh boʻyicha.

```python
n = int(input())
talabalar = []
for _ in range(n):
    ism, guruh, ball = input().split()
    talabalar.append((ism, int(guruh), int(ball)))

talabalar.sort(key=lambda t: -t[2])     # 1) ikkinchi darajali kalit: bal (kamayish)
talabalar.sort(key=lambda t: t[1])      # 2) asosiy kalit: guruh — barqaror!

for ism, guruh, ball in talabalar:
    print(guruh, ism, ball)
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    record Talaba(String ism, int guruh, int ball) {}

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt(), in.nextInt()));

        talabalar.sort(Comparator.comparingInt(Talaba::ball).reversed());   // 1) ikkinchi darajali kalit: bal (kamayish)
        talabalar.sort(Comparator.comparingInt(Talaba::guruh));             // 2) asosiy kalit: guruh — barqaror!

        for (Talaba t : talabalar) System.out.println(t.guruh() + " " + t.ism() + " " + t.ball());
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
    int guruh, ball;
};

int main() {
    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.guruh >> t.ball;

    stable_sort(talabalar.begin(), talabalar.end(),          // 1) ikkinchi darajali kalit: bal (kamayish)
                [](const Talaba& x, const Talaba& y) { return x.ball > y.ball; });
    stable_sort(talabalar.begin(), talabalar.end(),          // 2) asosiy kalit: guruh — barqaror!
                [](const Talaba& x, const Talaba& y) { return x.guruh < y.guruh; });

    for (const auto& t : talabalar) cout << t.guruh << " " << t.ism << " " << t.ball << endl;
    return 0;
}
```

Kirish:

```text
6
Aziz 102 85
Malika 101 92
Bekzod 102 92
Dilnoza 101 85
Anvar 102 85
Sardor 101 70
```

Chiqish:

```text
101 Malika 92
101 Dilnoza 85
101 Sardor 70
102 Bekzod 92
102 Aziz 85
102 Anvar 85
```

Birinchi saralashdan keyin talabalar bal boʻyicha tartiblangan. Ikkinchi saralash guruhlarni ajratadi, lekin **barqaror** boʻlgani uchun har bir guruh ichida bal tartibini buzmaydi. 102-guruhdagi Aziz va Anvarning bali teng (85) — ular kiritilgan tartibda qoldi. C++'da `sort` emas, `stable_sort` muhim: oddiy `sort` barqaror emas va natija notoʻgʻri boʻlishi mumkin.

:::warning
**Koʻp uchraydigan xatolar**

- **Birlashtirishda `<` yozish.** Teng elementlarda oʻngdagisi oldin olinadi va barqarorlik buziladi; `<=` yozing.
- **Sanab saralashni katta oraliqqa qoʻllash.** Qiymatlar 10⁹ gacha boʻlsa, sanoq massivi xotiraga sigʻmaydi; taqqoslashli saralash yoki razryadli saralash kerak.
- **Koʻp kalitli saralashda tartibni teskari qilish.** Avval **asosiy** kalit boʻyicha saralasangiz, ikkinchi saralash uni buzadi; avval ikkinchi darajali, keyin asosiy.
- **C++'da barqarorlikni `sort` dan kutish.** Barqaror tartib kerak boʻlsa — `stable_sort`.
:::

:::tip
Saralash algoritmini tanlashda uch savolga javob bering: maʼlumot xotiraga sigʻadimi (sigʻmasa — tashqi birlashtirib saralash)? Qiymatlar oraligʻi kichikmi (ha — sanab saralash)? Teng elementlar tartibi muhimmi (ha — barqaror saralash)?
:::

## Xulosa

- Birlashtirib saralash har doim *O*(*n* log *n*), barqaror, *O*(*n*) qoʻshimcha xotira talab qiladi va maʼlumotni ketma-ket oʻqiydi.
- Sanab saralash taqqoslashsiz *O*(*n* + *k*) da ishlaydi — qiymatlar oraligʻi kichik boʻlganda.
- Koʻp kalitli tartib: avval ikkinchi darajali, keyin asosiy kalit boʻyicha barqaror saralash.
- Algoritm maʼlumot hajmi, qiymatlar oraligʻi va barqarorlik talabiga qarab tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun birlashtirib saralash tashqi saralash uchun qulay?
A: U maʼlumotni ketma-ket oʻqiydi va yozadi; diskda tasodifiy murojaat kerak emas.
Q: Sanab saralash nima uchun Ω(*n* log *n*) chegarasidan tez ishlay oladi?
A: U elementlarni oʻzaro taqqoslamaydi — chegara faqat taqqoslashga asoslangan saralashlarga tegishli.
Q: Jadvalni avval „ism“, keyin „guruh“ ustuni boʻyicha barqaror saralasak, qaysi kalit asosiy boʻladi?
A: „Guruh“ — oxirgi saralash; guruh ichida ism tartibi saqlanadi.
Q: 5 · 10⁶ ta baho (0 dan 100 gacha) ni saralash uchun qaysi algoritm eng tez?
A: Sanab saralash: *O*(*n* + 101).
:::

:::link https://en.wikipedia.org/wiki/Counting_sort
Sanab saralash
Algoritm, uning murakkabligi va razryadli saralash bilan aloqasi (ingliz tilida).
:::
