---
title: Performing backups (Backup and Restore) in Windows. Types of privileges in Windows and the procedure for controlling access to files and folders
summary: Backup strategy — the 3-2-1 rule, full, incremental and differential backups, RPO and RTO, testing restores, an offline copy against ransomware; Windows tools — File History, Windows Backup, system image (Backup and Restore), restore points, wbadmin; account types and privileges in Windows — administrator, standard user, UAC, user rights; NTFS permissions — basic and special permissions, inheritance, explicit deny, effective permissions, share and NTFS permissions together; icacls; computing effective permissions in a program.
---

## Why does this topic matter?

The two topics — "what do we do if data is lost?" and "who can access the data?" — serve one goal: the **availability** and **confidentiality** of information (CIA). After ransomware, a disk failure or an accidental deletion only a tested backup saves an organization; wrong permissions, on the other hand, open a confidential folder to the whole organization. Both tasks are everyday work for a Windows administrator.

:::note
**After this assignment you will be able to:**

- design a backup strategy (3-2-1, backup types, RPO and RTO);
- use the Windows tools for creating backups and restoring;
- distinguish Windows account types, UAC and user rights;
- explain NTFS permissions, inheritance and explicit deny;
- compute a user's effective permissions.
:::

## Backup strategy

**The 3-2-1 rule**: at least **3** copies of the data, on **2** different media, **1** of them in another location (another building or the cloud). For protection against ransomware the modern recommendation is **3-2-1-1-0**: one more offline or immutable copy, and 0 errors in restore testing.

| Backup type | What is copied | Advantage | Drawback |
|---|---|---|---|
| **Full** | everything | simple and fast restore | much space and time |
| **Incremental** | what changed since the last backup of any type | fast, little space | the whole chain is needed to restore |
| **Differential** | what changed since the last full backup | 2 backups are enough to restore | grows over time |

**RPO** (Recovery Point Objective) — how much data, in time, we accept losing (for example, 24 hours → a daily backup). **RTO** (Recovery Time Objective) — how quickly the system must be restored. Both are set from business requirements and the risk assessment (M2).

**Testing restores**: an untested backup is not a backup. Regularly (for example, quarterly) restore random files and the whole system to a test environment.

## Windows backup tools

| Tool | What it saves | Where |
|---|---|---|
| **File History** | user folders, earlier versions of files | Settings or Control Panel → File History; an external disk or network folder |
| **Windows Backup** (Windows 11) | folders, settings, the list of apps — to OneDrive | the Windows Backup app |
| **System image** (Backup and Restore, Windows 7) | the whole disk, including the OS | Control Panel → Backup and Restore (Windows 7) → Create a system image |
| **Restore points** (System Restore) | system files and the registry (not personal files!) | `sysdm.cpl` → System Protection |
| **wbadmin** | backup and restore from the command line | `wbadmin start backup ...` (full support on Windows Server) |

**Practical work (on a test computer or in a virtual machine)**:

1. Connect an external disk and turn on **File History**; set the backup frequency (for example, every hour) and the retention period.
2. Create a test file, modify it and delete it; restore an earlier version through "Restore personal files".
3. Create a **system image** (Control Panel → Backup and Restore (Windows 7) → Create a system image) and prepare a recovery drive.
4. Enable system protection in `sysdm.cpl` and create a **restore point** manually.
5. In your report, justify an RPO and RTO for your home or a small office.

Note: if the backup disk is permanently connected, ransomware will encrypt it as well — disconnect it after the backup or use a cloud/network storage that supports versioning.

## Account types and privileges in Windows

| Account / mechanism | Description |
|---|---|
| **Administrator** | full control of the system: installing programs, settings, other accounts |
| **Standard user** | everyday work; cannot change system settings — **recommended for everyday work** |
| **UAC** (User Account Control) | even an administrator usually works with a restricted token; a privileged action asks for confirmation |
| **Guest, service accounts** | SYSTEM, LocalService, NetworkService — for services; the Guest account should be disabled |
| **User rights** (User Rights Assignment) | `secpol.msc` → Local Policies: "Log on locally", "Shut down the system", "Back up files and directories", "Debug programs" and others |

