---
title: Recursive functions. Organising a user library
summary: Recursion over an array, memoization (Fibonacci), generating all variants by backtracking, building your own library of functions — a Python module, a Java class, and a C++ header file with a namespace.
---

## Why it matters

In the first semester we saw the basics of recursion: the base case, the recursive step and the call stack. In practice recursion is especially useful in three situations: processing data **piece by piece** (an array, a string), **remembering results** when the same computations repeat many times, and **enumerating all variants** (passwords, timetables, game moves).

Copying useful functions you have written into every new program is inconvenient. They should be gathered into a **library** and plugged in where needed. In this session we practise both.

:::note
**After this session you will be able to:**

- write a recursive function over an array (sum, maximum, reversal);
- bring a recursion down from exponential to linear time with memoization;
- generate all variants with backtracking;
- gather your functions into a Python module, a Java class, or a C++ header file and namespace.
:::

## Recursion over an array

To solve an array problem recursively we reduce it to **the same problem on a smaller array**:

```text
yigindi(a, n) = 0,                          if n = 0
yigindi(a, n) = yigindi(a, n - 1) + a[n-1], otherwise
```

To avoid copying the array each time, the function receives the array itself and the **bounds** (`n`, or the indices `chap` and `ong` — left and right). Reversal works the same way: swap the two outer elements and reverse the inner part recursively; `chap >= ong` is the base case.

## Memoization

The Fibonacci numbers: *F*(0) = 0, *F*(1) = 1, *F*(*n*) = *F*(*n* − 1) + *F*(*n* − 2). Direct recursion is very slow:

| *n* | Calls in plain recursion | With memoization |
|---|---|---|
| 10 | 177 | 19 |
| 30 | 2,692,537 | 59 |
| 50 | ~4 · 10¹⁰ (several minutes) | 99 |

The reason is that the same values are computed again and again: inside *F*(50), *F*(48) is computed twice, *F*(47) three times and so on. **Memoization** writes every computed result into a table and takes it from the table next time. Then every *F*(*k*) is computed only once.

| Language | The memo table |
|---|---|
| Python | a dictionary or the `@functools.lru_cache` (`@cache`) decorator |
| Java | `long[] memo = new long[n + 1]` or a `HashMap` |
| C++ | `vector<long long> memo(n + 1, -1)` |

## Backtracking

To generate all variants, the answer is built **step by step**: at each step one of the possible choices is made, the recursion continues, then the choice is **undone** and the next one is tried.

```text
qur(joriy):
    if joriy is complete: print it; return
    for each possible choice t:
        add t to joriy
        qur(joriy)
        remove t                  ← "backtracking"
```

Choices that break the condition are dropped at once — this cuts the search considerably (*qur* means build, *joriy* the current one).

## A user library

Functions needed in several programs are gathered in a separate file:

| Language | The library file | Plugging it in |
|---|---|---|
| Python | `massivlar.py` — ordinary functions | `import massivlar` → `massivlar.yigindi(a)`, or `from massivlar import yigindi` |
| Java | `Massivlar.java` — a `public final class` with `static` methods (like the `Math` class) | in the same package — directly `Massivlar.yigindi(a)`; in another package — `import` |
| C++ | `massivlar.h` — declarations (prototypes), `massivlar.cpp` — the bodies | `#include "massivlar.h"`, compile with `g++ main.cpp massivlar.cpp` |

In C++ the functions go inside a **namespace** (`namespace massivlar { ... }`) so they do not clash with same-named functions of another library. A header file starts with `#pragma once`: even if the file is included several times, it is read once.

The automatic checking system accepts a single file, so in the examples below the library sits inside that file — but it is written ready to be moved into a separate file.

## Worked example 1: Fibonacci with memoization

*n* is given (0 ≤ *n* ≤ 90). Compute *F*(*n*) with memoized recursion.

