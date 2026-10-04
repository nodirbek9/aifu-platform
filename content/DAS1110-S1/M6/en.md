---
title: Multi-dimensional arrays
summary: The two-dimensional array (matrix), rows and columns, a[i][j], creating, filling and printing it, row and column sums, diagonals, transposition; the copying trap in Python.
---

## Why it matters

An electronic register is a table: students in the rows, subjects in the columns. A cinema hall is rows and seats. A chessboard is 8 × 8 squares. A digital image is a table of pixels. An Excel sheet is rows and columns. Such data is naturally expressed not as a single line but as a **table**.

Tabular data is kept in a program in a **two-dimensional array** — a **matrix**. This lesson teaches how to create and fill a matrix and how to do the most common operations on it.

:::note
**After this lesson you will be able to:**

- explain the structure of a two-dimensional array (row, column, `a[i][j]`);
- create an *n* × *m* matrix in Python, Java and C++, fill it and print it as a table;
- compute row and column sums and the sums of the main and secondary diagonals;
- transpose a matrix and avoid the "same row" trap in Python.
:::

## The two-dimensional array

A **two-dimensional array (matrix)** is an array of arrays: each of its elements is itself an array (a row). An *n* × *m* matrix has *n* rows and *m* columns.

| Idea | How it is written | Example (a 3 × 4 matrix) |
|---|---|---|
| Element | `a[i][j]` — row *i*, column *j* | `a[1][2]` — the 2nd row, 3rd column |
| Number of rows | *n* | 3 |
| Number of columns | *m* | 4 |
| Row indices | 0 … *n* − 1 | 0, 1, 2 |
| Column indices | 0 … *m* − 1 | 0, 1, 2, 3 |

The first index is the **row**, the second the **column**. This convention follows the order in which a table is read: first the line, then the place in it.

In memory a matrix is stored row by row: first all the elements of row 0, then those of row 1, and so on. That is why traversing a matrix by rows (outer loop — row, inner loop — column) is faster.

A **square matrix** has as many rows as columns (*n* × *n*). It has two diagonals:

| Diagonal | Elements | Condition |
|---|---|---|
| Main | `a[0][0]`, `a[1][1]`, …, `a[n-1][n-1]` | *i* = *j* |
| Secondary | `a[0][n-1]`, `a[1][n-2]`, …, `a[n-1][0]` | *i* + *j* = *n* − 1 |

## Creating a matrix

| Task | Python | Java | C++ |
|---|---|---|---|
| *n* × *m* zeros | `a = [[0] * m for _ in range(n)]` | `int[][] a = new int[n][m];` | `vector<vector<int>> a(n, vector<int>(m));` |
| With values | `a = [[1, 2], [3, 4]]` | `int[][] a = {{1, 2}, {3, 4}};` | `vector<vector<int>> a = {{1, 2}, {3, 4}};` |
| Number of rows | `len(a)` | `a.length` | `a.size()` |
| Number of columns | `len(a[0])` | `a[0].length` | `a[0].size()` |

C++ also has the classic static matrix: `int a[10][10];` — its size is fixed in advance.

### The Python trap

`a = [[0] * m] * n` is **wrong**! It does not create *n* separate rows but *n* references to **one** row. Write `a[0][0] = 5` and the first element of every row becomes 5. The right way is to create each row separately: `[[0] * m for _ in range(n)]`.

## Traversing a matrix

A matrix is traversed with a **nested loop**: the outer loop over the rows, the inner loop over the columns.

| Task | Outer loop | Inner loop | Action |
|---|---|---|---|
| All elements | *i*: 0 … *n* − 1 | *j*: 0 … *m* − 1 | `a[i][j]` |
| Sum of row *i* | — | over *j* | `s += a[i][j]` |
| Sum of column *j* | — | over *i* | `s += a[i][j]` |
| Main diagonal | *i*: 0 … *n* − 1 | not needed | `a[i][i]` |

A diagonal needs no nested loop: there is only one diagonal element per row. A nested loop over all *n*² elements with `if i == j` also works, but it is *n* times slower.

## Worked example 1: row and column sums

The first line has *n* and *m*, the next *n* lines *m* integers each. Print the sum of each row (each on its own line), then all the column sums on one line.

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

for i in range(n):
    satr = 0
    for j in range(m):
        satr += a[i][j]
    print(satr)

ustunlar = []
for j in range(m):
    ustun = 0
    for i in range(n):
        ustun += a[i][j]
    ustunlar.append(ustun)
print(*ustunlar)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                a[i][j] = in.nextInt();
            }
        }

        for (int i = 0; i < n; i++) {
            int satr = 0;
            for (int j = 0; j < m; j++) {
                satr += a[i][j];
            }
            System.out.println(satr);
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < m; j++) {
            int ustun = 0;
            for (int i = 0; i < n; i++) {
                ustun += a[i][j];
            }
            if (j > 0) sb.append(' ');
            sb.append(ustun);
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
    int n, m;
    cin >> n >> m;
    vector<vector<int>> a(n, vector<int>(m));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            cin >> a[i][j];
        }
    }

    for (int i = 0; i < n; i++) {
        int satr = 0;
        for (int j = 0; j < m; j++) {
            satr += a[i][j];
        }
        cout << satr << endl;
    }

    for (int j = 0; j < m; j++) {
        int ustun = 0;
        for (int i = 0; i < n; i++) {
            ustun += a[i][j];
        }
        if (j > 0) cout << ' ';
        cout << ustun;
    }
    cout << endl;
    return 0;
}
```

Input:

```text
3 4
5 4 3 5
4 4 5 3
3 5 4 4
```

Think of it as the marks of 3 students in 4 subjects. The row sums (*satr*) are each student's total: `17`, `16`, `16`. The column sums (*ustun*) are the total for each subject: `12 13 12 12`.

For column sums the order of the loops swaps: the **outer loop is the column** (*j*), the inner one the row (*i*). The indices are still `a[i][j]` — row first, then column.

## Worked example 2: diagonals and transposition

The first line has *n*, the next *n* lines *n* numbers each: a square matrix. Print the sums of the main and secondary diagonals, then the transposed matrix.

**Transposition** turns rows into columns: in the new matrix `t[i][j] = a[j][i]`.

```python
n = int(input())
a = [list(map(int, input().split())) for _ in range(n)]

