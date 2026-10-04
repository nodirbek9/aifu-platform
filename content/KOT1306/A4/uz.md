---
title: Berilganlarni kodlash usullari
summary: Chiziqli kodlash vazifalari — sinxronizatsiya, doimiy tashkil etuvchi, polosa kengligi; NRZ, NRZI, Manchester, differensial Manchester, AMI, MLT-3 kodlari; mantiqiy kodlash — 4B/5B, 8B/10B, skremblerlash; bit qoʻshish (bit stuffing) va HDLC bayrogʻi; kodlarni signal diagrammasi boʻyicha tahlil qilish.
---

## Nima uchun bu mavzu muhim?

Kabeldan "1" va "0" emas, kuchlanish oʻzgarishlari oʻtadi. Qabul qiluvchi ularni qayta bitlarga aylantirishi uchun ikkala tomon bir xil **kod** ishlatishi kerak. Lekin oddiy "1 — yuqori, 0 — past" usuli uzun nollar ketma-ketligida qabul qiluvchi soatini adashtiradi: 50 ta nolni 49 ta yoki 51 ta deb oʻqish mumkin. Bu amaliy mashgʻulotda asosiy chiziqli kodlarni signal diagrammasi orqali oʻrganamiz, soʻngra bu muammolarni yechadigan **mantiqiy kodlash** (4B/5B) va **bit qoʻshish** usullarini dastur bilan modellashtiramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- yaxshi chiziqli kodga qoʻyiladigan talablarni (sinxronizatsiya, doimiy tashkil etuvchi yoʻqligi, tor polosa) tushuntira olasiz;
- NRZ, NRZI, Manchester, differensial Manchester, AMI va MLT-3 kodlari boʻyicha signal diagrammasini chiza olasiz;
- 4B/5B kodlash qanday qilib uzun nollar ketma-ketligini yoʻqotishini koʻrsata olasiz;
- bit qoʻshish (bit stuffing) usulini qoʻllab, kadr bayrogʻi maʼlumot ichida uchramasligini taʼminlay olasiz.
:::

## Yaxshi kodga talablar

| Talab | Maʼnosi |
|---|---|
| **Oʻz-oʻzini sinxronlash** | signalda yetarlicha oʻtishlar boʻlishi kerak — qabul qiluvchi ular orqali soatini tuzatadi |
| **Doimiy tashkil etuvchi yoʻqligi** (DC balance) | yuqori va past sathlar oʻrtacha teng boʻlishi kerak — transformatorlar doimiy tokni oʻtkazmaydi |
| **Tor spektr** | sekundiga signal oʻzgarishlari qanchalik kam boʻlsa, kabelga talab shuncha past |
| **Xatoni aniqlash** | baʼzi kodlarda taqiqlangan kombinatsiyalar xato belgisi boʻladi |
| **Soddalik va arzonlik** | kodlovchi va dekodlovchi qurilma oddiy boʻlishi kerak |

## Asosiy chiziqli kodlar

Quyida `10110001` ketma-ketligi uchun sathlar (`+` — yuqori, `-` — past, `0` — nol sath) berilgan. Manchester kodida har bir bit ikki yarim davrga ega.

| Kod | Qoida | `1 0 1 1 0 0 0 1` |
|---|---|---|
| **NRZ-L** | 1 → `+`, 0 → `-` | `+ - + + - - - +` |
| **NRZI** | 1 → sath almashadi, 0 → saqlanadi (boshlangʻich `-`) | `+ + - + + + + -` |
| **Manchester** (IEEE 802.3) | 0 → `+-`, 1 → `-+` (bit oʻrtasida doim oʻtish) | `-+ +- -+ -+ +- +- +- -+` |
| **AMI** | 0 → `0`, 1 → navbat bilan `+` va `-` | `+ 0 - + 0 0 0 -` |
| **MLT-3** | 0 → saqlanadi, 1 → keyingi sath `0 → + → 0 → -` siklida (boshlangʻich `0`) | `+ + 0 - - - - 0` |

| Kod | Sinxronizatsiya | DC balans | Qayerda ishlatiladi |
|---|---|---|---|
| NRZ-L | uzun 0 va 1 larda yoʻqoladi | yoʻq | RS-232, ichki shinalar |
| NRZI | uzun 0 larda yoʻqoladi (1 lar bilan muammo yoʻq) | yoʻq | USB, 100BASE-FX (4B/5B bilan) |
| Manchester | har bir bitda oʻtish — **ideal** | bor | 10BASE-T Ethernet |
| Differensial Manchester | har bir bitda oʻtish; 0 — bit boshida qoʻshimcha oʻtish | bor | Token Ring |
| AMI | uzun 0 larda yoʻqoladi | bor | T1/E1 liniyalari |
| MLT-3 | uzun 0 larda yoʻqoladi | qisman | 100BASE-TX (4B/5B bilan) |

