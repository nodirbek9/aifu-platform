---
title: Identifikatsiya va autentifikatsiya. Parollar, biometrik tizimlar, koʻp omilli autentifikatsiya
summary: Identifikatsiya, autentifikatsiya va avtorizatsiya farqi; autentifikatsiya omillari — bilim, egalik, xususiyat; parollar — entropiya, hujumlar (toʻliq tanlash, lugʻat, parol purkash, credential stuffing), NIST SP 800-63B tavsiyalari, parol menejerlari; bir martalik parollar (HOTP, TOTP), tokenlar, smart-kartalar; biometrik autentifikatsiya — FAR, FRR, EER, tirik ekanini aniqlash; koʻp omilli autentifikatsiya va FIDO2/passkey; yagona kirish (SSO), Kerberos, OAuth 2.0 va OpenID Connect.
---

## Nima uchun bu mavzu muhim?

Ruxsatsiz kirishning eng koʻp uchraydigan yoʻli — zaif tizimni buzish emas, balki **birovning hisobidan kirish**: oʻgʻirlangan yoki taxmin qilingan parol, fishing orqali olingan SMS kod. Verizon DBIR hisobotlarida maʼlumotlar sizishining katta qismi oʻgʻirlangan hisob maʼlumotlari bilan bogʻliq. Shuning uchun "kim tizimga kiryapti?" degan savolga ishonchli javob berish — xavfsizlikning asosiy vazifalaridan biri.

:::note
**Bu darsdan soʻng siz:**

- identifikatsiya, autentifikatsiya va avtorizatsiyani farqlay olasiz;
- autentifikatsiya omillarini va ularning kuchli va zaif tomonlarini ayta olasiz;
- parol kuchini entropiya bilan baholab, parol siyosatiga zamonaviy talablarni bilasiz;
- biometrik tizim xatolarini (FAR, FRR, EER) hisoblay olasiz;
- MFA, FIDO2, SSO va OAuth ning vazifasini tushuntira olasiz.
:::

## Asosiy tushunchalar

| Tushuncha | Savol | Misol |
|---|---|---|
| **Identifikatsiya** | Siz kimsiz (deb daʼvo qilasiz)? | login, email, ID-karta raqami |
| **Autentifikatsiya** | Buni qanday isbotlaysiz? | parol, SMS kod, barmoq izi |
| **Avtorizatsiya** | Sizga nimaga ruxsat? | faylni oʻqish, buyurtmani tasdiqlash (M11) |
| **Hisobdorlik** (accounting) | Nima qildingiz? | kirish jurnallari, audit |

Autentifikatsiya **omillari**:

| Omil | Misollar | Zaif tomoni |
|---|---|---|
| **Bilim** (something you know) | parol, PIN, maxfiy savol | taxmin qilinadi, fishing bilan oʻgʻirlanadi, takror ishlatiladi |
| **Egalik** (something you have) | telefon, apparat kalit, smart-karta, token | yoʻqolishi yoki oʻgʻirlanishi mumkin |
| **Xususiyat** (something you are) | barmoq izi, yuz, koʻz qorachigʻi, ovoz | oʻzgartirib boʻlmaydi; xato ehtimoli bor |

Qoʻshimcha belgilar — joylashuv, vaqt, qurilma, xulq-atvor (yozish tezligi) — **adaptiv** autentifikatsiyada xavf darajasini baholash uchun ishlatiladi.

## Parollar

Parol kuchi uning **entropiyasi** bilan oʻlchanadi: *N* ta belgidan tasodifiy tanlangan *L* uzunlikdagi parol uchun *H* = *L* · log₂ *N* bit. Har bir qoʻshimcha bit tanlash ishini ikki baravar oshiradi. Lekin bu formula faqat **tasodifiy** parollarga toʻgʻri keladi: `Parol2024!` formal jihatdan murakkab, lekin lugʻatlarda bor.

| Hujum | Mohiyati | Himoya |
|---|---|---|
| **Toʻliq tanlash** (brute force) | barcha variantlar | uzunlik, sekin xesh (M8), urinishlarni cheklash |
| **Lugʻat hujumi** | mashhur parollar va ularning oʻzgarishlari | sizib chiqqan parollar roʻyxati bilan tekshirish |
| **Parol purkash** (password spraying) | bitta mashhur parol koʻp hisoblarga | MFA, anomaliyalarni kuzatish |
| **Credential stuffing** | boshqa saytdan sizgan login-parol juftlari | har bir sayt uchun alohida parol, MFA |
| **Fishing, keylogger** | parolni foydalanuvchidan olish | MFA (ayniqsa FIDO2), xabardorlik |

