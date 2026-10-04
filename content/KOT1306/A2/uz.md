---
title: Tarmoq topologiyalarini oʻrganish
summary: Fizik va mantiqiy topologiyalarni amalda tahlil qilish; tarmoqni graf koʻrinishida ifodalash; kabel turlari va ulanish sxemalari (T568A/T568B, toʻgʻri va kross kabel); aloqa uzilganda tarmoq bogʻliqligini tekshirish; yagona nosozlik nuqtalarini (kritik tugunlarni) topish; Packet Tracer da topologiya yigʻish.
---

## Nima uchun bu mavzu muhim?

M2 maʼruzasida topologiyalar turlari va ularning afzallik hamda kamchiliklarini koʻrdik. Amalda esa muhandis boshqa savolga javob beradi: "Agar shu kabel uzilsa yoki shu kommutator yonib ketsa, kim aloqasiz qoladi?" Bu mashgʻulotda tarmoqni **graf** sifatida tasvirlab, bu savolga dastur yordamida javob beramiz va Packet Tracer da topologiyalarni yigʻamiz.

:::note
**Bu mashgʻulotdan soʻng siz:**

- tarmoq sxemasini graf (tugunlar va qirralar) koʻrinishida yoza olasiz;
- T568A va T568B sxemalarini, toʻgʻri va kross kabellarning qachon ishlatilishini bilasiz;
- aloqa liniyalari uzilganda tarmoq nechta boʻlakka boʻlinishini hisoblay olasiz;
- tarmoqdagi yagona nosozlik nuqtalarini (kritik tugunlarni) topa olasiz;
- Packet Tracer da yulduz, halqa va gibrid topologiyalarni yigʻib, aloqani tekshira olasiz.
:::

## Tarmoq — bu graf

Har qanday topologiyani **graf** sifatida ifodalash mumkin: **tugunlar** — qurilmalar (kompyuter, kommutator, marshrutizator), **qirralar** — ular orasidagi aloqa liniyalari. Shunda topologiya xususiyatlari graf tushunchalariga aylanadi:

| Tarmoq savoli | Graf tushunchasi |
|---|---|
| Ikki kompyuter bir-biriga yeta oladimi? | ular orasida **yoʻl** bormi |
| Tarmoq nechta alohida boʻlakdan iborat? | **bogʻliqlik komponentlari** soni |
| Qaysi qurilma buzilsa, tarmoq boʻlinadi? | **ajratuvchi tugun** (kritik tugun) |
| Qaysi kabel uzilsa, tarmoq boʻlinadi? | **koʻprik qirra** |
| Markaziy qurilmaga nechta kabel ulanadi? | tugun **darajasi** |

Masalan, yulduz topologiyasida markaziy kommutator — kritik tugun, har bir kabel esa koʻprik qirra (uzilsa, bitta kompyuter ajraladi). Halqada kritik tugun yoʻq: bitta tugun buzilsa, qolganlari halqaning qolgan qismi orqali bogʻlangan qoladi. Toʻliq bogʻlangan tarmoqda kritik tugun ham, koʻprik qirra ham yoʻq.

## Kabellar va ulanish sxemalari

Oʻralgan juftlik kabeli RJ-45 ulagichiga ikki standart sxemadan biri boʻyicha ulanadi:

| Kontakt | T568A | T568B |
|---|---|---|
| 1 | oq-yashil | oq-toʻq sariq |
| 2 | yashil | toʻq sariq |
| 3 | oq-toʻq sariq | oq-yashil |
| 4 | koʻk | koʻk |
| 5 | oq-koʻk | oq-koʻk |
| 6 | toʻq sariq | yashil |
| 7 | oq-jigarrang | oq-jigarrang |
| 8 | jigarrang | jigarrang |

- **Toʻgʻri kabel** (straight-through) — ikki uchi bir xil sxemada (B–B). **Har xil** turdagi qurilmalarni ulaydi: kompyuter–kommutator, marshrutizator–kommutator.
- **Kross kabel** (crossover) — bir uchi A, ikkinchisi B. **Bir xil** turdagi qurilmalarni ulaydi: kompyuter–kompyuter, kommutator–kommutator, marshrutizator–kompyuter.
- Zamonaviy qurilmalarda **Auto-MDI/X** bor — ular kabel turini oʻzi aniqlaydi, lekin Packet Tracer da va imtihonda qoida soʻraladi.

## Amaliy ish: Packet Tracer da topologiyalar

