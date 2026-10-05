---
title: Analyzing real physical security cases (case study)
summary: A model for analyzing physical security incidents — threat type, the protection layer that failed, the control function that did not work (deter, delay, detect, respond); cases — Stuxnet and entering an air-gapped network via USB, the theft of a US Department of Veterans Affairs laptop, the fire at an OVHcloud data centre, the physical penetration test at an Iowa courthouse, data left on second-hand drives; general conclusions; a physical audit task for your own building; a test on the topic.
---

## Why does this topic matter?

In lecture M12 we covered physical threats and control measures in theory. Real incidents show that the greatest damage comes not from a lack of sophisticated technology but from **simple** things: an unencrypted laptop, a backup not kept in another building, a door left open, a stranger's flash drive plugged in. In this session we analyze five cases with a single model.

:::note
**After this session you will be able to:**

- analyze a physical security incident by threat type, protection layer and control function;
- identify in each case which cheap measure could have prevented the damage;
- explain how physical and information security depend on each other;
- carry out a simple physical audit of your own building.
:::

## The analysis model

We examine each case with the following questions:

| Question | Options |
|---|---|
| Threat type | natural, technological, human (external or insider) |
| Which layer was breached | perimeter, building, room, device, storage medium |
| Which function failed | deter, delay, detect, respond |
| Which property was violated | confidentiality, integrity, availability |
| What measure would have helped | technical (encryption, lock, sensor), organizational (policy, training), physical (another location) |

## Cases

:::case
**Case 1. Stuxnet (2010).** The network of the uranium enrichment plant in Natanz, Iran, was **physically separated** from the Internet (an air gap). Nevertheless, the malware reached the Siemens controllers that drove the centrifuges: it spread via USB flash drives and sent modified commands to the control system while showing "normal" readings to the operators. About a thousand centrifuges were damaged.

**Analysis.** Threat — human (targeted, state-level); layer — storage media and devices; failed functions — delay (control of media brought in) and detection. Violated properties — integrity and availability. Lesson: an air gap is not absolute protection; removable media management (only checked, registered devices, autorun disabled, USB ports blocked) and independent monitoring of process readings are needed.
:::

:::case
**Case 2. The Department of Veterans Affairs laptop (USA, 2006).** A laptop and an external drive that an employee had taken home were stolen from the house. They held the names, social security numbers and dates of birth of 26.5 million service members and veterans — **unencrypted**. The devices were later recovered and it was reported that the data had not been used, but large sums went on notifications, credit monitoring and lawsuits; the department revised its security policy.

**Analysis.** Threat — human (ordinary, untargeted theft); layer — a device outside the office; failed functions — delay (no encryption) and policy (permission to take confidential data home). Violated property — confidentiality. Lesson: full-disk encryption on all laptops and removable drives (BitLocker, FileVault); restricting the copying of confidential data to devices; a procedure for reporting losses immediately. If an encrypted device is stolen, the loss is only the price of the device, not a data breach.
:::

:::case
**Case 3. The fire at an OVHcloud data centre (Strasbourg, March 2021).** The SBG2 building of one of Europe's largest hosting providers burned down completely, and the neighbouring SBG1 was partly damaged. About 3.6 million websites went offline. Customers whose backups were kept **on the same campus**, or who had no backups at all, lost their data for good. According to the investigation, the fire started in power supply equipment; the building's natural-air cooling design helped the fire spread quickly.

**Analysis.** Threat — technological (fire); layer — the whole building; failed functions — delay (containing the fire) and response (automatic suppression was not enough). Violated property — availability (and loss of data). Lesson: the **3-2-1 rule** — 3 copies, 2 different media, 1 in another location (another city or region); with a cloud provider, too, the backup must be in another region; the recovery plan is tested regularly.
:::

:::case
**Case 4. The penetration test at an Iowa courthouse (USA, September 2019).** Two penetration testers working under a contract with the state court administration entered the Dallas County courthouse at night by opening a door lock and set off the alarm. They waited for the police and showed their contract, but the county administration that owned the building had not agreed to such a test — the testers were arrested. The charges were later dropped, but the incident was widely discussed in the industry.

**Analysis.** This was not an attack but an authorized test — yet it showed two things. First, the building door was opened, but **detection** (the alarm) and **response** (the police arrived quickly) worked — this is exactly how defense in depth should work. Second, for a physical penetration test it must be agreed clearly in writing **who can really authorize it** (the building owner), the scope of the test, its timing and the emergency contact procedure. Lesson: an unauthorized "test" is an offence; authorization documents must be with the tester and signed by the property owner.
:::

