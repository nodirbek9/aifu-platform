---
title: Fayllar bilan ishlash
summary: Fayl tushunchasi, matnli va ikkilik fayllar, faylni ochish rejimlari (oʻqish, yozish, qoʻshish), yozish va qatorma-qator oʻqish, faylni yopish, with va try-with-resources, xatolar, CSV formatidagi maʼlumot.
---

## Nima uchun bu mavzu muhim?

Shu paytgacha dasturlarimiz maʼlumotni klaviaturadan oldi va ekranga chiqardi. Dastur tugashi bilan hamma narsa yoʻqoldi: kiritilgan ballar, hisoblangan natijalar. Haqiqiy dasturlar esa maʼlumotni **saqlaydi**: jurnal ertaga ham ochiladi, hisobot faylga yoziladi, sozlamalar keyingi ishga tushirishda oʻqiladi.

Maʼlumotni uzoq muddat saqlashning eng oddiy usuli — **fayl**. Maʼlumotlar bazalari, Excel jadvallari, rasmlar, dastur kodining oʻzi — hammasi oxir-oqibat fayllarda. Bu darsda faylga yozish, fayldan oʻqish va eng koʻp ishlatiladigan matnli format — CSV bilan ishlashni oʻrganamiz.

:::note
**Bu darsdan soʻng siz:**

- matnli va ikkilik fayllarni farqlay olasiz;
- faylni kerakli rejimda (oʻqish, yozish, qoʻshish) ocha olasiz va toʻgʻri yopa olasiz;
- faylga maʼlumot yozib, uni qatorma-qator oʻqiy olasiz;
- fayl topilmasligi kabi xatolarni qayta ishlay olasiz;
- CSV formatidagi maʼlumotni yozib, oʻqib, tahlil qila olasiz.
:::

## Fayl tushunchasi

**Fayl** — diskda nom bilan saqlanadigan baytlar ketma-ketligi. Dastur ishlashi tugagandan keyin ham u saqlanib qoladi.

| Tur | Ichida nima | Misollar | Qanday oʻqiladi |
|---|---|---|---|
| **Matnli fayl** | belgilar, qatorlarga boʻlingan | `.txt`, `.csv`, `.json`, `.py`, `.html` | istalgan matn muharririda |
| **Ikkilik fayl** | ixtiyoriy baytlar | `.jpg`, `.mp3`, `.exe`, `.docx` | maxsus dastur bilan |

Bu darsda matnli fayllar bilan ishlaymiz.

Fayl bilan ishlash uch bosqichdan iborat: **ochish** → **oʻqish yoki yozish** → **yopish**. Ochishda **rejim** tanlanadi:

| Rejim | Python | Maʼnosi |
|---|---|---|
| Oʻqish | `"r"` | fayl mavjud boʻlishi shart |
| Yozish | `"w"` | fayl yaratiladi; mavjud boʻlsa — **tozalanadi** |
| Qoʻshish | `"a"` | yozuv fayl oxiriga qoʻshiladi |

## Faylni ochish va yopish

| Vazifa | Python | Java | C++ |
|---|---|---|---|
| Yozish uchun ochish | `f = open("a.txt", "w", encoding="utf-8")` | `PrintWriter w = new PrintWriter(new FileWriter("a.txt"));` | `ofstream f("a.txt");` |
| Yozish | `f.write("matn\n")` yoki `print(x, file=f)` | `w.println(x);` | `f << x << endl;` |
| Oʻqish uchun ochish | `f = open("a.txt", encoding="utf-8")` | `BufferedReader r = new BufferedReader(new FileReader("a.txt"));` | `ifstream f("a.txt");` |
| Qatorni oʻqish | `for qator in f:` | `while ((qator = r.readLine()) != null)` | `while (getline(f, qator))` |
| Yopish | `f.close()` | `w.close();` | `f.close();` (yoki avtomatik) |

