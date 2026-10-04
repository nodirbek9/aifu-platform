---
title: Функции для работы со строками
summary: Стандартные функции строк на практике — удаление пробелов, проверка начала и конца, поиск и выделение части, подсчёт, объединение, построители строк (StringBuilder); разбор адреса электронной почты, сжатие строки (RLE) и перенос текста по строкам.
---

## Почему это важно

Почти всё, что вводит пользователь, — имя, телефон, адрес, пароль, сообщение — приходит в виде строки. Проверить её, очистить и разобрать на нужные части — повседневная работа любой программы. В первом семестре мы познакомились с основными операциями над строками; теперь используем их **вместе** для решения реальных задач.

На этом занятии дополним таблицу строковых функций трёх языков и применим их в трёх практических задачах: проверка адреса электронной почты, сжатие строки и перенос текста по строкам заданной ширины.

:::note
**После этого занятия вы сможете:**

- выбирать и использовать основные строковые функции на трёх языках;
- разбивать строку на части и проверять каждую часть;
- использовать `StringBuilder` и `join` для эффективного построения строк;
- разбивать строковую задачу на последовательность простых функций.
:::

## Таблица строковых функций

| Задача | Python | Java | C++ (`std::string`) |
|---|---|---|---|
| длина | `len(s)` | `s.length()` | `s.size()` |
| убрать пробелы в начале/конце | `s.strip()` | `s.trim()` / `s.strip()` | вручную: `find_first_not_of(' ')` и `find_last_not_of(' ')` |
| начинается / заканчивается ли на … | `s.startswith(p)`, `s.endswith(p)` | `s.startsWith(p)`, `s.endsWith(p)` | `s.rfind(p, 0) == 0`; в C++20 — `s.starts_with(p)` |
| найти подстроку | `s.find(p)` (нет — `-1`) | `s.indexOf(p)` (нет — `-1`) | `s.find(p)` (нет — `string::npos`) |
| последнее вхождение | `s.rfind(p)` | `s.lastIndexOf(p)` | `s.rfind(p)` |
| подстрока | `s[i:j]` | `s.substring(i, j)` | `s.substr(i, длина)` |
| сколько раз встречается символ | `s.count(c)` | цикл или `s.chars().filter(...).count()` | `count(s.begin(), s.end(), c)` |
| в нижний регистр | `s.lower()` | `s.toLowerCase()` | цикл с `tolower` |
| разбить | `s.split(разделитель)` | `s.split(regex)` | `istringstream` или `find` + `substr` |
| объединить | `разделитель.join(список)` | `String.join(разделитель, список)` | цикл с `+=` |

В C++ второй аргумент `substr` — **длина**, а в Python и Java — **конечный индекс** (не включается). Эта разница — источник многих ошибок.

## Эффективное построение строки

В Java и Python строка **неизменяема**: `s = s + c` каждый раз создаёт новую строку и копирует старую. *n* добавлений в цикле занимают *O*(*n*²) времени. Правильный способ:

| Язык | Эффективное построение |
|---|---|
| Python | добавлять части в список через `append`, в конце `"".join(части)` |
| Java | `StringBuilder sb`; `sb.append(...)`; в конце `sb.toString()` |
| C++ | `string` изменяема: `s += c` в среднем *O*(1) |

## Разобранный пример 1: проверка адреса электронной почты

В первой строке *n*, в следующих *n* строках — адреса электронной почты (в начале и конце могут быть пробелы). Для каждого адреса: если в нём ровно один `@`, часть до `@` (логин) не пуста, а часть после (домен) не начинается с точки и содержит хотя бы одну точку, — выведите логин и домен в нижнем регистре; иначе `Ошибка`.

```python
def tekshir(manzil):
    manzil = manzil.strip().lower()
    if manzil.count("@") != 1:
        return None
    login, domen = manzil.split("@")
    if login == "" or domen.startswith(".") or "." not in domen:
        return None
    return login, domen


n = int(input())
for _ in range(n):
    natija = tekshir(input())
    print(*natija if natija else ["Ошибка"])
```
```java
import java.util.Scanner;

public class Main {
    static String tekshir(String manzil) {
        manzil = manzil.trim().toLowerCase();
        int belgi = manzil.indexOf('@');
        if (belgi == -1 || belgi != manzil.lastIndexOf('@')) return null;   // ровно один @
        String login = manzil.substring(0, belgi);
        String domen = manzil.substring(belgi + 1);
        if (login.isEmpty() || domen.startsWith(".") || !domen.contains(".")) return null;
        return login + " " + domen;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String natija = tekshir(in.nextLine());
            System.out.println(natija != null ? natija : "Ошибка");
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

string tozala(const string& s) {                        // strip + lower
    size_t bosh = s.find_first_not_of(' ');
    if (bosh == string::npos) return "";
    size_t oxir = s.find_last_not_of(' ');
    string natija = s.substr(bosh, oxir - bosh + 1);
    for (char& c : natija) c = tolower(c);
    return natija;
}

string tekshir(const string& kirish) {
    string manzil = tozala(kirish);
    if (count(manzil.begin(), manzil.end(), '@') != 1) return "";   // ровно один @
    size_t belgi = manzil.find('@');
    string login = manzil.substr(0, belgi);
    string domen = manzil.substr(belgi + 1);
    if (login.empty() || domen.empty() || domen[0] == '.' || domen.find('.') == string::npos) return "";
    return login + " " + domen;
}

int main() {
    int n;
    cin >> n;
    string satr;
    getline(cin, satr);                                 // конец строки после n
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        string natija = tekshir(satr);
        cout << (natija.empty() ? "Ошибка" : natija) << endl;
    }
    return 0;
}
```

