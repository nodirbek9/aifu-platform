---
title: Sending tax reports
summary: Independent study — the step-by-step procedure for sending an electronic tax report (digital signature and sign-in, choosing the form, filling it in or importing it from accounting software, checking, signing, sending, the acceptance notification); the corrective report; the reporting calendar and deadlines; the life cycle of an electronic invoice (ESF) — creation, signing, sending, acceptance or rejection; the VAT mechanism and reconciliation with electronic invoices; the tax authority's automatic analysis; a program calculating VAT payable; keeping documents; an assignment.
---

## Purpose of this independent study

In seminar S15 we learned that tax reports are filed electronically and got to know the main taxes, and in M15 we saw reporting systems. This independent study is a **practical guide**: how a report is prepared and sent, how electronic invoices circulate and how report figures are reconciled with them.

:::note
**After this independent study you will be able to:**

- name the stages of sending an electronic report in order;
- know when and how a corrective report is filed;
- explain the life cycle of an electronic invoice;
- calculate VAT from electronic invoices and reconcile it with the report.
:::

## The stages of sending a report

| Stage | Action | Pay attention to |
|---|---|---|
| **1. Preparation** | a valid digital signature key (MT30), E-IMZO or a mobile signature | the key's expiry date and authority (the director or a power of attorney) |
| **2. Sign-in** | signing in to the taxpayer's personal account (my.soliq.uz) with the digital signature | only from the official website (security in S13) |
| **3. Choosing the form** | the type of tax, the reporting period (month, quarter, year) | the current version of the form |
| **4. Filling in** | manually or by importing a file from accounting software (M15) | the figures match the accounting registers |
| **5. Checking** | the system's format and logic checks, the list of errors | do not ignore warnings |
| **6. Signing** | signing with the digital signature | the signature means responsibility for the report's accuracy |
| **7. Sending** | the report is sent to the tax authority | do not leave it until the last day of the deadline (S15) |
| **8. Notification** | a message that it was accepted or rejected | if rejected, fix the reason and send it again |
| **9. Keeping** | an archive of the report, the notification and supporting documents | keep them for the period set by law |

## The corrective report

If an error is found in a filed report (for example, a late-arriving electronic invoice was not taken into account), a **corrective report** is filed — it replaces the previous one and shows the correct figures for the whole period. If the error reduced the tax, paying the additional tax and penalty interest quickly reduces the damage; if the tax authority finds the error itself, fines may be added. It is important to file the correction in good time, before a tax audit begins.

## The reporting calendar

Each type of tax has its own period and deadline (monthly, quarterly, annual). A practical method is a **tax calendar**: a table or electronic calendar listing every report, payment deadline and responsible employee, with a reminder 3–5 days before the deadline. Check the current deadlines in the Tax Code and on the tax committee's website — they change.

## The life cycle of an electronic invoice (ESF)

```text
Seller: create → sign with the digital signature → send
                                                   │
Buyer:                       accept (sign) or reject (with a reason)
                                                   │
        An accepted invoice is reflected in both parties' accounts and in the tax authority's system
```

