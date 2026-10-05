---
title: Tools for statistical data analysis
summary: Independent study — the stages of statistical analysis (question, data collection, cleaning, descriptive analysis, visualization, conclusion); descriptive statistics (mean, median, mode, standard deviation, coefficient of variation, quartiles); relationships — correlation and regression; tools — Excel functions and the "Analysis ToolPak", SPSS, Stata, R, Python (pandas), Power BI; correlation and causation; a program analysing sales and advertising.
---

## Purpose of this independent study

In M4 and S4 we worked with formulas and pivot tables in Excel. In this independent study we look at statistical analysis systematically: which indicators are calculated, what they mean and which tools are used.

:::note
**After this independent study you will be able to:**

- name the stages of statistical analysis;
- calculate and interpret the main descriptive indicators;
- explain the idea of correlation and regression;
- choose a statistical tool to suit the task.
:::

## Stages of statistical analysis

1. **Asking the question**: "Does advertising spend affect sales?"
2. **Collecting data**: accounting, CRM, surveys, official statistics (stat.uz).
3. **Cleaning**: empty values, duplicates, mistyped figures, harmonizing units (millions and thousands of soums).
4. **Descriptive analysis**: average, spread, the shape of the distribution.
5. **Visualization**: charts and diagrams (M4, M5).
6. **Relationships and conclusions**: correlation, regression, hypothesis testing; interpreting the result in business language.

:::tip
Most of the analysis time (often more than half) goes into cleaning the data. "Garbage in, garbage out": dirty data does not produce correct conclusions.
:::

## Descriptive statistics

| Indicator | Meaning | Excel function |
|---|---|---|
| **Mean** | sum of values / their count | `AVERAGE` |
| **Median** | the middle value of the sorted series; not sensitive to extreme values | `MEDIAN` |
| **Mode** | the most frequent value | `MODE.SNGL` |
| **Standard deviation** | the average distance of values from the mean | `STDEV.S` (sample), `STDEV.P` (population) |
| **Coefficient of variation** | standard deviation / mean × 100%; for comparing series of different scales | by formula |
| **Quartiles** | values dividing the series into four equal parts | `QUARTILE.INC` |
| **Min, max, range** | the smallest and largest value and their difference | `MIN`, `MAX` |

**Mean or median?** In unevenly distributed data such as salaries or house prices, a few very large values "pull" the mean. For example, if 9 employees earn 5 million and the manager 50 million soums, the mean is 9.5 million while the median is 5 million — the median shows employees' typical pay better.

## Relationships: correlation and regression

- **The correlation coefficient** (r, Excel: `CORREL`) ranges from −1 to 1: close to 1 — a strong positive relationship, close to −1 — a strong negative one, close to 0 — no linear relationship.
- **Linear regression** (Excel: `SLOPE`, `INTERCEPT`, `LINEST`, a trend line on a chart): y = a + b·x; for example, if an extra 1 million soums is spent on advertising, sales rise on average by b million.
- **R²** (the coefficient of determination) shows what share of the variation in y the model explains.

:::warning
**Correlation is not causation.** Ice cream sales and drownings rise together — hot weather causes both. Advertising and sales rising together does not prove that advertising increased sales: both may depend on the season. Testing causality requires an experiment (an A/B test) or special econometric methods.
:::

## Statistical analysis tools

| Tool | Strength | Suits |
|---|---|---|
| **Excel** + the "Analysis ToolPak" (Data → Data Analysis) | available everywhere; descriptive statistics, histograms, correlation, regression in a few clicks | basic and intermediate analysis |
| **Google Sheets** | in the cloud, collaborative work | small team analyses |
| **SPSS** | menu-driven, survey analysis, tests | sociologists, marketers |
| **Stata, EViews** | econometrics, time series, panel data | economist researchers |
| **R** | free, thousands of statistical packages, high-quality charts | research, academic work |
| **Python** (pandas, statsmodels, matplotlib) | free, big data, automation, machine learning | analysts, Big Data (M7, S7) |
| **Power BI, Tableau** | interactive dashboards, reports for management | business intelligence |

