---
title: Katta maʼlumotlarni (Big Data) qayta ishlash texnologiyalari
summary: Mustaqil ish — vertikal va gorizontal masshtablash; MapReduce paradigmasi bosqichma-bosqich (map, combine, shuffle, reduce) va uni simulyatsiya qiluvchi dastur; Apache Spark — xotirada hisoblash va "dangasa" bajarish; oqimli qayta ishlash va vaqt oynalari; ETL va ELT; maʼlumotlar ombori, koʻli va "lakehouse", bronza-kumush-oltin qatlamlari; maʼlumot sifati oʻlchovlari; bulutdagi tahlil xarajatlari; topshiriq.
---

## Mustaqil ishning maqsadi

S7 seminarida Big Data xususiyatlari va texnologiyalar roʻyxati (Hadoop, Spark, Kafka) bilan tanishdik. Bu mustaqil ishda ular **qanday ishlashini** tushunamiz: nima uchun minglab oddiy kompyuterlar bitta superkompyuterdan arzonroq, MapReduce vazifani qanday boʻladi va maʼlumot qanday yoʻl bosib hisobotga aylanadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- vertikal va gorizontal masshtablashni farqlay olasiz;
- MapReduce bosqichlarini misolda tushuntira olasiz;
- Spark va oqimli qayta ishlashning afzalliklarini ayta olasiz;
- ETL/ELT jarayoni va maʼlumotlar koʻli arxitekturasini tasvirlay olasiz;
- maʼlumot sifatini oʻlchovlar boʻyicha baholay olasiz.
:::

## Masshtablash

| Usul | Mazmuni | Cheklovi |
|---|---|---|
| **Vertikal** (scale up) | bitta serverni kuchaytirish: koʻproq protsessor, xotira | qimmat, jismoniy chegara bor, bitta nosozlik — hammasi toʻxtaydi |
| **Gorizontal** (scale out) | koʻplab oddiy serverlarni klasterga birlashtirish | vazifani boʻlish va natijalarni yigʻish kerak |

Big Data texnologiyalari gorizontal masshtablashga asoslanadi: maʼlumot va hisoblash klaster tugunlari boʻylab taqsimlanadi, bir tugun ishdan chiqsa, uning ishini boshqasi bajaradi (maʼlumot nusxalari bor).

## MapReduce paradigmasi

Google (2004) taklif qilgan, Hadoop da amalga oshirilgan model. Vazifa ikki oddiy funksiyaga boʻlinadi:

1. **Map** — har bir tugun oʻz maʼlumot qismini oʻqiydi va (kalit, qiymat) juftlarini chiqaradi: (hudud, summa).
2. **Combine** (ixtiyoriy) — tugun ichida bir xil kalitlarni oldindan yigʻadi — tarmoq orqali kamroq maʼlumot uzatiladi.
3. **Shuffle** — bir xil kalitli juftlar bitta "reducer"ga yuboriladi.
4. **Reduce** — har bir kalit boʻyicha yakuniy natija: yigʻindi, soni, maksimum.

Asosiy gʻoya: **hisoblashni maʼlumot yoniga olib borish**. Terabaytlarni bitta serverga koʻchirish oʻrniga, har bir tugun oʻz diskidagi qismni oʻzi qayta ishlaydi.

## Ishlangan misol: MapReduce simulyatsiyasi

Savdo yozuvlari (hudud, summa mln soʻm) 3 ta tugunga teng qismlarda taqsimlanadi. Har bir tugun map va combine bajaradi, keyin reduce yakuniy yigʻindini beradi. Bu SQL dagi `GROUP BY hudud` (M7) ning taqsimlangan koʻrinishi.

