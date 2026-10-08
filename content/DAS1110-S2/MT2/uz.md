---
title: Matritsalarning funksiya parametrlari sifatida qoʻllanilishi
summary: Mustaqil ish: matritsani funksiyaga uzatish va funksiyadan qaytarish, oʻlchamlarni aniqlash, C++'da vector<vector<int>> va statik matritsa; namunaviy masala — matritsa izi va simmetriyani tekshirish; topshiriq — matritsalarni koʻpaytirish.
---

## Nima uchun bu mavzu muhim?

Matritsa bilan ishlovchi amallar — qoʻshish, koʻpaytirish, transponirlash, burish — muhandislik hisoblari, kompyuter grafikasi va sunʼiy intellektning asosi. Har bir amalni alohida funksiya qilib yozsangiz, ularni bir-biriga ulab murakkab hisoblarni qurish mumkin: `koʻpaytir(transponirla(A), B)`. Buning uchun matritsani funksiyaga uzatish va funksiyadan yangi matritsa qaytarishni bilish kerak.

:::note
**Bu mustaqil ishdan soʻng siz:**

- matritsani funksiyaga uch tilda uzata olasiz va uning oʻlchamlarini funksiya ichida aniqlay olasiz;
- funksiyadan yangi matritsa qaytara olasiz;
- matritsani oʻqish, chiqarish va hisoblash funksiyalarini ajratib yoza olasiz;
- matritsali funksiya uchun testlar jadvalini tuza olasiz.
:::

## Mavzu boʻyicha qisqa takror

| Til | Parametr | Satrlar soni | Ustunlar soni | Yangi matritsa qaytarish |
|---|---|---|---|---|
| Python | `def f(m):` | `len(m)` | `len(m[0])` | `return [[0] * p for _ in range(n)]` |
| Java | `static int f(int[][] m)` | `m.length` | `m[0].length` | `return new int[n][p];` |
| C++ | `int f(const vector<vector<int>>& m)` | `m.size()` | `m[0].size()` | `return vector<vector<int>>(n, vector<int>(p));` |
| C++ (statik) | `int f(int m[][100], int n, int k)` | parametr `n` | parametr `k` | qaytarib boʻlmaydi — natija parametr orqali |

Eslatmalar:

1. C++'da statik matritsani uzatishda **ikkinchi** oʻlcham albatta yoziladi (`int m[][100]`): kompilyator `m[i][j]` manzilini hisoblashi uchun satr uzunligini bilishi kerak. Shu sababli `vector` qulayroq.
2. Funksiya matritsani faqat oʻqisa, C++'da `const &` yozing — katta matritsaning nusxasi yaratilmaydi.
3. C++'da funksiyadan `vector` qaytarish tez: kompilyator nusxa olmasdan „koʻchiradi“ (move).
4. Matritsani oʻqish va chiqarishni ham alohida funksiya qiling — ular har bir matritsali masalada qayta kerak boʻladi.

## Namunaviy masala: iz va simmetriya

Birinchi satrda *n*, soʻng *n* × *n* kvadrat matritsa. `iz(m)` funksiyasi bosh diagonal elementlari yigʻindisini, `simmetrikmi(m)` funksiyasi esa matritsa transponirlanganiga tengligini (har bir `m[i][j] == m[j][i]`) aniqlasin. Izni va `Ha` yoki `Yoʻq` ni chiqaring.

```python
def oqi(n):
    return [list(map(int, input().split())) for _ in range(n)]


def iz(m):
    return sum(m[i][i] for i in range(len(m)))


def simmetrikmi(m):
    n = len(m)
    for i in range(n):
        for j in range(i + 1, n):           # faqat diagonal ustidagi yarmi
            if m[i][j] != m[j][i]:
                return False
    return True


n = int(input())
m = oqi(n)
print(iz(m))
print("Ha" if simmetrikmi(m) else "Yoʻq")
```
```java
import java.util.Scanner;

public class Main {
    static int[][] oqi(Scanner in, int n) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = in.nextInt();
        return m;
    }

    static long iz(int[][] m) {
        long s = 0;
        for (int i = 0; i < m.length; i++) s += m[i][i];
        return s;
    }

    static boolean simmetrikmi(int[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)     // faqat diagonal ustidagi yarmi
                if (m[i][j] != m[j][i]) return false;
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] m = oqi(in, n);
        System.out.println(iz(m));
        System.out.println(simmetrikmi(m) ? "Ha" : "Yoʻq");
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

using Matritsa = vector<vector<int>>;          // qisqa nom

Matritsa oqi(int n) {
    Matritsa m(n, vector<int>(n));
    for (auto& satr : m)
        for (int& x : satr) cin >> x;
    return m;
}

long long iz(const Matritsa& m) {
    long long s = 0;
    for (size_t i = 0; i < m.size(); i++) s += m[i][i];
    return s;
}

bool simmetrikmi(const Matritsa& m) {
    size_t n = m.size();
    for (size_t i = 0; i < n; i++)
        for (size_t j = i + 1; j < n; j++)      // faqat diagonal ustidagi yarmi
            if (m[i][j] != m[j][i]) return false;
    return true;
}

int main() {
    int n;
    cin >> n;
    Matritsa m = oqi(n);
    cout << iz(m) << endl;
    cout << (simmetrikmi(m) ? "Ha" : "Yoʻq") << endl;
    return 0;
}
```

