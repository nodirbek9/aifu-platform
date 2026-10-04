---
title: The algorithm and its definitions. The Turing machine and thesis
summary: The intuitive, formal and cybernetic definitions of an algorithm; the properties of discreteness, definiteness, effectiveness, generality and finiteness; kinds of algorithms; the Turing machine — tape, head, states and transition table; the Church–Turing thesis and uncomputable problems.
---

## Why it matters

We hear the word "algorithm" every day: a navigator finds a route, a social network orders a feed, a bank scores a loan. But what exactly may be called an algorithm? Is a recipe an algorithm? What about the instruction "write a good program"? These questions were taken seriously by mathematicians at the start of the 20th century: to answer "is there a mechanical method that solves every mathematical problem?", "mechanical method" itself first had to be defined precisely.

In this lecture we look at three definitions of the notion of an algorithm, its properties, and the most famous formal model — the **Turing machine**. This knowledge is the foundation of the whole course: when we compare the speed of algorithms in later topics, we rely on exactly these notions.

:::note
**After this lesson you will be able to:**

- tell the intuitive, formal and cybernetic definitions of an algorithm apart;
- list the five main properties of an algorithm and check them on examples;
- classify algorithms by their structure;
- explain how a Turing machine is built and trace a simple machine step by step;
- state what the Church–Turing thesis and uncomputable problems are.
:::

## Three definitions of an algorithm

