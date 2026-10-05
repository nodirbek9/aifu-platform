---
title: Tarmoqlarda xavfsizlik muammolari. Xavfsizlikni taʼminlash usullari (Firewall, VPN, IDS)
summary: Axborot xavfsizligi uchligi (maxfiylik, yaxlitlik, mavjudlik); tarmoq tahdidlari — skanerlash, tinglash, soxtalashtirish, MITM, DoS/DDoS, parol tanlash, fishing, zararli dasturlar; himoya usullari — brandmauerlar (paket filtri, holatni kuzatuvchi, NGFW, WAF), DMZ, VPN (IPsec, SSL/TLS, WireGuard), IDS/IPS (imzo va anomaliya), shifrlash, autentifikatsiya va MFA; chuqur himoya; skanerlash va parol tanlashni aniqlash.
---

## Nima uchun bu mavzu muhim?

Internetga ulangan har qanday server bir necha daqiqada avtomatik skanerlanadi: kimdir ochiq portlarni, eski dasturlarni va oddiy parollarni qidiradi. Universitet tarmogʻida talabalar maʼlumotlari, baholar va moliyaviy hujjatlar saqlanadi. Bitta ochiq qolgan port yoki "123456" paroli butun tarmoqni xavf ostiga qoʻyadi. Tarmoq muhandisi hujumlar qanday ishlashini va ularga qarshi qanday himoya qatlamlari qurilishini bilishi shart.

:::note
**Bu darsdan soʻng siz:**

- axborot xavfsizligining uchta asosiy talabini tushuntira olasiz;
- asosiy tarmoq tahdidlari va hujum turlarini ayta olasiz;
- brandmauer turlarini, DMZ va VPN vazifasini tushuntira olasiz;
- IDS va IPS ning farqini, imzo va anomaliya usullarini bilasiz;
- jurnaldagi yozuvlar boʻyicha skanerlash va parol tanlash hujumlarini aniqlay olasiz.
:::

## Xavfsizlik uchligi (CIA)

| Talab | Maʼnosi | Buzilishi | Himoya |
|---|---|---|---|
| **Maxfiylik** (Confidentiality) | maʼlumotni faqat ruxsat etilganlar koʻradi | tinglash, sizib chiqish | shifrlash, kirish nazorati |
| **Yaxlitlik** (Integrity) | maʼlumot ruxsatsiz oʻzgarmaydi | soxtalashtirish, MITM | xesh, raqamli imzo, MAC |
| **Mavjudlik** (Availability) | xizmat kerak paytda ishlaydi | DoS/DDoS, nosozlik | zaxira, filtrlash, CDN |

## Tarmoq tahdidlari

| Hujum | Mazmuni | Pogʻona |
|---|---|---|
| **Skanerlash** (port scan) | ochiq portlar va xizmatlar versiyasini aniqlash (nmap) — hujum tayyorgarligi | 3–4 |
| **Tinglash** (sniffing) | shifrlanmagan trafikdan parol va maʼlumotlarni olish | 2–7 |
| **ARP/DNS spufing** | soxta javoblar bilan trafikni oʻziga yoʻnaltirish | 2, 7 |
| **MITM** (oʻrtadagi odam) | ikki tomon orasiga kirib, trafikni oʻqish va oʻzgartirish | 2–7 |
| **DoS / DDoS** | xizmatni soʻrovlar bilan toʻldirib ishdan chiqarish (SYN flood, UDP flood, HTTP flood); DDoS — minglab zararlangan qurilmalardan (botnet) | 3–7 |
| **Parol tanlash** (brute force, lugʻat hujumi) | SSH, RDP, veb-kirishda parollarni ketma-ket sinash | 7 |
| **Fishing** | soxta xat yoki sayt orqali foydalanuvchini aldab, parolni olish | ijtimoiy muhandislik |
| **Zararli dasturlar** | viruslar, qurtlar, troyanlar, shifrlovchi (ransomware) | 7 |
| **Zaifliklardan foydalanish** | yangilanmagan dasturdagi xatoni ishlatish (masalan, SMB 1 — WannaCry) | 7 |

## Brandmauerlar

**Brandmauer** (firewall) — tarmoqlar orasidagi trafikni qoidalar boʻyicha oʻtkazuvchi yoki toʻsuvchi tizim.

