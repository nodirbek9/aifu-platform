---
title: Introduction. The history of computer networks. Classification of computer networks
summary: The notion of a computer network and its purposes; the history from terminal systems to ARPANET and the Internet; classifying networks by area (PAN, LAN, MAN, WAN), by management (client–server, peer-to-peer), by transmission medium and by switching method; network characteristics.
---

## Why it matters

Today almost no computer works alone: a phone sends a message, a banking app contacts its server, a university platform sends a student an assignment. All of this happens through a **computer network**. Knowing how a network is built is needed by a programmer, a system administrator and an information security specialist alike: why a site opens slowly, why a printer cannot be found, by what route and how securely data arrives — only someone who knows the basics of networks can answer these.

In this introductory lecture we look at the notion of a network, how networks developed and how they can be classified.

:::note
**After this lesson you will be able to:**

- define a computer network and its main purposes;
- list the main stages in the development of computer networks;
- classify networks by area, management method, transmission medium and switching type;
- explain a network's main characteristics (bandwidth, latency, reliability).
:::

## What is a computer network?

A **computer network** is a set of computers and other devices joined by communication channels to exchange data and share resources.

| Purpose of a network | Example |
|---|---|
| Exchanging data | e-mail, messengers, sending files |
| Sharing resources | one printer, a shared disk, an Internet connection |
| Centralised management | user accounts, backups |
| Remote services | websites, cloud computing, online learning |

Every network has three components: **nodes** (computers, servers, phones, printers), **communication channels** (cable, radio, optical fibre) and **protocols** — the rules of data exchange.

## History

| Period | Stage | Content |
|---|---|---|
| 1950s–1960s | Terminal systems | simple terminals (a screen and keyboard) are connected to one large mainframe; all computation is central |
| 1969 | ARPANET | a US Department of Defense project: 4 university computers were joined in the first **packet-switched** network |
| 1970s | Local networks | Ethernet (1973, Xerox) — joining the computers within one building |
| 1974–1983 | TCP/IP | V. Cerf and R. Kahn proposed the protocol stack; on 1 January 1983 ARPANET switched to TCP/IP — the "birthday" of the Internet |
| 1989–1991 | The World Wide Web | T. Berners-Lee (CERN) created web pages, HTTP and HTML |
| 1990s | The commercial Internet | providers, e-mail and the web opened to everyone |
| 2000s | Broadband and wireless | ADSL, optics, Wi-Fi, 3G |
| 2010s – today | Mobile and cloud | 4G/5G, cloud services, the Internet of Things (IoT) |

The key turning point is **packet switching**: data is split into small pieces (packets) and each is sent across the network independently. This is far more economical than the permanent connection of telephone networks, and if one line breaks, packets take another route.

## Classifying networks

### By area

| Kind | Full name | Coverage | Example |
|---|---|---|---|
| PAN | Personal Area Network | a few metres | a phone and wireless earbuds (Bluetooth) |
| LAN | Local Area Network | a room, a building, a campus | a university computer lab |
| MAN | Metropolitan Area Network | a city | a city provider's optical ring |
| WAN | Wide Area Network | a country, a continent, the whole world | the Internet, a bank's branch network |

### By management method

| Kind | Feature | Advantage | Drawback |
|---|---|---|---|
| **Client–server** | services are provided by dedicated servers, clients request them | centralised management and security | if the server fails, the service stops |
| **Peer-to-peer** | every computer is both a client and a server | simple, no server needed | hard to manage as it grows |

### By transmission medium

- **Wired**: twisted pair, coaxial cable, optical fibre.
- **Wireless**: radio (Wi-Fi, Bluetooth, mobile networks), infrared, satellite.

### By switching method

| Method | Idea | Example |
|---|---|---|
| Circuit switching | a permanent channel is reserved between two parties for the whole call | the traditional telephone network |
| Packet switching | data is split into packets, the channel is shared with others | the Internet |
| Message switching | a message is received completely and then passed on | the old telegraph, the principle of e-mail |

## Network characteristics

| Characteristic | Meaning | Unit |
|---|---|---|
| **Bandwidth** | the amount of data transmitted per unit of time | bit/s (Kbit/s, Mbit/s, Gbit/s) |
| **Latency** | the time for data to travel from the source to the receiver | milliseconds (ms) |
| **Reliability** | the probability of working without errors and interruptions | per cent, the share of lost packets |
| **Scalability** | the ability to increase the number of nodes | — |
| **Security** | protecting data from unauthorised access | — |

Note: network speed is measured in **bits**, while file size is often in **bytes**. A 100 Mbit/s channel transfers about 12.5 MB per second (1 byte = 8 bits).

:::warning
**Common mistakes**

- **Taking the Internet and the World Wide Web for the same thing.** The Internet is a network of networks; the web is one of the services running over it (mail, messengers and file sharing use the Internet too).
- **Mixing up Mbit/s and MB/s.** 100 Mbit/s ≠ 100 MB/s; the difference is a factor of 8.
- **Thinking of a LAN only as a "small" network.** A campus LAN may cover thousands of devices; the classification is about area and management, not the number of devices.
- **Confusing latency with bandwidth.** Even a wide channel can have a large latency over a long distance (say, via satellite).
:::

:::tip
Classify the networks around you: home Wi-Fi, the university computer lab, mobile Internet, the Bluetooth between your phone and your watch. Which area type, management method and transmission medium does each belong to?
:::

## Summary

- A computer network is devices joined to exchange data and share resources; it consists of nodes, channels and protocols.
- Development: terminal systems → ARPANET and packet switching → Ethernet → TCP/IP and the Internet → the web → mobile and cloud networks.
- Classification: area (PAN, LAN, MAN, WAN), management (client–server, peer-to-peer), medium (wired, wireless), switching (circuit, packet, message).
- The main characteristics: bandwidth, latency, reliability, scalability, security.

## Check yourself

:::check
Q: Which event is considered the "birthday" of the Internet?
A: ARPANET switching to the TCP/IP protocols on 1 January 1983.
Q: How does packet switching differ from circuit switching?
A: No permanent channel is reserved: data is split into packets that are sent independently, sharing the channel with others.
Q: What kind of network is a city provider's optical ring?
A: A MAN — a city-scale network.
Q: Roughly how many megabytes per second does a 50 Mbit/s channel transfer?
A: 50 / 8 ≈ 6.25 MB.
Q: What is the main drawback of a peer-to-peer network?
A: As the number of nodes grows, managing it and ensuring security become hard.
:::

:::link https://www.internetsociety.org/internet/history-internet/brief-history-internet/
Internet Society: A Brief History of the Internet
A historical overview written by the founders of the Internet.
:::
