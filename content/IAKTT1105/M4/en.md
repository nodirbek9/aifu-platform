---
title: Using spreadsheets in solving economic problems
summary: The structure of a spreadsheet — workbook, sheet, cell, address; data types and formats; formulas and relative, absolute and mixed references; main functions — mathematical (SUM, ROUND), statistical (AVERAGE, MAX, MIN, COUNT), logical (IF, AND, OR), lookup (VLOOKUP, XLOOKUP), conditional sums (SUMIF, COUNTIF), financial (PMT, FV, NPV); charts; sorting and filtering; pivot tables; worked economic examples — a sales report and a loan payment.
---

## Why does this topic matter?

Spreadsheets (Microsoft Excel, Google Sheets, LibreOffice Calc) are an economist's most used tool: budgets, estimates, sales reports, payroll, loan schedules, evaluating an investment project. A model built once with spreadsheet formulas recalculates automatically when the data changes — this saves time and makes "what if" analysis easy.

:::note
**After this lesson you will be able to:**

- know the structure of a spreadsheet and the data types;
- use relative, absolute and mixed references correctly;
- apply the main mathematical, statistical, logical, lookup and financial functions;
- sort, filter and chart data;
- build simple economic models in a spreadsheet.
:::

## The structure of a spreadsheet

- A **workbook** (a file, `.xlsx`) consists of several **sheets**.
- A sheet is made of **columns** (A, B, C, …) and **rows** (1, 2, 3, …); their intersection is a **cell**, with an address such as `B3`.
- A **range** is a group of cells: `A1:A10`, `B2:D5`.
- **Data types**: number, text, date and time, logical (TRUE/FALSE), formula. Number formats: currency (soums), percentage, thousands separator, number of decimal places.

## Formulas and references

A formula starts with the `=` sign: `=B2*C2`, `=SUM(D2:D10)`. Operations: `+ − * / ^` and parentheses.

| Reference type | Written as | When copied | When used |
|---|---|---|---|
| **Relative** | `B2` | the row and column shift | the same calculation for each row (price × quantity) |
| **Absolute** | `$B$2` | does not change | one common value: the VAT rate, an exchange rate |
| **Mixed** | `$B2` or `B$2` | only one part shifts | a multiplication table, two-dimensional calculations |

The `F4` key switches the reference type.

## Main functions

| Group | Function | Purpose | Example |
|---|---|---|---|
| Mathematical | `SUM`, `ROUND`, `ROUNDDOWN` | sum, rounding | `=SUM(D2:D10)`, `=ROUND(E2;0)` |
| Statistical | `AVERAGE`, `MAX`, `MIN`, `COUNT`, `MEDIAN` | average, largest/smallest, count | `=AVERAGE(D2:D10)` |
| Logical | `IF`, `AND`, `OR` | a value depending on a condition | `=IF(D2>=1000000;"large";"ordinary")` |
| Conditional | `SUMIF`, `COUNTIF`, `AVERAGEIF` | over the cells matching a condition | `=SUMIF(B2:B10;"Toshkent";D2:D10)` |
| Lookup | `VLOOKUP`, `XLOOKUP`, `INDEX`/`MATCH` | finding a value in a table | `=VLOOKUP(A2;Prices!A:B;2;FALSE)` |
| Text and date | `CONCAT`, `LEFT`, `TODAY`, `YEAR` | working with text and dates | `=YEAR(TODAY())` |
| Financial | `PMT`, `FV`, `PV`, `NPV`, `IRR` | loan payment, future value, investment effectiveness | `=PMT(24%/12;12;-10000000)` |

Note: the argument separator depends on the language settings — `;` or `,`. Function names may be translated with the interface language (for example `СУММ` in Russian Excel).

## Worked example 1: a sales report