:::case
**Case 5. Second-hand drives and phones.** Over a number of years, data recovery companies and universities have bought used drives, memory cards and phones at online auctions and found that a significant share of them (depending on the study, one in two or three) still held personal and corporate data — documents, photos, passwords, customer databases. In many cases the owners had "deleted" the files or quick-formatted the drive.

**Analysis.** Threat — human (anyone taking the opportunity); layer — the end of the storage medium's life; failed function — delay (no destruction process). Violated property — confidentiality. Lesson: Clear, Purge or Destroy according to NIST SP 800-88; tracking media and certificates of destruction; requiring a certificate from disposal contractors; disk encryption from the start — then cryptographic erase is instant.
:::

## General conclusions

| Observation | In which case | Cheap measure |
|---|---|---|
| A removable medium bypassed the protection | Stuxnet | USB control, autorun disabled |
| A device was stolen and the data was in the clear | Department of Veterans Affairs | full-disk encryption |
| The backup was lost together with the original system | OVHcloud | the 3-2-1 rule, a copy in another region |
| The door was opened, but detection and response worked | Iowa courthouse | layered protection, an alarm and a response plan |
| "Deleted" data was recovered | Second-hand drives | NIST 800-88, a certificate of destruction |

The general lesson: physical and information security are one whole. Encryption reduces the consequences of a device theft, and physical control closes the starting points of a cyberattack (USB, console, server room).

## Independent analysis task

Observe the building where you study or work (or one floor of it) and write a short **physical audit** report. Do not break any protection and do not enter unauthorized areas — record only what is openly visible:

1. Perimeter and entry: is there access control (guard, turnstile, card)? Are visitors registered?
2. Tailgating: is it easy to walk in behind someone through the doors?
3. Workplaces: are screens locked, are there documents or notes with passwords on desks?
4. Technical rooms: are server or network cabinets locked and labelled?
5. Environment: are there fire detectors, extinguishers, emergency exit signs?
6. Destruction of documents and media: is there a paper shredder or a special container?

For each finding propose a risk level (low, medium, high) and a cheap measure. Do not name specific people, passwords or codes in the report.

:::warning
**Common mistakes**

- **"We have cybersecurity — physical security is not needed".** Physical access to a console or a disk bypasses much of the protection.
- **Keeping the backup in the same room or building.** A fire or flood destroys both.
- **An unauthorized "test".** Even a well-meant physical penetration test without the building owner's written permission is an offence.
- **Not encrypting laptops.** Theft is the most common physical incident; encryption keeps it from turning into a data breach.
- **Plugging in a found flash drive to check it.** Hand it over to the IT or security service.
:::

:::tip
Take additional information about the cases from official reports and reliable media; do not rely on a single source. Ready-made checklists (for example, CISA's physical security guides) are a good starting point for an audit.
:::

## Summary

- Physical incidents are conveniently analyzed by threat type, breached layer, failed function and violated property.
- Even an air gap can be breached via USB (Stuxnet) — removable media are controlled.
- A stolen unencrypted laptop is a data breach; an encrypted one is only the loss of a device.
- The backup must be elsewhere (3-2-1); the cloud is not immune to fire either (OVHcloud).
- In layered protection, if one layer is breached, detection and response must work; a physical penetration test only with the owner's written permission.
- "Deleted" data can be recovered — media are destroyed according to NIST 800-88.

## Check yourself

:::check
Q: How did Stuxnet reach an air-gapped network?
A: Through USB flash drives — removable media bypassed the physical isolation.
Q: In the Veterans Affairs laptop case, which measure would have almost eliminated the damage?
A: Full-disk encryption — the thief could not have read the data.
Q: Why did some customers lose their data for good after the OVHcloud fire?
A: Their backups were kept on the same campus or did not exist; the 3-2-1 rule was not followed.
Q: What lesson about physical penetration tests comes from the Iowa case?
A: Permission must be obtained in writing from the building owner, and the scope and contact procedure must be clearly agreed.
Q: What should be done before selling a used disk?
A: Sanitize it according to NIST SP 800-88 (Clear or Purge) or destroy it; plain deletion and quick formatting are not enough.
:::

:::link https://www.cisa.gov/topics/physical-security
CISA: Physical Security
Physical security materials from the US Cybersecurity and Infrastructure Security Agency.
:::
