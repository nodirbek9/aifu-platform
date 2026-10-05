---
title: Kriptografik protokollar va ilovalar. SSL/TLS, IPSec, SSH, blockchain, e-hukumat va elektron toʻlovlar
summary: Kriptografik protokol tushunchasi; SSL/TLS — versiyalar, TLS 1.3 qoʻl siqishuvi, shifr toʻplamlari, oldinga maxfiylik (PFS); IPSec — AH, ESP, transport va tunnel rejimlari, IKE; SSH — kalit almashinuvi, kalit orqali kirish; blokcheyn — bloklar zanjiri, ish isboti (proof of work), smart-kontraktlar; e-hukumat va elektron toʻlovlarda kriptografiya — ERI, ID-karta, EMV chip, 3-D Secure, tokenizatsiya.
---

## Nima uchun bu mavzu muhim?

Oldingi darslarda alohida "gʻishtlar" — simmetrik shifrlar, RSA, Diffie–Hellman, xesh va ERI — bilan tanishdik. Amalda ular bitta-bitta emas, **protokollar** ichida birga ishlaydi: brauzerdagi qulf belgisi (TLS), ofisni filial bilan bogʻlovchi tunnel (IPSec), serverni boshqarish (SSH), bank kartasi bilan toʻlov (EMV), my.gov.uz portalidagi ERI. Protokolni tushunish — qayerda zaiflik paydo boʻlishi mumkinligini koʻrish demakdir.

:::note
**Bu darsdan soʻng siz:**

- TLS 1.3 qoʻl siqishuvi bosqichlarini va shifr toʻplami tarkibini tushuntira olasiz;
- IPSec va SSH qaysi qatlamda va qanday himoya berishini ayta olasiz;
- blokcheyn bloklari xesh orqali qanday bogʻlanishini va ish isbotini koʻrsata olasiz;
- elektron toʻlovlar va e-hukumatda kriptografiya qayerda ishlatilishini bilasiz.
:::

## Kriptografik protokol

**Kriptografik protokol** — ikki yoki undan ortiq tomon xavfsiz maqsadga (maxfiy kanal, autentifikatsiya, kalit kelishuvi) erishishi uchun bajaradigan xabarlar ketma-ketligi. Algoritm kuchli boʻlsa ham protokol xatosi butun tizimni buzishi mumkin: masalan, SSL 3.0 dagi **POODLE**, TLS dagi **Heartbleed** (OpenSSL dasturidagi xato) yoki eski versiyaga majburlash (**downgrade**) hujumlari.

## SSL/TLS

**TLS** (Transport Layer Security) — HTTPS, xavfsiz pochta (SMTPS, IMAPS) va koʻplab ilovalarning asosi. U transport qatlami (TCP) ustida ishlaydi.

| Versiya | Holati |
|---|---|
| SSL 2.0, SSL 3.0 | taqiqlangan (DROWN, POODLE) |
| TLS 1.0, TLS 1.1 | 2021-yildan rasman eskirgan (RFC 8996) |
| TLS 1.2 | ruxsat etilgan, toʻgʻri sozlansa xavfsiz |
| TLS 1.3 (2018) | tavsiya etiladi: tezroq, faqat kuchli algoritmlar |

**TLS 1.3 qoʻl siqishuvi** (handshake), soddalashtirilgan:

1. **ClientHello**: mijoz qoʻllab-quvvatlaydigan shifr toʻplamlari, tasodifiy son va oʻzining vaqtinchalik (EC)DH ochiq kaliti.
2. **ServerHello**: server shifr toʻplamini tanlaydi va oʻz (EC)DH ochiq kalitini yuboradi. Ikkala tomon endi umumiy maxfiy sonni hisoblaydi — keyingi xabarlar allaqachon shifrlangan.
3. **Certificate + CertificateVerify**: server X.509 sertifikatini va qoʻl siqishuvga qoʻygan imzosini yuboradi — mijoz sertifikat zanjirini tekshiradi.
4. **Finished**: ikkala tomon butun suhbat xeshining MAC qiymatini almashadi — hech kim xabarlarni yoʻlda oʻzgartirmaganini tasdiqlaydi.
5. Maʼlumotlar **AEAD** shifri (AES-GCM yoki ChaCha20-Poly1305) bilan uzatiladi.

