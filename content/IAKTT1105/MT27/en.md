---
title: Internet banking, internet trade, internet insurance, internet marketing
summary: Independent study — deepening seminar S13; the internet banking ecosystem — open banking and APIs, fintech companies, embedded finance, "buy now, pay later" (BNPL) and its risks; the economics of internet trade — last-mile costs, returns; internet insurance — risk-based pricing, usage-based policies; internet marketing — attribution models (last click, linear, position-based, data-driven), privacy and consent; an attribution program; an assignment.
---

## Purpose of this independent study

In seminar S13 we got to know the services, processes and security rules of four online areas — banking, trade, insurance and marketing. In this independent study we look more deeply at **the economic mechanisms and new trends** of each area and learn to allocate marketing effectiveness correctly.

:::note
**After this independent study you will be able to:**

- explain the idea of open banking and embedded finance;
- assess the risks of BNPL and microloans;
- name the economic features of internet trade and internet insurance;
- compare attribution models and allocate a marketing budget on a sound basis.
:::

## The internet banking ecosystem

- **Open banking**: with the customer's consent, a bank opens their account data and payment functions to other licensed services through an **API**. As a result, an accounting program receives statements automatically, and a finance app shows accounts at several banks on one screen.
- **Fintech companies** — payment services, digital wallets, online lending, money transfers; they compete and cooperate with banks.
- **Embedded finance** — a financial service inside a non-bank app: instalments on a marketplace, a wallet in a taxi app, a loan in an accounting program.
- **"Buy now, pay later"** (BNPL): the purchase amount is split into several equal parts, often "interest-free". Risks: falling into debt easily, parallel debts with several services, late fees, the debt burden not being taken into account. A question for an economist: if it is "interest-free", where does the service earn its income? (A commission from the seller, late fees.)

## The economics of internet trade

| Cost | Meaning | Effect |
|---|---|---|
| **The last mile** | delivery from the warehouse to the customer's door | often the largest part of delivery costs; pick-up points and parcel lockers are cheaper |
| **Returns** | the customer returns the goods (especially clothing) | re-inspection, logistics, loss of the goods' value |
| **Payment commission** | acquiring and payment systems | a percentage of every sale |
| **Marketplace commission** | the platform's share (MT26) | reduces the margin |
| **Customer acquisition** | advertising (CAC) | determines unit economics (MT26) |

Free delivery is free for the customer but not for the seller: its cost is "hidden" in the price of goods or a minimum order amount.

## Internet insurance

- **Risk-based pricing**: the more data there is, the closer the premium is calculated to each customer's real risk.
- **Usage-based policies**: "pay as you drive" — by the kilometres a car travels or by telematics (S13).
- **Parametric insurance** (S12): the event is determined by an objective indicator (rainfall, earthquake strength) — payouts are fast.
- **Microinsurance**: policies for small amounts — for example, per day of travel or for a phone just bought, inside an app.
- **The risk**: excessive personalization can make insurance very expensive for some groups — a question of fair use of data.

## Internet marketing: attribution

Before buying, a customer interacts with several channels: sees an Instagram ad, then searches on Google, and finally comes to the website directly and buys. Which channel should the revenue be "credited" to? This is the **attribution** problem.

| Model | Rule | Advantage / drawback |
|---|---|---|
| **Last click** | all revenue to the last channel | simple; undervalues channels that created awareness |
| **First click** | all revenue to the first channel | shows awareness; ignores the final decision |
| **Linear** | equally to all channels | looks fair; not every touch is equally important |
| **Position-based** (U-shaped) | 40% to the first, 40% to the last, 20% to those in between | balances the two |
| **Data-driven** | a statistical model estimates the channels' real contribution | more accurate; needs a lot of data |

## Worked example: last-click and linear attribution

Each line holds the purchase amount and the customer's path (channels separated by `>`, the last one before the purchase). Calculate the revenue of each channel under the two models.