1. **Yulduz**: bitta 2960 kommutator va 4 ta PC. Kompyuterlarga 192.168.1.1–192.168.1.4 manzillarini (maska 255.255.255.0) bering, PC1 dan `ping 192.168.1.4` ni bajaring.
2. **Kengaytirilgan yulduz (daraxt)**: ikkita kommutatorni kross kabel bilan ulang, har biriga 3 tadan PC qoʻshing. Ikki kommutator orasidagi kabelni oʻchirib, qaysi kompyuterlar aloqani yoʻqotishini kuzating.
3. **Halqa**: 4 ta kommutatorni halqa qilib ulang. Packet Tracer da bitta port toʻq sariq (bloklangan) boʻlib qoladi — bu **STP** (Spanning Tree Protocol) halqada kadrlar cheksiz aylanmasligi uchun bitta liniyani zaxiraga oʻtkazgani. Ishlayotgan liniyalardan birini uzing — bir necha soniyadan soʻng zaxira liniya ishga tushadi.
4. Har bir sxema uchun jadval tuzing: qurilmalar soni, kabellar soni, kritik tugunlar.

## Ishlangan misol 1: kabellar uzilganda boʻlaklar soni

Birinchi satrda tugunlar soni *n* va liniyalar soni *m*, keyin *m* ta liniya. Soʻng *q* va *q* ta uzilgan liniya ketma-ket keladi. Har bir uzilishdan keyin tarmoq nechta bogʻliqlik komponentiga boʻlinganini chiqaring.

```python
def komponentlar(n, qirralar):
    qoshni = [[] for _ in range(n + 1)]
    for u, v in qirralar:
        qoshni[u].append(v)
        qoshni[v].append(u)
    korilgan = [False] * (n + 1)
    soni = 0
    for boshi in range(1, n + 1):
        if korilgan[boshi]:
            continue
        soni += 1                               # yangi boʻlak topildi
        korilgan[boshi] = True
        stek = [boshi]
        while stek:
            x = stek.pop()
            for y in qoshni[x]:
                if not korilgan[y]:
                    korilgan[y] = True
                    stek.append(y)
    return soni


n, m = map(int, input().split())
qirralar = [tuple(map(int, input().split())) for _ in range(m)]
q = int(input())
for _ in range(q):
    u, v = map(int, input().split())
    qirralar = [e for e in qirralar if e != (u, v) and e != (v, u)]   # liniya uzildi
    print(komponentlar(n, qirralar))
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static int komponentlar(int n, List<int[]> qirralar) {
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int[] e : qirralar) {
            qoshni.get(e[0]).add(e[1]);
            qoshni.get(e[1]).add(e[0]);
        }
        boolean[] korilgan = new boolean[n + 1];
        int soni = 0;
        for (int boshi = 1; boshi <= n; boshi++) {
            if (korilgan[boshi]) continue;
            soni++;                                         // yangi boʻlak topildi
            korilgan[boshi] = true;
            ArrayDeque<Integer> stek = new ArrayDeque<>();
            stek.push(boshi);
            while (!stek.isEmpty()) {
                int x = stek.pop();
                for (int y : qoshni.get(x)) {
                    if (!korilgan[y]) {
                        korilgan[y] = true;
                        stek.push(y);
                    }
                }
            }
        }
        return soni;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<int[]> qirralar = new ArrayList<>();
        for (int i = 0; i < m; i++) qirralar.add(new int[] {in.nextInt(), in.nextInt()});
        int q = in.nextInt();
        for (int i = 0; i < q; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qirralar.removeIf(e -> (e[0] == u && e[1] == v) || (e[0] == v && e[1] == u));   // liniya uzildi
            System.out.println(komponentlar(n, qirralar));
        }
    }
}
```
```cpp
#include <iostream>
#include <utility>
#include <vector>
using namespace std;

int komponentlar(int n, const vector<pair<int, int>>& qirralar) {
    vector<vector<int>> qoshni(n + 1);
    for (auto& e : qirralar) {
        qoshni[e.first].push_back(e.second);
        qoshni[e.second].push_back(e.first);
    }
    vector<bool> korilgan(n + 1, false);
    int soni = 0;
    for (int boshi = 1; boshi <= n; boshi++) {
        if (korilgan[boshi]) continue;
        soni++;                                             // yangi boʻlak topildi
        korilgan[boshi] = true;
        vector<int> stek = {boshi};
        while (!stek.empty()) {
            int x = stek.back();
            stek.pop_back();
            for (int y : qoshni[x]) {
                if (!korilgan[y]) {
                    korilgan[y] = true;
                    stek.push_back(y);
                }
            }
        }
    }
    return soni;
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<pair<int, int>> qirralar(m);
    for (auto& e : qirralar) cin >> e.first >> e.second;
    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        int u, v;
        cin >> u >> v;
        vector<pair<int, int>> qolgan;
        for (auto& e : qirralar) {
            if (!((e.first == u && e.second == v) || (e.first == v && e.second == u))) qolgan.push_back(e);
        }
        qirralar = qolgan;                                  // liniya uzildi
        cout << komponentlar(n, qirralar) << endl;
    }
    return 0;
}
```

