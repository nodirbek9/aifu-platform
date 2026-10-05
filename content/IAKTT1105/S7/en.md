---
title: Technologies for processing Big Data
summary: Seminar — the 5 Vs of Big Data; structured, semi-structured and unstructured data; the stages of data processing — collection, storage, cleaning, analysis, visualization; batch and stream processing; Hadoop (HDFS, MapReduce), Apache Spark, Kafka, NoSQL, data warehouses and lakes, cloud platforms; types of analytics — descriptive, diagnostic, predictive, prescriptive; uses in the economy; a rule for detecting a suspicious transaction in real time (a program); privacy and ethics.
---

## Purpose of the seminar

In lecture M7 we covered databases and the concept of Big Data. In the seminar we look more deeply at **how** big data is processed — which stages and technologies are used — and try out in a program a simple rule for detecting a suspicious bank card payment in real time.

:::note
**After this seminar you will be able to:**

- explain the features of Big Data and the types of data;
- name the stages of data processing and the main technologies;
- distinguish batch and stream processing;
- know the types of analytics and their uses in the economy;
- apply a simple anomaly rule to streaming data.
:::

## Types of data

| Type | Description | Example |
|---|---|---|
| **Structured** | a strict table form | bank transactions, accounting entries |
| **Semi-structured** | tagged but flexible | JSON, XML, web logs |
| **Unstructured** | no fixed structure | texts, reviews, images, videos, voice recordings |

Studies show that a large share of data is unstructured — analyzing it requires text analytics and artificial intelligence.

## Processing stages and technologies

| Stage | Meaning | Tools |
|---|---|---|
| **Collection** | from tills, apps, sensors, websites | APIs, Apache Kafka (streams), ETL tools |
| **Storage** | large and varied data | HDFS (the Hadoop distributed file system), data lakes, NoSQL, cloud storage |
| **Cleaning** | duplicates, errors, missing values, aligning formats | Python (pandas), Spark, Power Query |
| **Analysis** | calculation, statistics, machine learning | Apache Spark, SQL, Python, R |
| **Visualization** | dashboards and reports | Power BI, Tableau, Looker Studio |

**Hadoop** stores data spread across many ordinary computers (HDFS) and processes it in parallel with the **MapReduce** method: *Map* — each computer processes its own part, *Reduce* — the results are combined. **Apache Spark** is much faster because it works in memory, and supports batch and stream analytics, SQL and machine learning.

**Batch and stream processing**: batch processing collects the data and processes it, for example, once a night (a monthly report); stream processing handles each event as soon as it arrives (a card payment, a stock price). Fraud detection needs stream processing — a payment must be approved or stopped in a fraction of a second.

## Types of analytics

| Type | Question | Example |
|---|---|---|
| **Descriptive** | what happened? | a monthly sales report |
| **Diagnostic** | why did it happen? | in which region and why sales fell |
| **Predictive** | what will happen? | next month's demand, the probability that a customer repays a loan |
| **Prescriptive** | what should be done? | the optimal price, stock level, route |

## Worked example: detecting a suspicious transaction

A bank checks every card payment in real time. A simple rule: if the payment amount is more than *k* times the average of the customer's previous (accepted) payments, it is **suspicious**, stopped for confirmation and not added to the history; the rule applies once there are at least 3 previous payments. Input: *k*, then the sequence of payments (thousand soums).

```python
import sys

t = list(map(int, sys.stdin.read().split()))
k, tolovlar = t[0], t[1:]
jami = soni = shubhali = 0
for i, summa in enumerate(tolovlar, start=1):
    if soni >= 3 and summa * soni > k * jami:        # amount > k * average (in integers)
        shubhali += 1
        print(f"{i}: {summa} SUSPICIOUS (average {jami / soni:.1f})")
        continue                                      # a suspicious payment is not added to the history
    jami += summa
    soni += 1
print(f"checked: {len(tolovlar)}, suspicious: {shubhali}")
```
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long k = in.nextLong();
        List<Long> tolovlar = new ArrayList<>();
        while (in.hasNextLong()) tolovlar.add(in.nextLong());
        long jami = 0, soni = 0;
        int shubhali = 0;
        for (int i = 0; i < tolovlar.size(); i++) {
            long summa = tolovlar.get(i);
            if (soni >= 3 && summa * soni > k * jami) {       // amount > k * average (in integers)
                shubhali++;
                System.out.println(String.format("%d: %d SUSPICIOUS (average %.1f)", i + 1, summa, (double) jami / soni));
                continue;                                      // a suspicious payment is not added to the history
            }
            jami += summa;
            soni++;
        }
        System.out.println("checked: " + tolovlar.size() + ", suspicious: " + shubhali);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <vector>
