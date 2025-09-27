# Software Requirements Specification (SRS) for FocusAlarmy
**Date**: August 31, 2025

---

## Table of Contents
1. [Introduction](#1-introduction)
    1.1 [Purpose](#11-purpose)
    1.2 [Scope](#12-scope)
    1.3 [Assumptions](#13-assumptions)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [System Architecture](#4-system-architecture)
5. [Constraints and Testing](#5-constraints-and-testing)


## 1. Introduction

### 1.1 Purpose
This SRS defines the technical requirements for **FocusAlarmy**, a mobile alarm clock app for heavy sleepers, students, office workers, and ADHD users. Features include ultra-loud alarms, mission-based wake-up tasks, sleep tracking, and customizable routines.

### 1.2 Scope
FocusAlarmy is an **iOS (15.0+)** and **Android (10.0+)** app using **local processing** for alarms and missions, with optional **cloud sync** for sleep sounds. It leverages system alert windows, camera, storage, accelerometer, and Accessibility Service API to ensure robust wake-up and anti-snooze functionality.

### 1.3 Assumptions
- Users have smartphones with **accelerometers and cameras**.
- Minimum OS: **iOS 15.0**, **Android 10.0**.
- The **Internet is optional** for core features.

---

## 2. Functional Requirements

### Ultra-Loud Alarms
- Play alarms with **5+ preloaded sounds** (e.g., _"End of the World"_) up to **100 dB**.
- Support **MP3 uploads**.
- Persist across **device reboots**.

### Wake-Up Missions
- **Barcode/Photo Mission**: Verify a scanned barcode or photo matches a pre-registered item within **10 seconds**; prompt flash in low light.
- **Math Mission**: Generate random arithmetic problems (e.g., _12 × 7_) with **3 difficulty levels**; escalate after **2 failed attempts**.
- **Typing Mission**: Display a **10–20 word motivational quote** (50 preloaded, user-added option) requiring **95% typing accuracy**.
- **Motion Mission**: Detect **10 shakes or squats** via accelerometer (**2g force threshold**) within **30 seconds**.

### Sleep Tracking
- Track sleep via **accelerometer**.
- Calculate **duration** and **quality**.
- Display results as **graphs**.

### Wake-Up Check
- Restart missions if ignored for **120 seconds**.
- Escalate **alarm volume** and **lock screen** until mission completion.

### Anti-Uninstall
- Prompt confirmation during uninstall attempts via **Accessibility Service API**, with **user consent at setup**.

---

## 3. Non-Functional Requirements

- **Performance**: Missions shall load in **under 2 seconds** on devices with **4GB RAM**.
- **Reliability**: Alarms shall trigger **99.9% of the time**, including in **Do Not Disturb mode**.
- **Usability**: UI shall follow **WCAG 2.1 AA** contrast ratios, use **16pt+ fonts**, and provide **voice prompts** for ADHD accessibility.
- **Security**: User data (e.g., photos) shall be **encrypted locally** using **AES-256**.

---

## 4. System Architecture

- **Local storage**: SQLite for alarms and missions.
- **Optional cloud storage**: AWS S3 for sound downloads.

### APIs Used
- **Camera**: for barcode/photo recognition.
- **Accelerometer**: for motion detection.
- **Accessibility Service**: for anti-uninstall feature.

### Data Model Example
```plaintext
Alarm {
  ID,
  time,
  sound,
  mission_type
}
````

### UI Flow

* **Home Screen**: Alarm setup
* **Mission Screen**: Locks until completion

---

## 5. Constraints and Testing

### Constraints

* Core functionality works **offline**.
* Cloud is used only for **sound downloads**.
* Minimum OS: **iOS 15.0**, **Android 10.0**.

### Testing

* Target **95% mission success rate** under:

    * **Low battery**
    * **Poor lighting**
    * **Failed mission scenarios**

