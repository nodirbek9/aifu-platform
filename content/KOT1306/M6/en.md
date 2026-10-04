---
title: Token Ring and other technologies. Token Ring, 100VG-AnyLAN, FDDI, Wi-Fi technologies
summary: Deterministic (token) and random methods of medium access; Token Ring (IEEE 802.5) — the token, the MAU, the active monitor, priorities; FDDI — two fibre rings and wrapping the ring on a failure; 100VG-AnyLAN (IEEE 802.12) — demand priority; Wi-Fi (IEEE 802.11) — standards, frequencies, CSMA/CA, the hidden station problem and RTS/CTS, BSS and ESS; comparing the technologies.
---

## Why it matters

Ethernet did not always win. In the 1980s–1990s banks and factories chose **Token Ring**: it has no collisions, and every computer knows exactly when its turn to transmit will come. University backbones were built on **FDDI** fibre rings. These technologies are hardly used today, but their ideas — token access, backup with two rings, priorities — live on in industrial networks and in Wi-Fi. Wi-Fi, in turn, is the most widespread access technology today.

:::note
**After this lesson you will be able to:**

- tell random and deterministic medium access methods apart;
- explain how the token circulates in Token Ring and who transmits when;
- describe FDDI's dual-ring backup and the demand-priority idea of 100VG-AnyLAN;
- explain the Wi-Fi standards, frequency bands and the CSMA/CA method;
- explain the hidden station problem and the RTS/CTS solution.
:::

## Medium access methods

| Method | Idea | Advantage | Drawback | Example |
|---|---|---|---|---|
| **Random** (contention) | any station transmits when the channel is free | minimal delay under light load, simple | under heavy load — collisions, unpredictable delay | Ethernet (CSMA/CD), Wi-Fi (CSMA/CA) |
| **Deterministic** (token) | only the station holding the "token" (a permission) transmits | no collisions, the longest wait is known | overhead of circulating the token; the lost-token problem | Token Ring, FDDI |
| **Polling** | a central device asks the stations in turn | priorities are easy to manage | the centre is a single point of failure | 100VG-AnyLAN, Bluetooth |

## Token Ring (IEEE 802.5)

Developed by IBM, it runs at 4 and 16 Mbit/s. Its logical topology is a **ring**, but its physical topology is a **star**: computers connect to a **MAU** (Multistation Access Unit), and the ring is formed inside the MAU. If a computer is switched off, the MAU bypasses its port and the ring is not broken.

How it works:

1. A special short frame — the **token** — circulates around the ring.
2. A station that wants to transmit seizes the token, turns it into a data frame and transmits. The token holding time is limited (**THT** — usually 10 ms).
3. The frame travels round the ring: the receiver copies it and sets the "received" mark.
4. When the frame returns to the sender, it removes the frame from the ring and passes a new token to the next station.

Additional mechanisms: the **active monitor** (one station watches for a lost token or "endlessly circulating" frames), **priorities** (the token has 8 priority levels), **early token release** (at 16 Mbit/s the token is released without waiting for the frame to return). The encoding is **differential Manchester**.

## FDDI

**FDDI** (Fiber Distributed Data Interface, ANSI X3T9.5) is a 100 Mbit/s fibre network using the token method. It spans up to 200 km with up to 500 stations. Its key feature is **two rings running in opposite directions**: the primary (working) ring and the secondary (backup) ring.

If a cable breaks or a station fails, the two stations next to the break connect the primary ring to the secondary (**wrap**) — the two rings become one long ring and the network keeps working. FDDI was used for campus and city backbones; its copper version is CDDI. Encoding: 4B/5B + NRZI.

## 100VG-AnyLAN (IEEE 802.12)

