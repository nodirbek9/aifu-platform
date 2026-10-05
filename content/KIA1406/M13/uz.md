---
title: Tarmoq xavfsizligi. OSI va TCP/IP modellari, zaiflik va tahdidlar, DoS hujumlari
summary: Tarmoq xavfsizligining vazifalari; OSI va TCP/IP modellari xavfsizlik nuqtai nazaridan — har bir qatlamdagi tahdid toifalari va himoya choralari; zaiflik, tahdid va xavf tushunchalari; tarmoq xavfsizligining asosiy tamoyillari (segmentlash, minimal ochiqlik, shifrlash, monitoring); xizmat koʻrsatishni rad etish (DoS) va taqsimlangan DoS (DDoS) — tushuncha, toifalar, oqibatlar, botnetlar; DoS ga qarshi himoya — tayyorgarlik, aniqlash, javob; soʻrovlar chastotasini cheklash va uni koʻrsatuvchi dastur; huquqiy jihatlar.
---

## Nima uchun bu mavzu muhim?

Deyarli barcha tizimlar — bank, davlat xizmatlari, savdo — tarmoq orqali ishlaydi. Tarmoq xavfsizligi uchta xususiyatni himoya qiladi: uzatilayotgan maʼlumot **maxfiyligi**, uning **yaxlitligi** va xizmatlarning **mavjudligi** (CIA uchligi). Bu maʼruzada tarmoq modellari qatlamlari boʻyicha qanday tahdid toifalari borligini, ularga qarshi qanday himoya qoʻllanishini va xizmatni ishdan chiqarishga qaratilgan DoS hodisalaridan qanday himoyalanishni koʻramiz. Keyingi maʼruza (M14) himoya vositalarini — tarmoq ekrani, VPN, IDS — batafsil koʻrib chiqadi.

:::note
**Bu darsdan soʻng siz:**

- OSI va TCP/IP qatlamlarini xavfsizlik nuqtai nazaridan tushuntira olasiz;
- har bir qatlamdagi tahdid toifalari va himoya choralarini ayta olasiz;
- zaiflik, tahdid va xavf tushunchalarini farqlay olasiz;
- DoS va DDoS ning mohiyati, oqibatlari va himoya usullarini bilasiz;
- soʻrovlar chastotasini cheklash qoidasini dasturda koʻrsata olasiz.
:::

## OSI va TCP/IP modellari: xavfsizlik nuqtai nazari

Tarmoq vazifalari qatlamlarga boʻlinadi (OSI — 7 qatlam, TCP/IP — 4 qatlam). Xavfsizlik uchun bu muhim: har bir qatlamning oʻz tahdidlari va oʻz himoya vositalari bor, va bitta qatlamdagi himoya boshqasining oʻrnini bosmaydi.

| OSI qatlami | TCP/IP | Tahdid toifasi (mazmuni) | Himoya choralari |
|---|---|---|---|
| **1. Fizik** | Kanal | kabel yoki uskunaga jismoniy kirish, uzish, oʻgʻirlash | fizik xavfsizlik (M12), server xonalarini qulflash, kabellarni himoyalash |
| **2. Kanal** | Kanal | mahalliy tarmoqda manzillarni soxtalashtirib trafikni boshqa qurilmaga yoʻnaltirish; ruxsatsiz qurilmani ulash | kommutatorlarda port xavfsizligi, 802.1X autentifikatsiya, ARP tekshiruvi, VLAN lar bilan segmentlash |
| **3. Tarmoq** | Internet | IP manzilni soxtalashtirish, marshrutlarni notoʻgʻri eʼlon qilish | kiruvchi va chiquvchi filtrlash (BCP 38), marshrutlarni tasdiqlash (RPKI), tarmoq ekrani |
| **4. Transport** | Transport | ulanish resurslarini tugatish, seanslarga aralashish | tarmoq ekrani holatini kuzatish, ulanishlar limiti, operatsion tizim himoya sozlamalari, TLS |
| **5–7. Seans, taqdimot, ilova** | Ilova | zaif veb-ilovalar, fishing, zararli fayllar, DNS soxtalashtirish, ochiq matnli protokollar | HTTPS/TLS, WAF, kiritishni tekshirish, DNSSEC, xavfsiz protokollar (A13), xodimlarni oʻqitish |

**Asosiy xulosa**: tajovuzkor eng zaif qatlamni tanlaydi. Shuning uchun himoya **chuqur** (bir nechta qatlamda) boʻlishi kerak — M14 dagi chuqur himoya tamoyili.

## Zaiflik, tahdid va xavf

