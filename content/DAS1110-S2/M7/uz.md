---
title: Visual Studio muhitida dasturlash
summary: Integratsiyalashgan ishlab chiqish muhiti (IDE) tarkibi, Visual Studioʻda yechim va loyiha, Debug va Release konfiguratsiyalari, kompilyator ogohlantirishlari, tuzatuvchi — toʻxtash nuqtalari, qadamlab bajarish, Watch va Call Stack oynalari; xatoni tizimli izlash.
---

## Nima uchun bu mavzu muhim?

Hozirgacha dasturlarni oddiy matn muharririda yozib, buyruq qatoridan kompilyatsiya qilgan boʻlishingiz mumkin. Kichik dasturlar uchun bu yetarli. Lekin dastur yuzlab qatorga yetganda va natija notoʻgʻri chiqqanda, „qayerda xato?“ savoliga `print` qoʻyib javob izlash soatlab vaqt oladi.

**Integratsiyalashgan ishlab chiqish muhiti** (IDE) muharrir, kompilyator va **tuzatuvchi**ni (debugger) bitta oynaga birlashtiradi. Tuzatuvchi dasturni istalgan qatorda toʻxtatib, barcha oʻzgaruvchilarning qiymatini koʻrsatadi va dasturni qadamma-qadam bajarishga imkon beradi. Professional dasturchilar vaqtining katta qismini aynan tuzatuvchida oʻtkazadi. Bu maʼruzada Visual Studio misolida shu vositalarni oʻrganamiz; ular IntelliJ IDEA, PyCharm va VS Codeʼda ham deyarli bir xil.

:::note
**Bu darsdan soʻng siz:**

- IDE ning asosiy qismlarini va ularning vazifasini ayta olasiz;
- Visual Studioʻda yechim va loyiha yaratib, C++ dasturini yigʻa va ishga tushira olasiz;
- Debug va Release konfiguratsiyalarining farqini tushuntira olasiz;
- toʻxtash nuqtasi qoʻyib, dasturni qadamlab bajara va oʻzgaruvchilarni kuzata olasiz;
- xatoni tizimli usulda — taxmin, tekshiruv, tuzatish — izlay olasiz.
:::

## IDE tarkibi

| Qism | Vazifasi | Visual Studioʻda |
|---|---|---|
| Muharrir | kod yozish, rang bilan ajratish, avtomatik toʻldirish | IntelliSense — nom yozishni boshlaganda variantlar taklif qilinadi |
| Loyiha tizimi | qaysi fayllar dasturga kirishini va qanday yigʻilishini saqlaydi | Solution Explorer oynasi |
| Yigʻuvchi (build) | kompilyator va bogʻlovchini (linker) chaqiradi | Build → Build Solution (Ctrl+Shift+B) |
| Xatolar roʻyxati | kompilyator xato va ogohlantirishlari | Error List oynasi (xatoga ikki marta bosilsa, oʻsha qatorga oʻtadi) |
| Tuzatuvchi | dasturni toʻxtatish, qadamlash, qiymatlarni koʻrish | Debug menyusi, Locals, Watch, Call Stack oynalari |

## Yechim va loyiha

Visual Studioʻda ish **yechim** (solution, `.sln` fayli) ichida tashkil etiladi. Yechim bir yoki bir nechta **loyiha**dan (project, C++ uchun `.vcxproj`) iborat; har bir loyiha bitta dastur yoki kutubxona beradi.

Yangi C++ dasturini yaratish:

1. File → New → Project → **Console App** (C++) shablonini tanlang.
2. Loyiha nomi va joylashuvini kiriting → Create.
3. Solution Explorerʼda `Source Files` ichida `.cpp` fayl paydo boʻladi — kodni shu yerda yozasiz.
4. Ctrl+F5 — tuzatuvchisiz ishga tushirish (oyna dastur tugagach yopilmaydi), F5 — tuzatuvchi bilan.

C++ standartini tanlash: loyiha ustida oʻng tugma → Properties → C/C++ → Language → **C++ Language Standard** (masalan, ISO C++17). Ogohlantirish darajasi: C/C++ → General → **Warning Level** — oʻquv dasturlari uchun `/W4` tavsiya etiladi.

## Debug va Release

