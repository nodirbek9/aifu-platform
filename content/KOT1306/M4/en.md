---
title: Data transmission methods. Physical media for data transmission. Data encoding and transmission methods
summary: Transmission modes (simplex, half duplex, full duplex), serial and parallel, synchronous and asynchronous transmission; analogue and digital signals, modulation; physical media — twisted pair, coaxial cable, optical fibre, wireless; channel capacity (the Nyquist and Shannon formulas); delays; error detection — the parity bit, the checksum, CRC.
---

## Why it matters

The lowest OSI layer — the physical layer — answers the question "how do we get a bit across a cable or through the air". Why could an ordinary telephone line never go faster than 56 Kbit/s, while optical fibre carries terabits? Why does Wi-Fi slow down behind a wall? Why does an Ethernet cable longer than 100 metres not work? The answers lie in the notions of signal, medium and channel capacity.

:::note
**After this lesson you will be able to:**

- tell apart the transmission modes (simplex, half duplex, full duplex) and methods (serial/parallel, synchronous/asynchronous);
- compare the main physical media and their properties;
- calculate channel capacity with the Nyquist and Shannon formulas;
- calculate transmission and propagation delays;
- explain how a parity bit detects an error and what its limits are.
:::

## Transmission modes and methods

| Mode | Meaning | Example |
|---|---|---|
| **Simplex** | data flows in one direction only | TV broadcasting, keyboard → computer |
| **Half duplex** | both directions, but in turn | a walkie-talkie, hub-based Ethernet |
| **Full duplex** | both directions at the same time | a phone call, switched Ethernet |

- **Parallel transmission** — several bits at once over separate wires (the old printer port). Fast over short distances, but over long ones the bits "fall behind" each other (skew).
- **Serial transmission** — bits one after another over a single line. All modern networks (Ethernet, USB, SATA) are serial.
- **Asynchronous transmission** — every character is wrapped in start and stop bits; the receiver re-aligns its clock on every character (RS-232).
- **Synchronous transmission** — large blocks are sent, and the sender and receiver clocks are synchronised from the signal itself; less overhead.

## Signals and modulation

An **analogue signal** changes continuously (sound, a radio wave), while a **digital signal** has a limited number of levels. Computer data is digital, but to send it by radio or over a telephone line the parameters of a **carrier wave** (a sinusoid) are varied — this is **modulation**:

| Modulation | What changes |
|---|---|
| **AM / ASK** (amplitude) | the height of the wave |
| **FM / FSK** (frequency) | the frequency of the wave |
| **PM / PSK** (phase) | the phase (shift) of the wave |
| **QAM** (quadrature amplitude) | amplitude and phase together — one symbol carries several bits (Wi-Fi, cable modems) |

On the receiving side the reverse happens — **demodulation**. A device doing both is a **modem** (modulator–demodulator).

**Baud** is the number of signal changes (symbols) per second; **bit/s** is the number of bits per second. If one symbol takes one of *M* states, it carries log₂ *M* bits: bit/s = baud × log₂ *M*. For example, in 16-QAM every symbol carries 4 bits.

## Physical media

| Medium | Structure | Speed and distance | Advantage | Drawback |
|---|---|---|---|---|
| **Twisted pair** — UTP, STP | 4 pairs of copper wire, each pair twisted | Cat5e: 1 Gbit/s, Cat6a: 10 Gbit/s; segment up to 100 m | cheap, easy to install | limited distance, sensitive to electromagnetic noise |
| **Coaxial cable** | a central conductor, insulation, a braid (shield) | hundreds of Mbit/s, hundreds of metres | resistant to noise | thick, more expensive; hardly used in LANs (cable TV, old Ethernet) |
| **Optical fibre** — single-mode, multi-mode | glass fibre, a light signal | 10–400 Gbit/s; multi-mode — hundreds of metres, single-mode — tens of km | huge capacity, immune to noise, long distance | more expensive, splicing needs special tools |
| **Wireless** (radio, microwave, infrared, satellite) | electromagnetic waves | Wi-Fi 6: several Gbit/s, Bluetooth: Mbit/s | mobility, no cable needed | obstacles, noise, security |

