---
title: Fundamentals of object-oriented programming
summary: Class and object, fields and methods, the constructor, self and this, turning an object into text, static fields, an object reference versus a copy; writing a class in three languages.
---

## Why it matters

Until now our programs were made of separate variables and functions. A student's name, grade and group sat in three separate variables, and the functions that processed them lived elsewhere in the program. As a program grows, it gets hard to track which data belongs to which function.

**Object-oriented programming** (OOP) joins data and the operations on it into one unit — an **object**. Java is built entirely on this idea, and in Python and C++ large programs are also mostly written with classes. This lecture and the next two are devoted to the basic concepts of OOP.

:::note
**After this lesson you will be able to:**

- tell the concepts of class and object apart;
- write a class with fields, methods and a constructor in Python, Java and C++;
- explain what `self` and `this` mean;
- write a method that turns an object into text;
- state the difference between a static field and an object's field;
- explain how assigning an object (`b = a`) works in each language.
:::

## Class and object

A **class** is the description of a new type: what data every value of this type has and what it can do. An **object** is a concrete instance of that class.

| Concept | Meaning | Example (the `Talaba` — student — class) |
|---|---|---|
| Class | a template, a blueprint | `Talaba` |
| Object | a concrete instance of the class | the student "Aziz, 2nd year" |
| Field (attribute) | data the object keeps | `ism` (name), `kurs` (year) |
| Method | an action the object performs | `kursni_oshir()` (move up a year) |
| Constructor | a special method run when the object is created | gives the fields their initial values |

Any number of objects can be created from one class; each has its own fields, while the methods are shared.

## Writing a class in three languages

```python
class Talaba:
    def __init__(self, ism, kurs):     # constructor
        self.ism = ism                 # fields
        self.kurs = kurs

    def kursni_oshir(self):            # method
        self.kurs += 1

t = Talaba("Aziz", 2)
t.kursni_oshir()
print(t.ism, t.kurs)                   # Aziz 3
```

| | Python | Java | C++ |
|---|---|---|---|
| Constructor | `def __init__(self, ...)` | named after the class: `Talaba(String ism, int kurs)` | named after the class: `Talaba(string ism, int kurs)` |
| Declaring a field | in the constructor: `self.ism = ...` | in the class body: `String ism;` | in the class body: `string ism;` |
| The current object | `self` (the first parameter, written explicitly) | `this` (implicit) | `this` (a pointer, implicit) |
| Creating an object | `Talaba("Aziz", 2)` | `new Talaba("Aziz", 2)` | `Talaba t("Aziz", 2);` |
| Turning into text | `def __str__(self)` | `public String toString()` | write a `<<` operator or your own method |

**`self` and `this`** are the very object the method was called on. When `t.kursni_oshir()` is called, inside the method `self` (`this` in Java/C++) is exactly `t`. In Java and C++, when a field has the same name as a parameter, `this.ism = ism` tells them apart.

## Static fields

An ordinary field exists separately in every object. A **static field** (in Python, a class attribute) belongs to the class and is **one** for all objects. For example, to count how many students were created:

| Language | Static field | Access |
|---|---|---|
| Python | in the class body: `soni = 0` | `Talaba.soni` |
| Java | `static int soni = 0;` | `Talaba.soni` |
| C++ | `static int soni;` and outside the class `int Talaba::soni = 0;` (since C++17 `inline static int soni = 0;`) | `Talaba::soni` |

## Reference and copy

Assigning an object to another variable works **differently** across languages:

- **Python and Java**: a variable holds not the object itself but a **reference** to it. After `b = a`, `a` and `b` are one object; change `b.kurs` and `a.kurs` changes too.
- **C++**: `Talaba b = a;` creates a **copy** of the object; changing `b` does not affect `a`. If you need a reference — `Talaba& b = a;`.

So in Java or Python, if you pass an object to a function and change its field inside, the change is visible outside as well.

## Worked example 1: a rectangle class

A rectangle's width and height are given. Write a `Tortburchak` (rectangle) class: a constructor and the methods `yuza()` (area) and `perimetr()`. Print the area and the perimeter.

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