| Tushuncha | Taʼrif | Misol |
|---|---|---|
| **Zaiflik** | tizimdagi kamchilik | yangilanmagan router dasturi, standart parol, ochiq qolgan Telnet |
| **Tahdid** | zaiflikdan foydalanishi mumkin boʻlgan hodisa yoki manba | zararli dastur, buzgʻunchi, xodim xatosi, tabiiy ofat |
| **Xavf** | tahdidning zaiflikdan foydalanish ehtimoli × zarar | A2 dagi xavf matritsasi |

Zaifliklar ochiq bazalarda (**CVE**) roʻyxatga olinadi va jiddiyligi (**CVSS**) bilan baholanadi. Tashkilot oʻz tizimlarini muntazam tekshiradi (ruxsat etilgan zaiflik skaneri bilan), yangilanishlarni oʻrnatadi va tuzatib boʻlmaydigan zaifliklar uchun qoʻshimcha choralar koʻradi.

## Tarmoq xavfsizligi tamoyillari

1. **Segmentlash** — tarmoqni zonalarga boʻlish (foydalanuvchilar, serverlar, IoT, DMZ); zonalar orasida faqat kerakli trafik (M14, M15).
2. **Minimal ochiqlik** — faqat kerakli xizmat va portlar ochiq; qolgani "sukut boʻyicha taqiqlangan".
3. **Shifrlash** — tarmoq orqali maxfiy maʼlumot faqat shifrlangan holda (TLS, VPN — A14).
4. **Autentifikatsiya** — qurilma va foydalanuvchi tarmoqqa kirishdan oldin tasdiqlanadi (802.1X, MFA).
5. **Monitoring** — trafik va jurnallarni kuzatish, anomaliyalarni aniqlash (A13, IDS va SIEM — M14).
6. **Yangilanishlar va sozlamalar** — tarmoq uskunalari dasturi yangilangan, standart parollar almashtirilgan.

## Xizmat koʻrsatishni rad etish (DoS va DDoS)

**DoS** (Denial of Service) — tizim yoki xizmatni qonuniy foydalanuvchilar uchun **mavjud boʻlmaydigan** holatga keltirishga qaratilgan harakat. U CIA uchligining **mavjudlik** xususiyatiga qarshi qaratilgan. **DDoS** (Distributed DoS) — bir vaqtning oʻzida koʻplab manbalardan, koʻpincha **botnet** — egallab olingan kompyuterlar va IoT qurilmalari tarmogʻidan (M15 dagi Mirai) amalga oshiriladi; shuning uchun bitta manzilni bloklash yetarli emas.

**Toifalar** (qaysi resurs tugatilishiga koʻra):

| Toifa | Nimaga qaratilgan | Asosiy himoya |
|---|---|---|
| **Hajmga asoslangan** | tarmoq kanalining oʻtkazish qobiliyatini toʻldirish | provayder va maxsus DDoS himoya xizmatlari, CDN, keng kanal zaxirasi |
| **Protokol resurslariga** | server, tarmoq ekrani yoki muvozanatlagich ulanish jadvallarini toʻldirish | operatsion tizim va tarmoq ekrani himoya sozlamalari, ulanish limitlari |
| **Ilova darajasida** | qimmat amallarni (qidiruv, kirish sahifasi) koʻp chaqirib, ilovani band qilish | soʻrovlar chastotasini cheklash, kesh, WAF, bot tekshiruvi |

**Oqibatlar**: xizmatning toʻxtab qolishi va yoʻqotilgan tushum, mijozlar noroziligi, qayta tiklash xarajatlari, obroʻga zarar. Baʼzan DoS boshqa harakatdan eʼtiborni chalgʻitish uchun ham ishlatiladi — shuning uchun hodisa paytida boshqa jurnallar ham kuzatiladi.

**Tarixiy misol**: 2016-yilda Mirai botneti (M15) yirik DNS provayderiga qarshi DDoS hujumi uyushtirgan, natijada koʻplab mashhur saytlar bir necha soat davomida foydalanuvchilarning bir qismi uchun ochilmay qolgan. Saboq: bitta tashqi xizmatga (DNS) qaramlik ham mavjudlik xavfi; zaxira provayderlar va xizmatlarni koʻpaytirish muhim.

## DoS ga qarshi himoya

| Bosqich | Choralar |
|---|---|
| **Tayyorgarlik** | kanal va server quvvati zaxirasi; xizmatlarni bir nechta joyga taqsimlash (CDN, anycast); DDoS himoya xizmati bilan shartnoma; Internet provayder bilan kelishilgan aloqa tartibi; muhim xizmatlar uchun zaxira DNS |
| **Aniqlash** | trafikning odatiy darajasini bilish (bazaviy chiziq), monitoring va ogohlantirishlar (A13), ilovadagi kechikish va xatolar soni |
| **Javob** | oldindan yozilgan reja (playbook): kim nima qiladi, provayderga xabar berish, trafikni tozalash xizmatiga yoʻnaltirish, ilovada himoya rejimini yoqish, mijozlarga xabar |
| **Tiklash va xulosa** | xizmatni tiklash, hodisani tahlil qilish, rejani yangilash |

