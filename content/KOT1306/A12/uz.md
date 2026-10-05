---
title: Windows operatsion tizimida foydalanuvchilar va ularning vakolatlarini boshqarish
summary: Lokal va domen hisoblari; oʻrnatilgan hisoblar va guruhlar; lusrmgr.msc, net user, net localgroup va PowerShell bilan foydalanuvchi va guruh yaratish; UAC; parol va bloklash siyosatlari; NTFS ruxsatlari, meros qilish, Deny ustuvorligi; icacls; eng kam imtiyoz tamoyili va huquqlarni guruhlar orqali berish (AGDLP); amaldagi ruxsatni hisoblash.
---

## Nima uchun bu mavzu muhim?

Kompyuter sinfida talaba boshqa talabaning ishini oʻchirib yubormasligi, oʻqituvchi esa hamma ishlarni koʻra olishi kerak. Buxgalteriya papkasiga faqat buxgalterlar kira olishi, administrator hisobi esa hech qachon kundalik ishda ishlatilmasligi kerak. Bularning barchasi **hisoblar, guruhlar va ruxsatlar** orqali taʼminlanadi. Xavfsizlik hodisalarining katta qismi aynan ortiqcha huquqlardan boshlanadi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- lokal va domen hisoblari, oʻrnatilgan hisoblar va guruhlarni farqlay olasiz;
- grafik vositalar va buyruqlar satri orqali foydalanuvchi va guruh yarata olasiz;
- parol va hisobni bloklash siyosatlarini sozlay olasiz;
- NTFS ruxsatlarini berib, meros va Deny qoidalarini tushuntira olasiz;
- foydalanuvchining amaldagi ruxsatini guruhlar va ACL boʻyicha hisoblay olasiz.
:::

## Hisoblar va guruhlar

| Hisob turi | Qayerda saqlanadi | Misol |
|---|---|---|
| **Lokal** | kompyuterning SAM bazasida | `PC07\talaba` |
| **Domen** | Active Directory da | `AIFU\a.karimov` |
| **Microsoft hisobi** | bulutda (uy kompyuterlari) | `ism@outlook.com` |

Oʻrnatilgan hisoblar: **Administrator** (toʻliq huquq; oddiy ishda ishlatilmaydi, nomini oʻzgartirish yoki oʻchirib qoʻyish tavsiya etiladi), **Guest** (sukut boʻyicha oʻchirilgan — shunday qolishi kerak).

Oʻrnatilgan guruhlar: **Administrators** (toʻliq boshqaruv), **Users** (oddiy ish), **Remote Desktop Users** (masofaviy ish stoliga kirish), **Backup Operators** (zaxira nusxa), **Guests**. Domenda: **Domain Admins**, **Domain Users** va boshqalar.

**UAC** (User Account Control): administrator ham sukut boʻyicha oddiy huquqlar bilan ishlaydi; tizim oʻzgarishi kerak boʻlganda tasdiqlash oynasi chiqadi.

## Foydalanuvchi va guruh yaratish

Grafik usul: **lusrmgr.msc** (Local Users and Groups) yoki **Computer Management**; domenda — **Active Directory Users and Computers** (dsa.msc).

Buyruqlar satri (administrator sifatida):

```text
net user a.karimov Parol#2026 /add /fullname:"Aziz Karimov"
net localgroup Talabalar /add
net localgroup Talabalar a.karimov /add
net user a.karimov /active:no            hisobni vaqtincha oʻchirish
net user a.karimov                       hisob haqida maʼlumot
whoami /groups                           joriy foydalanuvchining guruhlari
```

PowerShell:

```text
$p = Read-Host -AsSecureString "Parol"
New-LocalUser -Name "a.karimov" -Password $p -FullName "Aziz Karimov"
New-LocalGroup -Name "Talabalar"
Add-LocalGroupMember -Group "Talabalar" -Member "a.karimov"
Get-LocalGroupMember -Group "Administrators"
```

## Parol va bloklash siyosatlari