Ввод:

```text
4
  Aziz.Karimov@AIFU.uz
malika@@mail.uz
@aifu.uz
bekzod@localhost
```

Вывод:

```text
aziz.karimov aifu.uz
Ошибка
Ошибка
Ошибка
```

Каждое условие проверено отдельной простой функцией: `strip`, `lower`, `count`, `split`, `startswith` (*tekshir* — проверить, *manzil* — адрес, *domen* — домен). В Java «ровно один `@`» проверяется тем, что `indexOf` и `lastIndexOf` указывают на одно место. В C++ перед `getline` дочитывается конец строки, оставшийся после `n`, — иначе первый адрес окажется пустой строкой. Настоящие правила адресов гораздо сложнее, но принцип разбора тот же.

## Разобранный пример 2: сжатие строки (RLE)

Строка состоит из строчных латинских букв. Сожмите её методом **RLE** (run-length encoding): каждая группа одинаковых подряд идущих букв заменяется на «буква + количество» (`aaabcc` → `a3b1c2`). Выведите сжатую строку и совпадает ли её распаковка с исходной (`да`/`нет`).

```python
def siq(s):
    qismlar = []
    i = 0
    while i < len(s):
        j = i
        while j < len(s) and s[j] == s[i]:
            j += 1
        qismlar.append(s[i] + str(j - i))        # буква и длина группы
        i = j
    return "".join(qismlar)


def och(s):
    qismlar = []
    i = 0
    while i < len(s):
        harf = s[i]
        j = i + 1
        while j < len(s) and s[j].isdigit():
            j += 1
        qismlar.append(harf * int(s[i + 1:j]))
        i = j
    return "".join(qismlar)


satr = input().strip()
siqilgan = siq(satr)
print(siqilgan)
print("да" if och(siqilgan) == satr else "нет")
```
```java
import java.util.Scanner;

public class Main {
    static String siq(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int j = i;
            while (j < s.length() && s.charAt(j) == s.charAt(i)) j++;
            sb.append(s.charAt(i)).append(j - i);         // буква и длина группы
            i = j;
        }
        return sb.toString();
    }

    static String och(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char harf = s.charAt(i);
            int j = i + 1;
            while (j < s.length() && Character.isDigit(s.charAt(j))) j++;
            sb.append(String.valueOf(harf).repeat(Integer.parseInt(s.substring(i + 1, j))));
            i = j;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine().trim();
        String siqilgan = siq(satr);
        System.out.println(siqilgan);
        System.out.println(och(siqilgan).equals(satr) ? "да" : "нет");
    }
}
```
```cpp
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

string siq(const string& s) {
    string natija;
    size_t i = 0;
    while (i < s.size()) {
        size_t j = i;
        while (j < s.size() && s[j] == s[i]) j++;
        natija += s[i];
        natija += to_string(j - i);                     // буква и длина группы
        i = j;
    }
    return natija;
}

string och(const string& s) {
    string natija;
    size_t i = 0;
    while (i < s.size()) {
        char harf = s[i];
        size_t j = i + 1;
        while (j < s.size() && isdigit(s[j])) j++;
        natija += string(stoi(s.substr(i + 1, j - i - 1)), harf);
        i = j;
    }
    return natija;
}

int main() {
    string satr;
    cin >> satr;
    string siqilgan = siq(satr);
    cout << siqilgan << endl;
    cout << (och(siqilgan) == satr ? "да" : "нет") << endl;
    return 0;
}
```

При вводе `aaabccddddddddddddx` выводится `a3b1c2d12x1` и `да`. При распаковке количество может состоять из нескольких цифр (`d12`), поэтому после буквы читаем **до первого нецифрового символа** (*siq* — сжать, *och* — распаковать). Помните, что в `s.substr(i + 1, j - i - 1)` на C++ второй аргумент — длина. RLE применяется в форматах изображений (BMP, TIFF) и при передаче факсов: длинные полосы одного цвета сжимаются очень хорошо, а строка без повторов вроде `abc`, наоборот, удлиняется (`a1b1c1`).

## Разобранный пример 3: перенос текста по строкам

В первой строке ширина *W*, во второй — текст. Расположите слова, сохраняя их порядок, по строкам длиной не больше *W*: если слово помещается в текущую строку (между словами один пробел), оно добавляется в неё, иначе начинается новая строка. Ни одно слово не длиннее *W*. Выведите каждую строку между символами `|`, дополнив пробелами до *W*.

