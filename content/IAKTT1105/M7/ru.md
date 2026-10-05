---
title: Базы данных и технологии работы с большими объёмами данных
summary: Понятия базы данных и СУБД; модели данных — иерархическая, сетевая, реляционная, NoSQL; реляционная модель — таблица, запись, поле, первичный и внешний ключи, связи (1:1, 1:N, N:M); идея нормализации; основы SQL — SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE; популярные СУБД — MySQL, PostgreSQL, Oracle, SQL Server, Access, MongoDB; транзакции и целостность; большие данные (Big Data) — 5V, хранение и обработка (хранилище данных, озеро данных, Hadoop, Spark), применение в экономике; демонстрация JOIN и GROUP BY в программе.
---

## Почему эта тема важна?

Клиенты и счета банка, товары и продажи магазина, налогоплательщики, студенты и оценки — всё хранится в **базах данных**. Таблица Excel удобна до нескольких тысяч строк, но для системы с миллионами записей, с которой одновременно работают тысячи пользователей, нужна система управления базами данных (СУБД). Экономист, умеющий написать простой SQL-запрос, получает нужный отчёт сам, не дожидаясь ИТ-отдела.

:::note
**После этого урока вы сможете:**

- объяснить понятия базы данных, СУБД и моделей данных;
- знать основные понятия реляционной модели (таблица, ключ, связь);
- читать и писать простые SQL-запросы;
- знать понятия транзакции и целостности;
- назвать технологии больших данных и их применение в экономике.
:::

## База данных и СУБД

**База данных** — совокупность взаимосвязанных данных, организованных по определённой структуре. **СУБД** (система управления базами данных) — программа для хранения, поиска, изменения и защиты данных и одновременного обслуживания многих пользователей.

| Модель | Структура | Пример |
|---|---|---|
| Иерархическая | дерево | старые банковские системы, файловая система |
| Сетевая | граф | исторические системы |
| **Реляционная** | таблицы и связи между ними | бухгалтерия, банк, ERP — самая распространённая |
| **NoSQL** | документы, ключ-значение, граф, столбцы | большие и изменчивые данные, социальные сети |

**Популярные СУБД**: PostgreSQL и MySQL (открытый код), Oracle и Microsoft SQL Server (коммерческие, крупные предприятия), Microsoft Access (небольшой офис), SQLite (внутри приложений), MongoDB (документная NoSQL).

## Реляционная модель

- **Таблица** (отношение) — объекты одного вида: `mijozlar` (клиенты), `buyurtmalar` (заказы).
- **Запись** (строка) — один объект; **поле** (столбец) — свойство (имя, город).
- **Первичный ключ** (primary key) — однозначно определяет каждую запись (`id`, ПИНФЛ, ИНН).
- **Внешний ключ** (foreign key) — ссылка на запись другой таблицы (`buyurtmalar.mijoz_id` → `mijozlar.id`).
- **Связи**: 1:1 (сотрудник — паспорт), **1:N** (клиент — много заказов), N:M (студенты — предметы, через промежуточную таблицу).
- **Нормализация** — отсутствие дублирования: адрес клиента хранится один раз в таблице `mijozlar`, а не в каждом заказе. Дублирование ведёт к несогласованности.

## Основы SQL

**SQL** (Structured Query Language) — язык работы с реляционными базами.

| Команда | Назначение | Пример |
|---|---|---|
| `SELECT … FROM` | выбор данных | `SELECT ism, shahar FROM mijozlar;` |
| `WHERE` | условие | `… WHERE shahar = 'Toshkent'` |
| `ORDER BY` | сортировка | `… ORDER BY summa DESC` |
| `GROUP BY` + `SUM`, `COUNT`, `AVG` | группировка и вычисление | `SELECT mijoz_id, SUM(summa) FROM buyurtmalar GROUP BY mijoz_id;` |
| `JOIN` | соединение таблиц | `… FROM buyurtmalar b JOIN mijozlar m ON b.mijoz_id = m.id` |
| `INSERT`, `UPDATE`, `DELETE` | добавление, изменение, удаление | `UPDATE tovarlar SET narx = 26000 WHERE id = 5;` |

**Транзакция** — группа операций, выполняемая как единое целое: при банковском переводе списание с одного счёта и зачисление на другой либо выполняются оба, либо ни одно (свойства **ACID**: атомарность, согласованность, изолированность, долговечность). Это гарантирует, что деньги не «пропадут».

## Разобранный пример: JOIN и GROUP BY

