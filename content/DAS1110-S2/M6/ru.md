---
title: Контейнеры (коллекции)
summary: Понятие контейнера; список (list, ArrayList, vector), множество (set), словарь (dict, Map, map), очередь и дек (deque); сложность операций, упорядоченные и хеш-контейнеры, итераторы, выбор подходящего контейнера.
---

## Почему это важно

Подсчитать, сколько раз каждое слово встречается в тексте, убрать повторы из списка, обслужить клиентов в очереди — всё это можно написать и на массивах, но код получится длинным, медленным и полным ошибок. В стандартной библиотеке каждого языка для таких задач есть готовые проверенные структуры — **контейнеры** (коллекции).

Правильный выбор контейнера может ускорить программу в десятки раз: в списке из 10⁵ элементов вопрос «есть ли?» занимает 10⁵ шагов, в множестве — несколько. На этой лекции изучим основные контейнеры, их скорость и то, когда какой выбирать.

:::note
**После этого занятия вы сможете:**

- объяснять, что такое список, множество, словарь, очередь и дек и когда их используют;
- называть сложность основных операций каждого контейнера;
- пользоваться этими контейнерами в Python, Java и C++;
- объяснять разницу между упорядоченными и хеш-контейнерами;
- выбирать контейнер, подходящий для задачи.
:::

## Виды контейнеров

| Контейнер | Идея | Python | Java | C++ |
|---|---|---|---|---|
| Список | упорядоченные элементы, доступ по индексу | `list` | `ArrayList` | `vector` |
| Множество | элементы без повторов, вопрос «есть ли?» | `set` | `HashSet`, `TreeSet` | `unordered_set`, `set` |
| Словарь (отображение) | пары ключ → значение | `dict` | `HashMap`, `TreeMap` | `unordered_map`, `map` |
| Очередь | первым пришёл — первым ушёл (FIFO) | `collections.deque` | `ArrayDeque` (`Queue`) | `queue` |
| Дек | добавление и удаление с обоих концов | `collections.deque` | `ArrayDeque` (`Deque`) | `deque` |
| Стек | последним пришёл — первым ушёл (LIFO) | `list` (`append`/`pop`) | `ArrayDeque` (`push`/`pop`) | `stack` |

## Сложность операций

| Операция | Список | Хеш-множество/словарь | Упорядоченное множество/словарь | Дек |
|---|---|---|---|---|
| Добавить в конец | *O*(1), в среднем | — | — | *O*(1) |
| Добавить в начало | *O*(*n*) | — | — | *O*(1) |
| Взять по индексу | *O*(1) | — | — | *O*(1) |
| Есть ли элемент? | *O*(*n*) | *O*(1), в среднем | *O*(log *n*) | *O*(*n*) |
| Добавить / удалить (по ключу) | *O*(*n*) | *O*(1), в среднем | *O*(log *n*) | — |
| Обход по порядку | в порядке добавления | порядок не гарантирован | в **отсортированном** порядке | в порядке добавления |

«В среднем» — в большинстве случаев; при добавлении в список память время от времени расширяется, и одно добавление тогда длится дольше.

**Хеш-контейнер** (`HashSet`, `unordered_map`, `dict`/`set` в Python) сразу находит место элемента по его **хеш**-значению — очень быстро, но элементы не упорядочены. **Упорядоченный** контейнер (`TreeMap`, `map`/`set` в C++) хранит элементы в сбалансированном дереве — чуть медленнее, но при обходе ключи идут в отсортированном порядке. В Python `dict` хранит ключи **в порядке добавления**; если нужен отсортированный порядок, используют `sorted(d)`.

## Работа со словарём

Словарь — таблица «ключ → значение»: значение быстро находится по ключу. Самая частая задача — **подсчёт**:

| Язык | Увеличить значение | Есть ли ключ? |
|---|---|---|
| Python | `d[k] = d.get(k, 0) + 1` или `collections.Counter` | `k in d` |
| Java | `m.merge(k, 1, Integer::sum)` или `m.put(k, m.getOrDefault(k, 0) + 1)` | `m.containsKey(k)` |
| C++ | `m[k]++` (отсутствующий ключ создаётся с 0 автоматически) | `m.count(k)` или `m.find(k) != m.end()` |

