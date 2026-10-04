---
title: Working with dynamic memory
summary: The stack and the heap, addresses and pointers, the & and * operators, references, new and delete, the dynamic array, memory leaks; automatic memory management in Java and Python (the garbage collector).
---

## Why it matters

When a program is written, nobody knows how many students the group will have, how many photos a user will upload, or how many lines a file contains. With a static array `int a[1000];` we guess the "worst case": small data wastes memory, and large data does not fit.

**Dynamic memory** solves the problem: while the program runs it asks for exactly the amount of memory it needs and gives it back when it is no longer needed. This very mechanism works inside a Python list and a C++ `vector`. Understanding it helps you see the cause of the most serious memory bugs — leaks, dangling pointers, out-of-bounds access.

:::note
**After this lesson you will be able to:**

- explain the difference between stack and heap memory;
- declare a pointer in C++ and use the `&` and `*` operators;
- create a dynamic array with `new` / `delete` and free it correctly;
- recognise memory-leak and dangling-pointer bugs;
- explain how memory is managed automatically in Java and Python.
:::

## The stack and the heap

The two main parts of a program's memory:

| | Stack | Heap |
|---|---|---|
| What it holds | local variables, parameters, call frames | dynamically allocated data |
| Who manages it | automatic: freed when a function ends | in C++ the programmer (`new` / `delete`); in Java, Python the garbage collector |
| Size | small (usually 1–8 MB) | large (about the size of the RAM) |
| Speed | very fast | slower |
| When the size is known | at compile time | while the program runs |

That is why a large array declared on the stack (`int a[10000000];` inside a function) can overflow the stack, while on the heap it fits easily.

## Addresses and pointers (C++)

Every byte of memory has its own **address** — a sequence number. A variable lives somewhere in memory and has an address.

A **pointer** is a variable that holds the address of another variable.

| Written | Meaning |
|---|---|
| `int* p;` | *p* is a pointer to `int` |
| `p = &x;` | `&x` is the address of *x*; *p* now points to *x* |
| `*p` | the value at the address in *p* ("dereferencing") |
| `*p = 10;` | 10 is written into *x* — through *p* |
| `nullptr` | a pointer that points to nothing |

The `&` and `*` operators undo each other: `*(&x)` is *x* itself.

A **reference** `int& r = x;` is a second name for *x*. Unlike a pointer, a reference must be bound to some variable, is never rebound afterwards and is used without `*`. References are handier for function parameters (`vector<int>& a` in M7).

## new and delete

In C++ memory is taken from the heap with `new` and given back with `delete`:

| Task | Written |
|---|---|
| One `int` | `int* p = new int(5);` … `delete p;` |
| An array of *n* `int`s | `int* a = new int[n];` … `delete[] a;` |

The rule: one `delete` for every `new`, one `delete[]` for every `new[]`.

Three dangerous bugs when working with dynamic memory:

| Bug | What happens | Result |
|---|---|---|
| **Memory leak** | the `delete` was forgotten | the program takes more and more memory as it runs |
| **Dangling pointer** | a pointer is used after `delete` | undefined behaviour, random crashes |
| **Double free** | the same memory is deleted twice | the program crashes |

That is why modern C++ often uses `vector` and "smart pointers" (`unique_ptr`) instead of manual `new` / `delete` — they give the memory back automatically. But this very mechanism works inside them.

## Java and Python: the garbage collector

In Java and Python the programmer does not free memory by hand:

- **Java**: `new int[n]` creates an object on the heap; the variable holds a **reference** to the object. When no reference to the object is left, the **garbage collector** deletes it automatically.
- **Python**: every value is an object on the heap, and variables are references to them. When the reference count drops to zero, the object is deleted.

This removes most leaks and dangling pointers. In return the programmer does not control exactly when memory is freed, and the garbage collector costs some time and memory. Java and Python have no pointer arithmetic: addresses cannot be used directly.

## Worked example 1: swapping values through a function

Two integers are given. Swap them with a **function** and print them. Each language does it its own way.

