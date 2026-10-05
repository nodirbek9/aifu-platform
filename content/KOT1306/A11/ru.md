---
title: Установка и настройка сетевых служб в операционной системе Windows Server (DHCP, DNS)
summary: Установка и авторизация роли DHCP-сервера; область (scope), пул адресов, исключения, резервирования, срок аренды, параметры (шлюз, DNS); установка роли DNS-сервера; зоны прямого и обратного просмотра, записи A, CNAME, PTR, MX, сервера пересылки (forwarders); команды PowerShell; проверка на клиенте; моделирование пула DHCP.
---

## Почему это важно

На лекциях M9 и M12 мы изучили, как работают DHCP и DNS. Теперь мы их **настроим** сами: установленный в A10 сервер `SRV-DC01` будет автоматически раздавать адреса компьютерам сети и разрешать внутренние имена (`srv-dc01.aifu.local`, `printer.aifu.local`). Эти две службы — фундамент сети любой организации: если они не работают, для пользователей «ничего не работает».

:::note
**После этого занятия вы сможете:**

- устанавливать роль DHCP в Windows Server и настраивать область, исключения, резервирования и параметры;
- устанавливать роль DNS и создавать зоны прямого и обратного просмотра и записи;
- выполнять то же самое командами PowerShell;
- проверять результат на клиентском компьютере командами `ipconfig` и `nslookup`;
- объяснять, как DHCP-сервер выбирает адреса и ведёт аренды.
:::

## Схема лаборатории

| Устройство | Адрес | Роль |
|---|---|---|
| `SRV-DC01` | 192.168.50.10/24 (статический) | сервер DHCP и DNS |
| Маршрутизатор (шлюз) | 192.168.50.1 | выход в Интернет |
| Клиенты | 192.168.50.100–192.168.50.199 (DHCP) | компьютеры пользователей |
| Принтер | 192.168.50.150 (резервирование) | постоянный адрес, но через DHCP |

Все ВМ — во **внутренней сети**: тестовый DHCP-сервер не должен попасть в реальную сеть.

## Настройка DHCP-сервера

1. Server Manager → **Add Roles and Features** → **DHCP Server**. После установки — жёлтый флажок → **Complete DHCP configuration** (создание групп безопасности; в домене — **авторизация** сервера в AD: неавторизованный DHCP-сервер в сети домена не работает).
2. **Tools → DHCP** → IPv4 → **New Scope**:
   - имя: `Talabalar-LAN`;
   - пул: 192.168.50.100 – 192.168.50.199, маска /24;
   - **исключения**: 192.168.50.100–192.168.50.109 (для будущих статических устройств);
   - **срок аренды**: 8 часов (коротко для компьютерного класса, 8 дней для офиса);
   - **параметры**: 003 Router = 192.168.50.1, 006 DNS Servers = 192.168.50.10, 015 DNS Domain Name = `aifu.local`;
   - **активировать** область (Activate).
3. **Резервирование**: Scope → Reservations → New → MAC-адрес принтера и 192.168.50.150.

Эквивалент в PowerShell:

```text
Install-WindowsFeature DHCP -IncludeManagementTools
Add-DhcpServerv4Scope -Name "Talabalar-LAN" -StartRange 192.168.50.100 -EndRange 192.168.50.199 -SubnetMask 255.255.255.0 -LeaseDuration 08:00:00
Add-DhcpServerv4ExclusionRange -ScopeId 192.168.50.0 -StartRange 192.168.50.100 -EndRange 192.168.50.109
Set-DhcpServerv4OptionValue -ScopeId 192.168.50.0 -Router 192.168.50.1 -DnsServer 192.168.50.10 -DnsDomain aifu.local
Add-DhcpServerv4Reservation -ScopeId 192.168.50.0 -IPAddress 192.168.50.150 -ClientId "00-1A-2B-3C-4D-5E" -Name "Printer"
Get-DhcpServerv4Lease -ScopeId 192.168.50.0
```

## Настройка DNS-сервера

1. **Add Roles and Features** → **DNS Server** (на контроллере домена устанавливается автоматически вместе с AD).
2. **Tools → DNS** → **Forward Lookup Zones** → New Zone → Primary → `aifu.local`.
3. Добавьте записи:
   - **A**: `srv-dc01` → 192.168.50.10; `printer` → 192.168.50.150;
   - **CNAME**: `files` → `srv-dc01.aifu.local`;
   - **MX**: `aifu.local` → `mail.aifu.local` (приоритет 10).
