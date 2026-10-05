---
title: Настройка аутентификации пользователей и парольной политики в Windows
summary: Практическая настройка парольной политики и политики блокировки учётных записей в Windows — локальная политика безопасности (secpol.msc), команда net accounts, групповая политика (gpedit.msc, GPO); параметры парольной политики — журнал паролей, максимальный и минимальный срок, минимальная длина, сложность; параметры блокировки — порог, продолжительность, сброс счётчика; журнал событий (4624, 4625, 4740); Windows Hello и LAPS; моделирование логики блокировки и журнала паролей в программе; программное задание — оценка стойкости пароля.
---

## Почему эта тема важна?

В M10 мы рассмотрели теорию паролей и аутентификации. В организации эти правила не оставляют на усмотрение каждого пользователя — они обязательно задаются централизованно в виде **политики**. Windows — самая распространённая настольная система в организациях, поэтому умение настраивать её парольную политику и политику блокировки — базовый навык каждого системного администратора и специалиста по безопасности.

:::note
**После этого занятия вы сможете:**

- настраивать параметры парольной политики и политики блокировки учётных записей Windows;
- пользоваться `net accounts`, `secpol.msc` и групповой политикой;
- находить неудачные входы и блокировки в журнале событий;
- показать в программе, как работают блокировка и журнал паролей;
- написать программу оценки стойкости пароля (задание A10 на платформе).
:::

:::warning
Выполняйте практическую работу в **виртуальной машине** или на учебном компьютере. Перед изменением настроек безопасности на рабочем или личном компьютере запишите текущие значения; неправильная политика блокировки может заблокировать и вас самих.
:::

## Параметры парольной политики

`secpol.msc` → Account Policies → **Password Policy** (в редакции Windows Home этого окна нет — используйте команду `net accounts`):

| Параметр | Смысл | Рекомендация (CIS / базовый уровень Microsoft) |
|---|---|---|
| Enforce password history | сколько последних паролей нельзя использовать повторно | 24 |
| Maximum password age | через сколько дней пароль истекает | 365 или без ограничения (NIST: менять только при признаках компрометации) |
| Minimum password age | через какое время пароль можно снова сменить | 1 день — не даёт «прокрутить» журнал паролей |
| Minimum password length | минимальная длина | 14 |
| Password must meet complexity requirements | 3 типа символов, без имени пользователя | включено (старое требование; длина важнее) |
| Store passwords using reversible encryption | хранить пароли в восстановимом виде | **отключено** — иначе пароль фактически хранится открыто |

**Account Lockout Policy**:

| Параметр | Смысл | Рекомендация |
|---|---|---|
| Account lockout threshold | после скольких неудачных попыток учётная запись блокируется | 5–10 (в Windows 11 по умолчанию — 10) |
| Account lockout duration | сколько минут длится блокировка | 15 |
| Reset account lockout counter after | через сколько минут счётчик ошибок обнуляется | 15 |

Блокировка замедляет полный перебор, но слишком низкий порог (1–2) создаёт риск **отказа в обслуживании**: злоумышленник может намеренно вводить неверные пароли и заблокировать всех сотрудников.

## Практическая работа

**Шаг 1. Просмотр текущей политики.** В командной строке с правами администратора:

```text
net accounts
net user
net user student1
```

`net accounts` выводит минимальную длину, сроки, журнал и значения блокировки; `net user student1` — когда учётная запись меняла пароль и когда он истечёт.

**Шаг 2. Тестовый пользователь и политика.** Создайте тестовую учётную запись и задайте политику:

```text
net user student1 * /add
net accounts /minpwlen:12 /uniquepw:24 /minpwage:1 /maxpwage:unlimited
net accounts /lockoutthreshold:5 /lockoutduration:15 /lockoutwindow:15
```

То же самое выполните в графическом виде через `secpol.msc` (или в доменной сети — GPO в `gpmc.msc`: Computer Configuration → Windows Settings → Security Settings → Account Policies). Чтобы применить доменную политику — `gpupdate /force`, чтобы проверить результат — `gpresult /r`.

**Шаг 3. Проверка политики.** Попробуйте задать `student1` короткий пароль (например, `abc123`) — система откажет. Затем попытайтесь 5 раз войти как `student1` с неверным паролем — учётная запись будет заблокирована.

