---
title: Berilganlarni uzatish usullari. Berilganlarni uzatishning fizik muhitlari. Berilganlarni kodlash va uzatish usullari
summary: Uzatish rejimlari (simpleks, yarim dupleks, dupleks), ketma-ket va parallel, sinxron va asinxron uzatish; analog va raqamli signallar, modulyatsiya; fizik muhitlar — oʻralgan juftlik, koaksial kabel, optik tola, simsiz muhit; kanal sigʻimi (Naykvist va Shennon formulalari); kechikishlar; xatolarni aniqlash — juftlik biti, nazorat yigʻindisi, CRC.
---

## Nima uchun bu mavzu muhim?

OSI modelining eng pastki — fizik pogʻonasi "bitni qanday qilib kabel yoki havo orqali oʻtkazish" savoliga javob beradi. Nega oddiy telefon liniyasi orqali 56 Kbit/s dan tez ishlab boʻlmagan, optik tola esa terabitlarni uzatadi? Nega Wi-Fi devor ortida sekinlashadi? Nega 100 metrdan uzun Ethernet kabeli ishlamaydi? Bu savollarga javob signal, muhit va kanal sigʻimi tushunchalarida.

:::note
**Bu darsdan soʻng siz:**

- uzatish rejimlarini (simpleks, yarim dupleks, dupleks) va usullarini (ketma-ket/parallel, sinxron/asinxron) farqlay olasiz;
- asosiy fizik muhitlarni va ularning xususiyatlarini solishtira olasiz;
- Naykvist va Shennon formulalari bilan kanal sigʻimini hisoblay olasiz;
- uzatish va tarqalish kechikishlarini hisoblay olasiz;
- juftlik biti yordamida xato qanday aniqlanishini va uning cheklovlarini tushuntira olasiz.
:::

## Uzatish rejimlari va usullari

| Rejim | Maʼnosi | Misol |
|---|---|---|
| **Simpleks** | maʼlumot faqat bir yoʻnalishda | televizion eshittirish, klaviatura → kompyuter |
| **Yarim dupleks** (half-duplex) | ikki yoʻnalishda, lekin navbat bilan | ratsiya, konsentratorli Ethernet |
| **Toʻliq dupleks** (full-duplex) | ikki yoʻnalishda bir vaqtda | telefon suhbati, kommutatorli Ethernet |

- **Parallel uzatish** — bir nechta bit bir vaqtda alohida simlar orqali (eski printer porti). Qisqa masofada tez, lekin uzun masofada bitlar bir-biridan "orqada qoladi" (skew).
- **Ketma-ket uzatish** — bitlar bitta liniya orqali birin-ketin. Barcha zamonaviy tarmoqlar (Ethernet, USB, SATA) ketma-ket ishlaydi.
- **Asinxron uzatish** — har bir belgi start va stop bitlari bilan oʻraladi; qabul qiluvchi soatni har belgida qayta moslaydi (RS-232).
- **Sinxron uzatish** — katta bloklar uzatiladi, yuboruvchi va qabul qiluvchi soatlari signalning oʻzidan sinxronlanadi; sarf kamroq.

## Signallar va modulyatsiya

**Analog signal** uzluksiz oʻzgaradi (tovush, radio toʻlqin), **raqamli signal** esa cheklangan miqdordagi sathlarga ega. Kompyuter maʼlumoti raqamli, lekin uni radio yoki telefon liniyasi orqali uzatish uchun **tashuvchi toʻlqin** (sinusoida) parametrlari oʻzgartiriladi — bu **modulyatsiya**:

| Modulyatsiya | Nima oʻzgaradi |
|---|---|
| **AM / ASK** (amplituda) | toʻlqin balandligi |
| **FM / FSK** (chastota) | toʻlqin chastotasi |
| **PM / PSK** (faza) | toʻlqin fazasi (siljishi) |
| **QAM** (kvadratur amplituda) | amplituda va faza birga — bitta belgi bir nechta bitni tashiydi (Wi-Fi, kabel modemlar) |

Qabul qiluvchi tomonda teskari jarayon — **demodulyatsiya**. Ikkalasini bajaradigan qurilma **modem** (modulyator–demodulyator).

**Bod** (baud) — sekundiga signal oʻzgarishlari (belgilar) soni; **bit/s** — sekundiga bitlar soni. Agar bitta belgi *M* xil holatdan birini olsa, u log₂ *M* bit tashiydi: bit/s = bod × log₂ *M*. Masalan, 16-QAM da har bir belgi 4 bit tashiydi.

## Fizik muhitlar

