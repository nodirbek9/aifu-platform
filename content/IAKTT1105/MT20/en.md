---
title: The main types of cloud technologies
summary: Independent study — "everything as a service" (XaaS) — beyond IaaS, PaaS and SaaS: CaaS (containers), FaaS (serverless), DBaaS, DaaS (virtual desktops), AIaaS; the underlying technologies — virtualization, containers and Kubernetes; types of cloud storage (block, file, object); edge computing; multi-cloud and hybrid strategies; pricing models (on-demand, reserved, spot, serverless); a program comparing on-demand and reserved prices; an assignment.
---

## Purpose of this independent study

In M11 we got to know the NIST characteristics of the cloud, the three main service models (IaaS, PaaS, SaaS) and the deployment models. In this independent study we look in more depth at the **full "menu"** of cloud services, the technologies underneath them and the **pricing models** — for an economist, the choice of pricing model is exactly where large savings come from.

:::note
**After this independent study you will be able to:**

- name the types of cloud services beyond IaaS, PaaS and SaaS;
- explain the difference between virtualization and containers;
- choose a storage type to suit the task;
- compare pricing models and calculate the break-even point.
:::

## "Everything as a service" (XaaS)

| Model | What the provider supplies | What the customer manages | Example |
|---|---|---|---|
| **IaaS** | virtual servers, disks, network | the OS, programs, data | Amazon EC2, Azure VM |
| **CaaS** (Containers) | a platform for running containers | container images | Kubernetes services (EKS, AKS, GKE) |
| **PaaS** | an environment for running applications | application code and data | Heroku, Google App Engine |
| **DBaaS** | a managed database | tables and queries | Amazon RDS, Azure SQL |
| **FaaS** (serverless) | running a function on an event | only the function code | AWS Lambda, Azure Functions |
| **SaaS** | a ready-made program | settings and data | Microsoft 365, Google Workspace, cloud CRM |
| **DaaS** (Desktop) | a virtual desktop | the user's work | Windows 365, Azure Virtual Desktop |
| **AIaaS** | ready artificial intelligence models and APIs | requests and data | speech recognition, translation, language model APIs |

From the bottom up, the provider takes on more of the work, while the customer manages less and has less flexibility.

## The underlying technologies

**Virtualization** — several virtual machines (VMs) run on one physical server with the help of a **hypervisor**; each has its own operating system. This is the foundation of the cloud: the provider divides physical resources among many customers.

**Containers** (Docker) — an application and its libraries are packed into one "box"; containers share the OS kernel, so they are lighter than VMs and start faster. **Kubernetes** automatically manages thousands of containers (starting, scaling, recovering from failures).

| | Virtual machine | Container |
|---|---|---|
| Start-up | minutes | seconds |
| Size | gigabytes | megabytes |
| Isolation | strong (a separate OS) | lighter (a shared kernel) |
| Use | legacy applications, different OSs | microservices, modern applications |

## Types of cloud storage

| Type | Structure | Use |
|---|---|---|
| **Block** | a virtual disk attached to a server | databases, the OS disk |
| **File** | folders and files, a shared network drive | departmental documents, shared files |
| **Object** | a file + metadata + a unique address, unlimited scale | backups, archives, images and videos, data lakes (MT9) |

Object storage has "classes": frequently used data costs more, while a rarely used archive (for example, accounting documents kept for 7 years) is much cheaper but slower to restore.

## Edge computing and multi-cloud

- **Edge computing** — processing data close to its source (a factory, a shop, a base station): low latency (MT5) and less network traffic. Example: cameras in a shop analyse customer flow on site and send only statistics to the cloud.
- **Multi-cloud** — using several providers: reduces dependence on one provider but makes management more complex.
- **Hybrid cloud** (M11) — confidential data on one's own servers or in a national data centre, the rest in the public cloud (the localization requirement in S11).

## Pricing models

| Model | Meaning | When it pays |
|---|---|---|
| **On-demand** | per hour or second used | short or uneven workloads, testing |
| **Reserved** (a 1–3 year commitment) | a large discount in exchange for an advance commitment | servers that run constantly |
| **Spot** (spare capacity) | very cheap, but the provider can stop it at any moment | calculations that tolerate interruption |
| **Serverless** (per request) | only for the functions executed and their time | rare, irregularly called tasks |
| **Subscription** (SaaS) | monthly per user | office, CRM, accounting |

## Worked example: on-demand or reserved?

A virtual server costs 1,500 soums an hour on demand, or a fixed 650,000 soums a month when reserved. Each line holds a server name and how many hours a month it runs. Determine the break-even point and the cheaper option for each server.

