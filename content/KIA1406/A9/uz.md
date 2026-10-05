---
title: SSL/TLS sertifikatini tekshirish (brauzer misolida)
summary: Brauzerda sayt sertifikatini amalda tekshirish — qulf belgisi, sertifikatni koʻrish oynasi, egasi (Subject, SAN), beruvchi CA, amal qilish muddati, barmoq izi, sertifikat zanjiri; DV, OV va EV sertifikatlar; badssl.com da xato holatlar — muddati oʻtgan, nomi mos kelmaydigan, oʻz-oʻzini imzolagan va bekor qilingan sertifikatlar; SSL Labs va crt.sh vositalari; HSTS; sertifikat muddatini va nom mosligini dasturda tekshirish; mavzu boʻyicha test.
---

## Nima uchun bu mavzu muhim?

Brauzerdagi qulf belgisi ortida M8 va M9 da oʻrganilgan butun mexanizm turadi: X.509 sertifikati, CA imzosi, ishonch zanjiri va TLS qoʻl siqishuvi. Foydalanuvchi uchun eng muhim koʻnikma — sertifikat ogohlantirishini **tushunish** va uni eʼtiborsiz qoldirmaslik; administrator uchun — sertifikat muddati tugashidan oldin uni yangilash. Bu mashgʻulotda haqiqiy saytlar va ataylab buzilgan sinov saytlari sertifikatlarini koʻrib chiqamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- brauzerda sertifikat maydonlarini va zanjirini koʻrib, tahlil qila olasiz;
- DV, OV va EV sertifikatlar farqini ayta olasiz;
- brauzerning sertifikat xatolarini tanib, ularning sababini tushuntira olasiz;
- sertifikat muddati va domen nomi mosligini dasturda tekshira olasiz.
:::

## Amaliy ish

**1-qadam. Sertifikatni koʻrish.** Chrome yoki Edge da istalgan HTTPS saytni (masalan, my.gov.uz yoki google.com) oching → manzil satridagi belgi → "Connection is secure" → "Certificate is valid". Firefox da: qulf → "Connection secure" → "More information" → "View Certificate". Quyidagilarni yozib oling:

| Maydon | Nimani tekshirish kerak |
|---|---|
| **Issued To** (Subject, CN) | sayt nomi yoki tashkilot |
| **Subject Alternative Name** | sertifikat amal qiladigan barcha domenlar, jumladan `*.` bilan boshlanadigan wildcard nomlar |
| **Issued By** (Issuer) | beruvchi CA (masalan, Let's Encrypt, DigiCert, GlobalSign) |
| **Validity** | Not Before va Not After sanalari |
| **Public Key** | algoritm va uzunlik (RSA 2048, ECDSA P-256) |
| **Fingerprint** (SHA-256) | sertifikatning noyob barmoq izi |

"Details" yoki "Certification Path" yorligʻida **zanjirni** koʻring: sayt sertifikati → oraliq CA → ildiz CA. Ildiz sertifikat OT yoki brauzerning ishonchli roʻyxatida boʻladi (Windows da `certmgr.msc` → Trusted Root Certification Authorities).

**2-qadam. Sertifikat turlari.** Bir nechta saytni solishtiring:

| Tur | Nima tekshiriladi | Qayerda |
|---|---|---|
| **DV** (Domain Validation) | faqat domen egaligi (avtomatik, daqiqalarda) | bloglar, koʻp saytlar; Let's Encrypt bepul beradi |
| **OV** (Organization Validation) | domen + tashkilot hujjatlari | kompaniya saytlari |
| **EV** (Extended Validation) | chuqur yuridik tekshiruv | ilgari banklar; brauzerlar endi alohida yashil belgi koʻrsatmaydi |

Muhim: DV sertifikat faqat "siz shu domen bilan shifrlangan kanaldasiz" deydi, "sayt ishonchli" demaydi. Fishing saytlarining aksariyati ham HTTPS ishlatadi.

**3-qadam. Xato holatlar (badssl.com).** Quyidagi sinov saytlarini oching va har birida brauzer nima deyishini yozing (sahifaga **oʻtmang** — faqat ogohlantirishni oʻqing):

| Sayt | Muammo | Brauzer xabari (Chrome) |
|---|---|---|
| expired.badssl.com | muddati oʻtgan | NET::ERR_CERT_DATE_INVALID |
| wrong.host.badssl.com | domen nomi SAN ga mos emas | NET::ERR_CERT_COMMON_NAME_INVALID |
| self-signed.badssl.com | oʻz-oʻzini imzolagan | NET::ERR_CERT_AUTHORITY_INVALID |
| untrusted-root.badssl.com | ildiz CA ishonchli roʻyxatda yoʻq | NET::ERR_CERT_AUTHORITY_INVALID |
| revoked.badssl.com | sertifikat bekor qilingan | NET::ERR_CERT_REVOKED (brauzer bekor qilish roʻyxatini tekshirsa) |

Haqiqiy saytda bunday xabar chiqsa — bu "oʻrtadagi odam" hujumi, notoʻgʻri sozlangan server yoki kompyuterdagi notoʻgʻri sana boʻlishi mumkin. Parol yoki karta maʼlumotlarini **kiritmang**.

**4-qadam. Server tahlili.** ssllabs.com/ssltest da biror saytni tekshiring: umumiy baho (A+…F), qoʻllab-quvvatlanadigan TLS versiyalari, shifr toʻplamlari (M9), sertifikat zanjiri toʻliqligi, **HSTS** (brauzerni doim HTTPS ishlatishga majburlovchi sarlavha). crt.sh da domen nomini qidiring — **Certificate Transparency** jurnallarida shu domen uchun berilgan barcha sertifikatlar koʻrinadi; begona CA bergan sertifikat darhol aniqlanadi.

**5-qadam. Test.** Platformadagi "SSL/TLS va sertifikatlar" testini bajaring.

## Ishlangan misol 1: sertifikat muddatini kuzatish

Muddati oʻtgan sertifikat — eng koʻp uchraydigan va eng "uyatli" nosozlik: sayt bir kechada ishlamay qoladi. Sertifikatlar muddati qisqarib bormoqda: Let's Encrypt — 90 kun, CA/Browser Forum qarori boʻyicha umumiy maksimal muddat 2026-yil martdan 200 kun, 2029-yilga kelib 47 kun. Shuning uchun monitoring avtomatlashtiriladi. Har bir satrda domen, Not Before, Not After va bugungi sana (YYYY-MM-DD). Holatni chiqaring: hali kuchga kirmagan, muddati tugagan, 30 kundan kam qolgan (yangilash kerak) yoki amal qiladi.

```python
import sys


def kunlar(sana):                                  # 1970-01-01 dan beri kunlar soni
    y, m, d = map(int, sana.split("-"))
    y -= m <= 2                                    # yil martdan boshlanadi deb hisoblaymiz
    era = y // 400
    yoe = y - era * 400
    doy = (153 * (m + (-3 if m > 2 else 9)) + 2) // 5 + d - 1
    doe = yoe * 365 + yoe // 4 - yoe // 100 + doy  # kabisa yillari hisobga olinadi
    return era * 146097 + doe - 719468


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    domen, boshi, oxiri, bugun = satr.split()
    b, o, t = kunlar(boshi), kunlar(oxiri), kunlar(bugun)
    if t < b:
        holat = "hali kuchga kirmagan"
    elif t > o:
        holat = f"muddati tugagan ({t - o} kun oldin)"
    elif o - t < 30:
        holat = f"yangilang: {o - t} kun qoldi"
    else:
        holat = f"amal qiladi, {o - t} kun qoldi"
    print(f"{domen}: {holat}")
```
```java
import java.util.Scanner;

public class Main {
    static long kunlar(String sana) {                     // 1970-01-01 dan beri kunlar soni
        String[] q = sana.split("-");
        long y = Long.parseLong(q[0]), m = Long.parseLong(q[1]), d = Long.parseLong(q[2]);
        if (m <= 2) y--;                                  // yil martdan boshlanadi deb hisoblaymiz
        long era = y / 400;
        long yoe = y - era * 400;
        long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy; // kabisa yillari hisobga olinadi
        return era * 146097 + doe - 719468;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String domen = in.next();
            long b = kunlar(in.next()), o = kunlar(in.next()), t = kunlar(in.next());
            String holat;
            if (t < b) holat = "hali kuchga kirmagan";
            else if (t > o) holat = "muddati tugagan (" + (t - o) + " kun oldin)";
            else if (o - t < 30) holat = "yangilang: " + (o - t) + " kun qoldi";
            else holat = "amal qiladi, " + (o - t) + " kun qoldi";
            System.out.println(domen + ": " + holat);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

long long kunlar(const string& sana) {                    // 1970-01-01 dan beri kunlar soni
    long long y, m, d;
    sscanf(sana.c_str(), "%lld-%lld-%lld", &y, &m, &d);
    if (m <= 2) y--;                                      // yil martdan boshlanadi deb hisoblaymiz
    long long era = y / 400;
    long long yoe = y - era * 400;
    long long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    long long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;   // kabisa yillari hisobga olinadi
    return era * 146097 + doe - 719468;
}

int main() {
    string domen, boshi, oxiri, bugun;
    while (cin >> domen >> boshi >> oxiri >> bugun) {
        long long b = kunlar(boshi), o = kunlar(oxiri), t = kunlar(bugun);
        string holat;
        if (t < b) holat = "hali kuchga kirmagan";
        else if (t > o) holat = "muddati tugagan (" + to_string(t - o) + " kun oldin)";
        else if (o - t < 30) holat = "yangilang: " + to_string(o - t) + " kun qoldi";
        else holat = "amal qiladi, " + to_string(o - t) + " kun qoldi";
        cout << domen << ": " << holat << endl;
    }
    return 0;
}
```

Kirish:

```text
my.gov.uz 2026-03-01 2027-03-01 2026-10-05
bank.uz 2025-10-20 2026-10-20 2026-10-05
old.uz 2024-01-01 2026-09-25 2026-10-05
new.uz 2026-11-01 2027-11-01 2026-10-05
leap.uz 2024-01-15 2024-03-01 2024-02-28
```

Chiqish:

```text
my.gov.uz: amal qiladi, 147 kun qoldi
bank.uz: yangilang: 15 kun qoldi
old.uz: muddati tugagan (10 kun oldin)
new.uz: hali kuchga kirmagan
leap.uz: yangilang: 2 kun qoldi
```

Sanalarni kunlar soniga aylantirish oylar uzunligi va kabisa yillarini toʻgʻri hisobga oladi: 2024-02-28 dan 2024-03-01 gacha 2 kun (29-fevral bor). Brauzer ham aynan shunday solishtiradi — shuning uchun kompyuter sanasi notoʻgʻri boʻlsa, barcha saytlar "muddati oʻtgan" boʻlib koʻrinadi. Amalda monitoring tizimlari (masalan, certbot va Let's Encrypt) sertifikatni muddati tugashidan 30 kun oldin avtomatik yangilaydi.

## Ishlangan misol 2: domen nomi va SAN mosligi

Brauzer manzil satridagi nomni sertifikatning SAN roʻyxati bilan solishtiradi (RFC 6125 qoidalari). Katta-kichik harf farq qilmaydi; wildcard `*.` faqat **eng chap bitta yorliq** oʻrnida turadi va aynan bitta yorliqqa mos keladi: `*.example.uz` → `www.example.uz` ga mos, lekin `example.uz` ga ham, `a.b.example.uz` ga ham mos emas. Birinchi satrda SAN roʻyxati, keyingilarida tekshiriladigan nomlar. Mos kelgan SAN yozuvini yoki `mos emas` ni chiqaring.

```python
import sys


def mos(naqsh, nom):
    naqsh, nom = naqsh.lower(), nom.lower()        # DNS nomlarida katta-kichik harf farqsiz
    if not naqsh.startswith("*."):
        return naqsh == nom
    qism = nom.split(".", 1)                       # chap yorliq va qolgan qism
    return len(qism) == 2 and qism[0] != "" and qism[1] == naqsh[2:]


satrlar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
san = satrlar[0].split()
for nom in satrlar[1:]:
    topildi = next((n for n in san if mos(n, nom)), None)
    print(f"{nom} -> " + (f"mos ({topildi})" if topildi else "mos emas"))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean mos(String naqsh, String nom) {
        naqsh = naqsh.toLowerCase();                       // DNS nomlarida katta-kichik harf farqsiz
        nom = nom.toLowerCase();
        if (!naqsh.startsWith("*.")) return naqsh.equals(nom);
        int nuqta = nom.indexOf('.');                      // chap yorliq va qolgan qism
        return nuqta > 0 && nom.substring(nuqta + 1).equals(naqsh.substring(2));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s);
        }
        String[] san = satrlar.get(0).split("\\s+");
        for (int i = 1; i < satrlar.size(); i++) {
            String nom = satrlar.get(i);
            String topildi = null;
            for (String n : san) {
                if (mos(n, nom)) {
                    topildi = n;
                    break;
                }
            }
            System.out.println(nom + " -> " + (topildi != null ? "mos (" + topildi + ")" : "mos emas"));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

string kichik(string s) {
    transform(s.begin(), s.end(), s.begin(), [](unsigned char c) { return tolower(c); });
    return s;
}

bool mos(string naqsh, string nom) {
    naqsh = kichik(naqsh);                                 // DNS nomlarida katta-kichik harf farqsiz
    nom = kichik(nom);
    if (naqsh.rfind("*.", 0) != 0) return naqsh == nom;
    size_t nuqta = nom.find('.');                          // chap yorliq va qolgan qism
    return nuqta != string::npos && nuqta > 0 && nom.substr(nuqta + 1) == naqsh.substr(2);
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream ss(satr);
    vector<string> san;
    string n;
    while (ss >> n) san.push_back(n);
    string nom;
    while (cin >> nom) {
        string topildi;
        for (const string& x : san) {
            if (mos(x, nom)) {
                topildi = x;
                break;
            }
        }
        cout << nom << " -> " << (!topildi.empty() ? "mos (" + topildi + ")" : "mos emas") << endl;
    }
    return 0;
}
```

Kirish:

```text
example.uz *.example.uz mail.test.uz
example.uz
www.example.uz
WWW.Example.UZ
a.b.example.uz
test.uz
mail.test.uz
example.uz.evil.com
```

Chiqish:

```text
example.uz -> mos (example.uz)
www.example.uz -> mos (*.example.uz)
WWW.Example.UZ -> mos (*.example.uz)
a.b.example.uz -> mos emas
test.uz -> mos emas
mail.test.uz -> mos (mail.test.uz)
example.uz.evil.com -> mos emas
```

`example.uz.evil.com` — fishing saytlarining sevimli usuli: nom "example.uz" bilan boshlanadi, lekin aslida u `evil.com` domeni. Brauzer nomni oʻngdan, toʻliq tekshiradi va bunday nomni rad etadi — foydalanuvchi ham manzilni xuddi shunday oʻqishi kerak. wrong.host.badssl.com xatosi aynan shu tekshiruv natijasi.

:::warning
**Koʻp uchraydigan xatolar**

- **Sertifikat ogohlantirishida "Davom etish" ni bosish.** Ayniqsa jamoat Wi-Fi tarmogʻida — bu "oʻrtadagi odam" hujumining klassik belgisi.
- **"Qulf bor — sayt xavfsiz".** Qulf faqat kanal shifrlanganini bildiradi; domen nomini tekshiring.
- **Kompyuter sanasi notoʻgʻri.** Barcha saytlarda sertifikat xatosi chiqsa, avval soat va sanani tekshiring.
- **Sertifikat muddatini qoʻlda kuzatish.** Avtomatik yangilash (ACME, certbot) va muddat monitoringini sozlang.
- **Toʻliq boʻlmagan zanjir.** Server oraliq CA sertifikatini yubormasa, baʼzi qurilmalarda xato chiqadi; SSL Labs buni koʻrsatadi.
:::

:::tip
Terminalda sertifikatni koʻrish: `openssl s_client -connect my.gov.uz:443 -servername my.gov.uz </dev/null | openssl x509 -noout -subject -issuer -dates -ext subjectAltName`. Bu buyruq Subject, Issuer, amal qilish sanalari va SAN roʻyxatini chiqaradi.
:::

## Xulosa

- Sertifikatda tekshiriladi: Subject va SAN (domen), Issuer (CA), Validity (sanalar), kalit, barmoq izi va ishonch zanjiri.
- DV — faqat domen egaligi; OV va EV — tashkilot ham tekshiriladi; HTTPS sayt ishonchli ekanini kafolatlamaydi.
- Brauzer xatolari: muddati oʻtgan, nom mos emas, ishonchsiz CA, bekor qilingan — maʼlumot kiritmang.
- SSL Labs — server sozlamalari tahlili; crt.sh — Certificate Transparency jurnallari; HSTS — majburiy HTTPS.
- Muddat va nom mosligini dastur bilan tekshirish mumkin; wildcard faqat bitta chap yorliqqa mos keladi.

## Oʻzingizni tekshiring

:::check
Q: Sertifikatning qaysi maydoni u qaysi domenlar uchun amal qilishini koʻrsatadi?
A: Subject Alternative Name (SAN).
Q: *.example.uz sertifikati example.uz va a.b.example.uz uchun amal qiladimi?
A: Yoʻq: wildcard aynan bitta chap yorliqqa mos keladi — www.example.uz ga mos, example.uz va a.b.example.uz ga emas.
Q: Barcha saytlarda birdaniga "muddati oʻtgan" xatosi chiqsa, ehtimoliy sabab nima?
A: Kompyuterdagi sana yoki soat notoʻgʻri.
Q: DV sertifikatli sayt ishonchli tashkilotga tegishli ekanini kafolatlaydimi?
A: Yoʻq, DV faqat domenni boshqarish huquqini tasdiqlaydi; fishing saytlar ham DV sertifikat oladi.
:::

:::link https://badssl.com/
badssl.com
Sertifikat va TLS xatolarini xavfsiz sinash uchun ataylab notoʻgʻri sozlangan sinov saytlari (ingliz tilida).
:::
