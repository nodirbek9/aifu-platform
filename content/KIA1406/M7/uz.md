---
title: Ochiq kalitli kriptotizimlar. RSA, modul arifmetikasi, ochiq kalitlar, afzallik va muammolar
summary: Kalit tarqatish muammosi va ochiq kalitli kriptografiya gʻoyasi; bir tomonlama funksiyalar va "tuzoq eshik"; modul arifmetikasi — qoldiqlar, modul boʻyicha daraja, EKUB, teskari element, Eyler funksiyasi va teoremasi; Diffie–Hellman kalit almashinuvi va diskret logarifm; RSA — kalitlar yaratish, shifrlash, deshifrlash, xavfsizligi faktorizatsiya qiyinligiga asoslanishi; elliptik egri chiziqlar (ECC); afzalliklar, muammolar, toʻldirish, kvant tahdidi.
---

## Nima uchun bu mavzu muhim?

Simmetrik shifrlash tez, lekin bitta savol qoladi: hech qachon uchrashmagan ikki tomon — masalan, sizning brauzeringiz va bank serveri — umumiy maxfiy kalitni qanday kelishadi? Kalitni ochiq tarmoq orqali yuborib boʻlmaydi. 1976-yilda Diffie va Hellman, 1977-yilda Rivest, Shamir va Adleman bu muammoni "matematik qulf" yordamida hal qildilar: biror amalni bajarish oson, uni orqaga qaytarish esa maxfiy maʼlumotsiz amalda imkonsiz. Bugun har bir HTTPS ulanish, raqamli imzo va messenjer shu gʻoyaga tayanadi.

:::note
**Bu darsdan soʻng siz:**

- ochiq kalitli kriptografiya qaysi muammoni hal qilishini tushuntira olasiz;
- modul arifmetikasi amallarini (daraja, EKUB, teskari element, Eyler funksiyasi) bajarishni bilasiz;
- Diffie–Hellman kalit almashinuvini bosqichma-bosqich hisoblay olasiz;
- RSA kalitlari qanday yaratilishi va uning xavfsizligi nimaga asoslanishini tushuntira olasiz;
- ochiq kalitli tizimlarning afzallik va muammolarini, kalit uzunligi talablarini bilasiz.
:::

## Gʻoya: ochiq va yopiq kalit

Har bir foydalanuvchida kalitlar **jufti** bor: **ochiq kalit** (hammaga eʼlon qilinadi) va **yopiq kalit** (faqat egasida). Ochiq kalit bilan shifrlangan xabarni faqat yopiq kalit ochadi. Bu "pochta qutisi" ga oʻxshaydi: tirqishga har kim xat tashlay oladi, lekin qutini faqat kalit egasi ochadi.

Asos — **bir tomonlama funksiya**: *f*(*x*) ni hisoblash oson, *x* ni *f*(*x*) dan topish esa qiyin. Masalan, ikkita katta tub sonni koʻpaytirish — millisekundlar; 600 xonali koʻpaytmani tub koʻpaytuvchilarga ajratish — milliardlab yil. **Tuzoq eshikli** funksiyada maxfiy qoʻshimcha maʼlumot (*p* va *q*) boʻlsa, teskari amal ham oson boʻladi.

## Modul arifmetikasi

| Tushuncha | Taʼrif | Misol |
|---|---|---|
| Modul boʻyicha qoldiq | *a* mod *n* — *a* ni *n* ga boʻlgandagi qoldiq | 17 mod 5 = 2 |
| Modul boʻyicha daraja | *a*ᵇ mod *n* | 3⁴ mod 5 = 81 mod 5 = 1 |
| EKUB | eng katta umumiy boʻluvchi; Evklid algoritmi bilan | EKUB(48, 18) = 6 |
| Teskari element | *a*·*x* ≡ 1 (mod *n*); faqat EKUB(*a*, *n*) = 1 boʻlsa bor; kengaytirilgan Evklid algoritmi bilan | 3·7 ≡ 1 (mod 10) |
| Eyler funksiyasi φ(*n*) | 1..*n* oraligʻida *n* bilan oʻzaro tub sonlar soni; *p*, *q* tub boʻlsa φ(*pq*) = (*p* − 1)(*q* − 1) | φ(15) = 2·4 = 8 |
| Eyler teoremasi | EKUB(*a*, *n*) = 1 boʻlsa, *a*^φ(*n*) ≡ 1 (mod *n*) | 2⁸ mod 15 = 1 |