```python
import sys

oxirgi, chiziqli = {}, {}
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    summa, yol = satr.split()
    summa = int(summa)
    kanallar = yol.split(">")
    for k in kanallar:
        oxirgi.setdefault(k, 0)
        chiziqli[k] = chiziqli.get(k, 0) + summa // len(kanallar)   # equal share
    oxirgi[kanallar[-1]] += summa                                   # everything to the last channel

for k in sorted(oxirgi):
    print(f"{k}: last click {oxirgi[k]}, linear {chiziqli[k]}")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        TreeMap<String, Long> oxirgi = new TreeMap<>(), chiziqli = new TreeMap<>();
        while (in.hasNext()) {
            long summa = in.nextLong();
            String[] kanallar = in.next().split(">");
            for (String k : kanallar) {
                oxirgi.putIfAbsent(k, 0L);
                chiziqli.merge(k, summa / kanallar.length, Long::sum);   // equal share
            }
            oxirgi.merge(kanallar[kanallar.length - 1], summa, Long::sum);   // everything to the last channel
        }
        for (Map.Entry<String, Long> e : oxirgi.entrySet())
            System.out.println(e.getKey() + ": last click " + e.getValue() + ", linear " + chiziqli.get(e.getKey()));
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    map<string, long long> oxirgi, chiziqli;
    long long summa;
    string yol;
    while (cin >> summa >> yol) {
        vector<string> kanallar;
        stringstream ss(yol);
        string k;
        while (getline(ss, k, '>')) kanallar.push_back(k);
        for (auto& x : kanallar) {
            oxirgi[x] += 0;
            chiziqli[x] += summa / (long long) kanallar.size();      // equal share
        }
        oxirgi[kanallar.back()] += summa;                            // everything to the last channel
    }
    for (auto& [k, v] : oxirgi) cout << k << ": last click " << v << ", linear " << chiziqli[k] << "\n";
    return 0;
}
```

Input:

```text
120000 Instagram>Google>Direct
90000 Telegram>Direct
60000 Google
150000 Instagram>Telegram>Google>Direct
```

Output:

```text
Direct: last click 360000, linear 122500
Google: last click 60000, linear 137500
Instagram: last click 0, linear 77500
Telegram: last click 0, linear 82500
```

Under "last click", Instagram and Telegram brought no revenue at all — it looks as if their ads could be switched off. But the linear model shows that they attracted customers first and took part in 38% of total revenue. If they are switched off, visits to "Direct" may fall too. For a correct conclusion, compare several models and, if possible, run an experiment (for example, pausing ads in one region) ("correlation is not causation" from MT2).

## Privacy and consent

- Browsers are restricting third-party cookies; mobile platforms ask for consent to tracking — following a customer across channels is becoming harder.
- Companies rely more on **first-party data** (their own website, app, loyalty programme) and customer consent.
- Personal data legislation (S11, MT17): the purpose of data collection must be clear, consent voluntary and refusal easy.

:::warning
**Common mistakes**

- **Allocating the budget by last click alone.** Channels that create awareness wrongly look "useless".
- **Seeing "interest-free" instalments as free.** There is a risk of fees and parallel debts.
- **Not accounting for the cost of free delivery.** It still falls into unit economics.
- **Collecting data without consent.** A legal risk and a loss of trust.
:::

## Assignment

1. Add a **first-click** model to the example program and compare the three models.
2. Calculate by hand the channels' revenue in the example under the **position-based** model (40/20/40) (100% for a single-channel path, 50/50 for a two-channel one).
3. Compare the terms (fees, deadlines, commissions) of two BNPL or instalment services and write down the risks for the buyer.
4. Propose ways to reduce last-mile costs for one online shop.

## Summary

- The internet banking ecosystem: open banking and APIs, fintech, embedded finance; BNPL is convenient but risky.
- Internet trade costs: the last mile, returns, commissions, CAC.
- Internet insurance: risk-based and usage-based pricing, parametric and microinsurance.
- Attribution models allocate revenue across channels differently; comparing several models and experiments are needed.

## Check yourself

:::check
Q: What is open banking?
A: A bank opening, with the customer's consent, their account data and payment functions to other licensed services through an API.
Q: Give an example of embedded finance.
A: Instalments inside a marketplace or a wallet in a taxi app.
Q: What is the drawback of the "last click" model?
A: It ignores the contribution of channels that attracted the customer first and created awareness.
Q: How does usage-based insurance work?
A: The premium is calculated from actual use (for example, the kilometres driven or the driving style).
:::

:::link https://www.cgap.org/
CGAP: Consultative Group to Assist the Poor
Research on digital financial services and consumer protection.
:::
