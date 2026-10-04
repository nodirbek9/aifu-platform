---
title: Algoritmlarning murakkabligi. Izlash algoritmlari
summary: Vaqt va xotira murakkabligi, amallarni sanash, O, Ω va Θ belgilari, murakkablik sinflari (P va NP haqida tushuncha), rekursiv algoritmlar murakkabligi va rekurrent munosabatlar; chiziqli, binar va interpolyatsion izlash hamda ularning murakkabligi.
---

## Nima uchun bu mavzu muhim?

Ikki dasturchi bitta masalani yechdi: birining dasturi 1 000 ta yozuvda 0,01 soniya, ikkinchisiniki 0,02 soniya ishlaydi. Qaysi biri yaxshi? Javob yozuvlar soni oshganda nima boʻlishiga bogʻliq: 1 000 000 ta yozuvda birinchisi 3 soatga, ikkinchisi esa 0,5 soniyaga choʻzilishi mumkin. Algoritmlarni **soniyalar bilan emas, oʻsish tezligi bilan** solishtirish aynan shuning uchun kerak.

Bu maʼruzada algoritm murakkabligini qanday baholash, rekursiv algoritmlarning murakkabligini qanday topish va eng koʻp ishlatiladigan izlash algoritmlari — chiziqli, binar va interpolyatsion izlash — qanchalik tez ishlashini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- vaqt va xotira murakkabligini amallar sonini sanash orqali baholay olasiz;
- O, Ω va Θ belgilarining maʼnosini tushuntira olasiz;
- asosiy murakkablik sinflarini va P hamda NP tushunchalarini farqlay olasiz;
- oddiy rekurrent munosabatlardan rekursiv algoritm murakkabligini topa olasiz;
- chiziqli, binar va interpolyatsion izlashni yozib, ularning murakkabligini solishtira olasiz.
:::

## Vaqt va xotira murakkabligi

**Vaqt murakkabligi** — kirish oʻlchami *n* ga qarab algoritm bajaradigan asosiy amallar soni *T*(*n*). **Xotira murakkabligi** — algoritmga kerak boʻladigan qoʻshimcha xotira *S*(*n*). Soniyalar kompyuterga bogʻliq, amallar soni esa yoʻq — shuning uchun amallar sanaladi.

Odatda **eng yomon holat** baholanadi: algoritm qaysi kirishda eng koʻp ishlaydi. Baʼzan **oʻrtacha holat** ham muhim (masalan, tez saralash va interpolyatsion izlashda).

| Belgi | Maʼnosi | Oʻqilishi |
|---|---|---|
| *T*(*n*) = *O*(*f*(*n*)) | yuqoridan chegara: yetarlicha katta *n* da *T*(*n*) ≤ *c* · *f*(*n*) | „koʻpi bilan *f* tartibida“ |
| *T*(*n*) = Ω(*f*(*n*)) | pastdan chegara: *T*(*n*) ≥ *c* · *f*(*n*) | „kamida *f* tartibida“ |
| *T*(*n*) = Θ(*f*(*n*)) | ikki tomondan chegara | „aynan *f* tartibida“ |

Hisoblash qoidalari: oʻzgarmas koʻpaytuvchilar tashlanadi (3*n* → *O*(*n*)), eng tez oʻsuvchi had qoladi (*n*² + 100*n* → *O*(*n*²)), ketma-ket qismlar qoʻshiladi, ichma-ich sikllar koʻpaytiriladi.

## Murakkablik sinflari

| Sinf | *n* = 10⁶ da amallar | Misol |
|---|---|---|
| *O*(1) | 1 | massiv elementiga murojaat |
| *O*(log *n*) | ~20 | binar izlash |
| *O*(*n*) | 10⁶ | chiziqli izlash |
| *O*(*n* log *n*) | ~2 · 10⁷ | birlashtirib saralash |
| *O*(*n*²) | 10¹² | pufaksimon saralash |
| *O*(2ⁿ) | hisoblab boʻlmaydi | barcha qism toʻplamlarni sanash |

Zamonaviy kompyuter soniyasiga ~10⁸–10⁹ oddiy amal bajaradi. Demak, *n* = 10⁶ da *O*(*n* log *n*) — bir zumda, *O*(*n*²) — bir necha soat.

**P sinfi** — polinomial vaqtda (*O*(*n*ᵏ)) yechiladigan masalalar. **NP sinfi** — yechimi berilsa, uni polinomial vaqtda **tekshirish** mumkin boʻlgan masalalar (masalan, sudoku: yechishi qiyin, tekshirishi oson). P = NP mi? — informatikaning eng mashhur ochiq savoli; uning isboti uchun Kley instituti 1 million dollar mukofot taʼsis etgan.

