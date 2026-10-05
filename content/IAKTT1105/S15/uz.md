---
title: Soliq hisobotlari va bank kredit hamda omonat turlari
summary: Seminar — elektron soliq hisobotlari (soliq toʻlovchining shaxsiy kabineti, ERI, muddatlar, elektron hisob-fakturalar, onlayn kassalar); asosiy soliqlar (QQS, foyda soligʻi, aylanmadan olinadigan soliq, JShDS, ijtimoiy soliq); ish haqi soliqlarini hisoblash dasturi; bank kreditlari turlari (isteʼmol, ipoteka, avtokredit, mikroqarz, biznes kreditlar) va toʻlov usullari (annuitet, differensial); omonat turlari (talab qilib olinguncha, muddatli, jamgʻarma) va foiz turlari (oddiy, murakkab); samarali foiz stavkasi; moliyaviy savodxonlik.
---

## Seminarning maqsadi

Iqtisodchi har kuni soliq hisobotlari va bank mahsulotlari bilan ishlaydi. Seminarda hisobotlar elektron tarzda qanday topshirilishini, ish haqi soliqlari qanday hisoblanishini koʻramiz va kredit hamda omonat turlarini solishtiramiz. Seminardagi dasturlash topshiriqi — omonat foizlarini hisoblash — mustaqil bajariladi.

:::note
**Bu seminardan soʻng siz:**

- soliq hisobotlarini elektron topshirish tartibini tushuntira olasiz;
- asosiy soliqlarni va ish haqidan olinadigan soliqlarni hisoblay olasiz;
- kredit turlari va toʻlov usullarini farqlay olasiz;
- omonat turlari, oddiy va murakkab foizni tushuntira olasiz.
:::

## Elektron soliq hisobotlari

- **Soliq toʻlovchining shaxsiy kabineti** (my.soliq.uz): hisobotlarni tayyorlash va yuborish, qabul qilinganligi haqida bildirishnoma, soliq qarzi va ortiqcha toʻlovlarni koʻrish, maʼlumotnomalar olish.
- Hisobotlar **elektron raqamli imzo** (S14) bilan imzolanadi — qogʻoz shakl va soliq idorasiga borish shart emas.
- **Muddatlar**: soliq turiga qarab oylik, choraklik yoki yillik; muddat buzilsa — jarima va penya.
- **Elektron hisob-fakturalar (ESF)** va **onlayn kassalar** soliq organiga maʼlumotni real vaqtda beradi; shuning uchun hisobot raqamlari ular bilan mos kelishi kerak.
- Buxgalteriya tizimlari (M15) hisobotlarni avtomatik shakllantirib, toʻgʻridan-toʻgʻri yuborishi mumkin.

## Asosiy soliqlar

| Soliq | Nimaga | Izoh |
|---|---|---|
| **Qoʻshilgan qiymat soligʻi (QQS)** | tovar va xizmatlar sotuviga | xaridlardagi QQS hisobga olinadi (kamaytiriladi) |
| **Foyda soligʻi** | korxona foydasiga | yirik korxonalar uchun |
| **Aylanmadan olinadigan soliq** | tushumga | kichik biznes uchun soddalashtirilgan tartib |
| **Jismoniy shaxslardan olinadigan daromad soligʻi (JShDS)** | xodim ish haqiga | ish beruvchi ushlab qoladi va toʻlaydi |
| **Ijtimoiy soliq** | ish haqi fondiga | ish beruvchi oʻz hisobidan toʻlaydi |
| **Mol-mulk, yer, suv resurslaridan foydalanganlik soligʻi** | aktivlar va resurslarga | |

:::warning
Soliq stavkalari va imtiyozlari oʻzgarib turadi. Hisobdan oldin amaldagi stavkalarni **Soliq kodeksi** (lex.uz) va soliq qoʻmitasi saytidan tekshiring. Quyidagi misoldagi stavkalar — oʻquv maqsadida.
:::

## Ishlangan misol: ish haqi soliqlari

Har bir satrda xodim familiyasi va hisoblangan oylik ish haqi (soʻm). JShDS 12% (ish haqidan ushlanadi), ijtimoiy soliq 12% (ish beruvchi toʻlaydi), soliqlar butun soʻmgacha pastga yaxlitlanadi. Har bir xodim va jami boʻyicha hisobot uchun raqamlarni chiqaring.

