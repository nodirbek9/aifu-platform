---
title: Identification and authentication. Passwords, biometric systems, multi-factor authentication
summary: The difference between identification, authentication and authorization; authentication factors — knowledge, possession, inherence; passwords — entropy, attacks (brute force, dictionary, password spraying, credential stuffing), NIST SP 800-63B recommendations, password managers; one-time passwords (HOTP, TOTP), tokens, smart cards; biometric authentication — FAR, FRR, EER, liveness detection; multi-factor authentication and FIDO2/passkeys; single sign-on (SSO), Kerberos, OAuth 2.0 and OpenID Connect.
---

## Why does this topic matter?

The most common route to unauthorized access is not breaking a weak system but **logging in with someone else's account**: a stolen or guessed password, an SMS code obtained through phishing. In the Verizon DBIR reports a large share of data breaches involve stolen credentials. That is why giving a reliable answer to "who is logging in?" is one of the core tasks of security.

:::note
**After this lesson you will be able to:**

- distinguish identification, authentication and authorization;
- name the authentication factors and their strengths and weaknesses;
- estimate password strength with entropy and know the modern requirements for a password policy;
- compute the errors of a biometric system (FAR, FRR, EER);
- explain the purpose of MFA, FIDO2, SSO and OAuth.
:::

## Basic concepts

| Concept | Question | Example |
|---|---|---|
| **Identification** | Who are you (who do you claim to be)? | login, email, ID card number |
| **Authentication** | How do you prove it? | password, SMS code, fingerprint |
| **Authorization** | What are you allowed to do? | read a file, approve an order (M11) |
| **Accounting** | What did you do? | access logs, audit |

Authentication **factors**:

| Factor | Examples | Weakness |
|---|---|---|
| **Knowledge** (something you know) | password, PIN, secret question | can be guessed, stolen by phishing, reused |
| **Possession** (something you have) | phone, hardware key, smart card, token | can be lost or stolen |
| **Inherence** (something you are) | fingerprint, face, iris, voice | cannot be changed; there is a probability of error |

Additional signals — location, time, device, behaviour (typing rhythm) — are used in **adaptive** authentication to assess the level of risk.

## Passwords

Password strength is measured by its **entropy**: for a password of length *L* chosen at random from *N* symbols, *H* = *L* · log₂ *N* bits. Each extra bit doubles the guessing work. But this formula applies only to **random** passwords: `Parol2024!` is formally complex but appears in dictionaries.

| Attack | Essence | Protection |
|---|---|---|
| **Brute force** | all variants | length, slow hashing (M8), limiting attempts |
| **Dictionary attack** | popular passwords and their variations | checking against lists of breached passwords |
| **Password spraying** | one popular password against many accounts | MFA, anomaly monitoring |
| **Credential stuffing** | login–password pairs leaked from another site | a separate password for each site, MFA |
| **Phishing, keylogger** | obtaining the password from the user | MFA (especially FIDO2), awareness |

Modern recommendations of **NIST SP 800-63B**: at least 8 characters (better 15+), allow up to 64 characters, spaces and any symbols; composition rules ("uppercase + digit + symbol") and **mandatory periodic changes are not required** — they push people toward predictable passwords; instead, passwords are checked against breach lists, attempts are limited, and a password is changed only when there is evidence of compromise. The most practical solution is a **password manager** (a long random password for each site) or a **passphrase** made of several random words.

## One-time passwords and tokens

- **HOTP** (RFC 4226): the code is 6 digits taken from HMAC(key, counter); the counter increases on every use.
- **TOTP** (RFC 6238): time (usually a 30-second interval) instead of a counter — Google Authenticator, Microsoft Authenticator. The key is provided once through a QR code.
- **SMS code**: convenient but the weakest second factor — SIM swap, SS7 vulnerabilities, phishing.
- **Push approval**: "Is this you signing in?" — the "MFA fatigue" attack: the attacker sends requests until the user taps "Yes"; the protection is entering the number shown on screen (number matching).
- **Smart cards and hardware keys** (YubiKey): the private key never leaves the device.

## Biometric authentication

A biometric system compares a sample (fingerprint, face) with a template, computes a **similarity score** and compares it with a **threshold**. There are two kinds of error:

