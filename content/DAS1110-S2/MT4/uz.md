---
title: Rekursiv jarayonlarni tashkil etish
summary: Mustaqil ish: rekursiv funksiyani loyihalash tartibi — bazaviy holat, masalani kichraytirish, natijani yigʻish; chaqiruvlar jadvali; namunaviy masala — sonning raqamlarini teskari yozish va sanash; topshiriq — Xanoy minoralari.
---

## Nima uchun bu mavzu muhim?

Rekursiya — masalani **oʻzining kichikroq nusxasi** orqali yechish. Baʼzi masalalarni (Xanoy minoralari, daraxtlarni aylanish, barcha variantlarni sanash) sikl bilan yozish juda qiyin, rekursiya bilan esa bir necha qatorda yoziladi. Lekin rekursiv funksiya toʻgʻri ishlashiga ishonch hosil qilish uchun uni **tizimli** loyihalash va chaqiruvlarni kuzatib borish kerak. Bu mustaqil ishda shu koʻnikmani mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- rekursiv funksiyani uch qadamda loyihalay olasiz: bazaviy holat, kichraytirish, natijani yigʻish;
- rekursiv chaqiruvlar jadvalini (yoki daraxtini) tuza olasiz;
- rekursiya chuqurligi va chaqiruvlar sonini baholay olasiz;
- rekursiv yechimni uch tilda yozib, testlardan oʻtkaza olasiz.
:::

## Mavzu boʻyicha qisqa takror

Rekursiv funksiyani loyihalash tartibi:

1. **Bazaviy holat** — rekursiyasiz javob beriladigan eng kichik holat (`n == 0`, boʻsh satr, bitta disk).
2. **Kichraytirish** — masalani bir yoki bir nechta **kichikroq** xuddi shunday masalaga keltirish (`n // 10`, `n - 1`).
3. **Yigʻish** — kichik masalalar javobidan katta masala javobini olish.

| Tushuncha | Maʼnosi |
|---|---|
| Rekursiya chuqurligi | bir vaqtda stekda turgan chaqiruvlarning eng koʻp soni |
| Chaqiruvlar soni | jami necha marta funksiya chaqirilgani |
| Chiqarish tartibi | rekursiv chaqiruvdan **oldin** chiqarilsa — toʻgʻri tartib, **keyin** — teskari tartib |

Pythonʼda rekursiya chuqurligi sukut boʻyicha ~1000 bilan cheklangan; Java va C++'da chegara stek hajmiga bogʻliq (odatda bir necha ming–oʻn minglab chaqiruv).

## Namunaviy masala: raqamlarni teskari yozish

*n* kiritiladi (0 ≤ *n* ≤ 10¹⁸). Ikkita rekursiv funksiya yozing: `teskari(n)` *n* ning raqamlarini oxiridan boshlab bitta satrga chiqarsin (`1200` → `0021`), `raqamlar_soni(n)` esa raqamlar sonini qaytarsin. Ikkala natijani alohida satrlarda chiqaring.

```python
def teskari(n):
    print(n % 10, end="")           # avval oxirgi raqam
    if n >= 10:
        teskari(n // 10)            # keyin qolgan qismi


def raqamlar_soni(n):
    if n < 10:                      # bazaviy holat: bitta raqam
        return 1
    return 1 + raqamlar_soni(n // 10)


n = int(input())
teskari(n)
print()
print(raqamlar_soni(n))
```
```java
import java.util.Scanner;

public class Main {
    static void teskari(long n) {
        System.out.print(n % 10);   // avval oxirgi raqam
        if (n >= 10) teskari(n / 10);   // keyin qolgan qismi
    }

    static int raqamlarSoni(long n) {
        if (n < 10) return 1;       // bazaviy holat: bitta raqam
        return 1 + raqamlarSoni(n / 10);
    }

    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        teskari(n);
        System.out.println();
        System.out.println(raqamlarSoni(n));
    }
}
```
```cpp
#include <iostream>
using namespace std;

void teskari(long long n) {
    cout << n % 10;                 // avval oxirgi raqam
    if (n >= 10) teskari(n / 10);   // keyin qolgan qismi
}

int raqamlarSoni(long long n) {
    if (n < 10) return 1;           // bazaviy holat: bitta raqam
    return 1 + raqamlarSoni(n / 10);
}

int main() {
    long long n;
    cin >> n;
    teskari(n);
    cout << endl;
    cout << raqamlarSoni(n) << endl;
    return 0;
}
```

