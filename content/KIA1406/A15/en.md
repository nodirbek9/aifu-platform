---
title: Studying the security weaknesses of IoT devices (case study or presentation)
summary: Practical session — analysing IoT security through case studies; the analysis method (device inventory, assets and data flows, a STRIDE threat model, comparison with baseline requirements, risk assessment, measures); historical cases — the Mirai botnet, the casino aquarium thermometer, home cameras and baby monitors, a connected car and a software recall; the main cause and lessons of each case; coordinated vulnerability disclosure; recommendations for manufacturers and users; the structure and assessment criteria of a group presentation.
---

## Why does this topic matter?

In lecture M15 we saw the IoT architecture, widespread weaknesses and the baseline requirements (ETSI EN 303 645). In this practical session you analyse real incidents **from a defensive point of view**: what happened, which category the main cause belongs to, and which simple measure could have prevented the incident. The result is a group presentation or a written case analysis.

:::note
**After this practical session you will be able to:**

- build a threat model for an IoT system;
- analyse real incidents by cause category;
- propose protective measures for each cause;
- explain the process of coordinated vulnerability disclosure;
- present the results of the analysis in a clear presentation.
:::

:::warning
The session is analytical: incidents are studied from open-source information, for defensive conclusions. Do not test any real device or network without authorization — that is a breach of the law (cybercrime in A3). To check your own device, use the audit list from M15.
:::

## The analysis method

| Step | Question | Result |
|---|---|---|
| **1. Describing the system** | which devices, network, cloud and application are there? | an architecture diagram (the four layers from M15) |
| **2. Assets and data flows** | what data is collected, where does it go, who uses it? | a data flow diagram |
| **3. Threat model** | what threats are possible? | a STRIDE table |
| **4. Comparison with baseline requirements** | which clauses of ETSI EN 303 645 are not met? | a list of deviations |
| **5. Risk assessment** | what are the likelihood and impact? | a risk matrix (A2) |
| **6. Measures** | what should the manufacturer, the organization and the user do? | prioritized recommendations |

**STRIDE** is a way of categorizing threats:

| Category | Meaning | IoT example | Security property |
|---|---|---|---|
| **S**poofing | pretending to be someone else | a fake device connects to the cloud | authentication, device certificates |
| **T**ampering | altering data or software | unsigned firmware | integrity, signed updates |
| **R**epudiation | denying an action | who opened the door is not recorded | logs |
| **I**nformation disclosure | data being exposed | an unencrypted video stream | encryption, access control |
| **D**enial of service | a service stopping | the device stops working | resilience, limits |
| **E**levation of privilege | gaining higher rights | an ordinary user becomes an administrator | least privilege |

## Historical cases

Each case is presented from open-source information, only at the level of **cause and lesson**.

### 1. The Mirai botnet (2016)

- **What happened**: hundreds of thousands of cameras and routers whose factory passwords had not been changed were taken over by malware, joined into a botnet and used in DDoS attacks that made major online services unavailable.
- **Main cause**: universal default passwords, unnecessary open management services.
- **STRIDE**: Spoofing (use of the factory account), Denial of service (the consequence).
- **Lesson**: unique passwords and disabling unnecessary services — clauses 1 and 6 of ETSI EN 303 645; later several countries banned default passwords by law.

### 2. The casino aquarium thermometer (reported in 2017)

- **What happened**: according to a security company's report, an Internet-connected thermometer in a casino aquarium served as an entry point into the organization's network, and data was sent outside.
- **Main cause**: the IoT device was in the same segment as the internal network, and outbound traffic was not controlled.
- **STRIDE**: Information disclosure.
- **Lesson**: **segmentation** (IoT in a separate segment), restricting and monitoring outbound traffic (M14, A13), a device inventory.

### 3. Home cameras and baby monitors

- **What happened**: over the years there have been many reports of outsiders gaining access to home cameras and baby monitors, in some cases even "talking" to people in the room.
- **Main cause**: reuse of passwords stolen from other sites, no MFA, weak cloud accounts, unpatched software.
- **STRIDE**: Spoofing, Information disclosure.
- **Lesson**: a unique and strong password, **MFA**, protecting the cloud account, an encrypted video stream; for the manufacturer — MFA on by default, notifications about unusual sign-ins.