```python
import sys

TUGUNLAR = 3

satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
qism = (len(satrlar) + TUGUNLAR - 1) // TUGUNLAR

# 1-bosqich: Map + Combine — har bir tugun oʻz qismini qayta ishlaydi
oraliq = []
for t in range(TUGUNLAR):
    mahalliy = {}
    for satr in satrlar[t * qism:(t + 1) * qism]:
        hudud, summa = satr.split()
        mahalliy[hudud] = mahalliy.get(hudud, 0) + int(summa)
    oraliq.append(mahalliy)
    juftlar = ", ".join(f"{k}={mahalliy[k]}" for k in sorted(mahalliy))
    print(f"Tugun {t + 1}: {juftlar}")

# 2-bosqich: Shuffle — bir xil kalitlar bitta reducerga yigʻiladi
# 3-bosqich: Reduce — yakuniy yigʻindi
jami = {}
for mahalliy in oraliq:
    for k, v in mahalliy.items():
        jami[k] = jami.get(k, 0) + v
for k in sorted(jami):
    print(f"Jami {k}: {jami[k]}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static final int TUGUNLAR = 3;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) satrlar.add(s.trim());
        }
        int qism = (satrlar.size() + TUGUNLAR - 1) / TUGUNLAR;

        // 1-bosqich: Map + Combine — har bir tugun oʻz qismini qayta ishlaydi
        List<TreeMap<String, Long>> oraliq = new ArrayList<>();
        for (int t = 0; t < TUGUNLAR; t++) {
            TreeMap<String, Long> mahalliy = new TreeMap<>();
            for (int i = t * qism; i < Math.min((t + 1) * qism, satrlar.size()); i++) {
                String[] q = satrlar.get(i).split("\\s+");
                mahalliy.merge(q[0], Long.parseLong(q[1]), Long::sum);
            }
            oraliq.add(mahalliy);
            List<String> juftlar = new ArrayList<>();
            for (Map.Entry<String, Long> e : mahalliy.entrySet()) juftlar.add(e.getKey() + "=" + e.getValue());
            System.out.println("Tugun " + (t + 1) + ": " + String.join(", ", juftlar));
        }

        // 2-bosqich: Shuffle — bir xil kalitlar bitta reducerga yigʻiladi
        // 3-bosqich: Reduce — yakuniy yigʻindi
        TreeMap<String, Long> jami = new TreeMap<>();
        for (TreeMap<String, Long> mahalliy : oraliq)
            for (Map.Entry<String, Long> e : mahalliy.entrySet()) jami.merge(e.getKey(), e.getValue(), Long::sum);
        for (Map.Entry<String, Long> e : jami.entrySet())
            System.out.println("Jami " + e.getKey() + ": " + e.getValue());
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const int TUGUNLAR = 3;

int main() {
    vector<string> satrlar;
    string s;
    while (getline(cin, s))
        if (s.find_first_not_of(" \t\r") != string::npos) satrlar.push_back(s);
    int n = satrlar.size();
    int qism = (n + TUGUNLAR - 1) / TUGUNLAR;

    // 1-bosqich: Map + Combine — har bir tugun oʻz qismini qayta ishlaydi
    vector<map<string, long long>> oraliq;
    for (int t = 0; t < TUGUNLAR; t++) {
        map<string, long long> mahalliy;
        for (int i = t * qism; i < min((t + 1) * qism, n); i++) {
            istringstream ss(satrlar[i]);
            string hudud;
            long long summa;
            ss >> hudud >> summa;
            mahalliy[hudud] += summa;
        }
        oraliq.push_back(mahalliy);
        cout << "Tugun " << t + 1 << ": ";
        bool birinchi = true;
        for (auto& [k, v] : mahalliy) {
            cout << (birinchi ? "" : ", ") << k << "=" << v;
            birinchi = false;
        }
        cout << "\n";
    }

    // 2-bosqich: Shuffle — bir xil kalitlar bitta reducerga yigʻiladi
    // 3-bosqich: Reduce — yakuniy yigʻindi
    map<string, long long> jami;
    for (auto& mahalliy : oraliq)
        for (auto& [k, v] : mahalliy) jami[k] += v;
    for (auto& [k, v] : jami) cout << "Jami " << k << ": " << v << "\n";
    return 0;
}
```

Kirish:

```text
Toshkent 120
Samarqand 80
Toshkent 50
Buxoro 40
Samarqand 70
Toshkent 30
Buxoro 60
Andijon 90
Toshkent 100
```

