---
title: Foydalanishni boshqarish modellari. DAC, MAC, RBAC, ABAC, ACL va matritsalar
summary: Foydalanishni boshqarish tushunchalari — subyekt, obyekt, amal; tamoyillar — eng kam imtiyoz, bilish zarurati, vazifalarni ajratish, standart rad etish; foydalanish matritsasi, ACL va imkoniyatlar roʻyxati; diskretsion model (DAC) — Unix va NTFS huquqlari, troyan muammosi; mandatli model (MAC) — xavfsizlik belgilari, Bell–LaPadula va Biba modellari, SELinux; rollarga asoslangan model (RBAC) — rollar ierarxiyasi va cheklovlar; atributlarga asoslangan model (ABAC) — siyosatlar, XACML; modellarni solishtirish.
---

## Nima uchun bu mavzu muhim?

M10 da foydalanuvchi **kimligini** isbotlashni koʻrdik. Endi keyingi savol: autentifikatsiyadan oʻtgan foydalanuvchiga **nimaga ruxsat**? Talaba oʻz bahosini koʻrishi kerak, lekin oʻzgartira olmasligi; buxgalter toʻlov tayyorlashi, lekin oʻzi tasdiqlay olmasligi; maxfiy hujjat faqat tegishli darajadagi xodimlarga ochiq boʻlishi kerak. OWASP Top 10 roʻyxatida **Broken Access Control** — veb-ilovalardagi eng xavfli zaiflik sifatida birinchi oʻrinda turadi.

:::note
**Bu darsdan soʻng siz:**

- foydalanishni boshqarishning asosiy tamoyillarini ayta olasiz;
- foydalanish matritsasi, ACL va imkoniyatlar roʻyxatini tuza olasiz;
- DAC, MAC, RBAC va ABAC modellarini farqlay va solishtira olasiz;
- Bell–LaPadula va Biba qoidalarini qoʻllay olasiz;
- RBAC da soʻrovni tekshirish mantiqini dasturda koʻrsata olasiz.
:::

## Asosiy tushunchalar va tamoyillar

**Subyekt** (foydalanuvchi, jarayon) **obyekt** (fayl, jadval, printer) ustida **amal** (oʻqish, yozish, bajarish, oʻchirish) bajarishni soʻraydi; **foydalanishni boshqarish mexanizmi** (reference monitor) har bir soʻrovni siyosat boʻyicha tekshiradi. Reference monitor har doim chaqirilishi, chetlab oʻtib boʻlmasligi va tekshirsa boʻladigan darajada kichik boʻlishi kerak.

| Tamoyil | Mazmuni |
|---|---|
| **Eng kam imtiyoz** (least privilege) | faqat vazifa uchun zarur boʻlgan huquqlar, faqat zarur muddatga |
| **Bilish zarurati** (need to know) | maxfiylik darajasi yetarli boʻlsa ham, faqat ishga tegishli maʼlumot |
| **Vazifalarni ajratish** (separation of duties) | muhim amal ikki kishiga boʻlinadi: biri tayyorlaydi, boshqasi tasdiqlaydi |
| **Standart rad etish** (default deny) | aniq ruxsat berilmagan hamma narsa taqiqlangan |
| **Toʻliq vositachilik** (complete mediation) | har bir murojaat tekshiriladi, faqat birinchisi emas |

## Foydalanish matritsasi, ACL va imkoniyatlar

**Foydalanish matritsasi** (Lampson, 1971): satrlar — subyektlar, ustunlar — obyektlar, katakda — ruxsat etilgan amallar.

| | hisobot.docx | baza.db | printer |
|---|---|---|---|
| **Ali** | oʻqish, yozish, egasi | oʻqish | chop etish |
| **Vali** | oʻqish | — | chop etish |
| **zaxira jarayoni** | oʻqish | oʻqish | — |

Amalda matritsa juda katta va siyrak, shuning uchun ikki xil saqlanadi:

- **ACL** (Access Control List) — **ustun** boʻyicha: har bir obyektga "kim nima qila oladi" roʻyxati biriktiriladi. Fayl tizimlari (NTFS, Unix), tarmoq qurilmalari. Qulay: "bu faylga kimlar kira oladi?"; noqulay: "Ali nimalarga kira oladi?".
- **Imkoniyatlar roʻyxati** (capability list) — **satr** boʻyicha: har bir subyektda "qaysi obyektga qanday huquq" belgilari (tokenlar) boʻladi. Misollar: Kerberos chiptalari, OAuth tokenlari, ochiq fayl deskriptorlari. Huquqni boshqaga berish oson, lekin uni qaytarib olish qiyin.

## DAC — diskretsion model

**DAC** (Discretionary Access Control) da obyekt **egasi** kimga qanday huquq berishni oʻzi hal qiladi. Misollar: Unix huquqlari (`rwx` egasi, guruh, boshqalar uchun — A11 da), Windows NTFS ACL lari, Google Drive da "ulashish".

- Afzalligi: moslashuvchan, oddiy, foydalanuvchilarga qulay.
- Kamchiligi: markaziy nazorat yoʻq; xato yoki **troyan dastur** foydalanuvchi nomidan uning barcha huquqlari bilan ishlaydi — masalan, maxfiy faylni ochiq papkaga nusxalaydi va DAC buni toʻxtatmaydi.

## MAC — mandatli model

**MAC** (Mandatory Access Control) da qarorni egasi emas, **tizim siyosati** qabul qiladi: subyektlarga **ruxsatnoma darajasi** (clearance), obyektlarga **maxfiylik belgisi** (classification) beriladi, masalan: Ochiq (U) < Xizmat uchun (C) < Maxfiy (S) < Oʻta maxfiy (TS). Foydalanuvchi oʻz hujjati belgisini ham oʻzgartira olmaydi.

**Bell–LaPadula** (BLP, 1973) — **maxfiylikni** himoyalash modeli:

- **Simple security property** — "yuqoriga oʻqish yoʻq" (no read up): subyekt faqat oʻz darajasidagi yoki pastroq obyektni oʻqiydi.
- **\*-property** — "pastga yozish yoʻq" (no write down): subyekt faqat oʻz darajasidagi yoki yuqoriroq obyektga yozadi — bu maxfiy maʼlumotning past darajaga "oqib tushishi"ni (troyan orqali ham) toʻxtatadi.

**Biba** (1977) — **yaxlitlikni** himoyalash modeli, qoidalari teskari: "pastga oʻqish yoʻq" (ishonchsiz maʼlumot bilan ifloslanmaslik) va "yuqoriga yozish yoʻq" (past darajadagi jarayon muhim maʼlumotni buzmasligi). Windows dagi **Mandatory Integrity Control** (brauzer "past" yaxlitlik darajasida ishlaydi) — Biba gʻoyasi.

MAC harbiy va davlat tizimlarida, shuningdek **SELinux** va **AppArmor** (Linux, Android) da qoʻllanadi: hatto `root` huquqli buzilgan jarayon ham siyosatdan tashqariga chiqa olmaydi.

## RBAC — rollarga asoslangan model

**RBAC** (Role-Based Access Control, NIST standarti) da ruxsatlar foydalanuvchilarga emas, **rollarga** beriladi; foydalanuvchilar rollarga biriktiriladi: foydalanuvchi → rol → ruxsat. Xodim lavozimi oʻzgarsa, faqat rolini almashtirish yetarli.

- **Rollar ierarxiyasi**: "katta oʻqituvchi" rol "oʻqituvchi" ning barcha ruxsatlarini meros oladi.
- **Cheklovlar**: statik vazifalarni ajratish (bitta odam "toʻlov tayyorlovchi" va "toʻlov tasdiqlovchi" rollariga birga ega boʻlolmaydi), dinamik (bitta seansda ikkalasini faollashtira olmaydi).
- Qoʻllanishi: korporativ tizimlar, maʼlumotlar bazalari (`GRANT ... TO role`), bulut (AWS IAM rollari), Active Directory guruhlari, Kubernetes RBAC.
- Kamchiligi: "rollar portlashi" — har bir istisno uchun yangi rol yaratilib, rollar soni foydalanuvchilardan oshib ketadi.

