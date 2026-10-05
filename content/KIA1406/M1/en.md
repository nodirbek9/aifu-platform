---
title: Cybersecurity concepts and basic principles. Information security, confidentiality, integrity, availability. Threat, vulnerability, risk and controls
summary: The concepts of cybersecurity and information security; the CIA triad — confidentiality, integrity, availability; additional properties — authenticity, non-repudiation, accountability; asset, threat, vulnerability, exploit, risk; kinds of controls (administrative, technical, physical) and their functions (prevention, detection, recovery); defence in depth.
---

## Why it matters

Today a university's grade database, the money in a bank, a hospital's medical records and even the water supply system all live in computer systems. One successful attack can leak students' personal data, stop a service for days or cause losses of millions. Cybersecurity is not just "fighting hackers" but understanding what information is valuable, what threatens it and how much it is sensible to spend on protection. In this first lecture we meet the basic concepts of the subject — the "vocabulary" used throughout the course.

:::note
**After this lesson you will be able to:**

- tell the concepts of cybersecurity and information security apart;
- explain the CIA triad and give examples of each property being violated;
- distinguish asset, threat, vulnerability, exploit and risk;
- classify protective controls by kind and function;
- explain the principle of defence in depth.
:::

## Information security and cybersecurity

**Information security** is protecting information in any form (on paper, on a computer, in people's memory) from unauthorised access, modification, destruction and disclosure. **Cybersecurity** is its digital part: protecting computers, networks, programs and data from cyberattacks.

| | Information security | Cybersecurity |
|---|---|---|
| Object | information in any form | digital systems and data |
| Example | keeping a secret document in a safe | protecting a server with a firewall |
| Threats | theft, fire, an employee's mistake | malware, phishing, DDoS |

## The CIA triad

The three basic properties of information security — **CIA** (Confidentiality, Integrity, Availability):

| Property | Meaning | Example of violation | Protection |
|---|---|---|---|
| **Confidentiality** | data is open only to authorised people | the students' grade database leaked | encryption, access rights, authentication |
| **Integrity** | data is not changed without authorisation and unnoticed | the amount in a bank account was altered | hashes, digital signatures, change logs |
| **Availability** | an authorised user can use data and services when needed | a DDoS attack took the website down | backups, standby servers, DDoS protection |

The properties sometimes conflict: overly strict protection (say, a password every 5 minutes) raises confidentiality but lowers availability. Good security is a balance.

Additional properties:

- **Authenticity** — data or a user really comes from the claimed source.
- **Non-repudiation** — a sender cannot deny having sent a message (an electronic digital signature).
- **Accountability** — every action can be tied to a specific person (logs).

## Basic concepts

| Concept | Meaning | Example |
|---|---|---|
| **Asset** | something valuable to the organisation | the student database, a server, reputation |
| **Threat** | an event or actor that could harm an asset | a hacker, a virus, a fire, an employee's mistake |
| **Vulnerability** | a weakness in a system that a threat could use | unpatched software, a simple password, an open port |
| **Exploit** | a way or program for using a vulnerability | ready-made attack code for a known vulnerability |
| **Risk** | the likelihood and consequence of a threat using a vulnerability to cause harm | "data leaking through SQL injection in the database" |
| **Control** | a measure that reduces risk | a firewall, staff training, a lock |
| **Incident** | an actual security breach | unauthorised access to a server was detected |

They are linked: a **threat** acts on an **asset** through a **vulnerability**, and this gives rise to **risk**; **controls** close the vulnerability or reduce the consequence. A simplified formula: *Risk = Threat × Vulnerability × Impact*. With no vulnerability or no threat — no risk.

Sources of threats: **external** (hackers, criminal groups, state actors, competitors) and **internal** (disgruntled or careless employees); **deliberate** and **accidental**; **natural** (fire, flood, earthquake).

## Controls

**By kind**:

| Kind | Content | Examples |
|---|---|---|
| **Administrative** | policies, rules, training | a security policy, password rules, staff training |
| **Technical** (logical) | software and hardware means | encryption, firewall, antivirus, MFA |
| **Physical** | physical barriers | locks, access cards, cameras, a guard |

**By function**:

| Function | Goal | Example |
|---|---|---|
| **Preventive** | stop an incident from happening | a firewall, access rights |
| **Detective** | notice an incident | IDS, logs, a camera |
| **Corrective / recovery** | remove the consequences | restoring from backup, installing a patch |
| **Deterrent** | discourage an attacker | warning notices, penalties |
| **Compensating** | an alternative when the main measure is impossible | isolating an old system in a separate network |

**Defence in depth** means not relying on a single means: a firewall at the network edge, segmentation inside, antivirus on the computer, authentication in the application, encryption of the data, and awareness in people. If one layer is overcome, the next one holds.

:::warning
**Common mistakes**

- **Seeing security as a purely technical matter.** A large share of incidents start with the human factor (phishing, simple passwords, carelessness).
- **Mixing up threat and vulnerability.** A hacker is a threat; unpatched software is a vulnerability.
- **Thinking only of confidentiality.** Ransomware destroys availability completely, even without stealing data.
- **"Nobody will attack us."** Automated attacks target every system connected to the Internet.
:::

:::tip
Make a small table for your own digital life: 5 assets (phone, e-mail, banking app, social network, university account), and for each one threat, one vulnerability and one protective measure. Very often the strongest measure is two-step authentication and updates.
:::

## Summary

- Information security protects information in any form; cybersecurity is its digital part.
- CIA: confidentiality, integrity, availability; in addition — authenticity, non-repudiation, accountability.
- Asset, threat, vulnerability, exploit, risk, control, incident are the key terms.
- Controls: administrative, technical, physical; preventive, detective, corrective, deterrent, compensating.
- Defence in depth means several independent layers of protection.

## Check yourself

:::check
Q: Which CIA property means data is open only to authorised people?
A: Confidentiality.
Q: Which property is a DoS attack aimed at first of all?
A: Availability.
Q: Is unpatched software a threat or a vulnerability?
A: A vulnerability — a threat can make use of it.
Q: What kind of control is a CCTV camera by function?
A: Detective (it also has a deterrent effect); by kind it is physical.
Q: What provides non-repudiation?
A: An electronic digital signature.
:::

:::link https://www.nist.gov/cyberframework
NIST Cybersecurity Framework
An internationally recognised framework for managing cybersecurity.
:::
