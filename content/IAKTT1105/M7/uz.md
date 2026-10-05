---
title: Maʼlumotlar bazalari va katta hajmdagi maʼlumotlar bilan ishlash texnologiyalari
summary: Maʼlumotlar bazasi va MBBT tushunchasi; maʼlumotlar modellari — ierarxik, tarmoq, relyatsion, NoSQL; relyatsion model — jadval, yozuv, maydon, birlamchi va tashqi kalit, bogʻlanishlar (1:1, 1:N, N:M); normallashtirish gʻoyasi; SQL asoslari — SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE; mashhur MBBT — MySQL, PostgreSQL, Oracle, SQL Server, Access, MongoDB; tranzaksiyalar va yaxlitlik; katta maʼlumotlar (Big Data) — 5V, saqlash va qayta ishlash (maʼlumotlar ombori, maʼlumotlar koʻli, Hadoop, Spark), iqtisodiyotdagi qoʻllanishi; JOIN va GROUP BY ni dasturda koʻrsatish.
---

## Nima uchun bu mavzu muhim?

Bank mijozlari va hisoblari, doʻkonning tovarlari va sotuvlari, soliq toʻlovchilar, talabalar va baholar — barchasi **maʼlumotlar bazalarida** saqlanadi. Excel jadvali bir necha ming satrgacha qulay, lekin minglab foydalanuvchi bir vaqtda ishlaydigan, millionlab yozuvli tizim uchun maʼlumotlar bazasini boshqarish tizimi (MBBT) kerak. Iqtisodchi SQL da oddiy soʻrov yoza olsa, IT boʻlimini kutmasdan kerakli hisobotni oʻzi oladi.

:::note
**Bu darsdan soʻng siz:**

- maʼlumotlar bazasi, MBBT va maʼlumotlar modellarini tushuntira olasiz;
- relyatsion modelning asosiy tushunchalarini (jadval, kalit, bogʻlanish) bilasiz;
- oddiy SQL soʻrovlarini oʻqiy va yoza olasiz;
- tranzaksiya va yaxlitlik tushunchalarini bilasiz;
- katta maʼlumotlar texnologiyalari va ularning iqtisodiyotdagi qoʻllanishini ayta olasiz.
:::

## Maʼlumotlar bazasi va MBBT

**Maʼlumotlar bazasi** — maʼlum tuzilma boʻyicha tashkil etilgan, oʻzaro bogʻlangan maʼlumotlar majmui. **MBBT** (maʼlumotlar bazasini boshqarish tizimi) — maʼlumotlarni saqlash, izlash, oʻzgartirish, himoya qilish va bir vaqtda koʻp foydalanuvchiga xizmat koʻrsatish uchun dastur.

| Model | Tuzilma | Misol |
|---|---|---|
| Ierarxik | daraxt | eski bank tizimlari, fayl tizimi |
| Tarmoq | graf | tarixiy tizimlar |
| **Relyatsion** | jadvallar va ular orasidagi bogʻlanishlar | buxgalteriya, bank, ERP — eng keng tarqalgan |
| **NoSQL** | hujjatlar, kalit-qiymat, graf, ustunli | katta va oʻzgaruvchan maʼlumotlar, ijtimoiy tarmoqlar |

**Mashhur MBBT**: PostgreSQL va MySQL (ochiq manba), Oracle va Microsoft SQL Server (tijoriy, yirik korxonalar), Microsoft Access (kichik ofis), SQLite (ilovalar ichida), MongoDB (hujjatli NoSQL).

## Relyatsion model

- **Jadval** (relyatsiya) — bitta turdagi obyektlar: `mijozlar`, `buyurtmalar`.
- **Yozuv** (satr) — bitta obyekt; **maydon** (ustun) — xususiyat (ism, shahar).
- **Birlamchi kalit** (primary key) — har bir yozuvni noyob aniqlaydi (`id`, JSHSHIR, STIR).
- **Tashqi kalit** (foreign key) — boshqa jadvaldagi yozuvga havola (`buyurtmalar.mijoz_id` → `mijozlar.id`).
- **Bogʻlanishlar**: 1:1 (xodim — pasport), **1:N** (mijoz — koʻp buyurtmalar), N:M (talabalar — fanlar, oraliq jadval orqali).
- **Normallashtirish** — maʼlumotni takrorlamaslik: mijoz manzili har bir buyurtmada emas, bir marta `mijozlar` jadvalida saqlanadi. Takror maʼlumot nomuvofiqlikka olib keladi.

