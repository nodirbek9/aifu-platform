---
title: Soketlar yordamida tarmoq dasturini yaratish
summary: Soket turlari (oqimli va datagrammali); Python va Java da TCP echo server va mijoz; dasturni telnet, nc va curl bilan sinash; koʻp mijozlarga xizmat koʻrsatish; TCP oqimida xabar chegaralari va ularni ajratish usullari (uzunlik prefiksi, ajratuvchi belgi); matn kodlash; URL tuzilishi va uni tahlil qilish; HTTP — TCP ustidagi matnli protokol.
---

## Nima uchun bu mavzu muhim?

Brauzer, messenjer, onlayn oʻyin, ushbu oʻquv platformasi — barchasi ichida **soketlar** bilan ishlaydi. M10 maʼruzasida soket API ketma-ketligini koʻrdik; bu mashgʻulotda haqiqiy server va mijoz yozamiz, ularni standart vositalar bilan sinaymiz va har bir tarmoq dasturchisi duch keladigan muammoni — **TCP oqimida xabarlar chegarasi yoʻqligini** — hal qilamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- Python yoki Java da oddiy TCP server va mijoz yoza olasiz;
- serverni `telnet`, `nc` yoki `curl` bilan sinab koʻra olasiz;
- nima uchun bitta `recv()` bitta xabar emasligini tushuntira olasiz va xabarlarni uzunlik prefiksi bilan ajrata olasiz;
- URL ni qismlarga (sxema, host, port, yoʻl, soʻrov) ajrata olasiz;
- HTTP soʻrovi soket orqali qanday koʻrinishda yuborilishini bilasiz.
:::

## Soket turlari

| Tur | Konstanta | Transport | Xususiyati |
|---|---|---|---|
| Oqimli | `SOCK_STREAM` | TCP | ulanish, ishonchli **bayt oqimi**, xabar chegaralari yoʻq |
| Datagrammali | `SOCK_DGRAM` | UDP | ulanishsiz, har bir `sendto()` — alohida xabar, yoʻqolishi mumkin |

Manzil oilasi: `AF_INET` — IPv4, `AF_INET6` — IPv6. Server odatda `0.0.0.0` (barcha interfeyslar) yoki `127.0.0.1` (faqat shu kompyuter) manziliga bogʻlanadi.

## TCP echo server va mijoz (Python)

Server — kelgan har bir maʼlumotni orqaga qaytaradi:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("127.0.0.1", 5000))
    server.listen()
    print("5000-port tinglanmoqda")
    while True:
        ulanish, manzil = server.accept()          # yangi mijoz uchun yangi soket
        with ulanish:
            print("ulandi:", manzil)
            while True:
                malumot = ulanish.recv(1024)
                if not malumot:                    # boʻsh — mijoz ulanishni yopdi
                    break
                ulanish.sendall(malumot)
```

Mijoz:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as mijoz:
    mijoz.connect(("127.0.0.1", 5000))
    mijoz.sendall("Salom, server!".encode("utf-8"))
    javob = mijoz.recv(1024)
    print("javob:", javob.decode("utf-8"))
```

Java da xuddi shu gʻoya `ServerSocket` (server) va `Socket` (mijoz) sinflari bilan yoziladi: `new ServerSocket(5000)`, `server.accept()`, `socket.getInputStream()` / `getOutputStream()`.

## Amaliy ish

1. Serverni bitta terminalda, mijozni ikkinchisida ishga tushiring. Windows brandmaueri ruxsat soʻrasa — faqat "Xususiy tarmoqlar" uchun ruxsat bering.
2. Serverni mijozsiz sinang: `telnet 127.0.0.1 5000` (Windows da Telnet mijozini yoqish kerak boʻlishi mumkin) yoki `nc 127.0.0.1 5000` — yozgan matningiz qaytib keladi.
3. `netstat -an | findstr 5000` bilan LISTEN va ESTABLISHED holatlarini koʻring.
4. Ikki mijozni bir vaqtda ulang: ikkinchisi birinchisi uzilguncha kutadi. Sabab — server bitta oqimda ishlaydi. Har bir `accept()` dan keyin yangi **thread** ochib (`threading.Thread`), bu muammoni hal qiling.
5. HTTP — TCP ustidagi oddiy matn. Buni koʻrish uchun `curl -v http://example.com` ni bajaring: `>` bilan boshlangan qatorlar — soketga yozilgan soʻrov, `<` bilan boshlanganlari — javob sarlavhalari.

