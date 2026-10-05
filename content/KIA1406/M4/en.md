---
title: Cybersecurity architecture and policy. Security strategy, policy, hierarchy and types
summary: Principles of security architecture — defence in depth, least privilege, separation of duties, default deny, Zero Trust; security strategy; the document hierarchy — policy, standard, procedure, guideline; types of policy (organisational, issue-specific, system-specific) and examples (AUP, password, access, backup, incident response); ISO/IEC 27001 and the ISMS; an access rights audit.
---

## Why it matters

Even the most expensive firewall does not protect an organisation if employees write passwords on sticky notes or everyone has administrator rights. Security is built **systematically**: first management decides what must be protected and how (strategy and policy), then these rules become technical solutions and daily practice. In this lecture we look at the blueprint of the security "building" — architecture and policies.

:::note
**After this lesson you will be able to:**

- explain the main principles of security architecture;
- tell a policy, a standard, a procedure and a guideline apart;
- name the kinds of security policy and what they contain;
- explain what ISO/IEC 27001 and an ISMS are;
- check users' rights against the principle of least privilege.
:::

## Architecture principles

| Principle | Meaning | Example |
|---|---|---|
| **Defence in depth** | several independent layers of protection | firewall + segmentation + antivirus + encryption |
| **Least privilege** | everyone gets only the rights needed for their work | an accountant gets into the finance folder only |
| **Separation of duties** | an important action needs more than one person | one person enters a payment, another approves it |
| **Default deny** | everything not explicitly allowed is forbidden | the firewall's last rule is "deny all" |
| **Secure by default** | a system ships with secure settings | changing the default password is mandatory at first login |
| **Simplicity** | a complex system has more mistakes | turning off unneeded services |
| **Zero Trust** | even those inside the network are not trusted automatically; every request is checked | MFA and a device health check for every application |

## Security strategy

A **strategy** is the organisation's long-term security goals and the ways of reaching them: which assets matter most, what level of risk is acceptable, what resources are allocated, which indicators measure progress. The strategy is aligned with the business goals — security is not for its own sake but for the organisation's work.

## The document hierarchy

| Level | Document | Content | Example |
|---|---|---|---|
| 1 | **Policy** | what is done and why; approved by management; rarely changes | "All confidential data must be encrypted" |
| 2 | **Standard** | mandatory specific requirements | "Encryption — AES-256, keys rotated every year" |
| 3 | **Procedure** | how to do it step by step | "Turning on BitLocker on a laptop: 1) … 2) …" |
| 4 | **Guideline** | recommendations, not mandatory | "Using a passphrase instead of a password is recommended" |
| — | **Baseline** | the minimum secure settings of systems | "The CIS Benchmark for Windows Server" |

## Types of policy

- **Organisational (programme) policy** — the organisation's overall approach to security, roles and responsibilities; signed by the head.
- **Issue-specific policies** — on a particular matter: use of the Internet and e-mail, mobile devices (BYOD), remote work.
- **System-specific policies** — for a particular system: "rules for accessing the 1C accounting system".

Sample policies:

| Policy | Main rules |
|---|---|
| **AUP** (acceptable use) | how the organisation's resources may and may not be used |
| **Password policy** | length, complexity, rotation, MFA, lockout |
| **Access control** | the procedure for granting and revoking rights, regular review |
| **Data classification** | public, internal, confidential, strictly confidential — and protection requirements for each |
| **Backup** | what, how often and where to keep it; testing restores |
| **Incident response** | who acts, in what order and how quickly |

A good policy is clear, short, achievable, has a named owner and a review date, is communicated to staff (with a signature) and states the consequences of breaking it.

## ISO/IEC 27001 and the ISMS

An **ISMS** (Information Security Management System) is the single system of policies, processes, roles and controls for managing information security. **ISO/IEC 27001** is the standard of requirements for an ISMS; an organisation can be certified as compliant with it. Its annex (Annex A) lists 93 controls in 4 groups: organisational, people, physical and technological. An ISMS runs on the **PDCA** cycle: Plan → Do → Check → Act.

## Worked example: a least-privilege audit

The policy sets the rights required for each role. An audit compares each user's actual rights with the role's requirements. The first line holds the number of roles, then each role: its name and its rights separated by commas. Then the number of users and each one: name, role, actual rights. For each user print the excess rights (not required by the role) and the missing rights in alphabetical order.

