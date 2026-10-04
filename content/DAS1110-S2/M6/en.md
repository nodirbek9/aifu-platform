---
title: Containers (collections)
summary: The idea of a container; the list (list, ArrayList, vector), the set, the dictionary (dict, Map, map), the queue and the deque; the complexity of operations, ordered and hashed containers, iterators, choosing the right container.
---

## Why it matters

Counting how many times each word occurs in a text, removing duplicates from a list, serving customers waiting in a queue — all of this can be written with arrays, but the code turns out long, slow and error-prone. The standard library of every language has ready, well-tested structures for such jobs — **containers** (collections).

Choosing the right container can make a program dozens of times faster: in a list of 10⁵ elements the question "is it there?" takes 10⁵ steps, in a set only a few. In this lecture we learn the main containers, their speed and when to choose which.

:::note
**After this lesson you will be able to:**

- explain what a list, a set, a dictionary, a queue and a deque are and when each is used;
- state the complexity of each container's main operations;
- use these containers in Python, Java and C++;
- explain the difference between ordered and hashed containers;
- choose the container that fits a problem.
:::

## Kinds of containers

| Container | Idea | Python | Java | C++ |
|---|---|---|---|---|
| List | ordered elements, access by index | `list` | `ArrayList` | `vector` |
| Set | elements without duplicates, the question "is it there?" | `set` | `HashSet`, `TreeSet` | `unordered_set`, `set` |
| Dictionary (map) | key → value pairs | `dict` | `HashMap`, `TreeMap` | `unordered_map`, `map` |
| Queue | first in, first out (FIFO) | `collections.deque` | `ArrayDeque` (`Queue`) | `queue` |
| Deque | adding and removing at both ends | `collections.deque` | `ArrayDeque` (`Deque`) | `deque` |
| Stack | last in, first out (LIFO) | `list` (`append`/`pop`) | `ArrayDeque` (`push`/`pop`) | `stack` |

## The complexity of operations

| Operation | List | Hashed set/map | Ordered set/map | Deque |
|---|---|---|---|---|
| Add at the end | *O*(1), on average | — | — | *O*(1) |
| Add at the front | *O*(*n*) | — | — | *O*(1) |
| Get by index | *O*(1) | — | — | *O*(1) |
| Is the element there? | *O*(*n*) | *O*(1), on average | *O*(log *n*) | *O*(*n*) |
| Add / remove (by key) | *O*(*n*) | *O*(1), on average | *O*(log *n*) | — |
| Iterating in order | in insertion order | order not guaranteed | in **sorted** order | in insertion order |

"On average" means in most cases; when adding to a list, memory is grown from time to time and one addition then takes longer.

