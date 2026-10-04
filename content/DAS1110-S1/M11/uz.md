---
title: String toifasidagi satrlar
summary: C++ string, Java String va Python str: uzunlik, birlashtirish, qism satr, qidirish, almashtirish, solishtirish, katta-kichik harf, soʻzlarga ajratish; oʻzgarmas satrlar va StringBuilder.
---

## Nima uchun bu mavzu muhim?

Ism-familiyani tekshirish, parol qoidalari, e-pochta manzilidagi `@` ni topish, CSV fayldan ustunlarni ajratish, matndagi soʻzlarni sanash, qidiruv tizimi — dasturlarning katta qismi matn bilan ishlaydi. M10 darsida koʻrilgan C-satr bilan bularni qilish qiyin va xavfli: uzunlikni qoʻlda kuzatish, `'\0'` haqida oʻylash, chegaradan chiqishdan qoʻrqish kerak.

Zamonaviy tillarda buning uchun **satr klassi** bor: C++'da `string`, Java'da `String`, Python'da `str`. Ular uzunlikni oʻzi biladi, xotirani oʻzi boshqaradi va qidirish, kesish, almashtirish kabi tayyor amallarga ega.

:::note
**Bu darsdan soʻng siz:**

- satr yarata, birlashtira, uzunligini va alohida belgilarini ola olasiz;
- qism satr ajrata, satr ichida qidira va almashtira olasiz;
- satrlarni solishtira olasiz va katta-kichik harfga oʻtkaza olasiz;
- matnni soʻzlarga ajratib, ular bilan ishlay olasiz;
- Java va Python'da satr nima uchun oʻzgarmasligini va koʻp birlashtirishda `StringBuilder` / `join` nima uchun kerakligini tushuntira olasiz.
:::

## Asosiy amallar

| Amal | Python (`str`) | Java (`String`) | C++ (`string`) |
|---|---|---|---|
| Yaratish | `s = "Salom"` | `String s = "Salom";` | `string s = "Salom";` |
| Uzunlik | `len(s)` | `s.length()` | `s.size()` yoki `s.length()` |
| *i*-belgi | `s[i]` | `s.charAt(i)` | `s[i]` |
| Birlashtirish | `s + t` | `s + t` | `s + t` |
| Qism satr (*i* dan, *k* ta) | `s[i:i + k]` | `s.substring(i, i + k)` | `s.substr(i, k)` |
| Qidirish (birinchi oʻrin) | `s.find(t)` → −1 | `s.indexOf(t)` → −1 | `s.find(t)` → `string::npos` |
| Tenglik | `s == t` | `s.equals(t)` | `s == t` |
| Lugʻat tartibida solishtirish | `s < t` | `s.compareTo(t) < 0` | `s < t` |
| Katta harflarga | `s.upper()` | `s.toUpperCase()` | `toupper` har bir belgiga |
| Almashtirish (hammasi) | `s.replace(a, b)` | `s.replace(a, b)` | sikl bilan `find` + `replace` |

Eʼtibor bering: qism satrda Python va Java **oxirgi indeksni** oladi (u kirmaydi), C++ esa **uzunlikni**: `substr(2, 3)` — 2-indeksdan boshlab 3 ta belgi.

Python'da kesish (`slice`) kuchli vosita: `s[:3]` — dastlabki 3 belgi, `s[-3:]` — oxirgi 3 belgi, `s[::-1]` — teskari satr.

## Java'da satrlarni solishtirish

Java'da `==` satrlarning **mazmunini** emas, **havolalarini** solishtiradi: ikki xil obyektdagi bir xil matn uchun `==` yolgʻon boʻlishi mumkin. Mazmunni solishtirish uchun har doim `s.equals(t)` (katta-kichik harfni hisobga olmasdan — `s.equalsIgnoreCase(t)`) ishlating. Bu Java'dagi eng mashhur xatolardan biri.

C++ va Python'da `==` mazmunni solishtiradi.

## Oʻzgarmas satrlar

Java va Python'da satr **oʻzgarmas (immutable)**: yaratilgandan keyin uning belgilarini oʻzgartirib boʻlmaydi. `s.upper()`, `s.replace(...)` asl satrni oʻzgartirmaydi — **yangi** satr qaytaradi. `s[0] = 'X'` Python'da xato beradi.

Shuning uchun siklda `s = s + x` ni koʻp marta bajarish sekin: har safar yangi satr yaratilib, eskisi nusxalanadi. Yechim:

| Til | Koʻp qismdan satr yigʻish |
|---|---|
| Python | qismlarni roʻyxatga yigʻib, oxirida `"".join(qismlar)` |
| Java | `StringBuilder sb = new StringBuilder(); sb.append(x);` … `sb.toString()` |
| C++ | `string` oʻzgaruvchan: `s += x` tez ishlaydi |

C++'dagi `string` oʻzgaruvchan: `s[0] = 'X'` mumkin.