**Ilova darajasidagi choralar**: soʻrovlar chastotasini cheklash (rate limiting), natijalarni keshlash, WAF qoidalari, avtomatik masshtablash (M11, bulutda — lekin xarajat nazorati bilan), qimmat amallar uchun navbat va limitlar.

**"Yaxshi qoʻshni" tamoyili**: tashkilot oʻz tarmogʻidan soxta manba manzilli paketlar chiqmasligini taʼminlaydi (BCP 38 filtrlash) va oʻz qurilmalarini botnetga aylanishdan himoya qiladi (M15 dagi IoT auditi). Bu boshqalarga qarshi DDoS ehtimolini kamaytiradi.

## Ishlangan misol: soʻrovlar chastotasini cheklash

**Rate limiting** — bitta manzildan maʼlum vaqt oynasida ruxsat etilgan soʻrovlar sonini cheklash. Birinchi satrda limit va oyna: `N W` (W soniya ichida N tadan koʻp soʻrov boʻlmasligi kerak). Keyingi satrlarda vaqt boʻyicha tartiblangan soʻrovlar: soniya va IP manzil. Har bir manzil uchun soʻrovlar sonini va chegaradan birinchi marta qachon oshganini chiqaring (sirpanuvchi oyna: `(t − W, t]`).

```python
import sys
from collections import deque

satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
limit, oyna = map(int, satrlar[0].split())

vaqtlar, soni, birinchi, tartib = {}, {}, {}, []
for satr in satrlar[1:]:
    t, ip = satr.split()
    t = int(t)
    if ip not in vaqtlar:
        vaqtlar[ip] = deque()
        soni[ip] = 0
        tartib.append(ip)
    q = vaqtlar[ip]
    q.append(t)
    while q[0] <= t - oyna:                        # oynadan chiqqan soʻrovlarni olib tashlash
        q.popleft()
    soni[ip] += 1
    if len(q) > limit and ip not in birinchi:      # chegaradan birinchi marta oshdi
        birinchi[ip] = t

for ip in tartib:
    holat = f"chegaradan oshdi (t={birinchi[ip]})" if ip in birinchi else "meʼyorda"
    print(f"{ip}: {soni[ip]} soʻrov, {holat}")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int limit = in.nextInt(), oyna = in.nextInt();
        Map<String, ArrayDeque<Integer>> vaqtlar = new HashMap<>();
        Map<String, Integer> soni = new HashMap<>(), birinchi = new HashMap<>();
        List<String> tartib = new ArrayList<>();
        while (in.hasNext()) {
            int t = in.nextInt();
            String ip = in.next();
            if (!vaqtlar.containsKey(ip)) {
                vaqtlar.put(ip, new ArrayDeque<>());
                soni.put(ip, 0);
                tartib.add(ip);
            }
            ArrayDeque<Integer> q = vaqtlar.get(ip);
            q.addLast(t);
            while (q.peekFirst() <= t - oyna) q.pollFirst();        // oynadan chiqqan soʻrovlarni olib tashlash
            soni.merge(ip, 1, Integer::sum);
            if (q.size() > limit && !birinchi.containsKey(ip)) birinchi.put(ip, t);   // chegaradan birinchi marta oshdi
        }
        for (String ip : tartib) {
            String holat = birinchi.containsKey(ip) ? "chegaradan oshdi (t=" + birinchi.get(ip) + ")" : "meʼyorda";
            System.out.println(ip + ": " + soni.get(ip) + " soʻrov, " + holat);
        }
    }
}
```
```cpp
#include <deque>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

int main() {
    int limit, oyna, t;
    cin >> limit >> oyna;
    map<string, deque<int>> vaqtlar;
    map<string, int> soni, birinchi;
    vector<string> tartib;
    string ip;
    while (cin >> t >> ip) {
        if (!vaqtlar.count(ip)) tartib.push_back(ip);
        deque<int>& q = vaqtlar[ip];
        q.push_back(t);
        while (q.front() <= t - oyna) q.pop_front();               // oynadan chiqqan soʻrovlarni olib tashlash
        soni[ip]++;
        if ((int) q.size() > limit && !birinchi.count(ip)) birinchi[ip] = t;   // chegaradan birinchi marta oshdi
    }
    for (auto& x : tartib) {
        string holat = birinchi.count(x) ? "chegaradan oshdi (t=" + to_string(birinchi[x]) + ")" : "meʼyorda";
        cout << x << ": " << soni[x] << " soʻrov, " << holat << "\n";
    }
    return 0;
}
```

