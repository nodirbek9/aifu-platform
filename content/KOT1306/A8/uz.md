---
title: Marshrutizatorlarni boshqarish
summary: Marshrutizator tarkibi (CPU, RAM, NVRAM, Flash, ROM) va yuklanish jarayoni; konsol, SSH va Telnet orqali ulanish; Cisco IOS rejimlari va asosiy buyruqlar; interfeyslarni sozlash; parollar va SSH bilan himoyalash; statik va standart marshrutlar; show ip route chiqishini oʻqish; sozlamalarni saqlash; paket yoʻlini marshrutlash jadvallari boʻyicha kuzatish.
---

## Nima uchun bu mavzu muhim?

Marshrutizator — tarmoqlar orasidagi "chorraha". U toʻgʻri sozlanmasa, butun boʻlim Internetsiz qoladi; parolsiz qoldirilsa, begona odam butun tarmoqni boshqarib oladi. Tarmoq muhandisining kundalik ishi — marshrutizatorga ulanish, interfeyslarni sozlash, marshrut qoʻshish va nosozlik sababini `show` buyruqlari bilan topish. Bu mashgʻulotda Cisco IOS misolida shu koʻnikmalarni egallaymiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- marshrutizator xotira turlarini va yuklanish ketma-ketligini ayta olasiz;
- konsol yoki SSH orqali ulanib, IOS rejimlari orasida harakatlana olasiz;
- interfeyslarga IP manzil berib, statik va standart marshrutlarni qoʻsha olasiz;
- marshrutizatorni parollar va SSH bilan himoyalay olasiz;
- `show ip route` chiqishini oʻqib, paket qaysi yoʻl bilan ketishini aniqlay olasiz.
:::

## Marshrutizator tarkibi

| Qism | Nima saqlanadi |
|---|---|
| **ROM** | yuklovchi (bootstrap), POST testi, favqulodda ROMMON rejimi |
| **Flash** | IOS operatsion tizimi obrazi (`.bin` fayl) |
| **NVRAM** | **startup-config** — yoqilganda yuklanadigan sozlamalar |
| **RAM** | ishlayotgan IOS, **running-config**, marshrutlash va ARP jadvallari, paket buferlari |

**Yuklanish**: POST (uskunani tekshirish) → ROM dagi bootstrap → Flash dan IOS ni RAM ga yuklash → NVRAM dagi startup-config ni running-config ga koʻchirish. Startup-config boʻlmasa — boshlangʻich sozlash dialogi chiqadi.

Muhim: siz kiritgan buyruqlar darhol **running-config** ga (RAM) yoziladi. `copy running-config startup-config` (yoki `write memory`) qilinmasa, qayta yuklanishda **yoʻqoladi**.

## Ulanish usullari

| Usul | Qanday | Qachon |
|---|---|---|
| **Konsol** | maxsus konsol kabel, terminal dasturi (PuTTY, 9600 bod) | birinchi sozlash, tarmoq ishlamaganda |
| **SSH** | tarmoq orqali, shifrlangan | kundalik masofaviy boshqarish |
| **Telnet** | tarmoq orqali, **shifrlanmagan** | ishlatmaslik kerak — parol ochiq koʻrinadi |
| **AUX** | modem orqali | eski usul |

## IOS rejimlari va asosiy buyruqlar

```text
Router>                       foydalanuvchi rejimi (faqat koʻrish)
Router> enable
Router#                       imtiyozli rejim (show, copy, reload, debug)
Router# configure terminal
Router(config)#               global sozlash
Router(config)# interface g0/0
Router(config-if)#            interfeys sozlash
Router(config-if)# exit       bir pogʻona yuqoriga;  end yoki Ctrl+Z — imtiyozli rejimga
```

