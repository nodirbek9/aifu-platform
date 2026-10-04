---
title: Tarmoq qurishning asosiy tamoyillari. Tarmoq topologiyalari. Tarmoq arxitekturasi. Tarmoq qurilmalari
summary: Tarmoq qurish tamoyillari — standartlashtirish, pogʻonalilik, ortiqchalik; fizik va mantiqiy topologiyalar (shina, yulduz, halqa, toʻliq bogʻlangan, daraxt, gibrid), ularning afzallik va kamchiliklari; tarmoq arxitekturasi; tarmoq qurilmalari — adapter, takrorlagich, konsentrator, koʻprik, kommutator, marshrutizator.
---

## Nima uchun bu mavzu muhim?

Ikkita kompyuterni bitta kabel bilan ulash oson. Lekin 500 ta kompyuter, 20 ta printer va 10 ta serverdan iborat universitet tarmogʻini qanday qurish kerakki, u tez ishlasin, bitta kabel uzilganda toʻxtab qolmasin va kengaytirilishi oson boʻlsin? Bu savolga **topologiya** (qurilmalar qanday ulanadi), **arxitektura** (tarmoq qanday qatlamlarga ajratiladi) va **qurilmalar** (har bir tugunda nima turadi) javob beradi.

:::note
**Bu darsdan soʻng siz:**

- tarmoq qurishning asosiy tamoyillarini tushuntira olasiz;
- asosiy topologiyalarni chizib, ularning afzallik va kamchiliklarini solishtira olasiz;
- fizik va mantiqiy topologiyani farqlay olasiz;
- tarmoq qurilmalarining vazifasini va ular OSI modelining qaysi pogʻonasida ishlashini ayta olasiz;
- topologiya uchun kerakli aloqa liniyalari sonini hisoblay olasiz.
:::

## Tarmoq qurish tamoyillari

| Tamoyil | Maʼnosi |
|---|---|
| **Standartlashtirish** | turli ishlab chiqaruvchilar qurilmalari umumiy standartlar (IEEE 802, TCP/IP) boʻyicha birga ishlaydi |
| **Pogʻonalilik** (modullilik) | murakkab vazifa pogʻonalarga boʻlinadi; har bir pogʻona faqat qoʻshnisi bilan aloqa qiladi |
| **Ortiqchalik** (redundancy) | muhim joylarda zaxira liniyalar va qurilmalar — bittasi buzilsa, boshqasi ishlaydi |
| **Masshtablanuvchanlik** | tarmoqni toʻliq qayta qurmasdan kengaytirish imkoni |
| **Ierarxiya** | yadro (core), taqsimlash (distribution) va kirish (access) sathlari |

## Topologiyalar

**Fizik topologiya** — kabellar va qurilmalar haqiqatda qanday ulangani. **Mantiqiy topologiya** — signal yoki maʼlumot qanday yoʻl bilan harakatlanadi. Masalan, konsentratorli tarmoq fizik jihatdan yulduz, mantiqiy jihatdan esa shina: konsentrator signalni barcha portlarga tarqatadi.

| Topologiya | Tuzilishi | Liniyalar soni (*n* tugun) | Afzalligi | Kamchiligi |
|---|---|---|---|---|
| **Shina** (bus) | barcha tugunlar bitta umumiy kabelga ulanadi | 1 ta umumiy kabel | arzon, oddiy | kabel uzilsa — butun tarmoq ishlamaydi; toʻqnashuvlar |
| **Yulduz** (star) | har bir tugun markaziy qurilmaga ulanadi | *n* | bitta kabel uzilishi faqat bitta tugunga taʼsir qiladi; oson kengaytiriladi | markaziy qurilma buzilsa — hammasi toʻxtaydi |
| **Halqa** (ring) | har bir tugun ikki qoʻshnisiga ulanib, yopiq halqa hosil qiladi | *n* | toʻqnashuvsiz (marker usulida) | bitta uzilish (oddiy halqada) tarmoqni buzadi |
| **Toʻliq bogʻlangan** (full mesh) | har bir tugun barcha boshqalariga ulanadi | *n*(*n* − 1)/2 | eng ishonchli | juda qimmat; *n* oshganda liniyalar soni kvadratik oʻsadi |
| **Daraxt** (tree) | yulduzlarning ierarxik birikmasi | *n* − 1 | katta tarmoqlar uchun qulay | yuqori sathdagi qurilma buzilsa — butun shox uziladi |
| **Gibrid** | turli topologiyalar aralashmasi | — | moslashuvchan | loyihalash murakkab |

