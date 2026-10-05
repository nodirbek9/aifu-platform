---
title: Katta maʼlumotlarni (Big Data) qayta ishlash texnologiyalari
summary: Seminar — Big Data ning 5V xususiyati; tuzilmalangan, yarim tuzilmalangan va tuzilmalanmagan maʼlumotlar; maʼlumotlarni qayta ishlash bosqichlari — yigʻish, saqlash, tozalash, tahlil, vizualizatsiya; paketli va oqimli qayta ishlash; Hadoop (HDFS, MapReduce), Apache Spark, Kafka, NoSQL, maʼlumotlar ombori va koʻli, bulutli platformalar; tahlil turlari — tavsifiy, diagnostik, prognostik, preskriptiv; iqtisodiyotda qoʻllanish; real vaqtda shubhali tranzaksiyani aniqlash qoidasi (dastur); maxfiylik va etika.
---

## Seminarning maqsadi

M7 maʼruzasida maʼlumotlar bazalari va Big Data tushunchasini koʻrdik. Seminarda katta maʼlumotlar **qanday qayta ishlanishi** — qaysi bosqichlar va texnologiyalar ishlatilishini — chuqurroq koʻramiz va bank kartalarida real vaqtda shubhali toʻlovni aniqlashning oddiy qoidasini dasturda sinab koʻramiz.

:::note
**Bu seminardan soʻng siz:**

- Big Data xususiyatlari va maʼlumot turlarini tushuntira olasiz;
- maʼlumotlarni qayta ishlash bosqichlari va asosiy texnologiyalarni ayta olasiz;
- paketli va oqimli qayta ishlashni farqlay olasiz;
- tahlil turlarini va ularning iqtisodiyotdagi qoʻllanishini bilasiz;
- oqimli maʼlumotda oddiy anomaliya qoidasini qoʻllay olasiz.
:::

## Maʼlumot turlari

| Tur | Tavsif | Misol |
|---|---|---|
| **Tuzilmalangan** | qatʼiy jadval koʻrinishi | bank tranzaksiyalari, buxgalteriya yozuvlari |
| **Yarim tuzilmalangan** | belgilar bilan, lekin moslashuvchan | JSON, XML, veb-jurnallar |
| **Tuzilmalanmagan** | aniq tuzilmasiz | matnlar, sharhlar, rasmlar, videolar, ovozli yozuvlar |

Tadqiqotlar maʼlumotlarning katta qismi tuzilmalanmagan ekanini koʻrsatadi — ularni tahlil qilish uchun matn tahlili va sunʼiy intellekt kerak.

## Qayta ishlash bosqichlari va texnologiyalar

| Bosqich | Mazmuni | Vositalar |
|---|---|---|
| **Yigʻish** | kassalar, ilovalar, datchiklar, saytlardan | API, Apache Kafka (oqimlar), ETL vositalari |
| **Saqlash** | katta va xilma-xil maʼlumot | HDFS (Hadoop taqsimlangan fayl tizimi), maʼlumotlar koʻli, NoSQL, bulut omborlari |
| **Tozalash** | dublikatlar, xatolar, boʻsh qiymatlar, formatlarni moslashtirish | Python (pandas), Spark, Power Query |
| **Tahlil** | hisob-kitob, statistika, mashinali oʻrganish | Apache Spark, SQL, Python, R |
| **Vizualizatsiya** | dashbordlar va hisobotlar | Power BI, Tableau, Looker Studio |

**Hadoop** — maʼlumotni koʻplab oddiy kompyuterlarga taqsimlab saqlaydi (HDFS) va **MapReduce** usulida parallel qayta ishlaydi: *Map* — har bir kompyuter oʻz qismini qayta ishlaydi, *Reduce* — natijalar birlashtiriladi. **Apache Spark** — xotirada ishlagani uchun ancha tez, paketli va oqimli tahlilni, SQL va mashinali oʻrganishni qoʻllaydi.

**Paketli va oqimli qayta ishlash**: paketli (batch) — maʼlumot yigʻilib, masalan kechasi bir marta qayta ishlanadi (oylik hisobot); oqimli (stream) — har bir hodisa kelishi bilan darhol (karta toʻlovi, birja narxi). Firibgarlikni aniqlash uchun oqimli qayta ishlash zarur — toʻlov soniyaning bir qismida tasdiqlanishi yoki toʻxtatilishi kerak.

