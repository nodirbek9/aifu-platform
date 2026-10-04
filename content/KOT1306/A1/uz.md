---
title: Tarmoq qurilmalari va ularning vazifalari. Tarmoq adapterlari, takrorlagich, koʻprik, konsentrator
summary: Tarmoq adapteri va uning parametrlari; takrorlagich va konsentrator (1-pogʻona), koʻprik va kommutator (2-pogʻona), marshrutizator (3-pogʻona); kommutatorning MAC manzillarni oʻrganishi, kadrni uzatish, filtrlash va tarqatish; kolliziya va broadkast domenlari; adapterni operatsion tizim buyruqlari bilan koʻrish.
---

## Nima uchun bu mavzu muhim?

Ofisdagi tarmoq sekin ishlayapti: barcha kompyuterlar eski konsentratorga ulangan va bir-birining kadrlari bilan toʻqnashmoqda. Uni kommutatorga almashtirish bir zumda vaziyatni oʻzgartiradi — nima uchun? Bu amaliy mashgʻulotda tarmoq qurilmalari kadr bilan nima qilishini qadamma-qadam koʻramiz, kommutatorning "oʻrganish" jarayonini dastur bilan modellashtiramiz va oʻz kompyuteringiz adapterini tekshiramiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- tarmoq adapterining asosiy parametrlarini (MAC manzil, tezlik, dupleks) operatsion tizim buyruqlari bilan aniqlay olasiz;
- takrorlagich, konsentrator, koʻprik, kommutator va marshrutizator kadr yoki paket bilan nima qilishini tushuntira olasiz;
- kommutatorning MAC jadvalini qanday toʻldirishini va kadrni qachon uzatishi, filtrlashi yoki tarqatishini ayta olasiz;
- tarmoqdagi kolliziya va broadkast domenlari sonini hisoblay olasiz.
:::

## Tarmoq adapteri (NIC)

**Tarmoq adapteri** (Network Interface Card) — kompyuterni tarmoq muhitiga ulovchi qurilma. U 1 va 2-pogʻonalarda ishlaydi: bitlarni signalga aylantiradi va kadrlarni yigʻadi.

| Parametr | Maʼnosi | Misol |
|---|---|---|
| **MAC manzil** | ishlab chiqaruvchi tomonidan yozilgan 48 bitli noyob manzil | `3C-52-82-1A-9F-07` |
| **Tezlik** | qoʻllab-quvvatlanadigan tezliklar | 10/100/1000 Mbit/s |
| **Dupleks** | yarim yoki toʻliq dupleks | Full duplex |
| **Ulanish turi** | kompyuter ichida yoki tashqarisida | PCIe, USB, platada oʻrnatilgan |
| **Muhit** | qaysi kabel yoki radio | RJ-45 (oʻralgan juftlik), SFP (optika), Wi-Fi |

Ikki tomon tezlik va dupleksni avtomatik kelishib oladi (**autonegotiation**). Agar bir tomon qoʻlda "100 Mbit/s full", ikkinchisi "auto" qilib sozlansa, **dupleks nomuvofiqligi** yuzaga kelishi va tarmoq juda sekinlashishi mumkin.

## Qurilmalar kadr bilan nima qiladi

| Qurilma | Pogʻona | Kirgan signal yoki kadr bilan nima qiladi |
|---|---|---|
| **Takrorlagich** (repeater) | 1 | signalni tiklab, kuchaytirib, ikkinchi portga chiqaradi; kadr mazmunini bilmaydi |
| **Konsentrator** (hub) | 1 | koʻp portli takrorlagich: signalni **barcha boshqa** portlarga chiqaradi |
| **Koʻprik** (bridge) | 2 | ikki segment orasida turadi; MAC manzil boʻyicha kadrni oʻtkazadi yoki toʻsadi |
| **Kommutator** (switch) | 2 | koʻp portli koʻprik: kadrni faqat **qabul qiluvchi ulangan** portga yuboradi |
| **Marshrutizator** (router) | 3 | IP manzil boʻyicha paketni boshqa tarmoqqa yoʻnaltiradi; broadkastni oʻtkazmaydi |

Takrorlagich va konsentratorli eski Ethernet tarmoqlarida **5-4-3 qoidasi** amal qilgan: ikki kompyuter orasida eng koʻpi bilan 5 segment, 4 takrorlagich, ulardan faqat 3 tasida kompyuterlar boʻlishi mumkin — aks holda kolliziya oʻz vaqtida aniqlanmay qolar edi.

