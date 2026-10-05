---
title: Introduction to cryptography. Basic concepts, cryptosystems, Kerckhoffs's principle, historical methods
summary: Cryptology, cryptography and cryptanalysis; plaintext, ciphertext, key, encryption and decryption; the parts of a cryptosystem; symmetric and asymmetric systems; Kerckhoffs's principle; attack models; key space and brute force; historical ciphers — Atbash, the scytale, Caesar, the Polybius square, Vigenère, Enigma; substitution and transposition; frequency analysis; the services of cryptography.
---

## Why it matters

Every time you open a banking app, write in a messenger or see the "padlock" on a website, cryptography is at work. It makes data unreadable to outsiders, impossible to change unnoticed and impossible to forge. To understand modern algorithms you need the ideas behind them — the key, substitution, transposition, and why a "secret algorithm" cannot be trusted. Historical ciphers show these ideas in simple form, and the way they were broken explains today's requirements.

:::note
**After this lesson you will be able to:**

- explain the basic terms of cryptography and the parts of a cryptosystem;
- tell symmetric and asymmetric systems apart;
- explain and justify Kerckhoffs's principle;
- classify historical ciphers as substitution or transposition;
- calculate how frequency analysis and the size of the key space affect a cipher's strength.
:::

## Basic concepts

| Term | Meaning |
|---|---|
| **Cryptology** | the science of ciphers: cryptography + cryptanalysis |
| **Cryptography** | creating encryption methods to protect data |
| **Cryptanalysis** | methods of breaking a cipher without knowing the key |
| **Plaintext** | the original readable data *M* |
| **Ciphertext** | the encrypted data *C* |
| **Key** | the secret parameter *K* that controls encryption and decryption |
| **Encryption / decryption** | *C* = *E*(*K*, *M*), *M* = *D*(*K*, *C*) |

A **cryptosystem** is a system made of the sets of plaintexts, ciphertexts and keys and the encryption and decryption algorithms.

| Kind | Key | Advantage | Problem | Examples |
|---|---|---|---|---|
| **Symmetric** | one shared key for encryption and decryption | very fast | delivering the key securely | AES, ChaCha20, (the old) DES |
| **Asymmetric** (public key) | encrypt with the public key, decrypt with the private key | solves key exchange, digital signatures | slow | RSA, ECC, Diffie–Hellman |

In practice the two are used together (a **hybrid** system): the asymmetric method agrees a symmetric key, and the data is encrypted symmetrically (this is how TLS works).

## The services of cryptography

- **Confidentiality** — encryption.
- **Integrity** — hashes, MACs.
- **Authentication** — confirming that the parties are genuine.
- **Non-repudiation** — digital signatures.

## Kerckhoffs's principle

In 1883 Auguste Kerckhoffs stated: **a system's security must depend only on the secrecy of the key, not on the secrecy of the algorithm.** Even if the enemy knows the algorithm, they must not be able to break the cipher without the key. The reasons: an algorithm cannot be kept secret for long (programs are reverse-engineered, staff leave); an open algorithm is checked by thousands of experts and weaknesses are found earlier; a disclosed key is easy to change, an algorithm is not. Claude Shannon put it as "the enemy knows the system". That is why all modern algorithms such as AES and RSA are published openly.

## Attack models

| Model | What the attacker has |
|---|---|
| Ciphertext only | some ciphertexts |
| Known plaintext | plaintext–ciphertext pairs |
| Chosen plaintext | can have any text encrypted |
| Chosen ciphertext | can have any ciphertext decrypted |

A good cipher must withstand even the strongest model. The universal attack on any cipher is **brute force**: trying every key. If the key has *b* bits, there are 2^*b* keys and on average 2^(*b*−1) tries are needed. That is why key length is the main measure of strength.

## Historical ciphers

In **substitution**, each symbol is replaced by another; in **transposition**, the symbols stay the same and only their order changes.

