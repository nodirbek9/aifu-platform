---
title: Работа с файлами
summary: Понятие файла, текстовые и двоичные файлы, режимы открытия (чтение, запись, дозапись), запись и построчное чтение, закрытие файла, with и try-with-resources, ошибки, данные в формате CSV.
---

## Почему это важно

До сих пор наши программы брали данные с клавиатуры и выводили на экран. Как только программа завершалась, всё пропадало: введённые баллы, вычисленные результаты. Настоящие программы **сохраняют** данные: журнал откроется и завтра, отчёт пишется в файл, настройки читаются при следующем запуске.

Самый простой способ хранить данные долго — **файл**. Базы данных, таблицы Excel, изображения, сам код программы — всё это в конечном счёте файлы. На этом занятии научимся писать в файл, читать из файла и работать с самым распространённым текстовым форматом — CSV.

:::note
**После этого занятия вы сможете:**

- различать текстовые и двоичные файлы;
- открывать файл в нужном режиме (чтение, запись, дозапись) и правильно закрывать его;
- записывать данные в файл и читать его построчно;
- обрабатывать ошибки вроде отсутствия файла;
- записывать, читать и анализировать данные в формате CSV.
:::

## Понятие файла

**Файл** — последовательность байтов, хранящаяся на диске под именем. Он сохраняется и после завершения программы.

| Вид | Что внутри | Примеры | Как читается |
|---|---|---|---|
| **Текстовый файл** | символы, разбитые на строки | `.txt`, `.csv`, `.json`, `.py`, `.html` | в любом текстовом редакторе |
| **Двоичный файл** | произвольные байты | `.jpg`, `.mp3`, `.exe`, `.docx` | специальной программой |

На этом занятии работаем с текстовыми файлами.

Работа с файлом состоит из трёх этапов: **открытие** → **чтение или запись** → **закрытие**. При открытии выбирается **режим**:

| Режим | Python | Смысл |
|---|---|---|
| Чтение | `"r"` | файл обязан существовать |
| Запись | `"w"` | файл создаётся; если существует — **очищается** |
| Дозапись | `"a"` | запись добавляется в конец файла |

## Открытие и закрытие файла

| Задача | Python | Java | C++ |
|---|---|---|---|
| Открыть для записи | `f = open("a.txt", "w", encoding="utf-8")` | `PrintWriter w = new PrintWriter(new FileWriter("a.txt"));` | `ofstream f("a.txt");` |
| Записать | `f.write("текст\n")` или `print(x, file=f)` | `w.println(x);` | `f << x << endl;` |
| Открыть для чтения | `f = open("a.txt", encoding="utf-8")` | `BufferedReader r = new BufferedReader(new FileReader("a.txt"));` | `ifstream f("a.txt");` |
| Прочитать строку | `for qator in f:` | `while ((qator = r.readLine()) != null)` | `while (getline(f, qator))` |
| Закрыть | `f.close()` | `w.close();` | `f.close();` (или автоматически) |

**Почему закрывать важно?** Записанные данные сначала лежат в **буфере** в памяти и попадают на диск позже. При закрытии буфер полностью записывается на диск. В незакрытом файле последние записи могут потеряться; кроме того, операционная система ограничивает число одновременно открытых файлов.

Чтобы не забыть закрыть файл, в языках есть специальные конструкции:

| Язык | Автоматическое закрытие |
|---|---|
| Python | `with open(...) as f:` — закрывается при выходе из блока, даже при ошибке |
| Java | `try (PrintWriter w = ...) { ... }` — **try-with-resources** |
| C++ | объект `ofstream` / `ifstream` сам закрывается при выходе из своего блока |

## Ошибки

Ошибка при работе с файлом — обычное дело: файла нет, нет прав, диск заполнен. Каждый язык сообщает об этом по-своему:

| Язык | Если файл не найден |
|---|---|
| Python | исключение `FileNotFoundError`; перехватывается `try` / `except` |
| Java | `FileNotFoundException` (вид `IOException`); перехватывать через `try` / `catch` **обязательно** |
| C++ | исключения нет; проверяется `if (!f)` или `f.is_open()` |

Чтение без проверки наличия файла приводит к тихой ошибке или падению программы.

## Путь к файлу

`"baholar.txt"` — **относительный путь**: файл ищется в текущей папке, из которой запущена программа. `"C:/AIFU/baholar.txt"` или `"/home/talaba/baholar.txt"` — **полный путь**. В Windows обратная косая черта внутри строки удваивается (`"C:\\AIFU\\baholar.txt"`) или используется обычная `/`.

