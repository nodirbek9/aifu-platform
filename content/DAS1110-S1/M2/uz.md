---
title: Dasturlash tillarining tuzilmasi
summary: Til alifbosi, identifikatorlar va kalit soʻzlar, literallar, izohlar, maʼlumot turlari, ifoda va operatorlar, dasturning umumiy tuzilishi.
---

## Nima uchun bu mavzu muhim?

Oʻzbek tilida gap tuzish uchun harflar, soʻzlar, gap boʻlaklari va tinish belgilarini bilish kerak. Dasturlash tili ham xuddi shunday: oʻz alifbosi, „soʻzlari“, „gaplari“ va qatʼiy grammatikasi bor. Faqat bir farq bilan — kompyuter hech qanday xatoni „tushunib“ ketmaydi. Odam „kitop“ deb yozilgan soʻzni oʻqib tushunadi, kompilyator esa `prnt` ni koʻrib toʻxtaydi.

Bu darsda dasturlash tilining „qurilish materiallari“ni koʻrib chiqamiz. Ular Python, Java va C++'da deyarli bir xil: bir tilda oʻrgansangiz, ikkinchisini oʻqish oson boʻladi.

:::note
**Bu darsdan soʻng siz:**

- til alifbosi, identifikator, kalit soʻz va literalni farqlay olasiz;
- toʻgʻri va notoʻgʻri identifikatorlarni ajrata olasiz;
- asosiy maʼlumot turlarini (butun, haqiqiy, mantiqiy, belgi, satr) uch tilda yoza olasiz;
- ifoda, operator va izoh nima ekanini tushuntirib, oddiy dasturning tuzilishini tahlil qila olasiz.
:::

## Alifbo va leksemalar

Har bir dasturlash tilining **alifbosi** bor: lotin harflari (`A–Z`, `a–z`), raqamlar (`0–9`), maxsus belgilar (`+ - * / = < > ( ) [ ] { } ; , . : " '`) va boʻshliq belgilari (probel, tabulyatsiya, yangi qator).

Kompilyator yoki interpretator dastur matnini avval **leksemalarga (tokens)** — maʼnoli eng kichik boʻlaklarga ajratadi. Masalan, `narx = 2500 * soni;` qatori quyidagi leksemalardan iborat:

| Leksema | Turi |
|---|---|
| `narx` | identifikator |
| `=` | amal (taʼminlash) |
| `2500` | literal (butun son) |
| `*` | amal (koʻpaytirish) |
| `soni` | identifikator |
| `;` | ajratuvchi |

## Identifikatorlar

**Identifikator (identifier)** — oʻzgaruvchi, funksiya, klass kabi dastur elementining nomi. Uni dasturchi oʻzi tanlaydi, lekin qoidalarga rioya qilishi shart:

1. Faqat harf, raqam va pastki chiziq (`_`) dan iborat boʻladi.
2. Raqam bilan boshlanmaydi: `2son` — notoʻgʻri, `son2` — toʻgʻri.
3. Kalit soʻz boʻla olmaydi: `int`, `class`, `for` — nom boʻlolmaydi.
4. Katta-kichik harf farqlanadi: `Narx`, `narx` va `NARX` — uch xil nom.

| Nom | Toʻgʻrimi? | Sababi |
|---|---|---|
| `talaba_soni` | ha | harf va pastki chiziq |
| `jami2026` | ha | raqam oxirida |
| `_temp` | ha | pastki chiziq bilan boshlanishi mumkin |
| `2-guruh` | yoʻq | raqam bilan boshlanadi va `-` belgisi bor |
| `oʻrtacha` | tavsiya etilmaydi | `ʻ` belgisi koʻp tillarda ruxsat etilmaydi |
| `for` | yoʻq | kalit soʻz |

**Yaxshi nom** — maʼnosini aytib turadigan nom: `x` emas, `talabalar_soni`; `a1` emas, `oylik_maosh`. Python'da soʻzlar pastki chiziq bilan ajratiladi (`oylik_maosh`), Java va C++'da koʻpincha „tuya“ uslubi ishlatiladi (`oylikMaosh`).

## Kalit soʻzlar

**Kalit (xizmatchi) soʻzlar (keywords)** — til uchun maxsus maʼnoga ega boʻlgan, oldindan band qilingan soʻzlar. Ularni nom sifatida ishlatib boʻlmaydi.

