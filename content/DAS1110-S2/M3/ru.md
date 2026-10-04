---
title: Массивы объектов
summary: Хранение объектов в массиве и списке, null-ссылки в Java, изменение объектов в цикле (копия и ссылка в C++), поиск и агрегирование по полю, естественный порядок объектов (__lt__, Comparable, operator<).
---

## Почему это важно

Один объект-студент полезен, но в группе 25 студентов, а в университете — тысячи. Реальные программы обычно работают со **множеством объектов**: товары магазина, книги библиотеки, занятия в расписании. Самый простой способ их хранить — **массив (список) объектов**.

На прошлой лекции мы создали один класс и несколько объектов. Теперь положим объекты в массив и применим к ним все методы первого семестра — поиск, сумму, нахождение наибольшего, сортировку. Попутно увидим, как на практике проявляется важное различие языков: объект — это **копия** или **ссылка**?

:::note
**После этого занятия вы сможете:**

- создавать и заполнять массив (список) объектов на трёх языках;
- объяснять, что создаёт `new Talaba[n]` в Java и почему возникает ошибка `null`;
- правильно изменять объекты внутри цикла (`auto&` в C++);
- искать, суммировать и находить максимум по полю;
- задавать классу естественный порядок и сортировать объекты стандартной функцией.
:::

## Создание массива объектов

| Язык | Фиксированный размер | Растущий список |
|---|---|---|
| Python | — | `talabalar = []`, `talabalar.append(Talaba(...))` |
| Java | `Talaba[] t = new Talaba[n];` | `List<Talaba> t = new ArrayList<>();`, `t.add(new Talaba(...))` |
| C++ | `vector<Talaba> t(n);` (нужен конструктор по умолчанию) | `vector<Talaba> t;`, `t.push_back(Talaba(...))` или `t.emplace_back(...)` |

**Ловушка в Java.** `new Talaba[n]` **не** создаёт *n* объектов — он создаёт *n* пустых ссылок (`null`). Каждому элементу объект присваивается отдельно: `t[i] = new Talaba(...)`. Без этого обращение к `t[0].ism` даёт `NullPointerException`.

**Особенность C++.** `vector<Talaba> t(n);` сразу создаёт *n* объектов, для чего в классе нужен конструктор без параметров (по умолчанию). Если есть только конструктор с параметрами, создайте пустой `vector` и добавляйте через `push_back` или `emplace_back`.

## Обход объектов

Чтобы работать с каждым объектом массива, используют обычный цикл или цикл «для каждого элемента»:

```text
Python:  for t in talabalar: ...
Java:    for (Talaba t : talabalar) { ... }
C++:     for (auto& t : talabalar) { ... }
```

В Python и Java `t` — **ссылка** на объект в массиве, поэтому `t.ball += 5` меняет объект в массиве. В C++ же `for (auto t : talabalar)` берёт **копию** каждого объекта: меняется копия, а массив остаётся прежним. Чтобы изменить объект, пишут `auto&`, только для чтения — `const auto&` (копия тоже не создаётся).

## Поиск и агрегирование по полю

Алгоритмы для массивов из первого семестра работают и на массиве объектов — только сравнивается не сам элемент, а его **поле**:

| Задача | В массиве чисел | В массиве объектов |
|---|---|---|
| Сумма | `s += a[i]` | `s += t[i].narx * t[i].miqdor` |
| Максимум | `a[i] > eng` | `t[i].ball > eng.ball` (хранится лучший **объект**) |
| Поиск | `a[i] == x` | `t[i].ism.equals(x)` (Java), `t[i].ism == x` (Python, C++) |
| Подсчёт | `a[i] > 0` | `t[i].kurs == 2` |

При поиске максимума удобно запоминать не только наибольшее значение, но и **сам объект** (или его индекс): тогда можно вывести и другие его поля.

## Естественный порядок объектов

