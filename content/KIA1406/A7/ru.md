---
title: Применение алгоритма RSA на простом примере
summary: Применение RSA на практике — создание ключа, шифрование и расшифрование вручную на малых числах; программное задание (расширенный алгоритм Евклида, возведение в степень по модулю); 2048-битный ключ в OpenSSL, шифрование с OAEP и подпись с SHA-256; слабости «учебного» RSA — атака при малом e = 3, словарная атака на детерминированное шифрование, мультипликативность; почему дополнение (OAEP, PSS) обязательно.
---

## Почему эта тема важна?

На лекции M7 мы рассмотрели математику RSA: *n* = *p*·*q*, *e*·*d* ≡ 1 (mod φ(*n*)), *c* = *m*ᵉ mod *n*. На этом занятии вы выполните формулы вручную и в своей программе, а затем поработаете с реальным инструментом (OpenSSL). Главный урок: сами формулы — **«учебный RSA»** — небезопасны. В двух разобранных примерах ниже вы увидите, как RSA без дополнения вскрывается без знания ключа.

:::note
**После этого занятия вы сможете:**

- создать ключ RSA вручную на малых числах, зашифровать и расшифровать;
- реализовать шаги RSA в программе (задание A7 на платформе);
- создать ключ, шифровать с OAEP и подписывать в OpenSSL;
- показать слабости RSA без дополнения.
:::

## Практическая работа

**Шаг 1. RSA вручную.** Пусть *p* = 5, *q* = 11.

1. *n* = 5 · 11 = 55; φ(*n*) = 4 · 10 = 40.
2. *e* = 3: НОД(3, 40) = 1 — подходит.
3. Найдите *d*: 3 · *d* ≡ 1 (mod 40). Выполните расширенный алгоритм Евклида вручную или проверьте *d* = 1, 2, 3, … (ответ: *d* = 27, так как 3 · 27 = 81 = 2 · 40 + 1).
4. Зашифруйте *m* = 7: 7³ = 343; 343 mod 55 = 13 → *c* = 13.
5. Расшифрование: вычислите 13²⁷ mod 55 последовательным возведением в квадрат (13² = 169 ≡ 4, 13⁴ ≡ 16, 13⁸ ≡ 36, 13¹⁶ ≡ 31; 27 = 16 + 8 + 2 + 1) и убедитесь, что получается 7.

Самостоятельно: при *p* = 7, *q* = 13, *e* = 5 зашифруйте и расшифруйте *m* = 10.

**Шаг 2. Программа.** Выполните задание **RSA: генерация ключа, шифрование и расшифрование** на платформе: *n*, φ(*n*), *d* расширенным алгоритмом Евклида, *c* = *m*ᵉ mod *n* и проверка *c*ᵈ mod *n* = *m*. Следите, чтобы промежуточные произведения не переполнялись (`long` в Java, `long long` или `__int128` в C++).

**Шаг 3. Настоящий RSA (OpenSSL).** В Git Bash, Linux или macOS:

```text
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem
openssl pkeyutl -encrypt -pubin -inkey public.pem -pkeyopt rsa_padding_mode:oaep -in message.txt -out message.enc
openssl pkeyutl -decrypt -inkey private.pem -pkeyopt rsa_padding_mode:oaep -in message.enc
openssl dgst -sha256 -sign private.pem -out signature.bin document.txt
openssl dgst -sha256 -verify public.pem -signature signature.bin document.txt
```

Зашифруйте одно и то же сообщение дважды — так как OAEP добавляет случайное дополнение, файлы `message.enc` различаются. После подписи добавьте один символ в `document.txt` и повторите проверку: вместо «Verified OK» будет «Verification failure».

## Слабости «учебного RSA»

| Слабость | Суть | Защита |
|---|---|---|
| **Малое *e* и короткое сообщение** | если *m*ᵉ < *n*, mod не срабатывает: *m* = ∛*c* — обычный корень | дополнение OAEP расширяет *m* до размера *n* |
| **Детерминированность** | одно и то же *m* всегда даёт одно и то же *c* — сообщения с малым числом вариантов находятся по словарю | OAEP добавляет случайные байты |
| **Мультипликативность** | *c*₁ · *c*₂ mod *n* = (*m*₁ · *m*₂)ᵉ mod *n* — шифртекст можно осмысленно изменить | дополнение разрушает структуру |
| **Малые или близкие *p*, *q*** | разложить *n* легко (M7, пример 2) | *n* не менее 2048 бит, случайные простые |
| **Подпись без дополнения** | подделка подписи для нового сообщения перемножением подписей | хеш + дополнение PSS |

## Разобранный пример 1: e = 3 и короткое сообщение

