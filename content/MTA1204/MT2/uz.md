---
title: Mustaqil ish va uyga vazifalarni bajarish
summary: Mustaqil ish: algoritmik vazifani bajarish tartibi — shartni tahlil qilish, chekka holatlar, sekin va tez yechim; stress-test — tasodifiy testlar bilan tez yechimni ishonchli yechim bilan solishtirish; takrorlanadigan tasodifiy sonlar generatori; hisobot tuzish.
---

## Nima uchun bu mavzu muhim?

Algoritmik vazifada eng koʻp vaqt kod yozishga emas, **xatoni topishga** ketadi. Dastur namunaviy testdan oʻtadi, lekin avtomatik tekshiruvda „notoʻgʻri javob“ oladi — qaysi kirishda? Professional dasturchilar va olimpiada ishtirokchilari buning uchun **stress-test** usulidan foydalanadi: tez (lekin xato boʻlishi mumkin) yechim minglab tasodifiy kirishda sekin, lekin aniq toʻgʻri yechim bilan solishtiriladi. Birinchi farq — xatoni koʻrsatuvchi kichik misol.

:::note
**Bu mustaqil ishdan soʻng siz:**

- algoritmik vazifani bosqichma-bosqich bajara olasiz: tahlil, sekin yechim, tez yechim, tekshiruv;
- stress-test dasturini yozib, xatoni koʻrsatuvchi kirishni topa olasiz;
- har safar bir xil natija beradigan tasodifiy sonlar generatoridan foydalana olasiz;
- bajarilgan ish boʻyicha qisqa hisobot tuza olasiz.
:::

## Vazifani bajarish tartibi

1. **Shartni tahlil qilish** — kirish, chiqish, cheklovlar. Cheklovlardan kerakli murakkablikni aniqlang: *n* ≤ 10⁵ boʻlsa, *O*(*n*²) yaramaydi.
2. **Chekka holatlar roʻyxati** — bitta element, barcha elementlar teng, eng katta qiymatlar, boʻsh natija.
3. **Sekin yechim** — eng oddiy, aniq toʻgʻri usul (toʻliq tanlash, ichma-ich sikllar). U tez boʻlishi shart emas.
4. **Tez yechim** — cheklovlarga mos algoritm.
5. **Tekshiruv** — namunaviy testlar, chekka holatlar va stress-test.
6. **Hisobot** — algoritm, murakkablik, testlar va xulosa.

## Stress-test

Stress-test uch qismdan iborat:

| Qism | Vazifasi |
|---|---|
| Generator | kichik tasodifiy kirish yaratadi (kichik — xato topilsa, uni qoʻlda tahlil qilish oson boʻlsin) |
| Ishonchli yechim | sekin, lekin sodda va aniq toʻgʻri |
| Solishtiruvchi | ikkala yechimni bir xil kirishda ishga tushirib, natijalarni taqqoslaydi |

Takrorlanuvchanlik uchun tasodifiy sonlar **urugʻ** (seed) dan hisoblanadi: bir xil urugʻ — bir xil testlar ketma-ketligi. Tillar standart generatorlari turlicha ishlagani uchun quyida oddiy **chiziqli kongruent generator** ishlatiladi:

```text
holat = (holat · 1103515245 + 12345) mod 2³¹
```

Bu formula uchala tilda ham aynan bir xil sonlarni beradi — xatoli testni boshqa tilda ham qayta yaratish mumkin.

## Namunaviy masala: binar qidiruvdagi xatoni ushlash

Vazifa: saralangan massivda *x* dan **kichik** elementlar sonini topish (quyi chegara). Talaba binar qidiruvni yozdi, lekin bitta belgida adashdi. Stress-test bu xatoni topadi: urugʻ va testlar soni kiritiladi; har bir testda uzunligi 1–8 boʻlgan, 0–9 qiymatli tasodifiy saralangan massiv va *x* yaratiladi; ishonchli yechim — oddiy sanash.

