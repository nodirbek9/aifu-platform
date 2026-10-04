---
title: Daraxtlar. Fenvik daraxti. Kesmalar daraxti. Muvozanatlashtirilgan daraxtlar (amaliy mashgʻulot)
summary: Oraliq soʻrovlari masalasi; prefiks yigʻindilar — oʻzgarmas massivda O(1) soʻrov; kesmalar daraxti — oraliq minimumi va element yangilash O(log n) da; Fenvik daraxti gʻoyasi; binar qidiruv daraxtidan tugun oʻchirishning uch holati.
---

## Nima uchun bu mavzu muhim?

Koʻp masalalarda massiv ustida ikki xil amal aralash keladi: **oraliq soʻrovi** („3-kundan 10-kungacha savdo yigʻindisi qancha?“, „shu oraliqda eng past harorat qaysi?“) va **yangilash** („5-kun maʼlumoti tuzatildi“). Har soʻrovda oraliqni aylanib chiqish *O*(*n*) — 10⁵ ta soʻrovda bu 10¹⁰ amal. Daraxtga asoslangan tuzilmalar ikkala amalni ham *O*(log *n*) da bajaradi.

Bu mashgʻulotda oddiydan murakkabga boramiz: prefiks yigʻindilar (yangilashsiz), kesmalar daraxti (yangilash bilan) va binar qidiruv daraxtidan oʻchirish.

:::note
**Bu mashgʻulotdan soʻng siz:**

- prefiks yigʻindilar bilan oraliq yigʻindisini *O*(1) da topa olasiz;
- kesmalar daraxtini qurib, oraliq minimumini va element yangilashni *O*(log *n*) da bajara olasiz;
- Fenvik daraxti qachon qulay ekanini tushuntira olasiz;
- BQD dan barg, bitta bolali va ikki bolali tugunni toʻgʻri oʻchira olasiz.
:::

## Prefiks yigʻindilar

`p[i] = a[1] + a[2] + … + a[i]`, `p[0] = 0`. U holda [*l*, *r*] oraliq yigʻindisi:

```text
sum(l, r) = p[r] − p[l − 1]
```

Tayyorlash *O*(*n*), har bir soʻrov *O*(1). Kamchiligi — bitta element oʻzgarsa, undan keyingi barcha `p[i]` ni qayta hisoblash kerak: *O*(*n*). Shuning uchun prefiks yigʻindilar **oʻzgarmas** maʼlumot uchun.

## Kesmalar daraxti

**Kesmalar daraxti** (segment tree) — har bir tugun massivning bir kesmasi uchun natijani (yigʻindi, minimum, maksimum) saqlaydigan binar daraxt. Ildiz — butun massiv, barglar — alohida elementlar. Massivda saqlash: *n* ni ikkining darajasiga yaxlitlab, barglar `t[n … 2n − 1]` da, tugun `i` ning bolalari `2i` va `2i + 1` da turadi.

| Amal | Qanday | Murakkablik |
|---|---|---|
| Yangilash `a[i] = x` | bargni oʻzgartirib, ildizgacha otalarni qayta hisoblash | *O*(log *n*) |
| Oraliq soʻrovi [*l*, *r*] | ikki chegaradan yuqoriga koʻtarilib, toʻliq ichkaridagi tugunlarni birlashtirish | *O*(log *n*) |

Kesmalar daraxti istalgan **assotsiativ** amal (yigʻindi, min, max, EKUB) bilan ishlaydi.

## Fenvik daraxti

**Fenvik daraxti** (binar indeksli daraxt, 1994) — prefiks yigʻindilar uchun ixchamroq tuzilma: bitta massiv, *n* ta element, `i & (−i)` bit amali bilan yurish. Yangilash va prefiks yigʻindisi *O*(log *n*), kodi kesmalar daraxtidan ancha qisqa. Cheklovi: asosan yigʻindi kabi „teskarilanadigan“ amallar uchun (minimum uchun noqulay). Uni amaliy topshiriqda oʻzingiz yozasiz.

| Tuzilma | Yangilash | Soʻrov | Qachon |
|---|---|---|---|
| Prefiks yigʻindilar | *O*(*n*) | *O*(1) | maʼlumot oʻzgarmaydi |
| Fenvik daraxti | *O*(log *n*) | *O*(log *n*) | yigʻindi + yangilash |
| Kesmalar daraxti | *O*(log *n*) | *O*(log *n*) | min/max yoki murakkab amal + yangilash |