| | Debug | Release |
|---|---|---|
| Maqsadi | xatolarni izlash | foydalanuvchiga beriladigan versiya |
| Optimallashtirish | yoʻq — kod yozilganidek bajariladi | yoqilgan — kompilyator kodni qayta tartiblaydi |
| Tuzatish maʼlumoti | toʻliq — har bir qator va oʻzgaruvchi koʻrinadi | minimal |
| `assert` | ishlaydi | oʻchiriladi (`NDEBUG` belgilanadi) |
| Tezlik | sekinroq | bir necha marta tezroq boʻlishi mumkin |

Konfiguratsiya asboblar panelidagi roʻyxatdan tanlanadi. Muhim saboq: baʼzi xatolar (masalan, boshlangʻich qiymat berilmagan oʻzgaruvchi) Debugʻda „ishlab“, Releaseʼda boshqacha natija berishi mumkin — dastur ikkala rejimda ham tekshiriladi.

## Tuzatuvchi bilan ishlash

**Toʻxtash nuqtasi** (breakpoint) — dastur shu qatorga yetganda toʻxtaydigan belgi. U qator chap chetiga bosish yoki F9 bilan qoʻyiladi va qizil doira bilan koʻrsatiladi. Dastur toʻxtagach:

| Amal | Visual Studio / VS Code | IntelliJ IDEA / PyCharm |
|---|---|---|
| Tuzatuvchi bilan ishga tushirish | F5 | Shift+F9 |
| Toʻxtash nuqtasi qoʻyish/olish | F9 | Ctrl+F8 |
| Keyingi qatorga (funksiyaga kirmasdan) — Step Over | F10 | F8 |
| Funksiya ichiga kirish — Step Into | F11 | F7 |
| Funksiyadan chiqish — Step Out | Shift+F11 | Shift+F8 |
| Keyingi toʻxtash nuqtasigacha davom etish | F5 | F9 |

Toʻxtagan paytda foydali oynalar:

- **Locals** (Variables) — joriy funksiyaning barcha oʻzgaruvchilari va qiymatlari;
- **Watch** — siz qoʻshgan ifodalar (masalan, `a[i] + a[j]`, `yigindi / n`) har qadamda qayta hisoblanadi;
- **Call Stack** — qaysi funksiya qaysisini chaqirgani; rekursiyada chaqiruvlar zanjirini koʻrish uchun juda foydali;
- **Shartli toʻxtash nuqtasi** — toʻxtash nuqtasi ustida oʻng tugma → Conditions: masalan, `i == 500` boʻlganda toʻxtash. Katta sikllarda ming marta F5 bosmaslik uchun.

## Xatoni tizimli izlash

Tuzatuvchi — vosita; xatoni topish esa **usul**. Samarali tartib:

1. **Takrorlang.** Xatoni har safar chiqaradigan eng kichik kirishni toping.
2. **Kutilgan natijani qoʻlda hisoblang.** Dastur nima chiqarishi *kerak* edi?
3. **Taxmin qiling.** Qaysi qism notoʻgʻri ishlashi mumkin?
4. **Tekshiring.** Shu joyga toʻxtash nuqtasi qoʻyib, oʻzgaruvchilarni kutilgan qiymatlar bilan solishtiring. Qiymat birinchi marta „buzilgan“ qatorni toping.
5. **Tuzating va qayta tekshiring** — barcha avvalgi testlar bilan birga.

Toʻxtash nuqtasi qoʻyish imkoni boʻlmasa (masalan, avtomatik tekshiruv tizimida), tuzatuv maʼlumotini **xatolar oqimiga** chiqarish mumkin: `cerr` (C++), `System.err` (Java), `sys.stderr` (Python). Bu oqim asosiy natijaga aralashmaydi va testlar uni tekshirmaydi.

## Ishlangan misol 1: oʻrtacha qiymatdagi xato

Vazifa: *n* va *n* ta butun son kiritiladi; oʻrta arifmetikni ikki kasr xona bilan chiqarish. Talaba quyidagi C++ kodini yozdi va `3` hamda `1 2 2` uchun `1.00` oldi, garchi javob `1.67` boʻlishi kerak:

```text
int yigindi = 0;
for (int i = 0; i < n; i++) yigindi += a[i];
double ortacha = yigindi / n;          // ← toʻxtash nuqtasi shu qatorda
printf("%.2f\n", ortacha);
```

Tuzatuvchida ushbu qatorda toʻxtab, Watch oynasiga ifodalarni qoʻshamiz:

