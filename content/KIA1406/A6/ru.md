---
title: Практика с программой шифрования на симметричных алгоритмах (AES)
summary: Практическая работа с AES — проверка тестового вектора FIPS 197 в CyberChef, выбор ключа, IV и режима, получение ключа из пароля (PBKDF2), шифрование и расшифрование файла в OpenSSL, архив 7-Zip и шифрование дисков (BitLocker, VeraCrypt); внутренние шаги AES — умножение в поле GF(2⁸), вычисление S-box (SubBytes) и MixColumns; тест по теме.
---

## Почему эта тема важна?

На лекции M6 мы теоретически рассмотрели устройство AES и режимы работы. На практике ошибки бывают не в алгоритме, а в **его использовании**: простой пароль вместо ключа, повторённый IV, режим ECB, ключ рядом с шифртекстом. На этом занятии мы применим AES в реальных инструментах, проверим результат по официальному тестовому вектору и сами вычислим два внутренних шага AES.

:::note
**После этого занятия вы сможете:**

- шифровать и расшифровывать AES в CyberChef и OpenSSL;
- правильно выбирать ключ, IV, режим и дополнение;
- объяснить, как ключ получается из пароля (PBKDF2, соль);
- вычислять умножение в GF(2⁸), значения S-box и MixColumns.
:::

## Напоминание: параметры AES

| Параметр | Значение |
|---|---|
| Блок | всегда 128 бит (16 байт) |
| Ключ | 128, 192 или 256 бит → 10, 12 или 14 раундов |
| Раунд | SubBytes → ShiftRows → MixColumns → AddRoundKey (в последнем раунде нет MixColumns) |
| Режим | рекомендуется **GCM** (шифрование + целостность); CBC — если IV случайный; ECB — не используйте |
| IV / nonce | в CBC 16 случайных байт; в GCM 12 байт, с одним ключом **никогда не повторяется** |
| Дополнение | в CBC и ECB — PKCS#7 (*n* недостающих байт — *n* байт со значением *n*); в CTR и GCM не нужно |

## Практическая работа

**Шаг 1. Тестовый вектор (CyberChef).** На gchq.github.io/CyberChef выберите рецепт «AES Encrypt»: Key = `000102030405060708090a0b0c0d0e0f` (Hex), Mode = ECB, Input = `00112233445566778899aabbccddeeff` (Input format: Hex, Output: Hex). Результат должен совпасть со значением из стандарта FIPS 197: `69c4e0d86a7b0430d8cdb78070b4c55a` (из-за PKCS#7 после него идёт ещё один блок дополнения). Тестовый вектор — стандартный способ проверить, что программа работает правильно.

**Шаг 2. Режимы.** В том же рецепте задайте Mode = CBC, IV = 16 нулевых байт, затем измените один байт IV — изменится весь шифртекст. В режиме GCM к результату добавляется **Tag** (код аутентификации): если изменить один байт шифртекста и выполнить «AES Decrypt», расшифрование завершится ошибкой — это защита целостности.

**Шаг 3. Шифрование файла паролем (OpenSSL).** В Linux, macOS или Git Bash:

```text
openssl enc -aes-256-cbc -pbkdf2 -iter 600000 -salt -in report.txt -out report.enc
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in report.enc -out report2.txt
```

Здесь пароль не является ключом напрямую: **PBKDF2** 600 000 раз вычисляет хеш от пароля и случайной **соли** и получает 256-битный ключ и IV — подбор пароля замедляется во столько же раз. Зашифруйте один и тот же файл дважды: так как соль каждый раз новая, шифртексты различаются.

**Шаг 4. Повседневные инструменты.** При создании архива в 7-Zip выберите «Encryption method: AES-256» и «Encrypt file names». Шифрование всего диска: в Windows — **BitLocker** (в редакциях Pro), в других системах — **VeraCrypt**. Внутри всех них работает AES; безопасность же зависит в основном от **качества пароля** и от того, где хранится ключ восстановления.

**Шаг 5. Тест.** Пройдите тест «Симметричное шифрование и AES» на платформе.

## Разобранный пример 1: GF(2⁸) и S-box

AES рассматривает байты как элементы поля **GF(2⁸)**: сложение — XOR, умножение — перемножение многочленов с остатком по модулю *x*⁸ + *x*⁴ + *x*³ + *x* + 1 (0x11B). Таблица SubBytes (S-box) вычисляется для каждого байта в два шага: 1) обратный элемент в поле *b* = *x*⁻¹ (для 0 — 0); 2) аффинное преобразование *s* = *b* ⊕ rotl(*b*, 1) ⊕ rotl(*b*, 2) ⊕ rotl(*b*, 3) ⊕ rotl(*b*, 4) ⊕ 0x63. Для каждого hex-байта выведите обратный элемент и значение S-box.

