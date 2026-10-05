---
title: Maʼruza va amaliy mashgʻulotlar mavzularini oʻzlashtirish
summary: "Kiberxavfsizlik asoslari" fanini mustaqil oʻzlashtirish rejasi; modullar va ularning bogʻliqligi; haftalik jadval; maʼruza materialini faol oʻqish usuli; amaliy ishlarni uyda xavfsiz takrorlash uchun bepul vositalar (CyberChef, OpenSSL, virtual mashina); axloqiy va huquqiy chegaralar; tayanch tushunchalar lugʻati; modullar boʻyicha oʻz-oʻzini tekshirish savollari.
---

## Nima uchun bu mavzu muhim?

Kiberxavfsizlik — "keng" fan: unda huquq va risklarni boshqarish, matematika (kriptografiya), operatsion tizimlar va tarmoqlar birlashadi. Maʼruzada har bir mavzu qisqa koʻriladi; uni koʻnikmaga aylantirish uchun materialni qayta oʻqish, buyruqlarni oʻz qoʻlingiz bilan bajarish va oʻz-oʻzini tekshirish kerak. Mustaqil taʼlim soatlari aynan shu uchun ajratilgan. Bu ishda butun fanni qanday rejalashtirib oʻzlashtirishni koʻramiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- fan modullari va ularning bir-biriga bogʻliqligini bilasiz;
- haftalik oʻzlashtirish rejasini tuza olasiz;
- maʼruza materialini faol oʻqish usulidan foydalana olasiz;
- amaliy ishlarni uyda xavfsiz va qonuniy takrorlash uchun muhit tayyorlay olasiz;
- har bir modul boʻyicha bilimingizni tekshira olasiz.
:::

## Fan tuzilishi va oʻzlashtirish rejasi

| Modul | Maʼruzalar | Amaliy | Asosiy natija |
|---|---|---|---|
| 1. Asoslar, risklar va siyosat | M1–M4 | A1–A4 | CIA triadasi, risk = ehtimollik × taʼsir, qonunlar, xavfsizlik siyosati |
| 2. Kriptografiya | M5–M9 | A5–A9 | klassik va simmetrik shifrlar, RSA, xesh, ERI, TLS |
| 3. Autentifikatsiya, foydalanishni boshqarish va fizik xavfsizlik | M10–M12 | A10–A12 | parollar va MFA, DAC/MAC/RBAC/ABAC, fizik himoya |
| 4. Tarmoq, bulut va IoT xavfsizligi | M13–M15 | A13–A15 | tarmoq tahdidlari, himoya vositalari, bulut va IoT |

Modullar ketma-ket bogʻlangan: risk tushunchasi (1-modul) har bir choraning narxini asoslaydi; kriptografiya (2-modul) autentifikatsiya, VPN va TLS ning asosi; foydalanishni boshqarish (3-modul) tarmoq va bulut siyosatlarida qayta uchraydi. Shuning uchun bitta mavzuni oʻtkazib yuborsangiz, keyingilari qiyinlashadi.

**Haftalik reja** (har bir mavzu uchun):

1. **Maʼruzadan oldin** (20 daqiqa): platformadagi dars matnini koʻz yugurtirib oʻqing, notanish atamalarni yozib oling.
2. **Maʼruzadan keyin** (40 daqiqa): matnni faol oʻqing (quyida), ishlangan misollarni oʻzingiz bajaring.
3. **Amaliy mashgʻulotdan keyin** (40 daqiqa): buyruq va vositalarni uyda takrorlang, dasturiy topshiriqni yakunlang.
4. **Hafta oxirida** (20 daqiqa): "Oʻzingizni tekshiring" savollariga yozma javob bering, lugʻatni toʻldiring.

## Maʼruzani faol oʻqish

Matnni shunchaki oʻqish tez unutiladi. Quyidagi usuldan foydalaning:

