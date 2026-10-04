---
title: Строки типа String
summary: string в C++, String в Java и str в Python: длина, конкатенация, подстрока, поиск, замена, сравнение, регистр, разбиение на слова; неизменяемые строки и StringBuilder.
---

## Почему это важно

Проверка фамилии и имени, правила пароля, поиск `@` в адресе электронной почты, разбор столбцов CSV-файла, подсчёт слов в тексте, поисковая система — большая часть программ работает с текстом. Со строками в стиле C из занятия M10 это делать трудно и опасно: нужно вручную следить за длиной, думать о `'\0'`, опасаться выхода за границы.

В современных языках для этого есть **класс строки**: `string` в C++, `String` в Java, `str` в Python. Они сами знают свою длину, сами управляют памятью и имеют готовые операции поиска, вырезания, замены.

:::note
**После этого занятия вы сможете:**

- создавать строку, соединять строки, получать длину и отдельные символы;
- выделять подстроку, искать и заменять внутри строки;
- сравнивать строки и переводить их в верхний или нижний регистр;
- разбивать текст на слова и работать с ними;
- объяснять, почему строки в Java и Python неизменяемы и зачем при многократном соединении нужны `StringBuilder` / `join`.
:::

## Основные операции

| Операция | Python (`str`) | Java (`String`) | C++ (`string`) |
|---|---|---|---|
| Создание | `s = "Salom"` | `String s = "Salom";` | `string s = "Salom";` |
| Длина | `len(s)` | `s.length()` | `s.size()` или `s.length()` |
| Символ *i* | `s[i]` | `s.charAt(i)` | `s[i]` |
| Конкатенация | `s + t` | `s + t` | `s + t` |
| Подстрока (с *i*, *k* символов) | `s[i:i + k]` | `s.substring(i, i + k)` | `s.substr(i, k)` |
| Поиск (первая позиция) | `s.find(t)` → −1 | `s.indexOf(t)` → −1 | `s.find(t)` → `string::npos` |
| Равенство | `s == t` | `s.equals(t)` | `s == t` |
| Лексикографическое сравнение | `s < t` | `s.compareTo(t) < 0` | `s < t` |
| В верхний регистр | `s.upper()` | `s.toUpperCase()` | `toupper` для каждого символа |
| Замена (всех) | `s.replace(a, b)` | `s.replace(a, b)` | циклом `find` + `replace` |

Обратите внимание: для подстроки Python и Java берут **конечный индекс** (он не входит), а C++ — **длину**: `substr(2, 3)` — 3 символа начиная с индекса 2.

Срезы (`slice`) в Python — мощный инструмент: `s[:3]` — первые 3 символа, `s[-3:]` — последние 3, `s[::-1]` — перевёрнутая строка.

## Сравнение строк в Java

В Java `==` сравнивает не **содержимое** строк, а **ссылки**: для одинакового текста в двух разных объектах `==` может дать ложь. Для сравнения содержимого всегда используйте `s.equals(t)` (без учёта регистра — `s.equalsIgnoreCase(t)`). Это одна из самых известных ошибок в Java.

В C++ и Python `==` сравнивает содержимое.

## Неизменяемые строки

В Java и Python строка **неизменяема (immutable)**: после создания её символы нельзя изменить. `s.upper()`, `s.replace(...)` не меняют исходную строку — они возвращают **новую**. `s[0] = 'X'` в Python вызывает ошибку.

Поэтому многократное `s = s + x` в цикле работает медленно: каждый раз создаётся новая строка и копируется старая. Решение:

| Язык | Сборка строки из многих частей |
|---|---|
| Python | собрать части в список и в конце `"".join(qismlar)` |
| Java | `StringBuilder sb = new StringBuilder(); sb.append(x);` … `sb.toString()` |
| C++ | `string` изменяемый: `s += x` работает быстро |

`string` в C++ изменяемый: `s[0] = 'X'` допустимо.

## Разбиение на слова

Разбить текст на слова по пробелам:

| Язык | Способ |
|---|---|
| Python | `sozlar = s.split()` — корректно и при нескольких пробелах подряд |
| Java | `String[] w = s.trim().split("\\s+");` — регулярное выражение: один или несколько пробелов |
| C++ | `istringstream ss(s);` и `while (ss >> w)` — `<sstream>` |

