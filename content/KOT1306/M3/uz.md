---
title: ISO/OSI modeli. Ochiq tizimlarning oʻzaro muloqoti. Tarmoq pogʻonalari. Protokol, interfeys, standartlar tushunchalari
summary: Ochiq tizimlar va ularning oʻzaro muloqoti; ISO/OSI modelining yetti pogʻonasi, har birining vazifasi, maʼlumot birligi (PDU), protokol va qurilmalari; protokol va interfeys farqi; inkapsulyatsiya va dekapsulyatsiya; standartlashtirish tashkilotlari; OSI va TCP/IP modellarini solishtirish.
---

## Nima uchun bu mavzu muhim?

Brauzerda sahifa ochganingizda maʼlumot dastur ichidan chiqib, operatsion tizim, tarmoq adapteri, kabel, kommutator, marshrutizator orqali boshqa qitʼadagi serverga yetib boradi — va u yerda teskari yoʻlni bosib oʻtadi. Buni turli kompaniyalar ishlab chiqargan yuzlab qurilma va dasturlar bajaradi. Ular bir-birini qanday "tushunadi"? Javob — **pogʻonali model**: vazifa yetti qismga boʻlinadi, har bir qism uchun alohida standartlar yoziladi. ISO/OSI modeli tarmoq mutaxassislarining umumiy tili: "muammo ikkinchi pogʻonada" degan gap har bir muhandisga tushunarli.

:::note
**Bu darsdan soʻng siz:**

- ochiq tizim tushunchasini va pogʻonali yondashuvning afzalligini tushuntira olasiz;
- OSI modelining yetti pogʻonasini tartib bilan sanab, har birining vazifasini ayta olasiz;
- protokol va interfeysni farqlay olasiz;
- inkapsulyatsiya jarayonini bosqichma-bosqich tushuntirib, sarlavhalar hisobiga ketadigan sarfni hisoblay olasiz;
- asosiy standartlashtirish tashkilotlarini va OSI hamda TCP/IP modellari farqini bilasiz.
:::

## Ochiq tizimlar

**Ochiq tizim** — ochiq (hamma uchun eʼlon qilingan) standartlarga muvofiq qurilgan tizim. Uning interfeyslari va protokollari maxfiy emas, shuning uchun istalgan ishlab chiqaruvchi unga mos qurilma yoki dastur yarata oladi. Masalan, Ethernet standarti ochiq — shuning uchun turli firmalar adapterlari bitta kommutatorga ulanib, birga ishlaydi.

1984-yilda Xalqaro standartlashtirish tashkiloti (**ISO**) **ochiq tizimlarning oʻzaro muloqoti** (Open Systems Interconnection, **OSI**) uchun etalon modelni qabul qildi (ISO/IEC 7498). Model tarmoq orqali aloqani **yetti pogʻonaga** ajratadi. Model protokol emas — u qaysi vazifani qaysi pogʻona bajarishi kerakligini tavsiflovchi "xarita".

Pogʻonali yondashuvning afzalliklari:

- **Murakkablikni boʻlish** — har bir pogʻona faqat oʻz vazifasini hal qiladi.
- **Mustaqil almashtirish** — kabelni optikaga almashtirish yuqori pogʻonalardagi dasturlarni oʻzgartirishni talab qilmaydi.
- **Moslik** — har bir pogʻona standartlashtirilgani uchun turli ishlab chiqaruvchilar mahsulotlari birga ishlaydi.
- **Oʻqitish va diagnostika** — muammoni pogʻonama-pogʻona izlash mumkin.

## OSI modelining yetti pogʻonasi

| № | Pogʻona | Vazifasi | Maʼlumot birligi (PDU) | Protokol va texnologiyalar | Qurilmalar |
|---|---|---|---|---|---|
| 7 | **Amaliy** (Application) | foydalanuvchi dasturlariga tarmoq xizmatlarini beradi | xabar | HTTP, FTP, SMTP, DNS | shlyuz |
| 6 | **Taqdimot** (Presentation) | maʼlumot formatini oʻzgartirish, shifrlash, siqish | xabar | TLS/SSL, JPEG, ASCII, UTF-8 | — |
| 5 | **Seans** (Session) | aloqa seansini oʻrnatish, boshqarish va tugatish | xabar | NetBIOS, RPC | — |
| 4 | **Transport** | jarayonlar orasida ishonchli (yoki tez) uzatish, segmentlash, portlar | segment (TCP) / datagramma (UDP) | TCP, UDP | — |
| 3 | **Tarmoq** (Network) | mantiqiy manzillash va marshrutlash | paket | IP, ICMP, OSPF | marshrutizator |
| 2 | **Kanal** (Data Link) | bitta segment ichida kadrlarni uzatish, fizik (MAC) manzil, xatoni aniqlash | kadr (frame) | Ethernet, Wi-Fi (802.11), PPP | kommutator, koʻprik |
| 1 | **Fizik** (Physical) | bitlarni signal sifatida muhit orqali uzatish | bit | RS-232, 100BASE-TX, DSL | takrorlagich, konsentrator, kabel |

