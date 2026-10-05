---
title: Klassik shifrlash usullari (Caesar, Vigenère) ni qoʻllash
summary: Klassik shifrlarni amalda qoʻllash — Sezar shifri va uning kalit fazosi, Atbash va affin shifrlari, oddiy almashtirish shifri, Vijiner shifri va Vijiner jadvali; qoʻlda shifrlash mashqlari; dasturiy topshiriqlar; kriptotahlil — chastotali tahlil, Kasiski usuli, moslik indeksi (IC) yordamida kalit uzunligini aniqlash.
---

## Nima uchun bu mavzu muhim?

M5 maʼruzasida tarixiy shifrlar bilan tanishdik. Bugun ular hech narsani himoya qilmaydi, lekin ular orqali kriptografiyaning eng muhim saboqlarini oʻz qoʻlingiz bilan sinab koʻrasiz: kalit fazosi kichik boʻlsa — **toʻliq tanlash** yetarli; kalit fazosi katta boʻlsa ham tilning **statistikasi** shifrmatnda saqlansa — shifr baribir buziladi. Zamonaviy shifrlar aynan shu ikki zaiflikka qarshi loyihalangan.

:::note
**Bu mashgʻulotdan soʻng siz:**

- Sezar va Vijiner shifrlarida qoʻlda shifrlay va deshifrlay olasiz;
- ularni dasturda amalga oshirasiz (platformadagi A5 topshiriqlari);
- chastotali tahlil bilan Sezar shifrining kalitini topa olasiz;
- moslik indeksi yordamida Vijiner kaliti uzunligini aniqlay olasiz.
:::

## Qisqacha nazariya

Harflarni sonlar bilan almashtiramiz: A = 0, B = 1, …, Z = 25; barcha amallar **mod 26**.

| Shifr | Shifrlash | Kalit fazosi | Zaifligi |
|---|---|---|---|
| **Sezar** | *C* = (*P* + *k*) mod 26 | 25 ta kalit | toʻliq tanlash, chastotali tahlil |
| **Atbash** | *C* = 25 − *P* (A↔Z, B↔Y) | kalit yoʻq | algoritm maʼlum boʻlsa — darhol ochiladi |
| **Affin** | *C* = (*a*·*P* + *b*) mod 26, EKUB(*a*, 26) = 1 | 12 · 26 = 312 | toʻliq tanlash, ikki harf juftidan tenglama |
| **Oddiy almashtirish** | har bir harf jadval boʻyicha boshqasiga | 26! ≈ 4 · 10²⁶ | chastotali tahlil (kalit fazosi katta boʻlsa ham!) |
| **Vijiner** | *C*ᵢ = (*P*ᵢ + *K*ᵢ mod *m*) mod 26 | 26^*m* | kalit takrorlanadi: Kasiski usuli, moslik indeksi |

**Vijiner shifri** — bir nechta Sezar shifrining navbatma-navbat qoʻllanishi: kalitning har bir harfi oʻz siljishini beradi (A = 0, B = 1, …). Kalit oxiriga yetganda boshidan takrorlanadi. Qoʻlda ishlash uchun **Vijiner jadvali** (tabula recta) qoʻllanadi: satr — kalit harfi, ustun — ochiq matn harfi, kesishmada — shifrmatn harfi. Uch asr davomida "buzib boʻlmaydigan shifr" (le chiffre indéchiffrable) deb hisoblangan, 1863-yilda F. Kasiski uni ochish usulini eʼlon qilgan.

## Amaliy ish

**1-qadam. Qoʻlda shifrlash.** Daftarda bajaring va natijani tekshiring.

- Sezar, *k* = 3: HUJUM → H+3 = K, U+3 = X, J+3 = M, U+3 = X, M+3 = P → **KXMXP**.
- Vijiner, kalit KEY: SALOM. Kalitni matn ostiga takrorlab yozamiz: KEYKE. S(18)+K(10) = 28 mod 26 = 2 → C; A(0)+E(4) = 4 → E; L(11)+Y(24) = 35 mod 26 = 9 → J; O(14)+K(10) = 24 → Y; M(12)+E(4) = 16 → Q → **CEJYQ**.
- Mustaqil: Sezar *k* = 5 bilan ismingizni shifrlang; Vijiner, kalit LIMON bilan "TOSHKENT" ni shifrlang va qoʻshningizga deshifrlash uchun bering.