**Least privilege** (M11): everyday work in a standard account, the administrator account only for administration. Accounts and groups: `lusrmgr.msc` or `net user`, `net localgroup Administrators`.

## NTFS permissions

**Basic permissions**: Full control (full, including changing permissions), Modify (change and delete), Read & execute, List folder contents, Read, Write. Each of them is a set of **special permissions** (Advanced → Permissions).

**Rules**:

- **Inheritance**: permissions on a folder normally pass to the files and folders inside; it can be broken (Disable inheritance).
- **Explicit deny wins**: the permissions of the user and their groups are combined, but an explicit Deny removes that right.
- **Effective permissions**: the Advanced → Effective Access tab shows a user's actual rights.
- **Share + NTFS**: for network access the **more restrictive** of the share and NTFS permissions applies.
- **The owner** can always change the permissions.

On the command line:

```text
icacls D:\Reports
icacls D:\Reports /grant Buxgalteriya:(OI)(CI)M
icacls D:\Reports /deny Stajyorlar:(OI)(CI)W
icacls D:\Reports /inheritance:d
icacls D:\Reports /save permissions.txt /t
```

`(OI)(CI)` — inherit to files and folders, `M` — Modify; `/save` — save the permissions to a file (a backup before changes). (Buxgalteriya is the accounting group, Stajyorlar the interns.)

## Worked example: effective NTFS permissions

A simplified model: the rights are R (read), W (write), X (execute), D (delete). The first line holds the number of ACL entries, then the entries (`allow` or `deny`, a user or group, the rights). The following lines hold a user and their groups (comma-separated). Every user is also in the `Everyone` group. Effective rights = the union of all matching `allow` entries minus all matching `deny` entries. Show the rights lost because of a deny separately.

```python
import sys

TARTIB = "RWXD"
satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
a = int(satrlar[0][0])
acl = satrlar[1:1 + a]
for qator in satrlar[1 + a:]:
    kim = qator[0]
    guruhlar = set(qator[1].split(",")) | {kim, "Everyone"}   # the user + their groups + Everyone
    ruxsat, taqiq = set(), set()
    for tur, kimga, huquq in acl:
        if kimga in guruhlar:
            (ruxsat if tur == "allow" else taqiq).update(huquq)
    natija = "".join(h for h in TARTIB if h in ruxsat and h not in taqiq) or "-"   # deny wins
    rad = "".join(h for h in TARTIB if h in ruxsat and h in taqiq)
    print(f"{kim}: {natija}" + (f" (deny: {rad})" if rad else ""))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String TARTIB = "RWXD";
        int a = Integer.parseInt(in.next());
        List<String[]> acl = new ArrayList<>();
        for (int i = 0; i < a; i++) acl.add(new String[] {in.next(), in.next(), in.next()});
        while (in.hasNext()) {
            String kim = in.next();
            Set<String> guruhlar = new HashSet<>(Arrays.asList(in.next().split(",")));
            guruhlar.add(kim);                                   // the user + their groups + Everyone
            guruhlar.add("Everyone");
            Set<Character> ruxsat = new HashSet<>(), taqiq = new HashSet<>();
            for (String[] y : acl) {
                if (!guruhlar.contains(y[1])) continue;
                for (char h : y[2].toCharArray()) {
                    if (y[0].equals("allow")) ruxsat.add(h);
                    else taqiq.add(h);
                }
            }
            StringBuilder natija = new StringBuilder(), rad = new StringBuilder();
            for (char h : TARTIB.toCharArray()) {
                if (ruxsat.contains(h) && !taqiq.contains(h)) natija.append(h);   // deny wins
                if (ruxsat.contains(h) && taqiq.contains(h)) rad.append(h);
            }
            String qator = kim + ": " + (natija.length() > 0 ? natija : "-");
            if (rad.length() > 0) qator += " (deny: " + rad + ")";
            System.out.println(qator);
        }
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const string TARTIB = "RWXD";
    int a;
    cin >> a;
    vector<vector<string>> acl(a, vector<string>(3));
    for (auto& y : acl) cin >> y[0] >> y[1] >> y[2];
    string kim, gs;
    while (cin >> kim >> gs) {
        set<string> guruhlar = {kim, "Everyone"};               // the user + their groups + Everyone
        stringstream ss(gs);
        string g;
        while (getline(ss, g, ',')) guruhlar.insert(g);
        set<char> ruxsat, taqiq;
        for (const auto& y : acl) {
            if (!guruhlar.count(y[1])) continue;
            for (char h : y[2]) (y[0] == "allow" ? ruxsat : taqiq).insert(h);
        }
        string natija, rad;
        for (char h : TARTIB) {
            if (ruxsat.count(h) && !taqiq.count(h)) natija += h;   // deny wins
            if (ruxsat.count(h) && taqiq.count(h)) rad += h;
        }
        cout << kim << ": " << (natija.empty() ? "-" : natija);
        if (!rad.empty()) cout << " (deny: " << rad << ")";
        cout << endl;
    }
    return 0;
}
```

