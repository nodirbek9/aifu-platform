---
title: Blockchain technology and ways of using it
summary: The concept of blockchain — a distributed ledger, a chain of blocks, linking through hashes; key properties — immutability, transparency, decentralization; consensus mechanisms (Proof of Work, Proof of Stake); public and permissioned (private, consortium) blockchains; smart contracts; cryptocurrencies and digital assets; uses in the economy — supply chains, finance and payments, verifying documents and diplomas, land registries, public procurement; the regulation of crypto-assets in Uzbekistan; advantages, limitations and risks; a hash chain in a program.
---

## Why does this topic matter?

Trust is expensive in the economy: contracts, notaries, banks, auditors are all needed to guarantee that "the data is correct and unaltered". **Blockchain** provides this trust through technology: records are kept in copies by many participants and cannot be changed unnoticed. Made famous by Bitcoin, the technology is now being tested in supply chains, finance, government registries and document verification.

:::note
**After this lesson you will be able to:**

- explain the concept of blockchain and how blocks are linked;
- distinguish consensus mechanisms and types of blockchain;
- know the concepts of a smart contract and a digital asset;
- name the areas of blockchain use in the economy;
- judge when a blockchain is useful and when it is excessive.
:::

## The concept of blockchain

A **blockchain** is a **distributed ledger** in which records (transactions) are grouped into blocks and each block contains **the hash value of the previous block**. A hash is a short "fingerprint" of data: if the data changes even slightly, the hash becomes completely different.

| Property | Meaning |
|---|---|
| **Distribution** | copies of the ledger are kept by many participants (nodes) |
| **Immutability** | changing a past record breaks all following blocks, and other nodes reject it |
| **Transparency** | participants see the history of transactions |
| **Decentralization** | no single central operator (in public blockchains) |
| **Cryptographic protection** | transactions are signed with digital signatures |

**Consensus** — how the network agrees which block is added:

| Mechanism | Meaning | Example |
|---|---|---|
| **Proof of Work** | heavy computation to add a block; a lot of electricity | Bitcoin |
| **Proof of Stake** | selection according to the funds staked; energy-efficient | Ethereum (since 2022) |
| **Permissioned consensus** | votes of known participants | corporate systems such as Hyperledger Fabric |

## Types of blockchain

| Type | Who participates | Use |
|---|---|---|
| **Public** | anyone | cryptocurrencies, open platforms |
| **Private** | one organization | internal accounting and audit |
| **Consortium** | several organizations (a group of banks or companies) | interbank settlements, supply chains |

## Smart contracts and digital assets

A **smart contract** is a program stored on the blockchain that runs **automatically** when conditions are met. Example: once the arrival of goods at the warehouse is confirmed, the payment automatically goes to the supplier — without intermediaries or delays.

**Digital assets**: cryptocurrencies (Bitcoin, Ether), stablecoins (pegged to a fiat currency), tokens (a share in an asset, for example real estate or bonds). Some central banks are exploring **central bank digital currency** (CBDC) projects. Cryptocurrency prices are highly volatile — as an investment they are high-risk.

**In Uzbekistan** the circulation of crypto-assets is regulated by law: crypto exchanges and other service providers must obtain a licence from the authorized state body; check the current requirements in official sources.

## Uses in the economy

| Area | Use | Value |
|---|---|---|
| **Supply chains** | the origin of goods and every stage (cotton → yarn → fabric) | protection against counterfeiting, transparent exports |
| **Finance** | international transfers, trade finance, tokenized assets | speed, fewer intermediaries |
| **Verifying documents** | storing hashes of diplomas, certificates, contracts | detecting a forged document quickly |
| **Government registries** | land and real estate, licences | records cannot be changed secretly |
| **Public procurement** | the history of tender stages | transparency |

## Worked example: a hash chain

For each record the block hash = *H*(previous hash | record); the previous hash of the first block is 00000000. For teaching purposes *H* is the 32-bit FNV-1a (SHA-256 in real blockchains). Supply chain records are entered (cotton, yarn, fabric from farmer to factory to mill to export); print every block.

