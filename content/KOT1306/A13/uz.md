---
title: Tarmoq umumiy resurslarni yaratish va ulardan foydalanish
summary: Umumiy papkalar va SMB protokoli; papkani tarmoqqa ochish — grafik usul, net share, PowerShell; UNC yoʻllari va tarmoq diskini ulash; share va NTFS ruxsatlari, ularning birgalikdagi amali; yashirin va administrativ ulashishlar; umumiy printer; Linux da Samba; xavfsizlik; amaldagi tarmoq ruxsatini hisoblash.
---

## Nima uchun bu mavzu muhim?

Kafedra oʻqituvchilari bitta papkada maʼruza materiallarini saqlaydi, talabalar ularni oʻqiydi, lekin oʻzgartira olmaydi; topshiriqlar papkasiga talaba faqat oʻz faylini yuklaydi. Buning uchun fayllarni fleshkada tashish yoki pochtaga yuborish shart emas — **umumiy tarmoq resurslari** (shared folders) va toʻgʻri ruxsatlar yetarli. Notoʻgʻri sozlangan umumiy papka esa — maʼlumot sizib chiqishi va shifrlovchi viruslar tarqalishining eng keng tarqalgan yoʻli.

:::note
**Bu mashgʻulotdan soʻng siz:**

- papkani tarmoqqa ochib, unga UNC yoʻli orqali murojaat qila olasiz;
- tarmoq diskini ulay olasiz;
- share va NTFS ruxsatlarining farqini va ular birgalikda qanday ishlashini tushuntira olasiz;
- foydalanuvchining tarmoq orqali va lokal amaldagi huquqini hisoblay olasiz;
- umumiy resurslarni xavfsiz sozlash qoidalarini qoʻllay olasiz.
:::

## SMB va UNC yoʻllari

Windows da fayl va printerlarni ulashish protokoli — **SMB** (Server Message Block, TCP 445). Hozirgi versiya — **SMB 3** (shifrlash bilan); eski **SMB 1** xavfli (WannaCry virusi undan foydalangan) va oʻchirilishi kerak.

Umumiy resursga murojaat **UNC** (Universal Naming Convention) yoʻli bilan:

```text
\\SRV-DC01\Kurslar                  server nomi va ulashish nomi
\\192.168.50.10\Kurslar\KOT1306     IP manzil orqali, ichki papka bilan
\\SRV-DC01\C$                       administrativ ulashish (faqat administratorlar)
```

## Papkani ulashish

**Grafik usul**: papka → Properties → **Sharing** → **Advanced Sharing** → "Share this folder" → nomi → **Permissions** (share ruxsatlari). Serverda: Server Manager → File and Storage Services → Shares → **New Share**.

**Buyruqlar satri**:

```text
net share Kurslar=D:\Kurslar /grant:Talabalar,READ /grant:Oqituvchilar,FULL
net share                                    barcha ulashishlar roʻyxati
net use Z: \\SRV-DC01\Kurslar /persistent:yes   tarmoq diskini ulash
net use                                      ulangan disklar
net share Kurslar /delete
```

**PowerShell**:

```text
New-SmbShare -Name "Kurslar" -Path "D:\Kurslar" -ReadAccess "AIFU\Talabalar" -FullAccess "AIFU\Oqituvchilar"
Get-SmbShare
Get-SmbShareAccess -Name "Kurslar"
New-PSDrive -Name Z -PSProvider FileSystem -Root \\SRV-DC01\Kurslar -Persist
```

Oxiri `$` bilan tugaydigan nom (`Arxiv$`) — **yashirin ulashish**: tarmoqda koʻrinmaydi, lekin nomni bilgan kishi ulanadi (bu himoya emas, faqat qulaylik). `C$`, `ADMIN$`, `IPC$` — **administrativ** ulashishlar.

## Share va NTFS ruxsatlari

| | Share ruxsatlari | NTFS ruxsatlari |
|---|---|---|
| Qachon amal qiladi | faqat **tarmoq orqali** murojaatda | doim — tarmoqda ham, lokal ham |
| Darajalar | Read, Change, Full Control | Read, Read & execute, Modify, Full control va boshqalar |
| Qayerga beriladi | butun ulashishga | har bir papka va faylga, meros bilan |

**Asosiy qoida**: tarmoq orqali kirganda amaldagi huquq — share va NTFS ruxsatlaridan **eng cheklovchisi**. Har birining ichida esa foydalanuvchi va uning guruhlari ruxsatlari yigʻiladi (Deny ustun).

Zamonaviy tavsiya: share ruxsatini keng qilib qoʻyish (masalan, `Authenticated Users — Change` yoki `Full`), haqiqiy cheklovni esa batafsilroq **NTFS** ruxsatlari bilan berish. Shunda ruxsatlar bir joyda boshqariladi.

## Umumiy printer va Linux

