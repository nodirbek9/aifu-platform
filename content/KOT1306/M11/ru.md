---
title: Протоколы и службы прикладного уровня. Служба веб-сервера. Протоколы HTTP, FTP и электронной почты
summary: Прикладной уровень и модель клиент–сервер; HTTP — структура запроса и ответа, методы, коды состояния, заголовки, cookie, HTTP/1.1, HTTP/2, HTTP/3, HTTPS; веб-серверы и виртуальные хосты; FTP — управляющее соединение и соединение данных, активный и пассивный режим, SFTP; электронная почта — MUA, MTA, MDA, SMTP, POP3, IMAP, формат письма, MIME и Base64.
---

## Почему это важно

Для пользователя «Интернет» — это веб-страницы, почта и файлы. Всё это работает через протоколы **прикладного уровня**. Нижние уровни доставляют пакеты, но чтобы понимать, что значат ошибки вроде «страница не найдена» (404) или «письмо не принято» (550), почему не открывается сайт или почему письмо попадает в спам, нужно знать, как «разговаривают» HTTP и SMTP.

:::note
**После этого занятия вы сможете:**

- объяснять структуру HTTP-запроса и ответа, основные методы и коды состояния;
- описывать назначение веб-сервера и различия версий HTTP и HTTPS;
- объяснять работу FTP с двумя соединениями, активный и пассивный режимы;
- описывать путь письма (MUA → MTA → MDA) и назначение SMTP, POP3 и IMAP;
- читать диалог SMTP и показывать, как работает кодирование Base64.
:::

## Протоколы прикладного уровня

| Протокол | Порт | Транспорт | Назначение |
|---|---|---|---|
| HTTP / HTTPS | 80 / 443 | TCP (HTTP/3 — UDP/QUIC) | веб-страницы и API |
| FTP | 21 (управление), 20 (данные) | TCP | обмен файлами |
| SFTP / SSH | 22 | TCP | безопасный удалённый доступ и файлы |
| SMTP | 25, 587 | TCP | отправка почты |
| POP3 / POP3S | 110 / 995 | TCP | загрузка почты |
| IMAP / IMAPS | 143 / 993 | TCP | управление почтой на сервере |
| DNS | 53 | UDP, TCP | преобразование имён в адреса (M12) |
| DHCP | 67, 68 | UDP | назначение адресов (M9) |

Большинство из них работает по модели **клиент–сервер** и «разговаривает» **текстовыми** командами — их можно попробовать даже вручную через `telnet` или `nc`.

## HTTP

**Запрос** (request):

```text
GET /kurslar?semestr=1 HTTP/1.1          строка запроса: метод, путь, версия
Host: aifu.uz                            заголовки
User-Agent: Mozilla/5.0
Accept-Language: uz
Cookie: sessiya=abc123
                                         пустая строка
                                         (в POST здесь идёт тело)
```

**Ответ** (response):

```text
HTTP/1.1 200 OK                          строка состояния
Content-Type: text/html; charset=utf-8
Content-Length: 5120
Set-Cookie: sessiya=abc123; HttpOnly; Secure

<!doctype html> ...                      тело
```

| Метод | Назначение | Безопасный / идемпотентный |
|---|---|---|
| **GET** | получить ресурс | да / да |
| **POST** | отправить данные, создать новый ресурс | нет / нет |
| **PUT** | полностью заменить ресурс | нет / да |
| **PATCH** | частично изменить | нет / нет |
| **DELETE** | удалить | нет / да |
| **HEAD** | только заголовки (GET без тела) | да / да |

| Код состояния | Класс | Примеры |
|---|---|---|
| 1xx | информационные | 101 Switching Protocols (WebSocket) |
| **2xx** | успех | 200 OK, 201 Created, 204 No Content |
| **3xx** | перенаправление | 301 Moved Permanently, 302 Found, 304 Not Modified |
| **4xx** | ошибка клиента | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| **5xx** | ошибка сервера | 500 Internal Server Error, 502 Bad Gateway, 503 Service Unavailable |

