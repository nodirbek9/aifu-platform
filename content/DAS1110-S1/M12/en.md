---
title: Working with files
summary: The idea of a file, text and binary files, opening modes (read, write, append), writing and reading line by line, closing a file, with and try-with-resources, errors, data in the CSV format.
---

## Why it matters

So far our programs took data from the keyboard and printed to the screen. As soon as a program ended, everything was gone: the scores entered, the results computed. Real programs **keep** their data: the register opens again tomorrow, a report is written to a file, settings are read on the next start.

The simplest way to keep data for a long time is a **file**. Databases, Excel sheets, images, the program code itself — all of them are files in the end. In this lesson we learn to write to a file, read from a file and work with the most common text format — CSV.

:::note
**After this lesson you will be able to:**

- tell text files from binary files;
- open a file in the required mode (read, write, append) and close it properly;
- write data to a file and read it line by line;
- handle errors such as a missing file;
- write, read and analyse data in the CSV format.
:::

## The idea of a file

A **file** is a sequence of bytes stored on disk under a name. It stays after the program ends.

| Kind | What is inside | Examples | How it is read |
|---|---|---|---|
| **Text file** | characters, split into lines | `.txt`, `.csv`, `.json`, `.py`, `.html` | in any text editor |
| **Binary file** | arbitrary bytes | `.jpg`, `.mp3`, `.exe`, `.docx` | with a special program |

In this lesson we work with text files.

Working with a file has three stages: **open** → **read or write** → **close**. When opening, a **mode** is chosen:

| Mode | Python | Meaning |
|---|---|---|
| Read | `"r"` | the file must exist |
| Write | `"w"` | the file is created; if it exists, it is **emptied** |
| Append | `"a"` | writing goes to the end of the file |

## Opening and closing a file

| Task | Python | Java | C++ |
|---|---|---|---|
| Open for writing | `f = open("a.txt", "w", encoding="utf-8")` | `PrintWriter w = new PrintWriter(new FileWriter("a.txt"));` | `ofstream f("a.txt");` |
| Write | `f.write("text\n")` or `print(x, file=f)` | `w.println(x);` | `f << x << endl;` |
| Open for reading | `f = open("a.txt", encoding="utf-8")` | `BufferedReader r = new BufferedReader(new FileReader("a.txt"));` | `ifstream f("a.txt");` |
| Read a line | `for qator in f:` | `while ((qator = r.readLine()) != null)` | `while (getline(f, qator))` |
| Close | `f.close()` | `w.close();` | `f.close();` (or automatically) |

**Why does closing matter?** Data that is written first sits in a **buffer** in memory and reaches the disk later. When the file is closed, the buffer is written to disk in full. In a file left open the last writes can be lost; besides, the operating system limits how many files can be open at once.

So that closing is never forgotten, the languages have special constructs:

| Language | Automatic closing |
|---|---|
| Python | `with open(...) as f:` — closed on leaving the block, even after an error |
| Java | `try (PrintWriter w = ...) { ... }` — **try-with-resources** |
| C++ | an `ofstream` / `ifstream` object closes itself when it leaves its block |

## Errors

Errors are normal when working with files: the file does not exist, there is no permission, the disk is full. Each language reports this its own way:

| Language | When the file is not found |
|---|---|
| Python | a `FileNotFoundError` exception; caught with `try` / `except` |
| Java | `FileNotFoundException` (a kind of `IOException`); catching it with `try` / `catch` is **required** |
| C++ | no exception; checked with `if (!f)` or `f.is_open()` |

Reading without checking that the file exists leads to a silent bug or a crash.

## The file path

`"baholar.txt"` is a **relative path**: the file is looked for in the current folder the program was started from. `"C:/AIFU/baholar.txt"` or `"/home/talaba/baholar.txt"` is a **full path**. On Windows a backslash inside a string is doubled (`"C:\\AIFU\\baholar.txt"`), or a plain `/` is used.

## Worked example 1: writing numbers to a file and reading them back

The first line has *n*, the second *n* marks. Write the marks to the file `baholar.txt` (each on its own line), then read the file and print the number of marks, their sum and their average.

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