Pogʻonalarni eslab qolish uchun inglizcha ibora: **"Please Do Not Throw Sausage Pizza Away"** (1 dan 7 gacha: Physical, Data Link, Network, Transport, Session, Presentation, Application).

Pogʻonalar ikki guruhga boʻlinadi: **1–3** — tarmoqqa bogʻliq (maʼlumotni tugundan tugunga yetkazadi, marshrutizatorlarda ham ishlaydi), **4–7** — oxirgi tizimlarda (hostlarda) ishlaydi va dasturlar orasidagi aloqani taʼminlaydi.

## Protokol va interfeys

| Tushuncha | Kimlar orasida | Misol |
|---|---|---|
| **Protokol** | turli kompyuterlardagi **bir xil** pogʻonalar orasida (gorizontal) | ikki kompyuterning TCP modullari segmentlarni almashish qoidalari |
| **Interfeys** | bitta kompyuterdagi **qoʻshni** pogʻonalar orasida (vertikal) | dastur TCP ga socket orqali murojaat qiladi |
| **Xizmat** (service) | quyi pogʻona yuqori pogʻonaga beradigan imkoniyat | transport pogʻonasi amaliy pogʻonaga "ishonchli uzatish" xizmatini beradi |

**Protokol** uchta narsani belgilaydi: **sintaksis** (xabar formati — maydonlar va ularning uzunligi), **semantika** (har bir maydon maʼnosi va unga javob harakati) va **sinxronizatsiya** (harakatlar tartibi va vaqti). Bir kompyuterdagi barcha pogʻonalar protokollari toʻplami **protokollar steki** deyiladi.

Muhim nuqta: *N*-pogʻona boshqa kompyuterdagi *N*-pogʻona bilan **mantiqan** gaplashadi, lekin **haqiqatda** maʼlumot pastga tushib, fizik muhit orqali oʻtadi va narigi tomonda yuqoriga koʻtariladi.

## Inkapsulyatsiya va dekapsulyatsiya

Maʼlumot yuborilganda har bir pogʻona yuqoridan kelgan birlikni "konvertga soladi": oʻzining **sarlavhasini** (header) qoʻshadi, kanal pogʻonasi esa oxiriga **treyler** (masalan, FCS — nazorat yigʻindisi) ham qoʻshadi. Bu **inkapsulyatsiya**. Qabul qiluvchida jarayon teskari: har bir pogʻona oʻz sarlavhasini oʻqib, olib tashlaydi va qolganini yuqoriga uzatadi — **dekapsulyatsiya**.

```text
Amaliy      [            maʼlumot            ]
Transport   [TCP 20][        maʼlumot        ]               = segment
Tarmoq      [IP 20][TCP 20][     maʼlumot     ]              = paket
Kanal       [Eth 14][IP 20][TCP 20][ maʼlumot ][FCS 4]       = kadr
Fizik       0101100101...                                    = bitlar
```

Sarlavhalar foydali yuk emas — ular **sarf** (overhead). Ethernet + IPv4 + TCP uchun bitta kadrdagi sarf 14 + 20 + 20 + 4 = 58 bayt (qoʻshimcha maydonlarsiz). Ethernet kadri maʼlumot qismi (MTU) 1500 bayt, shundan IP va TCP sarlavhalari chiqarilsa, bitta segmentga eng koʻpi bilan 1460 bayt foydali maʼlumot sigʻadi (MSS — Maximum Segment Size).

## Standartlashtirish tashkilotlari

