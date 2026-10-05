---
title: Tarmoq xavfsizligini taʼminlovchi vositalar. Firewall, VPN, IDS, DLP, honeypot
summary: Tarmoqni himoyalash vositalari — xavfsizlik devorlari (paket filtri, holatni kuzatuvchi, ilova darajasidagi proksi, NGFW, WAF), qoidalar tartibi va standart rad etish, DMZ va segmentlash; VPN — saytdan saytga va masofaviy kirish, IPSec, SSL/TLS VPN, WireGuard; IDS va IPS — imzo va anomaliya asosida aniqlash, tarmoq va xost tizimlari, Snort, Suricata; SIEM; DLP — maʼlumot sizishining oldini olish; honeypot va aldash texnologiyalari; chuqurlashtirilgan himoya va nol ishonch.
---

## Nima uchun bu mavzu muhim?

Bitta vosita tarmoqni toʻliq himoya qila olmaydi: xavfsizlik devori ruxsat etilgan port orqali kelgan hujumni koʻrmaydi, IDS uni koʻradi, lekin toʻxtatmaydi, shifrlash esa maʼlumotni tinglashdan saqlaydi, lekin sizib chiqishidan emas. Shuning uchun tashkilotlar vositalarni **qatlamlab** joylashtiradi — har biri boshqasining boʻshligʻini yopadi. Bu darsda asosiy vositalarning vazifasi, ishlash tamoyili va cheklovlarini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- xavfsizlik devorlari turlarini va qoidalar qanday tekshirilishini tushuntira olasiz;
- DMZ va tarmoqni segmentlash gʻoyasini bilasiz;
- VPN turlari va qoʻllanish holatlarini ayta olasiz;
- IDS va IPS, imzo va anomaliya asosidagi aniqlashni farqlay olasiz;
- DLP va honeypot vazifasini va chuqurlashtirilgan himoyada ularning oʻrnini tushuntira olasiz.
:::

## Xavfsizlik devorlari (firewall)

Xavfsizlik devori tarmoqlar orasidagi trafikni **siyosat** boʻyicha oʻtkazadi yoki toʻxtatadi.

| Tur | Nimani tekshiradi | Afzalligi | Cheklovi |
|---|---|---|---|
| **Paket filtri** (stateless) | har bir paketning manzili, porti, protokoli | tez, oddiy | ulanish kontekstini bilmaydi |
| **Holatni kuzatuvchi** (stateful) | ulanishlar jadvali: javob paketi soʻralgan ulanishga tegishlimi | qaytuvchi trafik avtomatik, soxta "javob"lar toʻxtaydi | ilova mazmunini koʻrmaydi |
| **Ilova darajasidagi proksi** | HTTP, SMTP kabi protokol mazmuni | nozik nazorat, mazmunni tozalash | sekinroq, har bir protokol uchun alohida |
| **NGFW** (yangi avlod) | ilovani aniqlash, foydalanuvchi, IPS, TLS ni tekshirish | koʻp vazifa bitta qurilmada | murakkab, qimmat |
| **WAF** | veb-ilovaga soʻrovlar (SQL inyeksiya, XSS belgilar) | veb-hujumlarga ixtisoslashgan | xavfsiz kodni almashtirmaydi |
| **Xost devori** | bitta kompyuter (Windows Defender Firewall, iptables/nftables) | ichki tarmoqda ham himoya | har bir xostda boshqarish kerak |

**Qoidalar tartibi**: qoidalar yuqoridan pastga tekshiriladi va **birinchi mos kelgan** qoida qoʻllanadi; hech biri mos kelmasa — oxirgi, **standart rad etish** qoidasi. Shuning uchun aniq qoidalar umumiylaridan yuqorida turishi kerak. Faqat kiruvchi emas, **chiquvchi** trafikni ham filtrlash kerak — zararlangan kompyuter tashqariga ulanishga harakat qiladi.

