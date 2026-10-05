---
title: Server operating systems and workgroups. Server operating systems and their roles. Workgroups and domain systems
summary: The difference between server and workstation operating systems; Windows Server and Linux server distributions; server roles and services; the workgroup (peer-to-peer) and domain (client–server) models; Active Directory — domain, domain controller, OU, group policies, forest and tree; centralised authentication; the Linux alternatives.
---

## Why it matters

At home, creating a separate user on each of three or four computers is no problem. But for 1000 computers and 5000 students at a university, you cannot open a separate account on every machine: a student must be able to log in to any lab with their own login and password, and the administrator must manage the password policy, printers and programs from one place. **Server operating systems** and the **domain** model make this possible.

:::note
**After this lesson you will be able to:**

- explain the difference between server and workstation operating systems;
- name the main server roles and services;
- compare the workgroup and domain models and justify when each fits;
- explain the structure of Active Directory (domain, domain controller, OU, group policies).
:::

## Server operating systems

| | Workstation OS (Windows 11, Ubuntu Desktop) | Server OS (Windows Server, Ubuntu Server, RHEL) |
|---|---|---|
| Purpose | one user's work | serving many clients |
| Simultaneous connections | limited (e.g. 20 shared-folder connections) | thousands |
| Graphical interface | central | optional (Server Core, command line only) |
| Hardware | 1 processor, ordinary memory | many processors, large RAM, RAID, ECC memory |
| Reliability | reboots are normal | continuous operation, clustering, backups |
| Licence | cheap | by cores or by number of clients (CAL) |

**Windows Server** (2016, 2019, 2022, 2025) editions: **Standard** (up to 2 virtual machines), **Datacenter** (unlimited virtualisation). Installation types: **Desktop Experience** (graphical) and **Server Core** (no GUI, fewer resources and a smaller attack surface). Management — **Server Manager**, **PowerShell**, **Windows Admin Center**.

**Linux servers**: Ubuntu Server, Debian, Red Hat Enterprise Linux (RHEL), Rocky/AlmaLinux. A large share of the web servers on the Internet run Linux. Management — SSH and the command line.

## Server roles

| Role | Purpose | Windows | Linux |
|---|---|---|---|
| Directory service | the database of users and computers, logins | Active Directory Domain Services | Samba AD, FreeIPA, OpenLDAP |
| DHCP | assigning addresses automatically | DHCP Server | isc-dhcp, Kea |
| DNS | resolving names | DNS Server | BIND, Unbound |
| File server | shared folders | File and Storage Services (SMB) | Samba, NFS |
| Print server | shared printers | Print Services | CUPS |
| Web server | sites and applications | IIS | Nginx, Apache |
| Virtualisation | virtual machines | Hyper-V | KVM |
| Remote access | VPN, remote desktop | RRAS, RDS | OpenVPN, WireGuard |

Many roles can run on one physical server, but good practice is to keep each important role on its own (often virtual) server.

## Workgroup and domain

| | **Workgroup** | **Domain** |
|---|---|---|
| Model | peer-to-peer | client–server |
| Accounts | separate on each computer (local) | in a central database (on the domain controller) |
| Logging in | to this computer only | to any computer in the domain with one login |
| Management | by hand on each computer | centrally, with group policies |
| Size | up to ~10–20 computers | hundreds and thousands |
| Server | not needed | at least one domain controller (two is better) |
| Cost | free | servers and licences |

A workgroup suits a home or small office: simple, but changing a password on 10 computers is done 10 times. A domain suits organisations: centralised security and management.

## Active Directory

**Active Directory** (AD) is Microsoft's directory service: a hierarchical database of all objects in the network (users, groups, computers, printers).

- **Domain** — a group of objects with a shared database and security policy, with a DNS name: `aifu.local`.
- **Domain controller** (DC) — a server that stores the AD database and authenticates users. Several DCs **replicate** with each other — if one fails, another works.
- **OU** (Organizational Unit) — "folders" inside a domain: `Students`, `Teachers`, `Computer labs`. Policies are linked to them and management rights are delegated.
- **Group policies** (GPO) — settings applied centrally: password length, screen lock, installing programs, connecting printers, blocking USB.
- **Tree** and **forest** — domains joined by trust relationships: `aifu.local`, `talaba.aifu.local`.
- The authentication protocol is **Kerberos** (tickets), directory access is **LDAP** (port 389). AD depends heavily on DNS: clients find the DC through SRV records in DNS.

Joining a computer to a domain: system properties → "Domain" → `aifu.local` → a domain administrator's login and password → reboot. After that you can log in to the computer as `AIFU\talaba01`.

## In practice: a university network

| Element | Solution |
|---|---|
| Student and teacher accounts | the `Students` and `Teachers` OUs in AD |
| Password policy | a domain GPO: at least 10 characters, changed every 90 days |
| Computer labs | a `Labs` OU, a GPO to install programs and restrict USB |
| Files | a file server, a shared folder per group, rights through groups |
| Addresses and names | DHCP and DNS servers (configured in A11) |

:::warning
**Common mistakes**

- **Thinking a domain and a workgroup are the same.** A workgroup has no central database — each computer keeps its own accounts.
- **Running with a single domain controller.** If it fails, nobody can log in; at least two DCs are recommended.
- **Giving clients an external DNS (8.8.8.8).** Domain computers then cannot find the DC; clients must be given the internal DNS server.
- **Granting rights to each user separately.** Rights are granted to groups, and users are added to groups.
- **Doing everyday work on a server with the administrator account.** The administrator account is for management only.
:::

:::tip
On Windows, find out whether your computer is in a workgroup or a domain with `systeminfo | findstr /B "Domain"` (or the system properties). On a university computer run `whoami` and `echo %USERDOMAIN%` to see the domain name.
:::

## Summary

- A server OS is designed to serve many clients without interruption; Windows Server (Desktop Experience and Server Core) and Linux servers are widespread.
- The main roles: directory service, DHCP, DNS, file and print, web, virtualisation, remote access.
- A workgroup is a small peer-to-peer network with local accounts; a domain has central accounts and management.
- Active Directory: domain, domain controllers and replication, OU, GPO, tree and forest; it relies on Kerberos, LDAP and DNS.

## Check yourself

:::check
Q: What is the advantage of the Server Core installation type?
A: There is no graphical interface — it uses fewer resources, needs fewer updates and has a small attack surface.
Q: Where are user accounts stored in a workgroup?
A: On each computer itself (local accounts).
Q: What does a domain controller do?
A: It stores the Active Directory database and authenticates domain users.
Q: What is the difference between an OU and a group?
A: An OU is a container for organising objects and applying policies; a group is a set of users for granting rights.
Q: Why must domain computers use the internal DNS server?
A: They find the domain controller through SRV records in DNS; an external DNS has no such records.
:::

:::link https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/get-started/virtual-dc/active-directory-domain-services-overview
Active Directory Domain Services overview
Microsoft's official overview of AD DS.
:::
