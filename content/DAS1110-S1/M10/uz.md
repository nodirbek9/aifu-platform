---
title: Satrlar va kengaytirilgan belgilar
summary: Belgi turi va ASCII kodlari, belgilar arifmetikasi, C uslubidagi satr va '\0', satrni qator boʻyicha oʻqish, Unicode, UTF-8 va kengaytirilgan belgilar (wchar_t), oʻzbek harflari baytlarda.
---

## Nima uchun bu mavzu muhim?

Kompyuter faqat sonlarni saqlaydi. „A“ harfi ham, „ʻ“ belgisi ham, kulgich ham xotirada son koʻrinishida turadi. Qaysi son qaysi belgini bildirishi **kodlash (encoding)** bilan belgilanadi. Kodlashni notoʻgʻri tushunish dasturchilar tez-tez uchratadigan muammolarga olib keladi: „Oʻzbekiston“ oʻrniga „OÊ»zbekiston“ chiqadi, satr uzunligi kutilganidan katta boʻladi, parol tekshiruvi oʻzbekcha harflarda buziladi.

Bu darsda belgilar qanday saqlanishini, C uslubidagi satrlar qanday tuzilganini va oʻzbek tilidagi harflar bilan ishlaganda nimaga eʼtibor berish kerakligini koʻramiz.

:::note
**Bu darsdan soʻng siz:**

- belgining kodini olib, kod boʻyicha belgini tiklay olasiz va belgilar ustida arifmetik amallar bajara olasiz;
- C uslubidagi satrning tuzilishini (`char[]` va `'\0'`) tushuntira olasiz;
- probelli butun qatorni toʻgʻri oʻqiy olasiz (`getline`, `nextLine`);
- belgilarni turlarga ajrata olasiz (harf, raqam, probel);
- ASCII, Unicode va UTF-8 farqini va oʻzbek harflari nima uchun bir nechta bayt egallashini tushuntira olasiz.
:::

## Belgi turi va ASCII

**Belgi (character)** — bitta harf, raqam, tinish belgisi yoki boshqaruv belgisi. Har bir belgi son — **kod** bilan saqlanadi.

**ASCII** — 128 ta belgini (lotin harflari, raqamlar, tinish belgilari, boshqaruv belgilari) 0–127 kodlarga moslaydigan eng qadimiy jadval:

| Belgilar | Kodlar | Eslab qolish uchun |
|---|---|---|
| `'0'` … `'9'` | 48 … 57 | `'0'` = 48 |
| `'A'` … `'Z'` | 65 … 90 | `'A'` = 65 |
| `'a'` … `'z'` | 97 … 122 | `'a'` = 97 |
| probel `' '` | 32 | |
| yangi qator `'\n'` | 10 | |

Muhim xususiyatlar: harflar va raqamlar **ketma-ket** joylashgan; kichik harf kodi katta harfnikidan 32 ga katta.

| Vazifa | Python | Java | C++ |
|---|---|---|---|
| Belgining kodi | `ord('A')` → 65 | `(int) 'A'` → 65 | `(int) 'A'` → 65 |
| Kod boʻyicha belgi | `chr(66)` → `'B'` | `(char) 66` → `'B'` | `(char) 66` → `'B'` |
| Raqam belgisini songa | `ord(c) - ord('0')` | `c - '0'` | `c - '0'` |
| Keyingi harf | `chr(ord(c) + 1)` | `(char) (c + 1)` | `(char) (c + 1)` |

Java va C++'da `char` — butun son turi, unga sonni qoʻshish mumkin. Pythonʼda alohida belgi turi yoʻq: belgi — uzunligi 1 boʻlgan satr, shuning uchun `ord` va `chr` kerak.

## C uslubidagi satr

C++ C tilidan **C-satr**ni meros qilib olgan: belgilar massivi, oxirida **nol belgi** `'\0'` (kodi 0). Satr uzunligi alohida saqlanmaydi — `'\0'` gacha sanaladi.

```text
char s[] = "Salom";
indeks:  0    1    2    3    4    5
belgi:  'S'  'a'  'l'  'o'  'm'  '\0'
```

`"Salom"` 5 ta harfdan iborat, lekin massiv 6 ta joy egallaydi. `<cstring>` dagi `strlen(s)` 5 qaytaradi: u `'\0'` gacha sanaydi. Agar `'\0'` tushib qolsa, `strlen` va chiqarish xotirada keyingi tasodifiy nolgacha davom etadi — chegaradan chiqishning yana bir koʻrinishi.