| Cipher | Era | Kind | Idea | Weakness |
|---|---|---|---|---|
| **Scytale** | Sparta, 5th century BC | transposition | a strip is wound round a rod and written along it; the key is the rod's diameter | few row counts — quickly tried |
| **Atbash** | ancient Hebrew | substitution | A↔Z, B↔Y, … (the alphabet reversed) | no key |
| **Caesar** | Rome, 1st century BC | substitution | each letter is shifted by *k* places | only 25 keys |
| **Polybius square** | Greece | substitution | a letter becomes its row and column numbers in a 5×5 table | frequency analysis |
| **Vigenère** | 16th century | polyalphabetic substitution | each letter is shifted by the next letter of a key word | once the key length is found, it splits into Caesars (Kasiski, 1863) |
| **Enigma** | 20th century, Germany | electromechanical polyalphabetic | the rotors turn with every key press | broken by Polish and British cryptanalysts (Rejewski, Turing) |

Implementing the Caesar and Vigenère ciphers in a program is the task of practical session A5.

**Frequency analysis** (9th century, al-Kindi): in any language letters occur with different frequencies (in English E, T, A; in Uzbek A, I). In a simple substitution cipher a letter changes its "clothes" but keeps its frequency — the most common symbol in the ciphertext is very likely E or A. That is why monoalphabetic substitution is broken quickly however many keys it has (26! ≈ 4·10²⁶): a large key space is **not enough**, a cipher must also hide the statistics.

## Worked example 1: letter frequency

Count the Latin letters (case-insensitive) in a one-line text and print the five most frequent letters with their count and percentage (alphabetical order on a tie).

```python
matn = input()
soni = {}
for c in matn.upper():
    if "A" <= c <= "Z":
        soni[c] = soni.get(c, 0) + 1
jami = sum(soni.values())
print("letters: " + str(jami))
for harf, k in sorted(soni.items(), key=lambda x: (-x[1], x[0]))[:5]:   # most to least
    print(f"{harf} {k} {k * 100 / jami:.1f}%")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String matn = new Scanner(System.in).nextLine().toUpperCase();
        int[] soni = new int[26];
        int jami = 0;
        for (char c : matn.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                soni[c - 'A']++;
                jami++;
            }
        }
        System.out.println("letters: " + jami);
        List<Integer> harflar = new ArrayList<>();
        for (int i = 0; i < 26; i++) if (soni[i] > 0) harflar.add(i);
        harflar.sort((a, b) -> soni[b] != soni[a] ? soni[b] - soni[a] : a - b);   // most to least
        for (int i = 0; i < Math.min(5, harflar.size()); i++) {
            int h = harflar.get(i);
            System.out.println(String.format("%c %d %.1f%%", (char) ('A' + h), soni[h], soni[h] * 100.0 / jami));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string matn;
    getline(cin, matn);
    int soni[26] = {};
    int jami = 0;
    for (char c : matn) {
        c = toupper(c);
        if (c >= 'A' && c <= 'Z') {
            soni[c - 'A']++;
            jami++;
        }
    }
    printf("letters: %d\n", jami);
    vector<int> harflar;
    for (int i = 0; i < 26; i++) if (soni[i] > 0) harflar.push_back(i);
    sort(harflar.begin(), harflar.end(), [&](int a, int b) {
        return soni[a] != soni[b] ? soni[a] > soni[b] : a < b;               // most to least
    });
    for (size_t i = 0; i < min<size_t>(5, harflar.size()); i++) {
        int h = harflar[i];
        printf("%c %d %.1f%%\n", 'A' + h, soni[h], soni[h] * 100.0 / jami);
    }
    return 0;
}
```

Input:

```text
MEET ME AT THE OLD BRIDGE AFTER THE EVENING PRAYER AND BRING THE LETTER
```

Output:

```text
letters: 58
E 13 22.4%
T 8 13.8%
R 6 10.3%
A 4 6.9%
N 4 6.9%
```

Even if this text is encrypted with any monoalphabetic substitution, some symbol in the ciphertext occurs exactly 13 times (22.4%) — a cryptanalyst immediately guesses it is E. Polyalphabetic ciphers such as Vigenère were created precisely to "flatten" these frequencies.

## Worked example 2: how long brute force takes

Each line holds a key length *b* (bits) and the number of keys an attacker can try per second. Print the number of keys and the average time to find the key (in years, 1 year = 31,557,600 s) in scientific notation.

