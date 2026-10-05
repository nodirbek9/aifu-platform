---
title: Applying blockchain technologies in various sectors
summary: Seminar — when blockchain is needed and when it is not (decision questions); use by sector — finance and payments, supply chains, state registers, education (diplomas), healthcare, energy, insurance; smart contract examples; tokenization and digital assets; regulation of crypto-assets in Uzbekistan; limitations and risks; a case and group tasks.
---

## Purpose of the seminar

In lecture M12 we saw how blockchain works — blocks, hashes, consensus, smart contracts. In the seminar we analyze **where it truly adds value** and where an ordinary database is enough.

:::note
**After this seminar you will be able to:**

- apply the questions that decide whether blockchain is needed;
- give examples of blockchain use in different sectors;
- explain the idea of smart contracts and tokenization;
- assess the limitations and risks of blockchain.
:::

## Is blockchain needed? Decision questions

| Question | If "yes" |
|---|---|
| Do several **independent parties** maintain the data? | blockchain may be useful |
| Do the parties **not fully trust** each other or a single central operator? | blockchain is useful |
| Must records be **immutable**, with a verifiable history? | blockchain is useful |
| Is there a **single operator** everyone trusts (for example, one company)? | an ordinary DBMS (M7) is cheaper and faster |
| Are very many transactions and low latency needed? | be careful — public blockchains are slower |
| Is the data confidential, or might it have to be deleted? | do not write it to a public blockchain |

:::tip
A simple rule: if one organization controls everything and everyone trusts it, blockchain is not needed. Blockchain is a tool for places with a **trust problem**.
:::

## Use by sector

| Sector | Use | Benefit |
|---|---|---|
| **Finance and payments** | international transfers, interbank settlement, stablecoins | faster and cheaper transfers, fewer intermediaries |
| **Supply chains** | tracking a product's path from producer to shop (cotton, food, medicines) | confirming origin, fighting counterfeits |
| **State registers** | land and real estate, licences, public procurement | an unalterable history, transparency, less corruption risk |
| **Education** | verifying diplomas and certificates | an employer checks a document's authenticity instantly |
| **Healthcare** | tracking medicine batches, consents and access logs | against counterfeit medicines; the data itself is not on the blockchain, only hashes and logs |
| **Energy** | electricity trading between solar panel owners (P2P) | selling surplus energy to neighbours |
| **Insurance** | parametric insurance — automatic payout based on weather data | fast payouts, less paperwork |

## Smart contract examples

A **smart contract** is a program on a blockchain that runs automatically when its conditions are met.

- **Farmer insurance**: "if there is no rain in the region for 30 days, pay the insured". The weather data comes from a trusted source (an **oracle**).
- **Escrow**: the buyer's money is "frozen" in the contract and goes to the seller once delivery is confirmed.
- **Rental**: if a payment does not arrive, a digital key (for example, to a car or an electronic lock) is switched off automatically.

:::warning
A bug in a smart contract also becomes "immutable": a program written to a blockchain is hard to fix. That is why contract code is audited and tested. If the oracle provides wrong data, the contract also works wrongly.
:::

## Tokenization and digital assets

A **token** is a digital unit on a blockchain that represents some value or right. **Tokenization** means dividing a real asset (real estate, a bond, a commodity) into tokens: for example, splitting one building into 1,000 shares and selling them to small investors. Types: payment tokens (cryptocurrencies, stablecoins), utility tokens, asset tokens, non-fungible tokens (NFTs — certificates, digital works).

A **central bank digital currency (CBDC)** is a digital form of the national currency; the central bank issues and manages it. Many countries (China, India, the European Union and others) are piloting one.

**In Uzbekistan**: the circulation of crypto-assets is regulated by law; licensed crypto exchanges and crypto shops operate, supervised by the authorized state body (the National Agency of Perspective Projects). Using unlicensed platforms is risky.

## Limitations and risks

- **Scalability and speed**: public blockchains are slower than centralized systems.
- **Energy use**: Proof of Work consensus requires a lot of electricity (Proof of Stake requires less).
- **Privacy and the "right to be forgotten"**: a record cannot be deleted — personal data should not be written to a blockchain.
- **"Garbage in, garbage out"**: blockchain does not guarantee that the entered data is correct, only that it has not been altered afterwards.
- **Financial risks**: sharp swings in cryptocurrency prices, fraudulent projects and "pyramids", losing the key means losing the asset.
- **Legal uncertainty**: regulation differs between countries.

## Case: a cotton-textile cluster

The cluster grows cotton, produces yarn and fabric and exports to Europe. Foreign buyers demand confirmation of where and under what conditions the cotton was grown (without child labour, with a set water consumption). The chain includes farmers, the cluster, a laboratory, customs, a logistics company and the buyer.

**Group tasks**:

1. Using the decision questions table: is blockchain needed here? Justify your answer.
2. What record does each participant in the chain add to the blockchain (for example, "batch 2026-145: 20 t, field 17, laboratory certificate hash ...")?
3. Which data should be stored on the blockchain and which in an ordinary database?
4. Which smart contract could be useful (for example, payment once customs confirms)?
5. List the risks: wrongly entered data, participants not taking part, costs.

:::warning
**Common mistakes**

- **Seeing blockchain as the solution to every problem.** In many cases an ordinary database is enough.
- **Thinking blockchain = cryptocurrency.** Cryptocurrency is only one use of blockchain.
- **Writing personal data to a blockchain.** It cannot be deleted.
- **Trusting unlicensed crypto platforms and promises of "guaranteed returns".** This is often fraud.
:::

## Summary

- Blockchain is useful where there are several independent parties and a trust problem between them.
- Sectors: finance, supply chains, state registers, education, healthcare, energy, insurance.
- Smart contracts automate processes but depend on the oracle and on code quality.
- Tokenization turns real assets into digital shares; in Uzbekistan crypto-assets are regulated through licensing.
- Limitations: speed, energy, privacy, quality of entered data, financial risks.

## Check yourself

:::check
Q: In which case is an ordinary database enough instead of blockchain?
A: When one organization maintains the data and all parties trust it.
Q: What benefit does blockchain bring to a supply chain?
A: It allows a product's origin and path to be tracked in an unalterable way and helps fight counterfeits.
Q: What is an oracle?
A: A trusted source that delivers external data (for example, weather or an exchange rate) to a smart contract.
Q: Why is it not recommended to write personal data to a blockchain?
A: A record on a blockchain cannot be deleted or changed.
:::

:::link https://www.weforum.org/topics/blockchain/
World Economic Forum: Blockchain
Research and cases on the use of blockchain in various sectors.
:::
