---
title: Деревья. Дерево Фенвика. Дерево отрезков. Сбалансированные деревья (практическое занятие)
summary: Задача запросов на отрезке; префиксные суммы — запрос за O(1) на неизменном массиве; дерево отрезков — минимум на отрезке и обновление элемента за O(log n); идея дерева Фенвика; три случая удаления узла из двоичного дерева поиска.
---

## Почему это важно

Во многих задачах над массивом вперемешку идут два вида операций: **запрос на отрезке** («каковы продажи с 3-го по 10-й день?», «какая самая низкая температура на этом отрезке?») и **обновление** («данные 5-го дня исправлены»). Обходить отрезок при каждом запросе — *O*(*n*); на 10⁵ запросов это 10¹⁰ операций. Структуры на основе деревьев выполняют обе операции за *O*(log *n*).

На этом занятии идём от простого к сложному: префиксные суммы (без обновлений), дерево отрезков (с обновлениями) и удаление из двоичного дерева поиска.

:::note
**После этого занятия вы сможете:**

- находить сумму на отрезке за *O*(1) с помощью префиксных сумм;
- строить дерево отрезков и выполнять минимум на отрезке и обновление элемента за *O*(log *n*);
- объяснять, когда удобно дерево Фенвика;
- правильно удалять из ДДП лист, узел с одним и с двумя потомками.
:::

## Префиксные суммы

`p[i] = a[1] + a[2] + … + a[i]`, `p[0] = 0`. Тогда сумма отрезка [*l*, *r*]:

```text
sum(l, r) = p[r] − p[l − 1]
```

Подготовка — *O*(*n*), каждый запрос — *O*(1). Недостаток: если меняется один элемент, все `p[i]` после него надо пересчитать: *O*(*n*). Поэтому префиксные суммы — для **неизменных** данных.

## Дерево отрезков

**Дерево отрезков** (segment tree) — двоичное дерево, каждый узел которого хранит результат (сумму, минимум, максимум) для отрезка массива. Корень — весь массив, листья — отдельные элементы. Хранение в массиве: *n* округляется вверх до степени двойки, листья лежат в `t[n … 2n − 1]`, а потомки узла `i` — в `2i` и `2i + 1`.

| Операция | Как | Сложность |
|---|---|---|
| Обновление `a[i] = x` | изменить лист и пересчитать предков до корня | *O*(log *n*) |
| Запрос на отрезке [*l*, *r*] | подниматься от обеих границ, объединяя узлы, целиком лежащие внутри | *O*(log *n*) |

Дерево отрезков работает с любой **ассоциативной** операцией (сумма, min, max, НОД).

## Дерево Фенвика

**Дерево Фенвика** (двоичное индексированное дерево, 1994) — более компактная структура для префиксных сумм: один массив из *n* элементов, обход битовой операцией `i & (−i)`. Обновление и префиксная сумма — *O*(log *n*), а код гораздо короче, чем у дерева отрезков. Ограничение: в основном для «обратимых» операций вроде суммы (для минимума неудобно). Вы напишете его сами в практическом задании.

| Структура | Обновление | Запрос | Когда |
|---|---|---|---|
| Префиксные суммы | *O*(*n*) | *O*(1) | данные не меняются |
| Дерево Фенвика | *O*(log *n*) | *O*(log *n*) | суммы + обновления |
| Дерево отрезков | *O*(log *n*) | *O*(log *n*) | min/max или сложная операция + обновления |

## Удаление из ДДП

| Случай | Как |
|---|---|
| Узел — лист | просто удалить |
| Один потомок | поставить потомка на его место |
| Два потомка | скопировать в узел **наименьший** ключ правого поддерева (следующий элемент), затем удалить этот элемент из правого поддерева |

В третьем случае свойство ДДП сохраняется: следующий элемент больше всех слева и не больше всех справа.

## Разобранный пример 1: префиксные суммы

В первой строке *n*, во второй — *n* чисел, в третьей — *q*, затем *q* запросов `l r` (с 1). Для каждого запроса выведите сумму отрезка [*l*, *r*].

