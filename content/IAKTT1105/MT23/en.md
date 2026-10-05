---
title: The working principle of blockchain
summary: Independent study — the life cycle of a transaction (creation, digital signature, broadcasting, the waiting pool, inclusion in a block, confirmations); block structure — the header (previous hash, Merkle root, timestamp, difficulty, nonce) and the transactions; the Merkle tree and short proofs; consensus mechanisms — Proof of Work, Proof of Stake, agreement in permissioned networks; difficulty adjustment; chain forks and finality; a program showing the Merkle root and proof of work; an assignment.
---

## Purpose of this independent study

The example in M12 showed how blocks are linked into a chain through hashes. But a real blockchain raises more questions: who confirms a transaction and how, how do thousands of transactions in a block become "one hash", and how do participants who do not know each other reach agreement? This independent study shows **the internal working principle** of blockchain step by step.

:::note
**After this independent study you will be able to:**

- explain the path of a transaction from creation to confirmation;
- name the elements of a block header;
- show why the Merkle tree is needed;
- compare Proof of Work and Proof of Stake.
:::

## The life cycle of a transaction

1. **Creation**: Ali wants to transfer 500 units to Vali; his wallet program creates a transaction.
2. **Signing**: the transaction is signed with Ali's **private key** (S14). Anyone can verify the signature with Ali's public key, but nobody can create a forged transaction in Ali's name.
3. **Broadcasting**: the transaction is sent to the network's nodes; each node checks the signature and the balance.
4. **The waiting pool** (mempool): transactions that pass the checks wait to be included in a block; those paying a higher fee are usually included sooner.
5. **Inclusion in a block**: the block producer (a miner or validator) gathers transactions into a block and proposes it to the network according to the consensus rules.
6. **Confirmations**: as new blocks are added on top, changing the transaction becomes practically impossible. On Bitcoin 6 confirmations are usually considered enough.

## Block structure

| Part | Element | Role |
|---|---|---|
| **Header** | previous block hash | linking into the chain (M12) |
| | Merkle root | a single "fingerprint" of all transactions in the block |
| | timestamp | when the block was created |
| | difficulty | the target for proof of work (PoW) |
| | nonce | a number chosen to satisfy the difficulty condition |
| **Body** | the list of transactions | signed transfers |

## The Merkle tree

Transactions are hashed in pairs, the results are hashed in pairs again — at the top a single **Merkle root** remains:

```text
                 root = H(H12 + H34)
                /                    \
        H12 = H(H1 + H2)        H34 = H(H3 + H4)
         /         \              /         \
   H1 = H(tx1)  H2 = H(tx2)  H3 = H(tx3)  H4 = H(tx4)
```

**Why is it needed?**

- If one transaction changes, the root changes too — the block header protects the whole body.
- **A short proof**: to check that a transaction is in a block, you need not all the transactions but only the "neighbouring" hashes along the path — just ~20 hashes for 1,000,000 transactions (the log n idea from MT10). This is why phone wallets work without downloading the whole blockchain.

## Consensus mechanisms

| Mechanism | Who creates a block | What security rests on | Drawback |
|---|---|---|---|
| **Proof of Work** | the miner who first solves a hard computational puzzle | most of the computing power being in honest hands | a great deal of electricity |
| **Proof of Stake** | a randomly chosen validator, in proportion to the funds they have staked | validators fear losing their stake (it is cut if rules are broken) | large holders have more influence |
| **Permissioned agreement** (BFT type) | approved participants vote | more than a certain share of participants being honest | a limited number of participants |

**The idea of proof of work**: the hash of the block header must satisfy a certain condition (for example, a number of leading zeros). Since the hash cannot be predicted, there is no way but to try nonces one by one. When the required number of zeros increases by one, the average number of attempts grows 16-fold (in hexadecimal). Verification, however, takes a single hash calculation — **hard to create, easy to verify**.

**Difficulty adjustment**: on Bitcoin the difficulty is changed every 2,016 blocks so that a block is created on average every 10 minutes — if the network's power rises, so does the difficulty.

