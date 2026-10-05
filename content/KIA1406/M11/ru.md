---
title: Модели управления доступом. DAC, MAC, RBAC, ABAC, ACL и матрицы
summary: Понятия управления доступом — субъект, объект, операция; принципы — минимальные привилегии, необходимость знать, разделение обязанностей, запрет по умолчанию; матрица доступа, ACL и списки возможностей; дискреционная модель (DAC) — права Unix и NTFS, проблема троянского коня; мандатная модель (MAC) — метки безопасности, модели Белла–Лападулы и Бибы, SELinux; ролевая модель (RBAC) — иерархия ролей и ограничения; атрибутная модель (ABAC) — политики, XACML; сравнение моделей.
---

## Почему эта тема важна?

В M10 мы увидели, как пользователь доказывает, **кто он**. Следующий вопрос: что **разрешено** аутентифицированному пользователю? Студент должен видеть свою оценку, но не менять её; бухгалтер — готовить платёж, но не утверждать его сам; секретный документ должен быть открыт только сотрудникам соответствующего уровня. В списке OWASP Top 10 **Broken Access Control** стоит на первом месте как самая опасная уязвимость веб-приложений.

:::note
**После этого урока вы сможете:**

- назвать основные принципы управления доступом;
- построить матрицу доступа, ACL и списки возможностей;
- различать и сравнивать модели DAC, MAC, RBAC и ABAC;
- применять правила Белла–Лападулы и Бибы;
- показать логику проверки запроса в RBAC в программе.
:::

## Основные понятия и принципы

**Субъект** (пользователь, процесс) запрашивает выполнение **операции** (чтение, запись, выполнение, удаление) над **объектом** (файл, таблица, принтер); **механизм управления доступом** (reference monitor) проверяет каждый запрос по политике. Монитор обращений должен вызываться всегда, быть неотключаемым и достаточно малым для проверки.

| Принцип | Смысл |
|---|---|
| **Минимальные привилегии** (least privilege) | только права, нужные для задачи, и только на нужное время |
| **Необходимость знать** (need to know) | только относящаяся к работе информация, даже при достаточном уровне допуска |
| **Разделение обязанностей** (separation of duties) | важная операция делится на двоих: один готовит, другой утверждает |
| **Запрет по умолчанию** (default deny) | всё, что явно не разрешено, запрещено |
| **Полное посредничество** (complete mediation) | проверяется каждое обращение, а не только первое |

## Матрица доступа, ACL и возможности

**Матрица доступа** (Лэмпсон, 1971): строки — субъекты, столбцы — объекты, в ячейке — разрешённые операции.

| | report.docx | base.db | принтер |
|---|---|---|---|
| **Ali** | чтение, запись, владелец | чтение | печать |
| **Vali** | чтение | — | печать |
| **процесс резервного копирования** | чтение | чтение | — |

На практике матрица огромна и разрежена, поэтому её хранят одним из двух способов:

- **ACL** (Access Control List) — по **столбцу**: к каждому объекту прикрепляется список «кто что может». Файловые системы (NTFS, Unix), сетевые устройства. Удобно: «кто имеет доступ к этому файлу?»; неудобно: «к чему имеет доступ Ali?».
- **Список возможностей** (capability list) — по **строке**: у каждого субъекта есть метки (токены) «какое право на какой объект». Примеры: билеты Kerberos, токены OAuth, открытые файловые дескрипторы. Передать право легко, а отозвать — трудно.

## DAC — дискреционная модель

В **DAC** (Discretionary Access Control) **владелец** объекта сам решает, кому какие права дать. Примеры: права Unix (`rwx` для владельца, группы и остальных — в A11), ACL в Windows NTFS, «совместный доступ» в Google Drive.

- Достоинство: гибкость, простота, удобство для пользователей.
- Недостаток: нет централизованного контроля; ошибка или **троянская программа** действует от имени пользователя со всеми его правами — например, копирует секретный файл в общую папку, и DAC этому не мешает.

