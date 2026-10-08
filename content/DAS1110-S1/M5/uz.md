---
title: Bir oʻlchovli massivlar
summary: Massiv nima, indeks va uzunlik, massivni eʼlon qilish va toʻldirish, elementlarni aylanib chiqish, yigʻindi va eng katta element, teskari tartib, chegaradan chiqish xatosi.
---

## Nima uchun bu mavzu muhim?

Guruhda 25 talaba bor. Oʻrtacha balni topish uchun ballarni bitta yigʻuvchiga qoʻshib borish kifoya. Lekin „oʻrtachadan yuqori ball olgan talabalar nechta?“ degan savolga javob berish uchun avval oʻrtachani bilish, keyin har bir ballga **qaytadan** qarash kerak. 25 ta alohida oʻzgaruvchi (`ball1`, `ball2`, …, `ball25`) — noqulay va guruh 30 kishi boʻlsa, dastur ishlamaydi.

Yechim — **massiv**: bitta nom ostida saqlanadigan bir xil turdagi qiymatlar ketma-ketligi. Jurnal, narxlar roʻyxati, kunlik harorat, rasm piksellari — hammasi massiv. Bu darsdan boshlab dasturlar katta hajmdagi maʼlumot bilan ishlay boshlaydi.

:::note
**Bu darsdan soʻng siz:**

- massiv, element, indeks va uzunlik tushunchalarini tushuntira olasiz;
- Python, Java va C++'da massiv yarata, uni klaviaturadan toʻldira va chiqara olasiz;
- massivni sikl bilan aylanib chiqib, yigʻindi, oʻrtacha, eng katta element va uning oʻrnini topa olasiz;
- massivni joyida teskari tartibga keltira olasiz va chegaradan chiqish xatosini oldini olasiz.
:::

## Massiv tushunchasi

**Massiv (array)** — bir xil turdagi elementlarning tartiblangan, nomlangan toʻplami. Har bir element oʻz **indeksi** (tartib raqami) orqali olinadi.

| Tushuncha | Maʼnosi | Misol (`a = [7, 3, 9, 4]`) |
|---|---|---|
| Element | massivdagi bitta qiymat | 9 |
| Indeks | elementning oʻrni, **0 dan** boshlanadi | 9 ning indeksi — 2 |
| Uzunlik (oʻlcham) | elementlar soni | 4 |
| Birinchi element | `a[0]` | 7 |
| Oxirgi element | `a[n - 1]` | 4 |

Indeks 0 dan boshlanishining sababi: indeks — massiv boshidan **siljish**. Birinchi element boshidan 0 qadam uzoqlikda.

Xotirada massiv elementlari **ketma-ket** joylashadi. Shuning uchun `a[i]` ga murojaat juda tez: element manzili = boshlangʻich manzil + *i* · element oʻlchami. Massivning qaysi elementiga murojaat qilishning farqi yoʻq — hammasi bir xil tez.

## Massivni yaratish

| Vazifa | Python (`list`) | Java | C++ (`vector`) |
|---|---|---|---|
| Qiymatlar bilan | `a = [7, 3, 9, 4]` | `int[] a = {7, 3, 9, 4};` | `vector<int> a = {7, 3, 9, 4};` |
| *n* ta nol | `a = [0] * n` | `int[] a = new int[n];` | `vector<int> a(n);` |
| Uzunlik | `len(a)` | `a.length` | `a.size()` |
| Element | `a[i]` | `a[i]` | `a[i]` |

Pythonʼdagi `list` hajmi oʻzgaruvchan: `a.append(x)` oxiriga element qoʻshadi. Javaʼdagi massivning uzunligi yaratilganda belgilanadi va oʻzgarmaydi. C++'da `vector` (`#include <vector>`) — hajmi oʻzgaruvchan massiv; klassik C++ massivi `int a[100];` esa oʻlchami kompilyatsiya paytida maʼlum boʻlgan statik massiv (A9 mashgʻulotida koʻriladi).

## Massivni toʻldirish va aylanib chiqish

Odatda kirish shunday beriladi: birinchi qatorda *n*, ikkinchi qatorda *n* ta son.

- **Python**: `n = int(input())`, `a = list(map(int, input().split()))`.
- **Java, C++**: massiv yaratiladi va sikl bilan *n* ta son oʻqiladi.

Massivni **aylanib chiqish (traversal)** — har bir elementga navbat bilan murojaat qilish:

| Til | Indeks bilan | Faqat qiymatlar |
|---|---|---|
| Python | `for i in range(len(a)):` | `for x in a:` |
| Java | `for (int i = 0; i < a.length; i++)` | `for (int x : a)` |
| C++ | `for (int i = 0; i < (int) a.size(); i++)` | `for (int x : a)` |

Indeks kerak boʻlmasa (yigʻindi, sanash), qisqa shakl qulay. Indeks kerak boʻlsa (oʻrnini topish, qoʻshni elementlar, oʻzgartirish), indeksli sikl ishlatiladi.

## Chegaradan chiqish

