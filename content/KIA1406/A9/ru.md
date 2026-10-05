---
title: Проверка сертификата SSL/TLS (на примере браузера)
summary: Практическая проверка сертификата сайта в браузере — значок замка, окно просмотра сертификата, владелец (Subject, SAN), выдавший CA, срок действия, отпечаток, цепочка сертификатов; сертификаты DV, OV и EV; ошибочные случаи на badssl.com — просроченный, с несовпадающим именем, самоподписанный и отозванный сертификаты; инструменты SSL Labs и crt.sh; HSTS; программная проверка срока действия и совпадения имени; тест по теме.
---

## Почему эта тема важна?

За значком замка в браузере стоит весь механизм, изученный в M8 и M9: сертификат X.509, подпись CA, цепочка доверия и рукопожатие TLS. Для пользователя важнейший навык — **понимать** предупреждение о сертификате и не игнорировать его; для администратора — обновлять сертификат до истечения срока. На этом занятии мы рассмотрим сертификаты реальных сайтов и намеренно сломанных тестовых сайтов.

:::note
**После этого занятия вы сможете:**

- просматривать и анализировать поля и цепочку сертификата в браузере;
- назвать различия сертификатов DV, OV и EV;
- распознавать ошибки сертификатов в браузере и объяснять их причины;
- программно проверять срок действия сертификата и совпадение доменного имени.
:::

## Практическая работа

**Шаг 1. Просмотр сертификата.** В Chrome или Edge откройте любой HTTPS-сайт (например, my.gov.uz или google.com) → значок в адресной строке → «Connection is secure» → «Certificate is valid». В Firefox: замок → «Connection secure» → «More information» → «View Certificate». Запишите следующее:

| Поле | Что проверять |
|---|---|
| **Issued To** (Subject, CN) | имя сайта или организация |
| **Subject Alternative Name** | все домены, для которых действует сертификат, включая wildcard-имена, начинающиеся с `*.` |
| **Issued By** (Issuer) | выдавший CA (например, Let's Encrypt, DigiCert, GlobalSign) |
| **Validity** | даты Not Before и Not After |
| **Public Key** | алгоритм и длина (RSA 2048, ECDSA P-256) |
| **Fingerprint** (SHA-256) | уникальный отпечаток сертификата |

На вкладке «Details» или «Certification Path» посмотрите **цепочку**: сертификат сайта → промежуточный CA → корневой CA. Корневой сертификат находится в списке доверенных ОС или браузера (в Windows: `certmgr.msc` → Trusted Root Certification Authorities).

**Шаг 2. Типы сертификатов.** Сравните несколько сайтов:

| Тип | Что проверяется | Где |
|---|---|---|
| **DV** (Domain Validation) | только владение доменом (автоматически, за минуты) | блоги, большинство сайтов; Let's Encrypt выдаёт бесплатно |
| **OV** (Organization Validation) | домен + документы организации | сайты компаний |
| **EV** (Extended Validation) | углублённая юридическая проверка | раньше банки; браузеры больше не показывают отдельный зелёный индикатор |

Важно: сертификат DV говорит только «вы в зашифрованном канале с этим доменом», а не «сайт надёжный». Большинство фишинговых сайтов тоже используют HTTPS.

**Шаг 3. Ошибочные случаи (badssl.com).** Откройте следующие тестовые сайты и запишите, что сообщает браузер для каждого (**не переходите** на страницу — только прочитайте предупреждение):

| Сайт | Проблема | Сообщение браузера (Chrome) |
|---|---|---|
| expired.badssl.com | истёк срок | NET::ERR_CERT_DATE_INVALID |
| wrong.host.badssl.com | доменное имя не совпадает с SAN | NET::ERR_CERT_COMMON_NAME_INVALID |
| self-signed.badssl.com | самоподписанный | NET::ERR_CERT_AUTHORITY_INVALID |
| untrusted-root.badssl.com | корневого CA нет в списке доверенных | NET::ERR_CERT_AUTHORITY_INVALID |
| revoked.badssl.com | сертификат отозван | NET::ERR_CERT_REVOKED (если браузер проверяет отзыв) |

Если такое сообщение появилось на реальном сайте, это может быть атака «человек посередине», неправильно настроенный сервер или неверная дата на компьютере. **Не вводите** пароли и данные карт.

**Шаг 4. Анализ сервера.** Проверьте какой-нибудь сайт на ssllabs.com/ssltest: общая оценка (A+…F), поддерживаемые версии TLS, наборы шифров (M9), полнота цепочки, **HSTS** (заголовок, заставляющий браузер всегда использовать HTTPS). Найдите доменное имя на crt.sh — в журналах **Certificate Transparency** видны все сертификаты, выданные для этого домена; сертификат от постороннего CA обнаруживается сразу.

**Шаг 5. Тест.** Пройдите тест «SSL/TLS и сертификаты» на платформе.

## Разобранный пример 1: контроль срока действия сертификата

Просроченный сертификат — самая частая и самая «неловкая» неисправность: сайт перестаёт работать за одну ночь. Сроки сертификатов сокращаются: Let's Encrypt — 90 дней, по решению CA/Browser Forum общий максимум — 200 дней с марта 2026 года и 47 дней к 2029 году. Поэтому контроль автоматизируют. В каждой строке — домен, Not Before, Not After и сегодняшняя дата (YYYY-MM-DD). Выведите состояние: ещё не действует, истёк, осталось меньше 30 дней (нужно обновить) или действует.

```python
import sys


def kunlar(sana):                                  # число дней с 1970-01-01
    y, m, d = map(int, sana.split("-"))
    y -= m <= 2                                    # считаем, что год начинается с марта
    era = y // 400
    yoe = y - era * 400
    doy = (153 * (m + (-3 if m > 2 else 9)) + 2) // 5 + d - 1
    doe = yoe * 365 + yoe // 4 - yoe // 100 + doy  # високосные годы учитываются
    return era * 146097 + doe - 719468


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    domen, boshi, oxiri, bugun = satr.split()
    b, o, t = kunlar(boshi), kunlar(oxiri), kunlar(bugun)
    if t < b:
        holat = "ещё не действует"
    elif t > o:
        holat = f"истёк ({t - o} дн. назад)"
    elif o - t < 30:
        holat = f"обновите: {o - t} дн. осталось"
    else:
        holat = f"действует, {o - t} дн. осталось"
    print(f"{domen}: {holat}")
```
```java
import java.util.Scanner;

public class Main {
    static long kunlar(String sana) {                     // число дней с 1970-01-01
        String[] q = sana.split("-");
        long y = Long.parseLong(q[0]), m = Long.parseLong(q[1]), d = Long.parseLong(q[2]);
        if (m <= 2) y--;                                  // считаем, что год начинается с марта
        long era = y / 400;
        long yoe = y - era * 400;
        long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy; // високосные годы учитываются
        return era * 146097 + doe - 719468;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String domen = in.next();
            long b = kunlar(in.next()), o = kunlar(in.next()), t = kunlar(in.next());
            String holat;
            if (t < b) holat = "ещё не действует";
            else if (t > o) holat = "истёк (" + (t - o) + " дн. назад)";
            else if (o - t < 30) holat = "обновите: " + (o - t) + " дн. осталось";
            else holat = "действует, " + (o - t) + " дн. осталось";
            System.out.println(domen + ": " + holat);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

long long kunlar(const string& sana) {                    // число дней с 1970-01-01
    long long y, m, d;
    sscanf(sana.c_str(), "%lld-%lld-%lld", &y, &m, &d);
    if (m <= 2) y--;                                      // считаем, что год начинается с марта
    long long era = y / 400;
    long long yoe = y - era * 400;
    long long doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    long long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;   // високосные годы учитываются
    return era * 146097 + doe - 719468;
}

int main() {
    string domen, boshi, oxiri, bugun;
    while (cin >> domen >> boshi >> oxiri >> bugun) {
        long long b = kunlar(boshi), o = kunlar(oxiri), t = kunlar(bugun);
        string holat;
        if (t < b) holat = "ещё не действует";
        else if (t > o) holat = "истёк (" + to_string(t - o) + " дн. назад)";
        else if (o - t < 30) holat = "обновите: " + to_string(o - t) + " дн. осталось";
        else holat = "действует, " + to_string(o - t) + " дн. осталось";
        cout << domen << ": " << holat << endl;
    }
    return 0;
}
```

Ввод:

```text
my.gov.uz 2026-03-01 2027-03-01 2026-10-05
bank.uz 2025-10-20 2026-10-20 2026-10-05
old.uz 2024-01-01 2026-09-25 2026-10-05
new.uz 2026-11-01 2027-11-01 2026-10-05
leap.uz 2024-01-15 2024-03-01 2024-02-28
```

Вывод:

```text
my.gov.uz: действует, 147 дн. осталось
bank.uz: обновите: 15 дн. осталось
old.uz: истёк (10 дн. назад)
new.uz: ещё не действует
leap.uz: обновите: 2 дн. осталось
```

Перевод дат в число дней правильно учитывает длину месяцев и високосные годы: от 2024-02-28 до 2024-03-01 — 2 дня (есть 29 февраля). Браузер сравнивает точно так же — поэтому при неверной дате на компьютере все сайты выглядят «просроченными». На практике средства мониторинга (например, certbot с Let's Encrypt) автоматически обновляют сертификат за 30 дней до истечения.

## Разобранный пример 2: совпадение доменного имени и SAN

Браузер сравнивает имя из адресной строки со списком SAN сертификата (правила RFC 6125). Регистр букв не важен; wildcard `*.` может стоять только на месте **одной самой левой метки** и совпадает ровно с одной меткой: `*.example.uz` совпадает с `www.example.uz`, но не с `example.uz` и не с `a.b.example.uz`. В первой строке — список SAN, в следующих — проверяемые имена. Выведите совпавшую запись SAN или `не совпадает`.

```python
import sys


def mos(naqsh, nom):
    naqsh, nom = naqsh.lower(), nom.lower()        # имена DNS нечувствительны к регистру
    if not naqsh.startswith("*."):
        return naqsh == nom
    qism = nom.split(".", 1)                       # левая метка и остальная часть
    return len(qism) == 2 and qism[0] != "" and qism[1] == naqsh[2:]


satrlar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
san = satrlar[0].split()
for nom in satrlar[1:]:
    topildi = next((n for n in san if mos(n, nom)), None)
    print(f"{nom} -> " + (f"совпадает ({topildi})" if topildi else "не совпадает"))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean mos(String naqsh, String nom) {
        naqsh = naqsh.toLowerCase();                       // имена DNS нечувствительны к регистру
        nom = nom.toLowerCase();
        if (!naqsh.startsWith("*.")) return naqsh.equals(nom);
        int nuqta = nom.indexOf('.');                      // левая метка и остальная часть
        return nuqta > 0 && nom.substring(nuqta + 1).equals(naqsh.substring(2));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s);
        }
        String[] san = satrlar.get(0).split("\\s+");
        for (int i = 1; i < satrlar.size(); i++) {
            String nom = satrlar.get(i);
            String topildi = null;
            for (String n : san) {
                if (mos(n, nom)) {
                    topildi = n;
                    break;
                }
            }
            System.out.println(nom + " -> " + (topildi != null ? "совпадает (" + topildi + ")" : "не совпадает"));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

string kichik(string s) {
    transform(s.begin(), s.end(), s.begin(), [](unsigned char c) { return tolower(c); });
    return s;
}

bool mos(string naqsh, string nom) {
    naqsh = kichik(naqsh);                                 // имена DNS нечувствительны к регистру
    nom = kichik(nom);
    if (naqsh.rfind("*.", 0) != 0) return naqsh == nom;
    size_t nuqta = nom.find('.');                          // левая метка и остальная часть
    return nuqta != string::npos && nuqta > 0 && nom.substr(nuqta + 1) == naqsh.substr(2);
}

int main() {
    string satr;
    getline(cin, satr);
    istringstream ss(satr);
    vector<string> san;
    string n;
    while (ss >> n) san.push_back(n);
    string nom;
    while (cin >> nom) {
        string topildi;
        for (const string& x : san) {
            if (mos(x, nom)) {
                topildi = x;
                break;
            }
        }
        cout << nom << " -> " << (!topildi.empty() ? "совпадает (" + topildi + ")" : "не совпадает") << endl;
    }
    return 0;
}
```

Ввод:

```text
example.uz *.example.uz mail.test.uz
example.uz
www.example.uz
WWW.Example.UZ
a.b.example.uz
test.uz
mail.test.uz
example.uz.evil.com
```

Вывод:

```text
example.uz -> совпадает (example.uz)
www.example.uz -> совпадает (*.example.uz)
WWW.Example.UZ -> совпадает (*.example.uz)
a.b.example.uz -> не совпадает
test.uz -> не совпадает
mail.test.uz -> совпадает (mail.test.uz)
example.uz.evil.com -> не совпадает
```

`example.uz.evil.com` — любимый приём фишинговых сайтов: имя начинается с «example.uz», но на самом деле это домен `evil.com`. Браузер проверяет имя справа и целиком и отвергает такое имя — пользователь должен читать адрес так же. Ошибка wrong.host.badssl.com — именно результат этой проверки.

:::warning
**Частые ошибки**

- **Нажимать «Продолжить» при предупреждении о сертификате.** Особенно в публичной сети Wi-Fi — это классический признак атаки «человек посередине».
- **«Есть замок — сайт безопасен».** Замок означает только, что канал зашифрован; проверяйте доменное имя.
- **Неверная дата на компьютере.** Если ошибка сертификата на всех сайтах, сначала проверьте часы и дату.
- **Следить за сроком сертификата вручную.** Настройте автоматическое обновление (ACME, certbot) и контроль сроков.
- **Неполная цепочка.** Если сервер не отправляет сертификат промежуточного CA, на некоторых устройствах будет ошибка; SSL Labs это покажет.
:::

:::tip
Просмотр сертификата в терминале: `openssl s_client -connect my.gov.uz:443 -servername my.gov.uz </dev/null | openssl x509 -noout -subject -issuer -dates -ext subjectAltName`. Команда выводит Subject, Issuer, даты действия и список SAN.
:::

## Итоги

- В сертификате проверяют: Subject и SAN (домен), Issuer (CA), Validity (даты), ключ, отпечаток и цепочку доверия.
- DV — только владение доменом; OV и EV — проверяется и организация; HTTPS не гарантирует надёжность сайта.
- Ошибки браузера: истёк, имя не совпадает, недоверенный CA, отозван — не вводите данные.
- SSL Labs — анализ настроек сервера; crt.sh — журналы Certificate Transparency; HSTS — обязательный HTTPS.
- Срок и совпадение имени можно проверить программно; wildcard совпадает только с одной левой меткой.

## Проверьте себя

:::check
Q: Какое поле сертификата показывает, для каких доменов он действует?
A: Subject Alternative Name (SAN).
Q: Действует ли сертификат *.example.uz для example.uz и a.b.example.uz?
A: Нет: wildcard совпадает ровно с одной левой меткой — с www.example.uz, но не с example.uz и не с a.b.example.uz.
Q: Если на всех сайтах сразу появилась ошибка «срок истёк», какова вероятная причина?
A: Неверная дата или время на компьютере.
Q: Гарантирует ли сертификат DV, что сайт принадлежит надёжной организации?
A: Нет, DV подтверждает только управление доменом; фишинговые сайты тоже получают сертификаты DV.
:::

:::link https://badssl.com/
badssl.com
Намеренно неправильно настроенные тестовые сайты для безопасной проверки ошибок сертификатов и TLS (на английском языке).
:::
