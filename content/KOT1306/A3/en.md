---
title: Network emulators and how to use them
summary: The difference between a simulator and an emulator; Cisco Packet Tracer, GNS3, EVE-NG, Mininet and other tools; the Packet Tracer interface — devices, cables, real-time and simulation modes; building a first network, assigning IP addresses and checking with ping; the basics of configuring a device from the command line.
---

## Why it matters

Real routers and switches are expensive, and a lab has only a few of them. A configuration mistake can bring a real network down. That is why network engineers first try a new design **inside a computer** — in a simulator or emulator. We will use an emulator in the later practical sessions of this course too (routing, DHCP, VLANs), so today we master its main features.

:::note
**After this session you will be able to:**

- explain the difference between a simulator and an emulator;
- name the main tools (Packet Tracer, GNS3, EVE-NG, Mininet) and what each is suited for;
- place devices in Packet Tracer and connect them with the right cables;
- assign IP addresses to computers and check connectivity with `ping`;
- follow a packet's path in simulation mode.
:::

## Simulator and emulator

| | **Simulator** | **Emulator** |
|---|---|---|
| What it does | **models** a device's behaviour (a simplified program) | runs the device's **real operating system** (for example, a Cisco IOS image) in a virtual machine |
| Commands | only the supported set of commands | all the commands of the real device |
| Resources | few (runs on an ordinary laptop) | many (every device is a separate virtual machine) |
| Example | Cisco Packet Tracer | GNS3, EVE-NG |
| When | learning, studying concepts | preparing for certification, testing a real project |

## The main tools

| Tool | Kind | Feature |
|---|---|---|
| **Cisco Packet Tracer** | simulator | free (with a Cisco Networking Academy account), the most convenient for learning; shows every packet in simulation mode |
| **GNS3** | emulator | open source; connects real IOS, Juniper, MikroTik images and virtual machines |
| **EVE-NG** | emulator | a lab server used through the browser; images from many vendors |
| **Mininet** | emulator | hundreds of virtual hosts and switches on Linux; for SDN research |
| **Wireshark** | traffic analyser | does **not** model a network — it captures and analyses packets from a real or emulated one |

## The Packet Tracer interface

- **Workspace** (Logical Workspace) — where the diagram is drawn.
- **Device panel** (bottom left) — Network Devices (routers, switches, hubs, wireless devices), End Devices (PC, Laptop, Server, Printer), Connections (cables).
- **Cables**: straight-through (Copper Straight-Through), crossover (Copper Cross-Over), console, fibre, serial (Serial DCE/DTE). The lightning icon is "choose the cable automatically".
- **Modes** (bottom right): **Realtime** — the network runs in real time; **Simulation** — time stops, every packet is shown as an envelope and stepped through.
- **Device window** (click a device): Physical (adding modules), Config (graphical configuration), Desktop (for a computer: IP Configuration, Command Prompt, Web Browser), CLI (the command line of a router or switch).

Link lights: **green** — the link is up, **orange** — the port is starting up or blocked by STP, **red** — no link (the port is shut down or the cable is wrong).

## Practical work 1: a first network

1. Place one **2960** switch and three **PCs** in the workspace.
2. Connect each PC to the switch with a **straight-through cable** (PC: FastEthernet0 → Switch: Fa0/1, Fa0/2, Fa0/3). Wait for the lights to turn green.
3. On each PC set the address in *Desktop → IP Configuration*:

```text
PC0   IP 192.168.10.1   Mask 255.255.255.0
PC1   IP 192.168.10.2   Mask 255.255.255.0
PC2   IP 192.168.10.3   Mask 255.255.255.0
```

4. On PC0 open *Desktop → Command Prompt* and check connectivity:

```text
C:\> ping 192.168.10.3

Pinging 192.168.10.3 with 32 bytes of data:
Reply from 192.168.10.3: bytes=32 time<1ms TTL=128
Reply from 192.168.10.3: bytes=32 time<1ms TTL=128
...
```

The first request sometimes ends with "Request timed out" — at that moment the computer is finding the receiver's MAC address through ARP.

## Practical work 2: simulation mode

1. Switch to **Simulation** mode. In *Edit Filters* leave only **ICMP** and **ARP**.
2. Ping PC2 from PC0 again, or use the "Add Simple PDU" (closed envelope) icon on the top bar and click PC0 and then PC2.
3. Press **Capture/Forward** and follow the packets step by step: first the ARP broadcast goes through the switch to all PCs, then the reply comes back only to PC0, then the ICMP packets travel only through the needed ports.
4. Click any envelope — the **OSI Model** tab shows how the packet was handled at each layer, and **Inbound/Outbound PDU Details** shows the header fields.

## Practical work 3: meeting the command line

Click the switch and go to the **CLI** tab:

```text
Switch> enable                         enter privileged mode
Switch# show mac-address-table         the MAC table (filled after a ping)
Switch# configure terminal             configuration mode
Switch(config)# hostname SW1           rename the device
SW1(config)# exit
SW1# show running-config               the current configuration
SW1# copy running-config startup-config   save the configuration
```

Cisco device modes are hierarchical: user mode (`>`), privileged mode (`#`), global configuration (`(config)#`) and interface configuration (`(config-if)#`). Commands need not be typed in full — abbreviations such as `conf t` and `sh run` work, **Tab** completes a command and `?` gives help.

:::warning
**Common mistakes**

- **Taking Wireshark for an emulator.** Wireshark only captures and analyses traffic — it does not create a network.
- **Closing a diagram without saving it.** Save Packet Tracer work as a `.pkt` file; the settings inside a device are saved with `copy running-config startup-config`, otherwise they are lost when the device reloads.
- **Giving addresses from different subnets.** Computers 192.168.10.1 and 192.168.20.2 cannot `ping` each other without a router.
- **Staying in simulation mode.** In Simulation mode time stops — unless you press Capture/Forward, `ping` looks as if "nothing replies".
:::

:::tip
Ready-made activities built with Packet Tracer's **Activity Wizard** (`.pka` files) are graded automatically. Do the exercises of the "Getting Started with Cisco Packet Tracer" course on Netacad to master the interface faster.
:::

## Summary

- A simulator models a device's behaviour (Packet Tracer), an emulator runs the real device software (GNS3, EVE-NG).
- Packet Tracer: the device panel, cable types, Realtime and Simulation modes, Desktop and CLI in the device window.
- A first network: a switch, PCs, straight-through cables, IP addresses in one subnet and `ping`.
- Simulation mode shows the path of ARP and ICMP packets and the headers at each layer.

## Check yourself

:::check
Q: What is the main difference between a simulator and an emulator?
A: A simulator reproduces a device's behaviour with a simplified model, while an emulator runs the real device's operating system in a virtual environment.
Q: Which mode is needed in Packet Tracer to follow packets step by step?
A: Simulation mode (with the Capture/Forward button).
Q: Why does the first `ping` request sometimes give "Request timed out"?
A: The sender first finds the receiver's MAC address through ARP, and the first request is delayed meanwhile.
Q: Which commands take you into configuration mode on a Cisco switch?
A: `enable`, then `configure terminal`.
Q: What does it mean when the port lights are red?
A: There is no link — the port is shut down or the wrong cable was chosen.
:::

:::link https://www.netacad.com/courses/getting-started-cisco-packet-tracer
Getting Started with Cisco Packet Tracer
A free introductory Packet Tracer course from Cisco Networking Academy.
:::