Testlar jadvali:

| Kirish | Kutilgan natija | Nimani tekshiradi |
|---|---|---|
| `3` / `1 2 3` / `2 5 4` / `3 4 9` | `15`, `Ha` | simmetrik matritsa |
| `2` / `1 2` / `3 4` | `5`, `Yoʻq` | simmetrik emas |
| `1` / `-7` | `-7`, `Ha` | 1 × 1 matritsa |
| `2` / `0 0` / `0 0` | `0`, `Ha` | nol matritsa |

`simmetrikmi` ichki siklni `j = i + 1` dan boshlaydi: diagonal elementlarni oʻzi bilan solishtirish va har bir juftlikni ikki marta tekshirish shart emas. C++'dagi `using Matritsa = ...` — uzun turga qisqa nom; funksiya tavsiflarini oʻqishni osonlashtiradi.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: *A* (*n* × *m*) va *B* (*m* × *p*) matritsalarini qabul qilib, **C = A · B** ni qaytaruvchi funksiya yozing: `C[i][j]` — *A* ning *i*-satri va *B* ning *j*-ustuni elementlari koʻpaytmalari yigʻindisi. Topshiriladigan ish:

1. **Funksiyalar tavsifi** — kamida uchta funksiya: matritsani oʻqish, koʻpaytirish, chiqarish; har birining parametrlari va qaytaradigan qiymati.
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 4 ta test: kvadrat matritsalar, toʻrtburchak matritsalar (masalan, 2 × 3 va 3 × 1), birlik matritsaga koʻpaytirish, manfiy elementlar; har biri uchun natija qoʻlda hisoblangan.
4. **Xulosa** — 3–5 gap: koʻpaytirish nechta amal talab qiladi (*n*, *m*, *p* orqali) va nima uchun *A* ustunlari soni *B* satrlari soniga teng boʻlishi shart.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Koʻpaytirish alohida **funksiya** boʻlishi va yangi matritsa qaytarishi shart.
- Natija matritsasi oʻlchami *n* × *p*: ichki sikl *m* gacha boradi — indekslarni almashtirib yubormang.
- `C[i][j]` yigʻindisi har bir katak uchun noldan boshlanadi.
- Elementlar 1000 gacha va *m* 50 gacha: yigʻindi `int` ga sigʻadi, lekin chegarani tekshirishni xulosada koʻrsating.
:::

:::tip
Toʻrtburchak matritsalar bilan test qiling: kvadrat matritsalarda `n`, `m`, `p` ni almashtirib yuborish xatosi koʻrinmay qolishi mumkin.
:::

## Xulosa

- Matritsa funksiyaga massivlar massivi sifatida uzatiladi; oʻlchamlar `len(m)`, `len(m[0])` (yoki `length`, `size()`) bilan aniqlanadi.
- C++'da `const vector<vector<int>>&` — nusxasiz, oʻzgartirishsiz uzatish; statik matritsada ikkinchi oʻlcham yoziladi.
- Funksiyadan yangi matritsa qaytarish uch tilda ham oddiy `return`.
- Oʻqish, hisoblash va chiqarish alohida funksiyalarda boʻlsa, ular boshqa masalalarda ham qayta ishlatiladi.

## Oʻzingizni tekshiring

:::check
Q: Javaʼda `int[][] m` matritsaning ustunlar sonini qanday topish mumkin?
A: `m[0].length` (matritsa boʻsh boʻlmasa).
Q: C++'da `void f(int m[][])` nima uchun kompilyatsiya boʻlmaydi?
A: Ikkinchi oʻlcham koʻrsatilmagan; kompilyator `m[i][j]` manzilini hisoblash uchun satr uzunligini bilishi kerak.
Q: 2 × 3 matritsani 3 × 4 matritsaga koʻpaytirsak, natija oʻlchami qanday?
A: 2 × 4.
Q: Nima uchun `simmetrikmi` ichki siklni `j = i + 1` dan boshlaydi?
A: Diagonal elementlar oʻziga teng va har bir juftlik `(i, j)`–`(j, i)` bir marta tekshirilsa yetarli.
:::

:::link https://numpy.org/doc/stable/reference/generated/numpy.matmul.html
NumPy: matmul
Amalda matritsalar Pythonʼda NumPy kutubxonasi bilan koʻpaytiriladi — shu funksiya hujjati (ingliz tilida).
:::
