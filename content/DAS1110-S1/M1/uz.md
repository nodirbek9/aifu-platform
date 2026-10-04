---
title: Dasturlashning asosiy tushunchalari
summary: Algoritm va dastur, kompyuter dasturni qanday bajarishi, kompilyator va interpretator, birinchi dastur: kiritish, hisoblash, chiqarish.
---

## Nima uchun bu mavzu muhim?

Dekanatda har semestr oxirida 600 ta talabaning yakuniy balli hisoblanadi: joriy, oraliq va yakuniy nazorat ballari qoʻshiladi, natija baho shkalasiga solishtiriladi va qarzdorlar roʻyxati tuziladi. Buni qoʻlda qilish bir necha kun oladi va har bir hisobda xato ehtimoli bor. Xuddi shu ishni kompyuter bir soniyada, xatosiz bajaradi — lekin faqat unga **nima qilish kerakligini aniq aytib bersak**.

Kompyuterga ishni tushuntirish — dasturlash. Bu darsda biz kompyuter bilan „gaplashish“ning asosiy tushunchalarini oʻrganamiz: algoritm nima, dastur qanday tuziladi, kompyuter uni qanday bajaradi va eng oddiy dastur qanday koʻrinishda boʻladi.

:::note
**Bu darsdan soʻng siz:**

- algoritm va dastur oʻrtasidagi farqni tushuntira olasiz;
- algoritmning asosiy xossalarini sanab, misol bilan izohlay olasiz;
- kompilyator va interpretator qanday ishlashini taqqoslay olasiz;
- maʼlumotni kiritib, hisoblab, natijani chiqaradigan birinchi dasturni Python, Java va C++ tillarida oʻqiy va yoza olasiz.
:::

## Algoritm

**Algoritm (algorithm)** — biror masalani yechish uchun aniq belgilangan tartibda bajariladigan chekli sonli qadamlar ketma-ketligi.

Algoritm kundalik hayotda ham har joyda bor. Choy damlash retsepti, bankomatdan pul yechish tartibi, universitetga kirish uchun hujjat topshirish bosqichlari — bularning barchasi algoritmlar. Masalan, ikki sonning kattasini topish algoritmi:

1. Birinchi sonni oling va uni *a* deb nomlang.
2. Ikkinchi sonni oling va uni *b* deb nomlang.
3. Agar *a* > *b* boʻlsa, javob *a*; aks holda javob *b*.
4. Javobni ayting.

Algoritm qaysi tilda yozilishidan qatʼi nazar bir xil boʻladi: uni oʻzbek tilida, blok-sxema koʻrinishida yoki dasturlash tilida ifodalash mumkin. Dasturlashdagi eng muhim ish ham aynan algoritmni topish; uni tilga oʻtkazish — keyingi qadam.

### Algoritmning xossalari

| Xossa | Maʼnosi | Buzilsa nima boʻladi |
|---|---|---|
| **Diskretlik** | Algoritm alohida, ketma-ket qadamlardan iborat | Qaysi ishni qachon qilish noaniq qoladi |
| **Aniqlik** | Har bir qadam bir xil tushuniladi, ikki xil talqin qilinmaydi | „Bir oz tuz soling“ — har kim har xil soladi |
| **Cheklilik** | Algoritm chekli sondagi qadamdan keyin tugaydi | Dastur „osilib qoladi“, natija chiqmaydi |
| **Natijaviylik** | Algoritm aniq natija beradi | Ish qilinadi-yu, javob yoʻq |
| **Ommaviylik** | Algoritm bir turdagi barcha masalalar uchun ishlaydi | Faqat 5 va 7 uchun ishlaydigan „qoʻshish“ algoritmi foydasiz |

### Algoritmni ifodalash usullari

- **Soʻz bilan** — yuqoridagi kabi raqamlangan qadamlar. Oddiy masalalar uchun qulay.
- **Blok-sxema** — geometrik shakllar bilan: oval — boshlanish va tugash, toʻrtburchak — amal, romb — shart, parallelogramm — kiritish va chiqarish. Shartlar va takrorlanishlarni koʻrish uchun qulay.
- **Psevdokod** — dasturlash tiliga oʻxshash, lekin qatʼiy qoidalarsiz yozuv.
- **Dasturlash tilida** — kompyuter bajaradigan yagona koʻrinish.

## Dastur va dasturlash tili

**Dastur (program)** — algoritmning kompyuter tushunadigan tilda yozilgan koʻrinishi. **Dasturlash tili (programming language)** — dasturlarni yozish uchun qatʼiy qoidalarga ega boʻlgan sunʼiy til.

