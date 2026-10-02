-- ============================================================================
-- V903 (DEV ONLY) — Lesson material for the two seeded courses.
--
-- ⚠️  DEVELOPMENT CONTENT, NOT AN AIFU ACADEMIC DELIVERABLE.
-- It exists so the reading view, the teacher authoring view and the sandbox flow
-- can be exercised against a populated catalogue. Real ma'ruza material is
-- authored by teachers through POST /api/v1/teacher/lessons/{id}/blocks.
--
-- Every lesson is written as a short teaching sequence rather than a term list:
--   hook → why it matters → explanation → key terms → worked code → step-by-step
--   solution → the mistake beginners make → self-check questions → one source.
--
-- Payloads follow the CANONICAL shape produced by the per-type validators in
-- curriculum.app.content (this is what the API writes, so the UI reads it):
--   TEXT         payload {html}
--   CALLOUT      settings {variant} payload {html}
--   CODE_EXAMPLE settings {language} payload {code, explanation}
--   LINK         settings {url} payload {label}
--
-- Deliberately absent: IMAGE/VIDEO/AUDIO/FILE. Those are file-backed (file_id)
-- and the `files` module has no Java implementation yet, so there is nothing to
-- serve. EMBED needs a real provider id; inventing one yields a broken player.
--
-- IAKTT1105 · M7 already carries V902's fixture blocks and is left untouched.
-- ============================================================================

CREATE TEMP TABLE seed_content (
    subject    text NOT NULL,
    lesson     text NOT NULL,
    hook       text NOT NULL,
    why        text NOT NULL,
    explain    text[] NOT NULL,
    concepts   text[] NOT NULL,
    mistake    text NOT NULL,
    checkin    text[] NOT NULL,
    worked     text[] NOT NULL,
    code       text,
    lang       text,
    codewalk   text[],
    link_label text NOT NULL,
    link_url   text NOT NULL
);

-- --------------------------------------------------------------------------
-- DAS1110 · Dasturlash — 12 ma'ruza
-- --------------------------------------------------------------------------
INSERT INTO seed_content
(subject, lesson, hook, why, explain, concepts, mistake, checkin, worked, code, lang, codewalk, link_label, link_url) VALUES

('DAS1110','M1',
 '<p>Tasavvur qiling: buxgalter oyiga bir marta 400 ta hujjatni qoʼlda tekshiradi. Bitta xato — butun hisobot qaytadan. Agar tekshirish qoidasi bir marta yozib qoʼyilsa, kompyuter uni million marta, charchamasdan va bir xil aniqlikda bajaradi. Aynan shu farq dasturlashni paydo qildi.</p>',
 '<p>Iqtisodchi kod yozuvchi boʼlishi uchun emas, <strong>jarayonni qadamlarga ajrata olishi</strong> uchun dasturlashni oʼrganadi. Bir marta toʼgʼri yozilgan qoida vaqt tejaydi, xatoni kamaytiradi va natijani qayta tekshirish imkonini beradi.</p>',
 ARRAY[
  '<h2>Algoritm — dasturning mazmuni</h2><p>Algoritm — bu masalani yechish uchun belgilangan tartibda bajariladigan <strong>chekli sonli qadamlar</strong>. U dasturlash tiliga bogʼliq emas: choy quyish algoritmini qogʼozda ham, Pythonʼda ham yozish mumkin. Algoritm toʼrt xususiyatga ega boʼladi: <em>aniqlik</em> (har qadam bir maʼnoli), <em>omaviylik</em> (kirish qiymati oʼzgarsa ham ishlaydi), <em>natijadorlik</em> (chekli qadamdan keyin javob beradi) va <em>cheklilik</em> (hech qachon abadiy davom etmaydi).</p>',
  '<p><strong>Dastur</strong> — shu algoritmning maʼlum bir tilda yozilgan, kompyuter bajaradigan koʼrinishi. Algoritm fikr, dastur esa uning ijrosi. Bitta algoritmnii Python, Go yoki C++ da yozish mumkin — natija bir xil, kod boshqa.</p>',
  '<h2>Sintaksis va semantika</h2><p><strong>Sintaksis</strong> — tilning yozish qoidasi: qavs, vergul, chekinish. Qoida buzilsa dastur umumen ishga tushmaydi, kompyuter xatoni darhol aytadi. <strong>Semantika</strong> — yozilgan nima maʼnoni anglatishi. Sintaksisi mukammal dastur notoʼgʼri javob berishi mumkin; bunday xatoni kompyuter sezmaydi, uni inson tekshiradi.</p>',
  '<p>Bu ikkinchisi iqtisodiy ishda koʼp uchraydi: jadvaldagi formula <em>ishlaydi</em>, lekin notoʼgʼri ustunni yigʼadi. Son chiqadi, javob esa xato.</p>',
  '<h2>Kompilyator va interperator</h2><p><strong>Kompilyator</strong> butun dasturni oldindan mashina kodiga aylantiradi (Go, C++, Rust). Ishga tushirish tez boʼladi, xato esa hali dasturni ishga tushirguncha topiladi. <strong>Interperator</strong> satrma-satr tarjima qilib bajaradi (Python, JavaScript). Sinab koʼrish qulay, lekin har bajarishda tarjima xarajati takrorlanadi.</p>'
 ],
 ARRAY[
  '<strong>Algoritm</strong> — masalani yechishning chekli, aniq qadamlar ketma-ketligi.',
  '<strong>Dastur</strong> — algoritmning biror tilda yozilgan, kompyuter bajaradigan ifodasi.',
  '<strong>Sintaksis</strong> — tilda yozish qoidalari; buzilsa dastur ishga tushmaydi.',
  '<strong>Semantika</strong> — yozilgan ifodaning maʼnosi; xatosi sekin paydo boʼladi.',
  '<strong>Kompilyator</strong> — dasturni toʼliq mashina kodiga aylantiradi (Go).',
  '<strong>Interperator</strong> — kodni satrma-satr bajaradi (Python).',
  '<strong>Kirish / chiqish</strong> — dasturga beriladigan maʼlumot va uning qaytargan natijasi.'
 ],
 '<p>Boshlangʼich dasturchining eng keng tarqagan xatosi — <strong>oʼylashni kod yozish bilan almashtirish</strong>. Klaviaturaga oʼtirib, masalani qogʼozda yechmasdan dasturlash boshlanadi; natijada yarim tushunilgan mantiq qayta-qayta tuzatiladi. Toʼgʼri tartib: avval qadamlarni yozish, keyin tilga oʼtkazish.</p>',
 ARRAY[
  'Oʼz kasbingizdagi bir jarayonni toʼrt qadamli algoritmga aylantiring. Qaysi qadam shartga bogʼliq?',
  'Sintaksis xatosi bilan semantik xatoni bitta misolda farqlang.',
  'Nega bitta algoritmni turli tillarda yozish mumkin? Bu nimani anglatadi?',
  'Interperator oʼrniga kompilyator tanlashning bitta afzalligi va bitta kamchiligi nima?'
 ],
 ARRAY[
  'Masalani tanlang: berilgan uch sonning yigʼindisini topish.',
  'Qadamlarni qogʼozda yozing: sonlarni oʼqish → qoʼshish → natijani chiqarish.',
  'Har bir qadamga bir buyruq moslaymiz: input(), qoʼshish amali, print().',
  'Dasturni ishga tushiramiz va kirishni oʼzgartirib tekshiramiz — algoritm omaviy boʼlishi kerak.'
 ],
 'n1 = int(input("1-son: "))
n2 = int(input("2-son: "))
n3 = int(input("3-son: "))
yigindi = n1 + n2 + n3
print("Yigʼindi:", yigindi)',
 'python',
 ARRAY[
  '<code>input()</code> klaviaturadan matn oʼqiydi; <code>int()</code> uni butun songa aylantiradi.',
  '<code>yigindi = n1 + n2 + n3</code> — qoʼshish natijasi oʼzgaruvchida saqlanadi, keyin uni yana ishlatish mumkin.',
  '<code>print()</code> natijani ekranga chiqaradi; verguldan keyingi matn ham birga bosiladi.',
  'Dastur uch marta oʼqib, bitta amalni bajaradi — bu yuqoridagi uch qadamli algoritmning aynan kodga koʼchirilishi.'
 ],
 'Python rasmiy darsligi — birinchi boʼlim: asoslar', 'https://docs.python.org/3/tutorial/introduction.html'
),

('DAS1110','M2',
 '<p>Odamlar bilan suhbatda bitta soʼzning turli maʼnoda ishlatilishi mumkin. Kompyuter bilan shunday boʼlmaydi: u faqat aniq buyruqni tushunadi. Shuning uchun har bir dasturlash tili qatʼiy <em>tuzilmaga</em> ega — xuddi tilning grammatikasi kabi.</p>',
 '<p>Tilning tuzilishini bilish — xatoni tez topish demakdir. Iqtisodiy tahlilda maʼlumot turining notoʼgʼri tanlanishi hisob-kitobni butunlay buzadi: matn koʼrinishidagi son qoʼshilmaydi, biriktirib qoʼyiladi.</p>',
 ARRAY[
  '<h2>Dasturlash tilining qatlamlari</h2><p>Har qanday til uch qatlamdan tuziladi: <strong>leksiya</strong> (kalit soʼzlar va identifikatorlar), <strong>sintaksis</strong> (ularni qanday biriktirish) va <strong>semantika</strong> (biriktirma qanday ishlaydi). Kod oʼqiganda shu uch qatlamni ajratish koʼnikmasi xatoni ikki marta tez topdiradi.</p>',
  '<p><strong>Maʼlumot turi</strong> — qiymatlar toʼplami va ular ustida ruxsat etilgan amallar. Butun son (<code>int</code>) boʼlinmaydi, haqiqiy son (<code>float</code>) kasr beradi, matn (<code>str</code>) qoʼshiladi lekin koʼpaytirilmaydi, mantiqiy qiymat (<code>bool</code>) faqat shartda ishlaydi. Statik tipli tillarda (Go) tur kod yozilganda belgilanadi va buzilsa dastur ishga tushmaydi; dinamik tillarda (Python) tur ishla paytda oʼzgaradi — xato kechroq, sekinroq ochiladi.</p>',
  '<p><strong>Operator va ifoda.</strong> Operator — amalni bildiradigan belgi (<code>+ - * / // % == and or not</code>). Ifoda — qiymat qaytaradigan har qanday yozuv. <code>a + b</code> ifoda, <code>x = a + b</code> esa buyruq: ifodaning natijasi nomga biriktiriladi.</p>',
  '<p><strong>Buyruq va blok.</strong> Pythonda buyruqlar yangi satrga oʼtadi, blok esa chekinish (indentation) bilan belgilanadi. Goda bloki <code>{ }</code> qavslari bilan yopiladi. Bu farq tufayli Pythonʼda boʼsh joy — sintaksis, boshqa tillarda esa faqat chiroy.</p>',
  '<p><strong>Standart kutubxona</strong> — til bilan birga keladigan, sinovdan oʼtgan funksiyalar toʼplami. Oʼn qator kod yozish oʼrniga <code>math.sqrt</code> yoki Goʼda <code>fmt.Println</code> chaqirish xato ehtimolini kamaytiradi.</p>'
 ],
 ARRAY[
  '<strong>Identifikator</strong> — oʼzgaruvchi, funksiya yoki sinfga berilgan nom.',
  '<strong>Kalit soʼz</strong> — til oʼzi ajratib qoʼygan nom (<code>if</code>, <code>for</code>, <code>func</code>); identifikator boʼla olmaydi.',
  '<strong>Maʼlumot turi</strong> — qiymatlar toʼplami va ustidagi ruxsat etilgan amallar.',
  '<strong>Statik tip</strong> — tur kompilyatsiya vaqtida aniklanadi (Go). <strong>Dinamik tip</strong> — ishla paytida (Python).',
  '<strong>Operator</strong> — amalni bildiruvchi belgi; <strong>ifoda</strong> — qiymat qaytaruvchi yozuv.',
  '<strong>Buyruq / blok</strong> — bajariladigan amal va ularning guruhi.',
  '<strong>Kutubxona</strong> — tayyor, qayta ishlatiladigan funksiyalar toʼplami.'
 ],
 '<p>Koʼp uchraydigan xato — <strong>turni bilmasdan ishlatish</strong>. Pythonʼda <code>"5" + "3"</code> natijasi <code>58</code> emas, <code>"53"</code>: ikki matn bir-biriga ulanadi. Hisob-kitobda bunday xato son chiqqani uchun sezilmaydi. Yechim: kiritilgan qiymatni darhol kerakli turga oʼtkazish (<code>int()</code>, <code>float()</code>) va Goʼda turni ochiq eʼlon qilish.</p>',
 ARRAY[
  '<code>"12" + "8"</code> qanday natija beradi? Nega?',
  'Statik tip xatoni dinamik tipga qaraganda qachon aniqlaydi?',
  'Ifoda bilan buyruqni bir misolda farqlang.',
  'Python bloki nim bilan belgilanadi, Go bloki-chi?'
 ],
 ARRAY[
  'Masala: xarid summasidan 15% chegirma hisoblash.',
  'Kirishni butun emas, haqiqiy son deb olamiz — chegirma kasr beradi: <code>summa = float(input())</code>.',
  'Foizni alohida oʼzgaruvchida saqlaymiz: <code>chegirma = summa * 0.15</code>.',
  'Yakuniy qiymat: <code>summa - chegirma</code>, va natijani ikki xonagacha yaxlitlaymiz: <code>round(..., 2)</code>.',
  'Tur tanlash toʼgʼri boʼlsa, yaxlitlash ham toʼgʼri chiqadi — aks holda pul miqdori notoʼgʼri boʼladi.'
 ],
 'summa = float(input("Xarid summasi: "))
chegirma = summa * 0.15
yakuniy = round(summa - chegirma, 2)
print("Toʼlov:", yakuniy)',
 'python',
 ARRAY[
  '<code>float(input(...))</code> — oʼqilgan matn haqiqiy songa oʼtkaziladi, aks holda koʼpaytirish ishlamaydi.',
  '<code>chegirma</code> alohida saqlanadi: natijani tekshirish va hisobotda koʼrsatish oson boʼladi.',
  '<code>round(x, 2)</code> — pul uchun ikki xona yetarli; yaxlitlashni oxirida qilgan maʼqul, orada emas.',
  'Har bir qatorda bitta ish — kod oʼqiladigan va xatosi topiladigan boʼladi.'
 ],
 'Go — turlar va operatorlar boʼlimi (Tour of Go)', 'https://go.dev/tour/basics/1'
),

('DAS1110','M3',
 '<p>Bir bank kredit arizasini qaraydi. Daromad maʼlum chegaradan yuqorimi — avtomatik tasdiq; pastmi — qoʼlda tekshiruv; hujjat kammi — rad. Uch xil holat, bitta qaror jarayoni. Kompyuterga qaror berishni oʼrgatish — aynan shu tarmoqlanishni yozishdir.</p>',
 '<p>Iqtisodiy qarorlarning aksariyati shartga asoslanadi. Shartni kodga oʼtkaza olgan mutaxassis nafaqat hisoblaydi, balki <em>jarayonni avtomatlashtiradi</em>.</p>',
 ARRAY[
  '<h2>Shart — mantiqiy qiymat</h2><p>Tarmoqlanishning yuragi <strong>shart ifodasi</strong>: hisoblanganda <code>True</code> yoki <code>False</code> qaytaradigan yozuv. U solishtirish operatorlari bilan tuziladi (<code>== != &gt; &lt; &gt;= &lt;=</code>) va mantiqiy operatorlar bilan birlashtiriladi (<code>and or not</code>).</p>',
  '<p><strong>Toʼliq tarmoqlanish</strong> — <code>if / else</code>: shart bajarilsa birinchi, boʼlmasa ikkinchi blok ishlaydi. Bir tarmogʼi albatta bajariladi. <strong>Qisqa tarmoqlanish</strong> — faqat <code>if</code>: shart bajarilmasa hech narsa sodir boʼlmaydi. Qaysi birini tanlash masalaning maʼnosidan chiqadi: "aks holda nima boʼlishi kerak?" savoliga javob boʼlsa — <code>else</code> kerak.</p>',
  '<p><strong>Koʼp tarmoqli tanlash.</strong> Bir necha holat boʼlganda <code>if / elif / else</code> zanjiri ishlatiladi. Zanjir yuqoridan pastga tekshiriladi va <strong>birinchi toʼgʼri kelgan</strong> tarmoq bajarilib, qolgani oʼtkazib yuboriladi. Shuning uchun tartib javobgar: <code>&gt;= 90</code> ni <code>&gt;= 70</code> dan oldin yozish shart.</p>',
  '<p><strong>Blok va chekinish.</strong> Pythonda <code>if</code> satridan keyin ikki yoki toʼrt probel bilan chekinilgan qatorlar shu tarmoqqa tegishli. Xato chekinish — eng koʼp uchraydigan sintaksis muammosi.</p>',
  '<p><strong>Ichma-ich tarmoqlanish</strong> mumkin, lekin u oʼqishni qiyinlashtiradi. Ikki darajadan chuqur boʼlsa, kodni funksiyalarga boʼlish vaqtı keldi degani.</p>'
 ],
 ARRAY[
  '<strong>Shart ifodasi</strong> — mantiqiy qiymat qaytaradigan ifoda.',
  '<strong>Toʼliq tarmoqlanish</strong> — <code>if / else</code>, bitta tarmoq albatta bajariladi.',
  '<strong>Qisqa tarmoqlanish</strong> — faqat <code>if</code>, shart bajarilmasa amal boʼlmaydi.',
  '<strong>Zanjir (<code>elif</code>)</strong> — birinchi toʼgʼri kelgan tarmoq bajariladi, qolgani tekshirilmaydi.',
  '<strong>Guard clause</strong> — notoʼgʼri holatni boshidanoq chiqarib tashlash.',
  '<strong>Qisqa tutashuv</strong> — <code>and</code> yolgʼon boʼlsa ikkinchi qism, <code>or</code> koʼrinilsa keyingi qism hisoblanmaydi.'
 ],
 '<p>Eng keng xato — <strong>chegaralarni chalkashtirish</strong>. <code>if ball &gt;= 70 ... elif ball &gt;= 90 ...</code> deb yozilsa, 95 balli talaba ham birinchi tarmoqqa tushadi va "yaxshi" oladi. Zanjir qatʼiy <strong>yuqoridan pastga</strong> tekshirilishini esda tuting; chegaralarni kamayib boradigan tartibda yozing.</p>',
 ARRAY[
  'Nega <code>elif</code> zanjirida tartib javobgar?',
  '<code>if</code> va <code>if...else</code> qachon bir xil natija beradi?',
  '<code>not (a and b)</code> ifodasini De Morgan qonuni bilan soddalashtiring.',
  'Ichma-ich <code>if</code> oʼrniga qanday yozish oʼqishni osonlashtiradi?'
 ],
 ARRAY[
  'Masala: ball boʼyicha baho qoʼyish (0–100).',
  'Avval notoʼgʼri kirishni chiqaramiz — guard clause: <code>if ball &lt; 0 or ball &gt; 100</code>.',
  'Keyin eng yuqori chegaradan pastga qarab yozamiz: 90, 70, qolganida qoniqarli.',
  'Har tarmoq faqat bitta ishni qaytarsin — natijani oʼzgaruvchiga yozib, oxirida bir marta chiqaramiz.'
 ],
 'ball = int(input("Ball: "))

if ball &lt; 0 or ball &gt; 100:
    baho = "notoʼgʼri qiymat"
elif ball &gt;= 90:
    baho = "aloqi"
elif ball &gt;= 70:
    baho = "yaxshi"
else:
    baho = "qoniqarli"

print(baho)',
 'python',
 ARRAY[
  'Birinchi <code>if</code> — guard clause: cheklovdan tashqari qiymat davom etmaydi.',
  '<code>elif ball &gt;= 90</code> — yuqori chegara avval tekshiriladi, aks holda pastgi chegara uni yutadi.',
  '<code>else</code> — barcha holatni qoplaydi: 0..69 oraliq.',
  'Natija bitta oʼzgaruvchida yigʼiladi va oxirida bir marta bosiladi — chiqish nuqtasi bitta boʼlsa, tekshirish oson.'
 ],
 'Python — shartli bajarilish rasmiy boʼlimi', 'https://docs.python.org/3/tutorial/controlflow.html#if-statements'
),

('DAS1110','M4',
 '<p>Omborda 12 000 mahsulot qatori bor va har birining qiymati oʼzgaradi. Har birini qoʼlda koʼrib chiqish — bir kunlik ish. Bir xil amalni takrorlaydigan besh qator kod esa uni soniyada bajaradi. Lekin takrorlash — nazoratsiz qoʼyilsa xavfli: cheksiz sikl butun tizimni toʼxtatadi.</p>',
 '<p>Takrorlash — dasturlashning eng katta kuch tejaydigan vositasi. Iqtisodiy hisob-kitobda ham, statistik tahlilda ham maʼlumot qatorlari bilan ishlash shu prinsipga tayanadi.</p>',
 ARRAY[
  '<h2>Sikl nima uchun kerak</h2><p>Sikl — bir yoki bir nechta buyruqni <strong>shart bajarilguncha</strong> qaytarish. Har takrorlanish <em>iteratsiya</em> deb ataladi. Asosiy savol doim bitta: <strong>qancha marta?</strong> Javob oldindan maʼlum boʼlsa — parametrli sikl, nomaʼlum boʼlsa — shartli sikl tanlanadi.</p>',
  '<p><strong>Parametrli sikl (<code>for</code>).</strong> <code>for i in range(n)</code> — n marta takrorlanadi. <code>range</code> oxirgi chegarani <em>oʼz ichiga olmaydi</em>: <code>range(1, 5)</code> → 1, 2, 3, 4. Bu "yarim ochiq oraliq" boshlangʼichlar sonini toʼgʼri hisoblash uchun tanlangan.</p>',
  '<p><strong>Old shartli sikl (<code>while</code>).</strong> Avval shart tekshiriladi, keyin bajariladi — shart dastlabdan yolgʼon boʼlsa, sikl bir marta ham ishlamaydi. Hisoblagichni (<code>i += 1</code>) oʼzgartirish unutilsa, shart hech qachon buzilmaydi va dastur abadiy aylanadi.</p>',
  '<p><strong>Soʼng shartli sikl.</strong> Klassik <code>do...while</code> avval bir marta bajarib, keyin tekshiradi — "kamida bir marta soʼrash" kerak boʼlganda qulay. Goʼda bunday operator yoʼq: <code>for ... { if ... break }</code> andozasi ishlatiladi.</p>',
  '<p><strong>Siklni uzish.</strong> <code>break</code> sikldan butunlay chiqadi, <code>continue</code> esa keyingi iteratsiyaga oʼtadi. Javob topilganda ishni davom ettirmaslik — katta maʼlumotda katta vaqt tejami.</p>'
 ],
 ARRAY[
  '<strong>Iteratsiya</strong> — siklning bir marta bajarilishi.',
  '<strong>Hisoblagich</strong> — nechta qadam oʼtganini saqlovchi oʼzgaruvchi.',
  '<strong>Accumulator</strong> — har iteratsiyada natijani toʼplaydigan oʼzgaruvchi.',
  '<strong>Parametrli sikl</strong> — <code>for</code>: qadamlar soni oldindan aniq.',
  '<strong>Old shartli sikl</strong> — <code>while</code>: bir marta ham bajarilmasligi mumkin.',
  '<strong>break / continue</strong> — sikldan chiqish / keyingi qadamga oʼtish.',
  '<strong>Cheksiz sikl</strong> — chiqish sharti hech qachon bajarilmaydigan holat.'
 ],
 '<p>Eng xavfli xato — <strong>chiqish shartini unutish</strong>. <code>while n &gt; 0:</code> deb yozib, tanasida <code>n</code> ni kamaytirmasak, dastur toʼxtamaydi: sandboxʼda vaqt chegarasiga uriladi, ishlab turgan tizimda resursni yeb qoʼyadi. Ikkinchi keng xato — <strong>off-by-one</strong>: <code>range(n)</code> 0 dan boshlanib n−1 da tugaydi, <code>range(1, n)</code> esa n ni olmaydi. Chegarani doim qoʼlda bir kichik holatda tekshiring.</p>',
 ARRAY[
  '<code>range(2, 10, 2)</code> qanday qiymatlar beradi?',
  '<code>while</code> sikli nechta marta bajarilmasligi mumkin va nega?',
  'Javob birinchi topilganda qaysi operator ishlatiladi?',
  '<code>for</code> ni qachon <code>while</code> ga almashtirish toʼgʼri keladi?'
 ],
 ARRAY[
  'Masala: 1 dan n gacha sonlar yigʼindisi.',
  'Accumulator tayyorlaymiz: <code>jami = 0</code>.',
  '<code>for i in range(1, n + 1)</code> — n kirishi uchun +1 kerak, chunki oxirgi chegara oʼrinmaydi.',
  'Har iteratsiyada qoʼshamiz: <code>jami += i</code>.',
  'Sikldan keyin natijani chiqaramiz — sikl ichida emas, aks holda n qator bosiladi.'
 ],
 'n = int(input("n = "))
jami = 0

for i in range(1, n + 1):
    jami += i

print("1..", n, "yigʼindisi:", jami)',
 'python',
 ARRAY[
  '<code>jami = 0</code> — boshlangʼich qiymat shart: boʼsh oʼzgaruvchi bilan qoʼshish ishlamaydi.',
  '<code>range(1, n + 1)</code> — 1..n oraligʼini toʼliq qamrab oladi.',
  '<code>jami += i</code> — <code>jami = jami + i</code> ning qisqa yozuvi; har qadamda toʼplanadi.',
  '<code>print</code> sikl tarkidan tashqarida — chekinish bu yerda mantiqni bildiradi.'
 ],
 'Python — sikllar boʼlimi (official tutorial)', 'https://docs.python.org/3/tutorial/introduction.html#loops'
),