print(f"Count: {soni}, sum: {yigindi}, average: {yigindi / soni:.2f}")
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

        System.out.println(String.format("Count: %d, sum: %d, average: %.2f",
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
    }                                // the block is left — the file is closed

    int yigindi = 0, soni = 0;
    ifstream f("baholar.txt");
    int b;
    while (f >> b) {                 // goes on while numbers can be read
        yigindi += b;
        soni++;
    }

    cout << fixed << setprecision(2)
         << "Count: " << soni << ", sum: " << yigindi
         << ", average: " << (double) yigindi / soni << endl;
    return 0;
}
```

For the input `5` and `4 5 3 5 4` the file `baholar.txt` looks like this:

```text
4
5
3
5
4
```

The program reads it and prints `Count: 5, sum: 21, average: 4.20`.

1. The file opened for writing is closed at the end of the `with` / try-with-resources / `{ }` block — all the data is on disk before reading begins.
2. In Python and Java the file is read line by line and each line is turned into a number (`int(qator)` ignores the `\n` at the end of the line by itself; Java uses `trim()`).
3. In C++ `f >> b` skips spaces and new lines and reads the next number; when the file ends, the condition becomes false.

## Worked example 2: a CSV file

The first line has *n*, the next *n* lines a student's name and score separated by a comma: `Name,score`. Write the data to `talabalar.csv`, then read the file and print the students who scored 56 or more (in file order) and how many there are.

**CSV (comma-separated values)**: each line is one record, and the fields are separated by commas. Excel, Google Sheets and databases can read and write CSV.

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
print("Total:", len(otganlar))
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
        System.out.println("Total: " + otganlar.size());
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
        getline(ss, ism, ',');           // up to the comma
        getline(ss, ball);               // the rest
        if (stoi(ball) >= 56) {
            otganlar.push_back(ism);
        }
    }

    for (const string& ism : otganlar) {
        cout << ism << endl;
    }
    cout << "Total: " << otganlar.size() << endl;
    return 0;
}
```

Input:

```text
4
Aziz,78
Dilnoza,92
Bekzod,45
Malika,56
```

Output:

```text
Aziz
Dilnoza
Malika
Total: 3
```

Each line is split by the comma into two fields: `split(",")` in Python and Java, `getline(ss, ism, ',')` in C++, where the third argument names the separator character. `stoi` (C++) and `Integer.parseInt` (Java) turn a string into a number. Bekzod (45) is not on the list; Malika (56) is on the boundary and gets in thanks to `>=`. (*otganlar* are those who passed, *ism* is a name.)

:::warning
**Common mistakes**

- **Opening an existing file in `"w"` mode.** The file is emptied at once; for appending use `"a"` (Java: `new FileWriter(name, true)`, C++: `ofstream f(name, ios::app)`).
- **Not closing the file.** The last writes may never reach the disk; use `with` / try-with-resources.
- **Not checking that the file opened (C++).** If the file is missing, `ifstream` stays silent and reading gives nothing.
- **Forgetting the `\n` at the end of a line.** In Python use `qator.strip()`, otherwise comparisons and `split` give surprising results.
- **Not stating the encoding.** In Python, without `encoding="utf-8"`, Uzbek letters get mangled on Windows.
- **A comma inside a CSV field.** A field such as `"Aliyev, Aziz",78` is split wrongly by a plain `split(",")`; use a dedicated library (the `csv` module) in such cases.
:::

:::tip
Put writing and reading the file into two separate functions: `saqla(list, name)` ("save") and `yukla(name)` ("load"). Then they can be tested separately and the data format changed in one place.
:::

## Summary

- A file is a named sequence of bytes on disk; text files consist of lines.
- The order of work: open (with a mode) → read/write → close.
- The modes: read `r`, write `w` (empties the file), append `a`.
- Python's `with`, Java's try-with-resources and a C++ object's block close the file automatically.
- File errors: exceptions in Python and Java, the `if (!f)` check in C++.
- CSV is lines of comma-separated fields; they are split with `split(",")` or `getline(ss, x, ',')`.

## Check yourself

:::check
Q: How does a text file differ from a binary file?
A: A text file consists of characters and lines and can be read in a text editor; a binary file consists of arbitrary bytes and is opened with a special program.
Q: What happens to the old content of a file opened in Python with `open("x.txt", "w")`?
A: It is deleted: `"w"` mode opens the file by emptying it. To add to the end, use `"a"`.
Q: Why should a file be closed?
A: Written data sits in a buffer and is written to disk in full when the file is closed; without closing, the last writes can be lost. The number of open files is also limited.
Q: How do you find out in C++ that a file did not open?
A: `ifstream f("a.txt"); if (!f) { ... }` or with `f.is_open()`.
Q: How do you get the name and score from the line `Aziz,78` in Java?
A: `String[] m = qator.split(",");` — `m[0]` is the name, `Integer.parseInt(m[1])` the score.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: reading and writing files
`open`, `with` and file methods in the official tutorial.
:::

:::link https://en.cppreference.com/w/cpp/io/basic_fstream
cppreference: file streams
`ifstream`, `ofstream` and `fstream` in C++.
:::

:::link https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
Oracle Java Tutorial: try-with-resources
Closing resources automatically.
:::
