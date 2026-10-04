---
title: Parallel saralash algoritmlari. Dinamik tuzilmalar — navbat, stek va dek
summary: Bouz–Nelson va Betcher saralash tarmoqlari, taqqoslagichlar va tarmoq chuqurligi, juft-toq (transpozitsion) saralash, parallel saralashning afzalligi; maʼlumotlarning dinamik tuzilmalari — stek, navbat va dek, ularni massiv (halqasimon bufer) va koʻrsatkichlar bilan yaratish.
---

## Nima uchun bu mavzu muhim?

Zamonaviy protsessorda 8–16 yadro, videokartada esa minglab hisoblash bloklari bor. Oddiy saralash algoritmlari esa ketma-ket ishlaydi: har bir taqqoslash oldingisining natijasini kutadi. **Parallel saralash** algoritmlari koʻp taqqoslashni **bir vaqtda** bajarishga moʻljallangan — ular videokartalarda, tarmoq kommutatorlarida va maxsus apparat qurilmalarida ishlatiladi.

Maʼruzaning ikkinchi qismi — **dinamik tuzilmalar**: stek, navbat va dek. Ular deyarli har bir dasturda bor: funksiyalar chaqiruvi stekda saqlanadi, printerga yuborilgan hujjatlar navbatda turadi, brauzerning „orqaga-oldinga“ tugmalari stek bilan ishlaydi.

:::note
**Bu darsdan soʻng siz:**

- saralash tarmogʻi, taqqoslagich va tarmoq chuqurligi tushunchalarini tushuntira olasiz;
- juft-toq (transpozitsion) saralashni yozib, uning parallel ekanini asoslay olasiz;
- Betcher tarmogʻi qanday qurilishini va uning murakkabligini ayta olasiz;
- stek, navbat va dekning amallarini va qoʻllanilishini farqlay olasiz;
- navbatni halqasimon bufer koʻrinishida massiv bilan yarata olasiz.
:::

## Saralash tarmoqlari

**Saralash tarmogʻi** — oldindan belgilangan **taqqoslagichlar** ketma-ketligi. Har bir taqqoslagich (*i*, *j*) ikki elementni solishtiradi va kerak boʻlsa almashtiradi, shunda `a[i] ≤ a[j]` boʻladi. Muhim xususiyat: taqqoslashlar tartibi maʼlumotga **bogʻliq emas** — shuning uchun bir-biriga tegmaydigan taqqoslagichlarni bir vaqtda bajarish mumkin.

| Tushuncha | Maʼnosi |
|---|---|
| Taqqoslagich | (*i*, *j*) juftligi: `a[i] > a[j]` boʻlsa almashtir |
| Tarmoq hajmi | taqqoslagichlar soni — ketma-ket ishda vaqtni belgilaydi |
| Tarmoq chuqurligi | parallel bosqichlar soni — parallel ishda vaqtni belgilaydi |

**Bouz–Nelson tarmogʻi** (1962) — saralash tarmogʻini rekursiv qurishning birinchi usullaridan biri: massiv ikkiga boʻlinadi, har bir yarim alohida tarmoq bilan saralanadi, soʻng ikki natija maxsus birlashtiruvchi tarmoq bilan qoʻshiladi.

**Betcher juft-toq birlashtirish tarmogʻi** (K. Betcher, 1968) shu gʻoyani rivojlantiradi: ikkita saralangan ketma-ketlikning juft va toq oʻrindagi elementlari alohida birlashtiriladi, soʻng qoʻshni juftliklar tekshiriladi. Natija:

| *n* | Taqqoslagichlar | Chuqurlik |
|---|---|---|
| 2 | 1 | 1 |
| 4 | 5 | 3 |
| 8 | 19 | 6 |
| 16 | 63 | 10 |

Hajmi *O*(*n* log² *n*) — ketma-ket algoritmlardan (*O*(*n* log *n*)) koʻproq, lekin chuqurligi atigi *O*(log² *n*): *n* ta protsessor bilan saralash log² *n* bosqichda tugaydi. Betcherning yana bir mashhur tarmogʻi — **bitonik saralash**; u videokartalarda keng qoʻllanadi.

