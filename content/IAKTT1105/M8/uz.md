---
title: Algoritmlashtirish va dasturlash asoslari
summary: Algoritm tushunchasi va xossalari — diskretlik, aniqlik, tushunarlilik, natijaviylik, ommaviylik; algoritmni tasvirlash usullari — soʻz bilan, blok-sxema, psevdokod, dastur; asosiy tuzilmalar — chiziqli, tarmoqlanuvchi, takrorlanuvchi; oʻzgaruvchilar, maʼlumot turlari, amallar; dasturlash tillari va ularning iqtisodiyotdagi qoʻllanishi (Python, VBA, SQL, R, Java, C++); dastur yaratish bosqichlari; ishlangan misollar — zararsizlik nuqtasi (chiziqli va tarmoqlanuvchi) va oylik savdo tahlili (takrorlanuvchi).
---

## Nima uchun bu mavzu muhim?

Har qanday hisob-kitob — ish haqi, soliq, kredit, chegirma, hisobot — aniq qadamlar ketma-ketligi, yaʼni **algoritm**dir. Algoritmik fikrlash iqtisodchiga murakkab masalani bosqichlarga ajratish, xatolarni topish va takroriy ishni avtomatlashtirish (Excel makroslari, Python skriptlari) imkonini beradi. Bugun koʻplab moliya va tahlil vakansiyalarida Python yoki SQL ni bilish talab qilinadi.

:::note
**Bu darsdan soʻng siz:**

- algoritm tushunchasi va xossalarini ayta olasiz;
- algoritmni blok-sxema va psevdokodda tasvirlay olasiz;
- chiziqli, tarmoqlanuvchi va takrorlanuvchi tuzilmalarni farqlay olasiz;
- oʻzgaruvchi, maʼlumot turi va amal tushunchalarini bilasiz;
- oddiy iqtisodiy masalani dasturga aylantirish bosqichlarini bajara olasiz.
:::

## Algoritm va uning xossalari

**Algoritm** — qoʻyilgan masalani yechish uchun bajariladigan aniq va cheklangan amallar ketma-ketligi. Soʻz Muhammad ibn Muso al-Xorazmiy nomidan kelib chiqqan.

| Xossa | Mazmuni |
|---|---|
| **Diskretlik** | alohida qadamlardan iborat |
| **Aniqlik** | har bir qadam bir maʼnoli |
| **Tushunarlilik** | ijrochi (odam yoki kompyuter) bajara oladi |
| **Natijaviylik** | cheklangan qadamdan keyin natija beradi |
| **Ommaviylik** | bir turdagi masalalarning butun sinfi uchun ishlaydi (turli kirish maʼlumotlarida) |

## Tasvirlash usullari

- **Soʻz bilan**: "Summani 12 ga boʻl, natijani yaxlitla…"
- **Blok-sxema** (S8 va MT11 da batafsil): oval — boshlanish/tugash, parallelogramm — kiritish/chiqarish, toʻrtburchak — amal, romb — shart, strelkalar — tartib.
- **Psevdokod**: dasturga yaqin, lekin erkin yozuv.
- **Dastur**: dasturlash tilidagi aniq yozuv.

## Asosiy tuzilmalar

| Tuzilma | Mazmuni | Iqtisodiy misol |
|---|---|---|
| **Chiziqli** | amallar ketma-ket, bir marta | QQS bilan narxni hisoblash |
| **Tarmoqlanuvchi** | shartga qarab turli yoʻl | foyda bormi yoki zarar; soliq imtiyozi bormi |
| **Takrorlanuvchi** (sikl) | amallar bir necha marta | har oy uchun hisob, roʻyxatdagi barcha xodimlar ish haqi |

**Oʻzgaruvchi** — nomlangan xotira joyi (`narx`, `miqdor`); **maʼlumot turlari**: butun son, haqiqiy son, matn, mantiqiy; **amallar**: arifmetik (`+ − * /`), solishtirish (`> < =`), mantiqiy (VA, YOKI, EMAS).

## Dasturlash tillari iqtisodiyotda

| Til | Qoʻllanishi |
|---|---|
| **Python** | maʼlumotlar tahlili (pandas), avtomatlashtirish, mashinali oʻrganish, moliyaviy modellar |
| **VBA** | Excel va Office da makroslar |
| **SQL** | maʼlumotlar bazasidan hisobotlar (M7) |
| **R** | statistika va ekonometrika |
| **Java, C#** | bank va korporativ tizimlar |
| **C++** | yuqori tezlikdagi savdo tizimlari, hisoblash |
| **JavaScript** | veb-ilovalar, internet-doʻkonlar |