Kirish (1–2–3 halqa, 3–4 bogʻlovchi liniya, 4 atrofida 5 va 6 — yulduz):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
3
1 2
3 4
4 5
```

Chiqish:

```text
1
2
3
```

Halqadagi 1–2 liniyasi uzilganda tarmoq butunligicha qoladi (yoʻl 1–3–2 orqali). 3–4 liniyasi esa — koʻprik qirra: u uzilganda tarmoq ikkiga boʻlinadi. Yulduzdagi 4–5 liniyasi uzilsa, 5-kompyuter yolgʻiz qoladi.

## Ishlangan misol 2: kritik tugunlarni topish

Graf berilgan. Qaysi tugunlar **kritik**: ularni olib tashlasak, qolgan tugunlar endi bitta bogʻliq tarmoq boʻlmay qoladi? Har bir tugunni navbat bilan "oʻchirib", qolganlarini qidiruv bilan tekshiramiz (*n* marta qidiruv — kichik tarmoqlar uchun yetarli). Kritik tugunlarni oʻsish tartibida yoki ular boʻlmasa `yoʻq` chiqaring.

```python
def bogliqmi(n, qoshni, ochiq):
    tirik = [x for x in range(1, n + 1) if x != ochiq]
    korilgan = {tirik[0]}
    stek = [tirik[0]]
    while stek:
        x = stek.pop()
        for y in qoshni[x]:
            if y != ochiq and y not in korilgan:
                korilgan.add(y)
                stek.append(y)
    return len(korilgan) == len(tirik)        # hammasiga yetib bordikmi


n, m = map(int, input().split())
qoshni = [[] for _ in range(n + 1)]
for _ in range(m):
    u, v = map(int, input().split())
    qoshni[u].append(v)
    qoshni[v].append(u)
kritik = [str(v) for v in range(1, n + 1) if not bogliqmi(n, qoshni, v)]
print(" ".join(kritik) if kritik else "yoʻq")
```
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean bogliqmi(int n, List<List<Integer>> qoshni, int ochiq) {
        int boshi = ochiq == 1 ? 2 : 1;
        boolean[] korilgan = new boolean[n + 1];
        korilgan[boshi] = true;
        int soni = 1;
        ArrayDeque<Integer> stek = new ArrayDeque<>();
        stek.push(boshi);
        while (!stek.isEmpty()) {
            int x = stek.pop();
            for (int y : qoshni.get(x)) {
                if (y != ochiq && !korilgan[y]) {
                    korilgan[y] = true;
                    soni++;
                    stek.push(y);
                }
            }
        }
        return soni == n - 1;                     // hammasiga yetib bordikmi
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> qoshni = new ArrayList<>();
        for (int i = 0; i <= n; i++) qoshni.add(new ArrayList<>());
        for (int i = 0; i < m; i++) {
            int u = in.nextInt();
            int v = in.nextInt();
            qoshni.get(u).add(v);
            qoshni.get(v).add(u);
        }
        List<String> kritik = new ArrayList<>();
        for (int v = 1; v <= n; v++) {
            if (!bogliqmi(n, qoshni, v)) kritik.add(String.valueOf(v));
        }
        System.out.println(kritik.isEmpty() ? "yoʻq" : String.join(" ", kritik));
    }
}
```
```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

bool bogliqmi(int n, const vector<vector<int>>& qoshni, int ochiq) {
    int boshi = ochiq == 1 ? 2 : 1;
    vector<bool> korilgan(n + 1, false);
    korilgan[boshi] = true;
    int soni = 1;
    vector<int> stek = {boshi};
    while (!stek.empty()) {
        int x = stek.back();
        stek.pop_back();
        for (int y : qoshni[x]) {
            if (y != ochiq && !korilgan[y]) {
                korilgan[y] = true;
                soni++;
                stek.push_back(y);
            }
        }
    }
    return soni == n - 1;                         // hammasiga yetib bordikmi
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> qoshni(n + 1);
    for (int i = 0; i < m; i++) {
        int u, v;
        cin >> u >> v;
        qoshni[u].push_back(v);
        qoshni[v].push_back(u);
    }
    string kritik;
    for (int v = 1; v <= n; v++) {
        if (!bogliqmi(n, qoshni, v)) kritik += (kritik.empty() ? "" : " ") + to_string(v);
    }
    cout << (kritik.empty() ? "yoʻq" : kritik) << endl;
    return 0;
}
```

