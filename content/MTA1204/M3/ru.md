---
title: Поиск Фибоначчи. Поиск с помощью хеширования. Внутренняя и внешняя сортировка
summary: Алгоритм поиска Фибоначчи и его сложность; хеш-функция, хеш-таблица, коллизии и метод цепочек, коэффициент заполнения; классификация алгоритмов сортировки, внутренняя и внешняя сортировка, k-путевое слияние упорядоченных серий.
---

## Почему это важно

На прошлой лекции мы увидели, что двоичный поиск в отсортированном массиве занимает *O*(log *n*) шагов. Это очень быстро, но остаются два вопроса. Первый: можно ли искать ещё быстрее? Да — **хеширование** находит ключ в среднем за *O*(1), поэтому на нём построены словари, индексы баз данных и таблицы имён компиляторов. Второй: что, если данные не помещаются в оперативную память? Чтобы отсортировать не миллион, а миллиарды записей, нужна **внешняя сортировка**.

На этой лекции рассмотрим поиск Фибоначчи, хеш-таблицы и общую классификацию алгоритмов сортировки.

:::note
**После этого занятия вы сможете:**

- объяснять идею поиска Фибоначчи и писать его;
- объяснять понятия хеш-функции, хеш-таблицы и коллизии;
- строить простую хеш-таблицу методом цепочек;
- классифицировать алгоритмы сортировки по устойчивости, памяти и сложности;
- объяснять разницу между внутренней и внешней сортировкой и k-путевое слияние.
:::

## Поиск Фибоначчи

**Поиск Фибоначчи** — вариант двоичного поиска «на сложении и вычитании»: он делит отрезок не посередине, а в отношении соседних чисел Фибоначчи. Находится наименьшее число Фибоначчи *F*(*k*) ≥ *n*, и на каждом шаге проверяется позиция `смещение + F(k − 2)`; в зависимости от ответа отрезок сокращается до *F*(*k* − 1) или *F*(*k* − 2).

| Особенность | Двоичный поиск | Поиск Фибоначчи |
|---|---|---|
| Точка деления | середина (`/ 2`) | отношение Фибоначчи (≈ 0,618) |
| Операции | деление | только сложение и вычитание |
| Сложность | *O*(log *n*) | *O*(log *n*) |
| Когда полезен | в обычных случаях | на устройствах, где деление дорого; в памяти с последовательным чтением (обращения к близким позициям) |

## Поиск с помощью хеширования

**Хеш-функция** превращает ключ (число, строку) в индекс таблицы: *h*(*ключ*) ∈ {0, …, *M* − 1}. **Хеш-таблица** — массив из *M* «корзин»; ключ записывается в свою корзину и потом ищется именно в ней.

Распространённый хеш для строк — **полиномиальный** (схема Горнера):

```text
h = 0
для каждого символа c: h = (h · 31 + код(c)) mod M
```

Когда два разных ключа попадают в одну корзину, это называется **коллизией**. Способы их разрешения:

| Способ | Идея |
|---|---|
| **Метод цепочек** | каждая корзина — список; столкнувшиеся ключи добавляются в этот список |
| **Открытая адресация** | если корзина занята, ищется следующая свободная (линейное или квадратичное пробирование) |

**Коэффициент заполнения** α = *n* / *M* (число ключей / число корзин). В методе цепочек среднее время поиска — *O*(1 + α). Если α становится слишком большим (например, больше 0,75), таблицу увеличивают и ключи перераспределяют. В худшем случае (все ключи в одной корзине) поиск превращается в *O*(*n*) — поэтому хорошая хеш-функция должна распределять ключи равномерно.

`dict`/`set` в Python, `HashMap`/`HashSet` в Java и `unordered_map`/`unordered_set` в C++ — это именно хеш-таблицы.

## Классификация алгоритмов сортировки

