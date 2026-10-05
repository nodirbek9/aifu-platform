---
title: Cloud computing and IoT security. Cloud systems, privacy issues, IoT architecture and threats
summary: Cloud computing models and shared responsibility; the main categories of cloud risk (misconfiguration, compromised accounts and keys, insecure interfaces, data leaks); protective measures — IAM, encryption and key management, CSPM and CASB, logs; privacy — data location, localization, personal data; IoT architecture (device, gateway and network, cloud, application); IoT threats and their causes; baseline security requirements (ETSI EN 303 645, OWASP IoT); network segmentation; a program auditing IoT devices.
---

## Why does this topic matter?

Organizations are moving more and more data and applications to the cloud, while offices and homes fill with cameras, thermostats, smart locks and sensors — **Internet of Things** (IoT) devices. What they have in common: the boundary of a system is no longer the organization's building. A security specialist must be able to control cloud settings and thousands of small devices.

:::note
**After this lesson you will be able to:**

- explain the shared responsibility model and the main categories of cloud risk;
- name the measures for protecting the cloud (IAM, encryption, CSPM, CASB, logs);
- analyse privacy issues in the cloud and IoT;
- know the layers of IoT architecture and the protective measures at each;
- audit IoT devices against baseline requirements.
:::

## Cloud computing and shared responsibility

Cloud service models — **IaaS**, **PaaS**, **SaaS** — and deployment models — **public**, **private**, **hybrid**, **community**. The basic rule of security is the **shared responsibility model**:

| Layer | IaaS | PaaS | SaaS |
|---|---|---|---|
| Physical infrastructure, virtualization | provider | provider | provider |
| Operating system, updates | customer | provider | provider |
| Application | customer | customer | provider |
| Data, access rights, settings | **customer** | **customer** | **customer** |

In every model, data, accounts and settings remain the customer's responsibility.

## The main categories of cloud risk

| Category | Meaning | Main protection |
|---|---|---|
| **Misconfiguration** | storage left publicly open, unnecessarily open ports, default settings | automatic configuration checks (CSPM), "closed by default" |
| **Compromised accounts and keys** | a stolen password, an API key left in code | MFA, a secrets vault, key rotation |
| **Excessive permissions** | a user or service has more rights than needed | least privilege, roles, regular reviews |
| **Insecure interfaces and APIs** | APIs without authentication or with weak protection | authentication, authorization, rate limits |
| **Data leaks** | unencrypted storage or backups | encryption at rest and in transit |
| **Lack of visibility** | not knowing who did what | centralized logs, SIEM (M14) |

## Measures for protecting the cloud

