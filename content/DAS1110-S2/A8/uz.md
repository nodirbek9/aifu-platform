---
title: Matnli fayllar haqida umumiy tushuncha. Fayllar va oqimlar
summary: Oqim tushunchasi — standart kirish, chiqish va xatolar oqimi, fayl oqimlari; oqimni oxirigacha (EOF) oʻqish, qatorma-qator qayta ishlash, faylga yozib qayta oʻqish, buferlash; jurnal faylini filtrlash va qatorlarni raqamlash.
---

## Nima uchun bu mavzu muhim?

Dastur maʼlumotni klaviaturadan, fayldan, tarmoqdan yoki boshqa dasturdan olishi mumkin. Bularning hammasi uchun tillar bitta umumiy tushunchadan foydalanadi — **oqim** (stream): baytlar yoki belgilar ketma-ket keladigan „quvur“. Oqim bilan ishlashni oʻrgangan dasturchi uchun klaviaturadan oʻqish bilan fayldan oʻqishning deyarli farqi yoʻq.

Birinchi semestrda faylni ochish, yozish va oʻqish bilan tanishgan edik. Bu amaliy mashgʻulotda oqimlar bilan ishlashning amaliy usullarini mashq qilamiz: maʼlumot qancha ekani oldindan nomaʼlum boʻlganda oqimni oxirigacha oʻqish, maʼlumotni filtrlab faylga yozish va uni qayta oʻqish.

:::note
**Bu mashgʻulotdan soʻng siz:**

- standart kirish, chiqish va xatolar oqimini hamda fayl oqimlarini farqlay olasiz;
- oqimni oxirigacha (EOF) uch tilda oʻqiy olasiz;
- katta faylni xotiraga toʻliq yuklamasdan qatorma-qator qayta ishlay olasiz;
- natijani faylga yozib, uni qayta oʻqiy olasiz;
- buferlash nima ekanini va faylni yopish nima uchun muhimligini tushuntira olasiz.
:::

## Oqimlar

| Oqim | Vazifasi | Python | Java | C++ |
|---|---|---|---|---|
| Standart kirish | klaviatura yoki yoʻnaltirilgan fayl | `sys.stdin`, `input()` | `System.in` | `cin` |
| Standart chiqish | ekran | `sys.stdout`, `print()` | `System.out` | `cout` |
| Xatolar oqimi | xabarlar, tuzatuv maʼlumoti | `sys.stderr` | `System.err` | `cerr` |
| Fayldan oʻqish | | `open(nom)` | `BufferedReader`, `Files.newBufferedReader` | `ifstream` |
| Faylga yozish | | `open(nom, "w")` | `PrintWriter`, `Files.newBufferedWriter` | `ofstream` |

Buyruq qatorida standart oqimlarni faylga **yoʻnaltirish** mumkin: `dastur < kirish.txt > chiqish.txt`. Dastur bundan bexabar — u baribir `cin`/`input()` dan oʻqiydi. Avtomatik tekshiruv tizimlari ham aynan shunday ishlaydi: test fayli dasturning standart kirishiga ulanadi.

## Oqimni oxirigacha oʻqish

Koʻp masalalarda maʼlumot soni oldindan aytilmaydi: „fayl tugaguncha oʻqing“. Oqim tugashi **EOF** (end of file) deyiladi. Klaviaturadan EOF Windows'da Ctrl+Z va Enter, Linux/macOS'da Ctrl+D bilan beriladi.

| Til | Qatorma-qator EOF gacha | Soʻzma-soʻz (sonma-son) EOF gacha |
|---|---|---|
| Python | `for satr in sys.stdin:` | `sys.stdin.read().split()` |
| Java | `while ((satr = oquvchi.readLine()) != null)` | `while (in.hasNext())` (`Scanner`) |
| C++ | `while (getline(cin, satr))` | `while (cin >> x)` |

C++'da `cin >> x` ifodasi oʻqish muvaffaqiyatli boʻlsa `true`, EOF yoki xato boʻlsa `false` beradi — shuning uchun u toʻgʻridan-toʻgʻri `while` shartiga yoziladi. Python'da `sys.stdin` dan oʻqilgan har bir qator oxirida `\n` qoladi — u `rstrip("\n")` bilan olib tashlanadi.

## Buferlash va faylni yopish

