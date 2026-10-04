---
title: Работа с компонентами
summary: Понятие программного компонента; интерфейс как контракт компонента, взаимозаменяемые компоненты; свойства, методы и события; функции-обработчики событий (callback, listener); компоненты графического интерфейса и их размещение; композиция.
---

## Почему это важно

Современная программа — не единый кусок кода, а сборка из подключаемых друг к другу **компонентов**: кнопка, текстовое поле, платёжный модуль, подключение к базе данных, служба отправки сообщений. Каждый компонент знает своё дело и общается с остальными только через чётко определённый **интерфейс**. Благодаря этому компонент можно заменить (например, одну платёжную систему другой), не меняя остальную программу.

В средах вроде Visual Studio, IntelliJ или Qt Creator графическое приложение строится по этому принципу: на окно бросают готовые компоненты (кнопку, список, таблицу), настраивают их **свойства** и пишут функции, отвечающие на их **события**. На этой лекции рассмотрим основные идеи компонентного программирования на трёх языках.

:::note
**После этого занятия вы сможете:**

- объяснять, что такое программный компонент и его интерфейс;
- писать компоненты, взаимозаменяемые через интерфейс;
- различать свойства, методы и события компонента;
- регистрировать функции-обработчики событий (callback, listener) и вызывать их;
- собирать сложный компонент из простых с помощью композиции.
:::

## Компонент и интерфейс

**Компонент** — часть программы с чёткой задачей, которую можно заменить независимо. Он общается с внешним миром через **интерфейс** — контракт о том, какие методы есть и что они делают. Внутреннее устройство скрыто (инкапсуляция).

| Язык | Интерфейс | Реализация |
|---|---|---|
| Java | `interface Chegirma { long hisobla(long summa); }` | `class Foizli implements Chegirma` |
| C++ | класс с чисто виртуальными методами: `virtual long long hisobla(long long) const = 0;` | `class Foizli : public Chegirma` |
| Python | `abc.ABC` и `@abstractmethod` или просто методы с одинаковым именем | `class Foizli(Chegirma)` |

Код, написанный в расчёте на интерфейс («посчитать сумму заказа с помощью компонента скидки»), не знает, какой компонент подключён, — поэтому не меняется при добавлении нового вида скидки (*Chegirma* — скидка, *hisobla* — вычислить, *Foizli* — процентная).

## Свойства, методы и события

Компоненты графического интерфейса (кнопка, текстовое поле, надпись) имеют три вида членов:

| Член | Смысл | Пример для кнопки |
|---|---|---|
| Свойство (property) | состояние компонента | `Text = "Сохранить"`, `Enabled = false`, `Width = 120` |
| Метод | команда компоненту | `Focus()`, `Show()`, `Hide()` |
| Событие (event) | с компонентом что-то произошло | `Click`, `MouseEnter`, `TextChanged` |

В **событийно-ориентированном программировании** программа сама «не командует», а ждёт событий: когда пользователь нажимает кнопку, компонент вызывает все **зарегистрированные** у него функции. Такая функция называется **callback** или **listener** (слушатель).

| Язык / библиотека | Подключение обработчика события |
|---|---|
| C# Windows Forms | `button1.Click += Button1_Click;` |
| Java Swing | `tugma.addActionListener(e -> saqlash());` |
| Python Tkinter | `Button(oyna, text="Сохранить", command=saqlash)` |
| C++ Qt | `connect(tugma, &QPushButton::clicked, this, &Oyna::saqlash);` |

Чтобы хранить обработчики, языки используют функции как значения: в Java — лямбды и функциональные интерфейсы (`Runnable`, `Consumer<T>`), в C++ — `std::function`, в Python — обычную функцию или `lambda`.

## Образец графического интерфейса

Например, в Python Tkinter программа из одного окна, текстового поля и кнопки пишется так (ей нужна графическая среда, поэтому автоматическая проверка её не запускает):

```text
import tkinter as tk

def salomlash():
    belgi.config(text="Привет, " + maydon.get() + "!")   # изменение свойства

oyna = tk.Tk()
maydon = tk.Entry(oyna)
tugma = tk.Button(oyna, text="Поприветствовать", command=salomlash)   # подключение к событию
belgi = tk.Label(oyna, text="")
for komponent in (maydon, tugma, belgi):
    komponent.pack()                                   # размещение
oyna.mainloop()                                        # цикл событий
```

