---
title: Parallel sorting algorithms. Dynamic structures — the queue, the stack and the deque
summary: The Bose–Nelson and Batcher sorting networks, comparators and network depth, odd–even (transposition) sort, the advantage of parallel sorting; dynamic data structures — the stack, the queue and the deque, built with an array (a circular buffer) and with pointers.
---

## Why it matters

A modern processor has 8–16 cores, and a graphics card thousands of computing units. Ordinary sorting algorithms, however, work sequentially: every comparison waits for the result of the previous one. **Parallel sorting** algorithms are designed to perform many comparisons **at the same time** — they are used on graphics cards, in network switches and in special hardware.

The second part of the lecture is about **dynamic structures**: the stack, the queue and the deque. They are in almost every program: function calls are kept on a stack, documents sent to a printer wait in a queue, and a browser's "back" and "forward" buttons work with stacks.

:::note
**After this lesson you will be able to:**

- explain the notions of a sorting network, a comparator and network depth;
- write odd–even (transposition) sort and justify why it is parallel;
- state how Batcher's network is built and what its complexity is;
- tell the operations and uses of the stack, the queue and the deque apart;
- build a queue as a circular buffer in an array.
:::

## Sorting networks

A **sorting network** is a predetermined sequence of **comparators**. Each comparator (*i*, *j*) compares two elements and swaps them if needed, so that `a[i] ≤ a[j]`. The key property: the order of comparisons does **not depend** on the data — so comparators that do not touch one another can run at the same time.

| Notion | Meaning |
|---|---|
| Comparator | a pair (*i*, *j*): swap if `a[i] > a[j]` |
| Network size | the number of comparators — determines time when run sequentially |
| Network depth | the number of parallel stages — determines time when run in parallel |

**The Bose–Nelson network** (1962) is one of the first ways of building a sorting network recursively: the array is split in two, each half is sorted by its own network, and the two results are combined by a special merging network.

**Batcher's odd–even merging network** (K. Batcher, 1968) develops this idea: the elements at even and odd positions of two sorted sequences are merged separately, and then neighbouring pairs are checked. The result:

| *n* | Comparators | Depth |
|---|---|---|
| 2 | 1 | 1 |
| 4 | 5 | 3 |
| 8 | 19 | 6 |
| 16 | 63 | 10 |

Its size is *O*(*n* log² *n*) — more than sequential algorithms (*O*(*n* log *n*)) — but its depth is only *O*(log² *n*): with *n* processors, sorting ends in log² *n* stages. Another famous network of Batcher's is **bitonic sort**; it is widely used on graphics cards.

## Odd–even transposition sort

The simplest parallel sort: in an **even phase** the pairs (0, 1), (2, 3), (4, 5), … are compared and swapped if needed, in an **odd phase** the pairs (1, 2), (3, 4), …. The comparisons within one phase do not touch one another — they can run at the same time. *n* phases are always enough, so with *n*/2 processors the time is *O*(*n*) (sequentially it is *O*(*n*²) — in fact it is a parallel form of bubble sort).

## The stack, the queue and the deque

| Structure | Principle | Main operations | Uses |
|---|---|---|---|
| **Stack** | last in, first out (LIFO) | `push`, `pop`, `top` | function calls, "undo" (Ctrl+Z), evaluating expressions, checking brackets |
| **Queue** | first in, first out (FIFO) | `enqueue` (at the end), `dequeue` (from the front) | a printer queue, server requests, breadth-first traversal of a graph |
| **Deque** | adding and removing at both ends | `push_front`, `push_back`, `pop_front`, `pop_back` | a sliding window, browser history |

All operations are *O*(1). There are two ways to build them:

- **With an array**: a stack is an array and a "top" index. A plain array does not suit a queue (taking from the front requires shifting elements), so a **circular buffer** is used: the `bosh` (head) and `oxir` (tail) indices wrap back to the start with `% capacity` when they reach the end of the array.
- **With pointers** (linked nodes): every element keeps a pointer to the next one; the capacity is unlimited, but every element needs extra memory.

In the standard libraries: Python — `list` (a stack), `collections.deque` (a queue, a deque); Java — `ArrayDeque`; C++ — `stack`, `queue`, `deque`.

## Worked example 1: odd–even transposition sort

The first line holds *n*, the second *n* numbers. Sort them with odd–even sort: phases alternate between the even pairs (from 0) and the odd pairs (from 1); stop when two phases in a row make no swap (or after *n* phases). Print the sorted array and the number of phases performed.