('DAS1110','M5',
 '<p>Sinf jurnali — 30 talabaning baholari. Har birini alohida oʼzgaruvchi deb yozsak, 30 nom va ularga bir xil ish qilish uchun 30 qator kerak boʼladi. Bitta nom ostida qator saqlab, barchasiga bir sikl yetarli. Maʼlumotni <em>guruhlab</em> saqlash — dasturlashning kundalik asosi.</p>',
 '<p>Har qanday hisobot, byudjet yoki savdo tizili qatorlar bilan ishlaydi. Qatorni samarali boshqarish — maʼlumot tahlilining texnik poydevori.</p>',
 ARRAY[
  '<h2>Indeks va uzunlik</h2><p>Massiv (roʼyxat) — bitta nom ostidagi <strong>tartiblangan</strong> qiymatlar toʼplami. Har bir element tartib raqami — <strong>indeks</strong> — orqali topiladi. Koʼp tillarda, Python va Go da ham, indeks <strong>0 dan</strong> boshlanadi: 5 elementli roʼyxatda indekslar 0..4. Oxirgi elementga <code>[-1]</code> bilan murojaat qilinadi.</p>',
  '<p><strong>Uzunlik</strong> — elementlar soni, <code>len(a)</code>. U bilan indeks bir xil narsa emas: uzunlik 5 boʼlsa, maksimal indeks 4. Shu bittа farq "off-by-one" xatolarining yarmini tuzadi.</p>',
  '<p><strong>Slice</strong> — ketma-ket qismni olish: <code>a[1:4]</code> 1, 2, 3 indekslarini beradi; 4 kirmaydi. Uchinchi argument qadam: <code>a[::2]</code> — har ikkinchi element. Slice yangi roʼyxat yaratadi, eskirini oʼzgartirmaydi.</p>',
  '<h2>Qidiruv — ikki usul</h2><p><strong>Chiziqli qidiruv</strong> — boshdan oxirgacha koʼrib chiqish. Saralanmagan roʼyxatda yagona yoʼl; eng yomon holatda n qadam, yaʼni O(n).</p>',
  '<p><strong>Ikkilik qidiruv</strong> — faqat <em>saralangan</em> roʼyxatda ishlaydi. Oʼrtadagi element bilan solishtirib, qidiruv maydonini har qadamda ikkiga boʼladi: 1 000 000 element ≈ 20 qadam, yaʼni O(log n). Bu — algoritm tanlash iqtisodiy resursni qanday tejashini koʼrsatadigan eng yorqin misol.</p>'
 ],
 ARRAY[
  '<strong>Indeks</strong> — elementning tartib raqami, 0 dan boshlanadi.',
  '<strong>Uzunlik</strong> — elementlar soni; maksimal indeks = uzunlik − 1.',
  '<strong>Slice</strong> — <code>a[bosh:oxir:qadam]</code>, oxirgi chegara kirmaydi.',
  '<strong>Chiziqli qidiruv</strong> — O(n), saralash talab qilmaydi.',
  '<strong>Ikkilik qidiruv</strong> — O(log n), faqat saralangan maʼlumotda.',
  '<strong>Murakkablik (Big-O)</strong> — qadamlar sonining kirish hajmiga bogʼliqligi.'
 ],
 '<p>Chegaradan chiqish (<code>IndexError</code>) — eng koʼp uchraydigan halokat. <code>a[len(a)]</code> ga murojaat qilish xato, chunki soni 5 boʼlsa, oxirgi indeks 4. Shuningdek, <strong>saralanmagan roʼyxatda ikkilik qidiruv ishlatish</strong> xato javob beradi — u tuxum qoʼymaydi, jim javob bermaydi. Qidiruvdan oldin saralanganini tekshiring yoki saralang.</p>',
 ARRAY[
  'Uzunligi 8 boʼlgan roʼyxatning toʼrtinchi elementi qaysi indeksda?',
  '<code>a[2:5]</code> nechta element qaytaradi?',
  'Ikkilik qidiruv nega saralashni talab qiladi?',
  '100 000 elementni chiziqli qidirish taxminan nechta qadam, ikkilik-chi?'
 ],
 ARRAY[
  'Masala: roʼyxatdagi eng katta qiymatni topish.',
  'Birinchi elementni "eng katta" deb olamiz: <code>eng_katta = a[0]</code>.',
  'Ikkinchisidan boshlab har birini solishtiramiz: <code>for x in a[1:]</code>.',
  'Kattaroq boʼlsa yangilaymiz: <code>eng_katta = x</code>.',
  'Sikl tugagach natija — javob. Bu "choʼqqi" andozasi koʼp masalaga moslashadi.'
 ],
 'baholar = [78, 92, 65, 88, 71, 95]

eng_katta = baholar[0]
uchunqi = 0

for b in baholar:
    if b &gt; eng_katta:
        uchunqi += 1
        eng_katta = b

print("Eng yuqori ball:", eng_katta)
print("Solishtirishlar:", uchunqi)',
 'python',
 ARRAY[
  '<code>baholar[0]</code> — boʼsh roʼyxatda bu xato beradi; real kodda avval <code>len()</code> tekshiriladi.',
  'Sikl har elementni bir marta koʼradi — jami 6 qadam, yaʼni O(n).',
  '<code>if b &gt; eng_katta</code> — topilgan choʼqqi darhol yangilanadi.',
  '<code>uchunqi</code> oʼsishini kuzatadi: algoritm qancha ish qilganini koʼrsatadi.'
 ],
 'VisuAlgo — chiziqli va ikkilik qidiruvni jonli koʼrish', 'https://visualgo.net/en/binarysearch'
),

('DAS1110','M6',
 '<p>Oylik byudjet: 12 oy × 5 xarid moddasi. Buni 60 ta alohida oʼzgaruvchida saqlash aqlli emas — ularning tartibi yoʼqoladi. Bir jadvalda saqlasak, "har bir oy boʼyicha jami" yoki "har bir modda boʼyicha oʼrtacha" kabi savollarga sikl javob beradi.</p>',
 '<p>Iqtisodiy maʼlumotning tabiati — jadval. Satr va ustun tushunchasini kodda erkin boshqarish tahlilchi uchun asosiy koʼnikmadir.</p>',
 ARRAY[
  '<h2>Matritsa — massivdan keyingi qadam</h2><p>Koʼp oʼlchovli massiv — ichida boshqa massivlar saqlanadigan massiv. Ikki oʼlchovlisi <strong>matritsa</strong> deb ataladi: satrlar va ustunlar. Element <code>m[i][j]</code> bilan olinadi — avval satr, keyin ustun.</p>',
  '<p>Pythonʼda matritsa odatda roʼyxatlar roʼyxati sifatida tuziladi: <code>[[1,2,3], [4,5,6]]</code>. Goʼda esa aniq oʼlcham bildiriladi: <code>[3][4]int</code>. Birinchi indeks — satrlar soni, ikkinchisi — ustunlar.</p>',
  '<p><strong>Ichma-ich sikl.</strong> Tashqi sikl satrlarni, ichki sikl ustunlarni aylanadi. n×m matritsada jami n·m qadam — O(n²). Shu sababli katta matritsa bilan ishlashda har qadamning narxi seziladi.</p>',
  '<p><strong>Satr va ustun boʼyicha amal.</strong> Satr yigʼindisi tabiiy: bitta sikl. Ustun yigʼindisi esa ikki sikl: tashqida ustun, ichida satr. Ustunlar boʼyicha ishlash xotirada "sakrash" qilgani uchun satrlarga qaraganda sekinroq — bu maʼlumot joylash tartibining (row-major) amaliy natijasi.</p>',
  '<p><strong>Noteng matritsa.</strong> Python roʼyxatlari har xil uzunlikdagi qatorlarni saqlay oladi. Bunday holda <code>len(satr)</code> ni har satrda alohida olish kerak — aks holda indeks chegarasidan chiqasiz.</p>'
 ],
 ARRAY[
  '<strong>Matritsa</strong> — ikki oʼlchovli massiv: satr × ustun.',
  '<strong>Satr / ustun</strong> — mos ravishda <code>m[i]</code> va <code>m[*][j]</code>.',
  '<strong>Ichma-ich sikl</strong> — tashqi: satrlar, ichki: ustunlar; jami O(n·m).',
  '<strong>Diagonal</strong> — <code>i == j</code> boʼlgan elementlar.',
  '<strong>Transpozitsiya</strong> — satr va ustunni almashtirish: <code>t[j][i] = m[i][j]</code>.',
  '<strong>Row-major</strong> — qatorlar ketma-ket saqlanadi; satr boʼylab yurish tezroq.'
 ],
 '<p>Matritsa bilan eng koʼp uchraydigan xato — <strong>indeks tartibini chalkashtirish</strong>: <code>m[j][i]</code> oʼrniga <code>m[i][j]</code> yozish. Natija baʼzan baribir son beradi, lekin notoʼgʼri. Ikkinchisi — <strong>oʼlchamni tekshirmaslik</strong>: 3×4 matritsaga 4×3 deb qoʼshilsa, indeks xatosi yoki jim-jit notoʼgʼri javob chiqadi. Hisobga kirishdan oldin satr va ustun sonini solishtirish odat tuzib oling.</p>',
 ARRAY[
  '<code>m[2][1]</code> nimani anglatadi?',
  'Ustun yigʼindisi uchun sikllar qanday tartibda joylashadi?',
  '3×5 matritsada nechta element bor va diagonalda nechta?',
  'Noteng uzunlikdagi satrlar bilan ishlashda nimani hisobga olish kerak?'
 ],
 ARRAY[
  'Masala: har bir oy (satr) boʼyicha jami xaridni topish.',
  'Matritsani tayyorlaymiz: har satr — bir oy, har ustun — bir modda.',
  'Tashqi sikl satrlarni aylanadi.',
  'Ichki siklda oʼsha satr elementlarini toʼplaymiz: <code>jami += oy[j]</code>.',
  'Har satr oxirida natijani chiqaramiz — oylar tartibi saqlanadi.'
 ],
 'byudjet = [
    [120, 80, 45],    # Yanvar: ovqat, transport, kommunikatsiya
    [130, 75, 45],
    [145, 90, 50],
]

moddalar = ["ovqat", "transport", "aloqa"]

for oy in range(len(byudjet)):
    jami = 0
    for ustun in range(len(byudjet[oy])):
        jami += byudjet[oy][ustun]
    print(f"{oy + 1}-oy jami: {jami}")',
 'python',
 ARRAY[
  '<code>byudjet[oy]</code> — bitta oyning butun qatori, oddiy roʼyxat.',
  '<code>len(byudjet[oy])</code> — har satr uzunligi alohida olinadi: noteng holatga ham bardosh beradi.',
  'Ichki siklda <code>jami</code> har oy uchun noldan boshlanadi — tashqi sikl ichida eʼlon qilingani shuni taʼminlaydi.',
  'Notoʼgʼri joylashtirilsa (<code>jami = 0</code> tashqarida boʼlsa) barcha oylar yigʼindisi chiqadi — xato jim boʼlib oʼtadi.'
 ],
 'NumPy — koʼp oʼlchovli massivlar bilan ishlash', 'https://numpy.org/doc/stable/user/basics.indexing.html'
),

('DAS1110','M7',
 '<p>Bir dasturda hisob-kitob uch marta kerak boʼlsa va kod nusxalansa, xato topilganda uch joyi tuzatiladi — va bittasi unutib qolinadi. Funksiya shu muammoni yoʼq qiladi: ish bir marta yoziladi, istalgan joyda chaqiriladi.</p>',
 '<p>Funksiya — dasturni boshqariladigan boʼlaklarga boʼlish vositasi. Katta loyihada kod sifati funksiyaning qanchalik kichik va aniq ekanligi bilan oʼlchanadi.</p>',
 ARRAY[
  '<h2>Parametr, natija va soha</h2><p><strong>Parametr</strong> — funksiyaning kirishi. <strong>Qaytarish (return)</strong> — chiqishi. Funksiya natijani <em>qaytarsa</em>, uni yana ishlatish mumkin; faqat bosib chiqarsa, maʼlumot yoʼqoladi. Shu farq funksiyanani sinovdan oʼtkazish mumkinligini belgilaydi.</p>',
  '<p><strong>Local soha (scope)</strong> — funksiya ichida yaratilgan oʼzgaruvchi tashqarida koʼrinmaydi va aksincha, funksiya tashqi oʼzgaruvchini bilmasdan ishlashi kerak. Bu <em>izolyatsiya</em> xato manbasini toraytiradi: bir funksiyaning ishlagan boshqasiga tegmasligi — yirik loyihaning barqarorlik sharti.</p>',
  '<p><strong>Yon taʼsir (side effect)</strong> — funksiyaning kirishdan tashqari narsalarni oʼzgartirishi: global oʼzgaruvchi, fayl, baza. Yon taʼsir qancha kam boʼlsa, funksiyanii tekshirish shuncha oson. Ideal funksiya: bir kirish → bir natija.</p>',
  '<p><strong>Hujjat satri (docstring).</strong> Pythonʼda funksiyaning birinchi satridagi matn uning vazifasini yozadi. Kodni oʼqib tushunish vaqti yozish vaqtidan koʼp marotaba koʼp — shuning uchun izoh iqtisodiy jihatdan foydali.</p>',
  '<p><strong>Qayta kurishga tayyorgarlik.</strong> Funksiya oʼzini chaqirishi mumkin; keyingi maʼruzada shu holat — rekursiya — alohida koʼrib chiqiladi.</p>'
 ],
 ARRAY[
  '<strong>Parametr</strong> — funksiya qabul qiladigan kirish qiymati.',
  '<strong>Argument</strong> — chaqiruvda berilayotgan aniq qiymat.',
  '<strong>Qaytarish qiymati</strong> — funksiyaning natijasi (<code>return</code>).',
  '<strong>Local / global soha</strong> — oʼzgaruvchi koʼrinadigan chegara.',
  '<strong>Yon taʼsir</strong> — funksiyaning tashqi dunyoni oʼzgartirishi.',
  '<strong>Docstring</strong> — funksiya vazifasini tushuntiruvchi hujjat satri.',
  '<strong>Sof funksiya</strong> — bir xil kirishdan har doim bir xil natija beradi, tashqariga tegmaydi.'
 ],
 '<p>Keng xato — <strong>funksiyani juda katta qilish</strong>: bir vaqtning oʼzida oʼqish, hisoblash va faylga yozish. Bunday funksiyanii sinash qiyin, qayta ishlatish esa imkonsiz. Mezon: funksiya nomini eshitganda uning <em>bitta</em> ishini aytib boʼlishi kerak. Ikkinchi xato — <code>return</code> oʼrniga <code>print</code> ishlatish: natija ekranga chiqadi, lekin hisobda ishlatib boʼlmaydi.</p>',
 ARRAY[
  'Nega <code>return</code> qilingan natija <code>print</code> qilinganidan foydaliroq?',
  'Local oʼzgaruvchini funksiya tashqarisida ishlatib boʼladimi?',
  'Bir funksiya uchta ish qilsa — qanday boʼlish kerak?',
  'Docstring qaysi vaqtni tejaydi?'
 ],
 ARRAY[
  'Masala: sof qiymatni (NVQ) hisoblash — xarajatdan subsidiyani ayirish.',
  'Kirishlarni aniqlaymiz: xarajat va subsidiya.',
  'Hisobni alohida funksiya qilamiz — bitta ish.',
  'Natijani qaytaramiz, bosishni chaqiruvchi tomoniga qoldiramiz.',
  'Turli qiymatlar bilan ikki marta chaqirib tekshiramiz.'
 ],
 'def sof_qiymat(xarajat, subsidiya=0):
    """Xarajatdan subsidiyani ayirib, sof qiymatni qaytaradi."""
    return xarajat - subsidiya


oylik = sof_qiymat(1_250_000, 150_000)
print("Sof qiymat:", oylik)
print("Subsidiyasiz:", sof_qiymat(1_250_000))',
 'python',
 ARRAY[
  '<code>subsidiya=0</code> — standart qiymat: argument berilmasa funksiya baribir ishlaydi.',
  'Docstring funksiya vazifasini bir satrda ochib beradi.',
  '<code>return</code> natijani qaytaradi — uni oʼzgaruvchiga yozib, keyingi hisobda ishlatamiz.',
  'Funksiya hech narsa bosmaydi: chiqarish formatini chaqiruvchi oʼzi tanlaydi.'
 ],
 'Python — funksiyalar va soha qoidalari', 'https://docs.python.org/3/tutorial/controlflow.html#defining-functions'
),

('DAS1110','M8',
 '<p>Yillik kredit toʼlovini hisoblash uchun avval oylik foizni, uning uchun esa kunlik bazani bilish kerak. Har bir katta masala oʼziga oʼxshash, lekin kichikroq masalaga bogʼliq. Bunday holatda sikl emas, rekursiya tabiiy yoziladi.</p>',
 '<p>Rekursiv fikrlash — tarmoqlanuvchi jarayonlarni (hisob-kitob daraxti, tashkilot strukturasi, grafik) modelleshda bevosita ishlatiladi.</p>',
 ARRAY[
  '<h2>Rekursiya qanday ishlaydi</h2><p>Rekursiv funksiya oʼzini chaqiradi. Har chaqiruvda masala kichrayadi va bir nuqtada <strong>bazis holati</strong> — toʼxtash sharti — keladi. Bazissiz rekursiya cheksiz davom etadi va chaqiruv steki toʼlib, dastur halokat bilan toʼxtaydi.</p>',
  '<p>Har bir chaqiruv xotirada alohida qatlam egallaydi: oʼzgaruvchilar, qaytish manzili. Chuqurlik oʼsgan sari xotira sarfi ortadi. Pythonʼda standart cheklov ≈1000 qatlam; undan chuqur rekursiya <code>RecursionError</code> beradi.</p>',
  '<p><strong>Misol — faktorial.</strong> n! = n · (n−1)!. Bazis: 1! = 1. <code>faktorial(5)</code> → 5·faktorial(4) → 5·4·faktorial(3) → ... → 5·4·3·2·1. Chaqiruvlar avval chuqurlashadi, keyin orqaga qaytib koʼpaytiriladi.</p>',
  '<p><strong>Takroriy hisob va kayfiyat (memoization).</strong> Oddiy rekursiv Fibonachchi bir xil qiymatlarni marta-marta hisoblaydi va eksponensial sekinlashadi. Natijalarni saqlab qoʼyish (memoization) yoki siklga oʼtkazish muammoni yoʼqol qiladi.</p>',
  '<p><strong>Qachon rekursiya, qachon sikl?</strong> Masala tabiatan tarmoqli (daraxt, grafdan oʼtish, boʼlib yechish) boʼlsa — rekursiya aniqroq oʼqiladi. Qadamlar soni aniq va chiziqli boʼlsa — sikl tezroq va xotiraga tejamkor.</p>'
 ],
 ARRAY[
  '<strong>Bazis holati</strong> — rekursiya toʼxtaydigan, javob tayyor shart.',
  '<strong>Rekursiv qadam</strong> — masalani kichraytirib funksiyaning oʼziga berish.',
  '<strong>Chaqiruv steki</strong> — har chaqiruvning xotiradagi qatlami.',
  '<strong>Kayfiyat (memoization)</strong> — hisoblangan natijani saqlab, qayta hisoblashni oldini olish.',
  '<strong>Stek toʼlishi</strong> — chuqurlik chegarasidan oshib ketish (stack overflow).',
  '<strong>Iterativ yechim</strong> — rekursiyani sikl bilan almashtirish.'
 ],
 '<p>Bazis shartini unutish — klassik halokat: funksiya cheksiz oʼzini chaqiradi. Yana bir nozik xato — <strong>masala kichraymasligi</strong>: <code>faktorial(n + 1)</code> chaqirilsa bazisga hech qachon yetib borilmaydi. Har rekursiv chaqiruvda kirish aniq kichrayayotganini bir koʼz bilan tekshirib oʼrganing. Katta n da esa <code>RecursionError</code> ga duch kelish mumkin — bunday joyda iterativ yechim yoziladi.</p>',
 ARRAY[
  'Faktorialda bazis holat qaysi va nega aynan u?',
  'Chaqiruv steki nima uchun toʼladi?',
  'Rekursiv Fibonachchi ni sekinlashtiradigan narsa nima?',
  'Qanday masalada sikl oʼrniga rekursiya aniqroq yoziladi?'
 ],
 ARRAY[
  'Masala: n! hisoblash.',
  'Bazisni yozamiz: n ≤ 1 boʼlsa 1 qaytaramiz.',
  'Rekursiv qadam: n · faktorial(n − 1).',
  'Har chaqiruvda n kamayadi — bazisga albatta yetib boradi.',
  'Qaytish tartibini qogʼozda chizib koʼrish tushunishni tezlashtiradi.'
 ],
 'def faktorial(n):
    if n &lt;= 1:          # bazis holati
        return 1
    return n * faktorial(n - 1)   # rekursiv qadam


print(faktorial(5))   # 120',
 'python',
 ARRAY[
  '<code>if n &lt;= 1: return 1</code> — bazis. Bu satr boʼlmasa chaqiruv toʼxtamaydi.',
  '<code>faktorial(n - 1)</code> — masala aniq kichrayadi: n → n−1.',
  'Koʼpaytirish qaytishda sodir boʼladi: 5·(4·(3·(2·1))).',
  'faktorial(5) = 120 — qoʼlda hisoblab tekshirish mumkin boʼlgan kichik holat.'
 ],
 'Real Python — rekursiya boʼyicha chuqurlashtirilgan qoʼllanma', 'https://realpython.com/python-recursion/'
),

('DAS1110','M9',
 '<p>Million qatorli faylni bir yoʼla xotiraga olsangiz, dastur 800 MB yeydi va serverda ishdan chiqadi. Xuddi shu ishni qator-qator oʼqib, 2 MB bilan bajarish mumkin. Farq — maʼlumotni <em>qachon</em> va <em>qancha</em> xotiraga olayotganligingizda.</p>',
 '<p>Amaliy tahlilda maʼlumot hajmi doim oʼsadi. Xotirani toʼgʼri boshqarish — kodning ishla yoki ishlamasligini belgilaydigan omil.</p>',
 ARRAY[
  '<h2>Stek va uyba</h2><p><strong>Stek</strong> — chaqiruvlar va local oʼzgaruvchilar saqlanadigan, ketma-ket toʼldiriladigan qism; funksiya tugashi bilan boʼshaydi. <strong>Uyba (heap)</strong> — obyektlar va roʼyxatlar yashaydigan katta maydon; u yerda joy ajratish sekinroq, lekin hajmi erkin.</p>',
  '<p>Python, Go kabi tillarda xotirani <strong>axlat yigʼuvchi (garbage collector)</strong> boshqaradi: obyektaga birorta havola qolmasa, u boʼshatiladi. Shuning uchun <code>free()</code> yozish shart emas — lekin bu xotira sarfi yoʼq degani emas: keraksiz yerdagi katta roʼyxat ham baribir joy egallaydi.</p>',
  '<p><strong>Havola va nusxa.</strong> Murakkab obyekt oʼzgaruvchiga biriktirilganda yangi nusxa yaratilmaydi — havola koʼchadi. <code>b = a</code> dan keyin <code>b.append(...)</code> <code>a</code> ni oʼzgartiradi. Bu funksiya parametrari uchun ham shunday; kutilmagan oʼzgarishlarning manbai aynan shu.</p>',
  '<p><strong>Oqim (generator).</strong> <code>yield</code> bilan ishlaydigan funksiya qiymatlarni birma-bir, xotiraga saqlamasdan beradi. Million qatorni roʼyxatga yigʼish oʼrniga oqimdan oʼtkazish — xotira sarfini bir necha yuz marta kamaytiradi.</p>',
  '<p><strong>Sandboxʼda xotira chegarasi.</strong> Kod topshirigʼida konteynerga belgilangan limit beriladi (odatda 256 MB). Limitdan chiqsa verdict <code>MEMORY_LIMIT_EXCEEDED</code> boʼladi — xato emas, resurs talabi.</p>'
 ],
 ARRAY[
  '<strong>Stek</strong> — chaqiruv qatlamlari; tartib bilan toʼlib-boʼshaydi.',
  '<strong>Uyba (heap)</strong> — obyektlar uchun erkin maydon.',
  '<strong>Axlat yigʼuvchi</strong> — havolasiz obyektlarni avtomatik boʼshatadi.',
  '<strong>Havola</strong> — obyektning oʼziga koʼrsatish; nusxa yaratilmaydi.',
  '<strong>Oqim (generator)</strong> — qiymatni lazy, bittalab beradi.',
  '<strong>Xotira limiti (MLE)</strong> — sandboxʼda ruxsat etilgan maksimal hajm.'
 ],
 '<p>Keng xato — <strong>butun maʼlumotni bir yoʼla yigʼish</strong>: <code>lines = f.readlines()</code> katta faylda xotirani toʼldiradi. <code>for line in f:</code> esa faylni qator-qator oʼqiydi — hajmidan qatʼiy nazar barqaror. Ikkinchi xato — <strong>kutilmagan oʼzgarish</strong>: roʼyxatni funksiyaga berib, ichida oʼzgartirsangiz, chaqiruvchi tomondagi qiymat ham oʼzgaradi. Kerak boʼlsa <code>a.copy()</code> yoki <code>list(a)</code> bilan nusxa oling.</p>',
 ARRAY[
  'Nega <code>b = a</code> dan keyin <code>b</code> ni oʼzgartirish <code>a</code> ga tegadi?',
  'Generator qachon roʼyxatdan afzal?',
  'Axlat yigʼuvchi boʼlsa, xotira haqida oʼylash shart emasmi?',
  'MLE verdicti qanday xatodan kelib chiqadi?'
 ],
 ARRAY[
  'Masala: katta fayldan maʼlum bir belgi boʼlgan qatorlarni sanash.',
  'Faylni <code>with open(...)</code> ochamiz — blok oxirida avtomatik yopiladi.',
  'Butun faylni oʼqimasdan, <code>for line in f</code> bilan qatrama-qator yuramiz.',
  'Shartga mos kelganida hisoblagichni oʼstiramiz.',
  'Xotira sarfi fayl hajmiga bogʼliq boʼlib qolmaydi.'
 ],
 'def qatorlar(fayl):
    """Faylni xotiraga yuklamasdan, qatorma-qator beradi."""
    with open(fayl, encoding="utf-8") as f:
        for line in f:
            yield line.rstrip()


soni = sum(1 for q in qatorlar("hisobot.csv") if "xarid" in q)
print("Mos qatorlar:", soni)',
 'python',
 ARRAY[
  '<code>yield</code> funksiyani oqimga aylantiradi: qiymat saqlanmaydi, birma-bir beriladi.',
  '<code>with open(...)</code> faylni ish tugashi bilan yopadi — xato boʼlsa ham.',
  '<code>encoding="utf-8"</code> — oʼzbekcha belgilar buzilmasligi uchun majburiy deb hisoblang.',
  '<code>sum(1 for ...)</code> hisoblagichni roʼyxat yaratmasdan yigʼadi.'
 ],
 'Python — generatorlar va xotira bilan ishlash', 'https://docs.python.org/3/howto/functional.html'
),

