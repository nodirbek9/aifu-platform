---
title: Risk management. Risk level and frequency, the risk matrix, Risk Management Information Systems (RMIS)
summary: The risk management process — identifying assets, analysing threats and vulnerabilities, assessment, treatment, monitoring; qualitative and quantitative assessment; the risk matrix (likelihood × impact); quantitative measures — AV, EF, SLE, ARO, ALE; the economic benefit of a control; risk treatment strategies; the risk register and RMIS; ISO/IEC 27005 and NIST SP 800-30.
---

## Why it matters

No organisation can protect itself fully against every threat: money, time and specialists are limited. So the key question is "what must be protected first, and how much is it sensible to spend on it?". For example, buying a 50-million system against the risk of losing a 10-million laptop is a mistake. **Risk management** is the way of making these decisions by calculation rather than guesswork.

:::note
**After this lesson you will be able to:**

- explain the stages of the risk management process;
- tell qualitative (risk matrix) and quantitative (ALE) assessment apart;
- calculate SLE, ARO and ALE and determine the economic benefit of a control;
- choose among the four risk treatment strategies;
- explain the purpose of a risk register and an RMIS.
:::

## The risk management process

1. **Establish the context** — the organisation's goals, legal requirements, the risk appetite.
2. **Identify assets** — what is valuable and how much it is worth (a database, servers, reputation).
3. **Identify threats and vulnerabilities** — who or what could cause harm, and through which weaknesses.
4. **Analyse and evaluate risk** — determine likelihood and impact, rank the risks.
5. **Treat the risk** — choose and implement controls.
6. **Monitor and review** — risks change: a new system, a new threat, a new law.
7. **Communicate** — keep management and staff informed.

Standards: **ISO/IEC 27005** (information security risk management), **ISO 31000** (general risk management), **NIST SP 800-30** (a guide to risk assessment).

## Qualitative assessment: the risk matrix

Likelihood and impact are rated on a 1–5 scale; **risk level = likelihood × impact**.

| Likelihood \ Impact | 1 negligible | 2 minor | 3 moderate | 4 major | 5 catastrophic |
|---|---|---|---|---|---|
| **5 almost certain** | 5 | 10 | 15 | 20 | 25 |
| **4 likely** | 4 | 8 | 12 | 16 | 20 |
| **3 possible** | 3 | 6 | 9 | 12 | 15 |
| **2 unlikely** | 2 | 4 | 6 | 8 | 10 |
| **1 rare** | 1 | 2 | 3 | 4 | 5 |

The cells are coloured (green — low, yellow — medium, orange — high, red — critical); the organisation sets the boundaries in its risk policy. The advantage — quick and clear; the drawback — subjective, not measured in money. Building a risk matrix and ranking risks is the task of practical session A2.

## Quantitative assessment

| Measure | Formula | Meaning |
|---|---|---|
| **AV** (Asset Value) | — | the value of the asset |
| **EF** (Exposure Factor) | 0–100% | what share of the asset is lost in one incident |
| **SLE** (Single Loss Expectancy) | AV × EF | the expected loss from one incident |
| **ARO** (Annualized Rate of Occurrence) | — | how many times a year it is expected to happen (0.1 — once in 10 years) |
| **ALE** (Annualized Loss Expectancy) | SLE × ARO | the expected annual loss |

**The benefit of a control**: benefit = ALE (before the control) − ALE (after the control) − the annual cost of the control. If the benefit is positive, the control pays for itself.

## Risk treatment strategies

| Strategy | Content | Example |
|---|---|---|
| **Mitigate** | take measures to reduce likelihood or impact | antivirus, backups, training |
| **Transfer** | shift the financial consequence to someone else | cyber insurance, outsourcing |
| **Avoid** | give up the activity that creates the risk | turning off an old risky service |
| **Accept** | the risk is within limits — do nothing, but document it | loss of cheap equipment |

The risk that remains after controls is the **residual risk**. Management accepts it formally.

## The risk register and RMIS

A **risk register** is a table of all risks: an identifier, description, owner (the person responsible), likelihood, impact, level, controls, status, the next review date.

An **RMIS** (Risk Management Information System) is software for keeping the risk register, recording incidents, reporting and visualisation (heat maps, trends). It collects data from different departments, sends reminders to risk owners and prepares dashboards for management. Examples: GRC platforms (ServiceNow GRC, RSA Archer); in small organisations — spreadsheets and simple web applications.

## Worked example: annual loss and the benefit of a control

The first line holds the number of risks *n*. The following lines hold: a name, the asset value AV (thousand soʻm), EF (%), the expected number of incidents in 10 years (before and after the control) and the annual cost of the control. For each risk print SLE, ALE (before and after), the benefit and the recommendation; at the end print the total annual loss after the recommended controls are adopted, and the cost of the controls.

