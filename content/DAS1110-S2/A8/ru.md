---
title: Общее понятие о текстовых файлах. Файлы и потоки
summary: Понятие потока — стандартные потоки ввода, вывода и ошибок, файловые потоки; чтение потока до конца (EOF), построчная обработка, запись в файл и повторное чтение, буферизация; фильтрация файла журнала и нумерация строк.
---

## Почему это важно

Программа может получать данные с клавиатуры, из файла, из сети или от другой программы. Для всего этого языки используют одно общее понятие — **поток** (stream): «трубу», по которой байты или символы приходят друг за другом. Для программиста, освоившего потоки, чтение с клавиатуры почти не отличается от чтения из файла.

В первом семестре мы научились открывать, записывать и читать файл. На этом практическом занятии отработаем практические приёмы работы с потоками: чтение потока до конца, когда объём данных заранее неизвестен, фильтрацию данных в файл и повторное чтение.

:::note
**После этого занятия вы сможете:**

- различать стандартные потоки ввода, вывода и ошибок и файловые потоки;
- читать поток до конца (EOF) на трёх языках;
- обрабатывать большой файл построчно, не загружая его целиком в память;
- записывать результат в файл и читать его обратно;
- объяснять, что такое буферизация и почему важно закрывать файл.
:::

## Потоки

| Поток | Назначение | Python | Java | C++ |
|---|---|---|---|---|
| Стандартный ввод | клавиатура или перенаправленный файл | `sys.stdin`, `input()` | `System.in` | `cin` |
| Стандартный вывод | экран | `sys.stdout`, `print()` | `System.out` | `cout` |
| Поток ошибок | сообщения, отладочная информация | `sys.stderr` | `System.err` | `cerr` |
| Чтение файла | | `open(имя)` | `BufferedReader`, `Files.newBufferedReader` | `ifstream` |
| Запись в файл | | `open(имя, "w")` | `PrintWriter`, `Files.newBufferedWriter` | `ofstream` |

В командной строке стандартные потоки можно **перенаправить** в файлы: `программа < ввод.txt > вывод.txt`. Программа об этом не знает — она по-прежнему читает из `cin`/`input()`. Системы автоматической проверки работают именно так: тестовый файл подключается к стандартному вводу программы.

## Чтение потока до конца

Во многих задачах объём данных заранее не сообщается: «читайте, пока файл не закончится». Конец потока называют **EOF** (end of file). С клавиатуры EOF вводится в Windows клавишами Ctrl+Z и Enter, в Linux/macOS — Ctrl+D.

| Язык | Построчно до EOF | По словам (по числам) до EOF |
|---|---|---|
| Python | `for satr in sys.stdin:` | `sys.stdin.read().split()` |
| Java | `while ((satr = oquvchi.readLine()) != null)` | `while (in.hasNext())` (`Scanner`) |
| C++ | `while (getline(cin, satr))` | `while (cin >> x)` |

В C++ выражение `cin >> x` даёт `true`, если чтение удалось, и `false` при EOF или ошибке, — поэтому его пишут прямо в условие `while`. В Python у каждой строки, прочитанной из `sys.stdin`, в конце остаётся `\n` — его убирают через `rstrip("\n")`.

## Буферизация и закрытие файла

Записывать каждый символ на диск отдельно очень медленно. Поэтому потоки сначала собирают данные в **буфер** в памяти и записывают их разом, когда он заполнится (или поток закроется). Важный вывод: если файл **не закрыть**, последние данные могут остаться в буфере и не попасть в файл.

| Язык | Автоматическое закрытие |
|---|---|
| Python | `with open(имя, "w") as f:` — закрывается при выходе из блока |
| Java | `try (PrintWriter f = new PrintWriter(имя)) { ... }` — try-with-resources |
| C++ | объект `ofstream` закрывается при выходе из своей области видимости (деструктор); при необходимости `f.close()` |

Если программа записывает файл и хочет сразу прочитать его, поток записи должен быть закрыт **до** чтения.

## Разобранный пример 1: все числа потока

Во стандартном вводе на нескольких строках записаны целые числа; их количество неизвестно. Выведите количество чисел, их сумму и наибольшее (если чисел нет — `0 0 -`).

```python
import sys

sonlar = list(map(int, sys.stdin.read().split()))    # весь поток, разбитый на слова
if sonlar:
    print(len(sonlar), sum(sonlar), max(sonlar))
else:
    print(0, 0, "-")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long soni = 0, yigindi = 0, engKatta = Long.MIN_VALUE;
        while (in.hasNextLong()) {                          // пока поток не кончится
            long x = in.nextLong();
            soni++;
            yigindi += x;
            engKatta = Math.max(engKatta, x);
        }
        System.out.println(soni + " " + yigindi + " " + (soni > 0 ? String.valueOf(engKatta) : "-"));
    }
}
```
```cpp
#include <climits>
#include <iostream>
using namespace std;

int main() {
    long long x, soni = 0, yigindi = 0, engKatta = LLONG_MIN;
    while (cin >> x) {                                      // при EOF — false
        soni++;
        yigindi += x;
        if (x > engKatta) engKatta = x;
    }
    cout << soni << " " << yigindi << " ";
    if (soni > 0) cout << engKatta;
    else cout << "-";
    cout << endl;
    return 0;
}
```

