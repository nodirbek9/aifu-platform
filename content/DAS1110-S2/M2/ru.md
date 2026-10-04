---
title: Основы объектно-ориентированного программирования
summary: Класс и объект, поля и методы, конструктор, self и this, преобразование объекта в текст, статические поля, ссылка на объект и его копия; класс на трёх языках.
---

## Почему это важно

До сих пор наши программы состояли из отдельных переменных и функций. Имя студента, его оценка и группа лежали в трёх отдельных переменных, а обрабатывающие их функции — в другом месте программы. Чем больше программа, тем труднее уследить, какие данные к какой функции относятся.

**Объектно-ориентированное программирование** (ООП) объединяет данные и действия над ними в одно целое — **объект**. Java целиком построена на этой идее, и в Python и C++ большие программы тоже пишут в основном с классами. Эта и две следующие лекции посвящены основным понятиям ООП.

:::note
**После этого занятия вы сможете:**

- различать понятия класса и объекта;
- писать класс с полями, методами и конструктором на Python, Java и C++;
- объяснять, что означают `self` и `this`;
- писать метод, превращающий объект в текст;
- называть разницу между статическим полем и полем объекта;
- объяснять, как работает присваивание объекта (`b = a`) в разных языках.
:::

## Класс и объект

**Класс** (class) — описание нового типа: какие данные есть у каждого значения этого типа и что оно умеет делать. **Объект** — конкретный экземпляр (instance) этого класса.

| Понятие | Смысл | Пример (класс `Talaba` — студент) |
|---|---|---|
| Класс | шаблон, чертёж | `Talaba` |
| Объект | конкретный экземпляр класса | студент «Азиз, 2 курс» |
| Поле (атрибут) | данные, которые хранит объект | `ism` (имя), `kurs` (курс) |
| Метод | действие, которое выполняет объект | `kursni_oshir()` (перевести на следующий курс) |
| Конструктор | особый метод, работающий при создании объекта | задаёт полям начальные значения |

Из одного класса можно создать сколько угодно объектов; поля у каждого свои, а методы общие.

## Класс на трёх языках

```python
class Talaba:
    def __init__(self, ism, kurs):     # конструктор
        self.ism = ism                 # поля
        self.kurs = kurs

    def kursni_oshir(self):            # метод
        self.kurs += 1

t = Talaba("Aziz", 2)
t.kursni_oshir()
print(t.ism, t.kurs)                   # Aziz 3
```

| | Python | Java | C++ |
|---|---|---|---|
| Конструктор | `def __init__(self, ...)` | по имени класса: `Talaba(String ism, int kurs)` | по имени класса: `Talaba(string ism, int kurs)` |
| Объявление поля | в конструкторе: `self.ism = ...` | в теле класса: `String ism;` | в теле класса: `string ism;` |
| Текущий объект | `self` (первый параметр, пишется явно) | `this` (неявно) | `this` (указатель, неявно) |
| Создание объекта | `Talaba("Aziz", 2)` | `new Talaba("Aziz", 2)` | `Talaba t("Aziz", 2);` |
| Преобразование в текст | `def __str__(self)` | `public String toString()` | написать оператор `<<` или свой метод |

**`self` и `this`** — это сам объект, у которого вызван метод. При вызове `t.kursni_oshir()` внутри метода `self` (в Java/C++ — `this`) равен именно `t`. В Java и C++, если имя поля совпадает с именем параметра, их различают так: `this.ism = ism`.

## Статические поля

Обычное поле у каждого объекта своё. **Статическое поле** (в Python — атрибут класса) принадлежит классу и **одно** на все объекты. Например, чтобы считать, сколько создано студентов:

| Язык | Статическое поле | Обращение |
|---|---|---|
| Python | в теле класса: `soni = 0` | `Talaba.soni` |
| Java | `static int soni = 0;` | `Talaba.soni` |
| C++ | `static int soni;` и вне класса `int Talaba::soni = 0;` (с C++17 `inline static int soni = 0;`) | `Talaba::soni` |

## Ссылка и копия

Присваивание объекта другой переменной работает в языках **по-разному**:

- **Python и Java**: переменная хранит не сам объект, а **ссылку** на него. После `b = a` переменные `a` и `b` — один объект; измените `b.kurs` — изменится и `a.kurs`.
- **C++**: `Talaba b = a;` создаёт **копию** объекта; изменение `b` не влияет на `a`. Если нужна ссылка — `Talaba& b = a;`.

Поэтому в Java или Python, если передать объект в функцию и изменить внутри его поле, изменение будет видно и снаружи.

## Разобранный пример 1: класс прямоугольника

Даны ширина и высота прямоугольника. Напишите класс `Tortburchak` (прямоугольник): конструктор и методы `yuza()` (площадь) и `perimetr()`. Выведите площадь и периметр.

