---
title: Elektron bozor va elektron tovarlar tushunchasi
summary: Mustaqil ish — elektron bozor tushunchasi va turlari (elektron doʻkon, marketpleys, birja, auksion, teskari auksion — davlat xaridlari); elektron (raqamli) tovarlar — dasturlar, elektron kitoblar, musiqa, video, onlayn kurslar, oʻyin ichidagi narsalar; raqamli tovarlar iqtisodiyoti — nolga yaqin chekli xarajat, raqobatsizlik, tajriba tovari; narxlash strategiyalari — versiyalash, toʻplam (bundling), freemium, obuna, dinamik narx; ikki tomonlama platformalar; mualliflik huquqi va DRM; isteʼmolchi huquqlari; toʻplam narxini tanlash dasturi.
---

## Mustaqil ishning maqsadi

M13 da elektron biznes modellari va marketing koʻrsatkichlarini koʻrdik. Bu mustaqil ishda **elektron bozor** qanday tashkil etilishini va **raqamli tovarlar** oddiy tovarlardan iqtisodiy jihatdan nimasi bilan farq qilishini oʻrganamiz — bu farq narxlash va raqobatni tubdan oʻzgartiradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- elektron bozor turlarini farqlay olasiz;
- raqamli tovarlarning iqtisodiy xususiyatlarini tushuntira olasiz;
- raqamli tovarlar uchun narxlash strategiyalarini tanlay olasiz;
- toʻplam narxining foydasini hisoblay olasiz.
:::

## Elektron bozor

**Elektron bozor** — sotuvchi va xaridorlarning narx, miqdor va shartlar boʻyicha kelishuvi elektron vositalar orqali amalga oshiriladigan muhit.

| Tur | Narx qanday shakllanadi | Misol |
|---|---|---|
| **Elektron doʻkon** | sotuvchi qatʼiy narx qoʻyadi | brend sayti |
| **Marketpleys** | koʻp sotuvchi raqobatlashadi | Uzum Market, Amazon (M13) |
| **Elektron birja** | talab va taklif uchrashganda | tovar va valyuta birjalari |
| **Auksion** | xaridorlar narxni oshirib boradi | eBay, musodara qilingan mulk savdolari |
| **Teskari auksion** | sotuvchilar narxni pasaytirib boradi | davlat xaridlari portali, tenderlar |
| **Eʼlonlar platformasi** | tomonlar oʻzaro kelishadi | OLX kabi C2C saytlar |

## Elektron tovarlar

**Elektron (raqamli) tovar** — raqamli shaklda yaratiladigan, saqlanadigan va yetkaziladigan tovar: dasturlar va ilovalar, elektron kitoblar, musiqa va video, onlayn kurslar, shriftlar va shablonlar, oʻyin ichidagi narsalar, maʼlumotlar bazalari, raqamli chiptalar.

### Iqtisodiy xususiyatlari

| Xususiyat | Mazmuni | Natija |
|---|---|---|
| **Yuqori boshlangʻich, nolga yaqin chekli xarajat** | birinchi nusxa qimmat (ishlab chiqish), keyingisi deyarli bepul | narxni tannarx emas, xaridorlar qadrlashi belgilaydi |
| **Raqobatsizlik** | bitta nusxadan foydalanish boshqasiga xalaqit bermaydi | nusxalash oson — himoya kerak |
| **Tajriba tovari** | sifatini foydalangandan keyingina bilish mumkin | bepul sinov, namuna, sharhlar muhim |
| **Tarmoq effekti** | foydalanuvchi koʻpaygan sari qiymat oshadi (MT6) | "gʻolib koʻp narsani oladi" |
| **Qulflanish** (lock-in) | boshqa tovarga oʻtish xarajati (fayllar, odatlar) | sodiqlik va kuchli bozor mavqei |

## Narxlash strategiyalari

| Strategiya | Mazmuni | Misol |
|---|---|---|
| **Versiyalash** | bir mahsulotning turli imkoniyatli versiyalari turli narxda | "Asosiy", "Pro", "Biznes" |
| **Toʻplam** (bundling) | bir nechta tovarni birga, alohida yigʻindisidan arzonroq | ofis paketi, striming + musiqa |
| **Freemium** | asosiy funksiya bepul, kengaytirilgani pullik | bulutli saqlash 15 GB bepul |
| **Obuna** | oylik yoki yillik toʻlov, doimiy kirish | onlayn kino, SaaS (M11) |
| **Dinamik narx** | talab, vaqt, mijozga qarab oʻzgaradi | aviachiptalar, taksi |
| **Mikrotoʻlovlar** | kichik summalar ilova ichida | oʻyindagi narsalar |

**Toʻplam nima uchun foydali?** Xaridorlar tovarlarni turlicha qadrlaydi: biri birinchi tovarni qimmat, ikkinchisini arzon baholaydi, boshqasi — aksincha. Alohida sotishda sotuvchi har bir tovar uchun bitta narx tanlashi kerak va baʼzi xaridorlarni yoʻqotadi. Toʻplamda qadrlashlar yigʻiladi va bir-biriga yaqinlashadi — bitta narx bilan koʻproq xaridorni qamrab olish mumkin. Raqamli tovarlarda qoʻshimcha nusxa deyarli bepul boʻlgani uchun bu strategiya ayniqsa samarali.

