---
title: Elektron biznes va mobil tijorat
summary: Elektron biznes va elektron tijorat tushunchalari; modellar — B2B, B2C, C2C, B2G, D2C; biznes modellari — internet-doʻkon, marketpleys, obuna, platforma, freemium; elektron tijorat infratuzilmasi — sayt va ilova, toʻlov, logistika, mijozlarga xizmat; mobil tijorat — mobil ilovalar, QR toʻlovlar, mobil banking, superilovalar; Oʻzbekistonda elektron tijorat (Uzum, Click, Payme va boshq.); raqamli marketing va asosiy koʻrsatkichlar — konversiya, oʻrtacha chek, CAC, ROAS; huquqiy asoslar va xavflar; kanal samaradorligini hisoblash dasturi.
---

## Nima uchun bu mavzu muhim?

Xaridor bugun telefon orqali tovar tanlaydi, QR kod bilan toʻlaydi va ertasi kuni eshik oldida qabul qiladi. Elektron tijorat kichik tadbirkorga ham butun mamlakat va xorij bozoriga chiqish imkonini beradi. Iqtisodchi uchun muhim savollar: qaysi biznes modeli foydali, qaysi reklama kanali oʻzini oqlaydi, mijozni jalb qilish qanchaga tushadi?

:::note
**Bu darsdan soʻng siz:**

- elektron biznes va elektron tijorat farqini tushuntira olasiz;
- B2B, B2C, C2C va boshqa modellarni farqlay olasiz;
- elektron tijorat biznes modellari va infratuzilmasini ayta olasiz;
- mobil tijoratning xususiyatlarini bilasiz;
- raqamli marketingning asosiy koʻrsatkichlarini hisoblay olasiz.
:::

## Tushunchalar

**Elektron biznes** — biznes jarayonlarining (xarid, ishlab chiqarish, sotuv, hisob, hamkorlar bilan aloqa) raqamli texnologiyalar orqali tashkil etilishi. **Elektron tijorat** — uning bir qismi: tovar va xizmatlarni Internet orqali sotish va sotib olish, toʻlov bilan birga.

| Model | Kim kimga | Misol |
|---|---|---|
| **B2C** | biznes → isteʼmolchi | internet-doʻkon, onlayn chiptalar |
| **B2B** | biznes → biznes | ulgurji savdo platformalari, elektron hisob-fakturalar |
| **C2C** | isteʼmolchi → isteʼmolchi | eʼlonlar saytlari (OLX kabi) |
| **B2G** | biznes → davlat | davlat xaridlari portallari |
| **D2C** | ishlab chiqaruvchi → isteʼmolchi | brendning oʻz internet-doʻkoni |

## Biznes modellari

| Model | Qanday daromad qiladi | Misol |
|---|---|---|
| **Internet-doʻkon** | oʻz tovarini sotish | brend yoki doʻkon sayti |
| **Marketpleys** | sotuvchilardan komissiya, reklama | Uzum Market, Wildberries, Amazon |
| **Obuna** | oylik yoki yillik toʻlov | striming xizmatlar, SaaS (M11) |
| **Platforma** (ikki tomonlama bozor) | haydovchi va yoʻlovchi, kuryer va restoran orasida vositachilik | taksi va yetkazib berish ilovalari |
| **Freemium** | asosiy xizmat bepul, kengaytirilgani pullik | ofis va bulut xizmatlari |
| **Reklama** | foydalanuvchilar eʼtiboridan | ijtimoiy tarmoqlar, qidiruv tizimlari |

## Elektron tijorat infratuzilmasi

- **Sayt va mobil ilova**: katalog, qidiruv, savat, buyurtma.
- **Toʻlov**: bank kartalari (Uzcard, Humo, Visa), toʻlov tizimlari (Click, Payme va boshq.), QR toʻlov, muddatli toʻlov (BNPL), yetkazib berishda naqd.
- **Logistika**: omborlar, kuryerlik xizmatlari, topshirish punktlari, buyurtma holatini kuzatish.
- **Mijozlarga xizmat**: chat, chatbot, qaytarish va almashtirish siyosati, sharhlar.
- **Tahlil**: CRM (M3), veb-analitika, A/B testlar.

