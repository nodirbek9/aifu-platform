---
title: Tarmoq muammolarini bartaraf qilish. Tarmoqning ishlashi jarayonida yuzaga keladigan muammolarni aniqlash va ularni bartaraf qilish
summary: Nosozliklarni bartaraf qilish metodologiyasi; OSI boʻyicha yondashuvlar — pastdan yuqoriga, yuqoridan pastga, boʻl va hukmronlik qil; har bir pogʻonaning tipik muammolari va belgilari; asosiy utilitalar — ping, tracert, ipconfig, nslookup, arp, netstat, pathping; ping natijalarini tahlil qilish — yoʻqotish, RTT, jitter; hujjatlashtirish.
---

## Nima uchun bu mavzu muhim?

"Internet ishlamayapti" — tarmoq administratori eng koʻp eshitadigan gap. Uning ortida kabel uzilishi, notoʻgʻri IP manzil, DNS xatosi, brandmauer qoidasi yoki provayder muammosi turishi mumkin. Tasodifiy harakatlar (hamma narsani qayta yuklash) baʼzan yordam beradi, lekin muammoni tushunmasdan uni qayta takrorlanishiga yoʻl qoʻyadi. Tizimli yondashuv bilan muammoni bir necha daqiqada topish mumkin.

:::note
**Bu darsdan soʻng siz:**

- nosozliklarni bartaraf qilishning bosqichlarini va OSI ga asoslangan yondashuvlarni tushuntira olasiz;
- har bir pogʻonaning tipik muammolarini belgilariga koʻra aniqlay olasiz;
- `ping`, `tracert`, `ipconfig`, `nslookup`, `arp`, `netstat` natijalarini talqin qila olasiz;
- ping statistikasidan yoʻqotish, kechikish va jitterni hisoblay olasiz;
- tekshiruvlar natijasidan muammo qaysi pogʻonada ekanini aniqlay olasiz.
:::

## Metodologiya

1. **Muammoni aniqlash**: nima ishlamayapti, kimda, qachondan beri, nima oʻzgargan? ("Faqat sizda yoki butun xonada?")
2. **Taxmin qilish**: eng ehtimoliy sababni tanlash.
3. **Tekshirish**: taxminni sinovdan oʻtkazish; tasdiqlanmasa — keyingi taxmin.
4. **Reja va bajarish**: tuzatish (va uning taʼsirini oldindan baholash).
5. **Natijani tekshirish**: hamma narsa ishlayaptimi, yangi muammo paydo boʻlmadimi.
6. **Hujjatlashtirish**: sabab, belgi va yechimni yozib qoʻyish — keyingi safar tezroq.

## OSI ga asoslangan yondashuvlar

| Yondashuv | Qanday | Qachon qulay |
|---|---|---|
| **Pastdan yuqoriga** | kabel → kanal → IP → transport → ilova | fizik muammo ehtimoli yuqori, yangi ulanish |
| **Yuqoridan pastga** | ilova → … → kabel | faqat bitta dastur ishlamayapti |
| **Boʻl va hukmronlik qil** | oʻrta pogʻonadan (`ping` — 3-pogʻona) boshlash: ishlasa — yuqoriga, ishlamasa — pastga | tajribali muhandis uchun eng tez |

## Pogʻonalar boʻyicha tipik muammolar

| Pogʻona | Belgilar | Sabablar | Tekshirish |
|---|---|---|---|
| 1 — Fizik | port chirogʻi oʻchiq, "Network cable unplugged" | uzilgan yoki notoʻgʻri kabel, oʻchiq port, Wi-Fi signali past | chiroqlar, kabel testeri, boshqa port |
| 2 — Kanal | ulanish bor, lekin hech kimga yetib boʻlmaydi | notoʻgʻri VLAN, dupleks nomuvofiqligi, STP bloki, MAC filtr | `show interfaces`, `arp -a` |
| 3 — Tarmoq | 169.254.x.x, "Destination host unreachable" | DHCP ishlamaydi, notoʻgʻri IP/maska/shlyuz, marshrut yoʻq | `ipconfig /all`, `ping` shlyuz, `tracert` |
| 4 — Transport | ping ishlaydi, lekin xizmatga ulanib boʻlmaydi | port yopiq, brandmauer, xizmat ishlamayapti | `Test-NetConnection server -Port 443`, `netstat -an` |
| 7 — Ilova | IP bilan ishlaydi, nom bilan — yoʻq; sayt xatosi | DNS, proksi, sertifikat, ilova sozlamasi | `nslookup`, brauzer konsoli |

## Asosiy utilitalar

