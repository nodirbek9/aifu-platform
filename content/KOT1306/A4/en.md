---
title: Data encoding methods
summary: The jobs of line coding — synchronisation, DC component, bandwidth; the NRZ, NRZI, Manchester, differential Manchester, AMI and MLT-3 codes; block coding — 4B/5B, 8B/10B, scrambling; bit stuffing and the HDLC flag; analysing codes with signal diagrams.
---

## Why it matters

What crosses a cable is not "1" and "0" but voltage changes. For the receiver to turn them back into bits, both sides must use the same **code**. But the plain "1 — high, 0 — low" approach confuses the receiver's clock in long runs of zeros: 50 zeros may be read as 49 or 51. In this practical session we study the main line codes through signal diagrams, and then model with programs the **block coding** (4B/5B) and **bit stuffing** methods that solve these problems.

:::note
**After this session you will be able to:**

- explain the requirements for a good line code (synchronisation, no DC component, narrow bandwidth);
- draw the signal diagram for the NRZ, NRZI, Manchester, differential Manchester, AMI and MLT-3 codes;
- show how 4B/5B coding removes long runs of zeros;
- apply bit stuffing so that the frame flag never appears inside the data.
:::

## Requirements for a good code

| Requirement | Meaning |
|---|---|
| **Self-clocking** | the signal must have enough transitions — the receiver corrects its clock by them |
| **No DC component** (DC balance) | high and low levels must be equal on average — transformers do not pass direct current |
| **Narrow spectrum** | the fewer signal changes per second, the lower the demands on the cable |
| **Error detection** | in some codes, forbidden combinations signal an error |
| **Simplicity and low cost** | the encoder and decoder must be simple |

## The main line codes

Below are the levels for the sequence `10110001` (`+` — high, `-` — low, `0` — the zero level). In Manchester code each bit has two half-periods.

| Code | Rule | `1 0 1 1 0 0 0 1` |
|---|---|---|
| **NRZ-L** | 1 → `+`, 0 → `-` | `+ - + + - - - +` |
| **NRZI** | 1 → the level flips, 0 → it stays (starting at `-`) | `+ + - + + + + -` |
| **Manchester** (IEEE 802.3) | 0 → `+-`, 1 → `-+` (always a transition mid-bit) | `-+ +- -+ -+ +- +- +- -+` |
| **AMI** | 0 → `0`, 1 → alternately `+` and `-` | `+ 0 - + 0 0 0 -` |
| **MLT-3** | 0 → stays, 1 → the next level in the cycle `0 → + → 0 → -` (starting at `0`) | `+ + 0 - - - - 0` |

| Code | Synchronisation | DC balance | Where it is used |
|---|---|---|---|
| NRZ-L | lost in long runs of 0s and 1s | no | RS-232, internal buses |
| NRZI | lost in long runs of 0s (no problem with 1s) | no | USB, 100BASE-FX (with 4B/5B) |
| Manchester | a transition in every bit — **ideal** | yes | 10BASE-T Ethernet |
| Differential Manchester | a transition in every bit; 0 — an extra transition at the start of the bit | yes | Token Ring |
| AMI | lost in long runs of 0s | yes | T1/E1 lines |
| MLT-3 | lost in long runs of 0s | partly | 100BASE-TX (with 4B/5B) |

The price of Manchester code is a signal change rate **twice** that of NRZ. That is why 10 Mbit/s Ethernet used it, while faster standards chose another way: NRZI or MLT-3 plus **block coding**.

Implementing the NRZ, NRZI and Manchester codes in a program is this session's **independent task** (KOT-A4 below).

## Block coding: 4B/5B

NRZI gives a transition on 1s but stays silent in long runs of zeros. **4B/5B** replaces every 4 bits with a 5-bit code word; the code words are chosen so that **no more than three zeros come in a row** in the stream (each word has at most one leading zero and at most two trailing zeros). The price is 25% extra bits: 100 Mbit/s of data needs a 125 Mbaud line.

| 4 bits | 5 bits | 4 bits | 5 bits |
|---|---|---|---|
| 0000 | 11110 | 1000 | 10010 |
| 0001 | 01001 | 1001 | 10011 |
| 0010 | 10100 | 1010 | 10110 |
| 0011 | 10101 | 1011 | 10111 |
| 0100 | 01010 | 1100 | 11010 |
| 0101 | 01011 | 1101 | 11011 |
| 0110 | 01110 | 1110 | 11100 |
| 0111 | 01111 | 1111 | 11101 |