| Buyruq | Nimani koʻrsatadi |
|---|---|
| `show ip interface brief` | interfeyslar, IP manzillar, holati (up/down) |
| `show ip route` | marshrutlash jadvali |
| `show running-config` | joriy sozlamalar |
| `show version` | IOS versiyasi, ish vaqti, xotira |
| `show interfaces g0/0` | interfeys statistikasi, xatolar |
| `show cdp neighbors` | qoʻshni Cisco qurilmalar |

Interfeyslar holati: **up/up** — ishlayapti; **administratively down/down** — `no shutdown` berilmagan; **up/down** — kabel bor, lekin 2-pogʻonada muammo; **down/down** — kabel yoki qarama-qarshi qurilma yoʻq.

## Himoyalash

```text
Router(config)# hostname R1
R1(config)# enable secret Kuchli#Parol2026          imtiyozli rejim paroli (xeshlanadi)
R1(config)# service password-encryption               boshqa parollarni yashirish
R1(config)# line console 0
R1(config-line)# password KonsolParol
R1(config-line)# login
R1(config-line)# exit
R1(config)# ip domain-name aifu.local
R1(config)# crypto key generate rsa modulus 2048      SSH kalitlari
R1(config)# username admin secret AdminParol
R1(config)# line vty 0 4
R1(config-line)# login local
R1(config-line)# transport input ssh                  faqat SSH, Telnet yopiq
R1(config)# banner motd #Faqat ruxsat etilgan foydalanuvchilar uchun#
```

`enable password` emas, **`enable secret`** ishlating — birinchisi sozlamada ochiq matn sifatida saqlanadi.

## Marshrutlar

```text
R1(config)# ip route 10.20.0.0 255.255.0.0 192.168.1.2     statik marshrut: tarmoq maska keyingi_qadam
R1(config)# ip route 0.0.0.0 0.0.0.0 203.0.113.1           standart marshrut (default)
```

`show ip route` chiqishidagi kodlar: **C** — bevosita ulangan, **L** — interfeysning oʻz manzili (/32), **S** — statik, **R** — RIP, **O** — OSPF, **\*** — standart marshrut nomzodi. Yozuvdagi `[1/0]` — [administrativ masofa/metrika].

Marshrutizator har bir paket uchun jadvaldan **eng uzun mos prefiksni** tanlaydi; bu tanlov dasturi — ushbu mashgʻulotning mustaqil topshirigʻi (KOT-A8).

## Amaliy ish: uchta marshrutizator

1. Packet Tracer da R1, R2, R3 ni ketma-ket ulang (R1–R2: 10.0.12.0/30, R2–R3: 10.0.23.0/30). R1 ortida LAN-A (192.168.1.0/24), R3 ortida LAN-B (192.168.3.0/24).
2. Har bir routerda interfeyslarni sozlang (`ip address`, `no shutdown`) va `show ip interface brief` bilan tekshiring.
3. R1 va R3 ga qarama-qarshi LAN uchun statik marshrut, R2 ga ikkala LAN uchun marshrut qoʻshing.
4. LAN-A dagi kompyuterdan LAN-B ga `ping` va `tracert` bajaring.
5. R2 dagi bitta marshrutni oʻchiring (`no ip route ...`) va `tracert` qayerda toʻxtashini koʻring.
6. Barcha routerlarda sozlamalarni saqlang.

## Ishlangan misol 1: paket yoʻlini kuzatish

Birinchi satrda yozuvlar soni *k*, keyingi *k* satrda: router, manzil tarmogʻi nomi va keyingi qadam (`direct` — tarmoq shu routerga bevosita ulangan; manzil `default` — standart marshrut). Soʻng *q* ta soʻrov: boshlangʻich router va manzil tarmogʻi. Paket TTL = 6 bilan yuboriladi; har bir uzatishda TTL 1 ga kamayadi. Yoʻlni va natijani chiqaring: `yetkazildi`, `marshrut yoʻq` yoki `TTL tugadi`.