```python
import sys


def fnv1a(matn):                                 # a teaching hash function (SHA-256 in a real system)
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return h


oldingi = "00000000"
yozuvlar = [s for s in sys.stdin.read().split("\n") if s.strip()]
for i, yozuv in enumerate(yozuvlar, start=1):
    xesh = format(fnv1a(oldingi + "|" + yozuv), "08x")   # the block is linked to the previous hash
    print(f"block {i}: {yozuv} | previous {oldingi} | hash {xesh}")
    oldingi = xesh
```
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    static int fnv1a(String matn) {                             // a teaching hash function (SHA-256 in a real system)
        int h = 0x811C9DC5;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = (h ^ (b & 0xFF)) * 16777619;
        return h;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        String oldingi = "00000000";
        int i = 0;
        while (in.hasNextLine()) {
            String yozuv = in.nextLine();
            if (yozuv.isBlank()) continue;
            i++;
            String xesh = String.format("%08x", fnv1a(oldingi + "|" + yozuv));   // the block is linked to the previous hash
            System.out.println("block " + i + ": " + yozuv + " | previous " + oldingi + " | hash " + xesh);
            oldingi = xesh;
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

uint32_t fnv1a(const string& matn) {                           // a teaching hash function (SHA-256 in a real system)
    uint32_t h = 2166136261u;
    for (unsigned char b : matn) h = (h ^ b) * 16777619u;
    return h;
}

int main() {
    string oldingi = "00000000", yozuv;
    int i = 0;
    while (getline(cin, yozuv)) {
        if (!yozuv.empty() && yozuv.back() == '\r') yozuv.pop_back();
        if (yozuv.find_first_not_of(' ') == string::npos) continue;
        i++;
        char buf[16];
        snprintf(buf, sizeof buf, "%08x", fnv1a(oldingi + "|" + yozuv));   // the block is linked to the previous hash
        string xesh = buf;
        cout << "block " << i << ": " << yozuv << " | previous " << oldingi << " | hash " << xesh << endl;
        oldingi = xesh;
    }
    return 0;
}
```

Input:

```text
Paxta 20t Fermer->Zavod
Ip 15t Zavod->Fabrika
Mato 9000m Fabrika->Eksport
```

Output:

```text
block 1: Paxta 20t Fermer->Zavod | previous 00000000 | hash 174beafb
block 2: Ip 15t Zavod->Fabrika | previous 174beafb | hash a6917dc3
block 3: Mato 9000m Fabrika->Eksport | previous a6917dc3 | hash d839ca87
```

Now, if the amount of cotton in the first record is changed from "20t" to "25t", the program prints:

```text
block 1: Paxta 25t Fermer->Zavod | previous 00000000 | hash 1483cdfe
block 2: Ip 15t Zavod->Fabrika | previous 1483cdfe | hash 2e0c2669
block 3: Mato 9000m Fabrika->Eksport | previous 2e0c2669 | hash f6e44f26
```

Even though the second and third records did not change, their hashes did — the last hash (d839ca87) no longer matches the copies of other participants, and the forgery is detected immediately. That is why it is enough for each participant in the supply chain to keep the last hash. A real blockchain adds consensus and digital signatures to this (in detail with proof of work in M9 of the KIA course).

## Advantages, limitations and risks

| Advantage | Limitation and risk |
|---|---|
| trust without intermediaries | low speed, sometimes high transaction costs |
| an unalterable history | even a wrong record cannot be "deleted" |
| transparency and auditability | conflict with personal data ("the right to be forgotten") |
| automation (smart contracts) | smart contract bugs have led to large losses |
| | the volatility of cryptocurrency prices, fraudulent projects |
| | **"garbage in, garbage out"**: a blockchain does not guarantee that the entered data is correct |

**When is a blockchain needed?** When several parties who do not fully trust each other must keep one shared ledger and do not want to hand it to a single centre. If one trusted organization manages the data, an ordinary database is cheaper and faster.

:::warning
**Common mistakes**

- **Seeing blockchain as a solution to any problem.** In many cases an ordinary database is enough.
- **Equating blockchain with cryptocurrency.** A cryptocurrency is just one use of blockchain.
- **Trusting crypto projects that promise "guaranteed returns".** This is often a sign of fraud.
- **Losing a private key or giving it to someone.** The digital asset is lost irreversibly.
:::

:::tip
Change the second record in the example program: which hashes change and which do not? Explain the result with a diagram of the chain of blocks.
:::

## Summary

- A blockchain is a distributed ledger whose blocks are linked through the previous block's hash; immutable, transparent, decentralized.
- Consensus: Proof of Work, Proof of Stake, permissioned; types: public, private, consortium.
- A smart contract runs automatically when conditions are met; digital assets are high-risk.
- Uses: supply chains, finance, document verification, registries, procurement.
- A blockchain is useful when parties who do not trust each other keep a shared ledger; otherwise an ordinary database is enough.

## Check yourself

:::check
Q: How is each block in a blockchain linked to the previous block?
A: Through the hash value of the previous block.
Q: What is a distributed ledger?
A: A ledger kept in copies by many participants.
Q: What is a program on a blockchain that runs automatically when conditions are met called?
A: A smart contract.
Q: When is a blockchain preferable to an ordinary database?
A: When several parties who do not fully trust each other must keep a shared ledger without a single centre.
:::

:::link https://www.investopedia.com/terms/b/blockchain.asp
Investopedia: Blockchain
An explanation of blockchain technology and its uses in the economy.
:::
