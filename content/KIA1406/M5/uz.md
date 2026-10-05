---
title: Kriptografiyaga kirish. Asosiy tushunchalar, kriptotizimlar, Kerkxofs prinsipi, tarixiy usullar
summary: Kriptologiya, kriptografiya va kriptoanaliz; ochiq matn, shifrmatn, kalit, shifrlash va deshifrlash; kriptotizim tarkibi; simmetrik va asimmetrik tizimlar; Kerkxofs prinsipi; hujum modellari; kalit fazosi va toʻliq tanlash; tarixiy shifrlar — Atbash, Skital, Sezar, Polibiy kvadrati, Vijiner, Enigma; almashtirish va oʻrin almashtirish; chastota tahlili; kriptografiya xizmatlari.
---

## Nima uchun bu mavzu muhim?

Har safar bank ilovasiga kirganingizda, messenjerda xabar yozganingizda yoki saytda "qulf" belgisini koʻrganingizda, kriptografiya ishlaydi. U maʼlumotni begonalar oʻqiy olmaydigan, sezilmasdan oʻzgartira olmaydigan va soxtalashtira olmaydigan qiladi. Zamonaviy algoritmlarni tushunish uchun ularning asosidagi gʻoyalarni — kalit, almashtirish, oʻrin almashtirish va nima uchun "maxfiy algoritm" ishonchli emasligini — bilish kerak. Tarixiy shifrlar bu gʻoyalarni oddiy misolda koʻrsatadi va ularning qanday buzilgani zamonaviy talablarni tushuntiradi.

:::note
**Bu darsdan soʻng siz:**

- kriptografiyaning asosiy atamalarini va kriptotizim tarkibini tushuntira olasiz;
- simmetrik va asimmetrik tizimlarni farqlay olasiz;
- Kerkxofs prinsipini va uning ahamiyatini asoslay olasiz;
- tarixiy shifrlarni almashtirish va oʻrin almashtirish turlariga ajrata olasiz;
- chastota tahlili va kalit fazosi hajmi shifr bardoshliligiga qanday taʼsir qilishini hisoblay olasiz.
:::

## Asosiy tushunchalar

| Atama | Maʼnosi |
|---|---|
| **Kriptologiya** | shifrlash haqidagi fan: kriptografiya + kriptoanaliz |
| **Kriptografiya** | maʼlumotni himoyalash uchun shifrlash usullarini yaratish |
| **Kriptoanaliz** | kalitni bilmasdan shifrni ochish usullari |
| **Ochiq matn** (plaintext) | dastlabki oʻqiladigan maʼlumot *M* |
| **Shifrmatn** (ciphertext) | shifrlangan maʼlumot *C* |
| **Kalit** (key) | shifrlash va deshifrlashni boshqaruvchi maxfiy parametr *K* |
| **Shifrlash / deshifrlash** | *C* = *E*(*K*, *M*), *M* = *D*(*K*, *C*) |

**Kriptotizim** — ochiq matnlar, shifrmatnlar, kalitlar toʻplamlari va shifrlash hamda deshifrlash algoritmlaridan iborat tizim.

| Tur | Kalit | Afzalligi | Muammosi | Misollar |
|---|---|---|---|---|
| **Simmetrik** | shifrlash va deshifrlashda bitta umumiy kalit | juda tez | kalitni xavfsiz yetkazish | AES, ChaCha20, (eski) DES |
| **Asimmetrik** (ochiq kalitli) | ochiq kalit bilan shifrlanadi, yopiq kalit bilan ochiladi | kalit almashish muammosini hal qiladi, raqamli imzo | sekin | RSA, ECC, Diffie–Hellman |

Amalda ikkalasi birga ishlatiladi (**gibrid** tizim): asimmetrik usul bilan simmetrik kalit kelishiladi, maʼlumot esa simmetrik shifrlanadi (TLS shunday ishlaydi).

## Kriptografiya xizmatlari

- **Maxfiylik** — shifrlash.
- **Yaxlitlik** — xesh, MAC.
- **Autentifikatsiya** — tomonlar haqiqiyligini tasdiqlash.
- **Rad etib boʻlmaslik** — raqamli imzo.

## Kerkxofs prinsipi

