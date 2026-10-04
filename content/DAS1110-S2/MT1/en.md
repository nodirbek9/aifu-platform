---
title: Using one-dimensional arrays as function parameters
summary: Independent work: passing an array to a function — a copy or a reference, functions that change an array and functions that compute, passing the length; the sample problem — shifting an array by its minimum; the assignment — the maximum and its position.
---

## Why it matters

In a large program the code working with an array is split into several functions: one reads, one computes, one prints. Passing an array to a function correctly is a skill every programmer needs: does the function get a **copy** of the array or the array **itself**? Is a change made inside the function visible outside? In this independent work you find practical answers to these questions.

:::note
**After this independent work you will be able to:**

- pass an array to a function in three languages and determine its length correctly;
- predict what happens when a function changes an array;
- write functions that change an array (procedures) and functions that compute (return a value), keeping them apart;
- build a table of tests for a function.
:::

## A short review of the topic

| Language | Declaring the function | Length | Is a change inside the function visible outside? |
|---|---|---|---|
| Python | `def f(a):` | `len(a)` | yes — a reference to the list is passed |
| Java | `static void f(int[] a)` | `a.length` | yes — a reference to the array is passed |
| C++ (`vector`) | `void f(vector<int>& a)` | `a.size()` | with `&` — yes; without `&` — **no** (a copy) |
| C++ (a plain array) | `void f(int a[], int n)` | a separate parameter `n` | yes — a pointer is passed |

Rules:

1. If a function **only reads** the array, write `const vector<int>&` in C++ — no copy is made, and it cannot be changed by accident.
2. If a function **changes** the array, let its name say so (`siljit` — shift, `saralab_qoy` — sort in place) and let it return no value.
3. A plain C++ array arrives in a function as a pointer and "forgets" its length — the length is passed separately.
4. In Python `a = [...]` inside a function creates a **new** list and does not affect the outer one; `a[i] = ...` or `a.append(...)`, however, does.

## The sample problem: shifting by the minimum

The first line holds *n*, the second *n* integers. A function `eng_kichik(a)` (smallest) returns the minimum, and a procedure `siljit(a)` (shift) subtracts the minimum from every element, changing **the array itself**. The main program prints the changed array.

```python
def eng_kichik(a):
    natija = a[0]
    for x in a:
        if x < natija:
            natija = x
    return natija


def siljit(a):                      # the list itself changes
    m = eng_kichik(a)
    for i in range(len(a)):
        a[i] -= m


n = int(input())
a = list(map(int, input().split()))
siljit(a)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    static int engKichik(int[] a) {
        int natija = a[0];
        for (int x : a) if (x < natija) natija = x;
        return natija;
    }

    static void siljit(int[] a) {   // the array itself changes
        int m = engKichik(a);
        for (int i = 0; i < a.length; i++) a[i] -= m;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        siljit(a);
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
#include <vector>
using namespace std;

int engKichik(const vector<int>& a) {          // only reads
    int natija = a[0];
    for (int x : a) if (x < natija) natija = x;
    return natija;
}

void siljit(vector<int>& a) {                  // & — the array itself changes
    int m = engKichik(a);
    for (int& x : a) x -= m;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    siljit(a);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

The table of tests — the independent work requires exactly such a table:

| Input | Expected result | What it checks |
|---|---|---|
| `4` / `7 3 9 3` | `4 0 6 0` | the minimum occurs twice |
| `3` / `-2 -5 0` | `3 0 5` | negative numbers |
| `1` / `42` | `0` | one element |
| `3` / `5 5 5` | `0 0 0` | all elements equal |

If C++ says `siljit(vector<int> a)` (without `&`), the program reports no error, but the array does not change — the first test prints `7 3 9 3`. A table of tests is needed exactly to catch such cases.

## The independent work assignment

Solve the assignment below: write a function that takes an array as a parameter and returns **the largest element and its first position** (counting from 1). What to hand in:

1. **The function's description** — its name, parameters and return value(s); explain how the two results are returned (a tuple, a `record`, reference parameters or an array).
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of tests** — at least 5 tests: the maximum at the start, at the end, occurring several times, all elements negative, one element.
4. **A conclusion** — 3–5 sentences: does the function change the array, and why?

:::warning
**Grading criteria and common mistakes**

- The maximum and its position must be found by a **function**; a program written entirely inside `main` does not get full marks.
- If the maximum occurs several times, its **first** position is printed: mind the difference between `>` and `>=`.
- Positions count from 1, indices from 0 — do not forget `+ 1`.
- The starting value must be the first element, not `0`: all numbers may be negative.
:::

:::tip
First check the function on paper against the table of tests: for each test, what values does the function take step by step — only then move to code.
:::

## Summary

- A Python list and a Java array are passed to a function as a reference: a change inside is visible outside.
- To change a C++ `vector`, write `&`; to only read it, `const &`; without `&` a copy is passed.
- With a plain C++ array the length is given as a separate parameter.
- A computing function returns a value, while a changing procedure changes the array itself.

## Check yourself

:::check
Q: In Java, after calling `static void f(int[] a) { a[0] = 9; }`, what is `a[0]` in the caller's array?
A: 9 — a reference to the array was passed, and the function changed that very array.
Q: In C++, if `void f(vector<int> a) { a[0] = 9; }` is called, does the outer array change?
A: No: without `&` a copy was passed.
Q: In Python, if `def f(a): a = [0]` is called, does the outer list change?
A: No: `a = [0]` creates a new list inside the function and binds the local name to it.
Q: Why is the length passed separately with a plain C++ array?
A: The array arrives in the function as a pointer and loses the information about its length.
:::

:::link https://docs.python.org/3/faq/programming.html#how-do-i-write-a-function-with-output-parameters-call-by-reference
Python: passing arguments to a function
The official FAQ on how arguments are passed in Python.
:::
