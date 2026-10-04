---
title: The algorithm and its definitions. The Turing machine (practical session)
summary: Checking the properties of an algorithm in practice, building a table of steps, the "Russian peasant" multiplication method; a universal Turing machine simulator — entering a transition table, a unary addition machine, catching a non-halting machine with a step limit.
---

## Why it matters

In the lecture we met the properties of an algorithm and the Turing machine in theory. In practice we answer two questions: are the given instructions really an algorithm, and how can they be written in "machine language" — with precise steps and states?

In this session we check an ancient multiplication algorithm with a table of steps and write a **universal** Turing machine simulator: it reads a transition table from the input and runs any machine. Along the way we see the practical side of finiteness: how a machine that never stops can be "caught".

:::note
**After this session you will be able to:**

- check whether given instructions satisfy the properties of an algorithm;
- trace an algorithm's execution with a table of steps;
- build a Turing machine's transition table and try it in a simulator;
- detect a non-terminating process with a step limit.
:::

## How to check an algorithm

Before calling any list of instructions an algorithm, five questions are answered:

| Property | The question to check |
|---|---|
| Discreteness | is the process split into separate steps? |
| Definiteness | at every step, does the executor know unambiguously what to do? |
| Effectiveness | does the process end with the required result? |
| Generality | are the instructions for one input only, or for a whole class? |
| Finiteness | does the process stop for every valid input, and why? |

The usual way to show finiteness is to find a quantity that **strictly decreases** at every step and is bounded from below. For example, if *a* halves at every step and the condition is *a* > 0, the number of steps does not exceed log₂ *a*.

## Writing a Turing machine

A machine is given by a **transition table**. In the simulator every rule is one line:

```text
state symbol_read symbol_to_write move new_state
```

The move: `L` — left, `R` — right, `N` — stay. A blank cell is `_`. The machine stops when it reaches the `stop` state or when the table has no matching rule.

Since the tape is infinite, the simulator keeps it as a **dictionary** (position → symbol): unwritten cells automatically count as blank. The halting problem cannot be solved by an algorithm, so the simulator sets a **step limit**: a machine that has not stopped after 10,000 steps is reported as "did not halt". This is not a proof, but practical protection.

## Worked example 1: the "Russian peasant" multiplication method

An algorithm known since ancient times: to multiply *a* by *b*, keep halving *a* (dropping the remainder) and doubling *b*; add up the values of *b* in the rows where *a* is odd. *a* and *b* are given (1 ≤ *a*, *b* ≤ 10⁶). Print the product and the number of steps.

```python
a, b = map(int, input().split())
kopaytma = 0
qadam = 0
while a > 0:                    # a decreases at every step — finiteness
    qadam += 1
    if a % 2 == 1:
        kopaytma += b
    a //= 2
    b *= 2
print(kopaytma, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long a = in.nextLong(), b = in.nextLong();
        long kopaytma = 0, qadam = 0;
        while (a > 0) {                 // a decreases at every step — finiteness
            qadam++;
            if (a % 2 == 1) kopaytma += b;
            a /= 2;
            b *= 2;
        }
        System.out.println(kopaytma + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    long long kopaytma = 0, qadam = 0;
    while (a > 0) {                     // a decreases at every step — finiteness
        qadam++;
        if (a % 2 == 1) kopaytma += b;
        a /= 2;
        b *= 2;
    }
    cout << kopaytma << " " << qadam << endl;
    return 0;
}
```

The table of steps for `13 21`:

| *a* | *b* | Is *a* odd? | Sum |
|---|---|---|---|
| 13 | 21 | yes | 21 |
| 6 | 42 | no | 21 |
| 3 | 84 | yes | 105 |
| 1 | 168 | yes | 273 |

The output is `273 4`. Checking the properties: **discrete** (each row is a step), **definite** (one rule per step), **general** (any natural *a*, *b*), **effective** (13 · 21 = 273), **finite** (*a* at least halves at every step, so there are ⌊log₂ *a*⌋ + 1 steps). The algorithm is really writing *a* in binary: 13 = 1101₂, that is, 13 · 21 = 21 + 4 · 21 + 8 · 21 (*kopaytma* is the product, *qadam* a step). Computer processors multiply in a similar way.

## Worked example 2: a universal Turing machine simulator

The first line holds the number of rules *k*, the next *k* lines the rules (`state symbol write move new`), then the start state and the tape. The head stands on the first symbol of the tape. Run the machine: if it stops, print what is on the tape (without blank edges; `_` if the tape is empty) and the number of steps; if it has not stopped after 10,000 steps, print `did not halt`.

