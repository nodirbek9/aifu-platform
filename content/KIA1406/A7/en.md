---
title: Applying the RSA algorithm through a simple example
summary: Applying RSA in practice — creating a key, encrypting and decrypting by hand with small numbers; a programming task (the extended Euclidean algorithm, modular exponentiation); a 2048-bit key in OpenSSL, encryption with OAEP and signing with SHA-256; weaknesses of "textbook" RSA — the small e = 3 attack, a dictionary attack on deterministic encryption, multiplicativity; why padding (OAEP, PSS) is mandatory.
---

## Why does this topic matter?

In lecture M7 we saw the mathematics of RSA: *n* = *p*·*q*, *e*·*d* ≡ 1 (mod φ(*n*)), *c* = *m*ᵉ mod *n*. In this session you carry out the formulas by hand and in your own program, and then work with a real tool (OpenSSL). The most important lesson: the formulas alone — **"textbook RSA"** — are not secure. In the two worked examples below you will see how RSA without padding is opened without knowing the key.

:::note
**After this session you will be able to:**

- create an RSA key by hand with small numbers, and encrypt and decrypt with it;
- implement the RSA steps in a program (task A7 on the platform);
- create a key, encrypt with OAEP and sign in OpenSSL;
- demonstrate the weaknesses of RSA without padding.
:::

## Practical work

**Step 1. RSA by hand.** Let *p* = 5, *q* = 11.

1. *n* = 5 · 11 = 55; φ(*n*) = 4 · 10 = 40.
2. *e* = 3: gcd(3, 40) = 1 — it fits.
3. Find *d*: 3 · *d* ≡ 1 (mod 40). Run the extended Euclidean algorithm by hand or check *d* = 1, 2, 3, … (answer: *d* = 27, because 3 · 27 = 81 = 2 · 40 + 1).
4. Encrypt *m* = 7: 7³ = 343; 343 mod 55 = 13 → *c* = 13.
5. Decryption: compute 13²⁷ mod 55 by repeated squaring (13² = 169 ≡ 4, 13⁴ ≡ 16, 13⁸ ≡ 36, 13¹⁶ ≡ 31; 27 = 16 + 8 + 2 + 1) and check that you get 7.

On your own: with *p* = 7, *q* = 13, *e* = 5 encrypt and decrypt *m* = 10.

**Step 2. Program.** Complete the task **RSA: key generation, encryption and decryption** on the platform: *n*, φ(*n*), *d* with the extended Euclidean algorithm, *c* = *m*ᵉ mod *n* and the check *c*ᵈ mod *n* = *m*. Make sure intermediate products do not overflow (`long` in Java, `long long` or `__int128` in C++).

**Step 3. Real RSA (OpenSSL).** In Git Bash, Linux or macOS:

```text
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem
openssl pkeyutl -encrypt -pubin -inkey public.pem -pkeyopt rsa_padding_mode:oaep -in message.txt -out message.enc
openssl pkeyutl -decrypt -inkey private.pem -pkeyopt rsa_padding_mode:oaep -in message.enc
openssl dgst -sha256 -sign private.pem -out signature.bin document.txt
openssl dgst -sha256 -verify public.pem -signature signature.bin document.txt
```

Encrypt the same message twice — because OAEP adds random padding, the `message.enc` files differ. After signing, add one character to `document.txt` and repeat the verification: instead of "Verified OK" you get "Verification failure".

## Weaknesses of "textbook RSA"

| Weakness | Essence | Protection |
|---|---|---|
| **Small *e* and a short message** | if *m*ᵉ < *n*, the mod does nothing: *m* = ∛*c* is a plain root | OAEP padding expands *m* to the size of *n* |
| **Determinism** | the same *m* always gives the same *c* — messages with few variants can be found with a dictionary | OAEP adds random bytes |
| **Multiplicativity** | *c*₁ · *c*₂ mod *n* = (*m*₁ · *m*₂)ᵉ mod *n* — the ciphertext can be changed meaningfully | padding breaks the structure |
| **Small or close *p*, *q*** | factoring *n* is easy (M7, example 2) | an *n* of at least 2048 bits, random primes |
| **Signature without padding** | forging a signature for a new message by multiplying signatures | hash + PSS padding |

## Worked example 1: e = 3 and a short message

