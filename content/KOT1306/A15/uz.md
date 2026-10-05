---
title: Tarmoq nosozliklarini aniqlash, operatsion tizim utilitalaridan foydalanish
summary: Windows va Linux tarmoq utilitalari bilan amaliy ish — ipconfig, ping, tracert, pathping, nslookup, arp, netstat, route, getmac, Test-NetConnection; natijalarni talqin qilish; nosozlik stsenariylari (kabel, DHCP, shlyuz, DNS, port, marshrut); netstat va tracert chiqishini dastur bilan tahlil qilish; hisobot yozish.
---

## Nima uchun bu mavzu muhim?

M15 maʼruzasida nosozliklarni bartaraf qilish metodologiyasini oʻrgandik. Bu mashgʻulotda qoʻlimizni ishga solamiz: laboratoriya tarmogʻida ataylab yaratilgan nosozliklarni utilitalar yordamida topamiz. Haqiqiy ishda bu buyruqlarning chiqishi yuzlab qator boʻlishi mumkin — shuning uchun ularni dastur bilan tahlil qilishni ham oʻrganamiz: administratorlar shunday skriptlar yozib, kundalik tekshiruvlarni avtomatlashtiradi.

:::note
**Bu mashgʻulotdan soʻng siz:**

- asosiy tarmoq utilitalarini ishlatib, ularning natijalarini talqin qila olasiz;
- tipik nosozlik stsenariylarini belgilariga koʻra aniqlay olasiz;
- `netstat` chiqishidan tinglanayotgan portlar va ulanishlar statistikasini olishni bilasiz;
- `tracert` natijasidan kechikish qayerda paydo boʻlishini aniqlay olasiz;
- nosozlik boʻyicha qisqa hisobot yoza olasiz.
:::

## Utilitalar tezkor maʼlumotnomasi

| Vazifa | Windows | Linux |
|---|---|---|
| Manzil va sozlamalar | `ipconfig /all` | `ip addr`, `ip route`, `resolvectl status` |
| Manzilni yangilash | `ipconfig /release` + `/renew` | `sudo dhclient -r && sudo dhclient` |
| Bogʻlanish | `ping -n 10 host` | `ping -c 10 host` |
| Yoʻl | `tracert host` | `traceroute host` |
| Yoʻl + yoʻqotish | `pathping host` | `mtr host` |
| DNS | `nslookup host`, `ipconfig /flushdns` | `dig host`, `nslookup host` |
| ARP keshi | `arp -a`, `arp -d *` | `ip neigh` |
| Ulanishlar va portlar | `netstat -ano`, `Get-NetTCPConnection` | `ss -tunap` |
| Marshrut jadvali | `route print` | `ip route` |
| MAC manzillar | `getmac /v` | `ip link` |
| TCP port tekshiruvi | `Test-NetConnection host -Port 443` | `nc -zv host 443` |
| Kompyuter nomi | `hostname` | `hostname` |

## Nosozlik stsenariylari

Oʻqituvchi laboratoriyada quyidagi nosozliklarni yaratadi; har biri uchun **belgi → tekshiruv → sabab → yechim** jadvalini toʻldiring.

| № | Belgi | Tekshiruv | Odatiy sabab |
|---|---|---|---|
| 1 | "Network cable unplugged" | `ipconfig`, port chirogʻi | kabel uzilgan yoki port oʻchiq |
| 2 | manzil 169.254.x.x | `ipconfig /all`, `/renew` | DHCP server ishlamaydi yoki yetib boʻlmaydi |
| 3 | lokal tarmoq bor, Internet yoʻq | `ping` shlyuz, `route print` | shlyuz notoʻgʻri yoki yoʻq |
| 4 | `ping 8.8.8.8` ishlaydi, saytlar yoʻq | `nslookup` | DNS server manzili notoʻgʻri |
| 5 | sayt ochilmaydi, ping ishlaydi | `Test-NetConnection -Port 443`, `netstat -ano` | port yopiq, xizmat ishlamaydi, brandmauer |
| 6 | ikki kompyuter bir-birini koʻrmaydi | `ipconfig` maskalarini solishtirish | turli quyi tarmoq yoki notoʻgʻri maska |
| 7 | "Windows has detected an IP address conflict" | `arp -a`, `ping` | ikki qurilmada bir xil statik IP |
| 8 | Internet juda sekin, uziladi | `pathping`, `ping -n 100` | yoʻqotish, zaif Wi-Fi, toʻlgan kanal |

