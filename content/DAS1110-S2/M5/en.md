---
title: Working with templates
summary: The idea of generic programming; C++ function and class templates, Java generics and bounds (extends Comparable), duck typing and TypeVar in Python; a template function, a template class (a stack) and a generic sort.
---

## Why it matters

We wrote a function that finds the largest element of an array of integers. Tomorrow the same job is needed for real numbers, the day after for strings. Copying the function each time and changing only the type invites mistakes: a bug fixed in one copy stays in the others.

**Templates** (templates in C++, generics in Java) let us write an algorithm once with a **type parameter**: the largest element of an array of type `T` — and `T` can be `int`, `double` or `String`. The standard libraries themselves are built this way: `vector<int>`, `ArrayList<String>`, `sort` are all templates.

:::note
**After this lesson you will be able to:**

- explain why generic programming is needed;
- write a function and a class template in C++, and a generic method and class in Java;
- put a bound on a type parameter (`T extends Comparable<T>`);
- explain why one Python function works with different types ("duck typing");
- use template functions and classes in your own programs.
:::

## Generic programming

The algorithm for finding the largest element does not depend on the type: all it needs is that two elements can be **compared** (`<`). A template expresses exactly that: "for some type `T`, provided `T` values can be compared".

| Language | Function template | Call |
|---|---|---|
| C++ | `template <typename T> T engKatta(const vector<T>& a)` | `engKatta(sonlar)`, `engKatta(sozlar)` |
| Java | `static <T extends Comparable<T>> T engKatta(List<T> a)` | `engKatta(sonlar)` |
| Python | `def eng_katta(a): ...` (no type required) | `eng_katta(sonlar)` |

A type parameter is usually named with one capital letter: `T` (type), `K` and `V` (key, value), `E` (element).

## C++ templates

A C++ template is a **mould** for code. When the compiler sees the call `engKatta(sonlar)`, it creates a separate copy of the function for `T = int`, and for `engKatta(sozlar)` a copy for `T = string`. This process is called **instantiation**.

- The type is usually **deduced automatically** from the arguments; if needed it is written explicitly: `engKatta<double>(a)`.
- Whatever operations the template applies to `T` (say, `<`), the actual type must support — otherwise there is a compile error.
- A class template: `template <typename T> class Stek { vector<T> elementlar; ... };` and `Stek<int> s;`.
- The full code of a template must sit in a header file (`.h`): the compiler must see it for every type.

## Java generics

In Java the type parameter is written in angle brackets: `class Stek<T>` for a class, and `<T>` before the return type for a method.

- A type parameter can only be an **object type**: `Stek<Integer>`, not `Stek<int>`. Conversion between `int` and `Integer` is automatic (autoboxing).
- A **bound**: `<T extends Comparable<T>>` — "`T` must be a mutually comparable type". After that `a.compareTo(b)` can be called. For an unbounded `T` only the methods of `Object` are available.
- The compiler checks the types: adding a number to a `Stek<String>` is a compile error. At run time the type parameter is erased (type erasure) — which is why `new T[n]` cannot be written and an `ArrayList<T>` is used.

## Python: duck typing

Python is dynamically typed: a function parameter accepts a value of any type. If the operations inside the function (say, `>`) exist for the value, everything works. This is called **duck typing**: "if it walks like a duck and quacks like a duck, it is a duck".

So Python needs no special template syntax. But for readers of the code and checking tools, **type hints** are written:

```python
from typing import TypeVar

T = TypeVar("T")

def birinchi(a: list[T]) -> T:      # "whatever T comes in, that T comes out"
    return a[0]
```

Since Python 3.12 there is also a shorter form: `def birinchi[T](a: list[T]) -> T:`. The hints do not affect how the program runs (*birinchi* means first).

## Worked example 1: a template function

The first line holds integers, the second words. Using one generic `eng_katta` (largest) function and one generic `sanash` (count) function, print the largest element of each line and how many times it occurs (words are compared alphabetically).

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
        if (natija < x) natija = x;        // T needs only <
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

Input:

```text
4 9 2 9 7
olma nok uzum anor uzum
```

Output:

```text
9 2
uzum 2
```

In every language the algorithm is written **once** but worked with two different types. In C++ the compiler created two copies, `engKatta<int>` and `engKatta<string>`. In Java the `int`s are kept in a `List<Integer>`, and comparison goes through `compareTo` — `<` is only for numbers. Java uses `equals`: comparing `Integer` objects with `==` is dangerous.

## Worked example 2: a template class — a stack

