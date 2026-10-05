---
title: Maʼlumotlarni statistik tahlil qilish vositalari
summary: Mustaqil ish — statistik tahlil bosqichlari (savol, maʼlumot yigʻish, tozalash, tavsifiy tahlil, vizualizatsiya, xulosa); tavsifiy statistika koʻrsatkichlari (oʻrtacha, mediana, moda, standart chetlanish, variatsiya koeffitsiyenti, kvartillar); bogʻliqlik — korrelyatsiya va regressiya; vositalar — Excel funksiyalari va "Analysis ToolPak", SPSS, Stata, R, Python (pandas), Power BI; korrelyatsiya va sababiyat; sotuv va reklama tahlili dasturi.
---

## Mustaqil ishning maqsadi

M4 va S4 da Excel da formulalar va jamlama jadvallar bilan ishladik. Bu mustaqil ishda statistik tahlilni tizimli koʻrib chiqamiz: qanday koʻrsatkichlar hisoblanadi, ular nimani anglatadi va qaysi vositalar bilan bajariladi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- statistik tahlil bosqichlarini ayta olasiz;
- asosiy tavsifiy koʻrsatkichlarni hisoblay va izohlay olasiz;
- korrelyatsiya va regressiya gʻoyasini tushuntira olasiz;
- turli statistik vositalarni vazifaga qarab tanlay olasiz.
:::

## Statistik tahlil bosqichlari

1. **Savol qoʻyish**: "Reklama xarajati sotuvga taʼsir qiladimi?"
2. **Maʼlumot yigʻish**: buxgalteriya, CRM, soʻrovnomalar, rasmiy statistika (stat.uz).
3. **Tozalash**: boʻsh qiymatlar, takrorlar, xato kiritilgan raqamlar, birliklarni moslashtirish (mln va ming soʻm).
4. **Tavsifiy tahlil**: oʻrtacha, tarqoqlik, taqsimot shakli.
5. **Vizualizatsiya**: grafiklar va diagrammalar (M4, M5).
6. **Bogʻliqlik va xulosa**: korrelyatsiya, regressiya, gipotezalarni tekshirish; natijani biznes tilida izohlash.

:::tip
Tahlil vaqtining katta qismi (koʻpincha yarmidan koʻpi) maʼlumotlarni tozalashga ketadi. "Axlat kirsa — axlat chiqadi": iflos maʼlumotdan toʻgʻri xulosa chiqmaydi.
:::

## Tavsifiy statistika

| Koʻrsatkich | Maʼnosi | Excel funksiyasi |
|---|---|---|
| **Oʻrtacha** | qiymatlar yigʻindisi / soni | `AVERAGE` |
| **Mediana** | tartiblangan qatorning oʻrtasidagi qiymat; chetki qiymatlarga sezgir emas | `MEDIAN` |
| **Moda** | eng koʻp uchraydigan qiymat | `MODE.SNGL` |
| **Standart chetlanish** | qiymatlarning oʻrtachadan oʻrtacha uzoqligi | `STDEV.S` (tanlanma), `STDEV.P` (bosh toʻplam) |
| **Variatsiya koeffitsiyenti** | standart chetlanish / oʻrtacha × 100%; turli oʻlchamdagi qatorlarni solishtirish uchun | formula bilan |
| **Kvartillar** | qatorni toʻrt teng qismga boʻluvchi qiymatlar | `QUARTILE.INC` |
| **Min, max, kenglik** | eng kichik, eng katta qiymat va ularning farqi | `MIN`, `MAX` |

**Oʻrtacha yoki mediana?** Ish haqi, uy narxlari kabi notekis taqsimlangan maʼlumotlarda bir nechta juda katta qiymat oʻrtachani "tortib ketadi". Masalan, 9 xodim 5 mln, rahbar 50 mln soʻm olsa, oʻrtacha 9,5 mln, mediana esa 5 mln — xodimlarning odatiy maoshini mediana yaxshiroq koʻrsatadi.

## Bogʻliqlik: korrelyatsiya va regressiya

