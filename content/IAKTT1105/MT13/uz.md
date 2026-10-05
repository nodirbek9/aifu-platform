---
title: Tarmoqlanuvchi hisoblash jarayonini dasturlash
summary: Mustaqil ish — taqqoslash va mantiqiy amallar (VA, YOKI, EMAS) va rostlik jadvali; if, if-else, if-elif-else zanjiri, ichma-ich shartlar, match/switch; shartlar tartibi; kirish maʼlumotlarini tekshirish; chegaraviy qiymatlar bilan test qilish; MT11 dagi qaror jadvalini dasturga oʻtkazish — kredit arizasi boʻyicha qaror; Python, Java, C++ da sintaksis farqlari; koʻp uchraydigan xatolar.
---

## Mustaqil ishning maqsadi

S8 da tarmoqlanuvchi jarayonning blok-sxemasini chizdik, MT11 da qaror jadvalini tuzdik. Endi tarmoqlanishni **dasturda** yozishni chuqurroq oʻrganamiz: murakkab shartlar, ularning tartibi va dastur toʻgʻri ishlashini qanday tekshirish.

:::note
**Bu mustaqil ishdan soʻng siz:**

- taqqoslash va mantiqiy amallardan foydalana olasiz;
- if-else zanjiri va ichma-ich shartlarni yoza olasiz;
- qaror jadvalini dasturga oʻtkaza olasiz;
- chegaraviy qiymatlar bilan test tuza olasiz.
:::

## Taqqoslash va mantiqiy amallar

| Amal | Python | Java, C++ | Maʼnosi |
|---|---|---|---|
| teng | `==` | `==` | `a == b` |
| teng emas | `!=` | `!=` | |
| katta, kichik | `>`, `<` | `>`, `<` | |
| katta yoki teng, kichik yoki teng | `>=`, `<=` | `>=`, `<=` | |
| VA | `and` | `&&` | ikkala shart rost |
| YOKI | `or` | `\|\|` | kamida bittasi rost |
| EMAS | `not` | `!` | shartni teskarisiga |

**Rostlik jadvali**:

| A | B | A VA B | A YOKI B | EMAS A |
|---|---|---|---|---|
| rost | rost | rost | rost | yolgʻon |
| rost | yolgʻon | yolgʻon | rost | yolgʻon |
| yolgʻon | rost | yolgʻon | rost | rost |
| yolgʻon | yolgʻon | yolgʻon | yolgʻon | rost |

**Qisqa tutashuv**: `A VA B` da A yolgʻon boʻlsa, B tekshirilmaydi. Bundan foydalanib xavfli amallarni himoya qilish mumkin: `daromad > 0 and tolov / daromad < 0.5` — nolga boʻlish yuz bermaydi.

## Tarmoqlanish shakllari

| Shakl | Qachon | Python |
|---|---|---|
| **Toʻliq boʻlmagan** (if) | shart rost boʻlsa, qoʻshimcha amal | `if qoldiq < 0: ogohlantir()` |
| **Toʻliq** (if-else) | ikki yoʻldan biri | `if ... : A else: B` |
| **Zanjir** (if-elif-else) | koʻp oʻzaro istisno holatlar | `if ...: elif ...: else:` |
| **Ichma-ich** | shart ichida yana shart | `if a: if b: ...` |
| **Tanlash** (match / switch) | bitta qiymatning bir nechta aniq variantlari | hisob turi: "joriy", "omonat", "kredit" |

**Zanjirda tartib muhim**: shartlar yuqoridan pastga tekshiriladi va birinchi rost shartdan keyingilari tekshirilmaydi. Shuning uchun eng tor (qatʼiy) shartlar birinchi yoziladi.

## Ishlangan misol: kredit arizasi boʻyicha qaror

MT11 dagi qaror jadvali: skoring balli ≥ 600 va oylik toʻlov daromadning ≤ 50% boʻlsa — tasdiqlash; ball yetarli, lekin yuk katta — kamroq summa taklif qilish; ball past, lekin yuk mos — kafil bilan koʻrib chiqish; aks holda — rad etish. Daromad nol yoki manfiy boʻlsa — kirish xatosi. Har bir satrda: familiya, ball, oylik daromad, oylik toʻlov.