Даны таблицы клиентов и заказов. Для каждого клиента выведите число заказов и общую сумму в порядке убывания суммы (при равенстве — по id); клиентов без заказов покажите в конце. На SQL это записывается так:

```text
SELECT m.ism, m.shahar, COUNT(b.id) AS soni, SUM(b.summa) AS jami
FROM mijozlar m LEFT JOIN buyurtmalar b ON b.mijoz_id = m.id
GROUP BY m.id, m.ism, m.shahar
ORDER BY jami DESC;
```

Программа ниже делает то же самое. Ввод: *m* и *m* клиентов (id имя город), *k* и *k* заказов (id id_клиента сумма).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
m = int(satrlar[0][0])
mijozlar = {}                                     # id -> (имя, город)
for i in range(1, 1 + m):
    mijozlar[satrlar[i][0]] = (satrlar[i][1], satrlar[i][2])
k = int(satrlar[1 + m][0])
jami, soni = {}, {}
for _, mijoz_id, summa in satrlar[2 + m:2 + m + k]:
    jami[mijoz_id] = jami.get(mijoz_id, 0) + int(summa)     # GROUP BY + SUM
    soni[mijoz_id] = soni.get(mijoz_id, 0) + 1              # COUNT
for mid in sorted(jami, key=lambda x: (-jami[x], x)):        # ORDER BY jami DESC
    ism, shahar = mijozlar[mid]                               # JOIN
    print(f"{ism} ({shahar}): {soni[mid]} заказ(ов), {jami[mid]}")
for mid in sorted(mijozlar):                                  # LEFT JOIN: клиенты без заказов
    if mid not in jami:
        print(f"{mijozlar[mid][0]} ({mijozlar[mid][1]}): нет заказов")
```
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int m = in.nextInt();
        Map<String, String[]> mijozlar = new TreeMap<>();          // id -> (имя, город)
        for (int i = 0; i < m; i++) {
            String id = in.next();
            mijozlar.put(id, new String[] {in.next(), in.next()});
        }
        int k = in.nextInt();
        Map<String, Long> jami = new HashMap<>();
        Map<String, Integer> soni = new HashMap<>();
        for (int i = 0; i < k; i++) {
            in.next();
            String mijozId = in.next();
            long summa = in.nextLong();
            jami.merge(mijozId, summa, Long::sum);                  // GROUP BY + SUM
            soni.merge(mijozId, 1, Integer::sum);                   // COUNT
        }
        List<String> tartib = new ArrayList<>(jami.keySet());
        tartib.sort((a, b) -> !jami.get(a).equals(jami.get(b)) ? Long.compare(jami.get(b), jami.get(a)) : a.compareTo(b));   // ORDER BY jami DESC
        for (String mid : tartib) {
            String[] mj = mijozlar.get(mid);                         // JOIN
            System.out.println(mj[0] + " (" + mj[1] + "): " + soni.get(mid) + " заказ(ов), " + jami.get(mid));
        }
        for (String mid : mijozlar.keySet()) {                       // LEFT JOIN: клиенты без заказов
            if (!jami.containsKey(mid)) {
                System.out.println(mijozlar.get(mid)[0] + " (" + mijozlar.get(mid)[1] + "): нет заказов");
            }
        }
    }
}
```
```cpp
#include <algorithm>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>
using namespace std;

int main() {
    int m, k;
    cin >> m;
    map<string, pair<string, string>> mijozlar;                    // id -> (имя, город)
    for (int i = 0; i < m; i++) {
        string id, ism, shahar;
        cin >> id >> ism >> shahar;
        mijozlar[id] = {ism, shahar};
    }
    cin >> k;
    map<string, long long> jami;
    map<string, int> soni;
    for (int i = 0; i < k; i++) {
        string bid, mijozId;
        long long summa;
        cin >> bid >> mijozId >> summa;
        jami[mijozId] += summa;                                    // GROUP BY + SUM
        soni[mijozId] += 1;                                        // COUNT
    }
    vector<string> tartib;
    for (auto& p : jami) tartib.push_back(p.first);
    sort(tartib.begin(), tartib.end(), [&](const string& a, const string& b) {
        return jami[a] != jami[b] ? jami[a] > jami[b] : a < b;   // ORDER BY jami DESC
    });
    for (const string& mid : tartib) {
        auto& mj = mijozlar[mid];                                  // JOIN
        cout << mj.first << " (" << mj.second << "): " << soni[mid] << " заказ(ов), " << jami[mid] << endl;
    }
    for (auto& p : mijozlar) {                                     // LEFT JOIN: клиенты без заказов
        if (!jami.count(p.first)) cout << p.second.first << " (" << p.second.second << "): нет заказов" << endl;
    }
    return 0;
}
```