```python
k = int(input())
jadval = {}                                   # (router, tarmoq) -> keyingi qadam
for _ in range(k):
    router, tarmoq, keyingi = input().split()
    jadval[(router, tarmoq)] = keyingi
q = int(input())
for _ in range(q):
    joriy, tarmoq = input().split()
    yol = [joriy]
    ttl = 6
    while True:
        keyingi = jadval.get((joriy, tarmoq), jadval.get((joriy, "default")))
        if keyingi == "direct":
            natija = "yetkazildi"
            break
        if keyingi is None:
            natija = "marshrut yoʻq"
            break
        if ttl == 0:
            natija = "TTL tugadi"             # halqadagi paket tashlanadi
            break
        ttl -= 1
        joriy = keyingi
        yol.append(joriy)
    print(" -> ".join(yol) + " (" + natija + ")")
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
        Map<String, String> jadval = new HashMap<>();          // "router tarmoq" -> keyingi qadam
        for (int i = 0; i < k; i++) {
            String router = in.next();
            String tarmoq = in.next();
            jadval.put(router + " " + tarmoq, in.next());
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String joriy = in.next();
            String tarmoq = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(joriy);
            int ttl = 6;
            String natija;
            while (true) {
                String keyingi = jadval.getOrDefault(joriy + " " + tarmoq, jadval.get(joriy + " default"));
                if ("direct".equals(keyingi)) {
                    natija = "yetkazildi";
                    break;
                }
                if (keyingi == null) {
                    natija = "marshrut yoʻq";
                    break;
                }
                if (ttl == 0) {
                    natija = "TTL tugadi";                      // halqadagi paket tashlanadi
                    break;
                }
                ttl--;
                joriy = keyingi;
                yol.add(joriy);
            }
            System.out.println(String.join(" -> ", yol) + " (" + natija + ")");
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
    int k;
    cin >> k;
    map<pair<string, string>, string> jadval;                // (router, tarmoq) -> keyingi qadam
    for (int i = 0; i < k; i++) {
        string router, tarmoq, keyingi;
        cin >> router >> tarmoq >> keyingi;
        jadval[{router, tarmoq}] = keyingi;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string joriy, tarmoq;
        cin >> joriy >> tarmoq;
        vector<string> yol = {joriy};
        int ttl = 6;
        string natija;
        while (true) {
            string keyingi;
            if (jadval.count({joriy, tarmoq})) keyingi = jadval[{joriy, tarmoq}];
            else if (jadval.count({joriy, "default"})) keyingi = jadval[{joriy, "default"}];
            if (keyingi == "direct") {
                natija = "yetkazildi";
                break;
            }
            if (keyingi.empty()) {
                natija = "marshrut yoʻq";
                break;
            }
            if (ttl == 0) {
                natija = "TTL tugadi";                          // halqadagi paket tashlanadi
                break;
            }
            ttl--;
            joriy = keyingi;
            yol.push_back(joriy);
        }
        string chiqish;
        for (size_t j = 0; j < yol.size(); j++) chiqish += (j ? " -> " : "") + yol[j];
        cout << chiqish << " (" << natija << ")" << endl;
    }
    return 0;
}
```

Kirish:

```text
7
R1 LAN-A direct
R1 LAN-B R2
R2 LAN-A R1
R2 LAN-B R3
R2 default R3
R3 LAN-B direct
R3 default R2
4
R1 LAN-B
R3 LAN-A
R1 LAN-C
R2 LAN-C
```

Chiqish:

```text
R1 -> R2 -> R3 (yetkazildi)
R3 -> R2 -> R1 (yetkazildi)
R1 (marshrut yoʻq)
R2 -> R3 -> R2 -> R3 -> R2 -> R3 -> R2 (TTL tugadi)
```

Oxirgi qator — klassik **marshrutlash halqasi**: R2 ning standart marshruti R3 ga, R3 niki esa R2 ga qaraydi. Mavjud boʻlmagan LAN-C ga paket TTL tugaguncha ular orasida aylanadi. Haqiqiy tarmoqda `tracert` buni bir xil ikki manzilning navbatma-navbat takrorlanishi sifatida koʻrsatadi.

