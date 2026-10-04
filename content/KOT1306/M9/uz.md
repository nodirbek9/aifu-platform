---
title: Tarmoq protokollari va marshrutlash protokollari. Tarmoq protokollari va xizmatlari (ARP, ICMP, DHCP). Marshrutlash protokollari va ularning ishlash tamoyillari
summary: ARP — IP manzil boʻyicha MAC manzilni topish, ARP keshi, ARP spufing xavfi; ICMP xabarlari va ping, tracert; DHCP — DORA jarayoni, ijara, relay agent; marshrutlash jadvali, statik va dinamik marshrutlash, metrika va administrativ masofa; masofa-vektor (RIP), kanal holati (OSPF) va yoʻl-vektor (BGP) protokollari; ichki va tashqi marshrutlash.
---

## Nima uchun bu mavzu muhim?

Kompyuterni tarmoqqa ulasangiz, bir soniya ichida koʻp ish bajariladi: u **DHCP** dan manzil oladi, **ARP** bilan shlyuzning MAC manzilini topadi, paketlar esa yoʻlda **marshrutlash protokollari** qurgan jadvallar boʻyicha yuradi. Muammo boʻlsa, **ICMP** xabar beradi: "manzilga yetib boʻlmaydi" yoki "vaqt tugadi". Bu yordamchi protokollarni bilmasdan tarmoq nosozligini aniqlash mumkin emas.

:::note
**Bu darsdan soʻng siz:**

- ARP soʻrovi va javobining borishini, ARP keshi vazifasini tushuntira olasiz;
- asosiy ICMP xabarlarini va `ping`, `tracert` buyruqlari ular bilan qanday ishlashini ayta olasiz;
- DHCP ning DORA jarayonini bosqichma-bosqich tushuntira olasiz;
- statik va dinamik marshrutlashni, masofa-vektor va kanal holati protokollarini farqlay olasiz;
- masofa-vektor algoritmi qanday qilib marshrutlash jadvallarini quradi — hisoblay olasiz.
:::

## ARP — manzillarni moslash

Kompyuter 192.168.1.1 ga paket yubormoqchi, lekin Ethernet kadriga **MAC manzil** kerak. **ARP** (Address Resolution Protocol) shu muammoni hal qiladi:

1. Kompyuter **ARP keshi**ni tekshiradi (`arp -a`). Yozuv boʻlsa — darhol ishlatadi.
2. Yoʻq boʻlsa — **broadkast** ARP soʻrovi yuboradi: "Kimda 192.168.1.1 bor? 192.168.1.10 ga ayting".
3. 192.168.1.1 egasi **unicast** javob qaytaradi: "192.168.1.1 — men, MAC: 00:1A:2B:3C:4D:5E".
4. Javob keshga yoziladi va bir necha daqiqa saqlanadi.

Muhim: agar qabul qiluvchi **boshqa tarmoqda** boʻlsa, kompyuter uning MAC ini emas, **standart shlyuz** (router) MAC ini soʻraydi. **Gratuitous ARP** — qurilma oʻz manzilini eʼlon qiladi (IP ziddiyatini aniqlash uchun). **ARP spufing** — hujumchi soxta javoblar bilan oʻzini shlyuz qilib koʻrsatadi va trafikni oʻzi orqali oʻtkazadi (man-in-the-middle); himoya — kommutatorda Dynamic ARP Inspection.

## ICMP — xizmat xabarlari

| Tur | Xabar | Qachon |
|---|---|---|
| 0 / 8 | Echo Reply / Echo Request | `ping` |
| 3 | Destination Unreachable | tarmoq, xost yoki port mavjud emas; fragmentatsiya kerak, lekin DF qoʻyilgan |
| 5 | Redirect | router xostga yaxshiroq shlyuzni koʻrsatadi |
| 11 | Time Exceeded | TTL 0 ga tushdi — `tracert` shundan foydalanadi |

`ping` bogʻlanish bor-yoʻqligini va aylanib qaytish vaqtini (RTT) koʻrsatadi; `tracert` (Linux da `traceroute`) TTL = 1, 2, 3, … bilan paket yuborib, yoʻldagi har bir routerni aniqlaydi. Baʼzi tarmoqlarda ICMP brandmauer bilan yopilgan — `ping` javob bermasligi xost ishlamayapti degani emas.