```text
GET /index.html HTTP/1.1
Host: example.com
User-Agent: curl/8.0
Accept: */*

```

Soʻrov satri, sarlavhalar va **boʻsh satr** — server soʻrov tugaganini shundan biladi. Shunday soʻrovni qismlarga ajratuvchi dastur — ushbu mashgʻulotning mustaqil topshirigʻi (KOT-A9).

## Oqimda xabar chegaralari

TCP — **bayt oqimi**: yuboruvchi `send("Salom")` va `send("Hey")` qilsa, qabul qiluvchi bir `recv()` da `"SalomHey"` ni yoki `"Sal"` va `"omHey"` ni olishi mumkin. Xabarlarni ajratish uchun dasturning oʻz **protokoli** boʻlishi kerak:

- **Ajratuvchi belgi** — har bir xabar `\n` bilan tugaydi (HTTP sarlavhalari, SMTP, Redis);
- **Uzunlik prefiksi** — xabar oldidan uning uzunligi yoziladi: `5:Salom3:Hey` (ikkilik protokollar, WebSocket kadrlari);
- **Belgilangan uzunlik** — barcha xabarlar bir xil oʻlchamda.

Matn soketga baytlar sifatida yuboriladi — `encode("utf-8")` / `decode("utf-8")`. Diqqat: UTF-8 da "ʻ" kabi belgilar 2 bayt, shuning uchun uzunlik prefiksi **baytlarda** hisoblanishi kerak.

## Ishlangan misol 1: URL ni tahlil qilish

Har bir satrda URL. Uni sxema, host, port (koʻrsatilmagan boʻlsa — sxema boʻyicha standart: http 80, https 443, ftp 21), yoʻl (yoʻq boʻlsa `/`) va soʻrov qismiga (`?` dan keyingi; yoʻq boʻlsa `-`) ajrating; `#` dan keyingi qism tashlanadi. `://` boʻlmasa — `notoʻgʻri`.

```python
import sys

STANDART = {"http": "80", "https": "443", "ftp": "21"}

for url in sys.stdin.read().split():
    if "://" not in url:
        print(url, "notoʻgʻri")
        continue
    sxema, qolgan = url.split("://", 1)
    qolgan = qolgan.split("#", 1)[0]               # fragment serverga yuborilmaydi
    soruv = "-"
    if "?" in qolgan:
        qolgan, soruv = qolgan.split("?", 1)
    if "/" in qolgan:
        host_port, yol = qolgan.split("/", 1)
        yol = "/" + yol
    else:
        host_port, yol = qolgan, "/"
    if ":" in host_port:
        host, port = host_port.split(":", 1)
    else:
        host, port = host_port, STANDART.get(sxema, "-")
    if not host:
        print(url, "notoʻgʻri")
        continue
    print(sxema, host, port, yol, soruv)
```
```java
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Map<String, String> standart = Map.of("http", "80", "https", "443", "ftp", "21");
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String url = in.next();
            int ajr = url.indexOf("://");
            if (ajr < 0) {
                System.out.println(url + " notoʻgʻri");
                continue;
            }
            String sxema = url.substring(0, ajr);
            String qolgan = url.substring(ajr + 3);
            int panjara = qolgan.indexOf('#');
            if (panjara >= 0) qolgan = qolgan.substring(0, panjara);   // fragment serverga yuborilmaydi
            String soruv = "-";
            int savol = qolgan.indexOf('?');
            if (savol >= 0) {
                soruv = qolgan.substring(savol + 1);
                qolgan = qolgan.substring(0, savol);
            }
            String hostPort, yol;
            int chiziq = qolgan.indexOf('/');
            if (chiziq >= 0) {
                hostPort = qolgan.substring(0, chiziq);
                yol = qolgan.substring(chiziq);
            } else {
                hostPort = qolgan;
                yol = "/";
            }
            String host, port;
            int ikki = hostPort.indexOf(':');
            if (ikki >= 0) {
                host = hostPort.substring(0, ikki);
                port = hostPort.substring(ikki + 1);
            } else {
                host = hostPort;
                port = standart.getOrDefault(sxema, "-");
            }
            if (host.isEmpty()) {
                System.out.println(url + " notoʻgʻri");
                continue;
            }
            System.out.println(sxema + " " + host + " " + port + " " + yol + " " + soruv);
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    map<string, string> standart = {{"http", "80"}, {"https", "443"}, {"ftp", "21"}};
    string url;
    while (cin >> url) {
        size_t ajr = url.find("://");
        if (ajr == string::npos) {
            cout << url << " notoʻgʻri" << endl;
            continue;
        }
        string sxema = url.substr(0, ajr);
        string qolgan = url.substr(ajr + 3);
        size_t panjara = qolgan.find('#');
        if (panjara != string::npos) qolgan = qolgan.substr(0, panjara);   // fragment serverga yuborilmaydi
        string soruv = "-";
        size_t savol = qolgan.find('?');
        if (savol != string::npos) {
            soruv = qolgan.substr(savol + 1);
            qolgan = qolgan.substr(0, savol);
        }
        string hostPort, yol;
        size_t chiziq = qolgan.find('/');
        if (chiziq != string::npos) {
            hostPort = qolgan.substr(0, chiziq);
            yol = qolgan.substr(chiziq);
        } else {
            hostPort = qolgan;
            yol = "/";
        }
        string host, port;
        size_t ikki = hostPort.find(':');
        if (ikki != string::npos) {
            host = hostPort.substr(0, ikki);
            port = hostPort.substr(ikki + 1);
        } else {
            host = hostPort;
            port = standart.count(sxema) ? standart[sxema] : "-";
        }
        if (host.empty()) {
            cout << url << " notoʻgʻri" << endl;
            continue;
        }
        cout << sxema << " " << host << " " << port << " " << yol << " " << soruv << endl;
    }
    return 0;
}
```

