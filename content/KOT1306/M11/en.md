---
title: Application layer protocols and services. The web server service. HTTP, FTP and e-mail protocols
summary: The application layer and the client–server model; HTTP — the structure of a request and a response, methods, status codes, headers, cookies, HTTP/1.1, HTTP/2, HTTP/3, HTTPS; web servers and virtual hosts; FTP — control and data connections, active and passive mode, SFTP; e-mail — MUA, MTA, MDA, SMTP, POP3, IMAP, the message format, MIME and Base64.
---

## Why it matters

For a user, "the Internet" means web pages, mail and files. All of them work through **application layer** protocols. The lower layers deliver packets, but to understand what errors such as "page not found" (404) or "message rejected" (550) mean, why a site will not open or why a message lands in spam, you need to know how HTTP and SMTP "talk".

:::note
**After this lesson you will be able to:**

- explain the structure of an HTTP request and response, the main methods and status codes;
- describe the role of a web server and the differences between the HTTP versions and HTTPS;
- explain how FTP works with two connections, and its active and passive modes;
- describe the path of an e-mail (MUA → MTA → MDA) and the roles of SMTP, POP3 and IMAP;
- read an SMTP dialogue and show how Base64 encoding works.
:::

## Application layer protocols

| Protocol | Port | Transport | Purpose |
|---|---|---|---|
| HTTP / HTTPS | 80 / 443 | TCP (HTTP/3 — UDP/QUIC) | web pages and APIs |
| FTP | 21 (control), 20 (data) | TCP | file exchange |
| SFTP / SSH | 22 | TCP | secure remote access and files |
| SMTP | 25, 587 | TCP | sending mail |
| POP3 / POP3S | 110 / 995 | TCP | downloading mail |
| IMAP / IMAPS | 143 / 993 | TCP | managing mail on the server |
| DNS | 53 | UDP, TCP | turning names into addresses (M12) |
| DHCP | 67, 68 | UDP | assigning addresses (M9) |

Most of them work in the **client–server** model and "talk" with **text** commands — you can even try them by hand with `telnet` or `nc`.

## HTTP

A **request**:

```text
GET /kurslar?semestr=1 HTTP/1.1          request line: method, path, version
Host: aifu.uz                            headers
User-Agent: Mozilla/5.0
Accept-Language: uz
Cookie: sessiya=abc123
                                         empty line
                                         (with POST the body goes here)
```

A **response**:

```text
HTTP/1.1 200 OK                          status line
Content-Type: text/html; charset=utf-8
Content-Length: 5120
Set-Cookie: sessiya=abc123; HttpOnly; Secure

<!doctype html> ...                      body
```

| Method | Purpose | Safe / idempotent |
|---|---|---|
| **GET** | fetch a resource | yes / yes |
| **POST** | send data, create a new resource | no / no |
| **PUT** | replace a resource entirely | no / yes |
| **PATCH** | change part of it | no / no |
| **DELETE** | delete | no / yes |
| **HEAD** | headers only (GET without a body) | yes / yes |

| Status code | Class | Examples |
|---|---|---|
| 1xx | informational | 101 Switching Protocols (WebSocket) |
| **2xx** | success | 200 OK, 201 Created, 204 No Content |
| **3xx** | redirection | 301 Moved Permanently, 302 Found, 304 Not Modified |
| **4xx** | client error | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| **5xx** | server error | 500 Internal Server Error, 502 Bad Gateway, 503 Service Unavailable |

HTTP is **stateless**: every request stands alone. To "remember" a user, **cookies** are used — the server sets one with `Set-Cookie`, and the browser returns it in the `Cookie` header on later requests.

**Versions**: HTTP/1.0 — a new TCP connection per request; **HTTP/1.1** — persistent connections (keep-alive), the `Host` header is mandatory (many sites on one IP); **HTTP/2** — binary frames, parallel streams over one connection, header compression; **HTTP/3** — QUIC over UDP, faster connection setup. **HTTPS** = HTTP + **TLS** encryption; a certificate confirms the server is genuine.

## The web server

A **web server** (Apache, **Nginx**, IIS) listens on ports 80/443, accepts a request and either returns a static file or passes the request to an application (Python, Java, PHP). **Virtual hosts**: several sites on one server — which site is wanted is found from the `Host` header. A **reverse proxy**: Nginx distributes outside requests to inner application servers and handles TLS itself — this learning platform is built that way too.

## FTP

**FTP** (File Transfer Protocol) uses **two** TCP connections: a **control** connection (port 21; the commands `USER`, `PASS`, `LIST`, `RETR`, `STOR`) and a separate **data** connection for each file.

- **Active mode**: the server connects to the client from port 20 — the client-side firewall and NAT often block this.
- **Passive mode** (`PASV`): the server opens a port and the client connects to it — the norm today.

FTP sends the password **in the clear**. Secure alternatives: **SFTP** (over SSH, port 22) and **FTPS** (FTP + TLS).

## E-mail

```text
Sender                                                            Recipient
[MUA: Outlook] --SMTP 587--> [MTA: smtp.aifu.uz] --SMTP 25--> [MTA: mx.gmail.com] --> [MDA: mailbox]
                                                                                           |
                                                      [MUA: phone] <--IMAP 993 / POP3 995--
```

