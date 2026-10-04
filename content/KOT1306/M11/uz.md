---
title: Amaliy pogʻona protokollari va xizmatlari. Veb server xizmati. HTTP, FTP, elektron pochta protokollari
summary: Amaliy pogʻona va mijoz–server modeli; HTTP — soʻrov va javob tuzilishi, metodlar, holat kodlari, sarlavhalar, cookie, HTTP/1.1, HTTP/2, HTTP/3, HTTPS; veb serverlar va virtual hostlar; FTP — boshqaruv va maʼlumot ulanishlari, faol va passiv rejim, SFTP; elektron pochta — MUA, MTA, MDA, SMTP, POP3, IMAP, xat formati, MIME va Base64.
---

## Nima uchun bu mavzu muhim?

Foydalanuvchi uchun "Internet" — bu veb-sahifalar, pochta va fayllar. Ularning barchasi **amaliy pogʻona** protokollari orqali ishlaydi. Pastki pogʻonalar paketni yetkazadi, lekin "sahifa topilmadi" (404) yoki "xat qabul qilinmadi" (550) kabi xatolarning maʼnosini, sayt nega ochilmayotganini yoki xat nega spamga tushayotganini tushunish uchun HTTP va SMTP qanday "gaplashishini" bilish kerak.

:::note
**Bu darsdan soʻng siz:**

- HTTP soʻrovi va javobining tuzilishini, asosiy metodlar va holat kodlarini tushuntira olasiz;
- veb server vazifasini va HTTP versiyalari hamda HTTPS farqlarini ayta olasiz;
- FTP ning ikki ulanishli ishlashini va faol/passiv rejimlarni tushuntira olasiz;
- elektron pochta yoʻlini (MUA → MTA → MDA) va SMTP, POP3, IMAP vazifalarini ayta olasiz;
- SMTP dialogini oʻqiy olasiz va Base64 kodlash qanday ishlashini koʻrsata olasiz.
:::

## Amaliy pogʻona protokollari

| Protokol | Port | Transport | Vazifasi |
|---|---|---|---|
| HTTP / HTTPS | 80 / 443 | TCP (HTTP/3 — UDP/QUIC) | veb-sahifalar va API |
| FTP | 21 (boshqaruv), 20 (maʼlumot) | TCP | fayl almashish |
| SFTP / SSH | 22 | TCP | xavfsiz masofaviy kirish va fayllar |
| SMTP | 25, 587 | TCP | pochta yuborish |
| POP3 / POP3S | 110 / 995 | TCP | pochtani yuklab olish |
| IMAP / IMAPS | 143 / 993 | TCP | pochtani serverda boshqarish |
| DNS | 53 | UDP, TCP | nomlarni manzilga oʻgirish (M12) |
| DHCP | 67, 68 | UDP | manzil berish (M9) |

Koʻpchiligi **mijoz–server** modelida ishlaydi va **matnli** buyruqlar bilan "gaplashadi" — ularni `telnet` yoki `nc` bilan qoʻlda ham sinab koʻrish mumkin.

## HTTP

**Soʻrov** (request):

```text
GET /kurslar?semestr=1 HTTP/1.1          soʻrov satri: metod, yoʻl, versiya
Host: aifu.uz                            sarlavhalar
User-Agent: Mozilla/5.0
Accept-Language: uz
Cookie: sessiya=abc123
                                         boʻsh satr
                                         (POST da bu yerda tana boʻladi)
```

**Javob** (response):

```text
HTTP/1.1 200 OK                          holat satri
Content-Type: text/html; charset=utf-8
Content-Length: 5120
Set-Cookie: sessiya=abc123; HttpOnly; Secure

<!doctype html> ...                      tana
```

| Metod | Vazifasi | Xavfsiz / idempotent |
|---|---|---|
| **GET** | resursni olish | ha / ha |
| **POST** | maʼlumot yuborish, yangi resurs yaratish | yoʻq / yoʻq |
| **PUT** | resursni toʻliq almashtirish | yoʻq / ha |
| **PATCH** | qisman oʻzgartirish | yoʻq / yoʻq |
| **DELETE** | oʻchirish | yoʻq / ha |
| **HEAD** | faqat sarlavhalar (tanasiz GET) | ha / ha |

