---
title: Cryptographic protocols and applications. SSL/TLS, IPSec, SSH, blockchain, e-government and electronic payments
summary: The concept of a cryptographic protocol; SSL/TLS — versions, the TLS 1.3 handshake, cipher suites, perfect forward secrecy (PFS); IPSec — AH, ESP, transport and tunnel modes, IKE; SSH — key exchange, key-based login; blockchain — a chain of blocks, proof of work, smart contracts; cryptography in e-government and electronic payments — digital signatures, ID cards, EMV chips, 3-D Secure, tokenization.
---

## Why does this topic matter?

In the previous lessons we met the separate "bricks" — symmetric ciphers, RSA, Diffie–Hellman, hashes and digital signatures. In practice they do not work one by one but together inside **protocols**: the padlock in the browser (TLS), the tunnel linking an office with a branch (IPSec), server administration (SSH), a bank card payment (EMV), the digital signature on the my.gov.uz portal. Understanding a protocol means seeing where a weakness can appear.

:::note
**After this lesson you will be able to:**

- explain the steps of the TLS 1.3 handshake and the parts of a cipher suite;
- say at which layer and how IPSec and SSH provide protection;
- show how blockchain blocks are linked through hashes and how proof of work works;
- know where cryptography is used in electronic payments and e-government.
:::

## Cryptographic protocol

A **cryptographic protocol** is a sequence of messages that two or more parties exchange to reach a security goal (a confidential channel, authentication, key agreement). Even with strong algorithms, a protocol flaw can break the whole system: for example **POODLE** in SSL 3.0, **Heartbleed** in TLS (a bug in the OpenSSL software) or attacks that force an older version (**downgrade**).

## SSL/TLS

**TLS** (Transport Layer Security) underlies HTTPS, secure mail (SMTPS, IMAPS) and many applications. It runs on top of the transport layer (TCP).

| Version | Status |
|---|---|
| SSL 2.0, SSL 3.0 | prohibited (DROWN, POODLE) |
| TLS 1.0, TLS 1.1 | officially deprecated since 2021 (RFC 8996) |
| TLS 1.2 | allowed, secure when configured correctly |
| TLS 1.3 (2018) | recommended: faster, strong algorithms only |

**The TLS 1.3 handshake**, simplified:

1. **ClientHello**: the cipher suites the client supports, a random number and its ephemeral (EC)DH public key.
2. **ServerHello**: the server picks a cipher suite and sends its own (EC)DH public key. Both sides now compute a shared secret — the following messages are already encrypted.
3. **Certificate + CertificateVerify**: the server sends its X.509 certificate and its signature over the handshake — the client checks the certificate chain.
4. **Finished**: both sides exchange a MAC over the hash of the whole conversation — confirming that nobody altered the messages on the way.
5. Data is sent with an **AEAD** cipher (AES-GCM or ChaCha20-Poly1305).

A **cipher suite** is the name of the algorithms used. In TLS 1.2, for example, `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`: key exchange ECDHE, server authentication with an RSA certificate, cipher AES-256-GCM, hash SHA-384. In TLS 1.3 the name is shorter (`TLS_AES_128_GCM_SHA256`) — the key exchange is always ephemeral (EC)DHE.

**Perfect Forward Secrecy** (PFS): the session key is derived from ephemeral DH keys that are deleted afterwards. Even if the server's private key is stolen later, previously recorded traffic cannot be decrypted. When the key is transported with RSA (`TLS_RSA_WITH_...`) there is no PFS — that is why TLS 1.3 removed this method entirely.

## IPSec

**IPSec** works at the network layer (IP) and protects IP packets — applications do not need to change. Its main use is VPN (office–branch, remote employee).

| Component | Role |
|---|---|
| **AH** (Authentication Header) | integrity and source authentication, no encryption |
| **ESP** (Encapsulating Security Payload) | encryption + integrity (in practice almost always ESP) |
| **IKE** (Internet Key Exchange, IKEv2) | authenticates the parties and agrees on keys (based on DH) |
| **SA** (Security Association) | the agreed set of algorithms, keys and lifetimes |

**Transport mode** protects only the packet payload (between two hosts); **tunnel mode** encrypts the whole packet and wraps it in a new IP header (between two gateways — the classic VPN).

## SSH

**SSH** (Secure Shell, port 22) is a protocol for secure remote server administration, file transfer (SCP, SFTP) and tunneling; it replaced the old, unencrypted Telnet and FTP.