('DAS1110','M10',
 '<p>"Maʼlumotlar" soʼzi bazada toʼgʼri koʼrinadi, eksport qilingan CSV da esa "maʼlumotlar" boʼlib buzilib chiqadi. Sabab bitta: belgi va bayt adashtirilgan. Matn bilan ishlaydigan har bir dasturchi bu tuzoqqa bir marta tushadi.</p>',
 '<p>Koʼp tilli muhitda — oʼzbek, rus, ingliz — matnni buzilmasdan saqlash va uzatish xalqaro hisobotlar va mijoz bazalari uchun bevosita muhim.</p>',
 ARRAY[
  '<h2>Belgi va bayt</h2><p>Kompyuter baytlar bilan ishlaydi, odam esa belgilar bilan. Belgini baytga aylantirish — <strong>kodlash</strong> (encoding), orqaga — <strong>dodlash</strong> (decoding). <strong>ASCII</strong> — 128 belgi: lotin harflari, raqamlar, boshqarish belgilari. Oʼzbek, rus, xitoy tillari sigʼmaydi.</p>',
  '<p><strong>Unicode</strong> — dunyo yozuvlarining yagona belgilar toʼplami; har belgiga bir raqam (code point) beradi. <strong>UTF-8</strong> — shu raqamlarni baytlarga oʼtkazishning eng tarqalgan kodi: lotin belgisi 1 bayt, kirill 2, baʼzi belgilar 3–4 bayt. Shuning uchun <code>len()</code> (belgi) va fayl oʼlchami (bayt) har doim ham teng boʼlmaydi.</p>',
  '<p><strong>Escape ketma-ketliklari.</strong> <code>\\n</code> — yangi satr, <code>\\t</code> — tabulyatsiya, <code>\\</code> — belgisining oʼzi, <code>\\u0400</code> — Unicode nuqtasi. Matn ichida bu belgilar kodga emas, boshqarishga xizmat qiladi.</p>',
  '<p><strong>Normaallashtirish.</strong> Bir xil koʼrinadigan ikki matn har xil baytlardan tuzilgan boʼlishi mumkin (birlashtirilgan belgilar). Taqqoslashdan oldin <code>unicodedata.normalize("NFC", s)</code> ishlatiladi — aks holda "bir xil" qatorlar teng chiqmaydi.</p>',
  '<p><strong>Amaliy qoida.</strong> Fayl ochilganda kodlashni koʼrsating: <code>open(f, encoding="utf-8")</code>. Koʼpchilik tizim standartini oʼzgartirish xavfli — natijada bitta fayl turli mashinalarda turlicha oʼqiladi.</p>'
 ],
 ARRAY[
  '<strong>Belgi (character)</strong> — inson koʼradigan yozuv birligi.',
  '<strong>Bayt</strong> — kompyuter saqlaydigan 8 bitlik birlik.',
  '<strong>ASCII</strong> — 128 belgili eski kodlash jadvali.',
  '<strong>Unicode</strong> — barcha yozuvlarning yagona belgilar toʼplami.',
  '<strong>UTF-8</strong> — Unicode baytlarga oʼtkazishning standart kodi.',
  '<strong>Escape ketma-ketlik</strong> — <code>\\n</code>, <code>\\t</code> kabi boshqaruv belgilari.',
  '<strong>Normaallashtirish</strong> — bir xil matnning turli bayt koʼrinishlarini kelishilgan shaklga keltirish.'
 ],
 '<p>Eng keng tushkunlik — <strong>kodlashni koʼrsatmaslik</strong>. <code>open("f.txt")</code> turli tizimlarda turli standartda ochiladi va oʼzbekcha "oʼ, gʼ" belgilari buziladi. Ikkinchi xato — <strong>belgi va baytni chalkashtirish</strong>: limit baytda qoʼyilsa, <code>len(s)</code> bilan tekshirish notoʼgʼri natija beradi (<code>len(s.encode("utf-8"))</code> kerak).</p>',
 ARRAY[
  'Nega "salom" va "salom" soʼzlarining baytlari farq qiladi?',
  '<code>\\n</code> belgisi qancha bayt egallaydi?',
  'UTF-8 ning bitta afzalligi nima?',
  'Fayl ochilganda kodlashni nega koʼrsatish kerak?'
 ],
 ARRAY[
  'Masala: oʼzbekcha matn faylini toʼgʼri oʼqib, soʼz sonini hisoblash.',
  'Faylni <code>encoding="utf-8"</code> bilan ochamiz.',
  'Matnni boʼsh joy boʼyicha boʼlamiz: <code>split()</code>.',
  'Boʼsh qatorlarni chiqarib tashlaymiz.',
  'Natija — soʼzlar soni; belgilar buzilmagan.'
 ],
 'matn = "Iqtisodiyotda axborot texnologiyalari"
sozlar = matn.split()

print("Belgilar:", len(matn))
print("Baytlar:", len(matn.encode("utf-8")))
print("Soʼzlar:", len(sozlar))
print("Birinchi soʼz:", sozlar[0])',
 'python',
 ARRAY[
  '<code>len(matn)</code> — belgilar soni.',
  '<code>encode("utf-8")</code> — baytlar soni; oʼzbekcha belgilar 2 bayt boʼladi, shuning uchun ikkinchi son kattaroq.',
  '<code>split()</code> boʼsh joy boʼyicha boʼladi va qoʼshimcha boʼsh joylarni ham chiqaradi.',
  'Uch oʼlchamni birga bosish — xatoni darhol koʼrsatadi.'
 ],
 'Unicode — UTF-8 nima uchun standart boʼldi', 'https://home.unicode.org/zh-hans/what-is-unicode/'
),

('DAS1110','M11',
 '<p>Baza dan bitta buyruq bilan "ism, familiya" ustunini birlashtirish kerak boʼlsa, baʼzilar <code>+</code> bilan qoʼshadi va xato qiladi: Pythonʼda satr oʼzgarmas, har qoʼshishda yangi obyekt yaratiladi. Ming marta qoʼshilsa — ming yangi obyekt. Satr bilan ishlashning oʼz madaniyati bor.</p>',
 '<p>Hisobotlarda matn bilan ishlash — sana formatlash, kodlarni tozalash, nomlarni birlashtirish — kundalik ish. Toʼgʼri vosita soatlab vaqt tejaydi.</p>',
 ARRAY[
  '<h2>Oʼzgarmaslik (immutability)</h2><p>Python satri bir marta yaratilgach oʼzgartirilmaydi. <code>s += "x"</code> yangi satr yaratadi va eski havolani almashtiradi. Uzun roʼyxatni <code>+</code> bilan qoʼsa borish — O(n²) xotira amali. Shuning uchun qismlarni roʼyxatga yigʼib, oxirida <code>"".join(qismlar)</code> bilan bitta amalda birlashtirish kerak.</p>',
  '<p><strong>Asosiy metodlar.</strong> <code>strip()</code> — chekkadagi boʼsh joylarni oladi; <code>split(delim)</code> — roʼyxatga boʼladi; <code>join(list)</code> — birlashtiradi; <code>replace(a, b)</code> — almashtiradi; <code>lower()/upper()/title()</code> — holatni oʼzgartiradi; <code>startswith()/endswith()</code> — tekshiradi. Ular ham yangi satr qaytaradi, eskisini oʼzgartirmaydi.</p>',
  '<p><strong>Formatlash.</strong> <code>f"{ism} {yosh}"</code> — eng oʻqiladigan usul. Raqamli format ham shu ichida: <code>f"{summa:,.0f}"</code> minglik ajratgich bilan, <code>f"{ulush:.1%}"</code> foiz koʼrinishida. Hisobot chiqarishda bu juda qoʼl keladi.</p>',
  '<p><strong>Goʼda satrlar.</strong> Goʼda <code>string</code> tipi oʼzgarmas bayt ketma-ketligi; boʼlish, qidirish va birlashtirish <code>strings</code> paketida: <code>Split</code>, <code>Join</code>, <code>Contains</code>, <code>TrimSpace</code>. Koʼp qoʼshish uchun <code>strings.Builder</code> ishlatiladi — Pythonʼdagi <code>join</code> vazifasini bajaradi.</p>',
  '<p><strong>Solishtirish.</strong> <code>==</code> matnning oʼzini solishtiradi, lekin <code>"Salom" == "salom"</code> yolgʼon. Taqqoslashdan oldin holatni bir xil qilish (<code>lower()</code>) va boʼsh joylarni tozalash (<code>strip()</code>) keng qoʼllaniladigan himoya.</p>'
 ],
 ARRAY[
  '<strong>Oʼzgarmaslik</strong> — satr mazmuni oʼzgartirilmaydi, yangisi yaratiladi.',
  '<strong>split / join</strong> — boʼlish va birlashtirish; bir-biriga teskari.',
  '<strong>strip</strong> — chekkadagi boʼsh joy va belgilarni tozalash.',
  '<strong>F-string</strong> — qiymatlarni satr ichiga joylash va formatlash.',
  '<strong>strings.Builder (Go)</strong> — koʼp marta qoʼshishda tejamkor quruvchi.',
  '<strong>Case-insensitive</strong> — holatni hisobga olmagan taqqoslash.'
 ],
 '<p>Ikkita keng xato. Birinchisi — <strong>sikl ichida <code>s += ...</code></strong>: har qadamda yangi obyekt, tezlik keskin tushadi. Roʼyxatga yigʼib, oxirida <code>join</code> qiling. Ikkinchisi — <strong>boʼsh satrni tekshirmaslik</strong>: <code>split()</code> natijasi yoki bazadan kelgan <code>""</code> bilan hisoblash <code>IndexError</code> yoki notoʼgʼri javob beradi. Metodni ishlatishdan oldin qiymat boʼsh-yoʼqligini tekshirish odat qiling.</p>',
 ARRAY[
  'Nega <code>"".join(a)</code> <code>for s in a: r += s</code> dan tez?',
  '<code>"a,b,c".split(",")</code> nimani qaytaradi?',
  '<code>f"{x:,.2f}"</code> qanday format beradi?',
  'Goʼda koʼp marta qoʼshish uchun nima ishlatiladi?'
 ],
 ARRAY[
  'Masala: "ism familiya;shahar" koʼrinishidagi yozuvdan ustunlarni ajratish.',
  'Avval <code>strip()</code> bilan chekka boʼsh joylarni olamiz.',
  '<code>split(";")</code> bilan maydonlarga boʼlamiz.',
  'Birinchi maydonni boʼsh joy boʼyicha ikkiga ajratamiz.',
  'Natijani f-string bilan tartibli qatorga yigʼamiz.'
 ],
'yozuv = "  Aliyeva Malika;toshkent  "
yozuv = yozuv.strip()

maydonlar = yozuv.split(";")
ism, familiya = maydonlar[0].split()
shahar = maydonlar[1].title()

print(f"{familiya} {ism[0]}. | {shahar}")',
 'python',
 ARRAY[
  '<code>strip()</code> — kirish maʼlumotida koʼp uchraydigan yashirin boʼsh joylarni yoʼqol qiladi.',
  '<code>split(";")</code> roʼyxat qaytaradi; indeks bilan oʼrganish aniqroq.',
  '<code>title()</code> — "toshkent" ni "Toshkent" ga aylantiradi.',
  'f-string ichida <code>ism[0]</code> — birinchi harf; qatorni ixcham yozish mumkin.'
 ],
 'Go strings paketi — rasmiy hujjat', 'https://pkg.go.dev/strings'
),

('DAS1110','M12',
 '<p>Tahlil bir soat davom etdi, natija ekranga chiqdi — va brauzer yopildi. Hammasi yoʼqolgan. Faylga yozilmagan har qanday natiq — natija emas, vaqtinchalik ekran. Maʼlumotni saqlash — dasturning oxirgi, lekin eng javobgar qadami.</p>',
 '<p>Hisobot, baza eksporti, log — iqtisodchi ishining aksariyati fayl bilan tugaydi. Fayl bilan toʼgʼri ishlash buzilgan va qayta tiklanmaydigan maʼlumotdan asraydi.</p>',
 ARRAY[
  '<h2>Ochish rejimi</h2><p><code>"r"</code> — oʼqish (fayl boʼlmasa xato); <code>"w"</code> — yozish, <strong>mavjud faylni tozalaydi</strong>; <code>"a"</code> — oxiriga qoʼshadi; <code>"x"</code> — faqat yangi yaratish. <code>"w"</code> ni <code>"a"</code> bilan chalkashtirish — hisobotning eski versiyasi yoʼqolishiga olib keladi.</p>',
  '<p><strong>Oqim va toʼliq oʼqish.</strong> <code>read()</code> butun faylni xotiraga oladi; <code>readline()</code> bir qator; <code>for line in f</code> esa xotira hajmi fayl oʼlchamiga bogʼliq boʼlmagan holda qatrama-qator yuradi. Katta faylda uchinchi usul tanlanadi.</p>',
  '<p><strong>Kontekst boshqaruvchisi.</strong> <code>with open(...) as f:</code> blok chetiga chiqqanda faylni avtomatik yopadi — xato boʼlsa ham. Yopilagan faylga yozilgan maʼlumot buferda qolib, diskka tushmasligi mumkin. Shu sababli <code>with</code> — ehtiyotkorlik emas, majburiyat.</p>',
  '<p><strong>Formatlar.</strong> <strong>CSV</strong> — jadval uchun eng keng tarqalgan, ajratgichli matn; Pythonʼda <code>csv</code> paketi bilan oʼqiladi (vergul va qoʼshtirnoq tuzogʼidan asraydi). <strong>JSON</strong> — ichma-ich tuzilma va sozlamalar uchun; <code>json.dump/load</code>. Boshqa formatlar: XLSX (Excel), Parquet (katta hajm, siqilgan).</p>',
  '<p><strong>Yoʼllar.</strong> <code>os.path.join</code> yoki <code>pathlib.Path</code> — ishletiladigan operatsion tizimga qatʼiy yoʼlni toʼgʼri yigʼadi. <code>"data/out.csv"</code> yozish Windows va Linux da ishlashi mumkin, lekin <code>pathlib</code> yanada ishonchli.</p>'
 ],
 ARRAY[
  '<strong>Oqim (stream)</strong> — maʼlumotni boʼlaklab oʼqish/yozish kanali.',
  '<strong>Rejim</strong> — <code>r</code>, <code>w</code>, <code>a</code>, <code>x</code>; <code>w</code> mavjudni oʼchiradi.',
  '<strong>Kontekst boshqaruvchi</strong> — <code>with</code>: resursni avtomatik boʼshatadi.',
  '<strong>CSV</strong> — ajratgichli jadval fayli.',
  '<strong>JSON</strong> — tuzilma saqlovcha matn formati.',
  '<strong>pathlib.Path</strong> — platformaga bogʼliq boʼlmagan yoʼl bilan ishlash.'
 ],
 '<p>Eng zararli xato — <code>"w"</code> rejimini <strong>tekshirmasdan</strong> ochish: fayl darhol boʼshatiladi, eski maʼlumot yoʼqoladi. Yozishdan oldin zaxira nusxa olish yoki <code>"a"</code> rejimini tanlash kerak. Ikkinchi xato — <strong>kodlashni unutish</strong>: UTF-8 koʼrsatilmasa, oʼzbekcha belgilar buziladi va faylni qayta oʼqib boʼlmaydi. Uchinchi — <code>close()</code> ni chaqirmaslik: buferdagi maʼlumot diskka tushmaydi.</p>',
 ARRAY[
  '<code>"w"</code> va <code>"a"</code> farqi nimada?',
  'Nega <code>with open(...)</code> afzal?',
  'Katta faylni xotiraga yuklamasdan qanday oʼqiymiz?',
  'CSV va JSON ni qaysi holatda tanlaymiz?'
 ],
 ARRAY[
  'Masala: talaba baholari roʼyxatini CSV ga yozish va qayta oʼqib oʼrtacha hisoblash.',
  '<code>with open(..., "w", encoding="utf-8", newline="")</code> ochamiz.',
  '<code>csv.writer</code> bilan sarlavha va qatorlarni yozamiz.',
  'Faylni <code>"r"</code> da qayta ochib, <code>csv.DictReader</code> bilan oʼqiymiz.',
  'Ballarni toʼplaymiz va oʼrtacha qiymatni hisoblaymiz.'
 ],
 'import csv

baholar = [("Aliyev", 88), ("Valiyeva", 95)]

with open("baholar.csv", "w", encoding="utf-8", newline="") as f:
    yozuvchi = csv.writer(f)
    yozuvchi.writerow(["ism", "ball"])
    yozuvchi.writerows(baholar)

jami = 0
son = 0
with open("baholar.csv", encoding="utf-8") as f:
    for qator in csv.DictReader(f):
        jami += int(qator["ball"])
        son += 1

print("Oʼrtacha ball:", round(jami / son, 1))',
 'python',
 ARRAY[
  '<code>newline=""</code> — csv paketi qator oxirini oʼzi boshqaradi; boʼlmasa qoʼshimcha boʼsh qator chiqadi.',
  '<code>writerows</code> butun roʼyxatni bir amalda yozadi.',
  '<code>DictReader</code> sarlavhani kalitga aylantiradi — indeksga qarab xato qilish ehtimoli kamayadi.',
  '<code>int(...)</code> — CSV da hammasi matn; hisobdan oldin tur oʼtkazish shart.'
 ],
 'Python — fayllar va vaqtlar boʼlimi', 'https://docs.python.org/3/tutorial/inputoutput.html#reading-and-writing-files'
);

-- --------------------------------------------------------------------------
-- DAS1110 · amaliy mashgʼulotlar (A1–A9)
-- --------------------------------------------------------------------------
INSERT INTO seed_content
(subject, lesson, hook, why, explain, concepts, mistake, checkin, worked, code, lang, codewalk, link_label, link_url) VALUES

('DAS1110','A1',
 '<p>Bitta masalani ikki tilda yozib koʼrish — nima uchun baʼzi tillar "tez yoziladi", baʼzilari esa "tez ishlaydi" degan savolga oʼz tajribangiz bilan javob beradi.</p>',
 '<p>Amaliyotda til tanlash — loyiha narxiga taʼsir qiladigan iqtisodiy qaror: tez prototip yoki barqaror yuqori yuklama.</p>',
 ARRAY[
  '<p>Pythonʼda dastur faylini toʼgʼridan-toʼgʼri interperator bajaradi: <code>python3 dastur.py</code>. Goʼda esa avval <code>go build</code> bilan bajariladigan fayl yaratadi, keyin u ishga tushadi. Birinchi holatda sinov tez, ikkinchisida ishga tushirish tez va tur xatolari oldindan ushlanadi.</p>',
  '<p>Identifikator qoidalari ikkala tilda ham oʻxshash: harf yoki <code>_</code> bilan boshlanadi, kalit soʼz boʼlmaydi. Python registrni hisobga oladi (<code>ball</code> va <code>Ball</code> — boshqa nom), Go ham shunday, lekin eʼlon qilingan <em>va ishlatilmagan</em> oʼzgaruvchini xato deb hisoblaydi.</p>',
  '<p>Maʼlumot turlarini aniqlashda Pythonʼda <code>type(x)</code>, Goʼda <code>%T</code> formati yoki statik eʼlon (<code>var x int</code>) ishlatiladi. Shu farq — dinamik va statik tipning amaldagi namoyoni.</p>'
 ],
 ARRAY[
  '<strong>Bajarish sikli</strong> — yozish → (kompilyatsiya) → ishga tushirish.',
  '<strong>Identifikator</strong> — nom berish qoidalari.',
  '<strong>Statik tip</strong> — tur vaqtinchalik emas, kompilyatsiyada tekshiriladi.',
  '<strong>Type() / %T</strong> — qiymat turini soʼrash vositalari.'
 ],
 '<p>Amaliyotda eng koʼp adashtiriladigan narsa — <strong>turni oʼzgartirishni unutish</strong>: <code>input()</code> matn qaytaradi, uni son deb hisoblash xato beradi yoki (yomonroq) matnni qoʼshib, "12" + "8" = "128" chiqadi.</p>',
 ARRAY[
  'Ikkita tilda bir xil masalani bajarish vaqti nimaga bogʼliq?',
  'Goʼda ishlatilmagan oʼzgaruvchi nega xato hisoblanadi?',
  '<code>type("5")</code> nimani qaytaradi?'
 ],
 ARRAY[
  'Bir xil masalani — ikki son yigʼindisini — Python va Go da yozing.',
  'Har birini 5 marta ishga tushirib, vaqtni qayd eting.',
  'Xatoni ataylab keltiring: Python da tur xatosi qachon, Go da qachon koʼrinadi?',
  'Kuzatgan farqingizni bir xulosa qilib yozing.'
 ],
 'a = input("a = ")
b = input("b = ")
print(a + b)          # matn qoʼshiladi: "12" + "8" -> "128"
print(int(a) + int(b)) # toʼgʼrisi: 20',
 'python',
 ARRAY[
  'Birinchi qator xatoni koʼrsatadi: <code>input()</code> matn qaytaradi.',
  '<code>int()</code> bilan tur oʼtkazilgandagina arifmetika ishlaydi.',
  'Goʼda <code>fmt.Scan</code> birdan songa yozadi — bu xato erta, kompilyatsiyada seziladi.'
 ],
 'Go — nima uchun statik tiplar', 'https://go.dev/doc/effective_go#types'
),

('DAS1110','A2',
 '<p>Foydalanuvchi "oʼn" deb yozsa dastur nima qiladi? Yoki boʼsh qoldirsa? Maʼlumot kiritish — eng koʼp xato chiqadigan joy, chunki u inson bilan chegaradosh.</p>',
 '<p>Har qanday hisob-kitob dasturi tashqi, nazoratsiz kirishga tayyor boʼlishi kerak. Kirishni tekshirish — dastur sifati koʼrsatkichi.</p>',
 ARRAY[
  '<p><strong>Kiritish.</strong> <code>input()</code> har doim matn qaytaradi. Son kerak boʼlsa <code>int()</code> / <code>float()</code> ishlatiladi va notoʼgʼri kiritilsa <code>ValueError</code> koʼtariladi. Shuning uchun kiritish <code>try/except</code> ichida oʼraladi.</p>',
  '<p><strong>Chiqarish.</strong> <code>print()</code> ning <code>sep</code> va <code>end</code> parametrlari ajratgich va qator oxirini boshqaradi; <code>f"{x:.2f}"</code> esa kasr xonalarini belgilaydi. Hisobot formati shu yerdan boshlanadi.</p>',
  '<p><strong>Operatorlar ustunligi.</strong> Avval <code>**</code>, keyin <code>* / // %</code>, oxirida <code>+ -</code>. Qavs bilan ustunlikni oʼzgartirish mumkin — va kerak. <code>2 + 3 * 4</code> = 14, <code>(2 + 3) * 4</code> = 20.</p>',
  '<p><strong>Mantiqiy operatorlar</strong>da qisqa tutashuv bor: <code>x != 0 and 10 / x &gt; 1</code> da birinchi shart yolgʼon boʼlsa, ikkinchisi hisoblanmaydi — bu boʼlish xatosidan himoya usuli.</p>'
 ],
 ARRAY[
  '<strong>input() / fmt.Scan</strong> — kirishni oʼqish.',
  '<strong>Tur oʼtkazish</strong> — <code>int()</code>, <code>float()</code>: matndan songa.',
  '<strong>Ustunlik</strong> — amallar bajarilish tartibi.',
  '<strong>Qisqa tutashuv</strong> — mantiqiy ifodaning keraksiz qismi hisoblanmaydi.',
  '<strong>try/except</strong> — kutilmagan holatni ushlash.'
 ],
 '<p><strong>Boʼlishni butun boʼlish bilan chalkashtirish</strong>: <code>7 / 2</code> = 3.5, <code>7 // 2</code> = 3. Oʼrtacha qiymat hisobida <code>//</code> ishlatilsa natija doim pastga yaxlitlanadi va xato sezilmaydi. Ikkinchi xato — kiritishni tekshirmaslik: manfiy yoki matn qiymat dasturni toʼxtatadi.</p>',
 ARRAY[
  '<code>9 // 2</code> va <code>9 % 2</code> nimaga teng?',
  'Nega <code>input()</code> dan keyin <code>int()</code> kerak?',
  '<code>and</code> qisqa tutashuvi qanday himoya beradi?'
 ],
 ARRAY[
  'Masala: uch xodisning oʼrtacha maoshini hisoblash.',
  'Har bir maoshni <code>float</code> ga oʼtkazib olamiz.',
  'Yigʼindini hisoblab, 3 ga boʼlamiz — butun boʼlish emas!',
  'Natijani ikki xonagacha yaxlitlab chiqaramiz.'
 ],
 'ism = input("Xodim: ")
maosh = float(input("Maosh: "))
bonus = maosh * 0.25

print(f"{ism}: asosiy {maosh:,.0f}, bonus {bonus:,.0f}, jami {maosh + bonus:,.0f}")',
 'python',
 ARRAY[
  '<code>float(input(...))</code> — kasr qiymatga tayyorgarlik.',
  '<code>bonus</code> alohida oʼzgaruvchida: tekshirish va hisobotda koʼrsatish oson.',
  '<code>:,.0f</code> — minglik ajratgich bilan, kasrsiz bosish; pul koʼrinishi uchun qulay.',
  'f-string ichida ifoda ham yozish mumkin: <code>{maosh + bonus}</code>.'
 ],
 'Python — kiritish va chiqarish', 'https://docs.python.org/3/tutorial/inputoutput.html'
),

