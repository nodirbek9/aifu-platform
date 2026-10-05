---
title: Applying classical ciphers (Caesar, Vigenère)
summary: Applying classical ciphers in practice — the Caesar cipher and its key space, the Atbash and affine ciphers, the simple substitution cipher, the Vigenère cipher and the Vigenère table; encryption by hand; programming tasks; cryptanalysis — frequency analysis, the Kasiski method, finding the key length with the index of coincidence (IC).
---

## Why does this topic matter?

In lecture M5 we met historical ciphers. Today they protect nothing, but through them you can try out the most important lessons of cryptography with your own hands: if the key space is small, **exhaustive search** is enough; even if the key space is large, a cipher still breaks when the **statistics** of the language survive in the ciphertext. Modern ciphers are designed precisely against these two weaknesses.

:::note
**After this session you will be able to:**

- encrypt and decrypt with the Caesar and Vigenère ciphers by hand;
- implement them in a program (the A5 tasks on the platform);
- find a Caesar key with frequency analysis;
- determine the length of a Vigenère key with the index of coincidence.
:::

## Brief theory

We replace letters with numbers: A = 0, B = 1, …, Z = 25; all operations are **mod 26**.

| Cipher | Encryption | Key space | Weakness |
|---|---|---|---|
| **Caesar** | *C* = (*P* + *k*) mod 26 | 25 keys | exhaustive search, frequency analysis |
| **Atbash** | *C* = 25 − *P* (A↔Z, B↔Y) | no key | opens immediately once the algorithm is known |
| **Affine** | *C* = (*a*·*P* + *b*) mod 26, gcd(*a*, 26) = 1 | 12 · 26 = 312 | exhaustive search, equations from two letter pairs |
| **Simple substitution** | each letter is replaced by another according to a table | 26! ≈ 4 · 10²⁶ | frequency analysis (despite the large key space!) |
| **Vigenère** | *C*ᵢ = (*P*ᵢ + *K*ᵢ mod *m*) mod 26 | 26^*m* | the key repeats: the Kasiski method, the index of coincidence |

The **Vigenère cipher** is several Caesar ciphers applied in turn: each key letter gives its own shift (A = 0, B = 1, …). When the key runs out, it repeats from the start. For work by hand the **Vigenère table** (tabula recta) is used: the row is the key letter, the column is the plaintext letter, and the intersection is the ciphertext letter. For three centuries it was considered "the unbreakable cipher" (le chiffre indéchiffrable); in 1863 F. Kasiski published a method to break it.

## Practical work

**Step 1. Encryption by hand.** Do it in your notebook and check the result.

- Caesar, *k* = 3: HUJUM → H+3 = K, U+3 = X, J+3 = M, U+3 = X, M+3 = P → **KXMXP**.
- Vigenère, key KEY: SALOM. Write the key repeatedly under the text: KEYKE. S(18)+K(10) = 28 mod 26 = 2 → C; A(0)+E(4) = 4 → E; L(11)+Y(24) = 35 mod 26 = 9 → J; O(14)+K(10) = 24 → Y; M(12)+E(4) = 16 → Q → **CEJYQ**.
- On your own: encrypt your name with Caesar *k* = 5; encrypt "TOSHKENT" with Vigenère, key LIMON, and give it to your neighbour to decrypt.

**Step 2. Program.** Complete the two tasks on the platform: **Caesar cipher** (a negative *k* means decryption) and **Vigenère cipher** (E — encrypt, D — decrypt; spaces and punctuation stay unchanged, the key index advances only on letters). Pay attention to preserving letter case and to negative remainders mod 26.

**Step 3. Cryptanalysis.** Opening a ciphertext without knowing the key — the two worked examples below are devoted to exactly this.

## Worked example 1: frequency analysis of a Caesar cipher

In every language letters occur unevenly: in Uzbek Latin text the most frequent letter is **A** (roughly one letter in 6–7), followed by I, O, L, R. The Caesar cipher does not change this distribution, it only shifts it. Count the letters in the ciphertext and print the most frequent ones together with the key *k* under the assumption "this letter is really A".

