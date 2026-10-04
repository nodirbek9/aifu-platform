---
title: Создание сетевой программы с помощью сокетов
summary: Типы сокетов (потоковые и датаграммные); TCP эхо-сервер и клиент на Python и Java; проверка программы с помощью telnet, nc и curl; обслуживание многих клиентов; границы сообщений в потоке TCP и способы их разделения (префикс длины, разделитель); кодирование текста; структура URL и её разбор; HTTP — текстовый протокол поверх TCP.
---

## Почему это важно

Браузер, мессенджер, онлайн-игра, эта учебная платформа — все внутри работают с **сокетами**. На лекции M10 мы рассмотрели последовательность вызовов API сокетов; на этом занятии напишем настоящие сервер и клиент, проверим их стандартными инструментами и решим проблему, с которой сталкивается каждый сетевой программист, — **в потоке TCP нет границ сообщений**.

:::note
**После этого занятия вы сможете:**

- писать простые TCP-сервер и клиент на Python или Java;
- проверять сервер с помощью `telnet`, `nc` или `curl`;
- объяснять, почему один `recv()` — не одно сообщение, и разделять сообщения префиксом длины;
- разбирать URL на части (схема, хост, порт, путь, запрос);
- знать, как выглядит HTTP-запрос, отправляемый через сокет.
:::

## Типы сокетов

| Тип | Константа | Транспорт | Особенность |
|---|---|---|---|
| Потоковый | `SOCK_STREAM` | TCP | соединение, надёжный **поток байтов**, границ сообщений нет |
| Датаграммный | `SOCK_DGRAM` | UDP | без соединения, каждый `sendto()` — отдельное сообщение, может потеряться |

Семейство адресов: `AF_INET` — IPv4, `AF_INET6` — IPv6. Сервер обычно привязывается к `0.0.0.0` (все интерфейсы) или `127.0.0.1` (только этот компьютер).

## TCP эхо-сервер и клиент (Python)

Сервер возвращает обратно все полученные данные:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("127.0.0.1", 5000))
    server.listen()
    print("слушаю порт 5000")
    while True:
        conn, addr = server.accept()               # новый сокет для нового клиента
        with conn:
            print("подключился:", addr)
            while True:
                data = conn.recv(1024)
                if not data:                        # пусто — клиент закрыл соединение
                    break
                conn.sendall(data)
```

Клиент:

```text
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client:
    client.connect(("127.0.0.1", 5000))
    client.sendall("Привет, сервер!".encode("utf-8"))
    reply = client.recv(1024)
    print("ответ:", reply.decode("utf-8"))
```

В Java та же идея записывается классами `ServerSocket` (сервер) и `Socket` (клиент): `new ServerSocket(5000)`, `server.accept()`, `socket.getInputStream()` / `getOutputStream()`.

## Практическая работа

1. Запустите сервер в одном терминале, а клиент — во втором. Если брандмауэр Windows запросит разрешение, разрешите только для «Частных сетей».
2. Проверьте сервер без клиента: `telnet 127.0.0.1 5000` (в Windows может понадобиться включить клиент Telnet) или `nc 127.0.0.1 5000` — введённый текст вернётся обратно.
3. Посмотрите состояния LISTEN и ESTABLISHED командой `netstat -an | findstr 5000`.
4. Подключите два клиента одновременно: второй ждёт, пока отключится первый. Причина — сервер работает в одном потоке. Исправьте это, запуская после каждого `accept()` новый **поток** (`threading.Thread`).
5. HTTP — обычный текст поверх TCP. Чтобы увидеть это, выполните `curl -v http://example.com`: строки, начинающиеся с `>`, — запрос, записанный в сокет, а с `<` — заголовки ответа.

```text
GET /index.html HTTP/1.1
Host: example.com
User-Agent: curl/8.0
Accept: */*

```

Строка запроса, заголовки и **пустая строка** — по ней сервер понимает, что запрос закончился. Программа, разбирающая такой запрос на части, — самостоятельное задание этого занятия (KOT-A9).

## Границы сообщений в потоке

