---
title: Encapsulation and inheritance. Polymorphism
summary: The three principles of OOP; private fields, getters and setters; a derived class, super and the chain of constructors; overriding a method, virtual methods and different objects in one list; abstract classes.
---

## Why it matters

In the previous two lectures we wrote classes and kept objects in an array. But the fields of our classes were open to everyone: any code could write `ball = -500` into a student. Besides, for similar classes (say, an ordinary employee and a manager) we had to write the same code twice.

OOP solves these problems with three principles: **encapsulation** protects an object from an invalid state, **inheritance** keeps shared code in one place, and **polymorphism** lets us work with different objects in the same way. These three principles underlie any large program — from a mobile app to a banking system.

:::note
**After this lesson you will be able to:**

- explain the three principles of OOP in your own words;
- hide fields and manage them through a getter and a validating setter;
- write a derived class and call the base class constructor and methods through `super`;
- override a method and apply polymorphism to different objects in one list;
- explain why `virtual` is needed in C++ and what an abstract class is.
:::

## Encapsulation

**Encapsulation** means hiding an object's internal data from the outside and allowing access to it only through the class's methods. Then the object always stays in a **valid state**: the method does the checking.

| Language | Private field | Public method |
|---|---|---|
| Java | `private int ball;` | `public int getBall()`, `public void setBall(int b)` |
| C++ | in the `private:` section (the default in a `class`) | in the `public:` section |
| Python | a convention: `self._ball` (one underscore) — "internal, don't touch" | an ordinary method or `@property` |

A **getter** returns a field's value, and a **setter** sets a new value **after checking** it. Python has no `private` keyword: the underscore is a convention among programmers that the language does not enforce.

Access modifiers (Java and C++):

| Modifier | Who sees it |
|---|---|
| `private` | only the class itself |
| `protected` | the class and its subclasses (in Java also the same package) |
| `public` | everyone |

## Inheritance

**Inheritance** creates a new class (the **derived** class, the subclass) based on an existing one (the **base** class, the parent). The subclass gets all the parent's fields and methods and adds its own. This is the "**is a**" relation: a manager is an employee, a circle is a shape.

| | Python | Java | C++ |
|---|---|---|---|
| Declaring a subclass | `class Menejer(Xodim):` | `class Menejer extends Xodim` | `class Menejer : public Xodim` |
| The parent constructor | `super().__init__(ism, maosh)` | `super(ism, maosh);` (the first line) | `: Xodim(ism, maosh)` (in the initializer list) |
| A parent method | `super().oylik()` | `super.oylik()` | `Xodim::oylik()` |

When a subclass object is created, **the parent part is built first**, then the subclass part — that is why the parent constructor is called first.

## Polymorphism

**Polymorphism** ("many forms") means the same call works differently depending on the object's actual type. A subclass **overrides** a parent method: the same name and parameters, a different body.

The power of polymorphism is that different subclass objects can be kept in one list **of the parent type**, and the same method called on all of them — each runs its own version:

```text
shakllar = [Doira(2), Tortburchak(3, 4), Uchburchak(6, 2)]
for each s: s.yuza()     → each with its own formula
```

The languages differ:

- In **Python and Java** all methods are polymorphic by default. In Java the `@Override` annotation catches mistakes: if a method name is misspelled, the compiler warns.
- In **C++** the method must be declared `virtual` in the base class, and objects must be used through a **pointer** or a **reference**. Otherwise the parent version is always called. Writing `override` in the subclass is recommended.

An **abstract class** is a class you cannot create objects of; it is only a template for subclasses. Its abstract methods have no body, and every subclass must define them: `abstract` in Java, `virtual double yuza() const = 0;` in C++ (a pure virtual method), `@abstractmethod` from the `abc` module in Python.

## Worked example 1: a temperature class (encapsulation)

The first line holds *n*, then *n* integers — temperatures in Celsius. In the `Harorat` (temperature) class the value is kept in a private field; the setter rejects a value below absolute zero (−273 °C), and the old value is kept. Call the setter for every number: if it is accepted, print the temperature in Fahrenheit (with one decimal place); if rejected, print `Error`.

```python
class Harorat:
    def __init__(self):
        self._selsiy = 0                  # internal field: not touched from outside

    def selsiy(self):
        return self._selsiy

    def ornat(self, qiymat):
        if qiymat < -273:
            return False                  # an invalid value does not get in
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
        print("Error")
```
```java
import java.util.Scanner;

public class Main {
    static class Harorat {
        private int selsiy = 0;           // internal field: not touched from outside

        public int getSelsiy() {
            return selsiy;
        }

        public boolean setSelsiy(int qiymat) {
            if (qiymat < -273) return false;   // an invalid value does not get in
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
                System.out.println("Error");
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
    int selsiy = 0;                       // internal field: not touched from outside

public:
    int getSelsiy() const { return selsiy; }

    bool setSelsiy(int qiymat) {
        if (qiymat < -273) return false;  // an invalid value does not get in
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
            printf("Error\n");
        }
    }
    return 0;
}
```

Input:

```text
4
25
-300
-40
37
```

Output:

```text
77.0
Error
-40.0
98.6
```

`-300` was rejected and the object kept its previous value (25). In Java and C++ you cannot write `h.selsiy = -300;` — the compiler forbids access to a `private` field. The formula says `9 / 5.0`: `9 / 5` is integer division and would give 1.

## Worked example 2: employee and manager (inheritance)

