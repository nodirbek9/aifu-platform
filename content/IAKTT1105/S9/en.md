---
title: The main components of information systems
summary: Seminar — the types of support of an information system: technical, software, information (data and classifiers), mathematical, organizational, legal, ergonomic and personnel support; how they depend on each other; analyzing the components of a real system; the task of drawing up a requirements document for an information system.
---

## Purpose of the seminar

In lecture M9 we covered the functions and types of information systems. In the seminar we look in detail at **what a system consists of** — the types of "support" — and identify each one in a real system. This knowledge shows what must not be forgotten when a system is implemented: projects often fail not because of the hardware or the program but because of missing personnel, organizational or legal support.

:::note
**After this seminar you will be able to:**

- name the types of support of an information system;
- explain the role of each type of support with examples;
- analyze the components of a real system;
- draw up a list of requirements for a simple information system.
:::

## Types of support

| Support | Meaning | Example (a university student records system) |
|---|---|---|
| **Technical** | computers, servers, networks, printers, scanners | servers, the dean's office computers, Wi-Fi |
| **Software** | OS, DBMS, application programs, security tools | a Linux server, PostgreSQL, a web application, antivirus |
| **Information** | databases, classifiers and codes, document forms | tables of students, groups, courses, grades; course codes |
| **Mathematical** | calculation methods, algorithms, models | the GPA formula, the scholarship ranking algorithm |
| **Organizational** | procedures, instructions, roles and responsibilities | who enters grades, when they are closed, who fixes errors |
| **Legal** | laws, regulations, contracts, licences | the personal data law, the university charter, the software licence |
| **Ergonomic** | convenience for the user, the interface, the workplace | clear menus, a mobile version, an interface in three languages |
| **Personnel** | users and IT specialists and their skills | teachers, dean's office staff, administrators, training |
| **Linguistic** (additional) | terms, languages, dictionaries | terms in Uzbek, Russian and English |

## How the types of support depend on each other

- If the **information** support is poor (for example course codes are duplicated), even the best program gives wrong reports.
- Without **organizational** support (who enters grades and when is not defined), data does not appear in the system on time.
- If **personnel** are not prepared, staff keep working on paper.
- **Legal** support guarantees lawful handling of personal data and compliance with software licences.

## Practical task: analyzing a system

In groups, choose one real system: the university platform, a bank's mobile app, my.gov.uz, a shop's cash register system or a library catalogue.

1. Write at least 2 specific elements for each type of support (as a table).
2. Identify the system's users and their roles.
3. List the main data objects (tables) and their relationships (M7).
4. Which type of support do you think is the weakest? Why, and how can it be improved?

## Practical task: a requirements document

Draw up a requirements document for a "Student and payment records" system for a small organization (for example a training centre):

| Section | Contents |
|---|---|
| Goal | which problem the system solves |
| Users | administrator, teacher, accountant, student |
| Functional requirements | precise sentences such as "the system lists the students with payment debts every month" |
| Data | students, groups, payments — the main fields |
| Non-functional requirements | speed, availability, security, language, mobile devices |
| Support | the resources needed for technical, software, personnel and other support |
| Constraints | budget, deadline, legal requirements |

:::warning
**Common mistakes**

- **Paying attention only to technical and software support.** If organizational and personnel support are missing, the system does not work.
- **Vague requirements.** Not "the system should be convenient" but "a new user learns the main operations in 10 minutes".
- **Not organizing classifiers and codes.** The same object is entered under different names.
- **Forgetting legal requirements.** Personal data and licences are mandatory.
:::

:::tip
When writing the requirements document, phrase each functional requirement as "the user can …" or "the system does …" and add how it can be checked (an acceptance criterion).
:::

## Summary

- The types of support of an information system: technical, software, information, mathematical, organizational, legal, ergonomic, personnel (and linguistic).
- They depend on each other: weakness in one weakens the whole system.
- Analyzing a real system and drawing up a requirements document are an economist's key skills as a system customer.

## Check yourself

:::check
Q: What does information support include?
A: Databases, classifiers and codes, document forms.
Q: Why is organizational support needed?
A: To define who enters which data, when and how, and the roles and responsibilities.
Q: Which type of support does the scholarship ranking algorithm belong to?
A: Mathematical support.
Q: How is a good functional requirement written?
A: As a precise, testable sentence: "the system does …", with an acceptance criterion.
:::

:::link https://www.iso.org/standard/72089.html
ISO/IEC/IEEE 29148: Requirements engineering
A standard for drawing up system and software requirements.
:::
