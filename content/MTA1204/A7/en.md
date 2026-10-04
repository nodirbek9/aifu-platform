---
title: Optimisation problems on graphs. Dijkstra's and Floyd's algorithms (practical session)
summary: The shortest path in an unweighted graph — breadth-first search (BFS) and a maze; finding the centre of a graph with Floyd–Warshall (the hospital placement problem); negative edge weights and detecting a negative cycle; choosing the right shortest-path algorithm.
---

## Why it matters

In the lecture we saw Dijkstra's and Floyd's algorithms. In practice the question is: which algorithm when? If all edges have the same "cost" (steps in a maze, "handshakes" in a social network), Dijkstra is overkill — plain **breadth-first search** is enough and faster. If the question is about all pairs ("in which district should the hospital be built?"), Floyd is more convenient. And when negative weights appear, care is needed: a negative cycle breaks the very notion of a "shortest path".

:::note
**After this session you will be able to:**

- find the shortest path in an unweighted graph (a maze) with BFS;
- find the centre of a graph using the result of Floyd–Warshall;
- detect a negative cycle with Floyd;
- choose BFS, Dijkstra or Floyd by the conditions of the problem.
:::

## Breadth-first search (BFS)

**BFS** (breadth-first search) walks the graph from the start vertex **level by level**: first all vertices 1 step away, then those 2 steps away, and so on. A queue is used. In an unweighted graph the number of steps at which a vertex is first reached is its shortest distance.

```text
dist[s] = 0; queue = [s]
while the queue is not empty:
    v = from the front of the queue
    for every neighbour u of v not yet seen:
        dist[u] = dist[v] + 1; put u at the end of the queue
```

The complexity is *O*(*V* + *E*) — faster than Dijkstra. A maze is a grid-shaped graph: every cell is a vertex, and neighbouring free cells are joined by edges.

## The centre of a graph

A vertex's **eccentricity** is its distance to the farthest vertex. The **centre of a graph** is the vertex with the smallest eccentricity. In practice it is the place for a hospital, a fire station or a warehouse that minimises the travel time to the farthest address. Since the distances between all pairs are needed, Floyd–Warshall is convenient.

## Negative weights and negative cycles

A negative weight is, say, a "bonus" on the way or a profit in currency exchange. Dijkstra does not work with negative weights, while Floyd does — provided the graph has no **negative cycle** (a cycle whose weights sum to a negative number). If there is one, going round it again and again shortens a path endlessly — there is no "shortest path".

Detecting it with Floyd is easy: once the algorithm finishes, if `d[i][i] < 0` for some *i*, then *i* lies on a negative cycle.

| Problem | Algorithm | Complexity |
|---|---|---|
| An unweighted graph, one source | BFS | *O*(*V* + *E*) |
| Non-negative weights, one source | Dijkstra (with a heap) | *O*((*V* + *E*) log *V*) |
| Negative weights, one source | Bellman–Ford | *O*(*V* · *E*) |
| All pairs, small *V* | Floyd–Warshall | *O*(*V*³) |

## Worked example 1: getting out of a maze (BFS)

The first line holds *R* and *C*, the next *R* lines the maze: `.` — a passage, `#` — a wall, `S` — the start, `E` — the exit. You may move up, down, left and right. Print the fewest steps to the exit (`-1` if it cannot be reached).

