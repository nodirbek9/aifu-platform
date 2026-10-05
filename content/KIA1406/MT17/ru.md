---
title: Резервное копирование (Backup and Restore) в Windows. Виды привилегий в Windows, порядок управления доступом к файлам и каталогам
summary: Стратегия резервного копирования — правило 3-2-1, полные, инкрементные и дифференциальные копии, RPO и RTO, проверка восстановления, офлайн-копия против шифровальщиков; средства Windows — История файлов (File History), Windows Backup, образ системы (Backup and Restore), точки восстановления, wbadmin; типы учётных записей и привилегии в Windows — администратор, стандартный пользователь, UAC, права пользователей; разрешения NTFS — базовые и особые разрешения, наследование, явный запрет, действующие разрешения, общие и NTFS-разрешения вместе; icacls; программный расчёт действующих разрешений.
---

## Почему эта тема важна?

Две темы — «что делать, если данные потеряны?» и «кто может получить доступ к данным?» — служат одной цели: **доступности** и **конфиденциальности** информации (CIA). После шифровальщика, отказа диска или случайного удаления организацию спасает только проверенная резервная копия; неправильные разрешения, наоборот, открывают секретную папку всей организации. Обе задачи — повседневная работа администратора Windows.

:::note
**После этой самостоятельной работы вы сможете:**

- составить стратегию резервного копирования (3-2-1, виды копий, RPO и RTO);
- пользоваться средствами Windows для создания копий и восстановления;
- различать типы учётных записей Windows, UAC и права пользователей;
- объяснить разрешения NTFS, наследование и явный запрет;
- вычислить действующие разрешения пользователя.
:::

## Стратегия резервного копирования

**Правило 3-2-1**: не менее **3** копий данных, на **2** разных носителях, **1** из них — в другом месте (другое здание или облако). Для защиты от шифровальщиков современная рекомендация — **3-2-1-1-0**: ещё 1 офлайн- или неизменяемая (immutable) копия и 0 ошибок при проверке восстановления.

| Вид копии | Что копируется | Достоинство | Недостаток |
|---|---|---|---|
| **Полная** | всё | восстановление простое и быстрое | много места и времени |
| **Инкрементная** | изменения с последней копии любого вида | быстро, мало места | для восстановления нужна вся цепочка |
| **Дифференциальная** | изменения с последней полной копии | для восстановления достаточно 2 копий | со временем растёт |

**RPO** (Recovery Point Objective) — потерю данных за какой период мы допускаем (например, 24 часа → ежедневная копия). **RTO** (Recovery Time Objective) — за какое время система должна быть восстановлена. Оба показателя задаются требованиями бизнеса и оценкой рисков (M2).

**Проверка восстановления**: непроверенная копия — не копия. Регулярно (например, раз в квартал) восстанавливайте случайные файлы и всю систему в тестовую среду.

## Средства резервного копирования Windows

| Средство | Что сохраняет | Где |
|---|---|---|
| **История файлов** (File History) | папки пользователя, предыдущие версии файлов | Settings или Control Panel → File History; внешний диск или сетевая папка |
| **Windows Backup** (Windows 11) | папки, настройки, список приложений — в OneDrive | приложение Windows Backup |
| **Образ системы** (Backup and Restore, Windows 7) | весь диск вместе с ОС | Control Panel → Backup and Restore (Windows 7) → Create a system image |
| **Точки восстановления** (System Restore) | системные файлы и реестр (не личные файлы!) | `sysdm.cpl` → System Protection |
| **wbadmin** | копирование и восстановление из командной строки | `wbadmin start backup ...` (полностью — в Windows Server) |

**Практическая работа (на тестовом компьютере или в виртуальной машине)**:

1. Подключите внешний диск и включите **Историю файлов**; настройте частоту копирования (например, каждый час) и срок хранения.
2. Создайте тестовый файл, измените и удалите его; восстановите прежнюю версию через «Restore personal files».
3. Создайте **образ системы** (Control Panel → Backup and Restore (Windows 7) → Create a system image) и подготовьте диск восстановления.
4. Включите защиту системы в `sysdm.cpl` и вручную создайте **точку восстановления**.
5. В отчёте обоснуйте RPO и RTO для своего дома или небольшого офиса.

Замечание: если резервный диск подключён постоянно, шифровальщик зашифрует и его — отключайте его после копирования или используйте облачное/сетевое хранилище с поддержкой версий.

## Типы учётных записей и привилегии в Windows

