---
title: Kompyuter tarmoqlari tushunchasi va turlari
summary: Mustaqil ish — paketli kommutatsiya; qatlamli modellar (OSI va TCP/IP) va har bir qatlam vazifasi; manzillash — MAC, IPv4, IPv6, xususiy manzillar, NAT, DHCP, portlar; tarmoqlarni tasniflash (hudud, topologiya, boshqaruv — mijoz-server va peer-to-peer, uzatish muhiti); tarmoq sifati koʻrsatkichlari — oʻtkazish qobiliyati, kechikish, yoʻqotish; bit va bayt; ofis xodimi uchun oddiy diagnostika (ipconfig, ping, tracert, speedtest); fayl uzatish vaqtini hisoblash dasturi.
---

## Mustaqil ishning maqsadi

M6 da tarmoq turlari, qurilmalar va Internet xizmatlari bilan tanishdik. Bu mustaqil ishda tarmoq **ichkaridan qanday ishlashini** chuqurroq koʻramiz: maʼlumot qanday boʻlaklarga boʻlinadi, qatlamlar qanday hamkorlik qiladi, manzillar qanday beriladi va tarmoq sifati qanday oʻlchanadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- paketli kommutatsiya va qatlamli model gʻoyasini tushuntira olasiz;
- MAC va IP manzil, xususiy manzil, NAT, DHCP va port nima ekanini ayta olasiz;
- tarmoqlarni turli belgilar boʻyicha tasniflay olasiz;
- oʻtkazish qobiliyati va kechikishni farqlay olasiz va uzatish vaqtini hisoblay olasiz;
- oddiy tarmoq diagnostikasi buyruqlaridan foydalana olasiz.
:::

## Paketli kommutatsiya

Telefon tarmogʻida qoʻngʻiroq uchun alohida liniya ajratiladi (kanal kommutatsiyasi). Kompyuter tarmoqlarida maʼlumot kichik **paketlarga** boʻlinadi (odatda 1 500 baytgacha), har bir paket manzil bilan alohida yuboriladi va qabul qiluvchida qayta yigʻiladi. Afzalligi: bitta kanaldan koʻplab foydalanuvchilar bir vaqtda foydalanadi, yoʻl buzilsa paketlar boshqa yoʻldan ketadi.

## Qatlamli modellar

Tarmoq vazifalari qatlamlarga boʻlinadi: har bir qatlam faqat oʻz ishini qiladi va qoʻshni qatlamlar bilan "gaplashadi". **OSI** — 7 qatlamli nazariy model, **TCP/IP** — Internetda amalda ishlatiladigan 4 qatlamli model.

| TCP/IP qatlami | OSI qatlamlari | Vazifasi | Misollar |
|---|---|---|---|
| **Ilova** | 7 ilova, 6 taqdimot, 5 seans | foydalanuvchi xizmatlari | HTTP/HTTPS, SMTP, DNS, TLS |
| **Transport** | 4 transport | ilovalar orasida yetkazish, ishonchlilik | TCP (kafolatli), UDP (tez) |
| **Tarmoq (Internet)** | 3 tarmoq | manzillash va yoʻnaltirish | IP, marshrutizator |
| **Kanal va fizik** | 2 kanal, 1 fizik | qoʻshni qurilmalar orasida uzatish, signal | Ethernet, Wi-Fi, kommutator, kabel |

**Analogiya — pochta**: xat matni (ilova) → konvertga solish va buyurtma xat qilish (transport) → manzil va indeks (tarmoq) → mashina, poyezd, samolyot (kanal va fizik). Har bir bosqich boshqasining ichki ishini bilishi shart emas.

**TCP yoki UDP?** TCP har bir paket yetib borganini tekshiradi va yoʻqolganini qayta yuboradi — veb-sahifa, pochta, bank toʻlovi uchun. UDP tekshirmaydi, lekin tez — video qoʻngʻiroq, onlayn oʻyinlar uchun (bitta yoʻqolgan kadr muhim emas).

## Manzillash

| Tushuncha | Mazmuni | Misol |
|---|---|---|
| **MAC manzil** | tarmoq kartasining zavod manzili, mahalliy tarmoqda ishlatiladi | `3C-52-82-1A-7F-04` |
| **IPv4 manzil** | 32 bit, 4 ta 0–255 son; ~4,3 mlrd manzil — yetishmaydi | `213.230.96.15` |
| **IPv6 manzil** | 128 bit, amalda cheksiz manzil | `2001:db8::1` |
| **Xususiy manzillar** | faqat ichki tarmoqda, Internetda yoʻnaltirilmaydi | `192.168.x.x`, `10.x.x.x`, `172.16–31.x.x` |
| **NAT** | marshrutizator koʻplab ichki manzillarni bitta tashqi manzil orqali Internetga chiqaradi | uydagi 10 ta qurilma — 1 ta tashqi IP |
| **DHCP** | qurilmaga IP manzil, shlyuz va DNS ni avtomatik beradi | Wi-Fi ga ulanganda |
| **Port** | bitta kompyuterdagi qaysi ilovaga ekanini bildiradi | 443 — HTTPS, 25 — SMTP |
| **Niqob (mask)** | manzilning qaysi qismi tarmoq, qaysi qismi qurilma | `255.255.255.0` (/24 — 254 ta qurilma) |