```python
import sys

matn = sys.stdin.read().upper()
sanoq = [0] * 26
for ch in matn:
    if "A" <= ch <= "Z":
        sanoq[ord(ch) - 65] += 1
jami = sum(sanoq)
print(f"letters: {jami}")
tartib = sorted(range(26), key=lambda i: (-sanoq[i], i))   # most to least frequent, ties in alphabetical order
for i in tartib[:4]:
    print(f"{chr(65 + i)} {sanoq[i]} {100 * sanoq[i] / jami:.1f}% k={i}")   # if this letter is A, k = i − 0
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] sanoq = new int[26];
        while (in.hasNextLine()) {
            for (char ch : in.nextLine().toUpperCase().toCharArray()) {
                if (ch >= 'A' && ch <= 'Z') sanoq[ch - 'A']++;
            }
        }
        int jami = 0;
        for (int s : sanoq) jami += s;
        System.out.println("letters: " + jami);
        List<Integer> tartib = new ArrayList<>();
        for (int i = 0; i < 26; i++) tartib.add(i);
        tartib.sort((a, b) -> sanoq[a] != sanoq[b] ? sanoq[b] - sanoq[a] : a - b);   // most to least frequent, ties in alphabetical order
        for (int t = 0; t < 4; t++) {
            int i = tartib.get(t);
            System.out.println(String.format("%c %d %.1f%% k=%d", (char) ('A' + i), sanoq[i], 100.0 * sanoq[i] / jami, i));   // if this letter is A, k = i − 0
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
    vector<int> sanoq(26, 0);
    string satr;
    while (getline(cin, satr)) {
        for (unsigned char ch : satr) {
            char c = toupper(ch);
            if (c >= 'A' && c <= 'Z') sanoq[c - 'A']++;
        }
    }
    int jami = 0;
    for (int s : sanoq) jami += s;
    printf("letters: %d\n", jami);
    vector<int> tartib(26);
    for (int i = 0; i < 26; i++) tartib[i] = i;
    stable_sort(tartib.begin(), tartib.end(), [&](int a, int b) { return sanoq[a] > sanoq[b]; });   // most to least frequent, ties in alphabetical order
    for (int t = 0; t < 4; t++) {
        int i = tartib[t];
        printf("%c %d %.1f%% k=%d\n", 'A' + i, sanoq[i], 100.0 * sanoq[i] / jami, i);   // if this letter is A, k = i − 0
    }
    return 0;
}
```

Input:

```text
LYAHNH THQSPZ ZVHA VUKH IVZOSHUHKP IHYJOH EVKPTSHY RHAAH GHSNH RLSZPU OPZVIVASHYUP AHFFVYSHI XVFPUN
```

Output:

```text
letters: 87
H 17 19.5% k=7
V 8 9.2% k=21
P 7 8.0% k=15
S 7 8.0% k=18
```

**H** occurs twice as often as any other letter — it is almost certainly A. First hypothesis *k* = 7: run your Caesar program with *k* = −7 and check that readable text comes out ("ERTAGA MAJLIS…" — Uzbek for "the meeting is tomorrow…"). If the text is unreadable, try the next hypothesis. Statistics can mislead on a short text, but trying all 25 keys is also a one-second job.

## Worked example 2: finding the Vigenère key length

The Vigenère cipher "flattens" letter frequencies — plain frequency analysis does not work. But if the key length is *m*, every *m*-th letter is encrypted with **one** Caesar cipher. The **index of coincidence** is the probability that two randomly chosen letters are the same: IC = Σ *n*ᵢ(*n*ᵢ − 1) / (*N*(*N* − 1)). In natural text IC ≈ 0.07, in random letters ≈ 0.038. The first line holds the largest length to try, *M*, the second the ciphertext. For each *m* = 1…*M* split the text into *m* columns (every *m*-th letter), print the average IC of the columns and find the *m* with the largest IC.

```python
import sys

satrlar = sys.stdin.read().split("\n")
M = int(satrlar[0])
harflar = [ch for ch in " ".join(satrlar[1:]).upper() if "A" <= ch <= "Z"]
eng_yaxshi, eng_ic = 1, -1.0
for m in range(1, M + 1):
    jami = 0.0
    for r in range(m):
        ustun = harflar[r::m]                     # every m-th letter starting from letter r
        n = len(ustun)
        sanoq = [0] * 26
        for ch in ustun:
            sanoq[ord(ch) - 65] += 1
        jami += sum(c * (c - 1) for c in sanoq) / (n * (n - 1))
    ic = jami / m
    if ic > eng_ic:
        eng_yaxshi, eng_ic = m, ic
    print(f"m={m} IC={ic:.4f}")
print(f"probable key length: {eng_yaxshi}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int M = Integer.parseInt(in.nextLine().trim());
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            for (char ch : in.nextLine().toUpperCase().toCharArray()) {
                if (ch >= 'A' && ch <= 'Z') sb.append(ch);
            }
        }
        String harflar = sb.toString();
        int engYaxshi = 1;
        double engIc = -1.0;
        for (int m = 1; m <= M; m++) {
            double jami = 0.0;
            for (int r = 0; r < m; r++) {
                int[] sanoq = new int[26];
                int n = 0;
                for (int i = r; i < harflar.length(); i += m) {   // every m-th letter starting from letter r
                    sanoq[harflar.charAt(i) - 'A']++;
                    n++;
                }
                long juftlar = 0;
                for (int c : sanoq) juftlar += (long) c * (c - 1);
                jami += (double) juftlar / ((long) n * (n - 1));
            }
            double ic = jami / m;
            if (ic > engIc) {
                engYaxshi = m;
                engIc = ic;
            }
            System.out.println(String.format("m=%d IC=%.4f", m, ic));
        }
        System.out.println("probable key length: " + engYaxshi);
    }
}
```
```cpp
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int M = stoi(satr);
    string harflar;
    while (getline(cin, satr)) {
        for (unsigned char ch : satr) {
            char c = toupper(ch);
            if (c >= 'A' && c <= 'Z') harflar += c;
        }
    }
    int engYaxshi = 1;
    double engIc = -1.0;
    for (int m = 1; m <= M; m++) {
        double jami = 0.0;
        for (int r = 0; r < m; r++) {
            long long sanoq[26] = {0};
            long long n = 0;
            for (size_t i = r; i < harflar.size(); i += m) {   // every m-th letter starting from letter r
                sanoq[harflar[i] - 'A']++;
                n++;
            }
            long long juftlar = 0;
            for (long long c : sanoq) juftlar += c * (c - 1);
            jami += (double) juftlar / (n * (n - 1));
        }
        double ic = jami / m;
        if (ic > engIc) {
            engYaxshi = m;
            engIc = ic;
        }
        printf("m=%d IC=%.4f\n", m, ic);
    }
    printf("probable key length: %d\n", engYaxshi);
    return 0;
}
```