Har bir dasturlash tilining ikki tomoni bor:

- **Sintaksis (syntax)** — yozish qoidalari: qaysi soʻzlar ishlatiladi, qavslar, nuqtali vergul, chekinishlar qayerda qoʻyiladi. Sintaksis buzilsa, dastur umuman ishga tushmaydi.
- **Semantika (semantics)** — yozilgan narsaning maʼnosi: dastur aslida nima qiladi. Sintaksisi toʻgʻri, lekin maʼnosi notoʻgʻri dastur ishlaydi, ammo notoʻgʻri javob beradi. Bunday xatolarni topish ancha qiyin.

Bu kursda uchta tilni yonma-yon koʻramiz: **Python**, **Java** va **C++**. Uchalasi ham hozir sanoatda keng ishlatiladi, ammo har biri oʻz yoʻli bilan ishlaydi — buni quyida koʻramiz.

## Kompyuter dasturni qanday bajaradi

Protsessor faqat **mashina kodini** — ikkilik koʻrinishdagi juda oddiy buyruqlarni (ikki sonni qoʻsh, xotiraga yoz, keyingi buyruqqa oʻt) tushunadi. Biz yozadigan matn esa **dastlabki kod (source code)** deyiladi. Uni mashina kodiga aylantirishning ikki asosiy yoʻli bor.

**Kompilyator (compiler)** butun dasturni oldindan, bir marta tarjima qiladi va bajariladigan fayl hosil qiladi. Keyin shu fayl tarjimasiz, tez ishlaydi. Xato boʻlsa, kompilyator tarjima paytidayoq xabar beradi va fayl hosil qilinmaydi. C++ shunday ishlaydi.

**Interpretator (interpreter)** dasturni bajarish paytida qatorma-qator oʻqiydi va darhol bajaradi. Dasturni tez sinab koʻrish qulay, lekin bajarilish sekinroq; xato esa faqat oʻsha qatorga navbat kelganda chiqadi. Python shunday ishlaydi.

**Java** oraliq yoʻlni tanlaydi: kompilyator dasturni **bayt-kodga** oʻgiradi, uni esa **Java virtual mashinasi (JVM)** bajaradi. Shuning uchun bitta Java dasturi Windows'da ham, Linux'da ham qayta kompilyatsiyasiz ishlaydi.

| | C++ | Java | Python |
|---|---|---|---|
| Tarjima | Kompilyator → mashina kodi | Kompilyator → bayt-kod → JVM | Interpretator |
| Xato qachon topiladi | Kompilyatsiyada | Kompilyatsiyada | Bajarish paytida |
| Tezlik | Juda yuqori | Yuqori | Oʻrtacha |
| Birinchi dastur hajmi | Bir necha qator | Klass va `main` metodi kerak | Bir qator |

## Dasturning tuzilishi: kiritish, qayta ishlash, chiqarish

Oʻquv masalalarining aksariyati uch qismdan iborat:

1. **Kiritish (input)** — dastur maʼlumotni oladi: klaviaturadan, fayldan yoki tarmoqdan.
2. **Qayta ishlash (processing)** — maʼlumot ustida amallar bajariladi.
3. **Chiqarish (output)** — natija ekranga, faylga yoki boshqa dasturga beriladi.

Maʼlumot dastur ishlayotganda **oʻzgaruvchilarda (variables)** saqlanadi. Oʻzgaruvchi — xotiradagi nomlangan joy: `a = 5` yozuvi „*a* nomli joyga 5 ni yoz“ degani.

Quyidagi dastur ikki butun sonni oʻqiydi va ularning yigʻindisini chiqaradi. Uchala tildagi variantini solishtiring.

```python
# Ikki sonni oʻqiymiz (har biri alohida qatorda)
a = int(input())
b = int(input())

# Yigʻindini hisoblaymiz va chiqaramiz
s = a + b
print("Yigʻindi:", s)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // Ikki sonni oʻqiymiz
        int a = in.nextInt();
        int b = in.nextInt();

        // Yigʻindini hisoblaymiz va chiqaramiz
        int s = a + b;
        System.out.println("Yigʻindi: " + s);
    }
}
```
```cpp
#include <iostream>
using namespace std;

int main() {
    // Ikki sonni oʻqiymiz
    int a, b;
    cin >> a >> b;

    // Yigʻindini hisoblaymiz va chiqaramiz
    int s = a + b;
    cout << "Yigʻindi: " << s << endl;
    return 0;
}
```

## Dastur qadam-baqadam

Kirish maʼlumoti `12` va `30` boʻlsin. Dastur quyidagicha ishlaydi:

1. Birinchi qator oʻqiladi va butun songa aylantiriladi: *a* = 12. Python'da `input()` matn qaytaradi, shuning uchun `int(...)` kerak; Java'da `nextInt()`, C++'da `cin >>` sonni toʻgʻridan-toʻgʻri oʻqiydi.
2. Ikkinchi son oʻqiladi: *b* = 30.
3. `s = a + b` — yigʻindi hisoblanadi va *s* oʻzgaruvchisiga yoziladi: *s* = 42.
4. Natija chiqariladi: `Yigʻindi: 42`.

Uchala dasturda ham **bir xil algoritm**, farq faqat sintaksisda. Java va C++'da oʻzgaruvchining turi (`int` — butun son) oldindan yoziladi; Python'da tur qiymatga qarab oʻzi aniqlanadi. Java dasturi `class Main` ichida, C++ dasturi `main()` funksiyasi ichida yoziladi — dastur shu yerdan boshlanadi.

:::warning
**Koʻp uchraydigan xatolar**

- **Python'da `int()`ni unutish.** `input()` matn qaytaradi: `"12" + "30"` natijasi `"1230"` boʻladi, 42 emas.
- **C++ va Java'da nuqtali vergulni (`;`) tushirib qoldirish.** Bu sintaksis xatosi — dastur kompilyatsiya qilinmaydi.
- **Katta-kichik harflarni aralashtirish.** `Print`, `Main`, `System.Out` — boshqa nomlar. Uchala til ham harf registrini farqlaydi.
- **Masalani oʻylamasdan kod yozish.** Avval qogʻozda algoritmni tuzing: nima kiradi, nima chiqadi, oraliqda nima qilinadi.
:::

:::tip
Har bir dasturni yozishdan oldin oʻzingizga uchta savol bering: **nima beriladi? nima topilishi kerak? qanday qilib?** Shu uch savolga javob — tayyor algoritm.
:::

## Xulosa

- **Algoritm** — masalani yechishning aniq, chekli qadamlar ketma-ketligi; **dastur** — uning dasturlash tilidagi koʻrinishi.
- Algoritmning asosiy xossalari: diskretlik, aniqlik, cheklilik, natijaviylik, ommaviylik.
- **Kompilyator** dasturni oldindan butunlay tarjima qiladi (C++), **interpretator** bajarish paytida qatorma-qator ishlaydi (Python), Java esa bayt-kod va JVM orqali ishlaydi.
- **Sintaksis** — yozish qoidalari, **semantika** — maʼno; ikkalasi ham toʻgʻri boʻlishi kerak.
- Oddiy dastur uch qismdan iborat: kiritish, qayta ishlash, chiqarish; maʼlumot oʻzgaruvchilarda saqlanadi.

## Oʻzingizni tekshiring

:::check
Q: Algoritm bilan dastur oʻrtasidagi farq nima?
A: Algoritm — masalani yechish qadamlari, u istalgan koʻrinishda (soʻz, blok-sxema) yozilishi mumkin. Dastur — aynan shu algoritmning kompyuter bajara oladigan dasturlash tilidagi yozuvi.
Q: „Suv qaynaguncha kuting“ qadami algoritmning qaysi xossasini buzishi mumkin?
A: Cheklilikni: agar plita yoqilmagan boʻlsa, suv hech qachon qaynamaydi va algoritm tugamaydi.
Q: Kompilyator va interpretatorning asosiy farqi nimada?
A: Kompilyator butun dasturni oldindan tarjima qilib, bajariladigan fayl yaratadi; interpretator esa dasturni bajarish paytida qatorma-qator tarjima qilib bajaradi.
Q: Python'dagi `print(input() + input())` dasturiga `12` va `30` berilsa, nima chiqadi va nima uchun?
A: `1230` chiqadi, chunki `input()` matn qaytaradi va ikki matn qoʻshilganda ular yonma-yon yopishtiriladi. Son sifatida qoʻshish uchun `int(input())` kerak.
Q: Sintaksis xatosi va mantiqiy (semantik) xatoning farqi nimada?
A: Sintaksis xatosida dastur umuman ishga tushmaydi (kompilyator yoki interpretator xabar beradi). Mantiqiy xatoda dastur ishlaydi, lekin notoʻgʻri natija beradi.
:::

:::link https://docs.python.org/3/tutorial/introduction.html
Python rasmiy qoʻllanmasi: Python bilan tanishuv
Python'da birinchi qadamlar, sonlar va matnlar bilan ishlash.
:::

:::link https://en.cppreference.com/w/cpp/language/main_function
cppreference: main funksiyasi
C++ dasturi qayerdan boshlanishi haqida maʼlumotnoma.
:::
