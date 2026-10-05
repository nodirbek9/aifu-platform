---
title: Technologies for processing Big Data
summary: Independent study — vertical and horizontal scaling; the MapReduce paradigm step by step (map, combine, shuffle, reduce) and a program simulating it; Apache Spark — in-memory computing and lazy evaluation; stream processing and time windows; ETL and ELT; the data warehouse, data lake and "lakehouse", bronze-silver-gold layers; dimensions of data quality; the cost of analytics in the cloud; an assignment.
---

## Purpose of this independent study

In seminar S7 we got to know the characteristics of Big Data and a list of technologies (Hadoop, Spark, Kafka). In this independent study we understand **how they work**: why thousands of ordinary computers are cheaper than one supercomputer, how MapReduce divides a task, and what path data takes to become a report.

:::note
**After this independent study you will be able to:**

- distinguish vertical and horizontal scaling;
- explain the stages of MapReduce with an example;
- name the advantages of Spark and stream processing;
- describe the ETL/ELT process and the data lake architecture;
- assess data quality along its dimensions.
:::

## Scaling

| Approach | Meaning | Limitation |
|---|---|---|
| **Vertical** (scale up) | making one server more powerful: more processors, more memory | expensive, there is a physical limit, one failure stops everything |
| **Horizontal** (scale out) | combining many ordinary servers into a cluster | the task must be divided and the results gathered |

Big Data technologies are based on horizontal scaling: data and computation are distributed across the cluster's nodes, and if one node fails, another does its work (there are copies of the data).

## The MapReduce paradigm

A model proposed by Google (2004) and implemented in Hadoop. The task is split into two simple functions:

1. **Map** — each node reads its part of the data and emits (key, value) pairs: (region, amount).
2. **Combine** (optional) — pre-aggregates identical keys inside the node — less data is sent over the network.
3. **Shuffle** — pairs with the same key are sent to one "reducer".
4. **Reduce** — the final result for each key: the sum, the count, the maximum.

The key idea: **bring the computation to the data**. Instead of copying terabytes to one server, each node processes the part on its own disk.

## Worked example: simulating MapReduce

Sales records (region, amount in million soums) are distributed in equal parts across 3 nodes. Each node performs map and combine, then reduce gives the final total. This is the distributed form of SQL's `GROUP BY region` (M7).

