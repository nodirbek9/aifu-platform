---
title: Practice with an encryption program using symmetric algorithms (AES)
summary: Working with AES in practice — checking the FIPS 197 test vector in CyberChef, choosing the key, IV and mode, deriving a key from a password (PBKDF2), encrypting and decrypting a file with OpenSSL, 7-Zip archives and disk encryption (BitLocker, VeraCrypt); AES internals — multiplication in the field GF(2⁸), computing the S-box (SubBytes) and MixColumns; a test on the topic.
---

## Why does this topic matter?

In lecture M6 we studied the structure of AES and its modes of operation in theory. In practice the mistakes are not in the algorithm but in **how it is used**: a plain password instead of a key, a repeated IV, ECB mode, the key stored next to the ciphertext. In this session we apply AES in real tools, check the result against an official test vector and compute two of the internal AES steps ourselves.

:::note
**After this session you will be able to:**

- encrypt and decrypt with AES in CyberChef and OpenSSL;
- choose the key, IV, mode and padding correctly;
- explain how a key is derived from a password (PBKDF2, salt);
- compute multiplication in GF(2⁸), S-box and MixColumns values.
:::

## Reminder: AES parameters

| Parameter | Value |
|---|---|
| Block | always 128 bits (16 bytes) |
| Key | 128, 192 or 256 bits → 10, 12 or 14 rounds |
| Round | SubBytes → ShiftRows → MixColumns → AddRoundKey (no MixColumns in the last round) |
| Mode | **GCM** (encryption + integrity) is recommended; CBC — if the IV is random; ECB — do not use |
| IV / nonce | 16 random bytes in CBC; 12 bytes in GCM, **never repeated** with the same key |
| Padding | PKCS#7 in CBC and ECB (*n* missing bytes — *n* bytes of value *n*); not needed in CTR and GCM |

## Practical work

**Step 1. Test vector (CyberChef).** At gchq.github.io/CyberChef choose the "AES Encrypt" recipe: Key = `000102030405060708090a0b0c0d0e0f` (Hex), Mode = ECB, Input = `00112233445566778899aabbccddeeff` (Input format: Hex, Output: Hex). The result must equal the value from the FIPS 197 standard: `69c4e0d86a7b0430d8cdb78070b4c55a` (because of PKCS#7 one more padding block follows it). A test vector is the standard way to check that a program works correctly.

**Step 2. Modes.** In the same recipe set Mode = CBC, IV = 16 zero bytes, then change one byte of the IV — the whole ciphertext changes. In GCM mode a **Tag** (authentication code) is added to the result: if you change one byte of the ciphertext and run "AES Decrypt", decryption fails — this is integrity protection.

**Step 3. Encrypting a file with a password (OpenSSL).** On Linux, macOS or in Git Bash:

```text
openssl enc -aes-256-cbc -pbkdf2 -iter 600000 -salt -in report.txt -out report.enc
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in report.enc -out report2.txt
```

Here the password is not the key itself: **PBKDF2** computes a hash 600,000 times from the password and a random **salt** and produces a 256-bit key and an IV — guessing the password becomes that many times slower. Encrypt the same file twice: since the salt is new every time, the ciphertexts differ.

**Step 4. Everyday tools.** When creating an archive in 7-Zip choose "Encryption method: AES-256" and "Encrypt file names". Full-disk encryption: **BitLocker** on Windows (in Pro editions), **VeraCrypt** on other systems. All of them run AES inside; security depends mostly on **password quality** and on where you keep the recovery key.

**Step 5. Test.** Complete the "Symmetric encryption and AES" test on the platform.

## Worked example 1: GF(2⁸) and the S-box

AES treats bytes as elements of the field **GF(2⁸)**: addition is XOR, multiplication is multiplying polynomials and taking the remainder modulo *x*⁸ + *x*⁴ + *x*³ + *x* + 1 (0x11B). The SubBytes table (S-box) is computed for each byte in two steps: 1) the inverse element in the field *b* = *x*⁻¹ (0 for 0); 2) the affine transformation *s* = *b* ⊕ rotl(*b*, 1) ⊕ rotl(*b*, 2) ⊕ rotl(*b*, 3) ⊕ rotl(*b*, 4) ⊕ 0x63. For each hex byte print the inverse element and the S-box value.