| | A | B | C | D | E |
|---|---|---|---|---|---|
| 1 | Product | Price | Quantity | Amount | With VAT |
| 2 | Tea | 25,000 | 40 | `=B2*C2` → 1,000,000 | `=D2*(1+$H$1)` → 1,120,000 |
| 3 | Coffee | 60,000 | 15 | 900,000 | 1,008,000 |
| 4 | Sugar | 14,000 | 50 | 700,000 | 784,000 |
| 5 | Total | | `=SUM(C2:C4)` → 105 | `=SUM(D2:D4)` → 2,600,000 | 2,912,000 |

Cell `H1` holds the VAT rate of 12% (the VAT rate in Uzbekistan; check the current rate in tax legislation). In the formula of `E2`, `$H$1` is absolute — when the formula is copied down, the rate cell does not change, while `D2` becomes `D3`, `D4`. Additional indicators: the average amount `=AVERAGE(D2:D4)` → 866,667; the best seller `=MAX(D2:D4)` → 1,000,000; the share of each product `=D2/$D$5` (in percentage format) → 38.5%.

## Worked example 2: a loan payment

A bank gives a loan of 10,000,000 soums at 24% a year for 12 months with annuity (equal) payments. The monthly payment:

`=PMT(24%/12; 12; -10000000)` → **945,596** soums (approximately).

Here `24%/12` is the monthly rate (2%), `12` is the number of payments, and the loan amount is negative (money received from the bank is an inflow for you, payments are outflows). The total paid ≈ 945,596 × 12 = 11,347,152 soums, the overpayment (interest) ≈ 1,347,152 soums. By changing the rate or the term you can immediately see how the result changes — the advantage of "what if" analysis. Real loans may also include fees and insurance, so check the payment schedule given by the bank.

## Sorting, filtering and charts

- **Sorting**: Data → Sort — by one or several columns (for example, by amount in descending order).
- **Filter**: Data → Filter — showing only the needed rows (for example, only one region).
- **Conditional formatting**: highlighting values with colour (rows where the plan was not met in red).
- **Charts**: column (comparison), line (dynamics over time), pie (shares, no more than 5–6 parts), scatter (the relationship of two indicators).
- **Pivot table** (PivotTable): grouping a large table and quickly calculating sums and averages — in detail in seminar S4.

:::warning
**Common mistakes**

- **Typing a number into a formula.** Instead of `=B2*1.12`, put the rate in a separate cell (`$H$1`) — it is easy to change.
- **Forgetting an absolute reference.** When copied, the formula refers to an empty cell and gives a wrong result.
- **Storing numbers as text.** Left-aligned "numbers" are not included in the sum.
- **Confusing the format with the value.** The screen may show 866,667 while the cell holds 866,666.67.
- **Putting too many parts in a pie chart.** The chart becomes unreadable.
:::

:::tip
Build the table of worked example 1 yourself and change the VAT rate in `H1` to watch all results update automatically. Then calculate your own loan example with the `PMT` function.
:::

## Summary

- A spreadsheet: workbook, sheet, cell, range; data types and formats.
- Formulas start with `=`; relative, absolute (`$B$2`) and mixed references.
- Functions: SUM, AVERAGE, MAX/MIN, COUNT, IF, SUMIF/COUNTIF, VLOOKUP/XLOOKUP, PMT, FV, NPV.
- Sorting, filters, conditional formatting, charts and pivot tables speed up analysis.
- A spreadsheet model recalculates automatically when data changes — convenient for "what if" analysis.

## Check yourself

:::check
Q: What formula calculates the sum of the numbers in cells A1:A10?
A: =SUM(A1:A10).
Q: How is a reference that does not change when the formula is copied written?
A: An absolute reference, for example $B$2.
Q: If A1 = 12, A2 = 8, A3 = 10, what is the result of =AVERAGE(A1:A3)?
A: 10.
Q: Which function calculates the monthly payment of an annuity loan?
A: PMT.
:::

:::link https://support.microsoft.com/excel
Microsoft Excel help
Official guides to Excel functions, formulas and pivot tables.
:::
