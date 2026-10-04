---
title: One-dimensional arrays. Static arrays. Methods of sorting and searching
summary: The static array in C++, linear search, bubble sort, binary search; comparing the speed of the algorithms.
---

## Why it matters

How do you find "Rahimov" in a phone book? You do not read every name from the first page: you open the book in the middle, see whether the letter "R" is ahead or behind, and cut the search in half. That only works because the names are **in alphabetical order**.

Sorting and searching are the most used operations in programming: ordering goods by price in an online shop, finding a place in a ranking, finding a student in a database. In this practical session we write the simplest and most important of these methods by hand.

:::note
**After this session you will be able to:**

- declare a static array with a fixed size in C++;
- find the position of an element with linear search;
- write bubble sort and explain how it works step by step;
- apply binary search to a sorted array and explain why it is fast.
:::

## The static array (C++)

A **static array** is an array whose size is known at compile time and does not change while the program runs:

| Written | Meaning |
|---|---|
| `int a[100];` | room for 100 `int`s; the values are undefined ("garbage") |
| `int a[5] = {3, 1, 4, 1, 5};` | with values |
| `int a[100] = {};` | 100 zeros |
| `const int MAX = 1000;` `int a[MAX];` | the size through a constant |

When a problem says "*n* ≤ 1000", the array is declared for the largest case (`MAX = 1000`) and only the first *n* elements are used. A static array does not "know" its length — *n* has to be kept in a separate variable.

Java and Python have no static arrays: a Java array is created at run time with `new int[n]` (its length does not change afterwards), and a Python list can change size.

## Linear search

**Linear search** checks the elements one by one from the start until the wanted one is found. If it is not found, −1 is usually returned.

- The array does not need to be sorted.
- In the worst case all *n* elements are checked.

## Bubble sort

**Bubble sort** compares neighbouring elements and swaps those that are in the wrong order. On each pass the largest element "bubbles up" to the end.

1. On pass *i* the neighbouring pairs `a[j]` and `a[j + 1]` (*j* = 0 … *n* − 2 − *i*) are compared.
2. If `a[j] > a[j + 1]`, they are swapped.
3. If a pass makes no swaps at all, the array is sorted — we can stop.

After each pass the last element is in its place, so the next pass is one shorter. In the worst case about *n*²/2 comparisons are made.

Other simple methods are **selection sort** (find the smallest and put it first) and **insertion sort** (insert each element into its place in the sorted part). In practice the standard functions are used: Python `a.sort()`, Java `Arrays.sort(a)`, C++ `sort(a, a + n)` — they take ~*n* log *n* time.

## Binary search

**Binary search** works only on a **sorted** array:

1. The search range [*chap*, *ong*] is the whole array at first.
2. The middle element is taken: *orta* = (*chap* + *ong*) / 2.
3. If `a[orta]` is the number sought — found. If it is smaller — search the right half (*chap* = *orta* + 1); if larger — the left half (*ong* = *orta* − 1).
4. Repeat until the range is empty (*chap* > *ong*).

At each step the range is halved:

| Number of elements | Linear search (at most) | Binary search (at most) |
|---|---|---|
| 1 000 | 1 000 | 10 |
| 1 000 000 | 1 000 000 | 20 |
| 1 000 000 000 | 1 000 000 000 | 30 |

## Worked example 1: linear search

The first line has *n* (*n* ≤ 1000), the second *n* numbers, the third the number *x* to look for. Print the position (from 1) where *x* first appears; if it is not found, `-1`.

```python
n = int(input())
a = list(map(int, input().split()))
x = int(input())

orin = -1
for i in range(n):
    if a[i] == x:
        orin = i + 1
        break

print(orin)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        int x = in.nextInt();

        int orin = -1;
        for (int i = 0; i < n; i++) {
            if (a[i] == x) {
                orin = i + 1;
                break;
            }
        }

        System.out.println(orin);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];                    // a static array: room for the largest case

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    int x;
    cin >> x;

    int orin = -1;
    for (int i = 0; i < n; i++) {
        if (a[i] == x) {
            orin = i + 1;
            break;
        }
    }

    cout << orin << endl;
    return 0;
}
```

For the input `6`, `4 8 15 16 23 42`, `16` (*orin* is the position): 4, 8, 15 are checked, the 4th element is 16 — `orin = 4`, `break`. The output is `4`. For *x* = 7 all 6 elements are checked and `-1` is printed.

In the C++ version the array is declared outside `main` — **globally**: global arrays are filled with zeros automatically and do not overflow the stack even when large.

## Worked example 2: bubble sort

The first line has *n*, the second *n* numbers. Sort the array in ascending order and print it.