## Worked example: the Merkle root and proof of work

Each line holds one transaction. Calculate the Merkle root, then for the header `previous|root|nonce` find the smallest nonce whose hash starts with 1, 2 and 3 zeros. For teaching purposes the FNV-1a hash function from M12 is used (real systems use SHA-256).

```python
import sys

def fnv1a(matn):                                     # a teaching hash function (SHA-256 in real systems)
    h = 2166136261
    for b in matn.encode("utf-8"):
        h = ((h ^ b) * 16777619) & 0xFFFFFFFF
    return format(h, "08x")

tranzaksiyalar = [s.strip() for s in sys.stdin.read().split("\n") if s.strip()]
daraja = [fnv1a(t) for t in tranzaksiyalar]
while len(daraja) > 1:
    if len(daraja) % 2 == 1:
        daraja.append(daraja[-1])                    # if odd, the last one is repeated
    daraja = [fnv1a(daraja[i] + daraja[i + 1]) for i in range(0, len(daraja), 2)]
ildiz = daraja[0]
print(f"Merkle root: {ildiz}")

for q in range(1, 4):                                # proof of work: q leading zeros
    nonce = 0
    while True:
        xesh = fnv1a(f"00000000|{ildiz}|{nonce}")
        if xesh.startswith("0" * q):
            break
        nonce += 1
    print(f"difficulty {q}: nonce {nonce}, hash {xesh}, attempts {nonce + 1}")
```
```java
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String fnv1a(String matn) {                       // a teaching hash function (SHA-256 in real systems)
        long h = 2166136261L;
        for (byte b : matn.getBytes(StandardCharsets.UTF_8)) h = ((h ^ (b & 0xff)) * 16777619L) & 0xFFFFFFFFL;
        return String.format("%08x", h);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> daraja = new ArrayList<>();
        while (in.hasNextLine()) {
            String s = in.nextLine().trim();
            if (!s.isEmpty()) daraja.add(fnv1a(s));
        }
        while (daraja.size() > 1) {
            if (daraja.size() % 2 == 1) daraja.add(daraja.get(daraja.size() - 1));   // if odd, the last one is repeated
            List<String> keyingi = new ArrayList<>();
            for (int i = 0; i < daraja.size(); i += 2) keyingi.add(fnv1a(daraja.get(i) + daraja.get(i + 1)));
            daraja = keyingi;
        }
        String ildiz = daraja.get(0);
        System.out.println("Merkle root: " + ildiz);

        for (int q = 1; q <= 3; q++) {                       // proof of work: q leading zeros
            long nonce = 0;
            String xesh;
            while (true) {
                xesh = fnv1a("00000000|" + ildiz + "|" + nonce);
                if (xesh.startsWith("0".repeat(q))) break;
                nonce++;
            }
            System.out.println("difficulty " + q + ": nonce " + nonce + ", hash " + xesh + ", attempts " + (nonce + 1));
        }
    }
}
```
```cpp
#include <cstdint>
#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

string fnv1a(const string& matn) {                           // a teaching hash function (SHA-256 in real systems)
    uint64_t h = 2166136261ULL;
    for (unsigned char b : matn) h = ((h ^ b) * 16777619ULL) & 0xFFFFFFFFULL;
    char buf[16];
    snprintf(buf, sizeof buf, "%08llx", (unsigned long long) h);
    return buf;
}

int main() {
    vector<string> daraja;
    string s;
    while (getline(cin, s)) {
        size_t a = s.find_first_not_of(" \t\r"), b = s.find_last_not_of(" \t\r");
        if (a != string::npos) daraja.push_back(fnv1a(s.substr(a, b - a + 1)));
    }
    while (daraja.size() > 1) {
        if (daraja.size() % 2 == 1) daraja.push_back(daraja.back());   // if odd, the last one is repeated
        vector<string> keyingi;
        for (size_t i = 0; i < daraja.size(); i += 2) keyingi.push_back(fnv1a(daraja[i] + daraja[i + 1]));
        daraja = keyingi;
    }
    string ildiz = daraja[0];
    cout << "Merkle root: " << ildiz << "\n";

    for (int q = 1; q <= 3; q++) {                           // proof of work: q leading zeros
        long long nonce = 0;
        string xesh;
        while (true) {
            xesh = fnv1a("00000000|" + ildiz + "|" + to_string(nonce));
            if (xesh.compare(0, q, string(q, '0')) == 0) break;
            nonce++;
        }
        cout << "difficulty " << q << ": nonce " << nonce << ", hash " << xesh << ", attempts " << nonce + 1 << "\n";
    }
    return 0;
}
```

