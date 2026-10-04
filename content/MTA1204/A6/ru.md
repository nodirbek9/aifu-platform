---
title: Динамические структуры данных. Очередь, стек, дек. Списки (практическое занятие)
summary: Вычисление постфиксного выражения (обратной польской записи) стеком, максимум в скользящем окне деком (монотонный дек), курсор текстового редактора на двусвязном списке; выбор структуры по главной операции задачи.
---

## Почему это важно

Стек, очередь и дек — простые, но очень мощные структуры. Калькулятор вычисляет выражение стеком, биржевой график деком находит наибольшую цену за последние минуты, а текстовый редактор быстро вставляет и удаляет буквы вокруг курсора благодаря идее связного списка. На этом занятии решим эти три практические задачи.

:::note
**После этого занятия вы сможете:**

- вычислять постфиксное выражение с помощью стека;
- поддерживать дек «монотонным» и находить максимум скользящего окна за *O*(*n*);
- вставлять и удалять узлы двусвязного списка с помощью указателей;
- выбирать подходящую структуру по главной операции задачи.
:::

## Постфиксная запись и стек

В обычной (инфиксной) записи `(3 + 4) * 2` нужны скобки и приоритеты операций. В **постфиксной** (обратной польской) записи операция пишется **после** операндов: `3 4 + 2 *`. Ни скобки, ни приоритеты не нужны, а вычисление стеком очень просто:

```text
для каждого элемента:
    число → положить в стек
    операция → взять два числа (сначала b, потом a), положить a оп b
в конце в стеке одно число — ответ
```

Компиляторы и калькуляторы сначала переводят выражение в постфиксную форму (алгоритм «сортировочной станции» Дейкстры), а затем вычисляют его именно так.

## Монотонный дек

**Максимум в скользящем окне**: окно длины *k* скользит по массиву; для каждого положения нужно найти наибольший элемент окна. Каждый раз заново просматривать окно — *O*(*n* · *k*). С деком — *O*(*n*):

- в деке хранятся **индексы**, значения которых от начала к концу **убывают**;
- когда приходит новый элемент, с конца удаляются значения не больше него — они уже никогда не станут максимумом;
- если индекс в начале вышел из окна, он удаляется из начала;
- начало дека — максимум текущего окна.

Каждый индекс входит в дек один раз и выходит один раз — всего *O*(*n*).

## Двусвязный список

В двусвязном списке каждый узел хранит указатели `oldingi` (предыдущий) и `keyingi` (следующий). Вставка до или после известного узла и удаление узла — лишь замена нескольких указателей, *O*(1). **Узел-страж** (sentinel) — фиктивный узел в начале списка: он убирает особые случаи вроде пустого списка или «удаления головы».

## Разобранный пример 1: вычисление постфиксного выражения

В одной строке через пробел записано постфиксное выражение: целые числа и операции `+`, `-`, `*`. Выведите значение выражения.

```python
belgilar = input().split()
stek = []
for b in belgilar:
    if b in ("+", "-", "*"):
        ikkinchi = stek.pop()           # сначала правый операнд
        birinchi = stek.pop()
        if b == "+":
            stek.append(birinchi + ikkinchi)
        elif b == "-":
            stek.append(birinchi - ikkinchi)
        else:
            stek.append(birinchi * ikkinchi)
    else:
        stek.append(int(b))             # число — в стек
print(stek.pop())
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String[] belgilar = new Scanner(System.in).nextLine().trim().split("\\s+");
        Deque<Long> stek = new ArrayDeque<>();
        for (String b : belgilar) {
            if (b.equals("+") || b.equals("-") || b.equals("*")) {
                long ikkinchi = stek.pop();     // сначала правый операнд
                long birinchi = stek.pop();
                if (b.equals("+")) stek.push(birinchi + ikkinchi);
                else if (b.equals("-")) stek.push(birinchi - ikkinchi);
                else stek.push(birinchi * ikkinchi);
            } else {
                stek.push(Long.parseLong(b));   // число — в стек
            }
        }
        System.out.println(stek.pop());
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <stack>
#include <string>
using namespace std;

int main() {
    string satr, b;
    getline(cin, satr);
    istringstream oqim(satr);
    stack<long long> stek;
    while (oqim >> b) {
        if (b == "+" || b == "-" || b == "*") {
            long long ikkinchi = stek.top();    // сначала правый операнд
            stek.pop();
            long long birinchi = stek.top();
            stek.pop();
            if (b == "+") stek.push(birinchi + ikkinchi);
            else if (b == "-") stek.push(birinchi - ikkinchi);
            else stek.push(birinchi * ikkinchi);
        } else {
            stek.push(stoll(b));                // число — в стек
        }
    }
    cout << stek.top() << endl;
    return 0;
}
```