```python
class Tortburchak:
    def __init__(self, en, boy):
        self.en = en
        self.boy = boy

    def yuza(self):
        return self.en * self.boy

    def perimetr(self):
        return 2 * (self.en + self.boy)


en, boy = map(int, input().split())
t = Tortburchak(en, boy)
print(t.yuza(), t.perimetr())
```
```java
import java.util.Scanner;

public class Main {
    static class Tortburchak {
        long en, boy;

        Tortburchak(long en, long boy) {
            this.en = en;
            this.boy = boy;
        }

        long yuza() {
            return en * boy;
        }

        long perimetr() {
            return 2 * (en + boy);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tortburchak t = new Tortburchak(in.nextLong(), in.nextLong());
        System.out.println(t.yuza() + " " + t.perimetr());
    }
}
```
```cpp
#include <iostream>
using namespace std;

class Tortburchak {
public:
    long long en, boy;

    Tortburchak(long long en, long long boy) : en(en), boy(boy) {}

    long long yuza() const { return en * boy; }
    long long perimetr() const { return 2 * (en + boy); }
};

int main() {
    long long en, boy;
    cin >> en >> boy;
    Tortburchak t(en, boy);
    cout << t.yuza() << " " << t.perimetr() << endl;
    return 0;
}
```

При вводе `3 5` выводится `15 16`. В C++ `: en(en), boy(boy)` — **список инициализации**: поля получают значения до выполнения тела конструктора. `const` в конце метода — обещание «этот метод не меняет объект». В Java `static class` — самостоятельный класс внутри `Main`; так мы пишем всё в одном файле.

## Разобранный пример 2: класс времени

Даны часы и минуты (`HH MM`) и число минут *k*. Напишите класс `Vaqt` (время): метод `qosh(k)` (добавить) сдвигает время на *k* минут вперёд (через полночь — по кругу), а преобразование в текст даёт вид `HH:MM`.

```python
class Vaqt:
    def __init__(self, soat, daqiqa):
        self.daqiqalar = (soat * 60 + daqiqa) % 1440

    def qosh(self, k):
        self.daqiqalar = (self.daqiqalar + k) % 1440

    def __str__(self):
        return f"{self.daqiqalar // 60:02d}:{self.daqiqalar % 60:02d}"


soat, daqiqa = map(int, input().split())
k = int(input())
v = Vaqt(soat, daqiqa)
v.qosh(k)
print(v)
```
```java
import java.util.Scanner;

public class Main {
    static class Vaqt {
        private int daqiqalar;

        Vaqt(int soat, int daqiqa) {
            daqiqalar = (soat * 60 + daqiqa) % 1440;
        }

        void qosh(int k) {
            daqiqalar = (daqiqalar + k) % 1440;
        }

        @Override
        public String toString() {
            return String.format("%02d:%02d", daqiqalar / 60, daqiqalar % 60);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Vaqt v = new Vaqt(in.nextInt(), in.nextInt());
        v.qosh(in.nextInt());
        System.out.println(v);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

class Vaqt {
    int daqiqalar;

public:
    Vaqt(int soat, int daqiqa) : daqiqalar((soat * 60 + daqiqa) % 1440) {}

    void qosh(int k) { daqiqalar = (daqiqalar + k) % 1440; }

    string matn() const {
        char buf[6];
        snprintf(buf, sizeof buf, "%02d:%02d", daqiqalar / 60, daqiqalar % 60);
        return buf;
    }
};

int main() {
    int soat, daqiqa, k;
    cin >> soat >> daqiqa >> k;
    Vaqt v(soat, daqiqa);
    v.qosh(k);
    cout << v.matn() << endl;
    return 0;
}
```

При вводе `23 45` и `30` выводится `00:15`: через 30 минут после 23:45 мы перешли полночь. Внутри объекта время хранится не как часы и минуты, а как **минуты с полуночи** (*daqiqalar*) — так проще считать. Этот внутренний выбор не виден тому, кто пользуется классом: он знает только `qosh` и текстовый вид (*matn*). В Python `print(v)` автоматически вызывает `__str__`, в Java `println(v)` — `toString()`.

## Разобранный пример 3: класс дроби и статический счётчик

Даны две дроби `a b` и `c d` (`a/b` и `c/d`). Напишите класс `Kasr` (дробь): конструктор сокращает дробь, метод `qosh` возвращает новый объект `Kasr`. Выведите сумму и сколько объектов `Kasr` было создано за время работы.