| Definition | Content | Convenient for |
|---|---|---|
| **Intuitive** | a precise, ordered sequence of instructions given to an executor to reach a certain goal | everyday life, learning to program |
| **Formal** | a process written within a precise mathematical model (the Turing machine, Markov's normal algorithm, recursive functions) | mathematics, the theory of computation |
| **Cybernetic** | a system of commands that moves a controlled object from an initial state to a target state; the executor is a person, an automaton or a computer | control systems, automation |

The word "algorithm" comes from *Algoritmi*, the Latin form of the name of the great 9th-century scholar from Khorezm, **Muhammad ibn Musa al-Khwarizmi**. His work on calculating with Hindu numerals spread the rules of arithmetic through Europe.

## Properties of an algorithm

| Property | Meaning | An example of breaking it |
|---|---|---|
| **Discreteness** | the process consists of separate, successive steps | "heat it up slowly" — the step is not clear |
| **Definiteness** (determinism) | every step is unambiguous; the executor knows exactly what to do | "add a little salt" |
| **Effectiveness** | the algorithm gives the required result | a loop that never produces a result |
| **Generality** (mass character) | it works not for one problem but for a class of problems of one kind | instructions that find the GCD only of 12 and 18 |
| **Finiteness** | for any valid input it stops after a finite number of steps | `while true:` — a loop that never stops |

Proving finiteness is not always easy. The famous **Collatz conjecture**: starting from any natural number, halving it when it is even and multiplying it by 3 and adding 1 when it is odd, you eventually reach 1. This has been checked by computer for very large numbers but has **not been proved** — so for now nobody can guarantee that this process is finite for every input.

## Kinds of algorithms

| Kind | Structure | Example |
|---|---|---|
| Linear | commands in sequence, without conditions | computing a rectangle's area |
| Branching | different paths depending on a condition | the roots of a quadratic equation (the sign of the discriminant) |
| Repetitive (cyclic) | a group of commands runs many times | the sum of an array |
| Recursive | the algorithm calls itself with a smaller input | the factorial, the Towers of Hanoi |

Ways of describing algorithms: **in words**, as a **flowchart** (a diamond is a condition, a rectangle an action, a parallelogram input/output), in **pseudocode** and in a **programming language**.

## The Turing machine

In 1936 the English mathematician **Alan Turing** proposed an imaginary machine to define "mechanical computation". It is very simple, yet it can do whatever any computer can do.

| Part | Its job |
|---|---|
| **Tape** | infinite in both directions, divided into cells; each cell holds one symbol of the alphabet (a blank cell is `_`) |
| **Head** | sees one cell at a time: reads the symbol, writes, and moves one cell left (L) or right (R) |
| **States** | the machine's finite "memory": the start state, working states and the halting state |
| **Transition table** | the program: (current state, symbol read) → (symbol to write, move, new state) |

At every step the machine finds one row of the table and carries it out. If there is no matching row or the halting state is reached, the machine stops; the result is what remains on the tape.

## The Church–Turing thesis

**The Church–Turing thesis**: any function computable by an algorithm in the intuitive sense can also be computed by a Turing machine. It is not a theorem — since "intuitive algorithm" is not precisely defined, it cannot be proved. But every computational model proposed so far (Markov algorithms, the lambda calculus, modern programming languages) has been proved **equivalent in power** to the Turing machine.

An important consequence: there are problems that **cannot be solved by any algorithm at all**. The most famous is the **halting problem**: there is no algorithm that always correctly answers, for any program and input, "does this program stop?" (Turing, 1936).

## Worked example 1: the Collatz sequence and finiteness

*n* is given (1 ≤ *n* ≤ 10⁹). Print how many steps the Collatz rule takes to reach 1, and the largest value on the way.

```python
n = int(input())
qadam = 0
eng_katta = n
while n != 1:                   # a loop whose finiteness is unproved!
    if n % 2 == 0:
        n //= 2
    else:
        n = 3 * n + 1
    qadam += 1
    eng_katta = max(eng_katta, n)
print(qadam, eng_katta)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long qadam = 0, engKatta = n;
        while (n != 1) {                // a loop whose finiteness is unproved!
            if (n % 2 == 0) n /= 2;
            else n = 3 * n + 1;
            qadam++;
            engKatta = Math.max(engKatta, n);
        }
        System.out.println(qadam + " " + engKatta);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long qadam = 0, engKatta = n;
    while (n != 1) {                    // a loop whose finiteness is unproved!
        if (n % 2 == 0) n /= 2;
        else n = 3 * n + 1;
        qadam++;
        engKatta = max(engKatta, n);
    }
    cout << qadam << " " << engKatta << endl;
    return 0;
}
```

For `6`: 6 → 3 → 10 → 5 → 16 → 8 → 4 → 2 → 1, the output is `8 16`. For `27` it is `111 9232`: a small number climbs to 9232 and comes down after 111 steps (*qadam* is a step, *eng_katta* the largest). This program is **discrete** (each pass of the loop is one step), **definite** (one rule per step) and **general** (for any *n*). But its **finiteness** is an open problem of mathematics. The values can grow far beyond *n*, which is why Java and C++ use `long`.

## Worked example 2: a Turing machine — adding one to a binary number

A binary number is written on the tape, and the head stands on its **last** digit. The machine adds 1 to the number:

| State | Read | Write | Move | New state |
|---|---|---|---|---|
| `q0` | `1` | `0` | L | `q0` (the carry continues) |
| `q0` | `0` | `1` | — | `stop` |
| `q0` | `_` | `1` | — | `stop` (a new digit at the front) |

A binary number is given. Simulate the machine and print the result and the number of steps taken.

```python
lenta = list(input().strip())
kallak = len(lenta) - 1         # the last digit
holat = "q0"
qadam = 0

# the transition table: (state, symbol) -> (write, move, new state)
jadval = {
    ("q0", "1"): ("0", -1, "q0"),
    ("q0", "0"): ("1", 0, "stop"),
    ("q0", "_"): ("1", 0, "stop"),
}

while holat != "stop":
    if kallak < 0:              # the tape is infinite to the left: add a blank cell
        lenta.insert(0, "_")
        kallak = 0
    yoz, harakat, holat = jadval[(holat, lenta[kallak])]
    lenta[kallak] = yoz
    kallak += harakat
    qadam += 1

print("".join(lenta).strip("_"))
print(qadam)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    record Otish(char yoz, int harakat, String holat) {}

    public static void main(String[] args) {
        StringBuilder lenta = new StringBuilder(new Scanner(System.in).next());
        int kallak = lenta.length() - 1;    // the last digit
        String holat = "q0";
        int qadam = 0;

        // the transition table: state + symbol -> (write, move, new state)
        Map<String, Otish> jadval = new HashMap<>();
        jadval.put("q0 1", new Otish('0', -1, "q0"));
        jadval.put("q0 0", new Otish('1', 0, "stop"));
        jadval.put("q0 _", new Otish('1', 0, "stop"));

        while (!holat.equals("stop")) {
            if (kallak < 0) {               // the tape is infinite to the left: add a blank cell
                lenta.insert(0, '_');
                kallak = 0;
            }
            Otish o = jadval.get(holat + " " + lenta.charAt(kallak));
            lenta.setCharAt(kallak, o.yoz());
            kallak += o.harakat();
            holat = o.holat();
            qadam++;
        }

        System.out.println(lenta.toString().replace("_", ""));
        System.out.println(qadam);
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
#include <tuple>
using namespace std;

int main() {
    string lenta;
    cin >> lenta;
    int kallak = lenta.size() - 1;          // the last digit
    string holat = "q0";
    int qadam = 0;

    // the transition table: (state, symbol) -> (write, move, new state)
    map<pair<string, char>, tuple<char, int, string>> jadval = {
        {{"q0", '1'}, {'0', -1, "q0"}},
        {{"q0", '0'}, {'1', 0, "stop"}},
        {{"q0", '_'}, {'1', 0, "stop"}},
    };

    while (holat != "stop") {
        if (kallak < 0) {                   // the tape is infinite to the left: add a blank cell
            lenta.insert(lenta.begin(), '_');
            kallak = 0;
        }
        auto [yoz, harakat, yangi] = jadval[{holat, lenta[kallak]}];
        lenta[kallak] = yoz;
        kallak += harakat;
        holat = yangi;
        qadam++;
    }

    string natija;
    for (char c : lenta) if (c != '_') natija += c;
    cout << natija << endl << qadam << endl;
    return 0;
}
```

For `1011` (11) the output is `1100` (12) and `3`: the last two `1`s turn into `0` (the carry), and on the third step a `0` becomes `1`. For `111` (7) the result is `1000` (8) in `4` steps: on the last step the head leaves the start of the tape and writes `1` into a blank cell (*lenta* is the tape, *kallak* the head, *holat* the state, *jadval* the table). The machine's program is a table of just three rows, while the simulator works with any table: change the table and you get a different machine. A modern computer is really the same: the processor is the "simulator", the program the "table".

:::warning
**Common mistakes**

- **Calling any list of instructions an algorithm.** Vague steps such as "a little" or "until ready" break definiteness.
- **Not checking finiteness.** If a loop's condition never becomes false, the program hangs; for every loop ask "why does it stop?".
- **Calling the Church–Turing thesis a theorem.** It is unproved, though a universally accepted hypothesis.
- **Thinking a Turing machine is a real device.** It is a mathematical model; the infinite tape is only theoretical.
:::

:::tip
Trace a Turing machine's work on paper: for every step write down the state of the tape and underline the cell under the head. "Running" one machine by hand explains the idea better than dozens of definitions.
:::

## Summary

- The intuitive definition of an algorithm is a sequence of precise instructions; the formal definition a mathematical model; the cybernetic definition a system of commands that moves an object to a target state.
- The properties of an algorithm: discreteness, definiteness, effectiveness, generality, finiteness.
- By structure, algorithms are linear, branching, repetitive and recursive.
- A Turing machine consists of a tape, a head, finitely many states and a transition table; it can do whatever any computer can do.
- The Church–Turing thesis: intuitively computable = computable by a Turing machine; the halting problem cannot be solved by an algorithm.

## Check yourself

:::check
Q: Whose name does the word "algorithm" come from?
A: From *Algoritmi*, the Latin form of the name of the 9th-century scholar Muhammad ibn Musa al-Khwarizmi.
Q: Which property does the instruction "bake the pie until it is ready" break?
A: Definiteness (and finiteness too): the state "ready" is not precisely defined.
Q: What does one row of a Turing machine's transition table mean?
A: For a pair (current state, symbol read): which symbol is written, where the head moves, and which state the machine goes to.
Q: How many steps does the add-one machine take for `1111`?
A: 5: it turns four `1`s into `0`s and on the fifth step writes `1` into a blank cell.
Q: Why can the Church–Turing thesis not be proved?
A: It involves the notion of an "intuitive algorithm", which is not mathematically precisely defined.
:::

:::link https://plato.stanford.edu/entries/turing-machine/
Stanford Encyclopedia of Philosophy: Turing machines
The definition, examples and history of the Turing machine.
:::

:::link https://en.wikipedia.org/wiki/Collatz_conjecture
The Collatz conjecture
About the famous problem that is still unproved.
:::
