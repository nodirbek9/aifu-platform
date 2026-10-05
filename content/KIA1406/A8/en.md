---
title: Hash functions and creating a digital signature (with online tools)
summary: Practical work with hash functions and digital signatures — computing hashes in CyberChef and with operating system commands (certutil, Get-FileHash, sha256sum), checking the integrity of a downloaded file, computing an HMAC, creating and verifying a digital signature in GnuPG (Kleopatra) or OpenSSL, the E-IMZO system in Uzbekistan; storing passwords — salt and slow hashing; the Merkle tree; a programming task — checking integrity with SHA-256.
---

## Why does this topic matter?

In lecture M8 we covered hashes, MAC, digital signatures and PKI in theory. Now we apply them to everyday tasks: checking that a downloaded program is genuine, signing a document and verifying the signature, storing user passwords correctly. At the end of the session you will write an integrity checker based on SHA-256 yourself.

:::note
**After this session you will be able to:**

- compute the hash of a text or a file with online tools and OS commands;
- check the integrity of a downloaded file against a published hash;
- create and verify an HMAC and a digital signature;
- show why passwords must be stored with a salt and slow hashing;
- explain how a Merkle tree works.
:::

## Practical work

**Step 1. The avalanche effect (CyberChef).** At gchq.github.io/CyberChef choose the "SHA2" recipe (Size = 256). Compare the hashes of the texts `salom`, `Salom` and `salom ` (with a trailing space) — they are completely different. Then compare hash lengths with the "MD5" and "SHA3" recipes (32, 64 and 64 hex characters).

**Step 2. File integrity.** Download some official program (for example Python, VLC or 7-Zip) and find the SHA-256 value published on the page. Compute the hash:

```text
Windows (cmd):         certutil -hashfile file.exe SHA256
Windows (PowerShell):  Get-FileHash file.exe -Algorithm SHA256
Linux / macOS:         sha256sum file.exe   (macOS: shasum -a 256 file.exe)
```

If the values match, the file was not altered on the way. Note: if the site is compromised, the attacker can replace the hash value too — that is why developers also provide the hash list with a digital signature (a GPG signature).

**Step 3. HMAC.** In CyberChef compute the MAC of a message with the "HMAC" recipe (Key = `secret`, Hashing function = SHA256). Change one letter of the key — the MAC changes completely. Someone who does not know the key cannot produce a correct MAC.

**Step 4. Creating and verifying a digital signature.** Choose one of two ways:

- **GnuPG / Kleopatra** (Gpg4win, free): File → New Key Pair → name and email; select a document and "Sign" → a `.sig` file is created; check it with "Verify". Then add one character to the document and see the verification fail.
- **OpenSSL** (as in A7): `openssl dgst -sha256 -sign private.pem -out signature.bin document.txt` and `-verify public.pem`.

**Step 5. Digital signatures in Uzbekistan (E-IMZO).** For public services and the tax system, digital signature keys are issued by authorized centers (for example, e-imzo.uz): the key is stored as a file (`.pfx`) on the computer or on the ID card chip and is used in the browser through the E-IMZO module. The owner's identity is verified and a certificate is issued; when a document is signed, its hash is signed with the private key. **Never give your digital signature key or its password to anyone** — legally it equals your handwritten signature.

**Step 6. Program.** Complete the task **Checking integrity with SHA-256** on the platform: compute the SHA-256 of a message and compare it with the expected value (`Yaxlit` — intact, or `Buzilgan` — corrupted). Use each language's standard library (Python — `hashlib`, Java — `MessageDigest`); the C++ standard library has no SHA-256 — you write it yourself following the FIPS 180-4 standard.

## Worked example 1: salt and slow hashing