## DHCP — manzilni avtomatik berish

**DHCP** (Dynamic Host Configuration Protocol, UDP portlari: server 67, mijoz 68) jarayoni — **DORA**:

| Bosqich | Kimdan → kimga | Mazmuni |
|---|---|---|
| **D**iscover | mijoz → broadkast | "Tarmoqda DHCP server bormi?" (mijozda hali IP yoʻq: manba 0.0.0.0) |
| **O**ffer | server → mijoz | "192.168.1.57 ni taklif qilaman, maska, shlyuz, DNS, ijara 24 soat" |
| **R**equest | mijoz → broadkast | "192.168.1.57 ni olaman" (boshqa serverlar ham bilib qoʻyadi) |
| **A**ck | server → mijoz | "Tasdiqlayman" |

Manzil **ijaraga** beriladi; ijara muddatining yarmida mijoz uni uzaytiradi. Server boshqa quyi tarmoqda boʻlsa, routerdagi **DHCP relay agent** (`ip helper-address`) broadkastni serverga yetkazadi. `ipconfig /release` va `ipconfig /renew` bilan jarayonni qoʻlda takrorlash mumkin.

## Marshrutlash

Router har bir paket uchun **marshrutlash jadvalidan** yozuv tanlaydi: tarmoq/prefiks → keyingi qadam (next hop) yoki chiqish interfeysi. Bir nechta yozuv mos kelsa, **eng uzun prefiks** tanlanadi (bu tanlov dasturi — A8 amaliy mashgʻulotining topshirigʻi).

Jadval qanday toʻladi:

- **Bevosita ulangan** tarmoqlar — interfeysga IP berilgani zahoti.
- **Statik** marshrutlar — administrator qoʻlda yozadi (`ip route 10.0.0.0 255.0.0.0 192.168.1.2`); kichik tarmoqlar va standart marshrut `0.0.0.0/0` uchun.
- **Dinamik** — routerlar marshrutlash protokollari orqali bir-biriga xabar berib, jadvalni oʻzlari quradi va nosozlikda yangi yoʻl topadi.

Bir xil tarmoqqa turli manbalardan yoʻl boʻlsa, **administrativ masofa** (ishonch darajasi; Cisco: bevosita 0, statik 1, OSPF 110, RIP 120) boʻyicha, bitta protokol ichida esa **metrika** (sakrashlar soni, kanal narxi) boʻyicha tanlanadi.

## Marshrutlash protokollari

| Tur | Gʻoya | Protokol | Metrika | Xususiyati |
|---|---|---|---|---|
| **Masofa-vektor** | har bir router qoʻshnilariga oʻz jadvalini ("qaysi tarmoq — qancha masofa") yuboradi | **RIP** | sakrashlar soni (16 = yetib boʻlmaydi) | oddiy; sekin yaqinlashadi, "cheksizlikkacha sanash" muammosi |
| **Kanal holati** | har bir router butun tarmoq xaritasini oladi va Deykstra algoritmi bilan eng qisqa yoʻllarni hisoblaydi | **OSPF**, IS-IS | kanal narxi (tezlikka bogʻliq) | tez yaqinlashadi, katta tarmoqlar uchun; zonalar (area) |
| **Yoʻl-vektor** | marshrut bilan birga u oʻtadigan avtonom tizimlar roʻyxati yuboriladi | **BGP** | siyosatlar, AS yoʻli uzunligi | Internetning provayderlararo protokoli |

**Avtonom tizim** (AS) — bitta tashkilot boshqaradigan tarmoqlar guruhi. AS ichida — **ichki** protokollar (IGP: RIP, OSPF, EIGRP), AS lar orasida — **tashqi** protokol (EGP: BGP).

RIP muammolarini kamaytirish usullari: **split horizon** (marshrutni u kelgan qoʻshniga qaytarib eʼlon qilmaslik), **poison reverse**, maksimal 15 sakrash va **hold-down** taymerlari.

