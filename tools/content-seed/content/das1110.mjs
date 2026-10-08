// DAS1110 Dasturlash — practice for semester 1 (whose lectures V905 already fills) and full
// material for semester 2. Every task ships a Python and a Java solution.
import { LINKS, independent, practical, task, tip, topic } from './_blocks.mjs'

const J = (body, imports = 'import java.util.*;') => `${imports}\n\npublic class Main {\n${body}\n}\n`

// ============================================================================ semester 1 tasks
const s1Tasks = [
  {
    code: 'DAS-S1-A1',
    title: 'Birinchi dastur: salomlashish',
    description: '<p>Foydalanuvchi ismini oʻqib, unga salom beruvchi dastur yozing.</p>',
    instructions: 'Kirish: bitta satrda ism (boʻsh joy boʻlishi mumkin). Chiqish: Salom, <ism>!',
    constraints: 'Ism uzunligi 1 dan 100 gacha.',
    python: {
      starter: 'name = input().strip()\n# ...\n',
      solution: 'name = input().strip()\nprint(f"Salom, {name}!")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        String name = in.nextLine().trim();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        String name = in.nextLine().trim();\n        System.out.println("Salom, " + name + "!");\n    }'),
    },
    tests: [['Ali', 'Salom, Ali!'], ['Madina', 'Salom, Madina!'], ['Ozoda Karimova', 'Salom, Ozoda Karimova!']],
  },
  {
    code: 'DAS-S1-A2',
    title: 'Arifmetik amallar',
    description: '<p>Ikkita butun son uchun yigʻindi, ayirma, koʻpaytma, butun boʻlinma va qoldiqni hisoblang.</p>',
    instructions: 'Kirish: bitta satrda a va b. Chiqish: beshta satrda a+b, a-b, a*b, a div b, a mod b.',
    constraints: '0 ≤ a ≤ 10^9, 1 ≤ b ≤ 10^9. Koʻpaytma 32 bitga sigʻmasligi mumkin: Java’da long ishlating.',
    python: {
      starter: 'a, b = map(int, input().split())\n# ...\n',
      solution: 'a, b = map(int, input().split())\nprint(a + b)\nprint(a - b)\nprint(a * b)\nprint(a // b)\nprint(a % b)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        System.out.println(a + b);\n        System.out.println(a - b);\n        System.out.println(a * b);\n        System.out.println(a / b);\n        System.out.println(a % b);\n    }'),
    },
    tests: [
      ['17 5', '22\n12\n85\n3\n2'],
      ['7 9', '16\n-2\n63\n0\n7'],
      ['100 10', '110\n90\n1000\n10\n0'],
      ['1000000000 3', '1000000003\n999999997\n3000000000\n333333333\n1'],
    ],
  },
  {
    code: 'DAS-S1-A3',
    title: 'Kabisa yili',
    description: '<p>Yil kabisami yoki yoʻqligini mantiqiy amallar yordamida aniqlang. Yil 4 ga boʻlinsa kabisa, lekin 100 ga boʻlinib 400 ga boʻlinmasa, kabisa emas.</p>',
    instructions: 'Kirish: yil. Chiqish: Kabisa yoki Kabisa emas.',
    constraints: '1 ≤ yil ≤ 10000.',
    python: {
      starter: 'y = int(input())\n# ...\n',
      solution: 'y = int(input())\nprint("Kabisa" if (y % 4 == 0 and y % 100 != 0) or y % 400 == 0 else "Kabisa emas")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        int y = new Scanner(System.in).nextInt();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        int y = new Scanner(System.in).nextInt();\n        boolean leap = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0;\n        System.out.println(leap ? "Kabisa" : "Kabisa emas");\n    }'),
    },
    tests: [['2024', 'Kabisa'], ['2023', 'Kabisa emas'], ['1900', 'Kabisa emas'], ['2000', 'Kabisa']],
  },
  {
    code: 'DAS-S1-A4',
    title: 'Kvadrat tenglamaning ildizlari soni',
    description: '<p>ax² + bx + c = 0 tenglamaning nechta haqiqiy ildizi borligini shartli operator bilan aniqlang.</p>',
    instructions: 'Kirish: a b c butun sonlar (a ≠ 0). Chiqish: 0, 1 yoki 2.',
    constraints: '|a|, |b|, |c| ≤ 10^6. Diskriminantni long turida hisoblang.',
    python: {
      starter: 'a, b, c = map(int, input().split())\nd = b * b - 4 * a * c\n# ...\n',
      solution: 'a, b, c = map(int, input().split())\nd = b * b - 4 * a * c\nif d > 0:\n    print(2)\nelif d == 0:\n    print(1)\nelse:\n    print(0)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong(), c = in.nextLong();\n        long d = b * b - 4 * a * c;\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong(), c = in.nextLong();\n        long d = b * b - 4 * a * c;\n        if (d > 0) System.out.println(2);\n        else if (d == 0) System.out.println(1);\n        else System.out.println(0);\n    }'),
    },
    tests: [['1 -3 2', '2'], ['1 2 1', '1'], ['1 0 5', '0'], ['2 4 -6', '2'], ['1000000 1 1000000', '0']],
  },
  {
    code: 'DAS-S1-A5',
    title: 'Hafta kuni (tanlash operatori)',
    description: '<p>1 dan 7 gacha raqam boʻyicha hafta kunining nomini chiqaring. Pythonda <code>match</code>, Java’da <code>switch</code> operatoridan foydalaning.</p>',
    instructions: 'Kirish: butun son n. Chiqish: kun nomi (Dushanba … Yakshanba) yoki Xato.',
    constraints: '-100 ≤ n ≤ 100.',
    python: {
      starter: 'n = int(input())\nmatch n:\n    case 1:\n        print("Dushanba")\n    # ...\n',
      solution: 'n = int(input())\nmatch n:\n    case 1:\n        print("Dushanba")\n    case 2:\n        print("Seshanba")\n    case 3:\n        print("Chorshanba")\n    case 4:\n        print("Payshanba")\n    case 5:\n        print("Juma")\n    case 6:\n        print("Shanba")\n    case 7:\n        print("Yakshanba")\n    case _:\n        print("Xato")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        switch (n) {\n            case 1 -> System.out.println("Dushanba");\n            // ...\n            default -> System.out.println("Xato");\n        }\n    }'),
      solution: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        String day = switch (n) {\n            case 1 -> "Dushanba";\n            case 2 -> "Seshanba";\n            case 3 -> "Chorshanba";\n            case 4 -> "Payshanba";\n            case 5 -> "Juma";\n            case 6 -> "Shanba";\n            case 7 -> "Yakshanba";\n            default -> "Xato";\n        };\n        System.out.println(day);\n    }'),
    },
    tests: [['1', 'Dushanba'], ['5', 'Juma'], ['7', 'Yakshanba'], ['0', 'Xato'], ['9', 'Xato']],
  },
  {
    code: 'DAS-S1-A6',
    title: 'Faktorial (parametrli sikl)',
    description: '<p>n! = 1·2·…·n ni <code>for</code> sikli bilan hisoblang. 0! = 1.</p>',
    instructions: 'Kirish: n. Chiqish: n!.',
    constraints: '0 ≤ n ≤ 20 (natija long turiga sigʻadi).',
    python: {
      starter: 'n = int(input())\nf = 1\n# ...\nprint(f)\n',
      solution: 'n = int(input())\nf = 1\nfor i in range(2, n + 1):\n    f *= i\nprint(f)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        long f = 1;\n        // ...\n        System.out.println(f);\n    }'),
      solution: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        long f = 1;\n        for (int i = 2; i <= n; i++) f *= i;\n        System.out.println(f);\n    }'),
    },
    tests: [['5', '120'], ['0', '1'], ['10', '3628800'], ['20', '2432902008176640000']],
  },
  {
    code: 'DAS-S1-A7',
    title: 'Tub sonni aniqlash',
    description: '<p>Son tub ekanligini <code>while</code> sikli bilan tekshiring: 2 dan √n gacha boʻluvchi izlang.</p>',
    instructions: 'Kirish: n. Chiqish: Tub yoki Tub emas.',
    constraints: '1 ≤ n ≤ 10^9. 1 tub emas.',
    python: {
      starter: 'n = int(input())\nd = 2\n# ...\n',
      solution: 'n = int(input())\nprime = n >= 2\nd = 2\nwhile d * d <= n and prime:\n    if n % d == 0:\n        prime = False\n    d += 1\nprint("Tub" if prime else "Tub emas")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        long n = new Scanner(System.in).nextLong();\n        long d = 2;\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        long n = new Scanner(System.in).nextLong();\n        boolean prime = n >= 2;\n        long d = 2;\n        while (d * d <= n && prime) {\n            if (n % d == 0) prime = false;\n            d++;\n        }\n        System.out.println(prime ? "Tub" : "Tub emas");\n    }'),
    },
    tests: [['97', 'Tub'], ['100', 'Tub emas'], ['1', 'Tub emas'], ['2', 'Tub'], ['999999937', 'Tub']],
  },
  {
    code: 'DAS-S1-A8',
    title: 'Koʻpaytirish jadvali (ichma-ich sikl)',
    description: '<p>n × n koʻpaytirish jadvalini ichma-ich joylashgan sikllar bilan chiqaring.</p>',
    instructions: 'Kirish: n. Chiqish: n ta satr; i-satrda i·1, i·2, …, i·n sonlari bitta boʻsh joy bilan ajratilgan.',
    constraints: '1 ≤ n ≤ 9.',
    python: {
      starter: 'n = int(input())\n# ...\n',
      solution: 'n = int(input())\nfor i in range(1, n + 1):\n    print(" ".join(str(i * j) for j in range(1, n + 1)))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        StringBuilder out = new StringBuilder();\n        for (int i = 1; i <= n; i++) {\n            for (int j = 1; j <= n; j++) {\n                if (j > 1) out.append(\' \');\n                out.append(i * j);\n            }\n            out.append(\'\\n\');\n        }\n        System.out.print(out);\n    }'),
    },
    tests: [['3', '1 2 3\n2 4 6\n3 6 9'], ['1', '1'], ['5', '1 2 3 4 5\n2 4 6 8 10\n3 6 9 12 15\n4 8 12 16 20\n5 10 15 20 25']],
  },
  {
    code: 'DAS-S1-A9B',
    title: 'Massivda chiziqli qidiruv',
    description: '<p>Massivda x qiymatining birinchi uchrashgan oʻrnini (1 dan boshlab) toping. Topilmasa -1 chiqaring.</p>',
    instructions: 'Kirish: n; n ta butun son; x. Chiqish: indeks yoki -1.',
    constraints: '1 ≤ n ≤ 1000, qiymatlar |a| ≤ 10^9.',
    python: {
      starter: 'n = int(input())\na = list(map(int, input().split()))\nx = int(input())\n# ...\n',
      solution: 'n = int(input())\na = list(map(int, input().split()))\nx = int(input())\nans = -1\nfor i in range(n):\n    if a[i] == x:\n        ans = i + 1\n        break\nprint(ans)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        long x = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        long x = in.nextLong();\n        int ans = -1;\n        for (int i = 0; i < n; i++) {\n            if (a[i] == x) { ans = i + 1; break; }\n        }\n        System.out.println(ans);\n    }'),
    },
    tests: [['5\n4 8 15 16 23\n15', '3'], ['3\n1 2 3\n7', '-1'], ['6\n5 1 5 1 5 1\n1', '2'], ['1\n-9\n-9', '1']],
  },
  {
    code: 'DAS-S1-MT1',
    title: 'Boʻlakli funksiya',
    description: '<p>y = x² (x &lt; 0 boʻlsa), y = 2x + 1 (0 ≤ x ≤ 10 boʻlsa), y = x − 10 (x &gt; 10 boʻlsa) funksiyani hisoblang.</p>',
    instructions: 'Kirish: butun x. Chiqish: y.',
    constraints: '|x| ≤ 10^6.',
    python: {
      starter: 'x = int(input())\n# ...\n',
      solution: 'x = int(input())\nif x < 0:\n    print(x * x)\nelif x <= 10:\n    print(2 * x + 1)\nelse:\n    print(x - 10)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        long x = new Scanner(System.in).nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        long x = new Scanner(System.in).nextLong();\n        long y;\n        if (x < 0) y = x * x;\n        else if (x <= 10) y = 2 * x + 1;\n        else y = x - 10;\n        System.out.println(y);\n    }'),
    },
    tests: [['-3', '9'], ['4', '9'], ['0', '1'], ['10', '21'], ['25', '15'], ['-1000000', '1000000000000']],
  },
  {
    code: 'DAS-S1-MT2',
    title: 'Garmonik qator yigʻindisi',
    description: '<p>S = 1 + 1/2 + 1/3 + … + 1/n yigʻindini hisoblang va 4 ta kasr xonasi bilan chiqaring.</p>',
    instructions: 'Kirish: n. Chiqish: S, nuqtadan keyin 4 ta raqam.',
    constraints: '1 ≤ n ≤ 10^6. Java’da String.format(Locale.US, "%.4f", s) ishlating.',
    python: {
      starter: 'n = int(input())\ns = 0.0\n# ...\nprint(f"{s:.4f}")\n',
      solution: 'n = int(input())\ns = 0.0\nfor i in range(1, n + 1):\n    s += 1 / i\nprint(f"{s:.4f}")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        double s = 0;\n        // ...\n        System.out.println(String.format(Locale.US, "%.4f", s));\n    }'),
      solution: J('    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        double s = 0;\n        for (int i = 1; i <= n; i++) s += 1.0 / i;\n        System.out.println(String.format(Locale.US, "%.4f", s));\n    }'),
    },
    tests: [['1', '1.0000'], ['2', '1.5000'], ['10', '2.9290'], ['100', '5.1874']],
  },
  {
    code: 'DAS-S1-MT3',
    title: 'Massivni teskari tartibda chiqarish',
    description: '<p>Massiv elementlarini teskari tartibda chiqaring.</p>',
    instructions: 'Kirish: n; n ta butun son. Chiqish: elementlar teskari tartibda, boʻsh joy bilan.',
    constraints: '1 ≤ n ≤ 10^4.',
    python: {
      starter: 'n = int(input())\na = input().split()\n# ...\n',
      solution: 'n = int(input())\na = input().split()\nprint(" ".join(reversed(a)))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        StringBuilder sb = new StringBuilder();\n        for (int i = n - 1; i >= 0; i--) {\n            sb.append(a[i]);\n            if (i > 0) sb.append(\' \');\n        }\n        System.out.println(sb);\n    }'),
    },
    tests: [['4\n1 2 3 4', '4 3 2 1'], ['1\n7', '7'], ['5\n-1 0 5 -3 8', '8 -3 5 0 -1']],
  },
  {
    code: 'DAS-S1-MT4',
    title: 'Matritsa diagonallari',
    description: '<p>n × n matritsaning bosh va yordamchi diagonallari elementlari yigʻindisini toping.</p>',
    instructions: 'Kirish: n; soʻng n ta satrda n tadan butun son. Chiqish: bitta satrda ikki son — bosh diagonal va yordamchi diagonal yigʻindisi.',
    constraints: '1 ≤ n ≤ 100, |a| ≤ 10^6.',
    python: {
      starter: 'n = int(input())\nm = [list(map(int, input().split())) for _ in range(n)]\n# ...\n',
      solution: 'n = int(input())\nm = [list(map(int, input().split())) for _ in range(n)]\nmain = sum(m[i][i] for i in range(n))\nside = sum(m[i][n - 1 - i] for i in range(n))\nprint(main, side)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[][] m = new long[n][n];\n        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) m[i][j] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[][] m = new long[n][n];\n        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) m[i][j] = in.nextLong();\n        long main = 0, side = 0;\n        for (int i = 0; i < n; i++) {\n            main += m[i][i];\n            side += m[i][n - 1 - i];\n        }\n        System.out.println(main + " " + side);\n    }'),
    },
    tests: [['3\n1 2 3\n4 5 6\n7 8 9', '15 15'], ['1\n42', '42 42'], ['2\n1 2\n3 4', '5 5'], ['3\n1 0 0\n0 2 0\n9 0 3', '6 11']],
  },
  {
    code: 'DAS-S1-MT5',
    title: 'Vektorlarning skalyar koʻpaytmasi',
    description: '<p>Ikki n oʻlchamli vektorning skalyar koʻpaytmasini hisoblang: a₁b₁ + a₂b₂ + … + aₙbₙ.</p>',
    instructions: 'Kirish: n; a vektor (n ta son); b vektor (n ta son). Chiqish: skalyar koʻpaytma.',
    constraints: '1 ≤ n ≤ 10^4, |aᵢ|, |bᵢ| ≤ 10^4.',
    python: {
      starter: 'n = int(input())\na = list(map(int, input().split()))\nb = list(map(int, input().split()))\n# ...\n',
      solution: 'n = int(input())\na = list(map(int, input().split()))\nb = list(map(int, input().split()))\nprint(sum(x * y for x, y in zip(a, b)))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n], b = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        for (int i = 0; i < n; i++) b[i] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n], b = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        for (int i = 0; i < n; i++) b[i] = in.nextLong();\n        long s = 0;\n        for (int i = 0; i < n; i++) s += a[i] * b[i];\n        System.out.println(s);\n    }'),
    },
    tests: [['3\n1 2 3\n4 5 6', '32'], ['2\n1 0\n0 1', '0'], ['4\n-1 2 -3 4\n5 -6 7 -8', '-70']],
  },
  {
    code: 'DAS-S1-MT6',
    title: 'Yigʻindisi k ga teng juftliklar',
    description: '<p>Massivda i &lt; j va aᵢ + aⱼ = k shartni qanoatlantiruvchi juftliklar sonini ichma-ich sikllar bilan toping.</p>',
    instructions: 'Kirish: n va k; n ta butun son. Chiqish: juftliklar soni.',
    constraints: '1 ≤ n ≤ 1000.',
    python: {
      starter: 'n, k = map(int, input().split())\na = list(map(int, input().split()))\n# ...\n',
      solution: 'n, k = map(int, input().split())\na = list(map(int, input().split()))\ncount = 0\nfor i in range(n):\n    for j in range(i + 1, n):\n        if a[i] + a[j] == k:\n            count += 1\nprint(count)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long k = in.nextLong();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long k = in.nextLong();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        long count = 0;\n        for (int i = 0; i < n; i++)\n            for (int j = i + 1; j < n; j++)\n                if (a[i] + a[j] == k) count++;\n        System.out.println(count);\n    }'),
    },
    tests: [['5 6\n1 5 2 4 3', '2'], ['4 2\n1 1 1 1', '6'], ['3 100\n1 2 3', '0']],
  },
]