Katta darajalar **kvadratga koʻtarib koʻpaytirish** usuli bilan hisoblanadi: *a*ᵇ uchun *b* ning ikkilik yozuvi boʻyicha har qadamda asos kvadratga oshiriladi va 1 bitlarda natijaga koʻpaytiriladi — 2048 bitli daraja uchun ~2048 qadam, 2²⁰⁴⁸ emas.

## Diffie–Hellman kalit almashinuvi

1. Ochiq parametrlar: katta tub son *p* va generator *g*.
2. Alisa maxfiy *a* ni tanlab, *A* = *g*ᵃ mod *p* ni yuboradi; Bob maxfiy *b* bilan *B* = *g*ᵇ mod *p* ni yuboradi.
3. Alisa *s* = *B*ᵃ mod *p*, Bob *s* = *A*ᵇ mod *p* ni hisoblaydi — ikkalasi ham *g*ᵃᵇ mod *p* ga teng.

Tinglovchi *p*, *g*, *A*, *B* ni koʻradi, lekin *s* ni topish uchun **diskret logarifm** masalasini (*A* dan *a* ni topish) yechishi kerak — katta *p* uchun amalda imkonsiz. Muhim: oddiy DH tomonlarning **haqiqiyligini** tekshirmaydi — "oʻrtadagi odam" hujumiga qarshi u raqamli imzo yoki sertifikatlar bilan birga ishlatiladi (TLS da shunday).

## RSA

**Kalitlar yaratish**:

1. Ikkita katta tub son *p* va *q* tanlanadi (har biri ~1024 bit).
2. *n* = *p*·*q*, φ(*n*) = (*p* − 1)(*q* − 1).
3. Ochiq daraja *e* tanlanadi: EKUB(*e*, φ(*n*)) = 1 (odatda *e* = 65537).
4. Yopiq daraja *d* = *e*⁻¹ mod φ(*n*).
5. Ochiq kalit — (*e*, *n*), yopiq kalit — (*d*, *n*); *p*, *q*, φ(*n*) oʻchiriladi yoki sir saqlanadi.

**Shifrlash**: *c* = *m*ᵉ mod *n*; **deshifrlash**: *m* = *c*ᵈ mod *n* (Eyler teoremasi tufayli toʻgʻri ishlaydi). **Imzo**: *s* = *h*ᵈ mod *n* (xabar xeshi yopiq kalit bilan), tekshirish — *s*ᵉ mod *n* = *h*.

**Xavfsizlik** *n* ni *p* va *q* ga ajratish qiyinligiga asoslangan: kim *p* va *q* ni bilsa, φ(*n*) ni va demak *d* ni hisoblaydi. Bugungi tavsiya — *n* kamida **2048 bit** (3072 bit 2030-yildan keyin). Amalda "sof" RSA ishlatilmaydi: xabar **toʻldiriladi** (OAEP — shifrlash uchun, PSS — imzo uchun), aks holda bir xil xabar bir xil shifrmatn beradi va matematik hujumlar mumkin. RSA bilan kalit yaratish, shifrlash va deshifrlash dasturi — A7 amaliy mashgʻulotining topshirigʻi.

## Elliptik egri chiziqlar (ECC)

ECC xavfsizligi elliptik egri chiziq nuqtalari ustidagi diskret logarifm masalasiga asoslanadi. Uning afzalligi — ancha qisqa kalitlar bilan bir xil bardoshlilik:

| Simmetrik ekvivalent | RSA / DH | ECC |
|---|---|---|
| 80 bit | 1024 bit | 160 bit |
| 128 bit | 3072 bit | 256 bit |
| 256 bit | 15360 bit | 512 bit |

Shuning uchun telefonlar, smart-kartalar va zamonaviy protokollar ECC (ECDHE, ECDSA, Ed25519) ni afzal koʻradi.

## Afzalliklar va muammolar

| Afzalliklar | Muammolar |
|---|---|
| kalit tarqatish muammosini hal qiladi | simmetrik shifrlardan yuzlab–minglab marta sekin |
| raqamli imzo va rad etib boʻlmaslik | uzun kalitlar |
| *n* foydalanuvchi uchun 2*n* kalit (simmetrikda *n*(*n* − 1)/2) | ochiq kalit haqiqiyligini tasdiqlash kerak (PKI, sertifikatlar) |
| | kvant kompyuterlar (Shor algoritmi) RSA va ECC ni buzishi mumkin — postkvant algoritmlarga (ML-KEM, ML-DSA) oʻtish boshlangan |

## Ishlangan misol 1: Diffie–Hellman

