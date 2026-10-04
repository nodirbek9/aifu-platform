---
title: Working with components
summary: The idea of a software component; an interface as a component's contract, interchangeable components; properties, methods and events; functions that respond to events (callbacks, listeners); graphical interface components and their layout; composition.
---

## Why it matters

A modern program is not one solid piece of code but is assembled from **components** that plug into one another: a button, a text field, a payment module, a database connection, a messaging service. Each component knows its own job and talks to the others only through a clearly defined **interface**. Thanks to this, replacing a component (one payment system with another, say) is done without changing the rest of the program.

In environments such as Visual Studio, IntelliJ or Qt Creator a graphical application is built on this principle: ready components (a button, a list, a table) are dropped onto a window, their **properties** are set, and functions responding to their **events** are written. In this lecture we look at the main ideas of component-based programming in three languages.

:::note
**After this lesson you will be able to:**

- explain what a software component and its interface are;
- write components that are interchangeable through an interface;
- tell a component's properties, methods and events apart;
- register functions that respond to events (callbacks, listeners) and call them;
- assemble a complex component from simple ones by composition.
:::

## Component and interface

A **component** is a piece of a program with a clear job that can be replaced independently. It communicates with the outside world through an **interface** — a contract stating which methods exist and what they do. Its internal structure stays hidden (encapsulation).

| Language | Interface | Implementation |
|---|---|---|
| Java | `interface Chegirma { long hisobla(long summa); }` | `class Foizli implements Chegirma` |
| C++ | a class with pure virtual methods: `virtual long long hisobla(long long) const = 0;` | `class Foizli : public Chegirma` |
| Python | `abc.ABC` and `@abstractmethod`, or simply methods with the same name | `class Foizli(Chegirma)` |

Code written against the interface ("compute the order total using the discount component") does not know which component is plugged in — so it does not change when a new kind of discount is added (*Chegirma* means discount, *hisobla* compute, *Foizli* percentage-based).

## Properties, methods and events

Graphical interface components (a button, a text field, a label) have three kinds of members:

| Member | Meaning | Example for a button |
|---|---|---|
| Property | the component's state | `Text = "Save"`, `Enabled = false`, `Width = 120` |
| Method | a command to the component | `Focus()`, `Show()`, `Hide()` |
| Event | something happened to the component | `Click`, `MouseEnter`, `TextChanged` |

In **event-driven programming** the program does not "give orders" itself but waits for events: when the user clicks a button, the component calls all the functions **registered** with it. Such a function is called a **callback** or a **listener**.

| Language / library | Attaching an event handler |
|---|---|
| C# Windows Forms | `button1.Click += Button1_Click;` |
| Java Swing | `tugma.addActionListener(e -> saqlash());` |
| Python Tkinter | `Button(oyna, text="Save", command=saqlash)` |
| C++ Qt | `connect(tugma, &QPushButton::clicked, this, &Oyna::saqlash);` |

To store callbacks, languages use functions as values: lambdas and functional interfaces (`Runnable`, `Consumer<T>`) in Java, `std::function` in C++, an ordinary function or a `lambda` in Python.

## A graphical interface sample

For example, in Python Tkinter a program with one window, a text field and a button is written like this (it needs a graphical environment, so it is not run by the automatic check):

```text
import tkinter as tk

def salomlash():
    belgi.config(text="Hello, " + maydon.get() + "!")   # changing a property

oyna = tk.Tk()
maydon = tk.Entry(oyna)
tugma = tk.Button(oyna, text="Greet", command=salomlash)   # attaching to the event
belgi = tk.Label(oyna, text="")
for komponent in (maydon, tugma, belgi):
    komponent.pack()                                   # layout
oyna.mainloop()                                        # the event loop
```

`mainloop()` is the **event loop**: here the program waits for user actions and calls the matching callback for every event. All graphical libraries work on this scheme (*oyna* is window, *maydon* field, *tugma* button, *belgi* label).

## Composition

