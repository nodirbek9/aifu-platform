---
title: Methods of ensuring information security
summary: The concept of information security and the triad — confidentiality, integrity, availability; sources of threats (human error, fraud, malware, failures, natural disasters); protection methods — legal, organizational, technical and software, physical; authentication and access rights, backups (the 3-2-1 rule), updates; security policy and staff training; economic risk assessment — ALE and ROSI; information security legislation in Uzbekistan; a risk calculation program.
---

## Why does this topic matter?

For a company, data is an asset equal to money: the customer base, accounting, contracts, bank accounts. One employee trusting a fraudulent email, a server left without a backup or an unpatched program can cause the company great losses. The question for an economist is: **which protective measures are worth the money?**

:::note
**After this lesson you will be able to:**

- explain the three core properties of information security;
- name the sources of threats and the groups of protection methods;
- know the key protective measures — authentication, access rights, backups, updates;
- assess risks economically through ALE and ROSI.
:::

## The concept and the triad

**Information security** is the state of, and measures for, protecting information and the systems that process it from unauthorized access, modification, loss and disruption. Its three core properties (the **CIA triad**):

| Property | Meaning | Example of a breach |
|---|---|---|
| **Confidentiality** | only authorized people see the data | a leak of the customer base |
| **Integrity** | the data has not been changed without authorization | replacing the bank details on an invoice |
| **Availability** | data and services work when needed | an online shop stopping because of a server failure |

## Sources of threats

| Source | Examples |
|---|---|
| **Human error** | deleting the wrong file, sending an email to the wrong recipient, leaving a password on a sticky note |
| **Fraud and social engineering** | fake emails and pages (phishing), requests to transfer money "on behalf of the boss", asking for an SMS code |
| **Malware** | viruses, programs that encrypt files and demand a ransom (ransomware), spyware |
| **Insider threat** | an employee taking data out or misusing it |
| **Technical failures** | a disk failure, a power cut, a software bug |
| **Natural and physical** | fire, flooding, theft of equipment |

:::tip
Many incidents start not with a sophisticated technical attack but with simple human error and fraud. That is why training staff is one of the cheapest and most effective measures.
:::

## Protection methods

| Group | Measures |
|---|---|
| **Legal** | laws, confidentiality clauses in contracts, liability |
| **Organizational** | a security policy, roles and owners, staff training, an incident response plan, closing a departing employee's account immediately |
| **Technical and software** | authentication, access rights, encryption (S14), antivirus, a firewall, updates, backups, logs |
| **Physical** | restricting access to the server room, CCTV cameras, an uninterruptible power supply (UPS), fire safety |

## Key technical measures

- **Authentication** — verifying a user's identity. Long and unique passwords (with a password manager), **multi-factor authentication (MFA)**: a password plus a phone code or an app confirmation.
- **Access rights** — the **least privilege** principle: each employee sees only the data needed for their work (a cashier does not see accounting reports).
- **Updates** — known flaws in operating systems and programs are fixed by updates; an outdated system is an open door.
- **Backups** — the **3-2-1 rule**: 3 copies of the data, on 2 different media, 1 of them in another location (for example, in the cloud or offline). Restoring from a backup must be tested regularly.
- **Encryption** — laptop disks, backups and network traffic (HTTPS) are encrypted.
- **Logs and monitoring** — recording who accessed what and when, detecting suspicious events.

## Economic risk assessment

**Risk** = probability of an incident × damage. Metrics widely used in economic assessment:

| Metric | Formula | Meaning |
|---|---|---|
| **SLE** (single loss expectancy) | asset value × share of damage | how much one incident costs |
| **ARO** (annual rate of occurrence) | how many times a year it is expected | 0.5 = once every two years |
| **ALE** (annual loss expectancy) | SLE × ARO | how much we lose in an average year |
| **ROSI** (return on security investment) | (reduction in ALE − cost of the measure) / cost of the measure × 100% | whether the measure pays off |

## Worked example: choosing protective measures

