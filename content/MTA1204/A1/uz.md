---
title: Algoritm va uning taʼriflari. Tyuring mashinasi (amaliy mashgʻulot)
summary: Algoritm xossalarini amalda tekshirish, qadamlar jadvalini tuzish, „rus dehqoni“ koʻpaytirish usuli; universal Tyuring mashinasi simulyatori — oʻtishlar jadvalini kiritish, unar qoʻshish mashinasi, toʻxtamaydigan mashinani qadamlar chegarasi bilan aniqlash.
---

## Nima uchun bu mavzu muhim?

Maʼruzada algoritm xossalari va Tyuring mashinasi bilan nazariy tanishdik. Amaliyotda esa ikki savolga javob beramiz: berilgan koʻrsatmalar haqiqatan algoritmmi va uni qanday qilib „mashina tilida“ — aniq qadamlar va holatlar bilan — yozish mumkin?

Bu mashgʻulotda qadimiy koʻpaytirish algoritmini qadamlar jadvali bilan tekshiramiz va **universal** Tyuring mashinasi simulyatorini yozamiz: u oʻtishlar jadvalini kirishdan oʻqiydi va istalgan mashinani bajaradi. Shu bilan birga cheklilik xossasining amaliy tomonini koʻramiz: toʻxtamaydigan mashinani qanday „ushlash“ mumkin.

:::note
**Bu mashgʻulotdan soʻng siz:**

- berilgan koʻrsatmalar algoritm xossalariga mos kelishini tekshira olasiz;
- algoritm bajarilishini qadamlar jadvali bilan kuzata olasiz;
- Tyuring mashinasining oʻtishlar jadvalini tuza va uni simulyatorda sinay olasiz;
- toʻxtamaydigan jarayonni qadamlar chegarasi bilan aniqlay olasiz.
:::

## Algoritmni tekshirish tartibi

Har qanday koʻrsatmalar roʻyxatini algoritm deyishdan oldin besh savolga javob beriladi:

| Xossa | Tekshiruv savoli |
|---|---|
| Diskretlik | jarayon alohida qadamlarga ajratilganmi? |
| Aniqlik | har bir qadamda bajaruvchi nima qilishni bir maʼnoda biladimi? |
| Natijadorlik | jarayon oxirida kerakli natija hosil boʻladimi? |
| Ommaviylik | koʻrsatmalar faqat bitta kirish uchunmi yoki butun sinf uchunmi? |
| Cheklilik | har qanday toʻgʻri kirishda jarayon toʻxtaydimi va nima uchun? |

Cheklilikni koʻrsatishning odatiy usuli — har qadamda **qatʼiy kamayadigan** va pastdan chegaralangan miqdorni topish. Masalan, *a* har qadamda ikki baravar kamaysa va *a* > 0 shart boʻlsa, qadamlar soni log₂ *a* dan oshmaydi.

## Tyuring mashinasini yozish

Mashina **oʻtishlar jadvali** bilan beriladi. Simulyatorda har bir qoida bitta satr:

```text
holat oʻqilgan_belgi yoziladigan_belgi harakat yangi_holat
```

Harakat: `L` — chapga, `R` — oʻngga, `N` — joyida qolish. Boʻsh katak — `_`. Mashina `stop` holatiga kelganda yoki jadvalda mos qoida boʻlmaganda toʻxtaydi.

Lenta cheksiz boʻlgani uchun simulyatorda u **lugʻat** (pozitsiya → belgi) sifatida saqlanadi: yozilmagan kataklar avtomatik boʻsh hisoblanadi. Toʻxtash muammosi algoritm bilan yechilmaydi, shuning uchun simulyator **qadamlar chegarasi** qoʻyadi: 10 000 qadamda toʻxtamagan mashina „toʻxtamadi“ deb hisoblanadi. Bu isbot emas, amaliy himoya.

## Ishlangan misol 1: „rus dehqoni“ koʻpaytirish usuli

Qadimdan maʼlum algoritm: *a* va *b* ni koʻpaytirish uchun *a* ni ikkiga boʻlib (qoldiqsiz), *b* ni ikkiga koʻpaytirib boriladi; *a* toq boʻlgan qatorlardagi *b* qiymatlari qoʻshiladi. *a* va *b* kiritiladi (1 ≤ *a*, *b* ≤ 10⁶). Koʻpaytmani va qadamlar sonini chiqaring.

```python
a, b = map(int, input().split())
kopaytma = 0
qadam = 0
while a > 0:                    # a har qadamda kamayadi — cheklilik
    qadam += 1
    if a % 2 == 1:
        kopaytma += b
    a //= 2
    b *= 2
print(kopaytma, qadam)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long a = in.nextLong(), b = in.nextLong();
        long kopaytma = 0, qadam = 0;
        while (a > 0) {                 // a har qadamda kamayadi — cheklilik
            qadam++;
            if (a % 2 == 1) kopaytma += b;
            a /= 2;
            b *= 2;
        }
        System.out.println(kopaytma + " " + qadam);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long a, b;
    cin >> a >> b;
    long long kopaytma = 0, qadam = 0;
    while (a > 0) {                     // a har qadamda kamayadi — cheklilik
        qadam++;
        if (a % 2 == 1) kopaytma += b;
        a /= 2;
        b *= 2;
    }
    cout << kopaytma << " " << qadam << endl;
    return 0;
}
```

