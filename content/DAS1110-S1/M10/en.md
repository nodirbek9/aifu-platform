---
title: Strings and extended characters
summary: The character type and ASCII codes, character arithmetic, the C-style string and '\0', reading a whole line, Unicode, UTF-8 and extended characters (wchar_t), Uzbek letters in bytes.
---

## Why it matters

A computer stores only numbers. The letter "A", the sign "ʻ" and an emoji all sit in memory as numbers. Which number stands for which character is decided by the **encoding**. Misunderstanding encodings leads to problems programmers meet all the time: "OÊ»zbekiston" is printed instead of "Oʻzbekiston", a string is longer than expected, a password check breaks on Uzbek letters.

In this lesson we look at how characters are stored, how C-style strings are built and what to watch for when working with the letters of the Uzbek language.

:::note
**After this lesson you will be able to:**

- get the code of a character, rebuild a character from its code and do arithmetic on characters;
- explain the structure of a C-style string (`char[]` and `'\0'`);
- read a whole line with spaces correctly (`getline`, `nextLine`);
- classify characters (letter, digit, space);
- explain the difference between ASCII, Unicode and UTF-8 and why Uzbek letters take several bytes.
:::

## The character type and ASCII

A **character** is a single letter, digit, punctuation mark or control character. Every character is stored as a number — its **code**.

**ASCII** is the oldest table: it maps 128 characters (Latin letters, digits, punctuation, control characters) to the codes 0–127:

| Characters | Codes | To remember |
|---|---|---|
| `'0'` … `'9'` | 48 … 57 | `'0'` = 48 |
| `'A'` … `'Z'` | 65 … 90 | `'A'` = 65 |
| `'a'` … `'z'` | 97 … 122 | `'a'` = 97 |
| space `' '` | 32 | |
| new line `'\n'` | 10 | |

Important properties: the letters and the digits come **in sequence**; a small letter's code is 32 more than the capital's.

| Task | Python | Java | C++ |
|---|---|---|---|
| The code of a character | `ord('A')` → 65 | `(int) 'A'` → 65 | `(int) 'A'` → 65 |
| The character for a code | `chr(66)` → `'B'` | `(char) 66` → `'B'` | `(char) 66` → `'B'` |
| A digit character to a number | `ord(c) - ord('0')` | `c - '0'` | `c - '0'` |
| The next letter | `chr(ord(c) + 1)` | `(char) (c + 1)` | `(char) (c + 1)` |

In Java and C++ `char` is an integer type, so a number can be added to it. Python has no separate character type: a character is a string of length 1, hence `ord` and `chr`.

## The C-style string

C++ inherited the **C string** from C: an array of characters ending with the **null character** `'\0'` (code 0). The length of the string is not stored separately — it is counted up to `'\0'`.

```text
char s[] = "Salom";
index:     0    1    2    3    4    5
character: 'S'  'a'  'l'  'o'  'm'  '\0'
```

`"Salom"` has 5 letters, but the array takes 6 places. `strlen(s)` from `<cstring>` returns 5: it counts up to `'\0'`. If the `'\0'` is lost, `strlen` and printing carry on to the next random zero in memory — yet another form of going out of bounds.

C strings are dangerous and awkward to work with (they cannot be copied with `=`, and `==` compares addresses), so C++ usually uses the `string` class (M11). But understanding the C string matters: many libraries and operating-system functions work with exactly this form.

## Reading a whole line

`cin >> s` (C++) and `next()` (Java) read only **one word** — up to the first space. When the whole line is needed:

| Language | Reading a whole line |
|---|---|
| Python | `input()` — always a whole line |
| Java | `in.nextLine()` |
| C++ | `getline(cin, s)` |

The trap: if a number is read first (`cin >> k` or `nextInt()`) and then a line, the `'\n'` after the number stays in the input stream and `getline` / `nextLine` returns an empty line. The fix: `cin.ignore()` before `getline` in C++; in Java, read the number with `nextLine()` too and use `Integer.parseInt`.

## Unicode and UTF-8

ASCII covers only the English alphabet. **Unicode** is the standard that gives one number to the characters of every writing system in the world (over 150 000): `A` is U+0041, `Ж` U+0416, `ʻ` U+02BB, `😀` U+1F600.

The way a Unicode number is stored in memory is the **encoding**:

| Encoding | Bytes per character | Where |
|---|---|---|
| **UTF-8** | 1–4 bytes; ASCII characters take 1 byte | the web, files, Linux, C++ `string` |
| **UTF-16** | 2 or 4 bytes | Java `char` and `String`, Windows |
| **UTF-32** | always 4 bytes | rarely used |

