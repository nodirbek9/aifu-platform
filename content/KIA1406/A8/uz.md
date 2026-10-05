---
title: Xesh funksiyalar va ERI yaratish (onlayn vositalarda)
summary: Xesh-funksiyalar va ERI bilan amaliy ishlash — CyberChef va operatsion tizim buyruqlarida (certutil, Get-FileHash, sha256sum) xesh hisoblash, yuklab olingan fayl yaxlitligini tekshirish, HMAC hisoblash, GnuPG (Kleopatra) yoki OpenSSL da ERI yaratish va tekshirish, Oʻzbekistonda E-IMZO tizimi; parollarni saqlash — tuz va sekin xeshlash; Merkle daraxti; dasturiy topshiriq — SHA-256 bilan yaxlitlikni tekshirish.
---

## Nima uchun bu mavzu muhim?

M8 maʼruzasida xesh, MAC, ERI va PKI ni nazariy koʻrdik. Endi ularni har kungi vazifalarda qoʻllaymiz: yuklab olingan dastur asl ekanini tekshirish, hujjatni imzolash va imzoni tekshirish, foydalanuvchi parolini toʻgʻri saqlash. Mashgʻulot oxirida SHA-256 asosidagi yaxlitlik tekshiruvchisini oʻzingiz yozasiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- onlayn vositalar va OT buyruqlari bilan matn va fayl xeshini hisoblay olasiz;
- yuklab olingan faylning yaxlitligini eʼlon qilingan xesh bilan tekshira olasiz;
- HMAC va ERI ni yaratib, tekshira olasiz;
- parollarni tuz va sekin xeshlash bilan saqlash nima uchun kerakligini koʻrsata olasiz;
- Merkle daraxti qanday ishlashini tushuntira olasiz.
:::

## Amaliy ish

**1-qadam. Koʻchki effekti (CyberChef).** gchq.github.io/CyberChef da "SHA2" retseptini (Size = 256) tanlang. `salom`, `Salom` va `salom ` (oxirida boʻshliq) matnlarining xeshlarini solishtiring — ular butunlay boshqacha. Keyin "MD5" va "SHA3" retseptlari bilan xesh uzunligini solishtiring (32, 64 va 64 hex belgi).

**2-qadam. Fayl yaxlitligi.** Biror rasmiy dasturni (masalan, Python, VLC yoki 7-Zip) yuklab oling va sahifada eʼlon qilingan SHA-256 qiymatini toping. Xeshni hisoblang:

```text
Windows (cmd):         certutil -hashfile fayl.exe SHA256
Windows (PowerShell):  Get-FileHash fayl.exe -Algorithm SHA256
Linux / macOS:         sha256sum fayl.exe   (macOS: shasum -a 256 fayl.exe)
```

Qiymatlar mos kelsa — fayl yoʻlda oʻzgartirilmagan. Eslatma: agar sayt buzilgan boʻlsa, hujumchi xesh qiymatini ham almashtirishi mumkin — shuning uchun ishlab chiquvchilar xesh roʻyxatini ERI (GPG imzosi) bilan ham taqdim etadi.

**3-qadam. HMAC.** CyberChef da "HMAC" retsepti (Key = `maxfiy`, Hashing function = SHA256) bilan xabarning MAC qiymatini hisoblang. Kalitni bitta harfga oʻzgartiring — MAC butunlay oʻzgaradi. Kalitni bilmaydigan odam toʻgʻri MAC yasay olmaydi.

**4-qadam. ERI yaratish va tekshirish.** Ikki yoʻldan birini tanlang:

- **GnuPG / Kleopatra** (Gpg4win, bepul): File → New Key Pair → ism va email; hujjatni tanlab "Sign" → `.sig` fayl hosil boʻladi; "Verify" bilan tekshiring. Keyin hujjatga bitta belgi qoʻshib, tekshiruv xato berishini koʻring.
- **OpenSSL** (A7 dagi kabi): `openssl dgst -sha256 -sign yopiq.pem -out imzo.bin hujjat.txt` va `-verify ochiq.pem`.