| Tashkilot | Nimani standartlashtiradi |
|---|---|
| **ISO** (International Organization for Standardization) | OSI modeli, koʻplab xalqaro standartlar |
| **IEEE** (Institute of Electrical and Electronics Engineers) | 802 oilasi: 802.3 Ethernet, 802.11 Wi-Fi, 802.1Q VLAN |
| **IETF** (Internet Engineering Task Force) | Internet protokollari — **RFC** hujjatlari (RFC 791 — IP, RFC 9293 — TCP) |
| **ITU-T** (Xalqaro elektraloqa ittifoqi) | telekommunikatsiya: X.25, V.92 modemlar, G.992 ADSL |
| **ANSI / TIA / EIA** | kabel tizimlari: TIA-568 (oʻralgan juftlik ulanish sxemalari) |
| **W3C** | veb standartlari: HTML, CSS |

Standartlar ikki xil boʻladi: **de-yure** (rasmiy tashkilot tomonidan qabul qilingan, masalan, IEEE 802.3) va **de-fakto** (keng tarqalgani uchun amalda standartga aylangan, masalan, dastlabki TCP/IP).

## OSI va TCP/IP

Amalda Internet **TCP/IP** modelida ishlaydi. U OSI dan oldin paydo boʻlgan va soddaroq — toʻrt pogʻonali:

| TCP/IP pogʻonasi | OSI dagi mosligi | Protokollar |
|---|---|---|
| Amaliy | 7, 6, 5 | HTTP, DNS, SMTP, TLS |
| Transport | 4 | TCP, UDP |
| Internet | 3 | IP, ICMP, ARP* |
| Tarmoq kirishi (Link) | 2, 1 | Ethernet, Wi-Fi |

\*ARP odatda 2 va 3 pogʻonalar oraligʻida turadi deb hisoblanadi.

OSI modeli tushuntirish va diagnostika uchun til sifatida qoʻllanadi, TCP/IP esa haqiqiy protokollar toʻplami.

## Ishlangan misol 1: inkapsulyatsiya sarfi

Birinchi satrda *k*, keyingi *k* satrda yuborilayotgan maʼlumot hajmi *S* (bayt, *S* ≥ 1). Maʼlumot MSS = 1460 baytlik segmentlarga boʻlinadi, har bir kadrga 58 bayt sarf qoʻshiladi. Har bir *S* uchun kadrlar soni, kabeldan oʻtgan jami baytlar va samaradorlikni (foydali maʼlumot ulushi, %, ikki xona aniqlikda) chiqaring.

```python
MSS = 1460
SARF = 14 + 20 + 20 + 4          # Ethernet + IP + TCP + FCS

k = int(input())
for _ in range(k):
    s = int(input())
    kadrlar = (s + MSS - 1) // MSS   # yuqoriga yaxlitlangan boʻlinma
    jami = s + kadrlar * SARF
    print(s, kadrlar, jami, f"{s * 100 / jami:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    static final long MSS = 1460;
    static final long SARF = 14 + 20 + 20 + 4;   // Ethernet + IP + TCP + FCS

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        for (int i = 0; i < k; i++) {
            long s = in.nextLong();
            long kadrlar = (s + MSS - 1) / MSS;  // yuqoriga yaxlitlangan boʻlinma
            long jami = s + kadrlar * SARF;
            System.out.println(s + " " + kadrlar + " " + jami + " " + String.format("%.2f", s * 100.0 / jami));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
using namespace std;

const long long MSS = 1460;
const long long SARF = 14 + 20 + 20 + 4;   // Ethernet + IP + TCP + FCS

int main() {
    int k;
    cin >> k;
    for (int i = 0; i < k; i++) {
        long long s;
        cin >> s;
        long long kadrlar = (s + MSS - 1) / MSS;   // yuqoriga yaxlitlangan boʻlinma
        long long jami = s + kadrlar * SARF;
        printf("%lld %lld %lld %.2f\n", s, kadrlar, jami, s * 100.0 / jami);
    }
    return 0;
}
```

Kirish:

```text
4
1460
100
1000000
1
```

Chiqish:

```text
1460 1 1518 96.18
100 1 158 63.29
1000000 685 1039730 96.18
1 1 59 1.69
```

Xulosa: katta fayllarda sarf atigi ~4%, lekin kichik xabarlarda (masalan, bitta tugma bosilishi haqidagi 1 baytlik maʼlumot) kabeldan oʻtgan baytlarning 98% dan ortigʻi sarlavhalardir.

## Ishlangan misol 2: pogʻonalar boʻylab yoʻl

