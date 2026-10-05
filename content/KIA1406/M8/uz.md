---
title: Xesh funksiyalar va elektron raqamli imzo. Hash, MAC, ERI, PKI, X.509 sertifikatlari
summary: Xesh-funksiya tushunchasi va xossalari — bir tomonlamalik, kolliziyaga bardoshlilik, koʻchki effekti; algoritmlar — MD5, SHA-1, SHA-2, SHA-3; tugʻilgan kun paradoksi; parollarni saqlash (tuz, sekin xeshlar); MAC va HMAC; elektron raqamli imzo (ERI) — yaratish va tekshirish, RSA va ECDSA imzolari; Oʻzbekistonda ERI; PKI — sertifikatlash markazi, sertifikat zanjiri, bekor qilish; X.509 sertifikati tuzilishi.
---

## Nima uchun bu mavzu muhim?

Yuklab olingan dastur yoʻlda oʻzgartirilmaganini, bazadagi parol oʻgʻirlansa ham ochilmasligini, elektron hujjatni aynan shu rahbar imzolaganini qanday bilamiz? Javob — **xesh-funksiyalar** va ularga asoslangan **elektron raqamli imzo**. Oʻzbekistonda soliq hisobotlari, davlat xizmatlari portali va elektron hujjat almashinuvi ERI kalitlari bilan ishlaydi — har bir mutaxassis ularning ishlash tamoyilini bilishi kerak.

:::note
**Bu darsdan soʻng siz:**

- xesh-funksiyaning xossalarini va zamonaviy algoritmlarni ayta olasiz;
- koʻchki effektini va tugʻilgan kun paradoksini hisob bilan koʻrsata olasiz;
- parollarni xavfsiz saqlash usullarini tushuntira olasiz;
- MAC va ERI farqini, ERI yaratish va tekshirish bosqichlarini tushuntira olasiz;
- PKI tuzilishini va X.509 sertifikatining asosiy maydonlarini bilasiz.
:::

## Xesh-funksiya

**Kriptografik xesh-funksiya** *H* istalgan uzunlikdagi maʼlumotdan qatʼiy uzunlikdagi qiymat (xesh, "barmoq izi") hosil qiladi.

