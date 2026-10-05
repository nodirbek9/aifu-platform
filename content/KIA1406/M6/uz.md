---
title: Simmetrik kriptografik algoritmlar. Oqimli va blokli shifrlash, A5/1 va AES misolida
summary: Simmetrik shifrlash gʻoyasi; Shannon tamoyillari — chalkashtirish va tarqatish; oqimli shifrlar — bir martalik bloknot, kalit oqimi generatorlari, LFSR, A5/1 (GSM) va uning zaifliklari, ChaCha20; blokli shifrlar — Feystel tarmogʻi va DES, AES tuzilishi (SubBytes, ShiftRows, MixColumns, AddRoundKey), kalit uzunliklari va raundlar; ishlash rejimlari — ECB, CBC, CTR, GCM; toʻldirish va IV.
---

## Nima uchun bu mavzu muhim?

Telefoningizdagi maʼlumotlar, Wi-Fi trafigi, HTTPS sahifalar, shifrlangan disklar — bularning barchasi simmetrik shifrlar bilan himoyalanadi, chunki ular juda tez: zamonaviy protsessor AES ni sekundiga gigabaytlab bajaradi. Lekin "AES ishlatildi" degani hali xavfsizlik emas: notoʻgʻri rejim (masalan, ECB) yoki takrorlangan IV kuchli algoritmni ham foydasiz qiladi. Bu maʼruzada simmetrik shifrlarning ichki tuzilishini va ulardan toʻgʻri foydalanish qoidalarini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- oqimli va blokli shifrlarning farqini tushuntira olasiz;
- LFSR va A5/1 qanday kalit oqimi hosil qilishini va A5/1 ning zaifliklarini ayta olasiz;
- AES ning raund tuzilishini va kalit uzunliklarini bilasiz;
- ECB, CBC, CTR va GCM rejimlarini solishtira olasiz;
- nima uchun ECB rejimi maʼlumot tuzilishini oshkor qilishini koʻrsata olasiz.
:::

## Shannon tamoyillari

1949-yilda Claude Shannon yaxshi shifrning ikki xossasini taʼrifladi:

- **Chalkashtirish** (confusion) — kalit va shifrmatn orasidagi bogʻliqlik iloji boricha murakkab boʻlishi (almashtirishlar, S-bloklar).
- **Tarqatish** (diffusion) — ochiq matnning bitta biti shifrmatnning koʻp bitlariga taʼsir qilishi (oʻrin almashtirishlar, aralashtirish). Natija — **koʻchki effekti**: bitta bit oʻzgarsa, shifrmatnning taxminan yarmi oʻzgaradi.

## Oqimli shifrlar

Oqimli shifr kalitdan uzun psevdotasodifiy **kalit oqimi** hosil qiladi va uni ochiq matn bilan bitma-bit **XOR** qiladi: *C* = *M* ⊕ *K*ₛ, *M* = *C* ⊕ *K*ₛ (shifrlash va deshifrlash bir xil amal).

- **Bir martalik bloknot** (one-time pad) — kalit oqimi haqiqiy tasodifiy, matn uzunligida va bir marta ishlatiladi: nazariy jihatdan mutlaqo buzilmas (Shannon isbotlagan), lekin kalitni yetkazish amalda qiyin.
- **Muhim qoida**: kalit oqimini hech qachon takrorlamang. Agar ikki xabar bir xil oqim bilan shifrlansa, *C*₁ ⊕ *C*₂ = *M*₁ ⊕ *M*₂ — kalitsiz ham matnlar haqida koʻp maʼlumot olinadi.

**LFSR** (chiziqli teskari bogʻlanishli siljitish registri) — eng oddiy kalit oqimi generatori: har qadamda registr bir bit suriladi, yangi bit belgilangan "chiqish" (tap) bitlarining XOR i. Yaxshi tanlangan *n* bitli LFSR davri 2ⁿ − 1. Lekin LFSR **chiziqli**: 2*n* ta chiqish biti boʻyicha uning tuzilishi tiklanadi (Berlekamp–Massey algoritmi), shuning uchun yolgʻiz oʻzi xavfsiz emas.

