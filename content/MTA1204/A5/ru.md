---
title: Сортировка пузырьком и сортировка Хоара. Параллельные алгоритмы сортировки (практическое занятие)
summary: Число проходов сортировки пузырьком и проблема «черепах», шейкерная сортировка; поиск k-го наименьшего элемента разбиением Хоара (quickselect); проверка сортирующей сети по принципу 0-1.
---

## Почему это важно

На лекции мы рассмотрели сортировку пузырьком, быструю сортировку и сортирующие сети. На практике идеи этих алгоритмов встречаются в неожиданных местах: если гонять пузырёк в обе стороны, он заметно ускоряется, а разбиение Хоара находит медиану или лучших *k* студентов, не сортируя весь массив. А чтобы проверить правильность сортирующей сети, достаточно перебрать не все возможные входы, а только входы из 0 и 1.

:::note
**После этого занятия вы сможете:**

- измерять число проходов сортировки пузырьком и объяснять проблему «черепах»;
- писать шейкерную сортировку и показывать её преимущество;
- находить *k*-й наименьший элемент в среднем за *O*(*n*) разбиением Хоара;
- проверять сортирующую сеть с помощью принципа 0-1.
:::

## Кролики и черепахи

В сортировке пузырьком большой элемент за один проход «пролетает» в конец — это **кролик**. Маленький же элемент за проход сдвигается влево лишь на одну позицию — это **черепаха**. Один маленький элемент в конце массива (`2 3 4 5 6 7 1`) заставляет пузырёк сделать почти *n* проходов.

**Шейкерная сортировка** (cocktail sort) чередует проходы слева направо и справа налево: при обратном проходе и черепахи за один проход добираются до начала. В худшем случае сложность по-прежнему *O*(*n*²), но на «почти отсортированных» массивах она гораздо быстрее пузырька.

## Задача выбора и quickselect

**Задача выбора**: найти *k*-й наименьший элемент массива (*k* = *n*/2 — медиана). Полная сортировка — *O*(*n* log *n*). Алгоритм **quickselect** Хоара после разбиения продолжает только в **одной** части — той, куда попала *k*-я позиция:

| | Быстрая сортировка | Quickselect |
|---|---|---|
| После разбиения | обе части сортируются рекурсивно | рассматривается только одна часть |
| Среднее время | *O*(*n* log *n*) | *O*(*n*) (*n* + *n*/2 + *n*/4 + … ≈ 2*n*) |
| Худшее | *O*(*n*²) | *O*(*n*²) |

На этой идее основаны `nth_element` в C++, `heapq.nsmallest` в Python и быстрое вычисление медианы в статистике.

## Принцип 0-1

Чтобы доказать правильность сортирующей сети, есть **принцип 0-1** (Кнут): если сеть правильно сортирует **все** 2ⁿ входов из 0 и 1, то она правильно сортирует любую последовательность чисел. Для *n* = 16 это 65 536 проверок — несравнимо проще, чем перебрать все 16! ≈ 2 · 10¹³ порядков.

## Разобранный пример 1: проходы пузырька и шейкерной сортировки

В первой строке *n*, во второй — *n* чисел. Выведите число проходов улучшенной сортировки пузырьком (останавливается после прохода без обменов) и шейкерной сортировки.

```python
def pufaksimon(asl):
    a = asl[:]
    otish = 0
    while True:
        otish += 1
        almashdi = False
        for i in range(len(a) - otish):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        if not almashdi:                # проход без обменов — отсортирован
            return otish


def kokteyl(asl):
    a = asl[:]
    otish = 0
    chap, ong = 0, len(a) - 1
    while chap < ong:
        otish += 1                      # слева направо: кролики
        almashdi = False
        for i in range(chap, ong):
            if a[i] > a[i + 1]:
                a[i], a[i + 1] = a[i + 1], a[i]
                almashdi = True
        ong -= 1
        if not almashdi:
            break
        otish += 1                      # справа налево: черепахи
        almashdi = False
        for i in range(ong, chap, -1):
            if a[i - 1] > a[i]:
                a[i - 1], a[i] = a[i], a[i - 1]
                almashdi = True
        chap += 1
        if not almashdi:
            break
    return otish


n = int(input())
a = list(map(int, input().split()))
print(pufaksimon(a), kokteyl(a))
```
```java
import java.util.Scanner;

public class Main {
    static int pufaksimon(int[] asl) {
        int[] a = asl.clone();
        int otish = 0;
        while (true) {
            otish++;
            boolean almashdi = false;
            for (int i = 0; i < a.length - otish; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            if (!almashdi) return otish;            // проход без обменов — отсортирован
        }
    }

    static int kokteyl(int[] asl) {
        int[] a = asl.clone();
        int otish = 0, chap = 0, ong = a.length - 1;
        while (chap < ong) {
            otish++;                                // слева направо: кролики
            boolean almashdi = false;
            for (int i = chap; i < ong; i++) {
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            ong--;
            if (!almashdi) break;
            otish++;                                // справа налево: черепахи
            almashdi = false;
            for (int i = ong; i > chap; i--) {
                if (a[i - 1] > a[i]) {
                    int t = a[i - 1]; a[i - 1] = a[i]; a[i] = t;
                    almashdi = true;
                }
            }
            chap++;
            if (!almashdi) break;
        }
        return otish;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        System.out.println(pufaksimon(a) + " " + kokteyl(a));
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int pufaksimon(vector<int> a) {                     // a — копия
    int otish = 0;
    while (true) {
        otish++;
        bool almashdi = false;
        for (int i = 0; i < (int) a.size() - otish; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        if (!almashdi) return otish;                // проход без обменов — отсортирован
    }
}

int kokteyl(vector<int> a) {
    int otish = 0, chap = 0, ong = (int) a.size() - 1;
    while (chap < ong) {
        otish++;                                    // слева направо: кролики
        bool almashdi = false;
        for (int i = chap; i < ong; i++) {
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        ong--;
        if (!almashdi) break;
        otish++;                                    // справа налево: черепахи
        almashdi = false;
        for (int i = ong; i > chap; i--) {
            if (a[i - 1] > a[i]) {
                swap(a[i - 1], a[i]);
                almashdi = true;
            }
        }
        chap++;
        if (!almashdi) break;
    }
    return otish;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;
    cout << pufaksimon(a) << " " << kokteyl(a) << endl;
    return 0;
}
```

