---
title: Symmetric cryptographic algorithms. Stream and block ciphers, using A5/1 and AES as examples
summary: The idea of symmetric encryption; Shannon's principles — confusion and diffusion; stream ciphers — the one-time pad, keystream generators, LFSRs, A5/1 (GSM) and its weaknesses, ChaCha20; block ciphers — the Feistel network and DES, the structure of AES (SubBytes, ShiftRows, MixColumns, AddRoundKey), key lengths and rounds; modes of operation — ECB, CBC, CTR, GCM; padding and the IV.
---

## Why it matters

The data on your phone, Wi-Fi traffic, HTTPS pages, encrypted disks — all are protected by symmetric ciphers, because they are very fast: a modern processor runs AES at gigabytes per second. But "we used AES" is not yet security: the wrong mode (for example, ECB) or a repeated IV makes even a strong algorithm useless. In this lecture we look at the inner structure of symmetric ciphers and the rules for using them correctly.

:::note
**After this lesson you will be able to:**

- explain the difference between stream and block ciphers;
- explain how an LFSR and A5/1 produce a keystream, and the weaknesses of A5/1;
- know the round structure and key lengths of AES;
- compare the ECB, CBC, CTR and GCM modes;
- show why ECB mode reveals the structure of the data.
:::

## Shannon's principles

In 1949 Claude Shannon described two properties of a good cipher:

- **Confusion** — the relationship between the key and the ciphertext should be as complex as possible (substitutions, S-boxes).
- **Diffusion** — one bit of plaintext should affect many bits of ciphertext (permutations, mixing). The result is the **avalanche effect**: changing one bit changes about half of the ciphertext.

## Stream ciphers

A stream cipher produces from the key a long pseudo-random **keystream** and **XORs** it with the plaintext bit by bit: *C* = *M* ⊕ *K*ₛ, *M* = *C* ⊕ *K*ₛ (encryption and decryption are the same operation).

- **The one-time pad** — the keystream is truly random, as long as the text and used once: theoretically unbreakable (proved by Shannon), but key delivery is hard in practice.
- **An important rule**: never repeat a keystream. If two messages are encrypted with the same stream, *C*₁ ⊕ *C*₂ = *M*₁ ⊕ *M*₂ — a lot about the texts can be learned even without the key.

An **LFSR** (linear feedback shift register) is the simplest keystream generator: at each step the register shifts by one bit and the new bit is the XOR of chosen "tap" bits. A well-chosen *n*-bit LFSR has period 2ⁿ − 1. But an LFSR is **linear**: from 2*n* output bits its structure can be recovered (the Berlekamp–Massey algorithm), so on its own it is not secure.

**A5/1** is the stream cipher used in GSM mobile telephony: three LFSRs of lengths 19, 22 and 23 bits, a 64-bit key; the registers are clocked irregularly by a **majority rule**. In the 2000s it was broken in practice (in seconds with precomputed tables) — which is why 3G/4G moved to stronger algorithms such as KASUMI, SNOW 3G and AES. Today's modern stream cipher is **ChaCha20** (TLS, WireGuard, Android).

## Block ciphers

A block cipher splits data into fixed-length blocks and transforms each with the key; several **rounds** are repeated.

**DES** (1977) — a 64-bit block, a 56-bit key, a 16-round **Feistel network**. Because of its short key it was broken by brute force in 1998; the interim fix was 3DES (DES three times), which is now obsolete too.

**AES** (Rijndael, the US standard since 2001) — a **128-bit** block, a key of **128, 192 or 256** bits, and **10, 12, 14** rounds respectively. The block is treated as a 4×4 byte "state" matrix; each round has four steps:

| Step | Purpose | Shannon property |
|---|---|---|
| **SubBytes** | each byte is replaced using the S-box table (the only non-linear step) | confusion |
| **ShiftRows** | rows are shifted left by 0, 1, 2, 3 bytes | diffusion |
| **MixColumns** | each column is multiplied by a matrix in the field GF(2⁸) | diffusion |
| **AddRoundKey** | the state is XORed with the round key | binding to the key |

The last round has no MixColumns. The round keys are derived from the main key by the **key expansion** process. Modern processors have special instructions for AES (AES-NI).

## Modes of operation

A message is usually longer than one block — the **mode** sets how the blocks are linked.

| Mode | Idea | Feature |
|---|---|---|
| **ECB** | each block is encrypted separately | identical blocks → identical ciphertext; **do not use** |
| **CBC** | each block is XORed with the previous ciphertext block before encryption; the first one with the **IV** | needs a random IV, sequential, needs padding |
| **CTR** | an encrypted counter produces a keystream (turns a block cipher into a stream cipher) | parallel, no padding; the counter must never repeat |
| **GCM** | CTR + an authentication tag | confidentiality and integrity together (AEAD); the standard in TLS 1.3 |

**Padding**: if the last block is incomplete it is padded by the PKCS#7 method (if *k* bytes are missing, *k* bytes of value *k* are added). The **IV** (initialisation vector) is not secret, but it must be new and unpredictable for every message.

