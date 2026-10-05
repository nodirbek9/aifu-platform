---
title: Mustaqil ish va uyga vazifalarni bajarish
summary: Mustaqil ish turlari — referat, Packet Tracer loyihasi, laboratoriya hisoboti, dasturiy vazifa; mavzular roʻyxati; tarmoq loyihasini bajarish bosqichlari va hujjatlari (sxema, manzil rejasi, qurilmalar roʻyxati, sinov natijalari); baholash mezonlari; namunaviy vazifa — tarmoq inventarizatsiyasida IP va MAC ziddiyatlarini topish.
---

## Nima uchun bu mavzu muhim?

Tarmoq muhandisining ishi — faqat kabel ulash emas: loyiha tuzish, manzillarni rejalash, natijani sinash va **hujjatlashtirish**. Yaxshi hujjatlashtirilgan tarmoqni boshqa muhandis ham tez tushunadi; hujjatsiz tarmoqda har bir nosozlik "detektiv tergov"ga aylanadi. Mustaqil ishlar aynan shu koʻnikmalarni rivojlantiradi va yakuniy bahoning muhim qismini tashkil etadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- mustaqil ish turini tanlab, uni talablarga muvofiq bajara olasiz;
- kichik tarmoq loyihasini sxema, manzil rejasi va sinovlar bilan tayyorlay olasiz;
- baholash mezonlarini bilib, ishingizni topshirishdan oldin oʻzingiz tekshira olasiz;
- tarmoq inventarizatsiyasidagi xatolarni dastur yordamida aniqlay olasiz.
:::

## Mustaqil ish turlari

| Tur | Hajm | Natija |
|---|---|---|
| **Referat** | 8–12 bet | mavzu boʻyicha tahliliy matn, kamida 5 manba, xulosa |
| **Packet Tracer loyihasi** | 1 ta `.pkt` fayl + 3–5 bet hisobot | ishlaydigan tarmoq va hujjatlari |
| **Laboratoriya hisoboti** | 2–4 bet | amaliy mashgʻulot qadamlari, skrinshotlar, xulosa |
| **Dasturiy vazifa** | Python, Java yoki C++ dagi dastur | platformadagi avtomatik testlardan oʻtgan yechim |

### Referat mavzulari (namuna)

1. IPv6 ga oʻtish: sabablari, mexanizmlari (dual stack, tunnellar) va Oʻzbekistondagi holat.
2. 5G va Wi-Fi 7 texnologiyalarini solishtirish.
3. SDN (dasturiy belgilanadigan tarmoqlar) va OpenFlow.
4. DDoS hujumlari va ularga qarshi himoya usullari.
5. Maʼlumotlar markazlari tarmoq arxitekturasi (spine-leaf).
6. VPN texnologiyalari: IPsec, WireGuard va OpenVPN solishtirmasi.
7. Buyumlar interneti (IoT) tarmoq protokollari: MQTT, CoAP, Zigbee.
8. DNS xavfsizligi: DNSSEC, DoH, DoT.

## Packet Tracer loyihasi

**Vazifa namunasi**: uch qavatli fakultet binosi — har qavatda kompyuter sinfi (24 PC), oʻqituvchilar xonasi (8 PC), umumiy Wi-Fi; serverlar xonasi (DHCP, DNS, veb-server); Internetga chiqish.

Bajarish bosqichlari:

1. **Talablarni tahlil qilish** — qurilmalar soni, xizmatlar, xavfsizlik talablari.
2. **Mantiqiy sxema** — topologiya (yulduz yoki daraxt), qurilmalar va ulanishlar.
3. **Manzil rejasi** — VLSM bilan quyi tarmoqlar, shlyuzlar, serverlarning statik manzillari.
4. **Sozlash** — routerlar, kommutatorlar, DHCP, DNS, statik marshrutlar.
5. **Sinov** — har bir quyi tarmoqdan server va Internetga `ping`, `tracert`, veb-sahifani ochish.
6. **Hujjatlashtirish** — quyidagi shablon boʻyicha hisobot.

**Hisobot shabloni**:

```text
1. Vazifa va talablar
2. Tarmoq sxemasi (rasm)
3. Manzil rejasi: quyi tarmoq | prefiks | shlyuz | DHCP diapazoni | vazifasi
4. Qurilmalar roʻyxati: nomi | modeli | interfeyslar va manzillar
5. Asosiy sozlamalar (router va kommutator buyruqlari)
6. Sinov natijalari: tekshiruv | kutilgan natija | haqiqiy natija
7. Xulosa va takomillashtirish takliflari
```

