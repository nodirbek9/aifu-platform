---
title: Massivlarni tashkil etish
summary: Mustaqil ish: massiv bilan ishlaydigan dastur — kiritish, indekslar bilan ishlash, chiqarish va testlar; namunaviy masala — massivni k qadamga siklik siljitish.
---

## Nima uchun bu mavzu muhim?

Navbat tizimi, musiqa pleylisti, aylana boʻylab joylashgan oʻyinchilar — bunday maʼlumotlar massivda saqlanadi va koʻpincha elementlarning **oʻrni** bilan ishlash kerak boʻladi: aylantirish, teskari qilish, siljitish. Bu amallarning hammasi indekslar orasidagi toʻgʻri bogʻlanishni topishga tayanadi. Bu mustaqil ishda shunday bogʻlanishni mustaqil topib, uni dasturda qoʻllaysiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- massivni klaviaturadan toʻldirib, uni bir qatorda chiqara olasiz;
- eski va yangi indekslar orasidagi formulani topa olasiz;
- `%` amali bilan „aylana“ indekslardan foydalana olasiz;
- chegaraviy holatlar (1 ta element, *k* = 0, *k* ≥ *n*) uchun testlar tuza olasiz.
:::

## Mavzu boʻyicha qisqa takror

| Amal | Python | Java | C++ |
|---|---|---|---|
| *n* ta sonni oʻqish | `a = list(map(int, input().split()))` | sikl bilan `nextInt()` | sikl bilan `cin >>` |
| Uzunlik | `len(a)` | `a.length` | `a.size()` |
| Oxirgi element | `a[n - 1]` yoki `a[-1]` | `a[n - 1]` | `a[n - 1]` |
| Bir qatorda chiqarish | `print(*a)` | `StringBuilder` va probellar | sikl va probellar |

Elementlarning oʻrnini oʻzgartiradigan masalada eng muhim savol: ***i*-oʻrindagi element qayerga borishi kerak?** Javob formula koʻrinishida yoziladi. Masalan, teskari tartibda *i*-element *n* − 1 − *i* oʻringa boradi.

## Namunaviy masala: siklik siljitish

Birinchi qatorda *n*, ikkinchisida *n* ta son, uchinchisida *k* (0 ≤ *k* ≤ 10⁹). Massivni oʻngga *k* qadam **siklik** siljiting: oxiridan chiqqan element boshiga qaytadi.

Tahlil: oʻngga bir qadam siljitishda *i*-element *i* + 1 oʻringa, oxirgisi esa 0-oʻringa boradi. *k* qadamda — (*i* + *k*) oʻringa, lekin „aylana“ boʻylab: **(*i* + *k*) mod *n***. *k* juda katta boʻlsa ham formula ishlaydi: *n* qadam siljitish massivni oʻz holiga qaytaradi.

```text
1. n, a, k ni oʻqi
2. b — n ta elementli yangi massiv
3. har bir i = 0 … n-1 uchun: b[(i + k) mod n] = a[i]
4. b ni chiqar
```

```python
n = int(input())
a = list(map(int, input().split()))
k = int(input())

b = [0] * n
for i in range(n):
    b[(i + k) % n] = a[i]

print(*b)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        long k = in.nextLong();

        int[] b = new int[n];
        for (int i = 0; i < n; i++) {
            b[(int) ((i + k) % n)] = a[i];
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(b[i]);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }
    long long k;
    cin >> k;

    vector<int> b(n);
    for (int i = 0; i < n; i++) {
        b[(i + k) % n] = a[i];
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << b[i];
    }
    cout << endl;
    return 0;
}
```

Kirish `5`, `1 2 3 4 5`, `2` boʻlsa:

| *i* | `a[i]` | (*i* + 2) mod 5 |
|---|---|---|
| 0 | 1 | 2 |
| 1 | 2 | 3 |
| 2 | 3 | 4 |
| 3 | 4 | 0 |
| 4 | 5 | 1 |

Chiqadi: `4 5 1 2 3`. *k* = 7 uchun ham xuddi shu natija (7 mod 5 = 2), *k* = 5 uchun — massiv oʻzgarmaydi. *k* `long` turida oʻqiladi, chunki 10⁹ + *i* `int` chegarasiga yaqin.

## Mustaqil ish topshirigʻi

Quyida berilgan **massivni teskari tartibda chiqarish** masalasini yeching. Topshiriladigan ish:

1. **Formula** — *i*-element teskari tartibda qaysi oʻringa borishini yozing va 5 ta elementli misolda jadval bilan koʻrsating (yuqoridagi jadvaldek).
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar** — kamida 4 ta: 1 ta element, juft uzunlik, toq uzunlik, manfiy sonlar; har biri uchun kutilgan natija.
4. **Ikki usulni solishtirish** — yangi massivga yozish va joyida almashtirish (ikki koʻrsatkich): qaysi biri qancha qoʻshimcha xotira talab qiladi?

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Indeks 0 dan *n* − 1 gacha; `a[n]` ga murojaat — chegaradan chiqish.
- Chiqish formati shartdagi kabi boʻlishi shart (elementlar bitta probel bilan, bitta qatorda).
- Joyida almashtirishda sikl *n*/2 marta ishlashi kerak, *n* marta emas.
- Formula va jadval qoʻlda tuzilgan boʻlishi kerak.
:::

:::tip
Formulani topish uchun 5 ta elementli massivni qogʻozga yozing va har bir element qayerga borishini strelka bilan koʻrsating. Indekslar orasidagi bogʻlanish oʻzi koʻrinadi.
:::

## Xulosa

- Elementlar oʻrnini oʻzgartiradigan masalada „*i*-element qayerga boradi?“ degan formula topiladi.
- Siklik siljitishda yangi oʻrin — (*i* + *k*) mod *n*; *k* ning katta qiymatlari ham toʻgʻri ishlaydi.
- Teskari tartibda *i*-element *n* − 1 − *i* oʻringa boradi.
- Testlarda 1 ta element, juft va toq uzunliklar albatta boʻlsin.

## Oʻzingizni tekshiring

:::check
Q: 6 ta elementli massiv oʻngga 8 qadam siljitilsa, 0-element qaysi oʻringa boradi?
A: 2-oʻringa: (0 + 8) mod 6 = 2.
Q: Teskari tartibda 7 ta elementli massivning 2-indeksli elementi qaysi oʻringa boradi?
A: 4-oʻringa: 7 − 1 − 2 = 4.
Q: Massivni chapga *k* qadam siljitish uchun formula qanday boʻladi?
A: Yangi oʻrin — (*i* − *k*) mod *n*; manfiy qiymatdan saqlanish uchun (*i* − *k* mod *n* + *n*) mod *n*.
:::

:::link https://visualgo.net/en/list
VisuAlgo: massiv amallari
Massiv elementlari bilan amallarning vizualizatsiyasi.
:::
