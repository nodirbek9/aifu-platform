// KOT1306 Kompyuter tarmoqlari — lectures, practicals (coding tasks for computable topics,
// quizzes for configuration/lab topics), independent-study briefs.
// Task outputs stay ASCII: the Java sandbox's stdout charset follows the container locale.
import { LINKS, independent, practical, quiz, task, topic } from './_blocks.mjs'

const J = (body, imports = 'import java.util.*;') => `${imports}\n\npublic class Main {\n${body}\n}\n`
const IO = 'import java.io.*;\nimport java.util.*;'

// Shared Java helpers for IPv4 tasks.
const IP_JAVA = '    static long ip(String s) {\n        String[] p = s.split("\\\\.");\n        long v = 0;\n        for (String x : p) v = (v << 8) | Integer.parseInt(x);\n        return v;\n    }\n\n    static String str(long v) {\n        return ((v >> 24) & 255) + "." + ((v >> 16) & 255) + "." + ((v >> 8) & 255) + "." + (v & 255);\n    }\n\n    static long mask(int len) {\n        return len == 0 ? 0 : (0xFFFFFFFFL << (32 - len)) & 0xFFFFFFFFL;\n    }\n'

const tasks = [
  {
    code: 'KOT-A4',
    title: 'Raqamli kodlash: NRZ, NRZI va Manchester',
    description: '<p>Bitlar ketma-ketligini liniya signaliga aylantiring. Signal darajalari <code>+</code> (yuqori) va <code>-</code> (past) bilan belgilanadi.</p><ul><li><strong>NRZ</strong> (NRZ-L): 1 → <code>+</code>, 0 → <code>-</code>.</li><li><strong>NRZI</strong>: boshlangʻich daraja <code>-</code>; 1 kelganda daraja almashadi, 0 da saqlanadi.</li><li><strong>MANCHESTER</strong> (IEEE 802.3 kelishuvi): har bir bit ikki yarim davr; 0 → <code>+-</code> (yuqoridan pastga), 1 → <code>-+</code> (pastdan yuqoriga).</li></ul>',
    instructions: 'Kirish: sxema nomi va bitlar satri (boʻshliq bilan). Chiqish: signal satri.',
    constraints: 'Bitlar soni 1–1000.',
    python: {
      starter: 'scheme, bits = input().split()\n# ...\n',
      solution: 'scheme, bits = input().split()\nout = []\nif scheme == "NRZ":\n    out = ["+" if b == "1" else "-" for b in bits]\nelif scheme == "NRZI":\n    level = "-"\n    for b in bits:\n        if b == "1":\n            level = "+" if level == "-" else "-"\n        out.append(level)\nelse:\n    out = ["-+" if b == "1" else "+-" for b in bits]\nprint("".join(out))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        String scheme = in.next(), bits = in.next();\n        // ...\n    }'),
      solution: J('    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        String scheme = in.next(), bits = in.next();\n        StringBuilder out = new StringBuilder();\n        char level = \'-\';\n        for (char b : bits.toCharArray()) {\n            switch (scheme) {\n                case "NRZ" -> out.append(b == \'1\' ? \'+\' : \'-\');\n                case "NRZI" -> {\n                    if (b == \'1\') level = level == \'-\' ? \'+\' : \'-\';\n                    out.append(level);\n                }\n                default -> out.append(b == \'1\' ? "-+" : "+-");\n            }\n        }\n        System.out.println(out);\n    }'),
    },
    tests: [['NRZ 1011', '+-++'], ['NRZI 1011', '++-+'], ['MANCHESTER 10', '-++-'], ['MANCHESTER 1100', '-+-++-+-'], ['NRZI 0000', '----']],
  },
  {
    code: 'KOT-A7',
    title: 'IPv4 quyi tarmoq kalkulyatori',
    description: '<p>CIDR koʻrinishidagi manzil (masalan, <code>192.168.10.77/26</code>) boʻyicha tarmoq manzili, maska, keng tarqatish (broadcast) manzili, birinchi va oxirgi xost hamda xostlar sonini hisoblang.</p><p>Tarmoq = IP AND maska; broadcast = tarmoq OR (NOT maska); xostlar soni = 2^(32 − prefiks) − 2.</p>',
    instructions: 'Kirish: IP/prefiks. Chiqish (6 satr): Tarmoq, Maska, Broadcast, Birinchi xost, Oxirgi xost, Xostlar soni — namunadagi formatda.',
    constraints: '1 ≤ prefiks ≤ 30.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'cidr = input().strip()\n# ...\n',
      solution: 'def to_int(s):\n    a, b, c, d = map(int, s.split("."))\n    return (a << 24) | (b << 16) | (c << 8) | d\n\n\ndef to_str(v):\n    return ".".join(str((v >> s) & 255) for s in (24, 16, 8, 0))\n\n\naddr, prefix = input().strip().split("/")\nprefix = int(prefix)\nmask = (0xFFFFFFFF << (32 - prefix)) & 0xFFFFFFFF\nnet = to_int(addr) & mask\nbroadcast = net | (~mask & 0xFFFFFFFF)\nprint("Tarmoq:", to_str(net))\nprint("Maska:", to_str(mask))\nprint("Broadcast:", to_str(broadcast))\nprint("Birinchi xost:", to_str(net + 1))\nprint("Oxirgi xost:", to_str(broadcast - 1))\nprint("Xostlar soni:", 2 ** (32 - prefix) - 2)\n',
    },
    java: {
      starter: J(IP_JAVA + '\n    public static void main(String[] args) {\n        String[] parts = new Scanner(System.in).next().split("/");\n        // ...\n    }'),
      solution: J(IP_JAVA + '\n    public static void main(String[] args) {\n        String[] parts = new Scanner(System.in).next().split("/");\n        int prefix = Integer.parseInt(parts[1]);\n        long m = mask(prefix);\n        long net = ip(parts[0]) & m;\n        long bc = net | (~m & 0xFFFFFFFFL);\n        System.out.println("Tarmoq: " + str(net));\n        System.out.println("Maska: " + str(m));\n        System.out.println("Broadcast: " + str(bc));\n        System.out.println("Birinchi xost: " + str(net + 1));\n        System.out.println("Oxirgi xost: " + str(bc - 1));\n        System.out.println("Xostlar soni: " + ((1L << (32 - prefix)) - 2));\n    }'),
    },
    tests: [
      ['192.168.10.77/26', 'Tarmoq: 192.168.10.64\nMaska: 255.255.255.192\nBroadcast: 192.168.10.127\nBirinchi xost: 192.168.10.65\nOxirgi xost: 192.168.10.126\nXostlar soni: 62'],
      ['10.0.0.1/8', 'Tarmoq: 10.0.0.0\nMaska: 255.0.0.0\nBroadcast: 10.255.255.255\nBirinchi xost: 10.0.0.1\nOxirgi xost: 10.255.255.254\nXostlar soni: 16777214'],
      ['172.16.5.200/30', 'Tarmoq: 172.16.5.200\nMaska: 255.255.255.252\nBroadcast: 172.16.5.203\nBirinchi xost: 172.16.5.201\nOxirgi xost: 172.16.5.202\nXostlar soni: 2'],
      ['200.100.50.25/24', 'Tarmoq: 200.100.50.0\nMaska: 255.255.255.0\nBroadcast: 200.100.50.255\nBirinchi xost: 200.100.50.1\nOxirgi xost: 200.100.50.254\nXostlar soni: 254'],
      ['172.31.200.9/12', 'Tarmoq: 172.16.0.0\nMaska: 255.240.0.0\nBroadcast: 172.31.255.255\nBirinchi xost: 172.16.0.1\nOxirgi xost: 172.31.255.254\nXostlar soni: 1048574'],
    ],
  },
  {
    code: 'KOT-A8',
    title: 'Marshrutlash jadvali: eng uzun prefiks boʻyicha tanlash',
    description: '<p>Marshrutizator paketni yuborishda manzilga mos keladigan marshrutlar orasidan <strong>eng uzun prefiksli</strong> (eng aniq) marshrutni tanlaydi. <code>0.0.0.0/0</code> — standart marshrut (default route). Mos marshrut boʻlmasa paket tashlab yuboriladi.</p>',
    instructions: 'Kirish: n; n ta satrda tarmoq/prefiks keyingi_sakrash; q; q ta manzil. Chiqish: har bir manzil uchun keyingi sakrash nomi yoki DROP.',
    constraints: '1 ≤ n, q ≤ 1000.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\n\nlines = sys.stdin.read().split("\\n")\n# ...\n',
      solution: 'import sys\n\n\ndef to_int(s):\n    a, b, c, d = map(int, s.split("."))\n    return (a << 24) | (b << 16) | (c << 8) | d\n\n\ndata = sys.stdin.read().split()\nn = int(data[0])\nroutes = []\npos = 1\nfor _ in range(n):\n    net, length = data[pos].split("/")\n    length = int(length)\n    mask = (0xFFFFFFFF << (32 - length)) & 0xFFFFFFFF if length else 0\n    routes.append((to_int(net) & mask, mask, length, data[pos + 1]))\n    pos += 2\nq = int(data[pos])\nfor addr in data[pos + 1:pos + 1 + q]:\n    v = to_int(addr)\n    best = None\n    for net, mask, length, hop in routes:\n        if v & mask == net and (best is None or length > best[0]):\n            best = (length, hop)\n    print(best[1] if best else "DROP")\n',
    },
    java: {
      starter: J(IP_JAVA + '\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        // ...\n    }'),
      solution: J(IP_JAVA + '\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        long[] nets = new long[n], masks = new long[n];\n        int[] lens = new int[n];\n        String[] hops = new String[n];\n        for (int i = 0; i < n; i++) {\n            String[] p = in.next().split("/");\n            lens[i] = Integer.parseInt(p[1]);\n            masks[i] = mask(lens[i]);\n            nets[i] = ip(p[0]) & masks[i];\n            hops[i] = in.next();\n        }\n        int q = in.nextInt();\n        for (int k = 0; k < q; k++) {\n            long v = ip(in.next());\n            int best = -1;\n            for (int i = 0; i < n; i++)\n                if ((v & masks[i]) == nets[i] && (best < 0 || lens[i] > lens[best])) best = i;\n            System.out.println(best < 0 ? "DROP" : hops[best]);\n        }\n    }'),
    },
    tests: [
      ['4\n0.0.0.0/0 ISP\n10.0.0.0/8 R1\n10.1.0.0/16 R2\n10.1.2.0/24 R3\n4\n10.1.2.5\n10.1.9.9\n10.200.0.1\n8.8.8.8', 'R3\nR2\nR1\nISP'],
      ['1\n192.168.0.0/16 A\n2\n172.16.0.1\n192.168.255.255', 'DROP\nA'],
      ['3\n172.16.0.0/12 CORE\n172.16.4.0/22 LAB\n172.16.6.128/25 SRV\n3\n172.16.6.200\n172.16.6.100\n172.20.0.1', 'SRV\nLAB\nCORE'],
    ],
  },
  {
    code: 'KOT-A9',
    title: 'HTTP soʻrovini tahlil qilish',
    description: '<p>Soket orqali qabul qilingan xom HTTP soʻrovini tahlil qiluvchi server qismini yozing. Birinchi satr — <em>soʻrov satri</em>: metod, yoʻl (soʻrov parametrlari bilan) va versiya. Keyin boʻsh satrgacha <em>sarlavhalar</em> keladi (<code>Nom: qiymat</code>; sarlavha nomlari katta-kichik harfga sezgir emas).</p>',
    instructions: 'Kirish: HTTP soʻrov matni. Chiqish (6 satr): Metod, Yo\'l, So\'rov (parametrlar yoki -), Versiya, Host (yoki -), Sarlavhalar (soni).',
    constraints: 'Sarlavhalar soni 0–50.',
    python: {
      starter: 'import sys\n\nlines = sys.stdin.read().splitlines()\nmethod, target, version = lines[0].split()\n# ...\n',
      solution: 'import sys\n\nlines = sys.stdin.read().splitlines()\nmethod, target, version = lines[0].split()\npath, _, query = target.partition("?")\nheaders = {}\nfor line in lines[1:]:\n    if not line.strip():\n        break\n    name, _, value = line.partition(":")\n    headers[name.strip().lower()] = value.strip()\nprint("Metod:", method)\nprint("Yo\'l:", path)\nprint("So\'rov:", query or "-")\nprint("Versiya:", version)\nprint("Host:", headers.get("host", "-"))\nprint("Sarlavhalar:", len(headers))\n',
    },
    java: {
      starter: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String[] requestLine = br.readLine().trim().split(" ");\n        // ...\n    }', IO),
      solution: J('    public static void main(String[] args) throws IOException {\n        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\n        String[] requestLine = br.readLine().trim().split(" ");\n        String target = requestLine[1];\n        int qm = target.indexOf(\'?\');\n        String path = qm < 0 ? target : target.substring(0, qm);\n        String query = qm < 0 || qm == target.length() - 1 ? "-" : target.substring(qm + 1);\n        Map<String, String> headers = new LinkedHashMap<>();\n        String line;\n        while ((line = br.readLine()) != null && !line.isBlank()) {\n            int colon = line.indexOf(\':\');\n            headers.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());\n        }\n        System.out.println("Metod: " + requestLine[0]);\n        System.out.println("Yo\'l: " + path);\n        System.out.println("So\'rov: " + query);\n        System.out.println("Versiya: " + requestLine[2]);\n        System.out.println("Host: " + headers.getOrDefault("host", "-"));\n        System.out.println("Sarlavhalar: " + headers.size());\n    }', IO),
    },
    tests: [
      ['GET /courses?id=5&lang=uz HTTP/1.1\nHost: lms.example.uz\nUser-Agent: curl/8.0\nAccept: */*\n', 'Metod: GET\nYo\'l: /courses\nSo\'rov: id=5&lang=uz\nVersiya: HTTP/1.1\nHost: lms.example.uz\nSarlavhalar: 3'],
      ['POST /api/login HTTP/1.1\nhost: localhost:8080\nContent-Type: application/json\nContent-Length: 27\n\n{"username":"s","pw":"x"}', 'Metod: POST\nYo\'l: /api/login\nSo\'rov: -\nVersiya: HTTP/1.1\nHost: localhost:8080\nSarlavhalar: 3'],
      ['GET / HTTP/1.0', 'Metod: GET\nYo\'l: /\nSo\'rov: -\nVersiya: HTTP/1.0\nHost: -\nSarlavhalar: 0'],
    ],
  },
  {
    code: 'KOT-A14',
    title: 'Brandmauer qoidalarini tekshirish',
    description: '<p>Brandmauer (firewall) paketni qoidalar roʻyxati boʻyicha yuqoridan pastga tekshiradi va <strong>birinchi mos kelgan</strong> qoidani qoʻllaydi. Hech biri mos kelmasa — standart siyosat: taqiqlash.</p><p>Qoida: <code>AMAL protokol port manba_tarmogʻi</code>; protokol <code>tcp</code>, <code>udp</code> yoki <code>any</code>; port raqam yoki <code>*</code> (istalgan); manba CIDR koʻrinishida.</p>',
    instructions: 'Kirish: n; n ta qoida; m; m ta paket (protokol port manba_ip). Chiqish: har bir paket uchun ALLOW k yoki DENY k (k — qoida raqami, 1 dan) yoki DENY default.',
    constraints: '1 ≤ n, m ≤ 500.',
    difficulty: 'MEDIUM',
    python: {
      starter: 'import sys\n\ndata = sys.stdin.read().split()\n# ...\n',
      solution: 'import sys\n\n\ndef to_int(s):\n    a, b, c, d = map(int, s.split("."))\n    return (a << 24) | (b << 16) | (c << 8) | d\n\n\ndef in_cidr(addr, cidr):\n    net, length = cidr.split("/")\n    length = int(length)\n    mask = (0xFFFFFFFF << (32 - length)) & 0xFFFFFFFF if length else 0\n    return to_int(addr) & mask == to_int(net) & mask\n\n\ndata = sys.stdin.read().split()\nn = int(data[0])\nrules = [data[1 + 4 * i:5 + 4 * i] for i in range(n)]\npos = 1 + 4 * n\nm = int(data[pos])\nfor k in range(m):\n    proto, port, src = data[pos + 1 + 3 * k:pos + 4 + 3 * k]\n    verdict = "DENY default"\n    for i, (action, r_proto, r_port, r_src) in enumerate(rules, 1):\n        if r_proto in ("any", proto) and r_port in ("*", port) and in_cidr(src, r_src):\n            verdict = f"{action} {i}"\n            break\n    print(verdict)\n',
    },
    java: {
      starter: J(IP_JAVA + '\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        // ...\n    }'),
      solution: J(IP_JAVA + '\n    static boolean inCidr(String addr, String cidr) {\n        String[] p = cidr.split("/");\n        long m = mask(Integer.parseInt(p[1]));\n        return (ip(addr) & m) == (ip(p[0]) & m);\n    }\n\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int n = in.nextInt();\n        String[][] rules = new String[n][];\n        for (int i = 0; i < n; i++) rules[i] = new String[] {in.next(), in.next(), in.next(), in.next()};\n        int m = in.nextInt();\n        for (int k = 0; k < m; k++) {\n            String proto = in.next(), port = in.next(), src = in.next();\n            String verdict = "DENY default";\n            for (int i = 0; i < n; i++) {\n                String[] r = rules[i];\n                if ((r[1].equals("any") || r[1].equals(proto)) && (r[2].equals("*") || r[2].equals(port)) && inCidr(src, r[3])) {\n                    verdict = r[0] + " " + (i + 1);\n                    break;\n                }\n            }\n            System.out.println(verdict);\n        }\n    }'),
    },
    tests: [
      [
        '4\nALLOW tcp 22 10.0.0.0/8\nDENY tcp 22 0.0.0.0/0\nALLOW tcp 443 0.0.0.0/0\nALLOW udp 53 192.168.1.0/24\n6\ntcp 22 10.5.5.5\ntcp 22 8.8.8.8\ntcp 443 8.8.8.8\nudp 53 192.168.1.20\nudp 53 192.168.2.20\ntcp 80 10.0.0.1',
        'ALLOW 1\nDENY 2\nALLOW 3\nALLOW 4\nDENY default\nDENY default',
      ],
      ['2\nALLOW any * 127.0.0.0/8\nDENY any * 0.0.0.0/0\n2\nudp 9999 127.0.0.1\ntcp 443 1.1.1.1', 'ALLOW 1\nDENY 2'],
    ],
  },
]

