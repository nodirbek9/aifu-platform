---
title: Satrlar bilan ishlovchi funksiyalar
summary: Satrning standart funksiyalari amaliyotda — boʻsh joylarni olib tashlash, boshlanishi va tugashini tekshirish, qidirish va qismini ajratish, sanash, birlashtirish, satr yigʻuvchilar (StringBuilder); elektron pochtani tahlil qilish, satrni siqish (RLE) va matnni qatorlarga boʻlish.
---

## Nima uchun bu mavzu muhim?

Foydalanuvchi kiritgan maʼlumotning deyarli hammasi — ism, telefon, manzil, parol, xabar — satr koʻrinishida keladi. Uni tekshirish, tozalash va kerakli qismlarga ajratish — har qanday dasturning kundalik ishi. Birinchi semestrda satrlarning asosiy amallari bilan tanishdik; endi ularni **birgalikda** ishlatib, real masalalarni yechamiz.

Bu mashgʻulotda uch tilning satr funksiyalari jadvalini toʻldiramiz va ularni uch amaliy masalada qoʻllaymiz: elektron pochta manzilini tekshirish, satrni siqish va matnni berilgan kenglikdagi qatorlarga boʻlish.

:::note
**Bu mashgʻulotdan soʻng siz:**

- satrning asosiy funksiyalarini uch tilda tanlab ishlata olasiz;
- satrni qismlarga ajratib, har bir qismni tekshira olasiz;
- satrni samarali yigʻish uchun `StringBuilder` va `join` dan foydalana olasiz;
- satr ustidagi masalani bir nechta oddiy funksiyalar ketma-ketligiga boʻla olasiz.
:::

## Satr funksiyalari jadvali

| Vazifa | Python | Java | C++ (`std::string`) |
|---|---|---|---|
| uzunlik | `len(s)` | `s.length()` | `s.size()` |
| boshi/oxiridagi boʻsh joylarni olib tashlash | `s.strip()` | `s.trim()` / `s.strip()` | qoʻlda: `find_first_not_of(' ')` va `find_last_not_of(' ')` |
| … bilan boshlanadimi / tugaydimi | `s.startswith(p)`, `s.endswith(p)` | `s.startsWith(p)`, `s.endsWith(p)` | `s.rfind(p, 0) == 0`; C++20 — `s.starts_with(p)` |
| qism satrni qidirish | `s.find(p)` (yoʻq — `-1`) | `s.indexOf(p)` (yoʻq — `-1`) | `s.find(p)` (yoʻq — `string::npos`) |
| oxirgi uchrash | `s.rfind(p)` | `s.lastIndexOf(p)` | `s.rfind(p)` |
| qism satr | `s[i:j]` | `s.substring(i, j)` | `s.substr(i, uzunlik)` |
| belgi necha marta uchraydi | `s.count(c)` | sikl yoki `s.chars().filter(...).count()` | `count(s.begin(), s.end(), c)` |
| kichik harflarga | `s.lower()` | `s.toLowerCase()` | sikl bilan `tolower` |
| boʻlish | `s.split(ajratgich)` | `s.split(regex)` | `istringstream` yoki `find` + `substr` |
| birlashtirish | `ajratgich.join(roʻyxat)` | `String.join(ajratgich, roʻyxat)` | sikl bilan `+=` |

C++'da `substr` ning ikkinchi argumenti — **uzunlik**, Python va Javaʼda esa **oxirgi indeks** (unga kirmaydi). Bu farq koʻp xatoga sabab boʻladi.

## Satrni samarali yigʻish

Java va Pythonʼda satr **oʻzgarmas**: `s = s + c` har safar yangi satr yaratib, eskisini nusxalaydi. Sikl ichida *n* marta qoʻshish *O*(*n*²) vaqt oladi. Toʻgʻri usul:

| Til | Samarali yigʻish |
|---|---|
| Python | qismlarni roʻyxatga `append` qilib, oxirida `"".join(qismlar)` |
| Java | `StringBuilder sb`; `sb.append(...)`; oxirida `sb.toString()` |
| C++ | `string` oʻzgaruvchan: `s += c` oʻrtacha *O*(1) |

## Ishlangan misol 1: elektron pochtani tekshirish

