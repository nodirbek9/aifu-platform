---
title: The main components of information systems
summary: Independent study — the five-component model (hardware, software, data, procedures, people) and the network; types of data — master, transactional, reference data (classifiers: taxpayer ID, the classifier of economic activities, bank MFO codes), metadata; the three-tier architecture (presentation, business logic, data); integration — APIs, file exchange, an integration bus; analysing the components of the online cash register system; the "weakest link" principle; an assignment.
---

## Purpose of this independent study

In seminar S9 we looked at an information system by its types of support (technical, software, information, organizational, legal and others). In this independent study we study the **five-component model** widely used in the international literature and the internal structure of modern systems — tiers and integration.

:::note
**After this independent study you will be able to:**

- explain the five components of an information system and how they depend on each other;
- name the main types of data and the role of classifiers;
- describe the three-tier architecture;
- distinguish the methods of integrating systems.
:::

## The five-component model

| Component | Content | Example (a bank branch) |
|---|---|---|
| **Hardware** | computers, servers, network equipment, cash registers and terminals | an operator's computer, an ATM, a server |
| **Software** | the operating system, the DBMS, application programs | the core banking system (M15), a mobile app |
| **Data** | facts and records | customers, accounts, transactions |
| **Procedures** | instructions on how to use the system | the account opening regulation, the end-of-day closing procedure |
| **People** | users, administrators, developers | an operator, a cashier, an IT employee, a customer |

The **network** (M6, MT5) connects them. The five components divide into two "wings": on the left hardware and software (**the computer side**), on the right procedures and people (**the human side**), and in the middle **data**, which links the two.

:::tip
When a system is implemented, most attention usually goes to hardware and software, but most problems arise on the side of **procedures and people**: there is no manual, staff are not trained, responsibilities are not defined.
:::

## Types of data

| Type | Content | Example | Feature |
|---|---|---|---|
| **Master data** | the core business objects | customers, products, employees, counterparties | changes rarely, used in many systems |
| **Transactional data** | events and operations | sales, payments, receipts and issues | very numerous, keeps growing |
| **Reference data** (classifiers) | lists of standard codes | currency codes, countries, activity types | taken from external standards |
| **Metadata** | data about data | a column's name and type, who changed it and when | for finding and understanding data (MT9) |

**Important classifiers and identifiers in Uzbekistan**:

- **STIR** — the taxpayer identification number (companies and individuals).
- **JSHSHIR (PINFL)** — the personal identification number of an individual.
- **IFUT** — the national classifier of types of economic activity.
- **Bank MFO code** — the code of a bank branch; used in payment orders.
- **Identification codes of goods and services (IKPU)** — in electronic invoices and online cash register receipts.

Classifiers are needed so that different systems "understand" each other: the bank, tax and statistics systems recognize a company by the same STIR.

**Master data management** (MDM): if one customer is recorded differently in the CRM, accounting and website databases (the "uniqueness" problem from MT9), reports become contradictory. MDM means creating a single "source of truth" for the core objects.

## The three-tier architecture

| Tier | Role | Example |
|---|---|---|
| **Presentation** (interface) | interaction with the user | a web page, a mobile app, a cash register screen |
| **Business logic** (application server) | rules and calculations | calculating tax, a loan decision (MT13), checking limits |
| **Data** | storage and retrieval | the DBMS (MT8) |

The advantages of tiers: an interface (for example, a new mobile app) can be added without changing the business logic; each tier scales separately; security — the user cannot access the database directly.

## Integration

A company usually runs dozens of systems (accounting, CRM, website, warehouse, bank-client). Ways of exchanging data between them:

| Method | Content | Advantage / drawback |
|---|---|---|
| **Manual** | export to Excel and import into another system | simple, but slow and error-prone |
| **File exchange** | XML or CSV files at set times | straightforward; not real-time |
| **API** | systems send requests to each other through a program | real-time, automatic; requires development |
| **Integration bus (ESB)**, message queues | exchange through a central "post office" | order for many systems; complex |

In the public sector the inter-agency integration platform (M10) works on the same idea: a citizen does not carry a certificate from one agency to another — the systems exchange it through an API.

## Example: the online cash register system

| Component | Composition |
|---|---|
| **Hardware** | an online cash register or a smartphone with a cash register app, a fiscal module, a receipt printer, the Internet |
| **Software** | the cash register program, the fiscal data operator's server, the tax authority's system |
| **Data** | the receipt (transactional), IKPU codes and STIR (classifiers), the seller and goods (master) |
| **Procedures** | issuing a receipt for every sale, opening and closing a shift, what to do in case of a failure |
| **People** | the cashier, the shop owner, the tax inspector, the buyer (checks the receipt via QR) |
| **Integration** | cash register → operator → tax authority (in real time), buyer → receipt checking service |

If one component of this system does not work (the Internet goes down, the cashier does not issue a receipt), the whole system's goal — making trade transparent — is undermined. This is **the "weakest link" principle**: a system's effectiveness is determined by its weakest component.

:::warning
**Common mistakes**

- **Seeing an information system as just a program.** It does not work without people, procedures and data.
- **Ignoring classifiers.** A wrong IKPU or STIR — the invoice is rejected.
- **Keeping one object differently in several systems.** Master data management is needed.
- **Connecting systems by hand.** Copying via Excel is a source of errors and delays.
:::

## Assignment

Choose one information system (a university LMS, a bank's mobile app, the my.gov.uz portal, an online shop):

1. Describe the five components in a table.
2. Give 2 examples each of master, transactional and reference data in the system.
3. Show the three tiers: what the user sees, which rules work, where the data is stored.
4. Which external systems is it integrated with, and by which method?
5. Find the system's "weakest link" and propose how to strengthen it.

## Summary

- Five components: hardware, software, data, procedures, people; the network connects them.
- Data: master, transactional, reference (classifiers), metadata; MDM creates a single source of truth.
- Three tiers: presentation, business logic, data.
- Integration: manual, file, API, integration bus; a system works at the level of its weakest link.

## Check yourself

:::check
Q: What are the components of the five-component model?
A: Hardware, software, data, procedures and people.
Q: What is the difference between master and transactional data?
A: Master data are the business's rarely changing objects (customers, products); transactional data are events and operations (sales, payments).
Q: Why are classifiers needed?
A: So that different systems understand the same objects through common standard codes.
Q: What is the advantage of integration through an API?
A: Data is exchanged in real time and automatically, without manual work.
:::

:::link https://lex.uz/
Lex.uz
National classifiers and regulatory documents on information systems.
:::
