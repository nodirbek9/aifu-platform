---
title: Access control models. DAC, MAC, RBAC, ABAC, ACLs and matrices
summary: Access control concepts — subject, object, operation; principles — least privilege, need to know, separation of duties, default deny; the access matrix, ACLs and capability lists; the discretionary model (DAC) — Unix and NTFS permissions, the Trojan horse problem; the mandatory model (MAC) — security labels, the Bell–LaPadula and Biba models, SELinux; role-based access control (RBAC) — role hierarchies and constraints; attribute-based access control (ABAC) — policies, XACML; comparing the models.
---

## Why does this topic matter?

In M10 we saw how a user proves **who they are**. The next question is: what is an authenticated user **allowed to do**? A student should see their grade but not change it; an accountant should prepare a payment but not approve it themselves; a secret document should be open only to staff of the appropriate level. In the OWASP Top 10, **Broken Access Control** ranks first as the most dangerous weakness of web applications.

:::note
**After this lesson you will be able to:**

- state the basic principles of access control;
- build an access matrix, ACLs and capability lists;
- distinguish and compare the DAC, MAC, RBAC and ABAC models;
- apply the Bell–LaPadula and Biba rules;
- show the logic of checking a request in RBAC in a program.
:::

## Basic concepts and principles

A **subject** (user, process) requests to perform an **operation** (read, write, execute, delete) on an **object** (file, table, printer); the **access control mechanism** (reference monitor) checks every request against the policy. A reference monitor must always be invoked, impossible to bypass and small enough to be verified.

| Principle | Meaning |
|---|---|
| **Least privilege** | only the rights needed for the task, only for as long as needed |
| **Need to know** | only work-related information, even if the clearance level is sufficient |
| **Separation of duties** | a critical operation is split between two people: one prepares, the other approves |
| **Default deny** | everything not explicitly allowed is forbidden |
| **Complete mediation** | every access is checked, not only the first |

## The access matrix, ACLs and capabilities

**The access matrix** (Lampson, 1971): rows are subjects, columns are objects, a cell holds the allowed operations.

| | report.docx | base.db | printer |
|---|---|---|---|
| **Ali** | read, write, owner | read | print |
| **Vali** | read | — | print |
| **backup process** | read | read | — |

In practice the matrix is huge and sparse, so it is stored in one of two ways:

- **ACL** (Access Control List) — by **column**: each object carries a list of "who can do what". File systems (NTFS, Unix), network devices. Convenient: "who can access this file?"; inconvenient: "what can Ali access?".
- **Capability list** — by **row**: each subject holds tokens of "which right on which object". Examples: Kerberos tickets, OAuth tokens, open file descriptors. Passing a right on is easy, but revoking it is hard.

## DAC — the discretionary model

In **DAC** (Discretionary Access Control) the **owner** of an object decides who gets which rights. Examples: Unix permissions (`rwx` for owner, group and others — in A11), Windows NTFS ACLs, "sharing" in Google Drive.

- Advantage: flexible, simple, convenient for users.
- Drawback: there is no central control; a mistake or a **Trojan horse** works on the user's behalf with all their rights — for example, it copies a secret file into a public folder, and DAC does not stop it.

## MAC — the mandatory model

In **MAC** (Mandatory Access Control) the decision is made not by the owner but by the **system policy**: subjects receive a **clearance** level and objects a **classification** label, for example: Unclassified (U) < Confidential (C) < Secret (S) < Top Secret (TS). A user cannot change the label even of their own document.

**Bell–LaPadula** (BLP, 1973) is a model protecting **confidentiality**:

- **Simple security property** — "no read up": a subject reads only objects at its own level or lower.
- **\*-property** — "no write down": a subject writes only to objects at its own level or higher — this stops secret information "leaking down" to a lower level (even through a Trojan).

**Biba** (1977) is a model protecting **integrity**, with the rules reversed: "no read down" (do not get contaminated by untrusted data) and "no write up" (a low-level process must not corrupt important data). **Mandatory Integrity Control** in Windows (the browser runs at a "low" integrity level) is the Biba idea.

MAC is used in military and government systems and also in **SELinux** and **AppArmor** (Linux, Android): even a compromised process with `root` rights cannot go beyond the policy.

## RBAC — role-based access control