TCP — **поток байтов**: если отправитель делает `send("Salom")` и `send("Hey")`, получатель может получить за один `recv()` `"SalomHey"` или `"Sal"` и `"omHey"`. Чтобы разделять сообщения, у программы должен быть свой **протокол**:

- **Разделитель** — каждое сообщение заканчивается `\n` (заголовки HTTP, SMTP, Redis);
- **Префикс длины** — перед сообщением записывается его длина: `5:Salom3:Hey` (двоичные протоколы, кадры WebSocket);
- **Фиксированная длина** — все сообщения одного размера.

Текст передаётся в сокет байтами — `encode("utf-8")` / `decode("utf-8")`. Внимание: в UTF-8 символы вроде «ʻ» или кириллица занимают 2 байта, поэтому префикс длины нужно считать **в байтах**.

## Разобранный пример 1: разбор URL

В каждой строке — URL. Разделите его на схему, хост, порт (если не указан — стандартный для схемы: http 80, https 443, ftp 21), путь (`/`, если отсутствует) и запрос (часть после `?`; `-`, если нет); часть после `#` отбрасывается. Если нет `://` — `неверный`.

```python
import sys

STANDART = {"http": "80", "https": "443", "ftp": "21"}

for url in sys.stdin.read().split():
    if "://" not in url:
        print(url, "неверный")
        continue
    sxema, qolgan = url.split("://", 1)
    qolgan = qolgan.split("#", 1)[0]               # фрагмент на сервер не отправляется
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
        print(url, "неверный")
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
                System.out.println(url + " неверный");
                continue;
            }
            String sxema = url.substring(0, ajr);
            String qolgan = url.substring(ajr + 3);
            int panjara = qolgan.indexOf('#');
            if (panjara >= 0) qolgan = qolgan.substring(0, panjara);   // фрагмент на сервер не отправляется
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
                System.out.println(url + " неверный");
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
            cout << url << " неверный" << endl;
            continue;
        }
        string sxema = url.substr(0, ajr);
        string qolgan = url.substr(ajr + 3);
        size_t panjara = qolgan.find('#');
        if (panjara != string::npos) qolgan = qolgan.substr(0, panjara);   // фрагмент на сервер не отправляется
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
            cout << url << " неверный" << endl;
            continue;
        }
        cout << sxema << " " << host << " " << port << " " << yol << " " << soruv << endl;
    }
    return 0;
}
```

Ввод:

```text
http://aifu.uz/news?id=5
https://example.com:8443/api/v1/users#top
ftp://files.aifu.uz
aifu.uz/index.html
```

Вывод:

```text
http aifu.uz 80 /news id=5
https example.com 8443 /api/v1/users -
ftp files.aifu.uz 21 / -
aifu.uz/index.html неверный
```

Клиентская программа работает именно с этими частями: `host` — чтобы получить IP от DNS, `port` — для `connect()`, путь и запрос — для строки HTTP-запроса. Фрагмент (`#top`) остаётся в браузере и на сервер не отправляется.

## Разобранный пример 2: выделение сообщений из потока

Протокол: перед каждым сообщением записываются его длина и `:` (`5:Salom`). Каждая строка ввода — кусок, который вернул один вызов `recv()` (сообщения могут быть разрезаны между кусками). Выведите каждое полностью собранное сообщение; в конце выведите число неполных байтов, оставшихся в буфере.