**NIST SP 800-63B** zamonaviy tavsiyalari: kamida 8 belgi (yaxshisi 15+), 64 tagacha belgiga ruxsat, boʻshliq va har qanday belgilar mumkin; murakkablik qoidalari ("katta harf + raqam + belgi") va **muntazam majburiy almashtirish talab qilinmaydi** — ular odamlarni oldindan aytib boʻladigan parollarga undaydi; buning oʻrniga parol sizib chiqqanlar roʻyxatida tekshiriladi, urinishlar cheklanadi va parol faqat buzilish belgisi boʻlsa almashtiriladi. Eng amaliy yechim — **parol menejeri** (har bir sayt uchun tasodifiy uzun parol) yoki bir nechta tasodifiy soʻzlardan iborat **parol iborasi**.

## Bir martalik parollar va tokenlar

- **HOTP** (RFC 4226): kod = HMAC(kalit, hisoblagich) dan olingan 6 ta raqam; har bir foydalanishda hisoblagich oshadi.
- **TOTP** (RFC 6238): hisoblagich oʻrniga vaqt (odatda 30 soniyalik oraliq) — Google Authenticator, Microsoft Authenticator. Kalit QR kod orqali bir marta beriladi.
- **SMS kod**: qulay, lekin eng zaif ikkinchi omil — SIM almashtirish (SIM swap), SS7 zaifliklari, fishing.
- **Push-tasdiq**: "Siz kiryapsizmi?" — "MFA charchoqi" hujumi: hujumchi foydalanuvchi "Ha" ni bosguncha soʻrov yuboradi; himoya — ekrandagi raqamni kiritish (number matching).
- **Smart-karta va apparat kalitlar** (YubiKey): yopiq kalit qurilmadan chiqmaydi.

## Biometrik autentifikatsiya

Biometrik tizim namunani (barmoq izi, yuz) shablon bilan solishtirib, **oʻxshashlik bali** hisoblaydi va uni **chegara** bilan taqqoslaydi. Ikki turdagi xato:

| Koʻrsatkich | Maʼnosi | Chegara oshirilsa |
|---|---|---|
| **FAR** (False Acceptance Rate) | begona odam qabul qilindi | kamayadi |
| **FRR** (False Rejection Rate) | haqiqiy egasi rad etildi | oshadi |
| **EER** (Equal Error Rate) | FAR = FRR boʻlgan nuqta | — tizimlarni solishtirish uchun |

Bank uchun past FAR muhim (begonani kiritmaslik), ofis eshigi uchun — past FRR (xodimlarni navbatda qoldirmaslik). Biometriyaning oʻziga xos muammolari: uni **almashtirib boʻlmaydi** (barmoq izi sizsa — umrbod), soxta namuna (rasm, silikon barmoq) — shuning uchun **tirik ekanini aniqlash** (liveness detection) kerak; shaxsiy maʼlumot sifatida qonun bilan himoyalanadi. Odatda biometrika qurilmadagi yopiq kalitni "ochadi", serverga esa yuborilmaydi.

## Koʻp omilli autentifikatsiya va FIDO2

**MFA** — kamida ikki **turli** omil (parol + telefon); ikki parol — MFA emas. Microsoft maʼlumotlariga koʻra MFA avtomatlashtirilgan hisob hujumlarining 99% dan ortigʻini toʻxtatadi. Lekin SMS va TOTP kodlarini fishing sayti "oʻrtada turib" soʻrab olishi mumkin.

**FIDO2 / WebAuthn (passkey)** fishingga chidamli: qurilma har bir sayt uchun alohida kalit juftligini yaratadi, autentifikatsiyada server yuborgan tasodifiy soʻrovni yopiq kalit bilan imzolaydi (M8 dagi ERI gʻoyasi). Imzo **domen nomiga bogʻlangan** — soxta saytga imzo ishlamaydi; serverda parol ham, sir ham saqlanmaydi.

## Yagona kirish (SSO) va federatsiya

