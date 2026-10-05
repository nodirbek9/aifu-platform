---
title: Risk matritsasini qurish va risklarni baholash (amaliy misol)
summary: Kichik tashkilot misolida risklarni baholash — aktivlar va tahdidlar roʻyxati, ehtimollik va taʼsir shkalalari mezonlari, ekspert baholash, risk matritsasi va issiqlik xaritasi, chegaralarni tanlash, risk egalari, choralar va qoldiq risk; ikki baholash orasidagi dinamika; risklar reyestri namunasi.
---

## Nima uchun bu mavzu muhim?

M2 maʼruzasida risk matritsasi nazariyasini koʻrdik. Amalda eng qiyini — formulani qoʻllash emas, balki **ehtimollik va taʼsirni izchil baholash**: "4" nima uchun "3" emas? Turli mutaxassislar bir xil riskni turlicha baholamasligi uchun shkalalarga aniq mezonlar yoziladi. Bu mashgʻulotda kichik tashkilot uchun toʻliq baholashni bosqichma-bosqich bajaramiz va natijani issiqlik xaritasida koʻrsatamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- ehtimollik va taʼsir shkalalariga aniq mezonlar yoza olasiz;
- tashkilot uchun risklar roʻyxatini tuzib, ularni izchil baholay olasiz;
- natijani issiqlik xaritasida tasvirlay olasiz;
- har bir risk uchun egasi, chorasi va qoldiq darajasini belgilay olasiz;
- ikki baholash natijalarini solishtirib, risklar dinamikasini tahlil qila olasiz.
:::

## Shkalalar mezonlari

**Ehtimollik**:

| Ball | Daraja | Mezon |
|---|---|---|
| 1 | Juda kam | 10 yilda bir martadan kam |
| 2 | Kam | 3–10 yilda bir marta |
| 3 | Mumkin | 1–3 yilda bir marta |
| 4 | Ehtimolli | yiliga bir necha marta |
| 5 | Deyarli muqarrar | oyiga bir marta va koʻproq |

**Taʼsir**:

| Ball | Daraja | Moliyaviy | Operatsion | Obroʻ va huquq |
|---|---|---|---|---|
| 1 | Juda kam | < 1 mln soʻm | bir necha daqiqa uzilish | taʼsir yoʻq |
| 2 | Kam | 1–10 mln | bir necha soat | ichki shikoyatlar |
| 3 | Oʻrta | 10–100 mln | 1 kun | mahalliy OAV |
| 4 | Katta | 100 mln – 1 mlrd | bir necha kun | jarima, milliy OAV |
| 5 | Halokatli | > 1 mlrd | bir haftadan ortiq | litsenziyani yoʻqotish, jinoiy javobgarlik |

Taʼsir bir necha ustun boʻyicha baholansa, **eng yuqori** ball olinadi.

## Amaliy ish: "Bilim" oʻquv markazi

Tashkilot: 40 xodim, 600 talaba, bitta server (talabalar bazasi va buxgalteriya), Wi-Fi, onlayn toʻlov sahifasi.

1. **Aktivlar**: talabalar bazasi, buxgalteriya, sayt va toʻlov sahifasi, xodimlar noutbuklari, server xonasi, obroʻ.
2. **Tahdid–zaiflik juftlari** (har biri — alohida risk): fishing (xodimlar oʻqitilmagan), shifrlovchi virus (zaxira nusxa yoʻq), noutbuk oʻgʻirlanishi (disk shifrlanmagan), DDoS (himoya yoʻq), ichki buzgʻunchi (huquqlar keng), yongʻin (server xonasida oʻt oʻchirish tizimi yoʻq), oddiy parollar (siyosat yoʻq), kamera tizimining nosozligi.
3. **Baholash**: har bir riskka mezonlar boʻyicha ehtimollik va taʼsir; ikki-uch xodim mustaqil baholab, natijalar muhokama qilinadi.
4. **Matritsa va xarita** — quyidagi dastur bilan.
5. **Choralar va egalari**: kritik va yuqori risklar uchun — muddat va masʼul bilan reja.
6. **Qoldiq risk**: chora koʻrilgandan keyingi kutilgan ehtimollik va taʼsir.

Risklarni toifalash va daraja boʻyicha tartiblovchi dastur — ushbu mashgʻulotning mustaqil topshirigʻi (KIA-A2).

## Ishlangan misol 1: issiqlik xaritasi

