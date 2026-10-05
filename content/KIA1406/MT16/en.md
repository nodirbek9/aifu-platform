---
title: Social engineering. Using the ESET NOD32 antivirus tool
summary: Personal and organizational protection against social engineering — the signs of a phishing email, checking links and senders, the signs of phone and messenger scams; an awareness programme in an organization — training, authorized phishing simulations, metrics; ESET NOD32 Antivirus — installation and activation, updates, scan types, real-time protection, HIPS, anti-phishing, LiveGrid, quarantine, logs and reports; safe testing with EICAR.
---

## Why does this topic matter?

In MT8 we covered the psychological foundations of social engineering and real incidents. This assignment covers the practical side: how every user can **recognize** a deception and how an organization **prepares** its staff. The second part is hands-on work with a widespread antivirus product, ESET NOD32: it reduces the technical consequences of social engineering by blocking phishing pages, detecting malicious attachments and monitoring behaviour. But no program replaces the habit of "stop and verify".

:::note
**After this assignment you will be able to:**

- recognize the signs of a phishing email, a fake site and a phone scam;
- propose a simple awareness programme for an organization;
- install ESET NOD32 and configure its main protection modules;
- work with scanning, quarantine and logs.
:::

## Recognizing a phishing email

| Sign | What to check |
|---|---|
| **Sender** | not the display name but the full address and domain: instead of `bank.uz` it may be `bank-uz.support.com` |
| **Link** | hover over the link (without clicking) to see the real address; read the domain from the right (`example.uz.evil.com` from A9) |
| **Urgency and fear** | "within 24 hours", "your account will be blocked", "a fine" |
| **An unusual request** | a password, an SMS code, card details, changing payment details — official organizations do not ask for these by email |
| **Attachments** | an unexpected archive, a document with macros, a file with a double extension such as "invoice.pdf.exe" |
| **Language and design** | mistakes, a wrong logo — but modern phishing can be error-free too |

**On the phone and in messengers**: a "bank employee" asks for an SMS code; "your relative is in trouble, send money now"; "you have won a prize, pay a fee first"; a "manager" writes from a new number and gives a secret task. The rule: call back the official number yourself, and never tell anyone an SMS code or the CVV.

**If you have been fooled**: change your password at once (from another device), call the bank, in an organization report to the security service; reporting quickly sharply reduces the damage.

## An awareness programme in an organization

1. **Goal and audience**: a basic course for all staff, separate ones for finance, HR and the IT help desk (they are targeted more often).
2. **Short, regular training**: not a long lecture once a year, but 5–10-minute real examples every month.
3. **Authorized phishing simulations**: with the consent of management and the legal department, without humiliating staff; the goal is to teach, not to punish.
4. **Easy reporting**: a "report phishing" button in the mail client, a clear contact number.
5. **Metrics**: the share of people who clicked the link in a simulation (should fall) and the **share who reported it** (should rise — the most important metric).
6. **Linking to processes**: confirming payments and password resets through an independent channel (MT8).

## ESET NOD32 Antivirus

**ESET NOD32** is an antivirus product from the Slovak company ESET, known for its low resource use and consistent results in independent tests. The broader editions (Internet Security, Smart Security Premium) add a firewall, banking payment protection and a password manager; the corporate edition is managed from a central console (ESET PROTECT).

| Module | Role |
|---|---|
| **Real-time file system protection** | checks files as they are opened, created and run |
| **Anti-Phishing** | blocks known phishing and fake sites in the browser |
| **Web and email protection** | checks HTTP(S) traffic and mail attachments |
| **HIPS** | monitors and blocks suspicious changes to system processes, the registry and files |
| **Ransomware Shield** | detects mass file-encryption behaviour |
| **LiveGrid** | a cloud reputation system — fast information about new files |
| **Device control** | rules for using USB and other media |
| **Computer scan** | quick (Smart scan), full, custom (a selected location), removable media |

**Practical work (on a test computer or in a virtual machine)**:

1. First disable or remove any other real-time antivirus (two should not run together, MT9); Windows Defender switches to passive mode automatically when ESET is installed.
2. Download the trial version of ESET NOD32 **only from the official website** and install it; during installation enable LiveGrid and the detection of potentially unwanted applications (PUA).
3. **Update**: Update → Check for updates; write down the module and database versions.
4. Run a **Smart scan** and a **full scan**; record the number of objects checked, the time and the detections.
5. **Review the settings** (Setup / Advanced setup): check that real-time protection, HIPS, Anti-Phishing and Ransomware Shield are enabled; look at the option to protect the settings with a password.
6. **Safe testing**: try downloading the EICAR test file from the official eicar.org website — ESET should block it (MT10). To test Anti-Phishing, use the official test pages provided by vendors and AMTSO. Never open real malicious or phishing pages.
7. **Logs and quarantine**: look through Tools → Log files (detections, scans, HIPS events) and the Quarantine section; analyze them in your report with screenshots.

:::warning
**Common mistakes**

- **"The antivirus fully protects against phishing".** New phishing pages reach the databases late; an antivirus does not see phone deception at all.
- **Opening a link in an email "to check it".** Do not open a suspicious link — report it.
- **Running two antiviruses actively at the same time.** Conflicts and slowdowns.
- **An unofficial "key" or a "cracked" antivirus.** It may itself be a source of malware, and it is illegal.
- **Using phishing simulations to punish staff.** It destroys trust and reduces reporting.
:::

:::tip
Analyze 3 emails from the spam folder of your mailbox using the table (without opening them — look only at the subject and sender details): which signs show that it is phishing? Hide personal data in your report.
:::

## Summary

- Signs of phishing: the sender's domain, the real address of the link, urgency, an unusual request, suspicious attachments.
- On the phone and in messengers: never tell anyone an SMS code or card details; call the official number yourself.
- An awareness programme: short regular training, authorized simulations, easy reporting; the key metric is the share who report.
- ESET NOD32: real-time protection, Anti-Phishing, HIPS, Ransomware Shield, LiveGrid, device control, scanning, quarantine and logs.
- An antivirus is a helper; the habit "stop — verify — report" is the main protection.

## Check yourself

:::check
Q: How can you safely find out the real address of a link?
A: Hover over the link (without clicking) to see the address and read the domain from the right.
Q: What is the most important metric of an awareness programme?
A: The share of employees who reported a suspicious email (it should keep rising).
Q: What does the HIPS module in ESET do?
A: Monitors and blocks suspicious changes to system processes, the registry and files.
Q: What is used to check safely that an antivirus works?
A: The standard EICAR test file — it is harmless, but antiviruses detect it.
:::

:::link https://www.eset.com/
ESET
The official website of ESET NOD32 and other protection products, with documentation and guides.
:::