```python
import sys

MIN_BALL = 600        # skoring chegarasi
MAX_ULUSH = 50        # toʻlov daromadning necha foizidan oshmasligi kerak

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    ism, ball, daromad, tolov = satr.split()
    ball, daromad, tolov = int(ball), int(daromad), int(tolov)
    if daromad <= 0:                                   # avval kirish maʼlumotini tekshirish
        qaror = "xato — daromad notoʻgʻri"
    else:
        yuk_mos = tolov * 100 <= daromad * MAX_ULUSH   # butun sonlarda taqqoslash
        if ball >= MIN_BALL and yuk_mos:
            qaror = "tasdiqlash"
        elif ball >= MIN_BALL:
            qaror = "kamroq summa taklif qilish"
        elif yuk_mos:
            qaror = "kafil bilan koʻrib chiqish"
        else:
            qaror = "rad etish"
    print(f"{ism}: {qaror}")
```
```java
import java.util.Scanner;

public class Main {
    static final int MIN_BALL = 600;    // skoring chegarasi
    static final int MAX_ULUSH = 50;    // toʻlov daromadning necha foizidan oshmasligi kerak

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String ism = in.next();
            long ball = in.nextLong(), daromad = in.nextLong(), tolov = in.nextLong();
            String qaror;
            if (daromad <= 0) {                                    // avval kirish maʼlumotini tekshirish
                qaror = "xato — daromad notoʻgʻri";
            } else {
                boolean yukMos = tolov * 100 <= daromad * MAX_ULUSH;   // butun sonlarda taqqoslash
                if (ball >= MIN_BALL && yukMos) qaror = "tasdiqlash";
                else if (ball >= MIN_BALL) qaror = "kamroq summa taklif qilish";
                else if (yukMos) qaror = "kafil bilan koʻrib chiqish";
                else qaror = "rad etish";
            }
            System.out.println(ism + ": " + qaror);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const int MIN_BALL = 600;    // skoring chegarasi
const int MAX_ULUSH = 50;    // toʻlov daromadning necha foizidan oshmasligi kerak

int main() {
    string ism;
    long long ball, daromad, tolov;
    while (cin >> ism >> ball >> daromad >> tolov) {
        string qaror;
        if (daromad <= 0) {                                       // avval kirish maʼlumotini tekshirish
            qaror = "xato — daromad notoʻgʻri";
        } else {
            bool yukMos = tolov * 100 <= daromad * MAX_ULUSH;     // butun sonlarda taqqoslash
            if (ball >= MIN_BALL && yukMos) qaror = "tasdiqlash";
            else if (ball >= MIN_BALL) qaror = "kamroq summa taklif qilish";
            else if (yukMos) qaror = "kafil bilan koʻrib chiqish";
            else qaror = "rad etish";
        }
        cout << ism << ": " << qaror << "\n";
    }
    return 0;
}
```

Kirish:

```text
Aliyev 720 8000000 3000000
Karimova 650 6000000 3500000
Tursunov 580 10000000 2000000
Rahimov 540 4000000 2500000
Usmonova 600 7000000 3500000
Nazarov 610 0 1000000
```

Chiqish:

```text
Aliyev: tasdiqlash
Karimova: kamroq summa taklif qilish
Tursunov: kafil bilan koʻrib chiqish
Rahimov: rad etish
Usmonova: tasdiqlash
Nazarov: xato — daromad notoʻgʻri
```

Eʼtibor bering:

- **Usmonova** — chegaraviy holat: ball aynan 600, toʻlov aynan 50%. `>=` va `<=` tufayli tasdiqlanadi. Agar `>` yozilganda, u notoʻgʻri rad etilardi — shuning uchun testlarda chegaraviy qiymatlar albatta boʻlishi kerak.
- **Nazarov** — daromad 0: tekshiruv birinchi turgani uchun dastur nolga boʻlish yoki maʼnosiz qaror bilan "yiqilmaydi".
- Ulush `tolov * 100 <= daromad * 50` koʻrinishida **butun sonlarda** taqqoslanadi — haqiqiy sonlar xatosidan (MT12) qochish uchun.
- `elif ball >= MIN_BALL` qatoriga yetib kelganda `yuk_mos` albatta yolgʻon — oldingi shart buni allaqachon "filtrlagan". Zanjir tartibi mantiqning bir qismi.

