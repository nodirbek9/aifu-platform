---
title: Работа с функциями. Описание функции. Функция, возвращающая значение. Функция-процедура. Перегрузка функций
summary: Описание функции (сигнатура и прототип), функция, возвращающая значение, и процедура, возврат нескольких результатов (кортеж, ссылки, record), перегрузка функций и параметры по умолчанию, разбиение задачи на функции.
---

## Почему это важно

В первом семестре мы научились объявлять функцию, вызывать её и возвращать из неё значение. На практике вопросы звучат так: что, если функция должна вернуть **два** результата? Обязательно ли придумывать разные имена функциям, которые делают одно и то же с разными параметрами? Как разбить большую задачу на маленькие функции, которые можно проверить?

На этом практическом занятии тренируем навык **проектирования** функций: точно записывать описание функции, отличать процедуру от функции, возвращающей значение, возвращать несколько результатов и перегружать функции.

:::note
**После этого занятия вы сможете:**

- точно записывать описание функции (имя, параметры, тип результата);
- уместно использовать функции, возвращающие значение, и процедуры;
- возвращать из функции несколько результатов на трёх языках;
- перегружать функции в Java и C++, а в Python добиваться того же параметрами по умолчанию;
- разбивать задачу на маленькие функции, вызывающие друг друга.
:::

## Описание функции

**Описание** функции (её сигнатура) — всё, что нужно знать, чтобы ею пользоваться: имя, параметры и их типы, тип возвращаемого значения. Тело — «как она работает» — вызывающему не важно.

| Язык | Пример описания |
|---|---|
| C++ (прототип) | `double ortacha(const vector<int>& a);` |
| Java | `static double ortacha(int[] a)` |
| Python | `def ortacha(a: list[int]) -> float:` (аннотации типов необязательны) |

Хорошая функция делает **одно дело**, и её имя говорит, какое: `ortacha` (среднее), `tubmi` (простое ли), `chiqar_jadval` (вывести таблицу). Если описание функции требует союза «и» («читает **и** сортирует **и** выводит»), её надо разбить.

## Функция, возвращающая значение, и процедура

| | Функция, возвращающая значение | Процедура |
|---|---|---|
| Назначение | вычисляет и **возвращает** результат | **выполняет** действие (выводит, меняет массив) |
| Тип результата | `int`, `double`, `bool`, ... | `void` (Java, C++); в Python — `None` |
| Вызов | внутри выражения: `y = kvadrat(x) + 1` | отдельной командой: `chiqar(a)` |

Правило: вычисляющая функция **не выводит**, выводящая процедура **не вычисляет**. Тогда вычисляющую функцию можно использовать в другом месте и тестировать.

## Возврат нескольких результатов

`return` возвращает только одно значение. Если нужно несколько результатов:

| Язык | Способ | Пример |
|---|---|---|
| Python | вернуть кортеж | `return kichik, katta` и `a, b = f(x)` |
| Java | небольшой класс или `record` | `record Natija(int kichik, int katta)` |
| C++ | параметры-ссылки | `void f(..., int& kichik, int& katta)` |
| C++ | вернуть `pair` / `struct` | `return {kichik, katta};` |

В C++ `int&` — параметр-ссылка: функция напрямую меняет переменную вызывающего. Чтобы передавать большие объекты (вектор, строку) **без копирования**, пишут `const vector<int>&` — объект читается, но не меняется.

## Перегрузка функций

**Перегрузка** (overloading) — несколько функций с одинаковым именем, но разными параметрами (по количеству или типу). Компилятор выбирает нужную по аргументам вызова:

```text
yuza(r)          → круг
yuza(a, b)       → прямоугольник
yuza(a, b, c)    → треугольник (формула Герона)
```

- **Java и C++** поддерживают перегрузку напрямую. Функции, различающиеся только типом результата, перегрузить нельзя.
- **Python** перегрузку не поддерживает: второй `def` с тем же именем заменяет первый. Вместо неё используют **параметры по умолчанию** (`def f(a, b=None)`) или переменное число аргументов (`*args`).
- В C++ тоже есть параметры по умолчанию: `void chiqar(int x, char ajratgich = ' ');`

## Разобранный пример 1: перегруженная функция площади

В первой строке *n*, в следующих *n* строках — одно, два или три положительных числа: радиус круга, стороны прямоугольника или стороны треугольника. Для каждой строки выведите площадь с двумя знаками после запятой. В Java и C++ — три функции с одним именем `yuza` (площадь), в Python — одна функция с параметрами по умолчанию.

