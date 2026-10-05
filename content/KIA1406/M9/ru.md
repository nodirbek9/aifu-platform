---
title: Криптографические протоколы и приложения. SSL/TLS, IPSec, SSH, блокчейн, электронное правительство и электронные платежи
summary: Понятие криптографического протокола; SSL/TLS — версии, рукопожатие TLS 1.3, наборы шифров, прямая секретность (PFS); IPSec — AH, ESP, транспортный и туннельный режимы, IKE; SSH — обмен ключами, вход по ключу; блокчейн — цепочка блоков, доказательство работы (proof of work), смарт-контракты; криптография в электронном правительстве и электронных платежах — ЭЦП, ID-карта, чип EMV, 3-D Secure, токенизация.
---

## Почему эта тема важна?

На прошлых уроках мы познакомились с отдельными «кирпичиками» — симметричными шифрами, RSA, Диффи–Хеллманом, хешами и ЭЦП. На практике они работают не по отдельности, а вместе внутри **протоколов**: значок замка в браузере (TLS), туннель между офисом и филиалом (IPSec), администрирование сервера (SSH), оплата банковской картой (EMV), ЭЦП на портале my.gov.uz. Понимать протокол — значит видеть, где может появиться уязвимость.

:::note
**После этого урока вы сможете:**

- объяснить этапы рукопожатия TLS 1.3 и состав набора шифров;
- сказать, на каком уровне и как защищают IPSec и SSH;
- показать, как блоки блокчейна связаны через хеши и как работает доказательство работы;
- знать, где используется криптография в электронных платежах и электронном правительстве.
:::

## Криптографический протокол

**Криптографический протокол** — последовательность сообщений, которыми обмениваются две или более стороны для достижения цели безопасности (конфиденциальный канал, аутентификация, согласование ключа). Даже при сильных алгоритмах ошибка протокола может сломать всю систему: например, **POODLE** в SSL 3.0, **Heartbleed** в TLS (ошибка в программе OpenSSL) или атаки принудительного перехода на старую версию (**downgrade**).

## SSL/TLS

**TLS** (Transport Layer Security) — основа HTTPS, защищённой почты (SMTPS, IMAPS) и многих приложений. Он работает поверх транспортного уровня (TCP).

| Версия | Состояние |
|---|---|
| SSL 2.0, SSL 3.0 | запрещены (DROWN, POODLE) |
| TLS 1.0, TLS 1.1 | официально устарели с 2021 года (RFC 8996) |
| TLS 1.2 | допускается, безопасен при правильной настройке |
| TLS 1.3 (2018) | рекомендуется: быстрее, только сильные алгоритмы |

**Рукопожатие TLS 1.3** (handshake), упрощённо:

1. **ClientHello**: поддерживаемые клиентом наборы шифров, случайное число и его эфемерный открытый ключ (EC)DH.
2. **ServerHello**: сервер выбирает набор шифров и отправляет свой открытый ключ (EC)DH. Обе стороны теперь вычисляют общий секрет — следующие сообщения уже зашифрованы.
3. **Certificate + CertificateVerify**: сервер отправляет сертификат X.509 и свою подпись под рукопожатием — клиент проверяет цепочку сертификатов.
4. **Finished**: обе стороны обмениваются MAC от хеша всего диалога — это подтверждает, что никто не изменил сообщения по дороге.
5. Данные передаются с шифром **AEAD** (AES-GCM или ChaCha20-Poly1305).

**Набор шифров** (cipher suite) — название используемых алгоритмов. В TLS 1.2, например, `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`: обмен ключами ECDHE, аутентификация сервера сертификатом RSA, шифр AES-256-GCM, хеш SHA-384. В TLS 1.3 название короче (`TLS_AES_128_GCM_SHA256`) — обмен ключами всегда эфемерный (EC)DHE.

**Прямая секретность** (Perfect Forward Secrecy, PFS): сеансовый ключ получается из эфемерных ключей DH, которые затем удаляются. Даже если закрытый ключ сервера украдут позже, ранее записанный трафик расшифровать нельзя. Когда ключ передаётся с помощью RSA (`TLS_RSA_WITH_...`), PFS нет — поэтому TLS 1.3 полностью убрал этот способ.

## IPSec

**IPSec** работает на сетевом уровне (IP) и защищает IP-пакеты — приложения менять не нужно. Основное применение — VPN (офис–филиал, удалённый сотрудник).