**Shifr toʻplami** (cipher suite) — ishlatiladigan algoritmlar nomi. TLS 1.2 da masalan `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`: kalit almashinuvi ECDHE, server autentifikatsiyasi RSA sertifikati, shifr AES-256-GCM, xesh SHA-384. TLS 1.3 da nom qisqaroq (`TLS_AES_128_GCM_SHA256`) — kalit almashinuvi doim vaqtinchalik (EC)DHE.

**Oldinga maxfiylik** (Perfect Forward Secrecy, PFS): seans kaliti vaqtinchalik DH kalitlaridan olinadi va keyin oʻchiriladi. Server yopiq kaliti keyinroq oʻgʻirlansa ham, oldin yozib olingan trafikni ochib boʻlmaydi. Kalit RSA bilan uzatilganda (`TLS_RSA_WITH_...`) PFS yoʻq — shuning uchun TLS 1.3 bu usulni butunlay olib tashlagan.

## IPSec

**IPSec** tarmoq qatlamida (IP) ishlaydi va IP paketlarini himoyalaydi — ilovalarni oʻzgartirish shart emas. Asosiy qoʻllanishi — VPN (ofis–filial, masofaviy xodim).

| Komponent | Vazifasi |
|---|---|
| **AH** (Authentication Header) | yaxlitlik va manba autentifikatsiyasi, shifrlashsiz |
| **ESP** (Encapsulating Security Payload) | shifrlash + yaxlitlik (amalda deyarli doim ESP) |
| **IKE** (Internet Key Exchange, IKEv2) | tomonlarni autentifikatsiya qiladi va kalitlarni kelishadi (DH asosida) |
| **SA** (Security Association) | kelishilgan algoritm, kalit va muddatlar toʻplami |

**Transport rejimi** faqat paket maʼlumotini himoyalaydi (ikki xost oʻrtasida); **tunnel rejimi** butun paketni shifrlab, yangi IP sarlavhaga oʻraydi (ikki shlyuz oʻrtasida — klassik VPN).

## SSH

**SSH** (Secure Shell, 22-port) — masofaviy serverni xavfsiz boshqarish, fayl uzatish (SCP, SFTP) va tunnellash protokoli; eski, shifrlanmagan Telnet va FTP oʻrnini egallagan.

- **Kalit almashinuvi**: (EC)DH yoki Curve25519; server oʻz **xost kaliti** bilan imzolaydi. Birinchi ulanishda mijoz xost kalitining barmoq izini koʻrsatadi va `known_hosts` fayliga yozadi; keyinchalik kalit oʻzgarsa — ogohlantirish ("oʻrtadagi odam" belgisi boʻlishi mumkin).
- **Foydalanuvchi autentifikatsiyasi**: parol yoki — xavfsizroq — **kalit juftligi** (`ssh-keygen -t ed25519`): ochiq kalit serverdagi `authorized_keys` fayliga qoʻyiladi, yopiq kalit mijozda qoladi va parol iborasi bilan himoyalanadi.
- Yaxshi amaliyot: parol bilan kirishni va `root` bilan toʻgʻridan-toʻgʻri kirishni oʻchirish, kalitlardan foydalanish, kirish urinishlarini cheklash.

## Blokcheyn

**Blokcheyn** — har bir blok oldingi blok xeshini oʻz ichiga olgan bloklar zanjiri. Bitta blokdagi maʼlumot oʻzgarsa, uning xeshi oʻzgaradi va keyingi barcha bloklardagi bogʻlanish buziladi. Taqsimlangan tarmoqda minglab tugunlar zanjir nusxasini saqlaydi va **konsensus** orqali qaysi blok qoʻshilishini kelishadi:

- **Ish isboti** (Proof of Work, Bitcoin): blok qoʻshish uchun xeshi maʼlum sondagi nol bitlar bilan boshlanadigan **nonce** topish kerak — bu koʻp hisob talab qiladi, tekshirish esa bitta xesh. Zanjirni qayta yozish uchun hujumchiga tarmoq hisoblash quvvatining yarmidan koʻpi kerak ("51% hujumi").
- **Ulush isboti** (Proof of Stake, Ethereum 2022-yildan): blok yaratuvchilar garovga qoʻygan mablagʻiga qarab tanlanadi, energiya sarfi ancha kam.

