---
title: Tarmoqlanuvchi jarayonlarni tashkil etish (algoritm va dastur)
summary: Mustaqil ish: oraliqlarga boʻlingan masala uchun algoritm tuzish, blok-sxema, dastur va testlar jadvali; namunaviy masala — elektr energiyasi tarifi.
---

## Nima uchun bu mavzu muhim?

Soliq stavkasi daromadga, elektr toʻlovi isteʼmolga, chegirma xarid summasiga qarab oʻzgaradi. Bunday „oraliqlarga boʻlingan“ qoidalar hayotda juda koʻp va ularni dasturda xatosiz yozish — har bir dasturchidan kutiladigan koʻnikma. Bu mustaqil ishda siz oraliqli masalani boshidan oxirigacha mustaqil yechasiz: algoritm, dastur va testlar.

:::note
**Bu mustaqil ishdan soʻng siz:**

- oraliqlarga boʻlingan masalaning shartlarini toʻgʻri tartibda yoza olasiz;
- algoritmni soʻz bilan va blok-sxema koʻrinishida tasvirlay olasiz;
- har bir tarmoq va chegaraviy qiymat uchun testlar jadvalini tuza olasiz;
- yechimni Python, Java yoki C++'da yozib, avtomatik testlardan oʻtkaza olasiz.
:::

## Mavzu boʻyicha qisqa takror

Oraliqli masalada son oʻqi bir nechta boʻlakka boʻlinadi va har bir boʻlakda oʻz qoidasi ishlaydi. Yechish tartibi:

1. **Oraliqlarni yozib chiqing** va chegaralar qaysi oraliqqa tegishli ekanini aniqlang (`<` yoki `<=`).
2. **Zanjir tuzing**: `if` … `elif` / `else if` … `else`. Shartlarni bir tomondan (kichikdan kattaga yoki aksincha) yozsangiz, har birida faqat bitta chegarani tekshirish kifoya.
3. **Testlar**: har bir oraliqning ichidan bitta qiymat va har bir chegaraning ikki tomonidan qiymatlar.

| Xato | Oqibati |
|---|---|
| Chegara ikki oraliqqa ham kirdi | natija shartga zid |
| Chegara hech qaysi oraliqqa kirmadi | baʼzi qiymatlar uchun natija yoʻq |
| Shartlar tartibi buzildi | keng shart tor shartni „yutib“ yuboradi |

## Namunaviy masala: elektr energiyasi tarifi

Oylik isteʼmol (kVt·soat, butun son) kiritiladi. Toʻlovni hisoblang: dastlabki 100 kVt·soat — 450 soʻmdan, 101 dan 300 gacha — 900 soʻmdan, 300 dan ortigʻi — 1350 soʻmdan.

Algoritm soʻz bilan:

```text
1. q ni oʻqi
2. agar q <= 100: tolov = q * 450
3. aks holda agar q <= 300: tolov = 100 * 450 + (q - 100) * 900
4. aks holda: tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350
5. tolov ni chiqar
```

Blok-sxemada bu ikki ketma-ket romb: `q <= 100?` va `q <= 300?`, har birining „ha“ tarmogʻida bitta hisob.

```python
q = int(input())

if q <= 100:
    tolov = q * 450
elif q <= 300:
    tolov = 100 * 450 + (q - 100) * 900
else:
    tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350

print(tolov)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int q = in.nextInt();

        long tolov;
        if (q <= 100) {
            tolov = q * 450L;
        } else if (q <= 300) {
            tolov = 100 * 450L + (q - 100) * 900L;
        } else {
            tolov = 100 * 450L + 200 * 900L + (q - 300) * 1350L;
        }

        System.out.println(tolov);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long q;
    cin >> q;

    long long tolov;
    if (q <= 100) {
        tolov = q * 450;
    } else if (q <= 300) {
        tolov = 100 * 450 + (q - 100) * 900;
    } else {
        tolov = 100 * 450 + 200 * 900 + (q - 300) * 1350;
    }

    cout << tolov << endl;
    return 0;
}
```

Testlar jadvali — mustaqil ishda xuddi shunday jadval talab qilinadi:

| Kirish | Oraliq | Kutilgan natija | Izoh |
|---|---|---|---|
| 0 | birinchi | 0 | quyi chegara |
| 100 | birinchi | 45000 | chegara: birinchi oraliqqa kiradi |
| 101 | ikkinchi | 45900 | chegaradan keyingi qiymat |
| 150 | ikkinchi | 90000 | oraliq ichi |
| 300 | ikkinchi | 225000 | chegara |
| 350 | uchinchi | 292500 | oraliq ichi |

Java'da `450L` — `long` literal: katta isteʼmolda `int` toʻlib ketmasligi uchun.

## Mustaqil ish topshirigʻi

Quyida berilgan **boʻlakli funksiya** masalasini yeching. Topshiriladigan ish:

1. **Algoritm** — soʻz bilan (yuqoridagi namunadek) yoki blok-sxema rasmi.
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 6 ta test: har bir oraliqdan bittadan va har bir chegara (*x* = 0, *x* = 10) atrofidan; har biri uchun kutilgan natija qoʻlda hisoblangan.
4. **Xulosa** — 3–5 gap: qaysi chegarada xato qilish oson va siz buni qanday tekshirdingiz.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Algoritm va dastur bir-biriga mos boʻlishi kerak; algoritmsiz dastur toʻliq baholanmaydi.
- Chegaraviy qiymatlar (*x* = 0 va *x* = 10) shartda koʻrsatilgan oraliqqa kirishi shart: `0 ≤ x ≤ 10`.
- Kutilgan natijalar dasturdan koʻchirilmasdan, qoʻlda hisoblangan boʻlishi kerak.
- Boshqa talabaning ishidan koʻchirilgan dastur qabul qilinmaydi.
:::

:::tip
Avval testlar jadvalini toʻldiring, keyin dastur yozing. Dastur jadvaldagi barcha natijalarni bersa, uni topshiriq oynasida yuboring.
:::

## Xulosa

- Oraliqli masalada chegaralar qaysi oraliqqa tegishli ekanini birinchi aniqlang.
- Shartlarni bir tomondan tartiblang — har birida bitta chegara tekshiriladi.
- Testlar: har bir oraliq ichidan va har bir chegara atrofidan.
- Algoritm → testlar → dastur tartibida ishlash xatolarni kamaytiradi.

## Oʻzingizni tekshiring

:::check
Q: Namunaviy masalada 300 kVt·soat qaysi tarif boʻyicha hisoblanadi?
A: Ikkinchi oraliq boʻyicha (`q <= 300`): 45 000 + 200 · 900 = 225 000 soʻm.
Q: Uchta oraliqli masala uchun kamida nechta test kerak?
A: Kamida 3 ta (har bir oraliqdan bittadan), lekin ishonchli tekshiruv uchun har bir chegara atrofidan ham test qoʻshiladi.
Q: Agar `elif q <= 300` sharti `if q <= 100` dan oldin tursa, nima boʻladi?
A: 100 dan kichik isteʼmol ham ikkinchi tarif bilan hisoblanadi: keng shart tor shartni „yutib“ yuboradi.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html#if-statements
Python: if operatori
`if`, `elif`, `else` boʻyicha rasmiy qoʻllanma.
:::
