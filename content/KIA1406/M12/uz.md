---
title: Fizik xavfsizlik. Tabiiy va sunʼiy tahdidlar, nazorat choralari, axborotni yoʻq qilish usullari
summary: Fizik xavfsizlikning axborot xavfsizligidagi oʻrni; tahdidlar — tabiiy (zilzila, yongʻin, suv toshqini, harorat), texnogen (elektr uzilishi, avariyalar) va inson tomonidan (oʻgʻirlik, buzgʻunchilik, ruxsatsiz kirish, ichki tahdid); himoya qatlamlari — hudud, bino, xona, qurilma; nazorat choralari — toʻsiq, nazorat, aniqlash, javob; kirishni nazorat qilish (kartalar, biometriya, shlyuz kamerasi), CCTV, signalizatsiya; server xonasi muhiti — harorat, namlik, yongʻin oʻchirish, UPS va generator; axborot tashuvchilarini yoʻq qilish — NIST SP 800-88 (Clear, Purge, Destroy).
---

## Nima uchun bu mavzu muhim?

Eng kuchli shifrlash va xavfsizlik devori ham serverni koʻtarib ketgan oʻgʻridan, server xonasini suv bosishidan yoki axlatga tashlangan qattiq diskdan himoya qilmaydi. Hujumchi uchun ofisga "kuryer" yoki "texnik" boʻlib kirib, USB qurilmani kompyuterga ulash koʻpincha tarmoqni masofadan buzishdan osonroq. Shuning uchun fizik xavfsizlik — himoyaning **birinchi qatlami**: unga kirish boʻlsa, qolgan barcha choralarni chetlab oʻtish mumkin.

:::note
**Bu darsdan soʻng siz:**

- fizik tahdidlarni tabiiy, texnogen va inson tomonidan turlarga ajrata olasiz;
- himoya qatlamlarini va nazorat choralarining vazifalarini tushuntira olasiz;
- server xonasi muhitiga qoʻyiladigan talablarni bilasiz;
- axborot tashuvchisi uchun mos yoʻq qilish usulini tanlay olasiz.
:::

## Fizik tahdidlar

| Tur | Misollar | Asosiy choralar |
|---|---|---|
| **Tabiiy** | zilzila (Oʻzbekiston seysmik faol hudud), suv toshqini, chaqmoq, haddan tashqari issiq yoki sovuq, chang | joy tanlash, seysmik mustahkamlash, poldan baland oʻrnatish, chaqmoqdan himoya, zaxira markaz |
| **Texnogen** | elektr uzilishi va kuchlanish sakrashi, yongʻin, quvur yorilishi, konditsioner ishdan chiqishi | UPS, generator, yongʻin signalizatsiyasi va oʻchirish tizimi, suv sizish datchiklari, zaxira sovutish |
| **Inson tomonidan** | oʻgʻirlik (noutbuk, disk), buzgʻunchilik, ruxsatsiz kirish, ergashib kirish (tailgating), ichki tahdid, ijtimoiy muhandislik | kirish nazorati, kuzatuv, qulflar, xodimlarni tekshirish, xabardorlik |

## Himoya qatlamlari va nazorat choralari

Fizik himoya **chuqurlashtirilgan** (defense in depth) quriladi: hudud perimetri (devor, darvoza, yoritish) → bino (qabulxona, turniket) → qavat va xona (karta, kod) → server shkafi (qulf) → qurilma (korpus qulfi, BIOS paroli, disk shifrlash).

| Vazifa | Misollar |
|---|---|
| **Toʻsiq** (deter) | ogohlantiruvchi belgilar, yoritish, koʻrinadigan kameralar, qoʻriqchi |
| **Toʻxtatish** (delay/deny) | devor, eshik, qulflar, turniket, **shlyuz kamerasi** (mantrap — bir vaqtda faqat bitta odam oʻtadi) |
| **Aniqlash** (detect) | harakat va ochilish datchiklari, CCTV, kirish jurnallari, signalizatsiya |
| **Javob** (respond) | qoʻriqlash xizmati, politsiyani chaqirish, hodisaga javob rejasi |

**Kirishni nazorat qilish**: proksimiti kartalar (RFID — nusxalanishi mumkin, shuning uchun himoyalangan kartalar afzal), PIN kod, biometriya (M10), ularning kombinatsiyasi; tashrifchilar roʻyxatga olinadi, belgi (badge) taqadi va kuzatuvchi bilan yuradi. **Ergashib kirish** (tailgating, piggybacking) — eshikni ochgan xodim ortidan kirish — eng koʻp uchraydigan zaiflik; himoyasi — turniket, shlyuz kamerasi va xodimlarning xabardorligi.