Some of the remaining 16 five-bit combinations are control symbols (for example, `11111` — IDLE, the start and end of a frame), and the rest are forbidden — their appearance signals an error. Gigabit Ethernet uses the same idea as **8B/10B** (25% extra), and 10G uses **64B/66B** (only 3%). Another approach is **scrambling**: the bits are XORed with a pseudo-random sequence, and long identical runs become very unlikely.

## Bit stuffing

In protocols such as HDLC and PPP the start and end of a frame are marked by the **flag** `01111110`. If six 1s in a row also appear inside the data, the receiver takes them for a flag. The solution: the sender always inserts one `0` **after five 1s in a row**; the receiver removes the `0` following five 1s. As a result, six 1s never appear in the data.

## Worked example 1: 4B/5B encoding

One line holds hexadecimal digits (each digit is 4 bits). Print the 4-bit groups on the first line, the 4B/5B code words on the second, and on the third compare the longest run of zeros before and after encoding.

```python
JADVAL = ["11110", "01001", "10100", "10101", "01010", "01011", "01110", "01111",
          "10010", "10011", "10110", "10111", "11010", "11011", "11100", "11101"]


def eng_uzun_nollar(bitlar):
    eng, joriy = 0, 0
    for b in bitlar:
        joriy = joriy + 1 if b == "0" else 0
        eng = max(eng, joriy)
    return eng


satr = input().strip()
tort = [format(int(r, 16), "04b") for r in satr]
besh = [JADVAL[int(r, 16)] for r in satr]          # every 4 bits -> a 5-bit word
print(" ".join(tort))
print(" ".join(besh))
print("longest zero run:", eng_uzun_nollar("".join(tort)), "->", eng_uzun_nollar("".join(besh)))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final String[] JADVAL = {"11110", "01001", "10100", "10101", "01010", "01011", "01110", "01111",
            "10010", "10011", "10110", "10111", "11010", "11011", "11100", "11101"};

    static int engUzunNollar(String bitlar) {
        int eng = 0, joriy = 0;
        for (char b : bitlar.toCharArray()) {
            joriy = b == '0' ? joriy + 1 : 0;
            eng = Math.max(eng, joriy);
        }
        return eng;
    }

    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine().trim();
        List<String> tort = new ArrayList<>();
        List<String> besh = new ArrayList<>();
        for (char r : satr.toCharArray()) {
            int qiymat = Character.digit(r, 16);
            tort.add(String.format("%4s", Integer.toBinaryString(qiymat)).replace(' ', '0'));
            besh.add(JADVAL[qiymat]);                        // every 4 bits -> a 5-bit word
        }
        System.out.println(String.join(" ", tort));
        System.out.println(String.join(" ", besh));
        System.out.println("longest zero run: " + engUzunNollar(String.join("", tort)) + " -> " + engUzunNollar(String.join("", besh)));
    }
}
```
```cpp
#include <algorithm>
#include <bitset>
#include <iostream>
#include <string>
using namespace std;

const string JADVAL[] = {"11110", "01001", "10100", "10101", "01010", "01011", "01110", "01111",
                         "10010", "10011", "10110", "10111", "11010", "11011", "11100", "11101"};

int engUzunNollar(const string& bitlar) {
    int eng = 0, joriy = 0;
    for (char b : bitlar) {
        joriy = b == '0' ? joriy + 1 : 0;
        eng = max(eng, joriy);
    }
    return eng;
}

int main() {
    string satr;
    cin >> satr;
    string tort, besh, tortQator, beshQator;
    for (char r : satr) {
        int qiymat = stoi(string(1, r), nullptr, 16);
        string t = bitset<4>(qiymat).to_string();
        tort += t;
        besh += JADVAL[qiymat];                              // every 4 bits -> a 5-bit word
        tortQator += (tortQator.empty() ? "" : " ") + t;
        beshQator += (beshQator.empty() ? "" : " ") + JADVAL[qiymat];
    }
    cout << tortQator << endl;
    cout << beshQator << endl;
    cout << "longest zero run: " << engUzunNollar(tort) << " -> " << engUzunNollar(besh) << endl;
    return 0;
}
```

Input:

```text
00
```

Output:

```text
0000 0000
11110 11110
longest zero run: 8 -> 1
```