1883-yilda Ogyust Kerkxofs: **tizimning xavfsizligi algoritmning maxfiyligiga emas, faqat kalitning maxfiyligiga bogʻliq boʻlishi kerak.** Dushman algoritmni bilsa ham, kalitsiz shifrni ocha olmasligi lozim. Sabablari: algoritmni uzoq sir saqlab boʻlmaydi (dastur teskari tahlil qilinadi, xodimlar ketadi); ochiq algoritmni minglab mutaxassislar tekshiradi va zaifliklar oldinroq topiladi; kalit oshkor boʻlsa, uni almashtirish oson, algoritmni esa — yoʻq. Claude Shannon buni "dushman tizimni biladi" deb ifodalagan. Shuning uchun AES, RSA kabi barcha zamonaviy algoritmlar ochiq eʼlon qilingan.

## Hujum modellari

| Model | Hujumchida nima bor |
|---|---|
| Faqat shifrmatn | bir nechta shifrmatn |
| Maʼlum ochiq matn | ochiq matn – shifrmatn juftlari |
| Tanlangan ochiq matn | istalgan matnni shifrlata oladi |
| Tanlangan shifrmatn | istalgan shifrmatnni ochtira oladi |

Yaxshi shifr eng kuchli modelda ham bardoshli boʻlishi kerak. Har qanday shifrga qarshi universal hujum — **toʻliq tanlash** (brute force): barcha kalitlarni sinash. Kalit *b* bit boʻlsa, kalitlar soni 2^*b*, oʻrtacha 2^(*b*−1) ta urinish kerak. Shuning uchun kalit uzunligi bardoshlilikning asosiy oʻlchovi.

## Tarixiy shifrlar

**Almashtirish** (substitution) — har bir belgi boshqa belgi bilan almashtiriladi; **oʻrin almashtirish** (transposition) — belgilar oʻzgarmaydi, faqat tartibi oʻzgaradi.

| Shifr | Davr | Turi | Gʻoyasi | Zaifligi |
|---|---|---|---|---|
| **Skital** | Sparta, mil. av. V asr | oʻrin almashtirish | lenta tayoqchaga oʻraladi va boʻylab yoziladi; kalit — tayoqcha diametri | qatorlar soni kam — tez tanlanadi |
| **Atbash** | qadimgi ibroniy | almashtirish | A↔Z, B↔Y, … (alifbo teskari) | kalit yoʻq |
| **Sezar** | Rim, mil. av. I asr | almashtirish | har bir harf *k* oʻringa suriladi | atigi 25 ta kalit |
| **Polibiy kvadrati** | Yunoniston | almashtirish | harf 5×5 jadvaldagi qator va ustun raqamlari bilan | chastota tahlili |
| **Vijiner** | XVI asr | koʻp alifboli almashtirish | har bir harf kalit soʻzning navbatdagi harfiga qarab suriladi | kalit uzunligi topilsa, Sezarlarga boʻlinadi (Kasiski, 1863) |
| **Enigma** | XX asr, Germaniya | elektromexanik koʻp alifboli | rotorlar har bir bosishda aylanadi | Polsha va Britaniya kriptoanalitiklari (Rejewski, Turing) buzdi |

Sezar va Vijiner shifrlarini dasturda amalga oshirish — A5 amaliy mashgʻulotining topshirigʻi.

**Chastota tahlili** (IX asr, al-Kindiy): har qanday tilda harflar turlicha tez-tez uchraydi (ingliz tilida E, T, A; oʻzbek tilida A, I). Oddiy almashtirish shifrida harfning "kiyimi" oʻzgaradi, lekin chastotasi saqlanadi — shifrmatndagi eng koʻp uchraydigan belgi katta ehtimol bilan E yoki A. Shuning uchun bir alifboli almashtirish qancha kaliti boʻlmasin (26! ≈ 4·10²⁶) qisqa vaqtda buziladi: kalit fazosi katta boʻlishi **yetarli emas**, shifr statistikani ham yashirishi kerak.

## Ishlangan misol 1: harflar chastotasi

Bir satrli matnda lotin harflarini (katta-kichik farqsiz) sanab, eng koʻp uchraydigan beshta harfni miqdori va foizi bilan chiqaring (teng boʻlsa — alifbo tartibida).