A complex component is **assembled** from simple ones: a window consists of panels, a panel of buttons and fields. This is **composition** — the "has a" relation (a window has a button). Unlike inheritance, in composition a component does not depend on another's internal structure, and they can even be swapped while the program runs. A well-known rule among programmers: "favour composition over inheritance".

## Worked example 1: an interchangeable discount component

The first line holds the kind of discount: `yoq` (none), `foiz P` (percent) or `qatiy S` (fixed) — P per cent of the total or S sum off, but the total never goes negative. The second line holds *n*, the third *n* order totals. Print the final total of every order. The computing code knows only the `Chegirma` interface.

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


def chegirma_yarat(satr):                  # this is where the component is chosen
    q = satr.split()
    if q[0] == "foiz":
        return Foizli(int(q[1]))
    if q[0] == "qatiy":
        return Qatiy(int(q[1]))
    return Yoq()


chegirma = chegirma_yarat(input())
n = int(input())
for summa in map(int, input().split()):
    print(chegirma.hisobla(summa))         # this line does not know the type
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

    static Chegirma chegirmaYarat(String satr) {   // this is where the component is chosen
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
            System.out.println(chegirma.hisobla(in.nextLong()));   // this line does not know the type
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

unique_ptr<Chegirma> chegirmaYarat(const string& satr) {   // this is where the component is chosen
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
        cout << chegirma->hisobla(summa) << endl;   // this line does not know the type
    }
    return 0;
}
```

Input:

```text
foiz 15
3
100000 250000 40000
```

Output:

```text
85000
212500
34000
```

The main loop only calls the `hisobla` method. If tomorrow a new discount such as "50 % off the second order" is needed, a new class is written and one line is added to `chegirma_yarat` — the loop does not change. The component is created in one place (here `chegirma_yarat`) and used in another — this is the main rule of component design. With `qatiy 50000`, a total of 40000 becomes 0, not negative.

## Worked example 2: events and listeners

Several listeners are attached to a `Tugma` (button) component: a counter (how many times it was clicked), a log (records every click) and a warner (says `Be careful!` after the 3rd click). The input holds *n* events: `bos NAME` (the user NAME clicked) or `holat` (status). For `bos` the listeners are called in the order they were attached; for `holat` the total number of clicks is printed.

```python
class Tugma:
    def __init__(self, matn):
        self.matn = matn                    # a property
        self._tinglovchilar = []

    def bosilganda(self, funksiya):         # attaching an event handler
        self._tinglovchilar.append(funksiya)

    def bos(self, kim):                     # the event happened
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
        print("Be careful!")


