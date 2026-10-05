---
title: Algoritm tushunchasi va uning xususiyatlari
summary: Mustaqil ish — algoritm tushunchasi va tarixi (al-Xorazmiy); algoritm xossalari — diskretlik, aniqlik, natijaviylik, ommaviylik, tushunarlilik; tasvirlash usullari (soʻzli, blok-sxema, psevdokod, dastur); asosiy tuzilmalar (chiziqli, tarmoqlanuvchi, takrorlanuvchi); algoritm samaradorligi va murakkablik (O(1), O(log n), O(n), O(n²)); chiziqli va ikkilik qidiruvni solishtiruvchi dastur; maʼlumotlar bazasi indekslari bilan bogʻliqlik; topshiriqlar.
---

## Mustaqil ishning maqsadi

M8 da algoritm va dasturlash asoslarini koʻrdik. Bu mustaqil ishda algoritm tushunchasining oʻzini chuqurroq oʻrganamiz: qanday koʻrsatma algoritm hisoblanadi, uni qanday tasvirlash mumkin va bir xil masalani hal qiluvchi ikki algoritmdan qaysi biri yaxshiroq.

:::note
**Bu mustaqil ishdan soʻng siz:**

- algoritm taʼrifi va beshta xossasini ayta olasiz;
- algoritmni turli usullarda tasvirlay olasiz;
- uchta asosiy tuzilmani farqlay olasiz;
- algoritm samaradorligini taqqoslashlar soni orqali baholay olasiz.
:::

## Tushuncha va tarix

**Algoritm** — qoʻyilgan masalani hal qilish uchun bajariluvchiga beriladigan, cheklangan sonli qadamlardan iborat aniq va tushunarli koʻrsatmalar ketma-ketligi. Bajaruvchi — odam, kompyuter, robot yoki bankomat boʻlishi mumkin.

Soʻz IX asrda yashagan buyuk vatandoshimiz **Muhammad ibn Muso al-Xorazmiy** nomidan kelib chiqqan: uning hind raqamlari bilan hisoblash haqidagi asari lotin tilida "Algoritmi de numero Indorum" deb tarjima qilingan. Al-Xorazmiyning "al-jabr" haqidagi asari esa "algebra" soʻziga asos boʻlgan.

## Algoritm xossalari

| Xossa | Mazmuni | Buzilish misoli |
|---|---|---|
| **Diskretlik** | algoritm alohida, ketma-ket bajariladigan qadamlarga boʻlinadi | "Hisobotni tayyorla" — bitta katta, boʻlinmagan buyruq |
| **Aniqlik** (determinantlik) | har bir qadam bir maʼnoli; bir xil kirishda bir xil natija | "Narxni biroz oshiring" — "biroz" qancha? |
| **Natijaviylik** (chekli) | cheklangan qadamdan soʻng natija beradi | toʻxtash sharti yoʻq sikl |
| **Ommaviylik** | bir turdagi masalalarning butun sinfiga, turli kirish maʼlumotlariga yaroqli | faqat bitta aniq mijoz uchun yozilgan hisob |
| **Tushunarlilik** | bajaruvchi bajara oladigan buyruqlardan iborat | kompyuterga "Mijozning kayfiyatini sez" |

## Tasvirlash usullari

| Usul | Afzalligi | Kamchiligi |
|---|---|---|
| **Soʻzli** | hamma tushunadi | uzun, noaniqlikka moyil |
| **Blok-sxema** (S8, MT11) | tuzilma koʻrinadi | katta algoritmlar uchun noqulay |
| **Psevdokod** | qisqa, dasturga yaqin, tilga bogʻliq emas | qatʼiy standart yoʻq |
| **Dastur** | kompyuter bajaradi | dasturlash tilini bilish kerak (MT12) |

**Bitta algoritm uch xil koʻrinishda** — omborda mahsulot qoldigʻini tekshirish:

```text
Soʻzli:    Agar qoldiq minimal zaxiradan kam boʻlsa, yetkazib beruvchiga buyurtma ber,
           aks holda hech narsa qilma.

Psevdokod: AGAR qoldiq < min_zaxira U HOLDA
               buyurtma_berish(min_zaxira * 2 - qoldiq)
           TUGADI

Dastur:    if qoldiq < min_zaxira:
               buyurtma_berish(min_zaxira * 2 - qoldiq)
```

## Asosiy tuzilmalar

Har qanday algoritmni uchta tuzilma bilan qurish mumkin (Böhm–Jacopini teoremasi):