- **Korrelyatsiya koeffitsiyenti** (r, Excel: `CORREL`) −1 dan 1 gacha: 1 ga yaqin — kuchli toʻgʻri bogʻliqlik, −1 ga yaqin — kuchli teskari, 0 ga yaqin — chiziqli bogʻliqlik yoʻq.
- **Chiziqli regressiya** (Excel: `SLOPE`, `INTERCEPT`, `LINEST`, diagrammada trend chizigʻi): y = a + b·x; masalan, reklamaga qoʻshimcha 1 mln soʻm sarflansa, sotuv oʻrtacha b mln ga oshadi.
- **R²** (determinatsiya koeffitsiyenti) — y dagi oʻzgarishning qancha qismi modelda tushuntirilganini koʻrsatadi.

:::warning
**Korrelyatsiya sababiyat emas.** Muzqaymoq sotuvi va suvda choʻkish hodisalari birga oshadi — ikkalasiga ham issiq havo sabab. Reklama va sotuv birga oshgani reklama sotuvni oshirganini isbotlamaydi: ikkalasi ham mavsumga bogʻliq boʻlishi mumkin. Sababiyatni tekshirish uchun tajriba (A/B test) yoki maxsus ekonometrik usullar kerak.
:::

## Statistik tahlil vositalari

| Vosita | Kuchli tomoni | Kimga mos |
|---|---|---|
| **Excel** + "Analysis ToolPak" (Maʼlumotlar → Tahlil) | hamma joyda bor; tavsifiy statistika, gistogramma, korrelyatsiya, regressiya bir necha bosishda | boshlangʻich va oʻrta tahlil |
| **Google Sheets** | bulutda, birgalikda ishlash | jamoaviy kichik tahlil |
| **SPSS** | menyular orqali, soʻrovnomalar tahlili, testlar | sotsiologlar, marketologlar |
| **Stata, EViews** | ekonometrika, vaqt qatorlari, panel maʼlumotlar | iqtisodchi-tadqiqotchilar |
| **R** | bepul, minglab statistik paketlar, sifatli grafiklar | tadqiqot, akademik ish |
| **Python** (pandas, statsmodels, matplotlib) | bepul, katta maʼlumotlar, avtomatlashtirish, mashinali oʻqitish | tahlilchilar, Big Data (M7, S7) |
| **Power BI, Tableau** | interaktiv dashbordlar, rahbariyat uchun hisobotlar | biznes-tahlil |

## Ishlangan misol: sotuv va reklama

Kompaniyaning 12 oylik sotuvi (birinchi satr) va reklama xarajati (ikkinchi satr), mln soʻm. Tavsifiy koʻrsatkichlar va sotuvning reklama bilan korrelyatsiyasini hisoblang. Bu Excel dagi `AVERAGE`, `MEDIAN`, `STDEV.S` va `CORREL` funksiyalarining "ichidan" koʻrinishi.