Tranzaksiyalar ERI (ECDSA, Ed25519) bilan imzolanadi; manzil — ochiq kalit xeshi. **Smart-kontraktlar** — blokcheynda saqlanadigan va avtomatik bajariladigan dasturlar. Cheklovlar: tezlik past, xatoni qaytarib boʻlmaydi, yopiq kalit yoʻqolsa — mablagʻ ham yoʻqoladi.

## E-hukumat va elektron toʻlovlar

| Soha | Kriptografiya qayerda |
|---|---|
| **E-hukumat** (my.gov.uz, soliq kabineti) | TLS, ERI kaliti yoki ID-karta chipi orqali kirish, hujjatlarni ERI bilan imzolash, OneID yagona identifikatsiya tizimi |
| **Bank kartalari** (EMV chip, Uzcard, Humo, Visa) | chip har bir tranzaksiya uchun bir martalik kriptogramma hosil qiladi — magnit tasmadan farqli ravishda nusxa koʻchirish foydasiz |
| **Internet toʻlovlar** | TLS, **3-D Secure** (SMS yoki ilova orqali qoʻshimcha tasdiqlash), PCI DSS standarti |
| **Mobil toʻlovlar** (Apple Pay, Google Pay) | **tokenizatsiya**: haqiqiy karta raqami oʻrniga qurilmaga xos token ishlatiladi |
| **Mobil ilovalar** (Click, Payme) | TLS, sertifikatni mahkamlash (certificate pinning), SMS yoki biometrik tasdiqlash |

## Ishlangan misol 1: oʻquv blokcheyn

Birinchi satrda qiyinlik *d* (bit), keyingi satrlarda tranzaksiyalar. Har bir blok uchun shunday eng kichik nonce ni toping: "raqam|oldingi_xesh|tranzaksiya|nonce" satrining 32 bitli FNV-1a xeshining yuqori *d* biti nol boʻlsin. Birinchi blokning oldingi xeshi — 00000000. (Haqiqiy blokcheynda SHA-256 va ancha katta qiyinlik ishlatiladi.)

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
d = int(satrlar[0])
oldingi = "00000000"
for i, tranzaksiya in enumerate(satrlar[1:], start=1):
    nonce = 0
    while True:
        h = fnv1a(f"{i}|{oldingi}|{tranzaksiya}|{nonce}")
        if h >> (32 - d) == 0:                  # yuqori d bit nol — ish isboti topildi
            break
        nonce += 1
    xesh = format(h, "08x")
    print(f"#{i} prev={oldingi} nonce={nonce} hash={xesh} {tranzaksiya}")
    oldingi = xesh                                # keyingi blok shu xeshga bogʻlanadi
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
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) satrlar.add(s);
        }
        int d = Integer.parseInt(satrlar.get(0).trim());
        String oldingi = "00000000";
        for (int i = 1; i < satrlar.size(); i++) {
            String tranzaksiya = satrlar.get(i);
            int nonce = 0;
            int h;
            while (true) {
                h = fnv1a(i + "|" + oldingi + "|" + tranzaksiya + "|" + nonce);
                if ((h >>> (32 - d)) == 0) break;      // yuqori d bit nol — ish isboti topildi
                nonce++;
            }
            String xesh = String.format("%08x", h);
            System.out.println("#" + i + " prev=" + oldingi + " nonce=" + nonce + " hash=" + xesh + " " + tranzaksiya);
            oldingi = xesh;                              // keyingi blok shu xeshga bogʻlanadi
        }
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

