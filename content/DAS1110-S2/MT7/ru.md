---
title: Организация взаимосвязанных классов
summary: Самостоятельная работа: связи между классами — ассоциация, агрегация и композиция; распределение ответственности между классами, закрытые поля и проверяющие методы, обработка потока команд; образец — книга и библиотека; задание — класс банковского счёта.
---

## Почему это важно

В реальной программе классы не живут поодиночке: в библиотеке есть книги, в группе — студенты, в заказе — товары. Каждый класс отвечает за **своё дело** и общается с другими только через методы. Правильно распределить ответственность между классами — главный навык объектно-ориентированного проектирования. В этой самостоятельной работе вы построите небольшую систему из двух связанных классов и будете управлять ею потоком команд.

:::note
**После этой самостоятельной работы вы сможете:**

- различать виды связей между классами (ассоциация, агрегация, композиция);
- определять ответственность каждого класса и помещать поля и методы в нужный класс;
- писать методы, защищающие состояние объекта от неверных изменений;
- строить программу, обрабатывающую поток текстовых команд.
:::

## Краткое повторение темы

| Связь | Смысл | Пример |
|---|---|---|
| Ассоциация | один объект использует другой | преподаватель ведёт курс |
| Агрегация | «имеет», но части живут независимо | в группе есть студенты (если группу расформируют, студенты останутся) |
| Композиция | «имеет», части создаются и исчезают вместе с целым | в книге есть страницы |

Правила проектирования:

1. **Каждый элемент данных хранится в одном классе**: число экземпляров книги — в `Kitob` (книга), список книг — в `Kutubxona` (библиотека).
2. **Состояние меняет только владелец**: `Kutubxona` не пишет напрямую в поле книги `berilgan` (выдано), а вызывает `kitob.ber()`.
3. **Метод возвращает результат**: если действие невозможно (`false`), решает вызывающий — вывод сообщения — дело основной программы.
4. Поток команд: каждая строка — команда и аргументы; `if`/`elif` или `switch` вызывает нужный метод.

## Образец: книга и библиотека

В первой строке *n*, в следующих *n* строках — команды:

- `qosh НАЗВАНИЕ КОЛИЧЕСТВО` — добавить в библиотеку КОЛИЧЕСТВО экземпляров книги (ответ не выводится);
- `ber НАЗВАНИЕ` — выдать один экземпляр: если можно — `выдана`, иначе `нет`;
- `qaytar НАЗВАНИЕ` — принять экземпляр: если экземпляр этой книги был выдан — `принята`, иначе `ошибка`;
- `holat НАЗВАНИЕ` — `НАЗВАНИЕ: ДОСТУПНО/ВСЕГО` (если такой книги вообще нет — `НАЗВАНИЕ: 0/0`).

```python
class Kitob:
    def __init__(self, nom):
        self.nom = nom
        self._jami = 0
        self._berilgan = 0

    def nusxa_qosh(self, soni):
        self._jami += soni

    def ber(self):                          # состояние меняет только Kitob
        if self._berilgan == self._jami:
            return False
        self._berilgan += 1
        return True

    def qaytar(self):
        if self._berilgan == 0:
            return False
        self._berilgan -= 1
        return True

    def holat(self):
        return f"{self.nom}: {self._jami - self._berilgan}/{self._jami}"


class Kutubxona:
    def __init__(self):
        self._kitoblar = {}                 # агрегация: в библиотеке есть книги

    def qosh(self, nom, soni):
        if nom not in self._kitoblar:
            self._kitoblar[nom] = Kitob(nom)
        self._kitoblar[nom].nusxa_qosh(soni)

    def ber(self, nom):
        return nom in self._kitoblar and self._kitoblar[nom].ber()

    def qaytar(self, nom):
        return nom in self._kitoblar and self._kitoblar[nom].qaytar()

    def holat(self, nom):
        if nom not in self._kitoblar:
            return f"{nom}: 0/0"
        return self._kitoblar[nom].holat()


kutubxona = Kutubxona()
n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "qosh":
        kutubxona.qosh(q[1], int(q[2]))
    elif q[0] == "ber":
        print("выдана" if kutubxona.ber(q[1]) else "нет")
    elif q[0] == "qaytar":
        print("принята" if kutubxona.qaytar(q[1]) else "ошибка")
    else:
        print(kutubxona.holat(q[1]))
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static class Kitob {
        private final String nom;
        private int jami = 0, berilgan = 0;

        Kitob(String nom) { this.nom = nom; }

        void nusxaQosh(int soni) { jami += soni; }

        boolean ber() {                     // состояние меняет только Kitob
            if (berilgan == jami) return false;
            berilgan++;
            return true;
        }

        boolean qaytar() {
            if (berilgan == 0) return false;
            berilgan--;
            return true;
        }

        String holat() { return nom + ": " + (jami - berilgan) + "/" + jami; }
    }

    static class Kutubxona {
        private final Map<String, Kitob> kitoblar = new HashMap<>();   // агрегация

        void qosh(String nom, int soni) {
            kitoblar.computeIfAbsent(nom, Kitob::new).nusxaQosh(soni);
        }

        boolean ber(String nom) {
            Kitob k = kitoblar.get(nom);
            return k != null && k.ber();
        }

        boolean qaytar(String nom) {
            Kitob k = kitoblar.get(nom);
            return k != null && k.qaytar();
        }

        String holat(String nom) {
            Kitob k = kitoblar.get(nom);
            return k == null ? nom + ": 0/0" : k.holat();
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Kutubxona kutubxona = new Kutubxona();
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String buyruq = in.next(), nom = in.next();
            switch (buyruq) {
                case "qosh" -> kutubxona.qosh(nom, in.nextInt());
                case "ber" -> System.out.println(kutubxona.ber(nom) ? "выдана" : "нет");
                case "qaytar" -> System.out.println(kutubxona.qaytar(nom) ? "принята" : "ошибка");
                default -> System.out.println(kutubxona.holat(nom));
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

class Kitob {
    string nom;
    int jami = 0, berilgan = 0;

public:
    Kitob(string nom = "") : nom(nom) {}

    void nusxaQosh(int soni) { jami += soni; }

    bool ber() {                            // состояние меняет только Kitob
        if (berilgan == jami) return false;
        berilgan++;
        return true;
    }

    bool qaytar() {
        if (berilgan == 0) return false;
        berilgan--;
        return true;
    }

    string holat() const { return nom + ": " + to_string(jami - berilgan) + "/" + to_string(jami); }
};

class Kutubxona {
    map<string, Kitob> kitoblar;            // агрегация

public:
    void qosh(const string& nom, int soni) {
        if (!kitoblar.count(nom)) kitoblar[nom] = Kitob(nom);
        kitoblar[nom].nusxaQosh(soni);
    }

    bool ber(const string& nom) {
        auto it = kitoblar.find(nom);
        return it != kitoblar.end() && it->second.ber();
    }

    bool qaytar(const string& nom) {
        auto it = kitoblar.find(nom);
        return it != kitoblar.end() && it->second.qaytar();
    }

    string holat(const string& nom) const {
        auto it = kitoblar.find(nom);
        return it == kitoblar.end() ? nom + ": 0/0" : it->second.holat();
    }
};

int main() {
    Kutubxona kutubxona;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string buyruq, nom;
        cin >> buyruq >> nom;
        if (buyruq == "qosh") {
            int soni;
            cin >> soni;
            kutubxona.qosh(nom, soni);
        } else if (buyruq == "ber") {
            cout << (kutubxona.ber(nom) ? "выдана" : "нет") << endl;
        } else if (buyruq == "qaytar") {
            cout << (kutubxona.qaytar(nom) ? "принята" : "ошибка") << endl;
        } else {
            cout << kutubxona.holat(nom) << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
8
qosh Sarob 2
ber Sarob
ber Sarob
ber Sarob
holat Sarob
qaytar Sarob
qaytar Alpomish
holat Alpomish
```