| Алгоритм | В среднем | В худшем | Доп. память | Устойчив |
|---|---|---|---|---|
| Пузырьком | *O*(*n*²) | *O*(*n*²) | *O*(1) | да |
| Выбором | *O*(*n*²) | *O*(*n*²) | *O*(1) | нет |
| Вставками | *O*(*n*²) | *O*(*n*²) | *O*(1) | да |
| Шелла | ≈ *O*(*n*^1,3) | *O*(*n*²) | *O*(1) | нет |
| Слиянием | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(*n*) | да |
| Пирамидальная | *O*(*n* log *n*) | *O*(*n* log *n*) | *O*(1) | нет |
| Быстрая (Хоара) | *O*(*n* log *n*) | *O*(*n*²) | *O*(log *n*) | нет |

Любая сортировка, основанная на сравнениях, в худшем случае требует не менее Ω(*n* log *n*) сравнений — это доказанная нижняя граница.

## Внутренняя и внешняя сортировка

**Внутренняя сортировка** — все данные находятся в оперативной памяти (RAM); таковы алгоритмы выше. **Внешняя сортировка** — данные на диске и не помещаются в память (например, файл журнала 100 ГБ при 8 ГБ RAM). Стандартный способ — **внешняя сортировка слиянием**:

1. Читать файл кусками, помещающимися в память, сортировать каждый кусок внутренней сортировкой и записывать в отдельный файл («упорядоченная серия»).
2. **k-путевое слияние** всех серий одновременно: взять первый элемент каждой серии, записать в результат наименьший и взять следующий из той же серии. Чтобы быстро находить наименьший, используется **куча** (heap, очередь с приоритетом) — каждый шаг *O*(log *k*).

Во внешней сортировке основные затраты — обмен с диском, поэтому алгоритм рассчитан на **последовательное** и как можно более редкое чтение данных.

## Разобранный пример 1: поиск Фибоначчи

В первой строке *n*, во второй — *n* чисел по возрастанию, в третьей — *x*. Выведите позицию *x* (с 1; если нет — −1) и число проверок.

```python
def fibonachchi_izlash(a, x):
    n = len(a)
    f2, f1 = 0, 1                       # F(k-2), F(k-1)
    f = f1 + f2                         # F(k)
    while f < n:
        f2, f1 = f1, f
        f = f1 + f2
    siljish = -1
    qadam = 0
    while f > 1:
        i = min(siljish + f2, n - 1)
        qadam += 1
        if a[i] < x:                    # правая часть длиной F(k-1)
            f, f1 = f1, f2
            f2 = f - f1
            siljish = i
        elif a[i] > x:                  # левая часть длиной F(k-2)
            f, f1 = f2, f1 - f2
            f2 = f - f1
        else:
            return i + 1, qadam
    if f1 == 1 and siljish + 1 < n:     # мог остаться один элемент
        qadam += 1
        if a[siljish + 1] == x:
            return siljish + 2, qadam
    return -1, qadam


n = int(input())
a = list(map(int, input().split()))
x = int(input())
print(*fibonachchi_izlash(a, x))
```
```java
import java.util.Scanner;

public class Main {
    // результат: {позиция (с 1 или -1), шаги}
    static int[] fibonachchiIzlash(int[] a, int x) {
        int n = a.length;
        int f2 = 0, f1 = 1;                 // F(k-2), F(k-1)
        int f = f1 + f2;                    // F(k)
        while (f < n) {
            f2 = f1;
            f1 = f;
            f = f1 + f2;
        }
        int siljish = -1, qadam = 0;
        while (f > 1) {
            int i = Math.min(siljish + f2, n - 1);
            qadam++;
            if (a[i] < x) {                 // правая часть длиной F(k-1)
                f = f1;
                f1 = f2;
                f2 = f - f1;
                siljish = i;
            } else if (a[i] > x) {          // левая часть длиной F(k-2)
                f = f2;
                f1 = f1 - f2;
                f2 = f - f1;
            } else {
                return new int[] {i + 1, qadam};
            }
        }
        if (f1 == 1 && siljish + 1 < n) {   // мог остаться один элемент
            qadam++;
            if (a[siljish + 1] == x) return new int[] {siljish + 2, qadam};
        }
        return new int[] {-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int[] r = fibonachchiIzlash(a, in.nextInt());
        System.out.println(r[0] + " " + r[1]);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

// результат: {позиция (с 1 или -1), шаги}
pair<int, int> fibonachchiIzlash(const vector<int>& a, int x) {
    int n = a.size();
    int f2 = 0, f1 = 1;                     // F(k-2), F(k-1)
    int f = f1 + f2;                        // F(k)
    while (f < n) {
        f2 = f1;
        f1 = f;
        f = f1 + f2;
    }
    int siljish = -1, qadam = 0;
    while (f > 1) {
        int i = min(siljish + f2, n - 1);
        qadam++;
        if (a[i] < x) {                     // правая часть длиной F(k-1)
            f = f1;
            f1 = f2;
            f2 = f - f1;
            siljish = i;
        } else if (a[i] > x) {              // левая часть длиной F(k-2)
            f = f2;
            f1 = f1 - f2;
            f2 = f - f1;
        } else {
            return {i + 1, qadam};
        }
    }
    if (f1 == 1 && siljish + 1 < n) {       // мог остаться один элемент
        qadam++;
        if (a[siljish + 1] == x) return {siljish + 2, qadam};
    }
    return {-1, qadam};
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    int x;
    cin >> x;
    auto [orin, qadam] = fibonachchiIzlash(a, x);
    cout << orin << " " << qadam << endl;
    return 0;
}
```