```python
from collections import deque

r, c = map(int, input().split())
labirint = [input() for _ in range(r)]
for i in range(r):
    for j in range(c):
        if labirint[i][j] == "S":
            boshlanish = (i, j)

masofa = [[-1] * c for _ in range(r)]       # -1 means not visited yet
masofa[boshlanish[0]][boshlanish[1]] = 0
navbat = deque([boshlanish])
javob = -1
while navbat:
    x, y = navbat.popleft()
    if labirint[x][y] == "E":
        javob = masofa[x][y]                # the first arrival is the shortest
        break
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        a, b = x + dx, y + dy
        if 0 <= a < r and 0 <= b < c and labirint[a][b] != "#" and masofa[a][b] == -1:
            masofa[a][b] = masofa[x][y] + 1
            navbat.append((a, b))
print(javob)
```
```java
import java.util.ArrayDeque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = in.nextInt(), c = in.nextInt();
        char[][] labirint = new char[r][];
        int sx = 0, sy = 0;
        for (int i = 0; i < r; i++) {
            labirint[i] = in.next().toCharArray();
            for (int j = 0; j < c; j++) if (labirint[i][j] == 'S') { sx = i; sy = j; }
        }

        int[][] masofa = new int[r][c];
        for (int[] satr : masofa) java.util.Arrays.fill(satr, -1);   // -1 means not visited yet
        masofa[sx][sy] = 0;
        ArrayDeque<int[]> navbat = new ArrayDeque<>();
        navbat.add(new int[] {sx, sy});
        int[][] yonalish = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int javob = -1;
        while (!navbat.isEmpty()) {
            int[] v = navbat.poll();
            if (labirint[v[0]][v[1]] == 'E') {
                javob = masofa[v[0]][v[1]];                 // the first arrival is the shortest
                break;
            }
            for (int[] d : yonalish) {
                int a = v[0] + d[0], b = v[1] + d[1];
                if (a >= 0 && a < r && b >= 0 && b < c && labirint[a][b] != '#' && masofa[a][b] == -1) {
                    masofa[a][b] = masofa[v[0]][v[1]] + 1;
                    navbat.add(new int[] {a, b});
                }
            }
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int r, c;
    cin >> r >> c;
    vector<string> labirint(r);
    int sx = 0, sy = 0;
    for (int i = 0; i < r; i++) {
        cin >> labirint[i];
        for (int j = 0; j < c; j++) if (labirint[i][j] == 'S') { sx = i; sy = j; }
    }

    vector<vector<int>> masofa(r, vector<int>(c, -1));      // -1 means not visited yet
    masofa[sx][sy] = 0;
    queue<pair<int, int>> navbat;
    navbat.push({sx, sy});
    int dx[] = {1, -1, 0, 0}, dy[] = {0, 0, 1, -1};
    int javob = -1;
    while (!navbat.empty()) {
        auto [x, y] = navbat.front();
        navbat.pop();
        if (labirint[x][y] == 'E') {
            javob = masofa[x][y];                           // the first arrival is the shortest
            break;
        }
        for (int k = 0; k < 4; k++) {
            int a = x + dx[k], b = y + dy[k];
            if (a >= 0 && a < r && b >= 0 && b < c && labirint[a][b] != '#' && masofa[a][b] == -1) {
                masofa[a][b] = masofa[x][y] + 1;
                navbat.push({a, b});
            }
        }
    }
    cout << javob << endl;
    return 0;
}
```

Input:

```text
5 8
S..#....
.#.#.##.
.#...#..
.####.#.
......#E
```

Output:

```text
15
```

Thanks to the queue, cells are visited in order of their distance from `S`, so the distance at which we first reach `E` is the shortest (*labirint* is the maze, *masofa* the distance, *navbat* the queue). Every cell enters the queue once: a maze of *R* · *C* cells is solved in *O*(*R* · *C*). In a maze such as `S#E` separated by a wall, the queue empties before reaching `E` and the answer is `-1`.

## Worked example 2: the centre of a graph — where to put the hospital?

The first line holds the number of districts *n* and roads *m*, the next *m* lines two-way roads `u v w` (*w* in minutes). The graph is connected. Print the district for which the time to the farthest district is smallest (the lowest number if there are several), and that time.

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = d[v - 1][u - 1] = min(d[u - 1][v - 1], w)

for k in range(n):                          # Floyd–Warshall
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

eksentrisitet = [max(satr) for satr in d]   # to the farthest district
markaz = min(range(n), key=lambda i: (eksentrisitet[i], i))
print(markaz + 1, eksentrisitet[markaz])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) d[i][j] = i == j ? 0 : INF;
        for (int k = 0; k < m; k++) {
            int u = in.nextInt() - 1, v = in.nextInt() - 1;
            long w = in.nextLong();
            d[u][v] = d[v][u] = Math.min(d[u][v], w);
        }

        for (int k = 0; k < n; k++)                     // Floyd–Warshall
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

        int markaz = 0;
        long eng = INF;
        for (int i = 0; i < n; i++) {
            long eksentrisitet = 0;                     // to the farthest district
            for (int j = 0; j < n; j++) eksentrisitet = Math.max(eksentrisitet, d[i][j]);
            if (eksentrisitet < eng) {
                eng = eksentrisitet;
                markaz = i;
            }
        }
        System.out.println((markaz + 1) + " " + eng);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n, vector<long long>(n, INF));
    for (int i = 0; i < n; i++) d[i][i] = 0;
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        u--;
        v--;
        d[u][v] = d[v][u] = min(d[u][v], w);
    }

    for (int k = 0; k < n; k++)                         // Floyd–Warshall
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];

    int markaz = 0;
    long long eng = INF;
    for (int i = 0; i < n; i++) {
        long long eksentrisitet = *max_element(d[i].begin(), d[i].end());   // to the farthest district
        if (eksentrisitet < eng) {
            eng = eksentrisitet;
            markaz = i;
        }
    }
    cout << markaz + 1 << " " << eng << endl;
    return 0;
}
```

Input:

```text
5 6
1 2 4
2 3 3
3 4 2
4 5 6
1 5 10
2 4 7
```

Output:

```text
3 8
```

From district 3 the farthest district is 5 (3 → 4 → 5: 2 + 6 = 8 minutes); from any other district the farthest address is at least as far (*markaz* is the centre, *eksentrisitet* the eccentricity). Floyd runs once and gives all *n*² distances — after that the eccentricity of each candidate district is a plain maximum. Running single-source Dijkstra *n* times would also work, but for small *n* Floyd's code is much shorter.

## Worked example 3: detecting a negative cycle

The first line holds *n* and *m*, the next *m* lines **directed** edges `u v w` (*w* may be negative). If the graph has a negative cycle, print `negative cycle`; otherwise print the shortest distances from vertex 1 to all vertices (`-` if unreachable).

```python
INF = float("inf")
n, m = map(int, input().split())
d = [[0 if i == j else INF for j in range(n)] for i in range(n)]
for _ in range(m):
    u, v, w = map(int, input().split())
    d[u - 1][v - 1] = min(d[u - 1][v - 1], w)