## Ishlangan misol: alohida yoki toʻplam?

Har bir satrda xaridor va uning A va B mahsulotlarni qancha qadrlashi (ming soʻm). Har bir mahsulot uchun tushumni maksimal qiladigan narxni (teng boʻlsa — yuqoriroq narxni), keyin toʻplam uchun eng yaxshi narxni toping va solishtiring. Xaridor tovarni qadrlashi narxdan kam boʻlmasa, sotib oladi.

```python
import sys

def eng_yaxshi(qadrlar):
    # tushum maksimal; teng boʻlsa — yuqoriroq narx
    narx, eng_tushum = 0, 0
    for p in sorted(set(qadrlar)):
        t = p * sum(1 for q in qadrlar if q >= p)
        if t >= eng_tushum:
            narx, eng_tushum = p, t
    return narx, eng_tushum

a, b = [], []
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    _, qa, qb = satr.split()
    a.append(int(qa))
    b.append(int(qb))

na, ta = eng_yaxshi(a)
nb, tb = eng_yaxshi(b)
nt, tt = eng_yaxshi([x + y for x, y in zip(a, b)])     # har bir xaridor uchun toʻplam qiymati
print(f"Mahsulot A: narx {na}, tushum {ta}")
print(f"Mahsulot B: narx {nb}, tushum {tb}")
print(f"Alohida sotuv: {ta + tb}")
print(f"Toʻplam: narx {nt}, tushum {tt}")
print(f"Tavsiya: {'toʻplam' if tt > ta + tb else 'alohida'}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {
    static long[] engYaxshi(List<Long> qadrlar) {
        // tushum maksimal; teng boʻlsa — yuqoriroq narx
        long narx = 0, engTushum = 0;
        for (long p : new TreeSet<>(qadrlar)) {
            long soni = 0;
            for (long q : qadrlar) if (q >= p) soni++;
            if (p * soni >= engTushum) { narx = p; engTushum = p * soni; }
        }
        return new long[]{narx, engTushum};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Long> a = new ArrayList<>(), b = new ArrayList<>(), t = new ArrayList<>();
        while (in.hasNext()) {
            in.next();
            long qa = in.nextLong(), qb = in.nextLong();
            a.add(qa);
            b.add(qb);
            t.add(qa + qb);                                  // har bir xaridor uchun toʻplam qiymati
        }
        long[] ra = engYaxshi(a), rb = engYaxshi(b), rt = engYaxshi(t);
        System.out.println("Mahsulot A: narx " + ra[0] + ", tushum " + ra[1]);
        System.out.println("Mahsulot B: narx " + rb[0] + ", tushum " + rb[1]);
        System.out.println("Alohida sotuv: " + (ra[1] + rb[1]));
        System.out.println("Toʻplam: narx " + rt[0] + ", tushum " + rt[1]);
        System.out.println("Tavsiya: " + (rt[1] > ra[1] + rb[1] ? "toʻplam" : "alohida"));
    }
}
```
```cpp
#include <iostream>
#include <set>
#include <string>
#include <utility>
#include <vector>
using namespace std;

pair<long long, long long> engYaxshi(const vector<long long>& qadrlar) {
    // tushum maksimal; teng boʻlsa — yuqoriroq narx
    long long narx = 0, engTushum = 0;
    for (long long p : set<long long>(qadrlar.begin(), qadrlar.end())) {
        long long soni = 0;
        for (long long q : qadrlar) if (q >= p) soni++;
        if (p * soni >= engTushum) { narx = p; engTushum = p * soni; }
    }
    return {narx, engTushum};
}

int main() {
    vector<long long> a, b, t;
    string ism;
    long long qa, qb;
    while (cin >> ism >> qa >> qb) {
        a.push_back(qa);
        b.push_back(qb);
        t.push_back(qa + qb);                                // har bir xaridor uchun toʻplam qiymati
    }
    auto ra = engYaxshi(a), rb = engYaxshi(b), rt = engYaxshi(t);
    cout << "Mahsulot A: narx " << ra.first << ", tushum " << ra.second << "\n";
    cout << "Mahsulot B: narx " << rb.first << ", tushum " << rb.second << "\n";
    cout << "Alohida sotuv: " << ra.second + rb.second << "\n";
    cout << "Toʻplam: narx " << rt.first << ", tushum " << rt.second << "\n";
    cout << "Tavsiya: " << (rt.second > ra.second + rb.second ? "toʻplam" : "alohida") << "\n";
    return 0;
}
```

Kirish:

```text
Ali 120 30
Vali 30 120
Sardor 90 90
Kamola 60 50
```

Chiqish:

```text
Mahsulot A: narx 90, tushum 180
Mahsulot B: narx 90, tushum 180
Alohida sotuv: 360
Toʻplam: narx 150, tushum 450
Tavsiya: toʻplam
```

