---
title: Simmetrik algoritmlar (AES) bilan shifrlash dasturida mashq
summary: AES bilan amaliy ishlash — CyberChef da FIPS 197 test vektorini tekshirish, kalit, IV va rejimni tanlash, parol asosida kalit hosil qilish (PBKDF2), OpenSSL bilan faylni shifrlash va deshifrlash, 7-Zip arxivi va disk shifrlash (BitLocker, VeraCrypt); AES ichki qadamlari — GF(2⁸) maydonida koʻpaytirish, S-box (SubBytes) va MixColumns ni hisoblash; mavzu boʻyicha test.
---

## Nima uchun bu mavzu muhim?

M6 maʼruzasida AES tuzilishini va ishlash rejimlarini nazariy koʻrdik. Amalda xatolar algoritmda emas, uni **ishlatishda** boʻladi: kalit oʻrniga oddiy parol, takrorlangan IV, ECB rejimi, kalitni shifrmatn yonida saqlash. Bu mashgʻulotda AES ni haqiqiy vositalarda qoʻllaymiz, natijani rasmiy test vektori bilan tekshiramiz va AES ichidagi ikki qadamni oʻzimiz hisoblaymiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- CyberChef va OpenSSL da AES bilan shifrlash va deshifrlashni bajara olasiz;
- kalit, IV, rejim va toʻldirishni toʻgʻri tanlay olasiz;
- paroldan kalit qanday hosil qilinishini (PBKDF2, tuz) tushuntira olasiz;
- GF(2⁸) da koʻpaytirish, S-box va MixColumns qiymatlarini hisoblay olasiz.
:::

## Eslatma: AES parametrlari

| Parametr | Qiymati |
|---|---|
| Blok | doim 128 bit (16 bayt) |
| Kalit | 128, 192 yoki 256 bit → 10, 12 yoki 14 raund |
| Raund | SubBytes → ShiftRows → MixColumns → AddRoundKey (oxirgi raundda MixColumns yoʻq) |
| Rejim | **GCM** (shifrlash + yaxlitlik) tavsiya etiladi; CBC — IV tasodifiy boʻlsa; ECB — ishlatmang |
| IV / nonce | CBC da 16 bayt tasodifiy; GCM da 12 bayt, bitta kalit bilan **hech qachon takrorlanmaydi** |
| Toʻldirish | CBC va ECB da PKCS#7 (yetmagan *n* bayt — *n* qiymatli *n* ta bayt); CTR va GCM da kerak emas |

## Amaliy ish

**1-qadam. Test vektori (CyberChef).** gchq.github.io/CyberChef da "AES Encrypt" retseptini tanlang: Key = `000102030405060708090a0b0c0d0e0f` (Hex), Mode = ECB, Input = `00112233445566778899aabbccddeeff` (Input format: Hex, Output: Hex). Natija FIPS 197 standartidagi qiymatga teng boʻlishi kerak: `69c4e0d86a7b0430d8cdb78070b4c55a` (PKCS#7 tufayli undan keyin yana bitta toʻldirish bloki chiqadi). Test vektori — dastur toʻgʻri ishlashini tekshirishning standart usuli.

**2-qadam. Rejimlar.** Oʻsha retseptda Mode = CBC, IV = 16 ta nol bayt qiling, keyin IV ning bitta baytini oʻzgartiring — butun shifrmatn oʻzgaradi. GCM rejimida natijaga **Tag** (autentifikatsiya kodi) qoʻshiladi: shifrmatnning bitta baytini oʻzgartirib "AES Decrypt" qilsangiz, deshifrlash xato beradi — bu yaxlitlik himoyasi.

**3-qadam. Faylni parol bilan shifrlash (OpenSSL).** Linux, macOS yoki Git Bash da:

```text
openssl enc -aes-256-cbc -pbkdf2 -iter 600000 -salt -in hisobot.txt -out hisobot.enc
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in hisobot.enc -out hisobot2.txt
```

Bu yerda parol toʻgʻridan-toʻgʻri kalit emas: **PBKDF2** parol va tasodifiy **tuz** dan 600 000 marta xesh hisoblab, 256 bitli kalit va IV hosil qiladi — parolni tanlab topish shuncha marta sekinlashadi. Bir xil faylni ikki marta shifrlang: tuz har safar yangi boʻlgani uchun shifrmatnlar farq qiladi.

**4-qadam. Kundalik vositalar.** 7-Zip da arxiv yaratishda "Encryption method: AES-256" va "Encrypt file names" ni tanlang. Butun diskni shifrlash: Windows da **BitLocker** (Pro nashrlarida), boshqa tizimlarda **VeraCrypt**. Ularning barchasi ichida AES ishlaydi; xavfsizlik esa asosan **parol sifatiga** va tiklash kalitini qayerda saqlashingizga bogʻliq.

**5-qadam. Test.** Platformadagi "Simmetrik shifrlash va AES" testini bajaring.

## Ishlangan misol 1: GF(2⁸) va S-box

AES baytlarni **GF(2⁸)** maydoni elementlari sifatida qaraydi: qoʻshish — XOR, koʻpaytirish — koʻphadlarni koʻpaytirib, *x*⁸ + *x*⁴ + *x*³ + *x* + 1 (0x11B) moduli boʻyicha qoldiq olish. SubBytes jadvali (S-box) har bir bayt uchun ikki qadamda hisoblanadi: 1) maydondagi teskari element *b* = *x*⁻¹ (0 uchun 0); 2) affin almashtirish *s* = *b* ⊕ rotl(*b*, 1) ⊕ rotl(*b*, 2) ⊕ rotl(*b*, 3) ⊕ rotl(*b*, 4) ⊕ 0x63. Har bir hex bayt uchun teskari elementni va S-box qiymatini chiqaring.