Ввод:

```text
12 7
-3
40 5 9
```

Вывод:

```text
6 70 40
```

Ни одной из трёх программ не важно, как разбиты строки: придут ли числа в одной строке или каждое в своей, результат один. Java и C++ обрабатывают числа **потоком**, по одному, и не хранят их все в памяти — это важно для очень больших файлов. Версия на Python читает весь поток: коротко, но если поток занимает гигабайты, он может не поместиться в памяти.

## Разобранный пример 2: фильтрация файла журнала

Во стандартном вводе — строки журнала (лога) программы: `ДАТА УРОВЕНЬ СООБЩЕНИЕ`, уровень — `INFO`, `WARN` или `ERROR`. Запишите строки `ERROR` в файл `xatolar.txt` (ошибки), закройте его, затем прочитайте обратно и выведите число ошибок и каждое сообщение об ошибке (с датой).

```python
import sys

with open("xatolar.txt", "w", encoding="utf-8") as fayl:      # запись
    for satr in sys.stdin:
        qismlar = satr.split(maxsplit=2)
        if len(qismlar) == 3 and qismlar[1] == "ERROR":
            fayl.write(qismlar[0] + " " + qismlar[2].rstrip("\n") + "\n")
# вышли из блока with — файл закрыт, буфер записан на диск

with open("xatolar.txt", encoding="utf-8") as fayl:           # повторное чтение
    xatolar = [s.rstrip("\n") for s in fayl]
print(len(xatolar))
for x in xatolar:
    print(x)
```
```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader kirish = new BufferedReader(new InputStreamReader(System.in));
        try (PrintWriter fayl = new PrintWriter("xatolar.txt", "UTF-8")) {   // запись
            String satr;
            while ((satr = kirish.readLine()) != null) {
                String[] q = satr.trim().split("\\s+", 3);
                if (q.length == 3 && q[1].equals("ERROR")) fayl.println(q[0] + " " + q[2]);
            }
        }   // try закончился — файл закрыт

        List<String> xatolar = new ArrayList<>();
        try (BufferedReader fayl = new BufferedReader(new FileReader("xatolar.txt"))) {   // повторное чтение
            String satr;
            while ((satr = fayl.readLine()) != null) xatolar.add(satr);
        }
        System.out.println(xatolar.size());
        for (String x : xatolar) System.out.println(x);
    }
}
```
```cpp
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    {
        ofstream fayl("xatolar.txt");                       // запись
        string satr;
        while (getline(cin, satr)) {
            istringstream oqim(satr);
            string sana, daraja, xabar;
            oqim >> sana >> daraja;
            getline(oqim >> ws, xabar);                     // остаток строки
            if (daraja == "ERROR") fayl << sana << " " << xabar << "\n";
        }
    }   // блок закончился — ofstream закрыт

    ifstream fayl("xatolar.txt");                           // повторное чтение
    vector<string> xatolar;
    string satr;
    while (getline(fayl, satr)) xatolar.push_back(satr);
    cout << xatolar.size() << endl;
    for (const string& x : xatolar) cout << x << endl;
    return 0;
}
```

Ввод:

```text
2026-03-01 INFO сервер запущен
2026-03-01 ERROR нет подключения к базе
2026-03-02 WARN диск занят на 85%
2026-03-02 ERROR файл не найден: report.pdf
2026-03-03 INFO резервная копия готова
```

Вывод:

```text
2
2026-03-01 нет подключения к базе
2026-03-02 файл не найден: report.pdf
```

С `maxsplit=2` (в Java — `split(..., 3)`) строка разбивается только на **три** части — пробелы внутри сообщения сохраняются. В C++ `oqim >> ws` пропускает пробелы, а следующий `getline` берёт остаток строки (*sana* — дата, *daraja* — уровень, *xabar* — сообщение). Во всех трёх языках поток записи открыт **в отдельном блоке**: как только блок заканчивается, файл закрывается, и последующее чтение видит все данные.

## Разобранный пример 3: нумерация строк

Запишите все строки стандартного ввода (включая пустые) в файл `raqamlangan.txt` (пронумерованный) с номерами: номер выровнен вправо по ширине самого большого номера, затем `| ` и сама строка. Потом прочитайте файл и выведите его на экран.