## MAC — мандатная модель

В **MAC** (Mandatory Access Control) решение принимает не владелец, а **политика системы**: субъектам присваивается **уровень допуска** (clearance), объектам — **гриф секретности** (classification), например: Открыто (U) < Для служебного пользования (C) < Секретно (S) < Совершенно секретно (TS). Пользователь не может изменить гриф даже своего документа.

**Белл–Лападула** (BLP, 1973) — модель защиты **конфиденциальности**:

- **Simple security property** — «нет чтения вверх» (no read up): субъект читает только объекты своего уровня или ниже.
- **\*-свойство** — «нет записи вниз» (no write down): субъект пишет только в объекты своего уровня или выше — это останавливает «стекание» секретной информации на низкий уровень (в том числе через троян).

**Биба** (1977) — модель защиты **целостности**, правила обратные: «нет чтения вниз» (не загрязняться недоверенными данными) и «нет записи вверх» (низкоуровневый процесс не должен портить важные данные). **Mandatory Integrity Control** в Windows (браузер работает на «низком» уровне целостности) — идея Бибы.

MAC применяется в военных и государственных системах, а также в **SELinux** и **AppArmor** (Linux, Android): даже взломанный процесс с правами `root` не может выйти за пределы политики.

## RBAC — ролевая модель

В **RBAC** (Role-Based Access Control, стандарт NIST) разрешения выдаются не пользователям, а **ролям**; пользователи назначаются на роли: пользователь → роль → разрешение. При смене должности сотрудника достаточно сменить его роль.

- **Иерархия ролей**: роль «старший преподаватель» наследует все разрешения роли «преподаватель».
- **Ограничения**: статическое разделение обязанностей (один человек не может одновременно иметь роли «подготовитель платежа» и «утверждающий платёж»), динамическое (нельзя активировать обе в одном сеансе).
- Применение: корпоративные системы, базы данных (`GRANT ... TO role`), облака (роли AWS IAM), группы Active Directory, Kubernetes RBAC.
- Недостаток: «взрыв ролей» — для каждого исключения создаётся новая роль, и ролей становится больше, чем пользователей.

## ABAC — атрибутная модель

В **ABAC** (Attribute-Based Access Control) решение принимается правилами по **атрибутам**: субъекта (должность, отдел, допуск), объекта (тип, владелец, гриф), операции и **среды** (время, место, устройство, уровень риска). Например: «врач **может читать** карту пациента, если пациент в том же отделении, **и** врач сейчас на смене, **и** подключение идёт из сети больницы». Политики пишутся на XACML или на языках политик облаков (условия AWS IAM). ABAC — самая гибкая модель, но её политики труднее всего проектировать и проверять. Архитектура **нулевого доверия** (Zero Trust) построена именно на переоценке каждого запроса по контексту.

| Модель | Кто решает | Сильная сторона | Слабая сторона | Где |
|---|---|---|---|---|
| DAC | владелец объекта | простая, гибкая | нет центрального контроля, трояны | личные компьютеры, обмен файлами |
| MAC | политика системы (метки) | строгая, останавливает утечки | жёсткая, сложна в управлении | военные, госсистемы, SELinux |
| RBAC | администратор (роли) | соответствует структуре организации, лёгкий аудит | взрыв ролей | предприятия, СУБД, облака |
| ABAC | политика по атрибутам | контекстная, тонкий контроль | сложная, медленнее | облака, системы нулевого доверия |

## Разобранный пример 1: модель Белла–Лападулы

Уровни: U < C < S < TS. Ввод: *n* и *n* субъектов (имя уровень), *m* и *m* объектов (имя уровень), затем запросы (субъект операция объект, операция — `read` или `write`). Для каждого запроса выведите решение по правилам BLP.

