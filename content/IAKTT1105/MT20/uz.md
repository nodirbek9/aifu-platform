---
title: Bulutli texnologiyalarning asosiy turlari
summary: Mustaqil ish — "hammasi xizmat sifatida" (XaaS) — IaaS, PaaS, SaaS dan tashqari CaaS (konteynerlar), FaaS (serversiz), DBaaS, DaaS (virtual ish stoli), AIaaS; asosiy texnologiyalar — virtualizatsiya, konteynerlar va Kubernetes; bulutli saqlash turlari (blokli, faylli, obyektli); chekka hisoblash (edge); multi-cloud va gibrid strategiyalar; narx modellari (talab boʻyicha, rezerv, spot, serversiz); "talab boʻyicha" va "rezerv" narxlarini solishtiruvchi dastur; topshiriq.
---

## Mustaqil ishning maqsadi

M11 da bulutning NIST boʻyicha xususiyatlari, uchta asosiy xizmat modeli (IaaS, PaaS, SaaS) va joylashtirish modellari bilan tanishdik. Bu mustaqil ishda bulut xizmatlarining **toʻliq „menyusi“ni**, ularning ostidagi texnologiyalarni va **narx modellarini** chuqurroq koʻrib chiqamiz — iqtisodchi uchun aynan narx modeli tanlovi katta tejash beradi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- IaaS, PaaS, SaaS dan tashqari bulut xizmatlari turlarini ayta olasiz;
- virtualizatsiya va konteynerlarning farqini tushuntira olasiz;
- saqlash turlarini vazifaga qarab tanlay olasiz;
- narx modellarini solishtirib, zararsizlik nuqtasini hisoblay olasiz.
:::

## "Hammasi xizmat sifatida" (XaaS)

| Model | Provayder nimani beradi | Mijoz nimani boshqaradi | Misol |
|---|---|---|---|
| **IaaS** | virtual serverlar, disklar, tarmoq | OT, dasturlar, maʼlumotlar | Amazon EC2, Azure VM |
| **CaaS** (Containers) | konteynerlarni ishga tushirish platformasi | konteyner tasvirlari | Kubernetes xizmatlari (EKS, AKS, GKE) |
| **PaaS** | ilovani ishga tushirish muhiti | ilova kodi va maʼlumotlar | Heroku, Google App Engine |
| **DBaaS** | boshqariladigan maʼlumotlar bazasi | jadvallar va soʻrovlar | Amazon RDS, Azure SQL |
| **FaaS** (serversiz) | funksiyani hodisa boʻyicha bajarish | faqat funksiya kodi | AWS Lambda, Azure Functions |
| **SaaS** | tayyor dastur | sozlamalar va maʼlumotlar | Microsoft 365, Google Workspace, bulutli CRM |
| **DaaS** (Desktop) | virtual ish stoli | foydalanuvchi ishi | Windows 365, Azure Virtual Desktop |
| **AIaaS** | tayyor sunʼiy intellekt modellari va API | soʻrovlar va maʼlumotlar | nutqni tanish, tarjima, til modellari API lari |

Pastdan yuqoriga qarab provayder koʻproq ishni oʻz zimmasiga oladi, mijoz esa kamroq boshqaradi va kamroq moslashuvchanlikka ega boʻladi.

## Asosiy texnologiyalar

**Virtualizatsiya** — bitta jismoniy serverda **gipervizor** yordamida bir nechta virtual mashina (VM) ishlaydi; har birida oʻz operatsion tizimi. Bu bulutning asosi: provayder jismoniy resurslarni koʻplab mijozlarga boʻlib beradi.

**Konteynerlar** (Docker) — ilova va uning kutubxonalari bitta "qutiga" joylanadi; konteynerlar umumiy OT yadrosidan foydalanadi, shuning uchun VM dan yengilroq va tezroq ishga tushadi. **Kubernetes** — minglab konteynerlarni avtomatik boshqarish (ishga tushirish, masshtablash, nosozlikda qayta tiklash).

| | Virtual mashina | Konteyner |
|---|---|---|
| Ishga tushish | daqiqalar | soniyalar |
| Hajmi | gigabaytlar | megabaytlar |
| Izolyatsiya | kuchli (alohida OT) | yengilroq (umumiy yadro) |
| Qoʻllanish | eski ilovalar, turli OT | mikroservislar, zamonaviy ilovalar |

## Bulutli saqlash turlari

