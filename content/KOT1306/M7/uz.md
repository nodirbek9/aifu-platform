---
title: TCP/IP protokollar steki. Tarmoqlararo berilganlarni almashish tamoyillari. IP protokollari haqida tushuncha
summary: TCP/IP steki pogʻonalari va protokollari; tarmoqlararo almashinuv tamoyillari — datagramma, ulanishsiz va "eng yaxshi urinish" yetkazish, oxiridan-oxirigacha tamoyili; IPv4 sarlavhasi maydonlari; TTL; nazorat yigʻindisini hisoblash; fragmentatsiya va MTU; ICMP vazifasi; IPv6 ning asosiy farqlari.
---

## Nima uchun bu mavzu muhim?

Internet — minglab mustaqil tarmoqlarning (provayderlar, universitetlar, kompaniyalar) birlashmasi. Ular turli texnologiyalarda qurilgan: biri Ethernet, boshqasi Wi-Fi, uchinchisi optik magistral. Bularning hammasini bitta umumiy "tilga" birlashtiradigan narsa — **IP protokoli**. Har bir paket IP sarlavhasi bilan yuradi va har bir marshrutizator faqat shu sarlavhaga qarab qaror qabul qiladi. Shuning uchun IP sarlavhasini maydonma-maydon bilish — tarmoq muhandisi uchun alifbo.

:::note
**Bu darsdan soʻng siz:**

- TCP/IP stekining toʻrt pogʻonasini va har biridagi asosiy protokollarni ayta olasiz;
- IP ning datagramma, ulanishsiz va "eng yaxshi urinish" tamoyillarini tushuntira olasiz;
- IPv4 sarlavhasi maydonlarini oʻqib, ularning maʼnosini ayta olasiz;
- IP sarlavhasining nazorat yigʻindisini hisoblay va tekshira olasiz;
- paket MTU dan katta boʻlganda qanday fragmentlarga boʻlinishini hisoblay olasiz.
:::

## TCP/IP steki

| Pogʻona | Protokollar | Maʼlumot birligi | Manzil |
|---|---|---|---|
| **Amaliy** | HTTP, HTTPS, DNS, SMTP, FTP, SSH, DHCP | xabar | domen nomi, URL |
| **Transport** | TCP, UDP | segment / datagramma | port |
| **Internet** (tarmoqlararo) | IP (v4, v6), ICMP, IGMP | paket | IP manzil |
| **Tarmoq kirishi** | Ethernet, Wi-Fi, PPP; ARP | kadr | MAC manzil |

Stek **"qum soati"** shaklida: pastda koʻplab kanal texnologiyalari, yuqorida koʻplab ilovalar, oʻrtada esa — yagona **IP**. Bu "hamma narsa IP ustida, IP hamma narsa ustida" (everything over IP, IP over everything) tamoyili Internetning moslashuvchanligini taʼminlaydi: yangi kabel turi chiqsa, faqat pastki pogʻona oʻzgaradi; yangi ilova chiqsa, faqat yuqorisi.

## Tarmoqlararo almashinuv tamoyillari

| Tamoyil | Maʼnosi |
|---|---|
| **Datagramma** | har bir paket mustaqil — oʻz manzilini toʻliq oʻzida olib yuradi; turli paketlar turli yoʻllardan borishi mumkin |
| **Ulanishsiz** (connectionless) | yuborishdan oldin ulanish oʻrnatilmaydi, tarmoq "suhbat" holatini saqlamaydi |
| **Eng yaxshi urinish** (best effort) | IP yetkazishga harakat qiladi, lekin **kafolat bermaydi**: paket yoʻqolishi, takrorlanishi yoki tartibi buzilishi mumkin |
| **Oxiridan-oxirigacha** (end-to-end) | ishonchlilik, tartib va qayta yuborish — oxirgi tizimlar (TCP) vazifasi; tarmoq markazi oddiy va tez qoladi |
| **Marshrutlash bosqichma-bosqich** (hop-by-hop) | har bir marshrutizator faqat **keyingi qadamni** tanlaydi, butun yoʻlni bilmaydi |

## IPv4 sarlavhasi

Minimal sarlavha — **20 bayt** (beshta 32 bitli soʻz):

