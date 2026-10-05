---
title: Checking an SSL/TLS certificate (using a browser)
summary: Checking a site certificate in a browser in practice — the padlock icon, the certificate viewer, the owner (Subject, SAN), the issuing CA, the validity period, the fingerprint, the certificate chain; DV, OV and EV certificates; error cases on badssl.com — expired, wrong-name, self-signed and revoked certificates; the SSL Labs and crt.sh tools; HSTS; checking certificate validity and name matching in a program; a test on the topic.
---

## Why does this topic matter?

Behind the padlock icon in the browser stands the whole mechanism studied in M8 and M9: the X.509 certificate, the CA signature, the chain of trust and the TLS handshake. For a user the most important skill is to **understand** a certificate warning and not ignore it; for an administrator — to renew a certificate before it expires. In this session we examine the certificates of real sites and of deliberately broken test sites.

:::note
**After this session you will be able to:**

- view and analyze certificate fields and the chain in a browser;
- tell the difference between DV, OV and EV certificates;
- recognize browser certificate errors and explain their causes;
- check certificate validity and domain name matching in a program.
:::

## Practical work

**Step 1. Viewing a certificate.** In Chrome or Edge open any HTTPS site (for example my.gov.uz or google.com) → the icon in the address bar → "Connection is secure" → "Certificate is valid". In Firefox: padlock → "Connection secure" → "More information" → "View Certificate". Write down the following:

| Field | What to check |
|---|---|
| **Issued To** (Subject, CN) | the site name or organization |
| **Subject Alternative Name** | all domains the certificate is valid for, including wildcard names starting with `*.` |
| **Issued By** (Issuer) | the issuing CA (for example Let's Encrypt, DigiCert, GlobalSign) |
| **Validity** | the Not Before and Not After dates |
| **Public Key** | algorithm and length (RSA 2048, ECDSA P-256) |
| **Fingerprint** (SHA-256) | the certificate's unique fingerprint |

On the "Details" or "Certification Path" tab look at the **chain**: site certificate → intermediate CA → root CA. The root certificate is in the trusted list of the OS or the browser (on Windows: `certmgr.msc` → Trusted Root Certification Authorities).

**Step 2. Certificate types.** Compare several sites:

| Type | What is verified | Where |
|---|---|---|
| **DV** (Domain Validation) | only domain ownership (automatic, in minutes) | blogs, most sites; Let's Encrypt issues them free |
| **OV** (Organization Validation) | domain + organization documents | company sites |
| **EV** (Extended Validation) | an in-depth legal check | formerly banks; browsers no longer show a separate green indicator |

Important: a DV certificate only says "you are on an encrypted channel with this domain", not "the site is trustworthy". Most phishing sites use HTTPS too.

**Step 3. Error cases (badssl.com).** Open the following test sites and write down what the browser says for each (do **not** proceed to the page — just read the warning):

| Site | Problem | Browser message (Chrome) |
|---|---|---|
| expired.badssl.com | expired | NET::ERR_CERT_DATE_INVALID |
| wrong.host.badssl.com | domain name does not match the SAN | NET::ERR_CERT_COMMON_NAME_INVALID |
| self-signed.badssl.com | self-signed | NET::ERR_CERT_AUTHORITY_INVALID |
| untrusted-root.badssl.com | root CA not in the trusted list | NET::ERR_CERT_AUTHORITY_INVALID |
| revoked.badssl.com | certificate revoked | NET::ERR_CERT_REVOKED (if the browser checks revocation) |

If such a message appears on a real site, it may be a man-in-the-middle attack, a misconfigured server or a wrong date on the computer. Do **not** enter passwords or card details.

**Step 4. Server analysis.** Check a site at ssllabs.com/ssltest: the overall grade (A+…F), supported TLS versions, cipher suites (M9), chain completeness, **HSTS** (a header forcing the browser to always use HTTPS). Search for a domain name at crt.sh — the **Certificate Transparency** logs show every certificate issued for that domain; a certificate issued by an unexpected CA is detected immediately.

**Step 5. Test.** Complete the "SSL/TLS and certificates" test on the platform.

## Worked example 1: monitoring certificate expiry

An expired certificate is the most common and most "embarrassing" failure: the site stops working overnight. Certificate lifetimes keep shrinking: Let's Encrypt — 90 days; under a CA/Browser Forum decision the general maximum is 200 days from March 2026 and 47 days by 2029. That is why monitoring is automated. Each line holds a domain, Not Before, Not After and today's date (YYYY-MM-DD). Print the status: not yet valid, expired, less than 30 days left (needs renewal) or valid.

```python
import sys


def kunlar(sana):                                  # number of days since 1970-01-01
    y, m, d = map(int, sana.split("-"))
    y -= m <= 2                                    # treat the year as starting in March
    era = y // 400
    yoe = y - era * 400
    doy = (153 * (m + (-3 if m > 2 else 9)) + 2) // 5 + d - 1
    doe = yoe * 365 + yoe // 4 - yoe // 100 + doy  # leap years are taken into account
    return era * 146097 + doe - 719468


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    domen, boshi, oxiri, bugun = satr.split()
    b, o, t = kunlar(boshi), kunlar(oxiri), kunlar(bugun)
    if t < b:
        holat = "not yet valid"
    elif t > o:
        holat = f"expired ({t - o} days ago)"
    elif o - t < 30:
        holat = f"renew: {o - t} days left"
    else:
        holat = f"valid, {o - t} days left"
    print(f"{domen}: {holat}")
```
```java
import java.util.Scanner;

public class Main {
    static long kunlar(String sana) {                     // number of days since 1970-01-01
        String[] q = sana.split("-");
        long y = Long.parseLong(q[0]), m = Long.parseLong(q[1]), d = Long.parseLong(q[2]);
        if (m <= 2) y--;                                  // treat the year as starting in March
        long era = y / 400;
        long yoe = y - era * 400;
        long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy; // leap years are taken into account
        return era * 146097 + doe - 719468;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String domen = in.next();
            long b = kunlar(in.next()), o = kunlar(in.next()), t = kunlar(in.next());
            String holat;
            if (t < b) holat = "not yet valid";
            else if (t > o) holat = "expired (" + (t - o) + " days ago)";
            else if (o - t < 30) holat = "renew: " + (o - t) + " days left";
            else holat = "valid, " + (o - t) + " days left";
            System.out.println(domen + ": " + holat);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

long long kunlar(const string& sana) {                    // number of days since 1970-01-01
    long long y, m, d;
    sscanf(sana.c_str(), "%lld-%lld-%lld", &y, &m, &d);
    if (m <= 2) y--;                                      // treat the year as starting in March
    long long era = y / 400;
    long long yoe = y - era * 400;
    long long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    long long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;   // leap years are taken into account
    return era * 146097 + doe - 719468;
}

int main() {
    string domen, boshi, oxiri, bugun;
    while (cin >> domen >> boshi >> oxiri >> bugun) {
        long long b = kunlar(boshi), o = kunlar(oxiri), t = kunlar(bugun);
        string holat;
        if (t < b) holat = "not yet valid";
        else if (t > o) holat = "expired (" + to_string(t - o) + " days ago)";
        else if (o - t < 30) holat = "renew: " + to_string(o - t) + " days left";
        else holat = "valid, " + to_string(o - t) + " days left";
        cout << domen << ": " << holat << endl;
    }
    return 0;
}
```

Input:

```text
my.gov.uz 2026-03-01 2027-03-01 2026-10-05
bank.uz 2025-10-20 2026-10-20 2026-10-05
old.uz 2024-01-01 2026-09-25 2026-10-05
new.uz 2026-11-01 2027-11-01 2026-10-05
leap.uz 2024-01-15 2024-03-01 2024-02-28
```

Output:

```text
my.gov.uz: valid, 147 days left
bank.uz: renew: 15 days left
old.uz: expired (10 days ago)
new.uz: not yet valid
leap.uz: renew: 2 days left
```

Converting dates to a day count correctly accounts for month lengths and leap years: from 2024-02-28 to 2024-03-01 is 2 days (there is a February 29). The browser compares in exactly the same way — that is why, if the computer's date is wrong, every site looks "expired". In practice monitoring tools (for example certbot with Let's Encrypt) renew a certificate automatically 30 days before it expires.

## Worked example 2: matching a domain name against the SAN

The browser compares the name in the address bar with the certificate's SAN list (the rules of RFC 6125). Letter case does not matter; the wildcard `*.` may only stand in place of the **single leftmost label** and matches exactly one label: `*.example.uz` matches `www.example.uz`, but neither `example.uz` nor `a.b.example.uz`. The first line holds the SAN list, the following lines the names to check. Print the matching SAN entry or `no match`.

