---
title: The concept and tasks of e-government
summary: Independent study — the principles of e-government (user-centricity, "once-only", "digital by default", interoperability, openness, security); its tasks — service delivery, inter-agency cooperation, transparency, citizen participation; the legal framework (the Law "On E-Government" and others); base state registers; e-participation ("Open Budget", appeals portals); calculating the economic impact of e-government; an assignment.
---

## Purpose of this independent study

In M10 we got to know the models of e-government (G2C, G2B, G2G, G2E), its stages of development and the infrastructure in Uzbekistan. In this independent study we look more deeply at **which principles** e-government rests on, which **tasks** it solves and how its **economic impact** is calculated.

:::note
**After this independent study you will be able to:**

- explain the main principles of e-government;
- group its tasks;
- show the importance of base registers and the "once-only" principle;
- estimate the economic impact of an electronic service.
:::

## Definition

**E-government** is a system for organizing the interaction of government bodies with citizens, businesses and one another through information and communication technologies. The goal is not technology itself but making **public services fast, convenient, transparent and cheap**.

## Principles

| Principle | Meaning | In practice |
|---|---|---|
| **User-centricity** | a service is built around a citizen's life event, not the agency's structure | "Birth of a child" — birth certificate, allowance and registration in one application |
| **Once-only** | a citizen gives data to the state only once; agencies obtain the rest from each other | no certificates are demanded — the system requests them itself |
| **Digital by default** | a service is designed in electronic form first | paper is the exception, electronic the rule |
| **Interoperability** | agencies' systems work through common standards and an integration platform (MT15) | inter-agency data exchange |
| **Openness and transparency** | government data is published as open data | an open data portal, budget execution |
| **Security and privacy** | personal data is protected and access is logged | digital signatures, OneID, access logs (M14, S14) |
| **Inclusiveness** | all groups, including older people and people with disabilities, can use it | public service centres, a simple interface (MT19) |

## Tasks

| Group | Tasks |
|---|---|
| **Service delivery** | delivering public services electronically, accepting payments, issuing results electronically |
| **Internal efficiency** | electronic document management, inter-agency integration, eliminating repeated data collection |
| **Transparency and anti-corruption** | electronic public procurement, publishing budget execution, reducing the human factor |
| **Improving the business environment** | online registration, licences, tax reports (S15), electronic invoices |
| **Citizen participation** | appeals, petitions, discussing draft regulations, participatory budgeting |
| **Information for management** | data-driven decision-making, monitoring dashboards |

## The legal framework

In Uzbekistan e-government is governed by a number of laws: the Law "On E-Government" (2015), the laws "On Informatization", "On Electronic Digital Signatures", "On Electronic Document Management", "On Personal Data" and "On Cybersecurity", and the "Digital Uzbekistan — 2030" strategy (MT6). Check the current wording of these documents on Lex.uz.

## Base state registers

The "once-only" principle does not work without reliable **base registers** — they are the "source of truth" for all other systems (the master data idea from MT15 at national scale):

| Register | Main identifier |
|---|---|
| Population (individuals) | JSHSHIR (PINFL) |
| Legal entities and entrepreneurs | STIR |
| Real estate and land | the cadastral number |
| Addresses | the address code |
| Vehicles | the registration plate, VIN |

The registers are linked to each other through identifiers: a company (STIR) — its director (PINFL) — its building (cadastral number) — its address.

## E-participation

E-government is not only "service delivery" but also citizens' participation in governance:

- **Appeals portals** — accepting appeals from citizens and businesses and monitoring response deadlines.
- **A portal for discussing draft regulations** — submitting proposals on draft laws.
- **"Open Budget"** — participatory budgeting: citizens vote to choose which project (a road, a school, water supply) part of the local budget will be spent on.

## Calculating the economic impact

**Example**: a service for obtaining a certificate has been moved online. 200,000 applications a year.

| Indicator | Offline | Online |
|---|---|---|
| Citizen's time (travel, queue) | 3 hours | 10 minutes |
| Citizen's travel cost | 20,000 soums | 0 |
| Civil servant's time per application | 30 minutes | 5 minutes (checking) |

**Annual impact for citizens** (assuming one hour of time is worth 25,000 soums):

- time: 200,000 × (3 − 1/6) hours × 25,000 = 200,000 × 2.83 × 25,000 ≈ 14.2 billion soums;
- travel: 200,000 × 20,000 = 4 billion soums.

**For the state**: 200,000 × 25 minutes ≈ 83,300 hours of staff time are saved — a workload equal to roughly 45–50 full-time positions can be redirected to other tasks.

The project's worth is assessed by comparing this impact with the costs of building and supporting the system (TCO, M2). There are also benefits that are hard to measure in money: lower corruption risk, transparency, trust.

:::warning
**Common mistakes**

- **"Digitizing" a paper process unchanged.** The citizen still uploads 5 scanned documents — the "once-only" principle is broken.
- **Building a service around the agency's structure.** A citizen should not have to know which agency is responsible for what.
- **Forgetting inclusiveness.** Because of the digital divide (MT6), an offline channel and assistance must remain.
- **Measuring impact only by the number of services developed.** What matters is the take-up rate and the time saved.
:::

## Assignment

1. Choose one "life event" (starting a business, buying a home, the birth of a child) and list all the public services it requires. Which could be combined into one integrated service?
2. Show which base registers these services would draw data from.
3. For an electronic service you have used, estimate the economic impact as in the example above (take the number of applications from official statistics or estimate it).
4. Discuss the advantages and risks (for example, artificially inflated votes) of an e-participation tool such as "Open Budget".

## Summary

- E-government means using ICT to make public services fast, convenient, transparent and cheap.
- Principles: user-centricity, once-only, digital by default, interoperability, openness, security, inclusiveness.
- Tasks: services, internal efficiency, transparency, the business environment, citizen participation, management information.
- Base registers are the foundation of "once-only"; the impact is measured through time, costs and transparency.

## Check yourself

:::check
Q: What does the "once-only" principle mean?
A: A citizen gives data to the state only once; other agencies obtain it from each other through their systems.
Q: Why are base registers needed?
A: They are the single reliable source of data on the population, companies, property and addresses on which other systems rely.
Q: What form of e-participation is "Open Budget"?
A: Participatory budgeting — citizens vote to choose which project local budget funds will be spent on.
Q: How is the economic impact of an electronic service for citizens calculated?
A: The time saved (hours × the value of an hour) and travel costs are multiplied by the number of applications.
:::

:::link https://publicadministration.un.org/egovkb/
UN E-Government Knowledgebase
E-government principles, research and country rankings.
:::
