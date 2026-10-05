---
title: Обнаружение сетевых неисправностей, использование утилит операционной системы
summary: Практическая работа с сетевыми утилитами Windows и Linux — ipconfig, ping, tracert, pathping, nslookup, arp, netstat, route, getmac, Test-NetConnection; интерпретация результатов; сценарии неисправностей (кабель, DHCP, шлюз, DNS, порт, маршрут); анализ вывода netstat и tracert программой; составление отчёта.
---

## Почему это важно

На лекции M15 мы изучили методологию устранения неисправностей. На этом занятии поработаем руками: найдём с помощью утилит неисправности, специально созданные в сети лаборатории. В реальной работе вывод этих команд может занимать сотни строк — поэтому научимся и анализировать его программой: администраторы пишут такие скрипты, чтобы автоматизировать ежедневные проверки.

:::note
**После этого занятия вы сможете:**

- использовать основные сетевые утилиты и интерпретировать их результаты;
- определять типичные сценарии неисправностей по признакам;
- получать из вывода `netstat` прослушиваемые порты и статистику соединений;
- определять по результату `tracert`, где появляется задержка;
- составлять краткий отчёт о неисправности.
:::

## Краткий справочник утилит

| Задача | Windows | Linux |
|---|---|---|
| Адрес и настройки | `ipconfig /all` | `ip addr`, `ip route`, `resolvectl status` |
| Обновить адрес | `ipconfig /release` + `/renew` | `sudo dhclient -r && sudo dhclient` |
| Связность | `ping -n 10 host` | `ping -c 10 host` |
| Путь | `tracert host` | `traceroute host` |
| Путь + потери | `pathping host` | `mtr host` |
| DNS | `nslookup host`, `ipconfig /flushdns` | `dig host`, `nslookup host` |
| Кэш ARP | `arp -a`, `arp -d *` | `ip neigh` |
| Соединения и порты | `netstat -ano`, `Get-NetTCPConnection` | `ss -tunap` |
| Таблица маршрутов | `route print` | `ip route` |
| MAC-адреса | `getmac /v` | `ip link` |
| Проверка TCP-порта | `Test-NetConnection host -Port 443` | `nc -zv host 443` |
| Имя компьютера | `hostname` | `hostname` |

## Сценарии неисправностей

Преподаватель создаёт в лаборатории следующие неисправности; для каждой заполните таблицу **признак → проверка → причина → решение**.

| № | Признак | Проверка | Обычная причина |
|---|---|---|---|
| 1 | «Network cable unplugged» | `ipconfig`, индикатор порта | кабель отключён или порт выключен |
| 2 | адрес 169.254.x.x | `ipconfig /all`, `/renew` | сервер DHCP не работает или недоступен |
| 3 | локальная сеть есть, Интернета нет | `ping` шлюза, `route print` | шлюз неверный или отсутствует |
| 4 | `ping 8.8.8.8` работает, сайты — нет | `nslookup` | неверный адрес DNS-сервера |
| 5 | сайт не открывается, ping работает | `Test-NetConnection -Port 443`, `netstat -ano` | порт закрыт, служба не работает, межсетевой экран |
| 6 | два компьютера не видят друг друга | сравнить маски в `ipconfig` | разные подсети или неверная маска |
| 7 | «Windows has detected an IP address conflict» | `arp -a`, `ping` | один и тот же статический IP на двух устройствах |
| 8 | Интернет очень медленный и обрывается | `pathping`, `ping -n 100` | потери, слабый Wi-Fi, перегруженный канал |

## Форма отчёта

```text
Дата и время:       2026-10-05 10:40
Признак:            на PC07 не открываются сайты, Telegram работает
Проверки:           ipconfig /all — 192.168.50.107/24, шлюз .1, DNS 8.8.4.4
                    ping 8.8.8.8 — 0% потерь; nslookup aifu.uz — «timed out»
Причина:            адрес DNS-сервера введён вручную с ошибкой (8.8.4.44)
Решение:            в настройках IPv4 сделать DNS автоматическим (DHCP)
Результат:          nslookup и браузер работают
```