**Шаг 4. Журнал событий.** `eventvwr.msc` → Windows Logs → **Security**; найдите с помощью фильтра следующие события:

| Event ID | Событие |
|---|---|
| 4624 | успешный вход |
| 4625 | неудачная попытка входа |
| 4740 | учётная запись заблокирована |
| 4723 / 4724 | пользователь сменил пароль / администратор сбросил пароль |

В PowerShell: `Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 10`. Множество событий 4625 для многих учётных записей с одного адреса — признак распыления паролей.

**Шаг 5. Современные средства.** **Windows Hello** (PIN или биометрия) — ключ, привязанный к устройству и хранящийся в TPM; PIN не передаётся по сети. **LAPS** (Local Administrator Password Solution) делает пароль локального администратора каждого компьютера случайным и уникальным — это не позволяет захватить всю сеть одним общим паролем.

**Шаг 6. Программа.** Выполните задание **Парольная политика: оценка стойкости пароля** на платформе.

## Разобранный пример 1: политика блокировки учётных записей

В первой строке — политика: порог блокировки, окно наблюдения (минуты) и продолжительность блокировки (минуты). В следующих строках — попытки входа: время (HH:MM), пользователь, результат (`OK` или `FAIL`). Любая попытка входа в заблокированную учётную запись отклоняется и не учитывается; ошибки старше окна забываются; успешный вход обнуляет счётчик.

```python
import sys


def daqiqa(v):
    s, d = v.split(":")
    return int(s) * 60 + int(d)


def vaqt(m):
    return f"{m // 60:02d}:{m % 60:02d}"


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
chegara, oyna, muddat = map(int, satrlar[0])
xatolar, blok = {}, {}                          # пользователь -> время ошибок / окончание блокировки
for v, kim, natija in satrlar[1:]:
    t = daqiqa(v)
    if kim in blok and t < blok[kim]:
        print(f"{v} {kim}: отказ - заблокирован")
        continue
    if natija == "OK":
        xatolar[kim] = []                       # успешный вход сбрасывает счётчик
        print(f"{v} {kim}: вход выполнен")
        continue
    royxat = [x for x in xatolar.get(kim, []) if t - x < oyna] + [t]   # ошибки внутри окна
    if len(royxat) >= chegara:
        blok[kim] = t + muddat
        xatolar[kim] = []
        print(f"{v} {kim}: ЗАБЛОКИРОВАН (до {vaqt(t + muddat)})")
    else:
        xatolar[kim] = royxat
        print(f"{v} {kim}: ошибка ({len(royxat)}/{chegara})")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static int daqiqa(String v) {
        String[] q = v.split(":");
        return Integer.parseInt(q[0]) * 60 + Integer.parseInt(q[1]);
    }

    static String vaqt(int m) {
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int chegara = in.nextInt(), oyna = in.nextInt(), muddat = in.nextInt();
        Map<String, List<Integer>> xatolar = new HashMap<>();   // пользователь -> время ошибок / окончание блокировки
        Map<String, Integer> blok = new HashMap<>();
        while (in.hasNext()) {
            String v = in.next(), kim = in.next(), natija = in.next();
            int t = daqiqa(v);
            if (blok.containsKey(kim) && t < blok.get(kim)) {
                System.out.println(v + " " + kim + ": отказ - заблокирован");
                continue;
            }
            if (natija.equals("OK")) {
                xatolar.put(kim, new ArrayList<>());            // успешный вход сбрасывает счётчик
                System.out.println(v + " " + kim + ": вход выполнен");
                continue;
            }
            List<Integer> royxat = new ArrayList<>();
            for (int x : xatolar.getOrDefault(kim, new ArrayList<>())) {
                if (t - x < oyna) royxat.add(x);                 // ошибки внутри окна
            }
            royxat.add(t);
            if (royxat.size() >= chegara) {
                blok.put(kim, t + muddat);
                xatolar.put(kim, new ArrayList<>());
                System.out.println(v + " " + kim + ": ЗАБЛОКИРОВАН (до " + vaqt(t + muddat) + ")");
            } else {
                xatolar.put(kim, royxat);
                System.out.println(v + " " + kim + ": ошибка (" + royxat.size() + "/" + chegara + ")");
            }
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int daqiqa(const string& v) {
    return stoi(v.substr(0, 2)) * 60 + stoi(v.substr(3, 2));
}

string vaqt(int m) {
    char buf[8];
    snprintf(buf, sizeof buf, "%02d:%02d", m / 60, m % 60);
    return buf;
}

int main() {
    int chegara, oyna, muddat;
    cin >> chegara >> oyna >> muddat;
    map<string, vector<int>> xatolar;                            // пользователь -> время ошибок / окончание блокировки
    map<string, int> blok;
    string v, kim, natija;
    while (cin >> v >> kim >> natija) {
        int t = daqiqa(v);
        if (blok.count(kim) && t < blok[kim]) {
            cout << v << " " << kim << ": отказ - заблокирован" << endl;
            continue;
        }
        if (natija == "OK") {
            xatolar[kim].clear();                                // успешный вход сбрасывает счётчик
            cout << v << " " << kim << ": вход выполнен" << endl;
            continue;
        }
        vector<int> royxat;
        for (int x : xatolar[kim]) {
            if (t - x < oyna) royxat.push_back(x);               // ошибки внутри окна
        }
        royxat.push_back(t);
        if ((int) royxat.size() >= chegara) {
            blok[kim] = t + muddat;
            xatolar[kim].clear();
            cout << v << " " << kim << ": ЗАБЛОКИРОВАН (до " << vaqt(t + muddat) << ")" << endl;
        } else {
            xatolar[kim] = royxat;
            cout << v << " " << kim << ": ошибка (" << royxat.size() << "/" << chegara << ")" << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
3 15 15
09:00 ali FAIL
09:02 ali FAIL
09:03 vali OK
09:05 ali FAIL
09:10 ali OK
09:21 ali OK
09:30 vali FAIL
09:50 vali FAIL
09:52 vali FAIL
```