Kirish: *p*, *g*, Alisaning maxfiy *a* va Bobning maxfiy *b*. Ochiq qiymatlar *A*, *B* va ikki tomon hisoblagan umumiy kalitni chiqaring. Darajani kvadratga koʻtarib koʻpaytirish usuli bilan hisoblang.

```python
def daraja(asos, kor, modul):
    natija = 1
    asos %= modul
    while kor > 0:
        if kor & 1:                               # ikkilik yozuvdagi 1 bit
            natija = natija * asos % modul
        asos = asos * asos % modul                # kvadratga koʻtarish
        kor >>= 1
    return natija


p, g, a, b = map(int, input().split())
A = daraja(g, a, p)                               # Alisa yuboradi
B = daraja(g, b, p)                               # Bob yuboradi
print("A = " + str(A) + ", B = " + str(B))
print("Alisa: " + str(daraja(B, a, p)) + ", Bob: " + str(daraja(A, b, p)))
```
```java
import java.util.Scanner;

public class Main {
    static long daraja(long asos, long kor, long modul) {
        long natija = 1;
        asos %= modul;
        while (kor > 0) {
            if ((kor & 1) == 1) {                                  // ikkilik yozuvdagi 1 bit
                natija = natija * asos % modul;
            }
            asos = asos * asos % modul;                            // kvadratga koʻtarish
            kor >>= 1;
        }
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long p = in.nextLong(), g = in.nextLong(), a = in.nextLong(), b = in.nextLong();
        long A = daraja(g, a, p);                                  // Alisa yuboradi
        long B = daraja(g, b, p);                                  // Bob yuboradi
        System.out.println("A = " + A + ", B = " + B);
        System.out.println("Alisa: " + daraja(B, a, p) + ", Bob: " + daraja(A, b, p));
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long daraja(long long asos, long long kor, long long modul) {
    long long natija = 1;
    asos %= modul;
    while (kor > 0) {
        if (kor & 1) {                                             // ikkilik yozuvdagi 1 bit
            natija = natija * asos % modul;
        }
        asos = asos * asos % modul;                                // kvadratga koʻtarish
        kor >>= 1;
    }
    return natija;
}

int main() {
    long long p, g, a, b;
    cin >> p >> g >> a >> b;
    long long A = daraja(g, a, p);                                 // Alisa yuboradi
    long long B = daraja(g, b, p);                                 // Bob yuboradi
    cout << "A = " << A << ", B = " << B << endl;
    cout << "Alisa: " << daraja(B, a, p) << ", Bob: " << daraja(A, b, p) << endl;
    return 0;
}
```

Kirish (klassik darslik misoli):

```text
23 5 6 15
```

Chiqish:

```text
A = 8, B = 19
Alisa: 2, Bob: 2
```

Ikkala tomon kalitni hech qachon yubormasdan bir xil `2` ni oldi. *p* = 2147483647 kabi kattaroq son uchun ham dastur bir zumda ishlaydi (koʻpaytmalar 64 bitga sigʻishi uchun *p* < 2³¹ olinadi); haqiqiy tizimlarda *p* 2048 bit va undan katta boʻlib, katta sonlar kutubxonalari ishlatiladi.

## Ishlangan misol 2: kichik RSA modulini buzish

Hujumchi ochiq kalitdagi *n* ni tub koʻpaytuvchilarga ajratsa, yopiq kalitni hisoblay oladi. Har bir satrdagi *n* ni sinash boʻyicha boʻlish (√*n* gacha) bilan ajrating: `n = p * q` yoki `tub`.

```python
import sys

for satr in sys.stdin.read().split():
    n = int(satr)
    d = 2
    topildi = None
    while d * d <= n:                             # √n gacha yetarli
        if n % d == 0:
            topildi = d
            break
        d += 1
    if topildi is None:
        print(str(n) + ": tub")
    else:
        print(str(n) + " = " + str(topildi) + " * " + str(n // topildi))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long n = in.nextLong();
            long topildi = 0;
            for (long d = 2; d * d <= n; d++) {                    // √n gacha yetarli
                if (n % d == 0) {
                    topildi = d;
                    break;
                }
            }
            if (topildi == 0) {
                System.out.println(n + ": tub");
            } else {
                System.out.println(n + " = " + topildi + " * " + n / topildi);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long n;
    while (cin >> n) {
        long long topildi = 0;
        for (long long d = 2; d * d <= n; d++) {                   // √n gacha yetarli
            if (n % d == 0) {
                topildi = d;
                break;
            }
        }
        if (topildi == 0) {
            cout << n << ": tub" << endl;
        } else {
            cout << n << " = " << topildi << " * " << n / topildi << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
3233
1022117
1000003
999962000357
```

