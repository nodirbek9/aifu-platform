---
title: Fingerprint-based authentication. The digital signature
summary: Fingerprint biometrics — ridge pattern types (arch, loop, whorl), minutiae; scanners — optical, capacitive, ultrasonic; enrollment and matching steps; template, similarity score and threshold, FAR and FRR; liveness detection and fake fingers; protecting templates (on the device, in a secure enclave); fingerprints and digital signatures working together — biometrics unlocks the private key; legal aspects of digital signatures and their use in Uzbekistan; a program comparing binary templates with the Hamming distance.
---

## Why does this topic matter?

The fingerprint is the most widespread biometric trait: it is used in phones, laptops, banking apps, access control systems and passport control. A digital signature gives an electronic document legal force (M8). The two technologies often work together: you put your finger on the scanner, and the device signs a request with the private key inside it (FIDO2 from M10). Knowing how they work and their limitations is a condition for applying them correctly.

:::note
**After this assignment you will be able to:**

- explain which features of a fingerprint are used for matching;
- distinguish scanner types and their resistance to fake fingers;
- apply the concepts of template, similarity score, threshold, FAR and FRR;
- explain how biometrics and digital signatures work together;
- write a program comparing binary templates with the Hamming distance.
:::

## Fingerprint features

The pattern of **friction ridges** on the skin of a finger forms before birth and does not change during life; it differs even between twins.

| Level | Features | Use |
|---|---|---|
| Level 1 (overall pattern) | arch, loop, whorl | classification, fast database search |
| Level 2 (**minutiae**) | ridge endings, ridge bifurcations, their position and direction | the main matching method |
| Level 3 | sweat pores, ridge width | high-precision forensic analysis |

## Scanners

| Type | Principle | Features |
|---|---|---|
| **Optical** | capturing an image of the finger with a camera | cheap; more vulnerable to simple fakes |
| **Capacitive** | the difference in electrical capacitance between ridges and valleys | widespread in phones and laptops |
| **Ultrasonic** | an ultrasound wave into the skin and the returned signal | a 3D image, works even with a dirty or wet finger |
| **Thermal and others** | temperature differences | less common |

## Enrollment and matching

1. **Enrollment**: the finger is scanned several times, minutiae are extracted from the image and a **template** is built. Not the image itself, only the template is stored.
2. **Matching**: a template is built from the new sample and compared with the stored one → a **similarity score** (or a difference) → compared with a **threshold**.
3. **Verification** (1:1 — "is this Ali?") and **identification** (1:N — "who is this?", for example in a forensic database) differ; in 1:N the probability of error grows with the size of the database.

The errors are as in M10: **FAR** — an impostor accepted, **FRR** — the owner rejected; the threshold balances them. Two scans are never exactly identical (pressure, angle, moisture) — that is why closeness is checked, not exact equality.

## Security issues

- **Fake fingers** (silicone, gelatin, 3D-printed copies) — the protection is **liveness detection** (blood flow, sweat, skin layers, ultrasonic 3D imaging).
- **Irreplaceability**: a leaked password is replaced, a leaked fingerprint is leaked for life. That is why templates are stored not on a central server but encrypted in the device's **secure enclave** (Secure Enclave, TPM, TEE) and never leave it.
- **Coercion**: forcing someone to put a finger on a scanner is easier than forcing them to reveal a password — in very important systems biometrics are combined with a PIN.
- **Personal data**: biometric data is protected by the Law "On Personal Data"; collecting it requires consent and a legal basis.

## Fingerprints and digital signatures

In modern devices the fingerprint is **not the digital signature itself** — it **unlocks access to the private key** on the device:

1. A key pair is created on the device; the private key never leaves the secure enclave.
2. When a signature or login is requested, the user confirms with a fingerprint.
3. The enclave signs the hash of the document or request with the private key; the server verifies it with the public key (M8).

This way the biometric data never goes on the network at all, while the signature is cryptographically strong. In Uzbekistan a digital signature key is stored in a file or on the ID card chip and protected by a PIN/password; by law a digital signature is equivalent to a handwritten one, and certificates are issued by authorized centres (M8, A8).

## Worked example: comparing templates with the Hamming distance

