---
title: Axborotni himoyalashning kriptografik usullari. Enigma shifrlash mashinasi va uning bardoshliligi
summary: Mexanik va elektromexanik shifrlash mashinalari tarixi; Enigma tuzilishi — klaviatura, rotorlar, reflektor, kommutatsiya paneli, rotorlarning qadamlanishi; kalit fazosi va uning hisobi; Enigma xossalari — oʻzaro teskarilik va hech bir harf oʻziga shifrlanmasligi; buzilishi — Polsha matematiklari (Rejewski), Bletchley Park, Turing va Bombe, "crib" usuli; operator xatolari; zamonaviy kriptografiya uchun saboqlar; soddalashtirilgan Enigma modeli dasturda.
---

## Nima uchun bu mavzu muhim?

Enigma — kriptografiya tarixidagi eng mashhur mashina. Ikkinchi jahon urushida Germaniya armiyasi uni buzib boʻlmaydi deb hisoblagan: kalit fazosi 10²⁰ dan katta edi. Shunga qaramay, polyak va britaniyalik matematiklar uni muntazam ochib turishgan, tarixchilar bu urushni bir necha yilga qisqartirgan deb hisoblaydi. Enigma tarixi M5 dagi saboqlarni yorqin koʻrsatadi: katta kalit fazosi yetarli emas, shifrning tuzilmaviy xossalari va **odamlar xatolari** uni buzishi mumkin. Bundan tashqari, Enigmani ochish uchun qurilgan mashinalar zamonaviy kompyuterlarning oʻtmishdoshlaridan biri boʻldi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- Enigma tuzilishi va shifrlash jarayonini tushuntira olasiz;
- uning kalit fazosini baholay olasiz;
- Enigma zaifliklarini va ular qanday ishlatilganini ayta olasiz;
- soddalashtirilgan Enigma modelini dasturda koʻrsata olasiz;
- Enigma tarixidan zamonaviy kriptografiya uchun saboqlar chiqara olasiz.
:::

## Enigma tuzilishi

Enigma — elektromexanik **rotorli** shifrlash mashinasi (1920-yillarda tijorat uchun ixtiro qilingan, keyin Germaniya qurolli kuchlari tomonidan qabul qilingan).

| Qism | Vazifasi |
|---|---|
| **Klaviatura va chiroqli panel** | operator harf bosadi — shifrlangan harf chirogʻi yonadi |
| **Kommutatsiya paneli** (Steckerbrett) | 10 juft harfni kabel bilan oʻzaro almashtiradi (masalan, A↔Q) |
| **Rotorlar** (odatda 3 ta, 5 tadan tanlanadi) | har biri 26 kontaktli disk, ichida harflarni almashtiruvchi simlar |
| **Reflektor** | signalni orqaga, rotorlar orqali qaytaradi |
| **Halqa sozlamasi** (Ringstellung) | rotor simlarini uning harfli halqasiga nisbatan suradi |

**Shifrlash yoʻli**: klavisha → kommutatsiya paneli → oʻng, oʻrta, chap rotor → reflektor → chap, oʻrta, oʻng rotor (teskari yoʻnalishda) → kommutatsiya paneli → chiroq. Har bir klavisha bosilganda **oʻng rotor bir qadam buriladi**, toʻliq aylanishdan keyin oʻrtadagisi, keyin chapdagisi (soat strelkalari kabi). Shuning uchun bir xil harf har safar boshqacha shifrlanadi — Enigma juda uzun davrli **koʻp alifboli** almashtirish shifri (A5 dagi Vijinerning murakkab davomchisi).

**Kalit fazosi** (armiya Enigmasi uchun taxminiy hisob):

| Qism | Variantlar |
|---|---|
| 5 tadan 3 ta rotorni tartib bilan tanlash | 5 · 4 · 3 = 60 |
| Rotorlarning boshlangʻich holati | 26³ = 17 576 |
| Kommutatsiya panelida 10 juft | ≈ 1,5 · 10¹⁴ |
| Jami | ≈ 1,6 · 10²⁰ (halqa sozlamalari bundan tashqari) |

Bu taxminan 67 bitli kalitga teng — oʻsha davr uchun ulkan son: qoʻlda yoki mexanik toʻliq tanlash mumkin emas edi.

## Enigma xossalari va zaifliklari

