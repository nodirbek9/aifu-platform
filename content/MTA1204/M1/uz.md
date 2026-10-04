---
title: Algoritm va uning taʼriflari. Tyuring mashinasi va tezisi
summary: Algoritmning intuitiv, formal va kibernetik taʼriflari; diskretlik, aniqlik, natijadorlik, ommaviylik va cheklilik xossalari; algoritm turlari; Tyuring mashinasi — lenta, kallak, holatlar va oʻtishlar jadvali; Chyorch–Tyuring tezisi va hisoblab boʻlmaydigan masalalar.
---

## Nima uchun bu mavzu muhim?

„Algoritm“ soʻzini har kuni eshitamiz: navigator yoʻl topadi, ijtimoiy tarmoq lentani tartiblaydi, bank kreditni baholaydi. Lekin aniq nimani algoritm deyish mumkin? Retsept algoritmmi? „Yaxshi dastur yoz“ degan koʻrsatma-chi? Bu savollar XX asr boshida matematiklar oldida jiddiy turgan: „har qanday matematik masalani yechadigan mexanik usul bormi?“ degan savolga javob berish uchun avval „mexanik usul“ ning oʻzini aniq taʼriflash kerak edi.

Bu maʼruzada algoritm tushunchasining uch xil taʼrifini, uning xossalarini va eng mashhur formal model — **Tyuring mashinasi**ni koʻramiz. Bu bilimlar butun kursning poydevori: keyingi mavzularda algoritmlarning tezligini solishtirganimizda aynan shu tushunchalarga tayanamiz.

:::note
**Bu darsdan soʻng siz:**

- algoritmning intuitiv, formal va kibernetik taʼriflarini farqlay olasiz;
- algoritmning beshta asosiy xossasini sanab, misollarda tekshira olasiz;
- algoritmlarni tuzilishi boʻyicha turlarga ajrata olasiz;
- Tyuring mashinasining tuzilishini tushuntirib, oddiy mashina ishini qadamma-qadam kuzata olasiz;
- Chyorch–Tyuring tezisi va hisoblab boʻlmaydigan masalalar nima ekanini ayta olasiz.
:::

## Algoritmning uch taʼrifi

| Taʼrif | Mazmuni | Kimga qulay |
|---|---|---|
| **Intuitiv** | maʼlum maqsadga erishish uchun bajaruvchiga beriladigan aniq, tartiblangan koʻrsatmalar ketma-ketligi | kundalik hayot, dasturlashni oʻrganish |
| **Formal** | aniq matematik model (Tyuring mashinasi, Markov normal algoritmi, rekursiv funksiyalar) doirasida yozilgan jarayon | matematika, hisoblash nazariyasi |
| **Kibernetik** | boshqaruv obyektini boshlangʻich holatdan maqsadli holatga oʻtkazuvchi buyruqlar tizimi; bajaruvchi — inson, avtomat yoki kompyuter | boshqaruv tizimlari, avtomatika |

„Algoritm“ soʻzi IX asrda yashagan buyuk vatandoshimiz **Muhammad ibn Muso al-Xorazmiy** nomining lotincha shakli — *Algoritmi* dan kelib chiqqan. Uning hind raqamlari bilan hisoblash haqidagi asari Yevropada arifmetik amallarni bajarish qoidalarini tarqatgan.

## Algoritm xossalari

| Xossa | Maʼnosi | Buzilgan holatga misol |
|---|---|---|
| **Diskretlik** | jarayon alohida, ketma-ket qadamlardan iborat | „sekin-asta qizdiring“ — qadam aniq emas |
| **Aniqlik** (determinantlik) | har bir qadam bir maʼnoli, bajaruvchi nima qilishni aniq biladi | „bir oz tuz qoʻshing“ |
| **Natijadorlik** | algoritm kerakli natijani beradi | natija chiqarmaydigan sikl |
| **Ommaviylik** | bitta masala emas, bir turdagi masalalar sinfi uchun ishlaydi | faqat 12 va 18 uchun EKUB topadigan koʻrsatma |
| **Cheklilik** | har qanday toʻgʻri kirishda chekli qadamdan keyin toʻxtaydi | `while true:` — toʻxtamaydigan sikl |