## SQL asoslari

**SQL** (Structured Query Language) — relyatsion bazalar bilan ishlash tili.

| Buyruq | Vazifasi | Misol |
|---|---|---|
| `SELECT … FROM` | maʼlumot tanlash | `SELECT ism, shahar FROM mijozlar;` |
| `WHERE` | shart | `… WHERE shahar = 'Toshkent'` |
| `ORDER BY` | saralash | `… ORDER BY summa DESC` |
| `GROUP BY` + `SUM`, `COUNT`, `AVG` | guruhlash va hisoblash | `SELECT mijoz_id, SUM(summa) FROM buyurtmalar GROUP BY mijoz_id;` |
| `JOIN` | jadvallarni bogʻlash | `… FROM buyurtmalar b JOIN mijozlar m ON b.mijoz_id = m.id` |
| `INSERT`, `UPDATE`, `DELETE` | qoʻshish, oʻzgartirish, oʻchirish | `UPDATE tovarlar SET narx = 26000 WHERE id = 5;` |

**Tranzaksiya** — bir butun bajariladigan amallar guruhi: bank oʻtkazmasida bir hisobdan yechish va boshqasiga qoʻshish yoki ikkalasi bajariladi, yoki hech biri (**ACID** xossalari: atomarlik, izchillik, izolyatsiya, chidamlilik). Bu pul "yoʻqolib qolmasligini" kafolatlaydi.

## Ishlangan misol: JOIN va GROUP BY

Mijozlar va buyurtmalar jadvallari berilgan. Har bir mijoz uchun buyurtmalar soni va jami summasini, summa kamayishi boʻyicha (teng boʻlsa — id boʻyicha) chiqaring; buyurtmasi yoʻq mijozlarni oxirida koʻrsating. SQL da bu shunday yoziladi:

```text
SELECT m.ism, m.shahar, COUNT(b.id) AS soni, SUM(b.summa) AS jami
FROM mijozlar m LEFT JOIN buyurtmalar b ON b.mijoz_id = m.id
GROUP BY m.id, m.ism, m.shahar
ORDER BY jami DESC;
```

Quyidagi dastur xuddi shu ishni bajaradi. Kirish: *m* va *m* ta mijoz (id ism shahar), *k* va *k* ta buyurtma (id mijoz_id summa).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
m = int(satrlar[0][0])
mijozlar = {}                                     # id -> (ism, shahar)
for i in range(1, 1 + m):
    mijozlar[satrlar[i][0]] = (satrlar[i][1], satrlar[i][2])
k = int(satrlar[1 + m][0])
jami, soni = {}, {}
for _, mijoz_id, summa in satrlar[2 + m:2 + m + k]:
    jami[mijoz_id] = jami.get(mijoz_id, 0) + int(summa)     # GROUP BY + SUM
    soni[mijoz_id] = soni.get(mijoz_id, 0) + 1              # COUNT
for mid in sorted(jami, key=lambda x: (-jami[x], x)):        # ORDER BY jami DESC
    ism, shahar = mijozlar[mid]                               # JOIN
    print(f"{ism} ({shahar}): {soni[mid]} ta buyurtma, {jami[mid]}")