## Tahlil turlari

| Tur | Savol | Misol |
|---|---|---|
| **Tavsifiy** (descriptive) | nima boʻldi? | oylik savdo hisoboti |
| **Diagnostik** | nima uchun boʻldi? | savdo qaysi hududda va nima sababdan tushdi |
| **Prognostik** (predictive) | nima boʻladi? | keyingi oy talabi, mijozning kreditni qaytarish ehtimoli |
| **Preskriptiv** | nima qilish kerak? | optimal narx, zaxira hajmi, marshrut |

## Ishlangan misol: shubhali tranzaksiyani aniqlash

Bank har bir karta toʻlovini real vaqtda tekshiradi. Oddiy qoida: agar toʻlov summasi mijozning oldingi (qabul qilingan) toʻlovlari oʻrtachasidan *k* barobardan katta boʻlsa — **shubhali**, u tasdiqlash uchun toʻxtatiladi va tarixga qoʻshilmaydi; qoida kamida 3 ta oldingi toʻlov boʻlganda qoʻllanadi. Kirish: *k*, keyin toʻlovlar ketma-ketligi (ming soʻm).

```python
import sys

t = list(map(int, sys.stdin.read().split()))
k, tolovlar = t[0], t[1:]
jami = soni = shubhali = 0
for i, summa in enumerate(tolovlar, start=1):
    if soni >= 3 and summa * soni > k * jami:        # summa > k * oʻrtacha (butun sonlarda)
        shubhali += 1
        print(f"{i}: {summa} SHUBHALI (oʻrtacha {jami / soni:.1f})")
        continue                                      # shubhali toʻlov tarixga qoʻshilmaydi
    jami += summa
    soni += 1
print(f"tekshirildi: {len(tolovlar)}, shubhali: {shubhali}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long k = in.nextLong();
        List<Long> tolovlar = new ArrayList<>();
        while (in.hasNextLong()) tolovlar.add(in.nextLong());
        long jami = 0, soni = 0;
        int shubhali = 0;
        for (int i = 0; i < tolovlar.size(); i++) {
            long summa = tolovlar.get(i);
            if (soni >= 3 && summa * soni > k * jami) {       // summa > k * oʻrtacha (butun sonlarda)
                shubhali++;
                System.out.println(String.format("%d: %d SHUBHALI (oʻrtacha %.1f)", i + 1, summa, (double) jami / soni));
                continue;                                      // shubhali toʻlov tarixga qoʻshilmaydi
            }
            jami += summa;
            soni++;
        }
        System.out.println("tekshirildi: " + tolovlar.size() + ", shubhali: " + shubhali);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    long long k, x;
    cin >> k;
    vector<long long> tolovlar;
    while (cin >> x) tolovlar.push_back(x);
    long long jami = 0, soni = 0;
    int shubhali = 0;
    for (size_t i = 0; i < tolovlar.size(); i++) {
        long long summa = tolovlar[i];
        if (soni >= 3 && summa * soni > k * jami) {           // summa > k * oʻrtacha (butun sonlarda)
            shubhali++;
            printf("%zu: %lld SHUBHALI (oʻrtacha %.1f)\n", i + 1, summa, (double) jami / soni);
            continue;                                          // shubhali toʻlov tarixga qoʻshilmaydi
        }
        jami += summa;
        soni++;
    }
    printf("tekshirildi: %zu, shubhali: %d\n", tolovlar.size(), shubhali);
    return 0;
}
```

Kirish:

```text
3 120 135 128 140 118 980 125 132 130 415 127 138
```

Chiqish:

```text
6: 980 SHUBHALI (oʻrtacha 128.2)
10: 415 SHUBHALI (oʻrtacha 128.5)
tekshirildi: 12, shubhali: 2
```

