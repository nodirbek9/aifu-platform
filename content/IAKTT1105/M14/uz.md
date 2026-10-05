---
title: Axborot xavfsizligini taʼminlash usullari
summary: Axborot xavfsizligi tushunchasi va uchlik — maxfiylik, yaxlitlik, mavjudlik; tahdidlar manbalari (inson xatosi, firibgarlik, zararli dasturlar, nosozliklar, tabiiy ofatlar); himoya usullari — huquqiy, tashkiliy, texnik va dasturiy, jismoniy; autentifikatsiya va kirish huquqlari, zaxira nusxalar (3-2-1 qoidasi), yangilanishlar; xavfsizlik siyosati va xodimlarni oʻqitish; risklarni iqtisodiy baholash — ALE va ROSI; Oʻzbekistonda axborot xavfsizligi qonunchiligi; risk hisoblash dasturi.
---

## Nima uchun bu mavzu muhim?

Korxona uchun maʼlumot — pul bilan teng aktiv: mijozlar bazasi, buxgalteriya, shartnomalar, bank hisoblari. Bitta xodimning firibgar xatga ishonishi, zaxira nusxasiz qolgan server yoki yangilanmagan dastur kompaniyaga katta zarar keltirishi mumkin. Iqtisodchi uchun savol: **qaysi himoya choralariga qancha pul sarflash oqlanadi?**

:::note
**Bu darsdan soʻng siz:**

- axborot xavfsizligining uch asosiy xususiyatini tushuntira olasiz;
- tahdidlar manbalarini va himoya usullari guruhlarini ayta olasiz;
- asosiy himoya choralari — autentifikatsiya, kirish huquqlari, zaxira nusxa, yangilanishlarni bilasiz;
- risklarni ALE va ROSI orqali iqtisodiy baholay olasiz.
:::

## Tushuncha va uchlik

**Axborot xavfsizligi** — axborot va uni qayta ishlovchi tizimlarni ruxsatsiz kirish, oʻzgartirish, yoʻqotish va ishdan chiqishdan himoya qilish holati va choralari. Uning uch asosiy xususiyati (**CIA uchligi**):

| Xususiyat | Maʼnosi | Buzilish misoli |
|---|---|---|
| **Maxfiylik** (Confidentiality) | maʼlumotni faqat ruxsati borlar koʻradi | mijozlar bazasining sizib chiqishi |
| **Yaxlitlik** (Integrity) | maʼlumot ruxsatsiz oʻzgartirilmagan | hisob-fakturadagi bank rekvizitlarini almashtirish |
| **Mavjudlik** (Availability) | maʼlumot va xizmat kerak paytda ishlaydi | server nosozligi tufayli onlayn-doʻkon toʻxtashi |

## Tahdidlar manbalari

| Manba | Misollar |
|---|---|
| **Inson xatosi** | notoʻgʻri faylni oʻchirish, xatni notoʻgʻri adresatga yuborish, parolni stikerda qoldirish |
| **Firibgarlik va ijtimoiy muhandislik** | soxta xatlar va sahifalar (fishing), "rahbar nomidan" pul oʻtkazishni soʻrash, SMS kodni soʻrash |
| **Zararli dasturlar** | viruslar, fayllarni shifrlab toʻlov talab qiluvchi dasturlar (ransomware), josus dasturlar |
| **Ichki tahdid** | xodim tomonidan maʼlumotni olib chiqish yoki suisteʼmol |
| **Texnik nosozliklar** | disk ishdan chiqishi, elektr uzilishi, dastur xatosi |
| **Tabiiy va jismoniy** | yongʻin, suv bosishi, uskunani oʻgʻirlash |

:::tip
Koʻp hodisalar murakkab texnik hujumdan emas, oddiy inson xatosi va firibgarlikdan boshlanadi. Shuning uchun xodimlarni oʻqitish — eng arzon va samarali choralardan biri.
:::

## Himoya usullari

| Guruh | Choralar |
|---|---|
| **Huquqiy** | qonunlar, shartnomalardagi maxfiylik bandlari, javobgarlik |
| **Tashkiliy** | xavfsizlik siyosati, rollar va javobgarlar, xodimlarni oʻqitish, hodisalarga javob rejasi, ishdan ketgan xodim hisobini darhol yopish |
| **Texnik va dasturiy** | autentifikatsiya, kirish huquqlari, shifrlash (S14), antivirus, tarmoq ekrani (firewall), yangilanishlar, zaxira nusxa, jurnallar |
| **Jismoniy** | server xonasiga kirishni cheklash, kuzatuv kameralari, uzluksiz quvvat manbai (UPS), yongʻin xavfsizligi |

## Asosiy texnik choralar

