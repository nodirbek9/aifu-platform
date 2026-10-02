// IAKTT1105 Iqtisodiyotda AKT va tizimlar — every lesson already has syllabus content (V905), so
// this module only appends practice: knowledge-check quizzes and two introductory coding tasks
// for the algorithm seminar and the deposit/credit seminar.
import { quiz, task, tip } from './_blocks.mjs'

const J = (body, imports = 'import java.util.*;') => `${imports}\n\npublic class Main {\n${body}\n}\n`

const tasks = [
  {
    code: 'IAKTT-S8',
    title: 'Tarmoqlanuvchi algoritm: xarid chegirmasi',
    description: '<p>Doʻkon xarid summasiga qarab chegirma beradi (oʻquv misoli uchun tanlangan qoidalar):</p><ul><li>1 000 000 soʻm va undan koʻp — 10 %;</li><li>500 000 soʻm va undan koʻp — 5 %;</li><li>qolgan holatlarda — chegirma yoʻq.</li></ul><p>Avval blok-sxemasini chizing (shart bloki — romb), soʻng dasturini yozing. Chegirma butun soʻmgacha pastga yaxlitlanadi.</p>',
    instructions: 'Kirish: n; n ta xarid summasi. Chiqish: har bir xarid uchun chegirma va toʻlanadigan summa.',
    constraints: '1 ≤ n ≤ 100; 0 ≤ summa ≤ 10^12.',
    python: {
      starter: 'n = int(input())\nfor _ in range(n):\n    amount = int(input())\n    # TODO: if / elif / else\n',
      solution: 'n = int(input())\nfor _ in range(n):\n    amount = int(input())\n    if amount >= 1_000_000:\n        discount = amount * 10 // 100\n    elif amount >= 500_000:\n        discount = amount * 5 // 100\n    else:\n        discount = 0\n    print(discount, amount - discount)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        for (int i = 0; i < n; i++) {\n            long amount = in.nextLong();\n            // TODO: if / else if / else\n        }\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        for (int i = 0; i < n; i++) {\n            long amount = in.nextLong();\n            long discount;\n            if (amount >= 1_000_000) discount = amount * 10 / 100;\n            else if (amount >= 500_000) discount = amount * 5 / 100;\n            else discount = 0;\n            System.out.println(discount + " " + (amount - discount));\n        }\n    }'),
    },
    tests: [
      ['3\n1200000\n750000\n499999', '120000 1080000\n37500 712500\n0 499999'],
      ['2\n500000\n1000000', '25000 475000\n100000 900000'],
      ['2\n999999\n0', '49999 950000\n0 0'],
    ],
  },
  {
    code: 'IAKTT-S15',
    title: 'Takrorlanuvchi algoritm: omonat foizlarini hisoblash',
    description: '<p>Omonatga yillik <em>r</em> % foiz qoʻshiladi. <strong>Murakkab foizda</strong> har yil foiz joriy balansga hisoblanadi va qoʻshiladi; <strong>oddiy foizda</strong> esa har yil faqat boshlangʻich summaga hisoblanadi. Har yilgi foiz butun soʻmgacha pastga yaxlitlanadi.</p><p>Bu hisob-kitob modeli oʻquv misoli; haqiqiy bank shartlari (kapitallashtirish davri, soliq) shartnomada belgilanadi.</p>',
    instructions: 'Kirish: summa foiz yillar. Chiqish: har bir yil uchun “yil balans” (murakkab foiz), oxirgi satrda Oddiy: yakuniy_summa.',
    constraints: '1 ≤ summa ≤ 10^10; 0 ≤ foiz ≤ 100; 1 ≤ yillar ≤ 30.',
    python: {
      starter: 'principal, rate, years = map(int, input().split())\nbalance = principal\n# TODO: for year in range(1, years + 1):\n',
      solution: 'principal, rate, years = map(int, input().split())\nbalance = principal\nfor year in range(1, years + 1):\n    balance += balance * rate // 100\n    print(year, balance)\nprint("Oddiy:", principal + principal * rate * years // 100)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long principal = in.nextLong();\n        int rate = in.nextInt(), years = in.nextInt();\n        long balance = principal;\n        // TODO: for sikli\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long principal = in.nextLong();\n        int rate = in.nextInt(), years = in.nextInt();\n        long balance = principal;\n        for (int year = 1; year <= years; year++) {\n            balance += balance * rate / 100;\n            System.out.println(year + " " + balance);\n        }\n        System.out.println("Oddiy: " + (principal + principal * rate * years / 100));\n    }'),
    },
    tests: [
      ['10000000 20 3', '1 12000000\n2 14400000\n3 17280000\nOddiy: 16000000'],
      ['1000 7 2', '1 1070\n2 1144\nOddiy: 1140'],
      ['5000000 0 1', '1 5000000\nOddiy: 5000000'],
    ],
  },
]

