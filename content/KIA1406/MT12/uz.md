---
title: Barmoq iziga asoslangan autentifikatsiya usuli. Elektron raqamli imzo
summary: Barmoq izi biometriyasi — papillyar naqsh turlari (yoy, ilmoq, burama), minutsiyalar; skanerlar — optik, sigʻimli, ultratovushli; roʻyxatga olish va solishtirish bosqichlari; shablon, oʻxshashlik bali va chegara, FAR va FRR; tirik ekanini aniqlash va soxta barmoqlar; shablonlarni himoyalash (qurilmada, xavfsiz anklavda); barmoq izi va ERI ning birgalikdagi ishlashi — biometriya yopiq kalitni ochadi; ERI ning huquqiy jihatlari va Oʻzbekistonda qoʻllanishi; ikkilik shablonlarni Hamming masofasi bilan solishtirish dasturi.
---

## Nima uchun bu mavzu muhim?

Barmoq izi — eng keng tarqalgan biometrik belgi: u telefonlar, noutbuklar, bank ilovalari, kirish nazorati tizimlari va pasport-viza nazoratida ishlatiladi. ERI esa elektron hujjatga qonuniy kuch beradi (M8). Bu ikki texnologiya koʻpincha birga ishlaydi: siz barmogʻingizni skanerga qoʻyasiz, qurilma esa ichidagi yopiq kalit bilan soʻrovni imzolaydi (M10 dagi FIDO2). Ularning ishlash tamoyili va cheklovlarini bilish — toʻgʻri qoʻllash sharti.

:::note
**Bu mustaqil ishdan soʻng siz:**

- barmoq izining qanday belgilari solishtirishda ishlatilishini tushuntira olasiz;
- skaner turlari va ularning soxta barmoqqa chidamliligini farqlay olasiz;
- shablon, oʻxshashlik bali, chegara, FAR va FRR tushunchalarini qoʻllay olasiz;
- biometriya va ERI birgalikda qanday ishlashini tushuntira olasiz;
- ikkilik shablonlarni Hamming masofasi bilan solishtiruvchi dastur yoza olasiz.
:::

## Barmoq izi belgilari

Barmoq terisidagi **papillyar chiziqlar** naqshi homiladorlik davrida shakllanadi va umr boʻyi oʻzgarmaydi; hatto egizaklarda ham farq qiladi.

| Daraja | Belgilar | Ishlatilishi |
|---|---|---|
| 1-daraja (umumiy naqsh) | yoy, ilmoq, burama | tasnif, bazada tez qidirish |
| 2-daraja (**minutsiyalar**) | chiziq uchi, chiziqning ikkiga ajralishi, ularning joylashuvi va yoʻnalishi | asosiy solishtirish usuli |
| 3-daraja | teri teshiklari, chiziq qalinligi | yuqori aniqlikdagi kriminalistik tahlil |

## Skanerlar

| Tur | Ishlash tamoyili | Xususiyatlari |
|---|---|---|
| **Optik** | barmoq tasvirini kamera bilan olish | arzon; oddiy soxta nusxalarga zaifroq |
| **Sigʻimli** (capacitive) | chiziqlar va chuqurchalar orasidagi elektr sigʻimi farqi | telefon va noutbuklarda keng tarqalgan |
| **Ultratovushli** | teriga ultratovush toʻlqini va qaytgan signal | 3D tasvir, iflos yoki nam barmoq bilan ham ishlaydi |
| **Termal va boshqalar** | harorat farqi | kam tarqalgan |

## Roʻyxatga olish va solishtirish

1. **Roʻyxatga olish** (enrollment): barmoq bir necha marta skanerlanadi, tasvirdan minutsiyalar ajratib olinib, **shablon** tuziladi. Tasvirning oʻzi emas, faqat shablon saqlanadi.
2. **Solishtirish**: yangi namunadan shablon tuziladi va saqlangani bilan solishtiriladi → **oʻxshashlik bali** (yoki farq) → **chegara** bilan taqqoslanadi.
3. **Verifikatsiya** (1:1 — "bu ali mi?") va **identifikatsiya** (1:N — "bu kim?", masalan, kriminalistik bazada) farqlanadi; 1:N da xato ehtimoli baza hajmi bilan oshadi.

