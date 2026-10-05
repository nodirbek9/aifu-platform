---
title: Управление правами файлов и каталогов в ОС Linux
summary: Практическое управление правами файлов и каталогов в Linux — пользователи, группы, /etc/passwd и /etc/shadow; вывод ls -l; смысл r, w, x для файлов и каталогов; chmod (символьная и числовая запись), chown, chgrp; umask и права новых файлов по умолчанию; специальные биты — setuid, setgid, sticky; ACL (getfacl, setfacl); sudo и минимальные привилегии; порядок проверки прав (владелец → группа → остальные); программное задание — перевод строки rwx в восьмеричный вид.
---

## Почему эта тема важна?

Linux — основная система серверов, облаков и контейнеров. Модель DAC из M11 реализуется в Linux через **владельца**, **группу** и три тройки прав каждого файла. Неправильные права — одна из самых частых ошибок: закрытый SSH-ключ, читаемый всеми, веб-каталог, доступный всем на запись, или лишняя setuid-программа открывают путь к полному захвату системы.

:::note
**После этого занятия вы сможете:**

- читать вывод `ls -l` и объяснять права файлов и каталогов;
- менять права командами `chmod`, `chown`, `chgrp`, `umask`;
- знать назначение и риски битов setuid, setgid и sticky;
- показать, в каком порядке ядро проверяет права доступа;
- написать программу перевода строки `rwx` в восьмеричный вид (задание A11 на платформе).
:::

:::warning
Выполняйте практическую работу в виртуальной машине Linux или в WSL, в своём тестовом каталоге. Не пробуйте `chmod -R` и `chown -R` на системных каталогах вроде `/etc` и `/usr` — система может перестать работать.
:::

## Основы прав

```text
-rwxr-x--- 1 ali dev 4096 Oct  5 10:00 skript.sh
```

Первый символ — тип (`-` файл, `d` каталог, `l` символическая ссылка); далее три тройки: **владелец** (ali), **группа** (dev), **остальные**. В каждой тройке — `r`, `w`, `x` или `-`.

| Право | Для файла | Для каталога |
|---|---|---|
| `r` (4) | читать содержимое | видеть список имён внутри (`ls`) |
| `w` (2) | изменять содержимое | создавать, удалять и переименовывать файлы внутри |
| `x` (1) | выполнять как программу | входить в него (`cd`) и обращаться к файлам |

Обратите внимание: для удаления файла нужно право `w` не на файл, а на **каталог**. Пользователи хранятся в `/etc/passwd` (читается всеми), а хеши паролей — в `/etc/shadow` (только root).

## Практическая работа

**Шаг 1. Наблюдение.**

```text
id
ls -l /etc/passwd /etc/shadow
ls -ld /tmp ~
stat file.txt
```

`id` — текущий пользователь и его группы; обратите внимание на `t` (sticky-бит) у `/tmp`.

**Шаг 2. chmod, chown, chgrp.**

```text
mkdir ~/test && cd ~/test
echo hello > file.txt
chmod u=rw,g=r,o= file.txt      # символьная запись
chmod 640 file.txt              # то же, числовая запись
chmod +x script.sh              # добавить право выполнения всем
sudo chown ali:dev file.txt     # сменить владельца и группу
chgrp dev file.txt
```

После каждой команды проверяйте результат через `ls -l`. Попробуйте прочитать файл от имени другого пользователя (`sudo -u olim cat file.txt`).

**Шаг 3. umask.** Команда `umask` выводит текущую маску (обычно `0022`). Выполните `umask 077`, создайте новый файл и посмотрите его права — `rw-------`, только для владельца.

**Шаг 4. Специальные биты.**

| Бит | Число | Смысл для файла | Смысл для каталога |
|---|---|---|---|
| **setuid** | 4000 | программа работает с правами **владельца** (`/usr/bin/passwd` — root) | — |
| **setgid** | 2000 | программа работает с правами группы | новые файлы наследуют группу каталога (общая папка команды) |
| **sticky** | 1000 | — | файл может удалить только его владелец (`/tmp`) |

