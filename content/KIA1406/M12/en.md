---
title: Physical security. Natural and man-made threats, control measures, methods of destroying information
summary: The role of physical security in information security; threats — natural (earthquake, fire, flood, temperature), technological (power outages, accidents) and human (theft, vandalism, unauthorized entry, insider threat); layers of protection — site, building, room, device; control measures — deter, delay, detect, respond; access control (cards, biometrics, mantraps), CCTV, alarms; the server room environment — temperature, humidity, fire suppression, UPS and generators; destroying storage media — NIST SP 800-88 (Clear, Purge, Destroy).
---

## Why does this topic matter?

Even the strongest encryption and firewall do not protect against a thief who carries off a server, a flood in the server room or a hard disk thrown in the bin. For an attacker, walking into an office as a "courier" or a "technician" and plugging a USB device into a computer is often easier than breaking into the network remotely. That is why physical security is the **first layer** of protection: whoever gets physical access can bypass all the other measures.

:::note
**After this lesson you will be able to:**

- classify physical threats as natural, technological and human;
- explain the layers of protection and the purposes of control measures;
- know the requirements for a server room environment;
- choose a suitable destruction method for a storage medium.
:::

## Physical threats

| Type | Examples | Main measures |
|---|---|---|
| **Natural** | earthquake (Uzbekistan is a seismically active region), flood, lightning, extreme heat or cold, dust | site selection, seismic reinforcement, mounting above floor level, lightning protection, a backup site |
| **Technological** | power outages and surges, fire, burst pipes, air conditioning failure | UPS, generator, fire alarm and suppression system, water leak sensors, backup cooling |
| **Human** | theft (laptops, disks), vandalism, unauthorized entry, tailgating, insider threat, social engineering | access control, surveillance, locks, staff screening, awareness |

## Layers of protection and control measures

Physical protection is built **in depth** (defense in depth): the site perimeter (wall, gate, lighting) → the building (reception, turnstile) → the floor and room (card, code) → the server rack (lock) → the device (case lock, BIOS password, disk encryption).

| Purpose | Examples |
|---|---|
| **Deter** | warning signs, lighting, visible cameras, a guard |
| **Delay/deny** | walls, doors, locks, turnstiles, a **mantrap** (only one person passes at a time) |
| **Detect** | motion and door sensors, CCTV, access logs, alarms |
| **Respond** | security service, calling the police, an incident response plan |

**Access control**: proximity cards (RFID — can be cloned, so protected cards are preferred), PIN codes, biometrics (M10) and their combinations; visitors are registered, wear a badge and are escorted. **Tailgating** (piggybacking) — entering behind an employee who opened the door — is the most common weakness; the protection is turnstiles, mantraps and staff awareness.

**Clean desk and locked screen**: no documents left out, `Win + L` when leaving the computer, no sticky note with a password on the screen; laptops are secured with a cable lock and their disks are encrypted (BitLocker).

## The server room and data centre environment

| Parameter | Requirement |
|---|---|
| **Temperature** | ASHRAE recommendation — 18–27 °C inlet air; hot and cold aisles |
| **Humidity** | too low — static electricity, too high — condensation and corrosion; monitoring sensors |
| **Power** | UPS (short outages and clean voltage), generator (long outages), two independent feeds |
| **Fire** | early detection (smoke detectors, VESDA); waterless gas suppression (FM-200, Novec 1230, inert gases) — water damages equipment |
| **Water** | the room should not be under pipes; a raised floor, leak sensors |
| **Access** | authorized staff only, two-factor entry, a log and a camera |

The reliability of data centres is rated by **Tier I–IV** levels (Uptime Institute): Tier I — no redundancy (~99.67%), Tier IV — all systems duplicated and fault-tolerant (~99.995%, about 26 minutes of downtime per year).

## Destroying storage media

"Deleting" a file or formatting a disk does not destroy the data — it only marks the space as "free"; special software recovers it. There are many cases of bank account numbers and medical data found on used disks bought second-hand. **NIST SP 800-88** defines three levels:

