---
title: Ensuring information security in cloud technologies
summary: Independent study — the main risks in the cloud (misconfiguration, compromised accounts, data leaks, provider lock-in, data location); the shared responsibility model in IaaS, PaaS and SaaS; identity and access management (IAM, MFA, least privilege, roles); encryption at rest and in transit, key management (KMS, BYOK); logs and monitoring; backup and recovery; compliance (ISO/IEC 27001, 27017, 27018, SOC 2, localization); a security checklist for choosing a provider; an incident response plan.
---

## Purpose of this independent study

In M14 we saw the methods of information security, and in M11 and S11 moving to the cloud. Security in the cloud has its own features: the servers are at the provider, but responsibility for data and settings often stays with the customer. This independent study shows what an organization must control to use the cloud **safely**.

:::note
**After this independent study you will be able to:**

- name the main risks in the cloud;
- determine who is responsible for what in the different service models;
- explain access management, encryption and monitoring measures;
- assess a provider from a security point of view.
:::

## The main risks in the cloud

| Risk | Meaning | Example |
|---|---|---|
| **Misconfiguration** | the most common cause: a service left open | a folder with customer files in "public" mode |
| **Compromised accounts** | a password stolen or guessed, no MFA | data accessed through an administrator account |
| **Excessive permissions** | an employee or application has more rights than needed | an intern with permission to delete the whole database |
| **Data leaks** | confidential data becomes visible without authorization | an unencrypted backup |
| **Provider lock-in** | it is hard to move data elsewhere | proprietary formats, high exit costs |
| **Data location** | which country the data is stored in | the personal data localization requirement (S11) |
| **Service outage** | the provider or the Internet stops working | a failure in one region |

:::tip
Most cloud incidents happen not because the provider was "breached" but because of **the customer's own settings**. So "the cloud is secure" does not mean "our data is automatically secure".
:::

## The shared responsibility model

| Who is responsible? | IaaS | PaaS | SaaS |
|---|---|---|---|
| Physical data centre, equipment | provider | provider | provider |
| Virtualization, network infrastructure | provider | provider | provider |
| Operating system and its updates | **customer** | provider | provider |
| Application | **customer** | **customer** | provider |
| Access rights and accounts | **customer** | **customer** | **customer** |
| Data and its classification | **customer** | **customer** | **customer** |
| Settings (sharing, exposure) | **customer** | **customer** | **customer** |

In every model **data, access rights and settings** remain the customer's responsibility.

## Identity and access management (IAM)

- **Individual identity**: every employee has a personal account; there is no shared "admin" account.
- **Multi-factor authentication** (MFA) — mandatory, especially for administrators (M14).
- **Least privilege and roles**: roles such as "accountant", "analyst", "administrator" — permissions are given to the role, not the person.
- **Temporary permissions**: powerful rights only for the task at hand and for a limited time.
- **The life cycle**: when an employee leaves, the account is closed immediately; permissions are reviewed regularly (for example, every quarter).
- **Application keys** (API keys) are not written into code or open documents — they are stored in a dedicated secrets vault.

## Encryption and key management

| State | How it is protected |
|---|---|
| **In transit** | HTTPS/TLS, VPN (S14) |
| **At rest** | encrypting disks, databases and backups |
| **In use** | special protected environments (an advanced area) |

**Key management**: the provider's key management service (KMS) creates, stores and rotates keys. **BYOK** (bring your own key) — the customer creates and controls the key itself: if necessary, the key can be revoked, making the data unreadable even for the provider. Access to keys is also logged separately.

## Logs and monitoring

- All important actions are logged: who did what, when and from where (sign-ins, changing settings, downloading data).
- **Alerts**: a sign-in from an unusual country, many downloads at once, a setting changed to "public".
- **Automatic configuration checks**: providers' security posture services show resources left open and weak settings.
- Logs are kept in a tamper-proof place for the required period.

## Backup and recovery

- The 3-2-1 rule (M14) applies in the cloud too: at least one copy in **another account, region or provider**.
- Backups are also encrypted and access to them is restricted.
- Recovery is tested regularly; the **RTO** (how quickly the service must be restored) and **RPO** (how much data loss is acceptable) are set in advance.

## Compliance and certifications

| Standard / requirement | Content |
|---|---|
| **ISO/IEC 27001** | an information security management system |
| **ISO/IEC 27017** | security controls for cloud services |
| **ISO/IEC 27018** | protecting personal data in the public cloud |
| **SOC 2** | an independent audit report on a service organization's controls |
| **National requirements** | personal data localization, sector regulations (banking, healthcare) |

A certificate confirms the provider's part; the customer ensures its own part (permissions, settings) itself.

## A checklist for choosing a provider

1. In which country and region is the data stored? Does it meet the localization requirement?
2. Which certifications and independent audit reports does it have?
3. How is encryption handled — at rest and in transit; is BYOK available?
4. Are MFA, roles and logs supported?
5. The SLA: uptime, the deadline for notifying about incidents, compensation.
6. **The exit strategy**: in what format and how quickly can the data be retrieved; how is the data deleted when the contract ends?
7. Under what conditions can the provider's staff access the data?

## An incident response plan

The procedure for an incident must be written down in advance: **detection** (an alert) → **containment** (blocking the account, rotating keys, closing the resource) → **recovery** (from backups) → **notification** (management and, if required, the authorized body and affected customers) → **lessons learned** (the cause and measures against recurrence). The plan names the responsible people and their contact details.

:::warning
**Common mistakes**

- **Thinking "the provider is responsible for everything".** Data, permissions and settings are the customer's responsibility.
- **No MFA on the administrator account.** The most dangerous weakness.
- **Leaving a sharing setting as "public".** Confidential files may end up in search engines.
- **Keeping backups only in the same cloud account.** If the account is compromised, the backups are lost too.
- **Not thinking about an exit strategy.** When the provider has to be changed, the data is held "hostage".
:::

## Assignment

1. In a cloud service you use (Google Drive, OneDrive and so on), check whether MFA is enabled, which files are shared in "anyone with the link" mode and which devices have signed in. Write down the result (without personal data).
2. Draw up a 10-point cloud security policy for a small company (20 employees, cloud accounting and email).
3. Compare two cloud providers against the checklist above, using the information on their official websites.

## Summary

- The main risk in the cloud is the customer's misconfiguration and compromised accounts.
- Shared responsibility: in every model data, permissions and settings stay with the customer.
- Measures: IAM (MFA, roles, least privilege), encryption and key management, logs and monitoring, backups (RTO, RPO).
- A provider is checked for certifications, data location, SLA and the exit strategy; an incident response plan is prepared in advance.

## Check yourself

:::check
Q: What is the customer responsible for even in the SaaS model?
A: Access rights and accounts, the data and its classification, and sharing settings.
Q: What is BYOK?
A: The customer creates and controls the encryption key itself and can revoke it if necessary.
Q: What do RTO and RPO mean?
A: RTO is how quickly the service must be restored; RPO is how much data loss (in time) is acceptable.
Q: Why is an exit strategy needed?
A: To retrieve the data completely and on time when changing provider or ending the contract.
:::

:::link https://cloudsecurityalliance.org/research/guidance
Cloud Security Alliance: Security Guidance
International recommendations on cloud security.
:::
