---
title: Деревья как структура данных. Двоичные и сбалансированные деревья. Сортировка с помощью дерева
summary: Понятие дерева — корень, узел, лист, высота; ориентированные, упорядоченные и двоичные деревья; двоичное дерево поиска — вставка, поиск, обходы (прямой, симметричный, обратный, по уровням); сбалансированность, АВЛ и красно-чёрные деревья; сортировка деревом.
---

## Почему это важно

Папки на компьютере, структура HTML-страницы, структура организации, генеалогическое древо — всё это **деревья**. Дерево хранит данные иерархически и, если построено правильно, ищет, вставляет и удаляет за *O*(log *n*) — ищет так же быстро, как отсортированный массив, а меняется так же быстро, как список.

Индексы баз данных (B-деревья), `TreeMap` в Java и `map` в C++ (красно-чёрные деревья), синтаксические деревья в компиляторах — это практические воплощения темы этой лекции.

:::note
**После этого занятия вы сможете:**

- объяснять элементы дерева (корень, узел, лист, высота) и его виды;
- писать вставку и поиск в двоичном дереве поиска;
- обходить дерево в прямом, симметричном, обратном порядке и по уровням;
- объяснять, почему важна сбалансированность, и идею сбалансированных деревьев;
- сортировать с помощью дерева.
:::

## Понятие дерева

**Дерево** — связный граф без циклов; если одну вершину выбрать **корнем**, у каждого другого узла будет ровно один **родитель**.

| Понятие | Смысл |
|---|---|
| Корень | единственный узел без родителя |
| Лист | узел без потомков |
| Глубина узла | число рёбер от корня до него |
| Высота дерева | число уровней (глубина самого глубокого листа + 1) |
| Поддерево | узел и все его потомки |

**Ориентированное дерево** — рёбра направлены от корня к потомкам. **Упорядоченное дерево** — важен порядок потомков (первый, второй, …). **Двоичное дерево** — у каждого узла не более двух потомков: левый и правый.

## Двоичное дерево поиска

**Двоичное дерево поиска** (ДДП, binary search tree) — для каждого узла: все ключи левого поддерева **меньше** его, а правого — **больше или равны**.

- **Поиск**: от корня, если ключ меньше — влево, если больше — вправо, как в двоичном поиске.
- **Вставка**: идём, как при поиске, и в пустое место ставим новый лист.
- **Сложность**: *O*(*h*), где *h* — высота. В сбалансированном дереве *h* ≈ log₂ *n*, но если вставлять элементы в **отсортированном** порядке, дерево превращается в одну «ветку» и *h* = *n*.

## Обход дерева

| Способ | Порядок | Применение |
|---|---|---|
| Прямой (preorder) | узел → левое → правое | копирование дерева, префиксная запись выражения |
| Симметричный (inorder) | левое → узел → правое | в ДДП ключи выходят в **отсортированном** порядке |
| Обратный (postorder) | левое → правое → узел | удаление дерева, подсчёт размера папки |
| По уровням (level order) | от корня вниз, каждый уровень слева направо | с помощью очереди; поиск ближайшего узла |

## Сбалансированные деревья

Дерево **сбалансировано**, если в каждом узле высоты левого и правого поддеревьев почти равны. Тогда *h* = *O*(log *n*), и все операции быстры. Деревья, автоматически поддерживающие баланс, после каждой вставки и удаления выполняют **повороты** (rotations):

| Дерево | Условие баланса | Где |
|---|---|---|
| **АВЛ** (Адельсон-Вельский, Ландис, 1962) | в каждом узле высоты поддеревьев отличаются не более чем на 1 | системы с большим числом поисков |
| **Красно-чёрное** | правила цвета узлов; самый длинный путь не более чем вдвое длиннее кратчайшего | Java `TreeMap`, C++ `map`/`set` |
| **B-дерево** | много потомков, все листья на одном уровне | индексы баз данных и файловых систем |

## Сортировка с помощью дерева

**Сортировка деревом** (tree sort): вставить все элементы в ДДП, затем вывести симметричным обходом. Со сбалансированным деревом — *O*(*n* log *n*); с обычным ДДП в худшем случае (отсортированный вход) — *O*(*n*²). Преимущество: порядок поддерживается по мере добавления элементов, и отсортированный список можно получить в любой момент.

## Разобранный пример 1: ДДП и сортировка деревом

В первой строке *n*, во второй — *n* чисел. Вставьте их в ДДП в заданном порядке (равный ключ — вправо), выведите отсортированную последовательность симметричным обходом и высоту дерева.