| Xossa | Maʼnosi |
|---|---|
| **Deterministiklik** | bir xil kirish — doim bir xil xesh |
| **Bir tomonlamalik** (preimage resistance) | *H*(*x*) dan *x* ni topish amalda imkonsiz |
| **Ikkinchi asosga bardoshlilik** | berilgan *x* uchun *H*(*x'*) = *H*(*x*) boʻlgan boshqa *x'* ni topib boʻlmaydi |
| **Kolliziyaga bardoshlilik** | *H*(*x*) = *H*(*y*) boʻlgan istalgan juftni topib boʻlmaydi |
| **Koʻchki effekti** | kirishdagi bitta bit oʻzgarishi xeshning ~yarim bitini oʻzgartiradi |

| Algoritm | Xesh uzunligi | Holati |
|---|---|---|
| MD5 | 128 bit | **buzilgan** (kolliziyalar sekundlarda) — xavfsizlik uchun ishlatmang |
| SHA-1 | 160 bit | **buzilgan** (2017-yil SHAttered kolliziyasi) |
| SHA-256, SHA-512 (SHA-2) | 256, 512 bit | xavfsiz, keng tarqalgan |
| SHA-3 (Keccak) | 224–512 bit | xavfsiz, boshqa ichki tuzilma ("gubka") |
| Oʻz DSt 1106:2009 | 256 bit | Oʻzbekiston milliy xesh standarti |

Qoʻllanishi: fayl yaxlitligini tekshirish (yuklab olish sahifalaridagi SHA-256 qiymatlari), parollarni saqlash, raqamli imzo, blokcheyn, raqamli kriminalistika.

**Tugʻilgan kun paradoksi**: 23 kishilik guruhda ikki kishining tugʻilgan kuni mos kelish ehtimoli 50% dan oshadi. Xeshlarda ham shunday: *b* bitli xesh uchun kolliziyani taxminan **2^(*b*/2)** urinishda topish mumkin. Shuning uchun kolliziyaga bardoshlilik uchun xesh kamida 256 bit boʻlishi kerak (128 bitli xavfsizlik).

**Parollarni saqlash**: parolning oʻzi emas, uning xeshi saqlanadi. Lekin oddiy SHA-256 yetarli emas — hujumchi tayyor jadvallar (rainbow tables) va sekundiga milliardlab urinish bilan oddiy parollarni topadi. Toʻgʻri usul: har bir foydalanuvchi uchun tasodifiy **tuz** (salt) qoʻshish va ataylab **sekin** algoritmlar — **bcrypt, scrypt, Argon2, PBKDF2** — ishlatish.

## MAC va HMAC

Xesh yaxlitlikni tekshiradi, lekin uni har kim hisoblay oladi: hujumchi faylni ham, xeshini ham almashtirishi mumkin. **MAC** (Message Authentication Code) — **maxfiy kalit** bilan hisoblanadigan nazorat qiymati: kalitni bilmagan xabarni oʻzgartirsa, MAC ni moslay olmaydi. Eng keng tarqalgan — **HMAC** = *H*((*K* ⊕ opad) ‖ *H*((*K* ⊕ ipad) ‖ *M*)), masalan HMAC-SHA256 (JWT tokenlari, API imzolari). MAC yaxlitlik va autentifikatsiyani beradi, lekin **rad etib boʻlmaslikni** bermaydi: kalit ikkala tomonda bor, MAC ni kim yaratganini isbotlab boʻlmaydi.

## Elektron raqamli imzo (ERI)

ERI asimmetrik kriptografiyaga asoslanadi: imzoni faqat **yopiq kalit** egasi yaratadi, tekshirishni **ochiq kalit** bilan har kim bajaradi.

1. **Imzolash**: hujjat xeshi *h* = *H*(*M*) hisoblanadi; *h* yopiq kalit bilan imzolanadi → imzo *S*.
2. **Yuborish**: hujjat *M* va imzo *S* (va odatda sertifikat).
3. **Tekshirish**: qabul qiluvchi *H*(*M*) ni oʻzi hisoblaydi va imzoni yuboruvchining ochiq kaliti bilan tekshiradi; mos kelsa — hujjat oʻzgarmagan va aynan shu kalit egasi imzolagan.

ERI uchta xizmatni beradi: **yaxlitlik**, **autentiklik** va **rad etib boʻlmaslik**. Algoritmlar: RSA-PSS, ECDSA, EdDSA (Ed25519); Oʻzbekistonda — Oʻz DSt 1092:2009 milliy standarti. "Elektron raqamli imzo toʻgʻrisida"gi Qonunga koʻra ERI qoʻl imzosiga tenglashtiriladi; kalitlar va sertifikatlar vakolatli markazlar tomonidan beriladi (masalan, davlat xizmatlari va soliq tizimi uchun).

## PKI va X.509 sertifikatlari

Ochiq kalit haqiqatan "Bank X" ga tegishli ekanini qanday bilamiz? **PKI** (Public Key Infrastructure) bu masalani ishonchli uchinchi tomon orqali hal qiladi.

| Element | Vazifasi |
|---|---|
| **Sertifikatlash markazi** (CA) | ochiq kalit va egasini bogʻlovchi sertifikatni oʻz imzosi bilan beradi |
| **Roʻyxatga olish markazi** (RA) | egasining shaxsini tekshiradi |
| **Ildiz CA** | oʻz-oʻzini imzolagan; ochiq kaliti OT va brauzerlarga oldindan oʻrnatilgan |
| **Oraliq CA** | ildiz CA imzolagan; foydalanuvchi sertifikatlarini beradi (ildiz kaliti oflayn saqlanadi) |
| **Bekor qilish** | muddatidan oldin bekor qilingan sertifikatlar — CRL roʻyxati yoki OCSP xizmati |

**Ishonch zanjiri**: sayt sertifikati → oraliq CA → ildiz CA. Brauzer har bir sertifikat imzosini yuqoridagisining ochiq kaliti bilan tekshiradi, ildiz esa ishonchli roʻyxatda boʻlishi kerak.

**X.509 sertifikati** maydonlari:

| Maydon | Mazmuni |
|---|---|
| Version, Serial Number | versiya (3), noyob tartib raqami |
| Signature Algorithm | CA imzo algoritmi (masalan, sha256WithRSAEncryption) |
| Issuer | sertifikatni bergan CA |
| Validity | amal qilish muddati (Not Before, Not After) |
| Subject | egasi (sayt nomi, tashkilot) |
| Subject Public Key Info | egasining ochiq kaliti |
| Extensions | Subject Alternative Name (domenlar), Key Usage, Basic Constraints va boshqalar |
| CA imzosi | yuqoridagi barcha maydonlar xeshining imzosi |

## Ishlangan misol 1: koʻchki effekti

Oʻquv maqsadida oddiy 32 bitli **FNV-1a** xesh-funksiyasini olamiz (u kriptografik emas, lekin koʻchki effektini yaxshi koʻrsatadi): *h* = 2166136261; har bir bayt uchun *h* = (*h* ⊕ bayt) · 16777619 mod 2³². Har bir satr xeshini va oldingi satr xeshidan nechta bit farq qilishini chiqaring.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF   # mod 2^32
    return h


oldingi = None
for satr in sys.stdin.read().split("\n"):
    if not satr:
        continue
    h = fnv1a(satr)
    qator = format(h, "08x") + " " + satr
    if oldingi is not None:
        qator += " (farq: " + str(bin(h ^ oldingi).count("1")) + " bit)"   # oʻzgargan bitlar
    print(qator)
    oldingi = h
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;                                       // 2166136261
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;                   // mod 2^32 (int toshishi)
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        Integer oldingi = null;
        while (in.hasNextLine()) {
            String satr = in.nextLine();
            if (satr.isEmpty()) continue;
            int h = fnv1a(satr);
            String qator = String.format("%08x", h) + " " + satr;
            if (oldingi != null) {
                qator += " (farq: " + Integer.bitCount(h ^ oldingi) + " bit)";   // oʻzgargan bitlar
            }
            System.out.println(qator);
            oldingi = h;
        }
    }
}
```
```cpp
#include <bitset>
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;                               // mod 2^32 (uint32_t toshishi)
    }
    return h;
}

