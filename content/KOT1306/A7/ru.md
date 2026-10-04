---
title: IP-адреса. Классовые адреса. Адресация с помощью масок. Экономное использование IP-адресов
summary: Упражнения в переводе между двоичной и десятичной системами; классовые адреса и стандартные маски; таблица префикс–маска; ручное разбиение на подсети; экономное распределение адресного пространства масками переменной длины (VLSM); суммирование маршрутов (supernetting); /30 и /31 для каналов точка-точка; соединение двух подсетей маршрутизатором в Packet Tracer.
---

## Почему это важно

Университету выделен блок 192.168.10.0/24: в библиотеке 50 компьютеров, в лаборатории 100, в деканате 20, есть серверы и каналы между маршрутизаторами. Если поделить блок на равные части, кому-то не хватит, а у кого-то сотни адресов будут простаивать. **VLSM** — способ дать каждому отделу подсеть ровно нужного размера. Есть и обратная задача: **суммировать** много маленьких сетей в одну запись таблицы маршрутизации. На этом занятии сделаем и то, и другое вручную и программой.

:::note
**После этого занятия вы сможете:**

- быстро переводить октеты между двоичной и десятичной системами;
- переводить друг в друга префикс, маску и размер блока;
- распределять заданный блок между отделами с помощью VLSM;
- объединять несколько сетей в один суммарный маршрут;
- соединять две подсети через маршрутизатор в Packet Tracer.
:::

## Упражнения в двоичном переводе

«Веса» битов октета: **128 64 32 16 8 4 2 1**. Из десятичной в двоичную: начиная слева, если вес можно вычесть — 1, иначе 0.

| Десятичное | Двоичное | Десятичное | Двоичное |
|---|---|---|---|
| 192 | 11000000 | 248 | 11111000 |
| 224 | 11100000 | 252 | 11111100 |
| 240 | 11110000 | 254 | 11111110 |

В октетах маски встречаются только значения: **0, 128, 192, 224, 240, 248, 252, 254, 255**.

## Префикс, маска и блок

| Префикс | Маска | Блок (адресов) | Узлов |
|---|---|---|---|
| /24 | 255.255.255.0 | 256 | 254 |
| /25 | 255.255.255.128 | 128 | 126 |
| /26 | 255.255.255.192 | 64 | 62 |
| /27 | 255.255.255.224 | 32 | 30 |
| /28 | 255.255.255.240 | 16 | 14 |
| /29 | 255.255.255.248 | 8 | 6 |
| /30 | 255.255.255.252 | 4 | 2 |
| /31 | 255.255.255.254 | 2 | 2 (только каналы точка-точка, RFC 3021) |

Правило: блок для *H* узлов — наименьшая **степень двойки**, не меньшая *H* + 2. 50 узлов → 52 → 64 → /26.

## VLSM — маски переменной длины

**Алгоритм**:

1. Отсортируйте отделы по числу узлов в порядке **убывания**.
2. Для каждого найдите размер блока (степень 2 ≥ *H* + 2).
3. Выделяйте подряд с начала блока: каждая новая подсеть начинается там, где закончилась предыдущая.

Порядок от большего к меньшему важен: если большие блоки идут первыми, каждый блок начинается с адреса, кратного своему размеру (выровнен), и пробелов не остаётся.

**Для каналов между маршрутизаторами** используется /30 (2 узла) или /31 — выделение /24 растрачивает 252 адреса.

## Суммирование маршрутов

Если сети 172.16.0.0/24, 172.16.1.0/24, 172.16.2.0/24 и 172.16.3.0/24 достижимы через один маршрутизатор, другие маршрутизаторы могут хранить вместо четырёх записей одну — **172.16.0.0/22**. Суммарный префикс — это **общие начальные биты** всех адресов:

```text
172.16.0.0  = 10101100.00010000.000000|00.00000000
172.16.1.0  = 10101100.00010000.000000|01.00000000
172.16.2.0  = 10101100.00010000.000000|10.00000000
172.16.3.0  = 10101100.00010000.000000|11.00000000
              общая часть: 22 бита  ->  172.16.0.0/22
```