Indeks 0 dan *n* − 1 gacha boʻladi. `a[n]` — massivda **yoʻq** element:

| Til | `a[n]` ga murojaat qilinsa |
|---|---|
| Python | `IndexError: list index out of range` |
| Java | `ArrayIndexOutOfBoundsException` |
| C++ | xato xabari **yoʻq**: begona xotira oʻqiladi yoki buziladi — „aniqlanmagan xatti-harakat“ |

C++'dagi bu xususiyat eng xavfli xatolardan biri: dastur baʼzan ishlaydi, baʼzan qulaydi. Xavfsizlik zaifliklarining katta qismi aynan chegaradan chiqishdan kelib chiqadi. Shuning uchun sikl shartini doim `i < n` deb yozing, `i <= n` emas.

Pythonʼda manfiy indeks ham bor: `a[-1]` — oxirgi element, `a[-2]` — oxiridan ikkinchi. Java va C++'da bunday imkoniyat yoʻq.

## Ishlangan misol 1: statistik koʻrsatkichlar

Birinchi qatorda *n* (1 ≤ *n* ≤ 1000), ikkinchi qatorda *n* ta butun son — talabalar ballari. Yigʻindini, oʻrtachani, eng katta ballni va uning tartib raqamini (1 dan boshlab) hamda oʻrtachadan yuqori ball olganlar sonini chiqaring.

```python
n = int(input())
a = list(map(int, input().split()))

yigindi = 0
for x in a:
    yigindi += x
ortacha = yigindi / n

mx_i = 0                          # eng kattasining indeksi
for i in range(1, n):
    if a[i] > a[mx_i]:
        mx_i = i

yuqori = 0
for x in a:
    if x > ortacha:
        yuqori += 1

print("Yigʻindi:", yigindi)
print(f"Oʻrtacha: {ortacha:.2f}")
print("Eng katta:", a[mx_i], "tartib raqami:", mx_i + 1)
print("Oʻrtachadan yuqori:", yuqori)
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

        int yigindi = 0;
        for (int x : a) {
            yigindi += x;
        }
        double ortacha = (double) yigindi / n;

        int mxI = 0;                      // eng kattasining indeksi
        for (int i = 1; i < n; i++) {
            if (a[i] > a[mxI]) {
                mxI = i;
            }
        }

        int yuqori = 0;
        for (int x : a) {
            if (x > ortacha) {
                yuqori++;
            }
        }

        System.out.println("Yigʻindi: " + yigindi);
        System.out.println(String.format("Oʻrtacha: %.2f", ortacha));
        System.out.println("Eng katta: " + a[mxI] + " tartib raqami: " + (mxI + 1));
        System.out.println("Oʻrtachadan yuqori: " + yuqori);
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    int yigindi = 0;
    for (int x : a) {
        yigindi += x;
    }
    double ortacha = (double) yigindi / n;

    int mxI = 0;                      // eng kattasining indeksi
    for (int i = 1; i < n; i++) {
        if (a[i] > a[mxI]) {
            mxI = i;
        }
    }

    int yuqori = 0;
    for (int x : a) {
        if (x > ortacha) {
            yuqori++;
        }
    }

    cout << "Yigʻindi: " << yigindi << endl;
    cout << fixed << setprecision(2) << "Oʻrtacha: " << ortacha << endl;
    cout << "Eng katta: " << a[mxI] << " tartib raqami: " << mxI + 1 << endl;
    cout << "Oʻrtachadan yuqori: " << yuqori << endl;
    return 0;
}
```

Kirish: `5` va `78 92 65 88 70`.

1. Yigʻindi: 78 + 92 + 65 + 88 + 70 = 393; oʻrtacha 393 / 5 = 78.60.
2. Eng kattasi: `mx_i` 0 dan boshlanadi; *i* = 1 da 92 > 78 — `mx_i = 1`; keyingilari 92 dan katta emas. Eng katta — 92, tartib raqami 2.
3. Oʻrtachadan (78.60) yuqori: 92 va 88 — 2 ta.

Chiqadi:

```text
Yigʻindi: 393
Oʻrtacha: 78.60
Eng katta: 92 tartib raqami: 2
Oʻrtachadan yuqori: 2
```

Uchinchi bosqich nima uchun massiv kerakligini koʻrsatadi: oʻrtacha faqat barcha ballar oʻqilgandan keyin maʼlum, keyin esa ballarga **ikkinchi marta** qarash kerak. Eng kattasining qiymati emas, **indeksi** saqlanadi — shunda ham qiymatni (`a[mx_i]`), ham oʻrnini bilamiz.

## Ishlangan misol 2: massivni teskari tartibga keltirish

Birinchi qatorda *n*, ikkinchisida *n* ta son. Massivni **joyida** (yangi massiv yaratmasdan) teskari tartibga keltiring va chiqaring.

**Ikki koʻrsatkich** usuli: chap koʻrsatkich boshidan, oʻng koʻrsatkich oxiridan; ular uchrashguncha elementlar almashtiriladi.