Input:

```text
5
allow Everyone R
allow Buxgalteriya RW
allow Administrators RWXD
deny Stajyorlar WD
allow ali X
ali Buxgalteriya
vali Buxgalteriya,Stajyorlar
olim Administrators
sardor Stajyorlar
kamol Users
```

Output:

```text
ali: RWX
vali: R (deny: W)
olim: RWXD
sardor: R
kamol: R
```

vali got the write right from the accounting group, but is also an intern — the explicit deny won and write was removed. The X given to ali personally was added to the group rights. The read right given to `Everyone` applies to everyone, including the outsider kamol — for confidential folders this is a mistake. In real systems the "Effective Access" tab does the same calculation (and additionally takes share permissions, ownership and privileges into account).

:::warning
**Common mistakes**

- **Never test-restoring a backup.** On the day of the disaster it turns out the backup does not work.
- **Leaving the backup disk permanently connected.** Ransomware encrypts it too.
- **Treating a restore point as a backup.** It does not save personal files.
- **Doing everyday work in an administrator account.** Malware then also runs with administrator rights.
- **Giving Full control to Everyone or Users.** Grant rights to groups and only at the level needed; use Deny rarely and deliberately.
:::

:::tip
Before changing permissions, save the current state with `icacls folder /save backup.txt /t` — after a mistake, return it with `icacls folder /restore backup.txt`. This is a "backup for permissions".
:::

## Summary

- Backups: 3-2-1 (or 3-2-1-1-0), full, incremental and differential backups; RPO and RTO from business requirements; restores are tested regularly.
- Windows: File History, Windows Backup, system image, restore points, wbadmin; the backup disk is disconnected after copying.
- Privileges: administrator and standard accounts, UAC, user rights; everyday work in a standard account.
- NTFS: basic and special permissions, inheritance, Deny wins, effective permissions; over the network the more restrictive of share and NTFS.
- `icacls` — viewing, granting, denying and saving permissions.

## Check yourself

:::check
Q: What does the 3-2-1 rule mean?
A: At least 3 copies, on 2 different media, 1 of them in another location.
Q: What is the difference between incremental and differential backups?
A: Incremental saves what changed since the last backup of any type, differential since the last full backup.
Q: A user has Allow Write from one group and Deny Write from another. Can they write?
A: No, the explicit deny wins.
Q: For network access the share permission is Read and the NTFS permission is Modify. What is the effective right?
A: Read — the more restrictive of the two.
Q: Why is everyday work recommended in a standard account?
A: Malware or mistakes run with the user's rights; a standard account cannot change the system.
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/icacls
Microsoft Learn: icacls
A description of the icacls command, permissions and inheritance flags.
:::
