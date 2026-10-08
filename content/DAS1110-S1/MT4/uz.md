---
title: Matritsalarni tashkil etish
summary: Mustaqil ish: matritsa bilan ishlaydigan dastur — kiritish, satr va ustunlar boʻyicha hisob, kerakli elementlarni indeks sharti bilan tanlash; namunaviy masala — har satrning eng kattasi va yigʻindisi eng katta satr.
---

## Nima uchun bu mavzu muhim?

Guruh jurnalida har bir talabaning eng yuqori bahosini va eng yaxshi oʻzlashtirgan talabani topish, doʻkon filiallari boʻyicha oylik savdo jadvalida eng samarali filialni aniqlash, rasmning eng yorugʻ qatorini topish — bularning hammasi matritsa ustidagi bir xil amallar. Bu mustaqil ishda matritsaning kerakli elementlarini indekslar sharti orqali tanlashni mustaqil mashq qilasiz.

:::note
**Bu mustaqil ishdan soʻng siz:**

- *n* × *m* matritsani oʻqib, uni ichma-ich sikl bilan aylanib chiqa olasiz;
- har bir satr (yoki ustun) boʻyicha alohida natija hisoblay olasiz;
- kerakli elementlarni indekslar sharti bilan ifodalay olasiz (*i* = *j*, *i* + *j* = *n* − 1, *i* < *j* …);
- 2 × 2 va 3 × 3 misollarda natijani qoʻlda hisoblab, dasturni tekshira olasiz.
:::

## Mavzu boʻyicha qisqa takror

Matritsadagi koʻp „qism“lar indekslar sharti bilan tavsiflanadi (kvadrat matritsa, *n* × *n*):

| Qism | Shart | 3 × 3 da elementlar |
|---|---|---|
| Bosh diagonal | *i* = *j* | `a[0][0]`, `a[1][1]`, `a[2][2]` |
| Yordamchi diagonal | *i* + *j* = *n* − 1 | `a[0][2]`, `a[1][1]`, `a[2][0]` |
| Bosh diagonaldan yuqori | *i* < *j* | `a[0][1]`, `a[0][2]`, `a[1][2]` |
| Bosh diagonaldan pastda | *i* > *j* | `a[1][0]`, `a[2][0]`, `a[2][1]` |
| *k*-satr | *i* = *k* | `a[k][0]` … `a[k][n-1]` |

Agar qismda har satrda bitta element boʻlsa (diagonallar), bitta sikl yetarli. Agar koʻp boʻlsa — ichma-ich sikl va shart.

## Namunaviy masala: satrlar tahlili

Birinchi qatorda *n* va *m*, keyingi *n* qatorda *m* tadan son. Birinchi qatorda har bir satrning eng katta elementini chiqaring, ikkinchi qatorda — yigʻindisi eng katta satrning raqamini (1 dan; bir nechta boʻlsa — birinchisi).

```text
1. n, m va matritsani oʻqi
2. eng_yaxshi = 0, eng_katta_yigindi = birinchi satr yigʻindisi
3. har bir satr i uchun:
       mx = a[i][0], s = 0
       har bir j uchun: mx = max(mx, a[i][j]); s += a[i][j]
       mx ni eslab qol
       agar s > eng_katta_yigindi: eng_yaxshi = i, eng_katta_yigindi = s
4. barcha mx larni, keyin eng_yaxshi + 1 ni chiqar
```

