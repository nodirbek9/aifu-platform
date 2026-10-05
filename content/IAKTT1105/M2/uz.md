---
title: Axborot texnologiyalarining infratuzilmasi
summary: AT infratuzilmasi tushunchasi va tarkibi; kompyuter arxitekturasi — protsessor, operativ xotira, doimiy xotira (HDD, SSD), kiritish-chiqarish qurilmalari; axborot oʻlchov birliklari; serverlar va maʼlumotlar markazlari; tarmoq infratuzilmasi; tizim va amaliy dasturiy taʼminot qatlamlari; oʻz infratuzilmasi va bulut — CAPEX va OPEX; umumiy egalik qiymati (TCO); infratuzilmani rejalashtirishda iqtisodiy mezonlar; TCO ni hisoblash dasturi.
---

## Nima uchun bu mavzu muhim?

Har qanday raqamli xizmat — onlayn kassa, bank ilovasi, buxgalteriya dasturi — ortida apparat, tarmoq va dasturlardan iborat **infratuzilma** turadi. Iqtisodchi uchun bu nafaqat texnik, balki moliyaviy masala: qancha kompyuter va server kerak, ularni sotib olish yoki bulutdan ijaraga olish foydalimi, yillar davomida umumiy xarajat qancha boʻladi? Toʻgʻri qaror infratuzilma xarajatlarini sezilarli tejaydi.

:::note
**Bu darsdan soʻng siz:**

- AT infratuzilmasining tarkibiy qismlarini ayta olasiz;
- kompyuterning asosiy qurilmalari va ularning vazifasini tushuntira olasiz;
- axborot oʻlchov birliklaridan foydalana olasiz;
- oʻz infratuzilmasi va bulutni CAPEX/OPEX nuqtai nazaridan solishtira olasiz;
- umumiy egalik qiymatini (TCO) hisoblay olasiz.
:::

## AT infratuzilmasi tarkibi

| Qatlam | Tarkibi |
|---|---|
| **Apparat** | ish kompyuterlari, noutbuklar, serverlar, saqlash tizimlari, printerlar, kassa apparatlari, mobil qurilmalar |
| **Tarmoq** | kabellar, Wi-Fi nuqtalari, kommutatorlar, routerlar, Internet kanali (M6) |
| **Tizim dasturlari** | operatsion tizimlar, virtualizatsiya, maʼlumotlar bazasi boshqaruv tizimlari (S3, M7) |
| **Amaliy dasturlar** | ofis paketlari, buxgalteriya, ERP, CRM (M3) |
| **Maʼlumotlar markazi** | server xonasi yoki tashqi maʼlumotlar markazi: elektr taʼminoti, sovutish, xavfsizlik |
| **Xizmatlar va odamlar** | texnik yordam, administratorlar, yangilash va zaxira jarayonlari |

## Kompyuter arxitekturasi

| Qurilma | Vazifasi | Asosiy koʻrsatkich |
|---|---|---|
| **Protsessor** (CPU) | buyruqlarni bajaradi, hisoblaydi | yadrolar soni, chastota (GHz) |
| **Operativ xotira** (RAM) | ishlayotgan dastur va maʼlumotlarni vaqtincha saqlaydi; elektr oʻchsa tozalanadi | hajm (GB) |
| **Doimiy xotira** | fayllarni uzoq saqlaydi: HDD (arzon, sekin), SSD (tez, chidamli) | hajm (TB), tezlik |
| **Kiritish qurilmalari** | klaviatura, sichqoncha, skaner, shtrix-kod oʻquvchi (S2) | — |
| **Chiqarish qurilmalari** | monitor, printer, chek printeri (S2) | aniqlik, tezlik |
| **Ona plata va shina** | qurilmalarni bogʻlaydi | — |

**Axborot oʻlchov birliklari**: 1 bayt = 8 bit; 1 KB ≈ 10³ bayt, 1 MB ≈ 10⁶, 1 GB ≈ 10⁹, 1 TB ≈ 10¹² bayt (ikkilik birliklarda 1 KiB = 1024 bayt). Tarmoq tezligi **bit** da oʻlchanadi (Mbit/s), fayl hajmi — **bayt** da: 100 Mbit/s kanal sekundiga taxminan 12,5 MB uzatadi.

