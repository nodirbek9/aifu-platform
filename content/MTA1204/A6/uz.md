---
title: Maʼlumotlarning dinamik tuzilmalari. Navbat, stek, dek. Roʻyxatlar (amaliy mashgʻulot)
summary: Stek bilan postfiks (teskari polyak) ifodani hisoblash, dek bilan sirpanuvchi oynadagi maksimum (monoton dek), ikki bogʻlamli roʻyxat bilan matn muharriri kursori; tuzilmani masalaning asosiy amaliga qarab tanlash.
---

## Nima uchun bu mavzu muhim?

Stek, navbat va dek — oddiy, lekin juda kuchli tuzilmalar. Kalkulyator ifodani stek bilan hisoblaydi, birja grafigi oxirgi daqiqalardagi eng yuqori narxni dek bilan topadi, matn muharriri esa kursor atrofida harf qoʻshish va oʻchirishni bogʻlangan roʻyxat gʻoyasi bilan tez bajaradi. Bu mashgʻulotda shu uchta amaliy masalani yechamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- postfiks ifodani stek yordamida hisoblay olasiz;
- dekni „monoton“ holatda saqlab, sirpanuvchi oyna maksimumini *O*(*n*) da topa olasiz;
- ikki bogʻlamli roʻyxatda tugun qoʻshish va oʻchirishni koʻrsatkichlar bilan bajara olasiz;
- masalaning asosiy amaliga qarab mos tuzilmani tanlay olasiz.
:::

## Postfiks yozuv va stek

Odatdagi (infiks) yozuvda `(3 + 4) * 2` qavslar va amallar ustuvorligini talab qiladi. **Postfiks** (teskari polyak) yozuvda amal operandlardan **keyin** yoziladi: `3 4 + 2 *`. Unda qavs ham, ustuvorlik ham kerak emas, hisoblash esa stek bilan juda oddiy:

```text
har bir belgi uchun:
    son boʻlsa → stekka qoʻy
    amal boʻlsa → ikkita sonni ol (avval b, keyin a), a amal b ni stekka qoʻy
oxirida stekda bitta son — javob
```

Kompilyatorlar va kalkulyatorlar ifodani avval postfiks shaklga keltirib (Deykstraning „saralash stansiyasi“ algoritmi), keyin aynan shunday hisoblaydi.

## Monoton dek

**Sirpanuvchi oyna maksimumi**: *k* uzunlikdagi oyna massiv boʻylab siljiydi; har bir holat uchun oynadagi eng katta elementni topish kerak. Har safar oynani qayta koʻrib chiqish — *O*(*n* · *k*). Dek bilan — *O*(*n*):

- dekda **indekslar** saqlanadi, ularning qiymatlari boshidan oxiriga **kamayib** boradi;
- yangi element kelganda, oxiridan undan kichik yoki teng qiymatlilar olib tashlanadi — ular endi hech qachon maksimum boʻlmaydi;
- boshidagi indeks oynadan chiqib ketgan boʻlsa, u boshidan olib tashlanadi;
- dek boshi — joriy oyna maksimumi.

Har bir indeks dekka bir marta kiradi va bir marta chiqadi — jami *O*(*n*).

## Ikki bogʻlamli roʻyxat

Ikki bogʻlamli roʻyxatda har bir tugun `oldingi` va `keyingi` koʻrsatkichlarini saqlaydi. Maʼlum tugundan oldin yoki keyin qoʻshish va tugunni oʻchirish — faqat bir nechta koʻrsatkichni almashtirish, *O*(1). **Qoʻriqchi tugun** (sentinel) — roʻyxat boshidagi soxta tugun: u boʻsh roʻyxat va „boshni oʻchirish“ kabi alohida holatlarni yoʻqotadi.

## Ishlangan misol 1: postfiks ifodani hisoblash

Bitta satrda boʻsh joy bilan ajratilgan postfiks ifoda: butun sonlar va `+`, `-`, `*` amallari. Ifoda qiymatini chiqaring.

