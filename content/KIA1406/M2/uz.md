---
title: Risklarni boshqarish. Risk darajasi va chastotasi, risk matritsasi, Risk Management Information Systems (RMIS)
summary: Risklarni boshqarish jarayoni — aktivlarni aniqlash, tahdid va zaifliklarni tahlil qilish, baholash, munosabat bildirish, monitoring; sifat va miqdoriy baholash; risk matritsasi (ehtimollik × taʼsir); miqdoriy koʻrsatkichlar — AV, EF, SLE, ARO, ALE; himoya chorasining iqtisodiy samarasi; riskka munosabat strategiyalari; risklar reyestri va RMIS; ISO/IEC 27005 va NIST SP 800-30.
---

## Nima uchun bu mavzu muhim?

Hech bir tashkilot barcha tahdidlardan toʻliq himoyalana olmaydi: pul, vaqt va mutaxassislar cheklangan. Shuning uchun asosiy savol — "nimani birinchi navbatda himoyalash kerak va bunga qancha sarflash oqilona?". Masalan, 10 million soʻmlik noutbukni yoʻqotish riskiga qarshi 50 million soʻmlik tizim sotib olish — xato. **Risklarni boshqarish** — bu qarorlarni taxmin bilan emas, hisob-kitob bilan qabul qilish usuli.

:::note
**Bu darsdan soʻng siz:**

- risklarni boshqarish jarayonining bosqichlarini tushuntira olasiz;
- sifat (risk matritsasi) va miqdoriy (ALE) baholashni farqlay olasiz;
- SLE, ARO va ALE ni hisoblab, himoya chorasining iqtisodiy samarasini aniqlay olasiz;
- riskka munosabatning toʻrt strategiyasini tanlay olasiz;
- risklar reyestri va RMIS vazifasini tushuntira olasiz.
:::

## Risklarni boshqarish jarayoni

1. **Kontekstni belgilash** — tashkilot maqsadlari, qonun talablari, riskni qabul qilish chegarasi (risk appetite).
2. **Aktivlarni aniqlash** — nimalar qimmatli va qancha turadi (maʼlumotlar bazasi, serverlar, obroʻ).
3. **Tahdid va zaifliklarni aniqlash** — kim yoki nima zarar keltirishi mumkin va qaysi kamchiliklar orqali.
4. **Riskni tahlil qilish va baholash** — ehtimollik va taʼsirni aniqlash, risklarni tartiblash.
5. **Riskka munosabat** — choralarni tanlash va joriy etish.
6. **Monitoring va qayta koʻrib chiqish** — risklar oʻzgaradi: yangi tizim, yangi tahdid, yangi qonun.
7. **Kommunikatsiya** — rahbariyat va xodimlarni xabardor qilish.

Standartlar: **ISO/IEC 27005** (axborot xavfsizligi risklarini boshqarish), **ISO 31000** (umumiy risk boshqaruvi), **NIST SP 800-30** (risklarni baholash qoʻllanmasi).

## Sifat baholash: risk matritsasi

Ehtimollik va taʼsir 1–5 shkalada baholanadi; **risk darajasi = ehtimollik × taʼsir**.

| Ehtimollik \ Taʼsir | 1 juda kam | 2 kam | 3 oʻrta | 4 katta | 5 halokatli |
|---|---|---|---|---|---|
| **5 deyarli muqarrar** | 5 | 10 | 15 | 20 | 25 |
| **4 ehtimolli** | 4 | 8 | 12 | 16 | 20 |
| **3 mumkin** | 3 | 6 | 9 | 12 | 15 |
| **2 kam ehtimolli** | 2 | 4 | 6 | 8 | 10 |
| **1 juda kam ehtimolli** | 1 | 2 | 3 | 4 | 5 |

Hujayralar rang bilan belgilanadi (yashil — past, sariq — oʻrta, toʻq sariq — yuqori, qizil — kritik); chegaralarni tashkilot risk siyosatida oʻzi belgilaydi. Afzalligi — tez va tushunarli; kamchiligi — subyektiv, pul bilan oʻlchanmaydi. Risk matritsasini qurish va risklarni tartiblash — A2 amaliy mashgʻulotining topshirigʻi.

## Miqdoriy baholash

| Koʻrsatkich | Formula | Maʼnosi |
|---|---|---|
| **AV** (Asset Value) | — | aktiv qiymati |
| **EF** (Exposure Factor) | 0–100% | bitta hodisada aktivning qancha qismi yoʻqoladi |
| **SLE** (Single Loss Expectancy) | AV × EF | bitta hodisadan kutiladigan zarar |
| **ARO** (Annualized Rate of Occurrence) | — | yiliga necha marta sodir boʻlishi kutiladi (0,1 — 10 yilda bir marta) |
| **ALE** (Annualized Loss Expectancy) | SLE × ARO | yillik kutiladigan zarar |

**Himoya chorasining samarasi**: foyda = ALE (choragacha) − ALE (choradan keyin) − choraning yillik narxi. Foyda musbat boʻlsa — chora iqtisodiy jihatdan oʻzini oqlaydi.