```python
import sys

YIL = 31557600
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    b, tezlik = map(int, satr.split())
    kalitlar = 2.0 ** b
    vaqt = 2.0 ** (b - 1) / tezlik / YIL            # on average — half the key space
    print(f"{b} bit: {kalitlar:.2e} keys, average {vaqt:.2e} years")
```
```java
import java.util.Scanner;

public class Main {
    static final double YIL = 31557600;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int b = in.nextInt();
            long tezlik = in.nextLong();
            double kalitlar = Math.pow(2, b);
            double vaqt = Math.pow(2, b - 1) / tezlik / YIL;        // on average — half the key space
            System.out.println(String.format("%d bit: %.2e keys, average %.2e years", b, kalitlar, vaqt));
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

const double YIL = 31557600;

int main() {
    int b;
    long long tezlik;
    while (cin >> b >> tezlik) {
        double kalitlar = pow(2.0, b);
        double vaqt = pow(2.0, b - 1) / tezlik / YIL;               // on average — half the key space
        printf("%d bit: %.2e keys, average %.2e years\n", b, kalitlar, vaqt);
    }
    return 0;
}
```

Input:

```text
40 1000000000
56 1000000000
128 1000000000000
```

Output:

```text
40 bit: 1.10e+12 keys, average 1.74e-05 years
56 bit: 7.21e+16 keys, average 1.14e+00 years
128 bit: 3.40e+38 keys, average 5.39e+18 years
```

A 40-bit key (the "export" ciphers of the 1990s) is found in ~9 minutes at a billion tries a second. 56-bit DES takes ~1 year on one computer, days on special hardware (in 1998 "Deep Crack" broke it in 56 hours). A 128-bit AES key, even at a trillion tries a second, needs hundreds of millions of times the age of the universe — that is why the current standard is at least 128 bits.

:::warning
**Common mistakes**

- **Mixing up encoding and encryption.** Base64 or ASCII is encoding (no key, anyone can reverse it); encryption uses a key.
- **Thinking "my own secret algorithm is more reliable".** It contradicts Kerckhoffs's principle: an unreviewed algorithm is almost always weak.
- **Believing a large key space is enough.** Monoalphabetic substitution has 26! keys, yet frequency analysis breaks it in minutes.
- **Setting symmetric and asymmetric encryption against each other.** In practice they are used together (hybrid).
:::

:::tip
Feed any English text (for example, the lyrics of your favourite song) to the first example's program and compare the result with the average English frequencies (E ≈ 12.7%, T ≈ 9.1%, A ≈ 8.2%). The longer the text, the closer the result — that is why short ciphertexts are harder to break.
:::

## Summary

- Cryptology = cryptography + cryptanalysis; *C* = *E*(*K*, *M*), *M* = *D*(*K*, *C*).
- Symmetric systems are fast but have the key delivery problem; asymmetric ones are slow but enable key exchange and signatures; in practice — hybrid.
- Kerckhoffs's principle: security depends only on the secrecy of the key.
- Historical ciphers: substitution (Atbash, Caesar, Vigenère) and transposition (the scytale); frequency analysis breaks monoalphabetic ciphers.
- Brute force takes 2^(*b*−1) tries; modern keys are at least 128 bits.

## Check yourself

:::check
Q: What does Kerckhoffs's principle state?
A: A system's security must depend only on the secrecy of the key, not of the algorithm.
Q: What is the main problem of symmetric encryption?
A: Delivering the shared secret key securely between the parties.
Q: What kind of cipher is Caesar and how many keys does it have?
A: A monoalphabetic substitution; 25 useful keys in the Latin alphabet.
Q: Why does frequency analysis break monoalphabetic substitution?
A: Each letter is always replaced by the same symbol, so the letter frequencies of the language survive in the ciphertext.
Q: On average how many tries does brute force need for a 64-bit key?
A: 2⁶³ ≈ 9.2·10¹⁸.
:::

:::link https://www.cryptool.org/en/
CrypTool
A free platform for learning cryptography and cryptanalysis interactively.
:::