## Ishlangan misol 1: masofa-vektor algoritmi

*n* ta router va *m* ta kanal (narxi bilan) berilgan. Har bir raundda barcha routerlar bir vaqtda qoʻshnilarining oldingi raunddagi masofa vektorlarini olib, oʻz jadvalini yangilaydi: *d*(*i*, *j*) = min(*c*(*i*, *k*) + *d*(*k*, *j*)) barcha qoʻshni *k* boʻyicha. Boshida har bir router faqat oʻzini biladi. Jadvallar oʻzgarmay qolguncha nechta raund kerakligini, soʻngra *s*-routerning jadvalini (manzil, narx, keyingi qadam; teng boʻlsa — kichik raqamli qoʻshni) chiqaring.

```python
CHEKSIZ = 10 ** 9
n, m = map(int, input().split())
qoshni = [dict() for _ in range(n + 1)]
for _ in range(m):
    u, v, c = map(int, input().split())
    qoshni[u][v] = c
    qoshni[v][u] = c
s = int(input())

d = [[CHEKSIZ] * (n + 1) for _ in range(n + 1)]
for i in range(1, n + 1):
    d[i][i] = 0
raund = 0
while True:
    yangi = [qator[:] for qator in d]
    for i in range(1, n + 1):
        for j in range(1, n + 1):
            for k, c in qoshni[i].items():             # qoshnining eski vektori
                if d[k][j] < CHEKSIZ and c + d[k][j] < yangi[i][j]:
                    yangi[i][j] = c + d[k][j]
    if yangi == d:
        break
    d = yangi
    raund += 1

print("raundlar:", raund)
for j in range(1, n + 1):
    if j == s:
        continue
    keyingi = min(k for k, c in qoshni[s].items() if d[k][j] < CHEKSIZ and c + d[k][j] == d[s][j])
    print(j, d[s][j], keyingi)
```
```java
import java.util.Scanner;

public class Main {
    static final long CHEKSIZ = 1_000_000_000L;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        long[][] narx = new long[n + 1][n + 1];                // 0 — kanal yoʻq
        for (int i = 0; i < m; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            long c = in.nextLong();
            narx[u][v] = c;
            narx[v][u] = c;
        }
        int s = in.nextInt();

        long[][] d = new long[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) d[i][j] = i == j ? 0 : CHEKSIZ;
        }
        int raund = 0;
        while (true) {
            long[][] yangi = new long[n + 1][];
            for (int i = 0; i <= n; i++) yangi[i] = d[i].clone();
            boolean ozgardi = false;
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    for (int k = 1; k <= n; k++) {             // qoshnining eski vektori
                        if (narx[i][k] > 0 && d[k][j] < CHEKSIZ && narx[i][k] + d[k][j] < yangi[i][j]) {
                            yangi[i][j] = narx[i][k] + d[k][j];
                            ozgardi = true;
                        }
                    }
                }
            }
            if (!ozgardi) break;
            d = yangi;
            raund++;
        }

        System.out.println("raundlar: " + raund);
        for (int j = 1; j <= n; j++) {
            if (j == s) continue;
            int keyingi = 0;
            for (int k = 1; k <= n && keyingi == 0; k++) {
                if (narx[s][k] > 0 && d[k][j] < CHEKSIZ && narx[s][k] + d[k][j] == d[s][j]) keyingi = k;
            }
            System.out.println(j + " " + d[s][j] + " " + keyingi);
        }
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

const long long CHEKSIZ = 1000000000LL;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<long long>> narx(n + 1, vector<long long>(n + 1, 0));   // 0 — kanal yoʻq
    for (int i = 0; i < m; i++) {
        int u, v;
        long long c;
        cin >> u >> v >> c;
        narx[u][v] = c;
        narx[v][u] = c;
    }
    int s;
    cin >> s;

    vector<vector<long long>> d(n + 1, vector<long long>(n + 1, CHEKSIZ));
    for (int i = 1; i <= n; i++) d[i][i] = 0;
    int raund = 0;
    while (true) {
        vector<vector<long long>> yangi = d;
        bool ozgardi = false;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                for (int k = 1; k <= n; k++) {                 // qoshnining eski vektori
                    if (narx[i][k] > 0 && d[k][j] < CHEKSIZ && narx[i][k] + d[k][j] < yangi[i][j]) {
                        yangi[i][j] = narx[i][k] + d[k][j];
                        ozgardi = true;
                    }
                }
            }
        }
        if (!ozgardi) break;
        d = yangi;
        raund++;
    }

    cout << "raundlar: " << raund << endl;
    for (int j = 1; j <= n; j++) {
        if (j == s) continue;
        int keyingi = 0;
        for (int k = 1; k <= n && keyingi == 0; k++) {
            if (narx[s][k] > 0 && d[k][j] < CHEKSIZ && narx[s][k] + d[k][j] == d[s][j]) keyingi = k;
        }
        cout << j << " " << d[s][j] << " " << keyingi << endl;
    }
    return 0;
}
```

