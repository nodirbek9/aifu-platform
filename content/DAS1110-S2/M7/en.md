---
title: Programming in the Visual Studio environment
summary: The parts of an integrated development environment (IDE), the solution and project in Visual Studio, the Debug and Release configurations, compiler warnings, the debugger — breakpoints, stepping, the Watch and Call Stack windows; looking for a bug systematically.
---

## Why it matters

So far you may have written programs in a plain text editor and compiled them from the command line. For small programs that is enough. But once a program reaches hundreds of lines and the result comes out wrong, answering "where is the bug?" by adding `print`s can take hours.

An **integrated development environment** (IDE) brings the editor, the compiler and the **debugger** together in one window. The debugger stops the program at any line, shows the values of all variables and lets you run the program step by step. Professional programmers spend a large share of their time exactly in the debugger. In this lecture we learn these tools using Visual Studio; they are almost the same in IntelliJ IDEA, PyCharm and VS Code.

:::note
**After this lesson you will be able to:**

- name the main parts of an IDE and their jobs;
- create a solution and a project in Visual Studio and build and run a C++ program;
- explain the difference between the Debug and Release configurations;
- set a breakpoint, step through a program and watch variables;
- look for a bug systematically — hypothesis, check, fix.
:::

## The parts of an IDE

| Part | Its job | In Visual Studio |
|---|---|---|
| Editor | writing code, colour highlighting, auto-completion | IntelliSense — suggestions appear as you start typing a name |
| Project system | keeps which files make up the program and how it is built | the Solution Explorer window |
| Builder | calls the compiler and the linker | Build → Build Solution (Ctrl+Shift+B) |
| Error list | compiler errors and warnings | the Error List window (double-clicking an error jumps to that line) |
| Debugger | stopping the program, stepping, viewing values | the Debug menu, the Locals, Watch and Call Stack windows |

## Solution and project

In Visual Studio work is organised inside a **solution** (a `.sln` file). A solution consists of one or more **projects** (`.vcxproj` for C++); each project produces one program or library.

Creating a new C++ program:

1. File → New → Project → choose the **Console App** (C++) template.
2. Enter the project's name and location → Create.
3. A `.cpp` file appears under `Source Files` in Solution Explorer — this is where you write the code.
4. Ctrl+F5 runs without the debugger (the window does not close when the program ends), F5 runs with the debugger.

Choosing the C++ standard: right-click the project → Properties → C/C++ → Language → **C++ Language Standard** (say, ISO C++17). The warning level: C/C++ → General → **Warning Level** — `/W4` is recommended for study programs.

## Debug and Release

| | Debug | Release |
|---|---|---|
| Purpose | hunting bugs | the version given to users |
| Optimisation | none — the code runs as written | on — the compiler rearranges the code |
| Debug information | full — every line and variable is visible | minimal |
| `assert` | works | switched off (`NDEBUG` is defined) |
| Speed | slower | can be several times faster |

The configuration is chosen from a list on the toolbar. An important lesson: some bugs (say, a variable without an initial value) may "work" in Debug and give a different result in Release — a program is tested in both modes.

## Working with the debugger

A **breakpoint** is a mark where the program stops when it reaches that line. It is set by clicking the left margin of a line or with F9, and is shown as a red circle. Once the program stops:

| Action | Visual Studio / VS Code | IntelliJ IDEA / PyCharm |
|---|---|---|
| Start with the debugger | F5 | Shift+F9 |
| Set/remove a breakpoint | F9 | Ctrl+F8 |
| To the next line (without entering functions) — Step Over | F10 | F8 |
| Into a function — Step Into | F11 | F7 |
| Out of a function — Step Out | Shift+F11 | Shift+F8 |
| Continue to the next breakpoint | F5 | F9 |

Useful windows while stopped:

- **Locals** (Variables) — all variables of the current function and their values;
- **Watch** — expressions you add (say, `a[i] + a[j]`, `yigindi / n`), recomputed at every step;
- **Call Stack** — which function called which; very useful for seeing the chain of calls in recursion;
- **A conditional breakpoint** — right-click a breakpoint → Conditions: for example, stop when `i == 500`. In large loops it saves pressing F5 a thousand times.