**DMZ va segmentlash**: tashqaridan kirish kerak boʻlgan serverlar (veb, pochta) alohida **DMZ** segmentida turadi — u buzilsa ham ichki tarmoqqa yoʻl yopiq. Ichki tarmoq ham boʻlimlar, serverlar, foydalanuvchilar, IoT qurilmalar uchun VLAN va devorlar bilan **segmentlarga** boʻlinadi — bitta segmentdagi hodisa butun tarmoqqa tarqalmaydi.

## VPN

**VPN** ochiq tarmoq (Internet) orqali shifrlangan "tunnel" quradi.

| Tur | Qoʻllanishi |
|---|---|
| **Saytdan saytga** (site-to-site) | ofis va filial tarmoqlarini doimiy bogʻlash (odatda IPSec, M9) |
| **Masofaviy kirish** (remote access) | xodim uydan yoki safardan korporativ tarmoqqa ulanadi |
| **SSL/TLS VPN** | brauzer yoki mijoz dastur orqali, 443-port; xavfsizlik devorlaridan oson oʻtadi |
| **WireGuard** | zamonaviy, kodi kichik, tez; Curve25519 va ChaCha20 |

VPN kanalni himoyalaydi, lekin ulangan qurilmaning oʻzi zararlangan boʻlsa, xavf tarmoq ichiga kiradi. Shuning uchun masofaviy kirishda **MFA** (M10), qurilma holatini tekshirish va eng kam imtiyoz (faqat kerakli tizimlarga kirish) talab qilinadi. VPN qurilmalari Internetga ochiq boʻlgani uchun ularni tez yangilab borish muhim. Zamonaviy yondashuv — **ZTNA** (nol ishonchli tarmoqqa kirish): butun tarmoq emas, har bir ilovaga alohida, har safar tekshirilgan kirish.

## IDS va IPS

| | IDS (aniqlash) | IPS (oldini olish) |
|---|---|---|
| Joylashuvi | trafik nusxasini tinglaydi (SPAN port, TAP) | trafik yoʻlida (inline) |
| Harakati | ogohlantirish beradi | paketni toʻxtatadi, ulanishni uzadi |
| Xatoning narxi | soxta ogohlantirish — ortiqcha ish | soxta toʻxtatish — xizmat uzilishi |

**Aniqlash usullari**:

- **Imzo asosida** (signature): maʼlum hujumlar namunalari bazasi (Snort, Suricata qoidalari). Aniq va tushunarli, lekin yangi (zero-day) hujumni koʻrmaydi; bazani yangilab turish kerak.
- **Anomaliya asosida**: odatiy xatti-harakat modeli (trafik hajmi, vaqt, manzillar) va undan chetga chiqishni aniqlash. Yangi hujumlarni ham topadi, lekin soxta ogohlantirishlar koʻp.
- **Xost asosidagi** tizimlar (HIDS, EDR) fayllar, jarayonlar va jurnallarni kuzatadi; **tarmoq asosidagilar** (NIDS) — trafikni. Shifrlangan trafikda NIDS mazmunni koʻrmaydi — bu yerda xost darajasidagi kuzatuv muhim.

**SIEM** (Security Information and Event Management) barcha manbalar — devorlar, IDS, serverlar, Active Directory jurnallarini bir joyga yigʻib, ularni bogʻlaydi (korrelatsiya) va xavfsizlik operatsiyalari markazi (SOC) uchun ogohlantirishlar hosil qiladi.

## DLP va honeypot

**DLP** (Data Loss Prevention) maxfiy maʼlumotning tashkilotdan chiqishini kuzatadi va toʻxtatadi: email, veb-yuklash, USB, bulut, chop etish. Maʼlumot **naqshlar** (karta raqami, pasport, JSHSHIR), kalit soʻzlar, hujjat belgilari va "barmoq izlari" boʻyicha aniqlanadi. Siyosatlar: ogohlantirish, shifrlashni majburlash, bloklash. DLP tasodifiy sizishga qarshi yaxshi ishlaydi; ataylab harakat qilayotgan insayderni toʻliq toʻxtatmaydi, lekin iz qoldiradi.

