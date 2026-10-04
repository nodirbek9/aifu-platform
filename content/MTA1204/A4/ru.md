---
title: Алгоритмы сортировки. Внутренняя и внешняя сортировка. Пирамидальная сортировка и сортировка Шелла (практическое занятие)
summary: Сортировка слиянием и число её сравнений, сортировка подсчётом без сравнений и её линейное время, упорядочение по нескольким ключам с помощью устойчивой сортировки; выбор алгоритма сортировки под задачу.
---

## Почему это важно

На лекциях мы рассмотрели классификацию алгоритмов сортировки. На практике вопрос конкретен: какой алгоритм лучше всего для моих данных? Ответ зависит от данных: числа элементов, диапазона значений, требования устойчивости, ограничений памяти. Например, чтобы отсортировать 10⁶ студентов по возрасту, не нужно даже *O*(*n* log *n*) — возраст от 0 до 120, поэтому хватит *O*(*n*).

На этом занятии рассмотрим три практических метода: сортировку слиянием (основу внешней сортировки), сортировку подсчётом без сравнений и упорядочение по нескольким ключам с помощью устойчивой сортировки.

:::note
**После этого занятия вы сможете:**

- писать рекурсивную сортировку слиянием и измерять число её сравнений;
- применять сортировку подсчётом при малом диапазоне значений;
- упорядочивать по нескольким ключам последовательными устойчивыми сортировками;
- выбирать алгоритм сортировки по условию задачи.
:::

## Сортировка слиянием

**Сортировка слиянием** (merge sort, Дж. фон Нейман, 1945): массив делится пополам, каждая половина рекурсивно сортируется, затем две отсортированные половины **сливаются** — на каждом шаге в результат записывается меньший из текущих элементов двух половин.

| Особенность | Значение |
|---|---|
| Время (в любом случае) | *O*(*n* log *n*) |
| Дополнительная память | *O*(*n*) |
| Устойчивость | да (при равенстве первым берётся левый элемент) |
| Чтение данных | последовательное — поэтому удобна для внешней сортировки и связанных списков |

## Сортировка подсчётом

Сортировка, основанная на сравнениях, не может быть быстрее Ω(*n* log *n*). Но если значения лежат **в малом диапазоне** (0 … *k*), сравнения вообще не нужны: можно посчитать, сколько раз встречается каждое значение, и записать результат по счётчикам. **Сортировка подсчётом** (counting sort) работает за *O*(*n* + *k*) при памяти *O*(*k*).

| Когда полезна | Когда не подходит |
|---|---|
| возраст, оценка, номер месяца, символ ASCII | очень большой диапазон значений (например, 10⁹) |
| *k* примерно *n* или меньше | вещественные числа, строки |

Идея сортировки подсчётом лежит в основе **поразрядной сортировки** (radix sort): большие числа сортируются по разрядам, каждый раз устойчивой сортировкой подсчётом.

## Сортировка по нескольким ключам

«Сначала по группе, внутри группы — по баллу по убыванию» — такой порядок можно получить двумя способами:

1. **Составной ключ**: одна сортировка, функция сравнения сначала сравнивает группу, при равенстве — балл.
2. **Последовательные устойчивые сортировки**: сначала устойчивая сортировка по **вторичному** ключу (баллу), затем по **основному** (группе). Вторая сортировка сохраняет порядок первой внутри равных групп.

Второй способ — то, как работает «сортировка щелчком по заголовку столбца» в таблицах: последний щёлкнутый столбец становится основным ключом. Правильно это работает только с **устойчивой** сортировкой.

## Разобранный пример 1: сортировка слиянием

В первой строке *n*, во второй — *n* чисел. Отсортируйте их слиянием; выведите отсортированный массив и число сравнений при слиянии.

