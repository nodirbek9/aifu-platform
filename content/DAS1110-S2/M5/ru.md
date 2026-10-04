---
title: Работа с шаблонами
summary: Идея обобщённого программирования; шаблоны функций и классов в C++, дженерики Java и ограничения (extends Comparable), «утиная типизация» и TypeVar в Python; шаблонная функция, шаблонный класс (стек) и обобщённая сортировка.
---

## Почему это важно

Мы написали функцию, находящую наибольший элемент массива целых чисел. Завтра то же самое понадобится для вещественных чисел, послезавтра — для строк. Копировать функцию и менять только тип — путь к ошибкам: ошибку исправят в одной копии, а в остальных она останется.

**Шаблоны** (в C++ — template, в Java — generic) позволяют написать алгоритм один раз с **параметром типа**: наибольший элемент массива типа `T` — а вместо `T` можно подставить `int`, `double` или `String`. Сами стандартные библиотеки устроены так: `vector<int>`, `ArrayList<String>`, `sort` — всё это шаблоны.

:::note
**После этого занятия вы сможете:**

- объяснять, зачем нужно обобщённое (generic) программирование;
- писать шаблоны функций и классов в C++, обобщённые методы и классы в Java;
- накладывать ограничение на параметр типа (`T extends Comparable<T>`);
- объяснять, почему одна функция Python работает с разными типами («утиная типизация»);
- использовать шаблонные функции и классы в своих программах.
:::

## Обобщённое программирование

Алгоритм поиска наибольшего элемента не зависит от типа: ему нужно лишь, чтобы два элемента можно было **сравнить** (`<`). Шаблон выражает именно это: «для некоторого типа `T`, если значения `T` можно сравнивать».

| Язык | Шаблон функции | Вызов |
|---|---|---|
| C++ | `template <typename T> T engKatta(const vector<T>& a)` | `engKatta(sonlar)`, `engKatta(sozlar)` |
| Java | `static <T extends Comparable<T>> T engKatta(List<T> a)` | `engKatta(sonlar)` |
| Python | `def eng_katta(a): ...` (тип не требуется) | `eng_katta(sonlar)` |

Параметр типа обычно обозначают одной заглавной буквой: `T` (type), `K` и `V` (key, value), `E` (element).

## Шаблоны C++

Шаблон C++ — это **форма** для кода. Увидев вызов `engKatta(sonlar)`, компилятор создаёт отдельную копию функции для `T = int`, а для `engKatta(sozlar)` — копию для `T = string`. Этот процесс называется **инстанцированием**.

- Тип обычно **выводится автоматически** из аргументов; при необходимости его указывают явно: `engKatta<double>(a)`.
- Какие операции шаблон применяет к `T` (например, `<`), такие и должен поддерживать реальный тип — иначе ошибка компиляции.
- Шаблон класса: `template <typename T> class Stek { vector<T> elementlar; ... };` и `Stek<int> s;`.
- Полный код шаблона должен находиться в заголовочном файле (`.h`): компилятор должен видеть его для каждого типа.

## Дженерики Java

В Java параметр типа пишется в угловых скобках: `class Stek<T>` для класса и `<T>` перед типом результата для метода.

- Параметром типа может быть только **объектный тип**: `Stek<Integer>`, а не `Stek<int>`. Переход между `int` и `Integer` выполняется автоматически (autoboxing).
- **Ограничение**: `<T extends Comparable<T>>` — «`T` обязан быть взаимно сравнимым типом». После этого можно вызывать `a.compareTo(b)`. Для `T` без ограничения доступны только методы `Object`.
- Компилятор проверяет типы: добавление числа в `Stek<String>` — ошибка компиляции. Во время выполнения параметр типа стирается (type erasure), поэтому нельзя написать `new T[n]` — используют `ArrayList<T>`.

## Python: утиная типизация

Python — язык с динамической типизацией: параметр функции принимает значение любого типа. Если операции внутри функции (например, `>`) существуют для значения, всё работает. Это называется **утиной типизацией** (duck typing): «если оно ходит как утка и крякает как утка — значит, это утка».

Поэтому в Python особый синтаксис шаблонов не нужен. Но для читателей кода и инструментов проверки пишут **аннотации типов**:

```python
from typing import TypeVar

T = TypeVar("T")

def birinchi(a: list[T]) -> T:      # «какой T пришёл, такой T и вернётся»
    return a[0]
```

С Python 3.12 есть и более короткая запись: `def birinchi[T](a: list[T]) -> T:`. Аннотации не влияют на работу программы (*birinchi* — первый).