`find / -perm -4000 -type f 2>/dev/null` — все setuid-программы в системе. Незнакомая setuid-root-программа — серьёзный риск: ошибка в ней позволяет обычному пользователю стать root.

**Шаг 5. ACL.** Когда троек недостаточно (например, право чтения для ещё одного пользователя):

```text
setfacl -m u:olim:r file.txt
getfacl file.txt
```

В выводе `ls -l` в конце прав появится знак `+`.

**Шаг 6. sudo.** Не работайте постоянно под root; выполняйте через `sudo` только нужную команду. Редактируйте `/etc/sudoers` только через `visudo`; `sudo -l` — разрешённые вам команды.

**Шаг 7. Программа.** Выполните задание **Права Linux: из rwx в восьмеричный вид** на платформе.

## Разобранный пример 1: umask и права по умолчанию

Новый файл «запрашивает» права 666 (`rw-rw-rw-`), новый каталог — 777; биты маски **umask** убираются: права = запрошенные AND (NOT umask). Каждый элемент — umask (восьмеричный); выведите права нового файла и каталога в восьмеричном виде.

```python
import sys

for t in sys.stdin.read().split():
    niqob = int(t, 8)                             # читаем восьмеричное число
    fayl = 0o666 & ~niqob                         # биты маски убираются
    katalog = 0o777 & ~niqob
    print(f"umask {t}: файл {fayl:03o}, каталог {katalog:03o}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int niqob = Integer.parseInt(t, 8);                // читаем восьмеричное число
            int fayl = 0666 & ~niqob;                          // биты маски убираются
            int katalog = 0777 & ~niqob;
            System.out.println(String.format("umask %s: файл %03o, каталог %03o", t, fayl, katalog));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string t;
    while (cin >> t) {
        int niqob = stoi(t, nullptr, 8);                       // читаем восьмеричное число
        int fayl = 0666 & ~niqob;                              // биты маски убираются
        int katalog = 0777 & ~niqob;
        printf("umask %s: файл %03o, каталог %03o\n", t.c_str(), fayl, katalog);
    }
    return 0;
}
```

Ввод:

```text
022 002 077 027 000
```

Вывод:

```text
umask 022: файл 644, каталог 755
umask 002: файл 664, каталог 775
umask 077: файл 600, каталог 700
umask 027: файл 640, каталог 750
umask 000: файл 666, каталог 777
```

`022` — значение по умолчанию во многих системах: остальные могут читать, но не писать. `077` — для пользователей, работающих с конфиденциальными данными: новые файлы открыты только владельцу. `000` опасно: любой может изменить новый файл. Обратите внимание: umask никогда не добавляет файлам `x` — для исполняемого файла нужно явно выполнить `chmod +x`.

## Разобранный пример 2: порядок проверки доступа

Ядро Linux проверяет доступ в следующем порядке и применяет **только одну** тройку: если пользователь — владелец, то тройку владельца (тройки группы и остальных уже не рассматриваются!); иначе, если он в группе, — тройку группы; иначе — тройку остальных. root всегда получает `r` и `w`, а `x` — если есть хотя бы один бит `x`. Ввод: *m* файлов (имя владелец группа восьмеричный_режим), *u* пользователей (имя группа1,группа2), затем запросы (пользователь операция файл). Выведите решение и применённую тройку.

