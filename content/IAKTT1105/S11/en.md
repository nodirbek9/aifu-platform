---
title: Principles of implementing cloud technologies in various sectors
summary: Seminar — the principles and stages of moving to the cloud (assessment, planning, migration, optimization); migration strategies (the "6 Rs": rehost, replatform, refactor, repurchase, retain, retire); use by sector — education, healthcare, banking and finance, retail, public administration, small business; legal requirements (personal data localization); cost management (FinOps); a case and group tasks.
---

## Purpose of the seminar

In lecture M11 we covered cloud models and their economic aspects. In the seminar we analyze **how** an organization moves to the cloud — which stages, strategies and risks there are — and propose cloud solutions for different sectors.

:::note
**After this seminar you will be able to:**

- name the stages and principles of moving to the cloud;
- distinguish the "6 Rs" migration strategies;
- give examples of cloud use in different sectors;
- draw up a plan taking legal requirements and cost management into account.
:::

## Stages of moving to the cloud

| Stage | Meaning |
|---|---|
| **1. Assessment** | an inventory of existing systems and data, their importance and confidentiality level, a TCO calculation (M2) |
| **2. Planning** | which system moves when and with which strategy; choosing the model (SaaS, PaaS, IaaS) and the provider; security and backup requirements |
| **3. Migration** | a pilot project, data migration, testing, user training |
| **4. Optimization** | monitoring costs, right-sizing resources, automation |

**The "6 Rs" strategies**:

| Strategy | Meaning | Example |
|---|---|---|
| **Rehost** ("lift and shift") | the system moves unchanged to a virtual server (IaaS) | an old accounting server to a cloud VM |
| **Replatform** | with small adaptations | a managed cloud DBMS instead of your own |
| **Refactor** | rewriting for the cloud | an application into microservices and containers |
| **Repurchase** | replacing with SaaS | Microsoft 365 or Google Workspace instead of your own mail server |
| **Retain** | leaving it where it is for now | confidential systems required by law |
| **Retire** | switching off what is unneeded | old applications nobody uses |

## Use by sector

| Sector | Cloud solutions | Result |
|---|---|---|
| **Education** | LMS platforms, video lessons, online office, exam systems | distance learning, adapting to the load during exam sessions |
| **Healthcare** | electronic medical records, telemedicine, image archives | data available in any clinic; confidentiality is a strict requirement |
| **Banking and finance** | the mobile banking back end, scoring and analytics, fraud detection | scaling; often a private or hybrid cloud because of regulators' requirements |
| **Retail** | an online shop, marketplace integration, online cash registers, CRM | quickly adding resources at seasonal peaks ("Black Friday") |
| **Public administration** | public service portals, inter-agency platforms, government clouds | centralized costs, fast implementation |
| **Small business** | SaaS accounting, CRM, mail, website builders | professional tools without an initial investment |

## Legal requirements and security

- **Personal data localization**: the personal data of citizens of Uzbekistan must be stored on servers within the country (the Law "On Personal Data") — check this when using a foreign cloud.
- **Sector requirements**: the Central Bank's requirements for banks, special protection for medical data.
- **Shared responsibility**: the customer is responsible for access rights, encryption, backups and settings (M11, MT21).
- **The contract (SLA)**: uptime, returning data, the procedure for leaving the provider.

## Cost management (FinOps)

In the cloud costs depend on usage, so they must be managed continuously: budgets and alerts, switching off unused resources, right-sized servers, discounts for long-term reservations, tagging costs by department. This practice is called **FinOps** — finance and IT jointly manage cloud costs.

## Case: the "Ilm" private university

The university has 3,000 students. Currently: an old student records system on its own server, a separate mail server, classes only in lecture rooms, and the website "freezes" during exam sessions. Management is considering moving to the cloud.

**Group tasks**:

1. Choose and justify a "6 Rs" strategy for each system (student records, mail, website, a new LMS).
2. Which of SaaS, PaaS and IaaS suits which system?
3. Which requirements and measures are needed for personal data (students, grades, passport data)?
4. Draw up a step-by-step plan: a pilot, deadlines, training, risks.
5. How can costs be monitored and reduced?

:::warning
**Common mistakes**

- **Moving everything at once.** A big risk without a pilot project.
- **Moving an old system unchanged and expecting cloud benefits.** "Rehost" is fast but not always cheap.
- **Ignoring the localization requirement.** A breach of the law and fines.
- **Not drawing up an exit plan.** If the provider has to be changed, how can the data be retrieved?
:::

:::tip
For one organization you know (a shop, a training centre, a clinic), show in a table which systems could be moved to the cloud and which strategy fits each.
:::

## Summary

- Moving to the cloud: assessment → planning → migration (with a pilot) → optimization.
- The "6 Rs": rehost, replatform, refactor, repurchase, retain, retire.
- Sectors: education, healthcare, banking, retail, government, small business — each has its own requirements.
- Legal requirements (localization, sector regulations), shared responsibility and the SLA are taken into account.
- Costs are managed continuously through FinOps practice.

## Check yourself

:::check
Q: What does the "repurchase" strategy mean?
A: Replacing your own system with a ready SaaS product, for example Microsoft 365 instead of your own mail server.
Q: Why do banks often choose a private or hybrid cloud?
A: Because of regulators' requirements and the need to protect confidential data.
Q: What does the personal data localization requirement mean?
A: The personal data of citizens of Uzbekistan must be stored on servers within the country.
Q: What is FinOps?
A: The practice of finance and IT jointly monitoring and optimizing cloud costs.
:::

:::link https://aws.amazon.com/cloud-migration/
AWS: Cloud Migration
Materials on the stages and strategies of cloud migration.
:::
