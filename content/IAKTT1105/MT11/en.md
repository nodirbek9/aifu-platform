---
title: Graphical representation of computational processes
summary: Independent study — graphical tools beyond the flowchart; the Business Process Model and Notation BPMN (events, tasks, gateways, swimlanes); the UML activity diagram; the data flow diagram (DFD); decision tables; checking an algorithm by hand — the trace table; tools (draw.io, Visio, Lucidchart, Camunda Modeler); a modelling assignment based on reviewing a loan application.
---

## Purpose of this independent study

In seminar S8 we learned flowcharts and their standard symbols (ISO 5807). In the economy, however, what is described is often not one program but **a process involving several departments and people**: granting a loan, procurement, approving an invoice. Other graphical languages are used for this.

:::note
**After this independent study you will be able to:**

- read and draw the main elements of BPMN;
- show responsibility with swimlanes;
- know when a UML activity diagram and a DFD are needed;
- represent complex conditions with a decision table;
- check an algorithm by hand with a trace table.
:::

## Which representation when?

| Representation | What it shows | For whom |
|---|---|---|
| **Flowchart** (S8) | the steps of one algorithm | a programmer, a student |
| **BPMN** | a business process in an organization, its participants and documents | an analyst, a manager, IT |
| **UML activity diagram** | the sequence of actions in a system and parallelism | a systems analyst, a programmer |
| **DFD** (data flow) | where data comes from, where it is stored and where it goes | an information system designer (M9) |
| **Decision table** | combinations of many conditions and their outcomes | an analyst, an auditor |

## BPMN basics

**BPMN** (Business Process Model and Notation) is the international standard for describing business processes (ISO/IEC 19510).

| Element | Symbol | Meaning |
|---|---|---|
| **Start event** | a thin circle ○ | what triggers the process: "an application has arrived" |
| **End event** | a thick circle ◉ | the end of the process: "the loan has been granted" |
| **Intermediate event** | a double-lined circle | waiting, a timer, a message: "reply within 3 days" |
| **Task** | a rectangle with rounded corners | work to be done: "Check the scoring" |
| **Exclusive gateway** | a diamond with × inside | only one path is chosen (yes / no) |
| **Parallel gateway** | a diamond with + inside | several paths are performed at the same time |
| **Sequence flow** | a solid arrow | the order of tasks |
| **Message flow** | a dashed arrow | a message between organizations |
| **Pool and lane** | horizontal bands | who performs it: a department or role |
| **Data object** | a sheet with a folded corner | a document: an application, a contract |

**Example: a consumer loan application** (lanes — Customer, Loan manager, Risk department):

```text
┌ Customer ───────┬───────────────────────────────────────────────────────────────┐
│  ○ → [Submit application] ·······································> [Receive reply] → ◉
├ Loan manager ───┼───────────────────────────────────────────────────────────────┤
│        ↓                                                      ↑         ↑
│  [Check documents] → <×> complete? ── no → [Request documents] ─┘         │
│                         │ yes                                             │
├ Risk department ┼───────┼─────────────────────────────────────────────────┤
│                         ↓                                                 │
│        <+> → [Credit history] ─┐                                          │
│         └──> [Check income] → <+> → [Scoring] → <×> score ≥ 600? ─────────┘
└─────────────────┴───────────────────────────────────────────────────────────────┘
```

The parallel gateway (<+>) means that the credit history and income checks are performed at the same time — this speeds up the process. The lanes show **who is responsible** for each step.

## The UML activity diagram

UML (Unified Modeling Language) is a language for modelling software systems. An activity diagram resembles a flowchart but adds **parallel actions** (a thick bar — "fork" and "join") and **partitions**. For example, in an online shop, once payment is confirmed, "send the receipt" and "order from the warehouse" run in parallel.

## The data flow diagram (DFD)

A DFD shows not the sequence of a process but **the movement of data**. Four elements:

