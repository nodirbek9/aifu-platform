---
title: RSA algoritmini oddiy misol orqali qoʻllash
summary: RSA ni amalda qoʻllash — kichik sonlarda qoʻlda kalit yaratish, shifrlash va deshifrlash; dasturiy topshiriq (kengaytirilgan Evklid algoritmi, modul boʻyicha darajaga koʻtarish); OpenSSL da 2048 bitli kalit, OAEP bilan shifrlash va SHA-256 bilan imzolash; "darslik" RSA zaifliklari — kichik e = 3 hujumi, deterministik shifrlashga lugʻat hujumi, multiplikativlik; nima uchun toʻldirish (OAEP, PSS) majburiy.
---

## Nima uchun bu mavzu muhim?

M7 maʼruzasida RSA matematikasini koʻrdik: *n* = *p*·*q*, *e*·*d* ≡ 1 (mod φ(*n*)), *c* = *m*ᵉ mod *n*. Bu mashgʻulotda formulalarni oʻz qoʻlingiz va dasturingiz bilan bajarasiz, keyin haqiqiy vosita (OpenSSL) bilan ishlaysiz. Eng muhim saboq: formulalarning oʻzi — **"darslik RSA"** — xavfsiz emas. Quyidagi ikki ishlangan misolda toʻldirishsiz RSA kalitni bilmasdan qanday ochilishini koʻrasiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- kichik sonlarda RSA kalitini qoʻlda yaratib, shifrlay va deshifrlay olasiz;
- RSA bosqichlarini dasturda amalga oshirasiz (platformadagi A7 topshirigʻi);
- OpenSSL da kalit yaratish, OAEP bilan shifrlash va imzolashni bajara olasiz;
- toʻldirishsiz RSA ning zaifliklarini koʻrsata olasiz.
:::

## Amaliy ish

**1-qadam. Qoʻlda RSA.** *p* = 5, *q* = 11 boʻlsin.

1. *n* = 5 · 11 = 55; φ(*n*) = 4 · 10 = 40.
2. *e* = 3: EKUB(3, 40) = 1 — mos keladi.
3. *d* ni toping: 3 · *d* ≡ 1 (mod 40). Kengaytirilgan Evklid algoritmini qoʻlda bajaring yoki *d* = 1, 2, 3, … ni tekshiring (javob: *d* = 27, chunki 3 · 27 = 81 = 2 · 40 + 1).
4. *m* = 7 ni shifrlang: 7³ = 343; 343 mod 55 = 13 → *c* = 13.
5. Deshifrlash: 13²⁷ mod 55 ni ketma-ket kvadratga koʻtarish bilan hisoblang (13² = 169 ≡ 4, 13⁴ ≡ 16, 13⁸ ≡ 36, 13¹⁶ ≡ 31; 27 = 16 + 8 + 2 + 1) va 7 chiqishini tekshiring.

Mustaqil: *p* = 7, *q* = 13, *e* = 5 bilan *m* = 10 ni shifrlang va deshifrlang.

**2-qadam. Dastur.** Platformadagi **RSA: kalit yaratish, shifrlash va deshifrlash** topshirigʻini bajaring: *n*, φ(*n*), kengaytirilgan Evklid algoritmi bilan *d*, *c* = *m*ᵉ mod *n* va tekshiruv *c*ᵈ mod *n* = *m*. Oraliq koʻpaytmalar toshib ketmasligiga eʼtibor bering (Java da `long`, C++ da `long long` yoki `__int128`).

**3-qadam. Haqiqiy RSA (OpenSSL).** Git Bash, Linux yoki macOS da:

```text
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out yopiq.pem
openssl pkey -in yopiq.pem -pubout -out ochiq.pem
openssl pkeyutl -encrypt -pubin -inkey ochiq.pem -pkeyopt rsa_padding_mode:oaep -in xabar.txt -out xabar.enc
openssl pkeyutl -decrypt -inkey yopiq.pem -pkeyopt rsa_padding_mode:oaep -in xabar.enc
openssl dgst -sha256 -sign yopiq.pem -out imzo.bin hujjat.txt
openssl dgst -sha256 -verify ochiq.pem -signature imzo.bin hujjat.txt
```