```python
import sys

DARAJA = {"U": 0, "C": 1, "S": 2, "TS": 3}

t = sys.stdin.read().split()
i = 0
subyekt, obyekt = {}, {}
for jadval in (subyekt, obyekt):
    k = int(t[i])
    i += 1
    for _ in range(k):
        jadval[t[i]] = DARAJA[t[i + 1]]
        i += 2
while i + 2 < len(t):
    s, amal, o = t[i], t[i + 1], t[i + 2]
    i += 3
    if amal == "read":
        ruxsat, sabab = subyekt[s] >= obyekt[o], "no read up"      # простое свойство безопасности
    else:
        ruxsat, sabab = subyekt[s] <= obyekt[o], "no write down"   # *-свойство
    print(f"{s} {amal} {o}: " + ("разрешено" if ruxsat else f"запрещено ({sabab})"))
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final Map<String, Integer> DARAJA = Map.of("U", 0, "C", 1, "S", 2, "TS", 3);

    static Map<String, Integer> oqi(Scanner in) {
        Map<String, Integer> jadval = new HashMap<>();
        int k = in.nextInt();
        for (int j = 0; j < k; j++) {
            String nom = in.next();
            jadval.put(nom, DARAJA.get(in.next()));
        }
        return jadval;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Integer> subyekt = oqi(in);
        Map<String, Integer> obyekt = oqi(in);
        while (in.hasNext()) {
            String s = in.next(), amal = in.next(), o = in.next();
            boolean ruxsat;
            String sabab;
            if (amal.equals("read")) {
                ruxsat = subyekt.get(s) >= obyekt.get(o);                // простое свойство безопасности
                sabab = "no read up";
            } else {
                ruxsat = subyekt.get(s) <= obyekt.get(o);                // *-свойство
                sabab = "no write down";
            }
            System.out.println(s + " " + amal + " " + o + ": " + (ruxsat ? "разрешено" : "запрещено (" + sabab + ")"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

map<string, int> DARAJA = {{"U", 0}, {"C", 1}, {"S", 2}, {"TS", 3}};

map<string, int> oqi() {
    map<string, int> jadval;
    int k;
    cin >> k;
    for (int j = 0; j < k; j++) {
        string nom, d;
        cin >> nom >> d;
        jadval[nom] = DARAJA[d];
    }
    return jadval;
}

int main() {
    map<string, int> subyekt = oqi();
    map<string, int> obyekt = oqi();
    string s, amal, o;
    while (cin >> s >> amal >> o) {
        bool ruxsat;
        string sabab;
        if (amal == "read") {
            ruxsat = subyekt[s] >= obyekt[o];                            // простое свойство безопасности
            sabab = "no read up";
        } else {
            ruxsat = subyekt[s] <= obyekt[o];                            // *-свойство
            sabab = "no write down";
        }
        cout << s << " " << amal << " " << o << ": " << (ruxsat ? string("разрешено") : "запрещено (" + sabab + ")") << endl;
    }
    return 0;
}
```

Ввод:

```text
3
ali S
vali C
olim TS
4
elon U
hisobot C
reja S
kod TS
vali read reja
ali read hisobot
ali write hisobot
vali write reja
olim read kod
olim write elon
```

Вывод:

```text
vali read reja: запрещено (no read up)
ali read hisobot: разрешено
ali write hisobot: запрещено (no write down)
vali write reja: разрешено
olim read kod: разрешено
olim write elon: запрещено (no write down)
```

Обратите внимание: ali с уровнем «Секретно» может читать отчёт (hisobot) уровня «ДСП», но **не может писать** в него — иначе он (или троян от его имени) мог бы перенести секретную информацию на низкий уровень. vali, наоборот, может писать в «секретный» план (reja) («слепая запись»), но не может его читать. На практике пользователь высокого уровня, чтобы написать документ низкого уровня, входит в сеанс низкого уровня.

## Разобранный пример 2: проверка запроса RBAC

