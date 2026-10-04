---
title: Simsiz axborot uzatish texnologiyalari. Wi-Fi tarmogʻini sozlash
summary: Wi-Fi chastota diapazonlari va kanallari, 2,4 GHz da kesishmaydigan 1, 6, 11-kanallar; kanal kengligi; signal kuchi (dBm), signal/shovqin va erkin fazodagi soʻnish; xavfsizlik — WEP, WPA, WPA2, WPA3, WPS xavfi; kirish nuqtasi yoki routerni sozlash bosqichlari; qoʻshni tarmoqlarni tahlil qilib, kanal tanlash.
---

## Nima uchun bu mavzu muhim?

Koʻp qavatli uyda har bir xonadonning routeri bor va ularning aksariyati 2,4 GHz dagi bir xil kanalda ishlaydi — natijada Wi-Fi "bor, lekin ishlamaydi". Yana bir xavf: zavod sozlamalaridagi parol va eski shifrlash bilan qoldirilgan router begonalarga tarmoqni ochib beradi. Bu mashgʻulotda simsiz tarmoqni toʻgʻri sozlashni — kanal, kenglik va xavfsizlikni — hamda signal sifatini hisoblashni oʻrganamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- 2,4, 5 va 6 GHz diapazonlarining farqini va 2,4 GHz da nima uchun faqat 1, 6, 11-kanallar tavsiya etilishini tushuntira olasiz;
- qoʻshni tarmoqlar roʻyxati boʻyicha eng kam band kanalni tanlay olasiz;
- signal kuchini dBm da baholab, masofa va chastotaning taʼsirini hisoblay olasiz;
- xavfsizlik protokollarini solishtirib, uy yoki ofis routerini xavfsiz sozlay olasiz.
:::

## Diapazonlar va kanallar

| Diapazon | Kanallar | Afzalligi | Kamchiligi |
|---|---|---|---|
| **2,4 GHz** | 1–13 (har biri 5 MHz oraliqda, kengligi 20 MHz) | devorlardan yaxshi oʻtadi, uzoqqa yetadi | faqat 3 ta kesishmaydigan kanal, mikrotoʻlqinli pech va Bluetooth xalaqiti |
| **5 GHz** | 36–165 (20 MHz qadam bilan, 20+ kesishmaydigan kanal) | kanallar koʻp, tezlik yuqori | devorlardan yomonroq oʻtadi, masofa qisqaroq |
| **6 GHz** (Wi-Fi 6E/7) | 59 ta 20 MHz kanal | toza diapazon, faqat yangi qurilmalar | eng qisqa masofa |

2,4 GHz da *c*-kanal markazi = 2407 + 5·*c* MHz. Kanal kengligi 20 MHz boʻlgani uchun markazlari 20 MHz dan kam farq qiladigan kanallar (yaʼni raqami 4 dan kam farq qiladiganlar) bir-birini **kesishadi** va xalaqit beradi. Shuning uchun kesishmaydigan uchlik — **1, 6, 11**.

**Kanal kengligi**: 20 MHz — eng barqaror; 40 MHz — ikki baravar tez, lekin 2,4 GHz da deyarli butun diapazonni egallaydi; 5 GHz da 80 va 160 MHz kengliklar Wi-Fi 5/6 ning yuqori tezligini beradi.

## Signal kuchi

Wi-Fi signal kuchi **dBm** da (1 milliwattga nisbatan desibel) oʻlchanadi: 0 dBm = 1 mW, 20 dBm = 100 mW, −70 dBm = 0,0000001 mW. Qiymat manfiy va 0 ga qanchalik yaqin boʻlsa, signal shunchalik kuchli.

| Qabul qilingan signal | Sifat |
|---|---|
| −50 dBm va undan yuqori | aʼlo |
| −67 … −50 dBm | yaxshi (video qoʻngʻiroq, striming) |
| −80 … −67 dBm | past (sahifalar ochiladi, sekin) |
| −80 dBm dan past | amalda ishlamaydi |

Ochiq fazoda soʻnish (**FSPL**, free-space path loss): FSPL(dB) = 20·lg(*d*, km) + 20·lg(*f*, MHz) + 32,44. Masofa 2 marta oshsa — 6 dB yoʻqotiladi; chastota 2,4 dan 5 GHz ga oshsa — taxminan 6,5 dB koʻproq. Devorlar qoʻshimcha 3–15 dB "yeydi".

## Xavfsizlik

| Protokol | Yil | Shifrlash | Holati |
|---|---|---|---|
| **WEP** | 1997 | RC4 | bir necha daqiqada buziladi — **ishlatmang** |
| **WPA** | 2003 | TKIP | eskirgan |
| **WPA2** | 2004 | AES-CCMP | hozirgi minimal talab; kuchli parol bilan |
| **WPA3** | 2018 | AES, SAE kalit almashinuvi | eng yaxshi tanlov; parolni tanlab buzishga chidamli |