Cheklilikni isbotlash har doim ham oson emas. Mashhur **Kollats gipotezasi**: istalgan natural sondan boshlab, juft boʻlsa ikkiga boʻlib, toq boʻlsa 3 ga koʻpaytirib 1 qoʻshib borilsa, oxir-oqibat 1 ga kelinadi. Bu juda katta sonlargacha kompyuterda tekshirilgan, lekin hali **isbotlanmagan** — demak, bu jarayon har qanday kirish uchun chekli ekanini hozircha hech kim kafolatlay olmaydi.

## Algoritm turlari

| Tur | Tuzilishi | Misol |
|---|---|---|
| Chiziqli | buyruqlar ketma-ket, shartsiz | toʻrtburchak yuzini hisoblash |
| Tarmoqlanuvchi | shartga qarab turli yoʻllar | kvadrat tenglama ildizlari (diskriminant ishorasi) |
| Takrorlanuvchi (siklik) | buyruqlar guruhi koʻp marta bajariladi | massiv yigʻindisi |
| Rekursiv | algoritm oʻzini kichikroq kirish bilan chaqiradi | faktorial, Xanoy minoralari |

Algoritmlarni tasvirlash usullari: **soʻz bilan**, **blok-sxema** (romb — shart, toʻrtburchak — amal, parallelogramm — kirish/chiqish), **psevdokod** va **dasturlash tili**.

## Tyuring mashinasi

1936-yilda ingliz matematigi **Alan Tyuring** „mexanik hisoblash“ ni taʼriflash uchun xayoliy mashina taklif qildi. U juda oddiy, lekin har qanday kompyuter bajara oladigan ishni bajara oladi.

| Qism | Vazifasi |
|---|---|
| **Lenta** | ikki tomonga cheksiz, kataklarga boʻlingan; har bir katakda alifbodan bitta belgi (boʻsh katak — `_`) |
| **Kallak** | bir vaqtda bitta katakni koʻradi: belgini oʻqiydi, yozadi va bir katak chapga (L) yoki oʻngga (R) suriladi |
| **Holatlar** | mashinaning chekli „xotirasi“: boshlangʻich holat, ishchi holatlar va toʻxtash holati |
| **Oʻtishlar jadvali** | dastur: (joriy holat, oʻqilgan belgi) → (yoziladigan belgi, harakat, yangi holat) |

Har bir qadamda mashina jadvaldan bitta qatorni topib bajaradi. Jadvalda mos qator boʻlmasa yoki toʻxtash holatiga kelinsa, mashina toʻxtaydi; natija — lentada qolgan yozuv.

## Chyorch–Tyuring tezisi

**Chyorch–Tyuring tezisi**: intuitiv maʼnoda algoritm bilan hisoblanadigan har qanday funksiyani Tyuring mashinasi ham hisoblay oladi. Bu teorema emas — „intuitiv algoritm“ aniq taʼriflanmagani uchun uni isbotlab boʻlmaydi. Lekin hozirgacha taklif qilingan barcha hisoblash modellari (Markov algoritmlari, lambda-hisob, zamonaviy dasturlash tillari) Tyuring mashinasiga **teng kuchli** ekani isbotlangan.

Muhim natija: algoritm bilan **umuman yechib boʻlmaydigan** masalalar mavjud. Eng mashhuri — **toʻxtash muammosi**: istalgan dastur va kirish uchun „bu dastur toʻxtaydimi?“ degan savolga har doim toʻgʻri javob beruvchi algoritm yoʻq (Tyuring, 1936).

## Ishlangan misol 1: Kollats ketma-ketligi va cheklilik

*n* kiritiladi (1 ≤ *n* ≤ 10⁹). Kollats qoidasi boʻyicha 1 ga yetguncha nechta qadam kerakligini va yoʻldagi eng katta qiymatni chiqaring.

```python
n = int(input())
qadam = 0
eng_katta = n
while n != 1:                   # cheklilik isbotlanmagan sikl!
    if n % 2 == 0:
        n //= 2
    else:
        n = 3 * n + 1
    qadam += 1
    eng_katta = max(eng_katta, n)
print(qadam, eng_katta)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long qadam = 0, engKatta = n;
        while (n != 1) {                // cheklilik isbotlanmagan sikl!
            if (n % 2 == 0) n /= 2;
            else n = 3 * n + 1;
            qadam++;
            engKatta = Math.max(engKatta, n);
        }
        System.out.println(qadam + " " + engKatta);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long qadam = 0, engKatta = n;
    while (n != 1) {                    // cheklilik isbotlanmagan sikl!
        if (n % 2 == 0) n /= 2;
        else n = 3 * n + 1;
        qadam++;
        engKatta = max(engKatta, n);
    }
    cout << qadam << " " << engKatta << endl;
    return 0;
}
```