Вывод:

```text
09:00 ali: ошибка (1/3)
09:02 ali: ошибка (2/3)
09:03 vali: вход выполнен
09:05 ali: ЗАБЛОКИРОВАН (до 09:20)
09:10 ali: отказ - заблокирован
09:21 ali: вход выполнен
09:30 vali: ошибка (1/3)
09:50 vali: ошибка (1/3)
09:52 vali: ошибка (2/3)
```

ali заблокирован на 15 минут после третьей ошибки; даже **правильный** пароль в 09:10 не принят — во время блокировки система вообще не проверяет пароль, иначе злоумышленник мог бы продолжать перебор на заблокированной учётной записи. Ошибка vali в 09:30 к 09:50 вышла из 15-минутного окна — это и есть параметр «Reset account lockout counter after».

## Разобранный пример 2: журнал паролей

В первой строке — размер журнала *N*, в следующих — запросы на смену пароля (пользователь новый_пароль). Система хранит не сами пароли, а их хеши (в учебных целях — FNV-1a с добавлением имени пользователя; Windows хранит NT-хеш). Если новый пароль — один из последних *N* паролей пользователя, он отклоняется.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
n = int(satrlar[0][0])
tarix = {}                                       # пользователь -> последние N хешей
for kim, parol in satrlar[1:]:
    h = format(fnv1a(kim + ":" + parol), "08x")  # сам пароль не хранится
    oxirgi = tarix.setdefault(kim, [])
    if h in oxirgi:
        print(f"{kim}: отклонён - один из последних {n} паролей")
    else:
        oxirgi.append(h)
        if len(oxirgi) > n:
            oxirgi.pop(0)                        # самый старый забывается
        print(f"{kim}: принят [{h}]")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {
        int h = 0x811C9DC5;
        for (byte bayt : matn.getBytes(StandardCharsets.UTF_8)) {
            h = (h ^ (bayt & 0xFF)) * 16777619;
        }
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        int n = Integer.parseInt(in.next());
        Map<String, List<String>> tarix = new HashMap<>();        // пользователь -> последние N хешей
        while (in.hasNext()) {
            String kim = in.next(), parol = in.next();
            String h = String.format("%08x", fnv1a(kim + ":" + parol));   // сам пароль не хранится
            List<String> oxirgi = tarix.computeIfAbsent(kim, k -> new ArrayList<>());
            if (oxirgi.contains(h)) {
                System.out.println(kim + ": отклонён - один из последних " + n + " паролей");
            } else {
                oxirgi.add(h);
                if (oxirgi.size() > n) oxirgi.remove(0);             // самый старый забывается
                System.out.println(kim + ": принят [" + h + "]");
            }
        }
    }
}
```
```cpp
#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <deque>
#include <iostream>
#include <map>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

