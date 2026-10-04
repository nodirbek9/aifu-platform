---
title: Transport pogʻonasining vazifalari. TCP va UDP protokollari. Soketlar tushunchasi
summary: Transport pogʻonasi vazifalari — jarayonlar orasida yetkazish, multiplekslash, portlar; UDP — sarlavhasi va qoʻllanishi; TCP — sarlavha maydonlari, uch bosqichli ulanish, tartib raqamlari va tasdiqlar, qayta yuborish, sirpanuvchi oyna, oqim va tirbandlikni boshqarish, ulanishni yopish va holatlar; TCP va UDP ni solishtirish; soket tushunchasi va soket API ketma-ketligi.
---

## Nima uchun bu mavzu muhim?

IP paketni **kompyuterga** yetkazadi. Lekin kompyuterda bir vaqtda brauzer, messenjer, musiqa ilovasi va yangilanish xizmati ishlaydi — kelgan maʼlumot **qaysi dasturga** tegishli? Bundan tashqari, IP paket yoʻqolishi yoki tartibi buzilishi mumkin, fayl esa bayt-baytigacha toʻgʻri kelishi shart. Bu ikki muammoni **transport pogʻonasi** hal qiladi: **portlar** dasturni aniqlaydi, **TCP** esa ishonchli yetkazishni taʼminlaydi. Har bir tarmoq dasturi transport pogʻonasiga **soket** orqali murojaat qiladi.

:::note
**Bu darsdan soʻng siz:**

- transport pogʻonasi vazifalarini va portlar maʼnosini tushuntira olasiz;
- TCP va UDP ni solishtirib, qaysi vazifaga qaysi biri mosligini asoslay olasiz;
- uch bosqichli ulanish va tartib/tasdiq raqamlarini qadamma-qadam hisoblay olasiz;
- sirpanuvchi oyna tezlikni qanday cheklashini hisoblay olasiz;
- soket tushunchasini va server hamda mijoz soket chaqiruvlari ketma-ketligini ayta olasiz.
:::

## Transport pogʻonasi vazifalari

| Vazifa | Maʼnosi |
|---|---|
| **Jarayonlar orasida yetkazish** | IP — kompyuterdan kompyuterga, transport — dasturdan dasturga |
| **Multiplekslash** | koʻp dasturlar bitta IP manzildan bir vaqtda foydalanadi; ular **port** raqami bilan ajratiladi |
| **Segmentlash** | katta maʼlumot oqimini segmentlarga boʻlish va qayta yigʻish |
| **Ishonchlilik** (TCP) | yoʻqolganini qayta yuborish, takrorlarni olib tashlash, tartibni tiklash |
| **Oqimni boshqarish** (TCP) | tez yuboruvchi sekin qabul qiluvchini "bosib" qoʻymasligi |
| **Tirbandlikni boshqarish** (TCP) | tarmoq yuklanganda tezlikni kamaytirish |

**Port** — 16 bitli raqam (0–65 535): 0–1023 — **taniqli** (well-known) portlar, 1024–49 151 — roʻyxatdan oʻtgan, 49 152–65 535 — **vaqtinchalik** (ephemeral) portlar — mijoz ulanishlari uchun OS avtomatik beradi.

| Port | Protokol | Port | Protokol |
|---|---|---|---|
| 20, 21 | FTP | 80 | HTTP |
| 22 | SSH | 110 / 143 | POP3 / IMAP |
| 23 | Telnet | 443 | HTTPS |
| 25 | SMTP | 3389 | RDP |
| 53 | DNS (UDP va TCP) | 67, 68 | DHCP (UDP) |

Har bir TCP ulanish **beshlik** (5-tuple) bilan aniqlanadi: protokol, manba IP, manba port, qabul qiluvchi IP, qabul qiluvchi port. Shuning uchun bitta veb-serverning 443-portiga minglab mijoz bir vaqtda ulana oladi — ularning IP yoki porti farq qiladi.

## UDP

