---
title: Roʻyxatlar. Graflar. Deykstra va Floyd algoritmlari
summary: Bir bogʻlamli, ikki bogʻlamli va halqasimon roʻyxatlar, ularning massivdan farqi; graf elementlari va turlari, grafni kompyuterda tasvirlash — qoʻshnilik matritsasi va qoʻshnilik roʻyxati; eng qisqa yoʻl masalasi, Deykstra algoritmi va Floyd–Uorshell algoritmi.
---

## Nima uchun bu mavzu muhim?

Massiv elementlari xotirada yonma-yon turadi — shuning uchun oʻrtasiga element qoʻshish yoki oʻchirish qimmat. **Bogʻlangan roʻyxat** elementlarni koʻrsatkichlar bilan ulaydi va bu amallarni *O*(1) da bajaradi. Bu gʻoya keyingi barcha murakkab tuzilmalarning — daraxtlar va graflarning asosi.

**Graf** — oʻzaro bogʻlangan obyektlarning eng umumiy modeli: shaharlar va yoʻllar, foydalanuvchilar va doʻstlik, kompyuterlar va tarmoq kabellari, veb-sahifalar va havolalar. Navigator eng qisqa yoʻlni, internet marshrutizatorlari paket uchun yoʻnalishni aynan shu maʼruzadagi algoritmlar bilan topadi.

:::note
**Bu darsdan soʻng siz:**

- bir bogʻlamli, ikki bogʻlamli va halqasimon roʻyxatlarni farqlay olasiz va bir bogʻlamli roʻyxatni yoza olasiz;
- roʻyxat va massivning amallar murakkabligini solishtira olasiz;
- graf elementlari va turlarini tushuntirib, grafni matritsa va roʻyxat koʻrinishida saqlay olasiz;
- Deykstra algoritmi bilan bitta uchdan eng qisqa masofalar va yoʻlni topa olasiz;
- Floyd–Uorshell algoritmi bilan barcha juftliklar orasidagi masofani topa olasiz.
:::

## Bogʻlangan roʻyxatlar

Roʻyxat **tugunlardan** iborat; har bir tugun qiymat va keyingi tugunga koʻrsatkich (havola) saqlaydi. Birinchi tugun — **bosh**, oxirgisining koʻrsatkichi — boʻsh (`None`, `null`, `nullptr`).

| Tur | Tuzilishi | Afzalligi |
|---|---|---|
| Bir bogʻlamli | har tugunda `keyingi` | oddiy, kam xotira |
| Ikki bogʻlamli | `oldingi` va `keyingi` | ikki tomonga yurish, tugunni *O*(1) da oʻchirish |
| Halqasimon | oxirgi tugun boshiga ulangan | aylanma navbat, oʻyinchilar navbati |

| Amal | Massiv | Bogʻlangan roʻyxat |
|---|---|---|
| *i*-elementga murojaat | *O*(1) | *O*(*n*) — boshidan yurish kerak |
| Boshiga qoʻshish | *O*(*n*) | *O*(1) |
| Maʼlum tugundan keyin qoʻshish/oʻchirish | *O*(*n*) | *O*(1) |
| Qiymat boʻyicha qidirish | *O*(*n*) | *O*(*n*) |

Java `LinkedList` va C++ `std::list` — ikki bogʻlamli roʻyxatlar. Amalda massivga asoslangan roʻyxatlar (`ArrayList`, `vector`) koʻproq ishlatiladi: protsessor keshi yonma-yon elementlar bilan tezroq ishlaydi.

## Graflar

**Graf** *G* = (*V*, *E*) — **uchlar** (*V*) va ularni bogʻlovchi **qirralar** (*E*) toʻplami.