| Массив | Пузырёк | Шейкер |
|---|---|---|
| `2 3 4 5 6 7 1` | 7 | 3 |
| `5 4 3 2 1` | 5 | 4 |
| `1 2 3 4` | 1 | 1 |

В первой строке `1` — черепаха: пузырёк сдвигает её на одну позицию за проход и тратит 7 проходов, а первый обратный проход шейкера сразу переносит её в начало (*otish* — проход, *almashdi* — «был обмен»). Для отсортированного массива оба алгоритма останавливаются после одного проверочного прохода.

## Разобранный пример 2: quickselect — k-й наименьший элемент

В первой строке *n* и *k*, во второй — *n* чисел. Не сортируя массив полностью, найдите разбиением Хоара *k*-й наименьший элемент (*k* = 1 — наименьший).

```python
n, k = map(int, input().split())
a = list(map(int, input().split()))
k -= 1                                  # позиция, считая с 0
chap, ong = 0, n - 1
while chap < ong:
    p = a[(chap + ong) // 2]            # разбиение Хоара
    i, j = chap - 1, ong + 1
    while True:
        i += 1
        while a[i] < p:
            i += 1
        j -= 1
        while a[j] > p:
            j -= 1
        if i >= j:
            break
        a[i], a[j] = a[j], a[i]
    if k <= j:                          # продолжаем только в части с k
        ong = j
    else:
        chap = j + 1
print(a[chap])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt() - 1;     // позиция, считая с 0
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int chap = 0, ong = n - 1;
        while (chap < ong) {
            int p = a[(chap + ong) / 2];                // разбиение Хоара
            int i = chap - 1, j = ong + 1;
            while (true) {
                do i++; while (a[i] < p);
                do j--; while (a[j] > p);
                if (i >= j) break;
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
            if (k <= j) ong = j;                        // продолжаем только в части с k
            else chap = j + 1;
        }
        System.out.println(a[chap]);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    k--;                                                // позиция, считая с 0
    vector<int> a(n);
    for (int& v : a) cin >> v;

    int chap = 0, ong = n - 1;
    while (chap < ong) {
        int p = a[(chap + ong) / 2];                    // разбиение Хоара
        int i = chap - 1, j = ong + 1;
        while (true) {
            do i++; while (a[i] < p);
            do j--; while (a[j] > p);
            if (i >= j) break;
            swap(a[i], a[j]);
        }
        if (k <= j) ong = j;                            // продолжаем только в части с k
        else chap = j + 1;
    }
    cout << a[chap] << endl;
    return 0;
}
```

Для `6 3` и `7 10 4 3 20 15` выводится `7` (в отсортированном виде: 3 4 **7** 10 15 20); для `6 1` — `3`. Рекурсии нет — используется цикл, ведь каждый раз остаётся только одна часть. После разбиения Хоара ни один элемент левой части не больше элементов правой, поэтому *k*-й элемент лежит именно в той части, где *k*.

## Разобранный пример 3: проверка сортирующей сети по принципу 0-1

В первой строке *n* (1 ≤ *n* ≤ 16) и число компараторов *m*, в следующих *m* строках — компараторы `i j` (с 0, *i* < *j*). Переберите все 2ⁿ входов из 0 и 1 (в лексикографическом порядке): если сеть сортирует их все — выведите `да`, иначе — первый несортируемый вход.