| Vazifasi | Python | Java | C++ |
|---|---|---|---|
| Shart | `if`, `elif`, `else` | `if`, `else`, `switch` | `if`, `else`, `switch` |
| Takrorlash | `for`, `while` | `for`, `while`, `do` | `for`, `while`, `do` |
| Funksiya | `def`, `return` | `return` | `return` |
| Maʼlumot turi | — | `int`, `double`, `boolean`, `char` | `int`, `double`, `bool`, `char` |
| Mantiqiy qiymat | `True`, `False` | `true`, `false` | `true`, `false` |

## Literallar va maʼlumot turlari

**Literal** — dastur matnida toʻgʻridan-toʻgʻri yozilgan qiymat: `42`, `3.14`, `'A'`, `"Salom"`, `true`.

Har bir qiymatning **turi (type)** bor. Tur qiymat xotirada qanday saqlanishini va u bilan qanday amallar bajarish mumkinligini belgilaydi.

| Tur | Maʼnosi | Python | Java | C++ | Misol |
|---|---|---|---|---|---|
| Butun son | manfiy, nol, musbat butun | `int` | `int`, `long` | `int`, `long long` | `-7`, `0`, `2026` |
| Haqiqiy son | kasr qismi bor | `float` | `double` | `double` | `3.14`, `-0.5` |
| Mantiqiy | rost yoki yolgʻon | `bool` | `boolean` | `bool` | `True` / `true` |
| Belgi | bitta belgi | — (1 harfli `str`) | `char` | `char` | `'A'` |
| Satr | belgilar ketma-ketligi | `str` | `String` | `string` | `"Salom"` |

Java va C++ **statik turlashgan** tillar: oʻzgaruvchining turi eʼlon qilinganda yoziladi va keyin oʻzgarmaydi (`int yosh = 19;`). Python **dinamik turlashgan**: tur qiymatdan aniqlanadi (`yosh = 19`).

## Ifodalar va operatorlar

**Ifoda (expression)** — qiymatga ega boʻlgan birikma: `2 + 3`, `narx * soni`, `yosh >= 18`. **Operator (statement)** esa bajariladigan buyruq: taʼminlash, chiqarish, shart, takrorlash.

Asosiy amallar:

| Amal | Belgi | Misol | Natija |
|---|---|---|---|
| Qoʻshish, ayirish | `+`, `-` | `7 - 2` | `5` |
| Koʻpaytirish | `*` | `7 * 2` | `14` |
| Boʻlish | `/` | `7 / 2` | Python: `3.5`, Java/C++ (butun sonlar): `3` |
| Butun boʻlish | `//` (Python), `/` (Java/C++, butunlar uchun) | `7 // 2` | `3` |
| Qoldiq | `%` | `7 % 2` | `1` |
| Solishtirish | `==`, `!=`, `<`, `>`, `<=`, `>=` | `7 > 2` | rost |
| Taʼminlash | `=` | `x = 5` | *x* ga 5 yoziladi |

`=` va `==` ni farqlang: `=` qiymat beradi, `==` esa tenglikni tekshiradi.

## Izohlar

**Izoh (comment)** — kompilyator eʼtiborga olmaydigan, odam uchun yozilgan matn. U kod *nima uchun* shunday yozilganini tushuntiradi.

- Python: `# bir qatorli izoh`
- Java va C++: `// bir qatorli izoh` va `/* koʻp qatorli izoh */`

## Dasturning umumiy tuzilishi

Quyidagi dastur kiritilgan doira radiusi boʻyicha uning yuzini hisoblaydi. Unda shu darsda koʻrilgan barcha elementlar bor: izoh, identifikator, kalit soʻz, literal, tur, ifoda va operatorlar.

```python
# Doira yuzini hisoblash: S = pi * r^2
PI = 3.14159            # literal 3.14159, nomi PI

r = float(input())      # haqiqiy son kiritiladi
s = PI * r * r          # ifoda
print(round(s, 2))      # natija 2 xona aniqlikda
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Doira yuzini hisoblash: S = pi * r^2
        final double PI = 3.14159;   // oʻzgarmas (final)

        Scanner in = new Scanner(System.in);
        double r = in.nextDouble();  // haqiqiy son kiritiladi
        double s = PI * r * r;       // ifoda
        System.out.printf("%.2f%n", s);
    }
}
```
```cpp
#include <iostream>
#include <iomanip>
using namespace std;

int main() {
    // Doira yuzini hisoblash: S = pi * r^2
    const double PI = 3.14159;   // oʻzgarmas (const)

    double r;
    cin >> r;                    // haqiqiy son kiritiladi
    double s = PI * r * r;       // ifoda
    cout << fixed << setprecision(2) << s << endl;
    return 0;
}
```