Input:

```text
Ali->Vali 500
Vali->Sardor 200
Sardor->Kamola 150
Kamola->Ali 50
```

Output:

```text
Merkle root: b9802d65
difficulty 1: nonce 210, hash 0b3c9c4a, attempts 211
difficulty 2: nonce 231, hash 00380dcb, attempts 232
difficulty 3: nonce 4394, hash 0005b4b7, attempts 4395
```

Observations:

- On average ~16 attempts are expected for 1 zero, ~256 for 2 and ~4,096 for 3. This time 1 zero took 211 attempts — bad luck: proof of work is a random process; only the **average** difficulty is known in advance.
- Try changing the first transaction to "Ali->Vali 900": the Merkle root changes to `2b924064` and all the nonces change to completely different values. Forging one transaction requires "mining" the block again — and all the blocks after it as well.
- For a verifier who knows the nonce, a single hash calculation is enough.

## Chain forks and finality

If two miners find a block at the same time, the chain temporarily **forks**. Nodes choose the chain with the most work (usually the longest); the block on the shorter branch is discarded and its transactions return to the pool. That is why finality in PoW is **probabilistic**: as confirmations accumulate, the probability of a transaction being reversed falls sharply. In BFT agreement in permissioned networks finality is **immediate**: once a block is accepted, it is not reversed.

The system's security rests on one key condition: **most** of the computing power (PoW) or the stake (PoS) must be held by honest participants. That is why small networks are weaker than large ones.

:::warning
**Common mistakes**

- **Thinking "a miner can forge a transaction".** A miner cannot create a transaction without the signature; it only chooses the order and what to include.
- **Treating one confirmation as final.** Several confirmations are awaited for large amounts.
- **Confusing the Merkle root with a simple hash of a list.** The tree allows short proofs.
- **Seeing PoS as "free security".** It too rests on economic incentives and stakes.
:::

## Assignment

1. Test the example with 5 transactions. What happens at a level with an odd count? Draw the Merkle tree by hand.
2. On average, how many attempts are expected for 4 zeros? Check it in the program and measure the time.
3. Draw the path of one transaction from creation to 6 confirmations as a diagram (the BPMN or flowchart from MT11).
4. Compare PoW and PoS in a table by energy, security, centralization risk and cost of entry.

## Summary

- A transaction: creation → signature → broadcasting → pool → block → confirmations.
- The block header: previous hash, Merkle root, timestamp, difficulty, nonce.
- The Merkle tree protects all transactions with one root and enables short proofs.
- Consensus: PoW (computation), PoS (stake), permissioned BFT; proof of work is hard to create and easy to verify.
- In PoW finality is probabilistic, in BFT immediate; security rests on an honest majority.

## Check yourself

:::check
Q: Who signs a transaction, and with which key?
A: The sender, with their own private key; others verify it with the sender's public key.
Q: What is the Merkle root?
A: A single hash obtained by repeatedly hashing all the transactions in a block in pairs.
Q: Why is proof of work "hard to create, easy to verify"?
A: Finding a nonce that satisfies the condition takes many attempts, while checking it takes a single hash calculation.
Q: Why does a validator act honestly in Proof of Stake?
A: If it breaks the rules, it loses the funds it has staked.
:::

:::link https://bitcoin.org/bitcoin.pdf
Bitcoin: A Peer-to-Peer Electronic Cash System
Satoshi Nakamoto's original paper describing the working principles of blockchain.
:::