```python
n = int(input())
a = list(map(int, input().split()))

faza = 0
tinch = 0                                   # consecutive phases without swaps
while tinch < 2 and faza < n:
    almashdi = False
    for i in range(faza % 2, n - 1, 2):     # these comparisons do not touch one another
        if a[i] > a[i + 1]:
            a[i], a[i + 1] = a[i + 1], a[i]
            almashdi = True
    faza += 1
    tinch = 0 if almashdi else tinch + 1

print(*a)
print(faza)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int faza = 0, tinch = 0;                    // consecutive phases without swaps
        while (tinch < 2 && faza < n) {
            boolean almashdi = false;
            for (int i = faza % 2; i < n - 1; i += 2) {     // these comparisons do not touch one another
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            faza++;
            tinch = almashdi ? 0 : tinch + 1;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(faza);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    int faza = 0, tinch = 0;                        // consecutive phases without swaps
    while (tinch < 2 && faza < n) {
        bool almashdi = false;
        for (int i = faza % 2; i < n - 1; i += 2) {         // these comparisons do not touch one another
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        faza++;
        tinch = almashdi ? 0 : tinch + 1;
    }

    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl << faza << endl;
    return 0;
}
```

For `8` and `5 1 4 2 8 3 7 6` the output is `1 2 3 4 5 6 7 8` and `6`; for the reversed `8 7 6 5 4 3 2 1` it is 8 phases (the worst case: *n* phases). The *n*/2 comparisons within one phase are independent of one another: on a parallel computer each one is done by its own processor, and one phase finishes in a single step (*faza* is a phase, *tinch* the quiet phases, *almashdi* "swapped"). On a sequential computer, however, this is no different from ordinary bubble sort.

## Worked example 2: Batcher's sorting network

*k* is given (1 ≤ *k* ≤ 4), then 2ᵏ numbers. Build Batcher's odd–even merging network recursively, apply it to the array; print the number of comparators and the sorted array.

```python
def birlashtir(chap, ong, r, tarmoq):
    qadam = r * 2
    if qadam < ong - chap:
        birlashtir(chap, ong, qadam, tarmoq)            # the even positions
        birlashtir(chap + r, ong, qadam, tarmoq)        # the odd positions
        for i in range(chap + r, ong - r, qadam):       # checking neighbours
            tarmoq.append((i, i + r))
    else:
        tarmoq.append((chap, chap + r))


def tarmoq_qur(chap, ong, tarmoq):
    if ong - chap >= 1:
        orta = chap + (ong - chap) // 2
        tarmoq_qur(chap, orta, tarmoq)
        tarmoq_qur(orta + 1, ong, tarmoq)
        birlashtir(chap, ong, 1, tarmoq)


k = int(input())
a = list(map(int, input().split()))
tarmoq = []
tarmoq_qur(0, 2 ** k - 1, tarmoq)
for i, j in tarmoq:                                     # a comparator
    if a[i] > a[j]:
        a[i], a[j] = a[j], a[i]
print(len(tarmoq))
print(*a)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static List<int[]> tarmoq = new ArrayList<>();

    static void birlashtir(int chap, int ong, int r) {
        int qadam = r * 2;
        if (qadam < ong - chap) {
            birlashtir(chap, ong, qadam);                   // the even positions
            birlashtir(chap + r, ong, qadam);               // the odd positions
            for (int i = chap + r; i < ong - r; i += qadam) tarmoq.add(new int[] {i, i + r});   // checking neighbours
        } else {
            tarmoq.add(new int[] {chap, chap + r});
        }
    }

    static void tarmoqQur(int chap, int ong) {
        if (ong - chap >= 1) {
            int orta = chap + (ong - chap) / 2;
            tarmoqQur(chap, orta);
            tarmoqQur(orta + 1, ong);
            birlashtir(chap, ong, 1);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        int n = 1 << k;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        tarmoqQur(0, n - 1);
        for (int[] t : tarmoq) {                            // a comparator
            if (a[t[0]] > a[t[1]]) {
                int x = a[t[0]]; a[t[0]] = a[t[1]]; a[t[1]] = x;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(tarmoq.size());
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

vector<pair<int, int>> tarmoq;

void birlashtir(int chap, int ong, int r) {
    int qadam = r * 2;
    if (qadam < ong - chap) {
        birlashtir(chap, ong, qadam);                       // the even positions
        birlashtir(chap + r, ong, qadam);                   // the odd positions
        for (int i = chap + r; i < ong - r; i += qadam) tarmoq.push_back({i, i + r});   // checking neighbours
    } else {
        tarmoq.push_back({chap, chap + r});
    }
}

void tarmoqQur(int chap, int ong) {
    if (ong - chap >= 1) {
        int orta = chap + (ong - chap) / 2;
        tarmoqQur(chap, orta);
        tarmoqQur(orta + 1, ong);
        birlashtir(chap, ong, 1);
    }
}

int main() {
    int k;
    cin >> k;
    int n = 1 << k;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    tarmoqQur(0, n - 1);
    for (auto [i, j] : tarmoq) {                            // a comparator
        if (a[i] > a[j]) swap(a[i], a[j]);
    }

    cout << tarmoq.size() << endl;
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl;
    return 0;
}
```

For `3` and `5 1 4 2 8 3 7 6` the output is `19` and `1 2 3 4 5 6 7 8`. The network is built **before the data**: whatever the input, the same 19 comparators run in the same order — which allows them to be "hard-wired" into hardware or run in parallel on a graphics card (*tarmoq* is the network, *birlashtir* merge, *qadam* the step). Run sequentially, however, the network does a little more work: merge sort makes at most 17 comparisons for 8 elements.