('DAS1110','A3',
 '<p>"Talaba imtihonga yoʼl oladi: oʼrtacha 70 dan yuqori <em>va</em> qarz darsi boʼlmasa." Bitta shart yetarli emas — ikkitasi birga kerak. Mana shu "birga" — mantiqiy amallar.</p>',
 '<p>Seleksiya qoidalari, limit tekshiruvi, reyting mezonlari — barchasi mantiqiy ifodalarga quriladi.</p>',
 ARRAY[
  '<p><strong>Mantiqiy operatorlar.</strong> <code>and</code> — hammasi yolgʼon boʼlmasa true; <code>or</code> — bittasi ham true boʼlsa yetarli; <code>not</code> — qiymatni teskarisiga aylantiradi.</p>',
  '<p><strong>De Morgan qonunlari</strong> murakkab shartni soddalashtiradi: <code>not (a and b)</code> ≡ <code>(not a) or (not b)</code>. Buni bilmasa, inkorli shartlarni yozishda chalkashlik koʼp.</p>',
  '<p><strong>Matematik funksiyalar.</strong> <code>math</code> paketi <code>sqrt</code>, <code>pow</code>, <code>floor</code>, <code>ceil</code>, <code>hypot</code> ni beradi. <code>round()</code> esa "bankir yaxlitlashi" bilan ishlaydi: 2.5 → 2, 3.5 → 4. Pul hisobida bu farq muhim.</p>',
  '<p><strong>Haqiqiy son aniqligi.</strong> <code>0.1 + 0.2 == 0.3</code> ifodasi yolgʼon — ikilik kasrdagi yaxlitlash tufayli. Shuning uchun pullar soniyada emas, <code>Decimal</code> da yoki tiyinda (butun) saqlanadi.</p>'
 ],
 ARRAY[
  '<strong>and / or / not</strong> — mantiqiy amallar.',
  '<strong>De Morgan</strong> — inkorni tarqatish qonunlari.',
  '<strong>math paketi</strong> — sqrt, floor, ceil, hypot.',
  '<strong>Bankir yaxlitlashi</strong> — <code>round()</code> juft tomonga yaxlitlaydi.',
  '<strong>Kasr aniqligi</strong> — <code>0.1 + 0.2 != 0.3</code>.'
 ],
 '<p>Eng koʼp xato — <strong>haqiqiy sonni <code>==</code> bilan solishtirish</strong>. Yuqoridagi sabab tufayli u kutilmagan yolgʼon javob beradi. Toʼgʼri usul: <code>abs(a - b) &lt; 1e-9</code>. Ikkinchisi — <code>and</code>/<code>or</code> ni qavsiz aralashtirish: <code>a or b and c</code> kutilganidek oʼqilmaydi.</p>',
 ARRAY[
  '<code>not (x &gt; 5 and y &lt; 2)</code> ni qavsiz yozing.',
  '<code>round(2.5)</code> nechchi? Nega?',
  'Nega pul uchun <code>float</code> xavfli?'
 ],
 ARRAY[
  'Masala: uch tomoni berilgan uchburchakning teng tomonli va toʼgʼri burchakli ekanligini tekshirish.',
  'Tomonlarni olamiz: a, b, c.',
  'Kvadratlarni solishtiramiz — kasrga tegmaslik uchun butun sonlarda ishlaymiz.',
  'Pifagor sharti va teng tomonlilik shartini <code>and</code> bilan birlashtiramiz.'
 ],
 'import math

a, b, c = 3, 4, 5
gipotenuza = max(a, b, c)
katetlar = sorted([a, b, c])

toʼgʼri_burchakli = math.isclose(
    katetlar[0] ** 2 + katetlar[1] ** 2,
    katetlar[2] ** 2,
)

print("Gipotenuza:", gipotenuza)
print("Toʼgʼri burchaklimi:", toʼgʼri_burchakli)',
 'python',
 ARRAY[
  '<code>max</code> va <code>sorted</code> — qaysi tomon gipotenuza ekanligini avtomatik topadi.',
  '<code>** 2</code> — kvadrat; butun sonlarda kasr aniqligi muammosi yoʼq.',
  '<code>math.isclose</code> — <code>==</code> oʼrniga ishlatiladigan, xatolikka chidamli solishtirish.',
  'Natija mantiqiy qiymat: uni toʼgʼridan-toʼgʼri shartda ishlatish mumkin.'
 ],
 'Python math paketi', 'https://docs.python.org/3/library/math.html'
),

('DAS1110','A4',
 '<p>ATM karta raqamini tekshiradi: avval PIN boʼyicha, keyin balans boʼyicha. Har bosqichda javob boshqacha. Bunday ketma-ket tekshiruvni chiroyli yozish usuli bor — erta chiqish.</p>',
 '<p>Biznes-jarayon qoidalari koʼpincha shartlar zanjiri. Uni oʼqiladigan qilib yozish — keyinchsiz tuzatish xarajatini kamaytiradi.</p>',
 ARRAY[
  '<p><strong>Shartli oʼtish.</strong> <code>if / elif / else</code> bir nechta holatdan birini tanlaydi. Zanjir yuqoridan tekshiriladi — birinchi mos kelgan bajariladi.</p>',
  '<p><strong>Guard clause.</strong> Har xil holatni boshida tekshirib, funksiyani darhol tugatish (<code>return</code>) ichma-ich <code>if</code> larni yoʼqol qiladi. Kod "yaroqsiz hol" dan "asosiy yoʼl" ga qarab tekis oʼqiladi.</p>',
  '<p><strong>Ichma-ich shart</strong> mumkin, lekin ikki darajadan oshsa tushunish qiyinlashadi. Bunday joyda shartni birlashtirish (<code>and</code>) yoki funksiya ajratish yaxshiroq.</p>',
  '<p>Goʼda <code>if</code> da qavslar majburiy emas, lekin <code>{ }</code> majburiy; <code>else if</code> oʼrniga <code>else if</code> yoziladi va <code>switch</code> koʼp tarmoqda qisqaroq.</p>'
 ],
 ARRAY[
  '<strong>Shartli oʼtish</strong> — <code>if / elif / else</code>.',
  '<strong>Guard clause</strong> — yaroqsiz holni boshida chiqarish.',
  '<strong>Tarmoq qamrovi</strong> — har tarmoq testda tekshirilishi.',
  '<strong>Switch / match</strong> — qiymat boʼyicha tanlash.'
 ],
 '<p><strong>Boʼsh <code>else</code> qoldirish</strong> — kutilmagan qiymat sezilmasdan oʼtib ketadi. Har doim <code>else</code> da xabarni yozib chiqaring yoki istisno koʼtaring. Ikkinchisi — chegaralarni takrorlash: <code>&gt;= 70</code> va <code>&lt; 90></code> orasidagi 89.5 qayerga tushishini oʼylab koʼring.</p>',
 ARRAY[
  'Nega zanjirda yuqori chegara avval tekshiriladi?',
  'Guard clause qanday oʼqilishni yaxshilaydi?',
  '<code>else</code> boʼlmasa qanday xavf bor?'
 ],
 ARRAY[
  'Masala: mijoz toifasini aniqlash (chek, oylik xarid, staj).',
  'Avval maʼlumot yaroqliligini tekshiramiz — guard.',
  'Keyin eng yuqori toifadan pastga qarab shart yozamiz.',
  'Hech biriga mos kelmasa — aniq "yangi mijoz" tarmogʼi qoldiramiz.'
 ],
 'def toifa(xarid, staj):
    if xarid &lt; 0 or staj &lt; 0:
        return "maʼlumot notoʼgʼri"
    if xarid &gt;= 5_000_000 and staj &gt;= 3:
        return "oltin"
    if xarid &gt;= 2_000_000:
        return "kumush"
    return "yangi"


print(toifa(6_200_000, 4))
print(toifa(1_500_000, 1))
print(toifa(-1, 2))',
 'python',
 ARRAY[
  'Birinchi <code>if</code> — guard: yaroqsiz kirish davom etmaydi.',
  'Ikkinchi shartda <code>and</code> bilan ikki mezon birga tekshiriladi.',
  'Har tarmoq darhol <code>return</code> qiladi — <code>elif</code> ga hojat qolmaydi.',
  'Oxirgi qator — yagona "boshqa holat" javobi.'
 ],
 'Go — if, else va switch', 'https://go.dev/tour/flowcontrol/1'
),

('DAS1110','A5',
 '<p>Sotuvchi mijozga uch xil narx taklif qiladi va har biri uchun alohida matn yozadi. Kodda besh darajali <code>if</code> zanjiri paydo boʼladi — oʼqib boʼlmaydi. Tanlash operator shu yerda kerak.</p>',
 '<p>Kodlanadigan holatlar roʼyxati (buyurtma holati, valyuta, toʼlov turi) aniq boʼlsa, tanlash operatori ham aniqroq, ham tezroq.</p>',
 ARRAY[
  '<p><strong>Switch (Go).</strong> Bir ifodaning qiymatini holatlar bilan solishtiradi. Goʼda har holat avtomatik sinaydi — boshqa tillardagi <code>fallthrough</code> tuzogʼi yoʼq. Mos holat topilmasa <code>default</code> bajariladi.</p>',
  '<p><strong>Match (Python 3.10+).</strong> <code>match ... case</code> qiymatni, hatto tuzilmani (roʼyxat, roʼyxat ichidagi maydonlar) namuna bilan solishtiradi. <code>case 6 | 7</code> — bir nechta qiymat bitta tarmoqda.</p>',
  '<p><strong>Ternar operator.</strong> <code>x = "juft" if n % 2 == 0 else "toq"</code> — ikki tarmoqli ifodaning qisqa yozuvi. Bir qatorda aniq boʼlsa qulay, shartlar koʼpayib ketsa oʼqishni buzadi.</p>',
  '<p><strong>Qachon qaysi biri?</strong> Ikki holat — <code>if/else</code>. Bir necha aniq qiymat — <code>switch/match</code>. Diapazon va murakkab shart — <code>if/elif</code>.</p>'
 ],
 ARRAY[
  '<strong>Tanlash operatori</strong> — qiymat boʼyicha tarmoq tanlash.',
  '<strong>default / case _</strong> — mos kelmasa bajariladigan tarmoq.',
  '<strong>Pattern matching</strong> — tuzilmali namuna bilan solishtirish.',
  '<strong>Ternar ifoda</strong> — <code>A if shart else B</code>.'
 ],
 '<p>Pythonʼning eski uslubida <code>switch</code> yoʼq, shuning uchun uzun <code>if/elif</code> zanjiri yoziladi va har birida bir xil oʼzgaruvchi solishtiriladi — takrorlash. Buning oʼrniga <strong>lugʼdan (dict) foydalanish</strong> ham mumkin: <code>{"oltin": 0.15, "kumush": 0.10}</code>. Ikkinchi xato — <code>match</code> da oxirgi <code>case _</code> ni unutish: moslik boʼlmasa funksiya <code>None</code> qaytaradi.</p>',
 ARRAY[
  'Goʼda <code>break</code> nega kerak emas?',
  '<code>case 6 | 7:</code> nimani anglatadi?',
  'Ternar ifoda qachon zararli?',
  'Uzun <code>if/elif</code> oʼrniga qanday maʼlumot tuzilmasi ishlatiladi?'
 ],
 ARRAY[
  'Masala: kun raqami boʼyicha dars jadvali turini aniqlash.',
  'Kunni olamiz (1..7).',
  '<code>match</code> da dam olish kunlarini bitta tarmoqda yigʼamiz.',
  'Ish kunlari va notoʼgʼri qiymat uchun alohida tarmoq qoldiramiz.'
 ],
 'def kun_turi(kun):
    match kun:
        case 6 | 7:
            return "dam olish"
        case 1 | 2 | 3 | 4 | 5:
            return "ish kuni"
        case _:
            return "notoʼgʼri qiymat"


print(kun_turi(3), kun_turi(7), kun_turi(9))',
 'python',
 ARRAY[
  '<code>case 6 | 7</code> — ikki qiymat bitta tarmoqda.',
  '<code>case _</code> — default: hech biri mos kelmasa.',
  'Har tarmoq <code>return</code> bilan tugaydi, "fallthrough" muammosi yoʼq.',
  'Uch chaqiruv uchun turli javob — sinuv toʼliq boʼldi.'
 ],
 'Python — structural pattern matching (PEP 634)', 'https://peps.python.org/pep-0634/'
),

('DAS1110','A6',
 '<p>Kassa xodomi hisobni toʼgʼri kiritguncha qayta-qayta soʼraydi. Necha marta soʼralishi oldindan maʼlum emas. Mana shu — <code>while</code> siklining tabiiy holati.</p>',
 '<p>Qadamlar soni maʼlum boʼlmagan jarayonlar (kuzatish, qayta urinish, oqimni oʼqish) shartli siklni talab qiladi.</p>',
 ARRAY[
  '<p><strong>Parametrli vs old shartli.</strong> <code>for</code> — qadamlar aniq; <code>while</code> — shart bajarilguncha. <code>while True</code> + <code>break</code> — "kerakli javob kelguncha" andozasi.</p>',
  '<p><strong>Hisoblagichni boshqarish.</strong> <code>while</code> tanasida shartni buzadigan oʼzgarish boʼlishi shart: <code>i += 1</code> yoki foydalanuvchi javobi. Boʼlmasa — cheksiz sikl.</p>',
  '<p><strong>range</strong> variantlari: <code>range(n)</code> → 0..n−1, <code>range(a, b)</code> → a..b−1, <code>range(a, b, q)</code> — qadam bilan. Orqaga: <code>range(n, 0, -1)</code>.</p>',
  '<p><strong>continue</strong> — qolgan qatorni oʼtkazib yuborib keyingi iteratsiyaga oʼtadi. Filtrlashda qoʼl keladi.</p>'
 ],
 ARRAY[
  '<strong>Iteratsiya</strong> — siklning bir qadami.',
  '<strong>Chiqish sharti</strong> — siklni toʼxtatadigan mantiqiy ifoda.',
  '<strong>Hisoblagich</strong> — qadamlarni sanaydigan oʼzgaruvchi.',
  '<strong>break / continue</strong> — chiqish / keyingi qadamga oʼtish.'
 ],
 '<p><strong>Hisoblagichni oʼzgartirmaslik</strong> — klassik cheksiz sikl. Yana bir nozik xato: <code>while</code> shartini kirishdan <em>keyin</em> tekshirish kerak boʼlsa, <code>while</code> birinchi qadamni oʼtkazib yuboradi. Bunday holatda <code>while True</code> + ichida <code>break</code> ishlatiladi.</p>',
 ARRAY[
  '<code>while True</code> qachon oqilona?',
  '<code>range(10, 0, -2)</code> qanday qiymatlar beradi?',
  'Sikl chiqish shartini qanday tekshirasiz?'
 ],
 ARRAY[
  'Masala: foydalanuvchidan 1..100 oraligʼidagi sonni soʼrash.',
  '<code>while True</code> bilan takrorlanuvchi blok ochamiz.',
  'Kiritilgan qiymatni son ga oʼtkazamiz, xatoni <code>try/except</code> ushlaydi.',
  'Oraliqqa tushsa <code>break</code> bilan chiqamiz.'
 ],
 'while True:
    try:
        son = int(input("1..100 orasida son kiriting: "))
    except ValueError:
        print("Faqat butun son kiriting.")
        continue

    if 1 &lt;= son &lt;= 100:
        break
    print("Oraliqdan chiqdi, qayta urinib koʼring.")

print("Qabul qilindi:", son)',
 'python',
 ARRAY[
  '<code>while True</code> — chiqish sharti ichida: kirishni tekshirib boʼlgach <code>break</code>.',
  '<code>try/except ValueError</code> — matn kiritilsa dastur toʼxtamaydi.',
  '<code>continue</code> — qolgan qatorni oʼtkazib, qaytadan soʼraydi.',
  '<code>1 &lt;= son &lt;= 100</code> — Pythonʼdagi qulay qoʼsh solishtirish.'
 ],
 'Go — for: yagona sikl operatori', 'https://go.dev/tour/flowcontrol/3'
),

('DAS1110','A7',
 '<p>Ishlab chiqarish rejasi: har bir sex uchun har bir mahsulot boʼyicha reja tuziladi. Bitta sikl yetarli emas — ichkariga kirish kerak. Va har ichma-ich sikl qancha ish qilishini hisoblash shart.</p>',
 '<p>Ichma-ich sikllarning qadam soni — jarayon narxini baholashning toʼgʼridan-toʼgʼri vositasi: n×m qadam, n×m×k qadam.</p>',
 ARRAY[
  '<p><strong>Tashqi va ichki sikl.</strong> Tashqi — satrlar (sexlar), ichki — ustunlar (mahsulotlar). Ichki sikl tashqi har bir qadamida <em>toʼliq</em> aylanadi.</p>',
  '<p><strong>Murakkablik.</strong> n×m matritsani koʼrish — O(n·m). Ikkala tomon ham 10 000 boʼlsa, bu 10⁸ qadam — bir necha soniya. Shu sababli ichma-ich sikl yozishdan oldin hajmni baholang.</p>',
  '<p><strong>Erta uzilish.</strong> Javob topilgacha kuting — <code>break</code> yoki funksiyadan <code>return</code>. Eng yomon holat oʼzgarsa ham, oʼrtacha holatda katta tejim.</p>',
  '<p><strong>Andozalar.</strong> Koʼpaytirish jadvali, triangul chizish, juftlarni solishtirish (O(n²)), saralash algoritmlari — barchasi ichma-ich siklga quriladi.</p>'
 ],
 ARRAY[
  '<strong>Tashqi / ichki sikl</strong> — satr va ustun boʼyicha yurish.',
  '<strong>Kvadratik murakkablik</strong> — O(n²): hajm oʼsganda qadamlar keskin oʼsadi.',
  '<strong>Erta uzilish</strong> — javob topilsa davom ettirmaslik.',
  '<strong>Akkumulyator</strong> — ichki siklda qiymat toʼplash.'
 ],
 '<p>Ichki siklda <strong>hisoblagichni notoʼgʼri joyda nollash</strong>: <code>jami = 0</code> tashqi sikldan tashqarida boʼlsa, barcha satrlar yigʼindisi chiqadi — xato son "ishonarli" koʼrinadi. Shuningdek, ichma-ich siklni hajmini hisoblamaslik katta maʼlumotda soatlab sekinlikka olib keladi.</p>',
 ARRAY[
  '5×7 matritsani koʼrish nechta qadam?',
  '<code>jami = 0</code> qayerda eʼlon qilinishi kerak?',
  'Qachon <code>break</code> ichki sikldan tashqarisini ham chiqaradi?'
 ],
 ARRAY[
  'Masala: har sex boʼyicha jami ishlab chiqarishni hisoblash.',
  'Matritsa: satrlar — sexlar, ustunlar — kunlar.',
  'Tashqi sikl sexlarni aylanadi; har sexda <code>jami = 0</code>.',
  'Ichki sikl kunlarni qoʼshadi; natijani sex nomi bilan chiqaramiz.'
 ],
'ishlab_chiqarish = [
    [120, 135, 118],   # 1-sex: 3 kun
    [98, 104, 112],
    [145, 150, 149],
]

for sex in range(len(ishlab_chiqarish)):
    jami = 0
    for kun in range(len(ishlab_chiqarish[sex])):
        jami += ishlab_chiqarish[sex][kun]
    print(f"{sex + 1}-sex jami: {jami}")',
 'python',
 ARRAY[
  '<code>jami = 0</code> tashqi sikl ichida — har sex uchun yangidan boshlanadi.',
  '<code>len(ishlab_chiqarish[sex])</code> — har sexda kunlar soni turlicha boʼlsa ham ishlaydi.',
  '3×3 = 9 qadam: ichki sikl har tashqi qadamda toʼliq aylanadi.',
  'Natija tartibli chiqadi, chunki tashqi sikl indeks boʼyicha ketadi.'
 ],
 'Go — nested loops va range', 'https://go.dev/tour/flowcontrol/6'
),

('DAS1110','A8',
 '<p>Hisobotni qayta ishlashga buyruq berilganda, dastur avval bir marta bajarib, keyin "yana kerakmi?" deb soʼrashi lozim. <code>while</code> buni qanday qiladi — Goʼda <code>do-while</code> yoʼq?</p>',
 '<p>"Kamida bir marta" talab qiladigan jarayonlar (menyu, qayta urinish, kirish tasdiqlash) amaliyotda tez-tez uchraydi.</p>',
 ARRAY[
  '<p><strong>Soʼng shartli sikl.</strong> Klassik <code>do { } while (shart)</code> avval tanani bajaradi, keyin tekshiradi. Python va Goʼda bunday operator yoʼq — <code>while True</code> + oxirida <code>if ... break</code> bilan yoziladi.</p>',
  '<p><strong>Urinishlarni cheklash.</strong> Cheksiz soʼrash xavfli: hisoblagich qoʼyib, maksimal urinishdan keyin chiqish — ishonchli kodning belgisi.</p>',
  '<p><strong>Filtrlash.</strong> <code>continue</code> yordamida keraksiz elementlarni oʼtkazib yuborish, yoki roʼyxat tushunishi (list comprehension) bilan yangi roʼyxat qurish.</p>',
  '<p><strong>Siklda hisoblash vaqtini baholash</strong> — katta hajmli ishda qaysi qadam sekinligini aniqlash uchun <code>time.perf_counter()</code> ishlatiladi.</p>'
 ],
 ARRAY[
  '<strong>do-while</strong> — kamida bir marta bajariladigan sikl.',
  '<strong>Urinish chegarasi</strong> — cheksiz takrorlanishdan himoya.',
  '<strong>Filtrlash</strong> — shartga mos elementlarni tanlash.',
  '<strong>perf_counter</strong> — bajarish vaqtini oʼlchash.'
 ],
 '<p><code>break</code> ni <strong>faqat bitta shartda</strong> qoʼyish: ikkinchi chiqish yoʼlini unutib, sikl cheksiz aylanadi. Shuningdek, urinish hisoblagichini <code>while True</code> ichida nollab qoʼyish — limit ishlamay qoladi.</p>',
 ARRAY[
  'Pythonʼda "kamida bir marta" qanday yoziladi?',
  'Urinish chegarasini qoʼymasangiz nima boʼladi?',
  '<code>continue</code> qachon foydali?'
 ],
 ARRAY[
  'Masala: serverga ulanishni 3 marta sinab koʼrish.',
  'Hisoblagichni <code>urinish = 0</code> deb tashqarida ochamiz.',
  '<code>while True</code> ichida harakat qilamiz, muvaffaqiyat boʼlsa <code>break</code>.',
  'Muvaffaqiyatsizlikda urinishni oʼstiramiz va 3 ga yetganda chiqamiz.'
 ],
 'urinish = 0
muvaffaq = False

while True:
    urinish += 1
    print(f"{urinish}-urinish: urinish yapilmoqda...")
    javob = input("Ulanish muvaffaqiyatlimi? (ha/yoʼq) ")
    if javob == "ha":
        muvaffaq = True
        break
    if urinish &gt;= 3:
        break

print("Natija:", "ulandi" if muvaffaq else "ulanmadi")',
 'python',
 ARRAY[
  '<code>urinish</code> tashqarida eʼlon qilinadi — sikl ichida nollanmaydi.',
  'Ikki chiqish nuqtasi bor: muvaffaqiyat va limit.',
  '<code>muvaffaq</code> bayrogʼi yakuniy javobni aniq qiladi.',
  'Oxirgi qator — ternar ifoda: qisqa va aniq xulosa.'
 ],
 'Python — xatolar va istisnolar', 'https://docs.python.org/3/tutorial/errors.html'
),

('DAS1110','A9',
 '<p>100 000 mijoz orasidan bittasini qidirish — buni har soʼrovda qayta-qayta bajarasiz. Qidiruv usulini toʼgʼri tanlash sekundni millisekundga, server hisob-kitobini esa bir necha barobarga oʼzgartiradi.</p>',
 '<p>Bu mavzu sandboxʼda kod topshirigʼi bilan baholanadi: toʼgʼri algoritm — toʼgʼri natija va vaqt.</p>',
 ARRAY[
  '<p><strong>Saralash.</strong> Oddiy usullar (bubble, insertion) O(n²): 10⁵ element ≈ 10¹⁰ qadam — amaliy ishlamaydi. Tilning oʼzidagi <code>sorted()</code> / <code>slices.Sort</code> timsort yoki quicksort asosida O(n log n) beradi. Amaliyotda oʼz saralash algoritmingizni yozish oʼrniga tayyor funksiyani chaqirish toʼgʼri.</p>',
  '<p><strong>Chiziqli qidiruv</strong> — O(n), saralash talab qilmaydi. <strong>Ikkilik qidiruv</strong> — O(log n), lekin faqat saralangan maʼlumotda. 1 000 000 element uchun 20 ga yaqin qadam.</p>',
  '<p><strong>Massiv yigʼindisi</strong> — sandbox topshirigʼining klassik andozasi: kirishni oʼqish, accumulator bilan yigʼish, bitta qatorda chiqarish.</p>',
  '<p><strong>Sandbox chegaralari.</strong> CPU vaqt chegarasi (bu topshiriqda 2000 ms), xotira (256 MB) va tarmoq yoʼqligi. Yechim <code>input()</code> dan tashqariga chiqmasligi, fayl yoki internetga murojaat qilmasligi kerak.</p>'
 ],
 ARRAY[
  '<strong>Saralash</strong> — maʼlumotni tartibga solish; O(n log n) standart.',
  '<strong>Chiziqli qidiruv</strong> — O(n), tartib shart emas.',
  '<strong>Ikkilik qidiruv</strong> — O(log n), tartib talab qilinadi.',
  '<strong>Ikki koʼrsatkich</strong> — bosh va oxiridan yurish andozasi.',
  '<strong>Verdict</strong> — sandbox qaytargan baholash xulosasi.'
 ],
 '<p>Ikkilik qidiruvda <strong>chegara chalkashligi</strong>: <code>chap &lt;= oʼng</code> oʼrniga <code>&lt;</code> qoʼyilsa, oxirgi element topilmaydi. Yana bir xato — <code>oʼrta = (chap + oʼng) // 2</code> katta indekslarda toʼlib ketishi mumkin; xavfsizroq yozuv <code>chap + (oʼng - chap) // 2</code>. Va albatta: saralanganligiga ishonch hosil qilmay turib ikkilik qidiruv ishlatish.</p>',
 ARRAY[
  'Nega ikkilik qidiruv saralashni talab qiladi?',
  '<code>chap &lt;= oʼng</code> nega tenglik bilan?',
  'Sandboxʼda qaysi resurslar chegaralangan?',
  '10⁶ elementni chiziqli qidirish taxminan nechta qadam?'
 ],
 ARRAY[
  'Masala: massiv elementlari yigʼindisini chiqarish (DEV-TASK-SUM).',
  'Birinchi qatorda n, ikkinchisida n ta son beriladi.',
  'Soni oʼqib, qatorni boʼlib olamiz: <code>input().split()</code>.',
  'Har birini songa oʼtkazib yigʼamiz: <code>sum(map(int, ...))</code>.',
  'Bitta qatorda javob chiqaramiz — ortiqcha matn qoʼshmaslik kerak.'
 ],
 'n = int(input())
sonlar = list(map(int, input().split()))

yigindi = 0
for x in sonlar:
    yigindi += x

print(yigindi)',
 'python',
 ARRAY[
  '<code>map(int, ...)</code> — butun qatorni bir amalda songa oʼtkazadi.',
  '<code>for x in sonlar</code> — O(n), bir marta aylanish: 2000 ms ichiga bemalol sigʼadi.',
  'Javob soʼz bilan chiqmasligi kerak: faqat son, aks holda <code>WRONG_ANSWER</code>.',
  'Xuddi shu ishni <code>sum(sonlar)</code> ham qiladi — qisqa va oʼqiladigan.'
 ],
 'VisuAlgo — saralash va qidiruvni jonli koʼrish', 'https://visualgo.net/en/sorting'
);