| Tushuncha | Maʼnosi |
|---|---|
| Yoʻnaltirilmagan graf | qirra ikki tomonlama: doʻstlik |
| Yoʻnaltirilgan graf (orgraf) | qirra bir tomonlama — yoy: bir tomonlama koʻcha, veb-havola |
| Vaznli graf | har qirrada son — vazn: masofa, narx, vaqt |
| Uch darajasi | unga tegishli qirralar soni |
| Yoʻl | qirralar bilan ketma-ket bogʻlangan uchlar |
| Bogʻlamli graf | istalgan ikki uch orasida yoʻl bor |

Grafni kompyuterda tasvirlash:

| Usul | Xotira | „*u* va *v* qoʻshnimi?“ | Qachon |
|---|---|---|---|
| **Qoʻshnilik matritsasi** `d[u][v]` | *O*(*V*²) | *O*(1) | zich, kichik graflar (*V* ≤ ~2000) |
| **Qoʻshnilik roʻyxati** — har uchning qoʻshnilari roʻyxati | *O*(*V* + *E*) | *O*(daraja) | siyrak, katta graflar (yoʻllar tarmogʻi, ijtimoiy tarmoq) |

## Eng qisqa yoʻl: Deykstra algoritmi

**Deykstra algoritmi** (E. Deykstra, 1959) — manfiy boʻlmagan vaznli grafda bitta uchdan barcha uchlargacha eng qisqa masofalar:

```text
dist[s] = 0, qolganlari ∞; barcha uchlar „belgilanmagan“
n marta:
    belgilanmaganlar ichidan dist eng kichik v ni tanla va belgila
    v ning har bir qoʻshnisi u uchun:
        agar dist[v] + w(v, u) < dist[u]: dist[u] = dist[v] + w(v, u)   ← „boʻshatish“
```

Gʻoyasi: belgilangan uch masofasi endi oʻzgarmaydi — chunki vaznlar manfiy emas, boshqa yoʻl uzunroq boʻladi. Massiv bilan tanlash *O*(*V*²); **ustuvor navbat** (uyum) bilan *O*((*V* + *E*) log *V*) — katta siyrak graflar uchun. Manfiy vaznlarda Deykstra notoʻgʻri ishlaydi (Bellman–Ford algoritmi kerak).

## Floyd–Uorshell algoritmi

**Barcha juftliklar** orasidagi masofani topish uchun:

```text
k = 1..n uchun: i = 1..n uchun: j = 1..n uchun:
    d[i][j] = min(d[i][j], d[i][k] + d[k][j])
```

*k*-qadamdan keyin `d[i][j]` — oraliq uchlari faqat {1, …, *k*} dan boʻlgan eng qisqa yoʻl uzunligi. Uchta ichma-ich sikl — *O*(*V*³), xotira *O*(*V*²). Manfiy vaznli qirralar bilan ham ishlaydi (manfiy sikl boʻlmasa). Tashqi sikl **albatta** *k* boʻyicha boʻlishi kerak.

## Ishlangan misol 1: bir bogʻlamli roʻyxat

Birinchi satrda *q*, keyin *q* ta buyruq: `boshiga X`, `oxiriga X`, `ochir X` (birinchi uchragan *X* ni oʻchirish; yoʻq boʻlsa `yoʻq` chiqarish), `chiqar` (roʻyxatni chiqarish; boʻsh boʻlsa `boʻsh`). Roʻyxatni tugunlar va koʻrsatkichlar bilan yozing; oxiriga qoʻshish *O*(1) boʻlishi uchun oxirgi tugunni ham saqlang.