## BQD dan oʻchirish

| Holat | Qanday |
|---|---|
| Tugun — barg | shunchaki olib tashlanadi |
| Bitta bolasi bor | bola tugun oʻrniga qoʻyiladi |
| Ikki bolasi bor | oʻng qism daraxtdagi **eng kichik** kalit (keyingi element) tugun oʻrniga koʻchiriladi, soʻng oʻsha element oʻng qism daraxtdan oʻchiriladi |

Uchinchi holatda BQD xossasi saqlanadi: keyingi element chapdagilarning hammasidan katta va oʻngdagilardan kichik yoki teng.

## Ishlangan misol 1: prefiks yigʻindilar

Birinchi satrda *n*, ikkinchisida *n* ta son, uchinchisida *q*, keyin *q* ta soʻrov `l r` (1 dan). Har bir soʻrov uchun [*l*, *r*] yigʻindisini chiqaring.

```python
import sys

kirish = sys.stdin.read().split()           # katta kirish uchun tez oʻqish
n = int(kirish[0])
a = list(map(int, kirish[1:n + 1]))
p = [0] * (n + 1)
for i in range(n):
    p[i + 1] = p[i] + a[i]                  # p[i] — dastlabki i ta element yigʻindisi

q = int(kirish[n + 1])
natija = []
for k in range(q):
    l = int(kirish[n + 2 + 2 * k])
    r = int(kirish[n + 3 + 2 * k])
    natija.append(p[r] - p[l - 1])          # O(1)
print("\n".join(map(str, natija)))
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader oquvchi = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(oquvchi.readLine());
        int n = Integer.parseInt(st.nextToken());
        long[] p = new long[n + 1];
        st = new StringTokenizer(oquvchi.readLine());
        for (int i = 0; i < n; i++) p[i + 1] = p[i] + Long.parseLong(st.nextToken());   // p[i] — dastlabki i ta element yigʻindisi

        int q = Integer.parseInt(oquvchi.readLine().trim());
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            st = new StringTokenizer(oquvchi.readLine());
            int l = Integer.parseInt(st.nextToken()), r = Integer.parseInt(st.nextToken());
            sb.append(p[r] - p[l - 1]).append('\n');    // O(1)
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);                    // katta kirish uchun tez oʻqish
    cin.tie(nullptr);
    int n;
    cin >> n;
    vector<long long> p(n + 1, 0);
    for (int i = 0; i < n; i++) {
        long long x;
        cin >> x;
        p[i + 1] = p[i] + x;                        // p[i] — dastlabki i ta element yigʻindisi
    }
    int q;
    cin >> q;
    while (q--) {
        int l, r;
        cin >> l >> r;
        cout << p[r] - p[l - 1] << '\n';            // O(1)
    }
    return 0;
}
```

`6`, `5 2 8 1 9 3` va soʻrovlar `1 3`, `2 5`, `4 4`, `1 6` uchun chiqish: `15`, `20`, `1`, `28`. `p[0] = 0` tufayli `l = 1` holati alohida tekshirilmaydi. Soʻrovlar koʻp boʻlganda kiritish-chiqarish ham muhim: Python'da butun kirish bir marta oʻqiladi, Java'da `BufferedReader`, C++'da `sync_with_stdio(false)` ishlatiladi.

## Ishlangan misol 2: kesmalar daraxti — oraliq minimumi

Birinchi satrda *n*, ikkinchisida *n* ta son, uchinchisida *q*, keyin *q* ta buyruq: `min l r` — [*l*, *r*] dagi eng kichik qiymatni chiqarish, `set i x` — *i*-elementni *x* ga almashtirish (indekslar 1 dan).