-- --------------------------------------------------------------------------
-- IAKTT1105 · 14 ma'ruza (M7 V902 da allaqachon toʼldirilgan)
-- --------------------------------------------------------------------------
INSERT INTO seed_content
(subject, lesson, hook, why, explain, concepts, mistake, checkin, worked, code, lang, codewalk, link_label, link_url) VALUES

('IAKTT1105','M1',
 '<p>1990-yilda bir bank filiali oyiga 3 000 hujjatni qoʼlda qayta ishlar edi. Bugun bir tizim kuniga 3 million tranzaksiyani yozadi va xar birini iz qoldirib tekshiradi. Oʼzgarish texnikada emas — <em>axborotning iqtisodiy roʼlida</em>.</p>',
 '<p>Iqtisodchi uchun AKT — qoʼlida ushlab turgan asbob, nazarida oʼtish kerak boʼlgan falsafa emas. Bu kurs aynan shu asbobni tushunishdan boshlanadi: axborot qanday paydo boʼladi, qanday saqlanadi va qanday qarorga aylanadi.</p>',
 ARRAY[
  '<h2>Uch asosiy tushuncha</h2><p><strong>Maʼlumot (data)</strong> — qayd etilgan fakt: sana, summa, miqdor. Oʼz-oʼzidan qimmatli emas. <strong>Axborot (information)</strong> — maʼlumotga kontekst va maʼno berilganda paydo boʼladi: "sotuv avgustda 12% oshdi". <strong>Bilim (knowledge)</strong> — axborotni qarorga aylantirish qobiliyati.</p>',
  '<p><strong>Kommunikatsiya</strong> — axborotni bir nuqtadan ikkinchisiga uzatish. Uning sifatini uch oʼlcham belgilaydi: tezlik (qancha maʼlumot/sekund), kechikish (javob qancha kutadi) va ishonchlilik (buzilmasdan yetib boradimi). Iqtisodiy tilga oʼgirganda — bu xarajat va risk.</p>',
  '<p><strong>Axborot tizimi</strong> — maʼlumotni yigʼish, saqlash, qayta ishlash va taqsimlashning <em>tashkiliy va texnik</em> yechimi. Diqqat: "tashkiliy" qismi texnikadan kam emas — xodim, jarayon va javobgarlik aniqlanmagan tizim ishlaydi, lekin natija bermaydi.</p>',
  '<h2>Raqamli transformatsiya nima</h2><p>Qogʼoz hujjatni PDF ga oʼtkazish — raqamlashtirish (digitisation). Jarayonni axborot oqimi atrofida qayta loyihalash — raqamli transformatsiya. Farq iqtisodiy: birinchisi xarajatni ozgina kamaytiradi, ikkinchisi jarayonning oʼzini yoʼqol qiladi yoki boshqacha qiladi.</p>',
  '<p>Bugungi iqtisodchidan kutiladigan — texnik mutaxassis boʼlish emas, <em>texnik yechimni tushunish va baholay olish</em>: qanday maʼlumot kerak, qancha turadi, qanday xavf bor va natija qanday oʼlchanadi.</p>'
 ],
 ARRAY[
  '<strong>Maʼlumot</strong> — qayd etilgan fakt, maʼnosiz xom ashyo.',
  '<strong>Axborot</strong> — maʼnoga ega, qarorga yaroqli maʼlumot.',
  '<strong>Axborot tizimi</strong> — inson, jarayon, maʼlumot va texnologiya yigʼindisi.',
  '<strong>Raqamlashtirish</strong> — mavjud jarayonni raqamli koʼrinishga oʼtkazish.',
  '<strong>Raqamli transformatsiya</strong> — jarayonni raqamli muhitga moslab qayta qurish.',
  '<strong>Axborot oqimi</strong> — maʼlumotning tizim ichidagi harakati va javobgarligi.'
 ],
 '<p>Keng tarqalgan tushunmovchilik — <strong>texnologiyani maqsad deb hisoblash</strong>. "CRM sotib oldik, sotuv oshadi" degan fikr koʼp loyihaning muvaffaqiyatsizligiga olib keladi: jarayon oʼzgarmasa, vosita eski ishni tezlashtiradi xolos. Loyiha baholanayotganida avval savol beriladi: <em>qaysi qaror, qaysi maʼlumot tufayli yaxshilanadi?</em></p>',
 ARRAY[
  'Maʼlumot va axborotni oʼz sohangizdan bir misolda farqlang.',
  'Raqamlashtirish va raqamli transformatsiya farqi nimada?',
  'Axborot tizimining texnik boʼlmagan qismlarini sanang.',
  'Bitta jarayonni tanlab, uni axborot oqimi boʼyicha tavsiflang.'
 ],
 ARRAY[
  'Masala: kichik savdo korxonasida ombor hisobini raqamli tizimga oʼtkazish.',
  'Eski jarayonni qadamma-qadam yozing (qabul, yozuv, tekshiruv, hisobot).',
  'Har qadamda: kim bajaradi, qanday maʼlumot kiradi, nima chiqadi?',
  'Qaysi qadam takroriy va avtomatlashtirishga arziyini belgilang.',
  'Natijani oʼlchash mezonini oldindan yozing: qoldiq xatoligi, hisobot vaqti.'
 ],
 NULL, NULL, NULL,
 'World Bank — Digital Development yillik hisoboti', 'https://www.worldbank.org/en/topic/digitaldevelopment'
),

('IAKTT1105','M2',
 '<p>Bir onlayn doʼkon Black Friday da 40 barobar koʼp mijoz qabul qiladi. Server sigʼimi yetmasa sayt toʼxtaydi — bir soatlik savdo yoʼqoladi. Infratuzilma — koʼrinmaydigan, lekin toʼgʼridan-toʼgʼri daromadga taʼsir qiladigan omil.</p>',
 '<p>Korxona xarajatlarining bir qismi aynan shu "quvvat"ga ketadi. Uni toʼgʼri baholash — kapital va operatsion xarajatni farqlashni bilishni talab qiladi.</p>',
 ARRAY[
  '<h2>Infratuzilmaning qatlamlari</h2><p><strong>Apparat taʼminoti</strong> — serverlar, saqlash tizimlari, tarmoq uskanllari, ish stoli kompyuterlari. <strong>Platforma</strong> — operatsion tizim, maʼlumotlar bazasi, ilova serveri. <strong>Tarmoq</strong> — mahalliy (LAN) va global (WAN) ulanishlar. Har qatlamning oʼz sigʼimi, xarajati va zaifligi bor.</p>',
  '<p><strong>Sigʼim va uning oʼlchamlari.</strong> Oʼtkazuvchanlik (bps) — kanal qancha maʼlumot oʼtkazadi. Kechikish (latency, ms) — javob qancha kutadi. Mavqielik (%) — xizmat qancha vaqtda ishlab turadi: 99.9% yiliga ≈ 8 soat uzilish degani. Savdo uchun kechikish ham, uzilish ham toʼgʼridan-toʼgʼri pul.</p>',
  '<p><strong>Maʼlumotlar markazi.</strong> Hisoblash va saqlash quvvatini bir joyda toʼplaydi. Afzalligi — boshqaruv va tejamkorlik; xavfi — bitta nuqtada uzilish. Shu sababli zaxira maydon va geografik tarqatish standart amaliyot.</p>',
  '<p><strong>Ishonchlilikni rejalashtirish.</strong> Ikki raqam javobgar: RTO — uzilishdan keyin qancha vaqtda tiklanadi; RPO — qancha oxirgi maʼlumot yoʼqolishi mumkin. Zaxira nusxa shu ikkiga qarab tanlanadi, "har kuni bir marta" degan anʼana hamma joyga mos kelmaydi.</p>',
  '<p><strong>Muqovavat (redundancy).</strong> Bitta uskanlni ikkita bilan almashtirish xarajatni oshiradi, lekin uzilish narxidan kam. Bu — iqtisodiy muvozanat masalasi, texnik tanlov emas.</p>'
 ],
 ARRAY[
  '<strong>Oʼtkazuvchanlik</strong> — kanalning sekundiga uzata oladigan maʼlumoti.',
  '<strong>Kechikish (latency)</strong> — soʼrov javobigacha boʼlgan vaqt.',
  '<strong>Mavqielik</strong> — xizmat ishlab turgan vaqt ulushi.',
  '<strong>RTO / RPO</strong> — tiklanish vaqti va yoʼl qoʼyiladigan maʼlumot yoʼqolishi.',
  '<strong>Muqovavat</strong> — uzilishga bardosh berish uchun qoʼshimcha resurs.',
  '<strong>Skallash</strong> — gorizontal (koʼproq uskanl) va vertikal (kuchli bitta uskanl).'
 ],
 '<p>Xarajatni hisoblaganda <strong>faqat sotib olish narxini yozish</strong> — eng keng xato. Elektr, sovutish, maʼmuriy ish, litsenziya va uzilish narxi bilan birga <em>egallash jami qiymati</em> (TCO) hisoblanadi. Ikkinchi xato — zaxira nusxa olib qoʼyib, uni <strong>hech qachon tiklab koʼrmaslik</strong>: kerak boʼlganda fayl buzilgan chiqadi.</p>',
 ARRAY[
  '99.5% va 99.99% mavqielik yiliga qancha uzilish?',
  'RTO va RPO ni oʼz sohangizda bir misolda tushuntiring.',
  'Gorizontal skallashning bitta afzalligi va kamchiligi?',
  'TCO tarkibiga nimalar kiradi?'
 ],
 ARRAY[
  'Masala: filiallararo hisobot tizimi uchun infratuzilma tanlash.',
  'Yuklamani baholang: nechta foydalanuvchi, qachon choʼqqi?',
  'Mavqielik talabini belgilang — uzilish narxidan kelib chiqib.',
  'Ikkita variantni TCO boʼyicha solishtiring: oʼz serveri va bulut.',
  'RTO/RPO ga qarab zaxira rejasi va muqovavatni tanlang.'
 ],
 NULL, NULL, NULL,
 'NIST — Computer Security Resource Center', 'https://csrc.nist.gov/'
),

('IAKTT1105','M3',
 '<p>Korxona buxgalteriyasi, ombori va kadrlari uchun uch dastur sotib olgan. Uchala bazada uch xil mijoz nomi bor va hech qaysi biri boshqasiga bilmaydi. Dasturlar koʼp, maʼlumot yoʼq. Mana shu muammoning yechimi — sinf tasnifi.</p>',
 '<p>Dasturiy mahsulot tanlovi — millionlab xarajat qarori va u bir marta qabul qilinmaydi: almashtirish narxi juda yuqori.</p>',
 ARRAY[
  '<h2>Dasturlar darajasi</h2><p><strong>Ofis dasturlari</strong> — matn, jadval, taqdimot, pochta. Universal, arzon, lekin jarayonni boshqarmaydi. <strong>Maxsus dasturlar</strong> — buxgalteriya, sugʼurta, bank operatsiyalari: soha qoidalarini ichida saqlaydi. <strong>Korxona darajasi (ERP)</strong> — barcha funksiyalarni bitta maʼlumot bazasi va yagona mantiqda birlashtiradi.</p>',
  '<p><strong>CRM</strong> — mijoz bilan aloqa tarixi va savdo quvuri. <strong>SCM</strong> — yetkazib berish zanjiri. <strong>HRM</strong> — kadrlar. Ular ERP bilan integratsiyada ishlaydi; integratsiya boʼlmasa maʼlumot ikki marta kiritiladi va ziddiyat paydo boʼladi.</p>',
  '<p><strong>Ochiq va yopiq kod.</strong> Yopiq (proprietary) — litsenziya toʼlanadi, qoʼllab-quvvatlash kafolatlangan, lekin manbaga kirish yoʼq va yetkazib beruvchiga bogʼliqlik kuchli. Ochiq kodli — litsenziya bepul, moslashuvchan, ammo qoʼllab-quvvatlash va mutaxassis xarajati oʼz zimnangizda.</p>',
  '<p><strong>SaaS modeli</strong> — dasturni obuna bilan internet orqali ishlatish. Boshlangʼich xarajat past, tez ishga tushadi; uzoq muddatda jami toʼlov yuqori boʼlishi va maʼlumot joylashuvi cheklanishi mumkin.</p>',
  '<p>Tanlov mezonlari — funksional moslik, integratsiya imkoniyati, oʼsish chegarasi, maʼlumot xavfsizligi va <em>egallash jami qiymati</em>.</p>'
 ],
 ARRAY[
  '<strong>ERP</strong> — korxona resurslarini rejalashtirish tizimi.',
  '<strong>CRM / SCM / HRM</strong> — mijoz, zanjir va kadr boshqaruvi.',
  '<strong>Yopiq kod</strong> — manba yopiq, litsenziya toʼlanadi.',
  '<strong>Ochiq kod</strong> — manba ochiq, qoʼllab-quvvatlash alohida xarajat.',
  '<strong>SaaS</strong> — obuna asosida internet orqali taqdim etiladigan ilova.',
  '<strong>TCO</strong> — sotib olish, joriy etish va ekspluatatsiya jami qiymati.'
 ],
 '<p>Loyihalarda eng qimmat xato — <strong>funktsiyalar roʼyxatiga qarab tanlash</strong>. "Bunda modul ham bor" degan dastur jarayonga mos kelmasa, joriy etishda uni qayta yozishga toʼgʼri keladi. Ikkinchisi — <strong>joriy etish va oʼqitish xarajatini unutish</strong>: litsenziya narxidan koʼp marotaba koʼp vaqt va pul ketadi; xodimlar ishlatmasa, tizim ishlamaydi.</p>',
 ARRAY[
  'ERP ning CRM dan farqi nimada?',
  'Ochiq kodli dastur nega "bepul" emas?',
  'SaaS ga oʼtishning bitta riski nima?',
  'TCO ni qanday hisoblaysiz?'
 ],
 ARRAY[
  'Masala: 60 kishilik savdo korxonasi uchun CRM tanlash.',
  'Talablarni yozing: mijoz tarixi, savdo quvuri, hisobot, mobil kirish.',
  'Har bir variantni moslik, integratsiya va TCO boʼyicha baholang.',
  'Maʼlumot joylashuvi va xavfsizlik talabini tekshiring.',
  'Bir oylik sinov (pilot) va oʼtish rejasini shartnomaga kiriting.'
 ],
 NULL, NULL, NULL,
 'Gartner — ERP boʼyicha bozor tahlili', 'https://www.gartner.com/en/products/erp'
),

('IAKTT1105','M4',
 '<p>Ikkita xodim bir xil maʼlumotdan ikki xil xulosa chiqardi. Bittasi ustunni notoʼgʼri tanlab, ikkinchisi qoʼshimcha qatorni yigʼib yubordi. Jadval — tez ishlaydigan, lekin xatoni yashiradigan vosita.</p>',
 '<p>Koʼpgina iqtisodiy hisob-kitob hali ham jadval redaktorda bajariladi. Uni intizom bilan yuritish — malaka belgisi.</p>',
 ARRAY[
  '<h2>Manzil va formula</h2><p>Formulada manzil <strong>nispiy</strong> (A1) boʼlsa, nusxa koʼchirganda siljiydi; <strong>mutlaq</strong> ($A$1) boʼlsa, qotib turadi. Aralash variant ($A1 yoki A$1) qator yoki ustunni qatʼiy qiladi. Koʼp hujayrali hisob-kitobda shu uch xildan xato chiqadi.</p>',
  '<p><strong>Funksiyalar.</strong> <code>SUM, AVERAGE, COUNT</code> — umumlashtirish; <code>IF, IFS, SWITCH</code> — tanlash; <code>SUMIF/COUNTIF/SUMIFS</code> — shart boʼyicha yigʼish; <code>VLOOKUP/XLOOKUP</code> — boshqa jadvaldan qiymat olish. XLOOKUP eski VLOOKUP ning yoʼnalish va ustun cheklovlarini yoʼqol qiladi.</p>',
  '<p><strong>Pivot jadval</strong> — minglab qatorni bir necha soniyada guruhlaydi: satr, ustun, qiymat va filtr maydonlari. Bu — maʼlumotni soʼrovsiz tahlil qilishning eng tezkor yoʼli.</p>',
  '<p><strong>Maʼlumotni tekshirish.</strong> Kiritishga ruxsat etilgan qiymatlar roʼyxatini qoʼyish (masalan faqat "ha/yoʼq" yoki sana oraligʼi) — xatoning kelish manbaini quritadi.</p>',
  '<p><strong>Shartli formatlash</strong> — qiymat chegaradan chiqsa katakni boʼyaydi. Katta jadvalda anomaliyani koʼz bilan topishni osonlashtiradi.</p>'
 ],
 ARRAY[
  '<strong>Nispiy manzil</strong> — nusxa koʼchirilganda siljiydigan manzil (A1).',
  '<strong>Mutlaq manzil</strong> — qotib turadigan manzil ($A$1).',
  '<strong>Pivot jadval</strong> — maʼlumotni guruhlab umumlashtiruvchi tahlil vositasi.',
  '<strong>Shartli yigʼish</strong> — SUMIF/SUMIFS: mezon boʼyicha filtrlab hisoblash.',
  '<strong>Qidiruv funksiyasi</strong> — VLOOKUP/XLOOKUP: kalit boʼyicha qiymat olish.',
  '<strong>Shartli formatlash</strong> — qiymatga qarab katak koʼrinishini oʼzgartirish.'
 ],
 '<p>Eng keng muammo — <strong>model ichiga qoʼlda son kiritish</strong>. Bir oy oʼtgach, qaysi katak formula ekanligini hech kim eslamaydi va hisobot notoʼgʼri boʼlib qoladi. Yechim: kirish sahifasi alohida, formula sahifasi alohida boʼlsin va kirish kataklari boshqa rangda belgilansin. Ikkinchi xato — <code>AVERAGE</code> ni boʼsh kataklar bilan ishlash: ular hisobga olinmaydi, natija kutilgandan yuqori chiqadi.</p>',
 ARRAY[
  '$A$1 va A1 farqi nimada?',
  'SUMIFS COUNTIFS dan nimasi bilan ajralib turadi?',
  'Pivot jadval qachon oddiy formuladan afzal?',
  'Boʼsh katak AVERAGE ga qanday taʼsir qiladi?'
 ],
 ARRAY[
  'Masala: 12 oylik savdo boʼyicha toifalar kesimida hisobot.',
  'Maʼlumotni jadval koʼrinishida joylang: sana, toifa, summa.',
  'Har bir katakda formula emas, xom qiymat boʼlsin.',
  'Pivot jadvalda satrga toifani, qiymatga summani qoʼying.',
  'Oylik oʼsishni hisoblash uchun qoʼshimcha ustun oching va foizni tekshiring.'
 ],
 NULL, NULL, NULL,
 'Microsoft Learn — Excel funksiyalari qoʼllanmasi', 'https://learn.microsoft.com/en-us/office/client-developer/excel/excel-functions'
),

('IAKTT1105','M5',
 '<p>Moliya direktoriga 8 daqiqa beriladi. Bir slaydda 240 soʼz, ikkinchisida bitta grafik va bitta xulosa. Qaysi biri qaror qabul qildirdi? Taqdimot — tahlilning yakuniy mahsuli, chunki yetib bormagan natija natija emas.</p>',
 '<p>Tahlil qadar uni yetkazish ham professional koʼnikma. Xuddi shu maʼlumotni turli auditoriyaga boshqacha yetkazish talab etiladi.</p>',
 ARRAY[
  '<h2>Bitta slayd — bitta gʼoya</h2><p>Har slayd bitta xulosaga javob beradi va sarlavha oʼsha xulosaning oʼzi boʼladi: "Sotuv 12% oshdi" — "Sotuv tahlili" emas. Auditoriya sarlavhani oʼqib, qolgani tinglaydi.</p>',
  '<p><strong>Vizual iyerarxiya.</strong> Katta shrift — asosiy fikr, kichigi — tafsilot; rang — eʼtibor burchagi. Bitta slaydda uchdan ortiq rang ishlatilsa, urgʼu yoqoladi.</p>',
  '<p><strong>Grafikni toʼgʼri tanlash.</strong> Trend — chiziqli diagramma; qiyos — ustunli; ulush — halqali yoki steklangan, lekin 5-6 toifadan koʼp boʼlsa oʼqilmaydi; ikki oʼzgaruvchi bogʼliqligi — tarqalish (scatter). Donut va 3D diagrammalar koʼpincha chiroy uchun tanlanadi va aniqlikni yoʼqol qiladi.</p>',
  '<p><strong>Raqamli hikoya.</strong> Kontekst → muammo → dalil → xulosa tartibi esda qoladi. Har raqamga taqqos kerak: "12% oshdi" — oʼtgan yilga nisbatanmi, rejaga nisbatanmi?</p>',
  '<p><strong>Nutqqa tayyorgarlik.</strong> Vaqt bilan cheklash, savol-javob uchun zaxira slaydlar, texnik muammo boʼlsa chop etiladigan bir sahifalik xulosa — professional zaxira.</p>'
 ],
 ARRAY[
  '<strong>Xulosa-sarlavha</strong> — slayd sarlavhasi natijaning oʼzini ayitadi.',
  '<strong>Vizual iyerarxiya</strong> — oʼlcham, rang va joy orqali eʼtibor boshqaruvi.',
  '<strong>Chartjunk</strong> — maʼlumotni oʼqishga yordam bermaydigan bezak.',
  '<strong>Sparkline</strong> — kichik, qator ichidagi trend grafigi.',
  '<strong>Zaxira slayd</strong> — savollarga javob uchun tayyorlab qoʼyilgan sahifa.'
 ],
 '<p>Eng koʼp uchraydigan xato — <strong>slayd matnini oʼqib berish</strong>. Matn auditoriyada, nutq sizda boʼlishi kerak; ikkisi bir xil boʼlsa, birortasi ortiqcha. Ikkinchisi — <strong>oʼq oʼrniga ulush koʼrsatish</strong>: 3 ta toifani solishtirishda foiz aldaydi, chunki asos turlicha.</p>',
 ARRAY[
  'Nega sarlavha xulosa boʼlishi kerak?',
  'Ulush koʼrsatishda qaysi diagramma aldaydi?',
  'Auditoriyaga qarab bir tahlilni qanday boshqacha yetkazasiz?',
  'Zaxira slayd nimani oldini oladi?'
 ],
 ARRAY[
  'Masala: choraklik savdo natijasini 5 slaydda yetkazish.',
  'Bitta xulosa yozing — auditoriya nima bilib chiqishi kerak?',
  'Unga kerakli 4 ta dalilni tanlang, qolganini ilovaga qoʼying.',
  'Har dalil uchun eng sodda grafikni tanlang.',
  'Vaqt bilan sinab koʼring va 8 daqiqaga sigʼdiring.'
 ],
 NULL, NULL, NULL,
 'Storytelling with Data — ochiq maqolalar toʼplami', 'https://www.storytellingwithdata.com/blog'
),