| Metric | Meaning | When the threshold is raised |
|---|---|---|
| **FAR** (False Acceptance Rate) | an impostor was accepted | decreases |
| **FRR** (False Rejection Rate) | the genuine owner was rejected | increases |
| **EER** (Equal Error Rate) | the point where FAR = FRR | — for comparing systems |

For a bank a low FAR matters (do not let an impostor in), for an office door a low FRR (do not keep employees queuing). Biometrics have their own problems: they **cannot be replaced** (a leaked fingerprint is leaked for life), and fake samples (a photo, a silicone finger) exist — hence **liveness detection** is needed; as personal data they are protected by law. Usually biometrics "unlock" a private key on the device and are not sent to a server.

## Multi-factor authentication and FIDO2

**MFA** means at least two **different** factors (password + phone); two passwords are not MFA. According to Microsoft data, MFA stops more than 99% of automated account attacks. However, a phishing site standing "in the middle" can ask for SMS and TOTP codes.

**FIDO2 / WebAuthn (passkeys)** is phishing-resistant: the device creates a separate key pair for each site, and during authentication it signs the server's random challenge with the private key (the digital signature idea from M8). The signature is **bound to the domain name** — it does not work for a fake site; the server stores neither a password nor any secret.

## Single sign-on (SSO) and federation

- **Kerberos** (Windows Active Directory): the user authenticates once and receives a **ticket**; services are shown the ticket instead of the password. The Key Distribution Center (KDC) consists of the AS and the TGS.
- **SAML 2.0**: XML-based SSO for corporate web applications.
- **OAuth 2.0**: an authorization protocol — giving an application a **token** with limited rights without giving it the password ("allow access via Google").
- **OpenID Connect**: an authentication layer on top of OAuth 2.0 — "Sign in with Google"; information about the user is in the ID token (JWT).
- In Uzbekistan, **OneID** is the single identification system for public services (via login and password, a digital signature key, an ID card or a mobile app).

## Worked example 1: password entropy and guessing time

Each line holds the alphabet size *N* and the length *L*. Print the entropy *H* = *L* · log₂ *N* and the average guessing time (half of all variants, *N*^*L* / 2) at 10¹⁰ attempts per second (a GPU farm attacking a fast hash). If the time is less than 1 second, print it in seconds in scientific notation, otherwise in a suitable unit (minutes, hours, days, years).

```python
import math
import sys

TEZLIK = 1e10                                       # attempts per second
BIRLIKLAR = [(365.25 * 86400, "years"), (86400, "days"), (3600, "h"), (60, "min"), (1, "s")]

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    n, l = map(int, satr.split())
    bit = l * math.log2(n)
    vaqt = float(n) ** l / 2 / TEZLIK               # on average, half of the variants
    if vaqt < 1:
        matn = f"{vaqt:.2e} s"
    else:
        for hajm, nom in BIRLIKLAR:                 # the largest fitting unit
            if vaqt >= hajm:
                break
        matn = f"{vaqt / hajm:.1f} {nom}"
    print(f"N={n} L={l}: {bit:.1f} bit, ~{matn}")
```
```java
import java.util.Scanner;

public class Main {
    static final double TEZLIK = 1e10;                         // attempts per second
    static final double[] HAJM = {365.25 * 86400, 86400, 3600, 60, 1};
    static final String[] NOM = {"years", "days", "h", "min", "s"};

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int n = in.nextInt();
            int l = in.nextInt();
            double bit = l * (Math.log(n) / Math.log(2));
            double vaqt = Math.pow(n, l) / 2 / TEZLIK;          // on average, half of the variants
            String matn;
            if (vaqt < 1) {
                matn = String.format("%.2e s", vaqt);
            } else {
                int i = 0;
                while (vaqt < HAJM[i]) i++;                       // the largest fitting unit
                matn = String.format("%.1f %s", vaqt / HAJM[i], NOM[i]);
            }
            System.out.println(String.format("N=%d L=%d: %.1f bit, ~%s", n, l, bit, matn));
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    const double TEZLIK = 1e10;                                  // attempts per second
    const double HAJM[] = {365.25 * 86400, 86400, 3600, 60, 1};
    const char* NOM[] = {"years", "days", "h", "min", "s"};
    int n, l;
    while (cin >> n >> l) {
        double bit = l * log2((double) n);
        double vaqt = pow((double) n, l) / 2 / TEZLIK;           // on average, half of the variants
        char matn[64];
        if (vaqt < 1) {
            snprintf(matn, sizeof matn, "%.2e s", vaqt);
        } else {
            int i = 0;
            while (vaqt < HAJM[i]) i++;                          // the largest fitting unit
            snprintf(matn, sizeof matn, "%.1f %s", vaqt / HAJM[i], NOM[i]);
        }
        printf("N=%d L=%d: %.1f bit, ~%s\n", n, l, bit, matn);
    }
    return 0;
}
```