## Worked example 1: an LFSR and a stream cipher

A 16-bit LFSR (feedback: XOR of bits 0, 2, 3, 5, shifting right) produces a keystream from its initial state. Each output byte is the bits taken 8 times from the register's lowest bit. The message bytes are XORed with the keystream; doing it again restores the message.

```python
def kalit_oqimi(holat, n):
    natija = []
    for _ in range(n):
        bayt = 0
        for _ in range(8):
            yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1   # feedback
            bayt = (bayt << 1) | (holat & 1)
            holat = (holat >> 1) | (yangi << 15)
        natija.append(bayt)
    return natija


holat = int(input().strip(), 16)
xabar = input()
oqim = kalit_oqimi(holat, len(xabar))
shifr = [ord(c) ^ k for c, k in zip(xabar, oqim)]
ochilgan = "".join(chr(c ^ k) for c, k in zip(shifr, oqim))   # XOR again gives the original text
print("keystream: " + " ".join(format(k, "02x") for k in oqim))
print("ciphertext: " + " ".join(format(c, "02x") for c in shifr))
print("decrypted: " + ochilgan)
```
```java
import java.util.Scanner;

public class Main {
    static int[] kalitOqimi(int holat, int n) {
        int[] natija = new int[n];
        for (int i = 0; i < n; i++) {
            int bayt = 0;
            for (int j = 0; j < 8; j++) {
                int yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1;   // feedback
                bayt = (bayt << 1) | (holat & 1);
                holat = (holat >> 1) | (yangi << 15);
            }
            natija[i] = bayt;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int holat = Integer.parseInt(in.nextLine().trim(), 16);
        String xabar = in.nextLine();
        int[] oqim = kalitOqimi(holat, xabar.length());
        StringBuilder k = new StringBuilder(), s = new StringBuilder(), o = new StringBuilder();
        for (int i = 0; i < xabar.length(); i++) {
            int c = xabar.charAt(i) ^ oqim[i];
            k.append(i > 0 ? " " : "").append(String.format("%02x", oqim[i]));
            s.append(i > 0 ? " " : "").append(String.format("%02x", c));
            o.append((char) (c ^ oqim[i]));                        // XOR again gives the original text
        }
        System.out.println("keystream: " + k);
        System.out.println("ciphertext: " + s);
        System.out.println("decrypted: " + o);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

vector<int> kalitOqimi(unsigned holat, size_t n) {
    vector<int> natija;
    for (size_t i = 0; i < n; i++) {
        int bayt = 0;
        for (int j = 0; j < 8; j++) {
            unsigned yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1;   // feedback
            bayt = (bayt << 1) | (holat & 1);
            holat = (holat >> 1) | (yangi << 15);
        }
        natija.push_back(bayt);
    }
    return natija;
}

int main() {
    string satr, xabar;
    getline(cin, satr);
    getline(cin, xabar);
    unsigned holat = stoul(satr, nullptr, 16);
    vector<int> oqim = kalitOqimi(holat, xabar.size());
    string k, s, o;
    char buf[4];
    for (size_t i = 0; i < xabar.size(); i++) {
        int c = (unsigned char) xabar[i] ^ oqim[i];
        snprintf(buf, sizeof buf, "%02x", oqim[i]);
        k += (i ? " " : "") + string(buf);
        snprintf(buf, sizeof buf, "%02x", c);
        s += (i ? " " : "") + string(buf);
        o += (char) (c ^ oqim[i]);                                // XOR again gives the original text
    }
    cout << "keystream: " << k << endl;
    cout << "ciphertext: " << s << endl;
    cout << "decrypted: " << o << endl;
    return 0;
}
```

Input:

```text
ACE1
SALOM DUNYO
```

Output:

```text
keystream: 87 35 44 e2 ec 23 b9 c7 a8 11 4a
ciphertext: d4 74 08 ad a1 03 fd 92 e6 48 05
decrypted: SALOM DUNYO
```

The key is only a 16-bit initial state (65,535 variants) and the LFSR is linear: from a few known letters (for example "SALOM", Uzbek for "hello") an attacker finds the keystream and rebuilds the register. This example shows the **idea** of a stream cipher; in practice ChaCha20 or AES-CTR is used.

## Worked example 2: the difference between ECB and CBC

For learning purposes take an 8-bit "block cipher": *E*(*b*) = (*b* ⊕ *k*) rotated left by 3 bits. Apply it in ECB and CBC modes and compare the results for a repetitive text. Input: the key and the IV (hexadecimal), the plaintext.