| Muhit | Tuzilishi | Tezlik va masofa | Afzalligi | Kamchiligi |
|---|---|---|---|---|
| **Oʻralgan juftlik** (twisted pair) — UTP, STP | 4 juft mis sim, har bir juftlik buralgan | Cat5e: 1 Gbit/s, Cat6a: 10 Gbit/s; segment 100 m gacha | arzon, oson oʻrnatiladi | masofa cheklangan, elektromagnit shovqinga sezgir |
| **Koaksial kabel** | markaziy oʻtkazgich, izolyatsiya, oʻram (ekran) | yuzlab Mbit/s, yuzlab metr | shovqinga chidamli | qalin, qimmatroq; LAN da deyarli ishlatilmaydi (kabel TV, eski Ethernet) |
| **Optik tola** — bir modali (single-mode), koʻp modali (multi-mode) | shisha tola, yorugʻlik signali | 10–400 Gbit/s; koʻp modali — yuzlab metr, bir modali — oʻnlab km | juda katta sigʻim, shovqinga immun, uzoq masofa | qimmatroq, ulash maxsus asbob talab qiladi |
| **Simsiz** (radio, mikrotoʻlqin, infraqizil, sunʼiy yoʻldosh) | elektromagnit toʻlqinlar | Wi-Fi 6: bir necha Gbit/s, Bluetooth: Mbit/s | harakatchanlik, kabel shart emas | toʻsiqlar, shovqin, xavfsizlik |

Oʻralgan juftlikdagi buralish qoʻshni juftliklar orasidagi shovqinni (**perekrestli xalaqit**, crosstalk) kamaytiradi. Har qanday muhitda signal masofa bilan **soʻnadi** (attenuation) va **shovqin** qoʻshiladi — shuning uchun takrorlagichlar kerak.

## Kanal sigʻimi

**Naykvist formulasi** (shovqinsiz kanal): kengligi *B* Hz boʻlgan kanalda sekundiga eng koʻpi bilan 2*B* belgi uzatish mumkin, shuning uchun

*C* = 2 *B* log₂ *M* bit/s,

bunda *M* — signal sathlari soni.

**Shennon formulasi** (shovqinli kanal): sathlar sonidan qatʼi nazar, xatosiz uzatishning nazariy chegarasi