```python
import sys

SOAT_NARXI = 1500       # on-demand, soums/hour
REZERV = 650000         # reserved, soums/month (regardless of hours)

chegara = (REZERV + SOAT_NARXI - 1) // SOAT_NARXI    # round up
print(f"Break-even point (hours): {chegara}")
for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, soat = satr.split()
    soat = int(soat)
    talab = soat * SOAT_NARXI
    tanlov = "on-demand" if talab <= REZERV else "reserved"
    print(f"{nom}: {soat} h, on-demand {talab}, reserved {REZERV} -> {tanlov}")
```
```java
import java.util.Scanner;

public class Main {
    static final long SOAT_NARXI = 1500;      // on-demand, soums/hour
    static final long REZERV = 650000;        // reserved, soums/month (regardless of hours)

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        long chegara = (REZERV + SOAT_NARXI - 1) / SOAT_NARXI;   // round up
        System.out.println("Break-even point (hours): " + chegara);
        while (in.hasNext()) {
            String nom = in.next();
            long soat = in.nextLong();
            long talab = soat * SOAT_NARXI;
            String tanlov = talab <= REZERV ? "on-demand" : "reserved";
            System.out.println(nom + ": " + soat + " h, on-demand " + talab + ", reserved " + REZERV + " -> " + tanlov);
        }
    }
}
```
```cpp
#include <iostream>
#include <string>
using namespace std;

const long long SOAT_NARXI = 1500;      // on-demand, soums/hour
const long long REZERV = 650000;        // reserved, soums/month (regardless of hours)

int main() {
    long long chegara = (REZERV + SOAT_NARXI - 1) / SOAT_NARXI;    // round up
    cout << "Break-even point (hours): " << chegara << "\n";
    string nom;
    long long soat;
    while (cin >> nom >> soat) {
        long long talab = soat * SOAT_NARXI;
        string tanlov = talab <= REZERV ? "on-demand" : "reserved";
        cout << nom << ": " << soat << " h, on-demand " << talab << ", reserved " << REZERV << " -> " << tanlov << "\n";
    }
    return 0;
}
```

Input:

```text
test-server 120
report-server 400
crm-server 500
web-server 730
```

Output:

```text
Break-even point (hours): 434
test-server: 120 h, on-demand 180000, reserved 650000 -> on-demand
report-server: 400 h, on-demand 600000, reserved 650000 -> on-demand
crm-server: 500 h, on-demand 750000, reserved 650000 -> reserved
web-server: 730 h, on-demand 1095000, reserved 650000 -> reserved
```

For a server running more than 434 hours a month (~14 hours a day), reserving pays off. The constantly running web server (730 hours — the whole month) saves 445,000 soums a month, 5.3 million soums a year, when reserved. The test server is switched on only during working hours and is cheap on demand. A reservation is a commitment: it is paid even if the server is later no longer needed, so the workload must be analysed in advance (FinOps in S11).

:::warning
**Common mistakes**

- **One model for everything.** Reserved for constant workloads, on-demand or serverless for uneven ones.
- **Not switching off unused resources.** A "forgotten" on-demand server eats money every month.
- **Keeping an archive in an expensive storage class.** Move rarely used data to a cheap class.
- **Thinking containers and VMs are the same.** Their isolation and security requirements differ.
:::

## Assignment

1. Guess which XaaS models three services you know (for example, Telegram, a bank app, a university LMS) might rely on.
2. Modify the example program: if the reserved price is 500,000 soums, how does the break-even point change?
3. Which storage type and class should be chosen for a company's backups (50 GB, restored once a month) and for its daily accounting database? Justify.
4. In one provider's pricing calculator (AWS, Azure, Google Cloud), compare the on-demand and 1-year reserved prices of one virtual server.

## Summary

- XaaS: IaaS, CaaS, PaaS, DBaaS, FaaS, SaaS, DaaS, AIaaS — the provider's share of the work grows from the bottom up.
- The foundation: virtualization (VMs) and containers (Docker, Kubernetes).
- Storage: block, file, object; cheap classes for archives.
- Edge computing, multi-cloud and hybrid are directions of modern architecture.
- Pricing models: on-demand, reserved, spot, serverless, subscription; the choice is made through the break-even point.

## Check yourself

:::check
Q: How does a container differ from a virtual machine?
A: A container shares the OS kernel, so it is lighter and starts faster; a VM has its own OS and stronger isolation.
Q: What do you pay for in the serverless (FaaS) model?
A: Only for the number of function executions and their duration.
Q: Which storage type suits backups?
A: Object storage, with a cheap class for rarely used data.
Q: What is the risk of the reserved pricing model?
A: It is a commitment — it is paid even if the resource is no longer needed.
:::

:::link https://calculator.aws/
AWS Pricing Calculator
A calculator for estimating the cost of cloud services.
:::