```python
class Tugun:
    def __init__(self, qiymat):
        self.qiymat = qiymat
        self.keyingi = None


bosh = None
oxir = None                                 # oxirgi tugun — oxiriga qoʻshish O(1)

q = int(input())
for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "boshiga":
        t = Tugun(int(buyruq[1]))
        t.keyingi = bosh
        bosh = t
        if oxir is None:
            oxir = t
    elif buyruq[0] == "oxiriga":
        t = Tugun(int(buyruq[1]))
        if oxir is None:
            bosh = oxir = t
        else:
            oxir.keyingi = t
            oxir = t
    elif buyruq[0] == "ochir":
        x = int(buyruq[1])
        oldingi, joriy = None, bosh
        while joriy is not None and joriy.qiymat != x:
            oldingi, joriy = joriy, joriy.keyingi
        if joriy is None:
            print("yoʻq")
            continue
        if oldingi is None:                 # bosh oʻchiriladi
            bosh = joriy.keyingi
        else:
            oldingi.keyingi = joriy.keyingi # tugunni „aylanib oʻtamiz“
        if joriy is oxir:
            oxir = oldingi
    else:
        qiymatlar = []
        joriy = bosh
        while joriy is not None:
            qiymatlar.append(joriy.qiymat)
            joriy = joriy.keyingi
        print(*qiymatlar if qiymatlar else ["boʻsh"])
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        int qiymat;
        Tugun keyingi;
        Tugun(int qiymat) { this.qiymat = qiymat; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun bosh = null, oxir = null;             // oxirgi tugun — oxiriga qoʻshish O(1)
        int q = in.nextInt();
        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("boshiga")) {
                Tugun t = new Tugun(in.nextInt());
                t.keyingi = bosh;
                bosh = t;
                if (oxir == null) oxir = t;
            } else if (buyruq.equals("oxiriga")) {
                Tugun t = new Tugun(in.nextInt());
                if (oxir == null) {
                    bosh = oxir = t;
                } else {
                    oxir.keyingi = t;
                    oxir = t;
                }
            } else if (buyruq.equals("ochir")) {
                int x = in.nextInt();
                Tugun oldingi = null, joriy = bosh;
                while (joriy != null && joriy.qiymat != x) {
                    oldingi = joriy;
                    joriy = joriy.keyingi;
                }
                if (joriy == null) {
                    System.out.println("yoʻq");
                    continue;
                }
                if (oldingi == null) bosh = joriy.keyingi;      // bosh oʻchiriladi
                else oldingi.keyingi = joriy.keyingi;           // tugunni „aylanib oʻtamiz“
                if (joriy == oxir) oxir = oldingi;
            } else {
                StringBuilder sb = new StringBuilder();
                for (Tugun t = bosh; t != null; t = t.keyingi) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(t.qiymat);
                }
                System.out.println(sb.length() > 0 ? sb.toString() : "boʻsh");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

struct Tugun {
    int qiymat;
    Tugun* keyingi = nullptr;
};

int main() {
    Tugun* bosh = nullptr;
    Tugun* oxir = nullptr;                          // oxirgi tugun — oxiriga qoʻshish O(1)
    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "boshiga") {
            Tugun* t = new Tugun{0};
            cin >> t->qiymat;
            t->keyingi = bosh;
            bosh = t;
            if (oxir == nullptr) oxir = t;
        } else if (buyruq == "oxiriga") {
            Tugun* t = new Tugun{0};
            cin >> t->qiymat;
            if (oxir == nullptr) {
                bosh = oxir = t;
            } else {
                oxir->keyingi = t;
                oxir = t;
            }
        } else if (buyruq == "ochir") {
            int x;
            cin >> x;
            Tugun* oldingi = nullptr;
            Tugun* joriy = bosh;
            while (joriy != nullptr && joriy->qiymat != x) {
                oldingi = joriy;
                joriy = joriy->keyingi;
            }
            if (joriy == nullptr) {
                cout << "yoʻq" << endl;
                continue;
            }
            if (oldingi == nullptr) bosh = joriy->keyingi;      // bosh oʻchiriladi
            else oldingi->keyingi = joriy->keyingi;             // tugunni „aylanib oʻtamiz“
            if (joriy == oxir) oxir = oldingi;
            delete joriy;
        } else {
            if (bosh == nullptr) cout << "boʻsh";
            for (Tugun* t = bosh; t != nullptr; t = t->keyingi) {
                cout << t->qiymat << (t->keyingi ? " " : "");
            }
            cout << endl;
        }
    }
    while (bosh != nullptr) {                       // qolgan tugunlarni boʻshatamiz
        Tugun* keyingi = bosh->keyingi;
        delete bosh;
        bosh = keyingi;
    }
    return 0;
}
```

