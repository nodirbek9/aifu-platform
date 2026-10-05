---
title: Cryptographic methods of information protection
summary: Seminar — the tasks of cryptography (confidentiality, integrity, authentication, non-repudiation); classical ciphers (the Caesar cipher) and their weakness; symmetric encryption (AES) and the key exchange problem; asymmetric encryption (RSA) and public/private keys — a small example calculated by hand; hash functions; electronic digital signatures and certificates; HTTPS; uses in the economy (banking, tax, electronic invoices); practical tasks.
---

## Purpose of the seminar

In lecture M14 encryption was mentioned as one of the protective measures. In the seminar we calculate by hand, on simple examples, how cryptography works and analyze its role in banking, tax and electronic document flow.

:::note
**After this seminar you will be able to:**

- name the four tasks of cryptography;
- distinguish symmetric and asymmetric encryption;
- show the role of public and private keys on a small example;
- explain what a hash, an electronic digital signature and a certificate are.
:::

## The tasks of cryptography

| Task | Meaning | Tool |
|---|---|---|
| **Confidentiality** | outsiders cannot read the message | encryption |
| **Integrity** | the message was not altered in transit | hash, signature |
| **Authentication** | it is confirmed whom the message really came from | signature, certificate |
| **Non-repudiation** | the author cannot deny having signed | electronic digital signature |

Key terms: **plaintext** — the original message; **ciphertext** — the encrypted message; **key** — a secret parameter for encryption and decryption. The core principle of modern cryptography (**Kerckhoffs's principle**): the algorithm may be known to everyone; security relies only on keeping the key secret.

## A classical cipher: the Caesar cipher

Each letter is shifted along the alphabet by the same step (the key). With key 3: A → D, B → E, ..., X → A.

```text
Plaintext:   BANK
Key:         3
Ciphertext:  EDQN
```

**Its weakness**: the Latin alphabet allows only 25 possible keys — trying them all takes seconds. In addition, letter frequencies are preserved. This example shows why modern ciphers use a very large key space.

## Symmetric encryption

**One key** is used for both encryption and decryption. The modern standard is **AES** (a 128- or 256-bit key): trying 2^128 keys is practically impossible. Its advantage is speed, which makes it convenient for encrypting large volumes of data (disks, backups, traffic).

**The problem — key exchange**: how do the sender and the recipient agree on the key securely? If the key is sent over an open channel, whoever intercepts it can open all the messages. Asymmetric cryptography solves this problem.

## Asymmetric encryption

Each user has **two keys**: a **public key** (given to everyone) and a **private key** (held only by its owner). A message encrypted with the public key can be opened only with the matching private key. So if Ali wants to send Vali a secret message, he encrypts it with **Vali's public key**.

**A small RSA example** (real keys are numbers with hundreds of digits; small numbers are used here for understanding):

```text
1. Two primes: p = 5, q = 11
2. n = p × q = 55;  φ = (p − 1)(q − 1) = 40
3. Public exponent e = 3 (no common divisor with 40)
4. Private exponent d: e × d ≡ 1 (mod 40) → d = 27, since 3 × 27 = 81 = 2 × 40 + 1
   Public key: (e = 3, n = 55);  private key: (d = 27, n = 55)

Encryption (m = 8):  c = 8^3 mod 55 = 512 mod 55 = 17
Decryption:          m = 17^27 mod 55 = 8
```

An attacker knows n = 55, but to find d they must factor n into primes. For 55 this is easy, but for a number with more than 600 digits it is practically impossible for existing computers — this is what RSA's security rests on.

**In practice, a hybrid scheme**: asymmetric cryptography is slow, so it is used only to exchange a random symmetric key securely, while the data itself is encrypted with AES. This is exactly how HTTPS works.

## Hash functions

A **hash function** produces a short, fixed-length "fingerprint" from data of any length (for example, **SHA-256** — 256 bits). Properties: the same input → the same hash; if one character of the input changes, the hash changes completely; the original data cannot be recovered from the hash. Uses: checking file integrity, storing passwords (not the password itself but its hash), blockchain (M12), signatures.

## Electronic digital signatures and certificates

**An electronic digital signature (EDS)**:

1. The sender computes the hash of the document and signs it with **their own private key**.
2. The recipient verifies the signature with **the sender's public key** and computes the document's hash themselves to compare.
3. If they match, the document comes from exactly this author and has not been altered.

A **certificate** confirms that a public key really belongs to this person — it is issued by a trusted **certification authority**. In Uzbekistan EDS keys and certificates are issued by authorized centres; by law an EDS is equivalent to a handwritten signature (the Law on Electronic Digital Signatures).

## Uses in the economy

| Sector | Where cryptography is used |
|---|---|
| **Banking** | HTTPS, card chips, payment confirmation, mobile banking, interbank channels |
| **Tax** | signing reports with an EDS on my.soliq.uz, electronic invoices (ESF) |
| **Electronic document flow** | contracts, orders, applications for public services (my.gov.uz) |
| **Company** | laptop disks, backups, remote work through a VPN |

:::warning
**Common mistakes**

- **Giving your EDS key (a USB stick or a file) to others.** The owner is responsible for any document signed in their name.
- **Creating "our own encryption algorithm".** Proven standards (AES, RSA, SHA-256) are used; security lies in the secrecy of the key.
- **Storing passwords in plain form.** Systems should store a special hash of the password.
- **Ignoring certificate warnings in the browser.** This may be a sign of a fake site.
:::

## Practical tasks

1. Encrypt your name with the Caesar cipher (key 5), then swap with your neighbour and try to open their ciphertext without the key (by trying all keys).
2. In the RSA example above, encrypt the message m = 2: c = 2^3 mod 55 = ?
3. Ali is sending Vali a confidential document and wants to sign it. With which key does he encrypt it, and with which key does he sign it?
4. Open any bank's website in a browser, click the padlock icon and write down to whom and by whom the certificate was issued, and its validity period.
5. List where an EDS could be used in your company's document flow.

## Summary

- The tasks of cryptography: confidentiality, integrity, authentication, non-repudiation.
- Symmetric encryption (AES) — one key, fast; the problem is key exchange.
- Asymmetric encryption (RSA) — a public and a private key; in practice a hybrid scheme (HTTPS).
- A hash is a fingerprint of data; an EDS signs the hash with the private key; a certificate confirms the owner of a public key.
- In the economy: banking, tax reports, electronic invoices, electronic document flow.

## Check yourself

:::check
Q: What is the main problem of symmetric encryption?
A: Exchanging the key securely between the sender and the recipient.
Q: With which key must a message be encrypted so that only the recipient can open it?
A: With the recipient's public key.
Q: What does an electronic digital signature confirm?
A: The author of the document and that the document has not been altered since it was signed.
Q: What does Kerckhoffs's principle say?
A: The algorithm may be known to everyone; security relies only on keeping the key secret.
:::

:::link https://www.khanacademy.org/computing/computer-science/cryptography
Khan Academy: Cryptography
Interactive lessons from the Caesar cipher to public-key cryptography.
:::