```python
n, m = map(int, input().split())
a = [list(map(int, input().split())) for _ in range(n)]

maksimumlar = []
eng_yaxshi = 0
eng_katta = sum(a[0])
for i in range(n):
    mx = a[i][0]
    s = 0
    for j in range(m):
        if a[i][j] > mx:
            mx = a[i][j]
        s += a[i][j]
    maksimumlar.append(mx)
    if s > eng_katta:
        eng_katta = s
        eng_yaxshi = i

print(*maksimumlar)
print(eng_yaxshi + 1)
```
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                a[i][j] = in.nextInt();
            }
        }

        StringBuilder sb = new StringBuilder();
        int engYaxshi = 0;
        long engKatta = Long.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int mx = a[i][0];
            long s = 0;
            for (int j = 0; j < m; j++) {
                if (a[i][j] > mx) mx = a[i][j];
                s += a[i][j];
            }
            if (i > 0) sb.append(' ');
            sb.append(mx);
            if (s > engKatta) {
                engKatta = s;
                engYaxshi = i;
            }
        }

        System.out.println(sb);
        System.out.println(engYaxshi + 1);
    }
}
```
```cpp
#include <iostream>
#include <vector>
#include <climits>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<vector<int>> a(n, vector<int>(m));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            cin >> a[i][j];
        }
    }

    int engYaxshi = 0;
    long long engKatta = LLONG_MIN;
    for (int i = 0; i < n; i++) {
        int mx = a[i][0];
        long long s = 0;
        for (int j = 0; j < m; j++) {
            if (a[i][j] > mx) mx = a[i][j];
            s += a[i][j];
        }
        if (i > 0) cout << ' ';
        cout << mx;
        if (s > engKatta) {
            engKatta = s;
            engYaxshi = i;
        }
    }
    cout << endl << engYaxshi + 1 << endl;
    return 0;
}
```

Kirish:

```text
3 4
2 7 1 8
2 8 1 8
1 4 1 4
```

| Satr | Eng kattasi | Yigʻindi |
|---|---|---|
| 1 | 8 | 18 |
| 2 | 8 | 19 |
| 3 | 4 | 10 |

Chiqadi: `8 8 4` va `2`. Har bir satr uchun `mx` va `s` tashqi sikl **ichida** yangidan boshlanadi — bu eng muhim nuqta. Java va C++'da eng katta yigʻindi eng kichik mumkin boʻlgan qiymatdan (`Long.MIN_VALUE`, `LLONG_MIN`) boshlanadi; Pythonʼda birinchi satr yigʻindisidan.

## Mustaqil ish topshirigʻi

Quyida berilgan **matritsa diagonallari** masalasini yeching. Topshiriladigan ish:

1. **Indekslar sharti** — bosh va yordamchi diagonal elementlari uchun shartlarni yozing va 4 × 4 matritsa chizib, diagonal elementlarini belgilang.
2. **Toq *n* holati** — *n* toq boʻlganda ikki diagonal qaysi elementda kesishadi? Masala shartiga koʻra u ikkala yigʻindiga ham kiradimi — aniqlang va dasturda shunga amal qiling.
3. **Dastur** — Python, Java yoki C++'da; pastdagi topshiriq oynasida yuboriladi va avtomatik testlardan oʻtishi kerak.
4. **Testlar** — kamida 3 ta: 1 × 1, 2 × 2 va 3 × 3 matritsalar, natijalar qoʻlda hisoblangan.

:::warning
**Baholash mezonlari va koʻp uchraydigan xatolar**

- Yordamchi diagonal: `a[i][n - 1 - i]`, `a[i][n - i]` emas.
- Diagonal uchun ichma-ich sikl shart emas; ishlatilsa ham natija toʻgʻri boʻlishi kerak.
- Pythonʼda matritsani `[[0] * n] * n` bilan yaratmang — satrlar bitta obyekt boʻlib qoladi.
- Natijalar qoʻlda tekshirilgan boʻlishi kerak.
:::

:::tip
1 × 1 matritsada bitta element ikkala diagonalda ham turadi. Dasturingiz bu holda nima chiqarishini oldindan qogʻozga yozib qoʻying va natijani solishtiring.
:::

## Xulosa

- Matritsa qismlari indekslar sharti bilan tavsiflanadi: *i* = *j*, *i* + *j* = *n* − 1, *i* < *j*.
- Har satrda bitta element boʻlsa — bitta sikl, koʻp boʻlsa — ichma-ich sikl.
- Satr boʻyicha natijalar uchun yigʻuvchi va maksimum tashqi sikl ichida boshlanadi.
- Kichik matritsalarda (1 × 1, 2 × 2, 3 × 3) natijani qoʻlda hisoblab tekshiring.

## Oʻzingizni tekshiring

:::check
Q: 5 × 5 matritsada bosh diagonaldan yuqorida nechta element bor?
A: 10 ta: (25 − 5) / 2.
Q: *n* toq boʻlganda bosh va yordamchi diagonallar qaysi elementda kesishadi?
A: Markaziy elementda: `a[n // 2][n // 2]`.
Q: Namunaviy masalada `mx` sikldan tashqarida bir marta boshlansa, nima boʻladi?
A: Har bir satr uchun oldingi satrlarning maksimumi ham hisobga olinadi — natija notoʻgʻri boʻladi.
:::

:::link https://en.wikipedia.org/wiki/Main_diagonal
Wikipedia: bosh diagonal
Matritsa diagonallari haqida.
:::
