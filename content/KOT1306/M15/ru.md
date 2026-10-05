---
title: Устранение сетевых неисправностей. Обнаружение и устранение проблем, возникающих в процессе работы сети
summary: Методология устранения неисправностей; подходы на основе OSI — снизу вверх, сверху вниз, «разделяй и властвуй»; типичные проблемы и признаки каждого уровня; основные утилиты — ping, tracert, ipconfig, nslookup, arp, netstat, pathping; анализ результатов ping — потери, RTT, джиттер; документирование.
---

## Почему это важно

«Интернет не работает» — фраза, которую администратор сети слышит чаще всего. За ней может стоять обрыв кабеля, неправильный IP-адрес, ошибка DNS, правило межсетевого экрана или проблема провайдера. Случайные действия (перезагрузить всё) иногда помогают, но без понимания проблемы позволяют ей вернуться. При системном подходе проблему можно найти за несколько минут.

:::note
**После этого занятия вы сможете:**

- объяснять этапы устранения неисправностей и подходы на основе OSI;
- определять типичные проблемы каждого уровня по признакам;
- интерпретировать результаты `ping`, `tracert`, `ipconfig`, `nslookup`, `arp`, `netstat`;
- вычислять по статистике ping потери, задержку и джиттер;
- определять по результатам проверок, на каком уровне проблема.
:::

## Методология

1. **Определить проблему**: что не работает, у кого, с какого времени, что изменилось? («Только у вас или во всём классе?»)
2. **Выдвинуть гипотезу**: выбрать наиболее вероятную причину.
3. **Проверить**: испытать гипотезу; если не подтвердилась — следующая гипотеза.
4. **Спланировать и выполнить**: исправление (заранее оценив его последствия).
5. **Проверить результат**: всё ли работает, не появилась ли новая проблема.
6. **Задокументировать**: записать причину, признаки и решение — в следующий раз будет быстрее.

## Подходы на основе OSI

| Подход | Как | Когда удобен |
|---|---|---|
| **Снизу вверх** | кабель → канал → IP → транспорт → приложение | вероятна физическая проблема, новое подключение |
| **Сверху вниз** | приложение → … → кабель | не работает только одна программа |
| **Разделяй и властвуй** | начать со среднего уровня (`ping` — уровень 3): работает — вверх, нет — вниз | самый быстрый для опытного инженера |

## Типичные проблемы по уровням

| Уровень | Признаки | Причины | Проверка |
|---|---|---|---|
| 1 — Физический | индикатор порта не горит, «Network cable unplugged» | оборванный или неправильный кабель, выключенный порт, слабый Wi-Fi | индикаторы, кабельный тестер, другой порт |
| 2 — Канальный | подключение есть, но никого не достать | неверный VLAN, несовпадение дуплекса, блокировка STP, фильтр MAC | `show interfaces`, `arp -a` |
| 3 — Сетевой | 169.254.x.x, «Destination host unreachable» | DHCP не работает, неверные IP/маска/шлюз, нет маршрута | `ipconfig /all`, `ping` шлюза, `tracert` |
| 4 — Транспортный | ping работает, но к службе не подключиться | порт закрыт, межсетевой экран, служба не запущена | `Test-NetConnection server -Port 443`, `netstat -an` |
| 7 — Прикладной | по IP работает, по имени — нет; ошибка сайта | DNS, прокси, сертификат, настройки приложения | `nslookup`, консоль браузера |

## Основные утилиты

| Команда (Windows) | Linux | Что даёт |
|---|---|---|
| `ipconfig /all` | `ip addr`, `ip route` | адрес, маска, шлюз, DNS, DHCP |
| `ipconfig /release`, `/renew`, `/flushdns` | `dhclient -r`, `resolvectl flush-caches` | обновить адрес, очистить кэш DNS |
| `ping` | `ping` | связность и RTT (ICMP Echo) |
| `tracert` | `traceroute` | маршрутизаторы на пути |
| `pathping` | `mtr` | потери и задержка на каждом переходе |
| `nslookup` | `dig`, `nslookup` | запросы DNS |
| `arp -a` | `ip neigh` | кэш ARP |
| `netstat -ano` | `ss -tunap` | открытые порты и соединения |
| `Test-NetConnection` | `nc -zv` | открыт ли TCP-порт |

**Классическая последовательность** («Интернет не работает»):

```text
ipconfig /all                 есть ли адрес? если 169.254 — проблема DHCP
ping 127.0.0.1                работает ли стек TCP/IP
ping 192.168.1.1              доступен ли шлюз (локальная сеть)
ping 8.8.8.8                  есть ли выход в Интернет по IP
ping google.com               работает ли DNS (если 8.8.8.8 работает, а это нет — DNS)
tracert 8.8.8.8               где обрывается
```

## Как понимать результаты ping

- **Потери** (packet loss): 0% — норма; 1–2% — заметно (голосовая связь прерывается); больше — серьёзная проблема (шумный кабель, перегруженный канал, слабый Wi-Fi).
- **RTT** (round-trip time): в LAN < 1 мс, в пределах города 5–20 мс, до другого континента 100–250 мс.
- **Джиттер** — изменчивость последовательных RTT; важен для голоса и видео (не должен превышать 30 мс).
- `Request timed out` — ответ не пришёл (пакет потерян или ICMP заблокирован); `Destination host unreachable` — маршрутизатор не нашёл путь; `TTL expired in transit` — петля маршрутизации.

