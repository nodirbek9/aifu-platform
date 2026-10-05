---
title: Hash functions and digital signatures. Hash, MAC, digital signature, PKI, X.509 certificates
summary: The concept and properties of a hash function — one-wayness, collision resistance, avalanche effect; algorithms — MD5, SHA-1, SHA-2, SHA-3; the birthday paradox; storing passwords (salt, slow hashes); MAC and HMAC; the digital signature — creation and verification, RSA and ECDSA signatures; digital signatures in Uzbekistan; PKI — certification authority, certificate chain, revocation; the structure of an X.509 certificate.
---

## Why does this topic matter?

How do we know that a downloaded program was not altered on the way, that a password stolen from a database still cannot be read, that an electronic document was signed by exactly this manager? The answer is **hash functions** and the **digital signature** built on them. In Uzbekistan, tax reports, the public services portal and electronic document exchange all work with digital signature keys — every specialist should know how they work.

:::note
**After this lesson you will be able to:**

- name the properties of a hash function and the modern algorithms;
- demonstrate the avalanche effect and the birthday paradox with calculations;
- explain how to store passwords safely;
- explain the difference between a MAC and a digital signature, and the steps of creating and verifying a signature;
- describe the structure of PKI and the main fields of an X.509 certificate.
:::

## Hash function

A **cryptographic hash function** *H* turns data of any length into a fixed-length value (the hash, a "fingerprint").

| Property | Meaning |
|---|---|
| **Determinism** | the same input always gives the same hash |
| **One-wayness** (preimage resistance) | finding *x* from *H*(*x*) is practically impossible |
| **Second preimage resistance** | for a given *x* one cannot find another *x'* with *H*(*x'*) = *H*(*x*) |
| **Collision resistance** | one cannot find any pair with *H*(*x*) = *H*(*y*) |
| **Avalanche effect** | changing one input bit changes about half of the hash bits |

| Algorithm | Hash length | Status |
|---|---|---|
| MD5 | 128 bits | **broken** (collisions in seconds) — do not use it for security |
| SHA-1 | 160 bits | **broken** (the 2017 SHAttered collision) |
| SHA-256, SHA-512 (SHA-2) | 256, 512 bits | secure, widely used |
| SHA-3 (Keccak) | 224–512 bits | secure, a different internal design (the "sponge") |
| O'z DSt 1106:2009 | 256 bits | Uzbekistan's national hash standard |

Uses: checking file integrity (the SHA-256 values on download pages), storing passwords, digital signatures, blockchain, digital forensics.

**The birthday paradox**: in a group of 23 people, the probability that two share a birthday exceeds 50%. Hashes behave the same way: for a *b*-bit hash a collision can be found in about **2^(*b*/2)** attempts. That is why a hash needs at least 256 bits for collision resistance (128-bit security).

**Storing passwords**: not the password itself but its hash is stored. Plain SHA-256 is not enough, though — an attacker finds simple passwords with precomputed tables (rainbow tables) and billions of attempts per second. The right way: add a random **salt** per user and use deliberately **slow** algorithms — **bcrypt, scrypt, Argon2, PBKDF2**.

## MAC and HMAC

A hash checks integrity, but anyone can compute it: an attacker can replace both the file and its hash. A **MAC** (Message Authentication Code) is a check value computed with a **secret key**: someone who does not know the key and changes the message cannot produce a matching MAC. The most common one is **HMAC** = *H*((*K* ⊕ opad) ‖ *H*((*K* ⊕ ipad) ‖ *M*)), for example HMAC-SHA256 (JWT tokens, API signatures). A MAC provides integrity and authentication but not **non-repudiation**: both sides hold the key, so one cannot prove who created the MAC.

## Digital signature

A digital signature is based on asymmetric cryptography: only the owner of the **private key** can create the signature, and anyone can verify it with the **public key**.

1. **Signing**: the document hash *h* = *H*(*M*) is computed; *h* is signed with the private key → signature *S*.
2. **Sending**: the document *M* and the signature *S* (and usually the certificate).
3. **Verification**: the receiver computes *H*(*M*) themselves and checks the signature with the sender's public key; if it matches, the document is unchanged and it was signed by exactly this key owner.

A digital signature provides three services: **integrity**, **authenticity** and **non-repudiation**. Algorithms: RSA-PSS, ECDSA, EdDSA (Ed25519); in Uzbekistan — the national standard O'z DSt 1092:2009. Under the Law "On Electronic Digital Signature", a digital signature is equivalent to a handwritten one; keys and certificates are issued by authorized centers (for example, for public services and the tax system).