Birinchi satrda risklar soni, keyin har bir risk: nomi, ehtimollik va taʼsir (1–5). 5×5 xaritani chiqaring: qatorlar — ehtimollik 5 dan 1 gacha, ustunlar — taʼsir 1 dan 5 gacha, har bir katakda shu katakdagi risklar soni (`.` — risk yoʻq).

```python
n = int(input())
xarita = [[0] * 6 for _ in range(6)]           # [ehtimollik][taʼsir]
for _ in range(n):
    nom, e, t = input().split()
    xarita[int(e)][int(t)] += 1
print("  | 1 2 3 4 5")
for e in range(5, 0, -1):                       # yuqorida — eng ehtimolli
    kataklar = [str(xarita[e][t]) if xarita[e][t] else "." for t in range(1, 6)]
    print(str(e) + " | " + " ".join(kataklar))
print("jami: " + str(n))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[][] xarita = new int[6][6];                      // [ehtimollik][taʼsir]
        for (int i = 0; i < n; i++) {
            in.next();
            int e = in.nextInt();
            int t = in.nextInt();
            xarita[e][t]++;
        }
        System.out.println("  | 1 2 3 4 5");
        for (int e = 5; e >= 1; e--) {                       // yuqorida — eng ehtimolli
            StringBuilder qator = new StringBuilder(e + " |");
            for (int t = 1; t <= 5; t++) qator.append(" ").append(xarita[e][t] > 0 ? String.valueOf(xarita[e][t]) : ".");
            System.out.println(qator);
        }
        System.out.println("jami: " + n);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    int xarita[6][6] = {};                                    // [ehtimollik][taʼsir]
    for (int i = 0; i < n; i++) {
        string nom;
        int e, t;
        cin >> nom >> e >> t;
        xarita[e][t]++;
    }
    cout << "  | 1 2 3 4 5" << endl;
    for (int e = 5; e >= 1; e--) {                            // yuqorida — eng ehtimolli
        string qator = to_string(e) + " |";
        for (int t = 1; t <= 5; t++) qator += " " + (xarita[e][t] > 0 ? to_string(xarita[e][t]) : string("."));
        cout << qator << endl;
    }
    cout << "jami: " << n << endl;
    return 0;
}
```

Kirish:

```text
8
Fishing 4 3
Shifrlovchi 3 5
Noutbuk 3 2
DDoS 2 4
Insayder 2 5
Yongin 1 5
Oddiy_parol 4 3
Kamera 1 1
```

Chiqish:

```text
  | 1 2 3 4 5
5 | . . . . .
4 | . . 2 . .
3 | . 1 . . 1
2 | . . . 1 1
1 | 1 . . . 1
jami: 8
```

Xaritaning oʻng yuqori burchagiga yaqin kataklar — eng xavfli. Bu yerda ikkita risk (fishing va oddiy parollar) bir katakda — ikkalasini bitta chora (xodimlarni oʻqitish + MFA) bilan kamaytirish mumkin. Pastki oʻng burchakdagi "yongʻin" kam ehtimolli, lekin halokatli — bunday risklarni faqat ball boʻyicha emas, taʼsir boʻyicha ham alohida koʻrib chiqish kerak.

## Ishlangan misol 2: ikki baholash orasidagi dinamika

Choralar koʻrilgandan keyin risklar qayta baholandi. Har bir satrda: nom, oldingi ehtimollik va taʼsir, yangi ehtimollik va taʼsir. Har bir risk uchun daraja oʻzgarishini (`kamaydi`, `oshdi`, `oʻzgarmadi`) va umumiy daraja yigʻindisini chiqaring.

```python
import sys

oldin_jami = keyin_jami = 0
for satr in sys.stdin.read().strip().split("\n")[1:]:
    nom, e1, t1, e2, t2 = satr.split()
    oldin = int(e1) * int(t1)
    keyin = int(e2) * int(t2)
    oldin_jami += oldin
    keyin_jami += keyin
    holat = "kamaydi" if keyin < oldin else "oshdi" if keyin > oldin else "oʻzgarmadi"
    print(nom + ": " + str(oldin) + " -> " + str(keyin) + " " + holat)
print("jami: " + str(oldin_jami) + " -> " + str(keyin_jami))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int oldinJami = 0, keyinJami = 0;
        for (int i = 0; i < n; i++) {
            String nom = in.next();
            int oldin = in.nextInt() * in.nextInt();
            int keyin = in.nextInt() * in.nextInt();
            oldinJami += oldin;
            keyinJami += keyin;
            String holat = keyin < oldin ? "kamaydi" : keyin > oldin ? "oshdi" : "oʻzgarmadi";
            System.out.println(nom + ": " + oldin + " -> " + keyin + " " + holat);
        }
        System.out.println("jami: " + oldinJami + " -> " + keyinJami);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    int oldinJami = 0, keyinJami = 0;
    for (int i = 0; i < n; i++) {
        string nom;
        int e1, t1, e2, t2;
        cin >> nom >> e1 >> t1 >> e2 >> t2;
        int oldin = e1 * t1;
        int keyin = e2 * t2;
        oldinJami += oldin;
        keyinJami += keyin;
        string holat = keyin < oldin ? "kamaydi" : keyin > oldin ? "oshdi" : "oʻzgarmadi";
        cout << nom << ": " << oldin << " -> " << keyin << " " << holat << endl;
    }
    cout << "jami: " << oldinJami << " -> " << keyinJami << endl;
    return 0;
}
```

