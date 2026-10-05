---
title: Classification of information systems
summary: Independent study — classifying information systems by different characteristics: level of automation (manual, automated, automatic), nature of processing (information retrieval, information processing — managerial and advisory), level of management, functional area, field of application (banking, tax, statistics, accounting, geographic information, healthcare, education), scale (personal, group, corporate, national), architecture (file-server, client-server, multi-tier, cloud), type of data (factual, documentary); an assignment to build a classification table.
---

## Purpose of this independent study

In M9 information systems were classified by management level (TPS, MIS, DSS, EIS) and functional area. But one system can be described by many other characteristics too. In this independent study we build a complete classification scheme — it is needed when choosing, comparing and documenting systems.

:::note
**After this independent study you will be able to:**

- classify information systems by at least eight characteristics;
- give an example from Uzbekistan and the world for each class;
- build a complete classification "passport" for a real system.
:::

## By level of automation

| Type | Content | Example |
|---|---|---|
| **Manual** | a person performs all operations, without a computer | a paper journal, a card index |
| **Automated** | a person and a computer together; the decision stays with the person | an accounting program, CRM — most systems in the economy |
| **Automatic** | without human intervention | automatic payment transfers, controlling a production line, algorithmic trading on an exchange |

## By nature of processing

| Type | Content | Example |
|---|---|---|
| **Information retrieval** | finding and providing information, without calculations | Lex.uz, a library catalogue, reference systems |
| **Information processing: managerial** | performs calculations and produces results for management decisions | payroll, warehouse, planning |
| **Information processing: advisory** | offers options and recommendations; a person makes the decision | decision support (DSS), expert systems, scoring |

## By level of management and functional area

This was covered in detail in M9: **operational** (TPS), **management** (MIS), **decision support** (DSS), **executive** (EIS); functional — **accounting**, **human resources**, **marketing**, **production**, **finance**; enterprise — **ERP**, **CRM**, **SCM**.

## By field of application

| Field | Systems |
|---|---|
| **Banking** | core banking, card processing, internet banking (S13, M15) |
| **Tax** | the taxpayer's personal account, electronic invoices, online cash registers (S15) |
| **Statistics** | systems for collecting and processing statistical reports |
| **Accounting** | 1C, international equivalents (M15) |
| **Public administration** | the single interactive public services portal, inter-agency systems (M10) |
| **Geographic information (GIS)** | map-based data: the land cadastre, logistics routes, analysis of retail locations |
| **Healthcare** | electronic medical records, hospital systems |
| **Education** | LMS, student records |
| **Legal** | legislation databases, court document systems |

## By scale

| Type | Users | Example |
|---|---|---|
| **Personal (single-user)** | one user | a household budget app, a personal Excel model |
| **Group** | a department or small team (dozens of people) | departmental documents, project management |
| **Corporate** | the whole organization, including branches | ERP, core banking |
| **Inter-sectoral / national** | many organizations and citizens | the tax system, the public services portal |
| **Global** | worldwide | SWIFT, international booking systems |

## By architecture

| Type | Content | Feature |
|---|---|---|
| **Local (stand-alone)** | the program and the data on one computer | simple; no collaboration |
| **File-server** | data files on a shared server, processing on each computer | heavy network load, slow with many users |
| **Client-server** | the DBMS server processes queries, the client receives only the result | reliable, suits many users |
| **Multi-tier** | presentation, business logic and data separate (MT15) | scales, web and mobile apps |
| **Cloud (SaaS)** | the system on the provider's server, via the browser (M11) | quick start, subscription fee |

## By type of data

- **Factual** — specific facts in structured form: numbers, dates, names (accounting, warehouse).
- **Documentary** — text documents and searching them: electronic document management, archives, Lex.uz.
- **Multimedia and spatial** — images, video, maps (GIS, video surveillance).

## A classification "passport" — an example

**System**: the single interactive public services portal (my.gov.uz).

| Characteristic | Class |
|---|---|
| Level of automation | automated (many services automatic) |
| Nature of processing | information processing (receiving and processing applications) + information retrieval (the service catalogue) |
| Level of management | operational (G2C services) |
| Field of application | public administration |
| Scale | national |
| Architecture | multi-tier, web and mobile, with inter-agency integration |
| Type of data | factual and documentary |

:::tip
Classification is not a theoretical exercise: when buying or developing a system, the "passport" clarifies the requirements — for example, the description "corporate, client-server, automated, accounting" makes it easier to compare vendors.
:::

:::warning
**Common mistakes**

- **Putting one system into only one class.** Classification is multidimensional — separately for each characteristic.
- **Confusing "automated" and "automatic".** In the first, the decision stays with a person.
- **Not distinguishing file-server from client-server.** With many users a file-server slows down sharply.
- **Simplifying a cloud system to "a website on the Internet".** It involves data location, SLA and security issues (S11).
:::

## Assignment

1. Build a classification "passport" for three systems: (a) a bank's mobile app, (b) a university LMS, (c) an online cash register in a small shop.
2. Find at least one example from Uzbekistan for each characteristic (8 in all) and build a table.
3. Describe how one system changed its class over time (for example, from file-server accounting to cloud accounting).

## Summary

- Information systems are classified by many characteristics: automation, nature of processing, level of management, function, field, scale, architecture, type of data.
- Most systems in the economy are automated and information-processing.
- Architecture is moving from local and file-server towards client-server, multi-tier and cloud systems.
- A classification "passport" is a practical tool for choosing and comparing systems.

## Check yourself

:::check
Q: What is the difference between an automated and an automatic system?
A: In an automated system a person and a computer work together and the person makes the decision; in an automatic one the process runs without human intervention.
Q: Give an example of an information retrieval system.
A: The Lex.uz legislation database or a library catalogue.
Q: What is the main difference between file-server and client-server architecture?
A: In a file-server system data is processed on each computer; in client-server the server executes the query and sends only the result to the client.
Q: Where is a geographic information system used in the economy?
A: In the land cadastre, logistics routes and analysing the location of retail outlets.
:::

:::link https://my.gov.uz/
Single interactive public services portal
An example of a national-scale information system.
:::
