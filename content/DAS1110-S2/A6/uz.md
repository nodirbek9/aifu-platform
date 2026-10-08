---
title: Satrlar va kengaytirilgan belgilar. ASCII kodlari
summary: ASCII jadvalining tuzilishi, belgi va uning kodi orasida oʻtish, katta-kichik harfni kod orqali almashtirish, raqam belgisini songa aylantirish, harflar chastotasini kod boʻyicha indekslangan massivda sanash; kengaytirilgan belgilar bilan ehtiyotkorlik.
---

## Nima uchun bu mavzu muhim?

Kompyuter uchun har bir belgi — bu son. `A` — 65, `a` — 97, `0` — 48, boʻsh joy — 32. Birinchi semestrda belgi turi va ASCII jadvali bilan tanishgan edik. Bu jadvalning **tuzilishini** bilish koʻp masalalarni juda sodda qiladi: harflar va raqamlar jadvalda ketma-ket joylashgan, shuning uchun belgilar ustida oddiy arifmetika bajarish mumkin.

Bu amaliy mashgʻulotda belgilar kodlari bilan ishlashni mashq qilamiz: harf registrini almashtirish, raqamli satrni songa aylantirish va har bir harf necha marta uchrashini sanash — bularning hammasi tayyor funksiyalarsiz, faqat kodlar orqali.

:::note
**Bu mashgʻulotdan soʻng siz:**

- ASCII jadvalidagi asosiy oraliqlarni (raqamlar, katta va kichik harflar) ayta olasiz;
- belgidan kodga va koddan belgiga uch tilda oʻta olasiz;
- harf registrini kodlar farqi (32) orqali almashtira olasiz;
- raqamlar satrini tayyor funksiyasiz songa aylantira olasiz;
- harflar chastotasini `kod − 'a'` indeksli massivda sanay olasiz.
:::

## ASCII jadvalining tuzilishi

ASCII — 0 dan 127 gacha kodli 128 ta belgi. Eng koʻp kerak boʻladigan oraliqlar:

| Belgilar | Kodlar | Eslab qolish |
|---|---|---|
| boshqaruv belgilari (`\n` = 10, `\t` = 9) | 0–31 | chiqarilmaydi |
| boʻsh joy | 32 | |
| `0` … `9` | 48–57 | `'0'` + raqam |
| `A` … `Z` | 65–90 | `'A'` + harf tartibi |
| `a` … `z` | 97–122 | `'a'` + harf tartibi |

Muhim xossalar:

- raqam belgisi qiymati: `c - '0'` (masalan, `'7' - '0'` = 7);
- harfning alifbodagi oʻrni: `c - 'a'` (masalan, `'d' - 'a'` = 3);
- katta va kichik harf kodi **32** ga farq qiladi: `'a' - 'A'` = 32.

128 dan katta kodli belgilar (oʻzbekcha `ʻ`, kirill harflari, `€`) ASCIIʼga kirmaydi — ular Unicodeʼda boʻlib, UTF-8 da bir nechta bayt bilan yoziladi. Shuning uchun bu mashgʻulotdagi usullar faqat ASCII harflar uchun toʻgʻri.

## Belgi va kod orasida oʻtish

| Amal | Python | Java | C++ |
|---|---|---|---|
| belgi → kod | `ord(c)` | `(int) c` | `(int) c` |
| kod → belgi | `chr(k)` | `(char) k` | `(char) k` |
| raqam qiymati | `ord(c) - ord('0')` | `c - '0'` | `c - '0'` |
| harf ekanini tekshirish | `'a' <= c <= 'z'` | `c >= 'a' && c <= 'z'` | `c >= 'a' && c <= 'z'` |

Java va C++'da `char` — butun son turi, shuning uchun `c - '0'` toʻgʻridan-toʻgʻri ishlaydi. Pythonda belgi — uzunligi 1 boʻlgan satr, son emas: arifmetika uchun `ord` va `chr` shart. Javaʼda `char + int` natijasi `int` boʻladi — belgi kerak boʻlsa, `(char)` bilan qaytariladi.

## Ishlangan misol 1: registrni almashtirish

Bitta satr kiritiladi (ASCII belgilar). Har bir kichik harfni kattasiga, kattasini kichigiga almashtiring; boshqa belgilar oʻzgarmaydi. Tayyor `upper`/`lower` funksiyalaridan foydalanmang — faqat kodlar.