## Разобранный пример 1: шаблонная функция

В первой строке целые числа, во второй — слова. С помощью одной обобщённой функции `eng_katta` (наибольший) и одной обобщённой функции `sanash` (подсчёт) выведите наибольший элемент каждой строки и сколько раз он встречается (слова сравниваются по алфавиту).

```python
def eng_katta(a):
    natija = a[0]
    for x in a:
        if x > natija:
            natija = x
    return natija


def sanash(a, qiymat):
    return sum(1 for x in a if x == qiymat)


sonlar = list(map(int, input().split()))
sozlar = input().split()

k = eng_katta(sonlar)
print(k, sanash(sonlar, k))
k = eng_katta(sozlar)
print(k, sanash(sozlar, k))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    static <T extends Comparable<T>> T engKatta(List<T> a) {
        T natija = a.get(0);
        for (T x : a) {
            if (x.compareTo(natija) > 0) natija = x;
        }
        return natija;
    }

    static <T> int sanash(List<T> a, T qiymat) {
        int soni = 0;
        for (T x : a) {
            if (x.equals(qiymat)) soni++;
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> sonlar = new ArrayList<>();
        for (String s : in.nextLine().trim().split("\\s+")) sonlar.add(Integer.parseInt(s));
        List<String> sozlar = Arrays.asList(in.nextLine().trim().split("\\s+"));

        Integer k1 = engKatta(sonlar);
        System.out.println(k1 + " " + sanash(sonlar, k1));
        String k2 = engKatta(sozlar);
        System.out.println(k2 + " " + sanash(sozlar, k2));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

template <typename T>
T engKatta(const vector<T>& a) {
    T natija = a[0];
    for (const T& x : a) {
        if (natija < x) natija = x;        // для T нужен только <
    }
    return natija;
}

template <typename T>
int sanash(const vector<T>& a, const T& qiymat) {
    int soni = 0;
    for (const T& x : a) {
        if (x == qiymat) soni++;
    }
    return soni;
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim1(satr);
    vector<int> sonlar;
    int x;
    while (oqim1 >> x) sonlar.push_back(x);

    getline(cin, satr);
    istringstream oqim2(satr);
    vector<string> sozlar;
    string s;
    while (oqim2 >> s) sozlar.push_back(s);

    int k1 = engKatta(sonlar);                 // T = int
    cout << k1 << " " << sanash(sonlar, k1) << endl;
    string k2 = engKatta(sozlar);              // T = string
    cout << k2 << " " << sanash(sozlar, k2) << endl;
    return 0;
}
```

Ввод:

```text
4 9 2 9 7
olma nok uzum anor uzum
```

Вывод:

```text
9 2
uzum 2
```

В каждом языке алгоритм написан **один раз**, но сработал с двумя разными типами. В C++ компилятор создал две копии — `engKatta<int>` и `engKatta<string>`. В Java числа `int` хранятся в `List<Integer>`, а сравнение идёт через `compareTo` — `<` только для чисел. В Java использован `equals`: сравнивать объекты `Integer` через `==` опасно.

## Разобранный пример 2: шаблонный класс — стек

**Стек** — структура по принципу «последним пришёл — первым ушёл» (LIFO): элемент кладётся только наверх (`push`) и берётся только сверху (`pop`). Напишите один обобщённый класс `Stek` (стек) и используйте его для слов и чисел: выведите слова первой строки и числа второй в обратном порядке.

