---
title: Списки. Графы. Алгоритмы Дейкстры и Флойда
summary: Односвязные, двусвязные и кольцевые списки и их отличие от массива; элементы и виды графов, представление графа в компьютере — матрица смежности и списки смежности; задача о кратчайшем пути, алгоритм Дейкстры и алгоритм Флойда–Уоршелла.
---

## Почему это важно

Элементы массива лежат в памяти рядом — поэтому вставка или удаление в середине дороги. **Связный список** соединяет элементы указателями и выполняет эти операции за *O*(1). Эта идея лежит в основе всех последующих сложных структур — деревьев и графов.

**Граф** — самая общая модель взаимосвязанных объектов: города и дороги, пользователи и дружба, компьютеры и сетевые кабели, веб-страницы и ссылки. Навигатор находит кратчайший маршрут, а интернет-маршрутизаторы выбирают направление для пакета именно алгоритмами этой лекции.

:::note
**После этого занятия вы сможете:**

- различать односвязные, двусвязные и кольцевые списки и писать односвязный список;
- сравнивать сложность операций списка и массива;
- объяснять элементы и виды графов и хранить граф матрицей и списками;
- находить алгоритмом Дейкстры кратчайшие расстояния и путь от одной вершины;
- находить алгоритмом Флойда–Уоршелла расстояния между всеми парами.
:::

## Связные списки

Список состоит из **узлов**; каждый узел хранит значение и указатель (ссылку) на следующий узел. Первый узел — **голова**, указатель последнего пуст (`None`, `null`, `nullptr`).

| Вид | Устройство | Преимущество |
|---|---|---|
| Односвязный | в каждом узле `следующий` | прост, мало памяти |
| Двусвязный | `предыдущий` и `следующий` | движение в обе стороны, удаление узла за *O*(1) |
| Кольцевой | последний узел связан с головой | кольцевая очередь, очередь игроков |

| Операция | Массив | Связный список |
|---|---|---|
| Доступ к *i*-му элементу | *O*(1) | *O*(*n*) — надо идти от головы |
| Вставка в начало | *O*(*n*) | *O*(1) |
| Вставка/удаление после известного узла | *O*(*n*) | *O*(1) |
| Поиск по значению | *O*(*n*) | *O*(*n*) |

`LinkedList` в Java и `std::list` в C++ — двусвязные списки. На практике чаще используют списки на основе массива (`ArrayList`, `vector`): кеш процессора быстрее работает с соседними элементами.

## Графы

**Граф** *G* = (*V*, *E*) — множество **вершин** (*V*) и соединяющих их **рёбер** (*E*).

| Понятие | Смысл |
|---|---|
| Неориентированный граф | ребро в обе стороны: дружба |
| Ориентированный граф (орграф) | ребро в одну сторону — дуга: улица с односторонним движением, веб-ссылка |
| Взвешенный граф | на каждом ребре число — вес: расстояние, цена, время |
| Степень вершины | число инцидентных ей рёбер |
| Путь | вершины, последовательно соединённые рёбрами |
| Связный граф | между любыми двумя вершинами есть путь |

Представление графа в компьютере:

| Способ | Память | «Смежны ли *u* и *v*?» | Когда |
|---|---|---|---|
| **Матрица смежности** `d[u][v]` | *O*(*V*²) | *O*(1) | плотные небольшие графы (*V* ≤ ~2000) |
| **Списки смежности** — список соседей каждой вершины | *O*(*V* + *E*) | *O*(степень) | разреженные большие графы (дорожные сети, соцсети) |

## Кратчайший путь: алгоритм Дейкстры

**Алгоритм Дейкстры** (Э. Дейкстра, 1959) — кратчайшие расстояния от одной вершины до всех остальных в графе с неотрицательными весами:

```text
dist[s] = 0, остальные ∞; все вершины «непомеченные»
n раз:
    среди непомеченных выбрать v с наименьшим dist и пометить
    для каждого соседа u вершины v:
        если dist[v] + w(v, u) < dist[u]: dist[u] = dist[v] + w(v, u)   ← «релаксация»
```

Идея: расстояние помеченной вершины больше не меняется — поскольку веса неотрицательны, любой другой путь длиннее. Выбор массивом даёт *O*(*V*²); с **очередью с приоритетом** (кучей) — *O*((*V* + *E*) log *V*) для больших разреженных графов. С отрицательными весами Дейкстра работает неверно (нужен алгоритм Беллмана–Форда).

## Алгоритм Флойда–Уоршелла

Чтобы найти расстояния между **всеми парами**:

```text
для k = 1..n: для i = 1..n: для j = 1..n:
    d[i][j] = min(d[i][j], d[i][k] + d[k][j])
```

После шага *k* `d[i][j]` — длина кратчайшего пути, промежуточные вершины которого только из {1, …, *k*}. Три вложенных цикла — *O*(*V*³), память *O*(*V*²). Работает и с отрицательными весами рёбер (если нет отрицательного цикла). Внешним циклом **обязательно** должен быть цикл по *k*.