**UDP** (User Datagram Protocol) — minimal transport: sarlavhasi atigi **8 bayt** (manba port, qabul qiluvchi port, uzunlik, nazorat yigʻindisi). Ulanish oʻrnatilmaydi, tasdiq va qayta yuborish yoʻq, tartib kafolatlanmaydi. Afzalligi — **tezlik va kam kechikish**. Qoʻllanishi: DNS soʻrovlari, onlayn oʻyinlar, ovozli va video qoʻngʻiroqlar (kechikkan paketdan koʻra yoʻqolgani yaxshi), DHCP, IPTV multicast. Zamonaviy **QUIC** (HTTP/3) ishonchlilikni UDP ustiga oʻzi quradi.

## TCP

**TCP** (Transmission Control Protocol) — ishonchli, tartibli, ulanishga asoslangan **bayt oqimi**. Sarlavhasi kamida **20 bayt**:

| Maydon | Maʼnosi |
|---|---|
| Manba va qabul qiluvchi port | 16 + 16 bit |
| **Tartib raqami** (Sequence) | segmentdagi birinchi baytning oqimdagi raqami |
| **Tasdiq raqami** (Acknowledgment) | qabul qiluvchi **kutayotgan keyingi** bayt raqami |
| Sarlavha uzunligi | 4 baytli soʻzlarda |
| Bayroqlar | **SYN** (ulanish), **ACK** (tasdiq maydoni toʻgʻri), **FIN** (yopish), **RST** (uzish), PSH, URG |
| **Oyna** (Window) | qabul qiluvchi yana qancha bayt qabul qila oladi (oqimni boshqarish) |
| Nazorat yigʻindisi | sarlavha va maʼlumot uchun |

### Uch bosqichli ulanish (three-way handshake)

1. Mijoz → server: **SYN**, seq = *x* (tasodifiy boshlangʻich raqam, ISN).
2. Server → mijoz: **SYN+ACK**, seq = *y*, ack = *x* + 1.
3. Mijoz → server: **ACK**, seq = *x* + 1, ack = *y* + 1.

SYN va FIN bayroqlari bittadan "virtual bayt" egallaydi — shuning uchun tasdiq +1. Shundan soʻng maʼlumot uzatiladi: *L* baytli segment seq = *s* bilan kelsa, qabul qiluvchi ack = *s* + *L* deb javob beradi.

**Yopish** — toʻrt bosqichda (har bir tomon FIN yuboradi va ikkinchisi ACK qaytaradi). Faol yopgan tomon **TIME_WAIT** holatida 2×MSL kutadi — kechikkan segmentlar yangi ulanishga aralashmasligi uchun. Asosiy holatlar: LISTEN, SYN_SENT, SYN_RECEIVED, ESTABLISHED, FIN_WAIT, CLOSE_WAIT, TIME_WAIT, CLOSED (`netstat -an` da koʻrinadi).

### Ishonchlilik, oyna va tirbandlik

- **Qayta yuborish**: tasdiq taymer (RTO) tugaguncha kelmasa, segment qayta yuboriladi; uchta takroriy ACK kelsa — darhol (**fast retransmit**).
- **Sirpanuvchi oyna**: yuboruvchi tasdiq kutmasdan oyna hajmicha baytni yuborishi mumkin. Shuning uchun bitta ulanish tezligi ≤ oyna / RTT. Tarmoqni toʻliq yuklash uchun oyna kamida **BDP** (bandwidth-delay product = tezlik × RTT) boʻlishi kerak; 64 KB dan katta oynalar uchun **window scaling** opsiyasi bor.
- **Tirbandlikni boshqarish**: yuboruvchi qoʻshimcha **cwnd** oynasini yuritadi — **slow start** (har RTT da ikki baravar), soʻng **AIMD** (har RTT da +1 segment, yoʻqotishda yarmiga). Haqiqiy oyna = min(cwnd, qabul qiluvchi oynasi).

## TCP va UDP