4. **Reverse Lookup Zones** → New Zone → `50.168.192.in-addr.arpa` → записи **PTR** (флажок «Create associated PTR record» при создании записи A).
5. **Forwarders** (свойства сервера): DNS провайдера или 1.1.1.1 для имён, которых нет во внутренней зоне.

```text
Install-WindowsFeature DNS -IncludeManagementTools
Add-DnsServerPrimaryZone -Name "aifu.local" -ZoneFile "aifu.local.dns"
Add-DnsServerPrimaryZone -NetworkId "192.168.50.0/24" -ZoneFile "50.168.192.in-addr.arpa.dns"
Add-DnsServerResourceRecordA -ZoneName "aifu.local" -Name "srv-dc01" -IPv4Address 192.168.50.10 -CreatePtr
Add-DnsServerResourceRecordCName -ZoneName "aifu.local" -Name "files" -HostNameAlias "srv-dc01.aifu.local"
Set-DnsServerForwarder -IPAddress 1.1.1.1
```

DHCP можно связать с DNS: выдавая клиенту адрес, DHCP-сервер автоматически записывает его имя в DNS (**динамическое обновление**) — тогда разрешается и `talaba-pc07.aifu.local`.

## Проверка на клиенте

```text
ipconfig /release
ipconfig /renew
ipconfig /all              адрес, маска, шлюз, DNS, DHCP-сервер, срок аренды
nslookup srv-dc01.aifu.local
nslookup files.aifu.local
nslookup 192.168.50.10     работает ли обратная зона (PTR)
```

На сервере в разделе **Address Leases** должна появиться аренда клиента. Если клиент получил адрес 169.254.x.x — DHCP-сервер не найден (область не активна, нет авторизации или ВМ в разных сетях).

## Разобранный пример: моделирование пула DHCP

В первой строке — начало и конец пула (в пределах одной /24) и срок аренды (секунды). Затем число исключений и их адреса, число резервирований и пары «MAC адрес», затем число событий и каждое событие: время, MAC, `DISCOVER` или `RELEASE`. Правила сервера:

- клиент с резервированием всегда получает свой адрес;
- клиент, у которого аренда ещё не истекла, снова получает тот же адрес (аренда продлевается);
- иначе — **наименьший** адрес, не исключённый, не зарезервированный и не занятый; истёкшие аренды освобождаются;
- если свободного адреса нет — `нет адреса`.