```python
import sys

JSHDS = 12        # foizda, xodim ish haqidan ushlanadi
IJTIMOIY = 12     # foizda, ish beruvchi toʻlaydi

jami = [0, 0, 0, 0]
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    familiya, hisob = satr.split()
    hisob = int(hisob)
    jshds = hisob * JSHDS // 100
    qolga = hisob - jshds                       # xodim qoʻliga oladi
    ijtimoiy = hisob * IJTIMOIY // 100
    print(f"{familiya}: hisoblangan {hisob}, JShDS {jshds}, qoʻlga {qolga}, ijtimoiy soliq {ijtimoiy}")
    for i, x in enumerate([hisob, jshds, qolga, ijtimoiy]):
        jami[i] += x
print(f"Jami: hisoblangan {jami[0]}, JShDS {jami[1]}, qoʻlga {jami[2]}, ijtimoiy soliq {jami[3]}")
```
```java
import java.util.Scanner;

public class Main {
    static final int JSHDS = 12;        // foizda, xodim ish haqidan ushlanadi
    static final int IJTIMOIY = 12;     // foizda, ish beruvchi toʻlaydi

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long[] jami = new long[4];
        while (in.hasNext()) {
            String familiya = in.next();
            long hisob = in.nextLong();
            long jshds = hisob * JSHDS / 100;
            long qolga = hisob - jshds;                  // xodim qoʻliga oladi
            long ijtimoiy = hisob * IJTIMOIY / 100;
            System.out.println(familiya + ": hisoblangan " + hisob + ", JShDS " + jshds + ", qoʻlga " + qolga + ", ijtimoiy soliq " + ijtimoiy);
            long[] satr = {hisob, jshds, qolga, ijtimoiy};
            for (int i = 0; i < 4; i++) jami[i] += satr[i];
        }
        System.out.println("Jami: hisoblangan " + jami[0] + ", JShDS " + jami[1] + ", qoʻlga " + jami[2] + ", ijtimoiy soliq " + jami[3]);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const int JSHDS = 12;        // foizda, xodim ish haqidan ushlanadi
const int IJTIMOIY = 12;     // foizda, ish beruvchi toʻlaydi

int main() {
    string familiya;
    long long hisob, jami[4] = {0, 0, 0, 0};
    while (cin >> familiya >> hisob) {
        long long jshds = hisob * JSHDS / 100;
        long long qolga = hisob - jshds;                 // xodim qoʻliga oladi
        long long ijtimoiy = hisob * IJTIMOIY / 100;
        cout << familiya << ": hisoblangan " << hisob << ", JShDS " << jshds << ", qoʻlga " << qolga
             << ", ijtimoiy soliq " << ijtimoiy << "\n";
        long long satr[4] = {hisob, jshds, qolga, ijtimoiy};
        for (int i = 0; i < 4; i++) jami[i] += satr[i];
    }
    cout << "Jami: hisoblangan " << jami[0] << ", JShDS " << jami[1] << ", qoʻlga " << jami[2]
         << ", ijtimoiy soliq " << jami[3] << "\n";
    return 0;
}
```

Kirish:

```text
Aliyev 5000000
Karimova 7350000
Tursunov 12400000
```

Chiqish:

```text
Aliyev: hisoblangan 5000000, JShDS 600000, qoʻlga 4400000, ijtimoiy soliq 600000
Karimova: hisoblangan 7350000, JShDS 882000, qoʻlga 6468000, ijtimoiy soliq 882000
Tursunov: hisoblangan 12400000, JShDS 1488000, qoʻlga 10912000, ijtimoiy soliq 1488000
Jami: hisoblangan 24750000, JShDS 2970000, qoʻlga 21780000, ijtimoiy soliq 2970000
```

Kompaniya uchun bu xodimlarning umumiy xarajati: 24 750 000 + 2 970 000 = 27 720 000 soʻm (ish haqi fondi + ijtimoiy soliq). "Jami" satridagi raqamlar JShDS va ijtimoiy soliq hisobotlariga tushadi.

## Bank kreditlari

| Tur | Maqsadi | Xususiyati |
|---|---|---|
| **Isteʼmol krediti** | maishiy texnika, taʼmir, taʼlim | qisqa va oʻrta muddat, nisbatan yuqori foiz |
| **Ipoteka** | uy-joy sotib olish | uzoq muddat (10–20 yil), uy garov boʻladi, boshlangʻich badal |
| **Avtokredit** | avtomobil | avtomobil garov |
| **Mikroqarz** | kichik summa, tez | yuqori foiz, qisqa muddat |
| **Biznes kreditlar** | aylanma mablagʻ, uskunalar, investitsiya loyihalari | biznes-reja, garov, kafillik |
| **Kredit kartalar va overdraft** | qisqa muddatli ehtiyoj | imtiyozli davr, keyin yuqori foiz |

**Toʻlov usullari**:

- **Annuitet** — har oy bir xil summa; boshida toʻlovning katta qismi foiz, oxirida — asosiy qarz. Rejalashtirish oson.
- **Differensial** — har oy asosiy qarzning teng qismi + qoldiqqa foiz; boshida toʻlov katta, keyin kamayadi. Jami foiz annuitetdan kamroq.