Input:

```text
8
KXMWKYT IIOPSTHESGT PTB BTZ MKSSSBVOE CVRUY UNRIX DTJIQI ASSZJEKNLLB MHFVDS MLFYSY XIEEMZBEKR EIKAAWQU UEEAT UAEBT JACIK UEWIWS SSCGSNR CVRUY FHNIXTTB PLZHVLLZGS HPKA UIXOT KYEUTCLTOB FA DPNLHLTB HAETTBDLOB RAGWEKLLZGS ONPFKSWQZS KPZTU TLZFYQ LLFSNTAMBAEWKS EDI RKNRQEKNTAAVACVB FABBBNA ZZGKTLLB FA KIQSRL VNCXLTTBNT UNXTLHTW TPSLRICQU LOCIWS
```

Output:

```text
m=1 IC=0.0456
m=2 IC=0.0458
m=3 IC=0.0435
m=4 IC=0.0473
m=5 IC=0.0731
m=6 IC=0.0416
m=7 IC=0.0454
m=8 IC=0.0475
probable key length: 5
```

At *m* = 5 the IC jumps to the value of natural text (≈ 0.07) — at other lengths it stays close to random letters. So the key has 5 letters. Next step: split the text into 5 columns and apply the frequency analysis from example 1 to each — the most frequent letter of each column gives one key letter. This way you find the key (here KALIT) and decrypt with your Vigenère program. (Multiples of *m* — 10, 15 — also give a high IC; that is why the smallest "high" value is chosen.)

:::warning
**Common mistakes**

- **Negative remainder.** In Python (−3) % 26 = 23, but in Java and C++ it is −3; use (*x* % 26 + 26) % 26 when decrypting.
- **Advancing the key index on spaces as well.** In the Vigenère task the key moves forward only on letters.
- **Losing letter case.** Preserve the letter type: shifts based on 'a' and on 'A' are separate.
- **Frequency analysis on a short text.** With 20–30 letters the statistics are unreliable; try several hypotheses.
- **Using classical ciphers for real data.** They are for learning only; in practice AES (A6) is used.
:::

:::tip
Check your results in the online tool **CyberChef**: the "ROT13" recipe (Amount = *k*) performs the Caesar cipher, and "Vigenère Encode/Decode" the Vigenère cipher. Create your own ciphertext and ask your neighbour to open it without the key.
:::

## Summary

- Caesar: *C* = (*P* + *k*) mod 26, 25 keys — easily broken by exhaustive search and frequency analysis.
- The simple substitution cipher has a large key space (26!), but since letter statistics survive, frequency analysis breaks it.
- Vigenère — Caesar ciphers applied in turn by the key letters; because the key repeats, it is broken by the Kasiski method and the index of coincidence.
- Index of coincidence: ≈ 0.07 in natural text, ≈ 0.038 in random letters — at the right key length the column IC rises sharply.
- The lesson: a secure cipher must have a large key space and leave no language statistics in the ciphertext.

## Check yourself

:::check
Q: With a Caesar key k = 3, how is "ABC" encrypted?
A: "DEF".
Q: Why do the 26! keys of a simple substitution cipher not make it secure?
A: Each letter is always replaced by the same letter, so the language frequencies survive and frequency analysis finds the key without searching.
Q: What weakness does key repetition give the Vigenère cipher?
A: Every m-th letter is encrypted with one Caesar cipher — once the key length is found, the cipher splits into m Caesar ciphers.
Q: How does the index of coincidence reveal the key length?
A: At the right length each column is a single Caesar cipher and the IC rises to the natural-language value (≈ 0.07); at a wrong length it stays close to the random value.
:::

:::link https://gchq.github.io/CyberChef/
CyberChef
A free online tool for working with classical and modern ciphers, encodings and hashes.
:::