In UTF-8 the Uzbek letter `oʻ` consists of two characters: `o` (1 byte) and `ʻ` (U+02BB, 2 bytes). That is why "Oʻzbekiston" is 11 characters but 12 bytes in UTF-8.

**Extended characters**: in C++ a `char` is 1 byte and cannot hold one Unicode character. For that there are `wchar_t` (the wide character; 2 bytes on Windows, 4 bytes on Linux), `wstring`, and the `char16_t` and `char32_t` types. In practice UTF-8 bytes are usually kept in a `string` and the characters are counted separately when needed.

| Language | The length of "Oʻzbekiston" | What is counted |
|---|---|---|
| Python | `len(s)` → 11 | Unicode characters |
| Java | `s.length()` → 11 | UTF-16 units (equal to the characters here) |
| C++ | `s.size()` → 12 | UTF-8 bytes |

## Worked example 1: the Caesar cipher

The first line has a shift *k* (0 ≤ *k* < 26), the second a text (Latin letters, spaces, punctuation). Shift every letter *k* places to the right in the alphabet (after Z comes A), keeping its case; leave other characters unchanged.

```python
k = int(input())
matn = input()

natija = []
for c in matn:
    if 'a' <= c <= 'z':
        c = chr((ord(c) - ord('a') + k) % 26 + ord('a'))
    elif 'A' <= c <= 'Z':
        c = chr((ord(c) - ord('A') + k) % 26 + ord('A'))
    natija.append(c)

print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = Integer.parseInt(in.nextLine().trim());
        String matn = in.nextLine();

        StringBuilder natija = new StringBuilder();
        for (char c : matn.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                c = (char) ((c - 'a' + k) % 26 + 'a');
            } else if (c >= 'A' && c <= 'Z') {
                c = (char) ((c - 'A' + k) % 26 + 'A');
            }
            natija.append(c);
        }

        System.out.println(natija);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int k;
    cin >> k;
    cin.ignore();                    // drop the '\n' after the number
    string matn;
    getline(cin, matn);

    for (char& c : matn) {           // & — to change the character in place
        if (c >= 'a' && c <= 'z') {
            c = (char) ((c - 'a' + k) % 26 + 'a');
        } else if (c >= 'A' && c <= 'Z') {
            c = (char) ((c - 'A' + k) % 26 + 'A');
        }
    }

    cout << matn << endl;
    return 0;
}
```

For the input `3` and `Salom, Dunyo!` (*matn* is the text, *natija* the result):

1. `'S'`: code 83, `83 - 65 = 18` (its place in the alphabet), `(18 + 3) % 26 = 21`, `21 + 65 = 86` → `'V'`.
2. `'a'` → `'d'`, `'l'` → `'o'`, `'o'` → `'r'`, `'m'` → `'p'`.
3. `','`, the space and `'!'` do not change.
4. `'y'`: place 24, `(24 + 3) % 26 = 1` → `'b'` — back to the start of the alphabet.

The output is `Vdorp, Gxqbr!`. To decrypt, the same program is run with the shift `26 - k`. The remainder (`% 26`) turns the alphabet into a "ring".

## Worked example 2: classifying characters

A line of text (ASCII characters) is given. Print the number of letters, digits, spaces and other characters in it.

```python
qator = input()

harf = raqam = probel = boshqa = 0
for c in qator:
    if 'a' <= c <= 'z' or 'A' <= c <= 'Z':
        harf += 1
    elif '0' <= c <= '9':
        raqam += 1
    elif c == ' ':
        probel += 1
    else:
        boshqa += 1

print(harf, raqam, probel, boshqa)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String qator = in.nextLine();

        int harf = 0, raqam = 0, probel = 0, boshqa = 0;
        for (int i = 0; i < qator.length(); i++) {
            char c = qator.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                harf++;
            } else if (c >= '0' && c <= '9') {
                raqam++;
            } else if (c == ' ') {
                probel++;
            } else {
                boshqa++;
            }
        }

        System.out.println(harf + " " + raqam + " " + probel + " " + boshqa);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string qator;
    getline(cin, qator);

    int harf = 0, raqam = 0, probel = 0, boshqa = 0;
    for (char c : qator) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            harf++;
        } else if (c >= '0' && c <= '9') {
            raqam++;
        } else if (c == ' ') {
            probel++;
        } else {
            boshqa++;
        }
    }

    cout << harf << " " << raqam << " " << probel << " " << boshqa << endl;
    return 0;
}
```

For the input `Room 214, AIFU-2026`: the letters R, o, o, m, A, I, F, U — 8; the digits 2, 1, 4, 2, 0, 2, 6 — 7; spaces — 2; others, `,` and `-` — 2. The output is `8 7 2 2`.