*C* = *B* log₂(1 + *S*/*N*) bit/s,

bunda *S*/*N* — signal/shovqin nisbati. U odatda **desibel**da beriladi: *S*/*N* = 10^(dB/10). Masalan, 30 dB → 1000 marta.

Haqiqiy tezlik ikkalasining **kichigidan** oshmaydi: sathlarni koʻpaytirish Naykvist chegarasini oshiradi, lekin shovqin Shennon chegarasidan oʻtishga yoʻl qoʻymaydi.

## Kechikishlar

| Kechikish | Formula | Nimaga bogʻliq |
|---|---|---|
| **Uzatish kechikishi** (transmission) | *L* / *R* (bitlar soni / kanal tezligi) | paket hajmi va kanal tezligi |
| **Tarqalish kechikishi** (propagation) | *d* / *v* (masofa / signal tezligi) | masofa; mis va optikada *v* ≈ 200 000 km/s |
| **Qayta ishlash** va **navbat** | — | marshrutizator yuklamasi |

Kichik paketni uzoq masofaga yuborishda tarqalish, katta faylni sekin kanalda yuborishda esa uzatish kechikishi ustun boʻladi.

## Maʼlumotni kodlash va xatolarni aniqlash

Raqamli signal kabelda **chiziqli kodlar** (line coding) yordamida beriladi: bit qiymatlari kuchlanish sathlari yoki ularning oʻzgarishi bilan ifodalanadi. Asosiy kodlar — NRZ, NRZI, Manchester, 4B/5B, MLT-3 — **A4 amaliy mashgʻulotida** batafsil oʻrganiladi. Yaxshi kod ikki talabga javob berishi kerak: qabul qiluvchi soatini sinxronlashtirish uchun yetarli oʻzgarishlar va doimiy tok tashkil etuvchisi yoʻqligi.

Shovqin sabab bitlar buzilishi mumkin, shuning uchun yuboruvchi qoʻshimcha **nazorat bitlari** qoʻshadi:

- **Juftlik biti** (parity) — birlar soni juft boʻlishi uchun bitta bit qoʻshiladi (juft juftlik). Bitta bit buzilsa — aniqlanadi, ikkita bit buzilsa — **aniqlanmaydi**.
- **Nazorat yigʻindisi** (checksum) — soʻzlar yigʻindisi (IP, TCP sarlavhalarida).
- **CRC** (siklik ortiqcha kod) — koʻphadga boʻlish qoldigʻi; Ethernet kadrining FCS maydoni CRC-32. Paket xatolarini juda yaxshi aniqlaydi.

## Ishlangan misol 1: kanal sigʻimi

Birinchi satrda *k*, keyingi *k* satrda kanal kengligi *B* (Hz), signal/shovqin nisbati (dB) va sathlar soni *M*. Har bir kanal uchun Naykvist chegarasi, Shennon chegarasi va ulardan kichigini (bit/s, butunga yaxlitlab) chiqaring.

```python
import math

k = int(input())
for _ in range(k):
    b, db, m = map(int, input().split())
    snr = 10 ** (db / 10)                   # desibeldan oddiy nisbatga
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
            double snr = Math.pow(10, db / 10.0);          // desibeldan oddiy nisbatga
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
        double snr = pow(10.0, db / 10.0);                 // desibeldan oddiy nisbatga
        double naykvist = 2 * b * log2((double) m);
        double shennon = b * log2(1 + snr);
        printf("%.0f %.0f %.0f\n", naykvist, shennon, min(naykvist, shennon));
    }
    return 0;
}
```

Kirish:

```text
4
3000 30 2
3000 30 16
20000000 20 4
1000000 0 2
```

Chiqish:

```text
6000 29902 6000
24000 29902 24000
80000000 133164230 80000000
2000000 1000000 1000000
```

Birinchi ikki qator — klassik telefon liniyasi (3 kHz, 30 dB): ikki sathli signal bilan 6 Kbit/s, 16 sath bilan 24 Kbit/s, lekin har qanday usulda ham ~30 Kbit/s dan oshib boʻlmaydi. Oxirgi qatorda shovqin signal bilan teng (0 dB): sathlar qancha boʻlmasin, 1 Mbit/s chegara.

## Ishlangan misol 2: juft juftlik biti

Birinchi satrda soʻz (ASCII harflar) berilgan. Har bir harfning 7 bitli kodini, juftlik bitini va uzatiladigan 8 bitni (kod + juftlik biti) chiqaring. Keyin *n* va qabul qilingan *n* ta 8 bitli soʻz keladi: har biri uchun birlar soni juft boʻlsa `OK`, aks holda `XATO` chiqaring.

```python
soz = input()
for belgi in soz:
    bitlar = format(ord(belgi), "07b")      # 7 bitli ASCII kod
    juftlik = bitlar.count("1") % 2         # birlar soni toq boʻlsa 1
    print(belgi, bitlar, juftlik, bitlar + str(juftlik))

n = int(input())
for _ in range(n):
    qabul = input()
    print(qabul, "OK" if qabul.count("1") % 2 == 0 else "XATO")
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
            String bitlar = String.format("%7s", Integer.toBinaryString(belgi)).replace(' ', '0');  // 7 bitli ASCII kod
            int juftlik = birlar(bitlar) % 2;                // birlar soni toq boʻlsa 1
            System.out.println(belgi + " " + bitlar + " " + juftlik + " " + bitlar + juftlik);
        }

        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String qabul = in.nextLine().trim();
            System.out.println(qabul + " " + (birlar(qabul) % 2 == 0 ? "OK" : "XATO"));
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
        string bitlar = bitset<7>(belgi).to_string();      // 7 bitli ASCII kod
        int juftlik = birlar(bitlar) % 2;                  // birlar soni toq boʻlsa 1
        cout << belgi << " " << bitlar << " " << juftlik << " " << bitlar << juftlik << endl;
    }

    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string qabul;
        cin >> qabul;
        cout << qabul << " " << (birlar(qabul) % 2 == 0 ? "OK" : "XATO") << endl;
    }
    return 0;
}
```

Kirish:

```text
Salom
3
11000011
11100011
10100011
```

Chiqish:

```text
S 1010011 0 10100110
a 1100001 1 11000011
l 1101100 0 11011000
o 1101111 0 11011110
m 1101101 1 11011011
11000011 OK
11100011 XATO
10100011 OK
```

Diqqat qiling: `a` harfining toʻgʻri soʻzi `11000011`. Ikkinchi qabul qilingan soʻzda bitta bit buzilgan — xato aniqlandi. Uchinchisida **ikkita** bit buzilgan (2- va 3-bitlar), lekin birlar soni yana juft — xato **aniqlanmadi**. Shuning uchun tarmoqlarda juftlik biti emas, CRC ishlatiladi.

## Ishlangan misol 3: uzatish va tarqalish kechikishi

Har bir satrda paket hajmi *L* (bayt), kanal tezligi *R* (Mbit/s), masofa *d* (km) va signal tezligi *v* (km/s). Uzatish kechikishini, tarqalish kechikishini va ularning yigʻindisini millisekundlarda (uch xona aniqlikda) chiqaring. Kirish fayl oxirigacha oʻqiladi.

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

Kirish:

```text
1500 100 1000 200000
10000000 10 10 200000
64 1000 36000 300000
```

Chiqish:

```text
0.120 5.000 5.120
8000.000 0.050 8000.050
0.001 120.000 120.001
```

Birinchi qator: 1500 baytlik kadr 1000 km ga — tarqalish ustun. Ikkinchisi: 10 MB fayl sekin kanalda — uzatish ustun. Uchinchisi: geostatsionar sunʼiy yoʻldoshgacha (36 000 km) kichik paket — tezlik qanchalik katta boʻlmasin, 120 ms kechikish qoladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Bod va bit/s ni bir xil deb hisoblash.** Ular faqat ikki sathli signalda teng; 16-QAM da bit/s bod dan 4 marta katta.
- **Desibelni toʻgʻridan-toʻgʻri formulaga qoʻyish.** Shennon formulasiga 30 emas, 10^(30/10) = 1000 qoʻyiladi.
- **Tezlikni oshirsak kechikish ham kamayadi deb oʻylash.** Tezlik faqat uzatish kechikishini kamaytiradi; tarqalish kechikishi masofa va yorugʻlik tezligiga bogʻliq.
- **Juftlik biti barcha xatolarni topadi deb hisoblash.** Juft sondagi bitlar buzilishi aniqlanmaydi.
- **Oʻralgan juftlik kabelini 100 m dan uzun tortish.** Ethernet standarti segmentni 100 m bilan cheklaydi; undan uzunida kommutator yoki optika kerak.
:::

:::tip
Kompyuteringizda `ping 8.8.8.8` ni ishga tushiring va javob vaqtini koʻring. Signal tezligini 200 000 km/s deb, bu vaqt ichida signal qancha masofani bosib oʻtishi mumkinligini hisoblang (javob vaqti — borish va qaytish). Natijani serverning taxminiy joylashuvi bilan solishtiring.
:::

## Xulosa

- Uzatish rejimlari: simpleks, yarim dupleks, toʻliq dupleks; zamonaviy tarmoqlar ketma-ket uzatadi.
- Modulyatsiya tashuvchi toʻlqinning amplitudasi, chastotasi yoki fazasini oʻzgartiradi; QAM bitta belgida bir nechta bit tashiydi.
- Muhitlar: oʻralgan juftlik (100 m gacha), koaksial, optik tola (eng katta sigʻim va masofa), simsiz.
- Naykvist: *C* = 2*B* log₂ *M*; Shennon: *C* = *B* log₂(1 + *S*/*N*); haqiqiy tezlik ikkalasining kichigidan oshmaydi.
- Kechikish = uzatish (*L*/*R*) + tarqalish (*d*/*v*) + qayta ishlash + navbat.
- Juftlik biti bitta bit xatosini topadi, ikkitasini topmaydi; tarmoqlarda CRC ishlatiladi.

