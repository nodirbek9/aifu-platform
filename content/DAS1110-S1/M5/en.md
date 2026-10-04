---
title: One-dimensional arrays
summary: What an array is, index and length, declaring and filling an array, traversing the elements, the sum and the largest element, reversing, going out of bounds.
---

## Why it matters

A group has 25 students. To find the average score it is enough to keep adding the scores to one accumulator. But to answer "how many students scored above the average?" you first need the average and then have to look at every score **again**. 25 separate variables (`ball1`, `ball2`, …, `ball25`) are clumsy, and if the group has 30 people the program no longer works.

The answer is an **array**: a sequence of values of one type kept under one name. A register, a price list, daily temperatures, the pixels of an image — all of them are arrays. From this lesson on, programs start working with large amounts of data.

:::note
**After this lesson you will be able to:**

- explain the ideas of an array, an element, an index and a length;
- create an array in Python, Java and C++, fill it from the keyboard and print it;
- traverse an array with a loop and find the sum, the average, the largest element and its position;
- reverse an array in place and avoid going out of bounds.
:::

## The idea of an array

An **array** is an ordered, named collection of elements of the same type. Each element is reached by its **index** (its position number).

| Idea | Meaning | Example (`a = [7, 3, 9, 4]`) |
|---|---|---|
| Element | one value in the array | 9 |
| Index | the position of an element, starting **from 0** | the index of 9 is 2 |
| Length (size) | the number of elements | 4 |
| First element | `a[0]` | 7 |
| Last element | `a[n - 1]` | 4 |

The index starts at 0 because an index is an **offset** from the start of the array. The first element is 0 steps from the start.

In memory the elements of an array lie **one after another**. That makes accessing `a[i]` very fast: the element's address = the start address + *i* · the element size. Any element is reached equally fast.

## Creating an array

| Task | Python (`list`) | Java | C++ (`vector`) |
|---|---|---|---|
| With values | `a = [7, 3, 9, 4]` | `int[] a = {7, 3, 9, 4};` | `vector<int> a = {7, 3, 9, 4};` |
| *n* zeros | `a = [0] * n` | `int[] a = new int[n];` | `vector<int> a(n);` |
| Length | `len(a)` | `a.length` | `a.size()` |
| Element | `a[i]` | `a[i]` | `a[i]` |

A Python `list` can change size: `a.append(x)` adds an element at the end. The length of a Java array is fixed when it is created. In C++ a `vector` (`#include <vector>`) is a resizable array; the classic C++ array `int a[100];` is a static array whose size is known at compile time (covered in session A9).

## Filling and traversing an array

Input usually comes like this: *n* on the first line, *n* numbers on the second.

- **Python**: `n = int(input())`, `a = list(map(int, input().split()))`.
- **Java, C++**: the array is created and *n* numbers are read in a loop.

**Traversing** an array means visiting each element in turn:

| Language | With an index | Values only |
|---|---|---|
| Python | `for i in range(len(a)):` | `for x in a:` |
| Java | `for (int i = 0; i < a.length; i++)` | `for (int x : a)` |
| C++ | `for (int i = 0; i < (int) a.size(); i++)` | `for (int x : a)` |

When the index is not needed (a sum, a count), the short form is handy. When it is (finding a position, neighbouring elements, changing elements), use the indexed loop.

## Going out of bounds

The index runs from 0 to *n* − 1. `a[n]` is an element the array **does not have**:

| Language | When `a[n]` is accessed |
|---|---|
| Python | `IndexError: list index out of range` |
| Java | `ArrayIndexOutOfBoundsException` |
| C++ | **no** error message: someone else's memory is read or damaged — "undefined behaviour" |

This property of C++ makes for one of the most dangerous bugs: the program sometimes works and sometimes crashes. A large share of security vulnerabilities come exactly from out-of-bounds access. So always write the loop condition as `i < n`, not `i <= n`.

Python also has negative indices: `a[-1]` is the last element, `a[-2]` the second from the end. Java and C++ have nothing like it.

## Worked example 1: statistics

The first line has *n* (1 ≤ *n* ≤ 1000), the second *n* integers: students' scores. Print the sum, the average, the highest score and its position (counting from 1), and how many scores are above the average.

```python
n = int(input())
a = list(map(int, input().split()))

yigindi = 0
for x in a:
    yigindi += x
ortacha = yigindi / n

mx_i = 0                          # the index of the largest
for i in range(1, n):
    if a[i] > a[mx_i]:
        mx_i = i

yuqori = 0
for x in a:
    if x > ortacha:
        yuqori += 1

print("Sum:", yigindi)
print(f"Average: {ortacha:.2f}")
print("Highest:", a[mx_i], "position:", mx_i + 1)
print("Above average:", yuqori)
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

        int yigindi = 0;
        for (int x : a) {
            yigindi += x;
        }
        double ortacha = (double) yigindi / n;

        int mxI = 0;                      // the index of the largest
        for (int i = 1; i < n; i++) {
            if (a[i] > a[mxI]) {
                mxI = i;
            }
        }

        int yuqori = 0;
        for (int x : a) {
            if (x > ortacha) {
                yuqori++;
            }
        }

        System.out.println("Sum: " + yigindi);
        System.out.println(String.format("Average: %.2f", ortacha));
        System.out.println("Highest: " + a[mxI] + " position: " + (mxI + 1));
        System.out.println("Above average: " + yuqori);
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    int yigindi = 0;
    for (int x : a) {
        yigindi += x;
    }
    double ortacha = (double) yigindi / n;

    int mxI = 0;                      // the index of the largest
    for (int i = 1; i < n; i++) {
        if (a[i] > a[mxI]) {
            mxI = i;
        }
    }

    int yuqori = 0;
    for (int x : a) {
        if (x > ortacha) {
            yuqori++;
        }
    }

    cout << "Sum: " << yigindi << endl;
    cout << fixed << setprecision(2) << "Average: " << ortacha << endl;
    cout << "Highest: " << a[mxI] << " position: " << mxI + 1 << endl;
    cout << "Above average: " << yuqori << endl;
    return 0;
}
```