Chiqish:

```text
Tugun 1: Samarqand=80, Toshkent=170
Tugun 2: Buxoro=40, Samarqand=70, Toshkent=30
Tugun 3: Andijon=90, Buxoro=60, Toshkent=100
Jami Andijon: 90
Jami Buxoro: 100
Jami Samarqand: 150
Jami Toshkent: 300
```

Combine tufayli 1-tugun reducerga 3 ta yozuv oʻrniga 2 ta juft yuboradi. Milliardlab yozuvda bu tarmoq trafigini bir necha barobar kamaytiradi. Haqiqiy klasterda tugunlar parallel ishlaydi: 1 000 tugun maʼlumotni taxminan 1 000 barobar tezroq qayta ishlaydi (bir qismi tarmoq va muvofiqlashtirishga ketadi).

## Apache Spark

Hadoop MapReduce har bir bosqich natijasini diskka yozadi — bu sekin. **Spark** oraliq natijalarni **operativ xotirada** saqlaydi va koʻp bosqichli tahlilda (masalan, mashinali oʻqitishda bir xil maʼlumotdan koʻp marta oʻtishda) oʻnlab barobar tezroq ishlaydi. **"Dangasa" bajarish**: Spark buyruqlarni darhol bajarmaydi, avval butun rejani tuzib optimallashtiradi, natija soʻralgandagina hisoblaydi. Spark SQL, Python (PySpark), mashinali oʻqitish (MLlib) va oqim bilan ishlashni qoʻllab-quvvatlaydi.

## Oqimli qayta ishlash

Toʻlovlar, kassalar, sensorlar maʼlumoti uzluksiz keladi. Oqimli tizimlar (Kafka + Spark Streaming yoki Flink) uni kelishi bilan qayta ishlaydi. Asosiy tushuncha — **vaqt oynasi**:

| Oyna turi | Misol |
|---|---|
| **Qatʼiy** (tumbling) | har 5 daqiqada savdo summasi: 10:00–10:05, 10:05–10:10 |
| **Sirpanuvchi** (sliding) | soʻnggi 1 soatdagi summa, har daqiqada yangilanadi |
| **Seans** (session) | foydalanuvchining saytdagi bitta tashrifi (faollik tanaffusigacha) |

S7 dagi shubhali tranzaksiya qoidasi aynan shunday oqimda ishlaydi.

## ETL va ELT

| Jarayon | Tartib | Qachon |
|---|---|---|
| **ETL** (Extract — Transform — Load) | manbadan olish → tozalash va oʻzgartirish → omborga yuklash | anʼanaviy maʼlumotlar omborlari, qatʼiy tuzilma |
| **ELT** (Extract — Load — Transform) | xom holda yuklash → kerak boʻlganda ombor ichida oʻzgartirish | bulutli omborlar va maʼlumotlar koʻllari, katta hajm |

## Ombor, koʻl va lakehouse

- **Maʼlumotlar ombori** (data warehouse) — tozalangan, tuzilmali maʼlumotlar, hisobotlar uchun (SQL).
- **Maʼlumotlar koʻli** (data lake) — har qanday formatdagi xom maʼlumotlar arzon saqlanadi (fayllar, loglar, rasmlar).
- **Lakehouse** — koʻlning arzonligi va omborning tartibi birgalikda.

**Bronza — kumush — oltin** qatlamlari:

| Qatlam | Mazmuni | Misol |
|---|---|---|
| **Bronza** | xom, oʻzgartirilmagan maʼlumot | kassalardan kelgan cheklar JSON holida |
| **Kumush** | tozalangan, birlashtirilgan, takrorlar olib tashlangan | yagona formatdagi savdo jadvali |
| **Oltin** | biznes uchun tayyor agregatlar | hudud va kun boʻyicha tushum, dashbordlar uchun |

## Maʼlumot sifati