```python
import sys

sys.setrecursionlimit(10000)
taqqoslash = 0


def birlashtirib_saralash(a):
    global taqqoslash
    if len(a) <= 1:
        return a
    orta = len(a) // 2
    chap = birlashtirib_saralash(a[:orta])
    ong = birlashtirib_saralash(a[orta:])
    natija = []
    i = j = 0
    while i < len(chap) and j < len(ong):
        taqqoslash += 1
        if chap[i] <= ong[j]:           # <= — ради устойчивости
            natija.append(chap[i])
            i += 1
        else:
            natija.append(ong[j])
            j += 1
    natija.extend(chap[i:])             # оставшаяся часть
    natija.extend(ong[j:])
    return natija


n = int(input())
a = list(map(int, input().split()))
print(*birlashtirib_saralash(a))
print(taqqoslash)
```
```java
import java.util.Scanner;

public class Main {
    static long taqqoslash = 0;

    static void saralash(int[] a, int[] vaqtincha, int chap, int ong) {    // [chap, ong)
        if (ong - chap <= 1) return;
        int orta = (chap + ong) / 2;
        saralash(a, vaqtincha, chap, orta);
        saralash(a, vaqtincha, orta, ong);
        int i = chap, j = orta, k = chap;
        while (i < orta && j < ong) {
            taqqoslash++;
            if (a[i] <= a[j]) vaqtincha[k++] = a[i++];      // <= — ради устойчивости
            else vaqtincha[k++] = a[j++];
        }
        while (i < orta) vaqtincha[k++] = a[i++];           // оставшаяся часть
        while (j < ong) vaqtincha[k++] = a[j++];
        for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        saralash(a, new int[n], 0, n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(taqqoslash);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

long long taqqoslash = 0;

void saralash(vector<int>& a, vector<int>& vaqtincha, int chap, int ong) {   // [chap, ong)
    if (ong - chap <= 1) return;
    int orta = (chap + ong) / 2;
    saralash(a, vaqtincha, chap, orta);
    saralash(a, vaqtincha, orta, ong);
    int i = chap, j = orta, k = chap;
    while (i < orta && j < ong) {
        taqqoslash++;
        if (a[i] <= a[j]) vaqtincha[k++] = a[i++];          // <= — ради устойчивости
        else vaqtincha[k++] = a[j++];
    }
    while (i < orta) vaqtincha[k++] = a[i++];               // оставшаяся часть
    while (j < ong) vaqtincha[k++] = a[j++];
    for (k = chap; k < ong; k++) a[k] = vaqtincha[k];
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n), vaqtincha(n);
    for (int& v : a) cin >> v;
    saralash(a, vaqtincha, 0, n);
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl << taqqoslash << endl;
    return 0;
}
```

Для `7` и `38 27 43 3 9 82 10` выводится `3 9 10 27 38 43 82` и `13`. Для отсортированного и обратного массивов из 8 элементов число сравнений тоже 12 — сортировка слиянием почти безразлична к порядку входа (*birlashtirib saralash* — сортировка слиянием, *vaqtincha* — временный массив). В версиях на Java и C++ один массив `vaqtincha` выделяется заранее и используется всеми слияниями — создавать новый массив при каждом вызове было бы медленнее.

## Разобранный пример 2: сортировка подсчётом

В первой строке *n*, во второй — возрасты *n* студентов (от 0 до 120). Отсортируйте возрасты сортировкой подсчётом без сравнений; выведите отсортированный список и самый частый возраст (если таких несколько — наименьший) с его количеством.

```python
n = int(input())
yoshlar = list(map(int, input().split()))

sanoq = [0] * 121                       # сколько раз встречается каждый возраст
for y in yoshlar:
    sanoq[y] += 1

natija = []
for y in range(121):                    # возрасты по возрастанию
    natija.extend([y] * sanoq[y])

eng_kop = max(range(121), key=lambda y: (sanoq[y], -y))
print(*natija)
print(eng_kop, sanoq[eng_kop])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] sanoq = new int[121];                         // сколько раз встречается каждый возраст
        for (int i = 0; i < n; i++) sanoq[in.nextInt()]++;

        StringBuilder sb = new StringBuilder();
        int engKop = 0;
        for (int y = 0; y <= 120; y++) {                    // возрасты по возрастанию
            for (int k = 0; k < sanoq[y]; k++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(y);
            }
            if (sanoq[y] > sanoq[engKop]) engKop = y;
        }
        System.out.println(sb);
        System.out.println(engKop + " " + sanoq[engKop]);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> sanoq(121, 0);                              // сколько раз встречается каждый возраст
    for (int i = 0; i < n; i++) {
        int y;
        cin >> y;
        sanoq[y]++;
    }

    bool birinchi = true;
    int engKop = 0;
    for (int y = 0; y <= 120; y++) {                        // возрасты по возрастанию
        for (int k = 0; k < sanoq[y]; k++) {
            cout << (birinchi ? "" : " ") << y;
            birinchi = false;
        }
        if (sanoq[y] > sanoq[engKop]) engKop = y;
    }
    cout << endl << engKop << " " << sanoq[engKop] << endl;
    return 0;
}
```

Для `8` и `19 21 19 20 22 19 21 18` выводится `18 19 19 19 20 21 21 22` и `19 3`. В алгоритме нет ни одного сравнения (`<`) элементов: каждый возраст прямо служит индексом массива `sanoq` (счётчик). Для 10⁶ студентов это 10⁶ + 121 операция — быстрее любой сортировки сравнениями. Самое частое значение получается бесплатно из тех же счётчиков — дополнительная польза сортировки подсчётом (*yoshlar* — возрасты, *eng_kop* — самый частый).

## Разобранный пример 3: упорядочение по нескольким ключам устойчивой сортировкой