```python
import sys

sys.setrecursionlimit(10000)


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(tugun, kalit):
    if tugun is None:
        return Tugun(kalit)                 # пустое место — новый лист
    if kalit < tugun.kalit:
        tugun.chap = qosh(tugun.chap, kalit)
    else:
        tugun.ong = qosh(tugun.ong, kalit)
    return tugun


def simmetrik(tugun, natija):               # левое → узел → правое
    if tugun is not None:
        simmetrik(tugun.chap, natija)
        natija.append(tugun.kalit)
        simmetrik(tugun.ong, natija)


def balandlik(tugun):
    if tugun is None:
        return 0
    return 1 + max(balandlik(tugun.chap), balandlik(tugun.ong))


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
natija = []
simmetrik(ildiz, natija)
print(*natija)
print(balandlik(ildiz))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun tugun, int kalit) {
        if (tugun == null) return new Tugun(kalit);         // пустое место — новый лист
        if (kalit < tugun.kalit) tugun.chap = qosh(tugun.chap, kalit);
        else tugun.ong = qosh(tugun.ong, kalit);
        return tugun;
    }

    static void simmetrik(Tugun tugun, StringBuilder natija) {   // левое → узел → правое
        if (tugun == null) return;
        simmetrik(tugun.chap, natija);
        if (natija.length() > 0) natija.append(' ');
        natija.append(tugun.kalit);
        simmetrik(tugun.ong, natija);
    }

    static int balandlik(Tugun tugun) {
        if (tugun == null) return 0;
        return 1 + Math.max(balandlik(tugun.chap), balandlik(tugun.ong));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        StringBuilder natija = new StringBuilder();
        simmetrik(ildiz, natija);
        System.out.println(natija);
        System.out.println(balandlik(ildiz));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* tugun, int kalit) {
    if (tugun == nullptr) return new Tugun{kalit};          // пустое место — новый лист
    if (kalit < tugun->kalit) tugun->chap = qosh(tugun->chap, kalit);
    else tugun->ong = qosh(tugun->ong, kalit);
    return tugun;
}

void simmetrik(Tugun* tugun, vector<int>& natija) {         // левое → узел → правое
    if (tugun == nullptr) return;
    simmetrik(tugun->chap, natija);
    natija.push_back(tugun->kalit);
    simmetrik(tugun->ong, natija);
}

int balandlik(Tugun* tugun) {
    if (tugun == nullptr) return 0;
    return 1 + max(balandlik(tugun->chap), balandlik(tugun->ong));
}

void ochir(Tugun* tugun) {                                  // освобождение обратным обходом
    if (tugun == nullptr) return;
    ochir(tugun->chap);
    ochir(tugun->ong);
    delete tugun;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> natija;
    simmetrik(ildiz, natija);
    for (size_t i = 0; i < natija.size(); i++) cout << (i ? " " : "") << natija[i];
    cout << endl << balandlik(ildiz) << endl;
    ochir(ildiz);
    return 0;
}
```

Для `7` и `50 30 70 20 40 60 80` выводится `20 30 40 50 60 70 80` и `3`: идеально сбалансированное дерево (корень 50, затем 30 и 70, затем четыре листа). А для `5` и `1 2 3 4 5` вместе с отсортированным результатом выводится высота `5` — каждый новый элемент ушёл вправо, и дерево стало списком (*Tugun* — узел, *kalit* — ключ, *qosh* — вставить, *simmetrik* — симметричный обход, *balandlik* — высота). В конце версия на C++ освобождает узлы **обратным** обходом: потомков нужно удалить раньше родителя.

## Разобранный пример 2: прямой, обратный обход и обход по уровням

В первой строке *n*, во второй — *n* различных чисел. Вставьте их в ДДП и выведите дерево тремя способами: прямым, обратным и по уровням.