Kirish `5` boʻlsa, uchala dastur ham `78.54` chiqaradi:

1. `PI` — oʻzgarmas nom; Java'da `final`, C++'da `const` uning qiymati oʻzgarmasligini kafolatlaydi. Python'da oʻzgarmaslik kelishuv boʻyicha — nom katta harflarda yoziladi.
2. Radius haqiqiy son sifatida oʻqiladi: `float(...)`, `nextDouble()`, `cin >> r`.
3. `PI * r * r` ifodasi hisoblanib, *s* ga yoziladi: 3.14159 · 25 = 78.53975.
4. Natija ikki kasr xonagacha yaxlitlanib chiqariladi.

:::warning
**Koʻp uchraydigan xatolar**

- **`=` va `==` ni chalkashtirish.** `if (x = 5)` C++'da xato emas, lekin har doim rost — mantiqiy xato.
- **Butun sonlarni boʻlish.** Java va C++'da `7 / 2` natijasi `3`. Kasr kerak boʻlsa, sonlardan biri haqiqiy boʻlishi kerak: `7.0 / 2`.
- **Nomlarda oʻzbekcha ʻ belgisi yoki probel.** `oʻrtacha ball` — nom emas. `ortacha_ball` deb yozing.
- **Izohsiz murakkab kod.** Bir haftadan keyin oʻzingiz ham tushunmay qolasiz.
:::

:::tip
Nom tanlashda „bu nomni boshqa odam oʻqisa, izohsiz tushunadimi?“ deb oʻzingizdan soʻrang. Yaxshi tanlangan nom — eng yaxshi izoh.
:::

## Xulosa

- Dastur matni leksemalardan iborat: identifikatorlar, kalit soʻzlar, literallar, amallar va ajratuvchilar.
- Identifikator harf yoki `_` bilan boshlanadi, harf, raqam va `_` dan iborat, kalit soʻz boʻlolmaydi; katta-kichik harf farqlanadi.
- Asosiy turlar: butun, haqiqiy, mantiqiy, belgi, satr. Java va C++ statik, Python dinamik turlashgan.
- Ifoda — qiymat beradi, operator — bajariladi; `=` taʼminlash, `==` solishtirish.
- Izohlar kod nima uchun shunday yozilganini tushuntiradi.

## Oʻzingizni tekshiring

:::check
Q: `2guruh`, `guruh_2`, `class`, `_soni` — qaysilari toʻgʻri identifikator?
A: `guruh_2` va `_soni`. `2guruh` raqam bilan boshlanadi, `class` esa Java va C++'da kalit soʻz.
Q: Java'da `int a = 7, b = 2; System.out.println(a / b);` nima chiqaradi?
A: `3`. Ikkala son ham butun, shuning uchun boʻlish butun qismini qaytaradi.
Q: Statik va dinamik turlashgan til oʻrtasidagi farq nima?
A: Statik turlashgan tilda (Java, C++) oʻzgaruvchining turi eʼlon qilinganda yoziladi va oʻzgarmaydi. Dinamik turlashgan tilda (Python) tur qiymatdan aniqlanadi va oʻzgaruvchiga keyin boshqa turdagi qiymat berilishi mumkin.
Q: `x = 5` va `x == 5` ning farqi nimada?
A: Birinchisi *x* ga 5 ni yozadi (taʼminlash), ikkinchisi *x* 5 ga tengligini tekshiradi va rost yoki yolgʻon qaytaradi.
Q: `17 % 5` va `17 // 5` (Python) qiymatlari qanday?
A: `17 % 5` = 2 (qoldiq), `17 // 5` = 3 (butun boʻlinma).
:::

:::link https://docs.python.org/3/reference/lexical_analysis.html
Python: leksik tahlil (identifikatorlar va kalit soʻzlar)
Python tilining rasmiy maʼlumotnomasi.
:::

:::link https://en.cppreference.com/w/cpp/language/types
cppreference: C++ fundamental turlari
Butun, haqiqiy, mantiqiy va belgi turlarining oʻlchami va chegaralari.
:::
