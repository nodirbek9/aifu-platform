---
title: Analysing real cybercrime incidents (case study)
summary: A method for analysing real cyber incidents — the attack chain (Cyber Kill Chain), initial access, progression, consequences, response measures, legal aspects; four well-known incidents — WannaCry (2017), Equifax (2017), Colonial Pipeline (2021), the Twitter social engineering attack (2020); conclusions and lessons; an independent analysis task.
---

## Why it matters

The best textbooks of cybersecurity are other people's mistakes. After every major incident investigation reports are published: how the attacker got in, why nobody noticed, how much damage there was and what changed afterwards. Analysing these reports teaches a specialist what real attacks "look like" and which simple measures can prevent a big disaster.

:::note
**After this session you will be able to:**

- split an attack into stages using the Cyber Kill Chain model;
- identify the initial access method, the vulnerability and the consequences in a real incident;
- analyse the response measures and the legal consequences;
- draw practical conclusions for an organisation from each incident.
:::

## The analysis model: Cyber Kill Chain

The model proposed by Lockheed Martin splits an attack into seven stages; the defender can "break" the chain at any stage:

| Stage | What the attacker does | Defensive opportunity |
|---|---|---|
| 1. Reconnaissance | gathers information about the target (staff, technologies) | limiting public information |
| 2. Weaponisation | prepares a malicious file or exploit | — |
| 3. Delivery | sends it by e-mail, website, USB | mail filtering, web filtering |
| 4. Exploitation | uses a vulnerability | updates, EDR |
| 5. Installation | stays in the system (a backdoor) | application allow-listing |
| 6. Command and control (C2) | connects to an outside server | outbound traffic filtering, IDS |
| 7. Actions on objectives | data theft, encryption, sabotage | segmentation, DLP, backups |

## Cases

:::case
**Case 1. WannaCry (May 2017).** A ransomware worm infected over 200,000 computers in more than 150 countries within a few days. It spread across networks by itself using a vulnerability in Windows' SMBv1 protocol (the EternalBlue exploit). Microsoft had released the fix two months before the incident. In the UK health service operations were cancelled and factories stopped. The spread slowed by chance when a "kill switch" domain was registered.

**Analysis.** Initial access — port 445 open to the Internet; vulnerability — unpatched systems and obsolete SMBv1; violated property — availability. Lessons: installing updates quickly (patch management), turning off SMBv1, closing port 445 to the Internet, segmenting the network, offline backups.
:::

:::case
**Case 2. Equifax (2017).** The personal data of 147 million people (names, social security numbers, dates of birth) was stolen from a large US credit bureau. The attackers used a known vulnerability in the Apache Struts framework used in a web application — the fix had come out two months earlier but had not been installed. The attackers went unnoticed for ~76 days: the certificate of the device that inspected traffic had expired and it was not working.

**Analysis.** Initial access — a vulnerable web application; vulnerabilities — an unpatched component, monitoring that did not work, a lack of segmentation and encryption inside the network; violated property — confidentiality. Consequences: compensation and fines of hundreds of millions of dollars, the resignation of top management. Lessons: an inventory of software components, tracking patches, checking that monitoring tools actually work.
:::

:::case
**Case 3. Colonial Pipeline (May 2021).** After a ransomware attack, a company supplying fuel to the US east coast shut down its pipeline for several days; fuel shortages and panic followed. The company paid a ransom of ~4.4 million dollars (part of it was later recovered). The attackers had logged in to the VPN with a leaked password; multi-factor authentication was not enabled on the account.

**Analysis.** Initial access — the VPN with a stolen password; vulnerability — no MFA, an unused account left active; violated property — availability (data was stolen too). Lessons: mandatory MFA for remote access, removing unneeded accounts, separating IT and operational (OT) networks, preparing an incident response plan in advance.
:::

:::case
**Case 4. Twitter (July 2020).** Attackers phoned Twitter employees posing as the IT help desk (vishing) and talked them into entering their internal system logins on a fake site. Through an internal admin panel, messages saying "send bitcoin — we'll double it" were posted from the accounts of famous people (politicians, company heads). More than 100,000 dollars was collected in a few hours. The main participants were caught.

**Analysis.** Initial access — social engineering; vulnerabilities — broad access to internal tools, staff unprepared for such attacks; violated properties — integrity and authenticity (fake messages), reputation. Lessons: least privilege for internal tools, two-person approval for critical actions, MFA with hardware keys, social engineering training.
:::

## General conclusions

| Observation | In which case | A cheap measure |
|---|---|---|
| A known vulnerability was not closed in time | WannaCry, Equifax | a patch management process |
| One password opened everything | Colonial Pipeline | MFA |
| People were deceived | Twitter | training, hardware keys |
| The attack went unnoticed for a long time | Equifax | testing monitoring, analysing logs |
| Recovery was hard | WannaCry, Colonial | offline backups and a recovery plan |

## Independent analysis task

Choose one of the following and write a 2–3 page analysis using the template above (Kill Chain stages, vulnerability, violated property, consequences, legal aspects, lessons): NotPetya (2017), SolarWinds (2020), the Log4Shell vulnerability (2021), the MOVEit attacks (2023). Cite at least three reliable sources (investigation reports, CISA, major media).

:::warning
**Common mistakes**

- **Explaining an incident only as "the hackers were strong".** In almost every case simple, known and cheap measures were missing.
- **Writing without sources or relying on one article.** Check figures and facts against official reports.
- **Seeing only the technical side.** Analyse the legal, financial and reputational consequences too.
- **Treating paying the ransom as a solution.** Payment does not guarantee the data comes back and encourages the criminals.
:::

:::tip
For each case answer one question: "If I had been responsible for security at that organisation, which single measure would I have introduced a year before the incident?" Justify your answer with its cost and effect.
:::

## Summary

- The Cyber Kill Chain splits an attack into seven stages; defence can break the chain at any of them.
- Most major incidents started from known vulnerabilities, missing MFA, social engineering and weak monitoring.
- The main goal of analysis is to draw specific, practical and cheap lessons from each case.

## Check yourself

:::check
Q: Through which vulnerability did WannaCry spread, and what could have stopped it?
A: The Windows SMBv1 vulnerability (EternalBlue); a patch installed in time, turning off SMBv1 and closing port 445.
Q: How did the attackers get in at Colonial Pipeline?
A: Through a VPN account without MFA, using a leaked password.
Q: What kind of attack was used in the Twitter incident?
A: Social engineering — deception over the phone (vishing).
Q: Why did the attack at Equifax go unnoticed for so long?
A: The certificate of the traffic inspection device had expired and it was not working.
Q: What is the last stage of the Cyber Kill Chain called?
A: Actions on objectives — data theft, encryption or sabotage.
:::

:::link https://www.cisa.gov/news-events/cybersecurity-advisories
CISA Cybersecurity Advisories
The US cybersecurity agency's advisories on incidents and vulnerabilities.
:::
