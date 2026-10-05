---
title: Internet tarmogʻi va DNS xizmati. DNS xizmati va uning ishlash tamoyili
summary: Internet tuzilishi — provayderlar, avtonom tizimlar, almashinuv nuqtalari; domen nomlari ierarxiyasi — ildiz, yuqori darajali domenlar (TLD), zonalar; DNS server turlari; rekursiv va iterativ soʻrovlar; keshlash va TTL; resurs yozuvlari — A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; DNS xabari va nomning kodlanishi; nslookup va dig; DNS xavfsizligi.
---

## Nima uchun bu mavzu muhim?

Odam `aifu.uz` ni eslab qoladi, kompyuter esa `185.x.x.x` bilan ishlaydi. Har safar sayt ochilganda, xat yuborilganda yoki ilova serverga ulanganda avval **DNS** (Domain Name System) nomni manzilga aylantiradi. DNS ishlamasa — "Internet ishlamayapti" deb tuyuladi, garchi barcha kabellar joyida boʻlsa ham. DNS — Internetning "telefon kitobi" va eng muhim taqsimlangan maʼlumotlar bazasi.

:::note
**Bu darsdan soʻng siz:**

- Internetning provayderlar va avtonom tizimlardan iborat tuzilishini tushuntira olasiz;
- domen nomlari ierarxiyasini va zona tushunchasini ayta olasiz;
- rekursiv va iterativ soʻrovlar orqali nom qanday hal qilinishini bosqichma-bosqich tushuntira olasiz;
- asosiy resurs yozuvlari turlarini va ularning vazifasini bilasiz;
- `nslookup` yoki `dig` bilan DNS yozuvlarini tekshira olasiz.
:::

## Internet tuzilishi

Internet — yagona tarmoq emas, balki **avtonom tizimlarning** (AS) birlashmasi. Har bir AS — provayder, universitet yoki yirik kompaniya tarmogʻi, oʻz raqamiga (ASN) ega. Provayderlar darajalarga boʻlinadi: **Tier 1** — butun dunyo boʻylab magistrallarga ega, bir-biri bilan bepul almashadi; **Tier 2** — mintaqaviy; **Tier 3** — oxirgi foydalanuvchilarga xizmat qiladi. Tarmoqlar **IXP** (Internet Exchange Point) nuqtalarida bevosita ulanadi va **BGP** orqali marshrutlar almashadi. Manzillar va ASN larni **IANA** va mintaqaviy registrlar (Oʻzbekiston uchun **RIPE NCC**) taqsimlaydi.

## Domen nomlari ierarxiyasi

```text
                              . (ildiz)
          ┌──────────┬────────┼─────────┬──────────┐
         com        org      uz        net        edu        ← yuqori darajali domenlar (TLD)
          │                  │
       google              aifu                                ← ikkinchi daraja
          │                  │
         www         www   lms   mail                          ← xostlar / quyi domenlar
```

Nom oʻngdan chapga oʻqiladi: `lms.aifu.uz.` — ildiz (`.`), `uz` TLD, `aifu` domeni, `lms` xosti. Toʻliq nom (FQDN) oxiridagi nuqta ildizni bildiradi. TLD turlari: **gTLD** (umumiy: .com, .org, .net, .info), **ccTLD** (mamlakat: .uz, .ru, .de), yangi gTLD lar (.app, .dev). `.uz` zonasini Oʻzbekistonda **UZINFOCOM** boshqaradi.

**Zona** — bitta tashkilot boshqaradigan nomlar maydoni qismi. `aifu.uz` egasi oʻz zonasidagi yozuvlarni oʻzi belgilaydi, `uz` zonasida esa faqat "aifu.uz uchun javobni falon serverlardan soʻrang" (**delegatsiya**, NS yozuvlari) turadi.

## DNS serverlar va nomni hal qilish

| Server | Vazifasi |
|---|---|
| **Rekursiv resolver** (provayder, 8.8.8.8, 1.1.1.1) | mijoz uchun javobni oxirigacha topib beradi va keshlaydi |
| **Ildiz serverlar** | 13 ta nom (a…m.root-servers.net), dunyo boʻylab yuzlab nusxa; TLD serverlarini koʻrsatadi |
| **TLD serverlar** | domen uchun vakolatli serverlarni koʻrsatadi |
| **Vakolatli** (authoritative) server | zona yozuvlarining asl manbai |

`lms.aifu.uz` ni hal qilish:

1. Brauzer va OS **keshini** tekshiradi (va `hosts` faylini).
2. Kompyuter rekursiv resolverga **rekursiv** soʻrov yuboradi: "toʻliq javobni ber".
3. Resolver keshida yoʻq boʻlsa, **iterativ** soʻraydi: ildizdan → "`uz` ni falon serverdan soʻra"; `uz` TLD serveridan → "`aifu.uz` ni ns1.aifu.uz dan soʻra"; vakolatli serverdan → "`lms.aifu.uz` = 185.74.5.10".
4. Resolver javobni mijozga qaytaradi va yozuvning **TTL** vaqti davomida keshlaydi.

DNS soʻrovlari odatda **UDP 53** orqali, katta javoblar va zonalarni koʻchirish (AXFR) — **TCP 53** orqali.

## Resurs yozuvlari

| Tur | Vazifasi | Misol |
|---|---|---|
| **A** | nom → IPv4 | `aifu.uz. A 185.74.5.10` |
| **AAAA** | nom → IPv6 | `aifu.uz. AAAA 2a00:1d40::10` |
| **CNAME** | taxallus → asl nom | `www.aifu.uz. CNAME aifu.uz.` |
| **MX** | domen pochta serveri (ustuvorlik bilan) | `aifu.uz. MX 10 mail.aifu.uz.` |
| **NS** | zonaning vakolatli serverlari | `aifu.uz. NS ns1.aifu.uz.` |
| **PTR** | IP → nom (teskari zona) | `10.5.74.185.in-addr.arpa. PTR aifu.uz.` |
| **TXT** | matn: SPF, domen egaligini tasdiqlash | `aifu.uz. TXT "v=spf1 mx -all"` |
| **SOA** | zona parametrlari: asosiy server, seriya raqami, taymerlar | — |

Har bir yozuvning **TTL** i (soniyalarda) — uni qancha vaqt keshlash mumkinligi. Kichik TTL — oʻzgarishlar tez tarqaladi, lekin serverlarga yuk koʻp; katta TTL — aksincha.

## DNS xabari va nomning kodlanishi

DNS xabari: 12 baytli sarlavha (ID, bayroqlar, savollar va javoblar soni), soʻng **savol** boʻlimi va **javob**, **vakolat**, **qoʻshimcha** boʻlimlari. Nom matn sifatida emas, **yorliqlar** ketma-ketligi sifatida kodlanadi: har bir yorliq oldidan uning uzunligi (1 bayt), oxirida — ildizni bildiruvchi `00`. Yorliq 63 baytdan, butun nom 255 baytdan oshmaydi.

## Ishlangan misol 1: nomni DNS formatida kodlash

Har bir satrda domen nomi. Uning DNS xabaridagi koʻrinishini (baytlarni oʻn oltilik) va baytlar sonini chiqaring. Yorliq boʻsh yoki 63 belgidan uzun boʻlsa, yoxud harf, raqam va `-` dan boshqa belgi boʻlsa — `notoʻgʻri`. Oxirgi nuqta (ildiz) ruxsat etiladi.

```python
import sys

for nom in sys.stdin.read().split():
    toza = nom[:-1] if nom.endswith(".") else nom
    yorliqlar = toza.split(".")
    togri = all(0 < len(y) <= 63 and all(c.isascii() and (c.isalnum() or c == "-") for c in y)
                for y in yorliqlar)
    if not togri:
        print(nom, "notoʻgʻri")
        continue
    baytlar = []
    for y in yorliqlar:
        baytlar.append(len(y))                 # yorliq uzunligi
        baytlar.extend(ord(c) for c in y)
    baytlar.append(0)                          # ildiz
    print(nom, " ".join(format(b, "02x") for b in baytlar), "(" + str(len(baytlar)) + ")")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String toza = nom.endsWith(".") ? nom.substring(0, nom.length() - 1) : nom;
            String[] yorliqlar = toza.split("\\.", -1);
            boolean togri = true;
            for (String y : yorliqlar) {
                if (y.isEmpty() || y.length() > 63 || !y.matches("[A-Za-z0-9-]+")) togri = false;
            }
            if (!togri) {
                System.out.println(nom + " notoʻgʻri");
                continue;
            }
            List<Integer> baytlar = new ArrayList<>();
            for (String y : yorliqlar) {
                baytlar.add(y.length());                       // yorliq uzunligi
                for (char c : y.toCharArray()) baytlar.add((int) c);
            }
            baytlar.add(0);                                    // ildiz
            StringBuilder hex = new StringBuilder();
            for (int b : baytlar) hex.append(hex.length() > 0 ? " " : "").append(String.format("%02x", b));
            System.out.println(nom + " " + hex + " (" + baytlar.size() + ")");
        }
    }
}
```
```cpp
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string nom;
    while (cin >> nom) {
        string toza = !nom.empty() && nom.back() == '.' ? nom.substr(0, nom.size() - 1) : nom;
        vector<string> yorliqlar;
        size_t boshi = 0;
        while (true) {
            size_t nuqta = toza.find('.', boshi);
            yorliqlar.push_back(toza.substr(boshi, nuqta == string::npos ? string::npos : nuqta - boshi));
            if (nuqta == string::npos) break;
            boshi = nuqta + 1;
        }
        bool togri = true;
        for (auto& y : yorliqlar) {
            if (y.empty() || y.size() > 63) togri = false;
            for (unsigned char c : y) {
                if (!(isalnum(c) || c == '-') || c > 127) togri = false;
            }
        }
        if (!togri) {
            cout << nom << " notoʻgʻri" << endl;
            continue;
        }
        vector<int> baytlar;
        for (auto& y : yorliqlar) {
            baytlar.push_back(y.size());                       // yorliq uzunligi
            for (unsigned char c : y) baytlar.push_back(c);
        }
        baytlar.push_back(0);                                  // ildiz
        string hex;
        char buf[4];
        for (size_t i = 0; i < baytlar.size(); i++) {
            snprintf(buf, sizeof buf, "%02x", baytlar[i]);
            hex += (i ? " " : "") + string(buf);
        }
        cout << nom << " " << hex << " (" << baytlar.size() << ")" << endl;
    }
    return 0;
}
```