## Mobil tijorat

**Mobil tijorat** (m-commerce) — smartfon orqali xarid va toʻlov. Xususiyatlari: doim yonda boʻlish, joylashuvga asoslangan xizmatlar, push-xabarlar, biometrik tasdiqlash, bir tugma bilan toʻlov. Oʻzbekistonda keng tarqalgan: mobil banking, Click va Payme orqali kommunal va davlat toʻlovlari, QR orqali doʻkonlarda toʻlov, marketpleys ilovalari, taksi va yetkazib berish. **Superilova** — bitta ilovada toʻlov, xarid, taksi, bank xizmatlari (Osiyo tajribasi: WeChat, Grab).

## Raqamli marketing koʻrsatkichlari

| Koʻrsatkich | Formula | Maʼnosi |
|---|---|---|
| **Konversiya** | buyurtmalar / tashriflar × 100% | tashrif buyuruvchilarning qancha qismi xarid qildi |
| **Oʻrtacha chek** (AOV) | tushum / buyurtmalar | bitta buyurtmaning oʻrtacha summasi |
| **CAC** (mijoz jalb qilish narxi) | reklama xarajati / yangi mijozlar (buyurtmalar) | bitta xaridorni jalb qilish qanchaga tushdi |
| **ROAS** | tushum / reklama xarajati | reklamaga sarflangan har soʻm qancha tushum keltirdi |
| **LTV** (mijozning umr boʻyi qiymati) | mijoz butun davr davomida keltiradigan foyda | CAC < LTV boʻlsa, jalb qilish oʻzini oqlaydi |

## Ishlangan misol: reklama kanallarini solishtirish

