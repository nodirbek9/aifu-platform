---
title: Public key cryptosystems. RSA, modular arithmetic, public keys, advantages and problems
summary: The key distribution problem and the idea of public key cryptography; one-way functions and the "trapdoor"; modular arithmetic — residues, modular exponentiation, GCD, the inverse element, Euler's function and theorem; the Diffie–Hellman key exchange and the discrete logarithm; RSA — key generation, encryption, decryption, security based on the hardness of factoring; elliptic curves (ECC); advantages, problems, padding, the quantum threat.
---

## Why it matters

Symmetric encryption is fast, but one question remains: how do two parties who have never met — say, your browser and a bank's server — agree on a shared secret key? The key cannot be sent over an open network. In 1976 Diffie and Hellman, and in 1977 Rivest, Shamir and Adleman, solved this with a "mathematical lock": an operation that is easy to perform but practically impossible to reverse without secret information. Today every HTTPS connection, digital signature and messenger relies on this idea.

:::note
**After this lesson you will be able to:**

- explain which problem public key cryptography solves;
- perform modular arithmetic operations (exponentiation, GCD, inverse, Euler's function);
- calculate the Diffie–Hellman key exchange step by step;
- explain how RSA keys are created and what RSA's security rests on;
- know the advantages and problems of public key systems and the key length requirements.
:::

## The idea: public and private keys

Each user has a **pair** of keys: a **public key** (published to everyone) and a **private key** (kept only by its owner). A message encrypted with the public key can be opened only with the private key. It is like a letterbox: anyone can drop a letter into the slot, but only the key holder can open the box.

The basis is a **one-way function**: computing *f*(*x*) is easy, finding *x* from *f*(*x*) is hard. For example, multiplying two large primes takes milliseconds; splitting a 600-digit product into its prime factors would take billions of years. With a **trapdoor** function, some secret extra information (*p* and *q*) makes the reverse easy too.

## Modular arithmetic

| Concept | Definition | Example |
|---|---|---|
| Residue modulo *n* | *a* mod *n* — the remainder of dividing *a* by *n* | 17 mod 5 = 2 |
| Modular exponentiation | *a*ᵇ mod *n* | 3⁴ mod 5 = 81 mod 5 = 1 |
| GCD | the greatest common divisor; by Euclid's algorithm | GCD(48, 18) = 6 |
| Inverse element | *a*·*x* ≡ 1 (mod *n*); exists only if GCD(*a*, *n*) = 1; found by the extended Euclidean algorithm | 3·7 ≡ 1 (mod 10) |
| Euler's function φ(*n*) | the count of numbers in 1..*n* coprime to *n*; for primes *p*, *q*: φ(*pq*) = (*p* − 1)(*q* − 1) | φ(15) = 2·4 = 8 |
| Euler's theorem | if GCD(*a*, *n*) = 1, then *a*^φ(*n*) ≡ 1 (mod *n*) | 2⁸ mod 15 = 1 |

Large powers are computed by **square-and-multiply**: for *a*ᵇ, going through the binary form of *b*, the base is squared at each step and multiplied into the result on the 1 bits — ~2048 steps for a 2048-bit exponent, not 2²⁰⁴⁸.

## The Diffie–Hellman key exchange

1. Public parameters: a large prime *p* and a generator *g*.
2. Alice picks a secret *a* and sends *A* = *g*ᵃ mod *p*; Bob sends *B* = *g*ᵇ mod *p* with his secret *b*.
3. Alice computes *s* = *B*ᵃ mod *p*, Bob computes *s* = *A*ᵇ mod *p* — both equal *g*ᵃᵇ mod *p*.

An eavesdropper sees *p*, *g*, *A*, *B*, but to find *s* must solve the **discrete logarithm** problem (finding *a* from *A*) — practically impossible for a large *p*. Important: plain DH does not check that the parties are **genuine** — against the "man in the middle" it is used together with digital signatures or certificates (as in TLS).

## RSA

**Key generation**:

1. Two large primes *p* and *q* are chosen (~1024 bits each).
2. *n* = *p*·*q*, φ(*n*) = (*p* − 1)(*q* − 1).
3. A public exponent *e* is chosen with GCD(*e*, φ(*n*)) = 1 (usually *e* = 65537).
4. The private exponent *d* = *e*⁻¹ mod φ(*n*).
5. The public key is (*e*, *n*), the private key is (*d*, *n*); *p*, *q*, φ(*n*) are destroyed or kept secret.

**Encryption**: *c* = *m*ᵉ mod *n*; **decryption**: *m* = *c*ᵈ mod *n* (correct thanks to Euler's theorem). **Signature**: *s* = *h*ᵈ mod *n* (the message hash with the private key), verification — *s*ᵉ mod *n* = *h*.

**Security** rests on the hardness of splitting *n* into *p* and *q*: whoever knows *p* and *q* can compute φ(*n*) and hence *d*. The current advice is *n* of at least **2048 bits** (3072 bits after 2030). In practice "raw" RSA is not used: the message is **padded** (OAEP for encryption, PSS for signatures), otherwise the same message gives the same ciphertext and mathematical attacks become possible. A program for RSA key generation, encryption and decryption is the task of practical session A7.

## Elliptic curves (ECC)

The security of ECC rests on the discrete logarithm problem over the points of an elliptic curve. Its advantage is the same strength with much shorter keys:

| Symmetric equivalent | RSA / DH | ECC |
|---|---|---|
| 80 bits | 1024 bits | 160 bits |
| 128 bits | 3072 bits | 256 bits |
| 256 bits | 15360 bits | 512 bits |

That is why phones, smart cards and modern protocols prefer ECC (ECDHE, ECDSA, Ed25519).

## Advantages and problems

| Advantages | Problems |
|---|---|
| solves the key distribution problem | hundreds to thousands of times slower than symmetric ciphers |
| digital signatures and non-repudiation | long keys |
| 2*n* keys for *n* users (symmetric needs *n*(*n* − 1)/2) | the authenticity of a public key must be confirmed (PKI, certificates) |
| | quantum computers (Shor's algorithm) could break RSA and ECC — the move to post-quantum algorithms (ML-KEM, ML-DSA) has begun |

## Worked example 1: Diffie–Hellman

Input: *p*, *g*, Alice's secret *a* and Bob's secret *b*. Print the public values *A*, *B* and the shared key computed by each side. Compute powers by square-and-multiply.

```python
def daraja(asos, kor, modul):
    natija = 1
    asos %= modul
    while kor > 0:
        if kor & 1:                               # a 1 bit in the binary form
            natija = natija * asos % modul
        asos = asos * asos % modul                # squaring
        kor >>= 1
    return natija


p, g, a, b = map(int, input().split())
A = daraja(g, a, p)                               # Alice sends
B = daraja(g, b, p)                               # Bob sends
print("A = " + str(A) + ", B = " + str(B))
print("Alice: " + str(daraja(B, a, p)) + ", Bob: " + str(daraja(A, b, p)))
```
```java
import java.util.Scanner;

public class Main {
    static long daraja(long asos, long kor, long modul) {
        long natija = 1;
        asos %= modul;
        while (kor > 0) {
            if ((kor & 1) == 1) {                                  // a 1 bit in the binary form
                natija = natija * asos % modul;
            }
            asos = asos * asos % modul;                            // squaring
            kor >>= 1;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long p = in.nextLong(), g = in.nextLong(), a = in.nextLong(), b = in.nextLong();
        long A = daraja(g, a, p);                                  // Alice sends
        long B = daraja(g, b, p);                                  // Bob sends
        System.out.println("A = " + A + ", B = " + B);
        System.out.println("Alice: " + daraja(B, a, p) + ", Bob: " + daraja(A, b, p));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long daraja(long long asos, long long kor, long long modul) {
    long long natija = 1;
    asos %= modul;
    while (kor > 0) {
        if (kor & 1) {                                             // a 1 bit in the binary form
            natija = natija * asos % modul;
        }
        asos = asos * asos % modul;                                // squaring
        kor >>= 1;
    }
    return natija;
}

int main() {
    long long p, g, a, b;
    cin >> p >> g >> a >> b;
    long long A = daraja(g, a, p);                                 // Alice sends
    long long B = daraja(g, b, p);                                 // Bob sends
    cout << "A = " << A << ", B = " << B << endl;
    cout << "Alice: " << daraja(B, a, p) << ", Bob: " << daraja(A, b, p) << endl;
    return 0;
}
```

Input (the classic textbook example):

```text
23 5 6 15
```

Output:

```text
A = 8, B = 19
Alice: 2, Bob: 2
```

Without ever sending the key, both sides got the same `2`. For a larger number such as *p* = 2147483647 the program also runs instantly (*p* < 2³¹ is taken so that products fit in 64 bits); real systems use *p* of 2048 bits and more, with big-number libraries.

## Worked example 2: breaking a small RSA modulus

If an attacker splits the *n* from a public key into prime factors, they can compute the private key. Split each line's *n* by trial division (up to √*n*): `n = p * q` or `prime`.

```python
import sys

for satr in sys.stdin.read().split():
    n = int(satr)
    d = 2
    topildi = None
    while d * d <= n:                             # up to √n is enough
        if n % d == 0:
            topildi = d
            break
        d += 1
    if topildi is None:
        print(str(n) + ": prime")
    else:
        print(str(n) + " = " + str(topildi) + " * " + str(n // topildi))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long n = in.nextLong();
            long topildi = 0;
            for (long d = 2; d * d <= n; d++) {                    // up to √n is enough
                if (n % d == 0) {
                    topildi = d;
                    break;
                }
            }
            if (topildi == 0) {
                System.out.println(n + ": prime");
            } else {
                System.out.println(n + " = " + topildi + " * " + n / topildi);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long n;
    while (cin >> n) {
        long long topildi = 0;
        for (long long d = 2; d * d <= n; d++) {                   // up to √n is enough
            if (n % d == 0) {
                topildi = d;
                break;
            }
        }
        if (topildi == 0) {
            cout << n << ": prime" << endl;
        } else {
            cout << n << " = " << topildi << " * " << n / topildi << endl;
        }
    }
    return 0;
}
```

Input:

```text
3233
1022117
1000003
999962000357
```

Output:

```text
3233 = 53 * 61
1022117 = 1009 * 1013
1000003: prime
999962000357 = 999979 * 999983
```

Splitting a 12-digit *n* takes ~10⁶ divisions — under a second. Each extra digit multiplies the work by about √10 ≈ 3; even with the best algorithms (the number field sieve), splitting a 2048-bit (617-digit) *n* is impossible today. RSA's security rests on exactly this gap.

:::warning
**Common mistakes**

- **Mixing up the private and public keys.** Encrypt with the recipient's **public** key; sign with the sender's **private** key.
- **Short keys.** 1024-bit RSA is obsolete; use at least 2048 bits or 256-bit ECC.
- **"Textbook" RSA without padding.** The same message gives the same ciphertext and is open to attacks; use OAEP/PSS.
- **Encrypting large data with RSA.** RSA is slow and limited in length; it is only for transporting a symmetric key or for signatures.
- **Using DH without authentication.** A "man in the middle" agrees a separate key with each side and reads the traffic.
:::

:::tip
If OpenSSL is installed (Git Bash has it), create and inspect a real RSA key with `openssl genrsa -out kalit.pem 2048` and `openssl rsa -in kalit.pem -text -noout`: the modulus (*n*), publicExponent (*e* = 65537), privateExponent (*d*), prime1 and prime2 (*p*, *q*). Never show this file to anyone — it is a private key.
:::

## Summary

- Public key cryptography solves the key distribution problem and enables digital signatures; it is based on trapdoor one-way functions.
- Modular arithmetic: exponentiation (square-and-multiply), GCD, inverse, φ(*n*), Euler's theorem.
- Diffie–Hellman: *A* = *g*ᵃ, *B* = *g*ᵇ, the shared *s* = *g*ᵃᵇ mod *p*; security — the discrete logarithm; authentication is needed.
- RSA: *n* = *pq*, *ed* ≡ 1 (mod φ), *c* = *m*ᵉ, *m* = *c*ᵈ mod *n*; security — the hardness of factoring; 2048+ bits and padding.
- ECC gives the same strength with short keys; the quantum threat calls for the move to post-quantum algorithms.

## Check yourself

:::check
Q: Which main problem does public key cryptography solve?
A: Securely agreeing on a secret key between parties who have never met (the key distribution problem).
Q: If p = 11 and q = 13, what are n and φ(n)?
A: n = 143, φ(n) = 10 · 12 = 120.
Q: The hardness of which mathematical problem does RSA's security rest on?
A: Splitting a large number into prime factors (factoring).
Q: What does an eavesdropper see in Diffie–Hellman, and why can they not find the key?
A: p, g, A and B; finding a or b requires solving the discrete logarithm problem.
Q: Why is a 256-bit ECC key considered equal to 3072-bit RSA?
A: No subexponential algorithms are known for the discrete logarithm on elliptic curves, so a shorter key reaches the same strength.
:::

:::link https://csrc.nist.gov/projects/post-quantum-cryptography
NIST Post-Quantum Cryptography
The project standardising algorithms resistant to quantum computers.
:::