Для массива из 11 элементов `10 22 35 40 45 50 80 82 85 90 100`: *x* = 85 → `9 4`, *x* = 45 → `5 1` (найден при первой проверке: *F*(7) = 13 ≥ 11, первая позиция 0 + *F*(5) = 5), *x* = 23 → `-1 4`. В коде вообще нет деления — только сложение и вычитание (*siljish* — смещение, *qadam* — шаг). Последний `if` проверяет единственный элемент, который может остаться на отрезке.

## Разобранный пример 2: хеш-таблица методом цепочек

В первой строке слова (ключи), во второй — слова-запросы. Постройте хеш-таблицу из *M* = 7 корзин (полиномиальный хеш, основание 31; повторное слово не добавляется). Выведите длину каждой корзины, затем для каждого запроса `есть` или `нет` и число сравнений в корзине.

```python
M = 7


def xesh(soz):
    h = 0
    for c in soz:
        h = (h * 31 + ord(c)) % M       # схема Горнера
    return h


jadval = [[] for _ in range(M)]         # каждая корзина — список (цепочка)
for soz in input().split():
    savat = jadval[xesh(soz)]
    if soz not in savat:
        savat.append(soz)

print(*[len(savat) for savat in jadval])
for soz in input().split():
    taqqoslash = 0
    topildi = False
    for s in jadval[xesh(soz)]:         # просматривается только одна корзина
        taqqoslash += 1
        if s == soz:
            topildi = True
            break
    print(soz, "есть" if topildi else "нет", taqqoslash)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final int M = 7;

    static int xesh(String soz) {
        int h = 0;
        for (char c : soz.toCharArray()) h = (h * 31 + c) % M;   // схема Горнера
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<List<String>> jadval = new ArrayList<>();          // каждая корзина — список (цепочка)
        for (int i = 0; i < M; i++) jadval.add(new ArrayList<>());
        for (String soz : in.nextLine().trim().split("\\s+")) {
            List<String> savat = jadval.get(xesh(soz));
            if (!savat.contains(soz)) savat.add(soz);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < M; i++) {
            if (i > 0) sb.append(' ');
            sb.append(jadval.get(i).size());
        }
        System.out.println(sb);

        for (String soz : in.nextLine().trim().split("\\s+")) {
            int taqqoslash = 0;
            boolean topildi = false;
            for (String s : jadval.get(xesh(soz))) {           // просматривается только одна корзина
                taqqoslash++;
                if (s.equals(soz)) {
                    topildi = true;
                    break;
                }
            }
            System.out.println(soz + " " + (topildi ? "есть" : "нет") + " " + taqqoslash);
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <list>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const int M = 7;

int xesh(const string& soz) {
    int h = 0;
    for (unsigned char c : soz) h = (h * 31 + c) % M;      // схема Горнера
    return h;
}

int main() {
    vector<list<string>> jadval(M);                         // каждая корзина — список (цепочка)
    string satr, soz;
    getline(cin, satr);
    istringstream kalitlar(satr);
    while (kalitlar >> soz) {
        auto& savat = jadval[xesh(soz)];
        if (find(savat.begin(), savat.end(), soz) == savat.end()) savat.push_back(soz);
    }

    for (int i = 0; i < M; i++) cout << (i ? " " : "") << jadval[i].size();
    cout << endl;

    getline(cin, satr);
    istringstream sorovlar(satr);
    while (sorovlar >> soz) {
        int taqqoslash = 0;
        bool topildi = false;
        for (const string& s : jadval[xesh(soz)]) {         // просматривается только одна корзина
            taqqoslash++;
            if (s == soz) {
                topildi = true;
                break;
            }
        }
        cout << soz << " " << (topildi ? "есть" : "нет") << " " << taqqoslash << endl;
    }
    return 0;
}
```

