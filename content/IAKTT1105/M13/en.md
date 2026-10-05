---
title: E-business and mobile commerce
summary: The concepts of e-business and e-commerce; models — B2B, B2C, C2C, B2G, D2C; business models — online shop, marketplace, subscription, platform, freemium; e-commerce infrastructure — website and app, payment, logistics, customer service; mobile commerce — mobile apps, QR payments, mobile banking, super apps; e-commerce in Uzbekistan (Uzum, Click, Payme and others); digital marketing and key indicators — conversion, average order value, CAC, ROAS; the legal basis and risks; a program computing channel effectiveness.
---

## Why does this topic matter?

Today a buyer chooses goods on a phone, pays with a QR code and receives them at the door the next day. E-commerce lets even a small entrepreneur reach the whole country and foreign markets. Key questions for an economist: which business model is profitable, which advertising channel pays off, how much does it cost to acquire a customer?

:::note
**After this lesson you will be able to:**

- explain the difference between e-business and e-commerce;
- distinguish the B2B, B2C, C2C and other models;
- name e-commerce business models and infrastructure;
- know the features of mobile commerce;
- calculate the key indicators of digital marketing.
:::

## Concepts

**E-business** is organizing business processes (purchasing, production, sales, accounting, communication with partners) through digital technologies. **E-commerce** is a part of it: selling and buying goods and services over the Internet, including payment.

| Model | Who to whom | Example |
|---|---|---|
| **B2C** | business → consumer | an online shop, online tickets |
| **B2B** | business → business | wholesale trade platforms, electronic invoices |
| **C2C** | consumer → consumer | classified ad sites (such as OLX) |
| **B2G** | business → government | public procurement portals |
| **D2C** | manufacturer → consumer | a brand's own online shop |

## Business models

| Model | How it earns | Example |
|---|---|---|
| **Online shop** | selling its own goods | a brand or shop website |
| **Marketplace** | commission from sellers, advertising | Uzum Market, Wildberries, Amazon |
| **Subscription** | a monthly or yearly fee | streaming services, SaaS (M11) |
| **Platform** (a two-sided market) | intermediating between drivers and passengers, couriers and restaurants | taxi and delivery apps |
| **Freemium** | the basic service is free, the extended one paid | office and cloud services |
| **Advertising** | from users' attention | social networks, search engines |

## E-commerce infrastructure

- **Website and mobile app**: catalogue, search, cart, order.
- **Payment**: bank cards (Uzcard, Humo, Visa), payment systems (Click, Payme and others), QR payments, buy now pay later (BNPL), cash on delivery.
- **Logistics**: warehouses, courier services, pick-up points, order tracking.
- **Customer service**: chat, chatbots, return and exchange policy, reviews.
- **Analytics**: CRM (M3), web analytics, A/B tests.

## Mobile commerce

**Mobile commerce** (m-commerce) is buying and paying via a smartphone. Its features: always at hand, location-based services, push notifications, biometric confirmation, one-tap payment. Widespread in Uzbekistan: mobile banking, utility and public payments through Click and Payme, QR payments in shops, marketplace apps, taxis and delivery. A **super app** offers payments, shopping, taxis and banking in one app (Asian experience: WeChat, Grab).

## Digital marketing indicators

| Indicator | Formula | Meaning |
|---|---|---|
| **Conversion** | orders / visits × 100% | what share of visitors made a purchase |
| **Average order value** (AOV) | revenue / orders | the average amount of one order |
| **CAC** (customer acquisition cost) | advertising cost / new customers (orders) | how much it cost to acquire one buyer |
| **ROAS** | revenue / advertising cost | how much revenue each soum spent on advertising brought |
| **LTV** (customer lifetime value) | the profit a customer brings over the whole period | if CAC < LTV, acquisition pays off |

## Worked example: comparing advertising channels