```python
w = int(input())
sozlar = input().split()

qatorlar = []
joriy = []
uzunlik = 0
for soz in sozlar:
    kerak = len(soz) if not joriy else uzunlik + 1 + len(soz)
    if kerak <= w:
        joriy.append(soz)
        uzunlik = kerak
    else:
        qatorlar.append(" ".join(joriy))
        joriy = [soz]
        uzunlik = len(soz)
qatorlar.append(" ".join(joriy))

for q in qatorlar:
    print("|" + q.ljust(w) + "|")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int w = Integer.parseInt(in.nextLine().trim());
        String[] sozlar = in.nextLine().trim().split("\\s+");

        List<String> qatorlar = new ArrayList<>();
        StringBuilder joriy = new StringBuilder();
        for (String soz : sozlar) {
            int kerak = joriy.length() == 0 ? soz.length() : joriy.length() + 1 + soz.length();
            if (kerak <= w) {
                if (joriy.length() > 0) joriy.append(' ');
                joriy.append(soz);
            } else {
                qatorlar.add(joriy.toString());
                joriy = new StringBuilder(soz);
            }
        }
        qatorlar.add(joriy.toString());

        for (String q : qatorlar) {
            System.out.println("|" + q + " ".repeat(w - q.length()) + "|");
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int w;
    cin >> w;
    string satr;
    getline(cin, satr);                                 // конец строки после w
    getline(cin, satr);
    istringstream oqim(satr);

    vector<string> qatorlar;
    string joriy, soz;
    while (oqim >> soz) {
        size_t kerak = joriy.empty() ? soz.size() : joriy.size() + 1 + soz.size();
        if ((int) kerak <= w) {
            if (!joriy.empty()) joriy += ' ';
            joriy += soz;
        } else {
            qatorlar.push_back(joriy);
            joriy = soz;
        }
    }
    qatorlar.push_back(joriy);

    for (const string& q : qatorlar) {
        cout << "|" << q << string(w - q.size(), ' ') << "|" << endl;
    }
    return 0;
}
```

Ввод:

```text
16
Dasturlash har kuni ozgina mashq qilishni talab qiladi
```

Вывод:

```text
|Dasturlash har  |
|kuni ozgina     |
|mashq qilishni  |
|talab qiladi    |
```

Для каждого слова вычисляется «какой длины станет строка, если его добавить?» (`kerak` — нужно): перед первым словом пробел не нужен, перед остальными — один. Это **жадный** (greedy) метод: в каждую строку помещается как можно больше слов. Текстовые редакторы, терминалы и разбиение SMS используют именно этот алгоритм (*qatorlar* — строки, *joriy* — текущая).

:::warning
**Частые ошибки**

- **Второй аргумент `substr` в C++ принят за индекс.** `s.substr(i, j)` — это *j* символов начиная с *i*, а не до индекса *j*.
- **Неверная проверка результата `find`.** В C++ при неудаче возвращается `string::npos`, а не `-1` или `0`; в Python `find` возвращает `-1`, а `index` выбрасывает ошибку.
- **`getline` после `cin >>`.** Остаётся конец строки, и `getline` читает пустую строку; дочитайте его одним `getline` между ними.
- **Построение строки через `+` в цикле (Java, Python).** *O*(*n*²); используйте `StringBuilder` или `join`.
:::

:::tip
Прежде чем решать строковую задачу, составьте список крайних случаев: пустая строка, один символ, только пробелы, пробелы в начале и конце, одинаковые символы. Запишите для каждого ожидаемый результат и проверьте программу на них.
:::

## Итоги

- Строковые функции в трёх языках решают почти одни и те же задачи, но различаются названиями и поведением в крайних случаях.
- В C++ — `substr(i, длина)`, в Python — `s[i:j]`, в Java — `substring(i, j)` с конечным индексом.
- В Java и Python стройте строку в цикле через `StringBuilder` или `join`.
- Разбиение строковой задачи на маленькие функции (очистить, проверить, разобрать, собрать) её упрощает.
- Используя `getline` после `cin >>`, не забывайте про оставшийся конец строки.

## Проверьте себя

:::check
Q: Что возвращает в Python `"  salom  ".strip()`?
A: `"salom"` — пробелы в начале и конце удалены.
Q: Что возвращает в C++ `string("dasturlash").substr(3, 4)`?
A: `"turl"` — 4 символа начиная с индекса 3.
Q: Что означает в Java `"a@b@c".indexOf('@') != "a@b@c".lastIndexOf('@')`?
A: `@` встречается в строке не менее двух раз.
Q: Как сожмётся строка `aabbbc` методом RLE?
A: `a2b3c1`.
Q: Почему в Java `s = s + c` внутри цикла работает медленно?
A: Строки неизменяемы: каждое добавление создаёт новую строку и копирует старую — всего *O*(*n*²).
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: методы строк
Полный список методов: `strip`, `split`, `join`, `find`, `count` и другие (на английском).
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
C++: std::string
`find`, `substr`, `rfind` и другие функции-члены (на английском).
:::