## Ishlangan misol 2: sozlama buyruqlarini yaratish

Birinchi satrda router nomi, ikkinchisida interfeyslar soni *n*, keyin *n* ta "interfeys manzil/prefiks", soʻng marshrutlar soni *m* va *m* ta "tarmoq/prefiks keyingi_qadam". IOS ga kiritiladigan buyruqlar ketma-ketligini chiqaring (prefiks maskaga aylantiriladi).

```python
def maska(p):
    m = (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF if p > 0 else 0
    return ".".join(str((m >> s) & 255) for s in (24, 16, 8, 0))


nom = input().strip()
print("enable")
print("configure terminal")
print("hostname " + nom)
n = int(input())
for _ in range(n):
    interfeys, manzil = input().split()
    ip, p = manzil.split("/")
    print("interface " + interfeys)
    print(" ip address " + ip + " " + maska(int(p)))
    print(" no shutdown")
    print("exit")
m = int(input())
for _ in range(m):
    tarmoq, keyingi = input().split()
    ip, p = tarmoq.split("/")
    print("ip route " + ip + " " + maska(int(p)) + " " + keyingi)
print("end")
print("copy running-config startup-config")    # aks holda qayta yuklanishda yoʻqoladi
```
```java
import java.util.Scanner;

public class Main {
    static String maska(int p) {
        long m = p > 0 ? (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL : 0;
        return ((m >> 24) & 255) + "." + ((m >> 16) & 255) + "." + ((m >> 8) & 255) + "." + (m & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String nom = in.next();
        System.out.println("enable");
        System.out.println("configure terminal");
        System.out.println("hostname " + nom);
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String interfeys = in.next();
            String[] manzil = in.next().split("/");
            System.out.println("interface " + interfeys);
            System.out.println(" ip address " + manzil[0] + " " + maska(Integer.parseInt(manzil[1])));
            System.out.println(" no shutdown");
            System.out.println("exit");
        }
        int m = in.nextInt();
        for (int i = 0; i < m; i++) {
            String[] tarmoq = in.next().split("/");
            String keyingi = in.next();
            System.out.println("ip route " + tarmoq[0] + " " + maska(Integer.parseInt(tarmoq[1])) + " " + keyingi);
        }
        System.out.println("end");
        System.out.println("copy running-config startup-config");   // aks holda qayta yuklanishda yoʻqoladi
    }
}
```
```cpp
#include <cstdint>
#include <iostream>
#include <string>
using namespace std;

string maska(int p) {
    uint64_t m = p > 0 ? (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL : 0;
    return to_string((m >> 24) & 255) + "." + to_string((m >> 16) & 255) + "." + to_string((m >> 8) & 255) + "." + to_string(m & 255);
}

int main() {
    string nom;
    cin >> nom;
    cout << "enable" << endl;
    cout << "configure terminal" << endl;
    cout << "hostname " << nom << endl;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string interfeys, manzil;
        cin >> interfeys >> manzil;
        size_t chiziq = manzil.find('/');
        cout << "interface " << interfeys << endl;
        cout << " ip address " << manzil.substr(0, chiziq) << " " << maska(stoi(manzil.substr(chiziq + 1))) << endl;
        cout << " no shutdown" << endl;
        cout << "exit" << endl;
    }
    int m;
    cin >> m;
    for (int i = 0; i < m; i++) {
        string tarmoq, keyingi;
        cin >> tarmoq >> keyingi;
        size_t chiziq = tarmoq.find('/');
        cout << "ip route " << tarmoq.substr(0, chiziq) << " " << maska(stoi(tarmoq.substr(chiziq + 1))) << " " << keyingi << endl;
    }
    cout << "end" << endl;
    cout << "copy running-config startup-config" << endl;   // aks holda qayta yuklanishda yoʻqoladi
    return 0;
}
```

