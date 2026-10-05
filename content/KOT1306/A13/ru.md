---
title: Создание общих сетевых ресурсов и их использование
summary: Общие папки и протокол SMB; открытие папки в сеть — графически, net share, PowerShell; пути UNC и подключение сетевого диска; разрешения общего доступа и NTFS, их совместное действие; скрытые и административные общие ресурсы; общий принтер; Samba в Linux; безопасность; вычисление действующего сетевого разрешения.
---

## Почему это важно

Преподаватели кафедры хранят материалы лекций в одной папке, студенты их читают, но изменить не могут; в папку заданий студент загружает только свой файл. Для этого не нужны флешки и почта — достаточно **общих сетевых ресурсов** (shared folders) и правильных разрешений. Неправильно настроенная общая папка, напротив, — самый распространённый путь утечки данных и распространения вирусов-шифровальщиков.

:::note
**После этого занятия вы сможете:**

- открывать папку в общий доступ и обращаться к ней по пути UNC;
- подключать сетевой диск;
- объяснять различие разрешений общего доступа и NTFS и как они действуют вместе;
- вычислять действующие права пользователя по сети и локально;
- применять правила безопасной настройки общих ресурсов.
:::

## SMB и пути UNC

В Windows протокол общего доступа к файлам и принтерам — **SMB** (Server Message Block, TCP 445). Текущая версия — **SMB 3** (с шифрованием); старый **SMB 1** опасен (его использовал вирус WannaCry) и должен быть отключён.

К общему ресурсу обращаются по пути **UNC** (Universal Naming Convention):

```text
\\SRV-DC01\Kurslar                  имя сервера и имя общего ресурса
\\192.168.50.10\Kurslar\KOT1306     по IP-адресу, с вложенной папкой
\\SRV-DC01\C$                       административный ресурс (только администраторы)
```

## Открытие общего доступа к папке

**Графически**: папка → Properties → **Sharing** → **Advanced Sharing** → «Share this folder» → имя → **Permissions** (разрешения общего доступа). На сервере: Server Manager → File and Storage Services → Shares → **New Share**.

**Командная строка**:

```text
net share Kurslar=D:\Kurslar /grant:Talabalar,READ /grant:Oqituvchilar,FULL
net share                                    список всех общих ресурсов
net use Z: \\SRV-DC01\Kurslar /persistent:yes   подключить сетевой диск
net use                                      подключённые диски
net share Kurslar /delete
```

**PowerShell**:

```text
New-SmbShare -Name "Kurslar" -Path "D:\Kurslar" -ReadAccess "AIFU\Talabalar" -FullAccess "AIFU\Oqituvchilar"
Get-SmbShare
Get-SmbShareAccess -Name "Kurslar"
New-PSDrive -Name Z -PSProvider FileSystem -Root \\SRV-DC01\Kurslar -Persist
```

Имя, оканчивающееся на `$` (`Arxiv$`), — **скрытый ресурс**: в сети не виден, но знающий имя подключится (это удобство, а не защита). `C$`, `ADMIN$`, `IPC$` — **административные** ресурсы.

## Разрешения общего доступа и NTFS

| | Разрешения общего доступа (share) | Разрешения NTFS |
|---|---|---|
| Когда действуют | только при обращении **по сети** | всегда — и по сети, и локально |
| Уровни | Read, Change, Full Control | Read, Read & execute, Modify, Full control и другие |
| Куда назначаются | на весь общий ресурс | на каждую папку и файл, с наследованием |

**Главное правило**: при доступе по сети действующее право — **самое строгое** из разрешений share и NTFS. Внутри каждого из них разрешения пользователя и его групп суммируются (Deny побеждает).

Современная рекомендация: разрешение share делать широким (например, `Authenticated Users — Change` или `Full`), а реальные ограничения задавать более подробными разрешениями **NTFS**. Тогда разрешения управляются в одном месте.

## Общий принтер и Linux

- **Принтер**: свойства принтера → Sharing → «Share this printer»; на клиенте добавить `\\SRV-DC01\Printer1`. В домене — автоматическое подключение через GPO.
- **Linux**: сервер **Samba** предоставляет общие ресурсы SMB клиентам Windows (`/etc/samba/smb.conf`); между Linux-машинами используется также **NFS**. Клиент Linux монтирует ресурс Windows командой `mount -t cifs //server/Kurslar /mnt/kurslar`.