| Компонент | Назначение |
|---|---|
| **AH** (Authentication Header) | целостность и аутентификация источника, без шифрования |
| **ESP** (Encapsulating Security Payload) | шифрование + целостность (на практике почти всегда ESP) |
| **IKE** (Internet Key Exchange, IKEv2) | аутентифицирует стороны и согласует ключи (на основе DH) |
| **SA** (Security Association) | согласованный набор алгоритмов, ключей и сроков |

**Транспортный режим** защищает только полезную нагрузку пакета (между двумя хостами); **туннельный режим** шифрует весь пакет и упаковывает его в новый IP-заголовок (между двумя шлюзами — классический VPN).

## SSH

**SSH** (Secure Shell, порт 22) — протокол безопасного удалённого администрирования серверов, передачи файлов (SCP, SFTP) и туннелирования; он заменил старые нешифрованные Telnet и FTP.

- **Обмен ключами**: (EC)DH или Curve25519; сервер подписывает своим **ключом хоста**. При первом подключении клиент показывает отпечаток ключа хоста и записывает его в файл `known_hosts`; если ключ позже изменится — предупреждение (это может быть признаком «человека посередине»).
- **Аутентификация пользователя**: пароль или — надёжнее — **пара ключей** (`ssh-keygen -t ed25519`): открытый ключ кладётся в файл `authorized_keys` на сервере, закрытый остаётся у клиента и защищается парольной фразой.
- Хорошая практика: отключить вход по паролю и прямой вход под `root`, использовать ключи, ограничить число попыток входа.

## Блокчейн

**Блокчейн** — цепочка блоков, в которой каждый блок содержит хеш предыдущего. Если данные в одном блоке изменятся, изменится его хеш и нарушится связь во всех следующих блоках. В распределённой сети тысячи узлов хранят копию цепочки и через **консенсус** договариваются, какой блок добавить:

- **Доказательство работы** (Proof of Work, Bitcoin): чтобы добавить блок, нужно найти такой **nonce**, чтобы хеш начинался с заданного числа нулевых битов, — это требует много вычислений, а проверка — один хеш. Чтобы переписать цепочку, злоумышленнику нужно больше половины вычислительной мощности сети («атака 51%»).
- **Доказательство доли** (Proof of Stake, Ethereum с 2022 года): создатели блоков выбираются по размеру внесённого залога, расход энергии намного меньше.

Транзакции подписываются ЭЦП (ECDSA, Ed25519); адрес — хеш открытого ключа. **Смарт-контракты** — программы, хранящиеся в блокчейне и выполняемые автоматически. Ограничения: низкая скорость, ошибку нельзя отменить, при потере закрытого ключа теряются и средства.

## Электронное правительство и электронные платежи

| Сфера | Где криптография |
|---|---|
| **Электронное правительство** (my.gov.uz, кабинет налогоплательщика) | TLS, вход по ключу ЭЦП или чипу ID-карты, подписание документов ЭЦП, единая система идентификации OneID |
| **Банковские карты** (чип EMV, Uzcard, Humo, Visa) | чип формирует одноразовую криптограмму для каждой транзакции — в отличие от магнитной полосы, копирование бесполезно |
| **Интернет-платежи** | TLS, **3-D Secure** (дополнительное подтверждение по SMS или в приложении), стандарт PCI DSS |
| **Мобильные платежи** (Apple Pay, Google Pay) | **токенизация**: вместо настоящего номера карты используется токен, привязанный к устройству |
| **Мобильные приложения** (Click, Payme) | TLS, закрепление сертификата (certificate pinning), подтверждение по SMS или биометрии |

## Разобранный пример 1: учебный блокчейн

В первой строке — сложность *d* (бит), в следующих строках — транзакции. Для каждого блока найдите наименьший nonce, при котором старшие *d* бит 32-битного хеша FNV-1a строки «номер|предыдущий_хеш|транзакция|nonce» равны нулю. Предыдущий хеш первого блока — 00000000. (В настоящем блокчейне используются SHA-256 и намного большая сложность.)