- **Kerberos** (Windows Active Directory): foydalanuvchi bir marta autentifikatsiyadan oʻtib, **chipta** (ticket) oladi; xizmatlarga parol oʻrniga chipta koʻrsatiladi. Kalit tarqatish markazi (KDC) — AS va TGS.
- **SAML 2.0**: korporativ veb-ilovalar uchun XML asosidagi SSO.
- **OAuth 2.0**: avtorizatsiya protokoli — ilovaga parolni bermasdan, cheklangan huquqli **token** berish ("Google orqali ruxsat berish").
- **OpenID Connect**: OAuth 2.0 ustidagi autentifikatsiya qatlami — "Google bilan kirish"; foydalanuvchi haqidagi maʼlumot ID token (JWT) da.
- Oʻzbekistonda **OneID** — davlat xizmatlari uchun yagona identifikatsiya tizimi (login-parol, ERI kaliti, ID-karta yoki mobil ilova orqali).

## Ishlangan misol 1: parol entropiyasi va tanlash vaqti

Har bir satrda alifbo hajmi *N* va uzunlik *L*. Entropiya *H* = *L* · log₂ *N* ni va sekundiga 10¹⁰ urinish (GPU fermasining tezkor xeshga hujumi) tezligida oʻrtacha tanlash vaqtini (barcha variantlarning yarmi, *N*^*L* / 2) chiqaring. Vaqt 1 soniyadan kam boʻlsa, soniyalarda ilmiy koʻrinishda, aks holda mos birlikda (daqiqa, soat, kun, yil).

```python
import math
import sys

TEZLIK = 1e10                                       # sekundiga urinishlar
BIRLIKLAR = [(365.25 * 86400, "yil"), (86400, "kun"), (3600, "soat"), (60, "daqiqa"), (1, "soniya")]

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    n, l = map(int, satr.split())
    bit = l * math.log2(n)
    vaqt = float(n) ** l / 2 / TEZLIK               # oʻrtacha — variantlarning yarmi
    if vaqt < 1:
        matn = f"{vaqt:.2e} soniya"
    else:
        for hajm, nom in BIRLIKLAR:                 # eng katta mos birlik
            if vaqt >= hajm:
                break
        matn = f"{vaqt / hajm:.1f} {nom}"
    print(f"N={n} L={l}: {bit:.1f} bit, ~{matn}")
```
```java
import java.util.Scanner;

public class Main {
    static final double TEZLIK = 1e10;                         // sekundiga urinishlar
    static final double[] HAJM = {365.25 * 86400, 86400, 3600, 60, 1};
    static final String[] NOM = {"yil", "kun", "soat", "daqiqa", "soniya"};

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int n = in.nextInt();
            int l = in.nextInt();
            double bit = l * (Math.log(n) / Math.log(2));
            double vaqt = Math.pow(n, l) / 2 / TEZLIK;          // oʻrtacha — variantlarning yarmi
            String matn;
            if (vaqt < 1) {
                matn = String.format("%.2e soniya", vaqt);
            } else {
                int i = 0;
                while (vaqt < HAJM[i]) i++;                       // eng katta mos birlik
                matn = String.format("%.1f %s", vaqt / HAJM[i], NOM[i]);
            }
            System.out.println(String.format("N=%d L=%d: %.1f bit, ~%s", n, l, bit, matn));
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
    const double TEZLIK = 1e10;                                  // sekundiga urinishlar
    const double HAJM[] = {365.25 * 86400, 86400, 3600, 60, 1};
    const char* NOM[] = {"yil", "kun", "soat", "daqiqa", "soniya"};
    int n, l;
    while (cin >> n >> l) {
        double bit = l * log2((double) n);
        double vaqt = pow((double) n, l) / 2 / TEZLIK;           // oʻrtacha — variantlarning yarmi
        char matn[64];
        if (vaqt < 1) {
            snprintf(matn, sizeof matn, "%.2e soniya", vaqt);
        } else {
            int i = 0;
            while (vaqt < HAJM[i]) i++;                          // eng katta mos birlik
            snprintf(matn, sizeof matn, "%.1f %s", vaqt / HAJM[i], NOM[i]);
        }
        printf("N=%d L=%d: %.1f bit, ~%s\n", n, l, bit, matn);
    }
    return 0;
}
```

Kirish:

```text
10 4
10 6
26 8
62 8
94 12
7776 6
```

Chiqish:

```text
N=10 L=4: 13.3 bit, ~5.00e-07 soniya
N=10 L=6: 19.9 bit, ~5.00e-05 soniya
N=26 L=8: 37.6 bit, ~10.4 soniya
N=62 L=8: 47.6 bit, ~3.0 soat
N=94 L=12: 78.7 bit, ~754050.2 yil
N=7776 L=6: 77.5 bit, ~350270.5 yil
```

