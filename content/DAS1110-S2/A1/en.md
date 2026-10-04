---
title: Multidimensional arrays. Static arrays. Sorting elements
summary: Declaring and reading a two-dimensional array (matrix), traversing by rows and by columns, transposing, the limits of a static array, sorting within and among the rows and columns of a matrix.
---

## Why it matters

A timetable is days by periods, an exam record is students by subjects, an image is rows and columns of pixels. Such data naturally comes as a **table** and is kept in a program as a **two-dimensional array** (a matrix).

In the first semester we learned one-dimensional arrays, and in the last lecture we saw sorting methods. In this practical session we combine the two: we read a matrix, compute over its rows and columns, and sort its rows.

:::note
**After this session you will be able to:**

- declare a two-dimensional array in three languages and read it from the keyboard;
- traverse by rows and by columns and compute values such as a sum or a maximum;
- transpose a matrix;
- explain the difference between a static array and an array sized at run time;
- sort the rows of a matrix by some criterion.
:::

## A two-dimensional array

A two-dimensional array is an **array of arrays**: every element is itself an array (a row). `a[i][j]` is the element in row *i*, column *j*. Both indices start at 0.

| Language | Declaration (*n* × *m*, filled with zeros) | Element |
|---|---|---|
| Python | `a = [[0] * m for _ in range(n)]` | `a[i][j]` |
| Java | `int[][] a = new int[n][m];` | `a[i][j]` |
| C++ (static) | `int a[100][100];` | `a[i][j]` |
| C++ (size at run time) | `vector<vector<int>> a(n, vector<int>(m));` | `a[i][j]` |

In Python you cannot write `[[0] * m] * n`: it creates *n* references to one row, so changing one element changes the whole column.

## A static array

A **static array** is an array whose size is known at compile time. In C++ `int a[100][100];` reserves memory for 100 · 100 `int` values, even if the problem needs only a 3 × 4 matrix. Therefore:

- the size must be no smaller than the largest bound in the problem (`n, m ≤ 100` means `[100][100]`);
- the actual sizes are kept in separate variables `n` and `m`;
- a large static array is better declared **globally** rather than inside a function — a function's stack is small (usually 1–8 MB).

In Java and Python an array is created at run time (`new int[n][m]`), so its size can come from the `n` and `m` that were read.

## Traversing by rows and by columns

Two nested loops: the outer loop over rows and the inner one over columns is **traversal by rows**. If the outer loop is over columns and the inner one over rows, it is **traversal by columns**.

```text
by rows:     for i = 0..n-1: for j = 0..m-1: a[i][j]
by columns:  for j = 0..m-1: for i = 0..n-1: a[i][j]
```

For a row sum the sum is reset to zero at the start of every row; for a column sum, at the start of every column. **Transposing** turns rows into columns: `t[j][i] = a[i][j]`, and the result is *m* × *n*.

## Sorting in a matrix

Sorting problems with a matrix come in different kinds:

| Problem | What is sorted | How |
|---|---|---|
| Sort every row | the elements within a row | sort each `a[i]` as a separate array |
| Sort the rows | whole rows | swap the rows by a key (for example, the row sum) |
| Sort a column | the elements of a column | copy the column into a separate array, sort it, write it back |

When whole rows are sorted, the order inside a row does not change — only the rows change places.

## Worked example 1: row sums and column maximums

The first line holds *n* and *m*, then *n* lines of *m* integers. Print the sum of every row, then the largest element of every column.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

for i in range(n):
    print(sum(a[i]))

ustun_max = []
for j in range(m):
    eng_katta = a[0][j]
    for i in range(1, n):
        if a[i][j] > eng_katta:
            eng_katta = a[i][j]
    ustun_max.append(eng_katta)
