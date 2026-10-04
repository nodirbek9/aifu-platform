---
title: Изучение сетевых топологий
summary: Практический анализ физических и логических топологий; представление сети в виде графа; типы кабелей и схемы обжима (T568A/T568B, прямой и перекрёстный кабель); проверка связности сети при обрыве линий; поиск единых точек отказа (критических узлов); сборка топологий в Packet Tracer.
---

## Почему это важно

На лекции M2 мы рассмотрели виды топологий, их преимущества и недостатки. На практике инженер отвечает на другой вопрос: «Если этот кабель оборвётся или этот коммутатор сгорит, кто останется без связи?» На этом занятии мы опишем сеть как **граф**, ответим на этот вопрос программой и соберём топологии в Packet Tracer.

:::note
**После этого занятия вы сможете:**

- записывать схему сети в виде графа (узлы и рёбра);
- знать схемы T568A и T568B и когда применяются прямой и перекрёстный кабели;
- подсчитывать, на сколько частей распадается сеть при обрыве линий;
- находить единые точки отказа (критические узлы) в сети;
- собирать в Packet Tracer топологии звезда, кольцо и гибридную и проверять связь.
:::

## Сеть — это граф

Любую топологию можно представить **графом**: **узлы** — устройства (компьютеры, коммутаторы, маршрутизаторы), **рёбра** — линии связи между ними. Тогда свойства топологии превращаются в понятия графа:

| Вопрос о сети | Понятие графа |
|---|---|
| Может ли один компьютер достичь другого? | есть ли между ними **путь** |
| Из скольких отдельных частей состоит сеть? | число **компонент связности** |
| Отказ какого устройства разделит сеть? | **точка сочленения** (критический узел) |
| Обрыв какого кабеля разделит сеть? | **мост** (ребро-мост) |
| Сколько кабелей идёт к центральному устройству? | **степень** узла |

Например, в топологии «звезда» центральный коммутатор — критический узел, а каждый кабель — мост (при обрыве отрезается один компьютер). В кольце критических узлов нет: при отказе одного узла остальные остаются связанными через оставшуюся часть кольца. В полносвязной сети нет ни критических узлов, ни мостов.

## Кабели и схемы обжима

Кабель витой пары обжимается в разъём RJ-45 по одной из двух стандартных схем:

| Контакт | T568A | T568B |
|---|---|---|
| 1 | бело-зелёный | бело-оранжевый |
| 2 | зелёный | оранжевый |
| 3 | бело-оранжевый | бело-зелёный |
| 4 | синий | синий |
| 5 | бело-синий | бело-синий |
| 6 | оранжевый | зелёный |
| 7 | бело-коричневый | бело-коричневый |
| 8 | коричневый | коричневый |

- **Прямой кабель** (straight-through) — оба конца по одной схеме (B–B). Соединяет **разнотипные** устройства: компьютер–коммутатор, маршрутизатор–коммутатор.
- **Перекрёстный кабель** (crossover) — один конец A, другой B. Соединяет **однотипные** устройства: компьютер–компьютер, коммутатор–коммутатор, маршрутизатор–компьютер.
- У современных устройств есть **Auto-MDI/X** — они сами определяют тип кабеля, но в Packet Tracer и на экзамене правило спрашивают.

## Практическая работа: топологии в Packet Tracer

1. **Звезда**: один коммутатор 2960 и 4 ПК. Назначьте компьютерам адреса 192.168.1.1–192.168.1.4 (маска 255.255.255.0) и выполните с PC1 `ping 192.168.1.4`.
2. **Расширенная звезда (дерево)**: соедините два коммутатора перекрёстным кабелем, добавьте к каждому по 3 ПК. Удалите кабель между коммутаторами и посмотрите, какие компьютеры потеряют связь.
3. **Кольцо**: соедините 4 коммутатора в кольцо. В Packet Tracer один порт остаётся оранжевым (заблокированным) — это **STP** (Spanning Tree Protocol) перевёл одну линию в резерв, чтобы кадры не циркулировали по кольцу бесконечно. Разорвите одну из рабочих линий — через несколько секунд включится резервная.
4. Для каждой схемы составьте таблицу: число устройств, число кабелей, критические узлы.

## Разобранный пример 1: число частей при обрыве кабелей

В первой строке — число узлов *n* и линий *m*, затем *m* линий. Потом идут *q* и *q* линий, которые обрываются одна за другой. После каждого обрыва выведите, на сколько компонент связности распалась сеть.

