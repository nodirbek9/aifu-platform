---
title: Databases and models for developing them
summary: Independent study — data models (hierarchical, network, relational, document, key-value, graph); database design stages (requirements, conceptual, logical, physical model, implementation); ER modelling — entities, attributes, relationships and cardinality; keys and integrity constraints; normalization (1NF, 2NF, 3NF) using a sales table; anomalies; choosing between SQL and NoSQL; a design assignment.
---

## Purpose of this independent study

In M7 we got to know the concept of a database and SQL queries (JOIN, GROUP BY). In this independent study we step back: **how tables are designed** so that data is not duplicated, errors are fewer and queries give correct results.

:::note
**After this independent study you will be able to:**

- distinguish the main data models;
- name the stages of database design;
- draw a simple ER diagram;
- normalize a table up to the third normal form;
- choose a relational or NoSQL database for a task.
:::

## Data models

| Model | Structure | Example / use |
|---|---|---|
| **Hierarchical** | a tree: each record has one "parent" | file systems, old banking systems (IBM IMS) |
| **Network** | a graph of records linked many-to-many | systems of the 1970s |
| **Relational** | tables (relations) linked through keys | PostgreSQL, MySQL, Oracle, SQL Server — accounting, banking, ERP |
| **Document** (NoSQL) | JSON-like documents, a flexible structure | MongoDB — product catalogues, content |
| **Key-value** (NoSQL) | fast lookup by key | Redis — sessions, caches, shopping baskets |
| **Graph** (NoSQL) | nodes and edges | Neo4j — social connections, fraud networks |

The relational model (E. Codd, 1970) is still the core of economic systems: a strict structure, transactions and integrity guarantees (ACID) matter for finance.

## Design stages

| Stage | Result |
|---|---|
| **1. Gathering requirements** | which data, who uses it, which reports are needed |
| **2. Conceptual model** | an ER diagram: entities and relationships (independent of the DBMS) |
| **3. Logical model** | tables, columns, keys, normalization |
| **4. Physical model** | data types, indexes, tuning for a specific DBMS |
| **5. Implementation and support** | creation, data migration, backups, optimization |

## ER modelling

- **Entity** — an object about which data is stored: Customer, Order, Product.
- **Attribute** — a property of an entity: Customer → name, phone, city.
- **Relationship** and **cardinality**:

| Cardinality | Example |
|---|---|
| **1 : 1** | Employee — Passport |
| **1 : N** (one-to-many) | Customer — Orders (one customer has many orders) |
| **N : M** (many-to-many) | Order — Product (an order has many products, a product is in many orders) |

An N : M relationship is implemented in a relational database with a **junction table** (for example, OrderLine: order, product, quantity).

```text
 Customer 1 ──── N Order 1 ──── N OrderLine N ──── 1 Product
```

## Keys and integrity

- **Primary key** — uniquely identifies each row (customer_id). One per table.
- **Foreign key** — a reference to another table's primary key (Order.customer_id → Customer.customer_id).
- **Referential integrity** — you cannot record an order for a customer who does not exist; deleting a customer who has orders is restricted.
- **Other constraints**: `NOT NULL` (required), `UNIQUE` (no duplicates, for example a tax ID), `CHECK` (price > 0).

## Normalization: a worked example

A shop keeps its sales in one Excel table:

| order | date | customer | customer_phone | product | price | quantity |
|---|---|---|---|---|---|---|
| 101 | 03.10 | Aliyev | 901112233 | Teapot, Cup | 85000, 15000 | 1, 6 |
| 102 | 03.10 | Karimova | 935556677 | Teapot | 85000 | 2 |
| 103 | 04.10 | Aliyev | 901112233 | Cup | 15000 | 12 |

**Anomalies**:

- **Update**: if Aliyev changes his phone number, it must be changed in all rows — if one is missed, the data becomes contradictory.
- **Insertion**: a new product that has not yet been sold (with its price) cannot be entered — an order is needed.
- **Deletion**: if order 102 is deleted, the information about Karimova is lost too.

**1NF** (first normal form) — one value per cell. Order 101 is split into two rows: (101, Teapot, 85000, 1) and (101, Cup, 15000, 6).

**2NF** — every column depends on the whole key. After 1NF the key is (order, product), but the price depends only on the product, and the date and customer only on the order. The tables are split:

- **Order** (order, date, customer, customer_phone)
- **Product** (product, price)
- **OrderLine** (order, product, quantity)

**3NF** — a non-key column does not depend on another non-key column. In Order, customer_phone depends not on the order but on the customer. The result:

| Table | Columns | Key |
|---|---|---|
| **Customer** | customer_id, name, phone | customer_id |
| **Product** | product_id, name, price | product_id |
| **Order** | order_id, date, customer_id | order_id; customer_id — a foreign key |
| **OrderLine** | order_id, product_id, quantity | (order_id, product_id) |

Now the phone number is stored in one place, a product can be entered without an order, and the customer remains if an order is deleted. Reports are assembled with JOIN (M7).

:::tip
An important subtlety: a product's price changes over time. So that the totals of old orders do not change, in practice the **price at the time of sale** is also written to OrderLine — this is not duplication but a historical fact.
:::

## SQL or NoSQL?

| Situation | Choice |
|---|---|
| Money, accounts, balances — accuracy and transactions matter | relational (SQL) |
| A catalogue whose structure changes often, products with different properties | document |
| Very fast reads, temporary data | key-value |
| Analysing connections (who transferred money to whom) | graph |
| Large volumes, analytics (Big Data, M7, S7) | distributed systems, data warehouses |

Many companies use several types together ("polyglot persistence").

:::warning
**Common mistakes**

- **One big table ("Excel style").** Anomalies and contradictory data.
- **Several values in one cell** ("Teapot, Cup") — filtering and calculation become impossible.
- **Using a name or phone number as a key.** They repeat and change; use a dedicated identifier.
- **Over-normalizing.** If a report needs 10 JOINs, partial duplication is deliberately allowed in an analytical database.
:::

## Assignment

Design a database for a training centre: students, teachers, courses, groups, payments.

1. Write down the entities and their attributes.
2. Determine the relationships and cardinality (for example, student — group: N : M).
3. Draw an ER diagram (on paper or in draw.io).
4. Write the tables in 3NF: columns, primary and foreign keys.
5. Which tables must be joined for a "monthly revenue by course" report?

## Summary

- Models: hierarchical, network, relational (the main one), document, key-value, graph.
- Design: requirements → conceptual (ER) → logical → physical → implementation.
- Keys and constraints ensure data integrity.
- Normalization (1NF–3NF) removes duplication and anomalies; SQL or NoSQL is chosen to suit the task.

## Check yourself

:::check
Q: What is an update anomaly?
A: When the same data is stored in several places, it must be changed everywhere, and missing one place causes a contradiction.
Q: What does 1NF require?
A: That every cell holds only one (atomic) value.
Q: How is a many-to-many relationship implemented in a relational database?
A: With a junction table containing the keys of both tables.
Q: What is a foreign key?
A: A column referring to another table's primary key.
:::

:::link https://www.postgresql.org/docs/current/ddl-constraints.html
PostgreSQL: Constraints
The official documentation on primary and foreign keys and other constraints.
:::