```python
import sys


def kopaytir(a, b):                       # multiplication in GF(2^8)
    r = 0
    while b:
        if b & 1:
            r ^= a                        # addition = XOR
        a <<= 1
        if a & 0x100:
            a ^= 0x11B                    # modulo x^8 + x^4 + x^3 + x + 1
        b >>= 1
    return r


def teskari(a):                           # a^254 = a^(-1), because a^255 = 1
    if a == 0:
        return 0
    r = 1
    for _ in range(254):
        r = kopaytir(r, a)
    return r


def sbox(x):
    b = teskari(x)
    s = b
    for i in range(1, 5):
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF   # rotate left
    return s ^ 0x63


for t in sys.stdin.read().split():
    x = int(t, 16)
    print(f"{t} -> inv={teskari(x):02x} S={sbox(x):02x}")
```
```java
import java.util.Scanner;

public class Main {
    static int kopaytir(int a, int b) {                  // multiplication in GF(2^8)
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;                    // addition = XOR
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;            // modulo x^8 + x^4 + x^3 + x + 1
            b >>= 1;
        }
        return r;
    }

    static int teskari(int a) {                          // a^254 = a^(-1), because a^255 = 1
        if (a == 0) return 0;
        int r = 1;
        for (int i = 0; i < 254; i++) r = kopaytir(r, a);
        return r;
    }

    static int sbox(int x) {
        int b = teskari(x);
        int s = b;
        for (int i = 1; i <= 4; i++) {
            s ^= ((b << i) | (b >> (8 - i))) & 0xFF;     // rotate left
        }
        return s ^ 0x63;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int x = Integer.parseInt(t, 16);
            System.out.println(String.format("%s -> inv=%02x S=%02x", t, teskari(x), sbox(x)));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int kopaytir(int a, int b) {                             // multiplication in GF(2^8)
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;                               // addition = XOR
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;                       // modulo x^8 + x^4 + x^3 + x + 1
        b >>= 1;
    }
    return r;
}

int teskari(int a) {                                     // a^254 = a^(-1), because a^255 = 1
    if (a == 0) return 0;
    int r = 1;
    for (int i = 0; i < 254; i++) r = kopaytir(r, a);
    return r;
}

int sbox(int x) {
    int b = teskari(x);
    int s = b;
    for (int i = 1; i <= 4; i++) {
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF;         // rotate left
    }
    return s ^ 0x63;
}

int main() {
    string t;
    while (cin >> t) {
        int x = stoi(t, nullptr, 16);
        printf("%s -> inv=%02x S=%02x\n", t.c_str(), teskari(x), sbox(x));
    }
    return 0;
}
```

Input:

```text
00 01 53 c9 ff 10
```

Output:

```text
00 -> inv=00 S=63
01 -> inv=01 S=7c
53 -> inv=ca S=ed
c9 -> inv=27 S=dd
ff -> inv=1c S=16
10 -> inv=74 S=ca
```

The results match the S-box table in FIPS 197 (for example S(53) = ed — the example from the standard). Check by hand that 0x53 · 0xca = 1. The S-box is the only **non-linear** part of AES: because of the inverse element there is no simple linear relation between input and output — this is the main protection against linear and differential cryptanalysis. Practical programs do not compute the S-box; they use a ready 256-byte table.

## Worked example 2: MixColumns

MixColumns multiplies each column (4 bytes) of the state matrix in GF(2⁸) by the following matrix:

```text
02 03 01 01
01 02 03 01
01 01 02 03
03 01 01 02
```

Each line holds 4 hex bytes (a column); print the resulting column.

