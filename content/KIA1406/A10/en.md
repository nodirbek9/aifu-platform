---
title: Configuring user authentication and password policy in Windows
summary: Configuring password and account lockout policies in Windows in practice — local security policy (secpol.msc), the net accounts command, group policy (gpedit.msc, GPO); password policy settings — history, maximum and minimum age, minimum length, complexity; lockout settings — threshold, duration, counter reset; the event log (4624, 4625, 4740); Windows Hello and LAPS; modelling lockout and password history logic in a program; a programming task — rating password strength.
---

## Why does this topic matter?

In M10 we covered the theory of passwords and authentication. In an organization these rules are not left to each user's discretion — they are enforced centrally as a **policy**. Windows is the most widespread desktop system in organizations, so being able to configure its password and lockout policies is a core skill for every system administrator and security specialist.

:::note
**After this session you will be able to:**

- configure the Windows password policy and account lockout policy settings;
- use `net accounts`, `secpol.msc` and group policy;
- find failed logons and lockouts in the event log;
- show in a program how lockout and password history work;
- write a program that rates password strength (task A10 on the platform).
:::

:::warning
Do the practical work in a **virtual machine** or on a training computer. Before changing security settings on a work or personal computer, write down the current values; a wrong lockout policy can lock you out of the system as well.
:::

## Password policy settings

`secpol.msc` → Account Policies → **Password Policy** (the Windows Home edition does not have this window — use the `net accounts` command):

| Setting | Meaning | Recommendation (CIS / Microsoft baseline) |
|---|---|---|
| Enforce password history | how many recent passwords cannot be reused | 24 |
| Maximum password age | after how many days a password expires | 365 or unlimited (NIST: change only on evidence of compromise) |
| Minimum password age | how soon a password can be changed again | 1 day — prevents "cycling" through the history |
| Minimum password length | minimum length | 14 |
| Password must meet complexity requirements | 3 character types, must not contain the user name | enabled (an old requirement; length matters more) |
| Store passwords using reversible encryption | store passwords in a recoverable form | **disabled** — otherwise the password is effectively stored in clear text |

**Account Lockout Policy**:

| Setting | Meaning | Recommendation |
|---|---|---|
| Account lockout threshold | after how many failed attempts the account is locked | 5–10 (the default in Windows 11 is 10) |
| Account lockout duration | how many minutes the lockout lasts | 15 |
| Reset account lockout counter after | after how many minutes the failure counter returns to zero | 15 |

Lockout slows down brute force, but a very low threshold (1–2) creates a **denial of service** risk: an attacker can deliberately enter wrong passwords and lock out every employee.

## Practical work

**Step 1. View the current policy.** In a command prompt with administrator rights:

```text
net accounts
net user
net user student1
```

`net accounts` prints the minimum length, ages, history and lockout values; `net user student1` shows when the account last changed its password and when it expires.

**Step 2. A test user and a policy.** Create a test account and set the policy:

```text
net user student1 * /add
net accounts /minpwlen:12 /uniquepw:24 /minpwage:1 /maxpwage:unlimited
net accounts /lockoutthreshold:5 /lockoutduration:15 /lockoutwindow:15
```

Do the same in graphical form through `secpol.msc` (or, on a domain network, a GPO in `gpmc.msc`: Computer Configuration → Windows Settings → Security Settings → Account Policies). Use `gpupdate /force` to apply a domain policy and `gpresult /r` to check the result.

**Step 3. Test the policy.** Try to give `student1` a short password (for example `abc123`) — the system rejects it. Then try to log in as `student1` 5 times with a wrong password — the account gets locked.

**Step 4. Event log.** `eventvwr.msc` → Windows Logs → **Security**; use a filter to find these events:

| Event ID | Event |
|---|---|
| 4624 | successful logon |
| 4625 | failed logon attempt |
| 4740 | account locked out |
| 4723 / 4724 | user changed the password / administrator reset the password |

In PowerShell: `Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 10`. Many 4625 events for many accounts from one address are a sign of password spraying.

**Step 5. Modern tools.** **Windows Hello** (PIN or biometrics) is a device-bound key stored in the TPM; the PIN is never sent over the network. **LAPS** (Local Administrator Password Solution) makes the local administrator password of every computer random and unique — preventing the takeover of a whole network with one shared password.

**Step 6. Program.** Complete the task **Password policy: rating password strength** on the platform.

## Worked example 1: the account lockout policy

The first line holds the policy: lockout threshold, observation window (minutes) and lockout duration (minutes). The following lines hold login attempts: time (HH:MM), user, result (`OK` or `FAIL`). Any attempt on a locked account is denied and not counted; failures older than the window are forgotten; a successful login resets the counter.

