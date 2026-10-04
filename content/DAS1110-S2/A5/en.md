---
title: Working with dynamic memory. Pointers. Dynamic one- and multidimensional arrays
summary: Pointer arithmetic, a dynamic one-dimensional array, a dynamic matrix with new/delete, an array with rows of different lengths (a "jagged" array), keeping a matrix in one-dimensional memory; the Java and Python counterparts.
---

## Why it matters

In the first semester we met the stack and the heap, pointers, `new` and `delete`. The weakness of a static array is that its size is fixed at compile time: `int a[100][100]` always takes 40 KB, yet is not enough for a 1000 × 1000 matrix. Dynamic memory solves this — the array is created **while the program runs**, at the size needed.

In this practical session we practise three shapes of dynamic array: a rectangular matrix, an array whose rows have different lengths, and a matrix kept in one contiguous block of memory.

:::note
**After this session you will be able to:**

- explain pointer arithmetic (`p + i`, `*(p + i)`, `p++`);
- create a dynamic matrix with `new` in C++ and free it correctly with `delete[]`;
- create an array whose rows have different lengths (a "jagged" array);
- keep a matrix in a one-dimensional array with the index `i * m + j`;
- build the same structures in Java and Python.
:::

## Pointer arithmetic

In C++ an array's name works like a pointer to its first element. Adding an integer to a pointer moves it forward **by that many elements** (not bytes):

| Expression | Meaning |
|---|---|
| `p` | the address of the first element |
| `p + i` | the address of element *i* |
| `*(p + i)` | the value of element *i* — the same as `p[i]` |
| `p++` | move to the next element |
| `q - p` | the number of elements between two pointers |

For `p + i` the compiler computes `i * sizeof(int)` bytes automatically. Java and Python have no pointer arithmetic — only indices are used there, and going out of bounds is an error at once.

## A dynamic matrix

In C++ an *n* × *m* dynamic matrix is created in two stages: first an array of *n* **row pointers**, then *m* elements for each row:

```text
int** a = new int*[n];             // the row pointers
for each i: a[i] = new int[m];     // each row
...
for each i: delete[] a[i];         // freeing — in reverse order
delete[] a;
```

Every `new[]` must be matched by exactly one `delete[]`, otherwise **memory leaks**. That is why in practice `vector<vector<int>>` is mostly used — it frees memory by itself. But understanding `new`/`delete` matters: exactly this process hides inside `vector`.

| Language | A dynamic *n* × *m* matrix | Freeing |
|---|---|---|
| C++ | `new int*[n]` + `new int[m]` for each row | `delete[]` — each row and the pointer array |
| C++ (recommended) | `vector<vector<int>> a(n, vector<int>(m))` | automatic |
| Java | `int[][] a = new int[n][m]` | the garbage collector |
| Python | `[[0] * m for _ in range(n)]` | the garbage collector |

## A jagged array

Since the rows are created separately, they can have **different** lengths — such an array is called "jagged". For example, row *i* of Pascal's triangle has *i* + 1 elements; with a rectangular matrix almost half of it would sit unused.

| Language | A jagged array |
|---|---|
| C++ | `a[i] = new int[i + 1];` or `vector<vector<int>>` |
| Java | `int[][] a = new int[n][];` and `a[i] = new int[i + 1];` |
| Python | a list of lists — every inner list of any length |

## Keeping a matrix in a one-dimensional array

A matrix can also be kept in a single contiguous array: the element in row *i*, column *j* sits at position `i * m + j` (rows are laid out one after another). The advantages:

- one `new` and one `delete[]` — less room for mistakes;
- the elements are adjacent in memory — faster thanks to the processor cache;
- the whole matrix can be passed to a function like an ordinary array.

Graphics, image-processing and scientific-computing libraries use exactly this method.

## Worked example 1: a spiral matrix

*n* and *m* are given. Fill an *n* × *m* dynamic matrix with the numbers 1 to *n* · *m* in a clockwise spiral and print it (each number 3 characters wide).