```python
def komponentlar(n, qirralar):
    qoshni = [[] for _ in range(n + 1)]
    for u, v in qirralar:
        qoshni[u].append(v)
        qoshni[v].append(u)
    korilgan = [False] * (n + 1)
    soni = 0
    for boshi in range(1, n + 1):
        if korilgan[boshi]:
            continue
        soni += 1                               # найдена новая часть
        korilgan[boshi] = True
        stek = [boshi]
        while stek:
            x = stek.pop()
            for y in qoshni[x]:
                if not korilgan[y]:
                    korilgan[y] = True
                    stek.append(y)
    return soni


n, m = map(int, input().split())
qirralar = [tuple(map(int, input().split())) for _ in range(m)]
q = int(input())
for _ in range(q):
    u, v = map(int, input().split())
    qirralar = [e for e in qirralar if e != (u, v) and e != (v, u)]   # линия разорвана
    print(komponentlar(n, qirralar))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int komponentlar(int n, List<int[]> qirralar) {
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int[] e : qirralar) {
            qoshni.get(e[0]).add(e[1]);
            qoshni.get(e[1]).add(e[0]);
        }
        boolean[] korilgan = new boolean[n + 1];
        int soni = 0;
        for (int boshi = 1; boshi <= n; boshi++) {
            if (korilgan[boshi]) continue;
            soni++;                                         // найдена новая часть
            korilgan[boshi] = true;
            ArrayDeque<Integer> stek = new ArrayDeque<>();
            stek.push(boshi);
            while (!stek.isEmpty()) {
                int x = stek.pop();
                for (int y : qoshni.get(x)) {
                    if (!korilgan[y]) {
                        korilgan[y] = true;
                        stek.push(y);
                    }
                }
            }
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<int[]> qirralar = new ArrayList<>();
        for (int i = 0; i < m; i++) qirralar.add(new int[] {in.nextInt(), in.nextInt()});
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qirralar.removeIf(e -> (e[0] == u && e[1] == v) || (e[0] == v && e[1] == u));   // линия разорвана
            System.out.println(komponentlar(n, qirralar));
        }
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int komponentlar(int n, const vector<pair<int, int>>& qirralar) {
    vector<vector<int>> qoshni(n + 1);
    for (auto& e : qirralar) {
        qoshni[e.first].push_back(e.second);
        qoshni[e.second].push_back(e.first);
    }
    vector<bool> korilgan(n + 1, false);
    int soni = 0;
    for (int boshi = 1; boshi <= n; boshi++) {
        if (korilgan[boshi]) continue;
        soni++;                                             // найдена новая часть
        korilgan[boshi] = true;
        vector<int> stek = {boshi};
        while (!stek.empty()) {
            int x = stek.back();
            stek.pop_back();
            for (int y : qoshni[x]) {
                if (!korilgan[y]) {
                    korilgan[y] = true;
                    stek.push_back(y);
                }
            }
        }
    }
    return soni;
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> qirralar(m);
    for (auto& e : qirralar) cin >> e.first >> e.second;
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        int u, v;
        cin >> u >> v;
        vector<pair<int, int>> qolgan;
        for (auto& e : qirralar) {
            if (!((e.first == u && e.second == v) || (e.first == v && e.second == u))) qolgan.push_back(e);
        }
        qirralar = qolgan;                                  // линия разорвана
        cout << komponentlar(n, qirralar) << endl;
    }
    return 0;
}
```

Ввод (1–2–3 — кольцо, 3–4 — соединительная линия, вокруг 4 — 5 и 6, звезда):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
3
1 2
3 4
4 5
```

Вывод:

```text
1
2
3
```

При обрыве линии кольца 1–2 сеть остаётся целой (путь 1–3–2). А линия 3–4 — мост: при её обрыве сеть делится на две части. Если оборвать линию звезды 4–5, компьютер 5 останется один.

## Разобранный пример 2: поиск критических узлов

Дан граф. Какие узлы **критические**: если их убрать, оставшиеся узлы уже не образуют одну связную сеть? По очереди «выключаем» каждый узел и проверяем остальные поиском (*n* поисков — достаточно для небольших сетей). Выведите критические узлы по возрастанию или `нет`, если их нет.

```python
def bogliqmi(n, qoshni, ochiq):
    tirik = [x for x in range(1, n + 1) if x != ochiq]
    korilgan = {tirik[0]}
    stek = [tirik[0]]
    while stek:
        x = stek.pop()
        for y in qoshni[x]:
            if y != ochiq and y not in korilgan:
                korilgan.add(y)
                stek.append(y)
    return len(korilgan) == len(tirik)        # дошли ли до всех


n, m = map(int, input().split())
qoshni = [[] for _ in range(n + 1)]
for _ in range(m):
    u, v = map(int, input().split())
    qoshni[u].append(v)
    qoshni[v].append(u)