```python
import sys

BIT = {"r": 4, "w": 2, "x": 1}
satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
i = 0
fayllar = {}
m = int(satrlar[i][0])
i += 1
for _ in range(m):
    nom, egasi, guruh, rejim = satrlar[i]
    i += 1
    fayllar[nom] = (egasi, guruh, int(rejim, 8))
guruhlari = {}
u = int(satrlar[i][0])
i += 1
for _ in range(u):
    kim, gs = satrlar[i]
    i += 1
    guruhlari[kim] = gs.split(",")
for kim, amal, nom in satrlar[i:]:
    egasi, guruh, rejim = fayllar[nom]
    if kim == "root":
        ok = amal != "x" or rejim & 0o111 != 0        # для root x требует хотя бы одного бита x
        sinf = "root"
    elif kim == egasi:                                # только тройка владельца
        ok = (rejim >> 6) & BIT[amal] != 0
        sinf = "владелец"
    elif guruh in guruhlari[kim]:
        ok = (rejim >> 3) & BIT[amal] != 0
        sinf = "группа"
    else:
        ok = rejim & BIT[amal] != 0
        sinf = "остальные"
    print(f"{kim} {amal} {nom}: " + ("разрешено" if ok else "запрещено") + f" ({sinf})")
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String[]> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s.split("\\s+"));
        }
        Map<String, Integer> BIT = Map.of("r", 4, "w", 2, "x", 1);
        int i = 0;
        Map<String, String[]> fayllar = new HashMap<>();
        int m = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < m; j++) {
            String[] q = satrlar.get(i++);
            fayllar.put(q[0], q);
        }
        Map<String, List<String>> guruhlari = new HashMap<>();
        int u = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < u; j++) {
            String[] q = satrlar.get(i++);
            guruhlari.put(q[0], Arrays.asList(q[1].split(",")));
        }
        for (; i < satrlar.size(); i++) {
            String kim = satrlar.get(i)[0], amal = satrlar.get(i)[1], nom = satrlar.get(i)[2];
            String[] f = fayllar.get(nom);
            int rejim = Integer.parseInt(f[3], 8);
            int bit = BIT.get(amal);
            boolean ok;
            String sinf;
            if (kim.equals("root")) {
                ok = !amal.equals("x") || (rejim & 0111) != 0;    // для root x требует хотя бы одного бита x
                sinf = "root";
            } else if (kim.equals(f[1])) {                       // только тройка владельца
                ok = ((rejim >> 6) & bit) != 0;
                sinf = "владелец";
            } else if (guruhlari.get(kim).contains(f[2])) {
                ok = ((rejim >> 3) & bit) != 0;
                sinf = "группа";
            } else {
                ok = (rejim & bit) != 0;
                sinf = "остальные";
            }
            System.out.println(kim + " " + amal + " " + nom + ": " + (ok ? "разрешено" : "запрещено") + " (" + sinf + ")");
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<vector<string>> satrlar;
    string s;
    while (getline(cin, s)) {
        istringstream ss(s);
        vector<string> q;
        string w;
        while (ss >> w) q.push_back(w);
        if (!q.empty()) satrlar.push_back(q);
    }
    map<string, int> BIT = {{"r", 4}, {"w", 2}, {"x", 1}};
    size_t i = 0;
    map<string, vector<string>> fayllar;
    int m = stoi(satrlar[i++][0]);
    for (int j = 0; j < m; j++, i++) fayllar[satrlar[i][0]] = satrlar[i];
    map<string, vector<string>> guruhlari;
    int u = stoi(satrlar[i++][0]);
    for (int j = 0; j < u; j++, i++) {
        stringstream gs(satrlar[i][1]);
        string g;
        while (getline(gs, g, ',')) guruhlari[satrlar[i][0]].push_back(g);
    }
    for (; i < satrlar.size(); i++) {
        const string& kim = satrlar[i][0];
        const string& amal = satrlar[i][1];
        const string& nom = satrlar[i][2];
        const vector<string>& f = fayllar[nom];
        int rejim = stoi(f[3], nullptr, 8);
        int bit = BIT[amal];
        vector<string>& gl = guruhlari[kim];
        bool ok;
        string sinf;
        if (kim == "root") {
            ok = amal != "x" || (rejim & 0111) != 0;             // для root x требует хотя бы одного бита x
            sinf = "root";
        } else if (kim == f[1]) {                                // только тройка владельца
            ok = ((rejim >> 6) & bit) != 0;
            sinf = "владелец";
        } else if (find(gl.begin(), gl.end(), f[2]) != gl.end()) {
            ok = ((rejim >> 3) & bit) != 0;
            sinf = "группа";
        } else {
            ok = (rejim & bit) != 0;
            sinf = "остальные";
        }
        cout << kim << " " << amal << " " << nom << ": " << (ok ? "разрешено" : "запрещено") << " (" << sinf << ")" << endl;
    }
    return 0;
}
```

