---
title: Arrays of objects
summary: Keeping objects in an array or list, null references in Java, changing objects in a loop (copy versus reference in C++), searching and aggregating by a field, the natural order of objects (__lt__, Comparable, operator<).
---

## Why it matters

One student object is useful, but a group has 25 students and a university thousands. Real programs usually work with **many objects**: a shop's products, a library's books, the classes in a timetable. The simplest way to keep them is an **array (list) of objects**.

In the previous lecture we created one class and a few objects. Now we put objects into an array and apply to them all the methods of the first semester — searching, summing, finding the largest, sorting. Along the way we see how an important difference between the languages — is an object a **copy** or a **reference**? — shows up in practice.

:::note
**After this lesson you will be able to:**

- create and fill an array (list) of objects in three languages;
- explain what `new Talaba[n]` creates in Java and why a `null` error appears;
- change objects correctly inside a loop (`auto&` in C++);
- search, sum and find the maximum by a field;
- give a class a natural order and sort objects with the standard function.
:::

## Creating an array of objects

| Language | Fixed size | Growing list |
|---|---|---|
| Python | — | `talabalar = []`, `talabalar.append(Talaba(...))` |
| Java | `Talaba[] t = new Talaba[n];` | `List<Talaba> t = new ArrayList<>();`, `t.add(new Talaba(...))` |
| C++ | `vector<Talaba> t(n);` (needs a default constructor) | `vector<Talaba> t;`, `t.push_back(Talaba(...))` or `t.emplace_back(...)` |

**The trap in Java.** `new Talaba[n]` does **not** create *n* objects — it creates *n* empty references (`null`). Every element is given an object separately: `t[i] = new Talaba(...)`. Without that, accessing `t[0].ism` throws a `NullPointerException`.

**The C++ peculiarity.** `vector<Talaba> t(n);` creates *n* objects at once, which needs a constructor without parameters (a default constructor) in the class. If there is only a constructor with parameters, create an empty `vector` and add with `push_back` or `emplace_back`.

## Iterating over objects

To work with every object in the array, use an ordinary loop or a "for each element" loop:

```text
Python:  for t in talabalar: ...
Java:    for (Talaba t : talabalar) { ... }
C++:     for (auto& t : talabalar) { ... }
```

In Python and Java `t` is a **reference** to the object in the array, so `t.ball += 5` changes the object in the array. In C++, however, `for (auto t : talabalar)` takes a **copy** of every object: the copy changes, and the array stays as it was. To change an object, write `auto&`; just to read, `const auto&` (no copy is made either).

## Searching and aggregating by a field

The array algorithms of the first semester work on an array of objects too — only it is not the element itself but its **field** that is compared:

| Problem | In an array of numbers | In an array of objects |
|---|---|---|
| Sum | `s += a[i]` | `s += t[i].narx * t[i].miqdor` |
| Maximum | `a[i] > eng` | `t[i].ball > eng.ball` (the best **object** is kept) |
| Search | `a[i] == x` | `t[i].ism.equals(x)` (Java), `t[i].ism == x` (Python, C++) |
| Count | `a[i] > 0` | `t[i].kurs == 2` |

When finding a maximum it is handy to remember not just the largest value but **the object itself** (or its index): then its other fields can be printed too.

## The natural order of objects

In the last lecture we gave a key (`key`) or a comparison function for sorting. If a class has a "usual" order, it can be set **in the class itself** — then sorting works without extra parameters:

| Language | What the class contains | Sorting |
|---|---|---|
| Python | `def __lt__(self, boshqa): return ...` | `sorted(a)`, `a.sort()` |
| Java | `implements Comparable<Kitob>` and `public int compareTo(Kitob b)` | `Collections.sort(a)`, `Arrays.sort(a)` |
| C++ | `bool operator<(const Kitob& b) const` | `sort(a.begin(), a.end())` |

In Java `compareTo` returns a negative number ("I come first"), zero ("we are equal") or a positive number; `Integer.compare(x, y)` and `String.compareTo` give a ready value.

## Worked example 1: products in a warehouse

The first line holds *n*, the next *n* lines a product's name, price and quantity. Print the total value of the warehouse (the sum of price × quantity) and the name of the product with the most valuable **stock**.

