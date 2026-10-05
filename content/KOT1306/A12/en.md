---
title: Managing users and their permissions in the Windows operating system
summary: Local and domain accounts; built-in accounts and groups; creating users and groups with lusrmgr.msc, net user, net localgroup and PowerShell; UAC; password and lockout policies; NTFS permissions, inheritance, the precedence of Deny; icacls; the principle of least privilege and granting rights through groups (AGDLP); calculating effective permissions.
---

## Why it matters

In a computer lab a student must not be able to delete another student's work, while the teacher must be able to see all the work. Only accountants should get into the accounting folder, and the administrator account should never be used for everyday work. All of this is achieved through **accounts, groups and permissions**. A large share of security incidents start precisely with excessive rights.

:::note
**After this session you will be able to:**

- tell apart local and domain accounts, built-in accounts and groups;
- create users and groups with graphical tools and from the command line;
- configure the password and account lockout policies;
- grant NTFS permissions and explain the inheritance and Deny rules;
- calculate a user's effective permission from groups and the ACL.
:::

## Accounts and groups

| Account type | Where it is stored | Example |
|---|---|---|
| **Local** | in the computer's SAM database | `PC07\talaba` |
| **Domain** | in Active Directory | `AIFU\a.karimov` |
| **Microsoft account** | in the cloud (home computers) | `name@outlook.com` |

Built-in accounts: **Administrator** (full rights; not used for ordinary work, renaming or disabling it is recommended), **Guest** (disabled by default — it should stay that way).

Built-in groups: **Administrators** (full control), **Users** (ordinary work), **Remote Desktop Users** (remote desktop access), **Backup Operators** (backups), **Guests**. In a domain: **Domain Admins**, **Domain Users** and others.

**UAC** (User Account Control): even an administrator works with ordinary rights by default; when the system needs a change, a confirmation window appears.

## Creating users and groups

The graphical way: **lusrmgr.msc** (Local Users and Groups) or **Computer Management**; in a domain — **Active Directory Users and Computers** (dsa.msc).

The command line (as administrator):

```text
net user a.karimov Parol#2026 /add /fullname:"Aziz Karimov"
net localgroup Talabalar /add
net localgroup Talabalar a.karimov /add
net user a.karimov /active:no            disable the account temporarily
net user a.karimov                       information about the account
whoami /groups                           the current user's groups
```

PowerShell:

```text
$p = Read-Host -AsSecureString "Password"
New-LocalUser -Name "a.karimov" -Password $p -FullName "Aziz Karimov"
New-LocalGroup -Name "Talabalar"
Add-LocalGroupMember -Group "Talabalar" -Member "a.karimov"
Get-LocalGroupMember -Group "Administrators"
```

## Password and lockout policies

**secpol.msc** → Account Policies (in a domain — the Default Domain Policy GPO):

| Setting | Recommendation |
|---|---|
| Minimum password length | 12 characters |
| Password must meet complexity requirements | Enabled |
| Maximum password age | 90 days (or no expiry with a long password — the modern advice) |
| Enforce password history | 10 previous passwords |
| Account lockout threshold | 5 wrong attempts |
| Account lockout duration | 15 minutes |

Lockout slows down password-guessing (brute-force) attacks.

## NTFS permissions

Folder or file properties → the **Security** tab. Every object has an **ACL** (access control list): which permission is **Allowed** or **Denied** to which user or group.

| Permission | What it allows |
|---|---|
| **Full control** | everything, including changing permissions and taking ownership |
| **Modify** | read, write, delete |
| **Read & execute** | read and run programs |
| **List folder contents** | see what is inside a folder |
| **Read** | read files |
| **Write** | create new files and write |

The rules:

1. **Permissions add up**: the Allows given to the user and all their groups are combined.
2. **Deny wins**: if some group has a Deny, an Allow from another group cannot override it (for explicitly set permissions).
3. **Nothing granted** means no access (an implicit deny).
4. **Inheritance**: a folder's permissions pass to the files and folders inside it; inheritance can be broken (Advanced → Disable inheritance).

From the command line: `icacls D:\Kurslar` — view the ACL; `icacls D:\Kurslar /grant Talabalar:(OI)(CI)RX` — give the Talabalar group read and execute (inherited by the objects inside).

**The principle of least privilege**: everyone gets the minimum rights needed for their work. **Rights are granted to groups**, and users are added to groups (in a domain, **AGDLP**: Account → Global group → Domain Local group → Permission).

## Worked example: calculating the effective permission

The first line holds the number of groups *g*, each of the next *g* lines a group name and its members. Then the number of ACL entries and each entry: the subject (a user, a group or `Everyone`), `Allow`/`Deny` and permission letters (`R` — read, `W` — write, `D` — delete). Then the queries: a user and one permission letter. For each query print the outcome: if there is a Deny — `denied (Deny)`, if there is an Allow — `allowed`, if there is nothing — `denied (not granted)`.

