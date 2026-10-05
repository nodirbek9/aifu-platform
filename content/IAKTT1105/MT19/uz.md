---
title: Interaktiv xizmatlar va ular bilan ishlash
summary: Mustaqil ish — interaktiv xizmatni foydalanuvchi va tashkilot koʻzi bilan baholash; mijoz yoʻli xaritasi va xizmat sxemasi (service blueprint); foydalanish qulayligining oʻnta evristikasi (Nilsen); hammabop boʻlish (WCAG tamoyillari); xizmat sifati koʻrsatkichlari — yakunlash darajasi, bajarish vaqti, CSAT, NPS, CES; fikr-mulohaza va A/B test; chatbot va messenjer xizmatlarini loyihalash; NPS va boshqa koʻrsatkichlarni hisoblash dasturi; xizmat auditi topshiriqi.
---

## Mustaqil ishning maqsadi

S10 seminarida interaktiv xizmat tushunchasi, turlari va xizmat olish bosqichlarini koʻrdik. Bu mustaqil ishda **xizmat sifatini baholash va yaxshilash** usullarini oʻrganamiz: qanday qilib xizmatni foydalanuvchi uchun qulay qilish va bu qulaylikni raqamlarda oʻlchash mumkin.

:::note
**Bu mustaqil ishdan soʻng siz:**

- mijoz yoʻli xaritasini tuza olasiz;
- xizmatni foydalanish qulayligi evristikalari boʻyicha baholay olasiz;
- hammabop boʻlishning asosiy talablarini bilasiz;
- NPS, CSAT, CES va yakunlash darajasini hisoblay va izohlay olasiz.
:::

## Mijoz yoʻli xaritasi

**Mijoz yoʻli xaritasi** (customer journey map) — foydalanuvchi xizmatni olish jarayonida bosib oʻtadigan qadamlar, uning harakatlari, his-tuygʻulari va muammolari.

| Bosqich | Harakat | Muammo ("ogʻriq nuqtasi") | Yaxshilash gʻoyasi |
|---|---|---|---|
| Xabardorlik | xizmat borligini bilib qoladi | qayerdan boshlashni bilmaydi | qidiruvda aniq nom, yoʻriqnoma |
| Kirish | OneID orqali tizimga kiradi | parolni unutgan | mobil ERI, oddiy tiklash |
| Ariza | shaklni toʻldiradi | 20 ta maydon, keraksiz hujjatlar | maʼlumotni reyestrdan avtomatik olish (MT17) |
| Toʻlov | davlat bojini toʻlaydi | boshqa ilovaga oʻtish kerak | ichki toʻlov tugmasi |
| Kutish | holatni kuzatadi | holat yangilanmaydi | SMS va push-xabarnoma |
| Natija | hujjatni oladi | PDF ochilmaydi | QR kodli elektron hujjat |

**Xizmat sxemasi** (service blueprint) mijoz yoʻlini "sahna ortidagi" jarayonlar bilan toʻldiradi: qaysi xodim, qaysi tizim va qaysi integratsiya har bir qadamni taʼminlaydi (MT11 dagi BPMN yoʻlaklari bilan bogʻliq).

## Foydalanish qulayligining oʻnta evristikasi

Yakob Nilsenning mashhur tamoyillari interfeysni tez baholash uchun ishlatiladi:

| Evristika | Savol |
|---|---|
| 1. Tizim holati koʻrinadi | foydalanuvchi nima boʻlayotganini biladimi (yuklanish, ariza holati)? |
| 2. Real dunyo tili | atamalar foydalanuvchiga tushunarlimi, idora jargoni emasmi? |
| 3. Erkinlik va nazorat | xatoni bekor qilish, orqaga qaytish mumkinmi? |
| 4. Izchillik va standartlar | bir xil amallar hamma joyda bir xil koʻrinadimi? |
| 5. Xatolarning oldini olish | sana maydoni kalendarmi yoki erkin matn? |
| 6. Eslab qolish emas, tanib olish | oldingi kiritilgan maʼlumot koʻrsatiladimi? |
| 7. Moslashuvchanlik | tajribali foydalanuvchi uchun tezkor yoʻllar bormi? |
| 8. Minimalistik dizayn | ortiqcha matn va elementlar yoʻqmi? |
| 9. Xatodan chiqishga yordam | xato xabari aniq va yechim taklif qiladimi? |
| 10. Yordam va hujjatlar | kerakli joyda qisqa yoʻriqnoma bormi? |

## Hammabop boʻlish

Xizmatdan koʻrish, eshitish yoki harakatlanishda cheklovi bor odamlar, keksalar va past tezlikdagi Internet foydalanuvchilari ham foydalana olishi kerak. Xalqaro standart **WCAG** toʻrt tamoyilga asoslanadi:

- **Idrok etiladigan** — rasmlarga matnli tavsif, yetarli kontrast, videoga subtitr.
- **Boshqariladigan** — klaviatura bilan ishlash, yetarli vaqt, katta tugmalar.
- **Tushunarli** — oddiy til, oldindan aytib boʻladigan xatti-harakat.
- **Mustahkam** — turli brauzer, ekran oʻquvchi dasturlar va qurilmalarda ishlaydi.

## Sifat koʻrsatkichlari

| Koʻrsatkich | Qanday hisoblanadi | Nimani koʻrsatadi |
|---|---|---|
| **Yakunlash darajasi** | yakunlangan arizalar / boshlangan × 100% | foydalanuvchilar qayerdadir "tashlab ketyapti"mi |
| **Bajarish vaqti** | arizadan natijagacha oʻrtacha vaqt | tezlik, vaʼda qilingan muddatga rioya |
| **CSAT** (mijoz qoniqishi) | "Qoniqdingizmi?" (1–5) — oʻrtacha yoki 4–5 javoblar ulushi | bitta muloqotdan qoniqish |
| **NPS** (sodiqlik indeksi) | "Tavsiya qilasizmi?" (0–10): 9–10 targʻibotchilar % − 0–6 tanqidchilar % | umumiy sodiqlik, −100 dan +100 gacha |
| **CES** (sarflangan kuch) | "Xizmatni olish qanchalik oson boʻldi?" | ortiqcha qiyinchilik bormi |

## Ishlangan misol: soʻrovnoma natijalari

Birinchi satrda — "Xizmatni tavsiya qilasizmi?" savoliga 0–10 ballik javoblar, ikkinchisida — boshlangan va yakunlangan arizalar soni. NPS, oʻrtacha baho va yakunlash darajasini hisoblang.

```python
import sys

satrlar = sys.stdin.read().strip().split("\n")
baholar = [int(x) for x in satrlar[0].split()]
boshlangan, yakunlangan = map(int, satrlar[1].split())
n = len(baholar)

targibotchi = sum(1 for b in baholar if b >= 9)      # 9–10 ball
tanqidchi = sum(1 for b in baholar if b <= 6)        # 0–6 ball
neytral = n - targibotchi - tanqidchi                # 7–8 ball
nps = (targibotchi - tanqidchi) * 100 / n

print(f"Javoblar: {n}")
print(f"Targʻibotchilar: {targibotchi}, neytrallar: {neytral}, tanqidchilar: {tanqidchi}")
print(f"NPS: {nps:.1f}")
print(f"Oʻrtacha baho: {sum(baholar) / n:.2f}")
print(f"Yakunlash darajasi: {yakunlangan * 100 / boshlangan:.1f}%")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] baholar = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int boshlangan = in.nextInt(), yakunlangan = in.nextInt();
        int n = baholar.length, targibotchi = 0, tanqidchi = 0, jami = 0;
        for (int b : baholar) {
            if (b >= 9) targibotchi++;                   // 9–10 ball
            if (b <= 6) tanqidchi++;                     // 0–6 ball
            jami += b;
        }
        int neytral = n - targibotchi - tanqidchi;       // 7–8 ball
        double nps = (targibotchi - tanqidchi) * 100.0 / n;

        System.out.println("Javoblar: " + n);
        System.out.println("Targʻibotchilar: " + targibotchi + ", neytrallar: " + neytral + ", tanqidchilar: " + tanqidchi);
        System.out.println(String.format("NPS: %.1f", nps));
        System.out.println(String.format("Oʻrtacha baho: %.2f", (double) jami / n));
        System.out.println(String.format("Yakunlash darajasi: %.1f%%", yakunlangan * 100.0 / boshlangan));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    istringstream ss(satr);
    vector<int> baholar;
    int b;
    while (ss >> b) baholar.push_back(b);
    int boshlangan, yakunlangan;
    cin >> boshlangan >> yakunlangan;

    int n = baholar.size(), targibotchi = 0, tanqidchi = 0, jami = 0;
    for (int x : baholar) {
        if (x >= 9) targibotchi++;                       // 9–10 ball
        if (x <= 6) tanqidchi++;                         // 0–6 ball
        jami += x;
    }
    int neytral = n - targibotchi - tanqidchi;           // 7–8 ball
    double nps = (targibotchi - tanqidchi) * 100.0 / n;

    printf("Javoblar: %d\n", n);
    printf("Targʻibotchilar: %d, neytrallar: %d, tanqidchilar: %d\n", targibotchi, neytral, tanqidchi);
    printf("NPS: %.1f\n", nps);
    printf("Oʻrtacha baho: %.2f\n", (double) jami / n);
    printf("Yakunlash darajasi: %.1f%%\n", yakunlangan * 100.0 / boshlangan);
    return 0;
}
```

Kirish:

```text
10 9 9 10 8 7 6 10 9 3 8 9 10 5 7 9 8 4 6 0
1200 840
```

