---
title: Инкапсуляция и наследование. Полиморфизм
summary: Три принципа ООП; закрытые поля, геттеры и сеттеры; класс-наследник, super и цепочка конструкторов; переопределение метода, виртуальные методы и разные объекты в одном списке; абстрактный класс.
---

## Почему это важно

На двух прошлых лекциях мы писали классы и хранили объекты в массиве. Но поля наших классов были открыты всем: любой код мог записать студенту `ball = -500`. Кроме того, для похожих классов (например, обычного сотрудника и менеджера) приходилось дважды писать один и тот же код.

ООП решает эти проблемы тремя принципами: **инкапсуляция** защищает объект от неверного состояния, **наследование** хранит общий код в одном месте, а **полиморфизм** позволяет одинаково работать с разными объектами. Эти три принципа лежат в основе любой большой программы — от мобильного приложения до банковской системы.

:::note
**После этого занятия вы сможете:**

- своими словами объяснять три принципа ООП;
- закрывать поля и управлять ими через геттер и проверяющий сеттер;
- писать класс-наследник и вызывать конструктор и методы родителя через `super`;
- переопределять метод и применять полиморфизм к разным объектам в одном списке;
- объяснять, зачем в C++ нужен `virtual` и что такое абстрактный класс.
:::

## Инкапсуляция

**Инкапсуляция** — сокрытие внутренних данных объекта от внешнего мира и доступ к ним только через методы класса. Тогда объект всегда остаётся в **корректном состоянии**: проверку выполняет метод.

| Язык | Закрытое поле | Открытый метод |
|---|---|---|
| Java | `private int ball;` | `public int getBall()`, `public void setBall(int b)` |
| C++ | в разделе `private:` (по умолчанию в `class`) | в разделе `public:` |
| Python | соглашение: `self._ball` (одно подчёркивание) — «внутреннее, не трогать» | обычный метод или `@property` |

**Геттер** возвращает значение поля, **сеттер** устанавливает новое значение **после проверки**. В Python нет ключевого слова `private`: подчёркивание — соглашение между программистами, язык его не навязывает.

Модификаторы доступа (Java и C++):

| Модификатор | Кто видит |
|---|---|
| `private` | только сам класс |
| `protected` | класс и его наследники (в Java ещё и тот же пакет) |
| `public` | все |

## Наследование

**Наследование** (inheritance) — создание нового класса (**наследника**, derived) на основе существующего (**родителя**, base). Наследник получает все поля и методы родителя и добавляет свои. Это отношение «**является**»: менеджер является сотрудником, круг — фигурой.

| | Python | Java | C++ |
|---|---|---|---|
| Объявление наследника | `class Menejer(Xodim):` | `class Menejer extends Xodim` | `class Menejer : public Xodim` |
| Конструктор родителя | `super().__init__(ism, maosh)` | `super(ism, maosh);` (первая строка) | `: Xodim(ism, maosh)` (в списке инициализации) |
| Метод родителя | `super().oylik()` | `super.oylik()` | `Xodim::oylik()` |

При создании объекта-наследника **сначала строится родительская часть**, затем часть наследника — поэтому конструктор родителя вызывается первым.

## Полиморфизм

**Полиморфизм** («многообразие форм») — один и тот же вызов работает по-разному в зависимости от фактического типа объекта. Наследник **переопределяет** (override) метод родителя: имя и параметры те же, содержание другое.

Сила полиморфизма в том, что разные объекты-наследники можно хранить в одном списке **типа родителя** и вызывать у всех один метод — каждый выполнит свою версию:

```text
shakllar = [Doira(2), Tortburchak(3, 4), Uchburchak(6, 2)]
для каждого s: s.yuza()     → каждый по своей формуле
```

Различия языков:

- В **Python и Java** все методы полиморфны по умолчанию. В Java аннотация `@Override` ловит ошибки: если имя метода написано неверно, компилятор предупредит.
- В **C++** метод должен быть объявлен в родителе как `virtual`, а объекты должны использоваться через **указатель** или **ссылку**. Иначе всегда вызывается версия родителя. В наследнике рекомендуется писать `override`.