## PKI and X.509 certificates

How do we know that a public key really belongs to "Bank X"? **PKI** (Public Key Infrastructure) solves this through a trusted third party.

| Element | Role |
|---|---|
| **Certification authority** (CA) | issues a certificate, signed by itself, that binds a public key to its owner |
| **Registration authority** (RA) | verifies the owner's identity |
| **Root CA** | self-signed; its public key is preinstalled in operating systems and browsers |
| **Intermediate CA** | signed by the root CA; issues user certificates (the root key is kept offline) |
| **Revocation** | certificates revoked before expiry — the CRL list or the OCSP service |

**Chain of trust**: site certificate → intermediate CA → root CA. The browser checks the signature of each certificate with the public key of the one above it, and the root must be on the trusted list.

**X.509 certificate** fields:

| Field | Contents |
|---|---|
| Version, Serial Number | version (3), a unique serial number |
| Signature Algorithm | the CA's signature algorithm (for example, sha256WithRSAEncryption) |
| Issuer | the CA that issued the certificate |
| Validity | the validity period (Not Before, Not After) |
| Subject | the owner (site name, organization) |
| Subject Public Key Info | the owner's public key |
| Extensions | Subject Alternative Name (domains), Key Usage, Basic Constraints and others |
| CA signature | a signature over the hash of all the fields above |

## Worked example 1: the avalanche effect

For teaching purposes we take the simple 32-bit **FNV-1a** hash function (it is not cryptographic, but it shows the avalanche effect well): *h* = 2166136261; for each byte *h* = (*h* ⊕ byte) · 16777619 mod 2³². Print the hash of each line and how many bits differ from the previous line's hash.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF   # mod 2^32
    return h


oldingi = None
for satr in sys.stdin.read().split("\n"):
    if not satr:
        continue
    h = fnv1a(satr)
    qator = format(h, "08x") + " " + satr
    if oldingi is not None:
        qator += " (diff: " + str(bin(h ^ oldingi).count("1")) + " bit)"   # changed bits
    print(qator)
    oldingi = h
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;                                       // 2166136261
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;                   // mod 2^32 (int overflow)
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        Integer oldingi = null;
        while (in.hasNextLine()) {
            String satr = in.nextLine();
            if (satr.isEmpty()) continue;
            int h = fnv1a(satr);
            String qator = String.format("%08x", h) + " " + satr;
            if (oldingi != null) {
                qator += " (diff: " + Integer.bitCount(h ^ oldingi) + " bit)";   // changed bits
            }
            System.out.println(qator);
            oldingi = h;
        }
    }
}
```
```cpp
#include <bitset>
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;                               // mod 2^32 (uint32_t overflow)
    }
    return h;
}