## Разобранный пример 1: анализ вывода netstat

На входе — строки вывода `netstat -an` (протокол, локальный адрес, внешний адрес и для TCP — состояние). Выведите прослушиваемые TCP-порты (без повторов, по возрастанию), число TCP-соединений в каждом состоянии (по алфавиту названий состояний, кроме `LISTENING`) и внешний IP с наибольшим числом соединений (при равенстве — меньшую строку).

```python
import sys

tinglash = set()
holatlar = {}
masofaviy = {}
for satr in sys.stdin:
    q = satr.split()
    if len(q) < 4 or q[0] != "TCP":
        continue                                  # строки заголовка и UDP
    port = int(q[1].rsplit(":", 1)[1])
    holat = q[3]
    if holat == "LISTENING":
        tinglash.add(port)
        continue
    holatlar[holat] = holatlar.get(holat, 0) + 1
    ip = q[2].rsplit(":", 1)[0]
    masofaviy[ip] = masofaviy.get(ip, 0) + 1
print("LISTENING: " + " ".join(str(p) for p in sorted(tinglash)))
for holat in sorted(holatlar):
    print(holat + ": " + str(holatlar[holat]))
if masofaviy:
    eng = min(masofaviy, key=lambda ip: (-masofaviy[ip], ip))
    print("самый активный сервер: " + eng + " (" + str(masofaviy[eng]) + ")")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        TreeSet<Integer> tinglash = new TreeSet<>();
        Map<String, Integer> holatlar = new TreeMap<>();
        Map<String, Integer> masofaviy = new TreeMap<>();
        while (in.hasNextLine()) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q.length < 4 || !q[0].equals("TCP")) continue;          // строки заголовка и UDP
            int port = Integer.parseInt(q[1].substring(q[1].lastIndexOf(':') + 1));
            String holat = q[3];
            if (holat.equals("LISTENING")) {
                tinglash.add(port);
                continue;
            }
            holatlar.merge(holat, 1, Integer::sum);
            masofaviy.merge(q[2].substring(0, q[2].lastIndexOf(':')), 1, Integer::sum);
        }
        StringJoiner portlar = new StringJoiner(" ");
        for (int p : tinglash) portlar.add(String.valueOf(p));
        System.out.println("LISTENING: " + portlar);
        for (Map.Entry<String, Integer> e : holatlar.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        String eng = null;
        for (Map.Entry<String, Integer> e : masofaviy.entrySet()) {
            if (eng == null || e.getValue() > masofaviy.get(eng)) eng = e.getKey();
        }
        if (eng != null) System.out.println("самый активный сервер: " + eng + " (" + masofaviy.get(eng) + ")");
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    set<int> tinglash;
    map<string, int> holatlar, masofaviy;
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        vector<string> q;
        string s;
        while (ss >> s) q.push_back(s);
        if (q.size() < 4 || q[0] != "TCP") continue;                    // строки заголовка и UDP
        int port = stoi(q[1].substr(q[1].rfind(':') + 1));
        string holat = q[3];
        if (holat == "LISTENING") {
            tinglash.insert(port);
            continue;
        }
        holatlar[holat]++;
        masofaviy[q[2].substr(0, q[2].rfind(':'))]++;
    }
    string portlar;
    for (int p : tinglash) portlar += (portlar.empty() ? "" : " ") + to_string(p);
    cout << "LISTENING: " << portlar << endl;
    for (auto& e : holatlar) cout << e.first << ": " << e.second << endl;
    string eng;
    for (auto& e : masofaviy) {
        if (eng.empty() || e.second > masofaviy[eng]) eng = e.first;
    }
    if (!eng.empty()) cout << "самый активный сервер: " << eng << " (" << masofaviy[eng] << ")" << endl;
    return 0;
}
```

Ввод:

```text
  Proto  Local Address          Foreign Address        State
  TCP    0.0.0.0:135            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:445            0.0.0.0:0              LISTENING
  TCP    0.0.0.0:3389           0.0.0.0:0              LISTENING
  TCP    192.168.1.10:51000     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51002     142.250.74.46:443      ESTABLISHED
  TCP    192.168.1.10:51005     95.46.98.10:443        ESTABLISHED
  TCP    192.168.1.10:50990     95.46.98.10:443        TIME_WAIT
  TCP    192.168.1.10:51010     185.74.5.10:80         SYN_SENT
  UDP    0.0.0.0:5353           *:*
```

Вывод:

```text
LISTENING: 135 445 3389
ESTABLISHED: 3
SYN_SENT: 1
TIME_WAIT: 1
самый активный сервер: 142.250.74.46 (2)
```

Интерпретация: прослушивается 3389 (RDP) — если он не нужен, отключите службу или закройте порт в межсетевом экране. `SYN_SENT` — запрос на соединение отправлен, но ответа нет: порт 80 у 185.74.5.10 закрыт или блокируется. Много таких записей — проблема сети или сервера; `SYN_SENT` ко многим разным адресам — признак сканирования вредоносной программой.

## Разобранный пример 2: анализ результата tracert

В каждой строке — один переход: номер, три RTT (мс или `*`) и адрес маршрутизатора (`-` — неизвестен). Для каждого перехода выведите средний RTT (только по полученным ответам) или `нет ответа`, затем укажите переход, на котором задержка выросла сильнее всего по сравнению с предыдущим ответившим.

```python
import sys

oldingi = None
eng_sakrash, eng_oshish = None, -1.0
for satr in sys.stdin.read().strip().split("\n"):
    q = satr.split()
    vaqtlar = [int(x) for x in q[1:4] if x != "*"]
    if not vaqtlar:
        print(q[0] + " " + q[4] + ": нет ответа")      # маршрутизатор не отвечает на ICMP
        continue
    ortacha = sum(vaqtlar) / len(vaqtlar)
    print(f"{q[0]} {q[4]}: {ortacha:.1f} ms")
    if oldingi is not None and ortacha - oldingi > eng_oshish:
        eng_sakrash, eng_oshish = q[0], ortacha - oldingi
    oldingi = ortacha
if eng_sakrash is not None:
    print(f"наибольший рост на шаге {eng_sakrash} (+{eng_oshish:.1f} ms)")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Double oldingi = null;
        String engSakrash = null;
        double engOshish = -1.0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            double summa = 0;
            int soni = 0;
            for (int i = 1; i <= 3; i++) {
                if (!q[i].equals("*")) {
                    summa += Integer.parseInt(q[i]);
                    soni++;
                }
            }
            if (soni == 0) {
                System.out.println(q[0] + " " + q[4] + ": нет ответа");   // маршрутизатор не отвечает на ICMP
                continue;
            }
            double ortacha = summa / soni;
            System.out.println(String.format("%s %s: %.1f ms", q[0], q[4], ortacha));
            if (oldingi != null && ortacha - oldingi > engOshish) {
                engSakrash = q[0];
                engOshish = ortacha - oldingi;
            }
            oldingi = ortacha;
        }
        if (engSakrash != null) {
            System.out.println(String.format("наибольший рост на шаге %s (+%.1f ms)", engSakrash, engOshish));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string satr;
    bool bor = false;
    double oldingi = 0, engOshish = -1.0;
    string engSakrash;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string raqam, t[3], manzil;
        if (!(ss >> raqam >> t[0] >> t[1] >> t[2] >> manzil)) continue;
        double summa = 0;
        int soni = 0;
        for (auto& x : t) {
            if (x != "*") {
                summa += stoi(x);
                soni++;
            }
        }
        if (soni == 0) {
            printf("%s %s: нет ответа\n", raqam.c_str(), manzil.c_str());   // маршрутизатор не отвечает на ICMP
            continue;
        }
        double ortacha = summa / soni;
        printf("%s %s: %.1f ms\n", raqam.c_str(), manzil.c_str(), ortacha);
        if (bor && ortacha - oldingi > engOshish) {
            engSakrash = raqam;
            engOshish = ortacha - oldingi;
        }
        oldingi = ortacha;
        bor = true;
    }
    if (!engSakrash.empty()) printf("наибольший рост на шаге %s (+%.1f ms)\n", engSakrash.c_str(), engOshish);
    return 0;
}
```