Chiqish:

```text
Javoblar: 20
Targʻibotchilar: 9, neytrallar: 5, tanqidchilar: 6
NPS: 15.0
Oʻrtacha baho: 7.35
Yakunlash darajasi: 70.0%
```

NPS ijobiy (+15), lekin tanqidchilar ulushi 30% — ularning izohlarini oʻqib, sabablarni guruhlash kerak. Yakunlash darajasi 70%: har 10 ta boshlangan arizadan 3 tasi tashlab ketilgan. Mijoz yoʻli xaritasi orqali qaysi qadamda (koʻpincha — hujjat yuklash yoki toʻlov) chiqib ketilayotganini topish va oʻsha qadamni soddalashtirish kerak.

## Fikr-mulohaza va A/B test

- **Fikr-mulohaza yigʻish**: xizmat oxirida qisqa soʻrovnoma (1–3 savol), sharhlar, murojaatlar tahlili, qoʻngʻiroq markazi yozuvlari.
- **A/B test**: foydalanuvchilarning yarmiga shaklning eski, yarmiga yangi varianti koʻrsatiladi; yakunlash darajasi solishtiriladi. Qaror his-tuygʻu emas, maʼlumot asosida qabul qilinadi.
- **"Halqani yopish"**: shikoyat qilgan foydalanuvchiga nima oʻzgartirilganini xabar qilish — ishonchni oshiradi.

## Chatbot va messenjer xizmatlari

Yaxshi chatbot: bitta aniq vazifani hal qiladi (holatni bilish, maʼlumotnoma olish), tugmalar bilan yoʻnaltiradi, tushunmasa — operatorga ulaydi, shaxsiy maʼlumotni faqat zarur boʻlganda va xavfsiz kanal orqali soʻraydi. Rasmiy botlarni tasdiqlangan belgisi va rasmiy saytdagi havoladan tekshiring (S10 dagi xavfsizlik qoidalari).

:::warning
**Koʻp uchraydigan xatolar**

- **Xizmatni idora uchun loyihalash.** Foydalanuvchi qadamlari va "ogʻriq nuqtalari"dan boshlang.
- **Faqat bitta koʻrsatkichga qarash.** Yuqori NPS bilan birga yakunlash darajasi past boʻlishi mumkin.
- **Hammabop boʻlishni oxirgi bosqichga qoldirish.** Keyinroq tuzatish qimmatroq.
- **Fikr-mulohazani yigʻib, javob bermaslik.** Foydalanuvchilar soʻrovnomalarga javob berishni toʻxtatadi.
:::

## Topshiriq: xizmat auditi

Bitta interaktiv xizmatni tanlang (my.gov.uz dagi xizmat, bank ilovasidagi amal, universitet tizimi):

1. Mijoz yoʻli xaritasini tuzing (kamida 5 bosqich, har birida muammo va yaxshilash gʻoyasi).
2. Xizmatni oʻnta evristika boʻyicha 1–5 ballda baholang.
3. WCAG ning toʻrt tamoyili boʻyicha 2 tadan kamchilik toping.
4. Kursdoshlaringiz orasida 10–20 kishilik NPS soʻrovini oʻtkazing va misol dasturi bilan hisoblang.
5. Natijalar asosida 3 ta ustuvor yaxshilash taklifini yozing.

## Xulosa

- Mijoz yoʻli xaritasi va xizmat sxemasi xizmatni foydalanuvchi va tashkilot koʻzi bilan koʻrsatadi.
- Nilsenning oʻnta evristikasi — interfeysni tez baholash vositasi; WCAG — hammabop boʻlish standarti.
- Koʻrsatkichlar: yakunlash darajasi, bajarish vaqti, CSAT, NPS, CES.
- Yaxshilash: fikr-mulohaza, A/B test va "halqani yopish".

## Oʻzingizni tekshiring

:::check
Q: NPS qanday hisoblanadi?
A: 9–10 ball berganlar ulushidan 0–6 ball berganlar ulushi ayiriladi.
Q: Yakunlash darajasi past boʻlsa, nima qilish kerak?
A: Mijoz yoʻli xaritasi va maʼlumotlar orqali foydalanuvchilar qaysi qadamda chiqib ketayotganini topib, oʻsha qadamni soddalashtirish.
Q: WCAG ning toʻrt tamoyili qaysilar?
A: Idrok etiladigan, boshqariladigan, tushunarli va mustahkam.
Q: A/B test nima?
A: Foydalanuvchilarning ikki guruhiga xizmatning ikki variantini koʻrsatib, natijalarni solishtirish.
:::

:::link https://www.nngroup.com/articles/ten-usability-heuristics/
Nielsen Norman Group: 10 Usability Heuristics
Foydalanish qulayligining oʻnta evristikasi misollar bilan (ingliz tilida).
:::
