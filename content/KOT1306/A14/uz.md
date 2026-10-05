---
title: Windows operatsion tizimida brandmauerni sozlash. VPN xizmatidan foydalanish
summary: Windows Defender Firewall — profillar (domen, xususiy, ommaviy), kiruvchi va chiquvchi qoidalar, wf.msc, netsh advfirewall va PowerShell; qoidalarni sinash va jurnal; NAT va PAT — nima uchun NAT ham kiruvchi ulanishlarni toʻsadi; Windows dagi VPN mijoz (IKEv2, SSTP, L2TP/IPsec), WireGuard, Windows Server RRAS; VPN ulanishini tekshirish.
---

## Nima uchun bu mavzu muhim?

M14 maʼruzasida brandmauer va VPN nazariyasini koʻrdik. Endi ularni amalda sozlaymiz: server faqat kerakli portlarni ochishi, noutbuk kafe Wi-Fi sida "ommaviy" profil bilan yopiq turishi, uydagi oʻqituvchi esa universitet tarmogʻiga shifrlangan tunnel orqali ulanishi kerak. Shuningdek, uy routeridagi **NAT** ham himoya rolini oʻynashini dastur bilan koʻramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- Windows Defender Firewall profillarini va qoidalar tartibini tushuntira olasiz;
- grafik vosita, `netsh` va PowerShell bilan kiruvchi va chiquvchi qoidalar yarata olasiz;
- qoidani sinab, brandmauer jurnalini oʻqiy olasiz;
- NAT/PAT jadvali qanday ishlashini va nima uchun u kutilmagan kiruvchi paketlarni toʻsishini tushuntira olasiz;
- Windows da VPN ulanishini sozlab, uni tekshira olasiz.
:::

## Windows Defender Firewall

Windows ga oʻrnatilgan **holatni kuzatuvchi** brandmauer. U tarmoq turiga qarab uchta **profil** ishlatadi:

| Profil | Qachon | Odatiy sozlama |
|---|---|---|
| **Domain** | kompyuter domen tarmogʻida (DC topilgan) | GPO boshqaradi |
| **Private** | uy yoki ishonchli tarmoq | fayl ulashish va topish ruxsat etilishi mumkin |
| **Public** | kafe, aeroport, mehmonxona Wi-Fi | eng qatʼiy: kiruvchi ulanishlar yopiq |

Sukut boʻyicha: **kiruvchi** — ruxsat berilmagan hammasi taqiqlanadi; **chiquvchi** — taqiqlanmagan hammasi ruxsat etiladi. Qoida turlari: dastur boʻyicha, port boʻyicha, oldindan belgilangan (masalan, "File and Printer Sharing"), maxsus. Qoidada: yoʻnalish, amal (Allow/Block), protokol va port, manba va qabul qiluvchi manzillar, profil. **Block qoidasi Allow dan ustun.**

Vosita: **wf.msc** (Windows Defender Firewall with Advanced Security).

## Qoidalar yaratish

```text
netsh advfirewall show allprofiles                         profillar holati
netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80
netsh advfirewall firewall add rule name="Block Telnet" dir=in action=block protocol=TCP localport=23
netsh advfirewall firewall show rule name="Web 80"
netsh advfirewall firewall delete rule name="Web 80"
```

PowerShell:

```text
Get-NetFirewallProfile | Select-Object Name, Enabled, DefaultInboundAction
New-NetFirewallRule -DisplayName "RDP faqat laboratoriyadan" -Direction Inbound -Protocol TCP -LocalPort 3389 -RemoteAddress 192.168.50.0/24 -Action Allow
New-NetFirewallRule -DisplayName "ICMP Echo" -Direction Inbound -Protocol ICMPv4 -IcmpType 8 -Action Allow
Get-NetFirewallRule -Enabled True -Direction Inbound | Measure-Object
Set-NetFirewallProfile -Profile Domain,Public,Private -LogBlocked True -LogFileName "%systemroot%\system32\LogFiles\Firewall\pfirewall.log"
```

**Sinash**: boshqa kompyuterdan `Test-NetConnection 192.168.50.10 -Port 80` (`TcpTestSucceeded : True/False`) va `ping`. **Jurnal** (`pfirewall.log`) da toʻsilgan paketlar vaqti, manbasi va porti bilan yoziladi.