```python
import sys

kirish = sys.stdin.read().split()           # быстрое чтение большого ввода
n = int(kirish[0])
a = list(map(int, kirish[1:n + 1]))
p = [0] * (n + 1)
for i in range(n):
    p[i + 1] = p[i] + a[i]                  # p[i] — сумма первых i элементов

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
        for (int i = 0; i < n; i++) p[i + 1] = p[i] + Long.parseLong(st.nextToken());   // p[i] — сумма первых i элементов

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
    ios::sync_with_stdio(false);                    // быстрое чтение большого ввода
    cin.tie(nullptr);
    int n;
    cin >> n;
    vector<long long> p(n + 1, 0);
    for (int i = 0; i < n; i++) {
        long long x;
        cin >> x;
        p[i + 1] = p[i] + x;                        // p[i] — сумма первых i элементов
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

Для `6`, `5 2 8 1 9 3` и запросов `1 3`, `2 5`, `4 4`, `1 6` выводится `15`, `20`, `1`, `28`. Благодаря `p[0] = 0` случай `l = 1` не требует отдельной проверки. При большом числе запросов важны и ввод-вывод: Python читает весь ввод сразу, Java использует `BufferedReader`, C++ — `sync_with_stdio(false)` (*kirish* — ввод, *natija* — результат).

## Разобранный пример 2: дерево отрезков — минимум на отрезке

В первой строке *n*, во второй — *n* чисел, в третьей — *q*, затем *q* команд: `min l r` — вывести наименьшее значение на [*l*, *r*], `set i x` — заменить *i*-й элемент на *x* (индексы с 1).

```python
import sys

kirish = sys.stdin.read().split()
n = int(kirish[0])
olcham = 1
while olcham < n:
    olcham *= 2                             # степень двойки
INF = float("inf")
t = [INF] * (2 * olcham)
for i in range(n):
    t[olcham + i] = int(kirish[1 + i])      # листья
