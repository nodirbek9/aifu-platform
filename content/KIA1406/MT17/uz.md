---
title: Windows OT da zaxira nusxalashni (Backup and Restore) amalga oshirish. Windows OT da imtiyozlar turlari, fayl va kataloglar uchun foydalanishni boshqarish tartibi
summary: Zaxira nusxalash strategiyasi — 3-2-1 qoidasi, toʻliq, inkremental va differensial nusxalar, RPO va RTO, tiklashni sinash, shifrlovchi viruslardan himoya uchun oflayn nusxa; Windows vositalari — Fayl tarixi (File History), Windows Backup, tizim tasviri (Backup and Restore), tiklash nuqtalari, wbadmin; Windows da hisob turlari va imtiyozlar — administrator, standart foydalanuvchi, UAC, foydalanuvchi huquqlari (user rights); NTFS huquqlari — asosiy va maxsus ruxsatlar, meros olish, aniq rad etish, samarali huquqlar, ulashish va NTFS huquqlari birga; icacls; samarali huquqlarni dasturda hisoblash.
---

## Nima uchun bu mavzu muhim?

Ikki mavzu — "maʼlumot yoʻqolsa nima qilamiz?" va "maʼlumotga kim kira oladi?" — bitta maqsadga xizmat qiladi: axborotning **foydalanish mumkinligi** va **konfidensialligi** (CIA). Shifrlovchi virus, disk buzilishi yoki tasodifiy oʻchirishdan keyin tashkilotni faqat sinalgan zaxira nusxa qutqaradi; notoʻgʻri huquqlar esa maxfiy papkani butun tashkilotga ochib qoʻyadi. Ikkala vazifa ham Windows administratorining kundalik ishi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- zaxira nusxalash strategiyasini (3-2-1, nusxa turlari, RPO va RTO) tuza olasiz;
- Windows da zaxira nusxa yaratish va tiklash vositalaridan foydalana olasiz;
- Windows hisob turlari, UAC va foydalanuvchi huquqlarini farqlay olasiz;
- NTFS ruxsatlarini, meros olish va aniq rad etishni tushuntira olasiz;
- foydalanuvchining samarali huquqlarini hisoblay olasiz.
:::

## Zaxira nusxalash strategiyasi

**3-2-1 qoidasi**: maʼlumotning kamida **3** nusxasi, **2** xil tashuvchida, **1** tasi boshqa joyda (boshqa bino yoki bulut). Shifrlovchi viruslardan himoya uchun zamonaviy tavsiya — **3-2-1-1-0**: yana 1 ta oflayn yoki oʻzgartirib boʻlmaydigan (immutable) nusxa va tiklash sinovida 0 ta xato.

| Nusxa turi | Nima nusxalanadi | Afzalligi | Kamchiligi |
|---|---|---|---|
| **Toʻliq** | hammasi | tiklash oddiy va tez | koʻp joy va vaqt |
| **Inkremental** | oxirgi har qanday nusxadan beri oʻzgarganlar | tez, kam joy | tiklash uchun butun zanjir kerak |
| **Differensial** | oxirgi toʻliq nusxadan beri oʻzgarganlar | tiklash uchun 2 ta nusxa yetarli | vaqt oʻtishi bilan kattalashadi |

**RPO** (Recovery Point Objective) — qancha vaqtlik maʼlumotni yoʻqotishga rozimiz (masalan, 24 soat → kunlik nusxa). **RTO** (Recovery Time Objective) — tizim qancha vaqtda tiklanishi kerak. Ikkalasi biznes talablari va risklarni baholash (M2) asosida belgilanadi.

**Tiklashni sinash**: sinalmagan zaxira nusxa — zaxira emas. Muntazam ravishda (masalan, choraklik) tasodifiy fayllarni va butun tizimni sinov muhitiga tiklab koʻring.

## Windows zaxira vositalari

| Vosita | Nimani saqlaydi | Qayerda |
|---|---|---|
| **Fayl tarixi** (File History) | foydalanuvchi papkalari, fayllarning oldingi versiyalari | Settings yoki Control Panel → File History; tashqi disk yoki tarmoq papkasi |
| **Windows Backup** (Windows 11) | papkalar, sozlamalar, ilovalar roʻyxati — OneDrive ga | Windows Backup ilovasi |
| **Tizim tasviri** (Backup and Restore, Windows 7) | butun disk, OT bilan birga | Control Panel → Backup and Restore (Windows 7) → Create a system image |
| **Tiklash nuqtalari** (System Restore) | tizim fayllari va reestr (shaxsiy fayllar emas!) | `sysdm.cpl` → System Protection |
| **wbadmin** | buyruq satridan zaxira va tiklash | `wbadmin start backup ...` (Windows Server da toʻliq) |

**Amaliy ish (sinov kompyuteri yoki virtual mashinada)**:

1. Tashqi disk ulang va **Fayl tarixi** ni yoqing; nusxalash chastotasini (masalan, har soat) va saqlash muddatini sozlang.
2. Sinov faylini yarating, oʻzgartiring va oʻchiring; "Restore personal files" orqali eski versiyasini tiklang.
3. **Tizim tasviri** yarating (Control Panel → Backup and Restore (Windows 7) → Create a system image) va tiklash diskini tayyorlang.
4. `sysdm.cpl` da tizim himoyasini yoqib, qoʻlda **tiklash nuqtasi** yarating.
5. Hisobotda RPO va RTO ni oʻz uyingiz yoki kichik ofis uchun asoslang.

Eslatma: zaxira disk doimiy ulangan boʻlsa, shifrlovchi virus uni ham shifrlaydi — nusxalashdan keyin uzib qoʻying yoki versiyalashni qoʻllab-quvvatlovchi bulut/tarmoq saqlovchisidan foydalaning.

## Windows da hisob turlari va imtiyozlar

| Hisob / mexanizm | Tavsif |
|---|---|
| **Administrator** | tizimni toʻliq boshqaradi: dastur oʻrnatish, sozlamalar, boshqa hisoblar |
| **Standart foydalanuvchi** | kundalik ish; tizim sozlamalarini oʻzgartira olmaydi — **kundalik ish uchun tavsiya etiladi** |
| **UAC** (User Account Control) | administrator ham odatda cheklangan token bilan ishlaydi; imtiyozli amal uchun tasdiq soʻraladi |
| **Mehmon, xizmat hisoblari** | SYSTEM, LocalService, NetworkService — xizmatlar uchun; Mehmon hisobi oʻchirilgan boʻlishi kerak |
| **Foydalanuvchi huquqlari** (User Rights Assignment) | `secpol.msc` → Local Policies: "Log on locally", "Shut down the system", "Back up files and directories", "Debug programs" va boshq. |

**Eng kam imtiyoz** (M11): kundalik ish — standart hisobda, administrator hisobi faqat boshqaruv uchun. Hisoblar va guruhlar: `lusrmgr.msc` yoki `net user`, `net localgroup Administrators`.

## NTFS ruxsatlari

**Asosiy ruxsatlar**: Full control (toʻliq, huquqlarni oʻzgartirish ham), Modify (oʻzgartirish va oʻchirish), Read & execute, List folder contents, Read, Write. Ularning har biri bir nechta **maxsus ruxsatlar** toʻplami (Advanced → Permissions).

**Qoidalar**:

- **Meros olish**: katalogdagi ruxsatlar odatda ichidagi fayl va papkalarga oʻtadi; uni uzish mumkin (Disable inheritance).
- **Aniq rad etish (Deny) ustun**: foydalanuvchi va uning guruhlari ruxsatlari birlashadi, lekin aniq Deny shu huquqni olib tashlaydi.
- **Samarali huquqlar**: Advanced → Effective Access yorligʻida foydalanuvchining haqiqiy huquqlarini koʻrish mumkin.
- **Ulashish + NTFS**: tarmoq orqali kirishda ulashish (Share) va NTFS ruxsatlarining **eng cheklovchisi** amal qiladi.
- **Egasi** (Owner) har doim ruxsatlarni oʻzgartira oladi.

Buyruq satrida:

```text
icacls D:\Hisobot
icacls D:\Hisobot /grant Buxgalteriya:(OI)(CI)M
icacls D:\Hisobot /deny Stajyorlar:(OI)(CI)W
icacls D:\Hisobot /inheritance:d
icacls D:\Hisobot /save huquqlar.txt /t
```

`(OI)(CI)` — fayl va papkalarga meros qoldirish, `M` — Modify; `/save` — huquqlarni faylga saqlash (oʻzgartirishdan oldin zaxira).

## Ishlangan misol: samarali NTFS huquqlari

Soddalashtirilgan model: huquqlar — R (oʻqish), W (yozish), X (bajarish), D (oʻchirish). Birinchi satrda ACL yozuvlari soni, keyin yozuvlar (`allow` yoki `deny`, foydalanuvchi yoki guruh, huquqlar). Keyingi satrlarda foydalanuvchi va uning guruhlari (vergul bilan). Har bir foydalanuvchi `Everyone` guruhida ham. Samarali huquq = barcha mos `allow` lar birlashmasi minus barcha mos `deny` lar. Rad etish tufayli yoʻqotilgan huquqlarni alohida koʻrsating.