// Java for the V902 demo task (it shipped with Python and Go only).
const sumJava = {
  code: 'DEV-TASK-SUM',
  // V902's test cases, used only by verify-solutions.mjs.
  tests: [['3\n1 2 3', '6'], ['1\n42', '42'], ['4\n-5 5 -3 3', '0'], ['5\n1000000 1000000 1000000 1000000 1000000', '5000000']],
  starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        // ...\n    }'),
  solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long s = 0;\n        for (int i = 0; i < n; i++) s += in.nextLong();\n        System.out.println(s);\n    }'),
}

const refLesson = (...refs) => ({ append: true, blocks: [tip('<p>Quyidagi topshiriqni Python yoki Java tilida yeching. Yechimingiz avtomatik testlarda tekshiriladi.</p>'), ...refs.map(task)] })

const semester1 = {
  subject: 'DAS1110',
  cohort: 'DI-26-01-S1',
  tasks: s1Tasks,
  addJavaTo: [sumJava],
  lessons: {
    A1: refLesson('DAS-S1-A1'),
    A2: refLesson('DAS-S1-A2'),
    A3: refLesson('DAS-S1-A3'),
    A4: refLesson('DAS-S1-A4'),
    A5: refLesson('DAS-S1-A5'),
    A6: refLesson('DAS-S1-A6'),
    A7: refLesson('DAS-S1-A7'),
    A8: refLesson('DAS-S1-A8'),
    A9: refLesson('DEV-TASK-SUM', 'DAS-S1-A9B'),
    MT1: refLesson('DAS-S1-MT1'),
    MT2: refLesson('DAS-S1-MT2'),
    MT3: refLesson('DAS-S1-MT3'),
    MT4: refLesson('DAS-S1-MT4'),
    MT5: refLesson('DAS-S1-MT5'),
    MT6: refLesson('DAS-S1-MT6'),
  },
}