| Turi | Nimaga qaraydi | Xususiyati |
|---|---|---|
| **Paket filtri** (stateless) | har bir paketning IP, port, protokolini alohida | tez, oddiy; javob paketlari uchun alohida qoida kerak |
| **Holatni kuzatuvchi** (stateful) | ulanishlar jadvalini yuritadi | ichkaridan boshlangan ulanishning javobini avtomatik oʻtkazadi |
| **Ilova darajasidagi** (proxy, **WAF**) | HTTP soʻrov mazmuni | SQL injection, XSS kabi hujumlarni toʻsadi |
| **Keyingi avlod** (NGFW) | ilovalar, foydalanuvchilar, IPS, URL filtri | barchasi bitta qurilmada |

Qoidalar yuqoridan pastga tekshiriladi, **birinchi mos kelgan** qoida qoʻllanadi; oxirida — **standart siyosat** "hammasini taqiqlash" (default deny). Bunday qoidalar roʻyxatini tekshiruvchi dastur — A14 amaliy mashgʻulotining topshirigʻi.

**DMZ** (demilitarizatsiyalangan zona): Internetdan kirish kerak boʻlgan serverlar (veb, pochta) ichki tarmoqdan alohida segmentga qoʻyiladi. Veb-server buzilsa ham, hujumchi ichki tarmoqqa bevosita oʻta olmaydi.

```text
Internet ── [Brandmauer] ──┬── DMZ: veb-server, pochta serveri
                           └── [Ichki brandmauer] ── ichki tarmoq: AD, fayl serveri, kompyuterlar
```

## VPN

**VPN** (Virtual Private Network) — ochiq tarmoq (Internet) ustida **shifrlangan tunnel**. Ikki xil qoʻllanadi: **site-to-site** (filiallarni bogʻlash) va **remote access** (uydagi xodimni ofis tarmogʻiga ulash).

| Texnologiya | Pogʻona | Xususiyati |
|---|---|---|
| **IPsec** | 3 | AH (yaxlitlik), ESP (shifrlash), IKE (kalit almashish); site-to-site uchun standart |
| **SSL/TLS VPN** (OpenVPN, AnyConnect) | 4–7 | 443-port orqali, brandmauerlardan oson oʻtadi |
| **WireGuard** | 3 | zamonaviy, sodda va tez, kichik kod bazasi |

VPN trafikni shifrlaydi, lekin ulangan qurilma zararlangan boʻlsa, hujumchi ham "ichkariga" kiradi — shuning uchun VPN + MFA + qurilma tekshiruvi kerak.

## IDS va IPS

| | **IDS** (Intrusion Detection System) | **IPS** (Intrusion Prevention System) |
|---|---|---|
| Joylashuvi | trafik nusxasini tahlil qiladi (SPAN) | trafik yoʻlida turadi (inline) |
| Harakati | ogohlantiradi, jurnalga yozadi | shubhali paketni toʻsadi |
| Xavfi | hujum oʻtib ketadi | notoʻgʻri signal qonuniy trafikni toʻsadi |

Aniqlash usullari: **imzo boʻyicha** (maʼlum hujum naqshlari bazasi — Snort, Suricata qoidalari; yangi hujumni koʻrmaydi) va **anomaliya boʻyicha** (odatiy xatti-harakatdan chetlanish — masalan, bitta manzil qisqa vaqtda koʻp portga ulanadi; yangi hujumlarni ham topadi, lekin notoʻgʻri signallar koʻproq). Tarmoq IDS (NIDS) va xost IDS (HIDS) farqlanadi. Jurnallarni markazlashgan tahlil qiluvchi tizim — **SIEM**.

## Boshqa himoya choralari

- **Shifrlash**: HTTPS, SSH, SMB 3, WPA3 — maxfiylik va yaxlitlik.
- **Kuchli autentifikatsiya**: uzun parollar, **MFA** (ikki bosqichli tasdiq), kalitli SSH.
- **Yangilanishlar** va keraksiz xizmatlarni oʻchirish — hujum yuzasini kamaytirish.
- **Segmentlash** (VLAN, DMZ) — bir qism buzilsa, butun tarmoq buzilmaydi.
- **Zaxira nusxalar** (3-2-1 qoidasi) — shifrlovchi viruslarga qarshi.
- **Chuqur himoya** (defense in depth): bir nechta mustaqil himoya qatlami — biri oʻtilsa, keyingisi ushlaydi.

## Ishlangan misol 1: port skanerlashni aniqlash

