---
title: Interactive services and working with them
summary: Independent study — assessing an interactive service through the eyes of the user and the organization; the customer journey map and the service blueprint; the ten usability heuristics (Nielsen); accessibility (the WCAG principles); service quality indicators — completion rate, processing time, CSAT, NPS, CES; feedback and A/B testing; designing chatbot and messenger services; a program calculating NPS and other indicators; a service audit assignment.
---

## Purpose of this independent study

In seminar S10 we saw the concept and types of interactive services and the stages of receiving a service. In this independent study we learn methods for **assessing and improving service quality**: how to make a service convenient for the user and how to measure that convenience in numbers.

:::note
**After this independent study you will be able to:**

- build a customer journey map;
- assess a service against the usability heuristics;
- know the main accessibility requirements;
- calculate and interpret NPS, CSAT, CES and the completion rate.
:::

## The customer journey map

A **customer journey map** shows the steps a user goes through when receiving a service, their actions, feelings and problems.

| Stage | Action | Problem ("pain point") | Improvement idea |
|---|---|---|---|
| Awareness | learns that the service exists | does not know where to start | a clear name in search, a guide |
| Sign-in | signs in through OneID | has forgotten the password | a mobile digital signature, simple recovery |
| Application | fills in the form | 20 fields, unnecessary documents | pull data from registers automatically (MT17) |
| Payment | pays the state fee | has to switch to another app | a built-in payment button |
| Waiting | tracks the status | the status is not updated | SMS and push notifications |
| Result | receives the document | the PDF will not open | an electronic document with a QR code |

A **service blueprint** complements the customer journey with the "backstage" processes: which employee, which system and which integration support each step (linked to the BPMN lanes in MT11).

## The ten usability heuristics

Jakob Nielsen's well-known principles are used to assess an interface quickly:

| Heuristic | Question |
|---|---|
| 1. Visibility of system status | does the user know what is happening (loading, application status)? |
| 2. Real-world language | are the terms clear to the user and not agency jargon? |
| 3. User control and freedom | can a mistake be undone, can the user go back? |
| 4. Consistency and standards | do identical actions look the same everywhere? |
| 5. Error prevention | is the date field a calendar or free text? |
| 6. Recognition rather than recall | is previously entered data shown? |
| 7. Flexibility | are there shortcuts for experienced users? |
| 8. Minimalist design | is there no superfluous text or elements? |
| 9. Help with recovering from errors | is the error message clear and does it suggest a solution? |
| 10. Help and documentation | is there a short guide where it is needed? |

## Accessibility

A service must also be usable by people with visual, hearing or mobility impairments, older people and users with slow Internet. The international standard **WCAG** rests on four principles:

- **Perceivable** — text descriptions for images, sufficient contrast, subtitles for video.
- **Operable** — keyboard operation, enough time, large buttons.
- **Understandable** — plain language, predictable behaviour.
- **Robust** — works across browsers, screen readers and devices.

## Quality indicators

| Indicator | How it is calculated | What it shows |
|---|---|---|
| **Completion rate** | completed applications / started × 100% | whether users "drop out" somewhere |
| **Processing time** | the average time from application to result | speed, keeping to the promised deadline |
| **CSAT** (customer satisfaction) | "Were you satisfied?" (1–5) — the average or the share of 4–5 answers | satisfaction with one interaction |
| **NPS** (Net Promoter Score) | "Would you recommend it?" (0–10): % promoters (9–10) − % detractors (0–6) | overall loyalty, from −100 to +100 |
| **CES** (Customer Effort Score) | "How easy was it to get the service?" | whether there is unnecessary difficulty |

## Worked example: survey results

The first line holds 0–10 answers to the question "Would you recommend the service?", the second the numbers of started and completed applications. Calculate the NPS, the average rating and the completion rate.