| Oʻlchov | Savol | Buzilish misoli |
|---|---|---|
| **Toʻliqlik** | barcha kerakli qiymatlar bormi? | 15% mijozda telefon yoʻq |
| **Aniqlik** | qiymat haqiqatga mosmi? | narx 85 000 oʻrniga 850 000 |
| **Izchillik** | turli tizimlarda bir xilmi? | CRM va buxgalteriyada mijoz manzili turlicha |
| **Oʻz vaqtidalik** | maʼlumot yangimi? | hisobot kechagi emas, oʻtgan haftadagi maʼlumotda |
| **Yagonalik** | takrorlar yoʻqmi? | bitta mijoz uch marta turli yozilishda |
| **Yaroqlilik** | format va qoidaga mosmi? | sana "31.02.2025" |

## Bulutdagi tahlil xarajatlari

Bulutli Big Data xizmatlari (BigQuery, Snowflake, Databricks va boshq.) koʻpincha **skanerlangan maʼlumot hajmi** yoki **hisoblash vaqti** boʻyicha haq oladi. Shuning uchun: faqat kerakli ustunlarni soʻrang (`SELECT *` emas), jadvallarni sana boʻyicha boʻling (partitsiyalash), oltin qatlamdagi tayyor agregatlardan foydalaning, ishlatilmagan klasterlarni oʻchiring (FinOps, S11).

:::warning
**Koʻp uchraydigan xatolar**

- **Kichik maʼlumot uchun Big Data texnologiyasi.** Bir necha GB uchun Excel, Python yoki oddiy MBBT yetarli va arzon.
- **Xom maʼlumotni tozalamasdan hisobotga berish.** Sifat oʻlchovlarini tekshiring.
- **Koʻlni "botqoq"qa aylantirish.** Metamaʼlumotlarsiz (nima, qayerdan, qachon) maʼlumotni hech kim topa olmaydi.
- **Xarajatni nazorat qilmaslik.** Bitta notoʻgʻri soʻrov terabaytlarni skanerlab, katta hisob keltirishi mumkin.
:::

## Topshiriq

1. Misol dasturida tugunlar sonini 2 va 4 ga oʻzgartiring. Yakuniy natija oʻzgaradimi? Oraliq natijalar-chi? Nima uchun?
2. Dasturni oʻzgartirib, har bir hudud boʻyicha summa bilan birga **savdolar sonini** ham hisoblang.
3. Bitta internet-doʻkon uchun bronza, kumush va oltin qatlamlarida qanday maʼlumotlar boʻlishini jadvalda yozing.
4. Oʻzingiz bilgan bitta jadval (masalan, guruh roʻyxati) sifatini olti oʻlchov boʻyicha baholang.

## Xulosa

- Big Data gorizontal masshtablashga asoslanadi: hisoblash maʼlumot yoniga olib boriladi.
- MapReduce: map → combine → shuffle → reduce; Spark xotirada ishlaydi va tezroq.
- Oqimli qayta ishlash vaqt oynalari bilan ishlaydi; ETL/ELT maʼlumotni omborga yetkazadi.
- Arxitektura: ombor, koʻl, lakehouse; bronza → kumush → oltin.
- Maʼlumot sifati olti oʻlchov bilan baholanadi; bulut xarajatlari nazorat qilinadi.

## Oʻzingizni tekshiring

:::check
Q: Combine bosqichi nima uchun kerak?
A: Tugun ichida bir xil kalitlarni oldindan yigʻib, tarmoq orqali uzatiladigan maʼlumot hajmini kamaytirish uchun.
Q: Spark Hadoop MapReduce dan nima uchun tezroq?
A: Oraliq natijalarni diskka emas, operativ xotiraga saqlaydi.
Q: ETL va ELT ning farqi nima?
A: ETL da maʼlumot yuklashdan oldin oʻzgartiriladi, ELT da xom holda yuklanib, keyin ombor ichida oʻzgartiriladi.
Q: Oltin qatlamda qanday maʼlumot saqlanadi?
A: Biznes uchun tayyor agregatlar — hisobot va dashbordlar uchun.
:::

:::link https://spark.apache.org/docs/latest/quick-start.html
Apache Spark: Quick Start
Spark bilan ishlashni boshlash boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