```python
import sys


def fnv1a(matn):
    h = 2166136261
    for bayt in matn.encode("utf-8"):
        h = ((h ^ bayt) * 16777619) & 0xFFFFFFFF
    return h


satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
d = int(satrlar[0])
oldingi = "00000000"
for i, tranzaksiya in enumerate(satrlar[1:], start=1):
    nonce = 0
    while True:
        h = fnv1a(f"{i}|{oldingi}|{tranzaksiya}|{nonce}")
        if h >> (32 - d) == 0:                  # старшие d бит нулевые — доказательство работы найдено
            break
        nonce += 1
    xesh = format(h, "08x")
    print(f"#{i} prev={oldingi} nonce={nonce} hash={xesh} {tranzaksiya}")
    oldingi = xesh                                # следующий блок ссылается на этот хеш
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
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) satrlar.add(s);
        }
        int d = Integer.parseInt(satrlar.get(0).trim());
        String oldingi = "00000000";
        for (int i = 1; i < satrlar.size(); i++) {
            String tranzaksiya = satrlar.get(i);
            int nonce = 0;
            int h;
            while (true) {
                h = fnv1a(i + "|" + oldingi + "|" + tranzaksiya + "|" + nonce);
                if ((h >>> (32 - d)) == 0) break;      // старшие d бит нулевые — доказательство работы найдено
                nonce++;
            }
            String xesh = String.format("%08x", h);
            System.out.println("#" + i + " prev=" + oldingi + " nonce=" + nonce + " hash=" + xesh + " " + tranzaksiya);
            oldingi = xesh;                              // следующий блок ссылается на этот хеш
        }
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

int main() {
    vector<string> satrlar;
    string s;
    while (getline(cin, s)) {
        if (s.find_first_not_of(" \t\r") != string::npos) satrlar.push_back(s);
    }
    int d = stoi(satrlar[0]);
    string oldingi = "00000000";
    for (size_t i = 1; i < satrlar.size(); i++) {
        const string& tranzaksiya = satrlar[i];
        long long nonce = 0;
        uint32_t h;
        while (true) {
            h = fnv1a(to_string(i) + "|" + oldingi + "|" + tranzaksiya + "|" + to_string(nonce));
            if ((h >> (32 - d)) == 0) break;            // старшие d бит нулевые — доказательство работы найдено
            nonce++;
        }
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", h);
        string xesh = buf;
        cout << "#" << i << " prev=" << oldingi << " nonce=" << nonce << " hash=" << xesh << " " << tranzaksiya << endl;
        oldingi = xesh;                                  // следующий блок ссылается на этот хеш
    }
    return 0;
}
```

Ввод:

```text
16
Ali->Vali:5
Vali->Sardor:2
Sardor->Ali:1
```

Вывод:

```text
#1 prev=00000000 nonce=64572 hash=00001674 Ali->Vali:5
#2 prev=00001674 nonce=2317 hash=000048c2 Vali->Sardor:2
#3 prev=000048c2 nonce=143961 hash=0000d51e Sardor->Ali:1
```

Хеш каждого блока начинается с 16 нулевых битов (4 нуля в hex) — для этого нужно в среднем 2¹⁶ ≈ 65 тысяч попыток, а проверка — один хеш. Теперь допустим, что злоумышленник изменил первую транзакцию на `Ali->Vali:9` — та же программа выдаёт другую цепочку:

```text
#1 prev=00000000 nonce=31445 hash=00008283 Ali->Vali:9
#2 prev=00008283 nonce=12125 hash=00003031 Vali->Sardor:2
#3 prev=00003031 nonce=17797 hash=00005e59 Sardor->Ali:1
```

Изменение одного блока требует пересчитать хеши и nonce **всех** следующих блоков — а остальные узлы сети хранят старую цепочку и отвергают поддельную.

## Разобранный пример 2: анализ наборов шифров TLS

Каждое слово — название набора шифров. Разберите его на части: обмен ключами (kx), аутентификация (auth), шифр и хеш; определите наличие PFS и дайте оценку: если в названии есть NULL, EXPORT, RC4, DES (в том числе 3DES) или MD5 — `слабый`; если нет PFS или используется режим CBC — `устаревший`; иначе — `надёжный`. Название без `_WITH_` — набор TLS 1.3: обмен ключами (EC)DHE, аутентификация сертификатом.