| Учётная запись / механизм | Описание |
|---|---|
| **Администратор** | полное управление системой: установка программ, настройки, другие учётные записи |
| **Стандартный пользователь** | повседневная работа; не может менять системные настройки — **рекомендуется для повседневной работы** |
| **UAC** (User Account Control) | даже администратор обычно работает с ограниченным маркером; для привилегированного действия запрашивается подтверждение |
| **Гость, служебные учётные записи** | SYSTEM, LocalService, NetworkService — для служб; учётная запись «Гость» должна быть отключена |
| **Права пользователей** (User Rights Assignment) | `secpol.msc` → Local Policies: «Log on locally», «Shut down the system», «Back up files and directories», «Debug programs» и др. |

**Минимальные привилегии** (M11): повседневная работа — в стандартной учётной записи, учётная запись администратора — только для управления. Учётные записи и группы: `lusrmgr.msc` или `net user`, `net localgroup Administrators`.

## Разрешения NTFS

**Базовые разрешения**: Full control (полный доступ, включая изменение разрешений), Modify (изменение и удаление), Read & execute, List folder contents, Read, Write. Каждое из них — набор **особых разрешений** (Advanced → Permissions).

**Правила**:

- **Наследование**: разрешения папки обычно переходят к файлам и папкам внутри; его можно прервать (Disable inheritance).
- **Явный запрет (Deny) имеет приоритет**: разрешения пользователя и его групп объединяются, но явный Deny отнимает это право.
- **Действующие разрешения**: вкладка Advanced → Effective Access показывает реальные права пользователя.
- **Общий доступ + NTFS**: при доступе по сети действует **более строгое** из разрешений общего ресурса (Share) и NTFS.
- **Владелец** (Owner) всегда может изменить разрешения.

В командной строке:

```text
icacls D:\Reports
icacls D:\Reports /grant Buxgalteriya:(OI)(CI)M
icacls D:\Reports /deny Stajyorlar:(OI)(CI)W
icacls D:\Reports /inheritance:d
icacls D:\Reports /save permissions.txt /t
```

`(OI)(CI)` — наследование файлами и папками, `M` — Modify; `/save` — сохранить разрешения в файл (резервная копия перед изменениями). (Buxgalteriya — группа бухгалтерии, Stajyorlar — стажёры.)

## Разобранный пример: действующие разрешения NTFS

Упрощённая модель: права — R (чтение), W (запись), X (выполнение), D (удаление). В первой строке — число записей ACL, затем записи (`allow` или `deny`, пользователь или группа, права). В следующих строках — пользователь и его группы (через запятую). Каждый пользователь входит и в группу `Everyone`. Действующие права = объединение всех подходящих `allow` минус все подходящие `deny`. Права, потерянные из-за запрета, покажите отдельно.

```python
import sys

TARTIB = "RWXD"
satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
a = int(satrlar[0][0])
acl = satrlar[1:1 + a]
for qator in satrlar[1 + a:]:
    kim = qator[0]
    guruhlar = set(qator[1].split(",")) | {kim, "Everyone"}   # пользователь + его группы + Everyone
    ruxsat, taqiq = set(), set()
    for tur, kimga, huquq in acl:
        if kimga in guruhlar:
            (ruxsat if tur == "allow" else taqiq).update(huquq)
    natija = "".join(h for h in TARTIB if h in ruxsat and h not in taqiq) or "-"   # deny имеет приоритет
    rad = "".join(h for h in TARTIB if h in ruxsat and h in taqiq)
    print(f"{kim}: {natija}" + (f" (deny: {rad})" if rad else ""))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String TARTIB = "RWXD";
        int a = Integer.parseInt(in.next());
        List<String[]> acl = new ArrayList<>();
        for (int i = 0; i < a; i++) acl.add(new String[] {in.next(), in.next(), in.next()});
        while (in.hasNext()) {
            String kim = in.next();
            Set<String> guruhlar = new HashSet<>(Arrays.asList(in.next().split(",")));
            guruhlar.add(kim);                                   // пользователь + его группы + Everyone
            guruhlar.add("Everyone");
            Set<Character> ruxsat = new HashSet<>(), taqiq = new HashSet<>();
            for (String[] y : acl) {
                if (!guruhlar.contains(y[1])) continue;
                for (char h : y[2].toCharArray()) {
                    if (y[0].equals("allow")) ruxsat.add(h);
                    else taqiq.add(h);
                }
            }
            StringBuilder natija = new StringBuilder(), rad = new StringBuilder();
            for (char h : TARTIB.toCharArray()) {
                if (ruxsat.contains(h) && !taqiq.contains(h)) natija.append(h);   // deny имеет приоритет
                if (ruxsat.contains(h) && taqiq.contains(h)) rad.append(h);
            }
            String qator = kim + ": " + (natija.length() > 0 ? natija : "-");
            if (rad.length() > 0) qator += " (deny: " + rad + ")";
            System.out.println(qator);
        }
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const string TARTIB = "RWXD";
    int a;
    cin >> a;
    vector<vector<string>> acl(a, vector<string>(3));
    for (auto& y : acl) cin >> y[0] >> y[1] >> y[2];
    string kim, gs;
    while (cin >> kim >> gs) {
        set<string> guruhlar = {kim, "Everyone"};               // пользователь + его группы + Everyone
        stringstream ss(gs);
        string g;
        while (getline(ss, g, ',')) guruhlar.insert(g);
        set<char> ruxsat, taqiq;
        for (const auto& y : acl) {
            if (!guruhlar.count(y[1])) continue;
            for (char h : y[2]) (y[0] == "allow" ? ruxsat : taqiq).insert(h);
        }
        string natija, rad;
        for (char h : TARTIB) {
            if (ruxsat.count(h) && !taqiq.count(h)) natija += h;   // deny имеет приоритет
            if (ruxsat.count(h) && taqiq.count(h)) rad += h;
        }
        cout << kim << ": " << (natija.empty() ? "-" : natija);
        if (!rad.empty()) cout << " (deny: " << rad << ")";
        cout << endl;
    }
    return 0;
}
```