Chaqiruvlar jadvali — mustaqil ishda xuddi shunday jadval talab qilinadi (`raqamlar_soni(1200)` uchun):

| Chaqiruv | Bazaviy holatmi? | Qaytaradi |
|---|---|---|
| `raqamlar_soni(1200)` | yoʻq | 1 + `raqamlar_soni(120)` = 4 |
| `raqamlar_soni(120)` | yoʻq | 1 + `raqamlar_soni(12)` = 3 |
| `raqamlar_soni(12)` | yoʻq | 1 + `raqamlar_soni(1)` = 2 |
| `raqamlar_soni(1)` | ha | 1 |

Testlar: `1200` → `0021` va `4`; `7` → `7` va `1`; `0` → `0` va `1`. `teskari` da chiqarish rekursiv chaqiruvdan **oldin** turibdi: shuning uchun oxirgi raqam birinchi chiqadi. Agar `print` ni chaqiruvdan keyinga koʻchirsangiz, raqamlar asl tartibda chiqadi — bu ham foydali tajriba. *n* = 0 holati alohida tekshirilishi kerak: „0 ning raqamlari soni“ 0 emas, 1.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: **Xanoy minoralari** — *n* ta diskni 1-qoziqdan 3-qoziqqa oʻtkazing: bir martada bitta disk, katta disk kichigining ustiga qoʻyilmaydi. Rekursiv gʻoya: *n* − 1 diskni yordamchi qoziqqa, eng kattasini maqsadga, soʻng *n* − 1 diskni uning ustiga. Topshiriladigan ish:

1. **Rekursiv tavsif** — funksiya parametrlari (disklar soni, qayerdan, qayerga, yordamchi), bazaviy holat va rekursiv qadam soʻz bilan.
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Chaqiruvlar daraxti** — *n* = 3 uchun: har bir chaqiruv va u bajaradigan harakat.
4. **Xulosa** — 3–5 gap: harakatlar soni nima uchun 2ⁿ − 1 ga teng va *n* = 64 boʻlsa, sekundiga bitta harakat bilan qancha vaqt kerak boʻladi.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Yechim rekursiv boʻlishi shart; harakatlar roʻyxati qoʻlda yozilgan yoki faqat formula bilan hisoblangan dastur qabul qilinmaydi.
- Birinchi satrda harakatlar soni chiqariladi — uni harakatlarni chiqarishdan **oldin** bilish kerak (formula yoki avval roʻyxat yigʻish).
- Rekursiv chaqiruvlarda qoziqlar oʻrnini almashtirib yubormang: *n* − 1 disk avval **yordamchi** qoziqqa ketadi.
- Bazaviy holat — *n* = 0 (hech narsa qilinmaydi) yoki *n* = 1 (bitta harakat); ikkalasidan biri aniq boʻlsin.
:::

:::tip
*n* = 1, 2, 3 uchun harakatlarni avval qoʻlda, uchta tanga bilan bajarib koʻring. Qoʻlda topilgan ketma-ketlik dastur chiqarganiga mos kelsa, katta *n* uchun ham ishonish mumkin.
:::

## Xulosa

- Rekursiv funksiya: bazaviy holat → kichraytirish → natijani yigʻish.
- Chaqiruvlar jadvali yoki daraxti rekursiya qanday ishlashini koʻrsatadi va xatolarni topishga yordam beradi.
- Rekursiv chaqiruvdan oldin chiqarish — bir tartib, keyin chiqarish — teskari tartib.
- Chegaraviy holatlar (*n* = 0, bitta element) rekursiyada alohida eʼtibor talab qiladi.

## Oʻzingizni tekshiring

:::check
Q: Rekursiv funksiyani loyihalashning uch qadami qaysilar?
A: Bazaviy holatni aniqlash, masalani kichikroq xuddi shunday masalaga keltirish va kichik javoblardan kattasini yigʻish.
Q: `raqamlar_soni(5000)` necha marta chaqiriladi (birinchi chaqiruv bilan)?
A: 4 marta: 5000, 500, 50, 5.
Q: `teskari` da `print` rekursiv chaqiruvdan keyin tursa, `123` uchun nima chiqadi?
A: `123` — raqamlar asl tartibda.
Q: Xanoy minoralarida 4 disk uchun nechta harakat kerak?
A: 2⁴ − 1 = 15 ta.
:::

:::link https://en.wikipedia.org/wiki/Tower_of_Hanoi
Xanoy minoralari
Masala tarixi, rekursiv yechim va harakatlar soni (ingliz tilida).
:::