for k in range(n):
    for i in range(n):
        for j in range(n):
            if d[i][k] + d[k][j] < d[i][j]:
                d[i][j] = d[i][k] + d[k][j]

if any(d[i][i] < 0 for i in range(n)):      # returning to itself is negative — a negative cycle
    print("negative cycle")
else:
    print(*[x if x < INF else "-" for x in d[0]])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final long INF = Long.MAX_VALUE / 4;
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) d[i][j] = i == j ? 0 : INF;
        for (int k = 0; k < m; k++) {
            int u = in.nextInt() - 1, v = in.nextInt() - 1;
            d[u][v] = Math.min(d[u][v], in.nextLong());
        }

        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j])
                        d[i][j] = d[i][k] + d[k][j];

        boolean manfiySikl = false;
        for (int i = 0; i < n; i++) if (d[i][i] < 0) manfiySikl = true;   // returning to itself is negative — a negative cycle
        if (manfiySikl) {
            System.out.println("negative cycle");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < n; j++) {
            if (j > 0) sb.append(' ');
            sb.append(d[0][j] < INF ? String.valueOf(d[0][j]) : "-");
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    const long long INF = LLONG_MAX / 4;
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> d(n, vector<long long>(n, INF));
    for (int i = 0; i < n; i++) d[i][i] = 0;
    for (int k = 0; k < m; k++) {
        int u, v;
        long long w;
        cin >> u >> v >> w;
        d[u - 1][v - 1] = min(d[u - 1][v - 1], w);
    }

    for (int k = 0; k < n; k++)
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j])
                    d[i][j] = d[i][k] + d[k][j];

    for (int i = 0; i < n; i++) {
        if (d[i][i] < 0) {                              // returning to itself is negative — a negative cycle
            cout << "negative cycle" << endl;
            return 0;
        }
    }
    for (int j = 0; j < n; j++) {
        cout << (j ? " " : "");
        if (d[0][j] < INF) cout << d[0][j];
        else cout << "-";
    }
    cout << endl;
    return 0;
}
```

For `3 3` and the edges `1 2 4`, `2 3 -2`, `3 1 -3` the output is `negative cycle`: the cycle 1 → 2 → 3 → 1 weighs 4 − 2 − 3 = −1, and every lap shortens the path. If the last edge is `3 1 1`, the cycle sums to +3 and the answer is `0 4 2`: 1 → 2 (4), 1 → 2 → 3 (4 − 2 = 2). In Java and C++, checking for ∞ is essential with negative weights: otherwise `INF + (−3)` becomes "less than ∞" and a path that does not exist appears.

:::warning
**Common mistakes**

- **Using Dijkstra on an unweighted graph.** It works, but BFS is simpler and faster.
- **Marking a vertex in BFS when it is taken from the queue.** A vertex then enters the queue several times; mark it **when adding** it.
- **Arithmetic with ∞ and negative weights.** With a negative *w*, `INF + w` is less than ∞; check first that both parts are finite.
- **Printing a "shortest path" when there is a negative cycle.** In such a graph there is no answer — check `d[i][i] < 0` first.
:::

:::tip
Before choosing an algorithm, classify the graph with three questions: are the edges weighted? are there negative weights? is the question about one source or all pairs? The answers point to the right algorithm in the table above.
:::

## Summary

- In an unweighted graph the shortest path is found with BFS in *O*(*V* + *E*); a maze is a grid-shaped graph.
- The centre of a graph is the vertex with the smallest eccentricity; all the distances come from Floyd.
- Floyd works with negative weights; `d[i][i] < 0` means there is a negative cycle.
- An algorithm is chosen by the weights and the kind of question: BFS, Dijkstra, Bellman–Ford or Floyd.

## Check yourself

:::check
Q: Why does BFS give the shortest path in an unweighted graph?
A: Vertices are visited in order of their distance from the start; reaching a vertex first means reaching it in the fewest steps.
Q: What is a vertex's eccentricity?
A: Its shortest distance to the farthest vertex.
Q: If `d[2][2] = −5` after Floyd finishes, what does it mean?
A: Vertex 2 lies on a negative cycle.
Q: Which algorithm suits a maze of 10⁶ cells?
A: BFS — *O*(*R* · *C*), every cell is visited once.
:::

:::link https://cp-algorithms.com/graph/breadth-first-search.html
CP-Algorithms: breadth-first search
BFS, its uses and its complexity.
:::