```python
from math import pi, sqrt


def yuza(a, b=None, c=None):
    if b is None:                       # одно число — круг
        return pi * a * a
    if c is None:                       # два — прямоугольник
        return a * b
    p = (a + b + c) / 2                 # три — формула Герона
    return sqrt(p * (p - a) * (p - b) * (p - c))


n = int(input())
for _ in range(n):
    sonlar = list(map(float, input().split()))
    print(f"{yuza(*sonlar):.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static double yuza(double r) {                       // круг
        return Math.PI * r * r;
    }

    static double yuza(double a, double b) {             // прямоугольник
        return a * b;
    }

    static double yuza(double a, double b, double c) {   // треугольник (Герон)
        double p = (a + b + c) / 2;
        return Math.sqrt(p * (p - a) * (p - b) * (p - c));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            double s;
            if (q.length == 1) s = yuza(Double.parseDouble(q[0]));
            else if (q.length == 2) s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]));
            else s = yuza(Double.parseDouble(q[0]), Double.parseDouble(q[1]), Double.parseDouble(q[2]));
            System.out.printf("%.2f%n", s);
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

double yuza(double r) { return M_PI * r * r; }              // круг

double yuza(double a, double b) { return a * b; }           // прямоугольник

double yuza(double a, double b, double c) {                 // треугольник (Герон)
    double p = (a + b + c) / 2;
    return sqrt(p * (p - a) * (p - b) * (p - c));
}

int main() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream oqim(satr);
        vector<double> q;
        double x;
        while (oqim >> x) q.push_back(x);
        double s;
        if (q.size() == 1) s = yuza(q[0]);
        else if (q.size() == 2) s = yuza(q[0], q[1]);
        else s = yuza(q[0], q[1], q[2]);
        printf("%.2f\n", s);
    }
    return 0;
}
```

Ввод:

```text
3
1
3 4
3 4 5
```

Вывод:

```text
3.14
12.00
6.00
```

Количество чисел в строке заранее неизвестно, поэтому строка читается целиком, а затем разбивается (`split` в Java, `istringstream` — строковый поток — в C++). В Python `yuza(*sonlar)` раскладывает список на отдельные аргументы: `yuza(*[3.0, 4.0])` — это `yuza(3.0, 4.0)`.

## Разобранный пример 2: возврат нескольких результатов

В первой строке *n*, во второй — *n* целых чисел. Функция `statistika` должна вернуть наименьший и наибольший элементы массива и среднее арифметическое; основная программа выводит их в одной строке (среднее — с двумя знаками после запятой).

```python
def statistika(a):
    return min(a), max(a), sum(a) / len(a)      # кортеж


n = int(input())
a = list(map(int, input().split()))
kichik, katta, ortacha = statistika(a)          # распаковка кортежа
print(f"{kichik} {katta} {ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    record Statistika(int kichik, int katta, double ortacha) {}

    static Statistika statistika(int[] a) {
        int kichik = a[0], katta = a[0];
        long yigindi = 0;
        for (int x : a) {
            kichik = Math.min(kichik, x);
            katta = Math.max(katta, x);
            yigindi += x;
        }
        return new Statistika(kichik, katta, (double) yigindi / a.length);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Statistika s = statistika(a);
        System.out.printf("%d %d %.2f%n", s.kichik(), s.katta(), s.ortacha());
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

// результаты возвращаются через параметры-ссылки
void statistika(const vector<int>& a, int& kichik, int& katta, double& ortacha) {
    kichik = katta = a[0];
    long long yigindi = 0;
    for (int x : a) {
        if (x < kichik) kichik = x;
        if (x > katta) katta = x;
        yigindi += x;
    }
    ortacha = (double) yigindi / a.size();
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int kichik, katta;
    double ortacha;
    statistika(a, kichik, katta, ortacha);
    printf("%d %d %.2f\n", kichik, katta, ortacha);
    return 0;
}
```

При вводе `5` и `4 -2 9 7 1` выводится `-2 9 3.80`. Во всех трёх языках функция **вычисляет**, а выводит основная программа (*kichik* — наименьший, *katta* — наибольший, *ortacha* — среднее). В C++ `const vector<int>& a` — массив не копируется, и функция не может его изменить; `int& kichik`, наоборот, записывает в переменную вызывающего. `(double) yigindi` в Java — приведение типа, чтобы избежать целочисленного деления.

## Разобранный пример 3: разбиение задачи на функции

**Совершенное число** равно сумме своих делителей, кроме него самого (6 = 1 + 2 + 3). В первой строке *a* и *b*. Выведите в одной строке все совершенные числа на отрезке [*a*, *b*]; если их нет, выведите `-`.