В первой строке — модуль *n*, в следующих — шифртексты, зашифрованные с *e* = 3 без дополнения. Сообщение — число, составленное из байтов текста (например, «OK» = 0x4F4B = 20299). Если *m*³ < *n*, то *c* = *m*³ — обычный целый кубический корень даёт *m*. Для каждого *c* найдите целый кубический корень двоичным поиском; если это точный куб, выведите *m* и его текст, иначе — `не найдено`.

```python
import sys

satrlar = sys.stdin.read().split()
n = int(satrlar[0])                  # закрытый ключ не нужен — n только для справки
for t in satrlar[1:]:
    c = int(t)
    past, yuqori = 0, 2097151         # 2097151^3 < 2^63
    while past < yuqori:              # наибольшее r с r^3 <= c
        orta = (past + yuqori + 1) // 2
        if orta ** 3 <= c:
            past = orta
        else:
            yuqori = orta - 1
    if past ** 3 == c:
        matn = ""
        x = past
        while x > 0:
            matn = chr(x % 256) + matn    # байты числа — символы текста
            x //= 256
        print(f"{c} -> m={past} {matn}")
    else:
        print(f"{c} -> не найдено")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = in.nextLong();                     // закрытый ключ не нужен — n только для справки
        while (in.hasNextLong()) {
            long c = in.nextLong();
            long past = 0, yuqori = 2097151;        // 2097151^3 < 2^63
            while (past < yuqori) {                 // наибольшее r с r^3 <= c
                long orta = (past + yuqori + 1) / 2;
                if (orta * orta * orta <= c) past = orta;
                else yuqori = orta - 1;
            }
            if (past * past * past == c) {
                StringBuilder matn = new StringBuilder();
                for (long x = past; x > 0; x /= 256) {
                    matn.insert(0, (char) (x % 256));   // байты числа — символы текста
                }
                System.out.println(c + " -> m=" + past + " " + matn);
            } else {
                System.out.println(c + " -> не найдено");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    long long n, c;
    cin >> n;                                       // закрытый ключ не нужен — n только для справки
    while (cin >> c) {
        long long past = 0, yuqori = 2097151;       // 2097151^3 < 2^63
        while (past < yuqori) {                     // наибольшее r с r^3 <= c
            long long orta = (past + yuqori + 1) / 2;
            if (orta * orta * orta <= c) past = orta;
            else yuqori = orta - 1;
        }
        if (past * past * past == c) {
            string matn;
            for (long long x = past; x > 0; x /= 256) {
                matn = char(x % 256) + matn;        // байты числа — символы текста
            }
            cout << c << " -> m=" << past << " " << matn << endl;
        } else {
            cout << c << " -> не найдено" << endl;
        }
    }
    return 0;
}
```

Ввод:

```text
1000000104000000679
8364190790899
6328545249473
615573431271999514
```

Вывод:

```text
8364190790899 -> m=20299 OK
6328545249473 -> m=18497 HA
615573431271999514 -> не найдено
```

Первые два сообщения вскрыты без закрытого ключа, одним кубическим корнем: для «OK» и «HA» *m*³ < *n*, то есть приведения по модулю вообще не было. Третье сообщение («SIR» = 5458258) длиннее: *m*³ > *n*, приведение произошло, и обычный корень не работает. В настоящем RSA OAEP превращает любое сообщение в случайное на вид число длины *n* — эта атака становится невозможной.

## Разобранный пример 2: словарная атака на детерминированный RSA

Преподаватель отправляет студентам оценки (2, 3, 4, 5), зашифрованные открытым ключом без дополнения. Злоумышленник знает открытый ключ — он сам шифрует каждую возможную оценку и сравнивает с перехваченными шифртекстами. Ввод: 1-я строка *n* *e*; 2-я — возможные сообщения; 3-я — перехваченные шифртексты. Для каждого шифртекста выведите соответствующее сообщение или `?`.