| Holat kodi | Sinf | Misollar |
|---|---|---|
| 1xx | axborot | 101 Switching Protocols (WebSocket) |
| **2xx** | muvaffaqiyat | 200 OK, 201 Created, 204 No Content |
| **3xx** | qayta yoʻnaltirish | 301 Moved Permanently, 302 Found, 304 Not Modified |
| **4xx** | mijoz xatosi | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| **5xx** | server xatosi | 500 Internal Server Error, 502 Bad Gateway, 503 Service Unavailable |

HTTP **holatsiz** (stateless): har bir soʻrov alohida. Foydalanuvchini "eslab qolish" uchun **cookie** ishlatiladi — server `Set-Cookie` bilan beradi, brauzer keyingi soʻrovlarda `Cookie` sarlavhasida qaytaradi.

**Versiyalar**: HTTP/1.0 — har bir soʻrovga yangi TCP ulanish; **HTTP/1.1** — doimiy ulanish (keep-alive), `Host` sarlavhasi majburiy (bitta IP da koʻp sayt); **HTTP/2** — ikkilik kadrlar, bitta ulanishda parallel oqimlar, sarlavhalarni siqish; **HTTP/3** — UDP ustidagi QUIC, tezroq ulanish. **HTTPS** = HTTP + **TLS** shifrlash; sertifikat server haqiqiyligini tasdiqlaydi.

## Veb server

**Veb server** (Apache, **Nginx**, IIS) 80/443-portlarni tinglaydi, soʻrovni qabul qiladi va statik fayl qaytaradi yoki soʻrovni ilovaga (Python, Java, PHP) uzatadi. **Virtual hostlar**: bitta serverda bir nechta sayt — qaysi sayt soʻralgani `Host` sarlavhasidan aniqlanadi. **Teskari proksi** (reverse proxy): Nginx tashqi soʻrovlarni ichki ilova serverlariga taqsimlaydi va TLS ni oʻzi bajaradi — ushbu oʻquv platformasi ham shunday tuzilgan.

## FTP

**FTP** (File Transfer Protocol) **ikkita** TCP ulanish ishlatadi: **boshqaruv** (21-port; `USER`, `PASS`, `LIST`, `RETR`, `STOR` buyruqlari) va har bir fayl uchun alohida **maʼlumot** ulanishi.

- **Faol rejim**: server mijozga 20-portdan ulanadi — mijoz tomonidagi brandmauer va NAT buni koʻpincha toʻsadi.
- **Passiv rejim** (`PASV`): server port ochadi, mijoz unga ulanadi — bugun odatiy.

FTP parolni **ochiq** yuboradi. Xavfsiz alternativalar: **SFTP** (SSH ustida, 22-port) va **FTPS** (FTP + TLS).

## Elektron pochta

```text
Yuboruvchi                                                       Qabul qiluvchi
[MUA: Outlook] --SMTP 587--> [MTA: smtp.aifu.uz] --SMTP 25--> [MTA: mx.gmail.com] --> [MDA: pochta qutisi]
                                                                                           |
                                                     [MUA: telefon] <--IMAP 993 / POP3 995--
```

- **MUA** (Mail User Agent) — pochta dasturi; **MTA** (Mail Transfer Agent) — xatni serverdan serverga uzatadi; **MDA** — xatni pochta qutisiga joylaydi.
- Qabul qiluvchi server manzili DNS dagi **MX** yozuvidan topiladi.
- **POP3** — xatlarni yuklab oladi (odatda serverdan oʻchiradi); **IMAP** — xatlar serverda qoladi, papkalar va bir nechta qurilma bilan ishlash qulay.
- Xat formati: sarlavhalar (`From`, `To`, `Subject`, `Date`), boʻsh satr, tana. Rasm va fayllar **MIME** yordamida qoʻshiladi va **Base64** bilan matnga aylantiriladi, chunki SMTP dastlab faqat 7 bitli matnni tashigan.
- Soxtalashtirishga qarshi: **SPF** (qaysi serverlar yubora oladi), **DKIM** (xatning raqamli imzosi), **DMARC** (siyosat).

## Ishlangan misol 1: SMTP dialogi

Birinchi satrda yuboruvchi manzili, ikkinchisida qabul qiluvchilar soni *n*, keyin *n* ta manzil, soʻng mavzu va xat matni (fayl oxirigacha). Mijoz (`C:`) va server (`S:`) orasidagi SMTP dialogini chiqaring. `@` siz manzilga server `550` bilan javob beradi. Toʻgʻri qabul qiluvchi boʻlmasa, `DATA` yuborilmaydi. Matnda `.` bilan boshlangan qatorga yana bitta `.` qoʻshiladi (dot-stuffing), xat oxiri — yolgʻiz `.`.