('IAKTT1105','M6',
 '<p>Toshkentdagi ombor Nukusdagi filialni "onlayn" koʼradi. Lekin kabel uzilsa nima boʼladi? Toʼlov tizimi kechikishni sezsa mijoz kartani ikki marta bosadi — ikki marta yechiladi. Tarmoq — iqtisodiy jarayonning invisibıl asosi.</p>',
 '<p>Tarmoq xizmatini tanlash — oʼtkazuvchanlik, kechikish va tarifning muvozanati; bu toʼgʼridan-toʼgʼri xarajat va xizmat sifati qarori.</p>',
 ARRAY[
  '<h2>Qamrov va topologiya</h2><p><strong>LAN</strong> — bino ichi, tez va nazoratlangan. <strong>MAN</strong> — shahar miqyosi. <strong>WAN</strong> — geografik tarqalgan tarmoqlar. Topologiya (yulduz, halqa, toʼr) ishonchlilik va narxni belgilaydi: yulduzda markaziy uskanl yiqilsa — hamma toʼxtaydi; toʼrda yoʼnalishlar koʼp, boshqarish murakkab.</p>',
  '<p><strong>Protokollar.</strong> TCP/IP — ishonchli yetkazish; HTTP/HTTPS — veb almashinuv (S — shifrlangan); DNS — nomni manzilga aylantirish; FTP fayl uzatish uchun. DNS ishlamasa, internet "bor" boʼlsa ham hech narsa ochilmaydi.</p>',
  '<p><strong>Xizmatlar.</strong> Veb, pochta (SMTP/IMAP), bulutli saqlash, IP telefoniya, video konferensiya. Har biri tarmoqdan boshqa narsani talab qiladi: pochta — ishonchlilik, video — kam kechikish.</p>',
  '<p><strong>Xavfsizlik chegarasi.</strong> Marshrutizator yoʼnalishni belgilaydi, ekran (firewall) ruxsat etilmagan trafikni toʼsadi, VLAN bitta fizik tarmoqni mantiqiy boʼladi — masalan, buxgalteriya trafigini mehmon Wi-Fi dan ajratish uchun.</p>',
  '<p><strong>VPN</strong> — ochiq tarmoqda shifrlangan kanal. Masofadan ishlash va filiallararo aloqa uchun standart yechim.</p>'
 ],
 ARRAY[
  '<strong>LAN / MAN / WAN</strong> — qamrov maydoniga koʼra tasnif.',
  '<strong>IP manzil</strong> — tarmoqdagi uskanl identifikatori.',
  '<strong>DNS</strong> — nom va manzil oʼrtasidagi "telefon katalogi".',
  '<strong>Ekran (firewall)</strong> — ruxsat etilmagan trafikni toʼsuvchi qatlam.',
  '<strong>VLAN</strong> — bitta fizik tarmoqning mantiqiy boʼlinishi.',
  '<strong>VPN</strong> — ochiq tarmoq ustidagi shifrlangan kanal.'
 ],
 '<p>Tarmoq loyihasida <strong>bugungi trafigini hisoblab, oʼsishini hisoblamaslik</strong> — bir-ikki yildan keyin kanal toʼyib, xizmat sifati tushadi. Ikkinchi xato — <strong>simsiz tarmoqni ochiq qoldirish</strong): mehmon uchun alohida tarmoq ajratilmasa, ichki baza ham shu kanal orqali koʼrinadi.</p>',
 ARRAY[
  'Nega HTTPS HTTP dan xavfsizroq?',
  'DNS uzilganda nima boʼladi?',
  'VLAN nimani ajratadi?',
  'Kechikish va oʼtkazuvchanlik farqini misolda tushuntiring.'
 ],
 ARRAY[
  'Masala: uch filialli korxona uchun tarmoq tanlash.',
  'Har filialda nechta uskanl va qancha trafik borligini sanang.',
  'Filiallararo qancha maʼlumot almashinadi — markazga bogʼliqlikni baholang.',
  'Uch variantni solishtiring: alohida internet, VPN, boshqariladigan WAN.',
  'Uzilish narxini hisobga olib zaxira kanal rejalashtiring.'
 ],
 NULL, NULL, NULL,
 'Cloudflare Learning Center — tarmoq va internet', 'https://www.cloudflare.com/learning/'
),

('IAKTT1105','M8',
 '<p>Buxgalter har oy oxirida bir xil 12 ta amalni Excel da takrorlaydi. Bir kuni u bu ishni qoida koʼrinishida yozsa, boshqasi ham ishlatishi mumkin boʼladi. Mana shu — algoritmlashtirishning kundalik maʼnosi.</p>',
 '<p>Iqtisodchi dasturchi boʼlishi shart emas, lekin masalani qadamlarga ajrata olishi va yechimning qancha "qimmat" tushishini baholay olishi shart.</p>',
 ARRAY[
  '<h2>Algoritm va uning xossalari</h2><p>Algoritm — chekli, aniq, tekshiriladigan qadamlar. Uning toʼrt xossasi: <em>aniqlik</em> (har qadam bir maʼnoli), <em>omaviylik</em> (har qanday kirishda ishlaydi), <em>natijadorlik</em> (chekli qadamda javob beradi), <em>cheklilik</em> (abadiy davom etmaydi).</p>',
  '<p><strong>Blok-sxema</strong> — algoritmning grafik tili: oval — boshlash/tugash, parallelogramm — kiritish/chiqish, toʼrtburchak — amal, romb — shart. Murakkab jarayonni kelishilgan belgilar bilan yozish jamoaviy ishning asosi.</p>',
  '<p><strong>Oʼzgaruvchi va tip.</strong> Oʼzgaruvchi — qiymatga berilgan nom; tip — qiymatlar toʼplami va ruxsat etilgan amallar. Iqtisodiy maʼlumotda tur ahamiyatli: sana, pul va foiz boshqa-boshqa amallarga boʼysunadi.</p>',
  '<h2>Murakkablik — Big-O</h2><p>Big-O qadamlar sonining kirish hajmiga qanday oʼsishini koʼrsatadi. O(1) — doimiy; O(log n) — ikkilik qidiruv; O(n) — chiziqli; O(n log n) — samarali saralash; O(n²) — ichma-ich sikl.</p>',
  '<p>Bu — iqtisodiy tushuncha: <strong>chegaraviy xarajat</strong>. n ikki barobar oʼssа, O(n) ikki barobar, O(n²) toʼrt barobar koʼpayadi. Katta hajmli maʼlumotda farq server xarajatiga aylanadi.</p>'
 ],
 ARRAY[
  '<strong>Algoritm</strong> — yechimning chekli qadamlar ketma-ketligi.',
  '<strong>Blok-sxema</strong> — algoritmning standart belgilar bilan grafik tasviri.',
  '<strong>Oʼzgaruvchi</strong> — qiymatga biriktirilgan nom.',
  '<strong>Big-O</strong> — qadamlar sonining oʼsish bahosi.',
  '<strong>Kirish hajmi (n)</strong> — masala oʼlchami: elementlar yoki qatorlar soni.',
  '<strong>Andoza (pattern)</strong> — takrorlanuvchi yechim shakli: hisoblash, qidirish, saralash.'
 ],
 '<p>Eng koʼp uchraydigan xato — <strong>toʼgʼri, lekin sekin yechimni baholamaslik</strong>. O(n²) dastur 100 qatorda "ishlaydi", 100 000 qatorda soatlab turadi. Yechimni kichik hajmda emas, kutilayotgan hajmda sinash kerak. Ikkinchisi — shartnomada "tez ishlaydi" deb yozib, oʼlchov mezonini kelishmaslik.</p>',
 ARRAY[
  'Algoritmning toʼrt xossasini bir misolda koʼrsating.',
  'Romb belgisi nimani ifodalaydi?',
  'O(n) va O(n log n) farqini raqam bilan tushuntiring.',
  'Nega kichik maʼlumotda "tez" dastur katta hajmda sekin boʼladi?'
 ],
 ARRAY[
  'Masala: oylik hisobotda har xarid uchun toifasi boʼyicha jami yigʼish.',
  'Kirishni aniqlaymiz: sana, toifa, summa.',
  'Variant A — har toifa uchun butun roʼyxatni qayta aylanish: O(k·n).',
  'Variant B — bir marta aylanib, lugʼhaga yigʼish: O(n).',
  'n = 100 000, k = 20 boʼlganda qadam farqini hisoblang.'
 ],
'toifalar = {}

for xarid in xaridlar:
    toifalar[xarid.toifasi] = toifalar.get(xarid.toifasi, 0) + xarid.summa',
 'python',
 ARRAY[
  'Bitta sikl — har yozuvni bir marta koʼradi: O(n).',
  '<code>dict</code> kalit boʼyicha qiymatni deyarli O(1) da topadi.',
  '<code>get(kalit, 0)</code> — kalit boʼlmasa 0 qaytaradi: xato oldini oladi.',
  'Ikki sikl ishlatilganda bu 20×100 000 = 2 million qadam boʼlar edi.'
 ],
 'CS50x — algoritmlashtirishga kirish (bepul kurs)', 'https://cs50.harvard.edu/x/'
),

('IAKTT1105','M9',
 '<p>Ikki boʼlim bir xil mijoz haqida turli raqam aytadi. Buxgalteriyada — 12 mln, savdoda — 15 mln. Ikkalasi ham "toʼgʼri" hisoblagan, chunki ikki tizim bir-biriga bogʼlanmagan. Muammo hisobda emas, tizimning chegarasida.</p>',
 '<p>Tashkilotda axborot tizimi — nafaqat dastur, balki javobgarlik taqsimoti. Uni tushunmasa, integratsiya loyihalari muvaffaqiyatsiz tugaydi.</p>',
 ARRAY[
  '<h2>Tarkibiy qismlar</h2><p><strong>Kirish</strong> — maʼlumot qayerdan va qanday keladi (hujjat, sensor, foydalanuvchi). <strong>Qayta ishlash</strong> — biznes mantiq: hisob-kitob, tekshiruv, tasnif. <strong>Saqlash</strong> — maʼlumotlar bazasi va hujjatlar ombori. <strong>Chiqish</strong> — hisobot, panel, API orqali boshqa tizimga uzatish. <strong>Qayta aloqa</strong> — natijani baholab jarayonni toʼgʼrilash.</p>',
  '<p>Besh qismning biri boʼlmasa, tizim "ishlaydi", lekin ishonchli javob bermaydi. Eng koʼp unutilgani — qayta aloqa.</p>',
  '<h2>Daraja tasnifi</h2><p><strong>Operatsion tizimlar</strong> — kundalik operatsiyalarni yozadi (kassa, buyurtma). <strong>Boshqaruv axborot tizimlari (MIS)</strong> — haftalik/oylik hisobotlar, standart savollarga javob. <strong>Qaror qabul qilishni qoʼllab-quvvatlash (DSS)</strong> — nima boʼladi-agardi ssenariylari. <strong>Strategik / rahbarlik tizimlari (EIS)</strong> — uzoq muddatli koʼrsatkichlar paneli.</p>',
  '<p>Yuqoriga chiqqan sari maʼlumot umumlashadi va tuzilmaganlashadi: operatsion darajada aniq sonlar, strategik darajada tendensiyalar.</p>',
  '<p><strong>Hayot sikli.</strong> Tahlil → loyihalash → joriy etish → ekspluatatsiya → yangilanish. Har bosqichda xarajat tabiati oʼzgaradi va foydalanuvchi ishtiroki boʼlmasa, tizim talabga javob bermay qoladi.</p>'
 ],
 ARRAY[
  '<strong>Tizim</strong> — maqsadga qaratilgan oʼzaro bogʼliq qismlar toʼplami.',
  '<strong>Operatsion tizim</strong> — kundalik amaliyotni qayd etadi.',
  '<strong>MIS / DSS / EIS</strong> — boshqaruv, qoʼllab-quvvatlash va strategik darajalar.',
  '<strong>Maʼlumotlar ombori</strong> — tahlil uchun tayyorlangan integratsiyalangan saqlash.',
  '<strong>Qayta aloqa</strong> — natijani baholab boshqaruvni toʼgʼrilash.',
  '<strong>Hayot sikli</strong> — tizimning tahlildan yangilanishgacha boʼlgan bosqichlari.'
 ],
 '<p>Tizim loyihasida <strong>faqat texnikaga eʼtibor berish</strong> — foydalanuvchi jarayoni oʼzgarmasa, tizim ishlatilmaydi va xarajat bekor ketadi. Ikkinchi xato — <strong>maʼlumot egasini belgilamaslik</strong>: har maydon kimning javobgarligi aniq boʼlmasa, ziddiyatli qiymat tuzatilmay qoladi.</p>',
 ARRAY[
  'Besh tarkibiy qismni oʼz korxongiz misolida sanang.',
  'MIS va DSS farqi nimada?',
  'Nega qayta aloqa boʼlmasi tizim ishonchsiz?',
  'Maʼlumot egasi deganda nima nazarda tutiladi?'
 ],
 ARRAY[
  'Masala: sotuv tahlili tizimini loyihalash.',
  'Kim qanday savol beradi — operatsion yoki strategik?',
  'Maʼlumot manbalari va ularning javobgarini yozing.',
  'Kirish → qayta ishlash → chiqish zanjirini chizing.',
  'Qayta aloqa mexanizmini belgilang: kim, qachon, nimani toʼgʼrilaydi.'
 ],
 NULL, NULL, NULL,
 'Gartner — IT Service Management', 'https://www.gartner.com/en/information-technology/topics/itsm'
),

('IAKTT1105','M10',
 '<p>Pasport almashtirish uchun uch idoraga borish, har birida navbatda turish — jami bir kun. Bir oynadan keyin xizmat bir darvozadan: 20 daqiqa, hujjatlar avtomatik uzatiladi. Farq — idoralar oʼrtasidagi axborot oqimida.</p>',
 '<p>Elektron hukumat — davlat xizmatining narxini pasaytiradi: ham fuqaro vaqti, ham byudjet xarajati hisobiga.</p>',
 ARRAY[
  '<h2>Uch yoʼnalish</h2><p><strong>G2C</strong> (government-to-citizen) — fuqaroga xizmat: roʼyxatdan oʼtish, toʼlov, hujjat olish. <strong>G2B</strong> — biznesga: litsenziya, davlat xaridi, soliq hisoboti. <strong>G2G</strong> — idoralararo: maʼlumot almashinuvi va qoʼshma jarayonlar. Eng qiyini G2G, chunki u huquqiy kelishuvni talab qiladi.</p>',
  '<p><strong>Bir darvozа</strong> — fuqaro bir nuqtadan kiradi, qaysi idora qancha bosqich oʼtayotganini bilmaydi. Bu texnik yechim emas, <em>jarayonni qayta loyihalash</em> qarori.</p>',
  '<p><strong>Raqamli identifikatsiya.</strong> Bir martalik kod, biometrik tasdiq, elektron imzo. Imzo hujjat muallifligini va oʼzgartirilmaganligini tasdiqlaydi — qogʼozdagi muhrning raqamli ekvivalenti.</p>',
  '<p><strong>Ochiq maʼlumotlar.</strong> Davlat maʼlumotini qayta ishlatishga ochiq berish — shaffoflik va yangi xizmatlar uchun asos. Format mashina oʼqiy oladigan boʼlishi kerak (CSV/JSON), PDF skan emas.</p>',
  '<p><strong>Xizmat kartasi</strong> — bosqichlar, muddat, natija va javobgar idora. Xizmatni raqamlashtirish shu kartadan boshlanadi; mavjud qogʼoz protsessini koʼchirish koʼpincha eski ortiqcha bosqichni ham raqamga oʼtkazadi.</p>'
 ],
 ARRAY[
  '<strong>G2C / G2B / G2G</strong> — xizmat yoʼnalishlari.',
  '<strong>Bir darvozа</strong> — bitta kirish nuqtasi orqali xizmat olish.',
  '<strong>Elektron imzo</strong> — mualliflik va yaxlitlikni tasdiqlovchi raqamli imzo.',
  '<strong>Ochiq maʼlumotlar</strong> — qayta ishlatishga ochiq davlat maʼlumoti.',
  '<strong>Xizmat kartasi</strong> — xizmatning bosqich, muddat va natija tavsifi.',
  '<strong>Raqamli tenglik</strong> — internetga kirishi cheklanganlar uchun xizmat mavqieligi.'
 ],
 '<p>Eng keng xato — <strong>eski jarayonni koʼchirish</strong>. Qogʼozda uchta imzo kerak boʼlsa, raqamli xizmatda ham uchta skan soʼraladi: fuqaro uchun ogʼirlik yoʼqolmaydi. Ikkinchisi — <strong>oʼlchamaslik</strong>: nechta ariza onlayn tugadi, oʼrtacha qancha vaqt ketdi — bu koʼrsatkichlar yozib borilmasa, xizmat "bor" boʼlib, foyda yoʼq.</p>',
 ARRAY[
  'G2G ni nega eng qiyin deb hisoblaymiz?',
  'Elektron imzo nimani tasdiqlaydi?',
  'Ochiq maʼlumot qanday formatda berilishi kerak?',
  'Raqamli tenglik muammosini bitta yoʼl bilan yeching.'
 ],
 ARRAY[
  'Masala: bir xizmatni raqamlashtirish boʼyicha taklif tayyorlash.',
  'Xizmat kartasini toʼldiring: bosqich, muddat, javobgar.',
  'Ortiqcha bosqichlarni belgilang — qaysi biri maʼlumotni qayta yigʼadi?',
  'Idoralararo maʼlumot almashinuvini aniqlang (G2G).',
  'Muvaffaqiyat mezonini yozing: tugallangan onlayn ariza ulushi, oʼrtacha vaqt.'
 ],
 NULL, NULL, NULL,
 'United Nations — E-Government Survey', 'https://publicadministration.un.org/egov/'
),

('IAKTT1105','M11',
 '<p>Korxona server xonasi uchun 40 000 dollar sarfladi va uni 60% quvvatda ishlatdi. Boshqa bir korxona bir xil ishni oyiga 600 dollarlik obuna bilan bajardi — va tungi yuklama uchun toʼlamadi. Farq texnologiyada emas, xarajat modelida.</p>',
 '<p>Bulutga oʼtish — kapital xarajatni operatsion xarajatga aylantirish qarori. Moliyaviy jihatdan bu likvidlik, prognozlanuvchanlik va risk taqsimoti masalasi.</p>',
 ARRAY[
  '<h2>Uch xizmat modeli</h2><p><strong>IaaS</strong> — virtual server, saqlash, tarmoq. Boshqaruv sizda: operatsion tizim, ilova, maʼlumot. <strong>PaaS</strong> — platforma: kodni yuklaysiz, muhit va yetkazib berish quvuri tayyor. <strong>SaaS</strong> — tayyor ilova, brauzerdan ishlatiladi, obuna bilan.</p>',
  '<p>Qaysi qatlamni siz boshqarishingiz kerak — xavfsizlik javobgarligini ham shu belgilaydi. Provider yopiq qatlamni, siz esa oʼz qatlamingizni himoya qilasiz.</p>',
  '<p><strong>Joylashtirish turi.</strong> Public — koʼp mijozli umumiy infratuzilma, arzon. Private — bitta tashkilotga xos, nazorat yuqori, qimmat. Hybrid — ikkalasi birga: sezgir maʼlumot ichkarida, elastik yuklama tashqarida.</p>',
  '<p><strong>Xarajat dinamikasi.</strong> Toʼla-toʻkis (pay-as-you-go) modeli oʼsuvchi va mavsumiy yuklama uchun tejamkor; barqaror, toʼla yuklangan ish uchun oʼz serveri arzonroq boʼladi. Shu sababli bulut avtomatik arzonlashmaydi — yuklama profilini bilish kerak.</p>',
  '<p><strong>SLA va vendor lock-in.</strong> SLA — xizmat kafolati va uning javobgarligi. Lock-in — yetkazib beruvchining xususiy format va API lariga bogʼliqlik: chiqish narxi qancha yuqori boʼlsa, muzokara kuchi shuncha past.</p>'
 ],
 ARRAY[
  '<strong>IaaS / PaaS / SaaS</strong> — boshqaruv qatlamlari.',
  '<strong>Public / Private / Hybrid</strong> — joylashtirish modellari.',
  '<strong>SLA</strong> — xizmat darajasi kelishuvi va javobgarlik.',
  '<strong>Vendor lock-in</strong> — yetkazib beruvchiga qimmat bogʼliqlik.',
  '<strong>Elastiklik</strong> — yuklamaga qarab resursni avtomatik oʼzgartirish.',
  '<strong>Masraf (cost) nazorati</strong> — resurs boʼyicha toʼlovni kuzatish va cheklash.'
 ],
 '<p>Bulutda eng koʼp uchraydigan moliyaviy muammo — <strong>nazoratsiz resurs</strong>: sinov uchun yoqilgan server oylab ishlab turadi va hisob-kitob oʼsadi. Ikkinchisi — maʼlumotning <strong>joylashuv talabini</strong> hisoblamaslik: baʼzi sohalarda maʼlumot mamlakat chegarasidan chiqmasligi kerak.</p>',
 ARRAY[
  'PaaS va IaaS oʼrtasida kim koʼproq boshqaradi?',
  'Qachon oʼz serveri bulutdan arzon?',
  'Vendor lock-in ni qanday kamaytirish mumkin?',
  'Elastiklik qanday xarajat tejaydi?'
 ],
 ARRAY[
  'Masala: hisobot tizimini bulutga koʼchirishni asoslash.',
  'Yuklama profilini chizing: kunlik, mavsumiy, choʼqqi.',
  'Maʼlumotning huquqiy talabini tekshiring (joylashuv, maxfiylik).',
  'Ikkita modelni 3 yillik TCO boʼyicha solishtiring.',
  'Chiqish rejasi va SLA shartlarini hujjatga kiriting.'
 ],
 NULL, NULL, NULL,
 'NIST SP 800-145 — bulut tushunchasining rasmiy taʼrifi', 'https://csrc.nist.gov/glossary/term/cloud_computing'
),

('IAKTT1105','M12',
 '<p>Yetkazib beruvchi "malni oʼz zavodimizda ishlab chiqardik" deb yozdi. Tekshirish uchun uchta alohida hujjat kerak boʼldi va zanjirning boshiga yetib boʼlmadi. Blokcheyn aynan shu "kim tasdiqladi?" savoliga javob berish usuli.</p>',
 '<p>Ishonch — savdoning eng qimmat tarkibiy qismi. Vositachi ishonchini texnologik ishonchga almashtirish mumkin boʼlsa, tranzaksiya xarajati kamayadi.</p>',
 ARRAY[
  '<h2>Qanday ishlaydi</h2><p>Maʼlumot <strong>bloklarga</strong> yoziladi; har blok oldingisining <em>hash</em> qiymatini saqlaydi. Bittasini oʼzgartirsangiz, zanjirning davomi buziladi — shu sababli yozuv amalda oʼzgarmas. Nusxa minglab tugunlarda saqlanadi, bitta markaz yoʼq.</p>',
  '<p><strong>Konsensus</strong> — tugunlar qaysi blok toʼgʼriligiga kelishib olishi mexanizmi. Proof of Work hisoblash quvvatini sarflaydi (energiya xarajati yuqori); Proof of Stake aktivni garov qoʼyish orqali ishlaydi (juda kam energiya).</p>',
  '<p><strong>Aqlli kontent</strong> — shart bajarilganda oʼzi ishga tushadigan dastur: "yetkazib berish tasdiqlansa, toʼlov oʼtkazilsin". Bu vositachi va kutish vaqtini kamaytiradi.</p>',
  '<p><strong>Tokensizlash</strong> — real aktivni (energiya sertifikati, kovoz, sertifikat) raqamli yozuvga aylantirish. Boʼlinish va savdo tezligini oshiradi.</p>',
  '<p><strong>Qachon kerak emas?</strong> Maʼlumotni bitta ishonchli tomon yozsa yetarli boʼlsa, oddiy maʼlumotlar bazasi arzonroq va tezroq. Blokcheyn — koʼp ishtirokchi, oʼzaro ishonchsizlik va oʼzgarmas tarix kerak boʼlganda oqlanadi.</p>'
 ],
 ARRAY[
  '<strong>Blok va zanjir</strong> — hash orqali bogʼlangan oʼzgarmas yozuvlar.',
  '<strong>Hash</strong> — istalgan oʼzgarishni aniqlaydigan qisqa iz.',
  '<strong>Konsensus</strong> — tarmoqning kelishib tasdiqlash mexanizmi.',
  '<strong>Aqlli kontent</strong> — shart bajarilganda avtomatik ishlaydigan kod.',
  '<strong>Tokensizlash</strong> — aktivni raqamli boʼlinadigan yozuvga aylantirish.',
  '<strong>Kengayish (scalability)</strong> — sekundiga tranzaksiya va narx cheklovi.'
 ],
 '<p>Tehnikaga boʼlgan ishonch — <strong>"blokcheyn = xavfsiz" degan xato</strong>. Zanjir oʼzgarmas, lekin <em>ichiga notoʼgʼri maʼlumot kiritilsa, u ham oʼzgarmas boʼlib qoladi</em>. Garbage in, gospel out. Ikkinchi xato — har qanday hisobot masalasiga blokcheyn tanlash: yozuvni bitta idora tasdiqlasa yetarli boʼlsa, bu ortiqcha xarajat.</p>',
 ARRAY[
  'Nega hash oʼzgarishni aniqlaydi?',
  'Proof of Stake PoW dan nimasi bilan afzal?',
  'Aqlli kontent qaysi jarayonni qisqartiradi?',
  'Blokcheyn kerak boʼlmagan bitta holat keltiring.'
 ],
 ARRAY[
  'Masala: yetkazib berish zanjirida kelib chiqishni tasdiqlash.',
  'Ishtirokchilarni sanang: yetishtiruvchi, qayta ishlovchi, logistika, sotuvchi.',
  'Kimda markaziy ishonch bor? Boʼlmasa — blokcheyn asoslanadi.',
  'Har bosqichda qaysi maʼlumot va kimning imzosi yozilishini belgilang.',
  'Yozuvga kirishdan oldingi sifat nazoratini alohida tashkil qiling.'
 ],
 NULL, NULL, NULL,
 'Ethereum — rasmiy dasturchilar hujjati', 'https://ethereum.org/en/developers/docs/'
),

