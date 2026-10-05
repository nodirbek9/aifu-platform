---
title: Cloud technologies and working with them
summary: The concept of cloud computing and its five essential characteristics according to NIST; service models — IaaS, PaaS, SaaS (and FaaS); deployment models — public, private, hybrid, community cloud; major providers (AWS, Microsoft Azure, Google Cloud) and local data centres; economic aspects — moving from CAPEX to OPEX, "pay as you go", scaling; the shared responsibility model; advantages and risks (data location, provider dependence, Internet dependence); hands-on work with cloud office services.
---

## Why does this topic matter?

Today both a small shop and a large bank use cloud services: mail and documents (Microsoft 365, Google Workspace), accounting and CRM (SaaS), servers for websites and apps. The cloud lets a company get resources when needed and pay only for what it uses, without buying its own servers. For an economist the key question is when the cloud is profitable and what risks it carries.

:::note
**After this lesson you will be able to:**

- explain the concept and essential characteristics of cloud computing;
- distinguish the IaaS, PaaS and SaaS models;
- compare public, private and hybrid clouds;
- assess the economic advantages and risks of the cloud;
- explain the shared responsibility model.
:::

## The concept of cloud computing

**Cloud computing** is a model of on-demand, fast and easy access over a network to computing resources (servers, storage, programs). According to NIST, there are five essential characteristics:

| Characteristic | Meaning |
|---|---|
| **On-demand self-service** | the customer orders resources themselves, without an operator |
| **Broad network access** | from any device over the Internet |
| **Resource pooling** | the provider's resources are shared among many customers |
| **Rapid elasticity** | expands when the load grows, shrinks when it falls |
| **Measured service** | usage is metered and paid for accordingly |

## Service models

| Model | What the provider supplies | What the customer manages | Example |
|---|---|---|---|
| **IaaS** (infrastructure) | virtual servers, storage, networking | the OS, programs, data | AWS EC2, Azure Virtual Machines |
| **PaaS** (platform) | the OS, databases, a runtime environment | its own application and data | Google App Engine, Azure App Service, managed DBMSs |
| **SaaS** (software) | a ready program via a browser or app | only data and settings | Microsoft 365, Google Workspace, Salesforce, online accounting |
| **FaaS** (functions) | code runs only when an event arrives | code functions | AWS Lambda |

A simple analogy — "pizza as a service": you bake it yourself (your own server), you rent ready dough and an oven (IaaS), you order delivery and eat at home (PaaS), you eat at a restaurant (SaaS).

## Deployment models

| Model | Description | For whom |
|---|---|---|
| **Public cloud** | the provider's infrastructure serves many customers | start-ups, small and medium businesses |
| **Private cloud** | for one organization only (on its premises or at a provider) | banks, government bodies, confidential data |
| **Hybrid cloud** | public and private together | confidential data in the private part, peak load in the public part |
| **Community cloud** | for a group of organizations with common requirements | government agencies, medical institutions |

**Providers**: global — Amazon Web Services, Microsoft Azure, Google Cloud; local — data centres in Uzbekistan and the cloud services of telecom operators. Under the Law "On Personal Data", the personal data of citizens of Uzbekistan must be stored on servers within the country — this affects the choice of provider.

## Economic aspects

| Advantage | Comment |
|---|---|
| **CAPEX → OPEX** | a monthly fee instead of a large initial investment (M2) |
| **Pay as you go** | with a seasonal load (for example "Black Friday") you pay only for the extra resources |
| **Fast start** | a server is ready in minutes, not months |
| **Scaling** | resources are increased as the business grows |
| **Lower staff costs** | the provider manages the hardware and the data centre |

| Risk | Comment |
|---|---|
| **More expensive in the long run** | with a constant heavy load your own server may be cheaper (the TCO example in M2) |
| **Provider dependence** (vendor lock-in) | moving to another provider is hard and expensive |
| **Internet dependence** | if the connection fails, so does the service |
| **Data location and privacy** | legal requirements, third-party access |
| **Cost control** | forgotten resources raise the bill — budget alerts are needed |

## The shared responsibility model

In the cloud, security is **shared between the provider and the customer**: the provider protects the infrastructure (buildings, hardware, network, virtualization), while the customer protects its own data, users and access rights, and settings. In SaaS the customer's share is the smallest, in IaaS the largest. Many data leaks happen not because of the provider but because of a **customer's misconfiguration** (for example storage left open to everyone) — in detail in MT21.

## Practical work: a cloud office

1. In Google Workspace or Microsoft 365 (OneDrive) create a folder and open a spreadsheet and a document in it.
2. Give a partner **view-only** access and another one **edit** access; see the difference between sharing via a link and sharing with a specific person.
3. Edit the spreadsheet together and restore an earlier state from the **version history**.
4. Enable two-step authentication on your account.

:::warning
**Common mistakes**

- **"It's in the cloud, so it's secure and backed up".** The customer is responsible for data and access rights; check the backup policy.
- **Sharing confidential files in "anyone with the link" mode.** If the link spreads, everyone can read it.
- **Not monitoring resources and costs.** Unused servers also cost money.
- **Not thinking about an exit strategy.** Know in advance how and at what cost data can be moved elsewhere.
:::

:::tip
In the providers' pricing calculators (for example the AWS Pricing Calculator) estimate the monthly cost of a small website and compare it with the cost of your own server using the TCO method from M2.
:::

## Summary

- Cloud computing is on-demand resources over a network: self-service, broad access, resource pooling, elasticity, measured service.
- Service models: IaaS, PaaS, SaaS (and FaaS); deployment: public, private, hybrid, community.
- Economically: CAPEX → OPEX, pay as you go, a fast start; risks — higher long-run cost, dependence, privacy.
- Shared responsibility: the provider handles the infrastructure, the customer handles data, access and settings.

## Check yourself

:::check
Q: Which service model means using a ready program in a browser on a subscription basis?
A: SaaS.
Q: Which model means renting virtual servers and storage?
A: IaaS.
Q: What is a private cloud?
A: Cloud infrastructure dedicated to a single organization.
Q: What is the customer responsible for in the shared responsibility model?
A: Its own data, users and access rights, and settings.
:::

:::link https://csrc.nist.gov/pubs/sp/800/145/final
NIST SP 800-145: The NIST Definition of Cloud Computing
The official definition of cloud computing and its service and deployment models.
:::