```python
def almashtir(a, b):
    # in Python an int is an immutable object: the function returns a new pair
    return b, a


x, y = map(int, input().split())
x, y = almashtir(x, y)
print(x, y)
```
```java
import java.util.Scanner;

public class Main {
    // in Java an int is passed by value, an array by reference
    static void almashtir(int[] juft) {
        int t = juft[0];
        juft[0] = juft[1];
        juft[1] = t;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] juft = {in.nextInt(), in.nextInt()};
        almashtir(juft);
        System.out.println(juft[0] + " " + juft[1]);
    }
}
```
```cpp
#include <iostream>
using namespace std;

// Through pointers: the function changes the original variables
void almashtir(int* a, int* b) {
    int t = *a;
    *a = *b;
    *b = t;
}

int main() {
    int x, y;
    cin >> x >> y;
    almashtir(&x, &y);           // the addresses are passed
    cout << x << " " << y << endl;
    return 0;
}
```

For the input `3 8` all three programs print `8 3`.

- **C++**: `almashtir(&x, &y)` ("swap") passes the addresses of *x* and *y*. Inside the function `*a` is *x* itself, so the swap happens in the original variables. References work too: `void almashtir(int& a, int& b)` called as `almashtir(x, y)`.
- **Java**: an `int` is passed by value, so the two numbers are put into an array and a reference to the array is passed.
- **Python**: the function returns the new values; numbers in Python cannot be changed in place.

## Worked example 2: a dynamic array

The first line has *n*, the second *n* numbers. Create a new array of the even numbers only — exactly as large as their count — and print it.

```python
n = int(input())
a = list(map(int, input().split()))

juftlar = [x for x in a if x % 2 == 0]   # the list is created at the size needed

print(len(juftlar))
print(*juftlar)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];             // on the heap, size known at run time
        int soni = 0;
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
            if (a[i] % 2 == 0) soni++;
        }

        int[] juftlar = new int[soni];    // exactly the size needed
        int k = 0;
        for (int x : a) {
            if (x % 2 == 0) juftlar[k++] = x;
        }

        System.out.println(soni);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < soni; i++) {
            if (i > 0) sb.append(' ');
            sb.append(juftlar[i]);
        }
        System.out.println(sb);
        // no delete needed: the garbage collector cleans up
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    int* a = new int[n];              // n ints on the heap
    int soni = 0;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
        if (a[i] % 2 == 0) soni++;
    }

    int* juftlar = new int[soni];     // exactly the size needed
    int k = 0;
    for (int i = 0; i < n; i++) {
        if (a[i] % 2 == 0) juftlar[k++] = a[i];
    }

    cout << soni << endl;
    for (int i = 0; i < soni; i++) {
        if (i > 0) cout << ' ';
        cout << juftlar[i];
    }
    cout << endl;

    delete[] a;                       // a delete[] for every new[]
    delete[] juftlar;
    return 0;
}
```

For the input `7` and `3 8 5 12 7 6 1`: the even numbers (*juftlar*) are 8, 12, 6. The output is `3` and `8 12 6`.

The C++ version has two `new[]`s and two `delete[]`s. If a `delete[]` is forgotten, nothing shows in this small program (the operating system takes the memory back when it ends), but in a function called a million times a day a leak brings the server down. `juftlar[k++] = x` writes to `juftlar[k]` first and then increases *k* by 1.

## Worked example 3: a growing array

Numbers are given one after another and end with 0. Keep them in an array: its capacity starts at 2 and is **doubled** when it is full. At the end print the count of numbers, the capacity and the numbers in reverse order.

This is exactly how `vector` and the Python list work.

