---
title: Axborotni himoya qilish uchun kriptografik usullardan foydalanish
summary: Mustaqil ish — kriptografiyani amalda qoʻllash; ochiq kalitlar infratuzilmasi (PKI) — sertifikatlash markazlari, sertifikat zanjiri, sertifikat hayot sikli (berish, yangilash, bekor qilish, CRL va OCSP); Oʻzbekistonda ERI dan foydalanish (E-IMZO, kalit tashuvchilar, mobil imzo); TLS ulanishining soddalashtirilgan bosqichlari; parollarni xavfsiz saqlash — "tuz" va kalitni choʻzish (bcrypt, scrypt, Argon2) va buni koʻrsatuvchi oʻquv dasturi; disk, fayl va xabarlarni shifrlash; kalitlarni boshqarish qoidalari; topshiriq.
---

## Mustaqil ishning maqsadi

S14 seminarida kriptografiya asoslarini — simmetrik va asimmetrik shifrlash, xesh, elektron raqamli imzo — kichik misollarda koʻrdik. Bu mustaqil ishda kriptografiya **kundalik ish va tizimlarda qanday qoʻllanishini** oʻrganamiz: sertifikatlar qanday ishonch zanjirini hosil qiladi, ERI bilan qanday ishlanadi va parollar qanday xavfsiz saqlanadi.

:::note
**Bu mustaqil ishdan soʻng siz:**

- PKI va sertifikat zanjirini tushuntira olasiz;
- sertifikat hayot sikli bosqichlarini ayta olasiz;
- ERI bilan hujjat imzolash jarayonini va xavfsizlik qoidalarini bilasiz;
- parollarni "tuz" va kalitni choʻzish bilan saqlash nima uchun kerakligini koʻrsata olasiz.
:::

## Ochiq kalitlar infratuzilmasi (PKI)

S14 da sertifikat ochiq kalit egasini tasdiqlashini koʻrdik. Lekin sertifikatning oʻziga nima uchun ishonamiz? Bunga **ishonch zanjiri** javob beradi:

```text
Ildiz sertifikatlash markazi (Root CA)  — operatsion tizim va brauzerga oldindan oʻrnatilgan
        │ imzolaydi
Oraliq sertifikatlash markazi (Intermediate CA)
        │ imzolaydi
Foydalanuvchi yoki sayt sertifikati (masalan, bank.uz)
```

Brauzer saytga kirganda zanjirni pastdan yuqoriga tekshiradi: sayt sertifikati oraliq markaz tomonidan imzolanganmi, oraliq markaz — ildiz tomonidan, ildiz esa ishonchli roʻyxatdami. Biror boʻgʻin mos kelmasa — ogohlantirish chiqadi (S14).

## Sertifikat hayot sikli

| Bosqich | Mazmuni |
|---|---|
| **Soʻrov** | foydalanuvchi kalit juftini yaratadi va ochiq kalit bilan sertifikat soʻraydi |
| **Tekshirish** | sertifikatlash markazi shaxs yoki tashkilotni tekshiradi (hujjatlar, identifikatsiya) |
| **Berish** | markaz ochiq kalitni egasi maʼlumotlari bilan imzolaydi; amal qilish muddati belgilanadi |
| **Foydalanish** | imzolash, shifrlash, autentifikatsiya |
| **Yangilash** | muddat tugashidan oldin yangi sertifikat olinadi |
| **Bekor qilish** | kalit yoʻqolsa yoki oʻgʻirlansa, xodim ishdan ketsa — sertifikat muddatidan oldin bekor qilinadi |

**Bekor qilinganini tekshirish**: **CRL** (bekor qilingan sertifikatlar roʻyxati) yoki **OCSP** (sertifikat holatini real vaqtda soʻrash). Shuning uchun kalit yoʻqolganda darhol sertifikatlash markaziga xabar berish kerak — aks holda uning nomidan imzolar haqiqiy hisoblanadi.

## Oʻzbekistonda ERI

