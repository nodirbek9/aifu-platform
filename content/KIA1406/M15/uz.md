---
title: Bulutli hisoblash va IoT xavfsizligi. Bulutli tizimlar, maxfiylik masalalari, IoT arxitekturasi va tahdidlar
summary: Bulutli hisoblash modellari va umumiy javobgarlik; bulutdagi asosiy xavf toifalari (notoʻgʻri sozlamalar, hisoblar va kalitlarning buzilishi, xavfsiz boʻlmagan interfeyslar, maʼlumot sizishi); himoya choralari — IAM, shifrlash va kalitlarni boshqarish, CSPM va CASB, jurnallar; maxfiylik — maʼlumot joylashuvi, lokallashtirish, shaxsiy maʼlumotlar; IoT arxitekturasi (qurilma, shlyuz va tarmoq, bulut, ilova); IoT tahdidlari va ularning sabablari; bazaviy xavfsizlik talablari (ETSI EN 303 645, OWASP IoT); tarmoqni segmentlash; IoT qurilmalari auditini bajaruvchi dastur.
---

## Nima uchun bu mavzu muhim?

Tashkilotlar maʼlumot va ilovalarini tobora koʻproq bulutga koʻchirmoqda, ofis va uylarda esa kameralar, termostatlar, aqlli qulflar, sensorlar — **narsalar Interneti** (IoT) qurilmalari koʻpaymoqda. Ikkalasining umumiy jihati: tizim chegarasi endi tashkilot binosi bilan cheklanmaydi. Xavfsizlik mutaxassisi bulutdagi sozlamalar va minglab kichik qurilmalarni nazorat qila bilishi kerak.

:::note
**Bu darsdan soʻng siz:**

- bulutdagi umumiy javobgarlik modelini va asosiy xavf toifalarini tushuntira olasiz;
- bulutni himoya qilish choralarini (IAM, shifrlash, CSPM, CASB, jurnallar) ayta olasiz;
- bulut va IoT dagi maxfiylik masalalarini tahlil qila olasiz;
- IoT arxitekturasi qatlamlari va har biridagi himoya choralarini bilasiz;
- IoT qurilmalarini bazaviy talablar boʻyicha audit qila olasiz.
:::

## Bulutli hisoblash va umumiy javobgarlik

Bulut xizmat modellari — **IaaS**, **PaaS**, **SaaS** — va joylashtirish modellari — **ommaviy**, **xususiy**, **gibrid**, **jamoaviy**. Xavfsizlikning asosiy qoidasi — **umumiy javobgarlik modeli**:

| Qatlam | IaaS | PaaS | SaaS |
|---|---|---|---|
| Jismoniy infratuzilma, virtualizatsiya | provayder | provayder | provayder |
| Operatsion tizim, yangilanishlar | mijoz | provayder | provayder |
| Ilova | mijoz | mijoz | provayder |
| Maʼlumotlar, kirish huquqlari, sozlamalar | **mijoz** | **mijoz** | **mijoz** |

Har qanday modelda maʼlumot, hisoblar va sozlamalar mijoz javobgarligida qoladi.

## Bulutdagi asosiy xavf toifalari

| Toifa | Mazmuni | Asosiy himoya |
|---|---|---|
| **Notoʻgʻri sozlamalar** | ommaviy ochiq qolgan saqlash, ortiqcha ochiq portlar, standart sozlamalar | sozlamalarni avtomatik tekshirish (CSPM), "sukut boʻyicha yopiq" |
| **Hisoblar va kalitlarning buzilishi** | oʻgʻirlangan parol, kodda qolgan API kaliti | MFA, maxfiy maʼlumotlar ombori, kalitlarni almashtirish |
| **Ortiqcha huquqlar** | foydalanuvchi yoki xizmatga keragidan koʻp ruxsat | minimal imtiyoz, rollar, muntazam koʻrib chiqish |
| **Xavfsiz boʻlmagan interfeys va API** | autentifikatsiyasiz yoki zaif himoyalangan API | autentifikatsiya, avtorizatsiya, soʻrovlar limiti |
| **Maʼlumot sizishi** | shifrlanmagan saqlash yoki zaxira | saqlashda va uzatishda shifrlash |
| **Koʻrinuvchanlik yetishmasligi** | kim nima qilganini bilmaslik | markazlashgan jurnallar, SIEM (M14) |

## Bulutni himoya qilish choralari

