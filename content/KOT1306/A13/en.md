---
title: Creating and using shared network resources
summary: Shared folders and the SMB protocol; sharing a folder on the network — the graphical way, net share, PowerShell; UNC paths and mapping a network drive; share and NTFS permissions and how they work together; hidden and administrative shares; a shared printer; Samba on Linux; security; calculating the effective network permission.
---

## Why it matters

A department's teachers keep lecture materials in one folder, students read them but cannot change them; into the assignments folder a student uploads only their own file. For this you need no flash drives or e-mail — **shared network resources** (shared folders) and the right permissions are enough. A badly configured shared folder, on the other hand, is the most common route for data leaks and ransomware.

:::note
**After this session you will be able to:**

- share a folder on the network and reach it through a UNC path;
- map a network drive;
- explain the difference between share and NTFS permissions and how they work together;
- calculate a user's effective rights over the network and locally;
- apply the rules for configuring shared resources securely.
:::

## SMB and UNC paths

In Windows the protocol for sharing files and printers is **SMB** (Server Message Block, TCP 445). The current version is **SMB 3** (with encryption); the old **SMB 1** is dangerous (the WannaCry virus used it) and must be turned off.

A shared resource is reached with a **UNC** (Universal Naming Convention) path:

```text
\\SRV-DC01\Kurslar                  server name and share name
\\192.168.50.10\Kurslar\KOT1306     by IP address, with a subfolder
\\SRV-DC01\C$                       an administrative share (administrators only)
```

## Sharing a folder

**The graphical way**: folder → Properties → **Sharing** → **Advanced Sharing** → "Share this folder" → name → **Permissions** (share permissions). On a server: Server Manager → File and Storage Services → Shares → **New Share**.

**The command line**:

```text
net share Kurslar=D:\Kurslar /grant:Talabalar,READ /grant:Oqituvchilar,FULL
net share                                    list all shares
net use Z: \\SRV-DC01\Kurslar /persistent:yes   map a network drive
net use                                      mapped drives
net share Kurslar /delete
```

**PowerShell**:

```text
New-SmbShare -Name "Kurslar" -Path "D:\Kurslar" -ReadAccess "AIFU\Talabalar" -FullAccess "AIFU\Oqituvchilar"
Get-SmbShare
Get-SmbShareAccess -Name "Kurslar"
New-PSDrive -Name Z -PSProvider FileSystem -Root \\SRV-DC01\Kurslar -Persist
```

A name ending in `$` (`Arxiv$`) is a **hidden share**: it is not visible on the network, but anyone who knows the name can connect (this is a convenience, not protection). `C$`, `ADMIN$`, `IPC$` are **administrative** shares.

## Share and NTFS permissions

| | Share permissions | NTFS permissions |
|---|---|---|
| When they apply | only for access **over the network** | always — over the network and locally |
| Levels | Read, Change, Full Control | Read, Read & execute, Modify, Full control and others |
| Where they are set | on the whole share | on every folder and file, with inheritance |

**The key rule**: over the network the effective right is the **most restrictive** of the share and NTFS permissions. Within each, the permissions of the user and their groups add up (Deny wins).

The modern advice: make the share permission broad (for example `Authenticated Users — Change` or `Full`) and set the real restrictions with the more detailed **NTFS** permissions. Then permissions are managed in one place.

## A shared printer and Linux

- **Printer**: printer properties → Sharing → "Share this printer"; on the client add `\\SRV-DC01\Printer1`. In a domain — connect it automatically through a GPO.
- **Linux**: a **Samba** server provides SMB shares to Windows clients (`/etc/samba/smb.conf`); **NFS** is also used between Linux machines. A Linux client mounts a Windows share with `mount -t cifs //server/Kurslar /mnt/kurslar`.

## Practical work

