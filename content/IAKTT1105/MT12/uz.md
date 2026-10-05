---
title: Dasturlash tillari haqida umumiy maʼlumot
summary: Mustaqil ish — dasturlash tili tushunchasi; tillar avlodlari (mashina kodi, assembler, yuqori darajali, soʻrov tillari, low-code); kompilyatsiya, interpretatsiya va JIT; paradigmalar (protsedurali, obyektga yoʻnaltirilgan, funksional, deklarativ); tiplash (statik va dinamik); iqtisodchi uchun tillar — Python, R, SQL, VBA, Java, C++, JavaScript, 1C tili; pulni haqiqiy sonlarda hisoblash xavfi va uni koʻrsatuvchi dastur; ishlab chiqish muhitlari va sunʼiy intellekt yordamchilari; til tanlash.
---

## Mustaqil ishning maqsadi

Platformadagi misollar Python, Java va C++ da berilgan. Nima uchun aynan shu tillar? Ular nimasi bilan farq qiladi va iqtisodchi qaysi tilni oʻrganishi foydali? Bu mustaqil ish dasturlash tillari dunyosiga umumiy xarita beradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- dasturlash tillari avlodlari va turlarini ayta olasiz;
- kompilyatsiya va interpretatsiyani farqlay olasiz;
- asosiy paradigmalarni misol bilan tushuntira olasiz;
- iqtisodiy vazifalar uchun mos tilni tanlay olasiz;
- pul hisobida haqiqiy sonlar xavfini bilasiz.
:::

## Tushuncha va avlodlar

**Dasturlash tili** — kompyuterga bajariladigan koʻrsatmalarni yozish uchun qatʼiy qoidalarga (sintaksis va semantika) ega sunʼiy til.

| Avlod | Tavsif | Misol |
|---|---|---|
| **1GL** | mashina kodi — nol va birlar | `10110000 01100001` |
| **2GL** | assembler — mashina buyruqlarining qisqa nomlari | `MOV AL, 61h` |
| **3GL** | yuqori darajali, inson tiliga yaqin | C, Java, Python |
| **4GL** | natijani tasvirlash ("nima kerak", "qanday" emas) | SQL, R ning bir qismi, hisobot generatorlari |
| **Low-code / no-code** | vizual konstruktorlar, minimal kod | Power Apps, Zapier, Bubble |

## Dastur qanday bajariladi

| Usul | Qanday ishlaydi | Afzalligi | Misol |
|---|---|---|---|
| **Kompilyatsiya** | butun dastur oldindan mashina kodiga oʻgiriladi | tez ishlaydi, xatolar oldindan topiladi | C, C++, Go |
| **Interpretatsiya** | dastur qatorma-qator bajariladi | tez sinash, moslashuvchanlik | Python, R, JavaScript (anʼanaviy) |
| **Bayt-kod + JIT** | oraliq kodga kompilyatsiya, bajarishda tezlashtirish | platformadan mustaqillik, yaxshi tezlik | Java, C# |

## Paradigmalar

| Paradigma | Gʻoya | Misol |
|---|---|---|
| **Protsedurali** | dastur — buyruqlar va funksiyalar ketma-ketligi | `hisobla_soliq(summa)` |
| **Obyektga yoʻnaltirilgan (OOP)** | maʼlumot va amallar obyektlarga birlashtiriladi | `hisob.toʻldirish(500)` — "Hisob" obyekti |
| **Funksional** | natija funksiyalar kompozitsiyasi, holat oʻzgarmaydi | `sum(map(soliq, summalar))` |
| **Deklarativ** | "qanday" emas, "nima" kerakligini yozish | `SELECT SUM(summa) FROM savdo` |

Zamonaviy tillar koʻpincha bir nechta paradigmani qoʻllab-quvvatlaydi (Python — protsedurali, OOP va funksional).

**Tiplash**: **statik** (Java, C++) — oʻzgaruvchi turi oldindan eʼlon qilinadi, xatolar kompilyatsiyada topiladi; **dinamik** (Python, R) — tur bajarishda aniqlanadi, yozish tezroq, lekin ayrim xatolar faqat ishlaganda chiqadi.

## Iqtisodchi uchun tillar