Ввод: *r* и *r* строк «роль разрешение1 разрешение2 …»; *u* и *u* строк «пользователь роль1 роль2 …»; затем запросы «пользователь разрешение». Просматривайте роли пользователя в заданном порядке и выведите первую роль, дающую разрешение; иначе — запрет (запрет по умолчанию).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
i = 0
rollar = {}
r = int(satrlar[i][0])
i += 1
for _ in range(r):
    rollar[satrlar[i][0]] = set(satrlar[i][1:])     # роль -> разрешения
    i += 1
foydalanuvchilar = {}
u = int(satrlar[i][0])
i += 1
for _ in range(u):
    foydalanuvchilar[satrlar[i][0]] = satrlar[i][1:]  # пользователь -> роли
    i += 1
for kim, nima in satrlar[i:]:
    if kim not in foydalanuvchilar:
        print(f"{kim} {nima}: запрещено (пользователь не найден)")
        continue
    rol = next((x for x in foydalanuvchilar[kim] if nima in rollar.get(x, set())), None)
    print(f"{kim} {nima}: " + (f"разрешено ({rol})" if rol else "запрещено"))   # запрет по умолчанию
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
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String[]> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s.split("\\s+"));
        }
        int i = 0;
        Map<String, Set<String>> rollar = new HashMap<>();
        int r = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < r; j++, i++) {
            String[] q = satrlar.get(i);
            rollar.put(q[0], new HashSet<>(Arrays.asList(q).subList(1, q.length)));   // роль -> разрешения
        }
        Map<String, List<String>> foydalanuvchilar = new HashMap<>();
        int u = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < u; j++, i++) {
            String[] q = satrlar.get(i);
            foydalanuvchilar.put(q[0], Arrays.asList(q).subList(1, q.length));       // пользователь -> роли
        }
        for (; i < satrlar.size(); i++) {
            String kim = satrlar.get(i)[0], nima = satrlar.get(i)[1];
            if (!foydalanuvchilar.containsKey(kim)) {
                System.out.println(kim + " " + nima + ": запрещено (пользователь не найден)");
                continue;
            }
            String topildi = null;
            for (String rol : foydalanuvchilar.get(kim)) {
                if (rollar.getOrDefault(rol, Set.of()).contains(nima)) {
                    topildi = rol;
                    break;
                }
            }
            System.out.println(kim + " " + nima + ": " + (topildi != null ? "разрешено (" + topildi + ")" : "запрещено"));   // запрет по умолчанию
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
    vector<vector<string>> satrlar;
    string s;
    while (getline(cin, s)) {
        istringstream ss(s);
        vector<string> q;
        string w;
        while (ss >> w) q.push_back(w);
        if (!q.empty()) satrlar.push_back(q);
    }
    size_t i = 0;
    map<string, set<string>> rollar;
    int r = stoi(satrlar[i++][0]);
    for (int j = 0; j < r; j++, i++) {
        rollar[satrlar[i][0]] = set<string>(satrlar[i].begin() + 1, satrlar[i].end());   // роль -> разрешения
    }
    map<string, vector<string>> foydalanuvchilar;
    int u = stoi(satrlar[i++][0]);
    for (int j = 0; j < u; j++, i++) {
        foydalanuvchilar[satrlar[i][0]] = vector<string>(satrlar[i].begin() + 1, satrlar[i].end());   // пользователь -> роли
    }
    for (; i < satrlar.size(); i++) {
        const string& kim = satrlar[i][0];
        const string& nima = satrlar[i][1];
        if (!foydalanuvchilar.count(kim)) {
            cout << kim << " " << nima << ": запрещено (пользователь не найден)" << endl;
            continue;
        }
        string topildi;
        for (const string& rol : foydalanuvchilar[kim]) {
            if (rollar.count(rol) && rollar[rol].count(nima)) {
                topildi = rol;
                break;
            }
        }
        cout << kim << " " << nima << ": " << (!topildi.empty() ? "разрешено (" + topildi + ")" : string("запрещено")) << endl;   // запрет по умолчанию
    }
    return 0;
}
```

Ввод:

```text
4
talaba baho.korish jadval.korish
oqituvchi baho.korish baho.qoyish jadval.korish
dekan hisobot.korish baho.korish
admin foydalanuvchi.yaratish
4
ali talaba
vali oqituvchi
sardor oqituvchi dekan
olim admin
ali baho.qoyish
vali baho.qoyish
sardor hisobot.korish
olim baho.korish
ali jadval.korish
kamol baho.korish
```

Вывод:

```text
ali baho.qoyish: запрещено
vali baho.qoyish: разрешено (oqituvchi)
sardor hisobot.korish: разрешено (dekan)
olim baho.korish: запрещено
ali jadval.korish: разрешено (talaba)
kamol baho.korish: запрещено (пользователь не найден)
```

(Роли — студент, преподаватель, декан и администратор; разрешения — просмотр оценок, выставление оценок, просмотр расписания, просмотр отчётов и создание пользователей.) Системный администратор olim может создавать пользователей, но не видит оценки — это **разделение обязанностей** и **необходимость знать**: техническое право — не право доступа к данным. У sardor две роли, и он пользуется разрешениями обеих. Любой запрос, которого нет в списке, отклоняется — **запрет по умолчанию**. В реальных системах такая проверка выполняется **на сервере** при каждом API-запросе; просто скрыть кнопку (во фронтенде) — не защита.

:::warning
**Частые ошибки**

- **Проверка только в интерфейсе.** Даже если кнопка скрыта, запрос можно отправить напрямую; проверка обязана быть на сервере.
- **Права администратора всем.** Лишние права, выданные «чтобы ничего не сломалось», умножают ущерб от взлома.
- **Накопление привилегий** (privilege creep). При переходе сотрудника между отделами старые права остаются; регулярно пересматривайте права.
- **Учётная запись уволенного сотрудника.** Её нужно немедленно удалить или заблокировать.
- **Не проверять идентификатор объекта** (IDOR). Сменить `/grade?id=125` на `id=126` и получить чужие данные — самая частая веб-уязвимость.
:::

:::tip
В Windows посмотрите NTFS ACL файла: Properties → вкладка Security; в командной строке — `icacls file.txt`. В Linux `getfacl file` показывает расширенный ACL, а `ls -Z` — метку SELinux.
:::

## Итоги

- Управление доступом: субъект, объект, операция; принципы — минимальные привилегии, необходимость знать, разделение обязанностей, запрет по умолчанию.
- Матрица доступа хранится по столбцам — как ACL, по строкам — как списки возможностей.
- DAC — решает владелец; MAC — политика системы и метки (BLP: no read up, no write down; Биба — наоборот, для целостности).
- RBAC — разрешения через роли, с иерархией и ограничениями; ABAC — политика по атрибутам и среде.
- Проверка выполняется при каждом запросе, на сервере, с запретом по умолчанию.

## Проверьте себя

:::check
Q: Чем ACL отличается от списка возможностей?
A: ACL прикрепляется к объекту (кто что может), список возможностей — к субъекту (какое право на какой объект).
Q: Может ли в модели Белла–Лападулы пользователь уровня «Секретно» писать в «открытый» документ?
A: Нет, *-свойство (no write down) запрещает запись вниз.
Q: В чём слабость DAC по отношению к троянской программе?
A: Троян действует от имени пользователя со всеми его правами и может скопировать данные куда угодно.
Q: Что запрещает ограничение разделения обязанностей в RBAC?
A: Одновременное обладание одним пользователем конфликтующими ролями (например, подготовитель и утверждающий платежа).
Q: Какие атрибуты участвуют в решении ABAC?
A: Атрибуты субъекта, объекта, операции и среды (время, место, устройство).
:::

:::link https://csrc.nist.gov/pubs/sp/800/162/upd2/final
NIST SP 800-162: Guide to Attribute Based Access Control (ABAC)
Понятия ABAC и сравнение с другими моделями (на английском языке).
:::
