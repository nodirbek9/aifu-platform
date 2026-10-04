---
title: A general idea of text files. Files and streams
summary: The idea of a stream — standard input, output and error streams, file streams; reading a stream to its end (EOF), processing line by line, writing to a file and reading it back, buffering; filtering a log file and numbering lines.
---

## Why it matters

A program may get data from the keyboard, a file, the network or another program. For all of these, languages use one common idea — the **stream**: a "pipe" through which bytes or characters arrive one after another. For a programmer who has learned to work with streams there is almost no difference between reading from the keyboard and reading from a file.

In the first semester we learned to open, write and read a file. In this practical session we practise the working methods of streams: reading a stream to its end when the amount of data is not known in advance, filtering data into a file, and reading it back.

:::note
**After this session you will be able to:**

- tell the standard input, output and error streams and file streams apart;
- read a stream to its end (EOF) in three languages;
- process a large file line by line without loading it whole into memory;
- write a result to a file and read it back;
- explain what buffering is and why closing a file matters.
:::

## Streams

| Stream | Job | Python | Java | C++ |
|---|---|---|---|---|
| Standard input | the keyboard or a redirected file | `sys.stdin`, `input()` | `System.in` | `cin` |
| Standard output | the screen | `sys.stdout`, `print()` | `System.out` | `cout` |
| Error stream | messages, debug information | `sys.stderr` | `System.err` | `cerr` |
| Reading a file | | `open(name)` | `BufferedReader`, `Files.newBufferedReader` | `ifstream` |
| Writing a file | | `open(name, "w")` | `PrintWriter`, `Files.newBufferedWriter` | `ofstream` |

On the command line the standard streams can be **redirected** to files: `program < input.txt > output.txt`. The program does not know — it still reads from `cin`/`input()`. Automatic checking systems work exactly this way: the test file is connected to the program's standard input.

## Reading a stream to its end

In many problems the amount of data is not given in advance: "read until the file ends". The end of a stream is called **EOF** (end of file). From the keyboard, EOF is given with Ctrl+Z and Enter on Windows, and Ctrl+D on Linux/macOS.

| Language | Line by line up to EOF | Word by word (number by number) up to EOF |
|---|---|---|
| Python | `for satr in sys.stdin:` | `sys.stdin.read().split()` |
| Java | `while ((satr = oquvchi.readLine()) != null)` | `while (in.hasNext())` (`Scanner`) |
| C++ | `while (getline(cin, satr))` | `while (cin >> x)` |

In C++ the expression `cin >> x` gives `true` if reading succeeded and `false` at EOF or on an error — which is why it is written straight into the `while` condition. In Python every line read from `sys.stdin` keeps its `\n` at the end — it is removed with `rstrip("\n")`.

## Buffering and closing a file

Writing every character to disk separately is very slow. So streams first collect data in a **buffer** in memory and write it all at once when it fills up (or when the stream is closed). An important consequence: if a file is **not closed**, the last data may stay in the buffer and never reach the file.

| Language | Automatic closing |
|---|---|
| Python | `with open(name, "w") as f:` — closed on leaving the block |
| Java | `try (PrintWriter f = new PrintWriter(name)) { ... }` — try-with-resources |
| C++ | an `ofstream` object is closed when it leaves its scope (destructor); `f.close()` if needed |

If a program writes a file and wants to read it back at once, the writing stream must be closed **before** reading.

## Worked example 1: all numbers in a stream

Standard input holds integers on several lines; how many there are is unknown. Print their count, sum and largest value (if there are none — `0 0 -`).

```python
import sys

sonlar = list(map(int, sys.stdin.read().split()))    # the whole stream, split into words
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
        while (in.hasNextLong()) {                          // until the stream ends
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
    while (cin >> x) {                                      // false at EOF
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

Input:

```text
12 7
-3
40 5 9
```

Output:

```text
6 70 40
```

None of the three programs cares how the lines are split: whether the numbers come on one line or each on its own, the result is the same. Java and C++ process the numbers **as a stream**, one by one, and do not keep them all in memory — this matters for very large files. The Python version reads the whole stream: short, but if the stream is gigabytes long it may not fit in memory.

## Worked example 2: filtering a log file

Standard input holds the lines of a program log: `DATE LEVEL MESSAGE`, the level being `INFO`, `WARN` or `ERROR`. Write the `ERROR` lines to the file `xatolar.txt` (errors), close it, then read it back and print the number of errors and each error message (with its date).

```python
import sys

with open("xatolar.txt", "w", encoding="utf-8") as fayl:      # writing
    for satr in sys.stdin:
        qismlar = satr.split(maxsplit=2)
        if len(qismlar) == 3 and qismlar[1] == "ERROR":
            fayl.write(qismlar[0] + " " + qismlar[2].rstrip("\n") + "\n")
# we left the with block — the file is closed and the buffer is on disk

