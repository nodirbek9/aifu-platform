// MTA1204 Ma'lumotlar tuzilmasi va algoritmlar — lectures with Python/Java code, a coding task
// for every practical, two independent-study briefs.
import { LINKS, independent, practical, task, topic } from './_blocks.mjs'

const J = (body, imports = 'import java.util.*;') => `${imports}\n\npublic class Main {\n${body}\n}\n`
const IO = 'import java.io.*;\nimport java.util.*;'

const tasks = [
  {
    code: 'MTA-A1',
    title: 'Evklid algoritmi va qadamlar soni',
    description: '<p>Evklid algoritmi bilan EKUB ni toping va algoritm necha marta “qoldiqli boʻlish” qadamini bajarganini sanang. Bu algoritmning <em>natijadorlik</em> va <em>cheklilik</em> xossalarini kuzatish imkonini beradi.</p>',
    instructions: 'Kirish: a b. Chiqish: bitta satrda EKUB va qadamlar soni (b ≠ 0 boʻlgan har bir aylanish bitta qadam).',
    constraints: '1 ≤ a, b ≤ 10^9.',
    python: {
      starter: 'a, b = map(int, input().split())\nsteps = 0\n# ...\n',
      solution: 'a, b = map(int, input().split())\nsteps = 0\nwhile b != 0:\n    a, b = b, a % b\n    steps += 1\nprint(a, steps)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        int steps = 0;\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        long a = in.nextLong(), b = in.nextLong();\n        int steps = 0;\n        while (b != 0) {\n            long t = a % b;\n            a = b;\n            b = t;\n            steps++;\n        }\n        System.out.println(a + " " + steps);\n    }'),
    },
    tests: [['48 18', '6 3'], ['7 13', '1 4'], ['10 10', '10 1'], ['1 1000000000', '1 2'], ['832040 514229', '1 28']],
  },
  {
    code: 'MTA-A2',
    title: 'Fibonachchi sonlari: xotirali rekursiya',
    description: '<p>Oddiy rekursiya F(n) = F(n−1) + F(n−2) eksponensial vaqt oladi, chunki bir xil qiymatlar qayta-qayta hisoblanadi. Hisoblangan qiymatlarni saqlab (memoizatsiya) murakkablikni O(n) ga tushiring.</p>',
    instructions: 'Kirish: n. Chiqish: F(n), bunda F(0) = 0, F(1) = 1.',
    constraints: '0 ≤ n ≤ 90 (natija long turiga sigʻadi).',
    python: {
      starter: 'from functools import lru_cache\n\n\n@lru_cache(maxsize=None)\ndef fib(n):\n    # ...\n    pass\n\n\nprint(fib(int(input())))\n',
      solution: 'from functools import lru_cache\n\n\n@lru_cache(maxsize=None)\ndef fib(n):\n    if n < 2:\n        return n\n    return fib(n - 1) + fib(n - 2)\n\n\nprint(fib(int(input())))\n',
    },
    java: {
      starter: J('    static long[] memo = new long[91];\n\n    static long fib(int n) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) {\n        System.out.println(fib(new Scanner(System.in).nextInt()));\n    }'),
      solution: J('    static long[] memo = new long[91];\n\n    static long fib(int n) {\n        if (n < 2) return n;\n        if (memo[n] != 0) return memo[n];\n        return memo[n] = fib(n - 1) + fib(n - 2);\n    }\n\n    public static void main(String[] args) {\n        System.out.println(fib(new Scanner(System.in).nextInt()));\n    }'),
    },
    tests: [['10', '55'], ['0', '0'], ['1', '1'], ['50', '12586269025'], ['90', '2880067194370816120']],
  },
  {
    code: 'MTA-A3',
    title: 'Saralangan massivda qiymat necha marta uchraydi',
    description: '<p>Saralangan massivda x ning birinchi va oxirgi oʻrnini binar qidiruv bilan toping (pastki va yuqori chegara). Ular farqi — x ning uchrashlar soni. Har bir soʻrov O(log n).</p>',
    instructions: 'Kirish: n; n ta son (kamaymaydigan tartibda); q; q ta soʻrov. Chiqish: har bir soʻrov uchun alohida satrda uchrashlar soni.',
    constraints: '1 ≤ n, q ≤ 10^5.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\nfrom bisect import bisect_left, bisect_right\n\ndata = sys.stdin.read().split()\n# ...\n',
      solution: 'import sys\nfrom bisect import bisect_left, bisect_right\n\ndata = sys.stdin.read().split()\nn = int(data[0])\na = list(map(int, data[1:1 + n]))\nq = int(data[1 + n])\nout = []\nfor x in map(int, data[2 + n:2 + n + q]):\n    out.append(str(bisect_right(a, x) - bisect_left(a, x)))\nprint("\\n".join(out))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        // ...\n    }', IO),
      solution: J('    static int lowerBound(long[] a, long x) {\n        int lo = 0, hi = a.length;\n        while (lo < hi) {\n            int mid = (lo + hi) >>> 1;\n            if (a[mid] < x) lo = mid + 1; else hi = mid;\n        }\n        return lo;\n    }\n\n    public static void main(String[] args) throws IOException {\n        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));\n        st.nextToken();\n        int n = (int) st.nval;\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) { st.nextToken(); a[i] = (long) st.nval; }\n        st.nextToken();\n        int q = (int) st.nval;\n        StringBuilder out = new StringBuilder();\n        for (int k = 0; k < q; k++) {\n            st.nextToken();\n            long x = (long) st.nval;\n            out.append(lowerBound(a, x + 1) - lowerBound(a, x)).append(\'\\n\');\n        }\n        System.out.print(out);\n    }', IO),
    },
    tests: [['7\n1 2 2 2 5 7 7\n3\n2\n7\n4', '3\n2\n0'], ['1\n5\n2\n5\n6', '1\n0'], ['5\n-3 -3 -3 -3 -3\n2\n-3\n0', '5\n0']],
  },
  {
    code: 'MTA-A4',
    title: 'Piramidal saralash (heapsort)',
    description: '<p>Massivni piramida (maks-uyum) koʻrinishiga keltiring, soʻng eng katta elementni oxiriga oʻtkazib, piramidani qayta tiklang. Murakkablik O(n log n), qoʻshimcha xotira O(1). Tayyor saralash funksiyasidan foydalanmasdan yozing.</p>',
    instructions: 'Kirish: n; n ta butun son. Chiqish: oʻsish tartibida saralangan sonlar.',
    constraints: '1 ≤ n ≤ 10^5.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'def sift_down(a, i, n):\n    # ...\n    pass\n\n\ndef heap_sort(a):\n    # ...\n    pass\n\n\ninput()\na = list(map(int, input().split()))\nheap_sort(a)\nprint(" ".join(map(str, a)))\n',
      solution: 'def sift_down(a, i, n):\n    while True:\n        big = i\n        l, r = 2 * i + 1, 2 * i + 2\n        if l < n and a[l] > a[big]:\n            big = l\n        if r < n and a[r] > a[big]:\n            big = r\n        if big == i:\n            return\n        a[i], a[big] = a[big], a[i]\n        i = big\n\n\ndef heap_sort(a):\n    n = len(a)\n    for i in range(n // 2 - 1, -1, -1):\n        sift_down(a, i, n)\n    for end in range(n - 1, 0, -1):\n        a[0], a[end] = a[end], a[0]\n        sift_down(a, 0, end)\n\n\ninput()\na = list(map(int, input().split()))\nheap_sort(a)\nprint(" ".join(map(str, a)))\n',
    },
    java: {
      starter: J('    static void siftDown(long[] a, int i, int n) {\n        // ...\n    }\n\n    static void heapSort(long[] a) {\n        // ...\n    }\n\n    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        int n = Integer.parseInt(br.readLine().trim());\n        StringTokenizer st = new StringTokenizer(br.readLine());\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = Long.parseLong(st.nextToken());\n        heapSort(a);\n        // ...\n    }', IO),
      solution: J('    static void siftDown(long[] a, int i, int n) {\n        while (true) {\n            int big = i, l = 2 * i + 1, r = 2 * i + 2;\n            if (l < n && a[l] > a[big]) big = l;\n            if (r < n && a[r] > a[big]) big = r;\n            if (big == i) return;\n            long t = a[i]; a[i] = a[big]; a[big] = t;\n            i = big;\n        }\n    }\n\n    static void heapSort(long[] a) {\n        int n = a.length;\n        for (int i = n / 2 - 1; i >= 0; i--) siftDown(a, i, n);\n        for (int end = n - 1; end > 0; end--) {\n            long t = a[0]; a[0] = a[end]; a[end] = t;\n            siftDown(a, 0, end);\n        }\n    }\n\n    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        int n = Integer.parseInt(br.readLine().trim());\n        StringTokenizer st = new StringTokenizer(br.readLine());\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = Long.parseLong(st.nextToken());\n        heapSort(a);\n        StringBuilder out = new StringBuilder();\n        for (int i = 0; i < n; i++) {\n            if (i > 0) out.append(\' \');\n            out.append(a[i]);\n        }\n        System.out.println(out);\n    }', IO),
    },
    tests: [['6\n5 1 4 2 8 0', '0 1 2 4 5 8'], ['1\n3', '3'], ['5\n-1 -1 3 -7 2', '-7 -1 -1 2 3']],
  },
  {
    code: 'MTA-A5',
    title: 'Pufaksimon saralashdagi almashtirishlar soni',
    description: '<p>Pufaksimon saralash qoʻshni elementlar notoʻgʻri tartibda boʻlsa ularni almashtiradi. Almashtirishlar soni massivdagi <strong>inversiyalar</strong> (i &lt; j va aᵢ &gt; aⱼ juftliklar) soniga teng. Shuni hisoblang.</p>',
    instructions: 'Kirish: n; n ta son. Chiqish: almashtirishlar soni.',
    constraints: '1 ≤ n ≤ 2000.',
    python: {
      starter: 'n = int(input())\na = list(map(int, input().split()))\nswaps = 0\n# ...\nprint(swaps)\n',
      solution: 'n = int(input())\na = list(map(int, input().split()))\nswaps = 0\nfor i in range(n):\n    for j in range(n - 1 - i):\n        if a[j] > a[j + 1]:\n            a[j], a[j + 1] = a[j + 1], a[j]\n            swaps += 1\nprint(swaps)\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        long swaps = 0;\n        // ...\n        System.out.println(swaps);\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] a = new long[n];\n        for (int i = 0; i < n; i++) a[i] = in.nextLong();\n        long swaps = 0;\n        for (int i = 0; i < n; i++)\n            for (int j = 0; j < n - 1 - i; j++)\n                if (a[j] > a[j + 1]) { long t = a[j]; a[j] = a[j + 1]; a[j + 1] = t; swaps++; }\n        System.out.println(swaps);\n    }'),
    },
    tests: [['4\n4 3 2 1', '6'], ['5\n1 2 3 4 5', '0'], ['5\n2 4 1 3 5', '3'], ['3\n1 1 1', '0']],
  },
  {
    code: 'MTA-A6',
    title: 'Qavslar muvozanati (stek)',
    description: '<p>Satr faqat <code>( ) [ ] { }</code> qavslaridan iborat. Stek yordamida qavslar toʻgʻri ochilib-yopilganini tekshiring: ochuvchi qavs stekka qoʻyiladi, yopuvchi qavs stek tepasidagi bilan mos kelishi kerak.</p>',
    instructions: 'Kirish: bitta satr. Chiqish: Muvozanatli yoki Muvozanatsiz.',
    constraints: 'Satr uzunligi 1 dan 10^5 gacha.',
    python: {
      starter: 's = input().strip()\nstack = []\n# ...\n',
      solution: 's = input().strip()\npairs = {")": "(", "]": "[", "}": "{"}\nstack = []\nok = True\nfor ch in s:\n    if ch in "([{":\n        stack.append(ch)\n    elif not stack or stack.pop() != pairs[ch]:\n        ok = False\n        break\nprint("Muvozanatli" if ok and not stack else "Muvozanatsiz")\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine().trim();\n        Deque<Character> stack = new ArrayDeque<>();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        String s = new Scanner(System.in).nextLine().trim();\n        Deque<Character> stack = new ArrayDeque<>();\n        boolean ok = true;\n        for (char ch : s.toCharArray()) {\n            if (ch == \'(\' || ch == \'[\' || ch == \'{\') stack.push(ch);\n            else {\n                char open = ch == \')\' ? \'(\' : ch == \']\' ? \'[\' : \'{\';\n                if (stack.isEmpty() || stack.pop() != open) { ok = false; break; }\n            }\n        }\n        System.out.println(ok && stack.isEmpty() ? "Muvozanatli" : "Muvozanatsiz");\n    }'),
    },
    tests: [['([]{})', 'Muvozanatli'], ['([)]', 'Muvozanatsiz'], ['((', 'Muvozanatsiz'], ['{[()()]}', 'Muvozanatli'], [')(', 'Muvozanatsiz']],
  },
  {
    code: 'MTA-A7',
    title: 'Deykstra algoritmi: eng qisqa masofalar',
    description: '<p>Yoʻnaltirilmagan, manfiy boʻlmagan vaznli grafda s uchdan barcha uchlarga eng qisqa masofani Deykstra algoritmi bilan toping. Ustuvor navbat (heap) bilan murakkablik O((n + m) log n).</p>',
    instructions: 'Kirish: n m; m ta satrda u v w (qirra va vazni); s. Chiqish: bitta satrda 1..n uchlargacha masofalar; yetib boʻlmasa -1.',
    constraints: '1 ≤ n ≤ 10^4, 0 ≤ m ≤ 5·10^4, 0 ≤ w ≤ 10^6.',
    difficulty: 'HARD',
    python: {
      starter: 'import heapq\nimport sys\n\ndata = sys.stdin.read().split()\n# ...\n',
      solution: 'import heapq\nimport sys\n\ndata = sys.stdin.read().split()\nn, m = int(data[0]), int(data[1])\nadj = [[] for _ in range(n + 1)]\npos = 2\nfor _ in range(m):\n    u, v, w = int(data[pos]), int(data[pos + 1]), int(data[pos + 2])\n    pos += 3\n    adj[u].append((v, w))\n    adj[v].append((u, w))\ns = int(data[pos])\nINF = float("inf")\ndist = [INF] * (n + 1)\ndist[s] = 0\npq = [(0, s)]\nwhile pq:\n    d, u = heapq.heappop(pq)\n    if d > dist[u]:\n        continue\n    for v, w in adj[u]:\n        if d + w < dist[v]:\n            dist[v] = d + w\n            heapq.heappush(pq, (dist[v], v))\nprint(" ".join(str(d) if d != INF else "-1" for d in dist[1:]))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));\n        // ...\n    }', IO),
      solution: J('    public static void main(String[] args) throws IOException {\n        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));\n        st.nextToken(); int n = (int) st.nval;\n        st.nextToken(); int m = (int) st.nval;\n        List<List<long[]>> adj = new ArrayList<>();\n        for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());\n        for (int i = 0; i < m; i++) {\n            st.nextToken(); int u = (int) st.nval;\n            st.nextToken(); int v = (int) st.nval;\n            st.nextToken(); long w = (long) st.nval;\n            adj.get(u).add(new long[] {v, w});\n            adj.get(v).add(new long[] {u, w});\n        }\n        st.nextToken(); int s = (int) st.nval;\n        long[] dist = new long[n + 1];\n        Arrays.fill(dist, Long.MAX_VALUE);\n        dist[s] = 0;\n        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(x -> x[0]));\n        pq.add(new long[] {0, s});\n        while (!pq.isEmpty()) {\n            long[] top = pq.poll();\n            int u = (int) top[1];\n            if (top[0] > dist[u]) continue;\n            for (long[] e : adj.get(u)) {\n                int v = (int) e[0];\n                if (top[0] + e[1] < dist[v]) {\n                    dist[v] = top[0] + e[1];\n                    pq.add(new long[] {dist[v], v});\n                }\n            }\n        }\n        StringBuilder out = new StringBuilder();\n        for (int i = 1; i <= n; i++) {\n            if (i > 1) out.append(\' \');\n            out.append(dist[i] == Long.MAX_VALUE ? -1 : dist[i]);\n        }\n        System.out.println(out);\n    }', IO),
    },
    tests: [
      ['4 4\n1 2 1\n2 3 2\n1 3 5\n3 4 1\n1', '0 1 3 4'],
      ['3 1\n1 2 7\n3', '-1 -1 0'],
      ['5 6\n1 2 4\n1 3 1\n3 2 2\n2 4 1\n3 4 5\n4 5 3\n1', '0 3 1 4 7'],
    ],
  },
  {
    code: 'MTA-A8',
    title: 'Fenvik daraxti: yigʻindi soʻrovlari',
    description: '<p>Massivda ikki xil amalni tez bajaring: elementga qiymat qoʻshish va [l, r] oraliq yigʻindisini topish. Fenvik (binar indeksli) daraxti har ikkalasini O(log n) da bajaradi; oddiy sikl esa soʻrov boshiga O(n) oladi.</p>',
    instructions: 'Kirish: n; n ta son; q; q ta satrda add i x yoki sum l r (indekslar 1 dan). Chiqish: har bir sum soʻrovi uchun alohida satrda natija.',
    constraints: '1 ≤ n, q ≤ 10^5, |qiymatlar| ≤ 10^9.',
    difficulty: 'HARD',
    python: {
      starter: 'import sys\n\n\nclass Fenwick:\n    def __init__(self, n):\n        self.t = [0] * (n + 1)\n\n    def add(self, i, x):\n        # ...\n        pass\n\n    def prefix(self, i):\n        # ...\n        pass\n\n\ndata = sys.stdin.read().split()\n# ...\n',
      solution: 'import sys\n\n\nclass Fenwick:\n    def __init__(self, n):\n        self.n = n\n        self.t = [0] * (n + 1)\n\n    def add(self, i, x):\n        while i <= self.n:\n            self.t[i] += x\n            i += i & -i\n\n    def prefix(self, i):\n        s = 0\n        while i > 0:\n            s += self.t[i]\n            i -= i & -i\n        return s\n\n\ndata = sys.stdin.read().split()\nn = int(data[0])\nfw = Fenwick(n)\nfor i in range(n):\n    fw.add(i + 1, int(data[1 + i]))\nq = int(data[1 + n])\npos = 2 + n\nout = []\nfor _ in range(q):\n    op, a, b = data[pos], int(data[pos + 1]), int(data[pos + 2])\n    pos += 3\n    if op == "add":\n        fw.add(a, b)\n    else:\n        out.append(str(fw.prefix(b) - fw.prefix(a - 1)))\nprint("\\n".join(out))\n',
    },
    java: {
      starter: J('    static long[] t;\n    static int n;\n\n    static void add(int i, long x) {\n        // ...\n    }\n\n    static long prefix(int i) {\n        // ...\n        return 0;\n    }\n\n    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        // ...\n    }', IO),
      solution: J('    static long[] t;\n    static int n;\n\n    static void add(int i, long x) {\n        for (; i <= n; i += i & -i) t[i] += x;\n    }\n\n    static long prefix(int i) {\n        long s = 0;\n        for (; i > 0; i -= i & -i) s += t[i];\n        return s;\n    }\n\n    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        n = Integer.parseInt(br.readLine().trim());\n        t = new long[n + 1];\n        StringTokenizer st = new StringTokenizer(br.readLine());\n        for (int i = 1; i <= n; i++) add(i, Long.parseLong(st.nextToken()));\n        int q = Integer.parseInt(br.readLine().trim());\n        StringBuilder out = new StringBuilder();\n        for (int k = 0; k < q; k++) {\n            st = new StringTokenizer(br.readLine());\n            String op = st.nextToken();\n            int a = Integer.parseInt(st.nextToken());\n            long b = Long.parseLong(st.nextToken());\n            if (op.equals("add")) add(a, b);\n            else out.append(prefix((int) b) - prefix(a - 1)).append(\'\\n\');\n        }\n        System.out.print(out);\n    }', IO),
    },
    tests: [
      ['5\n1 2 3 4 5\n4\nsum 1 5\nadd 3 10\nsum 2 4\nsum 3 3', '15\n19\n13'],
      ['1\n-7\n2\nsum 1 1\nadd 1 7', '-7'],
      ['3\n1000000000 1000000000 1000000000\n1\nsum 1 3', '3000000000'],
    ],
  },
]

