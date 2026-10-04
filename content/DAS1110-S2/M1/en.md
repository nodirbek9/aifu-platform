---
title: Sorting algorithms
summary: Algorithm complexity (big-O notation), selection and insertion sort, the idea of merge sort and quick sort, stable sorting, the standard sort function and sorting by key.
---

## Why it matters

A leaderboard is ordered by score, a phone book by surname, an online shop by price. Binary search works only on sorted data. That is why sorting is one of the most common jobs computers do.

In the first semester we wrote bubble sort. It copes calmly with 100 elements, but it sorts the university's 10,000 students slowly and is practically useless on a million records. In this lecture we learn to compare sorting methods by **speed**, write the simple methods ourselves and use the standard function on large data.

:::note
**After this lesson you will be able to:**

- estimate an algorithm's complexity in big-O notation (*O*(*n*), *O*(*n*²), *O*(*n* log *n*));
- write selection sort and insertion sort in three languages;
- explain the idea behind merge sort and quick sort;
- explain what a stable sort is and when it matters;
- sort records by several keys with the standard sort function.
:::

## Algorithm complexity

An algorithm's speed is measured not in seconds but by **how the number of operations grows as the input size *n* grows**. This is written in **big-O notation**:

| Complexity | Name | Operations for *n* = 1000 | Example |
|---|---|---|---|
| *O*(1) | constant | 1 | accessing an array element |
| *O*(log *n*) | logarithmic | ~10 | binary search |
| *O*(*n*) | linear | 1,000 | linear search, a sum |
| *O*(*n* log *n*) | | ~10,000 | merge sort, the standard `sort` |
| *O*(*n*²) | quadratic | 1,000,000 | bubble, selection, insertion sort |

The difference shows clearly on large *n*: for *n* = 1,000,000, *O*(*n*²) means 10¹² operations (several hours on a computer), while *O*(*n* log *n*) means 2 · 10⁷ operations (under a second).

## Selection sort

**Selection sort**: find the smallest element of the unsorted part of the array and put it at the start of that part.

1. For *i* from 0 to *n* − 2:
2. find the index of the smallest element in the range *i* … *n* − 1;
3. swap it with `a[i]`.

After each step the left part is sorted and in its final place. There are always ~*n*²/2 comparisons, but at most *n* − 1 swaps — useful when writing is expensive.

## Insertion sort

**Insertion sort**: like arranging cards in your hand — each new element is inserted into its place in the sorted left part. Larger elements shift one position to the right.

On an almost sorted array insertion sort is very fast — about *n* steps. That is why standard sort functions use it for small pieces.

## Merge sort and quick sort

Both methods rest on the **divide and conquer** idea and are recursive:

| | Merge sort | Quick sort |
|---|---|---|
| Idea | split the array in half, sort each half and merge the two sorted parts | pick a pivot, send smaller elements left and larger ones right, sort each side |
| Complexity | always *O*(*n* log *n*) | *O*(*n* log *n*) on average, *O*(*n*²) in the worst case |
| Extra memory | *O*(*n*) | *O*(log *n*) |
| Stability | stable | not stable |

These algorithms are written in detail in later courses; in practice their ready, well-tested implementations are used.

## Stable sorting and sorting by key

A **stable sort** keeps the relative order of elements with equal keys. For example, if students stand in alphabetical order and we sort them stably by score, students with the same score stay in alphabetical order.

The standard functions:

| Language | Function | Stable? | Key / comparison |
|---|---|---|---|
| Python | `a.sort()`, `sorted(a)` | yes | `key=lambda x: ...`, `reverse=True` |
| Java | `Arrays.sort`, `list.sort` | yes for objects | `Comparator.comparing(...)` |
| C++ | `sort` / `stable_sort` (`<algorithm>`) | `sort` no, `stable_sort` yes | a comparison function (lambda) |

Sorting by several keys: first by the first key, and on a tie by the second. For example, score in **descending** order, and on a tie name in **ascending** order.

## Worked example 1: selection sort and the number of steps

The first line holds *n*, the second *n* numbers. Sort them with selection sort; print the sorted array and the number of swaps made (an element is not swapped with itself).

```python
n = int(input())
a = list(map(int, input().split()))

almashtirish = 0
for i in range(n - 1):
    eng_kichik = i
    for j in range(i + 1, n):
        if a[j] < a[eng_kichik]:
            eng_kichik = j
    if eng_kichik != i:
        a[i], a[eng_kichik] = a[eng_kichik], a[i]
        almashtirish += 1

print(*a)
print(almashtirish)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int almashtirish = 0;
        for (int i = 0; i < n - 1; i++) {
            int engKichik = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[engKichik]) engKichik = j;
            }
            if (engKichik != i) {
                int t = a[i];
                a[i] = a[engKichik];
                a[engKichik] = t;
                almashtirish++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(almashtirish);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    int almashtirish = 0;
    for (int i = 0; i < n - 1; i++) {
        int engKichik = i;
        for (int j = i + 1; j < n; j++) {
            if (a[j] < a[engKichik]) engKichik = j;
        }
        if (engKichik != i) {
            swap(a[i], a[engKichik]);
            almashtirish++;
        }
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl << almashtirish << endl;
    return 0;
}
```

For the input `5` and `29 10 14 37 13` (*eng_kichik* is the smallest, *almashtirish* the swaps):

| *i* | Smallest | Array after the step |
|---|---|---|
| 0 | 10 | 10 29 14 37 13 |
| 1 | 13 | 10 13 14 37 29 |
| 2 | 14 (already in place) | 10 13 14 37 29 |
| 3 | 29 | 10 13 14 29 37 |

