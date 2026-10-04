---
title: Рекурсивная функция. Организация пользовательской библиотеки
summary: Рекурсия над массивом, мемоизация (Фибоначчи), перебор всех вариантов методом возврата, создание собственной библиотеки функций — модуль Python, класс Java, заголовочный файл и пространство имён C++.
---

## Почему это важно

В первом семестре мы разобрали основы рекурсии: базовый случай, рекурсивный шаг и стек вызовов. На практике рекурсия особенно полезна в трёх ситуациях: обработка данных **по частям** (массив, строка), **запоминание результатов**, когда одни и те же вычисления многократно повторяются, и **перебор всех вариантов** (пароли, составление расписания, ходы в игре).

Переносить полезные функции в каждую новую программу неудобно. Их нужно собрать в **библиотеку** и подключать там, где нужно. На этом занятии тренируем и то, и другое.

:::note
**После этого занятия вы сможете:**

- писать рекурсивную функцию над массивом (сумма, максимум, разворот);
- с помощью мемоизации снижать время рекурсии с экспоненциального до линейного;
- перебирать все варианты методом возврата (backtracking);
- собирать свои функции в модуль Python, класс Java или заголовочный файл и пространство имён C++.
:::

## Рекурсия над массивом

Чтобы решить задачу с массивом рекурсивно, сводим её к **такой же задаче для меньшего массива**:

```text
yigindi(a, n) = 0,                          если n = 0
yigindi(a, n) = yigindi(a, n - 1) + a[n-1], иначе
```

Чтобы не копировать массив каждый раз, в функцию передают сам массив и **границы** (`n` или индексы `chap`, `ong` — левый, правый). Разворот устроен так же: меняем местами два крайних элемента и рекурсивно разворачиваем внутреннюю часть; `chap >= ong` — базовый случай.

## Мемоизация

Числа Фибоначчи: *F*(0) = 0, *F*(1) = 1, *F*(*n*) = *F*(*n* − 1) + *F*(*n* − 2). Прямая рекурсия очень медленная:

| *n* | Вызовов в обычной рекурсии | С мемоизацией |
|---|---|---|
| 10 | 177 | 19 |
| 30 | 2 692 537 | 59 |
| 50 | ~4 · 10¹⁰ (несколько минут) | 99 |

Причина — одни и те же значения вычисляются снова и снова: внутри *F*(50) значение *F*(48) считается дважды, *F*(47) — трижды и т. д. **Мемоизация** — записывать каждый вычисленный результат в таблицу и в следующий раз брать его из таблицы. Тогда каждое *F*(*k*) вычисляется только один раз.

| Язык | Таблица мемоизации |
|---|---|
| Python | словарь или декоратор `@functools.lru_cache` (`@cache`) |
| Java | `long[] memo = new long[n + 1]` или `HashMap` |
| C++ | `vector<long long> memo(n + 1, -1)` |

## Метод возврата (backtracking)

Чтобы получить все варианты, ответ строится **шаг за шагом**: на каждом шаге делается один из возможных выборов, рекурсия продолжается, затем выбор **отменяется** и пробуется следующий.

```text
qur(joriy):
    если joriy полный: вывести; вернуться
    для каждого возможного выбора t:
        добавить t к joriy
        qur(joriy)
        убрать t                  ← «возврат»
```

Выборы, нарушающие условие, отбрасываются сразу — это сильно сокращает перебор (*qur* — строить, *joriy* — текущий).

## Пользовательская библиотека

Функции, нужные в нескольких программах, собирают в отдельный файл:

| Язык | Файл библиотеки | Подключение |
|---|---|---|
| Python | `massivlar.py` — обычные функции | `import massivlar` → `massivlar.yigindi(a)` или `from massivlar import yigindi` |
| Java | `Massivlar.java` — `public final class` со `static`-методами (как класс `Math`) | в том же пакете — напрямую `Massivlar.yigindi(a)`; в другом — `import` |
| C++ | `massivlar.h` — объявления (прототипы), `massivlar.cpp` — тела | `#include "massivlar.h"`, компиляция: `g++ main.cpp massivlar.cpp` |

В C++ функции помещают в **пространство имён** (`namespace massivlar { ... }`), чтобы они не конфликтовали с одноимёнными функциями другой библиотеки. Заголовочный файл начинается с `#pragma once`: даже если файл подключён несколько раз, он читается один раз.

Система автоматической проверки принимает один файл, поэтому в примерах ниже библиотека находится внутри этого файла — но написана так, чтобы её можно было сразу вынести в отдельный файл.

## Разобранный пример 1: Фибоначчи с мемоизацией

