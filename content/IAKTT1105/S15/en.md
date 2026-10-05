---
title: Tax reports and types of bank loans and deposits
summary: Seminar — electronic tax reporting (the taxpayer's personal account, EDS, deadlines, electronic invoices, online cash registers); main taxes (VAT, profit tax, turnover tax, personal income tax, social tax); a payroll tax calculation program; types of bank loans (consumer, mortgage, car loans, microloans, business loans) and repayment methods (annuity, differentiated); types of deposits (demand, term, savings) and types of interest (simple, compound); the effective interest rate; financial literacy.
---

## Purpose of the seminar

An economist works with tax reports and bank products every day. In the seminar we see how reports are filed electronically and how payroll taxes are calculated, and we compare the types of loans and deposits. The seminar's programming task — calculating deposit interest — is done independently.

:::note
**After this seminar you will be able to:**

- explain the procedure for filing tax reports electronically;
- calculate the main taxes and the taxes on salaries;
- distinguish types of loans and repayment methods;
- explain the types of deposits, simple and compound interest.
:::

## Electronic tax reporting

- **The taxpayer's personal account** (my.soliq.uz): preparing and sending reports, notifications of acceptance, viewing tax debts and overpayments, obtaining certificates.
- Reports are signed with an **electronic digital signature** (S14) — no paper forms or visits to the tax office are needed.
- **Deadlines**: monthly, quarterly or annual depending on the tax; missing a deadline means fines and penalties.
- **Electronic invoices (ESF)** and **online cash registers** give the tax authority data in real time, so the report figures must match them.
- Accounting systems (M15) can generate reports automatically and send them directly.

## Main taxes

| Tax | Levied on | Note |
|---|---|---|
| **Value added tax (VAT)** | sales of goods and services | VAT on purchases is credited (deducted) |
| **Profit tax** | a company's profit | for large companies |
| **Turnover tax** | revenue | a simplified regime for small business |
| **Personal income tax (PIT)** | an employee's salary | withheld and paid by the employer |
| **Social tax** | the payroll | paid by the employer at its own expense |
| **Property, land and water use taxes** | assets and resources | |

:::warning
Tax rates and exemptions change over time. Before calculating, check the current rates in the **Tax Code** (lex.uz) and on the tax committee's website. The rates in the example below are for learning purposes.
:::

## Worked example: payroll taxes

Each line holds an employee's surname and the gross monthly salary (soums). PIT is 12% (withheld from the salary), social tax is 12% (paid by the employer), and taxes are rounded down to whole soums. Output the figures for each employee and the totals for the report.

```python
import sys

JSHDS = 12        # percent, withheld from the employee's salary
IJTIMOIY = 12     # percent, paid by the employer

jami = [0, 0, 0, 0]
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    familiya, hisob = satr.split()
    hisob = int(hisob)
    jshds = hisob * JSHDS // 100
    qolga = hisob - jshds                       # the employee's take-home pay
    ijtimoiy = hisob * IJTIMOIY // 100
    print(f"{familiya}: gross {hisob}, PIT {jshds}, net {qolga}, social tax {ijtimoiy}")
    for i, x in enumerate([hisob, jshds, qolga, ijtimoiy]):
        jami[i] += x
print(f"Total: gross {jami[0]}, PIT {jami[1]}, net {jami[2]}, social tax {jami[3]}")
```
```java
import java.util.Scanner;

public class Main {
    static final int JSHDS = 12;        // percent, withheld from the employee's salary
    static final int IJTIMOIY = 12;     // percent, paid by the employer

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long[] jami = new long[4];
        while (in.hasNext()) {
            String familiya = in.next();
            long hisob = in.nextLong();
            long jshds = hisob * JSHDS / 100;
            long qolga = hisob - jshds;                  // the employee's take-home pay
            long ijtimoiy = hisob * IJTIMOIY / 100;
            System.out.println(familiya + ": gross " + hisob + ", PIT " + jshds + ", net " + qolga + ", social tax " + ijtimoiy);
            long[] satr = {hisob, jshds, qolga, ijtimoiy};
            for (int i = 0; i < 4; i++) jami[i] += satr[i];
        }
        System.out.println("Total: gross " + jami[0] + ", PIT " + jami[1] + ", net " + jami[2] + ", social tax " + jami[3]);
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const int JSHDS = 12;        // percent, withheld from the employee's salary
const int IJTIMOIY = 12;     // percent, paid by the employer

int main() {
    string familiya;
    long long hisob, jami[4] = {0, 0, 0, 0};
    while (cin >> familiya >> hisob) {
        long long jshds = hisob * JSHDS / 100;
        long long qolga = hisob - jshds;                 // the employee's take-home pay
        long long ijtimoiy = hisob * IJTIMOIY / 100;
        cout << familiya << ": gross " << hisob << ", PIT " << jshds << ", net " << qolga
             << ", social tax " << ijtimoiy << "\n";
        long long satr[4] = {hisob, jshds, qolga, ijtimoiy};
        for (int i = 0; i < 4; i++) jami[i] += satr[i];
    }
    cout << "Total: gross " << jami[0] << ", PIT " << jami[1] << ", net " << jami[2]
         << ", social tax " << jami[3] << "\n";
    return 0;
}
```

Input:

```text
Aliyev 5000000
Karimova 7350000
Tursunov 12400000
```

Output:

```text
Aliyev: gross 5000000, PIT 600000, net 4400000, social tax 600000
Karimova: gross 7350000, PIT 882000, net 6468000, social tax 882000
Tursunov: gross 12400000, PIT 1488000, net 10912000, social tax 1488000
Total: gross 24750000, PIT 2970000, net 21780000, social tax 2970000
```

For the company the total cost of these employees is 24,750,000 + 2,970,000 = 27,720,000 soums (payroll + social tax). The figures in the "Total" line go into the PIT and social tax reports.

## Bank loans

| Type | Purpose | Features |
|---|---|---|
| **Consumer loan** | household appliances, repairs, education | short and medium term, relatively high interest |
| **Mortgage** | buying housing | long term (10–20 years), the home is collateral, a down payment |
| **Car loan** | a car | the car is collateral |
| **Microloan** | a small amount, fast | high interest, short term |
| **Business loans** | working capital, equipment, investment projects | a business plan, collateral, guarantees |
| **Credit cards and overdrafts** | short-term needs | a grace period, then high interest |

**Repayment methods**:

- **Annuity** — the same amount every month; at first most of the payment is interest, at the end it is principal. Easy to plan.
- **Differentiated** — an equal part of the principal each month plus interest on the outstanding balance; payments are large at first and then decrease. Total interest is lower than with an annuity.

**Important**: compare loans not by the nominal rate but by the **annual effective interest rate** (including all fees and charges) — the bank must state it in the contract. Monthly payments should not take up a large share of income (the debt burden).

## Bank deposits

| Type | Features |
|---|---|
| **Demand deposit** | money can be withdrawn at any time, low or no interest |
| **Term deposit** | for a fixed period, higher interest; early withdrawal reduces the interest |
| **Savings deposit** | can be topped up regularly, for saving towards a goal |
| **Foreign currency deposit** | in a foreign currency, interest is usually lower, exchange rate risk |

**Types of interest**:

- **Simple interest** — each year interest is calculated only on the **initial amount**; the balance grows by the same amount every year.
- **Compound interest (capitalization)** — interest is calculated on the **current balance**, that is, together with earlier interest; the balance grows faster and faster ("interest on interest").

Over a long period compound interest makes a noticeable difference. Calculating both methods year by year is the seminar's **programming task**: do it independently using a loop (an iterative algorithm).

The **real return** on a deposit also depends on inflation: if the deposit rate is 20% and inflation is 10%, purchasing power grows by roughly 10%. Deposits are guaranteed by law (the bank deposit guarantee system).

:::warning
**Common mistakes**

- **Comparing loans only by the nominal rate.** Fees and insurance raise the real cost.
- **Leaving the report until the last day.** A system overload or an EDS problem can make you miss the deadline.
- **ESF and report figures not matching.** The tax authority detects discrepancies automatically.
- **Ignoring inflation.** A high nominal rate does not mean a real return.
:::

## Practical tasks

1. Add a 4th employee (8,125,000 soums) to the example and check the total line by hand.
2. Compare the terms of consumer loans for the same amount at two banks (rate, fees, term): which is cheaper?
3. Discuss the advantages of annuity and differentiated payments from a household budget point of view.
4. Complete the programming task on deposit interest.

## Summary

- Tax reports are filed through my.soliq.uz with an EDS; they must match ESF and online cash register data.
- Main taxes: VAT, profit tax, turnover tax, personal income tax, social tax and others.
- Loans: consumer, mortgage, car, microloan, business; repayment by annuity or differentiated payments; compare by the effective rate.
- Deposits: demand, term, savings, foreign currency; simple and compound interest; the real return depends on inflation.

## Check yourself

:::check
Q: How is a tax report signed?
A: With an electronic digital signature, through the taxpayer's personal account.
Q: What is the difference between personal income tax and social tax?
A: Personal income tax is withheld from the employee's salary; social tax is paid by the employer at its own expense on the payroll.
Q: What is an annuity payment?
A: A payment of the same amount every month over the loan term.
Q: What is the difference between simple and compound interest?
A: Simple interest is calculated only on the initial amount, compound interest on the current balance including earlier interest.
:::

:::link https://soliq.uz/
State Tax Committee of the Republic of Uzbekistan
Tax rates, report forms, deadlines and electronic services.
:::