```python
matn = input()
soni = {}
for c in matn.upper():
    if "A" <= c <= "Z":
        soni[c] = soni.get(c, 0) + 1
jami = sum(soni.values())
print("harflar: " + str(jami))
for harf, k in sorted(soni.items(), key=lambda x: (-x[1], x[0]))[:5]:   # koʻp → kam
    print(f"{harf} {k} {k * 100 / jami:.1f}%")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String matn = new Scanner(System.in).nextLine().toUpperCase();
        int[] soni = new int[26];
        int jami = 0;
        for (char c : matn.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                soni[c - 'A']++;
                jami++;
            }
        }
        System.out.println("harflar: " + jami);
        List<Integer> harflar = new ArrayList<>();
        for (int i = 0; i < 26; i++) if (soni[i] > 0) harflar.add(i);
        harflar.sort((a, b) -> soni[b] != soni[a] ? soni[b] - soni[a] : a - b);   // koʻp → kam
        for (int i = 0; i < Math.min(5, harflar.size()); i++) {
            int h = harflar.get(i);
            System.out.println(String.format("%c %d %.1f%%", (char) ('A' + h), soni[h], soni[h] * 100.0 / jami));
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
    string matn;
    getline(cin, matn);
    int soni[26] = {};
    int jami = 0;
    for (char c : matn) {
        c = toupper(c);
        if (c >= 'A' && c <= 'Z') {
            soni[c - 'A']++;
            jami++;
        }
    }
    printf("harflar: %d\n", jami);
    vector<int> harflar;
    for (int i = 0; i < 26; i++) if (soni[i] > 0) harflar.push_back(i);
    sort(harflar.begin(), harflar.end(), [&](int a, int b) {
        return soni[a] != soni[b] ? soni[a] > soni[b] : a < b;               // koʻp → kam
    });
    for (size_t i = 0; i < min<size_t>(5, harflar.size()); i++) {
        int h = harflar[i];
        printf("%c %d %.1f%%\n", 'A' + h, soni[h], soni[h] * 100.0 / jami);
    }
    return 0;
}
```

Kirish:

```text
MEET ME AT THE OLD BRIDGE AFTER THE EVENING PRAYER AND BRING THE LETTER
```

Chiqish:

```text
harflar: 58
E 13 22.4%
T 8 13.8%
R 6 10.3%
A 4 6.9%
N 4 6.9%
```

Agar bu matn har qanday bir alifboli almashtirish bilan shifrlansa ham, shifrmatnda qaysidir harf aynan 13 marta (22,4%) uchraydi — kriptoanalitik uni darhol E deb taxmin qiladi. Vijiner kabi koʻp alifboli shifrlar aynan shu chastotalarni "tekislash" uchun yaratilgan.

## Ishlangan misol 2: toʻliq tanlash qancha vaqt oladi

Har bir satrda kalit uzunligi *b* (bit) va hujumchi sekundiga sinay oladigan kalitlar soni. Kalitlar sonini va oʻrtacha tanlash vaqtini (yillarda, 1 yil = 31 557 600 s) ilmiy koʻrinishda chiqaring.

```python
import sys

YIL = 31557600
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    b, tezlik = map(int, satr.split())
    kalitlar = 2.0 ** b
    vaqt = 2.0 ** (b - 1) / tezlik / YIL            # oʻrtacha — yarim kalit fazosi
    print(f"{b} bit: {kalitlar:.2e} kalit, oʻrtacha {vaqt:.2e} yil")
```
```java
import java.util.Scanner;

public class Main {
    static final double YIL = 31557600;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int b = in.nextInt();
            long tezlik = in.nextLong();
            double kalitlar = Math.pow(2, b);
            double vaqt = Math.pow(2, b - 1) / tezlik / YIL;        // oʻrtacha — yarim kalit fazosi
            System.out.println(String.format("%d bit: %.2e kalit, oʻrtacha %.2e yil", b, kalitlar, vaqt));
        }
    }
}
```
```cpp
#include <cmath>
#include <cstdio>
#include <iostream>
using namespace std;

const double YIL = 31557600;

int main() {
    int b;
    long long tezlik;
    while (cin >> b >> tezlik) {
        double kalitlar = pow(2.0, b);
        double vaqt = pow(2.0, b - 1) / tezlik / YIL;               // oʻrtacha — yarim kalit fazosi
        printf("%d bit: %.2e kalit, oʻrtacha %.2e yil\n", b, kalitlar, vaqt);
    }
    return 0;
}
```

