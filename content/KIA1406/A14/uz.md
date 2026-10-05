---
title: Virtual VPN ulash va trafikni himoyalash
summary: Amaliy mashgʻulot — VPN nima uchun kerak va undan nimani kutmaslik kerak; VPN turlari (masofaviy kirish, saytdan-saytga); protokollar — WireGuard, OpenVPN, IPsec va ularni solishtirish; laboratoriyada WireGuard ni sozlash — kalit juftlari, [Interface] va [Peer] boʻlimlari, AllowedIPs, Endpoint, ulanish va holatni tekshirish; toʻliq va boʻlingan tunnel (split tunnel); DNS sizishi va "kill switch"; VPN provayderini tanlash mezonlari; tashkilotda VPN siyosati; manzil tunnel orqali yoki toʻgʻridan-toʻgʻri ketishini aniqlovchi dastur.
---

## Nima uchun bu mavzu muhim?

M14 maʼruzasida VPN — ochiq tarmoq ustida shifrlangan tunnel ekanini koʻrdik. Bu amaliy mashgʻulotda oʻzingizning **oʻquv laboratoriyangizda** (ikkita virtual mashina yoki kompyuter va oʻquv serveri) VPN tunnelini sozlaysiz, uning ishlashini tekshirasiz va trafik qaysi yoʻldan ketishini boshqarishni oʻrganasiz. Masofaviy ishlaydigan xodimlar, filiallar va ommaviy Wi-Fi da ishlash — VPN ning kundalik qoʻllanish joylari.

:::note
**Bu amaliy mashgʻulotdan soʻng siz:**

- VPN qaysi xavflardan himoya qilishini va qaysilaridan qilmasligini tushuntira olasiz;
- WireGuard, OpenVPN va IPsec ni solishtira olasiz;
- laboratoriyada WireGuard tunnelini sozlab, uning ishlashini tekshira olasiz;
- toʻliq va boʻlingan tunnel farqini tushunib, yoʻnalishni CIDR boʻyicha aniqlay olasiz;
- DNS sizishi va "kill switch" ni tekshira olasiz.
:::

:::warning
Mashgʻulotni faqat oʻzingizga tegishli yoki oʻqituvchi ruxsat bergan oʻquv tarmogʻida bajaring. Tashkilot tarmogʻida VPN dan foydalanish uning xavfsizlik siyosatiga boʻysunadi; VPN dan foydalanish boʻyicha amaldagi qonunchilik talablariga rioya qiling.
:::

## VPN nimadan himoya qiladi?

| Himoya qiladi | Himoya qilmaydi |
|---|---|
| ommaviy Wi-Fi da trafikni begonalar oʻqishidan | zararli saytlar va fishingdan |
| provayder yoki tarmoq egasi trafik tarkibini koʻrishidan | qurilmadagi zararli dasturlardan |
| masofadan ichki tarmoqqa xavfsiz kirish (xodimlar, filiallar) | zaif parol yoki buzilgan hisobdan |
| ochiq tarmoqda maʼlumot yaxlitligini | VPN provayderining oʻzidan — u trafikni koʻradi |

VPN — maxfiylik va xavfsiz kirish vositasi, lekin "toʻliq himoya" emas: antivirus, yangilanishlar, MFA va ehtiyotkorlik baribir kerak.

## VPN turlari va protokollar

- **Masofaviy kirish** (remote access) — xodim noutbukdan ofis tarmogʻiga ulanadi.
- **Saytdan-saytga** (site-to-site) — ikki ofis tarmogʻi doimiy tunnel bilan birlashtiriladi.

| Protokol | Xususiyati | Afzallik | Kamchilik |
|---|---|---|---|
| **WireGuard** | zamonaviy, kod hajmi kichik, Linux yadrosida | tez, sozlash oddiy, zamonaviy kriptografiya | sozlamalar statik, foydalanuvchi boshqaruvi tashqi vositalar bilan |
| **OpenVPN** | TLS asosida, uzoq yillik tajriba | moslashuvchan, TCP yoki UDP, koʻp platformada | sekinroq, sozlash murakkabroq |
| **IPsec** (IKEv2) | tarmoq qatlamida ishlaydi (OSI 3) | koʻp operatsion tizim va qurilmalarda oʻrnatilgan, saytdan-saytga standarti | sozlash murakkab |