print(*ustun_max)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        for (int i = 0; i < n; i++) {
            long yigindi = 0;
            for (int j = 0; j < m; j++) yigindi += a[i][j];
            System.out.println(yigindi);
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < m; j++) {
            int engKatta = a[0][j];
            for (int i = 1; i < n; i++) {
                if (a[i][j] > engKatta) engKatta = a[i][j];
            }
            if (j > 0) sb.append(' ');
            sb.append(engKatta);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int a[100][100];   // a static array: sized for the largest case

int main() {
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            cin >> a[i][j];

    for (int i = 0; i < n; i++) {
        long long yigindi = 0;
        for (int j = 0; j < m; j++) yigindi += a[i][j];
        cout << yigindi << endl;
    }

    for (int j = 0; j < m; j++) {
        int engKatta = a[0][j];
        for (int i = 1; i < n; i++) {
            if (a[i][j] > engKatta) engKatta = a[i][j];
        }
        if (j > 0) cout << ' ';
        cout << engKatta;
    }
    cout << endl;
    return 0;
}
```

Input:

```text
3 4
5 1 8 2
7 3 0 4
1 9 6 2
```

Output:

```text
16
14
18
7 9 8 4
```

For the column maximum the outer loop runs over `j` — that is traversal by columns. The starting value is `a[0][j]`, not `0`: otherwise a column of negative numbers gives a wrong answer (*yigindi* is the sum, *eng_katta* the largest).

## Worked example 2: transposing

The first line holds *n* and *m*, then an *n* × *m* matrix. Print the transposed matrix (*m* × *n*).

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

t = [[0] * n for _ in range(m)]
for i in range(n):
    for j in range(m):
        t[j][i] = a[i][j]

for satr in t:
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        int[][] t = new int[m][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                t[j][i] = a[i][j];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(t[i][j]);
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

int a[100][100], t[100][100];

int main() {
    int n, m;
    cin >> n >> m;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            cin >> a[i][j];

    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            t[j][i] = a[i][j];

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << t[i][j];
        }
        cout << endl;
    }
    return 0;
}
```

For the input `2 3` and `1 2 3` / `4 5 6` the output is `1 4`, `2 5`, `3 6`. The result array `t` is *m* × *n* — the numbers of rows and columns have swapped (*satr* is a row).

## Worked example 3: sorting the rows by their sum

The first line holds *n* and *m*, then an *n* × *m* matrix. Arrange the rows by their sum in ascending order; rows with equal sums stay in input order. The elements inside a row do not change.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

a.sort(key=sum)          # stable: equal sums keep their order

for satr in a:
    print(*satr)
```
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    static long yigindi(int[] satr) {
        long s = 0;
        for (int x : satr) s += x;
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = in.nextInt();

        Arrays.sort(a, Comparator.comparingLong(Main::yigindi));   // stable for objects

        StringBuilder sb = new StringBuilder();
        for (int[] satr : a) {
            for (int j = 0; j < m; j++) {
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
#include <algorithm>
#include <iostream>
#include <numeric>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> a(n, vector<int>(m));
    for (auto& satr : a)
        for (auto& x : satr) cin >> x;

    stable_sort(a.begin(), a.end(), [](const vector<int>& x, const vector<int>& y) {
        return accumulate(x.begin(), x.end(), 0LL) < accumulate(y.begin(), y.end(), 0LL);
    });

    for (const auto& satr : a) {
        for (int j = 0; j < m; j++) {
            if (j > 0) cout << ' ';
            cout << satr[j];
        }
        cout << endl;
    }
    return 0;
}
```

Input:

```text
4 3
5 5 5
1 2 3
9 0 0
3 2 1
```

Output:

```text
1 2 3
3 2 1
9 0 0
5 5 5
```

The sums are 15, 6, 9, 6. `1 2 3` and `3 2 1` have equal sums (6), so they stay in input order — this is a **stable** sort. That is why C++ uses `stable_sort` rather than `sort`; since the rows are `vector`s they can be swapped as wholes (the rows of a static `int a[100][100]` cannot be swapped by `sort`).

:::warning
**Common mistakes**

- **`[[0] * m] * n` in Python.** All rows become one list; write `[[0] * m for _ in range(n)]`.
- **Swapping the indices.** `a[j][i]` goes out of bounds when *n* ≠ *m*; always `a[row][column]`.
- **Declaring a large static array inside `main`.** `int a[1000][1000]` overflows the stack and the program crashes on start; declare it globally.
- **Starting a maximum at `0`.** Wrong for negative numbers; start from the first element.
:::

:::tip
Test a matrix program on a small but non-square example (say 2 × 3): in a square matrix a mix-up of `i` and `j` may go unnoticed.
:::

## Summary

- A two-dimensional array is an array of arrays; `a[i][j]` is row *i*, column *j*.
- A static array is declared for the largest size; the actual size is kept in `n`, `m`.
- An outer loop over rows is traversal by rows; over columns, traversal by columns.
- Transposing: `t[j][i] = a[i][j]`, size *m* × *n*.
- Each row can be sorted as a separate array, or whole rows can be sorted by a key.

## Check yourself

:::check
Q: In a 3 × 5 matrix, which element is `a[2][4]`?
A: The element in the third row, fifth column — the last element of the last row.
Q: Why is `[[0] * 3] * 2` dangerous in Python?
A: Both rows refer to one list: after `a[0][0] = 1`, `a[1][0]` is 1 too.
Q: If we transpose a 2 × 5 matrix, what size is the result?
A: 5 × 2.
Q: What do you write in Python to sort every row in ascending order?
A: `for satr in a: satr.sort()` — each row is sorted as a separate list.
Q: When rows are sorted by their sum, does the order inside a row change?
A: No, whole rows change places.
:::

:::link https://docs.python.org/3/tutorial/datastructures.html#nested-list-comprehensions
Python: nested list comprehensions
Creating a matrix as a list of lists and transposing it.
:::

:::link https://en.cppreference.com/w/cpp/language/array
C++: arrays
A reference on static and multidimensional arrays.
:::