A 100 Mbit/s technology proposed by Hewlett-Packard. The access method is **demand priority**: the hub polls the ports in turn, and a station sends a request to transmit; requests are of **normal** or **high** priority (for example, for video). There are no collisions. It used all 4 pairs of Cat3 cable ("VG" — voice grade). It could carry both Ethernet and Token Ring frames ("AnyLAN"). Technically good, it lost the competition to the cheaper **Fast Ethernet**.

## Wi-Fi (IEEE 802.11)

| Standard | Generation | Year | Frequency | Maximum rate |
|---|---|---|---|---|
| 802.11b | — | 1999 | 2.4 GHz | 11 Mbit/s |
| 802.11a | — | 1999 | 5 GHz | 54 Mbit/s |
| 802.11g | — | 2003 | 2.4 GHz | 54 Mbit/s |
| 802.11n | Wi-Fi 4 | 2009 | 2.4 / 5 GHz | 600 Mbit/s |
| 802.11ac | Wi-Fi 5 | 2013 | 5 GHz | ~6.9 Gbit/s |
| 802.11ax | Wi-Fi 6 / 6E | 2019 / 2021 | 2.4 / 5 / 6 GHz | ~9.6 Gbit/s |
| 802.11be | Wi-Fi 7 | 2024 | 2.4 / 5 / 6 GHz | ~46 Gbit/s |

**Structure**: a **BSS** (Basic Service Set) is one access point and the devices connected to it; an **ESS** (Extended Service Set) is several access points working with the same **SSID** (network name), between which a user moves without disconnecting (roaming); **ad hoc** (IBSS) — no access point, devices talk directly.

**CSMA/CA** (Collision Avoidance): on radio a sender "shouting" its own signal cannot hear others, so collisions must be **avoided** rather than **detected**:

1. Listen to the channel; even if it is free, wait for the **DIFS** time.
2. Wait for a random **backoff** counter (the counter pauses while the channel is busy).
3. Transmit the frame; the receiver confirms with an **ACK**. If no ACK arrives, a collision or error is assumed and the frame is resent.

**The hidden station problem**: A and C cannot hear each other (a wall or distance), but both hear access point B. If both transmit at once, they collide at B. The solution is **RTS/CTS**: A says "I want to transmit" (RTS), B answers with a "go ahead, A" (CTS) heard by everyone, and C stays silent for the stated time.

## Worked example: modelling a token ring

A ring has *n* stations (1 to *n*), each with a number of frames waiting to be sent. The token circulates in order starting from station 1; when the token arrives a station sends at most *h* frames (the THT limit). For each rotation print who sent how many frames, and the total number of rotations.

```python
n, h = map(int, input().split())
navbat = list(map(int, input().split()))
aylana = 0
while sum(navbat) > 0:
    aylana += 1
    qismlar = []
    for i in range(n):                          # the token goes round from 1 to n
        yubordi = min(navbat[i], h)             # THT: at most h frames
        if yubordi > 0:
            navbat[i] -= yubordi
            qismlar.append("S" + str(i + 1) + "=" + str(yubordi))
    print("round " + str(aylana) + ": " + " ".join(qismlar))
print("total rounds: " + str(aylana))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int h = in.nextInt();
        int[] navbat = new int[n];
        int qolgan = 0;
        for (int i = 0; i < n; i++) {
            navbat[i] = in.nextInt();
            qolgan += navbat[i];
        }
        int aylana = 0;
        while (qolgan > 0) {
            aylana++;
            StringBuilder qismlar = new StringBuilder();
            for (int i = 0; i < n; i++) {                   // the token goes round from 1 to n
                int yubordi = Math.min(navbat[i], h);       // THT: at most h frames
                if (yubordi > 0) {
                    navbat[i] -= yubordi;
                    qolgan -= yubordi;
                    if (qismlar.length() > 0) qismlar.append(" ");
                    qismlar.append("S" + (i + 1) + "=" + yubordi);
                }
            }
            System.out.println("round " + aylana + ": " + qismlar);
        }
        System.out.println("total rounds: " + aylana);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int n, h;
    cin >> n >> h;
    vector<int> navbat(n);
    int qolgan = 0;
    for (int i = 0; i < n; i++) {
        cin >> navbat[i];
        qolgan += navbat[i];
    }
    int aylana = 0;
    while (qolgan > 0) {
        aylana++;
        string qismlar;
        for (int i = 0; i < n; i++) {                       // the token goes round from 1 to n
            int yubordi = min(navbat[i], h);                // THT: at most h frames
            if (yubordi > 0) {
                navbat[i] -= yubordi;
                qolgan -= yubordi;
                if (!qismlar.empty()) qismlar += " ";
                qismlar += "S" + to_string(i + 1) + "=" + to_string(yubordi);
            }
        }
        cout << "round " << aylana << ": " << qismlar << endl;
    }
    cout << "total rounds: " << aylana << endl;
    return 0;
}
```