## Tarmoqlarni tasniflash

| Belgi | Turlari |
|---|---|
| **Hudud** | PAN, LAN, MAN, WAN (M6) |
| **Topologiya** | yulduz, shina, halqa, toʻr (mesh) |
| **Boshqaruv modeli** | **mijoz-server** (markaziy server — korxona tarmogʻi, sayt) va **peer-to-peer** (teng huquqli tugunlar — fayl almashish, blokcheyn, M12) |
| **Uzatish muhiti** | simli (mis, optik tola), simsiz (Wi-Fi, mobil, sunʼiy yoʻldosh) |
| **Kirish huquqi** | ommaviy (Internet), xususiy (intranet), ekstranet (hamkorlar uchun), VPN orqali virtual xususiy tarmoq |

## Tarmoq sifati koʻrsatkichlari

| Koʻrsatkich | Maʼnosi | Birlik |
|---|---|---|
| **Oʻtkazish qobiliyati** (tezlik) | sekundiga qancha maʼlumot uzatiladi | Mbit/s, Gbit/s |
| **Kechikish** (ping, latency) | paketning borib-qaytish vaqti | ms |
| **Jitter** | kechikishning oʻzgaruvchanligi | ms |
| **Paket yoʻqotish** | yetib bormagan paketlar ulushi | % |

**Muhim**: provayder tezlikni **bit**larda (Mbit/s), fayl hajmi esa **bayt**larda (MB) beriladi; 1 bayt = 8 bit. 100 Mbit/s ≈ 12,5 MB/s. Real tezlik nazariydan odatda 10–30% past (protokollar "yuki", Wi-Fi shovqini, server tezligi).

Video qoʻngʻiroq uchun kechikish va jitter muhim (150 ms dan katta kechikish suhbatni qiyinlashtiradi); katta fayllar uchun — oʻtkazish qobiliyati.

## Ishlangan misol: fayl uzatish vaqti

Har bir satrda fayl nomi, hajmi (MB) va kanal tezligi (Mbit/s). Nazariy uzatish vaqtini va amaliy vaqtni (samaradorlik 80% deb) soat:daqiqa:soniya koʻrinishida, soniyalarni yuqoriga yaxlitlab chiqaring.

```python
import sys

def vaqt_matni(soniya):
    return f"{soniya // 3600}:{soniya % 3600 // 60:02d}:{soniya % 60:02d}"

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, hajm, tezlik = satr.split()
    hajm, tezlik = int(hajm), int(tezlik)
    bit = hajm * 8                                      # MB -> Mbit
    nazariy = (bit + tezlik - 1) // tezlik              # yuqoriga yaxlitlash
    amalda = (bit * 100 + tezlik * 80 - 1) // (tezlik * 80)   # samaradorlik 80%
    print(f"{nom}, {hajm} MB, {tezlik} Mbit/s: nazariy {vaqt_matni(nazariy)}, amalda {vaqt_matni(amalda)}")
```
```java
import java.util.Scanner;

public class Main {
    static String vaqtMatni(long soniya) {
        return String.format("%d:%02d:%02d", soniya / 3600, soniya % 3600 / 60, soniya % 60);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            long hajm = in.nextLong(), tezlik = in.nextLong();
            long bit = hajm * 8;                                         // MB -> Mbit
            long nazariy = (bit + tezlik - 1) / tezlik;                  // yuqoriga yaxlitlash
            long amalda = (bit * 100 + tezlik * 80 - 1) / (tezlik * 80);  // samaradorlik 80%
            System.out.println(nom + ", " + hajm + " MB, " + tezlik + " Mbit/s: nazariy " + vaqtMatni(nazariy) + ", amalda " + vaqtMatni(amalda));
        }
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

string vaqtMatni(long long soniya) {
    char buf[32];
    snprintf(buf, sizeof buf, "%lld:%02lld:%02lld", soniya / 3600, soniya % 3600 / 60, soniya % 60);
    return buf;
}

int main() {
    string nom;
    long long hajm, tezlik;
    while (cin >> nom >> hajm >> tezlik) {
        long long bit = hajm * 8;                                        // MB -> Mbit
        long long nazariy = (bit + tezlik - 1) / tezlik;                 // yuqoriga yaxlitlash
        long long amalda = (bit * 100 + tezlik * 80 - 1) / (tezlik * 80); // samaradorlik 80%
        cout << nom << ", " << hajm << " MB, " << tezlik << " Mbit/s: nazariy " << vaqtMatni(nazariy)
             << ", amalda " << vaqtMatni(amalda) << "\n";
    }
    return 0;
}
```