Kirish:

```text
5 10
1 10.0.0.5
2 10.0.0.7
2 10.0.0.5
3 10.0.0.5
4 10.0.0.5
5 10.0.0.5
6 10.0.0.5
8 10.0.0.7
12 10.0.0.9
13 10.0.0.9
14 10.0.0.9
15 10.0.0.5
20 10.0.0.7
```

Chiqish:

```text
10.0.0.5: 7 soʻrov, chegaradan oshdi (t=6)
10.0.0.7: 3 soʻrov, meʼyorda
10.0.0.9: 3 soʻrov, meʼyorda
```

`10.0.0.5` 6-soniyada 10 soniyalik oynada oltinchi soʻrovni yubordi — limit (5) oshdi. Real tizimda shu paytdan boshlab bu manzilning soʻrovlari vaqtincha rad etiladi yoki sekinlashtiriladi (HTTP 429 javobi), qolgan foydalanuvchilar esa normal ishlashda davom etadi. DDoS da manbalar koʻp boʻlgani uchun bu chora yetarli emas — u yuqoridagi tayyorgarlik va provayder darajasidagi himoya bilan birga ishlatiladi.

## Huquqiy jihatlar

DoS va DDoS hujumlarini uyushtirish, botnet yaratish yoki ulardan foydalanish, shuningdek "DDoS xizmatlari"ni buyurtma qilish — koʻp mamlakatlarda, jumladan Oʻzbekistonda, jinoiy javobgarlikka sabab boʻladigan harakatlar (A3). Tashkilot oʻz himoyasini faqat ruxsat etilgan sinovlar bilan, provayder va himoya xizmati bilan kelishilgan holda tekshiradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat bitta qatlamni himoyalash.** Masalan, faqat tarmoq ekrani bilan ilova darajasidagi tahdidlar toʻxtamaydi.
- **DoS ga tayyorgarliksiz qolish.** Hodisa boshlanganda provayder bilan aloqani endi izlash — vaqt yoʻqotish.
- **Odatiy trafik darajasini bilmaslik.** Bazaviy chiziqsiz anomaliyani aniqlab boʻlmaydi.
- **Bitta tashqi xizmatga (DNS, hosting) qaramlik.** Zaxira variantlar kerak.
- **Oʻz qurilmalaringiz botnetga aylanishini eʼtiborsiz qoldirish.** IoT va eski serverlarni audit qiling.
:::

:::tip
Oʻz tashkilotingiz (yoki universitet) saytining mavjudligi uchun bir sahifalik DoS javob rejasini tuzing: masʼullar, provayder kontaktlari, ogohlantirish mezonlari, birinchi 30 daqiqadagi harakatlar.
:::

## Xulosa

- Tarmoq xavfsizligi maxfiylik, yaxlitlik va mavjudlikni himoya qiladi; har bir OSI qatlamining oʻz tahdidlari va himoyasi bor.
- Zaiflik — kamchilik, tahdid — undan foydalanishi mumkin boʻlgan manba, xavf — ehtimol × zarar.
- Tamoyillar: segmentlash, minimal ochiqlik, shifrlash, autentifikatsiya, monitoring, yangilanishlar.
- DoS/DDoS mavjudlikka qarshi; toifalar — hajmga, protokol resurslariga, ilovaga asoslangan; DDoS koʻpincha botnetlardan.
- Himoya: tayyorgarlik (quvvat, CDN, DDoS xizmati, reja), aniqlash (bazaviy chiziq, monitoring), javob va tiklash; ilovada — rate limiting, kesh, WAF.

## Oʻzingizni tekshiring

:::check
Q: DoS hujumi CIA uchligining qaysi xususiyatiga qarshi qaratilgan?
A: Mavjudlikka (Availability).
Q: DDoS ning DoS dan asosiy farqi nima?
A: U koʻplab manbalardan (koʻpincha botnetdan) bir vaqtda amalga oshiriladi, shuning uchun bitta manzilni bloklash yetarli emas.
Q: Rate limiting nima?
A: Bitta manzildan maʼlum vaqt oynasida ruxsat etilgan soʻrovlar sonini cheklash.
Q: Nima uchun tarmoq himoyasi bir nechta qatlamda boʻlishi kerak?
A: Har bir qatlamning oʻz tahdidlari bor va bitta qatlamdagi himoya boshqasining oʻrnini bosmaydi.
:::

:::link https://www.cisa.gov/resources-tools/resources/understanding-and-responding-distributed-denial-service-attacks
CISA: Understanding and Responding to DDoS Attacks
DDoS ga tayyorgarlik va javob boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::