## Amaliy ish 1: serverni himoyalash

1. `SRV-DC01` da `wf.msc` ni oching, uchala profil yoqilganini tekshiring.
2. IIS yoki oddiy veb-server uchun **TCP 80** ga kiruvchi ruxsat qoʻshing; mijozdan brauzer bilan tekshiring.
3. RDP (3389) ni faqat laboratoriya quyi tarmogʻidan (`192.168.50.0/24`) ruxsat qiling.
4. ICMP Echo ni yoqing va oʻchiring — mijozdagi `ping` natijasini solishtiring.
5. Toʻsilganlarni jurnalga yozishni yoqing, mijozdan yopiq portga (`Test-NetConnection -Port 23`) ulanishga urinib, jurnalda yozuvni toping.
6. Qoidalar roʻyxatini eksport qiling: `netsh advfirewall export C:\fw-backup.wfw`.

Qoidalar roʻyxatini "birinchi mos kelgan" tamoyili boʻyicha tekshiruvchi dastur — ushbu mashgʻulotning mustaqil topshirigʻi (KOT-A14).

## NAT va PAT

Uy yoki ofis routeri ichki xususiy manzillarni (192.168.x.x) bitta ommaviy manzilga almashtiradi — **PAT** (Port Address Translation, "NAT overload"):

1. Ichkaridan chiquvchi paket: manba `192.168.1.10:51000` → router uni `203.0.113.5:50000` ga almashtiradi va **NAT jadvaliga** yozadi.
2. Javob `203.0.113.5:50000` ga keladi → router jadvaldan topib, `192.168.1.10:51000` ga yetkazadi.
3. Jadvalda yozuvi yoʻq tashqi paket — **tashlanadi**: ichkaridagi hech bir kompyuter uni kutmagan.

Shuning uchun NAT ortidagi kompyuterga tashqaridan toʻgʻridan-toʻgʻri ulanib boʻlmaydi (agar **port forwarding** — statik yozuv — sozlanmagan boʻlsa). Bu himoyaning yon taʼsiri, lekin brandmauerning oʻrnini bosmaydi.

## Ishlangan misol: PAT jadvali

Birinchi satrda routerning ommaviy IP manzili va birinchi tashqi port. Keyingi satrlarda hodisalar: `OUT ichki_ip ichki_port tashqi_ip tashqi_port` (ichkaridan chiquvchi paket) yoki `IN tashqi_ip tashqi_port router_porti` (tashqaridan kelgan paket). Chiquvchi paket uchun bir xil ichki manzil va port juftiga bir xil router porti beriladi, yangisiga — navbatdagi port. Kiruvchi paketni jadval boʻyicha ichki manzilga yetkazing yoki tashlang (yozuv yoʻq yoki paket boshqa serverdan kelgan).