**Honeypot** — hujumchini jalb qilish uchun ataylab qoʻyilgan soxta tizim (server, xizmat, hujjat yoki hisob). Unda haqiqiy foydalanuvchi boʻlmagani uchun unga har qanday murojaat — shubhali; soxta ogohlantirishlar deyarli yoʻq. Honeypot hujumchining usullarini oʻrganish va ichki tarmoqqa kirganini erta aniqlash uchun ishlatiladi; **honeytoken** — soxta parol yoki fayl, ishlatilsa darhol signal beradi. Honeypot ishlab chiqarish tarmogʻidan ajratilgan boʻlishi kerak.

## Chuqurlashtirilgan himoya va nol ishonch

| Qatlam | Vositalar |
|---|---|
| Perimetr | xavfsizlik devori, NGFW, DDoS himoyasi, pochta filtri |
| Tarmoq ichi | segmentlash, NIDS/IPS, honeypot |
| Xost | xost devori, EDR, yangilanishlar, disk shifrlash |
| Ilova | WAF, xavfsiz kod, autentifikatsiya |
| Maʼlumot | shifrlash, DLP, zaxira nusxa |
| Kuzatuv | jurnallar, SIEM, SOC |

**Nol ishonch** (Zero Trust, NIST SP 800-207) tamoyili: "hech qachon ishonma, doim tekshir" — tarmoq ichida boʻlish ishonch sababi emas; har bir soʻrov foydalanuvchi, qurilma va kontekst boʻyicha tekshiriladi (M11 dagi ABAC gʻoyasi).

## Ishlangan misol 1: xavfsizlik devori qoidalari

Birinchi satrda qoidalar soni *r*, keyin *r* ta qoida: amal (`allow`/`deny`), protokol, manba tarmogʻi (CIDR yoki `any`), maqsad porti (yoki `any`). Keyingi satrlarda paketlar: protokol, manba IP, port. Har bir paket uchun **birinchi mos** qoidani va amalni chiqaring; hech biri mos kelmasa — `deny (default)`.

```python
import sys


def ip_son(ip):                                 # IPv4 manzil -> 32 bitli son
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def tarmoqda(ip, cidr):
    if cidr == "any":
        return True
    manzil, uzunlik = cidr.split("/")
    n = int(uzunlik)
    niqob = (0xFFFFFFFF << (32 - n)) & 0xFFFFFFFF if n else 0   # /24 -> 255.255.255.0
    return ip_son(ip) & niqob == ip_son(manzil) & niqob


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
r = int(satrlar[0][0])
qoidalar = satrlar[1:1 + r]
for proto, ip, port in satrlar[1 + r:]:
    natija = ("deny", "default")                # standart rad etish
    for i, (amal, q_proto, q_tarmoq, q_port) in enumerate(qoidalar, start=1):
        if q_proto in ("any", proto) and tarmoqda(ip, q_tarmoq) and q_port in ("any", port):
            natija = (amal, f"#{i}")
            break                               # birinchi mos qoida
    print(f"{proto} {ip} -> {port}: {natija[0]} ({natija[1]})")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long ipSon(String ip) {                         // IPv4 manzil -> 32 bitli son
        String[] q = ip.split("\\.");
        long s = 0;
        for (String x : q) s = (s << 8) | Long.parseLong(x);
        return s;
    }

    static boolean tarmoqda(String ip, String cidr) {
        if (cidr.equals("any")) return true;
        String[] q = cidr.split("/");
        int n = Integer.parseInt(q[1]);
        long niqob = n == 0 ? 0 : (0xFFFFFFFFL << (32 - n)) & 0xFFFFFFFFL;   // /24 -> 255.255.255.0
        return (ipSon(ip) & niqob) == (ipSon(q[0]) & niqob);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = Integer.parseInt(in.next());
        List<String[]> qoidalar = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            qoidalar.add(new String[] {in.next(), in.next(), in.next(), in.next()});
        }
        while (in.hasNext()) {
            String proto = in.next(), ip = in.next(), port = in.next();
            String amal = "deny", qaysi = "default";              // standart rad etish
            for (int i = 0; i < r; i++) {
                String[] q = qoidalar.get(i);
                if ((q[1].equals("any") || q[1].equals(proto)) && tarmoqda(ip, q[2])
                        && (q[3].equals("any") || q[3].equals(port))) {
                    amal = q[0];
                    qaysi = "#" + (i + 1);
                    break;                                       // birinchi mos qoida
                }
            }
            System.out.println(proto + " " + ip + " -> " + port + ": " + amal + " (" + qaysi + ")");
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

uint32_t ipSon(const string& ip) {                         // IPv4 manzil -> 32 bitli son
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return (a << 24) | (b << 16) | (c << 8) | d;
}

bool tarmoqda(const string& ip, const string& cidr) {
    if (cidr == "any") return true;
    size_t p = cidr.find('/');
    int n = stoi(cidr.substr(p + 1));
    uint32_t niqob = n == 0 ? 0 : 0xFFFFFFFFu << (32 - n);   // /24 -> 255.255.255.0
    return (ipSon(ip) & niqob) == (ipSon(cidr.substr(0, p)) & niqob);
}

int main() {
    int r;
    cin >> r;
    vector<vector<string>> qoidalar(r, vector<string>(4));
    for (auto& q : qoidalar) cin >> q[0] >> q[1] >> q[2] >> q[3];
    string proto, ip, port;
    while (cin >> proto >> ip >> port) {
        string amal = "deny", qaysi = "default";                // standart rad etish
        for (int i = 0; i < r; i++) {
            const auto& q = qoidalar[i];
            if ((q[1] == "any" || q[1] == proto) && tarmoqda(ip, q[2]) && (q[3] == "any" || q[3] == port)) {
                amal = q[0];
                qaysi = "#" + to_string(i + 1);
                break;                                           // birinchi mos qoida
            }
        }
        cout << proto << " " << ip << " -> " << port << ": " << amal << " (" << qaysi << ")" << endl;
    }
    return 0;
}
```

