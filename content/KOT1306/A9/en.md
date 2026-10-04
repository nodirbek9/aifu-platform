---
title: Building a network program with sockets
summary: Socket types (stream and datagram); a TCP echo server and client in Python and Java; testing a program with telnet, nc and curl; serving many clients; message boundaries in a TCP stream and ways of separating messages (a length prefix, a delimiter); text encoding; the structure of a URL and parsing it; HTTP — a text protocol over TCP.
---

## Why it matters

A browser, a messenger, an online game, this very learning platform — all of them work with **sockets** inside. In lecture M10 we saw the order of socket API calls; in this session we write a real server and client, test them with standard tools, and solve the problem every network programmer runs into — **a TCP stream has no message boundaries**.

:::note
**After this session you will be able to:**

- write a simple TCP server and client in Python or Java;
- test a server with `telnet`, `nc` or `curl`;
- explain why one `recv()` is not one message, and separate messages with a length prefix;
- split a URL into parts (scheme, host, port, path, query);
- know what an HTTP request sent through a socket looks like.
:::

## Socket types

| Type | Constant | Transport | Feature |
|---|---|---|---|
| Stream | `SOCK_STREAM` | TCP | a connection, a reliable **byte stream**, no message boundaries |
| Datagram | `SOCK_DGRAM` | UDP | connectionless, every `sendto()` is a separate message, may be lost |

Address family: `AF_INET` — IPv4, `AF_INET6` — IPv6. A server usually binds to `0.0.0.0` (all interfaces) or `127.0.0.1` (this computer only).

## A TCP echo server and client (Python)

The server sends every piece of data it receives straight back:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("127.0.0.1", 5000))
    server.listen()
    print("listening on port 5000")
    while True:
        conn, addr = server.accept()               # a new socket for a new client
        with conn:
            print("connected:", addr)
            while True:
                data = conn.recv(1024)
                if not data:                        # empty — the client closed the connection
                    break
                conn.sendall(data)
```

The client:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client:
    client.connect(("127.0.0.1", 5000))
    client.sendall("Hello, server!".encode("utf-8"))
    reply = client.recv(1024)
    print("reply:", reply.decode("utf-8"))
```

In Java the same idea is written with the `ServerSocket` (server) and `Socket` (client) classes: `new ServerSocket(5000)`, `server.accept()`, `socket.getInputStream()` / `getOutputStream()`.

## Practical work

1. Start the server in one terminal and the client in another. If Windows Firewall asks for permission, allow it for "Private networks" only.
2. Test the server without the client: `telnet 127.0.0.1 5000` (on Windows the Telnet client may need to be enabled) or `nc 127.0.0.1 5000` — the text you type comes back.
3. Look at the LISTEN and ESTABLISHED states with `netstat -an | findstr 5000`.
4. Connect two clients at once: the second waits until the first disconnects. The reason is that the server runs in one thread. Fix this by starting a new **thread** (`threading.Thread`) after every `accept()`.
5. HTTP is plain text over TCP. To see this, run `curl -v http://example.com`: the lines starting with `>` are the request written to the socket, and those starting with `<` are the reply headers.

```text
GET /index.html HTTP/1.1
Host: example.com
User-Agent: curl/8.0
Accept: */*

```

The request line, the headers and an **empty line** — that is how the server knows the request has ended. A program that splits such a request into its parts is this session's independent task (KOT-A9).

## Message boundaries in a stream

TCP is a **byte stream**: if the sender does `send("Salom")` and `send("Hey")`, the receiver may get `"SalomHey"` in one `recv()`, or `"Sal"` and `"omHey"`. To separate messages, the program needs its own **protocol**:

- **A delimiter** — every message ends with `\n` (HTTP headers, SMTP, Redis);
- **A length prefix** — the message is preceded by its length: `5:Salom3:Hey` (binary protocols, WebSocket frames);
- **A fixed length** — all messages have the same size.

Text is sent through a socket as bytes — `encode("utf-8")` / `decode("utf-8")`. Note: in UTF-8 characters such as "ʻ" take 2 bytes, so a length prefix must be counted in **bytes**.

## Worked example 1: parsing a URL