**Toza stol va qulflangan ekran**: hujjatlar qoldirilmaydi, kompyuterdan ketishda `Win + L`, ekranga parol yozilgan qogʻoz yopishtirilmaydi; noutbuklar kabel qulfi bilan mahkamlanadi va disklari shifrlanadi (BitLocker).

## Server xonasi va maʼlumotlar markazi muhiti

| Parametr | Talab |
|---|---|
| **Harorat** | ASHRAE tavsiyasi — 18–27 °C kirish havosi; issiq va sovuq yoʻlaklar |
| **Namlik** | juda past — statik elektr, juda yuqori — kondensat va korroziya; nazorat datchiklari |
| **Elektr taʼminoti** | UPS (qisqa uzilish va toza kuchlanish), generator (uzoq uzilish), ikki mustaqil kirish |
| **Yongʻin** | erta aniqlash (tutun datchiklari, VESDA); suvsiz gaz bilan oʻchirish (FM-200, Novec 1230, inert gazlar) — suv uskunani buzadi |
| **Suv** | xona quvurlar ostida boʻlmasin, poldan baland pol, sizish datchiklari |
| **Kirish** | faqat ruxsat etilgan xodimlar, ikki omilli kirish, jurnal va kamera |

Maʼlumotlar markazlarining ishonchliligi **Tier I–IV** darajalari bilan baholanadi (Uptime Institute): Tier I — zaxirasiz (~99,67%), Tier IV — barcha tizimlar ikkilangan va nosozlikka chidamli (~99,995%, yiliga ~26 daqiqa toʻxtash).

## Axborot tashuvchilarini yoʻq qilish

Faylni "oʻchirish" yoki diskni formatlash maʼlumotni yoʻq qilmaydi — faqat joyni "boʻsh" deb belgilaydi; maxsus dasturlar uni tiklaydi. Sotib olingan ishlatilgan disklarda bank hisob raqamlari va tibbiy maʼlumotlar topilgan holatlar koʻp. **NIST SP 800-88** uchta darajani belgilaydi:

| Daraja | Mohiyati | Usullar | Qachon |
|---|---|---|---|
| **Clear** | oddiy tiklash vositalaridan himoya | barcha manzilli joylarni qayta yozish (bir marta yetarli), zavod sozlamalariga qaytarish | tashuvchi tashkilot ichida qayta ishlatiladi |
| **Purge** | laboratoriya usullaridan ham himoya | ATA/NVMe Sanitize, Secure Erase, **kriptografik tozalash** (shifrlash kalitini yoʻq qilish), degausser (faqat magnit tashuvchilar) | tashuvchi tashkilotdan chiqadi |
| **Destroy** | tashuvchidan foydalanib boʻlmaydi | maydalash (shredder), parchalash, eritish, yoqish | tashuvchi utilizatsiya qilinadi yoki juda maxfiy maʼlumot |

**SSD va flesh-xotira** uchun qayta yozish ishonchsiz: kontroller yozuvlarni boshqa yacheykalarga taqsimlaydi (wear leveling) va zaxira bloklarni yashiradi. Ular uchun ishlab chiqaruvchining Sanitize buyrugʻi yoki kriptografik tozalash qoʻllanadi; degausser SSD ga taʼsir qilmaydi. Qogʻoz hujjatlar uchun DIN 66399 standartidagi maydalagichlar: maxfiy hujjatlar uchun kamida P-4 (kichik zarrachalar). Har bir yoʻq qilish **dalolatnoma** (sertifikat) bilan rasmiylashtiriladi.

## Ishlangan misol 1: server xonasi muhit monitoringi

Har bir satrda vaqt, harorat (°C) va nisbiy namlik (%). Oʻquv chegaralari: harorat > 32 — `KRITIK`; 27 dan yuqori yoki 15 dan past — `OGOHLANTIRISH`; namlik < 20 — statik elektr xavfi, > 80 — kondensat xavfi (`OGOHLANTIRISH`). Har bir oʻlchov holatini va oxirida jami sonlarni chiqaring.