For the input `3 5` the output is `15 16`. In C++ `: en(en), boy(boy)` is an **initializer list**: the fields get their values before the constructor body runs. The `const` at the end of a method promises "this method does not change the object". In Java a `static class` is an independent class inside `Main`; we do this to write everything in one file.

## Worked example 2: a time class

Hours and minutes (`HH MM`) and a number *k* of minutes are given. Write a `Vaqt` (time) class: the method `qosh(k)` (add) moves the time *k* minutes forward (wrapping around midnight), and turning it into text gives the form `HH:MM`.

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

For the input `23 45` and `30` the output is `00:15`: 30 minutes after 23:45 we passed midnight. Inside the object the time is kept not as hours and minutes but as **minutes since midnight** (*daqiqalar*) — that makes the arithmetic simpler. This internal choice is invisible to whoever uses the class: they know only `qosh` and the text form (*matn*). In Python `print(v)` calls `__str__` automatically, in Java `println(v)` calls `toString()`.

## Worked example 3: a fraction class and a static counter

Two fractions `a b` and `c d` (`a/b` and `c/d`) are given. Write a `Kasr` (fraction) class: the constructor reduces the fraction, and the `qosh` method returns a new `Kasr` object. Print the sum and how many `Kasr` objects were created during the run.

```python
from math import gcd


class Kasr:
    soni = 0                        # static: one for all objects

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
        static int soni = 0;        // static: one for all objects
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
    inline static int soni = 0;     // static: one for all objects
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

For the input `1 6` and `1 3`: 1/6 + 1/3 = 3/18 + 6/18 = 9/18 = 1/2. The output is `1/2` and `3`: two fractions were read and a third was created inside `qosh`. Since `soni` (count) is static, all three objects increased one counter. `qosh` does not change the existing objects, it returns a **new** one — so `x` and `y` keep their values (*surat* is the numerator, *maxraj* the denominator). In C++ `gcd` is the ready function from `<numeric>` (C++17), and `inline static` lets a static field be initialised inside the class.

:::warning
**Common mistakes**

- **Forgetting `self` in a Python method.** With `def yuza():` the call `t.yuza()` fails; fields are also reached only through `self.en`.
- **The same name without `this` in Java/C++.** In a constructor `en = en;` assigns the parameter to itself and the field stays 0; write `this.en = en`.
- **An object without `new` in Java.** `Tortburchak t;` declares only a reference (`null`); the object is created with `new`.
- **Taking a reference for a copy.** In Python/Java `b = a` creates no new object; changing `b` changes `a` too.
:::

:::tip
When designing a class, first write on paper the answers to "what does this object **know**?" (fields) and "what can it **do**?" (methods), then move to code.
:::

## Summary

- A class describes a new type; an object is a concrete instance of it.
- Fields hold an object's data, methods its actions; the constructor puts the object into its initial state.
- `self` / `this` is the object the method was called on.
- A static field belongs to the class and is one for all objects.
- In Python and Java a variable holds a reference to an object; in C++ assignment makes a copy.

## Check yourself

:::check
Q: What is the difference between a class and an object?
A: A class is a type description (a blueprint), an object a concrete value created from that class; many objects can be created from one class.
Q: When does a constructor run?
A: When the object is being created, automatically and once.
Q: In Python, after `a = Talaba("Ali", 1)`, `b = a`, `b.kurs = 2`, what is `a.kurs`?
A: 2 — `a` and `b` refer to one object.
Q: In C++, after `Talaba b = a; b.kurs = 2;`, does `a.kurs` change?
A: No, `b` is a copy of `a`.
Q: How does a static field differ from an ordinary one?
A: It belongs to the class and is one for all objects; an ordinary field is separate in every object.
:::

:::link https://docs.python.org/3/tutorial/classes.html
Python: classes
The official tutorial on classes, objects, `__init__` and methods.
:::

:::link https://dev.java/learn/classes-objects/
Java: classes and objects
The official learning resource on classes, constructors and `this` in Java.
:::