```python
import sys

satrlar = sys.stdin.read().strip().split("\n")
ommaviy, keyingi = satrlar[0].split()
keyingi = int(keyingi)
jadval = {}                               # (ichki_ip, ichki_port) -> router porti
teskari = {}                              # router porti -> (ichki_ip, ichki_port, server_ip, server_port)
for satr in satrlar[1:]:
    q = satr.split()
    if q[0] == "OUT":
        kalit = (q[1], q[2])
        if kalit not in jadval:
            jadval[kalit] = keyingi       # yangi yozuv
            teskari[keyingi] = (q[1], q[2], q[3], q[4])
            keyingi += 1
        port = jadval[kalit]
        print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " -> " + q[3] + ":" + q[4])
    else:
        port = int(q[3])
        yozuv = teskari.get(port)
        if yozuv is not None and yozuv[2] == q[1] and yozuv[3] == q[2]:
            print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " -> " + yozuv[0] + ":" + yozuv[1])
        else:
            print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " tashlandi")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String ommaviy = in.next();
        int keyingi = in.nextInt();
        Map<String, Integer> jadval = new HashMap<>();             // "ichki_ip ichki_port" -> router porti
        Map<Integer, String[]> teskari = new HashMap<>();          // router porti -> {ichki_ip, ichki_port, server_ip, server_port}
        while (in.hasNext()) {
            String tur = in.next();
            if (tur.equals("OUT")) {
                String ip = in.next(), ichkiPort = in.next(), server = in.next(), serverPort = in.next();
                String kalit = ip + " " + ichkiPort;
                if (!jadval.containsKey(kalit)) {
                    jadval.put(kalit, keyingi);                     // yangi yozuv
                    teskari.put(keyingi, new String[] {ip, ichkiPort, server, serverPort});
                    keyingi++;
                }
                int port = jadval.get(kalit);
                System.out.println(ip + ":" + ichkiPort + " -> " + ommaviy + ":" + port + " -> " + server + ":" + serverPort);
            } else {
                String server = in.next(), serverPort = in.next();
                int port = in.nextInt();
                String[] yozuv = teskari.get(port);
                if (yozuv != null && yozuv[2].equals(server) && yozuv[3].equals(serverPort)) {
                    System.out.println(server + ":" + serverPort + " -> " + ommaviy + ":" + port + " -> " + yozuv[0] + ":" + yozuv[1]);
                } else {
                    System.out.println(server + ":" + serverPort + " -> " + ommaviy + ":" + port + " tashlandi");
                }
            }
        }
    }
}
```
```cpp
#include <array>
#include <iostream>
#include <map>
#include <string>
#include <utility>
using namespace std;

int main() {
    string ommaviy;
    int keyingi;
    cin >> ommaviy >> keyingi;
    map<pair<string, string>, int> jadval;                         // (ichki_ip, ichki_port) -> router porti
    map<int, array<string, 4>> teskari;                            // router porti -> {ichki_ip, ichki_port, server_ip, server_port}
    string tur;
    while (cin >> tur) {
        if (tur == "OUT") {
            string ip, ichkiPort, server, serverPort;
            cin >> ip >> ichkiPort >> server >> serverPort;
            pair<string, string> kalit = {ip, ichkiPort};
            if (!jadval.count(kalit)) {
                jadval[kalit] = keyingi;                           // yangi yozuv
                teskari[keyingi] = {ip, ichkiPort, server, serverPort};
                keyingi++;
            }
            int port = jadval[kalit];
            cout << ip << ":" << ichkiPort << " -> " << ommaviy << ":" << port << " -> " << server << ":" << serverPort << endl;
        } else {
            string server, serverPort;
            int port;
            cin >> server >> serverPort >> port;
            if (teskari.count(port) && teskari[port][2] == server && teskari[port][3] == serverPort) {
                cout << server << ":" << serverPort << " -> " << ommaviy << ":" << port << " -> " << teskari[port][0] << ":" << teskari[port][1] << endl;
            } else {
                cout << server << ":" << serverPort << " -> " << ommaviy << ":" << port << " tashlandi" << endl;
            }
        }
    }
    return 0;
}
```

Kirish:

```text
203.0.113.5 50000
OUT 192.168.1.10 51000 8.8.8.8 53
OUT 192.168.1.11 51000 142.250.74.46 443
IN 8.8.8.8 53 50000
IN 142.250.74.46 443 50001
OUT 192.168.1.10 51000 8.8.8.8 53
IN 198.51.100.66 443 50001
IN 198.51.100.66 3389 50002
```

Chiqish:

```text
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
192.168.1.11:51000 -> 203.0.113.5:50001 -> 142.250.74.46:443
8.8.8.8:53 -> 203.0.113.5:50000 -> 192.168.1.10:51000
142.250.74.46:443 -> 203.0.113.5:50001 -> 192.168.1.11:51000
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
198.51.100.66:443 -> 203.0.113.5:50001 tashlandi
198.51.100.66:3389 -> 203.0.113.5:50002 tashlandi
```

Ikki kompyuter bir xil ichki port (51000) ishlatsa ham, router ularni turli tashqi portlar bilan ajratdi. Oxirgi ikki paket: biri mavjud portga, lekin **boshqa** serverdan (soxta javobga urinish), ikkinchisi umuman yozuvsiz portga (RDP ni skanerlash) — ikkalasi ham tashlandi.

## VPN ga ulanish

**Windows mijozi**: Sozlamalar → Tarmoq va Internet → **VPN** → "VPN ulanish qoʻshish": provayder — Windows (built-in), server nomi (`vpn.aifu.uz`), VPN turi (**IKEv2** — tavsiya, **SSTP** — 443-port orqali, **L2TP/IPsec**), kirish maʼlumotlari turi (login va parol, sertifikat). Boshqa variantlar — **WireGuard** yoki OpenVPN mijozlari (konfiguratsiya fayli import qilinadi).

