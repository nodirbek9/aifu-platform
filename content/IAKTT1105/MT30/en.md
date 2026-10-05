---
title: Using cryptographic methods to protect information
summary: Independent study — applying cryptography in practice; public key infrastructure (PKI) — certification authorities, the certificate chain, the certificate life cycle (issuance, renewal, revocation, CRL and OCSP); using electronic digital signatures in Uzbekistan (E-IMZO, key carriers, mobile signatures); the simplified stages of a TLS connection; storing passwords securely — salt and key stretching (bcrypt, scrypt, Argon2) and a teaching program showing them; encrypting disks, files and messages; key management rules; an assignment.
---

## Purpose of this independent study

In seminar S14 we saw the basics of cryptography — symmetric and asymmetric encryption, hashing, the electronic digital signature — on small examples. In this independent study we learn **how cryptography is applied in everyday work and systems**: how certificates form a chain of trust, how to work with a digital signature and how passwords are stored securely.

:::note
**After this independent study you will be able to:**

- explain PKI and the certificate chain;
- name the stages of the certificate life cycle;
- know the process of signing a document with a digital signature and the security rules;
- show why passwords are stored with salt and key stretching.
:::

## Public key infrastructure (PKI)

In S14 we saw that a certificate confirms the owner of a public key. But why do we trust the certificate itself? The **chain of trust** answers this:

```text
Root certification authority (Root CA)  — pre-installed in the operating system and browser
        │ signs
Intermediate certification authority (Intermediate CA)
        │ signs
User or website certificate (for example, bank.uz)
```

When the browser visits a site, it checks the chain from the bottom up: is the site's certificate signed by the intermediate authority, the intermediate by the root, and is the root on the trusted list? If any link does not match, a warning appears (S14).

## The certificate life cycle

| Stage | Meaning |
|---|---|
| **Request** | the user creates a key pair and requests a certificate with the public key |
| **Verification** | the certification authority verifies the person or organization (documents, identification) |
| **Issuance** | the authority signs the public key with the owner's details; a validity period is set |
| **Use** | signing, encryption, authentication |
| **Renewal** | a new certificate is obtained before the old one expires |
| **Revocation** | if the key is lost or stolen, or an employee leaves, the certificate is revoked before it expires |