**2-qadam. Dastur.** Platformadagi ikkita topshiriqni bajaring: **Sezar shifri** (manfiy *k* — deshifrlash) va **Vijiner shifri** (E — shifrlash, D — deshifrlash; boʻshliq va tinish belgilari oʻzgarmaydi, kalit indeksi faqat harflarda siljiydi). Katta-kichik harfni saqlash va mod 26 da manfiy qoldiqqa eʼtibor bering.

**3-qadam. Kriptotahlil.** Kalitni bilmasdan shifrmatnni ochish — quyidagi ikki ishlangan misol aynan shunga bagʻishlangan.

## Ishlangan misol 1: Sezar shifriga chastotali tahlil

Har qanday tilda harflar notekis uchraydi: oʻzbek lotin matnida eng koʻp uchraydigan harf — **A** (taxminan har 6–7 harfdan biri), keyin I, O, L, R. Sezar shifri bu taqsimotni oʻzgartirmaydi, faqat suradi. Shifrmatndagi harflar sonini sanab, eng koʻp uchraydiganlarini va "agar bu harf aslida A boʻlsa" degan farazdagi kalit *k* ni chiqaring.

```python
import sys

matn = sys.stdin.read().upper()
sanoq = [0] * 26
for ch in matn:
    if "A" <= ch <= "Z":
        sanoq[ord(ch) - 65] += 1
jami = sum(sanoq)
print(f"harflar: {jami}")
tartib = sorted(range(26), key=lambda i: (-sanoq[i], i))   # koʻpdan kamga, teng boʻlsa alifbo boʻyicha
for i in tartib[:4]:
    print(f"{chr(65 + i)} {sanoq[i]} {100 * sanoq[i] / jami:.1f}% k={i}")   # agar bu harf A boʻlsa, k = i − 0
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] sanoq = new int[26];
        while (in.hasNextLine()) {
            for (char ch : in.nextLine().toUpperCase().toCharArray()) {
                if (ch >= 'A' && ch <= 'Z') sanoq[ch - 'A']++;
            }
        }
        int jami = 0;
        for (int s : sanoq) jami += s;
        System.out.println("harflar: " + jami);
        List<Integer> tartib = new ArrayList<>();
        for (int i = 0; i < 26; i++) tartib.add(i);
        tartib.sort((a, b) -> sanoq[a] != sanoq[b] ? sanoq[b] - sanoq[a] : a - b);   // koʻpdan kamga, teng boʻlsa alifbo boʻyicha
        for (int t = 0; t < 4; t++) {
            int i = tartib.get(t);
            System.out.println(String.format("%c %d %.1f%% k=%d", (char) ('A' + i), sanoq[i], 100.0 * sanoq[i] / jami, i));   // agar bu harf A boʻlsa, k = i − 0
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<int> sanoq(26, 0);
    string satr;
    while (getline(cin, satr)) {
        for (unsigned char ch : satr) {
            char c = toupper(ch);
            if (c >= 'A' && c <= 'Z') sanoq[c - 'A']++;
        }
    }
    int jami = 0;
    for (int s : sanoq) jami += s;
    printf("harflar: %d\n", jami);
    vector<int> tartib(26);
    for (int i = 0; i < 26; i++) tartib[i] = i;
    stable_sort(tartib.begin(), tartib.end(), [&](int a, int b) { return sanoq[a] > sanoq[b]; });   // koʻpdan kamga, teng boʻlsa alifbo boʻyicha
    for (int t = 0; t < 4; t++) {
        int i = tartib[t];
        printf("%c %d %.1f%% k=%d\n", 'A' + i, sanoq[i], 100.0 * sanoq[i] / jami, i);   // agar bu harf A boʻlsa, k = i − 0
    }
    return 0;
}
```

Kirish:

```text
LYAHNH THQSPZ ZVHA VUKH IVZOSHUHKP IHYJOH EVKPTSHY RHAAH GHSNH RLSZPU OPZVIVASHYUP AHFFVYSHI XVFPUN
```

Chiqish:

```text
harflar: 87
H 17 19.5% k=7
V 8 9.2% k=21
P 7 8.0% k=15
S 7 8.0% k=18
```

**H** boshqa harflardan ikki baravar koʻp uchraydi — bu deyarli aniq A. Birinchi faraz *k* = 7: Sezar dasturingizni *k* = −7 bilan ishga tushiring va oʻqiladigan matn chiqishini tekshiring ("ERTAGA MAJLIS…"). Agar matn oʻqilmasa — keyingi farazni sinang. Qisqa matnda statistika aldashi mumkin, lekin 25 ta kalitni toʻliq tanlash ham bir soniyalik ish.

## Ishlangan misol 2: Vijiner kaliti uzunligini topish