| Tur | Tuzilishi | Qoʻllanish |
|---|---|---|
| **Blokli** | virtual disk, serverga ulanadi | maʼlumotlar bazasi, OT diski |
| **Faylli** | papkalar va fayllar, umumiy tarmoq diski | boʻlim hujjatlari, umumiy fayllar |
| **Obyektli** | fayl + metamaʼlumot + noyob manzil, cheksiz masshtab | zaxira nusxalar, arxivlar, rasm va videolar, maʼlumotlar koʻli (MT9) |

Obyektli saqlashda "sinflar" bor: tez-tez ishlatiladigan maʼlumot qimmatroq, kam ishlatiladigan arxiv (masalan, 7 yil saqlanadigan buxgalteriya hujjatlari) ancha arzon, lekin tiklash sekinroq.

## Chekka hisoblash va multi-cloud

- **Chekka hisoblash** (edge computing) — maʼlumotni manbaga yaqin joyda (zavod, doʻkon, bazaviy stansiya) qayta ishlash: kechikish kam (MT5), tarmoq trafigi kamayadi. Misol: doʻkondagi kameralar xaridorlar oqimini joyida tahlil qilib, bulutga faqat statistikani yuboradi.
- **Multi-cloud** — bir nechta provayderdan foydalanish: bitta provayderga qaramlikni kamaytiradi, lekin boshqaruv murakkablashadi.
- **Gibrid bulut** (M11) — maxfiy maʼlumotlar oʻz serverida yoki milliy maʼlumotlar markazida, qolgani ommaviy bulutda (S11 dagi lokallashtirish talabi).

## Narx modellari

| Model | Mazmuni | Qachon foydali |
|---|---|---|
| **Talab boʻyicha** (on-demand) | foydalanilgan soat yoki soniya uchun | qisqa yoki notekis yuklama, sinov |
| **Rezerv** (1–3 yilga band qilish) | oldindan majburiyat evaziga katta chegirma | doimiy ishlaydigan serverlar |
| **Spot** (ortiqcha quvvat) | juda arzon, lekin provayder istalgan payt toʻxtatishi mumkin | toʻxtatilsa zarar qilmaydigan hisob-kitoblar |
| **Serversiz** (soʻrovlar boʻyicha) | faqat bajarilgan funksiyalar va vaqti uchun | kam va tasodifiy chaqiriladigan vazifalar |
| **Obuna** (SaaS) | foydalanuvchi boshiga oylik | ofis, CRM, buxgalteriya |

## Ishlangan misol: talab boʻyicha yoki rezerv?

Virtual server talab boʻyicha soatiga 1 500 soʻm yoki rezerv bilan oyiga qatʼiy 650 000 soʻm turadi. Har bir satrda server nomi va oyda necha soat ishlashi. Zararsizlik nuqtasini va har bir server uchun arzonroq variantni aniqlang.

```python
import sys

SOAT_NARXI = 1500       # talab boʻyicha, soʻm/soat
REZERV = 650000         # rezerv, soʻm/oy (soatlardan qatʼi nazar)

chegara = (REZERV + SOAT_NARXI - 1) // SOAT_NARXI    # yuqoriga yaxlitlash
print(f"Zararsizlik nuqtasi (soat): {chegara}")
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, soat = satr.split()
    soat = int(soat)
    talab = soat * SOAT_NARXI
    tanlov = "talab boʻyicha" if talab <= REZERV else "rezerv"
    print(f"{nom}: {soat} soat, talab boʻyicha {talab}, rezerv {REZERV} -> {tanlov}")
```
```java
import java.util.Scanner;

public class Main {
    static final long SOAT_NARXI = 1500;      // talab boʻyicha, soʻm/soat
    static final long REZERV = 650000;        // rezerv, soʻm/oy (soatlardan qatʼi nazar)

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long chegara = (REZERV + SOAT_NARXI - 1) / SOAT_NARXI;   // yuqoriga yaxlitlash
        System.out.println("Zararsizlik nuqtasi (soat): " + chegara);
        while (in.hasNext()) {
            String nom = in.next();
            long soat = in.nextLong();
            long talab = soat * SOAT_NARXI;
            String tanlov = talab <= REZERV ? "talab boʻyicha" : "rezerv";
            System.out.println(nom + ": " + soat + " soat, talab boʻyicha " + talab + ", rezerv " + REZERV + " -> " + tanlov);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const long long SOAT_NARXI = 1500;      // talab boʻyicha, soʻm/soat
const long long REZERV = 650000;        // rezerv, soʻm/oy (soatlardan qatʼi nazar)

int main() {
    long long chegara = (REZERV + SOAT_NARXI - 1) / SOAT_NARXI;    // yuqoriga yaxlitlash
    cout << "Zararsizlik nuqtasi (soat): " << chegara << "\n";
    string nom;
    long long soat;
    while (cin >> nom >> soat) {
        long long talab = soat * SOAT_NARXI;
        string tanlov = talab <= REZERV ? "talab boʻyicha" : "rezerv";
        cout << nom << ": " << soat << " soat, talab boʻyicha " << talab << ", rezerv " << REZERV << " -> " << tanlov << "\n";
    }
    return 0;
}
```