using namespace std;

int main() {
    long long k, x;
    cin >> k;
    vector<long long> tolovlar;
    while (cin >> x) tolovlar.push_back(x);
    long long jami = 0, soni = 0;
    int shubhali = 0;
    for (size_t i = 0; i < tolovlar.size(); i++) {
        long long summa = tolovlar[i];
        if (soni >= 3 && summa * soni > k * jami) {           // amount > k * average (in integers)
            shubhali++;
            printf("%zu: %lld SUSPICIOUS (average %.1f)\n", i + 1, summa, (double) jami / soni);
            continue;                                          // a suspicious payment is not added to the history
        }
        jami += summa;
        soni++;
    }
    printf("checked: %zu, suspicious: %d\n", tolovlar.size(), shubhali);
    return 0;
}
```

Input:

```text
3 120 135 128 140 118 980 125 132 130 415 127 138
```

Output:

```text
6: 980 SUSPICIOUS (average 128.2)
10: 415 SUSPICIOUS (average 128.5)
checked: 12, suspicious: 2
```

The rule does not recompute the average each time; it only updates the sum and the count — this is the main idea of stream processing: the data is passed once and little memory is used. Also, the condition `amount > k × average` is checked in integers as `amount × count > k × sum` — without fractional errors. Real banks use hundreds of features (place, time, device, shop type) and machine learning models, but the principle is the same: detecting a sharp deviation from usual behaviour.

## Uses in the economy

- **Banking**: credit scoring, fraud detection, customer segmentation.
- **Retail**: demand forecasting, recommendation systems ("bought together with this"), dynamic pricing.
- **Telecoms**: predicting customer churn, planning network load.
- **Government**: choosing tax audits by risk, customs analytics, traffic flows.
- **Agriculture**: yield forecasts, weather and satellite data.

**Privacy and ethics**: personal data is used only on a legal basis and for a purpose (the Law "On Personal Data"); data anonymization; algorithms must not lead to discrimination (for example, in lending).

:::warning
**Common mistakes**

- **"More data means the right conclusion".** Poor-quality data gives wrong results even in large volumes.
- **Treating correlation as causation.** If two indicators change together, one may not be the cause of the other.
- **Adding a suspicious transaction to the history.** An anomaly distorts the average and hides the next ones.
- **Using personal data without consent.** A breach of law and a loss of reputation.
:::

:::tip
Try *k* = 2 and *k* = 5 in the example: the stricter the rule, the more payments are flagged (but also the more false alarms) — a trade-off similar to the FAR and FRR balance in M10.
:::

## Summary

- Big Data: the 5 Vs; structured, semi-structured and unstructured data.
- Stages: collection → storage → cleaning → analysis → visualization.
- Technologies: Hadoop (HDFS, MapReduce), Spark, Kafka, NoSQL, data warehouses and lakes, BI.
- Batch and stream processing; fraud detection needs streaming.
- Types of analytics: descriptive, diagnostic, predictive, prescriptive; privacy and ethics requirements.

## Check yourself

:::check
Q: Which framework was created for processing large volumes of data distributed across many computers?
A: Apache Hadoop.
Q: What does unstructured data include?
A: Texts, images, videos and voice recordings.
Q: What is the difference between batch and stream processing?
A: Batch processing handles collected data periodically; stream processing handles each event as soon as it arrives.
Q: Give an example of Big Data use in the economy.
A: Forecasting demand by analyzing customer behaviour, or detecting fraud in card payments.
:::

:::link https://spark.apache.org/docs/latest/
Apache Spark Documentation
The official documentation of the big data processing platform.
:::