```python
satr = input()
natija = []
for c in satr:
    k = ord(c)
    if 97 <= k <= 122:              # 'a'..'z'
        natija.append(chr(k - 32))
    elif 65 <= k <= 90:             # 'A'..'Z'
        natija.append(chr(k + 32))
    else:
        natija.append(c)
print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine();
        StringBuilder natija = new StringBuilder();
        for (char c : satr.toCharArray()) {
            if (c >= 'a' && c <= 'z') natija.append((char) (c - 32));
            else if (c >= 'A' && c <= 'Z') natija.append((char) (c + 32));
            else natija.append(c);
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
    string satr;
    getline(cin, satr);
    for (char& c : satr) {                     // & — satrning oʻzini oʻzgartiramiz
        if (c >= 'a' && c <= 'z') c = c - 32;
        else if (c >= 'A' && c <= 'Z') c = c + 32;
    }
    cout << satr << endl;
    return 0;
}
```

`Salom, Dunyo! 2024` kiritilsa, chiqish: `sALOM, dUNYO! 2024`. Pythonʼda `97` va `122` oʻrniga `ord('a')` va `ord('z')` yozish ham mumkin — kodni yod olish shart emas. C++'da satr belgilari toʻgʻridan-toʻgʻri oʻzgartiriladi; Java va Pythonʼda satr oʻzgarmas, shuning uchun yangi satr yigʻiladi.

## Ishlangan misol 2: raqamlar satrini songa aylantirish

Bitta satrda ishorali butun son yoziladi (boshida `-` boʻlishi mumkin, uzunligi 18 raqamgacha). Uni `int()`, `parseInt`, `stoi` kabi tayyor funksiyalarsiz songa aylantiring va soni hamda raqamlari yigʻindisini chiqaring. Satrda raqamdan boshqa belgi uchrasa, `Xato` chiqaring.

```python
satr = input().strip()
manfiy = satr.startswith("-")
if manfiy:
    satr = satr[1:]

son = 0
yigindi = 0
togri = len(satr) > 0
for c in satr:
    if not ("0" <= c <= "9"):
        togri = False
        break
    raqam = ord(c) - ord("0")
    son = son * 10 + raqam                  # Horner sxemasi
    yigindi += raqam

if togri:
    print(-son if manfiy else son, yigindi)
else:
    print("Xato")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine().trim();
        boolean manfiy = satr.startsWith("-");
        if (manfiy) satr = satr.substring(1);

        long son = 0;
        int yigindi = 0;
        boolean togri = satr.length() > 0;
        for (char c : satr.toCharArray()) {
            if (c < '0' || c > '9') {
                togri = false;
                break;
            }
            int raqam = c - '0';
            son = son * 10 + raqam;                 // Horner sxemasi
            yigindi += raqam;
        }

        if (togri) System.out.println((manfiy ? -son : son) + " " + yigindi);
        else System.out.println("Xato");
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    cin >> satr;
    bool manfiy = !satr.empty() && satr[0] == '-';
    if (manfiy) satr = satr.substr(1);

    long long son = 0;
    int yigindi = 0;
    bool togri = !satr.empty();
    for (char c : satr) {
        if (c < '0' || c > '9') {
            togri = false;
            break;
        }
        int raqam = c - '0';
        son = son * 10 + raqam;                     // Horner sxemasi
        yigindi += raqam;
    }

    if (togri) cout << (manfiy ? -son : son) << " " << yigindi << endl;
    else cout << "Xato" << endl;
    return 0;
}
```

`-4071` kiritilsa — `-4071 12`; `12a5` kiritilsa — `Xato`. `son = son * 10 + raqam` — **Horner sxemasi**: `"407"` uchun 0 → 4 → 40 → 407. Tayyor `int()` funksiyalari ichida aynan shu jarayon bajariladi. Faqat `-` dan iborat satr ham `Xato` beradi: undan keyin raqam yoʻq.

## Ishlangan misol 3: harflar chastotasi

Bitta satr matn kiritiladi. Katta-kichik harfni farqlamasdan, har bir lotin harfi necha marta uchraganini sanang va uchragan harflarni alifbo tartibida gistogramma koʻrinishida chiqaring: harf, soni va shuncha yulduzcha.