```python
n = int(input())
jami_oldin = jami_keyin = choralar = 0.0
for _ in range(n):
    nom, av, ef, aro10, aro10_keyin, narx = input().split()
    sle = int(av) * int(ef) / 100                   # loss from one incident
    ale = sle * int(aro10) / 10                      # annual expected loss
    ale_keyin = sle * int(aro10_keyin) / 10
    foyda = ale - ale_keyin - int(narx)
    tavsiya = "adopt" if foyda > 0 else "reject"
    print(f"{nom}: SLE={sle:.1f} ALE={ale:.1f} -> {ale_keyin:.1f} benefit={foyda:.1f} {tavsiya}")
    jami_oldin += ale
    if foyda > 0:
        jami_keyin += ale_keyin
        choralar += int(narx)
    else:
        jami_keyin += ale                            # the control was not adopted
print(f"total annual loss: {jami_oldin:.1f} -> {jami_keyin:.1f}, cost of controls {choralar:.1f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        double jamiOldin = 0, jamiKeyin = 0, choralar = 0;
        for (int i = 0; i < n; i++) {
            String nom = in.next();
            long av = in.nextLong();
            int ef = in.nextInt();
            int aro10 = in.nextInt();
            int aro10Keyin = in.nextInt();
            long narx = in.nextLong();
            double sle = av * ef / 100.0;                         // loss from one incident
            double ale = sle * aro10 / 10;                        // annual expected loss
            double aleKeyin = sle * aro10Keyin / 10;
            double foyda = ale - aleKeyin - narx;
            String tavsiya = foyda > 0 ? "adopt" : "reject";
            System.out.println(String.format("%s: SLE=%.1f ALE=%.1f -> %.1f benefit=%.1f %s", nom, sle, ale, aleKeyin, foyda, tavsiya));
            jamiOldin += ale;
            if (foyda > 0) {
                jamiKeyin += aleKeyin;
                choralar += narx;
            } else {
                jamiKeyin += ale;                                 // the control was not adopted
            }
        }
        System.out.println(String.format("total annual loss: %.1f -> %.1f, cost of controls %.1f", jamiOldin, jamiKeyin, choralar));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    double jamiOldin = 0, jamiKeyin = 0, choralar = 0;
    for (int i = 0; i < n; i++) {
        string nom;
        long long av, narx;
        int ef, aro10, aro10Keyin;
        cin >> nom >> av >> ef >> aro10 >> aro10Keyin >> narx;
        double sle = av * ef / 100.0;                             // loss from one incident
        double ale = sle * aro10 / 10;                            // annual expected loss
        double aleKeyin = sle * aro10Keyin / 10;
        double foyda = ale - aleKeyin - narx;
        string tavsiya = foyda > 0 ? "adopt" : "reject";
        printf("%s: SLE=%.1f ALE=%.1f -> %.1f benefit=%.1f %s\n", nom.c_str(), sle, ale, aleKeyin, foyda, tavsiya.c_str());
        jamiOldin += ale;
        if (foyda > 0) {
            jamiKeyin += aleKeyin;
            choralar += narx;
        } else {
            jamiKeyin += ale;                                     // the control was not adopted
        }
    }
    printf("total annual loss: %.1f -> %.1f, cost of controls %.1f\n", jamiOldin, jamiKeyin, choralar);
    return 0;
}
```

Input (amounts in thousand soʻm; the risk names are in Uzbek: server ransomware, laptop theft, website DDoS, phishing):

```text
4
Server_shifrlovchi 200000 50 3 1 15000
Noutbuk_ogirligi 12000 100 20 15 8000
Sayt_DDoS 50000 20 5 1 2500
Fishing 30000 30 7 2 3000
```

Output:

```text
Server_shifrlovchi: SLE=100000.0 ALE=30000.0 -> 10000.0 benefit=5000.0 adopt
Noutbuk_ogirligi: SLE=12000.0 ALE=24000.0 -> 18000.0 benefit=-2000.0 reject
Sayt_DDoS: SLE=10000.0 ALE=5000.0 -> 1000.0 benefit=1500.0 adopt
Fishing: SLE=9000.0 ALE=6300.0 -> 1800.0 benefit=1500.0 adopt
total annual loss: 65300.0 -> 36800.0, cost of controls 20500.0
```

A backup system against server ransomware (15 million a year) cuts the expected annual loss by 20 million — worthwhile. A laptop tracking system (8 million), however, cuts the loss by only 6 million — another measure should be sought (for example, disk encryption and insurance) or the risk accepted.

:::warning
**Common mistakes**

- **Treating risk and threat as the same thing.** A threat is a source; risk is its likelihood and consequence.
- **Letting a control cost more than the asset.** The benefit of protection must exceed its cost.
- **Assessing a risk once and forgetting it.** The risk register is updated regularly (at least once a year and with every major change).
- **Ignoring intangible losses.** Reputation, customer trust and fines also count towards the asset value.
- **Not documenting residual risk.** Accepted risks are recorded with management's signature.
:::

:::tip
Draw up a small register of 5 risks for your university (for example, a leak of the grade database, a fire in the server room, a teacher's laptop being stolen, the website going down, phishing). For each give a likelihood, an impact, an owner and one control.
:::

## Summary

- Risk management: context → assets → threats and vulnerabilities → assessment → treatment → monitoring.
- Qualitative assessment is the risk matrix (likelihood × impact); quantitative is ALE = SLE × ARO, with SLE = AV × EF.
- The benefit of a control = ALE before − ALE after − cost; if positive, the control pays for itself.
- Strategies: mitigate, transfer, avoid, accept; residual risk is formally accepted.
- The risk register and RMIS are the tools for keeping, monitoring and reporting risks.

## Check yourself

:::check
Q: If AV = 80 million and EF = 25%, what is the SLE?
A: 20 million soʻm.
Q: SLE = 20 million and an incident is expected once in 4 years. What is the ALE?
A: 20 × 0.25 = 5 million soʻm a year.
Q: Which risk treatment strategy is cyber insurance?
A: Transfer.
Q: ALE before the control is 12 million, after it 4 million, the control costs 10 million a year. Should it be adopted?
A: No: benefit = 12 − 4 − 10 = −2 million.
Q: What is an RMIS for?
A: Keeping the risk register, recording incidents, monitoring and preparing reports for management.
:::

:::link https://csrc.nist.gov/pubs/sp/800/30/r1/final
NIST SP 800-30: Guide for Conducting Risk Assessments
NIST's guide to risk assessment.
:::
