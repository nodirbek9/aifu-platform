---
title: Konteynerlar (kolleksiyalar)
summary: Konteyner tushunchasi; roʻyxat (list, ArrayList, vector), toʻplam (set), lugʻat (dict, Map, map), navbat va deka (deque); amallar murakkabligi, tartiblangan va xeshli konteynerlar, iteratorlar, toʻgʻri konteynerni tanlash.
---

## Nima uchun bu mavzu muhim?

Matndagi har bir soʻz necha marta uchraganini sanash, roʻyxatdagi takrorlarni olib tashlash, navbatda turgan mijozlarga xizmat koʻrsatish — bularning hammasini massiv bilan ham yozish mumkin, lekin kod uzun, sekin va xatoga boy boʻladi. Har bir tilning standart kutubxonasida bunday ishlar uchun tayyor, sinovdan oʻtgan tuzilmalar bor — **konteynerlar** (kolleksiyalar).

Toʻgʻri konteynerni tanlash dasturni oʻnlab marta tezlashtirishi mumkin: 10⁵ ta elementli roʻyxatda „bormi?“ savoli 10⁵ qadam, toʻplamda esa bir necha qadam oladi. Bu maʼruzada asosiy konteynerlarni, ularning tezligini va qachon qaysi birini tanlashni oʻrganamiz.

:::note
**Bu darsdan soʻng siz:**

- roʻyxat, toʻplam, lugʻat, navbat va deka nima ekanini va qachon ishlatilishini tushuntira olasiz;
- har bir konteynerning asosiy amallari murakkabligini ayta olasiz;
- Python, Java va C++'da bu konteynerlardan foydalana olasiz;
- tartiblangan va xeshli konteynerlarning farqini tushuntira olasiz;
- masalaga mos konteynerni tanlay olasiz.
:::

## Konteynerlar turlari

| Konteyner | Gʻoyasi | Python | Java | C++ |
|---|---|---|---|---|
| Roʻyxat | tartiblangan elementlar, indeks boʻyicha murojaat | `list` | `ArrayList` | `vector` |
| Toʻplam | takrorsiz elementlar, „bormi?“ savoli | `set` | `HashSet`, `TreeSet` | `unordered_set`, `set` |
| Lugʻat (xarita) | kalit → qiymat juftliklari | `dict` | `HashMap`, `TreeMap` | `unordered_map`, `map` |
| Navbat | birinchi kirgan birinchi chiqadi (FIFO) | `collections.deque` | `ArrayDeque` (`Queue`) | `queue` |
| Deka | ikkala uchidan qoʻshish va olish | `collections.deque` | `ArrayDeque` (`Deque`) | `deque` |
| Stek | oxirgi kirgan birinchi chiqadi (LIFO) | `list` (`append`/`pop`) | `ArrayDeque` (`push`/`pop`) | `stack` |

## Amallar murakkabligi

| Amal | Roʻyxat | Xeshli toʻplam/lugʻat | Tartiblangan toʻplam/lugʻat | Deka |
|---|---|---|---|---|
| Oxiriga qoʻshish | *O*(1), oʻrtacha | — | — | *O*(1) |
| Boshiga qoʻshish | *O*(*n*) | — | — | *O*(1) |
| Indeks boʻyicha olish | *O*(1) | — | — | *O*(1) |
| Element bormi? | *O*(*n*) | *O*(1), oʻrtacha | *O*(log *n*) | *O*(*n*) |
| Qoʻshish / oʻchirish (kalit) | *O*(*n*) | *O*(1), oʻrtacha | *O*(log *n*) | — |
| Tartibda aylanish | kiritilgan tartibda | tartib kafolatlanmaydi | **saralangan** tartibda | kiritilgan tartibda |

„Oʻrtacha“ — koʻp hollarda shunday; roʻyxatga qoʻshishda vaqti-vaqti bilan xotira kengaytiriladi va bitta qoʻshish uzoqroq davom etadi.