- **Key exchange**: (EC)DH or Curve25519; the server signs with its **host key**. On the first connection the client shows the host key fingerprint and records it in the `known_hosts` file; if the key changes later — a warning (it may be a sign of a man-in-the-middle).
- **User authentication**: a password or — more securely — a **key pair** (`ssh-keygen -t ed25519`): the public key goes into the server's `authorized_keys` file, the private key stays on the client and is protected with a passphrase.
- Good practice: disable password login and direct `root` login, use keys, limit login attempts.

## Blockchain

A **blockchain** is a chain of blocks in which each block contains the hash of the previous one. If data in one block changes, its hash changes and the link in every following block breaks. In a distributed network thousands of nodes keep a copy of the chain and agree through **consensus** on which block is added:

- **Proof of Work** (Bitcoin): to add a block one has to find a **nonce** such that the hash starts with a given number of zero bits — this takes a lot of computation, while checking takes a single hash. To rewrite the chain an attacker needs more than half of the network's computing power (the "51% attack").
- **Proof of Stake** (Ethereum since 2022): block producers are chosen according to the funds they stake; energy use is far lower.

Transactions are signed with digital signatures (ECDSA, Ed25519); an address is the hash of a public key. **Smart contracts** are programs stored on the blockchain and executed automatically. Limitations: low speed, mistakes cannot be reversed, and if the private key is lost, the funds are lost too.

## E-government and electronic payments

| Area | Where cryptography is used |
|---|---|
| **E-government** (my.gov.uz, tax account) | TLS, login with a digital signature key or the ID card chip, signing documents with a digital signature, the OneID single identification system |
| **Bank cards** (EMV chip, Uzcard, Humo, Visa) | the chip generates a one-time cryptogram for every transaction — unlike the magnetic stripe, copying it is useless |
| **Online payments** | TLS, **3-D Secure** (extra confirmation by SMS or app), the PCI DSS standard |
| **Mobile payments** (Apple Pay, Google Pay) | **tokenization**: a device-specific token is used instead of the real card number |
| **Mobile apps** (Click, Payme) | TLS, certificate pinning, SMS or biometric confirmation |

## Worked example 1: a teaching blockchain

The first line holds the difficulty *d* (bits), the following lines hold transactions. For each block find the smallest nonce such that the top *d* bits of the 32-bit FNV-1a hash of the string "number|previous_hash|transaction|nonce" are zero. The previous hash of the first block is 00000000. (A real blockchain uses SHA-256 and a much higher difficulty.)

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
d = int(satrlar[0])
oldingi = "00000000"
for i, tranzaksiya in enumerate(satrlar[1:], start=1):
    nonce = 0
    while True:
        h = fnv1a(f"{i}|{oldingi}|{tranzaksiya}|{nonce}")
        if h >> (32 - d) == 0:                  # top d bits are zero — proof of work found
            break
        nonce += 1
    xesh = format(h, "08x")
    print(f"#{i} prev={oldingi} nonce={nonce} hash={xesh} {tranzaksiya}")
    oldingi = xesh                                # the next block links to this hash
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
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) satrlar.add(s);
        }
        int d = Integer.parseInt(satrlar.get(0).trim());
        String oldingi = "00000000";
        for (int i = 1; i < satrlar.size(); i++) {
            String tranzaksiya = satrlar.get(i);
            int nonce = 0;
            int h;
            while (true) {
                h = fnv1a(i + "|" + oldingi + "|" + tranzaksiya + "|" + nonce);
                if ((h >>> (32 - d)) == 0) break;      // top d bits are zero — proof of work found
                nonce++;
            }
            String xesh = String.format("%08x", h);
            System.out.println("#" + i + " prev=" + oldingi + " nonce=" + nonce + " hash=" + xesh + " " + tranzaksiya);
            oldingi = xesh;                              // the next block links to this hash
        }
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