## Oʻzingizni tekshiring

:::check
Q: Yarim dupleks va toʻliq dupleks rejimlarning farqi nimada?
A: Yarim dupleksda maʼlumot ikki yoʻnalishda navbat bilan, toʻliq dupleksda esa bir vaqtda uzatiladi.
Q: Kengligi 4000 Hz boʻlgan shovqinsiz kanalda 8 sathli signal bilan eng katta tezlik qancha?
A: Naykvist: 2 × 4000 × log₂ 8 = 24 000 bit/s.
Q: Signal/shovqin nisbati 20 dB boʻlsa, oddiy nisbat necha?
A: 10^(20/10) = 100.
Q: Optik tola oʻralgan juftlikdan qaysi jihatlari bilan ustun?
A: Sigʻimi va masofasi ancha katta, elektromagnit shovqinga taʼsirlanmaydi.
Q: 1000 baytli paket 1 Gbit/s kanalda qancha vaqtda uzatiladi?
A: 8000 bit / 10⁹ bit/s = 8 mikrosekund (0,008 ms).
:::

:::link https://en.wikipedia.org/wiki/Shannon%E2%80%93Hartley_theorem
Shennon–Hartli teoremasi
Shovqinli kanal sigʻimi formulasi, misollar va Naykvist chegarasi bilan bogʻliqligi (ingliz tilida).
:::