An online shop advertised through three channels in one month. Each line holds: channel, visits, orders, revenue, advertising cost (soums). Print the conversion, average order value, CAC and ROAS.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    kanal, tashrif, buyurtma, tushum, reklama = satr.split()
    tashrif, buyurtma, tushum, reklama = int(tashrif), int(buyurtma), int(tushum), int(reklama)
    konversiya = 100 * buyurtma / tashrif               # as a percentage
    chek = tushum // buyurtma                            # average order value
    cac = reklama // buyurtma                            # the cost of one buyer
    roas = tushum / reklama                              # return on advertising
    print(f"{kanal}: conversion {konversiya:.2f}%, average order {chek}, CAC {cac}, ROAS {roas:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String kanal = in.next();
            long tashrif = in.nextLong(), buyurtma = in.nextLong(), tushum = in.nextLong(), reklama = in.nextLong();
            double konversiya = 100.0 * buyurtma / tashrif;          // as a percentage
            long chek = tushum / buyurtma;                            // average order value
            long cac = reklama / buyurtma;                            // the cost of one buyer
            double roas = (double) tushum / reklama;                  // return on advertising
            System.out.println(String.format("%s: conversion %.2f%%, average order %d, CAC %d, ROAS %.2f", kanal, konversiya, chek, cac, roas));
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
    string kanal;
    long long tashrif, buyurtma, tushum, reklama;
    while (cin >> kanal >> tashrif >> buyurtma >> tushum >> reklama) {
        double konversiya = 100.0 * buyurtma / tashrif;              // as a percentage
        long long chek = tushum / buyurtma;                           // average order value
        long long cac = reklama / buyurtma;                           // the cost of one buyer
        double roas = (double) tushum / reklama;                      // return on advertising
        printf("%s: conversion %.2f%%, average order %lld, CAC %lld, ROAS %.2f\n", kanal.c_str(), konversiya, chek, cac, roas);
    }
    return 0;
}
```

Input:

```text
Instagram 12000 240 36000000 6000000
Telegram 8000 200 26000000 3000000
Google 15000 180 31500000 9000000
```

Output:

```text
Instagram: conversion 2.00%, average order 150000, CAC 25000, ROAS 6.00
Telegram: conversion 2.50%, average order 130000, CAC 15000, ROAS 8.67
Google: conversion 1.20%, average order 175000, CAC 50000, ROAS 3.50
```

Google brought the most visits and the largest average order, but its buyers were the most expensive (CAC 50,000) and its ROAS the lowest. Telegram is the most effective: 8.67 soums of revenue per soum of advertising. But for a conclusion it is **profit** that matters, not revenue: if the product margin is 20%, the channel with ROAS 3.5 (0.7 soums of profit per soum) makes a loss. The budget can be reallocated on the basis of this analysis.

## Legal basis and risks

- The Law "On Electronic Commerce", consumer protection, personal data and tax legislation (online cash registers, electronic receipts).
- Risks: fraudulent online shops and fake payment pages, data leaks, delivery problems, dependence on platforms (rising commissions).
- Protection: official websites, 3-D Secure, buyer reviews, a clear return policy, data protection.

:::warning
**Common mistakes**

- **Looking only at the number of visits.** What matters is conversion, CAC and profit.
- **Confusing revenue with profit.** Even a high ROAS can mean a loss if the margin is low.
- **Ignoring the mobile experience.** Much of shopping happens on phones — a slow or awkward site loses customers.
- **Hiding return and delivery terms.** Consumer rights are violated and trust is lost.
:::

:::tip
Assuming a 30% margin for the channels in the example, find each channel's profit (revenue × 0.3 − advertising). Which channel's budget should be increased and which cut?
:::

## Summary

- E-business is the digitalization of processes; e-commerce is trade and payment over the Internet.
- Models: B2C, B2B, C2C, B2G, D2C; business models: shop, marketplace, subscription, platform, freemium, advertising.
- Infrastructure: website and app, payment, logistics, service, analytics; mobile commerce is buying and QR payment via smartphone.
- Indicators: conversion, average order value, CAC, ROAS, LTV; decisions are made on the basis of profit.

## Check yourself

:::check
Q: What is the difference between e-business and e-commerce?
A: E-business is the digitalization of all business processes; e-commerce is part of it — selling and buying over the Internet.
Q: How does a marketplace earn money?
A: Through commissions from sellers and advertising.
Q: How is CAC calculated?
A: Advertising cost / the number of acquired customers (orders).
Q: What does a ROAS of 3.5 mean?
A: Each soum spent on advertising brought 3.5 soums of revenue.
:::

:::link https://unctad.org/topic/ecommerce-and-digital-economy
UNCTAD: E-commerce and the Digital Economy
International research and statistics on e-commerce and the digital economy.
:::
