---
title: Graflarda optimallashtirish masalalari. Deykstra va Floyd algoritmlari (amaliy mashgʻulot)
summary: Vaznsiz grafda eng qisqa yoʻl — kenglik boʻyicha qidiruv (BFS) va labirint; Floyd–Uorshell bilan graf markazini topish (shifoxona joylashuvi masalasi); manfiy vaznli qirralar va manfiy siklni aniqlash; masalaga mos qisqa yoʻl algoritmini tanlash.
---

## Nima uchun bu mavzu muhim?

Maʼruzada Deykstra va Floyd algoritmlarini koʻrdik. Amaliyotda esa savol: qaysi holatda qaysi algoritm? Agar barcha qirralar bir xil „narx“da boʻlsa (labirintdagi qadamlar, ijtimoiy tarmoqdagi „qoʻl berishlar“), Deykstra ortiqcha — oddiy **kenglik boʻyicha qidiruv** yetarli va tezroq. Agar savol barcha juftliklar haqida boʻlsa (masalan, „shifoxonani qaysi tumanga qurish kerak?“), Floyd qulayroq. Manfiy vaznlar paydo boʻlsa — ehtiyot boʻlish kerak: manfiy sikl „eng qisqa yoʻl“ tushunchasini buzadi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- vaznsiz grafda (labirintda) eng qisqa yoʻlni BFS bilan topa olasiz;
- Floyd–Uorshell natijasidan foydalanib graf markazini topa olasiz;
- Floyd yordamida manfiy siklni aniqlay olasiz;
- masala shartiga qarab BFS, Deykstra yoki Floyd ni tanlay olasiz.
:::

## Kenglik boʻyicha qidiruv (BFS)

**BFS** (breadth-first search) boshlangʻich uchdan boshlab grafni **sathma-sath** aylanadi: avval 1 qadamdagi barcha uchlar, keyin 2 qadamdagilar va hokazo. Navbat ishlatiladi. Vaznsiz grafda uchga birinchi marta yetilgan qadamlar soni — eng qisqa masofa.

```text
dist[s] = 0; navbat = [s]
toki navbat boʻsh emas:
    v = navbat boshidan
    v ning har bir qoʻshnisi u uchun, agar u koʻrilmagan boʻlsa:
        dist[u] = dist[v] + 1; u ni navbat oxiriga qoʻy
```

Murakkablik *O*(*V* + *E*) — Deykstradan tez. Labirint — panjara shaklidagi graf: har bir katak uch, qoʻshni boʻsh kataklar qirra bilan bogʻlangan.

## Graf markazi

Uchning **eksentrisiteti** — undan eng uzoq uchgacha boʻlgan masofa. **Graf markazi** — eksentrisiteti eng kichik uch. Amaliy maʼnosi: shifoxona, oʻt oʻchirish deposi yoki ombor uchun eng uzoq manzilgacha borish vaqtini minimallashtiradigan joy. Barcha juftliklar masofasi kerak boʻlgani uchun Floyd–Uorshell qulay.

## Manfiy vaznlar va manfiy sikl

Manfiy vazn — masalan, yoʻlda „bonus“ yoki valyuta ayirboshlashda foyda. Deykstra manfiy vaznlarda ishlamaydi, Floyd esa ishlaydi — agar grafda **manfiy sikl** (vaznlari yigʻindisi manfiy boʻlgan sikl) boʻlmasa. Manfiy sikl boʻlsa, uni qayta-qayta aylanib yoʻlni cheksiz qisqartirish mumkin — „eng qisqa yoʻl“ mavjud emas.

Floyd bilan aniqlash oson: algoritm tugagach, biror *i* uchun `d[i][i] < 0` boʻlsa, *i* manfiy siklda yotadi.

| Masala | Algoritm | Murakkablik |
|---|---|---|
| Vaznsiz graf, bitta manba | BFS | *O*(*V* + *E*) |
| Manfiy boʻlmagan vaznlar, bitta manba | Deykstra (uyum bilan) | *O*((*V* + *E*) log *V*) |
| Manfiy vaznlar, bitta manba | Bellman–Ford | *O*(*V* · *E*) |
| Barcha juftliklar, kichik *V* | Floyd–Uorshell | *O*(*V*³) |

