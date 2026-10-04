---
title: Organising interrelated classes
summary: Independent work: relations between classes — association, aggregation and composition; distributing responsibility among classes, private fields and validating methods, processing a stream of commands; the sample problem — a book and a library; the assignment — a bank account class.
---

## Why it matters

In a real program classes do not live alone: a library has books, a group has students, an order has products. Each class is responsible for **its own job** and talks to the others only through methods. Distributing responsibility among classes correctly is the core skill of object-oriented design. In this independent work you build a small system of two related classes and drive it with a stream of commands.

:::note
**After this independent work you will be able to:**

- tell the kinds of relations between classes apart (association, aggregation, composition);
- determine each class's responsibility and place fields and methods in the right class;
- write methods that protect an object's state from invalid changes;
- build a program that processes a stream of text commands.
:::

## A short review of the topic

| Relation | Meaning | Example |
|---|---|---|
| Association | one object uses another | a teacher teaches a course |
| Aggregation | "has a", but the parts live independently | a group has students (the students remain even if the group is dissolved) |
| Composition | "has a", the parts are created and destroyed together with the whole | a book has pages |

Design rules:

1. **Every piece of data lives in one class**: the number of copies of a book — in `Kitob` (book), the list of books — in `Kutubxona` (library).
2. **Only the owner changes the state**: `Kutubxona` does not write to a book's `berilgan` (lent) field directly, it calls `kitob.ber()`.
3. **A method returns a result**: if an action cannot be done (`false`), the caller decides what to do — printing a message is the main program's job.
4. A stream of commands: every line is a command and its arguments; `if`/`elif` or `switch` calls the matching method.

## The sample problem: a book and a library

The first line holds *n*, the next *n* lines commands:

- `qosh NAME COUNT` — add COUNT copies of a book to the library (no reply is printed);
- `ber NAME` — lend one copy: `issued` if possible, otherwise `unavailable`;
- `qaytar NAME` — take a copy back: `returned` if a copy of this book was lent, otherwise `error`;
- `holat NAME` — `NAME: AVAILABLE/TOTAL` (`NAME: 0/0` if there is no such book at all).

```python
class Kitob:
    def __init__(self, nom):
        self.nom = nom
        self._jami = 0
        self._berilgan = 0

    def nusxa_qosh(self, soni):
        self._jami += soni

    def ber(self):                          # only Kitob changes its state
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
        self._kitoblar = {}                 # aggregation: the library has books

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
        print("issued" if kutubxona.ber(q[1]) else "unavailable")
    elif q[0] == "qaytar":
        print("returned" if kutubxona.qaytar(q[1]) else "error")
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

        boolean ber() {                     // only Kitob changes its state
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
        private final Map<String, Kitob> kitoblar = new HashMap<>();   // aggregation

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
                case "ber" -> System.out.println(kutubxona.ber(nom) ? "issued" : "unavailable");
                case "qaytar" -> System.out.println(kutubxona.qaytar(nom) ? "returned" : "error");
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

    bool ber() {                            // only Kitob changes its state
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
    map<string, Kitob> kitoblar;            // aggregation

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
            cout << (kutubxona.ber(nom) ? "issued" : "unavailable") << endl;
        } else if (buyruq == "qaytar") {
            cout << (kutubxona.qaytar(nom) ? "returned" : "error") << endl;
        } else {
            cout << kutubxona.holat(nom) << endl;
        }
    }
    return 0;
}
```

Input:

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

Output:

```text
issued
issued
unavailable
Sarob: 0/2
returned
error
Alpomish: 0/0
```

`Kutubxona` does not count book copies itself — it only finds the right `Kitob` and calls its method. `Kitob`, in turn, prints no messages; it only returns `true`/`false`, and the main program chooses the text. Thanks to this `Kitob` can be used unchanged in another program (a graphical interface, say). In Java `computeIfAbsent(nom, Kitob::new)` is a short way to write "if there is no such book, create a new one" (*jami* is the total, *berilgan* the lent copies).

## The independent work assignment

Solve the assignment below: create a `BankAccount` class — its balance field is **private** and is managed only through the methods `deposit`, `withdraw` and `balance`; a withdrawal is refused if there are not enough funds. Process a stream of commands. What to hand in:

1. **A class diagram** — the fields (with access) and methods (with parameters and return values); which check each method performs.
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of tests** — at least 5 sequences of commands: an ordinary deposit and withdrawal, withdrawing more than the balance, withdrawing exactly the balance, `balance` in the initial state, large sums.
4. **A conclusion** — 3–5 sentences: why the balance field must be private and which mistakes this prevents.

:::warning
**Grading criteria and common mistakes**

- If the balance field is accessed directly from outside the class (`hisob.balans -= x`), the work does not get full marks.
- On a refused withdrawal the balance must **not change**.
- Sums are up to 10⁹ and commands up to 1000: the balance may not fit in an `int` — use `long` / `long long`.
- Let the main program, not a method of the class, print the message: the method returns a result.
:::

:::tip
Once the class is written, first test it without the command stream, with a few direct calls: `deposit(100)`, `withdraw(150)`, `balance()`. If the class works, the part that reads commands becomes simple.
:::

## Summary

- Classes are related by association, aggregation or composition.
- Every piece of data lives in one class and is changed only through that class's methods.
- Methods return a result; printing a message for the user is the main program's job.
- A stream of commands is processed by calling the matching method for each command name.

## Check yourself

:::check
Q: What is the difference between aggregation and composition?
A: In aggregation the parts live independently of the whole; in composition they are created and destroyed together with it.
Q: Why does `Kutubxona` not change a book's `berilgan` field directly?
A: Only the owner — `Kitob` — should change its state; then the check is in one place and the book never gets into an invalid state.
Q: Why does `Kitob.ber()` return `true`/`false` instead of printing a message?
A: The message text depends on the program; a method that returns a result can be reused in any program (console, graphical interface).
Q: In the sample, why did the third `ber Sarob` reply `unavailable`?
A: Both copies were already lent: `berilgan == jami`.
:::

:::link https://dev.java/learn/classes-objects/
Java: classes and objects
The official learning resource on designing classes, access and methods.
:::
