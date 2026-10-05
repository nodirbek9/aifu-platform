---
title: Управление пользователями и их полномочиями в операционной системе Windows
summary: Локальные и доменные учётные записи; встроенные учётные записи и группы; создание пользователей и групп через lusrmgr.msc, net user, net localgroup и PowerShell; UAC; политики паролей и блокировки; разрешения NTFS, наследование, приоритет Deny; icacls; принцип наименьших привилегий и назначение прав через группы (AGDLP); вычисление действующего разрешения.
---

## Почему это важно

В компьютерном классе студент не должен иметь возможности удалить работу другого студента, а преподаватель должен видеть все работы. В папку бухгалтерии должны попадать только бухгалтеры, а учётная запись администратора никогда не должна использоваться для повседневной работы. Всё это обеспечивается **учётными записями, группами и разрешениями**. Значительная часть инцидентов безопасности начинается именно с избыточных прав.

:::note
**После этого занятия вы сможете:**

- различать локальные и доменные учётные записи, встроенные учётные записи и группы;
- создавать пользователей и группы графическими средствами и из командной строки;
- настраивать политики паролей и блокировки учётных записей;
- назначать разрешения NTFS и объяснять правила наследования и Deny;
- вычислять действующее разрешение пользователя по группам и ACL.
:::

## Учётные записи и группы

| Тип учётной записи | Где хранится | Пример |
|---|---|---|
| **Локальная** | в базе SAM компьютера | `PC07\talaba` |
| **Доменная** | в Active Directory | `AIFU\a.karimov` |
| **Учётная запись Microsoft** | в облаке (домашние компьютеры) | `имя@outlook.com` |

Встроенные учётные записи: **Administrator** (полные права; для обычной работы не используется, рекомендуется переименовать или отключить), **Guest** (по умолчанию отключена — так и должно оставаться).

Встроенные группы: **Administrators** (полное управление), **Users** (обычная работа), **Remote Desktop Users** (вход на удалённый рабочий стол), **Backup Operators** (резервное копирование), **Guests**. В домене: **Domain Admins**, **Domain Users** и другие.

**UAC** (User Account Control): даже администратор по умолчанию работает с обычными правами; когда нужно изменение системы, появляется окно подтверждения.

## Создание пользователей и групп

Графически: **lusrmgr.msc** (Local Users and Groups) или **Computer Management**; в домене — **Active Directory Users and Computers** (dsa.msc).

Командная строка (от имени администратора):

```text
net user a.karimov Parol#2026 /add /fullname:"Aziz Karimov"
net localgroup Talabalar /add
net localgroup Talabalar a.karimov /add
net user a.karimov /active:no            временно отключить учётную запись
net user a.karimov                       сведения об учётной записи
whoami /groups                           группы текущего пользователя
```

PowerShell:

```text
$p = Read-Host -AsSecureString "Пароль"
New-LocalUser -Name "a.karimov" -Password $p -FullName "Aziz Karimov"
New-LocalGroup -Name "Talabalar"
Add-LocalGroupMember -Group "Talabalar" -Member "a.karimov"
Get-LocalGroupMember -Group "Administrators"
```

## Политики паролей и блокировки

**secpol.msc** → Account Policies (в домене — GPO Default Domain Policy):

| Параметр | Рекомендация |
|---|---|
| Minimum password length | 12 символов |
| Password must meet complexity requirements | Enabled |
| Maximum password age | 90 дней (или без срока при длинном пароле — современная рекомендация) |
| Enforce password history | 10 предыдущих паролей |
| Account lockout threshold | 5 неверных попыток |
| Account lockout duration | 15 минут |

Блокировка замедляет атаку подбора пароля (brute force).

## Разрешения NTFS

Свойства папки или файла → вкладка **Security**. У каждого объекта есть **ACL** (список управления доступом): какое разрешение какому пользователю или группе **разрешено** (Allow) или **запрещено** (Deny).

| Разрешение | Что позволяет |
|---|---|
| **Full control** | всё, включая изменение разрешений и смену владельца |
| **Modify** | чтение, запись, удаление |
| **Read & execute** | чтение и запуск программ |
| **List folder contents** | просмотр содержимого папки |
| **Read** | чтение файлов |
| **Write** | создание новых файлов и запись |