- **Autentifikatsiya** — foydalanuvchi shaxsini tasdiqlash. Uzun va noyob parollar (parol menejeri bilan), **koʻp faktorli autentifikatsiya (MFA)**: parol + telefon kodi yoki ilova tasdigʻi.
- **Kirish huquqlari** — **minimal imtiyoz** tamoyili: har bir xodim faqat ishi uchun kerak boʻlgan maʼlumotni koʻradi (kassir — buxgalteriya hisobotlarini emas).
- **Yangilanishlar** — operatsion tizim va dasturlardagi maʼlum xatolar yangilanishlar bilan tuzatiladi; eskirgan tizim — ochiq eshik.
- **Zaxira nusxa** — **3-2-1 qoidasi**: maʼlumotning 3 nusxasi, 2 xil tashuvchida, 1 tasi boshqa joyda (masalan, bulutda yoki oflayn). Zaxiradan tiklashni muntazam sinab koʻrish kerak.
- **Shifrlash** — noutbuk diski, zaxira nusxalar va tarmoq trafigi (HTTPS) shifrlanadi.
- **Jurnallar va monitoring** — kim qachon nimaga kirganini yozib borish, shubhali hodisalarni aniqlash.

## Risklarni iqtisodiy baholash

**Risk** = hodisa ehtimoli × zarar. Iqtisodiy baholashda keng qoʻllanadigan koʻrsatkichlar:

| Koʻrsatkich | Formula | Maʼnosi |
|---|---|---|
| **SLE** (bir martalik zarar) | aktiv qiymati × zarar ulushi | bitta hodisa qancha zarar keltiradi |
| **ARO** (yillik chastota) | yiliga necha marta yuz berishi kutiladi | 0,5 = ikki yilda bir marta |
| **ALE** (yillik kutilgan zarar) | SLE × ARO | bir yilda oʻrtacha qancha yoʻqotamiz |
| **ROSI** (xavfsizlik investitsiyasi rentabelligi) | (ALE kamayishi − chora narxi) / chora narxi × 100% | chora oʻzini oqlaydimi |

## Ishlangan misol: himoya choralarini tanlash

