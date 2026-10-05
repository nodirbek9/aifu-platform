---
title: Developing a security policy project (for a simple organisation)
summary: Writing an information security policy for a small organisation — preparation (assets, risks, legal requirements), the document structure, the organisational policy and issue-specific policies (AUP, password, access, backup, incident response), roles and responsibilities, acquainting staff and oversight; sample texts; assessment criteria.
---

## Why it matters

In small organisations security often rests on "the IT person knows everything". If they leave or fall ill, nobody knows where the passwords are, whether backups are taken and whom to call when an incident happens. A written **security policy** ties the rules to the organisation rather than to people. In this session we prepare a draft policy for a real (simplified) organisation.

:::note
**After this session you will be able to:**

- gather the assets, risks and requirements before writing a policy;
- know the structure of the organisational policy and of issue-specific policies;
- write clear, achievable and checkable rules;
- set out roles, responsibilities, the acquaintance procedure and the review cycle.
:::

## The organisation

**The private clinic "Sogʻlom Hayot"** ("Healthy Life"): 25 staff (doctors, nurses, reception, an accountant, the director), 1 IT specialist (half-time). Patients' electronic records are in a cloud medical system, accounting is on a local computer, reception has Wi-Fi and 6 computers, and staff chat on Telegram from their personal phones. Legal requirements: the Law "On Personal Data" (medical data is a special category).

## Stage 1: preparation

| Question | Sample answer |
|---|---|
| The most valuable assets | patients' medical records, the accounting database, the clinic's reputation |
| The main risks | theft of the cloud system password through phishing; ransomware on the accounting computer; sending medical data through personal messengers; patients joining the Wi-Fi |
| Laws and contracts | the personal data law, the contract with the cloud provider |
| Existing measures | antivirus, a Wi-Fi password (which patients know too) |

## Stage 2: the document structure

Every policy has the same sections:

1. **Purpose** — why it is needed.
2. **Scope** — whom and which systems it covers.
3. **Rules** — specific, checkable requirements ("must", "is prohibited").
4. **Roles and responsibilities**.
5. **Exceptions** — who grants them and how.
6. **Consequences of violation**.
7. **Related documents**, **owner**, **approval date**, **review date**.

## Stage 3: the organisational policy (sample)

> **Information Security Policy.** The clinic is obliged to ensure the confidentiality, integrity and availability of patients' and staff data. All staff follow this policy and the rules attached to it. The director is responsible for information security; the IT specialist does the practical work. Every employee reports an incident or suspicious situation to the IT specialist immediately. The policy is reviewed once a year and after every serious incident.

## Stage 4: issue-specific policies

**Passwords and authentication**:

- A password has at least 12 characters; reusing passwords from other services is prohibited.
- Two-step authentication is mandatory for the cloud medical system and e-mail.
- Telling a password to anyone, including the IT specialist, or writing it down on paper is prohibited.
- After 5 wrong attempts the account is locked for 15 minutes.

**Acceptable use (AUP)**:

- Clinic computers are for work only; installing other software is prohibited.
- Sending patient data through personal messengers, personal e-mail or flash drives is prohibited; only messages inside the medical system are used.
- The screen is locked when leaving the computer (Win+L); automatic lock after 5 minutes of inactivity.

**Access control**:

- Every employee has a personal account; shared accounts are prohibited.
- Rights are granted by position: reception — appointments and contacts, a doctor — their own patients' records, the accountant — the financial system.
- The account of an employee who leaves is deleted on their last working day; rights are reviewed every quarter.

**Backup**:

- The accounting database is copied daily; one copy is on an external disk (swapped weekly and kept in a safe).
- Restoring is tested every quarter and the result is logged.

**Network and Wi-Fi**:

- A separate guest Wi-Fi network for patients; the staff network is WPA3 (or WPA2-AES), its password known only to the IT specialist.

**Incident response**:

1. An employee reports an incident (a suspicious e-mail, a computer behaving oddly, a lost device) to the IT specialist by phone at once.
2. The IT specialist disconnects the infected computer from the network and informs the director.
3. If personal data has leaked, the director notifies the competent authority in the way the law requires.
4. The recovery and conclusions are written into a log.

## Stage 5: implementation and oversight

- All staff read the policy and sign it; a new employee — on their first day.
- A short training twice a year (phishing, passwords, reporting incidents).
- The IT specialist runs a check every quarter: the account list, backups, updates.

## Assignment

Write a 3–5 page draft policy for a small organisation of your choice (a pharmacy, a training centre, a small shop, a notary's office): a description of the organisation, the preparation table, the organisational policy and at least four issue-specific policies (password, AUP, access, backup or incident response).

**Assessment criteria**: fit to the organisation's risks (30%), clarity and checkability of the rules (25%), completeness of the structure (20%), roles and responsibilities (15%), language and presentation (10%).

:::warning
**Common mistakes**

- **Vague rules.** "Use strong passwords" cannot be checked; "at least 12 characters and MFA" can.
- **Unachievable requirements.** Writing a 24/7 security operations centre into a small clinic's policy leaves it on paper.
- **No person responsible.** Every rule needs an owner and someone who checks it.
- **Writing the policy only for IT.** The main readers are ordinary staff; write in plain language.
:::

:::tip
Have someone non-technical you know (for example, a parent) read your policy: can they understand each rule and say what they must do? Simplify any rule they do not understand.
:::

## Summary

- Before writing a policy, gather the assets, risks, legal requirements and existing measures.
- Every policy has a purpose, scope, rules, roles, exceptions, consequences, an owner and dates.
- The key policies for a small organisation: organisational, password, AUP, access, backup, network, incident response.
- Acquaintance, training and regular checks turn a policy into a "living" document.

## Check yourself

:::check
Q: Which is the highest, most general document in the security document hierarchy?
A: The organisational information security policy.
Q: "An employee gets only the rights needed to do their job" — which principle is this?
A: The principle of least privilege.
Q: What does a password policy usually include?
A: Length and complexity, a ban on reuse, MFA, rotation and lockout rules, not disclosing the password.
Q: What does an AUP set out?
A: How employees may and may not use the organisation's resources.
Q: Why does every policy state a review date?
A: Technologies, threats and laws change; an outdated policy does not protect.
:::

:::link https://www.sans.org/information-security-policy/
SANS Information Security Policy Templates
Free sample information security policies.
:::