| Ifoda | Qiymat | Xulosa |
|---|---|---|
| `yigindi` | 5 | toʻgʻri |
| `n` | 3 | toʻgʻri |
| `yigindi / n` | 1 | **xato shu yerda**: ikkala operand `int`, boʻlish butun |
| `(double) yigindi / n` | 1.6666… | kerakli qiymat |

Xato — `int / int` butun boʻlish: `ortacha` `double` boʻlsa ham, oʻng tomon avval butun son sifatida hisoblanadi. Tuzatilgan dastur:

```python
n = int(input())
a = list(map(int, input().split()))

yigindi = sum(a)
ortacha = yigindi / n                  # Python'da / har doim haqiqiy boʻlish
print(f"{ortacha:.2f}")
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int yigindi = 0;
        for (int i = 0; i < n; i++) yigindi += in.nextInt();

        double ortacha = (double) yigindi / n;     // avval double ga, keyin boʻlish
        System.out.printf("%.2f%n", ortacha);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    long long yigindi = 0;
    for (int i = 0; i < n; i++) yigindi += a[i];
    double ortacha = (double) yigindi / n;     // avval double ga, keyin boʻlish
    printf("%.2f\n", ortacha);
    return 0;
}
```

`3` va `1 2 2` uchun endi `1.67` chiqadi. Pythonda bu xato boʻlmaydi: `/` doim haqiqiy boʻlish, butun boʻlish esa `//`. Java va C++'da esa bu eng koʻp uchraydigan xatolardan biri — shuning uchun Watch oynasida **oraliq ifodalarni** (`yigindi / n`) alohida koʻrish juda foydali.

## Ishlangan misol 2: tuzatuv chiqishi va assert

*n* kiritiladi (2 ≤ *n* ≤ 10¹²). Uning tub koʻpaytuvchilarini oʻsish tartibida chiqaring. Dastur har bir qadamni **xatolar oqimiga** yozib boradi — bu asosiy natijaga taʼsir qilmaydi — va oxirida `assert` bilan natijani tekshiradi: koʻpaytuvchilar koʻpaytmasi *n* ga teng boʻlishi shart.

```python
import sys

n = int(input())
qoldiq = n
kopaytuvchilar = []
d = 2
while d * d <= qoldiq:
    while qoldiq % d == 0:
        kopaytuvchilar.append(d)
        qoldiq //= d
        print(f"[debug] {d} topildi, qoldi {qoldiq}", file=sys.stderr)
    d += 1
if qoldiq > 1:
    kopaytuvchilar.append(qoldiq)

kopaytma = 1
for p in kopaytuvchilar:
    kopaytma *= p
assert kopaytma == n, "koʻpaytuvchilar notoʻgʻri"

print(*kopaytuvchilar)
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        long n = new Scanner(System.in).nextLong();
        long qoldiq = n;
        List<Long> kopaytuvchilar = new ArrayList<>();
        for (long d = 2; d * d <= qoldiq; d++) {
            while (qoldiq % d == 0) {
                kopaytuvchilar.add(d);
                qoldiq /= d;
                System.err.println("[debug] " + d + " topildi, qoldi " + qoldiq);
            }
        }
        if (qoldiq > 1) kopaytuvchilar.add(qoldiq);

        long kopaytma = 1;
        for (long p : kopaytuvchilar) kopaytma *= p;
        assert kopaytma == n : "koʻpaytuvchilar notoʻgʻri";   // java -ea bilan yoqiladi

        StringBuilder sb = new StringBuilder();
        for (long p : kopaytuvchilar) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(p);
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <cassert>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    long long n;
    cin >> n;
    long long qoldiq = n;
    vector<long long> kopaytuvchilar;
    for (long long d = 2; d * d <= qoldiq; d++) {
        while (qoldiq % d == 0) {
            kopaytuvchilar.push_back(d);
            qoldiq /= d;
            cerr << "[debug] " << d << " topildi, qoldi " << qoldiq << endl;
        }
    }
    if (qoldiq > 1) kopaytuvchilar.push_back(qoldiq);

    long long kopaytma = 1;
    for (long long p : kopaytuvchilar) kopaytma *= p;
    assert(kopaytma == n);                     // Release'da (NDEBUG) oʻchiriladi

    for (size_t i = 0; i < kopaytuvchilar.size(); i++) {
        if (i > 0) cout << ' ';
        cout << kopaytuvchilar[i];
    }
    cout << endl;
    return 0;
}
```

`360` kiritilsa, asosiy chiqish `2 2 2 3 3 5`. Xatolar oqimida esa qadamlar koʻrinadi:

```text
[debug] 2 topildi, qoldi 180
[debug] 2 topildi, qoldi 90
[debug] 2 topildi, qoldi 45
[debug] 3 topildi, qoldi 15
[debug] 3 topildi, qoldi 5
```

Visual Studioʻda xatolar oqimi konsol oynasida asosiy natija bilan birga koʻrinadi, avtomatik tekshiruv esa faqat asosiy oqimni solishtiradi. `assert` sharti bajarilmasa, dastur xabar bilan toʻxtaydi va xato aynan shu joyda ekani darhol maʼlum boʻladi. Javada `assert` sukut boʻyicha oʻchiq — u `java -ea Main` bilan yoqiladi; C++'da esa Release konfiguratsiyasida `NDEBUG` tufayli oʻchiriladi. Shuning uchun `assert` ichida dastur ishiga taʼsir qiladigan amal yozilmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Ogohlantirishlarni eʼtiborsiz qoldirish.** „Variable used without being initialized“ kabi ogohlantirish — koʻpincha haqiqiy xato; `/W4` bilan yigʻing va ularni tuzating.
- **Releaseʼda tuzatishga urinish.** Optimallashtirish tufayli oʻzgaruvchilar „yoʻqolib“ qoladi va qadamlar sakraydi; tuzatish uchun Debug tanlang.
- **Tuzatuv `print`larini asosiy chiqishda qoldirish.** Avtomatik tekshiruv ularni xato javob deb hisoblaydi; `cerr`, `System.err`, `sys.stderr` ishlating yoki olib tashlang.
- **`assert` ichida muhim amal.** `assert(++k < n)` Releaseʼda umuman bajarilmaydi va dastur xatti-harakati oʻzgaradi.
:::

:::tip
Xato topilganda uni keltirib chiqargan kirishni test sifatida saqlab qoʻying. Har bir tuzatishdan soʻng barcha saqlangan testlarni qayta ishga tushiring — shunda eski xato qaytib kelmaydi.
:::

## Xulosa

- IDE muharrir, loyiha tizimi, yigʻuvchi, xatolar roʻyxati va tuzatuvchini birlashtiradi.
- Visual Studioʻda ish yechim (`.sln`) va loyihalar ichida tashkil etiladi; standart va ogohlantirish darajasi loyiha xususiyatlarida sozlanadi.
- Debug — tuzatish uchun (optimallashtirishsiz, `assert` ishlaydi), Release — foydalanuvchi uchun.
- Tuzatuvchi: toʻxtash nuqtasi (F9), Step Over (F10), Step Into (F11), Locals, Watch va Call Stack oynalari.
- Xato tizimli izlanadi: takrorlash → kutilgan natija → taxmin → tekshiruv → tuzatish.

## Oʻzingizni tekshiring

:::check
Q: Step Over (F10) va Step Into (F11) ning farqi nima?
A: Step Over joriy qatorni toʻliq bajarib keyingisiga oʻtadi; Step Into qatordagi funksiya chaqiruvi ichiga kiradi.
Q: Nima uchun tuzatishni Release konfiguratsiyasida qilish noqulay?
A: Optimallashtirish kodni qayta tartiblaydi: oʻzgaruvchilar koʻrinmaydi, qadamlar manba kodiga mos kelmaydi.
Q: Call Stack oynasi nimani koʻrsatadi?
A: Joriy nuqtaga qaysi funksiyalar zanjiri orqali kelinganini — kim kimni chaqirganini.
Q: Tuzatuv xabarlarini avtomatik tekshiruvga xalal bermaydigan qilib qayerga chiqarish mumkin?
A: Xatolar oqimiga: `cerr`, `System.err` yoki `sys.stderr`.
Q: `int yigindi = 5, n = 2; double x = yigindi / n;` dan keyin `x` nimaga teng va nega?
A: 2.0 ga: `yigindi / n` butun boʻlish, natija `double` ga keyin oʻtkaziladi.
:::

:::link https://learn.microsoft.com/en-us/visualstudio/debugger/debugger-feature-tour
Visual Studio: tuzatuvchi bilan tanishuv
Toʻxtash nuqtalari, qadamlash va oʻzgaruvchilarni kuzatish boʻyicha rasmiy qoʻllanma (ingliz tilida).
:::

:::link https://code.visualstudio.com/docs/editor/debugging
VS Code: tuzatish
VS Codeʼda Python, Java va C++ dasturlarini tuzatish (ingliz tilida).
:::