// ============================================================================ semester 2 tasks
const s2Tasks = [
  {
    code: 'DAS-S2-A1',
    title: 'Matritsa satrlarini saralash',
    description: '<p>n × m matritsaning har bir satrini oʻsish tartibida saralang.</p>',
    instructions: 'Kirish: n m; soʻng n ta satrda m tadan butun son. Chiqish: saralangan matritsa, har satr bitta boʻsh joy bilan.',
    constraints: '1 ≤ n, m ≤ 100.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'n, m = map(int, input().split())\nrows = [list(map(int, input().split())) for _ in range(n)]\n# ...\n',
      solution: 'n, m = map(int, input().split())\nfor _ in range(n):\n    row = sorted(map(int, input().split()))\n    print(" ".join(map(str, row)))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt(), m = in.nextInt();\n        long[][] a = new long[n][m];\n        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) a[i][j] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt(), m = in.nextInt();\n        StringBuilder out = new StringBuilder();\n        for (int i = 0; i < n; i++) {\n            long[] row = new long[m];\n            for (int j = 0; j < m; j++) row[j] = in.nextLong();\n            Arrays.sort(row);\n            for (int j = 0; j < m; j++) {\n                if (j > 0) out.append(\' \');\n                out.append(row[j]);\n            }\n            out.append(\'\\n\');\n        }\n        System.out.print(out);\n    }'),
    },
    tests: [['2 3\n3 1 2\n9 7 8', '1 2 3\n7 8 9'], ['1 1\n5', '5'], ['3 2\n2 -1\n0 0\n10 -10', '-1 2\n0 0\n-10 10']],
  },
  {
    code: 'DAS-S2-A2',
    title: 'Binar qidiruv',
    description: '<p>Oʻsish tartibida saralangan, takrorlanmaydigan massivda har bir soʻrov uchun binar qidiruv bilan oʻrin toping.</p>',
    instructions: 'Kirish: n; n ta son; q; soʻng q ta soʻrov x. Chiqish: har bir soʻrov uchun alohida satrda indeks (1 dan) yoki -1.',
    constraints: '1 ≤ n ≤ 10^5, 1 ≤ q ≤ 10^5. Chiziqli qidiruv vaqt limitidan oshadi.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\ndata = sys.stdin.read().split()\n# ...\n',
      solution: 'import sys\ndata = sys.stdin.read().split()\nn = int(data[0])\na = list(map(int, data[1:1 + n]))\nq = int(data[1 + n])\nout = []\nfor x in map(int, data[2 + n:2 + n + q]):\n    lo, hi, ans = 0, n - 1, -1\n    while lo <= hi:\n        mid = (lo + hi) // 2\n        if a[mid] == x:\n            ans = mid + 1\n            break\n        if a[mid] < x:\n            lo = mid + 1\n        else:\n            hi = mid - 1\n    out.append(str(ans))\nprint("\\n".join(out))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));\n        // ...\n    }', 'import java.io.*;\nimport java.util.*;'),
      solution: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        StringTokenizer st = new StringTokenizer(br.readLine());\n        int n = Integer.parseInt(st.nextToken());\n        long[] a = new long[n];\n        st = new StringTokenizer(br.readLine());\n        for (int i = 0; i < n; i++) a[i] = Long.parseLong(st.nextToken());\n        int q = Integer.parseInt(br.readLine().trim());\n        StringBuilder out = new StringBuilder();\n        for (int k = 0; k < q; k++) {\n            long x = Long.parseLong(br.readLine().trim());\n            int lo = 0, hi = n - 1, ans = -1;\n            while (lo <= hi) {\n                int mid = (lo + hi) >>> 1;\n                if (a[mid] == x) { ans = mid + 1; break; }\n                if (a[mid] < x) lo = mid + 1; else hi = mid - 1;\n            }\n            out.append(ans).append(\'\\n\');\n        }\n        System.out.print(out);\n    }', 'import java.io.*;\nimport java.util.*;'),
    },
    tests: [
      ['5\n1 3 5 7 9\n3\n7\n2\n1', '4\n-1\n1'],
      ['1\n10\n2\n10\n11', '1\n-1'],
      ['6\n-10 -5 0 5 10 15\n4\n15\n-10\n3\n0', '6\n1\n-1\n3'],
    ],
  },
  {
    code: 'DAS-S2-A3',
    title: 'EKUB va EKUK funksiyalari',
    description: '<p>Ikkita funksiya yozing: <code>gcd(a, b)</code> Evklid algoritmi bilan va <code>lcm(a, b) = a / gcd(a, b) · b</code>. Asosiy dastur ulardan foydalanadi.</p>',
    instructions: 'Kirish: a b. Chiqish: bitta satrda EKUB va EKUK.',
    constraints: '1 ≤ a, b ≤ 10^9 (EKUK long turiga sigʻadi).',
    python: {
      starter: 'def gcd(a, b):\n    # ...\n    pass\n\n\ndef lcm(a, b):\n    # ...\n    pass\n\n\na, b = map(int, input().split())\nprint(gcd(a, b), lcm(a, b))\n',
      solution: 'def gcd(a, b):\n    while b:\n        a, b = b, a % b\n    return a\n\n\ndef lcm(a, b):\n    return a // gcd(a, b) * b\n\n\na, b = map(int, input().split())\nprint(gcd(a, b), lcm(a, b))\n',
    },
    java: {
      starter: J('    static long gcd(long a, long b) {\n        // ...\n        return 0;\n    }\n\n    static long lcm(long a, long b) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        System.out.println(gcd(a, b) + " " + lcm(a, b));\n    }'),
      solution: J('    static long gcd(long a, long b) {\n        while (b != 0) {\n            long t = a % b;\n            a = b;\n            b = t;\n        }\n        return a;\n    }\n\n    static long lcm(long a, long b) {\n        return a / gcd(a, b) * b;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        System.out.println(gcd(a, b) + " " + lcm(a, b));\n    }'),
    },
    tests: [['12 18', '6 36'], ['7 13', '1 91'], ['1000000000 999999999', '1 999999999000000000'], ['24 24', '24 24']],
  },
  {
    code: 'DAS-S2-A4',
    title: 'Rekursiv tez darajaga koʻtarish',
    description: '<p>aⁿ mod m ni rekursiv funksiya bilan hisoblang: n juft boʻlsa aⁿ = (a^(n/2))², toq boʻlsa aⁿ = a · aⁿ⁻¹. Bu O(log n) qadam talab qiladi.</p>',
    instructions: 'Kirish: a n m. Chiqish: aⁿ mod m.',
    constraints: '1 ≤ a ≤ 10^9, 0 ≤ n ≤ 10^18, 1 ≤ m ≤ 10^9.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'def power(a, n, m):\n    # ...\n    pass\n\n\na, n, m = map(int, input().split())\nprint(power(a, n, m))\n',
      solution: 'def power(a, n, m):\n    if n == 0:\n        return 1 % m\n    half = power(a, n // 2, m)\n    result = half * half % m\n    if n % 2 == 1:\n        result = result * (a % m) % m\n    return result\n\n\na, n, m = map(int, input().split())\nprint(power(a, n, m))\n',
    },
    java: {
      starter: J('    static long power(long a, long n, long m) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), n = in.nextLong(), m = in.nextLong();\n        System.out.println(power(a, n, m));\n    }'),
      solution: J('    static long power(long a, long n, long m) {\n        if (n == 0) return 1 % m;\n        long half = power(a, n / 2, m);\n        long result = half * half % m;\n        if (n % 2 == 1) result = result * (a % m) % m;\n        return result;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), n = in.nextLong(), m = in.nextLong();\n        System.out.println(power(a, n, m));\n    }'),
    },
    tests: [['2 10 1000', '24'], ['3 0 7', '1'], ['5 3 1', '0'], ['2 1000000000000000000 1000000007', '719476260'], ['123456789 987654321 998244353', '730701112']],
  },
  {
    code: 'DAS-S2-A5',
    title: 'Oʻrtachadan katta elementlar (dinamik massiv)',
    description: '<p>Oʻlchami ishga tushganda maʼlum boʻladigan massiv (Python roʻyxati, Java’da <code>new long[n]</code> yoki <code>ArrayList</code>) yarating va oʻrta arifmetikdan katta elementlarni kiritilgan tartibda chiqaring.</p>',
    instructions: 'Kirish: n; n ta butun son. Chiqish: oʻrtachadan qatʼiy katta elementlar boʻsh joy bilan; bunday element boʻlmasa -.',
    constraints: '1 ≤ n ≤ 10^5. Kasr sonlardan qochish uchun aᵢ · n &gt; yigʻindi shartini tekshiring.',
    python: {
      starter: 'n = int(input())\na = list(map(int, input().split()))\n# ...\n',
      solution: 'n = int(input())\na = list(map(int, input().split()))\ntotal = sum(a)\nbig = [x for x in a if x * n > total]\nprint(" ".join(map(str, big)) if big else "-")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        long total = 0;\n        for (int i = 0; i < n; i++) { a[i] = in.nextLong(); total += a[i]; }\n        List<String> big = new ArrayList<>();\n        for (long x : a) if (x * n > total) big.add(String.valueOf(x));\n        System.out.println(big.isEmpty() ? "-" : String.join(" ", big));\n    }'),
    },
    tests: [['5\n1 2 3 4 5', '4 5'], ['3\n7 7 7', '-'], ['4\n-5 10 0 -1', '10']],
  },
  {
    code: 'DAS-S2-A6',
    title: 'Belgilarning ASCII kodlari',
    description: '<p>Satrdagi har bir belgining ASCII kodini chiqaring (boʻsh joy ham belgi: 32).</p>',
    instructions: 'Kirish: bitta satr (faqat ASCII belgilar). Chiqish: kodlar boʻsh joy bilan.',
    constraints: 'Satr uzunligi 1 dan 200 gacha.',
    python: {
      starter: 's = input()\n# ...\n',
      solution: 's = input()\nprint(" ".join(str(ord(ch)) for ch in s))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        StringBuilder sb = new StringBuilder();\n        for (int i = 0; i < s.length(); i++) {\n            if (i > 0) sb.append(\' \');\n            sb.append((int) s.charAt(i));\n        }\n        System.out.println(sb);\n    }'),
    },
    tests: [['Salom', '83 97 108 111 109'], ['A1 b', '65 49 32 98'], ['~!', '126 33']],
  },
  {
    code: 'DAS-S2-A7',
    title: 'Palindrom satr',
    description: '<p>Satr palindrommi yoki yoʻqligini aniqlang: faqat harf va raqamlar hisobga olinadi, katta-kichik harf farqlanmaydi.</p>',
    instructions: 'Kirish: bitta satr (ASCII). Chiqish: Palindrom yoki Palindrom emas.',
    constraints: 'Satr uzunligi 1 dan 1000 gacha.',
    python: {
      starter: 's = input()\n# ...\n',
      solution: 's = input()\nclean = [ch.lower() for ch in s if ch.isalnum()]\nprint("Palindrom" if clean == clean[::-1] else "Palindrom emas")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        StringBuilder clean = new StringBuilder();\n        for (char ch : s.toCharArray()) if (Character.isLetterOrDigit(ch)) clean.append(Character.toLowerCase(ch));\n        String c = clean.toString();\n        System.out.println(c.equals(new StringBuilder(c).reverse().toString()) ? "Palindrom" : "Palindrom emas");\n    }'),
    },
    tests: [['Anna', 'Palindrom'], ['Kitob', 'Palindrom emas'], ['A roza upala na lapu Azora', 'Palindrom'], ['12-21', 'Palindrom']],
  },
  {
    code: 'DAS-S2-A8',
    title: 'Matn statistikasi (oqim bilan ishlash)',
    description: '<p>Standart kirish oqimini fayl kabi oxirigacha oʻqing va satrlar, soʻzlar hamda belgilar sonini hisoblang (satr oxiri belgilari hisobga olinmaydi).</p>',
    instructions: 'Kirish: bir yoki bir nechta satr matn. Chiqish: bitta satrda uchta son — satrlar, soʻzlar, belgilar.',
    constraints: 'Matn hajmi 100 KB gacha.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\nlines = sys.stdin.read().splitlines()\n# ...\n',
      solution: 'import sys\nlines = sys.stdin.read().splitlines()\nwords = sum(len(line.split()) for line in lines)\nchars = sum(len(line) for line in lines)\nprint(len(lines), words, chars)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String line;\n        // ...\n    }', 'import java.io.*;'),
      solution: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String line;\n        long lines = 0, words = 0, chars = 0;\n        while ((line = br.readLine()) != null) {\n            lines++;\n            chars += line.length();\n            String t = line.trim();\n            if (!t.isEmpty()) words += t.split("\\\\s+").length;\n        }\n        System.out.println(lines + " " + words + " " + chars);\n    }', 'import java.io.*;'),
    },
    tests: [['Salom dunyo\nBu test', '2 4 18'], ['bir', '1 1 3'], ['  ikki   soʻz  \n\nuch', '3 3 18', { mode: 'TRIMMED' }]],
  },
  {
    code: 'DAS-S2-MT1',
    title: 'Massivning maksimumi va oʻrni (funksiya parametri)',
    description: '<p>Massivni parametr sifatida qabul qilib, eng katta element va uning birinchi oʻrnini qaytaruvchi funksiya yozing.</p>',
    instructions: 'Kirish: n; n ta son. Chiqish: maksimum va uning oʻrni (1 dan).',
    constraints: '1 ≤ n ≤ 10^5.',
    python: {
      starter: 'def find_max(a):\n    # ...\n    pass\n\n\nn = int(input())\na = list(map(int, input().split()))\nvalue, index = find_max(a)\nprint(value, index)\n',
      solution: 'def find_max(a):\n    best = 0\n    for i in range(1, len(a)):\n        if a[i] > a[best]:\n            best = i\n    return a[best], best + 1\n\n\nn = int(input())\na = list(map(int, input().split()))\nvalue, index = find_max(a)\nprint(value, index)\n',
    },
    java: {
      starter: J('    static int findMax(long[] a) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        int i = findMax(a);\n        System.out.println(a[i] + " " + (i + 1));\n    }'),
      solution: J('    static int findMax(long[] a) {\n        int best = 0;\n        for (int i = 1; i < a.length; i++) if (a[i] > a[best]) best = i;\n        return best;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        int i = findMax(a);\n        System.out.println(a[i] + " " + (i + 1));\n    }'),
    },
    tests: [['5\n3 9 2 9 1', '9 2'], ['1\n-4', '-4 1'], ['4\n-1 -2 -3 -4', '-1 1']],
  },
  {
    code: 'DAS-S2-MT2',
    title: 'Matritsalarni koʻpaytirish',
    description: '<p>A (n × m) va B (m × p) matritsalarni qabul qilib, C = A · B ni qaytaruvchi funksiya yozing.</p>',
    instructions: 'Kirish: n m p; A ning n ta satri; B ning m ta satri. Chiqish: C ning n ta satri.',
    constraints: '1 ≤ n, m, p ≤ 50, |aᵢⱼ|, |bᵢⱼ| ≤ 1000.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'def multiply(a, b):\n    # ...\n    pass\n\n\nn, m, p = map(int, input().split())\na = [list(map(int, input().split())) for _ in range(n)]\nb = [list(map(int, input().split())) for _ in range(m)]\nfor row in multiply(a, b):\n    print(" ".join(map(str, row)))\n',
      solution: 'def multiply(a, b):\n    n, m, p = len(a), len(b), len(b[0])\n    return [[sum(a[i][k] * b[k][j] for k in range(m)) for j in range(p)] for i in range(n)]\n\n\nn, m, p = map(int, input().split())\na = [list(map(int, input().split())) for _ in range(n)]\nb = [list(map(int, input().split())) for _ in range(m)]\nfor row in multiply(a, b):\n    print(" ".join(map(str, row)))\n',
    },
    java: {
      starter: J('    static long[][] multiply(long[][] a, long[][] b) {\n        // ...\n        return new long[0][0];\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt(), m = in.nextInt(), p = in.nextInt();\n        long[][] a = new long[n][m], b = new long[m][p];\n        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) a[i][j] = in.nextLong();\n        for (int i = 0; i < m; i++) for (int j = 0; j < p; j++) b[i][j] = in.nextLong();\n        // ...\n    }'),
      solution: J('    static long[][] multiply(long[][] a, long[][] b) {\n        int n = a.length, m = b.length, p = b[0].length;\n        long[][] c = new long[n][p];\n        for (int i = 0; i < n; i++)\n            for (int j = 0; j < p; j++)\n                for (int k = 0; k < m; k++) c[i][j] += a[i][k] * b[k][j];\n        return c;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt(), m = in.nextInt(), p = in.nextInt();\n        long[][] a = new long[n][m], b = new long[m][p];\n        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) a[i][j] = in.nextLong();\n        for (int i = 0; i < m; i++) for (int j = 0; j < p; j++) b[i][j] = in.nextLong();\n        StringBuilder out = new StringBuilder();\n        for (long[] row : multiply(a, b)) {\n            for (int j = 0; j < row.length; j++) {\n                if (j > 0) out.append(\' \');\n                out.append(row[j]);\n            }\n            out.append(\'\\n\');\n        }\n        System.out.print(out);\n    }'),
    },
    tests: [['2 2 2\n1 2\n3 4\n5 6\n7 8', '19 22\n43 50'], ['1 3 1\n1 2 3\n4\n5\n6', '32'], ['2 1 3\n1\n-2\n3 0 1', '3 0 1\n-6 0 -2']],
  },
  {
    code: 'DAS-S2-MT3',
    title: 'Ikki saralangan massivni birlashtirish',
    description: '<p>Ikkita oʻsish tartibidagi massivni bitta saralangan massivga birlashtiring (merge). Tayyor saralash funksiyasidan foydalanmang: ikki koʻrsatkichli usul O(n + m) ishlaydi.</p>',
    instructions: 'Kirish: n; a massiv; m; b massiv. Chiqish: birlashtirilgan massiv.',
    constraints: '1 ≤ n, m ≤ 10^5.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'n = int(input())\na = list(map(int, input().split()))\nm = int(input())\nb = list(map(int, input().split()))\n# ...\n',
      solution: 'n = int(input())\na = list(map(int, input().split()))\nm = int(input())\nb = list(map(int, input().split()))\ni = j = 0\nres = []\nwhile i < n and j < m:\n    if a[i] <= b[j]:\n        res.append(a[i])\n        i += 1\n    else:\n        res.append(b[j])\n        j += 1\nres.extend(a[i:])\nres.extend(b[j:])\nprint(" ".join(map(str, res)))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        int m = in.nextInt();\n        long[] b = new long[m];\n        for (int i = 0; i < m; i++) b[i] = in.nextLong();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        int m = in.nextInt();\n        long[] b = new long[m];\n        for (int i = 0; i < m; i++) b[i] = in.nextLong();\n        StringBuilder out = new StringBuilder();\n        int i = 0, j = 0;\n        while (i < n || j < m) {\n            long next = (j >= m || (i < n && a[i] <= b[j])) ? a[i++] : b[j++];\n            if (out.length() > 0) out.append(\' \');\n            out.append(next);\n        }\n        System.out.println(out);\n    }'),
    },
    tests: [['3\n1 4 7\n4\n2 3 8 9', '1 2 3 4 7 8 9'], ['1\n5\n1\n5', '5 5'], ['2\n-3 10\n3\n-5 0 20', '-5 -3 0 10 20']],
  },
  {
    code: 'DAS-S2-MT4',
    title: 'Xanoy minoralari (rekursiya)',
    description: '<p>n ta diskni 1-qoziqdan 3-qoziqqa oʻtkazing: bir martada bitta disk, katta disk kichigining ustiga qoʻyilmaydi. Rekursiya: n−1 diskni yordamchi qoziqqa, eng kattasini maqsadga, soʻng n−1 diskni uning ustiga.</p>',
    instructions: 'Kirish: n. Chiqish: birinchi satrda harakatlar soni, soʻng har bir harakat alohida satrda: qayerdan qayerga.',
    constraints: '1 ≤ n ≤ 10.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'def hanoi(n, src, dst, via, moves):\n    # ...\n    pass\n\n\nn = int(input())\nmoves = []\nhanoi(n, 1, 3, 2, moves)\nprint(len(moves))\nfor a, b in moves:\n    print(a, b)\n',
      solution: 'def hanoi(n, src, dst, via, moves):\n    if n == 0:\n        return\n    hanoi(n - 1, src, via, dst, moves)\n    moves.append((src, dst))\n    hanoi(n - 1, via, dst, src, moves)\n\n\nn = int(input())\nmoves = []\nhanoi(n, 1, 3, 2, moves)\nprint(len(moves))\nfor a, b in moves:\n    print(a, b)\n',
    },
    java: {
      starter: J('    static void hanoi(int n, int src, int dst, int via, List<int[]> moves) {\n        // ...\n    }\n\n    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        List<int[]> moves = new ArrayList<>();\n        hanoi(n, 1, 3, 2, moves);\n        StringBuilder out = new StringBuilder().append(moves.size()).append(\'\\n\');\n        for (int[] mv : moves) out.append(mv[0]).append(\' \').append(mv[1]).append(\'\\n\');\n        System.out.print(out);\n    }'),
      solution: J('    static void hanoi(int n, int src, int dst, int via, List<int[]> moves) {\n        if (n == 0) return;\n        hanoi(n - 1, src, via, dst, moves);\n        moves.add(new int[] {src, dst});\n        hanoi(n - 1, via, dst, src, moves);\n    }\n\n    public static void main(String[] args) {\n        int n = new Scanner(System.in).nextInt();\n        List<int[]> moves = new ArrayList<>();\n        hanoi(n, 1, 3, 2, moves);\n        StringBuilder out = new StringBuilder().append(moves.size()).append(\'\\n\');\n        for (int[] mv : moves) out.append(mv[0]).append(\' \').append(mv[1]).append(\'\\n\');\n        System.out.print(out);\n    }'),
    },
    tests: [['1', '1\n1 3'], ['2', '3\n1 2\n1 3\n2 3'], ['3', '7\n1 3\n1 2\n3 2\n1 3\n2 1\n2 3\n1 3']],
  },
  {
    code: 'DAS-S2-MT5',
    title: 'Akkerman funksiyasi (ichma-ich rekursiya)',
    description: '<p>A(0, n) = n + 1; A(m, 0) = A(m − 1, 1); A(m, n) = A(m − 1, A(m, n − 1)). Chaqiruv ichida yana chaqiruv — ichma-ich rekursiyaning klassik misoli.</p>',
    instructions: 'Kirish: m n. Chiqish: A(m, n).',
    constraints: '0 ≤ m ≤ 3, 0 ≤ n ≤ 5. Pythonda sys.setrecursionlimit(10000) qoʻying.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\nsys.setrecursionlimit(10000)\n\n\ndef ack(m, n):\n    # ...\n    pass\n\n\nm, n = map(int, input().split())\nprint(ack(m, n))\n',
      solution: 'import sys\nsys.setrecursionlimit(10000)\n\n\ndef ack(m, n):\n    if m == 0:\n        return n + 1\n    if n == 0:\n        return ack(m - 1, 1)\n    return ack(m - 1, ack(m, n - 1))\n\n\nm, n = map(int, input().split())\nprint(ack(m, n))\n',
    },
    java: {
      starter: J('    static long ack(long m, long n) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long m = in.nextLong(), n = in.nextLong();\n        System.out.println(ack(m, n));\n    }'),
      solution: J('    static long ack(long m, long n) {\n        if (m == 0) return n + 1;\n        if (n == 0) return ack(m - 1, 1);\n        return ack(m - 1, ack(m, n - 1));\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long m = in.nextLong(), n = in.nextLong();\n        System.out.println(ack(m, n));\n    }'),
    },
    tests: [['0 0', '1'], ['1 2', '4'], ['2 3', '9'], ['3 3', '61'], ['3 5', '253']],
  },
  {
    code: 'DAS-S2-MT6',
    title: 'Soʻzlarni bosh harf bilan yozish',
    description: '<p>Satrdagi har bir soʻzni bosh harf bilan boshlang, qolgan harflarni kichik qiling. Soʻzlar orasidagi ortiqcha boʻsh joylarni bitta qilib qoldiring.</p>',
    instructions: 'Kirish: bitta satr (ASCII harflar va boʻsh joylar). Chiqish: formatlangan satr.',
    constraints: 'Satr uzunligi 1 dan 1000 gacha, kamida bitta soʻz bor.',
    python: {
      starter: 's = input()\n# ...\n',
      solution: 's = input()\nprint(" ".join(w[0].upper() + w[1:].lower() for w in s.split()))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine();\n        String[] words = s.trim().split("\\\\s+");\n        StringBuilder sb = new StringBuilder();\n        for (String w : words) {\n            if (sb.length() > 0) sb.append(\' \');\n            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());\n        }\n        System.out.println(sb);\n    }'),
    },
    tests: [['salom dunyo', 'Salom Dunyo'], ['  aNIQ   va IJTIMOIY fanlar ', 'Aniq Va Ijtimoiy Fanlar'], ['x', 'X']],
  },
  {
    code: 'DAS-S2-MT7',
    title: 'Bank hisobi sinfi',
    description: '<p><code>BankAccount</code> sinfini yarating: balans maydoni yopiq (private), <code>deposit</code>, <code>withdraw</code> va <code>balance</code> metodlari orqali boshqariladi. Mablagʻ yetarli boʻlmasa, yechish rad etiladi.</p>',
    instructions: 'Kirish: n; soʻng n ta buyruq: deposit X, withdraw X yoki balance. Chiqish: har bir balance buyrugʻi uchun joriy balans; rad etilgan yechish uchun Mablag yetarli emas.',
    constraints: '1 ≤ n ≤ 1000, 1 ≤ X ≤ 10^9. Boshlangʻich balans 0.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'class BankAccount:\n    def __init__(self):\n        self._balance = 0\n\n    # ...\n\n\nacc = BankAccount()\nfor _ in range(int(input())):\n    parts = input().split()\n    # ...\n',
      solution: 'class BankAccount:\n    def __init__(self):\n        self._balance = 0\n\n    def deposit(self, amount):\n        self._balance += amount\n\n    def withdraw(self, amount):\n        if amount > self._balance:\n            return False\n        self._balance -= amount\n        return True\n\n    def balance(self):\n        return self._balance\n\n\nacc = BankAccount()\nfor _ in range(int(input())):\n    parts = input().split()\n    if parts[0] == "deposit":\n        acc.deposit(int(parts[1]))\n    elif parts[0] == "withdraw":\n        if not acc.withdraw(int(parts[1])):\n            print("Mablag yetarli emas")\n    else:\n        print(acc.balance())\n',
    },
    java: {
      starter: J('    static class BankAccount {\n        private long balance = 0;\n        // ...\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = Integer.parseInt(in.nextLine().trim());\n        BankAccount acc = new BankAccount();\n        for (int i = 0; i < n; i++) {\n            String[] parts = in.nextLine().trim().split(" ");\n            // ...\n        }\n    }'),
      solution: J('    static class BankAccount {\n        private long balance = 0;\n\n        void deposit(long amount) { balance += amount; }\n\n        boolean withdraw(long amount) {\n            if (amount > balance) return false;\n            balance -= amount;\n            return true;\n        }\n\n        long balance() { return balance; }\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = Integer.parseInt(in.nextLine().trim());\n        BankAccount acc = new BankAccount();\n        StringBuilder out = new StringBuilder();\n        for (int i = 0; i < n; i++) {\n            String[] parts = in.nextLine().trim().split(" ");\n            switch (parts[0]) {\n                case "deposit" -> acc.deposit(Long.parseLong(parts[1]));\n                case "withdraw" -> { if (!acc.withdraw(Long.parseLong(parts[1]))) out.append("Mablag yetarli emas\\n"); }\n                default -> out.append(acc.balance()).append(\'\\n\');\n            }\n        }\n        System.out.print(out);\n    }'),
    },
    tests: [
      ['5\ndeposit 100\nwithdraw 30\nbalance\nwithdraw 100\nbalance', '70\nMablag yetarli emas\n70'],
      ['2\nbalance\nwithdraw 1', '0\nMablag yetarli emas'],
      ['4\ndeposit 1000000000\ndeposit 1000000000\nwithdraw 1\nbalance', '1999999999'],
    ],
  },
]

