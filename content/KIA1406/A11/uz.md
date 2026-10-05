---
title: Linux OT da fayl va katalog huquqlarini boshqarish
summary: Linux da fayl va katalog huquqlarini amalda boshqarish — foydalanuvchilar, guruhlar, /etc/passwd va /etc/shadow; ls -l chiqishi; r, w, x ning fayl va katalog uchun maʼnosi; chmod (belgili va sonli), chown, chgrp; umask va yangi fayllarning standart huquqlari; maxsus bitlar — setuid, setgid, sticky; ACL (getfacl, setfacl); sudo va eng kam imtiyoz; huquqlarni tekshirish tartibi (egasi → guruh → boshqalar); dasturiy topshiriq — rwx satrini sakkizlik koʻrinishga aylantirish.
---

## Nima uchun bu mavzu muhim?

Linux serverlarning, bulut va konteynerlarning asosiy tizimi. M11 dagi DAC modeli Linux da har bir faylning **egasi**, **guruhi** va uchta huquqlar uchligi orqali amalga oshadi. Notoʻgʻri huquqlar — eng koʻp uchraydigan xatolardan biri: hamma oʻqiy oladigan SSH yopiq kaliti, hamma yoza oladigan veb-katalog yoki keraksiz setuid dastur tizimni butunlay egallashga yoʻl ochadi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- `ls -l` chiqishini oʻqib, fayl va katalog huquqlarini tushuntira olasiz;
- `chmod`, `chown`, `chgrp`, `umask` bilan huquqlarni oʻzgartira olasiz;
- setuid, setgid va sticky bitlarining vazifasini va xavfini bilasiz;
- yadro kirish huquqini qanday tartibda tekshirishini koʻrsata olasiz;
- `rwx` satrini sakkizlik koʻrinishga aylantiruvchi dastur yozasiz (platformadagi A11 topshirigʻi).
:::

:::warning
Amaliy ishni Linux virtual mashinasida yoki WSL da, oʻz sinov katalogingizda bajaring. `/etc`, `/usr` kabi tizim kataloglarida `chmod -R` va `chown -R` ni sinab koʻrmang — tizim ishlamay qolishi mumkin.
:::

## Huquqlar asoslari

```text
-rwxr-x--- 1 ali dev 4096 Oct  5 10:00 skript.sh
```

Birinchi belgi — tur (`-` fayl, `d` katalog, `l` ramziy havola); keyin uchta uchlik: **egasi** (ali), **guruh** (dev), **boshqalar**. Har bir uchlikda `r`, `w`, `x` yoki `-`.

| Huquq | Fayl uchun | Katalog uchun |
|---|---|---|
| `r` (4) | mazmunini oʻqish | ichidagi nomlar roʻyxatini koʻrish (`ls`) |
| `w` (2) | mazmunini oʻzgartirish | ichida fayl yaratish, oʻchirish, nomini oʻzgartirish |
| `x` (1) | dastur sifatida bajarish | ichiga kirish (`cd`) va fayllarga murojaat qilish |

Eʼtibor bering: faylni oʻchirish uchun faylning emas, **katalogning** `w` huquqi kerak. Foydalanuvchilar `/etc/passwd` da (hamma oʻqiy oladi), parol xeshlari esa `/etc/shadow` da (faqat root) saqlanadi.

## Amaliy ish

**1-qadam. Kuzatish.**

```text
id
ls -l /etc/passwd /etc/shadow
ls -ld /tmp ~
stat fayl.txt
```

`id` — joriy foydalanuvchi va uning guruhlari; `/tmp` da `t` (sticky bit) borligini koʻring.

**2-qadam. chmod, chown, chgrp.**

```text
mkdir ~/sinov && cd ~/sinov
echo salom > fayl.txt
chmod u=rw,g=r,o= fayl.txt      # belgili koʻrinish
chmod 640 fayl.txt              # xuddi shu, sonli koʻrinish
chmod +x skript.sh              # hammaga bajarish huquqini qoʻshish
sudo chown ali:dev fayl.txt     # egasi va guruhini almashtirish
chgrp dev fayl.txt
```