В Java `split` пустой строки даёт массив из одного пустого элемента — проверяйте этот случай отдельно.

## Разобранный пример 1: палиндром

Вводится строка текста. Определите, является ли она палиндромом (читается одинаково слева направо и справа налево), учитывая только буквы и без учёта регистра.

```python
s = input()

harflar = ""
for c in s:
    if c.isalpha():
        harflar += c.lower()

print("да" if harflar == harflar[::-1] else "нет")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        String harflar = sb.toString();
        String teskari = sb.reverse().toString();

        System.out.println(harflar.equals(teskari) ? "да" : "нет");
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <cctype>
using namespace std;

int main() {
    string s;
    getline(cin, s);

    string harflar;
    for (char c : s) {
        if (isalpha((unsigned char) c)) {
            harflar += (char) tolower((unsigned char) c);
        }
    }
    string teskari(harflar.rbegin(), harflar.rend());

    cout << (harflar == teskari ? "да" : "нет") << endl;
    return 0;
}
```

При вводе `A man, a plan, a canal: Panama`: буквы (*harflar*) `amanaplanacanalpanama` — перевёрнутая строка (*teskari*) такая же. Выводится: `да`. При вводе `Salom`: `salom` ≠ `molas`, выводится `нет`.

В Java `sb.reverse()` переворачивает сам `StringBuilder`, поэтому `harflar` получена **до** этого. В C++ `string teskari(harflar.rbegin(), harflar.rend())` создаёт новую строку, читая от конца к началу. Сравнение в Java — через `equals`.

## Разобранный пример 2: статистика слов

Вводится строка текста (слова разделены одним или несколькими пробелами). Выведите количество слов, самое длинное слово (если таких несколько — первое) и каждое слово с заглавной буквы.

```python
sozlar = input().split()

eng_uzun = ""
for w in sozlar:
    if len(w) > len(eng_uzun):
        eng_uzun = w

bosh_harfli = []
for w in sozlar:
    bosh_harfli.append(w[0].upper() + w[1:].lower())

print(len(sozlar))
print(eng_uzun)
print(" ".join(bosh_harfli))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] sozlar = in.nextLine().trim().split("\\s+");

        String engUzun = "";
        for (String w : sozlar) {
            if (w.length() > engUzun.length()) {
                engUzun = w;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String w : sozlar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0)));
            sb.append(w.substring(1).toLowerCase());
        }

        System.out.println(sozlar.length);
        System.out.println(engUzun);
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <cctype>
using namespace std;

int main() {
    string qator;
    getline(cin, qator);

    vector<string> sozlar;
    istringstream ss(qator);
    string w;
    while (ss >> w) {
        sozlar.push_back(w);
    }

    string engUzun;
    for (const string& s : sozlar) {
        if (s.size() > engUzun.size()) {
            engUzun = s;
        }
    }

    cout << sozlar.size() << endl;
    cout << engUzun << endl;
    for (size_t i = 0; i < sozlar.size(); i++) {
        string s = sozlar[i];
        s[0] = (char) toupper((unsigned char) s[0]);
        for (size_t j = 1; j < s.size(); j++) {
            s[j] = (char) tolower((unsigned char) s[j]);
        }
        if (i > 0) cout << ' ';
        cout << s;
    }
    cout << endl;
    return 0;
}
```

При вводе `  dasturlash   tili  PYTHON va java ` (*sozlar* — слова, *eng_uzun* — самое длинное):

1. Слова: `dasturlash`, `tili`, `PYTHON`, `va`, `java` — 5; лишние пробелы не учитываются.
2. Самое длинное: `dasturlash` (10 букв).
3. С заглавной буквы: `Dasturlash Tili Python Va Java`.

В C++ `istringstream` позволяет читать строку так же, как `cin`: `ss >> w` берёт следующее слово и пропускает пробелы.

## Разобранный пример 3: поиск и замена

Вводятся три строки: текст, искомая подстрока и строка, которую нужно поставить вместо неё. Посчитайте, сколько раз (без перекрытий) встречается подстрока, и выведите текст, где заменены все вхождения.

