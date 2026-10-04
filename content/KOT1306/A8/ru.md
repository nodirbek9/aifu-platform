---
title: Управление маршрутизаторами
summary: Состав маршрутизатора (CPU, RAM, NVRAM, Flash, ROM) и процесс загрузки; подключение через консоль, SSH и Telnet; режимы Cisco IOS и основные команды; настройка интерфейсов; защита паролями и SSH; статические маршруты и маршрут по умолчанию; чтение вывода show ip route; сохранение конфигурации; трассировка пути пакета по таблицам маршрутизации.
---

## Почему это важно

Маршрутизатор — «перекрёсток» между сетями. Если он настроен неправильно, целый отдел остаётся без Интернета; если оставлен без пароля, посторонний может захватить управление всей сетью. Повседневная работа сетевого инженера — подключиться к маршрутизатору, настроить интерфейсы, добавить маршруты и найти причину неисправности командами `show`. На этом занятии освоим эти навыки на примере Cisco IOS.

:::note
**После этого занятия вы сможете:**

- называть виды памяти маршрутизатора и последовательность загрузки;
- подключаться через консоль или SSH и переходить между режимами IOS;
- назначать интерфейсам IP-адреса и добавлять статические маршруты и маршрут по умолчанию;
- защищать маршрутизатор паролями и SSH;
- читать вывод `show ip route` и определять, каким путём пойдёт пакет.
:::

## Состав маршрутизатора

| Часть | Что хранится |
|---|---|
| **ROM** | загрузчик (bootstrap), тест POST, аварийный режим ROMMON |
| **Flash** | образ операционной системы IOS (файл `.bin`) |
| **NVRAM** | **startup-config** — конфигурация, загружаемая при включении |
| **RAM** | работающая IOS, **running-config**, таблицы маршрутизации и ARP, буферы пакетов |

**Загрузка**: POST (проверка оборудования) → bootstrap из ROM → загрузка IOS из Flash в RAM → копирование startup-config из NVRAM в running-config. Если startup-config нет — появляется диалог начальной настройки.

Важно: вводимые команды сразу записываются в **running-config** (RAM). Без `copy running-config startup-config` (или `write memory`) при перезагрузке они **пропадут**.

## Способы подключения

| Способ | Как | Когда |
|---|---|---|
| **Консоль** | специальный консольный кабель, терминальная программа (PuTTY, 9600 бод) | первая настройка, когда сеть не работает |
| **SSH** | по сети, с шифрованием | повседневное удалённое управление |
| **Telnet** | по сети, **без шифрования** | не использовать — пароль виден открытым текстом |
| **AUX** | через модем | устаревший способ |

## Режимы IOS и основные команды

```text
Router>                       пользовательский режим (только просмотр)
Router> enable
Router#                       привилегированный режим (show, copy, reload, debug)
Router# configure terminal
Router(config)#               глобальная настройка
Router(config)# interface g0/0
Router(config-if)#            настройка интерфейса
Router(config-if)# exit       на уровень выше;  end или Ctrl+Z — в привилегированный режим
```

| Команда | Что показывает |
|---|---|
| `show ip interface brief` | интерфейсы, IP-адреса, состояние (up/down) |
| `show ip route` | таблицу маршрутизации |
| `show running-config` | текущую конфигурацию |
| `show version` | версию IOS, время работы, память |
| `show interfaces g0/0` | статистику интерфейса, ошибки |
| `show cdp neighbors` | соседние устройства Cisco |

Состояния интерфейса: **up/up** — работает; **administratively down/down** — не дана команда `no shutdown`; **up/down** — кабель есть, но проблема на уровне 2; **down/down** — нет кабеля или устройства на другом конце.

## Защита

```text
Router(config)# hostname R1
R1(config)# enable secret Kuchli#Parol2026          пароль привилегированного режима (хешируется)
R1(config)# service password-encryption               скрыть остальные пароли
R1(config)# line console 0
R1(config-line)# password KonsolParol
R1(config-line)# login
R1(config-line)# exit
R1(config)# ip domain-name aifu.local
R1(config)# crypto key generate rsa modulus 2048      ключи SSH
R1(config)# username admin secret AdminParol
R1(config)# line vty 0 4
R1(config-line)# login local
R1(config-line)# transport input ssh                  только SSH, Telnet закрыт
R1(config)# banner motd #Только для авторизованных пользователей#
```

Используйте **`enable secret`**, а не `enable password` — второй хранится в конфигурации открытым текстом.