The twisting in twisted pair reduces interference between neighbouring pairs (**crosstalk**). In any medium a signal **attenuates** with distance and **noise** is added — which is why repeaters are needed.

## Channel capacity

**The Nyquist formula** (a noiseless channel): a channel of bandwidth *B* Hz can carry at most 2*B* symbols per second, so

*C* = 2 *B* log₂ *M* bit/s,

where *M* is the number of signal levels.

**The Shannon formula** (a noisy channel): whatever the number of levels, the theoretical limit of error-free transmission is

*C* = *B* log₂(1 + *S*/*N*) bit/s,

where *S*/*N* is the signal-to-noise ratio. It is usually given in **decibels**: *S*/*N* = 10^(dB/10). For example, 30 dB → a factor of 1000.

The real rate never exceeds the **smaller** of the two: more levels raise the Nyquist limit, but noise does not let you pass the Shannon limit.

## Delays

| Delay | Formula | Depends on |
|---|---|---|
| **Transmission delay** | *L* / *R* (number of bits / channel rate) | packet size and channel rate |
| **Propagation delay** | *d* / *v* (distance / signal speed) | distance; in copper and fibre *v* ≈ 200,000 km/s |
| **Processing** and **queuing** | — | router load |

When a small packet goes a long way, propagation dominates; when a large file goes over a slow channel, transmission delay dominates.

## Encoding data and detecting errors

A digital signal is put on the cable with **line codes**: bit values are expressed by voltage levels or by their changes. The main codes — NRZ, NRZI, Manchester, 4B/5B, MLT-3 — are studied in detail in **practical session A4**. A good code meets two requirements: enough transitions to synchronise the receiver's clock, and no DC component.

Noise may corrupt bits, so the sender adds extra **check bits**:

- **Parity bit** — one bit is added so that the number of ones is even (even parity). If one bit is corrupted, it is detected; if two bits are corrupted, it is **not detected**.
- **Checksum** — a sum of words (in IP and TCP headers).
- **CRC** (cyclic redundancy check) — the remainder of polynomial division; the FCS field of an Ethernet frame is CRC-32. It detects packet errors very well.

## Worked example 1: channel capacity

The first line holds *k*; each of the next *k* lines holds the channel bandwidth *B* (Hz), the signal-to-noise ratio (dB) and the number of levels *M*. For each channel print the Nyquist limit, the Shannon limit and the smaller of the two (bit/s, rounded to an integer).

```python
import math

k = int(input())
for _ in range(k):
    b, db, m = map(int, input().split())
    snr = 10 ** (db / 10)                   # from decibels to a plain ratio
    naykvist = 2 * b * math.log2(m)
    shennon = b * math.log2(1 + snr)
    print(f"{naykvist:.0f} {shennon:.0f} {min(naykvist, shennon):.0f}")
```
```java
import java.util.Scanner;

public class Main {
    static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        for (int i = 0; i < k; i++) {
            long b = in.nextLong();
            int db = in.nextInt();
            int m = in.nextInt();
            double snr = Math.pow(10, db / 10.0);          // from decibels to a plain ratio
            double naykvist = 2 * b * log2(m);
            double shennon = b * log2(1 + snr);
            System.out.println(String.format("%.0f %.0f %.0f", naykvist, shennon, Math.min(naykvist, shennon)));
        }
    }
}
```
```cpp
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    int k;
    cin >> k;
    for (int i = 0; i < k; i++) {
        long long b;
        int db, m;
        cin >> b >> db >> m;
        double snr = pow(10.0, db / 10.0);                 // from decibels to a plain ratio
        double naykvist = 2 * b * log2((double) m);
        double shennon = b * log2(1 + snr);
        printf("%.0f %.0f %.0f\n", naykvist, shennon, min(naykvist, shennon));
    }
    return 0;
}
```

Input:

```text
4
3000 30 2
3000 30 16
20000000 20 4
1000000 0 2
```

Output:

```text
6000 29902 6000
24000 29902 24000
80000000 133164230 80000000
2000000 1000000 1000000
```

The first two lines are a classic telephone line (3 kHz, 30 dB): 6 Kbit/s with a two-level signal, 24 Kbit/s with 16 levels, but no method can go beyond ~30 Kbit/s. In the last line the noise equals the signal (0 dB): however many levels there are, the limit is 1 Mbit/s.

## Worked example 2: an even parity bit

The first line holds a word (ASCII letters). For each letter print its 7-bit code, its parity bit and the 8 bits that are transmitted (code + parity bit). Then come *n* and *n* received 8-bit words: for each print `OK` if the number of ones is even, otherwise `ERROR`.

```python
soz = input()
for belgi in soz:
    bitlar = format(ord(belgi), "07b")      # 7-bit ASCII code
    juftlik = bitlar.count("1") % 2         # 1 if the number of ones is odd
    print(belgi, bitlar, juftlik, bitlar + str(juftlik))

n = int(input())
for _ in range(n):
    qabul = input()
    print(qabul, "OK" if qabul.count("1") % 2 == 0 else "ERROR")
```
```java
import java.util.Scanner;

public class Main {
    static int birlar(String s) {
        int soni = 0;
        for (char c : s.toCharArray()) {
            if (c == '1') soni++;
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String soz = in.nextLine();
        for (char belgi : soz.toCharArray()) {
            String bitlar = String.format("%7s", Integer.toBinaryString(belgi)).replace(' ', '0');  // 7-bit ASCII code
            int juftlik = birlar(bitlar) % 2;                // 1 if the number of ones is odd
            System.out.println(belgi + " " + bitlar + " " + juftlik + " " + bitlar + juftlik);
        }

        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String qabul = in.nextLine().trim();
            System.out.println(qabul + " " + (birlar(qabul) % 2 == 0 ? "OK" : "ERROR"));
        }
    }
}
```
```cpp
#include <bitset>
#include <iostream>
#include <string>
using namespace std;

int birlar(const string& s) {
    int soni = 0;
    for (char c : s) {
        if (c == '1') soni++;
    }
    return soni;
}

int main() {
    string soz;
    getline(cin, soz);
    for (char belgi : soz) {
        string bitlar = bitset<7>(belgi).to_string();      // 7-bit ASCII code
        int juftlik = birlar(bitlar) % 2;                  // 1 if the number of ones is odd
        cout << belgi << " " << bitlar << " " << juftlik << " " << bitlar << juftlik << endl;
    }

    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string qabul;
        cin >> qabul;
        cout << qabul << " " << (birlar(qabul) % 2 == 0 ? "OK" : "ERROR") << endl;
    }
    return 0;
}
```

Input:

```text
Salom
3
11000011
11100011
10100011
```

Output:

```text
S 1010011 0 10100110
a 1100001 1 11000011
l 1101100 0 11011000
o 1101111 0 11011110
m 1101101 1 11011011
11000011 OK
11100011 ERROR
10100011 OK
```

Note: the correct word for the letter `a` is `11000011`. In the second received word one bit is corrupted — the error is detected. In the third, **two** bits are corrupted (bits 2 and 3), but the number of ones is even again — the error is **not detected**. That is why networks use CRC, not a parity bit.

## Worked example 3: transmission and propagation delay

Each line holds the packet size *L* (bytes), the channel rate *R* (Mbit/s), the distance *d* (km) and the signal speed *v* (km/s). Print the transmission delay, the propagation delay and their sum in milliseconds (to three decimal places). Input is read until the end of the file.

```python
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    l, r, d, v = map(int, satr.split())
    uzatish = l * 8 / (r * 1_000_000) * 1000    # ms
    tarqalish = d / v * 1000                   # ms
    print(f"{uzatish:.3f} {tarqalish:.3f} {uzatish + tarqalish:.3f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long l = in.nextLong();
            long r = in.nextLong();
            long d = in.nextLong();
            long v = in.nextLong();
            double uzatish = l * 8 / (r * 1_000_000.0) * 1000;   // ms
            double tarqalish = (double) d / v * 1000;            // ms
            System.out.println(String.format("%.3f %.3f %.3f", uzatish, tarqalish, uzatish + tarqalish));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    long long l, r, d, v;
    while (cin >> l >> r >> d >> v) {
        double uzatish = l * 8 / (r * 1000000.0) * 1000;     // ms
        double tarqalish = (double) d / v * 1000;            // ms
        printf("%.3f %.3f %.3f\n", uzatish, tarqalish, uzatish + tarqalish);
    }
    return 0;
}
```

Input:

```text
1500 100 1000 200000
10000000 10 10 200000
64 1000 36000 300000
```

Output:

```text
0.120 5.000 5.120
8000.000 0.050 8000.050
0.001 120.000 120.001
```

The first line: a 1500-byte frame over 1000 km — propagation dominates. The second: a 10 MB file over a slow channel — transmission dominates. The third: a small packet up to a geostationary satellite (36,000 km) — however high the rate, a 120 ms delay remains.

:::warning
**Common mistakes**

- **Treating baud and bit/s as the same.** They are equal only for a two-level signal; in 16-QAM bit/s is 4 times the baud.
- **Putting decibels straight into the formula.** Into the Shannon formula goes 10^(30/10) = 1000, not 30.
- **Thinking that a higher rate also lowers the delay.** The rate only lowers transmission delay; propagation delay depends on distance and the speed of light.
- **Believing a parity bit finds all errors.** An even number of corrupted bits goes undetected.
- **Running twisted-pair cable longer than 100 m.** The Ethernet standard limits a segment to 100 m; beyond that you need a switch or fibre.
:::

:::tip
Run `ping 8.8.8.8` on your computer and look at the reply time. Taking the signal speed as 200,000 km/s, work out how far the signal could travel in that time (the reply time covers the trip there and back). Compare the result with the server's likely location.
:::

## Summary

- Transmission modes: simplex, half duplex, full duplex; modern networks transmit serially.
- Modulation varies the amplitude, frequency or phase of a carrier; QAM carries several bits per symbol.
- Media: twisted pair (up to 100 m), coaxial, optical fibre (the largest capacity and distance), wireless.
- Nyquist: *C* = 2*B* log₂ *M*; Shannon: *C* = *B* log₂(1 + *S*/*N*); the real rate never exceeds the smaller of the two.
- Delay = transmission (*L*/*R*) + propagation (*d*/*v*) + processing + queuing.
- A parity bit finds a one-bit error but not a two-bit one; networks use CRC.

## Check yourself

:::check
Q: What is the difference between half duplex and full duplex?
A: In half duplex data flows in both directions in turn, in full duplex at the same time.
Q: What is the highest rate on a noiseless 4000 Hz channel with an 8-level signal?
A: Nyquist: 2 × 4000 × log₂ 8 = 24,000 bit/s.
Q: If the signal-to-noise ratio is 20 dB, what is the plain ratio?
A: 10^(20/10) = 100.
Q: In what ways is optical fibre better than twisted pair?
A: Its capacity and distance are much greater, and it is not affected by electromagnetic noise.
Q: How long does it take to transmit a 1000-byte packet over a 1 Gbit/s channel?
A: 8000 bits / 10⁹ bit/s = 8 microseconds (0.008 ms).
:::

:::link https://en.wikipedia.org/wiki/Shannon%E2%80%93Hartley_theorem
The Shannon–Hartley theorem
The noisy-channel capacity formula, examples and its link to the Nyquist limit.
:::