Input:

```text
10 4
10 6
26 8
62 8
94 12
7776 6
```

Output:

```text
N=10 L=4: 13.3 bit, ~5.00e-07 s
N=10 L=6: 19.9 bit, ~5.00e-05 s
N=26 L=8: 37.6 bit, ~10.4 s
N=62 L=8: 47.6 bit, ~3.0 h
N=94 L=12: 78.7 bit, ~754050.2 years
N=7776 L=6: 77.5 bit, ~350270.5 years
```

4- and 6-digit PINs are recovered from a stolen hash in an instant — they are safe only where the number of attempts is limited (a bank card: 3 attempts). 8 lowercase letters take 10 seconds, 8 letters and digits take 3 hours. A random 12-character password or a phrase of 6 random words (the Diceware list, 7776 words) takes hundreds of thousands of years — and a phrase is much easier to remember. The result is valid **only for random** passwords: a dictionary attack finds human-invented passwords faster. Here only *N* and *L* are given; analyzing the password itself is practical task A10.

## Worked example 2: FAR and FRR in a biometric system

The first line holds the similarity scores of genuine owners, the second the scores of impostors, the third the thresholds to test. A score ≥ threshold is accepted. For each threshold print FAR and FRR, as well as the threshold where they are closest (the point near the EER).

```python
import sys

satrlar = sys.stdin.read().split("\n")
asl = [float(x) for x in satrlar[0].split()]       # scores of genuine owners
yot = [float(x) for x in satrlar[1].split()]       # scores of impostors
eng, farq = None, 2.0
for t in [float(x) for x in satrlar[2].split()]:
    far = sum(1 for x in yot if x >= t) / len(yot)   # impostor accepted
    frr = sum(1 for x in asl if x < t) / len(asl)    # owner rejected
    print(f"t={t:.2f} FAR={100 * far:.1f}% FRR={100 * frr:.1f}%")
    if abs(far - frr) < farq:
        eng, farq = t, abs(far - frr)
print(f"EER ~ t={eng:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static double[] oqi(String satr) {
        String[] q = satr.trim().split("\\s+");
        double[] a = new double[q.length];
        for (int i = 0; i < q.length; i++) a[i] = Double.parseDouble(q[i]);
        return a;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double[] asl = oqi(in.nextLine());                 // scores of genuine owners
        double[] yot = oqi(in.nextLine());                 // scores of impostors
        double[] chegaralar = oqi(in.nextLine());
        double eng = 0, farq = 2.0;
        for (double t : chegaralar) {
            int qabul = 0, rad = 0;
            for (double x : yot) if (x >= t) qabul++;      // impostor accepted
            for (double x : asl) if (x < t) rad++;         // owner rejected
            double far = (double) qabul / yot.length;
            double frr = (double) rad / asl.length;
            System.out.println(String.format("t=%.2f FAR=%.1f%% FRR=%.1f%%", t, 100 * far, 100 * frr));
            if (Math.abs(far - frr) < farq) {
                eng = t;
                farq = Math.abs(far - frr);
            }
        }
        System.out.println(String.format("EER ~ t=%.2f", eng));
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<double> oqi(const string& satr) {
    istringstream ss(satr);
    vector<double> a;
    double x;
    while (ss >> x) a.push_back(x);
    return a;
}

int main() {
    string s1, s2, s3;
    getline(cin, s1);
    getline(cin, s2);
    getline(cin, s3);
    vector<double> asl = oqi(s1);                          // scores of genuine owners
    vector<double> yot = oqi(s2);                          // scores of impostors
    double eng = 0, farq = 2.0;
    for (double t : oqi(s3)) {
        int qabul = 0, rad = 0;
        for (double x : yot) if (x >= t) qabul++;          // impostor accepted
        for (double x : asl) if (x < t) rad++;             // owner rejected
        double far = (double) qabul / yot.size();
        double frr = (double) rad / asl.size();
        printf("t=%.2f FAR=%.1f%% FRR=%.1f%%\n", t, 100 * far, 100 * frr);
        if (fabs(far - frr) < farq) {
            eng = t;
            farq = fabs(far - frr);
        }
    }
    printf("EER ~ t=%.2f\n", eng);
    return 0;
}
```