На прошлой лекции для сортировки мы задавали ключ (`key`) или функцию сравнения. Если у класса есть «обычный» порядок, его можно задать **в самом классе** — тогда сортировка работает без дополнительных параметров:

| Язык | Что пишется в классе | Сортировка |
|---|---|---|
| Python | `def __lt__(self, boshqa): return ...` | `sorted(a)`, `a.sort()` |
| Java | `implements Comparable<Kitob>` и `public int compareTo(Kitob b)` | `Collections.sort(a)`, `Arrays.sort(a)` |
| C++ | `bool operator<(const Kitob& b) const` | `sort(a.begin(), a.end())` |

В Java `compareTo` возвращает отрицательное число («я раньше»), ноль («мы равны») или положительное число; `Integer.compare(x, y)` и `String.compareTo` дают готовое значение.

## Разобранный пример 1: товары на складе

В первой строке *n*, в следующих *n* строках — название товара, цена и количество. Выведите общую стоимость склада (сумму цена × количество) и название товара с самым дорогим **запасом**.

```python
class Mahsulot:
    def __init__(self, nom, narx, miqdor):
        self.nom = nom
        self.narx = narx
        self.miqdor = miqdor

    def qiymat(self):
        return self.narx * self.miqdor


n = int(input())
mahsulotlar = []
for _ in range(n):
    nom, narx, miqdor = input().split()
    mahsulotlar.append(Mahsulot(nom, int(narx), int(miqdor)))

jami = 0
eng = mahsulotlar[0]
for m in mahsulotlar:
    jami += m.qiymat()
    if m.qiymat() > eng.qiymat():
        eng = m

print(jami)
print(eng.nom)
```
```java
import java.util.Scanner;

public class Main {
    static class Mahsulot {
        String nom;
        long narx, miqdor;

        Mahsulot(String nom, long narx, long miqdor) {
            this.nom = nom;
            this.narx = narx;
            this.miqdor = miqdor;
        }

        long qiymat() {
            return narx * miqdor;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Mahsulot[] mahsulotlar = new Mahsulot[n];        // пока n значений null
        for (int i = 0; i < n; i++) {
            mahsulotlar[i] = new Mahsulot(in.next(), in.nextLong(), in.nextLong());
        }

        long jami = 0;
        Mahsulot eng = mahsulotlar[0];
        for (Mahsulot m : mahsulotlar) {
            jami += m.qiymat();
            if (m.qiymat() > eng.qiymat()) eng = m;
        }

        System.out.println(jami);
        System.out.println(eng.nom);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class Mahsulot {
public:
    string nom;
    long long narx, miqdor;

    Mahsulot(string nom, long long narx, long long miqdor) : nom(nom), narx(narx), miqdor(miqdor) {}

    long long qiymat() const { return narx * miqdor; }
};

int main() {
    int n;
    cin >> n;
    vector<Mahsulot> mahsulotlar;
    for (int i = 0; i < n; i++) {
        string nom;
        long long narx, miqdor;
        cin >> nom >> narx >> miqdor;
        mahsulotlar.emplace_back(nom, narx, miqdor);
    }

    long long jami = 0;
    int eng = 0;                                    // индекс самого дорогого запаса
    for (int i = 0; i < n; i++) {
        jami += mahsulotlar[i].qiymat();
        if (mahsulotlar[i].qiymat() > mahsulotlar[eng].qiymat()) eng = i;
    }

    cout << jami << endl << mahsulotlar[eng].nom << endl;
    return 0;
}
```

Ввод:

```text
4
daftar 5000 120
ruchka 2000 300
kitob 45000 10
sumka 150000 5
```

Вывод:

```text
2400000
sumka
```

Стоимости запасов: 600 000, 600 000, 450 000, 750 000. У `daftar` (тетрадь) и `ruchka` (ручка) стоимости равны: будь они наибольшими, строгое условие `>` оставило бы **первый** встреченный (`daftar`), а `>=` — последний (`ruchka`). При равных значениях выбор объекта определяет само условие. В версии на C++ хранится не объект, а его **индекс** — копия не создаётся (*qiymat* — стоимость, *jami* — итог).