```python
import sys

qatorlar = [s.rstrip("\n") for s in sys.stdin]
en = len(str(len(qatorlar)))                         # ширина самого большого номера

with open("raqamlangan.txt", "w", encoding="utf-8") as fayl:
    for i, q in enumerate(qatorlar, start=1):
        fayl.write(f"{i:>{en}}| {q}\n")

with open("raqamlangan.txt", encoding="utf-8") as fayl:
    print(fayl.read(), end="")
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader kirish = new BufferedReader(new InputStreamReader(System.in));
        List<String> qatorlar = new ArrayList<>();
        String satr;
        while ((satr = kirish.readLine()) != null) qatorlar.add(satr);
        int en = String.valueOf(qatorlar.size()).length();      // ширина самого большого номера

        try (PrintWriter fayl = new PrintWriter("raqamlangan.txt", "UTF-8")) {
            for (int i = 0; i < qatorlar.size(); i++) {
                fayl.printf("%" + en + "d| %s%n", i + 1, qatorlar.get(i));
            }
        }

        System.out.print(Files.readString(Path.of("raqamlangan.txt"), StandardCharsets.UTF_8));
    }
}
```
```cpp
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<string> qatorlar;
    string satr;
    while (getline(cin, satr)) qatorlar.push_back(satr);
    int en = to_string(qatorlar.size()).size();             // ширина самого большого номера

    {
        ofstream fayl("raqamlangan.txt");
        for (size_t i = 0; i < qatorlar.size(); i++) {
            fayl << setw(en) << i + 1 << "| " << qatorlar[i] << "\n";
        }
    }

    ifstream fayl("raqamlangan.txt");
    while (getline(fayl, satr)) cout << satr << "\n";
    return 0;
}
```

Для ввода из 12 строк номера выравниваются в колонку шириной два символа:

```text
 1| первая строка
 2|
 ...
12| последняя строка
```

Ширина номеров известна лишь после чтения всех строк, поэтому здесь строки сначала собираются в памяти. Выравнивание колонок: в Python `f"{i:>{en}}"` берёт и ширину из переменной, в Java строка формата `"%2d| %s%n"` собирается внутри программы, в C++ `setw(en)` действует только на **следующее** выводимое значение. В Java `%n` в `printf` пишет конец строки платформы; поскольку автоматическая проверка работает в Linux, это `\n` (*qatorlar* — строки, *en* — ширина).

:::warning
**Частые ошибки**

- **Чтение записанного файла без закрытия.** Буфер мог ещё не попасть на диск, и файл выглядит пустым или неполным; закрывайте запись через `with`/`try`/блок.
- **`while (!cin.eof())` в C++.** Неудачное последнее чтение замечается слишком поздно, и последнее значение обрабатывается дважды; пишите `while (cin >> x)`.
- **Неудалённый `\n` в Python.** Строки из `sys.stdin` и файлов заканчиваются на `\n`; `print` тогда выводит лишнюю пустую строку.
- **Не указана кодировка.** В Windows кодировка по умолчанию может быть другой, и узбекские буквы испортятся; явно пишите `encoding="utf-8"`, `"UTF-8"`.
:::

:::tip
Проверяя программу, не набирайте ввод каждый раз вручную — сохраните его в файл и перенаправьте: `python main.py < test1.txt`. Подготовив несколько тестовых файлов, после каждого изменения можно мгновенно перепроверить их все.
:::

## Итоги

- Поток — «труба», по которой данные приходят последовательно; клавиатура, экран и файлы обрабатываются одинаково.
- До конца потока: `for satr in sys.stdin`, `readLine() != null`, `while (getline(cin, satr))` или `while (cin >> x)`.
- Потоковая обработка позволяет работать с большими файлами, не загружая их в память.
- Запись буферизуется: только при закрытии файла все данные попадают на диск; используйте `with`, try-with-resources или блок.
- Стандартные потоки можно перенаправить в файлы в командной строке: `программа < ввод.txt > вывод.txt`.

## Проверьте себя

:::check
Q: Что такое EOF и как его ввести с клавиатуры?
A: Состояние, означающее конец потока; в Windows — Ctrl+Z и Enter, в Linux/macOS — Ctrl+D.
Q: Когда останавливается `while (cin >> x)` в C++?
A: Когда поток закончился или встретились нечисловые данные — когда чтение не удалось.
Q: Зачем закрывать файл после записи?
A: Данные могут оставаться в буфере; при закрытии буфер записывается на диск, а файл возвращается системе.
Q: Что делает команда `программа < a.txt > b.txt`?
A: Перенаправляет стандартный ввод программы из `a.txt`, а стандартный вывод — в `b.txt`.
Q: Что возвращает в Python `"2026-03-01 ERROR файл не найден".split(maxsplit=2)`?
A: `["2026-03-01", "ERROR", "файл не найден"]` — разбиение только по первым двум пробелам.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: чтение и запись файлов
Официальное руководство по `open`, `with` и методам файлов (на английском).
:::

:::link https://dev.java/learn/java-io/
Java: ввод и вывод
Официальный учебный ресурс о потоках, чтении и записи файлов (на английском).
:::