Bir xil xabarni ikki marta shifrlang — OAEP tasodifiy toʻldirish qoʻshgani uchun `xabar.enc` fayllari har xil boʻladi. Imzodan keyin `hujjat.txt` ga bitta belgi qoʻshib, tekshiruvni takrorlang: "Verified OK" oʻrniga "Verification failure" chiqadi.

## "Darslik RSA" zaifliklari

| Zaiflik | Mohiyati | Himoya |
|---|---|---|
| **Kichik *e* va qisqa xabar** | *m*ᵉ < *n* boʻlsa, mod amali ishlamaydi: *m* = ∛*c* oddiy ildiz | OAEP toʻldirish *m* ni *n* oʻlchamigacha kengaytiradi |
| **Deterministiklik** | bir xil *m* doim bir xil *c* beradi — kam variantli xabarlarni lugʻat bilan topish mumkin | OAEP tasodifiy baytlar qoʻshadi |
| **Multiplikativlik** | *c*₁ · *c*₂ mod *n* = (*m*₁ · *m*₂)ᵉ mod *n* — shifrmatnni maʼnoli oʻzgartirish mumkin | toʻldirish strukturani buzadi |
| **Kichik yoki yaqin *p*, *q*** | *n* ni ajratish oson (M7, 2-misol) | kamida 2048 bitli *n*, tasodifiy tub sonlar |
| **Toʻldirishsiz imzo** | imzolarni koʻpaytirib yangi xabar uchun soxta imzo yasash | xesh + PSS toʻldirish |

## Ishlangan misol 1: e = 3 va qisqa xabar

Birinchi satrda modul *n*, keyingi satrlarda *e* = 3 bilan shifrlangan, toʻldirishsiz shifrmatnlar. Xabar matn baytlaridan tuzilgan son (masalan, "OK" = 0x4F4B = 20299). Agar *m*³ < *n* boʻlsa, *c* = *m*³ — oddiy butun kub ildiz *m* ni beradi. Har bir *c* uchun butun kub ildizni ikkilik qidiruv bilan toping; aniq kub boʻlsa, *m* va uning matnini, aks holda `topilmadi` ni chiqaring.

```python
import sys

satrlar = sys.stdin.read().split()
n = int(satrlar[0])                  # yopiq kalit kerak emas — n faqat maʼlumot uchun
for t in satrlar[1:]:
    c = int(t)
    past, yuqori = 0, 2097151         # 2097151^3 < 2^63
    while past < yuqori:              # r^3 <= c boʻlgan eng katta r
        orta = (past + yuqori + 1) // 2
        if orta ** 3 <= c:
            past = orta
        else:
            yuqori = orta - 1
    if past ** 3 == c:
        matn = ""
        x = past
        while x > 0:
            matn = chr(x % 256) + matn    # son baytlari — matn belgilari
            x //= 256
        print(f"{c} -> m={past} {matn}")
    else:
        print(f"{c} -> topilmadi")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = in.nextLong();                     // yopiq kalit kerak emas — n faqat maʼlumot uchun
        while (in.hasNextLong()) {
            long c = in.nextLong();
            long past = 0, yuqori = 2097151;        // 2097151^3 < 2^63
            while (past < yuqori) {                 // r^3 <= c boʻlgan eng katta r
                long orta = (past + yuqori + 1) / 2;
                if (orta * orta * orta <= c) past = orta;
                else yuqori = orta - 1;
            }
            if (past * past * past == c) {
                StringBuilder matn = new StringBuilder();
                for (long x = past; x > 0; x /= 256) {
                    matn.insert(0, (char) (x % 256));   // son baytlari — matn belgilari
                }
                System.out.println(c + " -> m=" + past + " " + matn);
            } else {
                System.out.println(c + " -> topilmadi");
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    long long n, c;
    cin >> n;                                       // yopiq kalit kerak emas — n faqat maʼlumot uchun
    while (cin >> c) {
        long long past = 0, yuqori = 2097151;       // 2097151^3 < 2^63
        while (past < yuqori) {                     // r^3 <= c boʻlgan eng katta r
            long long orta = (past + yuqori + 1) / 2;
            if (orta * orta * orta <= c) past = orta;
            else yuqori = orta - 1;
        }
        if (past * past * past == c) {
            string matn;
            for (long long x = past; x > 0; x /= 256) {
                matn = char(x % 256) + matn;        // son baytlari — matn belgilari
            }
            cout << c << " -> m=" << past << " " << matn << endl;
        } else {
            cout << c << " -> topilmadi" << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
1000000104000000679
8364190790899
6328545249473
615573431271999514
```