`13 21` uchun qadamlar jadvali:

| *a* | *b* | *a* toqmi? | Yigʻindi |
|---|---|---|---|
| 13 | 21 | ha | 21 |
| 6 | 42 | yoʻq | 21 |
| 3 | 84 | ha | 105 |
| 1 | 168 | ha | 273 |

Chiqish: `273 4`. Xossalar tekshiruvi: **diskret** (har qator — qadam), **aniq** (har qadamda bitta qoida), **ommaviy** (istalgan natural *a*, *b*), **natijador** (13 · 21 = 273), **chekli** (*a* har qadamda kamida ikki baravar kamayadi, demak ⌊log₂ *a*⌋ + 1 qadam). Algoritm aslida *a* ni ikkilik sanoq sistemasida yozish: 13 = 1101₂, yaʼni 13 · 21 = 21 + 4 · 21 + 8 · 21. Kompyuter protsessorlari koʻpaytirishni shunga oʻxshash usulda bajaradi.

## Ishlangan misol 2: universal Tyuring mashinasi simulyatori

Birinchi satrda qoidalar soni *k*, keyingi *k* satrda qoidalar (`holat belgi yoz harakat yangi`), soʻng boshlangʻich holat va lenta. Kallak lentaning birinchi belgisida turadi. Mashinani bajaring: toʻxtasa, lentadagi yozuvni (boʻsh chetlarsiz; lenta boʻsh qolsa `_`) va qadamlar sonini chiqaring; 10 000 qadamda toʻxtamasa — `toʻxtamadi`.

```python
k = int(input())
jadval = {}
for _ in range(k):
    holat, belgi, yoz, harakat, yangi = input().split()
    jadval[(holat, belgi)] = (yoz, harakat, yangi)
holat = input().strip()
lenta = dict(enumerate(input().strip()))    # pozitsiya -> belgi
kallak = 0
qadam = 0
SURILISH = {"L": -1, "R": 1, "N": 0}

while holat != "stop" and qadam < 10000:
    belgi = lenta.get(kallak, "_")
    if (holat, belgi) not in jadval:        # mos qoida yoʻq — toʻxtash
        break
    yoz, harakat, holat = jadval[(holat, belgi)]
    lenta[kallak] = yoz
    kallak += SURILISH[harakat]
    qadam += 1

if holat != "stop" and qadam >= 10000:
    print("toʻxtamadi")
else:
    band = [i for i in lenta if lenta[i] != "_"]
    if band:
        print("".join(lenta.get(i, "_") for i in range(min(band), max(band) + 1)))
    else:
        print("_")
    print(qadam)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        Map<String, String[]> jadval = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String holat = in.next(), belgi = in.next();
            jadval.put(holat + " " + belgi, new String[] {in.next(), in.next(), in.next()});
        }
        String holat = in.next();
        String kirish = in.next();
        TreeMap<Integer, Character> lenta = new TreeMap<>();   // pozitsiya -> belgi
        for (int i = 0; i < kirish.length(); i++) lenta.put(i, kirish.charAt(i));

        int kallak = 0, qadam = 0;
        while (!holat.equals("stop") && qadam < 10000) {
            char belgi = lenta.getOrDefault(kallak, '_');
            String[] q = jadval.get(holat + " " + belgi);
            if (q == null) break;                               // mos qoida yoʻq — toʻxtash
            lenta.put(kallak, q[0].charAt(0));
            kallak += q[1].equals("L") ? -1 : q[1].equals("R") ? 1 : 0;
            holat = q[2];
            qadam++;
        }

        if (!holat.equals("stop") && qadam >= 10000) {
            System.out.println("toʻxtamadi");
            return;
        }
        Integer bosh = null, oxir = null;
        for (Map.Entry<Integer, Character> e : lenta.entrySet()) {
            if (e.getValue() != '_') {
                if (bosh == null) bosh = e.getKey();
                oxir = e.getKey();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (bosh == null) sb.append('_');
        else for (int i = bosh; i <= oxir; i++) sb.append(lenta.getOrDefault(i, '_'));
        System.out.println(sb);
        System.out.println(qadam);
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
#include <tuple>
using namespace std;

int main() {
    int k;
    cin >> k;
    map<pair<string, char>, tuple<char, char, string>> jadval;
    for (int i = 0; i < k; i++) {
        string holat, yangi;
        char belgi, yoz, harakat;
        cin >> holat >> belgi >> yoz >> harakat >> yangi;
        jadval[{holat, belgi}] = {yoz, harakat, yangi};
    }
    string holat, kirish;
    cin >> holat >> kirish;
    map<int, char> lenta;                               // pozitsiya -> belgi
    for (int i = 0; i < (int) kirish.size(); i++) lenta[i] = kirish[i];

    int kallak = 0, qadam = 0;
    while (holat != "stop" && qadam < 10000) {
        char belgi = lenta.count(kallak) ? lenta[kallak] : '_';
        auto it = jadval.find({holat, belgi});
        if (it == jadval.end()) break;                  // mos qoida yoʻq — toʻxtash
        auto [yoz, harakat, yangi] = it->second;
        lenta[kallak] = yoz;
        kallak += harakat == 'L' ? -1 : harakat == 'R' ? 1 : 0;
        holat = yangi;
        qadam++;
    }

    if (holat != "stop" && qadam >= 10000) {
        cout << "toʻxtamadi" << endl;
        return 0;
    }
    int bosh = 0, oxir = -1;
    bool bor = false;
    for (auto& [pozitsiya, belgi] : lenta) {
        if (belgi != '_') {
            if (!bor) bosh = pozitsiya;
            oxir = pozitsiya;
            bor = true;
        }
    }
    string natija;
    if (!bor) natija = "_";
    for (int i = bosh; bor && i <= oxir; i++) natija += lenta.count(i) ? lenta[i] : '_';
    cout << natija << endl << qadam << endl;
    return 0;
}
```

