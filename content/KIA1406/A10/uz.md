---
title: Windows OT da foydalanuvchi autentifikatsiyasi va parol siyosatini sozlash
summary: Windows da parol va hisobni bloklash siyosatini amalda sozlash — mahalliy xavfsizlik siyosati (secpol.msc), net accounts buyrugʻi, guruh siyosati (gpedit.msc, GPO); parol siyosati parametrlari — tarix, maksimal va minimal muddat, minimal uzunlik, murakkablik; bloklash parametrlari — chegara, davomiylik, hisoblagichni tiklash; hodisalar jurnali (4624, 4625, 4740); Windows Hello va LAPS; bloklash va parol tarixi mantiqini dasturda modellashtirish; dasturiy topshiriq — parol kuchini baholash.
---

## Nima uchun bu mavzu muhim?

M10 da parollar va autentifikatsiya nazariyasini koʻrdik. Tashkilotda bu qoidalar har bir foydalanuvchining xohishiga qoldirilmaydi — ular **siyosat** sifatida markazlashgan holda majburiy qoʻyiladi. Windows — tashkilotlarda eng keng tarqalgan ish stoli tizimi, shuning uchun uning parol va bloklash siyosatlarini sozlay olish har bir tizim administratori va xavfsizlik mutaxassisi uchun asosiy koʻnikma.

:::note
**Bu mashgʻulotdan soʻng siz:**

- Windows parol siyosati va hisobni bloklash siyosati parametrlarini sozlay olasiz;
- `net accounts`, `secpol.msc` va guruh siyosatidan foydalana olasiz;
- muvaffaqiyatsiz kirishlar va bloklashlarni hodisalar jurnalidan topa olasiz;
- bloklash va parol tarixi qanday ishlashini dasturda koʻrsata olasiz;
- parol kuchini baholovchi dastur yozasiz (platformadagi A10 topshirigʻi).
:::

:::warning
Amaliy ishni **virtual mashinada** yoki oʻquv kompyuterida bajaring. Ish yoki shaxsiy kompyuterda xavfsizlik sozlamalarini oʻzgartirishdan oldin joriy qiymatlarni yozib oling; notoʻgʻri bloklash siyosati oʻzingizni ham tizimdan chiqarib qoʻyishi mumkin.
:::

## Parol siyosati parametrlari

`secpol.msc` → Account Policies → **Password Policy** (Windows Home nashrida bu oyna yoʻq — `net accounts` buyrugʻidan foydalaning):

| Parametr | Maʼnosi | Tavsiya (CIS / Microsoft asosiy chizigʻi) |
|---|---|---|
| Enforce password history | oxirgi nechta parolni qayta ishlatib boʻlmaydi | 24 |
| Maximum password age | parol necha kundan keyin eskiradi | 365 yoki cheklovsiz (NIST: faqat buzilish belgisi boʻlsa almashtirish) |
| Minimum password age | parolni qancha vaqtdan keyin yana almashtirish mumkin | 1 kun — tarixni ketma-ket almashtirib "aylanib oʻtish"ning oldini oladi |
| Minimum password length | minimal uzunlik | 14 |
| Password must meet complexity requirements | 3 turdagi belgi, foydalanuvchi nomi boʻlmasligi | yoqilgan (eski talab; uzunlik muhimroq) |
| Store passwords using reversible encryption | parolni tiklanadigan koʻrinishda saqlash | **oʻchirilgan** — aks holda parol amalda ochiq saqlanadi |

**Account Lockout Policy**:

| Parametr | Maʼnosi | Tavsiya |
|---|---|---|
| Account lockout threshold | nechta xato urinishdan keyin bloklanadi | 5–10 (Windows 11 da standart — 10) |
| Account lockout duration | bloklash necha daqiqa davom etadi | 15 |
| Reset account lockout counter after | xatolar hisoblagichi necha daqiqadan keyin nolga tushadi | 15 |

Bloklash toʻliq tanlash hujumini sekinlashtiradi, lekin juda past chegara (1–2) — **xizmatdan voz kechish** xavfini tugʻdiradi: hujumchi ataylab notoʻgʻri parol kiritib, barcha xodimlarni bloklashi mumkin.

## Amaliy ish

**1-qadam. Joriy siyosatni koʻrish.** Administrator huquqli buyruq satrida:

```text
net accounts
net user
net user talaba1
```

`net accounts` minimal uzunlik, muddatlar, tarix va bloklash qiymatlarini chiqaradi; `net user talaba1` — hisob qachon parol almashtirgani va qachon eskirishini.

**2-qadam. Sinov foydalanuvchisi va siyosat.** Sinov hisobini yarating va siyosatni oʻrnating:

```text
net user talaba1 * /add
net accounts /minpwlen:12 /uniquepw:24 /minpwage:1 /maxpwage:unlimited
net accounts /lockoutthreshold:5 /lockoutduration:15 /lockoutwindow:15
```

Xuddi shuni grafik koʻrinishda `secpol.msc` (yoki domen tarmogʻida — `gpmc.msc` dagi GPO: Computer Configuration → Windows Settings → Security Settings → Account Policies) orqali bajaring. Domen siyosati qoʻllanishi uchun `gpupdate /force`, natijani tekshirish uchun `gpresult /r`.

**3-qadam. Siyosatni sinash.** `talaba1` ga qisqa parol (masalan, `abc123`) qoʻyishga urinib koʻring — tizim rad etadi. Keyin tizimga `talaba1` sifatida 5 marta notoʻgʻri parol bilan kirishga urining — hisob bloklanadi.

**4-qadam. Hodisalar jurnali.** `eventvwr.msc` → Windows Logs → **Security**; filtr bilan quyidagi hodisalarni toping:

| Event ID | Hodisa |
|---|---|
| 4624 | muvaffaqiyatli kirish |
| 4625 | muvaffaqiyatsiz kirish urinishi |
| 4740 | hisob bloklandi |
| 4723 / 4724 | foydalanuvchi parolni almashtirdi / administrator parolni tikladi |

PowerShell da: `Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 10`. Bir xil manzildan koʻp hisoblarga 4625 — parol purkash belgisi.

**5-qadam. Zamonaviy vositalar.** **Windows Hello** (PIN yoki biometriya) — qurilmaga bogʻlangan, TPM da saqlanadigan kalit; PIN tarmoq orqali uzatilmaydi. **LAPS** (Local Administrator Password Solution) har bir kompyuterning mahalliy administrator parolini tasodifiy va noyob qiladi — bitta umumiy parol bilan butun tarmoqni egallashning oldini oladi.

**6-qadam. Dastur.** Platformadagi **Parol siyosati: parol kuchini baholash** topshirigʻini bajaring.

## Ishlangan misol 1: hisobni bloklash siyosati

Birinchi satrda siyosat: bloklash chegarasi, kuzatish oynasi (daqiqa) va bloklash davomiyligi (daqiqa). Keyingi satrlarda kirish urinishlari: vaqt (HH:MM), foydalanuvchi, natija (`OK` yoki `FAIL`). Bloklangan hisobga har qanday urinish rad etiladi va hisoblanmaydi; oynadan eski xatolar unutiladi; muvaffaqiyatli kirish hisoblagichni nolga tushiradi.

```python
import sys


def daqiqa(v):
    s, d = v.split(":")
    return int(s) * 60 + int(d)


def vaqt(m):
    return f"{m // 60:02d}:{m % 60:02d}"


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
chegara, oyna, muddat = map(int, satrlar[0])
xatolar, blok = {}, {}                          # foydalanuvchi -> xato vaqtlari / blok tugash vaqti
for v, kim, natija in satrlar[1:]:
    t = daqiqa(v)
    if kim in blok and t < blok[kim]:
        print(f"{v} {kim}: rad - bloklangan")
        continue
    if natija == "OK":
        xatolar[kim] = []                       # muvaffaqiyatli kirish hisoblagichni tiklaydi
        print(f"{v} {kim}: kirish")
        continue
    royxat = [x for x in xatolar.get(kim, []) if t - x < oyna] + [t]   # oynadagi xatolar
    if len(royxat) >= chegara:
        blok[kim] = t + muddat
        xatolar[kim] = []
        print(f"{v} {kim}: BLOKLANDI ({vaqt(t + muddat)} gacha)")
    else:
        xatolar[kim] = royxat
        print(f"{v} {kim}: xato ({len(royxat)}/{chegara})")
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
        Map<String, List<Integer>> xatolar = new HashMap<>();   // foydalanuvchi -> xato vaqtlari / blok tugash vaqti
        Map<String, Integer> blok = new HashMap<>();
        while (in.hasNext()) {
            String v = in.next(), kim = in.next(), natija = in.next();
            int t = daqiqa(v);
            if (blok.containsKey(kim) && t < blok.get(kim)) {
                System.out.println(v + " " + kim + ": rad - bloklangan");
                continue;
            }
            if (natija.equals("OK")) {
                xatolar.put(kim, new ArrayList<>());            // muvaffaqiyatli kirish hisoblagichni tiklaydi
                System.out.println(v + " " + kim + ": kirish");
                continue;
            }
            List<Integer> royxat = new ArrayList<>();
            for (int x : xatolar.getOrDefault(kim, new ArrayList<>())) {
                if (t - x < oyna) royxat.add(x);                 // oynadagi xatolar
            }
            royxat.add(t);
            if (royxat.size() >= chegara) {
                blok.put(kim, t + muddat);
                xatolar.put(kim, new ArrayList<>());
                System.out.println(v + " " + kim + ": BLOKLANDI (" + vaqt(t + muddat) + " gacha)");
            } else {
                xatolar.put(kim, royxat);
                System.out.println(v + " " + kim + ": xato (" + royxat.size() + "/" + chegara + ")");
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
    map<string, vector<int>> xatolar;                            // foydalanuvchi -> xato vaqtlari / blok tugash vaqti
    map<string, int> blok;
    string v, kim, natija;
    while (cin >> v >> kim >> natija) {
        int t = daqiqa(v);
        if (blok.count(kim) && t < blok[kim]) {
            cout << v << " " << kim << ": rad - bloklangan" << endl;
            continue;
        }
        if (natija == "OK") {
            xatolar[kim].clear();                                // muvaffaqiyatli kirish hisoblagichni tiklaydi
            cout << v << " " << kim << ": kirish" << endl;
            continue;
        }
        vector<int> royxat;
        for (int x : xatolar[kim]) {
            if (t - x < oyna) royxat.push_back(x);               // oynadagi xatolar
        }
        royxat.push_back(t);
        if ((int) royxat.size() >= chegara) {
            blok[kim] = t + muddat;
            xatolar[kim].clear();
            cout << v << " " << kim << ": BLOKLANDI (" << vaqt(t + muddat) << " gacha)" << endl;
        } else {
            xatolar[kim] = royxat;
            cout << v << " " << kim << ": xato (" << royxat.size() << "/" << chegara << ")" << endl;
        }
    }
    return 0;
}
```