Some biometric systems (for example iris codes and some fingerprint templates) store features as a **binary vector** and compare them with the **normalized Hamming distance**: HD = number of differing bits / number of bits. For random impostor samples HD ≈ 0.5; for samples of the same person it is much smaller. The first line holds the threshold, the second the stored template, the following lines "name sample". If HD ≤ threshold — `match`.

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
chegara = float(satrlar[0][0])
shablon = satrlar[1][0]
for nom, namuna in satrlar[2:]:
    farq = sum(1 for a, b in zip(shablon, namuna) if a != b)   # differing bits
    hd = farq / len(shablon)                                    # normalized Hamming distance
    print(f"{nom}: HD={hd:.3f} -> " + ("match" if hd <= chegara else "no match"))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double chegara = Double.parseDouble(in.next());
        String shablon = in.next();
        while (in.hasNext()) {
            String nom = in.next(), namuna = in.next();
            int farq = 0;
            for (int i = 0; i < shablon.length(); i++) {
                if (shablon.charAt(i) != namuna.charAt(i)) farq++;   // differing bits
            }
            double hd = (double) farq / shablon.length();             // normalized Hamming distance
            System.out.println(String.format("%s: HD=%.3f -> %s", nom, hd, hd <= chegara ? "match" : "no match"));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    double chegara;
    string shablon, nom, namuna;
    cin >> chegara >> shablon;
    while (cin >> nom >> namuna) {
        int farq = 0;
        for (size_t i = 0; i < shablon.size(); i++) {
            if (shablon[i] != namuna[i]) farq++;                     // differing bits
        }
        double hd = (double) farq / shablon.size();                  // normalized Hamming distance
        printf("%s: HD=%.3f -> %s\n", nom.c_str(), hd, hd <= chegara ? "match" : "no match");
    }
    return 0;
}
```

Input:

```text
0.25
1011001110001011110010100110101100011101
egasi_1 1011001110001011110010100110101100011101
egasi_2 1011011110001001110010100100101100011111
egasi_3 1001001010001011100010110110101000011101
begona_1 0000011100011101100000000011111001010110
begona_2 1100011011010110101001110010110101101001
```

Output:

```text
egasi_1: HD=0.000 -> match
egasi_2: HD=0.100 -> match
egasi_3: HD=0.125 -> match
begona_1: HD=0.475 -> no match
begona_2: HD=0.550 -> no match
```

(egasi means owner, begona means stranger.) The owner's samples differed by 10–12.5% (because of pressure and angle), the impostors' by about half, just like random bits. Note: `egasi_1` is **exactly identical** to the stored template (HD = 0). In real scanning this almost never happens — such a result may be a sign of replaying a previously captured template, which is why some systems treat a very small difference as suspicious too. If we lower the threshold below 0.25, FRR grows; if we raise it — FAR.

:::warning
**Common mistakes**

- **Treating a fingerprint as a "password".** It is not secret — we leave it on everything we touch; it is an identifier and a convenience factor, not a secret.
- **Storing biometric images on a server.** Only protected templates, and on the device where possible.
- **Requiring exact equality.** Biometrics are always approximate; a threshold and a FAR/FRR balance are needed.
- **A cheap scanner without liveness detection.** It is easy to fool with a fake finger.
- **Keeping a digital signature key without a PIN.** If the device or file is stolen, signatures can be forged.
:::

:::tip
When setting up fingerprint unlock on your phone, scan the same finger many times at different angles — this lowers FRR. For important apps (banking) turn on the combination of biometrics + PIN or the device password.
:::

## Summary

- Fingerprints are matched by minutiae; a template, not the image, is stored.
- Scanners: optical, capacitive, ultrasonic; liveness detection against fake fingers.
- Matching is approximate: similarity score, threshold, FAR and FRR; 1:1 verification and 1:N identification.
- Templates live in the device's secure enclave; biometrics cannot be replaced.
- A fingerprint unlocks the private key, while the signature is produced by cryptography (the digital signature).

## Check yourself

:::check
Q: Which features are mainly used for fingerprint matching?
A: Minutiae — ridge endings and bifurcations, their position and direction.
Q: Why are biometric templates stored in the device's secure enclave?
A: Biometrics cannot be replaced; a leak from a central database creates a lifelong risk.
Q: In a digital signature confirmed by a fingerprint, what actually creates the signature?
A: The private key on the device; the fingerprint only unlocks access to it.
Q: Why can a sample with a Hamming distance of 0 be suspicious?
A: Real scans always differ slightly; an identical sample may be a replayed template.
:::

:::link https://pages.nist.gov/800-63-4/sp800-63b.html
NIST SP 800-63B
Requirements for using biometrics in authentication (liveness detection, FAR, on-device storage).
:::
