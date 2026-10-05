---
title: Virtual private networks and their practical use (for example, CyberGhost or ExpressVPN)
summary: Types of VPN — corporate (remote access, site-to-site) and commercial personal VPN services (CyberGhost, ExpressVPN, Proton VPN and others); what a personal VPN protects against and what it does not; criteria for choosing a VPN provider — a no-logs policy and its independent audit, jurisdiction, protocols (WireGuard, OpenVPN, IKEv2), kill switch, DNS leak protection; the risks of free VPNs; practical work — installing and configuring a VPN and checking for leaks; complying with the law and organizational policy.
---

## Why does this topic matter?

In M14 we saw a VPN as a tool for protecting an organization's network. Today the word "VPN" often means something else — **commercial personal VPN services** whose advertising promises "complete anonymity". They can indeed be useful (for example, on public Wi-Fi), but their capabilities are often exaggerated. In this assignment we learn to use a VPN consciously — what it protects, how to choose a provider and how to check the configuration.

:::note
**After this assignment you will be able to:**

- explain the difference between corporate and personal VPNs;
- name the threats a personal VPN protects against and those it does not;
- choose a VPN provider using well-founded criteria;
- configure a VPN and check for IP and DNS leaks;
- know the legal and organizational boundaries of VPN use.
:::

## Corporate and personal VPNs

| | Corporate VPN | Personal (commercial) VPN |
|---|---|---|
| Purpose | connecting an employee securely to the organization's network | routing the user's traffic to the Internet through the provider's server |
| Who manages it | the organization's IT department | the VPN service company |
| Trust | in the organization | in the VPN provider — it now sees the metadata of your traffic |
| Examples | IPSec, SSL VPN, WireGuard, ZTNA (M14) | CyberGhost, ExpressVPN, Proton VPN, Mullvad and others |

## What a personal VPN protects against

| Protects against | Does not protect against |
|---|---|
| people on a public Wi-Fi network (café, airport) eavesdropping on your traffic | phishing sites and fake apps |
| your Internet provider (ISP) seeing which sites you visit | malware (a VPN is not an antivirus) |
| a site learning your real IP address | tracking through browser cookies, logins and "fingerprinting" |
| | a breach of a site or of your account |
| | the VPN provider itself — it now takes the ISP's place |

Since most sites now use HTTPS (TLS, M9), the content is already encrypted even on public Wi-Fi; a VPN additionally hides **which** sites you connect to (DNS queries, addresses). A promise of "100% anonymity" is advertising.

## Criteria for choosing a VPN provider

| Criterion | What to look at |
|---|---|
| **No-logs policy** | no logs of connections, IPs and activity are kept; above all — the results of an **independent audit** and confirmation in real events (court requests) |
| **Jurisdiction** | which country's laws the company is subject to, whether there are data retention obligations |
| **Protocols** | WireGuard, OpenVPN, IKEv2 — modern and proven; PPTP — outdated and weak |
| **Kill switch** | if the VPN drops, traffic does not leak into the open channel |
| **DNS and IPv6 leak protection** | DNS queries are directed to the provider's servers |
| **Independent security audits and open-source apps** | whether the app and infrastructure have been checked externally |
| **Transparency** | who the owner is, transparency reports, whether incidents are disclosed openly |
| **Payment and personal data** | how much personal data is required to sign up |

For example, CyberGhost and ExpressVPN are popular commercial services; they have published no-logs policies and report having had independent audits. Always check such claims against **the audit reports themselves** and their dates — an advertising page is not evidence. Also keep in mind that several well-known VPN brands may belong to one large company.

**The risks of free VPNs**: the service has to make money somehow — advertising, selling traffic data or tracking. Studies have shown that some free VPN apps do not encrypt traffic, request excessive permissions and even contain malicious code. Do not install from anywhere other than the official store and the vendor's website.

## Practical work

Before you start, check the policy of your organization (university, workplace) and the law of your country: using a personal VPN may be prohibited on some networks.

1. **Record the baseline**: without a VPN, visit ipleak.net or browserleaks.com — write down your IP address, country and DNS servers.
2. **Install a reliable VPN** (an audited service with a free plan, or a VPN provided by the university) — from the official website only.
3. **Configure it**: protocol — WireGuard or OpenVPN; **kill switch** enabled; DNS leak protection enabled; automatic connection on public Wi-Fi networks.
4. **Check**: with the VPN on, open ipleak.net again — the IP and DNS must belong to the VPN server, and your real IP and ISP DNS must not be visible. Check for WebRTC leaks too.
5. **Kill switch test**: disconnect the VPN — the Internet should stop temporarily.
6. **Compare speed**: run a speed test without and with the VPN; observe that distant servers are slower.
7. **Report**: a table of results (hide your real IP address in screenshots), a conclusion — which threats the VPN protected you from in your situation and which it does not.

:::warning
**Common mistakes**

- **Treating a VPN as "complete anonymity".** If you log in or cookies are kept, the site still recognizes you; and the VPN provider sees your traffic.
- **A free unknown VPN app.** Your data becomes the product.
- **Using it without a kill switch.** When the connection drops, traffic leaks in the open and you do not notice.
- **Taking a VPN for an antivirus or phishing protection.** Other tools do those jobs.
- **Ignoring the law and organizational policy.** An unauthorized VPN on a work network is a policy violation and bypasses security controls.
:::

:::tip
Sometimes the best "personal VPN" is one you run yourself: set up WireGuard on your home router or a small server, and on public Wi-Fi go out to the Internet through your home. In that case no third party sees your traffic.
:::

## Summary

- A corporate VPN connects to the organization's network; a personal VPN routes traffic to the Internet through the provider's server and moves trust from the ISP to the VPN provider.
- A personal VPN protects against eavesdropping on public Wi-Fi, ISP tracking and exposing your IP; not against phishing, malware or cookie tracking.
- Choosing a provider: an audited no-logs policy, jurisdiction, modern protocols, kill switch, DNS leak protection, transparency.
- A free unknown VPN is dangerous; install only from official sources.
- Check the configuration with tests such as ipleak, and comply with the law and organizational policy.

## Check yourself

:::check
Q: Who sees the metadata of your traffic when you use a personal VPN?
A: The VPN provider — it takes the place of the Internet provider.
Q: Why is a kill switch needed?
A: So that traffic does not leak through an unprotected channel when the VPN connection drops.
Q: Does a VPN protect against a phishing site?
A: No, a VPN protects the channel; it does not prevent entering a password on a fake site.
Q: How can a VPN provider's "we keep no logs" claim be checked?
A: Through independent audit reports, their dates and confirmations in real events (court requests).
:::

:::link https://ipleak.net/
ipleak.net
A free test page for checking IP, DNS and WebRTC leaks.
:::