Суммирование уменьшает таблицы и ускоряет маршрутизацию. Осторожно: общий префикс 192.168.1.0/24 и 192.168.2.0/24 — /22, он «накрывает» и сети 192.168.0.0 и 192.168.3.0; если они находятся в другом месте, трафик может пойти не туда.

## Практическая работа: две подсети в Packet Tracer

1. Разместите маршрутизатор **2911**, два коммутатора **2960** и 4 ПК.
2. Возьмите подсети 192.168.10.128/26 для библиотеки (50 узлов) и 192.168.10.192/27 для деканата (20 узлов) (из примера VLSM ниже).
3. Настройте интерфейсы маршрутизатора:

```text
Router> enable
Router# configure terminal
Router(config)# interface g0/0
Router(config-if)# ip address 192.168.10.129 255.255.255.192
Router(config-if)# no shutdown
Router(config-if)# interface g0/1
Router(config-if)# ip address 192.168.10.193 255.255.255.224
Router(config-if)# no shutdown
```

4. Назначьте компьютерам адреса из их подсети и адрес интерфейса маршрутизатора в качестве **шлюза** (например, 192.168.10.130/26, шлюз 192.168.10.129).
5. Выполните `ping` с компьютера одной подсети на другую. Затем удалите шлюз у одного компьютера и проверьте снова — что изменилось?

Программа, вычисляющая сеть, маску, широковещательный адрес и узлы по одному адресу CIDR, — самостоятельное задание этого занятия (KOT-A7).

## Разобранный пример 1: суммирование маршрутов

В первой строке *n*, в следующих *n* строках — сети в виде `a.b.c.d/p`. Выведите наименьшую суммарную сеть, покрывающую их все: префикс — число общих начальных битов всех адресов (и не больше заданных префиксов).

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


n = int(input())
manzillar = []
p = 32
for _ in range(n):
    ip, prefiks = input().split("/")
    manzillar.append(songa(ip))
    p = min(p, int(prefiks))
while p > 0 and any((x >> (32 - p)) != (manzillar[0] >> (32 - p)) for x in manzillar):
    p -= 1                                   # укорачиваем, пока не найдём общую часть
maska = 0 if p == 0 else (0xFFFFFFFF << (32 - p)) & 0xFFFFFFFF
print(manzilga(manzillar[0] & maska) + "/" + str(p))
```
```java
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] manzillar = new long[n];
        int p = 32;
        for (int i = 0; i < n; i++) {
            String[] qism = in.next().split("/");
            manzillar[i] = songa(qism[0]);
            p = Math.min(p, Integer.parseInt(qism[1]));
        }
        while (p > 0) {
            boolean bir = true;
            for (long x : manzillar) {
                if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
            }
            if (bir) break;
            p--;                                             // укорачиваем, пока не найдём общую часть
        }
        long maska = p == 0 ? 0 : (0xFFFFFFFFL << (32 - p)) & 0xFFFFFFFFL;
        System.out.println(manzilga(manzillar[0] & maska) + "/" + p);
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    int n;
    cin >> n;
    vector<uint64_t> manzillar(n);
    int p = 32;
    for (int i = 0; i < n; i++) {
        string s;
        cin >> s;
        size_t chiziq = s.find('/');
        manzillar[i] = songa(s.substr(0, chiziq));
        p = min(p, stoi(s.substr(chiziq + 1)));
    }
    while (p > 0) {
        bool bir = true;
        for (uint64_t x : manzillar) {
            if ((x >> (32 - p)) != (manzillar[0] >> (32 - p))) bir = false;
        }
        if (bir) break;
        p--;                                                 // укорачиваем, пока не найдём общую часть
    }
    uint64_t maska = p == 0 ? 0 : (0xFFFFFFFFULL << (32 - p)) & 0xFFFFFFFFULL;
    cout << manzilga(manzillar[0] & maska) << "/" << p << endl;
    return 0;
}
```

Ввод:

```text
4
172.16.0.0/24
172.16.1.0/24
172.16.2.0/24
172.16.3.0/24
```

Вывод:

```text
172.16.0.0/22
```

Для `10.1.4.0/24` и `10.1.7.0/24` ответ `10.1.4.0/22`, а для `192.168.1.0/24` и `192.168.2.0/24` — `192.168.0.0/22`; обратите внимание, что во втором случае покрываются и лишние (чужие) сети.

## Разобранный пример 2: распределение VLSM

В первой строке — исходный блок (`a.b.c.d/p`) и число отделов *k*. В следующих *k* строках — название отдела и нужное число узлов. Распределите отделы подряд из блока в порядке убывания числа узлов (при равенстве — в порядке ввода). Для каждого отдела выведите подсеть и число пригодных узлов в ней, а если места не хватило — `не хватило места`; в конце выведите, сколько адресов использовано.

```python
def songa(ip):
    a, b, c, d = map(int, ip.split("."))
    return (a << 24) | (b << 16) | (c << 8) | d