C-satrlar bilan ishlash xavfli va noqulay (`=` bilan nusxalab boʻlmaydi, `==` manzillarni solishtiradi), shuning uchun C++'da odatda `string` klassi ishlatiladi (M11). Lekin C-satrni tushunish muhim: koʻp kutubxonalar va operatsion tizim funksiyalari aynan shu koʻrinishda ishlaydi.

## Satrni qator boʻyicha oʻqish

`cin >> s` (C++) va `next()` (Java) faqat **bitta soʻzni** — birinchi probelgacha oʻqiydi. Butun qator kerak boʻlsa:

| Til | Butun qatorni oʻqish |
|---|---|
| Python | `input()` — har doim butun qator |
| Java | `in.nextLine()` |
| C++ | `getline(cin, s)` |

Tuzoq: avval son (`cin >> k` yoki `nextInt()`), keyin qator oʻqilsa, sondan keyingi `'\n'` kiritish oqimida qoladi va `getline` / `nextLine` boʻsh qator qaytaradi. Yechim: C++'da `getline` dan oldin `cin.ignore()`, Javaʼda sonni ham `nextLine()` bilan oʻqib, `Integer.parseInt` qilish.

## Unicode va UTF-8

ASCII faqat ingliz alifbosini qamraydi. **Unicode** — dunyodagi barcha yozuvlarning belgilariga (150 000 dan ortiq) yagona raqam beradigan standart: `A` — U+0041, `Ж` — U+0416, `ʻ` — U+02BB, `😀` — U+1F600.

Unicode raqamni xotirada saqlash usuli — **kodlash**:

| Kodlash | Bitta belgi necha bayt | Qayerda |
|---|---|---|
| **UTF-8** | 1–4 bayt; ASCII belgilar — 1 bayt | veb, fayllar, Linux, C++ `string` |
| **UTF-16** | 2 yoki 4 bayt | Java `char` va `String`, Windows |
| **UTF-32** | har doim 4 bayt | kam ishlatiladi |

UTF-8'da oʻzbek harfi `oʻ` ikki belgidan iborat: `o` (1 bayt) va `ʻ` (U+02BB, 2 bayt). Shuning uchun „Oʻzbekiston“ 11 belgi, lekin UTF-8'da 12 bayt.

**Kengaytirilgan belgilar**: C++'da `char` 1 bayt va bitta Unicode belgini sigʻdira olmaydi. Buning uchun `wchar_t` (keng belgi; Windowsʼda 2 bayt, Linuxʼda 4 bayt), `wstring`, shuningdek `char16_t` va `char32_t` turlari bor. Amalda koʻpincha UTF-8 baytlari `string` da saqlanadi va belgilar kerak boʻlganda alohida sanaladi.

| Til | „Oʻzbekiston“ uzunligi | Nimani sanaydi |
|---|---|---|
| Python | `len(s)` → 11 | Unicode belgilar |
| Java | `s.length()` → 11 | UTF-16 birliklari (bu yerda belgilar soni bilan teng) |
| C++ | `s.size()` → 12 | UTF-8 baytlar |

## Ishlangan misol 1: Sezar shifri

Birinchi qatorda siljish *k* (0 ≤ *k* < 26), ikkinchisida matn (lotin harflari, probellar, tinish belgilari). Har bir harfni alifboda *k* ta oʻngga siljiting (Z dan keyin A), katta-kichikligini saqlang; boshqa belgilar oʻzgarmasin.

```python
k = int(input())
matn = input()

natija = []
for c in matn:
    if 'a' <= c <= 'z':
        c = chr((ord(c) - ord('a') + k) % 26 + ord('a'))
    elif 'A' <= c <= 'Z':
        c = chr((ord(c) - ord('A') + k) % 26 + ord('A'))
    natija.append(c)

print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = Integer.parseInt(in.nextLine().trim());
        String matn = in.nextLine();

        StringBuilder natija = new StringBuilder();
        for (char c : matn.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                c = (char) ((c - 'a' + k) % 26 + 'a');
            } else if (c >= 'A' && c <= 'Z') {
                c = (char) ((c - 'A' + k) % 26 + 'A');
            }
            natija.append(c);
        }

        System.out.println(natija);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int k;
    cin >> k;
    cin.ignore();                    // sondan keyingi '\n' ni tashlab yuboramiz
    string matn;
    getline(cin, matn);

    for (char& c : matn) {           // & — belgini joyida oʻzgartirish uchun
        if (c >= 'a' && c <= 'z') {
            c = (char) ((c - 'a' + k) % 26 + 'a');
        } else if (c >= 'A' && c <= 'Z') {
            c = (char) ((c - 'A' + k) % 26 + 'A');
        }
    }

    cout << matn << endl;
    return 0;
}
```