## Juft-toq transpozitsion saralash

Eng oddiy parallel saralash: **juft fazada** (0, 1), (2, 3), (4, 5), … juftliklar, **toq fazada** (1, 2), (3, 4), … juftliklar solishtiriladi va kerak boʻlsa almashtiriladi. Bitta faza ichidagi taqqoslashlar bir-biriga tegmaydi — ular bir vaqtda bajarilishi mumkin. *n* ta faza har doim yetarli, demak *n*/2 protsessor bilan vaqt *O*(*n*) (ketma-ket ishda esa *O*(*n*²) — bu aslida pufaksimon saralashning parallel shakli).

## Stek, navbat va dek

| Tuzilma | Tamoyil | Asosiy amallar | Qoʻllanilishi |
|---|---|---|---|
| **Stek** | oxirgi kirgan — birinchi chiqadi (LIFO) | `push`, `pop`, `top` | funksiya chaqiruvlari, „bekor qilish“ (Ctrl+Z), ifodalarni hisoblash, qavslarni tekshirish |
| **Navbat** | birinchi kirgan — birinchi chiqadi (FIFO) | `enqueue` (oxiriga), `dequeue` (boshidan) | printer navbati, server soʻrovlari, grafda kenglik boʻyicha aylanish |
| **Dek** | ikkala uchidan qoʻshish va olish | `push_front`, `push_back`, `pop_front`, `pop_back` | sirpanuvchi oyna, brauzer tarixi |

Barcha amallar *O*(1). Ularni yaratishning ikki yoʻli:

- **Massiv bilan**: stek — massiv va „tepa“ indeksi. Navbat uchun oddiy massiv yaramaydi (boshidan olish elementlarni surishni talab qiladi), shuning uchun **halqasimon bufer** ishlatiladi: `bosh` va `oxir` indekslari massiv oxiriga yetganda `% sigim` bilan boshiga qaytadi.
- **Koʻrsatkichlar bilan** (bogʻlangan tugunlar): har bir element keyingisiga koʻrsatkich saqlaydi; sigʻim cheklanmagan, lekin har bir element uchun qoʻshimcha xotira kerak.

Standart kutubxonalarda: Python — `list` (stek), `collections.deque` (navbat, dek); Java — `ArrayDeque`; C++ — `stack`, `queue`, `deque`.

## Ishlangan misol 1: juft-toq transpozitsion saralash

Birinchi satrda *n*, ikkinchisida *n* ta son. Juft-toq saralash bilan saralang: fazalar navbatma-navbat juft (0 dan) va toq (1 dan) juftliklar bilan bajariladi; ketma-ket ikki fazada almashtirish boʻlmasa (yoki *n* faza bajarilsa) toʻxtang. Saralangan massivni va bajarilgan fazalar sonini chiqaring.

```python
n = int(input())
a = list(map(int, input().split()))

faza = 0
tinch = 0                                   # ketma-ket almashtirishsiz fazalar
while tinch < 2 and faza < n:
    almashdi = False
    for i in range(faza % 2, n - 1, 2):     # bu taqqoslashlar bir-biriga tegmaydi
        if a[i] > a[i + 1]:
            a[i], a[i + 1] = a[i + 1], a[i]
            almashdi = True
    faza += 1
    tinch = 0 if almashdi else tinch + 1

print(*a)
print(faza)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        int faza = 0, tinch = 0;                    // ketma-ket almashtirishsiz fazalar
        while (tinch < 2 && faza < n) {
            boolean almashdi = false;
            for (int i = faza % 2; i < n - 1; i += 2) {     // bu taqqoslashlar bir-biriga tegmaydi
                if (a[i] > a[i + 1]) {
                    int t = a[i]; a[i] = a[i + 1]; a[i + 1] = t;
                    almashdi = true;
                }
            }
            faza++;
            tinch = almashdi ? 0 : tinch + 1;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(sb);
        System.out.println(faza);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    int faza = 0, tinch = 0;                        // ketma-ket almashtirishsiz fazalar
    while (tinch < 2 && faza < n) {
        bool almashdi = false;
        for (int i = faza % 2; i < n - 1; i += 2) {         // bu taqqoslashlar bir-biriga tegmaydi
            if (a[i] > a[i + 1]) {
                swap(a[i], a[i + 1]);
                almashdi = true;
            }
        }
        faza++;
        tinch = almashdi ? 0 : tinch + 1;
    }

    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl << faza << endl;
    return 0;
}
```

