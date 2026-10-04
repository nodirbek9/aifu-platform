---
title: Daraxtlar — maʼlumotlar tuzilmasi sifatida. Binar va muvozanatlashtirilgan daraxtlar. Daraxt yordamida saralash
summary: Daraxt tushunchasi — ildiz, tugun, barg, balandlik; yoʻnaltirilgan, tartiblangan va binar daraxtlar; binar qidiruv daraxti — qoʻshish, qidirish, aylanish usullari (toʻgʻri, simmetrik, teskari, sathma-sath); muvozanat va AVL, qizil-qora daraxtlar; daraxt yordamida saralash.
---

## Nima uchun bu mavzu muhim?

Kompyuterdagi papkalar, HTML sahifa tuzilishi, tashkilot tuzilmasi, oilaviy shajara — bularning hammasi **daraxt**. Daraxt maʼlumotni ierarxik saqlaydi va, toʻgʻri qurilsa, qidirish, qoʻshish va oʻchirishni *O*(log *n*) da bajaradi — saralangan massiv kabi tez qidiradi, lekin roʻyxat kabi tez oʻzgaradi.

Maʼlumotlar bazalari indekslari (B-daraxtlar), Java `TreeMap` va C++ `map` (qizil-qora daraxtlar), kompilyatorlardagi sintaktik daraxtlar — bu maʼruza mavzusining amaldagi koʻrinishlari.

:::note
**Bu darsdan soʻng siz:**

- daraxt elementlarini (ildiz, tugun, barg, balandlik) va turlarini tushuntira olasiz;
- binar qidiruv daraxtiga element qoʻshish va qidirishni yoza olasiz;
- daraxtni toʻgʻri, simmetrik, teskari va sathma-sath aylanib chiqa olasiz;
- muvozanat nima uchun muhimligini va muvozanatlashtirilgan daraxtlar gʻoyasini tushuntira olasiz;
- daraxt yordamida saralashni bajara olasiz.
:::

## Daraxt tushunchasi

**Daraxt** — sikllari boʻlmagan bogʻlamli graf; uning bitta uchi **ildiz** deb tanlansa, qolgan har bir tugunning yagona **otasi** boʻladi.

| Tushuncha | Maʼnosi |
|---|---|
| Ildiz | otasi yoʻq yagona tugun |
| Barg | bolalari yoʻq tugun |
| Tugun chuqurligi | ildizdan unga qadar qirralar soni |
| Daraxt balandligi | sathlar soni (eng chuqur bargning chuqurligi + 1) |
| Qism daraxt | tugun va uning barcha avlodlari |

**Yoʻnaltirilgan daraxt** — qirralar ildizdan bolalarga yoʻnaltirilgan. **Tartiblangan daraxt** — bolalar tartibi muhim (birinchi, ikkinchi, …). **Binar daraxt** — har bir tugunda koʻpi bilan ikki bola: chap va oʻng.

## Binar qidiruv daraxti

**Binar qidiruv daraxti** (BQD, binary search tree) — har bir tugun uchun: chap qism daraxtdagi barcha kalitlar undan **kichik**, oʻng qism daraxtdagilar esa **katta yoki teng**.

- **Qidirish**: ildizdan boshlab, kalit kichik boʻlsa chapga, katta boʻlsa oʻngga — binar izlash kabi.
- **Qoʻshish**: xuddi qidirishdek yurib, boʻsh joyga yangi barg qoʻyiladi.
- **Murakkablik**: *O*(*h*), bunda *h* — balandlik. Muvozanatli daraxtda *h* ≈ log₂ *n*, lekin elementlar **saralangan** tartibda qoʻshilsa, daraxt bitta „shoxga“ aylanadi va *h* = *n*.

## Daraxtni aylanish

| Usul | Tartib | Qoʻllanilishi |
|---|---|---|
| Toʻgʻri (preorder) | tugun → chap → oʻng | daraxtni nusxalash, ifoda prefiks yozuvi |
| Simmetrik (inorder) | chap → tugun → oʻng | BQD da kalitlar **saralangan** tartibda chiqadi |
| Teskari (postorder) | chap → oʻng → tugun | daraxtni oʻchirish, papka hajmini hisoblash |
| Sathma-sath (level order) | ildizdan pastga, har sath chapdan oʻngga | navbat yordamida; eng yaqin tugunni topish |

## Muvozanatlashtirilgan daraxtlar

Daraxt **muvozanatli** — har bir tugunda chap va oʻng qism daraxtlar balandligi deyarli teng. Shunda *h* = *O*(log *n*) va barcha amallar tez. Muvozanatni avtomatik saqlovchi daraxtlar har qoʻshish va oʻchirishdan keyin **burilishlar** (rotations) bajaradi:

| Daraxt | Muvozanat sharti | Qayerda |
|---|---|---|
| **AVL** (Adelson-Velskiy, Landis, 1962) | har tugunda qism daraxtlar balandligi farqi ≤ 1 | qidiruv koʻp boʻlgan tizimlar |
| **Qizil-qora** | tugunlar rangi qoidalari; eng uzun yoʻl eng qisqasidan ≤ 2 baravar | Java `TreeMap`, C++ `map`/`set` |
| **B-daraxt** | koʻp bolali, barcha barglar bir sathda | maʼlumotlar bazasi va fayl tizimi indekslari |

## Daraxt yordamida saralash

**Daraxtli saralash** (tree sort): barcha elementlarni BQD ga qoʻshib, keyin simmetrik aylanish bilan chiqarish. Muvozanatli daraxt bilan *O*(*n* log *n*); oddiy BQD bilan eng yomon holatda (saralangan kirish) *O*(*n*²). Afzalligi — elementlar qoʻshilgan sari tartib saqlanadi va istalgan vaqtda saralangan roʻyxatni olish mumkin.

## Ishlangan misol 1: BQD va daraxtli saralash

Birinchi satrda *n*, ikkinchisida *n* ta son. Ularni berilgan tartibda BQD ga qoʻshing (teng kalit oʻngga), simmetrik aylanish bilan saralangan ketma-ketlikni va daraxt balandligini chiqaring.

```python
import sys

sys.setrecursionlimit(10000)


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(tugun, kalit):
    if tugun is None:
        return Tugun(kalit)                 # boʻsh joy — yangi barg
    if kalit < tugun.kalit:
        tugun.chap = qosh(tugun.chap, kalit)
    else:
        tugun.ong = qosh(tugun.ong, kalit)
    return tugun


def simmetrik(tugun, natija):               # chap → tugun → oʻng
    if tugun is not None:
        simmetrik(tugun.chap, natija)
        natija.append(tugun.kalit)
        simmetrik(tugun.ong, natija)


def balandlik(tugun):
    if tugun is None:
        return 0
    return 1 + max(balandlik(tugun.chap), balandlik(tugun.ong))


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
natija = []
simmetrik(ildiz, natija)
print(*natija)
print(balandlik(ildiz))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun tugun, int kalit) {
        if (tugun == null) return new Tugun(kalit);         // boʻsh joy — yangi barg
        if (kalit < tugun.kalit) tugun.chap = qosh(tugun.chap, kalit);
        else tugun.ong = qosh(tugun.ong, kalit);
        return tugun;
    }

    static void simmetrik(Tugun tugun, StringBuilder natija) {   // chap → tugun → oʻng
        if (tugun == null) return;
        simmetrik(tugun.chap, natija);
        if (natija.length() > 0) natija.append(' ');
        natija.append(tugun.kalit);
        simmetrik(tugun.ong, natija);
    }

    static int balandlik(Tugun tugun) {
        if (tugun == null) return 0;
        return 1 + Math.max(balandlik(tugun.chap), balandlik(tugun.ong));
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        StringBuilder natija = new StringBuilder();
        simmetrik(ildiz, natija);
        System.out.println(natija);
        System.out.println(balandlik(ildiz));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* tugun, int kalit) {
    if (tugun == nullptr) return new Tugun{kalit};          // boʻsh joy — yangi barg
    if (kalit < tugun->kalit) tugun->chap = qosh(tugun->chap, kalit);
    else tugun->ong = qosh(tugun->ong, kalit);
    return tugun;
}

void simmetrik(Tugun* tugun, vector<int>& natija) {         // chap → tugun → oʻng
    if (tugun == nullptr) return;
    simmetrik(tugun->chap, natija);
    natija.push_back(tugun->kalit);
    simmetrik(tugun->ong, natija);
}

int balandlik(Tugun* tugun) {
    if (tugun == nullptr) return 0;
    return 1 + max(balandlik(tugun->chap), balandlik(tugun->ong));
}

void ochir(Tugun* tugun) {                                  // teskari aylanish bilan boʻshatish
    if (tugun == nullptr) return;
    ochir(tugun->chap);
    ochir(tugun->ong);
    delete tugun;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> natija;
    simmetrik(ildiz, natija);
    for (size_t i = 0; i < natija.size(); i++) cout << (i ? " " : "") << natija[i];
    cout << endl << balandlik(ildiz) << endl;
    ochir(ildiz);
    return 0;
}
```