Alohida sotishda har bir mahsulotni faqat ikki kishi oladi (Ali va Sardor — A ni, Vali va Sardor — B ni). Toʻplam narxi 150 bilan esa Ali (150), Vali (150) va Sardor (180) toʻplamni sotib oladi — tushum 25% oshadi. Ali va Vali didlari qarama-qarshi, lekin toʻplamni bir xil qadrlaydi — bu toʻplamning "sehri". Faqat Kamola (110) ikkala holatda ham xarid qilmaydi.

## Ikki tomonlama platformalar

Marketpleys, taksi ilovasi, toʻlov tizimi — **ikki tomonlama bozorlar**: ular ikki guruhni (sotuvchi va xaridor, haydovchi va yoʻlovchi) bogʻlaydi. Bir tomon qancha koʻp boʻlsa, ikkinchi tomon uchun platforma shuncha qimmatli (oʻzaro tarmoq effekti). Shuning uchun platformalar koʻpincha bir tomonni subsidiyalaydi: xaridorlar uchun bepul yetkazib berish, haydovchilar uchun bonuslar — haq esa boshqa tomondan (komissiya) olinadi.

## Mualliflik huquqi va isteʼmolchi huquqlari

- **Mualliflik huquqi**: raqamli tovar nusxalanishi oson; noqonuniy tarqatish (qaroqchilik) mualliflar daromadini kamaytiradi. **DRM** (raqamli huquqlarni boshqarish) — nusxalash va tarqatishni texnik cheklash; lekin qonuniy xaridorlarga noqulaylik tugʻdirishi mumkin. Arzon va qulay qonuniy xizmatlar (obuna) qaroqchilikni kamaytirishning samarali yoʻli boʻlib chiqdi.
- **Litsenziya va egalik**: raqamli tovar sotib olinganda koʻpincha **foydalanish litsenziyasi** olinadi, tovarning oʻzi emas — platforma yopilsa, kirish yoʻqolishi mumkin.
- **Isteʼmolchi huquqlari**: tovar tavsifi aniq boʻlishi, toʻlovdan oldin shartlar koʻrsatilishi, avtomatik obunani oson bekor qilish imkoniyati.

:::warning
**Koʻp uchraydigan xatolar**

- **Raqamli tovar narxini tannarx asosida belgilash.** Chekli xarajat nolga yaqin — narx qadrlash va raqobatdan kelib chiqadi.
- **Toʻplamni har doim foydali deb hisoblash.** Agar xaridorlar tovarlarni bir xil tartibda qadrlasa, foyda kichik yoki yoʻq.
- **Freemium da pullik versiyaga sabab qoldirmaslik.** Bepul versiya haddan tashqari yaxshi boʻlsa, hech kim toʻlamaydi.
- **Obunani bekor qilishni qiyinlashtirish.** Isteʼmolchi huquqlarini buzadi va ishonchni yoʻqotadi.
:::

## Topshiriq

1. Misolda Kamolaning qadrlashlarini (60, 50) dan (80, 80) ga oʻzgartiring. Natija qanday oʻzgaradi?
2. Ikkala xaridor ham tovarlarni bir xil tartibda qadrlaydigan maʼlumot tuzing (masalan, A har doim B dan qimmat). Toʻplam hali ham foydalimi?
3. Oʻzingiz foydalanadigan bitta raqamli xizmatning narxlash strategiyasini tahlil qiling: versiyalar, freemium, toʻplam, obuna.
4. Davlat xaridlari portalida bitta teskari auksion natijasini toping: boshlangʻich va yakuniy narx farqi qancha?

## Xulosa

- Elektron bozor turlari: doʻkon, marketpleys, birja, auksion, teskari auksion, eʼlonlar platformasi.
- Raqamli tovarlar: yuqori boshlangʻich va nolga yaqin chekli xarajat, raqobatsizlik, tajriba tovari, tarmoq effekti, qulflanish.
- Narxlash: versiyalash, toʻplam, freemium, obuna, dinamik narx, mikrotoʻlovlar.
- Toʻplam turlicha qadrlashlarni tekislab, tushumni oshirishi mumkin; platformalar bir tomonni subsidiyalaydi.

## Oʻzingizni tekshiring

:::check
Q: Raqamli tovarning chekli xarajati nima uchun nolga yaqin?
A: Birinchi nusxa yaratilgandan keyin har bir qoʻshimcha nusxani tarqatish deyarli bepul.
Q: Teskari auksion nima?
A: Sotuvchilar xaridor (masalan, davlat buyurtmachisi) uchun narxni pasaytirib raqobatlashadigan savdo.
Q: Versiyalash strategiyasi nima?
A: Bitta mahsulotning turli imkoniyatli versiyalarini turli narxda sotish.
Q: Toʻplam qachon eng foydali?
A: Xaridorlar tovarlarni turlicha (qarama-qarshi) qadrlaganda.
:::

:::link https://www.oecd.org/en/topics/digital-economy.html
OECD: Digital economy
Raqamli iqtisodiyot va raqamli bozorlar boʻyicha tadqiqotlar (ingliz tilida).
:::