**Dastur yaratish bosqichlari**: masalani qoʻyish → matematik model → algoritm (blok-sxema) → dastur yozish → sinash (test maʼlumotlarida) → xatolarni tuzatish → foydalanish va hujjatlashtirish.

## Ishlangan misol 1: zararsizlik nuqtasi

Korxona oyiga doimiy xarajat *F* qiladi (ijara, ish haqi), mahsulot narxi *p*, bir dona uchun oʻzgaruvchan xarajat *v*. Har bir dona *p − v* marja beradi; zararsizlik uchun kamida ⌈*F* / (*p − v*)⌉ dona sotish kerak. Agar *p ≤ v* boʻlsa, sotuv zararni qoplamaydi. Har bir satrda *F p v*.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    doimiy, narx, ozgaruvchan = map(int, satr.split())
    marja = narx - ozgaruvchan                     # bir donadan qoladigan summa
    if marja <= 0:                                  # tarmoqlanish: shart
        print("zarar qoplanmaydi: narx juda past")
    else:
        miqdor = (doimiy + marja - 1) // marja      # yuqoriga yaxlitlangan boʻlinma
        print(f"zararsizlik nuqtasi: {miqdor} dona, tushum {miqdor * narx}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLong()) {
            long doimiy = in.nextLong(), narx = in.nextLong(), ozgaruvchan = in.nextLong();
            long marja = narx - ozgaruvchan;                   // bir donadan qoladigan summa
            if (marja <= 0) {                                   // tarmoqlanish: shart
                System.out.println("zarar qoplanmaydi: narx juda past");
            } else {
                long miqdor = (doimiy + marja - 1) / marja;     // yuqoriga yaxlitlangan boʻlinma
                System.out.println("zararsizlik nuqtasi: " + miqdor + " dona, tushum " + miqdor * narx);
            }
        }
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long doimiy, narx, ozgaruvchan;
    while (cin >> doimiy >> narx >> ozgaruvchan) {
        long long marja = narx - ozgaruvchan;                  // bir donadan qoladigan summa
        if (marja <= 0) {                                       // tarmoqlanish: shart
            cout << "zarar qoplanmaydi: narx juda past" << endl;
        } else {
            long long miqdor = (doimiy + marja - 1) / marja;    // yuqoriga yaxlitlangan boʻlinma
            cout << "zararsizlik nuqtasi: " << miqdor << " dona, tushum " << miqdor * narx << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
12000000 50000 30000
5000000 15000 15000
9100000 40000 25000
```

Chiqish:

```text
zararsizlik nuqtasi: 600 dona, tushum 30000000
zarar qoplanmaydi: narx juda past
zararsizlik nuqtasi: 607 dona, tushum 24280000
```

Uchinchi holatda 9 100 000 / 15 000 = 606,67 — 606 dona yetmaydi, shuning uchun **yuqoriga** yaxlitlanadi: (*F* + *m* − 1) div *m* formulasi butun sonlar bilan aynan shuni beradi. Ikkinchi holatda narx oʻzgaruvchan xarajatga teng — har qancha sotsa ham doimiy xarajat qoplanmaydi.

## Ishlangan misol 2: oylik savdo tahlili

Birinchi son — oylar soni *n*, keyin *n* ta oylik savdo (mln soʻm). Jami, oʻrtacha, eng yaxshi oy va oʻrtachadan yuqori oylar sonini chiqaring.

```python
import sys

t = list(map(int, sys.stdin.read().split()))
n, savdo = t[0], t[1:]
jami, eng = 0, 0
for i in range(n):                               # sikl: har bir oy
    jami += savdo[i]
    if savdo[i] > savdo[eng]:
        eng = i
ortacha = jami / n
yuqori = 0
for x in savdo:                                  # ikkinchi sikl: oʻrtacha bilan solishtirish
    if x > ortacha:
        yuqori += 1
print(f"jami: {jami}")
print(f"oʻrtacha: {ortacha:.1f}")
print(f"eng yaxshi oy: {eng + 1} ({savdo[eng]})")
print(f"oʻrtachadan yuqori oylar: {yuqori}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] savdo = new int[n];
        for (int i = 0; i < n; i++) savdo[i] = in.nextInt();
        long jami = 0;
        int eng = 0;
        for (int i = 0; i < n; i++) {                    // sikl: har bir oy
            jami += savdo[i];
            if (savdo[i] > savdo[eng]) eng = i;
        }
        double ortacha = (double) jami / n;
        int yuqori = 0;
        for (int x : savdo) {                             // ikkinchi sikl: oʻrtacha bilan solishtirish
            if (x > ortacha) yuqori++;
        }
        System.out.println("jami: " + jami);
        System.out.println(String.format("oʻrtacha: %.1f", ortacha));
        System.out.println("eng yaxshi oy: " + (eng + 1) + " (" + savdo[eng] + ")");
        System.out.println("oʻrtachadan yuqori oylar: " + yuqori);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> savdo(n);
    for (int i = 0; i < n; i++) cin >> savdo[i];
    long long jami = 0;
    int eng = 0;
    for (int i = 0; i < n; i++) {                        // sikl: har bir oy
        jami += savdo[i];
        if (savdo[i] > savdo[eng]) eng = i;
    }
    double ortacha = (double) jami / n;
    int yuqori = 0;
    for (int x : savdo) {                                // ikkinchi sikl: oʻrtacha bilan solishtirish
        if (x > ortacha) yuqori++;
    }
    printf("jami: %lld\n", jami);
    printf("oʻrtacha: %.1f\n", ortacha);
    printf("eng yaxshi oy: %d (%d)\n", eng + 1, savdo[eng]);
    printf("oʻrtachadan yuqori oylar: %d\n", yuqori);
    return 0;
}
```

Kirish:

```text
6 120 135 98 160 142 110
```

Chiqish:

```text
jami: 765
oʻrtacha: 127.5
eng yaxshi oy: 4 (160)
oʻrtachadan yuqori oylar: 3
```

Ikkita sikl kerak: oʻrtachani bilish uchun avval hammasini yigʻish, keyin har bir oyni u bilan solishtirish lozim. Excel da xuddi shu natija `SUM`, `AVERAGE`, `MAX`, `MATCH` va `COUNTIF` bilan olinadi (M4) — dastur esa minglab fayl va hisobotlar uchun bir xil tahlilni avtomatik takrorlaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Yaxlitlash yoʻnalishini notoʻgʻri tanlash.** Zararsizlik uchun dona soni yuqoriga yaxlitlanadi, chegirma odatda pastga — masala shartini oʻqing.
- **Chegaraviy holatlarni sinamaslik.** Nol, manfiy, juda katta qiymatlar va "shart aynan teng" holatlarini sinang.
- **Butun va haqiqiy sonlarni aralashtirish.** Ayrim tillarda `7 / 2 = 3` (butun boʻlish).
- **Pul uchun haqiqiy sonlar.** Yaxlitlash xatolaridan qochish uchun pulni tiyin yoki soʻmda butun son sifatida saqlang.
- **Blok-sxemasiz murakkab dastur yozish.** Avval algoritmni chizing, keyin kodlang.
:::

:::tip
Ishlangan misollarni platformadagi kod muharririda ishga tushiring va kirish maʼlumotlarini oʻzgartiring: narxni oʻzgaruvchan xarajatdan past qilib koʻring, oylik savdoga yangi oy qoʻshing. Har bir misol uchun blok-sxema chizing.
:::

## Xulosa

- Algoritm — aniq va cheklangan amallar ketma-ketligi; xossalari: diskretlik, aniqlik, tushunarlilik, natijaviylik, ommaviylik.
- Tasvirlash: soʻz, blok-sxema, psevdokod, dastur.
- Tuzilmalar: chiziqli, tarmoqlanuvchi (shart), takrorlanuvchi (sikl).
- Iqtisodiyotda tillar: Python, VBA, SQL, R, Java, C++, JavaScript.
- Bosqichlar: masala → model → algoritm → dastur → sinov → tuzatish → foydalanish.

## Oʻzingizni tekshiring

:::check
Q: Algoritmning natijaviylik xossasi nimani anglatadi?
A: Algoritm cheklangan qadamdan keyin natija beradi.
Q: Blok-sxemada shart qaysi shakl bilan belgilanadi?
A: Romb bilan.
Q: Roʻyxatdagi barcha xodimlarning ish haqini hisoblash qaysi tuzilmaga misol?
A: Takrorlanuvchi (sikl).
Q: Zararsizlik nuqtasidagi dona soni nima uchun yuqoriga yaxlitlanadi?
A: Kasr dona sotib boʻlmaydi, pastga yaxlitlansa doimiy xarajat toʻliq qoplanmaydi.
:::

:::link https://docs.python.org/3/tutorial/
The Python Tutorial
Python dasturlash tilining rasmiy boshlangʻich qoʻllanmasi (ingliz tilida).
:::
