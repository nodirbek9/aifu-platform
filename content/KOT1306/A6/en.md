---
title: Wireless data transmission technologies. Setting up a Wi-Fi network
summary: Wi-Fi frequency bands and channels, the non-overlapping channels 1, 6, 11 at 2.4 GHz; channel width; signal strength (dBm), signal-to-noise and free-space path loss; security — WEP, WPA, WPA2, WPA3, the danger of WPS; the steps of setting up an access point or router; choosing a channel by analysing neighbouring networks.
---

## Why it matters

In a block of flats every household has a router, and most of them run on the same 2.4 GHz channel — so Wi-Fi "is there but does not work". Another risk: a router left with its factory password and old encryption opens the network to strangers. In this session we learn to set up a wireless network properly — channel, width and security — and to calculate signal quality.

:::note
**After this session you will be able to:**

- explain the difference between the 2.4, 5 and 6 GHz bands and why only channels 1, 6, 11 are recommended at 2.4 GHz;
- choose the least busy channel from a list of neighbouring networks;
- assess signal strength in dBm and calculate the effect of distance and frequency;
- compare the security protocols and set up a home or office router securely.
:::

## Bands and channels

| Band | Channels | Advantage | Drawback |
|---|---|---|---|
| **2.4 GHz** | 1–13 (5 MHz apart, each 20 MHz wide) | passes walls well, reaches far | only 3 non-overlapping channels, interference from microwave ovens and Bluetooth |
| **5 GHz** | 36–165 (in 20 MHz steps, 20+ non-overlapping channels) | many channels, high speed | passes walls worse, shorter range |
| **6 GHz** (Wi-Fi 6E/7) | 59 channels of 20 MHz | a clean band, only new devices | the shortest range |

At 2.4 GHz the centre of channel *c* = 2407 + 5·*c* MHz. Since a channel is 20 MHz wide, channels whose centres differ by less than 20 MHz (that is, whose numbers differ by less than 4) **overlap** and interfere with each other. That is why the non-overlapping trio is **1, 6, 11**.

**Channel width**: 20 MHz — the most stable; 40 MHz — twice as fast, but at 2.4 GHz it takes up almost the whole band; at 5 GHz the 80 and 160 MHz widths give the high speeds of Wi-Fi 5/6.

## Signal strength

Wi-Fi signal strength is measured in **dBm** (decibels relative to 1 milliwatt): 0 dBm = 1 mW, 20 dBm = 100 mW, −70 dBm = 0.0000001 mW. The value is negative, and the closer it is to 0, the stronger the signal.

| Received signal | Quality |
|---|---|
| −50 dBm and above | excellent |
| −67 … −50 dBm | good (video calls, streaming) |
| −80 … −67 dBm | weak (pages open, slowly) |
| below −80 dBm | practically does not work |

Loss in open space (**FSPL**, free-space path loss): FSPL(dB) = 20·lg(*d*, km) + 20·lg(*f*, MHz) + 32.44. Doubling the distance loses 6 dB; raising the frequency from 2.4 to 5 GHz loses about 6.5 dB more. Walls "eat" another 3–15 dB.

## Security

| Protocol | Year | Encryption | Status |
|---|---|---|---|
| **WEP** | 1997 | RC4 | broken in minutes — **do not use** |
| **WPA** | 2003 | TKIP | obsolete |
| **WPA2** | 2004 | AES-CCMP | today's minimum; with a strong password |
| **WPA3** | 2018 | AES, SAE key exchange | the best choice; resistant to password guessing |

Additional rules: turn **WPS** off (its 8-digit PIN is broken quickly); change the router's **administrator password**; keep the firmware up to date; create a separate **guest network** for visitors. Hiding the SSID and MAC filtering are not real protection — with a sniffer both the SSID and the allowed MACs are visible.

## Practical work: setting up an access point