Input:

```text
0.91 0.85 0.78 0.95 0.74 0.88 0.72 0.93 0.81 0.58
0.12 0.35 0.48 0.22 0.67 0.31 0.54 0.09 0.41 0.73
0.50 0.60 0.70 0.80 0.90
```

Output:

```text
t=0.50 FAR=30.0% FRR=0.0%
t=0.60 FAR=20.0% FRR=10.0%
t=0.70 FAR=10.0% FRR=10.0%
t=0.80 FAR=0.0% FRR=40.0%
t=0.90 FAR=0.0% FRR=70.0%
EER ~ t=0.70
```

As the threshold rises, FAR falls and FRR grows — both cannot be brought to zero at the same time. At *t* = 0.80 impostors are stopped completely, but two owners out of every five are rejected. Real systems are tested on thousands of samples; for unlocking phones with a fingerprint or a face, manufacturers state a FAR of roughly 1/50,000 to 1/1,000,000.

:::warning
**Common mistakes**

- **Treating identification as authentication.** A login or an ID number is not a secret; knowing it does not prove identity.
- **Using one password on many sites.** If one site leaks, credential stuffing opens all the accounts.
- **A "complex but short" password.** A long phrase such as `uzum-tog-kitob-sariq` is much stronger than `P@ss1!`.
- **Approving an SMS code or a push request without thinking.** If you did not start the login — reject it and change your password.
- **Thinking biometrics can be "changed" like a password.** A leaked fingerprint cannot be replaced; biometric templates must be encrypted and kept on the device.
:::

:::tip
Check at haveibeenpwned.com whether your email appears in known breaches. Turn on MFA for your important accounts (email, bank, public services), and where possible use a passkey or an authenticator app rather than SMS.
:::

## Summary

- Identification is claiming an identity, authentication is proving it, authorization is determining permissions.
- Factors: knowledge, possession, inherence; MFA is at least two different factors.
- Password strength is entropy (*L* · log₂ *N*); length and randomness matter; NIST: breach lists, limits and a password manager instead of forced complexity and periodic changes.
- OTP (HOTP, TOTP), push, hardware keys; FIDO2/passkeys are phishing-resistant.
- Biometrics: FAR and FRR depend on the threshold in opposite ways, the EER is the comparison point; liveness detection is required.
- SSO: Kerberos, SAML, OAuth 2.0 (authorization), OpenID Connect (authentication), OneID.

## Check yourself

:::check
Q: What is the difference between authentication and authorization?
A: Authentication checks that the user is who they claim to be; authorization determines which actions they are allowed to perform.
Q: Are a password and a secret question together MFA?
A: No, both are knowledge factors; MFA needs different factors.
Q: What is the entropy of a random password of 8 lowercase Latin letters?
A: 8 · log₂ 26 ≈ 37.6 bits.
Q: How do FAR and FRR change when the threshold of a biometric system is raised?
A: FAR decreases (fewer impostors are accepted), FRR increases (more owners are rejected).
Q: Why are FIDO2/passkeys phishing-resistant?
A: The signature is bound to the site's domain and the private key never leaves the device — a fake site cannot obtain a valid signature.
:::

:::link https://pages.nist.gov/800-63-4/sp800-63b.html
NIST SP 800-63B: Authentication and Authenticator Management
Digital identity guidelines on passwords, OTP, biometrics and MFA.
:::