## Chegaraviy qiymatlar bilan test qilish

Har bir shart uchun uch xil qiymat sinanadi: chegaradan biroz past, aynan chegara, biroz yuqori. Ball uchun: 599, 600, 601. Toʻlov ulushi uchun: 49%, 50%, 51%. Bundan tashqari — notoʻgʻri kirishlar: 0, manfiy son, juda katta son. Toʻrt qoidali qaror jadvalining har bir ustuni kamida bitta test bilan qoplanishi kerak.

| Test | Ball | Ulush | Kutilgan qaror |
|---|---|---|---|
| 1 | 600 | 50% | tasdiqlash |
| 2 | 599 | 50% | kafil bilan koʻrib chiqish |
| 3 | 600 | 51% | kamroq summa taklif qilish |
| 4 | 599 | 51% | rad etish |

:::warning
**Koʻp uchraydigan xatolar**

- **`=` va `==` ni adashtirish.** `=` — qiymat berish, `==` — taqqoslash.
- **Shartlar tartibi notoʻgʻri.** Keng shart birinchi tursa, tor shartlarga navbat kelmaydi.
- **Oraliqlarning kesishishi yoki boʻshligʻi.** Masalan, `< 600` va `> 600` — aynan 600 hech qaysi tarmoqqa tushmaydi.
- **Haqiqiy sonlarni `==` bilan taqqoslash.** Butun sonlar yoki ruxsat etilgan farq ishlating.
- **Kirish maʼlumotini tekshirmaslik.** Nol, boʻsh yoki manfiy qiymat dasturni buzadi.
- **Java va C++ da qavslarsiz bir nechta qator.** `if` dan keyin `{ }` siz faqat birinchi qator shartga bogʻlanadi.
:::

## Topshiriqlar

1. Misol dasturini yuqoridagi 4 ta chegaraviy test bilan sinang (daromad 10 000 000, toʻlov 5 000 000 yoki 5 100 000).
2. Dasturga yangi qoida qoʻshing: ball 800 dan yuqori boʻlsa, toʻlov ulushi 60% gacha ruxsat etiladi. Qoida zanjirning qayeriga yozilishi kerak? Nima uchun?
3. MT11 da oʻzingiz tuzgan "ish haqiga mukofot" qaror jadvalini Python yoki boshqa tilda dasturga oʻtkazing.
4. Hisob turi ("joriy", "omonat", "kredit") boʻyicha xizmat haqini chiqaruvchi dasturni `match` (Python 3.10+) yoki `switch` (Java, C++) bilan yozing.

## Xulosa

- Taqqoslash va mantiqiy amallar (VA, YOKI, EMAS) shartlarni quradi; qisqa tutashuv xavfli amallarni himoya qiladi.
- Shakllar: if, if-else, if-elif-else zanjiri, ichma-ich shart, match/switch; zanjirda tartib muhim.
- Qaror jadvali dasturga bevosita oʻtkaziladi; kirish maʼlumotlari avval tekshiriladi.
- Test: har bir chegara uchun past, aynan va yuqori qiymatlar, notoʻgʻri kirishlar.

## Oʻzingizni tekshiring

:::check
Q: Qisqa tutashuv nima?
A: VA amalida birinchi shart yolgʻon boʻlsa (yoki YOKI da rost boʻlsa), ikkinchisi tekshirilmaydi.
Q: if-elif-else zanjirida nima uchun tartib muhim?
A: Shartlar yuqoridan pastga tekshiriladi va birinchi rost shartdan keyingilari bajarilmaydi.
Q: "ball ≥ 600" sharti uchun qaysi chegaraviy qiymatlar sinanadi?
A: 599, 600 va 601.
Q: Nima uchun misolda ulush butun sonlarda taqqoslangan?
A: Haqiqiy sonlar xatolari tufayli chegaraviy holat notoʻgʻri baholanmasligi uchun.
:::

:::link https://docs.python.org/3/tutorial/controlflow.html
Python: More Control Flow Tools
if, match va boshqa boshqaruv tuzilmalari boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
