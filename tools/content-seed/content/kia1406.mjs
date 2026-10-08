// KIA1406 Kiberxavfsizlik asoslari — lectures, practicals (coding tasks where the topic is
// computable, quizzes where it is conceptual), independent-study briefs.
// Task outputs stay ASCII: the Java sandbox's stdout charset follows the container locale.
import { LINKS, independent, practical, quiz, task, topic } from './_blocks.mjs'

const J = (body, imports = 'import java.util.*;') => `${imports}\n\npublic class Main {\n${body}\n}\n`
const IO = 'import java.io.*;\nimport java.util.*;'

const tasks = [
  {
    code: 'KIA-A2',
    title: 'Risk matritsasi: risklarni baholash va tartiblash',
    description: '<p>Har bir risk uchun <strong>ehtimollik</strong> (1–5) va <strong>taʼsir</strong> (1–5) berilgan. Risk darajasi = ehtimollik × taʼsir. Quyidagi 5×5 oʻquv shkalasi boʻyicha toifani aniqlang va risklarni darajasi boʻyicha kamayish tartibida chiqaring (teng boʻlsa — kiritilgan tartibda).</p><ul><li>1–4: <code>Past</code></li><li>5–9: <code>O\'rta</code></li><li>10–16: <code>Yuqori</code></li><li>17–25: <code>Kritik</code></li></ul><p>Shkala oʻquv misoli uchun tanlangan; tashkilotlar oʻz chegaralarini risk siyosatida belgilaydi.</p>',
    instructions: 'Kirish: n; n ta satrda nom ehtimollik taʼsir (nom boʻshliqsiz). Chiqish: har bir satrda nom daraja toifa.',
    constraints: '1 ≤ n ≤ 100.',
    python: {
      starter: 'n = int(input())\nrisks = []\nfor _ in range(n):\n    name, p, i = input().split()\n    # ...\n',
      solution: 'def level(score):\n    if score <= 4:\n        return "Past"\n    if score <= 9:\n        return "O\'rta"\n    if score <= 16:\n        return "Yuqori"\n    return "Kritik"\n\n\nn = int(input())\nrisks = []\nfor _ in range(n):\n    name, p, i = input().split()\n    risks.append((name, int(p) * int(i)))\nfor name, score in sorted(risks, key=lambda r: -r[1]):\n    print(name, score, level(score))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        // ...\n    }'),
      solution: J('    static String level(int s) {\n        if (s <= 4) return "Past";\n        if (s <= 9) return "O\'rta";\n        if (s <= 16) return "Yuqori";\n        return "Kritik";\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        List<String> names = new ArrayList<>();\n        List<Integer> scores = new ArrayList<>();\n        for (int k = 0; k < n; k++) {\n            names.add(in.next());\n            scores.add(in.nextInt() * in.nextInt());\n        }\n        Integer[] order = new Integer[n];\n        for (int k = 0; k < n; k++) order[k] = k;\n        Arrays.sort(order, (a, b) -> scores.get(b) - scores.get(a)); // barqaror\n        for (int k : order) System.out.println(names.get(k) + " " + scores.get(k) + " " + level(scores.get(k)));\n    }'),
    },
    tests: [
      ['4\nfishing 4 3\nflood 1 5\nransomware 3 5\nlaptop_loss 2 2', 'ransomware 15 Yuqori\nfishing 12 Yuqori\nflood 5 O\'rta\nlaptop_loss 4 Past'],
      ['2\nddos 5 5\ninsider 5 4', 'ddos 25 Kritik\ninsider 20 Kritik'],
      ['3\na 1 1\nb 3 3\nc 1 1', 'b 9 O\'rta\na 1 Past\nc 1 Past'],
    ],
  },
  {
    code: 'KIA-A5C',
    title: 'Sezar shifri',
    description: '<p>Sezar shifrida har bir lotin harfi alifboda <em>k</em> oʻringa suriladi (Z dan keyin A keladi). Katta-kichik harf saqlanadi, boshqa belgilar oʻzgarmaydi. Manfiy <em>k</em> — deshifrlash.</p>',
    instructions: 'Kirish: 1-satr k; 2-satr matn. Chiqish: shifrlangan matn.',
    constraints: '−1000 ≤ k ≤ 1000; matn faqat ASCII belgilaridan iborat.',
    python: {
      starter: 'k = int(input())\ntext = input()\n# ...\n',
      solution: 'k = int(input())\ntext = input()\nout = []\nfor ch in text:\n    if "a" <= ch <= "z":\n        out.append(chr((ord(ch) - 97 + k) % 26 + 97))\n    elif "A" <= ch <= "Z":\n        out.append(chr((ord(ch) - 65 + k) % 26 + 65))\n    else:\n        out.append(ch)\nprint("".join(out))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        int k = Integer.parseInt(br.readLine().trim());\n        String text = br.readLine();\n        // ...\n    }', IO),
      solution: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        int k = Integer.parseInt(br.readLine().trim());\n        String text = br.readLine();\n        StringBuilder out = new StringBuilder();\n        for (char ch : text.toCharArray()) {\n            if (ch >= \'a\' && ch <= \'z\') out.append((char) (\'a\' + Math.floorMod(ch - \'a\' + k, 26)));\n            else if (ch >= \'A\' && ch <= \'Z\') out.append((char) (\'A\' + Math.floorMod(ch - \'A\' + k, 26)));\n            else out.append(ch);\n        }\n        System.out.println(out);\n    }', IO),
    },
    tests: [['3\nHello, World!', 'Khoor, Zruog!'], ['-3\nKhoor, Zruog!', 'Hello, World!'], ['29\nxyz', 'abc'], ['13\nAIFU 2026', 'NVSH 2026'], ['-1000\nabc', 'opq']],
  },
  {
    code: 'KIA-A5V',
    title: 'Vijiner shifri',
    description: '<p>Vijiner shifri — koʻp alifboli almashtirish: matnning har bir harfi kalitning navbatdagi harfiga teng siljish bilan suriladi (A = 0, B = 1, …). Kalit indeksi faqat harflarda oldinga siljiydi; boʻshliq va tinish belgilari oʻzgarmaydi.</p>',
    instructions: 'Kirish: 1-satr E (shifrlash) yoki D (deshifrlash); 2-satr kalit (faqat harflar); 3-satr matn. Chiqish: natija.',
    constraints: 'Kalit uzunligi 1–50; matn ASCII.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'mode = input().strip()\nkey = input().strip().upper()\ntext = input()\n# ...\n',
      solution: 'mode = input().strip()\nkey = input().strip().upper()\ntext = input()\nsign = 1 if mode == "E" else -1\nout = []\nj = 0\nfor ch in text:\n    if ch.isascii() and ch.isalpha():\n        base = 65 if ch.isupper() else 97\n        shift = sign * (ord(key[j % len(key)]) - 65)\n        out.append(chr((ord(ch) - base + shift) % 26 + base))\n        j += 1\n    else:\n        out.append(ch)\nprint("".join(out))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String mode = br.readLine().trim();\n        String key = br.readLine().trim().toUpperCase();\n        String text = br.readLine();\n        // ...\n    }', IO),
      solution: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String mode = br.readLine().trim();\n        String key = br.readLine().trim().toUpperCase();\n        String text = br.readLine();\n        int sign = mode.equals("E") ? 1 : -1;\n        StringBuilder out = new StringBuilder();\n        int j = 0;\n        for (char ch : text.toCharArray()) {\n            boolean upper = ch >= \'A\' && ch <= \'Z\', lower = ch >= \'a\' && ch <= \'z\';\n            if (upper || lower) {\n                char base = upper ? \'A\' : \'a\';\n                int shift = sign * (key.charAt(j % key.length()) - \'A\');\n                out.append((char) (base + Math.floorMod(ch - base + shift, 26)));\n                j++;\n            } else out.append(ch);\n        }\n        System.out.println(out);\n    }', IO),
    },
    tests: [
      ['E\nLEMON\nATTACK AT DAWN', 'LXFOPV EF RNHR'],
      ['D\nLEMON\nLXFOPV EF RNHR', 'ATTACK AT DAWN'],
      ['E\nb\nabc xyz', 'bcd yza'],
      ['E\nKEY\nHello', 'Rijvs'],
      ['D\nkey\nRijvs', 'Hello'],
    ],
  },
  {
    code: 'KIA-A7',
    title: 'RSA: kalit yaratish, shifrlash va deshifrlash',
    description: '<p>Kichik tub sonlar p va q hamda ochiq daraja e berilgan. RSA bosqichlarini bajaring:</p><ol><li>n = p·q, φ(n) = (p − 1)(q − 1);</li><li>yopiq daraja d = e⁻¹ mod φ(n) (kengaytirilgan Evklid algoritmi);</li><li>shifrmatn c = mᵉ mod n;</li><li>tekshiruv: c^d mod n = m.</li></ol><p>Haqiqiy RSA’da p va q yuzlab raqamli boʻladi va toʻldirish (padding, masalan OAEP) qoʻllanadi; bu yerda faqat matematik gʻoya oʻrganiladi.</p>',
    instructions: 'Kirish: p q e m. Chiqish: bitta satrda n φ d c deshifrlangan_m.',
    constraints: 'p, q < 10^6 tub; gcd(e, φ) = 1; 0 ≤ m < n.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'p, q, e, m = map(int, input().split())\n# ...\n',
      solution: 'p, q, e, m = map(int, input().split())\nn = p * q\nphi = (p - 1) * (q - 1)\nd = pow(e, -1, phi)\nc = pow(m, e, n)\nprint(n, phi, d, c, pow(c, d, n))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        BigInteger p = in.nextBigInteger(), q = in.nextBigInteger(), e = in.nextBigInteger(), m = in.nextBigInteger();\n        // ...\n    }', 'import java.math.BigInteger;\nimport java.util.*;'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        BigInteger p = in.nextBigInteger(), q = in.nextBigInteger(), e = in.nextBigInteger(), m = in.nextBigInteger();\n        BigInteger n = p.multiply(q);\n        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));\n        BigInteger d = e.modInverse(phi);\n        BigInteger c = m.modPow(e, n);\n        System.out.println(n + " " + phi + " " + d + " " + c + " " + c.modPow(d, n));\n    }', 'import java.math.BigInteger;\nimport java.util.*;'),
    },
    tests: [
      ['61 53 17 65', '3233 3120 2753 2790 65'],
      ['11 13 7 9', '143 120 103 48 9'],
      ['1009 1013 65537 123456', '1022117 1020096 832193 571714 123456'],
      ['999983 1000003 65537 42', '999985999949 999983999964 83020476125 310228269852 42'],
    ],
  },
  {
    code: 'KIA-A8',
    title: 'SHA-256 bilan yaxlitlikni tekshirish',
    description: '<p>Xesh-funksiya ixtiyoriy uzunlikdagi maʼlumotdan qatʼiy uzunlikdagi “barmoq izi” hosil qiladi. Bitta belgi oʻzgarsa ham SHA-256 qiymati butunlay boshqacha boʻladi (koʻchki effekti). Xabarning SHA-256 qiymatini hisoblang va berilgan qiymat bilan solishtiring.</p>',
    instructions: 'Kirish: 1-satr xabar (UTF-8); 2-satr kutilgan xesh (hex). Chiqish: 1-satr hisoblangan xesh (kichik harfli hex); 2-satr Yaxlit yoki Buzilgan.',
    constraints: 'Xabar uzunligi 1–1000 belgi.',
    python: {
      starter: 'import hashlib\n\nmessage = input()\nexpected = input().strip().lower()\n# ...\n',
      solution: 'import hashlib\n\nmessage = input()\nexpected = input().strip().lower()\ndigest = hashlib.sha256(message.encode("utf-8")).hexdigest()\nprint(digest)\nprint("Yaxlit" if digest == expected else "Buzilgan")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws Exception {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));\n        String message = br.readLine();\n        String expected = br.readLine().trim().toLowerCase();\n        // ...\n    }', 'import java.io.*;\nimport java.nio.charset.StandardCharsets;\nimport java.security.MessageDigest;\nimport java.util.*;'),
      solution: J('    public static void main(String[] args) throws Exception {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));\n        String message = br.readLine();\n        String expected = br.readLine().trim().toLowerCase();\n        byte[] hash = MessageDigest.getInstance("SHA-256").digest(message.getBytes(StandardCharsets.UTF_8));\n        String digest = HexFormat.of().formatHex(hash);\n        System.out.println(digest);\n        System.out.println(digest.equals(expected) ? "Yaxlit" : "Buzilgan");\n    }', 'import java.io.*;\nimport java.nio.charset.StandardCharsets;\nimport java.security.MessageDigest;\nimport java.util.*;'),
    },
    tests: [
      ['abc\nba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad\nYaxlit'],
      ['abd\nba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'a52d159f262b2c6ddb724a61840befc36eb30c88877a4030b65cbe86298449c9\nBuzilgan'],
      ['Salom, AIFU!\n0000', 'c5b2659c0ecce6f9d33b7dca2a907118ca678a9eb3122ee99b2ad36ed80f2085\nBuzilgan'],
    ],
  },
  {
    code: 'KIA-A10',
    title: 'Parol siyosati: parol kuchini baholash',
    description: '<p>Parol siyosatini tekshiruvchi dastur yozing. Har bir mezon uchun 1 ball: uzunlik ≥ 8; uzunlik ≥ 12; kichik harf; katta harf; raqam; maxsus belgi (harf va raqam boʻlmagan belgi). Agar parol keng tarqalgan parollar roʻyxatida boʻlsa (<code>password, 123456, 12345678, qwerty, admin, parol</code>, katta-kichik harf farqsiz) — ball 0.</p><ul><li>0–2: <code>Zaif</code></li><li>3–4: <code>O\'rtacha</code></li><li>5–6: <code>Kuchli</code></li></ul>',
    instructions: 'Kirish: n; n ta parol (har biri alohida satrda, boʻshliqsiz). Chiqish: har bir parol uchun ball va daraja.',
    constraints: '1 ≤ n ≤ 100, parol ASCII.',
    python: {
      starter: 'COMMON = {"password", "123456", "12345678", "qwerty", "admin", "parol"}\n\nfor _ in range(int(input())):\n    pw = input().strip()\n    # ...\n',
      solution: 'COMMON = {"password", "123456", "12345678", "qwerty", "admin", "parol"}\n\n\ndef score(pw):\n    if pw.lower() in COMMON:\n        return 0\n    checks = [\n        len(pw) >= 8,\n        len(pw) >= 12,\n        any(c.islower() for c in pw),\n        any(c.isupper() for c in pw),\n        any(c.isdigit() for c in pw),\n        any(not c.isalnum() for c in pw),\n    ]\n    return sum(checks)\n\n\nfor _ in range(int(input())):\n    s = score(input().strip())\n    print(s, "Zaif" if s <= 2 else "O\'rtacha" if s <= 4 else "Kuchli")\n',
    },
    java: {
      starter: J('    static final Set<String> COMMON = Set.of("password", "123456", "12345678", "qwerty", "admin", "parol");\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        // ...\n    }'),
      solution: J('    static final Set<String> COMMON = Set.of("password", "123456", "12345678", "qwerty", "admin", "parol");\n\n    static int score(String pw) {\n        if (COMMON.contains(pw.toLowerCase())) return 0;\n        boolean lower = false, upper = false, digit = false, special = false;\n        for (char c : pw.toCharArray()) {\n            if (Character.isLowerCase(c)) lower = true;\n            else if (Character.isUpperCase(c)) upper = true;\n            else if (Character.isDigit(c)) digit = true;\n            else special = true;\n        }\n        int s = 0;\n        for (boolean b : new boolean[] {pw.length() >= 8, pw.length() >= 12, lower, upper, digit, special}) if (b) s++;\n        return s;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        for (int k = 0; k < n; k++) {\n            int s = score(in.next());\n            System.out.println(s + " " + (s <= 2 ? "Zaif" : s <= 4 ? "O\'rtacha" : "Kuchli"));\n        }\n    }'),
    },
    tests: [
      ['3\nQwerty\nsalom2026\nT0shkent#Bahor', '0 Zaif\n3 O\'rtacha\n6 Kuchli'],
      ['2\nPASSWORD\nAb1!', '0 Zaif\n4 O\'rtacha'],
      ['2\nabcdefghijkl\nAIFU-lms-2026', '3 O\'rtacha\n6 Kuchli'],
    ],
  },
  {
    code: 'KIA-A11',
    title: 'Linux huquqlari: rwx dan sakkizlik koʻrinishga',
    description: '<p><code>ls -l</code> buyrugʻi fayl huquqlarini <code>-rwxr-xr--</code> kabi koʻrsatadi: birinchi belgi fayl turi, keyin egasi, guruh va boshqalar uchun uchtadan belgi. Har bir uchlik sakkizlik raqamga aylanadi: r = 4, w = 2, x = 1. Masalan, <code>rwxr-xr--</code> → <code>754</code> (<code>chmod 754 fayl</code>).</p>',
    instructions: 'Kirish: n; n ta satrda 10 belgili huquq satri. Chiqish: har biri uchun sakkizlik kod va fayl turi (fayl, katalog yoki havola).',
    constraints: 'Fayl turi belgisi: - (fayl), d (katalog), l (havola).',
    python: {
      starter: 'for _ in range(int(input())):\n    s = input().strip()\n    # ...\n',
      solution: 'KIND = {"-": "fayl", "d": "katalog", "l": "havola"}\nVALUE = {"r": 4, "w": 2, "x": 1}\n\nfor _ in range(int(input())):\n    s = input().strip()\n    digits = ""\n    for g in range(3):\n        triple = s[1 + 3 * g:4 + 3 * g]\n        digits += str(sum(VALUE.get(c, 0) for c in triple))\n    print(digits, KIND[s[0]])\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        for (int k = 0; k < n; k++) {\n            String s = in.next();\n            StringBuilder digits = new StringBuilder();\n            for (int g = 0; g < 3; g++) {\n                int v = 0;\n                for (char c : s.substring(1 + 3 * g, 4 + 3 * g).toCharArray())\n                    v += c == \'r\' ? 4 : c == \'w\' ? 2 : c == \'x\' ? 1 : 0;\n                digits.append(v);\n            }\n            String kind = s.charAt(0) == \'d\' ? "katalog" : s.charAt(0) == \'l\' ? "havola" : "fayl";\n            System.out.println(digits + " " + kind);\n        }\n    }'),
    },
    tests: [
      ['2\n-rwxr-xr--\ndrwxr-xr-x', '754 fayl\n755 katalog'],
      ['1\n-rw-------', '600 fayl'],
      ['2\nlrwxrwxrwx\n----------', '777 havola\n000 fayl'],
    ],
  },
]