1. **Oʻzaro teskarilik**: reflektor tufayli shifrlash va deshifrlash bir xil amal. Bir xil sozlamada shifrmatnni qayta "shifrlash" ochiq matnni beradi — operatorlar uchun qulay, lekin almashtirish doim juftliklardan iborat.
2. **Hech bir harf oʻziga shifrlanmaydi**: signal reflektordan oʻsha simdan qaytolmaydi. Bu kichik xossa juda katta zaiflikka aylandi: agar shifrmatnda biror soʻz qayerda turishini taxmin qilsangiz (**crib** — masalan, "WETTERBERICHT", ob-havo hisoboti), u harflari shifrmatn harflari bilan mos keladigan joylarda tura olmaydi.
3. **Operator xatolari va tartiblar**: har kuni bir xil vaqtda bir xil shakldagi xabarlar (ob-havo, "hisobot yoʻq"), xabar kalitini ikki marta takrorlash (1940-yilgacha), oddiy kalitlar (klaviaturada yonma-yon harflar, ism harflari).
4. **Kommutatsiya paneli faqat juftliklarni almashtiradi** va rotorlar tuzilmasini yashirmaydi.

## Enigmaning buzilishi

- **Polsha (1932–1939)**: Marian Rejewski permutatsiyalar nazariyasi va takrorlangan xabar kalitidan foydalanib, rotorlar simlarini matematik tiklagan; Henryk Zygalski va Jerzy Różycki bilan birga "bomba" mashinalari va perforatsiyalangan varaqlar yaratilgan. 1939-yil iyulda natijalar Britaniya va Fransiyaga topshirilgan.
- **Bletchley Park (Buyuk Britaniya)**: Alan Turing va Gordon Welchman crib asosida ishlaydigan elektromexanik **Bombe** ni loyihalashgan — u rotor sozlamalarini tez tanlab, crib ga zid kelganlarini chiqarib tashlagan. Urush oxiriga kelib yuzlab Bombe ishlagan; olingan razvedka maʼlumotlari "Ultra" deb atalgan.
- **Qoʻlga olingan materiallar**: kemalar va suv osti kemalaridan olingan kalit jadvallari va mashinalar ham muhim rol oʻynagan.

## Ishlangan misol 1: soddalashtirilgan Enigma

Haqiqiy Enigma I ning I-rotori simlari (`EKMFLGDQVZNTOWYHXUSPAIBRCJ`) va B-reflektor (`YRUHQSLDPXNGOKMIEBFZCWVJAT`) bilan **bitta rotorli** model: har bir harfdan oldin rotor bir qadam buriladi; signal rotor → reflektor → rotor (teskari) yoʻlidan oʻtadi; harf boʻlmagan belgilar oʻzgarmaydi. Birinchi satrda rotorning boshlangʻich holati (0–25), ikkinchisida matn.

```python
import sys

ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ"        # Enigma I, I-rotor
REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT"    # B-reflektor

satrlar = sys.stdin.read().split("\n")
holat = int(satrlar[0])
natija = []
for ch in satrlar[1]:
    if not ("A" <= ch <= "Z"):
        natija.append(ch)
        continue
    holat = (holat + 1) % 26                # har bir harfdan oldin rotor buriladi
    c = ord(ch) - 65
    c = (ord(ROTOR[(c + holat) % 26]) - 65 - holat) % 26        # rotor orqali
    c = ord(REFLEKTOR[c]) - 65                                   # reflektor
    c = (ROTOR.index(chr((c + holat) % 26 + 65)) - holat) % 26  # rotor orqali teskari
    natija.append(chr(c + 65))
print("".join(natija))
```
```java
import java.util.Scanner;

public class Main {
    static final String ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";       // Enigma I, I-rotor
    static final String REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT";   // B-reflektor

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int holat = Integer.parseInt(in.nextLine().trim());
        String matn = in.nextLine();
        StringBuilder natija = new StringBuilder();
        for (char ch : matn.toCharArray()) {
            if (ch < 'A' || ch > 'Z') {
                natija.append(ch);
                continue;
            }
            holat = (holat + 1) % 26;                                 // har bir harfdan oldin rotor buriladi
            int c = ch - 'A';
            c = Math.floorMod(ROTOR.charAt((c + holat) % 26) - 'A' - holat, 26);        // rotor orqali
            c = REFLEKTOR.charAt(c) - 'A';                                                // reflektor
            c = Math.floorMod(ROTOR.indexOf((char) ((c + holat) % 26 + 'A')) - holat, 26);   // rotor orqali teskari
            natija.append((char) (c + 'A'));
        }
        System.out.println(natija);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const string ROTOR = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";        // Enigma I, I-rotor
const string REFLEKTOR = "YRUHQSLDPXNGOKMIEBFZCWVJAT";    // B-reflektor

int mod26(int x) { return (x % 26 + 26) % 26; }

int main() {
    string satr, matn;
    getline(cin, satr);
    int holat = stoi(satr);
    getline(cin, matn);
    if (!matn.empty() && matn.back() == '\r') matn.pop_back();
    string natija;
    for (char ch : matn) {
        if (ch < 'A' || ch > 'Z') {
            natija += ch;
            continue;
        }
        holat = (holat + 1) % 26;                                  // har bir harfdan oldin rotor buriladi
        int c = ch - 'A';
        c = mod26(ROTOR[(c + holat) % 26] - 'A' - holat);          // rotor orqali
        c = REFLEKTOR[c] - 'A';                                    // reflektor
        c = mod26((int) ROTOR.find((char) ((c + holat) % 26 + 'A')) - holat);   // rotor orqali teskari
        natija += (char) (c + 'A');
    }
    cout << natija << endl;
    return 0;
}
```