`7` va `50 30 70 20 40 60 80` uchun chiqish `20 30 40 50 60 70 80` va `3`: ideal muvozanatli daraxt (ildiz 50, keyin 30 va 70, keyin toʻrtta barg). `5` va `1 2 3 4 5` uchun esa saralangan natija bilan birga balandlik `5` — har bir yangi element oʻngga qoʻshildi va daraxt roʻyxatga aylandi. C++ versiyasi oxirida tugunlarni **teskari** aylanish bilan boʻshatadi: bolalar otasidan oldin oʻchirilishi kerak.

## Ishlangan misol 2: toʻgʻri, teskari va sathma-sath aylanish

Birinchi satrda *n*, ikkinchisida *n* ta turli son. Ularni BQD ga qoʻshing va daraxtni uchta usulda chiqaring: toʻgʻri, teskari va sathma-sath.

```python
from collections import deque


class Tugun:
    def __init__(self, kalit):
        self.kalit = kalit
        self.chap = None
        self.ong = None


def qosh(ildiz, kalit):                     # siklli qoʻshish — rekursiyasiz
    yangi = Tugun(kalit)
    if ildiz is None:
        return yangi
    joriy = ildiz
    while True:
        if kalit < joriy.kalit:
            if joriy.chap is None:
                joriy.chap = yangi
                return ildiz
            joriy = joriy.chap
        else:
            if joriy.ong is None:
                joriy.ong = yangi
                return ildiz
            joriy = joriy.ong


def togri(t, r):                            # tugun → chap → oʻng
    if t:
        r.append(t.kalit)
        togri(t.chap, r)
        togri(t.ong, r)


def teskari(t, r):                          # chap → oʻng → tugun
    if t:
        teskari(t.chap, r)
        teskari(t.ong, r)
        r.append(t.kalit)


def sathma_sath(ildiz):                     # navbat yordamida
    r = []
    navbat = deque([ildiz] if ildiz else [])
    while navbat:
        t = navbat.popleft()
        r.append(t.kalit)
        if t.chap:
            navbat.append(t.chap)
        if t.ong:
            navbat.append(t.ong)
    return r


n = int(input())
ildiz = None
for x in map(int, input().split()):
    ildiz = qosh(ildiz, x)
r = []
togri(ildiz, r)
print(*r)
r = []
teskari(ildiz, r)
print(*r)
print(*sathma_sath(ildiz))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static class Tugun {
        int kalit;
        Tugun chap, ong;
        Tugun(int kalit) { this.kalit = kalit; }
    }

    static Tugun qosh(Tugun ildiz, int kalit) {             // siklli qoʻshish — rekursiyasiz
        Tugun yangi = new Tugun(kalit);
        if (ildiz == null) return yangi;
        Tugun joriy = ildiz;
        while (true) {
            if (kalit < joriy.kalit) {
                if (joriy.chap == null) { joriy.chap = yangi; return ildiz; }
                joriy = joriy.chap;
            } else {
                if (joriy.ong == null) { joriy.ong = yangi; return ildiz; }
                joriy = joriy.ong;
            }
        }
    }

    static void togri(Tugun t, List<Integer> r) {           // tugun → chap → oʻng
        if (t == null) return;
        r.add(t.kalit);
        togri(t.chap, r);
        togri(t.ong, r);
    }

    static void teskari(Tugun t, List<Integer> r) {         // chap → oʻng → tugun
        if (t == null) return;
        teskari(t.chap, r);
        teskari(t.ong, r);
        r.add(t.kalit);
    }

    static List<Integer> sathmaSath(Tugun ildiz) {         // navbat yordamida
        List<Integer> r = new ArrayList<>();
        ArrayDeque<Tugun> navbat = new ArrayDeque<>();
        if (ildiz != null) navbat.add(ildiz);
        while (!navbat.isEmpty()) {
            Tugun t = navbat.poll();
            r.add(t.kalit);
            if (t.chap != null) navbat.add(t.chap);
            if (t.ong != null) navbat.add(t.ong);
        }
        return r;
    }

    static String satr(List<Integer> r) {
        StringBuilder sb = new StringBuilder();
        for (int x : r) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(x);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Tugun ildiz = null;
        for (int i = 0; i < n; i++) ildiz = qosh(ildiz, in.nextInt());
        List<Integer> r = new ArrayList<>();
        togri(ildiz, r);
        System.out.println(satr(r));
        r = new ArrayList<>();
        teskari(ildiz, r);
        System.out.println(satr(r));
        System.out.println(satr(sathmaSath(ildiz)));
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <vector>
using namespace std;

struct Tugun {
    int kalit;
    Tugun* chap = nullptr;
    Tugun* ong = nullptr;
};

Tugun* qosh(Tugun* ildiz, int kalit) {                      // siklli qoʻshish — rekursiyasiz
    Tugun* yangi = new Tugun{kalit};
    if (ildiz == nullptr) return yangi;
    Tugun* joriy = ildiz;
    while (true) {
        Tugun*& keyingi = kalit < joriy->kalit ? joriy->chap : joriy->ong;
        if (keyingi == nullptr) {
            keyingi = yangi;
            return ildiz;
        }
        joriy = keyingi;
    }
}

void togri(Tugun* t, vector<int>& r) {                      // tugun → chap → oʻng
    if (!t) return;
    r.push_back(t->kalit);
    togri(t->chap, r);
    togri(t->ong, r);
}

void teskari(Tugun* t, vector<int>& r) {                    // chap → oʻng → tugun
    if (!t) return;
    teskari(t->chap, r);
    teskari(t->ong, r);
    r.push_back(t->kalit);
}

vector<int> sathmaSath(Tugun* ildiz) {                      // navbat yordamida
    vector<int> r;
    queue<Tugun*> navbat;
    if (ildiz) navbat.push(ildiz);
    while (!navbat.empty()) {
        Tugun* t = navbat.front();
        navbat.pop();
        r.push_back(t->kalit);
        if (t->chap) navbat.push(t->chap);
        if (t->ong) navbat.push(t->ong);
    }
    return r;
}

void chiqar(const vector<int>& r) {
    for (size_t i = 0; i < r.size(); i++) cout << (i ? " " : "") << r[i];
    cout << endl;
}

int main() {
    int n;
    cin >> n;
    Tugun* ildiz = nullptr;
    for (int i = 0; i < n; i++) {
        int x;
        cin >> x;
        ildiz = qosh(ildiz, x);
    }
    vector<int> r;
    togri(ildiz, r);
    chiqar(r);
    r.clear();
    teskari(ildiz, r);
    chiqar(r);
    chiqar(sathmaSath(ildiz));
    return 0;
}
```