- **IAM** (identifikatsiya va kirishni boshqarish): har bir xodimning shaxsiy hisobi, MFA, rollar, vaqtinchalik yuqori huquqlar, ishdan ketganda hisobni darhol yopish.
- **Shifrlash**: uzatishda (TLS) va saqlashda; **kalitlarni boshqarish xizmati** (KMS), zarur boʻlsa — **BYOK** (mijozning oʻz kaliti).
- **CSPM** (Cloud Security Posture Management) — bulut sozlamalarini xavfsizlik siyosati bilan doimiy solishtirish va chetga chiqishlarni koʻrsatish.
- **CASB** (Cloud Access Security Broker) — xodimlarning bulut xizmatlaridan foydalanishini nazorat qilish, ruxsatsiz xizmatlarni ("soya IT") aniqlash, DLP siyosatlarini qoʻllash (M14).
- **Jurnallar va monitoring**: barcha boshqaruv amallari jurnallanadi, gʻayrioddiy faollik haqida ogohlantirish.
- **Zaxira**: kamida bitta nusxa boshqa hisob yoki mintaqada; tiklash muntazam sinaladi.

## Maxfiylik masalalari

- **Maʼlumot joylashuvi**: maʼlumot qaysi mamlakat serverida saqlanadi va qaysi qonunchilik amal qiladi.
- **Lokallashtirish**: Oʻzbekiston fuqarolarining shaxsiy maʼlumotlari mamlakat hududidagi serverlarda saqlanishi kerak ("Shaxsga doir maʼlumotlar toʻgʻrisida"gi Qonun).
- **Provayder xodimlarining kirishi**: qanday sharoitda va qanday jurnallar bilan.
- **Maʼlumotni oʻchirish**: shartnoma tugaganda maʼlumot qanday va qachon oʻchiriladi.
- **IoT va maxfiylik**: kamera, mikrofon va joylashuv sensorlari shaxsiy hayotga oid maʼlumot yigʻadi — faqat zarur maʼlumot yigʻilishi, foydalanuvchi xabardor boʻlishi va rozilik berishi kerak.

## IoT arxitekturasi

| Qatlam | Tarkibi | Himoya choralari |
|---|---|---|
| **Qurilma (sezish)** | sensorlar, kameralar, ijro mexanizmlari, mikrokontroller va proshivka | noyob parol, xavfsiz yuklash, imzolangan yangilanishlar, keraksiz xizmatlarni oʻchirish |
| **Shlyuz va tarmoq** | Wi-Fi, Zigbee, Bluetooth LE, LoRaWAN, mobil aloqa, shlyuzlar | shifrlangan protokollar, alohida tarmoq segmenti, tarmoq ekrani |
| **Bulut platformasi** | qurilmalarni boshqarish, maʼlumot yigʻish va tahlil | qurilma sertifikatlari, IAM, API himoyasi |
| **Ilova** | mobil va veb-ilovalar, dashbordlar | autentifikatsiya, MFA, xavfsiz sessiyalar |

## IoT tahdidlari va ularning sabablari

IoT qurilmalari koʻpincha arzon, cheklangan resursli va yillar davomida yangilanmasdan ishlaydi. Shuning uchun quyidagi **zaifliklar** keng tarqalgan:

| Zaiflik | Nima uchun xavfli |
|---|---|
| **Standart yoki zaif parollar** | barcha qurilmalarda bir xil zavod paroli — begona shaxs boshqaruvni egallashi mumkin |
| **Yangilanmaydigan proshivka** | maʼlum zaifliklar yillar davomida yopilmaydi |
| **Keraksiz ochiq xizmatlar** | masalan, eskirgan va shifrlanmagan telnet yoki HTTP boshqaruv paneli |
| **Shifrlanmagan aloqa** | maʼlumot va parollar tarmoqda ochiq uzatiladi |
| **Xavfsiz boʻlmagan bulut va ilova interfeyslari** | zaif autentifikatsiya, ortiqcha maʼlumot |
| **Jismoniy kirish** | qurilma ochiq joyda, portlari himoyalanmagan |

**Tarixiy saboq — Mirai** (2016): standart parollari oʻzgartirilmagan yuz minglab kameralar va routerlar zararli dastur bilan egallanib, botnetga birlashtirilgan va yirik xizmatlarga qarshi DDoS hujumlarida ishlatilgan. Asosiy sabab murakkab texnologiya emas, balki **oʻzgartirilmagan zavod parollari** edi. Shu hodisadan keyin koʻp mamlakatlar IoT uchun majburiy bazaviy talablarni joriy qila boshladi.

## Bazaviy xavfsizlik talablari

**ETSI EN 303 645** — isteʼmolchi IoT qurilmalari uchun Yevropa standarti; asosiy talablari:

1. Universal standart parollar yoʻq — har bir qurilmaning noyob paroli yoki birinchi ishga tushirishda parol oʻrnatish.
2. Zaifliklar haqida xabar qabul qilish siyosati.
3. Dasturiy taʼminotni xavfsiz yangilash va yangilanish muddatini eʼlon qilish.
4. Maxfiy maʼlumotlarni (kalitlar, parollar) xavfsiz saqlash.
5. Xavfsiz aloqa (shifrlash).
6. Hujum yuzasini kamaytirish — keraksiz xizmat va portlarni oʻchirish.
7. Shaxsiy maʼlumotlarni himoya qilish va foydalanuvchiga oʻchirish imkonini berish.

**OWASP IoT Top 10** ham shu yoʻnalishdagi asosiy xavflar roʻyxatini beradi. Ayrim mamlakatlar bunday talablarni qonun bilan majburiy qilgan (masalan, Buyuk Britaniyada 2024-yildan standart parollar taqiqlangan).

## Tarmoqni segmentlash

IoT qurilmalarini asosiy ishchi kompyuterlar va serverlar bilan bitta tarmoqda saqlamaslik kerak. **Alohida segment** (VLAN yoki mehmon Wi-Fi) va tarmoq ekrani qoidalari: qurilmalar faqat oʻz bulut xizmatiga chiqa oladi, ichki tarmoqqa kira olmaydi. Qurilma buzilsa ham, zarar shu segment bilan cheklanadi (M14 dagi chuqur himoya va nol ishonch).

## Ishlangan misol: IoT qurilmalari auditi