- **Chiziqli** (ketma-ketlik) — qadamlar birin-ketin: maʼlumotni oʻqi → hisobla → chiqar.
- **Tarmoqlanuvchi** (shart) — shartga qarab turli yoʻl (MT13, S8).
- **Takrorlanuvchi** (sikl) — qadamlar shart bajarilguncha yoki belgilangan marta takrorlanadi (S15).

## Algoritm samaradorligi

Bir masalani turli algoritmlar hal qilishi mumkin, lekin ular sarflaydigan vaqt keskin farq qiladi. Samaradorlik kirish hajmi **n** ga nisbatan qadamlar soni bilan baholanadi:

| Murakkablik | Nomi | n = 1 000 | n = 1 000 000 | Misol |
|---|---|---|---|---|
| **O(1)** | doimiy | 1 | 1 | jadvaldagi elementni raqami boʻyicha olish |
| **O(log n)** | logarifmik | ~10 | ~20 | tartiblangan roʻyxatda ikkilik qidiruv |
| **O(n)** | chiziqli | 1 000 | 1 000 000 | roʻyxatni boshidan oxirigacha koʻrish |
| **O(n²)** | kvadratik | 1 000 000 | 10¹² | har bir mijozni har biri bilan solishtirish |

**Ikkilik qidiruv** gʻoyasi — telefon kitobidan qidirish: oʻrtasini ochasiz, kerakli nom oldinda boʻlsa — chap yarmiga, keyinda boʻlsa — oʻng yarmiga oʻtasiz. Har qadamda qidiruv maydoni ikki barobar qisqaradi. Shart: roʻyxat **tartiblangan** boʻlishi kerak.

## Ishlangan misol: chiziqli va ikkilik qidiruv

Birinchi satrda — tartiblangan narxlar roʻyxati, ikkinchisida — qidiriladigan narxlar. Har biri uchun oʻrnini (yoki topilmaganini) hamda chiziqli va ikkilik qidiruvdagi taqqoslashlar sonini chiqaring.

```python
import sys

def chiziqli(roy, x):
    qadam = 0
    for i, v in enumerate(roy):
        qadam += 1
        if v == x:
            return i, qadam
    return -1, qadam

def ikkilik(roy, x):
    past, yuqori, qadam = 0, len(roy) - 1, 0
    while past <= yuqori:
        orta = (past + yuqori) // 2
        qadam += 1
        if roy[orta] == x:
            return orta, qadam
        if roy[orta] < x:
            past = orta + 1                 # oʻng yarmida qidirish
        else:
            yuqori = orta - 1               # chap yarmida qidirish
    return -1, qadam

satrlar = sys.stdin.read().strip().split("\n")
narxlar = [int(v) for v in satrlar[0].split()]
for x in [int(v) for v in satrlar[1].split()]:
    joy, a = chiziqli(narxlar, x)
    _, b = ikkilik(narxlar, x)
    natija = f"oʻrin {joy + 1}" if joy >= 0 else "topilmadi"
    print(f"{x}: {natija}, chiziqli {a}, ikkilik {b} taqqoslash")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static int[] chiziqli(int[] roy, int x) {
        int qadam = 0;
        for (int i = 0; i < roy.length; i++) {
            qadam++;
            if (roy[i] == x) return new int[]{i, qadam};
        }
        return new int[]{-1, qadam};
    }

    static int[] ikkilik(int[] roy, int x) {
        int past = 0, yuqori = roy.length - 1, qadam = 0;
        while (past <= yuqori) {
            int orta = (past + yuqori) / 2;
            qadam++;
            if (roy[orta] == x) return new int[]{orta, qadam};
            if (roy[orta] < x) past = orta + 1;          // oʻng yarmida qidirish
            else yuqori = orta - 1;                      // chap yarmida qidirish
        }
        return new int[]{-1, qadam};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] narxlar = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int[] soralgan = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        for (int x : soralgan) {
            int[] a = chiziqli(narxlar, x);
            int[] b = ikkilik(narxlar, x);
            String natija = a[0] >= 0 ? "oʻrin " + (a[0] + 1) : "topilmadi";
            System.out.println(x + ": " + natija + ", chiziqli " + a[1] + ", ikkilik " + b[1] + " taqqoslash");
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <utility>
#include <vector>
using namespace std;

pair<int, int> chiziqli(const vector<int>& roy, int x) {
    int qadam = 0;
    for (int i = 0; i < (int) roy.size(); i++) {
        qadam++;
        if (roy[i] == x) return {i, qadam};
    }
    return {-1, qadam};
}

pair<int, int> ikkilik(const vector<int>& roy, int x) {
    int past = 0, yuqori = roy.size() - 1, qadam = 0;
    while (past <= yuqori) {
        int orta = (past + yuqori) / 2;
        qadam++;
        if (roy[orta] == x) return {orta, qadam};
        if (roy[orta] < x) past = orta + 1;              // oʻng yarmida qidirish
        else yuqori = orta - 1;                          // chap yarmida qidirish
    }
    return {-1, qadam};
}

vector<int> oqi(const string& satr) {
    istringstream ss(satr);
    vector<int> v;
    int x;
    while (ss >> x) v.push_back(x);
    return v;
}

int main() {
    string a, b;
    getline(cin, a);
    getline(cin, b);
    vector<int> narxlar = oqi(a);
    for (int x : oqi(b)) {
        auto c = chiziqli(narxlar, x);
        auto d = ikkilik(narxlar, x);
        string natija = c.first >= 0 ? "oʻrin " + to_string(c.first + 1) : "topilmadi";
        cout << x << ": " << natija << ", chiziqli " << c.second << ", ikkilik " << d.second << " taqqoslash\n";
    }
    return 0;
}
```

