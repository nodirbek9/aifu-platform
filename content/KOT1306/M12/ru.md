---
title: Сеть Интернет и служба DNS. Служба DNS и принцип её работы
summary: Структура Интернета — провайдеры, автономные системы, точки обмена; иерархия доменных имён — корень, домены верхнего уровня (TLD), зоны; виды DNS-серверов; рекурсивные и итеративные запросы; кэширование и TTL; ресурсные записи — A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; сообщение DNS и кодирование имени; nslookup и dig; безопасность DNS.
---

## Почему это важно

Человек запоминает `aifu.uz`, а компьютер работает с `185.x.x.x`. Каждый раз, когда открывается сайт, отправляется письмо или приложение подключается к серверу, сначала **DNS** (Domain Name System) превращает имя в адрес. Если DNS не работает, кажется, что «не работает Интернет», хотя все кабели на месте. DNS — «телефонная книга» Интернета и его важнейшая распределённая база данных.

:::note
**После этого занятия вы сможете:**

- объяснять структуру Интернета из провайдеров и автономных систем;
- описывать иерархию доменных имён и понятие зоны;
- пошагово объяснять, как имя разрешается рекурсивными и итеративными запросами;
- знать основные типы ресурсных записей и их назначение;
- проверять записи DNS командами `nslookup` или `dig`.
:::

## Структура Интернета

Интернет — не единая сеть, а объединение **автономных систем** (AS). Каждая AS — сеть провайдера, университета или крупной компании — имеет свой номер (ASN). Провайдеры делятся на уровни: **Tier 1** — владеют магистралями по всему миру и обмениваются трафиком друг с другом бесплатно; **Tier 2** — региональные; **Tier 3** — обслуживают конечных пользователей. Сети соединяются напрямую в точках **IXP** (Internet Exchange Point) и обмениваются маршрутами через **BGP**. Адреса и ASN распределяют **IANA** и региональные регистраторы (для Узбекистана — **RIPE NCC**).

## Иерархия доменных имён

```text
                              . (корень)
          ┌──────────┬────────┼─────────┬──────────┐
         com        org      uz        net        edu        ← домены верхнего уровня (TLD)
          │                  │
       google              aifu                                ← второй уровень
          │                  │
         www         www   lms   mail                          ← узлы / поддомены
```

Имя читается справа налево: `lms.aifu.uz.` — корень (`.`), TLD `uz`, домен `aifu`, узел `lms`. Точка в конце полного имени (FQDN) обозначает корень. Виды TLD: **gTLD** (общие: .com, .org, .net, .info), **ccTLD** (страновые: .uz, .ru, .de), новые gTLD (.app, .dev). Зоной `.uz` в Узбекистане управляет **UZINFOCOM**.

**Зона** — часть пространства имён, которой управляет одна организация. Владелец `aifu.uz` сам задаёт записи своей зоны, а в зоне `uz` хранится лишь «про aifu.uz спрашивайте такие-то серверы» (**делегирование**, записи NS).

## DNS-серверы и разрешение имени

| Сервер | Назначение |
|---|---|
| **Рекурсивный резолвер** (провайдер, 8.8.8.8, 1.1.1.1) | находит для клиента полный ответ и кэширует его |
| **Корневые серверы** | 13 имён (a…m.root-servers.net), сотни копий по миру; указывают на серверы TLD |
| **Серверы TLD** | указывают на авторитетные серверы домена |
| **Авторитетный** (authoritative) сервер | первоисточник записей зоны |

Разрешение `lms.aifu.uz`:

1. Браузер и ОС проверяют **кэш** (и файл `hosts`).
2. Компьютер отправляет рекурсивному резолверу **рекурсивный** запрос: «дай полный ответ».
3. Если в кэше резолвера ничего нет, он спрашивает **итеративно**: корень → «про `uz` спроси такой-то сервер»; сервер TLD `uz` → «про `aifu.uz` спроси ns1.aifu.uz»; авторитетный сервер → «`lms.aifu.uz` = 185.74.5.10».
4. Резолвер возвращает ответ клиенту и кэширует его на время **TTL** записи.