int main() {
    vector<string> satrlar;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) satrlar.push_back(s);
    }
    int d = stoi(satrlar[0]);
    string oldingi = "00000000";
    for (size_t i = 1; i < satrlar.size(); i++) {
        const string& tranzaksiya = satrlar[i];
        long long nonce = 0;
        uint32_t h;
        while (true) {
            h = fnv1a(to_string(i) + "|" + oldingi + "|" + tranzaksiya + "|" + to_string(nonce));
            if ((h >> (32 - d)) == 0) break;            // yuqori d bit nol — ish isboti topildi
            nonce++;
        }
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", h);
        string xesh = buf;
        cout << "#" << i << " prev=" << oldingi << " nonce=" << nonce << " hash=" << xesh << " " << tranzaksiya << endl;
        oldingi = xesh;                                  // keyingi blok shu xeshga bogʻlanadi
    }
    return 0;
}
```

Kirish:

```text
16
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
```

Chiqish:

```text
#1 prev=00000000 nonce=64572 hash=00001674 Ali->Vali:5
#2 prev=00001674 nonce=2317 hash=000048c2 Vali->Sardor:2
#3 prev=000048c2 nonce=143961 hash=0000d51e Sardor->Ali:1
```

Har bir blok xeshi 16 ta nol bit (4 ta hex nol) bilan boshlanadi — buning uchun oʻrtacha 2¹⁶ ≈ 65 ming urinish kerak, tekshirish esa bitta xesh. Endi hujumchi birinchi tranzaksiyani `Ali->Vali:9` ga oʻzgartirdi deylik — xuddi shu dastur boshqa zanjir chiqaradi:

```text
#1 prev=00000000 nonce=31445 hash=00008283 Ali->Vali:9
#2 prev=00008283 nonce=12125 hash=00003031 Vali->Sardor:2
#3 prev=00003031 nonce=17797 hash=00005e59 Sardor->Ali:1
```

Bitta blok oʻzgarishi keyingi **barcha** bloklarning xeshi va nonce larini qayta hisoblashni talab qiladi — tarmoqdagi boshqa tugunlar esa eski zanjirni saqlaydi va soxtasini rad etadi.

## Ishlangan misol 2: TLS shifr toʻplamlarini tahlil qilish

Har bir soʻz — shifr toʻplami nomi. Uni qismlarga ajrating: kalit almashinuvi (kx), autentifikatsiya (auth), shifr va xesh; PFS borligini aniqlang va baho bering: nomda NULL, EXPORT, RC4, DES (3DES ham) yoki MD5 boʻlsa — `zaif`; PFS boʻlmasa yoki CBC rejimi boʻlsa — `eskirgan`; aks holda — `kuchli`. `_WITH_` boʻlmagan nom — TLS 1.3 toʻplami: kalit almashinuvi (EC)DHE, autentifikatsiya — sertifikat.

```python
import sys

ZAIF = ["NULL", "EXPORT", "RC4", "DES", "MD5"]   # "3DES" ham "DES" ni oʻz ichiga oladi

for nom in sys.stdin.read().split():
    if "_WITH_" in nom:                          # TLS 1.2 va oldingi: TLS_<kx>_<auth>_WITH_<shifr>_<xesh>
        chap, shifr = nom[4:].split("_WITH_")
        qismlar = chap.split("_")
        almashinuv = qismlar[0]
        autent = qismlar[1] if len(qismlar) > 1 else qismlar[0]
    else:                                        # TLS 1.3: kalit almashinuvi nomda yoʻq
        shifr = nom[4:]
        almashinuv, autent = "(EC)DHE", "cert"
    shifr, xesh = shifr.rsplit("_", 1)
    pfs = "DHE" in almashinuv                    # vaqtinchalik DH — oldinga maxfiylik
    if any(z in nom for z in ZAIF):
        baho = "zaif"
    elif not pfs or "CBC" in shifr:
        baho = "eskirgan"
    else:
        baho = "kuchli"
    print(f"{nom}: kx={almashinuv} auth={autent} cipher={shifr} hash={xesh} pfs={'+' if pfs else '-'} -> {baho}")
```
```java
import java.util.Scanner;