`7` va `50 30 70 20 40 60 80` uchun chiqish:

```text
50 30 20 40 70 60 80
20 40 30 60 80 70 50
50 30 70 20 40 60 80
```

Toʻgʻri aylanishda ildiz birinchi, teskarida oxirgi chiqadi. Sathma-sath aylanish rekursiya emas, **navbat** bilan ishlaydi: tugun chiqarilganda uning bolalari navbat oxiriga qoʻyiladi, shuning uchun yuqori sath pastkisidan oldin chiqadi. Qoʻshish bu safar sikl bilan yozilgan — saralangan kirishda chuqur rekursiya stekni toʻldirmasligi uchun. C++'dagi `Tugun*& keyingi` — koʻrsatkichga havola: u orqali `chap` yoki `ong` maydonining oʻziga yozish mumkin.

## Ishlangan misol 3: muvozanatli va muvozanatsiz daraxt balandligi

*n* kiritiladi (1 ≤ *n* ≤ 10⁵). 1, 2, …, *n* sonlaridan ikkita daraxt quring: (1) sonlarni ketma-ket BQD ga qoʻshib; (2) saralangan massivdan **oʻrtadagi elementni ildiz** qilib, chap va oʻng yarimlardan rekursiv. Ikkala daraxt balandligini chiqaring. Birinchi daraxt balandligini qurmasdan hisoblang — u zanjir boʻlishini isbotlash uchun tahlil yetarli.

```python
import sys

sys.setrecursionlimit(10000)


def muvozanatli_balandlik(chap, ong):
    """[chap, ong] dan oʻrtani ildiz qilib qurilgan daraxt balandligi."""
    if chap > ong:
        return 0
    orta = (chap + ong) // 2                # oʻrtadagi element — ildiz
    return 1 + max(muvozanatli_balandlik(chap, orta - 1),
                   muvozanatli_balandlik(orta + 1, ong))


n = int(input())
ketma_ket = n                               # har bir son oʻngga — zanjir
print(ketma_ket, muvozanatli_balandlik(1, n))
```
```java
import java.util.Scanner;

public class Main {
    /** [chap, ong] dan oʻrtani ildiz qilib qurilgan daraxt balandligi. */
    static int muvozanatliBalandlik(int chap, int ong) {
        if (chap > ong) return 0;
        int orta = (chap + ong) / 2;                // oʻrtadagi element — ildiz
        return 1 + Math.max(muvozanatliBalandlik(chap, orta - 1),
                            muvozanatliBalandlik(orta + 1, ong));
    }

    public static void main(String[] args) {
        int n = new Scanner(System.in).nextInt();
        int ketmaKet = n;                           // har bir son oʻngga — zanjir
        System.out.println(ketmaKet + " " + muvozanatliBalandlik(1, n));
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

// [chap, ong] dan oʻrtani ildiz qilib qurilgan daraxt balandligi
int muvozanatliBalandlik(int chap, int ong) {
    if (chap > ong) return 0;
    int orta = (chap + ong) / 2;                    // oʻrtadagi element — ildiz
    return 1 + max(muvozanatliBalandlik(chap, orta - 1),
                   muvozanatliBalandlik(orta + 1, ong));
}

int main() {
    int n;
    cin >> n;
    int ketmaKet = n;                               // har bir son oʻngga — zanjir
    cout << ketmaKet << " " << muvozanatliBalandlik(1, n) << endl;
    return 0;
}
```

