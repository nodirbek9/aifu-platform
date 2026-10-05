---
title: The concept of the electronic market and electronic goods
summary: Independent study — the concept and types of electronic markets (online shop, marketplace, exchange, auction, reverse auction — public procurement); electronic (digital) goods — software, e-books, music, video, online courses, in-game items; the economics of digital goods — near-zero marginal cost, non-rivalry, experience goods; pricing strategies — versioning, bundling, freemium, subscription, dynamic pricing; two-sided platforms; copyright and DRM; consumer rights; a program for choosing a bundle price.
---

## Purpose of this independent study

In M13 we saw e-business models and marketing metrics. In this independent study we learn how an **electronic market** is organized and how **digital goods** differ economically from ordinary goods — a difference that fundamentally changes pricing and competition.

:::note
**After this independent study you will be able to:**

- distinguish the types of electronic markets;
- explain the economic properties of digital goods;
- choose pricing strategies for digital goods;
- calculate the benefit of a bundle price.
:::

## The electronic market

An **electronic market** is an environment in which sellers and buyers agree on price, quantity and terms through electronic means.

| Type | How the price is formed | Example |
|---|---|---|
| **Online shop** | the seller sets a fixed price | a brand's website |
| **Marketplace** | many sellers compete | Uzum Market, Amazon (M13) |
| **Electronic exchange** | where supply and demand meet | commodity and currency exchanges |
| **Auction** | buyers bid the price up | eBay, sales of confiscated property |
| **Reverse auction** | sellers bid the price down | the public procurement portal, tenders |
| **Classifieds platform** | the parties agree with each other | C2C sites such as OLX |

## Electronic goods

An **electronic (digital) good** is a good created, stored and delivered in digital form: software and apps, e-books, music and video, online courses, fonts and templates, in-game items, databases, digital tickets.

### Economic properties

| Property | Meaning | Consequence |
|---|---|---|
| **High fixed cost, near-zero marginal cost** | the first copy is expensive (development), the next ones almost free | the price is set by buyers' valuation, not by cost |
| **Non-rivalry** | one person's use of a copy does not hinder another's | copying is easy — protection is needed |
| **Experience good** | its quality can be known only after use | free trials, samples and reviews matter |
| **Network effect** | value grows as users increase (MT6) | "the winner takes most" |
| **Lock-in** | the cost of switching to another good (files, habits) | loyalty and a strong market position |

## Pricing strategies

| Strategy | Meaning | Example |
|---|---|---|
| **Versioning** | versions of one product with different features at different prices | "Basic", "Pro", "Business" |
| **Bundling** | several goods together, cheaper than the sum of their separate prices | an office suite, streaming + music |
| **Freemium** | the basic features are free, extended ones paid | 15 GB of free cloud storage |
| **Subscription** | a monthly or annual payment for continuous access | online cinema, SaaS (M11) |
| **Dynamic pricing** | changes with demand, time, the customer | air tickets, taxis |
| **Micropayments** | small amounts inside an app | in-game items |

**Why does bundling pay?** Buyers value goods differently: one values the first good highly and the second low, another the reverse. When selling separately, the seller must choose one price for each good and loses some buyers. In a bundle the valuations add up and move closer to each other — one price can cover more buyers. With digital goods, where an extra copy is almost free, this strategy is especially effective.

## Worked example: separately or as a bundle?

Each line holds a buyer and how much they value products A and B (thousand soums). Find the price that maximizes revenue for each product (on a tie, the higher price), then the best price for the bundle, and compare. A buyer buys if their valuation is not lower than the price.