```python
import math
import sys

satrlar = sys.stdin.read().strip().split("\n")
sotuv = [float(x) for x in satrlar[0].split()]       # oylik sotuv, mln soʻm
reklama = [float(x) for x in satrlar[1].split()]     # oylik reklama xarajati
n = len(sotuv)

ortacha = sum(sotuv) / n
tartib = sorted(sotuv)
if n % 2 == 1:
    mediana = tartib[n // 2]
else:
    mediana = (tartib[n // 2 - 1] + tartib[n // 2]) / 2
# tanlanma standart chetlanishi (Excel dagi STDEV.S)
sd = math.sqrt(sum((x - ortacha) ** 2 for x in sotuv) / (n - 1))
cv = sd / ortacha * 100

# korrelyatsiya koeffitsiyenti (Excel dagi CORREL)
ortacha_r = sum(reklama) / n
kov = sum((x - ortacha) * (y - ortacha_r) for x, y in zip(sotuv, reklama))
sx = sum((x - ortacha) ** 2 for x in sotuv)
sy = sum((y - ortacha_r) ** 2 for y in reklama)
r = kov / math.sqrt(sx * sy)

print(f"Oylar: {n}")
print(f"Oʻrtacha: {ortacha:.2f}")
print(f"Mediana: {mediana:.2f}")
print(f"Standart chetlanish: {sd:.2f}")
print(f"Variatsiya koeffitsiyenti: {cv:.2f}%")
print(f"Min: {min(sotuv):.0f}, max: {max(sotuv):.0f}")
print(f"Reklama bilan korrelyatsiya: {r:.2f}")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static double[] oqi(String satr) {
        return Arrays.stream(satr.trim().split("\\s+")).mapToDouble(Double::parseDouble).toArray();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double[] sotuv = oqi(in.nextLine());        // oylik sotuv, mln soʻm
        double[] reklama = oqi(in.nextLine());      // oylik reklama xarajati
        int n = sotuv.length;

        double ortacha = Arrays.stream(sotuv).sum() / n;
        double[] tartib = sotuv.clone();
        Arrays.sort(tartib);
        double mediana = n % 2 == 1 ? tartib[n / 2] : (tartib[n / 2 - 1] + tartib[n / 2]) / 2;
        // tanlanma standart chetlanishi (Excel dagi STDEV.S)
        double sx = 0;
        for (double x : sotuv) sx += (x - ortacha) * (x - ortacha);
        double sd = Math.sqrt(sx / (n - 1));
        double cv = sd / ortacha * 100;

        // korrelyatsiya koeffitsiyenti (Excel dagi CORREL)
        double ortachaR = Arrays.stream(reklama).sum() / n;
        double kov = 0, sy = 0;
        for (int i = 0; i < n; i++) {
            kov += (sotuv[i] - ortacha) * (reklama[i] - ortachaR);
            sy += (reklama[i] - ortachaR) * (reklama[i] - ortachaR);
        }
        double r = kov / Math.sqrt(sx * sy);

        System.out.println("Oylar: " + n);
        System.out.println(String.format("Oʻrtacha: %.2f", ortacha));
        System.out.println(String.format("Mediana: %.2f", mediana));
        System.out.println(String.format("Standart chetlanish: %.2f", sd));
        System.out.println(String.format("Variatsiya koeffitsiyenti: %.2f%%", cv));
        System.out.println(String.format("Min: %.0f, max: %.0f", tartib[0], tartib[n - 1]));
        System.out.println(String.format("Reklama bilan korrelyatsiya: %.2f", r));
    }
}
```
```cpp
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<double> oqi(const string& satr) {
    istringstream ss(satr);
    vector<double> v;
    double x;
    while (ss >> x) v.push_back(x);
    return v;
}

int main() {
    string a, b;
    getline(cin, a);
    getline(cin, b);
    vector<double> sotuv = oqi(a);          // oylik sotuv, mln soʻm
    vector<double> reklama = oqi(b);        // oylik reklama xarajati
    int n = sotuv.size();

    double ortacha = 0;
    for (double x : sotuv) ortacha += x;
    ortacha /= n;
    vector<double> tartib = sotuv;
    sort(tartib.begin(), tartib.end());
    double mediana = n % 2 == 1 ? tartib[n / 2] : (tartib[n / 2 - 1] + tartib[n / 2]) / 2;
    // tanlanma standart chetlanishi (Excel dagi STDEV.S)
    double sx = 0;
    for (double x : sotuv) sx += (x - ortacha) * (x - ortacha);
    double sd = sqrt(sx / (n - 1));
    double cv = sd / ortacha * 100;

    // korrelyatsiya koeffitsiyenti (Excel dagi CORREL)
    double ortachaR = 0;
    for (double y : reklama) ortachaR += y;
    ortachaR /= n;
    double kov = 0, sy = 0;
    for (int i = 0; i < n; i++) {
        kov += (sotuv[i] - ortacha) * (reklama[i] - ortachaR);
        sy += (reklama[i] - ortachaR) * (reklama[i] - ortachaR);
    }
    double r = kov / sqrt(sx * sy);

    printf("Oylar: %d\n", n);
    printf("Oʻrtacha: %.2f\n", ortacha);
    printf("Mediana: %.2f\n", mediana);
    printf("Standart chetlanish: %.2f\n", sd);
    printf("Variatsiya koeffitsiyenti: %.2f%%\n", cv);
    printf("Min: %.0f, max: %.0f\n", tartib[0], tartib[n - 1]);
    printf("Reklama bilan korrelyatsiya: %.2f\n", r);
    return 0;
}
```

