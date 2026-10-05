---
title: Kiberxavfsizlik arxitekturasi va siyosati. Xavfsizlik strategiyasi, siyosati, iyerarxiyasi va turlari
summary: Xavfsizlik arxitekturasi tamoyillari — chuqur himoya, eng kam imtiyoz, vazifalarni ajratish, sukut boʻyicha rad etish, nol ishonch (Zero Trust); xavfsizlik strategiyasi; hujjatlar iyerarxiyasi — siyosat, standart, protsedura, yoʻriqnoma; siyosat turlari (umumiy, muammoga yoʻnaltirilgan, tizimga xos) va namunalar (AUP, parol, kirish, zaxira, hodisaga javob); ISO/IEC 27001 va ISMS; huquqlar auditi.
---

## Nima uchun bu mavzu muhim?

Eng qimmat brandmauer ham, agar xodimlar parollarni stikerga yozsa yoki har kim administrator huquqiga ega boʻlsa, tashkilotni himoyalamaydi. Xavfsizlik **tizimli** quriladi: avval nima va qanday himoyalanishi kerakligi rahbariyat darajasida belgilanadi (strategiya va siyosat), soʻng bu qoidalar texnik yechimlar va kundalik amallarga aylantiriladi. Bu maʼruzada xavfsizlik "binosining" chizmasi — arxitektura va siyosatlar bilan tanishamiz.

:::note
**Bu darsdan soʻng siz:**

- xavfsizlik arxitekturasining asosiy tamoyillarini tushuntira olasiz;
- siyosat, standart, protsedura va yoʻriqnomani farqlay olasiz;
- xavfsizlik siyosati turlarini va ularning tarkibini ayta olasiz;
- ISO/IEC 27001 va ISMS nima ekanini tushuntira olasiz;
- eng kam imtiyoz tamoyili boʻyicha foydalanuvchi huquqlarini tekshira olasiz.
:::

## Arxitektura tamoyillari

| Tamoyil | Maʼnosi | Misol |
|---|---|---|
| **Chuqur himoya** | bir nechta mustaqil himoya qatlami | brandmauer + segmentlash + antivirus + shifrlash |
| **Eng kam imtiyoz** (least privilege) | har kimga faqat ishi uchun zarur huquq | buxgalter faqat moliya papkasiga kiradi |
| **Vazifalarni ajratish** (separation of duties) | muhim amal bir nechta shaxs ishtirokini talab qiladi | toʻlovni bir kishi kiritadi, boshqasi tasdiqlaydi |
| **Sukut boʻyicha rad etish** (default deny) | aniq ruxsat etilmagan hamma narsa taqiqlangan | brandmauerning oxirgi qoidasi — "hammasini taqiqlash" |
| **Xavfsiz sukut** (secure by default) | tizim xavfsiz sozlamalar bilan yetkaziladi | standart parolni birinchi kirishda almashtirish majburiy |
| **Oddiylik** | murakkab tizimda xato koʻp | keraksiz xizmatlarni oʻchirish |
| **Nol ishonch** (Zero Trust) | tarmoq ichidagilarga ham avtomatik ishonilmaydi, har bir soʻrov tekshiriladi | har bir ilovaga kirishda MFA va qurilma holatini tekshirish |

## Xavfsizlik strategiyasi

**Strategiya** — tashkilotning uzoq muddatli xavfsizlik maqsadlari va ularga erishish yoʻllari: qaysi aktivlar eng muhim, qanday risk darajasi maqbul, qanday resurslar ajratiladi, qanday koʻrsatkichlar bilan oʻlchanadi. Strategiya biznes maqsadlariga moslashtiriladi — xavfsizlik oʻzi uchun emas, tashkilot faoliyati uchun.

## Hujjatlar iyerarxiyasi