## Hisobot shakli

```text
Sana va vaqt:       2026-10-05 10:40
Belgi:              PC07 da saytlar ochilmaydi, Telegram ishlaydi
Tekshiruvlar:       ipconfig /all — 192.168.50.107/24, shlyuz .1, DNS 8.8.4.4
                    ping 8.8.8.8 — 0% yoʻqotish; nslookup aifu.uz — "timed out"
Sabab:              DNS server manzili qoʻlda notoʻgʻri kiritilgan (8.8.4.44)
Yechim:             IPv4 sozlamalarida DNS ni avtomatik (DHCP) qilish
Natija:             nslookup va brauzer ishlaydi
```

## Ishlangan misol 1: netstat chiqishini tahlil qilish

Kirishda `netstat -an` chiqishining satrlari (protokol, mahalliy manzil, masofaviy manzil va TCP uchun holat). Tinglanayotgan TCP portlarini (takrorsiz, oʻsish tartibida), har bir holatdagi TCP ulanishlar sonini (holat nomi boʻyicha alifbo tartibida, `LISTENING` dan tashqari) va eng koʻp ulanish oʻrnatilgan masofaviy IP ni chiqaring (teng boʻlsa — kichik satr).

```python
import sys

tinglash = set()
holatlar = {}
masofaviy = {}
for satr in sys.stdin:
    q = satr.split()
    if len(q) < 4 or q[0] != "TCP":
        continue                                  # sarlavha va UDP qatorlari
    port = int(q[1].rsplit(":", 1)[1])
    holat = q[3]
    if holat == "LISTENING":
        tinglash.add(port)
        continue
    holatlar[holat] = holatlar.get(holat, 0) + 1
    ip = q[2].rsplit(":", 1)[0]
    masofaviy[ip] = masofaviy.get(ip, 0) + 1
print("LISTENING: " + " ".join(str(p) for p in sorted(tinglash)))
for holat in sorted(holatlar):
    print(holat + ": " + str(holatlar[holat]))
if masofaviy:
    eng = min(masofaviy, key=lambda ip: (-masofaviy[ip], ip))
    print("eng faol server: " + eng + " (" + str(masofaviy[eng]) + ")")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        TreeSet<Integer> tinglash = new TreeSet<>();
        Map<String, Integer> holatlar = new TreeMap<>();
        Map<String, Integer> masofaviy = new TreeMap<>();
        while (in.hasNextLine()) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q.length < 4 || !q[0].equals("TCP")) continue;          // sarlavha va UDP qatorlari
            int port = Integer.parseInt(q[1].substring(q[1].lastIndexOf(':') + 1));
            String holat = q[3];
            if (holat.equals("LISTENING")) {
                tinglash.add(port);
                continue;
            }
            holatlar.merge(holat, 1, Integer::sum);
            masofaviy.merge(q[2].substring(0, q[2].lastIndexOf(':')), 1, Integer::sum);
        }
        StringJoiner portlar = new StringJoiner(" ");
        for (int p : tinglash) portlar.add(String.valueOf(p));
        System.out.println("LISTENING: " + portlar);
        for (Map.Entry<String, Integer> e : holatlar.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        String eng = null;
        for (Map.Entry<String, Integer> e : masofaviy.entrySet()) {
            if (eng == null || e.getValue() > masofaviy.get(eng)) eng = e.getKey();
        }
        if (eng != null) System.out.println("eng faol server: " + eng + " (" + masofaviy.get(eng) + ")");
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    set<int> tinglash;
    map<string, int> holatlar, masofaviy;
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        vector<string> q;
        string s;
        while (ss >> s) q.push_back(s);
        if (q.size() < 4 || q[0] != "TCP") continue;                    // sarlavha va UDP qatorlari
        int port = stoi(q[1].substr(q[1].rfind(':') + 1));
        string holat = q[3];
        if (holat == "LISTENING") {
            tinglash.insert(port);
            continue;
        }
        holatlar[holat]++;
        masofaviy[q[2].substr(0, q[2].rfind(':'))]++;
    }
    string portlar;
    for (int p : tinglash) portlar += (portlar.empty() ? "" : " ") + to_string(p);
    cout << "LISTENING: " << portlar << endl;
    for (auto& e : holatlar) cout << e.first << ": " << e.second << endl;
    string eng;
    for (auto& e : masofaviy) {
        if (eng.empty() || e.second > masofaviy[eng]) eng = e.first;
    }
    if (!eng.empty()) cout << "eng faol server: " << eng << " (" << masofaviy[eng] << ")" << endl;
    return 0;
}
```

