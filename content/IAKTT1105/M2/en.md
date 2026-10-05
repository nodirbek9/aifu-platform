---
title: The infrastructure of information technologies
summary: The concept and components of IT infrastructure; computer architecture — processor, main memory, storage (HDD, SSD), input-output devices; units of information; servers and data centres; network infrastructure; layers of system and application software; own infrastructure versus the cloud — CAPEX and OPEX; total cost of ownership (TCO); economic criteria in infrastructure planning; a program computing TCO.
---

## Why does this topic matter?

Behind every digital service — an online cash register, a banking app, an accounting program — there is an **infrastructure** of hardware, networks and software. For an economist this is not only a technical but also a financial question: how many computers and servers are needed, is it better to buy them or rent them from the cloud, and what will the total cost be over the years? The right decision saves considerable infrastructure spending.

:::note
**After this lesson you will be able to:**

- name the components of IT infrastructure;
- explain the main devices of a computer and their roles;
- use units of information;
- compare own infrastructure and the cloud in terms of CAPEX/OPEX;
- compute the total cost of ownership (TCO).
:::

## Components of IT infrastructure

| Layer | Contents |
|---|---|
| **Hardware** | workstations, laptops, servers, storage systems, printers, cash registers, mobile devices |
| **Network** | cables, Wi-Fi access points, switches, routers, the Internet link (M6) |
| **System software** | operating systems, virtualization, database management systems (S3, M7) |
| **Application software** | office suites, accounting, ERP, CRM (M3) |
| **Data centre** | a server room or an external data centre: power supply, cooling, security |
| **Services and people** | technical support, administrators, update and backup processes |

## Computer architecture

| Device | Role | Key indicator |
|---|---|---|
| **Processor** (CPU) | executes instructions, computes | number of cores, frequency (GHz) |
| **Main memory** (RAM) | temporarily holds running programs and data; cleared when the power goes off | size (GB) |
| **Storage** | keeps files long term: HDD (cheap, slow), SSD (fast, durable) | size (TB), speed |
| **Input devices** | keyboard, mouse, scanner, barcode reader (S2) | — |
| **Output devices** | monitor, printer, receipt printer (S2) | resolution, speed |
| **Motherboard and bus** | connects the devices | — |

**Units of information**: 1 byte = 8 bits; 1 KB ≈ 10³ bytes, 1 MB ≈ 10⁶, 1 GB ≈ 10⁹, 1 TB ≈ 10¹² bytes (in binary units 1 KiB = 1024 bytes). Network speed is measured in **bits** (Mbit/s), file size in **bytes**: a 100 Mbit/s link transfers about 12.5 MB per second.

## Servers and data centres

A **server** is a powerful, reliable computer that serves other computers (a file, mail, database or web server). With **virtualization**, several virtual servers can run on one physical server — making better use of the hardware.

A **data centre** is a special building or room for servers: uninterruptible power supply, cooling, fire suppression, physical protection. Building its own data centre is expensive for a small company, so **colocation** (placing your own server in an external centre) or the **cloud** (M11) is often chosen.

## CAPEX, OPEX and total cost of ownership

| Model | Cost type | Advantage | Drawback |
|---|---|---|---|
| **Own infrastructure** (on-premise) | **CAPEX** — a large capital expense at the start (purchase), then maintenance | full control, can be cheaper in the long run | a large initial investment, staff needed, obsolescence |
| **Cloud** | **OPEX** — a monthly payment for use | quick start, scaling when needed, no initial investment | can become expensive in the long run, dependence on the provider |

**Total cost of ownership (TCO)** is all the costs of infrastructure over its whole life: purchase, installation, licences, electricity, maintenance, staff salaries, repairs and disposal. Deciding by purchase price alone is a common mistake.

## Worked example: a server or the cloud?

The server purchase price, the yearly maintenance cost, the cloud's monthly fee and the number of years are given. Print the accumulated costs at the end of each year and find the first year in which the own server becomes cheaper than the cloud (the payback year).