Ввод:

```text
4
1 Aziza Toshkent
2 Bekzod Samarqand
3 Dilnoza Toshkent
4 Javlon Buxoro
6
101 1 450000
102 2 1200000
103 1 300000
104 3 800000
105 2 250000
106 1 150000
```

Вывод:

```text
Bekzod (Samarqand): 2 заказ(ов), 1450000
Aziza (Toshkent): 3 заказ(ов), 900000
Dilnoza (Toshkent): 1 заказ(ов), 800000
Javlon (Buxoro): нет заказов
```

Имя и город клиента хранятся только один раз — в таблице `mijozlar`, а в заказе есть лишь `mijoz_id`, — это нормализация. В отчёте они объединяются через **JOIN**. Больше всего заказов сделала Aziza, но наибольшую **сумму** потратил Bekzod — по какому показателю сортировать, зависит от бизнес-вопроса. Javlon ничего не купил — для маркетинга это «неактивный клиент».

## Большие данные (Big Data)

**Большие данные** — данные такого объёма и скорости, что их нельзя хранить и обрабатывать традиционными средствами. Их описывают через **5V**:

| V | Смысл | Пример |
|---|---|---|
| **Volume** (объём) | терабайты и петабайты | все транзакции банка |
| **Velocity** (скорость) | поток в реальном времени | платежи по картам, биржевые котировки |
| **Variety** (разнообразие) | таблицы, текст, изображения, видео, журналы | отзывы клиентов, записи колл-центра |
| **Veracity** (достоверность) | качество и правильность данных | ошибочные или дублирующиеся записи |
| **Value** (ценность) | польза от анализа | выявление мошенничества, прогноз спроса |

**Технологии**: **хранилище данных** (data warehouse) — очищенные структурированные данные для анализа; **озеро данных** (data lake) — сырые данные разных видов; распределённая обработка — **Hadoop**, **Apache Spark**; облачные аналитические сервисы; бизнес-аналитика (Power BI, Tableau).

**В экономике**: кредитный скоринг и выявление мошенничества в реальном времени в банках; прогноз спроса и оптимизация цен в торговле; прогноз оттока клиентов в телекоме; анализ налоговых и таможенных данных в госстатистике. Подробно — на семинаре S7.

:::warning
**Частые ошибки**

- **Хранить одни и те же данные во многих местах.** Дублирование ведёт к несогласованности — нормализуйте.
- **Таблица без первичного ключа.** Записи нельзя однозначно определить и связать.
- **UPDATE или DELETE без WHERE.** Изменится или удалится вся таблица — сначала проверьте через SELECT.
- **Не проверять качество данных.** «Мусор на входе — мусор на выходе»: из неверных данных не получить верных выводов.
- **Хранить персональные данные без защиты.** Соблюдайте требования закона (M14).
:::

:::tip
В бесплатной SQL-среде в браузере (например, sqliteonline.com) создайте таблицы `mijozlar` и `buyurtmalar`, введите данные из примера и выполните SQL-запрос выше — проверьте, что результат совпадает с программным.
:::

## Итоги

- База данных — совокупность структурированных данных, СУБД — программа для управления ею; самая распространённая модель — реляционная.
- Реляционная модель: таблица, запись, поле, первичный и внешний ключи, связи 1:1, 1:N, N:M, нормализация.
- SQL: SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE, DELETE; транзакции и ACID.
- Большие данные: 5V; хранилища и озёра данных, Hadoop, Spark, BI.
- В экономике: скоринг, выявление мошенничества, прогноз спроса, прогноз оттока клиентов.

## Проверьте себя

:::check
Q: Что такое первичный ключ?
A: Поле, однозначно определяющее каждую запись таблицы (например, id).
Q: К какому виду относится связь «клиент — заказы»?
A: Один ко многим (1:N): у одного клиента может быть много заказов.
Q: Какая команда SQL используется для соединения таблиц?
A: JOIN.
Q: Назовите 5V больших данных.
A: Volume (объём), Velocity (скорость), Variety (разнообразие), Veracity (достоверность), Value (ценность).
:::

:::link https://www.postgresql.org/docs/current/tutorial.html
PostgreSQL Tutorial
Официальное вводное руководство по SQL и реляционным базам данных (на английском языке).
:::