Kirish:

```text
5 6
1 2 1
2 3 2
3 4 1
1 4 5
4 5 3
3 5 7
1
```

Chiqish:

```text
raundlar: 4
2 1 2
3 3 2
4 4 2
5 7 2
```

1-routerdan 4-routerga toʻgʻridan-toʻgʻri kanal (narxi 5) bor, lekin 1–2–3–4 yoʻli arzonroq (4) — algoritm buni ikkinchi va uchinchi raundlarda "kashf qiladi". Eng uzoq yoʻl (1 dan 5 gacha) 4 ta kanaldan iborat, shuning uchun jadvallar 4 raundda barqarorlashadi: masofa-vektor protokollarida axborot har raundda faqat bitta sakrashga tarqaladi.

## Ishlangan misol 2: ARP keshi

Birinchi satrda kesh yozuvining yashash vaqti *T* (soniya) va hodisalar soni *k*. Keyingi *k* satrda vaqt (soniya, oʻsish tartibida) va kompyuter paket yubormoqchi boʻlgan IP manzil. Yozuv keshda boʻlsa va yoshi *T* dan kichik boʻlsa — `kesh`, aks holda ARP soʻrovi yuboriladi (`ARP`) va yozuv yangilanadi. Oxirida yuborilgan ARP soʻrovlari sonini chiqaring.

```python
t_yashash, k = map(int, input().split())
kesh = {}                                     # IP -> yozilgan vaqt
soni = 0
for _ in range(k):
    vaqt, ip = input().split()
    vaqt = int(vaqt)
    if ip in kesh and vaqt - kesh[ip] < t_yashash:
        print(vaqt, ip, "kesh")
    else:
        print(vaqt, ip, "ARP")               # broadkast soʻrov va javob
        kesh[ip] = vaqt
        soni += 1
print("ARP soʻrovlari:", soni)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int tYashash = in.nextInt();
        int k = in.nextInt();
        Map<String, Integer> kesh = new HashMap<>();          // IP -> yozilgan vaqt
        int soni = 0;
        for (int i = 0; i < k; i++) {
            int vaqt = in.nextInt();
            String ip = in.next();
            if (kesh.containsKey(ip) && vaqt - kesh.get(ip) < tYashash) {
                System.out.println(vaqt + " " + ip + " kesh");
            } else {
                System.out.println(vaqt + " " + ip + " ARP");  // broadkast soʻrov va javob
                kesh.put(ip, vaqt);
                soni++;
            }
        }
        System.out.println("ARP soʻrovlari: " + soni);
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    int tYashash, k;
    cin >> tYashash >> k;
    map<string, int> kesh;                                    // IP -> yozilgan vaqt
    int soni = 0;
    for (int i = 0; i < k; i++) {
        int vaqt;
        string ip;
        cin >> vaqt >> ip;
        if (kesh.count(ip) && vaqt - kesh[ip] < tYashash) {
            cout << vaqt << " " << ip << " kesh" << endl;
        } else {
            cout << vaqt << " " << ip << " ARP" << endl;      // broadkast soʻrov va javob
            kesh[ip] = vaqt;
            soni++;
        }
    }
    cout << "ARP soʻrovlari: " << soni << endl;
    return 0;
}
```