| Level | Essence | Methods | When |
|---|---|---|---|
| **Clear** | protection against simple recovery tools | overwriting all addressable locations (one pass is enough), factory reset | the medium is reused inside the organization |
| **Purge** | protection even against laboratory methods | ATA/NVMe Sanitize, Secure Erase, **cryptographic erase** (destroying the encryption key), degausser (magnetic media only) | the medium leaves the organization |
| **Destroy** | the medium can no longer be used | shredding, disintegration, melting, incineration | the medium is disposed of, or the data is highly sensitive |

For **SSDs and flash memory** overwriting is unreliable: the controller spreads writes over other cells (wear leveling) and hides spare blocks. For them the manufacturer's Sanitize command or cryptographic erase is used; a degausser does not affect an SSD. For paper documents, shredders rated under DIN 66399: at least P-4 (small particles) for confidential documents. Every destruction is documented with a **certificate**.

## Worked example 1: server room environment monitoring

Each line holds a time, a temperature (°C) and relative humidity (%). Teaching thresholds: temperature > 32 — `CRITICAL`; above 27 or below 15 — `WARNING`; humidity < 20 — risk of static electricity, > 80 — risk of condensation (`WARNING`). Print the status of each measurement and the totals at the end.

```python
import sys

kritik = ogoh = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    vaqt, t_s, n_s = satr.split()
    t, n = float(t_s), float(n_s)
    sabablar = []
    daraja = 0                                    # 0 - normal, 1 - warning, 2 - critical
    if t > 32:
        sabablar.append("temperature")
        daraja = 2
    elif t > 27 or t < 15:
        sabablar.append("temperature")
        daraja = max(daraja, 1)
    if n < 20:
        sabablar.append("low humidity - static electricity")
        daraja = max(daraja, 1)
    elif n > 80:
        sabablar.append("high humidity - condensation")
        daraja = max(daraja, 1)
    holat = ["OK", "WARNING", "CRITICAL"][daraja]
    kritik += daraja == 2
    ogoh += daraja == 1
    print(f"{vaqt} {t_s}C {n_s}%: {holat}" + (f" ({', '.join(sabablar)})" if sabablar else ""))
print(f"critical: {kritik}, warnings: {ogoh}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] HOLAT = {"OK", "WARNING", "CRITICAL"};
        int kritik = 0, ogoh = 0;
        while (in.hasNext()) {
            String vaqt = in.next(), tS = in.next(), nS = in.next();
            double t = Double.parseDouble(tS), n = Double.parseDouble(nS);
            List<String> sabablar = new ArrayList<>();
            int daraja = 0;                                  // 0 - normal, 1 - warning, 2 - critical
            if (t > 32) {
                sabablar.add("temperature");
                daraja = 2;
            } else if (t > 27 || t < 15) {
                sabablar.add("temperature");
                daraja = Math.max(daraja, 1);
            }
            if (n < 20) {
                sabablar.add("low humidity - static electricity");
                daraja = Math.max(daraja, 1);
            } else if (n > 80) {
                sabablar.add("high humidity - condensation");
                daraja = Math.max(daraja, 1);
            }
            if (daraja == 2) kritik++;
            if (daraja == 1) ogoh++;
            String qator = vaqt + " " + tS + "C " + nS + "%: " + HOLAT[daraja];
            if (!sabablar.isEmpty()) qator += " (" + String.join(", ", sabablar) + ")";
            System.out.println(qator);
        }
        System.out.println("critical: " + kritik + ", warnings: " + ogoh);
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
    const string HOLAT[] = {"OK", "WARNING", "CRITICAL"};
    int kritik = 0, ogoh = 0;
    string vaqt, tS, nS;
    while (cin >> vaqt >> tS >> nS) {
        double t = stod(tS), n = stod(nS);
        vector<string> sabablar;
        int daraja = 0;                                      // 0 - normal, 1 - warning, 2 - critical
        if (t > 32) {
            sabablar.push_back("temperature");
            daraja = 2;
        } else if (t > 27 || t < 15) {
            sabablar.push_back("temperature");
            daraja = max(daraja, 1);
        }
        if (n < 20) {
            sabablar.push_back("low humidity - static electricity");
            daraja = max(daraja, 1);
        } else if (n > 80) {
            sabablar.push_back("high humidity - condensation");
            daraja = max(daraja, 1);
        }
        if (daraja == 2) kritik++;
        if (daraja == 1) ogoh++;
        string qator = vaqt + " " + tS + "C " + nS + "%: " + HOLAT[daraja];
        if (!sabablar.empty()) {
            qator += " (";
            for (size_t i = 0; i < sabablar.size(); i++) qator += (i ? ", " : "") + sabablar[i];
            qator += ")";
        }
        cout << qator << endl;
    }
    cout << "critical: " << kritik << ", warnings: " << ogoh << endl;
    return 0;
}
```

