---
title: Programming a branching computational process
summary: Independent study — comparison and logical operators (AND, OR, NOT) and the truth table; if, if-else, the if-elif-else chain, nested conditions, match/switch; the order of conditions; validating input data; testing with boundary values; turning the decision table from MT11 into a program — a decision on a loan application; syntax differences in Python, Java and C++; common mistakes.
---

## Purpose of this independent study

In S8 we drew the flowchart of a branching process, and in MT11 we built a decision table. Now we study writing branching **in a program** in more depth: complex conditions, their order and how to check that the program works correctly.

:::note
**After this independent study you will be able to:**

- use comparison and logical operators;
- write if-else chains and nested conditions;
- turn a decision table into a program;
- build tests with boundary values.
:::

## Comparison and logical operators

| Operation | Python | Java, C++ | Meaning |
|---|---|---|---|
| equal | `==` | `==` | `a == b` |
| not equal | `!=` | `!=` | |
| greater, less | `>`, `<` | `>`, `<` | |
| greater or equal, less or equal | `>=`, `<=` | `>=`, `<=` | |
| AND | `and` | `&&` | both conditions are true |
| OR | `or` | `\|\|` | at least one is true |
| NOT | `not` | `!` | inverts the condition |

**The truth table**:

| A | B | A AND B | A OR B | NOT A |
|---|---|---|---|---|
| true | true | true | true | false |
| true | false | false | true | false |
| false | true | false | true | true |
| false | false | false | false | true |

**Short-circuit evaluation**: in `A AND B`, if A is false, B is not evaluated. This can be used to guard risky operations: `income > 0 and payment / income < 0.5` — no division by zero happens.

## Forms of branching

| Form | When | Python |
|---|---|---|
| **Incomplete** (if) | an extra action if the condition is true | `if balance < 0: warn()` |
| **Complete** (if-else) | one of two paths | `if ... : A else: B` |
| **Chain** (if-elif-else) | many mutually exclusive cases | `if ...: elif ...: else:` |
| **Nested** | a condition inside a condition | `if a: if b: ...` |
| **Selection** (match / switch) | several exact values of one variable | account type: "current", "deposit", "loan" |

**Order matters in a chain**: conditions are checked from top to bottom, and after the first true condition the rest are not checked. So the narrowest (strictest) conditions are written first.

## Worked example: a decision on a loan application

The decision table from MT11: if the scoring score is ≥ 600 and the monthly payment is ≤ 50% of income — approve; the score is sufficient but the burden is high — offer a smaller amount; the score is low but the burden is acceptable — review with a guarantor; otherwise — reject. If income is zero or negative — an input error. Each line holds: surname, score, monthly income, monthly payment.

```python
import sys

MIN_BALL = 600        # scoring threshold
MAX_ULUSH = 50        # the payment must not exceed this percentage of income

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    ism, ball, daromad, tolov = satr.split()
    ball, daromad, tolov = int(ball), int(daromad), int(tolov)
    if daromad <= 0:                                   # check the input data first
        qaror = "error — invalid income"
    else:
        yuk_mos = tolov * 100 <= daromad * MAX_ULUSH   # compare in integers
        if ball >= MIN_BALL and yuk_mos:
            qaror = "approve"
        elif ball >= MIN_BALL:
            qaror = "offer a smaller amount"
        elif yuk_mos:
            qaror = "review with a guarantor"
        else:
            qaror = "reject"
    print(f"{ism}: {qaror}")
```
```java
import java.util.Scanner;

public class Main {
    static final int MIN_BALL = 600;    // scoring threshold
    static final int MAX_ULUSH = 50;    // the payment must not exceed this percentage of income

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String ism = in.next();
            long ball = in.nextLong(), daromad = in.nextLong(), tolov = in.nextLong();
            String qaror;
            if (daromad <= 0) {                                    // check the input data first
                qaror = "error — invalid income";
            } else {
                boolean yukMos = tolov * 100 <= daromad * MAX_ULUSH;   // compare in integers
                if (ball >= MIN_BALL && yukMos) qaror = "approve";
                else if (ball >= MIN_BALL) qaror = "offer a smaller amount";
                else if (yukMos) qaror = "review with a guarantor";
                else qaror = "reject";
            }
            System.out.println(ism + ": " + qaror);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const int MIN_BALL = 600;    // scoring threshold
const int MAX_ULUSH = 50;    // the payment must not exceed this percentage of income

int main() {
    string ism;
    long long ball, daromad, tolov;
    while (cin >> ism >> ball >> daromad >> tolov) {
        string qaror;
        if (daromad <= 0) {                                       // check the input data first
            qaror = "error — invalid income";
        } else {
            bool yukMos = tolov * 100 <= daromad * MAX_ULUSH;     // compare in integers
            if (ball >= MIN_BALL && yukMos) qaror = "approve";
            else if (ball >= MIN_BALL) qaror = "offer a smaller amount";
            else if (yukMos) qaror = "review with a guarantor";
            else qaror = "reject";
        }
        cout << ism << ": " << qaror << "\n";
    }
    return 0;
}
```