Kirish:

```text
1200 1850 2300 3100 4500 5200 6800 7400 8900 9600 10500 12000 13700 15000 18200
1200 9600 18200 5000
```

Chiqish:

```text
1200: oʻrin 1, chiziqli 1, ikkilik 4 taqqoslash
9600: oʻrin 10, chiziqli 10, ikkilik 3 taqqoslash
18200: oʻrin 15, chiziqli 15, ikkilik 4 taqqoslash
5000: topilmadi, chiziqli 15, ikkilik 4 taqqoslash
```

15 ta elementda farq kichik, lekin 1 000 000 ta mijozli bazada chiziqli qidiruv eng yomon holatda 1 000 000 ta, ikkilik qidiruv esa atigi ~20 ta taqqoslash qiladi. Maʼlumotlar bazasidagi **indekslar** (MT8) xuddi shu gʻoyaga asoslangan: mijozni STIR boʻyicha soniyaning mingdan bir ulushida topish aynan shu tufayli mumkin.

:::warning
**Koʻp uchraydigan xatolar**

- **Noaniq qadamlar** ("taxminan", "kerak boʻlsa") — algoritm emas.
- **Toʻxtash shartini unutish.** Sikl cheksiz ishlaydi — natijaviylik buziladi.
- **Tartiblanmagan roʻyxatda ikkilik qidiruv.** Notoʻgʻri natija beradi.
- **Samaradorlikni faqat kichik maʼlumotda tekshirish.** 100 ta yozuvda tez ishlagan algoritm millionda soatlab ishlashi mumkin.
:::

## Topshiriqlar

1. Bankomatdan pul yechish algoritmini soʻzli va psevdokod koʻrinishida yozing; har bir xossa qanday bajarilishini koʻrsating.
2. "Choy damlash" koʻrsatmasidagi xossalar buzilishini toping va tuzating.
3. Misol dasturida 7400 va 100 ni qidiring. Natijani qoʻlda tekshiring.
4. 1 000 000 ta elementli roʻyxatda ikkilik qidiruv koʻpi bilan nechta taqqoslash qiladi? (Ishora: 2²⁰ ≈ 1 000 000.)

## Xulosa

- Algoritm — aniq, cheklangan qadamlar ketma-ketligi; nomi al-Xorazmiydan.
- Xossalar: diskretlik, aniqlik, natijaviylik, ommaviylik, tushunarlilik.
- Tasvirlash: soʻzli, blok-sxema, psevdokod, dastur; tuzilmalar: chiziqli, tarmoqlanuvchi, takrorlanuvchi.
- Samaradorlik: O(1), O(log n), O(n), O(n²); ikkilik qidiruv tartiblangan roʻyxatda juda tez.

## Oʻzingizni tekshiring

:::check
Q: Natijaviylik xossasi nimani bildiradi?
A: Algoritm cheklangan sonli qadamdan soʻng albatta natija beradi.
Q: Ommaviylik xossasiga misol keltiring.
A: Ish haqini hisoblash algoritmi istalgan xodim va istalgan summa uchun ishlaydi.
Q: Ikkilik qidiruvning sharti nima?
A: Roʻyxat tartiblangan boʻlishi kerak.
Q: O(n²) murakkablik qachon xavfli?
A: Maʼlumot hajmi katta boʻlganda: n 1 000 marta oshsa, vaqt 1 000 000 marta oshadi.
:::

:::link https://www.khanacademy.org/computing/computer-science/algorithms
Khan Academy: Algorithms
Qidiruv, saralash va murakkablik boʻyicha interaktiv darslar (ingliz tilida).
:::