| *n* | Ketma-ket qoʻshilgan | Muvozanatli |
|---|---|---|
| 7 | 7 | 3 |
| 1000 | 1000 | 10 |
| 100 000 | 100 000 | 17 |

1, 2, …, *n* ketma-ket qoʻshilganda har bir yangi son avvalgilarning hammasidan katta — u oʻngdagi oxirgi tugunga ulanadi va daraxt zanjirga aylanadi: balandlik *n*, qidiruv *O*(*n*). Xuddi shu sonlardan oʻrtani ildiz qilib qurilgan daraxt balandligi ⌈log₂(*n* + 1)⌉: 100 000 element uchun atigi 17. AVL va qizil-qora daraxtlar sonlar qanday tartibda kelishidan qatʼi nazar shunday balandlikni avtomatik saqlaydi. Muvozanatli qurishda rekursiya chuqurligi ham log *n* — shuning uchun *n* = 10⁵ da ham xavfsiz.

:::warning
**Koʻp uchraydigan xatolar**

- **Saralangan maʼlumotni oddiy BQD ga qoʻshish.** Daraxt zanjirga aylanadi; muvozanatli daraxt (`TreeMap`, `map`) yoki tasodifiy tartib ishlating.
- **Rekursiv qoʻshish va aylanishda stek toʻlishi.** Zanjir daraxtda chuqurlik *n* ga teng; siklli qoʻshish yoki muvozanatli daraxt kerak.
- **Teng kalitlar uchun qoida yoʻqligi.** Teng kalit doim bir tomonga (masalan, oʻngga) ketishi kerak, aks holda qidiruv ularni topa olmaydi.
- **C++'da daraxtni boʻshatmaslik.** Har bir `new Tugun` ga `delete` kerak; buni teskari aylanish bilan bajaring.
:::

:::tip
BQD bilan ishlashda daraxtni qogʻozda chizing va har bir qoʻshilgan elementni ildizdan boshlab „chapmi, oʻngmi?“ deb yuritib chiqing. Simmetrik aylanish natijasi saralangan boʻlmasa, qoʻshishda xato bor.
:::

## Xulosa

- Daraxt — sikllarsiz bogʻlamli graf: ildiz, tugunlar, barglar; balandlik — sathlar soni.
- BQD: chapda kichik, oʻngda katta kalitlar; qidirish va qoʻshish *O*(*h*).
- Aylanishlar: toʻgʻri, simmetrik (BQD da saralangan tartib), teskari va sathma-sath (navbat bilan).
- Muvozanatsiz BQD saralangan kirishda zanjirga aylanadi (*h* = *n*); AVL, qizil-qora va B-daraxtlar *h* = *O*(log *n*) ni kafolatlaydi.
- Daraxtli saralash: elementlarni qoʻshib, simmetrik aylanish — muvozanatli daraxt bilan *O*(*n* log *n*).

## Oʻzingizni tekshiring

:::check
Q: BQD ning qaysi aylanishi kalitlarni saralangan tartibda beradi?
A: Simmetrik (inorder): chap → tugun → oʻng.
Q: 1, 2, 3, 4 ketma-ket BQD ga qoʻshilsa, daraxt balandligi qancha?
A: 4 — har bir element oʻngga qoʻshilib, zanjir hosil boʻladi.
Q: Sathma-sath aylanish uchun qaysi tuzilma kerak?
A: Navbat.
Q: Nima uchun Java `TreeMap` da qidiruv har doim *O*(log *n*)?
A: U qizil-qora daraxt — har qoʻshish va oʻchirishdan keyin muvozanatni burilishlar bilan tiklaydi.
:::

:::link https://visualgo.net/en/bst
VisuAlgo: binar qidiruv daraxti
BQD va AVL daraxtining animatsiyasi (ingliz tilida).
:::
