---
title: Настройка брандмауэра в операционной системе Windows. Использование службы VPN
summary: Брандмауэр Защитника Windows — профили (доменный, частный, общедоступный), правила для входящих и исходящих подключений, wf.msc, netsh advfirewall и PowerShell; проверка правил и журнал; NAT и PAT — почему NAT тоже блокирует входящие подключения; клиент VPN в Windows (IKEv2, SSTP, L2TP/IPsec), WireGuard, RRAS в Windows Server; проверка VPN-подключения.
---

## Почему это важно

На лекции M14 мы рассмотрели теорию межсетевых экранов и VPN. Теперь настроим их на практике: сервер должен открывать только нужные порты, ноутбук в Wi-Fi кафе — оставаться закрытым с профилем «общедоступная сеть», а преподаватель из дома — подключаться к сети университета через зашифрованный туннель. Также увидим с помощью программы, что **NAT** домашнего роутера тоже играет защитную роль.

:::note
**После этого занятия вы сможете:**

- объяснять профили брандмауэра Защитника Windows и порядок правил;
- создавать правила для входящих и исходящих подключений графически, через `netsh` и PowerShell;
- проверять правило и читать журнал брандмауэра;
- объяснять, как работает таблица NAT/PAT и почему она блокирует неожиданные входящие пакеты;
- настраивать VPN-подключение в Windows и проверять его.
:::

## Брандмауэр Защитника Windows

Встроенный в Windows межсетевой экран **с отслеживанием состояния**. В зависимости от типа сети он использует три **профиля**:

| Профиль | Когда | Типичная настройка |
|---|---|---|
| **Domain** | компьютер в сети домена (DC найден) | управляется GPO |
| **Private** | домашняя или доверенная сеть | общий доступ к файлам и обнаружение могут быть разрешены |
| **Public** | Wi-Fi кафе, аэропорта, гостиницы | самый строгий: входящие подключения закрыты |

По умолчанию: **входящие** — всё, что не разрешено, запрещено; **исходящие** — всё, что не запрещено, разрешено. Типы правил: для программы, для порта, предопределённые (например, «File and Printer Sharing»), настраиваемые. В правиле: направление, действие (Allow/Block), протокол и порт, адреса источника и назначения, профиль. **Правило Block сильнее Allow.**

Средство: **wf.msc** (Windows Defender Firewall with Advanced Security).

## Создание правил

```text
netsh advfirewall show allprofiles                         состояние профилей
netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80
netsh advfirewall firewall add rule name="Block Telnet" dir=in action=block protocol=TCP localport=23
netsh advfirewall firewall show rule name="Web 80"
netsh advfirewall firewall delete rule name="Web 80"
```

PowerShell:

```text
Get-NetFirewallProfile | Select-Object Name, Enabled, DefaultInboundAction
New-NetFirewallRule -DisplayName "RDP только из лаборатории" -Direction Inbound -Protocol TCP -LocalPort 3389 -RemoteAddress 192.168.50.0/24 -Action Allow
New-NetFirewallRule -DisplayName "ICMP Echo" -Direction Inbound -Protocol ICMPv4 -IcmpType 8 -Action Allow
Get-NetFirewallRule -Enabled True -Direction Inbound | Measure-Object
Set-NetFirewallProfile -Profile Domain,Public,Private -LogBlocked True -LogFileName "%systemroot%\system32\LogFiles\Firewall\pfirewall.log"
```

**Проверка**: с другого компьютера `Test-NetConnection 192.168.50.10 -Port 80` (`TcpTestSucceeded : True/False`) и `ping`. В **журнал** (`pfirewall.log`) записываются заблокированные пакеты со временем, источником и портом.

## Практическая работа 1: защита сервера

1. Откройте `wf.msc` на `SRV-DC01` и убедитесь, что все три профиля включены.
2. Добавьте разрешение входящих на **TCP 80** для IIS или простого веб-сервера; проверьте с клиента браузером.
3. Разрешите RDP (3389) только из подсети лаборатории (`192.168.50.0/24`).
4. Включите и выключите ICMP Echo — сравните результат `ping` на клиенте.
5. Включите запись заблокированных пакетов в журнал, попробуйте с клиента подключиться к закрытому порту (`Test-NetConnection -Port 23`) и найдите запись в журнале.
6. Экспортируйте список правил: `netsh advfirewall export C:\fw-backup.wfw`.