```python
import sys


def kopaytir(a, b):                       # GF(2^8) da koʻpaytirish
    r = 0
    while b:
        if b & 1:
            r ^= a                        # qoʻshish = XOR
        a <<= 1
        if a & 0x100:
            a ^= 0x11B                    # x^8 + x^4 + x^3 + x + 1 moduli
        b >>= 1
    return r


def teskari(a):                           # a^254 = a^(-1), chunki a^255 = 1
    if a == 0:
        return 0
    r = 1
    for _ in range(254):
        r = kopaytir(r, a)
    return r


def sbox(x):
    b = teskari(x)
    s = b
    for i in range(1, 5):
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF   # aylanma chapga surish
    return s ^ 0x63


for t in sys.stdin.read().split():
    x = int(t, 16)
    print(f"{t} -> inv={teskari(x):02x} S={sbox(x):02x}")
```
```java
import java.util.Scanner;

public class Main {
    static int kopaytir(int a, int b) {                  // GF(2^8) da koʻpaytirish
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;                    // qoʻshish = XOR
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;            // x^8 + x^4 + x^3 + x + 1 moduli
            b >>= 1;
        }
        return r;
    }

    static int teskari(int a) {                          // a^254 = a^(-1), chunki a^255 = 1
        if (a == 0) return 0;
        int r = 1;
        for (int i = 0; i < 254; i++) r = kopaytir(r, a);
        return r;
    }

    static int sbox(int x) {
        int b = teskari(x);
        int s = b;
        for (int i = 1; i <= 4; i++) {
            s ^= ((b << i) | (b >> (8 - i))) & 0xFF;     // aylanma chapga surish
        }
        return s ^ 0x63;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String t = in.next();
            int x = Integer.parseInt(t, 16);
            System.out.println(String.format("%s -> inv=%02x S=%02x", t, teskari(x), sbox(x)));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int kopaytir(int a, int b) {                             // GF(2^8) da koʻpaytirish
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;                               // qoʻshish = XOR
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;                       // x^8 + x^4 + x^3 + x + 1 moduli
        b >>= 1;
    }
    return r;
}

int teskari(int a) {                                     // a^254 = a^(-1), chunki a^255 = 1
    if (a == 0) return 0;
    int r = 1;
    for (int i = 0; i < 254; i++) r = kopaytir(r, a);
    return r;
}

int sbox(int x) {
    int b = teskari(x);
    int s = b;
    for (int i = 1; i <= 4; i++) {
        s ^= ((b << i) | (b >> (8 - i))) & 0xFF;         // aylanma chapga surish
    }
    return s ^ 0x63;
}

int main() {
    string t;
    while (cin >> t) {
        int x = stoi(t, nullptr, 16);
        printf("%s -> inv=%02x S=%02x\n", t.c_str(), teskari(x), sbox(x));
    }
    return 0;
}
```