```python
n, m = map(int, input().split())
tarmoq = [tuple(map(int, input().split())) for _ in range(m)]

javob = "да"
for maska in range(2 ** n):
    kirish = [(maska >> (n - 1 - i)) & 1 for i in range(n)]    # 0-1 последовательность
    a = kirish[:]
    for i, j in tarmoq:
        if a[i] > a[j]:
            a[i], a[j] = a[j], a[i]
    if any(a[t] > a[t + 1] for t in range(n - 1)):            # не отсортировано
        javob = "".join(map(str, kirish))
        break
print(javob)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] tarmoq = new int[m][2];
        for (int[] t : tarmoq) {
            t[0] = in.nextInt();
            t[1] = in.nextInt();
        }

        String javob = "да";
        for (int maska = 0; maska < (1 << n) && javob.equals("да"); maska++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // 0-1 последовательность
            int[] kirish = a.clone();
            for (int[] t : tarmoq) {
                if (a[t[0]] > a[t[1]]) {
                    int x = a[t[0]]; a[t[0]] = a[t[1]]; a[t[1]] = x;
                }
            }
            for (int i = 0; i + 1 < n; i++) {
                if (a[i] > a[i + 1]) {                                     // не отсортировано
                    StringBuilder sb = new StringBuilder();
                    for (int b : kirish) sb.append(b);
                    javob = sb.toString();
                    break;
                }
            }
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> tarmoq(m);
    for (auto& [i, j] : tarmoq) cin >> i >> j;

    for (int maska = 0; maska < (1 << n); maska++) {
        vector<int> a(n);
        for (int i = 0; i < n; i++) a[i] = (maska >> (n - 1 - i)) & 1;   // 0-1 последовательность
        vector<int> kirish = a;
        for (auto [i, j] : tarmoq) {
            if (a[i] > a[j]) swap(a[i], a[j]);
        }
        for (int i = 0; i + 1 < n; i++) {
            if (a[i] > a[i + 1]) {                                     // не отсортировано
                for (int b : kirish) cout << b;
                cout << endl;
                return 0;
            }
        }
    }
    cout << "да" << endl;
    return 0;
}
```

| Сеть (*n* = 4) | Результат |
|---|---|
| (0,1) (2,3) (0,2) (1,3) (1,2) | `да` — оптимальная сеть для 4 элементов |
| (0,1) (2,3) (0,2) (1,3) | `0101` — не хватает последнего компаратора (1,2) |

Во второй сети вход `0101` должен был стать `0011`, но два средних элемента так и не сравнились. Принцип 0-1 гарантирует: раз первая сеть сортирует все 16 входов из 0 и 1, она правильно сортирует и любые 4 числа (*tarmoq* — сеть, *maska* — битовая маска, *kirish* — вход). Брать вход из битов двоичного числа — самый короткий способ перебрать все 0-1 последовательности.

:::warning
**Частые ошибки**

- **Повторная проверка отсортированного хвоста в пузырьке.** После *k*-го прохода последние *k* элементов на местах; внутренний цикл идёт до `n − otish`.
- **Продолжение в обе части в quickselect.** Получится быстрая сортировка за *O*(*n* log *n*); только часть с *k*.
- **Смешение счёта с 1 и с 0 в quickselect.** Сразу превратите входное *k* в `k − 1`.
- **Проверка сети лишь на нескольких случайных входах.** Ошибка может проявиться на редких входах; принцип 0-1 даёт полную проверку.
:::

:::tip
Проверяя quickselect, сравнивайте результат с обычной сортировкой: `sorted(a)[k − 1]`. Если на нескольких случайных массивах ответы совпадают, алгоритм работает.
:::

## Итоги

- В пузырьке маленькие элементы («черепахи») движутся медленно; шейкерная сортировка исправляет это проходами в обе стороны.
- Quickselect после разбиения Хоара продолжает только в одной части и находит *k*-й элемент в среднем за *O*(*n*).
- Принцип 0-1: если сеть сортирует все 2ⁿ входов из 0 и 1, она сортирует любой вход.
- Идеи этих алгоритмов (разбиение, двусторонние проходы, сеть, не зависящая от данных) переиспользуются в других задачах.

## Проверьте себя

:::check
Q: Что такое «черепаха» в сортировке пузырьком?
A: Маленький элемент ближе к концу массива: за проход он сдвигается влево лишь на одну позицию.
Q: Почему quickselect в среднем работает за *O*(*n*)?
A: После каждого разбиения остаётся одна часть: *n* + *n*/2 + *n*/4 + … ≈ 2*n*.
Q: Сколько входов перебирают, чтобы проверить сеть на 8 входов по принципу 0-1?
A: 2⁸ = 256.
Q: Быстрее ли шейкер пузырька для `6 5 4 3 2 1`?
A: Незначительно: на обратном массиве оба — *O*(*n*²); шейкер выигрывает на «почти отсортированных» массивах с множеством черепах.
:::

:::link https://en.wikipedia.org/wiki/Quickselect
Quickselect
Алгоритм выбора Хоара и его сложность (на английском).
:::