## Практическая работа

1. На `SRV-DC01` создайте папку `D:\Kurslar`, а в ней — папки `Materiallar` (материалы) и `Topshiriqlar` (задания).
2. Группы: `Talabalar`, `Oqituvchilar` (из A12).
3. Общий ресурс `Kurslar`: разрешение share — `Authenticated Users: Change`.
4. NTFS: `Oqituvchilar` — Modify (на всю папку); `Talabalar` — Read & execute на `Materiallar`, Write на `Topshiriqlar` (создавать файлы, не читая чужие — «This folder only» + CREATOR OWNER — Modify).
5. На клиенте зайдите в `\\SRV-DC01\Kurslar` под учётными записями студента и преподавателя и проверьте каждое действие (чтение, создание, удаление).
6. На сервере **Computer Management → Shared Folders → Sessions / Open Files** — посмотрите, кто подключён и какие файлы открыты.

## Разобранный пример: действующее сетевое разрешение

Шкала уровней: `None` < `Read` < `Change` < `Full` (Modify в NTFS соответствует `Change`). В первой строке — число групп, затем каждая группа (имя и члены). Затем число записей share и записи (субъект, уровень), затем число записей NTFS и записи. В конце — число пользователей и их имена. Для каждого пользователя: наивысший уровень по share, наивысший по NTFS (`Everyone` относится ко всем), по сети — меньший из двух, локально — уровень NTFS.

```python
DARAJA = ["None", "Read", "Change", "Full"]

guruhlar = {}                                    # пользователь -> группы
for _ in range(int(input())):
    qism = input().split()
    for azo in qism[1:]:
        guruhlar.setdefault(azo, set()).add(qism[0])
share = [input().split() for _ in range(int(input()))]
ntfs = [input().split() for _ in range(int(input()))]


def eng_yuqori(yozuvlar, subyektlar):
    d = 0
    for subyekt, daraja in yozuvlar:
        if subyekt in subyektlar:
            d = max(d, DARAJA.index(daraja))     # разрешения суммируются
    return d


for _ in range(int(input())):
    user = input().strip()
    subyektlar = {user, "Everyone"} | guruhlar.get(user, set())
    s = eng_yuqori(share, subyektlar)
    n = eng_yuqori(ntfs, subyektlar)
    tarmoq = min(s, n)                           # самое строгое
    print(user + ": по сети " + DARAJA[tarmoq] + ", локально " + DARAJA[n])
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    static final List<String> DARAJA = Arrays.asList("None", "Read", "Change", "Full");

    static int engYuqori(List<String[]> yozuvlar, Set<String> subyektlar) {
        int d = 0;
        for (String[] e : yozuvlar) {
            if (subyektlar.contains(e[0])) d = Math.max(d, DARAJA.indexOf(e[1]));   // разрешения суммируются
        }
        return d;
    }

    static List<String[]> oqi(Scanner in) {
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> yozuvlar = new ArrayList<>();
        for (int i = 0; i < n; i++) yozuvlar.add(in.nextLine().trim().split(" "));
        return yozuvlar;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Set<String>> guruhlar = new HashMap<>();          // пользователь -> группы
        for (String[] qism : oqi(in)) {
            for (int j = 1; j < qism.length; j++) {
                guruhlar.computeIfAbsent(qism[j], k -> new HashSet<>()).add(qism[0]);
            }
        }
        List<String[]> share = oqi(in);
        List<String[]> ntfs = oqi(in);
        int q = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < q; i++) {
            String user = in.nextLine().trim();
            Set<String> subyektlar = new HashSet<>(guruhlar.getOrDefault(user, new HashSet<>()));
            subyektlar.add(user);
            subyektlar.add("Everyone");
            int s = engYuqori(share, subyektlar);
            int n = engYuqori(ntfs, subyektlar);
            int tarmoq = Math.min(s, n);                               // самое строгое
            System.out.println(user + ": по сети " + DARAJA.get(tarmoq) + ", локально " + DARAJA.get(n));
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const vector<string> DARAJA = {"None", "Read", "Change", "Full"};

vector<vector<string>> oqi() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    vector<vector<string>> yozuvlar;
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream ss(satr);
        vector<string> qism;
        string s;
        while (ss >> s) qism.push_back(s);
        yozuvlar.push_back(qism);
    }
    return yozuvlar;
}

int engYuqori(const vector<vector<string>>& yozuvlar, const set<string>& subyektlar) {
    int d = 0;
    for (auto& e : yozuvlar) {
        if (subyektlar.count(e[0])) {
            int indeks = find(DARAJA.begin(), DARAJA.end(), e[1]) - DARAJA.begin();
            d = max(d, indeks);                                        // разрешения суммируются
        }
    }
    return d;
}

int main() {
    map<string, set<string>> guruhlar;                                 // пользователь -> группы
    for (auto& qism : oqi()) {
        for (size_t j = 1; j < qism.size(); j++) guruhlar[qism[j]].insert(qism[0]);
    }
    auto share = oqi();
    auto ntfs = oqi();
    for (auto& qator : oqi()) {
        string user = qator[0];
        set<string> subyektlar = guruhlar[user];
        subyektlar.insert(user);
        subyektlar.insert("Everyone");
        int s = engYuqori(share, subyektlar);
        int n = engYuqori(ntfs, subyektlar);
        int tarmoq = min(s, n);                                        // самое строгое
        cout << user << ": по сети " << DARAJA[tarmoq] << ", локально " << DARAJA[n] << endl;
    }
    return 0;
}
```