The first line holds the modulus *n*, the following lines ciphertexts encrypted with *e* = 3 without padding. A message is the number built from the bytes of its text (for example, "OK" = 0x4F4B = 20299). If *m*³ < *n*, then *c* = *m*³ — a plain integer cube root gives *m*. For each *c* find the integer cube root by binary search; if it is an exact cube, print *m* and its text, otherwise print `not found`.

```python
import sys

satrlar = sys.stdin.read().split()
n = int(satrlar[0])                  # no private key needed — n is for information only
for t in satrlar[1:]:
    c = int(t)
    past, yuqori = 0, 2097151         # 2097151^3 < 2^63
    while past < yuqori:              # the largest r with r^3 <= c
        orta = (past + yuqori + 1) // 2
        if orta ** 3 <= c:
            past = orta
        else:
            yuqori = orta - 1
    if past ** 3 == c:
        matn = ""
        x = past
        while x > 0:
            matn = chr(x % 256) + matn    # the bytes of the number are the text characters
            x //= 256
        print(f"{c} -> m={past} {matn}")
    else:
        print(f"{c} -> not found")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = in.nextLong();                     // no private key needed — n is for information only
        while (in.hasNextLong()) {
            long c = in.nextLong();
            long past = 0, yuqori = 2097151;        // 2097151^3 < 2^63
            while (past < yuqori) {                 // the largest r with r^3 <= c
                long orta = (past + yuqori + 1) / 2;
                if (orta * orta * orta <= c) past = orta;
                else yuqori = orta - 1;
            }
            if (past * past * past == c) {
                StringBuilder matn = new StringBuilder();
                for (long x = past; x > 0; x /= 256) {
                    matn.insert(0, (char) (x % 256));   // the bytes of the number are the text characters
                }
                System.out.println(c + " -> m=" + past + " " + matn);
            } else {
                System.out.println(c + " -> not found");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    long long n, c;
    cin >> n;                                       // no private key needed — n is for information only
    while (cin >> c) {
        long long past = 0, yuqori = 2097151;       // 2097151^3 < 2^63
        while (past < yuqori) {                     // the largest r with r^3 <= c
            long long orta = (past + yuqori + 1) / 2;
            if (orta * orta * orta <= c) past = orta;
            else yuqori = orta - 1;
        }
        if (past * past * past == c) {
            string matn;
            for (long long x = past; x > 0; x /= 256) {
                matn = char(x % 256) + matn;        // the bytes of the number are the text characters
            }
            cout << c << " -> m=" << past << " " << matn << endl;
        } else {
            cout << c << " -> not found" << endl;
        }
    }
    return 0;
}
```

Input:

```text
1000000104000000679
8364190790899
6328545249473
615573431271999514
```

Output:

```text
8364190790899 -> m=20299 OK
6328545249473 -> m=18497 HA
615573431271999514 -> not found
```

The first two messages were opened without the private key, just with a cube root: for "OK" and "HA" *m*³ < *n*, that is, no modular reduction happened at all. The third message ("SIR" = 5458258) is longer: *m*³ > *n*, the reduction took place and the plain root does not work. In real RSA, OAEP turns any message into a random-looking number as long as *n* — this attack becomes impossible.

## Worked example 2: a dictionary attack on deterministic RSA

A teacher sends grades (2, 3, 4, 5) to students encrypted with a public key, without padding. The attacker knows the public key — they encrypt every possible grade themselves and compare with the intercepted ciphertexts. Input: line 1 *n* *e*; line 2 the possible messages; line 3 the intercepted ciphertexts. For each ciphertext print the matching message or `?`.

