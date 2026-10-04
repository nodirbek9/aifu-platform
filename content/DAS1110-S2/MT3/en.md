---
title: Dynamic arrays and pointers
summary: Independent work: an array whose size is known only at run time, the two-pass method (count first, then allocate), new[]/delete[] and walking with a pointer, returning a dynamic array from a function; the sample problem — a filtered array; the assignment — merging two sorted arrays.
---

## Why it matters

In many problems the size of the result array is not known in advance: "write the elements greater than a given number into a separate array", "merge two arrays into one". Such an array is created **while the program runs**, at the size needed. In this independent work you practise creating a dynamic array at the right size, walking through it with a pointer and returning it from a function.

:::note
**After this independent work you will be able to:**

- compute the size of a result array in advance and create it at exactly that size;
- free memory allocated with `new[]` correctly with `delete[]` in C++;
- walk through an array with a pointer;
- return a new dynamic array and its length from a function.
:::

## A short review of the topic

When the size of the result is unknown, there are two ways:

| Method | Idea | When |
|---|---|---|
| **Two passes** | pass 1: count how many elements are needed; allocate; pass 2: fill | a plain array (`new int[k]`, `new int[k]` in Java) |
| **A growing array** | start from an empty array and add every element at the end | `list`, `ArrayList`, `vector` |

| Language | Creating a dynamic array | Freeing | Returning from a function |
|---|---|---|---|
| Python | `[0] * k` or `append` | automatic | `return the_list` |
| Java | `new int[k]` | automatic | `return array;` (its length is in `length`) |
| C++ (`new`) | `int* p = new int[k];` | `delete[] p;` | the pointer **and** the length (say, through a reference parameter) |
| C++ (`vector`) | `vector<int> v(k);` | automatic | `return v;` |

In C++, if a function returns an array allocated with `new`, the **caller** must free it with `delete[]` — this agreement is written in the function's description. Walking with a pointer: `for (int* p = a; p != a + n; ++p)` — `*p` is the current element.

## The sample problem: a filtered dynamic array

The first line holds *n*, the second *n* integers, the third *x*. The function `kattalar(a, n, x)` (the larger ones) should create and return a **new** array of the elements greater than *x* (in the original order). Print the new array's length and its elements (`-` if it is empty).

```python
def kattalar(a, x):
    natija = []                          # a growing list
    for v in a:
        if v > x:
            natija.append(v)
    return natija


n = int(input())
a = list(map(int, input().split()))
x = int(input())
b = kattalar(a, x)
print(len(b))
print(*b if b else "-")
```
```java
import java.util.Scanner;

public class Main {
    static int[] kattalar(int[] a, int x) {
        int k = 0;
        for (int v : a) if (v > x) k++;  // pass 1: count
        int[] natija = new int[k];       // exactly the size needed
        int j = 0;
        for (int v : a) if (v > x) natija[j++] = v;   // pass 2: fill
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int x = in.nextInt();

        int[] b = kattalar(a, x);
        System.out.println(b.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(b[i]);
        }
        System.out.println(b.length > 0 ? sb.toString() : "-");
    }
}
```
```cpp
#include <iostream>
using namespace std;

// Returns a new array; the caller frees it with delete[].
int* kattalar(const int* a, int n, int x, int& k) {
    k = 0;
    for (const int* p = a; p != a + n; ++p)       // pass 1: count
        if (*p > x) k++;
    int* natija = new int[k];                     // exactly the size needed
    int* q = natija;
    for (const int* p = a; p != a + n; ++p)       // pass 2: fill
        if (*p > x) *q++ = *p;
    return natija;
}

int main() {
    int n;
    cin >> n;
    int* a = new int[n];
    for (int i = 0; i < n; i++) cin >> a[i];
    int x;
    cin >> x;

    int k;
    int* b = kattalar(a, n, x, k);
    cout << k << endl;
    if (k == 0) cout << "-";
    for (int i = 0; i < k; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;

    delete[] b;                                   // the memory kattalar() allocated
    delete[] a;
    return 0;
}
```

The table of tests:

| Input | Expected result | What it checks |
|---|---|---|
| `6` / `4 9 1 7 3 9` / `4` | `3`, `9 7 9` | the ordinary case, repeated elements |
| `3` / `1 2 3` / `5` | `0`, `-` | an empty result |
| `3` / `-1 -5 -3` / `-4` | `2`, `-1 -3` | negative numbers |
| `2` / `4 4` / `4` | `0`, `-` | elements equal to *x* are not taken |

In the C++ version `*q++ = *p` means "write to where *q* points, then move *q* to the next element". Memory was allocated in two places (`a` and inside `kattalar`) — so there must be two `delete[]`s at the end.

## The independent work assignment

Solve the assignment below: **merge two arrays sorted in ascending order into one sorted array** (merge). Do not use a ready sort function: the two-pointer method runs in *O*(*n* + *m*) time — at every step the smaller of the two arrays' current elements is written to the result. What to hand in:

1. **The algorithm** — in words or as a flowchart: how the pointers move and what happens when one of the arrays runs out.
2. **The program** — the result array is created dynamically at exactly *n* + *m* elements; in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of tests** — at least 5 tests: one array entirely smaller than the other, interleaved elements, equal elements, one array of a single element, arrays of different lengths.
4. **A conclusion** — 3–5 sentences: why this method is faster than `sort` and how much extra memory it needs.

:::warning
**Grading criteria and common mistakes**

- Using functions such as `sort`, `sorted` or `Arrays.sort` means the assignment is not done.
- Once one array runs out, do not forget to copy the **remaining** elements of the other into the result.
- If `new[]` is used in C++, each one needs a matching `delete[]`.
- Equal elements both go into the result — none must be lost.
:::

:::tip
Run the algorithm on paper on two small arrays: `1 4 9` and `2 3 10`. At every step write down in a table where both pointers are and what was written to the result.
:::

## Summary

- If the size of the result is unknown: two passes (count → allocate → fill) or a growing array.
- In C++ memory allocated with `new[]` is freed with `delete[]`; an array returned by a function is freed by the caller.
- Walking with a pointer: `p != a + n`, `*p` is the current element, `++p` the next.
- In Java and Python an array keeps its own length; a C++ pointer does not, so the length is returned separately.

## Check yourself

:::check
Q: Why is the first pass of the "two passes" method needed?
A: To find how many elements the result array will have and create it at exactly that size.
Q: In C++, after `int* b = kattalar(...)`, who must free the memory?
A: The caller — with `delete[] b;`, because the array was allocated with `new[]` inside the function.
Q: What does `*q++ = *p` do?
A: It writes the value `p` points to into the place `q` points to, then moves `q` to the next element.
Q: Why must a C++ function return the length together with the pointer?
A: A pointer keeps no information about the array's length.
:::

:::link https://en.cppreference.com/w/cpp/language/delete
C++: the delete expression
The rules for `delete` and `delete[]`.
:::
