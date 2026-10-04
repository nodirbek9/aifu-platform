---
title: Oʻzaro bogʻlangan sinflarni tashkil etish
summary: Mustaqil ish: sinflar orasidagi bogʻlanishlar — assotsiatsiya, agregatsiya va kompozitsiya; masʼuliyatni sinflarga taqsimlash, yopiq maydonlar va tekshiruvchi metodlar, buyruqlar oqimini qayta ishlash; namunaviy masala — kitob va kutubxona; topshiriq — bank hisobi sinfi.
---

## Nima uchun bu mavzu muhim?

Real dasturda sinflar yolgʻiz yashamaydi: kutubxonada kitoblar, guruhda talabalar, buyurtmada mahsulotlar bor. Har bir sinf **oʻz ishiga** javob beradi va boshqalar bilan faqat metodlar orqali gaplashadi. Masʼuliyatni sinflarga toʻgʻri taqsimlash — obyektga yoʻnaltirilgan loyihalashning asosiy koʻnikmasi. Bu mustaqil ishda ikki bogʻlangan sinfdan iborat kichik tizim quring va uni buyruqlar oqimi bilan boshqarasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- sinflar orasidagi bogʻlanish turlarini (assotsiatsiya, agregatsiya, kompozitsiya) farqlay olasiz;
- har bir sinfning masʼuliyatini aniqlab, maydon va metodlarni toʻgʻri sinfga joylay olasiz;
- obyekt holatini notoʻgʻri oʻzgarishdan himoya qiluvchi metodlar yoza olasiz;
- matnli buyruqlar oqimini qayta ishlovchi dastur tuza olasiz.
:::

## Mavzu boʻyicha qisqa takror

| Bogʻlanish | Maʼnosi | Misol |
|---|---|---|
| Assotsiatsiya | bir obyekt boshqasidan foydalanadi | oʻqituvchi kursni oʻqitadi |
| Agregatsiya | „…da … bor“, lekin qismlar mustaqil yashaydi | guruhda talabalar bor (guruh tarqalsa ham talabalar qoladi) |
| Kompozitsiya | „…da … bor“, qismlar butun bilan birga yaratiladi va yoʻqoladi | kitobda sahifalar bor |

Loyihalash qoidalari:

1. **Har bir maʼlumot bitta sinfda** saqlanadi: kitob nusxalari soni — `Kitob` da, kitoblar roʻyxati — `Kutubxona` da.
2. **Holatni faqat egasi oʻzgartiradi**: `Kutubxona` kitobning `berilgan` maydoniga toʻgʻridan-toʻgʻri yozmaydi, `kitob.ber()` ni chaqiradi.
3. **Metod natija qaytaradi**: amal bajarilmasa (`false`), buni chaqiruvchi hal qiladi — xabar chiqarish asosiy dasturning ishi.
4. Buyruqlar oqimi: har bir satr — buyruq va argumentlar; `if`/`elif` yoki `switch` bilan tegishli metod chaqiriladi.

## Namunaviy masala: kitob va kutubxona

Birinchi satrda *n*, keyingi *n* satrda buyruqlar:

- `qosh NOM SONI` — kutubxonaga kitobning SONI ta nusxasini qoʻshish (javob chiqarilmaydi);
- `ber NOM` — bitta nusxani berish: mumkin boʻlsa `berildi`, aks holda `yoʻq`;
- `qaytar NOM` — nusxani qabul qilish: shu kitobdan berilgan nusxa boʻlsa `qabul qilindi`, aks holda `xato`;
- `holat NOM` — `NOM: MAVJUD/JAMI` (kitob umuman boʻlmasa `NOM: 0/0`).