## Worked example: sales and advertising

A company's sales for 12 months (first line) and advertising spend (second line), in million soums. Calculate the descriptive indicators and the correlation of sales with advertising. This is a look "inside" Excel's `AVERAGE`, `MEDIAN`, `STDEV.S` and `CORREL` functions.

```python
import math
import sys

satrlar = sys.stdin.read().strip().split("\n")
sotuv = [float(x) for x in satrlar[0].split()]       # monthly sales, million soums
reklama = [float(x) for x in satrlar[1].split()]     # monthly advertising spend
n = len(sotuv)

ortacha = sum(sotuv) / n
tartib = sorted(sotuv)
if n % 2 == 1:
    mediana = tartib[n // 2]
else:
    mediana = (tartib[n // 2 - 1] + tartib[n // 2]) / 2
# sample standard deviation (STDEV.S in Excel)
sd = math.sqrt(sum((x - ortacha) ** 2 for x in sotuv) / (n - 1))
cv = sd / ortacha * 100

# correlation coefficient (CORREL in Excel)
ortacha_r = sum(reklama) / n
kov = sum((x - ortacha) * (y - ortacha_r) for x, y in zip(sotuv, reklama))
sx = sum((x - ortacha) ** 2 for x in sotuv)
sy = sum((y - ortacha_r) ** 2 for y in reklama)
r = kov / math.sqrt(sx * sy)

print(f"Months: {n}")
print(f"Mean: {ortacha:.2f}")
print(f"Median: {mediana:.2f}")
print(f"Standard deviation: {sd:.2f}")
print(f"Coefficient of variation: {cv:.2f}%")
print(f"Min: {min(sotuv):.0f}, max: {max(sotuv):.0f}")
print(f"Correlation with advertising: {r:.2f}")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static double[] oqi(String satr) {
        return Arrays.stream(satr.trim().split("\\s+")).mapToDouble(Double::parseDouble).toArray();
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        double[] sotuv = oqi(in.nextLine());        // monthly sales, million soums
        double[] reklama = oqi(in.nextLine());      // monthly advertising spend
        int n = sotuv.length;

        double ortacha = Arrays.stream(sotuv).sum() / n;
        double[] tartib = sotuv.clone();
        Arrays.sort(tartib);
        double mediana = n % 2 == 1 ? tartib[n / 2] : (tartib[n / 2 - 1] + tartib[n / 2]) / 2;
        // sample standard deviation (STDEV.S in Excel)
        double sx = 0;
        for (double x : sotuv) sx += (x - ortacha) * (x - ortacha);
        double sd = Math.sqrt(sx / (n - 1));
        double cv = sd / ortacha * 100;

        // correlation coefficient (CORREL in Excel)
        double ortachaR = Arrays.stream(reklama).sum() / n;
        double kov = 0, sy = 0;
        for (int i = 0; i < n; i++) {
            kov += (sotuv[i] - ortacha) * (reklama[i] - ortachaR);
            sy += (reklama[i] - ortachaR) * (reklama[i] - ortachaR);
        }
        double r = kov / Math.sqrt(sx * sy);

        System.out.println("Months: " + n);
        System.out.println(String.format("Mean: %.2f", ortacha));
        System.out.println(String.format("Median: %.2f", mediana));
        System.out.println(String.format("Standard deviation: %.2f", sd));
        System.out.println(String.format("Coefficient of variation: %.2f%%", cv));
        System.out.println(String.format("Min: %.0f, max: %.0f", tartib[0], tartib[n - 1]));
        System.out.println(String.format("Correlation with advertising: %.2f", r));
    }
}
```
```cpp
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

vector<double> oqi(const string& satr) {
    istringstream ss(satr);
    vector<double> v;
    double x;
    while (ss >> x) v.push_back(x);
    return v;
}

int main() {
    string a, b;
    getline(cin, a);
    getline(cin, b);
    vector<double> sotuv = oqi(a);          // monthly sales, million soums
    vector<double> reklama = oqi(b);        // monthly advertising spend
    int n = sotuv.size();

    double ortacha = 0;
    for (double x : sotuv) ortacha += x;
    ortacha /= n;
    vector<double> tartib = sotuv;
    sort(tartib.begin(), tartib.end());
    double mediana = n % 2 == 1 ? tartib[n / 2] : (tartib[n / 2 - 1] + tartib[n / 2]) / 2;
    // sample standard deviation (STDEV.S in Excel)
    double sx = 0;
    for (double x : sotuv) sx += (x - ortacha) * (x - ortacha);
    double sd = sqrt(sx / (n - 1));
    double cv = sd / ortacha * 100;

    // correlation coefficient (CORREL in Excel)
    double ortachaR = 0;
    for (double y : reklama) ortachaR += y;
    ortachaR /= n;
    double kov = 0, sy = 0;
    for (int i = 0; i < n; i++) {
        kov += (sotuv[i] - ortacha) * (reklama[i] - ortachaR);
        sy += (reklama[i] - ortachaR) * (reklama[i] - ortachaR);
    }
    double r = kov / sqrt(sx * sy);

    printf("Months: %d\n", n);
    printf("Mean: %.2f\n", ortacha);
    printf("Median: %.2f\n", mediana);
    printf("Standard deviation: %.2f\n", sd);
    printf("Coefficient of variation: %.2f%%\n", cv);
    printf("Min: %.0f, max: %.0f\n", tartib[0], tartib[n - 1]);
    printf("Correlation with advertising: %.2f\n", r);
    return 0;
}
```