### 4. A connected car and a software recall (2015)

- **What happened**: researchers showed that the control functions of a connected car could be affected remotely through its infotainment system, and published their results in cooperation with the manufacturer; the manufacturer recalled about 1.4 million vehicles for a security update.
- **Main cause**: insufficient separation between the infotainment and vehicle control systems, limited ability to update remotely.
- **STRIDE**: Elevation of privilege, Tampering.
- **Lesson**: separating critical systems, signed over-the-air (OTA) updates, **coordinated disclosure** — cooperation between the researcher and the manufacturer.

## Coordinated vulnerability disclosure

**Coordinated vulnerability disclosure** (CVD) means that when a vulnerability is found, it is first reported confidentially to the manufacturer, a fix is prepared, and only then is it made public:

1. The researcher reports the vulnerability through the manufacturer's official channel (clause 2 of ETSI EN 303 645 — the manufacturer must have such a channel).
2. The manufacturer confirms it and prepares a fix.
3. At the agreed time an update is released and the vulnerability is announced (often with a **CVE** identifier).
4. Users install the update.

This process protects users during the period when a vulnerability is known but not yet fixed.

## Who should do what

| Party | Recommendations |
|---|---|
| **Manufacturer** | unique passwords, security by design, signed updates and a published support period, a channel for vulnerability reports, MFA by default |
| **Organization** | an IoT device inventory, a separate segment, security requirements in procurement, regular audits (M15), monitoring outbound traffic |
| **User** | changing the factory password, installing updates, enabling MFA, disabling unnecessary features (remote access, UPnP), replacing unsupported devices |

## Practical work: a case analysis or presentation

A group of 3–4 people chooses one IoT system (a smart home, video surveillance, smart meters, a wearable device, a connected car, smart city traffic lights) or studies one of the cases above in more depth.

**Presentation structure** (10–12 slides, 10 minutes):

1. The system and its architecture (four layers).
2. Assets and data flows, personal data.
3. A STRIDE threat table.
4. Comparison with ETSI EN 303 645.
5. A real incident (if any) — cause and consequence, with open sources.
6. A risk matrix and priority measures.
7. Recommendations for the manufacturer, the organization and the user.
8. Conclusion and sources.

**Assessment criteria**:

| Criterion | Points |
|---|---|
| The architecture and data flows are described correctly | 20 |
| The threat model (STRIDE) is complete and justified | 25 |
| Comparison with the baseline requirements | 15 |
| The measures are specific, feasible and prioritized | 25 |
| Presentation quality, sources, answers to questions | 15 |
| **Total** | **100** |

:::warning
**Common mistakes**

- **Describing attack techniques in detail.** The goal is causes and protection; technical "recipes" are not needed.
- **Only technical measures.** Manufacturer policy, procurement requirements and user habits matter too.
- **Claims without sources.** Confirm every fact with a reliable source.
- **Forgetting privacy.** Cameras and sensors collect data about private life.
:::

:::tip
Give the presentation's key message in one sentence and write each slide title as a full assertion, for example: "80% of home camera security depends on three simple measures: a unique password, MFA and updates".
:::

## Summary

- Analysing an IoT case: system → assets and flows → STRIDE → baseline requirements → risk → measures.
- Lessons from the cases: default passwords (Mirai), lack of segmentation (the aquarium thermometer), reused passwords and no MFA (cameras), separating systems and updates (the connected car).
- Coordinated disclosure protects users until a fix is ready.
- Protection is the joint responsibility of the manufacturer, the organization and the user.

## Check yourself

:::check
Q: Which threat does the letter "I" in STRIDE stand for?
A: Information disclosure — data being exposed without authorization.
Q: What main lesson was learned from the Mirai incident?
A: The need to abandon universal default passwords and disable unnecessary services.
Q: Which measure could have prevented the aquarium thermometer incident?
A: Separating IoT devices into their own network segment and controlling outbound traffic.
Q: What is coordinated vulnerability disclosure?
A: A vulnerability is first reported confidentially to the manufacturer and is made public only after a fix has been released.
:::

:::link https://owasp.org/www-project-internet-of-things/
OWASP Internet of Things Project
The main risks and recommendations on IoT security.
:::