Birinchi satrda *n*, keyingi *n* satrda elektron pochta manzili (boshida va oxirida boʻsh joy boʻlishi mumkin). Har bir manzil uchun: agar unda aynan bitta `@` boʻlsa, `@` dan oldingi qism (login) boʻsh boʻlmasa, keyingi qism (domen) nuqta bilan boshlanmasa va kamida bitta nuqtaga ega boʻlsa — login va domenni kichik harflarda chiqaring; aks holda `Xato`.

```python
def tekshir(manzil):
    manzil = manzil.strip().lower()
    if manzil.count("@") != 1:
        return None
    login, domen = manzil.split("@")
    if login == "" or domen.startswith(".") or "." not in domen:
        return None
    return login, domen


n = int(input())
for _ in range(n):
    natija = tekshir(input())
    print(*natija if natija else ["Xato"])
```
```java
import java.util.Scanner;

public class Main {
    static String tekshir(String manzil) {
        manzil = manzil.trim().toLowerCase();
        int belgi = manzil.indexOf('@');
        if (belgi == -1 || belgi != manzil.lastIndexOf('@')) return null;   // aynan bitta @
        String login = manzil.substring(0, belgi);
        String domen = manzil.substring(belgi + 1);
        if (login.isEmpty() || domen.startsWith(".") || !domen.contains(".")) return null;
        return login + " " + domen;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String natija = tekshir(in.nextLine());
            System.out.println(natija != null ? natija : "Xato");
        }
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

string tozala(const string& s) {                        // strip + lower
    size_t bosh = s.find_first_not_of(' ');
    if (bosh == string::npos) return "";
    size_t oxir = s.find_last_not_of(' ');
    string natija = s.substr(bosh, oxir - bosh + 1);
    for (char& c : natija) c = tolower(c);
    return natija;
}

string tekshir(const string& kirish) {
    string manzil = tozala(kirish);
    if (count(manzil.begin(), manzil.end(), '@') != 1) return "";   // aynan bitta @
    size_t belgi = manzil.find('@');
    string login = manzil.substr(0, belgi);
    string domen = manzil.substr(belgi + 1);
    if (login.empty() || domen.empty() || domen[0] == '.' || domen.find('.') == string::npos) return "";
    return login + " " + domen;
}

int main() {
    int n;
    cin >> n;
    string satr;
    getline(cin, satr);                                 // n dan keyingi satr oxiri
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        string natija = tekshir(satr);
        cout << (natija.empty() ? "Xato" : natija) << endl;
    }
    return 0;
}
```

Kirish:

```text
4
  Aziz.Karimov@AIFU.uz
malika@@mail.uz
@aifu.uz
bekzod@localhost
```

Chiqish:

```text
aziz.karimov aifu.uz
Xato
Xato
Xato
```

Har bir shart alohida, oddiy funksiya bilan tekshirildi: `strip`, `lower`, `count`, `split`, `startswith`. Javaʼda „aynan bitta `@`“ sharti `indexOf` va `lastIndexOf` bir xil joyni koʻrsatishi bilan tekshiriladi. C++'da `getline` dan oldin `n` dan keyin qolgan satr oxiri oʻqib tashlanadi — aks holda birinchi manzil boʻsh satr boʻlib qoladi. Haqiqiy tizimlarda manzil qoidalari ancha murakkab, lekin tahlil tamoyili xuddi shunday.

## Ishlangan misol 2: satrni siqish (RLE)

Bitta satr kichik lotin harflaridan iborat. Uni **RLE** (run-length encoding) usulida siqing: ketma-ket keluvchi bir xil harflar guruhi „harf + soni“ bilan almashtiriladi (`aaabcc` → `a3b1c2`). Siqilgan satrni va uni qayta ochib, asl satr bilan mosligini (`ha`/`yoʻq`) chiqaring.

