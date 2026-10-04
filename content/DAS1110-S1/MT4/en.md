---
title: Organising matrices
summary: Independent work: a program working with a matrix — input, computations by rows and columns, choosing the needed elements by a condition on the indices; the sample problem is the maximum of each row and the row with the largest sum.
---

## Why it matters

Finding each student's best mark and the best student in a group register, finding the most effective branch in a table of monthly sales, finding the brightest row of an image — all of these are the same operations on a matrix. In this independent work you practise choosing the needed elements of a matrix through a condition on the indices.

:::note
**After this independent work you will be able to:**

- read an *n* × *m* matrix and traverse it with a nested loop;
- compute a separate result for every row (or column);
- express the needed elements as a condition on the indices (*i* = *j*, *i* + *j* = *n* − 1, *i* < *j* …);
- work out the result by hand on 2 × 2 and 3 × 3 examples and check the program.
:::

## A quick review of the topic

Many "parts" of a matrix are described by a condition on the indices (a square *n* × *n* matrix):

| Part | Condition | Elements in a 3 × 3 |
|---|---|---|
| Main diagonal | *i* = *j* | `a[0][0]`, `a[1][1]`, `a[2][2]` |
| Secondary diagonal | *i* + *j* = *n* − 1 | `a[0][2]`, `a[1][1]`, `a[2][0]` |
| Above the main diagonal | *i* < *j* | `a[0][1]`, `a[0][2]`, `a[1][2]` |
| Below the main diagonal | *i* > *j* | `a[1][0]`, `a[2][0]`, `a[2][1]` |
| Row *k* | *i* = *k* | `a[k][0]` … `a[k][n-1]` |

If the part has one element per row (the diagonals), one loop is enough. If it has many — a nested loop and a condition.

## Sample problem: analysing rows

The first line has *n* and *m*, the next *n* lines *m* numbers each. On the first output line print the largest element of each row; on the second, the number of the row with the largest sum (from 1; the first one if there are several).

```text
1. read n, m and the matrix
2. eng_yaxshi = 0, eng_katta = the sum of the first row
3. for each row i:
       mx = a[i][0], s = 0
       for each j: mx = max(mx, a[i][j]); s += a[i][j]
       remember mx
       if s > eng_katta: eng_yaxshi = i, eng_katta = s
4. print all the mx values, then eng_yaxshi + 1
```

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

maksimumlar = []
eng_yaxshi = 0
eng_katta = sum(a[0])
for i in range(n):
    mx = a[i][0]
    s = 0
    for j in range(m):
        if a[i][j] > mx:
            mx = a[i][j]
        s += a[i][j]
    maksimumlar.append(mx)
    if s > eng_katta:
        eng_katta = s
        eng_yaxshi = i

print(*maksimumlar)
print(eng_yaxshi + 1)
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

        StringBuilder sb = new StringBuilder();
        int engYaxshi = 0;
        long engKatta = Long.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int mx = a[i][0];
            long s = 0;
            for (int j = 0; j < m; j++) {
                if (a[i][j] > mx) mx = a[i][j];
                s += a[i][j];
            }
            if (i > 0) sb.append(' ');
            sb.append(mx);
            if (s > engKatta) {
                engKatta = s;
                engYaxshi = i;
            }
        }

        System.out.println(sb);
        System.out.println(engYaxshi + 1);
    }
}
```
```cpp
#include <iostream>
#include <vector>
#include <climits>
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

    int engYaxshi = 0;
    long long engKatta = LLONG_MIN;
    for (int i = 0; i < n; i++) {
        int mx = a[i][0];
        long long s = 0;
        for (int j = 0; j < m; j++) {
            if (a[i][j] > mx) mx = a[i][j];
            s += a[i][j];
        }
        if (i > 0) cout << ' ';
        cout << mx;
        if (s > engKatta) {
            engKatta = s;
            engYaxshi = i;
        }
    }
    cout << endl << engYaxshi + 1 << endl;
    return 0;
}
```

Input:

```text
3 4
2 7 1 8
2 8 1 8
1 4 1 4
```

| Row | Largest | Sum |
|---|---|---|
| 1 | 8 | 18 |
| 2 | 8 | 19 |
| 3 | 4 | 10 |

The output is `8 8 4` and `2`. For every row `mx` and `s` start afresh **inside** the outer loop — the most important point. In Java and C++ the largest sum (*eng_katta*) starts at the smallest possible value (`Long.MIN_VALUE`, `LLONG_MIN`); in Python at the sum of the first row.

## The independent work task

Solve the **matrix diagonals** problem given below. What you hand in:

1. **Index conditions** — write the conditions for the elements of the main and secondary diagonals, draw a 4 × 4 matrix and mark the diagonal elements.
2. **The odd *n* case** — at which element do the diagonals cross when *n* is odd? Does it belong to both sums according to the statement — decide and follow that in the program.
3. **The program** — in Python, Java or C++; submitted in the task window below, and it must pass the automatic tests.
4. **Tests** — at least 3: 1 × 1, 2 × 2 and 3 × 3 matrices, with the results worked out by hand.

:::warning
**Assessment criteria and common mistakes**

- The secondary diagonal is `a[i][n - 1 - i]`, not `a[i][n - i]`.
- A diagonal does not need a nested loop; if one is used, the result must still be right.
- Do not create a matrix in Python with `[[0] * n] * n` — the rows end up as one object.
- The results must be checked by hand.
:::

:::tip
In a 1 × 1 matrix the single element lies on both diagonals. Write down on paper beforehand what your program should print in that case, and compare.
:::

## Summary

- Parts of a matrix are described by conditions on the indices: *i* = *j*, *i* + *j* = *n* − 1, *i* < *j*.
- One element per row — one loop; many — a nested loop.
- For per-row results the accumulator and the maximum start inside the outer loop.
- Check the result by hand on small matrices (1 × 1, 2 × 2, 3 × 3).

## Check yourself

:::check
Q: How many elements lie above the main diagonal of a 5 × 5 matrix?
A: 10: (25 − 5) / 2.
Q: At which element do the main and secondary diagonals cross when *n* is odd?
A: At the centre element: `a[n // 2][n // 2]`.
Q: What happens in the sample if `mx` is set only once, outside the loop?
A: Each row would also take the maximum of the previous rows into account — the result would be wrong.
:::

:::link https://en.wikipedia.org/wiki/Main_diagonal
Wikipedia: the main diagonal
About the diagonals of a matrix.
:::