In **RBAC** (Role-Based Access Control, a NIST standard) permissions are given not to users but to **roles**; users are assigned to roles: user → role → permission. When an employee's position changes, changing their role is enough.

- **Role hierarchy**: the "senior teacher" role inherits all permissions of "teacher".
- **Constraints**: static separation of duties (one person cannot hold both the "payment preparer" and "payment approver" roles), dynamic (cannot activate both in one session).
- Uses: corporate systems, databases (`GRANT ... TO role`), cloud (AWS IAM roles), Active Directory groups, Kubernetes RBAC.
- Drawback: "role explosion" — a new role is created for every exception, until there are more roles than users.

## ABAC — attribute-based access control

In **ABAC** (Attribute-Based Access Control) the decision is made by rules over **attributes**: of the subject (position, department, clearance), the object (type, owner, classification), the operation and the **environment** (time, location, device, risk level). For example: "a doctor **may read** a patient's record if the patient is in the same department **and** the doctor is on shift now **and** the connection comes from the hospital network". Policies are written in XACML or in cloud policy languages (AWS IAM conditions). ABAC is the most flexible model, but its policies are the hardest to design and verify. The **Zero Trust** architecture is built precisely on re-evaluating every request by its context.

| Model | Who decides | Strength | Weakness | Where |
|---|---|---|---|---|
| DAC | the object owner | simple, flexible | no central control, Trojans | personal computers, file sharing |
| MAC | system policy (labels) | strict, stops leaks | rigid, hard to manage | military, government, SELinux |
| RBAC | the administrator (roles) | fits the organization's structure, easy to audit | role explosion | enterprises, DBMS, cloud |
| ABAC | a policy over attributes | contextual, fine-grained control | complex, slower | cloud, zero trust systems |

## Worked example 1: the Bell–LaPadula model

Levels: U < C < S < TS. Input: *n* and *n* subjects (name level), *m* and *m* objects (name level), then requests (subject operation object, the operation is `read` or `write`). For each request print the decision under the BLP rules.

```python
import sys

DARAJA = {"U": 0, "C": 1, "S": 2, "TS": 3}

t = sys.stdin.read().split()
i = 0
subyekt, obyekt = {}, {}
for jadval in (subyekt, obyekt):
    k = int(t[i])
    i += 1
    for _ in range(k):
        jadval[t[i]] = DARAJA[t[i + 1]]
        i += 2
while i + 2 < len(t):
    s, amal, o = t[i], t[i + 1], t[i + 2]
    i += 3
    if amal == "read":
        ruxsat, sabab = subyekt[s] >= obyekt[o], "no read up"      # simple security property
    else:
        ruxsat, sabab = subyekt[s] <= obyekt[o], "no write down"   # *-property
    print(f"{s} {amal} {o}: " + ("allow" if ruxsat else f"deny ({sabab})"))
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final Map<String, Integer> DARAJA = Map.of("U", 0, "C", 1, "S", 2, "TS", 3);

    static Map<String, Integer> oqi(Scanner in) {
        Map<String, Integer> jadval = new HashMap<>();
        int k = in.nextInt();
        for (int j = 0; j < k; j++) {
            String nom = in.next();
            jadval.put(nom, DARAJA.get(in.next()));
        }
        return jadval;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Integer> subyekt = oqi(in);
        Map<String, Integer> obyekt = oqi(in);
        while (in.hasNext()) {
            String s = in.next(), amal = in.next(), o = in.next();
            boolean ruxsat;
            String sabab;
            if (amal.equals("read")) {
                ruxsat = subyekt.get(s) >= obyekt.get(o);                // simple security property
                sabab = "no read up";
            } else {
                ruxsat = subyekt.get(s) <= obyekt.get(o);                // *-property
                sabab = "no write down";
            }
            System.out.println(s + " " + amal + " " + o + ": " + (ruxsat ? "allow" : "deny (" + sabab + ")"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

map<string, int> DARAJA = {{"U", 0}, {"C", 1}, {"S", 2}, {"TS", 3}};

map<string, int> oqi() {
    map<string, int> jadval;
    int k;
    cin >> k;
    for (int j = 0; j < k; j++) {
        string nom, d;
        cin >> nom >> d;
        jadval[nom] = DARAJA[d];
    }
    return jadval;
}

int main() {
    map<string, int> subyekt = oqi();
    map<string, int> obyekt = oqi();
    string s, amal, o;
    while (cin >> s >> amal >> o) {
        bool ruxsat;
        string sabab;
        if (amal == "read") {
            ruxsat = subyekt[s] >= obyekt[o];                            // simple security property
            sabab = "no read up";
        } else {
            ruxsat = subyekt[s] <= obyekt[o];                            // *-property
            sabab = "no write down";
        }
        cout << s << " " << amal << " " << o << ": " << (ruxsat ? string("allow") : "deny (" + sabab + ")") << endl;
    }
    return 0;
}
```