1. Connect to the router or access point by cable and open its management page in a browser (usually `192.168.0.1` or `192.168.1.1`). In Packet Tracer — the **Config → Wireless** tab of a **WRT300N** or **AccessPoint-PT** device.
2. Change the administrator password.
3. Set an **SSID** (without personal data, for example `AIFU-Lab-3`).
4. Security: **WPA2-Personal (AES)** or **WPA3-Personal**, a password of at least 12 characters.
5. Look at the neighbouring networks: on Windows

```text
netsh wlan show networks mode=bssid
```

   — prints each network's SSID, signal (%), channel and standard. Choose the least busy channel with the program of the first example below.
6. Set the channel width to 20 MHz for 2.4 GHz; 40 or 80 MHz for 5 GHz.
7. Connect a laptop and check the signal, channel and speed with `netsh wlan show interfaces`.

## Worked example 1: choosing a 2.4 GHz channel

The first line holds the number of neighbouring networks *n*, the second line their channels (1–13). For each of the non-overlapping channels 1, 6 and 11 print the number of neighbours overlapping it (channel numbers differing by less than 4) and recommend the least busy channel (the smaller one on a tie).

```python
n = int(input())
qoshnilar = list(map(int, input().split()))
eng_yaxshi, eng_kam = 0, n + 1
for kanal in (1, 6, 11):
    band = sum(1 for q in qoshnilar if abs(q - kanal) < 4)   # 20 MHz overlap
    print("channel " + str(kanal) + ": " + str(band))
    if band < eng_kam:
        eng_yaxshi, eng_kam = kanal, band
print("recommended: " + str(eng_yaxshi))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] qoshnilar = new int[n];
        for (int i = 0; i < n; i++) qoshnilar[i] = in.nextInt();
        int engYaxshi = 0, engKam = n + 1;
        for (int kanal : new int[] {1, 6, 11}) {
            int band = 0;
            for (int q : qoshnilar) {
                if (Math.abs(q - kanal) < 4) band++;                // 20 MHz overlap
            }
            System.out.println("channel " + kanal + ": " + band);
            if (band < engKam) {
                engYaxshi = kanal;
                engKam = band;
            }
        }
        System.out.println("recommended: " + engYaxshi);
    }
}
```
```cpp
#include <cstdlib>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> qoshnilar(n);
    for (int& q : qoshnilar) cin >> q;
    int engYaxshi = 0, engKam = n + 1;
    for (int kanal : {1, 6, 11}) {
        int band = 0;
        for (int q : qoshnilar) {
            if (abs(q - kanal) < 4) band++;                         // 20 MHz overlap
        }
        cout << "channel " << kanal << ": " << band << endl;
        if (band < engKam) {
            engYaxshi = kanal;
            engKam = band;
        }
    }
    cout << "recommended: " << engYaxshi << endl;
    return 0;
}
```

Input:

```text
6
1 1 6 3 4 13
```

Output:

```text
channel 1: 4
channel 6: 3
channel 11: 1
recommended: 11
```

Note: the neighbours on channels 3 and 4 (badly configured routers) interfere with both channel 1 and channel 6 at once — this is why choosing a channel other than 1, 6, 11 is bad.

## Worked example 2: signal strength by distance

Each line holds the transmitter power *P* (dBm), the frequency *f* (MHz) and the distance *d* (m). Print the free-space path loss, the received signal (dBm, one decimal place) and a quality rating. Input is read until the end of the file.