**A5/1** — GSM mobil aloqasida ishlatilgan oqimli shifr: uzunligi 19, 22 va 23 bitli uchta LFSR, 64 bitli kalit; registrlar **koʻpchilik qoidasi** bilan notekis siljiydi. 2000-yillarda u amalda buzildi (oldindan hisoblangan jadvallar bilan sekundlarda) — shuning uchun 3G/4G da KASUMI, SNOW 3G, AES kabi kuchliroq algoritmlarga oʻtildi. Bugungi zamonaviy oqimli shifr — **ChaCha20** (TLS, WireGuard, Android).

## Blokli shifrlar

Blokli shifr maʼlumotni qatʼiy uzunlikdagi bloklarga boʻlib, har birini kalit bilan oʻgiradi; bir necha **raund** takrorlanadi.

**DES** (1977) — 64 bitli blok, 56 bitli kalit, 16 raundli **Feystel tarmogʻi**. Kaliti qisqaligi sababli 1998-yilda toʻliq tanlash bilan buzildi; vaqtinchalik yechim — 3DES (uch marta DES), bugun u ham eskirgan.

**AES** (Rijndael, 2001-yildan AQSH standarti) — **128 bitli** blok, kalit **128, 192 yoki 256** bit, raundlar soni mos ravishda **10, 12, 14**. Blok 4×4 baytli "holat" matritsasi sifatida qaraladi; har bir raund toʻrt qadamdan iborat:

| Qadam | Vazifasi | Shannon xossasi |
|---|---|---|
| **SubBytes** | har bir bayt S-blok jadvali boʻyicha almashtiriladi (yagona nochiziqli qadam) | chalkashtirish |
| **ShiftRows** | qatorlar 0, 1, 2, 3 baytga chapga suriladi | tarqatish |
| **MixColumns** | har bir ustun GF(2⁸) maydonida matritsaga koʻpaytiriladi | tarqatish |
| **AddRoundKey** | holat raund kaliti bilan XOR qilinadi | kalitga bogʻlash |

Oxirgi raundda MixColumns yoʻq. Raund kalitlari asosiy kalitdan **kalitni kengaytirish** jarayoni bilan olinadi. Zamonaviy protsessorlarda AES uchun maxsus buyruqlar (AES-NI) bor.

## Ishlash rejimlari

Xabar odatda bitta blokdan uzun — bloklar qanday bogʻlanishini **rejim** belgilaydi.

| Rejim | Gʻoyasi | Xususiyati |
|---|---|---|
| **ECB** | har bir blok alohida shifrlanadi | bir xil bloklar → bir xil shifrmatn; **ishlatmang** |
| **CBC** | har bir blok oldingi shifrmatn bloki bilan XOR qilinib shifrlanadi; birinchisi — **IV** bilan | tasodifiy IV kerak, ketma-ket ishlaydi, toʻldirish kerak |
| **CTR** | hisoblagich shifrlanib kalit oqimi hosil qiladi (blok shifrni oqimliga aylantiradi) | parallel, toʻldirish shart emas; hisoblagich takrorlanmasligi shart |
| **GCM** | CTR + autentifikatsiya tegi | maxfiylik va yaxlitlik birga (AEAD); TLS 1.3 da standart |

**Toʻldirish** (padding): oxirgi blok toʻliq boʻlmasa, PKCS#7 usuli bilan toʻldiriladi (*k* bayt yetmasa, *k* qiymatli *k* bayt qoʻshiladi). **IV** (boshlangʻich vektor) maxfiy emas, lekin har bir xabar uchun yangi va oldindan aytib boʻlmaydigan boʻlishi kerak.

## Ishlangan misol 1: LFSR va oqimli shifr