`mainloop()` — **цикл событий**: здесь программа ждёт действий пользователя и для каждого события вызывает соответствующий обработчик. По этой схеме работают все графические библиотеки (*oyna* — окно, *maydon* — поле, *tugma* — кнопка, *belgi* — надпись).

## Композиция

Сложный компонент **собирается** из простых: окно состоит из панелей, панель — из кнопок и полей. Это **композиция** — отношение «имеет» (в окне есть кнопка). В отличие от наследования, при композиции компонент не зависит от внутреннего устройства другого, и их можно заменять даже во время работы программы. Известное правило программистов: «предпочитайте композицию наследованию».

## Разобранный пример 1: заменяемый компонент скидки

В первой строке вид скидки: `yoq` (нет), `foiz P` (процент) или `qatiy S` (фиксированная) — P процентов от суммы или S сумов, но сумма не становится отрицательной. Во второй строке *n*, в третьей — *n* сумм заказов. Выведите итоговую сумму каждого заказа. Вычисляющий код знает только интерфейс `Chegirma`.

```python
from abc import ABC, abstractmethod


class Chegirma(ABC):
    @abstractmethod
    def hisobla(self, summa):
        pass


class Yoq(Chegirma):
    def hisobla(self, summa):
        return summa


class Foizli(Chegirma):
    def __init__(self, foiz):
        self.foiz = foiz

    def hisobla(self, summa):
        return summa - summa * self.foiz // 100


class Qatiy(Chegirma):
    def __init__(self, miqdor):
        self.miqdor = miqdor

    def hisobla(self, summa):
        return max(summa - self.miqdor, 0)


def chegirma_yarat(satr):                  # здесь решается, какой компонент подключить
    q = satr.split()
    if q[0] == "foiz":
        return Foizli(int(q[1]))
    if q[0] == "qatiy":
        return Qatiy(int(q[1]))
    return Yoq()


chegirma = chegirma_yarat(input())
n = int(input())
for summa in map(int, input().split()):
    print(chegirma.hisobla(summa))         # эта строка не знает тип
```
```java
import java.util.Scanner;

public class Main {
    interface Chegirma {
        long hisobla(long summa);
    }

    static class Yoq implements Chegirma {
        public long hisobla(long summa) { return summa; }
    }

    static class Foizli implements Chegirma {
        private final long foiz;
        Foizli(long foiz) { this.foiz = foiz; }
        public long hisobla(long summa) { return summa - summa * foiz / 100; }
    }

    static class Qatiy implements Chegirma {
        private final long miqdor;
        Qatiy(long miqdor) { this.miqdor = miqdor; }
        public long hisobla(long summa) { return Math.max(summa - miqdor, 0); }
    }

    static Chegirma chegirmaYarat(String satr) {   // здесь решается, какой компонент подключить
        String[] q = satr.trim().split("\\s+");
        if (q[0].equals("foiz")) return new Foizli(Long.parseLong(q[1]));
        if (q[0].equals("qatiy")) return new Qatiy(Long.parseLong(q[1]));
        return new Yoq();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Chegirma chegirma = chegirmaYarat(in.nextLine());
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            System.out.println(chegirma.hisobla(in.nextLong()));   // эта строка не знает тип
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <memory>
#include <sstream>
#include <string>
using namespace std;

class Chegirma {
public:
    virtual ~Chegirma() = default;
    virtual long long hisobla(long long summa) const = 0;
};

class Yoq : public Chegirma {
public:
    long long hisobla(long long summa) const override { return summa; }
};

class Foizli : public Chegirma {
    long long foiz;
public:
    Foizli(long long foiz) : foiz(foiz) {}
    long long hisobla(long long summa) const override { return summa - summa * foiz / 100; }
};

class Qatiy : public Chegirma {
    long long miqdor;
public:
    Qatiy(long long miqdor) : miqdor(miqdor) {}
    long long hisobla(long long summa) const override { return max(summa - miqdor, 0LL); }
};

unique_ptr<Chegirma> chegirmaYarat(const string& satr) {   // здесь решается, какой компонент подключить
    istringstream oqim(satr);
    string tur;
    long long qiymat = 0;
    oqim >> tur >> qiymat;
    if (tur == "foiz") return make_unique<Foizli>(qiymat);
    if (tur == "qatiy") return make_unique<Qatiy>(qiymat);
    return make_unique<Yoq>();
}

int main() {
    string satr;
    getline(cin, satr);
    unique_ptr<Chegirma> chegirma = chegirmaYarat(satr);
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        long long summa;
        cin >> summa;
        cout << chegirma->hisobla(summa) << endl;   // эта строка не знает тип
    }
    return 0;
}
```