Har bir belgini diskka alohida yozish juda sekin. Shuning uchun oqimlar maʼlumotni avval xotiradagi **bufer**ga yigʻadi va u toʻlganda (yoki oqim yopilganda) bir yoʻla yozadi. Bundan muhim xulosa: fayl **yopilmasa**, oxirgi maʼlumotlar bufferda qolib, faylga yozilmasligi mumkin.

| Til | Avtomatik yopish |
|---|---|
| Python | `with open(nom, "w") as f:` — blokdan chiqishda yopiladi |
| Java | `try (PrintWriter f = new PrintWriter(nom)) { ... }` — try-with-resources |
| C++ | `ofstream` obyekti oʻz doirasidan chiqqanda yopiladi (destruktor); kerak boʻlsa `f.close()` |

Bir dasturda faylga yozib, uni darhol qayta oʻqimoqchi boʻlsangiz, yozish oqimi oʻqishdan **oldin** yopilgan boʻlishi shart.

## Ishlangan misol 1: oqimdagi barcha sonlar

Standart kirishda bir nechta qatorda butun sonlar yozilgan; ular nechta ekani nomaʼlum. Sonlar sonini, yigʻindisini va eng kattasini chiqaring (son boʻlmasa — `0 0 -`).

```python
import sys

sonlar = list(map(int, sys.stdin.read().split()))    # butun oqim, soʻzlarga boʻlingan
if sonlar:
    print(len(sonlar), sum(sonlar), max(sonlar))
else:
    print(0, 0, "-")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long soni = 0, yigindi = 0, engKatta = Long.MIN_VALUE;
        while (in.hasNextLong()) {                          // oqim tugaguncha
            long x = in.nextLong();
            soni++;
            yigindi += x;
            engKatta = Math.max(engKatta, x);
        }
        System.out.println(soni + " " + yigindi + " " + (soni > 0 ? String.valueOf(engKatta) : "-"));
    }
}
```
```cpp
#include <climits>
#include <iostream>
using namespace std;

int main() {
    long long x, soni = 0, yigindi = 0, engKatta = LLONG_MIN;
    while (cin >> x) {                                      // EOF da false
        soni++;
        yigindi += x;
        if (x > engKatta) engKatta = x;
    }
    cout << soni << " " << yigindi << " ";
    if (soni > 0) cout << engKatta;
    else cout << "-";
    cout << endl;
    return 0;
}
```

Kirish:

```text
12 7
-3
40 5 9
```

Chiqish:

```text
6 70 40
```

Uchala dastur ham qatorlar qanday boʻlinganiga eʼtibor bermaydi: sonlar bitta qatorda yoki har biri alohida qatorda kelsa ham natija bir xil. Java va C++ sonlarni **oqim boʻyicha** birma-bir qayta ishlaydi va hammasini xotirada saqlamaydi — bu juda katta fayllar uchun muhim. Python versiyasi esa butun oqimni oʻqiydi: qisqa, lekin oqim gigabaytlab boʻlsa, xotiraga sigʻmasligi mumkin.

## Ishlangan misol 2: jurnal faylini filtrlash

Standart kirishda dastur jurnali (log) qatorlari: `SANA DARAJA XABAR`, daraja — `INFO`, `WARN` yoki `ERROR`. `ERROR` qatorlarini `xatolar.txt` fayliga yozing, faylni yoping, keyin uni qayta oʻqib, xatolar sonini va har bir xato xabarini (sanasi bilan) chiqaring.