## Riskka munosabat strategiyalari

| Strategiya | Mazmuni | Misol |
|---|---|---|
| **Kamaytirish** (mitigate) | chora koʻrib, ehtimollik yoki taʼsirni kamaytirish | antivirus, zaxira nusxa, oʻqitish |
| **Oʻtkazish** (transfer) | moliyaviy oqibatni boshqaga yuklash | kibersugʻurta, autsorsing |
| **Qochish** (avoid) | riskni keltirib chiqaruvchi faoliyatdan voz kechish | eski xavfli xizmatni oʻchirish |
| **Qabul qilish** (accept) | risk chegarada — hech narsa qilmaslik, lekin hujjatlashtirish | arzon jihozlar yoʻqolishi |

Choralardan keyin ham qoladigan risk — **qoldiq risk** (residual risk). Uni rahbariyat rasman qabul qiladi.

## Risklar reyestri va RMIS

**Risklar reyestri** — barcha risklar jadvali: identifikator, tavsif, egasi (masʼul shaxs), ehtimollik, taʼsir, daraja, choralar, holat, keyingi koʻrib chiqish sanasi.

**RMIS** (Risk Management Information System) — risklar reyestrini yuritish, hodisalarni qayd etish, hisobotlar va vizualizatsiya (issiqlik xaritalari, trendlar) uchun dasturiy tizim. U turli boʻlimlardan maʼlumot yigʻadi, risk egalariga eslatma yuboradi, rahbariyat uchun boshqaruv panellarini tayyorlaydi. Misollar: GRC platformalari (ServiceNow GRC, RSA Archer), kichik tashkilotlarda — jadval va oddiy veb-ilovalar.

## Ishlangan misol: yillik zarar va choraning samarasi

Birinchi satrda risklar soni *n*. Keyingi satrlarda: nom, aktiv qiymati AV (ming soʻm), EF (%), 10 yildagi kutilgan hodisalar soni (choragacha va choradan keyin) hamda choraning yillik narxi. Har bir risk uchun SLE, ALE (choragacha va keyin), foyda va tavsiyani chiqaring; oxirida tavsiya etilgan choralar joriy etilgandan keyingi umumiy yillik zarar va choralar narxini chiqaring.

```python
n = int(input())
jami_oldin = jami_keyin = choralar = 0.0
for _ in range(n):
    nom, av, ef, aro10, aro10_keyin, narx = input().split()
    sle = int(av) * int(ef) / 100                   # bitta hodisa zarari
    ale = sle * int(aro10) / 10                      # yillik kutilgan zarar
    ale_keyin = sle * int(aro10_keyin) / 10
    foyda = ale - ale_keyin - int(narx)
    tavsiya = "joriy etish" if foyda > 0 else "rad etish"
    print(f"{nom}: SLE={sle:.1f} ALE={ale:.1f} -> {ale_keyin:.1f} foyda={foyda:.1f} {tavsiya}")
    jami_oldin += ale
    if foyda > 0:
        jami_keyin += ale_keyin
        choralar += int(narx)
    else:
        jami_keyin += ale                            # chora qabul qilinmadi
print(f"jami yillik zarar: {jami_oldin:.1f} -> {jami_keyin:.1f}, choralar narxi {choralar:.1f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        double jamiOldin = 0, jamiKeyin = 0, choralar = 0;
        for (int i = 0; i < n; i++) {
            String nom = in.next();
            long av = in.nextLong();
            int ef = in.nextInt();
            int aro10 = in.nextInt();
            int aro10Keyin = in.nextInt();
            long narx = in.nextLong();
            double sle = av * ef / 100.0;                         // bitta hodisa zarari
            double ale = sle * aro10 / 10;                        // yillik kutilgan zarar
            double aleKeyin = sle * aro10Keyin / 10;
            double foyda = ale - aleKeyin - narx;
            String tavsiya = foyda > 0 ? "joriy etish" : "rad etish";
            System.out.println(String.format("%s: SLE=%.1f ALE=%.1f -> %.1f foyda=%.1f %s", nom, sle, ale, aleKeyin, foyda, tavsiya));
            jamiOldin += ale;
            if (foyda > 0) {
                jamiKeyin += aleKeyin;
                choralar += narx;
            } else {
                jamiKeyin += ale;                                 // chora qabul qilinmadi
            }
        }
        System.out.println(String.format("jami yillik zarar: %.1f -> %.1f, choralar narxi %.1f", jamiOldin, jamiKeyin, choralar));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

int main() {
    int n;
    cin >> n;
    double jamiOldin = 0, jamiKeyin = 0, choralar = 0;
    for (int i = 0; i < n; i++) {
        string nom;
        long long av, narx;
        int ef, aro10, aro10Keyin;
        cin >> nom >> av >> ef >> aro10 >> aro10Keyin >> narx;
        double sle = av * ef / 100.0;                             // bitta hodisa zarari
        double ale = sle * aro10 / 10;                            // yillik kutilgan zarar
        double aleKeyin = sle * aro10Keyin / 10;
        double foyda = ale - aleKeyin - narx;
        string tavsiya = foyda > 0 ? "joriy etish" : "rad etish";
        printf("%s: SLE=%.1f ALE=%.1f -> %.1f foyda=%.1f %s\n", nom.c_str(), sle, ale, aleKeyin, foyda, tavsiya.c_str());
        jamiOldin += ale;
        if (foyda > 0) {
            jamiKeyin += aleKeyin;
            choralar += narx;
        } else {
            jamiKeyin += ale;                                     // chora qabul qilinmadi
        }
    }
    printf("jami yillik zarar: %.1f -> %.1f, choralar narxi %.1f\n", jamiOldin, jamiKeyin, choralar);
    return 0;
}
```

