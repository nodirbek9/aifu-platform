---
title: Cryptographic methods of information protection. The Enigma cipher machine and its strength
summary: The history of mechanical and electromechanical cipher machines; the structure of Enigma — keyboard, rotors, reflector, plugboard, rotor stepping; the key space and how to estimate it; the properties of Enigma — self-reciprocity and no letter ever encrypting to itself; how it was broken — Polish mathematicians (Rejewski), Bletchley Park, Turing and the Bombe, the "crib" method; operator mistakes; lessons for modern cryptography; a simplified Enigma model in a program.
---

## Why does this topic matter?

Enigma is the most famous machine in the history of cryptography. In the Second World War the German armed forces considered it unbreakable: its key space exceeded 10²⁰. Even so, Polish and British mathematicians read it regularly, and historians believe this shortened the war by several years. The story of Enigma vividly illustrates the lessons of M5: a large key space is not enough; the structural properties of a cipher and **human mistakes** can break it. Moreover, the machines built to break Enigma were among the forerunners of modern computers.

:::note
**After this assignment you will be able to:**

- explain the structure of Enigma and the encryption process;
- estimate its key space;
- name the weaknesses of Enigma and how they were exploited;
- show a simplified Enigma model in a program;
- draw lessons from the history of Enigma for modern cryptography.
:::

## The structure of Enigma

Enigma is an electromechanical **rotor** cipher machine (invented in the 1920s for commercial use and then adopted by the German armed forces).

| Part | Role |
|---|---|
| **Keyboard and lampboard** | the operator presses a letter — the lamp of the encrypted letter lights up |
| **Plugboard** (Steckerbrett) | swaps 10 pairs of letters with cables (for example A↔Q) |
| **Rotors** (usually 3, chosen from 5) | each a disc with 26 contacts and wires inside that substitute letters |
| **Reflector** | sends the signal back through the rotors |
| **Ring setting** (Ringstellung) | shifts a rotor's wiring relative to its lettered ring |

**The encryption path**: key → plugboard → right, middle, left rotor → reflector → left, middle, right rotor (in reverse) → plugboard → lamp. With every key press **the right rotor steps once**; after a full revolution the middle one steps, then the left one (like a clock). That is why the same letter is encrypted differently each time — Enigma is a **polyalphabetic** substitution cipher with a very long period (a sophisticated successor of the Vigenère cipher from A5).

**Key space** (an approximate estimate for the army Enigma):

| Part | Variants |
|---|---|
| Choosing 3 of 5 rotors in order | 5 · 4 · 3 = 60 |
| Initial rotor positions | 26³ = 17,576 |
| 10 pairs on the plugboard | ≈ 1.5 · 10¹⁴ |
| Total | ≈ 1.6 · 10²⁰ (ring settings not included) |

This equals a key of about 67 bits — a huge number for that time: exhaustive search by hand or mechanically was impossible.

## Properties and weaknesses of Enigma

1. **Self-reciprocity**: because of the reflector, encryption and decryption are the same operation. "Encrypting" the ciphertext again with the same settings gives the plaintext — convenient for operators, but the substitution always consists of pairs.
2. **No letter encrypts to itself**: the signal cannot return from the reflector along the same wire. This small property became a huge weakness: if you can guess where a word stands in the ciphertext (a **crib** — for example "WETTERBERICHT", a weather report), it cannot stand at places where any of its letters coincides with a ciphertext letter.
3. **Operator mistakes and procedures**: messages of the same form at the same time every day (weather, "nothing to report"), repeating the message key twice (until 1940), simple keys (adjacent keys on the keyboard, initials).
4. **The plugboard only swaps pairs** and does not hide the structure of the rotors.

## How Enigma was broken

- **Poland (1932–1939)**: Marian Rejewski used permutation theory and the repeated message key to reconstruct the rotor wiring mathematically; together with Henryk Zygalski and Jerzy Różycki he created the "bomba" machines and perforated sheets. In July 1939 the results were handed over to Britain and France.
- **Bletchley Park (Britain)**: Alan Turing and Gordon Welchman designed the crib-based electromechanical **Bombe** — it searched rotor settings quickly and eliminated those contradicting the crib. By the end of the war hundreds of Bombes were running; the intelligence obtained was called "Ultra".
- **Captured material**: key tables and machines taken from ships and submarines also played an important role.

## Worked example 1: a simplified Enigma