Kirish:

```text
5
allow tcp 10.0.0.0/8 22
deny tcp any 22
allow tcp any 443
allow udp 192.168.1.0/24 53
deny any 203.0.113.0/24 any
tcp 10.1.2.3 22
tcp 8.8.8.8 22
tcp 8.8.8.8 443
tcp 203.0.113.9 443
udp 192.168.1.77 53
udp 192.168.2.77 53
tcp 10.0.0.5 3389
```

Chiqish:

```text
tcp 10.1.2.3 -> 22: allow (#1)
tcp 8.8.8.8 -> 22: deny (#2)
tcp 8.8.8.8 -> 443: allow (#3)
tcp 203.0.113.9 -> 443: allow (#3)
udp 192.168.1.77 -> 53: allow (#4)
udp 192.168.2.77 -> 53: deny (default)
tcp 10.0.0.5 -> 3389: deny (default)
```

SSH (22-port) faqat ichki 10.0.0.0/8 tarmogʻidan ochiq. Lekin eʼtibor bering: administrator 203.0.113.0/24 tarmogʻini butunlay bloklamoqchi edi, ammo bu qoida **5-oʻrinda** — undan oldingi 3-qoida (443 hamma uchun) birinchi mos kelib, 203.0.113.9 ni oʻtkazib yubordi. Toʻgʻrisi — bloklash qoidasini roʻyxat boshiga qoʻyish. Qoidalar tartibidagi bunday xatolar — xavfsizlik devori auditida eng koʻp topiladigan muammo.

## Ishlangan misol 2: DLP — karta raqamlarini aniqlash

DLP tizimi chiquvchi xabarlarda bank karta raqamlarini qidiradi. Karta raqami — 13–19 ta raqam (chiziqchalar bilan ham yozilishi mumkin) va **Luhn** nazorat yigʻindisidan oʻtadi: oʻngdan har ikkinchi raqam ikkilantiriladi (9 dan katta boʻlsa 9 ayriladi), barcha raqamlar yigʻindisi 10 ga boʻlinishi kerak. Har bir soʻz shunday raqam boʻlsa, oxirgi 4 tasidan boshqasini `*` bilan yashiring va topilganlar sonini chiqaring.

