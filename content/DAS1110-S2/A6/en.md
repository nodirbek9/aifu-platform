---
title: Strings and extended characters. ASCII codes
summary: The layout of the ASCII table, moving between a character and its code, switching case through codes, turning a digit character into a number, counting letter frequencies in an array indexed by code; care with extended characters.
---

## Why it matters

To a computer every character is a number. `A` is 65, `a` is 97, `0` is 48, a space is 32. In the first semester we met the character type and the ASCII table. Knowing the **layout** of this table makes many problems very simple: letters and digits sit in the table one after another, so ordinary arithmetic can be done on characters.

In this practical session we practise working with character codes: switching a letter's case, turning a string of digits into a number, and counting how many times each letter occurs — all without ready functions, through codes only.

:::note
**After this session you will be able to:**

- name the main ranges of the ASCII table (digits, uppercase and lowercase letters);
- go from a character to its code and back in three languages;
- switch a letter's case through the difference of codes (32);
- turn a string of digits into a number without a ready function;
- count letter frequencies in an array indexed by `code − 'a'`.
:::

## The layout of the ASCII table

ASCII is 128 characters with codes from 0 to 127. The ranges needed most often:

| Characters | Codes | To remember |
|---|---|---|
| control characters (`\n` = 10, `\t` = 9) | 0–31 | not printed |
| space | 32 | |
| `0` … `9` | 48–57 | `'0'` + digit |
| `A` … `Z` | 65–90 | `'A'` + letter position |
| `a` … `z` | 97–122 | `'a'` + letter position |

Important properties:

- a digit character's value: `c - '0'` (for example, `'7' - '0'` = 7);
- a letter's position in the alphabet: `c - 'a'` (for example, `'d' - 'a'` = 3);
- uppercase and lowercase codes differ by **32**: `'a' - 'A'` = 32.

Characters with codes above 128 (Uzbek `ʻ`, Cyrillic letters, `€`) are not in ASCII — they are in Unicode and are written in UTF-8 with several bytes. So the methods of this session are correct only for ASCII letters.

## Moving between a character and its code

| Operation | Python | Java | C++ |
|---|---|---|---|
| character → code | `ord(c)` | `(int) c` | `(int) c` |
| code → character | `chr(k)` | `(char) k` | `(char) k` |
| a digit's value | `ord(c) - ord('0')` | `c - '0'` | `c - '0'` |
| is it a letter? | `'a' <= c <= 'z'` | `c >= 'a' && c <= 'z'` | `c >= 'a' && c <= 'z'` |

In Java and C++ `char` is an integer type, so `c - '0'` works directly. In Python a character is a string of length 1, not a number: arithmetic needs `ord` and `chr`. In Java the result of `char + int` is an `int` — if a character is needed, it is converted back with `(char)`.

## Worked example 1: switching case

One line is given (ASCII characters). Change every lowercase letter to uppercase and every uppercase one to lowercase; other characters stay the same. Do not use the ready `upper`/`lower` functions — codes only.

```python
satr = input()
natija = []
for c in satr:
    k = ord(c)
    if 97 <= k <= 122:              # 'a'..'z'
        natija.append(chr(k - 32))
    elif 65 <= k <= 90:             # 'A'..'Z'
        natija.append(chr(k + 32))
    else:
        natija.append(c)
print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine();
        StringBuilder natija = new StringBuilder();
        for (char c : satr.toCharArray()) {
            if (c >= 'a' && c <= 'z') natija.append((char) (c - 32));
            else if (c >= 'A' && c <= 'Z') natija.append((char) (c + 32));
            else natija.append(c);
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
    string satr;
    getline(cin, satr);
    for (char& c : satr) {                     // & — we change the string itself
        if (c >= 'a' && c <= 'z') c = c - 32;
        else if (c >= 'A' && c <= 'Z') c = c + 32;
    }
    cout << satr << endl;
    return 0;
}
```

For `Salom, Dunyo! 2024` the output is `sALOM, dUNYO! 2024`. In Python `ord('a')` and `ord('z')` can be written instead of `97` and `122` — there is no need to memorise the codes. In C++ the string's characters are changed directly; in Java and Python strings are immutable, so a new string is built (*satr* is the line, *natija* the result).

## Worked example 2: turning a string of digits into a number

One line holds a signed integer (it may start with `-`, up to 18 digits long). Turn it into a number without ready functions such as `int()`, `parseInt` or `stoi`, and print the number and the sum of its digits. If the line contains anything other than digits, print `Error`.

```python
satr = input().strip()
manfiy = satr.startswith("-")
if manfiy:
    satr = satr[1:]

son = 0
yigindi = 0
togri = len(satr) > 0
for c in satr:
    if not ("0" <= c <= "9"):
        togri = False
        break
    raqam = ord(c) - ord("0")
    son = son * 10 + raqam                  # Horner's scheme
    yigindi += raqam

if togri:
    print(-son if manfiy else son, yigindi)
else:
    print("Error")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine().trim();
        boolean manfiy = satr.startsWith("-");
        if (manfiy) satr = satr.substring(1);

        long son = 0;
        int yigindi = 0;
        boolean togri = satr.length() > 0;
        for (char c : satr.toCharArray()) {
            if (c < '0' || c > '9') {
                togri = false;
                break;
            }
            int raqam = c - '0';
            son = son * 10 + raqam;                 // Horner's scheme
            yigindi += raqam;
        }

        if (togri) System.out.println((manfiy ? -son : son) + " " + yigindi);
        else System.out.println("Error");
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    cin >> satr;
    bool manfiy = !satr.empty() && satr[0] == '-';
    if (manfiy) satr = satr.substr(1);

    long long son = 0;
    int yigindi = 0;
    bool togri = !satr.empty();
    for (char c : satr) {
        if (c < '0' || c > '9') {
            togri = false;
            break;
        }
        int raqam = c - '0';
        son = son * 10 + raqam;                     // Horner's scheme
        yigindi += raqam;
    }

    if (togri) cout << (manfiy ? -son : son) << " " << yigindi << endl;
    else cout << "Error" << endl;
    return 0;
}
```