Kirish:

```text
10
oxiriga 5
oxiriga 7
boshiga 3
chiqar
ochir 5
chiqar
ochir 9
ochir 3
ochir 7
chiqar
```

Chiqish:

```text
3 5 7
3 7
yoʻq
boʻsh
```

Oʻchirishda tugunning oʻzi joyidan qimirlamaydi — oldingi tugunning koʻrsatkichi uni „aylanib oʻtib“, keyingisiga ulanadi. Uchta alohida holat bor: boshni oʻchirish (`oldingi` yoʻq), oxirgi tugunni oʻchirish (`oxir` yangilanadi) va roʻyxat boʻsh qolishi. C++'da oʻchirilgan tugun `delete` bilan boʻshatiladi, dastur oxirida esa qolgan barcha tugunlar.

## Ishlangan misol 2: Deykstra algoritmi

Birinchi satrda *n* va *m* (uchlar va qirralar), keyingi *m* satrda yoʻnaltirilmagan qirralar `u v w`. 1-uchdan barcha uchlargacha eng qisqa masofalarni (yetib boʻlmasa `-`) va 1-uchdan *n*-uchgacha eng qisqa yoʻlni chiqaring. Qoʻshnilik matritsasi va *O*(*n*²) tanlashdan foydalaning.

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[INF] * (n + 1) for _ in range(n + 1)]     # qoʻshnilik matritsasi
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u][v] = d[v][u] = min(d[u][v], w)

dist = [INF] * (n + 1)
dist[1] = 0
belgilangan = [False] * (n + 1)
ota = [0] * (n + 1)                             # yoʻlni tiklash uchun
for _ in range(n):
    v = -1
    for i in range(1, n + 1):                   # eng yaqin belgilanmagan uch
        if not belgilangan[i] and (v == -1 or dist[i] < dist[v]):
            v = i
    if dist[v] == INF:
        break
    belgilangan[v] = True
    for u in range(1, n + 1):                   # boʻshatish
        if dist[v] + d[v][u] < dist[u]:
            dist[u] = dist[v] + d[v][u]
            ota[u] = v