```python
class Kitob:
    def __init__(self, nom):
        self.nom = nom
        self._jami = 0
        self._berilgan = 0

    def nusxa_qosh(self, soni):
        self._jami += soni

    def ber(self):                          # holatni faqat Kitob oʻzgartiradi
        if self._berilgan == self._jami:
            return False
        self._berilgan += 1
        return True

    def qaytar(self):
        if self._berilgan == 0:
            return False
        self._berilgan -= 1
        return True

    def holat(self):
        return f"{self.nom}: {self._jami - self._berilgan}/{self._jami}"


class Kutubxona:
    def __init__(self):
        self._kitoblar = {}                 # agregatsiya: kutubxonada kitoblar bor

    def qosh(self, nom, soni):
        if nom not in self._kitoblar:
            self._kitoblar[nom] = Kitob(nom)
        self._kitoblar[nom].nusxa_qosh(soni)

    def ber(self, nom):
        return nom in self._kitoblar and self._kitoblar[nom].ber()

    def qaytar(self, nom):
        return nom in self._kitoblar and self._kitoblar[nom].qaytar()

    def holat(self, nom):
        if nom not in self._kitoblar:
            return f"{nom}: 0/0"
        return self._kitoblar[nom].holat()


kutubxona = Kutubxona()
n = int(input())
for _ in range(n):
    q = input().split()
    if q[0] == "qosh":
        kutubxona.qosh(q[1], int(q[2]))
    elif q[0] == "ber":
        print("berildi" if kutubxona.ber(q[1]) else "yoʻq")
    elif q[0] == "qaytar":
        print("qabul qilindi" if kutubxona.qaytar(q[1]) else "xato")
    else:
        print(kutubxona.holat(q[1]))
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static class Kitob {
        private final String nom;
        private int jami = 0, berilgan = 0;

        Kitob(String nom) { this.nom = nom; }

        void nusxaQosh(int soni) { jami += soni; }

        boolean ber() {                     // holatni faqat Kitob oʻzgartiradi
            if (berilgan == jami) return false;
            berilgan++;
            return true;
        }

        boolean qaytar() {
            if (berilgan == 0) return false;
            berilgan--;
            return true;
        }

        String holat() { return nom + ": " + (jami - berilgan) + "/" + jami; }
    }

    static class Kutubxona {
        private final Map<String, Kitob> kitoblar = new HashMap<>();   // agregatsiya

        void qosh(String nom, int soni) {
            kitoblar.computeIfAbsent(nom, Kitob::new).nusxaQosh(soni);
        }

        boolean ber(String nom) {
            Kitob k = kitoblar.get(nom);
            return k != null && k.ber();
        }

        boolean qaytar(String nom) {
            Kitob k = kitoblar.get(nom);
            return k != null && k.qaytar();
        }

        String holat(String nom) {
            Kitob k = kitoblar.get(nom);
            return k == null ? nom + ": 0/0" : k.holat();
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Kutubxona kutubxona = new Kutubxona();
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            String buyruq = in.next(), nom = in.next();
            switch (buyruq) {
                case "qosh" -> kutubxona.qosh(nom, in.nextInt());
                case "ber" -> System.out.println(kutubxona.ber(nom) ? "berildi" : "yoʻq");
                case "qaytar" -> System.out.println(kutubxona.qaytar(nom) ? "qabul qilindi" : "xato");
                default -> System.out.println(kutubxona.holat(nom));
            }
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

class Kitob {
    string nom;
    int jami = 0, berilgan = 0;

public:
    Kitob(string nom = "") : nom(nom) {}

    void nusxaQosh(int soni) { jami += soni; }

    bool ber() {                            // holatni faqat Kitob oʻzgartiradi
        if (berilgan == jami) return false;
        berilgan++;
        return true;
    }

    bool qaytar() {
        if (berilgan == 0) return false;
        berilgan--;
        return true;
    }

    string holat() const { return nom + ": " + to_string(jami - berilgan) + "/" + to_string(jami); }
};

class Kutubxona {
    map<string, Kitob> kitoblar;            // agregatsiya

public:
    void qosh(const string& nom, int soni) {
        if (!kitoblar.count(nom)) kitoblar[nom] = Kitob(nom);
        kitoblar[nom].nusxaQosh(soni);
    }

    bool ber(const string& nom) {
        auto it = kitoblar.find(nom);
        return it != kitoblar.end() && it->second.ber();
    }

    bool qaytar(const string& nom) {
        auto it = kitoblar.find(nom);
        return it != kitoblar.end() && it->second.qaytar();
    }

    string holat(const string& nom) const {
        auto it = kitoblar.find(nom);
        return it == kitoblar.end() ? nom + ": 0/0" : it->second.holat();
    }
};

int main() {
    Kutubxona kutubxona;
    int n;
    cin >> n;
    for (int i = 0; i < n; i++) {
        string buyruq, nom;
        cin >> buyruq >> nom;
        if (buyruq == "qosh") {
            int soni;
            cin >> soni;
            kutubxona.qosh(nom, soni);
        } else if (buyruq == "ber") {
            cout << (kutubxona.ber(nom) ? "berildi" : "yoʻq") << endl;
        } else if (buyruq == "qaytar") {
            cout << (kutubxona.qaytar(nom) ? "qabul qilindi" : "xato") << endl;
        } else {
            cout << kutubxona.holat(nom) << endl;
        }
    }
    return 0;
}
```

Kirish:

```text
8
qosh Sarob 2
ber Sarob
ber Sarob
ber Sarob
holat Sarob
qaytar Sarob
qaytar Alpomish
holat Alpomish
```

Chiqish:

```text
berildi
berildi
yoʻq
Sarob: 0/2
qabul qilindi
xato
Alpomish: 0/0
```

`Kutubxona` kitob nusxalarini oʻzi sanamaydi — u faqat kerakli `Kitob` ni topadi va uning metodini chaqiradi. `Kitob` esa xabar chiqarmaydi, faqat `true`/`false` qaytaradi: matnni asosiy dastur tanlaydi. Shu tufayli `Kitob` ni boshqa dasturda (masalan, grafik interfeysda) oʻzgartirmasdan ishlatish mumkin. Java'dagi `computeIfAbsent(nom, Kitob::new)` — „kitob boʻlmasa, yangisini yarat“ ning qisqa yozuvi.

## Mustaqil ish topshirigʻi

Pastdagi topshiriqni yeching: `BankAccount` sinfini yarating — balans maydoni **yopiq** (private), u faqat `deposit`, `withdraw` va `balance` metodlari orqali boshqariladi; mablagʻ yetarli boʻlmasa, yechish rad etiladi. Buyruqlar oqimini qayta ishlang. Topshiriladigan ish:

1. **Sinf diagrammasi** — maydonlar (kirish huquqi bilan) va metodlar (parametrlari va qaytaradigan qiymati bilan); qaysi metod qanday tekshiruv bajaradi.
2. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
3. **Testlar jadvali** — kamida 5 ta buyruqlar ketma-ketligi: oddiy toʻldirish va yechish, balansdan koʻp yechish, balansga teng yechish, boshlangʻich holatda `balance`, katta summalar.
4. **Xulosa** — 3–5 gap: nima uchun balans maydoni yopiq boʻlishi kerak va bu qanday xatolarning oldini oladi.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Balans maydoniga sinfdan tashqaridan toʻgʻridan-toʻgʻri murojaat qilinsa (`hisob.balans -= x`), ish toʻliq baholanmaydi.
- Rad etilgan yechishda balans **oʻzgarmasligi** kerak.
- Summalar 10⁹ gacha va buyruqlar 1000 tagacha: balans `int` ga sigʻmasligi mumkin — `long` / `long long` ishlating.
- Xabarni sinf metodi emas, asosiy dastur chiqarsin: metod natija qaytaradi.
:::

:::tip
Sinfni yozib boʻlgach, avval uni buyruqlar oqimisiz, bir nechta toʻgʻridan-toʻgʻri chaqiruv bilan tekshiring: `deposit(100)`, `withdraw(150)`, `balance()`. Sinf toʻgʻri ishlasa, buyruqlarni oʻqiydigan qism oddiy boʻlib qoladi.
:::

## Xulosa

- Sinflar assotsiatsiya, agregatsiya yoki kompozitsiya orqali bogʻlanadi.
- Har bir maʼlumot bitta sinfda saqlanadi va faqat shu sinf metodlari orqali oʻzgaradi.
- Metodlar natija qaytaradi; foydalanuvchiga xabar chiqarish — asosiy dasturning vazifasi.
- Buyruqlar oqimi buyruq nomiga qarab tegishli metodni chaqirish orqali qayta ishlanadi.

## Oʻzingizni tekshiring

:::check
Q: Agregatsiya va kompozitsiyaning farqi nima?
A: Agregatsiyada qismlar butundan mustaqil yashaydi; kompozitsiyada ular butun bilan birga yaratiladi va yoʻqoladi.
Q: Nima uchun `Kutubxona` kitobning `berilgan` maydonini toʻgʻridan-toʻgʻri oʻzgartirmaydi?
A: Holatni faqat egasi — `Kitob` — oʻzgartirishi kerak; shunda tekshiruv bitta joyda boʻladi va kitob notoʻgʻri holatga tushmaydi.
Q: Nima uchun `Kitob.ber()` xabar chiqarmaydi, `true`/`false` qaytaradi?
A: Xabar matni dasturga bogʻliq; natija qaytaruvchi metod istalgan dasturda (konsol, grafik interfeys) qayta ishlatiladi.
Q: Namunada `ber Sarob` uchinchi marta nima uchun `yoʻq` qaytardi?
A: Ikkala nusxa ham berilgan edi: `berilgan == jami`.
:::

:::link https://dev.java/learn/classes-objects/
Java: sinflar va obyektlar
Sinflarni loyihalash, kirish huquqlari va metodlar boʻyicha rasmiy oʻquv resursi (ingliz tilida).
:::