```python
import sys


def daqiqa(v):
    s, d = v.split(":")
    return int(s) * 60 + int(d)


def vaqt(m):
    return f"{m // 60:02d}:{m % 60:02d}"


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
chegara, oyna, muddat = map(int, satrlar[0])
xatolar, blok = {}, {}                          # user -> failure times / lockout end time
for v, kim, natija in satrlar[1:]:
    t = daqiqa(v)
    if kim in blok and t < blok[kim]:
        print(f"{v} {kim}: denied - locked")
        continue
    if natija == "OK":
        xatolar[kim] = []                       # a successful login resets the counter
        print(f"{v} {kim}: login OK")
        continue
    royxat = [x for x in xatolar.get(kim, []) if t - x < oyna] + [t]   # failures inside the window
    if len(royxat) >= chegara:
        blok[kim] = t + muddat
        xatolar[kim] = []
        print(f"{v} {kim}: LOCKED (until {vaqt(t + muddat)})")
    else:
        xatolar[kim] = royxat
        print(f"{v} {kim}: failed ({len(royxat)}/{chegara})")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static int daqiqa(String v) {
        String[] q = v.split(":");
        return Integer.parseInt(q[0]) * 60 + Integer.parseInt(q[1]);
    }

    static String vaqt(int m) {
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int chegara = in.nextInt(), oyna = in.nextInt(), muddat = in.nextInt();
        Map<String, List<Integer>> xatolar = new HashMap<>();   // user -> failure times / lockout end time
        Map<String, Integer> blok = new HashMap<>();
        while (in.hasNext()) {
            String v = in.next(), kim = in.next(), natija = in.next();
            int t = daqiqa(v);
            if (blok.containsKey(kim) && t < blok.get(kim)) {
                System.out.println(v + " " + kim + ": denied - locked");
                continue;
            }
            if (natija.equals("OK")) {
                xatolar.put(kim, new ArrayList<>());            // a successful login resets the counter
                System.out.println(v + " " + kim + ": login OK");
                continue;
            }
            List<Integer> royxat = new ArrayList<>();
            for (int x : xatolar.getOrDefault(kim, new ArrayList<>())) {
                if (t - x < oyna) royxat.add(x);                 // failures inside the window
            }
            royxat.add(t);
            if (royxat.size() >= chegara) {
                blok.put(kim, t + muddat);
                xatolar.put(kim, new ArrayList<>());
                System.out.println(v + " " + kim + ": LOCKED (until " + vaqt(t + muddat) + ")");
            } else {
                xatolar.put(kim, royxat);
                System.out.println(v + " " + kim + ": failed (" + royxat.size() + "/" + chegara + ")");
            }
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int daqiqa(const string& v) {
    return stoi(v.substr(0, 2)) * 60 + stoi(v.substr(3, 2));
}

string vaqt(int m) {
    char buf[8];
    snprintf(buf, sizeof buf, "%02d:%02d", m / 60, m % 60);
    return buf;
}

int main() {
    int chegara, oyna, muddat;
    cin >> chegara >> oyna >> muddat;
    map<string, vector<int>> xatolar;                            // user -> failure times / lockout end time
    map<string, int> blok;
    string v, kim, natija;
    while (cin >> v >> kim >> natija) {
        int t = daqiqa(v);
        if (blok.count(kim) && t < blok[kim]) {
            cout << v << " " << kim << ": denied - locked" << endl;
            continue;
        }
        if (natija == "OK") {
            xatolar[kim].clear();                                // a successful login resets the counter
            cout << v << " " << kim << ": login OK" << endl;
            continue;
        }
        vector<int> royxat;
        for (int x : xatolar[kim]) {
            if (t - x < oyna) royxat.push_back(x);               // failures inside the window
        }
        royxat.push_back(t);
        if ((int) royxat.size() >= chegara) {
            blok[kim] = t + muddat;
            xatolar[kim].clear();
            cout << v << " " << kim << ": LOCKED (until " << vaqt(t + muddat) << ")" << endl;
        } else {
            xatolar[kim] = royxat;
            cout << v << " " << kim << ": failed (" << royxat.size() << "/" << chegara << ")" << endl;
        }
    }
    return 0;
}
```

Input:

```text
3 15 15
09:00 ali FAIL
09:02 ali FAIL
09:03 vali OK
09:05 ali FAIL
09:10 ali OK
09:21 ali OK
09:30 vali FAIL
09:50 vali FAIL
09:52 vali FAIL
```

Output:

```text
09:00 ali: failed (1/3)
09:02 ali: failed (2/3)
09:03 vali: login OK
09:05 ali: LOCKED (until 09:20)
09:10 ali: denied - locked
09:21 ali: login OK
09:30 vali: failed (1/3)
09:50 vali: failed (1/3)
09:52 vali: failed (2/3)
```

ali was locked for 15 minutes after the third failure; even the **correct** password at 09:10 was not accepted — during a lockout the system does not check the password at all, otherwise an attacker could continue guessing on a locked account. vali's failure at 09:30 had left the 15-minute window by 09:50 — this is exactly the "Reset account lockout counter after" setting.

## Worked example 2: password history