kritik = [str(v) for v in range(1, n + 1) if not bogliqmi(n, qoshni, v)]
print(" ".join(kritik) if kritik else "нет")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean bogliqmi(int n, List<List<Integer>> qoshni, int ochiq) {
        int boshi = ochiq == 1 ? 2 : 1;
        boolean[] korilgan = new boolean[n + 1];
        korilgan[boshi] = true;
        int soni = 1;
        ArrayDeque<Integer> stek = new ArrayDeque<>();
        stek.push(boshi);
        while (!stek.isEmpty()) {
            int x = stek.pop();
            for (int y : qoshni.get(x)) {
                if (y != ochiq && !korilgan[y]) {
                    korilgan[y] = true;
                    soni++;
                    stek.push(y);
                }
            }
        }
        return soni == n - 1;                     // дошли ли до всех
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int i = 0; i < m; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qoshni.get(u).add(v);
            qoshni.get(v).add(u);
        }
        List<String> kritik = new ArrayList<>();
        for (int v = 1; v <= n; v++) {
            if (!bogliqmi(n, qoshni, v)) kritik.add(String.valueOf(v));
        }
        System.out.println(kritik.isEmpty() ? "нет" : String.join(" ", kritik));
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

bool bogliqmi(int n, const vector<vector<int>>& qoshni, int ochiq) {
    int boshi = ochiq == 1 ? 2 : 1;
    vector<bool> korilgan(n + 1, false);
    korilgan[boshi] = true;
    int soni = 1;
    vector<int> stek = {boshi};
    while (!stek.empty()) {
        int x = stek.back();
        stek.pop_back();
        for (int y : qoshni[x]) {
            if (y != ochiq && !korilgan[y]) {
                korilgan[y] = true;
                soni++;
                stek.push_back(y);
            }
        }
    }
    return soni == n - 1;                         // дошли ли до всех
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> qoshni(n + 1);
    for (int i = 0; i < m; i++) {
        int u, v;
        cin >> u >> v;
        qoshni[u].push_back(v);
        qoshni[v].push_back(u);
    }
    string kritik;
    for (int v = 1; v <= n; v++) {
        if (!bogliqmi(n, qoshni, v)) kritik += (kritik.empty() ? "" : " ") + to_string(v);
    }
    cout << (kritik.empty() ? "нет" : kritik) << endl;
    return 0;
}
```

Ввод (гибридная сеть из первого примера, ничего не оборвано):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
```

Вывод:

```text
3 4
```

Узел 3 отделяет кольцо от остальной части, а узел 4 — центр звезды. Их нужно защитить резервными линиями — например, если добавить линию 2–4, узел 3 перестанет быть критическим. Для полносвязной сети из 4 узлов ответ — `нет`.

Внимание: программы написаны для сетей минимум из 2 узлов (после выключения одного узла должен остаться хотя бы один).

:::warning
**Частые ошибки**

- **Смешивать физическую и логическую топологию.** Кольцо в Packet Tracer физически — кольцо, но так как STP блокирует одну линию, логически оно работает как дерево.
- **Использовать прямой кабель вместо перекрёстного.** На устройствах без Auto-MDI/X два коммутатора или два компьютера прямым кабелем не соединятся.
- **Хранить ребро только в одном направлении.** Кабель двусторонний — в списки смежности записываются и *u* → *v*, и *v* → *u*; при обрыве удаляются оба направления.
- **Считать критическим узел с наибольшей степенью.** Степень не обязана быть большой: у узла 3 степень 3, у узла 1 — 2; важно то, что он разделяет сеть.
:::

:::tip
Нарисуйте на бумаге сеть своего дома или факультета (роутер, точки Wi-Fi, коммутаторы, компьютеры). Превратите её в граф и подайте программе второго примера: какое устройство — самая опасная единая точка отказа?
:::

## Итоги

- Любая топология — граф: узлы — устройства, рёбра — линии связи.
- Связность после обрыва линий проверяется числом компонент, единые точки отказа — критическими узлами.
- T568A/T568B — схемы обжима RJ-45; прямой кабель соединяет разнотипные устройства, перекрёстный — однотипные.
- В физических кольцах STP переводит одну линию в резерв и образует логическое дерево.

## Проверьте себя

:::check
Q: Какой узел критический в топологии «звезда»?
A: Центральное устройство (коммутатор или концентратор) — при его отказе все компьютеры теряют связь.
Q: Если в простом кольце оборвать одну линию, разделится ли сеть на две части?
A: Нет, узлы останутся связанными через оставшуюся часть кольца; сеть разделится после второго обрыва.
Q: Каким кабелем подключать компьютер к коммутатору?
A: Прямым (straight-through) — это разнотипные устройства.
Q: В каких контактах отличаются схемы T568A и T568B?
A: В контактах 1, 2 и 3, 6 — зелёная и оранжевая пары поменяны местами.
Q: Почему в Packet Tracer при соединении 3 коммутаторов в кольцо один порт блокируется?
A: STP переводит одну линию в резерв, чтобы кадры не циркулировали по кольцу бесконечно.
:::

:::link https://www.netacad.com/cisco-packet-tracer
Cisco Packet Tracer
Загрузка сетевого симулятора и бесплатные вводные курсы (на английском).
:::
