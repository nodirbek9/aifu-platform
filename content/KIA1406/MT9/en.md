---
title: Modern antivirus software and its capabilities. Cybercrime and cyberlaw
summary: From antivirus to EPP and EDR — modern endpoint protection; detection methods — signatures, heuristics, behaviour analysis, machine learning, cloud reputation, sandboxing; additional capabilities — web and mail filtering, ransomware protection, device control, central management; evaluating products — AV-TEST, AV-Comparatives; Windows Defender; the concept and types of cybercrime; the law of Uzbekistan — crimes in the field of computer information in the Criminal Code, the laws "On Cybersecurity" and "On Personal Data"; international instruments; what to do as a victim.
---

## Why does this topic matter?

The protection tool on a computer is the first line of defense for every user and organization. But today's "antivirus" differs greatly from the signature scanners of the 1990s: it watches behaviour, gets information from the cloud and responds to incidents. The second topic — cyberlaw — is the other side of protection: the law defines what is a crime, protects the victim and also sets boundaries for the specialist themselves (M3).

:::note
**After this assignment you will be able to:**

- explain the capabilities of modern endpoint protection (EPP, EDR);
- name the strengths and weaknesses of detection methods;
- evaluate antivirus products by the results of independent tests;
- know the types of cybercrime and the liability for them under the law of Uzbekistan;
- act correctly if you become a victim of cybercrime.
:::

## From antivirus to EPP and EDR

| Generation | Capabilities |
|---|---|
| **Classic antivirus** | a database of signatures of known malicious files, scanning on file open |
| **EPP** (Endpoint Protection Platform) | antivirus + host firewall, web filter, device (USB) control, application control, central management |
| **EDR** (Endpoint Detection and Response) | continuously records processes, network connections and file operations; detects suspicious chains; isolating the device from the network, stopping a process, data for investigation |
| **XDR / MDR** | combining endpoint, network, mail and cloud data; a service of external specialists (MDR) |

## Detection methods

| Method | Essence | Strength | Weakness |
|---|---|---|---|
| **Signatures** | hashes and byte sequences of known samples | precise, few false alerts | does not see new or modified samples |
| **Heuristics** | suspicious structure and instructions | finds modified samples too | false alerts |
| **Behaviour analysis** | what the program does: encrypting many files, changing system settings | effective against new threats | may trigger after the damage has started |
| **Machine learning** | a model trained on millions of samples | fast, generalizes to new samples | hard to explain, can be fooled |
| **Cloud reputation** | global information about a file or site | updated quickly | needs an Internet connection, a privacy issue |
| **Sandbox** | running a suspicious file in an isolated environment and observing it | seeing behaviour safely | slow, some samples detect the test environment |

Modern products **combine** these methods — none of them is enough on its own.

**Additional capabilities**: web and mail filtering (blocking phishing pages), ransomware protection (protected folders, for example Controlled Folder Access in Windows), device control, vulnerability scanning, a central console and reports.

**Windows Defender** (Microsoft Defender Antivirus) is built into Windows and shows high results in independent tests; it is often enough for a home user. Organizations choose corporate editions or products of other vendors (ESET, Kaspersky, Bitdefender, CrowdStrike, Sophos and others) for central management and EDR capabilities.

## Evaluating products

Rely not on advertising but on the results of **independent laboratories**:

| Laboratory | What it measures |
|---|---|
| **AV-TEST** (Germany) | protection, impact on system speed, usability (false alerts) |
| **AV-Comparatives** (Austria) | real-world protection, false alerts, performance, EDR tests |
| **SE Labs**, **MITRE ATT&CK Evaluations** | detection of attack chains, EDR effectiveness |

Comparison criteria: detection rate, number of false alerts, system slowdown, ease of management, price, support and the vendor's data handling policy.

**Important principles**: only **one** real-time antivirus on a computer (two interfere with each other); databases and the program update automatically; an antivirus does not replace updates, backups, least privilege and awareness.

## Cybercrime and cyberlaw

**Cybercrime** is a crime committed against computer systems or information, or with their help. The main types:

- **unauthorized access** to a computer system and unauthorized modification or destruction of information;
- creating and spreading malware;
- computer-assisted **fraud** and theft (bank cards, online scams);
- illegal collection and disclosure of personal data;
- extortion (including encrypting data and demanding a payment);
- copyright infringement, distributing prohibited content.

**The law of Uzbekistan** (main documents; full texts at lex.uz):

| Document | Content |
|---|---|
| **The Criminal Code** — the section "Crimes in the field of information technology" | liability for unauthorized access to computer information, malware, unauthorized modification of information and other acts |
| **The Law "On Cybersecurity"** (2022) | the cybersecurity system, the authorized body, requirements for critical information infrastructure, incident reporting |
| **The Law "On Personal Data"** (2019) | collection, storage and protection of personal data; the requirement to store the data of citizens of Uzbekistan within the country |
| **The Law "On Electronic Digital Signature"** | the legal status of digital signatures (M8) |
| **The Law "On Informatization"** | the foundations of protecting information resources and systems |

**International instruments**: the Budapest Convention on Cybercrime (Council of Europe, 2001) — a model for national laws and cooperation; the UN Convention against Cybercrime (adopted in 2024).

**If you become a victim of cybercrime**:

1. Contact the bank immediately (block the card, dispute the transaction).
2. Change passwords, enable MFA, check your accounts from another device.
3. Keep the evidence: messages, screenshots, dates, numbers, links — do not delete them.
4. File a report with law enforcement (the internal affairs bodies); in organizations, inform the security service and the authorized body (in Uzbekistan — the Cybersecurity Center).
5. Warn your contacts — messages may also have been sent to them from your account.

:::warning
**Common mistakes**

- **Installing two antivirus programs together.** They interfere with each other and slow the system down.
- **"I have an antivirus, so I am safe".** An antivirus does not fully protect against phishing, weak passwords and unpatched software.
- **Turning off the antivirus "for speed".** An unprotected system gets infected at the first mistake.
- **Cracked programs and key generators.** They are one of the most common ways of spreading malicious code, and illegal.
- **Not reporting cybercrime.** Reporting late reduces the chance of recovering money and finding the criminal.
:::

:::tip
To compare antivirus products in an essay, put the latest test results from the AV-TEST and AV-Comparatives websites in a table; take the texts of laws only from the current version at lex.uz and cite the articles precisely.
:::

## Summary

- Modern protection: antivirus → EPP → EDR → XDR/MDR; detection methods are combined.
- Signatures work against known threats; heuristics, behaviour analysis and machine learning against new ones; a sandbox for safe analysis.
- Products are evaluated by the results of independent laboratories (AV-TEST, AV-Comparatives).
- Cybercrime: unauthorized access, malware, fraud, personal data violations, extortion.
- In Uzbekistan: the Criminal Code and the laws "On Cybersecurity" and "On Personal Data"; as a victim — the bank, evidence, a report.

## Check yourself

:::check
Q: How does EDR differ from a classic antivirus?
A: It continuously monitors processes and connections, detects suspicious chains and provides response actions (isolation, stopping).
Q: What is the main drawback of signature-based detection?
A: It finds only known samples; it does not see new or modified ones.
Q: How can antivirus products be evaluated objectively?
A: By the test results of independent laboratories such as AV-TEST and AV-Comparatives.
Q: What is the first step after becoming a victim of bank card fraud?
A: Contact the bank immediately to block the card and dispute the transaction.
:::

:::link https://lex.uz/
Lex.uz — the National Database of Legislation of the Republic of Uzbekistan
The current texts of the Criminal Code, the Law "On Cybersecurity" and other laws.
:::