## ABAC — atributlarga asoslangan model

**ABAC** (Attribute-Based Access Control) da qaror **atributlar** boʻyicha qoidalar bilan qabul qilinadi: subyekt (lavozim, boʻlim, ruxsatnoma), obyekt (turi, egasi, maxfiylik), amal va **muhit** (vaqt, joylashuv, qurilma, xavf darajasi). Masalan: "shifokor bemorning kartasini **oʻqiy oladi**, agar bemor shu boʻlimda boʻlsa **va** shifokor hozir navbatchilikda boʻlsa **va** ulanish shifoxona tarmogʻidan boʻlsa". Siyosatlar XACML tilida yoki bulutdagi siyosat tillarida (AWS IAM shartlari) yoziladi. ABAC eng moslashuvchan, lekin siyosatlarni loyihalash va tekshirish eng qiyin. **Nol ishonch** (Zero Trust) arxitekturasi aynan har bir soʻrovni kontekst boʻyicha qayta baholashga asoslanadi.

| Model | Kim hal qiladi | Kuchli tomoni | Zaif tomoni | Qayerda |
|---|---|---|---|---|
| DAC | obyekt egasi | oddiy, moslashuvchan | markaziy nazorat yoʻq, troyan | shaxsiy kompyuterlar, fayl almashish |
| MAC | tizim siyosati (belgilar) | qatʼiy, sizib chiqishni toʻxtatadi | qattiq, boshqarish qiyin | harbiy, davlat, SELinux |
| RBAC | administrator (rollar) | tashkilot tuzilishiga mos, audit oson | rollar portlashi | korxonalar, MBBT, bulut |
| ABAC | atributlar boʻyicha siyosat | kontekstli, nozik nazorat | murakkab, sekinroq | bulut, nol ishonch tizimlari |

## Ishlangan misol 1: Bell–LaPadula modeli

Darajalar: U < C < S < TS. Kirish: *n* va *n* ta subyekt (nom daraja), *m* va *m* ta obyekt (nom daraja), keyin soʻrovlar (subyekt amal obyekt, amal — `read` yoki `write`). Har bir soʻrov uchun BLP qoidalari boʻyicha qarorni chiqaring.