tugma = Tugma("Save")
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
        final String matn;                                  // a property
        private final List<Consumer<String>> tinglovchilar = new ArrayList<>();

        Tugma(String matn) { this.matn = matn; }

        void bosilganda(Consumer<String> funksiya) {        // attaching an event handler
            tinglovchilar.add(funksiya);
        }

        void bos(String kim) {                              // the event happened
            for (Consumer<String> funksiya : tinglovchilar) funksiya.accept(kim);
        }
    }

    static int soni = 0;

    public static void main(String[] args) {
        Tugma tugma = new Tugma("Save");
        tugma.bosilganda(kim -> soni++);
        tugma.bosilganda(kim -> System.out.println(kim + ": " + tugma.matn));
        tugma.bosilganda(kim -> {
            if (soni > 3) System.out.println("Be careful!");
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
    string matn;                                            // a property

    Tugma(string matn) : matn(matn) {}

    void bosilganda(function<void(const string&)> funksiya) {   // attaching an event handler
        tinglovchilar.push_back(funksiya);
    }

    void bos(const string& kim) {                           // the event happened
        for (auto& funksiya : tinglovchilar) funksiya(kim);
    }
};

int main() {
    int soni = 0;
    Tugma tugma("Save");
    tugma.bosilganda([&](const string&) { soni++; });
    tugma.bosilganda([&](const string& kim) { cout << kim << ": " << tugma.matn << endl; });
    tugma.bosilganda([&](const string&) {
        if (soni > 3) cout << "Be careful!" << endl;
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

Input:

```text
6
bos Aziz
bos Malika
holat
bos Aziz
bos Bekzod
holat
```

Output:

```text
Aziz: Save
Malika: Save
2
Aziz: Save
Bekzod: Save
Be careful!
4
```

`Tugma` itself computes and prints nothing — it only calls the attached functions, saying "I was clicked". If new behaviour is needed, the button class does not change; a new listener is simply attached (*tinglovchilar* are the listeners, *bosilganda* means "when clicked"). In C++ `[&]` makes the lambda capture the outer variables (`soni`, `tugma`) by reference; in Java a lambda can change only a class field (`static int soni`), not a local variable.

## Worked example 3: composition — a simple form

The input holds *n* component descriptions: `belgi TEXT` (label), `maydon NAME WIDTH` (field) or `tugma TEXT` (button). Assemble a `Forma` (form) component from them and draw it as text: the form is enclosed in a frame, with every component on its own line inside. A label is its text, a field is `NAME: [____]` (WIDTH underscores), a button is `< TEXT >`. The frame's width follows the longest line.

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
        self.komponentlar = []              # composition: the form has components

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
        private final List<Komponent> komponentlar = new ArrayList<>();   // composition

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
    vector<unique_ptr<Komponent>> komponentlar;             // composition
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

Input (ASCII letters only in names: C++'s `size()` counts bytes, and characters such as `ʻ` would shift the frame):

```text
4
belgi Royxat
maydon Ism 10
maydon Parol 8
tugma Yuborish
```

Output:

```text
+-------------------+
| Royxat            |
| Ism: [__________] |
| Parol: [________] |
| < Yuborish >      |
+-------------------+
```

In Java and C++ `Forma` is itself a `Komponent`: it can be put inside another form — this is the well-known design pattern called "Composite". The form does not know how each component is drawn; it only calls `chiz()` (draw) and places the results in a frame. If a new component type (say, a checkbox) is added, the form's code does not change.

:::warning
**Common mistakes**

- **Depending on a component's internals.** Reaching into another component's fields directly makes it impossible to replace; use only the interface methods.
- **Long work inside an event handler.** In a graphical application, a long computation in a callback "freezes" the whole window: the event loop is stuck waiting.
- **Calling a callback instead of passing it.** In Tkinter `command=saqlash()` calls the function at once and attaches its result (`None`); write `command=saqlash`.
- **Doing everything with inheritance.** "A button is a window" is false; a window *has* a button — use composition.
:::

:::tip
When designing a new component, first write its **interface**: which methods, properties and events it will have. Once the interface is ready, the code that uses the component can be written and tested even before its internals exist.
:::

## Summary

- A component is a piece of a program with a clear job, plugged in and replaced through an interface.
- Code written against an interface does not know which component is plugged in; creating and using the component happen in different places.
- Graphical components have properties (state), methods (commands) and events (things that happened).
- Event handlers (callbacks, listeners) are attached to a component and called when the event happens; a graphical program runs in an event loop.
- A complex component is assembled from simple ones by composition.

## Check yourself

:::check
Q: Why does a component need an interface?
A: So that code using the component relies only on the contract and does not change when the component is replaced by another.
Q: What are a button's `Text`, `Click` and `Focus()` members, respectively?
A: `Text` is a property, `Click` an event, `Focus()` a method.
Q: What is a callback (listener)?
A: A function attached to a component and called by the component when a certain event happens.
Q: What does the event loop do?
A: It waits for user actions and, for every event, calls the functions attached to it.
Q: How does composition differ from inheritance?
A: Composition is the "has a" relation: an object contains other objects and works with them through their interface; inheritance is the "is a" relation.
:::

:::link https://docs.python.org/3/library/tkinter.html
Python: tkinter
Python's standard GUI library — components, events and layout.
:::

:::link https://learn.microsoft.com/en-us/dotnet/desktop/winforms/controls/
Windows Forms: controls
Components in Visual Studio, their properties and events.
:::