```python
holat = 0


def tasodifiy():
    global holat
    holat = (holat * 1103515245 + 12345) % 2147483648   # chiziqli kongruent generator
    return holat


def ishonchli(a, x):                        # sekin, lekin aniq toʻgʻri
    return sum(1 for v in a if v < x)


def tez(a, x):                              # binar qidiruv — xatoli!
    chap, ong = 0, len(a)
    while chap < ong:
        orta = (chap + ong) // 2
        if a[orta] <= x:                    # xato: < boʻlishi kerak edi
            chap = orta + 1
        else:
            ong = orta
    return chap


holat, testlar = map(int, input().split())
for t in range(1, testlar + 1):
    n = tasodifiy() % 8 + 1
    a = sorted(tasodifiy() % 10 for _ in range(n))
    x = tasodifiy() % 10
    kutilgan, olingan = ishonchli(a, x), tez(a, x)
    if kutilgan != olingan:
        print(f"Xato topildi: test {t}")
        print("a =", *a)
        print("x =", x)
        print(f"kutilgan {kutilgan}, olingan {olingan}")
        break
else:
    print(f"Hammasi toʻgʻri: {testlar} ta test")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static long holat;

    static long tasodifiy() {
        holat = (holat * 1103515245L + 12345) % 2147483648L;    // chiziqli kongruent generator
        return holat;
    }

    static int ishonchli(int[] a, int x) {          // sekin, lekin aniq toʻgʻri
        int soni = 0;
        for (int v : a) if (v < x) soni++;
        return soni;
    }

    static int tez(int[] a, int x) {                // binar qidiruv — xatoli!
        int chap = 0, ong = a.length;
        while (chap < ong) {
            int orta = (chap + ong) / 2;
            if (a[orta] <= x) chap = orta + 1;      // xato: < boʻlishi kerak edi
            else ong = orta;
        }
        return chap;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        holat = in.nextLong();
        int testlar = in.nextInt();
        for (int t = 1; t <= testlar; t++) {
            int n = (int) (tasodifiy() % 8) + 1;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = (int) (tasodifiy() % 10);
            Arrays.sort(a);
            int x = (int) (tasodifiy() % 10);
            int kutilgan = ishonchli(a, x), olingan = tez(a, x);
            if (kutilgan != olingan) {
                System.out.println("Xato topildi: test " + t);
                StringBuilder sb = new StringBuilder("a =");
                for (int v : a) sb.append(' ').append(v);
                System.out.println(sb);
                System.out.println("x = " + x);
                System.out.println("kutilgan " + kutilgan + ", olingan " + olingan);
                return;
            }
        }
        System.out.println("Hammasi toʻgʻri: " + testlar + " ta test");
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

long long holat;

long long tasodifiy() {
    holat = (holat * 1103515245LL + 12345) % 2147483648LL;     // chiziqli kongruent generator
    return holat;
}

int ishonchli(const vector<int>& a, int x) {        // sekin, lekin aniq toʻgʻri
    int soni = 0;
    for (int v : a) if (v < x) soni++;
    return soni;
}

int tez(const vector<int>& a, int x) {              // binar qidiruv — xatoli!
    int chap = 0, ong = a.size();
    while (chap < ong) {
        int orta = (chap + ong) / 2;
        if (a[orta] <= x) chap = orta + 1;          // xato: < boʻlishi kerak edi
        else ong = orta;
    }
    return chap;
}

int main() {
    int testlar;
    cin >> holat >> testlar;
    for (int t = 1; t <= testlar; t++) {
        int n = tasodifiy() % 8 + 1;
        vector<int> a(n);
        for (int& v : a) v = tasodifiy() % 10;
        sort(a.begin(), a.end());
        int x = tasodifiy() % 10;
        int kutilgan = ishonchli(a, x), olingan = tez(a, x);
        if (kutilgan != olingan) {
            cout << "Xato topildi: test " << t << endl;
            cout << "a =";
            for (int v : a) cout << ' ' << v;
            cout << endl << "x = " << x << endl;
            cout << "kutilgan " << kutilgan << ", olingan " << olingan << endl;
            return 0;
        }
    }
    cout << "Hammasi toʻgʻri: " << testlar << " ta test" << endl;
    return 0;
}
```