В первой строке *n*, в следующих *n* строках — имя студента, номер группы и балл. Выведите студентов по группе по возрастанию, а внутри группы — по баллу по убыванию; студенты с равным баллом сохраняют порядок ввода. Сделайте это двумя устойчивыми сортировками: сначала по баллу, потом по группе.

```python
n = int(input())
talabalar = []
for _ in range(n):
    ism, guruh, ball = input().split()
    talabalar.append((ism, int(guruh), int(ball)))

talabalar.sort(key=lambda t: -t[2])     # 1) вторичный ключ: балл (по убыванию)
talabalar.sort(key=lambda t: t[1])      # 2) основной ключ: группа — устойчиво!

for ism, guruh, ball in talabalar:
    print(guruh, ism, ball)
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    record Talaba(String ism, int guruh, int ball) {}

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt(), in.nextInt()));

        talabalar.sort(Comparator.comparingInt(Talaba::ball).reversed());   // 1) вторичный ключ: балл (по убыванию)
        talabalar.sort(Comparator.comparingInt(Talaba::guruh));             // 2) основной ключ: группа — устойчиво!

        for (Talaba t : talabalar) System.out.println(t.guruh() + " " + t.ism() + " " + t.ball());
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Talaba {
    string ism;
    int guruh, ball;
};

int main() {
    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.guruh >> t.ball;

    stable_sort(talabalar.begin(), talabalar.end(),          // 1) вторичный ключ: балл (по убыванию)
                [](const Talaba& x, const Talaba& y) { return x.ball > y.ball; });
    stable_sort(talabalar.begin(), talabalar.end(),          // 2) основной ключ: группа — устойчиво!
                [](const Talaba& x, const Talaba& y) { return x.guruh < y.guruh; });

    for (const auto& t : talabalar) cout << t.guruh << " " << t.ism << " " << t.ball << endl;
    return 0;
}
```

Ввод:

```text
6
Aziz 102 85
Malika 101 92
Bekzod 102 92
Dilnoza 101 85
Anvar 102 85
Sardor 101 70
```

Вывод:

```text
101 Malika 92
101 Dilnoza 85
101 Sardor 70
102 Bekzod 92
102 Aziz 85
102 Anvar 85
```

После первой сортировки студенты упорядочены по баллу. Вторая сортировка разделяет группы, но, будучи **устойчивой**, не нарушает порядок баллов внутри каждой группы. У Азиза и Анвара из группы 102 баллы равны (85) — они сохранили порядок ввода. В C++ важно использовать `stable_sort`, а не `sort`: обычный `sort` неустойчив, и результат может оказаться неверным.

:::warning
**Частые ошибки**

- **`<` при слиянии.** При равенстве первым берётся правый элемент, и устойчивость нарушается; пишите `<=`.
- **Сортировка подсчётом на большом диапазоне.** При значениях до 10⁹ массив счётчиков не поместится в память; нужна сортировка сравнениями или поразрядная.
- **Обратный порядок в многоключевой сортировке.** Если сначала отсортировать по **основному** ключу, вторая сортировка его разрушит; сначала вторичный, потом основной.
- **Ожидание устойчивости от `sort` в C++.** Если нужен устойчивый порядок — `stable_sort`.
:::

:::tip
Выбирая алгоритм сортировки, ответьте на три вопроса: помещаются ли данные в память (нет — внешняя сортировка слиянием)? Мал ли диапазон значений (да — сортировка подсчётом)? Важен ли порядок равных элементов (да — устойчивая сортировка)?
:::

## Итоги

- Сортировка слиянием всегда *O*(*n* log *n*), устойчива, требует *O*(*n*) дополнительной памяти и читает данные последовательно.
- Сортировка подсчётом работает без сравнений за *O*(*n* + *k*) — когда диапазон значений мал.
- Порядок по нескольким ключам: устойчивая сортировка сначала по вторичному, затем по основному ключу.
- Алгоритм выбирают по объёму данных, диапазону значений и требованию устойчивости.

## Проверьте себя

:::check
Q: Почему сортировка слиянием удобна для внешней сортировки?
A: Она читает и записывает данные последовательно; произвольный доступ к диску не нужен.
Q: Почему сортировка подсчётом может быть быстрее границы Ω(*n* log *n*)?
A: Она не сравнивает элементы между собой — граница относится только к сортировкам сравнениями.
Q: Если таблицу устойчиво отсортировать сначала по столбцу «имя», затем по «группе», какой ключ основной?
A: «Группа» — последняя сортировка; внутри группы сохраняется порядок по имени.
Q: Какой алгоритм быстрее всего отсортирует 5 · 10⁶ оценок (от 0 до 100)?
A: Сортировка подсчётом: *O*(*n* + 101).
:::

:::link https://en.wikipedia.org/wiki/Counting_sort
Сортировка подсчётом
Алгоритм, его сложность и связь с поразрядной сортировкой (на английском).
:::