def manzilga(x):
    return ".".join(str((x >> s) & 255) for s in (24, 16, 8, 0))


blok, k = input().split()
ip, prefiks = blok.split("/")
boshi = songa(ip)
oxiri = boshi + 2 ** (32 - int(prefiks))
bolimlar = []
for _ in range(int(k)):
    nom, xost = input().split()
    bolimlar.append((nom, int(xost)))
bolimlar.sort(key=lambda b: -b[1])            # от большего к меньшему (устойчиво)

joriy = boshi
for nom, xost in bolimlar:
    hajm = 4
    while hajm < xost + 2:                    # степень 2 >= узлов + 2
        hajm *= 2
    if joriy + hajm > oxiri:
        print(nom, "не хватило места")
        continue
    p = 32 - (hajm.bit_length() - 1)
    print(nom, manzilga(joriy) + "/" + str(p), hajm - 2)
    joriy += hajm
print("использовано:", joriy - boshi, "/", oxiri - boshi)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static long songa(String ip) {
        String[] q = ip.split("\\.");
        return (Long.parseLong(q[0]) << 24) | (Long.parseLong(q[1]) << 16) | (Long.parseLong(q[2]) << 8) | Long.parseLong(q[3]);
    }

    static String manzilga(long x) {
        return ((x >> 24) & 255) + "." + ((x >> 16) & 255) + "." + ((x >> 8) & 255) + "." + (x & 255);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] blok = in.next().split("/");
        int k = in.nextInt();
        long boshi = songa(blok[0]);
        long oxiri = boshi + (1L << (32 - Integer.parseInt(blok[1])));
        List<String> nomlar = new ArrayList<>();
        List<Long> xostlar = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            nomlar.add(in.next());
            xostlar.add(in.nextLong());
        }
        List<Integer> tartib = new ArrayList<>();
        for (int i = 0; i < k; i++) tartib.add(i);
        tartib.sort((a, b) -> Long.compare(xostlar.get(b), xostlar.get(a)));   // от большего к меньшему (устойчиво)

        long joriy = boshi;
        for (int i : tartib) {
            long hajm = 4;
            while (hajm < xostlar.get(i) + 2) {                // степень 2 >= узлов + 2
                hajm *= 2;
            }
            if (joriy + hajm > oxiri) {
                System.out.println(nomlar.get(i) + " не хватило места");
                continue;
            }
            int p = 32 - Long.numberOfTrailingZeros(hajm);
            System.out.println(nomlar.get(i) + " " + manzilga(joriy) + "/" + p + " " + (hajm - 2));
            joriy += hajm;
        }
        System.out.println("использовано: " + (joriy - boshi) + " / " + (oxiri - boshi));
    }
}
```
```cpp
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

uint64_t songa(const string& ip) {
    unsigned a, b, c, d;
    sscanf(ip.c_str(), "%u.%u.%u.%u", &a, &b, &c, &d);
    return ((uint64_t) a << 24) | (b << 16) | (c << 8) | d;
}

string manzilga(uint64_t x) {
    return to_string((x >> 24) & 255) + "." + to_string((x >> 16) & 255) + "." + to_string((x >> 8) & 255) + "." + to_string(x & 255);
}