В C++ `m[k]` создаёт отсутствующий ключ, даже если используется только для **чтения**, — поэтому для проверки применяют `count` или `find`.

## Итераторы

Чтобы обойти элементы контейнера, во всех языках работает цикл «для каждого элемента»: `for x in s`, `for (int x : s)`, `for (int x : s)`. За ним стоит **итератор** — объект, указывающий на текущую позицию в контейнере. В C++ итераторы используются явно: `s.begin()` — первый элемент, `s.end()` — позиция после последнего; алгоритмы вроде `sort(v.begin(), v.end())` и `find` работают именно с итераторами.

Удалять элементы из контейнера во время его обхода опасно: в Java — `ConcurrentModificationException`, в C++ — неопределённое поведение, в Python — `RuntimeError` (для словаря) или пропуск элементов.

## Разобранный пример 1: частоты слов (словарь)

Дана одна строка текста (строчные латинские буквы и пробелы). Выведите каждое слово и сколько раз оно встречается, слова — в алфавитном порядке.

```python
matn = input().split()

chastota = {}
for soz in matn:
    chastota[soz] = chastota.get(soz, 0) + 1

for soz in sorted(chastota):          # dict хранит порядок добавления — сортируем
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

        Map<String, Integer> chastota = new TreeMap<>();   // ключи отсортированы
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
    map<string, int> chastota;             // ключи отсортированы
    string soz;
    while (cin >> soz) chastota[soz]++;    // отсутствующий ключ создаётся с 0

    for (const auto& [kalit, soni] : chastota) {
        cout << kalit << " " << soni << endl;
    }
    return 0;
}
```

Ввод:

```text
bir ikki bir uch ikki bir
```

Вывод:

```text
bir 3
ikki 2
uch 1
```

`TreeMap` в Java и `map` в C++ — упорядоченные словари, поэтому при обходе ключи сами идут отсортированными. `dict` в Python хранит порядок добавления, поэтому добавлен `sorted` (*chastota* — частота, *soz* — слово). В C++ `auto& [kalit, soni]` раскладывает пару на две переменные (C++17).

## Разобранный пример 2: множества — общие элементы

В первой и второй строках — целые числа (возможны повторы). Выведите без повторов, по возрастанию числа, встречающиеся в обеих строках (если таких нет — `-`), а в следующей строке — сколько всего **различных** чисел было введено.

```python
a = set(map(int, input().split()))
b = set(map(int, input().split()))

umumiy = sorted(a & b)                 # пересечение
print(*umumiy if umumiy else "-")
print(len(a | b))                      # объединение
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

        Set<Integer> umumiy = new TreeSet<>(a);   // отсортированная копия
        umumiy.retainAll(b);                      // пересечение
        Set<Integer> hammasi = new HashSet<>(a);
        hammasi.addAll(b);                        // объединение

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
    while (oqim >> x) s.insert(x);         // повтор не добавляется
    return s;
}

int main() {
    set<int> a = oqish();
    set<int> b = oqish();

    bool birinchi = true;
    for (int x : a) {                      // set отсортирован
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

Ввод:

```text
5 1 3 3 9 7
3 8 5 5 2
```

Вывод:

```text
3 5
7
```

Различные числа: 1, 2, 3, 5, 7, 8, 9 — всего 7. Множество не принимает повторы, поэтому «число различных элементов» — это просто размер множества (*umumiy* — общие, *hammasi* — все). В Python `&` и `|` — операции пересечения и объединения; в Java — `retainAll` и `addAll`. С массивами эта задача потребовала бы вложенных циклов и времени *O*(*n* · *m*), со множествами она почти линейна.

## Разобранный пример 3: очередь — считалка

*n* человек стоят по кругу (с номерами от 1 до *n*). Счёт начинается с 1-го человека, и каждый *k*-й выходит из круга; счёт продолжается со следующего. Выведите порядок выбывания (это называется задачей Иосифа).

```python
from collections import deque

