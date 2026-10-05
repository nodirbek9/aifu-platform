---
title: Blockchain technology and its use
summary: Independent study — blockchain and distributed ledger technologies (DLT); the main platforms — Bitcoin, Ethereum, Hyperledger Fabric, Corda — and how they differ; scaling solutions (second layer, L2); transaction costs ("gas"); the stages of implementing a blockchain project (PoC, pilot, production); lessons from closed projects (TradeLens, we.trade); central bank digital currencies; the maturity of the technology; a project evaluation assignment.
---

## Purpose of this independent study

In M12 we saw the concept of blockchain, its types and a hash chain, and in S12 its use in various sectors. In this independent study we focus on **the practical choice of technology**: which platforms exist, how they differ, in which stages a project is implemented and why many well-known projects have closed.

:::note
**After this independent study you will be able to:**

- explain the difference between blockchain and distributed ledger technologies;
- compare the main platforms by their features;
- name the stages of implementing a blockchain project;
- draw lessons from unsuccessful projects.
:::

## Blockchain and DLT

**Distributed ledger technology** (DLT) is a ledger whose copies are kept by several participants and which is updated by agreement (consensus). **Blockchain** is one kind of DLT: records are grouped into blocks and linked into a chain through hashes (M12). Not every DLT takes the form of a chain of blocks — Corda, for example, stores transactions only between the parties involved.

## The main platforms

| Platform | Type | Consensus | Smart contracts | Use |
|---|---|---|---|---|
| **Bitcoin** (2009) | public, permissionless | Proof of Work | very limited | digital currency, store of value |
| **Ethereum** (2015) | public, permissionless | Proof of Stake (since 2022) | full (the Solidity language) | tokens, DeFi, NFTs, applications |
| **Hyperledger Fabric** | private, permissioned | agreement among participants (an ordering service) | yes (Go, Java, JavaScript) | inter-company processes, supply chains |
| **Corda** | private, permissioned | notary nodes | yes (Kotlin, Java) | banking and finance, trade finance |

**Permissionless** — anyone can run a node and take part; **permissioned** — only approved participants (for example, a consortium of banks). Companies usually choose permissioned networks: the participants are known, speed is higher, and keeping data confidential is easier.

## Scaling and costs

- Public blockchains process a limited number of transactions per second (Bitcoin — about 7, card payment systems — thousands).
- **Second-layer** (Layer 2) solutions — transactions are executed quickly outside the main chain, and the result is periodically written to the main chain (for example, the Lightning Network for Bitcoin, rollups for Ethereum).
- **The transaction fee** ("gas" on Ethereum) rises sharply when the network is busy; this is a problem for cheap micropayments.

## Implementation stages

| Stage | Goal | Result |
|---|---|---|
| **1. Defining the problem** | is there a trust problem? (the decision questions in S12) | is blockchain needed, or is an ordinary database enough |
| **2. PoC** (proof of concept) | showing that it works technically | a prototype, a few weeks |
| **3. Pilot** | with real participants on a limited scale | benefits and costs are measured |
| **4. Production** | moving the whole process | governance, support, a legal basis |

Many projects stop at stage 2 or 3: the technology works, but **getting the participants to agree and the business model** become the problem.

## Lessons from closed projects

- **TradeLens** (Maersk and IBM, 2018–2022) — a blockchain platform for ocean shipping documents. It worked technically, but competing carriers, worried that the platform was under Maersk's control, did not join in sufficient numbers — it closed without becoming commercially viable.
- **we.trade** (a consortium of European banks, closed in 2022) — trade finance for small businesses; it lacked funding and customers.

**Lessons**:

1. Blockchain depends on the **network effect** (MT6) — without many participants there is no value.
2. **Neutral governance** is needed — if the platform belongs to one competitor, the others do not trust it.
3. A clear **business model** — who pays and why?
4. **Integration** with existing systems and legislation — if paper documents keep their legal force, a digital record is not enough.

## Central bank digital currencies

A **CBDC** (S12) is a digital form of the national currency. Many central banks are conducting research and pilot projects (China — the e-yuan, Europe — the digital euro project, the Bahamas, Nigeria and others). Not all of them use blockchain — many projects prefer a centralized system, because the central bank is the single operator anyway. This confirms the rule from S12: if there is a single operator everyone trusts, blockchain is not necessary.

## The maturity of the technology

After the peak of "inflated expectations" in 2017–2018, blockchain has been narrowing down to areas where it delivers real results: digital assets and payments, stablecoins, asset tokenization, some parts of supply chains. The conclusion for an economist: assess every proposal critically in terms of **costs, benefits and participants**.

:::warning
**Common mistakes**

- **Starting from the technology.** Start not with "we need a blockchain" but with "what trust problem do we have?"
- **Relying only on the PoC result.** A working technical prototype does not guarantee business success.
- **Writing confidential data to a public blockchain.** A permissioned network or writing only hashes is needed.
- **Not taking transaction cost and speed into account.**
:::

## Assignment

1. Build a table comparing Hyperledger Fabric and Ethereum (access, consensus, speed, cost, confidentiality, typical users).
2. Find articles about TradeLens or we.trade and write the reasons for their closure in your own words.
3. Choose a platform for the cotton-textile cluster case in S12 and draw up a 4-stage implementation plan: the goal, duration and success criterion of each stage.

## Summary

- DLT is the general name for distributed ledgers; blockchain is the kind shaped as a chain of blocks.
- Platforms: Bitcoin and Ethereum (public), Hyperledger Fabric and Corda (enterprise, permissioned).
- Scaling — L2 solutions; the transaction fee depends on network load.
- Implementation: problem → PoC → pilot → production; success depends on participants, neutral governance and the business model.

## Check yourself

:::check
Q: What is the difference between a permissioned and a permissionless blockchain?
A: In a permissionless one anyone can take part; in a permissioned one only approved participants.
Q: Why are second-layer (Layer 2) solutions needed?
A: To solve the scaling problem by executing transactions quickly and cheaply outside the main chain.
Q: What is the main lesson of TradeLens's closure?
A: Technology is not enough — attracting competitors requires neutral governance and a clear business model.
Q: Why do many CBDC projects not use blockchain?
A: The central bank is the single trusted operator anyway, so a centralized system is more efficient.
:::

:::link https://www.hyperledger.org/
Hyperledger Foundation
Open projects for enterprise blockchain platforms.
:::