**Абстрактный класс** — класс, объекты которого создать нельзя; он лишь шаблон для наследников. Его абстрактные методы не имеют тела, и каждый наследник обязан их определить: в Java — `abstract`, в C++ — `virtual double yuza() const = 0;` (чисто виртуальный метод), в Python — `@abstractmethod` из модуля `abc`.

## Разобранный пример 1: класс температуры (инкапсуляция)

В первой строке *n*, затем *n* целых чисел — температура по Цельсию. В классе `Harorat` (температура) значение хранится в закрытом поле; сеттер отклоняет значение ниже абсолютного нуля (−273 °C), и старое значение сохраняется. Для каждого числа вызовите сеттер: если значение принято, выведите температуру по Фаренгейту (с одним знаком после запятой), если отклонено — `Ошибка`.

```python
class Harorat:
    def __init__(self):
        self._selsiy = 0                  # внутреннее поле: снаружи не трогаем

    def selsiy(self):
        return self._selsiy

    def ornat(self, qiymat):
        if qiymat < -273:
            return False                  # неверное значение в объект не попадает
        self._selsiy = qiymat
        return True

    def farengeyt(self):
        return self._selsiy * 9 / 5 + 32


n = int(input())
h = Harorat()
for _ in range(n):
    c = int(input())
    if h.ornat(c):
        print(f"{h.farengeyt():.1f}")
    else:
        print("Ошибка")
```
```java
import java.util.Scanner;

public class Main {
    static class Harorat {
        private int selsiy = 0;           // внутреннее поле: снаружи не трогаем

        public int getSelsiy() {
            return selsiy;
        }

        public boolean setSelsiy(int qiymat) {
            if (qiymat < -273) return false;   // неверное значение в объект не попадает
            selsiy = qiymat;
            return true;
        }

        public double farengeyt() {
            return selsiy * 9 / 5.0 + 32;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Harorat h = new Harorat();
        for (int i = 0; i < n; i++) {
            if (h.setSelsiy(in.nextInt())) {
                System.out.printf("%.1f%n", h.farengeyt());
            } else {
                System.out.println("Ошибка");
            }
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

class Harorat {
private:
    int selsiy = 0;                       // внутреннее поле: снаружи не трогаем

public:
    int getSelsiy() const { return selsiy; }

    bool setSelsiy(int qiymat) {
        if (qiymat < -273) return false;  // неверное значение в объект не попадает
        selsiy = qiymat;
        return true;
    }

    double farengeyt() const { return selsiy * 9 / 5.0 + 32; }
};

int main() {
    int n;
    cin >> n;
    Harorat h;
    for (int i = 0; i < n; i++) {
        int c;
        cin >> c;
        if (h.setSelsiy(c)) {
            printf("%.1f\n", h.farengeyt());
        } else {
            printf("Ошибка\n");
        }
    }
    return 0;
}
```

Ввод:

```text
4
25
-300
-40
37
```

Вывод:

```text
77.0
Ошибка
-40.0
98.6
```

`-300` отклонено, и объект сохранил прежнее значение (25). В Java и C++ нельзя написать `h.selsiy = -300;` — компилятор запрещает доступ к полю `private`. В формуле написано `9 / 5.0`: `9 / 5` — целочисленное деление, оно дало бы 1.

## Разобранный пример 2: сотрудник и менеджер (наследование)

В первой строке *n*, затем *n* строк: `xodim ИМЯ ОКЛАД` (сотрудник) или `menejer ИМЯ ОКЛАД ПРОЦЕНТ` (менеджер). Менеджер — это сотрудник, но к его зарплате добавляется премия в размере ПРОЦЕНТ процентов оклада. Выведите имя и зарплату каждого сотрудника, а в конце — общий фонд оплаты.