## Маршруты

```text
R1(config)# ip route 10.20.0.0 255.255.0.0 192.168.1.2     статический маршрут: сеть маска следующий_шаг
R1(config)# ip route 0.0.0.0 0.0.0.0 203.0.113.1           маршрут по умолчанию (default)
```

Коды в выводе `show ip route`: **C** — непосредственно подключённая, **L** — собственный адрес интерфейса (/32), **S** — статический, **R** — RIP, **O** — OSPF, **\*** — кандидат в маршрут по умолчанию. `[1/0]` в записи — [административное расстояние/метрика].

Для каждого пакета маршрутизатор выбирает из таблицы **самый длинный совпадающий префикс**; программа такого выбора — самостоятельное задание этого занятия (KOT-A8).

## Практическая работа: три маршрутизатора

1. В Packet Tracer соедините R1, R2, R3 цепочкой (R1–R2: 10.0.12.0/30, R2–R3: 10.0.23.0/30). За R1 — LAN-A (192.168.1.0/24), за R3 — LAN-B (192.168.3.0/24).
2. Настройте интерфейсы на каждом маршрутизаторе (`ip address`, `no shutdown`) и проверьте командой `show ip interface brief`.
3. Добавьте на R1 и R3 статический маршрут к противоположной LAN, на R2 — маршруты к обеим LAN.
4. Выполните `ping` и `tracert` с компьютера в LAN-A до LAN-B.
5. Удалите один маршрут на R2 (`no ip route ...`) и посмотрите, где остановится `tracert`.
6. Сохраните конфигурацию на всех маршрутизаторах.

## Разобранный пример 1: трассировка пути пакета

В первой строке — число записей *k*, в следующих *k* строках: маршрутизатор, имя сети назначения и следующий шаг (`direct` — сеть подключена к этому маршрутизатору непосредственно; назначение `default` — маршрут по умолчанию). Затем *q* запросов: начальный маршрутизатор и сеть назначения. Пакет отправляется с TTL = 6; каждая пересылка уменьшает TTL на 1. Выведите путь и результат: `доставлено`, `нет маршрута` или `TTL истёк`.

```python
k = int(input())
jadval = {}                                   # (маршрутизатор, сеть) -> следующий шаг
for _ in range(k):
    router, tarmoq, keyingi = input().split()
    jadval[(router, tarmoq)] = keyingi
q = int(input())
for _ in range(q):
    joriy, tarmoq = input().split()
    yol = [joriy]
    ttl = 6
    while True:
        keyingi = jadval.get((joriy, tarmoq), jadval.get((joriy, "default")))
        if keyingi == "direct":
            natija = "доставлено"
            break
        if keyingi is None:
            natija = "нет маршрута"
            break
        if ttl == 0:
            natija = "TTL истёк"             # пакет в петле отбрасывается
            break
        ttl -= 1
        joriy = keyingi
        yol.append(joriy)
    print(" -> ".join(yol) + " (" + natija + ")")
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
        Map<String, String> jadval = new HashMap<>();          // "маршрутизатор сеть" -> следующий шаг
        for (int i = 0; i < k; i++) {
            String router = in.next();
            String tarmoq = in.next();
            jadval.put(router + " " + tarmoq, in.next());
        }
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            String joriy = in.next();
            String tarmoq = in.next();
            List<String> yol = new ArrayList<>();
            yol.add(joriy);
            int ttl = 6;
            String natija;
            while (true) {
                String keyingi = jadval.getOrDefault(joriy + " " + tarmoq, jadval.get(joriy + " default"));
                if ("direct".equals(keyingi)) {
                    natija = "доставлено";
                    break;
                }
                if (keyingi == null) {
                    natija = "нет маршрута";
                    break;
                }
                if (ttl == 0) {
                    natija = "TTL истёк";                      // пакет в петле отбрасывается
                    break;
                }
                ttl--;
                joriy = keyingi;
                yol.add(joriy);
            }
            System.out.println(String.join(" -> ", yol) + " (" + natija + ")");
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

int main() {
    int k;
    cin >> k;
    map<pair<string, string>, string> jadval;                // (маршрутизатор, сеть) -> следующий шаг
    for (int i = 0; i < k; i++) {
        string router, tarmoq, keyingi;
        cin >> router >> tarmoq >> keyingi;
        jadval[{router, tarmoq}] = keyingi;
    }
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string joriy, tarmoq;
        cin >> joriy >> tarmoq;
        vector<string> yol = {joriy};
        int ttl = 6;
        string natija;
        while (true) {
            string keyingi;
            if (jadval.count({joriy, tarmoq})) keyingi = jadval[{joriy, tarmoq}];
            else if (jadval.count({joriy, "default"})) keyingi = jadval[{joriy, "default"}];
            if (keyingi == "direct") {
                natija = "доставлено";
                break;
            }
            if (keyingi.empty()) {
                natija = "нет маршрута";
                break;
            }
            if (ttl == 0) {
                natija = "TTL истёк";                          // пакет в петле отбрасывается
                break;
            }
            ttl--;
            joriy = keyingi;
            yol.push_back(joriy);
        }
        string chiqish;
        for (size_t j = 0; j < yol.size(); j++) chiqish += (j ? " -> " : "") + yol[j];
        cout << chiqish << " (" << natija << ")" << endl;
    }
    return 0;
}
```