```python
import sys

DARAJA = {"U": 0, "C": 1, "S": 2, "TS": 3}

t = sys.stdin.read().split()
i = 0
subyekt, obyekt = {}, {}
for jadval in (subyekt, obyekt):
    k = int(t[i])
    i += 1
    for _ in range(k):
        jadval[t[i]] = DARAJA[t[i + 1]]
        i += 2
while i + 2 < len(t):
    s, amal, o = t[i], t[i + 1], t[i + 2]
    i += 3
    if amal == "read":
        ruxsat, sabab = subyekt[s] >= obyekt[o], "no read up"      # oddiy xavfsizlik xossasi
    else:
        ruxsat, sabab = subyekt[s] <= obyekt[o], "no write down"   # *-xossa
    print(f"{s} {amal} {o}: " + ("ruxsat" if ruxsat else f"rad ({sabab})"))
```
```java
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final Map<String, Integer> DARAJA = Map.of("U", 0, "C", 1, "S", 2, "TS", 3);

    static Map<String, Integer> oqi(Scanner in) {
        Map<String, Integer> jadval = new HashMap<>();
        int k = in.nextInt();
        for (int j = 0; j < k; j++) {
            String nom = in.next();
            jadval.put(nom, DARAJA.get(in.next()));
        }
        return jadval;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Integer> subyekt = oqi(in);
        Map<String, Integer> obyekt = oqi(in);
        while (in.hasNext()) {
            String s = in.next(), amal = in.next(), o = in.next();
            boolean ruxsat;
            String sabab;
            if (amal.equals("read")) {
                ruxsat = subyekt.get(s) >= obyekt.get(o);                // oddiy xavfsizlik xossasi
                sabab = "no read up";
            } else {
                ruxsat = subyekt.get(s) <= obyekt.get(o);                // *-xossa
                sabab = "no write down";
            }
            System.out.println(s + " " + amal + " " + o + ": " + (ruxsat ? "ruxsat" : "rad (" + sabab + ")"));
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <string>
using namespace std;

map<string, int> DARAJA = {{"U", 0}, {"C", 1}, {"S", 2}, {"TS", 3}};

map<string, int> oqi() {
    map<string, int> jadval;
    int k;
    cin >> k;
    for (int j = 0; j < k; j++) {
        string nom, d;
        cin >> nom >> d;
        jadval[nom] = DARAJA[d];
    }
    return jadval;
}

int main() {
    map<string, int> subyekt = oqi();
    map<string, int> obyekt = oqi();
    string s, amal, o;
    while (cin >> s >> amal >> o) {
        bool ruxsat;
        string sabab;
        if (amal == "read") {
            ruxsat = subyekt[s] >= obyekt[o];                            // oddiy xavfsizlik xossasi
            sabab = "no read up";
        } else {
            ruxsat = subyekt[s] <= obyekt[o];                            // *-xossa
            sabab = "no write down";
        }
        cout << s << " " << amal << " " << o << ": " << (ruxsat ? string("ruxsat") : "rad (" + sabab + ")") << endl;
    }
    return 0;
}
```

Kirish:

```text
3
ali S
vali C
olim TS
4
elon U
hisobot C
reja S
kod TS
vali read reja
ali read hisobot
ali write hisobot
vali write reja
olim read kod
olim write elon
```

Chiqish:

```text
vali read reja: rad (no read up)
ali read hisobot: ruxsat
ali write hisobot: rad (no write down)
vali write reja: ruxsat
olim read kod: ruxsat
olim write elon: rad (no write down)
```

Eʼtibor bering: "Maxfiy" darajadagi ali "Xizmat uchun" hujjatini oʻqiy oladi, lekin unga **yoza olmaydi** — aks holda u (yoki uning nomidan ishlayotgan troyan) maxfiy maʼlumotni past darajaga koʻchirishi mumkin edi. vali esa "Maxfiy" rejaga yoza oladi ("koʻr yozish"), lekin uni oʻqiy olmaydi. Amalda yuqori darajadagi foydalanuvchi past darajadagi hujjat yozishi uchun past darajali seans bilan kiradi.

## Ishlangan misol 2: RBAC soʻrovini tekshirish

Kirish: *r* va *r* ta satr "rol ruxsat1 ruxsat2 …"; *u* va *u* ta satr "foydalanuvchi rol1 rol2 …"; keyin soʻrovlar "foydalanuvchi ruxsat". Foydalanuvchining rollarini berilgan tartibda koʻrib, ruxsat bergan birinchi rolni chiqaring; aks holda — rad (standart rad etish).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
i = 0
rollar = {}
r = int(satrlar[i][0])
i += 1
for _ in range(r):
    rollar[satrlar[i][0]] = set(satrlar[i][1:])     # rol -> ruxsatlar
    i += 1
foydalanuvchilar = {}
u = int(satrlar[i][0])
i += 1
for _ in range(u):
    foydalanuvchilar[satrlar[i][0]] = satrlar[i][1:]  # foydalanuvchi -> rollar
    i += 1
