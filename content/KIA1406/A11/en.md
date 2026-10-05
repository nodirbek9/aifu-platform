---
title: Managing file and directory permissions in Linux
summary: Managing file and directory permissions in Linux in practice — users, groups, /etc/passwd and /etc/shadow; the output of ls -l; the meaning of r, w, x for files and directories; chmod (symbolic and numeric), chown, chgrp; umask and the default permissions of new files; special bits — setuid, setgid, sticky; ACLs (getfacl, setfacl); sudo and least privilege; the order of permission checks (owner → group → others); a programming task — converting an rwx string to octal.
---

## Why does this topic matter?

Linux is the main system of servers, the cloud and containers. The DAC model from M11 is implemented in Linux through each file's **owner**, **group** and three permission triples. Wrong permissions are one of the most common mistakes: an SSH private key readable by everyone, a web directory writable by everyone or an unnecessary setuid program opens the way to a full system takeover.

:::note
**After this session you will be able to:**

- read the output of `ls -l` and explain file and directory permissions;
- change permissions with `chmod`, `chown`, `chgrp`, `umask`;
- know the purpose and risks of the setuid, setgid and sticky bits;
- show in which order the kernel checks access rights;
- write a program converting an `rwx` string to octal (task A11 on the platform).
:::

:::warning
Do the practical work in a Linux virtual machine or in WSL, in your own test directory. Do not try `chmod -R` and `chown -R` on system directories such as `/etc` or `/usr` — the system may stop working.
:::

## Permission basics

```text
-rwxr-x--- 1 ali dev 4096 Oct  5 10:00 skript.sh
```

The first character is the type (`-` file, `d` directory, `l` symbolic link); then three triples: the **owner** (ali), the **group** (dev), **others**. Each triple holds `r`, `w`, `x` or `-`.

| Permission | For a file | For a directory |
|---|---|---|
| `r` (4) | read the contents | list the names inside (`ls`) |
| `w` (2) | modify the contents | create, delete and rename files inside |
| `x` (1) | execute as a program | enter it (`cd`) and access the files |

Note: deleting a file requires the `w` permission not of the file but of the **directory**. Users are stored in `/etc/passwd` (readable by everyone), and password hashes in `/etc/shadow` (root only).

## Practical work

**Step 1. Observe.**

```text
id
ls -l /etc/passwd /etc/shadow
ls -ld /tmp ~
stat file.txt
```

`id` shows the current user and their groups; notice the `t` (sticky bit) on `/tmp`.

**Step 2. chmod, chown, chgrp.**

```text
mkdir ~/test && cd ~/test
echo hello > file.txt
chmod u=rw,g=r,o= file.txt      # symbolic form
chmod 640 file.txt              # the same, numeric form
chmod +x script.sh              # add execute permission for everyone
sudo chown ali:dev file.txt     # change the owner and group
chgrp dev file.txt
```

After each command check the result with `ls -l`. Try reading as another user (`sudo -u olim cat file.txt`).

**Step 3. umask.** The `umask` command prints the current mask (usually `0022`). Run `umask 077`, create a new file and look at its permissions — `rw-------`, for the owner only.

**Step 4. Special bits.**

| Bit | Number | Meaning on a file | Meaning on a directory |
|---|---|---|---|
| **setuid** | 4000 | the program runs with the **owner's** rights (`/usr/bin/passwd` — root) | — |
| **setgid** | 2000 | the program runs with the group's rights | new files inherit the directory's group (a team folder) |
| **sticky** | 1000 | — | a file can be deleted only by its owner (`/tmp`) |

`find / -perm -4000 -type f 2>/dev/null` lists all setuid programs on the system. An unfamiliar setuid-root program is a serious risk: a bug in it lets an ordinary user become root.

**Step 5. ACLs.** When the triples are not enough (for example, read access for one extra user):

```text
setfacl -m u:olim:r file.txt
getfacl file.txt
```

A `+` sign appears at the end of the permissions in `ls -l`.

**Step 6. sudo.** Do not work as root all the time; run only the needed command with `sudo`. Edit `/etc/sudoers` only through `visudo`; `sudo -l` shows the commands you are allowed to run.

**Step 7. Program.** Complete the task **Linux permissions: from rwx to octal** on the platform.

## Worked example 1: umask and default permissions

A new file "requests" permissions 666 (`rw-rw-rw-`), a new directory 777; the bits in the **umask** are removed: permissions = requested AND (NOT umask). Each item is a umask (octal); print the permissions of a new file and directory in octal.