## Baholash mezonlari

| Mezon | Ulushi |
|---|---|
| Vazifa talablarining bajarilishi (tarmoq ishlaydi, xizmatlar sozlangan) | 40% |
| Manzil rejasining toʻgʻriligi va tejamkorligi | 20% |
| Sinovlar va ularning natijalari | 15% |
| Hujjatlashtirish sifati (sxema, jadvallar, tushunarli til) | 15% |
| Mustaqillik va himoya (savollarga javob) | 10% |

Belgilangan muddatdan kech topshirilgan ish qabul qilinadi, lekin "kechikkan" deb belgilanadi. Boshqa talabaning ishini koʻchirish yoki sunʼiy intellekt yozgan matnni oʻzgartirmasdan topshirish — akademik halollik qoidasini buzish.

## Namunaviy vazifa: inventarizatsiyada ziddiyatlarni topish

Tarmoq hujjatlarida har bir qurilma uchun nom, IP va MAC manzil yoziladi. Xato bilan ikki qurilmaga bir xil IP berilsa — **IP ziddiyati**; bir xil MAC — nusxalangan virtual mashina yoki hujjatdagi xato. Dastur birinchi satrda qurilmalar sonini, keyingi satrlarda "nom IP MAC" ni oladi va har bir takrorlangan IP va MAC uchun ularni ishlatayotgan qurilmalarni chiqaradi (manzillar va nomlar alifbo tartibida).

```python
n = int(input())
ip_lar = {}
mac_lar = {}
for _ in range(n):
    nom, ip, mac = input().split()
    ip_lar.setdefault(ip, []).append(nom)
    mac_lar.setdefault(mac.upper(), []).append(nom)   # MAC katta-kichik harfga sezgir emas
topildi = False
for sarlavha, jadval in (("IP ziddiyati", ip_lar), ("MAC takrori", mac_lar)):
    for manzil in sorted(jadval):
        if len(jadval[manzil]) > 1:
            topildi = True
            print(sarlavha + ": " + manzil + " -> " + ", ".join(sorted(jadval[manzil])))
if not topildi:
    print("ziddiyat yoʻq")
```
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static boolean chiqar(String sarlavha, Map<String, List<String>> jadval) {
        boolean topildi = false;
        for (Map.Entry<String, List<String>> e : jadval.entrySet()) {
            if (e.getValue().size() > 1) {
                topildi = true;
                List<String> nomlar = new ArrayList<>(e.getValue());
                Collections.sort(nomlar);
                System.out.println(sarlavha + ": " + e.getKey() + " -> " + String.join(", ", nomlar));
            }
        }
        return topildi;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Map<String, List<String>> ipLar = new TreeMap<>();
        Map<String, List<String>> macLar = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            String nom = in.next(), ip = in.next(), mac = in.next();
            ipLar.computeIfAbsent(ip, k -> new ArrayList<>()).add(nom);
            macLar.computeIfAbsent(mac.toUpperCase(), k -> new ArrayList<>()).add(nom);   // MAC katta-kichik harfga sezgir emas
        }
        boolean birinchi = chiqar("IP ziddiyati", ipLar);
        boolean ikkinchi = chiqar("MAC takrori", macLar);
        if (!birinchi && !ikkinchi) System.out.println("ziddiyat yoʻq");
    }
}
```
```cpp
#include <algorithm>
#include <cctype>
#include <iostream>
#include <map>
#include <string>
#include <vector>
using namespace std;

bool chiqar(const string& sarlavha, map<string, vector<string>>& jadval) {
    bool topildi = false;
    for (auto& e : jadval) {
        if (e.second.size() > 1) {
            topildi = true;
            sort(e.second.begin(), e.second.end());
            string nomlar;
            for (size_t i = 0; i < e.second.size(); i++) nomlar += (i ? ", " : "") + e.second[i];
            cout << sarlavha << ": " << e.first << " -> " << nomlar << endl;
        }
    }
    return topildi;
}