Qoʻshimcha qoidalar: **WPS** ni oʻchiring (8 raqamli PIN tez buziladi); routerning **administrator parolini** almashtiring; dasturiy taʼminotni (firmware) yangilab turing; mehmonlar uchun alohida **mehmon tarmogʻi** yarating. SSID ni yashirish va MAC filtrlash haqiqiy himoya emas — sniffer bilan SSID ham, ruxsat etilgan MAC lar ham koʻrinadi.

## Amaliy ish: kirish nuqtasini sozlash

1. Router yoki kirish nuqtasiga kabel bilan ulanib, brauzerda boshqaruv sahifasini oching (odatda `192.168.0.1` yoki `192.168.1.1`). Packet Tracer da — **WRT300N** yoki **AccessPoint-PT** qurilmasining **Config → Wireless** varagʻi.
2. Administrator parolini almashtiring.
3. **SSID** bering (shaxsiy maʼlumotsiz, masalan `AIFU-Lab-3`).
4. Xavfsizlik: **WPA2-Personal (AES)** yoki **WPA3-Personal**, kamida 12 belgili parol.
5. Qoʻshni tarmoqlarni koʻring: Windows da

```text
netsh wlan show networks mode=bssid
```

   — har bir tarmoqning SSID, signal (%), kanal va standarti chiqadi. Quyidagi birinchi misol dasturi bilan eng kam band kanalni tanlang.
6. 2,4 GHz uchun kanal kengligini 20 MHz qilib qoʻying; 5 GHz uchun 40 yoki 80 MHz.
7. Noutbukni ulang va `netsh wlan show interfaces` bilan signal, kanal va tezlikni tekshiring.

## Ishlangan misol 1: 2,4 GHz kanalini tanlash

Birinchi satrda qoʻshni tarmoqlar soni *n*, ikkinchi satrda ularning kanallari (1–13). Kesishmaydigan 1, 6 va 11-kanallarning har biri uchun u bilan kesishadigan qoʻshnilar sonini (kanal raqamlari farqi 4 dan kichik) chiqaring va eng kam band kanalni tavsiya qiling (teng boʻlsa — kichigi).

```python
n = int(input())
qoshnilar = list(map(int, input().split()))
eng_yaxshi, eng_kam = 0, n + 1
for kanal in (1, 6, 11):
    band = sum(1 for q in qoshnilar if abs(q - kanal) < 4)   # 20 MHz kesishuvi
    print("kanal " + str(kanal) + ": " + str(band))
    if band < eng_kam:
        eng_yaxshi, eng_kam = kanal, band
print("tavsiya: " + str(eng_yaxshi))
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
                if (Math.abs(q - kanal) < 4) band++;                // 20 MHz kesishuvi
            }
            System.out.println("kanal " + kanal + ": " + band);
            if (band < engKam) {
                engYaxshi = kanal;
                engKam = band;
            }
        }
        System.out.println("tavsiya: " + engYaxshi);
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
            if (abs(q - kanal) < 4) band++;                         // 20 MHz kesishuvi
        }
        cout << "kanal " << kanal << ": " << band << endl;
        if (band < engKam) {
            engYaxshi = kanal;
            engKam = band;
        }
    }
    cout << "tavsiya: " << engYaxshi << endl;
    return 0;
}
```

Kirish:

```text
6
1 1 6 3 4 13
```

Chiqish:

```text
kanal 1: 4
kanal 6: 3
kanal 11: 1
tavsiya: 11
```

Diqqat qiling: 3 va 4-kanallardagi qoʻshnilar (notoʻgʻri sozlangan routerlar) bir vaqtda ham 1, ham 6-kanalga xalaqit beradi — 1, 6, 11 dan boshqa kanal tanlash nima uchun yomon ekani shunda.

## Ishlangan misol 2: masofa boʻyicha signal kuchi

Har bir satrda uzatgich quvvati *P* (dBm), chastota *f* (MHz) va masofa *d* (m). Ochiq fazodagi soʻnishni, qabul qilinadigan signalni (dBm, bir xona) va sifat bahosini chiqaring. Kirish fayl oxirigacha oʻqiladi.