Input:

```text
3
ali S
vali C
olim TS
4
elon U
hisobot C
reja S
kod TS
vali read reja
ali read hisobot
ali write hisobot
vali write reja
olim read kod
olim write elon
```

Output:

```text
vali read reja: deny (no read up)
ali read hisobot: allow
ali write hisobot: deny (no write down)
vali write reja: allow
olim read kod: allow
olim write elon: deny (no write down)
```

Note: ali, at the "Secret" level, can read the "Confidential" report (hisobot) but **cannot write** to it — otherwise they (or a Trojan acting on their behalf) could copy secret information to a lower level. vali, on the other hand, can write to the "Secret" plan (reja) ("blind write") but cannot read it. In practice a high-level user who needs to write a low-level document logs in with a low-level session.

## Worked example 2: checking an RBAC request

Input: *r* and *r* lines "role permission1 permission2 …"; *u* and *u* lines "user role1 role2 …"; then requests "user permission". Go through the user's roles in the given order and print the first role that grants the permission; otherwise — deny (default deny).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
i = 0
rollar = {}
r = int(satrlar[i][0])
i += 1
for _ in range(r):
    rollar[satrlar[i][0]] = set(satrlar[i][1:])     # role -> permissions
    i += 1
foydalanuvchilar = {}
u = int(satrlar[i][0])
i += 1
for _ in range(u):
    foydalanuvchilar[satrlar[i][0]] = satrlar[i][1:]  # user -> roles
    i += 1