**5-qadam. Oʻzbekistonda ERI (E-IMZO).** Davlat xizmatlari va soliq tizimida ERI kaliti vakolatli markazlar (masalan, e-imzo.uz) tomonidan beriladi: kalit fayl (`.pfx`) koʻrinishida kompyuterda yoki ID-karta chipida saqlanadi, brauzerda E-IMZO moduli orqali ishlatiladi. Kalit egasining shaxsi tekshirilib, sertifikat beriladi; hujjat imzolanganda uning xeshi yopiq kalit bilan imzolanadi. **ERI kaliti va uning parolini hech kimga bermang** — u qonunan qoʻl imzongizga teng.

**6-qadam. Dastur.** Platformadagi **SHA-256 bilan yaxlitlikni tekshirish** topshirigʻini bajaring: xabarning SHA-256 qiymatini hisoblang va kutilgan qiymat bilan solishtiring (`Yaxlit` yoki `Buzilgan`). Har bir tilning standart kutubxonasidan foydalaning (Python — `hashlib`, Java — `MessageDigest`); C++ standart kutubxonasida SHA-256 yoʻq — FIPS 180-4 standarti boʻyicha oʻzingiz yozasiz.

## Ishlangan misol 1: tuz va sekin xeshlash

Har bir satrda login, parol va foydalanuvchiga xos tasodifiy tuz. Uch xil saqlash usulini solishtiring: oddiy xesh *H*(parol), tuzli xesh *H*(tuz ‖ parol) va sekin xesh — tuzli xeshni 100 000 marta qayta xeshlash. Oʻquv maqsadida *H* — 32 bitli FNV-1a (M8 dagi kabi); haqiqiy tizimlarda bcrypt, scrypt, Argon2 yoki PBKDF2 ishlatiladi.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    login, parol, tuz = satr.split()
    oddiy = fnv1a(parol)
    tuzli = fnv1a(tuz + parol)                 # tuz parolga qoʻshiladi
    h = tuzli
    for _ in range(100000):                    # ataylab sekinlashtirish (key stretching)
        h = fnv1a(format(h, "08x") + tuz)
    print(f"{login}: oddiy={oddiy:08x} tuzli={tuzli:08x} sekin={h:08x}")