```python
import sys

for t in sys.stdin.read().split():
    niqob = int(t, 8)                             # read an octal number
    fayl = 0o666 & ~niqob                         # the bits of the mask are removed
    katalog = 0o777 & ~niqob
    print(f"umask {t}: file {fayl:03o}, directory {katalog:03o}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int niqob = Integer.parseInt(t, 8);                // read an octal number
            int fayl = 0666 & ~niqob;                          // the bits of the mask are removed
            int katalog = 0777 & ~niqob;
            System.out.println(String.format("umask %s: file %03o, directory %03o", t, fayl, katalog));
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
    string t;
    while (cin >> t) {
        int niqob = stoi(t, nullptr, 8);                       // read an octal number
        int fayl = 0666 & ~niqob;                              // the bits of the mask are removed
        int katalog = 0777 & ~niqob;
        printf("umask %s: file %03o, directory %03o\n", t.c_str(), fayl, katalog);
    }
    return 0;
}
```

Input:

```text
022 002 077 027 000
```

Output:

```text
umask 022: file 644, directory 755
umask 002: file 664, directory 775
umask 077: file 600, directory 700
umask 027: file 640, directory 750
umask 000: file 666, directory 777
```

`022` is the default on many systems: others can read but not write. `077` is for users working with confidential data: new files are open to the owner only. `000` is dangerous: anyone can modify a new file. Note that umask never adds `x` to files — for an executable file `chmod +x` must be run explicitly.

## Worked example 2: the order of access checks

The Linux kernel checks access in the following order and applies **only one** triple: if the user is the owner — the owner triple (the group and others triples are no longer considered!); otherwise, if they are in the group — the group triple; otherwise — the others triple. root always gets `r` and `w`, and `x` if at least one `x` bit is set. Input: *m* files (name owner group octal_mode), *u* users (name group1,group2), then requests (user operation file). Print the decision and which triple was applied.

```python
import sys

BIT = {"r": 4, "w": 2, "x": 1}
satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
i = 0
fayllar = {}
m = int(satrlar[i][0])
i += 1
for _ in range(m):
    nom, egasi, guruh, rejim = satrlar[i]
    i += 1
    fayllar[nom] = (egasi, guruh, int(rejim, 8))
guruhlari = {}
u = int(satrlar[i][0])
i += 1
for _ in range(u):
    kim, gs = satrlar[i]
    i += 1
    guruhlari[kim] = gs.split(",")
for kim, amal, nom in satrlar[i:]:
    egasi, guruh, rejim = fayllar[nom]
    if kim == "root":
        ok = amal != "x" or rejim & 0o111 != 0        # for root, x needs at least one x bit
        sinf = "root"
    elif kim == egasi:                                # only the owner triple
        ok = (rejim >> 6) & BIT[amal] != 0
        sinf = "owner"
    elif guruh in guruhlari[kim]:
        ok = (rejim >> 3) & BIT[amal] != 0
        sinf = "group"
    else:
        ok = rejim & BIT[amal] != 0
        sinf = "others"
    print(f"{kim} {amal} {nom}: " + ("allow" if ok else "deny") + f" ({sinf})")
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String[]> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s.split("\\s+"));
        }
        Map<String, Integer> BIT = Map.of("r", 4, "w", 2, "x", 1);
        int i = 0;
        Map<String, String[]> fayllar = new HashMap<>();
        int m = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < m; j++) {
            String[] q = satrlar.get(i++);
            fayllar.put(q[0], q);
        }
        Map<String, List<String>> guruhlari = new HashMap<>();
        int u = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < u; j++) {
            String[] q = satrlar.get(i++);
            guruhlari.put(q[0], Arrays.asList(q[1].split(",")));
        }
        for (; i < satrlar.size(); i++) {
            String kim = satrlar.get(i)[0], amal = satrlar.get(i)[1], nom = satrlar.get(i)[2];
            String[] f = fayllar.get(nom);
            int rejim = Integer.parseInt(f[3], 8);
            int bit = BIT.get(amal);
            boolean ok;
            String sinf;
            if (kim.equals("root")) {
                ok = !amal.equals("x") || (rejim & 0111) != 0;    // for root, x needs at least one x bit
                sinf = "root";
            } else if (kim.equals(f[1])) {                       // only the owner triple
                ok = ((rejim >> 6) & bit) != 0;
                sinf = "owner";
            } else if (guruhlari.get(kim).contains(f[2])) {
                ok = ((rejim >> 3) & bit) != 0;
                sinf = "group";
            } else {
                ok = (rejim & bit) != 0;
                sinf = "others";
            }
            System.out.println(kim + " " + amal + " " + nom + ": " + (ok ? "allow" : "deny") + " (" + sinf + ")");
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
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
    map<string, int> BIT = {{"r", 4}, {"w", 2}, {"x", 1}};
    size_t i = 0;
    map<string, vector<string>> fayllar;
    int m = stoi(satrlar[i++][0]);
    for (int j = 0; j < m; j++, i++) fayllar[satrlar[i][0]] = satrlar[i];
    map<string, vector<string>> guruhlari;
    int u = stoi(satrlar[i++][0]);
    for (int j = 0; j < u; j++, i++) {
        stringstream gs(satrlar[i][1]);
        string g;
        while (getline(gs, g, ',')) guruhlari[satrlar[i][0]].push_back(g);
    }
    for (; i < satrlar.size(); i++) {
        const string& kim = satrlar[i][0];
        const string& amal = satrlar[i][1];
        const string& nom = satrlar[i][2];
        const vector<string>& f = fayllar[nom];
        int rejim = stoi(f[3], nullptr, 8);
        int bit = BIT[amal];
        vector<string>& gl = guruhlari[kim];
        bool ok;
        string sinf;
        if (kim == "root") {
            ok = amal != "x" || (rejim & 0111) != 0;             // for root, x needs at least one x bit
            sinf = "root";
        } else if (kim == f[1]) {                                // only the owner triple
            ok = ((rejim >> 6) & bit) != 0;
            sinf = "owner";
        } else if (find(gl.begin(), gl.end(), f[2]) != gl.end()) {
            ok = ((rejim >> 3) & bit) != 0;
            sinf = "group";
        } else {
            ok = (rejim & bit) != 0;
            sinf = "others";
        }
        cout << kim << " " << amal << " " << nom << ": " << (ok ? "allow" : "deny") << " (" << sinf << ")" << endl;
    }
    return 0;
}
```