**Muhim**: kreditlarni nominal stavka emas, **yillik samarali foiz stavkasi** (barcha komissiya va toʻlovlar bilan) boʻyicha solishtiring — bank uni shartnomada koʻrsatishi shart. Oylik toʻlovlar daromadning katta qismini egallamasligi kerak (qarz yuki).

## Bank omonatlari

| Tur | Xususiyati |
|---|---|
| **Talab qilib olinguncha** | pulni istalgan vaqtda olish mumkin, foiz past yoki yoʻq |
| **Muddatli** | belgilangan muddatga, foiz yuqoriroq; muddatidan oldin olinsa foiz kamayadi |
| **Jamgʻarma** | muntazam toʻldirish mumkin, maqsadli jamgʻarish uchun |
| **Valyutadagi omonat** | xorijiy valyutada, foiz odatda pastroq, kurs xavfi |

**Foiz turlari**:

- **Oddiy foiz** — foiz har yil faqat **boshlangʻich summaga** hisoblanadi; balans har yil bir xil miqdorga oʻsadi.
- **Murakkab foiz (kapitallashtirish)** — foiz **joriy balansga**, yaʼni oldingi foizlar bilan birga hisoblanadi; balans tobora tezroq oʻsadi ("foizga foiz").

Uzoq muddatda murakkab foiz sezilarli farq qiladi. Bu ikki usulni yil boʻyicha hisoblash — seminarning **dasturlash topshiriqi**: uni sikl (takrorlanuvchi algoritm) yordamida mustaqil bajaring.

Shuningdek, omonatning **real daromadliligi** inflyatsiyaga bogʻliq: agar omonat foizi 20%, inflyatsiya esa 10% boʻlsa, xarid quvvati taxminan 10% ga oʻsadi. Omonatlar qonun boʻyicha kafolatlanadi (bank omonatlarini kafolatlash tizimi).

:::warning
**Koʻp uchraydigan xatolar**

- **Kreditlarni faqat nominal foiz boʻyicha solishtirish.** Komissiya va sugʻurtalar real narxni oshiradi.
- **Hisobotni oxirgi kunga qoldirish.** Tizim yuklamasi yoki ERI muammosi tufayli muddat oʻtib ketishi mumkin.
- **ESF va hisobot raqamlarining mos kelmasligi.** Soliq organi farqlarni avtomatik aniqlaydi.
- **Inflyatsiyani hisobga olmaslik.** Yuqori nominal foiz real daromadni anglatmaydi.
:::

## Amaliy topshiriqlar

1. Misoldagi xodimlarga 4-xodimni qoʻshing (8 125 000 soʻm) va jami satrini qoʻlda tekshiring.
2. Ikki bankning bir xil summadagi isteʼmol kreditlari shartlarini (stavka, komissiya, muddat) taqqoslang: qaysi biri arzonroq?
3. Annuitet va differensial toʻlovning afzalliklarini oila byudjeti nuqtai nazaridan muhokama qiling.
4. Omonat foizlari boʻyicha dasturlash topshiriqini bajaring.

## Xulosa

- Soliq hisobotlari my.soliq.uz orqali ERI bilan topshiriladi; ESF va onlayn kassalar maʼlumotlari bilan mos boʻlishi kerak.
- Asosiy soliqlar: QQS, foyda soligʻi, aylanmadan olinadigan soliq, JShDS, ijtimoiy soliq va boshqalar.
- Kreditlar: isteʼmol, ipoteka, avto, mikroqarz, biznes; toʻlov — annuitet yoki differensial; solishtirish samarali stavka boʻyicha.
- Omonatlar: talab qilib olinguncha, muddatli, jamgʻarma, valyutada; oddiy va murakkab foiz; real daromad inflyatsiyaga bogʻliq.

## Oʻzingizni tekshiring

:::check
Q: Soliq hisoboti qanday imzolanadi?
A: Elektron raqamli imzo bilan, soliq toʻlovchining shaxsiy kabineti orqali.
Q: JShDS va ijtimoiy soliqning farqi nima?
A: JShDS xodim ish haqidan ushlanadi; ijtimoiy soliqni ish beruvchi oʻz hisobidan ish haqi fondiga toʻlaydi.
Q: Annuitet toʻlov nima?
A: Kredit muddati davomida har oy bir xil summada toʻlanadigan toʻlov.
Q: Oddiy va murakkab foizning farqi nima?
A: Oddiy foiz faqat boshlangʻich summaga, murakkab foiz esa oldingi foizlar qoʻshilgan joriy balansga hisoblanadi.
:::

:::link https://soliq.uz/
Oʻzbekiston Respublikasi Soliq qoʻmitasi
Soliq stavkalari, hisobot shakllari, muddatlar va elektron xizmatlar.
:::