```python
import sys

kirish = sys.stdin.read().split()
n = int(kirish[0])
olcham = 1
while olcham < n:
    olcham *= 2                             # ikkining darajasi
INF = float("inf")
t = [INF] * (2 * olcham)
for i in range(n):
    t[olcham + i] = int(kirish[1 + i])      # barglar
for i in range(olcham - 1, 0, -1):
    t[i] = min(t[2 * i], t[2 * i + 1])      # ota = bolalar minimumi

q = int(kirish[n + 1])
oqish = n + 2
natija = []
for _ in range(q):
    buyruq, x, y = kirish[oqish], int(kirish[oqish + 1]), int(kirish[oqish + 2])
    oqish += 3
    if buyruq == "set":
        i = olcham + x - 1
        t[i] = y
        i //= 2
        while i >= 1:                       # ildizgacha qayta hisoblash
            t[i] = min(t[2 * i], t[2 * i + 1])
            i //= 2
    else:
        l, r = olcham + x - 1, olcham + y   # [l, r) yarim ochiq oraliq
        eng = INF
        while l < r:                        # chegaralardan yuqoriga koʻtarilish
            if l % 2 == 1:
                eng = min(eng, t[l])
                l += 1
            if r % 2 == 1:
                r -= 1
                eng = min(eng, t[r])
            l //= 2
            r //= 2
        natija.append(eng)
print("\n".join(map(str, natija)))
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader oquvchi = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(oquvchi.readLine().trim());
        int olcham = 1;
        while (olcham < n) olcham *= 2;                         // ikkining darajasi
        long[] t = new long[2 * olcham];
        Arrays.fill(t, Long.MAX_VALUE);
        StringTokenizer st = new StringTokenizer(oquvchi.readLine());
        for (int i = 0; i < n; i++) t[olcham + i] = Long.parseLong(st.nextToken());   // barglar
        for (int i = olcham - 1; i >= 1; i--) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // ota = bolalar minimumi

        int q = Integer.parseInt(oquvchi.readLine().trim());
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            st = new StringTokenizer(oquvchi.readLine());
            String buyruq = st.nextToken();
            int x = Integer.parseInt(st.nextToken());
            long y = Long.parseLong(st.nextToken());
            if (buyruq.equals("set")) {
                int i = olcham + x - 1;
                t[i] = y;
                for (i /= 2; i >= 1; i /= 2) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // ildizgacha qayta hisoblash
            } else {
                int l = olcham + x - 1, r = olcham + (int) y;  // [l, r) yarim ochiq oraliq
                long eng = Long.MAX_VALUE;
                while (l < r) {                                 // chegaralardan yuqoriga koʻtarilish
                    if (l % 2 == 1) eng = Math.min(eng, t[l++]);
                    if (r % 2 == 1) eng = Math.min(eng, t[--r]);
                    l /= 2;
                    r /= 2;
                }
                sb.append(eng).append('\n');
            }
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n;
    cin >> n;
    int olcham = 1;
    while (olcham < n) olcham *= 2;                             // ikkining darajasi
    vector<long long> t(2 * olcham, LLONG_MAX);
    for (int i = 0; i < n; i++) cin >> t[olcham + i];           // barglar
    for (int i = olcham - 1; i >= 1; i--) t[i] = min(t[2 * i], t[2 * i + 1]);   // ota = bolalar minimumi

    int q;
    cin >> q;
    while (q--) {
        string buyruq;
        long long x, y;
        cin >> buyruq >> x >> y;
        if (buyruq == "set") {
            int i = olcham + x - 1;
            t[i] = y;
            for (i /= 2; i >= 1; i /= 2) t[i] = min(t[2 * i], t[2 * i + 1]);   // ildizgacha qayta hisoblash
        } else {
            int l = olcham + x - 1, r = olcham + y;             // [l, r) yarim ochiq oraliq
            long long eng = LLONG_MAX;
            while (l < r) {                                     // chegaralardan yuqoriga koʻtarilish
                if (l % 2 == 1) eng = min(eng, t[l++]);
                if (r % 2 == 1) eng = min(eng, t[--r]);
                l /= 2;
                r /= 2;
            }
            cout << eng << '\n';
        }
    }
    return 0;
}
```

Kirish:

```text
6
5 2 8 1 9 3
7
min 1 3
min 3 6
set 4 10
min 3 6
min 4 4
set 2 7
min 1 2
```

Chiqish:

```text
2
1
3
10
5
```

`set 4 10` dan keyin 4-elementdagi `1` yoʻqoldi, shuning uchun [3, 6] minimumi `3` ga oʻzgardi — yangilash faqat barg va uning ota-bobolarini (log₂ 8 = 3 ta tugun) qayta hisobladi. Soʻrovda `l` va `r` barglardan yuqoriga koʻtariladi: chegaradagi „toq“ tugun toʻliq oraliq ichida boʻlsa natijaga qoʻshiladi. Daraxt hajmi 8 ga yaxlitlangani uchun ortiqcha barglar ∞ bilan toʻldirildi — ular minimumga taʼsir qilmaydi.

