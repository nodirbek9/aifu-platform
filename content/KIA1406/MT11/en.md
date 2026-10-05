---
title: The reasons for the spread of cybercrime. Face-based authentication and its features
summary: Economic, technological, social and legal reasons for the spread of cybercrime — low cost and high profit, anonymity and cryptocurrencies, the "crime as a service" market, the speed of digitalization and weak protection, lack of awareness, the difficulty of cross-border investigation; the crime triangle and prevention approaches; face authentication — 2D and 3D methods, processing steps, accuracy, liveness detection, fairness and privacy issues, legal requirements.
---

## Why does this topic matter?

Cybercrime is one of the fastest-growing types of crime in the world: the FBI's IC3 centre records hundreds of thousands of complaints and billions of dollars in losses every year, and in Uzbekistan too the number of cases related to card fraud and online scams has grown noticeably. Understanding why this is so is the basis for directing protection correctly. The second topic — face authentication — is now widely used in phones, banks and public services; knowing its strengths and weaknesses is also part of that protection.

:::note
**After this assignment you will be able to:**

- analyze the main reasons for the spread of cybercrime;
- propose prevention measures using the crime triangle model;
- explain how face authentication works;
- know the difference between 2D and 3D methods and liveness detection;
- name the privacy, fairness and legal issues of face recognition.
:::

## Reasons for the spread of cybercrime

| Reason | Meaning |
|---|---|
| **Low cost, high profit** | an attack needs no expensive equipment; one successful fraud or payment brings a large profit |
| **Anonymity and distance** | the criminal may be in another country and use fake accounts and cryptocurrencies |
| **"Crime as a service"** | ready-made tools, stolen data and attack infrastructure are sold as a paid "service" — people with little technical knowledge take part too |
| **Rapid digitalization** | online payments, public services, remote work — protection does not keep up |
| **Weak protection** | unpatched systems, reused passwords, no MFA (M10, the A3 cases) |
| **The human factor** | lack of awareness, trust, haste — social engineering relies on these |
| **Legal and investigative difficulties** | cross-border cases, the complexity of collecting evidence and extradition; victims often do not report |
| **Socio-economic factors** | unemployment, the temptation of easy money, not taking "virtual" crime seriously |

The **crime triangle** (motive, opportunity, rationalization) fits cybercrime too: motive is hard to reduce, but **opportunity** (technical protection, MFA, updates, payment confirmation) and **rationalization** (awareness, the certainty of law and punishment) can be reduced.

**Prevention approaches**: technical protection (M8–M14); digital literacy of the population and staff; fraud detection systems of banks and platforms; prompt reporting and investigation (in Uzbekistan — the internal affairs bodies and the bank, the Cybersecurity Center); international cooperation (the Budapest Convention, the UN Convention against Cybercrime). The legal foundations are covered in M3.

## Face authentication

**Processing steps**:

1. **Detection**: finding the face in the image.
2. **Alignment**: bringing the face into a standard position using points such as the eyes and nose.
3. **Feature extraction**: a deep neural network turns the face into a numeric vector — an **embedding** (for example, 128–512 numbers).
4. **Comparison**: a distance or similarity to the stored vector is computed and compared with a threshold (the FAR/FRR balance, as in MT12).

| Method | How | Features |
|---|---|---|
| **2D** (ordinary camera) | a colour image | cheap, on every device; can be fooled by a photo or video — liveness detection matters |
| **3D** (structured light, ToF) | a depth map of the face | resistant to photos and screens; for example, Apple states a FAR of ~1/1,000,000 for Face ID |
| **Infrared** | an IR image | works in the dark, distinguishes a picture on a screen |

**Liveness detection** (PAD — presentation attack detection): passive (analysis of texture, reflections, depth) or active (blinking, turning the head). A modern threat is **deepfake** videos and masks; that is why remote identification (opening a bank account online) requires certified PAD.

**Advantages**: contactless, fast, convenient. **Limitations**: lighting, angle, ageing, twins; masks and glasses; the error rate may differ between groups (NIST FRVT/FRTE tests have recorded demographic differences).

## Privacy, fairness and law

- **Consent and purpose**: unlocking a phone (the user chooses it, the template stays on the device) and mass surveillance in public places (without consent) are completely different matters.
- **Storage**: face templates are biometric personal data; under the Law "On Personal Data" a legal basis, protection and a retention period are required. In the European Union the GDPR and the AI Act strictly limit real-time identification in public spaces.
- **Fairness**: it must be tested that the system works equally accurately for people of different ages, genders and skin tones.
- **Human oversight**: important decisions (for example, an arrest) must not rest on an automatic match alone.

:::warning
**Common mistakes**

- **Treating cybercrime as "only a technical problem".** Its causes are also economic, social and legal.
- **Not reporting when victimized.** Prompt reporting (to the bank, law enforcement) increases the chance of recovering money and protecting others.
- **Using 2D face recognition as the only protection.** Without liveness detection it is fooled by a photo.
- **Collecting face images without consent.** This violates legal and ethical requirements.
- **Taking an automatic match as absolute truth.** Every biometric system has a probability of error.
:::

:::tip
For an essay take reliable statistics from the FBI IC3 annual reports, the ENISA Threat Landscape and official sources of Uzbekistan; for face recognition accuracy — from the results of NIST FRTE (Face Recognition Technology Evaluation).
:::

## Summary

- Reasons for the spread of cybercrime: low cost and high profit, anonymity, "crime as a service", rapid digitalization, weak protection, the human factor, investigative difficulties.
- The crime triangle: reducing opportunity and rationalization is the practical path to prevention.
- Face authentication: detection, alignment, embedding, comparison with a threshold.
- 3D and IR methods and liveness detection are needed against photos, videos and deepfakes.
- Privacy, consent, fairness and human oversight are mandatory conditions for using face recognition.

## Check yourself

:::check
Q: How does "crime as a service" affect the spread of cybercrime?
A: Because ready-made tools and services are sold, people with little technical knowledge can also join in.
Q: Which sides of the crime triangle can an organization influence?
A: Mainly opportunity (technical and organizational protection) and rationalization (awareness, the certainty of punishment).
Q: What is the main weakness of 2D face recognition?
A: It can be fooled by a photo or video; liveness detection is required.
Q: What is an "embedding" in a face recognition system?
A: A numeric vector that a neural network produces from a face image; comparison is done by the distance between such vectors.
:::

:::link https://www.ic3.gov/
FBI Internet Crime Complaint Center (IC3)
Annual reports and statistics on Internet crime.
:::