```python
from functools import cache


@cache                          # remembers the results automatically
def fib(n):
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)


n = int(input())
print(fib(n))
```
```java
import java.util.Scanner;

public class Main {
    static long[] memo;

    static long fib(int n) {
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];      // computed before
        memo[n] = fib(n - 1) + fib(n - 2);
        return memo[n];
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        memo = new long[n + 1];
        System.out.println(fib(n));
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

vector<long long> memo;

long long fib(int n) {
    if (n < 2) return n;
    if (memo[n] != -1) return memo[n];         // computed before
    return memo[n] = fib(n - 1) + fib(n - 2);
}

int main() {
    int n;
    cin >> n;
    memo.assign(n + 1, -1);
    cout << fib(n) << endl;
    return 0;
}
```

For `10` the output is `55`, for `90` it is `2880067194370816120` (this fits in a `long`; *F*(93) does not). In Java the condition `memo[n] != 0` works because *F*(*n*) is never 0 for *n* ≥ 2; in C++ `-1` serves as the "not computed" mark. Without memoization *n* = 90 could not be computed.

## Worked example 2: backtracking — strings without adjacent ones

*n* is given (1 ≤ *n* ≤ 15). Print, in lexicographic order, all strings of length *n* made of `0` and `1` in which no two `1`s stand side by side, and finally their count.

```python
def qur(joriy, n, natija):
    if len(joriy) == n:
        natija.append(joriy)
        return
    qur(joriy + "0", n, natija)
    if not joriy or joriy[-1] != "1":      # after a 1, no 1 may follow
        qur(joriy + "1", n, natija)


n = int(input())
natija = []
qur("", n, natija)
print("\n".join(natija))
print(len(natija))
```
```java
import java.util.Scanner;

public class Main {
    static StringBuilder chiqish = new StringBuilder();
    static int soni = 0;

    static void qur(char[] joriy, int i) {
        if (i == joriy.length) {
            chiqish.append(joriy).append('\n');
            soni++;
            return;
        }
        joriy[i] = '0';
        qur(joriy, i + 1);
        if (i == 0 || joriy[i - 1] != '1') {   // after a 1, no 1 may follow
            joriy[i] = '1';
            qur(joriy, i + 1);
        }
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        qur(new char[n], 0);
        System.out.print(chiqish);
        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int soni = 0;

void qur(string& joriy, int n) {
    if ((int) joriy.size() == n) {
        cout << joriy << '\n';
        soni++;
        return;
    }
    joriy.push_back('0');
    qur(joriy, n);
    joriy.pop_back();                          // undo the choice
    if (joriy.empty() || joriy.back() != '1') {  // after a 1, no 1 may follow
        joriy.push_back('1');
        qur(joriy, n);
        joriy.pop_back();
    }
}

int main() {
    int n;
    cin >> n;
    string joriy;
    qur(joriy, n);
    cout << soni << endl;
    return 0;
}
```

For the input `3` the output is:

```text
000
001
010
100
101
5
```

Since `0` is tried before `1`, the strings come out in lexicographic order by themselves. In the C++ version the "backtracking" is plain to see: `push_back` makes a choice, `pop_back` undoes it. In the Python version `joriy + "0"` creates a new string, so nothing needs undoing. A curious fact: the number of such strings grows along the Fibonacci numbers — 2, 3, 5, 8, 13, …

## Worked example 3: an arrays library

The first line holds *n*, the second *n* integers. Using the recursive functions of the `massivlar` (arrays) library, print the sum, the maximum and the array in reverse order.