```python
sigim = 2
massiv = [0] * sigim
soni = 0

x = int(input())
while x != 0:
    if soni == sigim:                  # out of room: an array twice as large
        sigim *= 2
        yangi = [0] * sigim
        for i in range(soni):
            yangi[i] = massiv[i]
        massiv = yangi                 # the garbage collector deletes the old list
    massiv[soni] = x
    soni += 1
    x = int(input())

print(soni, sigim)
print(*[massiv[i] for i in range(soni - 1, -1, -1)])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int sigim = 2;
        int[] massiv = new int[sigim];
        int soni = 0;

        int x = in.nextInt();
        while (x != 0) {
            if (soni == sigim) {           // out of room: an array twice as large
                sigim *= 2;
                int[] yangi = new int[sigim];
                for (int i = 0; i < soni; i++) {
                    yangi[i] = massiv[i];
                }
                massiv = yangi;            // the garbage collector deletes the old array
            }
            massiv[soni] = x;
            soni++;
            x = in.nextInt();
        }

        System.out.println(soni + " " + sigim);
        StringBuilder sb = new StringBuilder();
        for (int i = soni - 1; i >= 0; i--) {
            if (i < soni - 1) sb.append(' ');
            sb.append(massiv[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int sigim = 2;
    int* massiv = new int[sigim];
    int soni = 0;

    int x;
    cin >> x;
    while (x != 0) {
        if (soni == sigim) {           // out of room: an array twice as large
            sigim *= 2;
            int* yangi = new int[sigim];
            for (int i = 0; i < soni; i++) {
                yangi[i] = massiv[i];
            }
            delete[] massiv;           // the old one is freed by hand
            massiv = yangi;
        }
        massiv[soni] = x;
        soni++;
        cin >> x;
    }

    cout << soni << " " << sigim << endl;
    for (int i = soni - 1; i >= 0; i--) {
        if (i < soni - 1) cout << ' ';
        cout << massiv[i];
    }
    cout << endl;

    delete[] massiv;
    return 0;
}
```

For the input `4`, `9`, `2`, `7`, `5`, `0` (each on its own line; *soni* is the count, *sigim* the capacity):

| Entered | *soni* (before) | *sigim* | What happened |
|---|---|---|---|
| 4 | 0 | 2 | stored |
| 9 | 1 | 2 | stored |
| 2 | 2 | 2 → 4 | the array is full: a new array, 2 elements copied |
| 7 | 3 | 4 | stored |
| 5 | 4 | 4 → 8 | the array is full: a new array, 4 elements copied |

The output is `5 8` and `5 7 2 9 4`.

Why double? Growing by one each time copies the whole array on every addition — ~*n*²/2 copies for *n* numbers. With doubling, the total number of copies never exceeds 2*n*. In the C++ version `delete[] massiv` frees the old array — without it, memory would leak on every growth.

:::warning
**Common mistakes**

- **Freeing `new[]` with `delete` (without brackets).** An array always needs `delete[]`.
- **Using a pointer after `delete`.** The freed memory is no longer yours; set the pointer to `nullptr`.
- **Returning the address of a local variable from a function.** When the function ends, the local variable disappears from the stack — the pointer dangles.
- **An uninitialised pointer.** `int* p; *p = 5;` writes to a random address.
- **`b = a` in Java as a "copy" of an array.** It is not a copy but a second reference: `b[0] = 1` changes *a* too.
:::

:::tip
In C++, as soon as you write `new`, decide where the matching `delete` will be. If you cannot say for sure — use `vector`: it manages memory itself and can check bounds with `at()`.
:::

## Summary

- The stack is small, automatic memory for local variables; the heap is large memory for dynamic data.
- A pointer holds an address: `&x` is the address, `*p` the value at the address; a reference is a second name for a variable.
- In C++ `new` / `new[]` take memory from the heap and `delete` / `delete[]` give it back; each must have its pair.
- Leaks, dangling pointers and double frees are the main bugs of dynamic memory.
- In Java and Python the garbage collector gives memory back; variables hold references to objects.
- A growing array doubles when it is full — that is how `vector` and the Python list work.

## Check yourself

:::check
Q: What is *x* after `int x = 7; int* p = &x; *p = 20;`?
A: 20: *p* points to *x*, and `*p = 20` writes into *x*.
Q: How is memory allocated with `int* a = new int[100];` freed?
A: `delete[] a;`.
Q: What is a memory leak?
A: Allocated dynamic memory that is never freed: the program takes more and more memory as it runs.
Q: Why does Java have no `delete`?
A: Objects with no references left are deleted automatically by the garbage collector.
Q: In example 3, what is the final capacity if 9 numbers are entered (before 0)?
A: 16: 2 → 4 → 8 → 16 (when the 9th number arrives, all 8 places are taken).
:::

:::link https://en.cppreference.com/w/cpp/language/new
cppreference: the new expression
Allocating dynamic memory in C++.
:::

:::link https://en.cppreference.com/w/cpp/memory/unique_ptr
cppreference: std::unique_ptr
A "smart pointer" that gives memory back automatically.
:::