The first line holds the history size *N*, the following lines password change requests (user new_password). The system stores not the passwords themselves but their hashes (for teaching purposes — FNV-1a with the user name added; Windows stores the NT hash). If the new password is one of the user's last *N* passwords, it is rejected.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
n = int(satrlar[0][0])
tarix = {}                                       # user -> last N hashes
for kim, parol in satrlar[1:]:
    h = format(fnv1a(kim + ":" + parol), "08x")  # the password itself is not stored
    oxirgi = tarix.setdefault(kim, [])
    if h in oxirgi:
        print(f"{kim}: rejected - one of the last {n} passwords")
    else:
        oxirgi.append(h)
        if len(oxirgi) > n:
            oxirgi.pop(0)                        # the oldest one is forgotten
        print(f"{kim}: accepted [{h}]")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        int n = Integer.parseInt(in.next());
        Map<String, List<String>> tarix = new HashMap<>();        // user -> last N hashes
        while (in.hasNext()) {
            String kim = in.next(), parol = in.next();
            String h = String.format("%08x", fnv1a(kim + ":" + parol));   // the password itself is not stored
            List<String> oxirgi = tarix.computeIfAbsent(kim, k -> new ArrayList<>());
            if (oxirgi.contains(h)) {
                System.out.println(kim + ": rejected - one of the last " + n + " passwords");
            } else {
                oxirgi.add(h);
                if (oxirgi.size() > n) oxirgi.remove(0);             // the oldest one is forgotten
                System.out.println(kim + ": accepted [" + h + "]");
            }
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <deque>
#include <iostream>
#include <map>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

int main() {
    int n;
    cin >> n;
    map<string, deque<string>> tarix;                            // user -> last N hashes
    string kim, parol;
    while (cin >> kim >> parol) {
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", fnv1a(kim + ":" + parol));   // the password itself is not stored
        string h = buf;
        deque<string>& oxirgi = tarix[kim];
        if (find(oxirgi.begin(), oxirgi.end(), h) != oxirgi.end()) {
            cout << kim << ": rejected - one of the last " << n << " passwords" << endl;
        } else {
            oxirgi.push_back(h);
            if ((int) oxirgi.size() > n) oxirgi.pop_front();      // the oldest one is forgotten
            cout << kim << ": accepted [" << h << "]" << endl;
        }
    }
    return 0;
}
```

Input:

```text
3
ali Qwerty!1
ali Bahor2025#
ali Qwerty!1
vali Qwerty!1
ali Yoz2025#
ali Kuz2025#
ali Qwerty!1
```

Output:

```text
ali: accepted [9c7e87c1]
ali: accepted [c24f5411]
ali: rejected - one of the last 3 passwords
vali: accepted [d9787e95]
ali: accepted [f4d41845]
ali: accepted [32f0b1c1]
ali: accepted [9c7e87c1]
```

vali also chose `Qwerty!1`, but the hash is different — the user name acted as a "salt". In the last request ali went back to the old password: after three changes it had left the history. If "Minimum password age" = 0, a user can change the password 24 times in a minute and return to their favourite old password — that is why the minimum age is set to 1 day.

:::warning
**Common mistakes**

- **Setting the lockout threshold to 1–3.** An attacker can deliberately lock out employees; 5–10 with a temporary lockout is better balanced.
- **"Minimum password age" = 0 together with password history.** The history effectively does not work — users bypass it.
- **Enabling reversible encryption.** Passwords are stored in a recoverable form — almost the same as storing them in clear text.
- **The same local administrator password on every computer.** If one computer is compromised, all of them are; use LAPS.
- **Not looking at the logs.** A sharp rise in 4625 and 4740 events is a sign of an attack.
:::

:::tip
Save the output of `net accounts` to a file (`net accounts > policy_before.txt`) and compare it after making changes. In a domain environment, if different groups need different requirements (for example 20 characters for administrators), use **Fine-Grained Password Policy** in Active Directory.
:::

## Summary

- Password policy: history (24), minimum age (1 day), minimum length (14), complexity, reversible encryption — disabled.
- Lockout policy: threshold (5–10), duration and counter reset (15 minutes); a very low threshold risks denial of service.
- Tools: `net accounts`, `net user`, `secpol.msc`, `gpedit.msc`/GPO, `gpupdate`, `gpresult`.
- Log: 4624, 4625, 4740, 4723/4724; password spraying shows as 4625 for many accounts from one address.
- Windows Hello and LAPS reduce the dependence on passwords and the risk of shared passwords.

## Check yourself

:::check
Q: Why should "Minimum password age" be greater than 0?
A: Otherwise a user changes the password many times in a row, bypasses the password history and returns to the old password.
Q: What risk appears if the lockout threshold is set to 1?
A: Denial of service: an attacker or a random mistake easily locks users out.
Q: Which Event ID records an account lockout in the Windows event log?
A: 4740.
Q: Why is even a correct password rejected on a locked account?
A: During the lockout the system does not check the password — otherwise an attacker could keep guessing.
Q: Which problem does LAPS solve?
A: Having the same local administrator password on all computers — it gives each one a random unique password.
:::

:::link https://learn.microsoft.com/en-us/windows/security/threat-protection/security-policy-settings/password-policy
Microsoft Learn: Password Policy
Windows password policy settings and recommended values.
:::