Eight zeros would be eight periods of "silence" in NRZI; after 4B/5B, at most one zero comes in a row in the stream. For `0F1` the result is `0000 1111 0001` → `11110 11101 01001`, zeros 4 → 2.

## Worked example 2: bit stuffing

Each line holds a sequence of bits. For each, print the stuffed sequence and the number of zeros inserted. Input is read until the end of the file.

```python
import sys


def bit_qosh(bitlar):
    natija = []
    birlar = 0
    qoshildi = 0
    for b in bitlar:
        natija.append(b)
        if b == "1":
            birlar += 1
            if birlar == 5:                 # a 0 is inserted after five 1s
                natija.append("0")
                birlar = 0
                qoshildi += 1
        else:
            birlar = 0
    return "".join(natija), qoshildi


for satr in sys.stdin:
    satr = satr.strip()
    if satr:
        yangi, soni = bit_qosh(satr)
        print(satr + " -> " + yangi + " (+" + str(soni) + ")")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            StringBuilder natija = new StringBuilder();
            int birlar = 0;
            int qoshildi = 0;
            for (char b : satr.toCharArray()) {
                natija.append(b);
                if (b == '1') {
                    birlar++;
                    if (birlar == 5) {                  // a 0 is inserted after five 1s
                        natija.append('0');
                        birlar = 0;
                        qoshildi++;
                    }
                } else {
                    birlar = 0;
                }
            }
            System.out.println(satr + " -> " + natija + " (+" + qoshildi + ")");
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
    while (cin >> satr) {
        string natija;
        int birlar = 0;
        int qoshildi = 0;
        for (char b : satr) {
            natija += b;
            if (b == '1') {
                birlar++;
                if (birlar == 5) {                      // a 0 is inserted after five 1s
                    natija += '0';
                    birlar = 0;
                    qoshildi++;
                }
            } else {
                birlar = 0;
            }
        }
        cout << satr << " -> " << natija << " (+" << qoshildi << ")" << endl;
    }
    return 0;
}
```

Input:

```text
01111110
1111111111
0101
```

Output:

```text
01111110 -> 011111010 (+1)
1111111111 -> 111110111110 (+2)
0101 -> 0101 (+0)
```

The first line is the case where the flag itself occurs in the data: after stuffing it is no longer `01111110`, and the receiver will not mistake it for a frame boundary. The receiver does the reverse: it drops the `0` that follows five 1s.

:::warning
**Common mistakes**

- **Reading the level in NRZI as the bit value.** In NRZI the data is the **change** of level: a transition is 1, none is 0.
- **Mixing up the Manchester conventions.** In IEEE 802.3, 0 is high-to-low and 1 is low-to-high; in the G. E. Thomas convention it is the other way round. Check which one a task uses.
- **Thinking 4B/5B is encryption.** It is a substitution by a public table — its aim is synchronisation, not secrecy.
- **Not resetting the counter of ones in bit stuffing.** After the inserted 0 the count starts again; otherwise another 0 slips in at the next 1.
:::

:::tip
Before the independent task, draw the NRZ-L, NRZI and Manchester diagrams for `10110001` in your notebook and compare them with the table above. If the diagram is right, writing the program is much easier.
:::

## Summary

- A good line code is self-clocking, has no DC component and works in a narrow band.
- NRZ is simple but loses synchronisation on long runs; Manchester gives a transition in every bit but needs twice the bandwidth.
- 4B/5B (and 8B/10B, 64B/66B) removes long runs of zeros and works together with NRZI or MLT-3.
- Bit stuffing inserts a 0 after five 1s so that the `01111110` flag never appears inside the data.

## Check yourself

:::check
Q: Why can NRZ-L cause errors in a long run of zeros?
A: The signal does not change and there are no transitions to correct the receiver's clock — it may count the bits wrongly.
Q: What are the main advantage and drawback of Manchester code?
A: Since there is a transition in the middle of every bit, synchronisation is ideal; but the signal rate is twice as high — a wider band is needed.
Q: Which code word replaces `1010` in 4B/5B?
A: `10110`.
Q: What line rate is needed to send 100 Mbit/s of data with 4B/5B?
A: 125 Mbaud (5 bits for every 4).
Q: What is the result of bit stuffing the sequence `0111110`?
A: `01111100` — a 0 is inserted after five 1s, then the original last 0 follows.
:::

:::link https://en.wikipedia.org/wiki/4B5B
4B5B
The 4B/5B table, the control symbols and its use with NRZI and MLT-3.
:::