Kirish `3` va `Salom, Dunyo!` boʻlsa:

1. `'S'`: kod 83, `83 - 65 = 18` (alifbodagi oʻrni), `(18 + 3) % 26 = 21`, `21 + 65 = 86` → `'V'`.
2. `'a'` → `'d'`, `'l'` → `'o'`, `'o'` → `'r'`, `'m'` → `'p'`.
3. `','`, probel va `'!'` oʻzgarmaydi.
4. `'y'`: oʻrni 24, `(24 + 3) % 26 = 1` → `'b'` — alifbo boshiga qaytdi.

Chiqadi: `Vdorp, Gxqbr!`. Deshifrlash uchun siljish `26 - k` bilan xuddi shu dastur ishlatiladi. Qoldiq (`% 26`) alifboni „halqa“ga aylantiradi.

## Ishlangan misol 2: belgilarni turlarga ajratish

Bir qator matn kiritiladi (ASCII belgilar). Undagi harflar, raqamlar, probellar va boshqa belgilar sonini chiqaring.

```python
qator = input()

harf = raqam = probel = boshqa = 0
for c in qator:
    if 'a' <= c <= 'z' or 'A' <= c <= 'Z':
        harf += 1
    elif '0' <= c <= '9':
        raqam += 1
    elif c == ' ':
        probel += 1
    else:
        boshqa += 1

print(harf, raqam, probel, boshqa)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String qator = in.nextLine();

        int harf = 0, raqam = 0, probel = 0, boshqa = 0;
        for (int i = 0; i < qator.length(); i++) {
            char c = qator.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                harf++;
            } else if (c >= '0' && c <= '9') {
                raqam++;
            } else if (c == ' ') {
                probel++;
            } else {
                boshqa++;
            }
        }

        System.out.println(harf + " " + raqam + " " + probel + " " + boshqa);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string qator;
    getline(cin, qator);

    int harf = 0, raqam = 0, probel = 0, boshqa = 0;
    for (char c : qator) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            harf++;
        } else if (c >= '0' && c <= '9') {
            raqam++;
        } else if (c == ' ') {
            probel++;
        } else {
            boshqa++;
        }
    }

    cout << harf << " " << raqam << " " << probel << " " << boshqa << endl;
    return 0;
}
```

Kirish `Room 214, AIFU-2026` boʻlsa: harflar R, o, o, m, A, I, F, U — 8; raqamlar 2, 1, 4, 2, 0, 2, 6 — 7; probellar — 2; boshqalar `,` va `-` — 2. Chiqadi: `8 7 2 2`.

Belgilar oraliq bilan tekshirildi — bu ASCIIʼda harflar ketma-ket boʻlgani uchun ishlaydi. Standart funksiyalar ham bor: Python `c.isalpha()`, `c.isdigit()`; Java `Character.isLetter(c)`; C++ `isalpha(c)`, `isdigit(c)` (`<cctype>`). Ular oʻzbekcha va boshqa harflarni turlicha baholashi mumkin, shuning uchun bu misolda oraliqlar ishlatildi.

## Ishlangan misol 3: belgilar va baytlar

Bir qator matn kiritiladi (UTF-8). Undagi belgilar (Unicode) va baytlar sonini chiqaring.

```python
s = input()

belgilar = len(s)                       # Unicode belgilar
baytlar = len(s.encode("utf-8"))        # UTF-8 baytlar

print("Belgilar:", belgilar, "baytlar:", baytlar)
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        String s = in.nextLine();

        int belgilar = s.codePointCount(0, s.length());          // Unicode belgilar
        int baytlar = s.getBytes(StandardCharsets.UTF_8).length; // UTF-8 baytlar

        System.out.println("Belgilar: " + belgilar + " baytlar: " + baytlar);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    getline(cin, s);                 // string UTF-8 baytlarni saqlaydi

    int baytlar = s.size();
    int belgilar = 0;
    for (unsigned char c : s) {
        if ((c & 0xC0) != 0x80) {    // davom baytlari 10xxxxxx — ularni sanamaymiz
            belgilar++;
        }
    }

    cout << "Belgilar: " << belgilar << " baytlar: " << baytlar << endl;
    return 0;
}
```