## Amaliy ish: WireGuard tunnelini sozlash

**Laboratoriya**: ikkita Linux virtual mashina — **server** (masalan, 192.168.56.10) va **mijoz**. Tunnel ichidagi manzillar: server `10.8.0.1`, mijoz `10.8.0.2`.

**1. Oʻrnatish va kalitlar** (har ikki mashinada):

```text
sudo apt install wireguard
wg genkey | tee privatekey | wg pubkey > publickey
chmod 600 privatekey
```

Har bir tomon **yopiq kalitini** oʻzida saqlaydi va hech kimga bermaydi; **ochiq kalitlar** almashiladi (S14 va MT30 dagi asimmetrik kriptografiya gʻoyasi).

**2. Server sozlamasi** `/etc/wireguard/wg0.conf`:

```text
[Interface]
Address = 10.8.0.1/24
ListenPort = 51820
PrivateKey = <server yopiq kaliti>

[Peer]
PublicKey = <mijoz ochiq kaliti>
AllowedIPs = 10.8.0.2/32
```

**3. Mijoz sozlamasi** `/etc/wireguard/wg0.conf`:

```text
[Interface]
Address = 10.8.0.2/24
PrivateKey = <mijoz yopiq kaliti>
DNS = 10.8.0.1

[Peer]
PublicKey = <server ochiq kaliti>
Endpoint = 192.168.56.10:51820
AllowedIPs = 10.8.0.0/24
PersistentKeepalive = 25
```

**4. Ishga tushirish va tekshirish**:

```text
sudo wg-quick up wg0
sudo wg show
ping 10.8.0.1
```

`wg show` chiqishida **latest handshake** (oxirgi kelishuv vaqti) va **transfer** (yuborilgan va qabul qilingan baytlar) koʻrinishi kerak. Server tarmoq ekranida faqat 51820/UDP porti ochiladi (M14).

**5. Trafik shifrlanganini tekshirish**: A13 mashgʻulotidagi kabi trafik tahlilchisi bilan tashqi interfeysni kuzating — tunnel ichidagi `ping` yoki HTTP soʻrov oʻrniga faqat 51820-portga UDP paketlari koʻrinadi, ularning mazmuni shifrlangan.

## Toʻliq va boʻlingan tunnel

**AllowedIPs** mijoz tomonida "qaysi manzillar tunnel orqali yuborilsin" degan **marshrutlash qoidasi** vazifasini bajaradi:

| Rejim | AllowedIPs | Natija |
|---|---|---|
| **Toʻliq tunnel** | `0.0.0.0/0` | barcha trafik VPN orqali — ommaviy Wi-Fi da maksimal himoya, lekin sekinroq |
| **Boʻlingan tunnel** (split tunnel) | faqat ichki tarmoqlar, masalan `10.8.0.0/24, 192.168.50.0/24` | ofis resurslari — tunnel orqali, qolgan Internet — toʻgʻridan-toʻgʻri |

Boʻlingan tunnel kanalni tejaydi, lekin Internet trafigi VPN himoyasidan tashqarida qoladi — tashkilot siyosati qaysi rejim qachon ishlatilishini belgilaydi.

## Ishlangan misol: manzil qaysi yoʻldan ketadi?

Birinchi satrda mijozning AllowedIPs roʻyxati (CIDR, vergul bilan), keyingi satrlarda — manzil manzillari. Har bir manzil uchun u tunnel orqali (qaysi qoida boʻyicha) yoki toʻgʻridan-toʻgʻri ketishini aniqlang. Bir nechta qoida mos kelsa, eng uzun prefiksli (eng aniq) qoida tanlanadi — haqiqiy marshrutlashdagi kabi.