Har bir satrda qurilma: nom, turi, joriy proshivka, oxirgi proshivka, standart parol (`yes`/`no`), ochiq portlar (vergul bilan yoki `-`). Bazaviy talablar boʻyicha xavf balli hisoblanadi: standart parol +5, eskirgan proshivka +3, telnet (23-port) ochiq +4, shifrlanmagan HTTP boshqaruv (80-port) +2. 7 va undan yuqori — yuqori, 3–6 — oʻrta, 0–2 — past xavf. Har bir qurilma uchun tavsiyalar chiqariladi.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, _, joriy, oxirgi, standart, portlar = satr.split()
    portlar = [] if portlar == "-" else portlar.split(",")
    ball, tavsiya = 0, []
    if standart == "yes":                           # universal zavod paroli
        ball += 5
        tavsiya.append("parolni almashtirish")
    if joriy != oxirgi:                             # maʼlum zaifliklar yopilmagan
        ball += 3
        tavsiya.append("proshivkani yangilash")
    if "23" in portlar:                             # telnet — shifrlanmagan boshqaruv
        ball += 4
        tavsiya.append("telnetni oʻchirish")
    if "80" in portlar:                             # HTTP boshqaruv paneli
        ball += 2
        tavsiya.append("HTTPS ga oʻtish")
    if ball >= 7:
        daraja = "yuqori"
    elif ball >= 3:
        daraja = "oʻrta"
    else:
        daraja = "past"
    matn = ", ".join(tavsiya) if tavsiya else "choralar talab qilinmaydi"
    print(f"{nom}: ball {ball}, {daraja} xavf: {matn}")
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            in.next();
            String joriy = in.next(), oxirgi = in.next(), standart = in.next(), p = in.next();
            List<String> portlar = p.equals("-") ? new ArrayList<>() : Arrays.asList(p.split(","));
            int ball = 0;
            List<String> tavsiya = new ArrayList<>();
            if (standart.equals("yes")) { ball += 5; tavsiya.add("parolni almashtirish"); }       // universal zavod paroli
            if (!joriy.equals(oxirgi)) { ball += 3; tavsiya.add("proshivkani yangilash"); }        // maʼlum zaifliklar yopilmagan
            if (portlar.contains("23")) { ball += 4; tavsiya.add("telnetni oʻchirish"); }          // telnet — shifrlanmagan boshqaruv
            if (portlar.contains("80")) { ball += 2; tavsiya.add("HTTPS ga oʻtish"); }             // HTTP boshqaruv paneli
            String daraja = ball >= 7 ? "yuqori" : ball >= 3 ? "oʻrta" : "past";
            String matn = tavsiya.isEmpty() ? "choralar talab qilinmaydi" : String.join(", ", tavsiya);
            System.out.println(nom + ": ball " + ball + ", " + daraja + " xavf: " + matn);
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string nom, tur, joriy, oxirgi, standart, p;
    while (cin >> nom >> tur >> joriy >> oxirgi >> standart >> p) {
        vector<string> portlar;
        if (p != "-") {
            stringstream ss(p);
            string x;
            while (getline(ss, x, ',')) portlar.push_back(x);
        }
        auto bor = [&](const string& port) {
            for (auto& x : portlar) if (x == port) return true;
            return false;
        };
        int ball = 0;
        vector<string> tavsiya;
        if (standart == "yes") { ball += 5; tavsiya.push_back("parolni almashtirish"); }     // universal zavod paroli
        if (joriy != oxirgi) { ball += 3; tavsiya.push_back("proshivkani yangilash"); }      // maʼlum zaifliklar yopilmagan
        if (bor("23")) { ball += 4; tavsiya.push_back("telnetni oʻchirish"); }               // telnet — shifrlanmagan boshqaruv
        if (bor("80")) { ball += 2; tavsiya.push_back("HTTPS ga oʻtish"); }                  // HTTP boshqaruv paneli
        string daraja = ball >= 7 ? "yuqori" : ball >= 3 ? "oʻrta" : "past";
        string matn;
        for (size_t i = 0; i < tavsiya.size(); i++) matn += (i ? ", " : "") + tavsiya[i];
        if (tavsiya.empty()) matn = "choralar talab qilinmaydi";
        cout << nom << ": ball " << ball << ", " << daraja << " xavf: " << matn << "\n";
    }
    return 0;
}
```

Kirish:

```text
camera-01 camera 2.1.0 2.4.1 yes 23,80,554
thermostat thermostat 5.0.2 5.0.2 no 443
router-gw router 1.9 2.0 no 22,443
lock-door lock 3.3 3.3 no -
tv-lobby tv 7.2 7.2 yes 80
```

Chiqish:

```text
camera-01: ball 14, yuqori xavf: parolni almashtirish, proshivkani yangilash, telnetni oʻchirish, HTTPS ga oʻtish
thermostat: ball 0, past xavf: choralar talab qilinmaydi
router-gw: ball 3, oʻrta xavf: proshivkani yangilash
lock-door: ball 0, past xavf: choralar talab qilinmaydi
tv-lobby: ball 7, yuqori xavf: parolni almashtirish, HTTPS ga oʻtish
```

Kamera bazaviy talablarning toʻrttasini ham buzgan — u birinchi navbatda tuzatiladi yoki tuzatilgunga qadar tarmoqdan uziladi. Audit natijasi tashkilotning IoT siyosatiga aylanadi: yangi qurilma faqat noyob parol, yangilangan proshivka va keraksiz xizmatlar oʻchirilgan holda alohida segmentga ulanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **"Bulutda boʻlsa — provayder himoya qiladi" deb oʻylash.** Sozlamalar va huquqlar — mijoz javobgarligi.
- **IoT qurilmasini "kichik va xavfsiz" deb hisoblash.** U tarmoqqa kirish nuqtasi boʻlishi mumkin.
- **Zavod parolini qoldirish.** Eng koʻp uchraydigan va eng xavfli xato.
- **Qurilmalar roʻyxati yoʻqligi.** Bilmagan qurilmangizni himoya qila olmaysiz.
- **IoT ni ishchi tarmoq bilan bir segmentda saqlash.**
:::

:::tip
Uyingiz yoki ofisingizdagi barcha "aqlli" qurilmalarni roʻyxatga oling (router, kamera, televizor, printer) va misoldagi formatda audit qiling.
:::

## Xulosa

- Bulutda umumiy javobgarlik: maʼlumot, hisoblar va sozlamalar har doim mijozda.
- Asosiy bulut xavflari — notoʻgʻri sozlamalar, buzilgan hisoblar, ortiqcha huquqlar, zaif API; choralar — IAM, shifrlash va KMS, CSPM, CASB, jurnallar, zaxira.
- Maxfiylik: maʼlumot joylashuvi, lokallashtirish, IoT sensorlari yigʻadigan shaxsiy maʼlumot.
- IoT qatlamlari: qurilma, shlyuz va tarmoq, bulut, ilova; asosiy zaifliklar — standart parollar, yangilanmaydigan proshivka, keraksiz xizmatlar.
- Bazaviy talablar (ETSI EN 303 645, OWASP IoT), segmentlash va muntazam audit IoT himoyasining asosi.

## Oʻzingizni tekshiring

:::check
Q: SaaS modelida ham mijoz nimalarga javobgar?
A: Maʼlumotlar, kirish huquqlari va hisoblar, ulashish sozlamalari uchun.
Q: CSPM nima qiladi?
A: Bulut sozlamalarini xavfsizlik siyosati bilan doimiy solishtirib, chetga chiqishlarni aniqlaydi.
Q: Mirai hodisasining asosiy sababi nima edi?
A: IoT qurilmalarida zavod parollarining oʻzgartirilmagani.
Q: Nima uchun IoT qurilmalari alohida tarmoq segmentiga joylashtiriladi?
A: Qurilma buzilsa ham, zarar shu segment bilan cheklanishi va ichki tarmoq himoyalanishi uchun.
:::

:::link https://www.etsi.org/technologies/consumer-iot-security
ETSI: Consumer IoT Security
Isteʼmolchi IoT qurilmalari uchun EN 303 645 standarti va tavsiyalar (ingliz tilida).
:::