Запросы DNS обычно идут по **UDP 53**, большие ответы и передача зон (AXFR) — по **TCP 53**.

## Ресурсные записи

| Тип | Назначение | Пример |
|---|---|---|
| **A** | имя → IPv4 | `aifu.uz. A 185.74.5.10` |
| **AAAA** | имя → IPv6 | `aifu.uz. AAAA 2a00:1d40::10` |
| **CNAME** | псевдоним → каноническое имя | `www.aifu.uz. CNAME aifu.uz.` |
| **MX** | почтовый сервер домена (с приоритетом) | `aifu.uz. MX 10 mail.aifu.uz.` |
| **NS** | авторитетные серверы зоны | `aifu.uz. NS ns1.aifu.uz.` |
| **PTR** | IP → имя (обратная зона) | `10.5.74.185.in-addr.arpa. PTR aifu.uz.` |
| **TXT** | текст: SPF, подтверждение владения доменом | `aifu.uz. TXT "v=spf1 mx -all"` |
| **SOA** | параметры зоны: основной сервер, серийный номер, таймеры | — |

**TTL** каждой записи (в секундах) — сколько её можно кэшировать. Малый TTL — изменения распространяются быстро, но нагрузка на серверы больше; большой TTL — наоборот.

## Сообщение DNS и кодирование имени

Сообщение DNS: 12-байтный заголовок (ID, флаги, число вопросов и ответов), затем раздел **вопроса** и разделы **ответа**, **полномочий** и **дополнительный**. Имя кодируется не как текст, а как последовательность **меток**: перед каждой меткой стоит её длина (1 байт), в конце — `00`, обозначающий корень. Метка — не более 63 байт, всё имя — не более 255.

## Разобранный пример 1: кодирование имени в формате DNS

В каждой строке — доменное имя. Выведите, как оно выглядит в сообщении DNS (байты в шестнадцатеричном виде), и число байтов. Если метка пуста или длиннее 63 символов либо содержит что-то кроме букв, цифр и `-`, выведите `неверный`. Точка в конце (корень) допускается.

```python
import sys

for nom in sys.stdin.read().split():
    toza = nom[:-1] if nom.endswith(".") else nom
    yorliqlar = toza.split(".")
    togri = all(0 < len(y) <= 63 and all(c.isascii() and (c.isalnum() or c == "-") for c in y)
                for y in yorliqlar)
    if not togri:
        print(nom, "неверный")
        continue
    baytlar = []
    for y in yorliqlar:
        baytlar.append(len(y))                 # длина метки
        baytlar.extend(ord(c) for c in y)
    baytlar.append(0)                          # корень
    print(nom, " ".join(format(b, "02x") for b in baytlar), "(" + str(len(baytlar)) + ")")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String toza = nom.endsWith(".") ? nom.substring(0, nom.length() - 1) : nom;
            String[] yorliqlar = toza.split("\\.", -1);
            boolean togri = true;
            for (String y : yorliqlar) {
                if (y.isEmpty() || y.length() > 63 || !y.matches("[A-Za-z0-9-]+")) togri = false;
            }
            if (!togri) {
                System.out.println(nom + " неверный");
                continue;
            }
            List<Integer> baytlar = new ArrayList<>();
            for (String y : yorliqlar) {
                baytlar.add(y.length());                       // длина метки
                for (char c : y.toCharArray()) baytlar.add((int) c);
            }
            baytlar.add(0);                                    // корень
            StringBuilder hex = new StringBuilder();
            for (int b : baytlar) hex.append(hex.length() > 0 ? " " : "").append(String.format("%02x", b));
            System.out.println(nom + " " + hex + " (" + baytlar.size() + ")");
        }
    }
}
```
```cpp
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string nom;
    while (cin >> nom) {
        string toza = !nom.empty() && nom.back() == '.' ? nom.substr(0, nom.size() - 1) : nom;
        vector<string> yorliqlar;
        size_t boshi = 0;
        while (true) {
            size_t nuqta = toza.find('.', boshi);
            yorliqlar.push_back(toza.substr(boshi, nuqta == string::npos ? string::npos : nuqta - boshi));
            if (nuqta == string::npos) break;
            boshi = nuqta + 1;
        }
        bool togri = true;
        for (auto& y : yorliqlar) {
            if (y.empty() || y.size() > 63) togri = false;
            for (unsigned char c : y) {
                if (!(isalnum(c) || c == '-') || c > 127) togri = false;
            }
        }
        if (!togri) {
            cout << nom << " неверный" << endl;
            continue;
        }
        vector<int> baytlar;
        for (auto& y : yorliqlar) {
            baytlar.push_back(y.size());                       // длина метки
            for (unsigned char c : y) baytlar.push_back(c);
        }
        baytlar.push_back(0);                                  // корень
        string hex;
        char buf[4];
        for (size_t i = 0; i < baytlar.size(); i++) {
            snprintf(buf, sizeof buf, "%02x", baytlar[i]);
            hex += (i ? " " : "") + string(buf);
        }
        cout << nom << " " << hex << " (" << baytlar.size() << ")" << endl;
    }
    return 0;
}
```