- **Printer**: printer xususiyatlari → Sharing → "Share this printer"; mijozda `\\SRV-DC01\Printer1` ni qoʻshish. Domenda — GPO orqali avtomatik ulash.
- **Linux**: **Samba** serveri Windows mijozlariga SMB ulashishlarni beradi (`/etc/samba/smb.conf`); Linux lar orasida **NFS** ham ishlatiladi. Linux mijoz Windows ulashishni `mount -t cifs //server/Kurslar /mnt/kurslar` bilan ulaydi.

## Amaliy ish

1. `SRV-DC01` da `D:\Kurslar` papkasini yarating, ichida `Materiallar` va `Topshiriqlar` papkalari.
2. Guruhlar: `Talabalar`, `Oqituvchilar` (A12 dan).
3. Ulashish `Kurslar`: share ruxsati — `Authenticated Users: Change`.
4. NTFS: `Oqituvchilar` — Modify (butun papkaga); `Talabalar` — `Materiallar` ga Read & execute, `Topshiriqlar` ga Write (fayl yaratish, lekin boshqalarnikini oʻqimaslik uchun "This folder only" + CREATOR OWNER — Modify).
5. Mijozda talaba va oʻqituvchi hisoblari bilan `\\SRV-DC01\Kurslar` ga kiring va har bir amalni (oʻqish, yaratish, oʻchirish) sinab koʻring.
6. Serverda **Computer Management → Shared Folders → Sessions / Open Files** — kim ulangan va qaysi fayllar ochiqligini koʻring.

## Ishlangan misol: amaldagi tarmoq ruxsati

Darajalar shkalasi: `None` < `Read` < `Change` < `Full` (NTFS dagi Modify — `Change` ga mos). Birinchi satrda guruhlar soni va har bir guruh (nomi va aʼzolari). Keyin share yozuvlari soni va yozuvlar (subyekt, daraja), soʻng NTFS yozuvlari soni va yozuvlar. Oxirida foydalanuvchilar soni va nomlari. Har bir foydalanuvchi uchun: share boʻyicha eng yuqori daraja, NTFS boʻyicha eng yuqori daraja (`Everyone` hammaga taalluqli), tarmoq orqali — ularning kichigi, lokal — NTFS darajasi.

```python
DARAJA = ["None", "Read", "Change", "Full"]

guruhlar = {}                                    # foydalanuvchi -> guruhlar
for _ in range(int(input())):
    qism = input().split()
    for azo in qism[1:]:
        guruhlar.setdefault(azo, set()).add(qism[0])
share = [input().split() for _ in range(int(input()))]
ntfs = [input().split() for _ in range(int(input()))]


def eng_yuqori(yozuvlar, subyektlar):
    d = 0
    for subyekt, daraja in yozuvlar:
        if subyekt in subyektlar:
            d = max(d, DARAJA.index(daraja))     # ruxsatlar yigʻiladi
    return d


for _ in range(int(input())):
    user = input().strip()
    subyektlar = {user, "Everyone"} | guruhlar.get(user, set())
    s = eng_yuqori(share, subyektlar)
    n = eng_yuqori(ntfs, subyektlar)
    tarmoq = min(s, n)                           # eng cheklovchisi
    print(user + ": tarmoq " + DARAJA[tarmoq] + ", lokal " + DARAJA[n])
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    static final List<String> DARAJA = Arrays.asList("None", "Read", "Change", "Full");

    static int engYuqori(List<String[]> yozuvlar, Set<String> subyektlar) {
        int d = 0;
        for (String[] e : yozuvlar) {
            if (subyektlar.contains(e[0])) d = Math.max(d, DARAJA.indexOf(e[1]));   // ruxsatlar yigʻiladi
        }
        return d;
    }

    static List<String[]> oqi(Scanner in) {
        int n = Integer.parseInt(in.nextLine().trim());
        List<String[]> yozuvlar = new ArrayList<>();
        for (int i = 0; i < n; i++) yozuvlar.add(in.nextLine().trim().split(" "));
        return yozuvlar;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Set<String>> guruhlar = new HashMap<>();          // foydalanuvchi -> guruhlar
        for (String[] qism : oqi(in)) {
            for (int j = 1; j < qism.length; j++) {
                guruhlar.computeIfAbsent(qism[j], k -> new HashSet<>()).add(qism[0]);
            }
        }
        List<String[]> share = oqi(in);
        List<String[]> ntfs = oqi(in);
        int q = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < q; i++) {
            String user = in.nextLine().trim();
            Set<String> subyektlar = new HashSet<>(guruhlar.getOrDefault(user, new HashSet<>()));
            subyektlar.add(user);
            subyektlar.add("Everyone");
            int s = engYuqori(share, subyektlar);
            int n = engYuqori(ntfs, subyektlar);
            int tarmoq = Math.min(s, n);                               // eng cheklovchisi
            System.out.println(user + ": tarmoq " + DARAJA.get(tarmoq) + ", lokal " + DARAJA.get(n));
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const vector<string> DARAJA = {"None", "Read", "Change", "Full"};

vector<vector<string>> oqi() {
    string satr;
    getline(cin, satr);
    int n = stoi(satr);
    vector<vector<string>> yozuvlar;
    for (int i = 0; i < n; i++) {
        getline(cin, satr);
        istringstream ss(satr);
        vector<string> qism;
        string s;
        while (ss >> s) qism.push_back(s);
        yozuvlar.push_back(qism);
    }
    return yozuvlar;
}

int engYuqori(const vector<vector<string>>& yozuvlar, const set<string>& subyektlar) {
    int d = 0;
    for (auto& e : yozuvlar) {
        if (subyektlar.count(e[0])) {
            int indeks = find(DARAJA.begin(), DARAJA.end(), e[1]) - DARAJA.begin();
            d = max(d, indeks);                                        // ruxsatlar yigʻiladi
        }
    }
    return d;
}

int main() {
    map<string, set<string>> guruhlar;                                 // foydalanuvchi -> guruhlar
    for (auto& qism : oqi()) {
        for (size_t j = 1; j < qism.size(); j++) guruhlar[qism[j]].insert(qism[0]);
    }
    auto share = oqi();
    auto ntfs = oqi();
    for (auto& qator : oqi()) {
        string user = qator[0];
        set<string> subyektlar = guruhlar[user];
        subyektlar.insert(user);
        subyektlar.insert("Everyone");
        int s = engYuqori(share, subyektlar);
        int n = engYuqori(ntfs, subyektlar);
        int tarmoq = min(s, n);                                        // eng cheklovchisi
        cout << user << ": tarmoq " << DARAJA[tarmoq] << ", lokal " << DARAJA[n] << endl;
    }
    return 0;
}
```