Ввод:

```text
3
hisobot.txt ali dev 640
skript.sh vali dev 750
g.txt ali dev 074
4
ali dev
vali dev,docker
olim users
root root
vali r hisobot.txt
vali w hisobot.txt
olim r hisobot.txt
vali x skript.sh
olim x skript.sh
ali r g.txt
vali r g.txt
olim r g.txt
root w hisobot.txt
root x hisobot.txt
```

Вывод:

```text
vali r hisobot.txt: разрешено (группа)
vali w hisobot.txt: запрещено (группа)
olim r hisobot.txt: запрещено (остальные)
vali x skript.sh: разрешено (владелец)
olim x skript.sh: запрещено (остальные)
ali r g.txt: запрещено (владелец)
vali r g.txt: разрешено (группа)
olim r g.txt: разрешено (остальные)
root w hisobot.txt: разрешено (root)
root x hisobot.txt: запрещено (root)
```

Самый интересный случай — `g.txt` (074): ali — его **владелец** и к тому же в группе dev, но не может прочитать файл, потому что тройка владельца — `---`, а тройки группы и остальных к нему не применяются. При этом посторонний olim может читать как «остальные». Такие «перевёрнутые» права на практике редки, но хорошо показывают порядок проверки. Даже root не может выполнить файл, у которого вообще нет бита `x`, — это защищает от случайного запуска файла данных.

:::warning
**Частые ошибки**

- **«Не работает — chmod 777».** Это скрывает проблему и даёт всем право записи; выдавайте ровно нужное право.
- **Широкие права на SSH-ключи.** `~/.ssh` — 700, закрытый ключ — 600; иначе SSH откажется использовать ключ.
- **Файл защищён, а каталог открыт.** Любой с правом `w` на каталог может удалять и подменять файлы.
- **Бездумный `chmod -R`.** Каталогам и файлам нужны разные права (`x` — каталогам, а не файлам); задавайте их отдельно через `find -type d` / `-type f`.
- **Постоянная работа под root.** Любая ошибка или вредоносный скрипт затрагивает всю систему; используйте `sudo`.
:::

:::tip
`namei -l /var/www/html/index.html` показывает права каждого каталога на пути — самый быстрый способ найти причину «Permission denied». `stat -c '%a %A %U %G %n' *` выводит права файлов сразу в восьмеричном и символьном виде.
:::

## Итоги

- У каждого файла есть владелец, группа и три тройки (владелец, группа, остальные); `r`, `w`, `x` означают разное для файлов и каталогов.
- `chmod` (символьная и числовая запись), `chown`, `chgrp`; права новых файлов — 666/777 AND NOT umask.
- Специальные биты: setuid (4000), setgid (2000), sticky (1000); регулярно проверяйте setuid-программы.
- Ядро применяет только одну тройку: владелец → группа → остальные.
- ACL (`setfacl`, `getfacl`) — для тонких прав; `sudo` — для минимальных привилегий.

## Проверьте себя

:::check
Q: Какое право нужно, чтобы удалить файл в каталоге?
A: Право w (и x) на каталог; собственные права файла не важны (если нет sticky-бита).
Q: Какие права у нового файла и каталога при umask 027?
A: Файл 640, каталог 750.
Q: Если тройка владельца ---, а тройка группы rwx, может ли владелец, состоящий в группе, прочитать файл?
A: Нет, к владельцу применяется только тройка владельца.
Q: Что даёт sticky-бит на каталоге /tmp?
A: Создавать файлы может любой, но удалить файл может только его владелец (или root).
Q: Чем опасна незнакомая setuid-root-программа?
A: Она работает с правами root; уязвимость в ней позволяет обычному пользователю стать root.
:::

:::link https://man7.org/linux/man-pages/man1/chmod.1.html
chmod(1) — Linux manual page
Команда chmod, символьные и числовые режимы, специальные биты (на английском языке).
:::