int main() {
    string satr;
    bool bor = false;
    uint32_t oldingi = 0;
    while (getline(cin, satr)) {
        if (satr.empty()) continue;
        uint32_t h = fnv1a(satr);
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", h);
        string qator = string(buf) + " " + satr;
        if (bor) {
            qator += " (diff: " + to_string(bitset<32>(h ^ oldingi).count()) + " bit)";   // changed bits
        }
        cout << qator << endl;
        oldingi = h;
        bor = true;
    }
    return 0;
}
```

Input:

```text
salom
Salom
salom!
parol123
```

Output:

```text
6883e297 salom
10b9bd37 Salom (diff: 16 bit)
3d9de482 salom! (diff: 15 bit)
1e16e5c5 parol123 (diff: 12 bit)
```

Changing the case of one letter or adding one character changed about half of the 32 bits — looking at the hash, one cannot tell how "close" the inputs were. But FNV-1a is **not cryptographic**: finding collisions on purpose is easy. Real integrity checks use SHA-256 — that is the task of practical session A8.

## Worked example 2: the birthday paradox

Each line holds the hash length *b* (bits) and the number of hashed items *k*. Print the collision probability *P* ≈ 1 − e^(−*k*(*k*−1) / 2^(*b*+1)) and the number of items needed to reach 50% probability, ≈ 1.1774 · 2^(*b*/2).

```python
import math
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    b, k = map(int, satr.split())
    ehtimol = -math.expm1(-k * (k - 1.0) / 2.0 ** (b + 1))   # an accurate 1 − e^(−x)
    yarim = 1.1774 * 2.0 ** (b / 2)
    print(f"{b} bit, {k} items: P = {ehtimol:.2e}, 50% at ~{yarim:.2e} items")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int b = in.nextInt();
            long k = in.nextLong();
            double ehtimol = -Math.expm1(-k * (k - 1.0) / Math.pow(2.0, b + 1));   // an accurate 1 − e^(−x)
            double yarim = 1.1774 * Math.pow(2.0, b / 2.0);
            System.out.println(String.format("%d bit, %d items: P = %.2e, 50%% at ~%.2e items", b, k, ehtimol, yarim));
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    int b;
    long long k;
    while (cin >> b >> k) {
        double ehtimol = -expm1(-k * (k - 1.0) / pow(2.0, b + 1));    // an accurate 1 − e^(−x)
        double yarim = 1.1774 * pow(2.0, b / 2.0);
        printf("%d bit, %lld items: P = %.2e, 50%% at ~%.2e items\n", b, k, ehtimol, yarim);
    }
    return 0;
}
```

Input:

```text
32 10000
32 100000
64 100000000
128 1000000000000
```

Output:

```text
32 bit, 10000 items: P = 1.16e-02, 50% at ~7.72e+04 items
32 bit, 100000 items: P = 6.88e-01, 50% at ~7.72e+04 items
64 bit, 100000000 items: P = 2.71e-04, 50% at ~5.06e+09 items
128 bit, 1000000000000 items: P = 1.47e-15, 50% at ~2.17e+19 items
```

With a 32-bit hash (4 billion values) the collision probability reaches 50% at only 77 thousand items — that is why short hashes are useless for security. At 128 bits the probability is 10⁻¹⁵ even for a trillion items — but if an attacker can afford 2⁶⁴ attempts, there is a risk; the modern requirement is at least 256 bits.

:::warning
**Common mistakes**

- **Treating a hash as encryption.** A hash is one-way — it cannot be "decrypted"; there is no key either.
- **Storing passwords with MD5 or unsalted SHA-256.** Precomputed tables and GPUs find simple passwords quickly; use bcrypt or Argon2.
- **Using MD5 and SHA-1 for security.** Both are broken with respect to collisions.
- **Treating a MAC as a signature.** A MAC does not provide non-repudiation — both sides hold the key.
- **Ignoring certificate warnings.** "Untrusted certificate" may be a sign of a man-in-the-middle attack.
:::

:::tip
On Windows, compute the hash of any file with `certutil -hashfile file.exe SHA256` (in PowerShell, `Get-FileHash file.exe`) and compare it with the value published on the site you downloaded the program from. In a browser, click the padlock icon of any HTTPS site and look at the certificate chain (site → intermediate → root CA).
:::

## Summary

- Hash: fixed length, one-wayness, collision resistance, avalanche effect; MD5 and SHA-1 are broken, SHA-2 and SHA-3 are secure.
- Birthday paradox: a collision in ~2^(*b*/2) attempts; hence 256 bits.
- Passwords: salt + slow algorithms (bcrypt, scrypt, Argon2, PBKDF2).
- MAC (HMAC) — keyed integrity and authentication; a digital signature — signed with the private key, verified with the public key, provides non-repudiation.
- PKI: CA, RA, root and intermediate CAs, chain of trust, CRL/OCSP; an X.509 certificate binds the owner and their public key with the CA's signature.

## Check yourself

:::check
Q: What does collision resistance mean?
A: Finding two different inputs with the same hash is practically impossible.
Q: Why are bcrypt or Argon2 recommended for passwords instead of plain SHA-256?
A: They are deliberately slow and use a salt — this makes precomputed tables and high-speed guessing ineffective.
Q: What is the main difference between a MAC and a digital signature?
A: A MAC is computed with a symmetric key and does not provide non-repudiation; a digital signature is created with a private key, and only its owner can create it.
Q: The browser checks who signed a site's certificate. Who is that?
A: A certification authority (CA) — usually an intermediate CA, which is in turn linked to a root CA.
Q: Which field of an X.509 certificate shows which domains the certificate is valid for?
A: The Subject Alternative Name (SAN) extension.
:::

:::link https://www.rfc-editor.org/rfc/rfc5280
RFC 5280: X.509 Public Key Infrastructure Certificate Profile
The specification of X.509 certificates and the CRL structure.
:::