// ============================================================================ semester 2 lessons
const PY_JAVA = 'Kodni Python va Java’da solishtiring: gʻoya bir xil, sintaksis farq qiladi.'

const semester2 = {
  subject: 'DAS1110',
  cohort: 'DI-26-01-S2',
  tasks: s2Tasks,
  lessons: {
    M1: topic({
      intro: '<p>Saralash — maʼlumotlarni kalit boʻyicha tartiblash. Saralangan massivda qidirish (binar qidiruv), dublikatlarni topish va hisobot tuzish osonlashadi. Oddiy saralashlar (pufakcha, tanlash, qoʻyish) O(n²) qadam talab qiladi, samaralilari (tez saralash, birlashtirish, piramidal) — oʻrtacha O(n log n).</p>',
      points: [
        '<strong>Pufakcha (bubble)</strong>: qoʻshni elementlar juft-juft solishtirilib, kattasi oxirga “suzib chiqadi”.',
        '<strong>Tanlash (selection)</strong>: har qadamda qolgan qismning eng kichigi topilib, oʻz joyiga qoʻyiladi.',
        '<strong>Qoʻyish (insertion)</strong>: har bir element oldingi saralangan qismdagi joyiga kiritiladi; deyarli saralangan maʼlumotda tez.',
        '<strong>Barqarorlik</strong>: teng kalitli elementlarning dastlabki tartibi saqlanadimi.',
        'Amalda tilning tayyor funksiyasi ishlatiladi: Python <code>sorted()</code>, Java <code>Arrays.sort()</code>.',
      ],
      pyCode: 'def insertion_sort(a):\n    for i in range(1, len(a)):\n        key = a[i]\n        j = i - 1\n        while j >= 0 and a[j] > key:\n            a[j + 1] = a[j]\n            j -= 1\n        a[j + 1] = key\n    return a\n\n\nprint(insertion_sort([5, 2, 9, 1, 5]))  # [1, 2, 5, 5, 9]\n',
      javaCode: J('    static void insertionSort(int[] a) {\n        for (int i = 1; i < a.length; i++) {\n            int key = a[i], j = i - 1;\n            while (j >= 0 && a[j] > key) { a[j + 1] = a[j]; j--; }\n            a[j + 1] = key;\n        }\n    }\n\n    public static void main(String[] args) {\n        int[] a = {5, 2, 9, 1, 5};\n        insertionSort(a);\n        System.out.println(Arrays.toString(a)); // [1, 2, 5, 5, 9]\n    }'),
      pyNote: PY_JAVA,
      mistake: 'ichki siklda chegarani <code>j &gt;= 0</code> tekshirmasdan <code>a[j]</code> ga murojaat qilish — massiv chegarasidan chiqib ketadi.',
      practice: '[4, 3, 2, 1] massivni qoʻyish usuli bilan saralashda nechta surish (almashtirish) bajariladi? (Javob: 6.)',
      links: [LINKS.visualgo],
    }),
    M2: topic({
      intro: '<p>Obyektga yoʻnaltirilgan dasturlash (OYD) dasturni oʻzaro taʼsir qiluvchi <strong>obyektlar</strong> sifatida quradi. <strong>Sinf</strong> — shablon (qanday maydon va metodlar boʻladi), <strong>obyekt</strong> — shu sinfning aniq nusxasi. Masalan, <code>Talaba</code> sinfi: ism, guruh, ballar; metodlar — oʻrtacha ballni hisoblash.</p>',
      points: [
        '<strong>Maydon (field)</strong> — obyekt holatini saqlaydi.',
        '<strong>Metod</strong> — obyekt bajaradigan amal.',
        '<strong>Konstruktor</strong> — obyekt yaratilganda maydonlarni boshlangʻich qiymat bilan toʻldiradi (Python <code>__init__</code>, Java sinf nomi bilan).',
        '<code>self</code> (Python) va <code>this</code> (Java) — joriy obyektga havola.',
        'OYD ning toʻrt tamoyili: inkapsulyatsiya, vorislik, polimorfizm, abstraksiya.',
      ],
      pyCode: 'class Student:\n    def __init__(self, name, scores):\n        self.name = name\n        self.scores = scores\n\n    def average(self):\n        return sum(self.scores) / len(self.scores)\n\n\ns = Student("Ali", [80, 90, 70])\nprint(s.name, s.average())  # Ali 80.0\n',
      javaCode: J('    static class Student {\n        String name;\n        int[] scores;\n\n        Student(String name, int[] scores) {\n            this.name = name;\n            this.scores = scores;\n        }\n\n        double average() {\n            return Arrays.stream(scores).average().orElse(0);\n        }\n    }\n\n    public static void main(String[] args) {\n        Student s = new Student("Ali", new int[] {80, 90, 70});\n        System.out.println(s.name + " " + s.average()); // Ali 80.0\n    }'),
      mistake: 'Python metodida <code>self</code> parametrini yozmaslik yoki maydonga <code>self.</code>siz murojaat qilish — oʻzgaruvchi obyektga emas, metod ichiga tegishli boʻlib qoladi.',
      practice: '<code>Mahsulot</code> sinfini yozing: nom, narx, miqdor maydonlari va <code>jami()</code> metodi (narx × miqdor).',
      links: [LINKS.javaTutorial],
    }),
    M3: topic({
      intro: '<p>Bir xil turdagi koʻp obyektni massiv yoki roʻyxatda saqlash mumkin: masalan, guruhdagi barcha talabalar. Keyin ular ustida sikl bilan umumiy amallar bajariladi — eng yuqori ballni topish, saralash, filtrlash.</p>',
      points: [
        'Python: <code>students = [Student(...), ...]</code>; Java: <code>Student[] arr = new Student[n]</code> yoki <code>List&lt;Student&gt;</code>.',
        'Java’da <code>new Student[n]</code> faqat <em>havolalar</em> massivini yaratadi — har bir elementni alohida <code>new</code> bilan toʻldirish kerak.',
        'Obyektlarni maydon boʻyicha saralash: Python <code>sorted(lst, key=lambda s: s.score)</code>, Java <code>list.sort(Comparator.comparing(...))</code>.',
        'Qidiruv: sikl bilan shartga mos birinchi obyektni topish.',
      ],
      pyCode: 'class Product:\n    def __init__(self, name, price):\n        self.name, self.price = name, price\n\n\nitems = [Product("Daftar", 5000), Product("Ruchka", 2000), Product("Kitob", 45000)]\nitems.sort(key=lambda p: p.price)\nfor p in items:\n    print(p.name, p.price)\n',
      javaCode: J('    record Product(String name, int price) {}\n\n    public static void main(String[] args) {\n        List<Product> items = new ArrayList<>(List.of(\n            new Product("Daftar", 5000), new Product("Ruchka", 2000), new Product("Kitob", 45000)));\n        items.sort(Comparator.comparingInt(Product::price));\n        for (Product p : items) System.out.println(p.name() + " " + p.price());\n    }'),
      mistake: 'Java’da obyektlar massivini yaratib, elementlarini <code>new</code> bilan toʻldirmasdan ishlatish — <code>NullPointerException</code>.',
      practice: 'Talabalar roʻyxatidan balli 60 dan past boʻlganlarni alohida roʻyxatga ajrating.',
    }),
    M4: topic({
      intro: '<p><strong>Inkapsulyatsiya</strong> — obyekt holatini tashqaridan yashirib, faqat metodlar orqali oʻzgartirish. <strong>Merosxoʻrlik (vorislik)</strong> — yangi sinf mavjud sinfning maydon va metodlarini qabul qiladi. <strong>Polimorfizm</strong> — bir xil nomli metod turli sinflarda turlicha ishlaydi.</p>',
      points: [
        'Java: <code>private</code> maydon + <code>public</code> getter/setter; Python: <code>_balance</code> kelishuvi va <code>@property</code>.',
        'Vorislik: Java <code>class Circle extends Shape</code>, Python <code>class Circle(Shape)</code>.',
        'Metodni qayta aniqlash (override): voris sinf ota sinf metodini oʻz versiyasi bilan almashtiradi.',
        'Polimorfizm: <code>Shape</code> roʻyxatidagi har bir obyekt uchun <code>area()</code> chaqirilsa, har biri oʻz formulasini ishlatadi.',
        '<code>super()</code> — ota sinf konstruktori yoki metodini chaqirish.',
      ],
      pyCode: 'import math\n\n\nclass Shape:\n    def area(self):\n        raise NotImplementedError\n\n\nclass Circle(Shape):\n    def __init__(self, r):\n        self.r = r\n\n    def area(self):\n        return math.pi * self.r ** 2\n\n\nclass Rect(Shape):\n    def __init__(self, w, h):\n        self.w, self.h = w, h\n\n    def area(self):\n        return self.w * self.h\n\n\nfor s in [Circle(1), Rect(2, 3)]:\n    print(type(s).__name__, round(s.area(), 2))\n',
      javaCode: J('    static abstract class Shape { abstract double area(); }\n\n    static class Circle extends Shape {\n        private final double r;\n        Circle(double r) { this.r = r; }\n        @Override double area() { return Math.PI * r * r; }\n    }\n\n    static class Rect extends Shape {\n        private final double w, h;\n        Rect(double w, double h) { this.w = w; this.h = h; }\n        @Override double area() { return w * h; }\n    }\n\n    public static void main(String[] args) {\n        for (Shape s : List.of(new Circle(1), new Rect(2, 3)))\n            System.out.printf(Locale.US, "%s %.2f%n", s.getClass().getSimpleName(), s.area());\n    }'),
      mistake: 'maydonni <code>public</code> qilib, tashqaridan istalgan qiymat berishga ruxsat qoldirish — masalan, balansni manfiy qilib qoʻyish mumkin boʻladi.',
      practice: '<code>Hayvon</code> sinfi va undan meros oluvchi <code>Mushuk</code>, <code>It</code> sinflarini yozing; har biri <code>ovoz()</code> metodini oʻzicha qaytarsin.',
    }),
    M5: topic({
      intro: '<p>Shablonlar (C++ templates) turga bogʻliq boʻlmagan kod yozish imkonini beradi. Java’da ularning mos keluvchisi — <strong>generics</strong> (<code>List&lt;T&gt;</code>), Pythonda esa dinamik tiplash tufayli funksiya har qanday turdagi qiymat bilan ishlaydi; tip izohlari uchun <code>TypeVar</code> ishlatiladi.</p>',
      points: [
        'Umumlashgan funksiya: bitta <code>max_of</code> funksiyasi butun son, satr yoki sana uchun ishlaydi.',
        'Java generic metod: <code>static &lt;T extends Comparable&lt;T&gt;&gt; T maxOf(List&lt;T&gt; list)</code>.',
        'Generic sinf: <code>class Box&lt;T&gt; { T value; }</code>.',
        'Afzallik: kod takrorlanmaydi, xatolar kompilyatsiya vaqtida aniqlanadi (Java).',
      ],
      pyCode: 'from typing import TypeVar\n\nT = TypeVar("T")\n\n\ndef max_of(items: list[T]) -> T:\n    best = items[0]\n    for x in items[1:]:\n        if x > best:\n            best = x\n    return best\n\n\nprint(max_of([3, 9, 2]))            # 9\nprint(max_of(["olma", "anor"]))     # olma\n',
      javaCode: J('    static <T extends Comparable<T>> T maxOf(List<T> items) {\n        T best = items.get(0);\n        for (T x : items) if (x.compareTo(best) > 0) best = x;\n        return best;\n    }\n\n    public static void main(String[] args) {\n        System.out.println(maxOf(List.of(3, 9, 2)));          // 9\n        System.out.println(maxOf(List.of("olma", "anor")));   // olma\n    }'),
      mistake: 'Java’da generic turga primitiv turni berish (<code>List&lt;int&gt;</code>) — oʻrniga <code>List&lt;Integer&gt;</code> ishlatiladi.',
      practice: 'Ikki qiymatni oʻrnini almashtiruvchi generic <code>Pair&lt;A, B&gt;</code> sinfini va <code>swap()</code> metodini yozing.',
    }),
    M6: topic({
      intro: '<p>Konteynerlar (kolleksiyalar) — maʼlumotlarni saqlash va boshqarish uchun tayyor tuzilmalar. Toʻgʻri konteyner tanlash dastur tezligini belgilaydi.</p>',
      points: [
        '<strong>Roʻyxat</strong>: Python <code>list</code>, Java <code>ArrayList</code> — tartiblangan, indeks boʻyicha tez murojaat.',
        '<strong>Toʻplam</strong>: <code>set</code> / <code>HashSet</code> — takrorlanmas elementlar, aʼzolikni tekshirish oʻrtacha O(1).',
        '<strong>Lugʻat</strong>: <code>dict</code> / <code>HashMap</code> — kalit → qiymat juftliklari.',
        '<strong>Navbat va stek</strong>: <code>collections.deque</code> / <code>ArrayDeque</code>.',
        'Saralangan xarita: Java <code>TreeMap</code>; Pythonda <code>sorted(d.items())</code>.',
      ],
      pyCode: 'from collections import Counter\n\ntext = "olma anor olma nok olma anor"\ncount = Counter(text.split())\nfor word, n in count.most_common():\n    print(word, n)\n# olma 3 / anor 2 / nok 1\n',
      javaCode: J('    public static void main(String[] args) {\n        String text = "olma anor olma nok olma anor";\n        Map<String, Integer> count = new HashMap<>();\n        for (String w : text.split(" ")) count.merge(w, 1, Integer::sum);\n        count.entrySet().stream()\n            .sorted((a, b) -> b.getValue() - a.getValue())\n            .forEach(e -> System.out.println(e.getKey() + " " + e.getValue()));\n    }'),
      mistake: 'katta roʻyxatda <code>x in list</code> bilan koʻp marta qidirish — har safar O(n); tez-tez tekshiriladigan maʼlumot uchun <code>set</code> ishlating.',
      practice: 'Matndagi har bir harf necha marta uchrashini lugʻat (<code>dict</code>/<code>HashMap</code>) yordamida hisoblang.',
    }),
    M7: topic({
      intro: '<p>Integratsiyalashgan dasturlash muhiti (IDE) — kod yozish, kompilyatsiya, ishga tushirish va xatolarni tuzatishni bir joyda birlashtiradi. Sillabusda Visual Studio nomlangan; platformadagi Python va Java uchun keng tarqalgan muhitlar — Visual Studio Code, PyCharm va IntelliJ IDEA. Tamoyillar hammasida bir xil.</p>',
      points: [
        '<strong>Loyiha</strong> — manba fayllar, kutubxonalar va sozlamalar toʻplami.',
        '<strong>Debugger</strong>: toʻxtash nuqtasi (breakpoint), qadamma-qadam bajarish, oʻzgaruvchilar qiymatini kuzatish.',
        '<strong>Avtoyakunlash va refaktoring</strong>: nomni butun loyihada xavfsiz oʻzgartirish.',
        '<strong>Versiyalarni boshqarish</strong>: Git bilan integratsiya — oʻzgarishlar tarixi.',
        'Formatlash va linter: kodni bir xil uslubda saqlash, xatolarni ishga tushirishdan oldin topish.',
      ],
      detail: '<h3>Debugger bilan ishlash tartibi</h3><ol><li>Shubhali satrga toʻxtash nuqtasi qoʻying.</li><li>Dasturni debug rejimida ishga tushiring.</li><li>“Step over” bilan qadamma-qadam yuring, oʻzgaruvchilar panelini kuzating.</li><li>Kutilgan va haqiqiy qiymat farq qilgan joy — xato manbai.</li></ol>',
      mistake: 'xatoni topish uchun kodni tasodifiy oʻzgartirib koʻrish. Debugger bilan dastur holatini kuzatish tezroq va ishonchliroq.',
      practice: 'IDE da oddiy sikl yozing, sikl ichiga toʻxtash nuqtasi qoʻyib, har aylanishda hisoblagich qiymatini kuzating.',
      links: [LINKS.pyTutorial, LINKS.javaTutorial],
    }),
    M8: topic({
      intro: '<p>Komponenta — mustaqil, qayta ishlatiladigan dastur qismi: grafik interfeysda tugma, matn maydoni, roʻyxat; serverda esa alohida xizmat. Komponentalar hodisalar (events) orqali muloqot qiladi: foydalanuvchi tugmani bosadi → hodisa ishlovchisi (handler) chaqiriladi.</p>',
      points: [
        'Grafik interfeys kutubxonalari: Python — <code>tkinter</code>, Java — <code>Swing</code>/<code>JavaFX</code>.',
        '<strong>Hodisaga asoslangan dasturlash</strong>: dastur oqimini foydalanuvchi harakatlari boshqaradi.',
        '<strong>Joylashuv (layout)</strong>: komponentalarni oynada tartiblash qoidalari.',
        'Mantiqni interfeysdan ajratish: hisob-kitob alohida sinfda, interfeys faqat koʻrsatadi.',
      ],
      pyCode: '# Grafik oyna kompyuterda ishga tushiriladi (sandboxda ekran yoʻq).\nimport tkinter as tk\n\nroot = tk.Tk()\nroot.title("Hisoblagich")\ncount = tk.IntVar(value=0)\ntk.Label(root, textvariable=count, font=("Arial", 24)).pack()\ntk.Button(root, text="+1", command=lambda: count.set(count.get() + 1)).pack()\nroot.mainloop()\n',
      javaCode: J('    // Grafik oyna kompyuterda ishga tushiriladi (sandboxda ekran yoʻq).\n    public static void main(String[] args) {\n        javax.swing.SwingUtilities.invokeLater(() -> {\n            javax.swing.JFrame f = new javax.swing.JFrame("Hisoblagich");\n            javax.swing.JLabel label = new javax.swing.JLabel("0");\n            javax.swing.JButton b = new javax.swing.JButton("+1");\n            b.addActionListener(e -> label.setText(String.valueOf(Integer.parseInt(label.getText()) + 1)));\n            f.setLayout(new java.awt.FlowLayout());\n            f.add(label);\n            f.add(b);\n            f.setSize(240, 120);\n            f.setVisible(true);\n        });\n    }', ''),
      mistake: 'uzoq davom etadigan hisob-kitobni hodisa ishlovchisi ichida bajarish — interfeys “qotib” qoladi.',
      practice: 'Ikki matn maydoni va “Qoʻshish” tugmasidan iborat oyna chizing (qogʻozda yoki IDE da): qaysi hodisa qaysi metodni chaqiradi?',
    }),
    A1: practical({
      goal: '<p>Koʻp oʻlchamli statik massivlar bilan ishlash va har bir satr ustida saralash algoritmini qoʻllash.</p>',
      steps: ['Matritsani ikki oʻlchamli massivga oʻqing.', 'Har bir satrni alohida massiv sifatida saralang (tayyor funksiya yoki oʻzingiz yozgan qoʻyish usuli).', 'Natijani talab qilingan formatda chiqaring.'],
      refs: [task('DAS-S2-A1')],
      check: 'Ustunlar boʻyicha saralash uchun kod qanday oʻzgaradi?',
    }),
    A2: practical({
      goal: '<p>Qidirish usullari: chiziqli va binar qidiruvni solishtirish.</p>',
      theory: '<p>Binar qidiruv har qadamda qidiruv oraligʻini ikki barobar qisqartiradi: 10⁵ elementli massivda eng koʻpi bilan 17 qadam. Shart — massiv saralangan boʻlishi.</p>',
      steps: ['Massiv va soʻrovlarni tez oʻqing (Python: sys.stdin, Java: BufferedReader).', 'Har bir soʻrov uchun lo, hi chegaralari bilan binar qidiruv bajaring.', 'Natijalarni yigʻib, bir marta chiqaring.'],
      refs: [task('DAS-S2-A2')],
      check: 'mid = (lo + hi) / 2 ifodasi Java’da qachon toʻlib ketishi mumkin va uni qanday oldini olish mumkin?',
    }),
    A3: practical({
      goal: '<p>Funksiya tavsifi, qiymat qaytaruvchi funksiya va funksiyani qayta ishlatish.</p>',
      theory: '<p>Funksiyani qayta yuklash (overloading) Java’da bir xil nomli, lekin parametrlari farq qiladigan metodlar yozish imkonini beradi: <code>area(double r)</code> va <code>area(double w, double h)</code>. Pythonda buning oʻrniga standart qiymatli parametrlar ishlatiladi.</p>',
      steps: ['gcd funksiyasini Evklid algoritmi bilan yozing.', 'lcm funksiyasini gcd orqali ifodalang.', 'Asosiy dasturda faqat funksiyalarni chaqiring.'],
      refs: [task('DAS-S2-A3')],
      check: 'Nega lcm = a · b / gcd emas, balki a / gcd · b tartibida hisoblash xavfsizroq?',
    }),
    A4: practical({
      goal: '<p>Rekursiv funksiya va oʻz funksiyalar kutubxonasini tashkil etish.</p>',
      theory: '<p>Rekursiv funksiyada har doim <strong>toʻxtash sharti</strong> (bazaviy holat) boʻlishi shart. Tez darajaga koʻtarishda bazaviy holat — n = 0.</p>',
      steps: ['Bazaviy holatni yozing.', 'n ni ikkiga boʻlib rekursiv chaqiring va natijani kvadratga koʻtaring.', 'Toq n uchun yana bir marta a ga koʻpaytiring; har qadamda mod m oling.'],
      refs: [task('DAS-S2-A4')],
      check: 'n = 10¹⁸ boʻlganda nechta rekursiv chaqiruv bajariladi?',
    }),
    A5: practical({
      goal: '<p>Dinamik xotira: oʻlchami dastur ishlayotganda aniqlanadigan massivlar. C++ dagi koʻrsatkichlar va <code>new</code> oʻrniga Python roʻyxatlari va Java’dagi <code>new long[n]</code>, <code>ArrayList</code> ishlatiladi; xotirani tozalashni axlat yigʻuvchi (garbage collector) bajaradi.</p>',
      steps: ['n ni oʻqib, n elementli massiv yarating.', 'Yigʻindini hisoblang.', 'Har bir elementni butun sonli taqqoslash bilan (aᵢ · n &gt; S) oʻrtacha bilan solishtiring.'],
      refs: [task('DAS-S2-A5')],
      check: 'Nega oʻrtachani double sifatida hisoblab solishtirish ba’zan notoʻgʻri natija berishi mumkin?',
    }),
    A6: practical({
      goal: '<p>Satrlar va belgilar: har bir belgi kompyuterda son (kod) bilan ifodalanadi. ASCII jadvalida lotin harflari, raqamlar va tinish belgilari 0–127 oraligʻida.</p>',
      theory: '<p>“A” = 65, “a” = 97, “0” = 48, boʻsh joy = 32. Katta va kichik harf kodlari farqi 32 ga teng.</p>',
      steps: ['Satrni oʻqing.', 'Har bir belgining kodini oling (Python ord, Java (int)).', 'Kodlarni boʻsh joy bilan chiqaring.'],
      refs: [task('DAS-S2-A6')],
      check: 'Kod boʻyicha belgi olish uchun qaysi funksiyalar ishlatiladi? (Python chr, Java (char).)',
    }),
    A7: practical({
      goal: '<p>Satrlar bilan ishlovchi funksiyalar: filtrlash, registrni oʻzgartirish, teskari tartib.</p>',
      steps: ['Satrdan faqat harf va raqamlarni ajrating.', 'Hammasini kichik harfga oʻtkazing.', 'Natijani uning teskarisi bilan solishtiring.'],
      refs: [task('DAS-S2-A7')],
      check: 'Qoʻshimcha xotira ishlatmasdan, ikki koʻrsatkich (boshidan va oxiridan) bilan tekshirish qanday boʻladi?',
    }),
    A8: practical({
      goal: '<p>Matnli fayllar va oqimlar. Fayl ham, klaviatura ham dastur uchun <strong>oqim</strong>: maʼlumot satrma-satr oʻqiladi, oxiriga yetganda EOF (fayl oxiri) belgisi keladi.</p>',
      theory: '<p>Haqiqiy faylni ochish: Python <code>with open("data.txt", encoding="utf-8") as f:</code>, Java <code>Files.readAllLines(Path.of("data.txt"))</code>. Sandboxda fayl tizimi yopiq, shuning uchun topshiriqda matn standart kirish oqimidan oʻqiladi — kod tuzilishi bir xil.</p>',
      steps: ['Oqimni oxirigacha satrma-satr oʻqing.', 'Har bir satr uchun uzunlik va soʻzlar sonini qoʻshing.', 'Uch natijani bitta satrda chiqaring.'],
      refs: [task('DAS-S2-A8')],
      check: 'Faylni yopish nima uchun muhim va <code>with</code> / try-with-resources buni qanday kafolatlaydi?',
    }),
    MT1: independent({
      goal: '<p>Bir oʻlchamli massivni funksiyaga parametr sifatida uzatish va natijani qaytarish.</p>',
      tasks: ['Massivning maksimumi va uning oʻrnini qaytaruvchi funksiyani yozing (topshiriq quyida).', 'Xuddi shu uslubda minimum va oʻrtacha qiymatni qaytaruvchi funksiyalar yozing.', 'Python va Java’da massiv funksiyaga qanday uzatilishini (havola orqali) izohlang.'],
      refs: [task('DAS-S2-MT1')],
      links: [LINKS.pyTutorial],
    }),
    MT2: independent({
      goal: '<p>Matritsalarni funksiya parametri sifatida qoʻllash.</p>',
      tasks: ['Matritsalarni koʻpaytirish funksiyasini yozing (topshiriq quyida).', 'Transponirlash funksiyasini qoʻshing.', 'Koʻpaytirish murakkabligini (n · m · p amal) tushuntiring.'],
      refs: [task('DAS-S2-MT2')],
    }),
    MT3: independent({
      goal: '<p>Dinamik massivlar: oʻlchami oldindan nomaʼlum maʼlumotlarni saqlash.</p>',
      tasks: ['Ikki saralangan massivni birlashtiring (topshiriq quyida).', 'ArrayList (Java) yoki list (Python) sigʻimi qanday kengayishini oʻrganing va qisqacha yozing.'],
      refs: [task('DAS-S2-MT3')],
    }),
    MT4: independent({
      goal: '<p>Rekursiv jarayonlarni tashkil etish.</p>',
      tasks: ['Xanoy minoralari masalasini yeching (topshiriq quyida).', 'n disk uchun harakatlar soni 2ⁿ − 1 ekanini matematik induksiya bilan asoslang.'],
      refs: [task('DAS-S2-MT4')],
    }),
    MT5: independent({
      goal: '<p>Ichma-ich joylashgan rekursiv jarayonlar.</p>',
      tasks: ['Akkerman funksiyasini yozing (topshiriq quyida).', 'A(2, n) = 2n + 3 ekanini bir nechta qiymatda tekshiring.', 'Nega A(4, 2) ni hisoblash amalda imkonsiz ekanini izohlang.'],
      refs: [task('DAS-S2-MT5')],
    }),
    MT6: independent({
      goal: '<p>Satrlar bilan ishlash.</p>',
      tasks: ['Soʻzlarni bosh harf bilan yozuvchi dasturni tuzing (topshiriq quyida).', 'Satrdagi eng uzun soʻzni topuvchi funksiya yozing.'],
      refs: [task('DAS-S2-MT6')],
    }),
    MT7: independent({
      goal: '<p>Oʻzaro bogʻlangan sinflarni tashkil etish: bir sinf boshqa sinf obyektlarini oʻz ichiga oladi (masalan, <code>Bank</code> → koʻp <code>BankAccount</code>).</p>',
      tasks: ['BankAccount sinfini yozing (topshiriq quyida).', 'Bank sinfini loyihalang: hisoblar roʻyxati, hisob ochish va hisoblararo pul oʻtkazish metodi.', 'Sinflar diagrammasini chizing.'],
      refs: [task('DAS-S2-MT7')],
      links: [LINKS.javaTutorial],
    }),
  },
}

export default [semester1, semester2]