Kirish:

```text
test-server 120
report-server 400
crm-server 500
web-server 730
```

Chiqish:

```text
Zararsizlik nuqtasi (soat): 434
test-server: 120 soat, talab boʻyicha 180000, rezerv 650000 -> talab boʻyicha
report-server: 400 soat, talab boʻyicha 600000, rezerv 650000 -> talab boʻyicha
crm-server: 500 soat, talab boʻyicha 750000, rezerv 650000 -> rezerv
web-server: 730 soat, talab boʻyicha 1095000, rezerv 650000 -> rezerv
```

Oyiga 434 soatdan (kuniga ~14 soat) koʻp ishlaydigan server uchun rezerv foydali. Doimiy ishlaydigan veb-server (730 soat — butun oy) rezerv bilan oyiga 445 000 soʻm, yiliga 5,3 mln soʻm tejaydi. Test serveri esa faqat ish vaqtida yoqiladi va talab boʻyicha arzon. Rezerv — majburiyat: server keyin kerak boʻlmasa ham toʻlanadi, shuning uchun yuklamani oldindan tahlil qilish kerak (S11 dagi FinOps).

:::warning
**Koʻp uchraydigan xatolar**

- **Hamma narsaga bitta model.** Doimiy yuklamaga rezerv, notekisiga talab boʻyicha yoki serversiz.
- **Ishlatilmayotgan resurslarni oʻchirmaslik.** Talab boʻyicha toʻlanadigan "unutilgan" server har oy pul yeydi.
- **Arxivni qimmat saqlash sinfida saqlash.** Kam ishlatiladigan maʼlumotni arzon sinfga oʻtkazing.
- **Konteyner va VM ni bir xil deb oʻylash.** Izolyatsiya va xavfsizlik talablari turlicha.
:::

## Topshiriq

1. Oʻzingiz bilgan uchta xizmatni (masalan, Telegram, bank ilovasi, universitet LMS) qaysi XaaS modellariga tayanishi mumkinligini tahmin qiling.
2. Misol dasturini oʻzgartiring: rezerv narxi 500 000 soʻm boʻlsa, zararsizlik nuqtasi qanday oʻzgaradi?
3. Kompaniya zaxira nusxalari (50 GB, oyiga 1 marta tiklash) va kundalik buxgalteriya bazasi uchun qaysi saqlash turi va sinfini tanlash kerak? Asoslang.
4. Provayderlardan birining (AWS, Azure, Google Cloud) narx kalkulyatorida bitta virtual serverning talab boʻyicha va 1 yillik rezerv narxini solishtiring.

## Xulosa

- XaaS: IaaS, CaaS, PaaS, DBaaS, FaaS, SaaS, DaaS, AIaaS — provayder zimmasidagi ish pastdan yuqoriga oshadi.
- Asos: virtualizatsiya (VM) va konteynerlar (Docker, Kubernetes).
- Saqlash: blokli, faylli, obyektli; arxiv uchun arzon sinflar.
- Chekka hisoblash, multi-cloud va gibrid — zamonaviy arxitektura yoʻnalishlari.
- Narx modellari: talab boʻyicha, rezerv, spot, serversiz, obuna; tanlov zararsizlik nuqtasi orqali.

## Oʻzingizni tekshiring

:::check
Q: Konteyner virtual mashinadan nimasi bilan farq qiladi?
A: Konteyner umumiy OT yadrosidan foydalanadi, shuning uchun yengilroq va tezroq ishga tushadi; VM da alohida OT bor va izolyatsiya kuchliroq.
Q: Serversiz (FaaS) modelda nima uchun haq toʻlanadi?
A: Faqat funksiyaning bajarilish soni va vaqti uchun.
Q: Zaxira nusxalar uchun qaysi saqlash turi mos?
A: Obyektli saqlash, kam ishlatiladigan maʼlumotlar uchun arzon sinf bilan.
Q: Rezerv narx modelining xavfi nima?
A: Bu majburiyat — resurs kerak boʻlmay qolsa ham toʻlanadi.
:::

:::link https://calculator.aws/
AWS Pricing Calculator
Bulut xizmatlari narxini hisoblash kalkulyatori (ingliz tilida).
:::