Ввод:

```text
www.aifu.uz
aifu.uz.
mail..aifu.uz
my_site.uz
```

Вывод:

```text
www.aifu.uz 03 77 77 77 04 61 69 66 75 02 75 7a 00 (13)
aifu.uz. 04 61 69 66 75 02 75 7a 00 (9)
mail..aifu.uz неверный
my_site.uz неверный
```

Точки в сообщение не записываются — на их месте стоят байты длины. `_` в `my_site` в имени узла недопустим (исключение — некоторые служебные записи, например `_dmarc`).

## Разобранный пример 2: разрешение цепочки CNAME

В первой строке — число записей *k*, затем *k* записей: имя, тип (`A` или `CNAME`) и значение. Затем *q* запросов — имена. Для каждого запроса пройдите по цепочке CNAME до записи A и выведите путь. Если запись не найдена — `NXDOMAIN`; если цепочка длиннее 8 шагов — `петля`.

```python
k = int(input())
cname = {}
a = {}
for _ in range(k):
    nom, tur, qiymat = input().split()
    if tur == "CNAME":
        cname[nom] = qiymat
    else:
        a[nom] = qiymat
q = int(input())
for _ in range(q):
    nom = input().strip()
    yol = [nom]
    while nom in cname and len(yol) <= 8:
        nom = cname[nom]                       # от псевдонима к каноническому имени
        yol.append(nom)
    if len(yol) > 8:
        print(" -> ".join(yol[:3]) + " ... петля")
    elif nom in a:
        print(" -> ".join(yol) + " = " + a[nom])
    else:
        print(" -> ".join(yol) + " NXDOMAIN")
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
        Map<String, String> cname = new HashMap<>();
        Map<String, String> a = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String nom = in.next();
            String tur = in.next();
            String qiymat = in.next();
            if (tur.equals("CNAME")) cname.put(nom, qiymat);
            else a.put(nom, qiymat);
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String nom = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(nom);
            while (cname.containsKey(nom) && yol.size() <= 8) {
                nom = cname.get(nom);                              // от псевдонима к каноническому имени
                yol.add(nom);
            }
            if (yol.size() > 8) {
                System.out.println(String.join(" -> ", yol.subList(0, 3)) + " ... петля");
            } else if (a.containsKey(nom)) {
                System.out.println(String.join(" -> ", yol) + " = " + a.get(nom));
            } else {
                System.out.println(String.join(" -> ", yol) + " NXDOMAIN");
            }
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

string birlashtir(const vector<string>& v, size_t n) {
    string s;
    for (size_t i = 0; i < n; i++) s += (i ? " -> " : "") + v[i];
    return s;
}

int main() {
    int k;
    cin >> k;
    map<string, string> cname, a;
    for (int i = 0; i < k; i++) {
        string nom, tur, qiymat;
        cin >> nom >> tur >> qiymat;
        if (tur == "CNAME") cname[nom] = qiymat;
        else a[nom] = qiymat;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string nom;
        cin >> nom;
        vector<string> yol = {nom};
        while (cname.count(nom) && yol.size() <= 8) {
            nom = cname[nom];                                      // от псевдонима к каноническому имени
            yol.push_back(nom);
        }
        if (yol.size() > 8) {
            cout << birlashtir(yol, 3) << " ... петля" << endl;
        } else if (a.count(nom)) {
            cout << birlashtir(yol, yol.size()) << " = " << a[nom] << endl;
        } else {
            cout << birlashtir(yol, yol.size()) << " NXDOMAIN" << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
6
aifu.uz A 185.74.5.10
www.aifu.uz CNAME aifu.uz
lms.aifu.uz CNAME cdn.aifu.uz
cdn.aifu.uz A 185.74.5.20
x.aifu.uz CNAME y.aifu.uz
y.aifu.uz CNAME x.aifu.uz
4
www.aifu.uz
lms.aifu.uz
eski.aifu.uz
x.aifu.uz
```