**secpol.msc** → Account Policies (domenda — Default Domain Policy GPO):

| Sozlama | Tavsiya |
|---|---|
| Minimum password length | 12 belgi |
| Password must meet complexity requirements | Enabled |
| Maximum password age | 90 kun (yoki uzun parol bilan muddatsiz — zamonaviy tavsiya) |
| Enforce password history | 10 ta oldingi parol |
| Account lockout threshold | 5 ta notoʻgʻri urinish |
| Account lockout duration | 15 daqiqa |

Bloklash parolni tanlab buzish (brute force) hujumini sekinlashtiradi.

## NTFS ruxsatlari

Papka yoki fayl xususiyatlari → **Security** varagʻi. Har bir obyektda **ACL** (kirish roʻyxati) bor: qaysi foydalanuvchi yoki guruhga qanday ruxsat **Allow** yoki **Deny** qilingan.

| Ruxsat | Nimaga imkon beradi |
|---|---|
| **Full control** | hammasi, shu jumladan ruxsatlarni oʻzgartirish va egalikni olish |
| **Modify** | oʻqish, yozish, oʻchirish |
| **Read & execute** | oʻqish va dasturlarni ishga tushirish |
| **List folder contents** | papka ichini koʻrish |
| **Read** | fayllarni oʻqish |
| **Write** | yangi fayl yaratish va yozish |

Qoidalar:

1. **Ruxsatlar yigʻiladi**: foydalanuvchi va uning barcha guruhlariga berilgan Allow lar birlashadi.
2. **Deny ustun**: biror guruhda Deny boʻlsa, boshqa guruhdagi Allow uni bekor qila olmaydi (aniq berilgan ruxsatlar uchun).
3. **Hech narsa berilmagan** — demak, ruxsat yoʻq (yashirin rad).
4. **Meros**: papkadagi ruxsatlar ichidagi fayl va papkalarga oʻtadi; merosni uzish mumkin (Advanced → Disable inheritance).

Buyruqlar satridan: `icacls D:\Kurslar` — ACL ni koʻrish; `icacls D:\Kurslar /grant Talabalar:(OI)(CI)RX` — Talabalar guruhiga oʻqish va ishga tushirish (ichki obyektlarga meros bilan).

**Eng kam imtiyoz tamoyili**: har kimga ishi uchun kerakli minimal huquq. **Huquqlar guruhlarga beriladi**, foydalanuvchilar guruhlarga qoʻshiladi (domenda **AGDLP**: Account → Global group → Domain Local group → Permission).

## Ishlangan misol: amaldagi ruxsatni hisoblash

Birinchi satrda guruhlar soni *g*, keyingi *g* satrda guruh nomi va aʼzolari. Keyin ACL yozuvlari soni va har bir yozuv: subyekt (foydalanuvchi, guruh yoki `Everyone`), `Allow`/`Deny` va ruxsat harflari (`R` — oʻqish, `W` — yozish, `D` — oʻchirish). Soʻng soʻrovlar: foydalanuvchi va bitta ruxsat harfi. Har bir soʻrov uchun natijani chiqaring: Deny boʻlsa — `rad (Deny)`, Allow boʻlsa — `ruxsat`, hech narsa boʻlmasa — `rad (berilmagan)`.

```python
g = int(input())
guruhlar = {}                                  # foydalanuvchi -> guruhlar toʻplami
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
        print(user, ruxsat + ": rad (Deny)")       # Deny har doim ustun
    elif allow:
        print(user, ruxsat + ": ruxsat")
    else:
        print(user, ruxsat + ": rad (berilmagan)")
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
        Map<String, Set<String>> guruhlar = new HashMap<>();       // foydalanuvchi -> guruhlar toʻplami
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
                System.out.println(user + " " + ruxsat + ": rad (Deny)");   // Deny har doim ustun
            } else if (allow) {
                System.out.println(user + " " + ruxsat + ": ruxsat");
            } else {
                System.out.println(user + " " + ruxsat + ": rad (berilmagan)");
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
    map<string, set<string>> guruhlar;                             // foydalanuvchi -> guruhlar toʻplami
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
            cout << user << " " << ruxsat << ": rad (Deny)" << endl;   // Deny har doim ustun
        } else if (allow) {
            cout << user << " " << ruxsat << ": ruxsat" << endl;
        } else {
            cout << user << " " << ruxsat << ": rad (berilmagan)" << endl;
        }
    }
    return 0;
}
```