Vijiner shifri harflar chastotasini "tekislaydi" — oddiy chastotali tahlil ishlamaydi. Lekin kalit uzunligi *m* boʻlsa, har *m*-chi harf **bitta** Sezar shifri bilan shifrlangan. **Moslik indeksi** (index of coincidence) — tasodifan tanlangan ikki harfning bir xil boʻlish ehtimoli: IC = Σ *n*ᵢ(*n*ᵢ − 1) / (*N*(*N* − 1)). Tabiiy matnda IC ≈ 0,07, tasodifiy harflarda ≈ 0,038. Birinchi satrda eng katta sinab koʻriladigan uzunlik *M*, ikkinchisida shifrmatn. Har bir *m* = 1…*M* uchun matnni *m* ta ustunga (har *m*-chi harf) ajrating, ustunlar IC sining oʻrtachasini chiqaring va eng katta IC li *m* ni toping.

```python
import sys

satrlar = sys.stdin.read().split("\n")
M = int(satrlar[0])
harflar = [ch for ch in " ".join(satrlar[1:]).upper() if "A" <= ch <= "Z"]
eng_yaxshi, eng_ic = 1, -1.0
for m in range(1, M + 1):
    jami = 0.0
    for r in range(m):
        ustun = harflar[r::m]                     # r-harfdan boshlab har m-chi harf
        n = len(ustun)
        sanoq = [0] * 26
        for ch in ustun:
            sanoq[ord(ch) - 65] += 1
        jami += sum(c * (c - 1) for c in sanoq) / (n * (n - 1))
    ic = jami / m
    if ic > eng_ic:
        eng_yaxshi, eng_ic = m, ic
    print(f"m={m} IC={ic:.4f}")
print(f"taxminiy kalit uzunligi: {eng_yaxshi}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int M = Integer.parseInt(in.nextLine().trim());
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            for (char ch : in.nextLine().toUpperCase().toCharArray()) {
                if (ch >= 'A' && ch <= 'Z') sb.append(ch);
            }
        }
        String harflar = sb.toString();
        int engYaxshi = 1;
        double engIc = -1.0;
        for (int m = 1; m <= M; m++) {
            double jami = 0.0;
            for (int r = 0; r < m; r++) {
                int[] sanoq = new int[26];
                int n = 0;
                for (int i = r; i < harflar.length(); i += m) {   // r-harfdan boshlab har m-chi harf
                    sanoq[harflar.charAt(i) - 'A']++;
                    n++;
                }
                long juftlar = 0;
                for (int c : sanoq) juftlar += (long) c * (c - 1);
                jami += (double) juftlar / ((long) n * (n - 1));
            }
            double ic = jami / m;
            if (ic > engIc) {
                engYaxshi = m;
                engIc = ic;
            }
            System.out.println(String.format("m=%d IC=%.4f", m, ic));
        }
        System.out.println("taxminiy kalit uzunligi: " + engYaxshi);
    }
}
```
```cpp
#include <cctype>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int M = stoi(satr);
    string harflar;
    while (getline(cin, satr)) {
        for (unsigned char ch : satr) {
            char c = toupper(ch);
            if (c >= 'A' && c <= 'Z') harflar += c;
        }
    }
    int engYaxshi = 1;
    double engIc = -1.0;
    for (int m = 1; m <= M; m++) {
        double jami = 0.0;
        for (int r = 0; r < m; r++) {
            long long sanoq[26] = {0};
            long long n = 0;
            for (size_t i = r; i < harflar.size(); i += m) {   // r-harfdan boshlab har m-chi harf
                sanoq[harflar[i] - 'A']++;
                n++;
            }
            long long juftlar = 0;
            for (long long c : sanoq) juftlar += c * (c - 1);
            jami += (double) juftlar / (n * (n - 1));
        }
        double ic = jami / m;
        if (ic > engIc) {
            engYaxshi = m;
            engIc = ic;
        }
        printf("m=%d IC=%.4f\n", m, ic);
    }
    printf("taxminiy kalit uzunligi: %d\n", engYaxshi);
    return 0;
}
```

Kirish:

```text
8
KXMWKYT IIOPSTHESGT PTB BTZ MKSSSBVOE CVRUY UNRIX DTJIQI ASSZJEKNLLB MHFVDS MLFYSY XIEEMZBEKR EIKAAWQU UEEAT UAEBT JACIK UEWIWS SSCGSNR CVRUY FHNIXTTB PLZHVLLZGS HPKA UIXOT KYEUTCLTOB FA DPNLHLTB HAETTBDLOB RAGWEKLLZGS ONPFKSWQZS KPZTU TLZFYQ LLFSNTAMBAEWKS EDI RKNRQEKNTAAVACVB FABBBNA ZZGKTLLB FA KIQSRL VNCXLTTBNT UNXTLHTW TPSLRICQU LOCIWS
```