const quizzes = [
  {
    code: 'KOT-Q-A1',
    title: 'Tarmoq qurilmalari',
    questions: [
      { type: 'SINGLE', prompt: 'Kadrni faqat MAC manzili boʻyicha kerakli portga yuboruvchi qurilma?', options: ['Konsentrator (hub)', 'Kommutator (switch)', 'Takrorlagich (repeater)', 'Modem'], correct: [1] },
      { type: 'SINGLE', prompt: 'Signalni kuchaytirib, masofani oshiradigan, lekin kadrlarni tahlil qilmaydigan qurilma?', options: ['Takrorlagich', 'Marshrutizator', 'Koʻprik', 'Brandmauer'], correct: [0] },
      { type: 'SINGLE', prompt: 'Turli tarmoqlar orasida IP manzil boʻyicha paketlarni yoʻnaltiruvchi qurilma?', options: ['Hub', 'Switch', 'Router', 'Tarmoq adapteri'], correct: [2] },
      { type: 'TF', prompt: 'Konsentrator (hub) qabul qilgan signalni barcha portlarga tarqatadi.', answer: true },
      { type: 'SINGLE', prompt: 'Koʻprik (bridge) OSI modelining qaysi pogʻonasida ishlaydi?', options: ['Fizik', 'Kanal', 'Tarmoq', 'Transport'], correct: [1] },
    ],
  },
  {
    code: 'KOT-Q-A2',
    title: 'Tarmoq topologiyalari',
    questions: [
      { type: 'SINGLE', prompt: 'Barcha qurilmalar bitta markaziy qurilmaga ulangan topologiya?', options: ['Shina', 'Yulduz', 'Halqa', 'Toʻliq bogʻlangan'], correct: [1] },
      { type: 'SINGLE', prompt: 'Yulduz topologiyasining asosiy kamchiligi?', options: ['Markaziy qurilma ishdan chiqsa, butun tarmoq ishlamaydi', 'Kabel juda kam ketadi', 'Qurilma qoʻshish qiyin', 'Kolliziyalar boʻlmaydi'], correct: [0] },
      { type: 'NUM', prompt: '6 ta tugunli toʻliq bogʻlangan (mesh) tarmoqda nechta aloqa liniyasi bor?', answer: 15, explanation: 'n(n − 1)/2 = 6·5/2 = 15.' },
      { type: 'SINGLE', prompt: 'Token Ring texnologiyasi qaysi mantiqiy topologiyaga asoslangan?', options: ['Shina', 'Halqa', 'Yulduz', 'Daraxt'], correct: [1] },
    ],
  },
  {
    code: 'KOT-Q-A3',
    title: 'Tarmoq emulyatorlari',
    questions: [
      { type: 'MULTI', prompt: 'Qaysilari tarmoqni modellashtirish yoki emulyatsiya vositalari?', options: ['Cisco Packet Tracer', 'GNS3', 'Wireshark', 'EVE-NG'], correct: [0, 1, 3], explanation: 'Wireshark — trafik analizatori, u tarmoqni modellashtirmaydi.' },
      { type: 'TF', prompt: 'Emulyatorda real qurilmalar sotib olmasdan tarmoq sxemasini sinab koʻrish mumkin.', answer: true },
      { type: 'SINGLE', prompt: 'Packet Tracer’da ikki kompyuter orasidagi aloqani tekshirish uchun odatda qaysi buyruq ishlatiladi?', options: ['ping', 'format', 'mkdir', 'chmod'], correct: [0] },
    ],
  },
  {
    code: 'KOT-Q-A5',
    title: 'Ethernet va paketlarni tahlil qilish',
    questions: [
      { type: 'NUM', prompt: 'MAC manzil necha bitdan iborat?', answer: 48 },
      { type: 'SINGLE', prompt: 'Ethernet kadrida xatolarni aniqlash uchun qaysi maydon ishlatiladi?', options: ['FCS (CRC)', 'TTL', 'Port', 'Checksum IP'], correct: [0] },
      { type: 'SINGLE', prompt: 'Fast Ethernet standart tezligi?', options: ['10 Mbit/s', '100 Mbit/s', '1 Gbit/s', '10 Gbit/s'], correct: [1] },
      { type: 'SINGLE', prompt: 'FF:FF:FF:FF:FF:FF manzili nimani anglatadi?', options: ['Keng tarqatish (broadcast)', 'Standart shlyuz', 'Loopback', 'Multicast guruhi'], correct: [0] },
      { type: 'TF', prompt: 'Ethernet klassik (hub asosidagi) tarmoqlarda kirish usuli CSMA/CD dir.', answer: true },
    ],
  },
  {
    code: 'KOT-Q-A6',
    title: 'Simsiz tarmoqlar va Wi-Fi',
    questions: [
      { type: 'SINGLE', prompt: 'Wi-Fi tarmoqlari qaysi IEEE standartlar oilasiga asoslangan?', options: ['802.3', '802.11', '802.15', '802.5'], correct: [1] },
      { type: 'SINGLE', prompt: 'Uy Wi-Fi routeri uchun hozirgi tavsiya etiladigan himoya protokoli?', options: ['WEP', 'WPA', 'WPA2/WPA3', 'Ochiq tarmoq'], correct: [2] },
      { type: 'SINGLE', prompt: 'SSID nima?', options: ['Simsiz tarmoq nomi', 'Shifrlash kaliti', 'MAC manzil', 'Kanal raqami'], correct: [0] },
      { type: 'MULTI', prompt: 'Wi-Fi qaysi chastota diapazonlarida ishlaydi?', options: ['2,4 GGs', '5 GGs', '6 GGs (Wi-Fi 6E)', '900 MGs (GSM)'], correct: [0, 1, 2] },
    ],
  },
  {
    code: 'KOT-Q-A10',
    title: 'Server operatsion tizimini oʻrnatish',
    questions: [
      { type: 'SINGLE', prompt: 'Serverga odatda qanday IP manzil beriladi?', options: ['Statik', 'DHCP orqali har safar yangi', 'Faqat IPv6 link-local', 'Hech qanday'], correct: [0], explanation: 'Mijozlar serverni doimiy manzil boʻyicha topishi uchun unga statik manzil beriladi.' },
      { type: 'TF', prompt: 'Server Core oʻrnatish varianti grafik interfeyssiz boʻlib, kamroq resurs sarflaydi.', answer: true },
      { type: 'SINGLE', prompt: 'Windows Server’da rollar va komponentlarni qoʻshish uchun asosiy vosita?', options: ['Server Manager', 'Paint', 'Task Scheduler', 'Notepad'], correct: [0] },
    ],
  },
  {
    code: 'KOT-Q-A11',
    title: 'DHCP va DNS xizmatlari',
    questions: [
      { type: 'SINGLE', prompt: 'DHCP jarayoni bosqichlarining toʻgʻri tartibi?', options: ['Discover → Offer → Request → Acknowledge', 'Request → Offer → Discover → Acknowledge', 'Offer → Discover → Acknowledge → Request', 'Discover → Request → Offer → Acknowledge'], correct: [0] },
      { type: 'SINGLE', prompt: 'Domen nomini IPv4 manzilga bogʻlovchi DNS yozuvi?', options: ['A', 'MX', 'CNAME', 'PTR'], correct: [0] },
      { type: 'SINGLE', prompt: 'Pochta serverini koʻrsatuvchi DNS yozuvi?', options: ['A', 'MX', 'NS', 'TXT'], correct: [1] },
      { type: 'SINGLE', prompt: 'DNS soʻrovlari odatda qaysi portdan foydalanadi?', options: ['53', '67', '80', '443'], correct: [0] },
      { type: 'TF', prompt: 'DHCP mijozga IP manzildan tashqari standart shlyuz va DNS server manzilini ham berishi mumkin.', answer: true },
    ],
  },
  {
    code: 'KOT-Q-A12',
    title: 'Foydalanuvchilar va vakolatlar',
    questions: [
      { type: 'SINGLE', prompt: 'Vakolatlarni har bir foydalanuvchiga emas, guruhlarga berish nima uchun qulay?', options: ['Boshqarish osonlashadi va xatolar kamayadi', 'Parollar kerak boʻlmaydi', 'Tarmoq tezlashadi', 'Disk joyi tejaladi'], correct: [0] },
      { type: 'TF', prompt: 'Kundalik ish uchun administrator hisobidan foydalanish xavfsizlik nuqtai nazaridan tavsiya etiladi.', answer: false },
      { type: 'SINGLE', prompt: 'Windows’da lokal foydalanuvchi va guruhlarni boshqarish konsoli?', options: ['lusrmgr.msc', 'regedit', 'calc', 'mspaint'], correct: [0] },
    ],
  },
  {
    code: 'KOT-Q-A13',
    title: 'Umumiy tarmoq resurslari',
    questions: [
      { type: 'SINGLE', prompt: 'Windows’da umumiy papkaga tarmoq orqali murojaat qilish koʻrinishi?', options: ['\\\\server\\papka', 'http:/papka', 'C:papka', 'ftp:papka'], correct: [0] },
      { type: 'SINGLE', prompt: 'Windows fayl almashish protokoli?', options: ['SMB', 'SMTP', 'SNMP', 'SSH'], correct: [0] },
      { type: 'TF', prompt: 'Umumiy papkada amal qiluvchi huquq — share va NTFS huquqlaridan eng cheklovchisi.', answer: true },
    ],
  },
  {
    code: 'KOT-Q-A15',
    title: 'Tarmoq nosozliklarini aniqlash',
    questions: [
      { type: 'SINGLE', prompt: 'Paket manzilga qaysi marshrutizatorlar orqali borishini koʻrsatuvchi Windows utilitasi?', options: ['tracert', 'ipconfig', 'netstat', 'hostname'], correct: [0] },
      { type: 'SINGLE', prompt: 'Kompyuterning IP manzili, maskasi va shlyuzini koʻrsatuvchi Windows buyrugʻi?', options: ['ipconfig', 'ping', 'nslookup', 'arp -d'], correct: [0] },
      { type: 'SINGLE', prompt: 'Kompyuter 169.254.x.x manzil olgan. Ehtimoliy sabab?', options: ['DHCP serverga ulanib boʻlmadi', 'DNS ishlamayapti', 'Brauzer eskirgan', 'Disk toʻlgan'], correct: [0], explanation: '169.254.0.0/16 — APIPA (link-local) manzillar; DHCP javob bermaganda OT ularni oʻzi tayinlaydi.' },
      { type: 'SINGLE', prompt: 'IP manzil boʻyicha ping ishlaydi, lekin domen nomi boʻyicha ishlamaydi. Qaysi xizmat muammoli?', options: ['DNS', 'DHCP', 'ARP', 'Ethernet'], correct: [0] },
      { type: 'SINGLE', prompt: 'Nosozlikni qidirishda OSI modeli boʻyicha pastdan yuqoriga borish nimadan boshlanadi?', options: ['Kabel va ulanishni tekshirishdan', 'Brauzerni qayta oʻrnatishdan', 'DNS keshini tozalashdan', 'Serverni qayta yuklashdan'], correct: [0] },
    ],
  },
]