`6` kiritilsa: 6 → 3 → 10 → 5 → 16 → 8 → 4 → 2 → 1, chiqish `8 16`. `27` kiritilsa — `111 9232`: kichik son 9232 gacha koʻtarilib, 111 qadamdan keyin tushadi. Bu dastur **diskret** (har bir aylanish — bitta qadam), **aniq** (har qadamda qoida bitta) va **ommaviy** (istalgan *n* uchun). Lekin uning **chekli**ligi — matematikaning ochiq muammosi. Qiymatlar *n* dan ancha oshib ketishi mumkin, shuning uchun Java va C++'da `long` ishlatildi.

## Ishlangan misol 2: Tyuring mashinasi — ikkilik songa bir qoʻshish

Lentada ikkilik son yozilgan, kallak uning **oxirgi** raqamida turibdi. Mashina songa 1 qoʻshadi:

| Holat | Oʻqildi | Yoziladi | Harakat | Yangi holat |
|---|---|---|---|---|
| `q0` | `1` | `0` | L | `q0` (oʻtkazma davom etadi) |
| `q0` | `0` | `1` | — | `stop` |
| `q0` | `_` | `1` | — | `stop` (son boshiga yangi raqam) |

Ikkilik son kiritiladi. Mashinani simulyatsiya qilib, natijani va bajarilgan qadamlar sonini chiqaring.

```python
lenta = list(input().strip())
kallak = len(lenta) - 1         # oxirgi raqam
holat = "q0"
qadam = 0

# oʻtishlar jadvali: (holat, belgi) -> (yoziladi, harakat, yangi holat)
jadval = {
    ("q0", "1"): ("0", -1, "q0"),
    ("q0", "0"): ("1", 0, "stop"),
    ("q0", "_"): ("1", 0, "stop"),
}

while holat != "stop":
    if kallak < 0:              # lenta chapga cheksiz: boʻsh katak qoʻshamiz
        lenta.insert(0, "_")
        kallak = 0
    yoz, harakat, holat = jadval[(holat, lenta[kallak])]
    lenta[kallak] = yoz
    kallak += harakat
    qadam += 1

print("".join(lenta).strip("_"))
print(qadam)
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    record Otish(char yoz, int harakat, String holat) {}

    public static void main(String[] args) {
        StringBuilder lenta = new StringBuilder(new Scanner(System.in).next());
        int kallak = lenta.length() - 1;    // oxirgi raqam
        String holat = "q0";
        int qadam = 0;

        // oʻtishlar jadvali: holat + belgi -> (yoziladi, harakat, yangi holat)
        Map<String, Otish> jadval = new HashMap<>();
        jadval.put("q0 1", new Otish('0', -1, "q0"));
        jadval.put("q0 0", new Otish('1', 0, "stop"));
        jadval.put("q0 _", new Otish('1', 0, "stop"));

        while (!holat.equals("stop")) {
            if (kallak < 0) {               // lenta chapga cheksiz: boʻsh katak qoʻshamiz
                lenta.insert(0, '_');
                kallak = 0;
            }
            Otish o = jadval.get(holat + " " + lenta.charAt(kallak));
            lenta.setCharAt(kallak, o.yoz());
            kallak += o.harakat();
            holat = o.holat();
            qadam++;
        }

        System.out.println(lenta.toString().replace("_", ""));
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
    string lenta;
    cin >> lenta;
    int kallak = lenta.size() - 1;          // oxirgi raqam
    string holat = "q0";
    int qadam = 0;

    // oʻtishlar jadvali: (holat, belgi) -> (yoziladi, harakat, yangi holat)
    map<pair<string, char>, tuple<char, int, string>> jadval = {
        {{"q0", '1'}, {'0', -1, "q0"}},
        {{"q0", '0'}, {'1', 0, "stop"}},
        {{"q0", '_'}, {'1', 0, "stop"}},
    };

    while (holat != "stop") {
        if (kallak < 0) {                   // lenta chapga cheksiz: boʻsh katak qoʻshamiz
            lenta.insert(lenta.begin(), '_');
            kallak = 0;
        }
        auto [yoz, harakat, yangi] = jadval[{holat, lenta[kallak]}];
        lenta[kallak] = yoz;
        kallak += harakat;
        holat = yangi;
        qadam++;
    }

    string natija;
    for (char c : lenta) if (c != '_') natija += c;
    cout << natija << endl << qadam << endl;
    return 0;
}
```