**Xeshli** konteyner (`HashSet`, `unordered_map`, Python `dict`/`set`) elementning **xesh** qiymati boʻyicha uning joyini darhol topadi — juda tez, lekin elementlar tartibsiz. **Tartiblangan** konteyner (`TreeMap`, C++ `map`/`set`) elementlarni muvozanatlangan daraxtda saqlaydi — biroz sekinroq, lekin aylanib chiqqanda kalitlar saralangan tartibda keladi. Python'da `dict` kalitlarni **qoʻshilgan tartibda** saqlaydi; saralangan tartib kerak boʻlsa, `sorted(d)` ishlatiladi.

## Lugʻat bilan ishlash

Lugʻat — „kalit → qiymat“ jadvali: kalit boʻyicha qiymat tez topiladi. Eng koʻp uchraydigan ish — **sanash**:

| Til | Qiymatni oshirish | Kalit bormi? |
|---|---|---|
| Python | `d[k] = d.get(k, 0) + 1` yoki `collections.Counter` | `k in d` |
| Java | `m.merge(k, 1, Integer::sum)` yoki `m.put(k, m.getOrDefault(k, 0) + 1)` | `m.containsKey(k)` |
| C++ | `m[k]++` (yoʻq kalit avtomatik 0 bilan yaratiladi) | `m.count(k)` yoki `m.find(k) != m.end()` |

C++'da `m[k]` faqat **oʻqish** uchun ishlatilsa ham, yoʻq kalitni yaratib qoʻyadi — shuning uchun tekshirishda `count` yoki `find` ishlatiladi.

## Iteratorlar

Konteyner elementlarini aylanib chiqish uchun hamma tillarda „har bir element uchun“ sikli ishlaydi: `for x in s`, `for (int x : s)`, `for (int x : s)`. Uning ortida **iterator** — konteynerdagi joriy oʻrinni koʻrsatuvchi obyekt turadi. C++'da iteratorlar ochiq ishlatiladi: `s.begin()` — birinchi element, `s.end()` — oxirgidan keyingi oʻrin; `sort(v.begin(), v.end())` va `find` kabi algoritmlar aynan iteratorlar bilan ishlaydi.

Konteyner ustida aylanayotib undan element oʻchirish xavfli: Java'da `ConcurrentModificationException`, C++'da aniqlanmagan xatti-harakat, Python'da `RuntimeError` (lugʻat uchun) yoki elementlarni oʻtkazib yuborish.

## Ishlangan misol 1: soʻzlar chastotasi (lugʻat)

Bitta satr matn kiritiladi (kichik lotin harflari va boʻsh joylar). Har bir soʻz va uning necha marta uchraganini soʻzlar alifbo tartibida chiqaring.

```python
matn = input().split()

chastota = {}
for soz in matn:
    chastota[soz] = chastota.get(soz, 0) + 1

for soz in sorted(chastota):          # dict qoʻshilgan tartibda — saralaymiz
    print(soz, chastota[soz])
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] matn = in.nextLine().trim().split("\\s+");

        Map<String, Integer> chastota = new TreeMap<>();   // kalitlar saralangan
        for (String soz : matn) chastota.merge(soz, 1, Integer::sum);

        for (Map.Entry<String, Integer> e : chastota.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    map<string, int> chastota;             // kalitlar saralangan
    string soz;
    while (cin >> soz) chastota[soz]++;    // yoʻq kalit 0 bilan yaratiladi

    for (const auto& [kalit, soni] : chastota) {
        cout << kalit << " " << soni << endl;
    }
    return 0;
}
```

Kirish:

```text
bir ikki bir uch ikki bir
```

Chiqish:

```text
bir 3
ikki 2
uch 1
```

Java'da `TreeMap`, C++'da `map` — tartiblangan lugʻatlar, shuning uchun aylanib chiqqanda kalitlar oʻzi saralangan keladi. Python'da `dict` qoʻshilgan tartibni saqlaydi, shuning uchun `sorted` qoʻshildi. C++'dagi `auto& [kalit, soni]` — juftlikni ikki oʻzgaruvchiga ajratish (C++17).