Ввод:

```text
7
R1 LAN-A direct
R1 LAN-B R2
R2 LAN-A R1
R2 LAN-B R3
R2 default R3
R3 LAN-B direct
R3 default R2
4
R1 LAN-B
R3 LAN-A
R1 LAN-C
R2 LAN-C
```

Вывод:

```text
R1 -> R2 -> R3 (доставлено)
R3 -> R2 -> R1 (доставлено)
R1 (нет маршрута)
R2 -> R3 -> R2 -> R3 -> R2 -> R3 -> R2 (TTL истёк)
```

Последняя строка — классическая **петля маршрутизации**: маршрут по умолчанию R2 указывает на R3, а R3 — на R2. Пакет в несуществующую LAN-C циркулирует между ними, пока не истечёт TTL. В реальной сети `tracert` показывает это как поочерёдное повторение одних и тех же двух адресов.

## Разобранный пример 2: генерация команд конфигурации

В первой строке — имя маршрутизатора, во второй — число интерфейсов *n*, затем *n* строк «интерфейс адрес/префикс», затем число маршрутов *m* и *m* строк «сеть/префикс следующий_шаг». Выведите последовательность команд для ввода в IOS (префикс преобразуется в маску).

```python
def maska(p):
    m = (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF if p > 0 else 0
    return ".".join(str((m >> s) & 255) for s in (24, 16, 8, 0))


nom = input().strip()
print("enable")
print("configure terminal")
print("hostname " + nom)
n = int(input())
for _ in range(n):
    interfeys, manzil = input().split()
    ip, p = manzil.split("/")
    print("interface " + interfeys)
    print(" ip address " + ip + " " + maska(int(p)))
    print(" no shutdown")
    print("exit")
m = int(input())
for _ in range(m):
    tarmoq, keyingi = input().split()
    ip, p = tarmoq.split("/")
    print("ip route " + ip + " " + maska(int(p)) + " " + keyingi)
print("end")
print("copy running-config startup-config")    # иначе пропадёт при перезагрузке
```
```java
import java.util.Scanner;

public class Main {
    static String maska(int p) {
        long m = p > 0 ? (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL : 0;
        return ((m >> 24) & 255) + "." + ((m >> 16) & 255) + "." + ((m >> 8) & 255) + "." + (m & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String nom = in.next();
        System.out.println("enable");
        System.out.println("configure terminal");
        System.out.println("hostname " + nom);
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String interfeys = in.next();
            String[] manzil = in.next().split("/");
            System.out.println("interface " + interfeys);
            System.out.println(" ip address " + manzil[0] + " " + maska(Integer.parseInt(manzil[1])));
            System.out.println(" no shutdown");
            System.out.println("exit");
        }
        int m = in.nextInt();
        for (int i = 0; i < m; i++) {
            String[] tarmoq = in.next().split("/");
            String keyingi = in.next();
            System.out.println("ip route " + tarmoq[0] + " " + maska(Integer.parseInt(tarmoq[1])) + " " + keyingi);
        }
        System.out.println("end");
        System.out.println("copy running-config startup-config");   // иначе пропадёт при перезагрузке
    }
}
```
```cpp
#include <cstdint>
#include <iostream>
#include <string>
using namespace std;

string maska(int p) {
    uint64_t m = p > 0 ? (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL : 0;
    return to_string((m >> 24) & 255) + "." + to_string((m >> 16) & 255) + "." + to_string((m >> 8) & 255) + "." + to_string(m & 255);
}

int main() {
    string nom;
    cin >> nom;
    cout << "enable" << endl;
    cout << "configure terminal" << endl;
    cout << "hostname " << nom << endl;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string interfeys, manzil;
        cin >> interfeys >> manzil;
        size_t chiziq = manzil.find('/');
        cout << "interface " << interfeys << endl;
        cout << " ip address " << manzil.substr(0, chiziq) << " " << maska(stoi(manzil.substr(chiziq + 1))) << endl;
        cout << " no shutdown" << endl;
        cout << "exit" << endl;
    }
    int m;
    cin >> m;
    for (int i = 0; i < m; i++) {
        string tarmoq, keyingi;
        cin >> tarmoq >> keyingi;
        size_t chiziq = tarmoq.find('/');
        cout << "ip route " << tarmoq.substr(0, chiziq) << " " << maska(stoi(tarmoq.substr(chiziq + 1))) << " " << keyingi << endl;
    }
    cout << "end" << endl;
    cout << "copy running-config startup-config" << endl;   // иначе пропадёт при перезагрузке
    return 0;
}
```