Birinchi satrda chegara *N* (turli portlar soni) va oyna *W* (soniya). Keyingi satrlarda ulanish urinishlari jurnali: vaqt, manba IP va port (vaqt oʻsish tartibida). Agar bitta manba oxirgi *W* soniya ichida (vaqti *t* − *W* dan katta boʻlgan urinishlar) kamida *N* ta **turli** portga ulangan boʻlsa, birinchi marta aniqlanganda ogohlantirish chiqaring. Kirish fayl oxirigacha oʻqiladi.

```python
import sys

satrlar = sys.stdin.read().split("\n")
n, w = map(int, satrlar[0].split())
tarix = {}                                      # IP -> [(vaqt, port), ...]
aniqlangan = set()
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    t, ip, port = satr.split()
    t = int(t)
    yozuvlar = [(v, p) for v, p in tarix.get(ip, []) if v > t - w]   # faqat oynadagilar
    yozuvlar.append((t, port))
    tarix[ip] = yozuvlar
    portlar = {p for v, p in yozuvlar}
    if len(portlar) >= n and ip not in aniqlangan:
        aniqlangan.add(ip)
        print(str(t) + " " + ip + ": skanerlash (" + str(len(portlar)) + " port)")
print("shubhali manzillar: " + str(len(aniqlangan)))
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
        int n = in.nextInt();
        int w = in.nextInt();
        Map<String, List<long[]>> tarix = new HashMap<>();           // IP -> [(vaqt, port), ...]
        Set<String> aniqlangan = new HashSet<>();
        while (in.hasNextLong()) {
            long t = in.nextLong();
            String ip = in.next();
            long port = in.nextLong();
            List<long[]> yozuvlar = tarix.computeIfAbsent(ip, k -> new ArrayList<>());
            yozuvlar.removeIf(e -> e[0] <= t - w);                      // faqat oynadagilar
            yozuvlar.add(new long[] {t, port});
            Set<Long> portlar = new HashSet<>();
            for (long[] e : yozuvlar) portlar.add(e[1]);
            if (portlar.size() >= n && !aniqlangan.contains(ip)) {
                aniqlangan.add(ip);
                System.out.println(t + " " + ip + ": skanerlash (" + portlar.size() + " port)");
            }
        }
        System.out.println("shubhali manzillar: " + aniqlangan.size());
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n, w;
    cin >> n >> w;
    map<string, vector<pair<long long, long long>>> tarix;          // IP -> [(vaqt, port), ...]
    set<string> aniqlangan;
    long long t, port;
    string ip;
    while (cin >> t >> ip >> port) {
        vector<pair<long long, long long>> yozuvlar;
        for (auto& e : tarix[ip]) {
            if (e.first > t - w) yozuvlar.push_back(e);                 // faqat oynadagilar
        }
        yozuvlar.push_back({t, port});
        tarix[ip] = yozuvlar;
        set<long long> portlar;
        for (auto& e : yozuvlar) portlar.insert(e.second);
        if ((int) portlar.size() >= n && !aniqlangan.count(ip)) {
            aniqlangan.insert(ip);
            cout << t << " " << ip << ": skanerlash (" << portlar.size() << " port)" << endl;
        }
    }
    cout << "shubhali manzillar: " << aniqlangan.size() << endl;
    return 0;
}
```

Kirish (chegara: 10 soniya ichida 4 ta turli port):

```text
4 10
1 10.0.0.5 22
2 10.0.0.5 23
2 192.168.1.20 443
3 10.0.0.5 80
4 192.168.1.20 443
5 10.0.0.5 443
6 10.0.0.9 22
20 10.0.0.9 80
31 10.0.0.9 443
42 10.0.0.9 3389
```

Chiqish:

```text
5 10.0.0.5: skanerlash (4 port)
shubhali manzillar: 1
```

10.0.0.5 besh soniyada 4 ta turli portni sinadi — tipik skanerlash. 192.168.1.20 bir xil portga qayta-qayta ulanadi — oddiy foydalanuvchi. 10.0.0.9 ham 4 ta port sinagan, lekin har 10–11 soniyada bittadan — oynaga sigʻmaydi. Bu "sekin skanerlash" — IDS larni aldash usuli; shuning uchun oyna va chegarani tanlash — ehtiyotkorlik bilan qilinadigan sozlash.

## Ishlangan misol 2: parol tanlashga qarshi bloklash