| Buyruq (Windows) | Linux | Nima beradi |
|---|---|---|
| `ipconfig /all` | `ip addr`, `ip route` | manzil, maska, shlyuz, DNS, DHCP |
| `ipconfig /release`, `/renew`, `/flushdns` | `dhclient -r`, `resolvectl flush-caches` | manzilni yangilash, DNS keshini tozalash |
| `ping` | `ping` | bogʻlanish va RTT (ICMP Echo) |
| `tracert` | `traceroute` | yoʻldagi routerlar |
| `pathping` | `mtr` | har bir sakrashdagi yoʻqotish va kechikish |
| `nslookup` | `dig`, `nslookup` | DNS soʻrovlari |
| `arp -a` | `ip neigh` | ARP keshi |
| `netstat -ano` | `ss -tunap` | ochiq portlar va ulanishlar |
| `Test-NetConnection` | `nc -zv` | TCP port ochiqligi |

**Klassik ketma-ketlik** ("Internet ishlamayapti"):

```text
ipconfig /all                 manzil bormi? 169.254 boʻlsa — DHCP muammosi
ping 127.0.0.1                TCP/IP steki ishlayaptimi
ping 192.168.1.1              shlyuzga yetib boriladimi (lokal tarmoq)
ping 8.8.8.8                  Internetga IP orqali chiqiladimi
ping google.com               DNS ishlayaptimi (8.8.8.8 ishlab, bu ishlamasa — DNS)
tracert 8.8.8.8               qayerda uziladi
```

## Ping natijalarini tushunish

- **Yoʻqotish** (packet loss): 0% — normal; 1–2% — sezilarli (ovozli aloqa buziladi); koʻproq — jiddiy muammo (shovqinli kabel, toʻlgan kanal, zaif Wi-Fi).
- **RTT** (round-trip time): LAN da < 1 ms, shahar ichida 5–20 ms, boshqa qitʼaga 100–250 ms.
- **Jitter** — ketma-ket RTT lar oʻzgaruvchanligi; ovoz va videoda muhim (30 ms dan oshmasligi kerak).
- `Request timed out` — javob kelmadi (paket yoʻqoldi yoki ICMP toʻsilgan); `Destination host unreachable` — router yoʻl topa olmadi; `TTL expired in transit` — marshrutlash halqasi.

## Ishlangan misol 1: ping statistikasi

Har bir satrda bitta ping natijasi: RTT (ms, butun son) yoki `*` (javob yoʻq). Yuborilgan, qabul qilingan paketlar sonini va yoʻqotish foizini, minimal, oʻrtacha va maksimal RTT ni hamda jitterni (ketma-ket qabul qilingan javoblar RTT farqlari moduli oʻrtachasi) chiqaring.

```python
import sys

natijalar = sys.stdin.read().split()
rtt = [int(x) for x in natijalar if x != "*"]
yuborildi = len(natijalar)
qabul = len(rtt)
yoqotish = (yuborildi - qabul) * 100 / yuborildi
print(f"paketlar: {yuborildi} yuborildi, {qabul} qabul qilindi, {yoqotish:.1f}% yoʻqoldi")
if qabul == 0:
    print("RTT: javob yoʻq")
else:
    print(f"RTT: min {min(rtt)}, oʻrtacha {sum(rtt) / qabul:.1f}, max {max(rtt)} ms")
    farqlar = [abs(rtt[i] - rtt[i - 1]) for i in range(1, qabul)]
    jitter = sum(farqlar) / len(farqlar) if farqlar else 0.0   # ketma-ket farqlar oʻrtachasi
    print(f"jitter: {jitter:.1f} ms")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> rtt = new ArrayList<>();
        int yuborildi = 0;
        while (in.hasNext()) {
            String x = in.next();
            yuborildi++;
            if (!x.equals("*")) rtt.add(Integer.parseInt(x));
        }
        int qabul = rtt.size();
        double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
        System.out.println(String.format("paketlar: %d yuborildi, %d qabul qilindi, %.1f%% yoʻqoldi", yuborildi, qabul, yoqotish));
        if (qabul == 0) {
            System.out.println("RTT: javob yoʻq");
        } else {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            long summa = 0;
            for (int r : rtt) {
                min = Math.min(min, r);
                max = Math.max(max, r);
                summa += r;
            }
            System.out.println(String.format("RTT: min %d, oʻrtacha %.1f, max %d ms", min, (double) summa / qabul, max));
            double farqlar = 0;
            for (int i = 1; i < qabul; i++) farqlar += Math.abs(rtt.get(i) - rtt.get(i - 1));
            double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;     // ketma-ket farqlar oʻrtachasi
            System.out.println(String.format("jitter: %.1f ms", jitter));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<int> rtt;
    int yuborildi = 0;
    string x;
    while (cin >> x) {
        yuborildi++;
        if (x != "*") rtt.push_back(stoi(x));
    }
    int qabul = rtt.size();
    double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
    printf("paketlar: %d yuborildi, %d qabul qilindi, %.1f%% yoʻqoldi\n", yuborildi, qabul, yoqotish);
    if (qabul == 0) {
        printf("RTT: javob yoʻq\n");
    } else {
        long long summa = 0;
        for (int r : rtt) summa += r;
        printf("RTT: min %d, oʻrtacha %.1f, max %d ms\n", *min_element(rtt.begin(), rtt.end()),
               (double) summa / qabul, *max_element(rtt.begin(), rtt.end()));
        double farqlar = 0;
        for (int i = 1; i < qabul; i++) farqlar += abs(rtt[i] - rtt[i - 1]);
        double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;          // ketma-ket farqlar oʻrtachasi
        printf("jitter: %.1f ms\n", jitter);
    }
    return 0;
}
```