The first line holds *n*, then *n* lines: `xodim NAME SALARY` (employee) or `menejer NAME SALARY PERCENT` (manager). A manager is an employee, but their monthly pay gets a bonus of PERCENT per cent of the salary. Print each employee's name and monthly pay, and finally the total payroll.

```python
class Xodim:
    def __init__(self, ism, maosh):
        self.ism = ism
        self.maosh = maosh

    def oylik(self):
        return self.maosh


class Menejer(Xodim):
    def __init__(self, ism, maosh, foiz):
        super().__init__(ism, maosh)      # build the parent part
        self.foiz = foiz

    def oylik(self):                      # override
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
            super(ism, maosh);            // build the parent part
            this.foiz = foiz;
        }

        @Override
        long oylik() {                    // override
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
        : Xodim(ism, maosh), foiz(foiz) {}   // build the parent part

    long long oylik() const override {       // override
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

Input:

```text
3
xodim Aziz 5000000
menejer Malika 8000000 25
xodim Bekzod 4500000
```

Output:

```text
Aziz 5000000
Malika 10000000
Bekzod 4500000
19500000
```

`Menejer` did not rewrite the name and salary fields — it inherited them from `Xodim`. Inside `oylik()` (monthly pay) the parent version was called (`super().oylik()`, `super.oylik()`, `Xodim::oylik()`) and the bonus added to it. The fields are `protected`: visible to the subclass, not to outside code. In C++ keeping objects of different types in one `vector` takes **pointers**; `unique_ptr` is a "smart" pointer that deletes the object automatically (dynamic memory comes in detail in later topics). The `virtual ~Xodim()` in the base class makes sure a subclass object is deleted correctly (*fond* is the payroll).

## Worked example 3: shapes (polymorphism and an abstract class)

The first line holds *n*, then *n* lines: `doira R` (circle), `tortburchak A B` (rectangle) or `uchburchak BASE HEIGHT` (triangle). Print each shape's kind and area (with two decimal places), and finally the kind of the shape with the largest area. `Shakl` (shape) is an abstract class.

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
    print(f"{s.nom()} {s.yuza():.2f}")    # which yuza() runs is decided by the object's type
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
            System.out.printf("%s %.2f%n", s.nom(), s.yuza());   // which yuza() runs is decided by the object's type
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
    virtual double yuza() const = 0;      // pure virtual: Shakl is abstract
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
        printf("%s %.2f\n", s->nom().c_str(), s->yuza());   // which yuza() runs is decided by the object's type
        if (s->yuza() > eng->yuza()) eng = s.get();
    }
    printf("%s\n", eng->nom().c_str());
    return 0;
}
```

Input:

```text
3
doira 2
tortburchak 3 4
uchburchak 6 5
```

Output:

```text
doira 12.57
tortburchak 12.00
uchburchak 15.00
uchburchak
```

The main loop knows only about `Shakl`: it has no checks like `if (tur == "doira")`. If a `Kvadrat` (square) class is added tomorrow, the loop need not change — that is the main benefit of polymorphism. Since `Shakl` is abstract, `Shakl()` (Python), `new Shakl()` (Java) or `Shakl s;` (C++) is an error. The `override` keyword in a subclass makes the compiler check that the parent really has such a virtual method: if the name or parameters are wrong, the program does not compile.

:::warning
**Common mistakes**

- **No check in the setter.** `setBall(int b) { ball = b; }` is the same as leaving the field open; the point of encapsulation is the check.
- **Not putting `super(...)` on the first line in Java.** The parent constructor must be the first statement of the subclass constructor.
- **Overriding without `virtual` in C++.** Called through a pointer, the parent version runs; write `virtual` and `override`.
- **Copying a subclass object into a parent-type value in C++.** `Xodim x = menejer;` "slices off" the bonus part (slicing); use a pointer or a reference.
- **Using inheritance where there is no "is a".** A car has an engine, but it is *not* an engine — the engine is then a field, not a parent class.
:::

:::tip
To decide whether a new class should inherit from a parent, ask yourself "is X a Y?". If yes — inheritance; if "X has a Y" — a field (composition).
:::

## Summary

- Encapsulation: fields are private, changes go through validating methods; the object is always in a valid state.
- Inheritance: a subclass gets the parent's fields and methods; the parent constructor is called first through `super`.
- Polymorphism: a subclass overrides a method; in a list of the parent type every object runs its own version.
- In C++ polymorphism needs `virtual` methods and a pointer/reference; the base class gets a virtual destructor.
- No objects are created from an abstract class; it sets the methods its subclasses must implement.

## Check yourself

:::check
Q: What does encapsulation protect an object from?
A: From an invalid state: fields change only through methods that do the checking.
Q: Who sees a `protected` field?
A: The class itself and its subclasses (in Java, also the classes of the same package).
Q: When a subclass object is created, which constructor runs first?
A: The parent class constructor, then the subclass constructor.
Q: In C++, if the parent method is not `virtual`, which version does `Xodim* x = new Menejer(...); x->oylik()` call?
A: The parent (`Xodim`) version.
Q: Can an object be created from an abstract class?
A: No; objects are created only from subclasses that define all its abstract methods.
:::

:::link https://dev.java/learn/inheritance/
Java: inheritance
The official learning resource on subclasses, `super`, overriding and abstract classes.
:::

:::link https://en.cppreference.com/w/cpp/language/virtual
C++: virtual functions
A reference on `virtual`, `override` and pure virtual functions.
:::