```python
belgilar = input().split()
stek = []
for b in belgilar:
    if b in ("+", "-", "*"):
        ikkinchi = stek.pop()           # avval oʻng operand
        birinchi = stek.pop()
        if b == "+":
            stek.append(birinchi + ikkinchi)
        elif b == "-":
            stek.append(birinchi - ikkinchi)
        else:
            stek.append(birinchi * ikkinchi)
    else:
        stek.append(int(b))             # son — stekka
print(stek.pop())
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String[] belgilar = new Scanner(System.in).nextLine().trim().split("\\s+");
        Deque<Long> stek = new ArrayDeque<>();
        for (String b : belgilar) {
            if (b.equals("+") || b.equals("-") || b.equals("*")) {
                long ikkinchi = stek.pop();     // avval oʻng operand
                long birinchi = stek.pop();
                if (b.equals("+")) stek.push(birinchi + ikkinchi);
                else if (b.equals("-")) stek.push(birinchi - ikkinchi);
                else stek.push(birinchi * ikkinchi);
            } else {
                stek.push(Long.parseLong(b));   // son — stekka
            }
        }
        System.out.println(stek.pop());
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <stack>
#include <string>
using namespace std;

int main() {
    string satr, b;
    getline(cin, satr);
    istringstream oqim(satr);
    stack<long long> stek;
    while (oqim >> b) {
        if (b == "+" || b == "-" || b == "*") {
            long long ikkinchi = stek.top();    // avval oʻng operand
            stek.pop();
            long long birinchi = stek.top();
            stek.pop();
            if (b == "+") stek.push(birinchi + ikkinchi);
            else if (b == "-") stek.push(birinchi - ikkinchi);
            else stek.push(birinchi * ikkinchi);
        } else {
            stek.push(stoll(b));                // son — stekka
        }
    }
    cout << stek.top() << endl;
    return 0;
}
```

`3 4 + 2 *` uchun `14` ((3 + 4) · 2); `5 1 2 + 4 * + 3 -` uchun ham `14` (5 + (1 + 2) · 4 − 3); `2 7 -` uchun `-5`. Ayirishda operandlar tartibi muhim: stekdan birinchi olingan son — **oʻng** operand. `-5` kabi manfiy son ham belgi sifatida keladi va amal deb hisoblanmaydi, chunki u aynan `-` ga teng emas.

## Ishlangan misol 2: sirpanuvchi oyna maksimumi

Birinchi satrda *n* va *k*, ikkinchisida *n* ta son. *k* uzunlikdagi har bir ketma-ket oynaning maksimumini chiqaring (jami *n* − *k* + 1 ta son).

```python
from collections import deque

n, k = map(int, input().split())
a = list(map(int, input().split()))
dek = deque()                           # indekslar; qiymatlari kamayib boradi
natija = []
for i in range(n):
    while dek and a[dek[-1]] <= a[i]:   # endi hech qachon maksimum boʻlmaydi
        dek.pop()
    dek.append(i)
    if dek[0] <= i - k:                 # oynadan chiqib ketdi
        dek.popleft()
    if i >= k - 1:
        natija.append(a[dek[0]])        # dek boshi — maksimum
print(*natija)
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Deque<Integer> dek = new ArrayDeque<>();            // indekslar; qiymatlari kamayib boradi
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            while (!dek.isEmpty() && a[dek.peekLast()] <= a[i]) dek.pollLast();   // endi hech qachon maksimum boʻlmaydi
            dek.addLast(i);
            if (dek.peekFirst() <= i - k) dek.pollFirst();  // oynadan chiqib ketdi
            if (i >= k - 1) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(a[dek.peekFirst()]);              // dek boshi — maksimum
            }
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <deque>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    deque<int> dek;                                         // indekslar; qiymatlari kamayib boradi
    bool birinchi = true;
    for (int i = 0; i < n; i++) {
        while (!dek.empty() && a[dek.back()] <= a[i]) dek.pop_back();   // endi hech qachon maksimum boʻlmaydi
        dek.push_back(i);
        if (dek.front() <= i - k) dek.pop_front();          // oynadan chiqib ketdi
        if (i >= k - 1) {
            cout << (birinchi ? "" : " ") << a[dek.front()];    // dek boshi — maksimum
            birinchi = false;
        }
    }
    cout << endl;
    return 0;
}
```