## Kommutator qanday ishlaydi

Kommutator har bir port orqasida qaysi MAC manzillar turganini **MAC jadvalida** (CAM jadvali) saqlaydi. Har bir kadr kelganda u ikki ish qiladi:

1. **Oʻrganish** (learning): kadrning **yuboruvchi** manzilini va u kelgan portni jadvalga yozadi (yoki yangilaydi).
2. **Qaror** — **qabul qiluvchi** manzil boʻyicha:
   - manzil jadvalda va boshqa portda → **uzatish** (forwarding) faqat shu portga;
   - manzil jadvalda va kadr kelgan portning oʻzida → **filtrlash** (filtering): kadr tashlab yuboriladi;
   - manzil jadvalda yoʻq yoki broadkast (`FF-FF-FF-FF-FF-FF`) → **tarqatish** (flooding): kelgan portdan tashqari barcha portlarga.

Jadvaldagi yozuv odatda 300 soniya ishlatilmasa oʻchiriladi (**aging**). Kadrni uzatish usullari: **store-and-forward** (kadr toʻliq qabul qilinib, FCS tekshiriladi), **cut-through** (qabul qiluvchi manzil oʻqilishi bilan uzatish boshlanadi) va **fragment-free** (birinchi 64 bayt qabul qilingach).

## Kolliziya va broadkast domenlari

- **Kolliziya domeni** — bir vaqtda uzatgan ikki qurilmaning signallari toʻqnashishi mumkin boʻlgan segment. Konsentrator barcha portlarini **bitta** kolliziya domeniga birlashtiradi; kommutator va marshrutizatorning **har bir porti** alohida kolliziya domeni.
- **Broadkast domeni** — bitta broadkast kadri yetib boradigan qurilmalar toʻplami. Konsentrator ham, kommutator ham broadkastni oʻtkazadi; uni faqat **marshrutizator** (yoki VLAN) toʻsadi. Shuning uchun marshrutizatorning har bir interfeysi — alohida broadkast domeni.

## Amaliy ish: oʻz adapteringizni tekshiring

Windows da buyruqlar satrini (`cmd`) oching:

```text
ipconfig /all          adapterlar, MAC (Physical Address), IP, shlyuz, DNS
getmac /v              barcha adapterlarning MAC manzillari
arp -a                 kompyuter "tanigan" qoʻshni qurilmalarning MAC manzillari
```

Linux da: `ip link` (MAC va holat), `ip addr` (IP manzillar), `ethtool eth0` (tezlik va dupleks), `ip neigh` (ARP jadvali).

Natijada quyidagilarni yozib chiqing: simli va simsiz adapterlar nomlari, ularning MAC manzillari, ulanish tezligi, standart shlyuzning IP va MAC manzili (`arp -a` dan).

## Ishlangan misol 1: kommutatorning MAC jadvali

Birinchi satrda kommutator portlari soni *P* va kadrlar soni *k*. Keyingi *k* satrda kadr kelgan port, yuboruvchi va qabul qiluvchi manzil (`FF` — broadkast). Har bir kadr uchun kommutator qarorini chiqaring: `uzatish <port>`, `filtr` yoki `tarqatish <portlar>`; oxirida MAC jadvalini manzillar tartibida chiqaring.