## Разобранный пример 1: запись чисел в файл и обратное чтение

В первой строке *n*, во второй — *n* оценок. Запишите оценки в файл `baholar.txt` (каждую на отдельной строке), затем прочитайте файл и выведите количество, сумму и среднее оценок.

```python
n = int(input())
baholar = list(map(int, input().split()))

with open("baholar.txt", "w", encoding="utf-8") as f:
    for b in baholar:
        f.write(f"{b}\n")

yigindi = 0
soni = 0
with open("baholar.txt", encoding="utf-8") as f:
    for qator in f:
        yigindi += int(qator)
        soni += 1

print(f"Количество: {soni}, сумма: {yigindi}, среднее: {yigindi / soni:.2f}")
```
```java
import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        try (PrintWriter w = new PrintWriter(new FileWriter("baholar.txt"))) {
            for (int i = 0; i < n; i++) {
                w.println(in.nextInt());
            }
        }

        int yigindi = 0, soni = 0;
        try (BufferedReader r = new BufferedReader(new FileReader("baholar.txt"))) {
            String qator;
            while ((qator = r.readLine()) != null) {
                yigindi += Integer.parseInt(qator.trim());
                soni++;
            }
        }

        System.out.println(String.format("Количество: %d, сумма: %d, среднее: %.2f",
                soni, yigindi, (double) yigindi / soni));
    }
}
```
```cpp
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;

    {
        ofstream f("baholar.txt");
        for (int i = 0; i < n; i++) {
            int b;
            cin >> b;
            f << b << endl;
        }
    }                                // вышли из блока — файл закрыт

    int yigindi = 0, soni = 0;
    ifstream f("baholar.txt");
    int b;
    while (f >> b) {                 // продолжается, пока читаются числа
        yigindi += b;
        soni++;
    }

    cout << fixed << setprecision(2)
         << "Количество: " << soni << ", сумма: " << yigindi
         << ", среднее: " << (double) yigindi / soni << endl;
    return 0;
}
```

При вводе `5` и `4 5 3 5 4` файл `baholar.txt` выглядит так:

```text
4
5
3
5
4
```

Программа читает его и выводит: `Количество: 5, сумма: 21, среднее: 4.20`.

1. Файл, открытый для записи, закрывается по окончании блока `with` / try-with-resources / `{ }` — перед чтением все данные уже на диске.
2. В Python и Java файл читается построчно, и каждая строка превращается в число (`int(qator)` сам игнорирует `\n` в конце строки; в Java — `trim()`).
3. В C++ `f >> b` пропускает пробелы и переводы строк и читает следующее число; когда файл кончается, условие становится ложным.

## Разобранный пример 2: файл CSV

В первой строке *n*, в следующих *n* строках — имя студента и балл через запятую: `Имя,балл`. Запишите данные в `talabalar.csv`, затем прочитайте файл и выведите тех, кто набрал 56 баллов и выше (в порядке файла), и их количество.

**CSV (comma-separated values)**: каждая строка — одна запись, поля разделены запятыми. CSV умеют читать и записывать Excel, Google Sheets и базы данных.

```python
n = int(input())
with open("talabalar.csv", "w", encoding="utf-8") as f:
    for _ in range(n):
        f.write(input() + "\n")

otganlar = []
with open("talabalar.csv", encoding="utf-8") as f:
    for qator in f:
        ism, ball = qator.strip().split(",")
        if int(ball) >= 56:
            otganlar.append(ism)

for ism in otganlar:
    print(ism)
print("Всего:", len(otganlar))
```
```java
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        try (PrintWriter w = new PrintWriter(new FileWriter("talabalar.csv"))) {
            for (int i = 0; i < n; i++) {
                w.println(in.nextLine());
            }
        }

        List<String> otganlar = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader("talabalar.csv"))) {
            String qator;
            while ((qator = r.readLine()) != null) {
                String[] maydon = qator.trim().split(",");
                if (Integer.parseInt(maydon[1]) >= 56) {
                    otganlar.add(maydon[0]);
                }
            }
        }

        for (String ism : otganlar) {
            System.out.println(ism);
        }
        System.out.println("Всего: " + otganlar.size());
    }
}
```
```cpp
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    cin.ignore();
    {
        ofstream f("talabalar.csv");
        for (int i = 0; i < n; i++) {
            string qator;
            getline(cin, qator);
            f << qator << endl;
        }
    }

    vector<string> otganlar;
    ifstream f("talabalar.csv");
    string qator;
    while (getline(f, qator)) {
        stringstream ss(qator);
        string ism, ball;
        getline(ss, ism, ',');           // до запятой
        getline(ss, ball);               // остаток
        if (stoi(ball) >= 56) {
            otganlar.push_back(ism);
        }
    }

    for (const string& ism : otganlar) {
        cout << ism << endl;
    }
    cout << "Всего: " << otganlar.size() << endl;
    return 0;
}
```

