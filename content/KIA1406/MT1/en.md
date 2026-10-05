---
title: Mastering the lecture and practical session topics
summary: A plan for mastering the "Fundamentals of Cybersecurity" course independently; the modules and how they depend on each other; a weekly schedule; an active reading method for lecture material; free tools for safely repeating the practical work at home (CyberChef, OpenSSL, a virtual machine); ethical and legal boundaries; a glossary of key terms; self-check questions for each module.
---

## Why does this topic matter?

Cybersecurity is a "broad" subject: it brings together law and risk management, mathematics (cryptography), operating systems and networks. In a lecture each topic is covered briefly; turning it into a skill requires re-reading the material, carrying out the commands with your own hands and checking yourself. The independent study hours are set aside exactly for this. In this assignment we look at how to plan and master the whole course.

:::note
**After this assignment you will:**

- know the course modules and how they depend on each other;
- be able to build a weekly study plan;
- be able to use an active reading method for lecture material;
- be able to prepare an environment for repeating the practical work at home safely and legally;
- be able to check your knowledge for each module.
:::

## Course structure and study plan

| Module | Lectures | Practicals | Key outcome |
|---|---|---|---|
| 1. Foundations, risks and policy | M1–M4 | A1–A4 | the CIA triad, risk = likelihood × impact, laws, security policy |
| 2. Cryptography | M5–M9 | A5–A9 | classical and symmetric ciphers, RSA, hashes, digital signatures, TLS |
| 3. Authentication, access control and physical security | M10–M12 | A10–A12 | passwords and MFA, DAC/MAC/RBAC/ABAC, physical protection |
| 4. Network, cloud and IoT security | M13–M15 | A13–A15 | network threats, protection tools, cloud and IoT |

The modules build on each other: the concept of risk (module 1) justifies the cost of every measure; cryptography (module 2) underlies authentication, VPNs and TLS; access control (module 3) reappears in network and cloud policies. So if you skip one topic, the following ones get harder.

**Weekly plan** (for each topic):

1. **Before the lecture** (20 minutes): skim the lesson text on the platform and note unfamiliar terms.
2. **After the lecture** (40 minutes): read the text actively (see below) and do the worked examples yourself.
3. **After the practical session** (40 minutes): repeat the commands and tools at home and finish the programming task.
4. **At the end of the week** (20 minutes): answer the "Check yourself" questions in writing and update your glossary.

## Reading a lecture actively

Plain reading is quickly forgotten. Use this method:

- **Start with a question.** Read the "Why does this topic matter?" section, turn it into one question (for example, "Why is storing passwords with MD5 dangerous?") and answer it at the end.
- **Redraw the tables.** Write comparison tables (for example, DAC versus MAC) in your notebook from memory, then compare with the text.
- **Change the example.** Change the input of a worked example, predict the result, then check it with the program.
- **Explain in your own words.** Write each topic in 3–4 sentences without jargon, as if explaining to a family member. The parts you cannot explain are the parts to re-read.
- **Connect.** Link each new topic with the previous ones: "Which cryptographic building blocks does TLS use?"

## Tools for practice at home

| Tool | What for | Which topics |
|---|---|---|
| **CyberChef** (in the browser, no installation) | encryption, encoding, hashes, HMAC | M5–M8, A5–A8 |
| **OpenSSL** (available in Git Bash, Linux, macOS) | keys, encryption, signatures, certificates | A6–A9 |
| **VirtualBox** + a Linux (Ubuntu) virtual machine | Linux permissions, users, a safe test environment | A11 and later topics |
| **A Windows test account** or a Windows virtual machine | password and lockout policy, the event log | A10 |
| **A password manager** (Bitwarden, KeePassXC) | protecting your own accounts | M10 |
| **The code editor on the platform** | tasks in Python, Java, C++ | all programming tasks |

A virtual machine is the most important tool: you can change settings freely, return to a "snapshot" if you make a mistake, and your main computer is not harmed.