16 bitli LFSR (teskari bogʻlanish: 0, 2, 3, 5-bitlar XOR i, oʻngga siljish) boshlangʻich holatdan kalit oqimi hosil qiladi. Har bir chiqish bayti — registrning eng kichik bitidan 8 marta olingan bitlar. Xabar baytlari kalit oqimi bilan XOR qilinadi; shu amal yana bajarilsa, xabar tiklanadi.

```python
def kalit_oqimi(holat, n):
    natija = []
    for _ in range(n):
        bayt = 0
        for _ in range(8):
            yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1   # teskari bogʻlanish
            bayt = (bayt << 1) | (holat & 1)
            holat = (holat >> 1) | (yangi << 15)
        natija.append(bayt)
    return natija


holat = int(input().strip(), 16)
xabar = input()
oqim = kalit_oqimi(holat, len(xabar))
shifr = [ord(c) ^ k for c, k in zip(xabar, oqim)]
ochilgan = "".join(chr(c ^ k) for c, k in zip(shifr, oqim))   # XOR yana — asl matn
print("kalit oqimi: " + " ".join(format(k, "02x") for k in oqim))
print("shifrmatn: " + " ".join(format(c, "02x") for c in shifr))
print("ochilgan: " + ochilgan)
```
```java
import java.util.Scanner;

public class Main {
    static int[] kalitOqimi(int holat, int n) {
        int[] natija = new int[n];
        for (int i = 0; i < n; i++) {
            int bayt = 0;
            for (int j = 0; j < 8; j++) {
                int yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1;   // teskari bogʻlanish
                bayt = (bayt << 1) | (holat & 1);
                holat = (holat >> 1) | (yangi << 15);
            }
            natija[i] = bayt;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int holat = Integer.parseInt(in.nextLine().trim(), 16);
        String xabar = in.nextLine();
        int[] oqim = kalitOqimi(holat, xabar.length());
        StringBuilder k = new StringBuilder(), s = new StringBuilder(), o = new StringBuilder();
        for (int i = 0; i < xabar.length(); i++) {
            int c = xabar.charAt(i) ^ oqim[i];
            k.append(i > 0 ? " " : "").append(String.format("%02x", oqim[i]));
            s.append(i > 0 ? " " : "").append(String.format("%02x", c));
            o.append((char) (c ^ oqim[i]));                        // XOR yana — asl matn
        }
        System.out.println("kalit oqimi: " + k);
        System.out.println("shifrmatn: " + s);
        System.out.println("ochilgan: " + o);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

vector<int> kalitOqimi(unsigned holat, size_t n) {
    vector<int> natija;
    for (size_t i = 0; i < n; i++) {
        int bayt = 0;
        for (int j = 0; j < 8; j++) {
            unsigned yangi = (holat ^ (holat >> 2) ^ (holat >> 3) ^ (holat >> 5)) & 1;   // teskari bogʻlanish
            bayt = (bayt << 1) | (holat & 1);
            holat = (holat >> 1) | (yangi << 15);
        }
        natija.push_back(bayt);
    }
    return natija;
}

int main() {
    string satr, xabar;
    getline(cin, satr);
    getline(cin, xabar);
    unsigned holat = stoul(satr, nullptr, 16);
    vector<int> oqim = kalitOqimi(holat, xabar.size());
    string k, s, o;
    char buf[4];
    for (size_t i = 0; i < xabar.size(); i++) {
        int c = (unsigned char) xabar[i] ^ oqim[i];
        snprintf(buf, sizeof buf, "%02x", oqim[i]);
        k += (i ? " " : "") + string(buf);
        snprintf(buf, sizeof buf, "%02x", c);
        s += (i ? " " : "") + string(buf);
        o += (char) (c ^ oqim[i]);                                // XOR yana — asl matn
    }
    cout << "kalit oqimi: " << k << endl;
    cout << "shifrmatn: " << s << endl;
    cout << "ochilgan: " << o << endl;
    return 0;
}
```