- ERI kalitlari va sertifikatlari vakolatli **sertifikatlash markazlari** tomonidan beriladi (jismoniy shaxslar va tashkilot rahbarlari uchun).
- Kalit **tashuvchi**: kompyuterdagi himoyalangan fayl, USB-token yoki ID-karta; shuningdek **mobil imzo** (telefon ilovasi orqali).
- **E-IMZO** — brauzerda ERI bilan ishlash uchun dastur: soliq kabineti (S15, MT31), davlat xizmatlari, bank-mijoz tizimlari, elektron hisob-fakturalarda imzo qoʻyish.
- **Xavfsizlik qoidalari**: kalit va uning parolini hech kimga bermaslik (ERI egasi imzolangan hujjat uchun javob beradi); USB-tokenni kompyuterda qoldirmaslik; muddatini kuzatish; yoʻqolsa — darhol bekor qildirish.

## TLS: brauzer va sayt qanday xavfsiz kanal quradi

1. Brauzer saytga ulanadi va qoʻllab-quvvatlanadigan shifrlash usullarini aytadi.
2. Sayt oʻz **sertifikatini** yuboradi; brauzer ishonch zanjirini va muddatni tekshiradi.
3. Ikki tomon **kalit almashish** algoritmi yordamida umumiy maxfiy **seans kaliti**ni hosil qiladi — u tarmoq orqali uzatilmaydi.
4. Keyingi barcha maʼlumotlar shu kalit bilan **simmetrik** shifrlanadi (S14 dagi gibrid sxema).

Natija: maxfiylik (begona oʻqiy olmaydi), yaxlitlik (yoʻlda oʻzgartirib boʻlmaydi) va sayt haqiqiyligi (sertifikat).

## Parollarni xavfsiz saqlash

Tizim parolni oʻzi emas, uning **xeshini** saqlashi kerak (S14). Lekin oddiy xesh yetarli emas:

- **Bir xil parollar bir xil xesh beradi** — bazada kim bir xil parol ishlatganini koʻrish mumkin; mashhur parollarning xeshlari oldindan tayyorlangan jadvallarda bor.
- **Tuz** (salt) — har bir foydalanuvchi uchun tasodifiy qiymat, parolga qoʻshib xeshlanadi va xesh bilan birga saqlanadi. Natijada bir xil parollarning xeshlari har xil boʻladi va tayyor jadvallar foydasiz.
- **Kalitni choʻzish** — xeshlash minglab marta takrorlanadi yoki maxsus sekin funksiya ishlatiladi (**bcrypt, scrypt, Argon2**): bitta tekshiruv foydalanuvchi uchun sezilmaydi, lekin juda koʻp variantni sinash ancha qimmatlashadi.

## Ishlangan misol: tuz bilan parol saqlash

Oʻquv dasturi: `reg login tuz parol` qatori foydalanuvchini roʻyxatdan oʻtkazadi (parol emas, `tuz + parol` ning 1 000 marta takroriy xeshi saqlanadi), `login login parol` qatori kirishni tekshiradi. Tushunarli boʻlishi uchun M12 dagi FNV-1a ishlatiladi — real tizimlarda faqat bcrypt, scrypt yoki Argon2 kabi maxsus funksiyalar qoʻllanadi, tuz esa tasodifiy generator bilan yaratiladi.