- **Savol bilan boshlang.** "Nima uchun bu mavzu muhim?" boʻlimini oʻqib, uni bitta savolga aylantiring (masalan, "Nima uchun MD5 bilan parol saqlash xavfli?") va oxirida unga javob bering.
- **Jadvallarni qayta chizing.** Taqqoslash jadvallarini (masalan, DAC va MAC) xotiradan daftarga yozing, keyin matn bilan solishtiring.
- **Misolni oʻzgartiring.** Ishlangan misolning kirish maʼlumotini oʻzgartirib, natijani oldindan taxmin qiling, keyin dastur bilan tekshiring.
- **Oʻz soʻzingiz bilan tushuntiring.** Har bir mavzuni 3–4 jumlada, atamasiz, oila aʼzosiga tushuntirgandek yozing. Tushuntira olmagan joyingiz — qayta oʻqish kerak boʻlgan joy.
- **Bogʻlang.** Har bir yangi mavzuni oldingisi bilan bogʻlang: "TLS da qaysi kriptografik gʻishtlar ishlatiladi?"

## Uyda amaliyot uchun vositalar

| Vosita | Nima uchun | Qaysi mavzular |
|---|---|---|
| **CyberChef** (brauzerda, oʻrnatishsiz) | shifrlash, kodlash, xesh, HMAC | M5–M8, A5–A8 |
| **OpenSSL** (Git Bash, Linux, macOS da bor) | kalitlar, shifrlash, imzo, sertifikatlar | A6–A9 |
| **VirtualBox** + Linux (Ubuntu) virtual mashinasi | Linux huquqlari, foydalanuvchilar, xavfsiz sinov muhiti | A11, keyingi mavzular |
| **Windows sinov hisobi** yoki Windows virtual mashinasi | parol va bloklash siyosati, hodisalar jurnali | A10 |
| **Parol menejeri** (Bitwarden, KeePassXC) | oʻz hisoblaringizni himoyalash | M10 |
| **Platformadagi kod muharriri** | Python, Java, C++ da topshiriqlar | barcha dasturiy topshiriqlar |

Virtual mashina — eng muhim vosita: unda sozlamalarni bemalol oʻzgartirib, xato qilsangiz "snapshot" dan qaytasiz va asosiy kompyuteringizga zarar yetmaydi.

## Axloqiy va huquqiy chegaralar

Kiberxavfsizlik bilimi ikki tomonlama: himoya uchun oʻrganilgan narsa zarar uchun ham ishlatilishi mumkin. Shuning uchun quyidagi qoidalar majburiy:

- Faqat **oʻzingizga tegishli** yoki **yozma ruxsat** berilgan tizimlarda sinov oʻtkazing (oʻquv virtual mashinasi, oʻqituvchi koʻrsatgan laboratoriya).
- Universitet, ish joyi yoki boshqa birovning tarmogʻi, sayti, hisobini "tekshirib koʻrish" — ruxsatsiz kirish; Oʻzbekiston Jinoyat kodeksida kompyuter axboroti sohasidagi jinoyatlar uchun javobgarlik belgilangan (M3).
- Zaiflik topsangiz — uni tarqatmang, tizim egasiga yoki oʻqituvchiga xabar bering.
- Boshqalarning shaxsiy maʼlumotlari, parollari va yozishmalarini oʻqimang va saqlamang.

## Tayanch tushunchalar lugʻati

Lugʻatni daftarda yoki jadvalda yuriting: atama, oʻz soʻzingiz bilan taʼrif, misol, qaysi darsda.

| Atama | Qisqa taʼrif |
|---|---|
| Konfidensiallik, yaxlitlik, foydalanish mumkinligi (CIA) | axborot xavfsizligining uchta asosiy xossasi |
| Tahdid, zaiflik, risk | xavf manbai; tizimdagi kamchilik; zarar ehtimoli va hajmi |
| Shifrlash, deshifrlash, kalit | maʼlumotni maxfiylashtirish va tiklash; jarayonni boshqaruvchi sir |
| Simmetrik va asimmetrik kriptografiya | bitta umumiy kalit; ochiq va yopiq kalit juftligi |
| Xesh-funksiya | istalgan maʼlumotdan qatʼiy uzunlikdagi "barmoq izi" |
| ERI | yopiq kalit bilan yaratiladigan, ochiq kalit bilan tekshiriladigan imzo |
| Identifikatsiya, autentifikatsiya, avtorizatsiya | kimligini aytish; isbotlash; ruxsatlarni aniqlash |
| MFA | kamida ikki turli autentifikatsiya omili |
| Eng kam imtiyoz | faqat vazifa uchun zarur huquqlar |
| Xavfsizlik devori, IDS, VPN | trafikni siyosat boʻyicha filtrlash; hujumni aniqlash; shifrlangan tunnel |