Ввод:

```text
foiz 15
3
100000 250000 40000
```

Вывод:

```text
85000
212500
34000
```

Основной цикл лишь вызывает метод `hisobla`. Если завтра понадобится новая скидка вроде «50 % на второй заказ», пишется новый класс и добавляется одна строка в `chegirma_yarat` — цикл не меняется. Компонент создаётся в одном месте (здесь `chegirma_yarat`), а используется в другом — это главное правило компонентного проектирования. При `qatiy 50000` сумма 40000 становится 0, а не отрицательной.

## Разобранный пример 2: события и слушатели

К компоненту `Tugma` (кнопка) подключено несколько слушателей: счётчик (сколько раз нажата), журнал (записывает каждое нажатие) и предупреждающий (после 3-го нажатия говорит `Осторожно!`). На вводе *n* событий: `bos ИМЯ` (пользователь ИМЯ нажал) или `holat` (состояние). Для `bos` слушатели вызываются в порядке подключения; для `holat` выводится общее число нажатий.

```python
class Tugma:
    def __init__(self, matn):
        self.matn = matn                    # свойство
        self._tinglovchilar = []

    def bosilganda(self, funksiya):         # подключение обработчика события
        self._tinglovchilar.append(funksiya)

    def bos(self, kim):                     # событие произошло
        for funksiya in self._tinglovchilar:
            funksiya(kim)


soni = 0


def hisoblagich(kim):
    global soni
    soni += 1


def jurnal(kim):
    print(f"{kim}: {tugma.matn}")


def ogohlantirish(kim):
    if soni > 3:
        print("Осторожно!")


tugma = Tugma("Сохранить")
tugma.bosilganda(hisoblagich)
tugma.bosilganda(jurnal)
tugma.bosilganda(ogohlantirish)

n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "bos":
        tugma.bos(q[1])
    else:
        print(soni)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class Main {
    static class Tugma {
        final String matn;                                  // свойство
        private final List<Consumer<String>> tinglovchilar = new ArrayList<>();

        Tugma(String matn) { this.matn = matn; }

        void bosilganda(Consumer<String> funksiya) {        // подключение обработчика события
            tinglovchilar.add(funksiya);
        }

        void bos(String kim) {                              // событие произошло
            for (Consumer<String> funksiya : tinglovchilar) funksiya.accept(kim);
        }
    }

    static int soni = 0;

    public static void main(String[] args) {
        Tugma tugma = new Tugma("Сохранить");
        tugma.bosilganda(kim -> soni++);
        tugma.bosilganda(kim -> System.out.println(kim + ": " + tugma.matn));
        tugma.bosilganda(kim -> {
            if (soni > 3) System.out.println("Осторожно!");
        });

        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].equals("bos")) tugma.bos(q[1]);
            else System.out.println(soni);
        }
    }
}
```
```cpp
#include <functional>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class Tugma {
    vector<function<void(const string&)>> tinglovchilar;

public:
    string matn;                                            // свойство

    Tugma(string matn) : matn(matn) {}

    void bosilganda(function<void(const string&)> funksiya) {   // подключение обработчика события
        tinglovchilar.push_back(funksiya);
    }

    void bos(const string& kim) {                           // событие произошло
        for (auto& funksiya : tinglovchilar) funksiya(kim);
    }
};

int main() {
    int soni = 0;
    Tugma tugma("Сохранить");
    tugma.bosilganda([&](const string&) { soni++; });
    tugma.bosilganda([&](const string& kim) { cout << kim << ": " << tugma.matn << endl; });
    tugma.bosilganda([&](const string&) {
        if (soni > 3) cout << "Осторожно!" << endl;
    });

    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "bos") {
            string kim;
            cin >> kim;
            tugma.bos(kim);
        } else {
            cout << soni << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
6
bos Aziz
bos Malika
holat
bos Aziz
bos Bekzod
holat
```

Вывод:

```text
Aziz: Сохранить
Malika: Сохранить
2
Aziz: Сохранить
Bekzod: Сохранить
Осторожно!
4
```