Input (4 stations, *h* = 2):

```text
4 2
3 0 5 1
```

Output:

```text
round 1: S1=2 S3=2 S4=1
round 2: S1=1 S3=2
round 3: S3=1
total rounds: 3
```

The key conclusion: every station knows exactly when the token will come — in the worst case it waits while each of the other *n* − 1 stations sends *h* frames. Ethernet gives no such guarantee: an unlucky station may wait arbitrarily long because of collisions. That is why deterministic methods are valued in real-time systems (factory automation, cars).

:::warning
**Common mistakes**

- **Taking Token Ring for a physical ring.** The cables connect to the MAU in a star; the ring is logical.
- **Saying Wi-Fi uses CSMA/CD.** A radio station cannot detect a collision while transmitting — that is why CSMA/**CA** and ACK confirmations are used.
- **Thinking both FDDI rings always carry data.** Normally the secondary ring is a backup and comes into use only on a failure.
- **Taking a Wi-Fi standard's maximum rate for the real rate.** It is a theoretical limit for all antennas, the widest channel and ideal conditions; in practice it is several times lower.
:::

:::tip
Open the Wi-Fi network details on your phone or laptop: which standard (Wi-Fi 4/5/6), which band (2.4 or 5 GHz) and which channel is in use? On Windows, the command `netsh wlan show interfaces` shows this.
:::

## Summary

- Access methods: random (Ethernet, Wi-Fi), deterministic token (Token Ring, FDDI), polling (100VG-AnyLAN).
- Token Ring: 4/16 Mbit/s, a logical ring and a physical star (MAU), the token, THT, the active monitor, priorities.
- FDDI: a 100 Mbit/s dual fibre ring; on a failure the rings are joined into one ring (wrap).
- 100VG-AnyLAN: demand priority, no collisions, but it lost the competition to Fast Ethernet.
- Wi-Fi: 802.11b/a/g/n/ac/ax/be, 2.4/5/6 GHz, CSMA/CA and ACK, the hidden station problem and RTS/CTS, BSS/ESS/SSID.

## Check yourself

:::check
Q: When can a station transmit data in Token Ring?
A: Only when the token reaches it, and within the token holding time (THT).
Q: What happens when a cable breaks in an FDDI network?
A: The stations next to the break connect the primary ring to the secondary (wrap), one long ring is formed and the network keeps working.
Q: Why does Wi-Fi use CSMA/CA rather than CSMA/CD?
A: A radio station cannot hear other signals while transmitting, so detecting collisions is impossible — instead they are avoided and frames are confirmed with ACKs.
Q: What is the hidden station problem and how is it solved?
A: Two stations cannot hear each other but hear a common access point, so they transmit at once and collide; it is solved with an RTS/CTS exchange.
Q: Which IEEE standard does Wi-Fi 6 correspond to?
A: IEEE 802.11ax.
:::

:::link https://en.wikipedia.org/wiki/IEEE_802.11
IEEE 802.11
A detailed article on the Wi-Fi standards, frequencies, CSMA/CA and frame types.
:::