```
```java
import java.nio.charset.StandardCharsets;
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
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            String login = q[0], parol = q[1], tuz = q[2];
            int oddiy = fnv1a(parol);
            int tuzli = fnv1a(tuz + parol);                  // tuz parolga qoʻshiladi
            int h = tuzli;
            for (int i = 0; i < 100000; i++) {               // ataylab sekinlashtirish (key stretching)
                h = fnv1a(String.format("%08x", h) + tuz);
            }
            System.out.println(String.format("%s: oddiy=%08x tuzli=%08x sekin=%08x", login, oddiy, tuzli, h));
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <sstream>
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
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string login, parol, tuz;
        if (!(ss >> login >> parol >> tuz)) continue;
        uint32_t oddiy = fnv1a(parol);
        uint32_t tuzli = fnv1a(tuz + parol);                 // tuz parolga qoʻshiladi
        uint32_t h = tuzli;
        char buf[16];
        for (int i = 0; i < 100000; i++) {                   // ataylab sekinlashtirish (key stretching)
            snprintf(buf, sizeof buf, "%08x", h);
            h = fnv1a(string(buf) + tuz);
        }
        printf("%s: oddiy=%08x tuzli=%08x sekin=%08x\n", login.c_str(), oddiy, tuzli, h);
    }
    return 0;
}
```

Kirish:

```text
ali qwerty123 x7Kp
vali qwerty123 Qm2z
sardor Tosh2024! b9Lr
```

Chiqish:

```text
ali: oddiy=cffd7677 tuzli=bcb1b0b1 sekin=7e749555
vali: oddiy=cffd7677 tuzli=26151749 sekin=9151da10
sardor: oddiy=9c1e624e tuzli=da7e098f sekin=feab59f8
```

Oddiy xeshda ali va vali ning qiymati bir xil — baza oʻgʻirlansa, hujumchi ularning parollari bir xil ekanini darhol koʻradi va mashhur parollarning tayyor jadvali (rainbow table) bilan barchasini birdan topadi. Tuz bilan bir xil parollar har xil xesh beradi va tayyor jadval foydasiz: har bir foydalanuvchi uchun tanlashni alohida boshlash kerak. Sekin xesh esa har bir urinishni 100 000 baravar qimmatlashtiradi — foydalanuvchi buni sezmaydi (bir marta kirish), hujumchi esa milliardlab urinishda sezadi.

## Ishlangan misol 2: Merkle daraxti

**Merkle daraxti** koʻp maʼlumotni bitta xesh — **ildiz** bilan ifodalaydi: barglar — har bir yozuv xeshi, har bir ota tugun — ikki bola xeshlarini ulab xeshlash; toq sonli darajada oxirgi element takrorlanadi. U blokcheyn bloklarida, Git da, sertifikat shaffofligi (Certificate Transparency) jurnallarida ishlatiladi. Har bir satr — bitta yozuv; har bir darajani va ildizni chiqaring (*H* — FNV-1a).

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


daraja = [format(fnv1a(s), "08x") for s in sys.stdin.read().split("\n") if s.strip()]
qadam = 0
print(f"L{qadam}: " + " ".join(daraja))
while len(daraja) > 1:
    if len(daraja) % 2 == 1:
        daraja.append(daraja[-1])                # toq boʻlsa oxirgisi takrorlanadi
    daraja = [format(fnv1a(daraja[i] + daraja[i + 1]), "08x") for i in range(0, len(daraja), 2)]
    qadam += 1
    print(f"L{qadam}: " + " ".join(daraja))
print("ildiz = " + daraja[0])
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
        List<String> daraja = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) daraja.add(String.format("%08x", fnv1a(s)));
        }
        int qadam = 0;
        System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        while (daraja.size() > 1) {
            if (daraja.size() % 2 == 1) daraja.add(daraja.get(daraja.size() - 1));   // toq boʻlsa oxirgisi takrorlanadi
            List<String> keyingi = new ArrayList<>();
            for (int i = 0; i < daraja.size(); i += 2) {
                keyingi.add(String.format("%08x", fnv1a(daraja.get(i) + daraja.get(i + 1))));
            }
            daraja = keyingi;
            qadam++;
            System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        }
        System.out.println("ildiz = " + daraja.get(0));
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

string hex8(uint32_t h) {
    char buf[16];
    snprintf(buf, sizeof buf, "%08x", h);
    return buf;
}

void chiqar(int qadam, const vector<string>& daraja) {
    cout << "L" << qadam << ":";
    for (const string& x : daraja) cout << " " << x;
    cout << endl;
}

int main() {
    vector<string> daraja;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) daraja.push_back(hex8(fnv1a(s)));
    }
    int qadam = 0;
    chiqar(qadam, daraja);
    while (daraja.size() > 1) {
        if (daraja.size() % 2 == 1) daraja.push_back(daraja.back());   // toq boʻlsa oxirgisi takrorlanadi
        vector<string> keyingi;
        for (size_t i = 0; i < daraja.size(); i += 2) {
            keyingi.push_back(hex8(fnv1a(daraja[i] + daraja[i + 1])));
        }
        daraja = keyingi;
        qadam++;
        chiqar(qadam, daraja);
    }
    cout << "ildiz = " << daraja[0] << endl;
    return 0;
}
```

Kirish:

```text
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
Ali->Olim:3
Olim->Vali:4
```

Chiqish:

```text
L0: 47af2243 bfba4043 c18587fe fd725d1a c1e3e7a7
L1: 338b6726 5b128ae6 b54962fd
L2: 2514cf2e 22ad2995
L3: 43d5aefb
ildiz = 43d5aefb
```

Toʻrtinchi yozuvni `Ali->Olim:8` ga oʻzgartirsak:

```text
L0: 47af2243 bfba4043 c18587fe f4724eef c1e3e7a7
L1: 338b6726 1f581bad b54962fd
L2: e91b25a2 22ad2995
L3: cebf2f88
ildiz = cebf2f88
```