**Server tomoni**: Windows Server da **Remote Access → RRAS** (Routing and Remote Access) roli, Linux da WireGuard yoki OpenVPN. Brandmauerda tegishli portlar ochiladi: IKEv2 — UDP 500 va 4500, SSTP — TCP 443, WireGuard — UDP 51820.

**Tekshirish**:

```text
rasdial "AIFU VPN" login parol      buyruqlar satridan ulanish
ipconfig /all                       VPN adapteri va unga berilgan ichki manzil
route print                         qaysi trafik tunnel orqali ketadi
ping 192.168.50.10                  ichki serverga yetib borish
tracert 192.168.50.10               birinchi sakrash — VPN server
rasdial "AIFU VPN" /disconnect
```

**Split tunneling**: faqat ichki tarmoq trafigi tunnel orqali, qolgani — toʻgʻridan-toʻgʻri Internetga. Tejamli, lekin xavfsizlik siyosati baʼzan barcha trafikni tunnel orqali yuborishni (full tunnel) talab qiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **"Ishlamayapti" deb brandmauerni butunlay oʻchirish.** Toʻgʻri yoʻl — aynan kerakli port uchun qoida qoʻshish.
- **Ommaviy Wi-Fi da "Private" profilni tanlash.** Fayl ulashish va kompyuterni topish begonalarga ochiladi.
- **Qoidada manba manzilini cheklamaslik.** RDP ni "Any" dan ochish oʻrniga faqat kerakli quyi tarmoqni koʻrsating.
- **NAT ni brandmauer deb hisoblash.** Port forwarding yoki UPnP ochgan portlar orqali hujum toʻgʻridan-toʻgʻri keladi.
- **PPTP VPN dan foydalanish.** Eskirgan va buzilgan protokol; IKEv2, SSTP yoki WireGuard ishlating.
:::

:::tip
Uy routeringizning boshqaruv sahifasida (faqat oʻzingizniki!) NAT jadvali yoki "Attached devices" boʻlimini toping. Kompyuterda `netstat -n` bilan ochiq ulanishlarni koʻring va ularning ichki portlari router jadvalida qaysi tashqi portga mos kelishini solishtiring (agar router buni koʻrsatsa).
:::

## Xulosa

- Windows Defender Firewall — holatni kuzatuvchi; profillar Domain, Private, Public; kiruvchi sukut boʻyicha taqiqlangan, Block Allow dan ustun.
- Qoidalar: `wf.msc`, `netsh advfirewall`, `New-NetFirewallRule`; sinash — `Test-NetConnection`, jurnal — `pfirewall.log`.
- PAT ichki manzil:port ni ommaviy manzil:port ga almashtiradi; jadvalda yoʻq kiruvchi paketlar tashlanadi.
- VPN: Windows mijozi (IKEv2, SSTP, L2TP), WireGuard, RRAS; tekshirish — `ipconfig`, `route print`, `tracert`.

## Oʻzingizni tekshiring

:::check
Q: Windows Defender Firewall ning qaysi profili eng qatʼiy va qachon ishlatiladi?
A: Public — ommaviy tarmoqlarda (kafe, aeroport Wi-Fi).
Q: 80-portga kiruvchi ruxsatni `netsh` bilan qanday qoʻshish mumkin?
A: `netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80`.
Q: Nima uchun NAT ortidagi kompyuterga tashqaridan ulanib boʻlmaydi?
A: Routerning NAT jadvalida bu ulanish uchun yozuv yoʻq — paketni qaysi ichki kompyuterga berishni bilmaydi va tashlaydi.
Q: Brandmauer qoidalarining natijasini qanday sinash mumkin?
A: Boshqa kompyuterdan `Test-NetConnection server -Port N` va brandmauer jurnalini koʻrish orqali.
Q: Split tunneling nima?
A: VPN da faqat ichki tarmoq trafigini tunnel orqali, qolganini toʻgʻridan-toʻgʻri Internetga yuborish.
:::

:::link https://learn.microsoft.com/en-us/windows/security/operating-system-security/network-security/windows-firewall/
Windows Firewall
Microsoft ning Windows brandmaueri boʻyicha rasmiy hujjatlari (ingliz tilida).
:::