int main() {
    string satr;
    bool bor = false;
    uint32_t oldingi = 0;
    while (getline(cin, satr)) {
        if (satr.empty()) continue;
        uint32_t h = fnv1a(satr);
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", h);
        string qator = string(buf) + " " + satr;
        if (bor) {
            qator += " (farq: " + to_string(bitset<32>(h ^ oldingi).count()) + " bit)";   // oʻzgargan bitlar
        }
        cout << qator << endl;
        oldingi = h;
        bor = true;
    }
    return 0;
}
```

Kirish:

```text
salom
Salom
salom!
parol123
```

Chiqish:

```text
6883e297 salom
10b9bd37 Salom (farq: 16 bit)
3d9de482 salom! (farq: 15 bit)
1e16e5c5 parol123 (farq: 12 bit)
```

Bitta harfning katta-kichikligi yoki bitta belgi qoʻshilishi 32 bitning ~yarmini oʻzgartirdi — xeshga qarab kirish qanchalik "yaqin" ekanini bilib boʻlmaydi. Lekin FNV-1a **kriptografik emas**: unda kolliziyalarni maqsadli topish oson. Haqiqiy yaxlitlik tekshiruvi uchun SHA-256 ishlatiladi — bu A8 amaliy mashgʻulotining topshirigʻi.

## Ishlangan misol 2: tugʻilgan kun paradoksi

Har bir satrda xesh uzunligi *b* (bit) va xeshlangan obyektlar soni *k*. Kolliziya ehtimoli *P* ≈ 1 − e^(−*k*(*k*−1) / 2^(*b*+1)) va 50% ehtimolga yetish uchun kerakli obyektlar soni ≈ 1,1774 · 2^(*b*/2) ni chiqaring.

```python
import math
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    b, k = map(int, satr.split())
    ehtimol = -math.expm1(-k * (k - 1.0) / 2.0 ** (b + 1))   # 1 − e^(−x) aniq hisob
    yarim = 1.1774 * 2.0 ** (b / 2)
    print(f"{b} bit, {k} ta: P = {ehtimol:.2e}, 50% uchun ~{yarim:.2e} ta")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int b = in.nextInt();
            long k = in.nextLong();
            double ehtimol = -Math.expm1(-k * (k - 1.0) / Math.pow(2.0, b + 1));   // 1 − e^(−x) aniq hisob
            double yarim = 1.1774 * Math.pow(2.0, b / 2.0);
            System.out.println(String.format("%d bit, %d ta: P = %.2e, 50%% uchun ~%.2e ta", b, k, ehtimol, yarim));
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    int b;
    long long k;
    while (cin >> b >> k) {
        double ehtimol = -expm1(-k * (k - 1.0) / pow(2.0, b + 1));    // 1 − e^(−x) aniq hisob
        double yarim = 1.1774 * pow(2.0, b / 2.0);
        printf("%d bit, %lld ta: P = %.2e, 50%% uchun ~%.2e ta\n", b, k, ehtimol, yarim);
    }
    return 0;
}
```

Kirish:

```text
32 10000
32 100000
64 100000000
128 1000000000000
```

Chiqish:

```text
32 bit, 10000 ta: P = 1.16e-02, 50% uchun ~7.72e+04 ta
32 bit, 100000 ta: P = 6.88e-01, 50% uchun ~7.72e+04 ta
64 bit, 100000000 ta: P = 2.71e-04, 50% uchun ~5.06e+09 ta
128 bit, 1000000000000 ta: P = 1.47e-15, 50% uchun ~2.17e+19 ta
```

32 bitli xeshda (4 milliard qiymat) atigi 77 ming obyektdayoq kolliziya ehtimoli 50% — shuning uchun qisqa xeshlar xavfsizlikda yaroqsiz. 128 bitda trillion obyektda ham ehtimol 10⁻¹⁵ — lekin hujumchi 2⁶⁴ urinishga qodir boʻlsa, xavf bor; zamonaviy talab — kamida 256 bit.

:::warning
**Koʻp uchraydigan xatolar**

- **Xeshni shifrlash deb hisoblash.** Xesh bir tomonlama — uni "deshifrlab" boʻlmaydi; kalit ham yoʻq.
- **Parollarni MD5 yoki tuzsiz SHA-256 bilan saqlash.** Tayyor jadvallar va GPU lar oddiy parollarni tez topadi; bcrypt yoki Argon2 ishlating.
- **MD5 va SHA-1 dan xavfsizlik uchun foydalanish.** Ikkalasi ham kolliziyalar boʻyicha buzilgan.
- **MAC ni imzo deb hisoblash.** MAC rad etib boʻlmaslikni bermaydi — kalit ikkala tomonda.
- **Sertifikat ogohlantirishlarini eʼtiborsiz qoldirish.** "Ishonchsiz sertifikat" — "oʻrtadagi odam" hujumining belgisi boʻlishi mumkin.
:::

:::tip
Windows da istalgan fayl xeshini `certutil -hashfile fayl.exe SHA256` (PowerShell da `Get-FileHash fayl.exe`) bilan hisoblang va dasturni yuklab olgan saytda eʼlon qilingan qiymat bilan solishtiring. Brauzerda istalgan HTTPS saytning qulf belgisini bosib, sertifikat zanjirini (sayt → oraliq → ildiz CA) koʻring.
:::

## Xulosa

- Xesh: qatʼiy uzunlik, bir tomonlamalik, kolliziyaga bardoshlilik, koʻchki effekti; MD5 va SHA-1 buzilgan, SHA-2 va SHA-3 xavfsiz.
- Tugʻilgan kun paradoksi: kolliziya ~2^(*b*/2) urinishda; shuning uchun 256 bit.
- Parollar: tuz + sekin algoritmlar (bcrypt, scrypt, Argon2, PBKDF2).
- MAC (HMAC) — kalitli yaxlitlik va autentifikatsiya; ERI — yopiq kalit bilan imzo, ochiq kalit bilan tekshirish, rad etib boʻlmaslikni beradi.
- PKI: CA, RA, ildiz va oraliq CA, ishonch zanjiri, CRL/OCSP; X.509 sertifikati egasi va ochiq kalitini CA imzosi bilan bogʻlaydi.

## Oʻzingizni tekshiring

:::check
Q: Kolliziyaga bardoshlilik nimani anglatadi?
A: Bir xil xeshga ega ikki xil kirishni topish amalda imkonsiz.
Q: Nima uchun parollar uchun oddiy SHA-256 emas, bcrypt yoki Argon2 tavsiya etiladi?
A: Ular ataylab sekin va tuz ishlatadi — tayyor jadvallar va katta tezlikdagi tanlashni samarasiz qiladi.
Q: MAC va ERI ning asosiy farqi nima?
A: MAC simmetrik kalit bilan hisoblanadi va rad etib boʻlmaslikni bermaydi; ERI yopiq kalit bilan yaratiladi va uni faqat egasi yaratishi mumkin.
Q: Brauzer sayt sertifikatini kim imzolaganini tekshiradi. Bu kim?
A: Sertifikatlash markazi (CA) — odatda oraliq CA, u esa ildiz CA ga bogʻlangan.
Q: X.509 sertifikatining qaysi maydoni sertifikat qaysi domenlar uchun amal qilishini koʻrsatadi?
A: Subject Alternative Name (SAN) kengaytmasi.
:::

:::link https://www.rfc-editor.org/rfc/rfc5280
RFC 5280: X.509 Public Key Infrastructure Certificate Profile
X.509 sertifikatlari va CRL tuzilishining spetsifikatsiyasi (ingliz tilida).
:::
