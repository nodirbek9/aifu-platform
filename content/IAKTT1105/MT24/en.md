---
title: Features of using blockchain technology in various sectors
summary: Independent study — the specific requirements sectors place on blockchain (speed, confidentiality, legal force, integration); features in finance (settlement time, international remittances), trade finance (electronic bills of lading, letters of credit), agriculture (provenance tracking), state registers (Georgia's land registry experience), energy (guarantees of origin), healthcare and education (digital diplomas); the "oracle problem" and the link to the real world; a worked cost-benefit analysis; standards (ISO/TC 307); an assignment.
---

## Purpose of this independent study

In seminar S12 we saw where blockchain is used in various sectors and when it is needed. In this independent study we learn which **specific requirements** each sector places on blockchain and how the economic benefit of a project can be **calculated**.

:::note
**After this independent study you will be able to:**

- compare the requirements sectors place on blockchain;
- state precisely which problem blockchain solves in each sector;
- explain the "oracle problem";
- build a cost-benefit analysis of a blockchain project.
:::

## Sector requirements

| Requirement | Finance | Supply chain | State registers | Healthcare |
|---|---|---|---|---|
| **Speed** | very high (seconds) | medium | low–medium | medium |
| **Confidentiality** | very high (commercial and banking secrecy) | medium (prices are confidential) | openness matters, personal data closed | the highest (medical secrecy) |
| **Legal force** | regulators' requirements | international trade law | the record must be recognized by law | patient consent and the law |
| **Participants** | banks, the regulator | many companies and countries | government agencies, notaries | clinics, insurers, pharmacies |
| **Suitable network type** | permissioned (a consortium) | permissioned, with open verification | permissioned + public hashes | permissioned, hashes only |

## Features by sector

### Finance and payments

- **The problem**: settling securities transactions takes several days, and the parties spend a lot on reconciling their ledgers.
- **International remittances**: many families receive money from relatives abroad; the fee and time of a transfer are an important socio-economic issue. Stablecoins and interbank DLT networks try to cut costs, but currency control, anti-money-laundering requirements (AML/KYC) and licensing are mandatory.
- **The feature**: the regulator's participation and the ability to monitor transactions are needed.

### Trade finance

- **The electronic bill of lading** (eBL) — the digital form of the document that gives ownership of sea cargo; a paper bill of lading travels for weeks by courier.
- **Letter of credit** conditions (payment once documents are presented) can be automated with a smart contract.
- **The feature**: an electronic document must be legally recognized as equal to paper (for example, on the basis of the UNCITRAL Model Law on Electronic Transferable Records).

### Agriculture and food

- **Provenance tracking**: field → processing → export; buyers demand compliance with environmental and social standards (for example, cultivation without forced labour) (the cotton case in S12).
- **The feature**: data is entered in the field, often where the Internet is weak; a mobile app and a simple interface are needed; farmers' digital literacy (MT6).

### State registers

- **Georgia's experience** (since 2016): hashes of land and real estate documents have been written to a public blockchain — anyone can independently check that a register entry has not been changed later.
- **The feature**: blockchain confirms not the register itself but its **integrity**; legal force remains with the state register; personal data is not written to the blockchain.

### Energy

- **Guarantees of origin**: a digital certificate for 1 MWh of "green" electricity; blockchain guarantees it cannot be sold twice.
- **The feature**: data comes from smart meters (IoT) — the device's reliability matters.

### Healthcare

- Tracking medicine batches (against counterfeits), a log of patient consents.
- **The feature**: medical data itself is never on the blockchain — only hashes and the access log; strict confidentiality requirements.

### Education

- **Digital diplomas**: the university signs the diploma's hash and writes it to a register; an employer checks it in seconds (for example, Blockcerts, an open standard developed at MIT).
- **The feature**: the university's public key must be published in a trusted source; a mechanism for revoking a diploma is also needed.

## The "oracle problem"

A record inside a blockchain is immutable, but **how is it connected to the real world**? If a farmer writes "organic" or a meter reads wrongly, the blockchain stores this wrong data "forever" ("garbage in, garbage out" from S12). Solutions: certificates from independent laboratories and auditors, reliable IoT devices, data from several independent sources, liability for false data.

## Cost-benefit analysis: a worked example

**Project**: a trade finance document platform for a consortium of 6 banks. 12,000 operations a year.

| Costs (per year) | million soums |
|---|---|
| Supporting the platform and nodes | 1,800 |
| Integration (the start-up cost spread over 5 years) | 1,200 |
| Governance, audit, legal services | 600 |
| **Total cost** | **3,600** |

| Benefits (per year) | Calculation | million soums |
|---|---|---|
| Less manual document checking and reconciliation | 12,000 operations × 150,000 soums saved | 1,800 |
| Courier and paper costs | 12,000 × 40,000 soums | 480 |
| Lower losses from fraud (forged or duplicate documents) | estimated | 900 |
| Faster working capital (an operation is 5 days shorter) | financing costs saved | 1,100 |
| **Total benefit** | | **4,280** |

**Net annual benefit**: 4,280 − 3,600 = 680 million soums. The project pays off, but the margin is small: if only 4 banks take part and operations fall to 8,000, the benefits fall proportionally (to about 2,850 million) while most of the costs remain — the result turns into a loss. This is the **network effect** (MT6, MT22) in numbers: the number of participants is decisive.

## Standards and interoperability

- **ISO/TC 307** — the international standards committee for blockchain and DLT (terminology, security, interoperability).
- **Interoperability** between different blockchain networks and traditional systems is one of the main problems of sector projects: if every consortium builds its own "island", the benefit is limited.

:::warning
**Common mistakes**

- **Applying one solution to all sectors.** Confidentiality, speed and legal requirements differ sharply.
- **Ignoring the oracle problem.** Blockchain does not guarantee that entered data is true.
- **Counting only technological benefits.** The number of participants, legal recognition and integration costs are decisive.
- **Deciding without a sensitivity analysis.** How does the result change if participants or operations decrease?
:::

## Assignment

1. Choose one sector and, using the requirements table above, describe in detail its requirements for a blockchain system.
2. Build a cost-benefit table for one project in this sector (justify the figures).
3. Sensitivity analysis: how does the net benefit change if the number of participants falls by 30% or costs rise by 20%?
4. List the project's "oracle" sources and the ways to ensure their reliability.

## Summary

- Sectors place different requirements on blockchain: speed and confidentiality in finance, legal force in registers, strict confidentiality in healthcare.
- In every sector blockchain must solve a specific problem: reconciliation, paper documents, counterfeiting, confirming provenance.
- The oracle problem — the reliability of real-world data must be ensured separately.
- Economic evaluation — a cost-benefit and sensitivity analysis; the number of participants is decisive.

## Check yourself

:::check
Q: What does blockchain confirm in Georgia's land registry experience?
A: The integrity of the register entries — that they have not been changed later; legal force remains with the state register.
Q: What is the "oracle problem"?
A: The blockchain itself cannot guarantee that data entered from the real world is correct.
Q: Why is medical data itself not written to a blockchain?
A: Because of strict confidentiality requirements and because records cannot be deleted; only hashes and the access log are written.
Q: Which factor is often decisive in a cost-benefit analysis?
A: The number of participants and operations (the network effect).
:::

:::link https://www.iso.org/committee/6266604.html
ISO/TC 307 Blockchain and distributed ledger technologies
The international standards committee for blockchain.
:::