The characters are checked with ranges — which works because the letters are in sequence in ASCII. There are standard functions too: Python `c.isalpha()`, `c.isdigit()`; Java `Character.isLetter(c)`; C++ `isalpha(c)`, `isdigit(c)` (`<cctype>`). They may judge Uzbek and other letters differently, which is why this example uses ranges.

## Worked example 3: characters and bytes

A line of text (UTF-8) is given. Print the number of characters (Unicode) and bytes in it.

```python
s = input()

belgilar = len(s)                       # Unicode characters
baytlar = len(s.encode("utf-8"))        # UTF-8 bytes

print("Characters:", belgilar, "bytes:", baytlar)
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        String s = in.nextLine();

        int belgilar = s.codePointCount(0, s.length());          // Unicode characters
        int baytlar = s.getBytes(StandardCharsets.UTF_8).length; // UTF-8 bytes

        System.out.println("Characters: " + belgilar + " bytes: " + baytlar);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);                 // a string holds the UTF-8 bytes

    int baytlar = s.size();
    int belgilar = 0;
    for (unsigned char c : s) {
        if ((c & 0xC0) != 0x80) {    // continuation bytes 10xxxxxx are not counted
            belgilar++;
        }
    }

    cout << "Characters: " << belgilar << " bytes: " << baytlar << endl;
    return 0;
}
```

For the input `Oʻzbekiston` the output is `Characters: 11 bytes: 12`. For `Salom`: `Characters: 5 bytes: 5` — in ASCII text characters and bytes are equal.

In UTF-8 every character consists of one "lead" byte and 0–3 "continuation" bytes; continuation bytes look like `10xxxxxx` in binary. The C++ version counts only the bytes that are not continuations: `c & 0xC0` keeps the top two bits, and `0x80` is `10000000`. `ʻ` (U+02BB) is `CA BB` in UTF-8 — two bytes, one of them a continuation byte.

:::warning
**Common mistakes**

- **Treating `'5'` as the number 5.** The code of `'5'` is 53; the digit's value is `'5' - '0'`.
- **`getline` after `cin >> k`.** The leftover `'\n'` makes it read an empty line; add `cin.ignore()`.
- **Leaving no room for `'\0'` in a C string.** `char s[5] = "Salom";` needs 6 places.
- **Taking `s.size()` in C++ as the number of characters.** In UTF-8 text it is the number of bytes; Uzbek `ʻ` and Cyrillic letters take 2 bytes each.
- **Writing `'` (an apostrophe) instead of the Uzbek `ʻ`.** It is a different character: searching and sorting break.
:::

:::tip
Always test a program that works with text on Uzbek (`oʻ`, `gʻ`), Russian and emoji text. Code that worked on ASCII can break unexpectedly on another writing system.
:::

## Summary

- A character is stored in memory as a number — its code; in ASCII `'0'` = 48, `'A'` = 65, `'a'` = 97, and the letters are in sequence.
- In Java and C++ `char` is an integer; Python uses `ord` / `chr`.
- A C string is a `char` array ending with `'\0'`; `strlen` counts its length.
- A whole line is read with `input()`, `nextLine()`, `getline()`; mind the `'\n'` left after a number.
- Unicode gives every character a number, UTF-8 stores it in 1–4 bytes; the Uzbek `ʻ` takes 2 bytes.
- In C++ `wchar_t` and `wstring` are the extended characters; a `string` holds UTF-8 bytes.

## Check yourself

:::check
Q: What is the difference between the codes of `'a'` and `'A'`, and how can it be used?
A: 32. To turn a small letter into a capital, subtract 32 from its code: `(char) (c - 32)` (Latin letters only).
Q: How many bytes does the array `char s[] = "Hi";` take?
A: 3: `'H'`, `'i'` and `'\0'`.
Q: In C++, why can *s* be empty after `cin >> k; getline(cin, s);`?
A: `cin >> k` leaves the `'\n'` after the number in the stream, and `getline` reads that empty line. It needs `cin.ignore()` first.
Q: What does `'z'` become in the Caesar cipher with *k* = 3?
A: `'c'`: place 25, (25 + 3) % 26 = 2.
Q: Why does "Oʻzbekiston" take 12 bytes in UTF-8?
A: The 10 ASCII letters take 1 byte each and `ʻ` (U+02BB) takes 2: 10 + 2 = 12.
:::

:::link https://www.asciitable.com/
The ASCII table
All 128 ASCII characters and their codes.
:::

:::link https://en.wikipedia.org/wiki/UTF-8
Wikipedia: UTF-8
How the UTF-8 encoding is built and what its bytes look like.
:::