## Rekursiv algoritmlar murakkabligi

Rekursiv algoritm vaqti **rekurrent munosabat** bilan yoziladi: katta masala vaqti kichik masalalar vaqti orqali.

| Rekurrent munosabat | Algoritm | Yechimi |
|---|---|---|
| *T*(*n*) = *T*(*n* − 1) + *O*(1) | rekursiv faktorial, yigʻindi | *O*(*n*) |
| *T*(*n*) = *T*(*n*/2) + *O*(1) | binar izlash | *O*(log *n*) |
| *T*(*n*) = 2*T*(*n*/2) + *O*(*n*) | birlashtirib saralash | *O*(*n* log *n*) |
| *T*(*n*) = *T*(*n* − 1) + *T*(*n* − 2) + *O*(1) | oddiy rekursiv Fibonachchi | *O*(φⁿ) ≈ *O*(1,618ⁿ) |

Masalan, binar izlashda har qadam qidiruv oraligʻini ikki baravar qisqartiradi: *n* → *n*/2 → *n*/4 → … → 1. Bu log₂ *n* qadam.

## Izlash algoritmlari

| Algoritm | Shart | Eng yomon holat | Oʻrtacha |
|---|---|---|---|
| Chiziqli | yoʻq | *O*(*n*) | *O*(*n*) |
| Binar | massiv saralangan | *O*(log *n*) | *O*(log *n*) |
| Interpolyatsion | saralangan, qiymatlar **tekis taqsimlangan** | *O*(*n*) | *O*(log log *n*) |

**Interpolyatsion izlash** — telefon kitobidan ismni qidirishga oʻxshaydi: „Yoʻldoshev“ ni qidirganda kitobni oʻrtasidan emas, oxiriga yaqin joydan ochamiz. Keyingi tekshiriladigan oʻrin chiziqli interpolyatsiya bilan hisoblanadi:

```text
oʻrin = chap + (x − a[chap]) · (oʻng − chap) / (a[oʻng] − a[chap])
```

Qiymatlar tekis taqsimlangan boʻlsa (masalan, 10, 20, 30, …), oʻrin deyarli darhol topiladi. Notekis taqsimotda (1, 2, 4, 8, …) esa u binar izlashdan sekinroq boʻlishi mumkin.

## Ishlangan misol 1: chiziqli va binar izlash taqqoslash sonlari

*n* va *x* kiritiladi. Massiv `a[i] = 2i + 1` (birinchi *n* ta toq son, oʻsish tartibida). *x* ni chiziqli va binar izlash bilan qidirib, har biri nechta taqqoslash bajarganini bitta satrda chiqaring.

```python
n, x = map(int, input().split())


def a(i):                           # massivni xotirada saqlamaymiz
    return 2 * i + 1


chiziqli = 0
for i in range(n):
    chiziqli += 1
    if a(i) == x:
        break

binar = 0
chap, ong = 0, n - 1
while chap <= ong:
    binar += 1
    orta = (chap + ong) // 2
    if a(orta) == x:
        break
    if a(orta) < x:
        chap = orta + 1
    else:
        ong = orta - 1

print(chiziqli, binar)
```
```java
import java.util.Scanner;

public class Main {
    static long a(long i) {                 // massivni xotirada saqlamaymiz
        return 2 * i + 1;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long n = in.nextLong(), x = in.nextLong();

        long chiziqli = 0;
        for (long i = 0; i < n; i++) {
            chiziqli++;
            if (a(i) == x) break;
        }

        long binar = 0, chap = 0, ong = n - 1;
        while (chap <= ong) {
            binar++;
            long orta = (chap + ong) / 2;
            if (a(orta) == x) break;
            if (a(orta) < x) chap = orta + 1;
            else ong = orta - 1;
        }

        System.out.println(chiziqli + " " + binar);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long a(long long i) { return 2 * i + 1; }   // massivni xotirada saqlamaymiz

int main() {
    long long n, x;
    cin >> n >> x;

    long long chiziqli = 0;
    for (long long i = 0; i < n; i++) {
        chiziqli++;
        if (a(i) == x) break;
    }

    long long binar = 0, chap = 0, ong = n - 1;
    while (chap <= ong) {
        binar++;
        long long orta = (chap + ong) / 2;
        if (a(orta) == x) break;
        if (a(orta) < x) chap = orta + 1;
        else ong = orta - 1;
    }

    cout << chiziqli << " " << binar << endl;
    return 0;
}
```