Kirish:

```text
www.aifu.uz
aifu.uz.
mail..aifu.uz
my_site.uz
```

Chiqish:

```text
www.aifu.uz 03 77 77 77 04 61 69 66 75 02 75 7a 00 (13)
aifu.uz. 04 61 69 66 75 02 75 7a 00 (9)
mail..aifu.uz notoʻgʻri
my_site.uz notoʻgʻri
```

Nuqtalar xabarga yozilmaydi — ularning oʻrnida uzunlik baytlari turadi. `my_site` dagi `_` xost nomida ruxsat etilmaydi (baʼzi xizmat yozuvlari, masalan `_dmarc`, bundan mustasno).

## Ishlangan misol 2: CNAME zanjirini hal qilish

Birinchi satrda yozuvlar soni *k*, keyin *k* ta yozuv: nom, tur (`A` yoki `CNAME`) va qiymat. Keyin *q* ta soʻrov — nom. Har bir soʻrov uchun CNAME zanjiri boʻylab A yozuvigacha boring va yoʻlni chiqaring. Yozuv topilmasa — `NXDOMAIN`; zanjir 8 qadamdan oshsa — `halqa`.

```python
k = int(input())
cname = {}
a = {}
for _ in range(k):
    nom, tur, qiymat = input().split()
    if tur == "CNAME":
        cname[nom] = qiymat
    else:
        a[nom] = qiymat
q = int(input())
for _ in range(q):
    nom = input().strip()
    yol = [nom]
    while nom in cname and len(yol) <= 8:
        nom = cname[nom]                       # taxallusdan asl nomga
        yol.append(nom)
    if len(yol) > 8:
        print(" -> ".join(yol[:3]) + " ... halqa")
    elif nom in a:
        print(" -> ".join(yol) + " = " + a[nom])
    else:
        print(" -> ".join(yol) + " NXDOMAIN")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        Map<String, String> cname = new HashMap<>();
        Map<String, String> a = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String nom = in.next();
            String tur = in.next();
            String qiymat = in.next();
            if (tur.equals("CNAME")) cname.put(nom, qiymat);
            else a.put(nom, qiymat);
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String nom = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(nom);
            while (cname.containsKey(nom) && yol.size() <= 8) {
                nom = cname.get(nom);                              // taxallusdan asl nomga
                yol.add(nom);
            }
            if (yol.size() > 8) {
                System.out.println(String.join(" -> ", yol.subList(0, 3)) + " ... halqa");
            } else if (a.containsKey(nom)) {
                System.out.println(String.join(" -> ", yol) + " = " + a.get(nom));
            } else {
                System.out.println(String.join(" -> ", yol) + " NXDOMAIN");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

string birlashtir(const vector<string>& v, size_t n) {
    string s;
    for (size_t i = 0; i < n; i++) s += (i ? " -> " : "") + v[i];
    return s;
}

int main() {
    int k;
    cin >> k;
    map<string, string> cname, a;
    for (int i = 0; i < k; i++) {
        string nom, tur, qiymat;
        cin >> nom >> tur >> qiymat;
        if (tur == "CNAME") cname[nom] = qiymat;
        else a[nom] = qiymat;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string nom;
        cin >> nom;
        vector<string> yol = {nom};
        while (cname.count(nom) && yol.size() <= 8) {
            nom = cname[nom];                                      // taxallusdan asl nomga
            yol.push_back(nom);
        }
        if (yol.size() > 8) {
            cout << birlashtir(yol, 3) << " ... halqa" << endl;
        } else if (a.count(nom)) {
            cout << birlashtir(yol, yol.size()) << " = " << a[nom] << endl;
        } else {
            cout << birlashtir(yol, yol.size()) << " NXDOMAIN" << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
6
aifu.uz A 185.74.5.10
www.aifu.uz CNAME aifu.uz
lms.aifu.uz CNAME cdn.aifu.uz
cdn.aifu.uz A 185.74.5.20
x.aifu.uz CNAME y.aifu.uz
y.aifu.uz CNAME x.aifu.uz
4
www.aifu.uz
lms.aifu.uz
eski.aifu.uz
x.aifu.uz
```