Программа, проверяющая список правил по принципу «первое подходящее», — самостоятельное задание этого занятия (KOT-A14).

## NAT и PAT

Домашний или офисный роутер заменяет внутренние частные адреса (192.168.x.x) одним публичным — **PAT** (Port Address Translation, «NAT overload»):

1. Исходящий пакет изнутри: источник `192.168.1.10:51000` → роутер заменяет его на `203.0.113.5:50000` и записывает в **таблицу NAT**.
2. Ответ приходит на `203.0.113.5:50000` → роутер находит его в таблице и доставляет на `192.168.1.10:51000`.
3. Внешний пакет без записи в таблице **отбрасывается**: ни один компьютер внутри его не ждал.

Поэтому к компьютеру за NAT нельзя напрямую подключиться снаружи (если не настроен **проброс портов** — статическая запись). Это защищающий побочный эффект, но он не заменяет межсетевой экран.

## Разобранный пример: таблица PAT

В первой строке — публичный IP-адрес роутера и первый внешний порт. В следующих строках — события: `OUT внутр_ip внутр_порт внешн_ip внешн_порт` (исходящий пакет изнутри) или `IN внешн_ip внешн_порт порт_роутера` (пакет снаружи). Для исходящего пакета одной и той же паре внутреннего адреса и порта выдаётся один и тот же порт роутера, новой — следующий порт. Входящий пакет доставьте по таблице на внутренний адрес или отбросьте (нет записи или пакет пришёл с другого сервера).

```python
import sys

satrlar = sys.stdin.read().strip().split("\n")
ommaviy, keyingi = satrlar[0].split()
keyingi = int(keyingi)
jadval = {}                               # (внутр_ip, внутр_порт) -> порт маршрутизатора
teskari = {}                              # порт маршрутизатора -> (внутр_ip, внутр_порт, server_ip, server_port)
for satr in satrlar[1:]:
    q = satr.split()
    if q[0] == "OUT":
        kalit = (q[1], q[2])
        if kalit not in jadval:
            jadval[kalit] = keyingi       # новая запись
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
            print(q[1] + ":" + q[2] + " -> " + ommaviy + ":" + str(port) + " отброшен")
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
        Map<String, Integer> jadval = new HashMap<>();             // "внутр_ip внутр_порт" -> порт маршрутизатора
        Map<Integer, String[]> teskari = new HashMap<>();          // порт маршрутизатора -> {внутр_ip, внутр_порт, server_ip, server_port}
        while (in.hasNext()) {
            String tur = in.next();
            if (tur.equals("OUT")) {
                String ip = in.next(), ichkiPort = in.next(), server = in.next(), serverPort = in.next();
                String kalit = ip + " " + ichkiPort;
                if (!jadval.containsKey(kalit)) {
                    jadval.put(kalit, keyingi);                     // новая запись
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
                    System.out.println(server + ":" + serverPort + " -> " + ommaviy + ":" + port + " отброшен");
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
    map<pair<string, string>, int> jadval;                         // (внутр_ip, внутр_порт) -> порт маршрутизатора
    map<int, array<string, 4>> teskari;                            // порт маршрутизатора -> {внутр_ip, внутр_порт, server_ip, server_port}
    string tur;
    while (cin >> tur) {
        if (tur == "OUT") {
            string ip, ichkiPort, server, serverPort;
            cin >> ip >> ichkiPort >> server >> serverPort;
            pair<string, string> kalit = {ip, ichkiPort};
            if (!jadval.count(kalit)) {
                jadval[kalit] = keyingi;                           // новая запись
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
                cout << server << ":" << serverPort << " -> " << ommaviy << ":" << port << " отброшен" << endl;
            }
        }
    }
    return 0;
}
```

Ввод:

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

Вывод:

```text
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
192.168.1.11:51000 -> 203.0.113.5:50001 -> 142.250.74.46:443
8.8.8.8:53 -> 203.0.113.5:50000 -> 192.168.1.10:51000
142.250.74.46:443 -> 203.0.113.5:50001 -> 192.168.1.11:51000
192.168.1.10:51000 -> 203.0.113.5:50000 -> 8.8.8.8:53
198.51.100.66:443 -> 203.0.113.5:50001 отброшен
198.51.100.66:3389 -> 203.0.113.5:50002 отброшен
```