Kirish:

```text
12
15
*
11
30
```

Chiqish:

```text
paketlar: 5 yuborildi, 4 qabul qilindi, 20.0% yoʻqoldi
RTT: min 11, oʻrtacha 17.0, max 30 ms
jitter: 8.7 ms
```

20% yoʻqotish va 8,7 ms jitter — kabel uchun yomon, zaif Wi-Fi uchun xos manzara. Bitta 30 ms lik "sakrash" oʻrtachani ham, jitterni ham sezilarli oshirdi. Nosozlikni aniqlashda oʻrtacha qiymatgagina emas, eng yomon qiymat va tarqalishga ham qarash kerak.

## Ishlangan misol 2: pogʻonama-pogʻona tashxis

Har bir satrda bitta kompyuter uchun beshta tekshiruv natijasi (`ok` yoki `fail`) quyidagi tartibda: ulanish (kabel yoki Wi-Fi), IP manzil (169.254 emas), shlyuzga ping, nom boʻyicha soʻrov (DNS), Internetdagi IP ga ping. Birinchi muvaffaqiyatsiz tekshiruv boʻyicha muammo pogʻonasi va tavsiyani chiqaring; hammasi `ok` boʻlsa — tarmoq soz.

```python
import sys

TASHXIS = [
    "1-pogʻona: kabel yoki Wi-Fi ulanishini tekshiring",
    "3-pogʻona: DHCP dan manzil olinmadi (ipconfig /renew)",
    "3-pogʻona: shlyuzga yetib boʻlmaydi (maska, shlyuz, VLAN)",
    "7-pogʻona: DNS ishlamayapti (nslookup, DNS server manzili)",
    "3-pogʻona: provayder yoki marshrut muammosi (tracert)",
]

for raqam, satr in enumerate(sys.stdin.read().strip().split("\n"), 1):
    natija = satr.split()
    xato = next((i for i, n in enumerate(natija) if n == "fail"), -1)   # birinchi xato
    if xato < 0:
        print(str(raqam) + ": tarmoq soz")
    else:
        print(str(raqam) + ": " + TASHXIS[xato])
```
```java
import java.util.Scanner;

public class Main {
    static final String[] TASHXIS = {
        "1-pogʻona: kabel yoki Wi-Fi ulanishini tekshiring",
        "3-pogʻona: DHCP dan manzil olinmadi (ipconfig /renew)",
        "3-pogʻona: shlyuzga yetib boʻlmaydi (maska, shlyuz, VLAN)",
        "7-pogʻona: DNS ishlamayapti (nslookup, DNS server manzili)",
        "3-pogʻona: provayder yoki marshrut muammosi (tracert)",
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int raqam = 0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            raqam++;
            String[] natija = satr.split(" ");
            int xato = -1;
            for (int i = 0; i < natija.length && xato < 0; i++) {
                if (natija[i].equals("fail")) xato = i;                     // birinchi xato
            }
            if (xato < 0) {
                System.out.println(raqam + ": tarmoq soz");
            } else {
                System.out.println(raqam + ": " + TASHXIS[xato]);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const string TASHXIS[] = {
    "1-pogʻona: kabel yoki Wi-Fi ulanishini tekshiring",
    "3-pogʻona: DHCP dan manzil olinmadi (ipconfig /renew)",
    "3-pogʻona: shlyuzga yetib boʻlmaydi (maska, shlyuz, VLAN)",
    "7-pogʻona: DNS ishlamayapti (nslookup, DNS server manzili)",
    "3-pogʻona: provayder yoki marshrut muammosi (tracert)",
};

int main() {
    string satr;
    int raqam = 0;
    while (getline(cin, satr)) {
        if (satr.find_first_not_of(" \r") == string::npos) continue;
        raqam++;
        istringstream ss(satr);
        string n;
        int xato = -1;
        for (int i = 0; ss >> n; i++) {
            if (n == "fail" && xato < 0) xato = i;                          // birinchi xato
        }
        if (xato < 0) {
            cout << raqam << ": tarmoq soz" << endl;
        } else {
            cout << raqam << ": " << TASHXIS[xato] << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
ok ok ok fail ok
ok fail fail fail fail
ok ok ok ok ok
fail fail fail fail fail
ok ok ok ok fail
```

