---
title: Strings of the String type
summary: C++ string, Java String and Python str: length, concatenation, substrings, searching, replacing, comparing, case, splitting into words; immutable strings and StringBuilder.
---

## Why it matters

Checking a name, password rules, finding the `@` in an e-mail address, splitting the columns of a CSV file, counting the words of a text, a search engine — a large share of programs work with text. With the C-style strings of lesson M10 this is hard and dangerous: you have to track the length by hand, think about `'\0'` and fear going out of bounds.

Modern languages have a **string class** for this: `string` in C++, `String` in Java, `str` in Python. They know their own length, manage memory themselves and come with ready-made operations for searching, cutting and replacing.

:::note
**After this lesson you will be able to:**

- create a string, join strings, get the length and individual characters;
- extract a substring, search inside a string and replace parts of it;
- compare strings and convert them to upper or lower case;
- split a text into words and work with them;
- explain why strings are immutable in Java and Python and why `StringBuilder` / `join` are needed for repeated joining.
:::

## The main operations

| Operation | Python (`str`) | Java (`String`) | C++ (`string`) |
|---|---|---|---|
| Creating | `s = "Salom"` | `String s = "Salom";` | `string s = "Salom";` |
| Length | `len(s)` | `s.length()` | `s.size()` or `s.length()` |
| Character *i* | `s[i]` | `s.charAt(i)` | `s[i]` |
| Concatenation | `s + t` | `s + t` | `s + t` |
| Substring (from *i*, *k* characters) | `s[i:i + k]` | `s.substring(i, i + k)` | `s.substr(i, k)` |
| Search (first position) | `s.find(t)` → −1 | `s.indexOf(t)` → −1 | `s.find(t)` → `string::npos` |
| Equality | `s == t` | `s.equals(t)` | `s == t` |
| Dictionary-order comparison | `s < t` | `s.compareTo(t) < 0` | `s < t` |
| To upper case | `s.upper()` | `s.toUpperCase()` | `toupper` on each character |
| Replace (all) | `s.replace(a, b)` | `s.replace(a, b)` | a loop of `find` + `replace` |

Note: for a substring Python and Java take the **end index** (not included), while C++ takes the **length**: `substr(2, 3)` is 3 characters starting at index 2.

Python's slicing is a powerful tool: `s[:3]` — the first 3 characters, `s[-3:]` — the last 3, `s[::-1]` — the reversed string.

## Comparing strings in Java

In Java `==` compares not the **content** of strings but their **references**: for the same text in two different objects `==` can be false. To compare content always use `s.equals(t)` (ignoring case — `s.equalsIgnoreCase(t)`). This is one of the best-known Java bugs.

In C++ and Python `==` compares content.

## Immutable strings

In Java and Python a string is **immutable**: once created, its characters cannot be changed. `s.upper()` and `s.replace(...)` do not change the original string — they return a **new** one. `s[0] = 'X'` is an error in Python.

That is why doing `s = s + x` many times in a loop is slow: each time a new string is created and the old one copied. The fix:

| Language | Building a string from many pieces |
|---|---|
| Python | collect the pieces in a list and finish with `"".join(pieces)` |
| Java | `StringBuilder sb = new StringBuilder(); sb.append(x);` … `sb.toString()` |
| C++ | `string` is mutable: `s += x` is fast |

C++'s `string` is mutable: `s[0] = 'X'` is allowed.

## Splitting into words

Splitting a text into words by spaces:

| Language | How |
|---|---|
| Python | `sozlar = s.split()` — also correct with several spaces in a row |
| Java | `String[] w = s.trim().split("\\s+");` — a regular expression: one or more spaces |
| C++ | `istringstream ss(s);` and `while (ss >> w)` — `<sstream>` |

In Java, splitting an empty string gives an array with one empty element — check that case separately.

## Worked example 1: a palindrome

A line of text is given. Decide whether it is a palindrome (reads the same left to right and right to left), counting only the letters and ignoring case.

```python
s = input()

harflar = ""
for c in s:
    if c.isalpha():
        harflar += c.lower()

print("yes" if harflar == harflar[::-1] else "no")
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

        System.out.println(harflar.equals(teskari) ? "yes" : "no");
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

    cout << (harflar == teskari ? "yes" : "no") << endl;
    return 0;
}
```