## Разобранный пример 1: односвязный список

В первой строке *q*, затем *q* команд: `boshiga X` (в начало), `oxiriga X` (в конец), `ochir X` (удалить первое *X*; если его нет — вывести `нет`), `chiqar` (вывести список; если пуст — `пуст`). Напишите список узлами и указателями; храните и последний узел, чтобы добавление в конец было *O*(1).

```python
class Tugun:
    def __init__(self, qiymat):
        self.qiymat = qiymat
        self.keyingi = None


bosh = None
oxir = None                                 # последний узел — добавление в конец O(1)

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
            print("нет")
            continue
        if oldingi is None:                 # удаляется голова
            bosh = joriy.keyingi
        else:
            oldingi.keyingi = joriy.keyingi # «обходим» узел
        if joriy is oxir:
            oxir = oldingi
    else:
        qiymatlar = []
        joriy = bosh
        while joriy is not None:
            qiymatlar.append(joriy.qiymat)
            joriy = joriy.keyingi
        print(*qiymatlar if qiymatlar else ["пуст"])
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
        Tugun bosh = null, oxir = null;             // последний узел — добавление в конец O(1)
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
                    System.out.println("нет");
                    continue;
                }
                if (oldingi == null) bosh = joriy.keyingi;      // удаляется голова
                else oldingi.keyingi = joriy.keyingi;           // «обходим» узел
                if (joriy == oxir) oxir = oldingi;
            } else {
                StringBuilder sb = new StringBuilder();
                for (Tugun t = bosh; t != null; t = t.keyingi) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(t.qiymat);
                }
                System.out.println(sb.length() > 0 ? sb.toString() : "пуст");
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
    Tugun* oxir = nullptr;                          // последний узел — добавление в конец O(1)
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
                cout << "нет" << endl;
                continue;
            }
            if (oldingi == nullptr) bosh = joriy->keyingi;      // удаляется голова
            else oldingi->keyingi = joriy->keyingi;             // «обходим» узел
            if (joriy == oxir) oxir = oldingi;
            delete joriy;
        } else {
            if (bosh == nullptr) cout << "пуст";
            for (Tugun* t = bosh; t != nullptr; t = t->keyingi) {
                cout << t->qiymat << (t->keyingi ? " " : "");
            }
            cout << endl;
        }
    }
    while (bosh != nullptr) {                       // освобождаем оставшиеся узлы
        Tugun* keyingi = bosh->keyingi;
        delete bosh;
        bosh = keyingi;
    }
    return 0;
}
```

Ввод:

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

Вывод:

```text
3 5 7
3 7
нет
пуст
```

При удалении сам узел не двигается — указатель предыдущего узла «обходит» его и связывается со следующим (*Tugun* — узел, *keyingi* — следующий, *bosh* — голова, *oxir* — хвост, *oldingi* — предыдущий, *joriy* — текущий). Есть три отдельных случая: удаление головы (нет `oldingi`), удаление последнего узла (обновляется `oxir`) и опустевший список. В C++ удалённый узел освобождается через `delete`, а в конце программы — все оставшиеся узлы.

## Разобранный пример 2: алгоритм Дейкстры

В первой строке *n* и *m* (вершины и рёбра), в следующих *m* строках — неориентированные рёбра `u v w`. Выведите кратчайшие расстояния от вершины 1 до всех вершин (`-`, если недостижима) и кратчайший путь от вершины 1 до вершины *n*. Используйте матрицу смежности и выбор за *O*(*n*²).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[INF] * (n + 1) for _ in range(n + 1)]     # матрица смежности
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u][v] = d[v][u] = min(d[u][v], w)