A **single-rotor** model with the wiring of rotor I of the real Enigma I (`EKMFLGDQVZNTOWYHXUSPAIBRCJ`) and reflector B (`YRUHQSLDPXNGOKMIEBFZCWVJAT`): before each letter the rotor steps once; the signal goes rotor → reflector → rotor (in reverse); non-letter characters do not change. The first line holds the initial rotor position (0–25), the second the text.

```python
import sys

ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ"        # Enigma I, rotor I
REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT"    # reflector B

satrlar = sys.stdin.read().split("\n")
holat = int(satrlar[0])
natija = []
for ch in satrlar[1]:
    if not ("A" <= ch <= "Z"):
        natija.append(ch)
        continue
    holat = (holat + 1) % 26                # the rotor steps before every letter
    c = ord(ch) - 65
    c = (ord(ROTOR[(c + holat) % 26]) - 65 - holat) % 26        # through the rotor
    c = ord(REFLEKTOR[c]) - 65                                   # reflector
    c = (ROTOR.index(chr((c + holat) % 26 + 65)) - holat) % 26  # back through the rotor
    natija.append(chr(c + 65))
print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    static final String ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";       // Enigma I, rotor I
    static final String REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT";   // reflector B

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int holat = Integer.parseInt(in.nextLine().trim());
        String matn = in.nextLine();
        StringBuilder natija = new StringBuilder();
        for (char ch : matn.toCharArray()) {
            if (ch < 'A' || ch > 'Z') {
                natija.append(ch);
                continue;
            }
            holat = (holat + 1) % 26;                                 // the rotor steps before every letter
            int c = ch - 'A';
            c = Math.floorMod(ROTOR.charAt((c + holat) % 26) - 'A' - holat, 26);        // through the rotor
            c = REFLEKTOR.charAt(c) - 'A';                                                // reflector
            c = Math.floorMod(ROTOR.indexOf((char) ((c + holat) % 26 + 'A')) - holat, 26);   // back through the rotor
            natija.append((char) (c + 'A'));
        }
        System.out.println(natija);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const string ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";        // Enigma I, rotor I
const string REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT";    // reflector B

int mod26(int x) { return (x % 26 + 26) % 26; }

int main() {
    string satr, matn;
    getline(cin, satr);
    int holat = stoi(satr);
    getline(cin, matn);
    if (!matn.empty() && matn.back() == '\r') matn.pop_back();
    string natija;
    for (char ch : matn) {
        if (ch < 'A' || ch > 'Z') {
            natija += ch;
            continue;
        }
        holat = (holat + 1) % 26;                                  // the rotor steps before every letter
        int c = ch - 'A';
        c = mod26(ROTOR[(c + holat) % 26] - 'A' - holat);          // through the rotor
        c = REFLEKTOR[c] - 'A';                                    // reflector
        c = mod26((int) ROTOR.find((char) ((c + holat) % 26 + 'A')) - holat);   // back through the rotor
        natija += (char) (c + 'A');
    }
    cout << natija << endl;
    return 0;
}
```

Input:

```text
3
SALOM DUNYO
```

Output:

```text
GIMHC NMRUA
```

Now enter the output again with the same initial position (`3` and `GIMHC NMRUA`) — the program returns `SALOM DUNYO` (Uzbek for "hello world"): because of the reflector, encryption and decryption are the same operation. Note: the two letters O in the text were encrypted differently — to H and A, because the rotor position differs at each position; and no letter turned into itself at its place.

## Worked example 2: checking crib positions

Using the fact that no letter encrypts to itself, the Bletchley Park cryptanalysts slid the crib along the ciphertext and eliminated **impossible** positions: if at some position a crib letter equals the ciphertext letter, the crib cannot stand there. The first line holds the ciphertext, the second the crib. Print all possible starting positions (from 0) and their number.