Kompaniya toʻrtta tahdid uchun choralarni koʻrib chiqmoqda. Har bir satrda: tahdid, bitta hodisa zarari (SLE, soʻm), choragacha va choradan keyingi chastota (**10 yilda necha marta**), choraning yillik narxi. Har bir tahdid uchun ALE oldin va keyin, yillik foyda va ROSI ni hisoblang; ROSI musbat boʻlsa — chorani joriy etish, aks holda — qayta koʻrib chiqish.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, sle, oldin, keyin, narx = satr.split()
    sle, oldin, keyin, narx = int(sle), int(oldin), int(keyin), int(narx)
    ale_oldin = sle * oldin // 10                     # yillik kutilgan zarar
    ale_keyin = sle * keyin // 10
    foyda = ale_oldin - ale_keyin                     # yiliga tejaladigan zarar
    rosi = (foyda - narx) * 100 // narx               # foizda
    qaror = "joriy etish" if rosi > 0 else "qayta koʻrib chiqish"
    print(f"{nom}: ALE {ale_oldin} -> {ale_keyin}, foyda {foyda}, ROSI {rosi}%, {qaror}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            long sle = in.nextLong(), oldin = in.nextLong(), keyin = in.nextLong(), narx = in.nextLong();
            long aleOldin = sle * oldin / 10;                  // yillik kutilgan zarar
            long aleKeyin = sle * keyin / 10;
            long foyda = aleOldin - aleKeyin;                  // yiliga tejaladigan zarar
            long rosi = (foyda - narx) * 100 / narx;           // foizda
            String qaror = rosi > 0 ? "joriy etish" : "qayta koʻrib chiqish";
            System.out.println(nom + ": ALE " + aleOldin + " -> " + aleKeyin + ", foyda " + foyda + ", ROSI " + rosi + "%, " + qaror);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    string nom;
    long long sle, oldin, keyin, narx;
    while (cin >> nom >> sle >> oldin >> keyin >> narx) {
        long long aleOldin = sle * oldin / 10;                 // yillik kutilgan zarar
        long long aleKeyin = sle * keyin / 10;
        long long foyda = aleOldin - aleKeyin;                 // yiliga tejaladigan zarar
        long long rosi = (foyda - narx) * 100 / narx;          // foizda
        string qaror = rosi > 0 ? "joriy etish" : "qayta koʻrib chiqish";
        cout << nom << ": ALE " << aleOldin << " -> " << aleKeyin << ", foyda " << foyda
             << ", ROSI " << rosi << "%, " << qaror << "\n";
    }
    return 0;
}
```

Kirish:

```text
phishing 20000000 30 6 30000000
ransomware 300000000 2 1 40000000
outage 50000000 10 4 12000000
laptop 15000000 8 2 10000000
```

Chiqish:

```text
phishing: ALE 60000000 -> 12000000, foyda 48000000, ROSI 60%, joriy etish
ransomware: ALE 60000000 -> 30000000, foyda 30000000, ROSI -25%, qayta koʻrib chiqish
outage: ALE 50000000 -> 20000000, foyda 30000000, ROSI 150%, joriy etish
laptop: ALE 12000000 -> 3000000, foyda 9000000, ROSI -10%, qayta koʻrib chiqish
```

Xodimlarni fishingga qarshi oʻqitish (ROSI 60%) va zaxira elektr taʼminoti (150%) aniq oʻzini oqlaydi. Ransomware uchun tanlangan chora hisob boʻyicha manfiy — lekin ehtiyot boʻling: ALE "oʻrtacha" yilni koʻrsatadi, bitta hodisa esa 300 mln soʻm, yaʼni kompaniyani toʻxtatib qoʻyishi mumkin. Bunday kam uchraydigan, lekin halokatli risklar uchun arzonroq chora (masalan, 3-2-1 zaxira nusxa) izlash yoki riskni sugʻurtalash kerak. Noutbuklar uchun esa arzonroq yechim — diskni shifrlash — bilan qayta hisoblash mumkin.

## Riskka javob strategiyalari

| Strategiya | Mazmuni | Misol |
|---|---|---|
| **Kamaytirish** | himoya chorasi joriy etish | MFA, zaxira nusxa |
| **Oʻtkazish** | riskni boshqaga oʻtkazish | kiber-sugʻurta, xizmatni provayderga berish |
| **Qabul qilish** | zarar kichik — chora qimmat | eski printer ishdan chiqishi |
| **Chetlab oʻtish** | xavfli faoliyatdan voz kechish | kerak boʻlmagan maʼlumotlarni yigʻmaslik |

## Xavfsizlik siyosati va qonunchilik

**Xavfsizlik siyosati** — tashkilotning yozma qoidalari: parollar, qurilmalar, maʼlumot turlari va ularga kirish, zaxira, hodisalarda kimga xabar berish. Xalqaro standart — **ISO/IEC 27001** (axborot xavfsizligini boshqarish tizimi).

**Oʻzbekistonda**: "Axborotlashtirish toʻgʻrisida", "Shaxsga doir maʼlumotlar toʻgʻrisida", "Kiberxavfsizlik toʻgʻrisida"gi qonunlar; kiberxavfsizlik sohasida vakolatli organ va kiberhodisalarga javob berish markazi faoliyat yuritadi. Shaxsiy maʼlumotlar sizib chiqishi uchun tashkilot javobgar.

:::warning
**Koʻp uchraydigan xatolar**

- **"Bizda oʻgʻirlaydigan narsa yoʻq" deb oʻylash.** Kichik kompaniyalar ham fishing va ransomware nishoniga aylanadi.
- **Zaxira nusxani tekshirmaslik.** Tiklab boʻlmaydigan zaxira — zaxira emas.
- **Hammaga administrator huquqi berish.** Bitta xato yoki buzilgan hisob butun tizimga taʼsir qiladi.
- **Faqat texnik choralarga tayanish.** Oʻqitilmagan xodim eng yaxshi himoyani ham chetlab oʻtishi mumkin.
- **Risklarni faqat ALE bilan baholash.** Kam uchraydigan, lekin halokatli hodisalarni alohida koʻrib chiqing.
:::

:::tip
Misoldagi "laptop" tahdidi uchun yillik narxi 5 000 000 soʻm boʻlgan diskni shifrlash chorasi (choradan keyin chastota 10 yilda 2 marta) ROSI ni qanday oʻzgartiradi? Hisoblab koʻring.
:::

## Xulosa

- Axborot xavfsizligi — maxfiylik, yaxlitlik va mavjudlikni taʼminlash.
- Tahdidlar: inson xatosi, firibgarlik, zararli dasturlar, ichki tahdid, nosozliklar, tabiiy hodisalar.
- Himoya usullari: huquqiy, tashkiliy, texnik va dasturiy, jismoniy.
- Asosiy choralar: MFA, minimal imtiyoz, yangilanishlar, 3-2-1 zaxira, shifrlash, xodimlarni oʻqitish.
- Risk iqtisodiy baholanadi: ALE = SLE × ARO, ROSI choraning oʻzini oqlashini koʻrsatadi.

## Oʻzingizni tekshiring

:::check
Q: Axborot xavfsizligining uch asosiy xususiyati qaysilar?
A: Maxfiylik, yaxlitlik va mavjudlik.
Q: Minimal imtiyoz tamoyili nimani bildiradi?
A: Har bir foydalanuvchi faqat ishi uchun zarur boʻlgan maʼlumot va funksiyalarga kirish huquqiga ega boʻladi.
Q: 3-2-1 zaxira qoidasi nima?
A: Maʼlumotning 3 nusxasi, 2 xil tashuvchida, 1 tasi boshqa joyda saqlanadi.
Q: SLE 40 mln soʻm, ARO 0,5 boʻlsa, ALE qancha?
A: 40 mln × 0,5 = 20 mln soʻm yiliga.
:::

:::link https://www.iso.org/standard/27001
ISO/IEC 27001
Axborot xavfsizligini boshqarish tizimi xalqaro standarti (ingliz tilida).
:::