## Ishlangan misol 3: BQD dan oʻchirish

Birinchi satrda *n* ta kalit, ikkinchisida oʻchiriladigan kalitlar. Kalitlarni BQD ga qoʻshing, keyin berilgan kalitlarni oʻchiring (yoʻq kalit eʼtiborsiz qoldiriladi) va natijaviy daraxtni toʻgʻri aylanish (preorder) bilan chiqaring; daraxt boʻsh qolsa `-`.

```python
import sys

sys.setrecursionlimit(10000)


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(t, kalit):
    if t is None:
        return Tugun(kalit)
    if kalit < t.kalit:
        t.chap = qosh(t.chap, kalit)
    else:
        t.ong = qosh(t.ong, kalit)
    return t


def ochir(t, kalit):
    if t is None:                           # kalit yoʻq
        return None
    if kalit < t.kalit:
        t.chap = ochir(t.chap, kalit)
    elif kalit > t.kalit:
        t.ong = ochir(t.ong, kalit)
    elif t.chap is None:                    # barg yoki faqat oʻng bola
        return t.ong
    elif t.ong is None:                     # faqat chap bola
        return t.chap
    else:                                   # ikki bola: keyingi element bilan almashtirish
        keyingi = t.ong
        while keyingi.chap is not None:
            keyingi = keyingi.chap
        t.kalit = keyingi.kalit
        t.ong = ochir(t.ong, keyingi.kalit)
    return t


def togri(t, r):
    if t:
        r.append(t.kalit)
        togri(t.chap, r)
        togri(t.ong, r)


ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
for x in map(int, input().split()):
    ildiz = ochir(ildiz, x)
r = []
togri(ildiz, r)
print(*r if r else "-")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun t, int kalit) {
        if (t == null) return new Tugun(kalit);
        if (kalit < t.kalit) t.chap = qosh(t.chap, kalit);
        else t.ong = qosh(t.ong, kalit);
        return t;
    }

    static Tugun ochir(Tugun t, int kalit) {
        if (t == null) return null;                         // kalit yoʻq
        if (kalit < t.kalit) t.chap = ochir(t.chap, kalit);
        else if (kalit > t.kalit) t.ong = ochir(t.ong, kalit);
        else if (t.chap == null) return t.ong;              // barg yoki faqat oʻng bola
        else if (t.ong == null) return t.chap;              // faqat chap bola
        else {                                              // ikki bola: keyingi element bilan almashtirish
            Tugun keyingi = t.ong;
            while (keyingi.chap != null) keyingi = keyingi.chap;
            t.kalit = keyingi.kalit;
            t.ong = ochir(t.ong, keyingi.kalit);
        }
        return t;
    }

    static void togri(Tugun t, List<Integer> r) {
        if (t == null) return;
        r.add(t.kalit);
        togri(t.chap, r);
        togri(t.ong, r);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun ildiz = null;
        for (String s : in.nextLine().trim().split("\\s+")) ildiz = qosh(ildiz, Integer.parseInt(s));
        for (String s : in.nextLine().trim().split("\\s+")) ildiz = ochir(ildiz, Integer.parseInt(s));
        List<Integer> r = new ArrayList<>();
        togri(ildiz, r);
        StringBuilder sb = new StringBuilder();
        for (int x : r) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(r.isEmpty() ? "-" : sb.toString());
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* t, int kalit) {
    if (!t) return new Tugun{kalit};
    if (kalit < t->kalit) t->chap = qosh(t->chap, kalit);
    else t->ong = qosh(t->ong, kalit);
    return t;
}

Tugun* ochir(Tugun* t, int kalit) {
    if (!t) return nullptr;                                 // kalit yoʻq
    if (kalit < t->kalit) {
        t->chap = ochir(t->chap, kalit);
    } else if (kalit > t->kalit) {
        t->ong = ochir(t->ong, kalit);
    } else if (!t->chap) {                                  // barg yoki faqat oʻng bola
        Tugun* bola = t->ong;
        delete t;
        return bola;
    } else if (!t->ong) {                                   // faqat chap bola
        Tugun* bola = t->chap;
        delete t;
        return bola;
    } else {                                                // ikki bola: keyingi element bilan almashtirish
        Tugun* keyingi = t->ong;
        while (keyingi->chap) keyingi = keyingi->chap;
        t->kalit = keyingi->kalit;
        t->ong = ochir(t->ong, keyingi->kalit);
    }
    return t;
}

void togri(Tugun* t, vector<int>& r) {
    if (!t) return;
    r.push_back(t->kalit);
    togri(t->chap, r);
    togri(t->ong, r);
}

int main() {
    string satr;
    int x;
    Tugun* ildiz = nullptr;
    getline(cin, satr);
    istringstream kalitlar(satr);
    while (kalitlar >> x) ildiz = qosh(ildiz, x);
    getline(cin, satr);
    istringstream ochiriladigan(satr);
    while (ochiriladigan >> x) ildiz = ochir(ildiz, x);

    vector<int> r;
    togri(ildiz, r);
    if (r.empty()) cout << "-";
    for (size_t i = 0; i < r.size(); i++) cout << (i ? " " : "") << r[i];
    cout << endl;
    return 0;
}
```