int main() {
    vector<string> satrlar;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) satrlar.push_back(s);
    }
    int d = stoi(satrlar[0]);
    string oldingi = "00000000";
    for (size_t i = 1; i < satrlar.size(); i++) {
        const string& tranzaksiya = satrlar[i];
        long long nonce = 0;
        uint32_t h;
        while (true) {
            h = fnv1a(to_string(i) + "|" + oldingi + "|" + tranzaksiya + "|" + to_string(nonce));
            if ((h >> (32 - d)) == 0) break;            // top d bits are zero — proof of work found
            nonce++;
        }
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", h);
        string xesh = buf;
        cout << "#" << i << " prev=" << oldingi << " nonce=" << nonce << " hash=" << xesh << " " << tranzaksiya << endl;
        oldingi = xesh;                                  // the next block links to this hash
    }
    return 0;
}
```

Input:

```text
16
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
```

Output:

```text
#1 prev=00000000 nonce=64572 hash=00001674 Ali->Vali:5
#2 prev=00001674 nonce=2317 hash=000048c2 Vali->Sardor:2
#3 prev=000048c2 nonce=143961 hash=0000d51e Sardor->Ali:1
```

Each block hash starts with 16 zero bits (4 hex zeros) — this takes about 2¹⁶ ≈ 65 thousand attempts on average, while checking takes a single hash. Now suppose an attacker changed the first transaction to `Ali->Vali:9` — the same program produces a different chain:

```text
#1 prev=00000000 nonce=31445 hash=00008283 Ali->Vali:9
#2 prev=00008283 nonce=12125 hash=00003031 Vali->Sardor:2
#3 prev=00003031 nonce=17797 hash=00005e59 Sardor->Ali:1
```

Changing one block requires recomputing the hashes and nonces of **all** following blocks — and the other nodes in the network keep the old chain and reject the forged one.

## Worked example 2: analyzing TLS cipher suites

Each word is a cipher suite name. Split it into parts: key exchange (kx), authentication (auth), cipher and hash; determine whether there is PFS and give a verdict: if the name contains NULL, EXPORT, RC4, DES (3DES as well) or MD5 — `weak`; if there is no PFS or the CBC mode is used — `legacy`; otherwise — `strong`. A name without `_WITH_` is a TLS 1.3 suite: key exchange (EC)DHE, authentication by certificate.

```python
import sys

ZAIF = ["NULL", "EXPORT", "RC4", "DES", "MD5"]   # "3DES" also contains "DES"

for nom in sys.stdin.read().split():
    if "_WITH_" in nom:                          # TLS 1.2 and earlier: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
        chap, shifr = nom[4:].split("_WITH_")
        qismlar = chap.split("_")
        almashinuv = qismlar[0]
        autent = qismlar[1] if len(qismlar) > 1 else qismlar[0]
    else:                                        # TLS 1.3: the key exchange is not in the name
        shifr = nom[4:]
        almashinuv, autent = "(EC)DHE", "cert"
    shifr, xesh = shifr.rsplit("_", 1)
    pfs = "DHE" in almashinuv                    # ephemeral DH — forward secrecy
    if any(z in nom for z in ZAIF):
        baho = "weak"
    elif not pfs or "CBC" in shifr:
        baho = "legacy"
    else:
        baho = "strong"
    print(f"{nom}: kx={almashinuv} auth={autent} cipher={shifr} hash={xesh} pfs={'+' if pfs else '-'} -> {baho}")
```
```java
import java.util.Scanner;

