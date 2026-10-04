---
title: Ichma-ich joylashgan rekursiv jarayonlarni tashkil etish
summary: Mustaqil ish: ichma-ich (nested) rekursiya — chaqiruv argumentida yana shu funksiyaning chaqiruvi; chaqiruvlarni sanash, rekursiya chuqurligi va tez oʻsish; namunaviy masala — Makkarti 91 funksiyasi; topshiriq — Akkerman funksiyasi.
---

## Nima uchun bu mavzu muhim?

Oddiy rekursiyada funksiya oʻzini kichikroq argument bilan chaqiradi: *f*(*n* − 1). **Ichma-ich rekursiya**da esa argumentning oʻzi ham shu funksiya chaqiruvi boʻladi: *f*(*f*(*n* + 11)). Bunday funksiyalar juda qisqa yoziladi, lekin chaqiruvlar soni va natijasini oldindan aytish qiyin — ular hisoblash nazariyasida „rekursiya qanchalik kuchli“ degan savolga javob berish uchun oʻrganiladi. Bu mustaqil ishda ichma-ich rekursiyani kuzatish va uning xatti-harakatini tahlil qilishni mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- ichma-ich rekursiyani oddiy rekursiyadan farqlay olasiz;
- ichma-ich chaqiruvlar ketma-ketligini qadamma-qadam kuzata olasiz;
- global hisoblagich yordamida chaqiruvlar sonini oʻlchay olasiz;
- rekursiya chuqurligi chegarasini hisobga olib, dasturni toʻgʻri sozlay olasiz.
:::

## Mavzu boʻyicha qisqa takror

| Rekursiya turi | Koʻrinishi | Misol |
|---|---|---|
| Chiziqli | bitta chaqiruv | *n*! = *n* · (*n* − 1)! |
| Daraxtsimon | bir nechta mustaqil chaqiruv | *F*(*n*) = *F*(*n* − 1) + *F*(*n* − 2) |
| Ichma-ich | chaqiruv natijasi yana argument | *M*(*n*) = *M*(*M*(*n* + 11)) |

Ichma-ich chaqiruv **ichkaridan tashqariga** hisoblanadi: avval ichki *M*(*n* + 11) toʻliq hisoblanadi, uning natijasi tashqi *M* ga argument boʻlib beriladi. Bunday rekursiya toʻxtashini isbotlash oddiy rekursiyadagidan qiyin: argument har doim ham kamaymaydi (*n* + 11 — kattaroq!).

Chaqiruvlarni sanash uchun funksiyadan tashqarida **hisoblagich** eʼlon qilinadi va funksiyaning birinchi qatorida oshiriladi: Python'da `global soni`, Java'da `static long soni`, C++'da global oʻzgaruvchi.

## Namunaviy masala: Makkarti 91 funksiyasi

Jon Makkarti (sunʼiy intellekt atamasi muallifi) taklif qilgan funksiya:

```text
M(n) = n − 10,          agar n > 100
M(n) = M(M(n + 11)),    aks holda
```

*n* kiritiladi (0 ≤ *n* ≤ 200). *M*(*n*) ni va funksiya jami necha marta chaqirilganini chiqaring.

```python
import sys

sys.setrecursionlimit(10000)
soni = 0


def m(n):
    global soni
    soni += 1
    if n > 100:
        return n - 10
    return m(m(n + 11))             # ichma-ich chaqiruv


n = int(input())
natija = m(n)
print(natija)
print(soni)
```
```java
import java.util.Scanner;

public class Main {
    static long soni = 0;

    static int m(int n) {
        soni++;
        if (n > 100) return n - 10;
        return m(m(n + 11));        // ichma-ich chaqiruv
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int natija = m(n);
        System.out.println(natija);
        System.out.println(soni);
    }
}
```
```cpp
#include <iostream>
using namespace std;

long long soni = 0;

int m(int n) {
    soni++;
    if (n > 100) return n - 10;
    return m(m(n + 11));            // ichma-ich chaqiruv
}

int main() {
    int n;
    cin >> n;
    int natija = m(n);
    cout << natija << endl;
    cout << soni << endl;
    return 0;
}
```

Chaqiruvlar ketma-ketligi — mustaqil ishda xuddi shunday jadval talab qilinadi (*M*(99) uchun):

| Qadam | Chaqiruv | Nima boʻladi |
|---|---|---|
| 1 | *M*(99) | 99 ≤ 100 → *M*(*M*(110)) |
| 2 | *M*(110) | 110 > 100 → 100 |
| 3 | *M*(100) | 100 ≤ 100 → *M*(*M*(111)) |
| 4 | *M*(111) | 111 > 100 → 101 |
| 5 | *M*(101) | 101 > 100 → **91** |