Kirish:

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

Chiqish:

```text
09:00 ali: xato (1/3)
09:02 ali: xato (2/3)
09:03 vali: kirish
09:05 ali: BLOKLANDI (09:20 gacha)
09:10 ali: rad - bloklangan
09:21 ali: kirish
09:30 vali: xato (1/3)
09:50 vali: xato (1/3)
09:52 vali: xato (2/3)
```

ali uchinchi xatodan keyin 15 daqiqaga bloklandi; 09:10 dagi **toʻgʻri** parol ham qabul qilinmadi — bloklash vaqtida tizim parolni umuman tekshirmaydi, aks holda hujumchi bloklangan hisobda ham tanlashni davom ettirardi. vali ning 09:30 dagi xatosi 09:50 da 15 daqiqalik oynadan chiqib ketdi — "Reset account lockout counter after" parametri aynan shu.

## Ishlangan misol 2: parol tarixi

Birinchi satrda tarix hajmi *N*, keyingilarida parol almashtirish soʻrovlari (foydalanuvchi yangi_parol). Tizim parollarning oʻzini emas, xeshlarini saqlaydi (oʻquv maqsadida — foydalanuvchi nomi qoʻshilgan FNV-1a; Windows NT xeshini saqlaydi). Yangi parol foydalanuvchining oxirgi *N* ta parolidan biri boʻlsa — rad etiladi.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
n = int(satrlar[0][0])
tarix = {}                                       # foydalanuvchi -> oxirgi N ta xesh
for kim, parol in satrlar[1:]:
    h = format(fnv1a(kim + ":" + parol), "08x")  # parolning oʻzi saqlanmaydi
    oxirgi = tarix.setdefault(kim, [])
    if h in oxirgi:
        print(f"{kim}: rad - oxirgi {n} ta paroldan biri")
    else:
        oxirgi.append(h)
        if len(oxirgi) > n:
            oxirgi.pop(0)                        # eng eskisi unutiladi
        print(f"{kim}: qabul qilindi [{h}]")
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
        Map<String, List<String>> tarix = new HashMap<>();        // foydalanuvchi -> oxirgi N ta xesh
        while (in.hasNext()) {
            String kim = in.next(), parol = in.next();
            String h = String.format("%08x", fnv1a(kim + ":" + parol));   // parolning oʻzi saqlanmaydi
            List<String> oxirgi = tarix.computeIfAbsent(kim, k -> new ArrayList<>());
            if (oxirgi.contains(h)) {
                System.out.println(kim + ": rad - oxirgi " + n + " ta paroldan biri");
            } else {
                oxirgi.add(h);
                if (oxirgi.size() > n) oxirgi.remove(0);             // eng eskisi unutiladi
                System.out.println(kim + ": qabul qilindi [" + h + "]");
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
    map<string, deque<string>> tarix;                            // foydalanuvchi -> oxirgi N ta xesh
    string kim, parol;
    while (cin >> kim >> parol) {
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", fnv1a(kim + ":" + parol));   // parolning oʻzi saqlanmaydi
        string h = buf;
        deque<string>& oxirgi = tarix[kim];
        if (find(oxirgi.begin(), oxirgi.end(), h) != oxirgi.end()) {
            cout << kim << ": rad - oxirgi " << n << " ta paroldan biri" << endl;
        } else {
            oxirgi.push_back(h);
            if ((int) oxirgi.size() > n) oxirgi.pop_front();      // eng eskisi unutiladi
            cout << kim << ": qabul qilindi [" << h << "]" << endl;
        }
    }
    return 0;
}
```

Kirish:

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

Chiqish:

```text
ali: qabul qilindi [9c7e87c1]
ali: qabul qilindi [c24f5411]
ali: rad - oxirgi 3 ta paroldan biri
vali: qabul qilindi [d9787e95]
ali: qabul qilindi [f4d41845]
ali: qabul qilindi [32f0b1c1]
ali: qabul qilindi [9c7e87c1]
```

vali ham `Qwerty!1` ni tanladi, lekin xeshi boshqacha — foydalanuvchi nomi "tuz" vazifasini bajardi. Oxirgi soʻrovda ali eski parolga qaytdi: uch marta almashtirgandan keyin u tarixdan chiqib ketgan edi. Agar "Minimum password age" = 0 boʻlsa, foydalanuvchi parolni bir daqiqada 24 marta almashtirib, eski sevimli paroliga qaytishi mumkin — shuning uchun minimal muddat 1 kun qilib qoʻyiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Bloklash chegarasini 1–3 qilish.** Hujumchi xodimlarni ataylab bloklashi mumkin; 5–10 va vaqtinchalik bloklash muvozanatliroq.
- **"Minimum password age" = 0 va parol tarixi bilan birga.** Tarix amalda ishlamaydi — foydalanuvchi uni aylanib oʻtadi.
- **Reversible encryption ni yoqish.** Parollar tiklanadigan koʻrinishda saqlanadi — bu ochiq saqlash bilan deyarli teng.
- **Barcha kompyuterlarda bir xil mahalliy administrator paroli.** Bitta kompyuter buzilsa — hammasi buziladi; LAPS ishlating.
- **Jurnallarni koʻrmaslik.** 4625 va 4740 hodisalarining keskin oʻsishi — hujum belgisi.
:::

:::tip
`net accounts` natijasini fayl sifatida saqlang (`net accounts > siyosat_oldin.txt`) va oʻzgarishlardan keyin solishtiring. Domen muhitida turli guruhlar uchun turli talablar kerak boʻlsa (masalan, administratorlarga 20 belgi), Active Directory dagi **Fine-Grained Password Policy** dan foydalaniladi.
:::

## Xulosa

- Parol siyosati: tarix (24), minimal muddat (1 kun), minimal uzunlik (14), murakkablik, reversible encryption — oʻchirilgan.
- Bloklash siyosati: chegara (5–10), davomiylik va hisoblagichni tiklash (15 daqiqa); juda past chegara — xizmatdan voz kechish xavfi.
- Vositalar: `net accounts`, `net user`, `secpol.msc`, `gpedit.msc`/GPO, `gpupdate`, `gpresult`.
- Jurnal: 4624, 4625, 4740, 4723/4724; parol purkash — koʻp hisoblarga bir xil manzildan 4625.
- Windows Hello va LAPS — parolga bogʻliqlikni va umumiy parollar xavfini kamaytiradi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun "Minimum password age" 0 dan katta boʻlishi kerak?
A: Aks holda foydalanuvchi parolni ketma-ket koʻp marta almashtirib, parol tarixini aylanib oʻtadi va eski paroliga qaytadi.
Q: Bloklash chegarasi 1 qilib qoʻyilsa, qanday xavf paydo boʻladi?
A: Xizmatdan voz kechish: hujumchi yoki tasodifiy xato foydalanuvchilarni osongina bloklaydi.
Q: Windows hodisalar jurnalida hisob bloklanishi qaysi Event ID bilan yoziladi?
A: 4740.
Q: Bloklangan hisobga toʻgʻri parol kiritilsa, nima uchun ham rad etiladi?
A: Bloklash davomida tizim parolni tekshirmaydi — aks holda hujumchi tanlashni davom ettira olardi.
Q: LAPS qanday muammoni hal qiladi?
A: Barcha kompyuterlarda bir xil mahalliy administrator paroli boʻlishini — har biriga tasodifiy noyob parol beradi.
:::

:::link https://learn.microsoft.com/en-us/windows/security/threat-protection/security-policy-settings/password-policy
Microsoft Learn: Password Policy
Windows parol siyosati parametrlari va tavsiya etilgan qiymatlar (ingliz tilida).
:::