Kirish:

```text
00 01 53 c9 ff 10
```

Chiqish:

```text
00 -> inv=00 S=63
01 -> inv=01 S=7c
53 -> inv=ca S=ed
c9 -> inv=27 S=dd
ff -> inv=1c S=16
10 -> inv=74 S=ca
```

Natijalar FIPS 197 dagi S-box jadvaliga mos keladi (masalan, S(53) = ed — standartdagi misol). 0x53 · 0xca = 1 ekanini qoʻlda tekshirib koʻring. S-box AES ning yagona **chiziqsiz** qismi: teskari element tufayli kirish va chiqish orasida oddiy chiziqli bogʻlanish yoʻq — bu chiziqli va differensial kriptotahlilga qarshi asosiy himoya. Amaliy dasturlarda S-box hisoblanmaydi, tayyor 256 baytli jadval ishlatiladi.

## Ishlangan misol 2: MixColumns

MixColumns holat matritsasining har bir ustunini (4 bayt) GF(2⁸) da quyidagi matritsaga koʻpaytiradi:

```text
02 03 01 01
01 02 03 01
01 01 02 03
03 01 01 02
```

Har bir satrda 4 ta hex bayt (ustun); natijaviy ustunni chiqaring.

```python
import sys

M = [[2, 3, 1, 1], [1, 2, 3, 1], [1, 1, 2, 3], [3, 1, 1, 2]]


def kopaytir(a, b):                       # GF(2^8) da koʻpaytirish
    r = 0
    while b:
        if b & 1:
            r ^= a
        a <<= 1
        if a & 0x100:
            a ^= 0x11B
        b >>= 1
    return r


for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    ustun = [int(t, 16) for t in satr.split()]
    natija = []
    for i in range(4):
        v = 0
        for j in range(4):
            v ^= kopaytir(M[i][j], ustun[j])   # yigʻindi = XOR
        natija.append(v)
    print(satr.strip() + " -> " + " ".join(format(v, "02x") for v in natija))
```
```java
import java.util.Scanner;

public class Main {
    static final int[][] M = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

    static int kopaytir(int a, int b) {                  // GF(2^8) da koʻpaytirish
        int r = 0;
        while (b != 0) {
            if ((b & 1) != 0) r ^= a;
            a <<= 1;
            if ((a & 0x100) != 0) a ^= 0x11B;
            b >>= 1;
        }
        return r;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String satr = in.nextLine().trim();
            if (satr.isEmpty()) continue;
            String[] t = satr.split("\\s+");
            int[] ustun = new int[4];
            for (int j = 0; j < 4; j++) ustun[j] = Integer.parseInt(t[j], 16);
            StringBuilder sb = new StringBuilder(satr + " ->");
            for (int i = 0; i < 4; i++) {
                int v = 0;
                for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // yigʻindi = XOR
                sb.append(String.format(" %02x", v));
            }
            System.out.println(sb);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

const int M[4][4] = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

int kopaytir(int a, int b) {                             // GF(2^8) da koʻpaytirish
    int r = 0;
    while (b != 0) {
        if (b & 1) r ^= a;
        a <<= 1;
        if (a & 0x100) a ^= 0x11B;
        b >>= 1;
    }
    return r;
}

int main() {
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        int ustun[4];
        string t;
        int n = 0;
        while (n < 4 && ss >> t) ustun[n++] = stoi(t, nullptr, 16);
        if (n < 4) continue;
        for (int j = 0; j < 4; j++) printf(j ? " %02x" : "%02x", ustun[j]);
        printf(" ->");
        for (int i = 0; i < 4; i++) {
            int v = 0;
            for (int j = 0; j < 4; j++) v ^= kopaytir(M[i][j], ustun[j]);   // yigʻindi = XOR
            printf(" %02x", v);
        }
        printf("\n");
    }
    return 0;
}
```

Kirish:

```text
db 13 53 45
f2 0a 22 5c
01 01 01 01
c6 c6 c6 c6
d4 d4 d4 d5
2d 26 31 4c
```