```python
import sys

satrlar = sys.stdin.read().strip().split("\n")
baholar = [int(x) for x in satrlar[0].split()]
boshlangan, yakunlangan = map(int, satrlar[1].split())
n = len(baholar)

targibotchi = sum(1 for b in baholar if b >= 9)      # 9–10 points
tanqidchi = sum(1 for b in baholar if b <= 6)        # 0–6 points
neytral = n - targibotchi - tanqidchi                # 7–8 points
nps = (targibotchi - tanqidchi) * 100 / n

print(f"Responses: {n}")
print(f"Promoters: {targibotchi}, passives: {neytral}, detractors: {tanqidchi}")
print(f"NPS: {nps:.1f}")
print(f"Average rating: {sum(baholar) / n:.2f}")
print(f"Completion rate: {yakunlangan * 100 / boshlangan:.1f}%")
```
```java
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int[] baholar = Arrays.stream(in.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int boshlangan = in.nextInt(), yakunlangan = in.nextInt();
        int n = baholar.length, targibotchi = 0, tanqidchi = 0, jami = 0;
        for (int b : baholar) {
            if (b >= 9) targibotchi++;                   // 9–10 points
            if (b <= 6) tanqidchi++;                     // 0–6 points
            jami += b;
        }
        int neytral = n - targibotchi - tanqidchi;       // 7–8 points
        double nps = (targibotchi - tanqidchi) * 100.0 / n;

        System.out.println("Responses: " + n);
        System.out.println("Promoters: " + targibotchi + ", passives: " + neytral + ", detractors: " + tanqidchi);
        System.out.println(String.format("NPS: %.1f", nps));
        System.out.println(String.format("Average rating: %.2f", (double) jami / n));
        System.out.println(String.format("Completion rate: %.1f%%", yakunlangan * 100.0 / boshlangan));
    }
}
```
```cpp
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string satr;
    getline(cin, satr);
    istringstream ss(satr);
    vector<int> baholar;
    int b;
    while (ss >> b) baholar.push_back(b);
    int boshlangan, yakunlangan;
    cin >> boshlangan >> yakunlangan;

    int n = baholar.size(), targibotchi = 0, tanqidchi = 0, jami = 0;
    for (int x : baholar) {
        if (x >= 9) targibotchi++;                       // 9–10 points
        if (x <= 6) tanqidchi++;                         // 0–6 points
        jami += x;
    }
    int neytral = n - targibotchi - tanqidchi;           // 7–8 points
    double nps = (targibotchi - tanqidchi) * 100.0 / n;

    printf("Responses: %d\n", n);
    printf("Promoters: %d, passives: %d, detractors: %d\n", targibotchi, neytral, tanqidchi);
    printf("NPS: %.1f\n", nps);
    printf("Average rating: %.2f\n", (double) jami / n);
    printf("Completion rate: %.1f%%\n", yakunlangan * 100.0 / boshlangan);
    return 0;
}
```

Input:

```text
10 9 9 10 8 7 6 10 9 3 8 9 10 5 7 9 8 4 6 0
1200 840
```

Output:

```text
Responses: 20
Promoters: 9, passives: 5, detractors: 6
NPS: 15.0
Average rating: 7.35
Completion rate: 70.0%
```

The NPS is positive (+15), but detractors make up 30% — their comments should be read and the reasons grouped. The completion rate is 70%: 3 out of every 10 started applications are abandoned. The customer journey map should be used to find at which step people drop out (often uploading documents or payment) and to simplify that step.

## Feedback and A/B testing

- **Collecting feedback**: a short survey at the end of the service (1–3 questions), reviews, analysis of appeals, call centre records.
- **A/B testing**: half the users see the old version of a form and half the new one; the completion rates are compared. The decision is made on data, not on feelings.
- **"Closing the loop"**: telling a user who complained what has been changed — builds trust.

## Chatbot and messenger services

A good chatbot solves one clear task (checking a status, getting a certificate), guides with buttons, hands over to an operator when it does not understand, and asks for personal data only when necessary and through a secure channel. Check official bots by the verified badge and the link on the official website (the security rules in S10).

:::warning
**Common mistakes**

- **Designing the service for the agency.** Start from the user's steps and "pain points".
- **Looking at only one indicator.** A high NPS can go together with a low completion rate.
- **Leaving accessibility to the last stage.** Fixing it later is more expensive.
- **Collecting feedback and not responding.** Users stop answering surveys.
:::

## Assignment: a service audit

Choose one interactive service (a service on my.gov.uz, an operation in a bank app, a university system):

1. Build a customer journey map (at least 5 stages, each with a problem and an improvement idea).
2. Rate the service from 1 to 5 against the ten heuristics.
3. Find 2 shortcomings for each of the four WCAG principles.
4. Run an NPS survey among 10–20 classmates and calculate it with the example program.
5. Based on the results, write 3 priority improvement proposals.

## Summary

- The customer journey map and the service blueprint show a service through the eyes of the user and the organization.
- Nielsen's ten heuristics are a tool for quick interface assessment; WCAG is the accessibility standard.
- Indicators: completion rate, processing time, CSAT, NPS, CES.
- Improvement: feedback, A/B testing and "closing the loop".

## Check yourself

:::check
Q: How is NPS calculated?
A: The share of respondents giving 0–6 points is subtracted from the share giving 9–10 points.
Q: What should be done if the completion rate is low?
A: Use the customer journey map and data to find the step at which users drop out, and simplify that step.
Q: What are the four WCAG principles?
A: Perceivable, operable, understandable and robust.
Q: What is an A/B test?
A: Showing two groups of users two versions of a service and comparing the results.
:::

:::link https://www.nngroup.com/articles/ten-usability-heuristics/
Nielsen Norman Group: 10 Usability Heuristics
The ten usability heuristics with examples.
:::