## Разобранный пример 2: изменение объектов в цикле

В первой строке *n* и *k*, в следующих *n* строках — имя студента и балл. Каждому студенту с баллом ниже 60 добавьте *k* баллов (но не больше 100). Выведите всех студентов с новыми баллами, затем число сдавших (≥ 60).

```python
class Talaba:
    def __init__(self, ism, ball):
        self.ism = ism
        self.ball = ball


n, k = map(int, input().split())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append(Talaba(ism, int(ball)))

for t in talabalar:                 # t — сам объект из списка
    if t.ball < 60:
        t.ball = min(t.ball + k, 100)

otganlar = 0
for t in talabalar:
    print(t.ism, t.ball)
    if t.ball >= 60:
        otganlar += 1
print(otganlar)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Talaba {
        String ism;
        int ball;

        Talaba(String ism, int ball) {
            this.ism = ism;
            this.ball = ball;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt()));

        for (Talaba t : talabalar) {        // t — ссылка на объект в списке
            if (t.ball < 60) t.ball = Math.min(t.ball + k, 100);
        }

        int otganlar = 0;
        for (Talaba t : talabalar) {
            System.out.println(t.ism + " " + t.ball);
            if (t.ball >= 60) otganlar++;
        }
        System.out.println(otganlar);
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
    int ball;
};

int main() {
    int n, k;
    cin >> n >> k;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;

    for (auto& t : talabalar) {         // без & изменилась бы копия
        if (t.ball < 60) t.ball = min(t.ball + k, 100);
    }

    int otganlar = 0;
    for (const auto& t : talabalar) {
        cout << t.ism << " " << t.ball << endl;
        if (t.ball >= 60) otganlar++;
    }
    cout << otganlar << endl;
    return 0;
}
```

Ввод:

```text
4 15
Aziz 52
Malika 91
Bekzod 40
Dilnoza 95
```

Вывод:

```text
Aziz 67
Malika 91
Bekzod 55
Dilnoza 95
3
```

В C++ `struct` — класс, все члены которого открыты (`public`); он удобен для простых типов-носителей данных. Если в изменяющем цикле написать `auto` вместо `auto&`, программа не сообщит об ошибке, но баллы не изменятся: в выводе будут `Aziz 52`, `Bekzod 40`, а сдавших (*otganlar*) — 2. Такую ошибку трудно найти.

## Разобранный пример 3: естественный порядок книг

В первой строке *n*, в следующих *n* строках — год издания книги и её название (одно слово). Задайте классу `Kitob` (книга) естественный порядок: сначала по году, при равном годе — по названию в алфавитном порядке. Отсортируйте книги стандартной сортировкой и выведите.

```python
class Kitob:
    def __init__(self, yil, nom):
        self.yil = yil
        self.nom = nom

    def __lt__(self, boshqa):           # «стоит ли self раньше другого?»
        return (self.yil, self.nom) < (boshqa.yil, boshqa.nom)


n = int(input())
kitoblar = []
for _ in range(n):
    yil, nom = input().split()
    kitoblar.append(Kitob(int(yil), nom))

kitoblar.sort()
for k in kitoblar:
    print(k.yil, k.nom)
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Kitob implements Comparable<Kitob> {
        int yil;
        String nom;

        Kitob(int yil, String nom) {
            this.yil = yil;
            this.nom = nom;
        }

        @Override
        public int compareTo(Kitob boshqa) {
            if (yil != boshqa.yil) return Integer.compare(yil, boshqa.yil);
            return nom.compareTo(boshqa.nom);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Kitob> kitoblar = new ArrayList<>();
        for (int i = 0; i < n; i++) kitoblar.add(new Kitob(in.nextInt(), in.next()));

        Collections.sort(kitoblar);
        for (Kitob k : kitoblar) System.out.println(k.yil + " " + k.nom);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Kitob {
    int yil;
    string nom;

    bool operator<(const Kitob& boshqa) const {     // «стою ли я раньше другого?»
        if (yil != boshqa.yil) return yil < boshqa.yil;
        return nom < boshqa.nom;
    }
};

int main() {
    int n;
    cin >> n;
    vector<Kitob> kitoblar(n);
    for (auto& k : kitoblar) cin >> k.yil >> k.nom;

    sort(kitoblar.begin(), kitoblar.end());
    for (const auto& k : kitoblar) cout << k.yil << " " << k.nom << endl;
    return 0;
}
```