## Ishlangan misol 1: labirintdan chiqish (BFS)

Birinchi satrda *R* va *C*, keyingi *R* satrda labirint: `.` — yoʻlak, `#` — devor, `S` — boshlanish, `E` — chiqish. Yuqoriga, pastga, chapga va oʻngga yurish mumkin. Chiqishgacha eng kam qadamlar sonini chiqaring (yetib boʻlmasa `-1`).

```python
from collections import deque

r, c = map(int, input().split())
labirint = [input() for _ in range(r)]
for i in range(r):
    for j in range(c):
        if labirint[i][j] == "S":
            boshlanish = (i, j)

masofa = [[-1] * c for _ in range(r)]       # -1 — hali koʻrilmagan
masofa[boshlanish[0]][boshlanish[1]] = 0
navbat = deque([boshlanish])
javob = -1
while navbat:
    x, y = navbat.popleft()
    if labirint[x][y] == "E":
        javob = masofa[x][y]                # birinchi yetish — eng qisqa
        break
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        a, b = x + dx, y + dy
        if 0 <= a < r and 0 <= b < c and labirint[a][b] != "#" and masofa[a][b] == -1:
            masofa[a][b] = masofa[x][y] + 1
            navbat.append((a, b))
print(javob)
```
```java
import java.util.ArrayDeque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = in.nextInt(), c = in.nextInt();
        char[][] labirint = new char[r][];
        int sx = 0, sy = 0;
        for (int i = 0; i < r; i++) {
            labirint[i] = in.next().toCharArray();
            for (int j = 0; j < c; j++) if (labirint[i][j] == 'S') { sx = i; sy = j; }
        }

        int[][] masofa = new int[r][c];
        for (int[] satr : masofa) java.util.Arrays.fill(satr, -1);   // -1 — hali koʻrilmagan
        masofa[sx][sy] = 0;
        ArrayDeque<int[]> navbat = new ArrayDeque<>();
        navbat.add(new int[] {sx, sy});
        int[][] yonalish = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int javob = -1;
        while (!navbat.isEmpty()) {
            int[] v = navbat.poll();
            if (labirint[v[0]][v[1]] == 'E') {
                javob = masofa[v[0]][v[1]];                 // birinchi yetish — eng qisqa
                break;
            }
            for (int[] d : yonalish) {
                int a = v[0] + d[0], b = v[1] + d[1];
                if (a >= 0 && a < r && b >= 0 && b < c && labirint[a][b] != '#' && masofa[a][b] == -1) {
                    masofa[a][b] = masofa[v[0]][v[1]] + 1;
                    navbat.add(new int[] {a, b});
                }
            }
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int r, c;
    cin >> r >> c;
    vector<string> labirint(r);
    int sx = 0, sy = 0;
    for (int i = 0; i < r; i++) {
        cin >> labirint[i];
        for (int j = 0; j < c; j++) if (labirint[i][j] == 'S') { sx = i; sy = j; }
    }

    vector<vector<int>> masofa(r, vector<int>(c, -1));      // -1 — hali koʻrilmagan
    masofa[sx][sy] = 0;
    queue<pair<int, int>> navbat;
    navbat.push({sx, sy});
    int dx[] = {1, -1, 0, 0}, dy[] = {0, 0, 1, -1};
    int javob = -1;
    while (!navbat.empty()) {
        auto [x, y] = navbat.front();
        navbat.pop();
        if (labirint[x][y] == 'E') {
            javob = masofa[x][y];                           // birinchi yetish — eng qisqa
            break;
        }
        for (int k = 0; k < 4; k++) {
            int a = x + dx[k], b = y + dy[k];
            if (a >= 0 && a < r && b >= 0 && b < c && labirint[a][b] != '#' && masofa[a][b] == -1) {
                masofa[a][b] = masofa[x][y] + 1;
                navbat.push({a, b});
            }
        }
    }
    cout << javob << endl;
    return 0;
}
```

