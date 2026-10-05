---
title: Cybercrime and the legal framework. Internal and external cybercrime, international and national laws, cyberethics, digital forensics
summary: The notion and kinds of cybercrime; insiders and outsiders; attackers' motives; the law of Uzbekistan — the Criminal Code chapter on crimes in information technology, the laws "On Cybersecurity" and "On Personal Data"; international instruments — the Budapest Convention, GDPR; cyberethics; the stages of digital forensics, the chain of custody, confirming integrity with a hash; an incident timeline.
---

## Why it matters

Cybercrime is the fastest-growing kind of crime in the world: stealing money from bank cards, selling personal data, extorting organisations with ransomware. A cybersecurity specialist must not only stop an attack but also investigate it **lawfully**, keep the evidence fit for court, and work within the law and ethics themselves. Even entering someone else's system "to learn" can be a crime.

:::note
**After this lesson you will be able to:**

- tell kinds of cybercrime apart, and internal from external threats;
- know the key provisions of the legal instruments of Uzbekistan and international ones;
- explain the principles of cyberethics;
- explain the stages of digital forensics and why a chain of custody is needed;
- reconstruct an incident timeline from different logs.
:::

## Kinds of cybercrime

**Cybercrime** is crime committed using, or against, a computer, a network or a digital device.

| Kind | Content | Example |
|---|---|---|
| Unauthorised access | unlawfully entering a protected system | guessing a password to get into an e-mail account |
| Data theft | personal, financial, trade secrets | selling a customer database |
| Fraud | getting money or data by deception | phishing, fake online shops |
| Malware | creating and spreading it | demanding a ransom with ransomware |
| Disrupting services | DoS/DDoS | taking down a competitor's website |
| Content crimes | prohibited material, defamation | spreading illegal content |

An **insider** is an employee or former employee of the organisation: they know the system well and already have access, so they are hard to detect. **Outsiders**: cybercriminal groups (for money), hacktivists (for an idea), state-backed groups (APTs — espionage and sabotage), "script kiddies" (with ready-made tools, out of curiosity).

## The legal framework

**The Republic of Uzbekistan**:

| Instrument | Main content |
|---|---|
| **The Criminal Code, chapter XX¹** "Crimes in the field of information technology" (articles 278¹–278⁷) | breaking the rules of informatisation, unlawful use of computer information, modifying it, computer sabotage, creating and spreading malware |
| **The Law "On Cybersecurity"** (2022) | the subjects of cybersecurity, protecting critical information infrastructure, responding to cyber incidents, the competent bodies |
| **The Law "On Personal Data"** (2019) | rules for collecting, processing and storing personal data, the data subject's consent, the requirement to store citizens' data within the country |
| **The Law "On Informatisation"** | information resources and systems and their protection |

**International instruments**:

- **The Budapest Convention** (Council of Europe, 2001) — the first international treaty on cybercrime: a classification of offences, investigative powers, international cooperation.
- **GDPR** (European Union, 2018) — personal data protection; notifying a breach within 72 hours, large fines.
- Work has been under way within the **UN** on a convention on countering cybercrime.

Important: laws are limited by national borders, but cybercrime is not — so international cooperation and fast information exchange (24/7 points of contact) are essential.

## Cyberethics

**Cyberethics** is the rules of moral behaviour in the digital environment; it also covers actions that are not forbidden by law but are wrong.

- Respect other people's privacy; do not enter or watch without permission.
- Respect copyright (programs, texts, pictures).
- Report a vulnerability you find to its owner (**responsible disclosure**), rather than publishing or selling it.
- Test only with **written permission** (a penetration testing contract).
- Codes of ethics for security specialists: (ISC)², EC-Council.

## Digital forensics

**Digital forensics** is identifying, collecting, preserving, analysing and presenting digital evidence in a way that holds up in court.

| Stage | Content |
|---|---|
| **Identification** | which devices and data may be evidence |
| **Preservation and collection** | isolating the device, taking a **bit-for-bit copy** of the disk and memory (with a write blocker), not working on the original |
| **Confirming integrity** | computing and recording the **hash value** (SHA-256) of the original and the copy |
| **Analysis** | logs, files, deleted data, network traffic; the **timeline** |
| **Reporting** | presenting the evidence and conclusions clearly for the court |

The **chain of custody** is the record of who took, kept and handed over the evidence, when, where and for what purpose. If the chain is broken, the court may not accept the evidence. The **order of volatility**: first the data that disappears quickly (RAM, network connections), then disks and archives.

## Worked example: an incident timeline