**Checking revocation**: a **CRL** (certificate revocation list) or **OCSP** (querying a certificate's status in real time). That is why the certification authority must be informed immediately when a key is lost — otherwise signatures made in the owner's name remain valid.

## Digital signatures in Uzbekistan

- Digital signature keys and certificates are issued by authorized **certification authorities** (for individuals and for heads of organizations).
- The key **carrier**: a protected file on a computer, a USB token or an ID card; also a **mobile signature** (through a phone app).
- **E-IMZO** — a program for working with digital signatures in the browser: signing in the tax account (S15, MT31), public services, bank-client systems and electronic invoices.
- **Security rules**: never give the key or its password to anyone (the signature owner is responsible for signed documents); do not leave the USB token in the computer; watch the expiry date; if it is lost, have it revoked immediately.

## TLS: how a browser and a website build a secure channel

1. The browser connects to the site and lists the encryption methods it supports.
2. The site sends its **certificate**; the browser checks the chain of trust and the validity period.
3. Using a **key exchange** algorithm, the two sides derive a shared secret **session key** — it is never sent over the network.
4. All further data is encrypted **symmetrically** with this key (the hybrid scheme from S14).

The result: confidentiality (outsiders cannot read), integrity (nothing can be changed in transit) and the site's authenticity (the certificate).

## Storing passwords securely

A system should store not the password itself but its **hash** (S14). But a plain hash is not enough:

- **Identical passwords give identical hashes** — anyone looking at the database can see who uses the same password; the hashes of popular passwords are in precomputed tables.
- **Salt** — a random value for each user, added to the password before hashing and stored with the hash. As a result, identical passwords get different hashes and precomputed tables become useless.
- **Key stretching** — hashing is repeated thousands of times or a special slow function is used (**bcrypt, scrypt, Argon2**): one check is unnoticeable for the user, but trying a huge number of guesses becomes much more expensive.

## Worked example: storing a password with salt

A teaching program: a `reg login salt password` line registers a user (not the password but the hash of `salt + password` repeated 1,000 times is stored), and a `login login password` line checks a sign-in. For clarity FNV-1a from M12 is used — real systems use only special functions such as bcrypt, scrypt or Argon2, and the salt is created with a random generator.

```python
import sys

TAKROR = 1000     # key stretching (bcrypt, scrypt, Argon2 in real systems)

def fnv1a(matn):                                     # a teaching hash function
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return format(h, "08x")

def xeshlash(tuz, parol):
    x = tuz + parol
    for _ in range(TAKROR):
        x = fnv1a(x)
    return x

ombor = {}                                           # login -> (salt, hash); the password itself is not stored
for satr in sys.stdin.read().split("\n"):
    q = satr.split()
    if not q:
        continue
    if q[0] == "reg":
        _, login, tuz, parol = q
        ombor[login] = (tuz, xeshlash(tuz, parol))
        print(f"{login}: stored, salt {tuz}, hash {ombor[login][1]}")
    else:
        _, login, parol = q
        saqlangan = ombor[login]
        togri = xeshlash(saqlangan[0], parol) == saqlangan[1]
        print(f"{login}: {'access granted' if togri else 'wrong password'}")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final int TAKROR = 1000;     // key stretching (bcrypt, scrypt, Argon2 in real systems)

    static String fnv1a(String matn) {                               // a teaching hash function
        long h = 2166136261L;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = ((h ^ (b & 0xff)) * 16777619L) & 0xFFFFFFFFL;
        return String.format("%08x", h);
    }

    static String xeshlash(String tuz, String parol) {
        String x = tuz + parol;
        for (int i = 0; i < TAKROR; i++) x = fnv1a(x);
        return x;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, String[]> ombor = new HashMap<>();               // login -> (salt, hash); the password itself is not stored
        while (in.hasNextLine()) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].isEmpty()) continue;
            if (q[0].equals("reg")) {
                ombor.put(q[1], new String[]{q[2], xeshlash(q[2], q[3])});
                System.out.println(q[1] + ": stored, salt " + q[2] + ", hash " + ombor.get(q[1])[1]);
            } else {
                String[] saqlangan = ombor.get(q[1]);
                boolean togri = xeshlash(saqlangan[0], q[2]).equals(saqlangan[1]);
                System.out.println(q[1] + ": " + (togri ? "access granted" : "wrong password"));
            }
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <utility>
using namespace std;

const int TAKROR = 1000;     // key stretching (bcrypt, scrypt, Argon2 in real systems)

string fnv1a(const string& matn) {                                   // a teaching hash function
    uint64_t h = 2166136261ULL;
    for (unsigned char b : matn) h = ((h ^ b) * 16777619ULL) & 0xFFFFFFFFULL;
    char buf[16];
    snprintf(buf, sizeof buf, "%08llx", (unsigned long long) h);
    return buf;
}

string xeshlash(const string& tuz, const string& parol) {
    string x = tuz + parol;
    for (int i = 0; i < TAKROR; i++) x = fnv1a(x);
    return x;
}

int main() {
    map<string, pair<string, string>> ombor;                         // login -> (salt, hash); the password itself is not stored
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string buyruq, login, a, b;
        if (!(ss >> buyruq)) continue;
        if (buyruq == "reg") {
            ss >> login >> a >> b;
            ombor[login] = {a, xeshlash(a, b)};
            cout << login << ": stored, salt " << a << ", hash " << ombor[login].second << "\n";
        } else {
            ss >> login >> a;
            bool togri = xeshlash(ombor[login].first, a) == ombor[login].second;
            cout << login << ": " << (togri ? "access granted" : "wrong password") << "\n";
        }
    }
    return 0;
}
```

Input:

```text
reg ali 7f3a Qwerty2026
reg vali 91c4 Qwerty2026
login ali Qwerty2026
login vali qwerty2026
```

Output:

```text
ali: stored, salt 7f3a, hash c3fe1ad8
vali: stored, salt 91c4, hash 70db8b04
ali: access granted
vali: wrong password
```

Ali and Vali chose the same password, but because their salts differ, the hashes in the database are completely different — someone looking at the database cannot tell. Vali typed the password with a lowercase letter — a difference of one character gives a completely different hash, and the sign-in is rejected. The system never stores the password itself and cannot "remind you of your password" — it can only set a new one.

## Encrypting disks, files and messages

- **Full disk encryption** (BitLocker, FileVault, encryption on Android and iOS) — even if a laptop or phone is lost, the data cannot be read (the "laptop" risk in M14).
- **Encrypting archives with a password** — when sending files; send the password through a different channel (for example, the file by email, the password by SMS).
- **End-to-end encryption** — in messengers only the participants can open a message; even the service's server cannot.

## Key management rules

1. Keys are not stored openly in code, documents or emails (MT21).
2. A separate key for each purpose; regular rotation.
3. Backups of keys in a secure place — if a key is lost, the encrypted data is lost too.
4. A register of who holds which keys; when an employee leaves, their keys and certificates are revoked.

:::warning
**Common mistakes**

- **Giving your signature key and password to a colleague.** The owner is responsible for the signed document.
- **Not having the certificate revoked when a key is lost.**
- **Storing passwords in plain form or as an unsalted hash.**
- **Sending the encryption key in the same place as the encrypted file.**
- **Creating "your own cipher".** Only proven standards (S14).
:::

## Assignment

1. In the browser, open the certificate chain of a bank's website: write down the root, intermediate and site certificates and their validity periods.
2. Add a third user to the example program with the same salt and the same password as Ali. What hashes come out? This shows why the salt must be unique for each user.
3. Draw up a guideline (8–10 points) on the rules for using digital signatures in a company.
4. Check whether disk encryption is turned on on your own phone and computer.

## Summary

- The PKI chain of trust: root → intermediate → user certificate; certificates are issued, renewed and revoked (CRL, OCSP).
- Digital signatures in Uzbekistan: certification authorities, key carriers, mobile signatures, E-IMZO; the key is its owner's personal responsibility.
- TLS: certificate verification, key exchange, symmetric encryption.
- Passwords are hashed with salt and key stretching (bcrypt, scrypt, Argon2); disks, files and messages are encrypted; keys are managed by rules.

## Check yourself

:::check
Q: Why does a browser trust a site's certificate?
A: The certificate is checked through the chain of trust up to a root certification authority pre-installed in the browser.
Q: What should be done if a digital signature key is lost?
A: Inform the certification authority immediately and have the certificate revoked.
Q: Why is "salt" added to a password hash?
A: So that identical passwords give different hashes and precomputed hash tables become useless.
Q: How is the data itself encrypted in TLS?
A: With symmetric encryption using a session key derived through key exchange.
:::

:::link https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
OWASP: Password Storage Cheat Sheet
Practical recommendations on storing passwords securely.
:::