export default {
  subject: 'MTA1204',
  cohort: 'DI-26-01-S2',
  tasks,
  lessons: {
    M1: topic({
      intro: '<p><strong>Algoritm</strong> — masalani yechish uchun aniq belgilangan, chekli qadamlar ketma-ketligi. <em>Intuitiv</em> taʼrif uni “bajaruvchi uchun tushunarli koʻrsatmalar” deb biladi; <em>formal</em> taʼrif esa aniq matematik model talab qiladi — shunday modellardan biri <strong>Tyuring mashinasi</strong>. <em>Kibernetik</em> yondashuv algoritmni kirish axborotini chiqish axborotiga aylantiruvchi boshqaruv jarayoni sifatida koʻradi.</p>',
      points: [
        'Xossalar: <strong>diskretlik</strong>, <strong>aniqlik</strong>, <strong>natijadorlik</strong>, <strong>ommaviylik</strong>, <strong>cheklilik</strong>.',
        'Turlari: chiziqli, tarmoqlanuvchi, takrorlanuvchi, rekursiv.',
        '<strong>Tyuring mashinasi</strong>: cheksiz lenta, oʻqish-yozish kallagi, holatlar va oʻtish jadvali.',
        '<strong>Chyorch–Tyuring tezisi</strong>: intuitiv hisoblanuvchi har qanday funksiyani Tyuring mashinasi hisoblay oladi.',
      ],
      detail: '<p>Quyidagi dastur ikkilik sonni 1 ga oshiruvchi eng oddiy Tyuring mashinasini modellashtiradi: kallak oʻngdan chapga yuradi, 1 larni 0 ga aylantiradi va birinchi 0 (yoki boʻsh katak)ni 1 qiladi.</p>',
      pyCode: 'def turing_increment(tape):\n    cells = list(tape)\n    head = len(cells) - 1        # kallak eng oʻng katakda\n    state = "carry"\n    while state != "halt":\n        if head < 0:             # boʻsh katak: lentani chapga kengaytiramiz\n            cells.insert(0, "1")\n            state = "halt"\n        elif cells[head] == "1":\n            cells[head] = "0"\n            head -= 1\n        else:\n            cells[head] = "1"\n            state = "halt"\n    return "".join(cells)\n\n\nprint(turing_increment("1011"))  # 1100\nprint(turing_increment("111"))   # 1000\n',
      javaCode: J('    static String increment(String tape) {\n        StringBuilder cells = new StringBuilder(tape);\n        int head = cells.length() - 1;\n        while (true) {\n            if (head < 0) { cells.insert(0, \'1\'); break; }\n            if (cells.charAt(head) == \'1\') { cells.setCharAt(head, \'0\'); head--; }\n            else { cells.setCharAt(head, \'1\'); break; }\n        }\n        return cells.toString();\n    }\n\n    public static void main(String[] args) {\n        System.out.println(increment("1011")); // 1100\n        System.out.println(increment("111"));  // 1000\n    }'),
      mistake: 'algoritmni kod bilan tenglashtirish. Algoritm — gʻoya; uni tabiiy tilda, blok-sxemada yoki istalgan dasturlash tilida ifodalash mumkin.',
      practice: 'Evklid algoritmi beshta xossaning har birini qanoatlantirishini tushuntiring.',
      links: [LINKS.visualgo],
    }),
    M2: topic({
      intro: '<p>Algoritm <strong>murakkabligi</strong> — kirish hajmi n oshganda bajariladigan amallar (vaqt) yoki xotira qanday oʻsishi. U O-belgisi bilan ifodalanadi: konstantalar va kichik hadlar tashlanadi. Masalan, 3n² + 5n + 2 = O(n²).</p>',
      points: [
        'Asosiy sinflar: O(1) &lt; O(log n) &lt; O(n) &lt; O(n log n) &lt; O(n²) &lt; O(2ⁿ).',
        'Eng yomon, oʻrtacha va eng yaxshi holat murakkabligi farqlanadi.',
        '<strong>Rekursiv algoritm</strong> murakkabligi rekurrent munosabat bilan topiladi: T(n) = T(n/2) + O(1) ⇒ O(log n).',
        '<strong>Binar izlash</strong> saralangan massivda O(log n); <strong>interpolyatsion izlash</strong> tekis taqsimlangan maʼlumotda oʻrtacha O(log log n).',
      ],
      pyCode: 'def interpolation_search(a, x):\n    lo, hi = 0, len(a) - 1\n    while lo <= hi and a[lo] <= x <= a[hi]:\n        if a[hi] == a[lo]:\n            return lo if a[lo] == x else -1\n        # qiymatga qarab taxminiy oʻrin\n        pos = lo + (x - a[lo]) * (hi - lo) // (a[hi] - a[lo])\n        if a[pos] == x:\n            return pos\n        if a[pos] < x:\n            lo = pos + 1\n        else:\n            hi = pos - 1\n    return -1\n\n\nprint(interpolation_search([10, 20, 30, 40, 50, 60], 40))  # 3\n',
      javaCode: J('    static int binarySearch(int[] a, int x) {\n        int lo = 0, hi = a.length - 1;\n        while (lo <= hi) {\n            int mid = (lo + hi) >>> 1;   // toʻlib ketmaydigan oʻrtacha\n            if (a[mid] == x) return mid;\n            if (a[mid] < x) lo = mid + 1; else hi = mid - 1;\n        }\n        return -1;\n    }\n\n    public static void main(String[] args) {\n        System.out.println(binarySearch(new int[] {10, 20, 30, 40, 50, 60}, 40)); // 3\n    }'),
      mistake: 'murakkablikni aniq soniyalar bilan baholash. O-belgi kompyuter tezligiga emas, oʻsish sur’atiga qaraydi.',
      practice: 'n = 10⁶ uchun O(n²) va O(n log n) algoritmlar taxminan necha amal bajaradi? (10¹² va ~2·10⁷.)',
    }),
    M3: topic({
      intro: '<p><strong>Fibonachchi izlash</strong> binar izlashga oʻxshaydi, lekin oraliqni Fibonachchi sonlari nisbatida boʻladi va faqat qoʻshish-ayirish ishlatadi. <strong>Xeshlash</strong> kalitni xesh-funksiya orqali jadval indeksiga aylantiradi: qidiruv oʻrtacha O(1). <strong>Ichki saralash</strong> maʼlumot operativ xotiraga sigʻganda, <strong>tashqi saralash</strong> esa fayllar (diskda) bilan ishlaganda qoʻllanadi.</p>',
      points: [
        'Xesh-funksiya: h(k) = k mod m; yaxshi funksiya kalitlarni tekis taqsimlaydi.',
        '<strong>Kolliziya</strong> — ikki kalit bir indeksga tushishi; yechimlar: zanjirlar (chaining) va ochiq manzillash.',
        'Python <code>dict</code> va Java <code>HashMap</code> — tayyor xesh-jadvallar.',
        'Tashqi saralash: boʻlaklarni alohida saralab, keyin koʻp yoʻlli birlashtirish (merge).',
      ],
      pyCode: 'class HashTable:\n    """Zanjirli xesh-jadval: har bir katakda (kalit, qiymat) roʻyxati."""\n\n    def __init__(self, size=8):\n        self.buckets = [[] for _ in range(size)]\n\n    def _index(self, key):\n        return hash(key) % len(self.buckets)\n\n    def put(self, key, value):\n        bucket = self.buckets[self._index(key)]\n        for i, (k, _) in enumerate(bucket):\n            if k == key:\n                bucket[i] = (key, value)\n                return\n        bucket.append((key, value))\n\n    def get(self, key):\n        for k, v in self.buckets[self._index(key)]:\n            if k == key:\n                return v\n        return None\n\n\nt = HashTable()\nt.put("MTA1204", "Maʼlumotlar tuzilmasi")\nprint(t.get("MTA1204"))\n',
      javaCode: J('    public static void main(String[] args) {\n        Map<String, Integer> stock = new HashMap<>();\n        stock.put("daftar", 120);\n        stock.put("ruchka", 300);\n        stock.merge("daftar", 30, Integer::sum);     // mavjud qiymatga qoʻshish\n        System.out.println(stock.get("daftar"));     // 150\n        System.out.println(stock.containsKey("kitob")); // false\n    }'),
      mistake: 'xesh-jadvalda kalit sifatida oʻzgaruvchan obyektni (masalan, roʻyxatni) ishlatish — obyekt oʻzgarsa, xesh ham oʻzgaradi va element “yoʻqoladi”.',
      practice: 'h(k) = k mod 7 bilan 15, 22, 8, 29 kalitlari qaysi kataklarga tushadi? Qaysilarida kolliziya bor?',
    }),
    M4: topic({
      intro: '<p>Bu mavzuda toʻrt saralash algoritmi solishtiriladi. <strong>Piramidal (heapsort)</strong> — maks-uyum yordamida, O(n log n), qoʻshimcha xotirasiz. <strong>Shell saralashi</strong> — qoʻyish usulining kamayuvchi qadamli (gap) varianti. <strong>Pufaksimon</strong> — qoʻshni elementlarni almashtirish, O(n²). <strong>Xoar (tez saralash, quicksort)</strong> — tayanch element atrofida boʻlish, oʻrtacha O(n log n), eng yomon holatda O(n²).</p>',
      points: [
        'Uyum (heap): har bir tugun farzandlaridan katta; massivda i-tugunning farzandlari 2i+1 va 2i+2.',
        'Shell: qadamlar ketma-ketligi (masalan, n/2, n/4, …, 1) samaradorlikni belgilaydi.',
        'Quicksort tayanch elementni tasodifiy tanlash eng yomon holat ehtimolini kamaytiradi.',
        'Barqarorlik: pufaksimon va qoʻyish barqaror, heapsort va quicksort — yoʻq.',
      ],
      pyCode: 'def quicksort(a):\n    if len(a) <= 1:\n        return a\n    pivot = a[len(a) // 2]\n    less = [x for x in a if x < pivot]\n    equal = [x for x in a if x == pivot]\n    greater = [x for x in a if x > pivot]\n    return quicksort(less) + equal + quicksort(greater)\n\n\nprint(quicksort([33, 10, 55, 71, 29, 3, 18]))\n',
      javaCode: J('    static void shellSort(int[] a) {\n        for (int gap = a.length / 2; gap > 0; gap /= 2)\n            for (int i = gap; i < a.length; i++) {\n                int x = a[i], j = i;\n                while (j >= gap && a[j - gap] > x) { a[j] = a[j - gap]; j -= gap; }\n                a[j] = x;\n            }\n    }\n\n    public static void main(String[] args) {\n        int[] a = {33, 10, 55, 71, 29, 3, 18};\n        shellSort(a);\n        System.out.println(Arrays.toString(a));\n    }'),
      mistake: 'quicksort’da doim birinchi elementni tayanch qilib olish — allaqachon saralangan massivda O(n²) ga olib keladi.',
      practice: '[5, 3, 8, 1] massivni pufaksimon usulda saralashda har bir oʻtishdan keyingi holatini yozing.',
      links: [LINKS.visualgo],
    }),
    M5: topic({
      intro: '<p><strong>Parallel saralash</strong> bir vaqtda koʻp taqqoslashni bajaradi. <strong>Juft-toq saralash</strong> (odd-even transposition) navbatma-navbat juft va toq indeksli qoʻshni juftlarni solishtiradi — n protsessorda n fazada tugaydi. <strong>Betcher</strong> tarmoqlari (bitonik, juft-toq birlashtirish) va <strong>Bouz–Nelson</strong> usuli saralash tarmoqlarini qurish uchun ishlatiladi. Mavzuning ikkinchi qismi — <strong>dinamik tuzilmalar</strong>: navbat (FIFO), stek (LIFO), dek (ikki tomonlama navbat).</p>',
      points: [
        'Stek amallari: push, pop, peek — barchasi O(1).',
        'Navbat amallari: enqueue (oxiriga), dequeue (boshidan).',
        'Dek: ikkala uchidan qoʻshish va olish.',
        'Massiv asosida: halqa bufer (circular buffer); koʻrsatkichlar asosida: bogʻlangan roʻyxat.',
      ],
      pyCode: 'def odd_even_sort(a):\n    n = len(a)\n    for phase in range(n):\n        start = phase % 2          # juft yoki toq faza\n        for i in range(start, n - 1, 2):\n            if a[i] > a[i + 1]:\n                a[i], a[i + 1] = a[i + 1], a[i]\n    return a\n\n\nprint(odd_even_sort([6, 2, 9, 4, 1, 7]))\n\nfrom collections import deque\nq = deque([1, 2, 3])\nq.appendleft(0)   # dek: chapga qoʻshish\nq.pop()           # oʻngdan olish\nprint(list(q))    # [0, 1, 2]\n',
      javaCode: J('    public static void main(String[] args) {\n        Deque<Integer> stack = new ArrayDeque<>();\n        stack.push(1); stack.push(2); stack.push(3);\n        System.out.println(stack.pop());   // 3 (LIFO)\n\n        Queue<Integer> queue = new ArrayDeque<>();\n        queue.add(1); queue.add(2); queue.add(3);\n        System.out.println(queue.poll());  // 1 (FIFO)\n    }'),
      mistake: 'Java’da stek uchun eskirgan <code>Stack</code> sinfini ishlatish — oʻrniga <code>ArrayDeque</code> tavsiya etiladi.',
      practice: '[4, 3, 2, 1] massiv juft-toq saralashda nechta fazada saralanadi?',
    }),
    M6: topic({
      intro: '<p><strong>Bogʻlangan roʻyxat</strong> elementlari xotirada ketma-ket emas, har bir tugun keyingisiga (ikki bogʻlamlida — oldingisiga ham) havola saqlaydi. <strong>Graf</strong> G = (V, E) — uchlar va qirralar toʻplami. Kompyuterda graf qoʻshnilik matritsasi yoki qoʻshnilik roʻyxati bilan ifodalanadi. <strong>Deykstra</strong> algoritmi bitta manbadan eng qisqa yoʻllarni, <strong>Floyd</strong> algoritmi barcha juftliklar orasidagi masofalarni topadi.</p>',
      points: [
        'Roʻyxatga oʻrtasiga qoʻshish O(1) (tugun maʼlum boʻlsa), lekin indeks boʻyicha murojaat O(n).',
        'Graf turlari: yoʻnaltirilgan/yoʻnaltirilmagan, vaznli/vaznsiz, bogʻlamli/bogʻlamsiz.',
        'Deykstra: manfiy vazn boʻlmasa ishlaydi, O((V + E) log V).',
        'Floyd–Uorshell: dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j]), O(V³).',
      ],
      pyCode: 'INF = float("inf")\ngraph = [\n    [0, 3, INF, 7],\n    [8, 0, 2, INF],\n    [5, INF, 0, 1],\n    [2, INF, INF, 0],\n]\nn = len(graph)\ndist = [row[:] for row in graph]\nfor k in range(n):\n    for i in range(n):\n        for j in range(n):\n            if dist[i][k] + dist[k][j] < dist[i][j]:\n                dist[i][j] = dist[i][k] + dist[k][j]\nfor row in dist:\n    print(row)\n',
      javaCode: J('    static class Node {\n        int value;\n        Node next;\n        Node(int value, Node next) { this.value = value; this.next = next; }\n    }\n\n    public static void main(String[] args) {\n        Node head = null;\n        for (int v : new int[] {3, 2, 1}) head = new Node(v, head); // boshiga qoʻshish\n        for (Node cur = head; cur != null; cur = cur.next) System.out.print(cur.value + " ");\n        System.out.println(); // 1 2 3\n    }'),
      mistake: 'bogʻlangan roʻyxatdan tugunni oʻchirganda oldingi tugunning <code>next</code> havolasini yangilashni unutish — roʻyxat uzilib qoladi.',
      practice: '4 uchli grafni qoʻshnilik matritsasi va qoʻshnilik roʻyxati koʻrinishida yozing; qaysi biri siyrak graf uchun tejamliroq?',
      links: [LINKS.visualgo],
    }),
    M7: topic({
      intro: '<p><strong>Daraxt</strong> — sikl boʻlmagan bogʻlamli graf. <strong>Binar qidiruv daraxti</strong> (BST)da har bir tugunning chap qism daraxtidagi kalitlar kichik, oʻng qism daraxtidagilari katta. Qidirish, qoʻshish va oʻchirish daraxt balandligiga proporsional: muvozanatli daraxtda O(log n), “bir tomonga oqqan” daraxtda O(n). Shu sababli <strong>muvozanatlashtirilgan daraxtlar</strong> (AVL, qizil-qora) ishlatiladi.</p>',
      points: [
        'Atamalar: ildiz, barg, ota, farzand, balandlik.',
        'Aylanib chiqish: prefiks (pre-order), infiks (in-order), postfiks (post-order).',
        'BST ni infiks tartibda aylanish kalitlarni saralangan holda beradi — <strong>daraxt yordamida saralash</strong>.',
        'Java <code>TreeMap</code>/<code>TreeSet</code> — qizil-qora daraxt asosidagi tayyor tuzilmalar.',
      ],
      pyCode: 'class Node:\n    def __init__(self, key):\n        self.key, self.left, self.right = key, None, None\n\n\ndef insert(root, key):\n    if root is None:\n        return Node(key)\n    if key < root.key:\n        root.left = insert(root.left, key)\n    else:\n        root.right = insert(root.right, key)\n    return root\n\n\ndef inorder(root, out):\n    if root:\n        inorder(root.left, out)\n        out.append(root.key)\n        inorder(root.right, out)\n    return out\n\n\nroot = None\nfor k in [50, 30, 70, 20, 40, 60, 80]:\n    root = insert(root, k)\nprint(inorder(root, []))  # saralangan: [20, 30, 40, 50, 60, 70, 80]\n',
      javaCode: J('    public static void main(String[] args) {\n        TreeSet<Integer> set = new TreeSet<>(List.of(50, 30, 70, 20, 40, 60, 80));\n        System.out.println(set);                 // saralangan tartibda\n        System.out.println(set.ceiling(45));     // 45 dan katta yoki teng eng kichigi: 50\n        System.out.println(set.headSet(40));     // 40 dan kichiklari: [20, 30]\n    }'),
      mistake: 'saralangan maʼlumotni oddiy BST ga ketma-ket qoʻshish — daraxt roʻyxatga aylanib, barcha amallar O(n) boʻladi.',
      practice: '[8, 3, 10, 1, 6, 14] kalitlarini BST ga qoʻshing va prefiks tartibda aylanib chiqish natijasini yozing.',
    }),
    A1: practical({
      goal: '<p>Algoritm xossalarini aniq misolda koʻrish: Evklid algoritmi har doim cheklangan qadamda tugashini va qadamlar soni qanday oʻzgarishini kuzatish.</p>',
      steps: ['Algoritmni blok-sxema koʻrinishida chizing.', 'Qadamlar sonini sanaydigan dastur yozing (topshiriq quyida).', 'Qoʻshni Fibonachchi sonlari (832040 va 514229) uchun qadamlar soni nega eng koʻp boʻlishini izohlang (Lame teoremasi).'],
      refs: [task('MTA-A1')],
      check: 'Algoritmning qaysi xossasi uning abadiy davom etmasligini kafolatlaydi?',
    }),
    A2: practical({
      goal: '<p>Rekursiv algoritm murakkabligini hisoblash: oddiy rekursiv Fibonachchi O(φⁿ), xotirali variant O(n).</p>',
      steps: ['Avval memoizatsiyasiz rekursiv versiyani yozib, n = 35 da vaqtni oʻlchang.', 'Hisoblangan qiymatlarni saqlang va vaqtni qayta oʻlchang.', 'Topshiriqni topshiring (quyida).'],
      refs: [task('MTA-A2')],
      check: 'fib(5) ni oddiy rekursiya bilan hisoblaganda fib(2) necha marta chaqiriladi? (3 marta.)',
    }),
    A3: practical({
      goal: '<p>Izlash algoritmlari: binar izlashning pastki/yuqori chegara variantlari.</p>',
      steps: ['lower_bound: x dan kichik boʻlmagan birinchi element oʻrni.', 'upper_bound: x dan katta birinchi element oʻrni.', 'Uchrashlar soni = upper − lower.'],
      refs: [task('MTA-A3')],
      check: 'Python bisect modulidagi bisect_left va bisect_right qaysi chegaralarga mos keladi?',
      links: [LINKS.visualgo],
    }),
    A4: practical({
      goal: '<p>Piramidal saralashni qoʻlda amalga oshirish va murakkabligini tahlil qilish.</p>',
      steps: ['sift_down (elakdan oʻtkazish) funksiyasini yozing.', 'Massivdan uyum quring: n/2−1 dan 0 gacha sift_down.', 'Ildizni oxirgi element bilan almashtirib, uyum oʻlchamini kamaytiring.'],
      refs: [task('MTA-A4')],
      check: 'Uyum qurish nega O(n log n) emas, balki O(n) vaqt oladi?',
    }),
    A5: practical({
      goal: '<p>Pufaksimon saralash va inversiyalar tushunchasi. Almashtirishlar soni saralanmaganlik darajasini oʻlchaydi.</p>',
      steps: ['Pufaksimon saralashni yozing va almashtirishlarni sanang.', 'Natijani inversiyalar sonini toʻgʻridan-toʻgʻri sanash bilan solishtiring.', 'Birlashtirish (merge) saralashi yordamida inversiyalarni O(n log n) da sanash gʻoyasini oʻrganing.'],
      refs: [task('MTA-A5')],
      check: 'n elementli massivda inversiyalar soni eng koʻpi bilan nechta boʻladi? (n(n−1)/2.)',
    }),
    A6: practical({
      goal: '<p>Dinamik tuzilmalar: stekni massiv va roʻyxat asosida yaratish; amaliy masala — qavslar muvozanati.</p>',
      steps: ['Stek amallarini (push, pop, empty) tushuntiring.', 'Ochuvchi qavsni stekka qoʻying, yopuvchida tepadagi bilan solishtiring.', 'Oxirida stek boʻsh boʻlishi kerak.'],
      refs: [task('MTA-A6')],
      check: 'Navbat (queue) bilan bu masalani yechib boʻladimi? Nega?',
    }),
    A7: practical({
      goal: '<p>Graflarda optimallashtirish: eng qisqa yoʻl masalasi.</p>',
      steps: ['Grafni qoʻshnilik roʻyxati bilan saqlang.', 'Masofalar massivini cheksizlik bilan toʻldiring, manbani 0 qiling.', 'Ustuvor navbatdan eng yaqin uchni olib, qoʻshnilarini “boʻshashtiring” (relaxation).'],
      refs: [task('MTA-A7')],
      check: 'Nega Deykstra algoritmi manfiy vaznli qirralar bilan notoʻgʻri ishlashi mumkin?',
      links: [LINKS.visualgo],
    }),
    A8: practical({
      goal: '<p>Daraxtsimon tuzilmalar: Fenvik daraxti va kesmalar daraxti oraliq soʻrovlarini tez bajaradi.</p>',
      theory: '<p>Fenvik daraxtida t[i] katakchasi a[i − lowbit(i) + 1 .. i] yigʻindisini saqlaydi, bunda lowbit(i) = i &amp; (−i). Kesmalar daraxti esa minimum, maksimum kabi boshqa amallarni ham qoʻllab-quvvatlaydi.</p>',
      steps: ['add va prefix funksiyalarini yozing.', 'Oraliq yigʻindi = prefix(r) − prefix(l − 1).', 'Tez kiritish-chiqarishdan foydalaning.'],
      refs: [task('MTA-A8')],
      check: 'lowbit(12) nimaga teng? (4.)',
    }),
    MT1: independent({
      goal: '<p>Maʼruza va amaliy mashgʻulot mavzularini mustahkamlash (30 soat).</p>',
      tasks: [
        'Har bir maʼruza boʻyicha qisqa konspekt tuzing: taʼriflar, murakkablik, afzallik va kamchiliklar.',
        'Saralash algoritmlari jadvalini toʻldiring: eng yaxshi/oʻrtacha/eng yomon murakkablik, xotira, barqarorlik.',
        'VisuAlgo saytida kamida beshta algoritmni qadamma-qadam kuzating va kuzatuvlaringizni yozing.',
      ],
      deliverable: '<p>Konspekt va toʻldirilgan jadval (PDF yoki qoʻlyozma surati).</p>',
      links: [LINKS.visualgo],
    }),
    MT2: independent({
      goal: '<p>Mustaqil ish va uy vazifalari (30 soat). Sillabus talabiga koʻra topshiriqlarning uchdan bir qismi kichik guruhda bajariladi.</p>',
      tasks: [
        'Tanlangan tilda (Python yoki Java) quyidagilarni amalga oshiring: dinamik massiv, bir bogʻlamli roʻyxat, stek, navbat.',
        'Guruh bilan: Floyd–Uorshell algoritmini shahar xaritasi (kamida 6 ta nuqta) misolida qoʻllang.',
        'Amaliy mashgʻulotlardagi topshiriqlardan kamida oltitasini platformada muvaffaqiyatli topshiring.',
      ],
      deliverable: '<p>Dastur kodlari (izohlar bilan) va guruh ishi boʻyicha qisqa hisobot.</p>',
    }),
  },
}