HTTP **не хранит состояние** (stateless): каждый запрос отдельный. Чтобы «запомнить» пользователя, используются **cookie** — сервер выдаёт их через `Set-Cookie`, а браузер возвращает в заголовке `Cookie` в следующих запросах.

**Версии**: HTTP/1.0 — новое TCP-соединение на каждый запрос; **HTTP/1.1** — постоянное соединение (keep-alive), обязательный заголовок `Host` (много сайтов на одном IP); **HTTP/2** — двоичные кадры, параллельные потоки в одном соединении, сжатие заголовков; **HTTP/3** — QUIC поверх UDP, более быстрое установление соединения. **HTTPS** = HTTP + шифрование **TLS**; сертификат подтверждает подлинность сервера.

## Веб-сервер

**Веб-сервер** (Apache, **Nginx**, IIS) слушает порты 80/443, принимает запрос и либо возвращает статический файл, либо передаёт запрос приложению (Python, Java, PHP). **Виртуальные хосты**: несколько сайтов на одном сервере — какой сайт запрошен, определяется по заголовку `Host`. **Обратный прокси** (reverse proxy): Nginx распределяет внешние запросы по внутренним серверам приложений и сам выполняет TLS — так устроена и эта учебная платформа.

## FTP

**FTP** (File Transfer Protocol) использует **два** TCP-соединения: **управляющее** (порт 21; команды `USER`, `PASS`, `LIST`, `RETR`, `STOR`) и отдельное **соединение данных** для каждого файла.

- **Активный режим**: сервер подключается к клиенту с порта 20 — брандмауэр и NAT на стороне клиента часто это блокируют.
- **Пассивный режим** (`PASV`): сервер открывает порт, клиент подключается к нему — сегодня обычный вариант.

FTP передаёт пароль **открытым текстом**. Безопасные альтернативы: **SFTP** (поверх SSH, порт 22) и **FTPS** (FTP + TLS).

## Электронная почта

```text
Отправитель                                                      Получатель
[MUA: Outlook] --SMTP 587--> [MTA: smtp.aifu.uz] --SMTP 25--> [MTA: mx.gmail.com] --> [MDA: почтовый ящик]
                                                                                           |
                                                    [MUA: телефон] <--IMAP 993 / POP3 995--
```

- **MUA** (Mail User Agent) — почтовая программа; **MTA** (Mail Transfer Agent) — передаёт письмо от сервера к серверу; **MDA** — помещает письмо в почтовый ящик.
- Адрес сервера получателя находится по записи **MX** в DNS.
- **POP3** — загружает письма (обычно удаляя их с сервера); **IMAP** — письма остаются на сервере, удобно для папок и нескольких устройств.
- Формат письма: заголовки (`From`, `To`, `Subject`, `Date`), пустая строка, тело. Картинки и файлы прикрепляются с помощью **MIME** и превращаются в текст через **Base64**, так как SMTP изначально переносил только 7-битный текст.
- Против подделки: **SPF** (какие серверы могут отправлять), **DKIM** (цифровая подпись письма), **DMARC** (политика).

## Разобранный пример 1: диалог SMTP

В первой строке — адрес отправителя, во второй — число получателей *n*, затем *n* адресов, затем тема и текст письма (до конца файла). Выведите диалог SMTP между клиентом (`C:`) и сервером (`S:`). На адрес без `@` сервер отвечает `550`. Если нет ни одного правильного получателя, `DATA` не отправляется. К строке текста, начинающейся с `.`, добавляется ещё одна `.` (dot-stuffing), конец письма — одиночная `.`.

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
    mijoz("")                                  # пустая строка между заголовками и телом
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
            mijoz("");                                         // пустая строка между заголовками и телом
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
        mijoz("");                                             // пустая строка между заголовками и телом
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

Ввод:

```text
oqituvchi@aifu.uz
2
talaba@aifu.uz
guruh
Dars jadvali
Ertangi dars 9:00 da.
.NET 8 SDK ni yuklab oling.
```

Вывод:

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