## Soʻzlarga ajratish

Matnni probellar boʻyicha soʻzlarga ajratish:

| Til | Usul |
|---|---|
| Python | `sozlar = s.split()` — bir nechta probelni ham toʻgʻri ishlaydi |
| Java | `String[] w = s.trim().split("\\s+");` — muntazam ifoda: bir yoki bir nechta probel |
| C++ | `istringstream ss(s);` va `while (ss >> w)` — `<sstream>` |

Java'da boʻsh qatorni `split` qilish bitta boʻsh elementli massiv beradi — buni alohida tekshiring.

## Ishlangan misol 1: palindrom

Bir qator matn kiritiladi. Faqat harflarni hisobga olib va katta-kichikligiga qaramay, u palindrom (oʻngdan ham, chapdan ham bir xil oʻqiladi) ekanini aniqlang.

```python
s = input()

harflar = ""
for c in s:
    if c.isalpha():
        harflar += c.lower()

print("ha" if harflar == harflar[::-1] else "yoʻq")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        String harflar = sb.toString();
        String teskari = sb.reverse().toString();

        System.out.println(harflar.equals(teskari) ? "ha" : "yoʻq");
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <cctype>
using namespace std;

int main() {
    string s;
    getline(cin, s);

    string harflar;
    for (char c : s) {
        if (isalpha((unsigned char) c)) {
            harflar += (char) tolower((unsigned char) c);
        }
    }
    string teskari(harflar.rbegin(), harflar.rend());

    cout << (harflar == teskari ? "ha" : "yoʻq") << endl;
    return 0;
}
```

Kirish `A man, a plan, a canal: Panama` boʻlsa: harflar `amanaplanacanalpanama` — teskarisi ham shunday. Chiqadi: `ha`. Kirish `Salom` boʻlsa: `salom` ≠ `molas`, chiqadi `yoʻq`.

Java'da `sb.reverse()` `StringBuilder` ning oʻzini teskari aylantiradi, shuning uchun `harflar` undan **oldin** olingan. C++'da `string teskari(harflar.rbegin(), harflar.rend())` — oxiridan boshiga oʻqib yangi satr yaratadi. Solishtirish Java'da `equals` bilan.

## Ishlangan misol 2: soʻzlar statistikasi

Bir qator matn kiritiladi (soʻzlar bir yoki bir nechta probel bilan ajratilgan). Soʻzlar sonini, eng uzun soʻzni (bir nechta boʻlsa — birinchisini) chiqaring va har bir soʻzni bosh harf bilan yozing.

```python
sozlar = input().split()

eng_uzun = ""
for w in sozlar:
    if len(w) > len(eng_uzun):
        eng_uzun = w

bosh_harfli = []
for w in sozlar:
    bosh_harfli.append(w[0].upper() + w[1:].lower())

print(len(sozlar))
print(eng_uzun)
print(" ".join(bosh_harfli))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] sozlar = in.nextLine().trim().split("\\s+");

        String engUzun = "";
        for (String w : sozlar) {
            if (w.length() > engUzun.length()) {
                engUzun = w;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String w : sozlar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0)));
            sb.append(w.substring(1).toLowerCase());
        }

        System.out.println(sozlar.length);
        System.out.println(engUzun);
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <cctype>
using namespace std;

int main() {
    string qator;
    getline(cin, qator);

    vector<string> sozlar;
    istringstream ss(qator);
    string w;
    while (ss >> w) {
        sozlar.push_back(w);
    }

    string engUzun;
    for (const string& s : sozlar) {
        if (s.size() > engUzun.size()) {
            engUzun = s;
        }
    }

    cout << sozlar.size() << endl;
    cout << engUzun << endl;
    for (size_t i = 0; i < sozlar.size(); i++) {
        string s = sozlar[i];
        s[0] = (char) toupper((unsigned char) s[0]);
        for (size_t j = 1; j < s.size(); j++) {
            s[j] = (char) tolower((unsigned char) s[j]);
        }
        if (i > 0) cout << ' ';
        cout << s;
    }
    cout << endl;
    return 0;
}
```

Kirish `  dasturlash   tili  PYTHON va java ` boʻlsa:

1. Soʻzlar: `dasturlash`, `tili`, `PYTHON`, `va`, `java` — 5 ta; ortiqcha probellar eʼtiborga olinmaydi.
2. Eng uzuni: `dasturlash` (10 harf).
3. Bosh harf bilan: `Dasturlash Tili Python Va Java`.

C++'da `istringstream` satrni xuddi `cin` kabi oʻqishga imkon beradi: `ss >> w` keyingi soʻzni oladi va probellarni oʻtkazib yuboradi.

## Ishlangan misol 3: qidirish va almashtirish

Uch qator kiritiladi: matn, qidiriladigan qism satr va uning oʻrniga qoʻyiladigan satr. Qism satr necha marta uchrashini (bir-birini qoplamaydigan) sanang va barchasini almashtirilgan matnni chiqaring.

