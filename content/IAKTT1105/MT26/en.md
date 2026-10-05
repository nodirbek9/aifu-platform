---
title: E-business and e-commerce models
summary: Independent study — the business model canvas (Osterwalder) and its nine blocks using an e-business example; revenue models (sales margin, commission, subscription, advertising, transaction fees, affiliate programmes, licensing); modern forms of e-commerce — D2C, dropshipping, social and live-stream commerce, subscription boxes, omnichannel; unit economics — ARPU, margin, churn, LTV, CAC, payback period; a program comparing business models; an assignment.
---

## Purpose of this independent study

In M13 we got to know the types of e-business (B2C, B2B, C2C, B2G, D2C) and the main business models. In this independent study we learn to **describe a business model systematically** (the canvas) and to test its **economic viability** through unit economics.

:::note
**After this independent study you will be able to:**

- fill in a business model canvas for an e-business;
- distinguish revenue models;
- name modern forms of e-commerce;
- calculate LTV, CAC and the payback period and assess whether a model is healthy.
:::

## The business model canvas

Alexander Osterwalder's **business model canvas** describes a business on one sheet in nine blocks. An example — a small shop selling books online:

| Block | Question | Example |
|---|---|---|
| **Customer segments** | whom do we serve? | students, teachers, readers |
| **Value proposition** | which problem do we solve? | finding the right book quickly and delivering it the next day |
| **Channels** | how do we reach customers? | website, Telegram bot, marketplace |
| **Customer relationships** | how do we stay in touch? | personal recommendations, loyalty points |
| **Revenue streams** | what do we earn money from? | the margin on book sales, a subscription (1 book a month) |
| **Key resources** | what is needed? | a catalogue, a warehouse, a customer database |
| **Key activities** | what do we do? | purchasing, marketing, delivery |
| **Key partners** | whom do we work with? | publishers, a courier service, payment systems |
| **Cost structure** | what do we spend money on? | books, advertising, salaries, logistics |

## Revenue models

| Model | Meaning | Example |
|---|---|---|
| **Sales margin** | the difference between purchase and sale prices | an online shop |
| **Commission** | a share of each sale | a marketplace, a taxi app |
| **Subscription** | regular payments | SaaS, online cinema, subscription boxes |
| **Advertising** | selling the audience's attention | social networks, news sites |
| **Transaction fee** | for a payment or transfer | payment systems |
| **Affiliate** | a share for referring a customer to another seller | review sites, bloggers |
| **Licensing** | for the right to use | software, content |
| **Freemium** | a free base + paid extras | MT25 |

## Modern forms of e-commerce

- **D2C** (direct to consumer) — the manufacturer sells through its own website without intermediaries: a high margin and customer data, but it bears the marketing costs itself.
- **Dropshipping** — the seller has no warehouse; the order is passed to the supplier, who ships directly to the customer: cheap to enter, but low margins and quality control is hard.
- **Social commerce** — selling through Instagram, Telegram, TikTok; **live commerce** — goods are shown during a live stream and ordered immediately.
- **Subscription boxes** — a selected set of goods every month (cosmetics, coffee, books).
- **Omnichannel** — online and offline channels as one system: order on the website, pick up in a shop, return anywhere.

## Unit economics

A business model must be profitable **at the level of a single customer** — otherwise growth only increases losses.

| Indicator | Formula | Meaning |
|---|---|---|
| **ARPU** | revenue / number of customers (per month) | average monthly revenue per customer |
| **Margin** | (revenue − variable costs) / revenue | what share of revenue remains as profit |
| **Churn** | customers lost during the month / customers at the start of the month | how quickly customers leave |
| **Customer "lifetime"** | 1 / churn | how many months a customer stays on average |
| **LTV** | ARPU × margin × lifetime | the profit from a customer over the whole period (M13) |
| **CAC** | marketing spend / new customers | the cost of acquiring one customer |
| **Payback period** | CAC / (ARPU × margin) | in how many months the CAC is recovered |

**A rule of thumb**: LTV/CAC ≥ 3 — a healthy model; from 1 to 3 — caution is needed; below 1 — every new customer brings a loss.

## Worked example: comparing models