for kim, nima in satrlar[i:]:
    if kim not in foydalanuvchilar:
        print(f"{kim} {nima}: rad (foydalanuvchi topilmadi)")
        continue
    rol = next((x for x in foydalanuvchilar[kim] if nima in rollar.get(x, set())), None)
    print(f"{kim} {nima}: " + (f"ruxsat ({rol})" if rol else "rad"))   # standart rad etish
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String[]> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) satrlar.add(s.split("\\s+"));
        }
        int i = 0;
        Map<String, Set<String>> rollar = new HashMap<>();
        int r = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < r; j++, i++) {
            String[] q = satrlar.get(i);
            rollar.put(q[0], new HashSet<>(Arrays.asList(q).subList(1, q.length)));   // rol -> ruxsatlar
        }
        Map<String, List<String>> foydalanuvchilar = new HashMap<>();
        int u = Integer.parseInt(satrlar.get(i++)[0]);
        for (int j = 0; j < u; j++, i++) {
            String[] q = satrlar.get(i);
            foydalanuvchilar.put(q[0], Arrays.asList(q).subList(1, q.length));       // foydalanuvchi -> rollar
        }
        for (; i < satrlar.size(); i++) {
            String kim = satrlar.get(i)[0], nima = satrlar.get(i)[1];
            if (!foydalanuvchilar.containsKey(kim)) {
                System.out.println(kim + " " + nima + ": rad (foydalanuvchi topilmadi)");
                continue;
            }
            String topildi = null;
            for (String rol : foydalanuvchilar.get(kim)) {
                if (rollar.getOrDefault(rol, Set.of()).contains(nima)) {
                    topildi = rol;
                    break;
                }
            }
            System.out.println(kim + " " + nima + ": " + (topildi != null ? "ruxsat (" + topildi + ")" : "rad"));   // standart rad etish
        }
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    vector<vector<string>> satrlar;
    string s;
    while (getline(cin, s)) {
        istringstream ss(s);
        vector<string> q;
        string w;
        while (ss >> w) q.push_back(w);
        if (!q.empty()) satrlar.push_back(q);
    }
    size_t i = 0;
    map<string, set<string>> rollar;
    int r = stoi(satrlar[i++][0]);
    for (int j = 0; j < r; j++, i++) {
        rollar[satrlar[i][0]] = set<string>(satrlar[i].begin() + 1, satrlar[i].end());   // rol -> ruxsatlar
    }
    map<string, vector<string>> foydalanuvchilar;
    int u = stoi(satrlar[i++][0]);
    for (int j = 0; j < u; j++, i++) {
        foydalanuvchilar[satrlar[i][0]] = vector<string>(satrlar[i].begin() + 1, satrlar[i].end());   // foydalanuvchi -> rollar
    }
    for (; i < satrlar.size(); i++) {
        const string& kim = satrlar[i][0];
        const string& nima = satrlar[i][1];
        if (!foydalanuvchilar.count(kim)) {
            cout << kim << " " << nima << ": rad (foydalanuvchi topilmadi)" << endl;
            continue;
        }
        string topildi;
        for (const string& rol : foydalanuvchilar[kim]) {
            if (rollar.count(rol) && rollar[rol].count(nima)) {
                topildi = rol;
                break;
            }
        }
        cout << kim << " " << nima << ": " << (!topildi.empty() ? "ruxsat (" + topildi + ")" : string("rad")) << endl;   // standart rad etish
    }
    return 0;
}
```

Kirish:

```text
4
talaba baho.korish jadval.korish
oqituvchi baho.korish baho.qoyish jadval.korish
dekan hisobot.korish baho.korish
admin foydalanuvchi.yaratish
4
ali talaba
vali oqituvchi
sardor oqituvchi dekan
olim admin
ali baho.qoyish
vali baho.qoyish
sardor hisobot.korish
olim baho.korish
ali jadval.korish
kamol baho.korish
```

Chiqish:

```text
ali baho.qoyish: rad
vali baho.qoyish: ruxsat (oqituvchi)
sardor hisobot.korish: ruxsat (dekan)
olim baho.korish: rad
ali jadval.korish: ruxsat (talaba)
kamol baho.korish: rad (foydalanuvchi topilmadi)
```

Tizim administratori olim foydalanuvchilarni yarata oladi, lekin baholarni koʻrmaydi — bu **vazifalarni ajratish** va **bilish zarurati**: texnik huquq maʼlumotga kirish huquqi degani emas. sardor ikki rolga ega va ikkalasining ruxsatlaridan foydalanadi. Roʻyxatda yoʻq har qanday soʻrov rad etiladi — **standart rad etish**. Haqiqiy tizimlarda bunday tekshiruv har bir API soʻrovida **serverda** bajariladi; faqat tugmani yashirish (frontendda) himoya emas.

:::warning
**Koʻp uchraydigan xatolar**

- **Tekshiruvni faqat interfeysda qilish.** Tugma yashirilgan boʻlsa ham, soʻrovni toʻgʻridan-toʻgʻri yuborish mumkin; tekshiruv serverda boʻlishi shart.
- **Hammaga administrator huquqi.** "Ishlamay qolmasin" deb berilgan ortiqcha huquqlar — buzilish oqibatini koʻpaytiradi.
- **Imtiyozlarning toʻplanishi** (privilege creep). Xodim boʻlimdan boʻlimga oʻtganda eski huquqlari qoladi; huquqlarni muntazam qayta koʻrib chiqing.
- **Ishdan ketgan xodim hisobi.** Hisob darhol oʻchirilishi yoki bloklanishi kerak.
- **Obyekt identifikatorini tekshirmaslik** (IDOR). `/baho?id=125` ni `id=126` ga oʻzgartirib, birovning maʼlumotini olish — eng koʻp uchraydigan veb-zaiflik.
:::

:::tip
Windows da fayl → Properties → Security yorligʻida NTFS ACL ni koʻring; buyruq satrida `icacls fayl.txt`. Linux da `getfacl fayl` kengaytirilgan ACL ni, `ls -Z` esa SELinux belgisini koʻrsatadi.
:::

## Xulosa

- Foydalanishni boshqarish: subyekt, obyekt, amal; tamoyillar — eng kam imtiyoz, bilish zarurati, vazifalarni ajratish, standart rad etish.
- Foydalanish matritsasi ustun boʻyicha — ACL, satr boʻyicha — imkoniyatlar roʻyxati sifatida saqlanadi.
- DAC — egasi hal qiladi; MAC — tizim siyosati va belgilar (BLP: no read up, no write down; Biba — teskari, yaxlitlik uchun).
- RBAC — ruxsatlar rollar orqali, ierarxiya va cheklovlar bilan; ABAC — atributlar va muhit boʻyicha siyosat.
- Tekshiruv har bir soʻrovda, serverda, standart rad etish bilan bajariladi.

## Oʻzingizni tekshiring

:::check
Q: ACL va imkoniyatlar roʻyxatining farqi nima?
A: ACL obyektga biriktiriladi (kim nima qila oladi), imkoniyatlar roʻyxati subyektga (qaysi obyektga qanday huquq).
Q: Bell–LaPadula modelida "Maxfiy" darajadagi foydalanuvchi "Ochiq" hujjatga yoza oladimi?
A: Yoʻq, *-xossa (no write down) pastga yozishni taqiqlaydi.
Q: DAC ning troyan dasturga nisbatan zaifligi nimada?
A: Troyan foydalanuvchi nomidan uning barcha huquqlari bilan ishlaydi va maʼlumotni istalgan joyga nusxalashi mumkin.
Q: RBAC da vazifalarni ajratish cheklovi nimani taqiqlaydi?
A: Bitta foydalanuvchining bir-biriga zid rollarga (masalan, toʻlov tayyorlovchi va tasdiqlovchi) birga ega boʻlishini.
Q: ABAC qarorida qanday atributlar ishtirok etadi?
A: Subyekt, obyekt, amal va muhit (vaqt, joylashuv, qurilma) atributlari.
:::

:::link https://csrc.nist.gov/pubs/sp/800/162/upd2/final
NIST SP 800-162: Guide to Attribute Based Access Control (ABAC)
ABAC tushunchalari va boshqa modellar bilan solishtirish (ingliz tilida).
:::