| | TCP | UDP |
|---|---|---|
| Ulanish | bor (handshake) | yoʻq |
| Ishonchlilik | tasdiq va qayta yuborish | yoʻq |
| Tartib | tiklanadi | kafolatlanmaydi |
| Sarlavha | 20–60 bayt | 8 bayt |
| Oqim/tirbandlik boshqaruvi | bor | yoʻq |
| Maʼlumot | bayt oqimi | alohida xabarlar (datagrammalar) |
| Qoʻllanishi | veb, pochta, fayl, SSH | DNS, ovoz/video, oʻyinlar, DHCP |

## Soketlar

**Soket** — dastur va transport pogʻonasi orasidagi **dasturiy interfeys** (API); tarmoq ulanishining dasturdagi "uchi". U IP manzil, port va protokol bilan aniqlanadi. Soket API 1983-yilda BSD Unix da paydo boʻlgan va hozir barcha tillarda deyarli bir xil:

```text
TCP server                         TCP mijoz
socket()   soket yaratish          socket()
bind()     IP:port ga bogʻlash
listen()   ulanishlarni kutish
accept()   ulanishni qabul qilish  <—  connect()   (uch bosqichli ulanish)
recv()/send()                      send()/recv()
close()                            close()

UDP: socket() → bind() (server) → sendto()/recvfrom() → close()   (ulanishsiz)
```

`accept()` har bir mijoz uchun **yangi** soket qaytaradi, tinglovchi soket esa keyingi mijozlarni kutishda davom etadi. Soketlar bilan amaliy dastur — A9 mashgʻulotida.

## Ishlangan misol 1: tartib va tasdiq raqamlari

Birinchi satrda mijoz va serverning boshlangʻich tartib raqamlari (ISN). Keyingi satrlarda kim yuborayotgani (`C` — mijoz, `S` — server) va maʼlumot uzunligi. Uch bosqichli ulanishni, soʻng har bir maʼlumot segmentini va unga qarshi tomonning ACK javobini chiqaring. Kirish fayl oxirigacha oʻqiladi.

```python
import sys

satrlar = sys.stdin.read().split("\n")
c_seq, s_seq = map(int, satrlar[0].split())
print("C->S SYN seq=" + str(c_seq))
print("S->C SYN+ACK seq=" + str(s_seq) + " ack=" + str(c_seq + 1))
c_seq += 1                                   # SYN bitta raqam egallaydi
s_seq += 1
print("C->S ACK seq=" + str(c_seq) + " ack=" + str(s_seq))
for satr in satrlar[1:]:
    if not satr.strip():
        continue
    kim, uzunlik = satr.split()
    uzunlik = int(uzunlik)
    if kim == "C":
        print("C->S seq=" + str(c_seq) + " ack=" + str(s_seq) + " len=" + str(uzunlik))
        c_seq += uzunlik
        print("S->C ACK seq=" + str(s_seq) + " ack=" + str(c_seq))   # keyingi kutilayotgan bayt
    else:
        print("S->C seq=" + str(s_seq) + " ack=" + str(c_seq) + " len=" + str(uzunlik))
        s_seq += uzunlik
        print("C->S ACK seq=" + str(c_seq) + " ack=" + str(s_seq))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long cSeq = in.nextLong();
        long sSeq = in.nextLong();
        System.out.println("C->S SYN seq=" + cSeq);
        System.out.println("S->C SYN+ACK seq=" + sSeq + " ack=" + (cSeq + 1));
        cSeq += 1;                                            // SYN bitta raqam egallaydi
        sSeq += 1;
        System.out.println("C->S ACK seq=" + cSeq + " ack=" + sSeq);
        while (in.hasNext()) {
            String kim = in.next();
            long uzunlik = in.nextLong();
            if (kim.equals("C")) {
                System.out.println("C->S seq=" + cSeq + " ack=" + sSeq + " len=" + uzunlik);
                cSeq += uzunlik;
                System.out.println("S->C ACK seq=" + sSeq + " ack=" + cSeq);   // keyingi kutilayotgan bayt
            } else {
                System.out.println("S->C seq=" + sSeq + " ack=" + cSeq + " len=" + uzunlik);
                sSeq += uzunlik;
                System.out.println("C->S ACK seq=" + cSeq + " ack=" + sSeq);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    long long cSeq, sSeq;
    cin >> cSeq >> sSeq;
    cout << "C->S SYN seq=" << cSeq << endl;
    cout << "S->C SYN+ACK seq=" << sSeq << " ack=" << cSeq + 1 << endl;
    cSeq += 1;                                                // SYN bitta raqam egallaydi
    sSeq += 1;
    cout << "C->S ACK seq=" << cSeq << " ack=" << sSeq << endl;
    string kim;
    long long uzunlik;
    while (cin >> kim >> uzunlik) {
        if (kim == "C") {
            cout << "C->S seq=" << cSeq << " ack=" << sSeq << " len=" << uzunlik << endl;
            cSeq += uzunlik;
            cout << "S->C ACK seq=" << sSeq << " ack=" << cSeq << endl;   // keyingi kutilayotgan bayt
        } else {
            cout << "S->C seq=" << sSeq << " ack=" << cSeq << " len=" << uzunlik << endl;
            sSeq += uzunlik;
            cout << "C->S ACK seq=" << cSeq << " ack=" << sSeq << endl;
        }
    }
    return 0;
}
```