| Kirish | Chiziqli | Binar | Izoh |
|---|---|---|---|
| `1000000 1` | 1 | 19 | birinchi element — chiziqli uchun eng yaxshi holat |
| `1000000 1234567` | 617 284 | 20 | odatiy holat |
| `1000000 4` | 1 000 000 | 20 | juft son yoʻq — ikkalasi uchun ham eng yomon holat |

Million elementda binar izlash koʻpi bilan 20 ta taqqoslash bajaradi (2²⁰ ≈ 10⁶), chiziqli esa million tagacha. Ammo birinchi qatorda chiziqli izlash yutdi: murakkablik **eng yomon** yoki **oʻrtacha** holatni tavsiflaydi, har bir alohida kirishni emas.

## Ishlangan misol 2: interpolyatsion izlash

Birinchi satrda *n*, ikkinchisida oʻsish tartibidagi *n* ta butun son, uchinchisida *x*. *x* ning oʻrnini (1 dan; topilmasa −1) interpolyatsion izlash bilan toping; oʻrin va interpolyatsion hamda binar izlash bajargan qadamlar sonini chiqaring.

```python
def interpolyatsion(a, x):
    chap, ong = 0, len(a) - 1
    qadam = 0
    while chap <= ong and a[chap] <= x <= a[ong]:
        qadam += 1
        if a[ong] == a[chap]:
            orin = chap
        else:
            orin = chap + (x - a[chap]) * (ong - chap) // (a[ong] - a[chap])
        if a[orin] == x:
            return orin + 1, qadam
        if a[orin] < x:
            chap = orin + 1
        else:
            ong = orin - 1
    return -1, qadam


def binar(a, x):
    chap, ong = 0, len(a) - 1
    qadam = 0
    while chap <= ong:
        qadam += 1
        orta = (chap + ong) // 2
        if a[orta] == x:
            return orta + 1, qadam
        if a[orta] < x:
            chap = orta + 1
        else:
            ong = orta - 1
    return -1, qadam


n = int(input())
a = list(map(int, input().split()))
x = int(input())
orin, q1 = interpolyatsion(a, x)
_, q2 = binar(a, x)
print(orin, q1, q2)
```
```java
import java.util.Scanner;

public class Main {
    // natija: {oʻrin (1 dan yoki -1), qadamlar}
    static long[] interpolyatsion(long[] a, long x) {
        int chap = 0, ong = a.length - 1;
        long qadam = 0;
        while (chap <= ong && a[chap] <= x && x <= a[ong]) {
            qadam++;
            int orin = a[ong] == a[chap] ? chap
                    : (int) (chap + (x - a[chap]) * (ong - chap) / (a[ong] - a[chap]));
            if (a[orin] == x) return new long[] {orin + 1, qadam};
            if (a[orin] < x) chap = orin + 1;
            else ong = orin - 1;
        }
        return new long[] {-1, qadam};
    }

    static long[] binar(long[] a, long x) {
        int chap = 0, ong = a.length - 1;
        long qadam = 0;
        while (chap <= ong) {
            qadam++;
            int orta = (chap + ong) / 2;
            if (a[orta] == x) return new long[] {orta + 1, qadam};
            if (a[orta] < x) chap = orta + 1;
            else ong = orta - 1;
        }
        return new long[] {-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = in.nextLong();
        long x = in.nextLong();
        long[] i1 = interpolyatsion(a, x), b1 = binar(a, x);
        System.out.println(i1[0] + " " + i1[1] + " " + b1[1]);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

// natija: {oʻrin (1 dan yoki -1), qadamlar}
pair<long long, long long> interpolyatsion(const vector<long long>& a, long long x) {
    long long chap = 0, ong = (long long) a.size() - 1, qadam = 0;
    while (chap <= ong && a[chap] <= x && x <= a[ong]) {
        qadam++;
        long long orin = a[ong] == a[chap] ? chap
                       : chap + (x - a[chap]) * (ong - chap) / (a[ong] - a[chap]);
        if (a[orin] == x) return {orin + 1, qadam};
        if (a[orin] < x) chap = orin + 1;
        else ong = orin - 1;
    }
    return {-1, qadam};
}

pair<long long, long long> binar(const vector<long long>& a, long long x) {
    long long chap = 0, ong = (long long) a.size() - 1, qadam = 0;
    while (chap <= ong) {
        qadam++;
        long long orta = (chap + ong) / 2;
        if (a[orta] == x) return {orta + 1, qadam};
        if (a[orta] < x) chap = orta + 1;
        else ong = orta - 1;
    }
    return {-1, qadam};
}

int main() {
    int n;
    cin >> n;
    vector<long long> a(n);
    for (auto& v : a) cin >> v;
    long long x;
    cin >> x;
    auto [orin, q1] = interpolyatsion(a, x);
    auto [_, q2] = binar(a, x);
    cout << orin << " " << q1 << " " << q2 << endl;
    return 0;
}
```