```python
import sys

TUGUNLAR = 3

satrlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
qism = (len(satrlar) + TUGUNLAR - 1) // TUGUNLAR

# stage 1: Map + Combine — each node processes its own part
oraliq = []
for t in range(TUGUNLAR):
    mahalliy = {}
    for satr in satrlar[t * qism:(t + 1) * qism]:
        hudud, summa = satr.split()
        mahalliy[hudud] = mahalliy.get(hudud, 0) + int(summa)
    oraliq.append(mahalliy)
    juftlar = ", ".join(f"{k}={mahalliy[k]}" for k in sorted(mahalliy))
    print(f"Node {t + 1}: {juftlar}")

# stage 2: Shuffle — identical keys are gathered at one reducer
# stage 3: Reduce — the final total
jami = {}
for mahalliy in oraliq:
    for k, v in mahalliy.items():
        jami[k] = jami.get(k, 0) + v
for k in sorted(jami):
    print(f"Total {k}: {jami[k]}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static final int TUGUNLAR = 3;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> satrlar = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine();
            if (!s.isBlank()) satrlar.add(s.trim());
        }
        int qism = (satrlar.size() + TUGUNLAR - 1) / TUGUNLAR;

        // stage 1: Map + Combine — each node processes its own part
        List<TreeMap<String, Long>> oraliq = new ArrayList<>();
        for (int t = 0; t < TUGUNLAR; t++) {
            TreeMap<String, Long> mahalliy = new TreeMap<>();
            for (int i = t * qism; i < Math.min((t + 1) * qism, satrlar.size()); i++) {
                String[] q = satrlar.get(i).split("\\s+");
                mahalliy.merge(q[0], Long.parseLong(q[1]), Long::sum);
            }
            oraliq.add(mahalliy);
            List<String> juftlar = new ArrayList<>();
            for (Map.Entry<String, Long> e : mahalliy.entrySet()) juftlar.add(e.getKey() + "=" + e.getValue());
            System.out.println("Node " + (t + 1) + ": " + String.join(", ", juftlar));
        }

        // stage 2: Shuffle — identical keys are gathered at one reducer
        // stage 3: Reduce — the final total
        TreeMap<String, Long> jami = new TreeMap<>();
        for (TreeMap<String, Long> mahalliy : oraliq)
            for (Map.Entry<String, Long> e : mahalliy.entrySet()) jami.merge(e.getKey(), e.getValue(), Long::sum);
        for (Map.Entry<String, Long> e : jami.entrySet())
            System.out.println("Total " + e.getKey() + ": " + e.getValue());
    }
}
```
```cpp
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

const int TUGUNLAR = 3;

int main() {
    vector<string> satrlar;
    string s;
    while (getline(cin, s))
        if (s.find_first_not_of(" \t\r") != string::npos) satrlar.push_back(s);
    int n = satrlar.size();
    int qism = (n + TUGUNLAR - 1) / TUGUNLAR;

    // stage 1: Map + Combine — each node processes its own part
    vector<map<string, long long>> oraliq;
    for (int t = 0; t < TUGUNLAR; t++) {
        map<string, long long> mahalliy;
        for (int i = t * qism; i < min((t + 1) * qism, n); i++) {
            istringstream ss(satrlar[i]);
            string hudud;
            long long summa;
            ss >> hudud >> summa;
            mahalliy[hudud] += summa;
        }
        oraliq.push_back(mahalliy);
        cout << "Node " << t + 1 << ": ";
        bool birinchi = true;
        for (auto& [k, v] : mahalliy) {
            cout << (birinchi ? "" : ", ") << k << "=" << v;
            birinchi = false;
        }
        cout << "\n";
    }

    // stage 2: Shuffle — identical keys are gathered at one reducer
    // stage 3: Reduce — the final total
    map<string, long long> jami;
    for (auto& mahalliy : oraliq)
        for (auto& [k, v] : mahalliy) jami[k] += v;
    for (auto& [k, v] : jami) cout << "Total " << k << ": " << v << "\n";
    return 0;
}
```

Input:

```text
Toshkent 120
Samarqand 80
Toshkent 50
Buxoro 40
Samarqand 70
Toshkent 30
Buxoro 60
Andijon 90
Toshkent 100
```

Output:

```text
Node 1: Samarqand=80, Toshkent=170
Node 2: Buxoro=40, Samarqand=70, Toshkent=30
Node 3: Andijon=90, Buxoro=60, Toshkent=100
Total Andijon: 90
Total Buxoro: 100
Total Samarqand: 150
Total Toshkent: 300
```

Thanks to combine, node 1 sends 2 pairs to the reducer instead of 3 records. With billions of records this cuts network traffic several times over. In a real cluster the nodes work in parallel: 1,000 nodes process the data roughly 1,000 times faster (some of the gain goes to the network and coordination).

## Apache Spark

Hadoop MapReduce writes the result of every stage to disk, which is slow. **Spark** keeps intermediate results **in memory** and is tens of times faster in multi-stage analysis (for example, when machine learning passes over the same data many times). **Lazy evaluation**: Spark does not execute commands immediately; it first builds and optimizes the whole plan and computes only when a result is requested. Spark supports SQL, Python (PySpark), machine learning (MLlib) and streaming.

## Stream processing

Data from payments, cash registers and sensors arrives continuously. Streaming systems (Kafka + Spark Streaming or Flink) process it as it arrives. The key concept is the **time window**:

| Window type | Example |
|---|---|
| **Tumbling** | the sales total every 5 minutes: 10:00–10:05, 10:05–10:10 |
| **Sliding** | the total for the last hour, updated every minute |
| **Session** | one user visit to a website (until a break in activity) |

The suspicious transaction rule from S7 runs on exactly such a stream.

## ETL and ELT

| Process | Order | When |
|---|---|---|
| **ETL** (Extract — Transform — Load) | extract from the source → clean and transform → load into the warehouse | traditional data warehouses, a strict structure |
| **ELT** (Extract — Load — Transform) | load in raw form → transform inside the warehouse when needed | cloud warehouses and data lakes, large volumes |

## Warehouse, lake and lakehouse

- **Data warehouse** — cleaned, structured data for reports (SQL).
- **Data lake** — raw data in any format stored cheaply (files, logs, images).
- **Lakehouse** — the cheapness of a lake combined with the order of a warehouse.

The **bronze — silver — gold** layers:

| Layer | Content | Example |
|---|---|---|
| **Bronze** | raw, unchanged data | receipts from cash registers as JSON |
| **Silver** | cleaned, merged, duplicates removed | a sales table in a single format |
| **Gold** | business-ready aggregates | revenue by region and day, for dashboards |

## Data quality

| Dimension | Question | Example of a defect |
|---|---|---|
| **Completeness** | are all required values present? | 15% of customers have no phone number |
| **Accuracy** | does the value match reality? | price 850,000 instead of 85,000 |
| **Consistency** | is it the same across systems? | a customer's address differs in the CRM and accounting |
| **Timeliness** | is the data fresh? | the report uses last week's data instead of yesterday's |
| **Uniqueness** | are there no duplicates? | one customer recorded three times with different spellings |
| **Validity** | does it match the format and rules? | the date "31.02.2025" |

## The cost of analytics in the cloud

Cloud Big Data services (BigQuery, Snowflake, Databricks and others) often charge by the **volume of data scanned** or by **compute time**. Therefore: request only the columns you need (not `SELECT *`), partition tables by date, use the ready aggregates in the gold layer, and switch off unused clusters (FinOps, S11).

:::warning
**Common mistakes**

- **Big Data technology for small data.** For a few GB, Excel, Python or an ordinary DBMS is enough and cheaper.
- **Passing raw data to a report without cleaning it.** Check the quality dimensions.
- **Turning the lake into a "swamp".** Without metadata (what, from where, when) nobody can find the data.
- **Not controlling costs.** One wrong query can scan terabytes and produce a large bill.
:::

## Assignment

1. In the example program, change the number of nodes to 2 and to 4. Does the final result change? And the intermediate results? Why?
2. Modify the program to calculate the **number of sales** for each region as well as the total.
3. For one online shop, write in a table which data would be in the bronze, silver and gold layers.
4. Assess the quality of one table you know (for example, a group list) along the six dimensions.

## Summary

- Big Data is based on horizontal scaling: computation is brought to the data.
- MapReduce: map → combine → shuffle → reduce; Spark works in memory and is faster.
- Stream processing works with time windows; ETL/ELT delivers data to the warehouse.
- Architecture: warehouse, lake, lakehouse; bronze → silver → gold.
- Data quality is assessed along six dimensions; cloud costs are kept under control.

## Check yourself

:::check
Q: Why is the combine stage needed?
A: To pre-aggregate identical keys inside a node and reduce the volume of data sent over the network.
Q: Why is Spark faster than Hadoop MapReduce?
A: It keeps intermediate results in memory rather than on disk.
Q: What is the difference between ETL and ELT?
A: In ETL data is transformed before loading; in ELT it is loaded raw and transformed later inside the warehouse.
Q: What data is stored in the gold layer?
A: Business-ready aggregates — for reports and dashboards.
:::

:::link https://spark.apache.org/docs/latest/quick-start.html
Apache Spark: Quick Start
The official guide to getting started with Spark.
:::