Kirish (birinchi misoldagi gibrid tarmoq, hech narsa uzilmagan):

```text
6 6
1 2
2 3
3 1
3 4
4 5
4 6
```

Chiqish:

```text
3 4
```

3-tugun halqani qolgan qismdan ajratadi, 4-tugun esa yulduz markazi. Ularni zaxira liniyalar bilan himoyalash kerak — masalan, 2–4 liniyasini qoʻshsak, 3-tugun kritik boʻlmay qoladi. Toʻliq bogʻlangan 4 tugunli tarmoq uchun javob `yoʻq`.

Diqqat: kodlar kamida 2 ta tugunli tarmoq uchun yozilgan (bitta tugunni oʻchirgach, kamida bittasi qolishi kerak).

:::warning
**Koʻp uchraydigan xatolar**

- **Fizik va mantiqiy topologiyani aralashtirish.** Packet Tracer dagi halqa fizik jihatdan halqa, lekin STP bitta liniyani bloklagani uchun mantiqan daraxt boʻlib ishlaydi.
- **Kross kabel oʻrniga toʻgʻri kabel ishlatish.** Auto-MDI/X boʻlmagan qurilmalarda ikki kommutator yoki ikki kompyuter toʻgʻri kabel bilan ulanmaydi.
- **Qirrani faqat bir yoʻnalishda saqlash.** Kabel ikki tomonlama — qoʻshnilar roʻyxatiga ham *u* → *v*, ham *v* → *u* yoziladi; uzishda ham ikkala yoʻnalish olib tashlanadi.
- **Kritik tugunni darajasi eng katta tugun deb oʻylash.** Daraja katta boʻlishi shart emas: 3-tugunning darajasi 3, 1-tugunniki 2 — lekin muhim boʻlgani tarmoqni boʻlishi.
:::

:::tip
Oʻz uyingiz yoki fakultetingiz tarmogʻini qogʻozga chizing (router, Wi-Fi nuqtalari, kommutatorlar, kompyuterlar). Uni grafga aylantirib, ikkinchi misol dasturiga bering: qaysi qurilma eng xavfli yagona nosozlik nuqtasi ekan?
:::

## Xulosa

- Har qanday topologiya — graf: tugunlar qurilmalar, qirralar aloqa liniyalari.
- Liniyalar uzilgandan keyingi bogʻliqlik — komponentlar soni bilan, yagona nosozlik nuqtalari — kritik tugunlar bilan tekshiriladi.
- T568A/T568B — RJ-45 ulanish sxemalari; toʻgʻri kabel turli qurilmalarni, kross kabel bir xil qurilmalarni ulaydi.
- Halqali fizik ulanishlarda STP bitta liniyani zaxiraga olib, mantiqiy daraxt hosil qiladi.

## Oʻzingizni tekshiring

:::check
Q: Yulduz topologiyasida qaysi tugun kritik?
A: Markaziy qurilma (kommutator yoki konsentrator) — u buzilsa, barcha kompyuterlar aloqani yoʻqotadi.
Q: Oddiy halqa topologiyasida bitta liniya uzilsa, tarmoq ikkiga boʻlinadimi?
A: Yoʻq, tugunlar halqaning qolgan qismi orqali bogʻlangan qoladi; ikkinchi uzilishdan keyin boʻlinadi.
Q: Kompyuterni kommutatorga qanday kabel bilan ulash kerak?
A: Toʻgʻri (straight-through) kabel bilan — ular har xil turdagi qurilmalar.
Q: T568A va T568B sxemalari qaysi kontaktlarda farq qiladi?
A: 1, 2 va 3, 6 kontaktlarda — yashil va toʻq sariq juftliklar oʻrin almashgan.
Q: Packet Tracer da 3 ta kommutator halqa qilib ulanganda bitta port nima uchun bloklanadi?
A: STP halqada kadrlar cheksiz aylanmasligi uchun bitta liniyani zaxiraga oʻtkazadi.
:::

:::link https://www.netacad.com/cisco-packet-tracer
Cisco Packet Tracer
Tarmoq simulyatorini yuklab olish va bepul kirish kurslari (ingliz tilida).
:::