```python
import sys

satrlar = sys.stdin.read().split("\n")
yuboruvchi = satrlar[0].strip()
n = int(satrlar[1])
qabul = [s.strip() for s in satrlar[2:2 + n]]
mavzu = satrlar[2 + n].strip()
matn = satrlar[3 + n:]
while matn and matn[-1] == "":
    matn.pop()


def mijoz(s):
    print("C:" + (" " + s if s else ""))


print("S: 220 mail.aifu.uz ESMTP")
mijoz("HELO client.aifu.uz")
print("S: 250 mail.aifu.uz")
mijoz("MAIL FROM:<" + yuboruvchi + ">")
print("S: 250 OK")
togrilar = 0
for manzil in qabul:
    mijoz("RCPT TO:<" + manzil + ">")
    if "@" in manzil:
        print("S: 250 OK")
        togrilar += 1
    else:
        print("S: 550 No such user")
if togrilar > 0:
    mijoz("DATA")
    print("S: 354 End data with <CR><LF>.<CR><LF>")
    mijoz("Subject: " + mavzu)
    mijoz("")                                  # sarlavhalar va tana orasidagi boʻsh satr
    for s in matn:
        mijoz("." + s if s.startswith(".") else s)   # dot-stuffing
    mijoz(".")
    print("S: 250 OK: queued")
mijoz("QUIT")
print("S: 221 Bye")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static void mijoz(String s) {
        System.out.println("C:" + (s.isEmpty() ? "" : " " + s));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String yuboruvchi = in.nextLine().trim();
        int n = Integer.parseInt(in.nextLine().trim());
        List<String> qabul = new ArrayList<>();
        for (int i = 0; i < n; i++) qabul.add(in.nextLine().trim());
        String mavzu = in.nextLine().trim();
        List<String> matn = new ArrayList<>();
        while (in.hasNextLine()) matn.add(in.nextLine());
        while (!matn.isEmpty() && matn.get(matn.size() - 1).isEmpty()) matn.remove(matn.size() - 1);

        System.out.println("S: 220 mail.aifu.uz ESMTP");
        mijoz("HELO client.aifu.uz");
        System.out.println("S: 250 mail.aifu.uz");
        mijoz("MAIL FROM:<" + yuboruvchi + ">");
        System.out.println("S: 250 OK");
        int togrilar = 0;
        for (String manzil : qabul) {
            mijoz("RCPT TO:<" + manzil + ">");
            if (manzil.contains("@")) {
                System.out.println("S: 250 OK");
                togrilar++;
            } else {
                System.out.println("S: 550 No such user");
            }
        }
        if (togrilar > 0) {
            mijoz("DATA");
            System.out.println("S: 354 End data with <CR><LF>.<CR><LF>");
            mijoz("Subject: " + mavzu);
            mijoz("");                                         // sarlavhalar va tana orasidagi boʻsh satr
            for (String s : matn) {
                mijoz(s.startsWith(".") ? "." + s : s);        // dot-stuffing
            }
            mijoz(".");
            System.out.println("S: 250 OK: queued");
        }
        mijoz("QUIT");
        System.out.println("S: 221 Bye");
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

void mijoz(const string& s) {
    cout << "C:" << (s.empty() ? "" : " " + s) << endl;
}

string tozala(string s) {
    while (!s.empty() && (s.back() == ' ' || s.back() == '\r')) s.pop_back();
    return s;
}

int main() {
    string yuboruvchi, satr;
    getline(cin, yuboruvchi);
    yuboruvchi = tozala(yuboruvchi);
    getline(cin, satr);
    int n = stoi(satr);
    vector<string> qabul(n);
    for (auto& q : qabul) {
        getline(cin, q);
        q = tozala(q);
    }
    string mavzu;
    getline(cin, mavzu);
    mavzu = tozala(mavzu);
    vector<string> matn;
    while (getline(cin, satr)) matn.push_back(satr);
    while (!matn.empty() && matn.back().empty()) matn.pop_back();

    cout << "S: 220 mail.aifu.uz ESMTP" << endl;
    mijoz("HELO client.aifu.uz");
    cout << "S: 250 mail.aifu.uz" << endl;
    mijoz("MAIL FROM:<" + yuboruvchi + ">");
    cout << "S: 250 OK" << endl;
    int togrilar = 0;
    for (auto& manzil : qabul) {
        mijoz("RCPT TO:<" + manzil + ">");
        if (manzil.find('@') != string::npos) {
            cout << "S: 250 OK" << endl;
            togrilar++;
        } else {
            cout << "S: 550 No such user" << endl;
        }
    }
    if (togrilar > 0) {
        mijoz("DATA");
        cout << "S: 354 End data with <CR><LF>.<CR><LF>" << endl;
        mijoz("Subject: " + mavzu);
        mijoz("");                                             // sarlavhalar va tana orasidagi boʻsh satr
        for (auto& s : matn) {
            mijoz(!s.empty() && s[0] == '.' ? "." + s : s);    // dot-stuffing
        }
        mijoz(".");
        cout << "S: 250 OK: queued" << endl;
    }
    mijoz("QUIT");
    cout << "S: 221 Bye" << endl;
    return 0;
}
```