Хотя оба компьютера используют один и тот же внутренний порт (51000), роутер различил их разными внешними портами. Последние два пакета: один — на существующий порт, но с **другого** сервера (попытка поддельного ответа), второй — на порт вообще без записи (сканирование RDP); оба отброшены.

## Подключение к VPN

**Клиент Windows**: Параметры → Сеть и Интернет → **VPN** → «Добавить VPN-подключение»: поставщик — Windows (встроенный), имя сервера (`vpn.aifu.uz`), тип VPN (**IKEv2** — рекомендуется, **SSTP** — через порт 443, **L2TP/IPsec**), тип данных для входа (имя и пароль, сертификат). Другие варианты — клиенты **WireGuard** или OpenVPN (импортируется файл конфигурации).

**Сторона сервера**: в Windows Server — роль **Remote Access → RRAS** (Routing and Remote Access), в Linux — WireGuard или OpenVPN. В межсетевом экране открываются соответствующие порты: IKEv2 — UDP 500 и 4500, SSTP — TCP 443, WireGuard — UDP 51820.

**Проверка**:

```text
rasdial "AIFU VPN" логин пароль     подключение из командной строки
ipconfig /all                       адаптер VPN и выданный ему внутренний адрес
route print                         какой трафик идёт через туннель
ping 192.168.50.10                  доступ к внутреннему серверу
tracert 192.168.50.10               первый переход — VPN-сервер
rasdial "AIFU VPN" /disconnect
```

**Раздельное туннелирование** (split tunneling): через туннель идёт только трафик внутренней сети, остальное — напрямую в Интернет. Экономно, но политика безопасности иногда требует направлять через туннель весь трафик (full tunnel).

:::warning
**Частые ошибки**

- **Полностью отключать брандмауэр, потому что «не работает».** Правильно — добавить правило именно для нужного порта.
- **Выбирать профиль «Частная сеть» в общественном Wi-Fi.** Общий доступ к файлам и обнаружение открываются посторонним.
- **Не ограничивать адрес источника в правиле.** Вместо открытия RDP для «Any» укажите только нужную подсеть.
- **Считать NAT межсетевым экраном.** Атаки приходят напрямую через порты, открытые пробросом или UPnP.
- **Использовать VPN PPTP.** Устаревший и взломанный протокол; используйте IKEv2, SSTP или WireGuard.
:::

:::tip
На странице управления домашним роутером (только своим!) найдите таблицу NAT или раздел «Подключённые устройства». Посмотрите открытые соединения на компьютере командой `netstat -n` и сравните их внутренние порты с внешними портами в таблице роутера (если роутер их показывает).
:::

## Итоги

- Брандмауэр Защитника Windows — с отслеживанием состояния; профили Domain, Private, Public; входящие по умолчанию запрещены, Block сильнее Allow.
- Правила: `wf.msc`, `netsh advfirewall`, `New-NetFirewallRule`; проверка — `Test-NetConnection`, журнал — `pfirewall.log`.
- PAT заменяет внутренний адрес:порт на публичный адрес:порт; входящие пакеты без записи в таблице отбрасываются.
- VPN: клиент Windows (IKEv2, SSTP, L2TP), WireGuard, RRAS; проверка — `ipconfig`, `route print`, `tracert`.

## Проверьте себя

:::check
Q: Какой профиль брандмауэра Защитника Windows самый строгий и когда используется?
A: Public — в общественных сетях (Wi-Fi кафе, аэропорта).
Q: Как добавить разрешение входящих на порт 80 через `netsh`?
A: `netsh advfirewall firewall add rule name="Web 80" dir=in action=allow protocol=TCP localport=80`.
Q: Почему к компьютеру за NAT нельзя подключиться снаружи?
A: В таблице NAT роутера нет записи для такого подключения — он не знает, какому внутреннему компьютеру отдать пакет, и отбрасывает его.
Q: Как проверить действие правил брандмауэра?
A: Командой `Test-NetConnection server -Port N` с другого компьютера и просмотром журнала брандмауэра.
Q: Что такое раздельное туннелирование?
A: В VPN — отправка через туннель только трафика внутренней сети, а остального — напрямую в Интернет.
:::

:::link https://learn.microsoft.com/en-us/windows/security/operating-system-security/network-security/windows-firewall/
Windows Firewall
Официальная документация Microsoft по брандмауэру Windows (на английском).
:::