| Massiv | *x* | Natija | Interpolyatsion | Binar |
|---|---|---|---|---|
| `10 20 30 … 100` | 70 | 7 | 1 qadam | 4 qadam |
| `10 20 30 … 100` | 75 | −1 | 1 qadam | 4 qadam |
| `1 2 4 8 … 512` | 256 | 9 | 4 qadam | 3 qadam |

Tekis taqsimlangan massivda interpolyatsion izlash oʻrinni **birinchi urinishda** topdi. Ikki baravar oʻsuvchi sonlarda esa formula kichik qiymatlar tomonga „egilib“, binar izlashdan koʻproq qadam talab qildi. `a[ong] == a[chap]` tekshiruvi nolga boʻlishning oldini oladi. Koʻpaytma `(x − a[chap]) · (ong − chap)` katta boʻlishi mumkin, shuning uchun Java va C++'da `long` ishlatildi.

:::warning
**Koʻp uchraydigan xatolar**

- **Oʻzgarmas koʻpaytuvchilarga ortiqcha eʼtibor.** 2*n* va 100*n* — ikkalasi ham *O*(*n*); katta *n* da *n*² ga nisbatan farqi ahamiyatsiz.
- **Saralanmagan massivda binar yoki interpolyatsion izlash.** Natija notoʻgʻri; avval saralash (*O*(*n* log *n*)) kerak.
- **Interpolyatsion izlashda nolga boʻlish.** `a[ong] == a[chap]` holati alohida tekshiriladi.
- **Eng yaxshi holatni algoritm bahosi deb olish.** Chiziqli izlash birinchi elementni bir qadamda topadi, lekin uning murakkabligi baribir *O*(*n*).
:::

:::tip
Algoritm murakkabligini tekshirishning amaliy usuli: *n* ni ikki baravar oshirib, vaqt (yoki amallar soni) qanday oʻzgarishini kuzating. *O*(*n*) da ikki baravar, *O*(*n*²) da toʻrt baravar, *O*(log *n*) da esa bitta qadamga oshadi.
:::

## Xulosa

- Murakkablik — kirish oʻlchami oshganda amallar sonining oʻsish tezligi; odatda eng yomon holat baholanadi.
- *O* — yuqoridan, Ω — pastdan, Θ — ikki tomondan chegara.
- Asosiy sinflar: *O*(1), *O*(log *n*), *O*(*n*), *O*(*n* log *n*), *O*(*n*²), *O*(2ⁿ); P va NP — polinomial yechiladigan va polinomial tekshiriladigan masalalar.
- Rekursiv algoritm murakkabligi rekurrent munosabatdan topiladi.
- Chiziqli izlash *O*(*n*), binar *O*(log *n*), interpolyatsion oʻrtacha *O*(log log *n*) (tekis taqsimotda).

## Oʻzingizni tekshiring

:::check
Q: *T*(*n*) = 5*n*² + 3*n* + 100 algoritmining murakkabligi qanday?
A: *O*(*n*²) — eng tez oʻsuvchi had qoladi, koʻpaytuvchi tashlanadi.
Q: 1 000 000 elementli saralangan massivda binar izlash koʻpi bilan nechta taqqoslash bajaradi?
A: Taxminan 20 ta, chunki 2²⁰ ≈ 10⁶.
Q: *T*(*n*) = *T*(*n*/2) + *O*(1) qaysi murakkablikka teng?
A: *O*(log *n*).
Q: Interpolyatsion izlash qachon binar izlashdan tez ishlaydi?
A: Massiv saralangan va qiymatlar tekis taqsimlangan boʻlganda.
Q: NP sinfidagi masalaning asosiy belgisi nima?
A: Taklif qilingan yechimni polinomial vaqtda tekshirish mumkin.
:::

:::link https://www.bigocheatsheet.com/
Big-O cheat sheet
Asosiy algoritm va tuzilmalarning murakkablik jadvali (ingliz tilida).
:::

:::link https://en.wikipedia.org/wiki/Interpolation_search
Interpolyatsion izlash
Algoritm, uning murakkabligi va binar izlash bilan taqqoslash (ingliz tilida).
:::