int main() {
    int n;
    cin >> n;
    map<string, deque<string>> tarix;                            // пользователь -> последние N хешей
    string kim, parol;
    while (cin >> kim >> parol) {
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", fnv1a(kim + ":" + parol));   // сам пароль не хранится
        string h = buf;
        deque<string>& oxirgi = tarix[kim];
        if (find(oxirgi.begin(), oxirgi.end(), h) != oxirgi.end()) {
            cout << kim << ": отклонён - один из последних " << n << " паролей" << endl;
        } else {
            oxirgi.push_back(h);
            if ((int) oxirgi.size() > n) oxirgi.pop_front();      // самый старый забывается
            cout << kim << ": принят [" << h << "]" << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
3
ali Qwerty!1
ali Bahor2025#
ali Qwerty!1
vali Qwerty!1
ali Yoz2025#
ali Kuz2025#
ali Qwerty!1
```

Вывод:

```text
ali: принят [9c7e87c1]
ali: принят [c24f5411]
ali: отклонён - один из последних 3 паролей
vali: принят [d9787e95]
ali: принят [f4d41845]
ali: принят [32f0b1c1]
ali: принят [9c7e87c1]
```

vali тоже выбрал `Qwerty!1`, но хеш другой — имя пользователя сыграло роль «соли». В последнем запросе ali вернулся к старому паролю: после трёх смен он вышел из журнала. Если «Minimum password age» = 0, пользователь может сменить пароль 24 раза за минуту и вернуться к любимому старому паролю — поэтому минимальный срок ставят 1 день.

:::warning
**Частые ошибки**

- **Порог блокировки 1–3.** Злоумышленник может намеренно блокировать сотрудников; 5–10 с временной блокировкой — более сбалансированно.
- **«Minimum password age» = 0 вместе с журналом паролей.** Журнал фактически не работает — пользователи его обходят.
- **Включение reversible encryption.** Пароли хранятся в восстановимом виде — почти то же, что хранить их открыто.
- **Одинаковый пароль локального администратора на всех компьютерах.** Взлом одного компьютера — взлом всех; используйте LAPS.
- **Не смотреть журналы.** Резкий рост событий 4625 и 4740 — признак атаки.
:::

:::tip
Сохраните вывод `net accounts` в файл (`net accounts > policy_before.txt`) и сравните его после изменений. Если в домене разным группам нужны разные требования (например, 20 символов для администраторов), используйте **Fine-Grained Password Policy** в Active Directory.
:::

## Итоги

- Парольная политика: журнал (24), минимальный срок (1 день), минимальная длина (14), сложность, reversible encryption — отключено.
- Политика блокировки: порог (5–10), продолжительность и сброс счётчика (15 минут); слишком низкий порог — риск отказа в обслуживании.
- Средства: `net accounts`, `net user`, `secpol.msc`, `gpedit.msc`/GPO, `gpupdate`, `gpresult`.
- Журнал: 4624, 4625, 4740, 4723/4724; распыление паролей — 4625 для многих учётных записей с одного адреса.
- Windows Hello и LAPS снижают зависимость от паролей и риск общих паролей.

## Проверьте себя

:::check
Q: Почему «Minimum password age» должен быть больше 0?
A: Иначе пользователь много раз подряд сменит пароль, обойдёт журнал паролей и вернётся к старому.
Q: Какой риск возникает при пороге блокировки 1?
A: Отказ в обслуживании: злоумышленник или случайная ошибка легко блокируют пользователей.
Q: Под каким Event ID в журнале Windows записывается блокировка учётной записи?
A: 4740.
Q: Почему на заблокированной учётной записи отклоняется даже правильный пароль?
A: Во время блокировки система не проверяет пароль — иначе злоумышленник мог бы продолжать перебор.
Q: Какую проблему решает LAPS?
A: Одинаковый пароль локального администратора на всех компьютерах — он даёт каждому случайный уникальный пароль.
:::

:::link https://learn.microsoft.com/en-us/windows/security/threat-protection/security-policy-settings/password-policy
Microsoft Learn: Password Policy
Параметры парольной политики Windows и рекомендуемые значения (на английском языке).
:::
