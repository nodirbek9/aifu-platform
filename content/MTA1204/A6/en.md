---
title: Dynamic data structures. The queue, the stack, the deque. Lists (practical session)
summary: Evaluating a postfix (reverse Polish) expression with a stack, the sliding-window maximum with a deque (a monotone deque), a text editor's cursor with a doubly linked list; choosing a structure by the problem's main operation.
---

## Why it matters

The stack, the queue and the deque are simple yet very powerful structures. A calculator evaluates an expression with a stack, a stock chart finds the highest price of the last minutes with a deque, and a text editor inserts and deletes letters around the cursor quickly with the idea of a linked list. In this session we solve these three practical problems.

:::note
**After this session you will be able to:**

- evaluate a postfix expression with a stack;
- keep a deque "monotone" and find the sliding-window maximum in *O*(*n*);
- insert and delete nodes of a doubly linked list with pointers;
- choose a suitable structure by the problem's main operation.
:::

## Postfix notation and the stack

In ordinary (infix) notation `(3 + 4) * 2` needs brackets and operator precedence. In **postfix** (reverse Polish) notation the operator is written **after** its operands: `3 4 + 2 *`. It needs neither brackets nor precedence, and evaluation with a stack is very simple:

```text
for every token:
    a number → push it onto the stack
    an operator → pop two numbers (b first, then a), push a op b
at the end the stack holds one number — the answer
```