Правила:

1. **Разрешения суммируются**: Allow, выданные пользователю и всем его группам, объединяются.
2. **Deny побеждает**: если в какой-то группе есть Deny, Allow из другой группы не может его отменить (для явно заданных разрешений).
3. **Ничего не выдано** — значит, доступа нет (неявный запрет).
4. **Наследование**: разрешения папки переходят на вложенные файлы и папки; наследование можно прервать (Advanced → Disable inheritance).

Из командной строки: `icacls D:\Kurslar` — просмотр ACL; `icacls D:\Kurslar /grant Talabalar:(OI)(CI)RX` — дать группе Talabalar чтение и выполнение (с наследованием вложенными объектами).

**Принцип наименьших привилегий**: каждому — минимум прав, нужный для работы. **Права назначаются группам**, а пользователи добавляются в группы (в домене **AGDLP**: Account → Global group → Domain Local group → Permission).

## Разобранный пример: вычисление действующего разрешения

В первой строке — число групп *g*, в следующих *g* строках — имя группы и её члены. Затем число записей ACL и каждая запись: субъект (пользователь, группа или `Everyone`), `Allow`/`Deny` и буквы разрешений (`R` — чтение, `W` — запись, `D` — удаление). Затем запросы: пользователь и одна буква разрешения. Для каждого запроса выведите результат: если есть Deny — `запрещено (Deny)`, если есть Allow — `разрешено`, если ничего нет — `запрещено (не выдано)`.

```python
g = int(input())
guruhlar = {}                                  # пользователь -> множество групп
for _ in range(g):
    qism = input().split()
    for azo in qism[1:]:
        guruhlar.setdefault(azo, set()).add(qism[0])
acl = [input().split() for _ in range(int(input()))]
for _ in range(int(input())):
    user, ruxsat = input().split()
    subyektlar = {user, "Everyone"} | guruhlar.get(user, set())
    allow = deny = False
    for subyekt, tur, harflar in acl:
        if subyekt in subyektlar and ruxsat in harflar:
            if tur == "Deny":
                deny = True
            else:
                allow = True
    if deny:
        print(user, ruxsat + ": запрещено (Deny)")       # Deny всегда имеет приоритет
    elif allow:
        print(user, ruxsat + ": разрешено")
    else:
        print(user, ruxsat + ": запрещено (не выдано)")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int g = Integer.parseInt(in.nextLine().trim());
        Map<String, Set<String>> guruhlar = new HashMap<>();       // пользователь -> множество групп
        for (int i = 0; i < g; i++) {
            String[] qism = in.nextLine().trim().split(" ");
            for (int j = 1; j < qism.length; j++) {
                guruhlar.computeIfAbsent(qism[j], k -> new HashSet<>()).add(qism[0]);
            }
        }
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> acl = new ArrayList<>();
        for (int i = 0; i < n; i++) acl.add(in.nextLine().trim().split(" "));
        int q = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < q; i++) {
            String[] soruv = in.nextLine().trim().split(" ");
            String user = soruv[0];
            String ruxsat = soruv[1];
            Set<String> subyektlar = new HashSet<>(guruhlar.getOrDefault(user, new HashSet<>()));
            subyektlar.add(user);
            subyektlar.add("Everyone");
            boolean allow = false, deny = false;
            for (String[] e : acl) {
                if (subyektlar.contains(e[0]) && e[2].contains(ruxsat)) {
                    if (e[1].equals("Deny")) deny = true;
                    else allow = true;
                }
            }
            if (deny) {
                System.out.println(user + " " + ruxsat + ": запрещено (Deny)");   // Deny всегда имеет приоритет
            } else if (allow) {
                System.out.println(user + " " + ruxsat + ": разрешено");
            } else {
                System.out.println(user + " " + ruxsat + ": запрещено (не выдано)");
            }
        }
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
    string satr;
    getline(cin, satr);
    int g = stoi(satr);
    map<string, set<string>> guruhlar;                             // пользователь -> множество групп
    for (int i = 0; i < g; i++) {
        getline(cin, satr);
        istringstream ss(satr);
        string nom, azo;
        ss >> nom;
        while (ss >> azo) guruhlar[azo].insert(nom);
    }
    getline(cin, satr);
    int n = stoi(satr);
    vector<vector<string>> acl(n, vector<string>(3));
    for (auto& e : acl) cin >> e[0] >> e[1] >> e[2];
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        string user, ruxsat;
        cin >> user >> ruxsat;
        set<string> subyektlar = guruhlar[user];
        subyektlar.insert(user);
        subyektlar.insert("Everyone");
        bool allow = false, deny = false;
        for (auto& e : acl) {
            if (subyektlar.count(e[0]) && e[2].find(ruxsat) != string::npos) {
                if (e[1] == "Deny") deny = true;
                else allow = true;
            }
        }
        if (deny) {
            cout << user << " " << ruxsat << ": запрещено (Deny)" << endl;   // Deny всегда имеет приоритет
        } else if (allow) {
            cout << user << " " << ruxsat << ": разрешено" << endl;
        } else {
            cout << user << " " << ruxsat << ": запрещено (не выдано)" << endl;
        }
    }
    return 0;
}
```