Input:

```text
Aliyev 720 8000000 3000000
Karimova 650 6000000 3500000
Tursunov 580 10000000 2000000
Rahimov 540 4000000 2500000
Usmonova 600 7000000 3500000
Nazarov 610 0 1000000
```

Output:

```text
Aliyev: approve
Karimova: offer a smaller amount
Tursunov: review with a guarantor
Rahimov: reject
Usmonova: approve
Nazarov: error — invalid income
```

Note:

- **Usmonova** is a boundary case: the score is exactly 600 and the payment exactly 50%. Thanks to `>=` and `<=` she is approved. Had `>` been written, she would have been wrongly rejected — that is why tests must always include boundary values.
- **Nazarov** — income is 0: because the check comes first, the program does not "crash" with a division by zero or a meaningless decision.
- The share is compared **in integers** as `payment * 100 <= income * 50` — to avoid floating-point errors (MT12).
- When the line `elif score >= MIN_SCORE` is reached, the burden check is necessarily false — the previous condition has already "filtered" it out. The order of the chain is part of the logic.

## Testing with boundary values

For each condition three values are tested: slightly below the boundary, exactly at the boundary, slightly above it. For the score: 599, 600, 601. For the payment share: 49%, 50%, 51%. In addition — invalid input: 0, a negative number, a very large number. Every column of the four-rule decision table must be covered by at least one test.

| Test | Score | Share | Expected decision |
|---|---|---|---|
| 1 | 600 | 50% | approve |
| 2 | 599 | 50% | review with a guarantor |
| 3 | 600 | 51% | offer a smaller amount |
| 4 | 599 | 51% | reject |

:::warning
**Common mistakes**

- **Confusing `=` and `==`.** `=` assigns a value, `==` compares.
- **The wrong order of conditions.** If a broad condition comes first, the narrow ones never get their turn.
- **Overlapping ranges or gaps.** For example, `< 600` and `> 600` — exactly 600 falls into no branch.
- **Comparing floating-point numbers with `==`.** Use integers or an allowed tolerance.
- **Not validating input data.** A zero, empty or negative value breaks the program.
- **Several lines without braces in Java and C++.** Without `{ }` after `if`, only the first line belongs to the condition.
:::

## Assignments

1. Test the example program with the 4 boundary tests above (income 10,000,000, payment 5,000,000 or 5,100,000).
2. Add a new rule to the program: if the score is above 800, a payment share of up to 60% is allowed. Where in the chain should the rule go? Why?
3. Turn the "salary bonus" decision table you built in MT11 into a program in Python or another language.
4. Write a program that outputs the service fee by account type ("current", "deposit", "loan") using `match` (Python 3.10+) or `switch` (Java, C++).

## Summary

- Comparison and logical operators (AND, OR, NOT) build conditions; short-circuit evaluation guards risky operations.
- Forms: if, if-else, the if-elif-else chain, nested conditions, match/switch; order matters in a chain.
- A decision table translates directly into a program; input data is validated first.
- Testing: for every boundary, a value below, exactly at and above it, plus invalid input.

## Check yourself

:::check
Q: What is short-circuit evaluation?
A: In an AND operation, if the first condition is false (or in OR, if it is true), the second is not evaluated.
Q: Why does order matter in an if-elif-else chain?
A: Conditions are checked from top to bottom, and after the first true one the rest are not executed.
Q: Which boundary values are tested for the condition "score ≥ 600"?
A: 599, 600 and 601.
Q: Why is the share compared in integers in the example?
A: So that a boundary case is not misjudged because of floating-point errors.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html
Python: More Control Flow Tools
The official guide to if, match and other control structures.
:::