**Yopish nima uchun muhim?** Yozilgan maʼlumot avval xotiradagi **buferda** turadi va diskka keyinroq tushadi. Fayl yopilganda bufer diskka toʻliq yoziladi. Yopilmagan faylda oxirgi yozuvlar yoʻqolishi mumkin; bundan tashqari, operatsion tizim bir vaqtda ochiq fayllar sonini cheklaydi.

Yopishni unutmaslik uchun tillarda maxsus tuzilmalar bor:

| Til | Avtomatik yopish |
|---|---|
| Python | `with open(...) as f:` — blokdan chiqqanda yopiladi, xato boʻlsa ham |
| Java | `try (PrintWriter w = ...) { ... }` — **try-with-resources** |
| C++ | `ofstream` / `ifstream` obyekti oʻz blokidan chiqqanda oʻzi yopiladi |

## Xatolar

Fayl bilan ishlashda xato oddiy holat: fayl yoʻq, ruxsat yoʻq, disk toʻla. Har bir til buni oʻzicha bildiradi:

| Til | Fayl topilmasa |
|---|---|
| Python | `FileNotFoundError` istisnosi; `try` / `except` bilan ushlanadi |
| Java | `FileNotFoundException` (`IOException` turi); `try` / `catch` bilan ushlanishi **shart** |
| C++ | istisno yoʻq; `if (!f)` yoki `f.is_open()` bilan tekshiriladi |

Fayl yoʻqligini tekshirmasdan oʻqish dasturning jim xatoga yoki qulashga olib keladi.

## Fayl yoʻli

`"baholar.txt"` — **nisbiy yoʻl**: fayl dastur ishga tushirilgan joriy papkada qidiriladi. `"C:/AIFU/baholar.txt"` yoki `"/home/talaba/baholar.txt"` — **toʻliq yoʻl**. Windowsʼda teskari chiziq satr ichida ikkilanadi (`"C:\\AIFU\\baholar.txt"`) yoki oddiy `/` ishlatiladi.

## Ishlangan misol 1: sonlarni faylga yozish va qayta oʻqish

Birinchi qatorda *n*, ikkinchisida *n* ta baho. Baholarni `baholar.txt` fayliga (har birini alohida qatorga) yozing, keyin faylni oʻqib, baholar soni, yigʻindisi va oʻrtachasini chiqaring.

```python
n = int(input())
baholar = list(map(int, input().split()))

with open("baholar.txt", "w", encoding="utf-8") as f:
    for b in baholar:
        f.write(f"{b}\n")

yigindi = 0
soni = 0
with open("baholar.txt", encoding="utf-8") as f:
    for qator in f:
        yigindi += int(qator)
        soni += 1

print(f"Soni: {soni}, yigʻindi: {yigindi}, oʻrtacha: {yigindi / soni:.2f}")
```
```java
import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        try (PrintWriter w = new PrintWriter(new FileWriter("baholar.txt"))) {
            for (int i = 0; i < n; i++) {
                w.println(in.nextInt());
            }
        }

        int yigindi = 0, soni = 0;
        try (BufferedReader r = new BufferedReader(new FileReader("baholar.txt"))) {
            String qator;
            while ((qator = r.readLine()) != null) {
                yigindi += Integer.parseInt(qator.trim());
                soni++;
            }
        }

        System.out.println(String.format("Soni: %d, yigʻindi: %d, oʻrtacha: %.2f",
                soni, yigindi, (double) yigindi / soni));
    }
}
```
```cpp
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;

    {
        ofstream f("baholar.txt");
        for (int i = 0; i < n; i++) {
            int b;
            cin >> b;
            f << b << endl;
        }
    }                                // blokdan chiqildi — fayl yopildi

    int yigindi = 0, soni = 0;
    ifstream f("baholar.txt");
    int b;
    while (f >> b) {                 // son oʻqilmaguncha davom etadi
        yigindi += b;
        soni++;
    }

    cout << fixed << setprecision(2)
         << "Soni: " << soni << ", yigʻindi: " << yigindi
         << ", oʻrtacha: " << (double) yigindi / soni << endl;
    return 0;
}
```