bosh = 0
yordamchi = 0
for i in range(n):
    bosh += a[i][i]
    yordamchi += a[i][n - 1 - i]
print("Main diagonal:", bosh)
print("Secondary diagonal:", yordamchi)

for i in range(n):
    satr = []
    for j in range(n):
        satr.append(a[j][i])
    print(*satr)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] a = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = in.nextInt();
            }
        }

        int bosh = 0, yordamchi = 0;
        for (int i = 0; i < n; i++) {
            bosh += a[i][i];
            yordamchi += a[i][n - 1 - i];
        }
        System.out.println("Main diagonal: " + bosh);
        System.out.println("Secondary diagonal: " + yordamchi);

        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(a[j][i]);
            }
            System.out.println(sb);
        }
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
    vector<vector<int>> a(n, vector<int>(n));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            cin >> a[i][j];
        }
    }

    int bosh = 0, yordamchi = 0;
    for (int i = 0; i < n; i++) {
        bosh += a[i][i];
        yordamchi += a[i][n - 1 - i];
    }
    cout << "Main diagonal: " << bosh << endl;
    cout << "Secondary diagonal: " << yordamchi << endl;

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (j > 0) cout << ' ';
            cout << a[j][i];
        }
        cout << endl;
    }
    return 0;
}
```

Input `3` and the matrix:

```text
1 2 3
4 5 6
7 8 9
```

1. The main diagonal (*bosh*): 1 + 5 + 9 = 15.
2. The secondary diagonal (*yordamchi*): `a[0][2]` + `a[1][1]` + `a[2][0]` = 3 + 5 + 7 = 15.
3. The transposed matrix (`a[j][i]` is printed):

```text
1 4 7
2 5 8
3 6 9
```

The transposed matrix was not stored separately — swapping the indices is enough to print it. If a matrix has to be transposed in place, only the elements above the main diagonal (*j* > *i*) are swapped with `a[j][i]`; swapping all of them transposes the matrix twice and brings it back to where it started.

:::warning
**Common mistakes**

- **`[[0] * m] * n` in Python.** All the rows are one object: changing one element changes a whole column.
- **Swapped indices.** `a[j][i]` and `a[i][j]` are different elements; with *n* ≠ *m*, out of bounds.
- **Mixing up the numbers of rows and columns.** The number of columns is `len(a[0])` / `a[0].length`, not `len(a)`.
- **`a[i][n - i]` for the secondary diagonal.** The correct form is `a[i][n - 1 - i]`; otherwise it goes out of bounds at *i* = 0.
- **Resetting the row sum outside the loop.** The accumulator for each row is reset **inside** the outer loop.
:::

:::tip
Before solving a matrix problem, write a 3 × 3 example on paper with its indices (`a[0][0]` … `a[2][2]`). Mark the elements you need and find the relation between their indices (for example, *i* + *j* = 2).
:::

## Summary

- A matrix is an array of arrays; in `a[i][j]` the row comes first, then the column.
- Python: `[[0] * m for _ in range(n)]`, Java: `new int[n][m]`, C++: `vector<vector<int>> a(n, vector<int>(m))`.
- A matrix is traversed with a nested loop; for a row sum the outer loop is the row, for a column sum the column.
- Main diagonal: *i* = *j*; secondary: *i* + *j* = *n* − 1; one loop is enough for a diagonal.
- Transposition: `t[i][j] = a[j][i]`.
- Do not use `[[0] * m] * n` in Python — the rows end up as one object.

## Check yourself

:::check
Q: How many elements does a 4 × 5 matrix have, and what are the indices of the last one?
A: 20; the last element is `a[3][4]`.
Q: Which diagonal of a 5 × 5 matrix does `a[1][3]` lie on?
A: The secondary one: 1 + 3 = 4 = *n* − 1.
Q: What does Python print for `a = [[0] * 3] * 2; a[0][1] = 7; print(a)`?
A: `[[0, 7, 0], [0, 7, 0]]`: both rows are references to one list.
Q: What size is a 2 × 3 matrix after transposition?
A: 3 × 2.
Q: Does the sum of the main diagonal need a nested loop?
A: No: each row has one diagonal element (`a[i][i]`), so one loop is enough.
:::

:::link https://docs.python.org/3/faq/programming.html#how-do-i-create-a-multidimensional-list
Python FAQ: a multidimensional list
About the `[[0] * m] * n` trap and the right way.
:::

:::link https://en.wikipedia.org/wiki/Transpose
Wikipedia: matrix transposition
The definition and properties of transposition.
:::
