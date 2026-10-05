---
title: Network technologies and Internet services
summary: The concept and economic importance of computer networks; network types — PAN, LAN, MAN, WAN, the Internet; topologies; network devices; communication channels — optical fibre, copper cable, Wi-Fi, mobile communication (4G, 5G), satellite; basic concepts of IP addresses, DNS and domains, protocols (TCP/IP, HTTP/HTTPS); Internet services — the web, email, cloud storage, messengers, video conferencing, online payments and trade; corporate networks and intranets; economic criteria for choosing a channel (speed, reliability, price, SLA).
---

## Why does this topic matter?

Today almost every economic process goes through a network: online cash register receipts to the tax service, payments to the bank, orders to the warehouse, reports to the manager. If the Internet goes down, sales and settlements stop. An economist does not need to know how networks work at an engineer's level, but should know the basic concepts, Internet services and the criteria for choosing a communication channel.

:::note
**After this lesson you will be able to:**

- name the types and topologies of networks;
- know the main network devices and communication channels;
- explain the concepts of IP address, DNS, domain and protocol in plain words;
- show the use of Internet services in the economy with examples;
- choose a communication channel by economic criteria.
:::

## The concept and types of networks

A **computer network** is a set of devices connected to exchange data and share resources (printers, files, the Internet, programs).

| Type | Coverage | Example |
|---|---|---|
| **PAN** (personal) | a few metres | a phone and wireless earphones (Bluetooth) |
| **LAN** (local) | one room, building, campus | an office network, a university computer lab |
| **MAN** (metropolitan) | a city | a city operator's network |
| **WAN** (wide) | countries, continents | a network connecting bank branches |
| **The Internet** | the whole world | a network of networks |

**Topologies**: "star" (all devices connect to a central switch — the most widespread; if one cable breaks, only one computer is disconnected), "bus" and "ring" (historical), "mesh" (several paths — reliable, used in backbone networks).

## Network devices and communication channels

| Device | Role |
|---|---|
| **Network card** | connects a computer to the network (wired or Wi-Fi) |
| **Switch** | connects devices in a local network |
| **Router** | connects different networks, for example the office network with the Internet |
| **Wi-Fi access point** | wireless connection |
| **Modem / ONT** | connection to the provider's line (ONT for optical fibre) |

| Channel | Speed | Features |
|---|---|---|
| **Optical fibre** | from hundreds of Mbit/s to Gbit/s | the fastest and most stable, for businesses and homes |
| **Copper cable** (Ethernet, ADSL) | 1 Gbit/s in an office; ADSL low | ADSL is becoming obsolete |
| **Wi-Fi** | hundreds of Mbit/s | convenient, but depends on walls and interference |
| **Mobile** (4G LTE, 5G) | tens to hundreds of Mbit/s | on the move, as a backup channel |
| **Satellite** | varies | remote areas, latency (lower in new low-orbit systems) |

## Internet basics in plain words

- An **IP address** is a device's "postal address" on the network (for example `192.168.1.10` or an IPv6 address).
- A **domain name** is a human-friendly name (`soliq.uz`); **DNS** turns it into an IP address — like a "phone book".
- A **protocol** is a set of rules for exchanging data: TCP/IP (delivering data), HTTP/HTTPS (web pages; HTTPS — encrypted), SMTP/IMAP (mail).
- A **provider** (ISP) is a company providing Internet access.
- **Speed** is in Mbit/s; **latency** (ping) in milliseconds — important for video calls and online trading.

## Internet services and the economy

| Service | Economic use |
|---|---|
| **Websites and portals** | a company website, an online shop, the public services portal |
| **Email** | official correspondence, invoices, messages to customers |
| **Cloud storage and office** | co-editing files, backups (M11) |
| **Messengers** (Telegram and others) | fast contact with customers, orders, bots |
| **Video conferencing** (Zoom, Teams, Meet) | remote meetings, negotiations, online learning |
| **Online payments and banking** | Internet banking, Click, Payme, QR payments (M13) |
| **E-commerce and marketplaces** | online sales, delivery (M13) |
| **Search and online advertising** | attracting customers, marketing |

**Corporate networks**: an **intranet** is an internal portal for staff only (documents, news, applications); an **extranet** gives partners limited access; branches are connected through a secure channel (VPN).

## Economic criteria for choosing a channel

| Criterion | Questions |
|---|---|
| **Speed** | is it enough for the number of staff, cloud services, video calls? |
| **Reliability** | the provider's guarantee — the **SLA** (for example 99.9% uptime), the time to fix faults |
| **Price** | the monthly fee, connection and equipment costs |
| **Backup channel** | a second provider or mobile Internet — so that sales do not stop |
| **Static IP, support** | is it needed for a server or camera system, is there 24/7 support? |

An example calculation: is a backup mobile channel for 300 thousand soums a month expensive? If the main channel is down 2 days a year and the shop sells 5 million soums a day, the loss is 10 million soums, while the backup channel costs 3.6 million soums a year — the backup is more profitable.

:::warning
**Common mistakes**

- **Confusing megabits and megabytes.** 100 Mbit/s ≈ 12.5 MB/s (M2).
- **Choosing a provider by price only.** Outages and slow support lead to losses — check the SLA.
- **Working without a backup channel.** Risky for a business that depends on online cash registers and payments.
- **Doing confidential work on public Wi-Fi.** Without HTTPS and a VPN, data can be intercepted.
:::

:::tip
Measure the Internet speed and latency at home or at the university with speedtest.net or a similar service; compare the result with the speed the provider promises.
:::

## Summary

- A network is devices connected to share data and resources; types are PAN, LAN, MAN, WAN, the Internet.
- Devices: network card, switch, router, Wi-Fi access point; channels: optical fibre, cable, Wi-Fi, mobile, satellite.
- An IP address is a device address, DNS turns a domain into an IP, HTTPS is the encrypted web.
- Internet services: the web, mail, cloud, messengers, video conferencing, online payments and trade.
- Choosing a channel: speed, reliability (SLA), price, a backup channel, support.

## Check yourself

:::check
Q: What is the difference between a LAN and a WAN?
A: A LAN is a local network within one building or campus; a WAN is a wide network covering cities and countries.
Q: What does DNS do?
A: Turns a domain name (for example soliq.uz) into an IP address.
Q: How does a router differ from a switch?
A: A switch connects devices inside a local network; a router connects different networks, for example the office with the Internet.
Q: What is an SLA?
A: The provider's service quality guarantee — the uptime percentage and the time to fix faults.
:::

:::link https://www.itu.int/en/ITU-D/Statistics/Pages/default.aspx
ITU: ICT Statistics
Internet and communication indicators by country.
:::