| Til | Qayerda | Nimasi bilan foydali |
|---|---|---|
| **Python** | tahlil, avtomatlashtirish, mashinali oʻqitish | oddiy sintaksis, pandas, katta hamjamiyat (MT2, MT9) |
| **R** | statistika, ekonometrika, ilmiy tadqiqot | statistik paketlar, sifatli grafiklar |
| **SQL** | maʼlumotlar bazalari (M7, MT8) | deyarli har bir kompaniyada; tahlilchining asosiy vositasi |
| **VBA** | Excel va Office avtomatlashtirish | makroslar, takroriy hisobotlar |
| **Java, C#** | bank va korporativ tizimlar (M15) | ishonchlilik, katta loyihalar |
| **C++** | yuqori tezlikdagi savdo, hisoblash yadrolari | maksimal tezlik |
| **JavaScript** | veb-saytlar, dashbordlar, Google Sheets skriptlari | brauzerda ishlaydi |
| **1C tili** | 1C:Buxgalteriya va 1C:ERP konfiguratsiyalari | MDH va Oʻzbekistonda buxgalteriya tizimlari |

:::tip
Iqtisodchi uchun samarali yoʻl: **Excel → SQL → Python (yoki R)**. SQL maʼlumot olishni, Python esa tahlil va avtomatlashtirishni beradi.
:::

## Pul va haqiqiy sonlar

Kompyuter `double` (`float`) sonlarni ikkilik sanoq tizimida saqlaydi. 0,1 kabi oddiy oʻnli kasr ikkilikda cheksiz kasr boʻladi va taxminan saqlanadi. Natijada pul hisobida kichik xatolar yigʻiladi — buxgalteriya uchun bu qabul qilinmaydi. Toʻgʻri yechim: pulni **eng kichik birlikda butun son** (tiyin, sent) sifatida yoki maxsus oʻnli tur (`Decimal`, `BigDecimal`) bilan saqlash.

**Ishlangan misol**: birinchi satrda summalar (soʻm, ikki xonali tiyin bilan), ikkinchisida kutilgan jami. Yigʻindini `double` da va tiyinlarda butun son bilan hisoblang, kutilgan qiymatga tengligini tekshiring.

```python
import sys

def tiyin(matn):
    som, tiyinlar = matn.split(".")
    return int(som) * 100 + int(tiyinlar)

satrlar = sys.stdin.read().strip().split("\n")
summalar = satrlar[0].split()
kutilgan = satrlar[1].strip()

haqiqiy = 0.0
for s in summalar:
    haqiqiy += float(s)                    # double da qoʻshish
butun = sum(tiyin(s) for s in summalar)    # tiyinlarda butun son

print(f"double: yigʻindi == {kutilgan} -> {'ha' if haqiqiy == float(kutilgan) else 'yoʻq'}")
print(f"tiyin (butun son): yigʻindi == {tiyin(kutilgan)} -> {'ha' if butun == tiyin(kutilgan) else 'yoʻq'}")
```
```java
import java.util.Scanner;

public class Main {
    static long tiyin(String matn) {
        String[] q = matn.split("\\.");
        return Long.parseLong(q[0]) * 100 + Long.parseLong(q[1]);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] summalar = in.nextLine().trim().split("\\s+");
        String kutilgan = in.nextLine().trim();

        double haqiqiy = 0.0;
        long butun = 0;
        for (String s : summalar) {
            haqiqiy += Double.parseDouble(s);      // double da qoʻshish
            butun += tiyin(s);                     // tiyinlarda butun son
        }
        System.out.println("double: yigʻindi == " + kutilgan + " -> " + (haqiqiy == Double.parseDouble(kutilgan) ? "ha" : "yoʻq"));
        System.out.println("tiyin (butun son): yigʻindi == " + tiyin(kutilgan) + " -> " + (butun == tiyin(kutilgan) ? "ha" : "yoʻq"));
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

long long tiyin(const string& matn) {
    size_t nuqta = matn.find('.');
    return stoll(matn.substr(0, nuqta)) * 100 + stoll(matn.substr(nuqta + 1));
}

int main() {
    string satr, kutilgan, s;
    getline(cin, satr);
    getline(cin, kutilgan);
    kutilgan = kutilgan.substr(0, kutilgan.find_last_not_of(" \r") + 1);

    double haqiqiy = 0.0;
    long long butun = 0;
    istringstream ss(satr);
    while (ss >> s) {
        haqiqiy += stod(s);                        // double da qoʻshish
        butun += tiyin(s);                         // tiyinlarda butun son
    }
    cout << "double: yigʻindi == " << kutilgan << " -> " << (haqiqiy == stod(kutilgan) ? "ha" : "yoʻq") << "\n";
    cout << "tiyin (butun son): yigʻindi == " << tiyin(kutilgan) << " -> " << (butun == tiyin(kutilgan) ? "ha" : "yoʻq") << "\n";
    return 0;
}
```