Дано *n* (0 ≤ *n* ≤ 90). Вычислите *F*(*n*) рекурсией с мемоизацией.

```python
from functools import cache


@cache                          # автоматически запоминает результаты
def fib(n):
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)


n = int(input())
print(fib(n))
```
```java
import java.util.Scanner;

public class Main {
    static long[] memo;

    static long fib(int n) {
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];      // уже вычислено
        memo[n] = fib(n - 1) + fib(n - 2);
        return memo[n];
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        memo = new long[n + 1];
        System.out.println(fib(n));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

vector<long long> memo;

long long fib(int n) {
    if (n < 2) return n;
    if (memo[n] != -1) return memo[n];         // уже вычислено
    return memo[n] = fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    memo.assign(n + 1, -1);
    cout << fib(n) << endl;
    return 0;
}
```

Для `10` выводится `55`, для `90` — `2880067194370816120` (это помещается в `long`, а *F*(93) — уже нет). В Java условие `memo[n] != 0` работает, потому что при *n* ≥ 2 значение *F*(*n*) никогда не равно 0; в C++ признаком «не вычислено» служит `-1`. Без мемоизации *n* = 90 вычислить было бы невозможно.

## Разобранный пример 2: метод возврата — строки без соседних единиц

Дано *n* (1 ≤ *n* ≤ 15). Выведите в лексикографическом порядке все строки длины *n* из `0` и `1`, в которых две `1` не стоят рядом, а в конце — их количество.

```python
def qur(joriy, n, natija):
    if len(joriy) == n:
        natija.append(joriy)
        return
    qur(joriy + "0", n, natija)
    if not joriy or joriy[-1] != "1":      # после 1 нельзя ставить 1
        qur(joriy + "1", n, natija)


n = int(input())
natija = []
qur("", n, natija)
print("\n".join(natija))
print(len(natija))
```
```java
import java.util.Scanner;

public class Main {
    static StringBuilder chiqish = new StringBuilder();
    static int soni = 0;

    static void qur(char[] joriy, int i) {
        if (i == joriy.length) {
            chiqish.append(joriy).append('\n');
            soni++;
            return;
        }
        joriy[i] = '0';
        qur(joriy, i + 1);
        if (i == 0 || joriy[i - 1] != '1') {   // после 1 нельзя ставить 1
            joriy[i] = '1';
            qur(joriy, i + 1);
        }
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        qur(new char[n], 0);
        System.out.print(chiqish);
        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int soni = 0;

void qur(string& joriy, int n) {
    if ((int) joriy.size() == n) {
        cout << joriy << '\n';
        soni++;
        return;
    }
    joriy.push_back('0');
    qur(joriy, n);
    joriy.pop_back();                          // отменяем выбор
    if (joriy.empty() || joriy.back() != '1') {  // после 1 нельзя ставить 1
        joriy.push_back('1');
        qur(joriy, n);
        joriy.pop_back();
    }
}

int main() {
    int n;
    cin >> n;
    string joriy;
    qur(joriy, n);
    cout << soni << endl;
    return 0;
}
```

При вводе `3` выводится:

```text
000
001
010
100
101
5
```

Поскольку `0` пробуется раньше `1`, строки сами собой выходят в лексикографическом порядке. В версии на C++ «возврат» виден явно: `push_back` — выбор, `pop_back` — его отмена. В версии на Python `joriy + "0"` создаёт новую строку, поэтому отменять ничего не нужно. Любопытный факт: количество таких строк растёт по числам Фибоначчи — 2, 3, 5, 8, 13, …

## Разобранный пример 3: библиотека для массивов

В первой строке *n*, во второй — *n* целых чисел. С помощью рекурсивных функций библиотеки `massivlar` (массивы) выведите сумму, максимум и массив в обратном порядке.