```python
import sys

TAKROR = 1000     # kalitni choʻzish (real tizimlarda bcrypt, scrypt, Argon2)

def fnv1a(matn):                                     # oʻquv xesh-funksiyasi
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return format(h, "08x")

def xeshlash(tuz, parol):
    x = tuz + parol
    for _ in range(TAKROR):
        x = fnv1a(x)
    return x

ombor = {}                                           # login -> (tuz, xesh); parolning oʻzi saqlanmaydi
for satr in sys.stdin.read().split("\n"):
    q = satr.split()
    if not q:
        continue
    if q[0] == "reg":
        _, login, tuz, parol = q
        ombor[login] = (tuz, xeshlash(tuz, parol))
        print(f"{login}: saqlandi, tuz {tuz}, xesh {ombor[login][1]}")
    else:
        _, login, parol = q
        saqlangan = ombor[login]
        togri = xeshlash(saqlangan[0], parol) == saqlangan[1]
        print(f"{login}: {'kirish ruxsat etildi' if togri else 'notoʻgʻri parol'}")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final int TAKROR = 1000;     // kalitni choʻzish (real tizimlarda bcrypt, scrypt, Argon2)

    static String fnv1a(String matn) {                               // oʻquv xesh-funksiyasi
        long h = 2166136261L;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = ((h ^ (b & 0xff)) * 16777619L) & 0xFFFFFFFFL;
        return String.format("%08x", h);
    }

    static String xeshlash(String tuz, String parol) {
        String x = tuz + parol;
        for (int i = 0; i < TAKROR; i++) x = fnv1a(x);
        return x;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, String[]> ombor = new HashMap<>();               // login -> (tuz, xesh); parolning oʻzi saqlanmaydi
        while (in.hasNextLine()) {
            String[] q = in.nextLine().trim().split("\\s+");
            if (q[0].isEmpty()) continue;
            if (q[0].equals("reg")) {
                ombor.put(q[1], new String[]{q[2], xeshlash(q[2], q[3])});
                System.out.println(q[1] + ": saqlandi, tuz " + q[2] + ", xesh " + ombor.get(q[1])[1]);
            } else {
                String[] saqlangan = ombor.get(q[1]);
                boolean togri = xeshlash(saqlangan[0], q[2]).equals(saqlangan[1]);
                System.out.println(q[1] + ": " + (togri ? "kirish ruxsat etildi" : "notoʻgʻri parol"));
            }
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <utility>
using namespace std;

const int TAKROR = 1000;     // kalitni choʻzish (real tizimlarda bcrypt, scrypt, Argon2)

string fnv1a(const string& matn) {                                   // oʻquv xesh-funksiyasi
    uint64_t h = 2166136261ULL;
    for (unsigned char b : matn) h = ((h ^ b) * 16777619ULL) & 0xFFFFFFFFULL;
    char buf[16];
    snprintf(buf, sizeof buf, "%08llx", (unsigned long long) h);
    return buf;
}

string xeshlash(const string& tuz, const string& parol) {
    string x = tuz + parol;
    for (int i = 0; i < TAKROR; i++) x = fnv1a(x);
    return x;
}

int main() {
    map<string, pair<string, string>> ombor;                         // login -> (tuz, xesh); parolning oʻzi saqlanmaydi
    string satr;
    while (getline(cin, satr)) {
        istringstream ss(satr);
        string buyruq, login, a, b;
        if (!(ss >> buyruq)) continue;
        if (buyruq == "reg") {
            ss >> login >> a >> b;
            ombor[login] = {a, xeshlash(a, b)};
            cout << login << ": saqlandi, tuz " << a << ", xesh " << ombor[login].second << "\n";
        } else {
            ss >> login >> a;
            bool togri = xeshlash(ombor[login].first, a) == ombor[login].second;
            cout << login << ": " << (togri ? "kirish ruxsat etildi" : "notoʻgʻri parol") << "\n";
        }
    }
    return 0;
}
```

Kirish:

```text
reg ali 7f3a Qwerty2026
reg vali 91c4 Qwerty2026
login ali Qwerty2026
login vali qwerty2026
```

Chiqish:

```text
ali: saqlandi, tuz 7f3a, xesh c3fe1ad8
vali: saqlandi, tuz 91c4, xesh 70db8b04
ali: kirish ruxsat etildi
vali: notoʻgʻri parol
```

Ali va Vali bir xil parol tanlagan, lekin tuzlar har xil boʻlgani uchun bazadagi xeshlar butunlay farq qiladi — bazani koʻrgan odam buni bila olmaydi. Vali parolni kichik harf bilan yozgan — bitta belgi farqi butunlay boshqa xesh beradi va kirish rad etiladi. Tizim hech qachon parolning oʻzini saqlamaydi va "parolingizni eslatib yuborish" imkoniga ega emas — faqat yangi parol oʻrnatish mumkin.

## Disk, fayl va xabarlarni shifrlash