Kirish:

```text
5 8
S..#....
.#.#.##.
.#...#..
.####.#.
......#E
```

Chiqish:

```text
15
```

Navbat tufayli kataklar `S` dan uzoqlashish tartibida koʻriladi, shuning uchun `E` ga birinchi yetganimizdagi masofa — eng qisqa. Har bir katak bir marta navbatga kiradi: *R* · *C* katakli labirint *O*(*R* · *C*) da yechiladi. `S#E` kabi devor bilan ajratilgan labirintda navbat `E` ga yetmasdan boʻshab qoladi va javob `-1`.

## Ishlangan misol 2: graf markazi — shifoxona qayerda?

Birinchi satrda tumanlar soni *n* va yoʻllar soni *m*, keyingi *m* satrda ikki tomonlama yoʻllar `u v w` (*w* — daqiqalarda). Graf bogʻlamli. Eng uzoq tumangacha boʻlgan vaqt eng kichik boʻladigan tumanni (bir nechta boʻlsa — raqami kichigini) va shu vaqtni chiqaring.

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = d[v - 1][u - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                          # Floyd–Uorshell
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

eksentrisitet = [max(satr) for satr in d]   # eng uzoq tumangacha
markaz = min(range(n), key=lambda i: (eksentrisitet[i], i))
print(markaz + 1, eksentrisitet[markaz])
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
            long w = in.nextLong();
            d[u][v] = d[v][u] = Math.min(d[u][v], w);
        }

        for (int k = 0; k < n; k++)                     // Floyd–Uorshell
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

        int markaz = 0;
        long eng = INF;
        for (int i = 0; i < n; i++) {
            long eksentrisitet = 0;                     // eng uzoq tumangacha
            for (int j = 0; j < n; j++) eksentrisitet = Math.max(eksentrisitet, d[i][j]);
            if (eksentrisitet < eng) {
                eng = eksentrisitet;
                markaz = i;
            }
        }
        System.out.println((markaz + 1) + " " + eng);
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
        u--;
        v--;
        d[u][v] = d[v][u] = min(d[u][v], w);
    }

    for (int k = 0; k < n; k++)                         // Floyd–Uorshell
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

    int markaz = 0;
    long long eng = INF;
    for (int i = 0; i < n; i++) {
        long long eksentrisitet = *max_element(d[i].begin(), d[i].end());   // eng uzoq tumangacha
        if (eksentrisitet < eng) {
            eng = eksentrisitet;
            markaz = i;
        }
    }
    cout << markaz + 1 << " " << eng << endl;
    return 0;
}
```

Kirish:

```text
5 6
1 2 4
2 3 3
3 4 2
4 5 6
1 5 10
2 4 7
```

Chiqish:

```text
3 8
```

3-tumandan eng uzoq tuman — 5 (3 → 4 → 5: 2 + 6 = 8 daqiqa); boshqa har qanday tumandan eng uzoq manzil kamida shuncha yoki uzoqroq. Floyd bir marta ishlab, barcha *n*² masofani beradi — undan keyin har bir nomzod tuman uchun eksentrisitetni hisoblash oddiy maksimum. Bitta manbali Deykstrani *n* marta ishga tushirish ham mumkin edi, lekin kichik *n* da Floyd kodi ancha qisqa.

## Ishlangan misol 3: manfiy siklni aniqlash

Birinchi satrda *n* va *m*, keyingi *m* satrda **yoʻnaltirilgan** qirralar `u v w` (*w* manfiy boʻlishi mumkin). Grafda manfiy sikl boʻlsa `bor`, aks holda 1-uchdan barcha uchlargacha eng qisqa masofalarni chiqaring (yetib boʻlmasa `-`).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = min(d[u - 1][v - 1], w)

for k in range(n):
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

if any(d[i][i] < 0 for i in range(n)):      # oʻziga qaytish manfiy — manfiy sikl
    print("bor")
else:
    print(*[x if x < INF else "-" for x in d[0]])
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

        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j])
                        d[i][j] = d[i][k] + d[k][j];

        boolean manfiySikl = false;
        for (int i = 0; i < n; i++) if (d[i][i] < 0) manfiySikl = true;   // oʻziga qaytish manfiy — manfiy sikl
        if (manfiySikl) {
            System.out.println("bor");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < n; j++) {
            if (j > 0) sb.append(' ');
            sb.append(d[0][j] < INF ? String.valueOf(d[0][j]) : "-");
        }
        System.out.println(sb);
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

    for (int k = 0; k < n; k++)
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j])
                    d[i][j] = d[i][k] + d[k][j];

    for (int i = 0; i < n; i++) {
        if (d[i][i] < 0) {                              // oʻziga qaytish manfiy — manfiy sikl
            cout << "bor" << endl;
            return 0;
        }
    }
    for (int j = 0; j < n; j++) {
        cout << (j ? " " : "");
        if (d[0][j] < INF) cout << d[0][j];
        else cout << "-";
    }
    cout << endl;
    return 0;
}
```