```python
import sys


def mos(naqsh, nom):
    naqsh, nom = naqsh.lower(), nom.lower()        # DNS names are case-insensitive
    if not naqsh.startswith("*."):
        return naqsh == nom
    qism = nom.split(".", 1)                       # the left label and the rest
    return len(qism) == 2 and qism[0] != "" and qism[1] == naqsh[2:]


satrlar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
san = satrlar[0].split()
for nom in satrlar[1:]:
    topildi = next((n for n in san if mos(n, nom)), None)
    print(f"{nom} -> " + (f"match ({topildi})" if topildi else "no match"))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean mos(String naqsh, String nom) {
        naqsh = naqsh.toLowerCase();                       // DNS names are case-insensitive
        nom = nom.toLowerCase();
        if (!naqsh.startsWith("*.")) return naqsh.equals(nom);
        int nuqta = nom.indexOf('.');                      // the left label and the rest
        return nuqta > 0 && nom.substring(nuqta + 1).equals(naqsh.substring(2));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s);
        }
        String[] san = satrlar.get(0).split("\\s+");
        for (int i = 1; i < satrlar.size(); i++) {
            String nom = satrlar.get(i);
            String topildi = null;
            for (String n : san) {
                if (mos(n, nom)) {
                    topildi = n;
                    break;
                }
            }
            System.out.println(nom + " -> " + (topildi != null ? "match (" + topildi + ")" : "no match"));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

string kichik(string s) {
    transform(s.begin(), s.end(), s.begin(), [](unsigned char c) { return tolower(c); });
    return s;
}

bool mos(string naqsh, string nom) {
    naqsh = kichik(naqsh);                                 // DNS names are case-insensitive
    nom = kichik(nom);
    if (naqsh.rfind("*.", 0) != 0) return naqsh == nom;
    size_t nuqta = nom.find('.');                          // the left label and the rest
    return nuqta != string::npos && nuqta > 0 && nom.substr(nuqta + 1) == naqsh.substr(2);
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream ss(satr);
    vector<string> san;
    string n;
    while (ss >> n) san.push_back(n);
    string nom;
    while (cin >> nom) {
        string topildi;
        for (const string& x : san) {
            if (mos(x, nom)) {
                topildi = x;
                break;
            }
        }
        cout << nom << " -> " << (!topildi.empty() ? "match (" + topildi + ")" : "no match") << endl;
    }
    return 0;
}
```

Input:

```text
example.uz *.example.uz mail.test.uz
example.uz
www.example.uz
WWW.Example.UZ
a.b.example.uz
test.uz
mail.test.uz
example.uz.evil.com
```

Output:

```text
example.uz -> match (example.uz)
www.example.uz -> match (*.example.uz)
WWW.Example.UZ -> match (*.example.uz)
a.b.example.uz -> no match
test.uz -> no match
mail.test.uz -> match (mail.test.uz)
example.uz.evil.com -> no match
```

`example.uz.evil.com` is a favourite trick of phishing sites: the name starts with "example.uz", but it is actually the `evil.com` domain. The browser checks the name from the right, in full, and rejects such a name — a user should read the address the same way. The wrong.host.badssl.com error is exactly the result of this check.

:::warning
**Common mistakes**

- **Clicking "Proceed" on a certificate warning.** Especially on public Wi-Fi — it is a classic sign of a man-in-the-middle attack.
- **"There is a padlock, so the site is safe".** The padlock only means the channel is encrypted; check the domain name.
- **A wrong computer date.** If every site shows a certificate error, check the clock and date first.
- **Tracking certificate expiry by hand.** Set up automatic renewal (ACME, certbot) and expiry monitoring.
- **An incomplete chain.** If the server does not send the intermediate CA certificate, some devices show an error; SSL Labs reveals this.
:::

:::tip
Viewing a certificate in a terminal: `openssl s_client -connect my.gov.uz:443 -servername my.gov.uz </dev/null | openssl x509 -noout -subject -issuer -dates -ext subjectAltName`. This command prints the Subject, Issuer, validity dates and the SAN list.
:::

## Summary

- In a certificate, check: Subject and SAN (domain), Issuer (CA), Validity (dates), the key, the fingerprint and the chain of trust.
- DV verifies only domain ownership; OV and EV verify the organization as well; HTTPS does not guarantee that a site is trustworthy.
- Browser errors: expired, name mismatch, untrusted CA, revoked — do not enter any data.
- SSL Labs analyzes server configuration; crt.sh shows Certificate Transparency logs; HSTS enforces HTTPS.
- Validity and name matching can be checked by a program; a wildcard matches only one leftmost label.

## Check yourself

:::check
Q: Which certificate field shows the domains it is valid for?
A: Subject Alternative Name (SAN).
Q: Is a *.example.uz certificate valid for example.uz and a.b.example.uz?
A: No: a wildcard matches exactly one leftmost label — it matches www.example.uz, but not example.uz or a.b.example.uz.
Q: If every site suddenly shows an "expired" error, what is the likely cause?
A: The date or time on the computer is wrong.
Q: Does a DV certificate guarantee that the site belongs to a trustworthy organization?
A: No, DV only confirms control over the domain; phishing sites get DV certificates too.
:::

:::link https://badssl.com/
badssl.com
Deliberately misconfigured test sites for safely trying out certificate and TLS errors.
:::