```python
import sys

M = [[2, 3, 1, 1], [1, 2, 3, 1], [1, 1, 2, 3], [3, 1, 1, 2]]


def kopaytir(a, b):                       # multiplication in GF(2^8)
    r = 0
    while b:
        if b & 1:
            r ^= a
        a <<= 1
        if a & 0x100:
            a ^= 0x11B
        b >>= 1
    return r


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    ustun = [int(t, 16) for t in satr.split()]
    natija = []
    for i in range(4):
        v = 0
        for j in range(4):
            v ^= kopaytir(M[i][j], ustun[j])   # sum = XOR
        natija.append(v)
    print(satr.strip() + " -> " + " ".join(format(v, "02x") for v in natija))
```
```java
import java.util.Scanner;

public class Main {
    static final int[][] M = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

    static int kopaytir(int a, int b) {                  // multiplication in GF(2^8)
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;
            b >>= 1;
        }
        return r;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] t = satr.split("\\s+");
            int[] ustun = new int[4];
            for (int j = 0; j < 4; j++) ustun[j] = Integer.parseInt(t[j], 16);
            StringBuilder sb = new StringBuilder(satr + " ->");
            for (int i = 0; i < 4; i++) {
                int v = 0;
                for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // sum = XOR
                sb.append(String.format(" %02x", v));
            }
            System.out.println(sb);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const int M[4][4] = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

int kopaytir(int a, int b) {                             // multiplication in GF(2^8)
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;
        b >>= 1;
    }
    return r;
}

int main() {
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        int ustun[4];
        string t;
        int n = 0;
        while (n < 4 && ss >> t) ustun[n++] = stoi(t, nullptr, 16);
        if (n < 4) continue;
        for (int j = 0; j < 4; j++) printf(j ? " %02x" : "%02x", ustun[j]);
        printf(" ->");
        for (int i = 0; i < 4; i++) {
            int v = 0;
            for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // sum = XOR
            printf(" %02x", v);
        }
        printf("\n");
    }
    return 0;
}
```

Input:

```text
db 13 53 45
f2 0a 22 5c
01 01 01 01
c6 c6 c6 c6
d4 d4 d4 d5
2d 26 31 4c
```

Output:

```text
db 13 53 45 -> 8e 4d a1 bc
f2 0a 22 5c -> 9f dc 58 9d
01 01 01 01 -> 01 01 01 01
c6 c6 c6 c6 -> c6 c6 c6 c6
d4 d4 d4 d5 -> d5 d5 d7 d6
2d 26 31 4c -> 4d 7e bd f8
```

These are the widely used MixColumns test values. A column of identical bytes does not change (2 ⊕ 3 ⊕ 1 ⊕ 1 = 1), but in `d4 d4 d4 d5` a change of one bit in one byte spread to **all** four bytes of the column — this is Shannon's **diffusion** principle. Together with ShiftRows, after two rounds every input byte affects all 16 bytes of the state.

:::warning
**Common mistakes**

- **Using a password directly as the key.** Derive a key from a password only with PBKDF2, scrypt or Argon2, adding a salt.
- **Making the IV always zero or always the same.** In CBC, files that start the same give the same ciphertext; in GCM, a repeated nonce breaks the protection completely.
- **ECB mode.** It is often the "default" mode in libraries — always specify the mode explicitly.
- **Keeping the key next to the encrypted file.** The key belongs elsewhere: a key vault, a password manager, a hardware module.
- **Encryption only, without integrity.** CBC does not protect the ciphertext from modification; use GCM or encryption + HMAC.
:::

:::tip
OpenSSL's `-p` flag (`openssl enc -aes-256-cbc -pbkdf2 -p ...`) shows the salt, key and IV derived from the password — run it twice and see that the salt is new each time. Use this flag only on practice files: it prints the key to the screen.
:::

## Summary

- AES: 128-bit block, 128/192/256-bit key; in practice AES-GCM or AES-CBC + HMAC, a random IV or a non-repeating nonce.
- Program correctness is checked with official test vectors (FIPS 197: `69c4e0d8…`).
- A key from a password — with PBKDF2/scrypt/Argon2 and a salt; the password itself is not the key.
- In GF(2⁸) addition is XOR and multiplication is modulo 0x11B; the S-box is the inverse element + an affine transformation, the non-linear part of AES.
- MixColumns spreads a change of one byte over the whole column — the diffusion principle.

## Check yourself

:::check
Q: Why do the ciphertexts differ when the same file is encrypted twice with OpenSSL using the same password?
A: A new random salt is used each time, so PBKDF2 derives a different key and IV.
Q: What happens on decryption if one byte of a GCM ciphertext is changed?
A: The authentication code (tag) does not match and decryption is rejected with an error.
Q: Which AES step is non-linear and how is it computed?
A: SubBytes (the S-box): the inverse element in GF(2⁸) followed by an affine transformation.
Q: Which cryptographic principle does MixColumns implement?
A: Diffusion: a change of one input byte affects all bytes of the column.
:::

:::link https://csrc.nist.gov/pubs/fips/197/final
FIPS 197: Advanced Encryption Standard (AES)
The AES standard: the S-box table, MixColumns and test vectors.
:::