1. On `SRV-DC01` create the folder `D:\Kurslar` with the folders `Materiallar` (materials) and `Topshiriqlar` (assignments) inside.
2. Groups: `Talabalar`, `Oqituvchilar` (from A12).
3. The share `Kurslar`: share permission — `Authenticated Users: Change`.
4. NTFS: `Oqituvchilar` — Modify (on the whole folder); `Talabalar` — Read & execute on `Materiallar`, Write on `Topshiriqlar` (to create files without reading other people's — "This folder only" + CREATOR OWNER — Modify).
5. On a client, open `\\SRV-DC01\Kurslar` with a student and a teacher account and try every action (read, create, delete).
6. On the server, **Computer Management → Shared Folders → Sessions / Open Files** — see who is connected and which files are open.

## Worked example: the effective network permission

The scale of levels: `None` < `Read` < `Change` < `Full` (NTFS Modify corresponds to `Change`). The first line holds the number of groups and then each group (name and members). Then the number of share entries and the entries (subject, level), then the number of NTFS entries and the entries. Finally the number of users and their names. For each user: the highest share level, the highest NTFS level (`Everyone` applies to all), over the network — the smaller of the two, locally — the NTFS level.

```python
DARAJA = ["None", "Read", "Change", "Full"]

guruhlar = {}                                    # user -> groups
for _ in range(int(input())):
    qism = input().split()
    for azo in qism[1:]:
        guruhlar.setdefault(azo, set()).add(qism[0])
share = [input().split() for _ in range(int(input()))]
ntfs = [input().split() for _ in range(int(input()))]


def eng_yuqori(yozuvlar, subyektlar):
    d = 0
    for subyekt, daraja in yozuvlar:
        if subyekt in subyektlar:
            d = max(d, DARAJA.index(daraja))     # permissions add up
    return d


for _ in range(int(input())):
    user = input().strip()
    subyektlar = {user, "Everyone"} | guruhlar.get(user, set())
    s = eng_yuqori(share, subyektlar)
    n = eng_yuqori(ntfs, subyektlar)
    tarmoq = min(s, n)                           # the most restrictive
    print(user + ": network " + DARAJA[tarmoq] + ", local " + DARAJA[n])
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
    static final List<String> DARAJA = Arrays.asList("None", "Read", "Change", "Full");

    static int engYuqori(List<String[]> yozuvlar, Set<String> subyektlar) {
        int d = 0;
        for (String[] e : yozuvlar) {
            if (subyektlar.contains(e[0])) d = Math.max(d, DARAJA.indexOf(e[1]));   // permissions add up
        }
        return d;
    }

    static List<String[]> oqi(Scanner in) {
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> yozuvlar = new ArrayList<>();
        for (int i = 0; i < n; i++) yozuvlar.add(in.nextLine().trim().split(" "));
        return yozuvlar;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Set<String>> guruhlar = new HashMap<>();          // user -> groups
        for (String[] qism : oqi(in)) {
            for (int j = 1; j < qism.length; j++) {
                guruhlar.computeIfAbsent(qism[j], k -> new HashSet<>()).add(qism[0]);
            }
        }
        List<String[]> share = oqi(in);
        List<String[]> ntfs = oqi(in);
        int q = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < q; i++) {
            String user = in.nextLine().trim();
            Set<String> subyektlar = new HashSet<>(guruhlar.getOrDefault(user, new HashSet<>()));
            subyektlar.add(user);
            subyektlar.add("Everyone");
            int s = engYuqori(share, subyektlar);
            int n = engYuqori(ntfs, subyektlar);
            int tarmoq = Math.min(s, n);                               // the most restrictive
            System.out.println(user + ": network " + DARAJA.get(tarmoq) + ", local " + DARAJA.get(n));
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const vector<string> DARAJA = {"None", "Read", "Change", "Full"};

vector<vector<string>> oqi() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    vector<vector<string>> yozuvlar;
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream ss(satr);
        vector<string> qism;
        string s;
        while (ss >> s) qism.push_back(s);
        yozuvlar.push_back(qism);
    }
    return yozuvlar;
}

int engYuqori(const vector<vector<string>>& yozuvlar, const set<string>& subyektlar) {
    int d = 0;
    for (auto& e : yozuvlar) {
        if (subyektlar.count(e[0])) {
            int indeks = find(DARAJA.begin(), DARAJA.end(), e[1]) - DARAJA.begin();
            d = max(d, indeks);                                        // permissions add up
        }
    }
    return d;
}

int main() {
    map<string, set<string>> guruhlar;                                 // user -> groups
    for (auto& qism : oqi()) {
        for (size_t j = 1; j < qism.size(); j++) guruhlar[qism[j]].insert(qism[0]);
    }
    auto share = oqi();
    auto ntfs = oqi();
    for (auto& qator : oqi()) {
        string user = qator[0];
        set<string> subyektlar = guruhlar[user];
        subyektlar.insert(user);
        subyektlar.insert("Everyone");
        int s = engYuqori(share, subyektlar);
        int n = engYuqori(ntfs, subyektlar);
        int tarmoq = min(s, n);                                        // the most restrictive
        cout << user << ": network " << DARAJA[tarmoq] << ", local " << DARAJA[n] << endl;
    }
    return 0;
}
```

Input:

```text
2
Talabalar ali vali
Oqituvchilar nodir
2
Everyone Read
Oqituvchilar Full
3
Talabalar Change
Oqituvchilar Change
Administrators Full
3
ali
nodir
mehmon
```

Output:

```text
ali: network Read, local Change
nodir: network Change, local Change
mehmon: network None, local None
```

`ali` can change files by NTFS, but over the network only reads — the share permission (Everyone: Read) limits him. For `nodir` the share is Full but NTFS is Change — the result is Change. `mehmon` (a guest) is allowed to read by the share, but NTFS grants him nothing — he cannot get in.

:::warning
**Common mistakes**

- **Configuring only share permissions.** They do not apply to local access (or via RDP) — the main protection is in NTFS.
- **Leaving `Everyone — Full Control` on both the share and NTFS.** Anyone on the network, or ransomware, can wipe the folder.
- **Taking a hidden share (`$`) for protection.** It is only absent from the list; anyone who knows the name can connect.
- **Leaving SMB 1 enabled.** An old and weak protocol; check with `Get-SmbServerConfiguration` and turn it off.
- **Opening port 445 to the Internet.** SMB is used only on the internal network or over a VPN.
:::

:::tip
Share a folder between two computers (or two VMs) and watch the connection in Wireshark with the `smb2` filter: find the Negotiate (version agreement), Session Setup (authentication), Tree Connect (connecting to the share) and Create/Read requests.
:::

## Summary

- The Windows file-sharing protocol is SMB (TCP 445); access is by the UNC path `\\server\share`.
- Sharing: Advanced Sharing, `net share`, `New-SmbShare`; a network drive: `net use Z:`.
- Share permissions apply only over the network, NTFS always; the effective right over the network is the more restrictive of the two.
- The advice: a broad share permission, restrictions in NTFS; SMB 1 off, port 445 closed to the Internet.

## Check yourself

:::check
Q: How is a shared folder reached over the network in Windows?
A: With a UNC path: `\\server\folder`.
Q: What is the Windows file-sharing protocol called?
A: SMB (Server Message Block).
Q: The share permission is Full and the NTFS permission is Read. What is the effective right over the network?
A: Read — the more restrictive of share and NTFS applies.
Q: What is special about a share named `Arxiv$`?
A: It is hidden — not visible in the network list, but a user who knows the name can connect.
Q: How can a network drive be mapped from the command line?
A: `net use Z: \\SRV-DC01\Kurslar`.
:::

:::link https://learn.microsoft.com/en-us/windows-server/storage/file-server/file-server-smb-overview
SMB overview
Microsoft's overview of the SMB protocol and the file server.
:::