Kirish:

```text
  Proto  Local Address          Foreign Address        State
  TCP    0.0.0.0:135            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:445            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:3389           0.0.0.0:0              LISTENING
  TCP    192.168.1.10:51000     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51002     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51005     95.46.98.10:443        ESTABLISHED
  TCP    192.168.1.10:50990     95.46.98.10:443        TIME_WAIT
  TCP    192.168.1.10:51010     185.74.5.10:80         SYN_SENT
  UDP    0.0.0.0:5353           *:*
```

Chiqish:

```text
LISTENING: 135 445 3389
ESTABLISHED: 3
SYN_SENT: 1
TIME_WAIT: 1
eng faol server: 142.250.74.46 (2)
```

Talqin: 3389 (RDP) tinglanmoqda — kerak boʻlmasa, xizmatni oʻchirish yoki brandmauerda yopish kerak. `SYN_SENT` — ulanish soʻrovi yuborilgan, lekin javob yoʻq: 185.74.5.10 ning 80-porti yopiq yoki brandmauer toʻsyapti. Bunday holatlar koʻp boʻlsa — tarmoq yoki server muammosi; koʻp turli manzillarga `SYN_SENT` — zararli dastur skanerlashining belgisi.

## Ishlangan misol 2: tracert natijasini tahlil qilish

Har bir satrda bitta sakrash: tartib raqami, uchta RTT (ms yoki `*`) va router manzili (`-` — nomaʼlum). Har bir sakrash uchun oʻrtacha RTT ni (faqat javob kelganlari boʻyicha) yoki `javob yoʻq` ni chiqaring, soʻngra oldingi javob bergan sakrashga nisbatan kechikish eng koʻp oshgan sakrashni koʻrsating.

```python
import sys

oldingi = None
eng_sakrash, eng_oshish = None, -1.0
for satr in sys.stdin.read().strip().split("\n"):
    q = satr.split()
    vaqtlar = [int(x) for x in q[1:4] if x != "*"]
    if not vaqtlar:
        print(q[0] + " " + q[4] + ": javob yoʻq")      # router ICMP ga javob bermaydi
        continue
    ortacha = sum(vaqtlar) / len(vaqtlar)
    print(f"{q[0]} {q[4]}: {ortacha:.1f} ms")
    if oldingi is not None and ortacha - oldingi > eng_oshish:
        eng_sakrash, eng_oshish = q[0], ortacha - oldingi
    oldingi = ortacha
if eng_sakrash is not None:
    print(f"eng katta oshish: {eng_sakrash}-qadam (+{eng_oshish:.1f} ms)")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Double oldingi = null;
        String engSakrash = null;
        double engOshish = -1.0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            double summa = 0;
            int soni = 0;
            for (int i = 1; i <= 3; i++) {
                if (!q[i].equals("*")) {
                    summa += Integer.parseInt(q[i]);
                    soni++;
                }
            }
            if (soni == 0) {
                System.out.println(q[0] + " " + q[4] + ": javob yoʻq");   // router ICMP ga javob bermaydi
                continue;
            }
            double ortacha = summa / soni;
            System.out.println(String.format("%s %s: %.1f ms", q[0], q[4], ortacha));
            if (oldingi != null && ortacha - oldingi > engOshish) {
                engSakrash = q[0];
                engOshish = ortacha - oldingi;
            }
            oldingi = ortacha;
        }
        if (engSakrash != null) {
            System.out.println(String.format("eng katta oshish: %s-qadam (+%.1f ms)", engSakrash, engOshish));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string satr;
    bool bor = false;
    double oldingi = 0, engOshish = -1.0;
    string engSakrash;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string raqam, t[3], manzil;
        if (!(ss >> raqam >> t[0] >> t[1] >> t[2] >> manzil)) continue;
        double summa = 0;
        int soni = 0;
        for (auto& x : t) {
            if (x != "*") {
                summa += stoi(x);
                soni++;
            }
        }
        if (soni == 0) {
            printf("%s %s: javob yoʻq\n", raqam.c_str(), manzil.c_str());   // router ICMP ga javob bermaydi
            continue;
        }
        double ortacha = summa / soni;
        printf("%s %s: %.1f ms\n", raqam.c_str(), manzil.c_str(), ortacha);
        if (bor && ortacha - oldingi > engOshish) {
            engSakrash = raqam;
            engOshish = ortacha - oldingi;
        }
        oldingi = ortacha;
        bor = true;
    }
    if (!engSakrash.empty()) printf("eng katta oshish: %s-qadam (+%.1f ms)\n", engSakrash.c_str(), engOshish);
    return 0;
}
```