- **MUA** (Mail User Agent) — the mail program; **MTA** (Mail Transfer Agent) — passes a message from server to server; **MDA** — places the message in the mailbox.
- The receiving server's address is found from the **MX** record in DNS.
- **POP3** — downloads messages (usually deleting them from the server); **IMAP** — messages stay on the server, convenient for folders and several devices.
- The message format: headers (`From`, `To`, `Subject`, `Date`), an empty line, the body. Pictures and files are attached with **MIME** and turned into text with **Base64**, because SMTP originally carried only 7-bit text.
- Against forgery: **SPF** (which servers may send), **DKIM** (a digital signature of the message), **DMARC** (the policy).

## Worked example 1: an SMTP dialogue

The first line holds the sender's address, the second the number of recipients *n*, then *n* addresses, then the subject and the message text (until the end of the file). Print the SMTP dialogue between the client (`C:`) and the server (`S:`). The server answers `550` to an address without `@`. If there is no valid recipient, `DATA` is not sent. A text line starting with `.` gets one more `.` (dot-stuffing); the end of the message is a lone `.`.

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
    mijoz("")                                  # the empty line between headers and body
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
            mijoz("");                                         // the empty line between headers and body
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
        mijoz("");                                             // the empty line between headers and body
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

Input:

```text
oqituvchi@aifu.uz
2
talaba@aifu.uz
guruh
Dars jadvali
Ertangi dars 9:00 da.
.NET 8 SDK ni yuklab oling.
```

Output:

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

(The message is in Uzbek: "Timetable — tomorrow's class is at 9:00. Download the .NET 8 SDK.") The `.NET` line became `..NET`: the rule applies to every line starting with a dot, so a lone `.` (the end-of-message mark) never appears inside the text; the receiving server removes the extra dot. The first digit of every server reply carries the meaning: 2 — success, 3 — go on, 4 — a temporary error, 5 — a permanent error.

## Worked example 2: Base64 encoding

Encode the text of each line (its UTF-8 bytes) in Base64. The algorithm: every 3 bytes (24 bits) are split into 4 groups of 6 bits, each group is replaced by a character of the alphabet `A–Z a–z 0–9 + /`; at the end missing bytes are padded with `=`.

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
            belgilar[3 - j] = "="                                      # padding
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
                for (int j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';     // padding
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
            for (size_t j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';      // padding
            natija += belgilar;
        }
        cout << natija << endl;
    }
    return 0;
}
```

Input:

```text
Salom
Ma
talaba:Parol123
```

Output:

```text
U2Fsb20=
TWE=
dGFsYWJhOlBhcm9sMTIz
```

The third line is the header of HTTP **Basic** authentication: `Authorization: Basic dGFsYWJhOlBhcm9sMTIz`. Base64 is **not encryption**: anyone can turn it back in an instant. That is why Basic authentication may be used only over HTTPS.

:::warning
**Common mistakes**

- **Mixing up 404 and 500.** 4xx — the client asked wrongly (address, rights), 5xx — an error inside the server.
- **Taking Base64 for protection.** It only turns binary data into text; secrecy needs TLS.
- **Using FTP with a password over the Internet.** The password and files travel in the clear — use SFTP or FTPS.
- **Thinking POP3 and IMAP are the same.** POP3 downloads messages, IMAP synchronises the mailbox on the server.
- **Changing data with GET.** GET must be safe: search robots and prefetching may call it any number of times.
:::

:::tip
In the browser press F12 → the **Network** tab and reload any site. Select the first request and look at the method, the status code and the request and response headers. Turn on the `Protocol` column — find out whether the site uses HTTP/1.1, h2 or h3.
:::

## Summary

- The application layer: HTTP(S), FTP, SMTP, POP3, IMAP, DNS, DHCP, SSH — each has a standard port.
- HTTP: request line + headers + empty line + body; methods GET, POST, PUT, DELETE; status codes 1xx–5xx; stateless, sessions with cookies.
- HTTP/1.1 — keep-alive and Host; HTTP/2 — multiplexing; HTTP/3 — QUIC; HTTPS — TLS encryption.
- FTP: control (21) and data connections, active and passive mode; secure variants — SFTP, FTPS.
- Mail: MUA → SMTP → MTA → MX → MDA → IMAP/POP3; MIME and Base64 for attachments.

## Check yourself

:::check
Q: What is the difference between status codes 301 and 302?
A: 301 — the resource has moved permanently (browsers and search engines remember the new address), 302 — it is temporarily at another address.
Q: How do several sites work on one IP address?
A: Through virtual hosts: the server finds which site is wanted from the `Host` header.
Q: Why is FTP's passive mode the norm today?
A: In active mode the server connects to the client, which client-side NAT and firewalls block; in passive mode the client starts the connection.
Q: How is the recipient's mail server found?
A: Through the domain's MX record in DNS.
Q: Why does the Base64 code of "Ma" end with `=`?
A: 2 bytes do not fill a 3-byte group — an `=` stands in place of the missing byte.
:::

:::link https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview
MDN: An overview of HTTP
A guide to HTTP requests, responses, headers and versions.
:::
