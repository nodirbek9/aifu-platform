---
title: The concept and necessity of information security
summary: Independent study — information as an asset and its classification (public, internal, confidential, strictly confidential); why security is necessary — economic, legal, reputational and continuity reasons; what an incident costs; why organizations underspend on security (externalities, information asymmetry, "it won't happen to us"); the Gordon–Loeb model; key principles — defence in depth, least privilege, security by design; security culture; cyber insurance; an assignment.
---

## Purpose of this independent study

In M14 we saw the methods of ensuring information security and assessing risks through ALE and ROSI. This independent study answers a different question: **why** is information security necessary for an organization, why do many organizations not allocate enough money to it, and how much is it reasonable to spend?

:::note
**After this independent study you will be able to:**

- classify information as an asset;
- name the four groups of reasons why security is necessary;
- explain what the cost of an incident consists of;
- know the reasons for underinvestment in security and the Gordon–Loeb model.
:::

## Information is an asset

Much of a company's value now lies not in buildings and equipment but in **information**: the customer base, technologies, contracts, financial data, the brand. So information, like other assets, is inventoried, given an owner and protected according to its value.

| Category | Meaning | Example | Protection |
|---|---|---|---|
| **Public** | may be published | price lists, press releases | integrity (must not be falsified) |
| **Internal** | for employees only | internal guidelines, the phone list | must not leave the organization |
| **Confidential** | a limited circle | customers' personal data, contracts, salaries | access rights, encryption, logs |
| **Strictly confidential** | a very narrow circle | merger plans, keys, trade secrets | the strongest measures, separate systems |

Classification determines how much protection each piece of information needs — protecting everything equally strongly is expensive and unnecessary.

## Why it is necessary: four groups of reasons

| Reason | Meaning | Example |
|---|---|---|
| **Economic** | direct financial losses | a fraudulent payment, stopped sales, recovery costs |
| **Legal** | legal requirements and liability | a fine for a personal data leak, the banking regulator's requirements |
| **Reputational** | the trust of customers and partners | customers leaving after a data leak, a falling share price |
| **Continuity** | the uninterrupted operation of the business | being unable to work for several days because of ransomware |

## The cost of an incident

The cost of a security incident is much larger than its visible part:

- **Direct**: stolen money, investigation and recovery services, new equipment.
- **Downtime**: revenue lost for the hours not worked, salaries.
- **Legal**: fines, court costs, compensation to customers.
- **Notification**: informing affected customers, a call centre.
- **Reputation and customers**: customers who leave, a higher cost of acquiring new customers, partners' trust.

International studies measure the average cost of a data breach in millions of dollars; it varies with the organization's size and sector, and is usually highest in healthcare and finance.

## Why is too little spent on security?

| Reason | Meaning |
|---|---|
| **"It won't happen to us"** | the risk of low-probability events is underestimated (behavioural economics) |
| **An invisible result** | successful protection means "nothing happened"; the cost is visible, the benefit is not |
| **Externalities** | part of the damage is borne by others (customers, partners) — the company does not fully account for it |
| **Information asymmetry** | the buyer cannot check a product's security — the seller has little incentive to spend on it |
| **Short-term thinking** | the cost is today, the benefit in an uncertain future |

That is why the state intervenes through **regulation** (mandatory requirements, the obligation to report incidents) and **standards** (ISO/IEC 27001).

## How much is reasonable: the Gordon–Loeb model

According to the model of L. Gordon and M. Loeb (2002), the optimal investment in protecting a given set of information **does not exceed about 37% (1/e) of the expected loss** — and is often much less.

**Example**: if the customer base leaks, the damage is 2 billion soums; the probability of a leak is 10% a year. The expected annual loss = 2 billion × 0.1 = 200 million soums. According to the model, spending **more than 74 million soums** a year on protecting this base is not economically justified (200 × 0.37). This is an upper bound: specific measures are chosen through ROSI as in M14.

:::tip
The Gordon–Loeb model is simple but gives an important conclusion: there is no need to spend "without limit" on security, but not zero either — the investment should be proportionate to the value of and risk to the information being protected.
:::

## Key principles

| Principle | Meaning |
|---|---|
| **Defence in depth** | several independent layers of protection: if one fails, another stops the threat (password + MFA + logs + backups) |
| **Least privilege** | everyone gets only the rights they need (M14, MT21) |
| **Security by design** | security is built in when a system is designed, not "patched on" later |
| **Separation of duties** | one employee cannot both create and approve a payment |
| **The weakest link** (MT15) | a system's protection is at the level of its weakest point |

## Security culture

Many incidents start with human error and social engineering (M14). So beyond technical measures a **culture** is needed: management setting an example, regular short training sessions, phishing exercises, not punishing those who report incidents ("I made a mistake — I reported it at once" is the best outcome), seeing security as every employee's job.

## Cyber insurance

**Cyber insurance** is a policy covering the costs of a cyber incident (recovery, downtime, legal costs). It is a way of **transferring** risk (M14). Insurers usually require minimum measures (MFA, backups, updates) — so insurance also gives an incentive to improve security. But insurance cannot fully restore lost reputation and customer trust.

:::warning
**Common mistakes**

- **Seeing security as only the IT department's job.** It is a business risk and every employee's responsibility.
- **Protecting all information equally.** Use classification to direct resources where they matter.
- **Measuring the cost of an incident only by the direct loss.** Downtime and reputation often cost more.
- **Thinking insurance replaces security.** It covers only part of the financial damage.
:::

## Assignment

1. For an organization you know (a university, a shop, a bank branch), list 10 information assets and divide them into the four categories.
2. For one of them, roughly estimate the components of the cost of an incident.
3. Using the Gordon–Loeb model, find the upper bound of investment for protecting this asset.
4. Propose 5 specific measures to strengthen the security culture in the organization.

## Summary

- Information is an asset; it is divided into public, internal, confidential and strictly confidential categories.
- The necessity of security: economic, legal, reputational and continuity reasons.
- The cost of an incident is much larger than the direct loss; the reasons for underinvestment are behaviour, an invisible benefit, externalities, asymmetry.
- Gordon–Loeb: the optimal investment does not exceed ~37% of the expected loss.
- Principles: defence in depth, least privilege, security by design, separation of duties; culture and cyber insurance complement them.

## Check yourself

:::check
Q: Why is classifying information necessary?
A: To apply protection at a level suited to each category and direct resources to the most important information.
Q: Name one economic reason for underinvestment in security.
A: Externalities — part of the damage is borne not by the company but by customers and partners.
Q: What does the Gordon–Loeb model show?
A: That the optimal investment in protecting information does not exceed about 37% of the expected loss.
Q: What is the defence-in-depth principle?
A: Several independent layers of protection — if one fails, another stops the threat.
:::

:::link https://www.enisa.europa.eu/
ENISA — the European Union Agency for Cybersecurity
Reports on cybersecurity threats and recommendations.
:::