```python
n = int(input())
a = list(map(int, input().split()))

for i in range(n - 1):
    almashdi = False
    for j in range(n - 1 - i):
        if a[j] > a[j + 1]:
            a[j], a[j + 1] = a[j + 1], a[j]
            almashdi = True
    if not almashdi:
        break

print(*a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }

        for (int i = 0; i < n - 1; i++) {
            boolean almashdi = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int t = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = t;
                    almashdi = true;
                }
            }
            if (!almashdi) {
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    for (int i = 0; i < n - 1; i++) {
        bool almashdi = false;
        for (int j = 0; j < n - 1 - i; j++) {
            if (a[j] > a[j + 1]) {
                int t = a[j];
                a[j] = a[j + 1];
                a[j + 1] = t;
                almashdi = true;
            }
        }
        if (!almashdi) {
            break;
        }
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

For the input `5` and `5 1 4 2 8`:

| Pass | The array after the pass | In its place |
|---|---|---|
| 1 | 1 4 2 5 8 | 8 |
| 2 | 1 2 4 5 8 | 5 |
| 3 | 1 2 4 5 8 (no swaps) | — stop |

The output is `1 2 4 5 8`. On pass 3 there was not a single swap, so the `almashdi` ("swapped") flag stayed false and the sort finished early. An already sorted array is checked in one pass.

## Worked example 3: binary search

The first line has *n*, the second *n* numbers sorted in ascending order, the third *x*. Print the position of *x* (from 1) and the number of steps it took to find; if it is not found, `-1` and the number of steps.

```python
n = int(input())
a = list(map(int, input().split()))
x = int(input())

chap, ong = 0, n - 1
orin = -1
qadam = 0
while chap <= ong:
    qadam += 1
    orta = (chap + ong) // 2
    if a[orta] == x:
        orin = orta + 1
        break
    elif a[orta] < x:
        chap = orta + 1
    else:
        ong = orta - 1

print(orin, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        int x = in.nextInt();

        int chap = 0, ong = n - 1;
        int orin = -1;
        int qadam = 0;
        while (chap <= ong) {
            qadam++;
            int orta = (chap + ong) / 2;
            if (a[orta] == x) {
                orin = orta + 1;
                break;
            } else if (a[orta] < x) {
                chap = orta + 1;
            } else {
                ong = orta - 1;
            }
        }

        System.out.println(orin + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

const int MAX = 1000;
int a[MAX];

int main() {
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    int x;
    cin >> x;

    int chap = 0, ong = n - 1;
    int orin = -1;
    int qadam = 0;
    while (chap <= ong) {
        qadam++;
        int orta = (chap + ong) / 2;
        if (a[orta] == x) {
            orin = orta + 1;
            break;
        } else if (a[orta] < x) {
            chap = orta + 1;
        } else {
            ong = orta - 1;
        }
    }

    cout << orin << " " << qadam << endl;
    return 0;
}
```

For the input `10`, `3 7 11 15 19 23 27 31 35 39`, `27` (*chap* — left bound, *ong* — right bound, *orta* — middle):

| Step | *chap* | *ong* | *orta* | `a[orta]` | Decision |
|---|---|---|---|---|---|
| 1 | 0 | 9 | 4 | 19 | 19 < 27 → go right |
| 2 | 5 | 9 | 7 | 31 | 31 > 27 → go left |
| 3 | 5 | 6 | 5 | 23 | 23 < 27 → go right |
| 4 | 6 | 6 | 6 | 27 | found |

The output is `7 4` — at position 7, in 4 steps. Linear search would take 7 steps; for 1 000 000 elements the difference is 20 against up to a million. For *x* = 20 the range becomes empty and `-1 3` is printed.

:::warning
**Common mistakes**

- **Binary search on an unsorted array.** The result is wrong — with no error message at all.
- **`while chap < ong`.** When one element is left in the range it is not checked; the condition must be `<=`.
- **`chap = orta` or `ong = orta`.** The range may stop shrinking — an infinite loop. Write `orta + 1` and `orta - 1`.
- **The inner bubble loop as `j < n - i`.** On the last iteration `a[j + 1]` is out of bounds; the correct form is `j < n - 1 - i`.
- **Assuming a local static array in C++ holds zeros.** `int a[100];` inside `main` is full of "garbage"; zero it with `= {}` or declare it globally.
:::

:::tip
First run the sort and the search by hand on a paper example of 5–6 numbers, writing *chap*, *ong*, *orta* into a table at every step. If the program's result matches the table, the algorithm is understood correctly.
:::

## Summary

- The C++ static array `int a[MAX];` — the size is fixed in advance for the largest case; *n* is kept separately.
- Linear search works on any array, in at most *n* steps.
- Bubble sort swaps neighbours; with no swaps it stops early; ~*n*²/2 comparisons.
- Binary search works only on a sorted array and halves the range at every step: ~log₂ *n* steps.
- In practice the standard `sort` functions are used; writing them by hand is for understanding the algorithm.

## Check yourself

:::check
Q: Must the array be sorted for linear search? For binary search?
A: Not for linear search; for binary search it must be.
Q: What does the array `3 1 2` look like after the first pass of bubble sort?
A: `1 2 3`: 3 ↔ 1, then 3 ↔ 2.
Q: At most how many steps does binary search take on a sorted array of 1024 elements?
A: 11 (2¹⁰ = 1024; each step halves the range, and the single element left at the end is checked too).
Q: What does the `almashdi` flag give bubble sort?
A: If a pass made no swaps, the array is sorted and the remaining passes are skipped; a sorted array is checked in one pass.
Q: In C++, what is `a[0]` if `int a[5];` is declared inside `main`?
A: Undefined: a random value from memory. A global array or `int a[5] = {};` starts with zeros.
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: sorting algorithms
Animations of bubble, selection, insertion sort and others.
:::

:::link https://en.cppreference.com/w/cpp/algorithm/sort
cppreference: std::sort
The C++ standard sorting function.
:::