Chiqish:

```text
3233 = 53 * 61
1022117 = 1009 * 1013
1000003: tub
999962000357 = 999979 * 999983
```

12 xonali *n* ni ajratish uchun ~10⁶ ta boʻlish yetarli — bir soniyadan kam. Har bir xona qoʻshilganda ish taxminan √10 ≈ 3 baravar oshadi; eng yaxshi algoritmlar (sonlar maydoni elak usuli) bilan ham 2048 bitli (617 xonali) *n* ni ajratish hozircha imkonsiz. RSA xavfsizligi aynan shu farqqa asoslangan.

:::warning
**Koʻp uchraydigan xatolar**

- **Yopiq kalitni ochiq kalit bilan chalkashtirish.** Shifrlash — qabul qiluvchining **ochiq** kaliti bilan; imzolash — yuboruvchining **yopiq** kaliti bilan.
- **Qisqa kalitlar.** 1024 bitli RSA eskirgan; kamida 2048 bit yoki ECC 256 bit.
- **Toʻldirishsiz ("darslik") RSA.** Bir xil xabar bir xil shifrmatn beradi va hujumlarga ochiq; OAEP/PSS ishlating.
- **Katta maʼlumotni RSA bilan shifrlash.** RSA sekin va matn uzunligi cheklangan; u faqat simmetrik kalitni uzatish yoki imzo uchun.
- **DH ni autentifikatsiyasiz ishlatish.** "Oʻrtadagi odam" har bir tomon bilan alohida kalit kelishib, trafikni oʻqiydi.
:::

:::tip
OpenSSL oʻrnatilgan boʻlsa (Git Bash da bor), `openssl genrsa -out kalit.pem 2048` va `openssl rsa -in kalit.pem -text -noout` buyruqlari bilan haqiqiy RSA kalitini yarating va koʻrib chiqing: modulus (*n*), publicExponent (*e* = 65537), privateExponent (*d*), prime1 va prime2 (*p*, *q*). Bu faylni hech kimga koʻrsatmang — u yopiq kalit.
:::

## Xulosa

- Ochiq kalitli kriptografiya kalit tarqatish muammosini hal qiladi va raqamli imzoni imkon beradi; asos — tuzoq eshikli bir tomonlama funksiyalar.
- Modul arifmetikasi: daraja (kvadratga koʻtarib koʻpaytirish), EKUB, teskari element, φ(*n*), Eyler teoremasi.
- Diffie–Hellman: *A* = *g*ᵃ, *B* = *g*ᵇ, umumiy *s* = *g*ᵃᵇ mod *p*; xavfsizligi — diskret logarifm; autentifikatsiya kerak.
- RSA: *n* = *pq*, *ed* ≡ 1 (mod φ), *c* = *m*ᵉ, *m* = *c*ᵈ mod *n*; xavfsizligi — faktorizatsiya qiyinligi; 2048+ bit va toʻldirish.
- ECC — qisqa kalitlar bilan bir xil bardoshlilik; kvant tahdidi postkvant algoritmlarga oʻtishni talab qilmoqda.

## Oʻzingizni tekshiring

:::check
Q: Ochiq kalitli kriptografiya qaysi asosiy muammoni hal qiladi?
A: Hech qachon uchrashmagan tomonlar orasida maxfiy kalitni xavfsiz kelishish (kalit tarqatish) muammosini.
Q: p = 11, q = 13 boʻlsa, n va φ(n) qancha?
A: n = 143, φ(n) = 10 · 12 = 120.
Q: RSA ning xavfsizligi qaysi matematik masalaning qiyinligiga asoslangan?
A: Katta sonni tub koʻpaytuvchilarga ajratish (faktorizatsiya).
Q: Diffie–Hellman da tinglovchi nimalarni koʻradi va nima uchun kalitni topa olmaydi?
A: p, g, A va B ni; a yoki b ni topish uchun diskret logarifm masalasini yechishi kerak.
Q: Nima uchun 256 bitli ECC kaliti 3072 bitli RSA ga teng hisoblanadi?
A: Elliptik egri chiziqlardagi diskret logarifm uchun subeksponensial algoritmlar maʼlum emas, shuning uchun qisqaroq kalit bilan bir xil bardoshlilikka erishiladi.
:::

:::link https://csrc.nist.gov/projects/post-quantum-cryptography
NIST Post-Quantum Cryptography
Kvant kompyuterlarga bardoshli algoritmlarni standartlashtirish loyihasi (ingliz tilida).
:::