## Looking for a bug systematically

The debugger is a tool; finding a bug is a **method**. An effective order:

1. **Reproduce.** Find the smallest input that triggers the bug every time.
2. **Compute the expected result by hand.** What *should* the program print?
3. **Form a hypothesis.** Which part might be wrong?
4. **Check.** Set a breakpoint there and compare the variables with the expected values. Find the line where a value goes "bad" for the first time.
5. **Fix and test again** — together with all earlier tests.

When a breakpoint is not possible (in an automatic checking system, say), debug information can go to the **error stream**: `cerr` (C++), `System.err` (Java), `sys.stderr` (Python). This stream does not mix with the main output and tests do not check it.

## Worked example 1: a bug in an average

The task: *n* and *n* integers are given; print the arithmetic mean with two decimal places. A student wrote the following C++ code and got `1.00` for `3` and `1 2 2`, although the answer should be `1.67`:

```text
int yigindi = 0;
for (int i = 0; i < n; i++) yigindi += a[i];
double ortacha = yigindi / n;          // ← the breakpoint is on this line
printf("%.2f\n", ortacha);
```

Stopping on this line in the debugger, we add expressions to the Watch window:

| Expression | Value | Conclusion |
|---|---|---|
| `yigindi` | 5 | correct |
| `n` | 3 | correct |
| `yigindi / n` | 1 | **the bug is here**: both operands are `int`, so the division is integer |
| `(double) yigindi / n` | 1.6666… | the value we need |

The bug is `int / int` integer division: even though `ortacha` (average) is a `double`, the right-hand side is computed as an integer first (*yigindi* is the sum). The fixed program:

```python
n = int(input())
a = list(map(int, input().split()))

yigindi = sum(a)
ortacha = yigindi / n                  # in Python / is always true division
print(f"{ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int yigindi = 0;
        for (int i = 0; i < n; i++) yigindi += in.nextInt();

        double ortacha = (double) yigindi / n;     // first to double, then divide
        System.out.printf("%.2f%n", ortacha);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    long long yigindi = 0;
    for (int i = 0; i < n; i++) yigindi += a[i];
    double ortacha = (double) yigindi / n;     // first to double, then divide
    printf("%.2f\n", ortacha);
    return 0;
}
```

For `3` and `1 2 2` the output is now `1.67`. In Python this bug does not occur: `/` is always true division, and integer division is `//`. In Java and C++, however, it is one of the most common bugs — which is why watching **intermediate expressions** (`yigindi / n`) separately in the Watch window is so useful.

## Worked example 2: debug output and assert

*n* is given (2 ≤ *n* ≤ 10¹²). Print its prime factors in ascending order. The program writes every step to the **error stream** — this does not affect the main output — and at the end checks the result with `assert`: the product of the factors must equal *n*.

```python
import sys

n = int(input())
qoldiq = n
kopaytuvchilar = []
d = 2
while d * d <= qoldiq:
    while qoldiq % d == 0:
        kopaytuvchilar.append(d)
        qoldiq //= d
        print(f"[debug] {d} found, left {qoldiq}", file=sys.stderr)
    d += 1
if qoldiq > 1:
    kopaytuvchilar.append(qoldiq)

kopaytma = 1
for p in kopaytuvchilar:
    kopaytma *= p
assert kopaytma == n, "wrong factors"

print(*kopaytuvchilar)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long qoldiq = n;
        List<Long> kopaytuvchilar = new ArrayList<>();
        for (long d = 2; d * d <= qoldiq; d++) {
            while (qoldiq % d == 0) {
                kopaytuvchilar.add(d);
                qoldiq /= d;
                System.err.println("[debug] " + d + " found, left " + qoldiq);
            }
        }
        if (qoldiq > 1) kopaytuvchilar.add(qoldiq);

        long kopaytma = 1;
        for (long p : kopaytuvchilar) kopaytma *= p;
        assert kopaytma == n : "wrong factors";   // enabled with java -ea

        StringBuilder sb = new StringBuilder();
        for (long p : kopaytuvchilar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(p);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <cassert>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long qoldiq = n;
    vector<long long> kopaytuvchilar;
    for (long long d = 2; d * d <= qoldiq; d++) {
        while (qoldiq % d == 0) {
            kopaytuvchilar.push_back(d);
            qoldiq /= d;
            cerr << "[debug] " << d << " found, left " << qoldiq << endl;
        }
    }
    if (qoldiq > 1) kopaytuvchilar.push_back(qoldiq);

    long long kopaytma = 1;
    for (long long p : kopaytuvchilar) kopaytma *= p;
    assert(kopaytma == n);                     // switched off in Release (NDEBUG)

    for (size_t i = 0; i < kopaytuvchilar.size(); i++) {
        if (i > 0) cout << ' ';
        cout << kopaytuvchilar[i];
    }
    cout << endl;
    return 0;
}
```