Kirish `Oʻzbekiston` boʻlsa, chiqadi: `Belgilar: 11 baytlar: 12`. Kirish `Salom` boʻlsa: `Belgilar: 5 baytlar: 5` — ASCII matnda belgilar va baytlar teng.

UTF-8'da har bir belgi bir „boshlovchi“ va 0–3 ta „davom“ baytidan iborat; davom baytlari ikkilikda `10xxxxxx` koʻrinishida. C++ versiyasi faqat davom boʻlmagan baytlarni sanaydi: `c & 0xC0` yuqori ikki bitni ajratadi, `0x80` esa `10000000`. `ʻ` (U+02BB) UTF-8'da `CA BB` — ikki bayt, ulardan biri davom bayti.

:::warning
**Koʻp uchraydigan xatolar**

- **`'5'` ni 5 deb hisoblash.** `'5'` ning kodi 53; raqam qiymati `'5' - '0'`.
- **`cin >> k` dan keyin `getline`.** Qolgan `'\n'` sababli boʻsh qator oʻqiladi; `cin.ignore()` qoʻshing.
- **C-satrda `'\0'` uchun joy qoldirmaslik.** `char s[5] = "Salom";` — 6 ta joy kerak.
- **C++'da `s.size()` ni belgilar soni deb olish.** UTF-8 matnda bu baytlar soni; oʻzbekcha `ʻ` va kirill harflari 2 baytdan.
- **Oʻzbekcha `ʻ` oʻrniga `'` (apostrof) yozish.** Bu boshqa belgi: qidiruv va saralash buziladi.
:::

:::tip
Matn bilan ishlaydigan dasturni albatta oʻzbekcha (`oʻ`, `gʻ`), ruscha va kulgichli matn bilan sinang. ASCIIʼda ishlagan kod boshqa yozuvda kutilmaganda buzilishi mumkin.
:::

## Xulosa

- Belgi xotirada son — kod sifatida saqlanadi; ASCIIʼda `'0'` = 48, `'A'` = 65, `'a'` = 97, harflar ketma-ket.
- Java va C++'da `char` — butun son, Pythonʼda `ord` / `chr` ishlatiladi.
- C-satr — `'\0'` bilan tugaydigan `char` massivi; uzunligi `strlen` bilan sanaladi.
- Butun qator `input()`, `nextLine()`, `getline()` bilan oʻqiladi; sondan keyin qolgan `'\n'` ga eʼtibor bering.
- Unicode har bir belgiga raqam beradi, UTF-8 uni 1–4 baytda saqlaydi; oʻzbekcha `ʻ` — 2 bayt.
- C++'da `wchar_t` va `wstring` — kengaytirilgan belgilar; `string` UTF-8 baytlarni saqlaydi.

## Oʻzingizni tekshiring

:::check
Q: `'a'` va `'A'` kodlari orasidagi farq qancha va undan qanday foydalanish mumkin?
A: 32. Kichik harfni kattaga aylantirish uchun koddan 32 ayirish mumkin: `(char) (c - 32)` (faqat lotin harflari uchun).
Q: `char s[] = "Hi";` massivi necha bayt egallaydi?
A: 3: `'H'`, `'i'` va `'\0'`.
Q: C++'da `cin >> k; getline(cin, s);` dan keyin *s* nima uchun boʻsh boʻlishi mumkin?
A: `cin >> k` sondan keyingi `'\n'` ni oqimda qoldiradi, `getline` shu boʻsh qatorni oʻqiydi. Oldidan `cin.ignore()` kerak.
Q: Sezar shifrida *k* = 3 boʻlsa, `'z'` nimaga aylanadi?
A: `'c'`: oʻrni 25, (25 + 3) % 26 = 2.
Q: Nima uchun „Oʻzbekiston“ UTF-8'da 12 bayt?
A: 10 ta ASCII harf 1 baytdan, `ʻ` (U+02BB) esa 2 bayt: 10 + 2 = 12.
:::

:::link https://www.asciitable.com/
ASCII jadvali
Barcha 128 ta ASCII belgi va ularning kodlari.
:::

:::link https://en.wikipedia.org/wiki/UTF-8
Wikipedia: UTF-8
UTF-8 kodlashining tuzilishi va bayt koʻrinishlari.
:::