Har bir buyruqdan keyin `ls -l` bilan natijani tekshiring. Boshqa foydalanuvchi (`sudo -u olim cat fayl.txt`) nomidan oʻqishga urinib koʻring.

**3-qadam. umask.** `umask` buyrugʻi joriy niqobni chiqaradi (odatda `0022`). `umask 077` qilib yangi fayl yarating va uning huquqlarini koʻring — faqat egasi uchun `rw-------`.

**4-qadam. Maxsus bitlar.**

| Bit | Son | Fayldagi maʼnosi | Katalogdagi maʼnosi |
|---|---|---|---|
| **setuid** | 4000 | dastur **egasining** huquqlari bilan ishlaydi (`/usr/bin/passwd` — root) | — |
| **setgid** | 2000 | dastur guruh huquqlari bilan ishlaydi | yangi fayllar katalog guruhini meros oladi (jamoaviy papka) |
| **sticky** | 1000 | — | faylni faqat uning egasi oʻchira oladi (`/tmp`) |

`find / -perm -4000 -type f 2>/dev/null` — tizimdagi barcha setuid dasturlar. Notanish setuid-root dastur — jiddiy xavf: undagi xato orqali oddiy foydalanuvchi root boʻlib oladi.

**5-qadam. ACL.** Uchlik yetmasa (masalan, bitta qoʻshimcha foydalanuvchiga oʻqish huquqi):

```text
setfacl -m u:olim:r fayl.txt
getfacl fayl.txt
```

`ls -l` da huquqlar oxirida `+` belgisi paydo boʻladi.

**6-qadam. sudo.** Root sifatida doimiy ishlamang; `sudo` bilan faqat kerakli buyruqni bajaring. `/etc/sudoers` ni faqat `visudo` orqali tahrirlang; `sudo -l` — sizga ruxsat etilgan buyruqlar.

**7-qadam. Dastur.** Platformadagi **Linux huquqlari: rwx dan sakkizlik koʻrinishga** topshirigʻini bajaring.

## Ishlangan misol 1: umask va standart huquqlar

Yangi fayl 666 (`rw-rw-rw-`), yangi katalog 777 huquqlari bilan "soʻraladi"; **umask** niqobidagi bitlar olib tashlanadi: huquq = soʻralgan AND (NOT umask). Har bir satrda umask (sakkizlik); yangi fayl va katalog huquqlarini sakkizlik koʻrinishda chiqaring.

```python
import sys

for t in sys.stdin.read().split():
    niqob = int(t, 8)                             # sakkizlik sonni oʻqish
    fayl = 0o666 & ~niqob                         # niqobdagi bitlar olib tashlanadi
    katalog = 0o777 & ~niqob
    print(f"umask {t}: fayl {fayl:03o}, katalog {katalog:03o}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int niqob = Integer.parseInt(t, 8);                // sakkizlik sonni oʻqish
            int fayl = 0666 & ~niqob;                          // niqobdagi bitlar olib tashlanadi
            int katalog = 0777 & ~niqob;
            System.out.println(String.format("umask %s: fayl %03o, katalog %03o", t, fayl, katalog));
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
        int niqob = stoi(t, nullptr, 8);                       // sakkizlik sonni oʻqish
        int fayl = 0666 & ~niqob;                              // niqobdagi bitlar olib tashlanadi
        int katalog = 0777 & ~niqob;
        printf("umask %s: fayl %03o, katalog %03o\n", t.c_str(), fayl, katalog);
    }
    return 0;
}
```

Kirish:

```text
022 002 077 027 000
```

Chiqish:

```text
umask 022: fayl 644, katalog 755
umask 002: fayl 664, katalog 775
umask 077: fayl 600, katalog 700
umask 027: fayl 640, katalog 750
umask 000: fayl 666, katalog 777
```