Ввод:

```text
5
1999 Sarob
1926 Kecha
1999 Alpomish
2005 Dunyo
1926 Mehrob
```

Вывод:

```text
1926 Kecha
1926 Mehrob
1999 Alpomish
1999 Sarob
2005 Dunyo
```

Ни в одном из трёх языков в вызове сортировки не задан ключ: порядок находится в самом классе — в `__lt__`, `compareTo` или `operator<`. В Python сравнение кортежей `(yil, nom)` сравнивает сначала первые элементы, а при равенстве — вторые; это короткая запись порядка по двум ключам.

:::warning
**Частые ошибки**

- **Не созданы объекты после `new Talaba[n]` в Java.** Элементы равны `null`; без `t[i] = new Talaba(...)` будет `NullPointerException`.
- **`auto` вместо `auto&` в C++.** Цикл меняет копию, а массив остаётся прежним — программа молча работает с неверным результатом.
- **Сравнение строк через `==` в Java.** `==` сравнивает ссылки; при поиске по имени используйте `equals`.
- **Не учтены равные значения.** При поиске максимума `>` и `>=` выбирают разные объекты; определите, какой нужен по условию.
:::

:::tip
Работая с массивом объектов, сначала убедитесь, что вы его правильно прочитали: сразу после чтения напишите временный цикл, выводящий все объекты. Большая часть ошибок — именно на этапе чтения.
:::

## Итоги

- Объекты хранят в массиве или растущем списке: Python `list`, Java `ArrayList`, C++ `vector`.
- В Java `new T[n]` создаёт только ссылки `null`; каждый объект создаётся отдельно через `new`.
- В C++ для изменения объектов в цикле пишут `auto&`, для чтения — `const auto&`.
- Алгоритмы для массивов применимы и к объектам — сравниваются поля, хранится лучший объект или его индекс.
- Естественный порядок задаётся в классе через `__lt__`, `compareTo` или `operator<`.

## Проверьте себя

:::check
Q: Сколько объектов `Talaba` существует в Java после `Talaba[] t = new Talaba[3];`?
A: Ни одного: в массиве три ссылки `null`.
Q: В C++ после `for (auto t : v) t.ball = 0;` станут ли баллы в `v` нулевыми?
A: Нет, `t` — копия; нужно писать `for (auto& t : v)`.
Q: Меняет ли в Python `for t in talabalar: t.ball += 1` объекты в списке?
A: Да, `t` — ссылка на сам объект в списке.
Q: Какое значение должен возвращать `compareTo` в Java?
A: Отрицательное, если текущий объект должен стоять раньше, ноль при равенстве, положительное — если позже.
Q: Почему при поиске максимума удобно хранить объект или его индекс?
A: Тогда можно вывести и другие поля лучшего объекта (например, название).
:::

:::link https://docs.python.org/3/howto/sorting.html#odds-and-ends
Python: сортировка и __lt__
О сортировке объектов через `__lt__` (на английском).
:::

:::link https://dev.java/learn/api/collections-framework/
Java: коллекции
Официальный учебный ресурс о `List`, `ArrayList` и `Comparable` (на английском).
:::