```python
from collections import deque


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(ildiz, kalit):                     # вставка циклом — без рекурсии
    yangi = Tugun(kalit)
    if ildiz is None:
        return yangi
    joriy = ildiz
    while True:
        if kalit < joriy.kalit:
            if joriy.chap is None:
                joriy.chap = yangi
                return ildiz
            joriy = joriy.chap
        else:
            if joriy.ong is None:
                joriy.ong = yangi
                return ildiz
            joriy = joriy.ong


def togri(t, r):                            # узел → левое → правое
    if t:
        r.append(t.kalit)
        togri(t.chap, r)
        togri(t.ong, r)


def teskari(t, r):                          # левое → правое → узел
    if t:
        teskari(t.chap, r)
        teskari(t.ong, r)
        r.append(t.kalit)


def sathma_sath(ildiz):                     # с помощью очереди
    r = []
    navbat = deque([ildiz] if ildiz else [])
    while navbat:
        t = navbat.popleft()
        r.append(t.kalit)
        if t.chap:
            navbat.append(t.chap)
        if t.ong:
            navbat.append(t.ong)
    return r


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
r = []
togri(ildiz, r)
print(*r)
r = []
teskari(ildiz, r)
print(*r)
print(*sathma_sath(ildiz))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun ildiz, int kalit) {             // вставка циклом — без рекурсии
        Tugun yangi = new Tugun(kalit);
        if (ildiz == null) return yangi;
        Tugun joriy = ildiz;
        while (true) {
            if (kalit < joriy.kalit) {
                if (joriy.chap == null) { joriy.chap = yangi; return ildiz; }
                joriy = joriy.chap;
            } else {
                if (joriy.ong == null) { joriy.ong = yangi; return ildiz; }
                joriy = joriy.ong;
            }
        }
    }

    static void togri(Tugun t, List<Integer> r) {           // узел → левое → правое
        if (t == null) return;
        r.add(t.kalit);
        togri(t.chap, r);
        togri(t.ong, r);
    }

    static void teskari(Tugun t, List<Integer> r) {         // левое → правое → узел
        if (t == null) return;
        teskari(t.chap, r);
        teskari(t.ong, r);
        r.add(t.kalit);
    }

    static List<Integer> sathmaSath(Tugun ildiz) {         // с помощью очереди
        List<Integer> r = new ArrayList<>();
        ArrayDeque<Tugun> navbat = new ArrayDeque<>();
        if (ildiz != null) navbat.add(ildiz);
        while (!navbat.isEmpty()) {
            Tugun t = navbat.poll();
            r.add(t.kalit);
            if (t.chap != null) navbat.add(t.chap);
            if (t.ong != null) navbat.add(t.ong);
        }
        return r;
    }

    static String satr(List<Integer> r) {
        StringBuilder sb = new StringBuilder();
        for (int x : r) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        List<Integer> r = new ArrayList<>();
        togri(ildiz, r);
        System.out.println(satr(r));
        r = new ArrayList<>();
        teskari(ildiz, r);
        System.out.println(satr(r));
        System.out.println(satr(sathmaSath(ildiz)));
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* ildiz, int kalit) {                      // вставка циклом — без рекурсии
    Tugun* yangi = new Tugun{kalit};
    if (ildiz == nullptr) return yangi;
    Tugun* joriy = ildiz;
    while (true) {
        Tugun*& keyingi = kalit < joriy->kalit ? joriy->chap : joriy->ong;
        if (keyingi == nullptr) {
            keyingi = yangi;
            return ildiz;
        }
        joriy = keyingi;
    }
}

void togri(Tugun* t, vector<int>& r) {                      // узел → левое → правое
    if (!t) return;
    r.push_back(t->kalit);
    togri(t->chap, r);
    togri(t->ong, r);
}

void teskari(Tugun* t, vector<int>& r) {                    // левое → правое → узел
    if (!t) return;
    teskari(t->chap, r);
    teskari(t->ong, r);
    r.push_back(t->kalit);
}

vector<int> sathmaSath(Tugun* ildiz) {                      // с помощью очереди
    vector<int> r;
    queue<Tugun*> navbat;
    if (ildiz) navbat.push(ildiz);
    while (!navbat.empty()) {
        Tugun* t = navbat.front();
        navbat.pop();
        r.push_back(t->kalit);
        if (t->chap) navbat.push(t->chap);
        if (t->ong) navbat.push(t->ong);
    }
    return r;
}

void chiqar(const vector<int>& r) {
    for (size_t i = 0; i < r.size(); i++) cout << (i ? " " : "") << r[i];
    cout << endl;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> r;
    togri(ildiz, r);
    chiqar(r);
    r.clear();
    teskari(ildiz, r);
    chiqar(r);
    chiqar(sathmaSath(ildiz));
    return 0;
}
```

Для `7` и `50 30 70 20 40 60 80` выводится:

```text
50 30 20 40 70 60 80
20 40 30 60 80 70 50
50 30 70 20 40 60 80
```

В прямом обходе корень выводится первым, в обратном — последним (*togri*, *teskari* и *sathma_sath* — прямой, обратный и по уровням). Обход по уровням работает не рекурсией, а **очередью**: когда узел выводится, его потомки ставятся в конец очереди, поэтому верхний уровень выходит раньше нижнего. Вставка на этот раз написана циклом — чтобы на отсортированном входе глубокая рекурсия не переполнила стек. В C++ `Tugun*& keyingi` — ссылка на указатель: через неё можно записать в само поле `chap` или `ong`.

## Разобранный пример 3: высота сбалансированного и несбалансированного дерева

Дано *n* (1 ≤ *n* ≤ 10⁵). Постройте из чисел 1, 2, …, *n* два дерева: (1) последовательно вставляя числа в ДДП; (2) из отсортированного массива, **сделав средний элемент корнем** и рекурсивно построив левую и правую половины. Выведите высоты обоих деревьев. Высоту первого дерева вычислите, не строя его, — анализа достаточно, чтобы показать, что это цепочка.