```python
p, k = map(int, input().split())
jadval = {}                                     # MAC -> port
for i in range(1, k + 1):
    port, manba, manzil = input().split()
    port = int(port)
    jadval[manba] = port                        # oʻrganish
    if manzil != "FF" and manzil in jadval:
        if jadval[manzil] == port:
            print(str(i) + ": filtr")
        else:
            print(str(i) + ": uzatish " + str(jadval[manzil]))
    else:
        boshqalar = [str(x) for x in range(1, p + 1) if x != port]
        print(str(i) + ": tarqatish " + " ".join(boshqalar))
print("jadval:")
for mac in sorted(jadval):
    print(mac, jadval[mac])
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int p = in.nextInt();
        int k = in.nextInt();
        Map<String, Integer> jadval = new TreeMap<>();    // MAC -> port
        for (int i = 1; i <= k; i++) {
            int port = in.nextInt();
            String manba = in.next();
            String manzil = in.next();
            jadval.put(manba, port);                      // oʻrganish
            if (!manzil.equals("FF") && jadval.containsKey(manzil)) {
                if (jadval.get(manzil) == port) {
                    System.out.println(i + ": filtr");
                } else {
                    System.out.println(i + ": uzatish " + jadval.get(manzil));
                }
            } else {
                StringBuilder boshqalar = new StringBuilder();
                for (int x = 1; x <= p; x++) {
                    if (x != port) boshqalar.append(boshqalar.length() > 0 ? " " : "").append(x);
                }
                System.out.println(i + ": tarqatish " + boshqalar);
            }
        }
        System.out.println("jadval:");
        for (Map.Entry<String, Integer> e : jadval.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    int p, k;
    cin >> p >> k;
    map<string, int> jadval;                              // MAC -> port
    for (int i = 1; i <= k; i++) {
        int port;
        string manba, manzil;
        cin >> port >> manba >> manzil;
        jadval[manba] = port;                             // oʻrganish
        if (manzil != "FF" && jadval.count(manzil)) {
            if (jadval[manzil] == port) {
                cout << i << ": filtr" << endl;
            } else {
                cout << i << ": uzatish " << jadval[manzil] << endl;
            }
        } else {
            string boshqalar;
            for (int x = 1; x <= p; x++) {
                if (x != port) boshqalar += (boshqalar.empty() ? "" : " ") + to_string(x);
            }
            cout << i << ": tarqatish " << boshqalar << endl;
        }
    }
    cout << "jadval:" << endl;
    for (auto& e : jadval) {
        cout << e.first << " " << e.second << endl;
    }
    return 0;
}
```

Kirish:

```text
4 7
1 AA BB
2 BB AA
3 CC FF
1 AA CC
4 DD BB
1 EE AA
3 BB CC
```

Chiqish:

```text
1: tarqatish 2 3 4
2: uzatish 1
3: tarqatish 1 2 4
4: uzatish 3
5: uzatish 2
6: filtr
7: filtr
jadval:
AA 1
BB 3
CC 3
DD 4
EE 1
```

Tahlil: 1-kadrda `BB` hali notanish — tarqatildi. 3-kadr broadkast — doim tarqatiladi. 6-kadrda `EE` va `AA` ikkalasi 1-portda (u yerda konsentrator turibdi), kadr kommutatordan oʻtmaydi — filtrlanadi. 7-kadrda `BB` noutbuki 3-portga koʻchirilgan: jadval yangilandi.

## Ishlangan misol 2: domenlarni sanash

Marshrutizatorning *r* ta interfeysi bor. Har bir interfeysga `hub h` (*h* ta kompyuterli konsentrator), `switch s` (*s* ta kompyuterli kommutator) yoki `host` (bitta kompyuter toʻgʻridan-toʻgʻri) ulangan. Kolliziya va broadkast domenlari sonini chiqaring.

Hisob: konsentrator segmenti (marshrutizatorga ulanish bilan birga) — 1 kolliziya domeni; kommutator segmenti — *s* + 1 (har bir kompyuter porti va marshrutizatorga ulangan port); toʻgʻridan-toʻgʻri ulangan kompyuter — 1. Broadkast domenlari — interfeyslar soni *r*.

```python
r = int(input())
kolliziya = 0
for _ in range(r):
    qism = input().split()
    if qism[0] == "hub":
        kolliziya += 1                   # butun konsentrator — bitta domen
    elif qism[0] == "switch":
        kolliziya += int(qism[1]) + 1    # har bir port + marshrutizatorga ulanish
    else:
        kolliziya += 1                   # toʻgʻridan-toʻgʻri ulangan kompyuter
print("kolliziya domenlari:", kolliziya)
print("broadkast domenlari:", r)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int r = Integer.parseInt(in.nextLine().trim());
        int kolliziya = 0;
        for (int i = 0; i < r; i++) {
            String[] qism = in.nextLine().trim().split(" ");
            if (qism[0].equals("hub")) {
                kolliziya += 1;                           // butun konsentrator — bitta domen
            } else if (qism[0].equals("switch")) {
                kolliziya += Integer.parseInt(qism[1]) + 1;   // har bir port + marshrutizatorga ulanish
            } else {
                kolliziya += 1;                           // toʻgʻridan-toʻgʻri ulangan kompyuter
            }
        }
        System.out.println("kolliziya domenlari: " + kolliziya);
        System.out.println("broadkast domenlari: " + r);
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    int r = stoi(satr);
    int kolliziya = 0;
    for (int i = 0; i < r; i++) {
        getline(cin, satr);
        istringstream qism(satr);
        string tur;
        qism >> tur;
        if (tur == "hub") {
            kolliziya += 1;                               // butun konsentrator — bitta domen
        } else if (tur == "switch") {
            int s;
            qism >> s;
            kolliziya += s + 1;                           // har bir port + marshrutizatorga ulanish
        } else {
            kolliziya += 1;                               // toʻgʻridan-toʻgʻri ulangan kompyuter
        }
    }
    cout << "kolliziya domenlari: " << kolliziya << endl;
    cout << "broadkast domenlari: " << r << endl;
    return 0;
}
```

