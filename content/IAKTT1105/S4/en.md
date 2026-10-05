---
title: Tools for viewing, processing and summarizing data in Microsoft Excel
summary: Seminar — working with large tables: Format as Table, freezing rows and columns, sorting and filtering, advanced filter, conditional formatting, Data Validation, removing duplicates, subtotals, pivot tables (PivotTable) and pivot charts, "what if" analysis (Goal Seek, scenarios); practical tasks; showing the idea of a pivot table in a program.
---

## Purpose of the seminar

In lecture M4 we saw formulas and functions. In real work tables have hundreds and thousands of rows: a year of sales, a customer list, staff payroll. In the seminar we practise tools for quickly **viewing**, **cleaning** and **summarizing** such data.

:::note
**After this seminar you will be able to:**

- set up a large table for convenient viewing (table format, freezing);
- analyze data with sorting, filtering and conditional formatting;
- use data validation and cleaning tools;
- build a pivot table and a pivot chart;
- carry out "what if" analysis (Goal Seek).
:::

## Viewing tools

| Tool | Where | What it gives |
|---|---|---|
| **Format as Table** | Home → Format as Table (`Ctrl + T`) | automatic filters, banded rows, formulas spread to new rows by themselves, references by column name |
| **Freezing rows/columns** | View → Freeze Panes | the header stays visible when scrolling |
| **Grouping** | Data → Group | collapsing and expanding rows or columns |
| **Several windows** | View → New Window, Arrange All | viewing different parts of one file side by side |

## Processing tools

| Tool | Meaning |
|---|---|
| **Sorting** | several levels: for example, first by region (A→Z), then by amount (descending) |
| **Filter** | value, text, number and date conditions ("greater than 10", "Toshkent", "this month") |
| **Advanced filter** | complex conditions (AND/OR) in a separate criteria table |
| **Conditional formatting** | colour scales, data bars, icons, formula-based rules |
| **Data Validation** | allowing only values from a list or within limits in a cell — prevents errors |
| **Remove duplicates** | Data → Remove Duplicates |
| **Text to columns** | Data → Text to Columns (for example "Surname Name" → two columns) |
| **Flash Fill** | automatic filling by example (`Ctrl + E`) |

## Summarizing tools

**Subtotals** (Data → Subtotal): in a sorted table adds a sum or average for each group (for example, region).

A **pivot table** (Insert → PivotTable) summarizes a large table with a few mouse moves:

| Field | Purpose | Example |
|---|---|---|
| **Rows** | grouping | region |
| **Columns** | a second grouping | product or month |
| **Values** | calculation: Sum, Count, Average, Max | sum of sales amounts |
| **Filters** | filtering the whole table | year |

Values can be shown as "% of Grand Total", dates grouped by month and quarter, and **Slicers** used for interactive filtering. A **pivot chart** (PivotChart) is built from a pivot table. When the source data changes: PivotTable Analyze → Refresh.

**"What if" analysis**:

- **Goal Seek** (Data → What-If Analysis): finds one input value needed to reach a desired result. For example, how many products must be sold for the profit to be 50 million soums?
- **Scenario Manager**: saving and comparing optimistic, base and pessimistic variants.
- **Data Table**: a table of results when one or two parameters change.

## Worked example: the idea of a pivot table

A pivot table essentially **groups and summarizes** data. The program below groups sales records (region, product, amount) by region and prints for each region the sum, the number of records and the share of the grand total — the same result as Excel with Rows = region, Values = sum of amounts and count of records, shown as "% of Grand Total".