with open("xatolar.txt", encoding="utf-8") as fayl:           # reading back
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
        try (PrintWriter fayl = new PrintWriter("xatolar.txt", "UTF-8")) {   // writing
            String satr;
            while ((satr = kirish.readLine()) != null) {
                String[] q = satr.trim().split("\\s+", 3);
                if (q.length == 3 && q[1].equals("ERROR")) fayl.println(q[0] + " " + q[2]);
            }
        }   // the try ended — the file is closed

        List<String> xatolar = new ArrayList<>();
        try (BufferedReader fayl = new BufferedReader(new FileReader("xatolar.txt"))) {   // reading back
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
        ofstream fayl("xatolar.txt");                       // writing
        string satr;
        while (getline(cin, satr)) {
            istringstream oqim(satr);
            string sana, daraja, xabar;
            oqim >> sana >> daraja;
            getline(oqim >> ws, xabar);                     // the rest of the line
            if (daraja == "ERROR") fayl << sana << " " << xabar << "\n";
        }
    }   // the block ended — the ofstream is closed

    ifstream fayl("xatolar.txt");                           // reading back
    vector<string> xatolar;
    string satr;
    while (getline(fayl, satr)) xatolar.push_back(satr);
    cout << xatolar.size() << endl;
    for (const string& x : xatolar) cout << x << endl;
    return 0;
}
```

Input:

```text
2026-03-01 INFO server started
2026-03-01 ERROR cannot connect to the database
2026-03-02 WARN disk 85% full
2026-03-02 ERROR file not found: report.pdf
2026-03-03 INFO backup ready
```

Output:

```text
2
2026-03-01 cannot connect to the database
2026-03-02 file not found: report.pdf
```

With `maxsplit=2` (`split(..., 3)` in Java) the line is split into only **three** parts — the spaces inside the message are kept. In C++ `oqim >> ws` skips whitespace and the following `getline` takes the rest of the line (*sana* is the date, *daraja* the level, *xabar* the message). In all three languages the writing stream is opened **in a separate block**: as soon as the block ends, the file is closed and the reading that follows sees all the data.

## Worked example 3: numbering lines

Write all lines of standard input (empty ones too) to the file `raqamlangan.txt` (numbered), each with its number: the number right-aligned, as wide as the largest number, then `| ` and the line itself. Then read the file and print it on the screen.

```python
import sys

qatorlar = [s.rstrip("\n") for s in sys.stdin]
en = len(str(len(qatorlar)))                         # the width of the largest number

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
        int en = String.valueOf(qatorlar.size()).length();      // the width of the largest number

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
    int en = to_string(qatorlar.size()).size();             // the width of the largest number

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

For a 12-line input the numbers are aligned in a two-character column:

```text
 1| first line
 2|
 ...
12| last line
```

The width of the numbers is known only after all lines are read, so here the lines are first collected in memory. Aligning columns: in Python `f"{i:>{en}}"` takes the width from a variable too, in Java the format string `"%2d| %s%n"` is assembled inside the program, and in C++ `setw(en)` applies only to the **next** value printed. In Java `%n` in `printf` writes the platform's line ending; since the automatic check runs on Linux, it is `\n` (*qatorlar* are the lines, *en* the width).

:::warning
**Common mistakes**

- **Reading a written file without closing it.** The buffer may not have reached the disk yet, and the file looks empty or incomplete; close the writing with `with`/`try`/a block.
- **`while (!cin.eof())` in C++.** It notices the failed last read too late, and the last value is processed twice; write `while (cin >> x)`.
- **Not removing `\n` in Python.** Lines read from `sys.stdin` and from files end with `\n`; `print` then prints an extra empty line.
- **Not specifying the encoding.** On Windows the default encoding may differ, and Uzbek letters get garbled; write `encoding="utf-8"`, `"UTF-8"` explicitly.
:::

:::tip
When testing a program, instead of typing the input by hand every time, save it in a file and redirect it: `python main.py < test1.txt`. With several test files prepared, you can recheck all of them in a moment after every change.
:::

## Summary

- A stream is a "pipe" through which data arrives in sequence; the keyboard, the screen and files are handled the same way.
- To the end of a stream: `for satr in sys.stdin`, `readLine() != null`, `while (getline(cin, satr))` or `while (cin >> x)`.
- Processing as a stream lets large files be handled without loading them into memory.
- Writing is buffered: only when a file is closed does all the data reach the disk; use `with`, try-with-resources or a block.
- Standard streams can be redirected to files on the command line: `program < input.txt > output.txt`.

## Check yourself

:::check
Q: What is EOF and how is it given from the keyboard?
A: The state meaning the stream has ended; Ctrl+Z and Enter on Windows, Ctrl+D on Linux/macOS.
Q: When does `while (cin >> x)` stop in C++?
A: When the stream ends or non-numeric data appears — when reading fails.
Q: Why must a file be closed after writing to it?
A: The data may still be in the buffer; on closing, the buffer is written to disk and the file is returned to the system.
Q: What does the command `program < a.txt > b.txt` do?
A: It redirects the program's standard input from `a.txt` and its standard output to `b.txt`.
Q: What does `"2026-03-01 ERROR file not found".split(maxsplit=2)` return in Python?
A: `["2026-03-01", "ERROR", "file not found"]` — it splits only at the first two spaces.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: reading and writing files
The official tutorial on `open`, `with` and file methods.
:::

:::link https://dev.java/learn/java-io/
Java: input and output
The official learning resource on streams, reading and writing files.
:::