Zamonaviy LAN lar deyarli har doim **kommutatorli yulduz** yoki **daraxt**; WAN va provayder tarmoqlari esa ishonchlilik uchun **qisman bogʻlangan** (partial mesh).

## Tarmoq arxitekturasi

**Tarmoq arxitekturasi** — tarmoqning tuzilishi, protokollari va qurilmalari haqidagi umumiy tavsif: qanday pogʻonalarga boʻlingani (OSI, TCP/IP), qaysi protokollar ishlatilishi va tugunlar qanday rol oʻynashi. Asosiy arxitektura modellari:

- **Mijoz–server** — xizmatlar maxsus serverlarda;
- **Peer-to-peer** — tugunlar teng huquqli;
- **Uch sathli ierarxik model** (Cisco) — kirish sathi (foydalanuvchi kompyuterlari ulanadigan kommutatorlar), taqsimlash sathi (bino va qavatlar orasidagi marshrutlash), yadro (tez magistral).

## Tarmoq qurilmalari

| Qurilma | Vazifasi | OSI pogʻonasi | Asosiy xususiyati |
|---|---|---|---|
| **Tarmoq adapteri** (NIC) | kompyuterni tarmoqqa ulaydi | 1–2 | oʻzining MAC manziliga ega |
| **Takrorlagich** (repeater) | zaiflashgan signalni kuchaytiradi | 1 | maʼlumot mazmunini tushunmaydi |
| **Konsentrator** (hub) | koʻp portli takrorlagich | 1 | kadrni **barcha** portlarga yuboradi |
| **Koʻprik** (bridge) | ikki segmentni bogʻlaydi | 2 | MAC manzil boʻyicha kadrni oʻtkazadi yoki toʻsadi |
| **Kommutator** (switch) | koʻp portli koʻprik | 2 | kadrni faqat **kerakli** portga yuboradi |
| **Marshrutizator** (router) | turli tarmoqlarni bogʻlaydi | 3 | IP manzil boʻyicha eng yaxshi yoʻlni tanlaydi |
| **Shlyuz** (gateway) | turli protokollarni bogʻlaydi | 4–7 | protokollarni bir-biriga oʻgiradi |
| **Simsiz kirish nuqtasi** (access point) | Wi-Fi qurilmalarini simli tarmoqqa ulaydi | 1–2 | radio va Ethernet orasida koʻprik |

## Ishlangan misol 1: topologiya uchun liniyalar soni

Birinchi satrda *k*, keyingi *k* satrda topologiya nomi (`shina`, `yulduz`, `halqa`, `toliq`, `daraxt`) va tugunlar soni *n*. Har biri uchun kerakli aloqa liniyalari (kabellar) sonini chiqaring: shinada umumiy kabel bitta, yulduzda *n* (markaziy qurilmaga), halqada *n*, toʻliq bogʻlanganda *n*(*n* − 1)/2, daraxtda *n* − 1.

```python
def liniyalar(tur, n):
    if tur == "shina":
        return 1                        # bitta umumiy kabel
    if tur in ("yulduz", "halqa"):
        return n
    if tur == "toliq":
        return n * (n - 1) // 2         # har bir juftlik — alohida liniya
    return n - 1                        # daraxt


k = int(input())
for _ in range(k):
    tur, n = input().split()
    print(tur, liniyalar(tur, int(n)))
```
```java
import java.util.Scanner;

public class Main {
    static long liniyalar(String tur, long n) {
        switch (tur) {
            case "shina": return 1;                 // bitta umumiy kabel
            case "yulduz": case "halqa": return n;
            case "toliq": return n * (n - 1) / 2;   // har bir juftlik — alohida liniya
            default: return n - 1;                  // daraxt
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int k = in.nextInt();
        for (int i = 0; i < k; i++) {
            String tur = in.next();
            long n = in.nextLong();
            System.out.println(tur + " " + liniyalar(tur, n));
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

long long liniyalar(const string& tur, long long n) {
    if (tur == "shina") return 1;                   // bitta umumiy kabel
    if (tur == "yulduz" || tur == "halqa") return n;
    if (tur == "toliq") return n * (n - 1) / 2;     // har bir juftlik — alohida liniya
    return n - 1;                                   // daraxt
}

int main() {
    int k;
    cin >> k;
    for (int i = 0; i < k; i++) {
        string tur;
        long long n;
        cin >> tur >> n;
        cout << tur << " " << liniyalar(tur, n) << endl;
    }
    return 0;
}
```

Kirish:

```text
4
yulduz 30
halqa 30
toliq 30
toliq 300
```

Chiqish:

```text
yulduz 30
halqa 30
toliq 435
toliq 44850
```

30 ta kompyuter uchun yulduzda 30 ta kabel kerak, toʻliq bogʻlanganda esa 435 ta; 300 ta kompyuterda — 44 850 ta. Shuning uchun toʻliq bogʻlangan topologiya faqat bir necha muhim tugun (masalan, yadro marshrutizatorlari) orasida ishlatiladi.

## Ishlangan misol 2: grafdan topologiyani aniqlash

Birinchi satrda tugunlar soni *n* va ulanishlar soni *m*, keyingi *m* satrda ulanishlar `u v` (har bir juftlik bir marta). Topologiyani aniqlang: `yulduz` (bitta markaz hammaga ulangan, qolganlar faqat markazga), `halqa` (barcha tugunlar 2 ta qoʻshnili va bogʻlamli), `toliq` (har bir tugun qolgan hammaga ulangan), `chiziq` (ikki chetki tugun 1 ta, qolganlar 2 ta qoʻshnili, bogʻlamli), aks holda `aralash`. *n* ≥ 4 deb hisoblang.

```python
from collections import deque

n, m = map(int, input().split())
qoshni = [[] for _ in range(n + 1)]
for _ in range(m):
    u, v = map(int, input().split())
    qoshni[u].append(v)
    qoshni[v].append(u)

daraja = [len(qoshni[i]) for i in range(1, n + 1)]

korilgan = {1}                              # bogʻlamlilikni BFS bilan tekshiramiz
navbat = deque([1])
while navbat:
    v = navbat.popleft()
    for u in qoshni[v]:
        if u not in korilgan:
            korilgan.add(u)
            navbat.append(u)
boglamli = len(korilgan) == n

if all(d == n - 1 for d in daraja):
    print("toliq")
elif daraja.count(n - 1) == 1 and daraja.count(1) == n - 1:
    print("yulduz")
elif boglamli and all(d == 2 for d in daraja):
    print("halqa")
elif boglamli and daraja.count(1) == 2 and daraja.count(2) == n - 2:
    print("chiziq")
else:
    print("aralash")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), m = in.nextInt();
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int k = 0; k < m; k++) {
            int u = in.nextInt(), v = in.nextInt();
            qoshni.get(u).add(v);
            qoshni.get(v).add(u);
        }

        boolean[] korilgan = new boolean[n + 1];        // bogʻlamlilikni BFS bilan tekshiramiz
        ArrayDeque<Integer> navbat = new ArrayDeque<>();
        korilgan[1] = true;
        navbat.add(1);
        int soni = 1;
        while (!navbat.isEmpty()) {
            int v = navbat.poll();
            for (int u : qoshni.get(v)) {
                if (!korilgan[u]) {
                    korilgan[u] = true;
                    soni++;
                    navbat.add(u);
                }
            }
        }
        boolean boglamli = soni == n;

        int markazlar = 0, birlar = 0, ikkilar = 0;
        for (int i = 1; i <= n; i++) {
            int d = qoshni.get(i).size();
            if (d == n - 1) markazlar++;
            if (d == 1) birlar++;
            if (d == 2) ikkilar++;
        }

        if (markazlar == n) System.out.println("toliq");
        else if (markazlar == 1 && birlar == n - 1) System.out.println("yulduz");
        else if (boglamli && ikkilar == n) System.out.println("halqa");
        else if (boglamli && birlar == 2 && ikkilar == n - 2) System.out.println("chiziq");
        else System.out.println("aralash");
    }
}
```
```cpp
#include <iostream>
#include <queue>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> qoshni(n + 1);
    for (int k = 0; k < m; k++) {
        int u, v;
        cin >> u >> v;
        qoshni[u].push_back(v);
        qoshni[v].push_back(u);
    }

    vector<bool> korilgan(n + 1, false);                // bogʻlamlilikni BFS bilan tekshiramiz
    queue<int> navbat;
    korilgan[1] = true;
    navbat.push(1);
    int soni = 1;
    while (!navbat.empty()) {
        int v = navbat.front();
        navbat.pop();
        for (int u : qoshni[v]) {
            if (!korilgan[u]) {
                korilgan[u] = true;
                soni++;
                navbat.push(u);
            }
        }
    }
    bool boglamli = soni == n;

    int markazlar = 0, birlar = 0, ikkilar = 0;
    for (int i = 1; i <= n; i++) {
        int d = qoshni[i].size();
        if (d == n - 1) markazlar++;
        if (d == 1) birlar++;
        if (d == 2) ikkilar++;
    }

    if (markazlar == n) cout << "toliq" << endl;
    else if (markazlar == 1 && birlar == n - 1) cout << "yulduz" << endl;
    else if (boglamli && ikkilar == n) cout << "halqa" << endl;
    else if (boglamli && birlar == 2 && ikkilar == n - 2) cout << "chiziq" << endl;
    else cout << "aralash" << endl;
    return 0;
}
```

