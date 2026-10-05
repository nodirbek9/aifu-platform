---
title: Building a risk matrix and assessing risks (a practical example)
summary: Assessing risks for a small organisation — a list of assets and threats, criteria for the likelihood and impact scales, expert assessment, the risk matrix and heat map, choosing thresholds, risk owners, controls and residual risk; the change between two assessments; a sample risk register.
---

## Why it matters

In lecture M2 we saw the theory of the risk matrix. In practice the hardest part is not applying the formula but **assessing likelihood and impact consistently**: why "4" and not "3"? So that different specialists do not rate the same risk differently, precise criteria are written for the scales. In this session we carry out a full assessment for a small organisation step by step and show the result on a heat map.

:::note
**After this session you will be able to:**

- write precise criteria for likelihood and impact scales;
- draw up a list of risks for an organisation and assess them consistently;
- show the result on a heat map;
- set an owner, a control and a residual level for each risk;
- compare the results of two assessments and analyse how the risks changed.
:::

## Criteria for the scales

**Likelihood**:

| Score | Level | Criterion |
|---|---|---|
| 1 | Rare | less than once in 10 years |
| 2 | Unlikely | once in 3–10 years |
| 3 | Possible | once in 1–3 years |
| 4 | Likely | several times a year |
| 5 | Almost certain | once a month or more |

**Impact**:

| Score | Level | Financial | Operational | Reputation and law |
|---|---|---|---|---|
| 1 | Negligible | < 1 million soʻm | an outage of a few minutes | no effect |
| 2 | Minor | 1–10 million | a few hours | internal complaints |
| 3 | Moderate | 10–100 million | 1 day | local media |
| 4 | Major | 100 million – 1 billion | several days | a fine, national media |
| 5 | Catastrophic | > 1 billion | more than a week | loss of licence, criminal liability |

If impact is rated in several columns, the **highest** score is taken.

## Practical work: the "Bilim" training centre

The organisation: 40 staff, 600 students, one server (the student database and accounting), Wi-Fi, an online payment page.

1. **Assets**: the student database, accounting, the website and payment page, staff laptops, the server room, reputation.
2. **Threat–vulnerability pairs** (each one a separate risk): phishing (staff not trained), ransomware (no backups), laptop theft (disks not encrypted), DDoS (no protection), an insider (broad rights), fire (no fire suppression in the server room), simple passwords (no policy), a CCTV failure.
3. **Assessment**: each risk gets a likelihood and an impact by the criteria; two or three staff assess independently and the results are discussed.
4. **Matrix and map** — with the program below.
5. **Controls and owners**: for critical and high risks — a plan with deadlines and a responsible person.
6. **Residual risk**: the expected likelihood and impact after the control.

A program that categorises risks and orders them by level is this session's independent task (KIA-A2).

## Worked example 1: a heat map

The first line holds the number of risks, then each risk: its name, likelihood and impact (1–5). Print the 5×5 map: rows are likelihood from 5 down to 1, columns are impact from 1 to 5, each cell holds the number of risks in it (`.` — no risk).

```python
n = int(input())
xarita = [[0] * 6 for _ in range(6)]           # [likelihood][impact]
for _ in range(n):
    nom, e, t = input().split()
    xarita[int(e)][int(t)] += 1
print("  | 1 2 3 4 5")
for e in range(5, 0, -1):                       # the most likely at the top
    kataklar = [str(xarita[e][t]) if xarita[e][t] else "." for t in range(1, 6)]
    print(str(e) + " | " + " ".join(kataklar))
print("total: " + str(n))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] xarita = new int[6][6];                      // [likelihood][impact]
        for (int i = 0; i < n; i++) {
            in.next();
            int e = in.nextInt();
            int t = in.nextInt();
            xarita[e][t]++;
        }
        System.out.println("  | 1 2 3 4 5");
        for (int e = 5; e >= 1; e--) {                       // the most likely at the top
            StringBuilder qator = new StringBuilder(e + " |");
            for (int t = 1; t <= 5; t++) qator.append(" ").append(xarita[e][t] > 0 ? String.valueOf(xarita[e][t]) : ".");
            System.out.println(qator);
        }
        System.out.println("total: " + n);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    int xarita[6][6] = {};                                    // [likelihood][impact]
    for (int i = 0; i < n; i++) {
        string nom;
        int e, t;
        cin >> nom >> e >> t;
        xarita[e][t]++;
    }
    cout << "  | 1 2 3 4 5" << endl;
    for (int e = 5; e >= 1; e--) {                            // the most likely at the top
        string qator = to_string(e) + " |";
        for (int t = 1; t <= 5; t++) qator += " " + (xarita[e][t] > 0 ? to_string(xarita[e][t]) : string("."));
        cout << qator << endl;
    }
    cout << "total: " << n << endl;
    return 0;
}
```

Input (the risk names are in Uzbek: phishing, ransomware, laptop, DDoS, insider, fire, simple password, camera):

```text
8
Fishing 4 3
Shifrlovchi 3 5
Noutbuk 3 2
DDoS 2 4
Insayder 2 5
Yongin 1 5
Oddiy_parol 4 3
Kamera 1 1
```