```python
import sys

jami, soni = {}, {}
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    hudud, tovar, summa = satr.split()
    jami[hudud] = jami.get(hudud, 0) + int(summa)     # Values: Sum
    soni[hudud] = soni.get(hudud, 0) + 1              # Values: Count
umumiy = sum(jami.values())
for h in sorted(jami):                                # Rows: region (alphabetical)
    print(f"{h}: {jami[h]} ({soni[h]} rows, {100 * jami[h] / umumiy:.1f}%)")
print(f"Total: {umumiy}")
```
```java
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Map<String, Long> jami = new TreeMap<>();            // Rows: region (alphabetical)
        Map<String, Integer> soni = new TreeMap<>();
        while (in.hasNext()) {
            String hudud = in.next(), tovar = in.next();
            long summa = in.nextLong();
            jami.merge(hudud, summa, Long::sum);              // Values: Sum
            soni.merge(hudud, 1, Integer::sum);               // Values: Count
        }
        long umumiy = 0;
        for (long v : jami.values()) umumiy += v;
        for (String h : jami.keySet()) {
            System.out.println(String.format("%s: %d (%d rows, %.1f%%)", h, jami.get(h), soni.get(h), 100.0 * jami.get(h) / umumiy));
        }
        System.out.println("Total: " + umumiy);
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
using namespace std;

int main() {
    map<string, long long> jami;                             // Rows: region (alphabetical)
    map<string, int> soni;
    string hudud, tovar;
    long long summa;
    while (cin >> hudud >> tovar >> summa) {
        jami[hudud] += summa;                                // Values: Sum
        soni[hudud] += 1;                                    // Values: Count
    }
    long long umumiy = 0;
    for (auto& p : jami) umumiy += p.second;
    for (auto& p : jami) {
        printf("%s: %lld (%d rows, %.1f%%)\n", p.first.c_str(), p.second, soni[p.first], 100.0 * p.second / umumiy);
    }
    printf("Total: %lld\n", umumiy);
    return 0;
}
```

Input:

```text
Toshkent choy 1200000
Samarqand choy 800000
Toshkent qahva 900000
Buxoro shakar 450000
Samarqand qahva 650000
Toshkent shakar 300000
Buxoro choy 500000
```

Output:

```text
Buxoro: 950000 (2 rows, 19.8%)
Samarqand: 1450000 (2 rows, 30.2%)
Toshkent: 2400000 (3 rows, 50.0%)
Total: 4800000
```

(The products are tea, coffee and sugar.) Enter the same data in Excel, build a pivot table and compare the result. Then add "product" to the Columns field — you get a region × product table. The program and the pivot table do the same job, but for thousands of rows and repeated reports automation (a program or Power Query) is more convenient.

## Practical tasks

1. Build a sales table of at least 30 rows (date, region, product, quantity, price, amount) and format it as a table.
2. Freeze the header row; sort first by region, then by amount.
3. Use a filter to show only one region and the last month's sales.
4. Add a colour scale to the amount column and a rule for values greater than 1 million soums.
5. Add a drop-down list to the "region" column with Data Validation.
6. Build a pivot table: Rows — region, Columns — product, Values — sum of amounts; show shares as "% of Grand Total"; add a pivot chart.
7. With Goal Seek: what must the price of one product be for total sales to grow by 10%?

:::warning
**Common mistakes**

- **Empty rows and merged cells in a table.** Sorting, filtering and pivot tables work incorrectly.
- **Different data types in one column.** If numbers and text are mixed, the sum is wrong.
- **Forgetting to refresh a pivot table.** Refresh when the source changes.
- **Selecting only one column when sorting.** Rows get "torn apart" and the data is corrupted — select the whole table.
:::

:::tip
A table with only one data type per column, headers in the first row and no empty rows or merged cells is a "clean" table. Pivot tables and filters work reliably only on a clean table.
:::

## Summary

- Viewing: table format, freezing, grouping.
- Processing: sorting, filters, conditional formatting, data validation, duplicates, splitting text.
- Summarizing: subtotals, pivot tables and charts, slicers.
- "What if": Goal Seek, scenarios, data tables.
- A pivot table groups and summarizes; this can also be automated in a program.

## Check yourself

:::check
Q: Which tool groups a large table and quickly calculates sums and averages?
A: A pivot table (PivotTable).
Q: What is done so that the header row stays visible when scrolling?
A: The row is frozen with View → Freeze Panes.
Q: What is Data Validation for?
A: It ensures that only allowed values are entered in a cell, preventing errors.
Q: What does Goal Seek do?
A: Finds one input value needed to reach a desired result.
:::

:::link https://support.microsoft.com/office/create-a-pivottable-to-analyze-worksheet-data-a9a84538-bfe9-40a9-a8e9-f99134456576
Microsoft: Create a PivotTable
The official guide to creating a pivot table.
:::