Kirish:

```text
4
Fishing 4 3 2 3
Shifrlovchi 3 5 3 2
DDoS 2 4 3 4
Kamera 1 1 1 1
```

Chiqish:

```text
Fishing: 12 -> 6 kamaydi
Shifrlovchi: 15 -> 6 kamaydi
DDoS: 8 -> 12 oshdi
Kamera: 1 -> 1 oʻzgarmadi
jami: 36 -> 25
```

Oʻqitish fishing **ehtimolligini**, zaxira nusxa esa shifrlovchi virus **taʼsirini** kamaytirdi — choralar matritsaning turli oʻqlariga taʼsir qiladi. DDoS riski oshgan: tashkilot onlayn toʻlovlarni kengaytirdi va endi sayt toʻxtashi koʻproq zarar keltiradi — risklar faqat choralar bilan emas, biznes oʻzgarishi bilan ham oʻzgaradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Mezonlarsiz baholash.** "Menimcha, 4" — har xil odamlar har xil natija beradi; shkalaga aniq mezon yozing.
- **Tahdid–zaiflik juftini emas, faqat tahdidni baholash.** "Viruslar" — juda umumiy; "zaxirasiz serverda shifrlovchi virus" — baholanadigan risk.
- **Kam ehtimolli, lekin halokatli risklarni eʼtiborsiz qoldirish.** Yongʻin yoki suv toshqini kam boʻladi, lekin tashkilotni yoʻq qilishi mumkin.
- **Choradan keyin qayta baholamaslik.** Chora samarasini faqat qoldiq riskni oʻlchab bilish mumkin.
:::

:::tip
Oʻz oilangiz yoki yotoqxonangiz uchun 6–8 ta raqamli riskni (telefon yoʻqolishi, parol sizishi, Wi-Fi ga begona ulanishi va hokazo) yuqoridagi mezonlar bilan baholang va issiqlik xaritasini chizing. Eng yuqori ikki risk uchun bittadan arzon chora yozing.
:::

## Xulosa

- Ehtimollik va taʼsir shkalalariga aniq, oʻlchanadigan mezonlar yoziladi; taʼsir bir necha ustun boʻyicha baholanib, eng yuqorisi olinadi.
- Risk — tahdid va zaiflik jufti; har biri alohida baholanadi.
- Issiqlik xaritasi risklar taqsimotini koʻrsatadi va bir chora bilan yopiladigan guruhlarni aniqlashga yordam beradi.
- Choralar ehtimollikni yoki taʼsirni kamaytiradi; risklar dinamikasi qayta baholash bilan kuzatiladi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun ehtimollik shkalasiga aniq mezonlar (masalan, "yiliga bir necha marta") yoziladi?
A: Turli baholovchilar bir xil natija berishi, baholash izchil va takrorlanuvchan boʻlishi uchun.
Q: Taʼsir moliyaviy jihatdan 2, obroʻ jihatdan 4 ball boʻlsa, qaysi ball olinadi?
A: 4 — eng yuqori ball.
Q: Zaxira nusxa shifrlovchi virus riskining qaysi komponentini kamaytiradi?
A: Taʼsirni — maʼlumot tiklanadi, uzilish qisqaradi.
Q: Issiqlik xaritasida eng xavfli risklar qayerda joylashadi?
A: Yuqori oʻng burchakda — yuqori ehtimollik va yuqori taʼsir.
Q: Chora koʻrilmasa ham risk darajasi nima uchun oshishi mumkin?
A: Biznes yoki muhit oʻzgaradi: yangi xizmat, yangi tahdid, aktiv qiymatining oshishi.
:::

:::link https://www.iso.org/standard/80585.html
ISO/IEC 27005
Axborot xavfsizligi risklarini boshqarish boʻyicha yoʻriqnoma standarti (ingliz tilida).
:::