```python
from math import gcd


class Kasr:
    soni = 0                        # статическое: одно на все объекты

    def __init__(self, surat, maxraj):
        g = gcd(surat, maxraj)
        self.surat = surat // g
        self.maxraj = maxraj // g
        Kasr.soni += 1

    def qosh(self, boshqa):
        return Kasr(self.surat * boshqa.maxraj + boshqa.surat * self.maxraj,
                    self.maxraj * boshqa.maxraj)

    def __str__(self):
        return f"{self.surat}/{self.maxraj}"


a, b = map(int, input().split())
c, d = map(int, input().split())
x = Kasr(a, b)
y = Kasr(c, d)
print(x.qosh(y))
print(Kasr.soni)
```
```java
import java.util.Scanner;

public class Main {
    static class Kasr {
        static int soni = 0;        // статическое: одно на все объекты
        final long surat, maxraj;

        Kasr(long surat, long maxraj) {
            long g = ekub(surat, maxraj);
            this.surat = surat / g;
            this.maxraj = maxraj / g;
            soni++;
        }

        static long ekub(long a, long b) {
            return b == 0 ? a : ekub(b, a % b);
        }

        Kasr qosh(Kasr boshqa) {
            return new Kasr(surat * boshqa.maxraj + boshqa.surat * maxraj, maxraj * boshqa.maxraj);
        }

        @Override
        public String toString() {
            return surat + "/" + maxraj;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Kasr x = new Kasr(in.nextLong(), in.nextLong());
        Kasr y = new Kasr(in.nextLong(), in.nextLong());
        System.out.println(x.qosh(y));
        System.out.println(Kasr.soni);
    }
}
```
```cpp
#include <iostream>
#include <numeric>
using namespace std;

class Kasr {
public:
    inline static int soni = 0;     // статическое: одно на все объекты
    long long surat, maxraj;

    Kasr(long long s, long long m) {
        long long g = gcd(s, m);
        surat = s / g;
        maxraj = m / g;
        soni++;
    }

    Kasr qosh(const Kasr& boshqa) const {
        return Kasr(surat * boshqa.maxraj + boshqa.surat * maxraj, maxraj * boshqa.maxraj);
    }
};

int main() {
    long long a, b, c, d;
    cin >> a >> b >> c >> d;
    Kasr x(a, b), y(c, d);
    Kasr z = x.qosh(y);
    cout << z.surat << "/" << z.maxraj << endl;
    cout << Kasr::soni << endl;
    return 0;
}
```

При вводе `1 6` и `1 3`: 1/6 + 1/3 = 3/18 + 6/18 = 9/18 = 1/2. Выводится `1/2` и `3`: две дроби прочитаны, третья создана внутри `qosh`. Поскольку `soni` (количество) статическое, все три объекта увеличили один счётчик. `qosh` не меняет существующие объекты, а возвращает **новый** — поэтому `x` и `y` сохраняют значения (*surat* — числитель, *maxraj* — знаменатель). В C++ `gcd` — готовая функция из `<numeric>` (C++17), а `inline static` позволяет инициализировать статическое поле внутри класса.

:::warning
**Частые ошибки**

- **Забытый `self` в методе Python.** При `def yuza():` вызов `t.yuza()` даёт ошибку; к полям тоже обращаются только через `self.en`.
- **Одинаковые имена без `this` в Java/C++.** В конструкторе `en = en;` присваивает параметр самому себе, а поле остаётся 0; пишите `this.en = en`.
- **Объект без `new` в Java.** `Tortburchak t;` объявляет только ссылку (`null`); объект создаётся через `new`.
- **Принять ссылку за копию.** В Python/Java `b = a` не создаёт новый объект; изменение `b` меняет и `a`.
:::

:::tip
Проектируя класс, сначала ответьте на бумаге на вопросы «что этот объект **знает**?» (поля) и «что он **умеет**?» (методы), а потом переходите к коду.
:::

## Итоги

- Класс — описание нового типа; объект — его конкретный экземпляр.
- Поля хранят данные объекта, методы — его действия; конструктор приводит объект в начальное состояние.
- `self` / `this` — объект, у которого вызван метод.
- Статическое поле принадлежит классу и одно на все объекты.
- В Python и Java переменная хранит ссылку на объект, в C++ присваивание создаёт копию.

## Проверьте себя

:::check
Q: В чём разница между классом и объектом?
A: Класс — описание типа (чертёж), объект — конкретное значение, созданное из этого класса; из одного класса можно создать много объектов.
Q: Когда работает конструктор?
A: При создании объекта, автоматически и один раз.
Q: В Python после `a = Talaba("Ali", 1)`, `b = a`, `b.kurs = 2` чему равно `a.kurs`?
A: 2 — `a` и `b` ссылаются на один объект.
Q: В C++ после `Talaba b = a; b.kurs = 2;` изменится ли `a.kurs`?
A: Нет, `b` — копия `a`.
Q: Чем статическое поле отличается от обычного?
A: Оно принадлежит классу и одно на все объекты; обычное поле у каждого объекта своё.
:::

:::link https://docs.python.org/3/tutorial/classes.html
Python: классы
Официальное руководство по классам, объектам, `__init__` и методам (на английском).
:::

:::link https://dev.java/learn/classes-objects/
Java: классы и объекты
Официальный учебный ресурс о классах, конструкторах и `this` в Java (на английском).
:::