## Ishlangan misol 2: toʻplamlar — umumiy elementlar

Birinchi va ikkinchi satrda butun sonlar (takrorlanishi mumkin). Ikkala satrda ham uchraydigan sonlarni takrorsiz, oʻsish tartibida chiqaring (boʻlmasa `-`), keyingi satrda esa jami nechta **turli** son kiritilganini.

```python
a = set(map(int, input().split()))
b = set(map(int, input().split()))

umumiy = sorted(a & b)                 # kesishma
print(*umumiy if umumiy else "-")
print(len(a | b))                      # birlashma
```
```java
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    static Set<Integer> oqish(String satr) {
        Set<Integer> s = new HashSet<>();
        for (String t : satr.trim().split("\\s+")) s.add(Integer.parseInt(t));
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Set<Integer> a = oqish(in.nextLine());
        Set<Integer> b = oqish(in.nextLine());

        Set<Integer> umumiy = new TreeSet<>(a);   // saralangan nusxa
        umumiy.retainAll(b);                      // kesishma
        Set<Integer> hammasi = new HashSet<>(a);
        hammasi.addAll(b);                        // birlashma

        StringBuilder sb = new StringBuilder();
        for (int x : umumiy) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(umumiy.isEmpty() ? "-" : sb.toString());
        System.out.println(hammasi.size());
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <sstream>
#include <string>
using namespace std;

set<int> oqish() {
    string satr;
    getline(cin, satr);
    istringstream oqim(satr);
    set<int> s;
    int x;
    while (oqim >> x) s.insert(x);         // takror qoʻshilmaydi
    return s;
}

int main() {
    set<int> a = oqish();
    set<int> b = oqish();

    bool birinchi = true;
    for (int x : a) {                      // set — saralangan
        if (b.count(x)) {
            if (!birinchi) cout << ' ';
            cout << x;
            birinchi = false;
        }
    }
    if (birinchi) cout << "-";
    cout << endl;

    set<int> hammasi = a;
    hammasi.insert(b.begin(), b.end());
    cout << hammasi.size() << endl;
    return 0;
}
```

Kirish:

```text
5 1 3 3 9 7
3 8 5 5 2
```

Chiqish:

```text
3 5
7
```

Turli sonlar: 1, 2, 3, 5, 7, 8, 9 — jami 7 ta. Toʻplamga takror qoʻshilmaydi, shuning uchun „turli elementlar soni“ — shunchaki toʻplam oʻlchami. Python'da `&` va `|` — kesishma va birlashma amallari; Java'da `retainAll` va `addAll`. Massiv bilan bu masala ichma-ich sikl talab qilib, *O*(*n* · *m*) vaqt olardi, toʻplam bilan esa deyarli chiziqli.

## Ishlangan misol 3: navbat — sanoq oʻyini

*n* kishi doira boʻylab turibdi (1 dan *n* gacha raqamlangan). Sanoq 1-kishidan boshlanadi va har *k*-kishi doiradan chiqadi; sanoq keyingi kishidan davom etadi. Chiqish tartibini chiqaring (bu Iosif masalasi deb ataladi).

```python
from collections import deque

n, k = map(int, input().split())
navbat = deque(range(1, n + 1))

tartib = []
while navbat:
    navbat.rotate(-(k - 1))            # k-1 kishini oxiriga oʻtkazamiz
    tartib.append(navbat.popleft())    # k-kishi chiqadi
print(*tartib)
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        Deque<Integer> navbat = new ArrayDeque<>();
        for (int i = 1; i <= n; i++) navbat.addLast(i);

        StringBuilder sb = new StringBuilder();
        while (!navbat.isEmpty()) {
            for (int i = 0; i < k - 1; i++) navbat.addLast(navbat.pollFirst());   // boshidan oxiriga
            if (sb.length() > 0) sb.append(' ');
            sb.append(navbat.pollFirst());                                     // k-kishi chiqadi
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <queue>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    queue<int> navbat;
    for (int i = 1; i <= n; i++) navbat.push(i);

    bool birinchi = true;
    while (!navbat.empty()) {
        for (int i = 0; i < k - 1; i++) {      // boshidan oxiriga
            navbat.push(navbat.front());
            navbat.pop();
        }
        if (!birinchi) cout << ' ';
        cout << navbat.front();                // k-kishi chiqadi
        navbat.pop();
        birinchi = false;
    }
    cout << endl;
    return 0;
}
```