Each line holds a login, a password and a random per-user salt. Compare three storage methods: a plain hash *H*(password), a salted hash *H*(salt ‖ password) and a slow hash — re-hashing the salted hash 100,000 times. For teaching purposes *H* is the 32-bit FNV-1a (as in M8); real systems use bcrypt, scrypt, Argon2 or PBKDF2.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    login, parol, tuz = satr.split()
    oddiy = fnv1a(parol)
    tuzli = fnv1a(tuz + parol)                 # the salt is added to the password
    h = tuzli
    for _ in range(100000):                    # deliberate slowdown (key stretching)
        h = fnv1a(format(h, "08x") + tuz)
    print(f"{login}: plain={oddiy:08x} salted={tuzli:08x} slow={h:08x}")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            String login = q[0], parol = q[1], tuz = q[2];
            int oddiy = fnv1a(parol);
            int tuzli = fnv1a(tuz + parol);                  // the salt is added to the password
            int h = tuzli;
            for (int i = 0; i < 100000; i++) {               // deliberate slowdown (key stretching)
                h = fnv1a(String.format("%08x", h) + tuz);
            }
            System.out.println(String.format("%s: plain=%08x salted=%08x slow=%08x", login, oddiy, tuzli, h));
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

int main() {
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string login, parol, tuz;
        if (!(ss >> login >> parol >> tuz)) continue;
        uint32_t oddiy = fnv1a(parol);
        uint32_t tuzli = fnv1a(tuz + parol);                 // the salt is added to the password
        uint32_t h = tuzli;
        char buf[16];
        for (int i = 0; i < 100000; i++) {                   // deliberate slowdown (key stretching)
            snprintf(buf, sizeof buf, "%08x", h);
            h = fnv1a(string(buf) + tuz);
        }
        printf("%s: plain=%08x salted=%08x slow=%08x\n", login.c_str(), oddiy, tuzli, h);
    }
    return 0;
}
```

Input:

```text
ali qwerty123 x7Kp
vali qwerty123 Qm2z
sardor Tosh2024! b9Lr
```

Output:

```text
ali: plain=cffd7677 salted=bcb1b0b1 slow=7e749555
vali: plain=cffd7677 salted=26151749 slow=9151da10
sardor: plain=9c1e624e salted=da7e098f slow=feab59f8
```

With the plain hash ali and vali have the same value — if the database is stolen, the attacker immediately sees that their passwords are the same and finds them all at once with a ready table of popular passwords (a rainbow table). With a salt, identical passwords give different hashes and a ready table is useless: guessing has to start separately for each user. The slow hash makes every attempt 100,000 times more expensive — the user does not notice (one login), but the attacker does, over billions of attempts.

## Worked example 2: the Merkle tree

A **Merkle tree** represents a lot of data with one hash — the **root**: the leaves are the hashes of each record, each parent node is the hash of its two children's hashes concatenated; on a level with an odd count the last element is duplicated. It is used in blockchain blocks, in Git and in Certificate Transparency logs. Each line is one record; print every level and the root (*H* is FNV-1a).

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


daraja = [format(fnv1a(s), "08x") for s in sys.stdin.read().split("\n") if s.strip()]
qadam = 0
print(f"L{qadam}: " + " ".join(daraja))
while len(daraja) > 1:
    if len(daraja) % 2 == 1:
        daraja.append(daraja[-1])                # if odd, the last one is duplicated
    daraja = [format(fnv1a(daraja[i] + daraja[i + 1]), "08x") for i in range(0, len(daraja), 2)]
    qadam += 1
    print(f"L{qadam}: " + " ".join(daraja))
print("root = " + daraja[0])
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        List<String> daraja = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) daraja.add(String.format("%08x", fnv1a(s)));
        }
        int qadam = 0;
        System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        while (daraja.size() > 1) {
            if (daraja.size() % 2 == 1) daraja.add(daraja.get(daraja.size() - 1));   // if odd, the last one is duplicated
            List<String> keyingi = new ArrayList<>();
            for (int i = 0; i < daraja.size(); i += 2) {
                keyingi.add(String.format("%08x", fnv1a(daraja.get(i) + daraja.get(i + 1))));
            }
            daraja = keyingi;
            qadam++;
            System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        }
        System.out.println("root = " + daraja.get(0));
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

string hex8(uint32_t h) {
    char buf[16];
    snprintf(buf, sizeof buf, "%08x", h);
    return buf;
}

void chiqar(int qadam, const vector<string>& daraja) {
    cout << "L" << qadam << ":";
    for (const string& x : daraja) cout << " " << x;
    cout << endl;
}

int main() {
    vector<string> daraja;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) daraja.push_back(hex8(fnv1a(s)));
    }
    int qadam = 0;
    chiqar(qadam, daraja);
    while (daraja.size() > 1) {
        if (daraja.size() % 2 == 1) daraja.push_back(daraja.back());   // if odd, the last one is duplicated
        vector<string> keyingi;
        for (size_t i = 0; i < daraja.size(); i += 2) {
            keyingi.push_back(hex8(fnv1a(daraja[i] + daraja[i + 1])));
        }
        daraja = keyingi;
        qadam++;
        chiqar(qadam, daraja);
    }
    cout << "root = " << daraja[0] << endl;
    return 0;
}
```