Internet-doʻkon bir oyda uchta kanal orqali reklama qildi. Har bir satrda: kanal, tashriflar, buyurtmalar, tushum, reklama xarajati (soʻm). Konversiya, oʻrtacha chek, CAC va ROAS ni chiqaring.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    kanal, tashrif, buyurtma, tushum, reklama = satr.split()
    tashrif, buyurtma, tushum, reklama = int(tashrif), int(buyurtma), int(tushum), int(reklama)
    konversiya = 100 * buyurtma / tashrif               # foizda
    chek = tushum // buyurtma                            # oʻrtacha chek
    cac = reklama // buyurtma                            # bitta xaridor narxi
    roas = tushum / reklama                              # reklama rentabelligi
    print(f"{kanal}: konversiya {konversiya:.2f}%, chek {chek}, CAC {cac}, ROAS {roas:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String kanal = in.next();
            long tashrif = in.nextLong(), buyurtma = in.nextLong(), tushum = in.nextLong(), reklama = in.nextLong();
            double konversiya = 100.0 * buyurtma / tashrif;          // foizda
            long chek = tushum / buyurtma;                            // oʻrtacha chek
            long cac = reklama / buyurtma;                            // bitta xaridor narxi
            double roas = (double) tushum / reklama;                  // reklama rentabelligi
            System.out.println(String.format("%s: konversiya %.2f%%, chek %d, CAC %d, ROAS %.2f", kanal, konversiya, chek, cac, roas));
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
    string kanal;
    long long tashrif, buyurtma, tushum, reklama;
    while (cin >> kanal >> tashrif >> buyurtma >> tushum >> reklama) {
        double konversiya = 100.0 * buyurtma / tashrif;              // foizda
        long long chek = tushum / buyurtma;                           // oʻrtacha chek
        long long cac = reklama / buyurtma;                           // bitta xaridor narxi
        double roas = (double) tushum / reklama;                      // reklama rentabelligi
        printf("%s: konversiya %.2f%%, chek %lld, CAC %lld, ROAS %.2f\n", kanal.c_str(), konversiya, chek, cac, roas);
    }
    return 0;
}
```

Kirish:

```text
Instagram 12000 240 36000000 6000000
Telegram 8000 200 26000000 3000000
Google 15000 180 31500000 9000000
```

Chiqish:

```text
Instagram: konversiya 2.00%, chek 150000, CAC 25000, ROAS 6.00
Telegram: konversiya 2.50%, chek 130000, CAC 15000, ROAS 8.67
Google: konversiya 1.20%, chek 175000, CAC 50000, ROAS 3.50
```

Google eng koʻp tashrif va eng katta oʻrtacha chekni berdi, lekin xaridor eng qimmatga tushdi (CAC 50 000) va ROAS eng past. Telegram — eng samarali: har soʻm reklamaga 8,67 soʻm tushum. Lekin xulosa uchun tushum emas, **foyda** muhim: agar tovar marjasi 20% boʻlsa, ROAS 3,5 kanal (har soʻmga 0,7 soʻm foyda) zarar keltiradi. Byudjetni shu tahlil asosida qayta taqsimlash mumkin.

## Huquqiy asoslar va xavflar

- "Elektron tijorat toʻgʻrisida"gi Qonun, isteʼmolchilar huquqlarini himoya qilish, shaxsga doir maʼlumotlar va soliq qonunchiligi (onlayn kassa, elektron chek).
- Xavflar: firibgar internet-doʻkonlar va soxta toʻlov sahifalari, maʼlumot sizishi, yetkazib berishdagi muammolar, platformaga qaramlik (komissiyalar oshishi).
- Himoya: rasmiy saytlar, 3-D Secure, xaridor sharhlari, aniq qaytarish siyosati, maʼlumotlarni himoya qilish.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat tashriflar soniga qarash.** Muhimi — konversiya, CAC va foyda.
- **Tushumni foyda bilan adashtirish.** Yuqori ROAS ham marja past boʻlsa zarar boʻlishi mumkin.
- **Mobil tajribani eʼtiborsiz qoldirish.** Xaridlarning katta qismi telefondan — sekin yoki noqulay sayt mijozni yoʻqotadi.
- **Qaytarish va yetkazib berish shartlarini yashirish.** Isteʼmolchi huquqlari buziladi va ishonch yoʻqoladi.
:::

:::tip
Misoldagi kanallar uchun marja 30% deb hisoblab, har bir kanal foydasini toping (tushum × 0,3 − reklama). Qaysi kanal byudjetini oshirish va qaysisini qisqartirish kerak?
:::

## Xulosa

- Elektron biznes — jarayonlarning raqamlashuvi; elektron tijorat — Internet orqali savdo va toʻlov.
- Modellar: B2C, B2B, C2C, B2G, D2C; biznes modellari: doʻkon, marketpleys, obuna, platforma, freemium, reklama.
- Infratuzilma: sayt va ilova, toʻlov, logistika, xizmat, tahlil; mobil tijorat — smartfon orqali xarid va QR toʻlov.
- Koʻrsatkichlar: konversiya, oʻrtacha chek, CAC, ROAS, LTV; qaror foyda asosida qabul qilinadi.

## Oʻzingizni tekshiring

:::check
Q: Elektron biznes va elektron tijoratning farqi nima?
A: Elektron biznes — barcha biznes jarayonlarining raqamlashuvi; elektron tijorat — uning qismi, Internet orqali sotish va sotib olish.
Q: Marketpleys qanday daromad qiladi?
A: Sotuvchilardan komissiya va reklama orqali.
Q: CAC qanday hisoblanadi?
A: Reklama xarajati / jalb qilingan mijozlar (buyurtmalar) soni.
Q: ROAS 3,5 boʻlsa, bu nimani bildiradi?
A: Reklamaga sarflangan har soʻm 3,5 soʻm tushum keltirgan.
:::

:::link https://unctad.org/topic/ecommerce-and-digital-economy
UNCTAD: E-commerce and the Digital Economy
Elektron tijorat va raqamli iqtisodiyot boʻyicha xalqaro tadqiqotlar va statistika (ingliz tilida).
:::