## Разобранный пример 1: статистика ping

В каждой строке — один результат ping: RTT (мс, целое) или `*` (нет ответа). Выведите число отправленных и полученных пакетов и процент потерь, минимальный, средний и максимальный RTT, а также джиттер (среднее модулей разностей RTT последовательных полученных ответов).

```python
import sys

natijalar = sys.stdin.read().split()
rtt = [int(x) for x in natijalar if x != "*"]
yuborildi = len(natijalar)
qabul = len(rtt)
yoqotish = (yuborildi - qabul) * 100 / yuborildi
print(f"пакеты: {yuborildi} отправлено, {qabul} получено, {yoqotish:.1f}% потеряно")
if qabul == 0:
    print("RTT: нет ответа")
else:
    print(f"RTT: min {min(rtt)}, сред. {sum(rtt) / qabul:.1f}, max {max(rtt)} ms")
    farqlar = [abs(rtt[i] - rtt[i - 1]) for i in range(1, qabul)]
    jitter = sum(farqlar) / len(farqlar) if farqlar else 0.0   # среднее последовательных разностей
    print(f"jitter: {jitter:.1f} ms")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> rtt = new ArrayList<>();
        int yuborildi = 0;
        while (in.hasNext()) {
            String x = in.next();
            yuborildi++;
            if (!x.equals("*")) rtt.add(Integer.parseInt(x));
        }
        int qabul = rtt.size();
        double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
        System.out.println(String.format("пакеты: %d отправлено, %d получено, %.1f%% потеряно", yuborildi, qabul, yoqotish));
        if (qabul == 0) {
            System.out.println("RTT: нет ответа");
        } else {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            long summa = 0;
            for (int r : rtt) {
                min = Math.min(min, r);
                max = Math.max(max, r);
                summa += r;
            }
            System.out.println(String.format("RTT: min %d, сред. %.1f, max %d ms", min, (double) summa / qabul, max));
            double farqlar = 0;
            for (int i = 1; i < qabul; i++) farqlar += Math.abs(rtt.get(i) - rtt.get(i - 1));
            double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;     // среднее последовательных разностей
            System.out.println(String.format("jitter: %.1f ms", jitter));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<int> rtt;
    int yuborildi = 0;
    string x;
    while (cin >> x) {
        yuborildi++;
        if (x != "*") rtt.push_back(stoi(x));
    }
    int qabul = rtt.size();
    double yoqotish = (yuborildi - qabul) * 100.0 / yuborildi;
    printf("пакеты: %d отправлено, %d получено, %.1f%% потеряно\n", yuborildi, qabul, yoqotish);
    if (qabul == 0) {
        printf("RTT: нет ответа\n");
    } else {
        long long summa = 0;
        for (int r : rtt) summa += r;
        printf("RTT: min %d, сред. %.1f, max %d ms\n", *min_element(rtt.begin(), rtt.end()),
               (double) summa / qabul, *max_element(rtt.begin(), rtt.end()));
        double farqlar = 0;
        for (int i = 1; i < qabul; i++) farqlar += abs(rtt[i] - rtt[i - 1]);
        double jitter = qabul > 1 ? farqlar / (qabul - 1) : 0.0;          // среднее последовательных разностей
        printf("jitter: %.1f ms\n", jitter);
    }
    return 0;
}
```

Ввод:

```text
12
15
*
11
30
```

Вывод:

```text
пакеты: 5 отправлено, 4 получено, 20.0% потеряно
RTT: min 11, сред. 17.0, max 30 ms
jitter: 8.7 ms
```

20% потерь и 8,7 мс джиттера — плохо для кабеля, типично для слабого Wi-Fi. Один «скачок» в 30 мс заметно поднял и среднее, и джиттер. При диагностике смотрите не только на среднее, но и на худшее значение и разброс.

## Разобранный пример 2: диагностика по уровням

В каждой строке — результаты пяти проверок для одного компьютера (`ok` или `fail`) в таком порядке: подключение (кабель или Wi-Fi), IP-адрес (не 169.254), ping шлюза, запрос по имени (DNS), ping IP-адреса в Интернете. По первой неудачной проверке выведите уровень проблемы и совет; если всё `ok` — сеть исправна.