```python
# ---- the contents of massivlar.py ----
def yigindi(a, n):
    """The sum of the first n elements of a."""
    if n == 0:
        return 0
    return yigindi(a, n - 1) + a[n - 1]


def maksimum(a, n):
    if n == 1:
        return a[0]
    return max(maksimum(a, n - 1), a[n - 1])


def teskari(a, chap, ong):
    if chap >= ong:
        return
    a[chap], a[ong] = a[ong], a[chap]
    teskari(a, chap + 1, ong - 1)
# ---- the main program (in a separate file: import massivlar) ----


n = int(input())
a = list(map(int, input().split()))
print(yigindi(a, n))
print(maksimum(a, n))
teskari(a, 0, n - 1)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    // in Massivlar.java this would be a public final class
    static final class Massivlar {
        private Massivlar() {}                 // no objects: static methods only

        static long yigindi(int[] a, int n) {
            if (n == 0) return 0;
            return yigindi(a, n - 1) + a[n - 1];
        }

        static int maksimum(int[] a, int n) {
            if (n == 1) return a[0];
            return Math.max(maksimum(a, n - 1), a[n - 1]);
        }

        static void teskari(int[] a, int chap, int ong) {
            if (chap >= ong) return;
            int t = a[chap];
            a[chap] = a[ong];
            a[ong] = t;
            teskari(a, chap + 1, ong - 1);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        System.out.println(Massivlar.yigindi(a, n));
        System.out.println(Massivlar.maksimum(a, n));
        Massivlar.teskari(a, 0, n - 1);
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
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

// ---- the contents of massivlar.h and massivlar.cpp ----
namespace massivlar {
    long long yigindi(const vector<int>& a, int n) {
        if (n == 0) return 0;
        return yigindi(a, n - 1) + a[n - 1];
    }

    int maksimum(const vector<int>& a, int n) {
        if (n == 1) return a[0];
        return max(maksimum(a, n - 1), a[n - 1]);
    }

    void teskari(vector<int>& a, int chap, int ong) {
        if (chap >= ong) return;
        swap(a[chap], a[ong]);
        teskari(a, chap + 1, ong - 1);
    }
}
// ---- the main program ----

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    cout << massivlar::yigindi(a, n) << endl;
    cout << massivlar::maksimum(a, n) << endl;
    massivlar::teskari(a, 0, n - 1);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

For the input `5` and `3 8 -1 6 2` the output is `18`, `8` and `2 6 -1 8 3`. The library functions neither read nor print anything — that is why they can be reused in any program (*yigindi* is the sum, *teskari* reverse). In C++ `massivlar::yigindi` is access through the namespace; in Java `Massivlar.yigindi` is access through the class name, just like `Math.max`. Every recursive call takes space on the stack, so such functions are safe up to a few thousand elements; for very large arrays a loop is better.

:::warning
**Common mistakes**

- **Forgetting the base case or writing it wrongly.** The recursion never stops and the stack overflows (`RecursionError`, `StackOverflowError`).
- **Creating the memo table inside the function.** Every call gets a new table and nothing is remembered; the table must live outside the function.
- **Not undoing a choice in backtracking.** The shared state (say, `joriy` in C++) passes "dirty" into the next branch.
- **Putting test code into a file meant for `import` in Python.** That code runs too when the module is imported; put it under `if __name__ == "__main__":`.
:::

:::tip
When testing a recursive function, start from the smallest cases: *n* = 0, 1, 2. If they work and the recursive step is right, the function works for every *n* — that is mathematical induction itself.
:::

## Summary

- An array problem is reduced to the same smaller problem by narrowing the bounds.
- Memoization does not repeat the same computations: Fibonacci drops from exponential to linear time.
- Backtracking: choice → recursion → undo the choice; branches that do not fit are cut at once.
- A library: Python — a module, Java — a class with `static` methods, C++ — a header file, a `.cpp` file and a namespace.
- Library functions do not read or print — they only compute or change data.

## Check yourself

:::check
Q: Why is plain recursive Fibonacci slow?
A: The same values (for example, *F*(*n* − 2)) are recomputed many times; the number of calls grows exponentially.
Q: What is memoization?
A: Keeping computed results in a table and taking them from the table next time instead of recomputing.
Q: What does "backtracking" mean in the backtracking method?
A: Undoing the choice made before the recursive call and trying the next variant.
Q: Why is a namespace needed in C++?
A: So that same-named functions from different libraries do not clash: `massivlar::yigindi` and `matritsa::yigindi` are told apart.
Q: How do you keep the test code of a Python module from running on import?
A: Put it inside an `if __name__ == "__main__":` block.
:::

:::link https://docs.python.org/3/tutorial/modules.html
Python: modules
The official tutorial on creating your own module and importing it.
:::

:::link https://docs.python.org/3/library/functools.html#functools.cache
Python: functools.cache
A decorator that remembers a function's results automatically.
:::