`1011` (11) kiritilsa, chiqish `1100` (12) va `3`: ikkita oxirgi `1` → `0` ga aylanadi (oʻtkazma), uchinchi qadamda `0` → `1`. `111` (7) uchun natija `1000` (8) va `4` qadam: oxirgi qadamda kallak lenta boshidan chiqib, boʻsh katakka `1` yozadi. Mashina dasturi — atigi uch qatorli jadval, simulyator esa istalgan jadval bilan ishlaydi: jadvalni almashtirsangiz, boshqa mashina hosil boʻladi. Zamonaviy kompyuter ham aslida shunday: protsessor — „simulyator“, dastur — „jadval“.

:::warning
**Koʻp uchraydigan xatolar**

- **Har qanday koʻrsatmalar roʻyxatini algoritm deb hisoblash.** „Bir oz“, „tayyor boʻlguncha“ kabi noaniq qadamlar aniqlik xossasini buzadi.
- **Cheklilikni tekshirmaslik.** Sikl sharti hech qachon yolgʻon boʻlmasa, dastur osilib qoladi; har bir sikl uchun „nima uchun u toʻxtaydi?“ deb soʻrang.
- **Chyorch–Tyuring tezisini teorema deb atash.** U isbotlanmagan, lekin hamma tomonidan qabul qilingan gipoteza.
- **Tyuring mashinasini haqiqiy qurilma deb oʻylash.** U — matematik model; cheksiz lenta faqat nazariy.
:::

:::tip
Tyuring mashinasining ishini qogʻozda kuzating: har bir qadam uchun lenta holatini yozing va kallak turgan katakni tagiga chizing. Bitta mashinani qoʻlda „bajarish“ uning gʻoyasini oʻnlab taʼriflardan yaxshiroq tushuntiradi.
:::

## Xulosa

- Algoritmning intuitiv taʼrifi — aniq koʻrsatmalar ketma-ketligi; formal taʼrif — matematik model; kibernetik taʼrif — obyektni maqsadli holatga oʻtkazuvchi buyruqlar tizimi.
- Algoritm xossalari: diskretlik, aniqlik, natijadorlik, ommaviylik, cheklilik.
- Tuzilishi boʻyicha algoritmlar chiziqli, tarmoqlanuvchi, takrorlanuvchi va rekursiv boʻladi.
- Tyuring mashinasi lenta, kallak, chekli holatlar va oʻtishlar jadvalidan iborat; u har qanday kompyuter qila oladigan ishni bajara oladi.
- Chyorch–Tyuring tezisi: intuitiv hisoblanuvchi = Tyuring mashinasida hisoblanuvchi; toʻxtash muammosi algoritm bilan yechilmaydi.

## Oʻzingizni tekshiring

:::check
Q: „Algoritm“ soʻzi kimning nomidan kelib chiqqan?
A: IX asr olimi Muhammad ibn Muso al-Xorazmiy nomining lotincha shakli — *Algoritmi* dan.
Q: „Pirogni tayyor boʻlguncha pishiring“ koʻrsatmasi qaysi xossani buzadi?
A: Aniqlikni (va cheklilikni ham): „tayyor“ holati aniq taʼriflanmagan.
Q: Tyuring mashinasining oʻtishlar jadvalidagi bitta qator nimani bildiradi?
A: (Joriy holat, oʻqilgan belgi) juftligi uchun: qaysi belgi yoziladi, kallak qayerga suriladi va mashina qaysi holatga oʻtadi.
Q: Ikkilik songa bir qoʻshuvchi mashina `1111` uchun nechta qadam bajaradi?
A: 5 ta: toʻrtta `1` ni `0` ga aylantiradi va beshinchi qadamda boʻsh katakka `1` yozadi.
Q: Nima uchun Chyorch–Tyuring tezisini isbotlab boʻlmaydi?
A: Unda „intuitiv algoritm“ tushunchasi qatnashadi, u esa matematik jihatdan aniq taʼriflanmagan.
:::

:::link https://plato.stanford.edu/entries/turing-machine/
Stenford falsafa ensiklopediyasi: Tyuring mashinalari
Tyuring mashinasining taʼrifi, misollari va tarixi (ingliz tilida).
:::

:::link https://en.wikipedia.org/wiki/Collatz_conjecture
Kollats gipotezasi
Hozirgacha isbotlanmagan mashhur masala haqida (ingliz tilida).
:::