const quizzes = [
  {
    code: 'KIA-Q-A1',
    title: 'Axborot xavfsizligi tushunchalari',
    questions: [
      { type: 'SINGLE', prompt: 'Maʼlumot faqat vakolatli shaxslarga ochiq boʻlishi CIA triadasining qaysi xossasi?', options: ['Konfidensiallik', 'Yaxlitlik', 'Foydalanish mumkinligi', 'Hisobdorlik'], correct: [0] },
      { type: 'SINGLE', prompt: 'Bank hisobidagi summa ruxsatsiz oʻzgartirildi. Qaysi xossa buzilgan?', options: ['Konfidensiallik', 'Yaxlitlik', 'Foydalanish mumkinligi', 'Anonimlik'], correct: [1] },
      { type: 'SINGLE', prompt: 'DoS hujumi birinchi navbatda qaysi xossaga qarshi qaratilgan?', options: ['Yaxlitlik', 'Konfidensiallik', 'Foydalanish mumkinligi', 'Autentiklik'], correct: [2] },
      { type: 'SINGLE', prompt: 'Tizimdagi yangilanmagan dastur — bu…', options: ['Tahdid', 'Zaiflik', 'Risk', 'Nazorat vositasi'], correct: [1], explanation: 'Zaiflik — tahdid foydalanishi mumkin boʻlgan kamchilik. Risk esa tahdidning zaiflikdan foydalanish ehtimoli va oqibati birikmasi.' },
      { type: 'MULTI', prompt: 'Quyidagilardan qaysilari nazorat (himoya) vositalari?', options: ['Xavfsizlik devori (firewall)', 'Zaxira nusxalash', 'Fishing xati', 'Xodimlarni oʻqitish'], correct: [0, 1, 3] },
    ],
  },
  {
    code: 'KIA-Q-A3',
    title: 'Kiberjinoyatchilik va huquqiy asoslar',
    questions: [
      { type: 'SINGLE', prompt: 'Tashkilot xodimi oʻz vakolatidan foydalanib maʼlumot oʻgʻirlashi qanday tahdid turi?', options: ['Tashqi tahdid', 'Ichki tahdid (insider)', 'Tabiiy tahdid', 'Texnik nosozlik'], correct: [1] },
      { type: 'SINGLE', prompt: 'Raqamli kriminalistikada dalilning oʻzgarmaganini koʻrsatish uchun odatda nima qoʻllanadi?', options: ['Faylni siqish', 'Dalil nusxasining xesh-qiymatini qayd etish', 'Faylni shifrlash', 'Faylni qayta nomlash'], correct: [1] },
      { type: 'TF', prompt: '“Dalillar zanjiri” (chain of custody) dalil kim tomonidan va qachon ishlanganini hujjatlashtiradi.', answer: true },
      { type: 'SINGLE', prompt: 'Qurbonni aldab parolni olishga qaratilgan xat yuborish qanday ataladi?', options: ['Fishing', 'Brute force', 'Sniffing', 'DoS'], correct: [0] },
      { type: 'TF', prompt: 'Kiberetika faqat qonun bilan taqiqlangan harakatlarni oʻrganadi.', answer: false, explanation: 'Etika qonundan kengroq: qonuniy, lekin axloqan notoʻgʻri xatti-harakatlarni ham koʻrib chiqadi.' },
    ],
  },
  {
    code: 'KIA-Q-A4',
    title: 'Xavfsizlik siyosati',
    questions: [
      { type: 'SINGLE', prompt: 'Xavfsizlik hujjatlari iyerarxiyasida eng yuqori, umumiy darajadagi hujjat qaysi?', options: ['Xavfsizlik siyosati', 'Standart', 'Protsedura (yoʻriqnoma)', 'Konfiguratsiya fayli'], correct: [0] },
      { type: 'SINGLE', prompt: '“Xodimga faqat ishini bajarish uchun zarur boʻlgan huquqlar beriladi” — bu qaysi tamoyil?', options: ['Eng kam imtiyoz (least privilege)', 'Chuqur himoya', 'Xavfsizlik obskurantizmi', 'Ochiq dizayn'], correct: [0] },
      { type: 'MULTI', prompt: 'Parol siyosatiga odatda nimalar kiradi?', options: ['Minimal uzunlik', 'Murakkablik talablari', 'Parolni hamkasbga aytish tartibi', 'Muvaffaqiyatsiz urinishlardan keyin bloklash'], correct: [0, 1, 3] },
      { type: 'TF', prompt: 'Qabul qilinadigan foydalanish siyosati (AUP) xodimlar tashkilot resurslaridan qanday foydalanishi mumkinligini belgilaydi.', answer: true },
    ],
  },
  {
    code: 'KIA-Q-A6',
    title: 'Simmetrik shifrlash va AES',
    questions: [
      { type: 'SINGLE', prompt: 'Simmetrik shifrlashning asosiy xususiyati nima?', options: ['Shifrlash va deshifrlash uchun bir xil kalit ishlatiladi', 'Ikki xil kalit ishlatiladi', 'Kalit umuman ishlatilmaydi', 'Faqat xesh hisoblanadi'], correct: [0] },
      { type: 'SINGLE', prompt: 'AES blok uzunligi necha bit?', options: ['64', '128', '192', '256'], correct: [1], explanation: 'AES blok uzunligi har doim 128 bit; kalit uzunligi esa 128, 192 yoki 256 bit boʻlishi mumkin.' },
      { type: 'MULTI', prompt: 'AES qaysi kalit uzunliklarini qoʻllab-quvvatlaydi?', options: ['56 bit', '128 bit', '192 bit', '256 bit'], correct: [1, 2, 3] },
      { type: 'SINGLE', prompt: 'A5/1 algoritmi qaysi turdagi shifr?', options: ['Blokli', 'Oqimli', 'Ochiq kalitli', 'Xesh-funksiya'], correct: [1] },
      { type: 'TF', prompt: 'ECB rejimida bir xil ochiq matn bloklari bir xil shifrmatn bloklarini beradi, shuning uchun u tavsiya etilmaydi.', answer: true },
    ],
  },
  {
    code: 'KIA-Q-A9',
    title: 'SSL/TLS va sertifikatlar',
    questions: [
      { type: 'SINGLE', prompt: 'Brauzer sayt sertifikatini kim imzolaganini tekshiradi. Bu kim?', options: ['Sertifikatlash markazi (CA)', 'Internet provayder', 'DNS server', 'Foydalanuvchining oʻzi'], correct: [0] },
      { type: 'SINGLE', prompt: 'HTTPS standart holatda qaysi TCP portidan foydalanadi?', options: ['80', '21', '443', '25'], correct: [2] },
      { type: 'MULTI', prompt: 'X.509 sertifikatida qanday maʼlumotlar boʻladi?', options: ['Egasining nomi (subject)', 'Ochiq kalit', 'Amal qilish muddati', 'Yopiq kalit'], correct: [0, 1, 2], explanation: 'Yopiq kalit hech qachon sertifikatga kiritilmaydi — u faqat egasida saqlanadi.' },
      { type: 'TF', prompt: 'Muddati oʻtgan sertifikatni brauzer ishonchli deb hisoblaydi.', answer: false },
    ],
  },
  {
    code: 'KIA-Q-A12',
    title: 'Fizik xavfsizlik',
    questions: [
      { type: 'MULTI', prompt: 'Qaysilari fizik xavfsizlik nazorat vositalari?', options: ['Kirish kartalari', 'Videokuzatuv', 'Antivirus', 'Server xonasi qulfi'], correct: [0, 1, 3] },
      { type: 'SINGLE', prompt: 'Ruxsatsiz shaxs xodim ortidan eshikdan kirib olishi qanday ataladi?', options: ['Tailgating', 'Phishing', 'Spoofing', 'Sniffing'], correct: [0] },
      { type: 'SINGLE', prompt: 'Eskirgan qattiq diskdagi maxfiy maʼlumotni eng ishonchli yoʻq qilish usuli?', options: ['Fayllarni savatchaga tashlash', 'Diskni formatlash', 'Diskni fizik yoʻq qilish yoki maxsus qayta yozish', 'Diskni boshqa kompyuterga oʻrnatish'], correct: [2] },
      { type: 'TF', prompt: 'Yongʻin va suv toshqini — sunʼiy (inson omilli) tahdidlar.', answer: false },
    ],
  },
  {
    code: 'KIA-Q-A13',
    title: 'Tarmoq trafikini monitoring qilish',
    questions: [
      { type: 'SINGLE', prompt: 'Wireshark’da faqat HTTP trafikni koʻrsatish uchun qaysi displey-filtr ishlatiladi?', options: ['http', 'port = web', 'show http', 'tcp.http'], correct: [0] },
      { type: 'SINGLE', prompt: 'Tarmoq kartasini barcha kadrlarni qabul qiladigan rejimga oʻtkazish qanday ataladi?', options: ['Promiscuous mode', 'Safe mode', 'Duplex mode', 'Bridge mode'], correct: [0] },
      { type: 'SINGLE', prompt: 'Nima uchun HTTP orqali yuborilgan parolni snifferda koʻrish mumkin?', options: ['HTTP trafik shifrlanmagan', 'Parol xeshlangan', 'TLS ishlatilgan', 'Parol DNS orqali yuboriladi'], correct: [0] },
      { type: 'TF', prompt: 'Tarmoq trafikini faqat ruxsat berilgan, oʻzingizga tegishli yoki oʻquv tarmogʻida tahlil qilish kerak.', answer: true },
    ],
  },
  {
    code: 'KIA-Q-A14',
    title: 'VPN',
    questions: [
      { type: 'SINGLE', prompt: 'VPN ning asosiy vazifasi nima?', options: ['Ochiq tarmoq orqali shifrlangan tunnel yaratish', 'Internet tezligini oshirish', 'Viruslarni oʻchirish', 'IP manzil ajratish'], correct: [0] },
      { type: 'MULTI', prompt: 'Qaysilari VPN protokollari?', options: ['IPSec', 'WireGuard', 'OpenVPN', 'SMTP'], correct: [0, 1, 2] },
      { type: 'TF', prompt: 'VPN ulanganda foydalanuvchi zararli saytlardan toʻliq himoyalangan boʻladi.', answer: false, explanation: 'VPN trafikni shifrlaydi, lekin zararli dastur yoki fishing saytdan himoya qilmaydi.' },
      { type: 'SINGLE', prompt: 'IPSec qaysi OSI pogʻonasida ishlaydi?', options: ['Kanal', 'Tarmoq', 'Transport', 'Amaliy'], correct: [1] },
    ],
  },
  {
    code: 'KIA-Q-A15',
    title: 'IoT xavfsizligi',
    questions: [
      { type: 'SINGLE', prompt: 'IoT qurilmalarining eng keng tarqalgan zaifliklaridan biri?', options: ['Zavod (standart) parollari oʻzgartirilmagan', 'Juda kuchli protsessor', 'Koʻp xotira', 'Ochiq manba kodi'], correct: [0] },
      { type: 'SINGLE', prompt: '2016-yilda IoT qurilmalaridan iborat botnet bilan yirik DDoS hujumlari uyushtirgan zararli dastur?', options: ['Mirai', 'WannaCry', 'Stuxnet', 'Zeus'], correct: [0] },
      { type: 'MULTI', prompt: 'IoT qurilmasini himoyalash choralari:', options: ['Proshivkani yangilab turish', 'Standart parolni almashtirish', 'Qurilmani alohida tarmoq segmentiga ajratish', 'Barcha portlarni internetga ochish'], correct: [0, 1, 2] },
      { type: 'TF', prompt: 'Bulutli xizmatda maʼlumot xavfsizligi uchun faqat provayder javobgar.', answer: false, explanation: 'Umumiy javobgarlik modeli: provayder infratuzilmani, mijoz esa oʻz maʼlumotlari, kirish huquqlari va sozlamalarini himoya qiladi.' },
    ],
  },
]