```python
class Mahsulot:
    def __init__(self, nom, narx, miqdor):
        self.nom = nom
        self.narx = narx
        self.miqdor = miqdor

    def qiymat(self):
        return self.narx * self.miqdor


n = int(input())
mahsulotlar = []
for _ in range(n):
    nom, narx, miqdor = input().split()
    mahsulotlar.append(Mahsulot(nom, int(narx), int(miqdor)))

jami = 0
eng = mahsulotlar[0]
for m in mahsulotlar:
    jami += m.qiymat()
    if m.qiymat() > eng.qiymat():
        eng = m

print(jami)
print(eng.nom)
```
```java
import java.util.Scanner;

public class Main {
    static class Mahsulot {
        String nom;
        long narx, miqdor;

        Mahsulot(String nom, long narx, long miqdor) {
            this.nom = nom;
            this.narx = narx;
            this.miqdor = miqdor;
        }

        long qiymat() {
            return narx * miqdor;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Mahsulot[] mahsulotlar = new Mahsulot[n];        // n nulls for now
        for (int i = 0; i < n; i++) {
            mahsulotlar[i] = new Mahsulot(in.next(), in.nextLong(), in.nextLong());
        }

        long jami = 0;
        Mahsulot eng = mahsulotlar[0];
        for (Mahsulot m : mahsulotlar) {
            jami += m.qiymat();
            if (m.qiymat() > eng.qiymat()) eng = m;
        }

        System.out.println(jami);
        System.out.println(eng.nom);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class Mahsulot {
public:
    string nom;
    long long narx, miqdor;

    Mahsulot(string nom, long long narx, long long miqdor) : nom(nom), narx(narx), miqdor(miqdor) {}

    long long qiymat() const { return narx * miqdor; }
};

int main() {
    int n;
    cin >> n;
    vector<Mahsulot> mahsulotlar;
    for (int i = 0; i < n; i++) {
        string nom;
        long long narx, miqdor;
        cin >> nom >> narx >> miqdor;
        mahsulotlar.emplace_back(nom, narx, miqdor);
    }

    long long jami = 0;
    int eng = 0;                                    // index of the most valuable stock
    for (int i = 0; i < n; i++) {
        jami += mahsulotlar[i].qiymat();
        if (mahsulotlar[i].qiymat() > mahsulotlar[eng].qiymat()) eng = i;
    }

    cout << jami << endl << mahsulotlar[eng].nom << endl;
    return 0;
}
```

Input:

```text
4
daftar 5000 120
ruchka 2000 300
kitob 45000 10
sumka 150000 5
```

Output:

```text
2400000
sumka
```

The stock values are 600,000, 600,000, 450,000 and 750,000. `daftar` (notebook) and `ruchka` (pen) have equal values: were they the largest, the strict `>` condition would keep the **first** one met (`daftar`), and `>=` the last one (`ruchka`). With equal values the condition itself decides which object is chosen. The C++ version keeps not the object but its **index** — no copy is made (*qiymat* is value, *jami* the total).

## Worked example 2: changing objects in a loop

The first line holds *n* and *k*, the next *n* lines a student's name and score. Add *k* points to every student scoring below 60 (but not above 100). Print all students with their new scores, then the number who passed (≥ 60).

```python
class Talaba:
    def __init__(self, ism, ball):
        self.ism = ism
        self.ball = ball


n, k = map(int, input().split())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append(Talaba(ism, int(ball)))

for t in talabalar:                 # t is the very object in the list
    if t.ball < 60:
        t.ball = min(t.ball + k, 100)

otganlar = 0
for t in talabalar:
    print(t.ism, t.ball)
    if t.ball >= 60:
        otganlar += 1
print(otganlar)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Talaba {
        String ism;
        int ball;

        Talaba(String ism, int ball) {
            this.ism = ism;
            this.ball = ball;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) talabalar.add(new Talaba(in.next(), in.nextInt()));

        for (Talaba t : talabalar) {        // t refers to the object in the list
            if (t.ball < 60) t.ball = Math.min(t.ball + k, 100);
        }

        int otganlar = 0;
        for (Talaba t : talabalar) {
            System.out.println(t.ism + " " + t.ball);
            if (t.ball >= 60) otganlar++;
        }
        System.out.println(otganlar);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Talaba {
    string ism;
    int ball;
};

int main() {
    int n, k;
    cin >> n >> k;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;

    for (auto& t : talabalar) {         // without & a copy would change
        if (t.ball < 60) t.ball = min(t.ball + k, 100);
    }

    int otganlar = 0;
    for (const auto& t : talabalar) {
        cout << t.ism << " " << t.ball << endl;
        if (t.ball >= 60) otganlar++;
    }
    cout << otganlar << endl;
    return 0;
}
```

Input:

```text
4 15
Aziz 52
Malika 91
Bekzod 40
Dilnoza 95
```

Output:

```text
Aziz 67
Malika 91
Bekzod 55
Dilnoza 95
3
```

In C++ a `struct` is a class whose members are all open (`public`); it suits simple data-carrying types. If the changing loop says `auto` instead of `auto&`, the program reports no error, but the scores do not change: the output shows `Aziz 52`, `Bekzod 40`, and 2 passed (*otganlar*) — such a bug is hard to find.