export default {
  subject: 'KOT1306',
  cohort: 'DI-24-01-S3',
  tasks,
  quizzes,
  lessons: {
    M1: topic({
      intro: '<p><strong>Kompyuter tarmogʻi</strong> — maʼlumot va resurslarni almashish uchun aloqa kanallari orqali bogʻlangan kompyuterlar va qurilmalar majmui. Tarmoqlar 1960-yillardagi ARPANET dan boshlab bugungi global Internetgacha rivojlandi.</p>',
      points: [
        'Hududiy qamrov boʻyicha: PAN, LAN (lokal), MAN (shahar), WAN (global).',
        'Boshqaruv boʻyicha: bir rangli (peer-to-peer) va mijoz–server tarmoqlari.',
        'Uzatish muhiti boʻyicha: simli (mis kabel, optik tola) va simsiz (Wi-Fi, sotali aloqa).',
        'Tarixiy bosqichlar: ARPANET (1969), TCP/IP ga oʻtish (1983), World Wide Web (1989–1991).',
      ],
      mistake: 'Internet va Web ni bir narsa deb bilish. Internet — tarmoqlar tarmogʻi; Web esa uning ustida ishlaydigan xizmatlardan biri (HTTP).',
      practice: 'Universitet kampusidagi tarmoq qaysi turga kiradi? Uyingizdagi-chi?',
    }),
    M2: topic({
      intro: '<p>Tarmoq qurishda uning <strong>topologiyasi</strong> (qurilmalarning ulanish sxemasi), <strong>arxitekturasi</strong> va <strong>qurilmalari</strong> tanlanadi. Fizik topologiya kabellarning haqiqiy joylashuvini, mantiqiy topologiya esa maʼlumot qanday oqishini bildiradi.</p>',
      points: [
        'Topologiyalar: shina, yulduz, halqa, daraxt, toʻliq va qisman bogʻlangan (mesh).',
        'Qurilmalar: tarmoq adapteri, takrorlagich, konsentrator, koʻprik, kommutator, marshrutizator.',
        'Yulduz — bugungi LAN larda eng keng tarqalgan (markazda kommutator).',
      ],
      pyCode: '# n tugunli toʻliq bogʻlangan tarmoqdagi liniyalar soni\nfor n in (4, 6, 10, 50):\n    print(n, "tugun ->", n * (n - 1) // 2, "liniya")\n',
      javaCode: J('    public static void main(String[] args) {\n        // Yulduz topologiyasi: markaziy kommutator va unga ulangan xostlar\n        Map<String, List<String>> star = Map.of("switch-1", List.of("PC-1", "PC-2", "PC-3", "printer"));\n        star.forEach((center, hosts) -> hosts.forEach(h -> System.out.println(center + " <-> " + h)));\n    }'),
      mistake: 'hub va switch ni bir xil qurilma deb hisoblash. Hub barcha portlarga tarqatadi, switch esa MAC jadvali boʻyicha faqat kerakli portga yuboradi.',
      practice: 'Halqa topologiyasida bitta kabel uzilsa nima boʻladi?',
    }),
    M3: topic({
      intro: '<p><strong>ISO/OSI modeli</strong> turli ishlab chiqaruvchilar tizimlari oʻzaro ishlashi uchun tarmoq funksiyalarini yetti pogʻonaga ajratadi. Har bir pogʻona oʻzidan pastdagiga tayanadi va yuqoridagiga xizmat koʻrsatadi. <strong>Protokol</strong> — bir pogʻonadagi tomonlar orasidagi qoidalar; <strong>interfeys</strong> — qoʻshni pogʻonalar orasidagi chegara.</p>',
      points: [
        '7 Amaliy, 6 Taqdimot, 5 Seans, 4 Transport, 3 Tarmoq, 2 Kanal, 1 Fizik.',
        'Maʼlumot birliklari: bitlar (1), kadrlar (2), paketlar (3), segmentlar/datagrammalar (4).',
        '<strong>Inkapsulyatsiya</strong>: har bir pogʻona maʼlumotga oʻz sarlavhasini qoʻshadi.',
        'Standartlashtiruvchi tashkilotlar: ISO, IEEE, IETF (RFC hujjatlari), ITU-T.',
      ],
      pyCode: 'payload = "GET / HTTP/1.1"\nsegment = f"[TCP src=51000 dst=80]{payload}"\npacket = f"[IP 10.0.0.5->93.184.216.34]{segment}"\nframe = f"[ETH aa:bb->cc:dd]{packet}[FCS]"\nfor name, unit in [("Amaliy", payload), ("Transport", segment), ("Tarmoq", packet), ("Kanal", frame)]:\n    print(f"{name:10} {unit}")\n',
      javaCode: J('    public static void main(String[] args) {\n        String[] layers = {"Amaliy", "Taqdimot", "Seans", "Transport", "Tarmoq", "Kanal", "Fizik"};\n        String[] examples = {"HTTP, DNS", "TLS, JPEG", "RPC", "TCP, UDP", "IP, ICMP", "Ethernet, Wi-Fi", "Kabel, radio"};\n        for (int i = 0; i < layers.length; i++) System.out.printf("%d %-10s %s%n", 7 - i, layers[i], examples[i]);\n    }'),
      mistake: 'OSI modelini real protokollar steki deb oʻylash. Amalda TCP/IP steki ishlatiladi; OSI — tushuntirish uchun namuna model.',
      practice: 'Kommutator va marshrutizator mos ravishda qaysi OSI pogʻonalarida ishlaydi?',
    }),
    M4: topic({
      intro: '<p>Bitlar <strong>fizik muhit</strong> orqali elektr, yorugʻlik yoki radio signallari koʻrinishida uzatiladi. <strong>Kodlash</strong> bitlarni signalga shunday aylantiradiki, qabul qiluvchi ularni ishonchli ajrata olsin va sinxronlasha olsin.</p>',
      points: [
        'Muhitlar: oʻralgan juftlik (UTP, Cat5e/Cat6), koaksial kabel, optik tola (bir va koʻp modali), radio.',
        'Potensial kodlar: NRZ, NRZI — oddiy, lekin uzun 0/1 ketma-ketliklarida sinxronizatsiya yoʻqoladi.',
        'Manchester kodi — har bit oʻrtasida oʻtish boʻladi, shuning uchun oʻz-oʻzini sinxronlaydi (10 Mbit/s Ethernet).',
        'Blokli kodlar: 4B/5B, 8B/10B; modulyatsiya: AM, FM, PM, QAM.',
      ],
      pyCode: 'def manchester(bits):\n    # IEEE 802.3: 0 -> yuqoridan pastga, 1 -> pastdan yuqoriga\n    return "".join("-+" if b == "1" else "+-" for b in bits)\n\n\nprint(manchester("1011"))\n',
      javaCode: J('    public static void main(String[] args) {\n        String bits = "1011";\n        StringBuilder nrz = new StringBuilder();\n        for (char b : bits.toCharArray()) nrz.append(b == \'1\' ? \'+\' : \'-\');\n        System.out.println("NRZ: " + nrz);\n    }'),
      mistake: 'baud (signal oʻzgarishi tezligi) va bit/s ni doim teng deb hisoblash. Manchester kodida baud tezligi bit tezligidan ikki baravar yuqori.',
      practice: '00001111 ketma-ketligini NRZI va Manchester kodlarida yozing.',
    }),
    M5: topic({
      intro: '<p><strong>Ethernet</strong> (IEEE 802.3) — lokal tarmoqlarning asosiy texnologiyasi. Har bir tarmoq interfeysi 48 bitli <strong>MAC manzil</strong>ga ega: birinchi 24 bit ishlab chiqaruvchi kodi (OUI), qolgani qurilma raqami. Kommutator kadrlarni MAC manzillar jadvali asosida yoʻnaltiradi.</p>',
      points: [
        'Ethernet kadri: preambula, qabul qiluvchi MAC, joʻnatuvchi MAC, tur (EtherType), maʼlumot (46–1500 bayt), FCS.',
        'Tezliklar: 10 Mbit/s, Fast Ethernet 100 Mbit/s, Gigabit, 10 Gigabit va undan yuqori.',
        'Kommutator MAC manzillarni “oʻrganadi”: kiruvchi kadrning manba manzilini port bilan bogʻlaydi.',
        'Keng tarqatish manzili: FF:FF:FF:FF:FF:FF.',
      ],
      pyCode: 'mac_table = {}\n\n\ndef switch(frame_src, frame_dst, in_port):\n    mac_table[frame_src] = in_port             # oʻrganish\n    out = mac_table.get(frame_dst)\n    return f"port {out} ga" if out is not None else "barcha portlarga (flooding)"\n\n\nprint(switch("AA", "BB", 1))  # BB hali nomaʼlum\nprint(switch("BB", "AA", 2))  # AA 1-portda\n',
      javaCode: J('    public static void main(String[] args) {\n        String mac = "3C:52:82:1A:9F:07";\n        String oui = mac.substring(0, 8);\n        int firstOctet = Integer.parseInt(mac.substring(0, 2), 16);\n        System.out.println("OUI: " + oui);\n        System.out.println("Multicast: " + ((firstOctet & 1) == 1));\n        System.out.println("Lokal boshqariladigan: " + ((firstOctet & 2) == 2));\n    }'),
      mistake: 'MAC manzil paket butun Internet boʻylab oʻzgarmay boradi deb oʻylash. Har bir marshrutizatordan keyin kadr qayta tuziladi, MAC manzillar almashadi.',
      practice: 'Kompyuteringiz MAC manzilini toping: Windows’da <code>getmac</code> yoki <code>ipconfig /all</code>.',
    }),
    M6: topic({
      intro: '<p>Ethernet dan tashqari lokal tarmoqlarning boshqa texnologiyalari ham mavjud boʻlgan. <strong>Token Ring</strong> (IEEE 802.5) — halqada aylanib yuruvchi marker (token) olgan stansiya uzatadi. <strong>FDDI</strong> — optik tolali ikki halqali tarmoq. <strong>100VG-AnyLAN</strong> — ustuvorlikka asoslangan kirish usuli. Bugun eng muhimi — <strong>Wi-Fi</strong> (IEEE 802.11).</p>',
      points: [
        'Token Ring va FDDI da kolliziya boʻlmaydi — markerli kirish usuli.',
        'Wi-Fi da CSMA/CA (kolliziyadan qochish) ishlatiladi.',
        'Wi-Fi avlodlari: 802.11n (Wi-Fi 4), 802.11ac (Wi-Fi 5), 802.11ax (Wi-Fi 6/6E).',
        'Kirish nuqtasi (AP), SSID, kanal, himoya (WPA2/WPA3).',
      ],
      mistake: 'Wi-Fi tezligini faqat router tezligi bilan belgilash. Masofa, devorlar, kanal bandligi va qurilmalar soni katta taʼsir qiladi.',
      practice: 'Token Ring va Ethernet kirish usullarini solishtiring.',
    }),
    M7: topic({
      intro: '<p><strong>TCP/IP steki</strong> — Internetning amaliy protokollar toʻplami. U toʻrt pogʻonadan iborat: tarmoq kirish (kanal), Internet (IP), transport (TCP/UDP) va amaliy. <strong>IP</strong> protokoli paketlarni manzil boʻyicha tarmoqlar orasida yetkazadi, lekin yetkazishni kafolatlamaydi (“best effort”).</p>',
      points: [
        'IP paketi sarlavhasi: versiya, TTL, protokol, manba va qabul qiluvchi manzil.',
        'TTL har bir marshrutizatorda 1 ga kamayadi; 0 boʻlsa paket tashlanadi (tsikllardan himoya).',
        'IPv4 (32 bit) va IPv6 (128 bit).',
        'Tarmoqlararo almashish: xost paketni oʻz tarmogʻida boʻlmagan manzilga standart shlyuz orqali yuboradi.',
      ],
      pyCode: 'import ipaddress\n\nfor a in ["192.168.1.10", "8.8.8.8", "127.0.0.1", "169.254.3.4", "2001:db8::1"]:\n    ip = ipaddress.ip_address(a)\n    print(a, "v" + str(ip.version), "xususiy" if ip.is_private else "ommaviy",\n          "loopback" if ip.is_loopback else "")\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        for (String a : new String[] {"192.168.1.10", "127.0.0.1", "10.0.0.1"}) {\n            InetAddress ip = InetAddress.getByName(a);   // raqamli manzil — DNS soʻrovisiz\n            System.out.println(a + " xususiy: " + ip.isSiteLocalAddress() + " loopback: " + ip.isLoopbackAddress());\n        }\n    }', 'import java.net.InetAddress;'),
      mistake: 'IP yetkazishni kafolatlaydi deb oʻylash. Ishonchlilikni transport pogʻonasidagi TCP taʼminlaydi.',
      practice: 'Nega TTL maydoni kerak? U boʻlmasa marshrutlash tsiklida nima boʻladi?',
    }),
    M8: topic({
      intro: '<p><strong>IPv4 manzil</strong> 32 bitdan iborat va 4 ta oktet koʻrinishida yoziladi (192.168.10.77). Manzil <strong>tarmoq qismi</strong> va <strong>xost qismi</strong>ga boʻlinadi; chegarani <strong>maska</strong> (255.255.255.192) yoki <strong>prefiks</strong> (/26) belgilaydi.</p>',
      points: [
        'Sinfli manzillash (tarixiy): A (/8), B (/16), C (/24); hozir CIDR — ixtiyoriy prefiks.',
        'Xususiy diapazonlar (RFC 1918): 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16.',
        'Tarmoq manzili = IP AND maska; broadcast = barcha xost bitlari 1.',
        'Xostlar soni = 2^(32 − prefiks) − 2.',
        'NAT xususiy manzillarni bitta ommaviy manzil orqali Internetga chiqaradi.',
      ],
      pyCode: 'import ipaddress\n\nnet = ipaddress.ip_network("192.168.10.77/26", strict=False)\nprint("Tarmoq:", net.network_address, "Maska:", net.netmask)\nprint("Broadcast:", net.broadcast_address, "Xostlar:", net.num_addresses - 2)\n\n# /24 ni 4 ta teng quyi tarmoqqa boʻlish\nfor sub in ipaddress.ip_network("192.168.10.0/24").subnets(new_prefix=26):\n    print(sub)\n',
      javaCode: J('    public static void main(String[] args) {\n        int prefix = 26;\n        long mask = (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;\n        System.out.printf("Maska: %d.%d.%d.%d%n", mask >> 24 & 255, mask >> 16 & 255, mask >> 8 & 255, mask & 255);\n        System.out.println("Xostlar: " + ((1L << (32 - prefix)) - 2));\n    }'),
      mistake: 'xostlar sonini hisoblaganda tarmoq va broadcast manzillarini ayirishni unutish.',
      practice: '50 ta xost uchun eng kichik mos prefiks qaysi? (/26 — 62 xost.)',
    }),
    M9: topic({
      intro: '<p>IP ishlashi uchun yordamchi protokollar kerak. <strong>ARP</strong> IP manzil boʻyicha MAC manzilni topadi. <strong>ICMP</strong> xato va diagnostika xabarlarini yuboradi (ping, traceroute). <strong>DHCP</strong> xostlarga avtomatik ravishda IP sozlamalarini beradi. <strong>Marshrutlash protokollari</strong> marshrutizatorlarga marshrut jadvallarini avtomatik tuzishga yordam beradi.</p>',
      points: [
        'Statik marshrutlash — administrator qoʻlda kiritadi; dinamik — protokollar orqali.',
        'Masofa-vektor protokollari (RIP): sakrashlar soni; holat-kanal protokollari (OSPF): Deykstra algoritmi.',
        'BGP — avtonom tizimlar (provayderlar) orasidagi marshrutlash.',
        'Marshrut tanlash: eng uzun prefiks → administrativ masofa → metrika.',
      ],
      pyCode: '# Masofa-vektor (RIP gʻoyasi): qoʻshni jadvalidan yangi marshrutlarni oʻrganish\nmy_table = {"10.0.1.0/24": 0}\nneighbor = {"10.0.2.0/24": 1, "10.0.3.0/24": 2, "10.0.1.0/24": 1}\nfor net, hops in neighbor.items():\n    if hops + 1 < my_table.get(net, 16):   # 16 — RIP da “cheksiz”\n        my_table[net] = hops + 1\nprint(my_table)\n',
      javaCode: J('    public static void main(String[] args) {\n        // ARP keshi: IP -> MAC\n        Map<String, String> arp = new HashMap<>();\n        arp.put("192.168.1.1", "00:1A:2B:3C:4D:5E");\n        String target = "192.168.1.1";\n        System.out.println(arp.containsKey(target) ? "MAC: " + arp.get(target) : "ARP soʻrovi keng tarqatiladi: Who has " + target + "?");\n    }'),
      mistake: 'ping javob bermasa, xost ishlamayapti deb xulosa qilish — koʻp brandmauerlar ICMP ni bloklaydi.',
      practice: 'Windows’da <code>arp -a</code> buyrugʻi natijasini tahlil qiling.',
    }),
    M10: topic({
      intro: '<p><strong>Transport pogʻonasi</strong> maʼlumotni aniq ilovaga (portga) yetkazadi. <strong>TCP</strong> — ulanishga asoslangan, ishonchli, tartibni saqlaydigan protokol. <strong>UDP</strong> — ulanishsiz, tezkor, lekin kafolatsiz. <strong>Soket</strong> — IP manzil va port juftligi; dasturlar tarmoq bilan soketlar orqali ishlaydi.</p>',
      points: [
        'TCP ulanishi: uch bosqichli qoʻl siqish SYN → SYN-ACK → ACK.',
        'TCP: tartib raqamlari, tasdiqlash (ACK), qayta yuborish, oqimni boshqarish (oyna).',
        'UDP qoʻllanilishi: DNS, video/ovozli aloqa, onlayn oʻyinlar.',
        'Portlar: 0–1023 taniqli (80 HTTP, 443 HTTPS, 22 SSH, 53 DNS), 49152–65535 dinamik.',
      ],
      pyCode: 'import socket\nimport threading\n\n# Bitta jarayonda loopback orqali TCP server va mijoz\nserver = socket.socket(socket.AF_INET, socket.SOCK_STREAM)\nserver.bind(("127.0.0.1", 0))\nserver.listen(1)\nport = server.getsockname()[1]\n\n\ndef serve():\n    conn, _ = server.accept()\n    with conn:\n        conn.sendall(conn.recv(1024).upper())\n\n\nthreading.Thread(target=serve).start()\nwith socket.create_connection(("127.0.0.1", port)) as client:\n    client.sendall(b"salom, server")\n    print(client.recv(1024).decode())   # SALOM, SERVER\nserver.close()\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {\n            Thread t = new Thread(() -> {\n                try (Socket c = server.accept();\n                     BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));\n                     PrintWriter out = new PrintWriter(c.getOutputStream(), true)) {\n                    out.println(in.readLine().toUpperCase());\n                } catch (IOException e) { throw new UncheckedIOException(e); }\n            });\n            t.start();\n            try (Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort());\n                 PrintWriter out = new PrintWriter(s.getOutputStream(), true);\n                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {\n                out.println("salom, server");\n                System.out.println(in.readLine());\n            }\n            t.join();\n        }\n    }', 'import java.io.*;\nimport java.net.*;'),
      mistake: 'TCP “xabar” chegaralarini saqlaydi deb oʻylash. TCP — baytlar oqimi; bitta send() bir necha recv() ga boʻlinib kelishi mumkin.',
      practice: 'Nega DNS soʻrovlari odatda UDP orqali, fayl yuklash esa TCP orqali amalga oshiriladi?',
    }),
    M11: topic({
      intro: '<p><strong>Amaliy pogʻona</strong> protokollari foydalanuvchi xizmatlarini taʼminlaydi. <strong>HTTP</strong> — veb-sahifalar va API lar uchun soʻrov–javob protokoli. <strong>FTP</strong> — fayl uzatish. Elektron pochta: <strong>SMTP</strong> (yuborish), <strong>POP3</strong> va <strong>IMAP</strong> (qabul qilish).</p>',
      points: [
        'HTTP metodlari: GET, POST, PUT, PATCH, DELETE.',
        'Javob kodlari: 2xx muvaffaqiyat, 3xx yoʻnaltirish, 4xx mijoz xatosi, 5xx server xatosi.',
        'HTTPS = HTTP + TLS (port 443).',
        'Veb server (Apache, nginx, IIS) statik fayllarni beradi yoki soʻrovni ilovaga uzatadi.',
      ],
      pyCode: 'raw = "GET /courses?id=5 HTTP/1.1\\r\\nHost: lms.example.uz\\r\\nAccept: text/html\\r\\n\\r\\n"\nrequest_line, *header_lines = raw.split("\\r\\n")\nmethod, target, version = request_line.split()\nheaders = dict(h.split(": ", 1) for h in header_lines if h)\nprint(method, target, version)\nprint(headers)\n',
      javaCode: J('    public static void main(String[] args) {\n        Map<Integer, String> codes = new TreeMap<>(Map.of(\n            200, "OK", 201, "Created", 301, "Moved Permanently", 304, "Not Modified",\n            400, "Bad Request", 401, "Unauthorized", 403, "Forbidden", 404, "Not Found", 500, "Internal Server Error"));\n        codes.forEach((code, text) -> System.out.println(code + " " + text));\n    }'),
      mistake: 'parolni GET soʻrovi parametrlarida yuborish — URL loglarda va brauzer tarixida saqlanadi.',
      practice: 'Brauzerda F12 → Network boʻlimida bitta sahifa yuklanishini kuzating: nechta soʻrov yuborildi, qaysi kodlar qaytdi?',
      links: [LINKS.mdnHttp],
    }),
    M12: topic({
      intro: '<p><strong>DNS</strong> (Domain Name System) domen nomlarini IP manzillarga aylantiradi. U iyerarxik, taqsimlangan maʼlumotlar bazasi: ildiz serverlar → yuqori darajali domen (.uz, .com) serverlari → domenning vakolatli serverlari.</p>',
      points: [
        'Rekursiv soʻrov (mijoz → rezolver) va iterativ soʻrovlar (rezolver → serverlar).',
        'Yozuv turlari: A, AAAA, CNAME, MX, NS, TXT, PTR.',
        'TTL — yozuv keshda qancha saqlanishi.',
        'Diagnostika: <code>nslookup</code>, <code>dig</code>.',
      ],
      pyCode: 'import socket\n\n# Internet talab qiladi (platforma sandboksida tarmoq yoʻq) — oʻz kompyuteringizda ishga tushiring\nfor name in ["www.python.org", "localhost"]:\n    try:\n        print(name, "->", socket.gethostbyname(name))\n    except socket.gaierror as e:\n        print(name, "topilmadi:", e)\n',
      javaCode: J('    public static void main(String[] args) {\n        for (String name : new String[] {"localhost", "dev.java"}) {\n            try {\n                for (InetAddress a : InetAddress.getAllByName(name)) System.out.println(name + " -> " + a.getHostAddress());\n            } catch (UnknownHostException e) {\n                System.out.println(name + " topilmadi");\n            }\n        }\n    }', 'import java.net.*;'),
      mistake: 'DNS yozuvini oʻzgartirgandan keyin darhol hamma joyda yangilanadi deb kutish — eski qiymat TTL tugaguncha keshlarda qoladi.',
      practice: '<code>nslookup -type=MX gmail.com</code> buyrugʻi natijasini tushuntiring.',
    }),
    M13: topic({
      intro: '<p><strong>Server operatsion tizimlari</strong> (Windows Server, Linux distributivlari) tarmoq xizmatlarini (DHCP, DNS, fayl, pochta, veb) doimiy va ishonchli taqdim etish uchun moʻljallangan. Tarmoq boshqaruvi modeli boʻyicha <strong>ishchi guruh</strong> va <strong>domen</strong> farqlanadi.</p>',
      points: [
        'Ishchi guruh: har bir kompyuter oʻz foydalanuvchilarini alohida saqlaydi; kichik tarmoqlar uchun.',
        'Domen: markazlashgan hisoblar va siyosatlar (Windows’da Active Directory, domen kontrolleri).',
        'Guruh siyosatlari (GPO) bilan yuzlab kompyuterlarni bir joydan sozlash mumkin.',
        'Serverlarga statik IP, muntazam yangilanish va zaxira nusxa zarur.',
      ],
      mistake: 'domen kontrollerini bitta nusxada ushlab turish — u ishdan chiqsa, hech kim tizimga kira olmaydi.',
      practice: '30 ta kompyuterli oʻquv markazi uchun ishchi guruh yoki domen — qaysi biri mos? Asoslang.',
    }),
    M14: topic({
      intro: '<p>Tarmoq xavfsizligi tahdidlari: ruxsatsiz kirish, trafikni tinglash, soxtalashtirish (spoofing), DoS hujumlari, zararli dasturlar. Asosiy himoya vositalari — <strong>brandmauer</strong>, <strong>VPN</strong> va <strong>IDS/IPS</strong>.</p>',
      points: [
        'Brandmauer qoidalari yuqoridan pastga tekshiriladi; birinchi mos kelgani qoʻllanadi; oxirida “hammasini taqiqlash”.',
        'VPN: masofaviy xodim yoki filiallarni shifrlangan tunnel orqali ulash.',
        'IDS hujumni aniqlab ogohlantiradi; IPS uni toʻxtatadi.',
        'Segmentatsiya (VLAN), eng kam imtiyoz, yangilanishlar.',
      ],
      pyCode: 'RULES = [("ALLOW", "tcp", "443"), ("ALLOW", "tcp", "22"), ("DENY", "any", "*")]\n\n\ndef check(proto, port):\n    for i, (action, p, prt) in enumerate(RULES, 1):\n        if p in ("any", proto) and prt in ("*", port):\n            return action, i\n    return "DENY", "default"\n\n\nprint(check("tcp", "443"), check("udp", "53"))\n',
      javaCode: J('    public static void main(String[] args) {\n        String[][] rules = {{"ALLOW", "tcp", "443"}, {"ALLOW", "tcp", "22"}, {"DENY", "any", "*"}};\n        String proto = "tcp", port = "23";\n        for (int i = 0; i < rules.length; i++) {\n            String[] r = rules[i];\n            if ((r[1].equals("any") || r[1].equals(proto)) && (r[2].equals("*") || r[2].equals(port))) {\n                System.out.println(r[0] + " (qoida " + (i + 1) + ")");\n                break;\n            }\n        }\n    }'),
      mistake: 'keng qoidani aniq qoidadan yuqoriga qoʻyish — aniq qoida hech qachon ishlamaydi.',
      practice: 'Faqat 10.0.0.0/8 tarmogʻidan SSH ga ruxsat beruvchi va boshqalarga taqiqlovchi qoidalar tartibini yozing.',
    }),
    M15: topic({
      intro: '<p>Tarmoq nosozliklarini bartaraf qilish tizimli yondashuvni talab qiladi: muammoni aniqlash → farazlar → tekshirish → tuzatish → natijani hujjatlashtirish. Koʻpincha OSI modeli boʻyicha pastdan yuqoriga (fizik → amaliy) tekshiriladi.</p>',
      points: [
        'Fizik: kabel, port chiroqlari, Wi-Fi signali.',
        'Tarmoq: <code>ipconfig</code>/<code>ip addr</code>, <code>ping</code> shlyuzga, <code>tracert</code>/<code>traceroute</code>.',
        'Nom xizmati: <code>nslookup</code>; portlar va ulanishlar: <code>netstat</code>, <code>ss</code>.',
        'Umumiy belgilar: 169.254.x.x manzil — DHCP ishlamayapti; IP boʻyicha ishlaydi, nom boʻyicha yoʻq — DNS.',
      ],
      pyCode: 'import socket\n\n\ndef port_open(host, port, timeout=1.0):\n    """TCP port ochiqligini tekshirish (telnet/Test-NetConnection gʻoyasi)."""\n    try:\n        with socket.create_connection((host, port), timeout=timeout):\n            return True\n    except OSError:\n        return False\n\n\nprint("127.0.0.1:22 ochiq:", port_open("127.0.0.1", 22))\n',
      javaCode: J('    public static void main(String[] args) throws Exception {\n        InetAddress host = InetAddress.getLoopbackAddress();\n        System.out.println(host + " javob beradi: " + host.isReachable(1000));\n    }', 'import java.net.InetAddress;'),
      mistake: 'bir vaqtda koʻp sozlamani oʻzgartirish — qaysi oʻzgarish muammoni hal qilgani (yoki yangisini keltirib chiqargani) nomaʼlum qoladi.',
      practice: 'Foydalanuvchi “Internet ishlamayapti” deydi. Tekshirish qadamlaringizni tartib bilan yozing.',
    }),
    A1: practical({
      goal: '<p>Tarmoq qurilmalari va ularning vazifalarini oʻrganish: tarmoq adapterlari, takrorlagich, koʻprik, konsentrator.</p>',
      steps: ['Laboratoriyadagi qurilmalarni koʻzdan kechiring va har birining vazifasini yozing.', 'Har bir qurilma qaysi OSI pogʻonasida ishlashini jadvalga kiriting.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A1')],
    }),
    A2: practical({
      goal: '<p>Tarmoq topologiyalarini oʻrganish.</p>',
      steps: ['Shina, yulduz, halqa va mesh topologiyalarini chizing.', 'Har birining afzallik va kamchiliklarini jadvalda yozing.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A2')],
    }),
    A3: practical({
      goal: '<p>Tarmoq emulyatorlari (Cisco Packet Tracer, GNS3) bilan ishlash.</p>',
      steps: ['Emulyatorda 2 ta kommutator va 4 ta kompyuterdan iborat tarmoq yarating.', 'Kompyuterlarga bir tarmoqdan IP manzillar bering.', '<code>ping</code> bilan aloqani tekshiring.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A3')],
    }),
    A4: practical({
      goal: '<p>Maʼlumotlarni kodlash usullari: NRZ, NRZI va Manchester kodlarini dasturda amalga oshirish.</p>',
      steps: ['Har bir kodlash usuli uchun 10110010 ketma-ketligi signalini qoʻlda chizing.', 'Kodlovchi dasturni yozing (topshiriq quyida).', 'Qaysi kod oʻz-oʻzini sinxronlashini tushuntiring.'],
      refs: [task('KOT-A4')],
    }),
    A5: practical({
      goal: '<p>Lokal tarmoq texnologiyalari: Ethernet, Fast Ethernet. Snifferlar yordamida paketlarni tahlil qilish.</p>',
      steps: ['Wireshark’da oʻz kompyuteringiz trafigini yozib oling.', 'Bitta Ethernet kadrini oching: MAC manzillar, EtherType, IP sarlavhasi.', 'ARP soʻrov va javobini toping.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A5')],
    }),
    A6: practical({
      goal: '<p>Simsiz axborot uzatish texnologiyalari; Wi-Fi tarmogʻini sozlash.</p>',
      steps: ['Oʻquv routerida SSID, kanal va WPA2/WPA3 himoyasini sozlang.', 'Standart administrator parolini oʻzgartiring.', 'Qurilmani ulab, olingan IP manzilni tekshiring.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A6')],
    }),
    A7: practical({
      goal: '<p>IP manzillar, maskalar yordamida manzillash va quyi tarmoqlarga boʻlish (subnetting).</p>',
      steps: ['192.168.10.0/24 tarmogʻini 4 ta teng quyi tarmoqqa boʻling; har birining chegaralarini yozing.', 'Har xil oʻlchamli boʻlimlar uchun VLSM rejasini tuzing (60, 28, 12, 2 xost).', 'Kalkulyator dasturini yozing (topshiriq quyida).'],
      refs: [task('KOT-A7')],
      check: 'Nima uchun /31 va /32 prefikslar odatiy LAN uchun ishlatilmaydi?',
    }),
    A8: practical({
      goal: '<p>Marshrutizatorlarni boshqarish: statik marshrutlar va marshrut tanlash.</p>',
      steps: ['Emulyatorda ikki marshrutizator orqali uch tarmoqni bogʻlang.', 'Statik marshrutlarni kiriting (Cisco: <code>ip route 10.1.2.0 255.255.255.0 192.168.0.2</code>).', '<code>show ip route</code> natijasini tahlil qiling.', 'Eng uzun prefiks qoidasini dasturda amalga oshiring (topshiriq quyida).'],
      refs: [task('KOT-A8')],
    }),
    A9: practical({
      goal: '<p>Soketlar yordamida tarmoq dasturini yaratish.</p>',
      theory: '<p>M10 maʼruzasidagi TCP server–mijoz misolini oʻz kompyuteringizda ishga tushiring, soʻngra uni ikki alohida dasturga ajrating. Veb server soketdan olgan birinchi narsasi — HTTP soʻrovi; uni tahlil qilish quyidagi topshiriqda.</p>',
      steps: ['Server: <code>bind</code> → <code>listen</code> → <code>accept</code> → <code>recv</code>/<code>send</code>.', 'Mijoz: <code>connect</code> → <code>send</code> → <code>recv</code>.', 'Brauzerdan serveringizga ulaning va kelgan soʻrovni chop eting.', 'HTTP soʻrov tahlilchisini yozing (topshiriq quyida).'],
      refs: [task('KOT-A9')],
    }),
    A10: practical({
      goal: '<p>Windows Server operatsion tizimini oʻrnatish va boshlangʻich sozlash (virtual mashinada).</p>',
      steps: ['Virtual mashina yarating va OT ni oʻrnating.', 'Kompyuter nomi, statik IP manzil, vaqt zonasi va yangilanishlarni sozlang.', 'Server Manager’ni koʻrib chiqing.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A10')],
    }),
    A11: practical({
      goal: '<p>Tarmoq xizmatlarini oʻrnatish va sozlash (DHCP, DNS).</p>',
      steps: ['DHCP rolini qoʻshing va manzillar diapazonini (scope) yarating.', 'DNS rolini qoʻshing va A yozuv yarating.', 'Mijoz kompyuterda <code>ipconfig /renew</code> va <code>nslookup</code> bilan tekshiring.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A11')],
    }),
    A12: practical({
      goal: '<p>Windows OTda foydalanuvchilar va ularning vakolatlarini boshqarish.</p>',
      steps: ['Ikki guruh (Talabalar, Oʻqituvchilar) va ularga foydalanuvchilar yarating.', 'Guruhlarga turli huquqlar bering.', 'Standart foydalanuvchi sifatida kirib, cheklovlarni tekshiring.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A12')],
    }),
    A13: practical({
      goal: '<p>Umumiy tarmoq resurslarini yaratish va ulardan foydalanish.</p>',
      steps: ['Papka yarating va uni tarmoqda ulashing.', 'Share va NTFS huquqlarini sozlang.', 'Boshqa kompyuterdan <code>\\\\server\\papka</code> orqali ulaning va tarmoq diskini biriktiring.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A13')],
    }),
    A14: practical({
      goal: '<p>Windows OTda brandmauerni sozlash; VPN xizmatidan foydalanish.</p>',
      steps: ['Windows Defender Firewall’da kiruvchi qoida yarating (masalan, faqat lokal tarmoqdan 3389-portga ruxsat).', 'Qoidalar tartibi va standart siyosatni tushuntiring.', 'Qoidalarni tekshiruvchi dasturni yozing (topshiriq quyida).'],
      refs: [task('KOT-A14')],
    }),
    A15: practical({
      goal: '<p>Tarmoq nosozliklarini aniqlash; OT utilitalaridan foydalanish.</p>',
      steps: ['<code>ipconfig /all</code>, <code>ping</code>, <code>tracert</code>, <code>nslookup</code>, <code>netstat -an</code> buyruqlarini bajaring va natijalarni izohlang.', 'Oʻqituvchi tayyorlagan “buzilgan” sozlamali kompyuterda muammoni toping.', 'Testni topshiring.'],
      refs: [quiz('KOT-Q-A15')],
    }),
    MT1: independent({
      goal: '<p>Maʼruza va amaliy mashgʻulotlar mavzularini oʻzlashtirish (60 soat).</p>',
      tasks: [
        'Har bir maʼruza boʻyicha konspekt va atamalar lugʻati yuriting.',
        'Subnetting boʻyicha kamida 20 ta mashqni qoʻlda yeching va kalkulyator bilan tekshiring.',
        'Emulyatorda oʻzingiz loyihalagan kichik korxona tarmogʻini (kamida 3 ta quyi tarmoq, marshrutizator, DHCP) yigʻing.',
      ],
      deliverable: '<p>Konspekt, mashqlar yechimi va emulyator loyihasi fayli.</p>',
    }),
    MT2: independent({
      goal: '<p>Mustaqil ish va uyga vazifalarni bajarish (60 soat).</p>',
      tasks: [
        'Amaliy mashgʻulotlardagi barcha kodlash topshiriqlarini platformada Python yoki Java da topshiring.',
        'Soket asosidagi oddiy chat dasturini (server va mijoz) yozing.',
        'Tanlangan mavzu (masalan, IPv6, Wi-Fi 6, SDN) boʻyicha 8–10 slaydli taqdimot tayyorlang.',
      ],
      deliverable: '<p>Dastur kodi va taqdimot.</p>',
    }),
  },
}