```python
import sys

TARTIB = "RWXD"
satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
a = int(satrlar[0][0])
acl = satrlar[1:1 + a]
for qator in satrlar[1 + a:]:
    kim = qator[0]
    guruhlar = set(qator[1].split(",")) | {kim, "Everyone"}   # foydalanuvchi + guruhlari + Everyone
    ruxsat, taqiq = set(), set()
    for tur, kimga, huquq in acl:
        if kimga in guruhlar:
            (ruxsat if tur == "allow" else taqiq).update(huquq)
    natija = "".join(h for h in TARTIB if h in ruxsat and h not in taqiq) or "-"   # deny ustun
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
            guruhlar.add(kim);                                   // foydalanuvchi + guruhlari + Everyone
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
                if (ruxsat.contains(h) && !taqiq.contains(h)) natija.append(h);   // deny ustun
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
        set<string> guruhlar = {kim, "Everyone"};               // foydalanuvchi + guruhlari + Everyone
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
            if (ruxsat.count(h) && !taqiq.count(h)) natija += h;   // deny ustun
            if (ruxsat.count(h) && taqiq.count(h)) rad += h;
        }
        cout << kim << ": " << (natija.empty() ? "-" : natija);
        if (!rad.empty()) cout << " (deny: " << rad << ")";
        cout << endl;
    }
    return 0;
}
```

Kirish:

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

Chiqish:

```text
ali: RWX
vali: R (deny: W)
olim: RWXD
sardor: R
kamol: R
```

vali buxgalteriya guruhidan yozish huquqini olgan, lekin u stajyor ham — aniq rad etish ustun keldi va yozish olib tashlandi. ali ga shaxsan berilgan X guruh huquqlariga qoʻshildi. `Everyone` ga berilgan oʻqish huquqi hammaga, jumladan begona kamol ga ham amal qiladi — maxfiy papkalarda bu xato. Real tizimlarda "Effective Access" yorligʻi xuddi shu hisobni bajaradi (va yana ulashish ruxsatlari, egalik va imtiyozlarni hisobga oladi).

:::warning
**Koʻp uchraydigan xatolar**

- **Zaxira nusxani hech qachon tiklab koʻrmaslik.** Avariya kunida nusxa ishlamasligi aniqlanadi.
- **Zaxira diskni doimiy ulangan holda qoldirish.** Shifrlovchi virus uni ham shifrlaydi.
- **Tiklash nuqtasini zaxira nusxa deb hisoblash.** U shaxsiy fayllarni saqlamaydi.
- **Kundalik ishni administrator hisobida bajarish.** Zararli dastur ham administrator huquqi bilan ishlaydi.
- **Everyone yoki Users ga Full control berish.** Huquqlarni guruhlarga va faqat kerakli darajada bering; Deny ni kam va oʻylab ishlating.
:::

:::tip
Huquqlarni oʻzgartirishdan oldin `icacls papka /save zaxira.txt /t` bilan joriy holatni saqlang — xato boʻlsa `icacls papka /restore zaxira.txt` bilan qaytarasiz. Bu "huquqlar uchun zaxira nusxa".
:::

## Xulosa

- Zaxira: 3-2-1 (yoki 3-2-1-1-0), toʻliq, inkremental va differensial nusxalar; RPO va RTO biznes talablaridan; tiklash muntazam sinaladi.
- Windows: Fayl tarixi, Windows Backup, tizim tasviri, tiklash nuqtalari, wbadmin; zaxira disk nusxalashdan keyin uziladi.
- Imtiyozlar: administrator va standart hisob, UAC, foydalanuvchi huquqlari; kundalik ish — standart hisobda.
- NTFS: asosiy va maxsus ruxsatlar, meros olish, Deny ustun, samarali huquqlar; tarmoqda ulashish va NTFS ning eng cheklovchisi.
- `icacls` — huquqlarni koʻrish, berish, rad etish va saqlash.

## Oʻzingizni tekshiring

:::check
Q: 3-2-1 qoidasi nimani anglatadi?
A: Kamida 3 nusxa, 2 xil tashuvchida, 1 tasi boshqa joyda.
Q: Inkremental va differensial nusxalarning farqi nima?
A: Inkremental oxirgi har qanday nusxadan beri, differensial oxirgi toʻliq nusxadan beri oʻzgarganlarni saqlaydi.
Q: Foydalanuvchi bir guruhdan Allow Write, boshqasidan Deny Write olgan. Yoza oladimi?
A: Yoʻq, aniq rad etish ustun keladi.
Q: Tarmoq orqali kirishda ulashish ruxsati Read, NTFS ruxsati Modify. Samarali huquq qanday?
A: Read — ikkalasining eng cheklovchisi.
Q: Nima uchun kundalik ishni standart hisobda bajarish tavsiya etiladi?
A: Zararli dastur yoki xato foydalanuvchi huquqlari bilan ishlaydi; standart hisob tizimni oʻzgartira olmaydi.
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/icacls
Microsoft Learn: icacls
icacls buyrugʻi, ruxsatlar va meros olish bayroqlari tavsifi (ingliz tilida).
:::
