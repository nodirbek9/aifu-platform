---
title: Searching methods. Operations on elements
summary: Kinds of linear search (first, last, all occurrences, by a condition), lower and upper bounds in a sorted array, the standard search functions, deleting and inserting an element and cyclically shifting an array.
---

## Why it matters

Finding a book in a library catalogue, a name in a contact list, a student's grade in a table — all of these are **search**. Once found, an element is usually acted on: deleted, joined by a new one, or the elements are shifted.

In the first semester we saw the simple form of linear and binary search: "is the element there, and where?". In practice the questions are harder: where is the *last* occurrence, *how many* are there, which is the first element greater than *x*? In this session we learn the methods that answer such questions, and the basic operations that change an array.

:::note
**After this session you will be able to:**

- find the first, last and all occurrences with linear search;
- find the lower bound and upper bound in a sorted array with binary search;
- use the standard search functions of Python, Java and C++;
- delete an element from an array and insert one by shifting elements;
- shift an array cyclically.
:::

## Kinds of linear search

Linear search looks through the array from start to end — *O*(*n*). It works on an **unsorted** array too. The loop changes a little depending on the question:

| Question | Method |
|---|---|
| First occurrence | stop the loop as soon as it is found (`break`) |
| Last occurrence | search from the end, or do not stop and remember the last one found |
| All occurrences / their number | do not stop; record or count every one found |
| By a condition (say, the first negative) | any condition instead of `a[i] == x` |

Decide the "not found" result in advance: for an index the value `-1` is usual.

## Bounds in a sorted array

In a sorted array binary search takes *O*(log *n*) steps. Besides "is the element there?" it answers two important questions:

- the **lower bound** — the **first** index where `a[i] >= x`;
- the **upper bound** — the **first** index where `a[i] > x`.

If there is no such element, the answer is *n* (the end of the array). With them it is easy to find **how many times** *x* occurs: `upper − lower`. For example, for `1 2 2 2 5 7` and *x* = 2 the lower bound is 1, the upper bound 4, and there are 3 occurrences.

The general scheme for the lower bound — the answer always lies in the range `[chap, ong]` (left, right):

```text
chap = 0, ong = n
while chap < ong:
    orta = (chap + ong) / 2
    if a[orta] < x:  chap = orta + 1
    else:            ong = orta
answer: chap
```

For the upper bound only the condition changes, to `a[orta] <= x`.

## The standard search functions

| Language | Linear | Binary (in a sorted array) |
|---|---|---|
| Python | `x in a`, `a.index(x)`, `a.count(x)` | `bisect_left(a, x)`, `bisect_right(a, x)` (the `bisect` module) |
| Java | a loop or `list.indexOf(x)` | `Arrays.binarySearch(a, x)` — an index or a negative number |
| C++ | `find(a.begin(), a.end(), x)` | `lower_bound`, `upper_bound`, `binary_search` (`<algorithm>`) |

In Python `bisect_left` is the lower bound and `bisect_right` the upper bound. Java's `Arrays.binarySearch` does not guarantee **which** of repeated elements is found, so when you need the bounds you write them yourself.

## Operations on elements

An array lies contiguously in memory, so deleting or inserting in the middle is done by **shifting elements**:

- **deleting the element at position *k***: shift `a[k+1]`, `a[k+2]`, … one step left, then reduce the length by one;
- **inserting an element at position *k***: starting from the end, shift the elements down to `a[k]` one step right, then `a[k] = x`;
- **cyclic shift** (*k* steps right): every `a[i]` goes to position `(i + k) mod n` of a new array.

These operations take *O*(*n*) time. Python's `a.pop(k)`, `a.insert(k, x)` and C++'s `vector::erase` and `vector::insert` do the same shifting inside.

## Worked example 1: deleting all occurrences

The first line holds *n* and *x*, the second *n* numbers. Delete every element equal to *x*: print the remaining elements in input order, and on the next line the number deleted. If the array becomes empty, print `-` on the first line.

```python
n, x = map(int, input().split())
a = list(map(int, input().split()))

k = 0                       # the new length
for i in range(n):
    if a[i] != x:
        a[k] = a[i]         # move a kept element forward
        k += 1

print(*a[:k] if k > 0 else "-")
print(n - k)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), x = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int k = 0;                  // the new length
        for (int i = 0; i < n; i++) {
            if (a[i] != x) {
                a[k] = a[i];        // move a kept element forward
                k++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < k; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(k > 0 ? sb : "-");
        System.out.println(n - k);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, x;
    cin >> n >> x;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int k = 0;                  // the new length
    for (int i = 0; i < n; i++) {
        if (a[i] != x) {
            a[k] = a[i];        // move a kept element forward
            k++;
        }
    }

    if (k == 0) cout << "-";
    for (int i = 0; i < k; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl << n - k << endl;
    return 0;
}
```

For the input `7 3` and `3 1 3 4 3 5 3` the output is `1 4 5` and `4`. Deleting each element separately and shifting the rest would be *O*(*n*²); here "two pointers" — `i` reads, `k` writes — look through the array once, *O*(*n*).

