---
title: A test and case study on information security concepts
summary: Applying the M1 concepts in practice — identifying CIA properties, assets, threats, vulnerabilities, risks and controls in real situations; the case analysis method; six short cases (a university, a bank, a hospital, an online shop, a government service, a social network) with sample solutions; a task for independent analysis; the final test.
---

## Why it matters

Memorising concepts is easy, but quickly answering "which property was violated here, where is the vulnerability and what measure was needed?" for a real incident is a skill. This is exactly a security specialist's daily work: reading the description of an incident or project, translating it into the language of concepts and proposing a decision. In this session we practise that skill on cases.

:::note
**After this session you will be able to:**

- pick out the asset, threat, vulnerability and consequence from the description of a real situation;
- determine which CIA property was violated in each incident;
- propose preventive, detective and recovery measures for each case;
- write up a case analysis in a structured form.
:::

## The case analysis method

Analyse every case in the same order:

| Step | Question |
|---|---|
| 1. Asset | What should have been protected? What is it worth? |
| 2. Threat | Who or what caused the harm? Internal/external, deliberate/accidental? |
| 3. Vulnerability | Which weakness opened the way to the incident? |
| 4. Violated property | Confidentiality, integrity or availability (there may be several)? |
| 5. Consequence | Financial, legal, reputational, operational harm |
| 6. Measures | Preventive, detective, recovery; administrative, technical, physical |

## Cases and sample solutions

:::case
**Case 1. A university.** A teacher meant to send the grade spreadsheet to their personal e-mail but picked the wrong address and sent it to the mailing list of the whole student group. The spreadsheet held all the students' grades and phone numbers.

**Sample solution.** Asset — the students' personal data and grades. Threat — internal, accidental (an employee's mistake). Vulnerability — the practice of sending confidential data unencrypted by e-mail, no data classification policy. Violated property — **confidentiality**. Measures: rules for classifying and sending data (administrative), a DLP system and a warning when sending to external addresses (technical), staff training.
:::

:::case
**Case 2. A bank.** An attacker sent a customer an SMS in the bank's name saying "your card has been blocked" and lured them to a fake site. The customer entered their card details and the SMS code, and money was withdrawn from the account.

**Sample solution.** Asset — the customer's money and card details. Threat — external, deliberate (phishing/smishing). Vulnerability — the customer's lack of awareness, confirmation relying on an SMS code alone. Violated properties — **confidentiality** (card details) and **integrity** (the account state changed without authorisation). Measures: warning customers, a suspicious-transaction detection system, push confirmation in the app, blocking fake domains.
:::

:::case
**Case 3. A hospital.** Ransomware encrypted the server's files; work with patient records stopped for 3 days. The backups were kept on the second disk of the same server and were encrypted too.

**Sample solution.** Asset — the medical records and access to them. Threat — external, deliberate (ransomware). Vulnerabilities — backups not kept separately, probably an unpatched system or a phishing e-mail. Violated property — **availability** (and, if data was exfiltrated, confidentiality). Measures: backups by the 3-2-1 rule (one copy offline), updates, mail filtering, a recovery plan and testing it.
:::

:::case
**Case 4. An online shop.** Through an SQL injection vulnerability in the price database, an attacker changed the prices of several items to 1 soʻm and bought them.

**Sample solution.** Asset — the price database and revenue. Threat — external, deliberate. Vulnerability — program code that does not validate input. Violated property — **integrity**. Measures: parameterised queries, a WAF, security code review, logging price changes and alerting on sharp changes.
:::

:::case
**Case 5. A government services portal.** On the last day for submitting reports, the portal was unreachable for 6 hours because of a DDoS attack.

**Sample solution.** Asset — the portal service. Threat — external, deliberate. Vulnerability — no DDoS protection, a single data centre. Violated property — **availability**. Measures: DDoS filtering at the provider, a CDN, a standby data centre, a procedure for extending the deadline (administrative compensation).
:::

:::case
**Case 6. A social network.** A student used the same password on every site. After a small forum's data leaked, an attacker used that password to get into his social network account and sent his friends messages asking for money.

**Sample solution.** Asset — the account and the person's reputation. Threat — external, deliberate (credential stuffing). Vulnerability — password reuse, no MFA. Violated properties — **confidentiality** (messages), **integrity** (messages in his name), **authenticity**. Measures: a separate password for each site (a password manager), MFA, login notifications.
:::

## Task for independent analysis

Analyse the following three cases in table form (6 steps):

1. After an employee left, their account was not deleted for 2 months; they logged in from home and copied the customer database.
2. The air conditioner in the server room broke, the servers overheated and shut down; the website was down for 10 hours.
3. A teacher left their laptop on a bus; the disk held unencrypted exam questions.

:::warning
**Common mistakes**

- **Seeing only one property in each incident.** Often several are violated at once (phishing — confidentiality and integrity).
- **Writing a threat instead of a vulnerability.** "A hacker" is a threat; "an unpatched server" is a vulnerability.
- **Proposing only technical measures.** In many cases the main measure is administrative (a rule, training, a process).
- **Thinking only of preventive measures.** A detection and recovery plan is needed too: 100% prevention is impossible.
:::

:::tip
Find one real cyber incident from the news in the past month and analyse it with the 6-step table. Give the source and argue which measure could have prevented the incident most cheaply.
:::

## Summary

- Case analysis: asset → threat → vulnerability → violated property → consequence → measures.
- Several CIA properties may be violated in one incident; consider the additional properties (authenticity, non-repudiation) too.
- Measures are chosen in a balance of the three kinds (administrative, technical, physical) and the three functions (prevention, detection, recovery).

## Check yourself

:::check
Q: Which property is violated first of all in case 3, and what is the most important recovery measure?
A: Availability; a separate (offline) backup and a tested recovery plan.
Q: What is the vulnerability in case 4?
A: Program code that does not validate user input (SQL injection).
Q: What is the attack that exploits password reuse called?
A: Credential stuffing — trying leaked logins and passwords on other sites.
Q: Not deleting the account of an employee who has left breaks which policy?
A: The access control policy (revoking rights in time).
Q: Which measure protects the data when a laptop is lost?
A: Full disk encryption (for example, BitLocker).
:::

:::link https://www.cisa.gov/topics/cybersecurity-best-practices
CISA: Cybersecurity Best Practices
Practical advice from the US cybersecurity agency.
:::