```python
import sys

kritik = ogoh = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    vaqt, t_s, n_s = satr.split()
    t, n = float(t_s), float(n_s)
    sabablar = []
    daraja = 0                                    # 0 - norma, 1 - ogohlantirish, 2 - kritik
    if t > 32:
        sabablar.append("harorat")
        daraja = 2
    elif t > 27 or t < 15:
        sabablar.append("harorat")
        daraja = max(daraja, 1)
    if n < 20:
        sabablar.append("namlik past - statik elektr")
        daraja = max(daraja, 1)
    elif n > 80:
        sabablar.append("namlik yuqori - kondensat")
        daraja = max(daraja, 1)
    holat = ["NORMA", "OGOHLANTIRISH", "KRITIK"][daraja]
    kritik += daraja == 2
    ogoh += daraja == 1
    print(f"{vaqt} {t_s}C {n_s}%: {holat}" + (f" ({', '.join(sabablar)})" if sabablar else ""))
print(f"kritik: {kritik}, ogohlantirish: {ogoh}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] HOLAT = {"NORMA", "OGOHLANTIRISH", "KRITIK"};
        int kritik = 0, ogoh = 0;
        while (in.hasNext()) {
            String vaqt = in.next(), tS = in.next(), nS = in.next();
            double t = Double.parseDouble(tS), n = Double.parseDouble(nS);
            List<String> sabablar = new ArrayList<>();
            int daraja = 0;                                  // 0 - norma, 1 - ogohlantirish, 2 - kritik
            if (t > 32) {
                sabablar.add("harorat");
                daraja = 2;
            } else if (t > 27 || t < 15) {
                sabablar.add("harorat");
                daraja = Math.max(daraja, 1);
            }
            if (n < 20) {
                sabablar.add("namlik past - statik elektr");
                daraja = Math.max(daraja, 1);
            } else if (n > 80) {
                sabablar.add("namlik yuqori - kondensat");
                daraja = Math.max(daraja, 1);
            }
            if (daraja == 2) kritik++;
            if (daraja == 1) ogoh++;
            String qator = vaqt + " " + tS + "C " + nS + "%: " + HOLAT[daraja];
            if (!sabablar.isEmpty()) qator += " (" + String.join(", ", sabablar) + ")";
            System.out.println(qator);
        }
        System.out.println("kritik: " + kritik + ", ogohlantirish: " + ogoh);
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main() {
    const string HOLAT[] = {"NORMA", "OGOHLANTIRISH", "KRITIK"};
    int kritik = 0, ogoh = 0;
    string vaqt, tS, nS;
    while (cin >> vaqt >> tS >> nS) {
        double t = stod(tS), n = stod(nS);
        vector<string> sabablar;
        int daraja = 0;                                      // 0 - norma, 1 - ogohlantirish, 2 - kritik
        if (t > 32) {
            sabablar.push_back("harorat");
            daraja = 2;
        } else if (t > 27 || t < 15) {
            sabablar.push_back("harorat");
            daraja = max(daraja, 1);
        }
        if (n < 20) {
            sabablar.push_back("namlik past - statik elektr");
            daraja = max(daraja, 1);
        } else if (n > 80) {
            sabablar.push_back("namlik yuqori - kondensat");
            daraja = max(daraja, 1);
        }
        if (daraja == 2) kritik++;
        if (daraja == 1) ogoh++;
        string qator = vaqt + " " + tS + "C " + nS + "%: " + HOLAT[daraja];
        if (!sabablar.empty()) {
            qator += " (";
            for (size_t i = 0; i < sabablar.size(); i++) qator += (i ? ", " : "") + sabablar[i];
            qator += ")";
        }
        cout << qator << endl;
    }
    cout << "kritik: " << kritik << ", ogohlantirish: " << ogoh << endl;
    return 0;
}
```

Kirish:

```text
08:00 22.5 45
10:00 26.0 38
12:00 28.4 30
14:00 33.1 25
16:00 24.0 15
18:00 21.5 85
```

Chiqish:

```text
08:00 22.5C 45%: NORMA
10:00 26.0C 38%: NORMA
12:00 28.4C 30%: OGOHLANTIRISH (harorat)
14:00 33.1C 25%: KRITIK (harorat)
16:00 24.0C 15%: OGOHLANTIRISH (namlik past - statik elektr)
18:00 21.5C 85%: OGOHLANTIRISH (namlik yuqori - kondensat)
kritik: 1, ogohlantirish: 3
```

Harorat 10:00 dan 14:00 gacha ketma-ket oʻsgan — bu konditsioner ishdan chiqqanining belgisi: ogohlantirish bosqichida chora koʻrilsa (zaxira sovutish, yuklamani kamaytirish), serverlar avariyali oʻchmaydi. Haqiqiy tizimlar (SNMP datchiklari, DCIM) xuddi shunday qoidalar bilan SMS va email xabarnoma yuboradi.