- **IAM** (identity and access management): a personal account for every employee, MFA, roles, temporary elevated rights, closing the account immediately when someone leaves.
- **Encryption**: in transit (TLS) and at rest; a **key management service** (KMS) and, if needed, **BYOK** (the customer's own key).
- **CSPM** (Cloud Security Posture Management) — continuously comparing cloud settings with the security policy and showing deviations.
- **CASB** (Cloud Access Security Broker) — monitoring employees' use of cloud services, detecting unauthorized services ("shadow IT"), applying DLP policies (M14).
- **Logs and monitoring**: all management actions are logged, with alerts on unusual activity.
- **Backups**: at least one copy in another account or region; recovery is tested regularly.

## Privacy issues

- **Data location**: in which country's servers the data is stored and which legislation applies.
- **Localization**: the personal data of citizens of Uzbekistan must be stored on servers within the country (the Law "On Personal Data").
- **Provider staff access**: under what conditions and with what logs.
- **Deleting data**: how and when data is deleted when the contract ends.
- **IoT and privacy**: cameras, microphones and location sensors collect data about private life — only necessary data should be collected, and the user should be informed and give consent.

## IoT architecture

| Layer | Composition | Protective measures |
|---|---|---|
| **Device (sensing)** | sensors, cameras, actuators, a microcontroller and firmware | a unique password, secure boot, signed updates, disabling unnecessary services |
| **Gateway and network** | Wi-Fi, Zigbee, Bluetooth LE, LoRaWAN, mobile networks, gateways | encrypted protocols, a separate network segment, a firewall |
| **Cloud platform** | device management, data collection and analytics | device certificates, IAM, API protection |
| **Application** | mobile and web apps, dashboards | authentication, MFA, secure sessions |

## IoT threats and their causes

IoT devices are often cheap, resource-constrained and run for years without updates. That is why the following **weaknesses** are widespread:

| Weakness | Why it is dangerous |
|---|---|
| **Default or weak passwords** | the same factory password on all devices — an outsider may take over control |
| **Firmware that is never updated** | known vulnerabilities stay open for years |
| **Unnecessary open services** | for example, an outdated, unencrypted telnet or an HTTP management panel |
| **Unencrypted communication** | data and passwords travel over the network in the clear |
| **Insecure cloud and app interfaces** | weak authentication, excessive data |
| **Physical access** | the device is in an open place, its ports unprotected |

**A historical lesson — Mirai** (2016): hundreds of thousands of cameras and routers whose default passwords had not been changed were taken over by malware, joined into a botnet and used in large DDoS attacks against major services. The main cause was not sophisticated technology but **unchanged factory passwords**. After this incident many countries began to introduce mandatory baseline requirements for IoT.

## Baseline security requirements

**ETSI EN 303 645** — the European standard for consumer IoT devices; its main requirements:

1. No universal default passwords — a unique password for each device or setting a password on first use.
2. A policy for receiving vulnerability reports.
3. Updating software securely and publishing the update support period.
4. Storing sensitive data (keys, passwords) securely.
5. Secure communication (encryption).
6. Minimizing the attack surface — disabling unnecessary services and ports.
7. Protecting personal data and letting the user delete it.

The **OWASP IoT Top 10** gives a list of the main risks in the same area. Some countries have made such requirements mandatory by law (for example, in the United Kingdom default passwords have been banned since 2024).

## Network segmentation

IoT devices should not be kept on the same network as the main work computers and servers. A **separate segment** (a VLAN or guest Wi-Fi) and firewall rules: the devices can reach only their own cloud service and cannot enter the internal network. Even if a device is compromised, the damage is limited to that segment (defence in depth and zero trust from M14).

## Worked example: auditing IoT devices

Each line describes a device: name, type, current firmware, latest firmware, default password (`yes`/`no`), open ports (comma-separated or `-`). A risk score is calculated against the baseline requirements: default password +5, outdated firmware +3, telnet (port 23) open +4, unencrypted HTTP management (port 80) +2. 7 or more is high, 3–6 medium, 0–2 low risk. Recommendations are printed for each device.

```python
import sys

for satr in sys.stdin.read().split("\n"):
    if not satr.strip():
        continue
    nom, _, joriy, oxirgi, standart, portlar = satr.split()
    portlar = [] if portlar == "-" else portlar.split(",")
    ball, tavsiya = 0, []
    if standart == "yes":                           # universal factory password
        ball += 5
        tavsiya.append("change the password")
    if joriy != oxirgi:                             # known vulnerabilities not fixed
        ball += 3
        tavsiya.append("update the firmware")
    if "23" in portlar:                             # telnet — unencrypted management
        ball += 4
        tavsiya.append("disable telnet")
    if "80" in portlar:                             # HTTP management panel
        ball += 2
        tavsiya.append("switch to HTTPS")
    if ball >= 7:
        daraja = "high"
    elif ball >= 3:
        daraja = "medium"
    else:
        daraja = "low"
    matn = ", ".join(tavsiya) if tavsiya else "no action needed"
    print(f"{nom}: score {ball}, {daraja} risk: {matn}")
```
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            String nom = in.next();
            in.next();
            String joriy = in.next(), oxirgi = in.next(), standart = in.next(), p = in.next();
            List<String> portlar = p.equals("-") ? new ArrayList<>() : Arrays.asList(p.split(","));
            int ball = 0;
            List<String> tavsiya = new ArrayList<>();
            if (standart.equals("yes")) { ball += 5; tavsiya.add("change the password"); }       // universal factory password
            if (!joriy.equals(oxirgi)) { ball += 3; tavsiya.add("update the firmware"); }        // known vulnerabilities not fixed
            if (portlar.contains("23")) { ball += 4; tavsiya.add("disable telnet"); }          // telnet — unencrypted management
            if (portlar.contains("80")) { ball += 2; tavsiya.add("switch to HTTPS"); }             // HTTP management panel
            String daraja = ball >= 7 ? "high" : ball >= 3 ? "medium" : "low";
            String matn = tavsiya.isEmpty() ? "no action needed" : String.join(", ", tavsiya);
            System.out.println(nom + ": score " + ball + ", " + daraja + " risk: " + matn);
        }
    }
}
```
```cpp
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

