---
title: Token Ring va boshqa texnologiyalar. Token Ring, 100VG-AnyLAN, FDDI, Wi-Fi texnologiyalari
summary: Muhitga kirishning deterministik (marker) va tasodifiy usullari; Token Ring (IEEE 802.5) — marker, MAU, faol monitor, ustuvorliklar; FDDI — ikki optik halqa va nosozlikda halqani yopish; 100VG-AnyLAN (IEEE 802.12) — talab boʻyicha ustuvorlik; Wi-Fi (IEEE 802.11) — standartlar, chastotalar, CSMA/CA, yashirin stansiya muammosi va RTS/CTS, BSS va ESS; texnologiyalarni solishtirish.
---

## Nima uchun bu mavzu muhim?

Ethernet doim ham gʻolib boʻlmagan. 1980–1990-yillarda banklar va zavodlar **Token Ring** ni tanlagan: unda kolliziyalar yoʻq va har bir kompyuter uzatish navbati qachon kelishini aniq biladi. Universitet magistrallari **FDDI** optik halqalarida qurilgan. Bu texnologiyalar bugun deyarli ishlatilmaydi, lekin ularning gʻoyalari — marker bilan kirish, ikki halqa bilan zaxiralash, ustuvorliklar — sanoat tarmoqlarida va Wi-Fi da yashab kelmoqda. Wi-Fi esa bugun eng ommaviy kirish texnologiyasi.

:::note
**Bu darsdan soʻng siz:**

- muhitga tasodifiy va deterministik kirish usullarini farqlay olasiz;
- Token Ring da marker qanday aylanishini va kim qachon uzatishini tushuntira olasiz;
- FDDI ning ikki halqali zaxiralash usulini va 100VG-AnyLAN ning talab boʻyicha ustuvorlik gʻoyasini ayta olasiz;
- Wi-Fi standartlari, chastota diapazonlari va CSMA/CA usulini tushuntira olasiz;
- yashirin stansiya muammosini va RTS/CTS yechimini izohlay olasiz.
:::

## Muhitga kirish usullari

| Usul | Gʻoya | Afzalligi | Kamchiligi | Misol |
|---|---|---|---|---|
| **Tasodifiy** (contention) | xohlagan stansiya kanal boʻsh boʻlsa uzatadi | yuklama kam boʻlsa — kechikish minimal, oddiy | yuklama oshsa — kolliziyalar, kechikish oldindan aytib boʻlmaydi | Ethernet (CSMA/CD), Wi-Fi (CSMA/CA) |
| **Deterministik** (marker) | faqat "marker" (ruxsatnoma) olgan stansiya uzatadi | kolliziya yoʻq, eng katta kutish vaqti maʼlum | marker aylanishi uchun sarf; marker yoʻqolishi muammosi | Token Ring, FDDI |
| **Soʻrov** (polling) | markaziy qurilma stansiyalarni navbat bilan soʻraydi | ustuvorliklarni boshqarish oson | markaz — yagona nosozlik nuqtasi | 100VG-AnyLAN, Bluetooth |

## Token Ring (IEEE 802.5)

IBM tomonidan ishlab chiqilgan, 4 va 16 Mbit/s tezlikda ishlaydi. Mantiqiy topologiyasi — **halqa**, fizik topologiyasi esa **yulduz**: kompyuterlar **MAU** (Multistation Access Unit) qurilmasiga ulanadi, MAU ichida halqa hosil boʻladi. Kompyuter oʻchsa, MAU uning portini aylanib oʻtadi va halqa uzilmaydi.

Ishlash tamoyili:

1. Halqa boʻylab maxsus qisqa kadr — **marker** (token) aylanadi.
2. Uzatmoqchi boʻlgan stansiya markerni ushlab, uni maʼlumot kadriga aylantiradi va uzatadi. Markerni ushlab turish vaqti cheklangan (**THT**, token holding time — odatda 10 ms).
3. Kadr halqani aylanib chiqadi: qabul qiluvchi uni nusxalab, "qabul qilindi" belgisini qoʻyadi.
4. Kadr yuboruvchiga qaytgach, u kadrni halqadan olib, yangi markerni keyingi stansiyaga uzatadi.