## Ishlangan misol 2: yoʻq qilish usulini tanlash

Har bir satrda tashuvchi nomi, turi (HDD, SSD, PAPER), hajmi (GB), yozish tezligi (MB/s) va keyingi taqdiri: `ichki` — tashkilot ichida qayta ishlatiladi, `tashqi` — tashkilotdan chiqadi (sotiladi, taʼmirga beriladi), `utilizatsiya` — tashlab yuboriladi. NIST SP 800-88 gʻoyasi boʻyicha usulni tanlang; HDD ni Clear qilishda bir marta toʻliq qayta yozish vaqtini hisoblang.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, tur, hajm, tezlik, maqsad = satr.split()
    hajm, tezlik = float(hajm), float(tezlik)
    if tur == "PAPER":
        usul = "Destroy - kesib maydalash (P-4 va yuqori)"
    elif maqsad == "utilizatsiya":
        usul = "Destroy - maydalash yoki eritish"
    elif tur == "HDD" and maqsad == "ichki":
        soat = hajm * 1000 / tezlik / 3600            # GB -> MB, soniya -> soat
        usul = f"Clear - 1 marta qayta yozish, ~{soat:.1f} soat"
    elif tur == "HDD":
        usul = "Purge - degausser yoki ATA Secure Erase"
    else:                                             # SSD: qayta yozish ishonchsiz (wear leveling)
        usul = "Purge - kriptografik tozalash (crypto erase)"
    print(f"{nom} ({tur}, {maqsad}): {usul}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next(), tur = in.next();
            double hajm = Double.parseDouble(in.next()), tezlik = Double.parseDouble(in.next());
            String maqsad = in.next();
            String usul;
            if (tur.equals("PAPER")) {
                usul = "Destroy - kesib maydalash (P-4 va yuqori)";
            } else if (maqsad.equals("utilizatsiya")) {
                usul = "Destroy - maydalash yoki eritish";
            } else if (tur.equals("HDD") && maqsad.equals("ichki")) {
                double soat = hajm * 1000 / tezlik / 3600;        // GB -> MB, soniya -> soat
                usul = String.format("Clear - 1 marta qayta yozish, ~%.1f soat", soat);
            } else if (tur.equals("HDD")) {
                usul = "Purge - degausser yoki ATA Secure Erase";
            } else {                                              // SSD: qayta yozish ishonchsiz (wear leveling)
                usul = "Purge - kriptografik tozalash (crypto erase)";
            }
            System.out.println(nom + " (" + tur + ", " + maqsad + "): " + usul);
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    string nom, tur, maqsad;
    double hajm, tezlik;
    while (cin >> nom >> tur >> hajm >> tezlik >> maqsad) {
        string usul;
        if (tur == "PAPER") {
            usul = "Destroy - kesib maydalash (P-4 va yuqori)";
        } else if (maqsad == "utilizatsiya") {
            usul = "Destroy - maydalash yoki eritish";
        } else if (tur == "HDD" && maqsad == "ichki") {
            double soat = hajm * 1000 / tezlik / 3600;            // GB -> MB, soniya -> soat
            char buf[96];
            snprintf(buf, sizeof buf, "Clear - 1 marta qayta yozish, ~%.1f soat", soat);
            usul = buf;
        } else if (tur == "HDD") {
            usul = "Purge - degausser yoki ATA Secure Erase";
        } else {                                                  // SSD: qayta yozish ishonchsiz (wear leveling)
            usul = "Purge - kriptografik tozalash (crypto erase)";
        }
        cout << nom << " (" << tur << ", " << maqsad << "): " << usul << endl;
    }
    return 0;
}
```

Kirish:

```text
disk1 HDD 2000 150 ichki
disk2 HDD 1000 120 tashqi
ssd1 SSD 512 2000 ichki
ssd2 SSD 1000 3000 utilizatsiya
hujjat PAPER 0 0 utilizatsiya
disk3 HDD 500 120 ichki
```

Chiqish:

```text
disk1 (HDD, ichki): Clear - 1 marta qayta yozish, ~3.7 soat
disk2 (HDD, tashqi): Purge - degausser yoki ATA Secure Erase
ssd1 (SSD, ichki): Purge - kriptografik tozalash (crypto erase)
ssd2 (SSD, utilizatsiya): Destroy - maydalash yoki eritish
hujjat (PAPER, utilizatsiya): Destroy - kesib maydalash (P-4 va yuqori)
disk3 (HDD, ichki): Clear - 1 marta qayta yozish, ~1.2 soat
```

2 TB li diskni bir marta qayta yozish ~4 soat oladi; eski "7 marta" (DoD 5220.22-M) yoki "35 marta" (Gutmann) usullari zamonaviy disklar uchun keraksiz va vaqtni bekor sarflaydi. SSD uchun esa hatto qayta yozish ham har doim yetarli emas — shuning uchun bu yerda Purge tanlangan. Agar disk boshidanoq shifrlangan boʻlsa (BitLocker, FileVault), kalitni yoʻq qilish bir zumda butun diskni oʻqib boʻlmaydigan qiladi — bu kriptografik tozalashning gʻoyasi.

:::warning
**Koʻp uchraydigan xatolar**

- **Formatlashni yoʻq qilish deb hisoblash.** Tez formatlash maʼlumotni joyida qoldiradi; Clear, Purge yoki Destroy ni qoʻllang.
- **Ergashib kirishga yoʻl qoʻyish.** "Odob uchun" eshikni notanish odamga ushlab turish — fizik buzilishning eng oddiy yoʻli.
- **Server xonasini omborxona sifatida ishlatish.** Karton qutilar yongʻin xavfini va kirish nazoratini buzadi.
- **Zaxira nusxani asl tizim bilan bir xonada saqlash.** Yongʻin yoki suv ikkalasini birga yoʻq qiladi; nusxa boshqa binoda boʻlsin (3-2-1 qoidasi).
- **Topilgan USB qurilmani kompyuterga ulash.** Bu — hujumchilar ataylab qoldiradigan "oʻlja" boʻlishi mumkin.
:::

:::tip
Windows da `cipher /w:C:\` buyrugʻi diskdagi boʻsh joyni qayta yozib, ilgari "oʻchirilgan" fayllar qoldiqlarini yoʻq qiladi. Kompyuterni sotish yoki topshirishdan oldin: BitLocker yoqilgan boʻlsa — "Reset this PC" → "Remove everything" → "Clean the drive".
:::

## Xulosa

- Fizik xavfsizlik — himoyaning birinchi qatlami; tahdidlar tabiiy, texnogen va inson tomonidan boʻladi.
- Himoya qatlamlari: perimetr, bino, xona, shkaf, qurilma; vazifalar — toʻsiq, toʻxtatish, aniqlash, javob.
- Kirish nazorati: kartalar, PIN, biometriya, shlyuz kamerasi; ergashib kirish — asosiy zaiflik.
- Server xonasi: 18–27 °C, namlik nazorati, UPS va generator, gaz bilan yongʻin oʻchirish, suv datchiklari.
- NIST SP 800-88: Clear (ichki qayta foydalanish), Purge (tashkilotdan chiqish; SSD uchun Sanitize yoki crypto erase), Destroy (utilizatsiya).

## Oʻzingizni tekshiring

:::check
Q: Shlyuz kamerasi (mantrap) qanday tahdidga qarshi ishlatiladi?
A: Ergashib kirishga (tailgating) qarshi — bir vaqtda faqat bitta odam oʻtadi.
Q: Nima uchun server xonasida yongʻin suv bilan emas, gaz bilan oʻchiriladi?
A: Suv elektron uskunani buzadi; maxsus gazlar yongʻinni uskunaga zarar yetkazmasdan oʻchiradi.
Q: Juda past namlik server xonasi uchun qanday xavf tugʻdiradi?
A: Statik elektr toʻplanadi va zaryad elektron komponentlarni shikastlashi mumkin.
Q: Tashkilotdan chiqib ketadigan SSD uchun qaysi usul mos?
A: Purge — Sanitize buyrugʻi yoki kriptografik tozalash; oddiy qayta yozish wear leveling tufayli ishonchsiz.
Q: Diskni tez formatlash maʼlumotni yoʻq qiladimi?
A: Yoʻq, faqat joyni boʻsh deb belgilaydi; maʼlumotni maxsus dasturlar tiklay oladi.
:::

:::link https://csrc.nist.gov/pubs/sp/800/88/r1/final
NIST SP 800-88 Rev. 1: Guidelines for Media Sanitization
Axborot tashuvchilarini tozalash va yoʻq qilish boʻyicha qoʻllanma (ingliz tilida).
:::
