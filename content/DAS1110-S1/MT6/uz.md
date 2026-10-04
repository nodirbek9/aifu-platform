---
title: Ichma-ich joylashgan siklik jarayonlarni tashkil etish
summary: Mustaqil ish: barcha juftliklarni ichma-ich sikl bilan koʻrib chiqish, i < j sharti, iteratsiyalar sonini baholash; namunaviy masala — massivdagi inversiyalar soni.
---

## Nima uchun bu mavzu muhim?

Turnirda har bir jamoa har biri bilan bir marta oʻynaydi. Nechta oʻyin boʻladi va ularni qanday sanab chiqish mumkin? Bu — **barcha juftliklarni koʻrib chiqish** masalasi va u ichma-ich sikl bilan yechiladi. Xuddi shu andoza oʻxshash hujjatlarni qidirishda, eng yaqin ikki nuqtani topishda, saralangan-saralanmaganlikni oʻlchashda ishlatiladi. Bu mustaqil ishda juftliklar bilan ishlashni va dastur qancha ish bajarishini baholashni mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- massivdagi barcha (*i*, *j*) juftliklarni *i* < *j* sharti bilan takrorlanmasdan koʻrib chiqa olasiz;
- ichki sikl chegarasini tashqi hisoblagichga bogʻlay olasiz;
- ichki tana necha marta bajarilishini hisoblay olasiz (*n*(*n* − 1)/2);
- kichik kirish uchun barcha juftliklarni qoʻlda sanab, dasturni tekshira olasiz.
:::

## Mavzu boʻyicha qisqa takror

Barcha juftliklarni koʻrib chiqishning ikki usuli:

| Usul | Sikllar | Juftliklar soni | Muammo |
|---|---|---|---|
| Hamma bilan hamma | *i*: 0 … *n* − 1, *j*: 0 … *n* − 1 | *n*² | (*i*, *i*) va (*j*, *i*) ham kiradi |
| Faqat *i* < *j* | *i*: 0 … *n* − 1, *j*: *i* + 1 … *n* − 1 | *n*(*n* − 1)/2 | — |

Ikkinchi usulda har bir juftlik **aynan bir marta** uchraydi. Ichki sikl *i* + 1 dan boshlanadi — chegara tashqi hisoblagichga bogʻliq.

*n*(*n* − 1)/2 tez oʻsadi: *n* = 10 da 45, *n* = 1000 da ~500 ming, *n* = 100 000 da ~5 milliard juftlik. Shuning uchun katta *n* da ichma-ich sikl sekin ishlaydi va tezroq algoritm kerak boʻladi (saralash, xesh-jadvallar — keyingi semestrlarda).

## Namunaviy masala: inversiyalar soni

Birinchi qatorda *n*, ikkinchisida *n* ta son. **Inversiyalar** sonini toping: *i* < *j* va *aᵢ* > *aⱼ* boʻlgan juftliklar. Inversiyalar soni massiv saralangan holatdan qanchalik „uzoq“ ekanini koʻrsatadi: saralangan massivda 0, teskari tartiblangan massivda *n*(*n* − 1)/2.

```text
1. n va a ni oʻqi
2. soni = 0
3. har bir i = 0 … n-2 uchun:
       har bir j = i+1 … n-1 uchun:
           agar a[i] > a[j]: soni = soni + 1
4. soni ni chiqar
```

```python
n = int(input())
a = list(map(int, input().split()))

soni = 0
for i in range(n):
    for j in range(i + 1, n):
        if a[i] > a[j]:
            soni += 1

print(soni)
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

        long soni = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (a[i] > a[j]) {
                    soni++;
                }
            }
        }

        System.out.println(soni);
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

    long long soni = 0;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (a[i] > a[j]) {
                soni++;
            }
        }
    }

    cout << soni << endl;
    return 0;
}
```

Kirish `5` va `3 1 4 2 5` boʻlsa, barcha 10 ta juftlikdan inversiyalar:

| Juftlik (qiymatlar) | Inversiyami? |
|---|---|
| (3, 1) | ha |
| (3, 2) | ha |
| (4, 2) | ha |
| qolgan 7 ta | yoʻq |

Chiqadi: `3`. Kirish `4` va `4 3 2 1` boʻlsa — barcha 6 ta juftlik inversiya, chiqadi `6` = 4 · 3 / 2. Hisoblagich `long` turida: *n* katta boʻlsa, inversiyalar soni `int` dan oshishi mumkin.

## Mustaqil ish topshirigʻi

Quyida berilgan **yigʻindisi *k* ga teng juftliklar** masalasini yeching. Topshiriladigan ish:

1. **Qoʻlda sanash** — *a* = (1, 5, 3, 3, 2), *k* = 6 uchun barcha 10 ta juftlikni jadvalga yozing va mos keladiganlarini belgilang.
2. **Iteratsiyalar soni** — *n* = 5, 100, 10 000 uchun ichki sikl tanasi necha marta bajariladi? Dasturingiz *n* = 10 000 da qancha vaqt ishlashini taxmin qiling.
3. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
4. **Testlar** — kamida 3 ta: mos juftlik yoʻq, bir xil sonlar bor (masalan, 3 + 3), barcha juftliklar mos.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Ichki sikl `j = 0` dan boshlansa, har bir juftlik ikki marta sanaladi va (*i*, *i*) ham kiradi — `j = i + 1` dan boshlang.
- Bir xil qiymatli, lekin turli oʻrindagi elementlar (3 va 3) — alohida juftlik hisoblanadi.
- Ichki va tashqi siklda turli hisoblagich nomlari (`i`, `j`) boʻlishi shart.
- Qoʻlda sanash jadvali dasturdan koʻchirilmasligi kerak.
:::

:::tip
Avval *n* = 3 uchun barcha juftliklarni yozing: (0, 1), (0, 2), (1, 2). Dasturingiz ichki tanada aynan shu uchta juftlikni koʻrayotganini `print` bilan tekshirib, keyin bu chiqarishni olib tashlang.
:::

## Xulosa

- Barcha juftliklar *i* < *j* sharti bilan koʻrib chiqiladi: ichki sikl *i* + 1 dan boshlanadi.
- Juftliklar soni *n*(*n* − 1)/2 — *n* oshganda tez oʻsadi.
- Hisoblagich katta boʻlishi mumkin — keng tur (`long`) tanlang.
- Kichik kirish uchun juftliklarni qoʻlda sanab, dasturni tekshiring.

## Oʻzingizni tekshiring

:::check
Q: 8 ta jamoali turnirda (har bir jamoa har biri bilan bir marta) nechta oʻyin boʻladi?
A: 28: 8 · 7 / 2.
Q: Saralangan massivda nechta inversiya bor?
A: 0: hech qaysi juftlikda oldingi element keyingisidan katta emas.
Q: Agar ichki sikl `j = i` dan boshlansa, inversiyalar soni oʻzgaradimi?
A: Yoʻq, chunki `a[i] > a[i]` hech qachon rost emas; lekin keraksiz *n* ta tekshiruv qoʻshiladi. Juftlik yigʻindisi masalasida esa (*i*, *i*) xato natija berishi mumkin.
:::

:::link https://en.wikipedia.org/wiki/Inversion_(discrete_mathematics)
Wikipedia: inversiya
Ketma-ketlikdagi inversiyalar va ularning saralash bilan bogʻliqligi.
:::