Kirish:

```text
ACE1
SALOM DUNYO
```

Chiqish:

```text
kalit oqimi: 87 35 44 e2 ec 23 b9 c7 a8 11 4a
shifrmatn: d4 74 08 ad a1 03 fd 92 e6 48 05
ochilgan: SALOM DUNYO
```

Kalit — atigi 16 bitli boshlangʻich holat (65 535 variant) va LFSR chiziqli: hujumchi bir nechta maʼlum harfdan (masalan, "SALOM") kalit oqimini topib, registrni tiklaydi. Bu misol oqimli shifrning **gʻoyasini** koʻrsatadi; amalda ChaCha20 yoki AES-CTR ishlatiladi.

## Ishlangan misol 2: ECB va CBC farqi

Oʻquv maqsadida 8 bitli "blok shifr" olamiz: *E*(*b*) = (*b* ⊕ *k*) ni chapga 3 bitga aylantirish. Uni ECB va CBC rejimlarida qoʻllab, takrorlanuvchi matn uchun natijalarni solishtiring. Kirish: kalit va IV (oʻn oltilik), ochiq matn.

```python
def shifr(b, k):
    x = b ^ k
    return ((x << 3) | (x >> 5)) & 0xFF         # chapga 3 bit aylantirish


k = int(input().strip(), 16)
iv = int(input().strip(), 16)
matn = input()
ecb = [shifr(ord(c), k) for c in matn]          # har bir blok alohida
cbc = []
oldingi = iv
for c in matn:
    oldingi = shifr(ord(c) ^ oldingi, k)         # oldingi shifrmatn bilan zanjir
    cbc.append(oldingi)
print("ECB: " + " ".join(format(x, "02x") for x in ecb))
print("CBC: " + " ".join(format(x, "02x") for x in cbc))
```
```java
import java.util.Scanner;

public class Main {
    static int shifr(int b, int k) {
        int x = b ^ k;
        return ((x << 3) | (x >> 5)) & 0xFF;                      // chapga 3 bit aylantirish
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = Integer.parseInt(in.nextLine().trim(), 16);
        int iv = Integer.parseInt(in.nextLine().trim(), 16);
        String matn = in.nextLine();
        StringBuilder ecb = new StringBuilder(), cbc = new StringBuilder();
        int oldingi = iv;
        for (int i = 0; i < matn.length(); i++) {
            int c = matn.charAt(i);
            ecb.append(i > 0 ? " " : "").append(String.format("%02x", shifr(c, k)));   // har bir blok alohida
            oldingi = shifr(c ^ oldingi, k);                       // oldingi shifrmatn bilan zanjir
            cbc.append(i > 0 ? " " : "").append(String.format("%02x", oldingi));
        }
        System.out.println("ECB: " + ecb);
        System.out.println("CBC: " + cbc);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int shifr(int b, int k) {
    int x = b ^ k;
    return ((x << 3) | (x >> 5)) & 0xFF;                          // chapga 3 bit aylantirish
}

string hex2(int x) {
    char buf[4];
    snprintf(buf, sizeof buf, "%02x", x);
    return buf;
}

int main() {
    string satr, matn;
    getline(cin, satr);
    int k = stoi(satr, nullptr, 16);
    getline(cin, satr);
    int iv = stoi(satr, nullptr, 16);
    getline(cin, matn);
    string ecb, cbc;
    int oldingi = iv;
    for (size_t i = 0; i < matn.size(); i++) {
        int c = (unsigned char) matn[i];
        ecb += (i ? " " : "") + hex2(shifr(c, k));               // har bir blok alohida
        oldingi = shifr(c ^ oldingi, k);                          // oldingi shifrmatn bilan zanjir
        cbc += (i ? " " : "") + hex2(oldingi);
    }
    cout << "ECB: " << ecb << endl;
    cout << "CBC: " << cbc << endl;
    return 0;
}
```