const quizzes = [
  {
    code: 'IAKTT-Q-S3',
    title: 'Operatsion tizimlar',
    questions: [
      { type: 'SINGLE', prompt: 'Qaysi operatsion tizim ochiq manba kodli yadroga asoslangan?', options: ['Linux', 'Microsoft Windows', 'Apple macOS', 'MS-DOS'], correct: [0] },
      { type: 'SINGLE', prompt: 'Operatsion tizimning asosiy vazifasi nima?', options: ['Apparat resurslarini boshqarish va dasturlarga xizmat koʻrsatish', 'Faqat matn terish', 'Faqat Internetga ulanish', 'Faqat rasm chizish'], correct: [0] },
      { type: 'MULTI', prompt: 'Qaysilari operatsion tizimlar?', options: ['Windows', 'Linux', 'macOS', 'Microsoft Excel'], correct: [0, 1, 2] },
      { type: 'TF', prompt: 'Fayl tizimi maʼlumotlarning diskda qanday saqlanishi va nomlanishini belgilaydi.', answer: true },
    ],
  },
  {
    code: 'IAKTT-Q-S4',
    title: 'Microsoft Excel: formulalar va tahlil',
    questions: [
      { type: 'SINGLE', prompt: 'A1:A10 kataklaridagi sonlar yigʻindisini hisoblovchi formula?', options: ['=SUM(A1:A10)', '=ADD(A1:A10)', '=A1+A10', '=TOTAL(A1;A10)'], correct: [0] },
      { type: 'SINGLE', prompt: 'Formulani nusxalaganda oʻzgarmaydigan (absolyut) murojaat qaysi?', options: ['$B$2', 'B2', 'B$', '#B2'], correct: [0] },
      { type: 'SINGLE', prompt: 'Katta jadvalni guruhlab, yigʻindilar va oʻrtachalarni tez hisoblash vositasi?', options: ['Yigʻma jadval (PivotTable)', 'Imlo tekshiruvi', 'Sahifa chegarasi', 'Shrift oʻlchami'], correct: [0] },
      { type: 'NUM', prompt: 'A1 = 12, A2 = 8, A3 = 10 boʻlsa, =AVERAGE(A1:A3) natijasi?', answer: 10 },
      { type: 'SINGLE', prompt: 'Shartga qarab ikki xil qiymat qaytaruvchi funksiya?', options: ['IF', 'SUM', 'COUNT', 'ROUND'], correct: [0] },
    ],
  },
  {
    code: 'IAKTT-Q-S7',
    title: 'Katta maʼlumotlar (Big Data)',
    questions: [
      { type: 'MULTI', prompt: 'Big Data ning asosiy xususiyatlari (“V” lar) qaysilar?', options: ['Hajm (Volume)', 'Tezlik (Velocity)', 'Xilma-xillik (Variety)', 'Arzonlik (Value for free)'], correct: [0, 1, 2] },
      { type: 'SINGLE', prompt: 'Katta hajmdagi maʼlumotlarni koʻp kompyuterlarda taqsimlab qayta ishlash uchun yaratilgan freymvork?', options: ['Apache Hadoop', 'Microsoft Paint', 'Notepad', 'WinRAR'], correct: [0] },
      { type: 'TF', prompt: 'Tuzilmalanmagan maʼlumotlarga matnlar, rasmlar va videolar kiradi.', answer: true },
      { type: 'SINGLE', prompt: 'Iqtisodiyotda Big Data qoʻllanilishiga misol?', options: ['Xaridorlar xatti-harakatini tahlil qilib, talabni prognoz qilish', 'Hujjatni chop etish', 'Kompyuterni oʻchirish', 'Parolni almashtirish'], correct: [0] },
    ],
  },
  {
    code: 'IAKTT-Q-S11',
    title: 'Bulutli texnologiyalar',
    questions: [
      { type: 'SINGLE', prompt: 'Tayyor dasturni brauzer orqali obuna asosida ishlatish qaysi xizmat modeli?', options: ['SaaS', 'IaaS', 'PaaS', 'LAN'], correct: [0] },
      { type: 'SINGLE', prompt: 'Virtual serverlar va xotirani ijaraga olish qaysi model?', options: ['IaaS', 'SaaS', 'E-mail', 'ERP'], correct: [0] },
      { type: 'MULTI', prompt: 'Bulutli texnologiyalarning afzalliklari:', options: ['Resurslarni tez kengaytirish', 'Foydalanganingizcha toʻlash', 'Istalgan joydan kirish', 'Internetsiz doim ishlash'], correct: [0, 1, 2] },
      { type: 'TF', prompt: 'Xususiy bulut faqat bitta tashkilot uchun ajratilgan infratuzilma.', answer: true },
    ],
  },
  {
    code: 'IAKTT-Q-S12',
    title: 'Blokcheyn texnologiyasi',
    questions: [
      { type: 'SINGLE', prompt: 'Blokcheynda har bir blok oldingi blok bilan qanday bogʻlanadi?', options: ['Oldingi blokning xesh-qiymati orqali', 'Elektron pochta orqali', 'Fayl nomi orqali', 'Hech qanday bogʻlanmaydi'], correct: [0] },
      { type: 'TF', prompt: 'Blokcheyn reyestri koʻplab ishtirokchilarda nusxada saqlanadi (taqsimlangan reyestr).', answer: true },
      { type: 'SINGLE', prompt: 'Shartlar bajarilganda avtomatik ishga tushadigan blokcheyndagi dastur?', options: ['Aqlli shartnoma (smart contract)', 'Makros', 'Antivirus', 'Brauzer kengaytmasi'], correct: [0] },
      { type: 'MULTI', prompt: 'Blokcheyn qoʻllanilishi mumkin boʻlgan sohalar:', options: ['Taʼminot zanjirida tovar kelib chiqishini kuzatish', 'Raqamli aktivlar va toʻlovlar', 'Hujjatlar haqiqiyligini tasdiqlash', 'Monitor yorqinligini sozlash'], correct: [0, 1, 2] },
    ],
  },
  {
    code: 'IAKTT-Q-S14',
    title: 'Kriptografik himoya usullari',
    questions: [
      { type: 'SINGLE', prompt: 'Shifrlash va deshifrlash uchun bitta kalit ishlatiladigan usul?', options: ['Simmetrik shifrlash', 'Asimmetrik shifrlash', 'Xeshlash', 'Arxivlash'], correct: [0] },
      { type: 'SINGLE', prompt: 'Elektron raqamli imzo nimani tasdiqlaydi?', options: ['Hujjat muallifi va hujjat oʻzgartirilmaganini', 'Hujjat hajmini', 'Hujjat rangini', 'Internet tezligini'], correct: [0] },
      { type: 'TF', prompt: 'HTTPS protokoli brauzer va sayt orasidagi maʼlumotlarni shifrlaydi.', answer: true },
      { type: 'SINGLE', prompt: 'Ochiq kalitli kriptografiyada xabarni kimning kaliti bilan shifrlash kerak, shunda uni faqat qabul qiluvchi ocha olsin?', options: ['Qabul qiluvchining ochiq kaliti', 'Joʻnatuvchining ochiq kaliti', 'Qabul qiluvchining yopiq kaliti', 'Kalit kerak emas'], correct: [0] },
    ],
  },
]

const quizLesson = (ref) => ({
  append: true,
  blocks: [tip('<p><strong>Bilimingizni tekshiring:</strong> seminar mavzusi boʻyicha qisqa test. Natija va izohlar topshirgandan soʻng koʻrinadi.</p>'), quiz(ref)],
})
const taskLesson = (ref) => ({
  append: true,
  blocks: [tip('<p><strong>Amaliy topshiriq:</strong> algoritmning blok-sxemasini chizing, soʻng dasturni Python yoki Java tilida yozing. Yechim avtomatik testlarda tekshiriladi.</p>'), task(ref)],
})

export default {
  subject: 'IAKTT1105',
  cohort: 'IQT-25-01',
  tasks,
  quizzes,
  lessons: {
    S3: quizLesson('IAKTT-Q-S3'),
    S4: quizLesson('IAKTT-Q-S4'),
    S7: quizLesson('IAKTT-Q-S7'),
    S8: taskLesson('IAKTT-S8'),
    S11: quizLesson('IAKTT-Q-S11'),
    S12: quizLesson('IAKTT-Q-S12'),
    S14: quizLesson('IAKTT-Q-S14'),
    S15: taskLesson('IAKTT-S15'),
  },
}