`022` — koʻp tizimlarda standart: boshqalar oʻqiy oladi, lekin yoza olmaydi. `077` — maxfiy maʼlumot bilan ishlovchi foydalanuvchilar uchun: yangi fayllar faqat egasiga ochiq. `000` — xavfli: har kim yangi faylni oʻzgartira oladi. Eʼtibor bering, umask fayllarga `x` qoʻshmaydi — bajariladigan fayl uchun `chmod +x` ni aniq bajarish kerak.

## Ishlangan misol 2: kirish huquqini tekshirish tartibi

Linux yadrosi huquqni quyidagi tartibda tekshiradi va **faqat bitta** uchlikni qoʻllaydi: foydalanuvchi egasi boʻlsa — egasi uchligi (guruh va boshqalar uchliklari endi qaralmaydi!); aks holda guruhda boʻlsa — guruh uchligi; aks holda — boshqalar uchligi. root `r` va `w` ni doim oladi, `x` ni esa kamida bitta `x` biti boʻlsa. Kirish: *m* ta fayl (nom egasi guruh sakkizlik_rejim), *u* ta foydalanuvchi (nom guruh1,guruh2), keyin soʻrovlar (foydalanuvchi amal fayl). Qaror va qaysi uchlik qoʻllanganini chiqaring.

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
        ok = amal != "x" or rejim & 0o111 != 0        # root uchun x — kamida bitta x biti
        sinf = "root"
    elif kim == egasi:                                # faqat egasi uchligi
        ok = (rejim >> 6) & BIT[amal] != 0
        sinf = "egasi"
    elif guruh in guruhlari[kim]:
        ok = (rejim >> 3) & BIT[amal] != 0
        sinf = "guruh"
    else:
        ok = rejim & BIT[amal] != 0
        sinf = "boshqalar"
    print(f"{kim} {amal} {nom}: " + ("ruxsat" if ok else "rad") + f" ({sinf})")
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
                ok = !amal.equals("x") || (rejim & 0111) != 0;    // root uchun x — kamida bitta x biti
                sinf = "root";
            } else if (kim.equals(f[1])) {                       // faqat egasi uchligi
                ok = ((rejim >> 6) & bit) != 0;
                sinf = "egasi";
            } else if (guruhlari.get(kim).contains(f[2])) {
                ok = ((rejim >> 3) & bit) != 0;
                sinf = "guruh";
            } else {
                ok = (rejim & bit) != 0;
                sinf = "boshqalar";
            }
            System.out.println(kim + " " + amal + " " + nom + ": " + (ok ? "ruxsat" : "rad") + " (" + sinf + ")");
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
            ok = amal != "x" || (rejim & 0111) != 0;             // root uchun x — kamida bitta x biti
            sinf = "root";
        } else if (kim == f[1]) {                                // faqat egasi uchligi
            ok = ((rejim >> 6) & bit) != 0;
            sinf = "egasi";
        } else if (find(gl.begin(), gl.end(), f[2]) != gl.end()) {
            ok = ((rejim >> 3) & bit) != 0;
            sinf = "guruh";
        } else {
            ok = (rejim & bit) != 0;
            sinf = "boshqalar";
        }
        cout << kim << " " << amal << " " << nom << ": " << (ok ? "ruxsat" : "rad") << " (" << sinf << ")" << endl;
    }
    return 0;
}
```

Kirish:

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

Chiqish:

```text
vali r hisobot.txt: ruxsat (guruh)
vali w hisobot.txt: rad (guruh)
olim r hisobot.txt: rad (boshqalar)
vali x skript.sh: ruxsat (egasi)
olim x skript.sh: rad (boshqalar)
ali r g.txt: rad (egasi)
vali r g.txt: ruxsat (guruh)
olim r g.txt: ruxsat (boshqalar)
root w hisobot.txt: ruxsat (root)
root x hisobot.txt: rad (root)
```

Eng qiziq holat — `g.txt` (074): ali uning **egasi** va dev guruhida ham, lekin u faylni oʻqiy olmaydi, chunki egasi uchligi `---`, guruh va boshqalar esa unga qoʻllanmaydi. Ayni paytda begona olim "boshqalar" sifatida oʻqiy oladi. Bunday "teskari" huquqlar amalda kam, lekin tekshirish tartibini yaxshi koʻrsatadi. root ham `x` biti umuman yoʻq faylni bajara olmaydi — bu tasodifan maʼlumot faylini ishga tushirishning oldini oladi.

:::warning
**Koʻp uchraydigan xatolar**

- **"Ishlamayapti — chmod 777".** Bu muammoni yashiradi va har kimga yozish huquqini beradi; aniq kerakli huquqni bering.
- **SSH kalitlariga keng huquqlar.** `~/.ssh` — 700, yopiq kalit — 600; aks holda SSH kalitni ishlatishdan bosh tortadi.
- **Faylni himoyaladim, lekin katalog ochiq.** Katalogga `w` huquqi boʻlgan har kim fayllarni oʻchira va almashtira oladi.
- **`chmod -R` ni oʻylamay ishlatish.** Kataloglar va fayllar uchun turli huquqlar kerak (`x` kataloglarga, fayllarga emas); `find -type d` / `-type f` bilan alohida bering.
- **Root sifatida doimiy ishlash.** Har bir xato yoki zararli skript butun tizimga taʼsir qiladi; `sudo` dan foydalaning.
:::

:::tip
`namei -l /var/www/html/index.html` yoʻldagi har bir katalogning huquqlarini koʻrsatadi — "Permission denied" sababini topishning eng tez usuli. `stat -c '%a %A %U %G %n' *` esa fayllar huquqlarini sakkizlik va belgili koʻrinishda birga chiqaradi.
:::

## Xulosa

- Har bir faylda egasi, guruh va uchta uchlik (egasi, guruh, boshqalar); `r`, `w`, `x` fayl va katalog uchun turlicha maʼnoga ega.
- `chmod` (belgili va sonli), `chown`, `chgrp`; yangi fayllar huquqi — 666/777 AND NOT umask.
- Maxsus bitlar: setuid (4000), setgid (2000), sticky (1000); setuid dasturlarni muntazam tekshiring.
- Yadro faqat bitta uchlikni qoʻllaydi: egasi → guruh → boshqalar.
- ACL (`setfacl`, `getfacl`) — nozik huquqlar uchun; `sudo` — eng kam imtiyoz uchun.

## Oʻzingizni tekshiring

:::check
Q: Katalogdagi faylni oʻchirish uchun qaysi huquq kerak?
A: Katalogning w (va x) huquqi; faylning oʻz huquqlari ahamiyatsiz (agar sticky bit boʻlmasa).
Q: umask 027 boʻlsa, yangi fayl va katalog huquqlari qanday?
A: Fayl 640, katalog 750.
Q: Egasi uchligi --- , guruh uchligi rwx boʻlsa, guruhda ham boʻlgan egasi faylni oʻqiy oladimi?
A: Yoʻq, egasiga faqat egasi uchligi qoʻllanadi.
Q: /tmp katalogidagi sticky bit nima beradi?
A: Har kim fayl yarata oladi, lekin faylni faqat uning egasi (yoki root) oʻchira oladi.
Q: Nima uchun notanish setuid-root dastur xavfli?
A: U root huquqlari bilan ishlaydi; undagi zaiflik oddiy foydalanuvchiga root boʻlish imkonini beradi.
:::

:::link https://man7.org/linux/man-pages/man1/chmod.1.html
chmod(1) — Linux manual page
chmod buyrugʻi, belgili va sonli rejimlar, maxsus bitlar (ingliz tilida).
:::