Kirish (mijoz 100 baytlik soʻrov yuboradi, server 1460 va 540 baytlik ikki segmentda javob beradi):

```text
1000 5000
C 100
S 1460
S 540
```

Chiqish:

```text
C->S SYN seq=1000
S->C SYN+ACK seq=5000 ack=1001
C->S ACK seq=1001 ack=5001
C->S seq=1001 ack=5001 len=100
S->C ACK seq=5001 ack=1101
S->C seq=5001 ack=1101 len=1460
C->S ACK seq=1101 ack=6461
S->C seq=6461 ack=1101 len=540
C->S ACK seq=1101 ack=7001
```

Wireshark xuddi shu raqamlarni koʻrsatadi, lekin odatda **nisbiy** koʻrinishda (ISN = 0 deb). Haqiqiy ISN tasodifiy tanlanadi — hujumchi soxta segmentni "toʻgʻri" raqam bilan qoʻshib yubora olmasligi uchun.

## Ishlangan misol 2: oyna va tezlik chegarasi

Har bir satrda TCP oynasi (bayt), RTT (ms) va kanal tezligi (Mbit/s). Bitta ulanishning eng katta tezligini (Mbit/s, ikki xona; oyna/RTT va kanal tezligining kichigi) va kanalni toʻliq yuklash uchun kerakli oyna — BDP ni (bayt) chiqaring.

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    oyna, rtt, kanal = map(int, satr.split())
    oyna_tezligi = oyna * 8 / (rtt / 1000) / 1_000_000    # Mbit/s
    tezlik = min(oyna_tezligi, kanal)
    bdp = kanal * rtt * 125                               # Mbit/s * ms -> bayt
    print(f"{tezlik:.2f} Mbit/s BDP={bdp}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long oyna = in.nextLong();
            long rtt = in.nextLong();
            long kanal = in.nextLong();
            double oynaTezligi = oyna * 8 / (rtt / 1000.0) / 1_000_000;   // Mbit/s
            double tezlik = Math.min(oynaTezligi, kanal);
            long bdp = kanal * rtt * 125;                                // Mbit/s * ms -> bayt
            System.out.println(String.format("%.2f", tezlik) + " Mbit/s BDP=" + bdp);
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    long long oyna, rtt, kanal;
    while (cin >> oyna >> rtt >> kanal) {
        double oynaTezligi = oyna * 8 / (rtt / 1000.0) / 1000000;      // Mbit/s
        double tezlik = min(oynaTezligi, (double) kanal);
        long long bdp = kanal * rtt * 125;                             // Mbit/s * ms -> bayt
        printf("%.2f Mbit/s BDP=%lld\n", tezlik, bdp);
    }
    return 0;
}
```

Kirish:

```text
65535 50 100
1048576 50 100
65535 1 1000
```

Chiqish:

```text
10.49 Mbit/s BDP=625000
100.00 Mbit/s BDP=625000
524.28 Mbit/s BDP=125000
```

Birinchi qator — klassik muammo: 100 Mbit/s kanal, lekin 64 KB oyna va 50 ms RTT bilan ulanish atigi ~10 Mbit/s oladi; 625 000 baytli (≈ 610 KB) oyna kerak. Shuning uchun uzoq masofadagi serverdan yuklab olish tez kanalda ham sekin boʻlishi mumkin — window scaling yoqilmagan boʻlsa.

:::warning
**Koʻp uchraydigan xatolar**

- **Tasdiq raqami — oxirgi qabul qilingan bayt deb oʻylash.** ACK — **keyingi kutilayotgan** bayt raqami.
- **SYN va FIN ni "boʻsh" deb hisoblash.** Ular bittadan tartib raqami egallaydi — shuning uchun handshake dan keyin seq + 1.
- **UDP ni "yomon TCP" deb tushunish.** Real vaqt ilovalari uchun qayta yuborish zarar: kechikkan ovoz paketi keraksiz.
- **Port va soketni bir narsa deb hisoblash.** Port — raqam; soket — IP, port va protokol bilan aniqlangan dasturiy ulanish nuqtasi.
- **Tezlik faqat kanalga bogʻliq deb oʻylash.** Bitta TCP ulanish oyna/RTT dan tez ishlay olmaydi.
:::

:::tip
Brauzerda bir nechta saytni oching va buyruqlar satrida `netstat -an | findstr ESTABLISHED` (Linux: `ss -tn`) ni bajaring: har bir qatorda mahalliy IP:port (vaqtinchalik port) va masofaviy IP:443 koʻrinadi. Bu — sizning ochiq TCP ulanishlaringiz.
:::

## Xulosa

- Transport pogʻonasi dasturdan dasturga yetkazadi; dasturlar portlar bilan ajratiladi; ulanish 5 talik bilan aniqlanadi.
- UDP: 8 baytli sarlavha, ulanishsiz, kafolatsiz, tez — DNS, ovoz, oʻyinlar.
- TCP: uch bosqichli ulanish, seq/ack, qayta yuborish, sirpanuvchi oyna, oqim va tirbandlik boshqaruvi, toʻrt bosqichli yopish.
- Bitta TCP ulanish tezligi ≤ oyna / RTT; kanalni toʻliq yuklash uchun oyna ≥ BDP.
- Soket — dasturning tarmoqqa kirish nuqtasi: server socket–bind–listen–accept, mijoz socket–connect.

## Oʻzingizni tekshiring

:::check
Q: Mijoz SYN da seq = 300 yubordi. Server SYN+ACK da qanday ack qaytaradi?
A: ack = 301 — SYN bitta tartib raqami egallaydi.
Q: seq = 2000, uzunligi 500 bayt boʻlgan segment qabul qilindi. Javob ACK da tasdiq raqami qancha?
A: 2500 — keyingi kutilayotgan bayt.
Q: Nima uchun DNS soʻrovlari odatda UDP orqali yuboriladi?
A: Soʻrov va javob kichik, ulanish oʻrnatish ortiqcha kechikish qoʻshardi; yoʻqolsa, mijoz soʻrovni oʻzi takrorlaydi.
Q: Oyna 32 KB, RTT 100 ms. Bitta ulanishning eng katta tezligi qancha?
A: 32 768 × 8 / 0,1 ≈ 2,6 Mbit/s.
Q: Serverda `accept()` nima qaytaradi?
A: Qabul qilingan mijoz bilan ishlash uchun yangi soket; tinglovchi soket keyingi ulanishlarni kutishda qoladi.
:::

:::link https://www.rfc-editor.org/rfc/rfc9293
RFC 9293: Transmission Control Protocol
TCP protokolining joriy spetsifikatsiyasi (ingliz tilida).
:::
