---
title: Хеш-функции и создание ЭЦП (в онлайн-инструментах)
summary: Практическая работа с хеш-функциями и ЭЦП — вычисление хеша в CyberChef и командами операционной системы (certutil, Get-FileHash, sha256sum), проверка целостности скачанного файла, вычисление HMAC, создание и проверка ЭЦП в GnuPG (Kleopatra) или OpenSSL, система E-IMZO в Узбекистане; хранение паролей — соль и медленное хеширование; дерево Меркла; программное задание — проверка целостности с SHA-256.
---

## Почему эта тема важна?

На лекции M8 мы теоретически рассмотрели хеш, MAC, ЭЦП и PKI. Теперь применим их в повседневных задачах: проверим, что скачанная программа подлинная, подпишем документ и проверим подпись, правильно сохраним пароли пользователей. В конце занятия вы сами напишете проверку целостности на основе SHA-256.

:::note
**После этого занятия вы сможете:**

- вычислять хеш текста и файла онлайн-инструментами и командами ОС;
- проверять целостность скачанного файла по опубликованному хешу;
- создавать и проверять HMAC и ЭЦП;
- показать, зачем пароли хранят с солью и медленным хешированием;
- объяснить, как работает дерево Меркла.
:::

## Практическая работа

**Шаг 1. Лавинный эффект (CyberChef).** На gchq.github.io/CyberChef выберите рецепт «SHA2» (Size = 256). Сравните хеши текстов `salom`, `Salom` и `salom ` (с пробелом в конце) — они совершенно разные. Затем сравните длину хешей с рецептами «MD5» и «SHA3» (32, 64 и 64 hex-символа).

**Шаг 2. Целостность файла.** Скачайте какую-нибудь официальную программу (например, Python, VLC или 7-Zip) и найдите опубликованное на странице значение SHA-256. Вычислите хеш:

```text
Windows (cmd):         certutil -hashfile file.exe SHA256
Windows (PowerShell):  Get-FileHash file.exe -Algorithm SHA256
Linux / macOS:         sha256sum file.exe   (macOS: shasum -a 256 file.exe)
```

Если значения совпадают — файл не изменён по дороге. Замечание: если сайт взломан, злоумышленник может подменить и значение хеша — поэтому разработчики публикуют список хешей ещё и с ЭЦП (подписью GPG).

**Шаг 3. HMAC.** В CyberChef вычислите MAC сообщения рецептом «HMAC» (Key = `secret`, Hashing function = SHA256). Измените одну букву ключа — MAC изменится полностью. Тот, кто не знает ключа, не сможет создать правильный MAC.

**Шаг 4. Создание и проверка ЭЦП.** Выберите один из двух способов:

- **GnuPG / Kleopatra** (Gpg4win, бесплатно): File → New Key Pair → имя и email; выберите документ и «Sign» → появится файл `.sig`; проверьте его командой «Verify». Затем добавьте в документ один символ и убедитесь, что проверка не проходит.
- **OpenSSL** (как в A7): `openssl dgst -sha256 -sign private.pem -out signature.bin document.txt` и `-verify public.pem`.

**Шаг 5. ЭЦП в Узбекистане (E-IMZO).** Для государственных услуг и налоговой системы ключи ЭЦП выдают уполномоченные центры (например, e-imzo.uz): ключ хранится в виде файла (`.pfx`) на компьютере или в чипе ID-карты и используется в браузере через модуль E-IMZO. Личность владельца проверяется и выдаётся сертификат; при подписании документа его хеш подписывается закрытым ключом. **Никому не передавайте ключ ЭЦП и его пароль** — по закону он равен вашей собственноручной подписи.

**Шаг 6. Программа.** Выполните задание **Проверка целостности с SHA-256** на платформе: вычислите SHA-256 сообщения и сравните с ожидаемым значением (`Yaxlit` — целый или `Buzilgan` — повреждён). Используйте стандартную библиотеку каждого языка (Python — `hashlib`, Java — `MessageDigest`); в стандартной библиотеке C++ SHA-256 нет — напишите его сами по стандарту FIPS 180-4.

## Разобранный пример 1: соль и медленное хеширование

В каждой строке — логин, пароль и случайная соль пользователя. Сравните три способа хранения: обычный хеш *H*(пароль), хеш с солью *H*(соль ‖ пароль) и медленный хеш — повторное хеширование хеша с солью 100 000 раз. В учебных целях *H* — 32-битный FNV-1a (как в M8); в реальных системах используют bcrypt, scrypt, Argon2 или PBKDF2.

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    login, parol, tuz = satr.split()
    oddiy = fnv1a(parol)
    tuzli = fnv1a(tuz + parol)                 # соль добавляется к паролю
    h = tuzli
    for _ in range(100000):                    # намеренное замедление (key stretching)
        h = fnv1a(format(h, "08x") + tuz)
    print(f"{login}: plain={oddiy:08x} salted={tuzli:08x} slow={h:08x}")