Faqat oʻzgargan bargdan ildizgacha boʻlgan yoʻl (4 ta xesh) oʻzgardi, ildiz ham boshqacha. Shuning uchun blok sarlavhasida faqat ildizni saqlash yetarli. Bundan tashqari, bitta yozuv blokda borligini isbotlash uchun butun blok emas, faqat yoʻldagi "qoʻshni" xeshlar kerak — *n* ta yozuvda log₂ *n* ta xesh.

:::warning
**Koʻp uchraydigan xatolar**

- **Xeshni faqat oʻsha saytdan olib tekshirish.** Sayt buzilgan boʻlsa, xesh ham soxta boʻladi; imzolangan xesh roʻyxatini yoki boshqa manbani tekshiring.
- **Xesh qiymatini koʻz bilan "boshi va oxiri" boʻyicha solishtirish.** Toʻliq solishtiring yoki buyruq bilan avtomatik tekshiring.
- **Barcha foydalanuvchilarga bitta tuz.** Tuz har bir foydalanuvchi uchun alohida va tasodifiy boʻlishi kerak.
- **Matn kodlashini eʼtiborsiz qoldirish.** UTF-8 va boshqa kodlashda, satr oxiridagi `\n` yoki `\r\n` bilan xesh har xil chiqadi.
- **ERI kaliti faylini va parolini boshqalarga berish.** Bu — qoʻl imzongizni boshqaga topshirish bilan teng.
:::

:::tip
Yuklab olish sahifasida `SHA256SUMS` fayli va uning `.sig` imzosi boʻlsa, avval imzoni ishlab chiquvchining ochiq kaliti bilan (`gpg --verify SHA256SUMS.sig SHA256SUMS`), keyin faylni roʻyxat bilan (`sha256sum -c SHA256SUMS`) tekshiring — bu ikki bosqichli tekshiruv Linux distributivlarida standart amaliyot.
:::

## Xulosa

- Xeshni CyberChef, `certutil`, `Get-FileHash`, `sha256sum` bilan hisoblash mumkin; bitta belgi oʻzgarishi xeshni butunlay oʻzgartiradi.
- Fayl yaxlitligi — eʼlon qilingan xesh bilan solishtirish; ishonchli boʻlishi uchun xesh roʻyxati imzolangan boʻlishi kerak.
- HMAC — kalitli xesh; ERI — yopiq kalit bilan imzo, ochiq kalit bilan tekshirish (GnuPG, OpenSSL, E-IMZO).
- Parollar: har bir foydalanuvchiga tasodifiy tuz va ataylab sekin algoritm.
- Merkle daraxti koʻp yozuvni bitta ildiz xeshi bilan ifodalaydi; bitta yozuv oʻzgarsa, ildiz oʻzgaradi.

## Oʻzingizni tekshiring

:::check
Q: Yuklab olingan fayl xeshi sahifadagi qiymatga mos kelmasa, nima qilish kerak?
A: Faylni ishlatmaslik: u buzilgan yoki oʻzgartirilgan; rasmiy manbadan qayta yuklab, yana tekshirish.
Q: Nima uchun bir xil parolli ikki foydalanuvchining tuzli xeshlari har xil?
A: Har biriga alohida tasodifiy tuz qoʻshiladi, shuning uchun xeshlanadigan satrlar har xil.
Q: Sekin xeshlash foydalanuvchiga emas, hujumchiga qanday xalaqit beradi?
A: Foydalanuvchi bir marta kiradi va kechikishni sezmaydi; hujumchiga esa milliardlab urinishning har biri qimmatlashadi.
Q: Merkle daraxtida bitta yozuv oʻzgarsa, qaysi xeshlar oʻzgaradi?
A: Shu bargdan ildizgacha boʻlgan yoʻldagi xeshlar, jumladan ildiz.
:::

:::link https://csrc.nist.gov/pubs/fips/180-4/upd1/final
FIPS 180-4: Secure Hash Standard (SHS)
SHA-1 va SHA-2 oilasi (SHA-256, SHA-512) algoritmlarining rasmiy standarti (ingliz tilida).
:::