A company is considering measures against four threats. Each line holds: the threat, the loss from one incident (SLE, soums), the frequency before and after the measure (**times per 10 years**), the annual cost of the measure. For each threat compute the ALE before and after, the annual benefit and the ROSI; if ROSI is positive, implement the measure, otherwise reconsider it.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, sle, oldin, keyin, narx = satr.split()
    sle, oldin, keyin, narx = int(sle), int(oldin), int(keyin), int(narx)
    ale_oldin = sle * oldin // 10                     # annual loss expectancy
    ale_keyin = sle * keyin // 10
    foyda = ale_oldin - ale_keyin                     # loss avoided per year
    rosi = (foyda - narx) * 100 // narx               # in percent
    qaror = "implement" if rosi > 0 else "reconsider"
    print(f"{nom}: ALE {ale_oldin} -> {ale_keyin}, benefit {foyda}, ROSI {rosi}%, {qaror}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            long sle = in.nextLong(), oldin = in.nextLong(), keyin = in.nextLong(), narx = in.nextLong();
            long aleOldin = sle * oldin / 10;                  // annual loss expectancy
            long aleKeyin = sle * keyin / 10;
            long foyda = aleOldin - aleKeyin;                  // loss avoided per year
            long rosi = (foyda - narx) * 100 / narx;           // in percent
            String qaror = rosi > 0 ? "implement" : "reconsider";
            System.out.println(nom + ": ALE " + aleOldin + " -> " + aleKeyin + ", benefit " + foyda + ", ROSI " + rosi + "%, " + qaror);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string nom;
    long long sle, oldin, keyin, narx;
    while (cin >> nom >> sle >> oldin >> keyin >> narx) {
        long long aleOldin = sle * oldin / 10;                 // annual loss expectancy
        long long aleKeyin = sle * keyin / 10;
        long long foyda = aleOldin - aleKeyin;                 // loss avoided per year
        long long rosi = (foyda - narx) * 100 / narx;          // in percent
        string qaror = rosi > 0 ? "implement" : "reconsider";
        cout << nom << ": ALE " << aleOldin << " -> " << aleKeyin << ", benefit " << foyda
             << ", ROSI " << rosi << "%, " << qaror << "\n";
    }
    return 0;
}
```

Input:

```text
phishing 20000000 30 6 30000000
ransomware 300000000 2 1 40000000
outage 50000000 10 4 12000000
laptop 15000000 8 2 10000000
```

Output:

```text
phishing: ALE 60000000 -> 12000000, benefit 48000000, ROSI 60%, implement
ransomware: ALE 60000000 -> 30000000, benefit 30000000, ROSI -25%, reconsider
outage: ALE 50000000 -> 20000000, benefit 30000000, ROSI 150%, implement
laptop: ALE 12000000 -> 3000000, benefit 9000000, ROSI -10%, reconsider
```

Anti-phishing training for staff (ROSI 60%) and a backup power supply (150%) clearly pay off. The measure chosen against ransomware is negative on paper — but be careful: ALE describes an "average" year, while a single incident costs 300 million soums, which could stop the company. For such rare but catastrophic risks you should look for a cheaper measure (for example, 3-2-1 backups) or insure the risk. For the laptops the calculation can be redone with a cheaper solution — disk encryption.

## Risk response strategies

| Strategy | Meaning | Example |
|---|---|---|
| **Mitigate** | introduce a protective measure | MFA, backups |
| **Transfer** | pass the risk to someone else | cyber insurance, outsourcing a service to a provider |
| **Accept** | the damage is small and the measure expensive | an old printer breaking down |
| **Avoid** | give up the risky activity | not collecting data you do not need |

## Security policy and legislation

A **security policy** is an organization's written rules: passwords, devices, data categories and access to them, backups, whom to notify about incidents. The international standard is **ISO/IEC 27001** (information security management systems).

**In Uzbekistan**: the laws "On Informatization", "On Personal Data" and "On Cybersecurity"; an authorized body for cybersecurity and a cyber incident response centre operate. An organization is liable for leaks of personal data.

:::warning
**Common mistakes**

- **Thinking "we have nothing to steal".** Small companies also become targets of phishing and ransomware.
- **Not testing backups.** A backup that cannot be restored is not a backup.
- **Giving everyone administrator rights.** One mistake or one compromised account affects the whole system.
- **Relying only on technical measures.** An untrained employee can bypass even the best protection.
- **Assessing risks with ALE alone.** Consider rare but catastrophic incidents separately.
:::

:::tip
For the "laptop" threat in the example, how does a disk encryption measure costing 5,000,000 soums a year (frequency after the measure: 2 times per 10 years) change the ROSI? Calculate it.
:::

## Summary

- Information security means ensuring confidentiality, integrity and availability.
- Threats: human error, fraud, malware, insider threats, failures, natural events.
- Protection methods: legal, organizational, technical and software, physical.
- Key measures: MFA, least privilege, updates, 3-2-1 backups, encryption, staff training.
- Risk is assessed economically: ALE = SLE × ARO, and ROSI shows whether a measure pays off.

## Check yourself

:::check
Q: What are the three core properties of information security?
A: Confidentiality, integrity and availability.
Q: What does the least privilege principle mean?
A: Each user has access only to the data and functions needed for their work.
Q: What is the 3-2-1 backup rule?
A: 3 copies of the data, on 2 different media, 1 of them stored in another location.
Q: If SLE is 40 million soums and ARO is 0.5, what is the ALE?
A: 40 million × 0.5 = 20 million soums a year.
:::

:::link https://www.iso.org/standard/27001
ISO/IEC 27001
The international standard for information security management systems.
:::