```python
def siq(s):
    qismlar = []
    i = 0
    while i < len(s):
        j = i
        while j < len(s) and s[j] == s[i]:
            j += 1
        qismlar.append(s[i] + str(j - i))        # harf va guruh uzunligi
        i = j
    return "".join(qismlar)


def och(s):
    qismlar = []
    i = 0
    while i < len(s):
        harf = s[i]
        j = i + 1
        while j < len(s) and s[j].isdigit():
            j += 1
        qismlar.append(harf * int(s[i + 1:j]))
        i = j
    return "".join(qismlar)


satr = input().strip()
siqilgan = siq(satr)
print(siqilgan)
print("ha" if och(siqilgan) == satr else "yoʻq")
```
```java
import java.util.Scanner;

public class Main {
    static String siq(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int j = i;
            while (j < s.length() && s.charAt(j) == s.charAt(i)) j++;
            sb.append(s.charAt(i)).append(j - i);         // harf va guruh uzunligi
            i = j;
        }
        return sb.toString();
    }

    static String och(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char harf = s.charAt(i);
            int j = i + 1;
            while (j < s.length() && Character.isDigit(s.charAt(j))) j++;
            sb.append(String.valueOf(harf).repeat(Integer.parseInt(s.substring(i + 1, j))));
            i = j;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String satr = new Scanner(System.in).nextLine().trim();
        String siqilgan = siq(satr);
        System.out.println(siqilgan);
        System.out.println(och(siqilgan).equals(satr) ? "ha" : "yoʻq");
    }
}
```
```cpp
#include <cctype>
#include <iostream>
#include <string>
using namespace std;

string siq(const string& s) {
    string natija;
    size_t i = 0;
    while (i < s.size()) {
        size_t j = i;
        while (j < s.size() && s[j] == s[i]) j++;
        natija += s[i];
        natija += to_string(j - i);                     // harf va guruh uzunligi
        i = j;
    }
    return natija;
}

string och(const string& s) {
    string natija;
    size_t i = 0;
    while (i < s.size()) {
        char harf = s[i];
        size_t j = i + 1;
        while (j < s.size() && isdigit(s[j])) j++;
        natija += string(stoi(s.substr(i + 1, j - i - 1)), harf);
        i = j;
    }
    return natija;
}

int main() {
    string satr;
    cin >> satr;
    string siqilgan = siq(satr);
    cout << siqilgan << endl;
    cout << (och(siqilgan) == satr ? "ha" : "yoʻq") << endl;
    return 0;
}
```

`aaabccddddddddddddx` kiritilsa, chiqish: `a3b1c2d12x1` va `ha`. Ochishda raqamlar bir nechta boʻlishi mumkin (`d12`), shuning uchun harfdan keyin **raqam boʻlmagan belgigacha** oʻqiladi. C++'dagi `s.substr(i + 1, j - i - 1)` — ikkinchi argument uzunlik ekanini eslang. RLE rasm formatlari (BMP, TIFF) va faks uzatishda ishlatiladi: bir xil rangli uzun qatorlar juda yaxshi siqiladi, `abc` kabi takrorsiz satr esa aksincha uzayadi (`a1b1c1`).

## Ishlangan misol 3: matnni qatorlarga boʻlish

Birinchi satrda kenglik *W*, ikkinchisida matn. Soʻzlarni tartibini saqlab, har bir qator uzunligi *W* dan oshmaydigan qilib qatorlarga joylang: soʻz joriy qatorga sigʻsa (soʻzlar orasida bitta boʻsh joy), unga qoʻshiladi, aks holda yangi qator boshlanadi. Har bir soʻz uzunligi *W* dan oshmaydi. Har bir qatorni `|` belgilari orasida, *W* gacha boʻsh joy bilan toʻldirib chiqaring.

```python
w = int(input())
sozlar = input().split()

qatorlar = []
joriy = []
uzunlik = 0
for soz in sozlar:
    kerak = len(soz) if not joriy else uzunlik + 1 + len(soz)
    if kerak <= w:
        joriy.append(soz)
        uzunlik = kerak
    else:
        qatorlar.append(" ".join(joriy))
        joriy = [soz]
        uzunlik = len(soz)
qatorlar.append(" ".join(joriy))

for q in qatorlar:
    print("|" + q.ljust(w) + "|")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int w = Integer.parseInt(in.nextLine().trim());
        String[] sozlar = in.nextLine().trim().split("\\s+");

        List<String> qatorlar = new ArrayList<>();
        StringBuilder joriy = new StringBuilder();
        for (String soz : sozlar) {
            int kerak = joriy.length() == 0 ? soz.length() : joriy.length() + 1 + soz.length();
            if (kerak <= w) {
                if (joriy.length() > 0) joriy.append(' ');
                joriy.append(soz);
            } else {
                qatorlar.add(joriy.toString());
                joriy = new StringBuilder(soz);
            }
        }
        qatorlar.add(joriy.toString());

        for (String q : qatorlar) {
            System.out.println("|" + q + " ".repeat(w - q.length()) + "|");
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int w;
    cin >> w;
    string satr;
    getline(cin, satr);                                 // w dan keyingi satr oxiri
    getline(cin, satr);
    istringstream oqim(satr);

    vector<string> qatorlar;
    string joriy, soz;
    while (oqim >> soz) {
        size_t kerak = joriy.empty() ? soz.size() : joriy.size() + 1 + soz.size();
        if ((int) kerak <= w) {
            if (!joriy.empty()) joriy += ' ';
            joriy += soz;
        } else {
            qatorlar.push_back(joriy);
            joriy = soz;
        }
    }
    qatorlar.push_back(joriy);

    for (const string& q : qatorlar) {
        cout << "|" << q << string(w - q.size(), ' ') << "|" << endl;
    }
    return 0;
}
```

