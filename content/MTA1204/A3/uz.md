---
title: Izlash algoritmlari. Binar, interpolyatsion va Fibonachchi izlash. Xeshlash orqali izlash (amaliy mashgʻulot)
summary: Binar izlashning amaliy qoʻllanishlari — javob boʻyicha izlash (butun kvadrat ildiz), haqiqiy sonlarda bisektsiya usuli; xesh-toʻplam bilan juftlik izlash va uning O(n²) dan O(n) ga tezlashuvi; izlash usulini tanlash.
---

## Nima uchun bu mavzu muhim?

Maʼruzalarda izlash algoritmlarini massivda element topish nuqtai nazaridan koʻrdik. Amaliyotda esa binar izlashning eng kuchli qoʻllanishi — massivda emas, **javoblar oraligʻida** izlash: „shart bajariladigan eng katta son qaysi?“ Xeshlash esa koʻp masalalarda ichma-ich siklni bitta siklga aylantiradi.

Bu mashgʻulotda uchta amaliy masalani yechamiz: katta sonning butun kvadrat ildizi (javob boʻyicha binar izlash), tenglamaning ildizi (bisektsiya) va yigʻindisi berilgan son boʻlgan juftlik (xesh-toʻplam).

:::note
**Bu mashgʻulotdan soʻng siz:**

- binar izlashni „javob boʻyicha“ — monoton shartli oraliqda qoʻllay olasiz;
- haqiqiy sonlar uchun bisektsiya usuli bilan tenglamani yecha olasiz;
- xesh-toʻplam yordamida juftlik izlashni *O*(*n*) da bajara olasiz;
- masala shartiga qarab mos izlash usulini tanlay olasiz.
:::

## Javob boʻyicha binar izlash

Binar izlash nafaqat saralangan massivda ishlaydi. Agar savol „*P*(*x*) shart bajariladigan eng katta *x* qaysi?“ boʻlsa va *P* **monoton** boʻlsa (biror chegaragacha rost, undan keyin yolgʻon), javobni oraliqni ikkiga boʻlib topish mumkin:

```text
chap = eng kichik mumkin javob, ong = eng katta mumkin javob
toki chap < ong:
    orta = (chap + ong + 1) / 2        ← yuqoriga yaxlitlash
    agar P(orta): chap = orta          (javob orta yoki undan katta)
    aks holda:    ong = orta − 1
javob: chap
```

`+ 1` muhim: usiz `chap = orta` qadami oraliqni qisqartirmay, cheksiz siklga olib kelishi mumkin. Javoblar oraligʻi 0 dan 2 · 10⁹ gacha boʻlsa, ~31 qadam yetadi.

## Bisektsiya usuli

Haqiqiy sonlarda xuddi shu gʻoya **bisektsiya** (kesmani teng ikkiga boʻlish) deyiladi: *f*(*x*) uzluksiz va [*a*, *b*] uchlarida turli ishorali boʻlsa, oraliqni ikkiga boʻlib, ildiz yotgan yarmini tanlaymiz. Har qadamda xatolik ikki baravar kamayadi: 100 qadamdan keyin xatolik dastlabki uzunlikning 2⁻¹⁰⁰ qismi — `double` aniqligidan ham kichik.

## Xesh bilan izlash

„Massivda yigʻindisi *t* boʻlgan juftlik bormi?“ — toʻgʻridan-toʻgʻri yechim barcha juftliklarni tekshiradi: *O*(*n*²). Xesh-toʻplam bilan: har bir *x* uchun „*t* − *x* avval uchraganmi?“ savoli *O*(1) da javob topadi, jami *O*(*n*). Bu „toʻldiruvchini xeshda izlash“ usuli juda koʻp masalada ishlaydi.

| Masala | Mos usul |
|---|---|
| saralanmagan massivda bir martalik izlash | chiziqli |
| saralangan massivda koʻp izlash | binar |
| tekis taqsimlangan saralangan maʼlumot | interpolyatsion |
| kalit boʻyicha juda koʻp izlash, tartib kerak emas | xesh-jadval |
| monoton shart boʻyicha eng katta/kichik qiymat | javob boʻyicha binar izlash |

## Ishlangan misol 1: butun kvadrat ildiz

*n* kiritiladi (0 ≤ *n* ≤ 10¹⁸). *x*² ≤ *n* boʻladigan eng katta butun *x* ni va binar izlash qadamlari sonini chiqaring. Haqiqiy sonli `sqrt` dan foydalanmang — katta *n* da u yaxlitlash xatosi beradi.