for kim, nima in satrlar[i:]:
    if kim not in foydalanuvchilar:
        print(f"{kim} {nima}: deny (user not found)")
        continue
    rol = next((x for x in foydalanuvchilar[kim] if nima in rollar.get(x, set())), None)
    print(f"{kim} {nima}: " + (f"allow ({rol})" if rol else "deny"))   # default deny
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String[]> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s.split("\\s+"));
        }
        int i = 0;
        Map<String, Set<String>> rollar = new HashMap<>();
        int r = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < r; j++, i++) {
            String[] q = satrlar.get(i);
            rollar.put(q[0], new HashSet<>(Arrays.asList(q).subList(1, q.length)));   // role -> permissions
        }
        Map<String, List<String>> foydalanuvchilar = new HashMap<>();
        int u = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < u; j++, i++) {
            String[] q = satrlar.get(i);
            foydalanuvchilar.put(q[0], Arrays.asList(q).subList(1, q.length));       // user -> roles
        }
        for (; i < satrlar.size(); i++) {
            String kim = satrlar.get(i)[0], nima = satrlar.get(i)[1];
            if (!foydalanuvchilar.containsKey(kim)) {
                System.out.println(kim + " " + nima + ": deny (user not found)");
                continue;
            }
            String topildi = null;
            for (String rol : foydalanuvchilar.get(kim)) {
                if (rollar.getOrDefault(rol, Set.of()).contains(nima)) {
                    topildi = rol;
                    break;
                }
            }
            System.out.println(kim + " " + nima + ": " + (topildi != null ? "allow (" + topildi + ")" : "deny"));   // default deny
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<vector<string>> satrlar;
    string s;
    while (getline(cin, s)) {
        istringstream ss(s);
        vector<string> q;
        string w;
        while (ss >> w) q.push_back(w);
        if (!q.empty()) satrlar.push_back(q);
    }
    size_t i = 0;
    map<string, set<string>> rollar;
    int r = stoi(satrlar[i++][0]);
    for (int j = 0; j < r; j++, i++) {
        rollar[satrlar[i][0]] = set<string>(satrlar[i].begin() + 1, satrlar[i].end());   // role -> permissions
    }
    map<string, vector<string>> foydalanuvchilar;
    int u = stoi(satrlar[i++][0]);
    for (int j = 0; j < u; j++, i++) {
        foydalanuvchilar[satrlar[i][0]] = vector<string>(satrlar[i].begin() + 1, satrlar[i].end());   // user -> roles
    }
    for (; i < satrlar.size(); i++) {
        const string& kim = satrlar[i][0];
        const string& nima = satrlar[i][1];
        if (!foydalanuvchilar.count(kim)) {
            cout << kim << " " << nima << ": deny (user not found)" << endl;
            continue;
        }
        string topildi;
        for (const string& rol : foydalanuvchilar[kim]) {
            if (rollar.count(rol) && rollar[rol].count(nima)) {
                topildi = rol;
                break;
            }
        }
        cout << kim << " " << nima << ": " << (!topildi.empty() ? "allow (" + topildi + ")" : string("deny")) << endl;   // default deny
    }
    return 0;
}
```

Input:

```text
4
talaba baho.korish jadval.korish
oqituvchi baho.korish baho.qoyish jadval.korish
dekan hisobot.korish baho.korish
admin foydalanuvchi.yaratish
4
ali talaba
vali oqituvchi
sardor oqituvchi dekan
olim admin
ali baho.qoyish
vali baho.qoyish
sardor hisobot.korish
olim baho.korish
ali jadval.korish
kamol baho.korish
```

Output:

```text
ali baho.qoyish: deny
vali baho.qoyish: allow (oqituvchi)
sardor hisobot.korish: allow (dekan)
olim baho.korish: deny
ali jadval.korish: allow (talaba)
kamol baho.korish: deny (user not found)
```

(The roles are student, teacher, dean and admin; the permissions are viewing grades, setting grades, viewing the timetable, viewing reports and creating users.) The system administrator olim can create users but does not see grades — this is **separation of duties** and **need to know**: a technical right is not a right to access the data. sardor has two roles and uses the permissions of both. Any request not on the list is denied — **default deny**. In real systems such a check runs on the **server** for every API request; merely hiding a button (in the frontend) is no protection.

:::warning
**Common mistakes**

- **Checking only in the interface.** Even if a button is hidden, the request can be sent directly; the check must be on the server.
- **Administrator rights for everyone.** Excess rights given "so that nothing breaks" multiply the damage of a breach.
- **Privilege creep.** When an employee moves between departments, their old rights remain; review rights regularly.
- **The account of an employee who left.** It must be deleted or blocked immediately.
- **Not checking the object identifier** (IDOR). Changing `/grade?id=125` to `id=126` to get someone else's data is the most common web vulnerability.
:::

:::tip
In Windows, look at the NTFS ACL in a file's Properties → Security tab; on the command line use `icacls file.txt`. In Linux, `getfacl file` shows the extended ACL and `ls -Z` shows the SELinux label.
:::

## Summary

- Access control: subject, object, operation; principles — least privilege, need to know, separation of duties, default deny.
- The access matrix is stored by column as ACLs and by row as capability lists.
- DAC — the owner decides; MAC — system policy and labels (BLP: no read up, no write down; Biba — reversed, for integrity).
- RBAC — permissions through roles, with a hierarchy and constraints; ABAC — a policy over attributes and the environment.
- The check is performed on every request, on the server, with default deny.

## Check yourself

:::check
Q: What is the difference between an ACL and a capability list?
A: An ACL is attached to an object (who can do what), a capability list to a subject (which right on which object).
Q: In the Bell–LaPadula model, can a "Secret" user write to an "Unclassified" document?
A: No, the *-property (no write down) forbids writing down.
Q: What is the weakness of DAC with respect to a Trojan horse?
A: The Trojan works on the user's behalf with all their rights and can copy data anywhere.
Q: What does a separation of duties constraint forbid in RBAC?
A: One user holding conflicting roles at the same time (for example, payment preparer and approver).
Q: Which attributes take part in an ABAC decision?
A: Attributes of the subject, the object, the operation and the environment (time, location, device).
:::

:::link https://csrc.nist.gov/pubs/sp/800/162/upd2/final
NIST SP 800-162: Guide to Attribute Based Access Control (ABAC)
ABAC concepts and a comparison with other models.
:::