int main() {
    string blok;
    int k;
    cin >> blok >> k;
    size_t chiziq = blok.find('/');
    uint64_t boshi = songa(blok.substr(0, chiziq));
    uint64_t oxiri = boshi + (1ULL << (32 - stoi(blok.substr(chiziq + 1))));
    vector<pair<string, long long>> bolimlar(k);
    for (auto& b : bolimlar) cin >> b.first >> b.second;
    stable_sort(bolimlar.begin(), bolimlar.end(), [](const pair<string, long long>& a, const pair<string, long long>& b) {
        return a.second > b.second;                          // от большего к меньшему (устойчиво)
    });

    uint64_t joriy = boshi;
    for (auto& b : bolimlar) {
        long long hajm = 4;
        while (hajm < b.second + 2) {                        // степень 2 >= узлов + 2
            hajm *= 2;
        }
        if (joriy + hajm > oxiri) {
            cout << b.first << " не хватило места" << endl;
            continue;
        }
        int p = 32 - __builtin_ctzll(hajm);
        cout << b.first << " " << manzilga(joriy) << "/" << p << " " << hajm - 2 << endl;
        joriy += hajm;
    }
    cout << "использовано: " << joriy - boshi << " / " << oxiri - boshi << endl;
    return 0;
}
```

Ввод:

```text
192.168.10.0/24 5
Kutubxona 50
Dekanat 20
Laboratoriya 100
Server 10
Kanal 2
```

Вывод:

```text
Laboratoriya 192.168.10.0/25 126
Kutubxona 192.168.10.128/26 62
Dekanat 192.168.10.192/27 30
Server 192.168.10.224/28 14
Kanal 192.168.10.240/30 2
использовано: 244 / 256
```

(Названия отделов — на узбекском: библиотека, деканат, лаборатория, сервер, канал.) При равном делении (/27, 30 узлов) не поместились бы ни лаборатория, ни библиотека; с VLSM поместилось всё, и 12 адресов (192.168.10.244–255) остались на будущее.

:::warning
**Частые ошибки**

- **Забывать выбирать блок по *H* + 2, а не по *H*.** 62 узла помещаются в /26, а 63 — нет (63 + 2 > 64).
- **Начинать VLSM с маленьких отделов.** Тогда большие блоки оказываются невыровненными и в адресном пространстве появляются пробелы.
- **Выделять каналам /24.** Каналу между двумя маршрутизаторами достаточно /30 или /31.
- **Не учитывать, что суммирование накрывает чужие сети.** Суммарный маршрут верен, только если все покрываемые сети действительно находятся в этом направлении.
- **Указывать шлюз из другой подсети.** Шлюз компьютера — всегда адрес интерфейса маршрутизатора в его собственной подсети.
:::

:::tip
Составьте адресный план своего факультета: распределите блок 10.20.0.0/22 с помощью VLSM между 4 компьютерными классами (по 30 компьютеров), 1 сетью Wi-Fi (200 устройств), серверами (12) и 3 каналами между маршрутизаторами. Проверьте результат программой второго примера.
:::

## Итоги

- Октеты маски: 0, 128, 192, 224, 240, 248, 252, 254, 255; блок = 256 − октет маски.
- Блок для *H* узлов — степень 2, не меньшая *H* + 2.
- VLSM: сортировка от большего к меньшему, выделение подряд — адресное пространство экономится.
- Суммирование: общие начальные биты → один маршрут с более коротким префиксом; остерегайтесь лишнего покрытия.
- Для каналов точка-точка — /30 или /31.

## Проверьте себя

:::check
Q: Каков наименьший префикс подсети для 120 узлов?
A: /25 — 128 адресов, 126 узлов (122 ≤ 128).
Q: Каковы размер блока и число узлов для маски 255.255.255.248?
A: Блок 8, узлов 6.
Q: Как просуммировать 10.0.0.0/24, 10.0.1.0/24, 10.0.2.0/24 и 10.0.3.0/24?
A: 10.0.0.0/22.
Q: Почему в VLSM отделы сортируют от большего к меньшему?
A: Чтобы каждый блок начинался с адреса, кратного его размеру (был выровнен), и не оставалось пробелов.
Q: Какой префикс экономен для канала между двумя маршрутизаторами?
A: /30 (2 узла) или /31 по RFC 3021.
:::

:::link https://www.rfc-editor.org/rfc/rfc4632
RFC 4632: Classless Inter-domain Routing (CIDR)
CIDR, префиксы и правила суммирования маршрутов (на английском).
:::