Xatolar M10 dagidek: **FAR** — begona qabul qilindi, **FRR** — egasi rad etildi; chegara ularni muvozanatlaydi. Ikki skanerlash hech qachon toʻliq bir xil boʻlmaydi (bosim, burchak, namlik) — shuning uchun aniq tenglik emas, yaqinlik tekshiriladi.

## Xavfsizlik masalalari

- **Soxta barmoqlar** (silikon, jelatin, 3D-bosma nusxalar) — himoyasi: **tirik ekanini aniqlash** (qon aylanishi, ter, teri qatlamlari, ultratovushli 3D tasvir).
- **Almashtirib boʻlmaslik**: parol sizsa — almashtiriladi, barmoq izi sizsa — umrbod. Shuning uchun shablonlar markaziy serverda emas, **qurilmaning xavfsiz anklavida** (Secure Enclave, TPM, TEE) shifrlangan holda saqlanadi va tashqariga chiqmaydi.
- **Majburlash**: barmoqni majburan qoʻydirish parolni aytdirishdan osonroq — juda muhim tizimlarda biometriya PIN bilan birga ishlatiladi.
- **Shaxsiy maʼlumot**: biometrik maʼlumot "Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonun bilan himoyalanadi; uni yigʻish rozilik va asos talab qiladi.

## Barmoq izi va ERI

Zamonaviy qurilmalarda barmoq izi **ERI ning oʻzi emas** — u qurilmadagi **yopiq kalitga kirishni ochadi**:

1. Qurilmada kalit juftligi yaratiladi; yopiq kalit xavfsiz anklavdan chiqmaydi.
2. Imzolash yoki kirish soʻralganda foydalanuvchi barmoq izi bilan tasdiqlaydi.
3. Anklav hujjat yoki soʻrov xeshini yopiq kalit bilan imzolaydi; server ochiq kalit bilan tekshiradi (M8).

Shu tarzda biometrik maʼlumot tarmoqqa umuman chiqmaydi, imzo esa kriptografik jihatdan kuchli boʻladi. Oʻzbekistonda ERI kaliti fayl yoki ID-karta chipida saqlanadi va PIN/parol bilan himoyalanadi; ERI qonunga koʻra qoʻl imzosiga tenglashtirilgan, sertifikatlarni vakolatli markazlar beradi (M8, A8).

## Ishlangan misol: shablonlarni Hamming masofasi bilan solishtirish