Kirish:

```text
oqituvchi@aifu.uz
2
talaba@aifu.uz
guruh
Dars jadvali
Ertangi dars 9:00 da.
.NET 8 SDK ni yuklab oling.
```

Chiqish:

```text
S: 220 mail.aifu.uz ESMTP
C: HELO client.aifu.uz
S: 250 mail.aifu.uz
C: MAIL FROM:<oqituvchi@aifu.uz>
S: 250 OK
C: RCPT TO:<talaba@aifu.uz>
S: 250 OK
C: RCPT TO:<guruh>
S: 550 No such user
C: DATA
S: 354 End data with <CR><LF>.<CR><LF>
C: Subject: Dars jadvali
C:
C: Ertangi dars 9:00 da.
C: ..NET 8 SDK ni yuklab oling.
C: .
S: 250 OK: queued
C: QUIT
S: 221 Bye
```

`.NET` qatori `..NET` boʻlib ketdi: qoida nuqta bilan boshlangan har qanday qatorga qoʻllanadi, shunda matn ichida yolgʻiz `.` (xat oxiri belgisi) hech qachon uchramaydi; qabul qiluvchi server ortiqcha nuqtani olib tashlaydi. Har bir server javobining birinchi raqami maʼno beradi: 2 — muvaffaqiyat, 3 — davom eting, 4 — vaqtinchalik xato, 5 — doimiy xato.

## Ishlangan misol 2: Base64 kodlash

Har bir satrdagi matnni (UTF-8 baytlari) Base64 ga kodlang. Algoritm: har 3 bayt (24 bit) 4 ta 6 bitli guruhga boʻlinadi, har bir guruh `A–Z a–z 0–9 + /` alifbosidagi belgiga almashtiriladi; oxirida yetmagan baytlar `=` bilan toʻldiriladi.

```python
import sys

ALIFBO = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

for satr in sys.stdin.read().split("\n"):
    if not satr:
        continue
    b = satr.encode("utf-8")
    natija = ""
    for i in range(0, len(b), 3):
        uchlik = b[i:i + 3]
        son = int.from_bytes(uchlik + bytes(3 - len(uchlik)), "big")   # 24 bit
        belgilar = [ALIFBO[(son >> s) & 63] for s in (18, 12, 6, 0)]
        for j in range(3 - len(uchlik)):
            belgilar[3 - j] = "="                                      # toʻldirish
        natija += "".join(belgilar)
    print(natija)
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static final String ALIFBO = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            String satr = in.nextLine();
            if (satr.isEmpty()) continue;
            byte[] b = satr.getBytes(StandardCharsets.UTF_8);
            StringBuilder natija = new StringBuilder();
            for (int i = 0; i < b.length; i += 3) {
                int uzun = Math.min(3, b.length - i);
                int son = 0;
                for (int j = 0; j < 3; j++) {
                    son = (son << 8) | (j < uzun ? b[i + j] & 0xFF : 0);       // 24 bit
                }
                char[] belgilar = new char[4];
                for (int j = 0; j < 4; j++) belgilar[j] = ALIFBO.charAt((son >> (18 - 6 * j)) & 63);
                for (int j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';     // toʻldirish
                natija.append(belgilar);
            }
            System.out.println(natija);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const string ALIFBO = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

int main() {
    string satr;
    while (getline(cin, satr)) {
        if (satr.empty()) continue;
        string natija;
        for (size_t i = 0; i < satr.size(); i += 3) {
            size_t uzun = min<size_t>(3, satr.size() - i);
            unsigned son = 0;
            for (size_t j = 0; j < 3; j++) {
                son = (son << 8) | (j < uzun ? (unsigned char) satr[i + j] : 0);   // 24 bit
            }
            string belgilar(4, ' ');
            for (int j = 0; j < 4; j++) belgilar[j] = ALIFBO[(son >> (18 - 6 * j)) & 63];
            for (size_t j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';      // toʻldirish
            natija += belgilar;
        }
        cout << natija << endl;
    }
    return 0;
}
```