Kirish:

```text
3
SALOM DUNYO
```

Chiqish:

```text
GIMHC NMRUA
```

Endi chiqishni xuddi shu boshlangʻich holat bilan qayta kiriting (`3` va `GIMHC NMRUA`) — dastur `SALOM DUNYO` ni qaytaradi: reflektor tufayli shifrlash va deshifrlash bir xil amal. Eʼtibor bering: matndagi ikkita O harfi turlicha — H va A ga shifrlandi, chunki har bir pozitsiyada rotor holati boshqacha; va hech bir harf oʻz oʻrnida oʻziga aylanmadi.

## Ishlangan misol 2: crib joylashuvini tekshirish

Hech bir harf oʻziga shifrlanmasligidan foydalanib, Bletchley Park kriptoanalitiklari crib ni shifrmatn boʻylab surib, **mumkin boʻlmagan** joylarni chiqarib tashlagan: agar biror pozitsiyada crib harfi shifrmatn harfiga teng boʻlsa, crib bu joyda turolmaydi. Birinchi satrda shifrmatn, ikkinchisida crib. Barcha mumkin boʻlgan boshlangʻich pozitsiyalarni (0 dan) va ularning sonini chiqaring.

```python
import sys

satrlar = sys.stdin.read().split("\n")
shifr, crib = satrlar[0].strip(), satrlar[1].strip()
mumkin = []
for i in range(len(shifr) - len(crib) + 1):
    if all(shifr[i + j] != crib[j] for j in range(len(crib))):   # birorta harf ustma-ust tushmasin
        mumkin.append(i)
print("mumkin: " + " ".join(map(str, mumkin)))
print(f"{len(mumkin)} / {len(shifr) - len(crib) + 1}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String shifr = in.nextLine().trim(), crib = in.nextLine().trim();
        List<String> mumkin = new ArrayList<>();
        int jami = shifr.length() - crib.length() + 1;
        for (int i = 0; i < jami; i++) {
            boolean ok = true;
            for (int j = 0; j < crib.length(); j++) {
                if (shifr.charAt(i + j) == crib.charAt(j)) ok = false;   // birorta harf ustma-ust tushmasin
            }
            if (ok) mumkin.add(String.valueOf(i));
        }
        System.out.println("mumkin: " + String.join(" ", mumkin));
        System.out.println(mumkin.size() + " / " + jami);
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string shifr, crib;
    getline(cin, shifr);
    getline(cin, crib);
    while (!shifr.empty() && (shifr.back() == '\r' || shifr.back() == ' ')) shifr.pop_back();
    while (!crib.empty() && (crib.back() == '\r' || crib.back() == ' ')) crib.pop_back();
    vector<int> mumkin;
    int jami = (int) shifr.size() - (int) crib.size() + 1;
    for (int i = 0; i < jami; i++) {
        bool ok = true;
        for (size_t j = 0; j < crib.size(); j++) {
            if (shifr[i + j] == crib[j]) ok = false;                 // birorta harf ustma-ust tushmasin
        }
        if (ok) mumkin.push_back(i);
    }
    cout << "mumkin:";
    for (int p : mumkin) cout << " " << p;
    cout << endl << mumkin.size() << " / " << jami << endl;
    return 0;
}
```

Kirish:

```text
NAQBKGTKVCRUOSJDPSKKFMMZIZMBKU
WEATHERREPORT
```

Chiqish:

```text
mumkin: 0 1 5 6 8 9 10 11 12 13 14 15 16 17
14 / 18
```