Chiqish:

```text
1: 7-pogʻona: DNS ishlamayapti (nslookup, DNS server manzili)
2: 3-pogʻona: DHCP dan manzil olinmadi (ipconfig /renew)
3: tarmoq soz
4: 1-pogʻona: kabel yoki Wi-Fi ulanishini tekshiring
5: 3-pogʻona: provayder yoki marshrut muammosi (tracert)
```

Toʻrtinchi kompyuterda beshala tekshiruv muvaffaqiyatsiz, lekin sabab — bitta: kabel. Shuning uchun birinchi xatoga qaraladi: yuqori pogʻonalar pastkilariga bogʻliq, pastki muammo hamma narsani "sindiradi". Birinchi kompyuter — klassik holat: IP orqali Internet bor, lekin saytlar ochilmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Darhol murakkab sabab qidirish.** Avval oddiysi: kabel, chiroqlar, "oxirgi marta nima oʻzgartirildi".
- **`ping` javob bermasa, xost oʻchiq deb xulosa qilish.** Brandmauer ICMP ni toʻsishi mumkin; portni `Test-NetConnection` bilan tekshiring.
- **Bir vaqtda bir nechta narsani oʻzgartirish.** Qaysi oʻzgarish yordam berganini (yoki buzganini) bilib boʻlmaydi.
- **Hamma narsani qayta yuklash va muammoni yozib qoʻymaslik.** Muammo qaytadi, sababi esa nomaʼlum qoladi.
- **Faqat oʻrtacha RTT ga qarash.** Yoʻqotish va jitter foydalanuvchi tajribasini koʻproq buzadi.
:::

:::tip
Oʻz tarmogʻingizda "klassik ketma-ketlik" ni bajaring va natijalarni jadvalga yozing. Keyin `pathping 8.8.8.8` (yoki `mtr 8.8.8.8`) ni ishga tushirib, qaysi sakrashda kechikish va yoʻqotish paydo boʻlishini aniqlang.
:::

## Xulosa

- Bosqichlar: aniqlash → taxmin → tekshirish → tuzatish → natijani tekshirish → hujjatlashtirish.
- OSI yondashuvlari: pastdan yuqoriga, yuqoridan pastga, boʻl va hukmronlik qil.
- Har bir pogʻonaning oʻz belgilari va vositalari bor: chiroqlar, `arp`, `ipconfig`, `ping`, `tracert`, `Test-NetConnection`, `nslookup`.
- Ping natijasini yoʻqotish, RTT (min/oʻrtacha/max) va jitter boʻyicha baholash kerak.
- Birinchi muvaffaqiyatsiz pastki pogʻona — asosiy sabab: yuqoridagilar unga bogʻliq.

## Oʻzingizni tekshiring

:::check
Q: IP orqali `ping 8.8.8.8` ishlaydi, `ping google.com` esa yoʻq. Qaysi xizmat muammoli?
A: DNS.
Q: Kompyuter 169.254.x.x manzil olgan. Ehtimoliy sabab?
A: DHCP serverga ulanib boʻlmadi (APIPA manzili).
Q: Paket qaysi routerlar orqali borishini koʻrsatuvchi Windows utilitasi?
A: `tracert`.
Q: "Boʻl va hukmronlik qil" yondashuvi nimadan boshlanadi?
A: Oʻrta pogʻonadan, odatda `ping` (3-pogʻona) bilan; natijaga qarab yuqoriga yoki pastga harakatlaniladi.
Q: RTT lar 10, 12, 30, 11 ms. Jitter qancha?
A: (2 + 18 + 19) / 3 = 13 ms.
:::

:::link https://learn.microsoft.com/en-us/troubleshoot/windows-client/networking/tcpip-and-networking-troubleshooting-overview
TCP/IP troubleshooting
Microsoft ning tarmoq nosozliklarini aniqlash boʻyicha qoʻllanmasi (ingliz tilida).
:::
