---
title: Functions for working with strings
summary: The standard string functions in practice — trimming spaces, checking a beginning and an end, searching and taking a part, counting, joining, string builders (StringBuilder); parsing an e-mail address, compressing a string (RLE) and wrapping text into lines.
---

## Why it matters

Almost everything a user enters — a name, a phone number, an address, a password, a message — arrives as a string. Checking it, cleaning it and splitting it into the needed parts is the everyday work of any program. In the first semester we met the basic string operations; now we use them **together** to solve real problems.

In this session we complete a table of the string functions of the three languages and apply them in three practical problems: validating an e-mail address, compressing a string, and wrapping text into lines of a given width.

:::note
**After this session you will be able to:**

- choose and use the main string functions in three languages;
- split a string into parts and check each part;
- use `StringBuilder` and `join` to build strings efficiently;
- break a string problem into a sequence of simple functions.
:::

## A table of string functions

| Task | Python | Java | C++ (`std::string`) |
|---|---|---|---|
| length | `len(s)` | `s.length()` | `s.size()` |
| trim leading/trailing spaces | `s.strip()` | `s.trim()` / `s.strip()` | by hand: `find_first_not_of(' ')` and `find_last_not_of(' ')` |
| does it start / end with … | `s.startswith(p)`, `s.endswith(p)` | `s.startsWith(p)`, `s.endsWith(p)` | `s.rfind(p, 0) == 0`; C++20 — `s.starts_with(p)` |
| find a substring | `s.find(p)` (none — `-1`) | `s.indexOf(p)` (none — `-1`) | `s.find(p)` (none — `string::npos`) |
| last occurrence | `s.rfind(p)` | `s.lastIndexOf(p)` | `s.rfind(p)` |
| a substring | `s[i:j]` | `s.substring(i, j)` | `s.substr(i, length)` |
| how many times a character occurs | `s.count(c)` | a loop or `s.chars().filter(...).count()` | `count(s.begin(), s.end(), c)` |
| to lowercase | `s.lower()` | `s.toLowerCase()` | a loop with `tolower` |
| split | `s.split(separator)` | `s.split(regex)` | `istringstream` or `find` + `substr` |
| join | `separator.join(list)` | `String.join(separator, list)` | a loop with `+=` |

In C++ the second argument of `substr` is a **length**, while in Python and Java it is the **end index** (not included). This difference causes many mistakes.

## Building a string efficiently

In Java and Python a string is **immutable**: `s = s + c` creates a new string each time and copies the old one. Adding *n* times inside a loop takes *O*(*n*²) time. The right way:

| Language | Efficient building |
|---|---|
| Python | `append` the parts to a list, then `"".join(parts)` at the end |
| Java | `StringBuilder sb`; `sb.append(...)`; `sb.toString()` at the end |
| C++ | `string` is mutable: `s += c` is *O*(1) on average |

## Worked example 1: validating an e-mail address

The first line holds *n*, the next *n* lines e-mail addresses (possibly with spaces at the start and end). For each address: if it has exactly one `@`, the part before `@` (the login) is not empty, and the part after it (the domain) does not start with a dot and contains at least one dot — print the login and the domain in lowercase; otherwise `Error`.

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
    print(*natija if natija else ["Error"])
```
```java
import java.util.Scanner;

public class Main {
    static String tekshir(String manzil) {
        manzil = manzil.trim().toLowerCase();
        int belgi = manzil.indexOf('@');
        if (belgi == -1 || belgi != manzil.lastIndexOf('@')) return null;   // exactly one @
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
            System.out.println(natija != null ? natija : "Error");
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
    if (count(manzil.begin(), manzil.end(), '@') != 1) return "";   // exactly one @
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
    getline(cin, satr);                                 // the line end after n
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        string natija = tekshir(satr);
        cout << (natija.empty() ? "Error" : natija) << endl;
    }
    return 0;
}
```

Input:

```text
4
  Aziz.Karimov@AIFU.uz