```python
n = int(input())
chap, ong = 0, 2 * 10**9                # javob shu oraliqda
qadam = 0
while chap < ong:
    qadam += 1
    orta = (chap + ong + 1) // 2        # yuqoriga yaxlitlash
    if orta * orta <= n:                # shart rost — javob orta yoki kattaroq
        chap = orta
    else:
        ong = orta - 1
print(chap, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long chap = 0, ong = 2_000_000_000L;        // javob shu oraliqda
        int qadam = 0;
        while (chap < ong) {
            qadam++;
            long orta = (chap + ong + 1) / 2;       // yuqoriga yaxlitlash
            if (orta * orta <= n) chap = orta;      // shart rost — javob orta yoki kattaroq
            else ong = orta - 1;
        }
        System.out.println(chap + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long chap = 0, ong = 2000000000LL;         // javob shu oraliqda
    int qadam = 0;
    while (chap < ong) {
        qadam++;
        long long orta = (chap + ong + 1) / 2;      // yuqoriga yaxlitlash
        if (orta * orta <= n) chap = orta;          // shart rost — javob orta yoki kattaroq
        else ong = orta - 1;
    }
    cout << chap << " " << qadam << endl;
    return 0;
}
```

| *n* | Natija | Qadamlar |
|---|---|---|
| 10 | 3 | 31 |
| 10¹⁸ | 1 000 000 000 | 30 |
| 999 999 999 999 999 999 | 999 999 999 | 31 |

Oxirgi qatorda `sqrt` 10⁹ ni qaytarishi mumkin edi (haqiqiy sonning yaxlitlanishi tufayli), binar izlash esa aniq javob beradi. `orta * orta` ≤ 4 · 10¹⁸ — `long` (`long long`) ga sigʻadi; yuqori chegarani 2 · 10⁹ dan katta olsangiz, koʻpaytma toʻlib ketishi mumkin.

## Ishlangan misol 2: bisektsiya — tenglama ildizi

*c* > 0 kiritiladi. *x*³ + *x* = *c* tenglamaning musbat ildizini bisektsiya usuli bilan 6 kasr xona aniqlikda toping. *f*(*x*) = *x*³ + *x* oʻsuvchi, demak ildiz yagona va [0, max(1, *c*)] oraliqda yotadi.

```python
c = float(input())
chap, ong = 0.0, max(1.0, c)            # f(chap) < c <= f(ong)
for _ in range(100):                    # har qadam xatolikni 2 baravar kamaytiradi
    orta = (chap + ong) / 2
    if orta**3 + orta < c:
        chap = orta
    else:
        ong = orta
print(f"{chap:.6f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        double c = new Scanner(System.in).nextDouble();
        double chap = 0.0, ong = Math.max(1.0, c);  // f(chap) < c <= f(ong)
        for (int k = 0; k < 100; k++) {             // har qadam xatolikni 2 baravar kamaytiradi
            double orta = (chap + ong) / 2;
            if (orta * orta * orta + orta < c) chap = orta;
            else ong = orta;
        }
        System.out.printf("%.6f%n", chap);
    }
}
```
```cpp
#include <algorithm>
#include <cstdio>
#include <iostream>
using namespace std;

int main() {
    double c;
    cin >> c;
    double chap = 0.0, ong = max(1.0, c);           // f(chap) < c <= f(ong)
    for (int k = 0; k < 100; k++) {                 // har qadam xatolikni 2 baravar kamaytiradi
        double orta = (chap + ong) / 2;
        if (orta * orta * orta + orta < c) chap = orta;
        else ong = orta;
    }
    printf("%.6f\n", chap);
    return 0;
}
```

`10` uchun `2.000000` (2³ + 2 = 10), `1000` uchun `9.966667`, `0.5` uchun `0.423854`. Bu yerda „toki oraliq kichik boʻlguncha“ oʻrniga aniq 100 qadam bajarildi: `double` aniqligi chegaralangan, shuning uchun „oraliq < 10⁻¹⁵“ sharti baʼzan hech qachon bajarilmasligi mumkin. Bisektsiya sekin (har qadamda bitta ikkilik raqam), lekin juda ishonchli: ildiz oraliqda boʻlsa, albatta topiladi.

## Ishlangan misol 3: yigʻindisi berilgan juftlik (xesh-toʻplam)

Birinchi satrda *n* va *t*, ikkinchisida *n* ta butun son. Yigʻindisi *t* ga teng boʻlgan, ikkinchi elementi eng chapda turgan juftlikning oʻrinlarini (1 dan) chiqaring; bunday juftlik boʻlmasa `-`.