## Modullar boʻyicha oʻz-oʻzini tekshirish

**1-modul.** CIA triadasining har bir xossasini buzuvchi bittadan hodisa misolini keltiring. Ehtimollik 4, taʼsir 5 boʻlgan risk darajasi qancha va u qaysi toifaga kiradi?

**2-modul.** Nima uchun AES ECB rejimida ishlatilmaydi? RSA da *n* ni koʻpaytuvchilarga ajratish nima uchun qiyin boʻlishi kerak? MAC va ERI farqini tushuntiring.

**3-modul.** NIST nima uchun parollarni davriy majburiy almashtirishni tavsiya etmaydi? Bell–LaPadula modelining ikki qoidasini ayting. Nima uchun diskni formatlash maʼlumotni yoʻq qilmaydi?

**4-modul.** Xavfsizlik devorida qoidalar tartibi nima uchun muhim? VPN qanday himoya beradi va nimadan himoya qilmaydi?

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat imtihon oldidan oʻqish.** Kriptografiya va tarmoqlar ketma-ket mavzular; toʻplangan boʻshliqlarni bir kechada yopib boʻlmaydi.
- **Buyruqlarni oʻqib, bajarmaslik.** `chmod`, `openssl`, `net accounts` faqat qoʻl bilan bajarilganda esda qoladi.
- **Asosiy kompyuterda tajriba qilish.** Xavfsizlik sozlamalari bilan tajriba — faqat virtual mashinada.
- **Ruxsatsiz "tekshirish".** Birovning tizimida sinov — qonunbuzarlik, hatto niyat yaxshi boʻlsa ham.
- **Javobni koʻchirish.** Dasturiy topshiriqning yechimini koʻchirsangiz, keyingi mavzular uchun asos hosil boʻlmaydi.
:::

:::tip
Har hafta bitta "kiberxavfsizlik yangiligi"ni (masalan, katta maʼlumot sizishi yoki muhim zaiflik haqida) oʻqib, uni darsdagi tushunchalar bilan tahlil qiling: qaysi xossa buzildi, qaysi chora yordam berardi. Bu nazariyani real hayot bilan bogʻlashning eng yaxshi usuli.
:::

## Xulosa

- Fan toʻrt moduldan iborat va ular bir-biriga tayanadi: risklar → kriptografiya → foydalanishni boshqarish → tarmoq va bulut.
- Har bir mavzu uchun: oldindan koʻz yugurtirish, faol oʻqish, amaliyotni takrorlash, oʻz-oʻzini tekshirish.
- Uydagi vositalar: CyberChef, OpenSSL, virtual mashina, platformadagi kod muharriri.
- Sinovlar faqat oʻz yoki ruxsat etilgan tizimlarda; topilgan zaiflik egasiga xabar qilinadi.
- Lugʻat va yozma javoblar bilimni mustahkamlaydi.

## Oʻzingizni tekshiring

:::check
Q: Nima uchun kriptografiya moduli autentifikatsiya va tarmoq mavzularidan oldin oʻrganiladi?
A: Autentifikatsiya (parol xeshlari, ERI, FIDO2), VPN va TLS kriptografik algoritmlarga asoslanadi.
Q: Xavfsizlik sozlamalari bilan tajriba qilish uchun eng xavfsiz muhit qaysi?
A: Virtual mashina — xato boʻlsa, snapshot dan qaytish mumkin va asosiy tizimga zarar yetmaydi.
Q: Universitet tarmogʻidagi zaiflikni "tekshirib koʻrish" mumkinmi?
A: Yoʻq, faqat tizim egasining yozma ruxsati bilan; aks holda bu ruxsatsiz kirish hisoblanadi.
Q: Faol oʻqishning qaysi usuli tushunmagan joyingizni eng tez koʻrsatadi?
A: Mavzuni oʻz soʻzingiz bilan, atamasiz tushuntirishga urinish.
:::

:::link https://gchq.github.io/CyberChef/
CyberChef
Shifrlash, kodlash va xesh bilan ishlash uchun bepul onlayn vosita — kriptografiya modulini uyda takrorlash uchun (ingliz tilida).
:::