| Kirish (*n*, ulanishlar) | Natija |
|---|---|
| 5: 1-2, 1-3, 1-4, 1-5 | `yulduz` |
| 4: 1-2, 2-3, 3-4, 4-1 | `halqa` |
| 4: 1-2, 2-3, 3-4 | `chiziq` |
| 4: barcha 6 juftlik | `toliq` |
| 5: 1-2, 2-3, 3-1, 3-4, 4-5 | `aralash` |

Topologiyani aniqlash uchun ikki belgi yetarli: har bir tugunning **darajasi** (nechta qoʻshnisi borligi) va grafning **bogʻlamliligi**. Ikkita alohida halqa ham barcha tugunlari 2 darajali boʻladi, shuning uchun halqa va chiziq uchun bogʻlamlilik BFS bilan tekshirildi. Bu tahlil tarmoq monitoring tizimlarida ishlatiladi: ular qurilmalar ulanishini (masalan, LLDP orqali) yigʻib, tarmoq xaritasini avtomatik chizadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Fizik va mantiqiy topologiyani aralashtirish.** Konsentratorli yulduz mantiqan shina boʻlib ishlaydi: barcha kompyuterlar bitta „umumiy kanal“ uchun raqobatlashadi.
- **Konsentrator va kommutatorni bir qurilma deb hisoblash.** Konsentrator kadrni hamma portga, kommutator faqat kerakli portga yuboradi.
- **Yulduzning „yagona nosozlik nuqtasi“ni unutish.** Markaziy kommutator buzilsa, butun segment toʻxtaydi — muhim tarmoqlarda zaxira kommutator qoʻyiladi.
- **Marshrutizatorni kommutator oʻrnida ishlatish.** Marshrutizator turli tarmoqlarni (IP boʻyicha) bogʻlaydi; bitta LAN ichida qurilmalarni ulash — kommutator vazifasi.
:::

:::tip
Universitetingizdagi yoki uyingizdagi tarmoq sxemasini qogʻozga chizing: qaysi qurilma qayerda, qanday ulangan. Keyin har bir qurilma buzilsa, qaysi foydalanuvchilar aloqasiz qolishini belgilang — bu tarmoqning zaif joylarini koʻrsatadi.
:::

## Xulosa

- Tarmoq qurish tamoyillari: standartlashtirish, pogʻonalilik, ortiqchalik, masshtablanuvchanlik, ierarxiya.
- Asosiy topologiyalar: shina, yulduz, halqa, toʻliq bogʻlangan, daraxt, gibrid; fizik va mantiqiy topologiya farq qilishi mumkin.
- Toʻliq bogʻlangan topologiyada liniyalar soni *n*(*n* − 1)/2 — eng ishonchli, lekin eng qimmat.
- Qurilmalar pogʻonalar boʻyicha: takrorlagich va konsentrator (1), koʻprik va kommutator (2), marshrutizator (3), shlyuz (4–7).

## Oʻzingizni tekshiring

:::check
Q: 10 ta tugunli toʻliq bogʻlangan tarmoqda nechta liniya bor?
A: 10 · 9 / 2 = 45 ta.
Q: Yulduz topologiyasining asosiy kamchiligi nima?
A: Markaziy qurilma — yagona nosozlik nuqtasi: u buzilsa, butun tarmoq ishlamaydi.
Q: Kommutator kadrni qayerga yuborishni nimaga qarab hal qiladi?
A: Qabul qiluvchining MAC manziliga qarab (oʻzining MAC jadvali yordamida).
Q: Qaysi qurilma turli IP tarmoqlarini bogʻlaydi?
A: Marshrutizator.
Q: Konsentratorli tarmoq fizik va mantiqiy jihatdan qanday topologiyaga ega?
A: Fizik — yulduz, mantiqiy — shina.
:::

:::link https://www.cisco.com/c/en/us/solutions/small-business/resource-center/networking/networking-basics.html
Cisco: tarmoq asoslari
Tarmoq qurilmalari va topologiyalar haqida kirish materiali (ingliz tilida).
:::