Kirish:

```text
1 1 1 2 192.168.1.1
2 8 9 8 10.10.0.1
3 * * * -
4 12 11 13 195.158.1.1
5 60 * 62 72.14.215.1
6 61 60 59 8.8.8.8
```

Chiqish:

```text
1 192.168.1.1: 1.3 ms
2 10.10.0.1: 8.3 ms
3 -: javob yoʻq
4 195.158.1.1: 12.0 ms
5 72.14.215.1: 61.0 ms
6 8.8.8.8: 60.0 ms
eng katta oshish: 5-qadam (+49.0 ms)
```

Talqin: 3-qadam javob bermadi, lekin keyingilari ishlaydi — bu nosozlik emas, router ICMP ga javob bermaslikka sozlangan. 4 dan 5 ga oʻtishda kechikish ~49 ms oshdi — bu provayderning xalqaro kanali (masofa — boshqa davlatga). Agar katta oshish keyingi barcha qadamlarda saqlansa — sabab oʻsha joyda; faqat bitta qadamda boʻlsa — routerning oʻzi ICMP ga sekin javob beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **`tracert` dagi `*` ni uzilish deb hisoblash.** Keyingi qadamlar javob bersa, yoʻl ishlayapti — router faqat ICMP ga javob bermaydi.
- **`netstat -a` ni administrator huquqisiz `-b` bilan ishga tushirish.** Dastur nomlarini koʻrish uchun administrator kerak; PID ni `-o` bilan olib, Task Manager da solishtiring.
- **`arp -d *` yoki `ipconfig /release` ni masofaviy (RDP) seansda bajarish.** Ulanish uziladi va qayta kira olmaysiz.
- **Natijalarni yozmasdan tekshirish.** Hisobotsiz takroriy muammolar har safar noldan boshlab tekshiriladi.
:::

:::tip
Kompyuteringizda `netstat -ano > netstat.txt` ni bajarib, faylni birinchi misol dasturiga bering. Natijadagi tinglanayotgan har bir port uchun `tasklist /fi "PID eq <PID>"` bilan qaysi dastur ekanini aniqlang va keraksizlarini roʻyxatga yozing.
:::

## Xulosa

- Asosiy utilitalar: `ipconfig`, `ping`, `tracert`/`pathping`, `nslookup`, `arp`, `netstat`, `route`, `Test-NetConnection` (Linux: `ip`, `ss`, `dig`, `mtr`, `nc`).
- Har bir stsenariy uchun: belgi → tekshiruv → sabab → yechim → natija; barchasi hisobotga yoziladi.
- `netstat`: LISTENING — ochiq xizmatlar, ESTABLISHED — faol ulanishlar, SYN_SENT — javobsiz urinishlar, TIME_WAIT — yopilgan ulanishlar.
- `tracert`: `*` har doim ham uzilish emas; kechikishning keskin va barqaror oshishi muammoli joyni koʻrsatadi.
- Utilitalar chiqishini skript bilan tahlil qilish kundalik tekshiruvlarni avtomatlashtiradi.

## Oʻzingizni tekshiring

:::check
Q: Paket manzilga qaysi routerlar orqali borishini koʻrsatuvchi Windows utilitasi?
A: `tracert`.
Q: Kompyuterning IP manzili, maskasi va shlyuzini koʻrsatuvchi Windows buyrugʻi?
A: `ipconfig` (toʻliq — `ipconfig /all`).
Q: `netstat` da koʻp `SYN_SENT` holatlari nimani bildirishi mumkin?
A: Ulanish soʻrovlariga javob kelmayapti (port yopiq, brandmauer) yoki zararli dastur skanerlash qilmoqda.
Q: `tracert` ning bitta oraliq qadamida uchala natija `*`, keyingilari esa normal. Bu nimani bildiradi?
A: Oʻsha router ICMP ga javob bermaslikka sozlangan; yoʻl ishlayapti.
Q: Qaysi buyruq TCP port ochiqligini tekshiradi?
A: `Test-NetConnection host -Port N` (Linux da `nc -zv host N`).
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/windows-commands
Windows commands
Windows buyruqlar satri utilitalari boʻyicha toʻliq maʼlumotnoma (ingliz tilida).
:::