## Ethical and legal boundaries

Cybersecurity knowledge cuts both ways: what is learned for protection can also be used for harm. That is why these rules are mandatory:

- Test only on systems that **belong to you** or for which you have **written permission** (a training virtual machine, a lab set up by the teacher).
- "Testing" the network, website or account of the university, a workplace or someone else is unauthorized access; the Criminal Code of Uzbekistan establishes liability for crimes in the field of computer information (M3).
- If you find a vulnerability, do not spread it — report it to the system owner or the teacher.
- Do not read or keep other people's personal data, passwords or correspondence.

## Glossary of key terms

Keep the glossary in a notebook or a table: the term, a definition in your own words, an example, the lesson.

| Term | Short definition |
|---|---|
| Confidentiality, integrity, availability (CIA) | the three core properties of information security |
| Threat, vulnerability, risk | a source of danger; a weakness in a system; the likelihood and size of damage |
| Encryption, decryption, key | making data secret and recovering it; the secret controlling the process |
| Symmetric and asymmetric cryptography | one shared key; a public and private key pair |
| Hash function | a fixed-length "fingerprint" of any data |
| Digital signature | a signature created with a private key and verified with a public key |
| Identification, authentication, authorization | stating who you are; proving it; determining permissions |
| MFA | at least two different authentication factors |
| Least privilege | only the rights needed for the task |
| Firewall, IDS, VPN | filtering traffic by policy; detecting attacks; an encrypted tunnel |

## Self-check by module

**Module 1.** Give one incident that violates each property of the CIA triad. What is the level of a risk with likelihood 4 and impact 5, and which category does it fall into?

**Module 2.** Why is AES not used in ECB mode? Why must factoring *n* be hard in RSA? Explain the difference between a MAC and a digital signature.

**Module 3.** Why does NIST not recommend forced periodic password changes? State the two rules of the Bell–LaPadula model. Why does formatting a disk not destroy the data?

**Module 4.** Why does rule order matter in a firewall? What protection does a VPN give, and what does it not protect against?

:::warning
**Common mistakes**

- **Studying only before the exam.** Cryptography and networks are sequential topics; accumulated gaps cannot be closed overnight.
- **Reading commands without running them.** `chmod`, `openssl`, `net accounts` are remembered only when done by hand.
- **Experimenting on your main computer.** Experiments with security settings belong in a virtual machine only.
- **Unauthorized "testing".** Testing someone else's system is an offence, even with good intentions.
- **Copying answers.** If you copy the solution of a programming task, no foundation is built for the following topics.
:::

:::tip
Every week read one piece of "cybersecurity news" (for example, about a major data breach or an important vulnerability) and analyze it with the concepts from the lessons: which property was violated, which measure would have helped. This is the best way to connect theory with real life.
:::

## Summary

- The course has four modules that build on each other: risks → cryptography → access control → network and cloud.
- For each topic: skim in advance, read actively, repeat the practice, check yourself.
- Home tools: CyberChef, OpenSSL, a virtual machine, the code editor on the platform.
- Tests only on your own or authorized systems; a vulnerability you find is reported to the owner.
- A glossary and written answers consolidate knowledge.

## Check yourself

:::check
Q: Why is the cryptography module studied before the authentication and network topics?
A: Authentication (password hashes, digital signatures, FIDO2), VPNs and TLS are based on cryptographic algorithms.
Q: What is the safest environment for experimenting with security settings?
A: A virtual machine — after a mistake you can return to a snapshot, and the main system is not harmed.
Q: Is it allowed to "check" a vulnerability in the university network?
A: No, only with the system owner's written permission; otherwise it is unauthorized access.
Q: Which active reading technique reveals what you have not understood most quickly?
A: Trying to explain the topic in your own words, without jargon.
:::

:::link https://gchq.github.io/CyberChef/
CyberChef
A free online tool for encryption, encoding and hashing — for repeating the cryptography module at home.
:::