```python
import sys


def kopaytir(a, b):                       # умножение в GF(2^8)
    r = 0
    while b:
        if b & 1:
            r ^= a                        # сложение = XOR
        a <<= 1
        if a & 0x100:
            a ^= 0x11B                    # по модулю x^8 + x^4 + x^3 + x + 1
        b >>= 1
    return r


def teskari(a):                           # a^254 = a^(-1), так как a^255 = 1
    if a == 0:
        return 0
    r = 1
    for _ in range(254):
        r = kopaytir(r, a)
    return r


def sbox(x):
    b = teskari(x)
    s = b
    for i in range(1, 5):
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF   # циклический сдвиг влево
    return s ^ 0x63


for t in sys.stdin.read().split():
    x = int(t, 16)
    print(f"{t} -> inv={teskari(x):02x} S={sbox(x):02x}")
```
```java
import java.util.Scanner;

public class Main {
    static int kopaytir(int a, int b) {                  // умножение в GF(2^8)
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;                    // сложение = XOR
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;            // по модулю x^8 + x^4 + x^3 + x + 1
            b >>= 1;
        }
        return r;
    }

    static int teskari(int a) {                          // a^254 = a^(-1), так как a^255 = 1
        if (a == 0) return 0;
        int r = 1;
        for (int i = 0; i < 254; i++) r = kopaytir(r, a);
        return r;
    }

    static int sbox(int x) {
        int b = teskari(x);
        int s = b;
        for (int i = 1; i <= 4; i++) {
            s ^= ((b << i) | (b >> (8 - i))) & 0xFF;     // циклический сдвиг влево
        }
        return s ^ 0x63;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int x = Integer.parseInt(t, 16);
            System.out.println(String.format("%s -> inv=%02x S=%02x", t, teskari(x), sbox(x)));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int kopaytir(int a, int b) {                             // умножение в GF(2^8)
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;                               // сложение = XOR
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;                       // по модулю x^8 + x^4 + x^3 + x + 1
        b >>= 1;
    }
    return r;
}

int teskari(int a) {                                     // a^254 = a^(-1), так как a^255 = 1
    if (a == 0) return 0;
    int r = 1;
    for (int i = 0; i < 254; i++) r = kopaytir(r, a);
    return r;
}

int sbox(int x) {
    int b = teskari(x);
    int s = b;
    for (int i = 1; i <= 4; i++) {
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF;         // циклический сдвиг влево
    }
    return s ^ 0x63;
}

int main() {
    string t;
    while (cin >> t) {
        int x = stoi(t, nullptr, 16);
        printf("%s -> inv=%02x S=%02x\n", t.c_str(), teskari(x), sbox(x));
    }
    return 0;
}
```

Ввод:

```text
00 01 53 c9 ff 10
```

Вывод:

```text
00 -> inv=00 S=63
01 -> inv=01 S=7c
53 -> inv=ca S=ed
c9 -> inv=27 S=dd
ff -> inv=1c S=16
10 -> inv=74 S=ca
```

Результаты совпадают с таблицей S-box в FIPS 197 (например, S(53) = ed — пример из стандарта). Проверьте вручную, что 0x53 · 0xca = 1. S-box — единственная **нелинейная** часть AES: благодаря обратному элементу между входом и выходом нет простой линейной связи — это основная защита от линейного и дифференциального криптоанализа. В практических программах S-box не вычисляют, а используют готовую 256-байтную таблицу.

## Разобранный пример 2: MixColumns

MixColumns умножает каждый столбец (4 байта) матрицы состояния в GF(2⁸) на следующую матрицу:

```text
02 03 01 01
01 02 03 01
01 01 02 03
03 01 01 02
```

В каждой строке — 4 hex-байта (столбец); выведите результирующий столбец.

