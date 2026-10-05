---
title: Popular certifications in cybersecurity. Password management systems (for example, LastPass) and their use
summary: Professional certifications and their purpose; entry-level (CompTIA Security+, ISC2 CC, Cisco CyberOps), intermediate and specialized (CySA+, CEH, OSCP, GIAC, cloud certifications) and advanced (CISSP, CISM, CISA) certifications; how to prepare; password managers — how they work, zero-knowledge architecture, the master password, autofill and phishing protection; the LastPass 2022 incident and its lessons; Bitwarden, KeePassXC, browser managers; setting up a password manager in practice.
---

## Why does this topic matter?

The two topics are linked: certifications are a way of confirming a specialist's knowledge in a "language" employers understand (part of the career path in MT13), and password managers are one of the most important practical recommendations of M10 — a separate, long, random password for every site. Both raise the same key question: **what is trust based on?** A certification on knowledge having been tested; a password manager on its cryptography and the vendor's security practice.

:::note
**After this assignment you will be able to:**

- know the main cybersecurity certifications and their levels;
- choose a certification suited to your direction;
- explain how a password manager works and why it is secure;
- state the lessons of the LastPass incident;
- set up a password manager securely.
:::

## Professional certifications

A certification is a document confirming knowledge through an exam by an independent organization. It does not replace experience, but it helps in résumé screening and on the international market; for some positions and government contracts it is mandatory.

| Level | Certification | From | For whom |
|---|---|---|---|
| Entry | **CompTIA Security+** | CompTIA | security fundamentals, the most widely recognized entry certification |
| Entry | **ISC2 CC** (Certified in Cybersecurity) | ISC2 | entering the field, with free training programmes |
| Entry | **Cisco CyberOps Associate** | Cisco | SOC and monitoring fundamentals |
| Intermediate | **CompTIA CySA+** | CompTIA | security analyst, SOC |
| Intermediate | **CEH** | EC-Council | authorized testing basics (theoretical) |
| Specialized | **OSCP** | OffSec | practical penetration testing (a 24-hour hands-on exam) |
| Specialized | **GIAC** (GSEC, GCIH and others) | SANS/GIAC | various technical directions |
| Cloud | **AWS Security Specialty**, **Microsoft SC-200/SC-300**, **CCSP** | AWS, Microsoft, ISC2 | cloud security |
| Advanced | **CISSP** | ISC2 | experienced specialists and managers (5 years of experience required) |
| Advanced | **CISM**, **CISA** | ISACA | security management; information systems audit |
| Standard | **ISO/IEC 27001 Lead Implementer / Lead Auditor** | various accredited bodies | implementing and auditing an ISMS |

**Selection principles**: first define your direction (MT13); start with an entry-level certification; check which ones local and international job openings require; consider the price and the renewal conditions (continuing education credits, annual fees). Use the websites of the official exam centres and organizations — sites selling "exam questions" break the rules and can lead to the certification being revoked.

**Preparation**: a plan based on the official list of exam topics (exam objectives); hands-on labs; practice tests; 2–3 months of regular study.

## Password managers

A **password manager** is a program that stores all your passwords in an encrypted "vault". You remember only one strong **master password**.

**How it works** (zero-knowledge architecture):

1. An encryption key is derived from the master password on the device itself with a slow key derivation function (PBKDF2 or Argon2, M8).
2. The vault is encrypted on the device with this key (usually AES-256).
3. Only the **encrypted** vault is sent to the server — the provider knows neither the master password nor your passwords.

**Advantages**: a random password of 20+ characters for every site; no password reuse (against credential stuffing, M10); **autofill protects against phishing** — the manager does not offer the password on a fake domain; warnings about breached passwords.

**Risks**: all passwords in one place — if the master password is weak or the device is infected, everything is at risk. Hence: a long master password or passphrase, MFA on the account, a protected device.