Ввод:

```text
4
Aziz,78
Dilnoza,92
Bekzod,45
Malika,56
```

Выводится:

```text
Aziz
Dilnoza
Malika
Всего: 3
```

Каждая строка разбивается по запятой на два поля: в Python и Java — `split(",")`, в C++ — `getline(ss, ism, ',')`, где третий аргумент задаёт символ-разделитель. `stoi` (C++) и `Integer.parseInt` (Java) превращают строку в число. Бекзод (45) в список не попал; Малика (56) — на границе, попала благодаря `>=`. (*otganlar* — сдавшие, *ism* — имя.)

:::warning
**Частые ошибки**

- **Открыть существующий файл в режиме `"w"`.** Файл сразу очищается; для дозаписи — `"a"` (Java: `new FileWriter(имя, true)`, C++: `ofstream f(имя, ios::app)`).
- **Не закрыть файл.** Последние записи могут не попасть на диск; используйте `with` / try-with-resources.
- **Не проверить, открылся ли файл (C++).** Если файла нет, `ifstream` молчит, и чтение ничего не даёт.
- **Забыть про `\n` в конце строки.** В Python — `qator.strip()`, иначе сравнение и `split` дают неожиданный результат.
- **Не указать кодировку.** В Python без `encoding="utf-8"` в Windows узбекские буквы искажаются.
- **Запятая внутри поля CSV.** Поле вроде `"Aliyev, Aziz",78` разбивается простым `split(",")` неправильно; в таких случаях используйте специальную библиотеку (модуль `csv`).
:::

:::tip
Вынесите запись и чтение файла в две отдельные функции: `saqla(spisok, imya)` («сохранить») и `yukla(imya)` («загрузить»). Тогда их можно тестировать отдельно, а формат данных менять в одном месте.
:::

## Итоги

- Файл — именованная последовательность байтов на диске; текстовые файлы состоят из строк.
- Порядок работы: открыть (с режимом) → читать/писать → закрыть.
- Режимы: чтение `r`, запись `w` (очищает файл), дозапись `a`.
- `with` в Python, try-with-resources в Java, блок объекта в C++ закрывают файл автоматически.
- Ошибки работы с файлами: исключения в Python и Java, проверка `if (!f)` в C++.
- CSV — строки с полями через запятую; разбираются `split(",")` или `getline(ss, x, ',')`.

## Проверьте себя

:::check
Q: Чем текстовый файл отличается от двоичного?
A: Текстовый файл состоит из символов и строк, его можно читать в текстовом редакторе; двоичный состоит из произвольных байтов и открывается специальной программой.
Q: Что станет со старым содержимым файла, если открыть его в Python через `open("x.txt", "w")`?
A: Оно удалится: режим `"w"` открывает файл, очищая его. Для добавления в конец — `"a"`.
Q: Зачем закрывать файл?
A: Записанные данные лежат в буфере и при закрытии полностью записываются на диск; без закрытия последние записи могут потеряться. Кроме того, число открытых файлов ограничено.
Q: Как в C++ узнать, что файл не открылся?
A: `ifstream f("a.txt"); if (!f) { ... }` или через `f.is_open()`.
Q: Как в Java получить имя и балл из строки `Aziz,78`?
A: `String[] m = qator.split(",");` — `m[0]` — имя, `Integer.parseInt(m[1])` — балл.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: чтение и запись файлов
`open`, `with` и методы файлов в официальном руководстве (на английском).
:::

:::link https://en.cppreference.com/w/cpp/io/basic_fstream
cppreference: файловые потоки
`ifstream`, `ofstream` и `fstream` в C++ (на английском).
:::

:::link https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
Oracle Java Tutorial: try-with-resources
Автоматическое закрытие ресурсов (на английском).
:::
