---
title: Dinamik massivlar va koʻrsatkichlar
summary: Mustaqil ish: oʻlchami ishga tushganda aniqlanadigan massiv, ikki oʻtishli usul (avval sanash, keyin ajratish), new[]/delete[] va koʻrsatkich bilan aylanish, funksiyadan dinamik massiv qaytarish; namunaviy masala — filtrlangan massiv; topshiriq — ikki saralangan massivni birlashtirish.
---

## Nima uchun bu mavzu muhim?

Koʻp masalalarda natija massivining oʻlchami oldindan nomaʼlum: „berilgan sondan katta elementlarni alohida massivga yozing“, „ikki massivni bittaga birlashtiring“. Bunday massiv **dastur ishlayotganda**, kerakli oʻlchamda yaratiladi. Bu mustaqil ishda dinamik massivni toʻgʻri oʻlchamda yaratish, uni koʻrsatkich bilan aylanib chiqish va funksiyadan qaytarishni mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- natija massivi oʻlchamini oldindan hisoblab, uni aynan shu oʻlchamda yarata olasiz;
- C++'da `new[]` bilan ajratilgan xotirani `delete[]` bilan toʻgʻri boʻshata olasiz;
- massivni koʻrsatkich bilan aylanib chiqa olasiz;
- funksiyadan yangi dinamik massiv va uning uzunligini qaytara olasiz.
:::

## Mavzu boʻyicha qisqa takror

Natija oʻlchami nomaʼlum boʻlganda ikki yoʻl bor:

| Usul | Gʻoyasi | Qachon |
|---|---|---|
| **Ikki oʻtish** | 1-oʻtish: nechta element kerakligini sanash; ajratish; 2-oʻtish: toʻldirish | oddiy massiv (`new int[k]`, `new int[k]` Javaʼda) |
| **Oʻsuvchi massiv** | boʻsh massivdan boshlab, har bir elementni oxiriga qoʻshish | `list`, `ArrayList`, `vector` |

| Til | Dinamik massiv yaratish | Boʻshatish | Funksiyadan qaytarish |
|---|---|---|---|
| Python | `[0] * k` yoki `append` | avtomatik | `return royxat` |
| Java | `new int[k]` | avtomatik | `return massiv;` (uzunligi `length` da) |
| C++ (`new`) | `int* p = new int[k];` | `delete[] p;` | koʻrsatkich **va** uzunlik (masalan, havola parametr orqali) |
| C++ (`vector`) | `vector<int> v(k);` | avtomatik | `return v;` |

C++'da funksiya `new` bilan ajratilgan massivni qaytarsa, uni **chaqiruvchi** `delete[]` bilan boʻshatishi kerak — bu kelishuv funksiya tavsifida yozib qoʻyiladi. Koʻrsatkich bilan aylanish: `for (int* p = a; p != a + n; ++p)` — `*p` joriy element.

## Namunaviy masala: filtrlangan dinamik massiv

Birinchi satrda *n*, ikkinchisida *n* ta butun son, uchinchisida *x*. `kattalar(a, n, x)` funksiyasi *x* dan katta elementlardan iborat **yangi** massiv yaratib qaytarsin (asl tartibda). Yangi massiv uzunligini va elementlarini chiqaring (boʻsh boʻlsa — `-`).

```python
def kattalar(a, x):
    natija = []                          # oʻsuvchi roʻyxat
    for v in a:
        if v > x:
            natija.append(v)
    return natija


n = int(input())
a = list(map(int, input().split()))
x = int(input())
b = kattalar(a, x)
print(len(b))
print(*b if b else "-")
```
```java
import java.util.Scanner;

public class Main {
    static int[] kattalar(int[] a, int x) {
        int k = 0;
        for (int v : a) if (v > x) k++;  // 1-oʻtish: sanash
        int[] natija = new int[k];       // aynan kerakli oʻlcham
        int j = 0;
        for (int v : a) if (v > x) natija[j++] = v;   // 2-oʻtish: toʻldirish
        return natija;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        int x = in.nextInt();

        int[] b = kattalar(a, x);
        System.out.println(b.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(b[i]);
        }
        System.out.println(b.length > 0 ? sb.toString() : "-");
    }
}
```
```cpp
#include <iostream>
using namespace std;

// Yangi massiv qaytaradi; uni chaqiruvchi delete[] bilan boʻshatadi.
int* kattalar(const int* a, int n, int x, int& k) {
    k = 0;
    for (const int* p = a; p != a + n; ++p)       // 1-oʻtish: sanash
        if (*p > x) k++;
    int* natija = new int[k];                     // aynan kerakli oʻlcham
    int* q = natija;
    for (const int* p = a; p != a + n; ++p)       // 2-oʻtish: toʻldirish
        if (*p > x) *q++ = *p;
    return natija;
}

int main() {
    int n;
    cin >> n;
    int* a = new int[n];
    for (int i = 0; i < n; i++) cin >> a[i];
    int x;
    cin >> x;

    int k;
    int* b = kattalar(a, n, x, k);
    cout << k << endl;
    if (k == 0) cout << "-";
    for (int i = 0; i < k; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;

    delete[] b;                                   // kattalar() ajratgan xotira
    delete[] a;
    return 0;
}
```

