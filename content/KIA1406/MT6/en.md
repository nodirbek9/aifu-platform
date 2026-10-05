---
title: Computer viruses and methods of protection against them. An analysis of foreign digital signature algorithms. The concept and tasks of identification and authentication
summary: The concept of a computer virus and how it differs from other malware; classification of viruses by environment (file, boot sector, macro, script) and life cycle; signs of infection; protection methods — prevention, detection, recovery; foreign digital signature algorithms — RSA-PSS, DSA, ECDSA, EdDSA (USA, international), GOST R 34.10-2012 (Russia), SM2 (China), O'z DSt 1092:2009; post-quantum signatures — ML-DSA, SLH-DSA; criteria for comparing algorithms; the tasks of identification and authentication.
---

## Why does this topic matter?

The topic has three parts, but one question unites them: **whose (or what) code and documents can a system trust?** A virus is unauthorized code; a digital signature is a means of proving who created a document or program (for example, signed updates prevent their replacement by viruses); identification and authentication determine who is using the system. Different countries have developed their own digital signature standards, and knowing the differences matters in international cooperation.

:::note
**After this assignment you will be able to:**

- distinguish a computer virus from other malware and classify viruses;
- know the signs of infection and the protective measures;
- name the main foreign digital signature algorithms and their mathematical basis;
- compare digital signature algorithms by security, key length and speed;
- explain the tasks of identification and authentication.
:::

## The computer virus

A **computer virus** is a program that reproduces by adding its code to (infecting) another program, document or system area, and becomes active when the infected object runs. Its main distinguishing feature is that it needs a **host object**: a worm spreads on its own, and a Trojan disguises itself as a useful program but does not reproduce (a detailed classification is in MT4, MT5).

**Classification by environment**:

| Type | What it infects | Historical example |
|---|---|---|
| **File viruses** | executable files | DOS viruses of the 1990s |
| **Boot viruses** | the boot sector of a disk or flash drive | Brain (1986), Michelangelo |
| **Macro viruses** | macros in office documents | Melissa (1999) |
| **Script viruses** | script files (VBS, JS) | ILOVEYOU (2000, which also had worm features) |

"Classic" viruses are rare in modern attacks; today the main threats are worms, Trojans, ransomware and malware that works without creating files. But in everyday language the word "virus" is used for all malware.

**Signs of infection**: the computer slows down sharply; unknown processes, programs or browser extensions appear; the antivirus turns off or stops updating; files change or do not open; unknown network activity; messages are sent to friends in your name.

**Protection methods**:

| Stage | Measures |
|---|---|
| **Prevention** | updating the OS and programs, installing only from official sources, keeping macros disabled by default, everyday work in a standard account (MT17), control of removable media, awareness |
| **Detection** | antivirus or EDR (MT9), log monitoring, file integrity checking (A8) |
| **Response and recovery** | isolating the infected device from the network, a full scan, reinstalling the OS if needed, restoring from a backup (MT17), changing passwords |

## Foreign digital signature algorithms