```python
rollar = {}
for _ in range(int(input())):
    nom, huquqlar = input().split()
    rollar[nom] = set(huquqlar.split(","))
for _ in range(int(input())):
    user, rol, huquqlar = input().split()
    bor = set(huquqlar.split(","))
    kerak = rollar.get(rol, set())
    ortiqcha = sorted(bor - kerak)                # a breach of least privilege
    yetishmaydi = sorted(kerak - bor)
    if not ortiqcha and not yetishmaydi:
        print(user + ": compliant")
        continue
    qismlar = []
    if ortiqcha:
        qismlar.append("excess " + ",".join(ortiqcha))
    if yetishmaydi:
        qismlar.append("missing " + ",".join(yetishmaydi))
    print(user + ": " + "; ".join(qismlar))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, TreeSet<String>> rollar = new HashMap<>();
        int r = in.nextInt();
        for (int i = 0; i < r; i++) {
            String nom = in.next();
            rollar.put(nom, new TreeSet<>(Arrays.asList(in.next().split(","))));
        }
        int u = in.nextInt();
        for (int i = 0; i < u; i++) {
            String user = in.next();
            String rol = in.next();
            TreeSet<String> bor = new TreeSet<>(Arrays.asList(in.next().split(",")));
            TreeSet<String> kerak = rollar.getOrDefault(rol, new TreeSet<>());
            TreeSet<String> ortiqcha = new TreeSet<>(bor);
            ortiqcha.removeAll(kerak);                              // a breach of least privilege
            TreeSet<String> yetishmaydi = new TreeSet<>(kerak);
            yetishmaydi.removeAll(bor);
            if (ortiqcha.isEmpty() && yetishmaydi.isEmpty()) {
                System.out.println(user + ": compliant");
                continue;
            }
            List<String> qismlar = new ArrayList<>();
            if (!ortiqcha.isEmpty()) qismlar.add("excess " + String.join(",", ortiqcha));
            if (!yetishmaydi.isEmpty()) qismlar.add("missing " + String.join(",", yetishmaydi));
            System.out.println(user + ": " + String.join("; ", qismlar));
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <iterator>
#include <map>
#include <set>
#include <sstream>
#include <string>
using namespace std;

set<string> ajrat(const string& s) {
    set<string> natija;
    stringstream ss(s);
    string h;
    while (getline(ss, h, ',')) natija.insert(h);
    return natija;
}

string birlashtir(const set<string>& s) {
    string natija;
    for (auto& h : s) natija += (natija.empty() ? "" : ",") + h;
    return natija;
}

int main() {
    map<string, set<string>> rollar;
    int r;
    cin >> r;
    for (int i = 0; i < r; i++) {
        string nom, huquqlar;
        cin >> nom >> huquqlar;
        rollar[nom] = ajrat(huquqlar);
    }
    int u;
    cin >> u;
    for (int i = 0; i < u; i++) {
        string user, rol, huquqlar;
        cin >> user >> rol >> huquqlar;
        set<string> bor = ajrat(huquqlar), kerak = rollar[rol], ortiqcha, yetishmaydi;
        set_difference(bor.begin(), bor.end(), kerak.begin(), kerak.end(), inserter(ortiqcha, ortiqcha.end()));   // a breach of least privilege
        set_difference(kerak.begin(), kerak.end(), bor.begin(), bor.end(), inserter(yetishmaydi, yetishmaydi.end()));
        if (ortiqcha.empty() && yetishmaydi.empty()) {
            cout << user << ": compliant" << endl;
            continue;
        }
        string qismlar;
        if (!ortiqcha.empty()) qismlar += "excess " + birlashtir(ortiqcha);
        if (!yetishmaydi.empty()) qismlar += (qismlar.empty() ? "" : "; ") + string("missing ") + birlashtir(yetishmaydi);
        cout << user << ": " << qismlar << endl;
    }
    return 0;
}
```

Input (the role names are Uzbek: accountant, teacher, student):

```text
3
buxgalter finance_read,finance_write
oqituvchi grades_read,grades_write,lms
talaba lms,grades_read
4
dilnoza buxgalter finance_read,finance_write
bobur oqituvchi grades_read,grades_write,lms,finance_read
aziz talaba lms,grades_read,grades_write
sardor oqituvchi lms
```

Output:

```text
dilnoza: compliant
bobur: excess finance_read
aziz: excess grades_write
sardor: missing grades_read,grades_write
```

The most dangerous finding is `aziz`: a student has the right to **change** grades. Such "privilege creep" usually happens when an employee moves to another department and their old rights are not revoked; that is why an access policy requires rights to be reviewed at least once a quarter.

:::warning
**Common mistakes**

- **Copying a policy from the Internet.** A policy must fit the organisation's real processes and risks, otherwise nobody follows it.
- **Mixing up policy and procedure.** A policy is "what and why", a procedure is "how".
- **Not acquainting staff with the policy.** A rule nobody knows cannot be followed; acquaintance is recorded with a signature.
- **Only ever adding rights, never removing them.** Over time everyone ends up with access to "everything".
- **Taking Zero Trust for a single product.** Zero Trust is an architectural approach, not a product.
:::

:::tip
Write a one-page draft AUP for a computer lab at your university: allowed use, prohibitions (installing foreign software, using other people's accounts), personal data, consequences of violations and the person responsible.
:::

## Summary

- Architecture principles: defence in depth, least privilege, separation of duties, default deny, secure by default, simplicity, Zero Trust.
- The document hierarchy: policy → standard → procedure → guideline; a baseline is the minimum settings.
- Types of policy: organisational, issue-specific, system-specific; examples — AUP, password, access, classification, backup, incident response.
- ISO/IEC 27001 sets the requirements for an ISMS; the PDCA cycle.
- Regular access audits reveal breaches of least privilege.

## Check yourself

:::check
Q: Which is the highest, most general document in the security document hierarchy?
A: The policy.
Q: "An employee gets only the rights needed to do their job" — which principle is this?
A: Least privilege.
Q: How does a procedure differ from a policy?
A: A policy sets what is done and why, while a procedure says how to do it step by step.
Q: Give an example of separation of duties.
A: One employee enters a payment and another approves it — one person alone cannot commit fraud.
Q: What does ISO/IEC 27001 set out?
A: The requirements for an information security management system (ISMS).
:::

:::link https://www.iso.org/standard/27001
ISO/IEC 27001
The official page of the information security management systems standard.
:::
