---
title: Microsoft Excel dasturida maʼlumotlarni koʻrish, qayta ishlash va umumlashtirish vositalari
summary: Seminar — katta jadvallar bilan ishlash: jadval sifatida formatlash (Format as Table), satr va ustunlarni mahkamlash, saralash va filtrlash, kengaytirilgan filtr, shartli formatlash, maʼlumotlarni tekshirish (Data Validation), dublikatlarni olib tashlash, oraliq natijalar (Subtotal), yigʻma jadvallar (PivotTable) va yigʻma diagrammalar, "agar … boʻlsa" tahlili (Goal Seek, ssenariylar); amaliy topshiriqlar; yigʻma jadval gʻoyasini dasturda koʻrsatish.
---

## Seminarning maqsadi

M4 maʼruzasida formulalar va funksiyalarni koʻrdik. Real ishda jadvallar yuzlab va minglab satrdan iborat boʻladi: bir yillik savdo, mijozlar roʻyxati, xodimlar ish haqi. Seminarda bunday maʼlumotlarni tez **koʻrish**, **tozalash** va **umumlashtirish** vositalarini mashq qilamiz.

:::note
**Bu seminardan soʻng siz:**

- katta jadvalni qulay koʻrish uchun sozlay olasiz (jadval formati, mahkamlash);
- maʼlumotlarni saralash, filtrlash va shartli formatlash bilan tahlil qila olasiz;
- maʼlumotlarni tekshirish va tozalash vositalaridan foydalana olasiz;
- yigʻma jadval va yigʻma diagramma tuza olasiz;
- "agar … boʻlsa" tahlilini (Goal Seek) bajara olasiz.
:::

## Koʻrish vositalari

| Vosita | Qayerda | Nima beradi |
|---|---|---|
| **Jadval sifatida formatlash** | Home → Format as Table (`Ctrl + T`) | avtomatik filtrlar, chiziqli ranglash, formulalar yangi satrlarga oʻzi tarqaladi, ustun nomi boʻyicha murojaat |
| **Satr/ustunni mahkamlash** | View → Freeze Panes | sarlavha aylantirganda koʻrinib turadi |
| **Guruhlash** | Data → Group | satr yoki ustunlarni yigʻib-yoyish |
| **Bir nechta oyna** | View → New Window, Arrange All | bitta faylning turli qismlarini yonma-yon koʻrish |

## Qayta ishlash vositalari

| Vosita | Mazmuni |
|---|---|
| **Saralash** | bir nechta daraja: masalan, avval hudud (A→Ya), keyin summa (kamayish) |
| **Filtr** | qiymat, matn, son va sana shartlari ("10 dan katta", "Toshkent", "shu oy") |
| **Kengaytirilgan filtr** | murakkab shartlar (VA/YOKI) alohida shartlar jadvalida |
| **Shartli formatlash** | rang shkalalari, maʼlumot ustunchalari, ikonkalar, formula asosidagi qoidalar |
| **Maʼlumotlarni tekshirish** (Data Validation) | katakka faqat roʻyxatdagi yoki chegaradagi qiymat kiritish — xatolar oldini oladi |
| **Dublikatlarni olib tashlash** | Data → Remove Duplicates |
| **Matnni ustunlarga ajratish** | Data → Text to Columns (masalan, "Familiya Ism" → ikki ustun) |
| **Flash Fill** | namuna boʻyicha avtomatik toʻldirish (`Ctrl + E`) |

## Umumlashtirish vositalari

**Oraliq natijalar** (Data → Subtotal): saralangan jadvalda har bir guruh (masalan, hudud) uchun yigʻindi yoki oʻrtacha qoʻshadi.

**Yigʻma jadval** (Insert → PivotTable) — katta jadvalni bir necha sichqoncha harakati bilan umumlashtiradi:

| Maydon | Vazifasi | Misol |
|---|---|---|
| **Rows** (satrlar) | guruhlash | hudud |
| **Columns** (ustunlar) | ikkinchi guruhlash | tovar yoki oy |
| **Values** (qiymatlar) | hisoblash: Sum, Count, Average, Max | savdo summasi yigʻindisi |
| **Filters** (filtrlar) | butun jadvalni filtrlash | yil |

Qiymatlarni "% of Grand Total" koʻrinishida koʻrsatish, sanalarni oy va chorak boʻyicha guruhlash, **kesimlar** (Slicers) bilan interaktiv filtrlash mumkin. Yigʻma jadval asosida **yigʻma diagramma** (PivotChart) tuziladi. Manba maʼlumot oʻzgarsa: PivotTable Analyze → Refresh.

**"Agar … boʻlsa" tahlili**:

- **Goal Seek** (Data → What-If Analysis): kerakli natijaga erishish uchun bitta kirish qiymatini topadi. Masalan, foyda 50 mln soʻm boʻlishi uchun qancha mahsulot sotish kerak?
- **Ssenariylar menejeri**: optimistik, asosiy, pessimistik variantlarni saqlash va solishtirish.
- **Maʼlumotlar jadvali** (Data Table): bir yoki ikki parametr oʻzgarganda natijalar jadvali.

## Ishlangan misol: yigʻma jadval gʻoyasi

Yigʻma jadval aslida maʼlumotni **guruhlab, umumlashtiradi**. Quyidagi dastur savdo yozuvlarini (hudud, tovar, summa) hudud boʻyicha guruhlab, har bir hudud uchun yigʻindi, yozuvlar soni va umumiy summadagi ulushni chiqaradi — Excel da Rows = hudud, Values = summa yigʻindisi va yozuvlar soni, "% of Grand Total" bilan bir xil natija.

```python
import sys

jami, soni = {}, {}
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    hudud, tovar, summa = satr.split()
    jami[hudud] = jami.get(hudud, 0) + int(summa)     # Values: Sum
    soni[hudud] = soni.get(hudud, 0) + 1              # Values: Count
umumiy = sum(jami.values())
for h in sorted(jami):                                # Rows: hudud (alifbo boʻyicha)
    print(f"{h}: {jami[h]} ({soni[h]} ta, {100 * jami[h] / umumiy:.1f}%)")
print(f"Jami: {umumiy}")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Long> jami = new TreeMap<>();            // Rows: hudud (alifbo boʻyicha)
        Map<String, Integer> soni = new TreeMap<>();
        while (in.hasNext()) {
            String hudud = in.next(), tovar = in.next();
            long summa = in.nextLong();
            jami.merge(hudud, summa, Long::sum);              // Values: Sum
            soni.merge(hudud, 1, Integer::sum);               // Values: Count
        }
        long umumiy = 0;
        for (long v : jami.values()) umumiy += v;
        for (String h : jami.keySet()) {
            System.out.println(String.format("%s: %d (%d ta, %.1f%%)", h, jami.get(h), soni.get(h), 100.0 * jami.get(h) / umumiy));
        }
        System.out.println("Jami: " + umumiy);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    map<string, long long> jami;                             // Rows: hudud (alifbo boʻyicha)
    map<string, int> soni;
    string hudud, tovar;
    long long summa;
    while (cin >> hudud >> tovar >> summa) {
        jami[hudud] += summa;                                // Values: Sum
        soni[hudud] += 1;                                    // Values: Count
    }
    long long umumiy = 0;
    for (auto& p : jami) umumiy += p.second;
    for (auto& p : jami) {
        printf("%s: %lld (%d ta, %.1f%%)\n", p.first.c_str(), p.second, soni[p.first], 100.0 * p.second / umumiy);
    }
    printf("Jami: %lld\n", umumiy);
    return 0;
}
```

Kirish:

```text
Toshkent choy 1200000
Samarqand choy 800000
Toshkent qahva 900000
Buxoro shakar 450000
Samarqand qahva 650000
Toshkent shakar 300000
Buxoro choy 500000
```

Chiqish:

```text
Buxoro: 950000 (2 ta, 19.8%)
Samarqand: 1450000 (2 ta, 30.2%)
Toshkent: 2400000 (3 ta, 50.0%)
Jami: 4800000
```

Xuddi shu maʼlumotni Excel ga kiritib, yigʻma jadval tuzing va natijani solishtiring. Keyin Columns maydoniga "tovar" ni qoʻshing — hudud × tovar kesimidagi jadval hosil boʻladi. Dastur va yigʻma jadval bir xil ishni bajaradi, lekin minglab satrlar va takroriy hisobotlar uchun avtomatlashtirish (dastur yoki Power Query) qulayroq.

## Amaliy topshiriqlar

1. Kamida 30 satrli savdo jadvalini tuzing (sana, hudud, tovar, miqdor, narx, summa) va uni jadval sifatida formatlang.
2. Sarlavha satrini mahkamlang; avval hudud, keyin summa boʻyicha saralang.
3. Filtr bilan faqat bitta hudud va oxirgi oy savdosini koʻrsating.
4. Summa ustuniga rang shkalasi va 1 mln soʻmdan katta qiymatlar uchun qoida qoʻying.
5. "Hudud" ustuniga Data Validation bilan ochiladigan roʻyxat qoʻying.
6. Yigʻma jadval tuzing: Rows — hudud, Columns — tovar, Values — summa yigʻindisi; ulushlarni "% of Grand Total" da koʻrsating; yigʻma diagramma qoʻshing.
7. Goal Seek bilan: umumiy savdo 10% oshishi uchun bitta tovar narxi qancha boʻlishi kerak?

:::warning
**Koʻp uchraydigan xatolar**

- **Jadvalda boʻsh satr va birlashtirilgan kataklar.** Saralash, filtr va yigʻma jadval notoʻgʻri ishlaydi.
- **Bir ustunda turli turdagi maʼlumot.** Sonlar va matn aralashsa, yigʻindi notoʻgʻri.
- **Yigʻma jadvalni yangilashni unutish.** Manba oʻzgarganda Refresh qiling.
- **Saralashda faqat bitta ustunni belgilash.** Satrlar "uzilib", maʼlumot buziladi — butun jadvalni belgilang.
:::

:::tip
Har bir ustunda faqat bitta turdagi maʼlumot, birinchi satrda sarlavha, boʻsh satr va birlashtirilgan kataksiz jadval — "toza" jadval. Yigʻma jadval va filtrlar faqat toza jadvalda ishonchli ishlaydi.
:::

## Xulosa

- Koʻrish: jadval formati, mahkamlash, guruhlash.
- Qayta ishlash: saralash, filtr, shartli formatlash, maʼlumotlarni tekshirish, dublikatlar, matnni ajratish.
- Umumlashtirish: oraliq natijalar, yigʻma jadval va diagramma, kesimlar.
- "Agar … boʻlsa": Goal Seek, ssenariylar, maʼlumotlar jadvali.
- Yigʻma jadval — guruhlash va umumlashtirish; uni dasturda ham avtomatlashtirish mumkin.

## Oʻzingizni tekshiring

:::check
Q: Katta jadvalni guruhlab, yigʻindi va oʻrtachalarni tez hisoblash vositasi qaysi?
A: Yigʻma jadval (PivotTable).
Q: Aylantirganda sarlavha satri koʻrinib turishi uchun nima qilinadi?
A: View → Freeze Panes bilan satr mahkamlanadi.
Q: Data Validation nima uchun kerak?
A: Katakka faqat ruxsat etilgan qiymatlar kiritilishini taʼminlab, xatolarning oldini oladi.
Q: Goal Seek qanday vazifani bajaradi?
A: Kerakli natijaga erishish uchun bitta kirish qiymatini topadi.
:::

:::link https://support.microsoft.com/office/create-a-pivottable-to-analyze-worksheet-data-a9a84538-bfe9-40a9-a8e9-f99134456576
Microsoft: Create a PivotTable
Yigʻma jadval yaratish boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