## Serverlar va maʼlumotlar markazlari

**Server** — boshqa kompyuterlarga xizmat koʻrsatuvchi kuchli, ishonchli kompyuter (fayl, pochta, maʼlumotlar bazasi, veb-server). Bir fizik serverda **virtualizatsiya** yordamida bir nechta virtual server ishlashi mumkin — bu apparatdan samaraliroq foydalanish imkonini beradi.

**Maʼlumotlar markazi** — serverlar uchun maxsus bino yoki xona: uzluksiz elektr taʼminoti, sovutish, yongʻin oʻchirish, fizik himoya. Kichik korxona uchun oʻz maʼlumotlar markazini qurish qimmat, shuning uchun koʻpincha **kolokatsiya** (oʻz serverini tashqi markazga qoʻyish) yoki **bulut** (M11) tanlanadi.

## CAPEX, OPEX va umumiy egalik qiymati

| Model | Xarajat turi | Afzalligi | Kamchiligi |
|---|---|---|---|
| **Oʻz infratuzilmasi** (on-premise) | **CAPEX** — boshida katta kapital xarajat (xarid), keyin texnik xizmat | toʻliq nazorat, uzoq muddatda arzon boʻlishi mumkin | katta boshlangʻich sarmoya, xodimlar kerak, eskirish |
| **Bulut** | **OPEX** — har oy foydalanish uchun toʻlov | tez boshlash, kerak boʻlganda kengaytirish, boshlangʻich sarmoya yoʻq | uzoq muddatda qimmatlashishi mumkin, provayderga bogʻliqlik |

**Umumiy egalik qiymati (TCO)** — infratuzilmaning butun muddatidagi barcha xarajatlari: xarid, oʻrnatish, litsenziyalar, elektr energiyasi, texnik xizmat, xodimlar ish haqi, taʼmirlash va utilizatsiya. Faqat xarid narxiga qarab qaror qabul qilish — keng tarqalgan xato.

## Ishlangan misol: server yoki bulut?

Server xarid narxi, yillik texnik xizmat xarajati, bulutning oylik toʻlovi va yillar soni berilgan. Har yil oxiridagi jamgʻarilgan xarajatlarni chiqaring va oʻz serveri birinchi marta bulutdan arzon boʻladigan yilni (qoplanish yilini) toping.

```python
import sys

xarid, xizmat, bulut_oy, yillar = map(int, sys.stdin.read().split())
server_jami, bulut_jami = xarid, 0              # server boshida sotib olinadi
qoplanish = 0
for y in range(1, yillar + 1):
    server_jami += xizmat                        # har yilgi texnik xizmat
    bulut_jami += bulut_oy * 12                  # 12 oylik toʻlov
    if qoplanish == 0 and server_jami < bulut_jami:
        qoplanish = y
    print(f"yil {y}: server {server_jami}, bulut {bulut_jami}")
print(f"qoplanish yili: {qoplanish}" if qoplanish else "server qoplanmaydi")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long xarid = in.nextLong(), xizmat = in.nextLong(), bulutOy = in.nextLong();
        int yillar = in.nextInt();
        long serverJami = xarid, bulutJami = 0;          // server boshida sotib olinadi
        int qoplanish = 0;
        for (int y = 1; y <= yillar; y++) {
            serverJami += xizmat;                          // har yilgi texnik xizmat
            bulutJami += bulutOy * 12;                     // 12 oylik toʻlov
            if (qoplanish == 0 && serverJami < bulutJami) qoplanish = y;
            System.out.println("yil " + y + ": server " + serverJami + ", bulut " + bulutJami);
        }
        System.out.println(qoplanish > 0 ? "qoplanish yili: " + qoplanish : "server qoplanmaydi");
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    long long xarid, xizmat, bulutOy;
    int yillar;
    cin >> xarid >> xizmat >> bulutOy >> yillar;
    long long serverJami = xarid, bulutJami = 0;          // server boshida sotib olinadi
    int qoplanish = 0;
    for (int y = 1; y <= yillar; y++) {
        serverJami += xizmat;                              // har yilgi texnik xizmat
        bulutJami += bulutOy * 12;                         // 12 oylik toʻlov
        if (qoplanish == 0 && serverJami < bulutJami) qoplanish = y;
        cout << "yil " << y << ": server " << serverJami << ", bulut " << bulutJami << endl;
    }
    if (qoplanish > 0) cout << "qoplanish yili: " << qoplanish << endl;
    else cout << "server qoplanmaydi" << endl;
    return 0;
}
```