int main() {
    string nom, tur, joriy, oxirgi, standart, p;
    while (cin >> nom >> tur >> joriy >> oxirgi >> standart >> p) {
        vector<string> portlar;
        if (p != "-") {
            stringstream ss(p);
            string x;
            while (getline(ss, x, ',')) portlar.push_back(x);
        }
        auto bor = [&](const string& port) {
            for (auto& x : portlar) if (x == port) return true;
            return false;
        };
        int ball = 0;
        vector<string> tavsiya;
        if (standart == "yes") { ball += 5; tavsiya.push_back("change the password"); }     // universal factory password
        if (joriy != oxirgi) { ball += 3; tavsiya.push_back("update the firmware"); }      // known vulnerabilities not fixed
        if (bor("23")) { ball += 4; tavsiya.push_back("disable telnet"); }               // telnet — unencrypted management
        if (bor("80")) { ball += 2; tavsiya.push_back("switch to HTTPS"); }                  // HTTP management panel
        string daraja = ball >= 7 ? "high" : ball >= 3 ? "medium" : "low";
        string matn;
        for (size_t i = 0; i < tavsiya.size(); i++) matn += (i ? ", " : "") + tavsiya[i];
        if (tavsiya.empty()) matn = "no action needed";
        cout << nom << ": score " << ball << ", " << daraja << " risk: " << matn << "\n";
    }
    return 0;
}
```

Input:

```text
camera-01 camera 2.1.0 2.4.1 yes 23,80,554
thermostat thermostat 5.0.2 5.0.2 no 443
router-gw router 1.9 2.0 no 22,443
lock-door lock 3.3 3.3 no -
tv-lobby tv 7.2 7.2 yes 80
```

Output:

```text
camera-01: score 14, high risk: change the password, update the firmware, disable telnet, switch to HTTPS
thermostat: score 0, low risk: no action needed
router-gw: score 3, medium risk: update the firmware
lock-door: score 0, low risk: no action needed
tv-lobby: score 7, high risk: change the password, switch to HTTPS
```

The camera violates all four baseline requirements — it is fixed first or disconnected from the network until it is fixed. The audit result becomes the organization's IoT policy: a new device is connected to a separate segment only with a unique password, up-to-date firmware and unnecessary services disabled.

:::warning
**Common mistakes**

- **Thinking "if it's in the cloud, the provider protects it".** Settings and permissions are the customer's responsibility.
- **Considering an IoT device "small and safe".** It may be an entry point into the network.
- **Leaving the factory password.** The most common and most dangerous mistake.
- **Having no device inventory.** You cannot protect a device you do not know about.
- **Keeping IoT in the same segment as the work network.**
:::

:::tip
List all the "smart" devices in your home or office (router, camera, TV, printer) and audit them in the format of the example.
:::

## Summary

- In the cloud, shared responsibility: data, accounts and settings always stay with the customer.
- The main cloud risks are misconfiguration, compromised accounts, excessive permissions and weak APIs; the measures are IAM, encryption and KMS, CSPM, CASB, logs and backups.
- Privacy: data location, localization, the personal data collected by IoT sensors.
- IoT layers: device, gateway and network, cloud, application; the main weaknesses are default passwords, firmware that is never updated, unnecessary services.
- Baseline requirements (ETSI EN 303 645, OWASP IoT), segmentation and regular audits are the foundation of IoT protection.

## Check yourself

:::check
Q: What is the customer responsible for even in the SaaS model?
A: Data, access rights and accounts, and sharing settings.
Q: What does CSPM do?
A: It continuously compares cloud settings with the security policy and detects deviations.
Q: What was the main cause of the Mirai incident?
A: Factory passwords on IoT devices that had not been changed.
Q: Why are IoT devices placed in a separate network segment?
A: So that even if a device is compromised, the damage is limited to that segment and the internal network is protected.
:::

:::link https://www.etsi.org/technologies/consumer-iot-security
ETSI: Consumer IoT Security
The EN 303 645 standard and recommendations for consumer IoT devices.
:::