Kirish:

```text
2
Talabalar ali vali
Oqituvchilar nodir
2
Everyone Read
Oqituvchilar Full
3
Talabalar Change
Oqituvchilar Change
Administrators Full
3
ali
nodir
mehmon
```

Chiqish:

```text
ali: tarmoq Read, lokal Change
nodir: tarmoq Change, lokal Change
mehmon: tarmoq None, lokal None
```

`ali` NTFS boʻyicha oʻzgartira oladi, lekin tarmoq orqali faqat oʻqiydi — share ruxsati (Everyone: Read) cheklayapti. `nodir` uchun share Full, lekin NTFS Change — natija Change. `mehmon` share orqali oʻqishga ruxsat olgan boʻlsa ham, NTFS da unga hech narsa berilmagan — kira olmaydi.

:::warning
**Koʻp uchraydigan xatolar**

- **Faqat share ruxsatlarini sozlash.** Lokal kirishda (yoki RDP orqali) ular amal qilmaydi — asosiy himoya NTFS da.
- **`Everyone — Full Control` ni ham share, ham NTFS da qoldirish.** Tarmoqdagi istalgan kishi yoki shifrlovchi virus papkani yoʻq qiladi.
- **Yashirin ulashishni (`$`) himoya deb hisoblash.** U faqat roʻyxatda koʻrinmaydi; nomni bilgan har kim ulanadi.
- **SMB 1 ni yoqiq qoldirish.** Eski va zaif protokol; `Get-SmbServerConfiguration` bilan tekshirib oʻchiring.
- **445-portni Internetga ochish.** SMB faqat ichki tarmoq yoki VPN orqali ishlatiladi.
:::

:::tip
Ikki kompyuter (yoki ikki VM) orasida papka ulashing va Wireshark da `smb2` filtri bilan ulanishni kuzating: Negotiate (versiya kelishuvi), Session Setup (autentifikatsiya), Tree Connect (ulashishga ulanish) va Create/Read soʻrovlarini toping.
:::

## Xulosa

- Windows da fayl ulashish protokoli — SMB (TCP 445), murojaat — UNC yoʻli `\\server\ulashish`.
- Ulashish: Advanced Sharing, `net share`, `New-SmbShare`; tarmoq diski: `net use Z:`.
- Share ruxsatlari faqat tarmoqda, NTFS — doim amal qiladi; tarmoqda amaldagi huquq — ikkalasining eng cheklovchisi.
- Tavsiya: share ruxsati keng, cheklov — NTFS da; SMB 1 oʻchirilgan, 445-port Internetga yopiq.

## Oʻzingizni tekshiring

:::check
Q: Windows da umumiy papkaga tarmoq orqali qanday yoʻl bilan murojaat qilinadi?
A: UNC yoʻli bilan: `\\server\papka`.
Q: Windows fayl almashish protokoli qanday ataladi?
A: SMB (Server Message Block).
Q: Share ruxsati Full, NTFS ruxsati Read. Tarmoq orqali amaldagi huquq qanday?
A: Read — share va NTFS dan eng cheklovchisi amal qiladi.
Q: `Arxiv$` nomli ulashishning xususiyati nimada?
A: U yashirin — tarmoq roʻyxatida koʻrinmaydi, lekin nomni bilgan foydalanuvchi ulanishi mumkin.
Q: Tarmoq diskini buyruqlar satrida qanday ulash mumkin?
A: `net use Z: \\SRV-DC01\Kurslar`.
:::

:::link https://learn.microsoft.com/en-us/windows-server/storage/file-server/file-server-smb-overview
SMB overview
Microsoft ning SMB protokoli va fayl serveri haqidagi sharhi (ingliz tilida).
:::
