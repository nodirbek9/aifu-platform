---
title: The role of antivirus, IDS and IPS tools in ensuring the availability of information. Classification of malware and protection methods
summary: The availability property and threats to it; the role and limitations of antivirus, IDS and IPS in preserving availability; the reliability of protection tools — false alerts, IPS behaviour on failure (fail-open, fail-closed); a classification of malware — virus, worm, Trojan, ransomware, spyware, adware, rootkit, bot and botnet, keylogger, fileless malware; propagation routes; protection methods by type; an introduction to MITRE ATT&CK.
---

## Why does this topic matter?

The third property of the CIA triad — **availability** — is often the most expensive: if a hospital, a factory or a bank stops because of ransomware, every hour brings large losses. Antivirus, IDS and IPS protect availability, but when misconfigured they can themselves cause outages. At the same time, choosing protection correctly requires knowing the types into which malware is divided.

:::note
**After this assignment you will be able to:**

- name the threats to availability and the tools against them;
- explain the role and risks of antivirus, IDS and IPS for this property;
- classify malware into its main types;
- propose the main protective measures for each type.
:::

## Availability and threats to it

**Availability** means that an authorized user can use information and services when needed. Threats to it:

- malware (especially ransomware and data-destroying malware);
- denial of service attacks (DoS/DDoS);
- hardware failures, power outages, natural disasters (M12);
- human mistakes and wrong settings (including in the protection tools themselves).

## The role of protection tools

| Tool | How it protects availability | Risk or limitation |
|---|---|---|
| **Antivirus / EDR** (MT9) | stops malware before it runs or at an early stage; blocks encryption behaviour | a false alert may quarantine an important system file; a faulty update can disable many computers at once |
| **IDS** (M14) | detects an attack early and gives time to respond | does not stop it by itself; someone must look at the alerts |
| **IPS** (M14) | stops attack traffic in transit | a false block cuts off legitimate traffic; if the device itself fails, traffic stops |

**Behaviour on failure**: when an IPS or firewall fails there are two options: **fail-open** (passes traffic without inspection — the service works but is unprotected) and **fail-closed** (stops the traffic — protection is kept but the service is interrupted). The choice depends on the risk assessment: for a hospital system continuity often comes first, for a bank payment system protection does.

**The July 2024 incident**: a faulty configuration update of the CrowdStrike EDR product crashed millions of Windows computers — disrupting airlines, banks and hospitals. The lesson: updates of protection tools must also be rolled out in stages, and a recovery plan must be ready.

**Improving reliability**: tuning false alerts (exceptions only when justified), testing updates on a pilot group first, running an IPS in detection-only mode first, redundancy (two devices, a cluster), monitoring and a response plan.

## Classification of malware

| Type | Main feature | Main damage |
|---|---|---|
| **Virus** | reproduces by infecting a host object (MT6) | corrupting files and systems |
| **Worm** | spreads on its own over the network | fast mass infection, network load |
| **Trojan** | disguised as a useful program, does not reproduce | hidden access, data theft |
| **Ransomware** | encrypts files and demands payment; often steals data first | availability and confidentiality |
| **Spyware**, **keylogger** | collects the user's activity and entered data | passwords, card details |
| **Adware** | unwanted advertising, changes to browser settings | nuisance, tracking |
| **Rootkit** | hides itself and other malware from the OS | staying unnoticed for a long time |
| **Bot and botnet** | a network of remotely controlled infected devices | DDoS, spam, a resource for further attacks |
| **Fileless malware** | runs in memory and through system tools without writing files to disk | evading antivirus signatures |
| **Cryptominer** | uses the device's resources to mine cryptocurrency | slowdown, power consumption |
| **Wiper** | destroys data irrecoverably | availability |

In practice one sample combines features of several types (for example, ransomware that spreads like a worm). Researchers and SOC teams use the **MITRE ATT&CK** knowledge base to describe malicious activity — it describes attackers' tactics and techniques by stage and makes it possible to map protective measures to them.

## Propagation routes and protection

| Propagation route | Protection |
|---|---|
| phishing emails and malicious attachments | mail filter, awareness training, restricting macros |
| infected or fake websites | browser and OS updates, web filter, DNS filter |
| unpatched services and vulnerabilities | patch management, reducing Internet-facing services, segmentation |
| removable media | USB control, disabling autorun (A12, the Stuxnet case) |
| unofficial and "cracked" programs | official sources only, restricting applications with an allow list |
| stolen passwords | MFA, unique passwords (M10) |

**Main measures by type**: against ransomware — offline backups and a recovery plan (MT17); against spyware and keyloggers — MFA and hardware keys; against rootkits — Secure Boot and, if in doubt, a full OS reinstallation; against botnets — filtering outgoing traffic and segmenting IoT devices.

:::warning
**Common mistakes**

- **"Install and forget" protection tools.** False alerts, updates and alerts must be reviewed regularly.
- **Turning on an IPS in blocking mode right away.** First study the traffic in detection mode, then tune the rules.
- **Paying after a ransomware attack.** Paying does not guarantee the data comes back and finances criminals; first work with backups and the authorized bodies.
- **Continuing to use an infected computer after "cleaning".** With a rootkit or deep infection, reinstalling the OS is more reliable.
- **Mixing up the classification.** In academic work clearly distinguish viruses, worms and Trojans.
:::

:::tip
On the MITRE ATT&CK website (attack.mitre.org) find the "Data Encrypted for Impact" technique and read its "Mitigations" section — a systematic list of protective measures for your essay is there.
:::

## Summary

- Threats to availability: malware, DoS, failures, human mistakes.
- Antivirus/EDR, IDS and IPS protect availability, but false alerts and faulty updates can also cause outages; fail-open and fail-closed are a risk-based choice.
- Malware: virus, worm, Trojan, ransomware, spyware, adware, rootkit, botnet, fileless, cryptominer, wiper.
- Protection by propagation route: mail filter, updates, USB control, official sources, MFA, backups.
- MITRE ATT&CK is a knowledge base that systematizes attack techniques and protective measures.

## Check yourself

:::check
Q: How do the fail-open and fail-closed modes of an IPS differ?
A: Fail-open passes traffic without inspection on failure (the service works), fail-closed stops it (protection is kept, the service is interrupted).
Q: How does a worm differ from a virus?
A: A worm spreads on its own over the network; a virus needs a host object.
Q: What is the most important recovery measure against ransomware?
A: An offline or immutable backup and a tested recovery plan.
Q: What is the main feature of a rootkit?
A: It hides itself and other malware from the operating system and protection tools.
:::

:::link https://attack.mitre.org/
MITRE ATT&CK
A knowledge base of attacker tactics, techniques and the measures against them.
:::