Each line holds a URL. Split it into the scheme, the host, the port (if not given — the scheme's default: http 80, https 443, ftp 21), the path (`/` if absent) and the query (the part after `?`; `-` if absent); the part after `#` is dropped. If there is no `://` — `invalid`.

```python
import sys

STANDART = {"http": "80", "https": "443", "ftp": "21"}

for url in sys.stdin.read().split():
    if "://" not in url:
        print(url, "invalid")
        continue
    sxema, qolgan = url.split("://", 1)
    qolgan = qolgan.split("#", 1)[0]               # the fragment is not sent to the server
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
        print(url, "invalid")
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
                System.out.println(url + " invalid");
                continue;
            }
            String sxema = url.substring(0, ajr);
            String qolgan = url.substring(ajr + 3);
            int panjara = qolgan.indexOf('#');
            if (panjara >= 0) qolgan = qolgan.substring(0, panjara);   // the fragment is not sent to the server
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
                System.out.println(url + " invalid");
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
            cout << url << " invalid" << endl;
            continue;
        }
        string sxema = url.substr(0, ajr);
        string qolgan = url.substr(ajr + 3);
        size_t panjara = qolgan.find('#');
        if (panjara != string::npos) qolgan = qolgan.substr(0, panjara);   // the fragment is not sent to the server
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
            cout << url << " invalid" << endl;
            continue;
        }
        cout << sxema << " " << host << " " << port << " " << yol << " " << soruv << endl;
    }
    return 0;
}
```

Input:

```text
http://aifu.uz/news?id=5
https://example.com:8443/api/v1/users#top
ftp://files.aifu.uz
aifu.uz/index.html
```

Output:

```text
http aifu.uz 80 /news id=5
https example.com 8443 /api/v1/users -
ftp files.aifu.uz 21 / -
aifu.uz/index.html invalid
```

A client program works with exactly these parts: the `host` — to get an IP from DNS, the `port` — for `connect()`, the path and query — for the HTTP request line. The fragment (`#top`) stays in the browser and is not sent to the server.

## Worked example 2: separating messages in a stream

The protocol: every message is preceded by its length and `:` (`5:Salom`). Each input line is the piece one `recv()` call returned (messages may be split between pieces). Print every fully assembled message; at the end print the number of incomplete bytes left in the buffer.

```python
import sys

bufer = ""
for qism in sys.stdin.read().split("\n"):
    bufer += qism                                  # the bytes recv() returned
    while ":" in bufer:
        uzunlik = int(bufer[:bufer.index(":")])
        boshi = bufer.index(":") + 1
        if len(bufer) - boshi < uzunlik:
            break                                  # the message has not fully arrived yet
        print("message: " + bufer[boshi:boshi + uzunlik])
        bufer = bufer[boshi + uzunlik:]
print("left over: " + str(len(bufer)) + " bytes")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder bufer = new StringBuilder();
        while (in.hasNextLine()) {
            bufer.append(in.nextLine());                         // the bytes recv() returned
            while (bufer.indexOf(":") >= 0) {
                int ikki = bufer.indexOf(":");
                int uzunlik = Integer.parseInt(bufer.substring(0, ikki));
                int boshi = ikki + 1;
                if (bufer.length() - boshi < uzunlik) {
                    break;                                       // the message has not fully arrived yet
                }
                System.out.println("message: " + bufer.substring(boshi, boshi + uzunlik));
                bufer.delete(0, boshi + uzunlik);
            }
        }
        System.out.println("left over: " + bufer.length() + " bytes");
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
        bufer += qism;                                           // the bytes recv() returned
        while (bufer.find(':') != string::npos) {
            size_t ikki = bufer.find(':');
            size_t uzunlik = stoul(bufer.substr(0, ikki));
            size_t boshi = ikki + 1;
            if (bufer.size() - boshi < uzunlik) {
                break;                                           // the message has not fully arrived yet
            }
            cout << "message: " << bufer.substr(boshi, uzunlik) << endl;
            bufer.erase(0, boshi + uzunlik);
        }
    }
    cout << "left over: " << bufer.size() << " bytes" << endl;
    return 0;
}
```

Input (the results of three `recv()` calls):

```text
5:Salo
m3:Hey16:Kompyuter
_tarmoq2:o
```

Output:

```text
message: Salom
message: Hey
message: Kompyuter_tarmoq
left over: 3 bytes
```

In the first `recv()` "Salom" has not fully arrived — the program waits. In the second, one and a half messages arrived at once. The final `2:o` is the start of a message waiting for the next `recv()`. A program that does not work this way passes tests (on localhost) but gives random errors on a real network.

:::warning
**Common mistakes**

- **Assuming one `recv()` is one message.** TCP keeps no boundaries — a protocol (a length or a delimiter) is needed.
- **Thinking `send()` sends all the data.** `send()` may send only part; use `sendall()` in Python.
- **Mixing up strings and bytes.** Bytes are written to a socket; a length prefix is counted in UTF-8 bytes.
- **"Address already in use" when restarting the server.** The previous connection is in TIME_WAIT; set the `SO_REUSEADDR` option.
- **Opening the server on `0.0.0.0`.** A test server becomes open to everyone on the network; use `127.0.0.1` for exercises.
:::

:::tip
Turn the echo server into a chat server: open a separate thread for each client, send every incoming message to **all** connected clients, and separate messages with `\n`. Test it with `nc 127.0.0.1 5000` in two or three terminals.
:::

## Summary

- `SOCK_STREAM` — TCP (a reliable byte stream), `SOCK_DGRAM` — UDP (separate datagrams).
- Server: socket → bind → listen → accept → recv/sendall → close; client: socket → connect → sendall/recv → close.
- A server can be tested with `telnet`, `nc`, `curl -v`; many clients need threads.
- TCP has no message boundaries — a protocol is built with a length prefix or a delimiter.
- URL: scheme://host:port/path?query#fragment; HTTP is a text request sent through a socket.

## Check yourself

:::check
Q: What is the difference between `SOCK_STREAM` and `SOCK_DGRAM` sockets?
A: The first is TCP — a connection-based reliable byte stream; the second is UDP — connectionless separate datagrams.
Q: A client did two `send()` calls. Will the server get exactly two messages in two `recv()` calls?
A: There is no guarantee: TCP is a byte stream, they may arrive joined or split.
Q: Which port does a client connect to for `https://aifu.uz/login`?
A: 443 — the default port of the https scheme.
Q: What does it mean when `recv()` on a server socket returns an empty result?
A: The client has closed the connection.
Q: How does an HTTP server know the request headers have ended?
A: From the empty line (`\r\n\r\n`).
:::

:::link https://docs.python.org/3/howto/sockets.html
Python: Socket Programming HOWTO
The official guide to working with sockets.
:::