dist = [INF] * (n + 1)
dist[1] = 0
belgilangan = [False] * (n + 1)
ota = [0] * (n + 1)                             # для восстановления пути
for _ in range(n):
    v = -1
    for i in range(1, n + 1):                   # ближайшая непомеченная вершина
        if not belgilangan[i] and (v == -1 or dist[i] < dist[v]):
            v = i
    if dist[v] == INF:
        break
    belgilangan[v] = True
    for u in range(1, n + 1):                   # релаксация
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
        long[][] d = new long[n + 1][n + 1];                // матрица смежности
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
        int[] ota = new int[n + 1];                         // для восстановления пути
        for (int k = 0; k < n; k++) {
            int v = -1;
            for (int i = 1; i <= n; i++) {                  // ближайшая непомеченная вершина
                if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
            }
            if (dist[v] == INF) break;
            belgilangan[v] = true;
            for (int u = 1; u <= n; u++) {                  // релаксация
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
    vector<vector<long long>> d(n + 1, vector<long long>(n + 1, INF));   // матрица смежности
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u][v] = d[v][u] = min(d[u][v], w);
    }

    vector<long long> dist(n + 1, INF);
    dist[1] = 0;
    vector<bool> belgilangan(n + 1, false);
    vector<int> ota(n + 1, 0);                              // для восстановления пути
    for (int k = 0; k < n; k++) {
        int v = -1;
        for (int i = 1; i <= n; i++) {                      // ближайшая непомеченная вершина
            if (!belgilangan[i] && (v == -1 || dist[i] < dist[v])) v = i;
        }
        if (dist[v] == INF) break;
        belgilangan[v] = true;
        for (int u = 1; u <= n; u++) {                      // релаксация
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

Ввод:

```text
5 6
1 2 4
1 3 1
3 2 2
2 4 1
3 4 5
4 5 3
```

Вывод:

```text
0 3 1 4 7
1 3 2 4 5
```

Прямое ребро 1 → 2 весит 4, а путь 1 → 3 → 2 — 1 + 2 = 3, короче: когда вершина 3 была помечена, расстояние до вершины 2 «релаксировалось». Массив `ota` (родитель) запоминает, из какой вершины пришли в каждую; путь восстанавливается от конца к началу и выводится в обратном порядке (*belgilangan* — помечена, *yol* — путь). В Python прибавлять к `float("inf")` безопасно, а в Java и C++ за ∞ взято `MAX / 4` и наличие ребра проверяется отдельно — иначе `INF + w` переполнится.

## Разобранный пример 3: алгоритм Флойда–Уоршелла

В первой строке *n* и *m*, в следующих *m* строках — **ориентированные** рёбра `u v w`. Выведите матрицу кратчайших расстояний между всеми парами (`-`, если недостижимо).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                              # промежуточная вершина — внешний цикл!
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

        for (int k = 0; k < n; k++)                     // промежуточная вершина — внешний цикл!
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

    for (int k = 0; k < n; k++)                         // промежуточная вершина — внешний цикл!
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

Ввод:

```text
5 5
1 2 3
2 3 2
1 3 7
3 4 1
4 1 6
```

Вывод:

```text
0 3 5 6 -
9 0 2 3 -
7 10 0 1 -
6 9 11 0 -
- - - - 0
```

Прямое ребро 1 → 3 весит 7, а через вершину 2 — 3 + 2 = 5: Флойд нашёл это на шаге *k* = 2. У вершины 5 нет рёбер — она отрезана от остальных, и её строка и столбец заполнены `-`. Граф ориентированный, поэтому матрица несимметрична: расстояние 1 → 2 равно 3, а 2 → 1 — 9 (2 → 3 → 4 → 1). При *n* = 400 Флойд делает 6,4 · 10⁷ операций — меньше секунды; при *n* = 10⁴ было бы 10¹² — для таких графов лучше Дейкстра из каждой вершины.

:::warning
**Частые ошибки**

- **Забыть про голову или хвост при удалении из списка.** Указатели `bosh` и `oxir` остаются направленными на устаревший узел.
- **Дейкстра на графе с отрицательными весами.** Расстояние помеченной вершины могло бы потом уменьшиться — результат неверен.
- **Перестановка циклов во Флойде.** Если *k* — не внешний цикл, алгоритм работает неправильно.
- **Переполнение при прибавлении веса к ∞.** В Java и C++ `Long.MAX_VALUE + w` даёт отрицательное число; берите ∞ меньше или проверяйте наличие ребра.
:::

:::tip
Проверяя алгоритмы на графах, нарисуйте граф на бумаге: вершины — кружками, рёбра — линиями с весами. Записывайте в таблицу, какую вершину Дейкстра помечает на каждом шаге и как меняются расстояния.
:::

## Итоги

- Связный список соединяется указателями: вставка и удаление — *O*(1), но доступ по индексу — *O*(*n*).
- Граф — вершины и рёбра; бывает ориентированным/неориентированным и взвешенным; хранится матрицей (плотный) или списками смежности (разреженный).
- Дейкстра: от одной вершины, неотрицательные веса; *O*(*V*²) или с кучей *O*((*V* + *E*) log *V*).
- Флойд–Уоршелл: все пары, *O*(*V*³); внешний цикл — промежуточная вершина *k*.
- Путь восстанавливается через массив родителей от конца к началу.

## Проверьте себя

:::check
Q: Почему доступ к *i*-му элементу односвязного списка — *O*(*n*)?
A: Элементы не лежат рядом в памяти; чтобы до него дойти, нужно идти по указателям от головы.
Q: Как хранить дорожный граф с 10⁶ вершин и 3 · 10⁶ рёбер?
A: Списками смежности: матрице понадобилось бы 10¹² ячеек.
Q: Почему алгоритм Дейкстры не работает с отрицательными весами?
A: Расстояние помеченной вершины считается окончательным, а отрицательное ребро может потом его уменьшить.
Q: Какова сложность алгоритма Флойда–Уоршелла?
A: *O*(*V*³) по времени и *O*(*V*²) по памяти.
:::

:::link https://visualgo.net/en/sssp
VisuAlgo: кратчайшие пути
Анимация алгоритма Дейкстры и других (на английском).
:::

:::link https://cp-algorithms.com/graph/all-pair-shortest-path-floyd-warshall.html
CP-Algorithms: Флойд–Уоршелл
Алгоритм, доказательство и обнаружение отрицательных циклов (на английском).
:::