```python
import math
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    p, f, d = map(int, satr.split())
    fspl = 20 * math.log10(d / 1000) + 20 * math.log10(f) + 32.44   # d in km
    signal = p - fspl
    if signal >= -50:
        baho = "excellent"
    elif signal >= -67:
        baho = "good"
    elif signal >= -80:
        baho = "weak"
    else:
        baho = "unusable"
    print(f"{fspl:.1f} {signal:.1f} {baho}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int p = in.nextInt();
            int f = in.nextInt();
            int d = in.nextInt();
            double fspl = 20 * Math.log10(d / 1000.0) + 20 * Math.log10(f) + 32.44;   // d in km
            double signal = p - fspl;
            String baho;
            if (signal >= -50) {
                baho = "excellent";
            } else if (signal >= -67) {
                baho = "good";
            } else if (signal >= -80) {
                baho = "weak";
            } else {
                baho = "unusable";
            }
            System.out.println(String.format("%.1f %.1f ", fspl, signal) + baho);
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    int p, f, d;
    while (cin >> p >> f >> d) {
        double fspl = 20 * log10(d / 1000.0) + 20 * log10((double) f) + 32.44;   // d in km
        double signal = p - fspl;
        string baho;
        if (signal >= -50) {
            baho = "excellent";
        } else if (signal >= -67) {
            baho = "good";
        } else if (signal >= -80) {
            baho = "weak";
        } else {
            baho = "unusable";
        }
        printf("%.1f %.1f %s\n", fspl, signal, baho.c_str());
    }
    return 0;
}
```

Input:

```text
20 2437 10
20 2437 50
20 5180 50
20 5180 200
17 2412 1000
```

Output:

```text
60.2 -40.2 excellent
74.2 -54.2 good
80.7 -60.7 good
92.7 -72.7 weak
100.1 -83.1 unusable
```

At the same 50 m, a 5 GHz signal is ~6.5 dB weaker than a 2.4 GHz one. This is an open-space calculation — every wall in a home lowers the value further, so the real range indoors is much shorter.

:::warning
**Common mistakes**

- **Choosing channel 3, 4 or 9 at 2.4 GHz.** Such a channel interferes with — and suffers from — two "clean" channels at once.
- **A 40 MHz width at 2.4 GHz.** It occupies 9 of the 13 channels and leads to constant clashes with neighbours.
- **Taking a hidden SSID or MAC filtering for protection.** Both are easily bypassed with a sniffer; real protection is WPA2/WPA3 and a strong password.
- **Comparing dBm values like ordinary numbers.** A −60 dBm signal is 10 times stronger than −70 dBm; a 3 dB difference means twice the power.
:::

:::tip
Install a Wi-Fi analyser app on your phone and see which channels the neighbouring networks use at home or in the dormitory. Note the signal strength 1, 5 and 10 metres from the router (behind a wall) and compare it with the result of the second example.
:::

## Summary

- 2.4 GHz reaches far but has few channels (non-overlapping — 1, 6, 11); 5 and 6 GHz are faster and cleaner but have a shorter range.
- Signal strength in dBm: −50 and above is excellent, below −80 does not work; free-space loss grows logarithmically with distance and frequency.
- Security: WPA2 (AES) is the minimum, WPA3 the best; WEP and WPS are dangerous.
- Setup: the admin password, the SSID, WPA2/WPA3 and a strong password, choosing the channel by the neighbours, a suitable channel width.

## Check yourself

:::check
Q: Why are only channels 1, 6 and 11 recommended at 2.4 GHz?
A: The channel centres are 5 MHz apart while a channel is 20 MHz wide — only this trio does not overlap.
Q: Which is stronger, a −55 dBm or a −75 dBm signal, and by how much?
A: −55 dBm; a 20 dB difference means 100 times the power.
Q: Which security protocol should be chosen for a home router?
A: WPA3-Personal, or if unavailable WPA2-Personal (AES), with a strong password.
Q: Why is it recommended to turn WPS off?
A: Its 8-digit PIN is vulnerable to a brute-force attack.
Q: By how much does free-space loss grow when the distance doubles?
A: By about 6 dB (20·lg 2 ≈ 6).
:::

:::link https://en.wikipedia.org/wiki/List_of_WLAN_channels
List of WLAN channels
The Wi-Fi channels and frequencies in the 2.4, 5 and 6 GHz bands.
:::