```python
g = int(input())
guruhlar = {}                                  # user -> set of groups
for _ in range(g):
    qism = input().split()
    for azo in qism[1:]:
        guruhlar.setdefault(azo, set()).add(qism[0])
acl = [input().split() for _ in range(int(input()))]
for _ in range(int(input())):
    user, ruxsat = input().split()
    subyektlar = {user, "Everyone"} | guruhlar.get(user, set())
    allow = deny = False
    for subyekt, tur, harflar in acl:
        if subyekt in subyektlar and ruxsat in harflar:
            if tur == "Deny":
                deny = True
            else:
                allow = True
    if deny:
        print(user, ruxsat + ": denied (Deny)")       # Deny always wins
    elif allow:
        print(user, ruxsat + ": allowed")
    else:
        print(user, ruxsat + ": denied (not granted)")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int g = Integer.parseInt(in.nextLine().trim());
        Map<String, Set<String>> guruhlar = new HashMap<>();       // user -> set of groups
        for (int i = 0; i < g; i++) {
            String[] qism = in.nextLine().trim().split(" ");
            for (int j = 1; j < qism.length; j++) {
                guruhlar.computeIfAbsent(qism[j], k -> new HashSet<>()).add(qism[0]);
            }
        }
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> acl = new ArrayList<>();
        for (int i = 0; i < n; i++) acl.add(in.nextLine().trim().split(" "));
        int q = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < q; i++) {
            String[] soruv = in.nextLine().trim().split(" ");
            String user = soruv[0];
            String ruxsat = soruv[1];
            Set<String> subyektlar = new HashSet<>(guruhlar.getOrDefault(user, new HashSet<>()));
            subyektlar.add(user);
            subyektlar.add("Everyone");
            boolean allow = false, deny = false;
            for (String[] e : acl) {
                if (subyektlar.contains(e[0]) && e[2].contains(ruxsat)) {
                    if (e[1].equals("Deny")) deny = true;
                    else allow = true;
                }
            }
            if (deny) {
                System.out.println(user + " " + ruxsat + ": denied (Deny)");   // Deny always wins
            } else if (allow) {
                System.out.println(user + " " + ruxsat + ": allowed");
            } else {
                System.out.println(user + " " + ruxsat + ": denied (not granted)");
            }
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
    string satr;
    getline(cin, satr);
    int g = stoi(satr);
    map<string, set<string>> guruhlar;                             // user -> set of groups
    for (int i = 0; i < g; i++) {
        getline(cin, satr);
        istringstream ss(satr);
        string nom, azo;
        ss >> nom;
        while (ss >> azo) guruhlar[azo].insert(nom);
    }
    getline(cin, satr);
    int n = stoi(satr);
    vector<vector<string>> acl(n, vector<string>(3));
    for (auto& e : acl) cin >> e[0] >> e[1] >> e[2];
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string user, ruxsat;
        cin >> user >> ruxsat;
        set<string> subyektlar = guruhlar[user];
        subyektlar.insert(user);
        subyektlar.insert("Everyone");
        bool allow = false, deny = false;
        for (auto& e : acl) {
            if (subyektlar.count(e[0]) && e[2].find(ruxsat) != string::npos) {
                if (e[1] == "Deny") deny = true;
                else allow = true;
            }
        }
        if (deny) {
            cout << user << " " << ruxsat << ": denied (Deny)" << endl;   // Deny always wins
        } else if (allow) {
            cout << user << " " << ruxsat << ": allowed" << endl;
        } else {
            cout << user << " " << ruxsat << ": denied (not granted)" << endl;
        }
    }
    return 0;
}
```

Input (the group names are Uzbek: students, teachers, lab assistants):

```text
3
Talabalar ali vali sami
Oqituvchilar nodir
Laborantlar sami
5
Everyone Allow R
Talabalar Allow W
Laborantlar Deny D
Oqituvchilar Allow RWD
vali Deny W
6
ali W
vali W
sami D
ali D
nodir D
mehmon R
```

Output:

```text
ali W: allowed
vali W: denied (Deny)
sami D: denied (Deny)
ali D: denied (not granted)
nodir D: allowed
mehmon R: allowed
```

`vali` has write access through the Talabalar group, but the Deny given to him personally wins. `mehmon` (a guest) is in no group but can read through `Everyone` — that is why granting broad rights to `Everyone` is dangerous.

:::warning
**Common mistakes**

- **Doing everyday work as administrator.** Malware then runs with administrator rights too.
- **Granting permissions to each user separately.** Grant them to one group, not to 300 students one by one.
- **Overusing Deny.** Because it always wins, it leads to unexpected denials; usually it is enough not to grant an Allow.
- **Giving `Everyone` Full control or Modify.** This lets anyone on the network delete the folder.
- **Not deleting the account of an employee who has left.** Accounts of people who leave are deleted or disabled at once.
:::

:::tip
Run `whoami /groups` and `whoami /priv` on your computer: which groups are you in and what privileges do you have? Then run `icacls .` in any folder and explain the `(OI)(CI)`, `(F)`, `(M)`, `(RX)` marks in the ACL.
:::

## Summary

- Accounts: local (SAM), domain (AD); the built-in Administrator and Guest need care.
- Users and groups: lusrmgr.msc, dsa.msc, `net user`, `net localgroup`, `New-LocalUser`.
- Policies: password length and complexity, history, the lockout threshold.
- NTFS: Allows add up, Deny wins, nothing means denied; inheritance flows down; view with `icacls`.
- Least privilege and granting rights through groups simplify management and reduce mistakes.

## Check yourself

:::check
Q: Why are rights granted to groups rather than to users?
A: Management becomes easier and mistakes fewer: adding a new employee to a group is enough.
Q: A user gets Allow Write through one group and Deny Write through another. What is the result?
A: Writing is denied — Deny wins.
Q: What is the console for managing local users and groups called?
A: lusrmgr.msc (Local Users and Groups).
Q: What is the account lockout policy for?
A: After wrong attempts the account is locked for a while, which slows down password guessing.
Q: How does a permission given to a folder pass to the files inside it?
A: Through inheritance — unless inheritance has been broken.
:::

:::link https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/manage/understand-security-groups
Active Directory security groups
Microsoft's guide to security groups and rights.
:::