```
```java
import java.nio.charset.StandardCharsets;
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
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] q = satr.split("\\s+");
            String login = q[0], parol = q[1], tuz = q[2];
            int oddiy = fnv1a(parol);
            int tuzli = fnv1a(tuz + parol);                  // соль добавляется к паролю
            int h = tuzli;
            for (int i = 0; i < 100000; i++) {               // намеренное замедление (key stretching)
                h = fnv1a(String.format("%08x", h) + tuz);
            }
            System.out.println(String.format("%s: plain=%08x salted=%08x slow=%08x", login, oddiy, tuzli, h));
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <sstream>
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
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string login, parol, tuz;
        if (!(ss >> login >> parol >> tuz)) continue;
        uint32_t oddiy = fnv1a(parol);
        uint32_t tuzli = fnv1a(tuz + parol);                 // соль добавляется к паролю
        uint32_t h = tuzli;
        char buf[16];
        for (int i = 0; i < 100000; i++) {                   // намеренное замедление (key stretching)
            snprintf(buf, sizeof buf, "%08x", h);
            h = fnv1a(string(buf) + tuz);
        }
        printf("%s: plain=%08x salted=%08x slow=%08x\n", login.c_str(), oddiy, tuzli, h);
    }
    return 0;
}
```

Ввод:

```text
ali qwerty123 x7Kp
vali qwerty123 Qm2z
sardor Tosh2024! b9Lr
```

Вывод:

```text
ali: plain=cffd7677 salted=bcb1b0b1 slow=7e749555
vali: plain=cffd7677 salted=26151749 slow=9151da10
sardor: plain=9c1e624e salted=da7e098f slow=feab59f8
```

При обычном хеше у ali и vali одинаковое значение — если базу украдут, злоумышленник сразу увидит, что их пароли совпадают, и найдёт их все разом по готовой таблице популярных паролей (rainbow table). С солью одинаковые пароли дают разные хеши, и готовая таблица бесполезна: подбор нужно начинать отдельно для каждого пользователя. Медленный хеш делает каждую попытку в 100 000 раз дороже — пользователь этого не замечает (один вход), а злоумышленник замечает на миллиардах попыток.

## Разобранный пример 2: дерево Меркла

**Дерево Меркла** представляет большой объём данных одним хешем — **корнем**: листья — хеши каждой записи, каждый родительский узел — хеш от объединённых хешей двух детей; на уровне с нечётным числом элементов последний дублируется. Оно используется в блоках блокчейна, в Git, в журналах Certificate Transparency. Каждая строка — одна запись; выведите каждый уровень и корень (*H* — FNV-1a).

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


daraja = [format(fnv1a(s), "08x") for s in sys.stdin.read().split("\n") if s.strip()]
qadam = 0
print(f"L{qadam}: " + " ".join(daraja))
while len(daraja) > 1:
    if len(daraja) % 2 == 1:
        daraja.append(daraja[-1])                # если нечётно, последний дублируется
    daraja = [format(fnv1a(daraja[i] + daraja[i + 1]), "08x") for i in range(0, len(daraja), 2)]
    qadam += 1
    print(f"L{qadam}: " + " ".join(daraja))
print("root = " + daraja[0])
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
        List<String> daraja = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) daraja.add(String.format("%08x", fnv1a(s)));
        }
        int qadam = 0;
        System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        while (daraja.size() > 1) {
            if (daraja.size() % 2 == 1) daraja.add(daraja.get(daraja.size() - 1));   // если нечётно, последний дублируется
            List<String> keyingi = new ArrayList<>();
            for (int i = 0; i < daraja.size(); i += 2) {
                keyingi.add(String.format("%08x", fnv1a(daraja.get(i) + daraja.get(i + 1))));
            }
            daraja = keyingi;
            qadam++;
            System.out.println("L" + qadam + ": " + String.join(" ", daraja));
        }
        System.out.println("root = " + daraja.get(0));
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

uint32_t fnv1a(const string& matn) {
    uint32_t h = 2166136261u;
    for (unsigned char bayt : matn) {
        h = (h ^ bayt) * 16777619u;
    }
    return h;
}

string hex8(uint32_t h) {
    char buf[16];
    snprintf(buf, sizeof buf, "%08x", h);
    return buf;
}

void chiqar(int qadam, const vector<string>& daraja) {
    cout << "L" << qadam << ":";
    for (const string& x : daraja) cout << " " << x;
    cout << endl;
}

int main() {
    vector<string> daraja;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) daraja.push_back(hex8(fnv1a(s)));
    }
    int qadam = 0;
    chiqar(qadam, daraja);
    while (daraja.size() > 1) {
        if (daraja.size() % 2 == 1) daraja.push_back(daraja.back());   // если нечётно, последний дублируется
        vector<string> keyingi;
        for (size_t i = 0; i < daraja.size(); i += 2) {
            keyingi.push_back(hex8(fnv1a(daraja[i] + daraja[i + 1])));
        }
        daraja = keyingi;
        qadam++;
        chiqar(qadam, daraja);
    }
    cout << "root = " << daraja[0] << endl;
    return 0;
}
```