public class Main {
    static final String[] ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" ham "DES" ni oʻz ichiga oladi

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String shifr, almashinuv, autent;
            int w = nom.indexOf("_WITH_");
            if (w >= 0) {                                // TLS 1.2 va oldingi: TLS_<kx>_<auth>_WITH_<shifr>_<xesh>
                String[] qismlar = nom.substring(4, w).split("_");
                shifr = nom.substring(w + 6);
                almashinuv = qismlar[0];
                autent = qismlar.length > 1 ? qismlar[1] : qismlar[0];
            } else {                                     // TLS 1.3: kalit almashinuvi nomda yoʻq
                shifr = nom.substring(4);
                almashinuv = "(EC)DHE";
                autent = "cert";
            }
            int p = shifr.lastIndexOf('_');
            String xesh = shifr.substring(p + 1);
            shifr = shifr.substring(0, p);
            boolean pfs = almashinuv.contains("DHE");    // vaqtinchalik DH — oldinga maxfiylik
            boolean zaif = false;
            for (String z : ZAIF) {
                if (nom.contains(z)) zaif = true;
            }
            String baho;
            if (zaif) baho = "zaif";
            else if (!pfs || shifr.contains("CBC")) baho = "eskirgan";
            else baho = "kuchli";
            System.out.println(nom + ": kx=" + almashinuv + " auth=" + autent + " cipher=" + shifr
                    + " hash=" + xesh + " pfs=" + (pfs ? "+" : "-") + " -> " + baho);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const vector<string> ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" ham "DES" ni oʻz ichiga oladi
    string nom;
    while (cin >> nom) {
        string shifr, almashinuv, autent;
        size_t w = nom.find("_WITH_");
        if (w != string::npos) {                         // TLS 1.2 va oldingi: TLS_<kx>_<auth>_WITH_<shifr>_<xesh>
            string chap = nom.substr(4, w - 4);
            shifr = nom.substr(w + 6);
            size_t t = chap.find('_');
            almashinuv = chap.substr(0, t);
            autent = t == string::npos ? almashinuv : chap.substr(t + 1, chap.find('_', t + 1) - t - 1);
        } else {                                         // TLS 1.3: kalit almashinuvi nomda yoʻq
            shifr = nom.substr(4);
            almashinuv = "(EC)DHE";
            autent = "cert";
        }
        size_t p = shifr.rfind('_');
        string xesh = shifr.substr(p + 1);
        shifr = shifr.substr(0, p);
        bool pfs = almashinuv.find("DHE") != string::npos;   // vaqtinchalik DH — oldinga maxfiylik
        bool zaif = false;
        for (const string& z : ZAIF) {
            if (nom.find(z) != string::npos) zaif = true;
        }
        string baho;
        if (zaif) baho = "zaif";
        else if (!pfs || shifr.find("CBC") != string::npos) baho = "eskirgan";
        else baho = "kuchli";
        cout << nom << ": kx=" << almashinuv << " auth=" << autent << " cipher=" << shifr
             << " hash=" << xesh << " pfs=" << (pfs ? "+" : "-") << " -> " << baho << endl;
    }
    return 0;
}
```

Kirish:

```text
TLS_AES_128_GCM_SHA256
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256
TLS_RSA_WITH_AES_128_CBC_SHA
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA
TLS_RSA_WITH_RC4_128_MD5
```

Chiqish:

```text
TLS_AES_128_GCM_SHA256: kx=(EC)DHE auth=cert cipher=AES_128_GCM hash=SHA256 pfs=+ -> kuchli
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384: kx=ECDHE auth=RSA cipher=AES_256_GCM hash=SHA384 pfs=+ -> kuchli
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256: kx=ECDHE auth=ECDSA cipher=CHACHA20_POLY1305 hash=SHA256 pfs=+ -> kuchli
TLS_RSA_WITH_AES_128_CBC_SHA: kx=RSA auth=RSA cipher=AES_128_CBC hash=SHA pfs=- -> eskirgan
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA: kx=DHE auth=RSA cipher=3DES_EDE_CBC hash=SHA pfs=+ -> zaif
TLS_RSA_WITH_RC4_128_MD5: kx=RSA auth=RSA cipher=RC4_128 hash=MD5 pfs=- -> zaif
```

Server sozlamalarida faqat "kuchli" toʻplamlar qoldirilishi kerak. `TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA` da PFS bor, lekin 3DES ning 64 bitli bloki **Sweet32** hujumiga zaif — bitta kuchli qism butun toʻplamni xavfsiz qilmaydi. Haqiqiy serverni tekshirish uchun SSL Labs (ssllabs.com/ssltest) yoki `nmap --script ssl-enum-ciphers` kabi vositalar ishlatiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **"HTTPS bor — demak sayt ishonchli".** TLS faqat kanalni himoyalaydi; fishing sayti ham bepul sertifikat olishi mumkin. Domen nomini tekshiring.
- **Eski versiyalarni "moslik uchun" yoqib qoʻyish.** SSL 3.0, TLS 1.0/1.1 va zaif toʻplamlar downgrade hujumlariga yoʻl ochadi.
- **SSH xost kaliti ogohlantirishini eʼtiborsiz qoldirish.** Kalit kutilmaganda oʻzgargan boʻlsa, avval sababini aniqlang.
- **Blokcheynni "buzib boʻlmaydigan" deb hisoblash.** Zanjir yaxlit, lekin smart-kontrakt xatolari, birjalarning buzilishi va yopiq kalit oʻgʻirlanishi bilan katta mablagʻlar yoʻqotilgan.
- **Bank kartasi maʼlumotlari va SMS kodini hech kimga aytmaslik qoidasini buzish.** 3-D Secure kodi — aynan sizning tasdigʻingiz; "bank xodimi" ga aytilgan kod hujumchiga toʻlov qilishga imkon beradi.
:::

:::tip
Brauzerda F12 → Security yorligʻida joriy saytning TLS versiyasi va shifr toʻplamini koʻrishingiz mumkin. Terminalda `openssl s_client -connect example.com:443 -tls1_3` sertifikat zanjiri va kelishilgan toʻplamni chiqaradi.
:::

## Xulosa

- Kriptografik protokol algoritmlarni xabarlar ketma-ketligida birlashtiradi; protokol xatosi kuchli algoritmni ham foydasiz qiladi.
- TLS 1.3: (EC)DHE kalit kelishuvi, sertifikat bilan autentifikatsiya, AEAD shifr, majburiy PFS; SSL va TLS 1.0/1.1 eskirgan.
- IPSec — IP qatlamida (AH, ESP, IKE; transport va tunnel rejimlari); SSH — xost kaliti, kalit juftligi bilan kirish.
- Blokcheyn — xesh bilan bogʻlangan bloklar zanjiri va konsensus (PoW, PoS); bitta blok oʻzgarsa, keyingilari ham buziladi.
- E-hukumat va toʻlovlar: ERI, ID-karta, EMV kriptogrammasi, 3-D Secure, tokenizatsiya.

## Oʻzingizni tekshiring

:::check
Q: Oldinga maxfiylik (PFS) nimani kafolatlaydi?
A: Server yopiq kaliti keyinchalik oʻgʻirlansa ham, oldin yozib olingan seanslarni ochib boʻlmaydi — seans kalitlari vaqtinchalik DH kalitlaridan olinadi.
Q: IPSec ning tunnel rejimi transport rejimidan nimasi bilan farq qiladi?
A: Tunnel rejimi butun IP paketni shifrlab, yangi sarlavhaga oʻraydi (shlyuzlar oʻrtasida VPN); transport rejimi faqat maʼlumot qismini himoyalaydi.
Q: SSH da parol oʻrniga kalit juftligidan foydalanishning afzalligi nima?
A: Yopiq kalit tarmoq orqali uzatilmaydi va uni tanlab topib boʻlmaydi; parolni taxmin qilish hujumlari foydasiz boʻladi.
Q: Blokcheyndagi eski blokni oʻzgartirish nima uchun qiyin?
A: Blok xeshi oʻzgaradi va keyingi barcha bloklar uchun ish isbotini qayta bajarish kerak; qolgan tugunlar esa asl zanjirni saqlaydi.
Q: Tokenizatsiya mobil toʻlovlarda nimani himoyalaydi?
A: Haqiqiy karta raqamini — savdo nuqtasiga faqat qurilmaga xos token beriladi, u oʻgʻirlansa ham boshqa joyda foydasiz.
:::

:::link https://www.rfc-editor.org/rfc/rfc8446
RFC 8446: The Transport Layer Security (TLS) Protocol Version 1.3
TLS 1.3 protokolining rasmiy spetsifikatsiyasi (ingliz tilida).
:::