Kirish:

```text
60000000 8000000 3000000 5
```

Chiqish:

```text
yil 1: server 68000000, bulut 36000000
yil 2: server 76000000, bulut 72000000
yil 3: server 84000000, bulut 108000000
yil 4: server 92000000, bulut 144000000
yil 5: server 100000000, bulut 180000000
qoplanish yili: 3
```

Bulut oyiga 3 mln soʻm boʻlsa, 60 mln soʻmlik server uchinchi yildan foydaliroq. Agar bulut oyiga 1,5 mln soʻm boʻlsa (`60000000 8000000 1500000 5`), 5 yil davomida server qoplanmaydi — bulut arzonroq. Haqiqiy hisobda server 4–5 yilda eskirishi, elektr energiyasi, xodim ish haqi, pulning vaqt qiymati (diskontlash) va bulutning moslashuvchanligi ham hisobga olinadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat xarid narxini solishtirish.** TCO ga texnik xizmat, elektr, xodimlar va eskirish kiradi.
- **Bit va baytni aralashtirish.** 100 Mbit/s — sekundiga 100 megabayt emas, taxminan 12,5 megabayt.
- **Operativ xotira va doimiy xotirani adashtirish.** RAM elektr oʻchganda tozalanadi; fayllar diskda saqlanadi.
- **Zaxira va uzluksiz elektr taʼminotini rejalashtirmaslik.** Infratuzilma toʻxtashi biznesga toʻgʻridan-toʻgʻri zarar.
:::

:::tip
Oʻz kompyuteringizda Task Manager → Performance (Windows) oynasida protsessor, xotira va disk koʻrsatkichlarini koʻring; ularni ish yuklamasi bilan solishtirib, kompyuter vazifalarga yetarlimi yoki yangilash kerakmi — xulosa qiling.
:::

## Xulosa

- AT infratuzilmasi: apparat, tarmoq, tizim va amaliy dasturlar, maʼlumotlar markazi, xizmatlar va odamlar.
- Kompyuter: protsessor, operativ va doimiy xotira, kiritish-chiqarish qurilmalari.
- Oʻlchov birliklari: bit va bayt, KB–TB; tarmoq tezligi bitda, fayl hajmi baytda.
- Oʻz infratuzilmasi — CAPEX, bulut — OPEX; qaror TCO asosida qabul qilinadi.
- TCO barcha xarajatlarni butun muddat davomida hisobga oladi.

## Oʻzingizni tekshiring

:::check
Q: Operativ xotira doimiy xotiradan nimasi bilan farq qiladi?
A: Operativ xotira maʼlumotni vaqtincha saqlaydi va elektr oʻchganda tozalanadi; doimiy xotira fayllarni uzoq saqlaydi.
Q: 100 Mbit/s kanal sekundiga taxminan necha megabayt uzatadi?
A: Taxminan 12,5 MB (100 / 8).
Q: CAPEX va OPEX ning farqi nima?
A: CAPEX — boshlangʻich kapital xarajat (xarid); OPEX — doimiy operatsion xarajat (masalan, oylik toʻlov).
Q: TCO ga qanday xarajatlar kiradi?
A: Xarid, oʻrnatish, litsenziyalar, elektr energiyasi, texnik xizmat, xodimlar, taʼmirlash va utilizatsiya.
:::

:::link https://www.gartner.com/en/information-technology/glossary/total-cost-of-ownership-tco
Gartner: Total Cost of Ownership (TCO)
Umumiy egalik qiymati tushunchasining taʼrifi (ingliz tilida).
:::