Baʼzi biometrik tizimlar (masalan, koʻz qorachigʻi kodlari, baʼzi barmoq izi shablonlari) belgilarni **ikkilik vektor** koʻrinishida saqlaydi va ularni **normallashgan Hamming masofasi** bilan solishtiradi: HD = farq qiluvchi bitlar soni / bitlar soni. Tasodifiy begona namunalar uchun HD ≈ 0,5; bir odamning namunalari uchun — ancha kichik. Birinchi satrda chegara, ikkinchisida saqlangan shablon, keyingilarida "nom namuna". HD ≤ chegara boʻlsa — `mos`.

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
chegara = float(satrlar[0][0])
shablon = satrlar[1][0]
for nom, namuna in satrlar[2:]:
    farq = sum(1 for a, b in zip(shablon, namuna) if a != b)   # farq qiluvchi bitlar
    hd = farq / len(shablon)                                    # normallashgan Hamming masofasi
    print(f"{nom}: HD={hd:.3f} -> " + ("mos" if hd <= chegara else "mos emas"))
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double chegara = Double.parseDouble(in.next());
        String shablon = in.next();
        while (in.hasNext()) {
            String nom = in.next(), namuna = in.next();
            int farq = 0;
            for (int i = 0; i < shablon.length(); i++) {
                if (shablon.charAt(i) != namuna.charAt(i)) farq++;   // farq qiluvchi bitlar
            }
            double hd = (double) farq / shablon.length();             // normallashgan Hamming masofasi
            System.out.println(String.format("%s: HD=%.3f -> %s", nom, hd, hd <= chegara ? "mos" : "mos emas"));
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
    double chegara;
    string shablon, nom, namuna;
    cin >> chegara >> shablon;
    while (cin >> nom >> namuna) {
        int farq = 0;
        for (size_t i = 0; i < shablon.size(); i++) {
            if (shablon[i] != namuna[i]) farq++;                     // farq qiluvchi bitlar
        }
        double hd = (double) farq / shablon.size();                  // normallashgan Hamming masofasi
        printf("%s: HD=%.3f -> %s\n", nom.c_str(), hd, hd <= chegara ? "mos" : "mos emas");
    }
    return 0;
}
```

Kirish:

```text
0.25
1011001110001011110010100110101100011101
egasi_1 1011001110001011110010100110101100011101
egasi_2 1011011110001001110010100100101100011111
egasi_3 1001001010001011100010110110101000011101
begona_1 0000011100011101100000000011111001010110
begona_2 1100011011010110101001110010110101101001
```

Chiqish:

```text
egasi_1: HD=0.000 -> mos
egasi_2: HD=0.100 -> mos
egasi_3: HD=0.125 -> mos
begona_1: HD=0.475 -> mos emas
begona_2: HD=0.550 -> mos emas
```

Egasining namunalari 10–12,5% farq qildi (bosim, burchak tufayli), begonalarniki — taxminan yarmi, xuddi tasodifiy bitlar kabi. Eʼtibor bering: `egasi_1` saqlangan shablon bilan **aynan bir xil** (HD = 0). Haqiqiy skanerlashda bu deyarli boʻlmaydi — bunday natija ilgari ushlab olingan shablonni qayta yuborish (replay) belgisi boʻlishi mumkin, shuning uchun baʼzi tizimlar juda kichik farqni ham shubhali deb hisoblaydi. Chegarani 0,25 dan pastga tushirsak, FRR oshadi; yuqoriga koʻtarsak — FAR.

:::warning
**Koʻp uchraydigan xatolar**

- **Barmoq izini "parol" deb hisoblash.** U yashirin emas — biz uni har bir tegingan narsada qoldiramiz; u sir emas, identifikator va qulaylik omili.
- **Biometrik tasvirlarni serverda saqlash.** Faqat himoyalangan shablonlar va imkon qadar qurilmada.
- **Aniq tenglikni talab qilish.** Biometriya doim taxminiy; chegara va FAR/FRR muvozanati kerak.
- **Tirik ekanini aniqlashsiz arzon skaner.** Soxta barmoq bilan aldash oson.
- **ERI kalitini PIN siz saqlash.** Qurilma yoki fayl oʻgʻirlansa, imzo soxtalashtiriladi.
:::

:::tip
Telefoningizda barmoq izi bilan ochishni sozlashda bir xil barmoqni turli burchaklarda koʻp marta skanerlating — bu FRR ni kamaytiradi. Muhim ilovalar (bank) uchun biometriya + PIN yoki qurilma paroli kombinatsiyasini yoqing.
:::

## Xulosa

- Barmoq izi minutsiyalar boʻyicha solishtiriladi; tasvir emas, shablon saqlanadi.
- Skanerlar: optik, sigʻimli, ultratovushli; soxta barmoqqa qarshi — tirik ekanini aniqlash.
- Solishtirish taxminiy: oʻxshashlik bali, chegara, FAR va FRR; 1:1 verifikatsiya va 1:N identifikatsiya.
- Shablonlar qurilmaning xavfsiz anklavida; biometriyani almashtirib boʻlmaydi.
- Barmoq izi yopiq kalitni ochadi, imzoni esa kriptografiya (ERI) bajaradi.

## Oʻzingizni tekshiring

:::check
Q: Barmoq izini solishtirishda asosan qaysi belgilar ishlatiladi?
A: Minutsiyalar — chiziq uchlari va ajralishlari, ularning joylashuvi va yoʻnalishi.
Q: Nima uchun biometrik shablonlar qurilmaning xavfsiz anklavida saqlanadi?
A: Biometriyani almashtirib boʻlmaydi; markaziy bazadan sizish umrbod xavf tugʻdiradi.
Q: Barmoq izi bilan tasdiqlangan ERI da imzoni aslida nima yaratadi?
A: Qurilmadagi yopiq kalit; barmoq izi faqat unga kirishni ochadi.
Q: Hamming masofasi 0 boʻlgan namuna nima uchun shubhali boʻlishi mumkin?
A: Haqiqiy skanerlashlar har doim biroz farq qiladi; aynan bir xil namuna qayta yuborilgan shablon boʻlishi mumkin.
:::

:::link https://pages.nist.gov/800-63-4/sp800-63b.html
NIST SP 800-63B
Biometriyadan autentifikatsiyada foydalanish talablari (tirik ekanini aniqlash, FAR, qurilmada saqlash) (ingliz tilida).
:::