('IAKTT1105','M13',
 '<p>Mijoz doʼkonda mahsulotni koʼrdi, narxni onlayn taqqosladi, ilova orqali buyurt qildi va kuryerdan qabul qildi. Savdo kanali — bitta, lekin toʼrt bosqich. Har bosqichda maʼlumot qoladi va shu maʼlumot keyingi sotuvni boshqaradi.</p>',
 '<p>Elektron biznes — savdo modeli emas, maʼlumotga asoslangan operatsiya. Kanallararo uzilish konversiyani pasaytiradi.</p>',
 ARRAY[
  '<h2>Savdo modellari</h2><p><strong>B2C</strong> — korxonadan fuqaroga; <strong>B2B</strong> — korxonadan korxonaga (miqdor, shartnoma, kredit); <strong>C2C</strong> — platforma orqali shaxsdan shaxsga. Har birining narx shakllanishi, decision sikli va xaridor soni boshqa.</p>',
  '<p><strong>Marketpleys</strong> — savgar va xaridorni birlashtirgan platforma. Daromad komissiyadan; afzalligi — tanlov va ishonch mexanizmi (reyting, kafolat), kamchiligi — savgar brendni yoʼqotadi.</p>',
  '<p><strong>Toʼlov infratuzilmasi.</strong> Karta, QR, kassa orqali toʼlov, mobil pul oʼtkazma. Har birining komissiyasi va qaytish (chargeback) riski bor — savdo marjasiga bevosita taʼsir qiladi.</p>',
  '<p><strong>Omnichannel</strong> — onlayn va oflayn kanallarning yagona tajribasi: savat, narx va mijoz tarixi bir xil. Kanal alohida ishlaganda mijoz har birida boshdan suhbatlashadi.</p>',
  '<p><strong>Mobil tijorat</strong> — ilova, push-xabar va joylashuvga asoslangan taklif. Ekranning kichikligi maʼlumotni ixcham berishni talab qiladi; tezlik — konversiyaning birinchi omili.</p>'
 ],
 ARRAY[
  '<strong>B2C / B2B / C2C</strong> — elektron savdo modellari.',
  '<strong>Marketpleys</strong> — ikki tomonli platforma.',
  '<strong>Konversiya</strong> — tashrifchi sotib oluvchiga aylangan ulush.',
  '<strong>Chargeback</strong> — toʼlovning qaytarib olinishi.',
  '<strong>Omnichannel</strong> — kanallarning yagona mijoz tajribasi.',
  '<strong>Savdo quvuri</strong> — mijozning tanlovdan toʼlovga boʼlgan bosqichlari.'
 ],
 '<p>Onlayn savdoda eng koʼp yoʼqotiladigan narsa — <strong>ishonchni baholamaslik</strong>. Qaytarish sharti noaniq, qoʼllab-quvvatlash sekin boʼlsa, trafik sotuvga aylanmaydi: konversiya pasayadi va reklama xarajati oʼzini oqlamaydi. Ikkinchi xato — <strong>har kanalni alohida hisoblash</strong>: mijoz onlayn tanlab, oflayn olganida savdo "yoʼqoladi".</p>',
 ARRAY[
  'B2B va B2C savdo sikli nimaga farq qiladi?',
  'Marketpleys daromad modeli nimadan iborat?',
  'Omnichannel nimani birlashtiradi?',
  'Konversiyani qanday oʼlchaysiz?'
 ],
 ARRAY[
  'Masala: mahalliy ishlab chiqaruvchi uchun kanal strategiyasi.',
  'Maqsadli segment va chekni aniqlang.',
  'Kanallarni roʼyxat qiling: oʼz sayti, marketpleys, ulgurji mijoz.',
  'Har kanal uchun marja, logistika va toʼlov xarajatini yozing.',
  'Bitta KPI tanlang: 90 kunda konversiya va qaytib kelish ulushi.'
 ],
 NULL, NULL, NULL,
 'UNCTAD — E-commerce and Digital Economy statistikasi', 'https://unctad.org/topic/infrastructure/e-commerce-and-digital-economy'
),

('IAKTT1105','M14',
 '<p>Bir korxona xodimi e-pochtdagi "hisobingizni tasdiqlang" havolasini bosdi. Besh daqiqada ichki tizimga kirish huquqi oʼgʼirlandi. Himoya vositalari bor edi — ularning hech biri inson omilini yopmadi.</p>',
 '<p>Axborot xavfsizligi — texnik xarajat emas, riskni boshqarish. Har bir chora qaysi xavfni qancha kamaytirishini tushunish iqtisodiy bahodir.</p>',
 ARRAY[
  '<h2>Uch ustun: CIA</h2><p><strong>Konfidensiallik</strong> — maʼlumot ruxsatsiz ochilmasligi: shifrlash va kirish nazorati. <strong>Yaxlitlik</strong> — oʼzgartirilmaganligi: hash va raqamli imzo. <strong>Mavqielik</strong> — kerak paytda yetarli boʼlishi: zaxira, muqovavat, tiklash rejasi. Uchala ustunning buzilishi ham zarar keltiradi; koʼpchilik faqat birinchisini oʼylaydi.</p>',
  '<p><strong>Tahdid manbalari.</strong> Zararli dastur, fishing (alrab soʼraladigan maʼlumot), DDoS (xizmatni toʼxtatish), ichki buzilish va eski, yangilanmagan dasturlar. Statistik jihatdan ichki va odomiy xatolar tashqi hujumlardan koʼp.</p>',
  '<p><strong>Kirish boshqaruvi.</strong> Autentifikatsiya — "siz kimsiz?"; avtorizatsiya — "sizga nima ruxsat etilgan?". Ikki bosqichli tekshiruv parol oʼgʼirlansa ham kirishni qiyinlashtiradi. Eng kam huquq prinsipi — har foydalanuvchi ishi uchun kerakli minimumni oladi.</p>',
  '<p><strong>Shifrlash.</strong> Simmetrik (AES) — bitta kalit, tez, katta hajm uchun. Assimetrik (RSA/ECC) — ochiq va yopiq kalit jufti, kalit uzatish va imzo uchun. Amaliyotda ikkalasi birga ishlatiladi.</p>',
  '<p><strong>Boshqaruv choralari.</strong> Zaxira nusxa va tiklashni muntazam sinash, xodimlarni oʼqitish, hodisalarga javob rejasi. Texnologiya bir marta sotib olinadi; xavfsizlik — har kuni bajariladigan jarayon.</p>'
 ],
 ARRAY[
  '<strong>Konfidensiallik / yaxlitlik / mavqielik</strong> — xavfsizlikning uch ustuni.',
  '<strong>Autentifikatsiya</strong> — shaxsni tasdiqlash.',
  '<strong>Avtorizatsiya</strong> — ruxsat etilgan amallarni belgilash.',
  '<strong>Fishing</strong> — ishonchga kirib maʼlumot soʼrash.',
  '<strong>Eng kam huquq</strong> — faqat zarur minimum ruxsat.',
  '<strong>Hodisaga javob</strong> — uzilishni aniqlash, toʼxtatish va tiklash rejasi.'
 ],
 '<p>Eng koʼp uchraydigan holat — <strong>zaxira nusxa bor, tiklash sinovdan oʼtmagan</strong>. Buzilgan yoki juda sekin tiklanadigan nusxa foyda bermaydi. Ikkinchisi — parolni almashtirishni majburiy qilib, ikki bosqichli tekshiruvni yoʼlga qoʼymaslik: almashtirilgan parol ham oʼgʼirlanadi.</p>',
 ARRAY[
  'Uch ustunning qaysi biri DDoS hujumida buziladi?',
  'Autentifikatsiya va avtorizatsiya farqi?',
  'Nega simmetrik + asimmetrik birga ishlatiladi?',
  'Xodimni oʼqitish qaysi xavfni kamaytiradi?'
 ],
 ARRAY[
  'Masala: kichik korxona uchun xavfsizlik xarajatini asoslash.',
  'Eng qimmat maʼlumot va uni yoʼqotish narxini baholang.',
  'Uch ustun boʼyicha mavjud holatni tekshiring.',
  'Xavf ehtimoli × zarar miqdori bilan roʼyxatni tartiblang.',
  'Birinchi uch chorani tanlang va tiklash sinovini kalendariga qoʼying.'
 ],
 NULL, NULL, NULL,
 'OWASP Top 10 — veb ilovalar xavfsizligi', 'https://owasp.org/www-project-top-ten/'
),

('IAKTT1105','M15',
 '<p>Soliq hisoboti, bojxona deklaratsiyasi va bank toʼlovi — uch xil idora, uch format, bir xil maʼlumot. Korxona maʼlumotni bir marta kiritib, uchala tizimga uzata olmasa, xato va vaqt ikki barobar.</p>',
 '<p>Soha tizimlari orasidagi almashinuv — operatsion xarajatning koʼrinmas qismi. Standart format shu xarajatni kamaytiradi.</p>',
 ARRAY[
  '<h2>Kasbiy axborot komplekslari</h2><p>Bunlar sohaning qoida va hujjat shakllarini ichida saqlovchi tizimlardir: <strong>bank core</strong> hisob va toʼlov, <strong>insurtech</strong> polis va zarar, <strong>soliq va bojxona</strong> hisobot, <strong>logistika</strong> yuk kuzatuvi. Ular bir-biri bilan API yoki hujjat almashinuvi orqali bogʼlanadi.</p>',
  '<p><strong>Integratsiya usullari.</strong> Toʼgʼridan-toʼgʻri API — tez va real vaqtda, lekin har juftlik alohida ishlaydi. Oʼrtacha qatlam (middleware / ESB) — bitta markaz orqali koʼp tizimni bogʼlaydi, boshqaruvni markazlashtiradi. Fayl almashinuvi — kechikkan, lekin qonuniy hujjat aylanishida hali ishlatiladi.</p>',
  '<p><strong>Formatlar.</strong> XML — struktura va sxema bilan, rasmiy hujjatlarda keng. JSON — yengil, zamonaviy API larda standart. EDI — savdo hujjatlarining (shartnoma, fatura) asrlar davomida kelishilgan formati. Format tanlovi — hamkorlar kim va nima qoʼllashiga bogʼliq.</p>',
  '<p><strong>Boshqaruv paneli (dashboard).</strong> Rahbar uchun koʼrsatkichlarni bir ekranda jamlaydi. Foydasi — maʼlumotni soʼramaslik; zarari — notoʼgʼri koʼrsatkichni "rasmiy haqiqat" qilib qabul qilish.</p>',
  '<p><strong>Mutaxassis talabi.</strong> Iqtisodchidan kod yozish emas, <em>texnik topshiriq yozish va natijani baholash</em> kutiladi: qaysi maydon, qaysi tizimdan, qanday tekshiriladi.</p>'
 ],
 ARRAY[
  '<strong>Kasbiy axborot kompleksi</strong> — soha qoidalarini saqlovchi tizim.',
  '<strong>API</strong> — tizimlararo dasturiy interfeys.',
  '<strong>Middleware / ESB</strong> — integratsiyani markazlashtiruvchi qatlam.',
  '<strong>EDI</strong> — elektron savdo hujjatlari standarti.',
  '<strong>Maʼlumotlar almashinuv sxemasi</strong> — maydonlar va format kelishuvi.',
  '<strong>Boshqaruv paneli</strong> — koʼrsatkichlarni jamlagan interfeys.'
 ],
 '<p>Integratsiyada eng qimmat qism — <strong>maʼlumot mazmunini kelishish</strong>. Ikkala tizimda "mijoz kodi" boshqa narsani anglatishi mumkin; texnik bogʼlanish oson, semantik kelishuv qiyin. Ikkinchi xato — <strong>boshqaruv panelini tekshirmaslik</strong>: manba tizimdagi xato panel "aniq haqiqat" boʼlib koʼrinadi.</p>',
 ARRAY[
  'API va fayl almashinuvini qachon tanlaysiz?',
  'EDI nimaga hali ishlatiladi?',
  'Maʼlumot egasi kelishuvi nimani oldini oladi?',
  'Koʼrsatkich ishonchliligini qanday tekshirasiz?'
 ],
 ARRAY[
  'Masala: korxona hisobotini soliq tizimiga uzatish.',
  'Talab qilinadigan format va sxemani toping.',
  'Ichki tizimdagi maydonlarni sxemaga moslang (mapping).',
  'Kiritishda tekshiruv qoidalari belgilang.',
  'Qaytish xabarini (kvitansiyani) kuzatish va saqlash tartibini qoʼying.'
 ],
 NULL, NULL, NULL,
 'IBM — Electronic Data Interchange (EDI)', 'https://www.ibm.com/think/topics/edi'
);

-- --------------------------------------------------------------------------
-- IAKTT1105 · seminar mashgʼulotlari (S1–S15)
-- --------------------------------------------------------------------------
INSERT INTO seed_content
(subject, lesson, hook, why, explain, concepts, mistake, checkin, worked, code, lang, codewalk, link_label, link_url) VALUES

('IAKTT1105','S1',
 '<p>Seminar boshlanadi: "Sizning kasbingizda qaysi qaror axborot yetishmasligi kechikkan?" Bir javob — butun kursning mazmunini ochadi.</p>',
 '<p>Seminar — maʼruzani eslash emas, uni oʼz sohangizga tatbiq etish joyi. Baholash dalilga asoslanadi.</p>',
 ARRAY[
  '<p>Tartib: mustaqil tayyorgarlik (maʼruza matni va berilgan manba), ogʼzavih muhokama, yozma ish. Har talaba bitta pozitsiyani <em>manba bilan</em> himoya qiladi.</p>',
  '<p>Muhokama savollari aniq darajaga qaratiladi: tushunchani farqlash → qoʼllash → baholash. Bllom taksonomiyasining yuqori pogʼonalarida javob bir xil boʼlmaydi — baholash mezonlari shu sababli ochiq eʼlon qilinadi.</p>',
  '<p>Yakuniy mahsulot — qisqa tezis: bir jarayon, undagi axborot tanqisligi, taklif va kutilayotgan natija. Bu keyingi modullarda ishlatiladi.</p>'
 ],
 ARRAY[
  '<strong>Tezis</strong> — qisqa, asoslangan pozitsiya.',
  '<strong>Dalil</strong> — tezisni qoʼllab-quvvatlovchi manba yoki hisob-kitob.',
  '<strong>Baholash mezoni</strong> — javob qaysi mezon boʼyicha baholanishi.',
  '<strong>Korxona holati</strong> — real vaziyat tavsifi (case).'
 ],
 '<p>Seminarда <strong>fikrni dalilsiz</strong> aytish — eng keng baho pasayishi. "Meningcha, shunday" javob emas: manba, sana va raqam kerak.</p>',
 ARRAY[
  'Oʼz sohangizdan bitta qarorni axborot tanqisligi bilan bogʼlang.',
  'Qaysi maʼlumot boʼlsa, qaror tezroq boʼlardi?',
  'Taklifingiz natijasini qanday oʼlchaysiz?'
 ],
 ARRAY[
  'Maʼruza matnini oʼqib, uch savol yozing.',
  'Har savolga bitta manbadan javob toping.',
  'Seminarда bitta pozitsiyani himoya qiling.',
  'Yakunda tezisni bir sahifada topshiring.'
 ],
 NULL, NULL, NULL,
 'MIT OpenCourseWare — ochiq dars rejalar namunalari', 'https://ocw.mit.edu/courses/'
),

('IAKTT1105','S2',
 '<p>Skaner, shtrix-kod, RFID, sensor — barchasi "maʼlumot kiritish". Savdo zalida qaysi biri qancha vaqt tejashini hisoblab koʼring.</p>',
 '<p>Kiritish qurilmasini tanlash — xatolik darajasi va operatsion xarajatni tanlashdir.</p>',
 ARRAY[
  '<p><strong>Kiritish.</strong> Qoʼlda kiritish — sekin va xatoli; avtomatik identifikatsiya (shtrix-kod, RFID, OCR) — tez va takrorlanuvchi. Qoʼlda kiritish xatoligi 1% boʼlsa, 100 000 buyurtmada 1 000 xato demakdir.</p>',
  '<p><strong>Chiqarish.</strong> Monitor, printer, plotter, akustik signal. Chiqarish shakli qarorni tezlashtiradi: omborda printerdagi belgi emas, ekrandagi signal.</p>',
  '<p><strong>Saqlash.</strong> Magnit (HDD), optik (CD/DVD), qattiq holatli (SSD), bulut. Tanlov mezonlari: sigʼim, tezlik, xavfsizlik, narx/GB va saqlash muddati.</p>',
  '<p>Har bir qurilma uchun <em>egallash jami qiymati</em> hisoblanadi: sotib olish + xizmat + xato narxi.</p>'
 ],
 ARRAY[
  '<strong>Avtomatik identifikatsiya</strong> — maʼlumotni qayta kiritmasdan olish.',
  '<strong>OCR</strong> — matnni tasvirdan tanib olish.',
  '<strong>RFID</strong> — masofadan oʼqiladigan yorliq.',
  '<strong>Narx/GB</strong> — saqlash iqtisodiyotining asosiy koʼrsatkichi.'
 ],
 '<p>Faqat <strong>qurilma narxiga</strong> qarab tanlash: arzon skaner ishlamay qolganda qator toʼxtaydi va zarar qurilma narxidan koʼp.</p>',
 ARRAY[
  'Shtrix-kod va RFID farqini bir misolda tushuntiring.',
  'SSD ni qachon HDD ga afzal koʼramiz?',
  'Kiritish xatoligini qanday kamaytirish mumkin?'
 ],
 ARRAY[
  'Korxona jarayonini tanlang.',
  'Har bosqichda qaysi qurilma maʼlumot oladi — yozing.',
  'Xato ulushi va tuzatish vaqtini baholang.',
  'Avtomatlashtirishning qaytish muddatini hisoblang.'
 ],
 NULL, NULL, NULL,
 'Computer History Museum — saqlash tizimlari tarixi', 'https://computerhistory.org/topics/storage/'
),

('IAKTT1105','S3',
 '<p>Bir xil ilova Windows da ishlaydi, Linux serverda — yoʼq. Sabab ilovada emas, operatsion tizimning fayl tizimi va huquq modelida.</p>',
 '<p>Korxona platformasini tanlash — litsenziya xarajati, xavfsizlik modeli va kadr malakasi yigʼindisi.</p>',
 ARRAY[
  '<p><strong>Yadro turlari.</strong> Monolitik yadro (Linux, Windows) barcha xizmatlari bitta fazoda ishlaydi — tez, lekin bir drayver xatosi butun tizimni yiqitadi. Mikroyadro xizmatlarni ajratadi — barqarorroq, murakkabroq.</p>',
  '<p><strong>Fayl tizimi.</strong> NTFS — Windows, ext4 — Linux, APFS — macOS. Registrni hisobga olish, ruxsatlar va jurnal (xatodan tiklanish) xususiyatlari har xil.</p>',
  '<p><strong>Litsenziya.</strong> Mualliflik (proprietary), bepul (freeware), ochiq kodli (FOSS). Ochiq kodda manbani koʼrish va oʼzgartirish mumkin — bu xavfsizlik auditi va mustaqillik uchun muhim.</p>',
  '<p><strong>Xavfsizlik modeli.</strong> Foydalanuvchi va guruh huquqlari, SELinux/AppArmor siyosatlari, paket manbalarining imzosi.</p>'
 ],
 ARRAY[
  '<strong>Yadro</strong> — operatsion tizimning markazi.',
  '<strong>Drayver</strong> — qurilma bilan gaplashuvchi dastur.',
  '<strong>Fayl tizimi</strong> — maʼlumotni saqlash va nomlash tartibi.',
  '<strong>FOSS</strong> — bepul va ochiq manbali dastur.',
  '<strong>Paket menejeri</strong> — dasturlarni oʼrnatish va yangilash vositasi.'
 ],
 '<p>Ochiq kodli tizimni <strong>"bepul" deb hisoblash</strong>: qoʼllab-quvvatlash va mutaxassis xarajati litsenziyani oʼrnini bosadi.</p>',
 ARRAY[
  'Nega Linux serverlarda koʼp tarqalgan?',
  'NTFS va ext4 farqi nimada?',
  'FOSS ishlatishning bitta riski?'
 ],
 ARRAY[
  'Korxona uchun uchta platformani solishtiring.',
  'Har biri uchun litsenziya va qoʼllab-quvvatlash narxini yozing.',
  'Ishlatiladigan ilovalar mosligini tekshiring.',
  'Xavfsizlik siyosati va yangilanish tartibini qiyoslang.'
 ],
 NULL, NULL, NULL,
 'Linux Foundation — ochiq manbali dasturlar qoʼllanmasi', 'https://www.linuxfoundation.org/resources/open-source-guides'
),

('IAKTT1105','S4',
 '<p>Excel da 40 000 qator bor. Filtrlab, guruhlab, xulosa chiqarish — buni har safar qoʼlda qilish oʼrniga bir mavaq pivot qurish mumkin.</p>',
 '<p>Maʼlumotni tez umumlashtirish qobiliyati — tahlilchining eng koʼp ishlatadigan vositasi.</p>',
 ARRAY[
  '<p><strong>Tayyorgarlik.</strong> Jadval "toʼgʼri" shaklda boʼlsa, keyingi qadamlar oson: bir qatorda bir maydon, boʼsh qator va birlashtirilgan katak yoʼq, sana — haqiqiy sana.</p>',
  '<p><strong>Shart boʼyicha hisoblash.</strong> SUMIFS/COUNTIFS/AVERAGEIFS — bir nechta mezon bilan yigʼish va sanash. Bu funksiyalar oddiy filtr oʼrniga aniq javob beradi.</p>',
  '<p><strong>Pivot.</strong> Satr/ustun/qiymat maydonlarini suring; guruhlash (oy, kvartal, toifa) bir necha soniyada tayyor. "Pivot chart" — uning vizual koʼrinishi.</p>',
  '<p><strong>Qayta hisoblash xatolari.</strong> Manba oʼzgarsa pivot yangilanishi kerak; formulalar siljigan katakka ishora qilishi mumkin.</p>'
 ],
 ARRAY[
  '<strong>Toza jadval</strong> — bitta qatorda bitta yozuv, bitta sarlavha.',
  '<strong>Shartli agregat</strong> — SUMIFS, COUNTIFS, AVERAGEIFS.',
  '<strong>Pivot jadval</strong> — guruhlangan umumlashtirish.',
  '<strong>Slicer</strong> — pivot uchun vizual filtr tugmalari.'
 ],
 '<p><strong>Birlashtirilgan katak va boʼsh qatorlar</strong> — pivot va formulalarni jim buzadi. Maʼlumotni tahlildan oldin tozalash — intizom, vaqt isrofi emas.</p>',
 ARRAY[
  'Nega har ustunda bitta tur boʼlishi kerak?',
  'SUMIFS SUMIF dan nimasi bilan kuchli?',
  'Pivot qachon formuladan afzal?'
 ],
 ARRAY[
  'Savdo jurnalini oching va sarlavhalarni tekshiring.',
  'Takroriy va boʼsh qatorlarni olib tashlang.',
  'Toifa boʼyicha oylik yigʼindini SUMIFS bilan chiqaring.',
  'Pivot jadvalda tekshiring — ikkala natija teng boʼlishi kerak.'
 ],
 NULL, NULL, NULL,
 'Microsoft Learn — Excel funksiyalari', 'https://learn.microsoft.com/en-us/office/client-developer/excel/excel-functions'
),

('IAKTT1105','S5',
 '<p>Slaydlar yaxshi, lekin vaqt tugadi va xulosa aytilmadi. Taqdimot — kontent emas, yetkazish.</p>',
 '<p>Moliyaviy natijani himoya qila olmasa, tahlilning qiymati boʼlmaydi.</p>',
 ARRAY[
  '<p><strong>Konstruktsiya.</strong> Muammo → metod → natija → xulosa → taklif. Taklif boʼlmagan taqdimot axborot beradi, qaror qildirilmaydi.</p>',
  '<p><strong>Slayd zichligi.</strong> Bir slaydda bitta gʼoya va uni koʼrsatuvchi bitta vizual. Matn — eslatgich, nutq — asosiy kanal.</p>',
  '<p><strong>Boshqaruv.</strong> Oʼtishlar, vaqt taqsimoti, savol-javob uchun ajratilgan daqiqa. Zaxira slaydlarda tafsilot — asosiy oqimni toza saqlaydi.</p>'
 ],
 ARRAY[
  '<strong>Harvard strukturasi</strong> — xulosani boshda aytish.',
  '<strong>Zaxira slayd</strong> — savollar uchun tayyor dalil.',
  '<strong>Rehearsal</strong> — vaqt bilan sinab koʼrish.'
 ],
 '<p><strong>Slayd matnini oʼqib berish</strong> — auditoriya oʼqib boʼlgan, siz ortda qolasiz.</p>',
 ARRAY[
  'Xulosa birinchi slayddami?',
  'Qaysi slaydni olib tashlash mumkin?',
  'Taklif aniqmi va kimga qaratilgan?'
 ],
 ARRAY[
  'Bitta xulosani bir gapda yozing.',
  'Unga kerakli toʼrt dalilni tanlang.',
  'Har dalilga bitta slayd va bitta vizual.',
  '8 daqiqada sinab, ortiqcha slaydni zaxiraga oʼtkazing.'
 ],
 NULL, NULL, NULL,
 'Storytelling with Data — vizual bahs maqolalari', 'https://www.storytellingwithdata.com/blog'
),

('IAKTT1105','S6',
 '<p>Bank filiali bilan markaz oʼrtasidagi kanal uzildi. Kassa ishlashi davom etadimi? Arxitektura javob berishi kerak boʼlgan savol.</p>',
 '<p>Tarmoq tanlovi — uzilish narxini sugʼurtalashga oʼxshaydi: zaxira qancha turadi, uzilish qancha turadi?</p>',
 ARRAY[
  '<p>Soha boʼyicha tarmoq talabi turlicha: bankda kechikish va yaxlitlik, savdoda oʼtkazuvchanlik, ishlab chiqarishda real vaqt.</p>',
  '<p>Markazlashtirilgan arxitektura boshqaruvni osonlashtiradi, lekin yagona uzilish nuqtasi yaratadi. Tarqatilgan arxitektura bardoshli, ammo qimmatroq.</p>',
  '<p>SD-WAN va kanallararo avtomatik almashinuv (failover) — uzilishda ishni davom ettirishning zamonaviy yoʼllari.</p>'
 ],
 ARRAY[
  '<strong>Failover</strong> — uzilganda zaxira kanalga oʼtish.',
  '<strong>Yagona uzilish nuqtasi</strong> — butun tizimni toʼxtatadigan bitta element.',
  '<strong>QoS</strong> — muhim trafikka ustuvorlik berish.'
 ],
 '<p>Zaxira kanalni <strong>hech qachon sinamaslik</strong>: kerak boʼlganda u ishlamaydi.</p>',
 ARRAY[
  'Sizning sohangizda uzilish bir soati qancha?',
  'Qaysi trafik birinchi oʼtishi kerak?',
  'Failover qanday tekshiriladi?'
 ],
 ARRAY[
  'Jarayon roʼyxatini tuzing va uzilishga chidamliligini baholang.',
  'Har jarayonga uzilishga yoʼl qoʼyiladigan vaqtni belgilang.',
  'Kanal va zaxira variantlarini TCO boʼyicha solishtiring.',
  'Yiliga ikki marta almashinuv sinovini rejalashtiring.'
 ],
 NULL, NULL, NULL,
 'Cloudflare Learning Center — tarmoq qatlami', 'https://www.cloudflare.com/learning/network-layer/'
),