Chiqish:

```text
8364190790899 -> m=20299 OK
6328545249473 -> m=18497 HA
615573431271999514 -> topilmadi
```

Birinchi ikki xabar yopiq kalitsiz, faqat kub ildiz bilan ochildi: "OK" va "HA" uchun *m*³ < *n*, yaʼni modul boʻyicha qoldiq olish umuman sodir boʻlmagan. Uchinchi xabar ("SIR" = 5458258) uzunroq: *m*³ > *n*, qoldiq olingan va oddiy ildiz ishlamaydi. Haqiqiy RSA da OAEP har qanday xabarni *n* uzunligidagi tasodifiy koʻrinishdagi songa aylantiradi — bu hujum imkonsiz boʻladi.

## Ishlangan misol 2: deterministik RSA ga lugʻat hujumi

Oʻqituvchi baholarni (2, 3, 4, 5) talabalarga ochiq kalit bilan, toʻldirishsiz shifrlab yuboradi. Hujumchi ochiq kalitni biladi — u har bir mumkin boʻlgan bahoni oʻzi shifrlab, ushlab olingan shifrmatnlar bilan solishtiradi. Kirish: 1-satr *n* *e*; 2-satr mumkin boʻlgan xabarlar; 3-satr ushlangan shifrmatnlar. Har bir shifrmatn uchun mos xabarni yoki `?` ni chiqaring.

```python
import sys

satrlar = sys.stdin.read().split("\n")
n, e = map(int, satrlar[0].split())
lugat = {}
for m in satrlar[1].split():
    lugat[pow(int(m), e, n)] = m          # ochiq kalit bilan har bir variantni shifrlaymiz
for c in satrlar[2].split():
    print(f"{c} -> {lugat.get(int(c), '?')}")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static long darajaMod(long a, long k, long n) {   // a^k mod n (n < 2^31 — koʻpaytma long ga sigʻadi)
        long natija = 1;
        a %= n;
        while (k > 0) {
            if ((k & 1) == 1) natija = natija * a % n;
            a = a * a % n;
            k >>= 1;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] kalit = in.nextLine().trim().split("\\s+");
        long n = Long.parseLong(kalit[0]);
        long e = Long.parseLong(kalit[1]);
        Map<Long, String> lugat = new HashMap<>();
        for (String m : in.nextLine().trim().split("\\s+")) {
            lugat.put(darajaMod(Long.parseLong(m), e, n), m);   // ochiq kalit bilan har bir variantni shifrlaymiz
        }
        for (String c : in.nextLine().trim().split("\\s+")) {
            System.out.println(c + " -> " + lugat.getOrDefault(Long.parseLong(c), "?"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
using namespace std;

long long darajaMod(long long a, long long k, long long n) {   // a^k mod n (n < 2^31 — koʻpaytma long long ga sigʻadi)
    long long natija = 1;
    a %= n;
    while (k > 0) {
        if (k & 1) natija = natija * a % n;
        a = a * a % n;
        k >>= 1;
    }
    return natija;
}

int main() {
    string satr;
    getline(cin, satr);
    long long n, e;
    istringstream(satr) >> n >> e;
    map<long long, string> lugat;
    getline(cin, satr);
    istringstream variantlar(satr);
    string m;
    while (variantlar >> m) {
        lugat[darajaMod(stoll(m), e, n)] = m;      // ochiq kalit bilan har bir variantni shifrlaymiz
    }
    getline(cin, satr);
    istringstream ushlangan(satr);
    string c;
    while (ushlangan >> c) {
        auto it = lugat.find(stoll(c));
        cout << c << " -> " << (it != lugat.end() ? it->second : "?") << endl;
    }
    return 0;
}
```

Kirish:

```text
100160063 65537
2 3 4 5
1727207 24561622 82696754 1727207 11111
```

Chiqish:

```text
1727207 -> 4
24561622 -> 5
82696754 -> 2
1727207 -> 4
11111 -> ?
```

Hujumchi yopiq kalitni ham, *n* ning koʻpaytuvchilarini ham bilmay turib barcha baholarni oʻqidi — chunki xabarlar fazosi atigi 4 ta. Birinchi va toʻrtinchi shifrmatn bir xil — demak baholar ham bir xil, bu ham maʼlumot sizishi. Kalit uzunligi bu yerda yordam bermaydi: 4096 bitli *n* bilan ham natija shu. Yechim — **tasodifiy toʻldirish** (OAEP): bir xil baho har safar boshqa shifrmatn beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Oraliq koʻpaytmaning toshishi.** *a* · *b* mod *n* da *a* · *b* 64 bitdan oshishi mumkin; har qadamda mod oling va turni yetarli tanlang.
- **mᵉ ni avval toʻliq hisoblab, keyin mod olish.** Son ulkan boʻlib ketadi; ketma-ket kvadratga koʻtarish (square-and-multiply) ishlating.
- **EKUB(e, φ(n)) ≠ 1 boʻlgan e.** Bunday *e* uchun *d* mavjud emas.
- **"Darslik RSA" ni haqiqiy maʼlumot uchun ishlatish.** Shifrlash uchun faqat OAEP, imzo uchun PSS; amalda RSA odatda faqat simmetrik kalitni uzatish va imzo uchun qoʻllanadi.
- **Yopiq kalit faylini himoyasiz qoldirish.** `yopiq.pem` ni parol bilan shifrlang (`-aes256`) va hech kimga yubormang.
:::

:::tip
`openssl pkey -in yopiq.pem -text -noout` buyrugʻi kalit tarkibini koʻrsatadi: modulus, publicExponent (65537), privateExponent, prime1, prime2, hamda tezlashtirish uchun exponent1, exponent2 va coefficient (Xitoy qoldiqlar teoremasi parametrlari). Bu buyruqni faqat oʻquv kalitida ishlating.
:::

## Xulosa

- RSA bosqichlari: *n* = *pq*, φ(*n*), *e* (EKUB = 1), *d* = *e*⁻¹ mod φ(*n*), *c* = *m*ᵉ mod *n*, *m* = *c*ᵈ mod *n*.
- Hisoblash: kengaytirilgan Evklid algoritmi va ketma-ket kvadratga koʻtarish; toshishdan saqlaning.
- OpenSSL: `genpkey`, `pkeyutl` (OAEP) va `dgst -sign/-verify` (SHA-256 imzo).
- Toʻldirishsiz RSA zaif: kichik *e* da kub ildiz, deterministiklik tufayli lugʻat hujumi, multiplikativlik.
- Amalda: kamida 2048 bitli kalit, shifrlash uchun OAEP, imzo uchun PSS.

## Oʻzingizni tekshiring

:::check
Q: p = 5, q = 11, e = 3 boʻlsa, d nimaga teng?
A: 27, chunki φ(n) = 40 va 3 · 27 = 81 ≡ 1 (mod 40).
Q: Nima uchun e = 3 va qisqa xabarda shifrmatnni kalitsiz ochish mumkin?
A: m³ < n boʻlsa, modul boʻyicha qoldiq olinmaydi va c = m³ — oddiy butun kub ildiz m ni beradi.
Q: Deterministik shifrlash qanday hujumga yoʻl ochadi?
A: Lugʻat hujumiga: hujumchi ochiq kalit bilan barcha ehtimoliy xabarlarni shifrlab, shifrmatnlarni solishtiradi.
Q: OAEP qanday himoya beradi?
A: Tasodifiy toʻldirish qoʻshadi: bir xil xabar har safar boshqa shifrmatn beradi va xabar n uzunligigacha kengayadi.
:::

:::link https://www.rfc-editor.org/rfc/rfc8017
RFC 8017: PKCS #1 — RSA Cryptography Specifications
RSA, OAEP shifrlash va PSS imzo standartining spetsifikatsiyasi (ingliz tilida).
:::