Input:

```text
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
Ali->Olim:3
Olim->Vali:4
```

Output:

```text
L0: 47af2243 bfba4043 c18587fe fd725d1a c1e3e7a7
L1: 338b6726 5b128ae6 b54962fd
L2: 2514cf2e 22ad2995
L3: 43d5aefb
root = 43d5aefb
```

If we change the fourth record to `Ali->Olim:8`:

```text
L0: 47af2243 bfba4043 c18587fe f4724eef c1e3e7a7
L1: 338b6726 1f581bad b54962fd
L2: e91b25a2 22ad2995
L3: cebf2f88
root = cebf2f88
```

Only the path from the changed leaf to the root (4 hashes) changed, and the root is different too. That is why storing only the root in a block header is enough. Moreover, to prove that one record is in the block you need not the whole block but only the "neighbour" hashes along the path — log₂ *n* hashes for *n* records.

:::warning
**Common mistakes**

- **Checking a hash taken from the same site only.** If the site is compromised, the hash is fake too; check a signed hash list or another source.
- **Comparing a hash value by eye, "the start and the end".** Compare it in full or check it automatically with a command.
- **One salt for all users.** The salt must be separate and random for each user.
- **Ignoring text encoding.** With UTF-8 versus another encoding, or with `\n` versus `\r\n` at the end of a line, the hash comes out different.
- **Giving your digital signature key file and password to others.** This is the same as handing over your handwritten signature.
:::

:::tip
If a download page has a `SHA256SUMS` file and its `.sig` signature, first verify the signature with the developer's public key (`gpg --verify SHA256SUMS.sig SHA256SUMS`), then check the file against the list (`sha256sum -c SHA256SUMS`) — this two-step check is standard practice for Linux distributions.
:::

## Summary

- A hash can be computed with CyberChef, `certutil`, `Get-FileHash`, `sha256sum`; changing one character changes the hash completely.
- File integrity means comparing with a published hash; to be trustworthy, the hash list must be signed.
- HMAC is a keyed hash; a digital signature is signed with the private key and verified with the public key (GnuPG, OpenSSL, E-IMZO).
- Passwords: a random salt per user and a deliberately slow algorithm.
- A Merkle tree represents many records with one root hash; if one record changes, the root changes.

## Check yourself

:::check
Q: What should you do if the hash of a downloaded file does not match the value on the page?
A: Do not use the file: it is corrupted or altered; download it again from the official source and check again.
Q: Why do two users with the same password have different salted hashes?
A: Each gets a separate random salt, so the strings being hashed are different.
Q: How does slow hashing hinder the attacker rather than the user?
A: The user logs in once and does not notice the delay; for the attacker every one of billions of attempts becomes expensive.
Q: In a Merkle tree, which hashes change if one record changes?
A: The hashes on the path from that leaf to the root, including the root.
:::

:::link https://csrc.nist.gov/pubs/fips/180-4/upd1/final
FIPS 180-4: Secure Hash Standard (SHS)
The official standard of the SHA-1 and SHA-2 family (SHA-256, SHA-512) algorithms.
:::