Ввод:

```text
olma nok uzum anor shaftoli gilos olcha behi
anor banan behi
```

Вывод:

```text
0 1 1 2 1 2 1
anor есть 1
banan нет 1
behi есть 2
```

8 слов распределились по 7 корзинам: в двух корзинах по 2 слова (коллизии), одна пуста. Каждый запрос смотрит **только одну корзину**, а не все 8 слов (*jadval* — таблица, *savat* — корзина, *taqqoslash* — сравнение). Коэффициент заполнения α = 8/7 ≈ 1,14, поэтому в среднем хватает одного-двух сравнений. `banan` в таблице нет, но в его корзине есть другое слово — поэтому выполнено 1 сравнение. В реальных системах *M* — простое число, и таблица увеличивается по мере заполнения.

## Разобранный пример 3: k-путевое слияние упорядоченных серий

В первой строке *k*, в следующих *k* строках — упорядоченные серии: сначала длина, затем элементы. Слейте все серии в одну упорядоченную последовательность — именно так выглядит второй этап внешней сортировки.

```python
import heapq

k = int(input())
seriyalar = []
for _ in range(k):
    q = list(map(int, input().split()))
    seriyalar.append(q[1:])

uyum = []                                   # (значение, серия, позиция)
for s in range(k):
    if seriyalar[s]:
        heapq.heappush(uyum, (seriyalar[s][0], s, 0))

natija = []
while uyum:
    qiymat, s, i = heapq.heappop(uyum)      # наименьший текущий элемент
    natija.append(qiymat)
    if i + 1 < len(seriyalar[s]):
        heapq.heappush(uyum, (seriyalar[s][i + 1], s, i + 1))
print(*natija)
```
```java
import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        int[][] seriyalar = new int[k][];
        for (int s = 0; s < k; s++) {
            int m = in.nextInt();
            seriyalar[s] = new int[m];
            for (int i = 0; i < m; i++) seriyalar[s][i] = in.nextInt();
        }

        // {значение, серия, позиция}; порядок по значению
        PriorityQueue<int[]> uyum = new PriorityQueue<>((p, q) -> Integer.compare(p[0], q[0]));
        for (int s = 0; s < k; s++) {
            if (seriyalar[s].length > 0) uyum.add(new int[] {seriyalar[s][0], s, 0});
        }

        StringBuilder sb = new StringBuilder();
        while (!uyum.isEmpty()) {
            int[] eng = uyum.poll();                        // наименьший текущий элемент
            if (sb.length() > 0) sb.append(' ');
            sb.append(eng[0]);
            int s = eng[1], i = eng[2];
            if (i + 1 < seriyalar[s].length) uyum.add(new int[] {seriyalar[s][i + 1], s, i + 1});
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <functional>
#include <iostream>
#include <queue>
#include <tuple>
#include <vector>
using namespace std;

int main() {
    int k;
    cin >> k;
    vector<vector<int>> seriyalar(k);
    for (auto& seriya : seriyalar) {
        int m;
        cin >> m;
        seriya.resize(m);
        for (int& v : seriya) cin >> v;
    }

    // (значение, серия, позиция); greater — наименьший наверху
    priority_queue<tuple<int, int, int>, vector<tuple<int, int, int>>, greater<>> uyum;
    for (int s = 0; s < k; s++) {
        if (!seriyalar[s].empty()) uyum.push({seriyalar[s][0], s, 0});
    }

    bool birinchi = true;
    while (!uyum.empty()) {
        auto [qiymat, s, i] = uyum.top();                   // наименьший текущий элемент
        uyum.pop();
        cout << (birinchi ? "" : " ") << qiymat;
        birinchi = false;
        if (i + 1 < (int) seriyalar[s].size()) uyum.push({seriyalar[s][i + 1], s, i + 1});
    }
    cout << endl;
    return 0;
}
```