**Unar qoʻshish mashinasi.** Unar sanoq sistemasida *n* son *n* ta `1` bilan yoziladi: 3 + 2 — `111+11`. Mashina `+` ni `1` ga almashtiradi va oxirgi `1` ni oʻchiradi:

```text
5
q0 1 1 R q0
q0 + 1 R q1
q1 1 1 R q1
q1 _ _ L q2
q2 1 _ N stop
q0
111+11
```

Chiqish: `11111` va `8` — 3 + 2 = 5. Mashina avval oʻngga yuradi (`q0`, `q1`), oxiriga yetgach bir katak orqaga qaytib (`q2`), ortiqcha `1` ni oʻchiradi.

**Toʻxtamaydigan mashina.** Quyidagi jadvalda mashina lenta oxiriga yetib, bir katak qaytadi va yana oldinga yuradi — abadiy:

```text
3
q0 1 1 R q0
q0 _ _ L q1
q1 1 1 R q0
q0
11
```

Chiqish: `toʻxtamadi`. Simulyator mashina **hech qachon** toʻxtamasligini isbotlamadi — u faqat 10 000 qadamdan keyin kutishni bas qildi. Toʻxtash muammosining mohiyati shu: umumiy holatda „yana kutish kerakmi?“ degan savolga algoritm javob bera olmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Lentani oddiy massiv qilish.** Kallak chapga chiqib ketsa, manfiy indeks xatoga olib keladi; lugʻat yoki ikki tomonga kengayuvchi tuzilma ishlating.
- **Qadamlar chegarasisiz simulyator.** Toʻxtamaydigan mashina dasturni osib qoʻyadi.
- **Jadvalda bir xil (holat, belgi) juftligi uchun ikki qoida.** Bu aniqlik xossasini buzadi — mashina deterministik boʻlmay qoladi.
- **Cheklilikni „bir necha marta sinab koʻrdim, toʻxtadi“ deb asoslash.** Isbot kamayuvchi miqdor orqali beriladi.
:::

:::tip
Oʻz mashinangizni simulyatorga berishdan oldin uni qogʻozda 3–4 qadam bajaring: har bir qadamda lenta, kallak oʻrni va holatni yozing. Jadvaldagi xato koʻpincha shu bosqichda topiladi.
:::

## Xulosa

- Koʻrsatmalar algoritm boʻlishi uchun beshta xossaning har biri tekshiriladi; cheklilik kamayuvchi miqdor bilan asoslanadi.
- Qadamlar jadvali algoritm qanday ishlashini koʻrsatadi va natijani tekshirishga yordam beradi.
- Universal simulyator oʻtishlar jadvalini kirish sifatida qabul qiladi — jadval almashsa, mashina ham almashadi.
- Cheksiz lenta lugʻat bilan modellashtiriladi; toʻxtamaydigan mashina amalda qadamlar chegarasi bilan aniqlanadi.

## Oʻzingizni tekshiring

:::check
Q: „Rus dehqoni“ usulida *a* = 100 uchun nechta qadam bajariladi?
A: 7 ta: 100 → 50 → 25 → 12 → 6 → 3 → 1 → 0 (⌊log₂ 100⌋ + 1 = 7).
Q: Nima uchun simulyatorda lenta lugʻat sifatida saqlanadi?
A: Lenta ikki tomonga cheksiz; lugʻat istalgan (manfiy ham) pozitsiyani saqlaydi va yozilmagan kataklarni boʻsh deb hisoblashga imkon beradi.
Q: Unar qoʻshish mashinasi `1+1` uchun natija va qadamlar soni qanday?
A: `11` va 5 qadam.
Q: Simulyator „toʻxtamadi“ desa, bu mashina hech qachon toʻxtamasligini isbotlaydimi?
A: Yoʻq — faqat belgilangan qadamlar ichida toʻxtamaganini bildiradi.
:::

:::link https://turingmachine.io/
Turing machine visualization
Tyuring mashinalarini brauzerda qadamma-qadam kuzatish vositasi (ingliz tilida).
:::