Kirish `5` va `4 5 3 5 4` boʻlsa, `baholar.txt` quyidagicha boʻladi:

```text
4
5
3
5
4
```

Dastur uni oʻqib, chiqaradi: `Soni: 5, yigʻindi: 21, oʻrtacha: 4.20`.

1. Yozish uchun ochilgan fayl `with` / try-with-resources / `{ }` bloki tugaganda yopiladi — oʻqishdan oldin barcha maʼlumot diskda.
2. Python va Javaʼda fayl qatorma-qator oʻqiladi va har bir qator songa aylantiriladi (`int(qator)` qator oxiridagi `\n` ni oʻzi eʼtiborsiz qoldiradi; Javaʼda `trim()`).
3. C++'da `f >> b` probel va yangi qatorlarni oʻtkazib, keyingi sonni oʻqiydi; fayl tugaganda shart yolgʻon boʻladi.

## Ishlangan misol 2: CSV fayl

Birinchi qatorda *n*, keyingi *n* qatorda talaba ismi va bali vergul bilan: `Ism,ball`. Maʼlumotni `talabalar.csv` ga yozing, keyin faylni oʻqib, 56 va undan yuqori ball olganlarni (fayldagi tartibda) va ularning sonini chiqaring.

**CSV (comma-separated values)** — har bir qator — bitta yozuv, maydonlar vergul bilan ajratilgan. Excel, Google Sheets va maʼlumotlar bazalari CSV ni oʻqiy va yoza oladi.

```python
n = int(input())
with open("talabalar.csv", "w", encoding="utf-8") as f:
    for _ in range(n):
        f.write(input() + "\n")

otganlar = []
with open("talabalar.csv", encoding="utf-8") as f:
    for qator in f:
        ism, ball = qator.strip().split(",")
        if int(ball) >= 56:
            otganlar.append(ism)

for ism in otganlar:
    print(ism)
print("Jami:", len(otganlar))
```
```java
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        try (PrintWriter w = new PrintWriter(new FileWriter("talabalar.csv"))) {
            for (int i = 0; i < n; i++) {
                w.println(in.nextLine());
            }
        }

        List<String> otganlar = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader("talabalar.csv"))) {
            String qator;
            while ((qator = r.readLine()) != null) {
                String[] maydon = qator.trim().split(",");
                if (Integer.parseInt(maydon[1]) >= 56) {
                    otganlar.add(maydon[0]);
                }
            }
        }

        for (String ism : otganlar) {
            System.out.println(ism);
        }
        System.out.println("Jami: " + otganlar.size());
    }
}
```
```cpp
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    cin.ignore();
    {
        ofstream f("talabalar.csv");
        for (int i = 0; i < n; i++) {
            string qator;
            getline(cin, qator);
            f << qator << endl;
        }
    }

    vector<string> otganlar;
    ifstream f("talabalar.csv");
    string qator;
    while (getline(f, qator)) {
        stringstream ss(qator);
        string ism, ball;
        getline(ss, ism, ',');           // vergulgacha
        getline(ss, ball);               // qolgani
        if (stoi(ball) >= 56) {
            otganlar.push_back(ism);
        }
    }

    for (const string& ism : otganlar) {
        cout << ism << endl;
    }
    cout << "Jami: " << otganlar.size() << endl;
    return 0;
}
```

Kirish:

```text
4
Aziz,78
Dilnoza,92
Bekzod,45
Malika,56
```

Chiqadi:

```text
Aziz
Dilnoza
Malika
Jami: 3
```

Har bir qator vergul boʻyicha ikki maydonga ajratiladi: Python va Javaʼda `split(",")`, C++'da `getline(ss, ism, ',')` — uchinchi argument ajratuvchi belgini bildiradi. `stoi` (C++) va `Integer.parseInt` (Java) satrni songa aylantiradi. Bekzod (45) roʻyxatga kirmadi; Malika (56) — chegarada, `>=` tufayli kirdi.