```python
matn = input()
eski = input()
yangi = input()

soni = matn.count(eski)
print(soni)
print(matn.replace(eski, yangi))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String matn = in.nextLine();
        String eski = in.nextLine();
        String yangi = in.nextLine();

        int soni = 0;
        int i = matn.indexOf(eski);
        while (i != -1) {
            soni++;
            i = matn.indexOf(eski, i + eski.length());
        }

        System.out.println(soni);
        System.out.println(matn.replace(eski, yangi));
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string matn, eski, yangi;
    getline(cin, matn);
    getline(cin, eski);
    getline(cin, yangi);

    int soni = 0;
    size_t i = matn.find(eski);
    while (i != string::npos) {
        soni++;
        matn.replace(i, eski.size(), yangi);     // joyida almashtiramiz
        i = matn.find(eski, i + yangi.size());   // yangi qoʻyilgan qismdan keyin
    }

    cout << soni << endl;
    cout << matn << endl;
    return 0;
}
```

Kirish:

```text
Java va Python, Python va C++
Python
Go
```

Chiqadi: `2` va `Java va Go, Go va C++`.

Java versiyasida qidiruv `indexOf(eski, boshlash)` bilan davom etadi: har topilgandan keyin qidiruv topilgan qism tugagan joydan boshlanadi. C++ versiyasi topilgan joyni darhol `replace` qiladi va qidiruvni **yangi** qism satrdan keyin davom ettiradi — aks holda, masalan, „a“ ni „aa“ ga almashtirishda cheksiz siklga tushish mumkin. Python'da `count` va `replace` tayyor.

:::warning
**Koʻp uchraydigan xatolar**

- **Java'da satrlarni `==` bilan solishtirish.** Mazmun uchun `equals`.
- **`substr` va `substring` ni chalkashtirish.** C++'da ikkinchi argument — uzunlik, Java'da — oxirgi indeks.
- **Natijani saqlamaslik.** Python va Java'da `s.upper()` yangi satr qaytaradi; `s = s.upper()` deb yozish kerak.
- **C++'da `find` natijasini `-1` bilan solishtirish.** Toʻgʻrisi — `string::npos`.
- **Siklda `s = s + x` (Java).** Katta matnda juda sekin; `StringBuilder` ishlating.
- **Boʻsh satrda `w[0]`.** Boʻsh satrning 0-belgisi yoʻq; oldin uzunlikni tekshiring.
:::

:::tip
Satr bilan ishlaydigan har bir funksiyani chekka holatlarda sinang: boʻsh satr, bitta belgi, faqat probellar, qidirilgan qism boshida va oxirida. Matnli xatolarning deyarli hammasi shu yerda.
:::

## Xulosa

- `string` (C++), `String` (Java), `str` (Python) uzunlikni va xotirani oʻzi boshqaradi.
- Asosiy amallar: uzunlik, belgi, birlashtirish, qism satr, qidirish, almashtirish, solishtirish.
- Qism satr: Python va Java — oxirgi indeks, C++ — uzunlik; topilmasa: −1 yoki `string::npos`.
- Java'da mazmun `equals` bilan solishtiriladi.
- Java va Python satrlari oʻzgarmas: metodlar yangi satr qaytaradi; koʻp birlashtirish — `StringBuilder` / `join`.
- Soʻzlarga ajratish: `split()`, `split("\\s+")`, `istringstream`.

## Oʻzingizni tekshiring

:::check
Q: `s = "Dasturlash"` boʻlsa, Python'da `s[3:7]` va C++'da `s.substr(3, 4)` nimani beradi?
A: Ikkalasi ham `turl`: 3-indeksdan 4 ta belgi.
Q: Java'da `new String("ab") == "ab"` nimaga teng va nima uchun?
A: `false`: `==` havolalarni solishtiradi, bular ikki xil obyekt. `"ab".equals(new String("ab"))` esa `true`.
Q: Python'da `s = "salom"; s.upper(); print(s)` nima chiqaradi?
A: `salom`: satr oʻzgarmas, `upper()` yangi satr qaytaradi, lekin u saqlanmadi.
Q: Nima uchun Java'da siklda `s = s + x` oʻrniga `StringBuilder` tavsiya etiladi?
A: Har bir `+` yangi satr yaratib, eskisini toʻliq nusxalaydi; *n* marta birlashtirish ~*n*² ish talab qiladi. `StringBuilder` esa bitta oʻzgaruvchan bufer bilan ishlaydi.
Q: `"  bir  ikki "` Python'da `split()` bilan nechta soʻzga ajraladi?
A: 2 ta: `["bir", "ikki"]`.
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: satr metodlari
`split`, `join`, `find`, `replace`, `upper` va boshqalar.
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
cppreference: std::string
C++ `string` klassining barcha metodlari.
:::

:::link https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
Java 21: String klassi
`String` metodlarining rasmiy hujjati.
:::