Birinchi satrda chegara *L* (muvaffaqiyatsiz urinishlar), oyna *W* va bloklash muddati *B* (soniya). Keyingi satrlarda kirish jurnali: vaqt, IP va natija (`OK` yoki `FAIL`). Bloklangan manzil urinishlari rad etiladi. Oxirgi *W* soniyadagi muvaffaqiyatsiz urinishlar *L* ga yetsa, manzil *B* soniyaga bloklanadi (bu `fail2ban` dasturining gʻoyasi).

```python
import sys

satrlar = sys.stdin.read().split("\n")
chegara, oyna, muddat = map(int, satrlar[0].split())
xatolar = {}                                    # IP -> muvaffaqiyatsiz urinish vaqtlari
blok = {}                                       # IP -> bloklash tugaydigan vaqt
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    t, ip, natija = satr.split()
    t = int(t)
    if t < blok.get(ip, -1):
        print(str(t) + " " + ip + ": rad etildi (bloklangan)")
        continue
    if natija == "OK":
        print(str(t) + " " + ip + ": kirdi")
        continue
    vaqtlar = [v for v in xatolar.get(ip, []) if v > t - oyna] + [t]
    if len(vaqtlar) >= chegara:
        blok[ip] = t + muddat
        xatolar[ip] = []
        print(str(t) + " " + ip + ": bloklandi " + str(muddat) + " s")
    else:
        xatolar[ip] = vaqtlar
        print(str(t) + " " + ip + ": xato parol (" + str(len(vaqtlar)) + "/" + str(chegara) + ")")
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
        int chegara = in.nextInt();
        int oyna = in.nextInt();
        int muddat = in.nextInt();
        Map<String, List<Long>> xatolar = new HashMap<>();            // IP -> muvaffaqiyatsiz urinish vaqtlari
        Map<String, Long> blok = new HashMap<>();                     // IP -> bloklash tugaydigan vaqt
        while (in.hasNextLong()) {
            long t = in.nextLong();
            String ip = in.next();
            String natija = in.next();
            if (t < blok.getOrDefault(ip, -1L)) {
                System.out.println(t + " " + ip + ": rad etildi (bloklangan)");
                continue;
            }
            if (natija.equals("OK")) {
                System.out.println(t + " " + ip + ": kirdi");
                continue;
            }
            List<Long> vaqtlar = new ArrayList<>();
            for (long v : xatolar.getOrDefault(ip, new ArrayList<>())) {
                if (v > t - oyna) vaqtlar.add(v);
            }
            vaqtlar.add(t);
            if (vaqtlar.size() >= chegara) {
                blok.put(ip, t + muddat);
                xatolar.put(ip, new ArrayList<>());
                System.out.println(t + " " + ip + ": bloklandi " + muddat + " s");
            } else {
                xatolar.put(ip, vaqtlar);
                System.out.println(t + " " + ip + ": xato parol (" + vaqtlar.size() + "/" + chegara + ")");
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

int main() {
    int chegara, oyna, muddat;
    cin >> chegara >> oyna >> muddat;
    map<string, vector<long long>> xatolar;                          // IP -> muvaffaqiyatsiz urinish vaqtlari
    map<string, long long> blok;                                     // IP -> bloklash tugaydigan vaqt
    long long t;
    string ip, natija;
    while (cin >> t >> ip >> natija) {
        if (blok.count(ip) && t < blok[ip]) {
            cout << t << " " << ip << ": rad etildi (bloklangan)" << endl;
            continue;
        }
        if (natija == "OK") {
            cout << t << " " << ip << ": kirdi" << endl;
            continue;
        }
        vector<long long> vaqtlar;
        for (long long v : xatolar[ip]) {
            if (v > t - oyna) vaqtlar.push_back(v);
        }
        vaqtlar.push_back(t);
        if ((int) vaqtlar.size() >= chegara) {
            blok[ip] = t + muddat;
            xatolar[ip].clear();
            cout << t << " " << ip << ": bloklandi " << muddat << " s" << endl;
        } else {
            xatolar[ip] = vaqtlar;
            cout << t << " " << ip << ": xato parol (" << vaqtlar.size() << "/" << chegara << ")" << endl;
        }
    }
    return 0;
}
```

Kirish (60 soniyada 3 xato → 300 soniyaga blok):