```python
class Stek:
    def __init__(self):
        self._elementlar = []

    def push(self, x):
        self._elementlar.append(x)

    def pop(self):
        return self._elementlar.pop()

    def bosh(self):
        return len(self._elementlar) == 0


def teskari(qiymatlar):
    s = Stek()
    for x in qiymatlar:
        s.push(x)
    natija = []
    while not s.bosh():
        natija.append(s.pop())
    return natija


print(*teskari(input().split()))
print(*teskari(list(map(int, input().split()))))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Stek<T> {
        private final List<T> elementlar = new ArrayList<>();

        void push(T x) {
            elementlar.add(x);
        }

        T pop() {
            return elementlar.remove(elementlar.size() - 1);
        }

        boolean bosh() {
            return elementlar.isEmpty();
        }
    }

    static <T> String teskari(List<T> qiymatlar) {
        Stek<T> s = new Stek<>();
        for (T x : qiymatlar) s.push(x);
        StringBuilder sb = new StringBuilder();
        while (!s.bosh()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(s.pop());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> sozlar = List.of(in.nextLine().trim().split("\\s+"));
        List<Integer> sonlar = new ArrayList<>();
        for (String t : in.nextLine().trim().split("\\s+")) sonlar.add(Integer.parseInt(t));

        System.out.println(teskari(sozlar));
        System.out.println(teskari(sonlar));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

template <typename T>
class Stek {
    vector<T> elementlar;

public:
    void push(const T& x) { elementlar.push_back(x); }

    T pop() {
        T x = elementlar.back();
        elementlar.pop_back();
        return x;
    }

    bool bosh() const { return elementlar.empty(); }
};

template <typename T>
void teskariChiqar(istringstream& oqim) {
    Stek<T> s;
    T x;
    while (oqim >> x) s.push(x);
    bool birinchi = true;
    while (!s.bosh()) {
        if (!birinchi) cout << ' ';
        cout << s.pop();
        birinchi = false;
    }
    cout << endl;
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim1(satr);
    teskariChiqar<string>(oqim1);              // T указан явно
    getline(cin, satr);
    istringstream oqim2(satr);
    teskariChiqar<int>(oqim2);
    return 0;
}
```

Ввод:

```text
bir ikki uch
10 20 30 40
```

Вывод:

```text
uch ikki bir
40 30 20 10
```

`Stek<string>` и `Stek<int>` — два разных типа, полученных из одного шаблона класса (*bosh* — пустой). В `teskariChiqar<string>(oqim1)` на C++ тип указан явно: `T` не встречается среди аргументов функции, поэтому компилятор не может его вывести. В Java запись `s.push(5)` для `Stek<String>` не скомпилируется — дженерики ловят ошибку **до** запуска программы. Класс на Python вообще не объявляет типов и работает с любым значением.

## Разобранный пример 3: обобщённая сортировка с ограничением

В первой строке вещественные числа. Во второй — *n*, в следующих *n* строках — имя студента и балл. Одной обобщённой функцией `saralash` (сортировка вставками) отсортируйте числа по возрастанию, а студентов — в естественном порядке: по убыванию балла, при равном балле — по имени, и выведите их.

```python
def saralash(a):
    """Сортировка вставками: для любого типа, у которого есть <."""
    for i in range(1, len(a)):
        joriy = a[i]
        j = i - 1
        while j >= 0 and joriy < a[j]:
            a[j + 1] = a[j]
            j -= 1
        a[j + 1] = joriy


class Talaba:
    def __init__(self, ism, ball):
        self.ism = ism
        self.ball = ball

    def __lt__(self, boshqa):          # естественный порядок: балл по убыванию, затем имя
        return (-self.ball, self.ism) < (-boshqa.ball, boshqa.ism)


sonlar = list(map(float, input().split()))
saralash(sonlar)
print(*sonlar)

n = int(input())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append(Talaba(ism, int(ball)))
saralash(talabalar)
for t in talabalar:
    print(t.ism, t.ball)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** Сортировка вставками: для любого взаимно сравнимого типа. */
    static <T extends Comparable<T>> void saralash(List<T> a) {
        for (int i = 1; i < a.size(); i++) {
            T joriy = a.get(i);
            int j = i - 1;
            while (j >= 0 && joriy.compareTo(a.get(j)) < 0) {
                a.set(j + 1, a.get(j));
                j--;
            }
            a.set(j + 1, joriy);
        }
    }

    static class Talaba implements Comparable<Talaba> {
        final String ism;
        final int ball;

        Talaba(String ism, int ball) {
            this.ism = ism;
            this.ball = ball;
        }

        @Override
        public int compareTo(Talaba boshqa) {      // естественный порядок: балл по убыванию, затем имя
            if (ball != boshqa.ball) return Integer.compare(boshqa.ball, ball);
            return ism.compareTo(boshqa.ism);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Double> sonlar = new ArrayList<>();
        for (String s : in.nextLine().trim().split("\\s+")) sonlar.add(Double.parseDouble(s));
        saralash(sonlar);
        StringBuilder sb = new StringBuilder();
        for (double x : sonlar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(sb);

        int n = Integer.parseInt(in.nextLine().trim());
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            talabalar.add(new Talaba(q[0], Integer.parseInt(q[1])));
        }
        saralash(talabalar);
        for (Talaba t : talabalar) System.out.println(t.ism + " " + t.ball);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

// Сортировка вставками: для любого типа, у которого есть <
template <typename T>
void saralash(vector<T>& a) {
    for (size_t i = 1; i < a.size(); i++) {
        T joriy = a[i];
        int j = (int) i - 1;
        while (j >= 0 && joriy < a[j]) {
            a[j + 1] = a[j];
            j--;
        }
        a[j + 1] = joriy;
    }
}

struct Talaba {
    string ism;
    int ball;

    bool operator<(const Talaba& boshqa) const {   // естественный порядок: балл по убыванию, затем имя
        if (ball != boshqa.ball) return ball > boshqa.ball;
        return ism < boshqa.ism;
    }
};

int main() {
    string satr;
    getline(cin, satr);
    istringstream oqim(satr);
    vector<double> sonlar;
    double x;
    while (oqim >> x) sonlar.push_back(x);
    saralash(sonlar);
    for (size_t i = 0; i < sonlar.size(); i++) {
        if (i > 0) cout << ' ';
        cout << sonlar[i];
    }
    cout << endl;

    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;
    saralash(talabalar);
    for (const auto& t : talabalar) cout << t.ism << " " << t.ball << endl;
    return 0;
}
```