int main() {
    int n;
    cin >> n;
    map<string, vector<string>> ipLar, macLar;
    for (int i = 0; i < n; i++) {
        string nom, ip, mac;
        cin >> nom >> ip >> mac;
        ipLar[ip].push_back(nom);
        for (auto& c : mac) c = toupper(c);                          // MAC katta-kichik harfga sezgir emas
        macLar[mac].push_back(nom);
    }
    bool birinchi = chiqar("IP ziddiyati", ipLar);
    bool ikkinchi = chiqar("MAC takrori", macLar);
    if (!birinchi && !ikkinchi) cout << "ziddiyat yoʻq" << endl;
    return 0;
}
```

Kirish:

```text
6
PC01 192.168.50.101 00:1A:2B:00:00:01
PC02 192.168.50.102 00:1a:2b:00:00:02
PRN1 192.168.50.150 00:1A:2B:00:00:50
PC03 192.168.50.102 00:1A:2B:00:00:03
VM-A 192.168.50.120 08:00:27:AA:BB:CC
VM-B 192.168.50.121 08:00:27:aa:bb:cc
```

Chiqish:

```text
IP ziddiyati: 192.168.50.102 -> PC02, PC03
MAC takrori: 08:00:27:AA:BB:CC -> VM-A, VM-B
```

PC02 va PC03 bir xil manzilga ega — ulardan biri tarmoqda "Windows has detected an IP address conflict" xabarini oladi. VM-A va VM-B — bitta virtual mashinadan nusxalangan va MAC qayta yaratilmagan; ular bir segmentda boʻlsa, kommutator MAC jadvali chalkashadi. Bunday tekshiruvni loyiha hujjatlarini topshirishdan oldin bajaring.

## Topshiriq

Packet Tracer loyihasini yuqoridagi namuna yoki oʻqituvchi bergan variant boʻyicha bajaring va hisobotni shablon asosida tayyorlang. Qurilmalar roʻyxatini yuqoridagi dasturdan oʻtkazib, ziddiyatlar yoʻqligini tasdiqlang. Dasturiy vazifalar (KOT-A4, A7, A8, A9, A14) platformada avtomatik tekshiriladi — har birini muddatigacha topshiring.

:::warning
**Koʻp uchraydigan xatolar**

- **Sxemasiz va manzil rejasisiz sozlashni boshlash.** Keyin manzillar kesishadi va hammasini qayta qilishga toʻgʻri keladi.
- **Sinovlarni hisobotga yozmaslik.** "Ishlaydi" degan gap isbot emas — `ping` va `tracert` natijalari kerak.
- **Referatni bitta manbadan koʻchirish.** Kamida 5 ta manba, iqtiboslar va oʻz xulosangiz boʻlishi shart.
- **Topshirishni oxirgi kunga qoldirish.** Packet Tracer faylining versiyasi mos kelmasligi yoki saqlanmay qolishi mumkin — oldindan tekshiring.
:::

:::tip
Loyihani bosqichma-bosqich saqlang (`loyiha-v1.pkt`, `loyiha-v2.pkt`): biror sozlash hammasini buzsa, oldingi ishlaydigan versiyaga qaytish mumkin boʻladi. Bu — versiyalarni boshqarishning eng oddiy shakli.
:::

## Xulosa

- Mustaqil ish turlari: referat, Packet Tracer loyihasi, laboratoriya hisoboti, dasturiy vazifa.
- Loyiha bosqichlari: talablar → sxema → manzil rejasi → sozlash → sinov → hujjatlashtirish.
- Baho mezonlari: talablar bajarilishi, manzil rejasi, sinovlar, hujjatlar sifati, himoya.
- Inventarizatsiya hujjatlarini dastur bilan tekshirish IP va MAC ziddiyatlarini oldindan topadi.

## Oʻzingizni tekshiring

:::check
Q: Tarmoq loyihasi hisobotida qaysi boʻlimlar boʻlishi kerak?
A: Vazifa, sxema, manzil rejasi, qurilmalar roʻyxati, asosiy sozlamalar, sinov natijalari, xulosa.
Q: IP ziddiyati qanday yuzaga keladi va qanday namoyon boʻladi?
A: Ikki qurilmaga bir xil IP berilganda; ulardan biri ogohlantirish oladi va aloqa uzilib-uzilib ishlaydi.
Q: Nima uchun MAC manzillarni solishtirishda harflar kattaligi hisobga olinmaydi?
A: MAC — oʻn oltilik son; `aa` va `AA` bir xil qiymat, faqat yozuv shakli farq qiladi.
Q: Loyihada sinov natijalari qanday hujjatlashtiriladi?
A: Jadval bilan: tekshiruv, kutilgan natija va haqiqiy natija (masalan, `ping` va `tracert` chiqishi).
Q: Mustaqil ish bahosining eng katta qismi nimaga beriladi?
A: Vazifa talablarining bajarilishiga — tarmoq ishlashi va xizmatlar sozlanganiga.
:::

:::link https://www.netacad.com/cisco-packet-tracer
Cisco Packet Tracer
Loyihalarni bajarish uchun tarmoq simulyatori (ingliz tilida).
:::