n, k = map(int, input().split())
navbat = deque(range(1, n + 1))

tartib = []
while navbat:
    navbat.rotate(-(k - 1))            # переносим k-1 человек в конец
    tartib.append(navbat.popleft())    # k-й выходит
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
            for (int i = 0; i < k - 1; i++) navbat.addLast(navbat.pollFirst());   // из начала в конец
            if (sb.length() > 0) sb.append(' ');
            sb.append(navbat.pollFirst());                                     // k-й выходит
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
        for (int i = 0; i < k - 1; i++) {      // из начала в конец
            navbat.push(navbat.front());
            navbat.pop();
        }
        if (!birinchi) cout << ' ';
        cout << navbat.front();                // k-й выходит
        navbat.pop();
        birinchi = false;
    }
    cout << endl;
    return 0;
}
```

При вводе `7 3` выводится `3 6 2 7 5 1 4`. Очередь (*navbat*) моделирует круг: посчитанный, но не выбывший человек берётся из начала очереди и ставится в конец. Взятие из начала в деке и очереди — *O*(1), а `pop(0)` у `list` в Python — *O*(*n*), потому что остальные элементы сдвигаются. Поэтому для очереди используют `deque`, а не `list`.

:::warning
**Частые ошибки**

- **Многократный вопрос «есть ли?» к списку.** `if x in royxat` внутри цикла даёт *O*(*n*²); используйте множество.
- **Проверка через `m[k]` в C++.** Отсутствующий ключ создаётся, и словарь растёт; используйте `count` или `find`.
- **Ожидание порядка от хеш-контейнера.** `HashMap` и `unordered_map` выдают элементы в произвольном порядке; если порядок важен — `TreeMap`, `map` или сортировка.
- **`list.pop(0)` для очереди в Python.** Каждое извлечение — *O*(*n*); используйте `deque.popleft()`.
:::

:::tip
Выбирая контейнер, определите **самую частую операцию** задачи: взятие по индексу — список; «есть ли?» — множество; значение по ключу — словарь; работа с обоими концами — дек. Если важен порядок — упорядоченный вариант.
:::

## Итоги

- Контейнеры — готовые структуры данных стандартной библиотеки: список, множество, словарь, очередь, дек, стек.
- Список быстр по индексу, но медленен при поиске; множество и словарь быстры по ключу.
- Хеш-контейнеры работают в среднем за *O*(1), упорядоченные — за *O*(log *n*), зато обходятся в отсортированном порядке.
- Очередь и дек берут из начала за *O*(1).
- Контейнер выбирают по операции, которую задача выполняет чаще всего.

## Проверьте себя

:::check
Q: Сколько времени занимает вопрос «есть ли элемент?» в списке и в хеш-множестве?
A: В списке *O*(*n*), в хеш-множестве в среднем *O*(1).
Q: В чём главное различие `HashMap` и `TreeMap`?
A: `HashMap` быстрее (*O*(1)), но не упорядочен; `TreeMap` работает за *O*(log *n*), но хранит ключи в отсортированном порядке.
Q: Какой скрытый эффект у `if (m["olma"] > 0)` в C++?
A: Если ключа «olma» нет, он создаётся со значением 0 и добавляется в словарь.
Q: Почему в Python для очереди используют `deque`?
A: `deque.popleft()` — *O*(1), а `list.pop(0)` — *O*(*n*): остальные элементы сдвигаются.
Q: Как найти число различных слов в строке?
A: Положить слова во множество и взять его размер: `len(set(matn.split()))`.
:::

:::link https://docs.python.org/3/library/collections.html
Python: модуль collections
`deque`, `Counter`, `defaultdict` и другие контейнеры (на английском).
:::

:::link https://en.cppreference.com/w/cpp/container
C++: библиотека контейнеров
`vector`, `set`, `map`, `deque`, `queue` и их сложность (на английском).
:::
