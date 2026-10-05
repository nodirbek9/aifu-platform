---
title: Mastering the topics of the lectures and practical sessions
summary: A plan for studying "Computer Networks" independently; a weekly schedule by module; how to read lecture material; free tools for repeating the practical work at home (Packet Tracer, Wireshark, virtual machines); self-check questions; a glossary of key terms.
---

## Why it matters

Networking is a "layered" subject: you cannot understand routing without IP addressing, or HTTP and sockets without TCP. Miss one topic and the next ones get harder. The independent-study hours are set aside exactly for filling these gaps, repeating with your own hands what you saw in the lecture and turning knowledge into practical skill.

:::note
**After this independent work you will be able to:**

- draw up a personal weekly plan for mastering the subject;
- master each lecture with the active-reading method;
- repeat the practical work at home with free tools;
- test your own knowledge of each module with questions.
:::

## The structure of the subject and the study plan

| Week | Lecture | Practical | Key outcome of independent work |
|---|---|---|---|
| 1–2 | M1 Introduction and classification; M2 Topologies and devices | A1 Devices; A2 Topologies | a table of devices and topologies, a diagram of your home network |
| 3–4 | M3 The OSI model; M4 Transmission methods and media | A3 Emulators; A4 Encoding | drawing the OSI layers with their PDUs and devices from memory |
| 5–6 | M5 Ethernet and MAC; M6 Token Ring, FDDI, Wi-Fi | A5 Sniffers; A6 Wi-Fi | splitting a frame into fields in Wireshark |
| 7–8 | M7 TCP/IP and IP; M8 IPv4 addressing | A7 Masks and VLSM | solving 20 subnetting problems by hand |
| 9–10 | M9 ARP, ICMP, DHCP, routing; M10 TCP, UDP, sockets | A8 Routers; A9 Sockets | a three-router Packet Tracer diagram, an echo server |
| 11–12 | M11 HTTP, FTP, mail; M12 DNS | A10 Windows Server; A11 DHCP and DNS | HTTP analysis in the browser's F12, `nslookup` exercises |
| 13–14 | M13 Server OS and domains; M14 Security | A12 Users; A13 Shared resources; A14 Firewall and VPN | a server, users and shares in a VM |
| 15 | M15 Troubleshooting | A15 Utilities | a fault report |

Plan 2–3 hours of independent work a week: **1 hour** — rereading the lecture and making notes, **1 hour** — repeating the practical work, **0.5 hour** — the "Check yourself" questions and tests.

## Reading a lecture actively

1. **Survey** (5 minutes): the headings, tables, the "After this lesson you will be able to" list.
2. **Questions**: turn each section heading into a question ("How does TCP ensure reliability?").
3. **Read**: find the answer and write it briefly in your own words. Redraw the tables — the best way to memorise.
4. **Run the examples**: run the worked example code on a computer and try changing the input.
5. **Review**: after 1 day, 1 week and 1 month answer the "Check yourself" questions with the book closed.

## Tools for practice at home

| Tool | What for | Cost |
|---|---|---|
| **Cisco Packet Tracer** | topologies, routing, DHCP, VLANs | free (Netacad account) |
| **Wireshark** | packet analysis | free |
| **VirtualBox** / Hyper-V | Windows Server and Linux VMs | free |
| **Windows Server Evaluation** | sessions A10–A14 | free for 180 days |
| **Ubuntu Server** | Linux network services | free |
| The command line | `ipconfig`, `ping`, `tracert`, `nslookup`, `netstat` | built into the OS |

An important rule: experiment **only** on your own computer, your home network and your virtual lab. Scanning other networks or capturing their traffic is against the law.

## Glossary of key terms

| Term | Short explanation |
|---|---|
| Protocol | the rules of communication between the same layers |
| PDU | a layer's data unit: bit, frame, packet, segment, message |
| MAC address | a 48-bit physical address, layer 2 |
| IP address | a 32-bit (IPv4) or 128-bit (IPv6) logical address, layer 3 |
| Mask / prefix | separates the network and host parts of an address |
| Port | a 16-bit number identifying a program, layer 4 |
| Collision domain | a segment where signals can collide |
| Broadcast domain | the set of devices one broadcast reaches |
| Gateway | the router address for reaching other networks |
| DHCP / DNS | assigning addresses automatically / turning names into addresses |
| NAT | replacing private addresses with a public one |
| Firewall / VPN / IDS | filtering traffic / an encrypted tunnel / detecting attacks |

## Self-check by module

1. Name the seven OSI layers, each with its PDU and one protocol.
2. What do a hub, a switch and a router do with a frame or packet?
3. Explain the name 1000BASE-T. Why is the minimum Ethernet frame 64 bytes?
4. Find the network and broadcast addresses of 172.16.37.130/20 by hand.
5. Explain DHCP's DORA process and how an ARP request proceeds.
6. How do the seq and ack numbers change in the TCP three-way handshake?
7. Which protocols work between typing `https://aifu.uz` in a browser and the page opening? (DNS → TCP → TLS → HTTP.)
8. How do share and NTFS permissions work together?
9. Compare the roles of a firewall, a VPN and an IDS.
10. In the "the Internet isn't working" case, which commands do you run and in what order?

:::warning
**Common mistakes**

- **Only reading, without practice.** Networking is a practical subject: knowledge does not stick until you use `ping` and Wireshark yourself.
- **Leaving a missed topic "for later".** Without IP addressing the next 8 topics become incomprehensible.
- **Studying the night before the exam.** With review intervals (1 day, 1 week, 1 month) knowledge lasts.
- **Experimenting on other people's networks.** Only in your own lab.
:::

:::tip
At the end of each module make a one-page "cheat sheet": the key table, formulas and commands. At the end of the semester, 15 pages are the best exam preparation material.
:::

## Summary

- The subject has 15 lectures and 15 practical sessions; the topics build on each other — master them in order.
- Every week: active reading of the lecture, repeating the practical work, self-checking.
- Free tools: Packet Tracer, Wireshark, VirtualBox, Windows Server Evaluation, Ubuntu Server.
- Experiments only on your own computer and lab.

## Check yourself

:::check
Q: Why is it dangerous to skip the IP addressing topic?
A: Routing, DHCP, DNS, NAT, firewall rules and troubleshooting all rely on understanding IP addresses and masks.
Q: What is the first step of reading a lecture actively?
A: Surveying — looking over the headings, tables and the list of learning outcomes.
Q: How can Windows Server be tried at home for free?
A: By installing the Evaluation version (180 days) on a virtual machine in VirtualBox or Hyper-V.
Q: In what order do the protocols work when a page opens in a browser?
A: DNS (name to address) → TCP (connection) → TLS (encryption) → HTTP (request and response).
Q: On which networks may you scan and capture traffic?
A: Only your own, or with the owner's permission (a lab).
:::

:::link https://www.netacad.com/courses/networking-basics
Cisco Networking Academy: Networking Basics
A free online course on networking basics — an extra source for independent study.
:::
