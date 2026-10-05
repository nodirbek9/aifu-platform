---
title: Databases and technologies for working with large volumes of data
summary: The concepts of a database and a DBMS; data models — hierarchical, network, relational, NoSQL; the relational model — table, record, field, primary and foreign keys, relationships (1:1, 1:N, N:M); the idea of normalization; SQL basics — SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE; popular DBMSs — MySQL, PostgreSQL, Oracle, SQL Server, Access, MongoDB; transactions and integrity; Big Data — the 5 Vs, storage and processing (data warehouse, data lake, Hadoop, Spark), uses in the economy; showing JOIN and GROUP BY in a program.
---

## Why does this topic matter?

Bank customers and accounts, a shop's products and sales, taxpayers, students and grades — all are stored in **databases**. An Excel sheet is convenient up to a few thousand rows, but a system with millions of records used by thousands of users at once needs a database management system (DBMS). An economist who can write a simple SQL query gets the needed report without waiting for the IT department.

:::note
**After this lesson you will be able to:**

- explain databases, DBMSs and data models;
- know the basic concepts of the relational model (table, key, relationship);
- read and write simple SQL queries;
- know the concepts of transactions and integrity;
- name Big Data technologies and their uses in the economy.
:::

## Databases and DBMSs

A **database** is a set of interrelated data organized according to a certain structure. A **DBMS** (database management system) is software for storing, searching, changing and protecting data and serving many users at once.

| Model | Structure | Example |
|---|---|---|
| Hierarchical | a tree | old banking systems, the file system |
| Network | a graph | historical systems |
| **Relational** | tables and the relationships between them | accounting, banking, ERP — the most widespread |
| **NoSQL** | documents, key-value, graph, column | large and changing data, social networks |

**Popular DBMSs**: PostgreSQL and MySQL (open source), Oracle and Microsoft SQL Server (commercial, large enterprises), Microsoft Access (small offices), SQLite (inside applications), MongoDB (document NoSQL).

## The relational model

- A **table** (relation) holds objects of one kind: `mijozlar` (customers), `buyurtmalar` (orders).
- A **record** (row) is one object; a **field** (column) is a property (name, city).
- A **primary key** uniquely identifies each record (`id`, a personal ID number, a tax ID).
- A **foreign key** refers to a record in another table (`buyurtmalar.mijoz_id` → `mijozlar.id`).
- **Relationships**: 1:1 (employee — passport), **1:N** (a customer — many orders), N:M (students — courses, through a junction table).
- **Normalization** means not duplicating data: a customer's address is stored once in the `mijozlar` table, not in every order. Duplicated data leads to inconsistencies.

## SQL basics

**SQL** (Structured Query Language) is the language for working with relational databases.

| Command | Purpose | Example |
|---|---|---|
| `SELECT … FROM` | selecting data | `SELECT ism, shahar FROM mijozlar;` |
| `WHERE` | a condition | `… WHERE shahar = 'Toshkent'` |
| `ORDER BY` | sorting | `… ORDER BY summa DESC` |
| `GROUP BY` + `SUM`, `COUNT`, `AVG` | grouping and calculating | `SELECT mijoz_id, SUM(summa) FROM buyurtmalar GROUP BY mijoz_id;` |
| `JOIN` | joining tables | `… FROM buyurtmalar b JOIN mijozlar m ON b.mijoz_id = m.id` |
| `INSERT`, `UPDATE`, `DELETE` | adding, changing, deleting | `UPDATE tovarlar SET narx = 26000 WHERE id = 5;` |

A **transaction** is a group of operations executed as a whole: in a bank transfer, debiting one account and crediting another either both happen or neither does (the **ACID** properties: atomicity, consistency, isolation, durability). This guarantees that money does not "disappear".

## Worked example: JOIN and GROUP BY

Customer and order tables are given. For each customer print the number of orders and the total amount, in decreasing order of amount (on a tie — by id); show customers without orders at the end. In SQL this is written as:

```text
SELECT m.ism, m.shahar, COUNT(b.id) AS soni, SUM(b.summa) AS jami
FROM mijozlar m LEFT JOIN buyurtmalar b ON b.mijoz_id = m.id
GROUP BY m.id, m.ism, m.shahar
ORDER BY jami DESC;
```

The program below does the same job. Input: *m* and *m* customers (id name city), *k* and *k* orders (id customer_id amount).