```python
import sys

def eng_yaxshi(qadrlar):
    # maximum revenue; on a tie, the higher price
    narx, eng_tushum = 0, 0
    for p in sorted(set(qadrlar)):
        t = p * sum(1 for q in qadrlar if q >= p)
        if t >= eng_tushum:
            narx, eng_tushum = p, t
    return narx, eng_tushum

a, b = [], []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    _, qa, qb = satr.split()
    a.append(int(qa))
    b.append(int(qb))

na, ta = eng_yaxshi(a)
nb, tb = eng_yaxshi(b)
nt, tt = eng_yaxshi([x + y for x, y in zip(a, b)])     # each buyer's value of the bundle
print(f"Product A: price {na}, revenue {ta}")
print(f"Product B: price {nb}, revenue {tb}")
print(f"Separate sales: {ta + tb}")
print(f"Bundle: price {nt}, revenue {tt}")
print(f"Recommendation: {'bundle' if tt > ta + tb else 'separate'}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {
    static long[] engYaxshi(List<Long> qadrlar) {
        // maximum revenue; on a tie, the higher price
        long narx = 0, engTushum = 0;
        for (long p : new TreeSet<>(qadrlar)) {
            long soni = 0;
            for (long q : qadrlar) if (q >= p) soni++;
            if (p * soni >= engTushum) { narx = p; engTushum = p * soni; }
        }
        return new long[]{narx, engTushum};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Long> a = new ArrayList<>(), b = new ArrayList<>(), t = new ArrayList<>();
        while (in.hasNext()) {
            in.next();
            long qa = in.nextLong(), qb = in.nextLong();
            a.add(qa);
            b.add(qb);
            t.add(qa + qb);                                  // each buyer's value of the bundle
        }
        long[] ra = engYaxshi(a), rb = engYaxshi(b), rt = engYaxshi(t);
        System.out.println("Product A: price " + ra[0] + ", revenue " + ra[1]);
        System.out.println("Product B: price " + rb[0] + ", revenue " + rb[1]);
        System.out.println("Separate sales: " + (ra[1] + rb[1]));
        System.out.println("Bundle: price " + rt[0] + ", revenue " + rt[1]);
        System.out.println("Recommendation: " + (rt[1] > ra[1] + rb[1] ? "bundle" : "separate"));
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <string>
#include <utility>
#include <vector>
using namespace std;

pair<long long, long long> engYaxshi(const vector<long long>& qadrlar) {
    // maximum revenue; on a tie, the higher price
    long long narx = 0, engTushum = 0;
    for (long long p : set<long long>(qadrlar.begin(), qadrlar.end())) {
        long long soni = 0;
        for (long long q : qadrlar) if (q >= p) soni++;
        if (p * soni >= engTushum) { narx = p; engTushum = p * soni; }
    }
    return {narx, engTushum};
}

int main() {
    vector<long long> a, b, t;
    string ism;
    long long qa, qb;
    while (cin >> ism >> qa >> qb) {
        a.push_back(qa);
        b.push_back(qb);
        t.push_back(qa + qb);                                // each buyer's value of the bundle
    }
    auto ra = engYaxshi(a), rb = engYaxshi(b), rt = engYaxshi(t);
    cout << "Product A: price " << ra.first << ", revenue " << ra.second << "\n";
    cout << "Product B: price " << rb.first << ", revenue " << rb.second << "\n";
    cout << "Separate sales: " << ra.second + rb.second << "\n";
    cout << "Bundle: price " << rt.first << ", revenue " << rt.second << "\n";
    cout << "Recommendation: " << (rt.second > ra.second + rb.second ? "bundle" : "separate") << "\n";
    return 0;
}
```

Input:

```text
Ali 120 30
Vali 30 120
Sardor 90 90
Kamola 60 50
```

Output:

```text
Product A: price 90, revenue 180
Product B: price 90, revenue 180
Separate sales: 360
Bundle: price 150, revenue 450
Recommendation: bundle
```

When selling separately, each product is bought by only two people (Ali and Sardor buy A, Vali and Sardor buy B). With a bundle price of 150, Ali (150), Vali (150) and Sardor (180) buy the bundle — revenue rises by 25%. Ali's and Vali's tastes are opposite, but they value the bundle equally — this is the "magic" of bundling. Only Kamola (110) buys nothing in either case.

## Two-sided platforms

A marketplace, a taxi app, a payment system are **two-sided markets**: they connect two groups (sellers and buyers, drivers and passengers). The more there are on one side, the more valuable the platform is to the other side (a cross-side network effect). That is why platforms often subsidize one side: free delivery for buyers, bonuses for drivers — while the fee (commission) is taken from the other side.

## Copyright and consumer rights

- **Copyright**: a digital good is easy to copy; illegal distribution (piracy) reduces authors' income. **DRM** (digital rights management) technically restricts copying and distribution, but it can inconvenience legitimate buyers. Cheap and convenient legal services (subscriptions) have proved an effective way to reduce piracy.
- **Licence and ownership**: when buying a digital good you often obtain a **licence to use** it, not the good itself — if the platform closes, access may be lost.
- **Consumer rights**: an accurate description of the good, terms shown before payment, an easy way to cancel an automatic subscription.

:::warning
**Common mistakes**

- **Setting the price of a digital good from its cost.** The marginal cost is near zero — the price comes from valuation and competition.
- **Assuming bundling always pays.** If buyers value the goods in the same order, the gain is small or none.
- **Leaving no reason for the paid version in freemium.** If the free version is too good, nobody pays.
- **Making it hard to cancel a subscription.** This violates consumer rights and destroys trust.
:::

## Assignment

1. In the example, change Kamola's valuations from (60, 50) to (80, 80). How does the result change?
2. Create data in which all buyers value the goods in the same order (for example, A is always worth more than B). Does bundling still pay?
3. Analyse the pricing strategy of one digital service you use: versions, freemium, bundling, subscription.
4. Find the result of one reverse auction on the public procurement portal: what is the difference between the starting and final price?

## Summary

- Types of electronic market: shop, marketplace, exchange, auction, reverse auction, classifieds platform.
- Digital goods: high fixed and near-zero marginal cost, non-rivalry, experience goods, network effects, lock-in.
- Pricing: versioning, bundling, freemium, subscription, dynamic pricing, micropayments.
- Bundling can even out differing valuations and raise revenue; platforms subsidize one side.

## Check yourself

:::check
Q: Why is the marginal cost of a digital good near zero?
A: Once the first copy has been created, distributing each additional copy is almost free.
Q: What is a reverse auction?
A: A sale in which sellers compete by lowering the price for a buyer (for example, a government customer).
Q: What is the versioning strategy?
A: Selling versions of one product with different features at different prices.
Q: When does bundling pay most?
A: When buyers value the goods differently (in opposite ways).
:::

:::link https://www.oecd.org/en/topics/digital-economy.html
OECD: Digital economy
Research on the digital economy and digital markets.
:::