Kirish:

```text
0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10 0.10
1.00
```

Chiqish:

```text
double: yigʻindi == 1.00 -> yoʻq
tiyin (butun son): yigʻindi == 100 -> ha
```

Oʻnta 10 tiyinning yigʻindisi `double` da 0,9999999999999999 boʻladi va 1,00 ga teng emas! Uchala tilda natija bir xil — bu til xatosi emas, ikkilik sonlarning tabiati. Million tranzaksiyada bunday farqlar hisobot tafovutlariga olib keladi. Shuning uchun bank va buxgalteriya tizimlari pulni tiyinlarda yoki `Decimal` turida saqlaydi; Excel da esa `ROUND` funksiyasidan foydalaniladi.

## Ishlab chiqish muhitlari va yordamchilar

- **IDE** (integratsiyalashgan ishlab chiqish muhiti): Visual Studio Code, PyCharm, IntelliJ IDEA, RStudio — kod tahriri, xatolarni topish (debugger), avtoyakunlash.
- **Notebooklar**: Jupyter, Google Colab — kod, natija va izohlar bitta hujjatda; tahlil uchun qulay.
- **Versiyalarni boshqarish**: Git va GitHub — oʻzgarishlar tarixi, jamoaviy ish.
- **Sunʼiy intellekt yordamchilari** (GitHub Copilot va boshq.) kod yozishni tezlashtiradi, lekin natijani tushunish va tekshirish dasturchining vazifasi (MT1 dagi akademik halollikni eslang).

:::warning
**Koʻp uchraydigan xatolar**

- **"Eng yaxshi til"ni izlash.** Til vazifaga qarab tanlanadi.
- **Pulni `float`/`double` da saqlash.** Tiyinlarda butun son yoki `Decimal` ishlating.
- **Kodni tushunmasdan nusxalash.** Xato va xavfsizlik muammolari bilan birga keladi.
- **Bir vaqtda koʻp tilni oʻrganishga urinish.** Avval bittasini chuqur oʻrganing — keyingilari osonroq boʻladi.
:::

## Topshiriq

1. Platformadagi istalgan misolni Python, Java va C++ da solishtiring: qaysi qismlar oʻxshash, qaysilari farq qiladi (oʻzgaruvchi eʼloni, kirish oʻqish, chiqarish)?
2. Misol dasturini 0.10 oʻrniga 0.25 lar bilan sinab koʻring. Natija nima uchun oʻzgardi? (Ishora: 0,25 = 1/4 ikkilikda aniq ifodalanadi.)
3. Uchta ish eʼlonini tahlil qiling (tahlilchi, buxgalter, moliyachi): qaysi dasturlash tillari va vositalari talab qilinadi?
4. Python da 5 qatorlik dastur yozing: uchta mahsulot narxini kiritib, jami va oʻrtachasini chiqarsin.

## Xulosa

- Tillar avlodlari: mashina kodi → assembler → yuqori darajali → soʻrov tillari → low-code.
- Bajarish: kompilyatsiya, interpretatsiya, bayt-kod va JIT; tiplash statik yoki dinamik.
- Paradigmalar: protsedurali, OOP, funksional, deklarativ.
- Iqtisodchi uchun: SQL, Python yoki R, VBA; korporativ tizimlarda Java, C#, 1C.
- Pulni haqiqiy sonlarda saqlash xatolarga olib keladi — tiyinlarda butun son yoki Decimal.

## Oʻzingizni tekshiring

:::check
Q: Kompilyatsiya va interpretatsiyaning farqi nima?
A: Kompilyatsiyada dastur oldindan butunlay mashina kodiga oʻgiriladi; interpretatsiyada qatorma-qator bajariladi.
Q: SQL qaysi paradigmaga tegishli?
A: Deklarativ — natija "nima" ekanini tasvirlaydi, "qanday" hisoblashni emas.
Q: Nima uchun pulni double da saqlash xavfli?
A: 0,1 kabi oʻnli kasrlar ikkilikda aniq ifodalanmaydi va kichik xatolar yigʻiladi.
Q: Excel ni avtomatlashtirish uchun qaysi til ishlatiladi?
A: VBA (shuningdek, Office Scripts va Python).
:::

:::link https://docs.python.org/3/tutorial/floatingpoint.html
Python: Floating-Point Arithmetic
Haqiqiy sonlar xatolari va ularning sabablari boʻyicha rasmiy tushuntirish (ingliz tilida).
:::