Bir satrli xabar berilgan. Yuboruvchi tomonda 7-pogʻonadan 2-pogʻonagacha har bir pogʻona `H<raqam>|` sarlavhasini boshiga qoʻshadi, 2-pogʻona esa oxiriga `|T2` treylerini ham qoʻshadi; 1-pogʻona natijani bitlar soni sifatida uzatadi. Qabul qiluvchi tomonda sarlavhalar teskari tartibda olib tashlanadi. Har bir qadamni chiqaring (`->` — yuborish, `<-` — qabul).

```python
pogonalar = [
    (7, "Amaliy"), (6, "Taqdimot"), (5, "Seans"),
    (4, "Transport"), (3, "Tarmoq"), (2, "Kanal"),
]

xabar = input()
birlik = xabar
for raqam, nom in pogonalar:                 # yuqoridan pastga
    birlik = "H" + str(raqam) + "|" + birlik
    if raqam == 2:
        birlik = birlik + "|T2"              # kanal pogʻonasi treyler ham qoʻshadi
    print("->", raqam, nom + ":", birlik)
print("->", 1, "Fizik:", len(birlik) * 8, "bit")

birlik = birlik[:len(birlik) - 3]            # avval treyler olib tashlanadi
for raqam, nom in reversed(pogonalar):       # pastdan yuqoriga
    birlik = birlik[len("H" + str(raqam) + "|"):]
    print("<-", raqam, nom + ":", birlik)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int[] raqamlar = {7, 6, 5, 4, 3, 2};
        String[] nomlar = {"Amaliy", "Taqdimot", "Seans", "Transport", "Tarmoq", "Kanal"};
        Scanner in = new Scanner(System.in);
        String xabar = in.nextLine();
        String birlik = xabar;
        for (int i = 0; i < raqamlar.length; i++) {        // yuqoridan pastga
            birlik = "H" + raqamlar[i] + "|" + birlik;
            if (raqamlar[i] == 2) {
                birlik = birlik + "|T2";                    // kanal pogʻonasi treyler ham qoʻshadi
            }
            System.out.println("-> " + raqamlar[i] + " " + nomlar[i] + ": " + birlik);
        }
        System.out.println("-> " + 1 + " Fizik: " + birlik.length() * 8 + " bit");

        birlik = birlik.substring(0, birlik.length() - 3);  // avval treyler olib tashlanadi
        for (int i = raqamlar.length - 1; i >= 0; i--) {   // pastdan yuqoriga
            birlik = birlik.substring(("H" + raqamlar[i] + "|").length());
            System.out.println("<- " + raqamlar[i] + " " + nomlar[i] + ": " + birlik);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

int main() {
    int raqamlar[] = {7, 6, 5, 4, 3, 2};
    string nomlar[] = {"Amaliy", "Taqdimot", "Seans", "Transport", "Tarmoq", "Kanal"};
    string xabar;
    getline(cin, xabar);
    string birlik = xabar;
    for (int i = 0; i < 6; i++) {                          // yuqoridan pastga
        birlik = "H" + to_string(raqamlar[i]) + "|" + birlik;
        if (raqamlar[i] == 2) {
            birlik = birlik + "|T2";                        // kanal pogʻonasi treyler ham qoʻshadi
        }
        cout << "-> " << raqamlar[i] << " " << nomlar[i] << ": " << birlik << endl;
    }
    cout << "-> " << 1 << " Fizik: " << birlik.size() * 8 << " bit" << endl;

    birlik = birlik.substr(0, birlik.size() - 3);          // avval treyler olib tashlanadi
    for (int i = 5; i >= 0; i--) {                         // pastdan yuqoriga
        birlik = birlik.substr(("H" + to_string(raqamlar[i]) + "|").size());
        cout << "<- " << raqamlar[i] << " " << nomlar[i] << ": " << birlik << endl;
    }
    return 0;
}
```

Kirish:

```text
Salom
```

Chiqish:

```text
-> 7 Amaliy: H7|Salom
-> 6 Taqdimot: H6|H7|Salom
-> 5 Seans: H5|H6|H7|Salom
-> 4 Transport: H4|H5|H6|H7|Salom
-> 3 Tarmoq: H3|H4|H5|H6|H7|Salom
-> 2 Kanal: H2|H3|H4|H5|H6|H7|Salom|T2
-> 1 Fizik: 208 bit
<- 2 Kanal: H3|H4|H5|H6|H7|Salom
<- 3 Tarmoq: H4|H5|H6|H7|Salom
<- 4 Transport: H5|H6|H7|Salom
<- 5 Seans: H6|H7|Salom
<- 6 Taqdimot: H7|Salom
<- 7 Amaliy: Salom
```