```python
def shifr(b, k):
    x = b ^ k
    return ((x << 3) | (x >> 5)) & 0xFF         # rotate left by 3 bits


k = int(input().strip(), 16)
iv = int(input().strip(), 16)
matn = input()
ecb = [shifr(ord(c), k) for c in matn]          # each block separately
cbc = []
oldingi = iv
for c in matn:
    oldingi = shifr(ord(c) ^ oldingi, k)         # chained to the previous ciphertext
    cbc.append(oldingi)
print("ECB: " + " ".join(format(x, "02x") for x in ecb))
print("CBC: " + " ".join(format(x, "02x") for x in cbc))
```
```java
import java.util.Scanner;

public class Main {
    static int shifr(int b, int k) {
        int x = b ^ k;
        return ((x << 3) | (x >> 5)) & 0xFF;                      // rotate left by 3 bits
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = Integer.parseInt(in.nextLine().trim(), 16);
        int iv = Integer.parseInt(in.nextLine().trim(), 16);
        String matn = in.nextLine();
        StringBuilder ecb = new StringBuilder(), cbc = new StringBuilder();
        int oldingi = iv;
        for (int i = 0; i < matn.length(); i++) {
            int c = matn.charAt(i);
            ecb.append(i > 0 ? " " : "").append(String.format("%02x", shifr(c, k)));   // each block separately
            oldingi = shifr(c ^ oldingi, k);                       // chained to the previous ciphertext
            cbc.append(i > 0 ? " " : "").append(String.format("%02x", oldingi));
        }
        System.out.println("ECB: " + ecb);
        System.out.println("CBC: " + cbc);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int shifr(int b, int k) {
    int x = b ^ k;
    return ((x << 3) | (x >> 5)) & 0xFF;                          // rotate left by 3 bits
}

string hex2(int x) {
    char buf[4];
    snprintf(buf, sizeof buf, "%02x", x);
    return buf;
}

int main() {
    string satr, matn;
    getline(cin, satr);
    int k = stoi(satr, nullptr, 16);
    getline(cin, satr);
    int iv = stoi(satr, nullptr, 16);
    getline(cin, matn);
    string ecb, cbc;
    int oldingi = iv;
    for (size_t i = 0; i < matn.size(); i++) {
        int c = (unsigned char) matn[i];
        ecb += (i ? " " : "") + hex2(shifr(c, k));               // each block separately
        oldingi = shifr(c ^ oldingi, k);                          // chained to the previous ciphertext
        cbc += (i ? " " : "") + hex2(oldingi);
    }
    cout << "ECB: " << ecb << endl;
    cout << "CBC: " << cbc << endl;
    return 0;
}
```

Input:

```text
5C
37
AAAABBBB
```

Output:

```text
ECB: e8 e8 e8 e8 f0 f0 f0 f0
CBC: 51 62 fb 37 49 ba 25 d9
```

In ECB the four `A`s became four identical `e8`s — even without the key an attacker sees the structure of the text (the famous example is a picture of a penguin encrypted with ECB, whose outline remains visible). In CBC every block depends on the previous one and the repetitions are hidden. In real AES exactly the same happens at the level of 16-byte blocks.

:::warning
**Common mistakes**

- **Using ECB mode.** Identical blocks give identical ciphertext — the structure is revealed.
- **Repeating the IV or the CTR counter.** In stream and CTR modes this equals reusing the keystream — the texts are revealed.
- **Encrypting without checking integrity.** CBC and CTR do not detect that data was changed; an authenticated mode such as GCM or a separate MAC is needed.
- **Obsolete algorithms (DES, RC4, A5/1).** They are broken in practice; use AES-GCM or ChaCha20-Poly1305.
- **Writing the key into the program code.** Keys are kept separately, in a protected place (a key vault, an HSM).
:::

:::tip
In the online tool CyberChef (gchq.github.io/CyberChef), use the "AES Encrypt" recipe to encrypt a text made of identical 16-byte blocks in ECB and CBC modes and compare the results. Then change the IV in CBC and watch the whole ciphertext change.
:::

## Summary

- A good cipher provides confusion and diffusion; the avalanche effect — one changed bit changes half the ciphertext.
- A stream cipher: *C* = *M* ⊕ keystream; the stream must never repeat; LFSRs are linear, A5/1 is broken, today — ChaCha20.
- AES: a 128-bit block, a 128/192/256-bit key, 10/12/14 rounds; SubBytes, ShiftRows, MixColumns, AddRoundKey.
- Modes: ECB (do not use), CBC (a random IV), CTR (a never-repeating counter), GCM (confidentiality + integrity).

## Check yourself

:::check
Q: What is the main feature of symmetric encryption?
A: One shared secret key is used for both encryption and decryption.
Q: What is the AES block length, and which key lengths does it support?
A: The block is 128 bits; the key is 128, 192 or 256 bits.
Q: What kind of cipher is A5/1?
A: A stream cipher built on three LFSRs (GSM).
Q: Why is ECB mode not recommended?
A: Identical plaintext blocks give identical ciphertext blocks and the structure of the data is revealed.
Q: How is GCM mode better than CTR?
A: Besides confidentiality it also provides integrity through an authentication tag.
:::

:::link https://csrc.nist.gov/pubs/fips/197/final
FIPS 197: Advanced Encryption Standard (AES)
The official standard of the AES algorithm.
:::