Each line holds: the model, monthly ARPU (soums), margin (%), monthly churn (%), CAC (soums). Output LTV, LTV/CAC, the payback period (months, rounded up) and the status.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, arpu, marja, churn, cac = satr.split()
    arpu, marja, churn, cac = int(arpu), int(marja), int(churn), int(cac)
    oylik_foyda = arpu * marja // 100                  # monthly profit per customer
    ltv = oylik_foyda * 100 // churn                   # lifetime = 1 / churn
    nisbat = ltv / cac
    qoplash = (cac + oylik_foyda - 1) // oylik_foyda   # round up
    if nisbat >= 3:
        holat = "healthy"
    elif nisbat >= 1:
        holat = "caution"
    else:
        holat = "loss-making"
    print(f"{nom}: LTV {ltv}, CAC {cac}, LTV/CAC {nisbat:.1f}, payback {qoplash} months, {holat}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            long arpu = in.nextLong(), marja = in.nextLong(), churn = in.nextLong(), cac = in.nextLong();
            long oylikFoyda = arpu * marja / 100;                  // monthly profit per customer
            long ltv = oylikFoyda * 100 / churn;                   // lifetime = 1 / churn
            double nisbat = (double) ltv / cac;
            long qoplash = (cac + oylikFoyda - 1) / oylikFoyda;    // round up
            String holat = nisbat >= 3 ? "healthy" : nisbat >= 1 ? "caution" : "loss-making";
            System.out.println(String.format("%s: LTV %d, CAC %d, LTV/CAC %.1f, payback %d months, %s", nom, ltv, cac, nisbat, qoplash, holat));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string nom;
    long long arpu, marja, churn, cac;
    while (cin >> nom >> arpu >> marja >> churn >> cac) {
        long long oylikFoyda = arpu * marja / 100;                 // monthly profit per customer
        long long ltv = oylikFoyda * 100 / churn;                  // lifetime = 1 / churn
        double nisbat = (double) ltv / cac;
        long long qoplash = (cac + oylikFoyda - 1) / oylikFoyda;   // round up
        string holat = nisbat >= 3 ? "healthy" : nisbat >= 1 ? "caution" : "loss-making";
        printf("%s: LTV %lld, CAC %lld, LTV/CAC %.1f, payback %lld months, %s\n", nom.c_str(), ltv, cac, nisbat, qoplash, holat.c_str());
    }
    return 0;
}
```

Input:

```text
subscription 50000 70 5 140000
marketplace 20000 15 10 25000
shop 80000 30 8 150000
ads 10000 40 25 20000
```

Output:

```text
subscription: LTV 700000, CAC 140000, LTV/CAC 5.0, payback 4 months, healthy
marketplace: LTV 30000, CAC 25000, LTV/CAC 1.2, payback 9 months, caution
shop: LTV 300000, CAC 150000, LTV/CAC 2.0, payback 7 months, caution
ads: LTV 16000, CAC 20000, LTV/CAC 0.8, payback 5 months, loss-making
```

The subscription model is the healthiest thanks to a high margin and low churn: a customer stays for 20 months on average. The marketplace has a low margin (15%) — the cost of acquiring customers must be cut or purchase frequency raised. In the advertising model customers leave quickly (25% a month) and LTV is below CAC — every new user is a loss. The solution: reduce churn (quality, a loyalty programme) or use cheaper channels.

:::warning
**Common mistakes**

- **Confusing revenue with profit.** LTV is calculated with the margin, not with revenue.
- **Ignoring churn.** A monthly churn of 10% means a customer stays only 10 months on average.
- **Thinking "growth solves everything".** If LTV/CAC < 1, growth multiplies the losses.
- **Dependence on one channel or platform.** If the marketplace commission or advertising prices rise, the model breaks.
:::

## Assignment

1. Fill in a business model canvas for an e-business of your choice (an existing company or your own idea).
2. Write down its revenue model and the forms of e-commerce it uses.
3. Justify estimated values of ARPU, margin, churn and CAC and calculate the unit economics with the example program.
4. Propose 3 measures to raise LTV/CAC above 3 and check the effect of each in the program.

## Summary

- The business model canvas: segments, value proposition, channels, relationships, revenue, resources, activities, partners, costs.
- Revenue models: margin, commission, subscription, advertising, transaction fees, affiliate, licensing, freemium.
- Modern forms: D2C, dropshipping, social and live-stream commerce, subscription boxes, omnichannel.
- Unit economics: LTV = ARPU × margin × lifetime; LTV/CAC ≥ 3 is a healthy model.

## Check yourself

:::check
Q: How many blocks does the business model canvas have?
A: Nine.
Q: If monthly churn is 5%, how many months does a customer stay on average?
A: 1 / 0.05 = 20 months.
Q: What does LTV/CAC = 0.8 mean?
A: Acquiring a customer costs more than the profit they bring — every new customer causes a loss.
Q: What is the main drawback of dropshipping?
A: Low margins and difficulty controlling quality and delivery times.
:::

:::link https://www.strategyzer.com/library/the-business-model-canvas
Strategyzer: The Business Model Canvas
Official materials on the business model canvas.
:::