A **hashed** container (`HashSet`, `unordered_map`, Python's `dict`/`set`) finds an element's place at once from its **hash** value — very fast, but the elements are unordered. An **ordered** container (`TreeMap`, C++ `map`/`set`) keeps the elements in a balanced tree — a little slower, but when iterated the keys come in sorted order. In Python a `dict` keeps keys **in insertion order**; if sorted order is needed, `sorted(d)` is used.

## Working with a dictionary

A dictionary is a "key → value" table: the value is found quickly by its key. The most common job is **counting**:

| Language | Increasing a value | Is the key there? |
|---|---|---|
| Python | `d[k] = d.get(k, 0) + 1` or `collections.Counter` | `k in d` |
| Java | `m.merge(k, 1, Integer::sum)` or `m.put(k, m.getOrDefault(k, 0) + 1)` | `m.containsKey(k)` |
| C++ | `m[k]++` (a missing key is created with 0 automatically) | `m.count(k)` or `m.find(k) != m.end()` |

In C++, `m[k]` creates a missing key even when used only to **read** — so `count` or `find` is used for checking.

## Iterators

To go through a container's elements, a "for each element" loop works in every language: `for x in s`, `for (int x : s)`, `for (int x : s)`. Behind it stands an **iterator** — an object pointing at the current position in the container. In C++ iterators are used openly: `s.begin()` is the first element, `s.end()` the position after the last; algorithms such as `sort(v.begin(), v.end())` and `find` work exactly with iterators.

Removing elements from a container while iterating over it is dangerous: Java throws a `ConcurrentModificationException`, C++ has undefined behaviour, Python raises a `RuntimeError` (for a dictionary) or skips elements.

## Worked example 1: word frequencies (a dictionary)

One line of text is given (lowercase Latin letters and spaces). Print each word and how many times it occurs, words in alphabetical order.

```python
matn = input().split()

chastota = {}
for soz in matn:
    chastota[soz] = chastota.get(soz, 0) + 1

for soz in sorted(chastota):          # dict keeps insertion order — we sort
    print(soz, chastota[soz])
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] matn = in.nextLine().trim().split("\\s+");

        Map<String, Integer> chastota = new TreeMap<>();   // keys are sorted
        for (String soz : matn) chastota.merge(soz, 1, Integer::sum);

        for (Map.Entry<String, Integer> e : chastota.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    map<string, int> chastota;             // keys are sorted
    string soz;
    while (cin >> soz) chastota[soz]++;    // a missing key is created with 0

    for (const auto& [kalit, soni] : chastota) {
        cout << kalit << " " << soni << endl;
    }
    return 0;
}
```

Input:

```text
bir ikki bir uch ikki bir
```

Output:

```text
bir 3
ikki 2
uch 1
```

Java's `TreeMap` and C++'s `map` are ordered dictionaries, so when iterated the keys come sorted by themselves. Python's `dict` keeps insertion order, so `sorted` was added (*chastota* means frequency, *soz* word). In C++ `auto& [kalit, soni]` splits a pair into two variables (C++17).

## Worked example 2: sets — common elements

The first and second lines hold integers (repeats possible). Print the numbers occurring in both lines, without repeats, in ascending order (or `-` if there are none), and on the next line how many **distinct** numbers were given in total.

```python
a = set(map(int, input().split()))
b = set(map(int, input().split()))

umumiy = sorted(a & b)                 # intersection
print(*umumiy if umumiy else "-")
print(len(a | b))                      # union
```
```java
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    static Set<Integer> oqish(String satr) {
        Set<Integer> s = new HashSet<>();
        for (String t : satr.trim().split("\\s+")) s.add(Integer.parseInt(t));
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Set<Integer> a = oqish(in.nextLine());
        Set<Integer> b = oqish(in.nextLine());

        Set<Integer> umumiy = new TreeSet<>(a);   // a sorted copy
        umumiy.retainAll(b);                      // intersection
        Set<Integer> hammasi = new HashSet<>(a);
        hammasi.addAll(b);                        // union

        StringBuilder sb = new StringBuilder();
        for (int x : umumiy) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        System.out.println(umumiy.isEmpty() ? "-" : sb.toString());
        System.out.println(hammasi.size());
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <sstream>
#include <string>
using namespace std;

set<int> oqish() {
    string satr;
    getline(cin, satr);
    istringstream oqim(satr);
    set<int> s;
    int x;
    while (oqim >> x) s.insert(x);         // a repeat is not added
    return s;
}

int main() {
    set<int> a = oqish();
    set<int> b = oqish();

    bool birinchi = true;
    for (int x : a) {                      // a set is sorted
        if (b.count(x)) {
            if (!birinchi) cout << ' ';
            cout << x;
            birinchi = false;
        }
    }
    if (birinchi) cout << "-";
    cout << endl;

    set<int> hammasi = a;
    hammasi.insert(b.begin(), b.end());
    cout << hammasi.size() << endl;
    return 0;
}
```

Input:

```text
5 1 3 3 9 7
3 8 5 5 2
```

Output:

```text
3 5
7
```

The distinct numbers are 1, 2, 3, 5, 7, 8, 9 — 7 in all. A set does not take repeats, so "the number of distinct elements" is simply the set's size (*umumiy* means common, *hammasi* all). In Python `&` and `|` are the intersection and union operations; in Java `retainAll` and `addAll`. With arrays this problem would need nested loops and take *O*(*n* · *m*) time; with sets it is almost linear.

## Worked example 3: a queue — the counting-out game

*n* people stand in a circle (numbered 1 to *n*). Counting starts from person 1 and every *k*-th person leaves the circle; the count continues from the next person. Print the order in which they leave (this is called the Josephus problem).

```python
from collections import deque

n, k = map(int, input().split())
navbat = deque(range(1, n + 1))

tartib = []
while navbat:
    navbat.rotate(-(k - 1))            # move k-1 people to the end
    tartib.append(navbat.popleft())    # the k-th person leaves
print(*tartib)
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        Deque<Integer> navbat = new ArrayDeque<>();
        for (int i = 1; i <= n; i++) navbat.addLast(i);

        StringBuilder sb = new StringBuilder();
        while (!navbat.isEmpty()) {
            for (int i = 0; i < k - 1; i++) navbat.addLast(navbat.pollFirst());   // from the front to the end
            if (sb.length() > 0) sb.append(' ');
            sb.append(navbat.pollFirst());                                     // the k-th person leaves
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <queue>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    queue<int> navbat;
    for (int i = 1; i <= n; i++) navbat.push(i);

    bool birinchi = true;
    while (!navbat.empty()) {
        for (int i = 0; i < k - 1; i++) {      // from the front to the end
            navbat.push(navbat.front());
            navbat.pop();
        }
        if (!birinchi) cout << ' ';
        cout << navbat.front();                // the k-th person leaves
        navbat.pop();
        birinchi = false;
    }
    cout << endl;
    return 0;
}
```

For the input `7 3` the output is `3 6 2 7 5 1 4`. The queue (*navbat*) models the circle: a person counted but not leaving is taken from the front of the queue and put at the end. Taking from the front is *O*(1) in a deque and a queue, but `pop(0)` on a Python `list` is *O*(*n*), because the remaining elements are shifted. That is why a `deque`, not a `list`, is used for a queue.

:::warning
**Common mistakes**

- **Asking "is it there?" of a list many times.** `if x in royxat` inside a loop leads to *O*(*n*²); use a set.
- **Checking with `m[k]` in C++.** A missing key gets created and the dictionary grows; use `count` or `find`.
- **Expecting order from a hashed container.** `HashMap` and `unordered_map` give elements in arbitrary order; if order matters — `TreeMap`, `map` or sorting.
- **`list.pop(0)` for a queue in Python.** Every removal is *O*(*n*); use `deque.popleft()`.
:::

:::tip
When choosing a container, find the problem's **most frequent operation**: getting by index — a list; "is it there?" — a set; a value by key — a dictionary; working at both ends — a deque. If order matters — the ordered variant.
:::

## Summary

- Containers are the standard library's ready data structures: list, set, dictionary, queue, deque, stack.
- A list is fast by index but slow to search; sets and dictionaries are fast by key.
- Hashed containers are *O*(1) on average, ordered ones *O*(log *n*) but iterate in sorted order.
- A queue and a deque take from the front in *O*(1).
- A container is chosen by the operation the problem performs most often.

## Check yourself

:::check
Q: How long does "is the element there?" take in a list and in a hashed set?
A: *O*(*n*) in a list, *O*(1) on average in a hashed set.
Q: What is the main difference between `HashMap` and `TreeMap`?
A: `HashMap` is faster (*O*(1)) but unordered; `TreeMap` is *O*(log *n*) but keeps the keys in sorted order.
Q: What hidden effect does `if (m["olma"] > 0)` have in C++?
A: If the key "olma" does not exist, it is created with the value 0 and added to the dictionary.
Q: Why is `deque` used for a queue in Python?
A: `deque.popleft()` is *O*(1), while `list.pop(0)` is *O*(*n*) — the remaining elements are shifted.
Q: How can you find the number of distinct words in a line?
A: Put the words into a set and take its size: `len(set(matn.split()))`.
:::

:::link https://docs.python.org/3/library/collections.html
Python: the collections module
`deque`, `Counter`, `defaultdict` and other containers.
:::

:::link https://en.cppreference.com/w/cpp/container
C++: the containers library
`vector`, `set`, `map`, `deque`, `queue` and their complexity.
:::