## Worked example 2: the number of occurrences in a sorted array

The first line holds *n*, the second *n* numbers sorted in ascending order, the third *q*, then *q* queries *x*. For every query print how many times *x* occurs in the array.

```python
from bisect import bisect_left, bisect_right

n = int(input())
a = list(map(int, input().split()))
q = int(input())
for _ in range(q):
    x = int(input())
    print(bisect_right(a, x) - bisect_left(a, x))
```
```java
import java.util.Scanner;

public class Main {
    // the first index with a[i] >= x (qatiy = false) or a[i] > x (qatiy = true)
    static int chegara(int[] a, int x, boolean qatiy) {
        int chap = 0, ong = a.length;
        while (chap < ong) {
            int orta = (chap + ong) / 2;
            if (a[orta] < x || (qatiy && a[orta] == x)) chap = orta + 1;
            else ong = orta;
        }
        return chap;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int q = in.nextInt();
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < q; k++) {
            int x = in.nextInt();
            sb.append(chegara(a, x, true) - chegara(a, x, false)).append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    int q;
    cin >> q;
    while (q--) {
        int x;
        cin >> x;
        auto quyi = lower_bound(a.begin(), a.end(), x);
        auto yuqori = upper_bound(a.begin(), a.end(), x);
        cout << (yuqori - quyi) << '\n';
    }
    return 0;
}
```

Input:

```text
6
1 2 2 2 5 7
4
2
5
3
8
```

Output:

```text
3
1
0
0
```

For *x* = 3 both bounds are 4 (the first element `>= 3` is 5), so the difference is 0. For *x* = 8 both are 6 — the end of the array. Each query takes *O*(log *n*) time, so even 10⁵ queries run fast. In the Java version one function finds both bounds (*chegara* means bound): the `qatiy` ("strict") parameter chooses between `<=` and `<`.

## Worked example 3: cyclic shift

The first line holds *n* and *k*, the second *n* numbers. Shift the array cyclically *k* steps to the right: the last elements move to the front.

```python
n, k = map(int, input().split())
a = list(map(int, input().split()))

k %= n
b = [0] * n
for i in range(n):
    b[(i + k) % n] = a[i]

print(*b)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long k = in.nextLong();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int s = (int) (k % n);
        int[] b = new int[n];
        for (int i = 0; i < n; i++) b[(i + s) % n] = a[i];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(b[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    long long k;
    cin >> n >> k;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int s = k % n;
    vector<int> b(n);
    for (int i = 0; i < n; i++) b[(i + s) % n] = a[i];

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;
    return 0;
}
```

For the input `5 2` and `1 2 3 4 5` the output is `4 5 1 2 3`. The input `5 7` gives the same result: a shift of 7 steps equals a shift of 2, because after *n* = 5 steps the array returns to its original state — that is why `k % n` is taken first. Since *k* may be very large, Java and C++ read it as a `long`.

:::warning
**Common mistakes**

- **Binary search in an unsorted array.** The result is random; sort first or use linear search.
- **An endless loop in the bounds.** Writing `while (chap <= ong)` together with `ong = orta` never stops; keep the scheme's `chap < ong`.
- **Deleting from a Python list while iterating over it.** `for v in a: a.remove(v)` skips elements; build a new list or use the two-pointer method.
- **Forgetting `k % n` in a shift.** With a large *k* the loop spins needlessly or an index goes out of bounds.
:::

:::tip
After writing a binary search, test it on three edge cases: *x* smaller than every element, larger than every element, and repeated several times.
:::

## Summary

- Linear search is *O*(*n*) and works on any array; depending on the question it finds the first, last or all occurrences.
- In a sorted array the lower bound (`>= x`) and upper bound (`> x`) are found by binary search in *O*(log *n*); their difference is the number of occurrences.
- Python's `bisect` and C++'s `lower_bound`/`upper_bound` give the bounds ready-made.
- Deleting and inserting in the middle of an array is done by shifting elements, *O*(*n*).
- In a cyclic shift an element goes to position `(i + k) mod n`.

## Check yourself

:::check
Q: In the array `4 1 4 2 4`, at which index (from 0) is the last occurrence of 4?
A: At index 4 (the last element).
Q: In the array `1 3 3 6 9`, what is the lower bound for *x* = 4?
A: 3: the first element `>= 4` is 6, and its index is 3.
Q: How can you find the number of occurrences of *x* in a sorted array?
A: Subtract the lower bound from the upper bound: `upper_bound − lower_bound`.
Q: If one element is deleted from the start of a 1000-element array, how many elements are shifted?
A: 999 — all the remaining elements move one step left.
Q: What do we get if we shift the array `1 2 3 4` cyclically 1 step to the right?
A: `4 1 2 3`.
:::

:::link https://docs.python.org/3/library/bisect.html
Python: the bisect module
`bisect_left`, `bisect_right` and working with a sorted list.
:::

:::link https://en.cppreference.com/w/cpp/algorithm/lower_bound
C++: lower_bound
The lower bound function with examples.
:::