```python
import sys


def luhn(raqamlar):
    jami = 0
    for i, ch in enumerate(reversed(raqamlar)):
        d = int(ch)
        if i % 2 == 1:                            # oʻngdan har ikkinchi raqam
            d *= 2
            if d > 9:
                d -= 9
        jami += d
    return jami % 10 == 0


topildi = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    sozlar = satr.split(" ")
    for j, soz in enumerate(sozlar):
        raqam = soz.replace("-", "")
        if raqam.isdigit() and 13 <= len(raqam) <= 19 and luhn(raqam):
            sozlar[j] = "*" * (len(raqam) - 4) + raqam[-4:]   # faqat oxirgi 4 raqam qoladi
            topildi += 1
    print(" ".join(sozlar))
print(f"topildi: {topildi}")
```
```java
import java.util.Scanner;

public class Main {
    static boolean luhn(String raqamlar) {
        int jami = 0;
        for (int i = 0; i < raqamlar.length(); i++) {
            int d = raqamlar.charAt(raqamlar.length() - 1 - i) - '0';
            if (i % 2 == 1) {                                  // oʻngdan har ikkinchi raqam
                d *= 2;
                if (d > 9) d -= 9;
            }
            jami += d;
        }
        return jami % 10 == 0;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int topildi = 0;
        while (in.hasNextLine()) {
            String satr = in.nextLine();
            if (satr.isBlank()) continue;
            String[] sozlar = satr.split(" ", -1);
            for (int j = 0; j < sozlar.length; j++) {
                String raqam = sozlar[j].replace("-", "");
                if (raqam.matches("[0-9]{13,19}") && luhn(raqam)) {
                    sozlar[j] = "*".repeat(raqam.length() - 4) + raqam.substring(raqam.length() - 4);   // faqat oxirgi 4 raqam qoladi
                    topildi++;
                }
            }
            System.out.println(String.join(" ", sozlar));
        }
        System.out.println("topildi: " + topildi);
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

bool luhn(const string& raqamlar) {
    int jami = 0;
    for (size_t i = 0; i < raqamlar.size(); i++) {
        int d = raqamlar[raqamlar.size() - 1 - i] - '0';
        if (i % 2 == 1) {                                        // oʻngdan har ikkinchi raqam
            d *= 2;
            if (d > 9) d -= 9;
        }
        jami += d;
    }
    return jami % 10 == 0;
}

int main() {
    int topildi = 0;
    string satr;
    while (getline(cin, satr)) {
        if (satr.find_first_not_of(" \t\r") == string::npos) continue;
        vector<string> sozlar;
        size_t bosh = 0, p;
        while ((p = satr.find(' ', bosh)) != string::npos) {
            sozlar.push_back(satr.substr(bosh, p - bosh));
            bosh = p + 1;
        }
        sozlar.push_back(satr.substr(bosh));
        for (string& soz : sozlar) {
            string raqam;
            for (char c : soz) if (c != '-') raqam += c;
            bool hammasi = !raqam.empty() && all_of(raqam.begin(), raqam.end(), [](unsigned char c) { return isdigit(c); });
            if (hammasi && raqam.size() >= 13 && raqam.size() <= 19 && luhn(raqam)) {
                soz = string(raqam.size() - 4, '*') + raqam.substr(raqam.size() - 4);   // faqat oxirgi 4 raqam qoladi
                topildi++;
            }
        }
        for (size_t j = 0; j < sozlar.size(); j++) cout << (j ? " " : "") << sozlar[j];
        cout << endl;
    }
    cout << "topildi: " << topildi << endl;
    return 0;
}
```

Kirish:

```text
Mijoz kartasi 4111111111111111 tolov uchun
Buyurtma raqami 1234567812345678
Karta: 5500-0000-0000-0004 muddati 12/27
Telefon 998901234567 va karta 4000056655665556
```

Chiqish:

```text
Mijoz kartasi ************1111 tolov uchun
Buyurtma raqami 1234567812345678
Karta: ************0004 muddati 12/27
Telefon 998901234567 va karta ************5556
topildi: 3
```