Kirish:

```text
http://aifu.uz/news?id=5
https://example.com:8443/api/v1/users#top
ftp://files.aifu.uz
aifu.uz/index.html
```

Chiqish:

```text
http aifu.uz 80 /news id=5
https example.com 8443 /api/v1/users -
ftp files.aifu.uz 21 / -
aifu.uz/index.html notoʻgʻri
```

Mijoz dasturi aynan shu qismlar bilan ishlaydi: `host` — DNS dan IP olish uchun, `port` — `connect()` uchun, `yol` va `soruv` — HTTP soʻrov satri uchun. Fragment (`#top`) brauzerda qoladi va serverga yuborilmaydi.

## Ishlangan misol 2: oqimdan xabarlarni ajratish

Protokol: har bir xabar oldidan uning uzunligi va `:` yoziladi (`5:Salom`). Har bir kirish satri — bitta `recv()` chaqiruvi qaytargan qism (xabarlar qismlar orasida boʻlinib ketgan boʻlishi mumkin). Toʻliq yigʻilgan har bir xabarni chiqaring; oxirida buferda qolgan toʻliqsiz baytlar sonini chiqaring.

```python
import sys

bufer = ""
for qism in sys.stdin.read().split("\n"):
    bufer += qism                                  # recv() qaytargan baytlar
    while ":" in bufer:
        uzunlik = int(bufer[:bufer.index(":")])
        boshi = bufer.index(":") + 1
        if len(bufer) - boshi < uzunlik:
            break                                  # xabar hali toʻliq kelmagan
        print("xabar: " + bufer[boshi:boshi + uzunlik])
        bufer = bufer[boshi + uzunlik:]
print("qoldiq: " + str(len(bufer)) + " bayt")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder bufer = new StringBuilder();
        while (in.hasNextLine()) {
            bufer.append(in.nextLine());                         // recv() qaytargan baytlar
            while (bufer.indexOf(":") >= 0) {
                int ikki = bufer.indexOf(":");
                int uzunlik = Integer.parseInt(bufer.substring(0, ikki));
                int boshi = ikki + 1;
                if (bufer.length() - boshi < uzunlik) {
                    break;                                       // xabar hali toʻliq kelmagan
                }
                System.out.println("xabar: " + bufer.substring(boshi, boshi + uzunlik));
                bufer.delete(0, boshi + uzunlik);
            }
        }
        System.out.println("qoldiq: " + bufer.length() + " bayt");
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string bufer, qism;
    while (getline(cin, qism)) {
        bufer += qism;                                           // recv() qaytargan baytlar
        while (bufer.find(':') != string::npos) {
            size_t ikki = bufer.find(':');
            size_t uzunlik = stoul(bufer.substr(0, ikki));
            size_t boshi = ikki + 1;
            if (bufer.size() - boshi < uzunlik) {
                break;                                           // xabar hali toʻliq kelmagan
            }
            cout << "xabar: " << bufer.substr(boshi, uzunlik) << endl;
            bufer.erase(0, boshi + uzunlik);
        }
    }
    cout << "qoldiq: " << bufer.size() << " bayt" << endl;
    return 0;
}
```