Ввод:

```text
2
Talabalar ali vali
Oqituvchilar nodir
2
Everyone Read
Oqituvchilar Full
3
Talabalar Change
Oqituvchilar Change
Administrators Full
3
ali
nodir
mehmon
```

Вывод:

```text
ali: по сети Read, локально Change
nodir: по сети Change, локально Change
mehmon: по сети None, локально None
```

`ali` по NTFS может изменять файлы, но по сети только читает — ограничивает разрешение share (Everyone: Read). У `nodir` share — Full, но NTFS — Change, итог — Change. `mehmon` (гость) по share может читать, но в NTFS ему ничего не выдано — войти он не сможет.

:::warning
**Частые ошибки**

- **Настраивать только разрешения share.** При локальном доступе (или через RDP) они не действуют — основная защита в NTFS.
- **Оставлять `Everyone — Full Control` и в share, и в NTFS.** Любой в сети или вирус-шифровальщик уничтожит папку.
- **Считать скрытый ресурс (`$`) защитой.** Его просто нет в списке; знающий имя подключится.
- **Оставлять включённым SMB 1.** Старый и уязвимый протокол; проверьте `Get-SmbServerConfiguration` и отключите.
- **Открывать порт 445 в Интернет.** SMB используется только во внутренней сети или через VPN.
:::

:::tip
Откройте общий доступ к папке между двумя компьютерами (или двумя ВМ) и проследите подключение в Wireshark с фильтром `smb2`: найдите запросы Negotiate (согласование версии), Session Setup (аутентификация), Tree Connect (подключение к ресурсу) и Create/Read.
:::

## Итоги

- Протокол общего доступа к файлам в Windows — SMB (TCP 445), обращение — по пути UNC `\\сервер\ресурс`.
- Общий доступ: Advanced Sharing, `net share`, `New-SmbShare`; сетевой диск: `net use Z:`.
- Разрешения share действуют только по сети, NTFS — всегда; действующее право по сети — самое строгое из двух.
- Рекомендация: широкое разрешение share, ограничения в NTFS; SMB 1 отключён, порт 445 закрыт для Интернета.

## Проверьте себя

:::check
Q: Как в Windows обратиться к общей папке по сети?
A: По пути UNC: `\\сервер\папка`.
Q: Как называется протокол общего доступа к файлам Windows?
A: SMB (Server Message Block).
Q: Разрешение share — Full, NTFS — Read. Каково действующее право по сети?
A: Read — действует самое строгое из share и NTFS.
Q: Чем особенен общий ресурс с именем `Arxiv$`?
A: Он скрытый — не виден в списке сети, но пользователь, знающий имя, может подключиться.
Q: Как подключить сетевой диск из командной строки?
A: `net use Z: \\SRV-DC01\Kurslar`.
:::

:::link https://learn.microsoft.com/en-us/windows-server/storage/file-server/file-server-smb-overview
SMB overview
Обзор протокола SMB и файлового сервера от Microsoft (на английском).
:::
