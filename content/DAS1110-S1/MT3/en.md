---
title: Organising arrays
summary: Independent work: a program working with an array — input, index arithmetic, output and tests; the sample problem is a cyclic shift of an array by k places.
---

## Why it matters

A queue system, a music playlist, players sitting in a circle — such data is kept in an array, and often you have to work with the **positions** of the elements: reversing, shifting, rotating. All of these operations rest on finding the right relation between indices. In this independent work you find such a relation yourself and apply it in a program.

:::note
**After this independent work you will be able to:**

- fill an array from the keyboard and print it on one line;
- find the formula linking an old index and a new one;
- use "circular" indices with the `%` operator;
- build tests for the boundary cases (1 element, *k* = 0, *k* ≥ *n*).
:::

## A quick review of the topic

| Action | Python | Java | C++ |
|---|---|---|---|
| Read *n* numbers | `a = list(map(int, input().split()))` | `nextInt()` in a loop | `cin >>` in a loop |
| Length | `len(a)` | `a.length` | `a.size()` |
| Last element | `a[n - 1]` or `a[-1]` | `a[n - 1]` | `a[n - 1]` |
| Print on one line | `print(*a)` | `StringBuilder` and spaces | a loop and spaces |

The key question in a problem that moves elements around: **where must the element at position *i* go?** The answer is written as a formula. For example, when reversing, element *i* goes to position *n* − 1 − *i*.

## Sample problem: a cyclic shift

The first line has *n*, the second *n* numbers, the third *k* (0 ≤ *k* ≤ 10⁹). Shift the array **cyclically** right by *k* places: an element that leaves the end comes back at the start.

Analysis: shifting right by one place moves element *i* to position *i* + 1, and the last one to position 0. Shifting by *k* moves it to position (*i* + *k*), but "around the circle": **(*i* + *k*) mod *n***. The formula works even for a very large *k*: a shift by *n* places brings the array back to where it started.

```text
1. read n, a, k
2. b — a new array of n elements
3. for each i = 0 … n-1: b[(i + k) mod n] = a[i]
4. print b
```

```python
n = int(input())
a = list(map(int, input().split()))
k = int(input())

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
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        long k = in.nextLong();

        int[] b = new int[n];
        for (int i = 0; i < n; i++) {
            b[(int) ((i + k) % n)] = a[i];
        }

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
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    long long k;
    cin >> k;

    vector<int> b(n);
    for (int i = 0; i < n; i++) {
        b[(i + k) % n] = a[i];
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;
    return 0;
}
```

For the input `5`, `1 2 3 4 5`, `2`:

| *i* | `a[i]` | (*i* + 2) mod 5 |
|---|---|---|
| 0 | 1 | 2 |
| 1 | 2 | 3 |
| 2 | 3 | 4 |
| 3 | 4 | 0 |
| 4 | 5 | 1 |

The output is `4 5 1 2 3`. For *k* = 7 the result is the same (7 mod 5 = 2); for *k* = 5 the array does not change. *k* is read as a `long` because 10⁹ + *i* is close to the limit of an `int`.

## The independent work task

Solve the **print the array in reverse order** problem given below. What you hand in:

1. **A formula** — which position element *i* goes to when the array is reversed; show it with a table for an example of 5 elements (like the one above).
2. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
3. **Tests** — at least 4: 1 element, an even length, an odd length, negative numbers; the expected result of each.
4. **A comparison of two methods** — writing into a new array and swapping in place (two pointers): how much extra memory does each need?

:::warning
**Assessment criteria and common mistakes**

- Indices go from 0 to *n* − 1; accessing `a[n]` is out of bounds.
- The output format must be as in the statement (elements separated by one space, on one line).
- When swapping in place the loop must run *n*/2 times, not *n*.
- The formula and the table are worked out by hand.
:::

:::tip
To find the formula, write a 5-element array on paper and draw an arrow from each element to where it goes. The relation between the indices shows itself.
:::

## Summary

- In a problem that moves elements, find the formula "where does element *i* go".
- In a cyclic shift the new position is (*i* + *k*) mod *n*; large values of *k* work correctly too.
- When reversing, element *i* goes to position *n* − 1 − *i*.
- The tests must include 1 element and both an even and an odd length.

## Check yourself

:::check
Q: Where does element 0 of a 6-element array go after a right shift by 8?
A: To position 2: (0 + 8) mod 6 = 2.
Q: When a 7-element array is reversed, where does the element at index 2 go?
A: To position 4: 7 − 1 − 2 = 4.
Q: What is the formula for a left shift by *k* places?
A: The new position is (*i* − *k*) mod *n*; to avoid a negative value, (*i* − *k* mod *n* + *n*) mod *n*.
:::

:::link https://visualgo.net/en/list
VisuAlgo: array operations
Visualising operations on the elements of an array.
:::