`8` va `5 1 4 2 8 3 7 6` uchun chiqish: `1 2 3 4 5 6 7 8` va `6`; teskari tartibdagi `8 7 6 5 4 3 2 1` uchun — 8 faza (eng yomon holat: *n* faza). Bitta faza ichidagi *n*/2 ta taqqoslash bir-biridan mustaqil: parallel kompyuterda har birini alohida protsessor bajaradi va bir faza bitta qadamda tugaydi. Ketma-ket kompyuterda esa bu oddiy pufaksimon saralashdan farq qilmaydi.

## Ishlangan misol 2: Betcher saralash tarmogʻi

*k* kiritiladi (1 ≤ *k* ≤ 4), soʻng 2ᵏ ta son. Betcherning juft-toq birlashtirish tarmogʻini rekursiv quring, uni massivga qoʻllang; taqqoslagichlar sonini va saralangan massivni chiqaring.

```python
def birlashtir(chap, ong, r, tarmoq):
    qadam = r * 2
    if qadam < ong - chap:
        birlashtir(chap, ong, qadam, tarmoq)            # juft oʻrinlar
        birlashtir(chap + r, ong, qadam, tarmoq)        # toq oʻrinlar
        for i in range(chap + r, ong - r, qadam):       # qoʻshnilarni tekshirish
            tarmoq.append((i, i + r))
    else:
        tarmoq.append((chap, chap + r))


def tarmoq_qur(chap, ong, tarmoq):
    if ong - chap >= 1:
        orta = chap + (ong - chap) // 2
        tarmoq_qur(chap, orta, tarmoq)
        tarmoq_qur(orta + 1, ong, tarmoq)
        birlashtir(chap, ong, 1, tarmoq)


k = int(input())
a = list(map(int, input().split()))
tarmoq = []
tarmoq_qur(0, 2 ** k - 1, tarmoq)
for i, j in tarmoq:                                     # taqqoslagich
    if a[i] > a[j]:
        a[i], a[j] = a[j], a[i]
print(len(tarmoq))
print(*a)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static List<int[]> tarmoq = new ArrayList<>();

    static void birlashtir(int chap, int ong, int r) {
        int qadam = r * 2;
        if (qadam < ong - chap) {
            birlashtir(chap, ong, qadam);                   // juft oʻrinlar
            birlashtir(chap + r, ong, qadam);               // toq oʻrinlar
            for (int i = chap + r; i < ong - r; i += qadam) tarmoq.add(new int[] {i, i + r});   // qoʻshnilarni tekshirish
        } else {
            tarmoq.add(new int[] {chap, chap + r});
        }
    }

    static void tarmoqQur(int chap, int ong) {
        if (ong - chap >= 1) {
            int orta = chap + (ong - chap) / 2;
            tarmoqQur(chap, orta);
            tarmoqQur(orta + 1, ong);
            birlashtir(chap, ong, 1);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        int n = 1 << k;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        tarmoqQur(0, n - 1);
        for (int[] t : tarmoq) {                            // taqqoslagich
            if (a[t[0]] > a[t[1]]) {
                int x = a[t[0]]; a[t[0]] = a[t[1]]; a[t[1]] = x;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(a[i]);
        }
        System.out.println(tarmoq.size());
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

vector<pair<int, int>> tarmoq;

void birlashtir(int chap, int ong, int r) {
    int qadam = r * 2;
    if (qadam < ong - chap) {
        birlashtir(chap, ong, qadam);                       // juft oʻrinlar
        birlashtir(chap + r, ong, qadam);                   // toq oʻrinlar
        for (int i = chap + r; i < ong - r; i += qadam) tarmoq.push_back({i, i + r});   // qoʻshnilarni tekshirish
    } else {
        tarmoq.push_back({chap, chap + r});
    }
}

void tarmoqQur(int chap, int ong) {
    if (ong - chap >= 1) {
        int orta = chap + (ong - chap) / 2;
        tarmoqQur(chap, orta);
        tarmoqQur(orta + 1, ong);
        birlashtir(chap, ong, 1);
    }
}

int main() {
    int k;
    cin >> k;
    int n = 1 << k;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    tarmoqQur(0, n - 1);
    for (auto [i, j] : tarmoq) {                            // taqqoslagich
        if (a[i] > a[j]) swap(a[i], a[j]);
    }

    cout << tarmoq.size() << endl;
    for (int i = 0; i < n; i++) cout << (i ? " " : "") << a[i];
    cout << endl;
    return 0;
}
```