export default {
  subject: 'KIA1406',
  cohort: 'DI-24-01-S3',
  tasks,
  quizzes,
  lessons: {
    M1: topic({
      intro: '<p><strong>Kiberxavfsizlik</strong> — kompyuter tizimlari, tarmoqlar va maʼlumotlarni ruxsatsiz kirish, oʻzgartirish va yoʻq qilishdan himoyalash amaliyoti. Uning markazida uchta xossa turadi — <strong>CIA triadasi</strong>.</p>',
      points: [
        '<strong>Konfidensiallik</strong> — maʼlumot faqat vakolatli shaxslarga ochiq.',
        '<strong>Yaxlitlik</strong> — maʼlumot ruxsatsiz oʻzgartirilmagan.',
        '<strong>Foydalanish mumkinligi</strong> — maʼlumot va xizmatlar kerak paytda ishlaydi.',
        '<strong>Tahdid</strong> — zarar yetkazishi mumkin boʻlgan hodisa yoki shaxs; <strong>zaiflik</strong> — tizimdagi kamchilik; <strong>risk</strong> — tahdid zaiflikdan foydalanish ehtimoli × oqibati.',
        '<strong>Nazorat vositalari</strong>: maʼmuriy (siyosat, oʻqitish), texnik (shifrlash, firewall), fizik (qulflar).',
      ],
      detail: '<table><thead><tr><th>Hodisa</th><th>Buzilgan xossa</th></tr></thead><tbody><tr><td>Maʼlumotlar bazasi sizib chiqdi</td><td>Konfidensiallik</td></tr><tr><td>Baho ruxsatsiz oʻzgartirildi</td><td>Yaxlitlik</td></tr><tr><td>Sayt DDoS hujumi tufayli ishlamayapti</td><td>Foydalanish mumkinligi</td></tr></tbody></table>',
      mistake: 'xavfsizlikni faqat texnik masala deb bilish. Koʻp hodisalar inson omili (zaif parol, fishing) tufayli yuz beradi.',
      practice: 'Universitet LMS tizimi uchun har bir CIA xossasi buzilishiga bittadan misol keltiring.',
    }),
    M2: topic({
      intro: '<p><strong>Risklarni boshqarish</strong> — risklarni aniqlash, baholash, ularga javob choralarini tanlash va monitoring qilishning uzluksiz jarayoni. Risk odatda <strong>ehtimollik (chastota)</strong> va <strong>taʼsir (oqibat)</strong> koʻpaytmasi sifatida baholanadi va <strong>risk matritsasi</strong>da koʻrsatiladi.</p>',
      points: [
        'Riskka javob strategiyalari: kamaytirish, oʻtkazish (masalan, sugʻurta), qochish, qabul qilish.',
        'Sifat (past/oʻrta/yuqori) va miqdoriy (pul birligida) baholash.',
        'Miqdoriy baholash: ALE = SLE × ARO (yillik kutilgan zarar = bir martalik zarar × yillik takrorlanish).',
        '<strong>RMIS</strong> (Risk Management Information System) — risklar reyestri, baholash va hisobotlarni yurituvchi axborot tizimi.',
      ],
      pyCode: '# Miqdoriy baholash: yillik kutilgan zarar (ALE)\nasset_value = 50_000_000       # soʻm, server qiymati\nexposure = 0.4                 # hodisada yoʻqotiladigan ulush\nsle = asset_value * exposure   # bir martalik zarar\naro = 0.5                      # ikki yilda bir marta\nprint("SLE =", sle)\nprint("ALE =", sle * aro)\n',
      javaCode: J('    public static void main(String[] args) {\n        int likelihood = 4, impact = 3;           // 1..5 shkalalar\n        int score = likelihood * impact;\n        String level = score <= 4 ? "Past" : score <= 9 ? "O\'rta" : score <= 16 ? "Yuqori" : "Kritik";\n        System.out.println(score + " " + level); // 12 Yuqori\n    }'),
      mistake: 'riskni bir marta baholab, keyin qayta koʻrib chiqmaslik. Tahdidlar va tizimlar oʻzgaradi — risk reyestri muntazam yangilanishi kerak.',
      practice: 'SLE = 10 mln soʻm, ARO = 3 boʻlsa, ALE qancha? (30 mln soʻm.)',
    }),
    M3: topic({
      intro: '<p><strong>Kiberjinoyat</strong> — kompyuter yoki tarmoq vosita yoki nishon boʻlgan jinoyat. Tahdid manbasiga koʻra <strong>ichki</strong> (xodimlar, pudratchilar) va <strong>tashqi</strong> (xakerlar, jinoiy guruhlar, davlat homiyligidagi guruhlar) boʻladi. Kiberjinoyatlarga qarshi kurash xalqaro konvensiyalar (masalan, Yevropa Kengashining Budapesht konvensiyasi) va milliy qonunchilik asosida olib boriladi.</p>',
      points: [
        'Keng tarqalgan turlar: fishing, toʻlov talab qiluvchi dasturlar (ransomware), shaxsiy maʼlumot oʻgʻirlash, onlayn firibgarlik.',
        '<strong>Kiberetika</strong> — raqamli muhitdagi axloqiy xatti-harakat meʼyorlari.',
        '<strong>Raqamli kriminalistika</strong>: dalilni yigʻish → saqlash (xesh bilan) → tahlil → hisobot; dalillar zanjiri hujjatlashtiriladi.',
      ],
      detail: '<p>Oʻzbekiston qonunchiligidagi aniq modda va jazolarni oʻrganishda rasmiy manbalardan (qonunchilik maʼlumotlari milliy bazasi) foydalaning; bu yerda umumiy tushunchalar berilgan.</p>',
      mistake: 'raqamli dalilni asl nusxada tahlil qilish. Tahlil nusxada, asl nusxa esa oʻzgarmas holda saqlanadi.',
      practice: 'Ichki tahdidga ikkita, tashqi tahdidga ikkita misol keltiring.',
    }),
    M4: topic({
      intro: '<p><strong>Xavfsizlik arxitekturasi</strong> — tizimda himoya vositalarining qanday joylashishi va oʻzaro bogʻlanishi. <strong>Xavfsizlik siyosati</strong> esa tashkilotning “nima himoyalanadi, kim javobgar va qanday qoidalar amal qiladi” degan savollarga javob beruvchi rasmiy hujjati.</p>',
      points: [
        'Iyerarxiya: siyosat → standartlar → protseduralar (yoʻriqnomalar) → koʻrsatmalar.',
        'Siyosat turlari: tashkiliy (umumiy), muammoga yoʻnaltirilgan (masalan, parol, masofaviy ish), tizimga xos.',
        'Tamoyillar: <strong>eng kam imtiyoz</strong>, <strong>chuqur himoya</strong> (defense in depth), vazifalarni ajratish.',
        'Strategiya biznes maqsadlariga bogʻlanadi va rahbariyat tomonidan tasdiqlanadi.',
      ],
      mistake: 'siyosatni juda texnik yozish. Siyosat hamma xodim tushunadigan tilda boʻladi; texnik tafsilotlar standart va protseduralarda.',
      practice: 'Kichik oʻquv markazi uchun “Parol siyosati”ning beshta bandini yozing.',
    }),
    M5: topic({
      intro: '<p><strong>Kriptografiya</strong> — maʼlumotni uchinchi shaxslardan yashirish usullari haqidagi fan. <strong>Kriptotizim</strong> ochiq matn, shifrmatn, kalitlar toʻplami, shifrlash va deshifrlash algoritmlaridan iborat. <strong>Kerkxofs prinsipi</strong>: tizim xavfsizligi algoritm sirligiga emas, faqat kalit sirligiga tayanishi kerak.</p>',
      points: [
        'Tarixiy usullar: Sezar (siljitish), Vijiner (koʻp alifboli), Enigma (elektromexanik rotorli).',
        'Almashtirish (substitution) va oʻrin almashtirish (transposition) shifrlari.',
        'Kriptotahlil: chastota tahlili oddiy almashtirish shifrlarini osongina buzadi.',
      ],
      pyCode: 'from collections import Counter\n\ncipher = "Khoor Zruog Khoor"\nfreq = Counter(c for c in cipher.lower() if c.isalpha())\nprint(freq.most_common(3))  # eng koʻp uchragan harflar — chastota tahlili boshlanishi\n',
      javaCode: J('    public static void main(String[] args) {\n        String cipher = "Khoor Zruog";\n        for (int k = 1; k < 26; k++) {           // Sezar shifrini toʻliq tanlash bilan buzish\n            StringBuilder s = new StringBuilder();\n            for (char c : cipher.toCharArray())\n                s.append(Character.isLetter(c) ? (char) ((Character.isUpperCase(c) ? \'A\' : \'a\') + Math.floorMod(Character.toLowerCase(c) - \'a\' - k, 26)) : c);\n            if (k == 3) System.out.println(k + ": " + s); // Hello World\n        }\n    }'),
      mistake: 'algoritmni sir saqlash xavfsizlik beradi deb oʻylash (security through obscurity).',
      practice: 'Sezar shifrida nechta mumkin boʻlgan kalit bor? Nega bu uni zaif qiladi?',
    }),
    M6: topic({
      intro: '<p><strong>Simmetrik kriptografiyada</strong> shifrlash va deshifrlash uchun bitta kalit ishlatiladi. <strong>Oqimli shifrlar</strong> (masalan, GSM’dagi A5/1) kalit oqimini ochiq matn bilan bitma-bit XOR qiladi. <strong>Blokli shifrlar</strong> (masalan, AES) matnni qatʼiy uzunlikdagi bloklarga boʻlib shifrlaydi.</p>',
      points: [
        'AES: blok 128 bit, kalit 128/192/256 bit, 10/12/14 raund.',
        'Blokli shifrlash rejimlari: ECB (tavsiya etilmaydi), CBC, CTR, GCM (autentifikatsiyali).',
        'Asosiy muammo — kalitni xavfsiz tarqatish; uni ochiq kalitli kriptografiya hal qiladi.',
      ],
      pyCode: '# Oqimli shifr gʻoyasi (faqat oʻquv maqsadida, xavfsiz EMAS):\nimport random\n\nmessage = b"AIFU"\nrng = random.Random(2026)             # umumiy kalit = urugʻ (seed)\nkeystream = bytes(rng.randrange(256) for _ in message)\ncipher = bytes(m ^ k for m, k in zip(message, keystream))\nplain = bytes(c ^ k for c, k in zip(cipher, keystream))\nprint(cipher.hex(), plain)\n',
      pyNote: 'Python standart kutubxonasida AES yoʻq; amalda cryptography kabi tekshirilgan kutubxonalar ishlatiladi. random moduli kriptografik emas.',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        KeyGenerator gen = KeyGenerator.getInstance("AES");\n        gen.init(256);\n        SecretKey key = gen.generateKey();\n        byte[] iv = new byte[12];\n        new SecureRandom().nextBytes(iv);\n\n        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");\n        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));\n        byte[] encrypted = cipher.doFinal("Salom, AIFU".getBytes(StandardCharsets.UTF_8));\n\n        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));\n        System.out.println(new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8));\n    }', 'import java.nio.charset.StandardCharsets;\nimport java.security.SecureRandom;\nimport javax.crypto.*;\nimport javax.crypto.spec.GCMParameterSpec;'),
      javaNote: 'AES-GCM shifrlaydi va yaxlitlikni ham tekshiradi. Bitta kalit bilan IV (nonce) hech qachon takrorlanmasligi kerak.',
      mistake: 'bir xil kalit oqimini (yoki GCM’da bir xil IV ni) ikki xabar uchun ishlatish — ikkala xabarni XOR qilib, kalitsiz maʼlumot olish mumkin.',
      practice: 'Nega ECB rejimida shifrlangan rasm konturlari koʻrinib qoladi?',
    }),
    M7: topic({
      intro: '<p><strong>Ochiq kalitli (asimmetrik) kriptografiyada</strong> har bir foydalanuvchida juft kalit bor: <strong>ochiq kalit</strong> hammaga beriladi, <strong>yopiq kalit</strong> sir saqlanadi. Ochiq kalit bilan shifrlangan xabarni faqat yopiq kalit egasi ocha oladi. <strong>RSA</strong> katta sonni tub koʻpaytuvchilarga ajratish qiyinligiga asoslanadi.</p>',
      points: [
        'Kalit yaratish: n = p·q, φ = (p−1)(q−1), e tanlanadi (odatda 65537), d = e⁻¹ mod φ.',
        'Shifrlash c = mᵉ mod n; deshifrlash m = c^d mod n.',
        'Afzalligi: kalitni oldindan xavfsiz almashish shart emas; raqamli imzo imkoni.',
        'Muammolari: simmetrik shifrlardan ancha sekin, kalitlar uzun (2048+ bit); shuning uchun amalda gibrid sxema qoʻllanadi.',
      ],
      pyCode: 'p, q, e = 61, 53, 17\nn, phi = p * q, (p - 1) * (q - 1)\nd = pow(e, -1, phi)          # modul boʻyicha teskari element (Python 3.8+)\nm = 65\nc = pow(m, e, n)\nprint("ochiq kalit:", (e, n), "yopiq kalit:", (d, n))\nprint("shifrmatn:", c, "deshifr:", pow(c, d, n))\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");\n        gen.initialize(2048);\n        KeyPair pair = gen.generateKeyPair();\n        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");\n        rsa.init(Cipher.ENCRYPT_MODE, pair.getPublic());\n        byte[] c = rsa.doFinal("maxfiy".getBytes());\n        rsa.init(Cipher.DECRYPT_MODE, pair.getPrivate());\n        System.out.println(new String(rsa.doFinal(c)));\n    }', 'import java.security.*;\nimport javax.crypto.Cipher;'),
      mistake: 'RSA ni toʻldirishsiz (“darslik RSA”) ishlatish — bir xil xabar doim bir xil shifrmatn beradi va hujumlarga ochiq.',
      practice: 'p = 5, q = 11, e = 3 boʻlsa, d ni toping. (φ = 40, d = 27.)',
    }),
    M8: topic({
      intro: '<p><strong>Xesh-funksiya</strong> ixtiyoriy maʼlumotdan qatʼiy uzunlikdagi qiymat hosil qiladi va teskarisiga hisoblab boʻlmaydi. <strong>MAC</strong> (masalan, HMAC) xeshga maxfiy kalit qoʻshib, xabar kimdan kelganini ham tasdiqlaydi. <strong>Elektron raqamli imzo (ERI)</strong> xabar xeshini yopiq kalit bilan imzolaydi; <strong>PKI</strong> va <strong>X.509</strong> sertifikatlari ochiq kalit kimga tegishli ekanini kafolatlaydi.</p>',
      points: [
        'Xossalar: bir tomonlama, kolliziyaga bardoshli, koʻchki effekti.',
        'MD5 va SHA-1 eskirgan (kolliziyalar topilgan); SHA-256, SHA-3 tavsiya etiladi.',
        'Parollar oddiy xesh bilan emas, tuz (salt) va sekin funksiyalar (bcrypt, scrypt, Argon2, PBKDF2) bilan saqlanadi.',
        'ERI: yaxlitlik + autentiklik + rad eta olmaslik.',
      ],
      pyCode: 'import hashlib\nimport hmac\n\nprint(hashlib.sha256(b"AIFU").hexdigest())\nprint(hashlib.sha256(b"AIFu").hexdigest())   # bitta harf — butunlay boshqa xesh\n\ntag = hmac.new(b"maxfiy-kalit", b"baho=85", hashlib.sha256).hexdigest()\nprint("HMAC:", tag)\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        KeyPair pair = KeyPairGenerator.getInstance("EC").generateKeyPair();\n        Signature signer = Signature.getInstance("SHA256withECDSA");\n        signer.initSign(pair.getPrivate());\n        signer.update("Buyruq No 15".getBytes());\n        byte[] sig = signer.sign();\n\n        Signature verifier = Signature.getInstance("SHA256withECDSA");\n        verifier.initVerify(pair.getPublic());\n        verifier.update("Buyruq No 15".getBytes());\n        System.out.println("Imzo haqiqiy: " + verifier.verify(sig));\n    }', 'import java.security.*;'),
      mistake: 'xeshlashni shifrlash deb atash. Xeshdan asl maʼlumotni tiklab boʻlmaydi; shifrmatnni esa kalit bilan tiklash mumkin.',
      practice: 'Nega ERI “rad eta olmaslik”ni taʼminlaydi, HMAC esa yoʻq?',
    }),
    M9: topic({
      intro: '<p><strong>Kriptografik protokollar</strong> shifrlash, xesh va imzolarni birlashtirib, amaliy xavfsizlik vazifalarini hal qiladi. <strong>TLS</strong> veb (HTTPS) va pochtani, <strong>IPSec</strong> tarmoq pogʻonasidagi trafikni (VPN), <strong>SSH</strong> masofaviy boshqaruvni himoyalaydi. <strong>Blokcheyn</strong> xesh-zanjir va imzolar yordamida oʻzgartirib boʻlmaydigan reyestr yaratadi.</p>',
      points: [
        'TLS qoʻl siqish: sertifikatni tekshirish → kalit kelishuvi (ECDHE) → simmetrik sessiya kaliti.',
        'Oldinga maxfiylik (forward secrecy): yopiq kalit keyinchalik oshkor boʻlsa ham, eski sessiyalar ochilmaydi.',
        'E-hukumat va elektron toʻlovlarda ERI va TLS birgalikda ishlatiladi.',
      ],
      pyCode: 'import hashlib\n\n# Soddalashtirilgan blokcheyn: har bir blok oldingisining xeshini saqlaydi\nchain = []\nprev = "0" * 64\nfor data in ["A→B: 10", "B→C: 4", "C→A: 1"]:\n    h = hashlib.sha256((prev + data).encode()).hexdigest()\n    chain.append((data, prev[:8], h[:8]))\n    prev = h\nfor block in chain:\n    print(block)\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        // Java TLS orqali sertifikat zanjirini tekshiradi; notoʻgʻri sertifikatda istisno beradi.\n        HttpClient client = HttpClient.newHttpClient();\n        HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.python.org/")).build();\n        HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());\n        System.out.println(res.version() + " " + res.statusCode());\n    }', 'import java.net.URI;\nimport java.net.http.*;'),
      javaNote: 'Bu misol internet talab qiladi, platforma sandboksida tarmoq oʻchirilgan — uni oʻz kompyuteringizda ishga tushiring.',
      mistake: 'dasturda sertifikat tekshiruvini “vaqtincha” oʻchirib qoʻyish — oʻrtadagi odam hujumiga yoʻl ochadi.',
      practice: 'Blokcheyndagi ikkinchi blok maʼlumoti oʻzgartirilsa, qaysi xeshlar oʻzgaradi?',
      links: [LINKS.mdnHttp],
    }),
    M10: topic({
      intro: '<p><strong>Identifikatsiya</strong> — “men kimman” deb daʼvo qilish (login). <strong>Autentifikatsiya</strong> — daʼvoni isbotlash. <strong>Avtorizatsiya</strong> — tasdiqlangan foydalanuvchiga nima qilish mumkinligini aniqlash. Autentifikatsiya omillari: bilim (parol), egalik (telefon, token), biometrik xususiyat (barmoq izi, yuz).</p>',
      points: [
        '<strong>Koʻp omilli autentifikatsiya (MFA)</strong> — kamida ikki xil turdagi omil.',
        'Biometriya xatolari: FAR (begonani qabul qilish) va FRR (egasini rad etish).',
        'Parollar serverda tuzlangan sekin xesh sifatida saqlanadi.',
        'Bir martalik parollar (TOTP) vaqt va umumiy sir asosida hosil qilinadi.',
      ],
      pyCode: 'import hashlib\nimport os\n\n# Parolni saqlash: tuz + PBKDF2 (standart kutubxonada mavjud)\nsalt = os.urandom(16)\nstored = hashlib.pbkdf2_hmac("sha256", b"T0shkent#Bahor", salt, 200_000)\n\nattempt = hashlib.pbkdf2_hmac("sha256", b"T0shkent#Bahor", salt, 200_000)\nprint("Parol toʻgʻri:", attempt == stored)\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        byte[] salt = new byte[16];\n        new SecureRandom().nextBytes(salt);\n        PBEKeySpec spec = new PBEKeySpec("T0shkent#Bahor".toCharArray(), salt, 200_000, 256);\n        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();\n        System.out.println(HexFormat.of().formatHex(hash));\n    }', 'import java.security.SecureRandom;\nimport java.util.HexFormat;\nimport javax.crypto.SecretKeyFactory;\nimport javax.crypto.spec.PBEKeySpec;'),
      mistake: 'parol va xavfsizlik savoli — ikki omil deb hisoblash. Ikkalasi ham “bilim” omili, bu MFA emas.',
      practice: 'Bank kartasi + PIN kod qaysi omillardan iborat?',
    }),
    M11: topic({
      intro: '<p><strong>Foydalanishni boshqarish</strong> subyekt (foydalanuvchi, jarayon) obyektga (fayl, yozuv) qanday amallarni bajarishi mumkinligini belgilaydi. Asosiy modellar: <strong>DAC</strong>, <strong>MAC</strong>, <strong>RBAC</strong>, <strong>ABAC</strong>.</p>',
      points: [
        '<strong>DAC</strong> (ixtiyoriy): obyekt egasi huquqlarni oʻzi beradi (masalan, Linux fayl huquqlari).',
        '<strong>MAC</strong> (majburiy): maxfiylik belgilari (grif) va ruxsat darajalari tizim tomonidan qatʼiy qoʻllanadi.',
        '<strong>RBAC</strong> (rolga asoslangan): huquqlar rollarga, rollar foydalanuvchilarga beriladi — bu platformada TALABA, OʻQITUVCHI, ADMIN rollari.',
        '<strong>ABAC</strong>: qaror atributlar (boʻlim, vaqt, qurilma) asosida qabul qilinadi.',
        'Foydalanish matritsasi: qatorlar — subyektlar, ustunlar — obyektlar; ustun boʻyicha saqlansa — ACL.',
      ],
      pyCode: 'ROLE_PERMISSIONS = {\n    "STUDENT": {"lesson:read", "submission:create"},\n    "TEACHER": {"lesson:read", "lesson:edit", "submission:grade"},\n    "ADMIN": {"user:manage"},\n}\nuser_roles = {"student3": ["STUDENT"], "a.zoxidov": ["TEACHER"]}\n\n\ndef can(user, permission):\n    return any(permission in ROLE_PERMISSIONS[r] for r in user_roles.get(user, []))\n\n\nprint(can("student3", "lesson:edit"))   # False\nprint(can("a.zoxidov", "lesson:edit"))  # True\n',
      javaCode: J('    enum Role { STUDENT, TEACHER, ADMIN }\n\n    static final Map<Role, Set<String>> PERMS = Map.of(\n        Role.STUDENT, Set.of("lesson:read", "submission:create"),\n        Role.TEACHER, Set.of("lesson:read", "lesson:edit", "submission:grade"),\n        Role.ADMIN, Set.of("user:manage"));\n\n    public static void main(String[] args) {\n        Set<Role> roles = EnumSet.of(Role.TEACHER);\n        boolean allowed = roles.stream().anyMatch(r -> PERMS.get(r).contains("lesson:edit"));\n        System.out.println(allowed);\n    }'),
      mistake: 'foydalanuvchiga huquqlarni toʻgʻridan-toʻgʻri berish. Xodim lavozimi oʻzgarganda eski huquqlar qolib ketadi; rollar orqali boshqarish osonroq.',
      practice: 'Harbiy tizimdagi “Mutlaqo maxfiy” hujjatlarga kirish qaysi modelga mos keladi?',
    }),
    M12: topic({
      intro: '<p><strong>Fizik xavfsizlik</strong> — binolar, uskunalar va maʼlumot tashuvchilarni fizik taʼsirdan himoyalash. Eng yaxshi shifrlash ham server oʻgʻirlansa yoki yongʻinda yoʻq boʻlsa yordam bermaydi.</p>',
      points: [
        'Tabiiy tahdidlar: yongʻin, suv toshqini, zilzila, haddan tashqari issiqlik.',
        'Sunʼiy tahdidlar: oʻgʻirlik, vandalizm, ruxsatsiz kirish, elektr taʼminoti uzilishi.',
        'Nazorat choralari: perimetr, kirish kartalari, videokuzatuv, qoʻriqlash, UPS va generator, yongʻin oʻchirish tizimi.',
        'Axborotni yoʻq qilish: qogʻozni maydalash, disklarni qayta yozish (overwriting), magnitsizlantirish, fizik parchalash.',
      ],
      mistake: 'eski kompyuterlarni disklarini tozalamay sotish yoki berib yuborish.',
      practice: 'Universitet server xonasi uchun beshta fizik himoya chorasini sanang.',
    }),
    M13: topic({
      intro: '<p>Tarmoq xavfsizligini tushunish uchun <strong>OSI</strong> (7 pogʻona) va <strong>TCP/IP</strong> (4 pogʻona) modellarini bilish kerak: har bir pogʻonaning oʻz zaifliklari va hujumlari bor. <strong>DoS/DDoS</strong> hujumlari xizmatni soʻrovlar bilan “toʻldirib”, foydalanish mumkinligini buzadi.</p>',
      points: [
        'Kanal pogʻonasi: MAC spoofing, ARP spoofing.',
        'Tarmoq pogʻonasi: IP spoofing, ICMP flood.',
        'Transport pogʻonasi: SYN flood, port skanerlash.',
        'Amaliy pogʻona: SQL inyeksiya, XSS, fishing saytlar.',
      ],
      pyCode: '# Oddiy “rate limiting”: bir IP dan 1 soniyada 5 tadan ortiq soʻrovni rad etish\nfrom collections import defaultdict, deque\n\nWINDOW, LIMIT = 1.0, 5\nhistory = defaultdict(deque)\n\n\ndef allow(ip, now):\n    q = history[ip]\n    while q and now - q[0] > WINDOW:\n        q.popleft()\n    if len(q) >= LIMIT:\n        return False\n    q.append(now)\n    return True\n\n\nprint([allow("10.0.0.7", t / 10) for t in range(8)])\n',
      javaCode: J('    public static void main(String[] args) {\n        String[] layers = {"Fizik", "Kanal", "Tarmoq", "Transport", "Seans", "Taqdimot", "Amaliy"};\n        String[] attacks = {"Kabelni uzish", "ARP spoofing", "IP spoofing", "SYN flood", "Sessiyani egallash", "SSL stripping", "SQL inyeksiya"};\n        for (int i = 0; i < layers.length; i++) System.out.println((i + 1) + ". " + layers[i] + ": " + attacks[i]);\n    }'),
      mistake: 'faqat perimetrni himoyalash. Ichki tarmoqqa kirib olgan hujumchi ham toʻxtatilishi kerak (segmentatsiya, zero trust).',
      practice: 'SYN flood hujumi TCP qoʻl siqishining qaysi bosqichidan foydalanadi?',
      links: [LINKS.owaspTop10],
    }),
    M14: topic({
      intro: '<p>Tarmoq himoya vositalari chuqur himoya tamoyili boʻyicha qatlam-qatlam joylashtiriladi.</p>',
      points: [
        '<strong>Firewall</strong> — qoidalar asosida paketlarni oʻtkazadi yoki bloklaydi (paket filtri, holatni kuzatuvchi, amaliy pogʻona / WAF).',
        '<strong>VPN</strong> — ochiq tarmoq orqali shifrlangan tunnel (IPSec, WireGuard, OpenVPN).',
        '<strong>IDS/IPS</strong> — hujumlarni imzo yoki anomaliya boʻyicha aniqlash (IDS) va toʻxtatish (IPS).',
        '<strong>DLP</strong> — maxfiy maʼlumot tashkilotdan chiqib ketishining oldini olish.',
        '<strong>Honeypot</strong> — hujumchilarni jalb qilib, ularning usullarini oʻrganish uchun “tuzoq” tizim.',
      ],
      pyCode: 'RULES = [  # (amal, protokol, port) — birinchi mos kelgan qoida qoʻllanadi\n    ("allow", "tcp", 443),\n    ("allow", "tcp", 22),\n    ("deny", "any", None),\n]\n\n\ndef decide(proto, port):\n    for action, p, prt in RULES:\n        if p in (proto, "any") and prt in (port, None):\n            return action\n    return "deny"\n\n\nprint(decide("tcp", 443), decide("udp", 53))\n',
      javaCode: J('    record Rule(String action, String proto, Integer port) {}\n\n    public static void main(String[] args) {\n        List<Rule> rules = List.of(new Rule("allow", "tcp", 443), new Rule("allow", "tcp", 22), new Rule("deny", "any", null));\n        String proto = "tcp";\n        int port = 23;\n        for (Rule r : rules)\n            if ((r.proto().equals("any") || r.proto().equals(proto)) && (r.port() == null || r.port() == port)) {\n                System.out.println(r.action()); // deny\n                break;\n            }\n    }'),
      mistake: 'firewall qoidalari oxirida “hammasini taqiqlash” (default deny) qoidasini qoʻymaslik.',
      practice: 'IDS va IPS farqini bitta gapda tushuntiring.',
    }),
    M15: topic({
      intro: '<p><strong>Bulutli hisoblash</strong> resurslarni (server, xotira, dastur) internet orqali xizmat sifatida taqdim etadi: IaaS, PaaS, SaaS. Xavfsizlik <strong>umumiy javobgarlik modeli</strong> asosida taqsimlanadi. <strong>IoT</strong> (Buyumlar interneti) — sensorlar va “aqlli” qurilmalar tarmogʻi; ular koʻpincha kam resursli va kam himoyalangan boʻladi.</p>',
      points: [
        'Bulut tahdidlari: notoʻgʻri sozlangan saqlash joylari, oʻgʻirlangan kirish kalitlari, xavfsiz boʻlmagan API.',
        'Maxfiylik: maʼlumot qaysi mamlakatda saqlanishi, shifrlash va kalitlarni kim boshqarishi.',
        'IoT arxitekturasi: qurilma → shlyuz → bulut → ilova.',
        'IoT tahdidlari: standart parollar, yangilanmaydigan proshivka, shifrlanmagan aloqa, botnetlar.',
      ],
      mistake: 'bulutga koʻchirish bilan xavfsizlik avtomatik taʼminlanadi deb oʻylash.',
      practice: 'SaaS xizmatida (masalan, onlayn pochta) mijoz nimalar uchun javobgar?',
    }),
    A1: practical({
      goal: '<p>Axborot xavfsizligi asosiy tushunchalarini test va keys-stadi orqali mustahkamlash.</p>',
      steps: ['Maʼruza materialini takrorlang (M1).', 'Keys: “Talaba LMS paroli ijtimoiy tarmoqda oshkor boʻldi.” Qaysi xossalar xavf ostida? Qanday nazorat choralari kerak?', 'Quyidagi testni topshiring.'],
      refs: [quiz('KIA-Q-A1')],
      check: 'Tahdid, zaiflik va risk orasidagi farqni oʻz misolingizda tushuntiring.',
    }),
    A2: practical({
      goal: '<p>Risk matritsasini qurish: risklarni ehtimollik va taʼsir boʻyicha baholab, ustuvorlik tartibini aniqlash.</p>',
      steps: ['Tanlangan tashkilot uchun kamida 8 ta risk roʻyxatini tuzing.', 'Har biriga 1–5 shkala boʻyicha ehtimollik va taʼsir bering.', '5×5 matritsani chizing va risklarni joylashtiring.', 'Hisoblashni avtomatlashtiruvchi dasturni yozing (topshiriq quyida).'],
      refs: [task('KIA-A2')],
      check: 'Ehtimolligi past, lekin taʼsiri juda yuqori risk (masalan, zilzila) bilan qanday ishlash kerak?',
    }),
    A3: practical({
      goal: '<p>Real kiberjinoyat voqealarini tahlil qilish: hujum usuli, zaiflik, oqibat va saboqlar.</p>',
      steps: ['Ochiq manbalardan bitta mashhur hodisani tanlang (masalan, WannaCry, 2017).', 'Hujum zanjirini tasvirlang: kirish nuqtasi → tarqalish → zarar.', 'Qaysi nazorat choralari hodisani oldini olishi mumkin edi?', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A3')],
    }),
    A4: practical({
      goal: '<p>Oddiy tashkilot (masalan, 20 xodimli oʻquv markazi) uchun xavfsizlik siyosati loyihasini ishlab chiqish.</p>',
      steps: ['Maqsad va qamrov boʻlimini yozing.', 'Rollar va javobgarliklarni belgilang.', 'Parol, qurilmalardan foydalanish, zaxira nusxalash va hodisalarga javob boʻlimlarini yozing.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A4')],
    }),
    A5: practical({
      goal: '<p>Klassik shifrlash usullarini dasturda amalga oshirish va ularning zaifligini tushunish.</p>',
      steps: ['Sezar shifrini yozing va toʻliq tanlash (brute force) bilan buzib koʻring.', 'Vijiner shifrini yozing; kalit takrorlanishi qanday zaiflik tugʻdirishini muhokama qiling.'],
      refs: [task('KIA-A5C'), task('KIA-A5V')],
      check: 'Vijiner shifri nega oddiy chastota tahliliga Sezar shifriga qaraganda bardoshliroq?',
    }),
    A6: practical({
      goal: '<p>Simmetrik algoritm (AES) bilan shifrlash dasturida ishlash.</p>',
      steps: ['M6 maʼruzasidagi Java misolini oʻz kompyuteringizda ishga tushiring.', 'Bitta kalit bilan ikkita turli xabarni shifrlang; shifrmatnlarni solishtiring.', 'Shifrmatnning bitta baytini oʻzgartirib, GCM deshifrlashda nima boʻlishini kuzating.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A6')],
    }),
    A7: practical({
      goal: '<p>RSA algoritmini oddiy misol orqali qoʻllash.</p>',
      steps: ['Kichik tub sonlar bilan kalitlarni qoʻlda hisoblang (p = 61, q = 53, e = 17).', 'Kengaytirilgan Evklid algoritmi bilan d ni toping.', 'Dastur yordamida natijani tekshiring (topshiriq quyida).'],
      refs: [task('KIA-A7')],
      check: 'Nima uchun n ni tub koʻpaytuvchilarga ajratish RSA ni buzadi?',
    }),
    A8: practical({
      goal: '<p>Xesh funksiyalar yordamida fayl va xabarlar yaxlitligini tekshirish; ERI gʻoyasi.</p>',
      steps: ['Terminalda fayl xeshini hisoblang: Linux’da <code>sha256sum fayl</code>, Windows’da <code>certutil -hashfile fayl SHA256</code>.', 'Faylga bitta belgi qoʻshib, xeshni qayta hisoblang.', 'Yaxlitlikni tekshiruvchi dasturni yozing (topshiriq quyida).', 'M8 dagi ECDSA misoli bilan imzo yaratib, tekshirib koʻring.'],
      refs: [task('KIA-A8')],
    }),
    A9: practical({
      goal: '<p>Brauzerda SSL/TLS sertifikatini tekshirish.</p>',
      steps: ['Istalgan HTTPS saytda manzil satridagi qulf belgisini bosing va sertifikatni oching.', 'Egasi (subject), beruvchi (issuer), amal qilish muddati va zanjirni yozib oling.', 'Sertifikat zanjiri ildiz sertifikatgacha qanday tekshirilishini tushuntiring.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A9')],
    }),
    A10: practical({
      goal: '<p>Windows OTda foydalanuvchi autentifikatsiyasi va parol siyosatini sozlash.</p>',
      steps: ['Oʻquv kompyuterida (yoki virtual mashinada) <code>secpol.msc</code> → Account Policies → Password Policy boʻlimini oching.', 'Minimal uzunlik, murakkablik va hisobni bloklash chegarasini sozlang.', 'Siyosatni tekshiruvchi dasturni yozing (topshiriq quyida).'],
      refs: [task('KIA-A10')],
      check: 'Parolni tez-tez majburiy almashtirish har doim foydalimi? Nega?',
    }),
    A11: practical({
      goal: '<p>Linux OTda fayl va katalog huquqlarini boshqarish.</p>',
      steps: ['<code>ls -l</code> bilan huquqlarni koʻring.', '<code>chmod 640 fayl</code>, <code>chmod u+x skript.sh</code>, <code>chown</code> buyruqlarini sinab koʻring.', 'Huquq satrini sakkizlik kodga aylantiruvchi dasturni yozing (topshiriq quyida).'],
      refs: [task('KIA-A11')],
      check: 'Katalog uchun x (bajarish) huquqi nimani anglatadi?',
    }),
    A12: practical({
      goal: '<p>Fizik xavfsizlik boʻyicha real misollarni tahlil qilish.</p>',
      steps: ['Universitet binosi yoki oʻquv laboratoriyasi misolida fizik tahdidlar roʻyxatini tuzing.', 'Har bir tahdid uchun profilaktik, aniqlovchi va tiklovchi chorani taklif qiling.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A12')],
    }),
    A13: practical({
      goal: '<p>Tarmoq trafikini monitoring qilish (Wireshark yoki shunga oʻxshash vosita).</p>',
      steps: ['Faqat oʻzingizning yoki oʻquv tarmogʻingizda trafik yozib oling.', 'dns, http, tcp.flags.syn==1 kabi filtrlarni qoʻllang.', 'Bitta TCP qoʻl siqishini (SYN, SYN-ACK, ACK) toping va vaqtlarini yozing.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A13')],
    }),
    A14: practical({
      goal: '<p>VPN ulash va trafikni himoyalash.</p>',
      steps: ['Oʻquv muhitida VPN ulanishini sozlang (masalan, WireGuard yoki OpenVPN).', 'Ulanishdan oldin va keyin tashqi IP manzilingizni solishtiring.', 'Snifferda VPN trafigi qanday koʻrinishini kuzating.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A14')],
    }),
    A15: practical({
      goal: '<p>IoT qurilmalari xavfsizlik zaifliklarini oʻrganish (keys yoki taqdimot).</p>',
      steps: ['Bitta IoT qurilmasini tanlang (IP-kamera, aqlli rozetka, router).', 'Uning hujum yuzasini tavsiflang: interfeyslar, parollar, yangilanishlar.', 'Himoya tavsiyalarini tayyorlang.', 'Testni topshiring.'],
      refs: [quiz('KIA-Q-A15')],
    }),
    MT1: independent({
      goal: '<p>Maʼruza va amaliy mashgʻulotlar mavzularini oʻzlashtirish.</p>',
      tasks: ['Har bir maʼruzadan keyin asosiy atamalar lugʻatini yuriting (oʻzbekcha va inglizcha).', 'Har bir modul yakunida bir sahifalik xulosa yozing.'],
      deliverable: '<p>Atamalar lugʻati va modul xulosalari.</p>',
    }),
    MT2: independent({
      goal: '<p>Mustaqil ish va uyga vazifalarni bajarish.</p>',
      tasks: ['Amaliy mashgʻulotlardagi barcha kodlash topshiriqlarini platformada topshiring.', 'Oʻqituvchi bergan uy vazifalarini belgilangan muddatda bajaring.'],
    }),
    MT3: independent({
      goal: '<p>Kriptografik usullar; Enigma shifrlash mashinasi va uning bardoshliligi.</p>',
      tasks: ['Enigma rotorlari, reflektor va kommutatsiya paneli qanday ishlashini tushuntiring.', 'Nega harf hech qachon oʻziga shifrlanmasligi kriptotahlilchilarga yordam berganini yozing.', 'Enigma kalitlari sonini taxminan baholang.'],
      deliverable: '<p>3–4 sahifalik referat yoki 8–10 slaydli taqdimot.</p>',
    }),
    MT4: independent({
      goal: '<p>Foydalanish mumkinligini taʼminlashda antivirus, IDS, IPS vositalarining oʻrni; zararli dasturlar klassifikatsiyasi.</p>',
      tasks: ['Antivirus, IDS va IPS ni vazifasi, joylashuvi va aniqlash usuli boʻyicha jadvalda solishtiring.', 'Zararli dasturlarni turlari boʻyicha klassifikatsiya qiling (virus, qurt, troyan, ransomware, spyware, rootkit).'],
    }),
    MT5: independent({
      goal: '<p>AKT xavfsizligiga tahdidlar; zararkunanda dasturlarning turlari.</p>',
      tasks: ['Soʻnggi yillardagi uchta yirik zararli dastur hodisasini tasvirlang.', 'Har biri qaysi zaiflikdan foydalanganini va qanday himoya choralari mavjudligini yozing.'],
    }),
    MT6: independent({
      goal: '<p>Kompyuter viruslari va himoya; xorijiy ERI algoritmlari; identifikatsiya va autentifikatsiya.</p>',
      tasks: ['RSA, DSA, ECDSA va EdDSA imzo algoritmlarini kalit uzunligi va tezligi boʻyicha solishtiring.', 'Identifikatsiya, autentifikatsiya va avtorizatsiya farqini misollar bilan tushuntiring.'],
    }),
    MT7: independent({
      goal: '<p>Ruxsatsiz foydalanishlarni aniqlash uslubiyati; simsiz tarmoqlarda axborotni himoyalash.</p>',
      tasks: ['Imzo va anomaliyaga asoslangan aniqlash usullarini solishtiring.', 'WEP, WPA2 va WPA3 himoya protokollarini taqqoslang; uy routerini xavfsiz sozlash boʻyicha yoʻriqnoma yozing.'],
    }),
    MT8: independent({
      goal: '<p>Axborotni ruxsatsiz foydalanishlardan himoyalash; ijtimoiy injeneriya hujumlari tahlili.</p>',
      tasks: ['Fishing, pretexting, baiting va tailgating hujumlariga bittadan misol keltiring.', 'Xodimlar uchun “Fishing xatini qanday tanish mumkin” eslatmasini tayyorlang.'],
    }),
    MT9: independent({
      goal: '<p>Zamonaviy antivirus vositalari; kiberjinoyat va kiberhuquq.</p>',
      tasks: ['Ikki antivirus vositasining imkoniyatlarini (real vaqt himoyasi, xulq-atvor tahlili, bulutli tekshiruv) solishtiring.', 'Kiberjinoyat uchun javobgarlik belgilangan milliy qonunchilik manbalarini rasmiy saytdan toping va qisqacha bayon qiling.'],
    }),
    MT10: independent({
      goal: '<p>Tarmoqda uzatiladigan axborotni himoyalashda kriptografiyaning oʻrni; zararli dasturlardan himoyalanish.</p>',
      tasks: ['HTTPS, SSH va VPN da qaysi kriptografik primitivlar ishlatilishini jadvalda koʻrsating.', 'Zararli dasturni aniqlash va oʻchirish vositasi bilan ishlash tartibini (skanerlash, karantin, hisobot) tasvirlang.'],
    }),
    MT11: independent({
      goal: '<p>Kiberjinoyatchilik keng tarqalishining sabablari; yuz tasviriga asoslangan autentifikatsiya.</p>',
      tasks: ['Kiberjinoyatlar koʻpayishining iqtisodiy, texnik va ijtimoiy sabablarini tahlil qiling.', 'Yuz tanish tizimlarining afzalliklari, xatolari (FAR/FRR) va maxfiylik muammolarini yozing.'],
    }),
    MT12: independent({
      goal: '<p>Barmoq iziga asoslangan autentifikatsiya; elektron raqamli imzo.</p>',
      tasks: ['Barmoq izi skanerlari turlari (optik, sigʻimli, ultratovushli) va ularni aldash usullarini solishtiring.', 'ERI yaratish va tekshirish jarayonini sxema koʻrinishida chizing.'],
    }),
    MT13: independent({
      goal: '<p>Kiberxavfsizlik sohasida karyera yoʻllari.</p>',
      tasks: ['SOC tahlilchi, pentester, xavfsizlik muhandisi va GRC mutaxassisi vazifalarini solishtiring.', 'Tanlagan yoʻnalishingiz uchun bir yillik oʻquv rejasini tuzing.'],
    }),
    MT14: independent({
      goal: '<p>Mashhur sertifikatlar; parollarni boshqarish tizimlari.</p>',
      tasks: ['Boshlangʻich va professional darajadagi xavfsizlik sertifikatlarini (masalan, CompTIA Security+, CISSP) talablari boʻyicha solishtiring.', 'Parol menejeri qanday ishlashini (asosiy parol, shifrlangan ombor) tushuntiring va xatarlarini yozing.'],
    }),
    MT15: independent({
      goal: '<p>Virtual himoyalangan tarmoq (VPN) va undan amaliyotda foydalanish.</p>',
      tasks: ['Tijoriy VPN xizmatlari va korporativ VPN farqini tushuntiring.', 'VPN tanlashda nimalarga eʼtibor berish kerak: loglar siyosati, protokollar, yurisdiksiya.'],
    }),
    MT16: independent({
      goal: '<p>Sotsial injeneriya; antivirus vositasidan foydalanish.</p>',
      tasks: ['Oʻquv kompyuterida antivirus bilan toʻliq skanerlash oʻtkazing va hisobotni tahlil qiling.', 'Ijtimoiy injeneriya hujumiga duch kelganda harakatlar tartibini yozing.'],
    }),
    MT17: independent({
      goal: '<p>Windows OTda zaxira nusxalash; imtiyoz turlari va fayllarga kirishni boshqarish.</p>',
      tasks: ['Windows’da zaxira nusxa yarating va bitta faylni tiklab koʻring.', '3-2-1 zaxira nusxalash qoidasini tushuntiring.', 'NTFS huquqlari (Read, Write, Modify, Full control) va ularning meros qilib olinishini tushuntiring.'],
    }),
  },
}