`1 100` kiritilsa, chiqish:

```text
Xato topildi: test 5
a = 2 4 5 6 9 9
x = 4
kutilgan 1, olingan 2
```

Endi xatoni qoʻlda tahlil qilish oson: massivda 4 ga teng element bor va xatoli yechim uni ham „kichik“ deb sanadi — `<=` aslida **yuqori** chegarani hisoblaydi. `<` ga tuzatilgach, stress-test `Hammasi toʻgʻri` deydi. `42 1` (bitta test) uchun xato chiqmaydi — namunaviy testdan oʻtgan dastur hali toʻgʻri degani emas: `42 3` da xato 2-testdayoq topiladi.

## Mustaqil ish topshirigʻi

Kursdagi amaliy topshiriqlardan **ikkitasini** tanlang (masalan, piramidal saralash va Fenvik daraxti) va ular uchun:

1. **Sekin yechim** — eng oddiy toʻgʻri usulni yozing (masalan, tayyor `sort` yoki toʻliq sikl bilan yigʻindi).
2. **Stress-test** — kamida 1000 ta tasodifiy kichik test bilan tez yechimingizni sekin yechim bilan solishtiring; generator urugʻini hisobotga yozing.
3. **Xato tarixi** — stress-test topgan (yoki ataylab kiritilgan) bitta xatoni tasvirlang: kirish, kutilgan va olingan natija, sababi va tuzatish.
4. **Hisobot** — har bir topshiriq uchun: algoritm gʻoyasi, murakkablik, testlar jadvali, stress-test natijasi va 3–5 gapli xulosa.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Sekin yechim tez yechimdan **mustaqil** boʻlishi kerak: bir xil gʻoyani ikki marta yozish bir xil xatoni takrorlaydi.
- Generator kichik kirishlar yaratsin: 10⁵ elementli farqli test qoʻlda tahlil qilib boʻlmaydi.
- Urugʻsiz tasodifiy testlar xatoni qayta yaratishga imkon bermaydi — urugʻni albatta saqlang.
- Hisobotda faqat „hamma testdan oʻtdi“ deb yozish yetarli emas: qanday testlar va nechta ekanini koʻrsating.
:::

:::tip
Stress-testni vazifaning boshidayoq tayyorlab qoʻying: tez yechimni yozishingiz bilanoq uni ishga tushiring. Xato qanchalik erta topilsa, uni tuzatish shunchalik oson.
:::

## Xulosa

- Vazifa tartibi: tahlil → chekka holatlar → sekin yechim → tez yechim → tekshiruv → hisobot.
- Stress-test tez yechimni minglab tasodifiy kichik kirishda ishonchli yechim bilan solishtiradi va xatoni koʻrsatuvchi misolni topadi.
- Urugʻli chiziqli kongruent generator testlarni har safar va har qanday tilda bir xil qayta yaratadi.
- Namunaviy testdan oʻtish dastur toʻgʻriligini kafolatlamaydi.

## Oʻzingizni tekshiring

:::check
Q: Stress-testda „ishonchli yechim“ qanday boʻlishi kerak?
A: Sodda, aniq toʻgʻri va tez yechimdan mustaqil; tezligi muhim emas.
Q: Nima uchun generator kichik kirishlar yaratadi?
A: Xato topilganda uni qoʻlda tahlil qilish oson boʻlishi uchun.
Q: Tasodifiy testlarda urugʻ nima uchun kerak?
A: Xatoli testni aynan qayta yaratish uchun.
Q: Namunadagi xatoli binar qidiruv nimani hisoblaydi?
A: *x* dan kichik yoki teng elementlar sonini — yuqori chegarani.
:::

:::link https://en.wikipedia.org/wiki/Linear_congruential_generator
Chiziqli kongruent generator
Oddiy psevdotasodifiy sonlar generatori va uning xossalari (ingliz tilida).
:::