Input:

```text
42 38 45 51 47 55 60 58 49 44 53 62
3 2 4 5 4 6 7 6 5 3 5 8
```

Output:

```text
Months: 12
Mean: 50.33
Median: 50.00
Standard deviation: 7.51
Coefficient of variation: 14.92%
Min: 38, max: 62
Correlation with advertising: 0.98
```

Sales average about 50 million soums, the spread is moderate (CV ≈ 15%), and the mean and median are close — the distribution is roughly symmetric. The correlation with advertising is very strong (0.98), but this does not yet prove that advertising raised sales: both are high in the summer months. The next step is an experiment changing advertising in several regions.

:::warning
**Common mistakes**

- **Looking only at the mean.** Spread and extreme values matter too; compare with the median.
- **Confusing STDEV.S and STDEV.P.** STDEV.S is used for a sample (a part).
- **Drawing causal conclusions from correlation.**
- **Analysing data without cleaning it.** One mistyped zero corrupts the whole result.
:::

## Assignment

1. Enter the example data into Excel and check the results with `AVERAGE`, `MEDIAN`, `STDEV.S` and `CORREL`.
2. Enable the "Analysis ToolPak" (File → Options → Add-ins) and produce a "Descriptive Statistics" report.
3. Build a scatter chart of sales (y) against advertising (x), and display the trend line and R².
4. Take a 10-year series of some indicator from stat.uz and carry out a descriptive analysis.

## Summary

- Analysis: question → collection → cleaning → description → visualization → conclusion.
- Descriptive indicators: mean, median, mode, standard deviation, coefficient of variation, quartiles.
- Relationships: correlation (r), regression (y = a + b·x), R²; correlation is not causation.
- Tools: Excel and the ToolPak, SPSS, Stata, R, Python, Power BI — depending on the task and data volume.

## Check yourself

:::check
Q: When is the median better than the mean?
A: When the data is unevenly distributed or has very large extreme values (for example, salaries).
Q: What is the coefficient of variation for?
A: For comparing the spread of series of different scales in relative terms.
Q: What does r = −0.9 mean?
A: A strong negative linear relationship: when one rises, the other falls.
Q: Which Excel function calculates the sample standard deviation?
A: STDEV.S.
:::

:::link https://stat.uz/
Statistics Agency under the President of the Republic of Uzbekistan
Official statistical data on economic and social indicators.
:::