Kirish:

```text
16
Dasturlash har kuni ozgina mashq qilishni talab qiladi
```

Chiqish:

```text
|Dasturlash har  |
|kuni ozgina     |
|mashq qilishni  |
|talab qiladi    |
```

Har bir soʻz uchun „qoʻshilsa, qator uzunligi qancha boʻladi?“ (`kerak`) hisoblanadi: birinchi soʻzdan oldin boʻsh joy kerak emas, keyingilaridan oldin — bitta. Bu **ochkoʻz** (greedy) usul: har bir qatorga imkon qadar koʻp soʻz joylanadi. Matn muharrirlari, terminal va SMS xabarlarini boʻlish xuddi shu algoritmdan foydalanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **C++ `substr` ning ikkinchi argumentini indeks deb olish.** `s.substr(i, j)` — *i* dan boshlab *j* ta belgi, *j*-indeksgacha emas.
- **`find` natijasini notoʻgʻri tekshirish.** C++'da topilmasa `string::npos` qaytadi, `-1` yoki `0` emas; Pythonʼda `find` topilmasa `-1`, `index` esa xato beradi.
- **`cin >>` dan keyin `getline`.** Satr oxiri qolib ketadi va `getline` boʻsh satr oʻqiydi; oraliqda bitta `getline` bilan uni oʻqib tashlang.
- **Sikl ichida satrni `+` bilan yigʻish (Java, Python).** *O*(*n*²); `StringBuilder` yoki `join` ishlating.
:::

:::tip
Satr masalasini yechishdan oldin chekka holatlar roʻyxatini tuzing: boʻsh satr, bitta belgi, faqat boʻsh joylar, boshida va oxirida boʻsh joy, bir xil belgilar. Ularning har biri uchun kutilgan natijani yozib, dasturni tekshiring.
:::

## Xulosa

- Satr funksiyalari uch tilda deyarli bir xil vazifalarni bajaradi, lekin nomlari va chekka holatlardagi xatti-harakati farq qiladi.
- C++ `substr(i, uzunlik)`, Python `s[i:j]` va Java `substring(i, j)` — oxirgi indeks.
- Java va Pythonʼda satrni sikl ichida `StringBuilder` yoki `join` bilan yigʻing.
- Satr masalasini kichik funksiyalarga (tozalash, tekshirish, ajratish, yigʻish) boʻlish uni soddalashtiradi.
- `cin >>` dan keyin `getline` ishlatishda qolgan satr oxirini unutmang.

## Oʻzingizni tekshiring

:::check
Q: Pythonʼda `"  salom  ".strip()` nima qaytaradi?
A: `"salom"` — boshidagi va oxiridagi boʻsh joylar olib tashlanadi.
Q: C++'da `string("dasturlash").substr(3, 4)` nima qaytaradi?
A: `"turl"` — 3-indeksdan boshlab 4 ta belgi.
Q: Javaʼda `"a@b@c".indexOf('@') != "a@b@c".lastIndexOf('@')` nimani bildiradi?
A: Satrda `@` kamida ikki marta uchraydi.
Q: `aabbbc` satri RLE bilan qanday siqiladi?
A: `a2b3c1`.
Q: Nima uchun Javaʼda sikl ichida `s = s + c` sekin?
A: Satr oʻzgarmas: har bir qoʻshish yangi satr yaratib, eskisini nusxalaydi — jami *O*(*n*²).
:::

:::link https://docs.python.org/3/library/stdtypes.html#string-methods
Python: satr metodlari
`strip`, `split`, `join`, `find`, `count` va boshqa metodlarning toʻliq roʻyxati (ingliz tilida).
:::

:::link https://en.cppreference.com/w/cpp/string/basic_string
C++: std::string
`find`, `substr`, `rfind` va boshqa aʼzo funksiyalar (ingliz tilida).
:::