Chiqish:

```text
m=1 IC=0.0456
m=2 IC=0.0458
m=3 IC=0.0435
m=4 IC=0.0473
m=5 IC=0.0731
m=6 IC=0.0416
m=7 IC=0.0454
m=8 IC=0.0475
taxminiy kalit uzunligi: 5
```

*m* = 5 da IC keskin oshib, tabiiy matn qiymatiga (≈ 0,07) yetdi — boshqa uzunliklarda u tasodifiy harflarga yaqin. Demak, kalit 5 harfli. Keyingi qadam: matnni 5 ta ustunga ajratib, har biriga 1-misoldagi chastotali tahlilni qoʻllash — har bir ustunning eng koʻp harfi kalitning bitta harfini beradi. Shu yoʻl bilan kalitni (bu yerda KALIT) topib, Vijiner dasturingiz bilan deshifrlang. (*m* ning karralilari — 10, 15 — ham yuqori IC beradi; shuning uchun eng kichik "baland" qiymat tanlanadi.)

:::warning
**Koʻp uchraydigan xatolar**

- **Manfiy qoldiq.** Python da (−3) % 26 = 23, lekin Java va C++ da −3; deshifrlashda (*x* % 26 + 26) % 26 dan foydalaning.
- **Kalit indeksini boʻshliqda ham siljitish.** Vijiner topshirigʻida kalit faqat harflarda oldinga yuradi.
- **Katta-kichik harfni yoʻqotish.** Harf turini saqlang: 'a' asosidagi va 'A' asosidagi siljish alohida.
- **Qisqa matnga chastotali tahlil.** 20–30 harfda statistika ishonchsiz; bir nechta farazni sinang.
- **Klassik shifrlarni haqiqiy maʼlumot uchun ishlatish.** Ular faqat oʻquv maqsadida; amalda AES (A6) ishlatiladi.
:::

:::tip
Natijalaringizni **CyberChef** onlayn vositasida tekshiring: "ROT13" retsepti (Amount = *k*) Sezar shifrini, "Vigenère Encode/Decode" esa Vijiner shifrini bajaradi. Oʻz shifrmatningizni yaratib, qoʻshningizga kalitsiz ochishni topshiring.
:::

## Xulosa

- Sezar: *C* = (*P* + *k*) mod 26, 25 ta kalit — toʻliq tanlash va chastotali tahlil bilan oson ochiladi.
- Oddiy almashtirish shifrining kalit fazosi katta (26!), lekin harflar statistikasi saqlangani uchun chastotali tahlil bilan ochiladi.
- Vijiner — kalit harflari boʻyicha navbatma-navbat Sezar; kalit takrorlanishi tufayli Kasiski usuli va moslik indeksi bilan buziladi.
- Moslik indeksi: tabiiy matnda ≈ 0,07, tasodifiy harflarda ≈ 0,038 — toʻgʻri kalit uzunligida ustunlar IC si keskin oshadi.
- Saboq: xavfsiz shifr kalit fazosi katta va shifrmatnda til statistikasini qoldirmaydigan boʻlishi kerak.

## Oʻzingizni tekshiring

:::check
Q: Sezar shifrida kalit k = 3 boʻlsa, "ABC" qanday shifrlanadi?
A: "DEF".
Q: Nima uchun oddiy almashtirish shifrining 26! ta kaliti uni xavfsiz qilmaydi?
A: Har bir harf doim bitta harfga almashadi, shuning uchun til chastotalari saqlanadi va chastotali tahlil kalitni tanlamasdan topadi.
Q: Vijiner shifrida kalit takrorlanishi qanday zaiflik beradi?
A: Har m-chi harf bitta Sezar shifri bilan shifrlanadi — kalit uzunligi topilsa, shifr m ta Sezar shifriga ajraladi.
Q: Moslik indeksi kalit uzunligini qanday koʻrsatadi?
A: Toʻgʻri uzunlikda har bir ustun bitta Sezar shifri boʻladi va IC tabiiy til qiymatiga (≈ 0,07) koʻtariladi; notoʻgʻri uzunlikda u tasodifiy qiymatga yaqin qoladi.
:::

:::link https://gchq.github.io/CyberChef/
CyberChef
Klassik va zamonaviy shifrlar, kodlashlar va xeshlar bilan ishlash uchun bepul onlayn vosita (ingliz tilida).
:::