```python
n, m = map(int, input().split())
a = [[0] * m for _ in range(n)]

yuqori, past, chap, ong = 0, n - 1, 0, m - 1
k = 1
while yuqori <= past and chap <= ong:
    for j in range(chap, ong + 1):              # top row: left to right
        a[yuqori][j] = k; k += 1
    yuqori += 1
    for i in range(yuqori, past + 1):           # right column: top to bottom
        a[i][ong] = k; k += 1
    ong -= 1
    if yuqori <= past:
        for j in range(ong, chap - 1, -1):      # bottom row: right to left
            a[past][j] = k; k += 1
        past -= 1
    if chap <= ong:
        for i in range(past, yuqori - 1, -1):   # left column: bottom to top
            a[i][chap] = k; k += 1
        chap += 1

for satr in a:
    print("".join(f"{x:3d}" for x in satr))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];

        int yuqori = 0, past = n - 1, chap = 0, ong = m - 1, k = 1;
        while (yuqori <= past && chap <= ong) {
            for (int j = chap; j <= ong; j++) a[yuqori][j] = k++;      // top row: left to right
            yuqori++;
            for (int i = yuqori; i <= past; i++) a[i][ong] = k++;      // right column: top to bottom
            ong--;
            if (yuqori <= past) {
                for (int j = ong; j >= chap; j--) a[past][j] = k++;    // bottom row: right to left
                past--;
            }
            if (chap <= ong) {
                for (int i = past; i >= yuqori; i--) a[i][chap] = k++; // left column: bottom to top
                chap++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int[] satr : a) {
            for (int x : satr) sb.append(String.format("%3d", x));
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    int** a = new int*[n];                      // the row pointers
    for (int i = 0; i < n; i++) a[i] = new int[m];

    int yuqori = 0, past = n - 1, chap = 0, ong = m - 1, k = 1;
    while (yuqori <= past && chap <= ong) {
        for (int j = chap; j <= ong; j++) a[yuqori][j] = k++;      // top row: left to right
        yuqori++;
        for (int i = yuqori; i <= past; i++) a[i][ong] = k++;      // right column: top to bottom
        ong--;
        if (yuqori <= past) {
            for (int j = ong; j >= chap; j--) a[past][j] = k++;    // bottom row: right to left
            past--;
        }
        if (chap <= ong) {
            for (int i = past; i >= yuqori; i--) a[i][chap] = k++; // left column: bottom to top
            chap++;
        }
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) printf("%3d", a[i][j]);
        printf("\n");
    }

    for (int i = 0; i < n; i++) delete[] a[i];  // free every row
    delete[] a;                                 // then the pointer array
    return 0;
}
```

For the input `3 4` the output is:

```text
  1  2  3  4
 10 11 12  5
  9  8  7  6
```

The four bounds (`yuqori`, `past`, `chap`, `ong` — top, bottom, left, right) move inwards after every lap. The two inner `if`s are needed so that in a rectangular (not square) matrix a single remaining row or column is not filled twice. At the end of the C++ version memory is freed **in reverse order**: first the rows, then the pointer array — otherwise the rows' addresses would be lost.

## Worked example 2: Pascal's triangle (a jagged array)

*n* is given (1 ≤ *n* ≤ 30). Print the first *n* rows of Pascal's triangle: each row has 1 at its ends, and an inner element is the sum of the two neighbouring elements above it.

```python
n = int(input())
uchburchak = []
for i in range(n):
    satr = [1] * (i + 1)                       # row i has i+1 elements
    for j in range(1, i):
        satr[j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j]
    uchburchak.append(satr)

for satr in uchburchak:
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        long[][] uchburchak = new long[n][];            // rows not created yet
        for (int i = 0; i < n; i++) {
            uchburchak[i] = new long[i + 1];            // row i has i+1 elements
            uchburchak[i][0] = uchburchak[i][i] = 1;
            for (int j = 1; j < i; j++) {
                uchburchak[i][j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j];
            }
        }

        StringBuilder sb = new StringBuilder();
        for (long[] satr : uchburchak) {
            for (int j = 0; j < satr.length; j++) {
                if (j > 0) sb.append(' ');
                sb.append(satr[j]);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    long long** uchburchak = new long long*[n];
    for (int i = 0; i < n; i++) {
        uchburchak[i] = new long long[i + 1];       // row i has i+1 elements
        uchburchak[i][0] = uchburchak[i][i] = 1;
        for (int j = 1; j < i; j++) {
            uchburchak[i][j] = uchburchak[i - 1][j - 1] + uchburchak[i - 1][j];
        }
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j <= i; j++) {
            if (j > 0) cout << ' ';
            cout << uchburchak[i][j];
        }
        cout << endl;
    }

    for (int i = 0; i < n; i++) delete[] uchburchak[i];
    delete[] uchburchak;
    return 0;
}
```

For the input `5` the output is:

```text
1
1 1
1 2 1
1 3 3 1
1 4 6 4 1
```