Kirish:

```text
42 38 45 51 47 55 60 58 49 44 53 62
3 2 4 5 4 6 7 6 5 3 5 8
```

Chiqish:

```text
Oylar: 12
Oʻrtacha: 50.33
Mediana: 50.00
Standart chetlanish: 7.51
Variatsiya koeffitsiyenti: 14.92%
Min: 38, max: 62
Reklama bilan korrelyatsiya: 0.98
```

Sotuv oʻrtacha 50 mln soʻm atrofida, tarqoqlik oʻrtacha (CV ≈ 15%), oʻrtacha va mediana yaqin — taqsimot taxminan simmetrik. Reklama bilan korrelyatsiya juda kuchli (0,98), lekin bu hali reklama sotuvni oshirganini isbotlamaydi: yozgi oylarda ikkalasi ham yuqori. Keyingi qadam — bir necha hududda reklamani oʻzgartirib tajriba qilish.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat oʻrtachaga qarash.** Tarqoqlik va chetki qiymatlar ham muhim; mediana bilan solishtiring.
- **STDEV.S va STDEV.P ni adashtirish.** Tanlanma (bir qism) uchun STDEV.S ishlatiladi.
- **Korrelyatsiyadan sababiyat xulosasi chiqarish.**
- **Maʼlumotni tozalamasdan tahlil qilish.** Bitta xato kiritilgan nol butun natijani buzadi.
:::

## Topshiriq

1. Misoldagi maʼlumotlarni Excel ga kiriting va `AVERAGE`, `MEDIAN`, `STDEV.S`, `CORREL` bilan natijalarni tekshiring.
2. "Analysis ToolPak" ni yoqing (Fayl → Parametrlar → Qoʻshimchalar) va "Tavsifiy statistika" hisobotini oling.
3. Sotuv (y) va reklama (x) uchun nuqtali diagramma quring, trend chizigʻi va R² ni chiqaring.
4. stat.uz saytidan biror koʻrsatkichning 10 yillik qatorini oling va tavsifiy tahlil qiling.

## Xulosa

- Tahlil: savol → yigʻish → tozalash → tavsif → vizualizatsiya → xulosa.
- Tavsifiy koʻrsatkichlar: oʻrtacha, mediana, moda, standart chetlanish, variatsiya koeffitsiyenti, kvartillar.
- Bogʻliqlik: korrelyatsiya (r), regressiya (y = a + b·x), R²; korrelyatsiya sababiyat emas.
- Vositalar: Excel va ToolPak, SPSS, Stata, R, Python, Power BI — vazifa va hajmga qarab.

## Oʻzingizni tekshiring

:::check
Q: Qachon mediana oʻrtachadan yaxshiroq?
A: Maʼlumotlar notekis taqsimlangan yoki juda katta chetki qiymatlar boʻlganda (masalan, ish haqi).
Q: Variatsiya koeffitsiyenti nima uchun kerak?
A: Turli oʻlchamdagi qatorlarning tarqoqligini nisbiy koʻrinishda solishtirish uchun.
Q: r = −0,9 nimani bildiradi?
A: Kuchli teskari chiziqli bogʻliqlikni: biri oshganda ikkinchisi kamayadi.
Q: Excel da tanlanma standart chetlanishi qaysi funksiya bilan hisoblanadi?
A: STDEV.S.
:::

:::link https://stat.uz/
Oʻzbekiston Respublikasi Prezidenti huzuridagi Statistika agentligi
Iqtisodiy va ijtimoiy koʻrsatkichlarning rasmiy statistik maʼlumotlari.
:::