for i in range(olcham - 1, 0, -1):
    t[i] = min(t[2 * i], t[2 * i + 1])      # родитель = минимум потомков

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
        while i >= 1:                       # пересчёт до корня
            t[i] = min(t[2 * i], t[2 * i + 1])
            i //= 2
    else:
        l, r = olcham + x - 1, olcham + y   # [l, r) — полуоткрытый отрезок
        eng = INF
        while l < r:                        # подъём от границ вверх
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
        while (olcham < n) olcham *= 2;                         // степень двойки
        long[] t = new long[2 * olcham];
        Arrays.fill(t, Long.MAX_VALUE);
        StringTokenizer st = new StringTokenizer(oquvchi.readLine());
        for (int i = 0; i < n; i++) t[olcham + i] = Long.parseLong(st.nextToken());   // листья
        for (int i = olcham - 1; i >= 1; i--) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // родитель = минимум потомков

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
                for (i /= 2; i >= 1; i /= 2) t[i] = Math.min(t[2 * i], t[2 * i + 1]);   // пересчёт до корня
            } else {
                int l = olcham + x - 1, r = olcham + (int) y;  // [l, r) — полуоткрытый отрезок
                long eng = Long.MAX_VALUE;
                while (l < r) {                                 // подъём от границ вверх
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
    while (olcham < n) olcham *= 2;                             // степень двойки
    vector<long long> t(2 * olcham, LLONG_MAX);
    for (int i = 0; i < n; i++) cin >> t[olcham + i];           // листья
    for (int i = olcham - 1; i >= 1; i--) t[i] = min(t[2 * i], t[2 * i + 1]);   // родитель = минимум потомков

    int q;
    cin >> q;
    while (q--) {
        string buyruq;
        long long x, y;
        cin >> buyruq >> x >> y;
        if (buyruq == "set") {
            int i = olcham + x - 1;
            t[i] = y;
            for (i /= 2; i >= 1; i /= 2) t[i] = min(t[2 * i], t[2 * i + 1]);   // пересчёт до корня
        } else {
            int l = olcham + x - 1, r = olcham + y;             // [l, r) — полуоткрытый отрезок
            long long eng = LLONG_MAX;
            while (l < r) {                                     // подъём от границ вверх
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

Ввод:

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

Вывод:

```text
2
1
3
10
5
```

После `set 4 10` единица в 4-м элементе исчезла, поэтому минимум [3, 6] стал `3` — обновление пересчитало лишь лист и его предков (log₂ 8 = 3 узла) (*olcham* — размер, *eng* — наименьшее). В запросе `l` и `r` поднимаются от листьев: «нечётный» узел на границе, целиком лежащий внутри отрезка, добавляется к результату. Поскольку размер дерева округлён до 8, лишние листья заполнены ∞ — они не влияют на минимум.

## Разобранный пример 3: удаление из ДДП

В первой строке — *n* ключей, во второй — удаляемые ключи. Вставьте ключи в ДДП, затем удалите заданные (отсутствующий ключ игнорируется) и выведите получившееся дерево прямым обходом (preorder); если дерево пусто — `-`.

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
    if t is None:                           # ключа нет
        return None
    if kalit < t.kalit:
        t.chap = ochir(t.chap, kalit)
    elif kalit > t.kalit:
        t.ong = ochir(t.ong, kalit)
    elif t.chap is None:                    # лист или только правый потомок
        return t.ong
    elif t.ong is None:                     # только левый потомок
        return t.chap
    else:                                   # два потомка: замена следующим элементом
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
        if (t == null) return null;                         // ключа нет
        if (kalit < t.kalit) t.chap = ochir(t.chap, kalit);
        else if (kalit > t.kalit) t.ong = ochir(t.ong, kalit);
        else if (t.chap == null) return t.ong;              // лист или только правый потомок
        else if (t.ong == null) return t.chap;              // только левый потомок
        else {                                              // два потомка: замена следующим элементом
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
    if (!t) return nullptr;                                 // ключа нет
    if (kalit < t->kalit) {
        t->chap = ochir(t->chap, kalit);
    } else if (kalit > t->kalit) {
        t->ong = ochir(t->ong, kalit);
    } else if (!t->chap) {                                  // лист или только правый потомок
        Tugun* bola = t->ong;
        delete t;
        return bola;
    } else if (!t->ong) {                                   // только левый потомок
        Tugun* bola = t->chap;
        delete t;
        return bola;
    } else {                                                // два потомка: замена следующим элементом
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

Ввод:

```text
50 30 70 20 40 60 80
20 30 50 99
```

Вывод:

```text
60 40 70 80
```

`20` — лист: просто удалён. У `30` остался один потомок (`40`) — он занял место `30`. У `50` два потомка: наименьший ключ правого поддерева `60` скопирован в корень, а сам лист `60` удалён. `99` в дереве нет — проигнорирован (*ochir* — удалить, *keyingi* — следующий элемент). Итог: корень 60, слева 40, справа 70 с правым потомком 80 — свойство ДДП сохранено.

:::warning
**Частые ошибки**

- **Вычитание `p[l]` в префиксной сумме.** Правильно `p[r] − p[l − 1]`; иначе вычтется и *l*-й элемент.
- **Не пересчитаны предки после обновления в дереве отрезков.** Запросы вернут старое значение.
- **Заполнение лишних листьев нулём.** Для минимума 0 портит результат; нужен нейтральный элемент операции — ∞ (а для суммы — 0).
- **Копирование только ключа при удалении узла с двумя потомками.** Старый узел следующего элемента тоже нужно удалить, иначе ключ останется дважды.
:::

:::tip
Нарисуйте на бумаге дерево отрезков с 8 листьями и подпишите над каждым узлом его отрезок (`[1..4]`, `[5..6]`, …). Когда на рисунке видно, какие узлы объединяет запрос, код понять гораздо легче.
:::

## Итоги

- Префиксные суммы дают сумму отрезка неизменного массива за *O*(1): `p[r] − p[l − 1]`.
- Дерево отрезков выполняет обновление и запрос на отрезке за *O*(log *n*) для любой ассоциативной операции.
- Дерево Фенвика компактнее для сумм и с коротким кодом; для операций вроде минимума удобно дерево отрезков.
- Удаление из ДДП: лист — удалить, один потомок — поднять его, два потомка — заменить следующим элементом.

## Проверьте себя

:::check
Q: Каков массив префиксных сумм для `a = 4 1 3 2`?
A: `p = 0 4 5 8 10`.
Q: Почему префиксные суммы не подходят для часто обновляемых данных?
A: При изменении одного элемента надо пересчитать все `p[i]` после него — *O*(*n*).
Q: Сколько узлов меняет обновление в дереве отрезков?
A: Лист и всех его предков — log₂ *n* + 1.
Q: Какой ключ ставится на место узла с двумя потомками при удалении из ДДП?
A: Наименьший ключ правого поддерева (или наибольший левого).
:::

:::link https://cp-algorithms.com/data_structures/segment_tree.html
CP-Algorithms: дерево отрезков
Построение, запросы, обновления и варианты (на английском).
:::