```python
import sys

ZAIF = ["NULL", "EXPORT", "RC4", "DES", "MD5"]   # "3DES" тоже содержит "DES"

for nom in sys.stdin.read().split():
    if "_WITH_" in nom:                          # TLS 1.2 и ранее: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
        chap, shifr = nom[4:].split("_WITH_")
        qismlar = chap.split("_")
        almashinuv = qismlar[0]
        autent = qismlar[1] if len(qismlar) > 1 else qismlar[0]
    else:                                        # TLS 1.3: обмена ключами нет в названии
        shifr = nom[4:]
        almashinuv, autent = "(EC)DHE", "cert"
    shifr, xesh = shifr.rsplit("_", 1)
    pfs = "DHE" in almashinuv                    # эфемерный DH — прямая секретность
    if any(z in nom for z in ZAIF):
        baho = "слабый"
    elif not pfs or "CBC" in shifr:
        baho = "устаревший"
    else:
        baho = "надёжный"
    print(f"{nom}: kx={almashinuv} auth={autent} cipher={shifr} hash={xesh} pfs={'+' if pfs else '-'} -> {baho}")
```
```java
import java.util.Scanner;

public class Main {
    static final String[] ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" тоже содержит "DES"

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            String shifr, almashinuv, autent;
            int w = nom.indexOf("_WITH_");
            if (w >= 0) {                                // TLS 1.2 и ранее: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
                String[] qismlar = nom.substring(4, w).split("_");
                shifr = nom.substring(w + 6);
                almashinuv = qismlar[0];
                autent = qismlar.length > 1 ? qismlar[1] : qismlar[0];
            } else {                                     // TLS 1.3: обмена ключами нет в названии
                shifr = nom.substring(4);
                almashinuv = "(EC)DHE";
                autent = "cert";
            }
            int p = shifr.lastIndexOf('_');
            String xesh = shifr.substring(p + 1);
            shifr = shifr.substring(0, p);
            boolean pfs = almashinuv.contains("DHE");    // эфемерный DH — прямая секретность
            boolean zaif = false;
            for (String z : ZAIF) {
                if (nom.contains(z)) zaif = true;
            }
            String baho;
            if (zaif) baho = "слабый";
            else if (!pfs || shifr.contains("CBC")) baho = "устаревший";
            else baho = "надёжный";
            System.out.println(nom + ": kx=" + almashinuv + " auth=" + autent + " cipher=" + shifr
                    + " hash=" + xesh + " pfs=" + (pfs ? "+" : "-") + " -> " + baho);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const vector<string> ZAIF = {"NULL", "EXPORT", "RC4", "DES", "MD5"};   // "3DES" тоже содержит "DES"
    string nom;
    while (cin >> nom) {
        string shifr, almashinuv, autent;
        size_t w = nom.find("_WITH_");
        if (w != string::npos) {                         // TLS 1.2 и ранее: TLS_<kx>_<auth>_WITH_<cipher>_<hash>
            string chap = nom.substr(4, w - 4);
            shifr = nom.substr(w + 6);
            size_t t = chap.find('_');
            almashinuv = chap.substr(0, t);
            autent = t == string::npos ? almashinuv : chap.substr(t + 1, chap.find('_', t + 1) - t - 1);
        } else {                                         // TLS 1.3: обмена ключами нет в названии
            shifr = nom.substr(4);
            almashinuv = "(EC)DHE";
            autent = "cert";
        }
        size_t p = shifr.rfind('_');
        string xesh = shifr.substr(p + 1);
        shifr = shifr.substr(0, p);
        bool pfs = almashinuv.find("DHE") != string::npos;   // эфемерный DH — прямая секретность
        bool zaif = false;
        for (const string& z : ZAIF) {
            if (nom.find(z) != string::npos) zaif = true;
        }
        string baho;
        if (zaif) baho = "слабый";
        else if (!pfs || shifr.find("CBC") != string::npos) baho = "устаревший";
        else baho = "надёжный";
        cout << nom << ": kx=" << almashinuv << " auth=" << autent << " cipher=" << shifr
             << " hash=" << xesh << " pfs=" << (pfs ? "+" : "-") << " -> " << baho << endl;
    }
    return 0;
}
```

Ввод:

```text
TLS_AES_128_GCM_SHA256
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256
TLS_RSA_WITH_AES_128_CBC_SHA
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA
TLS_RSA_WITH_RC4_128_MD5
```

Вывод:

```text
TLS_AES_128_GCM_SHA256: kx=(EC)DHE auth=cert cipher=AES_128_GCM hash=SHA256 pfs=+ -> надёжный
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384: kx=ECDHE auth=RSA cipher=AES_256_GCM hash=SHA384 pfs=+ -> надёжный
TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256: kx=ECDHE auth=ECDSA cipher=CHACHA20_POLY1305 hash=SHA256 pfs=+ -> надёжный
TLS_RSA_WITH_AES_128_CBC_SHA: kx=RSA auth=RSA cipher=AES_128_CBC hash=SHA pfs=- -> устаревший
TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA: kx=DHE auth=RSA cipher=3DES_EDE_CBC hash=SHA pfs=+ -> слабый
TLS_RSA_WITH_RC4_128_MD5: kx=RSA auth=RSA cipher=RC4_128 hash=MD5 pfs=- -> слабый
```

В настройках сервера следует оставить только «надёжные» наборы. У `TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA` есть PFS, но 64-битный блок 3DES уязвим к атаке **Sweet32** — одна сильная часть не делает весь набор безопасным. Для проверки настоящего сервера используют SSL Labs (ssllabs.com/ssltest) или `nmap --script ssl-enum-ciphers`.

:::warning
**Частые ошибки**

- **«Есть HTTPS — значит, сайт надёжный».** TLS защищает только канал; фишинговый сайт тоже может получить бесплатный сертификат. Проверяйте доменное имя.
- **Оставлять старые версии включёнными «для совместимости».** SSL 3.0, TLS 1.0/1.1 и слабые наборы открывают путь атакам downgrade.
- **Игнорировать предупреждение SSH о ключе хоста.** Если ключ неожиданно изменился, сначала выясните причину.
- **Считать блокчейн «невзламываемым».** Цепочка целостна, но огромные суммы были потеряны из-за ошибок смарт-контрактов, взломов бирж и кражи закрытых ключей.
- **Нарушать правило никому не сообщать данные карты и SMS-код.** Код 3-D Secure — это именно ваше подтверждение; код, сказанный «сотруднику банка», позволяет злоумышленнику провести платёж.
:::

:::tip
В браузере F12 → вкладка Security показывает версию TLS и набор шифров текущего сайта. В терминале `openssl s_client -connect example.com:443 -tls1_3` выводит цепочку сертификатов и согласованный набор.
:::

## Итоги

- Криптографический протокол объединяет алгоритмы в последовательность сообщений; ошибка протокола делает бесполезным даже сильный алгоритм.
- TLS 1.3: согласование ключа (EC)DHE, аутентификация сертификатом, шифр AEAD, обязательная PFS; SSL и TLS 1.0/1.1 устарели.
- IPSec — на уровне IP (AH, ESP, IKE; транспортный и туннельный режимы); SSH — ключ хоста, вход по паре ключей.
- Блокчейн — цепочка блоков, связанных хешами, плюс консенсус (PoW, PoS); изменение одного блока ломает следующие.
- Электронное правительство и платежи: ЭЦП, ID-карта, криптограмма EMV, 3-D Secure, токенизация.

## Проверьте себя

:::check
Q: Что гарантирует прямая секретность (PFS)?
A: Даже если закрытый ключ сервера украдут позже, ранее записанные сеансы расшифровать нельзя — сеансовые ключи получаются из эфемерных ключей DH.
Q: Чем туннельный режим IPSec отличается от транспортного?
A: Туннельный режим шифрует весь IP-пакет и упаковывает его в новый заголовок (VPN между шлюзами); транспортный защищает только полезную нагрузку.
Q: В чём преимущество пары ключей перед паролем в SSH?
A: Закрытый ключ не передаётся по сети и его нельзя подобрать; атаки перебора паролей становятся бесполезными.
Q: Почему трудно изменить старый блок в блокчейне?
A: Меняется хеш блока, и доказательство работы нужно заново выполнить для всех следующих блоков, а остальные узлы хранят исходную цепочку.
Q: Что защищает токенизация в мобильных платежах?
A: Настоящий номер карты — точка продаж получает только токен, привязанный к устройству, который бесполезен в другом месте даже при краже.
:::

:::link https://www.rfc-editor.org/rfc/rfc8446
RFC 8446: The Transport Layer Security (TLS) Protocol Version 1.3
Официальная спецификация протокола TLS 1.3 (на английском языке).
:::