| Bayt | Maydon | Bit | Maʼnosi |
|---|---|---|---|
| 0 | Versiya, IHL | 4 + 4 | versiya = 4; IHL — sarlavha uzunligi 4 baytli soʻzlarda (odatda 5 → 20 bayt) |
| 1 | DSCP/ECN (xizmat turi) | 8 | ustuvorlik va tirbandlik belgisi |
| 2–3 | Umumiy uzunlik | 16 | sarlavha + maʼlumot, baytlarda (eng koʻpi 65 535) |
| 4–5 | Identifikator | 16 | bitta paketning fragmentlarini birlashtirish uchun |
| 6–7 | Bayroqlar, fragment siljishi | 3 + 13 | DF — "boʻlma", MF — "yana fragment bor"; siljish 8 baytli birliklarda |
| 8 | **TTL** | 8 | har bir marshrutizator 1 ga kamaytiradi; 0 boʻlsa — paket tashlanadi |
| 9 | Protokol | 8 | ichidagi maʼlumot: 1 — ICMP, 6 — TCP, 17 — UDP |
| 10–11 | Nazorat yigʻindisi | 16 | faqat **sarlavha** uchun |
| 12–15 | Manba IP | 32 | yuboruvchi |
| 16–19 | Qabul qiluvchi IP | 32 | oluvchi |

**TTL** (Time To Live) paketlarning marshrutlash halqalarida abadiy aylanishiga yoʻl qoʻymaydi. `tracert` (Windows) / `traceroute` (Linux) buyrugʻi aynan TTL = 1, 2, 3, … bilan paketlar yuborib, har bir marshrutizatordan kelgan "TTL tugadi" ICMP xabari orqali yoʻlni aniqlaydi.

## Nazorat yigʻindisi