Eʼtibor bering: eng koʻp sarlavhali birlik — kadr (2-pogʻona), qabul qiluvchida esa har bir pogʻona faqat **oʻzining** sarlavhasini olib tashlaydi — bu pogʻonalarning mustaqilligini koʻrsatadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Pogʻonalar tartibini teskari sanash.** 1-pogʻona — fizik (eng pastki), 7-pogʻona — amaliy (eng yuqori). Raqamlar pastdan yuqoriga oʻsadi.
- **"Amaliy pogʻona" ni foydalanuvchi dasturining oʻzi deb tushunish.** Brauzer — dastur; amaliy pogʻona esa u foydalanadigan protokol (HTTP).
- **Protokol va interfeysni chalkashtirish.** Protokol — turli kompyuterlardagi bir xil pogʻonalar orasidagi qoidalar, interfeys — bitta kompyuterdagi qoʻshni pogʻonalar orasidagi chegara.
- **Kommutatorni 3-pogʻona qurilmasi deb hisoblash.** Oddiy kommutator MAC manzil bilan ishlaydi — 2-pogʻona; IP bilan ishlaydigani marshrutizator (yoki maxsus "L3 kommutator").
- **Sarlavhalar sarfini eʼtiborsiz qoldirish.** 100 Mbit/s kanaldan hech qachon 100 Mbit/s foydali maʼlumot oʻtmaydi — bir qismini sarlavhalar egallaydi.
:::

:::tip
Wireshark dasturini oʻrnatib, istalgan veb-sahifani oching va bitta paketni tanlang: dastur uni pogʻonalar boʻyicha ochib koʻrsatadi — Frame, Ethernet II, Internet Protocol, Transmission Control Protocol, Hypertext Transfer Protocol. Har bir sarlavha necha bayt ekanini yozib chiqing.
:::

## Xulosa

- Ochiq tizim — ochiq standartlar asosida qurilgan, turli ishlab chiqaruvchilar mahsulotlari birga ishlay oladigan tizim; OSI — ISO ning 7 pogʻonali etalon modeli.
- Pogʻonalar: 1 fizik, 2 kanal, 3 tarmoq, 4 transport, 5 seans, 6 taqdimot, 7 amaliy; PDU: bit, kadr, paket, segment, xabar.
- Protokol — bir xil pogʻonalar orasidagi (gorizontal) qoidalar, interfeys — qoʻshni pogʻonalar orasidagi (vertikal) chegara.
- Inkapsulyatsiyada har bir pogʻona sarlavha qoʻshadi, dekapsulyatsiyada olib tashlaydi; sarlavhalar sarfi kichik xabarlarda juda katta.
- Standartlar: ISO, IEEE (802), IETF (RFC), ITU-T, TIA; amalda TCP/IP (4 pogʻona) ishlatiladi.

## Oʻzingizni tekshiring

:::check
Q: OSI modelining qaysi pogʻonasi marshrutlash bilan shugʻullanadi va uning PDU si nima?
A: 3-pogʻona — tarmoq pogʻonasi; maʼlumot birligi — paket.
Q: Protokol va interfeysning asosiy farqi nimada?
A: Protokol turli kompyuterlardagi bir xil pogʻonalar orasidagi qoidalar, interfeys esa bitta kompyuterdagi qoʻshni pogʻonalar orasidagi chegara.
Q: Qaysi pogʻona maʼlumot oxiriga treyler qoʻshadi va u nima uchun kerak?
A: Kanal pogʻonasi; treylerdagi FCS (nazorat yigʻindisi) kadr uzatilishida xato boʻlganini aniqlash uchun.
Q: 2920 bayt maʼlumot MSS = 1460 va 58 bayt sarf bilan yuborilsa, kabeldan necha bayt oʻtadi?
A: 2 ta kadr: 2920 + 2 × 58 = 3036 bayt.
Q: TCP/IP modelining "Amaliy" pogʻonasi OSI ning qaysi pogʻonalariga mos keladi?
A: OSI ning 5, 6 va 7-pogʻonalariga (seans, taqdimot, amaliy).
:::

:::link https://www.iso.org/standard/20269.html
ISO/IEC 7498-1: OSI etalon modeli
OSI modelining rasmiy standarti sahifasi (ingliz tilida).
:::