```python
import sys

xarid, xizmat, bulut_oy, yillar = map(int, sys.stdin.read().split())
server_jami, bulut_jami = xarid, 0              # the server is bought at the start
qoplanish = 0
for y in range(1, yillar + 1):
    server_jami += xizmat                        # yearly maintenance
    bulut_jami += bulut_oy * 12                  # 12 monthly payments
    if qoplanish == 0 and server_jami < bulut_jami:
        qoplanish = y
    print(f"year {y}: server {server_jami}, cloud {bulut_jami}")
print(f"payback year: {qoplanish}" if qoplanish else "the server does not pay off")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long xarid = in.nextLong(), xizmat = in.nextLong(), bulutOy = in.nextLong();
        int yillar = in.nextInt();
        long serverJami = xarid, bulutJami = 0;          // the server is bought at the start
        int qoplanish = 0;
        for (int y = 1; y <= yillar; y++) {
            serverJami += xizmat;                          // yearly maintenance
            bulutJami += bulutOy * 12;                     // 12 monthly payments
            if (qoplanish == 0 && serverJami < bulutJami) qoplanish = y;
            System.out.println("year " + y + ": server " + serverJami + ", cloud " + bulutJami);
        }
        System.out.println(qoplanish > 0 ? "payback year: " + qoplanish : "the server does not pay off");
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long xarid, xizmat, bulutOy;
    int yillar;
    cin >> xarid >> xizmat >> bulutOy >> yillar;
    long long serverJami = xarid, bulutJami = 0;          // the server is bought at the start
    int qoplanish = 0;
    for (int y = 1; y <= yillar; y++) {
        serverJami += xizmat;                              // yearly maintenance
        bulutJami += bulutOy * 12;                         // 12 monthly payments
        if (qoplanish == 0 && serverJami < bulutJami) qoplanish = y;
        cout << "year " << y << ": server " << serverJami << ", cloud " << bulutJami << endl;
    }
    if (qoplanish > 0) cout << "payback year: " << qoplanish << endl;
    else cout << "the server does not pay off" << endl;
    return 0;
}
```

Input:

```text
60000000 8000000 3000000 5
```

Output:

```text
year 1: server 68000000, cloud 36000000
year 2: server 76000000, cloud 72000000
year 3: server 84000000, cloud 108000000
year 4: server 92000000, cloud 144000000
year 5: server 100000000, cloud 180000000
payback year: 3
```

If the cloud costs 3 million soums a month, a 60-million-soum server is more profitable from the third year. If the cloud costs 1.5 million soums a month (`60000000 8000000 1500000 5`), the server does not pay off within 5 years — the cloud is cheaper. A real calculation also takes into account that a server becomes obsolete in 4–5 years, electricity, staff salaries, the time value of money (discounting) and the flexibility of the cloud.

:::warning
**Common mistakes**

- **Comparing only purchase prices.** TCO includes maintenance, electricity, staff and obsolescence.
- **Mixing up bits and bytes.** 100 Mbit/s is not 100 megabytes per second but about 12.5 megabytes.
- **Confusing main memory and storage.** RAM is cleared when the power goes off; files are kept on disk.
- **Not planning backups and an uninterruptible power supply.** An infrastructure outage directly harms the business.
:::

:::tip
On your computer, look at the processor, memory and disk indicators in Task Manager → Performance (Windows); compare them with your workload and conclude whether the computer is sufficient for its tasks or needs an upgrade.
:::

## Summary

- IT infrastructure: hardware, network, system and application software, a data centre, services and people.
- A computer: processor, main memory and storage, input-output devices.
- Units: bits and bytes, KB–TB; network speed in bits, file size in bytes.
- Own infrastructure is CAPEX, the cloud is OPEX; the decision is made on the basis of TCO.
- TCO accounts for all costs over the whole lifetime.

## Check yourself

:::check
Q: How does main memory differ from storage?
A: Main memory holds data temporarily and is cleared when the power goes off; storage keeps files long term.
Q: About how many megabytes per second does a 100 Mbit/s link transfer?
A: About 12.5 MB (100 / 8).
Q: What is the difference between CAPEX and OPEX?
A: CAPEX is an initial capital expense (a purchase); OPEX is an ongoing operating expense (for example, a monthly fee).
Q: Which costs are included in TCO?
A: Purchase, installation, licences, electricity, maintenance, staff, repairs and disposal.
:::

:::link https://www.gartner.com/en/information-technology/glossary/total-cost-of-ownership-tco
Gartner: Total Cost of Ownership (TCO)
A definition of the total cost of ownership concept.
:::