```python
import sys

satrlar = sys.stdin.read().split("\n")
n, e = map(int, satrlar[0].split())
lugat = {}
for m in satrlar[1].split():
    lugat[pow(int(m), e, n)] = m          # шифруем каждый вариант открытым ключом
for c in satrlar[2].split():
    print(f"{c} -> {lugat.get(int(c), '?')}")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static long darajaMod(long a, long k, long n) {   // a^k mod n (n < 2^31 — произведение помещается в long)
        long natija = 1;
        a %= n;
        while (k > 0) {
            if ((k & 1) == 1) natija = natija * a % n;
            a = a * a % n;
            k >>= 1;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] kalit = in.nextLine().trim().split("\\s+");
        long n = Long.parseLong(kalit[0]);
        long e = Long.parseLong(kalit[1]);
        Map<Long, String> lugat = new HashMap<>();
        for (String m : in.nextLine().trim().split("\\s+")) {
            lugat.put(darajaMod(Long.parseLong(m), e, n), m);   // шифруем каждый вариант открытым ключом
        }
        for (String c : in.nextLine().trim().split("\\s+")) {
            System.out.println(c + " -> " + lugat.getOrDefault(Long.parseLong(c), "?"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
using namespace std;

long long darajaMod(long long a, long long k, long long n) {   // a^k mod n (n < 2^31 — произведение помещается в long long)
    long long natija = 1;
    a %= n;
    while (k > 0) {
        if (k & 1) natija = natija * a % n;
        a = a * a % n;
        k >>= 1;
    }
    return natija;
}

int main() {
    string satr;
    getline(cin, satr);
    long long n, e;
    istringstream(satr) >> n >> e;
    map<long long, string> lugat;
    getline(cin, satr);
    istringstream variantlar(satr);
    string m;
    while (variantlar >> m) {
        lugat[darajaMod(stoll(m), e, n)] = m;      // шифруем каждый вариант открытым ключом
    }
    getline(cin, satr);
    istringstream ushlangan(satr);
    string c;
    while (ushlangan >> c) {
        auto it = lugat.find(stoll(c));
        cout << c << " -> " << (it != lugat.end() ? it->second : "?") << endl;
    }
    return 0;
}
```

Ввод:

```text
100160063 65537
2 3 4 5
1727207 24561622 82696754 1727207 11111
```

Вывод:

```text
1727207 -> 4
24561622 -> 5
82696754 -> 2
1727207 -> 4
11111 -> ?
```

Злоумышленник прочитал все оценки, не зная ни закрытого ключа, ни множителей *n*, — потому что пространство сообщений состоит всего из 4 элементов. Первый и четвёртый шифртексты одинаковы — значит, и оценки одинаковы; это тоже утечка информации. Длина ключа здесь не помогает: с 4096-битным *n* результат тот же. Решение — **случайное дополнение** (OAEP): одна и та же оценка каждый раз даёт другой шифртекст.

:::warning
**Частые ошибки**

- **Переполнение промежуточного произведения.** В *a* · *b* mod *n* произведение *a* · *b* может превысить 64 бита; берите mod на каждом шаге и выбирайте достаточно широкий тип.
- **Вычислять mᵉ целиком и только потом брать mod.** Число становится огромным; используйте последовательное возведение в квадрат (square-and-multiply).
- **e с НОД(e, φ(n)) ≠ 1.** Для такого *e* не существует *d*.
- **Использовать «учебный RSA» для реальных данных.** Для шифрования — только OAEP, для подписи — PSS; на практике RSA обычно применяют только для передачи симметричного ключа и для подписи.
- **Оставлять файл закрытого ключа без защиты.** Зашифруйте `private.pem` паролем (`-aes256`) и никому его не отправляйте.
:::

:::tip
Команда `openssl pkey -in private.pem -text -noout` показывает состав ключа: modulus, publicExponent (65537), privateExponent, prime1, prime2, а также для ускорения exponent1, exponent2 и coefficient (параметры китайской теоремы об остатках). Выполняйте её только на учебном ключе.
:::

## Итоги

- Шаги RSA: *n* = *pq*, φ(*n*), *e* (НОД = 1), *d* = *e*⁻¹ mod φ(*n*), *c* = *m*ᵉ mod *n*, *m* = *c*ᵈ mod *n*.
- Вычисления: расширенный алгоритм Евклида и последовательное возведение в квадрат; избегайте переполнения.
- OpenSSL: `genpkey`, `pkeyutl` (OAEP) и `dgst -sign/-verify` (подпись SHA-256).
- RSA без дополнения слаб: кубический корень при малом *e*, словарная атака из-за детерминированности, мультипликативность.
- На практике: ключ не менее 2048 бит, OAEP для шифрования, PSS для подписи.

## Проверьте себя

:::check
Q: Чему равно d при p = 5, q = 11, e = 3?
A: 27, так как φ(n) = 40 и 3 · 27 = 81 ≡ 1 (mod 40).
Q: Почему при e = 3 и коротком сообщении шифртекст можно вскрыть без ключа?
A: Если m³ < n, приведения по модулю нет и c = m³ — обычный целый кубический корень даёт m.
Q: Какую атаку допускает детерминированное шифрование?
A: Словарную: злоумышленник шифрует открытым ключом все вероятные сообщения и сравнивает шифртексты.
Q: Какую защиту даёт OAEP?
A: Добавляет случайное дополнение: одно и то же сообщение каждый раз даёт другой шифртекст, а сообщение расширяется до длины n.
:::

:::link https://www.rfc-editor.org/rfc/rfc8017
RFC 8017: PKCS #1 — RSA Cryptography Specifications
Спецификация RSA, шифрования OAEP и стандарта подписи PSS (на английском языке).
:::