Kirish:

```text
report.pdf 25 100
video.mp4 1500 100
video.mp4 1500 20
backup.zip 50000 100
backup.zip 50000 1000
```

Chiqish:

```text
report.pdf, 25 MB, 100 Mbit/s: nazariy 0:00:02, amalda 0:00:03
video.mp4, 1500 MB, 100 Mbit/s: nazariy 0:02:00, amalda 0:02:30
video.mp4, 1500 MB, 20 Mbit/s: nazariy 0:10:00, amalda 0:12:30
backup.zip, 50000 MB, 100 Mbit/s: nazariy 1:06:40, amalda 1:23:20
backup.zip, 50000 MB, 1000 Mbit/s: nazariy 0:06:40, amalda 0:08:20
```

50 GB lik zaxira nusxani 100 Mbit/s kanal orqali bulutga yuklash amalda 1 soat 23 daqiqa oladi — bu ish vaqtida boshqa xodimlarning Internetini sekinlashtiradi. Yechimlar: zaxirani tunda yuklash, faqat oʻzgargan fayllarni yuborish (inkremental zaxira) yoki tezroq kanal. Bunday hisob kanal tanlashda (M6) va zaxira rejasida (M14) kerak.

## Oddiy diagnostika

| Buyruq (Windows) | Nima qiladi |
|---|---|
| `ipconfig` | kompyuterning IP manzili, niqob, shlyuz |
| `ping 8.8.8.8` | tashqi serverga yetib borish va kechikish |
| `ping google.com` | DNS ishlashini ham tekshiradi |
| `tracert google.com` | paket qaysi marshrutizatorlar orqali oʻtishi |
| speedtest saytlari | real yuklash va yuborish tezligi, ping |

**Tartib**: "Internet yoʻq" boʻlsa — avval kabel yoki Wi-Fi, keyin `ipconfig` (manzil bormi?), keyin `ping` shlyuzga, keyin tashqi IP ga, keyin domenga. Qaysi qadamda uzilsa — muammo oʻsha joyda. Bu IT boʻlimiga aniq maʼlumot berishga yordam beradi.

:::warning
**Koʻp uchraydigan xatolar**

- **Mbit/s va MB/s ni adashtirish.** 8 marta farq.
- **Tezlikni kechikish bilan adashtirish.** Tez kanalda ham sunʼiy yoʻldosh aloqasi katta kechikish beradi.
- **Xususiy manzil (192.168...) ni Internetdan kirish mumkin deb oʻylash.** U faqat ichki tarmoqda koʻrinadi.
- **Provayder vaʼda qilgan tezlikni kafolat deb hisoblash.** Shartnomadagi SLA ni tekshiring.
:::

## Topshiriq

1. `ipconfig` buyrugʻi bilan kompyuteringiz IP manzili xususiymi yoki ommaviymi aniqlang.
2. Uch xil saytga `ping` qiling va kechikishlarni solishtiring; farqni tushuntiring.
3. Speedtest bilan uy yoki universitet Internetining real tezligini oʻlchang va provayder tarifidagi tezlik bilan solishtiring.
4. Misol dasturi bilan: 2 GB lik video darsni 4G (20 Mbit/s) va optik tola (200 Mbit/s) orqali yuklash vaqtini hisoblang.

## Xulosa

- Maʼlumot paketlarga boʻlinib uzatiladi; tarmoq vazifalari qatlamlarga boʻlinadi (OSI — 7, TCP/IP — 4).
- Manzillash: MAC, IPv4/IPv6, xususiy manzillar va NAT, DHCP, portlar.
- Tasnif: hudud, topologiya, boshqaruv modeli, muhit, kirish huquqi.
- Sifat: oʻtkazish qobiliyati, kechikish, jitter, yoʻqotish; 1 bayt = 8 bit.

## Oʻzingizni tekshiring

:::check
Q: TCP va UDP ning asosiy farqi nima?
A: TCP yetkazishni kafolatlaydi va yoʻqolgan paketlarni qayta yuboradi; UDP kafolatlamaydi, lekin tezroq.
Q: NAT nima uchun kerak?
A: Koʻplab ichki qurilmalarni bitta tashqi IP manzil orqali Internetga chiqarish uchun (IPv4 manzillari yetishmaydi).
Q: 100 Mbit/s kanal orqali 100 MB fayl nazariy jihatdan necha soniyada yuklanadi?
A: 100 × 8 / 100 = 8 soniya.
Q: DHCP nima qiladi?
A: Qurilmaga IP manzil, shlyuz va DNS sozlamalarini avtomatik beradi.
:::

:::link https://www.cloudflare.com/learning/network-layer/what-is-the-network-layer/
Cloudflare Learning Center: Network layer
Tarmoq qatlamlari, IP va marshrutlash boʻyicha sodda tushuntirishlar (ingliz tilida).
:::