```python
n = int(input())
a = list(map(int, input().split()))

chap, ong = 0, n - 1
while chap < ong:
    a[chap], a[ong] = a[ong], a[chap]
    chap += 1
    ong -= 1

print(*a)
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

        int chap = 0, ong = n - 1;
        while (chap < ong) {
            int t = a[chap];
            a[chap] = a[ong];
            a[ong] = t;
            chap++;
            ong--;
        }

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

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) {
        cin >> a[i];
    }

    int chap = 0, ong = n - 1;
    while (chap < ong) {
        int t = a[chap];
        a[chap] = a[ong];
        a[ong] = t;
        chap++;
        ong--;
    }

    for (int i = 0; i < n; i++) {
        if (i > 0) cout << ' ';
        cout << a[i];
    }
    cout << endl;
    return 0;
}
```

Kirish `5` va `1 2 3 4 5` boʻlsa:

| *chap* | *ong* | almashtirish | massiv |
|---|---|---|---|
| 0 | 4 | 1 ↔ 5 | 5 2 3 4 1 |
| 1 | 3 | 2 ↔ 4 | 5 4 3 2 1 |
| 2 | 2 | sikl tugaydi | 5 4 3 2 1 |

Chiqadi: `5 4 3 2 1`. Juft uzunlikda (masalan, 4 ta) koʻrsatkichlar bir-biridan oʻtib ketganda toʻxtaydi. Sikl *n*/2 marta ishlaydi. Pythonʼda `print(*a)` roʻyxat elementlarini probel bilan chiqaradi; qisqa yoʻl ham bor — `a.reverse()` yoki `a[::-1]`.

:::warning
**Koʻp uchraydigan xatolar**

- **Indeksni 1 dan boshlash.** Birinchi element — `a[0]`; `for i in range(1, n + 1)` oxirida chegaradan chiqadi.
- **`i <= n` shart.** Oxirgi iteratsiyada `a[n]` — Python va Javaʼda xato, C++'da jimgina begona xotira.
- **Eng kattasini 0 dan boshlash.** Barcha sonlar manfiy boʻlsa, javob notoʻgʻri 0 chiqadi. Birinchi elementdan boshlang.
- **Teskari tartibda sikl oxirigacha almashtirish.** *n* marta almashtirilsa, massiv ikki marta aylanib, avvalgi holiga qaytadi.
- **Javaʼda `a.length()`.** Massiv uchun `length` — qavssiz; `length()` satr uchun.
:::

:::tip
Massiv bilan ishlaydigan dasturni uch holatda tekshiring: bitta element, ikki element (juft) va uch element (toq). Chegaradagi xatolarning deyarli hammasi shu kichik holatlarda koʻrinadi.
:::

## Xulosa

- Massiv — bir xil turdagi elementlarning nomlangan, tartiblangan toʻplami; elementlar xotirada ketma-ket joylashadi.
- Indeks 0 dan *n* − 1 gacha; `a[i]` ga murojaat har qanday *i* uchun bir xil tez.
- Pythonʼda `list`, Javaʼda `int[]` (uzunligi oʻzgarmas), C++'da `vector<int>`; uzunlik: `len(a)`, `a.length`, `a.size()`.
- Aylanib chiqish: indeksli sikl yoki „har bir element uchun“ sikli.
- Eng katta elementning indeksini saqlang; boshlangʻich qiymat — birinchi element.
- Chegaradan chiqish Python va Javaʼda xato beradi, C++'da esa jimgina xotirani buzadi.

## Oʻzingizni tekshiring

:::check
Q: `a = [4, 8, 15, 16, 23, 42]` boʻlsa, `a[2]`, `a[5]` va `len(a)` nimaga teng?
A: 15, 42 va 6.
Q: Nima uchun massiv indeksi 0 dan boshlanadi?
A: Indeks — massiv boshidan siljish: birinchi element boshidan 0 element uzoqlikda.
Q: Javaʼda `int[] a = new int[5];` dan keyin `a[5] = 1;` nima beradi?
A: `ArrayIndexOutOfBoundsException`: indekslar 0 dan 4 gacha.
Q: Eng katta elementni qidirishda nima uchun `mx = 0` dan emas, birinchi elementdan boshlash kerak?
A: Barcha elementlar manfiy boʻlsa, 0 hech qaysi elementdan kichik boʻlmaydi va javob notoʻgʻri 0 chiqadi.
Q: 7 ta elementli massivni teskari tartibga keltirishda nechta almashtirish bajariladi?
A: 3 ta (7 // 2): oʻrtadagi element joyida qoladi.
:::

:::link https://docs.python.org/3/tutorial/datastructures.html#more-on-lists
Python: roʻyxatlar (list)
Roʻyxat metodlari: `append`, `reverse`, `sort` va boshqalar.
:::

:::link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
Oracle Java Tutorial: massivlar
Javaʼda massivni eʼlon qilish, yaratish va ishlatish.
:::

:::link https://visualgo.net/en/list
VisuAlgo: massiv va roʻyxat
Massiv amallarining vizualizatsiyasi.
:::