A **stack** is a structure working on the "last in, first out" (LIFO) principle: an element is only put on top (`push`) and only taken from the top (`pop`). Write one generic `Stek` (stack) class and use it for words and numbers: print the words of the first line and the numbers of the second in reverse order.

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
    teskariChiqar<string>(oqim1);              // T given explicitly
    getline(cin, satr);
    istringstream oqim2(satr);
    teskariChiqar<int>(oqim2);
    return 0;
}
```

Input:

```text
bir ikki uch
10 20 30 40
```

Output:

```text
uch ikki bir
40 30 20 10
```

`Stek<string>` and `Stek<int>` are two different types produced from one class template (*bosh* means empty). In C++'s `teskariChiqar<string>(oqim1)` the type is written explicitly: `T` does not appear among the function's arguments, so the compiler cannot deduce it. In Java, writing `s.push(5)` into a `Stek<String>` does not compile — generics catch the mistake **before** the program runs. The Python class declares no types at all and works with any value.

## Worked example 3: a bounded generic sort

The first line holds real numbers. The second holds *n*, the next *n* lines a student's name and score. With one generic `saralash` (sort) function — insertion sort — sort the numbers in ascending order and the students in their natural order — by score descending, and by name for equal scores — and print them.

```python
def saralash(a):
    """Insertion sort: for any type that has <."""
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

    def __lt__(self, boshqa):          # natural order: score descending, then name
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
    /** Insertion sort: for any mutually comparable type. */
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
        public int compareTo(Talaba boshqa) {      // natural order: score descending, then name
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

// Insertion sort: for any type that has <
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

    bool operator<(const Talaba& boshqa) const {   // natural order: score descending, then name
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

Input:

```text
3.5 -1.25 2.0 10.75
4
Malika 88
Aziz 92
Bekzod 88
Dilnoza 95
```

Output (Python and Java):

```text
-1.25 2.0 3.5 10.75
Dilnoza 95
Aziz 92
Bekzod 88
Malika 88
```

C++'s `cout` prints a real number without trailing zeros, so its first line is `-1.25 2 3.5 10.75`. `saralash` knows nothing about real numbers or students — it uses only `<` (`compareTo` in Java). Java's `<T extends Comparable<T>>` bound guarantees it: called with a type that is not `Comparable`, the compiler reports an error. In C++ no bound is written, but if `Talaba` had no `operator<`, the error would appear inside the template, on the line `joriy < a[j]`.

:::warning
**Common mistakes**

- **A primitive type parameter in Java.** `List<int>` cannot be written; use `List<Integer>`, `List<Double>`.
- **`compareTo` without a bound in Java.** For a plain `<T>`, `a.compareTo(b)` does not compile; write `<T extends Comparable<T>>`.
- **Hiding a template in a `.cpp` file in C++.** Called from another file, it gives an "undefined reference" error; template code lives in the header file.
- **Looking for a C++ template error in the wrong place.** The compiler reports the error inside the template, but the cause is often the type at the call — it lacks a needed operation (`<`, `==`, `<<`).
:::

:::tip
Before writing a template, write and test the algorithm for one concrete type (say, `int`). Once it works, replace `int` with `T` and list which operations `T` needs — these are the template's "requirements".
:::

## Summary

- A template (generic) lets an algorithm be written once with a type parameter.
- C++ creates a separate copy of a template for every type used; template code lives in the header file.
- Java generics work only with object types; `extends` sets a bound, and types are checked at compile time.
- In Python, thanks to duck typing, a function works with any suitable type; `TypeVar` is for annotation only.
- The requirements a template places on its type parameter are the operations it uses (`<`, `==`, …).

## Check yourself

:::check
Q: What problem do templates solve?
A: Writing copies of the same algorithm for different types: the algorithm is written once with a type parameter.
Q: Why can't you write `Stek<int>` in Java?
A: A type parameter can only be an object type; `Stek<Integer>` is used.
Q: What does `<T extends Comparable<T>>` mean?
A: `T` must be a mutually comparable type, so `compareTo` can be called.
Q: If `engKatta(sonlar)` and `engKatta(sozlar)` are called in C++, how many functions does the compiler create?
A: Two: a separate copy for `T = int` and for `T = string`.
Q: Why does Python need no special template syntax?
A: The language is dynamically typed: if the operations inside a function exist for a value, it works with any type (duck typing).
:::

:::link https://dev.java/learn/generics/
Java: generics
The official learning resource on generic classes and methods and on bounds.
:::

:::link https://en.cppreference.com/w/cpp/language/templates
C++: templates
A reference on function and class templates.
:::