Testlar jadvali:

| Kirish | Kutilgan natija | Nimani tekshiradi |
|---|---|---|
| `6` / `4 9 1 7 3 9` / `4` | `3`, `9 7 9` | oddiy holat, takror elementlar |
| `3` / `1 2 3` / `5` | `0`, `-` | boʻsh natija |
| `3` / `-1 -5 -3` / `-4` | `2`, `-1 -3` | manfiy sonlar |
| `2` / `4 4` / `4` | `0`, `-` | *x* ga teng elementlar olinmaydi |

C++ versiyasida `*q++ = *p` — „*q* koʻrsatgan joyga yoz, keyin *q* ni keyingi elementga sur“. Xotira ikki joyda ajratildi (`a` va `kattalar` ichida) — demak, oxirida ikkita `delete[]` boʻlishi shart.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: ikkita oʻsish tartibidagi massivni **bitta saralangan massivga birlashtiring** (merge). Tayyor saralash funksiyasidan foydalanmang: ikki koʻrsatkichli usul *O*(*n* + *m*) vaqtda ishlaydi — har qadamda ikki massivning joriy elementlaridan kichigi natijaga yoziladi. Topshiriladigan ish:

1. **Algoritm** — soʻz bilan yoki blok-sxema: koʻrsatkichlar qanday suriladi va massivlardan biri tugaganda nima qilinadi.
2. **Dastur** — natija massivi aynan *n* + *m* oʻlchamda dinamik yaratiladi; Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 5 ta test: bir massiv ikkinchisidan butunlay kichik, elementlar aralash, teng elementlar, massivlardan biri bitta elementli, turli uzunlikdagi massivlar.
4. **Xulosa** — 3–5 gap: nima uchun bu usul `sort` dan tezroq va qancha qoʻshimcha xotira talab qiladi.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- `sort`, `sorted`, `Arrays.sort` kabi funksiyalardan foydalanish — topshiriq bajarilmagan hisoblanadi.
- Massivlardan biri tugagach, ikkinchisining **qolgan** elementlarini natijaga koʻchirishni unutmang.
- C++'da `new[]` ishlatilsa, har biriga mos `delete[]` boʻlishi shart.
- Teng elementlar ikkalasi ham natijaga tushadi — hech biri yoʻqolmasligi kerak.
:::

:::tip
Algoritmni ikkita kichik massivda qogʻozda bajaring: `1 4 9` va `2 3 10`. Har bir qadamda ikkala koʻrsatkichning joyini va natijaga nima yozilganini jadvalga yozing.
:::

## Xulosa

- Natija oʻlchami nomaʼlum boʻlsa: ikki oʻtish (sanash → ajratish → toʻldirish) yoki oʻsuvchi massiv.
- C++'da `new[]` bilan ajratilgan xotira `delete[]` bilan boʻshatiladi; funksiya qaytargan massivni chaqiruvchi boʻshatadi.
- Koʻrsatkich bilan aylanish: `p != a + n`, `*p` — joriy element, `++p` — keyingisi.
- Java va Pythonʼda massiv uzunligi uning oʻzida saqlanadi, C++ koʻrsatkichida esa — yoʻq, u alohida qaytariladi.

## Oʻzingizni tekshiring

:::check
Q: „Ikki oʻtish“ usulining birinchi oʻtishi nima uchun kerak?
A: Natija massivida nechta element boʻlishini aniqlab, uni aynan shu oʻlchamda yaratish uchun.
Q: C++'da `int* b = kattalar(...)` dan keyin xotirani kim boʻshatishi kerak?
A: Chaqiruvchi — `delete[] b;` bilan, chunki massiv funksiya ichida `new[]` bilan ajratilgan.
Q: `*q++ = *p` nima qiladi?
A: `p` koʻrsatgan qiymatni `q` koʻrsatgan joyga yozadi, soʻng `q` ni keyingi elementga suradi.
Q: Nima uchun C++ funksiyasi koʻrsatkich bilan birga uzunlikni ham qaytarishi kerak?
A: Koʻrsatkich massiv uzunligi haqida maʼlumot saqlamaydi.
:::

:::link https://en.cppreference.com/w/cpp/language/delete
C++: delete ifodasi
`delete` va `delete[]` qoidalari (ingliz tilida).
:::