Kirish:

```text
5C
37
AAAABBBB
```

Chiqish:

```text
ECB: e8 e8 e8 e8 f0 f0 f0 f0
CBC: 51 62 fb 37 49 ba 25 d9
```

ECB da toʻrtta `A` toʻrtta bir xil `e8` ga aylandi — hujumchi kalitni bilmasa ham matn tuzilishini koʻradi (mashhur misol — ECB bilan shifrlangan pingvin rasmi, unda rasm konturi koʻrinib turadi). CBC da har bir blok oldingisiga bogʻliq, takrorlar yashiringan. Haqiqiy AES da xuddi shu hodisa 16 baytli bloklar darajasida roʻy beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **ECB rejimidan foydalanish.** Bir xil bloklar bir xil shifrmatn beradi — tuzilish oshkor boʻladi.
- **IV yoki CTR hisoblagichini takrorlash.** Oqim va CTR rejimlarida bu kalit oqimini qayta ishlatish bilan teng — matnlar ochiladi.
- **Faqat shifrlab, yaxlitlikni tekshirmaslik.** CBC va CTR maʼlumot oʻzgartirilganini aniqlamaydi; GCM kabi autentifikatsiyalangan rejim yoki alohida MAC kerak.
- **Eskirgan algoritmlar (DES, RC4, A5/1).** Ular amalda buzilgan; AES-GCM yoki ChaCha20-Poly1305 ishlating.
- **Kalitni dastur kodiga yozib qoʻyish.** Kalitlar alohida, himoyalangan joyda (kalitlar ombori, HSM) saqlanadi.
:::

:::tip
Onlayn CyberChef vositasida (gchq.github.io/CyberChef) "AES Encrypt" retsepti bilan bir xil 16 baytli bloklardan iborat matnni ECB va CBC rejimlarida shifrlang va natijalarni solishtiring. Keyin CBC da IV ni oʻzgartirib, shifrmatn butunlay oʻzgarishini kuzating.
:::

## Xulosa

- Yaxshi shifr chalkashtirish va tarqatishni taʼminlaydi; koʻchki effekti — bir bit oʻzgarishi shifrmatnning yarmini oʻzgartiradi.
- Oqimli shifr: *C* = *M* ⊕ kalit oqimi; oqimni takrorlamaslik shart; LFSR chiziqli, A5/1 buzilgan, bugun — ChaCha20.
- AES: 128 bitli blok, 128/192/256 bitli kalit, 10/12/14 raund; SubBytes, ShiftRows, MixColumns, AddRoundKey.
- Rejimlar: ECB (ishlatmang), CBC (tasodifiy IV), CTR (takrorlanmas hisoblagich), GCM (maxfiylik + yaxlitlik).

## Oʻzingizni tekshiring

:::check
Q: Simmetrik shifrlashning asosiy xususiyati nima?
A: Shifrlash va deshifrlashda bitta umumiy maxfiy kalit ishlatiladi.
Q: AES blok uzunligi necha bit va qaysi kalit uzunliklarini qoʻllaydi?
A: Blok 128 bit; kalit 128, 192 yoki 256 bit.
Q: A5/1 qanday turdagi shifr?
A: Uchta LFSR asosidagi oqimli shifr (GSM).
Q: Nima uchun ECB rejimi tavsiya etilmaydi?
A: Bir xil ochiq matn bloklari bir xil shifrmatn bloklarini beradi va maʼlumot tuzilishi oshkor boʻladi.
Q: GCM rejimi CTR dan nimasi bilan ustun?
A: U maxfiylikdan tashqari autentifikatsiya tegi orqali yaxlitlikni ham taʼminlaydi.
:::

:::link https://csrc.nist.gov/pubs/fips/197/final
FIPS 197: Advanced Encryption Standard (AES)
AES algoritmining rasmiy standarti (ingliz tilida).
:::