## Worked example 3: the natural order of books

The first line holds *n*, the next *n* lines a book's year of publication and title (one word). Give the `Kitob` (book) class a natural order: first by year, and for equal years alphabetically by title. Sort the books with the standard sort and print them.

```python
class Kitob:
    def __init__(self, yil, nom):
        self.yil = yil
        self.nom = nom

    def __lt__(self, boshqa):           # "does self come before the other?"
        return (self.yil, self.nom) < (boshqa.yil, boshqa.nom)


n = int(input())
kitoblar = []
for _ in range(n):
    yil, nom = input().split()
    kitoblar.append(Kitob(int(yil), nom))

kitoblar.sort()
for k in kitoblar:
    print(k.yil, k.nom)
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Kitob implements Comparable<Kitob> {
        int yil;
        String nom;

        Kitob(int yil, String nom) {
            this.yil = yil;
            this.nom = nom;
        }

        @Override
        public int compareTo(Kitob boshqa) {
            if (yil != boshqa.yil) return Integer.compare(yil, boshqa.yil);
            return nom.compareTo(boshqa.nom);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Kitob> kitoblar = new ArrayList<>();
        for (int i = 0; i < n; i++) kitoblar.add(new Kitob(in.nextInt(), in.next()));

        Collections.sort(kitoblar);
        for (Kitob k : kitoblar) System.out.println(k.yil + " " + k.nom);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Kitob {
    int yil;
    string nom;

    bool operator<(const Kitob& boshqa) const {     // "do I come before the other?"
        if (yil != boshqa.yil) return yil < boshqa.yil;
        return nom < boshqa.nom;
    }
};

int main() {
    int n;
    cin >> n;
    vector<Kitob> kitoblar(n);
    for (auto& k : kitoblar) cin >> k.yil >> k.nom;

    sort(kitoblar.begin(), kitoblar.end());
    for (const auto& k : kitoblar) cout << k.yil << " " << k.nom << endl;
    return 0;
}
```

Input:

```text
5
1999 Sarob
1926 Kecha
1999 Alpomish
2005 Dunyo
1926 Mehrob
```

Output:

```text
1926 Kecha
1926 Mehrob
1999 Alpomish
1999 Sarob
2005 Dunyo
```

In all three languages the call to the sort function gives no key at all: the order lives in the class itself — in `__lt__`, `compareTo` or `operator<`. In Python comparing the tuples `(yil, nom)` compares the first elements and, if they are equal, the second — a short way to write a two-key order.

:::warning
**Common mistakes**

- **Not creating objects after `new Talaba[n]` in Java.** The elements are `null`; without `t[i] = new Talaba(...)` you get a `NullPointerException`.
- **`auto` instead of `auto&` in C++.** The loop changes a copy and the array stays the same — the program runs silently with a wrong result.
- **Comparing strings with `==` in Java.** `==` compares references; use `equals` when searching by name.
- **Ignoring equal values.** When finding a maximum, `>` and `>=` pick different objects; decide which one the problem needs.
:::

:::tip
When working with an array of objects, first check that you read it correctly: right after reading, write a temporary loop that prints all the objects. A large share of bugs is in the reading step.
:::

## Summary

- Objects are kept in an array or a growing list: Python `list`, Java `ArrayList`, C++ `vector`.
- In Java `new T[n]` creates only `null` references; every object is created separately with `new`.
- In C++, write `auto&` to change objects in a loop and `const auto&` to read them.
- Array algorithms apply to objects too — fields are compared, and the best object or its index is kept.
- A natural order is given in the class with `__lt__`, `compareTo` or `operator<`.

## Check yourself

:::check
Q: In Java, how many `Talaba` objects exist after `Talaba[] t = new Talaba[3];`?
A: None: the array holds three `null` references.
Q: In C++, after `for (auto t : v) t.ball = 0;`, are the scores in `v` zero?
A: No, `t` is a copy; you must write `for (auto& t : v)`.
Q: In Python, does `for t in talabalar: t.ball += 1` change the objects in the list?
A: Yes, `t` refers to the object in the list itself.
Q: What value must `compareTo` return in Java?
A: A negative number if the current object should come first, zero if they are equal, a positive number if it comes later.
Q: Why is it handy to keep the object or its index when finding a maximum?
A: Then the best object's other fields (its name, say) can be printed too.
:::

:::link https://docs.python.org/3/howto/sorting.html#odds-and-ends
Python: sorting and __lt__
About sorting objects through `__lt__`.
:::

:::link https://dev.java/learn/api/collections-framework/
Java: collections
The official learning resource on `List`, `ArrayList` and `Comparable`.
:::