| Daraja | Hujjat | Mazmuni | Misol |
|---|---|---|---|
| 1 | **Siyosat** (policy) | nima qilinishi va nima uchun; rahbariyat tasdiqlaydi; kam oʻzgaradi | "Barcha maxfiy maʼlumotlar shifrlanishi kerak" |
| 2 | **Standart** (standard) | majburiy aniq talablar | "Shifrlash — AES-256, kalitlar 1 yilda almashtiriladi" |
| 3 | **Protsedura** (procedure) | qadamma-qadam qanday bajarish | "Noutbukda BitLocker ni yoqish: 1) … 2) …" |
| 4 | **Yoʻriqnoma** (guideline) | tavsiyalar, majburiy emas | "Parol oʻrniga parol iborasidan foydalanish tavsiya etiladi" |
| — | **Asos** (baseline) | tizimlarning minimal xavfsiz sozlamalari | "Windows Server uchun CIS Benchmark" |

## Siyosat turlari

- **Umumiy (dasturiy) siyosat** — tashkilotning xavfsizlikka umumiy yondashuvi, rollar va masʼuliyat; rahbar imzolaydi.
- **Muammoga yoʻnaltirilgan siyosatlar** — muayyan masala boʻyicha: Internet va elektron pochtadan foydalanish, mobil qurilmalar (BYOD), masofaviy ish.
- **Tizimga xos siyosatlar** — muayyan tizim uchun: "1C buxgalteriya tizimiga kirish qoidalari".

Namunaviy siyosatlar:

| Siyosat | Asosiy qoidalar |
|---|---|
| **AUP** (qabul qilinadigan foydalanish) | tashkilot resurslaridan qanday foydalanish mumkin va mumkin emas |
| **Parol siyosati** | uzunlik, murakkablik, almashtirish, MFA, bloklash |
| **Kirishni boshqarish** | huquq berish va olib qoʻyish tartibi, muntazam koʻrib chiqish |
| **Maʼlumotlarni tasniflash** | ochiq, ichki, maxfiy, qatʼiy maxfiy — va har biri uchun himoya talablari |
| **Zaxira nusxa** | nimani, qanchalik tez-tez, qayerda saqlash; tiklashni sinash |
| **Hodisaga javob** | kim, qanday tartibda va qancha vaqtda harakat qiladi |

Yaxshi siyosat: aniq, qisqa, bajarib boʻladigan, masʼul shaxsi va qayta koʻrib chiqish muddati belgilangan, xodimlarga tanishtirilgan (imzo bilan) va buzilishi uchun javobgarlik koʻrsatilgan.

## ISO/IEC 27001 va ISMS

**ISMS** (Information Security Management System) — axborot xavfsizligini boshqarish tizimi: siyosatlar, jarayonlar, rollar va nazorat vositalarining yagona tizimi. **ISO/IEC 27001** — ISMS ga talablar standarti; tashkilot unga muvofiqlik boʻyicha sertifikat olishi mumkin. Uning ilovasida (Annex A) 93 ta nazorat vositasi 4 guruhga boʻlingan: tashkiliy, odamlarga oid, fizik va texnologik. ISMS **PDCA** sikli boʻyicha ishlaydi: Plan (rejalash) → Do (bajarish) → Check (tekshirish) → Act (takomillashtirish).

## Ishlangan misol: eng kam imtiyoz auditi

Siyosatda har bir rol uchun zarur huquqlar belgilangan. Audit har bir foydalanuvchining haqiqiy huquqlarini rol talablari bilan solishtiradi. Birinchi satrda rollar soni, keyin har bir rol: nomi va vergul bilan ajratilgan huquqlari. Soʻng foydalanuvchilar soni va har biri: nomi, roli, haqiqiy huquqlari. Har bir foydalanuvchi uchun ortiqcha (rol talab qilmaydigan) va yetishmayotgan huquqlarni alifbo tartibida chiqaring.