Testlar:

| Kirish | *M*(*n*) | Chaqiruvlar soni |
|---|---|---|
| 99 | 91 | 5 |
| 87 | 91 | 29 |
| 150 | 140 | 1 |
| 0 | 91 | 203 |

Ajoyib xossa: 101 dan kichik **har qanday** *n* uchun natija doim 91 — funksiya nomi shundan. Bu natija oldindan koʻrinmaydi va uni faqat chaqiruvlarni kuzatib yoki matematik isbot bilan tushunish mumkin. Python'da `setrecursionlimit` chuqurlik chegarasini oshiradi: *n* = 0 da rekursiya 100 dan ortiq qavat chuqurlashadi.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: **Akkerman funksiyasi**:

```text
A(0, n) = n + 1
A(m, 0) = A(m − 1, 1),               m > 0
A(m, n) = A(m − 1, A(m, n − 1)),     m > 0, n > 0
```

*m* va *n* kiritiladi (0 ≤ *m* ≤ 3, 0 ≤ *n* ≤ 5); *A*(*m*, *n*) ni chiqaring. Topshiriladigan ish:

1. **Chaqiruvlar jadvali** — *A*(1, 2) uchun barcha chaqiruvlar ketma-ketligi (yuqoridagi namunadek).
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Tajriba jadvali** — *m* = 0, 1, 2, 3 va *n* = 0…3 uchun natijalar va (hisoblagich bilan oʻlchangan) chaqiruvlar soni.
4. **Xulosa** — 3–5 gap: natija va chaqiruvlar soni *m* oshganda qanday oʻsadi; nima uchun *A*(4, 2) ni hisoblab boʻlmaydi.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Funksiya taʼrifdagi uchta holatni aynan shu tartibda tekshirishi kerak: avval *m* = 0, keyin *n* = 0.
- Python'da `sys.setrecursionlimit` ni oshiring: *A*(3, 5) uchun chuqurlik yuzlab qavatga yetadi.
- Natija juda katta boʻlishi mumkin, lekin berilgan chegaralarda `int` ga sigʻadi — buni xulosada tekshiring.
- Tajriba jadvalidagi chaqiruvlar soni dastur bilan oʻlchanadi, taxmin qilinmaydi.
:::

:::tip
Avval *A*(0, *n*), keyin *A*(1, *n*) va *A*(2, *n*) uchun bir nechta qiymatni hisoblab, ularda qonuniyat qidiring (masalan, *A*(1, *n*) = *n* + 2). Topilgan formulalar dastur natijasini tekshirishga yordam beradi.
:::

## Xulosa

- Ichma-ich rekursiyada chaqiruv natijasi yana shu funksiyaga argument boʻladi; hisoblash ichkaridan tashqariga boradi.
- Bunday funksiyalarning natijasi va chaqiruvlar soni oldindan koʻrinmaydi — ularni kuzatish jadvali va hisoblagich bilan oʻrganiladi.
- Chaqiruvlar sonini oʻlchash uchun funksiyadan tashqaridagi hisoblagich har chaqiruvda oshiriladi.
- Chuqur rekursiyada til chegaralarini hisobga oling: Python'da `setrecursionlimit`, Java va C++'da stek hajmi.

## Oʻzingizni tekshiring

:::check
Q: Ichma-ich rekursiya daraxtsimon rekursiyadan nimasi bilan farq qiladi?
A: Daraxtsimonda chaqiruvlar mustaqil va natijalari qoʻshiladi; ichma-ichda bir chaqiruvning natijasi boshqa chaqiruvning argumenti boʻladi.
Q: *M*(*M*(110)) da qaysi chaqiruv birinchi bajariladi?
A: Ichki *M*(110); uning natijasi (100) tashqi *M* ga beriladi.
Q: *M*(50) nimaga teng?
A: 91 — 101 dan kichik har qanday *n* uchun *M*(*n*) = 91.
Q: Nima uchun chaqiruvlar hisoblagichi funksiya ichida emas, tashqarida eʼlon qilinadi?
A: Funksiya ichidagi oʻzgaruvchi har chaqiruvda qaytadan yaratiladi; umumiy son faqat tashqi (global yoki statik) oʻzgaruvchida yigʻiladi.
:::

:::link https://en.wikipedia.org/wiki/McCarthy_91_function
Makkarti 91 funksiyasi
Funksiya tarixi va nima uchun natija doim 91 ekanining isboti (ingliz tilida).
:::