Kirish:

```text
40 1000000000
56 1000000000
128 1000000000000
```

Chiqish:

```text
40 bit: 1.10e+12 kalit, oʻrtacha 1.74e-05 yil
56 bit: 7.21e+16 kalit, oʻrtacha 1.14e+00 yil
128 bit: 3.40e+38 kalit, oʻrtacha 5.39e+18 yil
```

40 bitli kalit (1990-yillarning "eksport" shifrlari) sekundiga milliard urinishda ~9 daqiqada topiladi. 56 bitli DES — bitta kompyuterda ~1 yil, maxsus qurilmalarda kunlar (1998-yilda "Deep Crack" 56 soatda buzgan). 128 bitli AES kaliti esa trillion urinishda ham koinot yoshidan yuzlab million marta koʻp vaqt talab qiladi — shuning uchun hozirgi standart kamida 128 bit.

:::warning
**Koʻp uchraydigan xatolar**

- **Kodlash va shifrlashni chalkashtirish.** Base64 yoki ASCII — kodlash (kalitsiz, hamma ocha oladi); shifrlash — kalit bilan.
- **"Oʻzim oʻylab topgan maxfiy algoritm ishonchliroq" deb hisoblash.** Kerkxofs prinsipiga zid: tekshirilmagan algoritm deyarli har doim zaif.
- **Kalit fazosi kattaligini yetarli deb bilish.** Bir alifboli almashtirishda 26! kalit bor, lekin chastota tahlili uni bir necha daqiqada ochadi.
- **Simmetrik va asimmetrik shifrlashni bir-biriga qarshi qoʻyish.** Amalda ular birga (gibrid) ishlatiladi.
:::

:::tip
Ixtiyoriy inglizcha matnni (masalan, sevimli qoʻshigʻingiz soʻzlarini) birinchi misol dasturiga bering va natijani ingliz tilining oʻrtacha chastotalari (E ≈ 12,7%, T ≈ 9,1%, A ≈ 8,2%) bilan solishtiring. Matn qanchalik uzun boʻlsa, natija shunchalik yaqinlashadi — shuning uchun qisqa shifrmatnlarni ochish qiyinroq.
:::

## Xulosa

- Kriptologiya = kriptografiya + kriptoanaliz; *C* = *E*(*K*, *M*), *M* = *D*(*K*, *C*).
- Simmetrik tizimlar tez, lekin kalitni yetkazish muammosi bor; asimmetrik — sekin, lekin kalit almashish va imzo imkonini beradi; amalda — gibrid.
- Kerkxofs prinsipi: xavfsizlik faqat kalitning maxfiyligiga bogʻliq.
- Tarixiy shifrlar: almashtirish (Atbash, Sezar, Vijiner) va oʻrin almashtirish (Skital); chastota tahlili bir alifboli shifrlarni ochadi.
- Toʻliq tanlash 2^(*b*−1) urinish talab qiladi; zamonaviy kalitlar — kamida 128 bit.

## Oʻzingizni tekshiring

:::check
Q: Kerkxofs prinsipi nimani taʼkidlaydi?
A: Tizim xavfsizligi algoritmning emas, faqat kalitning maxfiyligiga bogʻliq boʻlishi kerak.
Q: Simmetrik shifrlashning asosiy muammosi nima?
A: Umumiy maxfiy kalitni tomonlar orasida xavfsiz yetkazish.
Q: Sezar shifri qanday turdagi shifr va unda nechta kalit bor?
A: Bir alifboli almashtirish; lotin alifbosida 25 ta foydali kalit.
Q: Nima uchun chastota tahlili bir alifboli almashtirishni ochadi?
A: Har bir harf doim bir xil belgiga almashadi, shuning uchun til harflarining chastotalari shifrmatnda saqlanib qoladi.
Q: 64 bitli kalitni toʻliq tanlash uchun oʻrtacha nechta urinish kerak?
A: 2⁶³ ≈ 9,2·10¹⁸.
:::

:::link https://www.cryptool.org/en/
CrypTool
Kriptografiya va kriptoanalizni interaktiv oʻrganish uchun bepul platforma (ingliz tilida).
:::
