---
title: Working with strings
summary: Independent work: splitting a string into words and joining it back, extra spaces, changing characters inside a word, breaking a string problem into functions; the sample problem — reversing the order of words and the longest word; the assignment — writing every word with a capital letter.
---

## Why it matters

Most text-processing programs work on the same scheme: the string is **split into words**, every word is processed separately, and the result is **joined back**. Search engines, spell-checkers, headline formatting — all work this way. In this independent work you practise applying this scheme on your own in three languages.

:::note
**After this independent work you will be able to:**

- split a string into words correctly despite extra spaces;
- join a list of words back with the needed separator;
- change individual characters inside a word (for example, the first letter);
- build a table of tests for a string problem that includes the edge cases.
:::

## A short review of the topic

The "split — process — join" scheme in three languages:

| Stage | Python | Java | C++ |
|---|---|---|---|
| Splitting | `s.split()` — on any number of spaces | `s.trim().split("\\s+")` | `istringstream` and `>>` |
| Working on a word | `soz.upper()`, `soz[0]`, `soz[1:]` | `soz.toUpperCase()`, `soz.charAt(0)`, `soz.substring(1)` | `toupper(soz[0])`, `soz.substr(1)` |
| Joining | `" ".join(sozlar)` | `String.join(" ", sozlar)` | a loop with `+=` |

Important subtleties:

1. In Python `s.split()` (without an argument) drops leading, trailing and repeated spaces; `s.split(" ")`, however, gives **empty words**.
2. In Java `split("\\s+")` returns an empty string as the first element if the string starts with a space — hence `trim()` first.
3. In C++ the `>>` operator skips spaces by itself — the simplest way of splitting into words.

## The sample problem: reversing the order of words

One line of text is given (there may be several spaces between words, and spaces at the start and end). Print the words in reverse order, separated by single spaces; on the next line print the longest word (the first one if there are several).

```python
def sozlarga_ajrat(satr):
    return satr.split()                 # extra spaces are dropped


def eng_uzun(sozlar):
    natija = sozlar[0]
    for soz in sozlar:
        if len(soz) > len(natija):      # strict > — the first one stays
            natija = soz
    return natija


sozlar = sozlarga_ajrat(input())
print(" ".join(reversed(sozlar)))
print(eng_uzun(sozlar))
```
```java
import java.util.Scanner;

public class Main {
    static String[] sozlargaAjrat(String satr) {
        return satr.trim().split("\\s+");    // trim — so that no empty word comes first
    }

    static String engUzun(String[] sozlar) {
        String natija = sozlar[0];
        for (String soz : sozlar) {
            if (soz.length() > natija.length()) natija = soz;   // strict > — the first one stays
        }
        return natija;
    }

    public static void main(String[] args) {
        String[] sozlar = sozlargaAjrat(new Scanner(System.in).nextLine());
        StringBuilder sb = new StringBuilder();
        for (int i = sozlar.length - 1; i >= 0; i--) {
            sb.append(sozlar[i]);
            if (i > 0) sb.append(' ');
        }
        System.out.println(sb);
        System.out.println(engUzun(sozlar));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<string> sozlargaAjrat(const string& satr) {
    istringstream oqim(satr);
    vector<string> sozlar;
    string soz;
    while (oqim >> soz) sozlar.push_back(soz);     // >> skips spaces by itself
    return sozlar;
}

string engUzun(const vector<string>& sozlar) {
    string natija = sozlar[0];
    for (const string& soz : sozlar) {
        if (soz.size() > natija.size()) natija = soz;   // strict > — the first one stays
    }
    return natija;
}

int main() {
    string satr;
    getline(cin, satr);
    vector<string> sozlar = sozlargaAjrat(satr);
    for (int i = (int) sozlar.size() - 1; i >= 0; i--) {
        cout << sozlar[i];
        if (i > 0) cout << ' ';
    }
    cout << endl << engUzun(sozlar) << endl;
    return 0;
}
```

The table of tests:

| Input | Expected result | What it checks |
|---|---|---|
| `bugun   havo  juda yaxshi` | `yaxshi juda havo bugun`, `yaxshi` | repeated spaces |
| `␣␣salom dunyo␣` | `dunyo salom`, `salom` | spaces at the start and end; equal length — the first one |
| `yolgiz` | `yolgiz`, `yolgiz` | a single word |

(`␣` stands for a space.) In all three languages the problem is split into three independent parts: splitting (*sozlarga_ajrat*), finding the longest (*eng_uzun*), and joining in reverse order. Each part can be checked separately.

## The independent work assignment

Solve the assignment below: start **every word with a capital letter** and make the remaining letters lowercase; leave a single space between words (`  dASturLASH   tili ` → `Dasturlash Tili`). What to hand in:

1. **A description of the functions** — at least two functions: formatting one word, and formatting the whole line.
2. **The program** — in Python, Java or C++; submitted in the assignment window below, and it must pass the automatic tests.
3. **A table of tests** — at least 5 tests: the ordinary case, repeated and edge spaces, one-letter words, words in all capitals, a single word.
4. **A conclusion** — 3–5 sentences: which functions of your language you used, and which edge case caused the most mistakes.

:::warning
**Grading criteria and common mistakes**

- Do not use Python's ready `str.title()` — it also capitalises after apostrophes and digits, and the point of the assignment is to write the formatting yourself.
- For a one-letter word (`a`), `soz[1:]` / `substring(1)` gives an empty string — not a mistake, but test it.
- No extra space must remain at the end of the result.
- At least one word is guaranteed, but the line may start with a space.
:::

:::tip
First write the function that formats one word and test it on its own with a few words (`dASTUR`, `a`, `Ok`). Once it works, the function for the whole line becomes just "split — apply to each — join".
:::

## Summary

- The basic scheme of text processing: split into words → process every word → join back.
- Extra spaces: Python `split()`, Java `trim().split("\\s+")`, C++ `>>`.
- Characters inside a word are changed through indices and substrings.
- Breaking a problem into small functions lets each part be checked separately.

## Check yourself

:::check
Q: How do the results of `"  a  b ".split()` and `"  a  b ".split(" ")` differ in Python?
A: The first gives `["a", "b"]`, the second includes empty strings: `["", "", "a", "", "b", ""]`.
Q: Why is `trim()` called before `split("\\s+")` in Java?
A: If the string starts with a space, `split` returns an empty string as its first element.
Q: What is the simplest way to split a string into words in C++?
A: Create an `istringstream` and read from it word by word with `>>`.
Q: Which comparison keeps the first of several words of equal length?
A: The strict `>`: a later word of equal length does not replace the result.
:::

:::link https://docs.python.org/3/library/stdtypes.html#str.split
Python: str.split
The rules for splitting a string and the effect of the `sep` argument.
:::