Ввод:

```text
3.5 -1.25 2.0 10.75
4
Malika 88
Aziz 92
Bekzod 88
Dilnoza 95
```

Вывод (Python и Java):

```text
-1.25 2.0 3.5 10.75
Dilnoza 95
Aziz 92
Bekzod 88
Malika 88
```

`cout` в C++ выводит вещественное число без лишних нулей, поэтому его первая строка — `-1.25 2 3.5 10.75`. `saralash` ничего не знает ни о вещественных числах, ни о студентах — она использует только `<` (в Java — `compareTo`). Ограничение `<T extends Comparable<T>>` в Java это гарантирует: при вызове с типом, не являющимся `Comparable`, компилятор выдаст ошибку. В C++ ограничение не записано, но если бы у `Talaba` не было `operator<`, ошибка появилась бы внутри шаблона, в строке `joriy < a[j]`.

:::warning
**Частые ошибки**

- **Примитивный параметр типа в Java.** `List<int>` написать нельзя; используйте `List<Integer>`, `List<Double>`.
- **`compareTo` без ограничения в Java.** Для простого `<T>` вызов `a.compareTo(b)` не скомпилируется; пишите `<T extends Comparable<T>>`.
- **Шаблон, спрятанный в `.cpp`-файл в C++.** При вызове из другого файла будет ошибка «undefined reference»; код шаблона находится в заголовочном файле.
- **Поиск ошибки шаблона C++ не там.** Компилятор показывает ошибку внутри шаблона, но причина часто в типе при вызове — ему не хватает нужной операции (`<`, `==`, `<<`).
:::

:::tip
Прежде чем писать шаблон, напишите и проверьте алгоритм для одного конкретного типа (например, `int`). Когда он заработает, замените `int` на `T` и выпишите, какие операции нужны `T`, — это и есть «требования» шаблона.
:::

## Итоги

- Шаблон (дженерик) позволяет написать алгоритм один раз с параметром типа.
- C++ создаёт отдельную копию шаблона для каждого использованного типа; код шаблона находится в заголовочном файле.
- Дженерики Java работают только с объектными типами; `extends` задаёт ограничение, типы проверяются при компиляции.
- В Python благодаря утиной типизации функция работает с любым подходящим типом; `TypeVar` нужен только для аннотаций.
- Требования шаблона к параметру типа — это операции, которые он использует (`<`, `==`, …).

## Проверьте себя

:::check
Q: Какую проблему решают шаблоны?
A: Копирование одного и того же алгоритма для разных типов: алгоритм пишется один раз с параметром типа.
Q: Почему в Java нельзя написать `Stek<int>`?
A: Параметром типа может быть только объектный тип; используют `Stek<Integer>`.
Q: Что означает `<T extends Comparable<T>>`?
A: `T` обязан быть взаимно сравнимым типом, поэтому можно вызывать `compareTo`.
Q: Сколько функций создаст компилятор C++ при вызовах `engKatta(sonlar)` и `engKatta(sozlar)`?
A: Две: отдельную копию для `T = int` и для `T = string`.
Q: Почему в Python не нужен особый синтаксис шаблонов?
A: Язык динамически типизирован: если операции внутри функции существуют для значения, она работает с любым типом (утиная типизация).
:::

:::link https://dev.java/learn/generics/
Java: дженерики
Официальный учебный ресурс об обобщённых классах и методах и об ограничениях (на английском).
:::

:::link https://en.cppreference.com/w/cpp/language/templates
C++: шаблоны
Справочник по шаблонам функций и классов (на английском).
:::