For `-4071` the output is `-4071 12`; for `12a5` it is `Error`. `son = son * 10 + raqam` is **Horner's scheme**: for `"407"` it goes 0 → 4 → 40 → 407. Exactly this process runs inside the ready `int()` functions (*son* is the number, *raqam* a digit, *manfiy* negative, *togri* valid). A line made of `-` alone also gives `Error`: no digit follows it.

## Worked example 3: letter frequencies

One line of text is given. Ignoring case, count how many times each Latin letter occurs and print the letters that occur, in alphabetical order, as a histogram: the letter, its count and that many stars.

```python
satr = input()
soni = [0] * 26                             # soni[0] is 'a', soni[25] is 'z'
for c in satr:
    if "A" <= c <= "Z":
        c = chr(ord(c) + 32)                # to lowercase
    if "a" <= c <= "z":
        soni[ord(c) - ord("a")] += 1

for i in range(26):
    if soni[i] > 0:
        print(chr(ord("a") + i), soni[i], "*" * soni[i])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine();
        int[] soni = new int[26];                       // soni[0] is 'a', soni[25] is 'z'
        for (char c : satr.toCharArray()) {
            if (c >= 'A' && c <= 'Z') c = (char) (c + 32);   // to lowercase
            if (c >= 'a' && c <= 'z') soni[c - 'a']++;
        }

        for (int i = 0; i < 26; i++) {
            if (soni[i] > 0) {
                System.out.println((char) ('a' + i) + " " + soni[i] + " " + "*".repeat(soni[i]));
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int soni[26] = {0};                             // soni[0] is 'a', soni[25] is 'z'
    for (char c : satr) {
        if (c >= 'A' && c <= 'Z') c = c + 32;       // to lowercase
        if (c >= 'a' && c <= 'z') soni[c - 'a']++;
    }

    for (int i = 0; i < 26; i++) {
        if (soni[i] > 0) {
            cout << (char) ('a' + i) << " " << soni[i] << " " << string(soni[i], '*') << endl;
        }
    }
    return 0;
}
```

Input:

```text
Banan va Ananas!
```

Output:

```text
a 6 ******
b 1 *
n 4 ****
s 1 *
v 1 *
```

A 26-element array was used instead of a dictionary (`dict`, `Map`): the letter's code `c - 'a'` becomes the index directly, and the letters come out in alphabetical order automatically. In Java `(char) ('a' + i)` matters: `'a' + i` is an `int`, and without the cast a number would be printed. This method is widely used in cryptography (breaking the Caesar cipher), text analysis and compression.

:::warning
**Common mistakes**

- **Taking `char + int` for a character in Java.** `'a' + 1` is the number 98; for a character, `(char) ('a' + 1)`.
- **Subtracting a number from a character in Python.** `c - '0'` fails; write `ord(c) - ord('0')`.
- **Taking a digit character for the number itself.** The code of `'7'` is 55, its value 7; do not forget `c - '0'`.
- **Applying the ASCII method to Uzbek letters.** Characters such as `ʻ` or `ш` are above 127; for them `c - 'a'` goes outside the array — check `'a' <= c <= 'z'` first.
:::

:::tip
There is no need to memorise the codes: write character literals such as `'A'`, `'a'`, `'0'` — the compiler turns them into codes, and the program is easier to read.
:::

## Summary

- In ASCII the digits (48–57), uppercase letters (65–90) and lowercase letters (97–122) are consecutive.
- Character → code: `ord` / `(int)`; code → character: `chr` / `(char)`.
- A digit's value is `c - '0'`, a letter's position `c - 'a'`, the case difference 32.
- A string of digits is turned into a number with Horner's scheme: `son = son * 10 + raqam`.
- For letter frequencies a 26-element array with the index `c - 'a'` is convenient.

## Check yourself

:::check
Q: What is `'5' - '0'`?
A: 5: the code of `'5'` is 53, that of `'0'` is 48.
Q: How can `'G'` be turned into lowercase through its code?
A: By adding 32 to its code: `(char) ('G' + 32)` = `'g'`.
Q: What does `System.out.println('a' + 1);` print in Java?
A: 98 — the sum of a `char` and an `int` is an `int`.
Q: In what steps does Horner's scheme turn the string `"305"` into a number?
A: 0 → 3 → 30 → 305.
Q: Why must you check that a character is a letter before `soni[c - 'a']` when counting letters?
A: For other characters `c - 'a'` is negative or greater than 25 and goes outside the array.
:::

:::link https://en.wikipedia.org/wiki/ASCII#Printable_characters
The ASCII table
Printable characters and their codes.
:::

:::link https://docs.python.org/3/library/functions.html#ord
Python: ord and chr
Functions for moving between a character and its Unicode code.
:::