```python
import sys

satrlar = sys.stdin.read().split("\n")
shifr, crib = satrlar[0].strip(), satrlar[1].strip()
mumkin = []
for i in range(len(shifr) - len(crib) + 1):
    if all(shifr[i + j] != crib[j] for j in range(len(crib))):   # no letter may coincide
        mumkin.append(i)
print("possible: " + " ".join(map(str, mumkin)))
print(f"{len(mumkin)} / {len(shifr) - len(crib) + 1}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String shifr = in.nextLine().trim(), crib = in.nextLine().trim();
        List<String> mumkin = new ArrayList<>();
        int jami = shifr.length() - crib.length() + 1;
        for (int i = 0; i < jami; i++) {
            boolean ok = true;
            for (int j = 0; j < crib.length(); j++) {
                if (shifr.charAt(i + j) == crib.charAt(j)) ok = false;   // no letter may coincide
            }
            if (ok) mumkin.add(String.valueOf(i));
        }
        System.out.println("possible: " + String.join(" ", mumkin));
        System.out.println(mumkin.size() + " / " + jami);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string shifr, crib;
    getline(cin, shifr);
    getline(cin, crib);
    while (!shifr.empty() && (shifr.back() == '\r' || shifr.back() == ' ')) shifr.pop_back();
    while (!crib.empty() && (crib.back() == '\r' || crib.back() == ' ')) crib.pop_back();
    vector<int> mumkin;
    int jami = (int) shifr.size() - (int) crib.size() + 1;
    for (int i = 0; i < jami; i++) {
        bool ok = true;
        for (size_t j = 0; j < crib.size(); j++) {
            if (shifr[i + j] == crib[j]) ok = false;                 // no letter may coincide
        }
        if (ok) mumkin.push_back(i);
    }
    cout << "possible:";
    for (int p : mumkin) cout << " " << p;
    cout << endl << mumkin.size() << " / " << jami << endl;
    return 0;
}
```

Input:

```text
NAQBKGTKVCRUOSJDPSKKFMMZIZMBKU
WEATHERREPORT
```

Output:

```text
possible: 0 1 5 6 8 9 10 11 12 13 14 15 16 17
14 / 18
```

The ciphertext was produced with the model from example 1 (initial position 5) from the text `ATTACKATDAWNWEATHERREPORTCLEAR`; the crib really stands at position 12, and it stayed in the "possible" list. 4 of the 18 positions were eliminated at once. The longer the crib and the more messages, the stronger the filter; the Bombe checked the remaining candidates by searching rotor settings. Modern ciphers (AES) have no such "forbidden" correspondence between ciphertext and plaintext.

## Lessons for modern cryptography

| The Enigma experience | Modern principle |
|---|---|
| Machines and rotors were captured, the wiring reconstructed | **Kerckhoffs's principle**: security must depend only on the key (M5) |
| 10²⁰ keys, but weak structural properties | a large key space is necessary but not sufficient |
| Predictable texts (cribs) | a cipher must resist **known-plaintext attacks** |
| Repeating the message key, simple keys | protocol and operating procedures matter as much as the algorithm; random keys |
| The belief that "our cipher cannot be broken" | openly analyzed standards, continuous re-evaluation |

:::warning
**Common mistakes**

- **Treating Enigma as a "simple substitution cipher".** The rotor steps with every letter — it is a polyalphabetic cipher with a very long period.
- **Saying that only Turing broke it.** The main mathematical breakthrough belongs to the Polish cryptanalysts; Bletchley Park continued and extended their work.
- **Equating a large key space with security.** Enigma is the counterexample.
- **Not checking historical facts.** Popular films simplify events; in an essay cite historical research and museum sources.
:::

:::tip
Online Enigma simulators (for example the "Enigma" recipe in CyberChef) offer the full model with three rotors and a plugboard. Compare the model from example 1 with them: how does the result change when one rotor is added?
:::

## Summary

- Enigma is an electromechanical rotor cipher: plugboard, three rotors, reflector; the rotor steps with every letter.
- The key space is ≈ 1.6 · 10²⁰, but because of the reflector no letter encrypts to itself — this opened the way to the crib attack.
- Polish mathematicians (Rejewski) broke it first; Bletchley Park (Turing, Welchman) scaled it up with the Bombe.
- Operator mistakes and predictable messages mattered as much as algorithmic weaknesses.
- Lessons: Kerckhoffs's principle, resistance to known-plaintext attacks, correct operating procedures.

## Check yourself

:::check
Q: Why is the same letter encrypted differently each time in Enigma?
A: The rotor steps with every key press, and the substitution table changes.
Q: Which property of Enigma opened the way to the crib attack?
A: No letter encrypts to itself — a crib cannot stand where it coincides with a ciphertext letter.
Q: How is decryption done on Enigma?
A: By typing the ciphertext again with the same settings — because of the reflector the operation is self-reciprocal.
Q: What lesson about Kerckhoffs's principle does the history of Enigma teach?
A: The machine and its wiring became known to the enemy; security must rely only on the secret key.
:::

:::link https://www.bletchleypark.org.uk/
Bletchley Park
The official website of the museum devoted to Enigma and wartime cryptanalysis.
:::