Kirish:

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

Chiqish:

```text
ali W: ruxsat
vali W: rad (Deny)
sami D: rad (Deny)
ali D: rad (berilmagan)
nodir D: ruxsat
mehmon R: ruxsat
```

`vali` Talabalar guruhi orqali yozish huquqiga ega, lekin unga shaxsan berilgan Deny ustun keldi. `mehmon` hech qaysi guruhda emas, lekin `Everyone` orqali oʻqiy oladi — shuning uchun `Everyone` ga keng ruxsat berish xavfli.

:::warning
**Koʻp uchraydigan xatolar**

- **Kundalik ishni administrator hisobida bajarish.** Zararli dastur ham administrator huquqi bilan ishlaydi.
- **Ruxsatlarni har bir foydalanuvchiga alohida berish.** 300 talabaga alohida emas, bitta guruhga beriladi.
- **Deny ni haddan tashqari koʻp ishlatish.** U har doim ustun boʻlgani uchun kutilmagan rad etishlarga olib keladi; koʻpincha Allow bermaslik kifoya.
- **`Everyone` ga Full control yoki Modify berish.** Bu tarmoqdagi har kimga papkani oʻchirish imkonini beradi.
- **Ishdan ketgan xodimning hisobini oʻchirmaslik.** Ishdan ketganlarning hisoblari darhol oʻchiriladi yoki bloklanadi.
:::

:::tip
Oʻz kompyuteringizda `whoami /groups` va `whoami /priv` ni bajaring: siz qaysi guruhlarga kirasiz va qanday imtiyozlaringiz bor? Keyin istalgan papkada `icacls .` ni bajarib, ACL dagi `(OI)(CI)`, `(F)`, `(M)`, `(RX)` belgilarini izohlang.
:::

## Xulosa

- Hisoblar: lokal (SAM), domen (AD); oʻrnatilgan Administrator va Guest ehtiyotkorlik talab qiladi.
- Foydalanuvchi va guruhlar: lusrmgr.msc, dsa.msc, `net user`, `net localgroup`, `New-LocalUser`.
- Siyosatlar: parol uzunligi va murakkabligi, tarix, bloklash chegarasi.
- NTFS: Allow lar yigʻiladi, Deny ustun, hech narsa — rad; meros pastga oʻtadi; `icacls` bilan koʻriladi.
- Eng kam imtiyoz va huquqlarni guruhlar orqali berish — boshqaruvni soddalashtiradi va xatolarni kamaytiradi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun huquqlar foydalanuvchilarga emas, guruhlarga beriladi?
A: Boshqarish osonlashadi va xatolar kamayadi: yangi xodimni guruhga qoʻshish kifoya.
Q: Foydalanuvchi bir guruh orqali Allow Write, boshqasi orqali Deny Write olgan. Natija qanday?
A: Yozish rad etiladi — Deny ustun.
Q: Lokal foydalanuvchi va guruhlarni boshqarish konsoli qanday ataladi?
A: lusrmgr.msc (Local Users and Groups).
Q: Hisobni bloklash siyosati nima uchun kerak?
A: Notoʻgʻri urinishlardan keyin hisob vaqtincha bloklanib, parolni tanlab buzish sekinlashadi.
Q: Papkaga berilgan ruxsat ichidagi fayllarga qanday oʻtadi?
A: Meros orqali — agar meros uzilmagan boʻlsa.
:::

:::link https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/manage/understand-security-groups
Active Directory security groups
Microsoft ning xavfsizlik guruhlari va huquqlar haqidagi qoʻllanmasi (ingliz tilida).
:::