Для `3 4 + 2 *` выводится `14` ((3 + 4) · 2); для `5 1 2 + 4 * + 3 -` — тоже `14` (5 + (1 + 2) · 4 − 3); для `2 7 -` — `-5` (*stek* — стек, *birinchi*/*ikkinchi* — первый/второй операнд). При вычитании важен порядок операндов: первым из стека выходит **правый** операнд. Отрицательное число вроде `-5` тоже приходит как элемент и не принимается за операцию, потому что не равно в точности `-`.

## Разобранный пример 2: максимум в скользящем окне

В первой строке *n* и *k*, во второй — *n* чисел. Выведите максимум каждого последовательного окна длины *k* (всего *n* − *k* + 1 чисел).

```python
from collections import deque

n, k = map(int, input().split())
a = list(map(int, input().split()))
dek = deque()                           # индексы; их значения убывают
natija = []
for i in range(n):
    while dek and a[dek[-1]] <= a[i]:   # больше никогда не будет максимумом
        dek.pop()
    dek.append(i)
    if dek[0] <= i - k:                 # вышел из окна
        dek.popleft()
    if i >= k - 1:
        natija.append(a[dek[0]])        # начало дека — максимум
print(*natija)
```
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(), k = in.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();

        Deque<Integer> dek = new ArrayDeque<>();            // индексы; их значения убывают
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            while (!dek.isEmpty() && a[dek.peekLast()] <= a[i]) dek.pollLast();   // больше никогда не будет максимумом
            dek.addLast(i);
            if (dek.peekFirst() <= i - k) dek.pollFirst();  // вышел из окна
            if (i >= k - 1) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(a[dek.peekFirst()]);              // начало дека — максимум
            }
        }
        System.out.println(sb);
    }
}
```
```cpp
#include <deque>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    vector<int> a(n);
    for (int& v : a) cin >> v;

    deque<int> dek;                                         // индексы; их значения убывают
    bool birinchi = true;
    for (int i = 0; i < n; i++) {
        while (!dek.empty() && a[dek.back()] <= a[i]) dek.pop_back();   // больше никогда не будет максимумом
        dek.push_back(i);
        if (dek.front() <= i - k) dek.pop_front();          // вышел из окна
        if (i >= k - 1) {
            cout << (birinchi ? "" : " ") << a[dek.front()];    // начало дека — максимум
            birinchi = false;
        }
    }
    cout << endl;
    return 0;
}
```

Для `8 3` и `1 3 -1 -3 5 3 6 7` выводится `3 3 5 5 6 7`. Например, когда приходит `5`, из дека удаляются `-1` и `-3`: они левее `5` и меньше — пока `5` в окне, они никогда не станут максимумом (*dek* — дек, *natija* — результат). В деке хранятся **индексы, а не значения** — по индексу мы узнаём, что элемент вышел из окна.

## Разобранный пример 3: курсор в двусвязном списке

Текстовый редактор: сначала текст пуст, курсор в конце. В первой строке *q*, затем команды: `yoz C` — вставить символ *C* перед курсором, `chap` / `ong` — сдвинуть курсор на символ влево / вправо (у края стоит на месте), `ochir` — удалить символ перед курсором (если есть). В конце выведите текст. Текст хранится в двусвязном списке, курсор — указатель на узел слева от него.

```python
class Tugun:
    def __init__(self, belgi):
        self.belgi = belgi
        self.oldingi = None
        self.keyingi = None


qoriqchi = Tugun("")                        # фиктивный головной узел — без особых случаев
kursor = qoriqchi                           # узел слева от курсора

q = int(input())
for _ in range(q):
    buyruq = input().split()
    if buyruq[0] == "yoz":
        t = Tugun(buyruq[1])                # между курсором и следующим узлом
        t.oldingi = kursor
        t.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = t
        kursor.keyingi = t
        kursor = t
    elif buyruq[0] == "chap":
        if kursor is not qoriqchi:
            kursor = kursor.oldingi
    elif buyruq[0] == "ong":
        if kursor.keyingi:
            kursor = kursor.keyingi
    elif kursor is not qoriqchi:            # ochir: удалить узел слева от курсора
        kursor.oldingi.keyingi = kursor.keyingi
        if kursor.keyingi:
            kursor.keyingi.oldingi = kursor.oldingi
        kursor = kursor.oldingi

belgilar = []
t = qoriqchi.keyingi
while t:
    belgilar.append(t.belgi)
    t = t.keyingi
print("".join(belgilar))
```
```java
import java.util.Scanner;

public class Main {
    static class Tugun {
        char belgi;
        Tugun oldingi, keyingi;
        Tugun(char belgi) { this.belgi = belgi; }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Tugun qoriqchi = new Tugun(' ');                    // фиктивный головной узел — без особых случаев
        Tugun kursor = qoriqchi;                            // узел слева от курсора

        int q = in.nextInt();
        for (int k = 0; k < q; k++) {
            String buyruq = in.next();
            if (buyruq.equals("yoz")) {
                Tugun t = new Tugun(in.next().charAt(0));   // между курсором и следующим узлом
                t.oldingi = kursor;
                t.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = t;
                kursor.keyingi = t;
                kursor = t;
            } else if (buyruq.equals("chap")) {
                if (kursor != qoriqchi) kursor = kursor.oldingi;
            } else if (buyruq.equals("ong")) {
                if (kursor.keyingi != null) kursor = kursor.keyingi;
            } else if (kursor != qoriqchi) {                // ochir: удалить узел слева от курсора
                kursor.oldingi.keyingi = kursor.keyingi;
                if (kursor.keyingi != null) kursor.keyingi.oldingi = kursor.oldingi;
                kursor = kursor.oldingi;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Tugun t = qoriqchi.keyingi; t != null; t = t.keyingi) sb.append(t.belgi);
        System.out.println(sb);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

struct Tugun {
    char belgi;
    Tugun* oldingi = nullptr;
    Tugun* keyingi = nullptr;
};

int main() {
    Tugun* qoriqchi = new Tugun{' '};                       // фиктивный головной узел — без особых случаев
    Tugun* kursor = qoriqchi;                               // узел слева от курсора

    int q;
    cin >> q;
    for (int k = 0; k < q; k++) {
        string buyruq;
        cin >> buyruq;
        if (buyruq == "yoz") {
            char c;
            cin >> c;
            Tugun* t = new Tugun{c};                        // между курсором и следующим узлом
            t->oldingi = kursor;
            t->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = t;
            kursor->keyingi = t;
            kursor = t;
        } else if (buyruq == "chap") {
            if (kursor != qoriqchi) kursor = kursor->oldingi;
        } else if (buyruq == "ong") {
            if (kursor->keyingi) kursor = kursor->keyingi;
        } else if (kursor != qoriqchi) {                    // ochir: удалить узел слева от курсора
            Tugun* eski = kursor;
            kursor->oldingi->keyingi = kursor->keyingi;
            if (kursor->keyingi) kursor->keyingi->oldingi = kursor->oldingi;
            kursor = kursor->oldingi;
            delete eski;
        }
    }

    for (Tugun* t = qoriqchi->keyingi; t; t = t->keyingi) cout << t->belgi;
    cout << endl;
    while (qoriqchi) {
        Tugun* keyingi = qoriqchi->keyingi;
        delete qoriqchi;
        qoriqchi = keyingi;
    }
    return 0;
}
```

Ввод:

```text
8
yoz a
yoz b
yoz c
chap
ochir
yoz x
ong
yoz d
```

Вывод:

```text
axcd
```

После набора `abc` курсор справа от `c`; `chap` переносит его между `b` и `c`, а `ochir` удаляет `b` (*qoriqchi* — страж, *kursor* — курсор). Каждая команда меняет несколько указателей — *O*(1) при любой длине текста. В массиве же вставка и удаление в середине каждый раз сдвигали бы остальные символы. Благодаря узлу-стражу состояние «мы в начале текста» стало простой проверкой `kursor == qoriqchi`.

:::warning
**Частые ошибки**

- **Перестановка операндов в постфиксе.** В `a b -` стек сначала отдаёт `b`; вычисляется `a − b`, а не `b − a`.
- **Хранение значений в монотонном деке.** Нужен индекс, чтобы знать, когда элемент выйдет из окна.
- **Забытый указатель в двусвязном списке.** Вставка меняет четыре указателя, удаление — два; если пропустить один, список «рвётся».
- **Извлечение из пустого стека.** При неверном выражении стек может опустеть; в реальной программе проверяйте это.
:::

:::tip
Рисуйте структуры на указателях на бумаге: узлы — прямоугольниками, указатели — стрелками. Сравнив рисунок до и после каждой операции, сразу увидите, какая стрелка не обновлена.
:::

## Итоги

- Стек позволяет вычислять постфиксное выражение без скобок и приоритетов.
- Монотонный дек находит максимум скользящего окна за *O*(*n*): каждый индекс входит и выходит один раз.
- В двусвязном списке вставка и удаление вокруг известного узла — *O*(1); узел-страж убирает особые случаи.
- Структуру выбирают по главной операции задачи: брать последний — стек, работать с обоими концами — дек, менять в середине — связный список.

## Проверьте себя

:::check
Q: Каково значение постфиксного выражения `2 3 4 * +`?
A: 14: 2 + 3 · 4.
Q: Почему из конца монотонного дека удаляются индексы с малыми значениями?
A: Правее них есть больший элемент; пока он в окне, они никогда не станут максимумом.
Q: Какова сложность поиска максимума скользящего окна монотонным деком?
A: *O*(*n*) — каждый индекс добавляется в дек один раз и удаляется не более одного раза.
Q: Зачем нужен узел-страж?
A: Он гарантирует, что список никогда не пуст, и убирает особые случаи вроде удаления головы.
:::

:::link https://en.wikipedia.org/wiki/Reverse_Polish_notation
Обратная польская запись
Постфиксная запись и её вычисление стеком (на английском).
:::