Ввод:

```text
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
Ali->Olim:3
Olim->Vali:4
```

Вывод:

```text
L0: 47af2243 bfba4043 c18587fe fd725d1a c1e3e7a7
L1: 338b6726 5b128ae6 b54962fd
L2: 2514cf2e 22ad2995
L3: 43d5aefb
root = 43d5aefb
```

Если изменить четвёртую запись на `Ali->Olim:8`:

```text
L0: 47af2243 bfba4043 c18587fe f4724eef c1e3e7a7
L1: 338b6726 1f581bad b54962fd
L2: e91b25a2 22ad2995
L3: cebf2f88
root = cebf2f88
```

Изменился только путь от изменённого листа до корня (4 хеша), и корень стал другим. Поэтому в заголовке блока достаточно хранить только корень. Кроме того, чтобы доказать, что запись есть в блоке, нужен не весь блок, а только «соседние» хеши на пути — log₂ *n* хешей для *n* записей.

:::warning
**Частые ошибки**

- **Проверять хеш, взятый только с того же сайта.** Если сайт взломан, хеш тоже поддельный; проверяйте подписанный список хешей или другой источник.
- **Сравнивать хеш на глаз «по началу и концу».** Сравнивайте полностью или проверяйте автоматически командой.
- **Одна соль для всех пользователей.** Соль должна быть отдельной и случайной для каждого пользователя.
- **Игнорировать кодировку текста.** В UTF-8 и в другой кодировке, с `\n` или `\r\n` в конце строки хеш получается разным.
- **Передавать другим файл ключа ЭЦП и пароль.** Это то же самое, что отдать свою собственноручную подпись.
:::

:::tip
Если на странице загрузки есть файл `SHA256SUMS` и его подпись `.sig`, сначала проверьте подпись открытым ключом разработчика (`gpg --verify SHA256SUMS.sig SHA256SUMS`), затем файл по списку (`sha256sum -c SHA256SUMS`) — такая двухэтапная проверка — стандартная практика дистрибутивов Linux.
:::

## Итоги

- Хеш можно вычислить в CyberChef, `certutil`, `Get-FileHash`, `sha256sum`; изменение одного символа полностью меняет хеш.
- Целостность файла — сравнение с опубликованным хешем; чтобы ему доверять, список хешей должен быть подписан.
- HMAC — хеш с ключом; ЭЦП — подпись закрытым ключом, проверка открытым (GnuPG, OpenSSL, E-IMZO).
- Пароли: случайная соль для каждого пользователя и намеренно медленный алгоритм.
- Дерево Меркла представляет много записей одним корневым хешем; изменение одной записи меняет корень.

## Проверьте себя

:::check
Q: Что делать, если хеш скачанного файла не совпадает со значением на странице?
A: Не использовать файл: он повреждён или изменён; скачать заново из официального источника и проверить снова.
Q: Почему у двух пользователей с одинаковым паролем разные хеши с солью?
A: Каждому добавляется своя случайная соль, поэтому хешируемые строки разные.
Q: Как медленное хеширование мешает злоумышленнику, но не пользователю?
A: Пользователь входит один раз и не замечает задержки; для злоумышленника дорожает каждая из миллиардов попыток.
Q: Какие хеши меняются в дереве Меркла при изменении одной записи?
A: Хеши на пути от этого листа до корня, включая корень.
:::

:::link https://csrc.nist.gov/pubs/fips/180-4/upd1/final
FIPS 180-4: Secure Hash Standard (SHS)
Официальный стандарт алгоритмов семейства SHA-1 и SHA-2 (SHA-256, SHA-512) (на английском языке).
:::
