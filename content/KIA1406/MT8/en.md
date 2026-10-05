---
title: Protecting information from unauthorized use. Analysis of social engineering attacks
summary: A layered system of protection against unauthorized use — identification and authentication, access control, encryption, network and physical protection, monitoring and response; the concept of social engineering and psychological influence techniques (Cialdini's principles); attack types — phishing, spear phishing, vishing, smishing, pretexting, baiting, business email compromise, deepfakes; analysis of real incidents — Twitter (2020), MGM Resorts (2023), the Arup deepfake video call (2024); organizational protective measures.
---

## Why does this topic matter?

The stronger the technical protection, the more attackers turn to **people**: asking an employee for a password is easier than cracking it. According to the Verizon DBIR reports, the human factor — a mistake, deception or stolen credentials — is involved in a large share of data breaches. That is why protection against unauthorized use must cover people and processes as well as technical measures.

:::note
**After this assignment you will be able to:**

- name the layers of protection against unauthorized use;
- explain the psychological influence techniques used in social engineering;
- recognize the main types of social engineering attacks;
- analyze real incidents and identify the measures that could have stopped them.
:::

## Layers of protection against unauthorized use

| Layer | Measures | Details |
|---|---|---|
| Identification and authentication | unique accounts, strong passwords, MFA, FIDO2 | M10, A10 |
| Access control | least privilege, roles, separation of duties, regular reviews of rights | M11, A11, MT17 |
| Data protection | encryption (at rest and in transit), DLP | M6, M14, MT10 |
| Network | segmentation, firewalls, VPN, zero trust | M14 |
| Physical | access control, clean desk, locked screens | M12 |
| Monitoring and response | logs, SIEM, an incident response plan | MT7 |
| **People and processes** | awareness, verification procedures, a reporting culture | this topic, MT16 |

## Social engineering

**Social engineering** is tricking a person into voluntarily giving away confidential information or taking an action that breaks security. It exploits not a technical weakness but human psychology. The psychologist R. Cialdini's principles of influence work here too:

| Principle | How it appears in an attack |
|---|---|
| **Authority** | a request in the name of a "manager", the "bank's security service", the "IT department" |
| **Urgency / scarcity** | "your account will be blocked in 1 hour", "a fine if not paid today" |
| **Social proof** | "other employees have already confirmed" |
| **Liking** | a friendly conversation, mutual acquaintances, flattery |
| **Reciprocity** | first a small favour or gift, then a request |
| **Commitment and consistency** | moving from small agreements to a big request |

**Main attack types**:

| Type | Description |
|---|---|
| **Phishing** | mass fake emails: a fake site, attachment or link |
| **Spear phishing** | an email tailored to a specific person, using public information |
| **Vishing** | deception by phone (in recent years with voice cloning) |
| **Smishing** | via SMS and messenger messages |
| **Pretexting** | inventing a believable situation: an "auditor", a "courier", a "new employee" |
| **Baiting** | an interesting flash drive or a "free" download (A12) |
| **Business email compromise** (BEC) | changing payment details in the name of a manager or supplier |
| **Deepfake** | a fake voice or video created with artificial intelligence |

## Analysis of real incidents

:::case
**Case 1. Twitter (July 2020).** Attackers phoned employees and, in the name of the internal IT help desk, persuaded them to enter their login details on a fake site. Through an internal admin panel, cryptocurrency scam messages were posted from the accounts of famous people (A3).

**Analysis.** Technique — vishing and pretexting; principle — authority and urgency. Weaknesses: broad access to an internal tool, no procedure for verifying a phone request. Measures: least privilege for internal tools, phishing-resistant MFA (hardware keys), two-person approval for critical actions.
:::

:::case
**Case 2. MGM Resorts (USA, September 2023).** According to media reports, the attackers found information about an employee on social networks, called the IT help desk in the employee's name and got the account reset. The subsequent attack disabled hotel and casino systems for several days; the company estimated the damage at about 100 million dollars.

**Analysis.** Technique — vishing with prior open-source reconnaissance; principle — authority, a request for help. Weakness — no reliable identity verification procedure at the help desk. Measures: strict verification when resetting a password or MFA (calling back on the official number, a manager's approval, appearing in person), a separate procedure for privileged accounts.
:::

:::case
**Case 3. A deepfake video call (Hong Kong, early 2024).** A finance employee of the engineering company Arup talked in a video conference with the "chief financial officer" and other "colleagues" — all of whom were fake images created with artificial intelligence. The employee transferred about 25 million US dollars in several transactions.

**Analysis.** Technique — BEC and deepfake; principle — authority and social proof ("many people" in the conference). Weakness — no procedure for confirming large payments through an independent channel. Measures: confirming a large or unusual payment through a different, previously known channel; two-person signing; a "secret and urgent payment" is an automatic red flag.
:::

**The general lesson**: in all three cases technical protection existed, but the **process** — confirmation through an independent channel — was missing or did not work. The most effective measure against social engineering is a procedure for verifying important actions without relying on a single conversation or email.

## Organizational protective measures

- **Verification procedures**: changing payment details, resetting a password, giving out confidential information — only after re-checking through an independent channel.
- **Phishing-resistant authentication**: FIDO2/passkeys and hardware keys (M10).
- **An awareness programme**: regular short training, authorized phishing simulations, real examples (MT16).
- **A reporting culture**: reporting a suspicious email with one button; not punishing an employee who made a mistake but thanking them for reporting quickly.
- **Limiting public information**: employees should not share excessive information about the work structure and processes on social networks.
- **Technical support**: mail filtering (SPF, DKIM, DMARC), tagging external emails, monitoring look-alike domains.

:::warning
**Common mistakes**

- **"I would never be fooled".** Social engineering exploits fatigue, haste and trust — even experienced specialists are deceived.
- **Checking by calling back the number in the email.** Check only via a previously known, official number or channel.
- **Treating a video call as absolute proof.** Deepfake technology can fake voice and image.
- **Punishing a deceived employee.** This leads to future incidents being hidden.
- **Relying only on technical measures.** Without process and awareness, technical protection is bypassed.
:::

:::tip
Set yourself the rule "stop — verify — report": when an urgent and unusual request arrives, stop the action, check the request through an independent channel and, if it is suspicious, report it to the security service.
:::

## Summary

- Protection against unauthorized use is layered: authentication, access control, encryption, network, physical protection, monitoring and people.
- Social engineering exploits psychological principles: authority, urgency, social proof, liking, reciprocity.
- Types: phishing, spear phishing, vishing, smishing, pretexting, baiting, BEC, deepfakes.
- The Twitter, MGM and Arup cases: the main weakness was the lack of a procedure for confirming through an independent channel.
- Measures: verification procedures, phishing-resistant MFA, awareness, a reporting culture, mail filtering.

## Check yourself

:::check
Q: How is the principle of "urgency" used in social engineering?
A: Time pressure is created to deprive the victim of the chance to think and verify.
Q: What is the main goal of a business email compromise (BEC) attack?
A: Making a fake payment or changing payment details in the name of a manager or supplier.
Q: Which measure could have stopped the damage in the Arup incident?
A: Confirming the large payment through a previously known independent channel and two-person signing.
Q: Why is punishing a deceived employee dangerous?
A: Employees hide their future mistakes, and incidents are detected late.
:::

:::link https://www.cisa.gov/secure-our-world/recognize-and-report-phishing
CISA: Recognize and Report Phishing
Practical advice on recognizing and reporting phishing.
:::