```python
import sys

def son(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d

def mos(ip, cidr):
    tarmoq, uzunlik = cidr.split("/")
    uzunlik = int(uzunlik)
    niqob = 0 if uzunlik == 0 else (0xFFFFFFFF << (32 - uzunlik)) & 0xFFFFFFFF
    return (son(ip) & niqob) == (son(tarmoq) & niqob)

satrlar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
qoidalar = satrlar[0].split(",")
for ip in satrlar[1:]:
    eng_yaxshi = None                                  # eng uzun prefiksli mos qoida
    for q in qoidalar:
        if mos(ip, q) and (eng_yaxshi is None or int(q.split("/")[1]) > int(eng_yaxshi.split("/")[1])):
            eng_yaxshi = q
    if eng_yaxshi:
        print(f"{ip} -> tunnel orqali ({eng_yaxshi})")
    else:
        print(f"{ip} -> toʻgʻridan-toʻgʻri")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long son(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static boolean mos(String ip, String cidr) {
        String[] q = cidr.split("/");
        int uzunlik = Integer.parseInt(q[1]);
        long niqob = uzunlik == 0 ? 0 : (0xFFFFFFFFL << (32 - uzunlik)) & 0xFFFFFFFFL;
        return (son(ip) & niqob) == (son(q[0]) & niqob);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s);
        }
        String[] qoidalar = satrlar.get(0).split(",");
        for (String ip : satrlar.subList(1, satrlar.size())) {
            String engYaxshi = null;                       // eng uzun prefiksli mos qoida
            for (String q : qoidalar)
                if (mos(ip, q) && (engYaxshi == null || Integer.parseInt(q.split("/")[1]) > Integer.parseInt(engYaxshi.split("/")[1])))
                    engYaxshi = q;
            if (engYaxshi != null) System.out.println(ip + " -> tunnel orqali (" + engYaxshi + ")");
            else System.out.println(ip + " -> toʻgʻridan-toʻgʻri");
        }
    }
}
```
```cpp
#include <cstdint>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

uint32_t son(const string& ip) {
    uint32_t a, b, c, d;
    char t;
    istringstream ss(ip);
    ss >> a >> t >> b >> t >> c >> t >> d;
    return (a << 24) | (b << 16) | (c << 8) | d;
}

int uzunligi(const string& cidr) { return stoi(cidr.substr(cidr.find('/') + 1)); }

bool mos(const string& ip, const string& cidr) {
    int uzunlik = uzunligi(cidr);
    uint32_t niqob = uzunlik == 0 ? 0 : (0xFFFFFFFFu << (32 - uzunlik));
    return (son(ip) & niqob) == (son(cidr.substr(0, cidr.find('/'))) & niqob);
}

int main() {
    string satr;
    getline(cin, satr);
    vector<string> qoidalar;
    stringstream ss(satr);
    string q;
    while (getline(ss, q, ',')) {
        q.erase(q.find_last_not_of(" \r") + 1);
        qoidalar.push_back(q);
    }
    string ip;
    while (cin >> ip) {
        string engYaxshi;                                  // eng uzun prefiksli mos qoida
        for (auto& r : qoidalar)
            if (mos(ip, r) && (engYaxshi.empty() || uzunligi(r) > uzunligi(engYaxshi))) engYaxshi = r;
        if (!engYaxshi.empty()) cout << ip << " -> tunnel orqali (" << engYaxshi << ")\n";
        else cout << ip << " -> toʻgʻridan-toʻgʻri\n";
    }
    return 0;
}
```

Kirish:

```text
10.8.0.0/24,192.168.50.0/24,172.16.0.0/12,192.168.50.128/25
10.8.0.15
192.168.50.200
192.168.50.20
172.20.5.9
8.8.8.8
192.168.1.10
```

Chiqish:

```text
10.8.0.15 -> tunnel orqali (10.8.0.0/24)
192.168.50.200 -> tunnel orqali (192.168.50.128/25)
192.168.50.20 -> tunnel orqali (192.168.50.0/24)
172.20.5.9 -> tunnel orqali (172.16.0.0/12)
8.8.8.8 -> toʻgʻridan-toʻgʻri
192.168.1.10 -> toʻgʻridan-toʻgʻri
```

`192.168.50.200` ikkita qoidaga mos keladi; aniqroq `/25` tanlandi. `172.16.0.0/12` 172.16.0.0 dan 172.31.255.255 gacha boʻlgan xususiy diapazonni qamraydi. Uy tarmogʻidagi `192.168.1.10` va ommaviy `8.8.8.8` toʻgʻridan-toʻgʻri ketadi — bu boʻlingan tunnel. Agar roʻyxatga `0.0.0.0/0` qoʻshilsa, barcha manzillar tunnelga tushadi (toʻliq tunnel).