```python
import sys

with open("xatolar.txt", "w", encoding="utf-8") as fayl:      # yozish
    for satr in sys.stdin:
        qismlar = satr.split(maxsplit=2)
        if len(qismlar) == 3 and qismlar[1] == "ERROR":
            fayl.write(qismlar[0] + " " + qismlar[2].rstrip("\n") + "\n")
# with blokidan chiqildi — fayl yopildi, bufer diskka yozildi

with open("xatolar.txt", encoding="utf-8") as fayl:           # qayta oʻqish
    xatolar = [s.rstrip("\n") for s in fayl]
print(len(xatolar))
for x in xatolar:
    print(x)
```
```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader kirish = new BufferedReader(new InputStreamReader(System.in));
        try (PrintWriter fayl = new PrintWriter("xatolar.txt", "UTF-8")) {   // yozish
            String satr;
            while ((satr = kirish.readLine()) != null) {
                String[] q = satr.trim().split("\\s+", 3);
                if (q.length == 3 && q[1].equals("ERROR")) fayl.println(q[0] + " " + q[2]);
            }
        }   // try tugadi — fayl yopildi

        List<String> xatolar = new ArrayList<>();
        try (BufferedReader fayl = new BufferedReader(new FileReader("xatolar.txt"))) {   // qayta oʻqish
            String satr;
            while ((satr = fayl.readLine()) != null) xatolar.add(satr);
        }
        System.out.println(xatolar.size());
        for (String x : xatolar) System.out.println(x);
    }
}
```
```cpp
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    {
        ofstream fayl("xatolar.txt");                       // yozish
        string satr;
        while (getline(cin, satr)) {
            istringstream oqim(satr);
            string sana, daraja, xabar;
            oqim >> sana >> daraja;
            getline(oqim >> ws, xabar);                     // qatorning qolgan qismi
            if (daraja == "ERROR") fayl << sana << " " << xabar << "\n";
        }
    }   // blok tugadi — ofstream yopildi

    ifstream fayl("xatolar.txt");                           // qayta oʻqish
    vector<string> xatolar;
    string satr;
    while (getline(fayl, satr)) xatolar.push_back(satr);
    cout << xatolar.size() << endl;
    for (const string& x : xatolar) cout << x << endl;
    return 0;
}
```

Kirish:

```text
2026-03-01 INFO server ishga tushdi
2026-03-01 ERROR bazaga ulanib bo'lmadi
2026-03-02 WARN disk 85% band
2026-03-02 ERROR fayl topilmadi: hisobot.pdf
2026-03-03 INFO zaxira nusxa tayyor
```

Chiqish:

```text
2
2026-03-01 bazaga ulanib bo'lmadi
2026-03-02 fayl topilmadi: hisobot.pdf
```

Qator `maxsplit=2` (Java'da `split(..., 3)`) bilan faqat **uch** qismga boʻlinadi — xabar ichidagi boʻsh joylar saqlanib qoladi. C++'da `oqim >> ws` boʻsh joylarni oʻtkazib yuboradi, keyingi `getline` esa qatorning qolgan qismini oladi. Uchala tilda ham yozish oqimi **alohida blokda** ochilgan: blok tugashi bilan fayl yopiladi va keyingi oʻqish toʻliq maʼlumotni koʻradi.

## Ishlangan misol 3: qatorlarni raqamlash

Standart kirishdagi barcha qatorlarni (boʻsh qatorlar ham) `raqamlangan.txt` fayliga raqamlab yozing: raqam oʻngga tekislangan, kengligi — eng katta raqam uzunligiga teng, keyin `| ` va qatorning oʻzi. Soʻng faylni oʻqib, ekranga chiqaring.

```python
import sys

qatorlar = [s.rstrip("\n") for s in sys.stdin]
en = len(str(len(qatorlar)))                         # eng katta raqam uzunligi

with open("raqamlangan.txt", "w", encoding="utf-8") as fayl:
    for i, q in enumerate(qatorlar, start=1):
        fayl.write(f"{i:>{en}}| {q}\n")

with open("raqamlangan.txt", encoding="utf-8") as fayl:
    print(fayl.read(), end="")
```
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader kirish = new BufferedReader(new InputStreamReader(System.in));
        List<String> qatorlar = new ArrayList<>();
        String satr;
        while ((satr = kirish.readLine()) != null) qatorlar.add(satr);
        int en = String.valueOf(qatorlar.size()).length();      // eng katta raqam uzunligi

        try (PrintWriter fayl = new PrintWriter("raqamlangan.txt", "UTF-8")) {
            for (int i = 0; i < qatorlar.size(); i++) {
                fayl.printf("%" + en + "d| %s%n", i + 1, qatorlar.get(i));
            }
        }

        System.out.print(Files.readString(Path.of("raqamlangan.txt"), StandardCharsets.UTF_8));
    }
}
```
```cpp
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<string> qatorlar;
    string satr;
    while (getline(cin, satr)) qatorlar.push_back(satr);
    int en = to_string(qatorlar.size()).size();             // eng katta raqam uzunligi

    {
        ofstream fayl("raqamlangan.txt");
        for (size_t i = 0; i < qatorlar.size(); i++) {
            fayl << setw(en) << i + 1 << "| " << qatorlar[i] << "\n";
        }
    }

    ifstream fayl("raqamlangan.txt");
    while (getline(fayl, satr)) cout << satr << "\n";
    return 0;
}
```

12 qatorli kirish uchun raqamlar ikki xonali kenglikda tekislanadi:

```text
 1| birinchi qator
 2|
 ...