4 va 6 xonali PIN lar oʻgʻirlangan xeshdan bir zumda tiklanadi — ular faqat urinishlar soni cheklangan joyda (bank kartasi: 3 urinish) xavfsiz. 8 ta kichik harf — 10 soniya, 8 ta harf-raqam — 3 soat. 12 belgili tasodifiy parol yoki 6 ta tasodifiy soʻzdan iborat ibora (Diceware lugʻati, 7776 soʻz) yuz minglab yil talab qiladi — va iborani eslab qolish ancha oson. Natija **faqat tasodifiy** parollar uchun toʻgʻri: odam oʻylab topgan parollarni lugʻat hujumi tezroq topadi. Bu yerda faqat *N* va *L* berilgan; parolning oʻzini tahlil qilish — A10 amaliy topshirigʻi.

## Ishlangan misol 2: biometrik tizimda FAR va FRR

Birinchi satrda haqiqiy egalarning oʻxshashlik ballari, ikkinchisida begona odamlarning ballari, uchinchisida sinaladigan chegaralar. Ball ≥ chegara boʻlsa — qabul qilinadi. Har bir chegara uchun FAR va FRR ni hamda ular eng yaqin boʻlgan chegarani (EER ga yaqin nuqta) chiqaring.

```python
import sys

satrlar = sys.stdin.read().split("\n")
asl = [float(x) for x in satrlar[0].split()]       # haqiqiy egalar ballari
yot = [float(x) for x in satrlar[1].split()]       # begonalar ballari
eng, farq = None, 2.0
for t in [float(x) for x in satrlar[2].split()]:
    far = sum(1 for x in yot if x >= t) / len(yot)   # begona qabul qilindi
    frr = sum(1 for x in asl if x < t) / len(asl)    # egasi rad etildi
    print(f"t={t:.2f} FAR={100 * far:.1f}% FRR={100 * frr:.1f}%")
    if abs(far - frr) < farq:
        eng, farq = t, abs(far - frr)
print(f"EER ~ t={eng:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static double[] oqi(String satr) {
        String[] q = satr.trim().split("\\s+");
        double[] a = new double[q.length];
        for (int i = 0; i < q.length; i++) a[i] = Double.parseDouble(q[i]);
        return a;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double[] asl = oqi(in.nextLine());                 // haqiqiy egalar ballari
        double[] yot = oqi(in.nextLine());                 // begonalar ballari
        double[] chegaralar = oqi(in.nextLine());
        double eng = 0, farq = 2.0;
        for (double t : chegaralar) {
            int qabul = 0, rad = 0;
            for (double x : yot) if (x >= t) qabul++;      // begona qabul qilindi
            for (double x : asl) if (x < t) rad++;         // egasi rad etildi
            double far = (double) qabul / yot.length;
            double frr = (double) rad / asl.length;
            System.out.println(String.format("t=%.2f FAR=%.1f%% FRR=%.1f%%", t, 100 * far, 100 * frr));
            if (Math.abs(far - frr) < farq) {
                eng = t;
                farq = Math.abs(far - frr);
            }
        }
        System.out.println(String.format("EER ~ t=%.2f", eng));
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<double> oqi(const string& satr) {
    istringstream ss(satr);
    vector<double> a;
    double x;
    while (ss >> x) a.push_back(x);
    return a;
}

int main() {
    string s1, s2, s3;
    getline(cin, s1);
    getline(cin, s2);
    getline(cin, s3);
    vector<double> asl = oqi(s1);                          // haqiqiy egalar ballari
    vector<double> yot = oqi(s2);                          // begonalar ballari
    double eng = 0, farq = 2.0;
    for (double t : oqi(s3)) {
        int qabul = 0, rad = 0;
        for (double x : yot) if (x >= t) qabul++;          // begona qabul qilindi
        for (double x : asl) if (x < t) rad++;             // egasi rad etildi
        double far = (double) qabul / yot.size();
        double frr = (double) rad / asl.size();
        printf("t=%.2f FAR=%.1f%% FRR=%.1f%%\n", t, 100 * far, 100 * frr);
        if (fabs(far - frr) < farq) {
            eng = t;
            farq = fabs(far - frr);
        }
    }
    printf("EER ~ t=%.2f\n", eng);
    return 0;
}
```

Kirish:

```text
0.91 0.85 0.78 0.95 0.74 0.88 0.72 0.93 0.81 0.58
0.12 0.35 0.48 0.22 0.67 0.31 0.54 0.09 0.41 0.73
0.50 0.60 0.70 0.80 0.90
```

