---
title: Bir oʻlchamli massivlarning funksiya parametrlari sifatida qoʻllanilishi
summary: Mustaqil ish: massivni funksiyaga uzatish — nusxa yoki havola, massivni oʻzgartiruvchi va hisoblovchi funksiyalar, uzunlikni uzatish; namunaviy masala — massivni minimumga nisbatan siljitish; topshiriq — maksimum va uning oʻrni.
---

## Nima uchun bu mavzu muhim?

Katta dasturda massiv bilan ishlovchi kod bir necha funksiyaga boʻlinadi: biri oʻqiydi, biri hisoblaydi, biri chiqaradi. Massivni funksiyaga toʻgʻri uzatish — har bir dasturchi bilishi kerak boʻlgan koʻnikma: funksiya massivning **nusxasini** oladimi yoki **oʻzini**? Funksiya ichidagi oʻzgarish tashqarida koʻrinadimi? Bu mustaqil ishda shu savollarga amaliy javob topasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- massivni funksiyaga uch tilda uzata olasiz va uning uzunligini toʻgʻri aniqlay olasiz;
- funksiya massivni oʻzgartirganda nima boʻlishini oldindan ayta olasiz;
- massivni oʻzgartiruvchi (protsedura) va hisoblovchi (qiymat qaytaruvchi) funksiyalarni farqlab yoza olasiz;
- funksiya uchun testlar jadvalini tuza olasiz.
:::

## Mavzu boʻyicha qisqa takror

| Til | Funksiya eʼlon qilish | Uzunlik | Funksiya ichidagi oʻzgarish tashqarida koʻrinadimi? |
|---|---|---|---|
| Python | `def f(a):` | `len(a)` | ha — roʻyxatga havola uzatiladi |
| Java | `static void f(int[] a)` | `a.length` | ha — massivga havola uzatiladi |
| C++ (`vector`) | `void f(vector<int>& a)` | `a.size()` | `&` bilan — ha; `&` siz — **yoʻq** (nusxa) |
| C++ (oddiy massiv) | `void f(int a[], int n)` | alohida parametr `n` | ha — koʻrsatkich uzatiladi |

Qoidalar:

1. Funksiya massivni **faqat oʻqisa**, C++'da `const vector<int>&` yozing — nusxa yaratilmaydi va tasodifan oʻzgartirib boʻlmaydi.
2. Funksiya massivni **oʻzgartirsa**, buni nomi aytsin (`siljit`, `saralab_qoy`) va qiymat qaytarmasin.
3. Oddiy C++ massivi funksiyaga koʻrsatkich sifatida tushadi va uzunligini „unutadi“ — uzunlik alohida uzatiladi.
4. Pythonʼda `a = [...]` funksiya ichida **yangi** roʻyxat yaratadi va tashqaridagisiga taʼsir qilmaydi; `a[i] = ...` yoki `a.append(...)` esa taʼsir qiladi.

## Namunaviy masala: minimumga nisbatan siljitish

Birinchi satrda *n*, ikkinchisida *n* ta butun son. `eng_kichik(a)` funksiyasi minimumni qaytarsin, `siljit(a)` protsedurasi esa har bir elementdan minimumni ayirib, massivning **oʻzini** oʻzgartirsin. Asosiy dastur oʻzgargan massivni chiqarsin.