```python
import sys

satrlar = [s.split() for s in sys.stdin.read().split("\n") if s.strip()]
m = int(satrlar[0][0])
mijozlar = {}                                     # id -> (name, city)
for i in range(1, 1 + m):
    mijozlar[satrlar[i][0]] = (satrlar[i][1], satrlar[i][2])
k = int(satrlar[1 + m][0])
jami, soni = {}, {}
for _, mijoz_id, summa in satrlar[2 + m:2 + m + k]:
    jami[mijoz_id] = jami.get(mijoz_id, 0) + int(summa)     # GROUP BY + SUM
    soni[mijoz_id] = soni.get(mijoz_id, 0) + 1              # COUNT
for mid in sorted(jami, key=lambda x: (-jami[x], x)):        # ORDER BY jami DESC
    ism, shahar = mijozlar[mid]                               # JOIN
    print(f"{ism} ({shahar}): {soni[mid]} orders, {jami[mid]}")
for mid in sorted(mijozlar):                                  # LEFT JOIN: customers without orders
    if mid not in jami:
        print(f"{mijozlar[mid][0]} ({mijozlar[mid][1]}): no orders")
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
        Map<String, String[]> mijozlar = new TreeMap<>();          // id -> (name, city)
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
            System.out.println(mj[0] + " (" + mj[1] + "): " + soni.get(mid) + " orders, " + jami.get(mid));
        }
        for (String mid : mijozlar.keySet()) {                       // LEFT JOIN: customers without orders
            if (!jami.containsKey(mid)) {
                System.out.println(mijozlar.get(mid)[0] + " (" + mijozlar.get(mid)[1] + "): no orders");
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
    map<string, pair<string, string>> mijozlar;                    // id -> (name, city)
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
        cout << mj.first << " (" << mj.second << "): " << soni[mid] << " orders, " << jami[mid] << endl;
    }
    for (auto& p : mijozlar) {                                     // LEFT JOIN: customers without orders
        if (!jami.count(p.first)) cout << p.second.first << " (" << p.second.second << "): no orders" << endl;
    }
    return 0;
}
```

Input:

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

Output:

```text
Bekzod (Samarqand): 2 orders, 1450000
Aziza (Toshkent): 3 orders, 900000
Dilnoza (Toshkent): 1 orders, 800000
Javlon (Buxoro): no orders
```

The customer's name and city are stored only once, in the `mijozlar` table, while an order holds only `mijoz_id` — this is normalization. In the report they are combined with a **JOIN**. Aziza placed the most orders, but Bekzod spent the largest **amount** — which indicator to sort by depends on the business question. Javlon bought nothing — for marketing he is an "inactive customer".

## Big Data

**Big Data** is data of a volume and speed that cannot be stored and processed with traditional tools. It is described by the **5 Vs**:

| V | Meaning | Example |
|---|---|---|
| **Volume** | terabytes and petabytes | all of a bank's transactions |
| **Velocity** | a real-time stream | card payments, stock quotes |
| **Variety** | tables, text, images, video, logs | customer reviews, call centre recordings |
| **Veracity** | data quality and correctness | erroneous or duplicate records |
| **Value** | the benefit from analysis | detecting fraud, forecasting demand |

**Technologies**: a **data warehouse** — cleaned, structured data for analysis; a **data lake** — raw data of various kinds; distributed processing — **Hadoop**, **Apache Spark**; cloud analytics services; business intelligence (Power BI, Tableau).

**In the economy**: credit scoring and real-time fraud detection in banks; demand forecasting and price optimization in retail; predicting customer churn in telecoms; analyzing tax and customs data in state statistics. In detail — in seminar S7.

:::warning
**Common mistakes**

- **Storing the same data in many places.** Duplicated data leads to inconsistency — normalize.
- **A table without a primary key.** Records cannot be uniquely identified and linked.
- **UPDATE or DELETE without WHERE.** The whole table is changed or deleted — check with SELECT first.
- **Not checking data quality.** "Garbage in, garbage out": wrong data does not give right conclusions.
- **Storing personal data unprotected.** Follow the legal requirements (M14).
:::

:::tip
In a free browser-based SQL environment (for example sqliteonline.com) create the `mijozlar` and `buyurtmalar` tables, enter the example data and run the SQL query above — check that the result matches the program's.
:::

## Summary

- A database is a set of structured data, a DBMS is the software that manages it; the most widespread model is relational.
- The relational model: table, record, field, primary and foreign keys, 1:1, 1:N, N:M relationships, normalization.
- SQL: SELECT, WHERE, ORDER BY, GROUP BY, JOIN, INSERT, UPDATE, DELETE; transactions and ACID.
- Big Data: the 5 Vs; data warehouses and lakes, Hadoop, Spark, BI.
- In the economy: scoring, fraud detection, demand forecasting, churn prediction.

## Check yourself

:::check
Q: What is a primary key?
A: A field that uniquely identifies each record in a table (for example id).
Q: What type is the "customer — orders" relationship?
A: One-to-many (1:N): one customer can have many orders.
Q: Which SQL command is used to join tables?
A: JOIN.
Q: Name the 5 Vs of Big Data.
A: Volume, Velocity, Variety, Veracity, Value.
:::

:::link https://www.postgresql.org/docs/current/tutorial.html
PostgreSQL Tutorial
The official beginner's guide to SQL and relational databases.
:::
