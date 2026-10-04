---
title: Задачи оптимизации на графах. Алгоритмы Дейкстры и Флойда (практическое занятие)
summary: Кратчайший путь в невзвешенном графе — поиск в ширину (BFS) и лабиринт; поиск центра графа алгоритмом Флойда–Уоршелла (задача о размещении больницы); отрицательные веса рёбер и обнаружение отрицательного цикла; выбор подходящего алгоритма кратчайших путей.
---

## Почему это важно

На лекции мы рассмотрели алгоритмы Дейкстры и Флойда. На практике вопрос: какой алгоритм когда? Если все рёбра одинаковой «цены» (шаги в лабиринте, «рукопожатия» в соцсети), Дейкстра избыточен — достаточно и быстрее обычного **поиска в ширину**. Если вопрос касается всех пар («в каком районе строить больницу?»), удобнее Флойд. А при появлении отрицательных весов нужна осторожность: отрицательный цикл разрушает само понятие «кратчайшего пути».

:::note
**После этого занятия вы сможете:**

- находить кратчайший путь в невзвешенном графе (лабиринте) поиском в ширину;
- находить центр графа по результату Флойда–Уоршелла;
- обнаруживать отрицательный цикл алгоритмом Флойда;
- выбирать BFS, Дейкстру или Флойда по условию задачи.
:::

## Поиск в ширину (BFS)

**BFS** (breadth-first search) обходит граф от начальной вершины **по уровням**: сначала все вершины на расстоянии 1 шага, потом 2 шагов и так далее. Используется очередь. В невзвешенном графе число шагов, за которое вершина достигнута впервые, — кратчайшее расстояние.

```text
dist[s] = 0; очередь = [s]
пока очередь не пуста:
    v = из начала очереди
    для каждого непосещённого соседа u вершины v:
        dist[u] = dist[v] + 1; поставить u в конец очереди
```

Сложность *O*(*V* + *E*) — быстрее Дейкстры. Лабиринт — граф в виде сетки: каждая клетка — вершина, соседние свободные клетки соединены рёбрами.

## Центр графа

**Эксцентриситет** вершины — расстояние от неё до самой дальней вершины. **Центр графа** — вершина с наименьшим эксцентриситетом. На практике это место для больницы, пожарной части или склада, минимизирующее время до самого дальнего адреса. Поскольку нужны расстояния между всеми парами, удобен Флойд–Уоршелл.

## Отрицательные веса и отрицательный цикл

Отрицательный вес — например, «бонус» в пути или выгода при обмене валют. Дейкстра с отрицательными весами не работает, а Флойд работает — если в графе нет **отрицательного цикла** (цикла с отрицательной суммой весов). Если он есть, проходя его снова и снова, путь можно сокращать бесконечно — «кратчайшего пути» не существует.

Обнаружить его Флойдом легко: если после окончания алгоритма для некоторого *i* `d[i][i] < 0`, то *i* лежит на отрицательном цикле.

| Задача | Алгоритм | Сложность |
|---|---|---|
| Невзвешенный граф, один источник | BFS | *O*(*V* + *E*) |
| Неотрицательные веса, один источник | Дейкстра (с кучей) | *O*((*V* + *E*) log *V*) |
| Отрицательные веса, один источник | Беллман–Форд | *O*(*V* · *E*) |
| Все пары, малое *V* | Флойд–Уоршелл | *O*(*V*³) |

## Разобранный пример 1: выход из лабиринта (BFS)

В первой строке *R* и *C*, в следующих *R* строках — лабиринт: `.` — проход, `#` — стена, `S` — старт, `E` — выход. Ходить можно вверх, вниз, влево и вправо. Выведите наименьшее число шагов до выхода (`-1`, если недостижим).

```python
from collections import deque

r, c = map(int, input().split())
labirint = [input() for _ in range(r)]
for i in range(r):
    for j in range(c):
        if labirint[i][j] == "S":
            boshlanish = (i, j)

masofa = [[-1] * c for _ in range(r)]       # -1 — ещё не посещена
masofa[boshlanish[0]][boshlanish[1]] = 0
navbat = deque([boshlanish])
javob = -1
while navbat:
    x, y = navbat.popleft()
    if labirint[x][y] == "E":
        javob = masofa[x][y]                # первое достижение — кратчайшее
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
        for (int[] satr : masofa) java.util.Arrays.fill(satr, -1);   // -1 — ещё не посещена
        masofa[sx][sy] = 0;
        ArrayDeque<int[]> navbat = new ArrayDeque<>();
        navbat.add(new int[] {sx, sy});
        int[][] yonalish = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int javob = -1;
        while (!navbat.isEmpty()) {
            int[] v = navbat.poll();
            if (labirint[v[0]][v[1]] == 'E') {
                javob = masofa[v[0]][v[1]];                 // первое достижение — кратчайшее
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

    vector<vector<int>> masofa(r, vector<int>(c, -1));      // -1 — ещё не посещена
    masofa[sx][sy] = 0;
    queue<pair<int, int>> navbat;
    navbat.push({sx, sy});
    int dx[] = {1, -1, 0, 0}, dy[] = {0, 0, 1, -1};
    int javob = -1;
    while (!navbat.empty()) {
        auto [x, y] = navbat.front();
        navbat.pop();
        if (labirint[x][y] == 'E') {
            javob = masofa[x][y];                           // первое достижение — кратчайшее
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

Ввод:

```text
5 8
S..#....
.#.#.##.
.#...#..
.####.#.
......#E
```

Вывод:

```text
15
```

Благодаря очереди клетки посещаются в порядке удаления от `S`, поэтому расстояние, на котором мы впервые достигли `E`, — кратчайшее (*labirint* — лабиринт, *masofa* — расстояние, *navbat* — очередь). Каждая клетка попадает в очередь один раз: лабиринт из *R* · *C* клеток решается за *O*(*R* · *C*). В лабиринте вроде `S#E`, разделённом стеной, очередь опустеет, не дойдя до `E`, и ответ будет `-1`.