Manchester kodining narxi — signal oʻzgarishlari chastotasi NRZ ga qaraganda **ikki baravar** yuqori. Shuning uchun 10 Mbit/s Ethernet uni ishlatgan, tezroq standartlar esa boshqa yoʻl tanlagan: NRZI yoki MLT-3 + **mantiqiy kodlash**.

NRZ, NRZI va Manchester kodlarini dasturda amalga oshirish — ushbu mashgʻulotning **mustaqil topshirigʻi** (quyidagi KOT-A4).

## Mantiqiy kodlash: 4B/5B

NRZI 1 larda oʻtish beradi, lekin uzun nollarda jim qoladi. **4B/5B** har 4 bitni 5 bitli kod soʻzi bilan almashtiradi; kod soʻzlari shunday tanlanganki, oqimda **uchtadan ortiq nol ketma-ket kelmaydi** (har bir soʻzning boshida koʻpi bilan bitta, oxirida koʻpi bilan ikkita nol bor). Narxi — 25% ortiqcha bitlar: 100 Mbit/s maʼlumot uchun 125 Mbod liniya kerak.

| 4 bit | 5 bit | 4 bit | 5 bit |
|---|---|---|---|
| 0000 | 11110 | 1000 | 10010 |
| 0001 | 01001 | 1001 | 10011 |
| 0010 | 10100 | 1010 | 10110 |
| 0011 | 10101 | 1011 | 10111 |
| 0100 | 01010 | 1100 | 11010 |
| 0101 | 01011 | 1101 | 11011 |
| 0110 | 01110 | 1110 | 11100 |
| 0111 | 01111 | 1111 | 11101 |

Qolgan 16 ta 5 bitli kombinatsiyaning bir qismi boshqaruv belgilari (masalan, `11111` — IDLE, kadr boshi va oxiri), qolgani taqiqlangan — ularning paydo boʻlishi xatoni bildiradi. Gigabit Ethernet da xuddi shu gʻoya **8B/10B** koʻrinishida (25% ortiqcha), 10G da esa **64B/66B** (atigi 3%) ishlatiladi. Boshqa yondashuv — **skremblerlash**: bitlar psevdotasodifiy ketma-ketlik bilan XOR qilinadi va uzun bir xil ketma-ketliklar ehtimoli keskin kamayadi.

## Bit qoʻshish (bit stuffing)

HDLC va PPP kabi protokollarda kadr boshi va oxiri **bayroq** `01111110` bilan belgilanadi. Agar maʼlumot ichida ham ketma-ket oltita 1 uchrasa, qabul qiluvchi uni bayroq deb oʻylaydi. Yechim: yuboruvchi **ketma-ket beshta 1 dan keyin** doim bitta `0` qoʻshadi; qabul qiluvchi beshta 1 dan keyingi `0` ni olib tashlaydi. Natijada maʼlumotda oltita 1 hech qachon uchramaydi.

## Ishlangan misol 1: 4B/5B kodlash