## Worked example 3: a queue in a circular buffer

The first line holds the capacity *C* and the number of commands *q*, then *q* commands: `qosh X` — add to the end of the queue (`full` if it is full), `ol` — take from the front and print (`empty` if it is empty), `hajm` — the number of elements. The queue is kept in an ordinary array of size *C*.

```python
c, q = map(int, input().split())
bufer = [0] * c
bosh = 0                                    # the index of the first element
hajm = 0

for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "qosh":
        if hajm == c:
            print("full")
        else:
            bufer[(bosh + hajm) % c] = int(buyruq[1])    # the tail wraps around the ring
            hajm += 1
    elif buyruq[0] == "ol":
        if hajm == 0:
            print("empty")
        else:
            print(bufer[bosh])
            bosh = (bosh + 1) % c                        # the head wraps around the ring too
            hajm -= 1
    else:
        print(hajm)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int c = in.nextInt(), q = in.nextInt();
        int[] bufer = new int[c];
        int bosh = 0, hajm = 0;                             // bosh is the index of the first element

        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("qosh")) {
                int x = in.nextInt();
                if (hajm == c) {
                    System.out.println("full");
                } else {
                    bufer[(bosh + hajm) % c] = x;           // the tail wraps around the ring
                    hajm++;
                }
            } else if (buyruq.equals("ol")) {
                if (hajm == 0) {
                    System.out.println("empty");
                } else {
                    System.out.println(bufer[bosh]);
                    bosh = (bosh + 1) % c;                  // the head wraps around the ring too
                    hajm--;
                }
            } else {
                System.out.println(hajm);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int c, q;
    cin >> c >> q;
    vector<int> bufer(c);
    int bosh = 0, hajm = 0;                                 // bosh is the index of the first element

    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "qosh") {
            int x;
            cin >> x;
            if (hajm == c) {
                cout << "full" << endl;
            } else {
                bufer[(bosh + hajm) % c] = x;               // the tail wraps around the ring
                hajm++;
            }
        } else if (buyruq == "ol") {
            if (hajm == 0) {
                cout << "empty" << endl;
            } else {
                cout << bufer[bosh] << endl;
                bosh = (bosh + 1) % c;                      // the head wraps around the ring too
                hajm--;
            }
        } else {
            cout << hajm << endl;
        }
    }
    return 0;
}
```

Input:

```text
3 11
qosh 5
qosh 7
qosh 9
qosh 1
ol
qosh 1
ol
ol
ol
ol
hajm
```

Output:

```text
full
5
7
9
1
empty
0
```

The capacity is 3: the fourth `qosh` was refused. After `5` was taken, `1` was written into the freed place (`bufer[0]`) — even though `bosh` now stands at index 1: the tail is `(bosh + hajm) % c` = (1 + 2) % 3 = 0. This way the array works like a ring and elements never need shifting — every operation is *O*(1) (*bufer* is the buffer, *hajm* the size). Network cards and audio and video streams use exactly circular buffers.

:::warning
**Common mistakes**

- **Taking from the front of a queue in a plain array.** `a.pop(0)` or shifting elements is *O*(*n*); use a circular buffer or a `deque`.
- **Forgetting `% capacity` in a circular buffer.** The index runs past the end of the array.
- **Trying to tell "full" from "empty" by `bosh == oxir` alone.** The indices are equal in both cases; keep a separate `hajm` counter.
- **Changing a sorting network depending on the data.** The point of a network is that the order of comparisons is known in advance; if it depends on the data, the parallelism is lost.
:::

:::tip
Draw a circular buffer on paper as a circle: cells clockwise, `bosh` and `oxir` as two arrows. After every command mark how the arrows move.
:::

## Summary

- A sorting network is a sequence of comparators that does not depend on the data; its size determines sequential time and its depth parallel time.
- Odd–even transposition sort finishes in *n* phases; the comparisons in each phase can run at the same time.
- Batcher's network: size *O*(*n* log² *n*), depth *O*(log² *n*); 19 comparators for 8 elements.
- The stack (LIFO), the queue (FIFO) and the deque (both ends) — all operations *O*(1).
- A queue is built in an array as a circular buffer: the indices wrap around with `% capacity`.

## Check yourself

:::check
Q: What do a sorting network's size and depth mean?
A: Size is the number of comparators (sequential time), depth the number of parallel stages (parallel time).
Q: Why can the comparisons within one phase of odd–even sort run in parallel?
A: They work on pairs that do not overlap — every element takes part in only one comparison.
Q: In a circular buffer of capacity 5 with `bosh` = 3 and `hajm` = 4, at which index is a new element written?
A: (3 + 4) % 5 = 2.
Q: Which structure does a browser's "back" button work with?
A: A stack: the page opened last is returned first.
:::

:::link https://en.wikipedia.org/wiki/Batcher_odd%E2%80%93even_mergesort
Batcher odd–even mergesort
How the network is built, and its size and depth.
:::

:::link https://en.wikipedia.org/wiki/Circular_buffer
Circular buffer
A way of building a queue in an array.
:::