Ввод:

```text
R1
2
g0/0 192.168.1.1/24
g0/1 10.0.12.1/30
2
192.168.3.0/24 10.0.12.2
0.0.0.0/0 10.0.12.2
```

Вывод:

```text
enable
configure terminal
hostname R1
interface g0/0
 ip address 192.168.1.1 255.255.255.0
 no shutdown
exit
interface g0/1
 ip address 10.0.12.1 255.255.255.252
 no shutdown
exit
ip route 192.168.3.0 255.255.255.0 10.0.12.2
ip route 0.0.0.0 0.0.0.0 10.0.12.2
end
copy running-config startup-config
```

Такие «генераторы конфигураций» — простейшая форма автоматизации сети (Ansible, Python + Netmiko): в сети со многими маршрутизаторами ручной ввод команд ведёт к ошибкам, а шаблон всегда даёт одинаковый правильный результат.

:::warning
**Частые ошибки**

- **Забывать сохранить конфигурацию.** Без `copy running-config startup-config` при отключении питания всё пропадёт.
- **Не давать интерфейсу `no shutdown`.** Интерфейсы маршрутизаторов Cisco по умолчанию выключены (administratively down).
- **Использовать Telnet и `enable password`.** Пароли видны открытым текстом; используйте SSH и `enable secret`.
- **Указывать в статическом маршруте дальний маршрутизатор как следующий шаг.** Следующий шаг — адрес интерфейса непосредственно подключённого соседа.
- **Направлять маршруты по умолчанию двух маршрутизаторов друг на друга.** Это создаёт петлю маршрутизации (LAN-C в первом примере).
:::

:::tip
Выполнив практическую работу в Packet Tracer, скопируйте вывод `show ip route` на R2 и поясните для каждой строки код (C, L, S) и значения `[1/0]`. Затем включите `debug ip packet` (только в лаборатории!) и проследите, как маршрутизатор принимает решения во время `ping`.
:::

## Итоги

- Маршрутизатор: ROM (bootstrap), Flash (IOS), NVRAM (startup-config), RAM (running-config, таблицы).
- Подключение: консоль для первой настройки, затем только SSH; Telnet опасен.
- Режимы IOS: `>` → `enable` → `#` → `configure terminal` → `(config)#` → `interface` → `(config-if)#`.
- Основные команды: `show ip interface brief`, `show ip route`, `show running-config`; маршруты — `ip route сеть маска следующий_шаг`.
- Защита: `enable secret`, пароли консоли и VTY, `transport input ssh`, `service password-encryption`; в конце — сохранить конфигурацию.

## Проверьте себя

:::check
Q: Где хранятся running-config и startup-config?
A: Running-config — в RAM (текущая), startup-config — в NVRAM (загружается при включении).
Q: Что делать, если интерфейс в состоянии «administratively down»?
A: Дать команду `no shutdown` в режиме настройки интерфейса.
Q: Как записывается маршрут по умолчанию?
A: `ip route 0.0.0.0 0.0.0.0 <следующий_шаг>`.
Q: Почему используют `enable secret`, а не `enable password`?
A: `enable secret` хранит пароль в виде хеша, `enable password` — открытым текстом.
Q: Что означают коды `S` и `C` в `show ip route`?
A: S — статический маршрут, C — непосредственно подключённая сеть.
:::

:::link https://www.netacad.com/courses/ccna-introduction-networks
CCNA: Introduction to Networks
Бесплатный курс Cisco Networking Academy по настройке маршрутизаторов и коммутаторов (на английском).
:::