Input:

```text
08:00 22.5 45
10:00 26.0 38
12:00 28.4 30
14:00 33.1 25
16:00 24.0 15
18:00 21.5 85
```

Output:

```text
08:00 22.5C 45%: OK
10:00 26.0C 38%: OK
12:00 28.4C 30%: WARNING (temperature)
14:00 33.1C 25%: CRITICAL (temperature)
16:00 24.0C 15%: WARNING (low humidity - static electricity)
18:00 21.5C 85%: WARNING (high humidity - condensation)
critical: 1, warnings: 3
```

The temperature rose steadily from 10:00 to 14:00 — a sign of an air conditioning failure: if action is taken at the warning stage (backup cooling, reducing the load), the servers do not shut down in an emergency. Real systems (SNMP sensors, DCIM) send SMS and email notifications using exactly such rules.

## Worked example 2: choosing a destruction method

Each line holds the medium's name, type (HDD, SSD, PAPER), size (GB), write speed (MB/s) and its future: `ichki` — reused inside the organization, `tashqi` — leaves the organization (sold, sent for repair), `utilizatsiya` — disposed of. Choose the method following the idea of NIST SP 800-88; when clearing an HDD, compute the time of one full overwrite.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, tur, hajm, tezlik, maqsad = satr.split()
    hajm, tezlik = float(hajm), float(tezlik)
    if tur == "PAPER":
        usul = "Destroy - cross-cut shredding (P-4 or higher)"
    elif maqsad == "utilizatsiya":
        usul = "Destroy - shredding or melting"
    elif tur == "HDD" and maqsad == "ichki":
        soat = hajm * 1000 / tezlik / 3600            # GB -> MB, seconds -> hours
        usul = f"Clear - overwrite once, ~{soat:.1f} h"
    elif tur == "HDD":
        usul = "Purge - degausser or ATA Secure Erase"
    else:                                             # SSD: overwriting is unreliable (wear leveling)
        usul = "Purge - cryptographic erase"
    print(f"{nom} ({tur}, {maqsad}): {usul}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next(), tur = in.next();
            double hajm = Double.parseDouble(in.next()), tezlik = Double.parseDouble(in.next());
            String maqsad = in.next();
            String usul;
            if (tur.equals("PAPER")) {
                usul = "Destroy - cross-cut shredding (P-4 or higher)";
            } else if (maqsad.equals("utilizatsiya")) {
                usul = "Destroy - shredding or melting";
            } else if (tur.equals("HDD") && maqsad.equals("ichki")) {
                double soat = hajm * 1000 / tezlik / 3600;        // GB -> MB, seconds -> hours
                usul = String.format("Clear - overwrite once, ~%.1f h", soat);
            } else if (tur.equals("HDD")) {
                usul = "Purge - degausser or ATA Secure Erase";
            } else {                                              // SSD: overwriting is unreliable (wear leveling)
                usul = "Purge - cryptographic erase";
            }
            System.out.println(nom + " (" + tur + ", " + maqsad + "): " + usul);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string nom, tur, maqsad;
    double hajm, tezlik;
    while (cin >> nom >> tur >> hajm >> tezlik >> maqsad) {
        string usul;
        if (tur == "PAPER") {
            usul = "Destroy - cross-cut shredding (P-4 or higher)";
        } else if (maqsad == "utilizatsiya") {
            usul = "Destroy - shredding or melting";
        } else if (tur == "HDD" && maqsad == "ichki") {
            double soat = hajm * 1000 / tezlik / 3600;            // GB -> MB, seconds -> hours
            char buf[96];
            snprintf(buf, sizeof buf, "Clear - overwrite once, ~%.1f h", soat);
            usul = buf;
        } else if (tur == "HDD") {
            usul = "Purge - degausser or ATA Secure Erase";
        } else {                                                  // SSD: overwriting is unreliable (wear leveling)
            usul = "Purge - cryptographic erase";
        }
        cout << nom << " (" << tur << ", " << maqsad << "): " << usul << endl;
    }
    return 0;
}
```

Input:

```text
disk1 HDD 2000 150 ichki
disk2 HDD 1000 120 tashqi
ssd1 SSD 512 2000 ichki
ssd2 SSD 1000 3000 utilizatsiya
hujjat PAPER 0 0 utilizatsiya
disk3 HDD 500 120 ichki
```

Output:

```text
disk1 (HDD, ichki): Clear - overwrite once, ~3.7 h
disk2 (HDD, tashqi): Purge - degausser or ATA Secure Erase
ssd1 (SSD, ichki): Purge - cryptographic erase
ssd2 (SSD, utilizatsiya): Destroy - shredding or melting
hujjat (PAPER, utilizatsiya): Destroy - cross-cut shredding (P-4 or higher)
disk3 (HDD, ichki): Clear - overwrite once, ~1.2 h
```

Overwriting a 2 TB disk once takes about 4 hours; the old "7 passes" (DoD 5220.22-M) or "35 passes" (Gutmann) methods are unnecessary for modern disks and just waste time. For an SSD even overwriting is not always enough — that is why Purge is chosen here. If the disk was encrypted from the start (BitLocker, FileVault), destroying the key instantly makes the whole disk unreadable — this is the idea of cryptographic erase.

:::warning
**Common mistakes**

- **Treating formatting as destruction.** A quick format leaves the data in place; apply Clear, Purge or Destroy.
- **Allowing tailgating.** Holding the door for a stranger "out of politeness" is the simplest route to a physical breach.
- **Using the server room as a storeroom.** Cardboard boxes raise the fire risk and undermine access control.
- **Keeping the backup in the same room as the original system.** A fire or flood destroys both; keep a copy in another building (the 3-2-1 rule).
- **Plugging a found USB device into a computer.** It may be "bait" that attackers leave on purpose.
:::

:::tip
In Windows the command `cipher /w:C:\` overwrites the free space on a disk and destroys the remnants of previously "deleted" files. Before selling or handing over a computer: if BitLocker is on — "Reset this PC" → "Remove everything" → "Clean the drive".
:::

## Summary

- Physical security is the first layer of protection; threats are natural, technological and human.
- Layers of protection: perimeter, building, room, rack, device; purposes — deter, delay, detect, respond.
- Access control: cards, PINs, biometrics, mantraps; tailgating is the main weakness.
- Server room: 18–27 °C, humidity control, UPS and generator, gas fire suppression, water sensors.
- NIST SP 800-88: Clear (reuse inside), Purge (leaving the organization; Sanitize or crypto erase for SSDs), Destroy (disposal).

## Check yourself

:::check
Q: Against which threat is a mantrap used?
A: Against tailgating — only one person passes at a time.
Q: Why is a fire in a server room extinguished with gas rather than water?
A: Water damages electronic equipment; special gases put out the fire without harming it.
Q: What risk does very low humidity pose to a server room?
A: Static electricity builds up, and a discharge can damage electronic components.
Q: Which method fits an SSD that will leave the organization?
A: Purge — the Sanitize command or cryptographic erase; plain overwriting is unreliable because of wear leveling.
Q: Does a quick format destroy the data on a disk?
A: No, it only marks the space as free; special software can recover the data.
:::

:::link https://csrc.nist.gov/pubs/sp/800/88/r1/final
NIST SP 800-88 Rev. 1: Guidelines for Media Sanitization
Guidelines for sanitizing and destroying storage media.
:::
