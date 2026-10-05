---
title: Threats to the security of information and communication technologies. Types of malicious programs
summary: Classification of threats — by source (natural, technological, human), intent (accidental, deliberate), location (internal, external); threat sources (actors) — state-linked groups, organized criminals, hacktivists, insiders, inexperienced attackers; the ENISA threat landscape — ransomware, malware, social engineering, threats against data, threats against availability, information manipulation, supply chain attacks; types of malicious programs by purpose and mobile malware; the basics of threat modelling (STRIDE).
---

## Why does this topic matter?

Planning protection starts with the question "whom and what are we protecting against?" (risk = threat × vulnerability × impact, M2). A small shop and a government ministry are not attacked by the same attackers, and their goals and capabilities differ too. A systematic classification of threats and knowledge of the current threat landscape make it possible to spend limited resources on the most important areas.

:::note
**After this assignment you will be able to:**

- classify threats by source, intent and location;
- name the main threat actors and their goals;
- know the main threats of the ENISA landscape;
- group malicious programs by purpose and name mobile threats;
- identify the threats to a simple system with the STRIDE model.
:::

## Classification of threats

| Criterion | Types | Examples |
|---|---|---|
| **Source** | natural, technological, human | an earthquake; a power outage; an attacker or an employee who made a mistake |
| **Intent** | accidental, deliberate | an email sent to the wrong person; a targeted attack |
| **Location** | internal, external | an insider; an attacker on the Internet |
| **Violated property** | confidentiality, integrity, availability | a data leak; falsification; a service outage |

## Threat actors

| Actor | Goal | Capabilities |
|---|---|---|
| **State-linked groups** (APT) | espionage, sabotage, political influence | very high: long-term, targeted, their own tools |
| **Organized criminals** | money: extortion, fraud, selling data | high; the "crime as a service" market (MT11) |
| **Hacktivists** | ideological or political demonstration | medium: DDoS, website defacement, data leaks |
| **Insiders** | revenge, money, negligence | have legitimate access to systems |
| **Inexperienced attackers** ("script kiddies") | curiosity, reputation | low: ready-made tools, simple vulnerabilities |
| **Competitors** | industrial espionage | varies |

Knowing the threat actor helps choose the level of protection: for an ordinary organization the most likely threats are untargeted mass attacks and fraud; for critical infrastructure — targeted APTs.

## The ENISA threat landscape

The European Union Agency for Cybersecurity (ENISA) publishes the main threats every year. Recent reports consistently list:

| Threat | Meaning |
|---|---|
| **Ransomware** | extortion by encrypting and stealing data; the largest financial losses |
| **Malware** | Trojans, spyware, botnets and others (MT4) |
| **Social engineering** | phishing, phone scams, business email compromise (BEC) (MT8, MT16) |
| **Threats against data** | data leaks and breaches, misconfigured cloud storage |
| **Threats against availability** | DDoS and Internet service outages |
| **Information manipulation** | disinformation, deepfakes, fake content |
| **Supply chain attacks** | getting into many customers through a supplier or a software update (for example, SolarWinds, 2020) |

## Types of malicious programs

MT4 gives a detailed classification by propagation method. Grouping by **purpose**:

| Purpose | Types |
|---|---|
| **Making money** | ransomware, banking Trojans, cryptominers, adware |
| **Espionage and data theft** | spyware, keyloggers, information stealers (infostealers) |
| **Taking control** | remote access Trojans, bots, rootkits |
| **Destruction and sabotage** | wipers, malware against industrial systems |

**Mobile malware**: apps installed from unofficial sources (APK files), apps that request excessive permissions (SMS, accessibility), banking Trojans that intercept SMS codes, fake "updates" or "banking apps". Protection: official stores only, checking app permissions, OS updates, not installing apps from unknown links.

## Threat modelling: STRIDE

When a new system is designed, threats are identified systematically with the **STRIDE** model:

| Letter | Threat | Violated property | Example of protection |
|---|---|---|---|
| **S** — Spoofing | acting as another person | authenticity | MFA, certificates |
| **T** — Tampering | modifying data | integrity | hash, digital signature, MAC |
| **R** — Repudiation | denying an action | non-repudiation | logs, digital signatures |
| **I** — Information disclosure | data leakage | confidentiality | encryption, access control |
| **D** — Denial of service | stopping a service | availability | redundancy, rate limiting |
| **E** — Elevation of privilege | gaining more rights | authorization | least privilege, updates |

Practical exercise: for the "view grades" function of a university platform, write one threat and one protective measure for each STRIDE letter.

:::warning
**Common mistakes**

- **"Nobody targets us".** Many attacks are untargeted and automated: weak systems are found by themselves.
- **Considering only external threats.** Insiders and staff mistakes account for a large share.
- **Trusting suppliers blindly.** The supply chain is an important route of modern attacks; set security requirements in contracts.
- **Leaving mobile devices unprotected.** A phone holds banking, mail and MFA keys.
- **Using outdated statistics.** The threat landscape changes every year — use the latest reports.
:::

:::tip
For an essay, open the latest ENISA Threat Landscape report and analyze its list of main threats using your own organization (for example, the university) as an example: which threats are most likely, and which measures already exist?
:::

## Summary

- Threats are classified by source, intent, location and violated property.
- Actors: state groups, organized criminals, hacktivists, insiders, inexperienced attackers — with different goals and capabilities.
- The ENISA landscape: ransomware, malware, social engineering, threats against data and availability, information manipulation, the supply chain.
- Malicious programs by purpose: money, espionage, control, sabotage; mobile threats need special attention.
- STRIDE is a method for systematically identifying threats at the design stage.

## Check yourself

:::check
Q: How do APT groups differ from ordinary criminals?
A: They are usually state-linked, carry out long-term targeted attacks and have high resources.
Q: What is a supply chain attack?
A: Getting into a supplier's many customers by compromising the supplier or a software update.
Q: Which threat does the "R" in STRIDE stand for, and what measure counters it?
A: Repudiation — denying an action; logs and digital signatures.
Q: What is a common route for mobile malware?
A: Apps installed from unofficial sources that request excessive permissions.
:::

:::link https://www.enisa.europa.eu/topics/cyber-threats/threat-landscape
ENISA Threat Landscape
The annual threat reports of the European Union Agency for Cybersecurity.
:::