('IAKTT1105','S7',
 '<p>Har kuni 2 million yozuv keladi. Buni Excel da ochib boʼlmaydi. Katta maʼlumot — hajm muammosi emas, <em>shakl</em> muammosi.</p>',
 '<p>Tahlil savolini aniq qoʼymasangiz, eng qimmat infratuzilma ham javob bermaydi.</p>',
 ARRAY[
  '<p><strong>5V.</strong> Volume (hajm), Velocity (tezlik), Variety (xilma-xillik), Veracity (sifat), Value (qiymat). Oxirgi ikkitasi — iqtisodiy mezon.</p>',
  '<p><strong>Tuzilgan / tuzilmagan.</strong> Baza jadvallari bilan qatorlar, matn, rasm va loglar bir xil ishlanmaydi. Ikkinchisi uchun sxema keyin qoʼyiladi (schema-on-read).</p>',
  '<p><strong>Stek.</strong> HDFS — tarqatilgan saqlash; Spark — xotirada hisoblash; maʼlumot koʼli (data lake) — xom shaklda saqlash; data warehouse — tayyorlangan jadval.</p>'
 ],
 ARRAY[
  '<strong>Data lake</strong> — xom maʼlumot ombori.',
  '<strong>Data warehouse</strong> — tahlil uchun tayyorlangan baza.',
  '<strong>ETL / ELT</strong> — olish, tozalash, yuklash quvuri.',
  '<strong>Schema-on-read</strong> — sxemani oʼqishda qoʼllash.'
 ],
 '<p><strong>Hajm bilan faxrlanib, savolni unutish</strong> — petabayt javobsiz yotadi.</p>',
 ARRAY[
  'Qaysi V sizning sohangizda eng katta toʼsiq?',
  'Lake va warehouse farqi?',
  'Maʼlumot sifatini qanday tekshirasiz?'
 ],
 ARRAY[
  'Bitta aniq biznes savol yozing.',
  'Unga kerakli maʼlumot manbalarini sanang.',
  'Hajm, tezlik va shaklni baholang.',
  'Eng arzon yechimni tanlang: baʼzan SQL yetarli.'
 ],
 NULL, NULL, NULL,
 'Apache Spark — rasmiy hujjatlar', 'https://spark.apache.org/docs/latest/'
),

('IAKTT1105','S8',
 '<p>Jarayonni tushuntirishning eng tez yoʼli — uni chizish. Lekin notoʼgʼri belgi tushunmovchilikni ikki barobar qiladi.</p>',
 '<p>Blok-sxema — jamoaviy kelishuv vositasi: xato qayerda ekanligini hamma bir joyda koʼradi.</p>',
 ARRAY[
  '<p>Standart belgilar: oval — boshlash/tugash, parallelogramm — kiritish/chiqish, toʼrtburchak — amal, romb — shart, yoʼnaltirilgan chiziq — oqim.</p>',
  '<p>Shart rombidan kamida ikki chiqish boʼladi va ularning har biri belgilanadi (ha/yoʼq). Chiqishlar birlashsa — keyingi amalga bitta oqim ketadi.</p>',
  '<p>Tekshiruv: har yoʼldan boshlab tugashga yetib boradimi? Yetib bormasa — cheksiz sikl yoki "oʼlik" blok bor.</p>'
 ],
 ARRAY[
  '<strong>Oqim</strong> — belgilar orasidagi yoʼnalish.',
  '<strong>Shart rombi</strong> — tanlash nuqtasi.',
  '<strong>Oʼlik blok</strong> — hech qaysi yoʼldan yetib boʼlmaydigan qadam.',
  '<strong>Andoza</strong> — takrorlanuvchi qism.'
 ],
 '<p><strong>Chizgʼilarni kesishtrirish</strong> va belgi maʼnosini oʼzgartirib ishlatish — oʼqishni buzadi.</p>',
 ARRAY[
  'Rombdan nechta chiqish boʼlishi kerak?',
  'Qanday yoʼl "oʼlik blok"ni koʼrsatadi?',
  'Sizning jarayoningizda sikl bormi?'
 ],
 ARRAY[
  'Kundalik jarayonni tanlang.',
  'Boshlash va tugash nuqtalarini belgilang.',
  'Har qaror nuqtasini romb bilan chizing.',
  'Bir misol qiymat bilan yoʼlni yurib koʼring.'
 ],
 NULL, NULL, NULL,
 'Lucidchart — blok-sxema belgilari qoʼllanmasi', 'https://www.lucidchart.com/pages/flowchart-symbols'
),

('IAKTT1105','S9',
 '<p>Baza bor, ilova bor, hisobot bor — lekin ularning qaysi biri "mijoz" soʼziga javobgar? Javob yoʼq boʼlsa, ziddiyat ham yoʼqolmaydi.</p>',
 '<p>Tizimni qismlarga ajratib tahlil qilish — integratsiya loyihasining birinchi bosqichi.</p>',
 ARRAY[
  '<p>Har bir komponent uchun: kirish manbai, qayta ishlash qoidasi, saqlash joyi, chiqish oluvchisi va qayta aloqa mexanizmi.</p>',
  '<p>Komponentlar orasidagi bogʼlanish nuqtalari (interfeys) — eng nozik joy: oʼzgarish bir tizimga tegishi ikkinchisini buzadi.</p>',
  '<p>Foydalanuvchi bilan suhbat texnik hujjatdan koʼproq haqiqatni ochadi: jarayon rasmiy emas, amaliy oʼrganiladi.</p>'
 ],
 ARRAY[
  '<strong>Interfeys</strong> — komponentlar orasidagi kelishilgan chegarа.',
  '<strong>Bogʼliqlik</strong> — bir oʼzgarishning ikkinchisiga taʼsiri.',
  '<strong>As-is / to-be</strong> — hozirgi va maqsadli holat tavsifi.'
 ],
 '<p>Faqat rasmiy yoʼriqnoma boʼyicha yurish — faktik jarayon boshqacha.</p>',
 ARRAY[
  'Sizning tizimingizda qaysi qism zaif?',
  'Qayta aloqa kimdan keladi?',
  'As-is xaritasi bormi?'
 ],
 ARRAY[
  'Bitta tizimni tanlang.',
  'Besh komponent boʼyicha toʼldiring.',
  'Bogʼlanish nuqtalarini belgilang.',
  'Ikki foydalanuvchi bilan suhbat qiling va farqni yozing.'
 ],
 NULL, NULL, NULL,
 'Forrester — arxitektura boʼyicha ochiq maqolalar', 'https://www.forrester.com/blogs/'
),

('IAKTT1105','S10',
 '<p>Ariza onlayn qabul qilindi — keyin nima? Fuqaro bilmaydi va ofisga boradi. Raqamli xizmatning qiymati — shaffoflikda.</p>',
 '<p>Xizmat kartasi bor- yoʼqligi emas, u qancha bosqichni yoʼqol qilgani bilan oʼlchanadi.</p>',
 ARRAY[
  '<p>Xizmat kartasi: kirish hujjatlari, bosqichlar, muddat, natija, javobgar idora, toʼlov. Har bosqich uchun "maʼlumot qayerdan olindi?" savoli javobsiz qolsa — raqamlashtirish tugamagan.</p>',
  '<p>Holotni kuzatish (tracking) fuqaro navbatda turish vaqtini yoʼqol qiladi — bu eng arzon yaxshilanish.</p>',
  '<p>E-imzo va bir martalik kod identifikatsiyani kuchaytiradi; xavfsizlik va qulaylik muvozanati saqlanishi kerak.</p>'
 ],
 ARRAY[
  '<strong>Xizmat kartasi</strong> — rasmiy jarayon tavsifi.',
  '<strong>Holatni kuzatish</strong> — ariza yoʼlini koʼrsatish.',
  '<strong>E-imzo</strong> — raqamli tasdiq.',
  '<strong>Servis dizayni</strong> — xizmatni foydalanuvchi tajribasidan boshlab loyihalash.'
 ],
 '<p><strong>Qogʼoz protsessini koʼchirish</strong> — raqamli xizmat ham ogʼir boʼlib qoladi.</p>',
 ARRAY[
  'Qaysi bosqichni butunlay olib tashlash mumkin?',
  'Qaysi maʼlumotni idora oʼzi olishi mumkin?',
  'Natijani qanday oʼlchaysiz?'
 ],
 ARRAY[
  'Bir xizmatni tanlang va kartasini chizing.',
  'Har bosqichni "keraklimi?" deb soʼrang.',
  'Idoralararo maʼlumot almashinuvini belgilang.',
  'Holat kuzatuvi va bildirishnomani qoʼshing.'
 ],
 NULL, NULL, NULL,
 'eGov — xizmat kartalari portali', 'https://egov.kz/cms/uz'
),

('IAKTT1105','S11',
 '<p>Korxona bulutga oʼtdi va hisob ikki barobar oshdi. Sabab — oʼchirilmagan sinov serverlari.</p>',
 '<p>Bulut iqtisodiyoti: resursni oʼlchamasangiz, toʼlov nazoratsiz boʼladi.</p>',
 ARRAY[
  '<p>Migratsiya strategiyalari: rehost (koʼchirish), refactor (qayta yozish), retire (yopish). Hammasini koʼchirish — eng qimmat yoʼl.</p>',
  '<p>Tagging (belgilash) va byudjet ogohlantirishlari xarajatni koʼrinarli qiladi; nazoratsiz resurs shu yerda aniqlanadi.</p>',
  '<p>Maʼlumot joylashuvi va chiqish narxi (egress) shartnomada aniq yozilishi kerak.</p>'
 ],
 ARRAY[
  '<strong>Rehost / refactor / retire</strong> — migratsiya variantlari.',
  '<strong>Tagging</strong> — resursga javobgar va loyiha belgisi berish.',
  '<strong>Egress</strong> — bulutdan tashqariga maʼlumot chiqarish narxi.',
  '<strong>FinOps</strong> — bulut xarajatini boshqarish amaliyoti.'
 ],
 '<p><strong>Oʼlchamaslik</strong> — hisob-faktura kelguncha hech kim nima ketayotganini bilmaydi.</p>',
 ARRAY[
  'Qaysi resurs sizga keraksiz?',
  'Chiqish narxini qanday kamaytirish mumkin?',
  'Kim javobgar va qanday belgilanadi?'
 ],
 ARRAY[
  'Resurs roʼyxatini oling.',
  'Har biriga javobgar va loyiha belgisini qoʼying.',
  'Oylik prognoz va haqiqiy xarajatni solishtiring.',
  'Ishlatilmayotgan resursni oʼchirish tartibini kiriting.'
 ],
 NULL, NULL, NULL,
 'AWS — bulutga migratsiya qoʼllanmasi', 'https://aws.amazon.com/migration-support/'
),

('IAKTT1105','S12',
 '<p>Olma sharbati "tashkildan kelgan" deb yozilgan. Buni qanday tekshirasiz? Blokcheyn javob bermaydi — u javobni yozib qoʼyadi.</p>',
 '<p>Texnologiya qaysi ishonch muammosini yechishini aniq aytolish — tanlovning boshlanishi.</p>',
 ARRAY[
  '<p>Holatlarni baholash mezonlari: ishtirokchilar soni, oʼzaro ishonch darajasi, markaziy tasdiqlovchi bormi, oʼzgarmas tarix kerakmi.</p>',
  '<p>Zanjir — yozuv qatlami. Sensor yoki odam notoʼgʼri yozsa, blokcheyn uni "haqiqat" qilib muhrlaydi. Kirish nazorati alohida tashkil etiladi.</p>',
  '<p>Aqlli kontent shartni avtomatlashtiradi, lekin uning kodi xatosi ham tuzatilmaydi — audit talab qilinadi.</p>'
 ],
 ARRAY[
  '<strong>Ishtirokchi modeli</strong> — kim yozadi, kim koʼradi.',
  '<strong>Oracle muammosi</strong> — zanjirga kirishdagi ishonch masalasi.',
  '<strong>Audit</strong> — kontent kodini tekshirish.'
 ],
 '<p>Har hisobot masalasiga blokcheyn tanlash — xarajat, foyda yoʼq.</p>',
 ARRAY[
  'Sizning holatingizda markaziy ishonch bormi?',
  'Oʼzgarmas tarix kerakmi?',
  'Kirish maʼlumoti kimning javobgarligida?'
 ],
 ARRAY[
  'Zanjir holatini tanlang.',
  'Ishtirokchilar va ularning manfaatini yozing.',
  'Uch mezon boʼyicha baholang.',
  'Boshqa yechim (reestr, audit) bilan solishtiring.'
 ],
 NULL, NULL, NULL,
 'IBM — supply chain boʼyicha blokcheyn holatlari', 'https://www.ibm.com/think/topics/supply-chain-blockchain'
),

('IAKTT1105','S13',
 '<p>Mijoz ilovada tanladi, saytda toʼladi, doʼkonda oldi. Qaysi kanal sotdi? Bitta javob berilsa — byudjet notoʼgʼri taqsimlanadi.</p>',
 '<p>Kanal analitikasi marketing xarajatining qayerga ketishini belgilaydi.</p>',
 ARRAY[
  '<p>Asosiy koʼrsatkichlar: trafik, konversiya, oʼrtacha chek, qaytib kelish ulushi, mijozni jalb qilish narxi (CAC) va umrlik qiymat (LTV).</p>',
  '<p>Attribution — sotuvni kimga yozish: birinchi teginish, oxirgi, yoki koʼp bosqichli model. Tanlov reklama byudjetini oʼzgartiradi.</p>',
  '<p>Bank, sugʼurta va savdoda raqamli kanal xarajatni kamaytiradi, lekin maslahat va ishonch funksiyasi saqlanadi.</p>'
 ],
 ARRAY[
  '<strong>Konversiya</strong> — tashrifchi mijozga aylangan ulush.',
  '<strong>CAC / LTV</strong> — jalb qilish narxi va umrlik qiymat.',
  '<strong>Attribution</strong> — sotuvni kanalga yozish modeli.',
  '<strong>Retention</strong> — qaytib kelish ulushi.'
 ],
 '<p><strong>Faqat oxirgi klikni</strong> hisobga olish — birinchi tanituvchi kanal "nol natijali" koʼrinadi va yopiladi.</p>',
 ARRAY[
  'Sizning holatingizda LTV CAC dan qancha katta?',
  'Qaysi kanal qoʼshimcha qiymat beradi?',
  'Attribution modelini qanday tanlaysiz?'
 ],
 ARRAY[
  'Kanal roʼyxatini tuzing.',
  'Har kanal uchun toʼrt koʼrsatkich yozing.',
  'Ikkita attribution modeli bilan qayta hisoblang.',
  'Farqni izohlang va byudjet taklifini bering.'
 ],
 NULL, NULL, NULL,
 'Statista — online banking boʼyicha statistika', 'https://www.statista.com/topics/1677/online-banking/'
),

('IAKTT1105','S14',
 '<p>Parol bazasi sizib chiqdi. Bitta korxona zararsiz chiqdi — parollar shifrlangan holda saqlangan edi. Xavfsizlik koʼpincha bitta toʼgʼri qarorga borliq.</p>',
 '<p>Kriptografiya tushunchasini bilish — auditor bilan til topishish uchun zarur.</p>',
 ARRAY[
  '<p>Simmetrik shifrlash — bitta kalit (tez, katta hajm). Assimetrik — ochiq/yopiq juftlik (kalit uzatish va imzo uchun). Amaliy protokollarda ikkalasi birga ishlatiladi.</p>',
  '<p>Hash — bir tomonlama funksiya: parolni saqlashda va yaxlitlikni tekshirishda. Parol uchun salt qoʼshiladi, aks holda tayyor jadvallar bilan ochiladi.</p>',
  '<p>PKI va sertifikatlar kalit egasini tasdiqlaydi; sertifikat muddati va bekor qilish roʼyxati kuzatilishi shart.</p>'
 ],
 ARRAY[
  '<strong>Simmetrik / asimmetrik</strong> — ikki shifrlash turi.',
  '<strong>Hash</strong> — qaytarib boʼlmaydigan qisqa iz.',
  '<strong>Salt</strong> — hashga qoʼshiladigan tasodifiy qiymat.',
  '<strong>PKI</strong> — ochiq kalit infratuzilmasi.'
 ],
 '<p>Oʼz shifrlash algoritmini yozish — xavfli. Standart, sinovdan oʼtgan kutubxonalardan foydalaniladi.</p>',
 ARRAY[
  'Nega parolni shifrlash yetarli emas?',
  'Imzo nimani tasdiqlaydi?',
  'Sertifikat qachon yaroqsiz boʼladi?'
 ],
 ARRAY[
  'Maʼlumot turini tanlang (parol, hujjat, tranzaksiya).',
  'Uch ustun boʼyicha xavfni yozing.',
  'Mos mexanizmni tanlang: hash, shifrlash yoki imzo.',
  'Kalitni kim saqlashi va aylantirish tartibini belgilang.'
 ],
 NULL, NULL, NULL,
 'Mozilla — TLS va kalit konfiguratsiyasi boʼyicha qoʼllanma', 'https://infosec.mozilla.org/guidelines/openssh'
),

('IAKTT1105','S15',
 '<p>Ikkita bank omonati: birida 12.5% "yillik", ikkinchisida 1.05% "oylik". Qaysi biri foydali? Foiz foizga qoʼshilganda javob oʼzgaradi.</p>',
 '<p>Foiz va muddatni toʼgʼri solishtirish — shaxsiy moliyadan korporativ qarorgacha asosiy koʼnikma.</p>',
 ARRAY[
  '<p>Nominal va samarali stavka farqi kapitalizatsiyadan keladi: oylik qoʼshiladigan foiz yil oxirida koʼproq beradi.</p>',
  '<p>Kreditda APR/APC komissiya va sugʼurtani ham qamrab oladi — faqat foizga qarab tanlash aldaydi.</p>',
  '<p>Soliq hisobotida elektron format va muddat javobgarlik belgilaydi; kechikish jarimasi — prognozlanadigan xarajat.</p>'
 ],
 ARRAY[
  '<strong>Kapitalizatsiya</strong> — foizning foizga qoʼshilishi.',
  '<strong>APR / APC</strong> — yillik samarali toʼlov narxi.',
  '<strong>Qayta moliyalashtirish</strong> — kreditni boshqa shartga oʼtkazish.',
  '<strong>Muddat jarimasi</strong> — kechikkan hisob uchun javobgarlik.'
 ],
 '<p>Faqat <strong>reklamadagi foizni</strong> solishtirish — komissiya va sugʼurta bilan umumiy toʼlov boshqacha chiqadi.</p>',
 ARRAY[
  '12.5% yillik va 1.05% oylik — qaysi koʼp beradi?',
  'APR nega foizdan yuqori?',
  'Hisobot kechikishining narxini qanday hisoblaysiz?'
 ],
 ARRAY[
  'Ikkita mahsulot shartnomasini oling.',
  'Barcha toʼlovlarni bir yillik oqimga yigʼing.',
  'Kapitalizatsiyani hisobga olib qayta hisoblang.',
  'Umumiy narx boʼyicha xulosa chiqaring.'
 ],
 NULL, NULL, NULL,
 'Markaziy bank — rasmiy statistika va normativ hujjatlar', 'https://cbu.uz/uz/'
);

-- --------------------------------------------------------------------------
-- Generator: authored rows -> canonical content blocks.
-- Per lesson: motivation → explanation → key terms → (code) → worked steps →
-- the common mistake → self-check → one source.
-- Lessons that already carry blocks are skipped, so V902's M7 stays untouched.
-- --------------------------------------------------------------------------
DO $do$
DECLARE
    r         RECORD;
    v_lesson  uuid;
    v_pos     int;
    v_block   uuid;
    v_html    text;
    v_list    text;
BEGIN
    FOR r IN SELECT * FROM seed_content ORDER BY subject, lesson LOOP
        SELECT l.id INTO v_lesson
        FROM lessons l
        JOIN course_modules m ON m.id = l.module_id
        JOIN courses c        ON c.id = m.course_id
        JOIN subjects s       ON s.id = c.subject_id
        WHERE s.code = r.subject
          AND l.code = r.lesson
          AND c.id IN ('9c10f493-d95e-45ab-bf20-89eeee21f7f1',
                       '6a233536-0168-4f7c-a332-4c743f93aee7');

        IF v_lesson IS NULL THEN
            RAISE NOTICE 'V903: lesson not found — % %', r.subject, r.lesson;
            CONTINUE;
        END IF;

        IF EXISTS (SELECT 1 FROM content_blocks b WHERE b.lesson_id = v_lesson) THEN
            CONTINUE;
        END IF;

        v_pos := 0;

        -- 1 · Motivation: why the topic exists and why an economist needs it
        v_html := '<h2>Nima uchun bu mavzu muhim?</h2>' || r.hook || r.why;
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'TEXT', true, '{}'::jsonb) RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 2 · The explanation itself
        v_html := '<h2>Mavzuni ochib berish</h2>' || array_to_string(r.explain, '');
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'TEXT', true, '{}'::jsonb) RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 3 · Key terms
        SELECT string_agg('<li>' || t || '</li>', '') INTO v_list FROM unnest(r.concepts) AS t;
        v_html := '<h2>Asosiy tushunchalar</h2><ul>' || v_list || '</ul>';
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'TEXT', true, '{}'::jsonb) RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 4 · Worked code sample (programming course only)
        IF r.code IS NOT NULL THEN
            SELECT '<p>' || string_agg(x, '</p><p>') || '</p>' INTO v_list
            FROM unnest(r.codewalk) AS x;
            INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
            VALUES (v_lesson, v_pos, 'CODE_EXAMPLE', true,
                    jsonb_build_object('language', r.lang)) RETURNING id INTO v_block;
            INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
            VALUES (v_block, 'uz',
                    jsonb_build_object('code', r.code, 'explanation', coalesce(v_list, '')),
                    r.code || ' ' || coalesce(regexp_replace(v_list, '<[^>]*>', ' ', 'g'), ''));
            v_pos := v_pos + 1;
        END IF;

        -- 5 · Step-by-step solution
        SELECT string_agg('<li>' || t || '</li>', '') INTO v_list FROM unnest(r.worked) AS t;
        v_html := '<h2>Qadam-baqadam yechim</h2><ol>' || v_list || '</ol>';
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'TEXT', true, '{}'::jsonb) RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 6 · The mistake beginners actually make
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'CALLOUT', true, '{"variant":"WARNING"}'::jsonb)
        RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', r.mistake),
                regexp_replace(r.mistake, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 7 · Self-check
        SELECT string_agg('<li>' || t || '</li>', '') INTO v_list FROM unnest(r.checkin) AS t;
        v_html := '<p>Quyidagilarga javob bera olsangiz, mavzu oʼzlashtirilgan hisoblanadi:</p><ol>'
               || v_list || '</ol>';
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'CALLOUT', true, '{"variant":"INFO"}'::jsonb)
        RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));
        v_pos := v_pos + 1;

        -- 8 · One authoritative source
        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (v_lesson, v_pos, 'LINK', true, jsonb_build_object('url', r.link_url))
        RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz',
                jsonb_build_object('label', r.link_label, 'note',
                                   'Mavzuni chuqurlashtirish uchun tanlangan manba.'),
                r.link_label);
    END LOOP;
END
$do$;

-- --------------------------------------------------------------------------
-- Mustaqil taʼlim mavzulari (MT*). These are task briefs, not lectures, so they
-- are generated from the lesson title: goal, procedure, criteria, source.
-- --------------------------------------------------------------------------
DO $do$
DECLARE
    r       RECORD;
    v_block uuid;
    v_html  text;
    v_link  text;
    v_label text;
BEGIN
    FOR r IN
        SELECT l.id AS lesson_id, l.code, lt.title, s.code AS subject
        FROM lessons l
        JOIN course_modules m       ON m.id = l.module_id
        JOIN courses c              ON c.id = m.course_id
        JOIN subjects s             ON s.id = c.subject_id
        JOIN lesson_translations lt ON lt.lesson_id = l.id AND lt.locale = 'uz'
        WHERE l.session_type = 'INDEPENDENT_STUDY'
          AND c.id IN ('9c10f493-d95e-45ab-bf20-89eeee21f7f1',
                       '6a233536-0168-4f7c-a332-4c743f93aee7')
          AND NOT EXISTS (SELECT 1 FROM content_blocks b WHERE b.lesson_id = l.id)
        ORDER BY s.code, l.position
    LOOP
        IF r.subject = 'DAS1110' THEN
            v_link  := 'https://docs.python.org/3/tutorial/index.html';
            v_label := 'Python rasmiy darsligi — mustaqil oʼzlashtirish uchun';
        ELSE
            v_link  := 'https://ocw.mit.edu/courses/';
            v_label := 'MIT OpenCourseWare — mavzu boʼyicha ochiq maʼruzalar';
        END IF;

        v_html := '<h2>Maqsad</h2><p>Mustaqil ish mavzusi: <strong>' || r.title
               || '</strong>. Maqsad — mavzuni oʼzlashtirish, uni amaliy misolda qoʼllash va '
               || 'natijani hujjatlashtirish koʼnikmasini shakllantirish.</p>'
               || '<h2>Bajarish tartibi</h2><ol>'
               || '<li>Mavzu adabiyotini oʼrganib, asosiy tushunchalar lugʼatini tuzing (kamida 5 ta atama).</li>'
               || '<li>Nazariy qismni oʼz sohangizdan bir misol bilan bogʼlab tushuntiring.</li>'
               || '<li>Amaliy qismni bajaring: hisob-kitob, algoritm yoki dastur — va natijani izohlang.</li>'
               || '<li>Xulosada olingan natijaning iqtisodiy yoki kasbiy qiymatini koʼrsating.</li>'
               || '<li>Foydalanilgan manbalarni tartib bilan roʼyxat qiling (kamida uchta).</li>'
               || '</ol>';

        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (r.lesson_id, 0, 'TEXT', true, '{}'::jsonb) RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('html', v_html),
                regexp_replace(v_html, '<[^>]*>', ' ', 'g'));

        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (r.lesson_id, 1, 'CALLOUT', true, '{"variant":"WARNING"}'::jsonb)
        RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz',
                jsonb_build_object('html',
                    '<p><strong>Topshirish mezonlari:</strong> mavzuning toʼliq ochib berilishi, '
                 || 'amaliy qismning oʼzimiz bajarilganligi, xulosa va hisob-kitobning asosliligi, '
                 || 'manbalarni koʼrsatilishi. Boshqa ishdan koʼchirilgan qism qabul qilinmaydi.</p>'),
                'Topshirish mezonlari: toʼliqlik, amaliy qism, asoslilik, manbalar.');

        INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
        VALUES (r.lesson_id, 2, 'LINK', true, jsonb_build_object('url', v_link))
        RETURNING id INTO v_block;
        INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
        VALUES (v_block, 'uz', jsonb_build_object('label', v_label), v_label);
    END LOOP;
END
$do$;

DROP TABLE seed_content;