```python
import sys

satrlar = sys.stdin.read().split("\n")
n, e = map(int, satrlar[0].split())
lugat = {}
for m in satrlar[1].split():
    lugat[pow(int(m), e, n)] = m          # encrypt every candidate with the public key
for c in satrlar[2].split():
    print(f"{c} -> {lugat.get(int(c), '?')}")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static long darajaMod(long a, long k, long n) {   // a^k mod n (n < 2^31 — the product fits in long)
        long natija = 1;
        a %= n;
        while (k > 0) {
            if ((k & 1) == 1) natija = natija * a % n;
            a = a * a % n;
            k >>= 1;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] kalit = in.nextLine().trim().split("\\s+");
        long n = Long.parseLong(kalit[0]);
        long e = Long.parseLong(kalit[1]);
        Map<Long, String> lugat = new HashMap<>();
        for (String m : in.nextLine().trim().split("\\s+")) {
            lugat.put(darajaMod(Long.parseLong(m), e, n), m);   // encrypt every candidate with the public key
        }
        for (String c : in.nextLine().trim().split("\\s+")) {
            System.out.println(c + " -> " + lugat.getOrDefault(Long.parseLong(c), "?"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
using namespace std;

long long darajaMod(long long a, long long k, long long n) {   // a^k mod n (n < 2^31 — the product fits in long long)
    long long natija = 1;
    a %= n;
    while (k > 0) {
        if (k & 1) natija = natija * a % n;
        a = a * a % n;
        k >>= 1;
    }
    return natija;
}

int main() {
    string satr;
    getline(cin, satr);
    long long n, e;
    istringstream(satr) >> n >> e;
    map<long long, string> lugat;
    getline(cin, satr);
    istringstream variantlar(satr);
    string m;
    while (variantlar >> m) {
        lugat[darajaMod(stoll(m), e, n)] = m;      // encrypt every candidate with the public key
    }
    getline(cin, satr);
    istringstream ushlangan(satr);
    string c;
    while (ushlangan >> c) {
        auto it = lugat.find(stoll(c));
        cout << c << " -> " << (it != lugat.end() ? it->second : "?") << endl;
    }
    return 0;
}
```

Input:

```text
100160063 65537
2 3 4 5
1727207 24561622 82696754 1727207 11111
```

Output:

```text
1727207 -> 4
24561622 -> 5
82696754 -> 2
1727207 -> 4
11111 -> ?
```

The attacker read all the grades without knowing the private key or the factors of *n* — because the message space has only 4 elements. The first and fourth ciphertexts are equal, so the grades are equal too — that is also an information leak. Key length does not help here: with a 4096-bit *n* the result is the same. The solution is **random padding** (OAEP): the same grade gives a different ciphertext every time.

:::warning
**Common mistakes**

- **Overflow of an intermediate product.** In *a* · *b* mod *n* the product *a* · *b* may exceed 64 bits; reduce mod at every step and choose a wide enough type.
- **Computing mᵉ in full and only then taking mod.** The number becomes huge; use repeated squaring (square-and-multiply).
- **An e with gcd(e, φ(n)) ≠ 1.** For such an *e* no *d* exists.
- **Using "textbook RSA" for real data.** Use only OAEP for encryption and PSS for signatures; in practice RSA is usually used only to transport a symmetric key and for signatures.
- **Leaving the private key file unprotected.** Encrypt `private.pem` with a password (`-aes256`) and never send it to anyone.
:::

:::tip
The command `openssl pkey -in private.pem -text -noout` shows the contents of the key: modulus, publicExponent (65537), privateExponent, prime1, prime2, and for speed-up exponent1, exponent2 and coefficient (the parameters of the Chinese remainder theorem). Run it only on a practice key.
:::

## Summary

- RSA steps: *n* = *pq*, φ(*n*), *e* (gcd = 1), *d* = *e*⁻¹ mod φ(*n*), *c* = *m*ᵉ mod *n*, *m* = *c*ᵈ mod *n*.
- Computation: the extended Euclidean algorithm and repeated squaring; avoid overflow.
- OpenSSL: `genpkey`, `pkeyutl` (OAEP) and `dgst -sign/-verify` (SHA-256 signature).
- RSA without padding is weak: a cube root for small *e*, a dictionary attack because of determinism, multiplicativity.
- In practice: a key of at least 2048 bits, OAEP for encryption, PSS for signatures.

## Check yourself

:::check
Q: If p = 5, q = 11, e = 3, what is d?
A: 27, because φ(n) = 40 and 3 · 27 = 81 ≡ 1 (mod 40).
Q: Why can a ciphertext be opened without the key when e = 3 and the message is short?
A: If m³ < n, no modular reduction happens and c = m³ — a plain integer cube root gives m.
Q: What attack does deterministic encryption allow?
A: A dictionary attack: the attacker encrypts all likely messages with the public key and compares the ciphertexts.
Q: What protection does OAEP give?
A: It adds random padding: the same message gives a different ciphertext every time, and the message is expanded to the length of n.
:::

:::link https://www.rfc-editor.org/rfc/rfc8017
RFC 8017: PKCS #1 — RSA Cryptography Specifications
The specification of RSA, OAEP encryption and the PSS signature standard.
:::