```python
class Xodim:
    def __init__(self, ism, maosh):
        self.ism = ism
        self.maosh = maosh

    def oylik(self):
        return self.maosh


class Menejer(Xodim):
    def __init__(self, ism, maosh, foiz):
        super().__init__(ism, maosh)      # строим родительскую часть
        self.foiz = foiz

    def oylik(self):                      # переопределение
        return super().oylik() + self.maosh * self.foiz // 100


n = int(input())
xodimlar = []
for _ in range(n):
    q = input().split()
    if q[0] == "menejer":
        xodimlar.append(Menejer(q[1], int(q[2]), int(q[3])))
    else:
        xodimlar.append(Xodim(q[1], int(q[2])))

fond = 0
for x in xodimlar:
    print(x.ism, x.oylik())
    fond += x.oylik()
print(fond)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Xodim {
        protected String ism;
        protected long maosh;

        Xodim(String ism, long maosh) {
            this.ism = ism;
            this.maosh = maosh;
        }

        long oylik() {
            return maosh;
        }
    }

    static class Menejer extends Xodim {
        private final long foiz;

        Menejer(String ism, long maosh, long foiz) {
            super(ism, maosh);            // строим родительскую часть
            this.foiz = foiz;
        }

        @Override
        long oylik() {                    // переопределение
            return super.oylik() + maosh * foiz / 100;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Xodim> xodimlar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String tur = in.next();
            if (tur.equals("menejer")) {
                xodimlar.add(new Menejer(in.next(), in.nextLong(), in.nextLong()));
            } else {
                xodimlar.add(new Xodim(in.next(), in.nextLong()));
            }
        }

        long fond = 0;
        for (Xodim x : xodimlar) {
            System.out.println(x.ism + " " + x.oylik());
            fond += x.oylik();
        }
        System.out.println(fond);
    }
}
```
```cpp
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Xodim {
protected:
    string ism;
    long long maosh;

public:
    Xodim(string ism, long long maosh) : ism(ism), maosh(maosh) {}
    virtual ~Xodim() = default;

    string getIsm() const { return ism; }
    virtual long long oylik() const { return maosh; }
};

class Menejer : public Xodim {
    long long foiz;

public:
    Menejer(string ism, long long maosh, long long foiz)
        : Xodim(ism, maosh), foiz(foiz) {}   // строим родительскую часть

    long long oylik() const override {       // переопределение
        return Xodim::oylik() + maosh * foiz / 100;
    }
};

int main() {
    int n;
    cin >> n;
    vector<unique_ptr<Xodim>> xodimlar;
    for (int i = 0; i < n; i++) {
        string tur, ism;
        long long maosh;
        cin >> tur >> ism >> maosh;
        if (tur == "menejer") {
            long long foiz;
            cin >> foiz;
            xodimlar.push_back(make_unique<Menejer>(ism, maosh, foiz));
        } else {
            xodimlar.push_back(make_unique<Xodim>(ism, maosh));
        }
    }

    long long fond = 0;
    for (const auto& x : xodimlar) {
        cout << x->getIsm() << " " << x->oylik() << endl;
        fond += x->oylik();
    }
    cout << fond << endl;
    return 0;
}
```

Ввод:

```text
3
xodim Aziz 5000000
menejer Malika 8000000 25
xodim Bekzod 4500000
```

Вывод:

```text
Aziz 5000000
Malika 10000000
Bekzod 4500000
19500000
```

`Menejer` не переписывал поля имени и оклада — он унаследовал их от `Xodim`. Внутри `oylik()` (зарплата) вызвана версия родителя (`super().oylik()`, `super.oylik()`, `Xodim::oylik()`), и к ней добавлена премия. Поля `protected`: видны наследнику, но не внешнему коду. В C++ для хранения объектов разных типов в одном `vector` нужны **указатели**; `unique_ptr` — «умный» указатель, который сам удаляет объект (динамическая память подробно — в следующих темах). `virtual ~Xodim()` в родителе гарантирует правильное удаление объекта-наследника (*fond* — фонд оплаты).

## Разобранный пример 3: фигуры (полиморфизм и абстрактный класс)