malika@@mail.uz
@aifu.uz
bekzod@localhost
```

Output:

```text
aziz.karimov aifu.uz
Error
Error
Error
```

Each condition was checked with a separate, simple function: `strip`, `lower`, `count`, `split`, `startswith` (*tekshir* means check, *manzil* address, *domen* domain). In Java "exactly one `@`" is checked by `indexOf` and `lastIndexOf` pointing at the same place. In C++ the line end left after `n` is read away before `getline` — otherwise the first address would come out as an empty line. Real address rules are far more complex, but the principle of parsing is the same.

## Worked example 2: compressing a string (RLE)

One line consists of lowercase Latin letters. Compress it with **RLE** (run-length encoding): every group of identical consecutive letters is replaced with "letter + count" (`aaabcc` → `a3b1c2`). Print the compressed string and whether decompressing it gives back the original (`yes`/`no`).

```python
def siq(s):
    qismlar = []
    i = 0
    while i < len(s):
        j = i
        while j < len(s) and s[j] == s[i]:
            j += 1
        qismlar.append(s[i] + str(j - i))        # the letter and the group's length
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
print("yes" if och(siqilgan) == satr else "no")
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
            sb.append(s.charAt(i)).append(j - i);         // the letter and the group's length
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
        System.out.println(och(siqilgan).equals(satr) ? "yes" : "no");
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
        natija += to_string(j - i);                     // the letter and the group's length
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
    cout << (och(siqilgan) == satr ? "yes" : "no") << endl;
    return 0;
}
```

For `aaabccddddddddddddx` the output is `a3b1c2d12x1` and `yes`. When decompressing, a count may have several digits (`d12`), so after the letter we read **up to the first non-digit character** (*siq* means compress, *och* open). Remember that in C++'s `s.substr(i + 1, j - i - 1)` the second argument is a length. RLE is used in image formats (BMP, TIFF) and fax transmission: long runs of one colour compress very well, while a string without repeats such as `abc` actually grows (`a1b1c1`).

## Worked example 3: wrapping text into lines

The first line holds a width *W*, the second a text. Place the words, keeping their order, on lines no longer than *W*: if a word fits on the current line (one space between words), it is added to it, otherwise a new line starts. No word is longer than *W*. Print every line between `|` characters, padded with spaces up to *W*.

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
    getline(cin, satr);                                 // the line end after w
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

Input:

```text
16
Dasturlash har kuni ozgina mashq qilishni talab qiladi
```

Output:

```text
|Dasturlash har  |
|kuni ozgina     |
|mashq qilishni  |
|talab qiladi    |
```

For every word we compute "how long would the line be if it were added?" (`kerak`, needed): no space is needed before the first word, one before each of the others. This is the **greedy** method: each line takes as many words as possible. Text editors, terminals and SMS splitting use exactly this algorithm (*qatorlar* are the lines, *joriy* the current one).

:::warning
**Common mistakes**

- **Taking the second argument of C++'s `substr` for an index.** `s.substr(i, j)` is *j* characters starting at *i*, not up to index *j*.
- **Checking the result of `find` wrongly.** In C++ a failed search returns `string::npos`, not `-1` or `0`; in Python `find` returns `-1`, while `index` raises an error.
- **`getline` after `cin >>`.** The line end is left behind and `getline` reads an empty line; read it away with one `getline` in between.
- **Building a string with `+` in a loop (Java, Python).** *O*(*n*²); use `StringBuilder` or `join`.
:::

:::tip
Before solving a string problem, list the edge cases: an empty string, one character, only spaces, spaces at the start and end, identical characters. Write the expected result for each and test the program on them.
:::

## Summary

- The string functions do nearly the same jobs in the three languages, but their names and edge-case behaviour differ.
- C++ has `substr(i, length)`, Python `s[i:j]` and Java `substring(i, j)` — an end index.
- In Java and Python build strings in a loop with `StringBuilder` or `join`.
- Breaking a string problem into small functions (clean, check, split, build) simplifies it.
- When using `getline` after `cin >>`, do not forget the leftover line end.

## Check yourself

:::check
Q: What does `"  salom  ".strip()` return in Python?
A: `"salom"` — the leading and trailing spaces are removed.
Q: What does `string("dasturlash").substr(3, 4)` return in C++?
A: `"turl"` — 4 characters starting at index 3.
Q: What does `"a@b@c".indexOf('@') != "a@b@c".lastIndexOf('@')` mean in Java?
A: `@` occurs at least twice in the string.
Q: How is the string `aabbbc` compressed with RLE?
A: `a2b3c1`.
Q: Why is `s = s + c` inside a loop slow in Java?
A: Strings are immutable: every addition creates a new string and copies the old one — *O*(*n*²) in total.
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: string methods
The full list of methods: `strip`, `split`, `join`, `find`, `count` and others.
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
C++: std::string
`find`, `substr`, `rfind` and the other member functions.
:::