Вывод:

```text
выдана
выдана
нет
Sarob: 0/2
принята
ошибка
Alpomish: 0/0
```

`Kutubxona` сама не считает экземпляры — она лишь находит нужную `Kitob` и вызывает её метод. `Kitob`, в свою очередь, не выводит сообщений, а лишь возвращает `true`/`false`; текст выбирает основная программа. Благодаря этому `Kitob` можно без изменений использовать в другой программе (например, с графическим интерфейсом). В Java `computeIfAbsent(nom, Kitob::new)` — краткая запись «если такой книги нет, создать новую» (*jami* — всего, *berilgan* — выдано).

## Задание самостоятельной работы

Решите задание ниже: создайте класс `BankAccount` — поле баланса **закрыто** (private) и управляется только методами `deposit`, `withdraw` и `balance`; если средств недостаточно, снятие отклоняется. Обработайте поток команд. Что сдаётся:

1. **Диаграмма класса** — поля (с модификатором доступа) и методы (с параметрами и возвращаемым значением); какую проверку выполняет каждый метод.
2. **Программа** — на Python, Java или C++; отправляется в окне задания ниже и должна пройти автоматические тесты.
3. **Таблица тестов** — не менее 5 последовательностей команд: обычное пополнение и снятие, снятие больше баланса, снятие ровно баланса, `balance` в начальном состоянии, большие суммы.
4. **Вывод** — 3–5 предложений: почему поле баланса должно быть закрытым и от каких ошибок это защищает.

:::warning
**Критерии оценки и частые ошибки**

- Если к полю баланса обращаются напрямую извне класса (`hisob.balans -= x`), работа не получает полного балла.
- При отклонённом снятии баланс **не должен меняться**.
- Суммы до 10⁹, команд до 1000: баланс может не поместиться в `int` — используйте `long` / `long long`.
- Сообщение пусть выводит основная программа, а не метод класса: метод возвращает результат.
:::

:::tip
Написав класс, сначала проверьте его без потока команд несколькими прямыми вызовами: `deposit(100)`, `withdraw(150)`, `balance()`. Если класс работает, часть, читающая команды, становится простой.
:::

## Итоги

- Классы связываются ассоциацией, агрегацией или композицией.
- Каждый элемент данных хранится в одном классе и меняется только через методы этого класса.
- Методы возвращают результат; вывод сообщения пользователю — задача основной программы.
- Поток команд обрабатывается вызовом нужного метода по имени команды.

## Проверьте себя

:::check
Q: В чём разница между агрегацией и композицией?
A: При агрегации части живут независимо от целого; при композиции они создаются и исчезают вместе с ним.
Q: Почему `Kutubxona` не меняет поле книги `berilgan` напрямую?
A: Состояние должен менять только владелец — `Kitob`; тогда проверка находится в одном месте, и книга не попадёт в неверное состояние.
Q: Почему `Kitob.ber()` возвращает `true`/`false`, а не выводит сообщение?
A: Текст сообщения зависит от программы; метод, возвращающий результат, можно использовать в любой программе (консоль, графический интерфейс).
Q: Почему в образце третья команда `ber Sarob` ответила `нет`?
A: Оба экземпляра уже были выданы: `berilgan == jami`.
:::

:::link https://dev.java/learn/classes-objects/
Java: классы и объекты
Официальный учебный ресурс о проектировании классов, модификаторах доступа и методах (на английском).
:::