Kirish:

```text
R1
2
g0/0 192.168.1.1/24
g0/1 10.0.12.1/30
2
192.168.3.0/24 10.0.12.2
0.0.0.0/0 10.0.12.2
```

Chiqish:

```text
enable
configure terminal
hostname R1
interface g0/0
 ip address 192.168.1.1 255.255.255.0
 no shutdown
exit
interface g0/1
 ip address 10.0.12.1 255.255.255.252
 no shutdown
exit
ip route 192.168.3.0 255.255.255.0 10.0.12.2
ip route 0.0.0.0 0.0.0.0 10.0.12.2
end
copy running-config startup-config
```

Bunday "sozlama generatorlari" — tarmoqni avtomatlashtirishning (Ansible, Python + Netmiko) eng oddiy shakli: koʻp routerli tarmoqda buyruqlarni qoʻlda yozish xatoga olib keladi, shablon esa har doim bir xil va toʻgʻri natija beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Sozlamani saqlashni unutish.** `copy running-config startup-config` qilinmasa, elektr uzilganda hammasi yoʻqoladi.
- **Interfeysga `no shutdown` bermaslik.** Cisco router interfeyslari sukut boʻyicha oʻchiq (administratively down).
- **Telnet ishlatish va `enable password` qoʻyish.** Parollar ochiq koʻrinadi; SSH va `enable secret` ishlating.
- **Statik marshrutda keyingi qadam sifatida uzoqdagi routerni koʻrsatish.** Keyingi qadam — bevosita ulangan qoʻshni interfeys manzili.
- **Ikki routerning standart marshrutlarini bir-biriga qaratish.** Bu marshrutlash halqasini hosil qiladi (birinchi misoldagi LAN-C).
:::

:::tip
Packet Tracer dagi amaliy ishni bajarib boʻlgach, R2 da `show ip route` chiqishini nusxalab oling va har bir qatordagi kodni (C, L, S) va `[1/0]` qiymatlarini izohlang. Keyin `debug ip packet` ni yoqib (faqat laboratoriyada!), `ping` paytida router qaror qabul qilishini kuzating.
:::

## Xulosa

- Marshrutizator: ROM (bootstrap), Flash (IOS), NVRAM (startup-config), RAM (running-config, jadvallar).
- Ulanish: birinchi sozlashda konsol, keyin faqat SSH; Telnet — xavfli.
- IOS rejimlari: `>` → `enable` → `#` → `configure terminal` → `(config)#` → `interface` → `(config-if)#`.
- Asosiy buyruqlar: `show ip interface brief`, `show ip route`, `show running-config`; marshrutlar `ip route tarmoq maska keyingi_qadam`.
- Himoya: `enable secret`, konsol va VTY parollari, `transport input ssh`, `service password-encryption`; oxirida — sozlamani saqlash.

## Oʻzingizni tekshiring

:::check
Q: Running-config va startup-config qayerda saqlanadi?
A: Running-config — RAM da (joriy), startup-config — NVRAM da (yoqilganda yuklanadi).
Q: Interfeys holati "administratively down" boʻlsa, nima qilish kerak?
A: Interfeys sozlash rejimida `no shutdown` buyrugʻini berish.
Q: Standart marshrut qanday yoziladi?
A: `ip route 0.0.0.0 0.0.0.0 <keyingi_qadam>`.
Q: Nima uchun `enable password` emas, `enable secret` ishlatiladi?
A: `enable secret` parolni xesh sifatida saqlaydi, `enable password` esa ochiq matnda.
Q: `show ip route` da `S` va `C` kodlari nimani bildiradi?
A: S — statik marshrut, C — bevosita ulangan tarmoq.
:::

:::link https://www.netacad.com/courses/ccna-introduction-networks
CCNA: Introduction to Networks
Cisco Networking Academy ning router va kommutatorlarni sozlash boʻyicha bepul kursi (ingliz tilida).
:::