IP sarlavhasi nazorat yigʻindisi **birlarni toʻldiruvchi** (ones' complement) qoʻshish bilan hisoblanadi:

1. Nazorat yigʻindisi maydoni 0 deb olinadi.
2. Sarlavha 16 bitli soʻzlarga boʻlinib, barchasi qoʻshiladi.
3. 16 bitdan oshgan qism (koʻchirma) pastki 16 bitga qayta qoʻshiladi — natija 16 bitga sigʻguncha.
4. Natija inversiya qilinadi (0xFFFF dan ayiriladi).

Qabul qiluvchi butun sarlavhani (nazorat yigʻindisi bilan birga) xuddi shunday qoʻshadi: natija **0xFFFF** boʻlsa — sarlavha buzilmagan. Har bir marshrutizator TTL ni kamaytirgani uchun nazorat yigʻindisini ham qayta hisoblaydi.

## Fragmentatsiya

Har bir kanalning **MTU** si (Maximum Transmission Unit) bor: Ethernet — 1500 bayt. Paket kanal MTU sidan katta boʻlsa, marshrutizator uni **fragmentlarga** boʻladi (agar DF bayrogʻi qoʻyilmagan boʻlsa):

- har bir fragment oʻz IP sarlavhasiga ega, identifikator bir xil;
- maʼlumot qismi 8 ga karrali boʻladi (oxirgisidan tashqari), chunki siljish 8 baytli birliklarda yoziladi;
- oxirgisidan boshqa barcha fragmentlarda MF = 1;
- fragmentlar faqat **qabul qiluvchida** yigʻiladi.

DF qoʻyilgan va MTU dan katta paket tashlanadi, yuboruvchiga esa ICMP "Fragmentation needed" xabari qaytadi — **Path MTU Discovery** usuli shunga asoslangan. IPv6 da marshrutizatorlar umuman fragmentatsiya qilmaydi.

## ICMP va IPv6 haqida qisqacha

**ICMP** (Internet Control Message Protocol) — IP ning "xizmat xabarlari": `Echo Request/Reply` (`ping`), `Destination Unreachable`, `Time Exceeded` (TTL tugadi), `Fragmentation Needed`. ICMP oʻzi IP paketi ichida yuradi (protokol = 1).

**IPv6**: manzil 128 bit (`2001:db8::1`), sarlavha qatʼiy 40 bayt va soddalashtirilgan (nazorat yigʻindisi yoʻq, fragmentatsiya — faqat yuboruvchida), TTL oʻrniga Hop Limit, broadkast oʻrniga multicast. IPv4 manzillari tugaganidan keyin IPv6 tez tarqalmoqda.

## Ishlangan misol 1: IPv4 sarlavhasini tahlil qilish

Kirishda IPv4 sarlavhasining 20 bayti oʻn oltilik koʻrinishda. Asosiy maydonlarni chiqaring va nazorat yigʻindisini tekshiring; notoʻgʻri boʻlsa, toʻgʻri qiymatini ham chiqaring.

```python
import sys

b = [int(t, 16) for t in sys.stdin.read().split()]


def yigindi(baytlar):
    s = 0
    for i in range(0, len(baytlar), 2):
        s += baytlar[i] * 256 + baytlar[i + 1]
    while s > 0xFFFF:
        s = (s & 0xFFFF) + (s >> 16)          # koʻchirmani qaytarib qoʻshish
    return s


nomlar = {1: "ICMP", 6: "TCP", 17: "UDP"}
print("versiya:", b[0] >> 4, "sarlavha:", (b[0] & 15) * 4, "bayt")
print("umumiy uzunlik:", b[2] * 256 + b[3])
print("TTL:", b[8])
print("protokol: " + str(b[9]) + " (" + nomlar.get(b[9], "?") + ")")
print("manba:", ".".join(str(x) for x in b[12:16]))
print("qabul qiluvchi:", ".".join(str(x) for x in b[16:20]))
yozilgan = b[10] * 256 + b[11]
if yigindi(b[:20]) == 0xFFFF:
    print("nazorat yigʻindisi: 0x" + format(yozilgan, "04X") + " toʻgʻri")
else:
    toza = b[:10] + [0, 0] + b[12:20]
    togrisi = 0xFFFF - yigindi(toza)
    print("nazorat yigʻindisi: 0x" + format(yozilgan, "04X") + " xato, toʻgʻrisi 0x" + format(togrisi, "04X"))
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int yigindi(int[] baytlar) {
        int s = 0;
        for (int i = 0; i < baytlar.length; i += 2) {
            s += baytlar[i] * 256 + baytlar[i + 1];
        }
        while (s > 0xFFFF) {
            s = (s & 0xFFFF) + (s >> 16);                  // koʻchirmani qaytarib qoʻshish
        }
        return s;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Integer> oqilgan = new ArrayList<>();
        while (in.hasNext()) oqilgan.add(Integer.parseInt(in.next(), 16));
        int[] b = new int[20];
        for (int i = 0; i < 20; i++) b[i] = oqilgan.get(i);

        String nom = b[9] == 1 ? "ICMP" : b[9] == 6 ? "TCP" : b[9] == 17 ? "UDP" : "?";
        System.out.println("versiya: " + (b[0] >> 4) + " sarlavha: " + (b[0] & 15) * 4 + " bayt");
        System.out.println("umumiy uzunlik: " + (b[2] * 256 + b[3]));
        System.out.println("TTL: " + b[8]);
        System.out.println("protokol: " + b[9] + " (" + nom + ")");
        System.out.println("manba: " + b[12] + "." + b[13] + "." + b[14] + "." + b[15]);
        System.out.println("qabul qiluvchi: " + b[16] + "." + b[17] + "." + b[18] + "." + b[19]);
        int yozilgan = b[10] * 256 + b[11];
        if (yigindi(b) == 0xFFFF) {
            System.out.println("nazorat yigʻindisi: 0x" + String.format("%04X", yozilgan) + " toʻgʻri");
        } else {
            int[] toza = b.clone();
            toza[10] = 0;
            toza[11] = 0;
            int togrisi = 0xFFFF - yigindi(toza);
            System.out.println("nazorat yigʻindisi: 0x" + String.format("%04X", yozilgan) + " xato, toʻgʻrisi 0x" + String.format("%04X", togrisi));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int yigindi(const vector<int>& baytlar) {
    int s = 0;
    for (size_t i = 0; i < baytlar.size(); i += 2) {
        s += baytlar[i] * 256 + baytlar[i + 1];
    }
    while (s > 0xFFFF) {
        s = (s & 0xFFFF) + (s >> 16);                      // koʻchirmani qaytarib qoʻshish
    }
    return s;
}

string hex4(int x) {
    char buf[8];
    snprintf(buf, sizeof buf, "%04X", x);
    return buf;
}

int main() {
    vector<int> b;
    string t;
    while (cin >> t) b.push_back(stoi(t, nullptr, 16));
    b.resize(20);

    string nom = b[9] == 1 ? "ICMP" : b[9] == 6 ? "TCP" : b[9] == 17 ? "UDP" : "?";
    cout << "versiya: " << (b[0] >> 4) << " sarlavha: " << (b[0] & 15) * 4 << " bayt" << endl;
    cout << "umumiy uzunlik: " << b[2] * 256 + b[3] << endl;
    cout << "TTL: " << b[8] << endl;
    cout << "protokol: " << b[9] << " (" << nom << ")" << endl;
    cout << "manba: " << b[12] << "." << b[13] << "." << b[14] << "." << b[15] << endl;
    cout << "qabul qiluvchi: " << b[16] << "." << b[17] << "." << b[18] << "." << b[19] << endl;
    int yozilgan = b[10] * 256 + b[11];
    if (yigindi(b) == 0xFFFF) {
        cout << "nazorat yigʻindisi: 0x" << hex4(yozilgan) << " toʻgʻri" << endl;
    } else {
        vector<int> toza = b;
        toza[10] = 0;
        toza[11] = 0;
        int togrisi = 0xFFFF - yigindi(toza);
        cout << "nazorat yigʻindisi: 0x" << hex4(yozilgan) << " xato, toʻgʻrisi 0x" << hex4(togrisi) << endl;
    }
    return 0;
}
```

Kirish:

```text
45 00 00 3c 1c 46 40 00 40 06 b1 e6 ac 10 0a 63 ac 10 0a 0c
```

Chiqish:

```text
versiya: 4 sarlavha: 20 bayt
umumiy uzunlik: 60
TTL: 64
protokol: 6 (TCP)
manba: 172.16.10.99
qabul qiluvchi: 172.16.10.12
nazorat yigʻindisi: 0xB1E6 toʻgʻri
```

Endi TTL ni 64 dan 63 ga oʻzgartiramiz (9-bayt `3f`), nazorat yigʻindisini esa yangilamaymiz — xuddi marshrutizator xato ishlagandek:

```text
45 00 00 3c 1c 46 40 00 3f 06 b1 e6 ac 10 0a 63 ac 10 0a 0c
```

Chiqishning oxirgi qatori:

```text
nazorat yigʻindisi: 0xB1E6 xato, toʻgʻrisi 0xB2E6
```

TTL 1 ga kamaydi — nazorat yigʻindisi 0x0100 ga oshdi (TTL 16 bitli soʻzning yuqori baytida turadi). Marshrutizatorlar shu xossadan foydalanib, nazorat yigʻindisini butunlay qayta hisoblamasdan yangilaydi.

## Ishlangan misol 2: fragmentatsiya

Bir satrda IP paketining maʼlumot qismi uzunligi *L* (bayt) va kanal MTU si. Sarlavha 20 bayt. Har bir fragment uchun tartib raqami, umumiy uzunligi (sarlavha bilan), siljishi (8 baytli birliklarda) va MF bayrogʻini chiqaring.

```python
l, mtu = map(int, input().split())
qism = (mtu - 20) // 8 * 8               # fragmentdagi maʼlumot 8 ga karrali
joy = 0
raqam = 0
while joy < l:
    raqam += 1
    hajm = min(qism, l - joy)
    mf = 1 if joy + hajm < l else 0       # oxirgisidan tashqari MF = 1
    print(raqam, "len=" + str(hajm + 20), "offset=" + str(joy // 8), "MF=" + str(mf))
    joy += hajm
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int l = in.nextInt();
        int mtu = in.nextInt();
        int qism = (mtu - 20) / 8 * 8;                       // fragmentdagi maʼlumot 8 ga karrali
        int joy = 0;
        int raqam = 0;
        while (joy < l) {
            raqam++;
            int hajm = Math.min(qism, l - joy);
            int mf = joy + hajm < l ? 1 : 0;                  // oxirgisidan tashqari MF = 1
            System.out.println(raqam + " len=" + (hajm + 20) + " offset=" + joy / 8 + " MF=" + mf);
            joy += hajm;
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
using namespace std;

int main() {
    int l, mtu;
    cin >> l >> mtu;
    int qism = (mtu - 20) / 8 * 8;                           // fragmentdagi maʼlumot 8 ga karrali
    int joy = 0;
    int raqam = 0;
    while (joy < l) {
        raqam++;
        int hajm = min(qism, l - joy);
        int mf = joy + hajm < l ? 1 : 0;                      // oxirgisidan tashqari MF = 1
        cout << raqam << " len=" << hajm + 20 << " offset=" << joy / 8 << " MF=" << mf << endl;
        joy += hajm;
    }
    return 0;
}
```

Kirish:

```text
4000 1500
```

Chiqish:

```text
1 len=1500 offset=0 MF=1
2 len=1500 offset=185 MF=1
3 len=1060 offset=370 MF=0
```

4000 baytlik maʼlumot 1480 + 1480 + 1040 ga boʻlindi. Ikkinchi fragmentning siljishi 185 — bu 185 × 8 = 1480 bayt. MTU = 576 boʻlsa, fragmentdagi maʼlumot 556 emas, 552 bayt boʻladi (8 ga karrali).

:::warning
**Koʻp uchraydigan xatolar**

- **IHL ni baytlarda deb oʻqish.** IHL = 5 — bu 5 ta 4 baytli soʻz, yaʼni 20 bayt.
- **IP nazorat yigʻindisi maʼlumotni ham himoya qiladi deb oʻylash.** U faqat sarlavhani tekshiradi; maʼlumot uchun TCP/UDP nazorat yigʻindisi va kanal pogʻonasining CRC si bor.
- **Fragment siljishini baytlarda yozish.** Siljish 8 baytli birliklarda: 1480 bayt → 185.
- **Fragmentlarni marshrutizator yigʻadi deb hisoblash.** Fragmentlar faqat oxirgi qabul qiluvchida birlashtiriladi.
- **IP yetkazishni kafolatlaydi deb oʻylash.** IP — "eng yaxshi urinish"; ishonchlilik TCP vazifasi.
:::

:::tip
`tracert 8.8.8.8` (yoki `traceroute`) buyrugʻini bajarib, Wireshark da `icmp` filtrida "Time-to-live exceeded" javoblarini koʻring. Har bir javobdagi ichki IP sarlavhasida TTL qanday ekanini tekshiring.
:::

## Xulosa

- TCP/IP — 4 pogʻona: amaliy, transport, internet, tarmoq kirishi; oʻrtada yagona IP ("qum soati").
- IP — datagramma, ulanishsiz, eng yaxshi urinish; ishonchlilik — oxirgi tizimlarda.
- IPv4 sarlavhasi 20 bayt: versiya, IHL, uzunlik, identifikator, bayroqlar va siljish, TTL, protokol, nazorat yigʻindisi, manba va qabul qiluvchi.
- Nazorat yigʻindisi — 16 bitli soʻzlarning birlarni toʻldiruvchi yigʻindisining inversiyasi; toʻgʻri sarlavhaning yigʻindisi 0xFFFF.
- MTU dan katta paket 8 ga karrali boʻlaklarga boʻlinadi; MF va siljish orqali qabul qiluvchida yigʻiladi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun IP "eng yaxshi urinish" protokoli deyiladi?
A: U paketni yetkazishga harakat qiladi, lekin yetkazilishini, tartibini va takrorlanmasligini kafolatlamaydi.
Q: Birinchi bayti `46` boʻlgan IPv4 sarlavhasining uzunligi qancha?
A: IHL = 6 → 6 × 4 = 24 bayt (4 bayt qoʻshimcha maydonlar bilan).
Q: TTL maydoni nima uchun kerak?
A: Paket marshrutlash halqasida abadiy aylanmasligi uchun; har bir marshrutizator uni 1 ga kamaytiradi, 0 boʻlsa paket tashlanadi.
Q: 3000 bayt maʼlumot MTU = 1500 boʻlgan kanalda nechta fragmentga boʻlinadi?
A: 3 ta: 1480 + 1480 + 40 bayt maʼlumot.
Q: Protokol maydoni 17 boʻlsa, paket ichida nima bor?
A: UDP datagrammasi.
:::

:::link https://www.rfc-editor.org/rfc/rfc791
RFC 791: Internet Protocol
IPv4 protokolining asl spetsifikatsiyasi (ingliz tilida).
:::