print(*[x if x < INF else "-" for x in dist[1:]])
if dist[n] < INF:
    yol = []
    x = n
    while x != 0:
        yol.append(x)
        x = ota[x]
    print(*reversed(yol))
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n + 1][n + 1];                // qoʻshnilik matritsasi
        for (long[] satr : d) java.util.Arrays.fill(satr, INF);
        for (int k = 0; k < m; k++) {
            int u = in.nextInt(), v = in.nextInt();
            long w = in.nextLong();
            d[u][v] = d[v][u] = Math.min(d[u][v], w);
        }

        long[] dist = new long[n + 1];
        java.util.Arrays.fill(dist, INF);
        dist[1] = 0;
        boolean[] belgilangan = new boolean[n + 1];
        int[] ota = new int[n + 1];                         // yoʻlni tiklash uchun
        for (int k = 0; k < n; k++) {
            int v = -1;
            for (int i = 1; i <= n; i++) {                  // eng yaqin belgilanmagan uch
                if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
            }
            if (dist[v] == INF) break;
            belgilangan[v] = true;
            for (int u = 1; u <= n; u++) {                  // boʻshatish
                if (d[v][u] < INF && dist[v] + d[v][u] < dist[u]) {
                    dist[u] = dist[v] + d[v][u];
                    ota[u] = v;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(' ');
            sb.append(dist[i] < INF ? String.valueOf(dist[i]) : "-");
        }
        System.out.println(sb);
        if (dist[n] < INF) {
            List<Integer> yol = new ArrayList<>();
            for (int x = n; x != 0; x = ota[x]) yol.add(x);
            Collections.reverse(yol);
            StringBuilder y = new StringBuilder();
            for (int x : yol) {
                if (y.length() > 0) y.append(' ');
                y.append(x);
            }
            System.out.println(y);
        }
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n + 1, vector<long long>(n + 1, INF));   // qoʻshnilik matritsasi
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u][v] = d[v][u] = min(d[u][v], w);
    }

    vector<long long> dist(n + 1, INF);
    dist[1] = 0;
    vector<bool> belgilangan(n + 1, false);
    vector<int> ota(n + 1, 0);                              // yoʻlni tiklash uchun
    for (int k = 0; k < n; k++) {
        int v = -1;
        for (int i = 1; i <= n; i++) {                      // eng yaqin belgilanmagan uch
            if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
        }
        if (dist[v] == INF) break;
        belgilangan[v] = true;
        for (int u = 1; u <= n; u++) {                      // boʻshatish
            if (d[v][u] < INF && dist[v] + d[v][u] < dist[u]) {
                dist[u] = dist[v] + d[v][u];
                ota[u] = v;
            }
        }
    }

    for (int i = 1; i <= n; i++) {
        cout << (i > 1 ? " " : "");
        if (dist[i] < INF) cout << dist[i];
        else cout << "-";
    }
    cout << endl;
    if (dist[n] < INF) {
        vector<int> yol;
        for (int x = n; x != 0; x = ota[x]) yol.push_back(x);
        reverse(yol.begin(), yol.end());
        for (size_t i = 0; i < yol.size(); i++) cout << (i ? " " : "") << yol[i];
        cout << endl;
    }
    return 0;
}
```

Kirish:

```text
5 6
1 2 4
1 3 1
3 2 2
2 4 1
3 4 5
4 5 3
```

Chiqish:

```text
0 3 1 4 7
1 3 2 4 5
```

1 → 2 toʻgʻridan-toʻgʻri qirra 4 ga teng, lekin 1 → 3 → 2 yoʻli 1 + 2 = 3 — qisqaroq: 3-uch belgilanganda 2-uchning masofasi „boʻshatildi“. `ota` massivi har bir uchga qaysi uchdan kelinganini saqlaydi; yoʻl oxiridan boshiga tiklanib, teskari chiqariladi. Pythonʼda `float("inf")` qoʻshish xavfsiz, Java va C++'da esa ∞ sifatida `MAX / 4` olindi va qirra mavjudligi alohida tekshirildi — aks holda `INF + w` toʻlib ketadi.

## Ishlangan misol 3: Floyd–Uorshell algoritmi

Birinchi satrda *n* va *m*, keyingi *m* satrda **yoʻnaltirilgan** qirralar `u v w`. Barcha juftliklar orasidagi eng qisqa masofalar matritsasini chiqaring (yetib boʻlmasa `-`).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                              # oraliq uch — tashqi sikl!
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

for satr in d:
    print(*[x if x < INF else "-" for x in satr])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) d[i][j] = i == j ? 0 : INF;
        for (int k = 0; k < m; k++) {
            int u = in.nextInt() - 1, v = in.nextInt() - 1;
            d[u][v] = Math.min(d[u][v], in.nextLong());
        }

        for (int k = 0; k < n; k++)                     // oraliq uch — tashqi sikl!
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

        StringBuilder sb = new StringBuilder();
        for (long[] satr : d) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(satr[j] < INF ? String.valueOf(satr[j]) : "-");
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n, vector<long long>(n, INF));
    for (int i = 0; i < n; i++) d[i][i] = 0;
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u - 1][v - 1] = min(d[u - 1][v - 1], w);
    }

    for (int k = 0; k < n; k++)                         // oraliq uch — tashqi sikl!
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            cout << (j ? " " : "");
            if (d[i][j] < INF) cout << d[i][j];
            else cout << "-";
        }
        cout << endl;
    }
    return 0;
}
```

Kirish:

```text
5 5
1 2 3
2 3 2
1 3 7
3 4 1
4 1 6
```

Chiqish:

```text
0 3 5 6 -
9 0 2 3 -
7 10 0 1 -
6 9 11 0 -
- - - - 0
```