```python
satr = input()
soni = [0] * 26                             # soni[0] — 'a', soni[25] — 'z'
for c in satr:
    if "A" <= c <= "Z":
        c = chr(ord(c) + 32)                # kichik harfga
    if "a" <= c <= "z":
        soni[ord(c) - ord("a")] += 1

for i in range(26):
    if soni[i] > 0:
        print(chr(ord("a") + i), soni[i], "*" * soni[i])
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine();
        int[] soni = new int[26];                       // soni[0] — 'a', soni[25] — 'z'
        for (char c : satr.toCharArray()) {
            if (c >= 'A' && c <= 'Z') c = (char) (c + 32);   // kichik harfga
            if (c >= 'a' && c <= 'z') soni[c - 'a']++;
        }

        for (int i = 0; i < 26; i++) {
            if (soni[i] > 0) {
                System.out.println((char) ('a' + i) + " " + soni[i] + " " + "*".repeat(soni[i]));
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int soni[26] = {0};                             // soni[0] — 'a', soni[25] — 'z'
    for (char c : satr) {
        if (c >= 'A' && c <= 'Z') c = c + 32;       // kichik harfga
        if (c >= 'a' && c <= 'z') soni[c - 'a']++;
    }

    for (int i = 0; i < 26; i++) {
        if (soni[i] > 0) {
            cout << (char) ('a' + i) << " " << soni[i] << " " << string(soni[i], '*') << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
Banan va Ananas!
```

Chiqish:

```text
a 6 ******
b 1 *
n 4 ****
s 1 *
v 1 *
```

Lugʻat (`dict`, `Map`) oʻrniga 26 elementli massiv ishlatildi: harf kodi `c - 'a'` toʻgʻridan-toʻgʻri indeks boʻladi va harflar avtomatik alifbo tartibida chiqadi. Javaʼda `(char) ('a' + i)` muhim: `'a' + i` — `int`, qavssiz son chiqar edi. Bu usul kriptografiyada (Sezar shifrini buzish), matn tahlilida va arxivlashda keng qoʻllanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Javaʼda `char + int` ni belgi deb oʻylash.** `'a' + 1` — 98 soni; belgi uchun `(char) ('a' + 1)`.
- **Pythonda belgidan son ayirish.** `c - '0'` xato beradi; `ord(c) - ord('0')` yozing.
- **Raqam belgisini sonning oʻzi deb olish.** `'7'` ning kodi 55, qiymati esa 7; `c - '0'` ni unutmang.
- **ASCII usulini oʻzbekcha harflarga qoʻllash.** `ʻ`, `ш` kabi belgilar 127 dan katta; `c - 'a'` ular uchun massiv chegarasidan chiqadi — avval `'a' <= c <= 'z'` ni tekshiring.
:::

:::tip
Kodlarni yod olish shart emas: `'A'`, `'a'`, `'0'` kabi belgi literallarini yozing — kompilyator ularni kodga aylantiradi va dastur oʻqilishi osonroq boʻladi.
:::

## Xulosa

- ASCIIʼda raqamlar (48–57), katta harflar (65–90) va kichik harflar (97–122) ketma-ket joylashgan.
- Belgi → kod: `ord` / `(int)`; kod → belgi: `chr` / `(char)`.
- Raqam qiymati `c - '0'`, harf tartibi `c - 'a'`, registr farqi 32.
- Raqamlar satri songa Horner sxemasi bilan aylantiriladi: `son = son * 10 + raqam`.
- Harflar chastotasi uchun 26 elementli massiv `c - 'a'` indeks bilan qulay.

## Oʻzingizni tekshiring

:::check
Q: `'5' - '0'` nimaga teng?
A: 5 ga: `'5'` ning kodi 53, `'0'` niki 48.
Q: `'G'` ni kichik harfga kod orqali qanday aylantirish mumkin?
A: Kodiga 32 qoʻshib: `(char) ('G' + 32)` = `'g'`.
Q: Javaʼda `System.out.println('a' + 1);` nima chiqaradi?
A: 98 — `char` va `int` yigʻindisi `int`.
Q: `"305"` satri Horner sxemasi bilan qanday qadamlarda songa aylanadi?
A: 0 → 3 → 30 → 305.
Q: Nima uchun harflarni sanashda `soni[c - 'a']` dan oldin harf ekanini tekshirish kerak?
A: Boshqa belgilar uchun `c - 'a'` manfiy yoki 25 dan katta boʻlib, massiv chegarasidan chiqadi.
:::

:::link https://en.wikipedia.org/wiki/ASCII#Printable_characters
ASCII jadvali
Chop etiladigan belgilar va ularning kodlari (ingliz tilida).
:::

:::link https://docs.python.org/3/library/functions.html#ord
Python: ord va chr
Belgi va uning Unicode kodi orasida oʻtish funksiyalari (ingliz tilida).
:::