`Tugma` сама ничего не считает и не выводит — она лишь вызывает подключённые функции, сообщая «меня нажали». Если нужно новое поведение, класс кнопки не меняется — просто подключается новый слушатель (*tinglovchilar* — слушатели, *bosilganda* — «при нажатии»). В C++ `[&]` означает, что лямбда захватывает внешние переменные (`soni`, `tugma`) по ссылке; в Java лямбда может изменить только поле класса (`static int soni`), но не локальную переменную.

## Разобранный пример 3: композиция — простая форма

На вводе *n* описаний компонентов: `belgi ТЕКСТ` (надпись), `maydon ИМЯ ШИРИНА` (поле) или `tugma ТЕКСТ` (кнопка). Соберите из них компонент `Forma` (форма) и нарисуйте его текстом: форма обведена рамкой, внутри каждый компонент на отдельной строке. Надпись — сам текст, поле — `ИМЯ: [____]` (ШИРИНА подчёркиваний), кнопка — `< ТЕКСТ >`. Ширина рамки выбирается по самой длинной строке.

```python
class Belgi:
    def __init__(self, matn):
        self.matn = matn

    def chiz(self):
        return self.matn


class Maydon:
    def __init__(self, nom, kenglik):
        self.nom, self.kenglik = nom, kenglik

    def chiz(self):
        return f"{self.nom}: [{'_' * self.kenglik}]"


class Tugma:
    def __init__(self, matn):
        self.matn = matn

    def chiz(self):
        return f"< {self.matn} >"


class Forma:
    def __init__(self):
        self.komponentlar = []              # композиция: в форме есть компоненты

    def qosh(self, komponent):
        self.komponentlar.append(komponent)

    def chiz(self):
        satrlar = [k.chiz() for k in self.komponentlar]
        en = max(len(s) for s in satrlar)
        chegara = "+" + "-" * (en + 2) + "+"
        ichki = [f"| {s.ljust(en)} |" for s in satrlar]
        return "\n".join([chegara] + ichki + [chegara])


forma = Forma()
n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "belgi":
        forma.qosh(Belgi(q[1]))
    elif q[0] == "maydon":
        forma.qosh(Maydon(q[1], int(q[2])))
    else:
        forma.qosh(Tugma(q[1]))
print(forma.chiz())
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    interface Komponent {
        String chiz();
    }

    record Belgi(String matn) implements Komponent {
        public String chiz() { return matn; }
    }

    record Maydon(String nom, int kenglik) implements Komponent {
        public String chiz() { return nom + ": [" + "_".repeat(kenglik) + "]"; }
    }

    record Tugma(String matn) implements Komponent {
        public String chiz() { return "< " + matn + " >"; }
    }

    static class Forma implements Komponent {
        private final List<Komponent> komponentlar = new ArrayList<>();   // композиция

        void qosh(Komponent k) { komponentlar.add(k); }

        public String chiz() {
            List<String> satrlar = new ArrayList<>();
            int en = 0;
            for (Komponent k : komponentlar) {
                String s = k.chiz();
                satrlar.add(s);
                en = Math.max(en, s.length());
            }
            String chegara = "+" + "-".repeat(en + 2) + "+";
            StringBuilder sb = new StringBuilder(chegara).append('\n');
            for (String s : satrlar) {
                sb.append("| ").append(s).append(" ".repeat(en - s.length())).append(" |\n");
            }
            return sb.append(chegara).toString();
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Forma forma = new Forma();
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].equals("belgi")) forma.qosh(new Belgi(q[1]));
            else if (q[0].equals("maydon")) forma.qosh(new Maydon(q[1], Integer.parseInt(q[2])));
            else forma.qosh(new Tugma(q[1]));
        }
        System.out.println(forma.chiz());
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <memory>
#include <string>
#include <vector>
using namespace std;

class Komponent {
public:
    virtual ~Komponent() = default;
    virtual string chiz() const = 0;
};

class Belgi : public Komponent {
    string matn;
public:
    Belgi(string matn) : matn(matn) {}
    string chiz() const override { return matn; }
};

class Maydon : public Komponent {
    string nom;
    int kenglik;
public:
    Maydon(string nom, int kenglik) : nom(nom), kenglik(kenglik) {}
    string chiz() const override { return nom + ": [" + string(kenglik, '_') + "]"; }
};

class Tugma : public Komponent {
    string matn;
public:
    Tugma(string matn) : matn(matn) {}
    string chiz() const override { return "< " + matn + " >"; }
};

class Forma : public Komponent {
    vector<unique_ptr<Komponent>> komponentlar;             // композиция
public:
    void qosh(unique_ptr<Komponent> k) { komponentlar.push_back(move(k)); }

    string chiz() const override {
        vector<string> satrlar;
        size_t en = 0;
        for (const auto& k : komponentlar) {
            satrlar.push_back(k->chiz());
            en = max(en, satrlar.back().size());
        }
        string chegara = "+" + string(en + 2, '-') + "+";
        string natija = chegara + "\n";
        for (const string& s : satrlar) {
            natija += "| " + s + string(en - s.size(), ' ') + " |\n";
        }
        return natija + chegara;
    }
};

int main() {
    Forma forma;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string tur, matn;
        cin >> tur >> matn;
        if (tur == "belgi") {
            forma.qosh(make_unique<Belgi>(matn));
        } else if (tur == "maydon") {
            int kenglik;
            cin >> kenglik;
            forma.qosh(make_unique<Maydon>(matn, kenglik));
        } else {
            forma.qosh(make_unique<Tugma>(matn));
        }
    }
    cout << forma.chiz() << endl;
    return 0;
}
```

