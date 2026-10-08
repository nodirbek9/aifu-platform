---
title: Satrlar bilan ishlash
summary: Mustaqil ish: satrni soʻzlarga ajratish va qayta yigʻish, ortiqcha boʻsh joylar, soʻz ichidagi belgilarni oʻzgartirish, satr masalasini funksiyalarga boʻlish; namunaviy masala — soʻzlar tartibini teskari aylantirish va eng uzun soʻz; topshiriq — har bir soʻzni bosh harf bilan yozish.
---

## Nima uchun bu mavzu muhim?

Matnni qayta ishlovchi dasturlarning katta qismi bir xil sxema boʻyicha ishlaydi: satr **soʻzlarga ajratiladi**, har bir soʻz alohida qayta ishlanadi va natija **qayta yigʻiladi**. Qidiruv tizimlari, imlo tekshiruvchilar, sarlavhalarni formatlash — hammasi shunday. Bu mustaqil ishda shu sxemani uch tilda mustaqil qoʻllashni mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- satrni ortiqcha boʻsh joylarga qaramay toʻgʻri soʻzlarga ajrata olasiz;
- soʻzlar roʻyxatini kerakli ajratgich bilan qayta yigʻa olasiz;
- soʻz ichidagi alohida belgilarni (masalan, birinchi harfni) oʻzgartira olasiz;
- satr masalasi uchun chekka holatlarni oʻz ichiga olgan testlar jadvalini tuza olasiz.
:::

## Mavzu boʻyicha qisqa takror

„Ajrat — qayta ishla — yigʻ“ sxemasi uch tilda:

| Bosqich | Python | Java | C++ |
|---|---|---|---|
| Ajratish | `s.split()` — ixtiyoriy sondagi boʻsh joy boʻyicha | `s.trim().split("\\s+")` | `istringstream` va `>>` |
| Soʻz ustida ishlash | `soz.upper()`, `soz[0]`, `soz[1:]` | `soz.toUpperCase()`, `soz.charAt(0)`, `soz.substring(1)` | `toupper(soz[0])`, `soz.substr(1)` |
| Yigʻish | `" ".join(sozlar)` | `String.join(" ", sozlar)` | sikl va `+=` |

Muhim nozikliklar:

1. Pythonʼda `s.split()` (argumentsiz) boshidagi, oxiridagi va takroriy boʻsh joylarni tashlab yuboradi; `s.split(" ")` esa **boʻsh soʻzlar** beradi.
2. Javaʼda `split("\\s+")` satr boʻsh joy bilan boshlansa, birinchi element sifatida boʻsh satr qaytaradi — shuning uchun avval `trim()`.
3. C++'da `>>` operatori boʻsh joylarni oʻzi tashlab ketadi — soʻzlarga ajratishning eng oddiy usuli.

## Namunaviy masala: soʻzlar tartibini aylantirish

Bitta satr matn kiritiladi (soʻzlar orasida bir nechta boʻsh joy, boshida va oxirida boʻsh joylar boʻlishi mumkin). Soʻzlarni teskari tartibda, bittadan boʻsh joy bilan chiqaring; keyingi satrda eng uzun soʻzni (bir nechta boʻlsa — birinchisini) chiqaring.

```python
def sozlarga_ajrat(satr):
    return satr.split()                 # ortiqcha boʻsh joylar tashlanadi


def eng_uzun(sozlar):
    natija = sozlar[0]
    for soz in sozlar:
        if len(soz) > len(natija):      # qatʼiy > — birinchisi qoladi
            natija = soz
    return natija


sozlar = sozlarga_ajrat(input())
print(" ".join(reversed(sozlar)))
print(eng_uzun(sozlar))
```
```java
import java.util.Scanner;

public class Main {
    static String[] sozlargaAjrat(String satr) {
        return satr.trim().split("\\s+");    // trim — boshidagi boʻsh soʻz chiqmasligi uchun
    }

    static String engUzun(String[] sozlar) {
        String natija = sozlar[0];
        for (String soz : sozlar) {
            if (soz.length() > natija.length()) natija = soz;   // qatʼiy > — birinchisi qoladi
        }
        return natija;
    }

    public static void main(String[] args) {
        String[] sozlar = sozlargaAjrat(new Scanner(System.in).nextLine());
        StringBuilder sb = new StringBuilder();
        for (int i = sozlar.length - 1; i >= 0; i--) {
            sb.append(sozlar[i]);
            if (i > 0) sb.append(' ');
        }
        System.out.println(sb);
        System.out.println(engUzun(sozlar));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<string> sozlargaAjrat(const string& satr) {
    istringstream oqim(satr);
    vector<string> sozlar;
    string soz;
    while (oqim >> soz) sozlar.push_back(soz);     // >> boʻsh joylarni oʻzi tashlaydi
    return sozlar;
}

string engUzun(const vector<string>& sozlar) {
    string natija = sozlar[0];
    for (const string& soz : sozlar) {
        if (soz.size() > natija.size()) natija = soz;   // qatʼiy > — birinchisi qoladi
    }
    return natija;
}

int main() {
    string satr;
    getline(cin, satr);
    vector<string> sozlar = sozlargaAjrat(satr);
    for (int i = (int) sozlar.size() - 1; i >= 0; i--) {
        cout << sozlar[i];
        if (i > 0) cout << ' ';
    }
    cout << endl << engUzun(sozlar) << endl;
    return 0;
}
```

