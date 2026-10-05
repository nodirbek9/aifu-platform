---
title: A methodology for detecting unauthorized use of information systems and resources. Protecting information resources in wireless communication systems
summary: The process of detecting unauthorized use — data sources (logs, network, host), centralized log collection, time synchronization, SIEM and correlation rules, baselines and anomalies, alert triage, incident response phases (NIST SP 800-61); typical indicators; wireless network security — Wi-Fi standards, WEP, WPA2, WPA3, Personal and Enterprise (802.1X, RADIUS) modes, guest networks, rogue access points and detecting them, Bluetooth security; configuring home and office Wi-Fi securely.
---

## Why does this topic matter?

However strong the protection, sooner or later someone will try to get around it. According to reports, the time between a breach and its detection is often measured in weeks and months. **Detecting unauthorized use quickly** sharply reduces the damage. The second topic — wireless networks — extends an organization's perimeter beyond the building, into the car park and neighbouring offices: radio waves do not respect walls.

:::note
**After this assignment you will be able to:**

- explain the data sources and the process for detecting unauthorized use;
- know the idea of SIEM, correlation and anomaly detection;
- name the phases of incident response;
- distinguish Wi-Fi security standards and modes;
- configure a home or office Wi-Fi network securely.
:::

## A methodology for detecting unauthorized use

**1. Data sources**:

| Source | What it shows |
|---|---|
| OS logs (Windows Security, Linux auth.log) | logons, failed attempts, permission changes (A10) |
| Application and database logs | who accessed which record |
| Firewall, VPN and proxy logs | connections, blocked attempts |
| IDS/IPS and EDR alerts | known attack signs, suspicious processes (MT4, MT9) |
| Cloud and identity service logs | the place and time of logins, MFA results |
| Physical access systems | who entered the building and when (M12) |

**2. Collection and preparation**: logs are sent to a central server (a copy remains even if an attacker deletes the log on the device); **time is synchronized** on all devices (NTP) — otherwise events cannot be linked in sequence; the retention period is set by law and policy.

**3. Analysis**:

- **Rules and correlation** (SIEM): linking several events, for example "20 failed logons to one account in 10 minutes, then a successful logon".
- **Baselines and anomalies**: normal behaviour (working hours, usual location, usual data volume) and deviations from it — logins at night, impossible travel (logins from two countries within an hour), downloading more than usual.
- **Threat intelligence**: comparing with lists of known malicious addresses and file hashes.

**Typical indicators**: logons to a privileged account outside working hours; a new administrator account or addition to a group; cleared logs; an unknown program or service; a large volume of data going to an unknown external address; logon attempts to many accounts from one address.

**4. Alert triage**: there are many false alerts — prioritization (asset value, confidence level), context and confirmation are needed. This is the main work of a SOC analyst.

**5. Incident response** (per NIST SP 800-61): **preparation** → **detection and analysis** → **containment, eradication and recovery** → **post-incident activity** (lessons learned, improving rules). Preserving evidence (the chain of custody and hashes from MT3) matters for legal proceedings.

## Wireless network security

| Standard | Status |
|---|---|
| **Open network** | no encryption — do no confidential work without a VPN or HTTPS (MT15) |
| **WEP** | completely broken, do not use |
| **WPA (TKIP)** | outdated |
| **WPA2-Personal (AES/CCMP)** | widespread; its strength depends on the shared password — a weak password can be guessed offline |
| **WPA3-Personal (SAE)** | recommended: resistant to offline password guessing, forward secrecy |
| **WPA2/WPA3-Enterprise (802.1X)** | each user with their own account or certificate via a RADIUS server; for organizations |

**Measures for an office**: Enterprise mode (no shared password — when an employee leaves, only their account is blocked); separate networks (VLANs) for staff, guests and IoT devices; no access from the guest network to the internal network; updating the firmware of access points and the controller; detecting **rogue access points** (installed without authorization or imitating the organization's name) with a wireless IDS (WIDS); tuning signal strength so that it does not reach unnecessarily far outside the building.

**Measures for home**: change the router's default administrator password; WPA3 (or at least WPA2-AES) and a long password; **disable WPS**; update the router firmware; disable remote management; a separate network for guests and "smart" devices.

**For users**: on public Wi-Fi use only HTTPS sites and a VPN; even if a network name looks familiar, it may be fake; turn off automatic connection; turn off Wi-Fi and **Bluetooth** when not in use, and pair Bluetooth devices only in a trusted place.

:::warning
**Common mistakes**

- **Keeping logs only on the device itself.** An attacker deletes them first.
- **Devices with unsynchronized time.** The chain of events cannot be reconstructed.
- **Nobody looks at the alerts.** Collecting into a SIEM is a start; an analysis and response process is needed.
- **One shared Wi-Fi password in an office.** The number of people who know it grows uncontrollably over time.
- **Leaving WPS and the default router password.** These are the simplest weaknesses of home networks.
:::

:::tip
On your own Windows computer look through the logons of the last week in `eventvwr.msc` → the Security log (4624, 4625): at what times and of what types were the logons? This is a simple exercise in understanding a "baseline". In your router's settings check the security mode, WPS and the firmware version.
:::

## Summary

- The detection methodology: identify sources → centralized collection and time synchronization → rules, correlation, anomalies → triage → response.
- Typical indicators: privileged logons outside working hours, new administrators, cleared logs, large outbound data transfers.
- Incident response: preparation, detection, containment and recovery, lessons learned; preserving evidence.
- Wi-Fi: WEP and WPA are outdated; WPA3 or WPA2-AES; in organizations Enterprise (802.1X), segmentation, detecting rogue access points.
- At home: the router password, disabling WPS, updates, a guest network.

## Check yourself

:::check
Q: Why are logs sent to a central server?
A: A copy is kept even if an attacker deletes the log on the device, and different sources can be analyzed together.
Q: What is the "impossible travel" anomaly?
A: Logons to one account from distant places within a short time — a sign of stolen credentials.
Q: What is the main advantage of WPA2-Enterprise over Personal mode?
A: There is no shared password: each user logs in with their own account or certificate and can be blocked individually.
Q: What is a rogue access point?
A: An access point installed without authorization or imitating the name of a trusted network.
:::

:::link https://csrc.nist.gov/pubs/sp/800/61/r3/final
NIST SP 800-61: Incident Response Recommendations
Recommendations for detecting and responding to security incidents.
:::