```python
k = int(input())
jadval = {}
for _ in range(k):
    holat, belgi, yoz, harakat, yangi = input().split()
    jadval[(holat, belgi)] = (yoz, harakat, yangi)
holat = input().strip()
lenta = dict(enumerate(input().strip()))    # position -> symbol
kallak = 0
qadam = 0
SURILISH = {"L": -1, "R": 1, "N": 0}

while holat != "stop" and qadam < 10000:
    belgi = lenta.get(kallak, "_")
    if (holat, belgi) not in jadval:        # no matching rule — stop
        break
    yoz, harakat, holat = jadval[(holat, belgi)]
    lenta[kallak] = yoz
    kallak += SURILISH[harakat]
    qadam += 1

if holat != "stop" and qadam >= 10000:
    print("did not halt")
else:
    band = [i for i in lenta if lenta[i] != "_"]
    if band:
        print("".join(lenta.get(i, "_") for i in range(min(band), max(band) + 1)))
    else:
        print("_")
    print(qadam)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        Map<String, String[]> jadval = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String holat = in.next(), belgi = in.next();
            jadval.put(holat + " " + belgi, new String[] {in.next(), in.next(), in.next()});
        }
        String holat = in.next();
        String kirish = in.next();
        TreeMap<Integer, Character> lenta = new TreeMap<>();   // position -> symbol
        for (int i = 0; i < kirish.length(); i++) lenta.put(i, kirish.charAt(i));

        int kallak = 0, qadam = 0;
        while (!holat.equals("stop") && qadam < 10000) {
            char belgi = lenta.getOrDefault(kallak, '_');
            String[] q = jadval.get(holat + " " + belgi);
            if (q == null) break;                               // no matching rule — stop
            lenta.put(kallak, q[0].charAt(0));
            kallak += q[1].equals("L") ? -1 : q[1].equals("R") ? 1 : 0;
            holat = q[2];
            qadam++;
        }

        if (!holat.equals("stop") && qadam >= 10000) {
            System.out.println("did not halt");
            return;
        }
        Integer bosh = null, oxir = null;
        for (Map.Entry<Integer, Character> e : lenta.entrySet()) {
            if (e.getValue() != '_') {
                if (bosh == null) bosh = e.getKey();
                oxir = e.getKey();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (bosh == null) sb.append('_');
        else for (int i = bosh; i <= oxir; i++) sb.append(lenta.getOrDefault(i, '_'));
        System.out.println(sb);
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
    int k;
    cin >> k;
    map<pair<string, char>, tuple<char, char, string>> jadval;
    for (int i = 0; i < k; i++) {
        string holat, yangi;
        char belgi, yoz, harakat;
        cin >> holat >> belgi >> yoz >> harakat >> yangi;
        jadval[{holat, belgi}] = {yoz, harakat, yangi};
    }
    string holat, kirish;
    cin >> holat >> kirish;
    map<int, char> lenta;                               // position -> symbol
    for (int i = 0; i < (int) kirish.size(); i++) lenta[i] = kirish[i];

    int kallak = 0, qadam = 0;
    while (holat != "stop" && qadam < 10000) {
        char belgi = lenta.count(kallak) ? lenta[kallak] : '_';
        auto it = jadval.find({holat, belgi});
        if (it == jadval.end()) break;                  // no matching rule — stop
        auto [yoz, harakat, yangi] = it->second;
        lenta[kallak] = yoz;
        kallak += harakat == 'L' ? -1 : harakat == 'R' ? 1 : 0;
        holat = yangi;
        qadam++;
    }

    if (holat != "stop" && qadam >= 10000) {
        cout << "did not halt" << endl;
        return 0;
    }
    int bosh = 0, oxir = -1;
    bool bor = false;
    for (auto& [pozitsiya, belgi] : lenta) {
        if (belgi != '_') {
            if (!bor) bosh = pozitsiya;
            oxir = pozitsiya;
            bor = true;
        }
    }
    string natija;
    if (!bor) natija = "_";
    for (int i = bosh; bor && i <= oxir; i++) natija += lenta.count(i) ? lenta[i] : '_';
    cout << natija << endl << qadam << endl;
    return 0;
}
```

**A unary addition machine.** In the unary system the number *n* is written as *n* `1`s: 3 + 2 is `111+11`. The machine replaces `+` with `1` and erases the last `1`:

```text
5
q0 1 1 R q0
q0 + 1 R q1
q1 1 1 R q1
q1 _ _ L q2
q2 1 _ N stop
q0
111+11
```

The output is `11111` and `8` — 3 + 2 = 5. The machine first walks right (`q0`, `q1`), and on reaching the end steps back one cell (`q2`) and erases the extra `1`.

**A machine that never stops.** With the following table the machine reaches the end of the tape, steps back one cell and walks forward again — forever:

```text
3
q0 1 1 R q0
q0 _ _ L q1
q1 1 1 R q0
q0
11
```

The output is `did not halt`. The simulator did not prove that the machine **never** stops — it only gave up waiting after 10,000 steps. That is the essence of the halting problem: in general, no algorithm can answer the question "should we wait longer?" (*jadval* is the table, *lenta* the tape, *kallak* the head, *holat* the state).

:::warning
**Common mistakes**

- **Making the tape an ordinary array.** If the head moves off to the left, a negative index causes an error; use a dictionary or a structure that grows in both directions.
- **A simulator without a step limit.** A machine that never stops hangs the program.
- **Two rules for the same (state, symbol) pair in the table.** This breaks definiteness — the machine stops being deterministic.
- **Justifying finiteness with "I tried it a few times and it stopped".** The proof is given through a decreasing quantity.
:::

:::tip
Before giving your own machine to the simulator, run 3–4 steps of it on paper: at every step write down the tape, the head's position and the state. A mistake in the table is most often found at this stage.
:::

## Summary

- For instructions to be an algorithm, each of the five properties is checked; finiteness is justified by a decreasing quantity.
- A table of steps shows how an algorithm works and helps check its result.
- A universal simulator takes the transition table as input — change the table and the machine changes.
- An infinite tape is modelled with a dictionary; a machine that never stops is detected in practice with a step limit.

## Check yourself

:::check
Q: How many steps does the "Russian peasant" method take for *a* = 100?
A: 7: 100 → 50 → 25 → 12 → 6 → 3 → 1 → 0 (⌊log₂ 100⌋ + 1 = 7).
Q: Why is the tape kept as a dictionary in the simulator?
A: The tape is infinite in both directions; a dictionary holds any (even negative) position and lets unwritten cells count as blank.
Q: What are the result and the number of steps of the unary addition machine for `1+1`?
A: `11` and 5 steps.
Q: If the simulator says "did not halt", does that prove the machine never stops?
A: No — it only means the machine did not stop within the set number of steps.
:::

:::link https://turingmachine.io/
Turing machine visualization
A tool for watching Turing machines step by step in the browser.
:::