Ввод (в именах только ASCII-буквы: `size()` в C++ считает байты, а символы вроде `ʻ` сдвинут рамку):

```text
4
belgi Royxat
maydon Ism 10
maydon Parol 8
tugma Yuborish
```

Вывод:

```text
+-------------------+
| Royxat            |
| Ism: [__________] |
| Parol: [________] |
| < Yuborish >      |
+-------------------+
```

В Java и C++ `Forma` сама является `Komponent`: её можно вложить в другую форму — это известный шаблон проектирования «Компоновщик» (Composite). Форма не знает, как рисуется каждый компонент, — она лишь вызывает `chiz()` (нарисовать) и помещает результаты в рамку. Если добавить новый тип компонента (например, флажок), код формы не изменится.

:::warning
**Частые ошибки**

- **Зависимость от внутреннего устройства компонента.** Прямое обращение к полям другого компонента делает его незаменимым; пользуйтесь только методами интерфейса.
- **Долгая работа внутри обработчика события.** В графическом приложении долгое вычисление в callback «замораживает» всё окно: цикл событий стоит.
- **Вызов обработчика вместо передачи.** В Tkinter `command=saqlash()` сразу вызывает функцию и подключает её результат (`None`); пишите `command=saqlash`.
- **Всё через наследование.** «Кнопка является окном» — неверно; в окне *есть* кнопка — используйте композицию.
:::

:::tip
Проектируя новый компонент, сначала запишите его **интерфейс**: какие у него будут методы, свойства и события. Когда интерфейс готов, код, использующий компонент, можно писать и проверять ещё до того, как написана его внутренняя часть.
:::

## Итоги

- Компонент — часть программы с чёткой задачей, подключаемая и заменяемая через интерфейс.
- Код, написанный под интерфейс, не знает, какой компонент подключён; создание и использование компонента — в разных местах.
- У графических компонентов есть свойства (состояние), методы (команды) и события (то, что произошло).
- Обработчики событий (callback, listener) подключаются к компоненту и вызываются при событии; графическая программа работает в цикле событий.
- Сложный компонент собирается из простых с помощью композиции.

## Проверьте себя

:::check
Q: Зачем компоненту интерфейс?
A: Чтобы использующий компонент код опирался только на контракт и не менялся при замене компонента другим.
Q: Чем являются члены кнопки `Text`, `Click` и `Focus()` соответственно?
A: `Text` — свойство, `Click` — событие, `Focus()` — метод.
Q: Что такое callback (слушатель)?
A: Функция, подключённая к компоненту и вызываемая им, когда происходит определённое событие.
Q: Что делает цикл событий?
A: Ждёт действий пользователя и для каждого события вызывает подключённые к нему функции.
Q: Чем композиция отличается от наследования?
A: Композиция — отношение «имеет»: объект содержит другие объекты и работает с ними через интерфейс; наследование — отношение «является».
:::

:::link https://docs.python.org/3/library/tkinter.html
Python: tkinter
Стандартная библиотека графического интерфейса Python — компоненты, события и размещение (на английском).
:::

:::link https://learn.microsoft.com/en-us/dotnet/desktop/winforms/controls/
Windows Forms: элементы управления
Компоненты в Visual Studio, их свойства и события (на английском).
:::