```python
def eng_kichik(a):
    natija = a[0]
    for x in a:
        if x < natija:
            natija = x
    return natija


def siljit(a):                      # roʻyxatning oʻzi oʻzgaradi
    m = eng_kichik(a)
    for i in range(len(a)):
        a[i] -= m


n = int(input())
a = list(map(int, input().split()))
siljit(a)
print(*a)
```
```java
import java.util.Scanner;

public class Main {
    static int engKichik(int[] a) {
        int natija = a[0];
        for (int x : a) if (x < natija) natija = x;
        return natija;
    }

    static void siljit(int[] a) {   // massivning oʻzi oʻzgaradi
        int m = engKichik(a);
        for (int i = 0; i < a.length; i++) a[i] -= m;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        siljit(a);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int engKichik(const vector<int>& a) {          // faqat oʻqiydi
    int natija = a[0];
    for (int x : a) if (x < natija) natija = x;
    return natija;
}

void siljit(vector<int>& a) {                  // & — massivning oʻzi oʻzgaradi
    int m = engKichik(a);
    for (int& x : a) x -= m;
}

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];
    siljit(a);
    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

Testlar jadvali — mustaqil ishda xuddi shunday jadval talab qilinadi:

| Kirish | Kutilgan natija | Nimani tekshiradi |
|---|---|---|
| `4` / `7 3 9 3` | `4 0 6 0` | minimum ikki marta uchraydi |
| `3` / `-2 -5 0` | `3 0 5` | manfiy sonlar |
| `1` / `42` | `0` | bitta element |
| `3` / `5 5 5` | `0 0 0` | barcha elementlar teng |

C++'da `siljit(vector<int> a)` (`&` siz) yozilsa, dastur xato bermaydi, lekin massiv oʻzgarmaydi — birinchi test `7 3 9 3` ni chiqaradi. Aynan shunday holatlarni ushlash uchun testlar jadvali kerak.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: massivni parametr sifatida qabul qilib, **eng katta element va uning birinchi oʻrnini** (1 dan sanab) qaytaruvchi funksiya yozing. Topshiriladigan ish:

1. **Funksiya tavsifi** — nomi, parametrlari va qaytaradigan qiymat(lar)i; ikki natija qanday qaytarilishini tushuntiring (kortej, `record`, havola parametrlar yoki massiv).
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 5 ta test: maksimum boshida, oxirida, bir necha marta uchraydi, barcha elementlar manfiy, bitta element.
4. **Xulosa** — 3–5 gap: funksiya massivni oʻzgartiradimi va nima uchun.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Maksimum va oʻrinni **funksiya** topishi shart; hammasi `main` ichida yozilgan dastur toʻliq baholanmaydi.
- Maksimum bir necha marta uchrasa, **birinchi** oʻrni chiqariladi: `>` va `>=` farqiga eʼtibor bering.
- Oʻrin 1 dan sanaladi, indeks esa 0 dan — `+ 1` ni unutmang.
- Boshlangʻich qiymat `0` emas, birinchi element boʻlishi kerak: barcha sonlar manfiy boʻlishi mumkin.
:::

:::tip
Funksiyani avval qogʻozda testlar jadvali bilan tekshiring: har bir test uchun funksiya qadamma-qadam qanday qiymatlar oladi — shundan keyin kodga oʻting.
:::

## Xulosa

- Python roʻyxati va Java massivi funksiyaga havola sifatida uzatiladi: ichidagi oʻzgarish tashqarida koʻrinadi.
- C++ `vector` ni oʻzgartirish uchun `&`, faqat oʻqish uchun `const &` yoziladi; `&` siz nusxa uzatiladi.
- Oddiy C++ massivi bilan uzunlik alohida parametr sifatida beriladi.
- Hisoblovchi funksiya qiymat qaytaradi, oʻzgartiruvchi protsedura esa massivning oʻzini oʻzgartiradi.

## Oʻzingizni tekshiring

:::check
Q: Javaʼda `static void f(int[] a) { a[0] = 9; }` chaqirilgandan keyin chaqiruvchining massivida `a[0]` nimaga teng?
A: 9 ga — massivga havola uzatilgan, funksiya oʻsha massivni oʻzgartirdi.
Q: C++'da `void f(vector<int> a) { a[0] = 9; }` chaqirilsa, tashqi massiv oʻzgaradimi?
A: Yoʻq: `&` boʻlmagani uchun nusxa uzatilgan.
Q: Pythonʼda `def f(a): a = [0]` chaqirilsa, tashqi roʻyxat oʻzgaradimi?
A: Yoʻq: `a = [0]` funksiya ichida yangi roʻyxat yaratadi va lokal nomni unga bogʻlaydi.
Q: Nima uchun C++'da oddiy massiv bilan uzunlik alohida uzatiladi?
A: Massiv funksiyaga koʻrsatkich sifatida tushadi va oʻz uzunligi haqidagi maʼlumotni yoʻqotadi.
:::

:::link https://docs.python.org/3/faq/programming.html#how-do-i-write-a-function-with-output-parameters-call-by-reference
Python: funksiyaga argument uzatish
Pythonʼda argumentlar qanday uzatilishi haqida rasmiy savol-javob (ingliz tilida).
:::