`7 3` kiritilsa, chiqish: `3 6 2 7 5 1 4`. Navbat doirani modellashtiradi: sanalgan, lekin chiqmagan kishi navbat boshidan olinib, oxiriga qoʻyiladi. Navbat boshidan olish deka va navbatda *O*(1), Python `list` da esa `pop(0)` — *O*(*n*), chunki qolgan elementlar suriladi. Shuning uchun navbat uchun `list` emas, `deque` ishlatiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Roʻyxatda „bormi?“ savolini koʻp marta berish.** `if x in royxat` sikl ichida *O*(*n*²) ga olib keladi; toʻplam ishlating.
- **C++'da `m[k]` bilan tekshirish.** Yoʻq kalit yaratilib qoladi va lugʻat oʻsadi; `count` yoki `find` ishlating.
- **Xeshli konteynerdan tartib kutish.** `HashMap`, `unordered_map` elementlarni tasodifiy tartibda beradi; tartib kerak boʻlsa — `TreeMap`, `map` yoki saralash.
- **Python'da navbat uchun `list.pop(0)`.** Har bir olish *O*(*n*); `deque.popleft()` ishlating.
:::

:::tip
Konteyner tanlashda masalaning **eng koʻp bajariladigan amali**ni aniqlang: indeks boʻyicha olish — roʻyxat; „bormi?“ — toʻplam; kalit boʻyicha qiymat — lugʻat; ikki uchidan ishlash — deka. Tartib kerak boʻlsa — tartiblangan variant.
:::

## Xulosa

- Konteynerlar — standart kutubxonaning tayyor maʼlumot tuzilmalari: roʻyxat, toʻplam, lugʻat, navbat, deka, stek.
- Roʻyxat indeks boʻyicha tez, lekin qidirishda sekin; toʻplam va lugʻat kalit boʻyicha tez.
- Xeshli konteynerlar oʻrtacha *O*(1), tartiblanganlari *O*(log *n*), lekin saralangan tartibda aylanadi.
- Navbat va deka boshidan olishni *O*(1) da bajaradi.
- Konteyner masaladagi eng koʻp bajariladigan amalga qarab tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: Roʻyxatda va xeshli toʻplamda „element bormi?“ savoli qancha vaqt oladi?
A: Roʻyxatda *O*(*n*), xeshli toʻplamda oʻrtacha *O*(1).
Q: `HashMap` va `TreeMap` ning asosiy farqi nimada?
A: `HashMap` tezroq (*O*(1)), lekin tartibsiz; `TreeMap` *O*(log *n*), lekin kalitlarni saralangan tartibda saqlaydi.
Q: C++'da `if (m["olma"] > 0)` qanday yashirin taʼsirga ega?
A: Agar „olma“ kaliti boʻlmasa, u 0 qiymat bilan yaratiladi va lugʻatga qoʻshiladi.
Q: Nima uchun Python'da navbat uchun `deque` ishlatiladi?
A: `deque.popleft()` *O*(1), `list.pop(0)` esa *O*(*n*) — qolgan elementlar suriladi.
Q: Satrdagi turli soʻzlar sonini qanday topish mumkin?
A: Soʻzlarni toʻplamga qoʻshib, toʻplam oʻlchamini olish: `len(set(matn.split()))`.
:::

:::link https://docs.python.org/3/library/collections.html
Python: collections moduli
`deque`, `Counter`, `defaultdict` va boshqa konteynerlar (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/container
C++: konteynerlar kutubxonasi
`vector`, `set`, `map`, `deque`, `queue` va ularning murakkabligi (ingliz tilida).
:::