(Письмо на узбекском: «Расписание — завтра занятие в 9:00. Скачайте .NET 8 SDK».) Строка `.NET` превратилась в `..NET`: правило применяется к любой строке, начинающейся с точки, поэтому одиночная `.` (признак конца письма) никогда не встречается внутри текста; принимающий сервер удаляет лишнюю точку. Первая цифра каждого ответа сервера несёт смысл: 2 — успех, 3 — продолжайте, 4 — временная ошибка, 5 — постоянная ошибка.

## Разобранный пример 2: кодирование Base64

Закодируйте текст каждой строки (её байты UTF-8) в Base64. Алгоритм: каждые 3 байта (24 бита) делятся на 4 группы по 6 бит, каждая группа заменяется символом алфавита `A–Z a–z 0–9 + /`; в конце недостающие байты дополняются знаком `=`.

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
            belgilar[3 - j] = "="                                      # дополнение
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
                for (int j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';     // дополнение
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
            for (size_t j = 0; j < 3 - uzun; j++) belgilar[3 - j] = '=';      // дополнение
            natija += belgilar;
        }
        cout << natija << endl;
    }
    return 0;
}
```

Ввод:

```text
Salom
Ma
talaba:Parol123
```

Вывод:

```text
U2Fsb20=
TWE=
dGFsYWJhOlBhcm9sMTIz
```

Третья строка — заголовок **Basic**-аутентификации HTTP: `Authorization: Basic dGFsYWJhOlBhcm9sMTIz`. Base64 — **не шифрование**: любой мгновенно преобразует его обратно. Поэтому Basic-аутентификацию можно использовать только поверх HTTPS.

:::warning
**Частые ошибки**

- **Путать 404 и 500.** 4xx — клиент запросил неправильно (адрес, права), 5xx — ошибка внутри сервера.
- **Считать Base64 защитой.** Это лишь превращение двоичных данных в текст; для секретности нужен TLS.
- **Использовать FTP с паролем через Интернет.** Пароль и файлы идут открыто — используйте SFTP или FTPS.
- **Думать, что POP3 и IMAP одинаковы.** POP3 загружает письма, IMAP синхронизирует ящик на сервере.
- **Изменять данные через GET.** GET должен быть безопасным: поисковые роботы и предзагрузка могут вызывать его сколько угодно раз.
:::

:::tip
В браузере нажмите F12 → вкладка **Network** и обновите любой сайт. Выберите первый запрос и посмотрите метод, код состояния, заголовки запроса и ответа. Включите столбец `Protocol` — определите, использует ли сайт HTTP/1.1, h2 или h3.
:::

## Итоги

- Прикладной уровень: HTTP(S), FTP, SMTP, POP3, IMAP, DNS, DHCP, SSH — у каждого свой стандартный порт.
- HTTP: строка запроса + заголовки + пустая строка + тело; методы GET, POST, PUT, DELETE; коды состояния 1xx–5xx; без состояния, сессии через cookie.
- HTTP/1.1 — keep-alive и Host; HTTP/2 — мультиплексирование; HTTP/3 — QUIC; HTTPS — шифрование TLS.
- FTP: управляющее (21) соединение и соединения данных, активный и пассивный режимы; безопасные варианты — SFTP, FTPS.
- Почта: MUA → SMTP → MTA → MX → MDA → IMAP/POP3; MIME и Base64 для вложений.

## Проверьте себя

:::check
Q: Чем отличаются коды состояния 301 и 302?
A: 301 — ресурс перемещён навсегда (браузеры и поисковики запоминают новый адрес), 302 — временно находится по другому адресу.
Q: Как на одном IP-адресе работают несколько сайтов?
A: Через виртуальные хосты: сервер определяет нужный сайт по заголовку `Host`.
Q: Почему пассивный режим FTP сегодня обычный?
A: В активном режиме сервер подключается к клиенту, что блокируют NAT и брандмауэр клиента; в пассивном соединение начинает клиент.
Q: Как находится почтовый сервер получателя?
A: По записи MX домена в DNS.
Q: Почему код Base64 строки «Ma» заканчивается на `=`?
A: 2 байта не заполняют 3-байтную группу — на месте недостающего байта ставится `=`.
:::

:::link https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview
MDN: An overview of HTTP
Руководство по запросам, ответам, заголовкам и версиям HTTP (на английском).
:::