Ввод:

```text
3
4 1 5 9 14
3 2 3 10
5 0 6 7 8 20
```

Вывод:

```text
0 1 2 3 5 6 7 8 9 10 14 20
```

В куче (*uyum*) всегда не больше *k* элементов — по одному из каждой серии (*seriyalar*). Поэтому память — *O*(*k*), а слияние всего *N* элементов занимает *O*(*N* log *k*). В настоящей внешней сортировке серии — это файлы на диске, и из каждого одновременно читается лишь небольшой буфер, но алгоритм именно такой.

:::warning
**Частые ошибки**

- **Плохая хеш-функция.** Например, хеш только по первой букве — все слова на «a» попадут в одну корзину, и поиск станет *O*(*n*).
- **Выход хеша за модуль.** Если не брать `% M` на каждом шаге `h * 31 + c`, число переполнится (Java, C++).
- **Ожидание порядка от хеш-таблицы.** Хеш размещает ключи без порядка; если нужен отсортированный вывод, сортируйте отдельно.
- **Нет проверки границы в поиске Фибоначчи.** `смещение + f2` может выйти за конец массива — обязательно `min(…, n − 1)`.
:::

:::tip
Чтобы проверить, насколько хороша ваша хеш-функция, вставьте много ключей и выведите длины корзин (как в примере 2). Если длины близки друг к другу, функция распределяет ключи равномерно.
:::

## Итоги

- Поиск Фибоначчи делит отрезок в отношении чисел Фибоначчи, использует только сложение и вычитание и работает за *O*(log *n*).
- Хеш-таблица помещает ключ в корзину с помощью хеш-функции; средний поиск — *O*(1 + α).
- Коллизии разрешаются методом цепочек или открытой адресацией; при росте заполнения таблицу увеличивают.
- Алгоритмы сортировки различаются сложностью, дополнительной памятью и устойчивостью; сортировка сравнениями не может быть быстрее Ω(*n* log *n*).
- Внешняя сортировка: внутренняя сортировка кусков, затем k-путевое слияние с кучей — *O*(*N* log *k*).

## Проверьте себя

:::check
Q: Какой операции избегает поиск Фибоначчи, в отличие от двоичного?
A: Деления: позиции вычисляются только сложением и вычитанием.
Q: Что такое коллизия?
A: Когда хеш-функция даёт одинаковое значение (одну корзину) для двух разных ключей.
Q: Каков коэффициент заполнения хеш-таблицы с 1000 ключей и 500 корзинами?
A: α = 1000 / 500 = 2.
Q: Зачем в k-путевом слиянии внешней сортировки используется куча?
A: Чтобы находить наименьший из текущих элементов *k* серий за *O*(log *k*).
Q: Какие сортировки устойчивы?
A: Пузырьком, вставками и слиянием.
:::

:::link https://en.wikipedia.org/wiki/Hash_table
Хеш-таблица
Хеш-функции, разрешение коллизий и коэффициент заполнения (на английском).
:::

:::link https://en.wikipedia.org/wiki/External_sorting
Внешняя сортировка
Способы сортировки больших объёмов данных с помощью диска (на английском).
:::