Вывод:

```text
www.aifu.uz -> aifu.uz = 185.74.5.10
lms.aifu.uz -> cdn.aifu.uz = 185.74.5.20
eski.aifu.uz NXDOMAIN
x.aifu.uz -> y.aifu.uz -> x.aifu.uz ... петля
```

Настоящие резолверы тоже ограничивают длину цепочки CNAME — иначе неправильно настроенная зона загнала бы резолвер в бесконечный цикл. NXDOMAIN — официальный ответ DNS «такого имени не существует».

:::warning
**Частые ошибки**

- **Думать, что DNS работает только по UDP.** Большие ответы, DNSSEC и передача зон используют TCP 53.
- **Ожидать, что после изменения записи все сразу увидят новый адрес.** Старый ответ живёт в кэшах, пока не истечёт TTL.
- **Ставить CNAME на сам домен (`aifu.uz`).** На вершине зоны (apex) есть SOA и NS, поэтому CNAME там не допускается.
- **Путать рекурсивный и итеративный запросы.** Клиент спрашивает резолвер рекурсивно (полный ответ); резолвер спрашивает корневые, TLD и авторитетные серверы итеративно.
- **Считать DNS защищённым.** Обычный DNS не шифруется и может быть подделан; защита — DNSSEC, DoH/DoT.
:::

:::tip
Выполните в командной строке `nslookup aifu.uz`, `nslookup -type=MX gmail.com`, `nslookup -type=NS uz` (в Linux/macOS `dig aifu.uz +trace` покажет весь итеративный путь). Посмотрите кэш DNS Windows командой `ipconfig /displaydns` и очистите его командой `ipconfig /flushdns`.
:::

## Итоги

- Интернет — иерархия автономных систем и провайдеров, связанных через BGP; прямой обмен в IXP.
- Доменные имена образуют иерархию корень → TLD → домен → узел; зоны делегируются записями NS.
- Разрешение: кэш → рекурсивный резолвер → корень → TLD → авторитетный сервер; ответ кэшируется на время TTL.
- Записи: A, AAAA, CNAME, MX, NS, PTR, TXT, SOA; транспорт — UDP 53 (для больших ответов TCP 53).
- Имя кодируется в сообщении метками с префиксом длины: метка ≤ 63 байт, в конце `00`.

## Проверьте себя

:::check
Q: Чем отличаются рекурсивный и итеративный DNS-запросы?
A: При рекурсивном сервер находит для клиента полный ответ; при итеративном каждый сервер сообщает, какой сервер спросить следующим.
Q: В какой записи указывается почтовый сервер домена?
A: В записи MX.
Q: Изменили запись A с TTL = 3600. Сколько ещё может быть виден старый адрес?
A: До часа — сохранённый в кэше ответ используется, пока не истечёт TTL.
Q: Какими байтами кодируется имя `mail.uz` в сообщении DNS?
A: `04 6d 61 69 6c 02 75 7a 00`.
Q: Какая запись используется для поиска имени по IP-адресу?
A: Запись PTR (в обратной зоне in-addr.arpa).
:::

:::link https://www.cloudflare.com/learning/dns/what-is-dns/
Cloudflare: What is DNS?
Иллюстрированное объяснение работы DNS (на английском).
:::