```python
# ---- содержимое файла massivlar.py ----
def yigindi(a, n):
    """Сумма первых n элементов a."""
    if n == 0:
        return 0
    return yigindi(a, n - 1) + a[n - 1]


def maksimum(a, n):
    if n == 1:
        return a[0]
    return max(maksimum(a, n - 1), a[n - 1])


def teskari(a, chap, ong):
    if chap >= ong:
        return
    a[chap], a[ong] = a[ong], a[chap]
    teskari(a, chap + 1, ong - 1)
# ---- основная программа (в отдельном файле: import massivlar) ----


n = int(input())
a = list(map(int, input().split()))
print(yigindi(a, n))
print(maksimum(a, n))
teskari(a, 0, n - 1)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    // в файле Massivlar.java это будет public final class
    static final class Massivlar {
        private Massivlar() {}                 // объекты не создаются: только static-методы

        static long yigindi(int[] a, int n) {
            if (n == 0) return 0;
            return yigindi(a, n - 1) + a[n - 1];
        }

        static int maksimum(int[] a, int n) {
            if (n == 1) return a[0];
            return Math.max(maksimum(a, n - 1), a[n - 1]);
        }

        static void teskari(int[] a, int chap, int ong) {
            if (chap >= ong) return;
            int t = a[chap];
            a[chap] = a[ong];
            a[ong] = t;
            teskari(a, chap + 1, ong - 1);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        System.out.println(Massivlar.yigindi(a, n));
        System.out.println(Massivlar.maksimum(a, n));
        Massivlar.teskari(a, 0, n - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

// ---- содержимое massivlar.h и massivlar.cpp ----
namespace massivlar {
    long long yigindi(const vector<int>& a, int n) {
        if (n == 0) return 0;
        return yigindi(a, n - 1) + a[n - 1];
    }

    int maksimum(const vector<int>& a, int n) {
        if (n == 1) return a[0];
        return max(maksimum(a, n - 1), a[n - 1]);
    }

    void teskari(vector<int>& a, int chap, int ong) {
        if (chap >= ong) return;
        swap(a[chap], a[ong]);
        teskari(a, chap + 1, ong - 1);
    }
}
// ---- основная программа ----

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    cout << massivlar::yigindi(a, n) << endl;
    cout << massivlar::maksimum(a, n) << endl;
    massivlar::teskari(a, 0, n - 1);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

При вводе `5` и `3 8 -1 6 2` выводится `18`, `8` и `2 6 -1 8 3`. Функции библиотеки ничего не читают и не выводят — поэтому их можно использовать в любой программе (*yigindi* — сумма, *teskari* — разворот). В C++ `massivlar::yigindi` — обращение через пространство имён; в Java `Massivlar.yigindi` — через имя класса, как `Math.max`. Каждый рекурсивный вызов занимает место в стеке, поэтому такие функции безопасны до нескольких тысяч элементов; для очень больших массивов лучше цикл.

:::warning
**Частые ошибки**

- **Забытый или неверный базовый случай.** Рекурсия не останавливается, и стек переполняется (`RecursionError`, `StackOverflowError`).
- **Таблица мемоизации внутри функции.** При каждом вызове создаётся новая таблица, и ничего не запоминается; таблица должна быть вне функции.
- **Неотменённый выбор в методе возврата.** Общее состояние (например, `joriy` в C++) переходит в следующую ветку «испорченным».
- **Тестовый код в файле, предназначенном для `import` в Python.** Этот код тоже выполнится при подключении модуля; поместите его под `if __name__ == "__main__":`.
:::

:::tip
Проверяя рекурсивную функцию, начинайте с самых маленьких случаев: *n* = 0, 1, 2. Если они работают и рекурсивный шаг верен, функция работает при любом *n* — это и есть математическая индукция.
:::

## Итоги

- Задача с массивом сводится к такой же меньшей задаче сужением границ.
- Мемоизация не повторяет одинаковые вычисления: Фибоначчи переходит от экспоненциального времени к линейному.
- Метод возврата: выбор → рекурсия → отмена выбора; неподходящие ветки отсекаются сразу.
- Библиотека: Python — модуль, Java — класс со `static`-методами, C++ — заголовочный файл, `.cpp`-файл и пространство имён.
- Функции библиотеки не читают и не выводят — только вычисляют или изменяют данные.

## Проверьте себя

:::check
Q: Почему обычная рекурсивная функция Фибоначчи работает медленно?
A: Одни и те же значения (например, *F*(*n* − 2)) многократно пересчитываются; число вызовов растёт экспоненциально.
Q: Что такое мемоизация?
A: Хранение вычисленных результатов в таблице и взятие их из таблицы вместо повторного вычисления.
Q: Что означает «возврат» в методе возврата?
A: Отмену выбора, сделанного перед рекурсивным вызовом, и переход к следующему варианту.
Q: Зачем в C++ пространство имён?
A: Чтобы одноимённые функции разных библиотек не конфликтовали: `massivlar::yigindi` и `matritsa::yigindi` различаются.
Q: Как сделать, чтобы тестовый код модуля Python не выполнялся при импорте?
A: Поместить его в блок `if __name__ == "__main__":`.
:::

:::link https://docs.python.org/3/tutorial/modules.html
Python: модули
Официальное руководство по созданию своего модуля и его импорту (на английском).
:::

:::link https://docs.python.org/3/library/functools.html#functools.cache
Python: functools.cache
Декоратор, автоматически запоминающий результаты функции (на английском).
:::