## DNS sizishi va kill switch

- **DNS sizishi**: trafik tunnel orqali ketsa ham, DNS soʻrovlari mahalliy provayderga ketishi mumkin — u qaysi saytlarga kirilayotganini koʻradi. Tekshirish: VPN yoqilgan holda DNS sizishini tekshiruvchi sayt yoki `nslookup` bilan qaysi DNS server javob berayotganini koʻrish; yechim — mijoz sozlamasida `DNS =` qatori va toʻliq tunnelda DNS ni tunnel ichiga yoʻnaltirish.
- **Kill switch**: VPN uzilib qolsa, trafik avtomatik ravishda shifrlanmagan holda ketmasligi uchun — tunnel tiklanmaguncha Internetni bloklash (tarmoq ekrani qoidasi yoki mijoz dasturidagi sozlama).

## VPN provayderini tanlash

Tijorat VPN xizmatini tanlashda: jurnallar siyosati (qanday maʼlumot saqlanadi) va uni mustaqil audit tasdiqlaganmi, kompaniya qaysi yurisdiksiyada, qaysi protokollar va shifrlash, kill switch va DNS sizishidan himoya bormi. **Bepul VPN** lar koʻpincha foydalanuvchi maʼlumotini sotish yoki reklama orqali daromad qiladi — korporativ maqsadlar uchun faqat tashkilot tasdiqlagan VPN ishlatiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Yopiq kalitni boshqa tomonga yuborish yoki ochiq joyda saqlash.** Faqat ochiq kalitlar almashiladi; `privatekey` huquqlari 600.
- **AllowedIPs ni notoʻgʻri yozish.** Kerakli tarmoq tunnelga tushmaydi yoki aksincha butun trafik tunnelga ketadi.
- **VPN ni "toʻliq himoya" deb bilish.** Fishing va zararli dasturlardan himoya qilmaydi.
- **DNS sizishini tekshirmaslik.**
- **Server tarmoq ekranida keraksiz portlarni ochish.** Faqat VPN porti.
:::

:::tip
Mashgʻulot hisobotiga quyidagilarni kiriting: ikkala tomonning sozlama fayllari (yopiq kalitlarsiz!), `wg show` chiqishi, tunnel orqali `ping` natijasi va tashqi interfeysda faqat shifrlangan UDP paketlari koʻrinayotgani skrinshoti.
:::

## Xulosa

- VPN ochiq tarmoqda shifrlangan tunnel yaratadi; fishing, zararli dasturlar va zaif parollardan himoya qilmaydi.
- Protokollar: WireGuard (tez, oddiy), OpenVPN (moslashuvchan), IPsec (tarmoq qatlami, saytdan-saytga).
- WireGuard: kalit juftlari, [Interface] va [Peer], AllowedIPs — marshrutlash qoidasi; `wg show` bilan tekshirish.
- Toʻliq tunnel (`0.0.0.0/0`) va boʻlingan tunnel; eng uzun prefiksli qoida tanlanadi.
- DNS sizishi va kill switch tekshiriladi; provayder jurnallar siyosati va yurisdiksiya boʻyicha tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: WireGuard da AllowedIPs mijoz tomonida nimani belgilaydi?
A: Qaysi manzillarga trafik tunnel orqali yuborilishini — u marshrutlash qoidasi vazifasini bajaradi.
Q: Toʻliq tunnel uchun AllowedIPs qanday yoziladi?
A: 0.0.0.0/0.
Q: DNS sizishi nima?
A: Trafik tunnel orqali ketsa ham, DNS soʻrovlari tunneldan tashqarida mahalliy provayderga ketishi.
Q: VPN qaysi xavflardan himoya qilmaydi?
A: Fishing va zararli saytlardan, qurilmadagi zararli dasturlardan, zaif parollardan.
:::

:::link https://www.wireguard.com/quickstart/
WireGuard: Quick Start
WireGuard ni oʻrnatish va sozlash boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