Ввод:

```text
1 1 1 2 192.168.1.1
2 8 9 8 10.10.0.1
3 * * * -
4 12 11 13 195.158.1.1
5 60 * 62 72.14.215.1
6 61 60 59 8.8.8.8
```

Вывод:

```text
1 192.168.1.1: 1.3 ms
2 10.10.0.1: 8.3 ms
3 -: нет ответа
4 195.158.1.1: 12.0 ms
5 72.14.215.1: 61.0 ms
6 8.8.8.8: 60.0 ms
наибольший рост на шаге 5 (+49.0 ms)
```

Интерпретация: переход 3 не ответил, но следующие работают — это не неисправность, маршрутизатор настроен не отвечать на ICMP. При переходе с 4 на 5 задержка выросла на ~49 мс — международный канал провайдера (расстояние до другой страны). Если большой рост сохраняется на всех следующих переходах — причина в этом месте; если только на одном — сам маршрутизатор медленно отвечает на ICMP.

:::warning
**Частые ошибки**

- **Считать `*` в `tracert` обрывом.** Если следующие переходы отвечают, путь работает — маршрутизатор просто не отвечает на ICMP.
- **Запускать `netstat` с ключом `-b` без прав администратора.** Для имён программ нужны права администратора; получите PID ключом `-o` и сравните в диспетчере задач.
- **Выполнять `arp -d *` или `ipconfig /release` в удалённом (RDP) сеансе.** Соединение оборвётся, и вы не сможете вернуться.
- **Проверять, не записывая результаты.** Без отчёта повторяющиеся проблемы каждый раз расследуются с нуля.
:::

:::tip
Выполните на своём компьютере `netstat -ano > netstat.txt` и подайте файл программе первого примера. Для каждого прослушиваемого порта из результата определите программу командой `tasklist /fi "PID eq <PID>"` и выпишите ненужные.
:::

## Итоги

- Основные утилиты: `ipconfig`, `ping`, `tracert`/`pathping`, `nslookup`, `arp`, `netstat`, `route`, `Test-NetConnection` (Linux: `ip`, `ss`, `dig`, `mtr`, `nc`).
- Для каждого сценария: признак → проверка → причина → решение → результат; всё заносится в отчёт.
- `netstat`: LISTENING — открытые службы, ESTABLISHED — активные соединения, SYN_SENT — попытки без ответа, TIME_WAIT — закрытые соединения.
- `tracert`: `*` — не всегда обрыв; резкий и устойчивый рост задержки указывает на проблемное место.
- Анализ вывода утилит скриптом автоматизирует ежедневные проверки.

## Проверьте себя

:::check
Q: Какая утилита Windows показывает, через какие маршрутизаторы пакет идёт к адресу?
A: `tracert`.
Q: Какая команда Windows показывает IP-адрес, маску и шлюз компьютера?
A: `ipconfig` (полностью — `ipconfig /all`).
Q: Что может означать много состояний `SYN_SENT` в `netstat`?
A: На запросы соединения нет ответа (порт закрыт, межсетевой экран) или вредоносная программа выполняет сканирование.
Q: На одном промежуточном переходе `tracert` все три результата — `*`, а следующие нормальные. Что это значит?
A: Этот маршрутизатор настроен не отвечать на ICMP; путь работает.
Q: Какая команда проверяет, открыт ли TCP-порт?
A: `Test-NetConnection host -Port N` (в Linux `nc -zv host N`).
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/windows-commands
Windows commands
Полный справочник утилит командной строки Windows (на английском).
:::