```python
import sys

sys.setrecursionlimit(10000)


def muvozanatli_balandlik(chap, ong):
    """высота дерева из [chap, ong] с серединой в корне."""
    if chap > ong:
        return 0
    orta = (chap + ong) // 2                # средний элемент — корень
    return 1 + max(muvozanatli_balandlik(chap, orta - 1),
                   muvozanatli_balandlik(orta + 1, ong))


n = int(input())
ketma_ket = n                               # каждое число уходит вправо — цепочка
print(ketma_ket, muvozanatli_balandlik(1, n))
```
```java
import java.util.Scanner;

public class Main {
    /** высота дерева из [chap, ong] с серединой в корне. */
    static int muvozanatliBalandlik(int chap, int ong) {
        if (chap > ong) return 0;
        int orta = (chap + ong) / 2;                // средний элемент — корень
        return 1 + Math.max(muvozanatliBalandlik(chap, orta - 1),
                            muvozanatliBalandlik(orta + 1, ong));
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int ketmaKet = n;                           // каждое число уходит вправо — цепочка
        System.out.println(ketmaKet + " " + muvozanatliBalandlik(1, n));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

// высота дерева из [chap, ong] с серединой в корне
int muvozanatliBalandlik(int chap, int ong) {
    if (chap > ong) return 0;
    int orta = (chap + ong) / 2;                    // средний элемент — корень
    return 1 + max(muvozanatliBalandlik(chap, orta - 1),
                   muvozanatliBalandlik(orta + 1, ong));
}

int main() {
    int n;
    cin >> n;
    int ketmaKet = n;                               // каждое число уходит вправо — цепочка
    cout << ketmaKet << " " << muvozanatliBalandlik(1, n) << endl;
    return 0;
}
```

| *n* | Последовательная вставка | Сбалансированное |
|---|---|---|
| 7 | 7 | 3 |
| 1000 | 1000 | 10 |
| 100 000 | 100 000 | 17 |

При последовательной вставке 1, 2, …, *n* каждое новое число больше всех предыдущих — оно присоединяется к последнему узлу справа, и дерево превращается в цепочку: высота *n*, поиск *O*(*n*). Дерево из тех же чисел с серединой в корне имеет высоту ⌈log₂(*n* + 1)⌉: для 100 000 элементов всего 17 (*ketma_ket* — последовательно, *muvozanatli* — сбалансированное). АВЛ и красно-чёрные деревья автоматически поддерживают такую высоту, в каком бы порядке ни приходили числа. При сбалансированном построении глубина рекурсии тоже log *n* — поэтому оно безопасно даже при *n* = 10⁵.

:::warning
**Частые ошибки**

- **Вставка отсортированных данных в обычное ДДП.** Дерево превращается в цепочку; используйте сбалансированное дерево (`TreeMap`, `map`) или случайный порядок.
- **Переполнение стека в рекурсивной вставке и обходе.** В дереве-цепочке глубина равна *n*; нужна вставка циклом или сбалансированное дерево.
- **Нет правила для равных ключей.** Равный ключ должен всегда идти в одну сторону (например, вправо), иначе поиск их не найдёт.
- **Неосвобождённое дерево в C++.** Каждому `new Tugun` нужен `delete`; делайте это обратным обходом.
:::

:::tip
Работая с ДДП, нарисуйте дерево на бумаге и проведите каждый вставляемый элемент от корня, спрашивая «влево или вправо?». Если симметричный обход не отсортирован, во вставке есть ошибка.
:::

## Итоги

- Дерево — связный граф без циклов: корень, узлы, листья; высота — число уровней.
- ДДП: меньшие ключи слева, большие справа; поиск и вставка — *O*(*h*).
- Обходы: прямой, симметричный (в ДДП — отсортированный порядок), обратный и по уровням (очередью).
- Несбалансированное ДДП на отсортированном входе превращается в цепочку (*h* = *n*); АВЛ, красно-чёрные и B-деревья гарантируют *h* = *O*(log *n*).
- Сортировка деревом: вставить элементы, затем симметричный обход — *O*(*n* log *n*) со сбалансированным деревом.

## Проверьте себя

:::check
Q: Какой обход ДДП даёт ключи в отсортированном порядке?
A: Симметричный (inorder): левое → узел → правое.
Q: Какова высота ДДП, если последовательно вставить 1, 2, 3, 4?
A: 4 — каждый элемент уходит вправо, образуется цепочка.
Q: Какая структура нужна для обхода по уровням?
A: Очередь.
Q: Почему поиск в `TreeMap` Java всегда *O*(log *n*)?
A: Это красно-чёрное дерево — после каждой вставки и удаления оно восстанавливает баланс поворотами.
:::

:::link https://visualgo.net/en/bst
VisuAlgo: двоичное дерево поиска
Анимация ДДП и АВЛ-дерева (на английском).
:::