```python
def boluvchilar_yigindisi(n):
    """Сумма делителей n, кроме самого n."""
    if n == 1:
        return 0
    s = 1
    d = 2
    while d * d <= n:
        if n % d == 0:
            s += d
            if d != n // d:
                s += n // d
        d += 1
    return s


def mukammalmi(n):
    return boluvchilar_yigindisi(n) == n


a, b = map(int, input().split())
topilgan = [n for n in range(a, b + 1) if mukammalmi(n)]
print(*topilgan if topilgan else "-")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** Сумма делителей n, кроме самого n. */
    static long boluvchilarYigindisi(long n) {
        if (n == 1) return 0;
        long s = 1;
        for (long d = 2; d * d <= n; d++) {
            if (n % d == 0) {
                s += d;
                if (d != n / d) s += n / d;
            }
        }
        return s;
    }

    static boolean mukammalmi(long n) {
        return boluvchilarYigindisi(n) == n;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long a = in.nextLong(), b = in.nextLong();
        List<String> topilgan = new ArrayList<>();
        for (long n = a; n <= b; n++) {
            if (mukammalmi(n)) topilgan.add(String.valueOf(n));
        }
        System.out.println(topilgan.isEmpty() ? "-" : String.join(" ", topilgan));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

// сумма делителей n, кроме самого n
long long boluvchilarYigindisi(long long n) {
    if (n == 1) return 0;
    long long s = 1;
    for (long long d = 2; d * d <= n; d++) {
        if (n % d == 0) {
            s += d;
            if (d != n / d) s += n / d;
        }
    }
    return s;
}

bool mukammalmi(long long n) {
    return boluvchilarYigindisi(n) == n;
}

int main() {
    long long a, b;
    cin >> a >> b;
    vector<long long> topilgan;
    for (long long n = a; n <= b; n++) {
        if (mukammalmi(n)) topilgan.push_back(n);
    }
    if (topilgan.empty()) cout << "-";
    for (size_t i = 0; i < topilgan.size(); i++) {
        if (i > 0) cout << ' ';
        cout << topilgan[i];
    }
    cout << endl;
    return 0;
}
```

При вводе `1 10000` выводится `6 28 496 8128`; для `30 400` — `-`. Программа состоит из трёх слоёв: `boluvchilar_yigindisi` (сумма делителей) только вычисляет, `mukammalmi` (совершенное ли) отвечает «да/нет», а основная программа читает и выводит. Делители ищутся до √*n*: если *d* — делитель, то и *n* / *d* — делитель; условие `d != n / d` не даёт дважды прибавить корень у полных квадратов (например, 16 = 4 · 4).

:::warning
**Частые ошибки**

- **Вывод внутри вычисляющей функции.** При `def ortacha(a): print(...)` результат нельзя использовать в другом вычислении; делайте `return`.
- **Попытка перегрузки в Python.** Второй `def yuza(a, b)` заменяет первый `def yuza(r)`; используйте параметры по умолчанию.
- **«Результирующий» параметр без `&` в C++.** `void f(int kichik)` меняет копию, переменная вызывающего не меняется.
- **Изменяемое значение по умолчанию в Python.** При `def f(a, royxat=[])` список общий для всех вызовов; используйте `None` и создавайте новый список внутри.
:::

:::tip
Прежде чем писать функцию, запишите её описание и 2–3 примера вызова (вход → ожидаемый результат). Например: `mukammalmi(6) → True`, `mukammalmi(8) → False`. Когда функция готова, сначала проверьте её на этих примерах.
:::

## Итоги

- Описание функции — имя, параметры и тип результата; хорошая функция делает одно дело.
- Функция, возвращающая значение, вычисляет, процедура (`void`) действует; разделяйте вычисление и вывод.
- Несколько результатов: Python — кортеж, Java — `record`, C++ — параметры-ссылки или `pair`/`struct`.
- Java и C++ перегружают функции по количеству и типам параметров; в Python используют параметры по умолчанию.
- Большая задача разбивается на маленькие функции, вызывающие друг друга.

## Проверьте себя

:::check
Q: Что входит в описание функции?
A: Имя, параметры (и их типы) и тип возвращаемого значения.
Q: Чем вызов `void`-функции отличается от вызова функции, возвращающей значение?
A: Функция, возвращающая значение, используется внутри выражения (`y = f(x) + 1`), а `void`-функция вызывается отдельной командой.
Q: Можно ли в Java объявить вместе `int f(int x)` и `double f(int x)`?
A: Нет: функции с одинаковыми параметрами, различающиеся только типом результата, не перегружаются.
Q: Чему равна переменная вызывающего в C++ после вызова `void f(int& x) { x = 5; }`?
A: 5 — `x` является ссылкой, то есть самой переменной вызывающего.
Q: Как вернуть из функции Python два значения?
A: Кортежем: `return a, b`, а при вызове `x, y = f()`.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#default-argument-values
Python: значения аргументов по умолчанию
Официальное руководство по параметрам по умолчанию и `*args` (на английском).
:::

:::link https://en.cppreference.com/w/cpp/language/overload_resolution
C++: разрешение перегрузки
Как компилятор выбирает среди перегруженных функций (на английском).
:::
