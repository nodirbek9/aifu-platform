---
title: Installing and initially configuring the Windows Server operating system
summary: Hardware requirements and preparing a virtual machine (Hyper-V, VirtualBox); the steps of installing Windows Server — choosing the edition and installation type, disk partitions; the first logon and Server Manager; initial configuration — computer name, static IP, DNS, time zone, updates, remote management; the PowerShell equivalents; adding roles.
---

## Why it matters

A server is an organisation's "heart": the user database, files, DHCP and DNS run on it. A server started off badly (a random name, an address from DHCP, an unpatched system) causes many problems later. In this session we install Windows Server on a virtual machine and prepare it for the roles of the following sessions (A11 — DHCP and DNS, A12 — users, A13 — shared resources).

:::note
**After this session you will be able to:**

- prepare a virtual machine for Windows Server;
- choose the right edition and installation type and install the system;
- give the server a meaningful name, a static IP address and DNS;
- perform the basic configuration with Server Manager and PowerShell;
- know the procedure for adding a role to a server.
:::

## Preparation

| Parameter | Minimum (Windows Server 2022) | Recommended for the lab |
|---|---|---|
| Processor | 1.4 GHz, 64-bit | 2 virtual cores |
| Memory | 512 MB (Server Core), 2 GB (Desktop Experience) | 4 GB |
| Disk | 32 GB | 60 GB |
| Network | 1 adapter | an internal or "Host-only" network |

A **virtual machine** (Hyper-V, VirtualBox or VMware) is used: there is no risk to the real computer, and you can take a **snapshot** of the state and go back after a mistake. The installation image (ISO) is the 180-day evaluation version from the Microsoft Evaluation Center.

Network mode: **NAT** — the VM reaches the Internet but is not visible from outside; **Internal/Host-only** — only the VMs and the host see each other (safe for testing a DHCP server — a second DHCP does not leak into the university network); **Bridge** — the VM is like a separate computer on the real network.

## Installation steps

1. Boot the VM from the ISO, choose the language and keyboard, **Install now**.
2. Choose the edition: **Windows Server 2022 Standard (Desktop Experience)** — with a graphical interface. Server Core — command line only.
3. Accept the licence terms, choose **Custom: Install Windows only**.
4. Disk: choose the unallocated space (the installer creates the system partitions itself).
5. After installation and reboots, the **Administrator** password is requested — give a complex one (upper and lower case, digits, symbols, at least 8 characters).
6. Logging on: in a VM use the hypervisor menu's key instead of **Ctrl+Alt+Del** (VirtualBox: Host+Del).

## Initial configuration

After logon **Server Manager** opens → **Local Server**. Configure each item in turn:

| Item | What to do | Why |
|---|---|---|
| **Computer name** | a meaningful name like `SRV-DC01`, then reboot | changing a random `WIN-8F3K…` name after installing roles is troublesome |
| **Ethernet** (IP) | static: `192.168.50.10/24`, gateway `192.168.50.1`, DNS `127.0.0.1` (if it will later be a DNS server) or the internal DNS | clients find the server by a permanent address |
| **Time zone** | (UTC+05:00) Tashkent | Kerberos and logs depend on time |
| **Windows Update** | install all updates | close known vulnerabilities |
| **Remote Desktop** | enable if needed, for administrators only | remote management |
| **IE Enhanced Security** | do not use a browser on the server | a server is not a desktop |

The same settings in **PowerShell** (as administrator):

```text
Rename-Computer -NewName "SRV-DC01" -Restart
Get-NetAdapter
New-NetIPAddress -InterfaceAlias "Ethernet" -IPAddress 192.168.50.10 -PrefixLength 24 -DefaultGateway 192.168.50.1
Set-DnsClientServerAddress -InterfaceAlias "Ethernet" -ServerAddresses 192.168.50.10
Set-TimeZone -Id "West Asia Standard Time"
Get-ComputerInfo | Select-Object CsName, WindowsProductName, OsVersion
```

On Server Core the same is done through the `sconfig` menu: 2 — name, 8 — network, 6 — updates, 7 — Remote Desktop.

## Adding roles

Server Manager → **Manage → Add Roles and Features**:

1. **Role-based or feature-based installation**.
2. Choose the server (from the pool).
3. Tick the roles — for example **DHCP Server**, **DNS Server** (in A11), **File and Storage Services** (in A13). Required extra components are offered automatically.
4. **Install**. After installation some roles show a yellow flag with "post-deployment configuration" — they need further configuration.

In PowerShell: `Install-WindowsFeature -Name DHCP, DNS -IncludeManagementTools`. To see what is installed: `Get-WindowsFeature | Where-Object Installed`.

## Checklist

- [ ] The computer name is meaningful and the machine has been rebooted.
- [ ] Static IP, mask, gateway and DNS are correct (`ipconfig /all`).
- [ ] The time zone and time are correct.
- [ ] Updates are installed.
- [ ] The Administrator password is complex and disclosed to nobody.
- [ ] The VM state is saved as a snapshot ("Clean install").

:::warning
**Common mistakes**

- **Leaving the server with an address from DHCP.** If the address changes, clients lose the server; a server's address is always static.
- **Renaming after roles are installed.** Especially on a domain controller this is a serious problem; the name is set first.
- **Connecting a test DHCP server to the university network (Bridge).** A rogue DHCP hands wrong addresses to every computer on the network; use an internal network in the lab.
- **Doing everyday work as Administrator and downloading programs from the Internet on the server.** A server is for services only.
- **Making a big change without a snapshot.** There is no way back after a mistake.
:::

:::tip
Install a Windows 10/11 client as a second VM and connect it to the same internal network. Give the client the address `192.168.50.20/24` and `ping` the server. If there is no reply, the cause is usually Windows Firewall blocking ICMP; check with `Get-NetFirewallRule -DisplayName "*Echo*"`.
:::

## Summary

- In the lab, Windows Server is installed on a virtual machine, in an internal network, with snapshots.
- The edition (Standard/Datacenter) and installation type (Desktop Experience/Server Core) are chosen in advance.
- Initial configuration: name → static IP and DNS → time zone → updates → remote management.
- Roles are added with **Add Roles and Features** in Server Manager or with `Install-WindowsFeature`.

## Check yourself

:::check
Q: Why is a server given a static IP address?
A: Clients must find it by a permanent address; a DHCP address may change.
Q: What is the main difference between Server Core and Desktop Experience?
A: There is no graphical interface — management is through the command line, `sconfig` and PowerShell; fewer resources and fewer vulnerabilities.
Q: Which VM network mode is safe for a test DHCP server?
A: Internal or Host-only — the DHCP messages do not reach the real network.
Q: Where is the add-role menu in Server Manager?
A: Manage → Add Roles and Features.
Q: How can the computer name be changed in PowerShell?
A: `Rename-Computer -NewName "SRV-DC01" -Restart`.
:::

:::link https://learn.microsoft.com/en-us/windows-server/get-started/install-windows-server
Install Windows Server
Microsoft's official guide to installing Windows Server.
:::