for mid in sorted(mijozlar):                                  # LEFT JOIN: buyurtmasiz mijozlar
    if mid not in jami:
        print(f"{mijozlar[mid][0]} ({mijozlar[mid][1]}): buyurtmasiz")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int m = in.nextInt();
        Map<String, String[]> mijozlar = new TreeMap<>();          // id -> (ism, shahar)
        for (int i = 0; i < m; i++) {
            String id = in.next();
            mijozlar.put(id, new String[] {in.next(), in.next()});
        }
        int k = in.nextInt();
        Map<String, Long> jami = new HashMap<>();
        Map<String, Integer> soni = new HashMap<>();
        for (int i = 0; i < k; i++) {
            in.next();
            String mijozId = in.next();
            long summa = in.nextLong();
            jami.merge(mijozId, summa, Long::sum);                  // GROUP BY + SUM
            soni.merge(mijozId, 1, Integer::sum);                   // COUNT
        }
        List<String> tartib = new ArrayList<>(jami.keySet());
        tartib.sort((a, b) -> !jami.get(a).equals(jami.get(b)) ? Long.compare(jami.get(b), jami.get(a)) : a.compareTo(b));   // ORDER BY jami DESC
        for (String mid : tartib) {
            String[] mj = mijozlar.get(mid);                         // JOIN
            System.out.println(mj[0] + " (" + mj[1] + "): " + soni.get(mid) + " ta buyurtma, " + jami.get(mid));
        }
        for (String mid : mijozlar.keySet()) {                       // LEFT JOIN: buyurtmasiz mijozlar
            if (!jami.containsKey(mid)) {
                System.out.println(mijozlar.get(mid)[0] + " (" + mijozlar.get(mid)[1] + "): buyurtmasiz");
            }
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int m, k;
    cin >> m;
    map<string, pair<string, string>> mijozlar;                    // id -> (ism, shahar)
    for (int i = 0; i < m; i++) {
        string id, ism, shahar;
        cin >> id >> ism >> shahar;
        mijozlar[id] = {ism, shahar};
    }
    cin >> k;
    map<string, long long> jami;
    map<string, int> soni;
    for (int i = 0; i < k; i++) {
        string bid, mijozId;
        long long summa;
        cin >> bid >> mijozId >> summa;
        jami[mijozId] += summa;                                    // GROUP BY + SUM
        soni[mijozId] += 1;                                        // COUNT
    }
    vector<string> tartib;
    for (auto& p : jami) tartib.push_back(p.first);
    sort(tartib.begin(), tartib.end(), [&](const string& a, const string& b) {
        return jami[a] != jami[b] ? jami[a] > jami[b] : a < b;   // ORDER BY jami DESC
    });
    for (const string& mid : tartib) {
        auto& mj = mijozlar[mid];                                  // JOIN
        cout << mj.first << " (" << mj.second << "): " << soni[mid] << " ta buyurtma, " << jami[mid] << endl;
    }
    for (auto& p : mijozlar) {                                     // LEFT JOIN: buyurtmasiz mijozlar
        if (!jami.count(p.first)) cout << p.second.first << " (" << p.second.second << "): buyurtmasiz" << endl;
    }
    return 0;
}
```

Kirish:

```text
4
1 Aziza Toshkent
2 Bekzod Samarqand
3 Dilnoza Toshkent
4 Javlon Buxoro
6
101 1 450000
102 2 1200000
103 1 300000
104 3 800000
105 2 250000
106 1 150000
```

Chiqish:

```text
Bekzod (Samarqand): 2 ta buyurtma, 1450000
Aziza (Toshkent): 3 ta buyurtma, 900000
Dilnoza (Toshkent): 1 ta buyurtma, 800000
Javlon (Buxoro): buyurtmasiz
```

Mijoz ismi va shahri faqat `mijozlar` jadvalida bir marta saqlangan, buyurtmada esa faqat `mijoz_id` bor — bu normallashtirish. Hisobotda ular **JOIN** bilan birlashtiriladi. Eng koʻp buyurtma bergan Aziza emas, eng koʻp **summa**ni Bekzod bergan — qaysi koʻrsatkich boʻyicha saralash biznes savoliga bogʻliq. Javlon hech narsa sotib olmagan — u marketing uchun "faol boʻlmagan mijoz".

## Katta maʼlumotlar (Big Data)

**Katta maʼlumotlar** — anʼanaviy vositalar bilan saqlab va qayta ishlab boʻlmaydigan hajm va tezlikdagi maʼlumotlar. Ular **5V** bilan tavsiflanadi:

| V | Maʼnosi | Misol |
|---|---|---|
| **Volume** (hajm) | terabayt va petabaytlar | bankning barcha tranzaksiyalari |
| **Velocity** (tezlik) | real vaqtda oqim | kartadagi toʻlovlar, birja kotirovkalari |
| **Variety** (xilma-xillik) | jadval, matn, rasm, video, jurnallar | mijoz sharhlari, call-markaz yozuvlari |
| **Veracity** (ishonchlilik) | maʼlumot sifati va toʻgʻriligi | xato yoki dublikat yozuvlar |
| **Value** (qiymat) | tahlildan olinadigan foyda | firibgarlikni aniqlash, talabni prognozlash |

**Texnologiyalar**: **maʼlumotlar ombori** (data warehouse) — tahlil uchun tozalangan, tuzilgan maʼlumotlar; **maʼlumotlar koʻli** (data lake) — xom maʼlumotlarning har xil turlari; taqsimlangan qayta ishlash — **Hadoop**, **Apache Spark**; bulutdagi tahlil xizmatlari; biznes-tahlil (Power BI, Tableau).

**Iqtisodiyotda**: banklarda kredit skoringi va firibgarlikni real vaqtda aniqlash; savdoda talabni prognozlash va narxlarni optimallashtirish; telekomda mijozlar ketishini (churn) bashorat qilish; davlat statistikasida soliq va bojxona maʼlumotlarini tahlil qilish. Batafsil — S7 seminarida.

:::warning
**Koʻp uchraydigan xatolar**

- **Bir xil maʼlumotni koʻp joyda saqlash.** Takror maʼlumot nomuvofiqlikka olib keladi — normallashtiring.
- **Birlamchi kalitsiz jadval.** Yozuvlarni noyob aniqlab va bogʻlab boʻlmaydi.
- **WHERE siz UPDATE yoki DELETE.** Butun jadval oʻzgaradi yoki oʻchadi — avval SELECT bilan tekshiring.
- **Maʼlumot sifatini tekshirmaslik.** "Axlat kirsa — axlat chiqadi": notoʻgʻri maʼlumotdan toʻgʻri xulosa chiqmaydi.
- **Shaxsiy maʼlumotlarni himoyasiz saqlash.** Qonun talablariga rioya qiling (M14).
:::

:::tip
Brauzerda ishlaydigan bepul SQL muhitida (masalan, sqliteonline.com) `mijozlar` va `buyurtmalar` jadvallarini yarating, misoldagi maʼlumotlarni kiriting va yuqoridagi SQL soʻrovni bajaring — natija dasturnikiga mos kelishini tekshiring.
:::

## Xulosa

- Maʼlumotlar bazasi — tuzilgan maʼlumotlar majmui, MBBT — uni boshqarish dasturi; eng keng tarqalgan model — relyatsion.
- Relyatsion model: jadval, yozuv, maydon, birlamchi va tashqi kalit, 1:1, 1:N, N:M bogʻlanishlar, normallashtirish.
- SQL: SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE, DELETE; tranzaksiyalar va ACID.
- Katta maʼlumotlar: 5V; maʼlumotlar ombori va koʻli, Hadoop, Spark, BI.
- Iqtisodiyotda: skoring, firibgarlikni aniqlash, talabni prognozlash, mijozlar ketishini bashorat qilish.

## Oʻzingizni tekshiring

:::check
Q: Birlamchi kalit nima?
A: Jadvaldagi har bir yozuvni noyob aniqlovchi maydon (masalan, id).
Q: "Mijoz — buyurtmalar" bogʻlanishi qaysi turga kiradi?
A: Birga koʻp (1:N): bitta mijozda koʻp buyurtma boʻlishi mumkin.
Q: SQL da jadvallarni bogʻlash uchun qaysi buyruq ishlatiladi?
A: JOIN.
Q: Katta maʼlumotlarning 5V tavsifini ayting.
A: Volume (hajm), Velocity (tezlik), Variety (xilma-xillik), Veracity (ishonchlilik), Value (qiymat).
:::

:::link https://www.postgresql.org/docs/current/tutorial.html
PostgreSQL Tutorial
SQL va relyatsion maʼlumotlar bazalari boʻyicha rasmiy boshlangʻich qoʻllanma (ingliz tilida).
:::
