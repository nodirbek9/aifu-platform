---
title: Mobile commerce
summary: Independent study — the features of mobile commerce and the "mobile first" approach; mobile channels — apps, mobile web, PWAs, mini-programs inside super-apps; mobile payment technologies — NFC, static and dynamic QR codes, tokenization, digital wallets; location-based services and push notifications; app store economics; app metrics — installs, DAU/MAU, retention, cohort analysis; mobile security; a program calculating retention by cohort; an assignment.
---

## Purpose of this independent study

In M13 we briefly got to know the concept of mobile commerce. Today most Internet users in Uzbekistan go online mainly through a smartphone, so for many companies the mobile channel is the main channel. In this independent study we look more deeply at the technologies, economics and performance metrics of mobile commerce.

:::note
**After this independent study you will be able to:**

- compare the types of mobile channels and their advantages;
- explain mobile payment technologies;
- know the economics of app stores;
- calculate and interpret DAU/MAU and retention.
:::

## The features of mobile commerce

| Feature | Meaning | Opportunity for business |
|---|---|---|
| **Always at hand** | the phone is with the user all day | buying at any moment, timely messages |
| **Location** | the position is determined via GPS and Wi-Fi | the nearest shop, the delivery address, local offers |
| **Personal** | one device — one user | personal recommendations |
| **Camera and sensors** | scanning QR codes, photographing documents | payments, remote identification (S13) |
| **Biometrics** | fingerprint, face | fast and secure confirmation |
| **Small screen** | short attention, awkward input | a simple interface, one-tap payment |

**"Mobile first"** — a service is designed for the phone first and then adapted to larger screens.

## Mobile channels

| Channel | Advantage | Drawback |
|---|---|---|
| **Native app** (iOS, Android) | the fastest and most convenient, push notifications, device features | expensive to develop, must be installed, store commission |
| **Mobile website** | no installation, found through search | limited features, slower |
| **PWA** (progressive web app) | web, but feels like an app: add to home screen, offline mode | some limitations on iOS |
| **Mini-program inside a super-app** | ready access to millions of users, payments and identification ready | the platform's rules and commission, the customer "belongs" to the platform |
| **Messenger bot** | simple orders and services in messengers such as Telegram | awkward for a complex catalogue |

## Mobile payment technologies

- **NFC** — paying by holding a phone or watch near the terminal (contactless).
- **QR payments**: a **static QR** — a shop's permanent code, the buyer enters the amount; a **dynamic QR** — a new code with the amount for each purchase, confirmed automatically by the till.
- **Tokenization** — the wallet stores not the card number but a **token** that replaces it; even if the token is stolen, the real card data is not exposed (S14).
- **Digital wallets** — Apple Pay, Google Pay, Samsung Pay; in Uzbekistan, wallets inside payment apps and banking apps.
- **App-to-app transfers** — sending money by phone number.

## Location and push notifications

**Location-based services**: the nearest branch, the courier's real-time position, an offer on entering a "geofence". **Push notifications**: order status, promotions, a reminder about an abandoned basket. Both are used with the user's **consent** and in moderation — too many messages lead to the app being deleted.

## App store economics

- The App Store and Google Play take a **commission** on payments for digital goods and subscriptions (usually 15–30%, with lower rates for small developers and for the second year of a subscription).
- This commission usually does not apply to payments for physical goods (clothing, food) and services (taxis).
- Getting an app found: the store rating, reviews and keywords (**ASO** — app store optimization).
- The **CAC** of a customer acquired through an app (advertising + installation) enters unit economics (MT26).

## App metrics

| Metric | Formula | Meaning |
|---|---|---|
| **Installs** | the number of downloads | reach, but not activity |
| **DAU / MAU** | daily / monthly active users | "stickiness": 0.25 means the average user opens the app ~7.5 days a month |
| **Day-N retention** | users returning on day N / installs | whether the app is becoming a habit |
| **Conversion** | buyers / active users | M13 |
| **Uninstall rate** | the share of users who delete the app | a sign of dissatisfaction |

**Cohort analysis** — dividing users into groups (cohorts) who installed at the same time (for example, in the same month) and following each group's behaviour over time. If retention is rising in newer cohorts, the app is improving.

## Worked example: retention by cohort

Each line holds a cohort (the month of installation), the number of installs and the number of users who returned on days 1, 7 and 30. Output the retention percentages for each cohort and in total.