Every row was created at exactly the length needed: for *n* = 30 that is 465 elements, while a rectangular 30 × 30 matrix would take 900 places. In Java `new long[n][]` leaves the second dimension out, and the rows are created separately later. The edge assignment `[i][0] = [i][i] = 1` writes one element twice in the first row (*i* = 0) — that is not a mistake. The values grow fast (up to 77,558,760 in row 30, and in row 35 they exceed the `int` limit), which is why `long` was chosen (*uchburchak* means triangle).

## Worked example 3: rotating a matrix kept in a one-dimensional array

The first line holds *n* and *m*, then an *n* × *m* matrix. Keep the matrix in one one-dimensional array, rotate it 90° clockwise into a result (*m* × *n*) that is also a one-dimensional array, and print it.

```python
n, m = map(int, input().split())
a = []
for _ in range(n):
    a.extend(map(int, input().split()))         # rows one after another: a[i*m + j]

b = [0] * (n * m)                               # the result: m rows, n columns
for i in range(n):
    for j in range(m):
        b[j * n + (n - 1 - i)] = a[i * m + j]

for i in range(m):
    print(*b[i * n:(i + 1) * n])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[] a = new int[n * m];                       // rows one after another: a[i*m + j]
        for (int k = 0; k < n * m; k++) a[k] = in.nextInt();

        int[] b = new int[n * m];                       // the result: m rows, n columns
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                b[j * n + (n - 1 - i)] = a[i * m + j];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(b[i * n + j]);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    int* a = new int[n * m];                    // one contiguous block
    for (int* p = a; p < a + n * m; p++) cin >> *p;   // reading with a pointer

    int* b = new int[n * m];                    // the result: m rows, n columns
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            *(b + j * n + (n - 1 - i)) = *(a + i * m + j);

    for (int i = 0; i < m; i++) {
        const int* satr = b + i * n;            // the start of row i
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << satr[j];
        }
        cout << endl;
    }

    delete[] a;                                 // one new — one delete
    delete[] b;
    return 0;
}
```

Input:

```text
2 3
1 2 3
4 5 6
```

Output:

```text
4 1
5 2
6 3
```

In a rotation row *i* becomes column (*n* − 1 − *i*) of the result: the first row `1 2 3` landed in the last column. The C++ version shows pointer arithmetic in full: `for (int* p = a; p < a + n * m; p++)` walks the array without indices, and `b + i * n` gives the start of row *i*, which is then used like an ordinary array, `satr[j]`. One `new` and one `delete[]` per matrix are enough.

:::warning
**Common mistakes**

- **Mixing up `delete` and `delete[]`.** An array created with `new[]` is freed only with `delete[]`.
- **Deleting the pointer array first.** Accessing `a[i]` after `delete[] a;` is undefined behaviour; free the rows first.
- **Writing `i * n + j` in one-dimensional storage.** A row's length is the number of columns *m*; it must be `i * m + j` (when *n* = *m* the mistake does not show at once).
- **Running past the array's end with a pointer.** `p <= a + n` is one element too many; the condition is `p < a + n`.
:::

:::tip
When working with `new`/`delete` in C++, write the matching `delete` as soon as you write each `new`, then fill in the code between them. In real projects use `vector` and `unique_ptr` — they free memory by themselves.
:::

## Summary

- Adding *i* to a pointer moves it *i* elements forward; `*(p + i)` is `p[i]` itself.
- A dynamic matrix in C++: an array of pointers + each row; freeing goes in reverse order.
- If rows are created separately, they can have different lengths — a jagged array saves memory.
- Keeping a matrix in a one-dimensional array at position `i * m + j` — one allocation, faster access.
- In Java and Python memory is freed automatically; in C++ `vector` is preferred in practice.

## Check yourself

:::check
Q: If `int* p = a;`, what is `*(p + 3)` equal to?
A: To `a[3]` — the fourth element of the array.
Q: How is a matrix created in C++ with `new int*[n]` and `new int[m]` for each row freed?
A: First `delete[] a[i]` for every `a[i]`, then `delete[] a`.
Q: What does `new int[5][]` create in Java?
A: An array of 5 row references (`null`); every row is created separately later with `new int[...]`.
Q: If a 4 × 6 matrix is kept in a one-dimensional array, at which index is the element `[2][3]`?
A: At index 2 · 6 + 3 = 15.
Q: What is the advantage of a jagged array?
A: Every row has exactly the length needed, and no memory is wasted.
:::

:::link https://en.cppreference.com/w/cpp/language/new
C++: the new expression
About allocating dynamic memory and `new[]` for arrays.
:::

:::link https://en.cppreference.com/w/cpp/language/operator_arithmetic#Additive_operators
C++: pointer arithmetic
The rules for adding a number to a pointer and subtracting pointers.
:::