```text
3 60 300
10 203.0.113.7 FAIL
12 203.0.113.7 FAIL
13 192.168.1.20 FAIL
14 203.0.113.7 FAIL
20 203.0.113.7 OK
50 192.168.1.20 OK
320 203.0.113.7 FAIL
```

Chiqish:

```text
10 203.0.113.7: xato parol (1/3)
12 203.0.113.7: xato parol (2/3)
13 192.168.1.20: xato parol (1/3)
14 203.0.113.7: bloklandi 300 s
20 203.0.113.7: rad etildi (bloklangan)
50 192.168.1.20: kirdi
320 203.0.113.7: xato parol (1/3)
```

20-soniyada toʻgʻri parol boʻlsa ham rad etildi — bloklash muddati tugamagan. Bu hujumchining tanlash tezligini sekundiga oʻnlab urinishdan 5 daqiqada 3 tagacha tushiradi. Parolini bir marta xato yozgan foydalanuvchi esa (192.168.1.20) bemalol kirdi.

:::warning
**Koʻp uchraydigan xatolar**

- **Brandmauer bor — demak, xavfsiz deb hisoblash.** Ruxsat etilgan port orqali kelgan hujumni (masalan, veb-ilovadagi zaiflik) oddiy brandmauer koʻrmaydi.
- **"Hammasiga ruxsat" standart siyosati.** Toʻgʻri yondashuv — hammasini taqiqlab, faqat keraklisini ochish.
- **IDS ogohlantirishlarini hech kim koʻrmasligi.** Javob jarayonisiz IDS foydasiz; jurnallar SIEM da tahlil qilinadi.
- **VPN ni toʻliq himoya deb oʻylash.** VPN tunnelni shifrlaydi, lekin zararlangan qurilma va oʻgʻirlangan parolni toʻxtatmaydi — MFA kerak.
- **RDP (3389) yoki SSH (22) ni Internetga parol bilan ochiq qoldirish.** Ular bir necha soatda parol tanlash hujumiga uchraydi; VPN orqasiga yashiring, kalit yoki MFA ishlating.
:::

:::tip
Faqat **oʻz** kompyuteringiz yoki laboratoriya VM laridan birida `nmap -sV 127.0.0.1` bilan ochiq portlarni aniqlang (boshqa tarmoqlarni ruxsatsiz skanerlash qonunbuzarlik). Windows da `netstat -ano | findstr LISTENING` bilan qaysi dastur qaysi portni tinglayotganini solishtiring va keraksiz xizmatlarni oʻchiring.
:::

## Xulosa

- Xavfsizlik — maxfiylik, yaxlitlik va mavjudlik (CIA).
- Tahdidlar: skanerlash, tinglash, spufing, MITM, DoS/DDoS, parol tanlash, fishing, zararli dasturlar, zaifliklar.
- Brandmauerlar: paket filtri, holatni kuzatuvchi, WAF, NGFW; birinchi mos qoida va standart taqiqlash; DMZ.
- VPN: IPsec, SSL/TLS, WireGuard — ochiq tarmoqdagi shifrlangan tunnel.
- IDS aniqlaydi, IPS toʻsadi; imzo va anomaliya usullari; chuqur himoya — bir nechta qatlam.

## Oʻzingizni tekshiring

:::check
Q: CIA uchligidagi "mavjudlik" talabini qaysi hujum buzadi?
A: DoS/DDoS — xizmat foydalanuvchilar uchun ishlamay qoladi.
Q: Holatni kuzatuvchi brandmauerning oddiy paket filtridan afzalligi nimada?
A: U ulanishlar jadvalini yuritadi va ichkaridan boshlangan ulanishlarning javobini avtomatik oʻtkazadi.
Q: DMZ nima uchun kerak?
A: Internetdan kiriladigan serverlarni ichki tarmoqdan ajratish uchun — ular buzilsa ham ichki tarmoq himoyalangan qoladi.
Q: IDS va IPS ning asosiy farqi nimada?
A: IDS hujumni aniqlab ogohlantiradi, IPS trafik yoʻlida turib, uni toʻsadi.
Q: Imzo boʻyicha aniqlashning kamchiligi nimada?
A: Bazada naqshi yoʻq yangi (nolinchi kun) hujumlarni koʻrmaydi.
:::

:::link https://owasp.org/www-project-top-ten/
OWASP Top 10
Veb-ilovalardagi eng keng tarqalgan xavfsizlik zaifliklari roʻyxati (ingliz tilida).
:::