(Misollarda toʻlov tizimlarining rasmiy **test** raqamlari ishlatilgan.) Buyurtma raqami 16 xonali, lekin Luhn tekshiruvidan oʻtmadi — demak u karta emas, DLP uni tegmadi; telefon raqami esa juda qisqa. Nazorat yigʻindisi soxta ogohlantirishlarni keskin kamaytiradi. Haqiqiy DLP tizimlari bundan tashqari kontekst (yonidagi "karta", "CVV" soʻzlari), fayl ichidagi matn va hujjat belgilarini ham hisobga oladi.

:::warning
**Koʻp uchraydigan xatolar**

- **"Xavfsizlik devori bor — biz himoyalanganmiz".** Ruxsat etilgan portlar orqali keladigan hujumlar, fishing va ichki tahdidlarni devor toʻxtatmaydi.
- **Qoidalar tartibini tekshirmaslik.** Keng ruxsat beruvchi qoida yuqorida tursa, pastdagi taqiqlar ishlamaydi.
- **Faqat kiruvchi trafikni filtrlash.** Chiquvchi trafik nazorati zararlangan qurilmaning tashqi server bilan aloqasini toʻxtatadi.
- **IDS ogohlantirishlarini hech kim koʻrmasligi.** Kuzatilmaydigan IDS — foydasiz; ogohlantirishlar SIEM ga va masʼul shaxsga yetib borishi kerak.
- **VPN ni eskirgan holda qoldirish.** Internetga ochiq VPN shlyuzi yangilanmasa, u tarmoqqa kirishning eng oson yoʻliga aylanadi.
:::

:::tip
Windows da `wf.msc` oynasida Windows Defender Firewall ning kiruvchi va chiquvchi qoidalarini, ularning profillarini (Domain, Private, Public) koʻring. Linux da `sudo nft list ruleset` yoki `sudo iptables -L -n -v` joriy qoidalarni va har biriga mos kelgan paketlar sonini chiqaradi.
:::

## Xulosa

- Xavfsizlik devorlari: paket filtri, holatni kuzatuvchi, proksi, NGFW, WAF, xost devori; birinchi mos qoida va standart rad etish; chiquvchi trafikni ham filtrlash.
- DMZ va segmentlash hodisa tarqalishini cheklaydi.
- VPN: saytdan saytga va masofaviy kirish; MFA va yangilanishlar shart; zamonaviy alternativa — ZTNA.
- IDS aniqlaydi, IPS toʻxtatadi; imzo va anomaliya usullari bir-birini toʻldiradi; SIEM jurnallarni bogʻlaydi.
- DLP maxfiy maʼlumot chiqishini nazorat qiladi; honeypot va honeytoken hujumchini erta aniqlaydi.
- Chuqurlashtirilgan himoya va nol ishonch — vositalarni qatlamlab, har bir soʻrovni tekshirib ishlatish.

## Oʻzingizni tekshiring

:::check
Q: Holatni kuzatuvchi xavfsizlik devori oddiy paket filtridan nimasi bilan farq qiladi?
A: U ulanishlar jadvalini yuritadi va javob paketlarini faqat mavjud ulanishga tegishli boʻlsa oʻtkazadi.
Q: Nima uchun veb-server ichki tarmoqda emas, DMZ da joylashtiriladi?
A: Server buzilsa ham, hujumchi ichki tarmoqqa toʻgʻridan-toʻgʻri kira olmaydi.
Q: IDS va IPS ning asosiy farqi nima?
A: IDS faqat aniqlab ogohlantiradi, IPS trafik yoʻlida turib hujumni toʻxtatadi.
Q: Imzo asosidagi aniqlashning asosiy kamchiligi nima?
A: U faqat maʼlum hujumlarni topadi; yangi (zero-day) hujumlarni koʻrmaydi.
Q: Nima uchun honeypot ogohlantirishlarida soxta signallar kam?
A: Unda haqiqiy foydalanuvchilar yoʻq, shuning uchun unga har qanday murojaat shubhali.
:::

:::link https://csrc.nist.gov/pubs/sp/800/41/r1/final
NIST SP 800-41 Rev. 1: Guidelines on Firewalls and Firewall Policy
Xavfsizlik devorlari turlari va siyosatini loyihalash boʻyicha qoʻllanma (ingliz tilida).
:::