- An electronic invoice contains: the parties' STIRs, the goods or services and their identification codes (IKPU, MT15), quantity, price, the VAT rate and amount.
- If there is an error — a corrected invoice or cancellation (with the buyer's consent).
- The buyer credits (deducts) VAT **only on accepted** invoices.

## The VAT mechanism

**Value added tax** is levied at each stage only on the **value added**:

- **Output VAT** — at the rate on the amount of goods and services sold.
- **Input VAT** — the VAT on accepted invoices received from suppliers; it is credited.
- **VAT payable** = output VAT − creditable input VAT. If the result is negative, the excess is carried forward to later periods or refunded in the manner set by law.

The tax authority automatically reconciles the report's figures with **electronic invoice and online cash register data** (S15): a discrepancy is grounds for questions or an audit. So check your own invoices before filing the report.

## Worked example: VAT from electronic invoices

Each line holds: the type (`sale` or `purchase`), the invoice number, the amount excluding VAT (soums) and the status (`accepted`, `rejected` or `pending`). Only accepted invoices are counted; for teaching purposes the VAT rate is 12%.

```python
import sys

QQS_STAVKA = 12     # in percent; check the current rate in the Tax Code

sotuv_qqs = xarid_qqs = 0
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    tur, raqam, summa, holat = satr.split()
    if holat != "accepted":                           # only accepted invoices are counted
        print(f"{raqam}: not counted ({holat})")
        continue
    qqs = int(summa) * QQS_STAVKA // 100
    if tur == "sale":
        sotuv_qqs += qqs
    else:
        xarid_qqs += qqs

print(f"Output VAT: {sotuv_qqs}")
print(f"Input VAT (creditable): {xarid_qqs}")
farq = sotuv_qqs - xarid_qqs
if farq >= 0:
    print(f"VAT payable: {farq}")
else:
    print(f"Excess VAT (carried forward): {-farq}")
```
```java
import java.util.Scanner;

public class Main {
    static final long QQS_STAVKA = 12;     // in percent; check the current rate in the Tax Code

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long sotuvQqs = 0, xaridQqs = 0;
        while (in.hasNext()) {
            String tur = in.next(), raqam = in.next();
            long summa = in.nextLong();
            String holat = in.next();
            if (!holat.equals("accepted")) {                  // only accepted invoices are counted
                System.out.println(raqam + ": not counted (" + holat + ")");
                continue;
            }
            long qqs = summa * QQS_STAVKA / 100;
            if (tur.equals("sale")) sotuvQqs += qqs;
            else xaridQqs += qqs;
        }
        System.out.println("Output VAT: " + sotuvQqs);
        System.out.println("Input VAT (creditable): " + xaridQqs);
        long farq = sotuvQqs - xaridQqs;
        if (farq >= 0) System.out.println("VAT payable: " + farq);
        else System.out.println("Excess VAT (carried forward): " + (-farq));
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const long long QQS_STAVKA = 12;     // in percent; check the current rate in the Tax Code

int main() {
    string tur, raqam, holat;
    long long summa, sotuvQqs = 0, xaridQqs = 0;
    while (cin >> tur >> raqam >> summa >> holat) {
        if (holat != "accepted") {                            // only accepted invoices are counted
            cout << raqam << ": not counted (" << holat << ")\n";
            continue;
        }
        long long qqs = summa * QQS_STAVKA / 100;
        if (tur == "sale") sotuvQqs += qqs;
        else xaridQqs += qqs;
    }
    cout << "Output VAT: " << sotuvQqs << "\n";
    cout << "Input VAT (creditable): " << xaridQqs << "\n";
    long long farq = sotuvQqs - xaridQqs;
    if (farq >= 0) cout << "VAT payable: " << farq << "\n";
    else cout << "Excess VAT (carried forward): " << -farq << "\n";
    return 0;
}
```

Input:

```text
sale ESF-101 25000000 accepted
sale ESF-102 18000000 accepted
sale ESF-103 7000000 rejected
purchase ESF-201 12000000 accepted
purchase ESF-202 9500000 pending
purchase ESF-203 6000000 accepted
```

Output:

```text
ESF-103: not counted (rejected)
ESF-202: not counted (pending)
Output VAT: 5160000
Input VAT (creditable): 2160000
VAT payable: 3000000
```

Note:

- **ESF-202** (a purchase, pending) was not counted — the supplier or your own employee has not accepted it yet. If it is accepted after the report is filed, 1,140,000 soums of VAT will be credited in the next period or through a corrective report. Checking "pending" invoices before filing saves money.
- **ESF-103** (a sale, rejected) — the buyer did not agree: find the reason and send a corrected invoice; otherwise actual sales and the report's figures will differ.
- This calculation is a simplified version of the automatic reconciliation in the tax authority's system.

## Keeping documents and responsibility

- Reports, notifications, electronic invoices and supporting documents are kept for the period set by law (an electronic archive, backups — M14).
- The person who signs a report is responsible for its accuracy; handing your digital signature to someone else is a serious risk (MT30).
- Fines and penalty interest are set for missed deadlines, understatement and failure to file — check the current amounts in the Tax Code.

:::warning
**Common mistakes**

- **Waiting for the last day.** The digital signature may have expired or the system may be busy.
- **Not checking "pending" and rejected invoices.** Creditable VAT is lost or a discrepancy appears.
- **Ignoring the system's warnings.** Format errors lead to the report being rejected.
- **Not correcting an error with a corrective report in time.** Penalty interest grows every day.
- **Not keeping the notification.** It becomes harder to prove the report was filed on time.
:::

## Assignment

1. Draw up a one-quarter tax calendar for a small company (a VAT payer, 5 employees): reports, payments, responsible people, reminders.
2. Add two more invoices to the example (one an accepted purchase, one a pending sale) and check the result by hand.
3. Create data in which purchase invoices exceed sales and check that the program correctly outputs "excess VAT".
4. Draw the 9 stages of sending a report as a flowchart or in BPMN (MT11).

## Summary

- Sending a report: digital signature → sign-in → form → filling in or import → checking → signature → sending → notification → keeping.
- If an error is found — a corrective report; deadlines are controlled with a tax calendar.
- Electronic invoices: creation, signing, sending, acceptance or rejection; the buyer credits VAT only on accepted invoices.
- VAT payable = output VAT − creditable input VAT; the tax authority automatically reconciles the report with electronic invoices and online cash registers.

## Check yourself

:::check
Q: What is done if an error is found in a filed report?
A: A corrective report with the correct figures for the whole period is filed.
Q: On which electronic invoices does the buyer credit VAT?
A: Only on accepted (signed) ones.
Q: How is VAT payable calculated?
A: Creditable input VAT is subtracted from output VAT.
Q: Why should "pending" invoices be checked before filing a report?
A: Until they are accepted, the input VAT is not credited and the tax payable is overstated.
:::

:::link https://my.soliq.uz/
The taxpayer's personal account
Electronic reports, electronic invoices and tax services.
:::