`8 3` va `1 3 -1 -3 5 3 6 7` uchun chiqish `3 3 5 5 6 7`. Masalan, `5` kelganda dekdagi `-1` va `-3` olib tashlanadi: ular `5` dan chapda va kichik — `5` oynada turganda ular hech qachon maksimum boʻla olmaydi. Dekda **qiymatlar emas, indekslar** saqlanadi — element oynadan chiqqanini indeks boʻyicha bilamiz.

## Ishlangan misol 3: ikki bogʻlamli roʻyxatda kursor

Matn muharriri: boshida matn boʻsh, kursor oxirida. Birinchi satrda *q*, keyin buyruqlar: `yoz C` — kursor oldiga *C* belgisini qoʻshish, `chap` / `ong` — kursorni bir belgiga surish (chetda boʻlsa joyida qoladi), `ochir` — kursordan oldingi belgini oʻchirish (boʻlsa). Oxirida matnni chiqaring. Matn ikki bogʻlamli roʻyxatda saqlanadi, kursor — chapidagi tugunga koʻrsatkich.

```python
class Tugun:
    def __init__(self, belgi):
        self.belgi = belgi
        self.oldingi = None
        self.keyingi = None


qoriqchi = Tugun("")                        # soxta bosh tugun — alohida holatlar yoʻq
kursor = qoriqchi                           # kursor chapidagi tugun

q = int(input())
for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "yoz":
        t = Tugun(buyruq[1])                # kursor va keyingisi orasiga
        t.oldingi = kursor
        t.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = t
        kursor.keyingi = t
        kursor = t
    elif buyruq[0] == "chap":
        if kursor is not qoriqchi:
            kursor = kursor.oldingi
    elif buyruq[0] == "ong":
        if kursor.keyingi:
            kursor = kursor.keyingi
    elif kursor is not qoriqchi:            # ochir: kursor chapidagi tugunni olib tashlash
        kursor.oldingi.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = kursor.oldingi
        kursor = kursor.oldingi

belgilar = []
t = qoriqchi.keyingi
while t:
    belgilar.append(t.belgi)
    t = t.keyingi
print("".join(belgilar))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        char belgi;
        Tugun oldingi, keyingi;
        Tugun(char belgi) { this.belgi = belgi; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun qoriqchi = new Tugun(' ');                    // soxta bosh tugun — alohida holatlar yoʻq
        Tugun kursor = qoriqchi;                            // kursor chapidagi tugun

        int q = in.nextInt();
        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("yoz")) {
                Tugun t = new Tugun(in.next().charAt(0));   // kursor va keyingisi orasiga
                t.oldingi = kursor;
                t.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = t;
                kursor.keyingi = t;
                kursor = t;
            } else if (buyruq.equals("chap")) {
                if (kursor != qoriqchi) kursor = kursor.oldingi;
            } else if (buyruq.equals("ong")) {
                if (kursor.keyingi != null) kursor = kursor.keyingi;
            } else if (kursor != qoriqchi) {                // ochir: kursor chapidagi tugunni olib tashlash
                kursor.oldingi.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = kursor.oldingi;
                kursor = kursor.oldingi;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Tugun t = qoriqchi.keyingi; t != null; t = t.keyingi) sb.append(t.belgi);
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

struct Tugun {
    char belgi;
    Tugun* oldingi = nullptr;
    Tugun* keyingi = nullptr;
};

int main() {
    Tugun* qoriqchi = new Tugun{' '};                       // soxta bosh tugun — alohida holatlar yoʻq
    Tugun* kursor = qoriqchi;                               // kursor chapidagi tugun

    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "yoz") {
            char c;
            cin >> c;
            Tugun* t = new Tugun{c};                        // kursor va keyingisi orasiga
            t->oldingi = kursor;
            t->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = t;
            kursor->keyingi = t;
            kursor = t;
        } else if (buyruq == "chap") {
            if (kursor != qoriqchi) kursor = kursor->oldingi;
        } else if (buyruq == "ong") {
            if (kursor->keyingi) kursor = kursor->keyingi;
        } else if (kursor != qoriqchi) {                    // ochir: kursor chapidagi tugunni olib tashlash
            Tugun* eski = kursor;
            kursor->oldingi->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = kursor->oldingi;
            kursor = kursor->oldingi;
            delete eski;
        }
    }

    for (Tugun* t = qoriqchi->keyingi; t; t = t->keyingi) cout << t->belgi;
    cout << endl;
    while (qoriqchi) {
        Tugun* keyingi = qoriqchi->keyingi;
        delete qoriqchi;
        qoriqchi = keyingi;
    }
    return 0;
}
```