## Разобранный пример 2: центр графа — где поставить больницу?

В первой строке число районов *n* и дорог *m*, в следующих *m* строках — двусторонние дороги `u v w` (*w* — в минутах). Граф связный. Выведите район, для которого время до самого дальнего района наименьшее (при нескольких — с меньшим номером), и это время.

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = d[v - 1][u - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                          # Флойд–Уоршелл
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

eksentrisitet = [max(satr) for satr in d]   # до самого дальнего района
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

        for (int k = 0; k < n; k++)                     // Флойд–Уоршелл
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

        int markaz = 0;
        long eng = INF;
        for (int i = 0; i < n; i++) {
            long eksentrisitet = 0;                     // до самого дальнего района
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

    for (int k = 0; k < n; k++)                         // Флойд–Уоршелл
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

    int markaz = 0;
    long long eng = INF;
    for (int i = 0; i < n; i++) {
        long long eksentrisitet = *max_element(d[i].begin(), d[i].end());   // до самого дальнего района
        if (eksentrisitet < eng) {
            eng = eksentrisitet;
            markaz = i;
        }
    }
    cout << markaz + 1 << " " << eng << endl;
    return 0;
}
```

Ввод:

```text
5 6
1 2 4
2 3 3
3 4 2
4 5 6
1 5 10
2 4 7
```

Вывод:

```text
3 8
```

Из района 3 самый дальний — район 5 (3 → 4 → 5: 2 + 6 = 8 минут); из любого другого района самый дальний адрес не ближе (*markaz* — центр, *eksentrisitet* — эксцентриситет). Флойд работает один раз и даёт все *n*² расстояний — после этого эксцентриситет каждого района-кандидата — простой максимум. Можно было бы запустить Дейкстру из одного источника *n* раз, но при малых *n* код Флойда гораздо короче.

## Разобранный пример 3: обнаружение отрицательного цикла

В первой строке *n* и *m*, в следующих *m* строках — **ориентированные** рёбра `u v w` (*w* может быть отрицательным). Если в графе есть отрицательный цикл, выведите `есть`, иначе — кратчайшие расстояния от вершины 1 до всех вершин (`-`, если недостижима).

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

if any(d[i][i] < 0 for i in range(n)):      # возврат в себя отрицателен — отрицательный цикл
    print("есть")
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
        for (int i = 0; i < n; i++) if (d[i][i] < 0) manfiySikl = true;   // возврат в себя отрицателен — отрицательный цикл
        if (manfiySikl) {
            System.out.println("есть");
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
        if (d[i][i] < 0) {                              // возврат в себя отрицателен — отрицательный цикл
            cout << "есть" << endl;
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

Для `3 3` и рёбер `1 2 4`, `2 3 -2`, `3 1 -3` выводится `есть`: цикл 1 → 2 → 3 → 1 весит 4 − 2 − 3 = −1, и каждый его проход сокращает путь. Если последнее ребро — `3 1 1`, сумма цикла +3, и ответ `0 4 2`: 1 → 2 (4), 1 → 2 → 3 (4 − 2 = 2). В Java и C++ при отрицательных весах проверка на ∞ обязательна: иначе `INF + (−3)` окажется «меньше ∞», и появится несуществующий путь.

:::warning
**Частые ошибки**

- **Дейкстра на невзвешенном графе.** Работает, но BFS проще и быстрее.
- **Пометка вершины в BFS при извлечении из очереди.** Тогда вершина попадает в очередь несколько раз; помечайте её **при добавлении**.
- **Арифметика с ∞ при отрицательных весах.** При отрицательном *w* `INF + w` меньше ∞; сначала проверьте, что обе части конечны.
- **Вывод «кратчайшего пути» при отрицательном цикле.** В таком графе ответа нет — сначала проверьте `d[i][i] < 0`.
:::

:::tip
Прежде чем выбрать алгоритм, классифицируйте граф тремя вопросами: взвешены ли рёбра? есть ли отрицательные веса? вопрос об одном источнике или обо всех парах? Ответы укажут нужный алгоритм в таблице выше.
:::

## Итоги

- В невзвешенном графе кратчайший путь находится BFS за *O*(*V* + *E*); лабиринт — граф в виде сетки.
- Центр графа — вершина с наименьшим эксцентриситетом; все расстояния даёт Флойд.
- Флойд работает с отрицательными весами; `d[i][i] < 0` означает отрицательный цикл.
- Алгоритм выбирают по весам и типу вопроса: BFS, Дейкстра, Беллман–Форд или Флойд.

## Проверьте себя

:::check
Q: Почему BFS даёт кратчайший путь в невзвешенном графе?
A: Вершины посещаются в порядке удаления от старта; первое достижение вершины — за наименьшее число шагов.
Q: Что такое эксцентриситет вершины?
A: Кратчайшее расстояние от неё до самой дальней вершины.
Q: Если после Флойда `d[2][2] = −5`, что это значит?
A: Вершина 2 лежит на отрицательном цикле.
Q: Какой алгоритм подходит для лабиринта из 10⁶ клеток?
A: BFS — *O*(*R* · *C*), каждая клетка посещается один раз.
:::

:::link https://cp-algorithms.com/graph/breadth-first-search.html
CP-Algorithms: поиск в ширину
BFS, его применения и сложность (на английском).
:::