```python
import sys

bufer = ""
for qism in sys.stdin.read().split("\n"):
    bufer += qism                                  # байты, которые вернул recv()
    while ":" in bufer:
        uzunlik = int(bufer[:bufer.index(":")])
        boshi = bufer.index(":") + 1
        if len(bufer) - boshi < uzunlik:
            break                                  # сообщение ещё не пришло целиком
        print("сообщение: " + bufer[boshi:boshi + uzunlik])
        bufer = bufer[boshi + uzunlik:]
print("остаток: " + str(len(bufer)) + " байт")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder bufer = new StringBuilder();
        while (in.hasNextLine()) {
            bufer.append(in.nextLine());                         // байты, которые вернул recv()
            while (bufer.indexOf(":") >= 0) {
                int ikki = bufer.indexOf(":");
                int uzunlik = Integer.parseInt(bufer.substring(0, ikki));
                int boshi = ikki + 1;
                if (bufer.length() - boshi < uzunlik) {
                    break;                                       // сообщение ещё не пришло целиком
                }
                System.out.println("сообщение: " + bufer.substring(boshi, boshi + uzunlik));
                bufer.delete(0, boshi + uzunlik);
            }
        }
        System.out.println("остаток: " + bufer.length() + " байт");
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
        bufer += qism;                                           // байты, которые вернул recv()
        while (bufer.find(':') != string::npos) {
            size_t ikki = bufer.find(':');
            size_t uzunlik = stoul(bufer.substr(0, ikki));
            size_t boshi = ikki + 1;
            if (bufer.size() - boshi < uzunlik) {
                break;                                           // сообщение ещё не пришло целиком
            }
            cout << "сообщение: " << bufer.substr(boshi, uzunlik) << endl;
            bufer.erase(0, boshi + uzunlik);
        }
    }
    cout << "остаток: " << bufer.size() << " байт" << endl;
    return 0;
}
```

Ввод (результаты трёх вызовов `recv()`):

```text
5:Salo
m3:Hey16:Kompyuter
_tarmoq2:o
```

Вывод:

```text
сообщение: Salom
сообщение: Hey
сообщение: Kompyuter_tarmoq
остаток: 3 байт
```

В первом `recv()` «Salom» пришло не полностью — программа ждёт. Во втором сразу пришло полтора сообщения. Последнее `2:o` — начало сообщения, ожидающего следующего `recv()`. Программа, работающая иначе, проходит проверку (на localhost), но в реальной сети даёт случайные ошибки.

:::warning
**Частые ошибки**

- **Считать один `recv()` одним сообщением.** TCP не сохраняет границы — нужен протокол (длина или разделитель).
- **Думать, что `send()` отправляет все данные.** `send()` может отправить только часть; в Python используйте `sendall()`.
- **Путать строки и байты.** В сокет пишутся байты; префикс длины считается в байтах UTF-8.
- **«Address already in use» при перезапуске сервера.** Предыдущее соединение в TIME_WAIT; установите опцию `SO_REUSEADDR`.
- **Открывать сервер на `0.0.0.0`.** Тестовый сервер становится доступен всем в сети; для упражнений используйте `127.0.0.1`.
:::

:::tip
Превратите эхо-сервер в чат-сервер: для каждого клиента откройте отдельный поток, отправляйте каждое пришедшее сообщение **всем** подключённым клиентам, разделяйте сообщения `\n`. Проверьте в двух-трёх терминалах с `nc 127.0.0.1 5000`.
:::

## Итоги

- `SOCK_STREAM` — TCP (надёжный поток байтов), `SOCK_DGRAM` — UDP (отдельные датаграммы).
- Сервер: socket → bind → listen → accept → recv/sendall → close; клиент: socket → connect → sendall/recv → close.
- Сервер можно проверить `telnet`, `nc`, `curl -v`; для многих клиентов нужны потоки.
- В TCP нет границ сообщений — протокол строится на префиксе длины или разделителе.
- URL: схема://хост:порт/путь?запрос#фрагмент; HTTP — текстовый запрос, отправляемый через сокет.

## Проверьте себя

:::check
Q: Чем отличаются сокеты `SOCK_STREAM` и `SOCK_DGRAM`?
A: Первый — TCP, надёжный поток байтов с соединением; второй — UDP, отдельные датаграммы без соединения.
Q: Клиент сделал два `send()`. Получит ли сервер ровно два сообщения за два `recv()`?
A: Гарантии нет: TCP — поток байтов, они могут прийти слитно или разрезанными.
Q: К какому порту подключится клиент для `https://aifu.uz/login`?
A: 443 — стандартный порт схемы https.
Q: Что означает пустой результат `recv()` на сокете сервера?
A: Клиент закрыл соединение.
Q: Как HTTP-сервер узнаёт, что заголовки запроса закончились?
A: По пустой строке (`\r\n\r\n`).
:::

:::link https://docs.python.org/3/howto/sockets.html
Python: Socket Programming HOWTO
Официальное руководство по работе с сокетами (на английском).
:::