```python
import sys

TASHXIS = [
    "уровень 1: проверьте кабель или подключение Wi-Fi",
    "уровень 3: адрес от DHCP не получен (ipconfig /renew)",
    "уровень 3: шлюз недоступен (маска, шлюз, VLAN)",
    "уровень 7: DNS не работает (nslookup, адрес DNS-сервера)",
    "уровень 3: проблема провайдера или маршрута (tracert)",
]

for raqam, satr in enumerate(sys.stdin.read().strip().split("\n"), 1):
    natija = satr.split()
    xato = next((i for i, n in enumerate(natija) if n == "fail"), -1)   # первая ошибка
    if xato < 0:
        print(str(raqam) + ": сеть исправна")
    else:
        print(str(raqam) + ": " + TASHXIS[xato])
```
```java
import java.util.Scanner;

public class Main {
    static final String[] TASHXIS = {
        "уровень 1: проверьте кабель или подключение Wi-Fi",
        "уровень 3: адрес от DHCP не получен (ipconfig /renew)",
        "уровень 3: шлюз недоступен (маска, шлюз, VLAN)",
        "уровень 7: DNS не работает (nslookup, адрес DNS-сервера)",
        "уровень 3: проблема провайдера или маршрута (tracert)",
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int raqam = 0;
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            raqam++;
            String[] natija = satr.split(" ");
            int xato = -1;
            for (int i = 0; i < natija.length && xato < 0; i++) {
                if (natija[i].equals("fail")) xato = i;                     // первая ошибка
            }
            if (xato < 0) {
                System.out.println(raqam + ": сеть исправна");
            } else {
                System.out.println(raqam + ": " + TASHXIS[xato]);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const string TASHXIS[] = {
    "уровень 1: проверьте кабель или подключение Wi-Fi",
    "уровень 3: адрес от DHCP не получен (ipconfig /renew)",
    "уровень 3: шлюз недоступен (маска, шлюз, VLAN)",
    "уровень 7: DNS не работает (nslookup, адрес DNS-сервера)",
    "уровень 3: проблема провайдера или маршрута (tracert)",
};

int main() {
    string satr;
    int raqam = 0;
    while (getline(cin, satr)) {
        if (satr.find_first_not_of(" \r") == string::npos) continue;
        raqam++;
        istringstream ss(satr);
        string n;
        int xato = -1;
        for (int i = 0; ss >> n; i++) {
            if (n == "fail" && xato < 0) xato = i;                          // первая ошибка
        }
        if (xato < 0) {
            cout << raqam << ": сеть исправна" << endl;
        } else {
            cout << raqam << ": " << TASHXIS[xato] << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
ok ok ok fail ok
ok fail fail fail fail
ok ok ok ok ok
fail fail fail fail fail
ok ok ok ok fail
```

Вывод:

```text
1: уровень 7: DNS не работает (nslookup, адрес DNS-сервера)
2: уровень 3: адрес от DHCP не получен (ipconfig /renew)
3: сеть исправна
4: уровень 1: проверьте кабель или подключение Wi-Fi
5: уровень 3: проблема провайдера или маршрута (tracert)
```

У четвёртого компьютера не прошли все пять проверок, но причина одна — кабель. Поэтому смотрят на первую ошибку: верхние уровни зависят от нижних, и нижняя проблема «ломает» всё. Первый компьютер — классический случай: Интернет по IP есть, но сайты не открываются.

:::warning
**Частые ошибки**

- **Сразу искать сложную причину.** Сначала простое: кабель, индикаторы, «что меняли последним».
- **Делать вывод, что узел выключен, раз `ping` не отвечает.** Межсетевой экран может блокировать ICMP; проверьте порт командой `Test-NetConnection`.
- **Менять несколько вещей одновременно.** Невозможно понять, какое изменение помогло (или сломало).
- **Перезагружать всё и не записывать проблему.** Проблема вернётся, а причина останется неизвестной.
- **Смотреть только на средний RTT.** Потери и джиттер портят впечатление пользователя сильнее.
:::

:::tip
Выполните «классическую последовательность» в своей сети и занесите результаты в таблицу. Затем запустите `pathping 8.8.8.8` (или `mtr 8.8.8.8`) и определите, на каком переходе появляются задержка и потери.
:::

## Итоги

- Этапы: определить → гипотеза → проверка → исправление → проверка результата → документирование.
- Подходы OSI: снизу вверх, сверху вниз, «разделяй и властвуй».
- У каждого уровня свои признаки и средства: индикаторы, `arp`, `ipconfig`, `ping`, `tracert`, `Test-NetConnection`, `nslookup`.
- Результат ping оценивается по потерям, RTT (min/сред./max) и джиттеру.
- Первый неисправный нижний уровень — основная причина: верхние от него зависят.

## Проверьте себя

:::check
Q: `ping 8.8.8.8` по IP работает, а `ping google.com` — нет. Какая служба неисправна?
A: DNS.
Q: Компьютер получил адрес 169.254.x.x. Какова вероятная причина?
A: Не удалось связаться с DHCP-сервером (адрес APIPA).
Q: Какая утилита Windows показывает, через какие маршрутизаторы идёт пакет?
A: `tracert`.
Q: С чего начинается подход «разделяй и властвуй»?
A: Со среднего уровня, обычно с `ping` (уровень 3); по результату двигаются вверх или вниз.
Q: RTT равны 10, 12, 30, 11 мс. Каков джиттер?
A: (2 + 18 + 19) / 3 = 13 мс.
:::

:::link https://learn.microsoft.com/en-us/troubleshoot/windows-client/networking/tcpip-and-networking-troubleshooting-overview
TCP/IP troubleshooting
Руководство Microsoft по диагностике сетевых проблем (на английском).
:::