```python
def son(ip):
    return int(ip.split(".")[3])


boshi, oxiri, muddat = input().split()
tarmoq = boshi.rsplit(".", 1)[0] + "."
muddat = int(muddat)
istisno = {son(input().strip()) for _ in range(int(input()))}
rezerv = {}                                  # MAC -> последний октет
for _ in range(int(input())):
    mac, ip = input().split()
    rezerv[mac] = son(ip)
ijara = {}                                   # октет -> (MAC, время окончания)
for _ in range(int(input())):
    vaqt, mac, tur = input().split()
    vaqt = int(vaqt)
    for oktet in [o for o, (m, t) in ijara.items() if t <= vaqt]:
        del ijara[oktet]                     # истёкшие аренды
    if tur == "RELEASE":
        for oktet, (m, t) in list(ijara.items()):
            if m == mac:
                del ijara[oktet]
                print(vaqt, mac, "RELEASE " + tarmoq + str(oktet))
        continue
    if mac in rezerv:
        print(vaqt, mac, "OFFER " + tarmoq + str(rezerv[mac]))
        continue
    tanlangan = next((o for o, (m, t) in ijara.items() if m == mac), None)
    if tanlangan is None:
        for o in range(son(boshi), son(oxiri) + 1):
            if o not in istisno and o not in rezerv.values() and o not in ijara:
                tanlangan = o
                break
    if tanlangan is None:
        print(vaqt, mac, "нет адреса")
    else:
        ijara[tanlangan] = (mac, vaqt + muddat)
        print(vaqt, mac, "OFFER " + tarmoq + str(tanlangan))
```
```java
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;

public class Main {
    static int son(String ip) {
        return Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String boshi = in.next();
        String oxiri = in.next();
        int muddat = in.nextInt();
        String tarmoq = boshi.substring(0, boshi.lastIndexOf('.') + 1);
        Set<Integer> istisno = new HashSet<>();
        int n = in.nextInt();
        for (int i = 0; i < n; i++) istisno.add(son(in.next()));
        Map<String, Integer> rezerv = new HashMap<>();             // MAC -> последний октет
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String mac = in.next();
            rezerv.put(mac, son(in.next()));
        }
        Map<Integer, String> egasi = new TreeMap<>();              // октет -> MAC
        Map<Integer, Integer> tugash = new HashMap<>();            // октет -> время окончания
        n = in.nextInt();
        for (int i = 0; i < n; i++) {
            int vaqt = in.nextInt();
            String mac = in.next();
            String tur = in.next();
            egasi.keySet().removeIf(o -> tugash.get(o) <= vaqt);    // истёкшие аренды
            if (tur.equals("RELEASE")) {
                for (Integer o : egasi.keySet().toArray(new Integer[0])) {
                    if (egasi.get(o).equals(mac)) {
                        egasi.remove(o);
                        System.out.println(vaqt + " " + mac + " RELEASE " + tarmoq + o);
                    }
                }
                continue;
            }
            if (rezerv.containsKey(mac)) {
                System.out.println(vaqt + " " + mac + " OFFER " + tarmoq + rezerv.get(mac));
                continue;
            }
            Integer tanlangan = null;
            for (Map.Entry<Integer, String> e : egasi.entrySet()) {
                if (e.getValue().equals(mac)) tanlangan = e.getKey();
            }
            if (tanlangan == null) {
                for (int o = son(boshi); o <= son(oxiri); o++) {
                    if (!istisno.contains(o) && !rezerv.containsValue(o) && !egasi.containsKey(o)) {
                        tanlangan = o;
                        break;
                    }
                }
            }
            if (tanlangan == null) {
                System.out.println(vaqt + " " + mac + " нет адреса");
            } else {
                egasi.put(tanlangan, mac);
                tugash.put(tanlangan, vaqt + muddat);
                System.out.println(vaqt + " " + mac + " OFFER " + tarmoq + tanlangan);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <utility>
using namespace std;

int son(const string& ip) {
    return stoi(ip.substr(ip.rfind('.') + 1));
}

int main() {
    string boshi, oxiri;
    int muddat, n;
    cin >> boshi >> oxiri >> muddat;
    string tarmoq = boshi.substr(0, boshi.rfind('.') + 1);
    set<int> istisno;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string ip;
        cin >> ip;
        istisno.insert(son(ip));
    }
    map<string, int> rezerv;                                    // MAC -> последний октет
    set<int> rezervlangan;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string mac, ip;
        cin >> mac >> ip;
        rezerv[mac] = son(ip);
        rezervlangan.insert(son(ip));
    }
    map<int, pair<string, int>> ijara;                          // октет -> (MAC, время окончания)
    cin >> n;
    for (int i = 0; i < n; i++) {
        int vaqt;
        string mac, tur;
        cin >> vaqt >> mac >> tur;
        for (auto it = ijara.begin(); it != ijara.end();) {
            if (it->second.second <= vaqt) it = ijara.erase(it);    // истёкшие аренды
            else ++it;
        }
        if (tur == "RELEASE") {
            for (auto it = ijara.begin(); it != ijara.end();) {
                if (it->second.first == mac) {
                    cout << vaqt << " " << mac << " RELEASE " << tarmoq << it->first << endl;
                    it = ijara.erase(it);
                } else {
                    ++it;
                }
            }
            continue;
        }
        if (rezerv.count(mac)) {
            cout << vaqt << " " << mac << " OFFER " << tarmoq << rezerv[mac] << endl;
            continue;
        }
        int tanlangan = -1;
        for (auto& e : ijara) {
            if (e.second.first == mac) tanlangan = e.first;
        }
        if (tanlangan < 0) {
            for (int o = son(boshi); o <= son(oxiri); o++) {
                if (!istisno.count(o) && !rezervlangan.count(o) && !ijara.count(o)) {
                    tanlangan = o;
                    break;
                }
            }
        }
        if (tanlangan < 0) {
            cout << vaqt << " " << mac << " нет адреса" << endl;
        } else {
            ijara[tanlangan] = {mac, vaqt + muddat};
            cout << vaqt << " " << mac << " OFFER " << tarmoq << tanlangan << endl;
        }
    }
    return 0;
}
```