Kirish:

```text
8
yoz a
yoz b
yoz c
chap
ochir
yoz x
ong
yoz d
```

Chiqish:

```text
axcd
```

`abc` yozilgandan keyin kursor `c` ning oʻngida; `chap` uni `b` va `c` orasiga suradi, `ochir` esa `b` ni oʻchiradi. Har bir buyruq bir nechta koʻrsatkichni almashtiradi — matn uzunligidan qatʼi nazar *O*(1). Massivda esa oʻrtaga yozish va oʻchirish har safar qolgan belgilarni surishni talab qilardi. Qoʻriqchi tugun tufayli „matn boshida turibmiz“ holati oddiy `kursor == qoriqchi` tekshiruviga aylandi.

:::warning
**Koʻp uchraydigan xatolar**

- **Postfiksda operandlar tartibini almashtirish.** `a b -` da stekdan avval `b` chiqadi; `b − a` emas, `a − b` hisoblanadi.
- **Monoton dekda qiymat saqlash.** Element oynadan chiqqanini bilish uchun indeks kerak.
- **Ikki bogʻlamli roʻyxatda bitta koʻrsatkichni yangilashni unutish.** Qoʻshishda toʻrtta, oʻchirishda ikkita koʻrsatkich oʻzgaradi; bittasi qolsa, roʻyxat „uziladi“.
- **Boʻsh stekdan olish.** Notoʻgʻri ifodada stek boʻshab qolishi mumkin; haqiqiy dasturda buni tekshiring.
:::

:::tip
Koʻrsatkichli tuzilmalarni qogʻozda tugunlarni toʻrtburchak, koʻrsatkichlarni strelka qilib chizing. Har bir amaldan oldin va keyin rasmni solishtirsangiz, qaysi strelka yangilanmaganini darhol koʻrasiz.
:::

## Xulosa

- Stek postfiks ifodani qavssiz va ustuvorliksiz hisoblash imkonini beradi.
- Monoton dek sirpanuvchi oyna maksimumini *O*(*n*) da topadi: har bir indeks bir marta kiradi va chiqadi.
- Ikki bogʻlamli roʻyxatda maʼlum tugun atrofida qoʻshish va oʻchirish *O*(1); qoʻriqchi tugun alohida holatlarni yoʻqotadi.
- Tuzilma masalaning asosiy amaliga qarab tanlanadi: oxirgisini olish — stek, ikki uchdan ishlash — dek, oʻrtada oʻzgartirish — bogʻlangan roʻyxat.

## Oʻzingizni tekshiring

:::check
Q: `2 3 4 * +` postfiks ifodaning qiymati qancha?
A: 14: 2 + 3 · 4.
Q: Monoton dekda nima uchun kichik qiymatli indekslar oxiridan olib tashlanadi?
A: Ulardan oʻngda kattaroq element bor; u oynada turgan vaqtda ular hech qachon maksimum boʻla olmaydi.
Q: Sirpanuvchi oyna maksimumi monoton dek bilan qanday murakkablikda ishlaydi?
A: *O*(*n*) — har bir indeks dekka bir marta qoʻshiladi va koʻpi bilan bir marta olinadi.
Q: Qoʻriqchi tugun nima uchun kerak?
A: U roʻyxat hech qachon boʻsh boʻlmasligini taʼminlaydi va boshni oʻchirish kabi alohida holatlarni yoʻqotadi.
:::

:::link https://en.wikipedia.org/wiki/Reverse_Polish_notation
Teskari polyak yozuvi
Postfiks yozuv va uni stek bilan hisoblash (ingliz tilida).
:::