| Manager | Feature |
|---|---|
| **Bitwarden** | open source, independently audited, has a free plan, can also run on your own server |
| **KeePassXC** | open source, offline: the vault file stays only on your device |
| **1Password** | commercial; uses a "Secret Key" in addition to the master password |
| **Browser managers** (Chrome, Edge, Firefox, iCloud Keychain) | convenient, synchronized; depend on device and account protection |
| **LastPass** | a popular commercial manager; the 2022 incident is described below |

## The LastPass incident (2022)

In August 2022 attackers got into a LastPass developer's work account and stole source code. Using this information, they later got into the **home computer** of one of the company's key employees (through outdated software), obtained the keys to cloud backup storage and stole backups of users' vaults. The vaults were encrypted, but:

- some fields in the vaults (for example, website addresses) were not encrypted — the attackers could see who used which sites;
- old accounts had a low PBKDF2 iteration count (some only a few thousand) — making offline guessing of weak master passwords easier;
- in the following years researchers and journalists linked the incident to cryptocurrency thefts from the stolen vaults.

**Lessons**: (1) the zero-knowledge architecture worked — vaults with strong master passwords were not opened; (2) the strength of the master password is decisive; (3) slow hashing parameters must be kept up to date; (4) metadata must be encrypted too; (5) employees' home devices are part of the organization's perimeter as well; (6) after a breach notice, change your important passwords (especially email and bank).

## Practical work: setting up a password manager

1. Choose an open-source manager (Bitwarden or KeePassXC) and install it from the official website.
2. Create a **master passphrase** of at least 4–5 random words; never store it digitally, only on paper in a safe place (as a backup).
3. Enable **MFA** on the account (an authenticator app or a hardware key).
4. Generator settings: length 20+, all character types.
5. Change the passwords of your 3 most important accounts (email, bank, public services) to unique passwords created by the manager.
6. Keep emergency access or recovery codes in a safe place.
7. In your report show only screenshots of the settings — **not** passwords or vault contents.

:::warning
**Common mistakes**

- **Using the master password elsewhere.** It must be used only for the password manager.
- **Not enabling MFA on the password manager.** Access to the vault means access to every account.
- **Installing from an unofficial source.** A fake "password manager" is the most direct way to steal passwords.
- **Copying a password by hand when autofill does not work.** This may be a sign of a fake site — check the domain first.
- **Equating a certification with knowledge.** A certification is a confirmation, but practical experience is required separately.
:::

:::tip
Use the manager's "security report" (password health) feature: it shows reused, weak and breached passwords — make a plan to change them one by one.
:::

## Summary

- Certifications confirm knowledge: entry (Security+, ISC2 CC), intermediate and specialized (CySA+, OSCP, GIAC, cloud), advanced (CISSP, CISM, CISA).
- Choose by direction and job requirements; prepare with the official topics and practice.
- A password manager: the key is derived from the master password on the device, the vault is stored encrypted (zero knowledge).
- Advantages: unique long passwords, phishing protection; the risk is a single point, hence a strong master password and MFA.
- LastPass 2022: the encryption worked, but weak master passwords, low iteration counts and unencrypted metadata created risk.

## Check yourself

:::check
Q: What does zero-knowledge architecture mean?
A: The vault is encrypted on the device with a key derived from the master password; the provider knows neither the master password nor the passwords.
Q: How does a password manager's autofill protect against phishing?
A: It offers the password only on the saved domain; on a fake domain the password is not filled in automatically.
Q: Why were users with weak master passwords at greater risk in the LastPass incident?
A: The encrypted vaults were stolen; weak passwords (especially with a low PBKDF2 iteration count) can be guessed offline.
Q: Which certifications suit a specialist just entering cybersecurity?
A: Entry-level ones: CompTIA Security+, ISC2 CC or Cisco CyberOps Associate.
:::

:::link https://bitwarden.com/help/bitwarden-security-white-paper/
Bitwarden Security Whitepaper
A description of the cryptographic architecture of an open-source password manager.
:::