1 → 3 toʻgʻridan-toʻgʻri qirra 7, lekin 2-uch orqali 3 + 2 = 5 — Floyd buni *k* = 2 qadamda topdi. 5-uchga hech qanday qirra yoʻq — u boshqalardan ajralgan va satri hamda ustuni `-` bilan toʻldi. Graf yoʻnaltirilgan, shuning uchun matritsa simmetrik emas: 1 → 2 masofasi 3, 2 → 1 esa 9 (2 → 3 → 4 → 1). *n* = 400 da Floyd 6,4 · 10⁷ amal — bir soniyadan kam; *n* = 10⁴ da esa 10¹² — bunday graflar uchun har bir uchdan Deykstra yaxshiroq.

:::warning
**Koʻp uchraydigan xatolar**

- **Roʻyxatda boshni yoki oxirni oʻchirishni unutish.** `bosh` va `oxir` koʻrsatkichlari eskirgan tugunga qarab qoladi.
- **Deykstrani manfiy vaznli grafda ishlatish.** Belgilangan uch masofasi keyinchalik kamayishi mumkin — natija notoʻgʻri.
- **Floydʼda sikllar tartibini almashtirish.** *k* tashqi sikl boʻlmasa, algoritm notoʻgʻri ishlaydi.
- **∞ ga vazn qoʻshib toʻldirish.** Java va C++'da `Long.MAX_VALUE + w` manfiy son beradi; ∞ ni kichikroq oling yoki qirra borligini tekshiring.
:::

:::tip
Graf algoritmlarini tekshirishda grafni qogʻozda chizing: uchlarni doiralar, qirralarni vaznli chiziqlar bilan. Deykstra har bir qadamda qaysi uchni belgilashini va masofalar qanday oʻzgarishini jadvalga yozing.
:::

## Xulosa

- Bogʻlangan roʻyxat koʻrsatkichlar bilan ulanadi: qoʻshish va oʻchirish *O*(1), lekin indeks boʻyicha murojaat *O*(*n*).
- Graf — uchlar va qirralar; yoʻnaltirilgan/yoʻnaltirilmagan, vaznli boʻladi; matritsa (zich) yoki qoʻshnilik roʻyxati (siyrak) bilan saqlanadi.
- Deykstra: bitta uchdan, manfiy boʻlmagan vaznlar; *O*(*V*²) yoki uyum bilan *O*((*V* + *E*) log *V*).
- Floyd–Uorshell: barcha juftliklar, *O*(*V*³); tashqi sikl — oraliq uch *k*.
- Yoʻl `ota` massivi orqali oxiridan boshiga tiklanadi.

## Oʻzingizni tekshiring

:::check
Q: Bir bogʻlamli roʻyxatda *i*-elementga murojaat nima uchun *O*(*n*)?
A: Elementlar xotirada yonma-yon emas; unga yetish uchun boshidan koʻrsatkichlar boʻyicha yurish kerak.
Q: 10⁶ uchli va 3 · 10⁶ qirrali yoʻllar grafini qanday saqlash kerak?
A: Qoʻshnilik roʻyxati bilan: matritsa 10¹² katak talab qilardi.
Q: Deykstra algoritmi nima uchun manfiy vaznlarda ishlamaydi?
A: Belgilangan uchning masofasi yakuniy deb hisoblanadi, manfiy qirra esa uni keyinchalik kamaytirishi mumkin.
Q: Floyd–Uorshell algoritmining murakkabligi qanday?
A: *O*(*V*³) vaqt va *O*(*V*²) xotira.
:::

:::link https://visualgo.net/en/sssp
VisuAlgo: eng qisqa yoʻllar
Deykstra va boshqa algoritmlarning animatsiyasi (ingliz tilida).
:::

:::link https://cp-algorithms.com/graph/all-pair-shortest-path-floyd-warshall.html
CP-Algorithms: Floyd–Uorshell
Algoritm, isbot va manfiy sikllarni aniqlash (ingliz tilida).
:::