Shifrmatn 1-misoldagi model bilan (boshlangʻich holat 5) `ATTACKATDAWNWEATHERREPORTCLEAR` matnidan olingan; crib haqiqatan 12-pozitsiyada turibdi va u "mumkin" roʻyxatida qoldi. 18 ta pozitsiyadan 4 tasi darhol chiqarib tashlandi. Crib qancha uzun va xabarlar qancha koʻp boʻlsa, filtr shuncha kuchli ishlaydi; qolgan nomzodlarni Bombe rotor sozlamalarini tanlash orqali tekshirgan. Zamonaviy shifrlarda (AES) shifrmatn va ochiq matn orasida bunday "taqiqlangan" moslik yoʻq.

## Zamonaviy kriptografiya uchun saboqlar

| Enigma tajribasi | Zamonaviy tamoyil |
|---|---|
| Mashina va rotorlar qoʻlga tushgan, simlar tiklangan | **Kerkxofs tamoyili**: xavfsizlik faqat kalitga bogʻliq boʻlishi kerak (M5) |
| 10²⁰ kalit, lekin tuzilmaviy xossalar zaif | katta kalit fazosi — zarur, lekin yetarli emas |
| Bashorat qilinadigan matnlar (crib) | shifr **maʼlum ochiq matn hujumiga** bardoshli boʻlishi kerak |
| Xabar kalitini takrorlash, oddiy kalitlar | protokol va foydalanish tartibi algoritm kabi muhim; tasodifiy kalitlar |
| "Bizning shifrimiz buzilmaydi" degan ishonch | ochiq tahlil qilingan standartlar, doimiy qayta baholash |

:::warning
**Koʻp uchraydigan xatolar**

- **Enigmani "oddiy almashtirish shifri" deb hisoblash.** Har bir harfda rotor buriladi — u juda uzun davrli koʻp alifboli shifr.
- **Uni faqat Turing buzgan deb aytish.** Asosiy matematik yutuq polyak kriptoanalitiklariga tegishli; Bletchley Park ularning ishini davom ettirgan va kengaytirgan.
- **Katta kalit fazosini xavfsizlik bilan tenglashtirish.** Enigma — aksincha misol.
- **Tarixiy faktlarni tekshirmaslik.** Mashhur filmlar voqealarni soddalashtiradi; referatda tarixiy tadqiqotlar va muzey manbalariga havola bering.
:::

:::tip
Onlayn Enigma simulyatorlari (masalan, CyberChef dagi "Enigma" retsepti) uch rotorli, kommutatsiya panelli toʻliq modelni beradi. 1-misoldagi modelni ular bilan solishtiring: bitta rotor qoʻshilsa, natija qanday oʻzgaradi?
:::

## Xulosa

- Enigma — rotorli elektromexanik shifr: kommutatsiya paneli, uchta rotor, reflektor; har bir harfda rotor buriladi.
- Kalit fazosi ≈ 1,6 · 10²⁰, lekin reflektor tufayli hech bir harf oʻziga shifrlanmaydi — bu crib hujumiga yoʻl ochgan.
- Polyak matematiklari (Rejewski) birinchi boʻlib ochgan, Bletchley Park (Turing, Welchman) Bombe bilan sanoat darajasiga olib chiqqan.
- Operator xatolari va bashorat qilinadigan xabarlar algoritm zaifliklari kabi muhim boʻlgan.
- Saboqlar: Kerkxofs tamoyili, maʼlum ochiq matn hujumiga bardoshlilik, toʻgʻri foydalanish tartibi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun Enigmada bir xil harf har safar turlicha shifrlanadi?
A: Har bir klavisha bosilganda rotor buriladi va almashtirish jadvali oʻzgaradi.
Q: Enigmaning qaysi xossasi crib hujumiga yoʻl ochgan?
A: Hech bir harf oʻziga shifrlanmasligi — crib shifrmatn harfi bilan mos keladigan joyda turolmaydi.
Q: Enigmada deshifrlash qanday bajariladi?
A: Xuddi shu sozlamada shifrmatnni qayta kiritish orqali — reflektor tufayli amal oʻzaro teskari.
Q: Enigma tarixidan Kerkxofs tamoyiliga oid qanday saboq olinadi?
A: Mashina va simlar dushmanga maʼlum boʻlib qolgan; xavfsizlik faqat maxfiy kalitga tayanishi kerak.
:::

:::link https://www.bletchleypark.org.uk/
Bletchley Park
Enigma va urush davri kriptoanalizi tarixiga bagʻishlangan muzeyning rasmiy sayti (ingliz tilida).
:::