Kirish:

```text
120 6
0 192.168.1.1
5 192.168.1.1
30 192.168.1.20
100 192.168.1.1
125 192.168.1.1
130 192.168.1.20
```

Chiqish:

```text
0 192.168.1.1 ARP
5 192.168.1.1 kesh
30 192.168.1.20 ARP
100 192.168.1.1 kesh
125 192.168.1.1 ARP
130 192.168.1.20 kesh
ARP soʻrovlari: 3
```

125-soniyada shlyuz yozuvining yoshi 125 ≥ 120 — u eskirgan va qayta soʻraladi. Kesh boʻlmaganida har bir paket uchun broadkast soʻrov ketardi — 6 ta oʻrniga 3 ta.

:::warning
**Koʻp uchraydigan xatolar**

- **Boshqa tarmoqdagi kompyuterning MAC manzili ARP bilan soʻraladi deb oʻylash.** Soʻraladigan — standart shlyuzning MAC manzili.
- **`ping` ishlamasa, xost oʻchiq deb xulosa qilish.** ICMP brandmauerda yopilgan boʻlishi mumkin.
- **DHCP ning Request bosqichi unicast deb hisoblash.** Mijoz Request ni ham broadkast qiladi — boshqa serverlar oʻz takliflarini bekor qilishi uchun.
- **RIP metrikasini kanal tezligi deb oʻylash.** RIP faqat sakrashlarni sanaydi: sekin 2 sakrashli yoʻl tez 3 sakrashlidan "yaxshiroq" hisoblanadi.
- **Administrativ masofa va metrikani chalkashtirish.** Administrativ masofa turli manbalarni, metrika bitta protokol ichidagi yoʻllarni solishtiradi.
:::

:::tip
`arp -a` bilan ARP keshingizni koʻring, soʻng `arp -d *` (administrator huquqi bilan) bilan tozalab, `ping` dan keyin qayta qarang. Wireshark da `arp or dhcp` filtri bilan `ipconfig /release` va `ipconfig /renew` paytida DORA ning toʻrtta bosqichini toping.
:::

## Xulosa

- ARP IP manzil boʻyicha MAC manzilni broadkast soʻrov va unicast javob bilan topadi; natija keshda saqlanadi.
- ICMP — xizmat xabarlari: Echo (ping), Destination Unreachable, Time Exceeded (tracert).
- DHCP — DORA: Discover, Offer, Request, Ack; manzil ijaraga beriladi, boshqa tarmoq uchun relay agent kerak.
- Marshrutlash jadvali bevosita, statik va dinamik yozuvlardan iborat; tanlov — eng uzun prefiks, administrativ masofa, metrika.
- Protokollar: masofa-vektor (RIP), kanal holati (OSPF), yoʻl-vektor (BGP); AS ichida IGP, AS lar orasida BGP.

## Oʻzingizni tekshiring

:::check
Q: ARP soʻrovi va ARP javobi qanday manzilga yuboriladi?
A: Soʻrov — broadkast (FF:FF:FF:FF:FF:FF), javob — soʻrovchiga unicast.
Q: `tracert` qaysi ICMP xabaridan foydalanadi?
A: Time Exceeded (11-tur) — TTL tugaganda routerlar yuboradigan xabar.
Q: DORA qisqartmasi nimani bildiradi?
A: Discover, Offer, Request, Acknowledge — DHCP orqali manzil olish bosqichlari.
Q: Nima uchun OSPF RIP dan tezroq yaqinlashadi?
A: Har bir router butun tarmoq xaritasini oladi va eng qisqa yoʻllarni oʻzi hisoblaydi; RIP da axborot har yangilanishda faqat bitta sakrashga tarqaladi.
Q: RIP da metrika 16 boʻlsa, bu nimani bildiradi?
A: Tarmoqqa yetib boʻlmaydi (cheksizlik).
:::

:::link https://www.rfc-editor.org/rfc/rfc2131
RFC 2131: Dynamic Host Configuration Protocol
DHCP protokolining spetsifikatsiyasi (ingliz tilida).
:::