public class Main {
    static final String[] ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" also contains "DES"

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String shifr, almashinuv, autent;
            int w = nom.indexOf("_WITH_");
            if (w >= 0) {                                // TLS 1.2 and earlier: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
                String[] qismlar = nom.substring(4, w).split("_");
                shifr = nom.substring(w + 6);
                almashinuv = qismlar[0];
                autent = qismlar.length > 1 ? qismlar[1] : qismlar[0];
            } else {                                     // TLS 1.3: the key exchange is not in the name
                shifr = nom.substring(4);
                almashinuv = "(EC)DHE";
                autent = "cert";
            }
            int p = shifr.lastIndexOf('_');
            String xesh = shifr.substring(p + 1);
            shifr = shifr.substring(0, p);
            boolean pfs = almashinuv.contains("DHE");    // ephemeral DH — forward secrecy
            boolean zaif = false;
            for (String z : ZAIF) {
                if (nom.contains(z)) zaif = true;
            }
            String baho;
            if (zaif) baho = "weak";
            else if (!pfs || shifr.contains("CBC")) baho = "legacy";
            else baho = "strong";
            System.out.println(nom + ": kx=" + almashinuv + " auth=" + autent + " cipher=" + shifr
                    + " hash=" + xesh + " pfs=" + (pfs ? "+" : "-") + " -> " + baho);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const vector<string> ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" also contains "DES"
    string nom;
    while (cin >> nom) {
        string shifr, almashinuv, autent;
        size_t w = nom.find("_WITH_");
        if (w != string::npos) {                         // TLS 1.2 and earlier: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
            string chap = nom.substr(4, w - 4);
            shifr = nom.substr(w + 6);
            size_t t = chap.find('_');
            almashinuv = chap.substr(0, t);
            autent = t == string::npos ? almashinuv : chap.substr(t + 1, chap.find('_', t + 1) - t - 1);
        } else {                                         // TLS 1.3: the key exchange is not in the name
            shifr = nom.substr(4);
            almashinuv = "(EC)DHE";
            autent = "cert";
        }
        size_t p = shifr.rfind('_');
        string xesh = shifr.substr(p + 1);
        shifr = shifr.substr(0, p);
        bool pfs = almashinuv.find("DHE") != string::npos;   // ephemeral DH — forward secrecy
        bool zaif = false;
        for (const string& z : ZAIF) {
            if (nom.find(z) != string::npos) zaif = true;
        }
        string baho;
        if (zaif) baho = "weak";
        else if (!pfs || shifr.find("CBC") != string::npos) baho = "legacy";
        else baho = "strong";
        cout << nom << ": kx=" << almashinuv << " auth=" << autent << " cipher=" << shifr
             << " hash=" << xesh << " pfs=" << (pfs ? "+" : "-") << " -> " << baho << endl;
    }
    return 0;
}
```

Input:

```text
TLS_AES_128_GCM_SHA256
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256
TLS_RSA_WITH_AES_128_CBC_SHA
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA
TLS_RSA_WITH_RC4_128_MD5
```

Output:

```text
TLS_AES_128_GCM_SHA256: kx=(EC)DHE auth=cert cipher=AES_128_GCM hash=SHA256 pfs=+ -> strong
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384: kx=ECDHE auth=RSA cipher=AES_256_GCM hash=SHA384 pfs=+ -> strong
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256: kx=ECDHE auth=ECDSA cipher=CHACHA20_POLY1305 hash=SHA256 pfs=+ -> strong
TLS_RSA_WITH_AES_128_CBC_SHA: kx=RSA auth=RSA cipher=AES_128_CBC hash=SHA pfs=- -> legacy
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA: kx=DHE auth=RSA cipher=3DES_EDE_CBC hash=SHA pfs=+ -> weak
TLS_RSA_WITH_RC4_128_MD5: kx=RSA auth=RSA cipher=RC4_128 hash=MD5 pfs=- -> weak
```

A server configuration should keep only "strong" suites. `TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA` has PFS, but the 64-bit block of 3DES is vulnerable to the **Sweet32** attack — one strong part does not make the whole suite secure. To check a real server, tools such as SSL Labs (ssllabs.com/ssltest) or `nmap --script ssl-enum-ciphers` are used.

:::warning
**Common mistakes**

- **"It has HTTPS, so the site is trustworthy".** TLS only protects the channel; a phishing site can also get a free certificate. Check the domain name.
- **Leaving old versions enabled "for compatibility".** SSL 3.0, TLS 1.0/1.1 and weak suites open the way to downgrade attacks.
- **Ignoring an SSH host key warning.** If the key changed unexpectedly, find out why first.
- **Believing a blockchain is "unbreakable".** The chain is intact, but large sums have been lost through smart contract bugs, exchange breaches and stolen private keys.
- **Breaking the rule of never telling anyone your card details and SMS code.** A 3-D Secure code is your own confirmation; a code told to a "bank employee" lets the attacker make a payment.
:::

:::tip
In a browser, F12 → the Security tab shows the TLS version and cipher suite of the current site. In a terminal, `openssl s_client -connect example.com:443 -tls1_3` prints the certificate chain and the negotiated suite.
:::

## Summary

- A cryptographic protocol combines algorithms into a sequence of messages; a protocol flaw makes even a strong algorithm useless.
- TLS 1.3: (EC)DHE key agreement, certificate authentication, AEAD cipher, mandatory PFS; SSL and TLS 1.0/1.1 are deprecated.
- IPSec works at the IP layer (AH, ESP, IKE; transport and tunnel modes); SSH — host key, key-pair login.
- Blockchain is a chain of blocks linked by hashes plus consensus (PoW, PoS); if one block changes, the following ones break too.
- E-government and payments: digital signatures, ID cards, the EMV cryptogram, 3-D Secure, tokenization.

## Check yourself

:::check
Q: What does Perfect Forward Secrecy (PFS) guarantee?
A: Even if the server's private key is stolen later, previously recorded sessions cannot be decrypted — the session keys are derived from ephemeral DH keys.
Q: How does IPSec tunnel mode differ from transport mode?
A: Tunnel mode encrypts the whole IP packet and wraps it in a new header (VPN between gateways); transport mode protects only the payload.
Q: What is the advantage of using a key pair instead of a password in SSH?
A: The private key is never sent over the network and cannot be guessed; password-guessing attacks become useless.
Q: Why is it hard to change an old block in a blockchain?
A: The block's hash changes and proof of work has to be redone for all following blocks, while the other nodes keep the original chain.
Q: What does tokenization protect in mobile payments?
A: The real card number — the point of sale only receives a device-specific token, which is useless elsewhere even if stolen.
:::

:::link https://www.rfc-editor.org/rfc/rfc8446
RFC 8446: The Transport Layer Security (TLS) Protocol Version 1.3
The official specification of the TLS 1.3 protocol.
:::