The output is `10 13 14 29 37` and `3` — on one of the 4 steps the element was already in place. In C++ `swap` is the ready swap function from `<utility>`.

## Worked example 2: insertion sort

The first line holds *n*, the second *n* numbers. Sort them with insertion sort.

```python
n = int(input())
a = list(map(int, input().split()))

for i in range(1, n):
    joriy = a[i]
    j = i - 1
    while j >= 0 and a[j] > joriy:
        a[j + 1] = a[j]          # shift the larger element right
        j -= 1
    a[j + 1] = joriy             # put it in the freed place

print(*a)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        for (int i = 1; i < n; i++) {
            int joriy = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > joriy) {
                a[j + 1] = a[j];         // shift the larger element right
                j--;
            }
            a[j + 1] = joriy;            // put it in the freed place
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
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    for (int i = 1; i < n; i++) {
        int joriy = a[i];
        int j = i - 1;
        while (j >= 0 && a[j] > joriy) {
            a[j + 1] = a[j];         // shift the larger element right
            j--;
        }
        a[j + 1] = joriy;            // put it in the freed place
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

For the input `6` and `5 2 4 6 1 3` the sorted left part grows by one element on each step (*joriy* is the current one):

| *i* | *joriy* | Array after the step |
|---|---|---|
| 1 | 2 | **2 5** 4 6 1 3 |
| 2 | 4 | **2 4 5** 6 1 3 |
| 3 | 6 | **2 4 5 6** 1 3 |
| 4 | 1 | **1 2 4 5 6** 3 |
| 5 | 3 | **1 2 3 4 5 6** |

The output is `1 2 3 4 5 6`. In the `while` condition `j >= 0` comes first: thanks to short-circuit evaluation `a[-1]` is never accessed.

## Worked example 3: student ranking

The first line holds *n*, the next *n* lines a student's name and score separated by a space. Print the students by score in descending order, and with equal scores alphabetically.

```python
n = int(input())
talabalar = []
for _ in range(n):
    ism, ball = input().split()
    talabalar.append((ism, int(ball)))

talabalar.sort(key=lambda t: (-t[1], t[0]))

for ism, ball in talabalar:
    print(ism, ball)
```
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    record Talaba(String ism, int ball) {}

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        List<Talaba> talabalar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            talabalar.add(new Talaba(in.next(), in.nextInt()));
        }

        talabalar.sort(Comparator.comparingInt(Talaba::ball).reversed()
                .thenComparing(Talaba::ism));

        for (Talaba t : talabalar) {
            System.out.println(t.ism() + " " + t.ball());
        }
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
    int n;
    cin >> n;
    vector<Talaba> talabalar(n);
    for (auto& t : talabalar) cin >> t.ism >> t.ball;

    sort(talabalar.begin(), talabalar.end(), [](const Talaba& x, const Talaba& y) {
        if (x.ball != y.ball) return x.ball > y.ball;   // score: descending
        return x.ism < y.ism;                           // name: alphabetical
    });

    for (const auto& t : talabalar) {
        cout << t.ism << " " << t.ball << endl;
    }
    return 0;
}
```

Input:

```text
5
Malika 88
Aziz 92
Bekzod 88
Dilnoza 75
Anvar 92
```

Output:

```text
Anvar 92
Aziz 92
Bekzod 88
Malika 88
Dilnoza 75
```

In Python the key is the pair `(-ball, ism)`: the minus sign reverses the order of scores while the name keeps its normal order. The Java `Comparator` chain and the C++ comparison function express the same rule: "should *x* come before *y*?". These standard functions run in *O*(*n* log *n*).

:::warning
**Common mistakes**

- **An *O*(*n*²) sort on large data.** For 100,000 elements bubble sort exceeds the time limit; use the standard `sort`.
- **`<=` in a C++ comparison function.** The comparison must be "strictly less"; `<=` leads to undefined behaviour.
- **Not keeping the result of `sorted(a)` in Python.** `sorted` returns a new list; `a.sort()` sorts in place and returns `None`.
- **Relying on stability.** C++ `sort` is not stable; if the order matters, use `stable_sort` or a full key.
:::

:::tip
When testing a sorting algorithm, always try three special inputs: an already sorted array, an array sorted in reverse, and an array of identical elements.
:::

## Summary

- An algorithm's speed is judged by how the number of operations grows with *n*: the gap between *O*(*n*²) and *O*(*n* log *n*) on large *n* is huge.
- Selection sort finds the smallest and puts it first; insertion sort inserts each element into the sorted part; both are *O*(*n*²).
- Merge sort and quick sort are *O*(*n* log *n*) algorithms built on divide and conquer.
- A stable sort keeps the order of equal elements.
- In practice the standard `sort` is used; a key or a comparison function sets any order.

## Check yourself

:::check
Q: How many times longer does an *O*(*n*²) algorithm run if *n* grows 10 times?
A: About 100 times.
Q: How many swaps does selection sort make for the array `4 3 2 1`?
A: 2: 4 ↔ 1, then 3 ↔ 2; on the other steps the elements are already in place.
Q: When is insertion sort very fast?
A: When the array is almost sorted: every element is close to its place, so there are few shifts — about *n* steps.
Q: What is a stable sort?
A: A sort that keeps the original relative order of elements with equal keys.
Q: How do you sort a list in Python by score descending and by name ascending?
A: `a.sort(key=lambda t: (-t.ball, t.ism))` (or for pairs `(-t[1], t[0])`).
:::

:::link https://visualgo.net/en/sorting
VisuAlgo: sorting algorithms
An animation of selection, insertion, merge and quick sort.
:::

:::link https://docs.python.org/3/howto/sorting.html
Python: Sorting HOW TO
About `sorted`, `key`, `reverse` and stability.
:::