```python
matn = input()
eski = input()
yangi = input()

soni = matn.count(eski)
print(soni)
print(matn.replace(eski, yangi))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String matn = in.nextLine();
        String eski = in.nextLine();
        String yangi = in.nextLine();

        int soni = 0;
        int i = matn.indexOf(eski);
        while (i != -1) {
            soni++;
            i = matn.indexOf(eski, i + eski.length());
        }

        System.out.println(soni);
        System.out.println(matn.replace(eski, yangi));
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string matn, eski, yangi;
    getline(cin, matn);
    getline(cin, eski);
    getline(cin, yangi);

    int soni = 0;
    size_t i = matn.find(eski);
    while (i != string::npos) {
        soni++;
        matn.replace(i, eski.size(), yangi);     // заменяем на месте
        i = matn.find(eski, i + yangi.size());   // после вставленной части
    }

    cout << soni << endl;
    cout << matn << endl;
    return 0;
}
```

Ввод (*matn* — текст, *eski* — старое, *yangi* — новое):

```text
Java va Python, Python va C++
Python
Go
```

Выводится: `2` и `Java va Go, Go va C++`.

В версии на Java поиск продолжается через `indexOf(eski, начало)`: после каждого найденного вхождения поиск начинается с места, где оно закончилось. Версия на C++ сразу заменяет найденное через `replace` и продолжает поиск после **вставленной** подстроки — иначе, например, при замене «a» на «aa» можно попасть в бесконечный цикл. В Python `count` и `replace` готовы.

:::warning
**Частые ошибки**

- **Сравнение строк через `==` в Java.** Для содержимого — `equals`.
- **Путаница `substr` и `substring`.** В C++ второй аргумент — длина, в Java — конечный индекс.
- **Результат не сохранён.** В Python и Java `s.upper()` возвращает новую строку; нужно писать `s = s.upper()`.
- **Сравнение результата `find` с `-1` в C++.** Правильно — `string::npos`.
- **`s = s + x` в цикле (Java).** На большом тексте очень медленно; используйте `StringBuilder`.
- **`w[0]` у пустой строки.** У пустой строки нет нулевого символа; сначала проверьте длину.
:::

:::tip
Проверяйте каждую функцию, работающую со строками, на крайних случаях: пустая строка, один символ, только пробелы, искомое в начале и в конце. Почти все текстовые ошибки — там.
:::

## Итоги

- `string` (C++), `String` (Java), `str` (Python) сами управляют длиной и памятью.
- Основные операции: длина, символ, конкатенация, подстрока, поиск, замена, сравнение.
- Подстрока: в Python и Java — конечный индекс, в C++ — длина; не найдено: −1 или `string::npos`.
- В Java содержимое сравнивают через `equals`.
- Строки в Java и Python неизменяемы: методы возвращают новую строку; многократная сборка — `StringBuilder` / `join`.
- Разбиение на слова: `split()`, `split("\\s+")`, `istringstream`.

## Проверьте себя

:::check
Q: Что дают `s[3:7]` в Python и `s.substr(3, 4)` в C++ при `s = "Dasturlash"`?
A: Оба — `turl`: 4 символа начиная с индекса 3.
Q: Чему равно в Java `new String("ab") == "ab"` и почему?
A: `false`: `==` сравнивает ссылки, а это два разных объекта. А `"ab".equals(new String("ab"))` — `true`.
Q: Что выведет Python: `s = "salom"; s.upper(); print(s)`?
A: `salom`: строка неизменяема, `upper()` возвращает новую строку, но она не сохранена.
Q: Почему в Java вместо `s = s + x` в цикле рекомендуют `StringBuilder`?
A: Каждый `+` создаёт новую строку и полностью копирует старую; *n* соединений требуют ~*n*² работы. `StringBuilder` работает с одним изменяемым буфером.
Q: На сколько слов разбивает `split()` в Python строку `"  bir  ikki "`?
A: На 2: `["bir", "ikki"]`.
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: методы строк
`split`, `join`, `find`, `replace`, `upper` и другие (на английском).
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
cppreference: std::string
Все методы класса `string` в C++ (на английском).
:::

:::link https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
Java 21: класс String
Официальная документация методов `String` (на английском).
:::