`3 3` va qirralar `1 2 4`, `2 3 -2`, `3 1 -3` uchun chiqish `bor`: 1 → 2 → 3 → 1 sikli 4 − 2 − 3 = −1, uni har aylanganda yoʻl qisqaradi. Oxirgi qirra `3 1 1` boʻlsa, sikl yigʻindisi +3 va javob `0 4 2`: 1 → 2 (4), 1 → 2 → 3 (4 − 2 = 2). Java va C++'da manfiy vaznlar bilan ∞ ni tekshirish shart: aks holda `INF + (−3)` „∞ dan kichik“ boʻlib, mavjud boʻlmagan yoʻl paydo boʻladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Vaznsiz grafda Deykstra ishlatish.** Ishlaydi, lekin BFS soddaroq va tezroq.
- **BFS da uchni navbatdan olinganda belgilash.** Bir uch navbatga bir necha marta tushib qoladi; uni **qoʻshayotganda** belgilang.
- **Manfiy vaznlarda ∞ bilan arifmetika.** `INF + w` manfiy *w* da ∞ dan kichik boʻladi; avval ikkala qism ham chekli ekanini tekshiring.
- **Manfiy sikl bilan „eng qisqa yoʻl“ chiqarish.** Bunday grafda javob mavjud emas — avval `d[i][i] < 0` ni tekshiring.
:::

:::tip
Algoritm tanlashdan oldin grafni uch savol bilan tasniflang: qirralar vaznlimi? manfiy vazn bormi? savol bitta manba haqidami yoki barcha juftliklar haqidami? Javoblar yuqoridagi jadvaldan kerakli algoritmni koʻrsatadi.
:::

## Xulosa

- Vaznsiz grafda eng qisqa yoʻl BFS bilan *O*(*V* + *E*) da topiladi; labirint — panjara shaklidagi graf.
- Graf markazi — eksentrisiteti eng kichik uch; barcha masofalar Floyd bilan olinadi.
- Floyd manfiy vaznlarda ishlaydi; `d[i][i] < 0` manfiy sikl borligini bildiradi.
- Algoritm vaznlar va savol turiga qarab tanlanadi: BFS, Deykstra, Bellman–Ford yoki Floyd.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun vaznsiz grafda BFS eng qisqa yoʻlni beradi?
A: Uchlar boshlanishdan uzoqlashish tartibida koʻriladi; uchga birinchi yetish — eng kam qadam bilan.
Q: Uchning eksentrisiteti nima?
A: Undan eng uzoq uchgacha boʻlgan eng qisqa masofa.
Q: Floyd tugagach `d[2][2] = −5` boʻlsa, bu nimani bildiradi?
A: 2-uch manfiy siklda yotadi.
Q: 10⁶ katakli labirint uchun qaysi algoritm mos?
A: BFS — *O*(*R* · *C*), har bir katak bir marta koʻriladi.
:::

:::link https://cp-algorithms.com/graph/breadth-first-search.html
CP-Algorithms: kenglik boʻyicha qidiruv
BFS, uning qoʻllanishlari va murakkabligi (ingliz tilida).
:::