Kirish (uchta `recv()` natijasi):

```text
5:Salo
m3:Hey16:Kompyuter
_tarmoq2:o
```

Chiqish:

```text
xabar: Salom
xabar: Hey
xabar: Kompyuter_tarmoq
qoldiq: 3 bayt
```

Birinchi `recv()` da "Salom" toʻliq kelmagan — dastur kutadi. Ikkinchisida birdaniga bir yarim xabar keldi. Oxirgi `2:o` — keyingi `recv()` ni kutayotgan xabar boshi. Shu tarzda ishlamagan dastur sinovda (localhost da) ishlaydi, lekin real tarmoqda tasodifiy xatolar beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Bitta `recv()` bitta xabar deb hisoblash.** TCP chegaralarni saqlamaydi — protokol (uzunlik yoki ajratuvchi) kerak.
- **`send()` hamma maʼlumotni yuboradi deb oʻylash.** `send()` qisman yuborishi mumkin; Python da `sendall()` ishlating.
- **Satr va baytlarni chalkashtirish.** Soketga baytlar yoziladi; uzunlik prefiksi UTF-8 baytlarida hisoblanadi.
- **Serverni qayta ishga tushirganda "Address already in use".** Oldingi ulanish TIME_WAIT da; `SO_REUSEADDR` opsiyasini qoʻying.
- **Serverni `0.0.0.0` ga ochib qoʻyish.** Sinov serveri tarmoqdagi hammaga ochiq boʻladi; mashqlar uchun `127.0.0.1` dan foydalaning.
:::

:::tip
Echo serverni chat serverga aylantiring: har bir mijoz uchun alohida thread oching, kelgan xabarni **barcha** ulangan mijozlarga yuboring, xabarlarni `\n` bilan ajrating. Ikki-uch terminalda `nc 127.0.0.1 5000` bilan sinab koʻring.
:::

## Xulosa

- `SOCK_STREAM` — TCP (ishonchli bayt oqimi), `SOCK_DGRAM` — UDP (alohida datagrammalar).
- Server: socket → bind → listen → accept → recv/sendall → close; mijoz: socket → connect → sendall/recv → close.
- Serverni `telnet`, `nc`, `curl -v` bilan sinash mumkin; koʻp mijoz uchun thread lar kerak.
- TCP da xabar chegarasi yoʻq — uzunlik prefiksi yoki ajratuvchi belgi bilan protokol tuziladi.
- URL: sxema://host:port/yoʻl?soʻrov#fragment; HTTP — soket orqali yuboriladigan matnli soʻrov.

## Oʻzingizni tekshiring

:::check
Q: `SOCK_STREAM` va `SOCK_DGRAM` soketlarining farqi nimada?
A: Birinchisi TCP — ulanishli ishonchli bayt oqimi, ikkinchisi UDP — ulanishsiz alohida datagrammalar.
Q: Mijoz ikkita `send()` qildi. Server ikkita `recv()` da aynan ikkita xabarni oladimi?
A: Kafolat yoʻq: TCP bayt oqimi, ular birlashib yoki boʻlinib kelishi mumkin.
Q: `https://aifu.uz/login` uchun mijoz qaysi portga ulanadi?
A: 443 — https sxemasining standart porti.
Q: Server soketida `recv()` boʻsh natija qaytarsa, bu nimani bildiradi?
A: Mijoz ulanishni yopgan.
Q: HTTP serveri soʻrov sarlavhalari tugaganini qanday biladi?
A: Boʻsh satrdan (`\r\n\r\n`).
:::

:::link https://docs.python.org/3/howto/sockets.html
Python: Socket Programming HOWTO
Soketlar bilan ishlash boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