```python
n, t = map(int, input().split())
a = list(map(int, input().split()))

korilgan = {}                           # qiymat -> birinchi oʻrni
javob = None
for j, x in enumerate(a):
    if t - x in korilgan:               # toʻldiruvchi avval uchraganmi? — O(1)
        javob = (korilgan[t - x] + 1, j + 1)
        break
    if x not in korilgan:
        korilgan[x] = j

print(*javob if javob else "-")
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        long t = in.nextLong();
        Map<Long, Integer> korilgan = new HashMap<>();      // qiymat -> birinchi oʻrni
        String javob = "-";
        for (int j = 0; j < n; j++) {
            long x = in.nextLong();
            Integer i = korilgan.get(t - x);                // toʻldiruvchi avval uchraganmi? — O(1)
            if (i != null) {
                javob = (i + 1) + " " + (j + 1);
                break;
            }
            korilgan.putIfAbsent(x, j);
        }
        System.out.println(javob);
    }
}
```
```cpp
#include <iostream>
#include <unordered_map>
#include <vector>
using namespace std;

int main() {
    int n;
    long long t;
    cin >> n >> t;
    vector<long long> a(n);
    for (auto& v : a) cin >> v;

    unordered_map<long long, int> korilgan;                 // qiymat -> birinchi oʻrni
    for (int j = 0; j < n; j++) {
        auto it = korilgan.find(t - a[j]);                  // toʻldiruvchi avval uchraganmi? — O(1)
        if (it != korilgan.end()) {
            cout << it->second + 1 << " " << j + 1 << endl;
            return 0;
        }
        korilgan.emplace(a[j], j);                          // mavjud boʻlsa qoʻshilmaydi
    }
    cout << "-" << endl;
    return 0;
}
```

`6 12` va `8 3 11 5 7 2` uchun chiqish `4 5` (5 + 7 = 12); `3 10` va `1 2 3` uchun — `-`; `2 8` va `4 4` uchun — `1 2`. Har bir element bir marta koʻriladi va xesh-toʻplamga bitta murojaat qilinadi — *O*(*n*). Ichma-ich sikl bilan 10⁵ elementda 5 · 10⁹ juftlik tekshirilar edi; xesh bilan esa 10⁵ ta amal. Teng elementlar (`4 4`) holati alohida eʼtibor talab qiladi: element oʻzi bilan juftlanmasligi uchun avval tekshiriladi, keyin qoʻshiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Javob boʻyicha binar izlashda `+ 1` ni unutish.** `chap = orta` bilan birga `orta = (chap + ong) / 2` cheksiz siklga olib keladi.
- **Katta sonlarda `sqrt` ga ishonish.** `double` 2⁵³ dan katta butun sonlarni aniq saqlamaydi; butun binar izlash ishlating.
- **Bisektsiyani aniqlik sharti bilan toʻxtatish.** `ong - chap > 1e-15` sharti yaxlitlash tufayli hech qachon yolgʻon boʻlmasligi mumkin; qadamlar sonini belgilang.
- **Xeshda elementni avval qoʻshib, keyin tekshirish.** `t = 2x` boʻlganda element oʻzi bilan juftlanib qoladi.
:::

:::tip
Javob boʻyicha binar izlash yozishdan oldin monoton shartni bitta qatorda yozing: „*x*² ≤ *n*“, „*x*³ + *x* < *c*“. Agar shart *x* oshganda faqat bir marta rostdan yolgʻonga (yoki aksincha) oʻtsa, binar izlash ishlaydi.
:::

## Xulosa

- Binar izlash monoton shartli istalgan oraliqda ishlaydi: javob oraligʻini ikkiga boʻlib, *O*(log oraliq) qadamda topiladi.
- Butun sonlarda `orta = (chap + ong + 1) / 2` va `chap = orta` juftligi cheksiz siklning oldini oladi.
- Bisektsiya haqiqiy tenglamani qatʼiy qadamlar soni bilan ishonchli yechadi.
- Xesh-toʻplam „toʻldiruvchi bormi?“ savoliga *O*(1) da javob berib, juftlik izlashni *O*(*n*²) dan *O*(*n*) ga tushiradi.
- Izlash usuli maʼlumot tuzilishi va savolga qarab tanlanadi.

## Oʻzingizni tekshiring

:::check
Q: Javob boʻyicha binar izlash uchun shart qanday boʻlishi kerak?
A: Monoton: biror chegaragacha rost, undan keyin yolgʻon (yoki aksincha).
Q: 0 dan 10⁹ gacha boʻlgan javoblar oraligʻida binar izlash nechta qadam talab qiladi?
A: Taxminan 30 ta (2³⁰ ≈ 10⁹).
Q: Nima uchun bisektsiyada qadamlar soni oldindan belgilanadi?
A: `double` aniqligi cheklangan; „xatolik juda kichik“ sharti yaxlitlash tufayli hech qachon bajarilmasligi mumkin.
Q: Juftlik izlashda xesh-toʻplam nima uchun tezlashtiradi?
A: Har bir element uchun „*t* − *x* bormi?“ savoli butun massivni aylanmasdan *O*(1) da javob topadi.
:::

:::link https://cp-algorithms.com/num_methods/binary_search.html
CP-Algorithms: binar izlash
Javob boʻyicha binar izlash va uning variantlari (ingliz tilida).
:::