```python
import sys

kogortalar = []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, ornatish, k1, k7, k30 = satr.split()
    kogortalar.append((nom, int(ornatish), int(k1), int(k7), int(k30)))

def foiz(qism, butun):
    return 100 * qism / butun

for nom, n, a, b, c in kogortalar:
    print(f"{nom}: day 1 {foiz(a, n):.1f}%, day 7 {foiz(b, n):.1f}%, day 30 {foiz(c, n):.1f}%")

# total — weighted by the number of installs
n = sum(k[1] for k in kogortalar)
a = sum(k[2] for k in kogortalar)
b = sum(k[3] for k in kogortalar)
c = sum(k[4] for k in kogortalar)
print(f"Total: day 1 {foiz(a, n):.1f}%, day 7 {foiz(b, n):.1f}%, day 30 {foiz(c, n):.1f}%")
```
```java
import java.util.Scanner;

public class Main {
    static double foiz(long qism, long butun) {
        return 100.0 * qism / butun;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = 0, a = 0, b = 0, c = 0;
        while (in.hasNext()) {
            String nom = in.next();
            long ornatish = in.nextLong(), k1 = in.nextLong(), k7 = in.nextLong(), k30 = in.nextLong();
            System.out.println(String.format("%s: day 1 %.1f%%, day 7 %.1f%%, day 30 %.1f%%", nom, foiz(k1, ornatish), foiz(k7, ornatish), foiz(k30, ornatish)));
            n += ornatish; a += k1; b += k7; c += k30;          // total — weighted by the number of installs
        }
        System.out.println(String.format("Total: day 1 %.1f%%, day 7 %.1f%%, day 30 %.1f%%", foiz(a, n), foiz(b, n), foiz(c, n)));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

double foiz(long long qism, long long butun) {
    return 100.0 * qism / butun;
}

int main() {
    string nom;
    long long ornatish, k1, k7, k30, n = 0, a = 0, b = 0, c = 0;
    while (cin >> nom >> ornatish >> k1 >> k7 >> k30) {
        printf("%s: day 1 %.1f%%, day 7 %.1f%%, day 30 %.1f%%\n", nom.c_str(), foiz(k1, ornatish), foiz(k7, ornatish), foiz(k30, ornatish));
        n += ornatish; a += k1; b += k7; c += k30;               // total — weighted by the number of installs
    }
    printf("Total: day 1 %.1f%%, day 7 %.1f%%, day 30 %.1f%%\n", foiz(a, n), foiz(b, n), foiz(c, n));
    return 0;
}
```

Input:

```text
2026-01 2000 800 500 300
2026-02 2500 1100 700 400
2026-03 3000 1500 1050 690
```

Output:

```text
2026-01: day 1 40.0%, day 7 25.0%, day 30 15.0%
2026-02: day 1 44.0%, day 7 28.0%, day 30 16.0%
2026-03: day 1 50.0%, day 7 35.0%, day 30 23.0%
Total: day 1 45.3%, day 7 30.0%, day 30 18.5%
```

Retention is rising in every new cohort: 23% of those who installed in March are still using the app a month later (15% in January). This may show that changes introduced in March (for example, simpler registration or a first-purchase bonus) have worked. Looking only at the number of installs, this important improvement would have gone unnoticed.

## Mobile security

- Install apps only from official stores; check the permissions an app asks for (contacts, SMS, location).
- Never give SMS codes to anyone (S13); beware of fraud through screen-sharing apps.
- For developers: tokenization, certificate checking, storing data encrypted on the phone, biometric confirmation.

:::warning
**Common mistakes**

- **Measuring only the number of installs.** What matters is retention and active users.
- **Too many push notifications.** The user deletes the app.
- **Not including the app store commission in unit economics.**
- **Making the mobile version a "shrunken website".** Mobile users' needs are different.
:::

## Assignment

1. Compare two shopping or payment apps you use: registration steps, payment methods, push notifications, permissions.
2. Add an April cohort to the example program (3,200 installs, 1,700 / 1,200 / 800) and interpret the trend.
3. Choose a mobile channel for a small shop (native app, PWA, mini-program or bot) and justify it by costs and benefits.
4. Compare in a table the advantages of static and dynamic QR payments for the seller and the buyer.

## Summary

- Mobile commerce: always at hand, location, personal, camera, biometrics; the "mobile first" approach.
- Channels: native apps, mobile web, PWAs, super-app mini-programs, bots.
- Payments: NFC, static and dynamic QR codes, tokenization, digital wallets.
- Metrics: DAU/MAU, day-N retention, cohort analysis; the app store commission and CAC enter unit economics.

## Check yourself

:::check
Q: What is the difference between static and dynamic QR payments?
A: A static QR is permanent and the buyer enters the amount; a dynamic QR is generated with the amount for each purchase and confirmed automatically.
Q: Why is tokenization needed?
A: The wallet stores a token instead of the card number — even if it is stolen, the real card data is not exposed.
Q: What does DAU/MAU = 0.25 mean?
A: The average active user opens the app on about a quarter of the days in a month (≈7–8 days).
Q: Why is cohort analysis needed?
A: To compare the behaviour of groups of users who came at different times and see whether the product is improving.
:::

:::link https://www.gsma.com/r/mobileeconomy/
GSMA: The Mobile Economy
Reports on the mobile economy by country and region.
:::