Bir satrda oʻn oltilik raqamlar berilgan (har bir raqam — 4 bit). Birinchi qatorda 4 bitli guruhlarni, ikkinchisida 4B/5B kod soʻzlarini chiqaring, uchinchisida esa kodlashgacha va kodlashdan keyingi eng uzun nollar ketma-ketligini solishtiring.

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
besh = [JADVAL[int(r, 16)] for r in satr]          # har 4 bit -> 5 bitli soʻz
print(" ".join(tort))
print(" ".join(besh))
print("eng uzun nollar:", eng_uzun_nollar("".join(tort)), "->", eng_uzun_nollar("".join(besh)))
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
            besh.add(JADVAL[qiymat]);                        // har 4 bit -> 5 bitli soʻz
        }
        System.out.println(String.join(" ", tort));
        System.out.println(String.join(" ", besh));
        System.out.println("eng uzun nollar: " + engUzunNollar(String.join("", tort)) + " -> " + engUzunNollar(String.join("", besh)));
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
        besh += JADVAL[qiymat];                              // har 4 bit -> 5 bitli soʻz
        tortQator += (tortQator.empty() ? "" : " ") + t;
        beshQator += (beshQator.empty() ? "" : " ") + JADVAL[qiymat];
    }
    cout << tortQator << endl;
    cout << beshQator << endl;
    cout << "eng uzun nollar: " << engUzunNollar(tort) << " -> " << engUzunNollar(besh) << endl;
    return 0;
}
```

Kirish:

```text
00
```

Chiqish:

```text
0000 0000
11110 11110
eng uzun nollar: 8 -> 1
```

Sakkizta nol NRZI da sakkiz davr davomida "jimlik" boʻlardi; 4B/5B dan keyin esa oqimda koʻpi bilan bitta nol ketma-ket keladi. `0F1` uchun natija `0000 1111 0001` → `11110 11101 01001`, nollar 4 → 2.

## Ishlangan misol 2: bit qoʻshish

Har bir satrda bitlar ketma-ketligi. Har biri uchun bit qoʻshilgan ketma-ketlikni va qoʻshilgan nollar sonini chiqaring. Kirish fayl oxirigacha oʻqiladi.

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
            if birlar == 5:                 # beshta 1 dan keyin 0 qoʻshiladi
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
                    if (birlar == 5) {                  // beshta 1 dan keyin 0 qoʻshiladi
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
                if (birlar == 5) {                      // beshta 1 dan keyin 0 qoʻshiladi
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

Kirish:

```text
01111110
1111111111
0101
```

Chiqish:

```text
01111110 -> 011111010 (+1)
1111111111 -> 111110111110 (+2)
0101 -> 0101 (+0)
```

Birinchi satr — bayroqning oʻzi maʼlumot ichida kelgan holat: bit qoʻshilgach u endi `01111110` emas, qabul qiluvchi uni kadr chegarasi deb adashmaydi. Qabul qiluvchi teskari amalni bajaradi: beshta 1 dan keyin kelgan `0` ni tashlab yuboradi.

:::warning
**Koʻp uchraydigan xatolar**

- **NRZI da sathni bitning qiymati deb oʻqish.** NRZI da maʼlumot — sathning **oʻzgarishi**: oʻtish bor — 1, yoʻq — 0.
- **Manchester kelishuvlarini aralashtirish.** IEEE 802.3 da 0 — yuqoridan pastga, 1 — pastdan yuqoriga; G. E. Thomas kelishuvida aksincha. Topshiriqda qaysi biri ishlatilishini tekshiring.
- **4B/5B ni shifrlash deb oʻylash.** Bu ochiq jadval boʻyicha almashtirish — maqsadi sinxronizatsiya, maxfiylik emas.
- **Bit qoʻshishda birlar hisoblagichini nolga tushirmaslik.** Qoʻshilgan 0 dan keyin hisob yangidan boshlanadi; aks holda keyingi 1 da yana 0 qoʻshilib ketadi.
:::

:::tip
Mustaqil topshiriqdan oldin `10110001` ketma-ketligi uchun NRZ-L, NRZI va Manchester diagrammalarini daftarga chizing va yuqoridagi jadval bilan solishtiring. Diagrammani toʻgʻri chizgan boʻlsangiz, dastur yozish ancha oson boʻladi.
:::

## Xulosa

- Yaxshi chiziqli kod oʻz-oʻzini sinxronlaydi, doimiy tashkil etuvchiga ega emas va tor polosada ishlaydi.
- NRZ oddiy, lekin uzun ketma-ketliklarda sinxronizatsiya yoʻqoladi; Manchester har bir bitda oʻtish beradi, lekin ikki baravar keng polosa talab qiladi.
- 4B/5B (va 8B/10B, 64B/66B) uzun nollarni yoʻqotadi va NRZI yoki MLT-3 bilan birga ishlaydi.
- Bit qoʻshish beshta 1 dan keyin 0 qoʻyib, `01111110` bayrogʻining maʼlumot ichida uchramasligini taʼminlaydi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun NRZ-L uzun nollar ketma-ketligida xatoga olib kelishi mumkin?
A: Signal oʻzgarmaydi va qabul qiluvchi soatini tuzatish uchun oʻtishlar yoʻq — bitlar sonini notoʻgʻri sanashi mumkin.
Q: Manchester kodining asosiy afzalligi va kamchiligi nima?
A: Har bir bit oʻrtasida oʻtish boʻlgani uchun sinxronizatsiya ideal; lekin signal chastotasi ikki baravar yuqori — kengroq polosa kerak.
Q: 4B/5B da `1010` qaysi kod soʻzi bilan almashtiriladi?
A: `10110`.
Q: 100 Mbit/s maʼlumotni 4B/5B bilan uzatish uchun liniya tezligi qancha boʻlishi kerak?
A: 125 Mbod (har 4 bitga 5 bit).
Q: `0111110` ketma-ketligiga bit qoʻshilsa, natija qanday boʻladi?
A: `01111100` — beshta 1 dan keyin 0 qoʻshiladi, soʻng asl oxirgi 0 keladi.
:::

:::link https://en.wikipedia.org/wiki/4B5B
4B5B kodlash
4B/5B jadvali, boshqaruv belgilari va uning NRZI hamda MLT-3 bilan ishlatilishi (ingliz tilida).
:::