Input: `5` and `78 92 65 88 70`.

1. The sum: 78 + 92 + 65 + 88 + 70 = 393; the average 393 / 5 = 78.60.
2. The largest: `mx_i` starts at 0; at *i* = 1, 92 > 78 — `mx_i = 1`; nothing after is larger than 92. The highest is 92, at position 2.
3. Above the average (78.60): 92 and 88 — 2.

The output:

```text
Sum: 393
Average: 78.60
Highest: 92 position: 2
Above average: 2
```

The third step shows why the array is needed: the average is known only after all the scores have been read, and then the scores have to be looked at **a second time**. It is the **index** of the largest that is kept, not its value — that way we know both the value (`a[mx_i]`) and its position.

## Worked example 2: reversing an array

The first line has *n*, the second *n* numbers. Reverse the array **in place** (without creating a new array) and print it.

The **two-pointer** method: the left pointer starts at the beginning, the right pointer at the end; until they meet, their elements are swapped.

```python
n = int(input())
a = list(map(int, input().split()))

chap, ong = 0, n - 1
while chap < ong:
    a[chap], a[ong] = a[ong], a[chap]
    chap += 1
    ong -= 1

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

        int chap = 0, ong = n - 1;
        while (chap < ong) {
            int t = a[chap];
            a[chap] = a[ong];
            a[ong] = t;
            chap++;
            ong--;
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
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    int chap = 0, ong = n - 1;
    while (chap < ong) {
        int t = a[chap];
        a[chap] = a[ong];
        a[ong] = t;
        chap++;
        ong--;
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

For the input `5` and `1 2 3 4 5` (*chap* is left, *ong* is right):

| *chap* | *ong* | swap | array |
|---|---|---|---|
| 0 | 4 | 1 ↔ 5 | 5 2 3 4 1 |
| 1 | 3 | 2 ↔ 4 | 5 4 3 2 1 |
| 2 | 2 | the loop ends | 5 4 3 2 1 |

The output is `5 4 3 2 1`. With an even length (say 4) the pointers stop when they pass each other. The loop runs *n*/2 times. In Python `print(*a)` prints the elements of a list separated by spaces; there are short ways too — `a.reverse()` or `a[::-1]`.

:::warning
**Common mistakes**

- **Indices from 1.** The first element is `a[0]`; `for i in range(1, n + 1)` goes out of bounds at the end.
- **The condition `i <= n`.** On the last iteration `a[n]` is an error in Python and Java and silent memory damage in C++.
- **Starting the largest at 0.** If all the numbers are negative, the answer comes out as a wrong 0. Start from the first element.
- **Swapping all the way when reversing.** With *n* swaps the array is reversed twice and returns to where it started.
- **`a.length()` in Java.** For an array it is `length` without brackets; `length()` is for strings.
:::

:::tip
Check a program that works with an array in three cases: one element, two elements (even) and three elements (odd). Almost all boundary bugs show up in these small cases.
:::

## Summary

- An array is a named, ordered collection of elements of one type; the elements lie one after another in memory.
- Indices go from 0 to *n* − 1; accessing `a[i]` is equally fast for any *i*.
- Python has `list`, Java `int[]` (fixed length), C++ `vector<int>`; the length is `len(a)`, `a.length`, `a.size()`.
- Traversal: an indexed loop or a "for each element" loop.
- Keep the index of the largest element; start from the first element.
- Going out of bounds is an error in Python and Java, and silently damages memory in C++.

## Check yourself

:::check
Q: For `a = [4, 8, 15, 16, 23, 42]`, what are `a[2]`, `a[5]` and `len(a)`?
A: 15, 42 and 6.
Q: Why does an array index start at 0?
A: The index is an offset from the start of the array: the first element is 0 elements away from the start.
Q: In Java, what does `a[5] = 1;` do after `int[] a = new int[5];`?
A: `ArrayIndexOutOfBoundsException`: the indices are 0 to 4.
Q: Why should the search for the largest start from the first element rather than `mx = 0`?
A: If all the elements are negative, 0 is not smaller than any of them and the answer comes out as a wrong 0.
Q: How many swaps does reversing an array of 7 elements take?
A: 3 (7 // 2): the middle element stays in place.
:::

:::link https://docs.python.org/3/tutorial/datastructures.html#more-on-lists
Python: lists
List methods: `append`, `reverse`, `sort` and others.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
Oracle Java Tutorial: arrays
Declaring, creating and using arrays in Java.
:::

:::link https://visualgo.net/en/list
VisuAlgo: arrays and lists
Visualising array operations.
:::