Ввод (названия групп на узбекском: студенты, преподаватели, лаборанты):

```text
3
Talabalar ali vali sami
Oqituvchilar nodir
Laborantlar sami
5
Everyone Allow R
Talabalar Allow W
Laborantlar Deny D
Oqituvchilar Allow RWD
vali Deny W
6
ali W
vali W
sami D
ali D
nodir D
mehmon R
```

Вывод:

```text
ali W: разрешено
vali W: запрещено (Deny)
sami D: запрещено (Deny)
ali D: запрещено (не выдано)
nodir D: разрешено
mehmon R: разрешено
```

У `vali` есть право записи через группу Talabalar, но лично выданный ему Deny победил. `mehmon` (гость) не входит ни в одну группу, но может читать через `Everyone` — поэтому давать `Everyone` широкие права опасно.

:::warning
**Частые ошибки**

- **Работать повседневно под администратором.** Вредоносная программа тоже получает права администратора.
- **Назначать разрешения каждому пользователю отдельно.** Не 300 студентам по одному, а одной группе.
- **Злоупотреблять Deny.** Поскольку он всегда побеждает, это ведёт к неожиданным запретам; обычно достаточно не выдавать Allow.
- **Давать `Everyone` Full control или Modify.** Это позволяет любому в сети удалить папку.
- **Не удалять учётную запись уволившегося сотрудника.** Учётные записи ушедших сразу удаляются или отключаются.
:::

:::tip
Выполните на своём компьютере `whoami /groups` и `whoami /priv`: в каких вы группах и какие у вас привилегии? Затем выполните в любой папке `icacls .` и поясните отметки `(OI)(CI)`, `(F)`, `(M)`, `(RX)` в ACL.
:::

## Итоги

- Учётные записи: локальные (SAM), доменные (AD); встроенные Administrator и Guest требуют осторожности.
- Пользователи и группы: lusrmgr.msc, dsa.msc, `net user`, `net localgroup`, `New-LocalUser`.
- Политики: длина и сложность пароля, история, порог блокировки.
- NTFS: Allow суммируются, Deny побеждает, ничего — значит запрет; наследование идёт вниз; просмотр через `icacls`.
- Наименьшие привилегии и назначение прав через группы упрощают управление и уменьшают ошибки.

## Проверьте себя

:::check
Q: Почему права назначают группам, а не пользователям?
A: Управлять проще и ошибок меньше: нового сотрудника достаточно добавить в группу.
Q: Пользователь получает Allow Write через одну группу и Deny Write через другую. Каков результат?
A: Запись запрещена — Deny имеет приоритет.
Q: Как называется консоль управления локальными пользователями и группами?
A: lusrmgr.msc (Local Users and Groups).
Q: Зачем нужна политика блокировки учётных записей?
A: После неверных попыток учётная запись временно блокируется, и подбор пароля замедляется.
Q: Как разрешение, выданное папке, переходит на файлы внутри неё?
A: Через наследование — если оно не прервано.
:::

:::link https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/manage/understand-security-groups
Active Directory security groups
Руководство Microsoft по группам безопасности и правам (на английском).
:::