`3` va `5 1 4 2 8 3 7 6` uchun chiqish: `19` va `1 2 3 4 5 6 7 8`. Tarmoq **maʼlumotdan oldin** quriladi: qaysi kirish boʻlishidan qatʼi nazar, xuddi shu 19 ta taqqoslagich xuddi shu tartibda bajariladi — bu ularni apparatga „qotirib qoʻyish“ yoki videokartada parallel bajarish imkonini beradi. Ketma-ket ishda esa tarmoq biroz koʻproq ish qiladi: birlashtirib saralash 8 element uchun koʻpi bilan 17 ta taqqoslash bajaradi.

## Ishlangan misol 3: halqasimon buferdagi navbat

Birinchi satrda sigʻim *C* va buyruqlar soni *q*, keyin *q* ta buyruq: `qosh X` — navbat oxiriga qoʻshish (toʻlgan boʻlsa `toʻla`), `ol` — boshidan olib chiqarish (boʻsh boʻlsa `boʻsh`), `hajm` — elementlar soni. Navbat *C* oʻlchamli oddiy massivda saqlanadi.

```python
c, q = map(int, input().split())
bufer = [0] * c
bosh = 0                                    # birinchi element indeksi
hajm = 0

for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "qosh":
        if hajm == c:
            print("toʻla")
        else:
            bufer[(bosh + hajm) % c] = int(buyruq[1])    # oxir — halqa boʻylab
            hajm += 1
    elif buyruq[0] == "ol":
        if hajm == 0:
            print("boʻsh")
        else:
            print(bufer[bosh])
            bosh = (bosh + 1) % c                        # bosh ham halqa boʻylab
            hajm -= 1
    else:
        print(hajm)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int c = in.nextInt(), q = in.nextInt();
        int[] bufer = new int[c];
        int bosh = 0, hajm = 0;                             // bosh — birinchi element indeksi

        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("qosh")) {
                int x = in.nextInt();
                if (hajm == c) {
                    System.out.println("toʻla");
                } else {
                    bufer[(bosh + hajm) % c] = x;           // oxir — halqa boʻylab
                    hajm++;
                }
            } else if (buyruq.equals("ol")) {
                if (hajm == 0) {
                    System.out.println("boʻsh");
                } else {
                    System.out.println(bufer[bosh]);
                    bosh = (bosh + 1) % c;                  // bosh ham halqa boʻylab
                    hajm--;
                }
            } else {
                System.out.println(hajm);
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    int c, q;
    cin >> c >> q;
    vector<int> bufer(c);
    int bosh = 0, hajm = 0;                                 // bosh — birinchi element indeksi

    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "qosh") {
            int x;
            cin >> x;
            if (hajm == c) {
                cout << "toʻla" << endl;
            } else {
                bufer[(bosh + hajm) % c] = x;               // oxir — halqa boʻylab
                hajm++;
            }
        } else if (buyruq == "ol") {
            if (hajm == 0) {
                cout << "boʻsh" << endl;
            } else {
                cout << bufer[bosh] << endl;
                bosh = (bosh + 1) % c;                      // bosh ham halqa boʻylab
                hajm--;
            }
        } else {
            cout << hajm << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
3 11
qosh 5
qosh 7
qosh 9
qosh 1
ol
qosh 1
ol
ol
ol
ol
hajm
```