Chiqish:

```text
www.aifu.uz -> aifu.uz = 185.74.5.10
lms.aifu.uz -> cdn.aifu.uz = 185.74.5.20
eski.aifu.uz NXDOMAIN
x.aifu.uz -> y.aifu.uz -> x.aifu.uz ... halqa
```

Haqiqiy resolverlar ham CNAME zanjiri uzunligini cheklaydi — aks holda notoʻgʻri sozlangan zona resolverni cheksiz siklga tushirardi. NXDOMAIN — "bunday nom mavjud emas" degan rasmiy DNS javobi.

:::warning
**Koʻp uchraydigan xatolar**

- **DNS faqat UDP orqali ishlaydi deb oʻylash.** Katta javoblar, DNSSEC va zonani koʻchirish TCP 53 dan foydalanadi.
- **Yozuvni oʻzgartirgach, hamma darhol yangi manzilni koʻradi deb kutish.** Eski javob TTL tugaguncha keshlarda yashaydi.
- **Domenning oʻzi (`aifu.uz`) uchun CNAME qoʻyish.** Zona choʻqqisida (apex) SOA va NS boʻlgani uchun u yerda CNAME ruxsat etilmaydi.
- **Rekursiv va iterativ soʻrovni aralashtirish.** Mijoz resolverdan rekursiv (toʻliq javob) soʻraydi; resolver ildiz, TLD va vakolatli serverlardan iterativ soʻraydi.
- **DNS ni xavfsiz deb hisoblash.** Oddiy DNS shifrlanmagan va soxtalashtirilishi mumkin; himoya — DNSSEC, DoH/DoT.
:::

:::tip
Buyruqlar satrida `nslookup aifu.uz`, `nslookup -type=MX gmail.com`, `nslookup -type=NS uz` ni bajaring (Linux/macOS da `dig aifu.uz +trace` — butun iterativ yoʻlni koʻrsatadi). Windows DNS keshini `ipconfig /displaydns` bilan koʻring va `ipconfig /flushdns` bilan tozalang.
:::

## Xulosa

- Internet — BGP orqali bogʻlangan avtonom tizimlar va provayderlar ierarxiyasi; IXP larda bevosita almashinuv.
- Domen nomlari — ildiz → TLD → domen → xost ierarxiyasi; zonalar NS yozuvlari bilan delegatsiya qilinadi.
- Hal qilish: kesh → rekursiv resolver → ildiz → TLD → vakolatli server; javob TTL davomida keshlanadi.
- Yozuvlar: A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; transport — UDP 53 (katta javoblar uchun TCP 53).
- Nom xabarda uzunlik prefiksli yorliqlar sifatida kodlanadi: yorliq ≤ 63 bayt, oxirida `00`.

## Oʻzingizni tekshiring

:::check
Q: Rekursiv va iterativ DNS soʻrovlarining farqi nimada?
A: Rekursivda server mijozga toʻliq javobni topib beradi; iterativda har bir server keyingi qaysi serverdan soʻrash kerakligini aytadi.
Q: Domen pochta serveri qaysi yozuvda koʻrsatiladi?
A: MX yozuvida.
Q: TTL = 3600 boʻlgan A yozuvi oʻzgartirildi. Eski manzil qancha vaqt koʻrinishi mumkin?
A: Bir soatgacha — keshlarda saqlangan javob TTL tugaguncha ishlatiladi.
Q: `mail.uz` nomi DNS xabarida qanday baytlar bilan kodlanadi?
A: `04 6d 61 69 6c 02 75 7a 00`.
Q: IP manzil boʻyicha nomni topish uchun qaysi yozuv ishlatiladi?
A: PTR yozuvi (in-addr.arpa teskari zonasida).
:::

:::link https://www.cloudflare.com/learning/dns/what-is-dns/
Cloudflare: What is DNS?
DNS ishlashining rasmlar bilan tushuntirilishi (ingliz tilida).
:::