Qoʻshimcha mexanizmlar: **faol monitor** (bitta stansiya markerning yoʻqolganini yoki "abadiy aylanuvchi" kadrlarni kuzatadi), **ustuvorliklar** (marker 8 darajali ustuvorlikka ega), **erta marker chiqarish** (16 Mbit/s da — kadr qaytishini kutmasdan marker chiqariladi). Kodlash — **differensial Manchester**.

## FDDI

**FDDI** (Fiber Distributed Data Interface, ANSI X3T9.5) — 100 Mbit/s tezlikdagi, marker usulida ishlaydigan optik tarmoq. Uzunligi 200 km gacha, 500 tagacha stansiya. Asosiy xususiyati — **ikki qarama-qarshi yoʻnalishdagi halqa**: birlamchi (ishchi) va ikkilamchi (zaxira).

Agar kabel uzilsa yoki stansiya ishdan chiqsa, uzilish yonidagi ikki stansiya birlamchi halqani ikkilamchiga ulab qoʻyadi (**wrap**) — natijada ikki halqadan bitta uzun halqa hosil boʻladi va tarmoq ishlashda davom etadi. FDDI kampus va shahar magistrallari uchun ishlatilgan; uning mis kabeldagi varianti — CDDI. Kodlash: 4B/5B + NRZI.

## 100VG-AnyLAN (IEEE 802.12)

Hewlett-Packard taklif qilgan 100 Mbit/s texnologiya. Kirish usuli — **talab boʻyicha ustuvorlik** (demand priority): konsentrator portlarni navbat bilan soʻraydi, stansiya uzatishga soʻrov yuboradi; soʻrovlar **oddiy** va **yuqori** ustuvorlikda boʻladi (masalan, video uchun). Kolliziyalar yoʻq. Cat3 kabelning 4 juftligi ishlatilgan ("VG" — voice grade). Ethernet va Token Ring kadrlarini ham tashiy olgan ("AnyLAN"). Texnik jihatdan yaxshi boʻlsa-da, arzonroq **Fast Ethernet** bilan raqobatda yutqazgan.

## Wi-Fi (IEEE 802.11)

| Standart | Avlod | Yil | Chastota | Eng katta tezlik |
|---|---|---|---|---|
| 802.11b | — | 1999 | 2,4 GHz | 11 Mbit/s |
| 802.11a | — | 1999 | 5 GHz | 54 Mbit/s |
| 802.11g | — | 2003 | 2,4 GHz | 54 Mbit/s |
| 802.11n | Wi-Fi 4 | 2009 | 2,4 / 5 GHz | 600 Mbit/s |
| 802.11ac | Wi-Fi 5 | 2013 | 5 GHz | ~6,9 Gbit/s |
| 802.11ax | Wi-Fi 6 / 6E | 2019 / 2021 | 2,4 / 5 / 6 GHz | ~9,6 Gbit/s |
| 802.11be | Wi-Fi 7 | 2024 | 2,4 / 5 / 6 GHz | ~46 Gbit/s |

**Tuzilishi**: **BSS** (Basic Service Set) — bitta kirish nuqtasi va unga ulangan qurilmalar; **ESS** (Extended Service Set) — bir xil **SSID** (tarmoq nomi) bilan ishlaydigan bir nechta kirish nuqtalari, foydalanuvchi ular orasida uzilmasdan oʻtadi (roaming); **ad hoc** (IBSS) — kirish nuqtasisiz, qurilmalar toʻgʻridan-toʻgʻri.

**CSMA/CA** (Collision Avoidance): radioda yuboruvchi oʻz signalini "qichqirib" turib boshqalarni eshita olmaydi, shuning uchun kolliziyani **aniqlash** emas, **oldini olish** kerak:

1. Kanalni tinglash; boʻsh boʻlsa ham **DIFS** vaqtini kutish.
2. Tasodifiy **backoff** hisoblagichini kutish (kanal band boʻlsa, hisoblagich toʻxtaydi).
3. Kadrni uzatish; qabul qiluvchi **ACK** bilan tasdiqlaydi. ACK kelmasa — kolliziya yoki xato deb hisoblanib, kadr qayta yuboriladi.

**Yashirin stansiya muammosi**: A va C bir-birini eshitmaydi (devor yoki masofa), lekin ikkalasi ham B kirish nuqtasini eshitadi. Ikkalasi bir vaqtda uzatsa, B da toʻqnashuv boʻladi. Yechim — **RTS/CTS**: A "uzatmoqchiman" (RTS) deydi, B hammaga eshitiladigan "A ga ruxsat" (CTS) javobini beradi, C esa koʻrsatilgan vaqt davomida jim turadi.

## Ishlangan misol: marker halqasini modellashtirish

Halqada *n* ta stansiya (1 dan *n* gacha), har birida uzatishni kutayotgan kadrlar soni berilgan. Marker 1-stansiyadan boshlab tartib bilan aylanadi; marker kelganda stansiya koʻpi bilan *h* ta kadr uzatadi (THT cheklovi). Har bir aylanishda kim nechta kadr uzatganini va jami aylanishlar sonini chiqaring.

```python
n, h = map(int, input().split())
navbat = list(map(int, input().split()))
aylana = 0
while sum(navbat) > 0:
    aylana += 1
    qismlar = []
    for i in range(n):                          # marker 1 dan n gacha aylanadi
        yubordi = min(navbat[i], h)             # THT: koʻpi bilan h ta kadr
        if yubordi > 0:
            navbat[i] -= yubordi
            qismlar.append("S" + str(i + 1) + "=" + str(yubordi))
    print("aylana " + str(aylana) + ": " + " ".join(qismlar))
print("jami aylanalar: " + str(aylana))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int h = in.nextInt();
        int[] navbat = new int[n];
        int qolgan = 0;
        for (int i = 0; i < n; i++) {
            navbat[i] = in.nextInt();
            qolgan += navbat[i];
        }
        int aylana = 0;
        while (qolgan > 0) {
            aylana++;
            StringBuilder qismlar = new StringBuilder();
            for (int i = 0; i < n; i++) {                   // marker 1 dan n gacha aylanadi
                int yubordi = Math.min(navbat[i], h);       // THT: koʻpi bilan h ta kadr
                if (yubordi > 0) {
                    navbat[i] -= yubordi;
                    qolgan -= yubordi;
                    if (qismlar.length() > 0) qismlar.append(" ");
                    qismlar.append("S" + (i + 1) + "=" + yubordi);
                }
            }
            System.out.println("aylana " + aylana + ": " + qismlar);
        }
        System.out.println("jami aylanalar: " + aylana);
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
    int n, h;
    cin >> n >> h;
    vector<int> navbat(n);
    int qolgan = 0;
    for (int i = 0; i < n; i++) {
        cin >> navbat[i];
        qolgan += navbat[i];
    }
    int aylana = 0;
    while (qolgan > 0) {
        aylana++;
        string qismlar;
        for (int i = 0; i < n; i++) {                       // marker 1 dan n gacha aylanadi
            int yubordi = min(navbat[i], h);                // THT: koʻpi bilan h ta kadr
            if (yubordi > 0) {
                navbat[i] -= yubordi;
                qolgan -= yubordi;
                if (!qismlar.empty()) qismlar += " ";
                qismlar += "S" + to_string(i + 1) + "=" + to_string(yubordi);
            }
        }
        cout << "aylana " << aylana << ": " << qismlar << endl;
    }
    cout << "jami aylanalar: " << aylana << endl;
    return 0;
}
```

Kirish (4 stansiya, *h* = 2):

```text
4 2
3 0 5 1
```

Chiqish:

```text
aylana 1: S1=2 S3=2 S4=1
aylana 2: S1=1 S3=2
aylana 3: S3=1
jami aylanalar: 3
```