- **Diskni toʻliq shifrlash** (BitLocker, FileVault, Android va iOS dagi shifrlash) — noutbuk yoki telefon yoʻqolsa ham maʼlumot oʻqilmaydi (M14 dagi "laptop" riski).
- **Arxivlarni parol bilan shifrlash** — fayl yuborishda; parolni boshqa kanal orqali yuborish (masalan, xat — pochtada, parol — SMS da).
- **Uchdan-uchga shifrlash** (end-to-end) — messenjerlarda xabarni faqat suhbatdoshlar ocha oladi, xizmat serveri ham ocha olmaydi.

## Kalitlarni boshqarish qoidalari

1. Kalitlar kod, hujjat yoki elektron xatda ochiq saqlanmaydi (MT21).
2. Har bir maqsad uchun alohida kalit; muntazam almashtirish.
3. Kalitlarning zaxira nusxasi xavfsiz joyda — kalit yoʻqolsa, shifrlangan maʼlumot ham yoʻqoladi.
4. Kimda qaysi kalit borligi roʻyxati; xodim ketganda kalitlar va sertifikatlar bekor qilinadi.

:::warning
**Koʻp uchraydigan xatolar**

- **ERI kalitini va parolini hamkasbga berish.** Imzolangan hujjat uchun egasi javob beradi.
- **Kalit yoʻqolganda sertifikatni bekor qildirmaslik.**
- **Parollarni ochiq yoki tuzsiz xesh bilan saqlash.**
- **Shifrlash kalitini shifrlangan fayl bilan bir joyda yuborish.**
- **"Oʻz shifrini" yaratish.** Faqat sinalgan standartlar (S14).
:::

## Topshiriq

1. Brauzerda bank saytining sertifikat zanjirini oching: ildiz, oraliq va sayt sertifikatlarini, ularning muddatlarini yozing.
2. Misol dasturiga uchinchi foydalanuvchini Ali bilan bir xil tuz va bir xil parol bilan qoʻshing. Xeshlar qanday chiqadi? Bu nima uchun tuz har bir foydalanuvchi uchun noyob boʻlishi kerakligini koʻrsatadi.
3. Kompaniya uchun ERI dan foydalanish qoidalari yoʻriqnomasini (8–10 band) tuzing.
4. Oʻz telefoningiz va kompyuteringizda disk shifrlash yoqilganmi — tekshiring.

## Xulosa

- PKI ishonch zanjiri: ildiz → oraliq → foydalanuvchi sertifikati; sertifikat beriladi, yangilanadi va bekor qilinadi (CRL, OCSP).
- Oʻzbekistonda ERI: sertifikatlash markazlari, kalit tashuvchilar, mobil imzo, E-IMZO; kalit — egasining shaxsiy javobgarligi.
- TLS: sertifikat tekshiruvi, kalit almashish, simmetrik shifrlash.
- Parollar tuz va kalitni choʻzish bilan xeshlanadi (bcrypt, scrypt, Argon2); disk, fayl va xabarlar shifrlanadi; kalitlar qoidalar boʻyicha boshqariladi.

## Oʻzingizni tekshiring

:::check
Q: Brauzer sayt sertifikatiga nima uchun ishonadi?
A: Sertifikat ishonch zanjiri orqali brauzerda oldindan oʻrnatilgan ildiz sertifikatlash markazigacha tekshiriladi.
Q: ERI kaliti yoʻqolsa, nima qilish kerak?
A: Darhol sertifikatlash markaziga xabar berib, sertifikatni bekor qildirish.
Q: Parol xeshiga "tuz" nima uchun qoʻshiladi?
A: Bir xil parollar har xil xesh berishi va oldindan tayyorlangan xesh jadvallari foydasiz boʻlishi uchun.
Q: TLS da maʼlumotning oʻzi qanday shifrlanadi?
A: Kalit almashish orqali hosil qilingan seans kaliti bilan simmetrik shifrlash yordamida.
:::

:::link https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
OWASP: Password Storage Cheat Sheet
Parollarni xavfsiz saqlash boʻyicha amaliy tavsiyalar (ingliz tilida).
:::