```python
rollar = {}
for _ in range(int(input())):
    nom, huquqlar = input().split()
    rollar[nom] = set(huquqlar.split(","))
for _ in range(int(input())):
    user, rol, huquqlar = input().split()
    bor = set(huquqlar.split(","))
    kerak = rollar.get(rol, set())
    ortiqcha = sorted(bor - kerak)                # eng kam imtiyoz buzilishi
    yetishmaydi = sorted(kerak - bor)
    if not ortiqcha and not yetishmaydi:
        print(user + ": mos")
        continue
    qismlar = []
    if ortiqcha:
        qismlar.append("ortiqcha " + ",".join(ortiqcha))
    if yetishmaydi:
        qismlar.append("yetishmaydi " + ",".join(yetishmaydi))
    print(user + ": " + "; ".join(qismlar))
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, TreeSet<String>> rollar = new HashMap<>();
        int r = in.nextInt();
        for (int i = 0; i < r; i++) {
            String nom = in.next();
            rollar.put(nom, new TreeSet<>(Arrays.asList(in.next().split(","))));
        }
        int u = in.nextInt();
        for (int i = 0; i < u; i++) {
            String user = in.next();
            String rol = in.next();
            TreeSet<String> bor = new TreeSet<>(Arrays.asList(in.next().split(",")));
            TreeSet<String> kerak = rollar.getOrDefault(rol, new TreeSet<>());
            TreeSet<String> ortiqcha = new TreeSet<>(bor);
            ortiqcha.removeAll(kerak);                              // eng kam imtiyoz buzilishi
            TreeSet<String> yetishmaydi = new TreeSet<>(kerak);
            yetishmaydi.removeAll(bor);
            if (ortiqcha.isEmpty() && yetishmaydi.isEmpty()) {
                System.out.println(user + ": mos");
                continue;
            }
            List<String> qismlar = new ArrayList<>();
            if (!ortiqcha.isEmpty()) qismlar.add("ortiqcha " + String.join(",", ortiqcha));
            if (!yetishmaydi.isEmpty()) qismlar.add("yetishmaydi " + String.join(",", yetishmaydi));
            System.out.println(user + ": " + String.join("; ", qismlar));
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <iterator>
#include <map>
#include <set>
#include <sstream>
#include <string>
using namespace std;

set<string> ajrat(const string& s) {
    set<string> natija;
    stringstream ss(s);
    string h;
    while (getline(ss, h, ',')) natija.insert(h);
    return natija;
}

string birlashtir(const set<string>& s) {
    string natija;
    for (auto& h : s) natija += (natija.empty() ? "" : ",") + h;
    return natija;
}

int main() {
    map<string, set<string>> rollar;
    int r;
    cin >> r;
    for (int i = 0; i < r; i++) {
        string nom, huquqlar;
        cin >> nom >> huquqlar;
        rollar[nom] = ajrat(huquqlar);
    }
    int u;
    cin >> u;
    for (int i = 0; i < u; i++) {
        string user, rol, huquqlar;
        cin >> user >> rol >> huquqlar;
        set<string> bor = ajrat(huquqlar), kerak = rollar[rol], ortiqcha, yetishmaydi;
        set_difference(bor.begin(), bor.end(), kerak.begin(), kerak.end(), inserter(ortiqcha, ortiqcha.end()));   // eng kam imtiyoz buzilishi
        set_difference(kerak.begin(), kerak.end(), bor.begin(), bor.end(), inserter(yetishmaydi, yetishmaydi.end()));
        if (ortiqcha.empty() && yetishmaydi.empty()) {
            cout << user << ": mos" << endl;
            continue;
        }
        string qismlar;
        if (!ortiqcha.empty()) qismlar += "ortiqcha " + birlashtir(ortiqcha);
        if (!yetishmaydi.empty()) qismlar += (qismlar.empty() ? "" : "; ") + string("yetishmaydi ") + birlashtir(yetishmaydi);
        cout << user << ": " << qismlar << endl;
    }
    return 0;
}
```

Kirish:

```text
3
buxgalter finance_read,finance_write
oqituvchi grades_read,grades_write,lms
talaba lms,grades_read
4
dilnoza buxgalter finance_read,finance_write
bobur oqituvchi grades_read,grades_write,lms,finance_read
aziz talaba lms,grades_read,grades_write
sardor oqituvchi lms
```

Chiqish:

```text
dilnoza: mos
bobur: ortiqcha finance_read
aziz: ortiqcha grades_write
sardor: yetishmaydi grades_read,grades_write
```

Eng xavfli topilma — `aziz`: talaba baholarni **oʻzgartirish** huquqiga ega. Bunday "huquq toʻplanishi" odatda xodim boʻlim almashtirganda eski huquqlari olib qoʻyilmaganidan kelib chiqadi; shuning uchun kirish siyosati huquqlarni kamida chorakda bir marta koʻrib chiqishni talab qiladi.

:::warning
**Koʻp uchraydigan xatolar**

- **Siyosatni internetdan koʻchirib olish.** Siyosat tashkilotning haqiqiy jarayonlari va risklariga mos boʻlishi kerak, aks holda uni hech kim bajarmaydi.
- **Siyosat va protsedurani aralashtirish.** Siyosat — "nima va nima uchun", protsedura — "qanday qilib".
- **Xodimlarni siyosat bilan tanishtirmaslik.** Bilinmagan qoidani bajarib boʻlmaydi; tanishtirish imzo bilan qayd etiladi.
- **Huquqlarni faqat qoʻshish, hech qachon olib qoʻymaslik.** Vaqt oʻtishi bilan har kim "hamma narsaga" kira oladigan boʻlib qoladi.
- **Nol ishonchni bitta mahsulot deb tushunish.** Zero Trust — mahsulot emas, arxitektura yondashuvi.
:::

:::tip
Universitetingizdagi kompyuter sinfi uchun bir sahifalik AUP loyihasini yozing: ruxsat etilgan foydalanish, taqiqlar (begona dasturlar oʻrnatish, boshqalar hisobidan foydalanish), shaxsiy maʼlumotlar, qoidabuzarlik uchun javobgarlik va masʼul shaxs.
:::

## Xulosa

- Arxitektura tamoyillari: chuqur himoya, eng kam imtiyoz, vazifalarni ajratish, sukut boʻyicha rad etish, xavfsiz sukut, oddiylik, nol ishonch.
- Hujjatlar iyerarxiyasi: siyosat → standart → protsedura → yoʻriqnoma; asos — minimal sozlamalar.
- Siyosat turlari: umumiy, muammoga yoʻnaltirilgan, tizimga xos; namunalar — AUP, parol, kirish, tasniflash, zaxira, hodisaga javob.
- ISO/IEC 27001 — ISMS ga talablar; PDCA sikli.
- Huquqlarni muntazam audit qilish eng kam imtiyoz buzilishlarini aniqlaydi.

## Oʻzingizni tekshiring

:::check
Q: Xavfsizlik hujjatlari iyerarxiyasidagi eng yuqori, umumiy darajadagi hujjat qaysi?
A: Siyosat (policy).
Q: "Xodimga faqat ishini bajarish uchun zarur huquqlar beriladi" — bu qaysi tamoyil?
A: Eng kam imtiyoz (least privilege).
Q: Protsedura siyosatdan nimasi bilan farq qiladi?
A: Siyosat nima va nima uchun qilinishini belgilaydi, protsedura esa uni qadamma-qadam qanday bajarishni.
Q: Vazifalarni ajratish tamoyiliga misol keltiring.
A: Toʻlovni bir xodim kiritadi, boshqasi tasdiqlaydi — bitta odam yolgʻiz firibgarlik qila olmaydi.
Q: ISO/IEC 27001 nimani belgilaydi?
A: Axborot xavfsizligini boshqarish tizimiga (ISMS) qoʻyiladigan talablarni.
:::

:::link https://www.iso.org/standard/27001
ISO/IEC 27001
Axborot xavfsizligini boshqarish tizimlari standartining rasmiy sahifasi (ingliz tilida).
:::