:::warning
**Koʻp uchraydigan xatolar**

- **`"w"` rejimida mavjud faylni ochish.** Fayl darhol tozalanadi; qoʻshish uchun `"a"` (Java: `new FileWriter(nom, true)`, C++: `ofstream f(nom, ios::app)`).
- **Faylni yopmaslik.** Oxirgi yozuvlar diskka tushmasligi mumkin; `with` / try-with-resources ishlating.
- **Fayl ochilganini tekshirmaslik (C++).** Fayl yoʻq boʻlsa, `ifstream` jim turadi, oʻqish hech narsa bermaydi.
- **Qator oxiridagi `\n` ni unutish.** Pythonʼda `qator.strip()`, aks holda solishtirish va `split` kutilmagan natija beradi.
- **Kodlashni koʻrsatmaslik.** Pythonʼda `encoding="utf-8"` siz Windowsʼda oʻzbekcha harflar buziladi.
- **CSV maydonida vergul.** `"Aliyev, Aziz",78` kabi maydon oddiy `split(",")` bilan notoʻgʻri ajraladi; bunday hollarda maxsus kutubxona (`csv` moduli) ishlating.
:::

:::tip
Faylni yozish va oʻqishni ikki alohida funksiyaga ajrating: `saqla(royxat, nom)` va `yukla(nom)`. Shunda ularni alohida sinash va maʼlumot formatini bitta joyda oʻzgartirish mumkin.
:::

## Xulosa

- Fayl — diskdagi nomli baytlar ketma-ketligi; matnli fayllar qatorlardan iborat.
- Ish tartibi: ochish (rejim bilan) → oʻqish/yozish → yopish.
- Rejimlar: oʻqish `r`, yozish `w` (faylni tozalaydi), qoʻshish `a`.
- Python `with`, Java try-with-resources, C++ obyekt bloki faylni avtomatik yopadi.
- Fayl xatolari: Python va Javaʼda istisnolar, C++'da `if (!f)` tekshiruvi.
- CSV — vergul bilan ajratilgan maydonli qatorlar; `split(",")` yoki `getline(ss, x, ',')` bilan ajratiladi.

## Oʻzingizni tekshiring

:::check
Q: Matnli va ikkilik fayl oʻrtasidagi farq nima?
A: Matnli fayl belgilar va qatorlardan iborat, uni matn muharririda oʻqish mumkin; ikkilik fayl ixtiyoriy baytlardan iborat va maxsus dastur bilan ochiladi.
Q: Pythonʼda `open("x.txt", "w")` mavjud faylni ochsa, uning eski mazmuni nima boʻladi?
A: Oʻchib ketadi: `"w"` rejimi faylni tozalab ochadi. Oxiriga qoʻshish uchun `"a"`.
Q: Nima uchun faylni yopish kerak?
A: Yozilgan maʼlumot buferda turadi va yopilganda diskka toʻliq yoziladi; yopilmasa, oxirgi yozuvlar yoʻqolishi mumkin. Ochiq fayllar soni ham cheklangan.
Q: C++'da fayl ochilmaganini qanday bilasiz?
A: `ifstream f("a.txt"); if (!f) { ... }` yoki `f.is_open()` bilan.
Q: `Aziz,78` qatoridan Javaʼda ism va ballni qanday olasiz?
A: `String[] m = qator.split(",");` — `m[0]` ism, `Integer.parseInt(m[1])` ball.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: fayllarni oʻqish va yozish
Rasmiy qoʻllanmadagi `open`, `with` va fayl metodlari.
:::

:::link https://en.cppreference.com/w/cpp/io/basic_fstream
cppreference: fayl oqimlari
C++'da `ifstream`, `ofstream` va `fstream`.
:::

:::link https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
Oracle Java Tutorial: try-with-resources
Resurslarni avtomatik yopish.
:::