Qoida oʻrtachani har safar qayta hisoblamasdan, faqat yigʻindi va sonni yangilaydi — bu oqimli qayta ishlashning asosiy gʻoyasi: maʼlumot bir marta oʻtadi, xotira kam ishlatiladi. Shuningdek, `summa > k × oʻrtacha` sharti butun sonlarda `summa × soni > k × jami` koʻrinishida tekshiriladi — kasr sonlar xatosiz. Haqiqiy banklarda yuzlab belgi (joy, vaqt, qurilma, doʻkon turi) va mashinali oʻrganish modellari ishlatiladi, lekin tamoyil bir xil: odatdagi xatti-harakatdan keskin chetga chiqishni aniqlash.

## Iqtisodiyotda qoʻllanish

- **Bank**: kredit skoringi, firibgarlikni aniqlash, mijozlarni segmentlash.
- **Savdo**: talabni prognozlash, tavsiya tizimlari ("bu bilan birga sotib olishadi"), dinamik narxlash.
- **Telekom**: mijoz ketishini bashorat qilish, tarmoq yuklamasini rejalashtirish.
- **Davlat**: soliq tekshiruvlarini xavf boʻyicha tanlash, bojxona tahlili, transport oqimlari.
- **Qishloq xoʻjaligi**: hosil prognozi, ob-havo va sunʼiy yoʻldosh maʼlumotlari.

**Maxfiylik va etika**: shaxsiy maʼlumotlar faqat qonuniy asosda va maqsadda ishlatiladi ("Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonun); maʼlumotlarni anonimlashtirish; algoritmlar kamsitish (masalan, kreditda) keltirib chiqarmasligi kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **"Koʻp maʼlumot — demak toʻgʻri xulosa".** Sifatsiz maʼlumot katta hajmda ham notoʻgʻri natija beradi.
- **Korrelyatsiyani sabab deb hisoblash.** Ikki koʻrsatkich birga oʻzgarsa, biri ikkinchisining sababi boʻlmasligi mumkin.
- **Shubhali tranzaksiyani tarixga qoʻshish.** Anomaliya oʻrtachani buzadi va keyingilarini yashiradi.
- **Shaxsiy maʼlumotlarni roziliksiz ishlatish.** Qonun buzilishi va obroʻni yoʻqotish.
:::

:::tip
Misoldagi *k* ni 2 va 5 ga oʻzgartirib sinang: qoida qanchalik qattiq boʻlsa, shubhali deb topilganlar shuncha koʻp (lekin soxta signallar ham koʻp) — bu M10 dagi FAR va FRR muvozanatiga oʻxshash tanlov.
:::

## Xulosa

- Big Data: 5V; tuzilmalangan, yarim tuzilmalangan va tuzilmalanmagan maʼlumotlar.
- Bosqichlar: yigʻish → saqlash → tozalash → tahlil → vizualizatsiya.
- Texnologiyalar: Hadoop (HDFS, MapReduce), Spark, Kafka, NoSQL, maʼlumotlar ombori va koʻli, BI.
- Paketli va oqimli qayta ishlash; firibgarlikni aniqlash uchun oqimli kerak.
- Tahlil turlari: tavsifiy, diagnostik, prognostik, preskriptiv; maxfiylik va etika talablari.

## Oʻzingizni tekshiring

:::check
Q: Katta hajmdagi maʼlumotlarni koʻp kompyuterlarda taqsimlab qayta ishlash uchun yaratilgan freymvork qaysi?
A: Apache Hadoop.
Q: Tuzilmalanmagan maʼlumotlarga nimalar kiradi?
A: Matnlar, rasmlar, videolar va ovozli yozuvlar.
Q: Paketli va oqimli qayta ishlashning farqi nima?
A: Paketli — yigʻilgan maʼlumot vaqti-vaqti bilan qayta ishlanadi; oqimli — har bir hodisa kelishi bilan darhol.
Q: Iqtisodiyotda Big Data qoʻllanishiga misol keltiring.
A: Xaridorlar xatti-harakatini tahlil qilib talabni prognozlash yoki karta toʻlovlarida firibgarlikni aniqlash.
:::

:::link https://spark.apache.org/docs/latest/
Apache Spark Documentation
Katta maʼlumotlarni qayta ishlash platformasining rasmiy hujjatlari (ingliz tilida).
:::