Kirish:

```text
Salom
Ma
talaba:Parol123
```

Chiqish:

```text
U2Fsb20=
TWE=
dGFsYWJhOlBhcm9sMTIz
```

Uchinchi qator — HTTP **Basic** autentifikatsiyasining sarlavhasi: `Authorization: Basic dGFsYWJhOlBhcm9sMTIz`. Base64 — **shifrlash emas**: uni har kim bir zumda qayta oʻgiradi. Shuning uchun Basic autentifikatsiya faqat HTTPS ustida ishlatilishi mumkin.

:::warning
**Koʻp uchraydigan xatolar**

- **404 va 500 ni aralashtirish.** 4xx — mijoz notoʻgʻri soʻradi (manzil, huquq), 5xx — server ichida xato.
- **Base64 ni himoya deb hisoblash.** Bu faqat ikkilik maʼlumotni matnga aylantirish; maxfiylik uchun TLS kerak.
- **FTP ni Internetda parol bilan ishlatish.** Parol va fayllar ochiq oʻtadi — SFTP yoki FTPS ishlating.
- **POP3 va IMAP ni bir xil deb oʻylash.** POP3 xatni yuklab oladi, IMAP serverdagi qutini sinxronlaydi.
- **GET bilan maʼlumotni oʻzgartirish.** GET xavfsiz boʻlishi kerak: qidiruv robotlari va oldindan yuklash uni istalgancha chaqirishi mumkin.
:::

:::tip
Brauzerda F12 → **Network** varagʻini oching va istalgan saytni yangilang. Birinchi soʻrovni tanlab, metod, holat kodi, soʻrov va javob sarlavhalarini koʻring. `Protocol` ustunini yoqing — sayt HTTP/1.1, h2 yoki h3 dan foydalanayotganini aniqlang.
:::

## Xulosa

- Amaliy pogʻona: HTTP(S), FTP, SMTP, POP3, IMAP, DNS, DHCP, SSH — har birining standart porti bor.
- HTTP: soʻrov satri + sarlavhalar + boʻsh satr + tana; metodlar GET, POST, PUT, DELETE; holat kodlari 1xx–5xx; holatsiz, cookie bilan sessiya.
- HTTP/1.1 — keep-alive va Host; HTTP/2 — multipleks; HTTP/3 — QUIC; HTTPS — TLS shifrlash.
- FTP: boshqaruv (21) va maʼlumot ulanishlari, faol va passiv rejim; xavfsiz variantlar — SFTP, FTPS.
- Pochta: MUA → SMTP → MTA → MX → MDA → IMAP/POP3; MIME va Base64 ilovalar uchun.

## Oʻzingizni tekshiring

:::check
Q: 301 va 302 holat kodlarining farqi nimada?
A: 301 — resurs doimiy koʻchgan (brauzer va qidiruv tizimlari yangi manzilni eslab qoladi), 302 — vaqtincha boshqa manzilda.
Q: Bitta IP manzilda bir nechta sayt qanday ishlaydi?
A: Virtual hostlar orqali: server qaysi sayt soʻralganini `Host` sarlavhasidan aniqlaydi.
Q: Nima uchun FTP ning passiv rejimi bugun odatiy?
A: Faol rejimda server mijozga ulanadi va buni mijoz tomonidagi NAT hamda brandmauer toʻsadi; passivda ulanishni mijoz boshlaydi.
Q: Qabul qiluvchining pochta serveri qanday topiladi?
A: Domen uchun DNS dagi MX yozuvi orqali.
Q: "Ma" satrining Base64 kodi nima uchun `=` bilan tugaydi?
A: 2 bayt 3 baytlik guruhni toʻldirmaydi — yetmagan bayt oʻrnida `=` qoʻyiladi.
:::

:::link https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview
MDN: An overview of HTTP
HTTP soʻrovlari, javoblari, sarlavhalar va versiyalar haqida qoʻllanma (ingliz tilida).
:::