Kirish:

```text
3
hub 5
switch 8
host
```

Chiqish:

```text
kolliziya domenlari: 11
broadkast domenlari: 3
```

Konsentratorni 5 portli kommutatorga almashtirsak, birinchi segment 1 emas, 6 ta kolliziya domeni boʻladi — har bir kompyuter oʻz "yoʻlagiga" ega va kolliziyalar yoʻqoladi. Broadkast domenlari soni esa oʻzgarmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Konsentrator kadrni faqat qabul qiluvchiga yuboradi deb oʻylash.** Konsentrator manzilni oʻqimaydi — signal barcha portlarga chiqadi.
- **Kommutator broadkastni toʻsadi deb hisoblash.** Kommutator broadkastni barcha portlarga tarqatadi; broadkast domenini faqat marshrutizator yoki VLAN ajratadi.
- **Kommutator qabul qiluvchi manzilni oʻrganadi deb oʻylash.** U faqat **yuboruvchi** manzilni va kirish portini yozadi.
- **MAC manzil va IP manzilni chalkashtirish.** MAC — adapterga bogʻlangan fizik manzil (2-pogʻona), IP — tarmoqdagi mantiqiy manzil (3-pogʻona).
:::

:::tip
Cisco Packet Tracer da 4 ta kompyuterni avval konsentratorga, keyin kommutatorga ulang va simulyatsiya rejimida bitta `ping` ni kuzating: konsentrator kadrni hamma kompyuterga yuboradi, kommutator esa birinchi kadrdan keyin faqat kerakli portga. Kommutatorning MAC jadvalini `show mac-address-table` buyrugʻi bilan koʻring.
:::

## Xulosa

- Tarmoq adapteri kompyuterni muhitga ulaydi, uning MAC manzili, tezligi va dupleks rejimi bor; `ipconfig /all`, `getmac`, `ip link` bilan koʻriladi.
- Takrorlagich va konsentrator — 1-pogʻona (signalni hamma joyga), koʻprik va kommutator — 2-pogʻona (MAC boʻyicha), marshrutizator — 3-pogʻona (IP boʻyicha).
- Kommutator yuboruvchi manzilni oʻrganadi, qabul qiluvchi boʻyicha uzatadi, filtrlaydi yoki tarqatadi.
- Kommutatorning har bir porti — alohida kolliziya domeni; broadkast domenlarini marshrutizator ajratadi.

## Oʻzingizni tekshiring

:::check
Q: Kommutator kadr kelganda MAC jadvaliga qaysi manzilni yozadi?
A: Kadrning yuboruvchi (manba) MAC manzilini va kadr kelgan portni.
Q: Qabul qiluvchi manzil jadvalda boʻlmasa, kommutator nima qiladi?
A: Kadrni kelgan portdan tashqari barcha portlarga tarqatadi (flooding).
Q: 24 portli konsentratorga 24 ta kompyuter ulangan. Nechta kolliziya domeni bor?
A: Bitta — konsentrator barcha portlarni bitta kolliziya domeniga birlashtiradi.
Q: Marshrutizatorning 4 ta interfeysiga 4 ta kommutator ulangan. Nechta broadkast domeni bor?
A: 4 ta — har bir marshrutizator interfeysi alohida broadkast domeni.
Q: Store-and-forward va cut-through usullari farqi nimada?
A: Store-and-forward kadrni toʻliq qabul qilib, FCS ni tekshiradi; cut-through qabul qiluvchi manzilni oʻqishi bilan uzatishni boshlaydi — tezroq, lekin buzilgan kadrlarni ham oʻtkazadi.
:::

:::link https://en.wikipedia.org/wiki/Network_switch
Tarmoq kommutatori
Kommutator turlari, MAC jadvali, uzatish usullari va domenlar haqida maqola (ingliz tilida).
:::