Chiqish:

```text
t=0.50 FAR=30.0% FRR=0.0%
t=0.60 FAR=20.0% FRR=10.0%
t=0.70 FAR=10.0% FRR=10.0%
t=0.80 FAR=0.0% FRR=40.0%
t=0.90 FAR=0.0% FRR=70.0%
EER ~ t=0.70
```

Chegara oshgani sari FAR kamayadi, FRR esa oshadi — ikkalasini bir vaqtda nolga tushirib boʻlmaydi. *t* = 0,80 da begonalar butunlay toʻxtatildi, lekin har beshta egadan ikkitasi rad etiladi. Haqiqiy tizimlarda minglab namunalar ustida sinov oʻtkaziladi; telefonlarni barmoq izi va yuz bilan ochish uchun ishlab chiqaruvchilar taxminan 1/50 000 dan 1/1 000 000 gacha FAR ni eʼlon qiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Identifikatsiyani autentifikatsiya deb hisoblash.** Login yoki ID raqami — sir emas; uni bilish shaxsni isbotlamaydi.
- **Bitta parolni koʻp saytda ishlatish.** Bitta sayt sizsa, credential stuffing barcha hisoblarni ochadi.
- **"Murakkab, lekin qisqa" parol.** `P@ss1!` dan `uzum-tog-kitob-sariq` kabi uzun ibora ancha kuchli.
- **SMS kodni yoki push-soʻrovni oʻylamay tasdiqlash.** Kirishni siz boshlamagan boʻlsangiz — rad eting va parolni almashtiring.
- **Biometrikani parol kabi "almashtirsa boʻladi" deb oʻylash.** Sizgan barmoq izini almashtirib boʻlmaydi; biometrik shablonlar shifrlangan va qurilmada saqlanishi kerak.
:::

:::tip
haveibeenpwned.com saytida emailingiz maʼlum sizishlarda borligini tekshiring. Muhim hisoblaringizda (email, bank, davlat xizmatlari) MFA ni yoqing, imkon boʻlsa — passkey yoki autentifikator ilovasini, SMS emas.
:::

## Xulosa

- Identifikatsiya — kimligini daʼvo qilish, autentifikatsiya — isbotlash, avtorizatsiya — ruxsatlarni aniqlash.
- Omillar: bilim, egalik, xususiyat; MFA — kamida ikki turli omil.
- Parol kuchi — entropiya (*L* · log₂ *N*); uzunlik va tasodifiylik muhim; NIST: majburiy murakkablik va davriy almashtirish oʻrniga sizganlar roʻyxati, cheklov va parol menejeri.
- OTP (HOTP, TOTP), push, apparat kalitlar; FIDO2/passkey — fishingga chidamli.
- Biometriya: FAR va FRR chegaraga qarama-qarshi bogʻliq, EER — solishtirish nuqtasi; tirik ekanini aniqlash zarur.
- SSO: Kerberos, SAML, OAuth 2.0 (avtorizatsiya), OpenID Connect (autentifikatsiya), OneID.

## Oʻzingizni tekshiring

:::check
Q: Autentifikatsiya va avtorizatsiyaning farqi nima?
A: Autentifikatsiya foydalanuvchi aytgan shaxs ekanini tekshiradi; avtorizatsiya unga qaysi amallarga ruxsat borligini aniqlaydi.
Q: Parol va maxfiy savol birgalikda MFA boʻladimi?
A: Yoʻq, ikkalasi ham bilim omili; MFA uchun turli omillar kerak.
Q: 8 ta kichik lotin harfidan iborat tasodifiy parolning entropiyasi qancha?
A: 8 · log₂ 26 ≈ 37,6 bit.
Q: Biometrik tizimda chegara oshirilsa, FAR va FRR qanday oʻzgaradi?
A: FAR kamayadi (begonalar kamroq qabul qilinadi), FRR oshadi (egalar koʻproq rad etiladi).
Q: Nima uchun FIDO2/passkey fishingga chidamli?
A: Imzo sayt domeniga bogʻlangan va yopiq kalit qurilmadan chiqmaydi — soxta sayt yaroqli imzo ololmaydi.
:::

:::link https://pages.nist.gov/800-63-4/sp800-63b.html
NIST SP 800-63B: Authentication and Authenticator Management
Parollar, OTP, biometriya va MFA boʻyicha raqamli identifikatsiya tavsiyalari (ingliz tilida).
:::