Ввод:

```text
5
allow Everyone R
allow Buxgalteriya RW
allow Administrators RWXD
deny Stajyorlar WD
allow ali X
ali Buxgalteriya
vali Buxgalteriya,Stajyorlar
olim Administrators
sardor Stajyorlar
kamol Users
```

Вывод:

```text
ali: RWX
vali: R (deny: W)
olim: RWXD
sardor: R
kamol: R
```

vali получил право записи от группы бухгалтерии, но он ещё и стажёр — явный запрет победил, и запись отнята. X, выданный лично ali, добавился к правам групп. Право чтения для `Everyone` действует на всех, включая постороннего kamol, — для секретных папок это ошибка. В реальных системах вкладка «Effective Access» выполняет тот же расчёт (и дополнительно учитывает разрешения общего ресурса, владение и привилегии).

:::warning
**Частые ошибки**

- **Никогда не проверять восстановление из копии.** В день аварии выясняется, что копия не работает.
- **Оставлять резервный диск постоянно подключённым.** Шифровальщик зашифрует и его.
- **Считать точку восстановления резервной копией.** Она не сохраняет личные файлы.
- **Повседневная работа под администратором.** Вредоносная программа тоже работает с правами администратора.
- **Давать Full control группам Everyone или Users.** Выдавайте права группам и только в нужном объёме; Deny используйте редко и обдуманно.
:::

:::tip
Перед изменением разрешений сохраните текущее состояние командой `icacls папка /save backup.txt /t` — при ошибке верните его командой `icacls папка /restore backup.txt`. Это «резервная копия разрешений».
:::

## Итоги

- Резервное копирование: 3-2-1 (или 3-2-1-1-0), полные, инкрементные и дифференциальные копии; RPO и RTO — из требований бизнеса; восстановление регулярно проверяется.
- Windows: История файлов, Windows Backup, образ системы, точки восстановления, wbadmin; резервный диск отключается после копирования.
- Привилегии: администратор и стандартная учётная запись, UAC, права пользователей; повседневная работа — в стандартной учётной записи.
- NTFS: базовые и особые разрешения, наследование, приоритет Deny, действующие разрешения; по сети — более строгое из общего и NTFS.
- `icacls` — просмотр, выдача, запрет и сохранение разрешений.

## Проверьте себя

:::check
Q: Что означает правило 3-2-1?
A: Не менее 3 копий, на 2 разных носителях, 1 из них — в другом месте.
Q: Чем инкрементная копия отличается от дифференциальной?
A: Инкрементная сохраняет изменения с последней копии любого вида, дифференциальная — с последней полной.
Q: Пользователь получил Allow Write от одной группы и Deny Write от другой. Может ли он писать?
A: Нет, явный запрет имеет приоритет.
Q: При доступе по сети разрешение общего ресурса — Read, а NTFS — Modify. Каково действующее право?
A: Read — более строгое из двух.
Q: Почему повседневную работу рекомендуют выполнять в стандартной учётной записи?
A: Вредоносная программа или ошибка действуют с правами пользователя; стандартная учётная запись не может изменить систему.
:::

:::link https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/icacls
Microsoft Learn: icacls
Описание команды icacls, разрешений и флагов наследования (на английском языке).
:::