```python
import sys

M = [[2, 3, 1, 1], [1, 2, 3, 1], [1, 1, 2, 3], [3, 1, 1, 2]]


def kopaytir(a, b):                       # умножение в GF(2^8)
    r = 0
    while b:
        if b & 1:
            r ^= a
        a <<= 1
        if a & 0x100:
            a ^= 0x11B
        b >>= 1
    return r


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    ustun = [int(t, 16) for t in satr.split()]
    natija = []
    for i in range(4):
        v = 0
        for j in range(4):
            v ^= kopaytir(M[i][j], ustun[j])   # сумма = XOR
        natija.append(v)
    print(satr.strip() + " -> " + " ".join(format(v, "02x") for v in natija))
```
```java
import java.util.Scanner;

public class Main {
    static final int[][] M = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

    static int kopaytir(int a, int b) {                  // умножение в GF(2^8)
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;
            b >>= 1;
        }
        return r;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] t = satr.split("\\s+");
            int[] ustun = new int[4];
            for (int j = 0; j < 4; j++) ustun[j] = Integer.parseInt(t[j], 16);
            StringBuilder sb = new StringBuilder(satr + " ->");
            for (int i = 0; i < 4; i++) {
                int v = 0;
                for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // сумма = XOR
                sb.append(String.format(" %02x", v));
            }
            System.out.println(sb);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const int M[4][4] = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

int kopaytir(int a, int b) {                             // умножение в GF(2^8)
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;
        b >>= 1;
    }
    return r;
}

int main() {
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        int ustun[4];
        string t;
        int n = 0;
        while (n < 4 && ss >> t) ustun[n++] = stoi(t, nullptr, 16);
        if (n < 4) continue;
        for (int j = 0; j < 4; j++) printf(j ? " %02x" : "%02x", ustun[j]);
        printf(" ->");
        for (int i = 0; i < 4; i++) {
            int v = 0;
            for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // сумма = XOR
            printf(" %02x", v);
        }
        printf("\n");
    }
    return 0;
}
```

Ввод:

```text
db 13 53 45
f2 0a 22 5c
01 01 01 01
c6 c6 c6 c6
d4 d4 d4 d5
2d 26 31 4c
```

Вывод:

```text
db 13 53 45 -> 8e 4d a1 bc
f2 0a 22 5c -> 9f dc 58 9d
01 01 01 01 -> 01 01 01 01
c6 c6 c6 c6 -> c6 c6 c6 c6
d4 d4 d4 d5 -> d5 d5 d7 d6
2d 26 31 4c -> 4d 7e bd f8
```

Это широко известные тестовые значения MixColumns. Столбец из одинаковых байтов не меняется (2 ⊕ 3 ⊕ 1 ⊕ 1 = 1), но в `d4 d4 d4 d5` изменение одного бита одного байта распространилось на **все** четыре байта столбца — это принцип **рассеивания** (diffusion) Шеннона. Вместе с ShiftRows через два раунда каждый входной байт влияет на все 16 байт состояния.

:::warning
**Частые ошибки**

- **Использовать пароль напрямую как ключ.** Получайте ключ из пароля только через PBKDF2, scrypt или Argon2 с солью.
- **Делать IV всегда нулевым или одинаковым.** В CBC файлы с одинаковым началом дают одинаковый шифртекст; в GCM повтор nonce полностью ломает защиту.
- **Режим ECB.** В библиотеках он часто «по умолчанию» — всегда указывайте режим явно.
- **Хранить ключ рядом с зашифрованным файлом.** Ключ — в другом месте: хранилище ключей, менеджер паролей, аппаратный модуль.
- **Только шифрование, без целостности.** CBC не защищает шифртекст от изменения; используйте GCM или шифрование + HMAC.
:::

:::tip
Флаг `-p` в OpenSSL (`openssl enc -aes-256-cbc -pbkdf2 -p ...`) показывает соль, ключ и IV, полученные из пароля, — запустите дважды и убедитесь, что соль каждый раз новая. Используйте этот флаг только на учебных файлах: он выводит ключ на экран.
:::

## Итоги

- AES: блок 128 бит, ключ 128/192/256 бит; на практике AES-GCM или AES-CBC + HMAC, случайный IV или неповторяющийся nonce.
- Правильность программы проверяется официальными тестовыми векторами (FIPS 197: `69c4e0d8…`).
- Ключ из пароля — через PBKDF2/scrypt/Argon2 с солью; сам пароль не является ключом.
- В GF(2⁸) сложение — XOR, умножение — по модулю 0x11B; S-box — обратный элемент + аффинное преобразование, нелинейная часть AES.
- MixColumns распространяет изменение одного байта на весь столбец — принцип рассеивания.

## Проверьте себя

:::check
Q: Почему при двукратном шифровании одного файла в OpenSSL одним паролем шифртексты различаются?
A: Каждый раз используется новая случайная соль, поэтому PBKDF2 получает другой ключ и IV.
Q: Что произойдёт при расшифровании, если изменить один байт шифртекста GCM?
A: Код аутентификации (tag) не совпадёт, и расшифрование будет отклонено с ошибкой.
Q: Какой шаг AES нелинейный и как он вычисляется?
A: SubBytes (S-box): обратный элемент в GF(2⁸) и затем аффинное преобразование.
Q: Какой криптографический принцип реализует MixColumns?
A: Рассеивание (diffusion): изменение одного входного байта влияет на все байты столбца.
:::

:::link https://csrc.nist.gov/pubs/fips/197/final
FIPS 197: Advanced Encryption Standard (AES)
Стандарт AES: таблица S-box, MixColumns и тестовые векторы (на английском языке).
:::