Kirish:

```text
50 30 70 20 40 60 80
20 30 50 99
```

Chiqish:

```text
60 40 70 80
```

`20` — barg: shunchaki olib tashlandi. `30` ning bitta bolasi (`40`) qoldi — u `30` oʻrnini egalladi. `50` ning ikki bolasi bor: oʻng qism daraxtdagi eng kichik kalit `60` ildizga koʻchirildi va oʻsha `60` bargi oʻchirildi. `99` daraxtda yoʻq — eʼtiborsiz qoldi. Natijada ildiz 60, chapda 40, oʻngda 70 va uning oʻng bolasi 80 — BQD xossasi saqlangan.

:::warning
**Koʻp uchraydigan xatolar**

- **Prefiks yigʻindida `p[l]` dan ayirish.** Toʻgʻrisi `p[r] − p[l − 1]`; aks holda *l*-element ham ayiriladi.
- **Kesmalar daraxtida yangilashdan keyin otalarni qayta hisoblamaslik.** Soʻrovlar eski qiymatni qaytaradi.
- **Ortiqcha barglarni 0 bilan toʻldirish.** Minimum uchun 0 natijani buzadi; ∞ (yigʻindi uchun esa 0) — amalning neytral elementi kerak.
- **BQD dan ikki bolali tugunni oʻchirishda faqat kalitni koʻchirish.** Keyingi elementning eski tugunini ham oʻchirish shart, aks holda kalit ikki marta qoladi.
:::

:::tip
Kesmalar daraxtini qogʻozda 8 bargli daraxt qilib chizing va har bir tugun ustiga uning kesmasini (`[1..4]`, `[5..6]`, …) yozing. Soʻrov qaysi tugunlarni birlashtirishini rasmda koʻrish kodni tushunishni ancha osonlashtiradi.
:::

## Xulosa

- Prefiks yigʻindilar oʻzgarmas massivda oraliq yigʻindisini *O*(1) da beradi: `p[r] − p[l − 1]`.
- Kesmalar daraxti istalgan assotsiativ amal uchun yangilash va oraliq soʻrovini *O*(log *n*) da bajaradi.
- Fenvik daraxti yigʻindi uchun ixchamroq va kodi qisqa; minimum kabi amallar uchun kesmalar daraxti qulay.
- BQD dan oʻchirish: barg — olib tashlash, bitta bola — uni koʻtarish, ikki bola — keyingi element bilan almashtirish.

## Oʻzingizni tekshiring

:::check
Q: `a = 4 1 3 2` uchun prefiks yigʻindilar massivi qanday?
A: `p = 0 4 5 8 10`.
Q: Nima uchun prefiks yigʻindilar tez-tez yangilanadigan maʼlumot uchun yaramaydi?
A: Bitta element oʻzgarsa, undan keyingi barcha `p[i]` ni qayta hisoblash kerak — *O*(*n*).
Q: Kesmalar daraxtida yangilash nechta tugunni oʻzgartiradi?
A: Barg va uning barcha ota-bobolari — log₂ *n* + 1 ta.
Q: BQD dan ikki bolali tugun oʻchirilganda uning oʻrniga qaysi kalit qoʻyiladi?
A: Oʻng qism daraxtdagi eng kichik kalit (yoki chap qism daraxtdagi eng kattasi).
:::

:::link https://cp-algorithms.com/data_structures/segment_tree.html
CP-Algorithms: kesmalar daraxti
Qurish, soʻrovlar, yangilash va variantlar (ingliz tilida).
:::