Output:

```text
  | 1 2 3 4 5
5 | . . . . .
4 | . . 2 . .
3 | . 1 . . 1
2 | . . . 1 1
1 | 1 . . . 1
total: 8
```

The cells near the top right corner of the map are the most dangerous. Here two risks (phishing and simple passwords) share a cell — both can be reduced with one measure (staff training + MFA). "Fire" in the bottom right corner is unlikely but catastrophic — such risks must be considered separately by impact, not only by score.

## Worked example 2: the change between two assessments

After controls were applied, the risks were assessed again. Each line holds: a name, the earlier likelihood and impact, the new likelihood and impact. For each risk print the change of level (`decreased`, `increased`, `unchanged`) and the total of the levels.

```python
import sys

oldin_jami = keyin_jami = 0
for satr in sys.stdin.read().strip().split("\n")[1:]:
    nom, e1, t1, e2, t2 = satr.split()
    oldin = int(e1) * int(t1)
    keyin = int(e2) * int(t2)
    oldin_jami += oldin
    keyin_jami += keyin
    holat = "decreased" if keyin < oldin else "increased" if keyin > oldin else "unchanged"
    print(nom + ": " + str(oldin) + " -> " + str(keyin) + " " + holat)
print("total: " + str(oldin_jami) + " -> " + str(keyin_jami))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int oldinJami = 0, keyinJami = 0;
        for (int i = 0; i < n; i++) {
            String nom = in.next();
            int oldin = in.nextInt() * in.nextInt();
            int keyin = in.nextInt() * in.nextInt();
            oldinJami += oldin;
            keyinJami += keyin;
            String holat = keyin < oldin ? "decreased" : keyin > oldin ? "increased" : "unchanged";
            System.out.println(nom + ": " + oldin + " -> " + keyin + " " + holat);
        }
        System.out.println("total: " + oldinJami + " -> " + keyinJami);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    int oldinJami = 0, keyinJami = 0;
    for (int i = 0; i < n; i++) {
        string nom;
        int e1, t1, e2, t2;
        cin >> nom >> e1 >> t1 >> e2 >> t2;
        int oldin = e1 * t1;
        int keyin = e2 * t2;
        oldinJami += oldin;
        keyinJami += keyin;
        string holat = keyin < oldin ? "decreased" : keyin > oldin ? "increased" : "unchanged";
        cout << nom << ": " << oldin << " -> " << keyin << " " << holat << endl;
    }
    cout << "total: " << oldinJami << " -> " << keyinJami << endl;
    return 0;
}
```

Input:

```text
4
Fishing 4 3 2 3
Shifrlovchi 3 5 3 2
DDoS 2 4 3 4
Kamera 1 1 1 1
```

Output:

```text
Fishing: 12 -> 6 decreased
Shifrlovchi: 15 -> 6 decreased
DDoS: 8 -> 12 increased
Kamera: 1 -> 1 unchanged
total: 36 -> 25
```

Training reduced the **likelihood** of phishing, while backups reduced the **impact** of ransomware — controls act on different axes of the matrix. The DDoS risk grew: the organisation expanded online payments, and now a website outage causes more harm — risks change not only through controls but also through changes in the business.

:::warning
**Common mistakes**

- **Assessing without criteria.** "I think it's a 4" — different people give different results; write a precise criterion for the scale.
- **Assessing only the threat, not the threat–vulnerability pair.** "Viruses" is too general; "ransomware on a server without backups" is a risk that can be assessed.
- **Ignoring unlikely but catastrophic risks.** Fire or flood is rare but can destroy an organisation.
- **Not reassessing after a control.** The effect of a control can be known only by measuring the residual risk.
:::

:::tip
Assess 6–8 digital risks for your family or dormitory (losing a phone, a password leak, a stranger joining the Wi-Fi and so on) with the criteria above and draw a heat map. Write one cheap measure for each of the two highest risks.
:::

## Summary

- Precise, measurable criteria are written for the likelihood and impact scales; impact is rated in several columns and the highest is taken.
- A risk is a threat–vulnerability pair; each is assessed separately.
- A heat map shows how risks are distributed and helps find groups closed by one measure.
- Controls reduce likelihood or impact; the change in risks is tracked by reassessment.

## Check yourself

:::check
Q: Why are precise criteria (for example, "several times a year") written for the likelihood scale?
A: So that different assessors give the same result and the assessment is consistent and repeatable.
Q: If the financial impact is 2 and the reputational impact is 4, which score is taken?
A: 4 — the highest score.
Q: Which component of the ransomware risk do backups reduce?
A: The impact — data is restored and the outage is shorter.
Q: Where on a heat map are the most dangerous risks?
A: In the top right corner — high likelihood and high impact.
Q: Why can a risk level grow even with no change in controls?
A: The business or environment changes: a new service, a new threat, a higher asset value.
:::

:::link https://www.iso.org/standard/80585.html
ISO/IEC 27005
The standard giving guidance on information security risk management.
:::