12| oxirgi qator
```

Raqam kengligi faqat barcha qatorlar oʻqilgandan keyin maʼlum boʻladi, shuning uchun bu yerda qatorlar avval xotiraga yigʻiladi. Ustunlarni tekislash: Python'da `f"{i:>{en}}"` — kenglik ham oʻzgaruvchidan, Java'da formatlash satri `"%2d| %s%n"` dastur ichida yigʻiladi, C++'da `setw(en)` — faqat **keyingi** chiqariladigan qiymat uchun amal qiladi. Java'da `printf` dagi `%n` platformaga mos satr oxirini yozadi; avtomatik tekshiruv Linux'da ishlagani uchun bu `\n` boʻladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Yozilgan faylni yopmasdan oʻqish.** Bufer hali diskka yozilmagan boʻlishi mumkin va fayl boʻsh yoki chala koʻrinadi; yozishni `with`/`try`/blok bilan yoping.
- **C++'da `while (!cin.eof())`.** Oxirgi oʻqish muvaffaqiyatsiz boʻlganini kech sezadi va oxirgi qiymat ikki marta ishlanadi; `while (cin >> x)` yozing.
- **Python'da `\n` ni olib tashlamaslik.** `sys.stdin` va fayldan oʻqilgan qatorlar `\n` bilan tugaydi; `print` qoʻshimcha boʻsh qator chiqaradi.
- **Kodlashni koʻrsatmaslik.** Windows'da sukut boʻyicha kodlash boshqa boʻlishi mumkin va oʻzbekcha harflar buziladi; `encoding="utf-8"`, `"UTF-8"` ni aniq yozing.
:::

:::tip
Dasturni tekshirishda kirishni har safar qoʻlda terish oʻrniga uni faylga saqlang va yoʻnaltiring: `python main.py < test1.txt`. Bir nechta test faylini tayyorlab qoʻysangiz, har bir oʻzgarishdan keyin hammasini bir zumda qayta tekshirish mumkin.
:::

## Xulosa

- Oqim — maʼlumot ketma-ket keladigan „quvur“; klaviatura, ekran va fayllar bir xil usulda ishlanadi.
- Oqim oxirigacha: `for satr in sys.stdin`, `readLine() != null`, `while (getline(cin, satr))` yoki `while (cin >> x)`.
- Oqim boʻyicha qayta ishlash katta fayllarni xotiraga yuklamasdan ishlash imkonini beradi.
- Yozish buferlanadi: fayl yopilgandagina maʼlumot toʻliq diskka tushadi; `with`, try-with-resources yoki blok ishlating.
- Standart oqimlarni buyruq qatorida faylga yoʻnaltirish mumkin: `dastur < kirish.txt > chiqish.txt`.

## Oʻzingizni tekshiring

:::check
Q: EOF nima va klaviaturadan qanday beriladi?
A: Oqim tugaganini bildiruvchi holat; Windows'da Ctrl+Z va Enter, Linux/macOS'da Ctrl+D.
Q: C++'da `while (cin >> x)` qachon toʻxtaydi?
A: Oqim tugaganda yoki son boʻlmagan maʼlumot uchraganda — oʻqish muvaffaqiyatsiz boʻlganda.
Q: Nima uchun faylga yozgandan keyin uni yopish kerak?
A: Maʼlumot buferda turgan boʻlishi mumkin; yopilganda bufer diskka yoziladi va fayl tizimga qaytariladi.
Q: `dastur < a.txt > b.txt` buyrugʻi nima qiladi?
A: Dasturning standart kirishini `a.txt` dan, standart chiqishini `b.txt` ga yoʻnaltiradi.
Q: Python'da `"2026-03-01 ERROR fayl topilmadi".split(maxsplit=2)` nima qaytaradi?
A: `["2026-03-01", "ERROR", "fayl topilmadi"]` — faqat birinchi ikki boʻsh joyda boʻlinadi.
:::

:::link https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files
Python: fayllarni oʻqish va yozish
`open`, `with` va fayl metodlari haqida rasmiy qoʻllanma (ingliz tilida).
:::

:::link https://dev.java/learn/java-io/
Java: kiritish-chiqarish
Oqimlar, fayllarni oʻqish va yozish boʻyicha rasmiy oʻquv resursi (ingliz tilida).
:::