For the input `A man, a plan, a canal: Panama`: the letters (*harflar*) are `amanaplanacanalpanama`, and the reversed string (*teskari*) is the same. The output is `yes`. For `Salom`: `salom` ≠ `molas`, so `no`.

In Java `sb.reverse()` reverses the `StringBuilder` itself, which is why `harflar` is taken **before** it. In C++ `string teskari(harflar.rbegin(), harflar.rend())` builds a new string by reading from the end to the start. Java compares with `equals`.

## Worked example 2: word statistics

A line of text is given (words separated by one or more spaces). Print the number of words, the longest word (the first one if there are several) and every word with a capital first letter.

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

For the input `  dasturlash   tili  PYTHON va java ` (*sozlar* are the words, *eng_uzun* the longest):

1. The words: `dasturlash`, `tili`, `PYTHON`, `va`, `java` — 5; the extra spaces are ignored.
2. The longest: `dasturlash` (10 letters).
3. With capitals: `Dasturlash Tili Python Va Java`.

In C++ `istringstream` lets a string be read just like `cin`: `ss >> w` takes the next word and skips the spaces.

## Worked example 3: search and replace

Three lines are given: a text, the substring to look for and the string to put in its place. Count how many times the substring occurs (without overlaps) and print the text with every occurrence replaced.

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
        matn.replace(i, eski.size(), yangi);     // replace in place
        i = matn.find(eski, i + yangi.size());   // after the inserted part
    }

    cout << soni << endl;
    cout << matn << endl;
    return 0;
}
```

Input (*matn* — the text, *eski* — old, *yangi* — new):

```text
Java va Python, Python va C++
Python
Go
```

The output is `2` and `Java va Go, Go va C++`.

The Java version continues searching with `indexOf(eski, start)`: after each occurrence the search starts where it ended. The C++ version replaces each occurrence at once with `replace` and continues searching after the **inserted** substring — otherwise, replacing "a" with "aa", for example, could fall into an infinite loop. In Python `count` and `replace` are ready-made.

:::warning
**Common mistakes**

- **Comparing strings with `==` in Java.** Use `equals` for content.
- **Mixing up `substr` and `substring`.** In C++ the second argument is a length, in Java an end index.
- **Not saving the result.** In Python and Java `s.upper()` returns a new string; you must write `s = s.upper()`.
- **Comparing the result of `find` with `-1` in C++.** The correct value is `string::npos`.
- **`s = s + x` in a loop (Java).** Very slow on large text; use `StringBuilder`.
- **`w[0]` of an empty string.** An empty string has no character 0; check the length first.
:::

:::tip
Test every function that works with strings on the edge cases: an empty string, one character, only spaces, the searched part at the start and at the end. Almost all text bugs live there.
:::

## Summary

- `string` (C++), `String` (Java) and `str` (Python) manage their length and memory themselves.
- The main operations: length, character, concatenation, substring, search, replace, comparison.
- Substrings: Python and Java take an end index, C++ a length; not found gives −1 or `string::npos`.
- In Java content is compared with `equals`.
- Strings in Java and Python are immutable: methods return a new string; repeated building needs `StringBuilder` / `join`.
- Splitting into words: `split()`, `split("\\s+")`, `istringstream`.

## Check yourself

:::check
Q: With `s = "Dasturlash"`, what do `s[3:7]` in Python and `s.substr(3, 4)` in C++ give?
A: Both give `turl`: 4 characters from index 3.
Q: In Java, what is `new String("ab") == "ab"`, and why?
A: `false`: `==` compares references and these are two different objects. `"ab".equals(new String("ab"))` is `true`.
Q: What does Python print for `s = "salom"; s.upper(); print(s)`?
A: `salom`: the string is immutable; `upper()` returns a new string, but it was not saved.
Q: Why is `StringBuilder` recommended over `s = s + x` in a Java loop?
A: Every `+` creates a new string and copies the old one in full; *n* joins take ~*n*² work. `StringBuilder` works with one mutable buffer.
Q: How many words does Python's `split()` make of `"  bir  ikki "`?
A: 2: `["bir", "ikki"]`.
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: string methods
`split`, `join`, `find`, `replace`, `upper` and others.
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
cppreference: std::string
Every method of the C++ `string` class.
:::

:::link https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
Java 21: the String class
The official documentation of the `String` methods.
:::