В первой строке *n*, затем *n* строк: `doira R` (круг), `tortburchak A B` (прямоугольник) или `uchburchak ОСНОВАНИЕ ВЫСОТА` (треугольник). Выведите вид и площадь каждой фигуры (с двумя знаками после запятой), а в конце — вид фигуры с наибольшей площадью. `Shakl` (фигура) — абстрактный класс.

```python
from abc import ABC, abstractmethod
from math import pi


class Shakl(ABC):
    @abstractmethod
    def yuza(self):
        pass

    @abstractmethod
    def nom(self):
        pass


class Doira(Shakl):
    def __init__(self, r):
        self.r = r

    def yuza(self):
        return pi * self.r * self.r

    def nom(self):
        return "doira"


class Tortburchak(Shakl):
    def __init__(self, a, b):
        self.a, self.b = a, b

    def yuza(self):
        return self.a * self.b

    def nom(self):
        return "tortburchak"


class Uchburchak(Shakl):
    def __init__(self, asos, h):
        self.asos, self.h = asos, h

    def yuza(self):
        return self.asos * self.h / 2

    def nom(self):
        return "uchburchak"


n = int(input())
shakllar = []
for _ in range(n):
    q = input().split()
    if q[0] == "doira":
        shakllar.append(Doira(float(q[1])))
    elif q[0] == "tortburchak":
        shakllar.append(Tortburchak(float(q[1]), float(q[2])))
    else:
        shakllar.append(Uchburchak(float(q[1]), float(q[2])))

eng = shakllar[0]
for s in shakllar:
    print(f"{s.nom()} {s.yuza():.2f}")    # какой yuza() вызвать — решает тип объекта
    if s.yuza() > eng.yuza():
        eng = s
print(eng.nom())
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    abstract static class Shakl {
        abstract double yuza();
        abstract String nom();
    }

    static class Doira extends Shakl {
        final double r;
        Doira(double r) { this.r = r; }
        @Override double yuza() { return Math.PI * r * r; }
        @Override String nom() { return "doira"; }
    }

    static class Tortburchak extends Shakl {
        final double a, b;
        Tortburchak(double a, double b) { this.a = a; this.b = b; }
        @Override double yuza() { return a * b; }
        @Override String nom() { return "tortburchak"; }
    }

    static class Uchburchak extends Shakl {
        final double asos, h;
        Uchburchak(double asos, double h) { this.asos = asos; this.h = h; }
        @Override double yuza() { return asos * h / 2; }
        @Override String nom() { return "uchburchak"; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Shakl> shakllar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String tur = in.next();
            if (tur.equals("doira")) shakllar.add(new Doira(in.nextDouble()));
            else if (tur.equals("tortburchak")) shakllar.add(new Tortburchak(in.nextDouble(), in.nextDouble()));
            else shakllar.add(new Uchburchak(in.nextDouble(), in.nextDouble()));
        }

        Shakl eng = shakllar.get(0);
        for (Shakl s : shakllar) {
            System.out.printf("%s %.2f%n", s.nom(), s.yuza());   // какой yuza() вызвать — решает тип объекта
            if (s.yuza() > eng.yuza()) eng = s;
        }
        System.out.println(eng.nom());
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Shakl {
public:
    virtual ~Shakl() = default;
    virtual double yuza() const = 0;      // чисто виртуальный: Shakl — абстрактный
    virtual string nom() const = 0;
};

class Doira : public Shakl {
    double r;
public:
    Doira(double r) : r(r) {}
    double yuza() const override { return M_PI * r * r; }
    string nom() const override { return "doira"; }
};

class Tortburchak : public Shakl {
    double a, b;
public:
    Tortburchak(double a, double b) : a(a), b(b) {}
    double yuza() const override { return a * b; }
    string nom() const override { return "tortburchak"; }
};

class Uchburchak : public Shakl {
    double asos, h;
public:
    Uchburchak(double asos, double h) : asos(asos), h(h) {}
    double yuza() const override { return asos * h / 2; }
    string nom() const override { return "uchburchak"; }
};

int main() {
    int n;
    cin >> n;
    vector<unique_ptr<Shakl>> shakllar;
    for (int i = 0; i < n; i++) {
        string tur;
        double x, y;
        cin >> tur >> x;
        if (tur == "doira") {
            shakllar.push_back(make_unique<Doira>(x));
        } else {
            cin >> y;
            if (tur == "tortburchak") shakllar.push_back(make_unique<Tortburchak>(x, y));
            else shakllar.push_back(make_unique<Uchburchak>(x, y));
        }
    }

    const Shakl* eng = shakllar[0].get();
    for (const auto& s : shakllar) {
        printf("%s %.2f\n", s->nom().c_str(), s->yuza());   // какой yuza() вызвать — решает тип объекта
        if (s->yuza() > eng->yuza()) eng = s.get();
    }
    printf("%s\n", eng->nom().c_str());
    return 0;
}
```