Ввод (пул .100–.104, аренда 60 с, .101 исключён, .104 зарезервирован для принтера):

```text
192.168.50.100 192.168.50.104 60
1
192.168.50.101
1
AA:AA 192.168.50.104
9
0 BB:01 DISCOVER
5 BB:02 DISCOVER
10 AA:AA DISCOVER
20 BB:03 DISCOVER
30 BB:01 RELEASE
35 BB:04 DISCOVER
50 BB:03 DISCOVER
66 BB:05 DISCOVER
70 BB:02 DISCOVER
```

Вывод:

```text
0 BB:01 OFFER 192.168.50.100
5 BB:02 OFFER 192.168.50.102
10 AA:AA OFFER 192.168.50.104
20 BB:03 OFFER 192.168.50.103
30 BB:01 RELEASE 192.168.50.100
35 BB:04 OFFER 192.168.50.100
50 BB:03 OFFER 192.168.50.103
66 BB:05 OFFER 192.168.50.102
70 BB:02 нет адреса
```

Обратите внимание: на 50-й секунде BB:03 продлил аренду (окончание сдвинулось на 110). Аренда BB:02 истекла на 65-й секунде, и он её не продлил — его адрес .102 на 66-й секунде получил BB:05. Когда BB:02 вернулся на 70-й секунде, свободных адресов в пуле не осталось: .100 — BB:04, .102 — BB:05, .103 — BB:03, .101 исключён, .104 зарезервирован для принтера. В реальной сети это значит, что пул слишком мал или аренда слишком длинная.

:::warning
**Частые ошибки**

- **Не авторизовать DHCP-сервер или не активировать область.** Клиенты получают 169.254.x.x.
- **Не исключить диапазон устройств со статическими адресами.** DHCP выдаст такой адрес другому компьютеру, возникнет конфликт IP.
- **Указывать внешний DNS в параметре 006.** Внутренние имена (`aifu.local`) не разрешатся; клиентам даётся внутренний DNS, а для внешних имён на сервере настраивается пересылка.
- **Забыть обратную зону.** `nslookup` не находит имя по IP, и некоторые службы замедляются.
- **Запускать два DHCP-сервера с одинаковым пулом.** Они могут выдать один адрес двум клиентам; для резервирования используется **DHCP Failover**.
:::

:::tip
Запустите Wireshark на клиентской ВМ и выполните `ipconfig /release` и `ipconfig /renew`. С фильтром `dhcp` найдите четыре сообщения DORA и сравните параметры 003, 006, 015 и 051 (срок аренды) в сообщении Offer с настроенными вами значениями.
:::

## Итоги

- DHCP: установка роли → авторизация → область (пул, исключения, аренда) → параметры (003, 006, 015) → активация → резервирования.
- DNS: установка роли → прямая зона (`aifu.local`) → обратная зона → записи A, CNAME, MX, PTR → пересылка.
- Всё это можно сделать и командами PowerShell вроде `Add-DhcpServerv4Scope`, `Add-DnsServerPrimaryZone`.
- Проверка: `ipconfig /all`, `ipconfig /renew`, `nslookup`, Address Leases на сервере.
- DHCP-сервер выбирает в порядке: резервирование → текущая аренда → наименьший свободный адрес.

## Проверьте себя

:::check
Q: Что дают параметры области 003 и 006?
A: 003 — основной шлюз (маршрутизатор), 006 — адрес DNS-сервера.
Q: Чем исключение отличается от резервирования?
A: Исключённый адрес DHCP не выдаёт вообще; зарезервированный выдаётся только устройству с указанным MAC.
Q: Почему после получения адреса `nslookup srv-dc01` может не работать?
A: Клиенту выдан внешний DNS вместо внутреннего, или в зоне `aifu.local` нет записи A.
Q: Зачем нужна зона обратного просмотра?
A: Для поиска имени по IP-адресу (записи PTR).
Q: В чём преимущество короткого срока аренды?
A: Там, где клиенты часто меняются (компьютерный класс, Wi-Fi), адреса освобождаются быстрее.
:::

:::link https://learn.microsoft.com/en-us/windows-server/networking/technologies/dhcp/dhcp-top
DHCP in Windows Server
Официальная документация Microsoft по DHCP-серверу (на английском).
:::