| Element | Example |
|---|---|
| **External entity** (rectangle) | Customer, the Tax Committee |
| **Process** (circle or rounded rectangle) | "Process the order" |
| **Data store** (open rectangle) | the "Orders" database |
| **Data flow** (arrow) | "order details", "electronic invoice" |

A DFD helps determine which tables and integrations are needed when designing an information system (M9, MT8).

## Decision tables

When there are many conditions, a flowchart turns into a "forest" of diamonds. A **decision table** shows all combinations compactly. An example — the decision on a loan application:

| Condition / Rule | 1 | 2 | 3 | 4 |
|---|---|---|---|---|
| Scoring score ≥ 600 | yes | yes | no | no |
| Monthly payment ≤ 50% of income | yes | no | yes | no |
| **Action** | approve | offer a smaller amount | review with a guarantor | reject |

The table is clear to an auditor too: every case has a definite rule and no combination has been "forgotten".

## The trace table

To check an algorithm or a flowchart without a computer, a **trace table** is built: the values of the variables are written down at every step. An example — the sum of monthly revenues and the largest month:

```text
revenues: 40, 55, 35, 60
total = 0, largest = 0
FOR EACH r:
    total = total + r
    IF r > largest THEN largest = r
```

| Step | r | total | r > largest? | largest |
|---|---|---|---|---|
| start | — | 0 | — | 0 |
| 1 | 40 | 40 | yes | 40 |
| 2 | 55 | 95 | yes | 55 |
| 3 | 35 | 130 | no | 55 |
| 4 | 60 | 190 | yes | 60 |

Result: total 190, largest month 60. A trace table helps find errors before writing a program — for example, if `largest` starts at 0, the algorithm works wrongly when all revenues are negative (losses).

## Tools

| Tool | Features |
|---|---|
| **draw.io** (diagrams.net) | free, in the browser, BPMN, UML and flowchart shapes |
| **Microsoft Visio** | corporate, with Microsoft 365 |
| **Lucidchart** | cloud-based, collaborative |
| **Camunda Modeler, Bizagi** | BPMN modelling and process automation |
| **PlantUML, Mermaid** | diagrams from text (stored as code) |

:::warning
**Common mistakes**

- **A process without lanes.** Who is responsible remains unclear.
- **No labels on the paths leaving a gateway** ("yes", "no", the condition). The reader does not know which path is chosen when.
- **Everything in one diagram.** Split a large process into smaller sub-processes.
- **A combination missing from the decision table.** With n yes/no conditions there should be 2ⁿ columns (or merged ones should be shown).
:::

## Assignment

1. Draw the process of applying to a university (or ordering from an online shop) in BPMN with at least 3 lanes (in draw.io).
2. The process must contain at least one exclusive and one parallel gateway.
3. Draw a DFD for the same process: external entities, processes, data stores.
4. Build a decision table with 3 conditions for a "salary bonus" rule (for example, plan fulfilment, length of service, discipline).
5. Check the trace table example with revenues of −10, −5, −20 and fix the error.

## Summary

- A flowchart is for one algorithm; for organizational processes BPMN is used, for systems UML and DFD.
- BPMN: events, tasks, gateways (× and +), flows, pools and lanes.
- A DFD shows the movement of data; a decision table expresses rules with many conditions compactly.
- A trace table is a tool for checking an algorithm by hand and finding errors early.

## Check yourself

:::check
Q: What does a lane mean in BPMN?
A: The department or role performing the steps of the process.
Q: What is the difference between an exclusive and a parallel gateway?
A: In an exclusive gateway only one path is chosen; in a parallel one several paths are performed at the same time.
Q: How does a DFD differ from a flowchart?
A: It shows not the sequence of actions but where data comes from, where it is stored and where it goes.
Q: How many rules (columns) does a decision table with 3 yes/no conditions have?
A: 2³ = 8.
:::

:::link https://www.bpmn.org/
BPMN.org
Official information and examples for the BPMN notation.
:::