Chiqish:

```text
toʻla
5
7
9
1
boʻsh
0
```

Sigʻim 3: toʻrtinchi `qosh` rad etildi. `5` olingandan keyin boʻshagan joyga (`bufer[0]`) `1` yozildi — garchi `bosh` endi 1-indeksda tursa ham: oxir `(bosh + hajm) % c` = (1 + 2) % 3 = 0. Shu tarzda massiv halqa kabi ishlaydi va hech qachon elementlarni surish kerak boʻlmaydi — har bir amal *O*(1). Tarmoq kartalari, audio va video oqimlari aynan halqasimon buferlardan foydalanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Navbatni oddiy massivda boshidan olish.** `a.pop(0)` yoki elementlarni surish *O*(*n*); halqasimon bufer yoki `deque` ishlating.
- **Halqasimon buferda `% sigim` ni unutish.** Indeks massiv chegarasidan chiqadi.
- **„Toʻla“ va „boʻsh“ holatlarni faqat `bosh == oxir` bilan farqlashga urinish.** Ikkala holatda ham indekslar teng boʻladi; alohida `hajm` hisoblagichi saqlang.
- **Saralash tarmogʻini maʼlumotga qarab oʻzgartirish.** Tarmoq afzalligi — taqqoslashlar tartibi oldindan maʼlum; agar ular maʼlumotga bogʻliq boʻlsa, parallellik yoʻqoladi.
:::

:::tip
Halqasimon buferni qogʻozda doira qilib chizing: kataklar soat strelkasi boʻyicha, `bosh` va `oxir` — ikki strelka. Har bir buyruqdan keyin strelkalar qanday siljishini belgilang.
:::

## Xulosa

- Saralash tarmogʻi — maʼlumotga bogʻliq boʻlmagan taqqoslagichlar ketma-ketligi; hajmi ketma-ket vaqtni, chuqurligi parallel vaqtni belgilaydi.
- Juft-toq transpozitsion saralash *n* fazada tugaydi; har bir fazadagi taqqoslashlar bir vaqtda bajarilishi mumkin.
- Betcher tarmogʻi: hajmi *O*(*n* log² *n*), chuqurligi *O*(log² *n*); 8 element uchun 19 taqqoslagich.
- Stek (LIFO), navbat (FIFO) va dek (ikki uch) — barcha amallar *O*(1).
- Navbat massivda halqasimon bufer bilan amalga oshiriladi: indekslar `% sigim` bilan aylanadi.

## Oʻzingizni tekshiring

:::check
Q: Saralash tarmogʻining hajmi va chuqurligi nimani bildiradi?
A: Hajm — taqqoslagichlar soni (ketma-ket ish vaqti), chuqurlik — parallel bosqichlar soni (parallel ish vaqti).
Q: Juft-toq saralashda bitta faza ichidagi taqqoslashlarni nima uchun parallel bajarish mumkin?
A: Ular bir-birini kesib oʻtmaydigan juftliklar bilan ishlaydi — har bir element faqat bitta taqqoslashda qatnashadi.
Q: Sigʻimi 5 boʻlgan halqasimon buferda `bosh` = 3 va `hajm` = 4 boʻlsa, yangi element qaysi indeksga yoziladi?
A: (3 + 4) % 5 = 2.
Q: Brauzerdagi „orqaga“ tugmasi qaysi tuzilma bilan ishlaydi?
A: Stek bilan: oxirgi ochilgan sahifa birinchi qaytariladi.
:::

:::link https://en.wikipedia.org/wiki/Batcher_odd%E2%80%93even_mergesort
Betcher juft-toq birlashtirib saralashi
Tarmoq qurilishi, uning hajmi va chuqurligi (ingliz tilida).
:::

:::link https://en.wikipedia.org/wiki/Circular_buffer
Halqasimon bufer
Massivda navbatni amalga oshirish usuli (ingliz tilida).
:::