Compilers and calculators first turn an expression into postfix form (Dijkstra's "shunting-yard" algorithm) and then evaluate it exactly like this.

## The monotone deque

**The sliding-window maximum**: a window of length *k* slides along the array; for every position find the largest element in the window. Re-scanning the window each time is *O*(*n* · *k*). With a deque it is *O*(*n*):

- the deque holds **indices** whose values **decrease** from front to back;
- when a new element arrives, smaller or equal values are removed from the back — they can never be the maximum again;
- if the index at the front has left the window, it is removed from the front;
- the front of the deque is the current window's maximum.

Every index enters the deque once and leaves once — *O*(*n*) in total.

## The doubly linked list

In a doubly linked list every node keeps `oldingi` (previous) and `keyingi` (next) pointers. Inserting before or after a known node and deleting a node take only a few pointer changes, *O*(1). A **sentinel node** is a dummy node at the start of the list: it removes special cases such as an empty list or "deleting the head".

## Worked example 1: evaluating a postfix expression

One line holds a postfix expression separated by spaces: integers and the operators `+`, `-`, `*`. Print the value of the expression.

```python
belgilar = input().split()
stek = []
for b in belgilar:
    if b in ("+", "-", "*"):
        ikkinchi = stek.pop()           # the right operand first
        birinchi = stek.pop()
        if b == "+":
            stek.append(birinchi + ikkinchi)
        elif b == "-":
            stek.append(birinchi - ikkinchi)
        else:
            stek.append(birinchi * ikkinchi)
    else:
        stek.append(int(b))             # a number goes onto the stack
print(stek.pop())
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String[] belgilar = new Scanner(System.in).nextLine().trim().split("\\s+");
        Deque<Long> stek = new ArrayDeque<>();
        for (String b : belgilar) {
            if (b.equals("+") || b.equals("-") || b.equals("*")) {
                long ikkinchi = stek.pop();     // the right operand first
                long birinchi = stek.pop();
                if (b.equals("+")) stek.push(birinchi + ikkinchi);
                else if (b.equals("-")) stek.push(birinchi - ikkinchi);
                else stek.push(birinchi * ikkinchi);
            } else {
                stek.push(Long.parseLong(b));   // a number goes onto the stack
            }
        }
        System.out.println(stek.pop());
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <stack>
#include <string>
using namespace std;

int main() {
    string satr, b;
    getline(cin, satr);
    istringstream oqim(satr);
    stack<long long> stek;
    while (oqim >> b) {
        if (b == "+" || b == "-" || b == "*") {
            long long ikkinchi = stek.top();    // the right operand first
            stek.pop();
            long long birinchi = stek.top();
            stek.pop();
            if (b == "+") stek.push(birinchi + ikkinchi);
            else if (b == "-") stek.push(birinchi - ikkinchi);
            else stek.push(birinchi * ikkinchi);
        } else {
            stek.push(stoll(b));                // a number goes onto the stack
        }
    }
    cout << stek.top() << endl;
    return 0;
}
```

For `3 4 + 2 *` the output is `14` ((3 + 4) · 2); for `5 1 2 + 4 * + 3 -` it is also `14` (5 + (1 + 2) · 4 − 3); for `2 7 -` it is `-5` (*stek* is the stack, *birinchi*/*ikkinchi* the first/second operand). With subtraction the order of operands matters: the number popped first is the **right** operand. A negative number such as `-5` also arrives as a token and is not taken for an operator, since it is not exactly `-`.

## Worked example 2: the sliding-window maximum

The first line holds *n* and *k*, the second *n* numbers. Print the maximum of every consecutive window of length *k* (*n* − *k* + 1 numbers in total).

```python
from collections import deque

n, k = map(int, input().split())
a = list(map(int, input().split()))
dek = deque()                           # indices; their values decrease
natija = []
for i in range(n):
    while dek and a[dek[-1]] <= a[i]:   # can never be the maximum again
        dek.pop()
    dek.append(i)
    if dek[0] <= i - k:                 # it has left the window
        dek.popleft()
    if i >= k - 1:
        natija.append(a[dek[0]])        # the front of the deque is the maximum
print(*natija)
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Deque<Integer> dek = new ArrayDeque<>();            // indices; their values decrease
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            while (!dek.isEmpty() && a[dek.peekLast()] <= a[i]) dek.pollLast();   // can never be the maximum again
            dek.addLast(i);
            if (dek.peekFirst() <= i - k) dek.pollFirst();  // it has left the window
            if (i >= k - 1) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(a[dek.peekFirst()]);              // the front of the deque is the maximum
            }
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <deque>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    deque<int> dek;                                         // indices; their values decrease
    bool birinchi = true;
    for (int i = 0; i < n; i++) {
        while (!dek.empty() && a[dek.back()] <= a[i]) dek.pop_back();   // can never be the maximum again
        dek.push_back(i);
        if (dek.front() <= i - k) dek.pop_front();          // it has left the window
        if (i >= k - 1) {
            cout << (birinchi ? "" : " ") << a[dek.front()];    // the front of the deque is the maximum
            birinchi = false;
        }
    }
    cout << endl;
    return 0;
}
```

For `8 3` and `1 3 -1 -3 5 3 6 7` the output is `3 3 5 5 6 7`. For example, when `5` arrives, `-1` and `-3` are removed from the deque: they are to the left of `5` and smaller — while `5` is in the window they can never be the maximum (*dek* is the deque, *natija* the result). The deque holds **indices, not values** — the index tells us when an element has left the window.

## Worked example 3: a cursor in a doubly linked list

A text editor: the text is empty at first, the cursor at the end. The first line holds *q*, then commands: `yoz C` — insert the character *C* before the cursor, `chap` / `ong` — move the cursor one character left / right (it stays put at an edge), `ochir` — delete the character before the cursor (if any). Finally print the text. The text is kept in a doubly linked list, and the cursor is a pointer to the node on its left.

```python
class Tugun:
    def __init__(self, belgi):
        self.belgi = belgi
        self.oldingi = None
        self.keyingi = None


qoriqchi = Tugun("")                        # a dummy head node — no special cases
kursor = qoriqchi                           # the node left of the cursor

q = int(input())
for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "yoz":
        t = Tugun(buyruq[1])                # between the cursor and the next node
        t.oldingi = kursor
        t.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = t
        kursor.keyingi = t
        kursor = t
    elif buyruq[0] == "chap":
        if kursor is not qoriqchi:
            kursor = kursor.oldingi
    elif buyruq[0] == "ong":
        if kursor.keyingi:
            kursor = kursor.keyingi
    elif kursor is not qoriqchi:            # ochir: remove the node left of the cursor
        kursor.oldingi.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = kursor.oldingi
        kursor = kursor.oldingi

belgilar = []
t = qoriqchi.keyingi
while t:
    belgilar.append(t.belgi)
    t = t.keyingi
print("".join(belgilar))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        char belgi;
        Tugun oldingi, keyingi;
        Tugun(char belgi) { this.belgi = belgi; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun qoriqchi = new Tugun(' ');                    // a dummy head node — no special cases
        Tugun kursor = qoriqchi;                            // the node left of the cursor

        int q = in.nextInt();
        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("yoz")) {
                Tugun t = new Tugun(in.next().charAt(0));   // between the cursor and the next node
                t.oldingi = kursor;
                t.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = t;
                kursor.keyingi = t;
                kursor = t;
            } else if (buyruq.equals("chap")) {
                if (kursor != qoriqchi) kursor = kursor.oldingi;
            } else if (buyruq.equals("ong")) {
                if (kursor.keyingi != null) kursor = kursor.keyingi;
            } else if (kursor != qoriqchi) {                // ochir: remove the node left of the cursor
                kursor.oldingi.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = kursor.oldingi;
                kursor = kursor.oldingi;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Tugun t = qoriqchi.keyingi; t != null; t = t.keyingi) sb.append(t.belgi);
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

struct Tugun {
    char belgi;
    Tugun* oldingi = nullptr;
    Tugun* keyingi = nullptr;
};

int main() {
    Tugun* qoriqchi = new Tugun{' '};                       // a dummy head node — no special cases
    Tugun* kursor = qoriqchi;                               // the node left of the cursor

    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "yoz") {
            char c;
            cin >> c;
            Tugun* t = new Tugun{c};                        // between the cursor and the next node
            t->oldingi = kursor;
            t->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = t;
            kursor->keyingi = t;
            kursor = t;
        } else if (buyruq == "chap") {
            if (kursor != qoriqchi) kursor = kursor->oldingi;
        } else if (buyruq == "ong") {
            if (kursor->keyingi) kursor = kursor->keyingi;
        } else if (kursor != qoriqchi) {                    // ochir: remove the node left of the cursor
            Tugun* eski = kursor;
            kursor->oldingi->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = kursor->oldingi;
            kursor = kursor->oldingi;
            delete eski;
        }
    }

    for (Tugun* t = qoriqchi->keyingi; t; t = t->keyingi) cout << t->belgi;
    cout << endl;
    while (qoriqchi) {
        Tugun* keyingi = qoriqchi->keyingi;
        delete qoriqchi;
        qoriqchi = keyingi;
    }
    return 0;
}
```

Input:

```text
8
yoz a
yoz b
yoz c
chap
ochir
yoz x
ong
yoz d
```

Output:

```text
axcd
```

After `abc` is written, the cursor is to the right of `c`; `chap` moves it between `b` and `c`, and `ochir` deletes `b` (*qoriqchi* is the sentinel, *kursor* the cursor). Every command changes a few pointers — *O*(1) whatever the text's length. In an array, writing or deleting in the middle would shift the remaining characters every time. Thanks to the sentinel node, "we are at the start of the text" becomes a plain `kursor == qoriqchi` check.

:::warning
**Common mistakes**

- **Swapping the operands in postfix.** In `a b -` the stack gives `b` first; `a − b` is computed, not `b − a`.
- **Keeping values in a monotone deque.** An index is needed to know when an element leaves the window.
- **Forgetting to update one pointer in a doubly linked list.** Insertion changes four pointers and deletion two; if one is missed, the list "breaks".
- **Popping from an empty stack.** With an invalid expression the stack may run empty; check this in a real program.
:::

:::tip
Draw pointer-based structures on paper with nodes as rectangles and pointers as arrows. Comparing the picture before and after each operation shows at once which arrow was not updated.
:::

## Summary

- A stack evaluates a postfix expression without brackets or precedence.
- A monotone deque finds the sliding-window maximum in *O*(*n*): every index enters and leaves once.
- In a doubly linked list inserting and deleting around a known node is *O*(1); a sentinel node removes special cases.
- A structure is chosen by the problem's main operation: taking the last — a stack, working at both ends — a deque, changing in the middle — a linked list.

## Check yourself

:::check
Q: What is the value of the postfix expression `2 3 4 * +`?
A: 14: 2 + 3 · 4.
Q: Why are indices with small values removed from the back of a monotone deque?
A: There is a larger element to their right; while it is in the window they can never be the maximum.
Q: What is the complexity of the sliding-window maximum with a monotone deque?
A: *O*(*n*) — every index is added to the deque once and removed at most once.
Q: Why is a sentinel node needed?
A: It keeps the list from ever being empty and removes special cases such as deleting the head.
:::

:::link https://en.wikipedia.org/wiki/Reverse_Polish_notation
Reverse Polish notation
Postfix notation and evaluating it with a stack.
:::