Chiqish:

```text
db 13 53 45 -> 8e 4d a1 bc
f2 0a 22 5c -> 9f dc 58 9d
01 01 01 01 -> 01 01 01 01
c6 c6 c6 c6 -> c6 c6 c6 c6
d4 d4 d4 d5 -> d5 d5 d7 d6
2d 26 31 4c -> 4d 7e bd f8
```

Bular MixColumns ning keng tarqalgan test qiymatlari. Bir xil baytlardan iborat ustun oʻzgarmaydi (2 ⊕ 3 ⊕ 1 ⊕ 1 = 1), lekin `d4 d4 d4 d5` da bitta baytning bitta biti oʻzgarishi ustunning **barcha** toʻrt baytiga tarqaldi — bu Shannon ning **tarqatish** (diffusion) tamoyili. ShiftRows bilan birga ikki raunddan keyin har bir kirish bayti holatning barcha 16 baytiga taʼsir qiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Parolni toʻgʻridan-toʻgʻri kalit sifatida ishlatish.** Paroldan kalitni faqat PBKDF2, scrypt yoki Argon2 bilan, tuz qoʻshib hosil qiling.
- **IV ni doim nol yoki bir xil qilish.** CBC da bir xil boshlanadigan fayllar bir xil shifrmatn beradi; GCM da nonce takrorlansa, himoya butunlay buziladi.
- **ECB rejimi.** Kutubxonalarda koʻpincha "standart" rejim — doim rejimni aniq koʻrsating.
- **Kalitni shifrlangan fayl yonida saqlash.** Kalit — alohida joyda: kalitlar ombori, parol menejeri, apparat modul.
- **Faqat shifrlash, yaxlitliksiz.** CBC shifrmatnni oʻzgartirishdan himoyalamaydi; GCM yoki shifrlash + HMAC ishlating.
:::

:::tip
OpenSSL ning `-p` bayrogʻi (`openssl enc -aes-256-cbc -pbkdf2 -p ...`) paroldan hosil qilingan tuz, kalit va IV ni koʻrsatadi — ikki marta ishga tushirib, tuz har safar yangi ekanini koʻring. Bu bayroqni faqat oʻquv fayllarida ishlating: u kalitni ekranga chiqaradi.
:::

## Xulosa

- AES: 128 bitli blok, 128/192/256 bitli kalit; amalda AES-GCM yoki AES-CBC + HMAC, tasodifiy IV yoki takrorlanmas nonce.
- Dastur toʻgʻriligi rasmiy test vektorlari bilan tekshiriladi (FIPS 197: `69c4e0d8…`).
- Paroldan kalit — PBKDF2/scrypt/Argon2 va tuz bilan; parolning oʻzi kalit emas.
- GF(2⁸) da qoʻshish — XOR, koʻpaytirish — 0x11B moduli boʻyicha; S-box — teskari element + affin almashtirish, AES ning chiziqsiz qismi.
- MixColumns bitta bayt oʻzgarishini butun ustunga tarqatadi — tarqatish tamoyili.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun bir xil faylni OpenSSL bilan bir xil parolda ikki marta shifrlaganda shifrmatnlar har xil chiqadi?
A: Har safar yangi tasodifiy tuz ishlatiladi, shuning uchun PBKDF2 boshqa kalit va IV hosil qiladi.
Q: GCM rejimida shifrmatnning bitta bayti oʻzgartirilsa, deshifrlashda nima boʻladi?
A: Autentifikatsiya kodi (tag) mos kelmaydi va deshifrlash xato bilan rad etiladi.
Q: AES ning qaysi qadami chiziqsiz va u qanday hisoblanadi?
A: SubBytes (S-box): GF(2⁸) dagi teskari element va undan keyingi affin almashtirish.
Q: MixColumns qaysi kriptografik tamoyilni amalga oshiradi?
A: Tarqatish (diffusion): bitta kirish bayti oʻzgarishi ustunning barcha baytlariga taʼsir qiladi.
:::

:::link https://csrc.nist.gov/pubs/fips/197/final
FIPS 197: Advanced Encryption Standard (AES)
AES standarti: S-box jadvali, MixColumns va test vektorlari (ingliz tilida).
:::