```python
import math
import sys

for satr in sys.stdin:
    if not satr.strip():
        continue
    p, f, d = map(int, satr.split())
    fspl = 20 * math.log10(d / 1000) + 20 * math.log10(f) + 32.44   # d km da
    signal = p - fspl
    if signal >= -50:
        baho = "aʼlo"
    elif signal >= -67:
        baho = "yaxshi"
    elif signal >= -80:
        baho = "past"
    else:
        baho = "ishlamaydi"
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
            double fspl = 20 * Math.log10(d / 1000.0) + 20 * Math.log10(f) + 32.44;   // d km da
            double signal = p - fspl;
            String baho;
            if (signal >= -50) {
                baho = "aʼlo";
            } else if (signal >= -67) {
                baho = "yaxshi";
            } else if (signal >= -80) {
                baho = "past";
            } else {
                baho = "ishlamaydi";
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
        double fspl = 20 * log10(d / 1000.0) + 20 * log10((double) f) + 32.44;   // d km da
        double signal = p - fspl;
        string baho;
        if (signal >= -50) {
            baho = "aʼlo";
        } else if (signal >= -67) {
            baho = "yaxshi";
        } else if (signal >= -80) {
            baho = "past";
        } else {
            baho = "ishlamaydi";
        }
        printf("%.1f %.1f %s\n", fspl, signal, baho.c_str());
    }
    return 0;
}
```

Kirish:

```text
20 2437 10
20 2437 50
20 5180 50
20 5180 200
17 2412 1000
```

Chiqish:

```text
60.2 -40.2 aʼlo
74.2 -54.2 yaxshi
80.7 -60.7 yaxshi
92.7 -72.7 past
100.1 -83.1 ishlamaydi
```

Bir xil 50 m masofada 5 GHz signali 2,4 GHz ga qaraganda ~6,5 dB kuchsizroq. Bu ochiq fazo uchun hisob — xonadagi har bir devor qiymatni yana pasaytiradi, shuning uchun uyda haqiqiy masofa ancha qisqa.

:::warning
**Koʻp uchraydigan xatolar**

- **2,4 GHz da 3, 4 yoki 9-kanalni tanlash.** Bunday kanal ikki "toza" kanalga birdan xalaqit beradi va ulardan xalaqit oladi.
- **2,4 GHz da 40 MHz kenglik.** U 13 kanaldan 9 tasini egallaydi va qoʻshnilar bilan doimiy toʻqnashuvga olib keladi.
- **SSID ni yashirish yoki MAC filtrlashni himoya deb hisoblash.** Ikkalasi ham sniffer bilan oson aylanib oʻtiladi; haqiqiy himoya — WPA2/WPA3 va kuchli parol.
- **dBm qiymatini oddiy son kabi solishtirish.** −60 dBm signal −70 dBm dan 10 baravar kuchli; 3 dB farq — quvvat 2 baravar.
:::

:::tip
Telefoningizga Wi-Fi analizator ilovasini oʻrnatib, uyingiz yoki yotoqxonangizda qoʻshni tarmoqlar qaysi kanallarda ekanini koʻring. Routerdan 1, 5 va 10 metr uzoqlikda (devor ortida) signal kuchini yozib oling va ikkinchi misol natijasi bilan solishtiring.
:::

## Xulosa

- 2,4 GHz uzoqqa yetadi, lekin kanallari kam (kesishmaydigan — 1, 6, 11); 5 va 6 GHz tezroq va tozaroq, lekin masofasi qisqa.
- Signal kuchi dBm da: −50 va yuqori — aʼlo, −80 dan past — ishlamaydi; ochiq fazodagi soʻnish masofa va chastota bilan logarifmik oʻsadi.
- Xavfsizlik: WPA2 (AES) — minimal, WPA3 — eng yaxshisi; WEP va WPS — xavfli.
- Sozlash: admin paroli, SSID, WPA2/WPA3 va kuchli parol, qoʻshnilarga qarab kanal tanlash, mos kanal kengligi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun 2,4 GHz da faqat 1, 6 va 11-kanallar tavsiya etiladi?
A: Kanallar markazlari 5 MHz oraliqda, kengligi esa 20 MHz — faqat shu uchlik bir-biri bilan kesishmaydi.
Q: −55 dBm va −75 dBm signallardan qaysi biri kuchliroq va necha baravar?
A: −55 dBm; 20 dB farq — quvvat 100 baravar katta.
Q: Uy routeri uchun qaysi xavfsizlik protokoli tanlanishi kerak?
A: WPA3-Personal yoki u boʻlmasa WPA2-Personal (AES), kuchli parol bilan.
Q: Nima uchun WPS ni oʻchirish tavsiya etiladi?
A: Uning 8 raqamli PIN kodi tanlab buzish hujumiga zaif.
Q: Masofa 2 baravar oshsa, ochiq fazodagi soʻnish qanchaga oshadi?
A: Taxminan 6 dB ga (20·lg 2 ≈ 6).
:::

:::link https://en.wikipedia.org/wiki/List_of_WLAN_channels
WLAN kanallari roʻyxati
2,4, 5 va 6 GHz diapazonlaridagi Wi-Fi kanallari va chastotalari (ingliz tilida).
:::