Ввод:

```text
3
doira 2
tortburchak 3 4
uchburchak 6 5
```

Вывод:

```text
doira 12.57
tortburchak 12.00
uchburchak 15.00
uchburchak
```

Основной цикл знает только о `Shakl`: в нём нет проверок вида `if (tur == "doira")`. Если завтра добавится класс `Kvadrat` (квадрат), цикл менять не придётся — в этом главная польза полиморфизма. Поскольку `Shakl` абстрактный, `Shakl()` (Python), `new Shakl()` (Java) или `Shakl s;` (C++) — ошибка. Ключевое слово `override` в наследнике заставляет компилятор проверить, что у родителя действительно есть такой виртуальный метод: при ошибке в имени или параметрах программа не скомпилируется.

:::warning
**Частые ошибки**

- **Сеттер без проверки.** `setBall(int b) { ball = b; }` — то же самое, что открытое поле; смысл инкапсуляции — в проверке.
- **`super(...)` не в первой строке в Java.** Конструктор родителя должен быть первой командой конструктора наследника.
- **Переопределение без `virtual` в C++.** При вызове через указатель работает версия родителя; пишите `virtual` и `override`.
- **Копирование наследника в значение типа родителя в C++.** `Xodim x = menejer;` «срезает» часть с премией (slicing); используйте указатель или ссылку.
- **Наследование там, где нет «является».** У машины есть двигатель, но она *не является* двигателем — тогда двигатель будет полем, а не родительским классом.
:::

:::tip
Решая, должен ли новый класс наследовать родителя, спросите себя: «X является Y?». Если да — наследование, если «у X есть Y» — поле (композиция).
:::

## Итоги

- Инкапсуляция: поля закрыты, изменения идут через проверяющие методы; объект всегда в корректном состоянии.
- Наследование: наследник получает поля и методы родителя; конструктор родителя вызывается первым через `super`.
- Полиморфизм: наследник переопределяет метод; в списке типа родителя каждый объект выполняет свою версию.
- В C++ для полиморфизма нужны `virtual`-методы и указатель/ссылка; в родителе пишут виртуальный деструктор.
- Объекты абстрактного класса не создаются; он задаёт методы, которые наследники обязаны реализовать.

## Проверьте себя

:::check
Q: От чего инкапсуляция защищает объект?
A: От неверного состояния: поля меняются только через методы, выполняющие проверку.
Q: Кто видит поле `protected`?
A: Сам класс и его наследники (в Java — ещё классы того же пакета).
Q: Какой конструктор работает первым при создании объекта-наследника?
A: Конструктор родителя, затем конструктор наследника.
Q: Если в C++ метод родителя не `virtual`, какую версию вызовет `Xodim* x = new Menejer(...); x->oylik()`?
A: Версию родителя (`Xodim`).
Q: Можно ли создать объект абстрактного класса?
A: Нет; объекты создаются только из наследников, определивших все его абстрактные методы.
:::

:::link https://dev.java/learn/inheritance/
Java: наследование
Официальный учебный ресурс о наследниках, `super`, переопределении и абстрактных классах (на английском).
:::

:::link https://en.cppreference.com/w/cpp/language/virtual
C++: виртуальные функции
Справочник по `virtual`, `override` и чисто виртуальным функциям (на английском).
:::