Kirish (summalar ming soʻmda):

```text
4
Server_shifrlovchi 200000 50 3 1 15000
Noutbuk_ogirligi 12000 100 20 15 8000
Sayt_DDoS 50000 20 5 1 2500
Fishing 30000 30 7 2 3000
```

Chiqish:

```text
Server_shifrlovchi: SLE=100000.0 ALE=30000.0 -> 10000.0 foyda=5000.0 joriy etish
Noutbuk_ogirligi: SLE=12000.0 ALE=24000.0 -> 18000.0 foyda=-2000.0 rad etish
Sayt_DDoS: SLE=10000.0 ALE=5000.0 -> 1000.0 foyda=1500.0 joriy etish
Fishing: SLE=9000.0 ALE=6300.0 -> 1800.0 foyda=1500.0 joriy etish
jami yillik zarar: 65300.0 -> 36800.0, choralar narxi 20500.0
```

Server shifrlovchi virusiga qarshi zaxira nusxa tizimi (yiliga 15 mln) yillik kutilgan zararni 20 mln ga kamaytiradi — foydali. Noutbuklarni kuzatish tizimi esa (8 mln) zararni atigi 6 mln ga kamaytiradi — boshqa chora (masalan, disk shifrlash va sugʻurta) izlash yoki riskni qabul qilish kerak.

:::warning
**Koʻp uchraydigan xatolar**

- **Risk va tahdidni bir narsa deb hisoblash.** Tahdid — manba; risk — uning ehtimolligi va oqibati.
- **Choraning narxini aktiv qiymatidan oshirib yuborish.** Himoya foydasi uning narxidan katta boʻlishi kerak.
- **Riskni bir marta baholab, unutish.** Risklar reyestri muntazam (kamida yiliga bir marta va har katta oʻzgarishda) yangilanadi.
- **Nomoddiy zararni hisobga olmaslik.** Obroʻ, mijozlar ishonchi va jarimalar ham aktiv qiymatiga kiradi.
- **Qoldiq riskni hujjatlashtirmaslik.** Qabul qilingan risklar rahbariyat imzosi bilan qayd etiladi.
:::

:::tip
Universitetingiz uchun 5 ta riskdan iborat kichik reyestr tuzing (masalan, baholar bazasining sizishi, server xonasida yongʻin, oʻqituvchi noutbukining oʻgʻirlanishi, sayt ishdan chiqishi, fishing). Har biriga ehtimollik, taʼsir, egasi va bitta chora yozing.
:::

## Xulosa

- Risklarni boshqarish: kontekst → aktivlar → tahdid va zaifliklar → baholash → munosabat → monitoring.
- Sifat baholash — risk matritsasi (ehtimollik × taʼsir); miqdoriy — ALE = SLE × ARO, SLE = AV × EF.
- Chora foydasi = ALE oldin − ALE keyin − narx; musbat boʻlsa, chora oʻzini oqlaydi.
- Strategiyalar: kamaytirish, oʻtkazish, qochish, qabul qilish; qoldiq risk rasman qabul qilinadi.
- Risklar reyestri va RMIS — risklarni yuritish, monitoring va hisobot vositalari.

## Oʻzingizni tekshiring

:::check
Q: AV = 80 mln, EF = 25% boʻlsa, SLE qancha?
A: 20 mln soʻm.
Q: SLE = 20 mln, hodisa 4 yilda bir marta kutilsa, ALE qancha?
A: 20 × 0,25 = 5 mln soʻm yiliga.
Q: Kibersugʻurta riskka munosabatning qaysi strategiyasi?
A: Oʻtkazish (transfer).
Q: Choragacha ALE 12 mln, keyin 4 mln, chora narxi yiliga 10 mln. Chorani joriy etish kerakmi?
A: Yoʻq: foyda = 12 − 4 − 10 = −2 mln.
Q: RMIS nima uchun kerak?
A: Risklar reyestrini yuritish, hodisalarni qayd etish, monitoring va rahbariyat uchun hisobotlar tayyorlash uchun.
:::

:::link https://csrc.nist.gov/pubs/sp/800/30/r1/final
NIST SP 800-30: Guide for Conducting Risk Assessments
Risklarni baholash boʻyicha NIST qoʻllanmasi (ingliz tilida).
:::