Muhim xulosa: har bir stansiya marker qachon kelishini aniq biladi — eng yomon holatda u boshqa *n* − 1 stansiyaning har biri *h* tadan kadr uzatishini kutadi. Ethernet da bunday kafolat yoʻq: omadsiz stansiya kolliziyalar sababli istalgancha kutishi mumkin. Shuning uchun deterministik usullar real vaqt tizimlarida (zavod avtomatikasi, avtomobillar) qadrlanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Token Ring ni fizik halqa deb hisoblash.** Kabellar MAU ga yulduz shaklida ulanadi; halqa — mantiqiy.
- **Wi-Fi da CSMA/CD ishlaydi deb aytish.** Radio stansiya uzatish paytida kolliziyani aniqlay olmaydi — shuning uchun CSMA/**CA** va ACK tasdiqlari ishlatiladi.
- **FDDI ning ikkala halqasi ham doim maʼlumot tashiydi deb oʻylash.** Odatda ikkilamchi halqa zaxira boʻlib, faqat nosozlikda ishga tushadi.
- **Wi-Fi standartidagi eng katta tezlikni real tezlik deb hisoblash.** Bu nazariy chegara barcha antennalar, eng keng kanal va ideal sharoit uchun; amalda u bir necha baravar kam.
:::

:::tip
Telefoningiz yoki noutbukingizda Wi-Fi tarmoq maʼlumotlarini oching: qaysi standart (Wi-Fi 4/5/6), qaysi chastota (2,4 yoki 5 GHz) va qaysi kanal ishlatilyapti? Windows da buni `netsh wlan show interfaces` buyrugʻi koʻrsatadi.
:::

## Xulosa

- Kirish usullari: tasodifiy (Ethernet, Wi-Fi), deterministik marker (Token Ring, FDDI), soʻrov (100VG-AnyLAN).
- Token Ring: 4/16 Mbit/s, mantiqiy halqa va fizik yulduz (MAU), marker, THT, faol monitor, ustuvorliklar.
- FDDI: 100 Mbit/s optik ikki halqa; nosozlikda halqalar bitta halqaga ulanadi (wrap).
- 100VG-AnyLAN: talab boʻyicha ustuvorlik, kolliziyasiz, lekin Fast Ethernet bilan raqobatda yutqazgan.
- Wi-Fi: 802.11b/a/g/n/ac/ax/be, 2,4/5/6 GHz, CSMA/CA va ACK, yashirin stansiya muammosi va RTS/CTS, BSS/ESS/SSID.

## Oʻzingizni tekshiring

:::check
Q: Token Ring da stansiya qachon maʼlumot uzata oladi?
A: Faqat marker (token) unga kelganda va markerni ushlab turish vaqti (THT) chegarasida.
Q: FDDI tarmogʻida kabel uzilsa nima sodir boʻladi?
A: Uzilish yonidagi stansiyalar birlamchi halqani ikkilamchiga ulaydi (wrap) va bitta uzun halqa hosil boʻlib, tarmoq ishlashda davom etadi.
Q: Nima uchun Wi-Fi CSMA/CD emas, CSMA/CA ishlatadi?
A: Radio stansiya uzatish paytida boshqa signallarni eshita olmaydi, kolliziyani aniqlash imkonsiz — shuning uchun uning oldi olinadi va ACK bilan tasdiqlanadi.
Q: Yashirin stansiya muammosi nima va u qanday hal qilinadi?
A: Ikki stansiya bir-birini eshitmaydi, lekin umumiy kirish nuqtasini eshitadi va bir vaqtda uzatib, toʻqnashadi; RTS/CTS almashinuvi bilan hal qilinadi.
Q: Wi-Fi 6 qaysi IEEE standartiga mos keladi?
A: IEEE 802.11ax.
:::

:::link https://en.wikipedia.org/wiki/IEEE_802.11
IEEE 802.11
Wi-Fi standartlari, chastotalar, CSMA/CA va kadr turlari haqida batafsil maqola (ingliz tilida).
:::