| Algorithm | Country / standard | Mathematical basis | Features |
|---|---|---|---|
| **RSA-PSS** (PKCS #1) | USA, international (RFC 8017) | the difficulty of factoring large numbers | the most widespread; key 2048–4096 bits; the signature equals the key length |
| **DSA** | USA, FIPS 186 | the discrete logarithm in a finite field | no longer recommended for new signatures in FIPS 186-5 |
| **ECDSA** | USA (FIPS 186-5), international | the discrete logarithm on an elliptic curve (P-256, P-384) | short keys (256 bits ≈ RSA 3072); TLS, Bitcoin; the random number for each signature must be chosen correctly |
| **EdDSA (Ed25519)** | international (RFC 8032), FIPS 186-5 | an Edwards curve | fast, deterministic (no risk of random-number mistakes), in SSH and modern protocols |
| **GOST R 34.10-2012** | Russia | an elliptic curve | 256- and 512-bit variants, hash — GOST R 34.11-2012 (Streebog) |
| **SM2** | China | an elliptic curve | hash — SM3; a Chinese commercial cryptography standard |
| **O'z DSt 1092:2009** | Uzbekistan | the discrete logarithm | the national digital signature standard, hash — O'z DSt 1106:2009 (M8) |
| **ML-DSA** (FIPS 204, 2024) | USA, post-quantum | module lattices | considered resistant to quantum computers; larger signatures and keys |
| **SLH-DSA** (FIPS 205, 2024) | USA, post-quantum | hash functions only | very conservative security, but large and slow signatures |

**Comparison criteria**: the security level (in bits); key and signature length; signing and verification speed; robustness against implementation mistakes (for example, in ECDSA reusing the random number reveals the private key — this is how the Sony PlayStation 3 key leaked in 2010); standardization and legal recognition; the **threat of quantum computers** — Shor's algorithm could break RSA and elliptic curves, which is why a transition to post-quantum algorithms is being planned.

**Why countries create their own standards**: cryptographic independence, the question of trust in foreign algorithms, a national certification system. As a result, international document exchange requires supporting several algorithms or mutual recognition agreements.

## The tasks of identification and authentication

Covered in detail in M10; briefly:

- **Identification** — assigning a unique identifier to a subject and recognizing it (a login, an ID number, the Subject in a certificate).
- **Authentication** — confirming the claimed identity (knowledge, possession and inherence factors; MFA).
- **Tasks**: preventing unauthorized access; the basis for authorization (M11); accountability — recording in logs who did what; non-repudiation — together with a digital signature, proving that an action was performed by exactly this person.

A digital signature is also an authentication tool: a certificate binds the owner's identifier (Subject) to their public key, and signing with the private key proves possession of it.

:::warning
**Common mistakes**

- **Calling any malware a "virus".** In academic work apply the classification precisely: virus, worm, Trojan and so on.
- **Enabling macros in a document "to open it".** Do not enable macros in a document from an unknown source.
- **Comparing signature algorithms by key length alone.** A 256-bit ECDSA key gives the same security as a 3072-bit RSA key.
- **Generating the random number incorrectly in ECDSA.** One mistake reveals the private key; use proven libraries.
- **Ignoring the quantum threat.** Long-lived signatures and encrypted data require post-quantum planning.
:::

:::tip
In an essay compare the algorithms in a table and cite the official standard document for each (FIPS 186-5, FIPS 204, RFC 8032, GOST R 34.10-2012, O'z DSt 1092:2009). On your computer, `openssl list -signature-algorithms` shows which signature algorithms are supported.
:::

## Summary

- A virus reproduces by infecting a host object; by environment — file, boot, macro and script viruses.
- Protection: prevention (updates, official sources, restricting macros, a standard account), detection (antivirus, EDR), recovery (isolation, backups).
- Signature algorithms: RSA-PSS, DSA, ECDSA, EdDSA, GOST R 34.10-2012, SM2, O'z DSt 1092:2009; post-quantum — ML-DSA, SLH-DSA.
- Comparison: security, key and signature size, speed, robustness against implementation mistakes, standardization, the quantum threat.
- Identification and authentication are the basis for preventing unauthorized access, authorization, accountability and non-repudiation.

## Check yourself

:::check
Q: How does a virus differ from a worm?
A: A virus needs a host object (a file, document, boot sector) to reproduce; a worm spreads on its own.
Q: Why is Ed25519 more robust against implementation mistakes than ECDSA?
A: It is deterministic: no random number has to be generated for each signature, so the key cannot leak through that mistake.
Q: What are the digital signature standards of Russia and China called?
A: GOST R 34.10-2012 (Russia) and SM2 (China).
Q: Why is a transition to post-quantum signature algorithms being planned?
A: On quantum computers Shor's algorithm could break signatures based on RSA and elliptic curves.
:::

:::link https://csrc.nist.gov/pubs/fips/186-5/final
FIPS 186-5: Digital Signature Standard (DSS)
The current US standard for the RSA, ECDSA and EdDSA signature algorithms.
:::
