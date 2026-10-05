---
title: The role of cryptography in protecting information transmitted over a network. Malicious programs and protection against them (for example, Malwarebytes)
summary: Which properties cryptography provides on a network — confidentiality, integrity, authenticity; protocols by layer — Wi-Fi (WPA3), IPSec, TLS, SSH, DNSSEC and encrypted DNS (DoH, DoT), mail (S/MIME, PGP, MTA-STS), end-to-end encryption (the Signal protocol); key management and certificates; the limits of cryptography — metadata, endpoints; Malwarebytes — capabilities, scanning and quarantine, real-time protection, using it alongside another antivirus; the procedure for cleaning an infected computer.
---

## Why does this topic matter?

Any data sent over a network — a password, a message, a payment — passes through many devices (routers, providers, Wi-Fi access points), each of which could see or modify it. Cryptography protects this path; malware, on the other hand, steals data on the device itself — **before** it is encrypted or **after** it is decrypted. That is why the two topics are studied together: protecting the channel and protecting the endpoint complement each other.

:::note
**After this assignment you will be able to:**

- explain which properties cryptography provides on a network;
- name the protection protocols at different layers;
- know the limits of cryptography (metadata, endpoints);
- explain the capabilities and role of tools such as Malwarebytes;
- carry out the procedure for cleaning an infected computer.
:::

## What cryptography provides on a network

| Property | Mechanism | Example |
|---|---|---|
| **Confidentiality** | symmetric encryption (AES, ChaCha20, M6) | the content of the traffic cannot be eavesdropped |
| **Integrity** | MAC, AEAD (AES-GCM) (M8) | a packet modified in transit is rejected |
| **Authenticity** | certificates and digital signatures (M8), key exchange (M7) | you really connected to the bank's server |
| **Forward secrecy** | ephemeral DH keys (M9) | old traffic cannot be decrypted even if the key is stolen later |

## Protection protocols by layer

| Layer / area | Protocols | What they protect |
|---|---|---|
| Wireless link | WPA2, WPA3 (MT7) | between the device and the access point |
| Network | IPSec, WireGuard (M9, M14) | IP packets, VPN tunnels |
| Transport / application | TLS (HTTPS, IMAPS, SMTPS) (M9) | web, mail and API connections |
| Remote administration | SSH (M9) | server access and file transfer |
| DNS | DNSSEC (authenticity of answers), DoH / DoT (privacy of queries) | translating names into addresses |
| Email | TLS between servers, MTA-STS; S/MIME and PGP (encrypting and signing the message itself) | the content and author of a message |
| Messaging | **end-to-end encryption** (the Signal protocol — Signal, WhatsApp) | even the server cannot read the message |

**Key management**: the strength of cryptography depends on how correctly keys and certificates are created, stored, rotated and revoked (PKI, M8). Most problems lie not in the algorithm but exactly here: expired certificates, keys hard-coded in source code, passwords that are never changed.

**The limits of cryptography**:

- **Metadata** — who communicated with whom, when and how much — is often not encrypted.
- **Endpoints**: data is in the clear on the device; malware reads it before encryption (keyboard, screen) or after decryption.
- **Misuse**: ignoring certificate warnings, outdated protocols (A9).

## Protection from malicious programs and Malwarebytes

**Malwarebytes** is a program specialized in detecting and removing malware (Windows, macOS, Android, iOS). The free version is for on-demand scanning and cleaning, the paid version adds real-time protection.

| Capability | Description |
|---|---|
| **Scanning** | quick scan (memory, autostart, system areas), full scan, a selected folder |
| **Detection** | signatures, heuristics, behaviour; especially good at detecting potentially unwanted programs (PUPs) and adware |
| **Quarantine** | detected objects are isolated in a safe place; they can be restored after a mistake |
| **Real-time protection** (paid) | web protection, ransomware protection, exploit protection |
| **Alongside another antivirus** | can be used together with Windows Defender as a "second opinion" scanner; do not enable two real-time protections at once (MT9) |

**Practical work (on a test computer or in a virtual machine)**:

1. Download Malwarebytes only from the **official website**; check the installer's digital signature (file → Properties → Digital Signatures).
2. Update the database and run a **full scan**.
3. Analyze the results: the type of detected objects (malicious, PUP, suspicious) and their location; look through the quarantine section.
4. For safe testing use the **EICAR test file** — a standard test string that all antiviruses recognize as "malicious" but that does no harm; it is for checking that protection works. Never download real malware samples.
5. In the report: scan time, the number of objects checked, the detections and the decision taken on them.

**The procedure for cleaning an infected computer**:

1. Disconnect the device from the network (so that the damage does not spread and data does not leak).
2. From another, clean device, change the passwords of important accounts (email, bank).
3. Run a full scan with an updated antivirus and a second scanner (for example Malwarebytes); in safe mode if needed.
4. Quarantine the detections, then scan again.
5. With deep infection, a rootkit or ransomware, a full OS reinstallation and restoring from a clean backup (MT17) is more reliable.
6. Find the cause (which file, link or program) and take measures so that it does not repeat; in an organization, inform the IT service.

:::warning
**Common mistakes**

- **"It's HTTPS, so everything is safe".** Cryptography protects the channel, not against malware on the device or phishing.
- **Ignoring certificate warnings.** This cancels the authenticity part of the encryption.
- **Installing "cleaner" programs from ads or pop-ups.** Fake antiviruses are a common type of malware.
- **Enabling two real-time protections at once.** Conflicts and slowdowns.
- **Not changing passwords after cleaning.** Stolen passwords keep working even after the malware is removed.
:::

:::tip
In your browser look at the connection information of several sites (padlock → certificate; F12 → Security): write down the TLS version and the cipher suite (A9, M9). In the DNS settings enable "Secure DNS" (DoH) and explain what it hides.
:::

## Summary

- On a network cryptography provides confidentiality, integrity, authenticity and forward secrecy.
- Protocols by layer: WPA3, IPSec/WireGuard, TLS, SSH, DNSSEC/DoH, S/MIME/PGP, end-to-end encryption.
- Managing keys and certificates is the weakest link of cryptography; metadata and endpoints are not protected.
- Malwarebytes is a malware detection and cleaning tool; a "second opinion" alongside the main antivirus.
- Cleaning: disconnect from the network, change passwords, a full scan, reinstall if needed, find the cause.

## Check yourself

:::check
Q: How does end-to-end encryption differ from ordinary TLS?
A: The message is decrypted only on the sender's and recipient's devices; even the service's server cannot read it.
Q: What does cryptography not protect?
A: Metadata (who, with whom, when) and data on the endpoint — malware reads it before encryption or after decryption.
Q: What is the EICAR test file used for?
A: For safely checking that an antivirus works — it is harmless, but all antiviruses detect it.
Q: What is the first step in cleaning an infected computer?
A: Disconnecting the device from the network.
:::

:::link https://www.malwarebytes.com/
Malwarebytes
The official website of the malware protection tool.
:::