Testlar jadvali:

| Kirish | Kutilgan natija | Nimani tekshiradi |
|---|---|---|
| `bugun   havo  juda yaxshi` | `yaxshi juda havo bugun`, `yaxshi` | takroriy boʻsh joylar |
| `␣␣salom dunyo␣` | `dunyo salom`, `salom` | boshida va oxirida boʻsh joy; teng uzunlik — birinchisi |
| `yolgiz` | `yolgiz`, `yolgiz` | bitta soʻz |

(`␣` — boʻsh joy belgisi.) Uchala tilda ham masala uchta mustaqil qismga boʻlingan: ajratish, eng uzunini topish va teskari tartibda yigʻish. Har bir qism alohida tekshirilishi mumkin.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: satrdagi **har bir soʻzni bosh harf bilan** boshlang, qolgan harflarni kichik qiling; soʻzlar orasidagi ortiqcha boʻsh joylarni bitta qilib qoldiring (`  dASturLASH   tili ` → `Dasturlash Tili`). Topshiriladigan ish:

1. **Funksiyalar tavsifi** — kamida ikkita funksiya: bitta soʻzni formatlash va butun satrni formatlash.
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 5 ta test: oddiy holat, takroriy va chetki boʻsh joylar, bitta harfli soʻzlar, toʻliq katta harfli soʻzlar, bitta soʻz.
4. **Xulosa** — 3–5 gap: tilingizdagi qaysi funksiyalardan foydalandingiz va qaysi chekka holat eng koʻp xato berdi.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Pythonʼdagi tayyor `str.title()` dan foydalanmang — u apostrof va raqamlardan keyin ham bosh harf qoʻyadi va topshiriq maqsadi — formatlashni oʻzingiz yozish.
- Bitta harfli soʻz (`a`) uchun `soz[1:]` / `substring(1)` boʻsh satr beradi — xato emas, lekin tekshirib koʻring.
- Natija oxirida ortiqcha boʻsh joy qolmasligi kerak.
- Kamida bitta soʻz borligi kafolatlangan, lekin satr boʻsh joy bilan boshlanishi mumkin.
:::

:::tip
Avval bitta soʻzni formatlovchi funksiyani yozib, uni alohida bir necha soʻzda tekshiring (`dASTUR`, `a`, `Ok`). U toʻgʻri ishlagach, butun satr uchun funksiya faqat „ajrat — har biriga qoʻlla — yigʻ“ boʻlib qoladi.
:::

## Xulosa

- Matnni qayta ishlashning asosiy sxemasi: soʻzlarga ajratish → har bir soʻzni qayta ishlash → qayta yigʻish.
- Ortiqcha boʻsh joylar: Python `split()`, Java `trim().split("\\s+")`, C++ `>>`.
- Soʻz ichidagi belgilar indeks va qism satr orqali oʻzgartiriladi.
- Masalani kichik funksiyalarga boʻlish har bir qismni alohida tekshirish imkonini beradi.

## Oʻzingizni tekshiring

:::check
Q: Pythonʼda `"  a  b ".split()` va `"  a  b ".split(" ")` natijalari qanday farq qiladi?
A: Birinchisi `["a", "b"]`, ikkinchisi boʻsh satrlar bilan: `["", "", "a", "", "b", ""]`.
Q: Javaʼda nima uchun `split("\\s+")` dan oldin `trim()` chaqiriladi?
A: Satr boʻsh joy bilan boshlansa, `split` birinchi element sifatida boʻsh satr qaytaradi.
Q: C++'da satrni soʻzlarga ajratishning eng oddiy usuli qaysi?
A: `istringstream` yaratib, undan `>>` bilan soʻzma-soʻz oʻqish.
Q: Teng uzunlikdagi soʻzlardan birinchisini saqlash uchun qaysi taqqoslash ishlatiladi?
A: Qatʼiy `>`: keyingi teng uzunlikdagi soʻz natijani almashtirmaydi.
:::

:::link https://docs.python.org/3/library/stdtypes.html#str.split
Python: str.split
Satrni boʻlish qoidalari va `sep` argumentining taʼsiri (ingliz tilida).
:::
