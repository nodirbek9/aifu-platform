---
title: Using matrices as function parameters
summary: Independent work: passing a matrix to a function and returning one, finding its dimensions, vector<vector<int>> and a static matrix in C++; the sample problem — a matrix's trace and a symmetry check; the assignment — multiplying matrices.
---

## Why it matters

Operations on matrices — addition, multiplication, transposition, rotation — underlie engineering computations, computer graphics and artificial intelligence. If you write every operation as a separate function, you can connect them to build complex computations: `kopaytir(transponirla(A), B)` (multiply, transpose). For that you need to know how to pass a matrix to a function and return a new matrix from one.

:::note
**After this independent work you will be able to:**

- pass a matrix to a function in three languages and find its dimensions inside the function;
- return a new matrix from a function;
- write separate functions for reading, printing and computing with a matrix;
- build a table of tests for a matrix function.
:::

## A short review of the topic

| Language | Parameter | Number of rows | Number of columns | Returning a new matrix |
|---|---|---|---|---|
| Python | `def f(m):` | `len(m)` | `len(m[0])` | `return [[0] * p for _ in range(n)]` |
| Java | `static int f(int[][] m)` | `m.length` | `m[0].length` | `return new int[n][p];` |
| C++ | `int f(const vector<vector<int>>& m)` | `m.size()` | `m[0].size()` | `return vector<vector<int>>(n, vector<int>(p));` |
| C++ (static) | `int f(int m[][100], int n, int k)` | the parameter `n` | the parameter `k` | cannot be returned — the result goes through a parameter |

Notes:

1. In C++, when passing a static matrix the **second** dimension must be written (`int m[][100]`): the compiler needs the row length to compute the address of `m[i][j]`. That is why `vector` is more convenient.
2. If a function only reads the matrix, write `const &` in C++ — no copy of a large matrix is made.
3. Returning a `vector` from a function in C++ is fast: the compiler "moves" it instead of copying.
4. Make reading and printing a matrix separate functions too — they are needed again in every matrix problem.

## The sample problem: trace and symmetry

The first line holds *n*, then an *n* × *n* square matrix. The function `iz(m)` (trace) finds the sum of the main diagonal, and `simmetrikmi(m)` (is symmetric) whether the matrix equals its transpose (every `m[i][j] == m[j][i]`). Print the trace and `yes` or `no`.

```python
def oqi(n):
    return [list(map(int, input().split())) for _ in range(n)]


def iz(m):
    return sum(m[i][i] for i in range(len(m)))


def simmetrikmi(m):
    n = len(m)
    for i in range(n):
        for j in range(i + 1, n):           # only the half above the diagonal
            if m[i][j] != m[j][i]:
                return False
    return True


n = int(input())
m = oqi(n)
print(iz(m))
print("yes" if simmetrikmi(m) else "no")
```
```java
import java.util.Scanner;

public class Main {
    static int[][] oqi(Scanner in, int n) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = in.nextInt();
        return m;
    }

    static long iz(int[][] m) {
        long s = 0;
        for (int i = 0; i < m.length; i++) s += m[i][i];
        return s;
    }

    static boolean simmetrikmi(int[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)     // only the half above the diagonal
                if (m[i][j] != m[j][i]) return false;
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] m = oqi(in, n);
        System.out.println(iz(m));
        System.out.println(simmetrikmi(m) ? "yes" : "no");
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

using Matritsa = vector<vector<int>>;          // a short name

Matritsa oqi(int n) {
    Matritsa m(n, vector<int>(n));
    for (auto& satr : m)
        for (int& x : satr) cin >> x;
    return m;
}

long long iz(const Matritsa& m) {
    long long s = 0;
    for (size_t i = 0; i < m.size(); i++) s += m[i][i];
    return s;
}

bool simmetrikmi(const Matritsa& m) {
    size_t n = m.size();
    for (size_t i = 0; i < n; i++)
        for (size_t j = i + 1; j < n; j++)      // only the half above the diagonal
            if (m[i][j] != m[j][i]) return false;
    return true;
}

int main() {
    int n;
    cin >> n;
    Matritsa m = oqi(n);
    cout << iz(m) << endl;
    cout << (simmetrikmi(m) ? "yes" : "no") << endl;
    return 0;
}
```

The table of tests:

| Input | Expected result | What it checks |
|---|---|---|
| `3` / `1 2 3` / `2 5 4` / `3 4 9` | `15`, `yes` | a symmetric matrix |
| `2` / `1 2` / `3 4` | `5`, `no` | not symmetric |
| `1` / `-7` | `-7`, `yes` | a 1 × 1 matrix |
| `2` / `0 0` / `0 0` | `0`, `yes` | the zero matrix |

`simmetrikmi` starts the inner loop at `j = i + 1`: there is no need to compare diagonal elements with themselves or to check every pair twice (*oqi* means read). In C++ `using Matritsa = ...` gives a long type a short name; it makes the function descriptions easier to read.

## The independent work assignment

Solve the assignment below: write a function that takes matrices *A* (*n* × *m*) and *B* (*m* × *p*) and returns **C = A · B**: `C[i][j]` is the sum of the products of the elements of row *i* of *A* and column *j* of *B*. What to hand in:

1. **A description of the functions** — at least three functions: reading a matrix, multiplying, printing; the parameters and return value of each.
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of tests** — at least 4 tests: square matrices, rectangular matrices (say, 2 × 3 and 3 × 1), multiplying by the identity matrix, negative elements; with each result computed by hand.
4. **A conclusion** — 3–5 sentences: how many operations multiplication takes (in terms of *n*, *m*, *p*), and why the number of columns of *A* must equal the number of rows of *B*.

:::warning
**Grading criteria and common mistakes**

- Multiplication must be a separate **function** that returns a new matrix.
- The result matrix is *n* × *p*: the inner loop runs up to *m* — do not mix up the indices.
- The sum `C[i][j]` starts from zero for every cell.
- Elements are up to 1000 and *m* up to 50: the sum fits in an `int`, but show the bound check in your conclusion.
:::

:::tip
Test with rectangular matrices: with square ones a mix-up of `n`, `m` and `p` may go unnoticed.
:::

## Summary

- A matrix is passed to a function as an array of arrays; its dimensions are found with `len(m)`, `len(m[0])` (or `length`, `size()`).
- In C++ `const vector<vector<int>>&` passes it without copying or changing; a static matrix needs its second dimension written.
- Returning a new matrix from a function is an ordinary `return` in all three languages.
- With reading, computing and printing in separate functions, they can be reused in other problems.

## Check yourself

:::check
Q: How do you find the number of columns of an `int[][] m` matrix in Java?
A: `m[0].length` (if the matrix is not empty).
Q: Why does `void f(int m[][])` not compile in C++?
A: The second dimension is missing; the compiler needs the row length to compute the address of `m[i][j]`.
Q: If a 2 × 3 matrix is multiplied by a 3 × 4 matrix, what size is the result?
A: 2 × 4.
Q: Why does `simmetrikmi` start the inner loop at `j = i + 1`?
A: Diagonal elements equal themselves, and checking each pair `(i, j)`–`(j, i)` once is enough.
:::

:::link https://numpy.org/doc/stable/reference/generated/numpy.matmul.html
NumPy: matmul
In practice Python multiplies matrices with the NumPy library — this is that function's documentation.
:::