An investigator obtained log records from different systems (firewall, web server, database); they are in mixed order. The first line holds the keyword being tracked (for example, the attacker's IP address), the second the number of records, then the records: time (`HH:MM:SS`), source and event text. Print the records sorted by time (input order on ties), marking those containing the keyword with `*`, and at the end print the number of marked events and the times of the first and last.

```python
kalit = input().strip()
n = int(input())
yozuvlar = []
for i in range(n):
    satr = input().strip()
    vaqt, manba, matn = satr.split(" ", 2)
    yozuvlar.append((vaqt, i, manba, matn))
yozuvlar.sort()                                       # time, then input order
belgilangan = []
for vaqt, _, manba, matn in yozuvlar:
    belgi = "*" if kalit in matn else " "
    if belgi == "*":
        belgilangan.append(vaqt)
    print(belgi + " " + vaqt + " [" + manba + "] " + matn)
if belgilangan:
    print("events: " + str(len(belgilangan)) + ", first " + belgilangan[0] + ", last " + belgilangan[-1])
else:
    print("events: 0")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String kalit = in.nextLine().trim();
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> yozuvlar = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            yozuvlar.add(in.nextLine().trim().split(" ", 3));
        }
        yozuvlar.sort((a, b) -> a[0].compareTo(b[0]));           // stable: input order on ties
        List<String> belgilangan = new ArrayList<>();
        for (String[] y : yozuvlar) {
            String belgi = y[2].contains(kalit) ? "*" : " ";
            if (belgi.equals("*")) belgilangan.add(y[0]);
            System.out.println(belgi + " " + y[0] + " [" + y[1] + "] " + y[2]);
        }
        if (!belgilangan.isEmpty()) {
            System.out.println("events: " + belgilangan.size() + ", first " + belgilangan.get(0) + ", last " + belgilangan.get(belgilangan.size() - 1));
        } else {
            System.out.println("events: 0");
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

struct Yozuv {
    string vaqt, manba, matn;
};

int main() {
    string kalit, satr;
    getline(cin, kalit);
    getline(cin, satr);
    int n = stoi(satr);
    vector<Yozuv> yozuvlar;
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        size_t a = satr.find(' ');
        size_t b = satr.find(' ', a + 1);
        yozuvlar.push_back({satr.substr(0, a), satr.substr(a + 1, b - a - 1), satr.substr(b + 1)});
    }
    stable_sort(yozuvlar.begin(), yozuvlar.end(), [](const Yozuv& x, const Yozuv& y) {
        return x.vaqt < y.vaqt;                               // stable: input order on ties
    });
    vector<string> belgilangan;
    for (auto& y : yozuvlar) {
        string belgi = y.matn.find(kalit) != string::npos ? "*" : " ";
        if (belgi == "*") belgilangan.push_back(y.vaqt);
        cout << belgi << " " << y.vaqt << " [" << y.manba << "] " << y.matn << endl;
    }
    if (!belgilangan.empty()) {
        cout << "events: " << belgilangan.size() << ", first " << belgilangan.front() << ", last " << belgilangan.back() << endl;
    } else {
        cout << "events: 0" << endl;
    }
    return 0;
}
```

Input:

```text
203.0.113.9
7
10:15:02 firewall port scan from 203.0.113.9
10:20:40 web failed login admin from 203.0.113.9
10:21:05 web successful login admin from 203.0.113.9
10:05:00 ad user ali logged on
10:22:30 db admin exported 5000 records
10:20:41 web failed login admin from 203.0.113.9
10:30:00 firewall large outbound transfer to 203.0.113.9
```

Output:

```text
  10:05:00 [ad] user ali logged on
* 10:15:02 [firewall] port scan from 203.0.113.9
* 10:20:40 [web] failed login admin from 203.0.113.9
* 10:20:41 [web] failed login admin from 203.0.113.9
* 10:21:05 [web] successful login admin from 203.0.113.9
  10:22:30 [db] admin exported 5000 records
* 10:30:00 [firewall] large outbound transfer to 203.0.113.9
events: 5, first 10:15:02, last 10:30:00
```

The timeline reveals the story of the attack: scanning → password guessing → logging in as `admin` → an export from the database (no IP given, but the time and account match) → moving the data out. In a real investigation the clocks of all systems must be **synchronised** (NTP), otherwise the order comes out wrong.

:::warning
**Common mistakes**

- **Analysing the original device.** Any opening changes file times; work only on a copy and keep the hash of the original.
- **Switching the computer off at once.** Evidence in RAM (processes, connections, keys) is lost.
- **Not recording the chain of custody.** If who handled the evidence and when is not documented, the court may reject it.
- **Testing without permission "with good intentions".** Checking a vulnerability without the owner's written permission also breaks the law.
- **Unsynchronised system clocks.** Logs from different sources fall into the wrong order.
:::

:::tip
Pick a well-known cyber incident (for example, WannaCry 2017, the Equifax data breach 2017 or Colonial Pipeline 2021) and draw up its timeline: the initial access method, the date it was detected, the consequences and the measures taken against the criminals.
:::

## Summary

- Cybercrime is crime committed with or against a computer; offenders are internal or external.
- In Uzbekistan: chapter XX¹ of the Criminal Code, the laws "On Cybersecurity" (2022) and "On Personal Data" (2019); internationally — the Budapest Convention, GDPR.
- Cyberethics: respect for privacy, testing with permission, responsible disclosure.
- Digital forensics: identification → preservation → integrity by hash → analysis → report; the chain of custody is mandatory.
- A timeline joins different logs by synchronised time to reconstruct the story of an attack.

## Check yourself

:::check
Q: What kind of threat is an employee using their own access to steal data?
A: An insider threat.
Q: What is used in digital forensics to show that evidence has not changed?
A: The hash values (for example, SHA-256) of the original and the copy are computed and compared.
Q: What does the chain of custody document?
A: Who took, kept and handed over the evidence, when, where and for what purpose.
Q: Which is the first international treaty on cybercrime?
A: The Council of Europe's Budapest Convention (2001).
Q: Why is switching the computer off at once not recommended?
A: Volatile evidence in RAM — processes, network connections, encryption keys — is lost.
:::

:::link https://www.coe.int/en/web/cybercrime/the-budapest-convention
The Budapest Convention on Cybercrime
The Council of Europe's page on the convention on cybercrime.
:::