Input:

```text
3
hisobot.txt ali dev 640
skript.sh vali dev 750
g.txt ali dev 074
4
ali dev
vali dev,docker
olim users
root root
vali r hisobot.txt
vali w hisobot.txt
olim r hisobot.txt
vali x skript.sh
olim x skript.sh
ali r g.txt
vali r g.txt
olim r g.txt
root w hisobot.txt
root x hisobot.txt
```

Output:

```text
vali r hisobot.txt: allow (group)
vali w hisobot.txt: deny (group)
olim r hisobot.txt: deny (others)
vali x skript.sh: allow (owner)
olim x skript.sh: deny (others)
ali r g.txt: deny (owner)
vali r g.txt: allow (group)
olim r g.txt: allow (others)
root w hisobot.txt: allow (root)
root x hisobot.txt: deny (root)
```

The most interesting case is `g.txt` (074): ali is its **owner** and also in the dev group, yet cannot read the file, because the owner triple is `---` and the group and others triples do not apply to them. At the same time the outsider olim can read it as "others". Such "inverted" permissions are rare in practice, but they show the check order well. Even root cannot execute a file that has no `x` bit at all — this prevents accidentally running a data file.

:::warning
**Common mistakes**

- **"It doesn't work — chmod 777".** This hides the problem and gives everyone write access; grant exactly the permission needed.
- **Wide permissions on SSH keys.** `~/.ssh` — 700, the private key — 600; otherwise SSH refuses to use the key.
- **The file is protected, but the directory is open.** Anyone with `w` on the directory can delete and replace files.
- **Using `chmod -R` without thinking.** Directories and files need different permissions (`x` for directories, not files); set them separately with `find -type d` / `-type f`.
- **Working as root all the time.** Every mistake or malicious script affects the whole system; use `sudo`.
:::

:::tip
`namei -l /var/www/html/index.html` shows the permissions of every directory along the path — the fastest way to find the cause of "Permission denied". `stat -c '%a %A %U %G %n' *` prints file permissions in octal and symbolic form together.
:::

## Summary

- Every file has an owner, a group and three triples (owner, group, others); `r`, `w`, `x` mean different things for files and directories.
- `chmod` (symbolic and numeric), `chown`, `chgrp`; the permissions of new files are 666/777 AND NOT umask.
- Special bits: setuid (4000), setgid (2000), sticky (1000); audit setuid programs regularly.
- The kernel applies only one triple: owner → group → others.
- ACLs (`setfacl`, `getfacl`) for fine-grained permissions; `sudo` for least privilege.

## Check yourself

:::check
Q: Which permission is needed to delete a file in a directory?
A: The directory's w (and x) permission; the file's own permissions do not matter (unless the sticky bit is set).
Q: With umask 027, what are the permissions of a new file and directory?
A: File 640, directory 750.
Q: If the owner triple is --- and the group triple is rwx, can the owner, who is also in the group, read the file?
A: No, only the owner triple applies to the owner.
Q: What does the sticky bit on /tmp provide?
A: Anyone can create files, but a file can be deleted only by its owner (or root).
Q: Why is an unfamiliar setuid-root program dangerous?
A: It runs with root rights; a vulnerability in it lets an ordinary user become root.
:::

:::link https://man7.org/linux/man-pages/man1/chmod.1.html
chmod(1) — Linux manual page
The chmod command, symbolic and numeric modes, special bits.
:::