For `360` the main output is `2 2 2 3 3 5`. The error stream shows the steps:

```text
[debug] 2 found, left 180
[debug] 2 found, left 90
[debug] 2 found, left 45
[debug] 3 found, left 15
[debug] 3 found, left 5
```

In Visual Studio the error stream shows in the console window together with the main output, while an automatic check compares only the main stream (*kopaytuvchilar* are the factors, *qoldiq* the remainder, *kopaytma* the product). If the `assert` condition fails, the program stops with a message, and it is clear at once that the bug is right there. In Java `assert` is off by default — it is turned on with `java -ea Main`; in C++ it is switched off in the Release configuration because of `NDEBUG`. That is why nothing that affects the program's work is written inside an `assert`.

:::warning
**Common mistakes**

- **Ignoring warnings.** A warning such as "variable used without being initialized" is often a real bug; build with `/W4` and fix them.
- **Trying to debug in Release.** Because of optimisation, variables "disappear" and steps jump; choose Debug for debugging.
- **Leaving debug `print`s in the main output.** An automatic check counts them as a wrong answer; use `cerr`, `System.err`, `sys.stderr`, or remove them.
- **An important action inside `assert`.** `assert(++k < n)` is not executed at all in Release, and the program's behaviour changes.
:::

:::tip
When a bug is found, keep the input that caused it as a test. After every fix, run all the saved tests again — that way an old bug does not come back.
:::

## Summary

- An IDE combines the editor, project system, builder, error list and debugger.
- In Visual Studio work is organised in a solution (`.sln`) and its projects; the standard and warning level are set in the project properties.
- Debug is for debugging (no optimisation, `assert` works), Release for users.
- The debugger: a breakpoint (F9), Step Over (F10), Step Into (F11), the Locals, Watch and Call Stack windows.
- A bug is hunted systematically: reproduce → expected result → hypothesis → check → fix.

## Check yourself

:::check
Q: What is the difference between Step Over (F10) and Step Into (F11)?
A: Step Over runs the current line completely and moves to the next; Step Into enters the function called on that line.
Q: Why is debugging in the Release configuration inconvenient?
A: Optimisation rearranges the code: variables are not visible and the steps do not match the source code.
Q: What does the Call Stack window show?
A: Through which chain of functions the current point was reached — who called whom.
Q: Where can debug messages go so that they do not disturb an automatic check?
A: To the error stream: `cerr`, `System.err` or `sys.stderr`.
Q: After `int yigindi = 5, n = 2; double x = yigindi / n;`, what is `x` and why?
A: 2.0: `yigindi / n` is integer division, and the result is converted to `double` only afterwards.
:::

:::link https://learn.microsoft.com/en-us/visualstudio/debugger/debugger-feature-tour
Visual Studio: a first look at the debugger
The official guide to breakpoints, stepping and watching variables.
:::

:::link https://code.visualstudio.com/docs/editor/debugging
VS Code: debugging
Debugging Python, Java and C++ programs in VS Code.
:::
